---
description: Triggers a request to the running microservice so the developer can verify message flow visually in the CamelBee debugger UI. Suggest this after all tests pass.
argument-hint: [interface] e.g. REST, KAFKA, MONGODB
---

# CamelBee Debug: $ARGUMENTS

Trigger a request to the running microservice and let the developer observe the message flow in the CamelBee debugger UI.

## Prerequisites

Start the backends and the application before triggering:

```bash
# 1. Start all backend services
docker compose -f src/integration-test/resources/compose-backends.yml up -d

# 2. Run the application
./mvnw quarkus:dev
# Or with Docker Compose: docker compose up --build
```

3. Open the CamelBee debugger UI in a browser: `http://localhost:8080/camelbee/index.html`

## CamelBee Debugger UI Features

- **Route topology** — interactive graph showing all routes and their connections
- **Message tracing** — see messages flow through routes with animated visualization
- **Request/response inspection** — headers + body at every processing step
- **Health & metrics** — JVM memory, CPU, GC, thread counts, route exchange counts
- **Built-in trigger** — fire requests directly from the UI to test routes

## Configuration (in `application.yml`)

```yaml
camelbee:
  notifier-enabled: true       # Event notification
  tracer-enabled: true          # Message tracing
  tracer-max-idle-time: 60000   # Idle timeout (ms)
  tracer-max-messages-count: 10000  # Message buffer size
```

## Trigger Commands by Interface

Use the CLI tools already available in the Docker containers to trigger a request. The developer can watch the route topology light up in the debugger UI.

| Interface | How to trigger |
|-----------|---------------|
| MCP | Trigger via MCP client or CamelBee debugger UI built-in trigger |

## Instructions

1. If the argument specifies an interface (e.g., `REST`), use the corresponding trigger command from the table above
2. If no argument is given, use the first available HTTP-based interface (REST > GraphQL > SOAP > gRPC)
3. Run the trigger command and show the output to the developer
4. Remind the developer to check the CamelBee debugger UI at `http://localhost:8080/camelbee/index.html` to see the message flow
5. For non-HTTP interfaces (Kafka, MQTT, databases), explain that the developer should watch the debugger UI while you execute the command, as the consumer will poll and process the message