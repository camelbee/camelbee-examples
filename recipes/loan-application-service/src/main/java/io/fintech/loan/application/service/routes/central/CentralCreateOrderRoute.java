package io.fintech.loan.application.service.routes.central;

import io.fintech.loan.application.service.constants.Constants;
import io.fintech.loan.application.service.model.domain.ApplicationStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

/**
 * Submit flow: generate applicationId, set status=RECEIVED, save (JPA + Cache),
 * publish LoanApplicationSubmittedEvent to Kafka. Returns the application with RECEIVED status.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CentralCreateOrderRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralCreateOrder").routeId("centralCreateLoanApplicationRoute")
        .process(exchange -> {
          LoanApplication app = exchange.getIn().getBody(LoanApplication.class);
          if (app == null) {
            throw new ValidationException(exchange, "Loan application body cannot be empty");
          }
          if (app.getApplicantId() == null || app.getApplicantId().isBlank()) {
            throw new ValidationException(exchange, "applicantId is required");
          }
          if (app.getRequestedAmount() == null) {
            throw new ValidationException(exchange, "requestedAmount is required");
          }
          if (app.getCreditScore() == null) {
            throw new ValidationException(exchange, "creditScore is required");
          }
          app.setApplicationId(UUID.randomUUID().toString());
          app.setStatus(ApplicationStatus.RECEIVED);
          app.setSubmittedAt(Instant.now());
        })
        .setProperty(Constants.ORIGINAL_BODY, body())
        .to("direct:createOrderJpa").id("createOrderJpaEndpoint")
        .to("direct:createOrderCache").id("createOrderCacheEndpoint")
        .to("direct:createOrderKafka").id("createOrderKafkaEndpoint")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY));
  }
}
