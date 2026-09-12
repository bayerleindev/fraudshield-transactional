# Phase 3 - Database And Persistence

## Goal

Create the relational persistence model for the transaction evaluation audit trail and the contextual entities used by the risk engine.

## Outcomes

- Flyway migrations create the MVP 1 tables.
- JPA entities and repositories exist.
- Evaluation data can be persisted and queried.
- Seed data can support local manual testing.

## Scope

Included:

- `customers`
- `devices`
- `beneficiaries`
- `transactions`
- `risk_decisions`
- `risk_reasons`
- JPA repositories for required reads and writes.
- Indexes for lookup fields used during evaluation.

Excluded:

- Advanced audit event store.
- Event sourcing.
- Kafka outbox.
- Redis counters.

## Suggested Migrations

```text
V1__create_customers_table.sql
V2__create_devices_table.sql
V3__create_beneficiaries_table.sql
V4__create_transactions_table.sql
V5__create_risk_decisions_table.sql
V6__create_risk_reasons_table.sql
V7__seed_local_reference_data.sql
```

The seed migration is optional. If used, keep it safe for local development and document it clearly.

## Table Responsibilities

`customers`:

- Holds minimal customer risk context.
- Supports `NEW_ACCOUNT` and `RECENT_PASSWORD_CHANGE`.

`devices`:

- Tracks known devices per customer.
- Supports `NEW_DEVICE` and `UNTRUSTED_DEVICE`.

`beneficiaries`:

- Tracks known beneficiaries per customer.
- Supports `NEW_BENEFICIARY`.

`transactions`:

- Stores normalized incoming transaction payloads.

`risk_decisions`:

- Stores final decision, score, rules version, and evaluation timestamp.

`risk_reasons`:

- Stores every reason that contributed to the score.

## Data Integrity

Recommended constraints:

- Unique `customers.customer_id`.
- Unique `(devices.customer_id, devices.device_id)`.
- Unique `(beneficiaries.customer_id, beneficiaries.beneficiary_id)`.
- Unique `transactions.transaction_id`.
- Foreign key from `risk_decisions.transaction_id` to `transactions.transaction_id`.
- Foreign key from `risk_reasons.risk_decision_id` to `risk_decisions.id`.

## Indexes

Recommended indexes:

- `customers(customer_id)`
- `devices(customer_id, device_id)`
- `beneficiaries(customer_id, beneficiary_id)`
- `transactions(transaction_id)`
- `risk_decisions(transaction_id)`

## Validation

Run:

```bash
docker compose up -d postgres
./gradlew bootRun
```

Flyway should apply all migrations cleanly.

## Acceptance Criteria

- Database schema matches the MVP 1 domain needs.
- Application starts with Flyway migrations enabled.
- Repositories support required lookup and persistence operations.
- Constraints prevent duplicate transaction IDs.
- Audit persistence can store one decision with multiple reasons.
