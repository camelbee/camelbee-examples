---
description: Checks whether the microservice and backend containers are running; if not, builds with ./mvnw clean package -DskipTests and starts with docker compose up --build; then triggers a request and opens the CamelBee debugger UI.
---

# CamelBee Debug: $ARGUMENTS

Trigger a request to the running microservice and let the developer observe the message flow in the CamelBee debugger UI.

## Step 1 — Check whether the stack is already running

Run the following command to see which containers are currently up:

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

Look for:
- The **microservice container** (usually named after `camelbeeService` or the `artifactId`)
- All **backend containers** referenced in `docker-compose.yml` (databases, brokers, localstack, wiremock, etc.)

### If ALL required containers are running and healthy → skip to Step 3

### If any container is missing, stopped, or unhealthy → proceed to Step 2

## Step 2 — Build and start the stack

First, build the application JAR (skip tests for speed):

```bash
./mvnw clean package -DskipTests
```

Wait for the build to succeed. If the build fails, report the Maven error to the developer and stop.

Then start all services with Docker Compose:

```bash
docker compose up --build -d
```

> **How the stack is composed:** the root `docker-compose.yml` starts the microservice container and `include`s `src/integration-test/resources/compose-backends.yml` — the **same** backend services (and credentials/ports) that the integration-test and black-box-test `DockerComposeContainer` boots. Never start backends by any other mechanism; one compose definition is shared by runtime debugging and all test levels.
>
> **Alternative for hot reload / IDE debugging:** start only the backends with `docker compose -f src/integration-test/resources/compose-backends.yml up -d`, then run the app on the host with `./mvnw quarkus:dev` — `application.yml` defaults already point at the published `localhost` ports. The debugger UI is at the same URL either way.

After starting, wait ~15 seconds and then verify the containers came up healthy:

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

If any container shows `unhealthy` or `Exited`, immediately fetch its logs and report the error to the developer:

```bash
docker logs <container-name> 2>&1 | tail -60
```

Do NOT proceed to Step 3 until the microservice container is running and healthy.

## Step 3 — Confirm the debugger UI is reachable

The CamelBee debugger UI is available at: `http://localhost:8080/camelbee/index.html`

Tell the developer to open that URL in a browser now that the service is running.

> **Important:** To see messages flowing through the routes, the developer must click the **"Start Tracing"** button in the UI before triggering a request. Without it, the route topology is visible but no message details will be captured or animated.

> **If message bodies show as `ClassName@abc123`:** The domain or infra model class is missing a `toString()` implementation. Generated models use Lombok — add `@ToString` (or `@ToString(exclude = "fieldName")` for classes with bidirectional JPA relationships) to the affected class and rebuild.

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
| REST | `curl -s http://localhost:8080/camelbeeService/v1/orders?salesChannel=ONLINE` |
| MQTT | `docker compose exec mqtt mosquitto_pub -t 'camelbeeService/northbound/createorder/json' -f test-data.json` |

## Step 4 — Trigger a request

1. If the argument specifies an interface (e.g., `REST`), use the corresponding trigger command from the table above.
2. If no argument is given, use the first available HTTP-based interface (REST > GraphQL > SOAP > gRPC).
3. Run the trigger command and show the output to the developer.
4. For non-HTTP interfaces (Kafka, MQTT, databases), tell the developer to watch the debugger UI while you execute the command — the consumer will poll and process the message shortly.
5. Remind the developer to observe the route topology lighting up at `http://localhost:8080/camelbee/index.html`.