package io.fintech.loan.application.service.itest;

import static org.assertj.core.api.Assertions.assertThat;

import io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase;
import java.util.Arrays;
import java.util.List;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;

/**
 * Integration test for REST interface CreateOrder operation.
 */

public class CreateOrderIntegrationTest extends IntegrationTest {

  protected static final String CREATEORDER_BASE_PATH_API = BASE_PATH_API + "%s/createorder/";
  protected static final String CREATEORDER_BASE_PATH_INFRA = BASE_PATH_INFRA + "%s/createpurchase/";

  @EndpointInject("mock:captureCreateOrderJpa")
  protected MockEndpoint captureCreateOrderJpa;

  @EndpointInject("mock:verifyCreateOrderKafka")
  protected MockEndpoint verifyCreateOrderKafka;

  @EndpointInject("mock:captureCreateOrderKafka")
  protected MockEndpoint captureCreateOrderKafka;

  @EndpointInject("mock:verifyCreateOrderCache")
  protected MockEndpoint verifyCreateOrderCache;

  @EndpointInject("mock:captureCreateOrderCache")
  protected MockEndpoint captureCreateOrderCache;

  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
        captureCreateOrderJpa,
        captureCreateOrderKafka,
        captureCreateOrderCache
    );

    verifyMockEndpoints = Arrays.asList(
        verifyCreateOrderKafka,
        verifyCreateOrderCache
    );

    super.setup();

    /*
     * We use a flag to ensure routes are create or advised only once across all test classes
     * that extend this base class. A static initialization block cannot be used here
     * because it would run before Spring injects the camelContext, making it
     * unavailable during class loading.
     */
    if (!initialized) {

      var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

      var routeKafkaSouthbound = new RouteDefinition();
      routeKafkaSouthbound.from("kafka:{{camelbeeservice.southbound-createorder-topic}}"
          + "?groupId=camelbeeinttest"
          + "&autoOffsetReset=earliest"
          + "&valueDeserializer=io.apicurio.registry.serde.avro.AvroKafkaDeserializer"
          + "&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer"
          + "&additionalProperties.apicurio.registry.use-specific-avro-reader=true")
          .to("mock:verifyCreateOrderKafka");

      modelContext.addRouteDefinition(routeKafkaSouthbound);

      // add a new endpoint at the end of the kafka route
      AdviceWith.adviceWith(camelContext, "createOrderKafkaRoute",
          a -> a.weaveAddLast().to("mock:captureCreateOrderKafka"));

      // add a new endpoint at the end of the jpa route
      AdviceWith.adviceWith(camelContext, "createOrderJpaRoute",
          a -> a.weaveAddLast().to("mock:captureCreateOrderJpa"));

      // add a new endpoint at the end of the cache route
      AdviceWith.adviceWith(camelContext, "createOrderCacheRoute",
          a -> a.weaveAddLast().to("mock:captureCreateOrderCache"));

    }

    initialized = true;

    super.resetBeforeAll();

  }

  protected void setupCreateOrderSuccessScenario(String fileName) throws Exception {

    resetAllMockedEndpoints();

    String requestFile = CREATEORDER_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    captureError.expectedMessageCount(0);

    captureCreateOrderJpa.expectedMessageCount(1);

    clearJpaTables();

    captureCreateOrderKafka.expectedMessageCount(1);
    verifyCreateOrderKafka.expectedBodiesReceived(readBodyForKafkaSchemaRegistry("avro",
        requestFile.formatted("avro") + ".avro", io.fintech.loan.application.service.model.infra.avro.Purchase.class));

    captureCreateOrderCache.expectedMessageCount(1);

    clearCacheTables();

  }

  protected void validateCreateOrderSuccessScenario(String fileName) throws Exception {

    String requestFile = CREATEORDER_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    captureError.assertIsSatisfied();

    captureCreateOrderJpa.assertIsSatisfied();

    var jpaResult = queryJpaPurchases();
    assertThat(jpaResult).hasSize(1);
    assertThat(((List<Purchase>) jpaResult).get(0).getItems()).hasSize(10);

    captureCreateOrderKafka.assertIsSatisfied();
    verifyCreateOrderKafka.assertIsSatisfied();

    captureCreateOrderCache.assertIsSatisfied();
    // Verify write-through: read the value straight out of the cache via its Camel component.
    io.fintech.loan.application.service.model.domain.Order cachedOrder = (io.fintech.loan.application.service.model.domain.Order) captureCreateOrderCache
        .getReceivedExchanges().get(0).getIn().getBody();
    String cachedJson = getCachedPurchaseJson(cachedOrder.getId());
    assertThat(cachedJson).as("Cache should contain the purchase after write-through").isNotNull();
    assertThat(cachedJson).contains(cachedOrder.getId());

  }

  protected void setupCreateOrderBadRequestFromTheInterfaceScenario(String fileName, int interfaceRetryCount, boolean globalErrorHandlerUsed) throws Exception {

    resetAllMockedEndpoints();

    String requestFile = CREATEORDER_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    captureError.expectedMessageCount(globalErrorHandlerUsed ? interfaceRetryCount : 0);

    captureCreateOrderJpa.expectedMessageCount(0);

    clearJpaTables();

    captureCreateOrderKafka.expectedMessageCount(0);
    verifyCreateOrderKafka.expectedMessageCount(0);

    captureCreateOrderCache.expectedMessageCount(0);

  }

  protected void validateBadRequestFromTheInterfaceScenario() throws Exception {
    /*
    check if the captureError satisfied
    you can even go further to check the error captured via captureError.getReceivedExhanges(0).getError to check the type of error
     */
    captureError.assertIsSatisfied();

    captureCreateOrderJpa.assertIsSatisfied();
    var jpaResult = queryJpaPurchases();
    assertThat(jpaResult).hasSize(0);

    captureCreateOrderKafka.assertIsSatisfied();
    verifyCreateOrderKafka.assertIsSatisfied();

    captureCreateOrderCache.assertIsSatisfied();

  }

}
