package io.fintech.loan.application.service.routes.central;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.fintech.loan.application.service.exception.DataNotFoundException;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.routes.UnitTest;
import io.fintech.loan.application.service.utils.TestDataFactory;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CentralGetLoanApplicationRoute — cache-aside")
class CentralGetOrderRouteUnitTest extends UnitTest {

  @EndpointInject("mock:cacheRead")
  protected MockEndpoint mockCacheRead;
  @EndpointInject("mock:jpaRead")
  protected MockEndpoint mockJpaRead;
  @EndpointInject("mock:cacheWarm")
  protected MockEndpoint mockCacheWarm;

  @Test
  @DisplayName("Cache hit: returns cached body, JPA NOT called")
  void test_CacheHit() throws Exception {
    LoanApplication cached = TestDataFactory.bureauApprovalInput();
    AdviceWith.adviceWith(camelContext, "centralGetLoanApplicationRoute", a -> {
      a.weaveById("getOrderCacheEndpoint").replace().process(e -> e.getIn().setBody(cached)).to("mock:cacheRead");
      a.weaveById("getOrderJpaEndpoint").replace().to("mock:jpaRead");
      a.weaveById("cacheWarmOnMissEndpoint").replace().to("mock:cacheWarm");
    });
    camelContext.start();

    mockCacheRead.expectedMessageCount(1);
    mockJpaRead.expectedMessageCount(0);
    mockCacheWarm.expectedMessageCount(0);

    var result = fluentProducerTemplate.to("direct:centralGetOrder")
        .withHeader("applicationId", cached.getApplicationId())
        .send();

    MockEndpoint.assertIsSatisfied(camelContext);
    LoanApplication response = result.getMessage().getBody(LoanApplication.class);
    assertThat(response.getApplicationId()).isEqualTo(cached.getApplicationId());
  }

  @Test
  @DisplayName("Cache miss + JPA hit: warms cache, returns JPA result")
  void test_CacheMiss_JpaHit() throws Exception {
    LoanApplication fromJpa = TestDataFactory.bureauApprovalInput();
    AdviceWith.adviceWith(camelContext, "centralGetLoanApplicationRoute", a -> {
      a.weaveById("getOrderCacheEndpoint").replace().process(e -> e.getIn().setBody(null)).to("mock:cacheRead");
      a.weaveById("getOrderJpaEndpoint").replace().process(e -> e.getIn().setBody(fromJpa)).to("mock:jpaRead");
      a.weaveById("cacheWarmOnMissEndpoint").replace().to("mock:cacheWarm");
    });
    camelContext.start();

    mockCacheRead.expectedMessageCount(1);
    mockJpaRead.expectedMessageCount(1);
    mockCacheWarm.expectedMessageCount(1);

    var result = fluentProducerTemplate.to("direct:centralGetOrder")
        .withHeader("applicationId", fromJpa.getApplicationId())
        .send();

    MockEndpoint.assertIsSatisfied(camelContext);
    LoanApplication response = result.getMessage().getBody(LoanApplication.class);
    assertThat(response.getApplicationId()).isEqualTo(fromJpa.getApplicationId());
  }

  @Test
  @DisplayName("Cache miss + JPA miss: throws DataNotFoundException")
  void test_NotFound() throws Exception {
    AdviceWith.adviceWith(camelContext, "centralGetLoanApplicationRoute", a -> {
      a.weaveById("getOrderCacheEndpoint").replace().process(e -> e.getIn().setBody(null)).to("mock:cacheRead");
      a.weaveById("getOrderJpaEndpoint").replace().process(e -> e.getIn().setBody(null)).to("mock:jpaRead");
      a.weaveById("cacheWarmOnMissEndpoint").replace().to("mock:cacheWarm");
    });
    camelContext.start();

    assertThatThrownBy(() -> fluentProducerTemplate.to("direct:centralGetOrder")
        .withHeader("applicationId", "missing-id")
        .request())
        .hasRootCauseInstanceOf(DataNotFoundException.class);
  }
}
