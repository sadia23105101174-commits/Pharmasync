import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 5 },
    { duration: '1m30s', target: 5 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p95<400'],
  },
};

export function setup() {
  // Pre-seed: create medicine with stock
  const mPayload = {
    name: `LowBandMedicine`,
    description: `Low bandwidth test medicine`,
    price: 30,
    quantity: 100,
    expiryDate: new Date().add(365, 'd').toISOString(),
    batchNumber: `LB`,
    medicineType: 'OTC',
  };
  http.post('http://localhost:8080/api/medicines', JSON.stringify(mPayload), {
    headers: { 'Content-Type': 'application/json' },
  });
  return {};
}

export default function() {
  // Q5: Low bandwidth - 800 sales under 60s
  const payload = {
    medicineId: 1,
    quantity: 1,
    patientName: `LowBandPatient${Math.random()}`,
    prescriberRegNo: `LB${Math.random()}`,
    prescriptionDate: new Date().toISOString(),
  };
  const res = http.post('http://localhost:8080/api/sales', JSON.stringify(payload), {
    headers: { 'Content-Type': 'application/json' },
  });
  check(res, { 'sale status is 201': (r) => r.status === 201 });
  sleep(0.5);
}