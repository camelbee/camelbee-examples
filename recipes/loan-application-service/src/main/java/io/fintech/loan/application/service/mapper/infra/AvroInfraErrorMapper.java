package io.fintech.loan.application.service.mapper.infra;

import io.fintech.loan.application.service.config.SharedMapperConfig;
import io.fintech.loan.application.service.model.domain.Error;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Domain Error to Infra Avro Error and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AvroInfraErrorMapper {

  // Avro Error to Domain Error
  @Mapping(source = "code", target = "code")
  @Mapping(source = "message", target = "message")
  Error avroErrorToDomainError(io.fintech.loan.application.service.model.infra.avro.Error error);

  // Domain Error to Avro Purchase
  @Mapping(source = "code", target = "code")
  @Mapping(source = "message", target = "message")
  io.fintech.loan.application.service.model.infra.avro.Error domainErrorToAvroError(Error error);

  default String map(CharSequence value) {
    return value != null ? value.toString() : null;
  }

  default CharSequence map(String value) {
    return value != null ? value : null;
  }

}
