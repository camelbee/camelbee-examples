package com.mycompany.product.catalog.itest.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.mycompany.product.catalog.itest.GetProductIntegrationTest;
import com.mycompany.product.catalog.itest.IntegrationTestProfile;
import com.mycompany.product.catalog.utils.testdata.BaseDomainTestDataProducer;
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
        arguments("prod-001", BaseDomainTestDataProducer.TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("getProductSuccessParameters")
  @DisplayName("Should get product by ID successfully")
  void given_ValidId_When_GetProductCalled_Then_ResultIsSuccess(
      String productId, String transactionId) throws Exception {

    setupGetProductSuccessScenario();

    client.when()
        .toolsCall("getProduct", Map.of(
            "id", productId,
            "userId", "test-user",
            "transactionId", transactionId
        ), r -> {
          assertThat(r).isNotNull();
          assertThat(r.content()).isNotEmpty();
        })
        .thenAssertResults();

    validateGetProductSuccessScenario(productId);
  }

  // =========================================================================
  // VALIDATION ERROR
  // =========================================================================

  private static Stream<Arguments> getProductErrorParameters() {
    return Stream.of(
        arguments("", BaseDomainTestDataProducer.TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("getProductErrorParameters")
  @DisplayName("Should return error when product ID is empty")
  void given_EmptyId_When_GetProductCalled_Then_ResultIsError(
      String productId, String transactionId) throws Exception {

    setupGetProductErrorScenario();

    client.when()
        .toolsCall("getProduct", Map.of(
            "id", productId,
            "userId", "test-user",
            "transactionId", transactionId
        ), r -> {
          assertThat(r).isNotNull();
          assertThat(r.isError()).isTrue();
        })
        .thenAssertResults();

    validateGetProductErrorScenario();
  }
}
