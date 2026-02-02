package com.mycompany.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import java.time.format.DateTimeFormatter;

/**
 * JsonSerDe.
 *
 * @param <T> Class
 */
public class JsonSerDe<T> {

  private final TypeReference<T> typeReference;
  private final ObjectMapper objectMapper;

  /**
   * Constructs a utility for JSON serialization and deserialization.
   *
   * @param typeReference The target type for deserialization.
   */
  public JsonSerDe(TypeReference<T> typeReference) {
    if (typeReference == null) {
      throw new IllegalArgumentException("Type reference cannot be null");
    }
    this.typeReference = typeReference;
    this.objectMapper = new ObjectMapper();

    // Register JavaTimeModule with custom serializers
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(
        java.time.LocalDate.class,
        new LocalDateSerializer(DateTimeFormatter.ISO_DATE) // "YYYY-MM-DD"
    );
    javaTimeModule.addSerializer(
        java.time.OffsetDateTime.class,
        OffsetDateTimeSerializer.INSTANCE // ISO-8601 with timezone offset
    );

    this.objectMapper.registerModule(javaTimeModule);

    // Write dates as ISO-8601 strings
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Enable pretty printing
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    // Don't render null values
    this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /**
   * Serializes an object to a JSON string.
   *
   * @param object The object to serialize.
   * @return A JSON string.
   * @throws Exception if serialization fails.
   */
  public String serialize(T object) throws Exception {
    if (object == null) {
      throw new IllegalArgumentException("Object to serialize cannot be null");
    }
    return objectMapper.writeValueAsString(object);
  }

  /**
   * Deserializes a JSON string into an object.
   *
   * @param jsonData The JSON string to deserialize.
   * @return The deserialized object.
   * @throws Exception if deserialization fails.
   */
  public T deserialize(String jsonData) throws Exception {
    if (jsonData == null || jsonData.isEmpty()) {
      throw new IllegalArgumentException("JSON data cannot be null or empty");
    }
    return objectMapper.readValue(jsonData, typeReference);
  }

  /**
   * Gets the configured ObjectMapper instance.
   *
   * @return The ObjectMapper used for JSON operations.
   */
  public ObjectMapper getObjectMapper() {
    return this.objectMapper;
  }
}