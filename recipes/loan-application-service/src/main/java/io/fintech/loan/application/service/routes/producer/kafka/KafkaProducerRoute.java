package io.fintech.loan.application.service.routes.producer.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fintech.loan.application.service.constants.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

/**
 * Kafka Producer Route.
 *
 * @author camelbee
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final ObjectMapper objectMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:createOrderKafka").routeId("createOrderKafkaRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .convertBodyTo(io.fintech.loan.application.service.model.infra.avro.Purchase.class)
        .to("kafka:{{camelbeeservice.southbound-createorder-topic}}"
            + "?valueSerializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer"
            + "&keySerializer=org.apache.kafka.common.serialization.StringSerializer");

    from("direct:updateOrderKafka").routeId("updateOrderKafkaRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .convertBodyTo(io.fintech.loan.application.service.model.infra.avro.Purchase.class)
        .to("kafka:{{camelbeeservice.southbound-updateorder-topic}}"
            + "?valueSerializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer"
            + "&keySerializer=org.apache.kafka.common.serialization.StringSerializer");

  }
}
