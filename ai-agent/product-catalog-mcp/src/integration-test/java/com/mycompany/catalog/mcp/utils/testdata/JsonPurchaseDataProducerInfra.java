
package com.mycompany.catalog.mcp.utils.testdata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mycompany.catalog.mcp.mapper.infra.JsonInfraErrorMapper;
import com.mycompany.catalog.mcp.mapper.infra.JsonInfraErrorMapperImpl;
import com.mycompany.catalog.mcp.mapper.infra.JsonPurchaseMapper;
import com.mycompany.catalog.mcp.mapper.infra.JsonPurchaseMapperImpl;
import com.mycompany.catalog.mcp.model.infra.json.Error;
import com.mycompany.catalog.mcp.model.infra.json.Purchase;
import com.mycompany.catalog.mcp.utils.JsonSerDe;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.SneakyThrows;

/**
 * Generates test Json messages for various order scenarios.
 */
public class JsonPurchaseDataProducerInfra {

  JsonPurchaseMapper jsonPurchaseMapper = new JsonPurchaseMapperImpl();
  JsonInfraErrorMapper jsonErrorMapper = new JsonInfraErrorMapperImpl();

  public static void main(String[] args) {
    new JsonPurchaseDataProducerInfra().generateAllFiles();
  }

  public void generateAllFiles() {
    generateListOrdersResponse();
  }






  private void generateListOrdersResponse() {

    for (int page = 1; page < 5; page++) {
      List<RequestResponseScenario> ordersFiles = ListOrdersDomainTestDataProducer.generateListOrdersResponse(page);

      ordersFiles.stream().forEach(o -> {

        List<Purchase> purchases = o.getOrders().stream().map(jsonPurchaseMapper::domainOrderToJsonPurchase).toList();

        writeToFile("listpurchases", o.getName(), purchases, true);
      });
    }

    List<RequestResponseScenario> errorFiles = ListOrdersDomainTestDataProducer.generateListOrdersErrorResponse();

    errorFiles.stream().forEach(o -> {
      Error error = jsonErrorMapper.domainErrorToJsonError(o.getError());
      writeToFile("listpurchases", o.getName(), error, true);
    });
  }

  @SneakyThrows
  public static void writeToFile(String folder, String fileName, Purchase jsonObject, boolean copyToWiremock) {

    JsonSerDe<Purchase> jsonSerDe = new JsonSerDe<>(new TypeReference<Purchase>() {
    });

    String fullFolderPath = "src/integration-test/resources/data/inttest/infra/json/" + folder;

    Files.createDirectories(Paths.get(fullFolderPath));

    try (FileOutputStream outputStream = new FileOutputStream(fullFolderPath + "/" + fileName + ".json")) {
      outputStream.write(jsonSerDe.serialize(jsonObject).getBytes());
      System.out.printf("Binary data written to %s%n", fileName);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (copyToWiremock) {
      String wiremocFolderPath = "src/integration-test/resources/docker/wiremock/__files/rest/json/" + folder;

      FileUtils.copyFile(fullFolderPath, fileName + ".json", wiremocFolderPath, fileName + ".json");

    }
  }

  @SneakyThrows
  public static void writeToFile(String folder, String fileName, List<Purchase> jsonObject, boolean copyToWiremock) {

    JsonSerDe<List<Purchase>> jsonSerDe = new JsonSerDe<>(new TypeReference<List<Purchase>>() {
    });

    String fullFolderPath = "src/integration-test/resources/data/inttest/infra/json/" + folder;

    Files.createDirectories(Paths.get(fullFolderPath));

    try (FileOutputStream outputStream = new FileOutputStream(fullFolderPath + "/" + fileName + ".json")) {
      outputStream.write(jsonSerDe.serialize(jsonObject).getBytes());
      System.out.printf("Binary data written to %s%n", fileName);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (copyToWiremock) {
      String wiremocFolderPath = "src/integration-test/resources/docker/wiremock/__files/rest/json/" + folder;

      FileUtils.copyFile(fullFolderPath, fileName + ".json", wiremocFolderPath, fileName + ".json");

    }
  }

  @SneakyThrows
  public void writeToFile(String folder, String fileName, Error jsonObject, boolean copyToWiremock) {

    JsonSerDe<Error> jsonSerDe = new JsonSerDe<>(new TypeReference<Error>() {
    });

    String fullFolderPath = "src/integration-test/resources/data/inttest/infra/json/" + folder;

    Files.createDirectories(Paths.get(fullFolderPath));

    try (FileOutputStream outputStream = new FileOutputStream(fullFolderPath + "/" + fileName + ".json")) {
      outputStream.write(jsonSerDe.serialize(jsonObject).getBytes());
      System.out.printf("Binary data written to %s%n", fileName);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (copyToWiremock) {
      String wiremocFolderPath = "src/integration-test/resources/docker/wiremock/__files/rest/json/" + folder;

      FileUtils.copyFile(fullFolderPath, fileName + ".json", wiremocFolderPath, fileName + ".json");

    }
  }

}
