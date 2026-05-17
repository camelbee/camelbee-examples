
package io.fintech.loan.application.service.utils.testdata;

import io.fintech.loan.application.service.mapper.infra.AvroInfraErrorMapper;
import io.fintech.loan.application.service.mapper.infra.AvroInfraErrorMapperImpl;
import io.fintech.loan.application.service.mapper.infra.AvroPurchaseMapper;
import io.fintech.loan.application.service.mapper.infra.AvroPurchaseMapperImpl;
import io.fintech.loan.application.service.utils.AvroSerDe;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.apache.avro.specific.SpecificRecordBase;

/**
 * Generates test Avro messages for various order scenarios.
 */
public class AvroPurchaseDataProducerInfra {

  AvroPurchaseMapper avroPurchaseMapper = new AvroPurchaseMapperImpl();
  AvroInfraErrorMapper avroErrorMapper = new AvroInfraErrorMapperImpl();

  public static void main(String[] args) {
    new AvroPurchaseDataProducerInfra().generateAllFiles();
  }

  public void generateAllFiles() {
  }

  @SneakyThrows
  public static <T extends SpecificRecordBase> void writeToFile(String folder, String fileName, T avroObject, boolean copyToWiremock) {

    AvroSerDe<T> avroSerDe = new AvroSerDe<>((Class<T>) avroObject.getClass());

    String fullFolderPath = "src/integration-test/resources/data/inttest/infra/avro/" + folder;

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

    if (copyToWiremock) {
      String wiremocFolderPath = "src/integration-test/resources/docker/wiremock/__files/rest/avro/" + folder;

      FileUtils.copyFile(fullFolderPath, fileName + ".avro", wiremocFolderPath, fileName + ".avro");
      FileUtils.copyFile(fullFolderPath, fileName + ".avro.json", wiremocFolderPath, fileName + ".avro.json");
    }
  }

}
