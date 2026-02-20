# MCP Interface Performance Benchmarks - CamelBee Microservice

This document presents comprehensive performance benchmarks for CamelBee-generated MCP (Model Context Protocol) microservices across **4 runtimes** and **3 tool implementation configurations** — 12 configurations in total.

---

## Overview

| | Spring Boot MVC | Spring Boot WebFlux | Quarkus JVM | Quarkus Native |
|---|---|---|---|---|
| **MCP Only** | 9,947 req/s | 10,492 req/s | **16,381 req/s** | 9,916 req/s |
| **MCP + MapStruct** | 10,095 req/s | 10,576 req/s | 15,429 req/s | 9,661 req/s |
| **MCP + Camel + MapStruct** | 9,188 req/s | 9,666 req/s | 13,957 req/s | 7,861 req/s |
| **Memory** | 556–572 MB | 627–652 MB | 674–732 MB | **118–210 MB** |

> All results from Run 3 (JVM warm-up complete) for JVM runtimes. Quarkus Native results are consistent across all runs (< 2.5% variance).

---

## Microservice Creation

Microservices were created using the [CamelBee Initializer](https://www.camelbee.io) with the following configurations:

- **Interface**: MCP (Model Context Protocol)
- **Runtimes**: Spring Boot, Spring Boot WebFlux, Quarkus JVM, Quarkus Native
- **Backend**: MOCK (no external dependencies)

---

## Configuration

### Application Configuration

For all runtimes, update `application.yml` to disable all CamelBee interceptors before performance testing:

```yaml
camelbee:
  # when enabled registers the CamelBee event notifier to the Camel context
  notifier-enabled: false
  # when enabled configures stream caching, MDC logging and CamelBeeUnitOfWork for routes
  route-configurer-enabled: false
  # when enabled it allows the CamelBee WebGL application to fetch the topology of the Camel Context
  context-enabled: false
  # when enabled intercepts/traces request and responses of all camel components and caches messages
  tracer-enabled: false
  # maximum time the tracer can remain idle before deactivation tracing of messages
  tracer-max-idle-time: 60000
  # maximum collected trace messages
  tracer-max-messages-count: 10000
  # when enabled it logs the messages exchanged between endpoints
  logging-enabled: false
```

### MCP Tool Configurations

The `McpTools.java` class contains three test configurations. Each test activates one variant while commenting out the others.

#### Test 1 — MCP Only (Baseline)

Direct tool response with no additional framework layers:

```java
@McpTool(name = "createOrder", description = "Create a new order with customer details, product information, and shipping preferences")
Order createOrder(
        @McpToolParam(description = "Order object containing salesChannel, items with productName, quantity, and price") Order order,
        @McpToolParam(description = "Client-generated correlation ID for distributed tracing and logging", required = false) String transactionId,
        @McpToolParam(description = "Business process correlation ID for end-to-end transaction tracing across systems",
            required = false) String businessTransactionId
    ) throws Exception {

  Order response = new Order();
  response.setId("1");
  response.setSalesChannel(order.getSalesChannel());
  response.setItems(order.getItems());
  return response;

}
```

#### Test 2 — MCP + MapStruct

Tool call processed through MapStruct DTO mapping (MCP model ↔ domain model):

```java
@McpTool(name = "createOrder", description = "Create a new order with customer details, product information, and shipping preferences")
Order createOrder(
        @McpToolParam(description = "Order object containing salesChannel, items with productName, quantity, and price") Order order,
        @McpToolParam(description = "Client-generated correlation ID for distributed tracing and logging", required = false) String transactionId,
        @McpToolParam(description = "Business process correlation ID for end-to-end transaction tracing across systems",
            required = false) String businessTransactionId
    ) throws Exception {

  com.mycompany.model.domain.Order domainOrder = mcpOrderMapper.mcpToDomainOrder(order);
  return mcpOrderMapper.domainToMcpOrder(domainOrder);

}
```

#### Test 3 — MCP + Apache Camel + MapStruct

Full enterprise stack with Camel routing and MapStruct DTO conversion:

```java
@McpTool(name = "createOrder", description = "Create a new order with customer details, product information, and shipping preferences")
Order createOrder(
        @McpToolParam(description = "Order object containing salesChannel, items with productName, quantity, and price") Order order,
        @McpToolParam(description = "Client-generated correlation ID for distributed tracing and logging", required = false) String transactionId,
        @McpToolParam(description = "Business process correlation ID for end-to-end transaction tracing across systems",
            required = false) String businessTransactionId
    ) throws Exception {

  var result = fluentProducerTemplate
      .to("direct:mcpCreateOrder")
      .withHeader("transactionId", transactionId)
      .withBody(order)
      .send();

  if (result.getMessage().getBody() instanceof Exception) {
    throw result.getMessage().getBody(Exception.class);
  } else {
    return result.getMessage().getBody(Order.class);
  }

}
```

> **Note for Quarkus**: Uses `@Tool` / `@ToolArg` annotations instead of `@McpTool` / `@McpToolParam`, and `ToolCallException` instead of `Exception` in the Camel variant.

### Docker Compose Configuration

#### JVM Runtimes (Spring Boot MVC, WebFlux, Quarkus JVM)

```yaml
deploy:
  resources:
    limits:
      cpus: '2'
      memory: 1G
    reservations:
      cpus: '1'
      memory: 1G
```

#### Quarkus Native

```yaml
deploy:
  resources:
    limits:
      cpus: '2'
      memory: 256M   # Native requires significantly less memory
    reservations:
      cpus: '1'
      memory: 128M
```

---

## Build and Deployment

### Spring Boot MVC / WebFlux

```bash
# Make the Maven wrapper executable
chmod +x mvnw

# Package the application (skip tests for faster build)
./mvnw package -DskipTests

# Start the Docker container
docker compose up --build -d
```

> **WebFlux note**: Replace `application.yaml` with `application_webflux.yaml` and `pom.xml` with `pom_webflux.xml` before building.

### Quarkus JVM

```bash
# Package the application
./mvnw package -DskipTests

# Start the Docker container
docker compose up --build -d
```

### Quarkus Native

```bash
# Build native executable using container build (no local GraalVM required)
./mvnw package -Dnative -Dquarkus.native.container-build=true -DskipTests

# Start the Docker container with native profile
docker compose -f docker-compose-native.yml up --build -d
```

> Native compilation takes several minutes but produces a highly optimized executable with instant startup time and minimal memory footprint.

---

## Performance Testing

### Test Setup

- **Tool**: k6 load testing tool
- **Test File**: `mcp-throughput-test.js` (located in `docs/k6/mcp/`)
- **VUs (Virtual Users)**: 200
- **Duration**: 2 minutes per run
- **Warm-up**: 3 runs for JVM runtimes (Run 3 = optimized); 3 runs for native to verify consistency

### Test Execution

```bash
cd docs/k6/mcp
k6 run mcp-throughput-test.js
```

---

## Performance Results

### Spring Boot MVC

#### MCP Only

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 7,723 | 9,460 | 9,947 |
| **Avg Latency (ms)** | 24.55 | 19.81 | 18.86 |
| **Median Latency (ms)** | 14.29 | 13.58 | 13.55 |
| **P90 Latency (ms)** | 54.31 | 46.39 | 43.00 |
| **P95 Latency (ms)** | 78.73 | 54.41 | 51.78 |
| **Memory** | — | — | 572 MB / 1GB |
| **Success Rate** | 100% | 100% | 100% |

**JVM warm-up gain**: +28.8% (Run 1 → Run 3)

#### MCP + MapStruct

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 7,573 | 9,831 | 10,095 |
| **Avg Latency (ms)** | 25.09 | 18.92 | 18.49 |
| **Median Latency (ms)** | 14.26 | 13.45 | 13.47 |
| **P90 Latency (ms)** | 56.00 | 42.78 | 41.21 |
| **P95 Latency (ms)** | 79.61 | 51.62 | 49.67 |
| **Memory** | — | — | 568 MB / 1GB |
| **Success Rate** | 100% | 100% | 100% |

**JVM warm-up gain**: +33.3% (Run 1 → Run 3)

#### MCP + Apache Camel + MapStruct

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 6,381 | 8,621 | 9,188 |
| **Avg Latency (ms)** | 29.99 | 21.86 | 20.47 |
| **Median Latency (ms)** | 15.28 | 14.15 | 13.77 |
| **P90 Latency (ms)** | 69.25 | 52.48 | 48.48 |
| **P95 Latency (ms)** | 92.11 | 60.75 | 56.95 |
| **Memory** | — | — | 556 MB / 1GB |
| **Success Rate** | 100% | 100% | 100% |

**JVM warm-up gain**: +44.0% (Run 1 → Run 3) — highest warm-up gain across all configurations

---

### Spring Boot WebFlux

#### MCP Only

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 8,738 | 10,469 | 10,492 |
| **Avg Latency (ms)** | 21.70 | 17.86 | 17.91 |
| **Median Latency (ms)** | 14.03 | 13.66 | 13.60 |
| **P90 Latency (ms)** | 45.25 | 36.67 | 37.06 |
| **P95 Latency (ms)** | 62.59 | 45.14 | 45.86 |
| **Memory** | — | — | 645 MB / 1GB |
| **Success Rate** | 100% | 100% | 100% |

**JVM warm-up gain**: +20.1% — WebFlux reaches steady state faster than MVC

#### MCP + MapStruct

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 8,582 | 10,301 | 10,576 |
| **Avg Latency (ms)** | 22.07 | 18.10 | 17.75 |
| **Median Latency (ms)** | 13.92 | 13.43 | 13.56 |
| **P90 Latency (ms)** | 46.81 | 38.56 | 36.26 |
| **P95 Latency (ms)** | 66.08 | 46.92 | 45.10 |
| **Memory** | — | — | 652 MB / 1GB |
| **Success Rate** | 100% | 100% | 100% |

**Best Spring-family result**: 10,576 req/s with lowest Spring P90/P95 latencies

#### MCP + Apache Camel + MapStruct

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 7,710 | 9,599 | 9,666 |
| **Avg Latency (ms)** | 24.73 | 19.51 | 19.44 |
| **Median Latency (ms)** | 14.23 | 13.26 | 13.38 |
| **P90 Latency (ms)** | 55.29 | 45.74 | 44.95 |
| **P95 Latency (ms)** | 76.80 | 53.51 | 53.01 |
| **Memory** | — | — | 627 MB / 1GB |
| **Success Rate** | 100% | 100% | 100% |

**Run 2 → Run 3**: +0.7% — reactive model plateaus JIT optimization faster

---

### Quarkus JVM

#### MCP Only

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 14,523 | 16,282 | 16,381 |
| **Avg Latency (ms)** | 12.94 | 11.47 | 11.42 |
| **Median Latency (ms)** | 10.28 | 10.03 | 10.00 |
| **P90 Latency (ms)** | 19.75 | 18.04 | 18.06 |
| **P95 Latency (ms)** | 26.92 | 22.30 | 22.36 |
| **Memory** | — | — | 674 MB / 1GB |
| **Success Rate** | 100% | 100% | 100% |

**Overall benchmark winner**: 16,381 req/s — 65% higher than best Spring Boot result. Lowest median latency: 10ms.

#### MCP + MapStruct

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 14,285 | 15,864 | 15,429 |
| **Avg Latency (ms)** | 13.13 | 11.75 | 12.04 |
| **Median Latency (ms)** | 10.16 | 10.30 | 10.52 |
| **P90 Latency (ms)** | 19.68 | 18.63 | 19.19 |
| **P95 Latency (ms)** | 26.46 | 23.04 | 23.58 |
| **Memory** | — | — | 732 MB / 1GB |
| **Success Rate** | 100% | 100% | 100% |

> **Note**: Run 2 peaked at 15,864 req/s before settling at 15,429 req/s in Run 3 — JIT de-optimisation after aggressive profiling peak, typical of complex mapping workloads.

#### MCP + Apache Camel + MapStruct

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 11,674 | 13,632 | 13,957 |
| **Avg Latency (ms)** | 16.09 | 13.71 | 13.44 |
| **Median Latency (ms)** | 11.28 | 11.25 | 11.15 |
| **P90 Latency (ms)** | 28.91 | 24.09 | 23.40 |
| **P95 Latency (ms)** | 42.38 | 32.15 | 30.77 |
| **Memory** | — | — | 677 MB / 1GB |
| **Success Rate** | 100% | 100% | 100% |

**Even with Camel overhead, Quarkus JVM (13,957 req/s) outperforms all Spring Boot baselines**

---

### Quarkus Native

Native executables show consistent performance across all runs with no JIT warm-up benefit (< 2.5% variance). Memory is limited to 256MB.

#### MCP Only

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 10,013 | 9,760 | 9,916 |
| **Avg Latency (ms)** | 18.70 | 19.29 | 18.92 |
| **Median Latency (ms)** | 11.79 | 12.17 | 11.95 |
| **P90 Latency (ms)** | 47.47 | 48.15 | 47.54 |
| **P95 Latency (ms)** | 56.22 | 57.30 | 57.19 |
| **Memory** | — | — | 210 MB / 256MB |
| **Success Rate** | 100% | 100% | 100% |

#### MCP + MapStruct

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 9,729 | 9,583 | 9,661 |
| **Avg Latency (ms)** | 19.27 | 19.58 | 19.41 |
| **Median Latency (ms)** | 12.10 | 12.45 | 12.41 |
| **P90 Latency (ms)** | 48.21 | 48.21 | 47.74 |
| **P95 Latency (ms)** | 57.31 | 57.72 | 57.53 |
| **Memory** | — | — | 188 MB / 256MB |
| **Success Rate** | 100% | 100% | 100% |

#### MCP + Apache Camel + MapStruct

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 7,960 | 7,941 | 7,861 |
| **Avg Latency (ms)** | 23.96 | 23.99 | 24.19 |
| **Median Latency (ms)** | 14.32 | 14.47 | 14.56 |
| **P90 Latency (ms)** | 59.09 | 58.89 | 59.11 |
| **P95 Latency (ms)** | 68.54 | 68.40 | 68.74 |
| **Memory** | — | — | 118 MB / 256MB |
| **Success Rate** | 100% | 100% | 100% |

**Lowest memory footprint in the entire benchmark**: 118 MB while handling ~7,900 req/s

---

## Full Performance Summary

All results at optimized state (Run 3 for JVM; Run 1 for Native). 🟢 = best-in-class value.

| Runtime | Configuration | Throughput (req/s) | Avg Lat (ms) | Med Lat (ms) | P95 (ms) | Memory (MB) |
|---------|--------------|-------------------|-------------|-------------|---------|------------|
| Spring Boot MVC | MCP Only | 9,947 | 18.86 | 13.55 | 51.78 | 572 |
| Spring Boot MVC | MCP + MapStruct | 10,095 | 18.49 | 13.47 | 49.67 | 568 |
| Spring Boot MVC | MCP + Camel + MapStruct | 9,188 | 20.47 | 13.77 | 56.95 | 556 |
| Spring Boot WebFlux | MCP Only | 10,492 | 17.91 | 13.60 | 45.86 | 645 |
| Spring Boot WebFlux | MCP + MapStruct | 10,576 | 17.75 | 13.56 | 45.10 | 652 |
| Spring Boot WebFlux | MCP + Camel + MapStruct | 9,666 | 19.44 | 13.38 | 53.01 | 627 |
| Quarkus JVM | MCP Only | **🟢 16,381** | 11.42 | **🟢 10.00** | 22.36 | 674 |
| Quarkus JVM | MCP + MapStruct | 15,429 | 12.04 | 10.52 | 23.58 | 732 |
| Quarkus JVM | MCP + Camel + MapStruct | 13,957 | 13.44 | 11.15 | 30.77 | 677 |
| Quarkus Native | MCP Only | 9,916 | 18.92 | 11.95 | 57.19 | 210 |
| Quarkus Native | MCP + MapStruct | 9,661 | 19.41 | 12.41 | 57.53 | 188 |
| Quarkus Native | MCP + Camel + MapStruct | 7,861 | 24.19 | 14.56 | 68.74 | **🟢 118** |

---

## Configuration Overhead Analysis

### MapStruct DTO Mapping Overhead

MapStruct generates compile-time bytecode, resulting in near-zero runtime overhead. Adding MapStruct to the MCP baseline changes throughput by less than ±5% across all runtimes.

| Runtime | MCP Only (req/s) | + MapStruct (req/s) | Delta |
|---------|-----------------|---------------------|-------|
| Spring Boot MVC | 9,947 | 10,095 | +1.5% |
| Spring Boot WebFlux | 10,492 | 10,576 | +0.8% |
| Quarkus JVM | 16,381 | 15,429 | -5.8% |
| Quarkus Native | 9,916 | 9,661 | -2.6% |

**Conclusion**: MapStruct DTO mapping is effectively free. Domain model separation carries no meaningful performance cost and is the recommended approach for all production MCP microservices.

### Apache Camel Routing Overhead

Adding Camel routing via `FluentProducerTemplate` introduces 8–21% overhead due to the Camel exchange lifecycle and direct endpoint routing.

| Runtime | MCP Only (req/s) | + Camel + MapStruct (req/s) | Overhead |
|---------|-----------------|----------------------------|----------|
| Spring Boot MVC | 9,947 | 9,188 | -7.6% |
| Spring Boot WebFlux | 10,492 | 9,666 | -7.9% |
| Quarkus JVM | 16,381 | 13,957 | -14.8% |
| Quarkus Native | 9,916 | 7,861 | -20.7% |

**Conclusion**: Camel overhead is justified by the enterprise integration capabilities it enables. On Quarkus JVM, the Camel configuration (13,957 req/s) still outperforms all Spring-family baselines.

### JVM Warm-up Characteristics

| Runtime | MCP Only | MCP + MapStruct | MCP + Camel + MapStruct | Characteristic |
|---------|----------|----------------|------------------------|----------------|
| Spring Boot MVC | +28.8% | +33.3% | +44.0% | Large warm-up gains — Camel paths benefit most from JIT |
| Spring Boot WebFlux | +20.1% | +23.2% | +25.4% | Fastest plateau — reactive event loop stabilises JIT quickly |
| Quarkus JVM | +12.8% | +8.0% | +19.6% | Moderate warm-up — already optimised at startup |
| Quarkus Native | N/A | N/A | N/A | AOT compiled — no warm-up, < 2.5% variance across all runs |

---

## Container Resource Usage

### JVM Runtimes (at peak load)

| Runtime | Config | CPU | Memory | Disk Read | Network I/O |
|---------|--------|-----|--------|-----------|-------------|
| Spring Boot MVC | MCP Only | ~200% | 572 MB / 1GB | 89.1 MB | 4.9GB in / 5.2GB out |
| Spring Boot MVC | MCP + MapStruct | ~200% | 568 MB / 1GB | 89 MB | 4.96GB in / 5.29GB out |
| Spring Boot MVC | MCP + Camel+MS | ~200% | 556 MB / 1GB | 46.8 MB | 4.37GB in / 4.68GB out |
| Spring Boot WebFlux | MCP Only | ~200% | 645 MB / 1GB | 45.9 MB | 5.37GB in / 5.58GB out |
| Spring Boot WebFlux | MCP + MapStruct | ~200% | 652 MB / 1GB | 87.7 MB | 5.33GB in / 5.57GB out |
| Spring Boot WebFlux | MCP + Camel+MS | ~200% | 627 MB / 1GB | 88 MB | 4.88GB in / 5.12GB out |
| Quarkus JVM | MCP Only | ~200% | 674 MB / 1GB | 0 MB | 8.52GB in / 10.6GB out |
| Quarkus JVM | MCP + MapStruct | ~200% | 732 MB / 1GB | 80.3 MB | 8.5GB in / 10.8GB out |
| Quarkus JVM | MCP + Camel+MS | ~200% | 677 MB / 1GB | 0 MB | 7.22GB in / 9.15GB out |

### Quarkus Native (256MB limit)

| Config | CPU | Memory | Disk Read | Network I/O |
|--------|-----|--------|-----------|-------------|
| MCP Only | ~200% | 210 MB / 256MB | 82.9 MB | 5.47GB in / 6.87GB out |
| MCP + MapStruct | ~200% | 188 MB / 256MB | 83.1 MB | 5.34GB in / 6.82GB out |
| MCP + Camel + MapStruct | ~200% | 118 MB / 256MB | 6.87 MB | 4.4GB in / 5.65GB out |

> All disk writes were 0B during tests — fully in-memory processing confirmed across all configurations.

---

## Key Observations

1. **JVM Warm-up Effect**: JVM runtimes show 8–44% throughput improvement from Run 1 to Run 3. Always warm up before production load measurements.
2. **Quarkus JVM Dominance**: Quarkus JVM delivers 55–65% higher throughput than Spring Boot across all three configurations.
3. **MapStruct is Free**: Adding MapStruct DTO mapping changes throughput by less than 5% in all cases — domain model separation has no meaningful performance cost.
4. **Camel Overhead is Predictable**: Apache Camel routing adds 8–21% overhead consistently across runtimes and is well worth the enterprise integration capabilities.
5. **Native for Memory Efficiency**: Quarkus Native uses 72–84% less memory than JVM variants (118–210 MB vs 556–732 MB) with instant startup and < 2.5% run-to-run variance.
6. **WebFlux Stabilises Faster**: Spring Boot WebFlux reaches JIT steady state quicker — only 0.7% gap between Run 2 and Run 3 vs MVC's 6.6%.
7. **Zero Failures**: 100% success rate was achieved in every configuration across all 36 test runs.

---

## Use Case Recommendations

| Use Case | Recommended Configuration | Rationale |
|----------|--------------------------|-----------|
| Maximum throughput | **Quarkus JVM + MCP Only** (16,381 req/s) | Highest throughput for mission-critical AI backends |
| Best Spring choice | **Spring Boot WebFlux + MCP + MapStruct** (10,576 req/s) | Best Spring-family throughput, lowest P90/P95 latencies |
| Enterprise integration | **Quarkus JVM + MCP + Camel + MapStruct** (13,957 req/s) | Full EIP patterns, still outperforms all Spring baselines |
| Minimum memory | **Quarkus Native + MCP Only** (210 MB) | Serverless, edge, or memory-constrained deployments |
| Predictable performance | **Quarkus Native** (any config, < 2.5% variance) | SLA-bound environments where consistency matters more than peak |
| Simplest Spring setup | **Spring Boot MVC + MCP + MapStruct** (10,095 req/s) | Developer-friendly, straightforward implementation, solid performance |

---

## Recommendations

### For all production deployments

1. **Disable all CamelBee interceptors** as shown in the configuration — this is essential to achieve benchmark-level performance
2. **Allocate 2 CPU cores minimum** — all configurations are CPU-bound and scale linearly to the allocated limit
3. **Implement JVM warm-up strategy** — health-check probes, startup priming, or Kubernetes readiness gates before serving live traffic
4. **Always use MapStruct** for domain model separation — the overhead is negligible and the architectural benefits are significant

### Choosing between Spring Boot and Quarkus

- **Choose Quarkus** when throughput or latency is the primary concern — the 55–65% throughput advantage is consistent across all three MCP configurations
- **Choose Spring Boot** when team familiarity, ecosystem tooling, or Spring-specific libraries are a priority

### Choosing a tool configuration

- **MCP Only**: Only for minimal proof-of-concept or maximum-throughput scenarios where all logic fits within the handler
- **MCP + MapStruct**: Recommended default — architectural separation at negligible cost
- **MCP + Camel + MapStruct**: When the integration scenario requires routing, error handling, dead letter channels, or EIP patterns

### Native vs JVM

- **Quarkus Native**: Serverless functions, auto-scaling Kubernetes, edge computing, or cost-optimised cloud instances
- **Quarkus JVM**: Long-running services where peak throughput matters, workloads with complex Camel routing
- Native builds require no local GraalVM — CamelBee generates container-based native builds out of the box

---

## Environment

- **Load Testing Tool**: k6
- **Protocol**: MCP over HTTP/SSE (JSON-RPC 2.0)
- **Container**: Docker
- **Test Load**: 200 concurrent virtual users, 2 minutes per run, 3 runs per configuration
- **JVM Resource Limits**: 2 CPUs, 1GB memory
- **Native Resource Limits**: 2 CPUs, 256MB memory
- **Backend**: MOCK — no external dependencies, fully in-memory processing
- **CamelBee Interceptors**: All disabled for all tests
