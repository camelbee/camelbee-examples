package io.iot.sensor.ingestion.mapper.api;

import io.iot.sensor.ingestion.config.SharedMapperConfig;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting between domain SensorReading/SensorReadingPage and
 * the OpenAPI-generated API JSON models.
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JsonSensorReadingMapper {

  io.iot.sensor.ingestion.model.api.json.SensorReading domainToApiSensorReading(io.iot.sensor.ingestion.model.domain.SensorReading reading);

  io.iot.sensor.ingestion.model.domain.SensorReading apiToDomainSensorReading(io.iot.sensor.ingestion.model.api.json.SensorReading reading);

  io.iot.sensor.ingestion.model.api.json.SensorReadingPage domainToApiSensorReadingPage(io.iot.sensor.ingestion.model.domain.SensorReadingPage page);

  io.iot.sensor.ingestion.model.domain.SensorReadingPage apiToDomainSensorReadingPage(io.iot.sensor.ingestion.model.api.json.SensorReadingPage page);

  default OffsetDateTime instantToOffsetDateTime(Instant instant) {
    return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
  }

  default Instant offsetDateTimeToInstant(OffsetDateTime offsetDateTime) {
    return offsetDateTime != null ? offsetDateTime.toInstant() : null;
  }
}
