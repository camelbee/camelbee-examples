package com.mycompany.catalog.mcp.bbtest;

import com.mycompany.catalog.mcp.utils.DataVerifier;

/**
 * Black-box test base for ListProducts operation.
 */
public class ListProductsBlackBoxTest extends BlackBoxTest {

  protected DataVerifier dataVerifier = new DataVerifier();

  protected void setupListProductsSuccessScenario() {
    dataVerifier.clearAuditLogTable();
  }

  protected void validateListProductsSuccessScenario() {
    // Verify audit log was written
    int auditLogCount = dataVerifier.countAuditLogs("tool_name = 'listProducts'");
    assert auditLogCount > 0 : "Expected at least 1 audit log entry for listProducts";
  }

}
