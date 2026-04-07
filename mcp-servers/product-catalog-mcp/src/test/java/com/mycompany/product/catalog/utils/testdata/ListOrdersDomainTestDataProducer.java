package com.mycompany.product.catalog.utils.testdata;

import com.mycompany.product.catalog.model.domain.Error;
import com.mycompany.product.catalog.model.domain.Order;
import com.mycompany.product.catalog.model.domain.Order.StatusEnum;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates test data specifically for the List Orders operation.
 */
public class ListOrdersDomainTestDataProducer extends BaseDomainTestDataProducer {

  // Request scenario name constants
  public static final class RequestScenarios {

    public static final String LIST_ORDERS_SUCCESS_PAGE_1 = "listorders-success-page-1-request";
    public static final String LIST_ORDERS_SUCCESS_PAGE_2 = "listorders-success-page-2-request";
    public static final String LIST_ORDERS_SUCCESS_PAGE_3 = "listorders-success-page-3-request";
    public static final String LIST_ORDERS_SUCCESS_PAGE_4 = "listorders-success-page-4-request";
    public static final String LIST_ORDERS_SUCCESS_PAGE_EMPTY = "listorders-success-page-empty-request";
    public static final String LIST_ORDERS_ERROR_EMPTY_SALES_CHANNEL = "listorders-interface-emptysaleschannel-error-400-request";
    public static final String LIST_ORDERS_ERROR_INVALID_SALES_CHANNEL = "listorders-interface-invalidsaleschannel-error-400-request";
    public static final String LIST_ORDERS_ERROR_REST_400 = "listorders-backend-error-rest-400-request";
    public static final String LIST_ORDERS_ERROR_REST_404 = "listorders-backend-error-rest-404-request";
    public static final String LIST_ORDERS_ERROR_REST_500 = "listorders-backend-error-rest-500-request";
    public static final String LIST_ORDERS_ERROR_SOAP_CLIENT_ERROR = "listorders-backend-error-soap-clienterror-500-request";
    public static final String LIST_ORDERS_ERROR_SOAP_SERVER_ERROR = "listorders-backend-error-soap-servererror-500-request";
    public static final String LIST_ORDERS_ERROR_GRPC_INVALIDARGUMENT = "listorders-backend-error-grpc-invalidargument-request";
    public static final String LIST_ORDERS_ERROR_GRPC_NOTFOUND = "listorders-backend-error-grpc-notfound-request";
    public static final String LIST_ORDERS_ERROR_GRPC_INTERNAL = "listorders-backend-error-grpc-internal-request";
  }

  // Response scenario name constants
  public static final class ResponseScenarios {

    public static final String LIST_PURCHASES_SUCCESS_FORMAT = "listpurchases-success-page-%d-response";
    public static final String LIST_PURCHASES_ERROR = "listpurchases-error-response";
  }

  /**
   * Generates test data for list orders requests.
   */
  public static List<RequestResponseScenario> generateListOrdersRequests() {
    List<RequestResponseScenario> orders = new ArrayList<>();

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_1).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_SUCCESS).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_2).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("2").pageSize("5").transactionId(TXID_FOR_SUCCESS).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_3).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("3").pageSize("5").transactionId(TXID_FOR_SUCCESS).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_4).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("4").pageSize("5").transactionId(TXID_FOR_SUCCESS).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_EMPTY).salesChannel(WHOLESALE).order(
        generateOrder("", null, WHOLESALE, 0, false, null, null,
            0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_SUCCESS).build());

    orders.add(RequestResponseScenario.builder().transactionId(TXID_FOR_SUCCESS).name(RequestScenarios.LIST_ORDERS_ERROR_EMPTY_SALES_CHANNEL).salesChannel(null)
        .order(
            generateOrder("", null, null, 0, false, null, null,
                0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_SUCCESS).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_ERROR_INVALID_SALES_CHANNEL).salesChannel("AONLINE").order(
        generateOrder("", null, "AONLINE", 0, false, null, null,
            0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_SUCCESS).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_ERROR_REST_400).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_REST_400_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_ERROR_REST_404).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_REST_404_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_ERROR_REST_500).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_REST_500_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_ERROR_SOAP_CLIENT_ERROR).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_SOAP_CLIENT_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_ERROR_SOAP_SERVER_ERROR).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_SOAP_SERVER_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_ERROR_GRPC_INVALIDARGUMENT).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_GRPC_INVALIDARGUMENT_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_ERROR_GRPC_NOTFOUND).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_GRPC_NOTFOUND_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.LIST_ORDERS_ERROR_GRPC_INTERNAL).salesChannel(ONLINE_CHANNEL).order(
        generateOrder("", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).page("1").pageSize("5").transactionId(TXID_FOR_GRPC_INTERNAL_ERROR).build());

    return orders;
  }

  /**
   * Generates test data for list orders responses.
   *
   * @param page The page number for the response
   * @return A list of request-response scenarios
   */
  public static List<RequestResponseScenario> generateListOrdersResponse(int page) {
    List<RequestResponseScenario> orders = new ArrayList<>();

    // Number of orders to generate based on page
    int numberOfOrders = switch (page) {
      case 1, 2 -> 5;
      case 3 -> 2;
      default -> 0;
    };

    List<Order> ordersList = new ArrayList<>();

    // Only create OrdersFile if we have orders to generate
    if (numberOfOrders > 0) {
      // Generate the specified number of orders
      for (int i = 0; i < numberOfOrders; i++) {
        ordersList.add(
            generateOrder("" + ((page - 1) * 5 + (i + 1)), StatusEnum.PENDING, ONLINE_CHANNEL, 5, true, "10",
                "Product10", 1, LocalDate.now(), OffsetDateTime.now()));
      }
    }

    orders.add(RequestResponseScenario.builder()
        .name(String.format(ResponseScenarios.LIST_PURCHASES_SUCCESS_FORMAT, page))
        .orders(ordersList)
        .build());

    return orders;
  }

  /**
   * Generates test data for error responses when listing orders.
   */
  public static List<RequestResponseScenario> generateListOrdersErrorResponse() {
    List<RequestResponseScenario> errors = new ArrayList<>();

    errors.add(RequestResponseScenario.builder().name(ResponseScenarios.LIST_PURCHASES_ERROR)
        .error(new Error("0005", "Purchase Api listpurchase error message!")).build());

    return errors;
  }
}