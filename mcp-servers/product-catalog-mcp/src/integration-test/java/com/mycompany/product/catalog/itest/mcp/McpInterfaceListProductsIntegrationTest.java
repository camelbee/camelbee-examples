package com.mycompany.product.catalog.itest.mcp;

import static com.mycompany.product.catalog.utils.testdata.BaseDomainTestDataProducer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.mycompany.product.catalog.itest.IntegrationTestProfile;
import com.mycompany.product.catalog.itest.ListProductsIntegrationTest;
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
        arguments("1", "5", 5, TXID_FOR_SUCCESS),
        arguments("2", "5", 5, TXID_FOR_SUCCESS),
        arguments("3", "5", 2, TXID_FOR_SUCCESS),
        arguments("4", "5", 0, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("listProductsSuccessParameters")
  @DisplayName("Should list products successfully with pagination")
  void given_ValidParams_When_ListProductsCalled_Then_ResultIsSuccess(
      String page, String pageSize, int expectedProducts, String transactionId) throws Exception {

    setupListProductsSuccessScenario();

    client.when()
        .toolsCall("listProducts", Map.of(
            "page", Integer.parseInt(page),
            "pageSize", Integer.parseInt(pageSize),
            "userId", "test-user",
            "transactionId", transactionId
        ), r -> {
          assertThat(r).isNotNull();
          assertThat(r.content()).isNotEmpty();
        })
        .thenAssertResults();

    validateListProductsSuccessScenario(page, pageSize, expectedProducts);
  }

  // =========================================================================
  // ERROR
  // =========================================================================

  private static Stream<Arguments> listProductsRestBackendErrorParameters() {
    return Stream.of(
        arguments("1", "5", TXID_FOR_REST_400_ERROR),
        arguments("1", "5", TXID_FOR_REST_404_ERROR),
        arguments("1", "5", TXID_FOR_REST_500_ERROR)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("listProductsRestBackendErrorParameters")
  @DisplayName("Should return error when REST backend fails")
  void given_ValidParams_When_ListProductsCalled_And_RestBackendFails_Then_ResultIsError(
      String page, String pageSize, String transactionId) throws Exception {

    setupListProductsRestBackendErrorScenario();

    client.when()
        .toolsCall("listProducts", Map.of(
            "page", Integer.parseInt(page),
            "pageSize", Integer.parseInt(pageSize),
            "userId", "test-user",
            "transactionId", transactionId
        ), r -> {
          assertThat(r).isNotNull();
          assertThat(r.isError()).isTrue();
        })
        .thenAssertResults();

    validateListProductsRestBackendErrorScenario(page, pageSize);
  }
}
