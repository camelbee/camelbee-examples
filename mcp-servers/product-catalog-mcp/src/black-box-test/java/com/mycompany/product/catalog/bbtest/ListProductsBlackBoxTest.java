package com.mycompany.product.catalog.bbtest;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.mycompany.product.catalog.utils.DataSeeder;
import com.mycompany.product.catalog.utils.DataVerifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ListProductsBlackBoxTest extends BlackBoxTest {

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

  protected String productRestApiUri = "/products";

  protected void setupListProductsSuccessScenario() throws Exception {
    wireMock.resetRequests();
    dataSeeder.resetData();
  }

  protected void validateListProductsSuccessScenario() throws Exception {
    wireMock.verify(getRequestedFor(urlPathEqualTo(productRestApiUri)));
    assertThat(dataVerifier.countAuditLogs()).isGreaterThanOrEqualTo(1);
  }

  protected void setupListProductsErrorScenario() throws Exception {
    wireMock.resetRequests();
  }

  protected void validateListProductsBadRequestScenario() throws Exception {
    wireMock.verify(0, getRequestedFor(urlPathEqualTo(productRestApiUri)));
  }

  protected void validateListProductsRestBackendErrorScenario() throws Exception {
    wireMock.verify(1, getRequestedFor(urlPathEqualTo(productRestApiUri)));
  }
}
