/**
 * MCP Throughput Test - Max Speed
 * Tests: How fast can you process MCP tool calls?
 */
import http from 'k6/http';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const requestsSent = new Counter('requests_sent');
const requestsReceived = new Counter('requests_received');
const requestLatency = new Trend('request_latency');

const orderData = JSON.parse(open('../../../src/integration-test/resources/data/inttest/api/mcp/createorder/createorder-success-request.json'));

export const options = {
  scenarios: {
    throughput_test: {
      executor: 'per-vu-iterations',
      vus: 200,
      iterations: 150,
      maxDuration: '2m',
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<1000'],
  },
};

export default function () {
  const url = 'http://localhost:8080/mcp';

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json, text/event-stream',
    },
  };

  // Step 1: Initialize the MCP session
  const initRequest = {
    jsonrpc: '2.0',
    id: uuidv4(),
    method: 'initialize',
    params: {
      protocolVersion: '2024-11-05',
      capabilities: {
        tools: {}
      },
      clientInfo: {
        name: 'k6-performance-test',
        version: '1.0.0'
      }
    }
  };

  const initResponse = http.post(url, JSON.stringify(initRequest), params);

  if (!check(initResponse, {
    'init status is 200': (r) => r.status === 200,
  })) {
    console.log(`VU ${__VU}: Initialization failed`);
    return;
  }

  // Extract session ID from response headers
  const sessionId = initResponse.headers['Mcp-Session-Id'];
  if (!sessionId) {
    console.log(`VU ${__VU}: No session ID received`);
    return;
  }

  // Update headers with session ID for subsequent requests
  const sessionParams = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json, text/event-stream',
      'Mcp-Session-Id': sessionId,
    },
  };

  // Step 2: Now send the tool calls
  const requestsToSend = 100;
  let sentCount = 0;
  let receivedCount = 0;

  for (let i = 0; i < requestsToSend; i++) {
    const startTime = Date.now();

    // Generate valid UUIDs for transaction IDs
    const transactionId = uuidv4();
    const businessTransactionId = uuidv4();

    const mcpRequest = {
      jsonrpc: '2.0',
      id: uuidv4(),
      method: 'tools/call',
      params: {
        name: 'createOrder',
        arguments: {
          order: orderData,
          transactionId: transactionId,
          businessTransactionId: businessTransactionId
        }
      }
    };

    const response = http.post(url, JSON.stringify(mcpRequest), sessionParams);

    requestsSent.add(1);
    sentCount++;

    const success = check(response, {
      'status is 200': (r) => r.status === 200,
      'is valid JSON-RPC': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.jsonrpc === '2.0' && body.id && !body.error;
        } catch (e) {
          return false;
        }
      },
      'has result': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.result !== undefined;
        } catch (e) {
          return false;
        }
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
