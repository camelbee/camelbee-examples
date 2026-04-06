package com.mycompany.catalog.mcp.config;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * ReflectionConfig - registers model classes for Quarkus native compilation.
 */
@RegisterForReflection(classNames = {

    // MCP API Models (generated from OpenAPI)
    "com.mycompany.catalog.mcp.model.api.mcp.Product",
    "com.mycompany.catalog.mcp.model.api.mcp.ProductPage",
    "com.mycompany.catalog.mcp.model.api.mcp.ProductSearchCriteria",

    // REST Backend Models (generated from OpenAPI)
    "com.mycompany.catalog.mcp.model.infra.json.Product",
    "com.mycompany.catalog.mcp.model.infra.json.Error",

    // Domain Models
    "com.mycompany.catalog.mcp.model.domain.Product",
    "com.mycompany.catalog.mcp.model.domain.ProductPage",
    "com.mycompany.catalog.mcp.model.domain.ProductSearchCriteria",
    "com.mycompany.catalog.mcp.model.domain.AuditLog",
    "com.mycompany.catalog.mcp.model.domain.AuditLog$ResponseStatus",

    // JPA Entities
    "com.mycompany.catalog.mcp.model.infra.jpa.postgresql.AuditLogEntity",
    "com.mycompany.catalog.mcp.model.infra.jpa.postgresql.AuditLogEntity$ResponseStatus",

})
public class ReflectionConfig {
}
