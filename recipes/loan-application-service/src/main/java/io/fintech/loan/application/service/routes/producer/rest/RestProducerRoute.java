package io.fintech.loan.application.service.routes.producer.rest;

import static io.fintech.loan.application.service.constants.Constants.APPLICATION_JSON;
import static org.apache.camel.Exchange.CONTENT_TYPE;

import io.fintech.loan.application.service.constants.Constants;
import io.fintech.loan.application.service.mapper.infra.JsonPurchaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Order Route.
 *
 * @author camelbee
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RestProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final JsonPurchaseMapper jsonPurchaseMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:updateOrderRest").routeId("updateOrderRestRoute")
        .removeHeader(Exchange.HTTP_URL)
        .setHeader(Exchange.HTTP_METHOD, constant("PATCH"))
        .setHeader(Exchange.HTTP_PATH, simple("${header.id}"))
        .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON))
        .setHeader(HttpHeaders.ACCEPT, constant(APPLICATION_JSON))
        .convertBodyTo(io.fintech.loan.application.service.model.infra.json.Purchase.class)
        .marshal().json().to("http:{{backend-purchase-rest-api.url}}?bridgeEndpoint=true")
        .unmarshal().json(JsonLibrary.Jackson, io.fintech.loan.application.service.model.infra.json.Purchase.class).convertBodyTo(
            io.fintech.loan.application.service.model.domain.Order.class)
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

  }

}
