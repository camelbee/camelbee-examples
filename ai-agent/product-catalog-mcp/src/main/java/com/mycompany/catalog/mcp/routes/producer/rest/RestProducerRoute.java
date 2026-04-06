package com.mycompany.catalog.mcp.routes.producer.rest;

import static com.mycompany.catalog.mcp.constants.Constants.APPLICATION_JSON;
import static org.apache.camel.Exchange.CONTENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.catalog.mcp.constants.Constants;
import com.mycompany.catalog.mcp.mapper.infra.JsonProductMapper;
import com.mycompany.catalog.mcp.model.domain.ProductPage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * REST Producer Route for product catalog backend calls.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class RestProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final JsonProductMapper jsonProductMapper;
  final ObjectMapper objectMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    final JacksonDataFormat productsFormat = new JacksonDataFormat(objectMapper,
        com.mycompany.catalog.mcp.model.infra.json.Product[].class);

    final JacksonDataFormat productFormat = new JacksonDataFormat(objectMapper,
        com.mycompany.catalog.mcp.model.infra.json.Product.class);

    // =========================================================================
    // LIST PRODUCTS
    // =========================================================================
    from("direct:listProductsRest").routeId("listProductsRestRoute")
        .removeHeader(Exchange.HTTP_PATH)
        .removeHeader(Exchange.HTTP_URL)
        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
        .setHeader("CamelHttpQuery", simple("page=${header.page}&pageSize=${header.pageSize}"))
        .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON))
        .setHeader(HttpHeaders.ACCEPT, constant(APPLICATION_JSON))
        .to("http:{{backend-product-rest-api.url}}?bridgeEndpoint=true")
        .unmarshal(productsFormat)
        .process(e -> {
          var jsonProducts = List.of(e.getIn().getBody(com.mycompany.catalog.mcp.model.infra.json.Product[].class));
          var domainProducts = jsonProductMapper.jsonProductsToDomainProducts(jsonProducts);
          int totalItems = e.getIn().getHeader("totalItems", 0, Integer.class);
          int totalPages = e.getIn().getHeader("totalPages", 0, Integer.class);
          int page = e.getIn().getHeader("page", 1, Integer.class);
          int pageSize = e.getIn().getHeader("pageSize", 10, Integer.class);
          e.getIn().setBody(ProductPage.builder()
              .products(domainProducts)
              .page(page)
              .pageSize(pageSize)
              .totalPages(totalPages)
              .totalItems(totalItems)
              .build());
        })
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

    // =========================================================================
    // SEARCH PRODUCTS
    // =========================================================================
    from("direct:searchProductsRest").routeId("searchProductsRestRoute")
        .removeHeader(Exchange.HTTP_PATH)
        .removeHeader(Exchange.HTTP_URL)
        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
        .process(e -> {
          StringBuilder query = new StringBuilder();
          query.append("page=").append(e.getIn().getHeader("page", "1"));
          query.append("&pageSize=").append(e.getIn().getHeader("pageSize", "10"));
          String q = e.getIn().getHeader("query", String.class);
          if (q != null && !q.isBlank()) {
            query.append("&query=").append(q);
          }
          String cat = e.getIn().getHeader("category", String.class);
          if (cat != null && !cat.isBlank()) {
            query.append("&category=").append(cat);
          }
          Object minPrice = e.getIn().getHeader("minPrice");
          if (minPrice != null) {
            query.append("&minPrice=").append(minPrice);
          }
          Object maxPrice = e.getIn().getHeader("maxPrice");
          if (maxPrice != null) {
            query.append("&maxPrice=").append(maxPrice);
          }
          Object inStock = e.getIn().getHeader("inStock");
          if (inStock != null) {
            query.append("&inStock=").append(inStock);
          }
          e.getIn().setHeader("CamelHttpQuery", query.toString());
        })
        .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON))
        .setHeader(HttpHeaders.ACCEPT, constant(APPLICATION_JSON))
        .to("http:{{backend-product-rest-api.search-url}}?bridgeEndpoint=true")
        .unmarshal(productsFormat)
        .process(e -> {
          var jsonProducts = List.of(e.getIn().getBody(com.mycompany.catalog.mcp.model.infra.json.Product[].class));
          var domainProducts = jsonProductMapper.jsonProductsToDomainProducts(jsonProducts);
          int totalItems = e.getIn().getHeader("totalItems", 0, Integer.class);
          int totalPages = e.getIn().getHeader("totalPages", 0, Integer.class);
          int page = e.getIn().getHeader("page", 1, Integer.class);
          int pageSize = e.getIn().getHeader("pageSize", 10, Integer.class);
          e.getIn().setBody(ProductPage.builder()
              .products(domainProducts)
              .page(page)
              .pageSize(pageSize)
              .totalPages(totalPages)
              .totalItems(totalItems)
              .build());
        })
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

    // =========================================================================
    // GET PRODUCT
    // =========================================================================
    from("direct:getProductRest").routeId("getProductRestRoute")
        .removeHeader(Exchange.HTTP_PATH)
        .removeHeader(Exchange.HTTP_URL)
        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
        .setHeader(Exchange.HTTP_PATH, simple("${header.productId}"))
        .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON))
        .setHeader(HttpHeaders.ACCEPT, constant(APPLICATION_JSON))
        .to("http:{{backend-product-rest-api.url}}?bridgeEndpoint=true")
        .unmarshal(productFormat)
        .process(e -> {
          var jsonProduct = e.getIn().getBody(com.mycompany.catalog.mcp.model.infra.json.Product.class);
          e.getIn().setBody(jsonProductMapper.jsonProductToDomainProduct(jsonProduct));
        })
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

  }

}
