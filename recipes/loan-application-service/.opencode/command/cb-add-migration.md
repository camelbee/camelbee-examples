---
description: Adds a new Flyway migration (V{n}__{description}.sql) for every active SQL/JPA vendor directory. Pays attention to per-vendor syntax.
---

# Add Flyway Migration: $ARGUMENTS

Creates a new versioned migration file in every vendor directory this project ships. You'll then edit each file to apply vendor-specific SQL.


---

## Active vendor directories

Only the vendors this project ships will have migration dirs — check which exist:

```bash
ls src/integration-test/resources/docker/jdbc/
```

Expected:
- `postgresql/`

---

## Steps

### 1. Find the next version number

```bash
ls src/integration-test/resources/docker/jdbc/postgresql/ | grep -E '^V' | sort -V | tail -1
```

Pick the next integer (or decimal, e.g. `V2_1__`).

### 2. Create the file in every vendor directory

Create `V{n}__{Description_With_Underscores}.sql` in each vendor dir with a stub body.

```bash
VERSION=3  # replace with your chosen number
DESC="$ARGUMENTS"    # e.g. Add_Index_On_CustomerId

for vendor in postgresql mysql mariadb oracle mssql db2; do
  DIR="src/integration-test/resources/docker/jdbc/$vendor"
  [ -d "$DIR" ] && cat > "$DIR/V${VERSION}__${DESC}.sql" <<EOF
-- Migration V${VERSION}: ${DESC} (${vendor})
-- Edit this file with ${vendor}-specific SQL.

EOF
done
```

### 3. Write vendor-appropriate SQL in each

See `.claude/rules/db-migration-patterns.md` for per-vendor quirks. Common cases:

**postgresql/** — standard ANSI SQL works; `SERIAL` for auto-increment; `RETURNING id` on INSERT.

### 4. Update reset scripts (if the migration changes seed data columns)

For each vendor, edit `src/integration-test/resources/backend/sql/reset-{vendor}.sql` to include the new columns in the seed `INSERT`s and the `TRUNCATE`s at the top.

### 5. Update JPA entities (if JPA is used)

If you added a column, add the corresponding `@Column` / field to the JPA entity under `src/main/java/**/model/infra/jpa/{vendor}/*.java`.

### 6. Update `DataVerifier` / `DataSeeder` helpers (if needed)

If tests need to assert on new columns, add helpers in `src/black-box-test/java/**/utils/DataVerifier.java` (count/get) and `DataSeeder.java` (seed).

### 7. Verify migrations apply cleanly

```bash
# Integration tests run Flyway migrations during container startup
./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify

# Black-box tests too
./mvnw verify -Pblack-box-test
```

If integration tests hang on "starting flyway-{vendor}", check Docker logs: `docker logs flyway-{vendor}`. Flyway prints per-migration status on stdout.

---

## Flyway rules you can't break

| Rule | Consequence |
|---|---|
| **Never edit an existing versioned migration** | Flyway tracks checksums — altered migrations abort startup with `FlywayValidateException`. Create `V{n+1}__Fix_X.sql` instead. |
| **Version numbers must be unique per vendor** | Two `V1__*` files fail to apply |
| **Use `_` as separator, not space** | `V1__Add Index.sql` → invalid |
| **Test-only seed data goes in `reset-*.sql`, not `V*__*.sql`** | Otherwise prod databases get fixtures |
| **`R__*.sql` for repeatables** — re-runs on checksum change | Useful for views, triggers, reference-data tables |

---

## See also

- `.claude/rules/db-migration-patterns.md` — full per-vendor reference, reset mechanics, `DataSourceConfig`
- `.claude/rules/test-patterns.md` § "Backend credential & connection alignment" — credentials must match across four touchpoints including the Flyway container
