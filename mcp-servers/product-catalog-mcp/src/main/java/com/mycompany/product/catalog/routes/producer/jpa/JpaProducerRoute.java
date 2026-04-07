package com.mycompany.product.catalog.routes.producer.jpa;

import com.mycompany.product.catalog.constants.Constants;
import com.mycompany.product.catalog.mapper.infra.JpaPurchaseMapper;
import com.mycompany.product.catalog.model.domain.Order;
import com.mycompany.product.catalog.model.infra.jpa.postgresql.Purchase;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

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
        .to("jpa:com.mycompany.product.catalog.model.infra.jpa.postgresql.Purchase")
        .convertBodyTo(Order.class)
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

  }

}
