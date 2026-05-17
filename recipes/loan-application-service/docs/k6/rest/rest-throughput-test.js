/**
 * REST Throughput Test — Loan Application submission (POST /loan-applications)
 *
 * Run with the app + backends up via `docker compose up -d`, then:
 *   k6 run docs/k6/rest/rest-throughput-test.js
 */

import http from 'k6/http';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const requestsSent = new Counter('requests_sent');
const requestsAccepted = new Counter('requests_accepted');
const requestLatency = new Trend('request_latency');

export const options = {
  scenarios: {
    throughput_test: {
      executor: 'per-vu-iterations',
      vus: 50,
      iterations: 100,
      maxDuration: '2m',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1000'],
    http_req_failed: ['rate<0.01'],
  },
};

const url = 'http://localhost:8080/camelbee-service/loan-applications';

function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

export default function () {
  const applicantSuffix = `${__VU}-${__ITER}`;
  const payload = JSON.stringify({
    applicantId: `K6-${applicantSuffix}`,
    applicantName: `K6 User ${applicantSuffix}`,
    applicantEmail: `k6-${applicantSuffix}@example.com`,
    requestedAmount: 10000 + Math.floor(Math.random() * 40000),
    purpose: 'PERSONAL',
    termMonths: 36,
    monthlyIncome: 5000 + Math.floor(Math.random() * 5000),
    creditScore: 600 + Math.floor(Math.random() * 200),
    employmentStatus: 'EMPLOYED',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      transactionId: uuid(),
      requestId: uuid(),
    },
  };

  const startTime = Date.now();
  const response = http.post(url, payload, params);
  requestsSent.add(1);
  const accepted = check(response, {
    'status is 202': (r) => r.status === 202,
  });
  if (accepted) {
    requestsAccepted.add(1);
    requestLatency.add(Date.now() - startTime);
  }
}
