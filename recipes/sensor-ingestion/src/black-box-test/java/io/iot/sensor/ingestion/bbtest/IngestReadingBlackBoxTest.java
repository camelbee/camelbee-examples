package io.iot.sensor.ingestion.bbtest;

import static org.assertj.core.api.Assertions.assertThat;

import io.iot.sensor.ingestion.utils.DataSeeder;
import io.iot.sensor.ingestion.utils.DataVerifier;
import io.iot.sensor.ingestion.utils.MessageVerifier;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IngestReadingBlackBoxTest extends BlackBoxTest {

  protected DataVerifier dataVerifier = new DataVerifier();
  protected DataSeeder dataSeeder = new DataSeeder();
  protected MessageVerifier messageVerifier = new MessageVerifier();
  protected static final String KAFKA_INGEST_TOPIC = "sensor-readings";

  @AfterAll
  void closeConnections() {
    dataVerifier.close();
    dataSeeder.close();
    messageVerifier.close();
  }

  protected void setupIngestSuccessScenario() throws Exception {
    dataVerifier.clearMongodbCollection("sensorReadings");
  }

  protected void validateIngestSuccessScenario() throws Exception {
    var kafkaMessages = messageVerifier.consumeKafkaMessages(KAFKA_INGEST_TOPIC, 1, Duration.ofSeconds(10));
  }

  protected void setupIngestErrorScenario() throws Exception {
    dataVerifier.clearMongodbCollection("sensorReadings");
  }

  protected void validateIngestBadRequestScenario() throws Exception {
    validateNoDataPersisted();
  }

  protected void validateNoDataPersisted() throws Exception {
    assertThat(dataVerifier.countMongodbCollection("sensorReadings")).isEqualTo(0);
    assertThat(messageVerifier.consumeKafkaMessages(KAFKA_INGEST_TOPIC, 1, Duration.ofSeconds(1)))
        .isEmpty();
  }
}
