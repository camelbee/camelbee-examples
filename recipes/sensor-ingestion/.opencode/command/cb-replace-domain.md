---
description: Guided walkthrough for replacing the Order/Purchase reference domain with the user's actual domain. Enumerates every file and test that must change so nothing is missed.
---

# Replace Reference Domain: $ARGUMENTS

The CamelBee archetype ships with an **Order / Purchase** reference domain so every route, test, and script works out of the box. Once you've verified things run locally, you'll want to replace Order/Purchase with your actual business domain.

This is tedious to do from memory — there are ~40 touchpoints. This skill walks through every one.

---

## Before you start

1. **Commit the generated project as-is first (HARD GATE — do not proceed without it).** `git init && git add -A && git commit -m "Initial CamelBee generation" && git tag camelbee-baseline`. If git is unavailable, copy the whole project to a sibling backup folder instead: `mkdir -p ../sensor-ingestion-camelbee-baseline && cp -R . ../sensor-ingestion-camelbee-baseline/`. Verify before continuing: `git tag -l camelbee-baseline` must print the tag (or the backup folder must exist). If something breaks later — or a test level "seems to be missing" — the baseline is where you recover it from: `git show camelbee-baseline:<path>` / `git checkout camelbee-baseline -- <path>`, or for the folder baseline `diff -u ../sensor-ingestion-camelbee-baseline/<path> <path>` to compare and `cp -R ../sensor-ingestion-camelbee-baseline/<path> <path>` to restore.
2. **Make sure all tests pass** on the untouched generation: `./mvnw test && ./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify && ./mvnw verify -Pblack-box-test`. Don't start a rename on a broken tree.
3. **Inventory the tests you must preserve:** `find src/test src/integration-test src/black-box-test -name "*Test.java" | wc -l`. The transformation renames these tests — it never removes them. When you finish, the per-level counts must be equal or higher and all three levels must pass; "there are no black-box tests" is never a valid end state.
4. **Pick a naming scheme now:**
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
| `src/main/resources/openapi/order-api.yaml` | Rename schemas (Order → $1, Orders → $2), paths (/orders → /$2), operationIds (createOrder → create$1). Optionally rename the file too — then update `pom.xml` `<inputSpec>` and the `rest().openApi().specification(...)` path in the REST consumer | REST API |
| `docs/postman/*.json` | Rename collection entries (or regenerate from the updated OpenAPI spec) | Postman tests |

### pom.xml touchpoints (same phase — the build must match the renamed specs)

- OpenAPI Generator `<inputSpec>` paths — if you renamed the spec files

After editing specs: **run `/cb-regen`** (`./mvnw generate-sources`) and verify new generated code appears under `target/generated-sources/`.

---

## Phase 2 — Rename hand-written Java

Use IDE "rename symbol" for speed. Files to touch:

### Domain models (`src/main/java/**/model/domain/`)

- `Order.java`, `OrderItem.java`, `OrderStatus.java` (and any sub-types) → your domain
- Don't forget `@ToString` on the new classes — the CamelBee tracer depends on it

### Hand-written API & infra models (`model/api/` and `model/infra/`) — NOT covered by the specs

Only the JSON/XML/Proto/Avro/SOAP/gRPC models are plugin-generated from Phase 1. The following are **hand-written** and must be renamed manually (all keep `@ToString`):

- `model/api/{tech}/OrderEvent*.java` — event wrappers for database/messaging/streaming interfaces (one folder per enabled tech: sql, jpa, mongodb, cassandra, awsdynamodb, csv, graphql, sse)
- `model/infra/{tech}/Purchase*.java` + `PurchaseItem*.java` — backend entities per enabled backend (jpa, sql, mongodb, cassandra, awsdynamodb, cache, messaging, csv, sse). For JPA entities keep the `@ToString(exclude = ...)` pattern on bidirectional relationships

### Mappers (`src/main/java/**/mapper/`)

All classes that include `Order`, `Orders`, `Purchase`, `Purchases` in their name:
- `mapper/api/{Format}OrderMapper.java` (per format: Json, Xml, Proto, Avro, Soap, Grpc, Graphql, Mcp, Csv)
- `mapper/infra/{Format}PurchaseMapper.java` (per active format/backend)
- `mapper/api/{Format}ErrorMapper.java` — usually keep the name `Error`, just review

### Routes (`src/main/java/**/routes/`)

- `routes/consumer/{tech}/` — rename all route classes (e.g. `RestCreateOrderRoute` → `RestCreate$1Route`) and update `.routeId()` values
  - **REST consumer stays contract-first:** keep the `rest().openApi().specification("openapi/<your-api>.yaml").missingOperation("ignore")` binding and rename the `from("direct:{operationId}")` routes to match the new operationIds in your spec. Renaming the spec file itself is fine — update its path in this binding AND in the pom.xml OpenAPI Generator `<inputSpec>`. Do NOT convert to manual `rest().get(...)` / `rest().post(...)` definitions — the endpoints must keep coming from the spec
- `routes/central/Central{Operation}OrderRoute.java` — per operation
- `routes/producer/{tech}/` — rename producer routes + update `.id()` on backend endpoints

### Support classes (`src/main/java/**/`)

- `config/SharedMapperConfig.java` — only needs review if you renamed mapper packages
- `config/ReflectionConfig.java` — re-register every renamed model class for native builds; a stale entry compiles fine but fails at native runtime
- Other `config/` classes that embed domain names (collection/table names in `MongoConfig`, `DataSourceConfig`, etc.) — grep the folder for "order" / "purchase"
- `constants/Constants.java` — domain-named constants such as `AGGREGATED_BATCH_ORDERS` (rename the constant AND its string value together with all usages)
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

### MongoDB

- `src/integration-test/resources/docker/mongodb/init-mongo.js` — db + collection names
- `src/integration-test/resources/backend/mongodb/reset-mongodb.json` — seed

---

## Phase 5 — Config files

- `src/main/resources/application.yml` — rename any topic/queue/table properties that include "order" or "purchase" (Kafka topics, AMQP queues, table names)
- the `%itest` profile section at the bottom of `application.yml` — poll periods and logging levels usually have nothing to rename, but check
- `docker-compose.yml` — env vars that reference the domain
- `src/integration-test/resources/compose-backends.yml` — env vars + any domain references
- `src/black-box-test/resources/compose-blackbox.yml` + `compose-blackbox-native.yml` — env var overrides

---

## Phase 6 — Load test scripts & docs

- `docs/k6/**/*.js` — rename URLs, payloads, queue/topic names per protocol
- `docs/uml/*.puml` — update diagrams
- `README.md` — update examples that reference the old domain
- `docs/postman/` and `docs/soap/` — regenerate from updated specs or edit by hand

---

## Phase 7 — Verify

Run all three test levels. Every failure points at a rename you missed — usually a hard-coded string in a test data file or a reset script.

```bash
./mvnw test
./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify
./mvnw verify -Pblack-box-test
```

Then run a **residue grep** — after a complete transformation, every remaining hit must be individually justifiable (e.g. "sort order", a third-party class name, this skill's own text):

```bash
grep -rin --exclude-dir=target --exclude-dir=.git "order\|purchase" src pom.xml docker-compose.yml docs
```

Unjustified hits are renames you missed — this single check catches stale pom Replacer tokens, broker queue definitions, config constants, and forgotten test fixtures alike.

**If the NEW domain itself contains "order" or "purchase"** (e.g. `Order` → `PurchaseOrder`, or a `WorkOrder` domain), the substring grep above can no longer separate old from new — do NOT bulk-remove hits. Use these instead:

1. **Baseline diff is the reliable residue detector:** `git diff --stat camelbee-baseline` (or `diff -rq` against the folder baseline). Any file with **zero changes** since the baseline is untransformed reference code — that's the real leftover list, independent of naming overlap.
2. Grep for reference-domain strings your new domain does NOT share, instead of the bare words: the sample field names (`salesChannel`), infra table/collection names (`purchases`, `camelbee_purchases_table`), the WireMock backend path (`/purchases`), old topic/queue fragments (`createorder-topic`, `createorder-queue`).
3. Judge every remaining hit in context — never delete a match just because it contains "order"; it may be your new domain's code.

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
| XML responses carry the OLD element names | pom.xml Maven Replacer executions still target the old class files/tokens |
| JMS/AMQP consumer never receives messages | `broker.xml` / `default.json` queue definitions still use the old operation names |
| Quarkus native build runs but fails at runtime with reflection errors | Renamed model classes not re-registered in `config/ReflectionConfig.java` |
