package io.fintech.loan.application.service.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase;
import java.util.Arrays;
import java.util.List;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.factory.annotation.Value;

/**
 * Integration test for REST interface UpdateOrder operation.
 */

public class UpdateOrderIntegrationTest extends IntegrationTest {

  protected static final String UPDATEORDER_BASE_PATH_API = BASE_PATH_API + "%s/updateorder/";
  protected static final String UPDATEORDER_BASE_PATH_INFRA = BASE_PATH_INFRA + "%s/updatepurchase/";

  @Value("${backend-purchase-rest-api.uri}")
  protected String purchaseRestApiUri;

  @EndpointInject("mock:captureUpdateOrderRest")
  protected MockEndpoint captureUpdateOrderRest;

  @EndpointInject("mock:captureUpdateOrderJpa")
  protected MockEndpoint captureUpdateOrderJpa;

  @EndpointInject("mock:verifyUpdateOrderKafka")
  protected MockEndpoint verifyUpdateOrderKafka;

  @EndpointInject("mock:captureUpdateOrderKafka")
  protected MockEndpoint captureUpdateOrderKafka;

  @EndpointInject("mock:verifyUpdateOrderCache")
  protected MockEndpoint verifyUpdateOrderCache;

  @EndpointInject("mock:captureUpdateOrderCache")
  protected MockEndpoint captureUpdateOrderCache;

  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
        captureUpdateOrderRest,
        captureUpdateOrderJpa,
        captureUpdateOrderKafka,
        captureUpdateOrderCache
    );

    verifyMockEndpoints = Arrays.asList(
        verifyUpdateOrderKafka,
        verifyUpdateOrderCache
    );

    super.setup();

    /*
     * We use a flag to ensure routes are create or advised only once across all test classes
     * that extend this base class. A static initialization block cannot be used here
     * because it would run before Spring injects the camelContext, making it
     * unavailable during class loading.
     */
    if (!initialized) {

      // add a new endpoint at the end of the rest route
      AdviceWith.adviceWith(camelContext, "updateOrderRestRoute",
          a -> a.weaveAddLast().to("mock:captureUpdateOrderRest"));

      var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

      var routeKafkaSouthbound = new RouteDefinition();
      routeKafkaSouthbound.from("kafka:{{camelbeeservice.southbound-updateorder-topic}}"
          + "?groupId=camelbeeinttest"
          + "&autoOffsetReset=earliest"
          + "&valueDeserializer=io.apicurio.registry.serde.avro.AvroKafkaDeserializer"
          + "&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer"
          + "&additionalProperties.apicurio.registry.use-specific-avro-reader=true")
          .to("mock:verifyUpdateOrderKafka");

      modelContext.addRouteDefinition(routeKafkaSouthbound);

      // add a new endpoint at the end of the kafka route
      AdviceWith.adviceWith(camelContext, "updateOrderKafkaRoute",
          a -> a.weaveAddLast().to("mock:captureUpdateOrderKafka"));

      // add a new endpoint at the end of the jpa route
      AdviceWith.adviceWith(camelContext, "updateOrderJpaRoute",
          a -> a.weaveAddLast().to("mock:captureUpdateOrderJpa"));

      // add a new endpoint at the end of the cache route
      AdviceWith.adviceWith(camelContext, "updateOrderCacheRoute",
          a -> a.weaveAddLast().to("mock:captureUpdateOrderCache"));

    }
    initialized = true;

    super.resetBeforeAll();
  }

  protected void setupUpdateOrderSuccessScenario(String fileName) throws Exception {

    resetAllMockedEndpoints();

    String requestFile = UPDATEORDER_BASE_PATH_INFRA + fileName.replace("updateorder", "updatepurchase");

    captureError.expectedMessageCount(0);

    captureUpdateOrderRest.expectedMessageCount(1);

    captureUpdateOrderJpa.expectedMessageCount(1);

    captureUpdateOrderKafka.expectedMessageCount(1);
    verifyUpdateOrderKafka.expectedBodiesReceived(readBodyForKafkaSchemaRegistry("avro",
        requestFile.formatted("avro") + ".avro", io.fintech.loan.application.service.model.infra.avro.Purchase.class));

    captureUpdateOrderCache.expectedMessageCount(1);

    clearCacheTables();
  }

  protected void validateUpdateOrderSuccessScenario(String fileName, String orderId) throws Exception {

    String requestFile = UPDATEORDER_BASE_PATH_INFRA + fileName.replace("updateorder", "updatepurchase");

    captureError.assertIsSatisfied();

    captureUpdateOrderRest.assertIsSatisfied();

    wireMock.verify(patchRequestedFor(urlPathTemplate(purchaseRestApiUri + "/{id}"))
        .withPathParam("id", equalTo(orderId))
        .withRequestBody(equalToJson(readResource(requestFile.formatted("json") + ".json"))));

    captureUpdateOrderJpa.assertIsSatisfied();

    var jpaResult = queryJpaPurchases("purchase.id=%s".formatted(orderId));
    assertThat(jpaResult).hasSize(1);
    assertThat(((List<Purchase>) jpaResult).get(0).getItems()).hasSize(15);

    captureUpdateOrderKafka.assertIsSatisfied();
    verifyUpdateOrderKafka.assertIsSatisfied();

    captureUpdateOrderCache.assertIsSatisfied();
    // Verify write-through on UPDATE: cache should contain the updated purchase JSON.
    io.fintech.loan.application.service.model.domain.Order updatedCachedOrder = (io.fintech.loan.application.service.model.domain.Order) captureUpdateOrderCache
        .getReceivedExchanges().get(0).getIn().getBody();
    String updatedCachedJson = getCachedPurchaseJson(updatedCachedOrder.getId());
    assertThat(updatedCachedJson).as("Cache should contain the purchase after UPO write-through").isNotNull();
    assertThat(updatedCachedJson).contains(updatedCachedOrder.getId());

  }

  protected void setupUpdateOrderBadRequestFromTheInterfaceScenario(String fileName, int interfaceRetryCount, boolean globalErrorHandlerUsed) throws Exception {

    resetAllMockedEndpoints();

    captureError.expectedMessageCount(globalErrorHandlerUsed ? interfaceRetryCount : 0);

    captureUpdateOrderRest.expectedMessageCount(0);

    captureUpdateOrderJpa.expectedMessageCount(0);

    captureUpdateOrderKafka.expectedMessageCount(0);
    verifyUpdateOrderKafka.expectedMessageCount(0);

    captureUpdateOrderCache.expectedMessageCount(0);
  }

  protected void validateBadRequestFromTheInterfaceScenario(String fileName, String orderId) throws Exception {

    String requestFile = UPDATEORDER_BASE_PATH_INFRA + fileName.replace("updateorder", "updatepurchase");

    /*
    check if the captureError satisfied
    you can even go further to check the error captured via captureError.getReceivedExhanges(0).getError to check the type of error
     */
    captureError.assertIsSatisfied();

    captureUpdateOrderRest.assertIsSatisfied();

    wireMock.verify(0, patchRequestedFor(urlPathTemplate(purchaseRestApiUri + "/{id}"))
        .withPathParam("id", equalTo(orderId)));

    captureUpdateOrderJpa.assertIsSatisfied();

    var jpaResult = queryJpaPurchases("purchase.id=%s".formatted(orderId));
    assertThat(jpaResult).hasSize(1);
    assertThat(((List<Purchase>) jpaResult).get(0).getItems()).hasSize(5);

    captureUpdateOrderKafka.assertIsSatisfied();
    verifyUpdateOrderKafka.assertIsSatisfied();

    captureUpdateOrderCache.assertIsSatisfied();
  }

  protected void setupUpdateOrderErrorFromTheRestBackendScenario(int interfaceRetryCount, boolean globalErrorHandlerUsed) throws Exception {

    resetAllMockedEndpoints();

    captureError.expectedMessageCount(globalErrorHandlerUsed ? interfaceRetryCount : 0);

    captureUpdateOrderRest.expectedMessageCount(0);

    captureUpdateOrderJpa.expectedMessageCount(0);

    captureUpdateOrderKafka.expectedMessageCount(0);
    verifyUpdateOrderKafka.expectedMessageCount(0);

    captureUpdateOrderCache.expectedMessageCount(0);
  }

  protected void validateErrorFromTheRestBackendScenario(String fileName, String orderId, int interfaceRetryCount) throws Exception {

    String requestFile = UPDATEORDER_BASE_PATH_INFRA + fileName.replace("updateorder", "updatepurchase");

    /*
    check if the captureError is satisfied.
    you can even go further to check the error captured via captureError.getReceivedExhanges(0).getError to check the type of error.
    */
    captureError.assertIsSatisfied();

    captureUpdateOrderRest.assertIsSatisfied();

    wireMock.verify(interfaceRetryCount, patchRequestedFor(urlPathTemplate(purchaseRestApiUri + "/{id}"))
        .withPathParam("id", equalTo(orderId))
        .withRequestBody(equalToJson(readResource(requestFile.formatted("json") + ".json"))));

    captureUpdateOrderJpa.assertIsSatisfied();

    var jpaResult = queryJpaPurchases("purchase.id=%s".formatted(orderId));
    assertThat(jpaResult).hasSize(1);
    assertThat(((List<Purchase>) jpaResult).get(0).getItems()).hasSize(5);

    captureUpdateOrderKafka.assertIsSatisfied();
    verifyUpdateOrderKafka.assertIsSatisfied();

    captureUpdateOrderCache.assertIsSatisfied();
  }

}
