# MCP (Model Context Protocol) Performance Test


Tests the maximum throughput of CamelBee's MCP endpoint for AI tool integration.


**`mcp-throughput-test.js`**


- **Maximum request/sec capacity** - How fast can the service process MCP tool calls?
- **Latency under load** - Response time at peak throughput
- **Error rate** - Percentage of JSON-RPC errors


```javascript
VUs: 200 virtual users
Iterations: 150 per VU
Total Requests: 200 × 150 × 100 = 3,000,000 requests
Duration: ~2 minutes (maxDuration)
```


- **URL**: `http://localhost:8399/camelbee-mcp/rpc`
- **Method**: POST
- **Protocol**: JSON-RPC 2.0
- **Content-Type**: application/json


Uses MCP tool call from:
```
../../../src/integration-test/resources/data/inttest/api/mcp/createorder/createorder-success-request.json
```

Contains JSON-RPC 2.0 request:
```json
{
  "jsonrpc": "2.0",
  "id": "unique-id",
  "method": "tools/call",
  "params": {
    "name": "create_order",
    "arguments": {
      "customer": {...},
      "items": [...],
      "delivery": {...}
    }
  }
}
```


**Model Context Protocol** is Anthropic's standard for AI tool integration:
- JSON-RPC 2.0 based
- Designed for LLM tool calling
- Type-safe via JSON Schema
- Supports discovery, validation, and execution


```bash
k6 run mcp-throughput-test.js
```


**With 1GB RAM / 1 CPU:**
- Throughput: 1,200-1,600 req/s
- Latency p95: 80-100ms

**With 2GB RAM / 2 CPU:**
- Throughput: 3,500-4,500 req/s
- Latency p95: 40-60ms


✅ **Good Results:**
- Throughput > 1,200 req/s
- p95 latency < 100ms
- No JSON-RPC errors
- Success rate > 99%

⚠️ **Needs Investigation:**
- Throughput < 900 req/s
- p95 latency > 200ms
- JSON-RPC errors present
- Success rate < 95%


```
✓ status is 200
✓ is json-rpc response
✓ no json-rpc error

     checks.........................: 100.00%
     http_req_duration..............: avg=49ms  p(95)=91ms
     http_req_failed................: 0.00%
     
     requests_sent..................: 172000
     requests_received..............: 171540
     request_latency................: avg=47ms  p(95)=88ms

Throughput: 1,456 req/s
```


Test checks for valid JSON-RPC 2.0 response:

```javascript
'is json-rpc response': (r) => {
  const body = JSON.parse(r.body);
  return body.jsonrpc === '2.0' && 
         (body.result !== undefined || body.error !== undefined);
}
```

**Success response:**
```json
{
  "jsonrpc": "2.0",
  "id": "unique-id",
  "result": {
    "orderId": "12345",
    "status": "created"
  }
}
```

**Error response:**
```json
{
  "jsonrpc": "2.0",
  "id": "unique-id",
  "error": {
    "code": -32602,
    "message": "Invalid params"
  }
}
```


Standard codes:
- `-32700`: Parse error
- `-32600`: Invalid request
- `-32601`: Method not found
- `-32602`: Invalid params
- `-32603`: Internal error


**Performance:**
- Similar to REST/GraphQL (all JSON over HTTP)
- JSON-RPC wrapper adds ~50 bytes overhead
- Minimal performance impact

**When to use MCP:**
- ✅ Building AI agents with Claude/GPT
- ✅ Need tool discovery and validation
- ✅ Want LLM-friendly tool interface
- ✅ Implementing MCP server for AI integration

**When to use REST/GraphQL:**
- ✅ Traditional web/mobile apps
- ✅ Human-facing APIs
- ✅ Need HTTP semantics (GET/POST/PUT/DELETE)
- ✅ Want REST maturity benefits


```bash
k6 run rest-throughput-test.js      # ~1,500 req/s
k6 run graphql-throughput-test.js   # ~1,485 req/s
k6 run mcp-throughput-test.js       # ~1,456 req/s (3% slower due to JSON-RPC wrapper)
```

All three are essentially equivalent - choose based on use case, not performance.


1. **AI Assistant Tools**
   - Claude Desktop app integration
   - Custom GPT actions
   - LangChain tool wrappers

2. **Agent Orchestration**
   - Multi-agent systems
   - Tool chaining
   - Autonomous workflows

3. **Enterprise AI**
   - RAG system integrations
   - Document processing pipelines
   - Data extraction services


```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": -32700,
    "message": "Parse error"
  }
}
```
**Fix**: Verify request is valid JSON

```json
{
  "error": {
    "code": -32601,
    "message": "Method not found: tools/call"
  }
}
```
**Fix**: Check MCP method name and endpoint URL

```json
{
  "error": {
    "code": -32602,
    "message": "Invalid params: missing 'name' field"
  }
}
```
**Fix**: Verify params structure matches MCP spec


- **Spec**: https://modelcontextprotocol.io
- **Anthropic Docs**: https://docs.anthropic.com/mcp
- **GitHub**: https://github.com/anthropics/mcp


- MCP is relatively new (2024) but gaining adoption
- CamelBee provides MCP server implementation
- Enables AI agents to call CamelBee as a tool
- Bridges enterprise systems with LLMs
