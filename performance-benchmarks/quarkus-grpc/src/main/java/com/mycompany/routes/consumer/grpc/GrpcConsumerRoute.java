package com.mycompany.routes.consumer.grpc;

import com.mycompany.exception.GenericExceptionHandler;
import com.mycompany.mapper.api.GrpcOrderMapper;
import com.mycompany.model.domain.Order;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.grpc.GrpcConstants;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * Grpc Listener Route.
 *
 * @author camelbee
 */

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class GrpcConsumerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final GenericExceptionHandler genericExceptionHandler;
  final GrpcOrderMapper grpcOrderMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);

    from("grpc://0.0.0.0:{{camelbee.grpc-server.port}}/com.mycompany.order.grpc.OrderService").id("grpcOrderServiceRoute")
        .doTry()
        .to("direct:grpcMain")
        .doCatch(Exception.class)
        .to("direct:error")
        .end();

    from("direct:grpcMain")
        .choice()
        // CREATE ORDER
        .when(header(GrpcConstants.GRPC_METHOD_NAME_HEADER).isEqualTo("createOrder"))
        .process(e -> {
          com.mycompany.order.grpc.CreateOrderRequest createOrderRequest = e.getIn().getBody(com.mycompany.order.grpc.CreateOrderRequest.class);
          e.getIn().setHeader("transactionId", createOrderRequest.getTransactionId());
          e.getIn().setBody(grpcOrderMapper.protoToDomainOrder(createOrderRequest.getOrder()));
        })
        .to("direct:centralCreateOrder")
        .process(e -> {
          com.mycompany.order.grpc.CreateOrderResponse createOrderResponse = com.mycompany.order.grpc.CreateOrderResponse.newBuilder()
              .setOrder(grpcOrderMapper.domainToProtoOrder(e.getIn().getBody(Order.class))).build();

          e.getIn().setBody(createOrderResponse);
        })
        .otherwise()
        .throwException(new RuntimeException());

  }
}
