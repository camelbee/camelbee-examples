---
paths:
  - "src/main/resources/application.yml"
  - "src/main/resources/application-quarkus.yml"
  - "src/main/java/**/config/*Jwt*.java"
  - "src/main/java/**/config/CamelbeeSecurity*.java"
  - "src/main/java/**/utils/JwtAuthorizationUtils.java"
  - "src/integration-test/resources/docker/keycloak/**"
  - "src/black-box-test/java/**/BlackBoxTest.java"
---
# Security: OAuth2 / JWT / Keycloak

_This microservice does NOT currently have any OAuth-protected interface or backend._ The file is included so that if you **add** an OAuth interface later, the full pattern is documented. To enable OAuth on a new interface, pass `OAUTH_K` (Keycloak) as part of `interfacesDetails` when regenerating, or follow the pattern below.

---

## Which interfaces can use OAuth

OAuth is enabled at **interface level**, not at operation level. Interfaces that support it:
- REST (most common)
- GraphQL
- MCP
- WebSocket

Each protected interface shares the same `camelbee.security.*` config block in `application.yml`.

---

## `application.yml` security block

```yaml
camelbee:
  security:
    enabled: ${CAMELBEE_SECURITY_ENABLED:true}
    jwt:
      issuer: ${CAMELBEE_SECURITY_JWT_ISSUER:http://localhost:8085/realms/camelbee}
      audience: ${CAMELBEE_SECURITY_JWT_AUDIENCE:camelbee-app}
      clock-skew-seconds: 30          # tolerance for slight clock drift between IdP and service
    jwks:
      url: ${CAMELBEE_SECURITY_JWKS_URL:http://keycloak:8080/realms/camelbee/protocol/openid-connect/certs}
      cache-duration-minutes: 5       # how long JWKS is cached before re-fetch
    roles:
      claim-path: "realm_access.roles"    # JSON path inside JWT claims
    scopes:
      claim-path: "scope"                 # space-separated string
```

**Env var naming**: all keys map to SCREAMING_SNAKE_CASE with `CAMELBEE_SECURITY_` prefix. Deploy by setting these, not by editing YAML in prod.

**Host name trap (compose-blackbox.yml)**: note that `issuer` often uses `localhost:8085` (so the token's `iss` claim matches what bbtests obtain), while `jwks.url` uses `keycloak:8080` (so the app, running inside Docker, can reach Keycloak by service name). These are two different contexts; don't "fix" one to match the other.

---

## JWKS fetching & caching

`CamelbeeJwtProvider` (auto-configured) handles JWKS:
- Fetches the key set from `jwks.url` on first use
- Caches for `cache-duration-minutes` (default 5 min)
- Re-fetches on expiry or on a `kid` miss (new key rotation)
- Thread-safe; shared across all concurrent requests

If Keycloak rotates keys faster than the cache duration, you'll see intermittent `InvalidSignature` errors for ~5 minutes. Either lower the cache or trigger a manual refresh.

---

## Route-level authorization

`JwtAuthorizationUtils` is a **static helper** — call it from a processor or `.process()` block:

```java
.process(exchange -> {
    JwtAuthorizationUtils.hasAllRoles(exchange, "camelbee", List.of("create-order"));
    JwtAuthorizationUtils.hasScope(exchange, "orders:write");
})
```

Both methods throw `InsufficientPrivilegesException` if the check fails, which the error framework maps to:
- `ERROR-AUTH010` → 403 Forbidden (missing role)
- `ERROR-AUTH011` → 403 Forbidden (missing scope)

Place these checks in the consumer route **after** JWT validation has happened (which is automatic via the configured security filter) but **before** calling the central route. Never put them in central or producer routes.

### Role vs scope semantics

| | Roles | Scopes |
|---|---|---|
| Source | `realm_access.roles` | `scope` (space-separated) |
| Meaning | What the user IS (admin, customer) | What the user can DO in this request (orders:write) |
| Granularity | User-level | Token-level — a single user can have different-scoped tokens |
| When to check | Role = authentication-style (identity) | Scope = authorization-style (consent/capability) |

Use roles for "you are not this kind of user" and scopes for "you didn't ask for this capability when you got the token."

---

## Keycloak realm setup

The integration-test and bbtest Docker environment ships a pre-configured Keycloak realm:

- File: `src/integration-test/resources/docker/keycloak/realm-import.json`
- Realm name: `camelbee`
- Client: `camelbee-app` (confidential, with client secret)
- Roles defined in the realm: see the JSON — typically one role per major operation, plus `admin`
- Test users: usually `testuser` / `testpass`

### Adding a new role

1. Edit `realm-import.json` → add under the `roles.realm` array
2. Assign it to the test user or to the client scope
3. Add the role check in your route via `JwtAuthorizationUtils.hasAllRoles(...)`
4. Rebuild the Docker containers — the realm-import.json is loaded on Keycloak container startup, not at runtime

### Realm import loading

`docker-compose` mounts the file read-only and passes `--import-realm` to Keycloak. If you edit `realm-import.json` while containers are running, Keycloak does **not** pick up the change — you must restart the Keycloak service.

---

## Test-side token acquisition

### Black-box tests

`BlackBoxTest.obtainAccessToken()` is a helper in the base class. It hits Keycloak's token endpoint:

```java
String token = obtainAccessToken("camelbee-app", "client-secret-here");
RestAssured.given()
    .header("Authorization", "Bearer " + token)
    .contentType("application/json")
    .body(payload)
    .post("/api/orders");
```

The client secret is pulled from `CAMELBEE_SECURITY_JWT_CLIENT_SECRET` env var (usually set in `compose-blackbox.yml`). Don't hard-code it in test code.

### Integration tests

Integration tests use the same helper via the `IntegrationTest` base class. Because the test JVM runs on the host (not inside Docker), it reaches Keycloak at `localhost:8085` — matching the `issuer` URL in the JWT.

### Negative tests

Always include at least one test per protected operation that sends no token or a malformed one. The error framework should return 401/403 with the correct `code` in the body.

---

## Enabling OAuth on an existing interface

If the microservice was generated without `OAUTH_K` in the interface detail but you want to add it later:

1. Add `camelbee.security.*` block to `application.yml` (template above)
2. Add `CamelbeeSecurityConfig.java` if not present (wires the JWT filter)
3. For each protected route, add `.process(...)` with `JwtAuthorizationUtils.hasAllRoles(...)` / `hasScope(...)` checks
4. Add Keycloak service to `src/integration-test/resources/compose-backends.yml` + `compose-blackbox.yml`
5. Add `realm-import.json` under `src/integration-test/resources/docker/keycloak/`
6. Add the `getKeycloakHost()`/`getKeycloakPort()` wait in `TestContainerConfiguration` (both integration-test and black-box versions)
7. Update tests to obtain and include bearer tokens
8. Document the required role/scope in the OpenAPI spec `security` section

The existing archetype patterns are the best reference — search the codebase for `camelbee.security` and `obtainAccessToken` to find all the touchpoints.

---

## Credential alignment applies here too

See `test-patterns.md` § "Backend credential & connection alignment". Keycloak is subject to the same four-touchpoint rule as any other backend: admin creds, realm config, app-side JWKS URL, test-side issuer URL. A change in one requires matching changes in the others.

---

## Anti-patterns

| Wrong | Right |
|---|---|
| Checking roles in a central or producer route | Check in the consumer only — central/producer should assume auth already happened |
| Hard-coding the client secret in test code | Read from env var; provide via `compose-*.yml` |
| Using `issuer=keycloak:8080` (Docker name) for bbtest token acquisition | Use `localhost:8085` — the token `iss` must match what both the app and the test see |
| Catching `InsufficientPrivilegesException` and converting to 500 | Let it propagate — the error framework maps it to 403 with `ERROR-AUTH010/011` |
| Enabling OAuth globally via a Camel interceptor | Apply per-interface; some interfaces (e.g. internal messaging) may not need auth |
| Forgetting to restart Keycloak after editing `realm-import.json` | Realm is loaded on container start, not at runtime |
| Setting `cache-duration-minutes: 0` to "always fetch" | You'll DDOS your Keycloak; 1–5 minutes is the right range |