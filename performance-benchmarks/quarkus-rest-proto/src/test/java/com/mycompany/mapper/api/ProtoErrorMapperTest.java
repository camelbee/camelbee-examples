package com.mycompany.mapper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mycompany.model.domain.Error;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProtoErrorMapperTest {

  private final ProtoErrorMapper mapper = Mappers.getMapper(ProtoErrorMapper.class);

  @Test
  void testProtoToDomainError() {
    // Given
    com.mycompany.model.api.proto.Error.Builder protoErrorBuilder = com.mycompany.model.api.proto.Error.newBuilder();
    protoErrorBuilder.setCode("ERR001");
    protoErrorBuilder.setMessage("Test error message");
    com.mycompany.model.api.proto.Error protoError = protoErrorBuilder.build();

    // When
    Error domainError = mapper.protoToDomainError(protoError);

    // Then
    assertEquals("ERR001", domainError.getCode());
    assertEquals("Test error message", domainError.getMessage());
  }

  @Test
  void testProtoToDomainErrorWithEmptyValues() {
    // Given
    com.mycompany.model.api.proto.Error protoError = com.mycompany.model.api.proto.Error.newBuilder().build();

    // When
    Error domainError = mapper.protoToDomainError(protoError);

    // Then
    assertEquals("", domainError.getCode());
    assertEquals("", domainError.getMessage());
  }

  @Test
  void testDomainToProtoError() {
    // Given
    Error domainError = new Error();
    domainError.setCode("ERR001");
    domainError.setMessage("Test error message");

    // When
    com.mycompany.model.api.proto.Error protoError = mapper.domainToProtoError(domainError);

    // Then
    assertEquals("ERR001", protoError.getCode());
    assertEquals("Test error message", protoError.getMessage());
  }

  @Test
  void testDomainToProtoErrorWithNullValues() {
    // Given
    Error domainError = new Error();

    // When
    com.mycompany.model.api.proto.Error protoError = mapper.domainToProtoError(domainError);

    // Then
    // Proto buffers defaults to empty string for string fields
    assertEquals("", protoError.getCode());
    assertEquals("", protoError.getMessage());
  }
}
