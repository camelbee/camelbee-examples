package com.mycompany;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * MyCamelbeeService microservice to handle MyCamelbeeService related functionality.
 *
 */
@SpringBootApplication
@ComponentScan(
    basePackages = {"org.camelbee", "com.mycompany"})
public class MyCamelbeeServiceApplication {

  /**
   * A main method to start this application.
   */
  public static void main(String[] args) {
    SpringApplication.run(MyCamelbeeServiceApplication.class, args);
  }

}
