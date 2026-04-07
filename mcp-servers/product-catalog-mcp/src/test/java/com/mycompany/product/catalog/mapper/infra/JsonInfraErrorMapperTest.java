package com.mycompany.product.catalog.mapper.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mycompany.product.catalog.model.domain.Error;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class JsonInfraErrorMapperTest {

  private final JsonInfraErrorMapper mapper = Mappers.getMapper(JsonInfraErrorMapper.class);

  @Test
  void jsonErrorToDomainError_shouldMapAllFields() {
    // Given
    String code = "E123";
    String message = "Test error message";

    com.mycompany.product.catalog.model.infra.json.Error jsonError = new com.mycompany.product.catalog.model.infra.json.Error();
    jsonError.setCode(code);
    jsonError.setMessage(message);

    // When
    Error domainError = mapper.jsonErrorToDomainError(jsonError);

    // Then
    assertEquals(code, domainError.getCode());
    assertEquals(message, domainError.getMessage());
  }

  @Test
  void jsonErrorToDomainError_withNullValues_shouldHandleNullValues() {
    // Given
    com.mycompany.product.catalog.model.infra.json.Error jsonError = new com.mycompany.product.catalog.model.infra.json.Error();
    jsonError.setCode(null);
    jsonError.setMessage(null);

    // When
    Error domainError = mapper.jsonErrorToDomainError(jsonError);

    // Then
    assertNull(domainError.getCode());
    assertNull(domainError.getMessage());
  }

  @Test
  void domainErrorToJsonError_shouldMapAllFields() {
    // Given
    String code = "E123";
    String message = "Test error message";

    Error domainError = new Error();
    domainError.setCode(code);
    domainError.setMessage(message);

    // When
    com.mycompany.product.catalog.model.infra.json.Error jsonError = mapper.domainErrorToJsonError(domainError);

    // Then
    assertEquals(code, jsonError.getCode());
    assertEquals(message, jsonError.getMessage());
  }

  @Test
  void domainErrorToJsonError_withNullValues_shouldHandleNullValues() {
    // Given
    Error domainError = new Error();
    domainError.setCode(null);
    domainError.setMessage(null);

    // When
    com.mycompany.product.catalog.model.infra.json.Error jsonError = mapper.domainErrorToJsonError(domainError);

    // Then
    assertNull(jsonError.getCode());
    assertNull(jsonError.getMessage());
  }
}
