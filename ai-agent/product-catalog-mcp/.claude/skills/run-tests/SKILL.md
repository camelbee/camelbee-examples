---
description: Runs tests at the specified level (unit, integration, black-box, load, or all)
argument-hint: [unit|integration|blackbox|load|all]
---

# Run Tests: $ARGUMENTS

Run the requested test level. Default is `all` if no argument provided.

## Test Levels

### Unit Tests
```bash
./mvnw test
```
Tests mapper logic and route behavior with mocked backends (AdviceWith). Fast, no Docker required.

### Integration Tests
```bash
./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify
```
Tests routes with real backends via TestContainers. Requires Docker running.

### Black-box Tests
```bash
./mvnw package -DskipTests
./mvnw verify -Pblack-box-test
```
Tests the fully dockerized application via protocol calls. Requires Docker running.

### Load Tests (k6)
```bash
# Requires k6 installed and the app running (docker compose up)
k6 run docs/k6/rest/rest-throughput-test.js
k6 run docs/k6/grpc/grpc-throughput-test.js
k6 run docs/k6/graphql/graphql-throughput-test.js
k6 run docs/k6/soap/soap-throughput-test.js
k6 run docs/k6/websocket/websocket-throughput-test.js
k6 run docs/k6/mcp/mcp-throughput-test.js
```
Performance/load tests using k6. Requires the app to be running and k6 installed.

### All Tests
```bash
./mvnw test && ./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify
```

## Instructions

1. Run the appropriate command based on the argument
2. If tests fail, analyze the output and report:
   - Which tests failed
   - The root cause
   - Suggested fix
3. If all tests pass, report the summary (tests run, passed, skipped)