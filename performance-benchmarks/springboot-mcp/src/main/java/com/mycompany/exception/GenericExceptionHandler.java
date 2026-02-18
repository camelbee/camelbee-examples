package com.mycompany.exception;

import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Global Error Handler.
 *
 * @author camelbee
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.TooManyStaticImports")
public class GenericExceptionHandler extends RouteBuilder {

  final GlobalErrorProcessor globalErrorProcessor;

  /**
   * The creates a new deadletter channel builder.
   */

  @Bean
  public DeadLetterChannelBuilder appErrorHandler() {
    var deadLetterChannelBuilder = new DeadLetterChannelBuilder();
    deadLetterChannelBuilder.setDeadLetterUri("direct:error");
    deadLetterChannelBuilder.logHandled(false);
    deadLetterChannelBuilder.useOriginalMessage();
    return deadLetterChannelBuilder;
  }

  /**
   * Configure global error route.
   *
   * @throws Exception can be thrown during configuration
   */
  @Override
  public void configure() throws Exception {

    from("direct:error").routeId("errorHandlerRoute")
        .process(globalErrorProcessor)
        .choice()
        .when(this::needsErrorMarshalMcp)
        .marshal().json().endChoice().end();

  }

  /**
   * Determines whether the response needs JSON formatting
   * based on the originating endpoint.
   *
   * @param exchange The Camel exchange
   * @return true if JSON formatting is needed, false otherwise
   */
  private boolean needsErrorMarshalMcp(Exchange exchange) {
    String fromEndpoint = exchange.getFromEndpoint().toString();
    // Add your specific endpoint conditions here
    return fromEndpoint.contains("camelbee-mcp/rpc");
  }

}
