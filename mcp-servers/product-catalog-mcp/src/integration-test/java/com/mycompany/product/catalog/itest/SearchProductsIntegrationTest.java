package com.mycompany.product.catalog.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.product.catalog.model.domain.PaginatedResponse;
import com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog;
import java.util.Arrays;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * Integration test base for SearchProducts operation.
 */
public class SearchProductsIntegrationTest extends IntegrationTest {

  protected static final String searchProductsUri = "/products/search";

  @EndpointInject("mock:captureSearchProductsRest")
  protected MockEndpoint captureSearchProductsRest;

  @EndpointInject("mock:captureSearchWriteAuditLogJpa")
  protected MockEndpoint captureSearchWriteAuditLogJpa;

  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
        captureSearchProductsRest,
        captureSearchWriteAuditLogJpa
    );

    verifyMockEndpoints = Arrays.asList();

    super.setup();

    if (!initialized) {
      AdviceWith.adviceWith(camelContext, "searchProductsRestRoute",
          a -> a.weaveAddLast().to("mock:captureSearchProductsRest"));
      AdviceWith.adviceWith(camelContext, "writeAuditLogJpaRoute",
          a -> a.weaveAddLast().to("mock:captureSearchWriteAuditLogJpa"));
    }

    initialized = true;

    super.resetBeforeAll();
  }

  protected void setupSearchProductsSuccessScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(0);
    captureSearchProductsRest.expectedMessageCount(1);
    captureSearchWriteAuditLogJpa.expectedMessageCount(1);
    clearAuditLogTable();
  }

  protected void validateSearchProductsSuccessScenario(int expectedProducts) throws Exception {
    captureError.assertIsSatisfied();
    captureSearchProductsRest.assertIsSatisfied();
    captureSearchWriteAuditLogJpa.assertIsSatisfied();

    PaginatedResponse response = (PaginatedResponse) captureSearchProductsRest.getReceivedExchanges().get(0).getIn().getBody();
    assertThat(response.getItems()).hasSize(expectedProducts);

    wireMock.verify(getRequestedFor(urlPathEqualTo(searchProductsUri)));

    var auditLogs = queryAuditLogs();
    assertThat(auditLogs).hasSize(1);
    assertThat(((AuditLog) auditLogs.get(0)).getToolName()).isEqualTo("searchProducts");
    assertThat(((AuditLog) auditLogs.get(0)).getResponseStatus()).isEqualTo(AuditLog.ResponseStatusEnum.SUCCESS);
  }

  protected void setupSearchProductsErrorScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(1);
    captureSearchProductsRest.expectedMessageCount(0);
    captureSearchWriteAuditLogJpa.expectedMessageCount(0);
    clearAuditLogTable();
  }

  protected void validateSearchProductsErrorScenario() throws Exception {
    captureError.assertIsSatisfied();
    captureSearchProductsRest.assertIsSatisfied();
    captureSearchWriteAuditLogJpa.assertIsSatisfied();
  }
}
