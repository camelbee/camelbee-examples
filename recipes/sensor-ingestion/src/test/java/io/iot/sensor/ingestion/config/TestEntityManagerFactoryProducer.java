package io.iot.sensor.ingestion.config;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IfBuildProperty(name = "test.mock.entitymanager", stringValue = "true")
public class TestEntityManagerFactoryProducer {

}
