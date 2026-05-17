package io.fintech.loan.application.service.routes.producer.kafka;

import io.fintech.loan.application.service.constants.Constants;
import io.fintech.loan.application.service.mapper.api.AvroLoanApplicationEventMapper;
import io.fintech.loan.application.service.model.api.avro.LoanApplicationProcessedEvent;
import io.fintech.loan.application.service.model.api.avro.LoanApplicationSubmittedEvent;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final AvroLoanApplicationEventMapper avroLoanApplicationEventMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:createOrderKafka").routeId("publishLoanApplicationSubmittedRoute")
        .process(e -> {
          LoanApplication app = e.getProperty(Constants.ORIGINAL_BODY, LoanApplication.class);
          LoanApplicationSubmittedEvent event = avroLoanApplicationEventMapper.domainToSubmittedEvent(app);
          e.getIn().setBody(event);
          e.getIn().setHeader("kafka.KEY", app.getApplicationId());
        })
        .to("kafka:{{camelbeeservice.southbound-submitted-topic}}"
            + "?valueSerializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer"
            + "&keySerializer=org.apache.kafka.common.serialization.StringSerializer");

    from("direct:updateOrderKafka").routeId("publishLoanApplicationProcessedRoute")
        .process(e -> {
          LoanApplication app = e.getProperty(Constants.ORIGINAL_BODY, LoanApplication.class);
          LoanApplicationProcessedEvent event = avroLoanApplicationEventMapper.domainToProcessedEvent(app);
          e.getIn().setBody(event);
          e.getIn().setHeader("kafka.KEY", app.getApplicationId());
        })
        .to("kafka:{{camelbeeservice.southbound-processed-topic}}"
            + "?valueSerializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer"
            + "&keySerializer=org.apache.kafka.common.serialization.StringSerializer");
  }
}
