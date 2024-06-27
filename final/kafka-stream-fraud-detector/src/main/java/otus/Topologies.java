package otus;

import dto.*;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import serdes.*;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

interface Topologies {

    Logger logger = LoggerFactory.getLogger(StreamApp.class);

    KeycloakJsonSerde<KeycloakDto> keycloakJsonSerde = new KeycloakJsonSerde<>();
    IpJsonSerde<IpDto> fraudIpSerde = new IpJsonSerde<>();
    IpIUCJsonSerde<IpIUCDto> fraudIpIUCSerde = new IpIUCJsonSerde<>();
    LocationJsonSerde<LocationDto> fraudLocationSerde = new LocationJsonSerde<>();
    Serde<String> stringSerde = Serdes.String();

    static Topology getTopologyMaliciousUNF(String inputTopic, String outputTopic, long unf_count, long unf_minutes) {
        StreamsBuilder builder = new StreamsBuilder();
        builder
                .stream(inputTopic, Consumed.with(Serdes.String(), keycloakJsonSerde))
                .filter((k, v) -> v.getType().equals("LOGIN_ERROR"))
                .filter((k,v) -> v.getError().equals("user_not_found"))
                .map((k,v) -> new KeyValue<>(v.getIpAddress(), v))
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(unf_minutes)))
                .count()
                .filter((k, count) -> count > unf_count)
                .toStream()
                .filter((k,v) -> v != null )
                .map((k, v) -> new KeyValue<>(String.format("%s@<%s,%s>",k.key(), k.window().startTime(), k.window().endTime()),
                        new IpDto(k.key(),v,k.window().startTime().toString(),k.window().endTime().toString())))
                .peek((k,v) -> logger.info("Send: {key:[{}], value:[{}]}", k, v))
                .to(outputTopic, Produced.with(stringSerde, fraudIpSerde ));

        return builder.build();
    }

    static Topology getTopologyMaliciousIUC(String inputTopic, String outputTopic, long iuc_count, long iuc_minutes) {
        StreamsBuilder builder = new StreamsBuilder();
        builder
                .stream(inputTopic, Consumed.with(Serdes.String(), keycloakJsonSerde))
                .filter((k, v) -> v.getType().equals("LOGIN_ERROR"))
                .filter((k,v) -> v.getError().equals("invalid_user_credentials"))
                .map((k,v) -> new KeyValue<>(String.format("%s:%s:%s",v.getUserId(),v.getRealmId(),v.getIpAddress()), v))
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(iuc_minutes)))
                .count()
                .filter((k, count) -> count > iuc_count)
                .toStream()
                .filter((k,v) -> v != null )
                .map((k, v) -> new KeyValue<>(String.format("%s@<%s,%s>",k.key(), k.window().startTime(), k.window().endTime()),
                        new IpIUCDto(k.key(),v,k.window().startTime().toString(),k.window().endTime().toString())))
                .peek((k,v) -> logger.info("Send: {key:[{}], value:[{}]}", k, v))
                .to(outputTopic, Produced.with(stringSerde, fraudIpIUCSerde ));

        return builder.build();
    }

    static Topology getNewLocationTopology(String inputTopic1, String inputTopic2, String outputTopic1, String outputTopic2) {
        StreamsBuilder builder = new StreamsBuilder();
        final KTable<String, LocationDto> knownUserLocation = builder.table(inputTopic1, Consumed.with(stringSerde, fraudLocationSerde));

        KStream<String, KeycloakDto> deviceStream = builder
                .stream(inputTopic2, Consumed.with(stringSerde, keycloakJsonSerde))
                .filter((k, v) -> v.getError() == null)
                .map((key, v) -> new KeyValue<>(String.format("%s:%s:%s",v.getUserId(),v.getRealmId(),v.getIpAddress()), v))
                .leftJoin(knownUserLocation,
                        (left, right) -> {
                            if (right == null) return left;
                            return null;
                        })
                .filter((key, value) -> value != null)
                .peek((k,v) -> logger.info("Send: {key:[{}], value:[{}]}", k, v));

        deviceStream.to(outputTopic1, Produced.with(stringSerde, keycloakJsonSerde));

        deviceStream
                .map((k,v) -> new KeyValue<>(String.format("%s:%s:%s",v.getUserId(),v.getRealmId(),v.getIpAddress()),
                        new LocationDto(v.getUserId().toString(),v.getRealmId(),v.getIpAddress(),v.getTime()) ))
                .to(outputTopic2, Produced.with(stringSerde, fraudLocationSerde));

        return builder.build();
    }
}
