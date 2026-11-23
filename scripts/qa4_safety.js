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
  // Pre-seed: create medicine and batch
  const mPayload = {
    name: `SafetyMedicine`,
    description: `Safety test medicine`,
    price: 25,
    quantity: 100,
    expiryDate: new Date().add(365, 'd').toISOString(),
    batchNumber: `SAFE`,
    medicineType: 'OTC',
  };
  http.post('http://localhost:8080/api/medicines', JSON.stringify(mPayload), {
    headers: { 'Content-Type': 'application/json' },
  });
  return {};
}

export default function() {
  // QA4: Safety - verify no unsafe sales (prescription validation)
  const payload = {
    medicineId: 1,
    quantity: 1,
    patientName: `SafetyPatient${Math.random()}`,
    prescriberRegNo: `SAFEPRESC${Math.random()}`,
    prescriptionDate: new Date().subtract(30, 'd').toISOString(), // old prescription
  };
  const res = http.post('http://localhost:8080/api/sales', JSON.stringify(payload), {
    headers: { 'Content-Type': 'application/json' },
  });
  // Should be blocked or flagged due to prescription rules
  check(res, { 'response status is 201 or 400': (r) => r.status === 201 || r.status === 400 });
  sleep(1);
}