package com.mycompany.mapper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mycompany.model.domain.Error;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class JsonErrorMapperTest {

  private final JsonErrorMapper mapper = Mappers.getMapper(JsonErrorMapper.class);

  @Test
  void testJsonToDomainError() {
    // Given
    com.mycompany.model.api.json.Error jsonError = new com.mycompany.model.api.json.Error();
    jsonError.setCode("ERR001");
    jsonError.setMessage("Test error message");

    // When
    Error domainError = mapper.jsonToDomainError(jsonError);

    // Then
    assertEquals("ERR001", domainError.getCode());
    assertEquals("Test error message", domainError.getMessage());
  }

  @Test
  void testJsonToDomainErrorWithNullValues() {
    // Given
    com.mycompany.model.api.json.Error jsonError = new com.mycompany.model.api.json.Error();

    // When
    Error domainError = mapper.jsonToDomainError(jsonError);

    // Then
    assertNull(domainError.getCode());
    assertNull(domainError.getMessage());
  }

  @Test
  void testDomainToJsonError() {
    // Given
    Error domainError = new Error();
    domainError.setCode("ERR001");
    domainError.setMessage("Test error message");

    // When
    com.mycompany.model.api.json.Error jsonError = mapper.domainToJsonError(domainError);

    // Then
    assertEquals("ERR001", jsonError.getCode());
    assertEquals("Test error message", jsonError.getMessage());
  }

  @Test
  void testDomainToJsonErrorWithNullValues() {
    // Given
    Error domainError = new Error();

    // When
    com.mycompany.model.api.json.Error jsonError = mapper.domainToJsonError(domainError);

    // Then
    assertNull(jsonError.getCode());
    assertNull(jsonError.getMessage());
  }
}