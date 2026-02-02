package com.mycompany.config;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IfBuildProperty(name = "test.mock.entitymanager", stringValue = "true")
public class TestEntityManagerFactoryProducer {

}
