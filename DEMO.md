# PharmaSync Demo: Sync End-to-End

This demo shows the complete branch-to-head-office sync workflow using the PharmaSync codebase.

## Prerequisites

- Maven installed (`mvnw.cmd` is included in the repo)
- Docker Desktop with Kubernetes/Compose (for head-office with PostgreSQL)
- Environment variables configured (see below)

## Step 1: Start the Head Office Server

Start the head-office module with PostgreSQL via Docker Compose:

```cmd
cd head-office
docker compose up --build -d
```

Wait for the server to be ready (PostgreSQL + Spring Boot app). The head-office API will be available at `http://localhost:8080`.

### Environment variables for head-office

Set these before starting Docker, or export them in your shell:

```powershell
$env:BRANCH_DEVICE_CREDENTIALS="branch1:secret1,branch2:secret2"
$env:BRANCH_DEVICE_SECRET="secret1"
```

These are read by `BranchDeviceSecurityConfig` and `BranchSecurityProperties` for HTTP Basic auth on `/api/sync/**`.

## Step 2: Make a Sale at the Branch

With the head-office running, make a sale at the branch. Use curl with the device credentials:

```cmd
curl -u branch1:secret1 -X POST http://localhost:8080/api/sales \
  -H "Content-Type: application/json" \
  -d "{\"medicineId\":1,\"quantity\":1,\"patientName\":\"John Doe\",\"prescriberRegNo\":\"MD12345\",\"prescriptionDate\":\"2024-01-15\"}"
```

Expected response (201 Created):

```json
{
  "id": 1,
  "medicineId": 1,
  "quantity": 1,
  "patientName": "John Doe",
  "prescriberRegNo": "MD12345",
  "prescriptionDate": "2024-01-15",
  "recalledBatchSale": false
}
```

The sale is persisted locally (H2) and an `outbox` entry is created with status `PENDING`.

## Step 3: Wait for Sync

The `BranchSyncClient` (scheduled task with exponential backoff) will:

1. Fetch pending outbox entries ordered by Lamport clock
2. Send them to the head-office `POST /api/sync/operations` with Basic auth (`branch1:secret1`)
3. On server ack, mark the outbox entry as `ACKED`
4. Entries marked `ACKED` are never re-sent

## Step 4: Verify Central Stock at Head Office

Once the sync has run, verify the central stock at head office:

```cmd
curl -u branch1:secret1 "http://localhost:8080/api/sync/stock?page=0&size=10"
```

Response example:

```json
{
  "content": [
    {
      "branchId": "branch1",
      "productId": 1,
      "batchNumber": "B001",
      "onHand": 4,
      "lastSync": "2024-01-15T10:30:00Z",
      "pendingOps": 0
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

The `onHand` value should reflect the sold quantity (decremented from the original batch stock).

## Step 5: Verify Recall and Conflict Rules

### Recall

```cmd
curl -X POST http://localhost:8080/api/v1/recalls \
  -H "Content-Type: application/json" \
  -d "{\"batchNumber\":\"B001\",\"recallTime\":\"2024-02-01T00:00:00Z\"}"
```

### Conflict Rule 1 - Old price sale stands

If a batch's price changes and a sale occurs before the recall time, the old price sale stands and the price difference is reported.

### Conflict Rule 2 - Negative stock after sync creates count task

If sync causes negative stock, a `STOCK_COUNT` task is created.

### Conflict Rule 3 - Sales after recall time flagged

Sales after the recall time will have `recalledBatchSale=true` and are flagged in the report.

## Full Demo Script (ideal environment)

```cmd
:: 1. Start head office
cd head-office
docker compose up --build -d

:: 2. Make a sale at the branch
curl -u branch1:secret1 -X POST http://localhost:8080/api/sales \
  -H "Content-Type: application/json" \
  -d "{\"medicineId\":1,\"quantity\":1,\"patientName\":\"John Doe\",\"prescriberRegNo\":\"MD12345\",\"prescriptionDate\":\"2024-01-15\"}"

:: 3. Wait a few seconds for sync to complete

:: 4. Check central stock
curl -u branch1:secret1 "http://localhost:8080/api/sync/stock?page=0&size=10"

:: 5. Trigger a recall
curl -X POST http://localhost:8080/api/v1/recalls \
  -H "Content-Type: application/json" \
  -d "{\"batchNumber\":\"B001\",\"recallTime\":\"2024-02-01T00:00:00Z\"}"

:: 6. Make a sale after recall time
curl -u branch1:secret1 -X POST http://localhost:8080/api/sales \
  -H "Content-Type: application/json" \
  -d "{\"medicineId\":1,\"quantity\":1,\"patientName\":\"Jane Doe\",\"prescriberRegNo\":\"MD12345\",\"prescriptionDate\":\"2024-02-15\"}"

:: 7. Check the recall report
curl -u branch1:secret1 "http://localhost:8080/api/v1/recalls/report/B001"
```