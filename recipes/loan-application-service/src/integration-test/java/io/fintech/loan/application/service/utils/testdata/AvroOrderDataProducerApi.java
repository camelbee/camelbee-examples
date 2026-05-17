package io.fintech.loan.application.service.utils.testdata;

import io.fintech.loan.application.service.mapper.api.AvroOrderMapper;
import io.fintech.loan.application.service.mapper.api.AvroOrderMapperImpl;
import io.fintech.loan.application.service.model.api.avro.Order;
import io.fintech.loan.application.service.utils.AvroSerDe;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.avro.specific.SpecificRecordBase;

/**
 * Generates test Avro messages for various order scenarios.
 */
public class AvroOrderDataProducerApi {

  AvroOrderMapper avroOrderMapper = new AvroOrderMapperImpl();

  public static void main(String[] args) {
    new AvroOrderDataProducerApi().generateAllFiles();
  }

  public void generateAllFiles() {
    generateUpdateOrderRequests();

  }

  private void generateUpdateOrderRequests() {

    List<RequestResponseScenario> orderFiles = UpdateOrderDomainTestDataProducer.generateUpdateOrderRequests();

    orderFiles.stream().forEach(o -> {
      Order order = avroOrderMapper.domainToAvroOrder(o.getOrder());
      writeToFile("updateorder", o.getName(), order);
    });
  }

  @SneakyThrows
  public static <T extends SpecificRecordBase> void writeToFile(String folder, String fileName, T avroObject) {

    AvroSerDe<T> avroSerDe = new AvroSerDe<>((Class<T>) avroObject.getClass());

    String fullFolderPath = "src/integration-test/resources/data/inttest/api/avro/" + folder;

    Files.createDirectories(Paths.get(fullFolderPath));

    String filePath = fullFolderPath + "/" + fileName + ".avro";

    try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
      outputStream.write(avroSerDe.serialize(avroObject));
      System.out.printf("✓ Binary written to %s%n", filePath);
    } catch (Exception e) {
      System.err.printf("✗ Failed to write JSON: %s%n", e.getMessage());
      throw e;
    }

    filePath = fullFolderPath + "/" + fileName + ".avro.json";

    try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
      outputStream.write(avroSerDe.serializeToJson(avroObject).getBytes());
      System.out.printf("✓ JSON data written to %s%n", filePath);
    } catch (Exception e) {
      System.err.printf("✗ Failed to write binary: %s%n", e.getMessage());
      throw e;
    }

  }

}
