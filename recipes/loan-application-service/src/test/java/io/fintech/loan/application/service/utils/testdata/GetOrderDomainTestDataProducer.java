package io.fintech.loan.application.service.utils.testdata;

import io.fintech.loan.application.service.model.domain.Error;
import io.fintech.loan.application.service.model.domain.Order.StatusEnum;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates test data specifically for the Get Order operation.
 */
public class GetOrderDomainTestDataProducer extends BaseDomainTestDataProducer {

  // Request scenario name constants
  public static final class RequestScenarios {

    public static final String GET_ORDER_SUCCESS = "getorder-success-request";
    public static final String GET_ORDER_SUCCESS_NOTFOUND_COMBINATION_1 = "getorder-success-notfound-combination-1-request";
    public static final String GET_ORDER_SUCCESS_NOTFOUND_COMBINATION_2 = "getorder-success-notfound-combination-2-request";
    public static final String GET_ORDER_ERROR_REST_400 = "getorder-backend-error-rest-400-request";
    public static final String GET_ORDER_ERROR_REST_404 = "getorder-backend-error-rest-404-request";
    public static final String GET_ORDER_ERROR_REST_500 = "getorder-backend-error-rest-500-request";
    public static final String GET_ORDER_ERROR_SOAP_CLIENT_ERROR = "getorder-backend-error-soap-clienterror-500-request";
    public static final String GET_ORDER_ERROR_SOAP_SERVER_ERROR = "getorder-backend-error-soap-servererror-500-request";
    public static final String GET_ORDER_ERROR_GRPC_INVALIDARGUMENT = "getorder-backend-error-grpc-invalidargument-request";
    public static final String GET_ORDER_ERROR_GRPC_NOTFOUND = "getorder-backend-error-grpc-notfound-request";
    public static final String GET_ORDER_ERROR_GRPC_INTERNAL = "getorder-backend-error-grpc-internal-request";
    public static final String GET_ORDER_ERROR_NULL_ID = "getorder-interface-nullorderid-error-400-request";
  }

  // Response scenario name constants
  public static final class ResponseScenarios {

    public static final String GET_PURCHASE_SUCCESS = "getpurchase-success-response";
    public static final String GET_PURCHASE_ERROR = "getpurchase-error-response";
  }

  /**
   * Generates test data for get order requests.
   */
  public static List<RequestResponseScenario> generateGetOrderRequests() {
    List<RequestResponseScenario> orders = new ArrayList<>();

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_SUCCESS).order(
        generateOrder("1", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_SUCCESS).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_SUCCESS_NOTFOUND_COMBINATION_1).order(
        generateOrder("111", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_SUCCESS).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_SUCCESS_NOTFOUND_COMBINATION_2).order(
        generateOrder("1", null, WHOLESALE, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_SUCCESS).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_ERROR_REST_400).order(
        generateOrder("2", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_REST_400_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_ERROR_REST_404).order(
        generateOrder("3", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_REST_404_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_ERROR_REST_500).order(
        generateOrder("4", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_REST_500_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_ERROR_SOAP_CLIENT_ERROR).order(
        generateOrder("5", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_SOAP_CLIENT_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_ERROR_SOAP_SERVER_ERROR).order(
        generateOrder("6", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_SOAP_SERVER_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_ERROR_GRPC_INVALIDARGUMENT).order(
        generateOrder("1", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_GRPC_INVALIDARGUMENT_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_ERROR_GRPC_NOTFOUND).order(
        generateOrder("1", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_GRPC_NOTFOUND_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_ERROR_GRPC_INTERNAL).order(
        generateOrder("1", null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_GRPC_INTERNAL_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.GET_ORDER_ERROR_NULL_ID).order(
        generateOrder(null, null, ONLINE_CHANNEL, 0, false, null, null,
            0, null, null)).transactionId(TXID_FOR_SUCCESS).build());

    return orders;
  }

  /**
   * Generates test data for successful get order responses.
   */
  public static List<RequestResponseScenario> generateGetOrderForPurchaseResponses() {
    List<RequestResponseScenario> orders = new ArrayList<>();

    orders.add(RequestResponseScenario.builder().name(ResponseScenarios.GET_PURCHASE_SUCCESS).order(
        generateOrder("1", StatusEnum.PENDING, ONLINE_CHANNEL, 5, true, "10", "Product10",
            1, LocalDate.now(), OffsetDateTime.now())).build());

    return orders;
  }

  /**
   * Generates test data for error responses when getting orders.
   */
  public static List<RequestResponseScenario> generateGetOrderErrorForPurchaseErrorResponse() {
    List<RequestResponseScenario> errors = new ArrayList<>();

    errors.add(RequestResponseScenario.builder().name(ResponseScenarios.GET_PURCHASE_ERROR)
        .error(new Error("0004", "Purchase Api getpurchase error message!")).build());

    return errors;
  }
}