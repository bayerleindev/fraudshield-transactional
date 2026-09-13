# FraudShield Transactional

Transactional anti-fraud backend for deterministic transaction risk evaluation. The MVP exposes a REST API, evaluates in-memory rules, returns an explainable decision, and persists the full audit trail in PostgreSQL.

## Stack

- Java 21
- Spring Boot 3
- Gradle
- PostgreSQL
- Flyway
- JUnit 5
- Docker Compose

## Run Locally

Start PostgreSQL and the application with Docker Compose:

```bash
docker compose up --build -d app
```

The API will be available at `http://localhost:8080`. Compose binds both the app and PostgreSQL to `127.0.0.1` for local development.

Stop the services:

```bash
docker compose down
```

## Build And Test

Run the test suite:

```bash
./gradlew test
```

Build the application:

```bash
./gradlew build
```

## Transaction Evaluation API

`POST /transactions/evaluate`

Decisions:

- `APPROVE`: score `0` to `29`
- `CHALLENGE`: score `30` to `59`
- `REVIEW`: score `60` to `89`
- `DENY`: score `90+`

Every response includes `X-Correlation-Id`. If the request sends this header, the same value is returned. If it is absent, the application generates one.

Seed a local demo customer before running the successful request:

```bash
docker exec fraudshield-postgres psql -U fraudshield -d fraudshield -c \
  "insert into customers (customer_id, created_at, last_password_change_at, status)
   values ('cus-demo-001', now() - interval '30 days', null, 'ACTIVE')
   on conflict (customer_id) do nothing;"
```

Example request:

```bash
curl -i \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-Id: local-demo-001' \
  -d '{
    "transactionId": "tx-demo-001",
    "customerId": "cus-demo-001",
    "amount": 8500.00,
    "currency": "BRL",
    "paymentMethod": "PIX",
    "beneficiaryId": "ben-demo-001",
    "deviceId": "dev-demo-001",
    "ipAddress": "177.10.20.30",
    "occurredAt": "2026-09-12T14:30:00Z"
  }' \
  http://localhost:8080/transactions/evaluate
```

Successful response shape:

```json
{
  "transactionId": "tx-demo-001",
  "decision": "REVIEW",
  "score": 75,
	  "reasons": [
	    {
	      "code": "HIGH_AMOUNT",
	      "description": "Transaction amount is greater than or equal to 5000.",
	      "scoreImpact": 30
	    },
	    {
	      "code": "NEW_DEVICE",
	      "description": "Transaction originated from a device not seen before for this customer.",
	      "scoreImpact": 20
	    },
	    {
	      "code": "NEW_BENEFICIARY",
	      "description": "Customer has no previous relationship with this beneficiary.",
	      "scoreImpact": 25
	    }
	  ],
	  "rulesVersion": "v1",
  "evaluatedAt": "2026-09-12T14:30:01Z"
}
```

Validation error example:

```bash
curl -i \
  -H 'Content-Type: application/json' \
  -d '{}' \
  http://localhost:8080/transactions/evaluate
```

The API returns stable error codes such as `VALIDATION_ERROR`, `INVALID_REQUEST`, `CUSTOMER_NOT_FOUND`, and `DUPLICATE_TRANSACTION`.
