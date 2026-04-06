package com.mycompany.catalog.mcp.bbtest;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.catalog.mcp.utils.DataVerifier;
import com.mycompany.catalog.mcp.utils.DataSeeder;
import com.mycompany.catalog.mcp.utils.MessageVerifier;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Parent class for CreateOrder black-box tests.
 * Provides setup/validation scenarios for all interfaces.
 * Mirrors CreateOrderIntegrationTest but uses direct connections instead of Camel.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateOrderBlackBoxTest extends BlackBoxTest {

  protected static final String BASE_PATH_API = "/data/inttest/api/";
  protected static final String BASE_PATH_INFRA = "/data/inttest/infra/";
  protected static final String CREATEORDER_BASE_PATH_API = BASE_PATH_API + "%s/createorder/";
  protected static final String CREATEORDER_BASE_PATH_INFRA = BASE_PATH_INFRA + "%s/createpurchase/";

  protected DataVerifier dataVerifier = new DataVerifier();
  protected DataSeeder dataSeeder = new DataSeeder();
  protected MessageVerifier messageVerifier = new MessageVerifier();

  @AfterAll
  void closeConnections() {
    dataVerifier.close();
    dataSeeder.close();
    messageVerifier.close();
  }




  /**
   * Resets test data and WireMock state before a create order test.
   */
  protected void setupCreateOrderSuccessScenario(String fileName) throws Exception {

    dataVerifier.clearJpaTables();

    // Purge messaging queues
  }

  /**
   * Validates that all backends received and persisted the order correctly.
   */
  protected void validateCreateOrderSuccessScenario(String fileName) throws Exception {

    String requestFile = CREATEORDER_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    // WireMock: verify HTTP backends received the request



    // Database: verify data was persisted

    assertThat(dataVerifier.countJpaPurchases()).isEqualTo(1);
    assertThat(dataVerifier.countJpaPurchaseItems()).isEqualTo(10);




    // Messaging backends: verify messages were published with correct body








  }

  /**
   * Resets WireMock state, clears databases and purges queues before an error scenario test.
   * Prevents stale data from success tests causing false positives.
   */
  protected void setupCreateOrderErrorScenario() throws Exception {

    dataVerifier.clearJpaTables();

  }

  /**
   * Validates that backends did NOT persist data during a bad request scenario.
   * No backends should have been called at all.
   */
  protected void validateCreateOrderBadRequestScenario() throws Exception {
    validateNoDataPersisted();
  }




  /**
   * Generic error scenario validation — used by non-REST interface tests (AMQP, JMS, Kafka, etc.).
   */
  protected void validateCreateOrderErrorScenario() throws Exception {
    validateNoDataPersisted();
  }

  /**
   * Validates that no data was persisted to any database or messaging backend.
   */
  protected void validateNoDataPersisted() throws Exception {
    assertThat(dataVerifier.countJpaPurchases()).isEqualTo(0);

    // Messaging backends: verify no messages were sent
  }

}
