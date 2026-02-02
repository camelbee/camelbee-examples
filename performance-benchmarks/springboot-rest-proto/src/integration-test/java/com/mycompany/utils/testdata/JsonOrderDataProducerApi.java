package com.mycompany.utils.testdata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mycompany.mapper.api.JsonOrderMapper;
import com.mycompany.mapper.api.JsonOrderMapperImpl;
import com.mycompany.model.api.json.Order;
import com.mycompany.utils.JsonSerDe;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.SneakyThrows;

/**
 * Generates test Json messages for various order scenarios.
 */
public class JsonOrderDataProducerApi {

  JsonOrderMapper jsonOrderMapper = new JsonOrderMapperImpl();

  public static void main(String[] args) {
    new JsonOrderDataProducerApi().generateAllFiles();
  }

  public void generateAllFiles() {

  }

  @SneakyThrows
  public void writeToFile(String folder, String fileName, Order jsonObject) {

    JsonSerDe<Order> jsonSerDe = new JsonSerDe<>(new TypeReference<Order>() {
    });

    String fullFolderPath = "src/integration-test/resources/data/inttest/api/json/" + folder;

    Files.createDirectories(Paths.get(fullFolderPath));

    try (FileOutputStream outputStream = new FileOutputStream(fullFolderPath + "/" + fileName + ".json")) {
      outputStream.write(jsonSerDe.serialize(jsonObject).getBytes());
      System.out.printf("Binary data written to %s%n", fileName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @SneakyThrows
  public void writeToFile(String folder, String fileName, List<Order> jsonObject) {

    JsonSerDe<List<Order>> jsonSerDe = new JsonSerDe<>(new TypeReference<List<Order>>() {
    });

    String fullFolderPath = "src/integration-test/resources/data/inttest/api/json/" + folder;

    Files.createDirectories(Paths.get(fullFolderPath));

    try (FileOutputStream outputStream = new FileOutputStream(fullFolderPath + "/" + fileName + ".json")) {
      outputStream.write(jsonSerDe.serialize(jsonObject).getBytes());
      System.out.printf("Binary data written to %s%n", fileName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
