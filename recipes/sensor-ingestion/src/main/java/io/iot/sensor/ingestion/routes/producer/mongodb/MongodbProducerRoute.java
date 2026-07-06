package io.iot.sensor.ingestion.routes.producer.mongodb;

import io.iot.sensor.ingestion.constants.Constants;
import io.iot.sensor.ingestion.exception.DataNotFoundException;
import io.iot.sensor.ingestion.model.domain.SensorReading;
import io.iot.sensor.ingestion.model.domain.SensorReadingPage;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.camelbee.config.CamelBeeRouteConfigurer;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MongodbProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:ingestReadingMongoDb").routeId("ingestReadingMongoDbRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .process(e -> {
          SensorReading reading = e.getIn().getBody(SensorReading.class);
          Document doc = new Document()
              .append("_id", new ObjectId())
              .append("readingId", reading.getReadingId())
              .append("deviceId", reading.getDeviceId())
              .append("sensorType", reading.getSensorType() != null ? reading.getSensorType().name() : null)
              .append("value", reading.getValue())
              .append("unit", reading.getUnit())
              .append("quality", reading.getQuality() != null ? reading.getQuality().name() : null)
              .append("location", reading.getLocation())
              .append("recordedAt", reading.getRecordedAt() != null ? reading.getRecordedAt().toString() : null)
              .append("receivedAt", reading.getReceivedAt() != null ? reading.getReceivedAt().toString() : null);
          e.getIn().setBody(doc);
        })
        .to("mongodb:mongoBean?database={{camelbeeservice.mongodb.database}}&collection={{camelbeeservice.mongodb.sensorreadings-collection}}&operation=insert")
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

    from("direct:listReadingsMongoDb").routeId("listReadingsMongoDbRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .process(e -> {
          String deviceId = e.getIn().getHeader("deviceId", String.class);
          String from = e.getIn().getHeader("from", String.class);
          String to = e.getIn().getHeader("to", String.class);
          Integer page = e.getIn().getHeader("page", Integer.class);
          Integer pageSize = e.getIn().getHeader("pageSize", Integer.class);
          if (page == null || page < 0)
            page = 0;
          if (pageSize == null || pageSize <= 0)
            pageSize = 50;

          Document query = new Document("deviceId", deviceId);
          if (from != null && !from.isEmpty()) {
            query.append("recordedAt", new Document("$gte", from));
          }
          if (to != null && !to.isEmpty()) {
            Document recordedAt = (Document) query.get("recordedAt");
            if (recordedAt != null) {
              recordedAt.append("$lte", to);
            } else {
              query.append("recordedAt", new Document("$lte", to));
            }
          }

          e.getIn().setBody(query);
          e.getIn().setHeader("CamelMongoDbSortBy", new Document("recordedAt", -1));
          e.getIn().setHeader("CamelMongoDbSkip", page * pageSize);
          e.getIn().setHeader("CamelMongoDbLimit", pageSize);
          e.setProperty("queryDeviceId", deviceId);
          e.setProperty("queryFrom", from);
          e.setProperty("queryTo", to);
          e.setProperty("queryPage", page);
          e.setProperty("queryPageSize", pageSize);
        })
        .to("mongodb:mongoBean?database={{camelbeeservice.mongodb.database}}&collection={{camelbeeservice.mongodb.sensorreadings-collection}}&operation=findAll")
        .process(e -> {
          List<?> docs = e.getIn().getBody(List.class);
          List<SensorReading> readings = new ArrayList<>();
          if (docs != null) {
            for (Object obj : docs) {
              readings.add(documentToSensorReading((Document) obj));
            }
          }
          String deviceId = (String) e.getProperty("queryDeviceId");
          String from = (String) e.getProperty("queryFrom");
          String to = (String) e.getProperty("queryTo");
          Integer page = (Integer) e.getProperty("queryPage");
          Integer pageSize = (Integer) e.getProperty("queryPageSize");

          SensorReadingPage result = SensorReadingPage.builder()
              .readings(readings)
              .totalItems(readings.size())
              .page(page)
              .pageSize(pageSize)
              .deviceId(deviceId)
              .from(from)
              .to(to)
              .build();
          e.getIn().setBody(result);
        })
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

    from("direct:getReadingMongoDb").routeId("getReadingMongoDbRoute")
        .process(e -> {
          String readingId = e.getIn().getHeader("readingId", String.class);
          log.info("getReadingMongoDb: readingId header = '{}', all headers = {}", readingId, e.getIn().getHeaders());
          Document query = new Document("readingId", readingId);
          e.getIn().setBody(query);
        })
        .to("mongodb:mongoBean?database={{camelbeeservice.mongodb.database}}&collection={{camelbeeservice.mongodb.sensorreadings-collection}}&operation=findOneByQuery")
        .process(e -> {
          Document doc = e.getIn().getBody(Document.class);
          if (doc == null) {
            throw new DataNotFoundException("Sensor reading not found");
          }
          e.getIn().setBody(documentToSensorReading(doc));
        })
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());
  }

  private SensorReading documentToSensorReading(Document doc) {
    SensorReading reading = new SensorReading();
    reading.setReadingId(doc.getString("readingId"));
    reading.setDeviceId(doc.getString("deviceId"));
    String sensorTypeStr = doc.getString("sensorType");
    if (sensorTypeStr != null) {
      try {
        reading.setSensorType(io.iot.sensor.ingestion.model.domain.SensorType.valueOf(sensorTypeStr));
      } catch (IllegalArgumentException e) {
        log.warn("Unknown sensor type: {}", sensorTypeStr);
      }
    }
    reading.setValue(doc.getDouble("value"));
    reading.setUnit(doc.getString("unit"));
    String qualityStr = doc.getString("quality");
    if (qualityStr != null) {
      try {
        reading.setQuality(io.iot.sensor.ingestion.model.domain.ReadingQuality.valueOf(qualityStr));
      } catch (IllegalArgumentException e) {
        log.warn("Unknown reading quality: {}", qualityStr);
      }
    }
    reading.setLocation(doc.getString("location"));
    String recordedAtStr = doc.getString("recordedAt");
    if (recordedAtStr != null) {
      reading.setRecordedAt(Instant.parse(recordedAtStr));
    }
    String receivedAtStr = doc.getString("receivedAt");
    if (receivedAtStr != null) {
      reading.setReceivedAt(Instant.parse(receivedAtStr));
    }
    return reading;
  }
}
