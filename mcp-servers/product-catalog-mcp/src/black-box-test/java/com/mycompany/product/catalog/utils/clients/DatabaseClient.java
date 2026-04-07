package com.mycompany.product.catalog.utils.clients;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client helper for inserting records into databases to trigger consumer routes in black-box tests.
 * Inserts into the outbound events tables that the consumer routes poll.
 */
public class DatabaseClient {

  private static final Logger log = LoggerFactory.getLogger(DatabaseClient.class);

  public String readResource(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null)
        throw new RuntimeException("Resource not found: " + path);
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public byte[] readResourceBinary(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null)
        throw new RuntimeException("Resource not found: " + path);
      return is.readAllBytes();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
