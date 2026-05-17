package io.fintech.loan.application.service.routes.consumer.rest;

import static io.fintech.loan.application.service.constants.Constants.APPLICATION_JSON;
import static io.fintech.loan.application.service.constants.Constants.ORIGINAL_ACCEPT_CONTENT_TYPE;
import static io.fintech.loan.application.service.constants.Constants.ORIGINAL_CONTENT_TYPE;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import io.fintech.loan.application.service.exception.GenericExceptionHandler;
import io.fintech.loan.application.service.mapper.api.JsonOrderMapper;
import io.fintech.loan.application.service.model.api.json.Order;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
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

  final JsonOrderMapper jsonOrderMapper;

  /**
   * Configure.
   *
   * @throws Exception the exception
   */
  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(genericExceptionHandler.appErrorHandler());

    /*
     turn off binding for supporting json, xml and protobuf.
     even it is only json it is good to unmarshal the incoming data to objects
     in a controlled way, so you will handle parsing errors properly in your globalErrorProcessor
     */
    restConfiguration().bindingMode(RestBindingMode.off);

    rest().openApi().specification("openapi/order-api.yaml").missingOperation("ignore");

    from("direct:listOrders")
        .routeId("listOrdersRoute")
        .process(this::setOriginalContentTypeProperties)
        .to("direct:centralListOrders")
        .process(e -> {
          e.getIn().setBody(jsonOrderMapper.domainToJsonOrders((List<io.fintech.loan.application.service.model.domain.Order>) e.getIn().getBody()));
        })
        .marshal().json().process(this::setOriginalContentTypePropertiesBack)
        .setHeader(HTTP_RESPONSE_CODE, constant(200));

    from("direct:createOrder")
        .routeId("createOrderOperationRoute")
        .process(this::setOriginalContentTypeProperties)
        .unmarshal().json(JsonLibrary.Jackson, Order.class).to("bean-validator://camelbee")
        .convertBodyTo(io.fintech.loan.application.service.model.domain.Order.class)
        .to("direct:centralCreateOrder")
        .convertBodyTo(Order.class)
        .marshal().json().process(this::setOriginalContentTypePropertiesBack)
        .setHeader(HTTP_RESPONSE_CODE, constant(201));

    from("direct:getOrder")
        .routeId("getOrderOperationRoute")
        .process(this::setOriginalContentTypeProperties)
        .to("direct:centralGetOrder")
        .convertBodyTo(Order.class)
        .marshal().json().process(this::setOriginalContentTypePropertiesBack)
        .setHeader(HTTP_RESPONSE_CODE, constant(200));

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

    // Ensure JAXB marshaling always uses UTF-8 regardless of JVM platform default encoding.
    // Quarkus containers (UBI/Alpine base images) default to ISO-8859-1, which causes
    // <?xml ... encoding="ISO-8859-1"?> in marshaled responses and backend messages.
    exchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");

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
