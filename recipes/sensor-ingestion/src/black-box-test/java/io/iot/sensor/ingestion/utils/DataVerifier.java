package io.iot.sensor.ingestion.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataVerifier implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(DataVerifier.class);

  private MongoClient mongoClient;

  private MongoClient getMongoClient() {
    if (mongoClient == null) {
      mongoClient = MongoClients.create("mongodb://root:example@localhost:27017/?authSource=admin");
    }
    return mongoClient;
  }

  public int countMongodbCollection(String collection) {
    try {
      MongoDatabase db = getMongoClient().getDatabase("camelbee");
      return (int) db.getCollection(collection).countDocuments();
    } catch (Exception e) {
      log.error("Failed to count MongoDB collection {}: {}", collection, e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public void clearMongodbCollection(String collection) {
    try {
      MongoDatabase db = getMongoClient().getDatabase("camelbee");
      db.getCollection(collection).deleteMany(new Document());
    } catch (Exception e) {
      log.error("Failed to clear MongoDB collection {}: {}", collection, e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      if (mongoClient != null)
        mongoClient.close();
    } catch (Exception e) {
      log.error("Failed to close MongoDB client: {}", e.getMessage());
    }
  }
}
