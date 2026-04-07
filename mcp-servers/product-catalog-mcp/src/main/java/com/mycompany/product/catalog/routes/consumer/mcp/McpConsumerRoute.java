package com.mycompany.product.catalog.routes.consumer.mcp;

import com.mycompany.product.catalog.exception.GenericExceptionHandler;
import com.mycompany.product.catalog.mapper.api.McpOrderMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * Grpahql Listener Route.
 *
 * @author camelbee
 */
@ApplicationScoped
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
          e.getIn().setBody(mcpOrderMapper.domainToMcpOrders((List<com.mycompany.product.catalog.model.domain.Order>) e.getIn().getBody()));
        });

    from("direct:mcpCreateOrder")
        .routeId("mcpCreateOrderRoute")
        .convertBodyTo(com.mycompany.product.catalog.model.domain.Order.class)
        .to("direct:centralCreateOrder")
        .convertBodyTo(com.mycompany.product.catalog.model.api.mcp.Order.class);

  }

}
