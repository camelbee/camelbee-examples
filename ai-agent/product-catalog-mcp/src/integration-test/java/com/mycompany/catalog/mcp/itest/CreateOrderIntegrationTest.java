package com.mycompany.catalog.mcp.itest;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import com.mycompany.catalog.mcp.model.infra.jpa.postgresql.Purchase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.catalog.mcp.model.api.json.Order;
import com.mycompany.catalog.mcp.utils.JsonSerDe;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;
import java.util.List;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * Integration test for REST interface CreateOrder operation.
 */

public class CreateOrderIntegrationTest extends IntegrationTest {

  protected static final String CREATEORDER_BASE_PATH_API = BASE_PATH_API + "%s/createorder/";
  protected static final String CREATEORDER_BASE_PATH_INFRA = BASE_PATH_INFRA + "%s/createpurchase/";





  @EndpointInject("mock:captureCreateOrderJpa")
  protected MockEndpoint captureCreateOrderJpa;
























  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
            captureCreateOrderJpa
);


    verifyMockEndpoints = Arrays.asList(
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










        // add a new endpoint at the end of the jpa route
        AdviceWith.adviceWith(camelContext, "createOrderJpaRoute",
            a -> a.weaveAddLast().to("mock:captureCreateOrderJpa"));








    }

    initialized = true;

      super.resetBeforeAll();

  }


  protected void setupCreateOrderSuccessScenario(String fileName) throws Exception {

    resetAllMockedEndpoints();

    String requestFile = CREATEORDER_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase") ;

    captureError.expectedMessageCount(0);





      captureCreateOrderJpa.expectedMessageCount(1);

    clearJpaTables();



    
        

        










  }

  protected void validateCreateOrderSuccessScenario(String fileName) throws Exception {

    String requestFile = CREATEORDER_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    captureError.assertIsSatisfied();







    captureCreateOrderJpa.assertIsSatisfied();

    var jpaResult = queryJpaPurchases();
    assertThat(jpaResult).hasSize(1);
    assertThat(((List<Purchase>) jpaResult).get(0).getItems()).hasSize(10);


        












  }


  protected void setupCreateOrderBadRequestFromTheInterfaceScenario(String fileName, int interfaceRetryCount, boolean globalErrorHandlerUsed) throws Exception {

    resetAllMockedEndpoints();

    String requestFile = CREATEORDER_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    captureError.expectedMessageCount(globalErrorHandlerUsed ? interfaceRetryCount : 0);





      captureCreateOrderJpa.expectedMessageCount(0);

    clearJpaTables();




        













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















  }









}
