package io.fintech.loan.application.service.utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies messages published by producer routes to messaging backends.
 * Uses direct client connections to consume/check messages from queues, topics, buckets, and files.
 * Mirrors the verification logic from integration tests (captureXxx/verifyXxx mock endpoints).
 */
public class MessageVerifier implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(MessageVerifier.class);

  // Unique per JVM run (fresh offsets each run), stable within run (iterations don't re-read)
  private static final String KAFKA_GROUP_ID = "bbtest-verifier-" + ProcessHandle.current().pid();

  // --- Kafka Verification ---

  private KafkaConsumer<String, String> kafkaTextConsumer;
  private String kafkaTextConsumerTopic;
  private KafkaConsumer<String, byte[]> kafkaBinaryConsumer;
  private String kafkaBinaryConsumerTopic;

  private KafkaConsumer<String, String> getKafkaTextConsumer(String topic) {
    if (kafkaTextConsumer == null) {
      Properties props = new Properties();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
      props.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_GROUP_ID);
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      kafkaTextConsumer = new KafkaConsumer<>(props);
    }
    if (!topic.equals(kafkaTextConsumerTopic)) {
      kafkaTextConsumer.subscribe(Collections.singletonList(topic));
      kafkaTextConsumerTopic = topic;
    }
    return kafkaTextConsumer;
  }

  private KafkaConsumer<String, byte[]> getKafkaBinaryConsumer(String topic) {
    if (kafkaBinaryConsumer == null) {
      Properties props = new Properties();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
      props.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_GROUP_ID);
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
      kafkaBinaryConsumer = new KafkaConsumer<>(props);
    }
    if (!topic.equals(kafkaBinaryConsumerTopic)) {
      kafkaBinaryConsumer.subscribe(Collections.singletonList(topic));
      kafkaBinaryConsumerTopic = topic;
    }
    return kafkaBinaryConsumer;
  }

  /**
   * Consumes messages from a Kafka topic and returns their string values.
   */
  public List<String> consumeKafkaMessages(String topic, int expectedCount, Duration timeout) {
    List<String> messages = new ArrayList<>();
    try {
      KafkaConsumer<String, String> consumer = getKafkaTextConsumer(topic);
      long deadline = System.currentTimeMillis() + timeout.toMillis();
      while (messages.size() < expectedCount && System.currentTimeMillis() < deadline) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
        for (ConsumerRecord<String, String> record : records) {
          messages.add(record.value());
        }
      }
      consumer.commitSync();
    } catch (Exception e) {
      log.error("Failed to consume Kafka messages from {}: {}", topic, e.getMessage());
      throw new RuntimeException(e);
    }
    return messages;
  }

  /**
   * Consumes binary messages from a Kafka topic and returns their byte values.
   */
  public List<byte[]> consumeKafkaBinaryMessages(String topic, int expectedCount, Duration timeout) {
    List<byte[]> messages = new ArrayList<>();
    try {
      KafkaConsumer<String, byte[]> consumer = getKafkaBinaryConsumer(topic);
      long deadline = System.currentTimeMillis() + timeout.toMillis();
      while (messages.size() < expectedCount && System.currentTimeMillis() < deadline) {
        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
        for (ConsumerRecord<String, byte[]> record : records) {
          messages.add(record.value());
        }
      }
      consumer.commitSync();
    } catch (Exception e) {
      log.error("Failed to consume Kafka binary messages from {}: {}", topic, e.getMessage());
      throw new RuntimeException(e);
    }
    return messages;
  }

  /**
   * Consumes Schema Registry-encoded Avro messages from a Kafka topic via
   * Apicurio's AvroKafkaDeserializer. SR support in this archetype is Avro-only:
   * Apicurio's JSON Schema parser does not auto-derive schemas from POJOs, and
   * Apicurio's Protobuf serdes are incompatible with protobuf-java 4.x.
   * The targetType is embedded in the writer schema when specific-avro-reader=true.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<Object> consumeKafkaSchemaRegistryMessages(String topic, Class<?> targetType,
      int expectedCount, Duration timeout) {
    if (!topic.endsWith("-avro")) {
      throw new IllegalArgumentException(
          "Topic " + topic + " has no -avro suffix; SR consume in this archetype is Avro-only");
    }
    var props = new java.util.Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_GROUP_ID + "-sr-" + System.nanoTime());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.apicurio.registry.serde.avro.AvroKafkaDeserializer");
    props.put("apicurio.registry.url",
        System.getenv().getOrDefault("KAFKA_SCHEMA_REGISTRY_URL", "http://localhost:8081/apis/registry/v2"));
    props.put("apicurio.registry.use-specific-avro-reader", "true");

    List<Object> messages = new ArrayList<>();
    try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(Collections.singletonList(topic));
      long deadline = System.currentTimeMillis() + timeout.toMillis();
      while (messages.size() < expectedCount && System.currentTimeMillis() < deadline) {
        ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));
        for (ConsumerRecord<String, Object> record : records) {
          messages.add(record.value());
        }
      }
      consumer.commitSync();
    } catch (Exception e) {
      log.error("Failed to consume Kafka SR messages from {}: {}", topic, e.getMessage());
      throw new RuntimeException(e);
    }
    return messages;
  }

  @Override
  public void close() {
    try {
      if (kafkaTextConsumer != null)
        kafkaTextConsumer.close();
    } catch (Exception e) {
      log.error("Failed to close Kafka text consumer: {}", e.getMessage());
    }
    try {
      if (kafkaBinaryConsumer != null)
        kafkaBinaryConsumer.close();
    } catch (Exception e) {
      log.error("Failed to close Kafka binary consumer: {}", e.getMessage());
    }
  }

}
