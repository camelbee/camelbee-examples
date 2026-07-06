package io.iot.sensor.ingestion.bbtest;

import io.iot.sensor.ingestion.utils.DataSeeder;
import io.iot.sensor.ingestion.utils.DataVerifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetReadingBlackBoxTest extends BlackBoxTest {

  protected DataVerifier dataVerifier = new DataVerifier();
  protected DataSeeder dataSeeder = new DataSeeder();

  @AfterAll
  void closeConnections() {
    dataVerifier.close();
    dataSeeder.close();
  }

  protected void setupGetSuccessScenario() throws Exception {
    dataSeeder.resetData();
  }

  protected void validateGetSuccessScenario() throws Exception {
  }

  protected void setupGetErrorScenario() throws Exception {
  }

  protected void validateGetBadRequestScenario() throws Exception {
  }
}
