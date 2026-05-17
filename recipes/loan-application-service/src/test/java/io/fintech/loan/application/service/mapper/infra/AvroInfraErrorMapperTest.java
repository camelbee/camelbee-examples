package io.fintech.loan.application.service.mapper.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fintech.loan.application.service.model.domain.Error;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AvroInfraErrorMapperTest {

  private final AvroInfraErrorMapper mapper = Mappers.getMapper(AvroInfraErrorMapper.class);

  @Test
  void avroErrorToDomainError_shouldMapAllFields() {
    // Given
    String code = "E123";
    String message = "Test error message";

    io.fintech.loan.application.service.model.infra.avro.Error avroError = new io.fintech.loan.application.service.model.infra.avro.Error();
    avroError.setCode(code);
    avroError.setMessage(message);

    // When
    Error domainError = mapper.avroErrorToDomainError(avroError);

    // Then
    assertEquals(code, domainError.getCode());
    assertEquals(message, domainError.getMessage());
  }

  @Test
  void domainErrorToAvroError_shouldMapAllFields() {
    // Given
    String code = "E123";
    String message = "Test error message";

    Error domainError = new Error();
    domainError.setCode(code);
    domainError.setMessage(message);

    // When
    io.fintech.loan.application.service.model.infra.avro.Error avroError = mapper.domainErrorToAvroError(domainError);

    // Then
    assertEquals(code, avroError.getCode().toString());
    assertEquals(message, avroError.getMessage().toString());
  }

  @Test
  void mapCharSequenceToString_shouldConvertCharSequence() {
    // Given
    CharSequence charSequence = "Test String";

    // When
    String result = mapper.map(charSequence);

    // Then
    assertEquals("Test String", result);
  }

  @Test
  void mapStringToCharSequence_shouldConvertString() {
    // Given
    String string = "Test String";

    // When
    CharSequence result = mapper.map(string);

    // Then
    assertEquals("Test String", result.toString());
  }
}
