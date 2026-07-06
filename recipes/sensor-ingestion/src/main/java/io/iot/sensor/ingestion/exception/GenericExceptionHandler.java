package io.iot.sensor.ingestion.exception;

import static io.iot.sensor.ingestion.constants.Constants.APPLICATION_JSON;
import static io.iot.sensor.ingestion.constants.Constants.ORIGINAL_ACCEPT_CONTENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;

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

  final ObjectMapper mapper;

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

    JacksonDataFormat dataFormat = new JacksonDataFormat();
    dataFormat.setObjectMapper(mapper);

    from("direct:error").routeId("errorHandlerRoute")
        .process(globalErrorProcessor)
        .choice()
        .when(this::needsErrorMarshalRest)
        .to("direct:marshalErrorRest")
        .end();

    from("direct:marshalErrorRest")
        // marshal response regarding the Content-Type
        .choice()
        .when(header(ORIGINAL_ACCEPT_CONTENT_TYPE).contains(APPLICATION_JSON))
        .convertBodyTo(io.iot.sensor.ingestion.model.api.json.Error.class)
        .marshal(dataFormat)
        .endChoice();

  }

  /**
   * Determines whether the response needs JSON formatting
   * based on the originating endpoint.
   *
   * @param exchange The Camel exchange
   * @return true if JSON formatting is needed, false otherwise
   */
  private boolean needsErrorMarshalRest(Exchange exchange) {
    String fromEndpoint = exchange.getFromEndpoint().toString();
    // Add your specific endpoint conditions here
    return fromEndpoint.startsWith("platform-http:///camelbee-service");
  }

}
