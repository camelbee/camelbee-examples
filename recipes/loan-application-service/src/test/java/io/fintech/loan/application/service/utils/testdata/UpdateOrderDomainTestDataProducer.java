package io.fintech.loan.application.service.utils.testdata;

import io.fintech.loan.application.service.model.domain.Error;
import io.fintech.loan.application.service.model.domain.Order.StatusEnum;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates test data specifically for the Update Order operation.
 */
public class UpdateOrderDomainTestDataProducer extends BaseDomainTestDataProducer {

  // Request scenario name constants
  public static final class RequestScenarios {

    public static final String UPDATE_ORDER_SUCCESS_ID_FORMAT = "updateorder-success-orderid-%d-request";
    public static final String UPDATE_ORDER_ERROR_REST_400 = "updateorder-backend-error-rest-400-request";
    public static final String UPDATE_ORDER_ERROR_REST_404 = "updateorder-backend-error-rest-404-request";
    public static final String UPDATE_ORDER_ERROR_REST_500 = "updateorder-backend-error-rest-500-request";
    public static final String UPDATE_ORDER_ERROR_SOAP_CLIENT_ERROR = "updateorder-backend-error-soap-clienterror-500-request";
    public static final String UPDATE_ORDER_ERROR_SOAP_SERVER_ERROR = "updateorder-backend-error-soap-servererror-500-request";
    public static final String UPDATE_ORDER_ERROR_GRPC_INVALIDARGUMENT = "updateorder-backend-error-grpc-invalidargument-request";
    public static final String UPDATE_ORDER_ERROR_GRPC_NOTFOUND = "updateorder-backend-error-grpc-notfound-request";
    public static final String UPDATE_ORDER_ERROR_GRPC_INTERNAL = "updateorder-backend-error-grpc-internal-request";
    public static final String UPDATE_ORDER_ERROR_NO_ITEMS = "updateorder-interface-noorderitems-error-400-request";
  }

  // Response scenario name constants
  public static final class ResponseScenarios {

    public static final String UPDATE_PURCHASE_SUCCESS = "updatepurchase-success-response";
    public static final String UPDATE_PURCHASE_ERROR = "updatepurchase-error-response";
  }

  /**
   * Generates test data for update order requests.
   */
  public static List<RequestResponseScenario> generateUpdateOrderRequests() {
    List<RequestResponseScenario> orders = new ArrayList<>();

    // For success scenario for updateOrder update 2 items and add 10 more new items
    for (int i = 1; i < 5; i++) {
      RequestResponseScenario successScenario = RequestResponseScenario.builder()
          .name(String.format(RequestScenarios.UPDATE_ORDER_SUCCESS_ID_FORMAT, i))
          .order(generateOrder(String.valueOf(i), StatusEnum.PENDING, ONLINE_CHANNEL, 2, true, "10", "Product10",
              1, null, null))
          .transactionId(TXID_FOR_SUCCESS).build();

      successScenario.getOrder().getItems().addAll(generateOrderItems(i, false, 10, "10", "Product10", 1));
      orders.add(successScenario);
    }

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.UPDATE_ORDER_ERROR_REST_400).order(
        generateOrder("7", StatusEnum.PENDING, ONLINE_CHANNEL, 10, true, "40", "Product40",
            1, null, null)).transactionId(TXID_FOR_REST_400_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.UPDATE_ORDER_ERROR_REST_404).order(
        generateOrder("7", StatusEnum.PENDING, ONLINE_CHANNEL, 10, true, "50", "Product50",
            1, null, null)).transactionId(TXID_FOR_REST_500_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.UPDATE_ORDER_ERROR_REST_500).order(
        generateOrder("7", StatusEnum.PENDING, ONLINE_CHANNEL, 10, true, "60", "Product60",
            1, null, null)).transactionId(TXID_FOR_REST_404_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.UPDATE_ORDER_ERROR_SOAP_CLIENT_ERROR).order(
        generateOrder("6", StatusEnum.PENDING, ONLINE_CHANNEL, 10, true, "70", "Product70",
            1, null, null)).transactionId(TXID_FOR_SOAP_CLIENT_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.UPDATE_ORDER_ERROR_SOAP_SERVER_ERROR).order(
        generateOrder("6", StatusEnum.PENDING, ONLINE_CHANNEL, 10, true, "80", "Product80",
            1, null, null)).transactionId(TXID_FOR_SOAP_SERVER_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.UPDATE_ORDER_ERROR_GRPC_INVALIDARGUMENT).order(
        generateOrder("7", StatusEnum.PENDING, ONLINE_CHANNEL, 10, true, "40", "Product40",
            1, null, null)).transactionId(TXID_FOR_GRPC_INVALIDARGUMENT_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.UPDATE_ORDER_ERROR_GRPC_NOTFOUND).order(
        generateOrder("7", StatusEnum.PENDING, ONLINE_CHANNEL, 10, true, "50", "Product50",
            1, null, null)).transactionId(TXID_FOR_GRPC_NOTFOUND_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.UPDATE_ORDER_ERROR_GRPC_INTERNAL).order(
        generateOrder("7", StatusEnum.PENDING, ONLINE_CHANNEL, 10, true, "60", "Product60",
            1, null, null)).transactionId(TXID_FOR_GRPC_INTERNAL_ERROR).build());

    orders.add(RequestResponseScenario.builder().name(RequestScenarios.UPDATE_ORDER_ERROR_NO_ITEMS).order(
        generateOrder("9", StatusEnum.PENDING, ONLINE_CHANNEL, 0, true, null, null,
            1, null, null)).transactionId(TXID_FOR_SUCCESS).build());

    return orders;
  }

  /**
   * Generates test data for successful update order responses.
   */
  public static List<RequestResponseScenario> generateUpdateOrderForPurchaseResponses() {
    List<RequestResponseScenario> orders = new ArrayList<>();

    orders.add(RequestResponseScenario.builder().name(ResponseScenarios.UPDATE_PURCHASE_SUCCESS).order(
        generateOrder("1", StatusEnum.PENDING, ONLINE_CHANNEL, 10, true, "10", "Product10",
            1, LocalDate.now(), OffsetDateTime.now())).build());

    return orders;
  }

  /**
   * Generates test data for error responses when updating orders.
   */
  public static List<RequestResponseScenario> generateUpdateOrderErrorForPurchaseErrorResponse() {
    List<RequestResponseScenario> errors = new ArrayList<>();

    errors.add(RequestResponseScenario.builder().name(ResponseScenarios.UPDATE_PURCHASE_ERROR)
        .error(new Error("0003", "Purchase Api updatepurchase error message!")).build());

    return errors;
  }
}