package com.mycompany.catalog.mcp.exception.response;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import com.mycompany.catalog.mcp.exception.ErrorMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import io.quarkiverse.mcp.server.ToolCallException;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Error mapper for MCP/JSON-RPC endpoints.
 * JSON-RPC typically returns 200 OK with error details in the response.
 */
@ApplicationScoped
@Slf4j
public class McpErrorResponseFormatter implements ResponseFormatter {

  @Override
  public boolean supports(Exchange exchange) {
    String from = exchange.getFromEndpoint().toString();
    return from.contains("mcp");
  }

  @Override
  public void format(Exchange exchange, ErrorMeta meta) {
    log.debug("Handling MCP  error - Code: {}, Message: {}, HTTP Status: {}",
        meta.code(), meta.message(), meta.status());
    ToolCallException toolCallException = new ToolCallException(meta.message());
    exchange.getMessage().setBody(toolCallException);
  }
}
