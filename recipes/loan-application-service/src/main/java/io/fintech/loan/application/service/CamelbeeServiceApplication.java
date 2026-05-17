package io.fintech.loan.application.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * camelbeeService microservice to handle camelbeeService related functionality.
 *
 */
@SpringBootApplication(exclude = {JpaRepositoriesAutoConfiguration.class})
@ComponentScan(
    basePackages = {"org.camelbee", "io.fintech.loan.application.service"})
public class CamelbeeServiceApplication {

  /**
   * A main method to start this application.
   */
  public static void main(String[] args) {
    SpringApplication.run(CamelbeeServiceApplication.class, args);
  }

}
