package com.mycompany.catalog.mcp.bbtest;

import com.mycompany.catalog.mcp.utils.DataVerifier;

/**
 * Black-box test base for GetProduct operation.
 */
public class GetProductBlackBoxTest extends BlackBoxTest {

  protected DataVerifier dataVerifier = new DataVerifier();

  protected void setupGetProductSuccessScenario() {
    dataVerifier.clearAuditLogTable();
  }

  protected void validateGetProductSuccessScenario() {
    int auditLogCount = dataVerifier.countAuditLogs("tool_name = 'getProduct'");
    assert auditLogCount > 0 : "Expected at least 1 audit log entry for getProduct";
  }

}
