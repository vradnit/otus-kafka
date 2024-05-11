package ru.otus.utils;

import com.github.javafaker.Faker;
import org.apache.kafka.clients.producer.ProducerRecord;


public class Utils {
    static final Faker faker = new Faker();
    public static ProducerRecord<String, String> generateMessage(String topicName) {
        return new ProducerRecord<>(topicName, faker.gameOfThrones().city(), faker.gameOfThrones().character() );
    }
}
