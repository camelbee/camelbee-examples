package com.mycompany.catalog.mcp.routes.producer.jpa;

import com.mycompany.catalog.mcp.mapper.infra.JpaAuditLogMapper;
import com.mycompany.catalog.mcp.model.domain.AuditLog;
import com.mycompany.catalog.mcp.model.infra.jpa.postgresql.AuditLogEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * JPA Producer Route for writing audit logs.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class JpaProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final JpaAuditLogMapper jpaAuditLogMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:writeAuditLogJpa").routeId("writeAuditLogJpaRoute")
        .process(e -> {
          String userId = e.getIn().getHeader("userId", "anonymous", String.class);
          String toolName = e.getIn().getHeader("toolName", String.class);
          String parameters = e.getIn().getHeader("toolParameters", String.class);

          AuditLog auditLog = AuditLog.builder()
              .userId(userId)
              .toolName(toolName)
              .parameters(parameters)
              .timestamp(Instant.now())
              .responseStatus(AuditLog.ResponseStatus.SUCCESS)
              .build();

          // Save current body (product response) and set audit log as body for JPA persist
          Object responseBody = e.getIn().getBody();
          AuditLogEntity entity = jpaAuditLogMapper.domainAuditLogToJpaAuditLogEntity(auditLog);
          e.getIn().setBody(entity);
          // Keep the response body accessible after JPA persist
          e.setProperty("productResponseBody", responseBody);
        })
        .to("jpa:com.mycompany.catalog.mcp.model.infra.jpa.postgresql.AuditLogEntity").id("writeAuditLogJpaEndpoint")
        .process(e -> {
          // Restore the product response body so it's returned to the consumer
          e.getIn().setBody(e.getProperty("productResponseBody"));
        });

  }

}
