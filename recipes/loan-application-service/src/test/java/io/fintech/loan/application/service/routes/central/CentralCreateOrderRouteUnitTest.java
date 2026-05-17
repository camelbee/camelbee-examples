package io.fintech.loan.application.service.routes.central;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.fintech.loan.application.service.model.domain.ApplicationStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.routes.UnitTest;
import io.fintech.loan.application.service.utils.TestDataFactory;
import org.apache.camel.EndpointInject;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CentralCreateLoanApplicationRoute")
class CentralCreateOrderRouteUnitTest extends UnitTest {

  @EndpointInject("mock:createOrderJpa")
  protected MockEndpoint mockJpa;

  @EndpointInject("mock:createOrderCache")
  protected MockEndpoint mockCache;

  @EndpointInject("mock:createOrderKafka")
  protected MockEndpoint mockKafka;

  @BeforeEach
  void setUp() throws Exception {
    AdviceWith.adviceWith(camelContext, "centralCreateLoanApplicationRoute", a -> {
      a.weaveById("createOrderJpaEndpoint").replace().to("mock:createOrderJpa");
      a.weaveById("createOrderCacheEndpoint").replace().to("mock:createOrderCache");
      a.weaveById("createOrderKafkaEndpoint").replace().to("mock:createOrderKafka");
    });
    camelContext.start();
  }

  @Test
  @DisplayName("Success: generates applicationId, sets RECEIVED, fans out to all backends")
  void test_CreateLoanApplication_Success() throws Exception {
    mockJpa.expectedMessageCount(1);
    mockCache.expectedMessageCount(1);
    mockKafka.expectedMessageCount(1);

    LoanApplication input = TestDataFactory.submissionInput();

    var result = fluentProducerTemplate.to("direct:centralCreateOrder")
        .withBody(input)
        .send();

    MockEndpoint.assertIsSatisfied(camelContext);
    LoanApplication response = result.getMessage().getBody(LoanApplication.class);
    assertThat(response.getApplicationId()).isNotBlank();
    assertThat(response.getStatus()).isEqualTo(ApplicationStatus.RECEIVED);
    assertThat(response.getSubmittedAt()).isNotNull();
  }

  @Test
  @DisplayName("Error: missing applicantId triggers ValidationException")
  void test_CreateLoanApplication_ValidationError_MissingApplicantId() {
    LoanApplication invalid = TestDataFactory.submissionInput();
    invalid.setApplicantId(null);

    assertThatThrownBy(() -> fluentProducerTemplate.to("direct:centralCreateOrder")
        .withBody(invalid)
        .request())
        .hasRootCauseInstanceOf(ValidationException.class);
  }
}
