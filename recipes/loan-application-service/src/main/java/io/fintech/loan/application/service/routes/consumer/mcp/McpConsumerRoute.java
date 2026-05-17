package io.fintech.loan.application.service.routes.consumer.mcp;

import io.fintech.loan.application.service.exception.GenericExceptionHandler;
import io.fintech.loan.application.service.mapper.api.McpLoanApplicationMapper;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class McpConsumerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final GenericExceptionHandler genericExceptionHandler;
  final McpLoanApplicationMapper mcpLoanApplicationMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(genericExceptionHandler.appErrorHandler());

    from("direct:mcpSubmitLoanApplication")
        .routeId("mcpSubmitLoanApplicationRoute")
        .process(e -> {
          var req = e.getIn().getBody(io.fintech.loan.application.service.model.api.mcp.LoanApplicationSubmissionRequest.class);
          e.getIn().setBody(mcpLoanApplicationMapper.mcpRequestToDomain(req));
        })
        .to("direct:centralCreateOrder")
        .process(e -> {
          LoanApplication app = e.getIn().getBody(LoanApplication.class);
          e.getIn().setBody(mcpLoanApplicationMapper.domainToMcpSubmissionResponse(app));
        });

    from("direct:mcpGetLoanApplicationStatus")
        .routeId("mcpGetLoanApplicationStatusRoute")
        .to("direct:centralGetOrder")
        .process(e -> {
          LoanApplication app = e.getIn().getBody(LoanApplication.class);
          e.getIn().setBody(mcpLoanApplicationMapper.domainToMcpLoanApplication(app));
        });

    from("direct:mcpListPendingApplications")
        .routeId("mcpListPendingApplicationsRoute")
        .to("direct:centralListOrders")
        .process(e -> {
          @SuppressWarnings("unchecked")
          List<LoanApplication> apps = (List<LoanApplication>) e.getIn().getBody();
          int page = e.getIn().getHeader("page", 0, Integer.class);
          int pageSize = e.getIn().getHeader("pageSize", 10, Integer.class);
          int totalItems = e.getIn().getHeader("totalItems", apps.size(), Integer.class);
          e.getIn().setBody(mcpLoanApplicationMapper.toMcpPage(apps, totalItems, page, pageSize));
        });
  }
}
