package com.mycompany.catalog.mcp.routes.producer.rest;

import static org.apache.camel.Exchange.CONTENT_TYPE;

import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.component.jackson.JacksonDataFormat;

import static com.mycompany.catalog.mcp.constants.Constants.APPLICATION_JSON;

import com.mycompany.catalog.mcp.mapper.infra.JsonPurchaseMapper;

import jakarta.ws.rs.core.HttpHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.catalog.mcp.constants.Constants;
import org.camelbee.config.CamelBeeRouteConfigurer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.model.dataformat.AvroLibrary;
import org.apache.camel.model.dataformat.JsonLibrary;
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

      final JacksonDataFormat purchasesFormat = new JacksonDataFormat(objectMapper, com.mycompany.catalog.mcp.model.infra.json.Purchase[].class);





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
          e.getIn().setBody(jsonPurchaseMapper.jsonPurchasesToDomainOrders(List.of(e.getIn().getBody(com.mycompany.catalog.mcp.model.infra.json.Purchase[].class))));
       })
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());


  }


}
