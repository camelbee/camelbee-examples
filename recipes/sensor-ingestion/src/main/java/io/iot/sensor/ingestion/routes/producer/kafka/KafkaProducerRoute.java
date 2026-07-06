package io.iot.sensor.ingestion.routes.producer.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iot.sensor.ingestion.constants.Constants;
import io.iot.sensor.ingestion.model.domain.SensorReading;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final ObjectMapper objectMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:ingestReadingKafka").routeId("ingestReadingKafkaRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .process(e -> {
          SensorReading reading = e.getIn().getBody(SensorReading.class);
          e.getIn().setHeader("kafka.KEY", reading.getDeviceId());
          String json = objectMapper.writeValueAsString(reading);
          e.getIn().setBody(json);
        })
        .to("kafka:{{camelbeeservice.southbound-ingest-topic}}");
  }
}
