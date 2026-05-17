package io.fintech.loan.application.service.routes.consumer.mcp;

import io.fintech.loan.application.service.model.api.mcp.LoanApplication;
import io.fintech.loan.application.service.model.api.mcp.LoanApplicationPage;
import io.fintech.loan.application.service.model.api.mcp.LoanApplicationSubmissionRequest;
import io.fintech.loan.application.service.model.api.mcp.LoanApplicationSubmissionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.FluentProducerTemplate;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class McpTools {

  private final FluentProducerTemplate fluentProducerTemplate;

  @McpTool(name = "submitLoanApplication",
      description = "Submit a new loan application for async processing. Returns immediately with RECEIVED status.")
  LoanApplicationSubmissionResponse submitLoanApplication(
      @McpToolParam(description = "Loan application submission request") LoanApplicationSubmissionRequest request,
      @McpToolParam(description = "Client-generated correlation ID for distributed tracing and logging",
          required = false) String transactionId
  ) throws Exception {
    log.debug("MCP Tool: submitLoanApplication(applicantId: {})", request.getApplicantId());

    var result = fluentProducerTemplate
        .to("direct:mcpSubmitLoanApplication")
        .withHeader("transactionId", transactionId)
        .withBody(request)
        .send();

    if (result.getMessage().getBody() instanceof Exception ex) {
      throw ex;
    }
    return result.getMessage().getBody(LoanApplicationSubmissionResponse.class);
  }

  @McpTool(name = "getLoanApplicationStatus",
      description = "Get the current status and decision of a loan application. Served from cache when available.")
  LoanApplication getLoanApplicationStatus(
      @McpToolParam(description = "The application ID") String applicationId,
      @McpToolParam(description = "Client-generated correlation ID for distributed tracing and logging",
          required = false) String transactionId
  ) throws Exception {
    log.debug("MCP Tool: getLoanApplicationStatus(applicationId: {})", applicationId);

    var result = fluentProducerTemplate
        .to("direct:mcpGetLoanApplicationStatus")
        .withHeader("applicationId", applicationId)
        .withHeader("transactionId", transactionId)
        .send();

    if (result.getMessage().getBody() instanceof Exception ex) {
      throw ex;
    }
    return result.getMessage().getBody(LoanApplication.class);
  }

  @McpTool(name = "listPendingApplications",
      description = "List applications requiring manual review (status = PENDING_REVIEW), paginated.")
  LoanApplicationPage listPendingApplications(
      @McpToolParam(description = "Page number (zero-based)", required = false) Integer page,
      @McpToolParam(description = "Number of applications per page", required = false) Integer pageSize,
      @McpToolParam(description = "Client-generated correlation ID for distributed tracing and logging",
          required = false) String transactionId
  ) throws Exception {
    int effectivePage = page == null ? 0 : page;
    int effectivePageSize = pageSize == null ? 10 : pageSize;
    log.debug("MCP Tool: listPendingApplications(page: {}, pageSize: {})", effectivePage, effectivePageSize);

    var result = fluentProducerTemplate
        .to("direct:mcpListPendingApplications")
        .withHeader("status", "PENDING_REVIEW")
        .withHeader("page", effectivePage)
        .withHeader("pageSize", effectivePageSize)
        .withHeader("transactionId", transactionId)
        .send();

    if (result.getMessage().getBody() instanceof Exception ex) {
      throw ex;
    }
    return result.getMessage().getBody(LoanApplicationPage.class);
  }
}
