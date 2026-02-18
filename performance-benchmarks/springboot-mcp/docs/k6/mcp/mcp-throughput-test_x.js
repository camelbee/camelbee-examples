/**
 * MCP Throughput Test - Max Speed (Spring Boot)
 * Tests: How fast can you process MCP tool calls?
 *
 * Spring Boot MCP returns SSE format:
 *   id:4bcba5ec-3c56-4958-b35b-0864dc4c9694
 *   event:message
 *   data:{"jsonrpc":"2.0","id":"...","result":{...}}
 */
import http from 'k6/http';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const requestsSent     = new Counter('requests_sent');
const requestsReceived = new Counter('requests_received');
const requestLatency   = new Trend('request_latency');

const orderData = JSON.parse(
  open('../../../src/integration-test/resources/data/inttest/api/mcp/createorder/createorder-success-request.json')
);

export const options = {
  scenarios: {
    throughput_test: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 1,
      maxDuration: '2m',
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<1000'],
  },
};

// -----------------------------------------------------------------------------
// Spring Boot MCP returns SSE format — extract JSON from "data:" line
// -----------------------------------------------------------------------------
function parseMcpBody(body) {
  const match = body.match(/^data:(.+)$/m);
  if (match) {
    try {
      return JSON.parse(match[1]);
    } catch (e) {
      return null;
    }
  }
  return null;
}

export default function () {
  const url = 'http://localhost:8080/mcp';

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json, text/event-stream',
    },
  };

  // ---------------------------------------------------------------------------
  // Step 1: Initialize MCP session
  // ---------------------------------------------------------------------------
  const initResponse = http.post(url, JSON.stringify({
    jsonrpc: '2.0',
    id: uuidv4(),
    method: 'initialize',
    params: {
      protocolVersion: '2024-11-05',
      capabilities: { tools: {} },
      clientInfo: {
        name: 'k6-performance-test',
        version: '1.0.0'
      }
    }
  }), params);

  if (!check(initResponse, {
    'init status is 200': (r) => r.status === 200,
  })) {
    console.log(`VU ${__VU}: Initialization failed — status ${initResponse.status}`);
    return;
  }

  const sessionId = initResponse.headers['Mcp-Session-Id'];
  if (!sessionId) {
    console.log(`VU ${__VU}: No session ID received`);
    return;
  }

  const sessionParams = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json, text/event-stream',
      'Mcp-Session-Id': sessionId,
    },
  };

  // ---------------------------------------------------------------------------
  // Step 2: Send tool calls
  //
  // Spring Boot uses positional arg names (confirmed via tools/list):
  //   arg0 = order, arg1 = transactionId, arg2 = businessTransactionId
  // ---------------------------------------------------------------------------
  const requestsToSend = 100;
  let sentCount     = 0;
  let receivedCount = 0;

  for (let i = 0; i < requestsToSend; i++) {
    const startTime = Date.now();

    const response = http.post(url, JSON.stringify({
      jsonrpc: '2.0',
      id: uuidv4(),
      method: 'tools/call',
      params: {
        name: 'createOrder',
        arguments: {
          arg0: orderData,
          arg1: uuidv4(),  // transactionId
          arg2: uuidv4()   // businessTransactionId
        }
      }
    }), sessionParams);

    requestsSent.add(1);
    sentCount++;

    const success = check(response, {
      'status is 200': (r) => r.status === 200,
      'is valid JSON-RPC': (r) => {
        const body = parseMcpBody(r.body);
        return body !== null && body.jsonrpc === '2.0' && body.id && !body.error;
      },
      'has result': (r) => {
        const body = parseMcpBody(r.body);
        return body !== null && body.result !== undefined;
      }
    });

    if (success) {
      requestsReceived.add(1);
      receivedCount++;
      requestLatency.add(Date.now() - startTime);
    }
  }

  console.log(`VU ${__VU}: Sent=${sentCount}, Received=${receivedCount}`);
}