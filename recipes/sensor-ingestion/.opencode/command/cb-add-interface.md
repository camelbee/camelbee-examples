---
description: Adds a new consumer interface (entry point) to this microservice by generating a reference project via the CamelBee MCP server and copying the relevant pieces.
---

# Add Interface: $ARGUMENTS

You are adding a new **consumer interface** (entry point) to this CamelBee microservice.

## Important

Adding a new interface is complex — it involves routes, models, mappers, pom.xml plugins, Docker services, TestContainers config, test data producers, and more. **Do NOT try to build it from scratch.** Instead, use the CamelBee MCP server to generate a reference project that includes the new technology, then copy the relevant pieces.

## Step 1: Generate a Reference Project via CamelBee MCP Server

Use the CamelBee MCP server (registered in `.mcp.json` — requires a paid subscription and API key from [camelbee.io](http://www.camelbee.io)) to generate a reference project. Call the MCP server with parameters that include BOTH the existing interfaces AND the new one you want to add. The MCP server will generate a complete microservice with the new technology included — copy it to a temporary folder (e.g., `/tmp/reference-project/`) for inspection.

**Key steps:**
1. Call the CamelBee MCP server to generate a project with the same framework, service name, existing interfaces + the new interface, and same backends
2. Save the generated project to a temporary directory
3. Inspect the generated code to understand what the new interface requires: consumer routes, models, mappers, config, Docker services, tests

## Step 2: Identify What's New

Compare the reference project with the current project to find all files related to the new interface:

```bash
diff -rq reference-project/ . --exclude=target --exclude=.git --exclude=.claude
```

## Step 3: Copy from Reference Project

Use the checklist below to copy all pieces from the reference project into the current project. **Copy exactly as generated — do not modify the patterns.**

### 3a. Maven Dependencies & Plugins
- [ ] Compare `pom.xml` — add any new dependencies for the interface technology
- [ ] Add any new code generation plugin executions (OpenAPI, Protobuf, Avro, XJC, CXF, Maven Replacer)
- [ ] Run `./mvnw generate-sources` to verify generation works

### 3b. Consumer Route
- [ ] Copy `routes/consumer/{technology}/` from the reference project
- [ ] Verify it has `camelBeeRouteConfigurer.configureRoute(this)` first
- [ ] Verify it has `errorHandler(genericExceptionHandler.appErrorHandler())`
- [ ] Verify `.routeId()` on every route

### 3c. API Models & Mappers
- [ ] Copy new models in `model/api/{format}/` (or spec files for generated models)
- [ ] Copy new mapper in `mapper/api/{Format}OrderMapper.java`
- [ ] Copy mapper test in `src/test/java/**/mapper/api/`

### 3d. Configuration
- [ ] Merge new properties from reference `application.yml` into current `application.yml`
- [ ] Add Docker service in `docker-compose.yml`
- [ ] Add Docker service in `src/integration-test/resources/compose-backends.yml`
- [ ] Add Docker service in `src/black-box-test/resources/compose-blackbox.yml`
- [ ] Copy any Docker init scripts from `src/integration-test/resources/docker/{technology}/`

### 3e. TestContainers
- [ ] Compare `TestContainerConfiguration.java` — add new container setup for the interface
- [ ] Compare `IntegrationTest.java` base class — add new mock capture endpoints and test infrastructure

### 3f. Integration Tests
- [ ] Copy test class from `src/integration-test/java/**/itest/{technology}/`
- [ ] Copy test data files from `src/integration-test/resources/data/`
- [ ] Copy test data producer from `src/integration-test/java/**/utils/testdata/`

### 3g. Black-box Tests
- [ ] Copy test class from `src/black-box-test/java/**/bbtest/{technology}/`
- [ ] Compare black-box `TestContainerConfiguration.java` for new service setup

### 3h. Adapt to Current Domain
- [ ] Replace the reference Order/Purchase domain references with the current project's domain
- [ ] Update mapper method names and model types to match

## Step 4: Verify

- [ ] `./mvnw test` — all unit tests pass
- [ ] `./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify` — all integration tests pass
- [ ] `./mvnw verify -Pblack-box-test` — all black-box tests pass

## Step 5: Clean Up

- [ ] Delete the reference project — it was only needed as a guide