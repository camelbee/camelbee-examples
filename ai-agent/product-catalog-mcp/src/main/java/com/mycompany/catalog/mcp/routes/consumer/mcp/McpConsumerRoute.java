package com.mycompany.catalog.mcp.routes.consumer.mcp;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import com.mycompany.catalog.mcp.mapper.api.McpOrderMapper;
import com.mycompany.catalog.mcp.exception.GenericExceptionHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
          e.getIn().setBody(mcpOrderMapper.domainToMcpOrders((List<com.mycompany.catalog.mcp.model.domain.Order>) e.getIn().getBody()));
        });


    from("direct:mcpCreateOrder")
        .routeId("mcpCreateOrderRoute")
        .convertBodyTo(com.mycompany.catalog.mcp.model.domain.Order.class)
        .to("direct:centralCreateOrder")
        .convertBodyTo(com.mycompany.catalog.mcp.model.api.mcp.Order.class);






  }

}
