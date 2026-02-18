package com.mycompany.routes.consumer.mcp;

import com.mycompany.exception.GenericExceptionHandler;
import com.mycompany.mapper.api.McpOrderMapper;
import com.mycompany.model.api.mcp.Order;
import jakarta.enterprise.context.ApplicationScoped;
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

    from("direct:mcpCreateOrder")
        .routeId("mcpCreateOrderRoute")
        .convertBodyTo(com.mycompany.model.domain.Order.class)
        .to("direct:centralCreateOrder")
        .convertBodyTo(Order.class);

  }

}
