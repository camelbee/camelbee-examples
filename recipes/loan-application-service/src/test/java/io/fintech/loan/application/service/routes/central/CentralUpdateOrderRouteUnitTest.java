package io.fintech.loan.application.service.routes.central;

import static org.assertj.core.api.Assertions.assertThat;

import io.fintech.loan.application.service.model.domain.ApplicationStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.model.infra.json.CreditAssessmentResult;
import io.fintech.loan.application.service.routes.UnitTest;
import io.fintech.loan.application.service.utils.TestDataFactory;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CentralUpdateLoanApplicationRoute — three-path content-based routing")
class CentralUpdateOrderRouteUnitTest extends UnitTest {

  @EndpointInject("mock:bureau")
  protected MockEndpoint mockBureau;
  @EndpointInject("mock:updateJpa")
  protected MockEndpoint mockJpa;
  @EndpointInject("mock:updateCache")
  protected MockEndpoint mockCache;
  @EndpointInject("mock:updateKafka")
  protected MockEndpoint mockKafka;

  @BeforeEach
  void setUp() throws Exception {
    AdviceWith.adviceWith(camelContext, "centralUpdateLoanApplicationRoute", a -> {
      a.weaveById("creditBureauAssessmentEndpoint").replace().process(e -> {
        CreditAssessmentResult r = new CreditAssessmentResult();
        r.setAssessmentId("MOCK-1");
        r.setApproved(true);
        r.setRiskScore(33);
        r.setReason("Credit profile acceptable");
        r.setRecommendedMaxAmount(java.math.BigDecimal.valueOf(50000));
        e.setProperty("creditAssessmentResult", r);
      }).to("mock:bureau");
      a.weaveById("updateOrderJpaEndpoint").replace().to("mock:updateJpa");
      a.weaveById("updateOrderCacheEndpoint").replace().to("mock:updateCache");
      a.weaveById("updateOrderKafkaEndpoint").replace().to("mock:updateKafka");
    });
    camelContext.start();
  }

  @Test
  @DisplayName("Path 1 (Auto-approve): low amount + high credit → no bureau call, APPROVED with risk=10")
  void test_AutoApprove() throws Exception {
    mockBureau.expectedMessageCount(0);
    mockJpa.expectedMessageCount(1);
    mockCache.expectedMessageCount(1);
    mockKafka.expectedMessageCount(1);

    var result = fluentProducerTemplate.to("direct:centralUpdateOrder")
        .withBody(TestDataFactory.autoApproveInput()).send();

    MockEndpoint.assertIsSatisfied(camelContext);
    LoanApplication response = result.getMessage().getBody(LoanApplication.class);
    assertThat(response.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
    assertThat(response.getRiskScore()).isEqualTo(10);
    assertThat(response.getDecisionReason()).isEqualTo("Auto-approved: low-risk application");
    assertThat(response.getProcessedAt()).isNotNull();
  }

  @Test
  @DisplayName("Path 2 (Auto-reject): low credit → no bureau call, REJECTED with risk=95")
  void test_AutoReject() throws Exception {
    mockBureau.expectedMessageCount(0);
    mockJpa.expectedMessageCount(1);

    var result = fluentProducerTemplate.to("direct:centralUpdateOrder")
        .withBody(TestDataFactory.autoRejectInput()).send();

    MockEndpoint.assertIsSatisfied(camelContext);
    LoanApplication response = result.getMessage().getBody(LoanApplication.class);
    assertThat(response.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    assertThat(response.getRiskScore()).isEqualTo(95);
    assertThat(response.getDecisionReason()).isEqualTo("Auto-rejected: credit score below minimum threshold");
  }

  @Test
  @DisplayName("Path 3 (Credit Bureau approved): bureau called → APPROVED with bureau riskScore")
  void test_BureauApprovalPath() throws Exception {
    mockBureau.expectedMessageCount(1);
    mockJpa.expectedMessageCount(1);

    var result = fluentProducerTemplate.to("direct:centralUpdateOrder")
        .withBody(TestDataFactory.bureauApprovalInput()).send();

    MockEndpoint.assertIsSatisfied(camelContext);
    LoanApplication response = result.getMessage().getBody(LoanApplication.class);
    assertThat(response.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
    assertThat(response.getRiskScore()).isEqualTo(33);
    assertThat(response.getDecisionReason()).isEqualTo("Credit profile acceptable");
  }

  // Note: the credit-bureau "review" path (approved=false → PENDING_REVIEW)
  // and the bureau-503 error path are covered by integration tests, which
  // exercise the real WireMock stubs with creditScore < 650 and the
  // X-Force-Bureau-Failure trigger respectively.
}
