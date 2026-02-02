# gRPC Performance Test


Tests the maximum throughput of CamelBee's gRPC endpoint.


**`grpc-throughput-test.js`**


- **Maximum request/sec capacity** - How fast can the service process gRPC calls?
- **Latency under load** - Response time at peak throughput
- **Success rate** - Percentage of successful gRPC calls


```javascript
VUs: 200 virtual users
Iterations: 150 per VU
Total Requests: 200 × 150 × 100 = 3,000,000 requests
Duration: ~2 minutes (maxDuration)
```


- **Host**: `localhost:8199`
- **Service**: `com.mycompany.order.grpc.OrderService`
- **Method**: `CreateOrder`
- **Protocol**: gRPC (HTTP/2 + Protocol Buffers)


Uses Protobuf order creation request from:
```
../../../src/integration-test/resources/data/inttest/api/grpc/createorder/createorder-success-request.pb.json
```


Located at:
```
../../../../src/main/resources/grpc/order-service.proto
```


```bash
k6 run grpc-throughput-test.js
```


**With 1GB RAM / 1 CPU:**
- Throughput: 1,600-2,100 req/s
- Latency p95: 40-60ms

**With 2GB RAM / 2 CPU:**
- Throughput: 4,500-6,000 req/s
- Latency p95: 20-40ms


✅ **Good Results:**
- Throughput > 1,500 req/s
- p95 latency < 60ms
- Success rate > 99%

⚠️ **Needs Investigation:**
- Throughput < 1,000 req/s
- p95 latency > 100ms
- Success rate < 95%


```
✓ status is OK

     checks.........................: 100.00%
     grpc_req_duration..............: avg=35ms  p(95)=52ms
     
     requests_sent..................: 200000
     requests_received..............: 199850
     request_latency................: avg=33ms  p(95)=50ms

Throughput: 2,068 req/s
```


- **Binary Protocol**: Protobuf is more efficient than JSON
- **HTTP/2**: Multiplexing, header compression, server push
- **Smaller Payloads**: ~30-50% smaller than JSON
- **Efficient Serialization**: Faster parsing than JSON


```
Error: proto file not found
```
**Fix**: Verify proto file path is correct relative to test location

```
Error: connection refused
```
**Fix**: Ensure gRPC port 8199 is exposed and service is running

- Check if HTTP/2 is enabled
- Verify Protobuf compilation is correct
- Profile gRPC handler performance


```bash
k6 run rest-throughput-test.js   # ~1,500 req/s
k6 run grpc-throughput-test.js   # ~2,000 req/s (33% faster)
```

**gRPC advantages:**
- 30-50% higher throughput
- 20-40% lower latency
- 50% smaller payload size
- Built-in streaming support

**REST advantages:**
- Easier debugging (human-readable JSON)
- Better browser/curl support
- More familiar to developers
- Wider ecosystem


- gRPC performs best with streaming (not tested here)
- Results show unary request/response performance
- For streaming tests, use `grpc-stream-test.js` (if available)
