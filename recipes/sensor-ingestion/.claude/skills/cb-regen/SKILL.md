---
description: Regenerates spec-driven code (OpenAPI, XSD/JAXB, Protobuf, Avro, WSDL) after editing any spec file. Run this whenever a .yaml/.xsd/.proto/.avsc/.wsdl changes.
---

# Regenerate Spec-Driven Code

CamelBee generates Java code from multiple spec formats at build time. When you edit a spec, the corresponding Java under `target/generated-sources/` is **stale until you regenerate**.

---

## When to run this

After editing any of:

- `src/main/resources/openapi/*.yaml` — REST / MCP API models

---

## Run

```bash
./mvnw generate-sources
```

This runs every generator Maven plugin configured in `pom.xml`:
- OpenAPI Generator (`openapi-generator-maven-plugin`)

Generated output lands in `target/generated-sources/` under per-plugin subdirs.

---

## Verify what changed

After running, quickly spot-check:

```bash
# List most recently modified generated files
find target/generated-sources -name "*.java" -newer pom.xml | head -20

# Count classes per format (sanity — these numbers should match your spec content)
find target/generated-sources/openapi -name "*.java" | wc -l
```

---

## What NOT to do

- **Never edit files under `target/generated-sources/`** — they're overwritten by the next `generate-sources` run. Edit the spec instead.
- **Never commit `target/`** — it's in `.gitignore` for a reason.
- **Don't confuse `generate-sources` with `compile`** — `compile` runs `generate-sources` first, so `./mvnw compile` also regenerates. But if you want to regenerate without compiling (e.g. to inspect generated files before fixing a build error), use `generate-sources` alone.

---

## Follow-up steps

After regeneration, depending on what you changed:

1. **If you added a new XML root class**: add a Maven Replacer execution in `pom.xml` (it fixes `@XmlRootElement` placement). See `.claude/rules/build-plugins.md` § "Maven Replacer Plugin".
3. **If you added new model classes (Quarkus native builds)**: register them in `config/ReflectionConfig.java` with `@RegisterForReflection`. Without this, the native image will fail at runtime.
4. **Re-run tests**: `./mvnw test` at minimum. New specs often break existing mapper tests.

---

## Troubleshooting

| Symptom | Fix |
|---|---|
| `BUILD FAILURE` in `generate-sources` phase | Read the error — usually a spec syntax issue (YAML indent, XSD malformed, proto syntax) |
| Java classes exist under `target/generated-sources/` but IDE can't see them | In IntelliJ: right-click `target/generated-sources/{plugin}/` → "Mark Directory as → Generated Sources Root". Or just run `./mvnw compile` to refresh IDE indexing |
| OpenAPI generator creates an unexpected package name | Check the `modelPackage` / `apiPackage` config in `pom.xml` under the relevant `openapi-generator-maven-plugin` execution |
| New proto field isn't appearing in generated Java | Protobuf plugin doesn't always detect spec changes — `./mvnw clean generate-sources` to force a full regeneration |
| XJC generates `@XmlRootElement` on the wrong class | Maven Replacer has an allowlist — add a new `<execution>` for your class. See `.claude/rules/build-plugins.md` |