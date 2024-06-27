package otus;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import otus.Topologies;

import java.util.Properties;

import static utils.Utils.getProperties;

public class KafkaStream {
    String KEYCLOAK_EVENT_TOPIC;
    String MALICIOUS_USER_NOT_FOUND_TOPIC;
    String MALICIOUS_INVALID_USER_CREDENTIALS_TOPIC;
    String KNOWN_USER_LOCATION_TOPIC;
    String NEW_USER_LOCATION_TOPIC;
    String BOOTSTRAP_SERVERS;
    long MALICIOUS_INVALID_USER_CREDENTIALS_COUNT;
    long MALICIOUS_INVALID_USER_CREDENTIALS_MINUTES;
    long MALICIOUS_USER_NOT_FOUND_COUNT;
    long MALICIOUS_USER_NOT_FOUND_MINUTES;

    Properties props;

    public KafkaStream(Properties propertiesSystem) {
        KEYCLOAK_EVENT_TOPIC = propertiesSystem.getProperty("KEYCLOAK_EVENT_TOPIC");
        MALICIOUS_USER_NOT_FOUND_TOPIC = propertiesSystem.getProperty("MALICIOUS_USER_NOT_FOUND_TOPIC");
        MALICIOUS_INVALID_USER_CREDENTIALS_TOPIC = propertiesSystem.getProperty("MALICIOUS_INVALID_USER_CREDENTIALS_TOPIC");
        KNOWN_USER_LOCATION_TOPIC = propertiesSystem.getProperty("KNOWN_USER_LOCATION_TOPIC");
        NEW_USER_LOCATION_TOPIC = propertiesSystem.getProperty("NEW_USER_LOCATION_TOPIC");
        BOOTSTRAP_SERVERS = propertiesSystem.getProperty("BOOTSTRAP_SERVERS");

        MALICIOUS_INVALID_USER_CREDENTIALS_COUNT = Long.parseLong(propertiesSystem.getProperty("MALICIOUS_INVALID_USER_CREDENTIALS_COUNT"));
        MALICIOUS_INVALID_USER_CREDENTIALS_MINUTES = Long.parseLong(propertiesSystem.getProperty("MALICIOUS_INVALID_USER_CREDENTIALS_MINUTES"));
        MALICIOUS_USER_NOT_FOUND_COUNT = Long.parseLong(propertiesSystem.getProperty("MALICIOUS_USER_NOT_FOUND_COUNT"));
        MALICIOUS_USER_NOT_FOUND_MINUTES = Long.parseLong(propertiesSystem.getProperty("MALICIOUS_USER_NOT_FOUND_MINUTES"));

        props = getProperties(BOOTSTRAP_SERVERS);
    }

    KafkaStreams maliciousUserNotFound() {
        Properties properties = props;
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "malicious-user-not-found");

        Topology topology = Topologies.getTopologyMaliciousUNF(
                KEYCLOAK_EVENT_TOPIC,
                MALICIOUS_USER_NOT_FOUND_TOPIC,
                MALICIOUS_USER_NOT_FOUND_COUNT,
                MALICIOUS_USER_NOT_FOUND_MINUTES
        );
        KafkaStreams kafkaStreams = new KafkaStreams(
                topology,
                properties);
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        return kafkaStreams;
    }

    KafkaStreams maliciousInvalidUserCredentials() {
        Properties properties = props;
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "malicious-invalid-user-credentials");

        Topology topology = Topologies.getTopologyMaliciousIUC(
                KEYCLOAK_EVENT_TOPIC,
                MALICIOUS_INVALID_USER_CREDENTIALS_TOPIC,
                MALICIOUS_INVALID_USER_CREDENTIALS_COUNT,
                MALICIOUS_INVALID_USER_CREDENTIALS_MINUTES
        );
        KafkaStreams kafkaStreams = new KafkaStreams(
                topology,
                properties);
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        return kafkaStreams;
    }

    KafkaStreams newUserLocationTopology() {
        Properties properties = props;
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "new-user-location");

        Topology topology = Topologies.getNewLocationTopology(
                KNOWN_USER_LOCATION_TOPIC,
                KEYCLOAK_EVENT_TOPIC,
                NEW_USER_LOCATION_TOPIC,
                KNOWN_USER_LOCATION_TOPIC);

        KafkaStreams kafkaStreams = new KafkaStreams(
                topology,
                properties);
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        return kafkaStreams;
    }

}
