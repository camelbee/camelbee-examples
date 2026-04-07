package com.mycompany.product.catalog.routes.central;

import com.mycompany.product.catalog.constants.Constants;
import com.mycompany.product.catalog.model.domain.AuditLog;
import com.mycompany.product.catalog.model.domain.ResponseStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * Central route for GetProduct operation.
 *
 * @author camelbee
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CentralGetProductRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralGetProduct").routeId("centralGetProductRoute")
        .setProperty(Constants.ORIGINAL_BODY, body())
        .process(exchange -> {
          String productId = exchange.getIn().getHeader("productId", String.class);
          if (productId == null || productId.isBlank()) {
            throw new org.apache.camel.ValidationException(exchange, "Product ID cannot be empty!");
          }
        })
        .to("direct:getProductRest").id("getProductRestEndpoint")
        .process(exchange -> {
          String params = String.format("{\"id\":\"%s\"}",
              exchange.getIn().getHeader("productId", String.class));
          AuditLog auditLog = AuditLog.builder()
              .userId(exchange.getIn().getHeader("userId", String.class))
              .toolName("getProduct")
              .parameters(params)
              .timestamp(Instant.now())
              .responseStatus(ResponseStatus.SUCCESS)
              .build();
          exchange.getIn().setBody(auditLog);
        })
        .to("direct:writeAuditLogJpa").id("getProductWriteAuditLogJpaEndpoint")
        .setBody(exchangeProperty(Constants.ACTUAL_RESPONSE_BODY));
  }
}
