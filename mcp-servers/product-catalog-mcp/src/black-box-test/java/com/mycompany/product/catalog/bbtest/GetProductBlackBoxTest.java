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
public class GetProductBlackBoxTest extends BlackBoxTest {

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

  protected void setupGetProductSuccessScenario() throws Exception {
    wireMock.resetRequests();
    dataSeeder.resetData();
  }

  protected void validateGetProductSuccessScenario(String productId) throws Exception {
    wireMock.verify(getRequestedFor(urlPathEqualTo(productRestApiUri + "/" + productId)));
    assertThat(dataVerifier.countAuditLogs()).isGreaterThanOrEqualTo(1);
  }

  protected void setupGetProductErrorScenario() throws Exception {
    wireMock.resetRequests();
  }

  protected void validateGetProductBadRequestScenario() throws Exception {
    wireMock.verify(0, getRequestedFor(urlPathEqualTo(productRestApiUri)));
  }
}
