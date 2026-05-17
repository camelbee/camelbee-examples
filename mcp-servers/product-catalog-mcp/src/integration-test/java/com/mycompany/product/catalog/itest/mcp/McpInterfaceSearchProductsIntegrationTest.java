package com.mycompany.product.catalog.itest.mcp;

import static com.mycompany.product.catalog.utils.testdata.BaseDomainTestDataProducer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.mycompany.product.catalog.itest.IntegrationTestProfile;
import com.mycompany.product.catalog.itest.SearchProductsIntegrationTest;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.util.HashMap;
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
 * Integration test for MCP interface SearchProducts operation.
 */
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@DisplayName("MCP SearchProducts Integration Tests")
public class McpInterfaceSearchProductsIntegrationTest extends SearchProductsIntegrationTest {

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

  private static Stream<Arguments> searchProductsSuccessParameters() {
    return Stream.of(
        arguments("wireless", "Electronics", 1, 10, 2, TXID_FOR_SUCCESS),
        arguments(null, null, 1, 10, 2, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("searchProductsSuccessParameters")
  @DisplayName("Should search products successfully")
  void given_ValidParams_When_SearchProductsCalled_Then_ResultIsSuccess(
      String query, String category, int page, int pageSize, int expectedProducts, String transactionId) throws Exception {

    setupSearchProductsSuccessScenario();

    Map<String, Object> args = new HashMap<>();
    args.put("page", page);
    args.put("pageSize", pageSize);
    args.put("userId", "test-user");
    args.put("transactionId", transactionId);
    if (query != null) {
      args.put("query", query);
    }
    if (category != null) {
      args.put("category", category);
    }

    client.when()
        .toolsCall("searchProducts", args, r -> {
          assertThat(r).isNotNull();
          assertThat(r.content()).isNotEmpty();
        })
        .thenAssertResults();

    validateSearchProductsSuccessScenario(expectedProducts);
  }

  // =========================================================================
  // ERROR
  // =========================================================================

  private static Stream<Arguments> searchProductsErrorParameters() {
    return Stream.of(
        arguments("1", "10", TXID_FOR_REST_500_ERROR)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("searchProductsErrorParameters")
  @DisplayName("Should return error when REST backend fails")
  void given_ValidParams_When_SearchProductsCalled_And_RestBackendFails_Then_ResultIsError(
      String page, String pageSize, String transactionId) throws Exception {

    setupSearchProductsErrorScenario();

    client.when()
        .toolsCall("searchProducts", Map.of(
            "page", Integer.parseInt(page),
            "pageSize", Integer.parseInt(pageSize),
            "userId", "test-user",
            "transactionId", transactionId
        ), r -> {
          assertThat(r).isNotNull();
          assertThat(r.isError()).isTrue();
        })
        .thenAssertResults();

    validateSearchProductsErrorScenario();
  }
}
