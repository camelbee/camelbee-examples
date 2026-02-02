package com.mycompany.exception;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.builder.RouteBuilder;

/**
 * Global Error Handler.
 *
 * @author camelbee
 */
@ApplicationScoped
@RequiredArgsConstructor
@SuppressWarnings("PMD.TooManyStaticImports")
public class GenericExceptionHandler extends RouteBuilder {

  final GlobalErrorProcessor globalErrorProcessor;

  /**
   * The creates a new deadletter channel builder.
   */

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
        .process(globalErrorProcessor);

  }

}
