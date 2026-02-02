package com.mycompany.utils;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.util.JsonFormat;

/**
 * ProtobufSerDe.
 *
 * @param <T> Class
 */
public class ProtobufSerDe<T extends Message> {

  private final Parser<T> parser;
  private final JsonFormat.Printer jsonPrinter;
  private final JsonFormat.Parser jsonParser;

  /**
   * Constructs a ProtobufSerDe instance for a specific Protobuf type.
   * By default, ignores unknown fields during JSON deserialization.
   *
   * @param parser The Protobuf parser for the target type.
   */
  public ProtobufSerDe(Parser<T> parser) {
    this(parser, true);
  }

  /**
   * Constructs a ProtobufSerDe instance for a specific Protobuf type.
   *
   * @param parser              The Protobuf parser for the target type.
   * @param ignoreUnknownFields Whether to ignore unknown fields during JSON deserialization.
   */
  public ProtobufSerDe(Parser<T> parser, boolean ignoreUnknownFields) {
    if (parser == null) {
      throw new IllegalArgumentException("Protobuf parser cannot be null");
    }
    this.parser = parser;
    this.jsonPrinter = JsonFormat.printer();
    this.jsonParser = ignoreUnknownFields
        ? JsonFormat.parser().ignoringUnknownFields()
        : JsonFormat.parser();
  }

  /**
   * Constructs a ProtobufSerDe instance with custom JSON formatting options.
   *
   * @param parser      The Protobuf parser for the target type.
   * @param jsonPrinter Custom JSON printer for serialization.
   * @param jsonParser  Custom JSON parser for deserialization.
   */
  public ProtobufSerDe(Parser<T> parser, JsonFormat.Printer jsonPrinter, JsonFormat.Parser jsonParser) {
    if (parser == null) {
      throw new IllegalArgumentException("Protobuf parser cannot be null");
    }
    this.parser = parser;
    this.jsonPrinter = jsonPrinter != null ? jsonPrinter : JsonFormat.printer();
    this.jsonParser = jsonParser != null ? jsonParser : JsonFormat.parser();
  }

  /**
   * Serializes a Protobuf object to binary format.
   *
   * @param message The Protobuf object to serialize.
   * @return A byte array containing the serialized data.
   * @throws IllegalArgumentException if the input message is null.
   */
  public byte[] serialize(T message) {
    if (message == null) {
      throw new IllegalArgumentException("Protobuf message to serialize cannot be null");
    }
    return message.toByteArray();
  }

  /**
   * Deserializes binary data into a Protobuf object.
   *
   * @param data The binary data to deserialize.
   * @return The deserialized Protobuf object.
   * @throws Exception If deserialization fails.
   */
  public T deserialize(byte[] data) throws Exception {
    return parser.parseFrom(data);
  }

  /**
   * Serializes a Protobuf object to JSON format.
   *
   * @param message The Protobuf object to serialize.
   * @return A JSON string containing the serialized data.
   * @throws IllegalArgumentException if the input message is null.
   * @throws Exception                If JSON serialization fails.
   */
  public String serializeToJson(T message) throws Exception {
    if (message == null) {
      throw new IllegalArgumentException("Protobuf message to serialize cannot be null");
    }
    return jsonPrinter.print(message);
  }

  /**
   * Deserializes JSON data into a Protobuf object.
   *
   * @param json The JSON string to deserialize.
   * @return The deserialized Protobuf object.
   * @throws IllegalArgumentException if the input JSON is null or empty.
   * @throws Exception                If JSON deserialization fails.
   */
  public T deserializeFromJson(String json) throws Exception {
    if (json == null || json.isEmpty()) {
      throw new IllegalArgumentException("JSON string cannot be null or empty");
    }
    Message.Builder builder = parser.parseFrom(new byte[0]).toBuilder();
    jsonParser.merge(json, builder);
    @SuppressWarnings("unchecked")
    T result = (T) builder.build();
    return result;
  }
}