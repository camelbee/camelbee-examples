package com.mycompany.catalog.mcp.constants;

/**
 * Utility for constant values.
 *
 * @author camelbee
 */
public final class Constants {

  private Constants() {
    throw new IllegalStateException("Utility class");
  }

  public static final String ORIGINAL_BODY = "originalBody";

  public static final String ORIGINAL_ROUTE_BODY = "originalRouteBody";

  public static final String ORIGINAL_REQUEST_BODY = "originalRequestBody";

  public static final String ACTUAL_RESPONSE_BODY = "actualResponseBody";

  public static final String AGGREGATED_BATCH_ORDERS = "aggregatedBatchOrders";

  public static final String ORIGINAL_SPLIT_ITEM_BODY = "originalSplitItemBody";

  public static final String ORIGINAL_CONTENT_TYPE = "originalRequestContentType";

  public static final String ORIGINAL_ACCEPT_CONTENT_TYPE = "originalRequestAcceptContentType";

  public static final String SOAPACTION = "SOAPAction";

  public static final String APPLICATION_JSON = "application/json";

  public static final String APPLICATION_XML = "application/xml";

  public static final String APPLICATION_PROTOBUF = "application/x-protobuf";

  public static final String APPLICATION_AVRO = "avro/binary";

}
