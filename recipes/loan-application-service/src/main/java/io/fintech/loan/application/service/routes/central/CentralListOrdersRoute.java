package io.fintech.loan.application.service.routes.central;

import io.fintech.loan.application.service.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CentralListOrdersRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralListOrders").routeId("centralListLoanApplicationsRoute")
        .process(exchange -> {
          ValidationUtils.validateNumericHeader(exchange, "page", 0, Integer.MAX_VALUE);
          ValidationUtils.validateNumericHeader(exchange, "pageSize", 1, 100);
        })
        .to("direct:listOrdersJpa").id("listOrdersJpaEndpoint");
  }
}
