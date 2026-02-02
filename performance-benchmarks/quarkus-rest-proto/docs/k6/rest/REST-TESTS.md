# REST API Performance Test


Tests the maximum throughput of CamelBee's REST API endpoint.


**`rest-throughput-test.js`**


- **Maximum request/sec capacity** - How fast can the service process REST API calls?
- **Latency under load** - Response time at peak throughput
- **Success rate** - Percentage of successful requests


```javascript
VUs: 200 virtual users
Iterations: 150 per VU
Total Requests: 200 × 150 × 100 = 3,000,000 requests
Duration: ~2 minutes (maxDuration)
```


- **URL**: `http://localhost:8080/camelbee-service/orders`
- **Method**: POST
- **Content-Type**: application/json


Uses order creation request from:
```
../../../src/integration-test/resources/data/inttest/api/json/createorder/createorder-success-request.json
```

Contains:
- Customer information
- Order with 10 line items
- Delivery address
- Payment details


```bash
k6 run rest-throughput-test.js
```


**With 1GB RAM / 1 CPU:**
- Throughput: 1,200-1,600 req/s
- Latency p95: 80-100ms

**With 2GB RAM / 2 CPU:**
- Throughput: 3,500-4,500 req/s
- Latency p95: 40-60ms


✅ **Good Results:**
- Throughput > 1,000 req/s
- p95 latency < 100ms
- Success rate > 99%

⚠️ **Needs Investigation:**
- Throughput < 800 req/s
- p95 latency > 200ms
- Success rate < 95%


```
✓ status is 200 or 201

     ✓ status is 200 or 201

     checks.........................: 100.00%
     http_req_duration..............: avg=45ms  p(95)=85ms
     http_req_failed................: 0.00%
     
     requests_sent..................: 180000
     requests_received..............: 179820
     request_latency................: avg=43ms  p(95)=82ms

Throughput: 1,528 req/s
```


- Check CPU usage: `docker stats`
- Increase heap: `-Xmx768m` → `-Xmx1536m`
- Check Camel route for blocking operations

- Profile with VisualVM or async-profiler
- Check database connection pool size
- Review GC logs for pauses

- Increase max connections in application
- Check OS file descriptor limits
- Verify network settings


Run all protocol tests to compare:

```bash
k6 run rest-throughput-test.js      # REST baseline
k6 run grpc-throughput-test.js      # gRPC (expect 1.5-2x faster)
k6 run soap-throughput-test.js      # SOAP (expect 20-30% slower)
k6 run graphql-throughput-test.js   # GraphQL (expect similar to REST)
k6 run mcp-throughput-test.js       # MCP (expect similar to REST)
```


- This test sends requests as fast as possible (no think time)
- Represents peak load, not realistic user behavior
- Use for capacity planning and performance comparison
- For sustained load testing, use constant-arrival-rate executor
