package com.mycompany.catalog.mcp.utils.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCP client for black-box tests.
 * Connects to the MCP endpoint via HTTP and calls tools.
 */
public class McpTestClient {

  private static final Logger log = LoggerFactory.getLogger(McpTestClient.class);
  private static final String MCP_URL = "http://localhost:8080/mcp";
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private McpSyncClient client;

  /**
   * Initializes the MCP client connection.
   */
  public void connect() {
    try {
      var transportBuilder = HttpClientStreamableHttpTransport.builder(MCP_URL);
      client = io.modelcontextprotocol.client.McpClient.sync(transportBuilder.build()).build();
      client.initialize();
      log.info("MCP client connected to {}", MCP_URL);
    } catch (Exception e) {
      throw new RuntimeException("Failed to connect MCP client", e);
    }
  }

  /**
   * Calls an MCP tool with the given arguments.
   */
  public CallToolResult callTool(String toolName, Map<String, Object> args) {
    if (client == null) {
      connect();
    }
    try {
      return client.callTool(new CallToolRequest(toolName, args));
    } catch (Exception e) {
      throw new RuntimeException("Failed to call MCP tool: " + toolName, e);
    }
  }

  /**
   * Calls a create/replace/update/delete tool with order data from a resource file.
   * Uses SpringBoot positional argument style (arg0, arg1).
   */
  public CallToolResult callToolWithOrder(String toolName, String resourcePath, String transactionId) {
    String json = readResource(resourcePath);
    try {
      Object order = objectMapper.readValue(json, Object.class);
      Map<String, Object> args = new HashMap<>();
      args.put("arg0", order);
      args.put("arg1", transactionId);
      return callTool(toolName, args);
    } catch (Exception e) {
      throw new RuntimeException("Failed to prepare MCP tool call", e);
    }
  }

  /**
   * Calls a create/replace/update/delete tool with order data from a resource file.
   * Uses Quarkus named argument style (e.g. "order", "orders", "transactionId").
   */
  public CallToolResult callToolWithOrderNamed(String toolName, String orderParamName, String resourcePath, String transactionId) {
    String json = readResource(resourcePath);
    try {
      Object order = objectMapper.readValue(json, Object.class);
      Map<String, Object> args = new HashMap<>();
      args.put(orderParamName, order);
      args.put("transactionId", transactionId);
      return callTool(toolName, args);
    } catch (Exception e) {
      throw new RuntimeException("Failed to prepare MCP tool call", e);
    }
  }

  /**
   * Closes the MCP client.
   */
  public void close() {
    if (client != null) {
      try {
        client.close();
      } catch (Exception e) {
        log.warn("Failed to close MCP client", e);
      }
    }
  }


  public String readResource(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null) throw new RuntimeException("Resource not found: " + path);
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) { throw new RuntimeException(e); }
  }
}
