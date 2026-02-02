package com.mycompany.itest;

import io.quarkus.test.junit.QuarkusTestProfile;

public class IntegrationTestProfile implements QuarkusTestProfile {

  @Override
  public String getConfigProfile() {
    return "itest";
  }

}
