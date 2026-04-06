package com.mycompany.catalog.mcp.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.catalog.mcp.model.domain.ProductPage;
import com.mycompany.catalog.mcp.model.infra.jpa.postgresql.AuditLogEntity;
import java.util.Arrays;
import java.util.List;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Integration test base for ListProducts operation.
 */
public class ListProductsIntegrationTest extends IntegrationTest {

  @ConfigProperty(name = "backend-product-rest-api.uri")
  protected String productRestApiUri;

  @EndpointInject("mock:captureListProductsRest")
  protected MockEndpoint captureListProductsRest;

  @EndpointInject("mock:captureListProductsAuditLog")
  protected MockEndpoint captureListProductsAuditLog;

  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
        captureListProductsRest,
        captureListProductsAuditLog
    );

    verifyMockEndpoints = Arrays.asList();

    super.setup();

    if (!initialized) {
      AdviceWith.adviceWith(camelContext, "listProductsRestRoute",
          a -> a.weaveAddLast().to("mock:captureListProductsRest"));
      AdviceWith.adviceWith(camelContext, "writeAuditLogJpaRoute",
          a -> a.weaveAddLast().to("mock:captureListProductsAuditLog"));
    }

    initialized = true;

    super.resetBeforeAll();
  }

  protected void setupListProductsSuccessScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(0);
    captureListProductsRest.expectedMessageCount(1);
    captureListProductsAuditLog.expectedMessageCount(1);
    clearAuditLogTable();
  }

  protected void validateListProductsSuccessScenario(String page, String pageSize, int expectedProducts) throws Exception {
    captureError.assertIsSatisfied();
    captureListProductsRest.assertIsSatisfied();

    ProductPage productPage = captureListProductsRest.getReceivedExchanges().get(0).getIn().getBody(ProductPage.class);
    assertThat(productPage.getProducts()).hasSize(expectedProducts);

    wireMock.verify(getRequestedFor(urlPathEqualTo(productRestApiUri))
        .withQueryParam("page", equalTo(page))
        .withQueryParam("pageSize", equalTo(pageSize)));

    // Verify audit log was written
    List auditLogs = queryAuditLogs();
    assertThat(auditLogs).hasSize(1);
    assertThat(((AuditLogEntity) auditLogs.get(0)).getToolName()).isEqualTo("listProducts");
    assertThat(((AuditLogEntity) auditLogs.get(0)).getResponseStatus()).isEqualTo(AuditLogEntity.ResponseStatus.SUCCESS);
  }

  protected void setupListProductsBadRequestScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(1);
    captureListProductsRest.expectedMessageCount(0);
    captureListProductsAuditLog.expectedMessageCount(0);
  }

  protected void validateBadRequestScenario() throws Exception {
    captureError.assertIsSatisfied();
    captureListProductsRest.assertIsSatisfied();
  }

  protected void setupListProductsBackendErrorScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(1);
    captureListProductsRest.expectedMessageCount(0);
    captureListProductsAuditLog.expectedMessageCount(0);
  }

  protected void validateBackendErrorScenario(String page, String pageSize) throws Exception {
    captureError.assertIsSatisfied();
    captureListProductsRest.assertIsSatisfied();

    wireMock.verify(getRequestedFor(urlPathEqualTo(productRestApiUri))
        .withQueryParam("page", equalTo(page))
        .withQueryParam("pageSize", equalTo(pageSize)));
  }

}
