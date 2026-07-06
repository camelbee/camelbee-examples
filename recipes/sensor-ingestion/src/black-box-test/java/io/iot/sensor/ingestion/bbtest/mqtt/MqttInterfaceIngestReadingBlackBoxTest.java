package io.iot.sensor.ingestion.bbtest.mqtt;

import static org.awaitility.Awaitility.await;

import io.iot.sensor.ingestion.bbtest.IngestReadingBlackBoxTest;
import io.iot.sensor.ingestion.utils.clients.MessageClient;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MqttInterfaceIngestReadingBlackBoxTest extends IngestReadingBlackBoxTest {

  static final String MQTT_TOPIC = "sensors/device-001/readings";
  private final MessageClient messageClient = new MessageClient();

  private static Stream<Arguments> ingestSuccessParameters() {
    return Stream.of(Arguments.of("ingest-reading-success"));
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("ingestSuccessParameters")
  void given_ValidReading_When_IngestedViaMqtt_Then_ResultIsSuccess(String fileName) throws Exception {
    setupIngestSuccessScenario();
    String json = "{\"deviceId\":\"device-001\",\"sensorType\":\"TEMPERATURE\",\"value\":23.5,\"unit\":\"C\",\"quality\":\"GOOD\",\"recordedAt\":\"2025-01-01T00:00:00Z\"}";
    messageClient.sendMqttBinaryMessage(MQTT_TOPIC, json.getBytes(), "test-tx-id");
    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
      validateIngestSuccessScenario();
    });
  }
}
