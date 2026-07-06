package io.iot.sensor.ingestion.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

class ExchangeHelperTest {

  @Test
  void given_ValidFilename_When_ExtractTransactionId_Then_ReturnUUID() {
    String fileName = "createorder-success-request-779d2950-7f02-440a-a4a3-b36b82ec6c29.json";
    String result = ExchangeHelper.extractTransactionId(fileName);
    assertEquals("779d2950-7f02-440a-a4a3-b36b82ec6c29", result);
  }

  @Test
  void given_NullFilename_When_ExtractTransactionId_Then_ThrowIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> ExchangeHelper.extractTransactionId(null));
  }

  @Test
  void given_InvalidFilename_When_ExtractTransactionId_Then_ThrowIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> ExchangeHelper.extractTransactionId("invalid-filename.txt"));
  }

  @Test
  void given_FilenameWithoutUUID_When_ExtractTransactionId_Then_ThrowIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> ExchangeHelper.extractTransactionId("no-uuid-here.json"));
  }

  @Test
  void given_NullExchange_When_NormalizeTransactionIdHeader_Then_ThrowIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> ExchangeHelper.normalizeTransactionIdHeader(null));
  }

  @Test
  void given_ByteArrayTransactionIdHeader_When_NormalizeTransactionIdHeader_Then_ConvertToString() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.getIn().setHeader("transactionId", "test-id-123".getBytes());
    ExchangeHelper.normalizeTransactionIdHeader(exchange);
    assertEquals("test-id-123", exchange.getIn().getHeader("transactionId", String.class));
  }

  @Test
  void given_NoTransactionIdHeader_When_NormalizeTransactionIdHeader_Then_NoChange() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    ExchangeHelper.normalizeTransactionIdHeader(exchange);
    assertNull(exchange.getIn().getHeader("transactionId"));
  }

  @Test
  void given_StringTransactionIdHeader_When_NormalizeTransactionIdHeader_Then_NoChange() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.getIn().setHeader("transactionId", "already-string");
    ExchangeHelper.normalizeTransactionIdHeader(exchange);
    assertEquals("already-string", exchange.getIn().getHeader("transactionId", String.class));
  }
}
