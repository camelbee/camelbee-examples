package com.mycompany.routes;

import com.mycompany.model.domain.Order;
import com.mycompany.utils.testdata.RequestResponseScenario;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * Abstract base class for Camel route unit testing.
 *
 * @author camelbee
 */
@CamelSpringBootTest
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@UseAdviceWith
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class UnitTest {

  @Autowired
  protected FluentProducerTemplate fluentProducerTemplate;

  @Autowired
  protected CamelContext camelContext;

  /**
   * Retrieves an Order object from a specific test scenario by its filename.
   * <p>
   * This method searches all pre-loaded test scenarios and returns the Order
   * from the first scenario matching the specified filename.
   * </p>
   *
   * @param scenarioName The filename of the test scenario to retrieve
   * @return The Order object from the requested scenario
   * @throws NoSuchElementException If no scenario with the specified name is found
   */
  protected Order getOrderByScenarioName(List<RequestResponseScenario> singleOrderScenarios, String scenarioName) {
    Optional<RequestResponseScenario> scenario = singleOrderScenarios.stream()
        .filter(s -> s.getName().equals(scenarioName))
        .findFirst();

    return scenario.orElseThrow(() -> new NoSuchElementException("No scenario found with name: " + scenarioName))
        .getOrder();
  }

  /**
   * Retrieves a List of Order objects from a specific test scenario by its filename.
   * <p>
   * This method searches all pre-loaded test scenarios and returns the Orders list
   * from the first scenario matching the specified filename.
   * </p>
   *
   * @param scenarioName The filename of the test scenario to retrieve
   * @return The List of Order objects from the requested scenario
   * @throws NoSuchElementException If no scenario with the specified name is found
   */
  protected List<Order> getOrdersByScenarioName(List<RequestResponseScenario> multipleOrdersScenarios, String scenarioName) {
    Optional<RequestResponseScenario> scenario = multipleOrdersScenarios.stream()
        .filter(s -> s.getName().equals(scenarioName))
        .findFirst();

    return scenario.orElseThrow(() -> new NoSuchElementException("No scenario found with name: " + scenarioName))
        .getOrders();
  }

  /**
   * Reads resource files from the classpath.
   * <p>
   * This utility method loads test data files from the classpath,
   * typically used for loading test request/response data.
   * </p>
   *
   * @param path The resource path (relative to classpath)
   * @return The content of the resource as a string
   * @throws IOException If the resource cannot be read
   */
  protected String readResource(String path) throws IOException {
    return IOUtils.resourceToString(path, StandardCharsets.UTF_8);
  }
}
