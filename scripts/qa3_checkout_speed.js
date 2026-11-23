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
    http_req_duration: ['p95<200'],
  },
};

export function setup() {
  // Pre-seed a medicine with stock
  const mPayload = {
    name: `CheckoutMedicine`,
    description: `Checkout test medicine`,
    price: 15,
    quantity: 100,
    expiryDate: new Date().add(365, 'd').toISOString(),
    batchNumber: `CHKOUT`,
    medicineType: 'Rx',
  };
  http.post('http://localhost:8080/api/medicines', JSON.stringify(mPayload), {
    headers: { 'Content-Type': 'application/json' },
  });
  // Create a branch
  const bPayload = {
    branchName: `CheckoutBranch`,
    location: `Checkout loc`,
  };
  http.post('http://localhost:8080/api/branches', JSON.stringify(bPayload), {
    headers: { 'Content-Type': 'application/json' },
  });
  return {};
}

export default function() {
  // QA3: Checkout speed - make a sale and verify fast response
  const payload = {
    medicineId: 1,
    quantity: 1,
    patientName: `SpeedPatient${Math.random()}`,
    prescriberRegNo: `SP${Math.random()}`,
    prescriptionDate: new Date().toISOString(),
  };
  const res = http.post('http://localhost:8080/api/sales', JSON.stringify(payload), {
    headers: { 'Content-Type': 'application/json' },
  });
  check(res, { 'sale status is 201': (r) => r.status === 201 });
  sleep(1);
}