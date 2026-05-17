package io.fintech.loan.application.service.routes.consumer.rest;

import static io.fintech.loan.application.service.constants.Constants.APPLICATION_JSON;
import static io.fintech.loan.application.service.constants.Constants.ORIGINAL_ACCEPT_CONTENT_TYPE;
import static io.fintech.loan.application.service.constants.Constants.ORIGINAL_CONTENT_TYPE;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import io.fintech.loan.application.service.exception.GenericExceptionHandler;
import io.fintech.loan.application.service.mapper.api.JsonLoanApplicationMapper;
import io.fintech.loan.application.service.model.api.json.LoanApplicationSubmissionRequest;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestConsumerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final GenericExceptionHandler genericExceptionHandler;
  final JsonLoanApplicationMapper jsonLoanApplicationMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(genericExceptionHandler.appErrorHandler());

    restConfiguration().bindingMode(RestBindingMode.off);
    rest().openApi().specification("openapi/order-api.yaml").missingOperation("ignore");

    // POST /loan-applications  (OpenAPI operationId = createOrder)
    from("direct:createOrder")
        .routeId("createLoanApplicationRestRoute")
        .process(this::setOriginalContentTypeProperties)
        .unmarshal().json(JsonLibrary.Jackson, LoanApplicationSubmissionRequest.class)
        .to("bean-validator://camelbee")
        .process(e -> {
          var req = e.getIn().getBody(LoanApplicationSubmissionRequest.class);
          e.getIn().setBody(jsonLoanApplicationMapper.jsonRequestToDomain(req));
        })
        .to("direct:centralCreateOrder")
        .process(e -> {
          LoanApplication app = e.getIn().getBody(LoanApplication.class);
          e.getIn().setBody(jsonLoanApplicationMapper.domainToJsonSubmissionResponse(app));
        })
        .marshal().json()
        .process(this::setOriginalContentTypePropertiesBack)
        .setHeader(HTTP_RESPONSE_CODE, constant(202));

    // GET /loan-applications/{applicationId}  (operationId = getOrder)
    from("direct:getOrder")
        .routeId("getLoanApplicationRestRoute")
        .process(this::setOriginalContentTypeProperties)
        .to("direct:centralGetOrder")
        .process(e -> {
          LoanApplication app = e.getIn().getBody(LoanApplication.class);
          e.getIn().setBody(jsonLoanApplicationMapper.domainToJsonLoanApplication(app));
        })
        .marshal().json()
        .process(this::setOriginalContentTypePropertiesBack)
        .setHeader(HTTP_RESPONSE_CODE, constant(200));

    // GET /loan-applications  (operationId = listOrders)
    from("direct:listOrders")
        .routeId("listLoanApplicationsRestRoute")
        .process(this::setOriginalContentTypeProperties)
        .to("direct:centralListOrders")
        .process(e -> {
          @SuppressWarnings("unchecked")
          List<LoanApplication> apps = (List<LoanApplication>) e.getIn().getBody();
          int page = e.getIn().getHeader("page", 0, Integer.class);
          int pageSize = e.getIn().getHeader("pageSize", 10, Integer.class);
          int totalItems = e.getIn().getHeader("totalItems", apps.size(), Integer.class);
          e.getIn().setBody(jsonLoanApplicationMapper.toJsonPage(apps, totalItems, page, pageSize));
        })
        .marshal().json()
        .process(this::setOriginalContentTypePropertiesBack)
        .setHeader(HTTP_RESPONSE_CODE, constant(200));
  }

  private void setOriginalContentTypeProperties(Exchange exchange) {
    Message message = exchange.getIn();
    String contentType = message.getHeader(CONTENT_TYPE, String.class);
    if (contentType == null) {
      contentType = APPLICATION_JSON;
    }
    exchange.setProperty(ORIGINAL_CONTENT_TYPE, contentType);
    exchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
    String acceptHeader = message.getHeader(HttpHeaders.ACCEPT, String.class);
    if (acceptHeader == null || acceptHeader.isEmpty() || "*/*".equals(acceptHeader)) {
      exchange.setProperty(ORIGINAL_ACCEPT_CONTENT_TYPE, contentType);
    } else {
      exchange.setProperty(ORIGINAL_ACCEPT_CONTENT_TYPE, acceptHeader);
    }
  }

  private void setOriginalContentTypePropertiesBack(Exchange exchange) {
    if (exchange.getProperty(ORIGINAL_ACCEPT_CONTENT_TYPE) != null) {
      exchange.getIn().setHeader(CONTENT_TYPE, exchange.getProperty(ORIGINAL_ACCEPT_CONTENT_TYPE));
    }
    exchange.getIn().removeHeader(HttpHeaders.ACCEPT);
  }
}
