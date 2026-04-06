package com.mycompany.catalog.mcp.utils.testdata;

import com.mycompany.catalog.mcp.model.domain.Error;
import com.mycompany.catalog.mcp.model.domain.Order.StatusEnum;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates test data specifically for the Create Order operation (single order).
 */
public class CreateOrderDomainTestDataProducer extends BaseDomainTestDataProducer {

  // Request scenario name constants
  public static final class RequestScenarios {

    public static final String CREATE_ORDER_SUCCESS = "createorder-success-request";
    public static final String CREATE_ORDER_ERROR_REST_400 = "createorder-backend-error-rest-400-request";
    public static final String CREATE_ORDER_ERROR_REST_404 = "createorder-backend-error-rest-404-request";
    public static final String CREATE_ORDER_ERROR_REST_500 = "createorder-backend-error-rest-500-request";
    public static final String CREATE_ORDER_ERROR_SOAP_CLIENT_ERROR = "createorder-backend-error-soap-clienterror-500-request";
    public static final String CREATE_ORDER_ERROR_SOAP_SERVER_ERROR = "createorder-backend-error-soap-servererror-500-request";
    public static final String CREATE_ORDER_ERROR_NO_ITEMS = "createorder-interface-noorderitems-error-400-request";
    public static final String CREATE_ORDER_ERROR_GRPC_INVALIDARGUMENT = "createorder-backend-error-grpc-invalidargument-request";
    public static final String CREATE_ORDER_ERROR_GRPC_NOTFOUND = "createorder-backend-error-grpc-notfound-request";
    public static final String CREATE_ORDER_ERROR_GRPC_INTERNAL = "createorder-backend-error-grpc-internal-request";
  }

  // Response scenario name constants
  public static final class ResponseScenarios {

    public static final String CREATE_PURCHASE_SUCCESS = "createpurchase-success-response";
    public static final String CREATE_PURCHASE_ERROR = "createpurchase-error-response";
  }

  /**
   * Generates test data for create order requests.
   * These domain Order objects serve three purposes:
   * 1. Direct usage in unit tests where CentralCreateOrderRoute accepts Order objects as exchange body
   * 2. Mapstruct classes unit testing
   * 2. Usage in integration tests after conversion to API interface formats : JSON, XML, Avro, or Protocol Buffers formats
   * with the utility classes under com.mycompany.catalog.mcp.utils.testdata.*
   *    (converted formats are stored under integration test resources to trigger microservice interfaces)
   *    under integration-test/resources/data/inttest/api
   */
  public static List<RequestResponseScenario> generateCreateOrderRequests() {
    List<RequestResponseScenario> orders = new ArrayList<>();

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.CREATE_ORDER_SUCCESS).order(
        generateOrder("", StatusEnum.PENDING, ONLINE_CHANNEL, 10, false, "10", "Product10",
            1, null, null)).transactionId(TXID_FOR_SUCCESS).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.CREATE_ORDER_ERROR_REST_400).order(
        generateOrder("", StatusEnum.PENDING, ONLINE_CHANNEL, 2, false, "40", "Product40",
            1, null, null)).transactionId(TXID_FOR_REST_400_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.CREATE_ORDER_ERROR_REST_404).order(
        generateOrder("", StatusEnum.PENDING, ONLINE_CHANNEL, 2, false, "50", "Product50",
            1, null, null)).transactionId(TXID_FOR_REST_404_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.CREATE_ORDER_ERROR_REST_500).order(
        generateOrder("", StatusEnum.PENDING, ONLINE_CHANNEL, 2, false, "60", "Product60",
            1, null, null)).transactionId(TXID_FOR_REST_500_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.CREATE_ORDER_ERROR_SOAP_CLIENT_ERROR).order(
        generateOrder("", StatusEnum.PENDING, ONLINE_CHANNEL, 2, false, "70", "Product70",
            1, null, null)).transactionId(TXID_FOR_SOAP_CLIENT_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.CREATE_ORDER_ERROR_SOAP_SERVER_ERROR).order(
        generateOrder("", StatusEnum.PENDING, ONLINE_CHANNEL, 2, false, "80", "Product80",
            1, null, null)).transactionId(TXID_FOR_SOAP_SERVER_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.CREATE_ORDER_ERROR_GRPC_INVALIDARGUMENT).order(
        generateOrder("", StatusEnum.PENDING, ONLINE_CHANNEL, 2, false, "40", "Product40",
            1, null, null)).transactionId(TXID_FOR_GRPC_INVALIDARGUMENT_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.CREATE_ORDER_ERROR_GRPC_NOTFOUND).order(
        generateOrder("", StatusEnum.PENDING, ONLINE_CHANNEL, 2, false, "50", "Product50",
            1, null, null)).transactionId(TXID_FOR_GRPC_NOTFOUND_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.CREATE_ORDER_ERROR_GRPC_INTERNAL).order(
        generateOrder("", StatusEnum.PENDING, ONLINE_CHANNEL, 2, false, "60", "Product60",
            1, null, null)).transactionId(TXID_FOR_GRPC_INTERNAL_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.CREATE_ORDER_ERROR_NO_ITEMS).order(
        generateOrder("", StatusEnum.PENDING, ONLINE_CHANNEL, 0, false, null, null,
            1, null, null)).transactionId(TXID_FOR_SUCCESS).build());

    return orders;
  }

  /**
   * Generates test data for successful create order responses.
   * TODO UPDATE comments to mention where this is being used like wiremock, sse, websocket configuration!!!!
   */
  public static List<RequestResponseScenario> generateCreateOrderForPurchaseResponses() {
    List<RequestResponseScenario> orders = new ArrayList<>();

    orders.add(RequestResponseScenario.builder().name(ResponseScenarios.CREATE_PURCHASE_SUCCESS).order(
        generateOrder("1", StatusEnum.PENDING, ONLINE_CHANNEL, 10, true, "10", "Product10",
            1, LocalDate.now(), OffsetDateTime.now())).build());

    return orders;
  }

  /**
   * Generates test data for error responses when creating orders.
   * TODO UPDATE comments to mention where this is being used like wiremock, sse, websocket configuration!!!!
   */
  public static List<RequestResponseScenario> generateCreateOrderErrorForPurchaseErrorResponse() {
    List<RequestResponseScenario> errors = new ArrayList<>();

    errors.add(
        RequestResponseScenario.builder().name(ResponseScenarios.CREATE_PURCHASE_ERROR)
            .error(new Error("0001", "Purchase Api createpurchase error message!"))
            .build());

    return errors;
  }
}