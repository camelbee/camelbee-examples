---
paths:
  - "src/integration-test/resources/docker/jdbc/**"
  - "src/integration-test/resources/docker/cassandra/**"
  - "src/integration-test/resources/docker/mongodb/**"
  - "src/integration-test/resources/docker/localstack/**"
  - "src/integration-test/resources/backend/**"
  - "src/black-box-test/java/**/DataSeeder.java"
  - "src/main/java/**/config/DataSourceConfig.java"
---
# Database Migrations & Seed Data

This file covers Flyway migrations (SQL/JPA), init + reset scripts per data store, DataSeeder / DataVerifier mechanics, and multi-vendor configuration. When you add a new backend or change a schema, there are multiple files to touch — the patterns here are what keeps them consistent.

---

## Which data stores live where

| Data store | Schema init location | Reset script | Used by (test level) |
|---|---|---|---|
| SQL / JPA | `src/integration-test/resources/docker/jdbc/{vendor}/V*__*.sql` (Flyway) | `src/integration-test/resources/backend/sql/reset-{vendor}.sql` | Integration + BB |

---

## Flyway migrations (SQL / JPA)

### File naming

```
V{version}__{description}.sql        — versioned migration, runs once
R__{description}.sql                  — repeatable migration, runs whenever checksum changes
```

- `version` is numeric with underscores as separators: `V1__`, `V1_1__`, `V2__`, `V2_1__`
- Migrations run in version order, each exactly once per vendor
- Repeatable migrations run after all versioned ones, in alphabetical order, whenever the file content changes

### Per-vendor subdirectories

Flyway reads a **separate** migration history per vendor:

```
src/integration-test/resources/docker/jdbc/
├── postgresql/
│   ├── V1__Create_User.sql
│   ├── V2__Create_Tables.sql
│   └── R__Verify_Ready.sql
├── mysql/
├── mariadb/
├── oracle/
├── mssql/
└── db2/
```

When you add a new migration, **add it to every vendor directory you're shipping** — Flyway doesn't automatically translate SQL across vendors. Pay attention to per-vendor syntax (see "Per-vendor quirks" below).

### Running migrations

Flyway runs inside its own Docker service (`flyway-{vendor}`) that depends on the DB container being healthy. On integration test + bbtest startup:

1. DB container starts → health check passes
2. Flyway container runs `migrate` → exits with status 0
3. App container starts (depends on `flyway-*: condition: service_completed_successfully`)

If you see the app starting before migration, the `depends_on` condition is wrong.

### Use `/cb-add-migration`

The skill generates the `V{n+1}__{description}.sql` file in every active vendor directory with a shared stub. Edit each to apply the vendor-specific syntax.

---

## Per-vendor quirks

### PostgreSQL

- Use `RETURNING id` on inserts to get the generated key
- `SERIAL` / `BIGSERIAL` for auto-increment
- Schema default: `public` (no explicit schema in most queries)
- Flyway connects as user `camelbee_user` (see `compose-backends.yml`)






---

## Reset scripts — what `DataSeeder` / `@BeforeEach` do

Black-box and integration tests need a known state before each test. Each data store has a reset mechanism:

### SQL / JPA

- `DataSeeder.executeSqlScript(jdbcUrl, user, password, "reset-{vendor}.sql")` in bbtests
- `resetPersistenceLayers()` helper in `IntegrationTest.java` for integration tests (executes the same script via `FluentProducerTemplate`)
- The script typically `TRUNCATE`s tables then `INSERT`s seed data




---

## `DataSourceConfig` — multi-vendor routing

If your service talks to multiple SQL/JPA backends (e.g. same service with both Postgres and Oracle), `DataSourceConfig.java` creates **multiple datasources**, one per unique vendor detected at generation time. The runtime picks between them via `DB_VENDOR` env var (e.g. `DB_VENDOR=postgresql`).

SpringBoot auto-configuration is **disabled** when multiple datasources exist — `DataSourceConfig` defines beans manually, including the `EntityManagerFactory` and `TransactionManager` per vendor.

**Do not** add `@EnableAutoConfiguration` hints that re-enable Spring's datasource auto-config — you'll end up with phantom datasources that never get wired to anything.

---

## Adding a new SQL migration (walkthrough)

1. Pick the next version number — look at `src/integration-test/resources/docker/jdbc/postgresql/` and find the highest `V{n}__*.sql`
2. Create `V{n+1}__{Description_With_Underscores}.sql` in **every** vendor directory you ship
3. Write vendor-appropriate SQL in each (see "Per-vendor quirks")
4. Update the Java entity / JSON/XML/PROTO model if the schema change affects it — run `/cb-regen` if you edit a spec
5. Update `reset-{vendor}.sql` if new data columns need seeding for tests
6. Update `DataVerifier.java` / `DataSeeder.java` if new columns affect the count/get helpers
7. Run integration tests to verify migration applies cleanly: `./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify`
8. Run bbtests: `./mvnw verify -Pblack-box-test`

The `/cb-add-migration` skill automates steps 1–2 and leaves you with stubs to edit.

---

## Anti-patterns

| Wrong | Right |
|---|---|
| Editing an existing `V{n}__*.sql` to "fix" a bad migration | Create `V{n+1}__Fix_Previous.sql` — Flyway tracks checksums and refuses to re-run altered migrations |
| Adding a migration to only one vendor dir | Add to **every** vendor you ship — Flyway per-vendor state is independent |
| Using `IDENTITY` columns across all vendors | Oracle wants sequences; MSSQL wants `IDENTITY(1,1)`; PostgreSQL/MariaDB want `SERIAL`/`AUTO_INCREMENT` |
| `TRUNCATE` in production code | Only in test reset scripts — truncate is non-transactional in most vendors |
| Adding seed data in `V*__*.sql` versioned migrations | Seed data belongs in `reset-{vendor}.sql` (test-only) or `R__*.sql` (repeatable) — otherwise production gets test fixtures |
| Forgetting to update `DataSeeder` / `DataVerifier` when schema changes | New columns need matching assertions/helpers; without them bbtests silently under-verify |
| Using `localhost:<port>` inside `application.yml` for Docker-Compose-run app | Use the Docker service name + internal port — see `test-patterns.md` § credential alignment |