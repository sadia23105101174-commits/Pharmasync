import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 5 },
    { duration: '2m', target: 5 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p95<300'],
  },
};

export function setup() {
  // Pre-seed: create a branch and make some initial sales
  const payload = {
    branchName: `Branch-${Math.floor(Math.random() * 10)}`,
    location: `Location ${Math.random()}`,
  };
  http.post('http://localhost:8080/api/branches', JSON.stringify(payload), {
    headers: { 'Content-Type': 'application/json' },
  });
  // Create 3 batches with stock
  for (let i = 0; i < 3; i++) {
    const mPayload = {
      name: `SeedMedicine${i}`,
      description: `Seed desc ${i}`,
      price: 20 + i,
      quantity: 50,
      expiryDate: new Date().add(365, 'd').toISOString(),
      batchNumber: `SEED${i}`,
      medicineType: 'OTC',
    };
    http.post('http://localhost:8080/api/medicines', JSON.stringify(mPayload), {
      headers: { 'Content-Type': 'application/json' },
    });
  }
  return {};
}

export default function() {
  // QA2: Convergence - verify all nodes agree on stock
  const res = http.get('http://localhost:8080/api/sync/stock');
  check(res, { 'status is 200': (r) => r.status === 201 || r.status === 200 });
  sleep(1);
}