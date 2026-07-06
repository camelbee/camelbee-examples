package io.iot.sensor.ingestion.utils.clients;

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
   * Sends a message to an MQTT topic.
   */
  public void sendMqttMessage(String topic, String body, String transactionId) {
    try {
      var client = new org.eclipse.paho.mqttv5.client.MqttClient("tcp://localhost:1883", "bbtest-" + System.currentTimeMillis());
      client.connect();
      var message = new org.eclipse.paho.mqttv5.common.MqttMessage(body.getBytes(StandardCharsets.UTF_8));
      message.setQos(1);
      if (transactionId != null) {
        var mqttProps = new org.eclipse.paho.mqttv5.common.packet.MqttProperties();
        mqttProps.setUserProperties(java.util.List.of(
            new org.eclipse.paho.mqttv5.common.packet.UserProperty("transactionId", transactionId)));
        message.setProperties(mqttProps);
      }
      client.publish(topic, message);
      client.disconnect();
      client.close();
      log.info("Sent MQTT message to topic: {} with transactionId: {}", topic, transactionId);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send MQTT message to " + topic, e);
    }
  }

  /**
   * Sends a binary message to an MQTT topic (for PROTO/AVRO formats).
   */
  public void sendMqttBinaryMessage(String topic, byte[] body, String transactionId) {
    try {
      var client = new org.eclipse.paho.mqttv5.client.MqttClient("tcp://localhost:1883", "bbtest-" + System.currentTimeMillis());
      client.connect();
      var message = new org.eclipse.paho.mqttv5.common.MqttMessage(body);
      message.setQos(1);
      if (transactionId != null) {
        var mqttProps = new org.eclipse.paho.mqttv5.common.packet.MqttProperties();
        mqttProps.setUserProperties(java.util.List.of(
            new org.eclipse.paho.mqttv5.common.packet.UserProperty("transactionId", transactionId)));
        message.setProperties(mqttProps);
      }
      client.publish(topic, message);
      client.disconnect();
      client.close();
      log.info("Sent MQTT binary message to topic: {} with transactionId: {}", topic, transactionId);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send MQTT binary message to " + topic, e);
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
