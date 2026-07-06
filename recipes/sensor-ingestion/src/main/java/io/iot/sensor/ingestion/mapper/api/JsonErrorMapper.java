package io.iot.sensor.ingestion.mapper.api;

import io.iot.sensor.ingestion.config.SharedMapperConfig;
import io.iot.sensor.ingestion.model.domain.Error;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Api Json Error/ErrorItem To Domain Error/ErrorItem to and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JsonErrorMapper {

  // CPD-OFF
  // Json Error to Domain Error
  @Mapping(source = "code", target = "code")
  @Mapping(source = "message", target = "message")
  Error jsonToDomainError(io.iot.sensor.ingestion.model.api.json.Error error);

  // Domain Error to Json Error
  @Mapping(source = "code", target = "code")
  @Mapping(source = "message", target = "message")
  io.iot.sensor.ingestion.model.api.json.Error domainToJsonError(Error error);

}
