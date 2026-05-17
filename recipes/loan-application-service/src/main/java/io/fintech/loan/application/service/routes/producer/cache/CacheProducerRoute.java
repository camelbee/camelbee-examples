package io.fintech.loan.application.service.routes.producer.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fintech.loan.application.service.constants.Constants;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.redis.RedisConstants;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

/**
 * Cache producer routes — Redis write-through for CRO and UPO, plus an
 * explicit GET path so the cache-aside flow in CentralGetOrderRoute renders
 * as distinct nodes in the CamelBee topology.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheProducerRoute extends RouteBuilder {

  private static final String CACHE_KEY_PREFIX = "loanapplicationservice:loan-application:";
  private static final long TTL_SECONDS = 3600L;
  private static final String REDIS_ENDPOINT = "spring-redis://{{camelbeeservice.cache.host}}:{{camelbeeservice.cache.port}}?redisTemplate=#stringRedisTemplate";

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final ObjectMapper objectMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    // Shared write path used by CRO/UPO and by the cache-aside warm-on-miss flow.
    from("direct:loanApplicationCacheWrite").routeId("loanApplicationCacheWriteRoute")
        .process(e -> {
          LoanApplication app = e.getIn().getBody(LoanApplication.class);
          String json = objectMapper.writeValueAsString(app);
          e.getIn().setHeader(RedisConstants.COMMAND, "SETEX");
          e.getIn().setHeader(RedisConstants.KEY, CACHE_KEY_PREFIX + app.getApplicationId());
          e.getIn().setHeader(RedisConstants.VALUE, json);
          e.getIn().setHeader(RedisConstants.TIMEOUT, TTL_SECONDS);
        })
        .to(REDIS_ENDPOINT);

    // CRO — write cache after JPA insert.
    from("direct:createOrderCache").routeId("createLoanApplicationCacheRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .to("direct:loanApplicationCacheWrite").id("cacheWriteOnCreateEndpoint")
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, exchangeProperty(Constants.ORIGINAL_BODY))
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY));

    // UPO — overwrite cache after JPA update.
    from("direct:updateOrderCache").routeId("updateLoanApplicationCacheRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .to("direct:loanApplicationCacheWrite").id("cacheWriteOnUpdateEndpoint")
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, exchangeProperty(Constants.ORIGINAL_BODY))
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY));

    // GEO step 1 — Redis read. Body becomes the cached LoanApplication or null.
    from("direct:getOrderCache").routeId("getLoanApplicationCacheRoute")
        .process(this::redisGetHeaders)
        .to(REDIS_ENDPOINT)
        .process(e -> {
          // Coerce through Camel's type converter — Redis returns the value as a String
          // wrapped in a stream cache, so a direct cast to byte[]/String would fail.
          String json = e.getIn().getBody(String.class);
          if (json == null || json.isEmpty()) {
            e.getIn().setBody(null);
            return;
          }
          e.getIn().setBody(objectMapper.readValue(json, LoanApplication.class));
        });
  }

  private void redisGetHeaders(Exchange exchange) {
    String applicationId = exchange.getIn().getHeader("applicationId", String.class);
    exchange.getIn().setHeader(RedisConstants.COMMAND, "GET");
    exchange.getIn().setHeader(RedisConstants.KEY, CACHE_KEY_PREFIX + applicationId);
  }
}
