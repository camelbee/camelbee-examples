package com.mycompany.catalog.mcp.routes;

import com.mycompany.catalog.mcp.model.domain.Product;
import com.mycompany.catalog.mcp.model.domain.ProductPage;
import com.mycompany.catalog.mcp.utils.testdata.RequestResponseScenario;
import io.quarkus.test.junit.QuarkusTestProfile;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.apache.commons.io.IOUtils;

/**
 * Abstract base class for Camel route unit testing.
 */
public abstract class UnitTest extends CamelQuarkusTestSupport implements QuarkusTestProfile {

  @Inject
  protected FluentProducerTemplate fluentProducerTemplate;

  @Inject
  protected CamelContext camelContext;

  protected Product getProductByScenarioName(List<RequestResponseScenario> scenarios, String scenarioName) {
    Optional<RequestResponseScenario> scenario = scenarios.stream()
        .filter(s -> s.getName().equals(scenarioName))
        .findFirst();

    return scenario.orElseThrow(() -> new NoSuchElementException("No scenario found with name: " + scenarioName))
        .getProduct();
  }

  protected ProductPage getProductPageByScenarioName(List<RequestResponseScenario> scenarios, String scenarioName) {
    Optional<RequestResponseScenario> scenario = scenarios.stream()
        .filter(s -> s.getName().equals(scenarioName))
        .findFirst();

    return scenario.orElseThrow(() -> new NoSuchElementException("No scenario found with name: " + scenarioName))
        .getProductPage();
  }

  protected String readResource(String path) throws IOException {
    return IOUtils.resourceToString(path, StandardCharsets.UTF_8);
  }

  @Override
  public String getConfigProfile() {
    return "test";
  }

}
