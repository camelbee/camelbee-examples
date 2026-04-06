package com.mycompany.catalog.mcp.utils.clients;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
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
}
