package com.mycompany.product.catalog.routes.producer.rest;

import static com.mycompany.product.catalog.constants.Constants.APPLICATION_JSON;
import static org.apache.camel.Exchange.CONTENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.product.catalog.constants.Constants;
import com.mycompany.product.catalog.mapper.infra.JsonPurchaseMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * Order Route.
 *
 * @author camelbee
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class RestProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final JsonPurchaseMapper jsonPurchaseMapper;
  final ObjectMapper objectMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    final JacksonDataFormat purchasesFormat = new JacksonDataFormat(objectMapper, com.mycompany.product.catalog.model.infra.json.Purchase[].class);

    from("direct:listOrdersRest").routeId("listOrdersRestRoute")
        .removeHeader(Exchange.HTTP_PATH)
        .removeHeader(Exchange.HTTP_URL)
        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
        .setHeader("CamelHttpQuery", simple("page=${header.page}&pageSize=${header.pageSize}&salesChannel=${header.salesChannel}"))
        .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON))
        .setHeader(HttpHeaders.ACCEPT, constant(APPLICATION_JSON))
        .to("http:{{backend-purchase-rest-api.url}}?bridgeEndpoint=true")
        .setHeader("totalOrders", header("totalPurchases"))
        .unmarshal(purchasesFormat)
        .process(e -> {
          e.getIn().setBody(jsonPurchaseMapper.jsonPurchasesToDomainOrders(List.of(e.getIn().getBody(
              com.mycompany.product.catalog.model.infra.json.Purchase[].class))));
        })
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

  }

}
