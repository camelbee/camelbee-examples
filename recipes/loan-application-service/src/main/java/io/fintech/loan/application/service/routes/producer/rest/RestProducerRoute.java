package io.fintech.loan.application.service.routes.producer.rest;

import static io.fintech.loan.application.service.constants.Constants.APPLICATION_JSON;
import static org.apache.camel.Exchange.CONTENT_TYPE;

import io.fintech.loan.application.service.mapper.infra.JsonCreditAssessmentMapper;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.model.infra.json.CreditAssessmentRequest;
import io.fintech.loan.application.service.model.infra.json.CreditAssessmentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * REST producer — calls the credit bureau /credit-assessments endpoint.
 * Stores the resulting {@link CreditAssessmentResult} on exchange property "creditAssessmentResult"
 * so the central route can fold it back into the LoanApplication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RestProducerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final JsonCreditAssessmentMapper jsonCreditAssessmentMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:updateOrderRest").routeId("creditBureauAssessmentRoute")
        .removeHeader(Exchange.HTTP_PATH)
        .removeHeader(Exchange.HTTP_URL)
        .setHeader(Exchange.HTTP_METHOD, constant("POST"))
        .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON))
        .setHeader(HttpHeaders.ACCEPT, constant(APPLICATION_JSON))
        .process(e -> {
          LoanApplication app = e.getIn().getBody(LoanApplication.class);
          CreditAssessmentRequest req = jsonCreditAssessmentMapper.domainToCreditAssessmentRequest(app);
          e.getIn().setBody(req);
        })
        .marshal().json()
        .to("http:{{backend-credit-bureau-api.url}}?bridgeEndpoint=true").id("creditBureauBackendEndpoint")
        .unmarshal().json(JsonLibrary.Jackson, CreditAssessmentResult.class)
        .process(e -> {
          CreditAssessmentResult result = e.getIn().getBody(CreditAssessmentResult.class);
          e.setProperty("creditAssessmentResult", result);
        });
  }
}
