package com.mycompany.exception.response;

import com.mycompany.exception.ErrorMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

/**
 * Error mapper for MCP/JSON-RPC endpoints.
 * JSON-RPC typically returns 200 OK with error details in the response.
 */
@Component
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
    RuntimeException toolCallException = new RuntimeException(meta.message());
    exchange.getMessage().setBody(toolCallException);
  }
}
