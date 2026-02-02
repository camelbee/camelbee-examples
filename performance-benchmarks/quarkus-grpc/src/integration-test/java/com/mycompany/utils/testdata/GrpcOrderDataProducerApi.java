package com.mycompany.utils.testdata;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.util.JsonFormat;
import com.mycompany.mapper.api.GrpcOrderMapper;
import com.mycompany.mapper.api.GrpcOrderMapperImpl;
import com.mycompany.order.grpc.Order;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;

/**
 * Generates test Proto messages for various order scenarios.
 */
public class GrpcOrderDataProducerApi {

  GrpcOrderMapper grpcOrderMapper = new GrpcOrderMapperImpl();

  // Define printer once as static field
  private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer()
      .includingDefaultValueFields()
      .preservingProtoFieldNames();

  public static void main(String[] args) {
    new GrpcOrderDataProducerApi().generateAllFiles();
  }

  public void generateAllFiles() {
    generateCreateOrderRequests();
  }

  private void generateCreateOrderRequests() {

    List<RequestResponseScenario> orderFiles = CreateOrderDomainTestDataProducer.generateCreateOrderRequests();

    orderFiles.stream().forEach(o -> {

      Order order = grpcOrderMapper.domainToProtoOrder(o.getOrder());

      com.mycompany.order.grpc.CreateOrderRequest createOrderRequest = com.mycompany.order.grpc.CreateOrderRequest.newBuilder()
          .setOrder(order)
          .setTransactionId(UUID.randomUUID().toString()).build();

      writeToFile("createorder", o.getName(), createOrderRequest);
    });

  }

  @SneakyThrows
  private void writeToFile(String folder, String fileName, GeneratedMessageV3 message) {

    String fullFolderPath = "src/integration-test/resources/data/inttest/api/grpc/" + folder;
    Files.createDirectories(Paths.get(fullFolderPath));

    String filePath = fullFolderPath + "/" + fileName + ".pb.json";

    try (FileWriter writer = new FileWriter(filePath)) {
      String json = JSON_PRINTER.print(message);
      writer.write(json);
      System.out.printf("✓ JSON written to %s%n", filePath);
    } catch (IOException e) {
      System.err.printf("✗ Failed to write JSON: %s%n", e.getMessage());
      throw e;
    }

    filePath = fullFolderPath + "/" + fileName + ".pb";

    try (FileOutputStream output = new FileOutputStream(filePath)) {
      message.writeTo(output);
      System.out.printf("✓ Binary data written to %s%n", filePath);
    } catch (IOException e) {
      System.err.printf("✗ Failed to write binary: %s%n", e.getMessage());
      throw e;
    }

  }

}
