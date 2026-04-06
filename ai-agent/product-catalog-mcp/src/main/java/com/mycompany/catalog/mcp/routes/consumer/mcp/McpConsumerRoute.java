package com.mycompany.catalog.mcp.routes.consumer.mcp;

import com.mycompany.catalog.mcp.exception.GenericExceptionHandler;
import com.mycompany.catalog.mcp.mapper.api.McpProductMapper;
import com.mycompany.catalog.mcp.model.domain.Product;
import com.mycompany.catalog.mcp.model.domain.ProductPage;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * MCP Consumer Route for product catalog operations.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("PMD.TooManyStaticImports")
public class McpConsumerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final GenericExceptionHandler genericExceptionHandler;

  final McpProductMapper mcpProductMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(genericExceptionHandler.appErrorHandler());

    from("direct:mcpListProducts")
        .routeId("mcpListProductsRoute")
        .to("direct:centralListProducts")
        .process(e -> {
          ProductPage domainPage = e.getIn().getBody(ProductPage.class);
          e.getIn().setBody(mcpProductMapper.domainToMcpProductPage(domainPage));
        });

    from("direct:mcpSearchProducts")
        .routeId("mcpSearchProductsRoute")
        .to("direct:centralSearchProducts")
        .process(e -> {
          ProductPage domainPage = e.getIn().getBody(ProductPage.class);
          e.getIn().setBody(mcpProductMapper.domainToMcpProductPage(domainPage));
        });

    from("direct:mcpGetProduct")
        .routeId("mcpGetProductRoute")
        .to("direct:centralGetProduct")
        .process(e -> {
          Product domainProduct = e.getIn().getBody(Product.class);
          e.getIn().setBody(mcpProductMapper.domainToMcpProduct(domainProduct));
        });

  }

}
