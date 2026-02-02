package com.mycompany.routes.consumer.rest;

import static com.mycompany.constants.Constants.APPLICATION_JSON;
import static com.mycompany.constants.Constants.ORIGINAL_ACCEPT_CONTENT_TYPE;
import static com.mycompany.constants.Constants.ORIGINAL_CONTENT_TYPE;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import com.mycompany.exception.GenericExceptionHandler;
import com.mycompany.mapper.api.ProtoOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.platform.http.PlatformHttpEndpoint;
import org.apache.camel.model.rest.RestBindingMode;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Rest Listener Route.
 *
 * @author camelbee
 */
@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("PMD.TooManyStaticImports")
public class RestConsumerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final GenericExceptionHandler genericExceptionHandler;

  final ProtoOrderMapper protoOrderMapper;

  /**
   * Configure.
   *
   * @throws Exception the exception
   */
  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(genericExceptionHandler.appErrorHandler());

    // setUseStreaming to true not to apply encoding to the binary data for protobuf or avro
    PlatformHttpEndpoint endpoint = getContext().getEndpoint("platform-http:///camelbee-service?matchOnUriPrefix=true", PlatformHttpEndpoint.class);
    endpoint.setUseStreaming(true);
    /*
     turn off binding for supporting json, xml and protobuf.
     even it is only json it is good to unmarshal the incoming data to objects
     in a controlled way, so you will handle parsing errors properly in your globalErrorProcessor
     */
    restConfiguration().bindingMode(RestBindingMode.off);

    rest().openApi().specification("openapi/order-api.yaml").missingOperation("ignore");

    from("direct:createOrder")
        .routeId("createOrderOperationRoute")
        .process(this::setOriginalContentTypeProperties)
        .convertBodyTo(byte[].class)
        .unmarshal().protobuf(com.mycompany.model.api.proto.Order.getDefaultInstance())
        .convertBodyTo(com.mycompany.model.domain.Order.class)
        .to("direct:centralCreateOrder")
        .convertBodyTo(com.mycompany.model.api.proto.Order.class)
        .marshal().protobuf()
        .process(this::setOriginalContentTypePropertiesBack)
        .setHeader(HTTP_RESPONSE_CODE, constant(201));

  }

  /**
   * Sets the original content type properties for the exchange.
   * If Accept header is missing, it uses the Content-Type as the Accept content type.
   *
   * @param exchange The Camel exchange
   */
  private void setOriginalContentTypeProperties(Exchange exchange) {
    Message message = exchange.getIn();

    // Store original Content-Type
    String contentType = message.getHeader(CONTENT_TYPE, String.class);

    if (contentType == null) {
      contentType = APPLICATION_JSON;
    }

    exchange.setProperty(ORIGINAL_CONTENT_TYPE, contentType);

    // Get Accept header
    String acceptHeader = message.getHeader(HttpHeaders.ACCEPT, String.class);

    // If Accept header is missing, use Content-Type instead
    if (acceptHeader == null || acceptHeader.isEmpty() || "*/*".equals(acceptHeader)) {
      exchange.setProperty(ORIGINAL_ACCEPT_CONTENT_TYPE, contentType);
    } else {
      exchange.setProperty(ORIGINAL_ACCEPT_CONTENT_TYPE, acceptHeader);
    }
  }

  /**
   * Sets the original content type properties for the exchange.
   * If Accept header is missing, it uses the Content-Type as the Accept content type.
   *
   * @param exchange The Camel exchange
   */
  private void setOriginalContentTypePropertiesBack(Exchange exchange) {
    if (exchange.getProperty(ORIGINAL_ACCEPT_CONTENT_TYPE) != null) {
      exchange.getIn().setHeader(CONTENT_TYPE, exchange.getProperty(ORIGINAL_ACCEPT_CONTENT_TYPE));
    }
    exchange.getIn().removeHeader(HttpHeaders.ACCEPT);
  }
}
