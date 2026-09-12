# FraudShield Transactional - MVP 1 Roadmap

## Objetivo

Construir a primeira versao do FraudShield Transactional: uma API backend capaz de receber uma transacao, calcular risco com regras deterministicas, retornar uma decisao antifraude explicavel e registrar a auditoria da decisao.

Fluxo essencial:

```text
Transaction Request -> Feature Extraction -> Rule Evaluation -> Risk Score -> Decision -> Audit Log
```

## Escopo Do MVP 1

Incluido:

- Endpoint REST para avaliacao de transacoes.
- Modelo inicial para transacoes, clientes, dispositivos, beneficiarios e decisoes de risco.
- Motor de regras em memoria.
- Calculo de score baseado em motivos de risco.
- Decisoes: `APPROVE`, `CHALLENGE`, `REVIEW` e `DENY`.
- Explicabilidade da decisao por meio de `reasons`.
- Persistencia em PostgreSQL.
- Migracoes com Flyway.
- Auditoria da decisao.
- Testes unitarios do motor de risco.
- Testes de integracao do endpoint principal.
- Docker Compose para banco local.
- README com instrucoes de execucao.

Fora do MVP 1:

- Kafka ou mensageria.
- Redis e velocity checks reais em janela de tempo.
- Machine learning.
- Dashboard frontend.
- Revisao manual de casos.
- Deteccao por grafos.
- Autenticacao e autorizacao.
- Deploy em Kubernetes.
- Observabilidade completa com Prometheus/Grafana.

## Stack Recomendada

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Validation
- Spring Data JPA
- PostgreSQL
- Flyway
- JUnit 5
- AssertJ
- Testcontainers
- Docker Compose
- Gradle

## Arquitetura Inicial

Comecar como um monolito modular. A separacao por pacote deve refletir o dominio e facilitar uma extracao futura para servicos separados.

```text
src/main/java/com/fraudshield/transactional/
  FraudShieldApplication.java
  transaction/
    api/
    application/
    domain/
    infra/
  risk/
    application/
    domain/
    rules/
  customer/
    domain/
    infra/
  device/
    domain/
    infra/
  beneficiary/
    domain/
    infra/
  audit/
    domain/
    infra/
  shared/
    exception/
    time/
    idempotency/
```

## Contrato Principal Da API

### `POST /transactions/evaluate`

Recebe uma transacao e retorna a decisao antifraude.

Request:

```json
{
  "transactionId": "tx-001",
  "customerId": "cus-123",
  "amount": 8500.00,
  "currency": "BRL",
  "paymentMethod": "PIX",
  "beneficiaryId": "ben-999",
  "deviceId": "dev-abc",
  "ipAddress": "177.10.20.30",
  "occurredAt": "2026-09-12T14:30:00Z"
}
```

Response:

```json
{
  "transactionId": "tx-001",
  "decision": "REVIEW",
  "score": 72,
  "reasons": [
    {
      "code": "HIGH_AMOUNT",
      "description": "Transaction amount is above the configured threshold.",
      "scoreImpact": 30
    },
    {
      "code": "NEW_BENEFICIARY",
      "description": "Customer has no previous relationship with this beneficiary.",
      "scoreImpact": 25
    },
    {
      "code": "NEW_DEVICE",
      "description": "Transaction originated from a device not seen before for this customer.",
      "scoreImpact": 17
    }
  ],
  "rulesVersion": "v1",
  "evaluatedAt": "2026-09-12T14:30:01Z"
}
```

## Modelo De Dominio Inicial

### Transaction

- `transactionId`
- `customerId`
- `amount`
- `currency`
- `paymentMethod`
- `beneficiaryId`
- `deviceId`
- `ipAddress`
- `occurredAt`
- `createdAt`

### RiskDecision

- `id`
- `transactionId`
- `decision`
- `score`
- `rulesVersion`
- `evaluatedAt`

### RiskReason

- `id`
- `riskDecisionId`
- `code`
- `description`
- `scoreImpact`

### Customer

- `customerId`
- `createdAt`
- `lastPasswordChangeAt`
- `status`

### Device

- `deviceId`
- `customerId`
- `firstSeenAt`
- `lastSeenAt`
- `trusted`

### Beneficiary

- `beneficiaryId`
- `customerId`
- `firstTransactionAt`
- `trusted`

## Regras Iniciais

### `HIGH_AMOUNT`

- Condicao: `amount >= 5000`
- Impacto: `30`

### `VERY_HIGH_AMOUNT`

- Condicao: `amount >= 20000`
- Impacto: `50`

### `NEW_DEVICE`

- Condicao: dispositivo inexistente para o cliente
- Impacto: `20`

### `UNTRUSTED_DEVICE`

- Condicao: `trusted = false`
- Impacto: `15`

### `NEW_BENEFICIARY`

- Condicao: beneficiario inexistente para o cliente
- Impacto: `25`

### `RECENT_PASSWORD_CHANGE`

- Condicao: senha alterada nas ultimas 24 horas
- Impacto: `20`

### `NEW_ACCOUNT`

- Condicao: conta criada nos ultimos 7 dias
- Impacto: `20`

## Politica De Decisao

O score final e a soma dos impactos das regras disparadas.

```text
0  - 29  -> APPROVE
30 - 59  -> CHALLENGE
60 - 89  -> REVIEW
90+      -> DENY
```

## Auditoria

Toda avaliacao deve salvar:

- payload normalizado da transacao
- features usadas na avaliacao
- regras disparadas
- score final
- decisao final
- versao das regras
- data/hora da avaliacao

No MVP 1, a auditoria pode estar nas tabelas relacionais `transactions`, `risk_decisions` e `risk_reasons`.

## Banco De Dados

Tabelas iniciais:

- `customers`
- `devices`
- `beneficiaries`
- `transactions`
- `risk_decisions`
- `risk_reasons`

Migracoes Flyway sugeridas:

- `V1__create_customers_table.sql`
- `V2__create_devices_table.sql`
- `V3__create_beneficiaries_table.sql`
- `V4__create_transactions_table.sql`
- `V5__create_risk_decisions_table.sql`
- `V6__create_risk_reasons_table.sql`

## Testes

Unitarios:

- calculo de score
- disparo de cada regra
- politica de decisao por faixa de score
- combinacao de multiplas regras
- ausencia de regras disparadas

Integracao:

- `POST /transactions/evaluate` com transacao aprovada
- `POST /transactions/evaluate` com transacao em revisao
- persistencia de transacao
- persistencia de decisao
- persistencia de motivos
- validacao de payload invalido

Casos importantes:

- Valor baixo, device conhecido, beneficiario conhecido: `APPROVE`
- Valor alto, device conhecido, beneficiario conhecido: `CHALLENGE`
- Valor alto, device novo, beneficiario novo: `REVIEW`
- Valor muito alto, device novo, conta nova: `DENY`
- Request sem `transactionId`: erro `400`
- Request com `amount <= 0`: erro `400`

## Criterios De Aceite

O MVP 1 esta pronto quando:

- A aplicacao sobe localmente.
- O PostgreSQL sobe via Docker Compose.
- As migracoes criam todas as tabelas.
- O endpoint `POST /transactions/evaluate` responde com decisao, score e motivos.
- Cada decisao e persistida no banco.
- Cada motivo da decisao e persistido no banco.
- Payloads invalidos retornam erro `400`.
- Testes unitarios do motor de risco passam.
- Testes de integracao do endpoint passam.
- O README explica como rodar o projeto.

## Ordem De Implementacao

Detailed phase documents:

- `docs/mvp-1-phases/01-bootstrap.md`
- `docs/mvp-1-phases/02-domain.md`
- `docs/mvp-1-phases/03-database.md`
- `docs/mvp-1-phases/04-risk-engine.md`
- `docs/mvp-1-phases/05-api.md`
- `docs/mvp-1-phases/06-integration-tests.md`
- `docs/mvp-1-phases/07-documentation.md`

### Fase 1 - Bootstrap

- Criar projeto Spring Boot.
- Configurar Gradle.
- Criar `docker-compose.yml` com PostgreSQL.
- Configurar `application.yml`.
- Adicionar Flyway.

### Fase 2 - Dominio

- Criar enums de decisao e motivos.
- Criar entidades principais.
- Criar objetos de request/response.
- Definir validacoes do payload.

### Fase 3 - Banco

- Criar migracoes.
- Criar repositories.
- Criar dados seed opcionais para clientes, devices e beneficiarios.

### Fase 4 - Motor De Risco

- Criar contrato `RiskRule`.
- Implementar regras iniciais.
- Criar `RiskEngine`.
- Criar `DecisionPolicy`.
- Adicionar testes unitarios.

### Fase 5 - API

- Criar controller `TransactionEvaluationController`.
- Criar application service para orquestrar o caso de uso.
- Persistir transacao, decisao e motivos.
- Retornar response explicavel.

### Fase 6 - Testes De Integracao

- Configurar Testcontainers com PostgreSQL.
- Testar endpoint principal.
- Testar validacoes.
- Testar persistencia.

### Fase 7 - Documentacao

- Criar README com:
  - objetivo do projeto
  - stack
  - como rodar
  - exemplo de request
  - exemplo de response
  - regras do MVP 1

## Backlog Pos-MVP 1

- Redis para velocity checks.
- Kafka para eventos de decisao.
- Idempotencia robusta por `transactionId`.
- Case management para decisoes `REVIEW`.
- API de regras versionadas.
- Reprocessamento/replay de decisoes antigas.
- Metricas com Prometheus.
- Tracing com OpenTelemetry.
- Dashboard operacional.
- Modulo de deteccao por grafos.

## Marco Final Do MVP 1

Ao final deste MVP, o FraudShield deve funcionar como uma API antifraude transacional simples, auditavel e explicavel. A sofisticacao ainda nao esta em volume ou ML, mas na clareza arquitetural: o sistema sabe receber uma transacao, enriquecer com contexto, aplicar regras, decidir, explicar e registrar.
