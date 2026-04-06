package com.mycompany.catalog.mcp.itest.mcp;

import static com.mycompany.catalog.mcp.utils.testdata.BaseDomainTestDataProducer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.mycompany.catalog.mcp.itest.IntegrationTestProfile;
import com.mycompany.catalog.mcp.itest.ListProductsIntegrationTest;
import com.mycompany.catalog.mcp.utils.testdata.ListProductsDomainTestDataProducer.RequestScenarios;
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
 * Integration test for MCP interface ListProducts operation.
 */
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("MCP ListProducts Integration Tests")
public class McpInterfaceListProductsIntegrationTest extends ListProductsIntegrationTest {

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

  private static Stream<Arguments> listProductsSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_PRODUCTS_SUCCESS_PAGE_1, "1", "10", 3, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("listProductsSuccessParameters")
  @DisplayName("Should list products successfully")
  void given_ValidParameters_When_ListProducts_Then_Success(
      String scenarioName, String page, String pageSize, int expectedProducts, String transactionId) throws Exception {

    setupListProductsSuccessScenario();

    client.when()
        .toolsCall("listProducts", buildToolArgs(page, pageSize, transactionId), r -> {
          assertThat(r).isNotNull();
          assertThat(r.content()).isNotEmpty();
        })
        .thenAssertResults();

    validateListProductsSuccessScenario(page, pageSize, expectedProducts);
  }

  // =========================================================================
  // BAD REQUEST
  // =========================================================================

  private static Stream<Arguments> listProductsBadRequestParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_PRODUCTS_ERROR_INVALID_PAGE, "0", "10", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("listProductsBadRequestParameters")
  @DisplayName("Should return error for invalid parameters")
  void given_InvalidParameters_When_ListProducts_Then_BadRequest(
      String scenarioName, String page, String pageSize, String transactionId) throws Exception {

    setupListProductsBadRequestScenario();

    client.when()
        .toolsCall("listProducts", buildToolArgs(page, pageSize, transactionId), r -> {
          assertThat(r).isNotNull();
          assertThat(r.isError()).isTrue();
        })
        .thenAssertResults();

    validateBadRequestScenario();
  }

  // =========================================================================
  // BACKEND ERROR
  // =========================================================================

  private static Stream<Arguments> listProductsBackendErrorParameters() {
    return Stream.of(
        arguments("REST_400", "1", "10", TXID_FOR_REST_400_ERROR),
        arguments("REST_500", "1", "10", TXID_FOR_REST_500_ERROR)
    );
  }

  @ParameterizedTest
  @Order(3)
  @MethodSource("listProductsBackendErrorParameters")
  @DisplayName("Should return error when backend fails")
  void given_ValidParameters_When_ListProducts_And_BackendFails_Then_Error(
      String errorType, String page, String pageSize, String transactionId) throws Exception {

    setupListProductsBackendErrorScenario();

    client.when()
        .toolsCall("listProducts", buildToolArgs(page, pageSize, transactionId), r -> {
          assertThat(r).isNotNull();
          assertThat(r.isError()).isTrue();
        })
        .thenAssertResults();

    validateBackendErrorScenario(page, pageSize);
  }

  // =========================================================================
  // HELPERS
  // =========================================================================

  private Map<String, Object> buildToolArgs(String page, String pageSize, String transactionId) {
    return Map.of(
        "page", Integer.parseInt(page),
        "pageSize", Integer.parseInt(pageSize),
        "transactionId", transactionId
    );
  }

}
