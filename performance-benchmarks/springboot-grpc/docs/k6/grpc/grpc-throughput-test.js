/**
 * gRPC Throughput Test - Max Speed
 * Tests: How fast can you process gRPC requests?
 */

import grpc from 'k6/net/grpc';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const requestsSent = new Counter('requests_sent');
const requestsReceived = new Counter('requests_received');
const requestLatency = new Trend('request_latency');

const testData = JSON.parse(open('../../../src/integration-test/resources/data/inttest/api/grpc/createorder/createorder-success-request.pb.json'));

// Load proto in init context (must be done here)
const client = new grpc.Client();
client.load(['../../../../src/main/resources/grpc', './proto'], '../../../../src/main/resources/grpc/order-service.proto');

// Track connections per VU
const vuConnections = {};

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
    'grpc_req_duration': ['p(95)<1000'],
  },
};

export default function () {
  const vuId = __VU;

  // FIX: Each VU connects independently using the globally loaded client
  if (!vuConnections[vuId]) {
    client.connect('localhost:8199', { plaintext: true });
    vuConnections[vuId] = true;
  }

  const requestsToSend = 100;
  let sentCount = 0;
  let receivedCount = 0;

  for (let i = 0; i < requestsToSend; i++) {
    const startTime = Date.now();
    // Use the global client with its loaded proto
    const response = client.invoke('com.mycompany.order.grpc.OrderService/CreateOrder', testData);

    requestsSent.add(1);
    sentCount++;

    const success = check(response, {
      'status is OK': (r) => r && r.status === grpc.StatusOK,
    });

    if (success) {
      requestsReceived.add(1);
      receivedCount++;
      requestLatency.add(Date.now() - startTime);
    }
  }

  console.log(`VU ${__VU}: Sent=${sentCount}, Received=${receivedCount}`);
}

export function teardown() {
  client.close();
}