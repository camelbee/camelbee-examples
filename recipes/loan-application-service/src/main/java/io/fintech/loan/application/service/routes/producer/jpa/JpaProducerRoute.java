package io.fintech.loan.application.service.routes.producer.jpa;

import io.fintech.loan.application.service.constants.Constants;
import io.fintech.loan.application.service.mapper.infra.JpaLoanApplicationMapper;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final JpaLoanApplicationMapper jpaMapper;

  private static final String JPA_ENTITY = "jpa:io.fintech.loan.application.service.model.infra.jpa.postgresql.LoanApplication";

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    // INSERT — used by CRO. Body is domain LoanApplication on entry.
    from("direct:createOrderJpa").routeId("createLoanApplicationJpaRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .process(e -> {
          LoanApplication app = e.getIn().getBody(LoanApplication.class);
          e.getIn().setBody(jpaMapper.domainToJpa(app));
        })
        .to(JPA_ENTITY)
        .process(e -> {
          var entity = e.getIn().getBody(
              io.fintech.loan.application.service.model.infra.jpa.postgresql.LoanApplication.class);
          e.getIn().setBody(jpaMapper.jpaToDomain(entity));
        })
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

    // UPDATE — used by UPO. Body is the (already-modified) domain LoanApplication.
    from("direct:updateOrderJpa").routeId("updateLoanApplicationJpaRoute")
        .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
        .process(e -> {
          LoanApplication app = e.getIn().getBody(LoanApplication.class);
          Map<String, Object> params = new HashMap<>();
          params.put("applicationId", app.getApplicationId());
          e.getIn().setHeader("CamelJpaParameters", params);
          e.setProperty("incomingApplication", app);
        })
        .to(JPA_ENTITY + "?namedQuery=LoanApplication.findByApplicationId&singleResult=true")
        .process(e -> {
          var entity = e.getIn().getBody(
              io.fintech.loan.application.service.model.infra.jpa.postgresql.LoanApplication.class);
          LoanApplication updated = e.getProperty("incomingApplication", LoanApplication.class);
          entity.setStatus(updated.getStatus() == null ? entity.getStatus() : updated.getStatus().name());
          entity.setRiskScore(updated.getRiskScore() != null ? updated.getRiskScore() : entity.getRiskScore());
          entity.setDecisionReason(updated.getDecisionReason() != null ? updated.getDecisionReason() : entity.getDecisionReason());
          entity.setProcessedAt(updated.getProcessedAt() != null ? updated.getProcessedAt() : entity.getProcessedAt());
          e.getIn().setBody(entity);
        })
        .to(JPA_ENTITY)
        .process(e -> {
          var entity = e.getIn().getBody(
              io.fintech.loan.application.service.model.infra.jpa.postgresql.LoanApplication.class);
          e.getIn().setBody(jpaMapper.jpaToDomain(entity));
        })
        .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());

    // GET by applicationId — used by cache-aside GEO.
    from("direct:getOrderJpa").routeId("getLoanApplicationJpaRoute")
        .process(this::setApplicationIdParam)
        .to(JPA_ENTITY + "?namedQuery=LoanApplication.findByApplicationId&singleResult=true")
        .process(e -> {
          var entity = e.getIn().getBody(
              io.fintech.loan.application.service.model.infra.jpa.postgresql.LoanApplication.class);
          e.getIn().setBody(entity == null ? null : jpaMapper.jpaToDomain(entity));
        });

    // LIST with optional status filter, paginated.
    from("direct:listOrdersJpa").routeId("listLoanApplicationsJpaRoute")
        .process(this::setListQueryParams)
        .choice()
        .when(header("status").isNotNull())
        .to(JPA_ENTITY + "?namedQuery=LoanApplication.countByStatus&singleResult=true")
        .otherwise()
        .to(JPA_ENTITY + "?namedQuery=LoanApplication.countAll&singleResult=true")
        .end()
        .setHeader("totalItems", body())
        .process(this::setPaginationHeaders)
        .choice()
        .when(header("status").isNotNull())
        .to(JPA_ENTITY + "?namedQuery=LoanApplication.findByStatus")
        .otherwise()
        .to(JPA_ENTITY + "?namedQuery=LoanApplication.findAll")
        .end()
        .process(e -> {
          @SuppressWarnings("unchecked")
          List<io.fintech.loan.application.service.model.infra.jpa.postgresql.LoanApplication> entities = (List<io.fintech.loan.application.service.model.infra.jpa.postgresql.LoanApplication>) e
              .getIn().getBody();
          e.getIn().setBody(jpaMapper.jpaToDomainList(entities));
          Number total = e.getIn().getHeader("totalItems", Number.class);
          e.getIn().setHeader("totalItems", total == null ? 0 : total.intValue());
        });
  }

  private void setApplicationIdParam(Exchange exchange) {
    Map<String, Object> params = new HashMap<>();
    params.put("applicationId", exchange.getIn().getHeader("applicationId"));
    exchange.getIn().setHeader("CamelJpaParameters", params);
  }

  private void setListQueryParams(Exchange exchange) {
    String status = exchange.getIn().getHeader("status", String.class);
    if (status != null && !status.isBlank()) {
      Map<String, Object> params = new HashMap<>();
      params.put("status", status);
      exchange.getIn().setHeader("CamelJpaParameters", params);
    }
  }

  private void setPaginationHeaders(Exchange exchange) {
    int page = exchange.getIn().getHeader("page", 0, Integer.class);
    int pageSize = exchange.getIn().getHeader("pageSize", 10, Integer.class);
    int offset = page * pageSize;
    exchange.getIn().setHeader("CamelJpaFirstResult", offset);
    exchange.getIn().setHeader("CamelJpaMaximumResults", pageSize);
  }
}
