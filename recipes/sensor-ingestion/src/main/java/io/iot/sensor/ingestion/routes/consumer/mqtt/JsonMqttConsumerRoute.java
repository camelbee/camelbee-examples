package io.iot.sensor.ingestion.routes.consumer.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iot.sensor.ingestion.exception.GenericExceptionHandler;
import io.iot.sensor.ingestion.model.domain.SensorReading;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class JsonMqttConsumerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final GenericExceptionHandler genericExceptionHandler;
  final ObjectMapper objectMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);

    errorHandler(genericExceptionHandler.appErrorHandler());

    from("paho-mqtt5:sensors/+/readings"
        + "?brokerUrl={{camelbeeservice.mqtt.broker-url}}&clientId=sensor-ingestion-consumer")
        .routeId("mqttSensorIngestConsumerRoute")
        .convertBodyTo(String.class)
        .process(exchange -> {
          String json = exchange.getIn().getBody(String.class);
          SensorReading reading = objectMapper.readValue(json, SensorReading.class);
          reading.setReadingId(UUID.randomUUID().toString());
          reading.setReceivedAt(Instant.now());
          exchange.getIn().setBody(reading);
        })
        .to("direct:centralIngestReading");

  }
}
