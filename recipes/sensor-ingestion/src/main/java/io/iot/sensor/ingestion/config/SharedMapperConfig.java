package io.iot.sensor.ingestion.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Shared mapStruct mapper configuration.
 */
@MapperConfig(componentModel = "cdi", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SharedMapperConfig {

}
