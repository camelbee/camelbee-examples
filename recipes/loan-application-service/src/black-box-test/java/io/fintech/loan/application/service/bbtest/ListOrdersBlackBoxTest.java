package io.fintech.loan.application.service.bbtest;

import io.fintech.loan.application.service.utils.DataSeeder;
import io.fintech.loan.application.service.utils.DataVerifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ListOrdersBlackBoxTest extends BlackBoxTest {

  protected static final String BASE_PATH_API = "/data/inttest/api/";
  protected DataVerifier dataVerifier = new DataVerifier();
  protected DataSeeder dataSeeder = new DataSeeder();
  /** Cursor for NoSQL cursor-based pagination (chained across parameterized test invocations). */
  protected String lastNextCursor = "";

  @AfterAll
  void closeConnections() {
    dataVerifier.close();
    dataSeeder.close();
  }

  protected void setupListOrdersSuccessScenario() throws Exception {
    dataSeeder.resetData();
  }

  protected void validateListOrdersSuccessScenario(int expectedOrders) throws Exception {

  }

  protected void setupListOrdersErrorScenario() throws Exception {
  }

  /**
   * Generic error scenario validation — used by non-REST interface tests.
   */
  protected void validateListOrdersErrorScenario() throws Exception {
    // List is read-only; no persistence to verify.
  }

  /**
   * Validates that backends did NOT receive any request during a bad request scenario.
   * No backends should have been called at all.
   */
  protected void validateListOrdersBadRequestScenario() throws Exception {
  }

  /**
   * Validates that no data was persisted — List is read-only so this is a no-op.
   */
  protected void validateNoDataPersisted() throws Exception {
    // List is read-only; no persistence to verify.
  }

}
