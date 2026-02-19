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

    // =============================================================================
    // TEST 3 — MCP Tool + MapStruct + Apache Camel (single Camel routing layer)
    // -----------------------------------------------------------------------------
    // Goal: Measure the overhead of a single Camel route without chaining to
    //       downstream routes. The route performs the MapStruct mapping inline
    //       via a processor, isolating just the Camel context dispatch overhead.
    //
    //       The original full-stack route (commented out below) would chain to
    //       a central route via convertBodyTo + direct:centralCreateOrder.
    //       This simplified version keeps the same MapStruct logic but removes
    //       the extra routing hop to give a cleaner baseline for Camel overhead.
    //
    //       Flow: direct:mcpCreateOrder
    //               ↓  Camel route dispatch
    //             Processor: MapStruct mcpToDomainOrder → domainToMcpOrder
    //               ↓
    //             MCP Tool response → returned to MCP client
    // =============================================================================

    /*
    // Original multi-hop route (chaining to centralCreateOrder):
    from("direct:mcpCreateOrder")
        .routeId("mcpCreateOrderRoute")
        .convertBodyTo(com.mycompany.model.domain.Order.class)
        .to("direct:centralCreateOrder")
        .convertBodyTo(Order.class);
    */

    from("direct:mcpCreateOrder")
        .routeId("mcpCreateOrderRoute")
        .process(e -> {
          Order mcpOrder = e.getIn().getBody(Order.class);
          com.mycompany.model.domain.Order domainOrder = mcpOrderMapper.mcpToDomainOrder(mcpOrder);
          domainOrder.setId("1");
          e.getIn().setBody(mcpOrderMapper.domainToMcpOrder(domainOrder));
        });

  }

}
