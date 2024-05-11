package ru.otus.kafkastream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.kafka.streams.StreamsConfig;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class EventsCounter {
    static final String srcTopic = "events";
    static final String outTopic = "out";
    static final String bootstrapServers = "localhost:39091,localhost:39092,localhost:39093";
    static final Duration fiveMinutes = Duration.ofMinutes(5);

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(EventsCounter.class.getSimpleName());

        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "EventsConsumer");

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String,String> eventsStream = builder.stream(srcTopic, Consumed.with(Serdes.String(),Serdes.String()));

        eventsStream
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(fiveMinutes))
                .count()
                .toStream()
                .map((k,v) -> new KeyValue<>("%s@<%s,%s>".formatted(k.key(), k.window().startTime(), k.window().endTime()),v.toString()))
                .peek((k,v) -> logger.info("Send to: {topic:[{}], key:[{}], value:[{}]}",outTopic,k,v))
                .to(outTopic,Produced.with(Serdes.String(),Serdes.String()));

        try (KafkaStreams streams = new KafkaStreams(builder.build(), props)) {
            streams.start();
            new CountDownLatch(1).await();
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage());
        }
    }
}
