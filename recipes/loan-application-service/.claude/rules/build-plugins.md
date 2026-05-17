---
paths:
  - "pom.xml"
---
# Build Plugins & Code Generation

This file documents all Maven plugins that generate, transform, or post-process code in this CamelBee microservice. **Do NOT modify plugin versions or configurations unless explicitly asked.**

---

## Code Generation Pipeline

All code generation happens during `./mvnw generate-sources`. The pipeline:

```
Spec files (YAML/XSD/proto/avsc/WSDL)
    |
    v
Maven Plugins (generate-sources phase)
    |
    v
target/generated-sources/{format}/  (Java classes)
    |
    v
Post-processing (annotation injection, descriptor copy, cleanup)
    |
    v
Ready for compilation
```

---

## Plugin Reference

### 1. OpenAPI Generator (`org.openapitools:openapi-generator-maven-plugin`)

Generates Java model classes (POJOs) from OpenAPI YAML specs.

| Execution | Input | Output Package | When |
|-----------|-------|----------------|------|
| REST/MCP API models | `src/main/resources/openapi/order-api.yaml` | `io.fintech.loan.application.service.model.api.json` | REST, MCP, WebSocket, or JSON formats |
| MCP API models | `src/main/resources/mcp/order-mcp-api.yaml` | `io.fintech.loan.application.service.model.api.mcp` | MCP interface |
| REST backend models | `src/main/resources/backends/openapi/purchase-api.yaml` | `io.fintech.loan.application.service.model.infra.json` | REST backend |

**Key config:**
- Generator: `spring` with `useSpringBoot3=true`
- Models only: `generateApis=false`, `generateSupportingFiles=false`
- Adds Jakarta validation annotations (`useBeanValidation=true`)
- Output: `target/generated-sources/openapi/`

**When to touch:** Modify the YAML spec, then `./mvnw generate-sources`. Never edit the generated Java files.

---

### 5. Avro Maven Plugin (`org.apache.avro:avro-maven-plugin`)

Generates Java classes from Avro schema (`.avsc`) files.

| Execution | Input | Output |
|-----------|-------|--------|
| API Avro models | `src/main/resources/avro/` | `target/generated-sources/avro/` (package `io.fintech.loan.application.service.model.api.avro`) |
| Backend Avro models | `src/main/resources/backends/avro/` | `target/generated-sources/avro/` (package `io.fintech.loan.application.service.model.infra.avro`) |

**Key config:** Goal: `schema`. Both executions write to the same output directory.

---

## MapStruct (Annotation Processor, not a Plugin)

MapStruct is configured as a **compiler annotation processor**, not a build plugin. It processes `@Mapper` annotations during compilation.

- Config class: `config/SharedMapperConfig.java`
- Component model: `spring` (generates Spring `@Component` implementations)
- Generated mapper implementations appear in `target/generated-sources/annotations/`
- Null mapping strategy: `NullValuePropertyMappingStrategy.IGNORE` (partial updates work correctly)

---

## Adding a New Spec File

When adding a new data model:

1. **Create the spec file** in the appropriate directory (see table above)
2. **Add a plugin execution** in `pom.xml` if the existing executions don't cover the new source directory
3. **Run** `./mvnw generate-sources` to verify generation works
4. **Never edit** the generated Java files — always edit the spec

## Common Mistakes

- Editing generated Java files instead of the spec → changes will be overwritten on next `generate-sources`
- Adding new proto/avro/xsd files without adding a plugin execution → models won't be generated
- Changing plugin versions → may break generation; these versions are tested by CamelBee
