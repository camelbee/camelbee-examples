package com.mycompany.catalog.mcp.utils.testdata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.catalog.mcp.mapper.api.McpOrderMapper;
import com.mycompany.catalog.mcp.mapper.api.McpOrderMapperImpl;
import com.mycompany.catalog.mcp.model.api.mcp.Order;
import com.mycompany.catalog.mcp.utils.JsonSerDe;
import lombok.SneakyThrows;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * Generates test JSON messages for different order scenarios,
 * intended for use as JSON-RPC request arguments in MCP tool calls.
 */
public class McpOrderDataProducerApi {

  McpOrderMapper mcpOrderMapper = new McpOrderMapperImpl();
  private static final ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args) {
    new McpOrderDataProducerApi().generateAllFiles();
  }

  public void generateAllFiles() {

    // Generate order management arguments for tool calls
    generateCreateOrderRequests();
  }


  private void generateCreateOrderRequests() {
    List<RequestResponseScenario> orderFiles = CreateOrderDomainTestDataProducer.generateCreateOrderRequests();

    orderFiles.stream().forEach(o -> {
      Order order = mcpOrderMapper.domainToMcpOrder(o.getOrder());
      writeToFile("createorder", o.getName(), order);
    });
  }






  @SneakyThrows
  public void writeToFile(String folder, String fileName, Order jsonObject) {

    JsonSerDe<Order> jsonSerDe = new JsonSerDe<>(new TypeReference<Order>() {
    });

    String fullFolderPath = "src/integration-test/resources/data/inttest/api/mcp/" + folder;

    Files.createDirectories(Paths.get(fullFolderPath));

    try (FileOutputStream outputStream = new FileOutputStream(fullFolderPath + "/" + fileName + ".json")) {
      outputStream.write(jsonSerDe.serialize(jsonObject).getBytes());
      System.out.printf("Json data written to %s%n", fileName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @SneakyThrows
  public void writeToFile(String folder, String fileName, List<Order> jsonObject) {

    JsonSerDe<List<Order>> jsonSerDe = new JsonSerDe<>(new TypeReference<List<Order>>() {
    });

    String fullFolderPath = "src/integration-test/resources/data/inttest/api/mcp/" + folder;

    Files.createDirectories(Paths.get(fullFolderPath));

    try (FileOutputStream outputStream = new FileOutputStream(fullFolderPath + "/" + fileName + ".json")) {
      outputStream.write(jsonSerDe.serialize(jsonObject).getBytes());
      System.out.printf("Json data written to %s%n", fileName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
