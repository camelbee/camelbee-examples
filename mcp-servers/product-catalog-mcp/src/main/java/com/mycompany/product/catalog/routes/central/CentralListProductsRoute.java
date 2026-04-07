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
 * Central route for ListProducts operation.
 *
 * @author camelbee
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CentralListProductsRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralListProducts").routeId("centralListProductsRoute")
        .setProperty(Constants.ORIGINAL_BODY, body())
        .process(exchange -> {
          ValidationUtils.validateNumericHeader(exchange, "page", 1, Integer.MAX_VALUE);
          ValidationUtils.validateNumericHeader(exchange, "pageSize", 1, 100);
        })
        .to("direct:listProductsRest").id("listProductsRestEndpoint")
        .process(exchange -> {
          String params = String.format("{\"page\":%s,\"pageSize\":%s}",
              exchange.getIn().getHeader("page"),
              exchange.getIn().getHeader("pageSize"));
          AuditLog auditLog = AuditLog.builder()
              .userId(exchange.getIn().getHeader("userId", String.class))
              .toolName("listProducts")
              .parameters(params)
              .timestamp(Instant.now())
              .responseStatus(ResponseStatus.SUCCESS)
              .build();
          exchange.setProperty("auditLog", auditLog);
          exchange.getIn().setBody(auditLog);
        })
        .to("direct:writeAuditLogJpa").id("writeAuditLogJpaEndpoint")
        .setBody(exchangeProperty(Constants.ACTUAL_RESPONSE_BODY));
  }
}
