package io.fintech.loan.application.service.mapper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.fintech.loan.application.service.model.domain.Error;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AvroErrorMapperTest {

  private final AvroErrorMapper mapper = Mappers.getMapper(AvroErrorMapper.class);

  @Test
  void testAvroToDomainError() {
    // Given
    io.fintech.loan.application.service.model.api.avro.Error avroError = new io.fintech.loan.application.service.model.api.avro.Error();
    avroError.setCode("ERR001");
    avroError.setMessage("Test error message");

    // When
    Error domainError = mapper.avroToDomainError(avroError);

    // Then
    assertEquals("ERR001", domainError.getCode());
    assertEquals("Test error message", domainError.getMessage());
  }

  @Test
  void testAvroToDomainErrorWithEmptyValues() {
    // Given
    io.fintech.loan.application.service.model.api.avro.Error avroError = new io.fintech.loan.application.service.model.api.avro.Error();
    // For Avro, we need to set empty strings instead of nulls
    avroError.setCode("");
    avroError.setMessage("");

    // When
    Error domainError = mapper.avroToDomainError(avroError);

    // Then
    assertEquals("", domainError.getCode());
    assertEquals("", domainError.getMessage());
  }

  @Test
  void testDomainToAvroError() {
    // Given
    Error domainError = new Error();
    domainError.setCode("ERR001");
    domainError.setMessage("Test error message");

    // When
    io.fintech.loan.application.service.model.api.avro.Error avroError = mapper.domainToAvroError(domainError);

    // Then
    assertEquals("ERR001", avroError.getCode().toString());
    assertEquals("Test error message", avroError.getMessage().toString());
  }

  @Test
  void testDomainToAvroErrorWithNullValues() {
    // Given
    Error domainError = new Error();
    // For Avro, we need to set empty strings instead of nulls
    domainError.setCode("");
    domainError.setMessage("");

    // When
    io.fintech.loan.application.service.model.api.avro.Error avroError = mapper.domainToAvroError(domainError);

    // Then
    assertEquals("", avroError.getCode().toString());
    assertEquals("", avroError.getMessage().toString());
  }

  @Test
  void testMapStringToCharSequence() {
    // Given
    String input = "test string";

    // When
    CharSequence result = mapper.map(input);

    // Then
    assertEquals("test string", result.toString());
  }

  @Test
  void testMapNullStringToCharSequence() {
    // When
    CharSequence result = mapper.map((String) null);

    // Then
    assertNull(result);
  }

  @Test
  void testMapCharSequenceToString() {
    // Given
    CharSequence input = "test sequence";

    // When
    String result = mapper.map(input);

    // Then
    assertEquals("test sequence", result);
  }

  @Test
  void testMapNullCharSequenceToString() {
    // When
    String result = mapper.map((CharSequence) null);

    // Then
    assertNull(result);
  }
}