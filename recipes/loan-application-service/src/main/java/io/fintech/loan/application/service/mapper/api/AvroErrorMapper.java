package io.fintech.loan.application.service.mapper.api;

import io.fintech.loan.application.service.config.SharedMapperConfig;
import io.fintech.loan.application.service.model.domain.Error;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Api Avro Error/ErrorItem To Domain Error/ErrorItem to and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AvroErrorMapper {

  // CPD-OFF
  // Avro Error to Domain Error
  @Mapping(source = "code", target = "code")
  @Mapping(source = "message", target = "message")
  Error avroToDomainError(io.fintech.loan.application.service.model.api.avro.Error error);

  // Domain Error to Avro Error
  @Mapping(source = "code", target = "code")
  @Mapping(source = "message", target = "message")
  io.fintech.loan.application.service.model.api.avro.Error domainToAvroError(Error error);

  default String map(CharSequence value) {
    return value != null ? value.toString() : null;
  }

  default CharSequence map(String value) {
    return value != null ? value : null;
  }

}
