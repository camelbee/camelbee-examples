/**
 * MCP Throughput Test — submitLoanApplication tool
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

const requestsSent = new Counter('requests_sent');
const requestsReceived = new Counter('requests_received');
const requestLatency = new Trend('request_latency');

export const options = {
  scenarios: {
    throughput_test: {
      executor: 'per-vu-iterations',
      vus: 50,
      iterations: 50,
      maxDuration: '2m',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000'],
  },
};

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

const url = 'http://localhost:8080/mcp';

export default function () {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json, text/event-stream',
    },
  };

  const initResponse = http.post(url, JSON.stringify({
    jsonrpc: '2.0',
    id: uuidv4(),
    method: 'initialize',
    params: {
      protocolVersion: '2024-11-05',
      capabilities: { tools: {} },
      clientInfo: { name: 'k6-loan-app-test', version: '1.0.0' },
    },
  }), params);
  if (!check(initResponse, { 'init status is 200': (r) => r.status === 200 })) {
    return;
  }

  const sessionId = initResponse.headers['Mcp-Session-Id']
    || initResponse.headers['mcp-session-id'];
  if (sessionId) {
    params.headers['Mcp-Session-Id'] = sessionId;
  }

  const suffix = `${__VU}-${__ITER}`;
  const callPayload = {
    jsonrpc: '2.0',
    id: uuidv4(),
    method: 'tools/call',
    params: {
      name: 'submitLoanApplication',
      arguments: {
        request: {
          applicantId: `K6MCP-${suffix}`,
          applicantName: `K6 MCP ${suffix}`,
          applicantEmail: `k6-mcp-${suffix}@example.com`,
          requestedAmount: 12000 + Math.floor(Math.random() * 30000),
          purpose: 'PERSONAL',
          termMonths: 36,
          monthlyIncome: 5500,
          creditScore: 600 + Math.floor(Math.random() * 200),
          employmentStatus: 'EMPLOYED',
        },
        transactionId: uuidv4(),
      },
    },
  };

  const startTime = Date.now();
  const callResponse = http.post(url, JSON.stringify(callPayload), params);
  requestsSent.add(1);

  const json = parseMcpBody(callResponse.body || '');
  const ok = check(callResponse, {
    'status is 200': (r) => r.status === 200,
    'result has applicationId': () => !!(json && json.result),
  });
  if (ok) {
    requestsReceived.add(1);
    requestLatency.add(Date.now() - startTime);
  }
}
