package com.mycompany.product.catalog.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.product.catalog.model.domain.PaginatedResponse;
import com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog;
import java.util.Arrays;
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

  @EndpointInject("mock:captureWriteAuditLogJpa")
  protected MockEndpoint captureWriteAuditLogJpa;

  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
        captureListProductsRest,
        captureWriteAuditLogJpa
    );

    verifyMockEndpoints = Arrays.asList();

    super.setup();

    if (!initialized) {
      AdviceWith.adviceWith(camelContext, "listProductsRestRoute",
          a -> a.weaveAddLast().to("mock:captureListProductsRest"));
      AdviceWith.adviceWith(camelContext, "writeAuditLogJpaRoute",
          a -> a.weaveAddLast().to("mock:captureWriteAuditLogJpa"));
    }

    initialized = true;

    super.resetBeforeAll();
  }

  protected void setupListProductsSuccessScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(0);
    captureListProductsRest.expectedMessageCount(1);
    captureWriteAuditLogJpa.expectedMessageCount(1);
    clearAuditLogTable();
  }

  protected void validateListProductsSuccessScenario(String page, String pageSize, int expectedProducts) throws Exception {
    captureError.assertIsSatisfied();
    captureListProductsRest.assertIsSatisfied();
    captureWriteAuditLogJpa.assertIsSatisfied();

    PaginatedResponse response = (PaginatedResponse) captureListProductsRest.getReceivedExchanges().get(0).getIn().getBody();
    assertThat(response.getItems()).hasSize(expectedProducts);

    wireMock.verify(getRequestedFor(urlPathEqualTo(productRestApiUri))
        .withQueryParam("page", equalTo(page))
        .withQueryParam("pageSize", equalTo(pageSize)));

    var auditLogs = queryAuditLogs();
    assertThat(auditLogs).hasSize(1);
    assertThat(((AuditLog) auditLogs.get(0)).getToolName()).isEqualTo("listProducts");
    assertThat(((AuditLog) auditLogs.get(0)).getResponseStatus()).isEqualTo(AuditLog.ResponseStatusEnum.SUCCESS);
  }

  protected void setupListProductsErrorScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(1);
    captureListProductsRest.expectedMessageCount(0);
    captureWriteAuditLogJpa.expectedMessageCount(0);
    clearAuditLogTable();
  }

  protected void validateListProductsErrorScenario() throws Exception {
    captureError.assertIsSatisfied();
    captureListProductsRest.assertIsSatisfied();
    captureWriteAuditLogJpa.assertIsSatisfied();
  }

  protected void setupListProductsRestBackendErrorScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(1);
    captureListProductsRest.expectedMessageCount(0);
    captureWriteAuditLogJpa.expectedMessageCount(0);
    clearAuditLogTable();
  }

  protected void validateListProductsRestBackendErrorScenario(String page, String pageSize) throws Exception {
    captureError.assertIsSatisfied();
    captureListProductsRest.assertIsSatisfied();
    captureWriteAuditLogJpa.assertIsSatisfied();

    wireMock.verify(getRequestedFor(urlPathEqualTo(productRestApiUri))
        .withQueryParam("page", equalTo(page))
        .withQueryParam("pageSize", equalTo(pageSize)));
  }
}
