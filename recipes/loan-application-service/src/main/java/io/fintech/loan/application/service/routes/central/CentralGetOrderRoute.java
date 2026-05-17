package io.fintech.loan.application.service.routes.central;

import io.fintech.loan.application.service.constants.Constants;
import io.fintech.loan.application.service.exception.DataNotFoundException;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

/**
 * Cache-aside GetLoanApplication:
 * 1. read Redis (direct:getOrderCache)
 * 2. on miss, read JPA (direct:getOrderJpa)
 * 3. on JPA hit, warm the cache (direct:loanApplicationCacheWrite)
 *
 * <p>Each step lives on its own producer route id so they appear as distinct
 * paths in the CamelBee topology.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CentralGetOrderRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralGetOrder").routeId("centralGetLoanApplicationRoute")
        .process(this::ensureApplicationIdHeader)
        .to("direct:getOrderCache").id("getOrderCacheEndpoint")
        .choice()
        .when(body().isNull())
        .log("cache-miss applicationId=${header.applicationId}")
        .to("direct:getOrderJpa").id("getOrderJpaEndpoint")
        .process(this::ensureFoundOrThrow)
        .setProperty(Constants.ORIGINAL_BODY, body())
        .to("direct:loanApplicationCacheWrite").id("cacheWarmOnMissEndpoint")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .otherwise()
        .log("cache-hit applicationId=${header.applicationId}")
        .end()
        .convertBodyTo(LoanApplication.class);
  }

  private void ensureApplicationIdHeader(Exchange exchange) throws ValidationException {
    String applicationId = exchange.getIn().getHeader("applicationId", String.class);
    if (applicationId == null || applicationId.isBlank()) {
      throw new ValidationException(exchange, "applicationId is required");
    }
  }

  private void ensureFoundOrThrow(Exchange exchange) {
    if (exchange.getIn().getBody() == null) {
      String id = exchange.getIn().getHeader("applicationId", String.class);
      throw new DataNotFoundException("ERROR-NOT-FOUND", "Loan application not found: " + id);
    }
  }
}
