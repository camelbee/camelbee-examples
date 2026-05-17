package io.fintech.loan.application.service.utils.clients;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client helper for sending messages to queues/topics in black-box tests.
 * Provides methods for each messaging technology using native client libraries.
 */
public class MessageClient {

  private static final Logger log = LoggerFactory.getLogger(MessageClient.class);

  /**
   * Sends a message to a Kafka topic.
   */
  public void sendKafkaMessage(String topic, String key, String body, String transactionId) {
    var props = new java.util.Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    try (var producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(props)) {
      var record = new org.apache.kafka.clients.producer.ProducerRecord<>(topic, key, body);
      if (transactionId != null) {
        record.headers().add("transactionId", transactionId.getBytes(StandardCharsets.UTF_8));
      }
      producer.send(record).get();
      log.info("Sent Kafka message to topic: {} with transactionId: {}", topic, transactionId);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send Kafka message to " + topic, e);
    }
  }

  /**
   * Sends a binary message to a Kafka topic (for PROTO/AVRO formats).
   */
  public void sendKafkaBinaryMessage(String topic, String key, byte[] body, String transactionId) {
    var props = new java.util.Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
    try (var producer = new org.apache.kafka.clients.producer.KafkaProducer<String, byte[]>(props)) {
      var record = new org.apache.kafka.clients.producer.ProducerRecord<>(topic, key, body);
      if (transactionId != null) {
        record.headers().add("transactionId", transactionId.getBytes(StandardCharsets.UTF_8));
      }
      producer.send(record).get();
      log.info("Sent Kafka binary message to topic: {} with transactionId: {}", topic, transactionId);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send Kafka binary message to " + topic, e);
    }
  }

  /**
   * Sends a typed Avro payload to a Kafka topic via Apicurio Schema Registry.
   * SR support in this archetype is Avro-only — Apicurio's JSON Schema parser
   * does not auto-derive schemas from POJOs, and Apicurio's Protobuf serdes are
   * incompatible with protobuf-java 4.x. Body must be an Avro SpecificRecord;
   * the serializer auto-registers the schema with the registry on first publish.
   */
  public void sendKafkaSchemaRegistryMessage(String topic, String key, Object payload, String transactionId) {
    if (!topic.endsWith("-avro")) {
      throw new IllegalArgumentException(
          "Topic " + topic + " has no -avro suffix; SR send in this archetype is Avro-only");
    }
    var props = new java.util.Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("apicurio.registry.url",
        System.getenv().getOrDefault("KAFKA_SCHEMA_REGISTRY_URL", "http://localhost:8081/apis/registry/v2"));
    props.put("apicurio.registry.auto-register", "true");
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "io.apicurio.registry.serde.avro.AvroKafkaSerializer");

    try (var producer = new org.apache.kafka.clients.producer.KafkaProducer<String, Object>(props)) {
      var record = new org.apache.kafka.clients.producer.ProducerRecord<>(topic, key, payload);
      if (transactionId != null) {
        record.headers().add("transactionId", transactionId.getBytes(StandardCharsets.UTF_8));
      }
      producer.send(record).get();
      log.info("Sent Kafka SR message to topic: {} with transactionId: {}", topic, transactionId);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send Kafka SR message to " + topic, e);
    }
  }

  public String readResource(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null)
        throw new RuntimeException("Resource not found: " + path);
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public byte[] readResourceBinary(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null)
        throw new RuntimeException("Resource not found: " + path);
      return is.readAllBytes();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
