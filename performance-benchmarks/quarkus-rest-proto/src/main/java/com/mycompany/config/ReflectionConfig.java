package com.mycompany.config;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * ReflectionConfig.
 */
@RegisterForReflection(classNames = {

    // Google Type Date
    "com.google.type.Date",
    "com.google.type.Date$Builder",
    "com.google.type.DateOrBuilder",

    // Google Protobuf Common Types
    "com.google.protobuf.Timestamp",
    "com.google.protobuf.Timestamp$Builder",
    "com.google.protobuf.TimestampOrBuilder",

    // Add other gRPC message types you have
    // "com.mycompany.order.grpc.UpdateOrderRequest",
    // "com.mycompany.order.grpc.UpdateOrderRequest$Builder",
    // "com.mycompany.order.grpc.UpdateOrderRequestOrBuilder",

    // Protobuf base classes
    "com.google.protobuf.GeneratedMessageV3",
    "com.google.protobuf.GeneratedMessageV3$Builder",
    "com.google.protobuf.GeneratedMessageV3$FieldAccessorTable",
    "com.google.protobuf.GeneratedMessageV3$FieldAccessorTable$SingularMessageFieldAccessor",
    "com.google.protobuf.AbstractMessage",
    "com.google.protobuf.AbstractMessage$Builder"

})
public class ReflectionConfig {
  // TODO add all the possible combinations!!!! 14.04.2025
}