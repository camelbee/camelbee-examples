---
description: Runs k6 load tests from docs/k6/ against the running microservice. Verifies the service is up first and prints a summary.
---

# Load Test: $ARGUMENTS

Run k6 performance tests against the running microservice. Scripts live under `docs/k6/{protocol}/` — one script per operation.

---

## Prerequisites

1. **Service running**: bring it up with `docker compose up --build -d` and wait until `/health` (SpringBoot) or `/q/health` (Quarkus) returns 200
2. **k6 installed**: `brew install k6` / `choco install k6` / see https://k6.io/docs/get-started/installation/
   - Alternatively use Docker: `docker run --rm -i --network host grafana/k6 run - < script.js`

---

## Available k6 scripts

Scripts are organized by protocol — check `docs/k6/` for what's shipped:

- `docs/k6/rest/` — REST endpoints (default)

---

## Run

```bash
# Default REST load test
k6 run docs/k6/rest/createorder.js

# With a specific scenario
k6 run --vus 10 --duration 30s docs/k6/rest/createorder.js

# All operations for a protocol (shell loop)
for f in docs/k6/rest/*.js; do k6 run "$f"; done
```

### Useful k6 flags

| Flag | Purpose |
|---|---|
| `--vus N` | Virtual users (concurrent clients) |
| `--duration 30s` | Test duration |
| `--summary-export=summary.json` | Export summary for CI parsing |
| `--out json=raw.json` | Full timing data for analysis |
| `--http-debug=full` | Dump every request/response (debugging, not benchmarking) |

---

## Workflow

1. **Check the service is up**
   ```bash
   curl -f http://localhost:8080/health || echo "service not ready"
   ```
2. **Enable the CamelBee tracer** for a visible playback (optional but great for demos):
   ```bash
   CAMELBEE_TRACER_ENABLED=true docker compose up
   ```
   Then open `http://localhost:8080/camelbee/index.html` during the load test.
3. **Pick a baseline** — run with `--vus 1 --duration 10s` first. You need a healthy single-user latency number before you can interpret high-concurrency results.
4. **Ramp up** — rerun at 10, 50, 100 VUs. Watch where latency knee bends.
5. **Read the summary** — k6 prints p50/p95/p99, RPS, error rate at the end.

---

## Common pitfalls

| Symptom | Likely cause |
|---|---|
| All requests fail fast with "connection refused" | Service isn't up — check `docker ps` and logs |
| Latency spikes immediately | Tracer is on (overhead) — disable for realistic numbers: `CAMELBEE_TRACER_ENABLED=false` |
| Error rate climbs with VUs | Backend (DB/broker) is the bottleneck, not the app — load-test the backend in isolation |
| `xk6-grpc` errors | Install the gRPC extension: `xk6 build --with github.com/grafana/xk6-grpc` |
| Script references `localhost:8080` but service runs on different port | Edit script's `BASE_URL` or pass `-e BASE_URL=http://host:port` |

---

## Interpreting results

k6 output blocks worth reading:
- `http_req_duration` — p95 is your SLO indicator
- `http_req_failed` — must be near 0; any spike means the app is erroring
- `iterations` — total completed test iterations; divide by duration for effective throughput
- `vus` / `vus_max` — how many virtual users actually ran

For CI gating, use k6 `thresholds` inside the script:
```js
export const options = {
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed:   ['rate<0.01'],
  },
};
```
k6 exits non-zero on threshold failure — wire it into CI that way.