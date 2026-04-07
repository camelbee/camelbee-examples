package com.mycompany.product.catalog.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.product.catalog.model.domain.Product;
import com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog;
import java.util.Arrays;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Integration test base for GetProduct operation.
 */
public class GetProductIntegrationTest extends IntegrationTest {

  @ConfigProperty(name = "backend-product-rest-api.uri")
  protected String productRestApiUri;

  @EndpointInject("mock:captureGetProductRest")
  protected MockEndpoint captureGetProductRest;

  @EndpointInject("mock:captureGetProductWriteAuditLogJpa")
  protected MockEndpoint captureGetProductWriteAuditLogJpa;

  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
        captureGetProductRest,
        captureGetProductWriteAuditLogJpa
    );

    verifyMockEndpoints = Arrays.asList();

    super.setup();

    if (!initialized) {
      AdviceWith.adviceWith(camelContext, "getProductRestRoute",
          a -> a.weaveAddLast().to("mock:captureGetProductRest"));
      AdviceWith.adviceWith(camelContext, "writeAuditLogJpaRoute",
          a -> a.weaveAddLast().to("mock:captureGetProductWriteAuditLogJpa"));
    }

    initialized = true;

    super.resetBeforeAll();
  }

  protected void setupGetProductSuccessScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(0);
    captureGetProductRest.expectedMessageCount(1);
    captureGetProductWriteAuditLogJpa.expectedMessageCount(1);
    clearAuditLogTable();
  }

  protected void validateGetProductSuccessScenario(String productId) throws Exception {
    captureError.assertIsSatisfied();
    captureGetProductRest.assertIsSatisfied();
    captureGetProductWriteAuditLogJpa.assertIsSatisfied();

    Product product = (Product) captureGetProductRest.getReceivedExchanges().get(0).getIn().getBody();
    assertThat(product.getId()).isEqualTo(productId);

    wireMock.verify(getRequestedFor(urlPathEqualTo(productRestApiUri + "/" + productId)));

    var auditLogs = queryAuditLogs();
    assertThat(auditLogs).hasSize(1);
    assertThat(((AuditLog) auditLogs.get(0)).getToolName()).isEqualTo("getProduct");
    assertThat(((AuditLog) auditLogs.get(0)).getResponseStatus()).isEqualTo(AuditLog.ResponseStatusEnum.SUCCESS);
  }

  protected void setupGetProductErrorScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(1);
    captureGetProductRest.expectedMessageCount(0);
    captureGetProductWriteAuditLogJpa.expectedMessageCount(0);
    clearAuditLogTable();
  }

  protected void validateGetProductErrorScenario() throws Exception {
    captureError.assertIsSatisfied();
    captureGetProductRest.assertIsSatisfied();
    captureGetProductWriteAuditLogJpa.assertIsSatisfied();
  }
}
