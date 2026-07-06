package io.iot.sensor.ingestion.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSeeder implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  public void resetData() {
    log.info("Resetting and seeding test data...");
    resetMongodbData();
    log.info("Test data seeding completed.");
  }

  private MongoClient mongoSeederClient;

  private MongoClient getMongoSeederClient() {
    if (mongoSeederClient == null) {
      mongoSeederClient = MongoClients.create("mongodb://root:example@localhost:27017/?authSource=admin");
    }
    return mongoSeederClient;
  }

  private void resetMongodbData() {
    log.info("Resetting MongoDB backend data...");
    try {
      MongoDatabase db = getMongoSeederClient().getDatabase("camelbee");
      MongoCollection<Document> collection = db.getCollection("sensorReadings");
      collection.deleteMany(new Document());

      String json = readResource("/backend/mongodb/reset-mongodb.json");
      List<Document> docs = Document.parse("{\"d\":" + json + "}").getList("d", Document.class);
      for (Document doc : docs) {
        collection.insertOne(doc);
      }
      log.info("MongoDB data reset completed. Inserted {} documents.", docs.size());
    } catch (Exception e) {
      log.error("Failed to reset MongoDB data", e);
      throw new RuntimeException("Failed to reset MongoDB data", e);
    }
  }

  private String readResource(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null) {
        throw new RuntimeException("Resource not found: " + path);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read resource: " + path, e);
    }
  }

  @Override
  public void close() {
    try {
      if (mongoSeederClient != null)
        mongoSeederClient.close();
    } catch (Exception e) {
      log.error("Failed to close MongoDB seeder client: {}", e.getMessage());
    }
  }
}
