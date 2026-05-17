# loan-application-service

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.0.0--SNAPSHOT-blue)
![License](https://img.shields.io/badge/license-Apache%202.0-green)

Apache Camel + Spring Boot loan-application processing microservice generated
by the [CamelBee MCP Server](https://www.camelbee.io/mcp) and transformed
from the Order/Purchase reference scaffold into a real Loan Application
Processing domain.

---

## What it does

Accepts a loan application via REST or MCP, persists it as `RECEIVED`,
publishes a `LoanApplicationSubmittedEvent` to Kafka, then asynchronously
processes it through three possible paths:

1. **Auto-approve** — `requestedAmount <= 5000` AND `creditScore >= 700`
2. **Auto-reject** — `creditScore < 500`
3. **Credit Bureau assessment** — everything else; calls the credit-bureau
   REST backend; result is either APPROVED or PENDING_REVIEW depending on
   the bureau response

In all paths the final state is saved to JPA, mirrored into Redis (TTL 1
hour), and emitted as a `LoanApplicationProcessedEvent` to Kafka.

`GET /loan-applications/{applicationId}` uses **cache-aside**: Redis is
checked first; on miss the JPA row is read and the cache is warmed.

---

## Three-layer route architecture

```
REST  / MCP  /  Kafka(submitted)        (Consumer)
        │
        ▼
 Central{Create|Get|List|Update}LoanApplicationRoute     (orchestration)
        │
        ▼
 JPA  /  Redis  /  Kafka  /  Credit-Bureau REST          (Producer)
```

Routes follow the CamelBee scaffold conventions — `camelBeeRouteConfigurer`
first, `noErrorHandler()` on central+producer, `appErrorHandler()` on
consumers, `Constants.ORIGINAL_BODY` / `ACTUAL_RESPONSE_BODY` for
cross-route state.

---

## REST API

| Method | Path | Operation |
|---|---|---|
| `POST` | `/camelbee-service/loan-applications` | Submit a new application → `202 RECEIVED` |
| `GET`  | `/camelbee-service/loan-applications/{applicationId}` | Fetch by id (cache-aside) → `200` or `404` |
| `GET`  | `/camelbee-service/loan-applications?status=PENDING_REVIEW&page=0&pageSize=10` | List (filter + paginate) |

OpenAPI spec: [`src/main/resources/openapi/order-api.yaml`](src/main/resources/openapi/order-api.yaml)
(filename kept for build-plugin compatibility — content is the Loan
Application Service API).

### Example

```bash
curl -X POST http://localhost:8080/camelbee-service/loan-applications \
  -H "transactionId: $(uuidgen)" \
  -H "requestId: $(uuidgen)" \
  -H "Content-Type: application/json" \
  -d '{
    "applicantId": "APP-001",
    "applicantName": "Jane Doe",
    "applicantEmail": "jane@example.com",
    "requestedAmount": 25000.00,
    "purpose": "PERSONAL",
    "termMonths": 36,
    "monthlyIncome": 5000.00,
    "creditScore": 720,
    "employmentStatus": "EMPLOYED"
  }'
```

---

## MCP Tools

| Tool | Purpose |
|---|---|
| `submitLoanApplication` | Submit a new application (returns RECEIVED immediately) |
| `getLoanApplicationStatus` | Get current status / decision (served from cache when available) |
| `listPendingApplications` | List PENDING_REVIEW applications (paginated) |

---

## Kafka Topics

| Topic | Direction | Schema |
|---|---|---|
| `loan-applications.submitted` | Producer (after submit) + Consumer (triggers processing) | `LoanApplicationSubmittedEvent` (AVRO) |
| `loan-applications.processed` | Producer (after processing) | `LoanApplicationProcessedEvent` (AVRO) |

Avro schemas are registered in **Apicurio Schema Registry** at
`http://schema-registry:8081/apis/registry/v2`.

---

## Persistence

| Backend | Purpose |
|---|---|
| **PostgreSQL** | Authoritative store for `LOAN_APPLICATIONS` (table created by Flyway `V2`) |
| **Redis** | Cache-aside reads on GET; write-through after submit/process; TTL 1 hour |
| **Apache Kafka** | Asynchronous event bus for submit → process |
| **Credit Bureau (REST)** | External `/credit-assessments` call (mocked by WireMock in tests) |

---

## Quick start

```bash
# Build the jar (skips tests for speed)
./mvnw clean package -DskipTests

# Bring up app + all backends
docker compose up --build -d

# Health check
curl http://localhost:8080/health
```

Open the CamelBee debugger UI at <http://localhost:8080/camelbee/index.html>
to see message flow across the three-layer architecture.

---

## Testing

| Level | Command | What it tests |
|---|---|---|
| Unit | `./mvnw test` | Mappers + central-route logic (mocked backends) |
| Integration | `./mvnw compile failsafe:integration-test failsafe:verify` | Routes against real Postgres / Kafka / Redis / Apicurio / WireMock (via `docker compose up -d`) |
| Black-box | `./mvnw verify -Pblack-box-test` | Fully dockerized end-to-end (app + backends, app talks to its own backends inside Docker) |

Integration and black-box tests use the same docker-compose stack
(`src/integration-test/resources/compose-backends.yml`) — `TestContainerConfiguration`
brings it up via the `docker` CLI and leaves it running between Maven
invocations for fast iteration. Tear down with:

```bash
docker compose -f src/integration-test/resources/compose-backends.yml down -v
```

---

## Load testing

k6 scripts under `docs/k6/`:

```bash
k6 run docs/k6/rest/rest-throughput-test.js   # POST /loan-applications under load
k6 run docs/k6/mcp/mcp-throughput-test.js     # submitLoanApplication MCP tool under load
```

---

## Project layout

```
src/main/java/io/fintech/loan/application/service/
  model/
    domain/                        # LoanApplication + enums
    api/{json,mcp,avro}/           # API-side models (generated)
    infra/{jpa,cache,json}/        # Backend-side models (entity, cache, REST)
  mapper/
    api/                           # JSON / MCP / AVRO  <->  Domain
    infra/                         # JPA / Cache / Credit Bureau <-> Domain
  routes/
    consumer/{rest,mcp,kafka}/     # Entry points
    central/                       # Orchestration (Create/Get/List/Update)
    producer/{rest,jpa,kafka,cache}/  # Backend calls
  exception/                       # Error framework + REST/MCP formatters
src/integration-test/...           # Real-backend tests (TestContainers)
src/black-box-test/...             # Fully dockerized end-to-end tests
src/main/resources/
  openapi/                         # OpenAPI spec for the LoanApplication API
  mcp/                             # MCP schemas
  backends/openapi/                # Credit Bureau API spec
  avro/                            # Event schemas (Submitted / Processed)
docs/
  k6/                              # Load tests
  postman/                         # Postman collection
```

---

## Replacing the reference domain again

This project was generated from the CamelBee Order/Purchase scaffold and then
transformed into the Loan Application domain. To replace this domain with
yet another one, follow `/cb-replace-domain` (`.claude/skills/cb-replace-domain/SKILL.md`).
