package io.fintech.loan.application.service.utils.testdata;

public class MultiFormatTestDataGenerator {

  public static void main(String[] args) {

    new AvroOrderDataProducerApi().generateAllFiles();
    new AvroPurchaseDataProducerInfra().generateAllFiles();

    new JsonOrderDataProducerApi().generateAllFiles();
    new JsonPurchaseDataProducerInfra().generateAllFiles();

    new McpOrderDataProducerApi().generateAllFiles();

    System.out.println("created and places all test data!");
  }

}
