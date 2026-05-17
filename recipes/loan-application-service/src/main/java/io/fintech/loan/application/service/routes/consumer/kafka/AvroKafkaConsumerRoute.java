package io.fintech.loan.application.service.routes.consumer.kafka;

import io.fintech.loan.application.service.exception.GenericExceptionHandler;
import io.fintech.loan.application.service.mapper.api.AvroLoanApplicationEventMapper;
import io.fintech.loan.application.service.model.api.avro.LoanApplicationSubmittedEvent;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

/**
 * Listens on loan-applications.submitted and triggers processing (UPO).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AvroKafkaConsumerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final GenericExceptionHandler genericExceptionHandler;
  final AvroLoanApplicationEventMapper avroEventMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(genericExceptionHandler.appErrorHandler());

    from(
        "kafka:{{camelbeeservice.northbound-submitted-topic}}"
            + "?groupId=camelbee"
            + "&autoOffsetReset=earliest"
            + "&valueDeserializer=io.apicurio.registry.serde.avro.AvroKafkaDeserializer"
            + "&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer"
            + "&additionalProperties.apicurio.registry.use-specific-avro-reader=true")
        .routeId("avroKafkaSubmittedConsumerRoute")
        .process(e -> {
          var event = e.getIn().getBody(LoanApplicationSubmittedEvent.class);
          LoanApplication domain = avroEventMapper.submittedEventToDomain(event);
          e.getIn().setBody(domain);
          if (domain != null && domain.getApplicationId() != null) {
            e.getIn().setHeader("applicationId", domain.getApplicationId());
          }
        })
        .to("direct:centralUpdateOrder");
  }
}
