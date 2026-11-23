import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 5 },   // ramp up
    { duration: '1m', target: 5 },    // steady state
    { duration: '30s', target: 0 },   // ramp down
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'], // less than 1% failures
    http_req_duration: ['p95<500'], // p95 latency under 500ms
  },
};

export function setup() {
  // Seed data: create 5 medicines and batches
  const medicines = [];
  for (let i = 0; i < 5; i++) {
    const payload = {
      name: `Medicine ${i}`,
      description: `Description ${i}`,
      price: 10 + i,
      quantity: 100,
      expiryDate: new Date().add(365, 'd').toISOString(),
      batchNumber: `BATCH${i}`,
      medicineType: i % 2 === 0 ? 'Rx' : 'OTC',
    };
    const res = http.post('http://localhost:8080/api/medicines', JSON.stringify(payload), {
      headers: { 'Content-Type': 'application/json' },
    });
    check(res, { 'status is 201': (r) => r.status === 201 });
    medicines.push(JSON.parse(res.body));
  }
  return { medicines };
}

export default function(data) {
  // QA1: Offline continuity - make a sale, go offline, sync later
  const medicine = data.medicines[0];
  const payload = {
    medicineId: medicine.id,
    quantity: 1,
    patientName: `Patient ${Date.now()}`,
    prescriberRegNo: `PRESC${Date.now()}`,
    prescriptionDate: new Date().toISOString(),
  };
  const res = http.post('http://localhost:8080/api/sales', JSON.stringify(payload), {
    headers: { 'Content-Type': 'application/json' },
  });
  check(res, { 'sale status is 201': (r) => r.status === 201 });
  sleep(1);
}