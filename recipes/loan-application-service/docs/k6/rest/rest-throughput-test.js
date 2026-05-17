/**
 * REST Throughput Test - Max Speed
 * Tests: How fast can you process REST requests?
 */

import http from 'k6/http';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const requestsSent = new Counter('requests_sent');
const requestsReceived = new Counter('requests_received');
const requestLatency = new Trend('request_latency');

const testData = JSON.parse(open('../../../src/integration-test/resources/data/inttest/api/json/createorder/createorder-success-request.json'));

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
  const url = 'http://localhost:8080/camelbee-service/orders';
  const payload = JSON.stringify(testData);
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const requestsToSend = 100;
  let sentCount = 0;
  let receivedCount = 0;

  for (let i = 0; i < requestsToSend; i++) {
    const startTime = Date.now();
    const response = http.post(url, payload, params);
    
    requestsSent.add(1);
    sentCount++;
    
    const success = check(response, {
      'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    });
    
    if (success) {
      requestsReceived.add(1);
      receivedCount++;
      requestLatency.add(Date.now() - startTime);
    }
  }
  
  console.log(`VU ${__VU}: Sent=${sentCount}, Received=${receivedCount}`);
}
