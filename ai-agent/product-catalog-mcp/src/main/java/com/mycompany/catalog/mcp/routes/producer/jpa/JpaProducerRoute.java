package com.mycompany.catalog.mcp.routes.producer.jpa;

import com.mycompany.catalog.mcp.constants.Constants;
import com.mycompany.catalog.mcp.mapper.infra.JpaPurchaseMapper;
import com.mycompany.catalog.mcp.model.domain.Order;
import com.mycompany.catalog.mcp.model.infra.jpa.postgresql.Purchase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.camelbee.config.CamelBeeRouteConfigurer;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * Jpa Producer Route.
 *
 * @author camelbee
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class JpaProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final JpaPurchaseMapper jpaPurchaseMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:createOrderJpa").routeId("createOrderJpaRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .convertBodyTo(Purchase.class)
        .to("jpa:com.mycompany.catalog.mcp.model.infra.jpa.postgresql.Purchase")
        .convertBodyTo(Order.class)
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());






  }




}
