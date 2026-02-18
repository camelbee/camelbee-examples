package com.mycompany.utils.testdata;

public class MultiFormatTestDataGenerator {

  public static void main(String[] args) {

    new McpOrderDataProducerApi().generateAllFiles();

    System.out.println("created and places all test data!");
  }

}
