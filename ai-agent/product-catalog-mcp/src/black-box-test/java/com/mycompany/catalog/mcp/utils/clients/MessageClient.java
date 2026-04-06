package com.mycompany.catalog.mcp.utils.clients;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client helper for sending messages to queues/topics in black-box tests.
 * Provides methods for each messaging technology using native client libraries.
 */
public class MessageClient {

  private static final Logger log = LoggerFactory.getLogger(MessageClient.class);







  public String readResource(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null) throw new RuntimeException("Resource not found: " + path);
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) { throw new RuntimeException(e); }
  }

  public byte[] readResourceBinary(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null) throw new RuntimeException("Resource not found: " + path);
      return is.readAllBytes();
    } catch (IOException e) { throw new RuntimeException(e); }
  }
}
