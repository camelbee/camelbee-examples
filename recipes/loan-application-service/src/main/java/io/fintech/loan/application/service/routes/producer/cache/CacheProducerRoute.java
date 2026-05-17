package io.fintech.loan.application.service.routes.producer.cache;

import io.fintech.loan.application.service.constants.Constants;
import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.model.infra.cache.Purchase;
import io.fintech.loan.application.service.model.infra.cache.PurchaseItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.redis.RedisConstants;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

/**
 * Cache Producer Route — write-through cache (CRO/REO/UPO/DEO only).
 *
 * <p>CACHE is a write-through backend: it keeps the cache in sync with primary-backend writes.
 * GEO and LSO are intentionally NOT supported — those are cache-aside / read-through patterns
 * that require conditional routing (check cache first, fall back to backend on miss) and do not
 * fit the fan-out architecture where all backends are called for every operation.
 *
 * @author camelbee
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  private static final String CACHE_KEY_PREFIX = "camelbeeservice:purchase:";

  private void setCachePutHeaders(Exchange e, String key, String jsonValue) {
    e.getIn().setHeader(RedisConstants.COMMAND, "SET");
    e.getIn().setHeader(RedisConstants.KEY, key);
    e.getIn().setHeader(RedisConstants.VALUE, jsonValue);
  }

  private void ensureIds(Purchase p) {
    if (p.getId() == null || p.getId().isEmpty()) {
      p.setId(java.util.UUID.randomUUID().toString());
    }
    if (p.getItems() != null) {
      for (PurchaseItem pi : p.getItems()) {
        if (pi.getId() == null || pi.getId().isEmpty()) {
          pi.setId(java.util.UUID.randomUUID().toString());
        }
      }
    }
  }

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    // CPD-OFF
    from("direct:createOrderCache").routeId("createOrderCacheRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .convertBodyTo(Purchase.class)
        .process(e -> {
          Purchase p = e.getIn().getBody(Purchase.class);
          ensureIds(p);
          e.setProperty("cachedPurchase", p);
        })
        .marshal().json().process(e -> {
          Purchase p = e.getProperty("cachedPurchase", Purchase.class);
          setCachePutHeaders(e, CACHE_KEY_PREFIX + p.getId(), e.getIn().getBody(String.class));
        })
        .to("spring-redis://{{camelbeeservice.cache.host}}:{{camelbeeservice.cache.port}}?redisTemplate=#stringRedisTemplate")
        .process(e -> e.getIn().setBody(e.getProperty("cachedPurchase", Purchase.class)))
        .convertBodyTo(Order.class)
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

    from("direct:updateOrderCache").routeId("updateOrderCacheRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .convertBodyTo(Purchase.class)
        .process(e -> {
          Purchase p = e.getIn().getBody(Purchase.class);
          ensureIds(p);
          e.setProperty("cachedPurchase", p);
        })
        .marshal().json().process(e -> {
          Purchase p = e.getProperty("cachedPurchase", Purchase.class);
          setCachePutHeaders(e, CACHE_KEY_PREFIX + p.getId(), e.getIn().getBody(String.class));
        })
        .to("spring-redis://{{camelbeeservice.cache.host}}:{{camelbeeservice.cache.port}}?redisTemplate=#stringRedisTemplate")
        .process(e -> e.getIn().setBody(e.getProperty("cachedPurchase", Purchase.class)))
        .convertBodyTo(Order.class)
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

  }

}
