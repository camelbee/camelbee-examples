package io.iot.sensor.ingestion.itest;

import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTest extends CamelQuarkusTestSupport {

  protected static final String BASE_PATH_API = "/data/inttest/api/";
  protected static final String BASE_PATH_INFRA = "/data/inttest/infra/";

  @Inject
  protected CamelContext camelContext;

  @Inject
  protected FluentProducerTemplate fluentProducerTemplate;

  @EndpointInject("mock:error")
  protected MockEndpoint captureError;

  protected List<MockEndpoint> captureMockEndpoints;
  protected List<MockEndpoint> verifyMockEndpoints;

  private static boolean initialized = false;
  private static boolean mongodbResetRoutesAdded = false;

  public void setup() throws Exception {
    if (initialized) {
      return;
    }
    initialized = true;

    AdviceWith.adviceWith(camelContext, "errorHandlerRoute",
        a -> a.weaveAddFirst().to("mock:error"));

    addMongodbResetRoutes();
  }

  private synchronized void addMongodbResetRoutes() throws Exception {
    if (mongodbResetRoutesAdded) {
      return;
    }
    var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

    var routeMongodbResetRemove = new RouteDefinition();
    routeMongodbResetRemove.from("direct:resetMongodbRemove")
        .toD("mongodb:mongoBean?database={{camelbeeservice.mongodb.database}}&collection=${header.collection}&operation=remove");
    modelContext.addRouteDefinition(routeMongodbResetRemove);

    var routeMongodbResetInsert = new RouteDefinition();
    routeMongodbResetInsert.from("direct:resetMongodbInsert")
        .toD("mongodb:mongoBean?database={{camelbeeservice.mongodb.database}}&collection=${header.collection}&operation=insert");
    modelContext.addRouteDefinition(routeMongodbResetInsert);

    var routeMongodbQueryFindAll = new RouteDefinition();
    routeMongodbQueryFindAll.from("direct:queryMongodbFindAll")
        .toD("mongodb:mongoBean?database={{camelbeeservice.mongodb.database}}&collection=${header.collection}&operation=findAll");
    modelContext.addRouteDefinition(routeMongodbQueryFindAll);

    mongodbResetRoutesAdded = true;
  }

  protected void resetAllMockedEndpoints() {
    captureMockEndpoints.forEach(endpoint -> {
      endpoint.reset();
      endpoint.setResultWaitTime(100);
    });
    verifyMockEndpoints.forEach(endpoint -> {
      endpoint.reset();
      endpoint.setResultWaitTime(5000);
    });
  }

  protected void resetBeforeAll() throws Exception {
    addMongodbResetRoutes();
    resetMongodbData();
  }

  protected void clearMongoDbTables() {
    String collection = "sensorReadings";
    fluentProducerTemplate.to("direct:resetMongodbRemove")
        .withHeader("collection", collection)
        .withBody(new org.bson.Document()).request();
  }

  private void resetMongodbData() throws Exception {
    String collection = "sensorReadings";
    fluentProducerTemplate.to("direct:resetMongodbRemove")
        .withHeader("collection", collection)
        .withBody(new org.bson.Document()).request();

    String json = readResource("/backend/mongodb/reset-mongodb.json");
    List<org.bson.Document> docs = org.bson.Document.parse("{\"d\":" + json + "}").getList("d", org.bson.Document.class);
    for (org.bson.Document doc : docs) {
      fluentProducerTemplate.to("direct:resetMongodbInsert")
          .withHeader("collection", collection)
          .withBody(doc).request();
    }
  }

  @SuppressWarnings("unchecked")
  protected int countMongodbCollection(String collection) throws Exception {
    java.util.List<org.bson.Document> result = (java.util.List<org.bson.Document>) fluentProducerTemplate.to("direct:queryMongodbFindAll")
        .withHeader("collection", collection)
        .withBody(null).request();
    return result != null ? result.size() : 0;
  }

  protected String readResource(String path) throws Exception {
    return IOUtils.resourceToString(path, StandardCharsets.UTF_8);
  }

  protected byte[] readResourceBinary(String path) throws Exception {
    return IOUtils.resourceToByteArray(path);
  }

  protected void setBody(Exchange exchange, String payloadFormat, String requestFile) throws Exception {
    if (isBinaryFormat(payloadFormat)) {
      byte[] data = readResourceBinary(requestFile);
      exchange.getIn().setBody(data);
      exchange.getIn().setHeader("Content-Length", data.length);
    } else {
      exchange.getIn().setBody(readResource(requestFile));
    }
  }

  protected boolean isBinaryFormat(String payloadFormat) {
    return Set.of("avro", "proto").contains(payloadFormat);
  }

  protected String getFilePostFix(String payloadFormat) {
    if (payloadFormat == null || payloadFormat.isBlank()) {
      throw new IllegalArgumentException("payloadFormat must not be null or blank");
    }
    return switch (payloadFormat.toLowerCase()) {
      case "json" -> "json";
      case "xml" -> "xml";
      case "proto" -> "pb";
      case "avro" -> "avro";
      default -> throw new IllegalArgumentException("Unsupported payload format: " + payloadFormat);
    };
  }
}
