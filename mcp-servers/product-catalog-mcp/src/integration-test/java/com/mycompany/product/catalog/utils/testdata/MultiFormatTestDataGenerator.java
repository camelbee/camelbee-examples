package com.mycompany.product.catalog.utils.testdata;

public class MultiFormatTestDataGenerator {

  public static void main(String[] args) {

    new JsonPurchaseDataProducerInfra().generateAllFiles();

    new McpOrderDataProducerApi().generateAllFiles();

    System.out.println("created and places all test data!");
  }

}
