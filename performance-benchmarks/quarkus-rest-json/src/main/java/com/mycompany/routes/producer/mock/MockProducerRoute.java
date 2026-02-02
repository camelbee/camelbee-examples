package com.mycompany.routes.producer.mock;

import com.mycompany.constants.Constants;
import com.mycompany.model.domain.Order;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * Order Route.
 *
 * @author camelbee
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MockProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    // CPD-OFF
    from("direct:createOrderMock").routeId("createOrderMockRoute")
        .process(e -> {

          Order order = e.getProperty(Constants.ORIGINAL_BODY, Order.class);
          order.setId("1");
          order.setOrderDate(LocalDate.now());
          order.setLastUpdateTimestamp(OffsetDateTime.now());

          AtomicInteger counter = new AtomicInteger(1);
          order.getItems().forEach(o -> o.setId(String.valueOf(counter.getAndIncrement())));

          e.getIn().setBody(order);

        })
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

  }

}
