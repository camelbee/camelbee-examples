# REST with JSON Performance Testing - CamelBee Microservice (Spring Boot)

This document outlines the configuration, setup, and performance test results for a CamelBee-generated REST microservice using JSON serialization on Spring Boot.

## Microservice Creation

The microservice was created using the [CamelBee Initializer](https://www.camelbee.io) with the following configuration:

- **Interface**: REST with JSON serialization
- **Runtime**: Spring Boot
- **Backend**: MOCK (no external dependencies)

After generating and downloading the microservice from CamelBee, apply the following configurations for optimal performance testing.

## Configuration

### Application Configuration

After creating the microservice, update the `application.yml` with the following settings to disable all interceptors:

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

### Docker Compose Configuration

Update the CPU allocation in `docker-compose.yml` to **2 cores** for optimal performance:

```yaml
deploy:
  resources:
    limits:
      cpus: '2'      # Updated from default to 2 cores
      memory: 1G
    reservations:
      cpus: '1'
      memory: 1G
```

> **Note**: Increasing the CPU limit to 2 cores significantly improves throughput and allows the microservice to handle higher concurrent loads efficiently.

## Build and Deployment

### Build Steps

```bash
# Make the Maven wrapper executable
chmod +x mvnw

# Package the application (skip tests for faster build)
./mvnw package -DskipTests

# Start the Docker container
docker compose up --build -d
```

## Performance Testing

### Test Setup

- **Tool**: k6 load testing tool
- **Test File**: `rest-throughput-test.js` (located in `docs/k6/grpc/`)
- **Protocol**: REST with JSON serialization
- **VUs (Virtual Users)**: 200
- **Duration**: 2 minutes per run
- **Warm-up**: 2 test runs to ensure JVM optimization, 3rd run for final results

### Test Execution

```bash
cd docs/k6/grpc
k6 run rest-throughput-test.js
```

## Performance Results

### Run 1 - Initial Warm-up

```
Duration: 2m01.6s
✓ checks.........................: 100.00% ✓ 623,900  ✗ 0
  data_received..................: 963 MB  7.9 MB/s
  data_sent......................: 654 MB  5.4 MB/s
  dropped_iterations.............: 23,761  195.37/s
✓ http_req_duration..............: avg=38.62ms  min=510µs    med=14.16ms  max=3.49s
                                   p(90)=82.19ms  p(95)=96.59ms
  http_req_failed................: 0.00%   ✓ 0  ✗ 623,900
  http_req_receiving.............: avg=121.44µs min=4µs      med=8µs      max=106.5ms
  http_req_sending...............: avg=5.35µs   min=1µs      med=3µs      max=17.11ms
  http_req_waiting...............: avg=38.49ms  min=497µs    med=14.09ms  max=3.49s
  http_reqs......................: 623,900  5,129.99/s
  iteration_duration.............: avg=3.88s    min=1.59s    med=2.91s    max=23.6s
  iterations.....................: 6,239   51.30/s
```

**Throughput**: ~5,130 requests/second

**Screenshot of the result:**

![img.png](docs/images/img.png)

---

### Run 2 - JVM Optimization Phase

```
Duration: 2m01.6s
✓ checks.........................: 100.00% ✓ 840,800  ✗ 0
  data_received..................: 1.3 GB  11 MB/s
  data_sent......................: 881 MB  7.2 MB/s
  dropped_iterations.............: 21,592  177.51/s
✓ http_req_duration..............: avg=28.74ms  min=476µs    med=13.13ms  max=328.96ms
                                   p(90)=72.43ms  p(95)=76.03ms
  http_req_failed................: 0.00%   ✓ 0  ✗ 840,800
  http_req_receiving.............: avg=88.83µs  min=4µs      med=8µs      max=77.1ms
  http_req_sending...............: avg=5.02µs   min=1µs      med=3µs      max=25.84ms
  http_req_waiting...............: avg=28.64ms  min=415µs    med=13.08ms  max=328.95ms
  http_reqs......................: 840,800  6,912.30/s
  iteration_duration.............: avg=2.88s    min=1.63s    med=2.89s    max=3.54s
  iterations.....................: 8,408   69.12/s
```

**Throughput**: ~6,912 requests/second
**Improvement**: +34.7% from Run 1

**Screenshot of the result:**

![img_1.png](docs/images/img_1.png)

---

### Run 3 - Optimized Performance

```
Duration: 2m01.5s
✓ checks.........................: 100.00% ✓ 873,300  ✗ 0
  data_received..................: 1.3 GB  11 MB/s
  data_sent......................: 915 MB  7.5 MB/s
  dropped_iterations.............: 21,267  175.04/s
✓ http_req_duration..............: avg=27.64ms  min=419µs    med=12.55ms  max=369.41ms
                                   p(90)=71.58ms  p(95)=74.97ms
  http_req_failed................: 0.00%   ✓ 0  ✗ 873,300
  http_req_receiving.............: avg=85.02µs  min=4µs      med=8µs      max=83ms
  http_req_sending...............: avg=5.07µs   min=1µs      med=3µs      max=24.93ms
  http_req_waiting...............: avg=27.55ms  min=407µs    med=12.5ms   max=369.4ms
  http_reqs......................: 873,300  7,187.95/s
  iteration_duration.............: avg=2.77s    min=1.49s    med=2.79s    max=3.57s
  iterations.....................: 8,733   71.88/s
```

**Throughput**: ~7,188 requests/second
**Improvement**: +40.1% from Run 1, +4.0% from Run 2

**Screenshot of the result:**

![img_2.png](docs/images/img_2.png)

---

## Performance Summary

| Metric | Run 1 | Run 2 | Run 3 |
|--------|-------|-------|-------|
| **Throughput (req/s)** | 5,130 | 6,912 | 7,188 |
| **Avg Latency (ms)** | 38.62 | 28.74 | 27.64 |
| **Median Latency (ms)** | 14.16 | 13.13 | 12.55 |
| **P90 Latency (ms)** | 82.19 | 72.43 | 71.58 |
| **P95 Latency (ms)** | 96.59 | 76.03 | 74.97 |
| **Max Latency (ms)** | 3,490 | 328.96 | 369.41 |
| **Total Requests** | 623,900 | 840,800 | 873,300 |
| **Success Rate** | 100% | 100% | 100% |

## Key Observations

1. **Dramatic JVM Warm-up Effect**: Performance improved by 40% after warm-up, demonstrating effective JIT compilation
2. **Strong Throughput**: Achieved stable ~7.2K requests/second after warm-up
3. **Low Latency**: Median latency consistently around 12-13ms
4. **Zero Failures**: 100% success rate across all test runs
5. **JSON Serialization**: Text-based JSON serialization provides good performance with human-readable payloads
6. **Improved Stability**: Max latency improved dramatically from 3.49s to ~370ms after warm-up
7. **Higher Data Volume**: JSON payloads are larger than Protocol Buffers, resulting in higher network throughput (11 MB/s received)

## Container Resource Usage

During the performance tests, the container exhibited the following resource utilization patterns:

- **CPU Usage**: Peak at ~210% (utilizing 2 CPU cores effectively), averaging ~0.27% during idle periods
- **Memory Usage**: Stable at ~628.6MB out of 1GB allocated (62.9% utilization)
- **Disk Read/Write**: 86MB read / 0B write
- **Network I/O**: 2.78GB received / 4.01GB sent across all three test runs

The CPU graph shows three clear spikes corresponding to each test run, demonstrating efficient utilization of the allocated resources. Memory usage showed an initial spike during startup, then stabilized at a consistent level during the test runs. The higher network throughput reflects the larger payload sizes of JSON compared to binary formats.

**Screenshot of Docker container statistics:**

![img_3.png](docs/images/img_3.png)

## Recommendations

1. **Production Deployment**: Disable all CamelBee interceptors as shown in the configuration for optimal performance
2. **Resource Allocation**: 2 CPU cores and 1GB memory provides excellent performance for this workload
3. **Warm-up Strategy**: Perform at least 2 warm-up runs before measuring production performance to allow JIT compilation to optimize
4. **Monitoring**: Track P95 and P99 latencies in production to catch performance degradation early
5. **Serialization Choice**: Use JSON when human-readable payloads and broad compatibility are priorities; consider Protocol Buffers for better performance and smaller payloads
6. **Memory Considerations**: JSON processing uses slightly less memory (~629MB) compared to Protocol Buffers on Spring Boot

## Environment

- **Runtime**: Spring Boot with Apache Camel
- **Protocol**: REST (HTTP/1.1) with JSON serialization
- **Container**: Docker
- **Resource Limits**: 2 CPUs, 1GB memory
- **Test Load**: 200 concurrent virtual users
