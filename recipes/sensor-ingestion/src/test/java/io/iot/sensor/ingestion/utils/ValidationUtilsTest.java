package io.iot.sensor.ingestion.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

class ValidationUtilsTest {

  @Test
  void given_ValidNumericHeaderInRange_When_ValidateNumericHeader_Then_Success() throws Exception {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.getIn().setHeader("pageSize", "50");
    ValidationUtils.validateNumericHeader(exchange, "pageSize", 1, 100);
  }

  @Test
  void given_NumericHeaderBelowMin_When_ValidateNumericHeader_Then_ThrowValidationException() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.getIn().setHeader("pageSize", "0");
    assertThrows(ValidationException.class,
        () -> ValidationUtils.validateNumericHeader(exchange, "pageSize", 1, 100));
  }

  @Test
  void given_NumericHeaderAboveMax_When_ValidateNumericHeader_Then_ThrowValidationException() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.getIn().setHeader("pageSize", "200");
    assertThrows(ValidationException.class,
        () -> ValidationUtils.validateNumericHeader(exchange, "pageSize", 1, 100));
  }

  @Test
  void given_NonNumericHeader_When_ValidateNumericHeader_Then_ThrowValidationException() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.getIn().setHeader("pageSize", "abc");
    assertThrows(ValidationException.class,
        () -> ValidationUtils.validateNumericHeader(exchange, "pageSize", 1, 100));
  }

}
