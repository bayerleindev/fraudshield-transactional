# EV1 Phase 3 - Persistencia E Auditoria

## Goal

Criar o modelo relacional necessario para persistir o contexto da avaliacao, a transacao recebida, a decisao tomada e os motivos que compoem a explicabilidade.

A EV1 deve conseguir demonstrar valor nao apenas pela resposta HTTP, mas tambem pelo rastro auditavel deixado no banco.

## Outcomes

- Migracoes Flyway para as tabelas iniciais.
- Entidades JPA para contexto e auditoria.
- Repositories para consultas e persistencia do fluxo principal.
- Constraints basicas de integridade.
- Indices para os caminhos de lookup usados pela avaliacao.
- Dados locais opcionais para demonstracao manual.

## Scope

Included:

- Tabela `customers`.
- Tabela `devices`.
- Tabela `beneficiaries`.
- Tabela `transactions`.
- Tabela `risk_decisions`.
- Tabela `risk_reasons`.
- Repositories para clientes, dispositivos, beneficiarios, transacoes, decisoes e motivos.
- Relacionamento entre decisao e motivos.
- Unicidade de `transactionId`.

Excluded:

- Event sourcing.
- Outbox transacional.
- Historico append-only avancado.
- Particionamento de tabelas.
- Criptografia de campos.
- Retencao automatica de dados.

## Suggested Migrations

```text
src/main/resources/db/migration/V1__create_customers_table.sql
src/main/resources/db/migration/V2__create_devices_table.sql
src/main/resources/db/migration/V3__create_beneficiaries_table.sql
src/main/resources/db/migration/V4__create_transactions_table.sql
src/main/resources/db/migration/V5__create_risk_decisions_table.sql
src/main/resources/db/migration/V6__create_risk_reasons_table.sql
src/main/resources/db/migration/V7__seed_local_reference_data.sql
```

The seed migration is optional. If it exists, keep it clearly local/demo-oriented and safe to rerun through Flyway semantics.

## Suggested Java Files

```text
src/main/java/com/fraudshield/transactional/customer/infra/CustomerEntity.java
src/main/java/com/fraudshield/transactional/customer/infra/CustomerRepository.java
src/main/java/com/fraudshield/transactional/device/infra/DeviceEntity.java
src/main/java/com/fraudshield/transactional/device/infra/DeviceRepository.java
src/main/java/com/fraudshield/transactional/beneficiary/infra/BeneficiaryEntity.java
src/main/java/com/fraudshield/transactional/beneficiary/infra/BeneficiaryRepository.java
src/main/java/com/fraudshield/transactional/transaction/infra/TransactionEntity.java
src/main/java/com/fraudshield/transactional/transaction/infra/TransactionRepository.java
src/main/java/com/fraudshield/transactional/risk/infra/RiskDecisionEntity.java
src/main/java/com/fraudshield/transactional/risk/infra/RiskDecisionRepository.java
src/main/java/com/fraudshield/transactional/risk/infra/RiskReasonEntity.java
src/main/java/com/fraudshield/transactional/risk/infra/RiskReasonRepository.java
```

## Table Responsibilities

`customers`:

- Stores minimal customer context.
- Supports `NEW_ACCOUNT`.
- Supports `RECENT_PASSWORD_CHANGE`.

`devices`:

- Stores known devices by customer.
- Supports `NEW_DEVICE`.
- Supports `UNTRUSTED_DEVICE`.

`beneficiaries`:

- Stores known beneficiaries by customer.
- Supports `NEW_BENEFICIARY`.

`transactions`:

- Stores normalized transaction payload.
- Provides the root audit record for an evaluation.

`risk_decisions`:

- Stores final decision, score, rules version and evaluation timestamp.

`risk_reasons`:

- Stores each rule reason that contributed to the score.

## Recommended Constraints

- Unique `customers.customer_id`.
- Unique `(devices.customer_id, devices.device_id)`.
- Unique `(beneficiaries.customer_id, beneficiaries.beneficiary_id)`.
- Unique `transactions.transaction_id`.
- Foreign key from `risk_decisions.transaction_id` to `transactions.transaction_id`.
- Foreign key from `risk_reasons.risk_decision_id` to `risk_decisions.id`.

## Recommended Indexes

- `customers(customer_id)`
- `devices(customer_id, device_id)`
- `beneficiaries(customer_id, beneficiary_id)`
- `transactions(transaction_id)`
- `risk_decisions(transaction_id)`

## Data Handling Notes

- Treat customer, device, beneficiary and IP identifiers as sensitive.
- Do not log full transaction payloads by default.
- Keep audit persistence explicit and reliable.
- A successful API evaluation must not lose its reasons.

## Validation

Run:

```bash
docker compose up -d postgres
./gradlew bootRun
```

Expected result:

- Flyway applies all migrations.
- JPA validates the schema.
- Application starts without auto-DDL creating tables.

## Acceptance Criteria

- Schema supports all EV1 entities.
- Repositories support required lookup and write operations.
- Duplicate `transactionId` is prevented by a constraint.
- One decision can persist multiple reasons.
- Transaction, decision and reasons can be persisted in one transaction.
- No out-of-scope persistence pattern is introduced.
