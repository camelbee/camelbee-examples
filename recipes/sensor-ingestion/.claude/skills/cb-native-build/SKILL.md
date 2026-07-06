---
description: Builds and runs the Quarkus native image. Troubleshoots common reflection/resource/JNI failures and compares JVM vs native vs native-micro image sizes.
argument-hint: [build|run|troubleshoot] (default: build)
---

# Quarkus Native Build: $ARGUMENTS

Build, run, and troubleshoot the Quarkus native image. Native mode compiles Java ahead-of-time via GraalVM — startup in ~50ms and tiny memory footprint, at the cost of longer build times (~5-10 min) and lost runtime reflection.

---

## Three build modes

This project ships three Dockerfiles for three different runtime modes:

| Mode | Dockerfile | Image size | Startup | When to use |
|---|---|---|---|---|
| JVM | `src/main/docker/Dockerfile.jvm` | ~200 MB | ~2s | Dev, most prod (cheapest to operate for small/medium throughput) |
| Native | `src/main/docker/Dockerfile.native` | ~80 MB | ~50ms | Serverless, high-scale, fast-scaling workloads |
| Native-micro | `src/main/docker/Dockerfile.native-micro` | ~20 MB | ~50ms | Extreme footprint pressure (edge, IoT) — uses UPX compression + scratch base |

### Build the native executable

```bash
./mvnw package -Pnative -DskipTests
```

Takes 5–10 minutes on a modern laptop. Output: `target/*-runner` (executable, not a JAR).

### Build the native Docker image

```bash
# Standard native image (~80 MB)
docker build -f src/main/docker/Dockerfile.native -t sensor-ingestion-native .

# Native-micro (~20 MB with UPX)
docker build -f src/main/docker/Dockerfile.native-micro -t sensor-ingestion-native-micro .
```

### Run with all backends

```bash
docker compose -f docker-compose-native.yml up --build
```

### Run bbtests in native mode

```bash
./mvnw verify -Pblack-box-test -Dbbtest.native=true
```

The `-Dbbtest.native=true` switch tells `TestContainerConfiguration` to use `compose-blackbox-native.yml` instead of `compose-blackbox.yml`.

---

## Reflection registration (MANDATORY for hand-written models)

Native images strip out runtime reflection by default. Any class that Jackson, JAXB, MapStruct, or Camel accesses reflectively at runtime **must be registered** in `config/ReflectionConfig.java`:

```java
@RegisterForReflection(targets = {
    YourNewDomainClass.class,
    YourNewEventClass.class,
})
public class ReflectionConfig {}
```

Or per-class via annotation:
```java
@RegisterForReflection
public class YourNewDomainClass {
  // ...
}
```

**Generator-produced classes** (OpenAPI, Protobuf, Avro, JAXB) — usually auto-registered by Quarkus extensions. The requirement is for your hand-written domain / DTO / event classes under `src/main/java/**/model/domain/**`.

### How to find missing registrations quickly

Run the native build, then run the app. The first reflection miss throws:
```
java.lang.ClassNotFoundException: ... at runtime reflective call
```

Add the class to `ReflectionConfig.java`, rebuild, re-run. Rinse and repeat. For faster iteration, use JVM-mode testing first to catch logic bugs before paying the native compile cost.

---

## Native-mode rules (camel-dsl-patterns.md has the DSL details)

| Rule | Why |
|---|---|
| Use `method(this, "methodName")` not lambdas in `.split()` / `.aggregate()` | Lambdas don't register as reflection targets |
| Explicit `JacksonDataFormat` with injected `ObjectMapper`, not `.marshal().json()` | Inline JSON uses runtime reflection |
| MapStruct `componentModel = "cdi"` | Not `"spring"` (that's SpringBoot) |
| `@ApplicationScoped` / `@Inject`, not `@Component` / `@Autowired` | Quarkus DI model |
| Collection DI via `Instance<T>`, not `List<T>` | Quarkus can't auto-inject raw `List` of beans |

---

## Troubleshooting

### Build fails during `native-image` phase

| Message | Likely cause | Fix |
|---|---|---|
| `UnsupportedFeatureError: Unsafe.getObject` | Native-hostile lib transitively imported | Add `<quarkus.native.additional-build-args>-H:+AllowIncompleteClasspath</quarkus.native.additional-build-args>` to `pom.xml` — last resort, prefer replacing the offending library |
| `Classes that should be initialized at run time got initialized during image building` | Static init of class with network/file access | Add `--initialize-at-run-time=<class>` to native-image args |
| `OutOfMemoryError` during compile | GraalVM needs ~8 GB RAM to compile | Set `-Xmx8g` in `MAVEN_OPTS`, or allocate more to Docker if building inside Docker |
| `LinkageError` at runtime | Missing resource not embedded | Add `@Resource` or list in `quarkus.native.resources.includes` |

### Runtime failures (image built, but app errors)

| Message | Fix |
|---|---|
| `ClassNotFoundException` on a model class | Register in `ReflectionConfig.java` |
| `NoSuchMethodError` on a Jackson deserialization | The model's constructor isn't reflection-visible — either add a no-arg constructor or register for reflection including methods |
| `FileNotFoundException` on a classpath resource | Add to `quarkus.native.resources.includes=**/*.xml,**/*.json` in `application.yml` |
| Slow first request (cold start) after "50ms startup" | That's lazy init — Quarkus warms up some beans on first request; not a native-specific issue |

### Image size tuning

| Want smaller image? | Try |
|---|---|
| From 200 MB (JVM) to 80 MB | Native build: `Dockerfile.native` |
| From 80 MB to 20 MB | `Dockerfile.native-micro` (UPX compresses + scratch base) |
| Tracking where the bytes are | `docker history <image>` — look for heavy layers |

UPX compression in `native-micro` adds ~1s to startup but saves ~60 MB. Turn off if startup time matters more than image size — edit `Dockerfile.native-micro` and remove the `upx` step.

---

## Reality check

Native mode is powerful but **not** the default choice for most services. Go native when:
- Serverless (Lambda, Cloud Run): faster cold starts = real money
- Thousands of short-lived pods in K8s: faster scale-up under load
- Memory-constrained edge devices

Skip native when:
- You're on a fixed-size VM anyway — JVM is fine
- Your team doesn't want to maintain a second set of reflection config
- Build time (~5-10 min) blocks your dev cycle — run JVM in dev, native only in CI/prod
