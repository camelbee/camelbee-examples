package com.mycompany.product.catalog.routes.central;

import com.mycompany.product.catalog.constants.Constants;
import com.mycompany.product.catalog.model.domain.AuditLog;
import com.mycompany.product.catalog.model.domain.ResponseStatus;
import com.mycompany.product.catalog.utils.ValidationUtils;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * Central route for SearchProducts operation.
 *
 * @author camelbee
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CentralSearchProductsRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralSearchProducts").routeId("centralSearchProductsRoute")
        .setProperty(Constants.ORIGINAL_BODY, body())
        .process(exchange -> {
          ValidationUtils.validateNumericHeader(exchange, "page", 1, Integer.MAX_VALUE);
          ValidationUtils.validateNumericHeader(exchange, "pageSize", 1, 100);
        })
        .to("direct:searchProductsRest").id("searchProductsRestEndpoint")
        .process(exchange -> {
          String params = String.format(
              "{\"query\":\"%s\",\"category\":\"%s\",\"minPrice\":%s,\"maxPrice\":%s,\"inStock\":%s,\"page\":%s,\"pageSize\":%s}",
              exchange.getIn().getHeader("query", ""),
              exchange.getIn().getHeader("category", ""),
              exchange.getIn().getHeader("minPrice", "null"),
              exchange.getIn().getHeader("maxPrice", "null"),
              exchange.getIn().getHeader("inStock", "null"),
              exchange.getIn().getHeader("page"),
              exchange.getIn().getHeader("pageSize"));
          AuditLog auditLog = AuditLog.builder()
              .userId(exchange.getIn().getHeader("userId", String.class))
              .toolName("searchProducts")
              .parameters(params)
              .timestamp(Instant.now())
              .responseStatus(ResponseStatus.SUCCESS)
              .build();
          exchange.getIn().setBody(auditLog);
        })
        .to("direct:writeAuditLogJpa").id("searchWriteAuditLogJpaEndpoint")
        .setBody(exchangeProperty(Constants.ACTUAL_RESPONSE_BODY));
  }
}
