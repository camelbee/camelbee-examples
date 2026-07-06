package io.iot.sensor.ingestion.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MongoDbIndexInitializer {

  private static final Logger log = LoggerFactory.getLogger(MongoDbIndexInitializer.class);

  @Inject
  MongoClient mongoClient;

  @ConfigProperty(name = "camelbeeservice.mongodb.database")
  String databaseName;

  @ConfigProperty(name = "camelbeeservice.mongodb.sensorreadings-collection")
  String collectionName;

  public void init(@Observes io.quarkus.runtime.StartupEvent event) {
    try {
      MongoDatabase db = mongoClient.getDatabase(databaseName);
      MongoCollection<?> collection = db.getCollection(collectionName);

      collection.createIndex(
          Indexes.ascending("deviceId", "recordedAt"),
          new IndexOptions().name("idx_deviceId_recordedAt")
      );

      collection.createIndex(
          Indexes.ascending("recordedAt"),
          new IndexOptions()
              .name("idx_recordedAt_ttl")
              .expireAfter(30L, TimeUnit.DAYS)
      );

      log.info("MongoDB indexes created/verified for collection: {}", collectionName);
    } catch (Exception e) {
      log.warn("Failed to create MongoDB indexes (may already exist): {}", e.getMessage());
    }
  }
}
