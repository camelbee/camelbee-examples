package com.mycompany.mapper.api;

import com.mycompany.config.SharedMapperConfig;
import com.mycompany.model.domain.Error;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Api Proto Error/ErrorItem To Domain Error/ErrorItem to and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ProtoErrorMapper {

  // CPD-OFF
  // Proto Error to Domain Error
  @Mapping(source = "code", target = "code")
  @Mapping(source = "message", target = "message")
  Error protoToDomainError(com.mycompany.model.api.proto.Error error);

  // Domain Error to Proto Error
  @Mapping(source = "code", target = "code")
  @Mapping(source = "message", target = "message")
  com.mycompany.model.api.proto.Error domainToProtoError(Error error);

}
