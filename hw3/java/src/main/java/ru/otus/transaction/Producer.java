package ru.otus.transaction;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.utils.Utils;

public class Producer {
  static final String[] topics = {"topic1", "topic2"};
  static final String transactionalId = "TransactionProducer";
  static final String bootstrapServers = "localhost:39091,localhost:39092,localhost:39093";

  public static void main(String[] args) {
    Logger logger = LoggerFactory.getLogger(Producer.class.getSimpleName());

    Properties props = new Properties();

    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

    try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
      producer.initTransactions();

      logger.info("Start: committed transaction");
      producer.beginTransaction();
      for (int i = 0; i < 5; ++i) {
        for (String topicName: topics) {
          ProducerRecord<String, String> record = Utils.generateMessage(topicName, i, "good");
          producer.send(record);
          logger.info("[topic: {}, key: {}] {}", record.topic(), record.key(), record.value());
        }
      }
      producer.commitTransaction();
      logger.info("End: committed transaction");

      logger.info("Start: aborted transaction");
      producer.beginTransaction();
      for (int i = 0; i < 2; ++i) {
        for (String topicName: topics) {
          ProducerRecord<String, String> record = Utils.generateMessage(topicName, i, "fail");
          producer.send(record);
          logger.info("[topic: {}, key: {}] {}", record.topic(), record.key(), record.value());
        }
      }
      producer.abortTransaction();
      logger.info("End: Aborted transaction");
    }
  }
}
