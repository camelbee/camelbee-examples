package io.fintech.loan.application.service.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

/**
 * AvroSerDe.
 *
 * @param <T> Class
 */
public class AvroSerDe<T> {

  private final Class<T> clazz;
  private final Schema schema;

  /**
   * Constructs an AvroSerDe instance for a specific Avro type.
   *
   * @param clazz The class type of the Avro object.
   */
  public AvroSerDe(Class<T> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("Class type cannot be null");
    }
    this.clazz = clazz;
    this.schema = getSchemaFromClass(clazz);
  }

  /**
   * Extracts the schema from an Avro SpecificRecord class.
   *
   * @param clazz The Avro class.
   * @return The schema for the class.
   */
  private Schema getSchemaFromClass(Class<T> clazz) {
    try {
      if (SpecificRecordBase.class.isAssignableFrom(clazz)) {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        return ((SpecificRecordBase) instance).getSchema();
      }
      // Fallback: try to get schema via static method
      return (Schema) clazz.getMethod("getClassSchema").invoke(null);
    } catch (Exception e) {
      throw new IllegalArgumentException("Unable to extract schema from class: " + clazz.getName(), e);
    }
  }

  /**
   * Serializes an Avro object to binary format.
   *
   * @param object The Avro object to serialize.
   * @return A byte array containing the serialized data.
   * @throws Exception If serialization fails.
   */
  public byte[] serialize(T object) throws Exception {
    if (object == null) {
      throw new IllegalArgumentException("Avro object to serialize cannot be null");
    }

    DatumWriter<T> writer = new SpecificDatumWriter<>(clazz);
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
      writer.write(object, encoder);
      encoder.flush();
      return outputStream.toByteArray();
    }
  }

  /**
   * Deserializes binary data into an Avro object.
   *
   * @param data The binary data to deserialize.
   * @return The deserialized Avro object.
   * @throws Exception If deserialization fails.
   */
  public T deserialize(byte[] data) throws Exception {
    if (data == null || data.length == 0) {
      throw new IllegalArgumentException("Avro data cannot be null or empty");
    }

    DatumReader<T> reader = new SpecificDatumReader<>(clazz);
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
      Decoder decoder = DecoderFactory.get().binaryDecoder(inputStream, null);
      return reader.read(null, decoder);
    }
  }

  /**
   * Serializes an Avro object to JSON format.
   *
   * @param object The Avro object to serialize.
   * @return A JSON string containing the serialized data.
   * @throws IllegalArgumentException if the input object is null.
   * @throws Exception                If JSON serialization fails.
   */
  public String serializeToJson(T object) throws Exception {
    if (object == null) {
      throw new IllegalArgumentException("Avro object to serialize cannot be null");
    }

    DatumWriter<T> writer = new SpecificDatumWriter<>(schema);
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Encoder encoder = EncoderFactory.get().jsonEncoder(schema, outputStream);
      writer.write(object, encoder);
      encoder.flush();
      return outputStream.toString(StandardCharsets.UTF_8.name());
    }
  }

  /**
   * Deserializes JSON data into an Avro object.
   *
   * @param json The JSON string to deserialize.
   * @return The deserialized Avro object.
   * @throws IllegalArgumentException if the input JSON is null or empty.
   * @throws Exception                If JSON deserialization fails.
   */
  public T deserializeFromJson(String json) throws Exception {
    if (json == null || json.isEmpty()) {
      throw new IllegalArgumentException("JSON string cannot be null or empty");
    }

    DatumReader<T> reader = new SpecificDatumReader<>(schema);
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
      Decoder decoder = DecoderFactory.get().jsonDecoder(schema, inputStream);
      return reader.read(null, decoder);
    }
  }
}