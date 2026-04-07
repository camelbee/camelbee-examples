package com.mycompany.product.catalog.routes.consumer.mcp;

import com.mycompany.product.catalog.exception.GenericExceptionHandler;
import com.mycompany.product.catalog.mapper.api.McpProductMapper;
import com.mycompany.product.catalog.model.domain.PaginatedResponse;
import com.mycompany.product.catalog.model.domain.Product;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * MCP Consumer Route for Product Catalog operations.
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
  final McpProductMapper mcpProductMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(genericExceptionHandler.appErrorHandler());

    from("direct:mcpListProducts")
        .routeId("mcpListProductsRoute")
        .to("direct:centralListProducts")
        .process(e -> {
          PaginatedResponse domainResponse = e.getIn().getBody(PaginatedResponse.class);
          e.getIn().setBody(mcpProductMapper.domainToMcpPaginatedResponse(domainResponse));
        });

    from("direct:mcpSearchProducts")
        .routeId("mcpSearchProductsRoute")
        .to("direct:centralSearchProducts")
        .process(e -> {
          PaginatedResponse domainResponse = e.getIn().getBody(PaginatedResponse.class);
          e.getIn().setBody(mcpProductMapper.domainToMcpPaginatedResponse(domainResponse));
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
