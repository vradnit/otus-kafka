package ru.otus.kafkastream;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.utils.Utils;


public class Producer {
  static final String topic = "events";
  static final String bootstrapServers = "localhost:39091,localhost:39092,localhost:39093";

  public static void main(String[] args) {
    Logger logger = LoggerFactory.getLogger(Producer.class.getSimpleName());

    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

    logger.info("Start:");
    while (!Thread.interrupted()) {
      try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
        ProducerRecord<String, String> record = Utils.generateMessage(topic);
        producer.send(record);
        logger.info("Send to: {topic:[{}], key:[{}], value:[{}]}", record.topic(), record.key(), record.value());
        Thread.sleep(100);
      }
      catch (Exception e) {
        System.out.println("EXCEPTION: " + e.getMessage());
      }
    }
  }
}
