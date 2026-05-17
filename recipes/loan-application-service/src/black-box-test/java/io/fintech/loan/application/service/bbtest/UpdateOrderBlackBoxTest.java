package io.fintech.loan.application.service.bbtest;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.fintech.loan.application.service.utils.DataSeeder;
import io.fintech.loan.application.service.utils.DataVerifier;
import io.fintech.loan.application.service.utils.MessageVerifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UpdateOrderBlackBoxTest extends BlackBoxTest {

  protected static final String BASE_PATH_API = "/data/inttest/api/";
  protected static final String BASE_PATH_INFRA = "/data/inttest/infra/";
  protected static final String UPDATEORDER_BASE_PATH_INFRA = BASE_PATH_INFRA + "%s/updatepurchase/";
  protected DataVerifier dataVerifier = new DataVerifier();
  protected DataSeeder dataSeeder = new DataSeeder();
  protected MessageVerifier messageVerifier = new MessageVerifier();

  @AfterAll
  void closeConnections() {
    dataVerifier.close();
    dataSeeder.close();
    messageVerifier.close();
  }

  protected static final String KAFKA_UPDATEORDER_TOPIC = "camelbeeservice-southbound-updateorder-topic";

  protected static WireMock wireMock;
  static {
    WireMock.configureFor("localhost", 8091);
    wireMock = new WireMock("localhost", 8091);
  }

  protected String purchaseRestApiUri = "/purchase-service-api/purchases";

  protected void setupUpdateOrderSuccessScenario(String fileName) throws Exception {
    wireMock.resetRequests();
    if (dataVerifier.isCacheReachable())
      dataVerifier.clearCache();
    dataSeeder.resetData();
  }

  /**
   * No-arg overload for non-REST interface tests.
   */
  protected void validateUpdateOrderSuccessScenario(String fileName) throws Exception {
    validateUpdateOrderSuccessScenario(fileName, null);
  }

  protected void validateUpdateOrderSuccessScenario(String fileName, String orderId) throws Exception {
    String requestFile = UPDATEORDER_BASE_PATH_INFRA + fileName.replace("updateorder", "updatepurchase");

    wireMock.verify(patchRequestedFor(urlPathTemplate(purchaseRestApiUri + "/{id}"))
        .withRequestBody(equalToJson(readResource(requestFile.formatted("json") + ".json"))));

    // Update adds items to existing — query by specific orderId (resetData seeds many)
    if (orderId != null) {
      assertThat(dataVerifier.countJpaPurchases("ID=" + orderId)).isEqualTo(1);
      // Verify write-through on UPDATE: specific key should be cached.
      // Caffeine skipped (in-JVM, not reachable from bbtest).
      if (dataVerifier.isCacheReachable()) {
        assertThat(dataVerifier.getCachedPurchaseJson(orderId))
            .as("Cache should contain the updated purchase JSON after UPO")
            .isNotNull();
      }
    }

    assertThat(messageVerifier.consumeKafkaMessages(KAFKA_UPDATEORDER_TOPIC, 1, java.time.Duration.ofSeconds(10)))
        .hasSize(1);
  }

  protected void setupUpdateOrderErrorScenario() throws Exception {
    wireMock.resetRequests();
    dataVerifier.clearJpaTables();
    if (dataVerifier.isCacheReachable())
      dataVerifier.clearCache();
  }

  /**
   * Generic error scenario validation — used by non-REST interface tests (AMQP, JMS, Kafka, etc.).
   */
  protected void validateUpdateOrderErrorScenario() throws Exception {
    validateNoDataPersisted();
  }

  /**
   * Validates that backends did NOT receive any request during a bad request scenario.
   * No backends should have been called at all.
   */
  protected void validateUpdateOrderBadRequestScenario() throws Exception {
    wireMock.verify(0, patchRequestedFor(urlPathTemplate(purchaseRestApiUri + "/{id}")));
    validateNoDataPersisted();
  }

  /**
   * Validates the error scenario when the REST backend returned an error.
   * REST backend was called but returned error; SOAP and GRPC should NOT have been called.
   */
  protected void validateErrorFromTheRestBackendScenario(String fileName) throws Exception {

    String requestFile = UPDATEORDER_BASE_PATH_INFRA + fileName.replace("updateorder", "updatepurchase");

    wireMock.verify(1, patchRequestedFor(urlPathTemplate(purchaseRestApiUri + "/{id}"))
        .withRequestBody(equalToJson(readResource(requestFile.formatted("json") + ".json"))));

    validateNoDataPersisted();
  }

  /**
   * Validates that no data was persisted to any database or messaging backend.
   */
  protected void validateNoDataPersisted() throws Exception {
    assertThat(dataVerifier.countJpaPurchases()).isEqualTo(0);

    // Messaging backends: verify no messages were sent
    assertThat(messageVerifier.consumeKafkaMessages(KAFKA_UPDATEORDER_TOPIC, 1, java.time.Duration.ofSeconds(1)))
        .isEmpty();
  }

}
