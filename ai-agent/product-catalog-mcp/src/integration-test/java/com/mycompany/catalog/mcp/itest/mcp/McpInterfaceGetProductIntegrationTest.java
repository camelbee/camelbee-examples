package com.mycompany.catalog.mcp.itest.mcp;

import static com.mycompany.catalog.mcp.utils.testdata.BaseDomainTestDataProducer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.mycompany.catalog.mcp.itest.GetProductIntegrationTest;
import com.mycompany.catalog.mcp.itest.IntegrationTestProfile;
import com.mycompany.catalog.mcp.utils.testdata.GetProductDomainTestDataProducer.RequestScenarios;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Integration test for MCP interface GetProduct operation.
 */
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("MCP GetProduct Integration Tests")
public class McpInterfaceGetProductIntegrationTest extends GetProductIntegrationTest {

  McpStreamableTestClient client;

  @BeforeAll
  public void setup() throws Exception {
    super.setup();
  }

  @BeforeEach
  public void setupEndpoints() throws Exception {
    client = McpAssured.newStreamableClient().build();
    client.connect();
    wireMock.resetRequests();
  }

  // =========================================================================
  // SUCCESS
  // =========================================================================

  private static Stream<Arguments> getProductSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.GET_PRODUCT_SUCCESS, "prod-001", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("getProductSuccessParameters")
  @DisplayName("Should get product successfully")
  void given_ValidProductId_When_GetProduct_Then_Success(
      String scenarioName, String productId, String transactionId) throws Exception {

    setupGetProductSuccessScenario();

    client.when()
        .toolsCall("getProduct", buildToolArgs(productId, transactionId), r -> {
          assertThat(r).isNotNull();
          assertThat(r.content()).isNotEmpty();
        })
        .thenAssertResults();

    validateGetProductSuccessScenario(productId);
  }

  // =========================================================================
  // BAD REQUEST
  // =========================================================================

  private static Stream<Arguments> getProductBadRequestParameters() {
    return Stream.of(
        arguments(RequestScenarios.GET_PRODUCT_ERROR_EMPTY_ID, "", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("getProductBadRequestParameters")
  @DisplayName("Should return error for empty product ID")
  void given_EmptyProductId_When_GetProduct_Then_BadRequest(
      String scenarioName, String productId, String transactionId) throws Exception {

    setupGetProductBadRequestScenario();

    client.when()
        .toolsCall("getProduct", buildToolArgs(productId, transactionId), r -> {
          assertThat(r).isNotNull();
          assertThat(r.isError()).isTrue();
        })
        .thenAssertResults();

    validateGetProductBadRequestScenario();
  }

  // =========================================================================
  // HELPERS
  // =========================================================================

  private Map<String, Object> buildToolArgs(String productId, String transactionId) {
    return Map.of(
        "productId", productId,
        "transactionId", transactionId
    );
  }

}
