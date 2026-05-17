
package io.fintech.loan.application.service.utils.testdata;

import com.fasterxml.jackson.core.type.TypeReference;
import io.fintech.loan.application.service.mapper.infra.JsonInfraErrorMapper;
import io.fintech.loan.application.service.mapper.infra.JsonInfraErrorMapperImpl;
import io.fintech.loan.application.service.mapper.infra.JsonPurchaseMapper;
import io.fintech.loan.application.service.mapper.infra.JsonPurchaseMapperImpl;
import io.fintech.loan.application.service.model.infra.json.Error;
import io.fintech.loan.application.service.model.infra.json.Purchase;
import io.fintech.loan.application.service.utils.JsonSerDe;
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
    generateUpdatePurchaseRequests();
    generateUpdateOrderForPurchaseResponses();
  }

  private void generateUpdatePurchaseRequests() {

    List<RequestResponseScenario> orderFiles = UpdateOrderDomainTestDataProducer.generateUpdateOrderRequests();

    orderFiles.stream().forEach(o -> {
      Purchase purchase = jsonPurchaseMapper.domainOrderToJsonPurchase(o.getOrder());
      writeToFile("updatepurchase", o.getName().replace("updateorder", "updatepurchase"), purchase, false);
    });

  }

  private void generateUpdateOrderForPurchaseResponses() {

    List<RequestResponseScenario> orderFiles = UpdateOrderDomainTestDataProducer.generateUpdateOrderForPurchaseResponses();

    orderFiles.stream().forEach(o -> {
      Purchase purchase = jsonPurchaseMapper.domainOrderToJsonPurchase(o.getOrder());
      writeToFile("updatepurchase", o.getName(), purchase, true);
    });

    List<RequestResponseScenario> errorFiles = UpdateOrderDomainTestDataProducer.generateUpdateOrderErrorForPurchaseErrorResponse();

    errorFiles.stream().forEach(o -> {
      Error error = jsonErrorMapper.domainErrorToJsonError(o.getError());
      writeToFile("updatepurchase", o.getName(), error, true);
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
