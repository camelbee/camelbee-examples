package com.mycompany.catalog.mcp.itest.mcp;

import static com.mycompany.catalog.mcp.utils.testdata.BaseDomainTestDataProducer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mycompany.catalog.mcp.itest.ListOrdersIntegrationTest;
import com.mycompany.catalog.mcp.utils.JsonSerDe;
import com.mycompany.catalog.mcp.utils.testdata.ListOrdersDomainTestDataProducer.RequestScenarios;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import com.mycompany.catalog.mcp.itest.IntegrationTestProfile;

/**
 * Integration test for MCP interface ListOrders operation.
 */
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class McpInterfaceListOrdersIntegrationTest extends ListOrdersIntegrationTest {


  McpStreamableTestClient client;


  @BeforeAll
  public void setup() throws Exception {
    super.setup();
  }

  @BeforeEach
  public void setupEndpoints() throws Exception {

      client = McpAssured.newStreamableClient()
        .build();
    client.connect();

      wireMock.resetRequests();
  }

  // =========================================================================
  // SUCCESS
  // =========================================================================

  private static Stream<Arguments> listOrdersSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_1, "ONLINE", "1", "5", 5, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_2, "ONLINE", "2", "5", 5, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_3, "ONLINE", "3", "5", 2, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_4, "ONLINE", "4", "5", 0, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_EMPTY, "WHOLESALE", "1", "5", 0, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("listOrdersSuccessParameters")
  void given_ValidQueryParameters_When_ListOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(
      String fileName, String salesChannel, String page, String pageSize, int expectedOrders, String transactionId) throws Exception {

    setupListOrdersSuccessScenario();

    client.when()
        .toolsCall("listOrders", buildToolArgs(salesChannel, page, pageSize, transactionId), r -> {
          assertThat(r).isNotNull();

          // Concatenate all text content
          String jsonArrayText = "[" + r.content().stream()
              .map(content -> content.asText().text())
              .collect(Collectors.joining(",")) + "]";

        validateMcpOrdersFromText(jsonArrayText, expectedOrders);

        })
        .thenAssertResults();

    validateListOrdersSuccessScenario(fileName, page, pageSize, salesChannel, expectedOrders);
  }

  // =========================================================================
  // BAD REQUEST FROM INTERFACE
  // =========================================================================

  private static Stream<Arguments> listOrdersBadHeaderFromTheInterfaceParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_ORDERS_ERROR_EMPTY_SALES_CHANNEL, "", TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_ERROR_INVALID_SALES_CHANNEL, "INVALID", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("listOrdersBadHeaderFromTheInterfaceParameters")
  void given_InValidQueryParameters_When_ListOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String salesChannel, String transactionId) throws Exception {

    setupListOrdersBadRequestFromTheInterfaceScenario();

    client.when()
        .toolsCall("listOrders", buildToolArgs(salesChannel, "1", "5", transactionId), r -> {
          assertThat(r).isNotNull();
          assertThat(r.isError()).isTrue();
        })
        .thenAssertResults();

    validateBadRequestFromTheInterfaceScenario();
  }

  // =========================================================================
  // REST BACKEND ERROR
  // =========================================================================

  private static Stream<Arguments> listOrdersErrorFromTheRestBackendParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_1, "ONLINE", "1", "5", 5, TXID_FOR_REST_400_ERROR),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_2, "ONLINE", "2", "5", 5, TXID_FOR_REST_404_ERROR),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_3, "ONLINE", "3", "5", 5, TXID_FOR_REST_500_ERROR)
    );
  }

  @ParameterizedTest
  @Order(3)
  @MethodSource("listOrdersErrorFromTheRestBackendParameters")
  void given_ValidQueryParameters_When_ListOrderRouteCalled_And_RestBackendReturnedError_Then_ResultIsError(
      String fileName, String salesChannel, String page, String pageSize, int expectedOrders, String transactionId) throws Exception {

    setupListOrdersErrorFromTheRestBackendScenario();

    client.when()
        .toolsCall("listOrders", buildToolArgs(salesChannel, page, pageSize, transactionId), r -> {
          assertThat(r).isNotNull();
          assertThat(r.isError()).isTrue();
        })
        .thenAssertResults();

    validateErrorFromTheRestBackendScenario(page, pageSize, salesChannel);
  }



  // =========================================================================
  // HELPERS
  // =========================================================================

  private Map<String, Object> buildToolArgs(String salesChannel, String page, String pageSize, String transactionId) {
    return Map.of(
        "salesChannel", salesChannel,
        "page", Integer.parseInt(page),
        "pageSize", Integer.parseInt(pageSize),
        "transactionId", transactionId,
        "cursor", lastNextCursor != null ? lastNextCursor : ""
    );
  }

  private void validateMcpOrdersFromText(String resultText, int expectedOrder) {
    try {
      JsonSerDe<List<com.mycompany.catalog.mcp.model.api.mcp.Order>> jsonOrdersSerDe = new JsonSerDe<>(new TypeReference<>() {
      });

      List<com.mycompany.catalog.mcp.model.api.mcp.Order> orders = jsonOrdersSerDe.deserialize(resultText);

      assertThat(orders).hasSize(expectedOrder);
    } catch (Exception e) {
      throw new RuntimeException("Failed to validate MCP orders response", e);
    }
  }

}
