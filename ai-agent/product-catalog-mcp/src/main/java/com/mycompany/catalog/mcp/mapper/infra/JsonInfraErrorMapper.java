package com.mycompany.catalog.mcp.mapper.infra;

import com.mycompany.catalog.mcp.config.SharedMapperConfig;
import com.mycompany.catalog.mcp.model.domain.Error;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Domain Error/ErrorItem to Infra Json Error/ErrorItem and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JsonInfraErrorMapper {

  // Json Error to Domain Error
  @Mapping(source = "code", target = "code")
  @Mapping(source = "message", target = "message")
  Error jsonErrorToDomainError(com.mycompany.catalog.mcp.model.infra.json.Error error);

  // Domain Error to Json Error
  @Mapping(source = "code", target = "code")
  @Mapping(source = "message", target = "message")
  com.mycompany.catalog.mcp.model.infra.json.Error domainErrorToJsonError(Error error);

}
