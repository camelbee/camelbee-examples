package io.fintech.loan.application.service.routes.consumer.kafka;

import io.fintech.loan.application.service.exception.GenericExceptionHandler;
import io.fintech.loan.application.service.mapper.api.AvroOrderMapper;
import io.fintech.loan.application.service.utils.ExchangeHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

/**
 * Kafka Listener Route.
 *
 * @author camelbee
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AvroKafkaConsumerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final GenericExceptionHandler genericExceptionHandler;
  final AvroOrderMapper avroOrderMapper;

  /**
   * Configure.
   *
   * @throws Exception the exception
   */
  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);

    errorHandler(genericExceptionHandler.appErrorHandler());

    // CPD-OFF

    from(
        "kafka:{{camelbeeservice.northbound-updateorder-topic}}-avro"
            + "?groupId=camelbee"
            + "&autoOffsetReset=earliest"
            + "&valueDeserializer=io.apicurio.registry.serde.avro.AvroKafkaDeserializer"
            + "&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer"
            + "&additionalProperties.apicurio.registry.use-specific-avro-reader=true")
        .routeId("avroKafkaUpdateOrderConsumerRoute")
        .process(ExchangeHelper::normalizeTransactionIdHeader)
        .process(e -> {
          var order = e.getIn().getBody(io.fintech.loan.application.service.model.api.avro.Order.class);

          var headers = e.getIn().getHeaders();
          headers.put("id", order.getId().toString());
          headers.put("salesChannel", order.getSalesChannel().toString());

        })
        .convertBodyTo(io.fintech.loan.application.service.model.domain.Order.class)
        .to("direct:centralUpdateOrder");

  }
}
