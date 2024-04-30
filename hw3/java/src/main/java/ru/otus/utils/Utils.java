package ru.otus.utils;

import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.UUID;

public class Utils {
    public static ProducerRecord<String, String> generateMessage(String topicName, int key, String iter) {
        return new ProducerRecord<>(topicName, String.valueOf(key), iter + "-" +UUID.randomUUID().toString() );
    }
}
