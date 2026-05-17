package io.fintech.loan.application.service.routes.producer.jpa;

import io.fintech.loan.application.service.constants.Constants;
import io.fintech.loan.application.service.mapper.infra.JpaPurchaseMapper;
import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.model.domain.OrderItem;
import io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase;
import io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase.StatusEnum;
import io.fintech.loan.application.service.model.infra.jpa.postgresql.PurchaseItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

/**
 * Jpa Producer Route.
 *
 * @author camelbee
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JpaProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final JpaPurchaseMapper jpaPurchaseMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:createOrderJpa").routeId("createOrderJpaRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .convertBodyTo(Purchase.class)
        .to("jpa:io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase")
        .convertBodyTo(Order.class)
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

    from("direct:updateOrderJpa").routeId("updateOrderJpaRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .process(this::setJpaParametersFromOrder)
        // First fetch the existing purchase using named query
        .to("jpa:io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase?"
            + "namedQuery=Purchase.findPurchaseBySalesChannelAndId&singleResult=true")
        .process(this::updatePurchase)
        .to("jpa:io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase")
        .convertBodyTo(Order.class)
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

    // CPD-OFF
    from("direct:listOrdersJpa").routeId("listOrdersJpaRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .process(e -> {
          // Set query parameters
          Map<String, Object> params = new HashMap<>();
          params.put("salesChannel", e.getIn().getHeader("salesChannel"));
          e.getIn().setHeader("CamelJpaParameters", params);
        })
        // Get total count using named query
        .to("jpa:io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase?"
            + "namedQuery=Purchase.countPurchasesBySalesChannel&singleResult=true")
        .setProperty("totalPurchases", body())
        .process(exchange -> {
          int page = exchange.getIn().getHeader("page", Integer.class);
          int pageSize = exchange.getIn().getHeader("pageSize", Integer.class);
          int offset = (page - 1) * pageSize;

          // Set JPA pagination headers
          exchange.getIn().setHeader("CamelJpaFirstResult", offset);
          exchange.getIn().setHeader("CamelJpaMaximumResults", pageSize);
        })
        // Fetch paginated purchases using named query
        .to("jpa:io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase?"
            + "namedQuery=Purchase.findPurchasesBySalesChannel")
        .process(e -> {
          e.getIn().setBody(jpaPurchaseMapper.jpaPurchasesToDomainOrders((List<Purchase>) e.getIn().getBody()));

          e.getIn().setHeader("currentPage", e.getIn().getHeader("page"));
          e.getIn().setHeader("totalOrders", e.getProperty("totalPurchases"));

          int pageSize = e.getIn().getHeader("pageSize", Integer.class);
          long totalPurchases = e.getProperty("totalPurchases", Long.class);
          long totalPages = (totalPurchases + pageSize - 1) / pageSize;

          e.getIn().setHeader("totalPages", totalPages);
        })
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

    // CPD-OFF
    from("direct:getOrderJpa").routeId("getOrderJpaRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .process(this::setJpaParametersFromHeaders)
        // Fetch purchase using named query
        .to("jpa:io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase?persistenceUnit=mariadb&"
            + "namedQuery=Purchase.findPurchaseBySalesChannelAndId&singleResult=true")
        .convertBodyTo(Order.class)
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

  }

  private void setJpaParametersFromOrder(Exchange exchange) {

    // Set query parameters
    Map<String, Object> params = new HashMap<>();
    params.put("salesChannel", exchange.getIn().getBody(Order.class).getSalesChannel());
    params.put("id", exchange.getIn().getBody(Order.class).getId());
    exchange.getIn().setHeader("CamelJpaParameters", params);
  }

  private void setJpaParametersFromHeaders(Exchange exchange) {

    // Set query parameters
    Map<String, Object> params = new HashMap<>();
    params.put("salesChannel", exchange.getIn().getHeader("salesChannel"));
    params.put("id", exchange.getIn().getHeader("id"));
    exchange.getIn().setHeader("CamelJpaParameters", params);
  }

  private void updatePurchase(Exchange exchange) {
    Purchase existingPurchase = exchange.getIn().getBody(Purchase.class);
    Order updatedOrder = exchange.getProperty(Constants.ORIGINAL_BODY, Order.class);

    // Update only the changed fields (PATCH behavior)
    if (updatedOrder.getStatus() != null) {
      existingPurchase.setStatus(StatusEnum.fromValue(updatedOrder.getStatus().getValue()));
    }

    if (updatedOrder.getOrderDate() != null) {
      existingPurchase.setPurchaseDate(updatedOrder.getOrderDate());
    }
    if (updatedOrder.getLastUpdateTimestamp() != null) {
      existingPurchase.setLastUpdateTimestamp(updatedOrder.getLastUpdateTimestamp().toLocalDateTime());
    }

    // Update items if provided
    if (updatedOrder.getItems() != null && !updatedOrder.getItems().isEmpty()) {
      // Update existing items and add new ones
      for (OrderItem updatedItem : updatedOrder.getItems()) {

        // check if the supplied id is numeric
        Long purchaseId = Optional.ofNullable(updatedItem.getId())
            .filter(id -> id.matches("\\d+"))
            .map(Long::parseLong)
            .orElse(-1L);

        PurchaseItem existingItem = existingPurchase.getItems().stream()
            .filter(item -> purchaseId.equals(item.getId()))
            .findFirst()
            .orElse(null);

        if (existingItem != null) {
          // Update existing item only if new values are provided
          if (updatedItem.getProductName() != null) {
            existingItem.setProductName(updatedItem.getProductName());
          }
          if (updatedItem.getProductId() != null) {
            existingItem.setProductId(updatedItem.getProductId());
          }
          if (updatedItem.getPrice() != null) {
            existingItem.setPrice(updatedItem.getPrice());
          }
          if (updatedItem.getQuantity() != null) {
            existingItem.setQuantity(updatedItem.getQuantity());
          }
        } else {
          PurchaseItem purchaseItem = jpaPurchaseMapper.domainOrderItemToJsonPurchaseItem(updatedItem);
          purchaseItem.setPurchase(existingPurchase);
          // Add new item
          existingPurchase.getItems().add(purchaseItem);
        }
      }
    }

    exchange.getIn().setBody(existingPurchase);
  }

}
