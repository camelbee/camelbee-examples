package com.mycompany.product.catalog.routes.producer.jpa;

import com.mycompany.product.catalog.mapper.infra.JpaAuditLogMapper;
import com.mycompany.product.catalog.model.domain.AuditLog;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * JPA Producer Route for audit log persistence.
 *
 * @author camelbee
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
          AuditLog domainAuditLog = e.getIn().getBody(AuditLog.class);
          e.getIn().setBody(jpaAuditLogMapper.domainToJpaAuditLog(domainAuditLog));
        })
        .to("jpa:com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog")
        .id("writeAuditLogJpaBackendEndpoint");

  }

}
