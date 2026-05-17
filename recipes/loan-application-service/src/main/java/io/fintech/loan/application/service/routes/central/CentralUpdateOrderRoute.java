package io.fintech.loan.application.service.routes.central;

import io.fintech.loan.application.service.constants.Constants;
import io.fintech.loan.application.service.model.domain.ApplicationStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.model.infra.json.CreditAssessmentResult;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

/**
 * Processing flow with three-path content-based routing.
 *
 * <p>Path 1 (Auto-Approve): requestedAmount <= 5000 AND creditScore >= 700
 * Path 2 (Auto-Reject): creditScore < 500
 * Path 3 (Credit Bureau): everything else — calls the credit-bureau REST backend.
 *
 * <p>After the decision is applied to the body, the central route fans out:
 * JPA update -> Cache overwrite -> Kafka publish LoanApplicationProcessedEvent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CentralUpdateOrderRoute extends RouteBuilder {

  private static final BigDecimal AUTO_APPROVE_AMOUNT_CEILING = new BigDecimal("5000");
  private static final int AUTO_APPROVE_CREDIT_FLOOR = 700;
  private static final int AUTO_REJECT_CREDIT_CEILING_EXCLUSIVE = 500;

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralUpdateOrder").routeId("centralUpdateLoanApplicationRoute")
        .process(exchange -> {
          LoanApplication app = exchange.getIn().getBody(LoanApplication.class);
          if (app == null || app.getApplicationId() == null) {
            throw new ValidationException(exchange, "applicationId is required");
          }
        })
        .setProperty(Constants.ORIGINAL_BODY, body())
        .choice()
        .when().method(this, "isAutoApprove")
        .process(this::applyAutoApprove)
        .id("autoApprovePath")
        .when().method(this, "isAutoReject")
        .process(this::applyAutoReject)
        .id("autoRejectPath")
        .otherwise()
        .to("direct:updateOrderRest").id("creditBureauAssessmentEndpoint")
        .process(this::applyCreditBureauResult)
        .id("creditBureauPath")
        .end()
        .setProperty(Constants.ORIGINAL_BODY, body())
        .to("direct:updateOrderJpa").id("updateOrderJpaEndpoint")
        .to("direct:updateOrderCache").id("updateOrderCacheEndpoint")
        .to("direct:updateOrderKafka").id("updateOrderKafkaEndpoint")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY));
  }

  public boolean isAutoApprove(Exchange exchange) {
    LoanApplication app = exchange.getIn().getBody(LoanApplication.class);
    return app != null
        && app.getRequestedAmount() != null
        && app.getRequestedAmount().compareTo(AUTO_APPROVE_AMOUNT_CEILING) <= 0
        && app.getCreditScore() != null
        && app.getCreditScore() >= AUTO_APPROVE_CREDIT_FLOOR;
  }

  public boolean isAutoReject(Exchange exchange) {
    LoanApplication app = exchange.getIn().getBody(LoanApplication.class);
    return app != null
        && app.getCreditScore() != null
        && app.getCreditScore() < AUTO_REJECT_CREDIT_CEILING_EXCLUSIVE;
  }

  private void applyAutoApprove(Exchange exchange) {
    LoanApplication app = exchange.getIn().getBody(LoanApplication.class);
    app.setStatus(ApplicationStatus.APPROVED);
    app.setRiskScore(10);
    app.setDecisionReason("Auto-approved: low-risk application");
    app.setProcessedAt(Instant.now());
    exchange.getIn().setBody(app);
  }

  private void applyAutoReject(Exchange exchange) {
    LoanApplication app = exchange.getIn().getBody(LoanApplication.class);
    app.setStatus(ApplicationStatus.REJECTED);
    app.setRiskScore(95);
    app.setDecisionReason("Auto-rejected: credit score below minimum threshold");
    app.setProcessedAt(Instant.now());
    exchange.getIn().setBody(app);
  }

  private void applyCreditBureauResult(Exchange exchange) {
    LoanApplication app = exchange.getProperty(Constants.ORIGINAL_BODY, LoanApplication.class);
    CreditAssessmentResult result = exchange.getProperty("creditAssessmentResult", CreditAssessmentResult.class);
    if (result == null) {
      throw new IllegalStateException("Credit bureau result missing after backend call");
    }
    if (Boolean.TRUE.equals(result.getApproved())) {
      app.setStatus(ApplicationStatus.APPROVED);
      app.setDecisionReason(result.getReason());
    } else {
      app.setStatus(ApplicationStatus.PENDING_REVIEW);
      app.setDecisionReason(result.getReason() + " — queued for manual review");
    }
    app.setRiskScore(result.getRiskScore());
    app.setProcessedAt(Instant.now());
    exchange.getIn().setBody(app);
  }
}
