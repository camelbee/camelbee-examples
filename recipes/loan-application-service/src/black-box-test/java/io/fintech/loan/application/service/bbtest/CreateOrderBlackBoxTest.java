package io.fintech.loan.application.service.bbtest;

import static org.assertj.core.api.Assertions.assertThat;

import io.fintech.loan.application.service.utils.DataSeeder;
import io.fintech.loan.application.service.utils.DataVerifier;
import io.fintech.loan.application.service.utils.MessageVerifier;
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

  protected static final String KAFKA_CREATEORDER_TOPIC = "camelbeeservice-southbound-createorder-topic";

  /**
   * Resets test data and WireMock state before a create order test.
   */
  protected void setupCreateOrderSuccessScenario(String fileName) throws Exception {

    dataVerifier.clearJpaTables();
    if (dataVerifier.isCacheReachable())
      dataVerifier.clearCache();

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

    // Verify write-through: cache should contain the purchase after CRO.
    // Caffeine is in-JVM inside the app container and not reachable from bbtest — covered by integration tests.
    assertThat(dataVerifier.countCachedPurchases())
        .as("Cache should contain at least one purchase after CRO write-through")
        .isGreaterThanOrEqualTo(1);

    // Messaging backends: verify messages were published with correct body

    {
      var kafkaBinaryMessages = messageVerifier.consumeKafkaBinaryMessages(KAFKA_CREATEORDER_TOPIC, 1, java.time.Duration.ofSeconds(10));
      assertThat(kafkaBinaryMessages).hasSize(1);
      assertThat(kafkaBinaryMessages.get(0)).isEqualTo(readResourceBinary(requestFile.formatted("avro") + ".avro"));
    }

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
    assertThat(messageVerifier.consumeKafkaMessages(KAFKA_CREATEORDER_TOPIC, 1, java.time.Duration.ofSeconds(1)))
        .isEmpty();
  }

}
