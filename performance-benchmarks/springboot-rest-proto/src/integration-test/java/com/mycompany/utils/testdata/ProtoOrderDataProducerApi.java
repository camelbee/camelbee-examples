package com.mycompany.utils.testdata;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.util.JsonFormat;
import com.mycompany.mapper.api.ProtoOrderMapper;
import com.mycompany.mapper.api.ProtoOrderMapperImpl;
import com.mycompany.model.api.proto.Order;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.SneakyThrows;

/**
 * Generates test Proto messages for various order scenarios.
 */
public class ProtoOrderDataProducerApi {

  ProtoOrderMapper protoOrderMapper = new ProtoOrderMapperImpl();

  // Define printer once as static field
  private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer()
      .includingDefaultValueFields()
      .preservingProtoFieldNames();

  public static void main(String[] args) {
    new ProtoOrderDataProducerApi().generateAllFiles();
  }

  public void generateAllFiles() {
    generateCreateOrderRequests();

  }

  private void generateCreateOrderRequests() {

    List<RequestResponseScenario> orderFiles = CreateOrderDomainTestDataProducer.generateCreateOrderRequests();

    orderFiles.stream().forEach(o -> {
      com.mycompany.model.api.proto.Order order = protoOrderMapper.domainToProtoOrder(o.getOrder());
      writeToFile("createorder", o.getName(), order);
    });

  }

  @SneakyThrows
  private void writeToFile(String folder, String fileName, GeneratedMessageV3 message) {

    String fullFolderPath = "src/integration-test/resources/data/inttest/api/proto/" + folder;
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
