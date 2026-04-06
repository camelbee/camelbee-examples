package com.mycompany.catalog.mcp.itest;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Collections;
import java.util.List;

public class IntegrationTestProfile implements QuarkusTestProfile {

  @Override
  public String getConfigProfile() {
    return "itest";
  }

  @Override
  public List<TestResourceEntry> testResources() {
    return Collections.singletonList(new TestResourceEntry(TestContainerConfiguration.class));
  }
}