package io.fintech.loan.application.service.routes.consumer.mcp;

import io.fintech.loan.application.service.exception.GenericExceptionHandler;
import io.fintech.loan.application.service.mapper.api.McpOrderMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

/**
 * Grpahql Listener Route.
 *
 * @author camelbee
 */
@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("PMD.TooManyStaticImports")
public class McpConsumerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final GenericExceptionHandler genericExceptionHandler;

  final McpOrderMapper mcpOrderMapper;

  /**
   * Configure.
   *
   * @throws Exception the exception
   */
  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(genericExceptionHandler.appErrorHandler());

    from("direct:mcpListOrders")
        .routeId("mcpListOrdersRoute")
        .to("direct:centralListOrders")
        .process(e -> {
          e.getIn().setBody(mcpOrderMapper.domainToMcpOrders((List<io.fintech.loan.application.service.model.domain.Order>) e.getIn().getBody()));
        });

    from("direct:mcpCreateOrder")
        .routeId("mcpCreateOrderRoute")
        .convertBodyTo(io.fintech.loan.application.service.model.domain.Order.class)
        .to("direct:centralCreateOrder")
        .convertBodyTo(io.fintech.loan.application.service.model.api.mcp.Order.class);

    from("direct:mcpGetOrder")
        .routeId("mcpGetOrderRoute")
        .to("direct:centralGetOrder")
        .convertBodyTo(io.fintech.loan.application.service.model.api.mcp.Order.class);

  }

}
