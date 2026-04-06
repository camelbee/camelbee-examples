package com.mycompany.catalog.mcp.config;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import jakarta.inject.Singleton;

/**
 * Quarkus Camel ObjectMapperCustomizer.
 */
@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {

  /**
   * Customize the mapper.
   *
   * @param mapper the mapper
   */
  public void customize(ObjectMapper mapper) {
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new ParameterNamesModule());
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    mapper.setDateFormat(dateFormat);
  }
}