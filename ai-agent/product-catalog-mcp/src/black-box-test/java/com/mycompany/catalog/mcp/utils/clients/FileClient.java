package com.mycompany.catalog.mcp.utils.clients;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client helper for writing files to trigger file-based consumer routes in black-box tests.
 */
public class FileClient {

  private static final Logger log = LoggerFactory.getLogger(FileClient.class);



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
