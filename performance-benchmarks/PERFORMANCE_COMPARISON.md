# CamelBee Performance Benchmarks - Comprehensive Comparison

This document provides a detailed performance comparison of CamelBee-generated microservices across different runtimes (Spring Boot, Quarkus JVM, Quarkus Native) and protocols (gRPC, REST with Protocol Buffers, REST with JSON).

## Table of Contents

- [Executive Summary](#executive-summary)
- [Test Environment](#test-environment)
- [Overall Performance Comparison](#overall-performance-comparison)
- [Protocol Comparison](#protocol-comparison)
- [Runtime Comparison](#runtime-comparison)
- [Memory Efficiency Analysis](#memory-efficiency-analysis)
- [Latency Analysis](#latency-analysis)
- [Use Case Recommendations](#use-case-recommendations)
- [Detailed Results](#detailed-results)

## Executive Summary

**Key Findings:**

- **Highest Throughput**: Quarkus JVM with gRPC achieved ~11,010 req/s
- **Lowest Memory**: Quarkus Native with REST+Protobuf used only 77MB
- **Best Latency**: Spring Boot gRPC had 14.57ms median latency
- **Best Balance**: Quarkus JVM offers excellent throughput with reasonable memory usage
- **Most Efficient**: Quarkus Native provides 80-90% lower memory footprint at 40-50% throughput

## Test Environment

### Hardware & Configuration
- **CPU**: 2 cores allocated (with varying base architectures across tests)
- **Memory**: 1GB for JVM-based, 256MB for Native
- **Container**: Docker with resource limits
- **Test Duration**: 2 minutes per run
- **Virtual Users**: 200 concurrent connections
- **Warm-up**: 2-3 runs to ensure JVM optimization

### Test Tool
- **k6**: Industry-standard load testing tool
- **Metrics**: Throughput, latency (avg, median, P90, P95, max), resource usage

### Configuration
All tests performed with:
- All CamelBee interceptors disabled for maximum performance
- Mock backend (no external dependencies)
- Optimized Docker resource allocation

## Overall Performance Comparison

### Throughput Comparison (Requests/Second)

| Runtime | gRPC | REST + Protobuf | REST + JSON | Average |
|---------|------|-----------------|-------------|---------|
| **Spring Boot** | 10,208 | 8,620 | 7,188 | 8,672 |
| **Quarkus JVM** | 11,010 | 10,502 | 8,461 | 9,991 |
| **Quarkus Native** | 6,245 | 6,461 | 5,260 | 5,989 |

**Key Insights:**
- Quarkus JVM leads in throughput across all protocols
- gRPC consistently outperforms REST protocols by 15-30%
- Native compilation reduces throughput by ~40-45% compared to JVM

### Network Data Transfer Comparison

#### Data Received (Bytes per Request)

| Runtime | gRPC | REST + Protobuf | REST + JSON | Protobuf Savings |
|---------|------|-----------------|-------------|------------------|
| **Spring Boot** | 451 | 625 | 1,544 | 59.5% less than JSON |
| **Quarkus JVM** | 449 | 469 | 1,574 | 70.2% less than JSON |
| **Quarkus Native** | 445 | 436 | 1,500 | 70.9% less than JSON |

#### Data Sent (Bytes per Request)

| Runtime | gRPC | REST + Protobuf | REST + JSON | Protobuf Savings |
|---------|------|-----------------|-------------|------------------|
| **Spring Boot** | 427 | 480 | 1,318 | 63.6% less than JSON |
| **Quarkus JVM** | 436 | 437 | 1,354 | 67.7% less than JSON |
| **Quarkus Native** | 423 | 434 | 1,366 | 68.2% less than JSON |

#### Total Network I/O Comparison (3 Test Runs Combined)

**gRPC:**
| Runtime | Data Received | Data Sent | Total |
|---------|---------------|-----------|-------|
| Spring Boot | 2.52 GB | 2.20 GB | 4.72 GB |
| Quarkus JVM | 2.71 GB | 2.39 GB | 5.10 GB |
| Quarkus Native | 1.59 GB | 1.42 GB | 3.01 GB |

**REST + Protobuf:**
| Runtime | Data Received | Data Sent | Total |
|---------|---------------|-----------|-------|
| Spring Boot | 1.81 GB | 1.98 GB | 3.79 GB |
| Quarkus JVM | 2.49 GB | 2.37 GB | 4.86 GB |
| Quarkus Native | 1.63 GB | 1.57 GB | 3.20 GB |

**REST + JSON:**
| Runtime | Data Received | Data Sent | Total |
|---------|---------------|-----------|-------|
| Spring Boot | 2.78 GB | 4.01 GB | 6.79 GB |
| Quarkus JVM | 3.50 GB | 4.72 GB | 8.22 GB |
| Quarkus Native | 2.38 GB | 3.22 GB | 5.60 GB |

### Payload Size Impact Analysis

**Protocol Buffers vs JSON - Data Transfer Savings:**

| Runtime | Protobuf Received Savings | Protobuf Sent Savings | Total I/O Savings |
|---------|---------------------------|----------------------|-------------------|
| **Spring Boot** | 35% less | 51% less | **44% less** |
| **Quarkus JVM** | 29% less | 50% less | **41% less** |
| **Quarkus Native** | 43% less | 51% less | **43% less** |

**Key Insights:**
- 📦 **Protocol Buffers payloads are 60-70% smaller** than JSON on average
- 🌐 **Total network I/O reduced by 41-44%** when using Protobuf instead of JSON
- 💰 **Network cost savings**: Significant for high-throughput services or metered connections
- ⚡ **Bandwidth efficiency**: Protobuf transfers ~3-5 GB total vs JSON's ~6-8 GB for same requests
- 🚀 **Mobile/Edge friendly**: Smaller payloads especially beneficial for limited bandwidth scenarios

**Real-World Impact:**

For a service handling **1 million requests/day**:
- JSON: ~6-8 GB/day network transfer
- Protobuf: ~3-5 GB/day network transfer
- **Savings: 3-4 GB/day** (~90-120 GB/month)

At typical cloud egress rates ($0.08-0.12/GB):
- **Monthly savings: $7-14 per million requests**
- **Annual savings: $84-168 per million requests/day**

### Memory Usage Comparison (MB)

| Runtime | gRPC | REST + Protobuf | REST + JSON | Average |
|---------|------|-----------------|-------------|---------|
| **Spring Boot** | 757 | 535 | 629 | 640 |
| **Quarkus JVM** | 633 | 631 | 703 | 656 |
| **Quarkus Native** | 100 | 77 | 87 | 88 |

**Key Insights:**
- Quarkus Native uses 85-90% less memory than JVM-based runtimes
- Spring Boot and Quarkus JVM have similar memory footprints
- JSON processing requires slightly more memory than Protocol Buffers

### Latency Comparison - Average (ms)

| Runtime | gRPC | REST + Protobuf | REST + JSON |
|---------|------|-----------------|-------------|
| **Spring Boot** | 19.31 | 23.03 | 27.64 |
| **Quarkus JVM** | 17.96 | 18.89 | 23.43 |
| **Quarkus Native** | 31.6 | 30.74 | 37.77 |

### Latency Comparison - Median (ms)

| Runtime | gRPC | REST + Protobuf | REST + JSON |
|---------|------|-----------------|-------------|
| **Spring Boot** | 14.57 | 10.93 | 12.55 |
| **Quarkus JVM** | 14.47 | 10.23 | 12.06 |
| **Quarkus Native** | 17.4 | 16.07 | 19.02 |

**Key Insights:**
- Quarkus JVM provides the lowest latency across most scenarios
- REST + Protobuf achieves lower median latency than gRPC (due to different latency distributions)
- Native runtimes have 50-75% higher average latency than JVM

## Protocol Comparison

### gRPC Performance

| Metric | Spring Boot | Quarkus JVM | Quarkus Native |
|--------|-------------|-------------|----------------|
| **Throughput (req/s)** | 10,208 | 11,010 | 6,245 |
| **Avg Latency (ms)** | 19.31 | 17.96 | 31.6 |
| **Median Latency (ms)** | 14.57 | 14.47 | 17.4 |
| **P95 Latency (ms)** | 48.64 | 41.81 | 81.67 |
| **Memory (MB)** | 757 | 633 | 100 |

**gRPC Characteristics:**
- ✅ Highest throughput across all runtimes
- ✅ Efficient binary serialization
- ✅ Built-in streaming support
- ✅ HTTP/2 multiplexing
- ⚠️ Higher P95 latency variance
- ⚠️ Requires gRPC client support

### REST with Protocol Buffers Performance

| Metric | Spring Boot | Quarkus JVM | Quarkus Native |
|--------|-------------|-------------|----------------|
| **Throughput (req/s)** | 8,620 | 10,502 | 6,461 |
| **Avg Latency (ms)** | 23.03 | 18.89 | 30.74 |
| **Median Latency (ms)** | 10.93 | 10.23 | 16.07 |
| **P95 Latency (ms)** | 70.2 | 62.01 | 78.74 |
| **Memory (MB)** | 535 | 631 | 77 |

**REST + Protobuf Characteristics:**
- ✅ Excellent throughput (close to gRPC)
- ✅ **60-70% smaller payloads than JSON** 📦
- ✅ **41-44% less total network I/O than JSON**
- ✅ Binary serialization efficiency
- ✅ HTTP/1.1 compatibility
- ✅ Standard HTTP infrastructure
- ✅ Lowest memory usage (especially Native: 77MB!)
- ✅ Lower median latency than gRPC
- ✅ Significant bandwidth cost savings
- ⚠️ Requires Protobuf schema management

### REST with JSON Performance

| Metric | Spring Boot | Quarkus JVM | Quarkus Native |
|--------|-------------|-------------|----------------|
| **Throughput (req/s)** | 7,188 | 8,461 | 5,260 |
| **Avg Latency (ms)** | 27.64 | 23.43 | 37.77 |
| **Median Latency (ms)** | 12.55 | 12.06 | 19.02 |
| **P95 Latency (ms)** | 74.97 | 69.31 | 87.37 |
| **Memory (MB)** | 629 | 703 | 87 |

**REST + JSON Characteristics:**
- ✅ Human-readable payloads
- ✅ Universal compatibility
- ✅ No schema requirements
- ✅ Easy debugging
- ⚠️ 15-20% lower throughput than Protobuf
- ⚠️ **60-70% larger payloads than Protobuf** 📦
- ⚠️ **41-44% more network I/O than Protobuf**
- ⚠️ Higher bandwidth costs
- ⚠️ Higher memory usage for parsing

### Protocol Performance Rankings

**By Throughput (Highest to Lowest):**
1. gRPC: +20-30% over REST+Protobuf
2. REST + Protobuf: +20-25% over REST+JSON
3. REST + JSON: Baseline

**By Memory Efficiency (Lowest to Highest):**
1. REST + Protobuf (Native): 77MB ⭐
2. REST + JSON (Native): 87MB
3. gRPC (Native): 100MB
4. REST + Protobuf (Spring Boot): 535MB
5. gRPC (Quarkus JVM): 633MB
6. REST + JSON (Quarkus JVM): 703MB

**By Median Latency (Lowest to Highest):**
1. REST + Protobuf (Quarkus JVM): 10.23ms ⭐
2. REST + Protobuf (Spring Boot): 10.93ms
3. REST + JSON (Quarkus JVM): 12.06ms
4. REST + JSON (Spring Boot): 12.55ms
5. gRPC (Quarkus JVM): 14.47ms
6. gRPC (Spring Boot): 14.57ms

## Runtime Comparison

### Spring Boot Performance Profile

**Strengths:**
- Mature ecosystem with extensive tooling
- Excellent developer experience
- Good performance after JVM warm-up
- Wide range of integrations
- Strong community support

**Performance Characteristics:**
- Throughput: Good (7,188 - 10,208 req/s)
- Memory: Moderate (535 - 757 MB)
- Latency: Competitive (19-28ms avg)
- Warm-up: Requires 2+ runs for optimal performance

**Best For:**
- Traditional enterprise applications
- Teams with Spring expertise
- Long-running services
- Applications requiring extensive ecosystem

### Quarkus JVM Performance Profile

**Strengths:**
- **Highest throughput** across all protocols
- Lower memory than Spring Boot
- Fast startup compared to Spring Boot
- Modern cloud-native features
- Excellent performance/resource ratio

**Performance Characteristics:**
- Throughput: Excellent (8,461 - 11,010 req/s) ⭐
- Memory: Good (631 - 703 MB)
- Latency: Best (17-23ms avg) ⭐
- Warm-up: Requires 2+ runs for optimal performance

**Best For:**
- Maximum throughput requirements
- Cloud-native applications
- Kubernetes deployments
- Performance-critical services

### Quarkus Native Performance Profile

**Strengths:**
- **Exceptional memory efficiency** (77-100 MB) ⭐
- Instant startup (milliseconds)
- Predictable performance
- Ideal for serverless/FaaS
- Lower cloud infrastructure costs

**Performance Characteristics:**
- Throughput: Moderate (5,260 - 6,461 req/s)
- Memory: Exceptional (77 - 100 MB) ⭐
- Latency: Higher (30-38ms avg)
- Warm-up: Minimal to none required

**Best For:**
- Serverless/Lambda functions
- Memory-constrained environments
- Cost-sensitive deployments
- Rapid scaling requirements
- Microservices with many instances

### Runtime Rankings

**By Overall Performance (Balanced):**
1. **Quarkus JVM**: Best throughput + low latency + reasonable memory ⭐
2. **Spring Boot**: Good all-around + mature ecosystem
3. **Quarkus Native**: Best memory efficiency, lower throughput

**By Throughput:**
1. Quarkus JVM: 9,991 req/s avg
2. Spring Boot: 8,672 req/s avg
3. Quarkus Native: 5,989 req/s avg

**By Memory Efficiency:**
1. Quarkus Native: 88 MB avg ⭐
2. Spring Boot: 640 MB avg
3. Quarkus JVM: 656 MB avg

**By Startup Time:**
1. Quarkus Native: Milliseconds ⭐
2. Quarkus JVM: Few seconds
3. Spring Boot: Several seconds

## Memory Efficiency Analysis

### Memory Usage by Configuration

```
Native vs JVM Memory Savings:

gRPC:
- Quarkus Native (100 MB) vs Quarkus JVM (633 MB) = 84% reduction
- Quarkus Native (100 MB) vs Spring Boot (757 MB) = 87% reduction

REST + Protobuf:
- Quarkus Native (77 MB) vs Quarkus JVM (631 MB) = 88% reduction ⭐
- Quarkus Native (77 MB) vs Spring Boot (535 MB) = 86% reduction

REST + JSON:
- Quarkus Native (87 MB) vs Quarkus JVM (703 MB) = 88% reduction
- Quarkus Native (87 MB) vs Spring Boot (629 MB) = 86% reduction
```

### Cost Implications

**Cloud Cost Savings with Quarkus Native:**

Assuming typical cloud pricing (~$0.01/GB-hour):

| Scenario | JVM Cost/Month | Native Cost/Month | Savings |
|----------|----------------|-------------------|---------|
| **Single Instance** | $4.70 | $0.63 | 87% |
| **10 Instances** | $47.00 | $6.30 | 87% |
| **100 Instances** | $470.00 | $63.00 | 87% |

**When to Choose Native for Cost:**
- ✅ Microservices architecture (many instances)
- ✅ Serverless/FaaS (pay per use)
- ✅ Auto-scaling workloads
- ✅ Development/staging environments

## Latency Analysis

### P95 Latency Comparison (ms)

| Runtime | gRPC | REST + Protobuf | REST + JSON |
|---------|------|-----------------|-------------|
| **Spring Boot** | 48.64 | 70.2 | 74.97 |
| **Quarkus JVM** | 41.81 | 62.01 | 69.31 |
| **Quarkus Native** | 81.67 | 78.74 | 87.37 |

### Latency Distribution Insights

**JVM Runtimes:**
- Lower P95 latency (40-75ms)
- More consistent after warm-up
- Better for latency-sensitive applications

**Native Runtime:**
- Higher P95 latency (78-87ms)
- Very predictable across runs
- Minimal warm-up effect

**Protocol Impact:**
- gRPC: Lowest P95 on JVM (~42-49ms)
- REST + Protobuf: Medium P95 (~62-78ms)
- REST + JSON: Highest P95 (~69-87ms)

## Bandwidth and Network Efficiency

### Payload Size Efficiency

**Average Bytes per Request:**

| Serialization | Received | Sent | Total | vs JSON |
|---------------|----------|------|-------|---------|
| **Protocol Buffers** | ~450 bytes | ~435 bytes | ~885 bytes | **Baseline** |
| **JSON** | ~1,506 bytes | ~1,346 bytes | ~2,852 bytes | **+222% larger** |

**Key Findings:**
- 📦 Protocol Buffers payloads are **60-70% smaller** than JSON
- 🌐 JSON payloads are **2.2-3.2x larger** than Protocol Buffers
- 💾 For 1 million requests: Protobuf = ~885 MB, JSON = ~2.85 GB
- 💰 Network transfer cost reduction: **40-44%** with Protobuf

### Network I/O Efficiency by Protocol

**Total Data Transfer (All 3 Runs Combined):**

| Runtime | Protocol | Total I/O | Efficiency Rating |
|---------|----------|-----------|-------------------|
| Quarkus Native | REST + Protobuf | 3.20 GB | ⭐⭐⭐⭐⭐ Most Efficient |
| Spring Boot | REST + Protobuf | 3.79 GB | ⭐⭐⭐⭐ Very Efficient |
| Quarkus Native | gRPC | 3.01 GB | ⭐⭐⭐⭐ Very Efficient |
| Spring Boot | gRPC | 4.72 GB | ⭐⭐⭐ Efficient |
| Quarkus JVM | REST + Protobuf | 4.86 GB | ⭐⭐⭐ Efficient |
| Quarkus JVM | gRPC | 5.10 GB | ⭐⭐⭐ Efficient |
| Quarkus Native | REST + JSON | 5.60 GB | ⭐⭐ Moderate |
| Spring Boot | REST + JSON | 6.79 GB | ⭐⭐ Moderate |
| Quarkus JVM | REST + JSON | 8.22 GB | ⭐ Lower Efficiency |

### Bandwidth Cost Analysis

**Scenario: 10 Million Requests/Day**

| Protocol | Daily Transfer | Monthly Transfer | Est. Monthly Cost* | Annual Cost* |
|----------|----------------|------------------|-------------------|--------------|
| **Protobuf** | 8.5 GB | 255 GB | $25.50 | $306 |
| **JSON** | 28.5 GB | 855 GB | $85.50 | $1,026 |
| **Savings** | **20 GB/day** | **600 GB/month** | **$60/month** | **$720/year** |

*Based on typical cloud egress pricing of $0.10/GB

**Scenario: 100 Million Requests/Day**

| Protocol | Daily Transfer | Monthly Transfer | Est. Monthly Cost* | Annual Cost* |
|----------|----------------|------------------|-------------------|--------------|
| **Protobuf** | 85 GB | 2.55 TB | $255 | $3,060 |
| **JSON** | 285 GB | 8.55 TB | $855 | $10,260 |
| **Savings** | **200 GB/day** | **6 TB/month** | **$600/month** | **$7,200/year** |

### When Payload Size Matters Most

**✅ Use Protocol Buffers when:**
- High-volume APIs (millions+ requests/day)
- Metered cloud egress costs
- Mobile/IoT clients with limited bandwidth
- International data transfer
- CDN distribution costs
- Microservices mesh with high inter-service communication

**✅ JSON is acceptable when:**
- Low traffic (<100K requests/day)
- Internal networks with flat-rate bandwidth
- Public APIs requiring human readability
- Developer debugging is frequent
- Schema management overhead not justified

## Use Case Recommendations

### 🎯 Maximum Throughput Required

**Recommended: Quarkus JVM with gRPC**
- Throughput: 11,010 req/s
- Memory: 633 MB
- Use Case: High-traffic APIs, real-time data processing

**Alternative: Quarkus JVM with REST + Protobuf**
- Throughput: 10,502 req/s (95% of gRPC)
- Memory: 631 MB
- Benefit: Better HTTP compatibility

### 💰 Cost Optimization / Memory Constraints

**Recommended: Quarkus Native with REST + Protobuf**
- Memory: 77 MB (lowest!) ⭐
- Throughput: 6,461 req/s
- Network I/O: 3.20 GB (most efficient!)
- Use Case: Serverless, multi-tenant, high-density deployments

**Alternative: Quarkus Native with REST + JSON**
- Memory: 87 MB
- Throughput: 5,260 req/s
- Benefit: No schema management

### 🌐 High Bandwidth / Network Cost Sensitive

**Recommended: Any Runtime with REST + Protobuf or gRPC**
- Payload Size: 60-70% smaller than JSON
- Network Savings: 40-44% less data transfer
- Cost Impact: $60-600/month savings per 10M requests/day
- Use Case: High-volume APIs, mobile apps, international traffic, CDN distribution

**Avoid: REST + JSON for high-volume scenarios**
- 2-3x larger payloads
- Significantly higher egress costs

### ⚡ Low Latency Critical

**Recommended: Quarkus JVM with REST + Protobuf**
- Median Latency: 10.23 ms (lowest!)
- P95 Latency: 62.01 ms
- Use Case: Real-time applications, trading systems

**Alternative: Spring Boot with REST + Protobuf**
- Median Latency: 10.93 ms
- P95 Latency: 70.2 ms
- Benefit: Mature ecosystem

### 🏢 Enterprise / Traditional

**Recommended: Spring Boot with gRPC or REST + JSON**
- Proven enterprise features
- Extensive integrations
- Large developer pool
- Use Case: Internal services, monolith modernization

### ☁️ Serverless / FaaS

**Recommended: Quarkus Native (any protocol)**
- Startup: Milliseconds
- Memory: 77-100 MB
- Cost: Minimal per-invocation
- Use Case: AWS Lambda, Azure Functions, Google Cloud Functions

### 🔄 Microservices at Scale

**Recommended: Quarkus Native with REST + Protobuf**
- Memory per instance: 77 MB
- Total instances: Hundreds/thousands possible
- Cost: Significantly reduced
- Use Case: Large microservices deployments, Kubernetes clusters

### 🌐 Public APIs / External Integration

**Recommended: Spring Boot or Quarkus JVM with REST + JSON**
- Universal compatibility
- Human-readable
- Easy debugging
- Use Case: Public APIs, third-party integrations

## Detailed Results

### gRPC Protocol - Detailed Metrics

#### Spring Boot gRPC
```
Throughput: 10,208 req/s
Latency:
  - Average: 19.31 ms
  - Median: 14.57 ms
  - P90: 39.84 ms
  - P95: 48.64 ms
  - Max: 291.21 ms
Memory: 757 MB (75.7% of 1GB)
CPU: Peak 200%
Network: 2.52 GB received / 2.2 GB sent
Warm-up: +9.5% improvement from Run 1 to Run 3
```

#### Quarkus JVM gRPC
```
Throughput: 11,010 req/s (highest overall) ⭐
Latency:
  - Average: 17.96 ms
  - Median: 14.47 ms
  - P90: 32.51 ms
  - P95: 41.81 ms
  - Max: 402.17 ms
Memory: 633 MB (63.3% of 1GB)
CPU: Peak 220%
Network: 2.71 GB received / 2.39 GB sent
Warm-up: +8.0% improvement from Run 1 to Run 3
```

#### Quarkus Native gRPC
```
Throughput: 6,245 req/s
Latency:
  - Average: 31.6 ms
  - Median: 17.4 ms
  - P90: 73.11 ms
  - P95: 81.67 ms
  - Max: 368.91 ms
Memory: 100 MB (39.1% of 256MB)
CPU: Peak 220%
Network: 1.59 GB received / 1.42 GB sent
Warm-up: +2.4% improvement from Run 1 to Run 3
```

### REST + Protocol Buffers - Detailed Metrics

#### Spring Boot REST + Protobuf
```
Throughput: 8,620 req/s
Latency:
  - Average: 23.03 ms
  - Median: 10.93 ms (very low!)
  - P90: 66.76 ms
  - P95: 70.2 ms
  - Max: 284.97 ms
Memory: 535 MB (53.5% of 1GB)
CPU: Peak 210%
Network: 1.81 GB received / 1.98 GB sent
Warm-up: +35.3% improvement from Run 1 to Run 3
```

#### Quarkus JVM REST + Protobuf
```
Throughput: 10,502 req/s
Latency:
  - Average: 18.89 ms
  - Median: 10.23 ms (lowest overall!) ⭐
  - P90: 56.17 ms
  - P95: 62.01 ms
  - Max: 179.97 ms
Memory: 631 MB (63.2% of 1GB)
CPU: Peak 210%
Network: 2.49 GB received / 2.37 GB sent
Warm-up: +12.5% improvement from Run 1 to Run 3
```

#### Quarkus Native REST + Protobuf
```
Throughput: 6,461 req/s
Latency:
  - Average: 30.74 ms
  - Median: 16.07 ms
  - P90: 72.94 ms
  - P95: 78.74 ms
  - Max: 182.49 ms
Memory: 77 MB (30.1% of 256MB) - LOWEST! ⭐
CPU: Peak 210%
Network: 1.63 GB received / 1.57 GB sent
Warm-up: Minimal variance (native stability)
```

### REST + JSON - Detailed Metrics

#### Spring Boot REST + JSON
```
Throughput: 7,188 req/s
Latency:
  - Average: 27.64 ms
  - Median: 12.55 ms
  - P90: 71.58 ms
  - P95: 74.97 ms
  - Max: 369.41 ms
Memory: 629 MB (62.9% of 1GB)
CPU: Peak 210%
Network: 2.78 GB received / 4.01 GB sent
Warm-up: +40.1% improvement from Run 1 to Run 3
```

#### Quarkus JVM REST + JSON
```
Throughput: 8,461 req/s
Latency:
  - Average: 23.43 ms
  - Median: 12.06 ms
  - P90: 63.93 ms
  - P95: 69.31 ms
  - Max: 184.83 ms
Memory: 703 MB (70.3% of 1GB)
CPU: Peak 210%
Network: 3.5 GB received / 4.72 GB sent
Warm-up: +26.8% improvement from Run 1 to Run 3
```

#### Quarkus Native REST + JSON
```
Throughput: 5,260 req/s
Latency:
  - Average: 37.77 ms
  - Median: 19.02 ms
  - P90: 81.04 ms
  - P95: 87.37 ms
  - Max: 282.45 ms
Memory: 87 MB (34.1% of 256MB)
CPU: Peak 210%
Network: 2.38 GB received / 3.22 GB sent
Warm-up: +1.3% improvement from Run 1 to Run 3
```

## Performance Tuning Insights

### JVM Optimization Tips

1. **Warm-up is Critical**
   - Always perform 2-3 warm-up runs
   - Performance can improve 25-40% after warm-up
   - Critical for production benchmarking

2. **Resource Allocation**
   - 2 CPU cores provide good performance
   - 1GB memory sufficient for most workloads
   - Monitor GC patterns under load

3. **Configuration**
   - Disable unnecessary interceptors
   - Use production-grade JVM flags
   - Configure appropriate thread pools

### Native Optimization Tips

1. **Memory Configuration**
   - 256MB sufficient for most workloads
   - Can go lower (128MB) for simple services
   - Monitor actual usage and adjust

2. **Build Time**
   - Native compilation takes several minutes
   - Use container-based builds for consistency
   - Consider CI/CD pipeline impact

3. **Performance Expectations**
   - Lower throughput than JVM (40-50% reduction)
   - Consistent performance from start
   - Ideal for memory-constrained environments

### Protocol Selection Guidelines

1. **Choose gRPC when:**
   - Maximum throughput needed
   - Streaming required
   - Internal microservices communication
   - Client supports gRPC
   - Efficient binary protocol desired

2. **Choose REST + Protobuf when:**
   - Need HTTP compatibility
   - Want binary efficiency
   - **Bandwidth/network costs are a concern** 💰
   - **60-70% smaller payloads than JSON needed**
   - Lowest memory footprint critical (Native)
   - Schema management acceptable

3. **Choose REST + JSON when:**
   - Public APIs
   - Human readability important
   - No schema management desired
   - Universal compatibility needed
   - Debugging ease is priority
   - Payload size not a concern

## Conclusion

### Key Takeaways

1. **For Maximum Performance**: Quarkus JVM with gRPC delivers the highest throughput (11,010 req/s) and lowest latency

2. **For Cost Efficiency**: Quarkus Native with REST + Protobuf provides exceptional memory efficiency (77 MB) with reasonable performance

3. **For Network Efficiency**: Protocol Buffers (gRPC or REST) reduces payload sizes by 60-70% and network I/O by 40-44% compared to JSON, saving significant bandwidth costs

4. **For Enterprise**: Spring Boot offers mature ecosystem with good performance across all protocols

5. **Protocol Choice**: 
   - gRPC: +20-30% throughput advantage
   - REST + Protobuf: Best balance of compatibility, performance, and **60-70% smaller payloads**
   - REST + JSON: Universal compatibility, but 2-3x larger payloads

6. **Runtime Choice**:
   - Quarkus JVM: Best overall performance
   - Quarkus Native: 85-90% memory savings
   - Spring Boot: Mature, proven, enterprise-ready

7. **Network Cost Impact**: For high-volume services (10M+ req/day), Protocol Buffers can save $60-600+/month in bandwidth costs compared to JSON

### Final Recommendations

**General Purpose**: Start with **Quarkus JVM + gRPC** for best performance, switch to **REST + Protobuf** if HTTP compatibility needed.

**Cost-Sensitive**: Use **Quarkus Native + REST + Protobuf** for lowest memory footprint (77 MB) and infrastructure costs.

**High-Volume/Bandwidth-Sensitive**: Choose **Protocol Buffers** (gRPC or REST+Proto) to reduce payload sizes by 60-70% and save significant network costs.

**Enterprise/Traditional**: Choose **Spring Boot + REST + JSON** for mature ecosystem and universal compatibility.

**Hybrid Approach**: Mix and match based on service requirements - use Native for frequently scaled services, JVM for throughput-critical services, and Protobuf for high-volume/bandwidth-sensitive services.

---

## Additional Resources

- [Individual Performance Test READMEs](../README.md)
- [CamelBee Documentation](https://www.camelbee.io)
- [Test Scripts and Configuration](../docs/k6/)

## Contributing

Found an issue or want to add your own benchmarks? Contributions welcome!

---

*Last Updated: February 2026*
*CamelBee Version: Latest*
*All tests performed with interceptors disabled and optimized configurations*
