package com.mycompany;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * myservice microservice to handle myservice related functionality.
 *
 */
@SpringBootApplication
@ComponentScan(
    basePackages = {"org.camelbee", "com.mycompany"})
public class MyserviceApplication {

  /**
   * A main method to start this application.
   */
  public static void main(String[] args) {
    SpringApplication.run(MyserviceApplication.class, args);
  }

}
