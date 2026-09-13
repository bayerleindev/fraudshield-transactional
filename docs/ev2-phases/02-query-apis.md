# EV2 Phase 2 - APIs De Consulta

## Goal

Permitir consulta posterior das decisoes antifraude sem expor payloads sensiveis completos.

## Outcomes

- Endpoint de consulta por transacao.
- Endpoint de historico por cliente.
- Response com decisao, score, reasons, versao, tipo de avaliacao e features agregadas.
- Limite simples para consultas de historico.
- Erros padronizados para recursos inexistentes.

## Scope

Included:

- `GET /transactions/{transactionId}/decision`.
- `GET /customers/{customerId}/risk-decisions`.
- Consulta da decisao mais recente por transacao.
- Consulta de historico por cliente em ordem decrescente.
- Query parameter `limit` com valor default e maximo.
- Reuso dos DTOs de response quando fizer sentido.

Excluded:

- Busca avancada por filtros multiplos.
- Exportacao CSV.
- API administrativa.
- Paginacao complexa com cursor.
- Exposicao do payload integral da transacao.

## Suggested Files

```text
src/main/java/com/fraudshield/transactional/transaction/api/TransactionDecisionQueryController.java
src/main/java/com/fraudshield/transactional/customer/api/CustomerRiskDecisionController.java
src/main/java/com/fraudshield/transactional/transaction/application/GetTransactionDecisionService.java
src/main/java/com/fraudshield/transactional/customer/application/GetCustomerRiskDecisionsService.java
src/main/java/com/fraudshield/transactional/risk/api/RiskDecisionSummaryResponse.java
src/main/java/com/fraudshield/transactional/risk/api/RiskDecisionDetailResponse.java
```

## Endpoint Contracts

Transaction decision:

```http
GET /transactions/{transactionId}/decision
```

Customer history:

```http
GET /customers/{customerId}/risk-decisions?limit=20
```

Recommended limits:

- Default: `20`.
- Maximum: `100`.

## Response Rules

Return:

- Transaction identifier.
- Decision.
- Score.
- Reasons.
- Rules version.
- Evaluation type.
- Evaluation timestamp.
- Feature snapshot when available.

Do not return by default:

- Full IP address.
- Full original request payload.
- Internal database identifiers unless needed by the contract.

## Acceptance Criteria

- Existing decisions can be retrieved by `transactionId`.
- Unknown transaction returns `404`.
- Customer history returns newest decisions first.
- History endpoint enforces a safe `limit`.
- Responses avoid unnecessary sensitive payload exposure.
