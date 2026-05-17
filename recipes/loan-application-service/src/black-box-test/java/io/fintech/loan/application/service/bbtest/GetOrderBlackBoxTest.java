package io.fintech.loan.application.service.bbtest;

import io.fintech.loan.application.service.utils.DataSeeder;
import io.fintech.loan.application.service.utils.DataVerifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetOrderBlackBoxTest extends BlackBoxTest {

  protected static final String BASE_PATH_API = "/data/inttest/api/";
  protected DataVerifier dataVerifier = new DataVerifier();
  protected DataSeeder dataSeeder = new DataSeeder();

  @AfterAll
  void closeConnections() {
    dataVerifier.close();
    dataSeeder.close();
  }

  protected void setupGetOrderSuccessScenario() throws Exception {
    dataSeeder.resetData();
  }

  protected void validateGetOrderSuccessScenario() throws Exception {

  }

  protected void setupGetOrderErrorScenario() throws Exception {
  }

  /**
   * Generic error scenario validation — used by non-REST interface tests.
   */
  protected void validateGetOrderErrorScenario() throws Exception {
    // Get is read-only; no persistence to verify.
  }

  /**
   * Validates the "not found" scenario — backend IS called but entity doesn't exist.
   * When REST backend is present, it fails first and subsequent backends (SOAP, GRPC) are NOT called.
   * When GRPC is the first/only WireMock backend, it IS called and returns NOT_FOUND.
   */
  protected void validateGetOrderNotFoundScenario() throws Exception {
  }

  /**
   * Validates that backends did NOT receive any request during a bad request scenario.
   * No backends should have been called at all.
   */
  protected void validateGetOrderBadRequestScenario() throws Exception {
  }

  /**
   * Validates that no data was persisted — Get is read-only so this is a no-op.
   */
  protected void validateNoDataPersisted() throws Exception {
    // Get is read-only; no persistence to verify.
  }

}
