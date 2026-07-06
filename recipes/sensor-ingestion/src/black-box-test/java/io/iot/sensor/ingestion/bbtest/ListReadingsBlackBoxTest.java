package io.iot.sensor.ingestion.bbtest;

import io.iot.sensor.ingestion.utils.DataSeeder;
import io.iot.sensor.ingestion.utils.DataVerifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ListReadingsBlackBoxTest extends BlackBoxTest {

  protected DataVerifier dataVerifier = new DataVerifier();
  protected DataSeeder dataSeeder = new DataSeeder();

  @AfterAll
  void closeConnections() {
    dataVerifier.close();
    dataSeeder.close();
  }

  protected void setupListSuccessScenario() throws Exception {
    dataSeeder.resetData();
  }

  protected void validateListSuccessScenario() throws Exception {
  }

  protected void setupListErrorScenario() throws Exception {
  }

  protected void validateListBadRequestScenario() throws Exception {
  }
}
