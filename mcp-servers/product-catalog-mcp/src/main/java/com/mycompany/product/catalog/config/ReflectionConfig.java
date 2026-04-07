package com.mycompany.product.catalog.config;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * ReflectionConfig for Quarkus native build.
 */
@RegisterForReflection(classNames = {
    "com.mycompany.product.catalog.model.domain.Product",
    "com.mycompany.product.catalog.model.domain.PaginatedResponse",
    "com.mycompany.product.catalog.model.domain.ProductSearchCriteria",
    "com.mycompany.product.catalog.model.domain.AuditLog",
    "com.mycompany.product.catalog.model.domain.ResponseStatus",
    "com.mycompany.product.catalog.model.domain.Error",
    "com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog",
    "com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog$ResponseStatusEnum",
})
public class ReflectionConfig {
}
