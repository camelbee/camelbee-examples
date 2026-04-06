package com.mycompany.catalog.mcp.bbtest;

import com.mycompany.catalog.mcp.utils.DataVerifier;
import com.mycompany.catalog.mcp.utils.DataSeeder;

import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.client.WireMock;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ListOrdersBlackBoxTest extends BlackBoxTest {

  protected static final String BASE_PATH_API = "/data/inttest/api/";
  protected DataVerifier dataVerifier = new DataVerifier();
  protected DataSeeder dataSeeder = new DataSeeder();

  @AfterAll
  void closeConnections() {
    dataVerifier.close();
    dataSeeder.close();
  }

  protected static WireMock wireMock;
  static {
    WireMock.configureFor("localhost", 8091);
    wireMock = new WireMock("localhost", 8091);
  }

  protected String purchaseRestApiUri = "/purchase-service-api/purchases";

  protected void setupListOrdersSuccessScenario() throws Exception {
    wireMock.resetRequests();
    dataSeeder.resetData();
  }

  protected void validateListOrdersSuccessScenario(int expectedOrders) throws Exception {
    wireMock.verify(getRequestedFor(urlPathEqualTo(purchaseRestApiUri)));


  }

  protected void setupListOrdersErrorScenario() throws Exception {
    wireMock.resetRequests();
  }

  /**
   * Generic error scenario validation — used by non-REST interface tests.
   */
  protected void validateListOrdersErrorScenario() throws Exception {
    // List is read-only; no persistence to verify.
  }

  /**
   * Validates that backends did NOT receive any request during a bad request scenario.
   * No backends should have been called at all.
   */
  protected void validateListOrdersBadRequestScenario() throws Exception {
    wireMock.verify(0, getRequestedFor(urlPathEqualTo(purchaseRestApiUri)));
  }

  /**
   * Validates the error scenario when the REST backend returned an error.
   * REST backend was called but returned error; SOAP and GRPC should NOT have been called.
   */
  protected void validateErrorFromTheRestBackendScenario(String fileName) throws Exception {

    wireMock.verify(1, getRequestedFor(urlPathEqualTo(purchaseRestApiUri)));

  }



  /**
   * Validates that no data was persisted — List is read-only so this is a no-op.
   */
  protected void validateNoDataPersisted() throws Exception {
    // List is read-only; no persistence to verify.
  }

}
