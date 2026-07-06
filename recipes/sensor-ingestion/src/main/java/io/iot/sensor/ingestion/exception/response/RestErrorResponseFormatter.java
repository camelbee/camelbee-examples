package io.iot.sensor.ingestion.exception.response;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import io.iot.sensor.ingestion.constants.Constants;
import io.iot.sensor.ingestion.exception.ErrorMeta;
import io.iot.sensor.ingestion.model.domain.Error;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;

/**
 * Error mapper for REST endpoints (platform-http).
 * Handles complex error scenarios including SOAP faults and gRPC errors from backend services.
 */
@ApplicationScoped
@Slf4j
public class RestErrorResponseFormatter implements ResponseFormatter {

  @Override
  public boolean supports(Exchange exchange) {
    String from = exchange.getFromEndpoint().toString();
    return from.contains("platform-http");
  }

  @Override
  public int getPriority() {
    return 10; // Higher priority for REST endpoints
  }

  @Override
  public void format(Exchange exchange, ErrorMeta meta) {
    Error error = new Error();

    error.setCode(meta.code());
    error.setMessage(meta.message());
    exchange.getMessage().setHeader(HTTP_RESPONSE_CODE, meta.status());

    // Set content type headers
    if (exchange.getProperty(Constants.ORIGINAL_ACCEPT_CONTENT_TYPE) != null) {
      exchange.getIn().setHeader(CONTENT_TYPE, exchange.getProperty(Constants.ORIGINAL_ACCEPT_CONTENT_TYPE));
    } else {
      exchange.getIn().setHeader(HttpHeaders.ACCEPT, exchange.getProperty(Constants.ORIGINAL_CONTENT_TYPE));
    }

    exchange.getMessage().setBody(error);
  }

}
