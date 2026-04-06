package com.mycompany.catalog.mcp.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.catalog.mcp.model.domain.Product;
import com.mycompany.catalog.mcp.model.infra.jpa.postgresql.AuditLogEntity;
import java.util.Arrays;
import java.util.List;
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

  @EndpointInject("mock:captureGetProductAuditLog")
  protected MockEndpoint captureGetProductAuditLog;

  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
        captureGetProductRest,
        captureGetProductAuditLog
    );

    verifyMockEndpoints = Arrays.asList();

    super.setup();

    if (!initialized) {
      AdviceWith.adviceWith(camelContext, "getProductRestRoute",
          a -> a.weaveAddLast().to("mock:captureGetProductRest"));
      // Note: audit log route already advised in ListProducts test, so we skip re-advising
    }

    initialized = true;

    super.resetBeforeAll();
  }

  protected void setupGetProductSuccessScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(0);
    captureGetProductRest.expectedMessageCount(1);
    clearAuditLogTable();
  }

  protected void validateGetProductSuccessScenario(String productId) throws Exception {
    captureError.assertIsSatisfied();
    captureGetProductRest.assertIsSatisfied();

    Product product = captureGetProductRest.getReceivedExchanges().get(0).getIn().getBody(Product.class);
    assertThat(product).isNotNull();
    assertThat(product.getId()).isEqualTo(productId);

    wireMock.verify(getRequestedFor(urlPathEqualTo(productRestApiUri + "/" + productId)));

    // Verify audit log
    List auditLogs = queryAuditLogs();
    assertThat(auditLogs).hasSize(1);
    assertThat(((AuditLogEntity) auditLogs.get(0)).getToolName()).isEqualTo("getProduct");
  }

  protected void setupGetProductBadRequestScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(1);
    captureGetProductRest.expectedMessageCount(0);
  }

  protected void validateGetProductBadRequestScenario() throws Exception {
    captureError.assertIsSatisfied();
    captureGetProductRest.assertIsSatisfied();
  }

}
