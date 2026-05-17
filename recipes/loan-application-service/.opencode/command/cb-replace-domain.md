---
description: Guided walkthrough for replacing the Order/Purchase reference domain with the user's actual domain. Enumerates every file and test that must change so nothing is missed.
---

# Replace Reference Domain: $ARGUMENTS

The CamelBee archetype ships with an **Order / Purchase** reference domain so every route, test, and script works out of the box. Once you've verified things run locally, you'll want to replace Order/Purchase with your actual business domain.

This is tedious to do from memory — there are ~40 touchpoints. This skill walks through every one.

---

## Before you start

1. **Commit the generated project as-is first.** `git init && git add -A && git commit -m "Initial CamelBee generation"`. If something breaks later, you can `git diff` against this baseline.
2. **Make sure all tests pass** on the untouched generation: `./mvnw test && ./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify && ./mvnw verify -Pblack-box-test`. Don't start a rename on a broken tree.
3. **Pick a naming scheme now:**
   - API-side noun (what external clients see): `Order` → `$1`
   - Infra-side noun (what the backend stores): `Purchase` → decide now, e.g. `ShipmentRecord`
   - Plural forms for both
   - Operation prefixes: `createOrder` → `create{$1}`, etc.

Once you start editing, use your IDE's "rename symbol" feature — it's faster and safer than manual find/replace.

---

## Phase 1 — Update the specs (source of truth)

The spec files drive code generation. Edit these **first** and run `/cb-regen` before touching Java.

| Spec | Edit | Purpose |
|---|---|---|
| `src/main/resources/openapi/order-api.yaml` | Rename schemas (Order → $1, Orders → $2), paths (/orders → /$2), operationIds (createOrder → create$1) | REST API |
| `src/main/resources/mcp/order-mcp-api.yaml` | Same pattern — update tool names and schemas | MCP tools |
| `src/main/resources/backends/openapi/purchase-api.yaml` | Rename Purchase → your infra noun | REST backend models |
| `src/main/resources/avro/*.avsc` | Rename record names | Avro API |
| `src/main/resources/backends/avro/*.avsc` | Rename backend records | Avro backend |
| `docs/postman/*.json` | Rename collection entries (or regenerate from the updated OpenAPI spec) | Postman tests |

After editing specs: **run `/cb-regen`** (`./mvnw generate-sources`) and verify new generated code appears under `target/generated-sources/`.

---

## Phase 2 — Rename hand-written Java

Use IDE "rename symbol" for speed. Files to touch:

### Domain models (`src/main/java/**/model/domain/`)

- `Order.java`, `OrderItem.java`, `OrderStatus.java` (and any sub-types) → your domain
- Don't forget `@ToString` on the new classes — the CamelBee tracer depends on it

### Mappers (`src/main/java/**/mapper/`)

All classes that include `Order`, `Orders`, `Purchase`, `Purchases` in their name:
- `mapper/api/{Format}OrderMapper.java` (per format: Json, Xml, Proto, Avro, Soap, Grpc, Graphql, Mcp, Csv)
- `mapper/infra/{Format}PurchaseMapper.java` (per active format/backend)
- `mapper/api/{Format}ErrorMapper.java` — usually keep the name `Error`, just review

### Routes (`src/main/java/**/routes/`)

- `routes/consumer/{tech}/` — rename all route classes (e.g. `RestCreateOrderRoute` → `RestCreate$1Route`) and update `.routeId()` values
- `routes/central/Central{Operation}OrderRoute.java` — per operation
- `routes/producer/{tech}/` — rename producer routes + update `.id()` on backend endpoints

### Support classes (`src/main/java/**/`)

- `config/SharedMapperConfig.java` — only needs review if you renamed mapper packages
- Any custom `@Processor` classes referencing Order/Purchase

---

## Phase 3 — Test fixtures & data

### Test data producers

- `src/test/java/**/utils/testdata/{Operation}DomainTestDataProducer.java` — update to produce your domain objects
- `src/integration-test/java/**/utils/testdata/{Format}OrderDataProducerApi.java` — per format
- `src/integration-test/java/**/utils/testdata/{Format}PurchaseDataProducerInfra.java` — per format
- `src/integration-test/java/**/utils/testdata/MultiFormatTestDataGenerator.java` — main entry point

Run it to regenerate binary test data: execute `MultiFormatTestDataGenerator.main()` from your IDE, or `./mvnw exec:java -Dexec.mainClass="...MultiFormatTestDataGenerator"`.

### Test data files

- `src/integration-test/resources/data/inttest/api/{format}/{operation}/` — JSON/XML editable by hand; binary regenerated above
- `src/integration-test/resources/data/inttest/infra/{format}/{operation}/` — same for infra
- `src/black-box-test/resources/data/bbtest/api/{format}/{operation}/` — same structure for bbtests
- `src/black-box-test/resources/data/bbtest/infra/{format}/{operation}/`
- `src/integration-test/resources/docker/wiremock/mappings/{backend}/{operation}/*.json` — rename operations
- `src/integration-test/resources/docker/wiremock/__files/{backend}/{format}/{operation}/` — response body files

### Test classes

- `src/test/java/**/mapper/*Test.java` — mapper tests
- `src/test/java/**/routes/central/Central{Operation}OrderRouteUnitTest.java` — route unit tests
- `src/integration-test/java/**/{Operation}IntegrationTest.java` — one per operation × one base + one per interface
- `src/black-box-test/java/**/{Operation}BlackBoxTest.java` — one base per operation
- `src/black-box-test/java/**/{interface}/{Interface}Interface{Operation}BlackBoxTest.java` — one per interface × operation

### Test helpers

- `src/black-box-test/java/**/utils/DataSeeder.java` — update methods/table names
- `src/black-box-test/java/**/utils/DataVerifier.java` — update count/get/clear helpers
- `src/black-box-test/java/**/utils/MessageVerifier.java` — update topic/queue names if renamed
- `src/integration-test/java/**/itest/IntegrationTest.java` — base class, update reset helpers

---

## Phase 4 — Docker, SQL, CQL, JSON init scripts

### SQL / JPA

- `src/integration-test/resources/docker/jdbc/{vendor}/V*__*.sql` — rename tables + columns to match your domain
- `src/integration-test/resources/backend/sql/reset-{vendor}.sql` — seed data
- Consider adding a new migration `V{n+1}__Rename_Tables.sql` rather than editing existing ones — see `db-migration-patterns.md`

---

## Phase 5 — Config files

- `src/main/resources/application.yml` — rename any topic/queue/table properties that include "order" or "purchase" (Kafka topics, AMQP queues, table names)
- `src/main/resources/application-quarkus.yml` (if Quarkus)
- `docker-compose.yml` — env vars that reference the domain
- `src/integration-test/resources/compose-backends.yml` — env vars + any domain references
- `src/black-box-test/resources/compose-blackbox.yml` + `compose-blackbox-native.yml` — env var overrides

---

## Phase 6 — Load test scripts & docs

- `docs/k6/**/*.js` — rename URLs, payloads, queue/topic names per protocol
- `docs/uml/*.puml` — update diagrams
- `README.md` / `README-quarkus.md` — update examples that reference the old domain
- `docs/postman/` and `docs/soap/` — regenerate from updated specs or edit by hand

---

## Phase 7 — Verify

Run all three test levels. Every failure points at a rename you missed — usually a hard-coded string in a test data file or a reset script.

```bash
./mvnw test
./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify
./mvnw verify -Pblack-box-test
```

Also run `/cb-debug` to visually verify message flow through the CamelBee UI — the message bodies there should display as your new domain's `toString()` output.

---

## Common misses

| Symptom | Likely cause |
|---|---|
| Integration test passes but bbtest fails with 500 | Forgot to rename a table/topic in `compose-blackbox.yml` env vars |
| 404 on REST endpoint | Forgot to rename the path in OpenAPI spec + consumer route |
| `ClassName@abc123` in CamelBee debugger UI | New domain class missing `@ToString` or `toString()` |
| `Unable to find mapper for ...` | Forgot a mapper class; run `/cb-regen` and check `target/generated-sources/` |
| WireMock "no stub matched" | Renamed operation in spec but not in `docker/wiremock/mappings/` |
| DataVerifier count is 0 when it should be N | Table name mismatch between SQL migration and `DataVerifier.countSqlPurchases()` helper |
| Binary test data tests fail | Didn't regenerate binary fixtures — run `MultiFormatTestDataGenerator` |
