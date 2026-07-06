---
description: Adds a new producer backend (outgoing call) to this microservice by generating a reference project via the CamelBee MCP server and copying the relevant pieces.
---

# Add Backend: $ARGUMENTS

You are adding a new **producer backend** (outgoing call) to this CamelBee microservice.

## Important

Adding a new backend is complex — it involves producer routes, infra models, mappers, pom.xml plugins, Docker services, TestContainers config, test data producers, WireMock stubs, init scripts, and more. **Do NOT try to build it from scratch.** Instead, use the CamelBee MCP server to generate a reference project that includes the new technology, then copy the relevant pieces.

## Step 1: Generate a Reference Project via CamelBee MCP Server

Use the CamelBee MCP server (registered in `.mcp.json` — requires a paid subscription and API key from [camelbee.io](http://www.camelbee.io)) to generate a reference project. Call the MCP server with parameters that include BOTH the existing backends AND the new one you want to add. The MCP server will generate a complete microservice with the new technology included — copy it to a temporary folder (e.g., `/tmp/reference-project/`) for inspection.

**Key steps:**
1. Call the CamelBee MCP server to generate a project with the same framework, service name, existing backends + the new backend, and same interfaces
2. Save the generated project to a temporary directory
3. Inspect the generated code to understand what the new backend requires: producer routes, infra models, mappers, central route wiring, config, Docker services, tests

## Step 2: Identify What's New

Compare the reference project with the current project to find all files related to the new backend:

```bash
diff -rq reference-project/ . --exclude=target --exclude=.git --exclude=.claude
```

## Step 3: Copy from Reference Project

Use the checklist below to copy all pieces from the reference project into the current project. **Copy exactly as generated — do not modify the patterns.**

### 3a. Maven Dependencies & Plugins
- [ ] Compare `pom.xml` — add any new dependencies for the backend technology
- [ ] Add any new code generation plugin executions (Protobuf, Avro, CXF, Maven Replacer for new XML classes)
- [ ] Run `./mvnw generate-sources` to verify generation works

### 3b. Producer Route
- [ ] Copy `routes/producer/{technology}/` from the reference project
- [ ] Verify it has `camelBeeRouteConfigurer.configureRoute(this)` first
- [ ] Verify it has `errorHandler(noErrorHandler())`
- [ ] Verify `.routeId()` on every route

### 3c. Infrastructure Models & Mappers
- [ ] Copy new models in `model/infra/{technology}/`
- [ ] Copy new mapper in `mapper/infra/{Technology}PurchaseMapper.java`
- [ ] Copy mapper test in `src/test/java/**/mapper/infra/`

### 3d. Wire into Central Routes
- [ ] Compare central routes — add `.to("direct:{operation}{Technology}").id("{operation}{Technology}Endpoint")` for the new backend
- [ ] The `.id()` is critical — it enables AdviceWith mocking in unit tests

### 3e. Configuration
- [ ] Merge new properties from reference `application.yml` into current `application.yml`
- [ ] Add Docker service in `docker-compose.yml`
- [ ] Add Docker service in `src/integration-test/resources/compose-backends.yml`
- [ ] Add Docker service in `src/black-box-test/resources/compose-blackbox.yml`
- [ ] Copy any Docker init scripts from `src/integration-test/resources/docker/{technology}/`
- [ ] Copy any WireMock stubs from `src/integration-test/resources/docker/wiremock/` (for REST/SOAP/gRPC backends)

### 3f. TestContainers & Test Infrastructure
- [ ] Compare `TestContainerConfiguration.java` — add new container setup
- [ ] Compare `IntegrationTest.java` base class — add new mock capture endpoints (`weaveAddLast` with `mock:capture{Operation}{Technology}`)
- [ ] Copy test data producer from `src/integration-test/java/**/utils/testdata/`
- [ ] Copy test data files from `src/integration-test/resources/data/inttest/infra/{format}/`

### 3g. Unit Tests
- [ ] Compare `Central*RouteUnitTest.java` — add `weaveById("{operation}{Technology}Endpoint")` mocks for the new backend
- [ ] Copy mapper test

### 3h. Integration Tests
- [ ] Compare existing integration test files — add backend data verification assertions
- [ ] For databases: verify data with `FluentProducerTemplate` queries
- [ ] For messaging: verify with `consumerTemplate.receive()`
- [ ] For REST/SOAP/gRPC: verify with WireMock `verify()`

### 3i. Black-box Tests
- [ ] Compare black-box test files — add backend verification using data verifiers
- [ ] Compare black-box `TestContainerConfiguration.java` for new service setup

### 3j. Adapt to Current Domain
- [ ] Replace the reference Order/Purchase domain references with the current project's domain
- [ ] Update mapper method names and model types to match

## Step 4: Verify

- [ ] `./mvnw test` — all unit tests pass
- [ ] `./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify` — all integration tests pass
- [ ] `./mvnw verify -Pblack-box-test` — all black-box tests pass

## Step 5: Clean Up

- [ ] Delete the reference project — it was only needed as a guide