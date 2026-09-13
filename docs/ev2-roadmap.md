# FraudShield Transactional - EV2 Roadmap

## Objetivo Da EV2

Evoluir a plataforma de uma avaliacao antifraude transacional explicavel para um servico operacionalmente mais confiavel, consultavel e sensivel ao comportamento recente do cliente.

A EV2 deve provar o segundo ciclo de valor:

```text
Reavaliar com seguranca -> Consultar decisoes -> Usar historico recente -> Preservar auditoria
```

## Proposta De Valor

Ao final da EV2, um usuario tecnico, analista ou avaliador deve conseguir:

- Enviar a mesma transacao mais de uma vez sem gerar auditoria duplicada indevida.
- Consultar a decisao antifraude de uma transacao ja avaliada.
- Consultar historico basico de decisoes por cliente.
- Reavaliar uma transacao de forma controlada e auditavel.
- Aplicar regras simples baseadas em comportamento recente.
- Entender quais features foram usadas na decisao.

## Resultado Esperado

A EV2 deve transformar a API da EV1 em uma base mais realista para operacao: a plataforma continua simples e deterministica, mas passa a lidar melhor com repeticao de requests, consulta posterior, reavaliacao e sinais temporais calculados a partir do proprio PostgreSQL.

## Escopo Incluido

- Idempotencia robusta para `POST /transactions/evaluate`.
- Endpoint de consulta por transacao:
  - `GET /transactions/{transactionId}/decision`
- Endpoint de historico por cliente:
  - `GET /customers/{customerId}/risk-decisions`
- Endpoint de reavaliacao controlada:
  - `POST /transactions/{transactionId}/reevaluate`
- Snapshot das features usadas em cada decisao.
- Distincao entre avaliacao original e reavaliacoes.
- Velocity checks simples usando PostgreSQL.
- Novas regras deterministicas baseadas em historico recente.
- Evolucao do modelo de auditoria.
- Padronizacao mais clara de erros de API.
- Testes unitarios para novas regras.
- Testes de integracao para idempotencia, consultas, reavaliacao e velocity checks.
- README atualizado com exemplos da EV2.

## Escopo Excluido

Estes itens continuam fora da EV2:

- Kafka ou mensageria.
- Redis.
- Machine learning.
- Dashboard frontend.
- Case management completo para analistas.
- Autenticacao e autorizacao completas.
- Kubernetes.
- Observabilidade completa com Prometheus, Grafana ou tracing distribuido.
- API administrativa para edicao dinamica de regras.
- Multi-tenancy.

## Personas Da EV2

### Integrador Tecnico

Quer ter seguranca para repetir chamadas sem criar inconsistencias e consultar o resultado depois.

Necessidade principal:

- Idempotencia previsivel.
- Consulta por `transactionId`.
- Erros padronizados.

### Analista De Risco

Quer entender o comportamento recente de um cliente e revisar decisoes ja tomadas.

Necessidade principal:

- Historico por cliente.
- Features usadas na decisao.
- Reavaliacoes auditaveis.

### Avaliador Do Projeto

Quer ver maturidade alem do CRUD: consistencia, auditoria, historico e regras com contexto temporal.

Necessidade principal:

- Testes de cenarios reais.
- Arquitetura ainda clara.
- Escopo controlado sem pular para microservicos.

## Jornada Principal

1. Cliente envia uma transacao para avaliacao.
2. API aplica idempotencia e evita duplicidade indevida.
3. Plataforma monta contexto atual e historico recente.
4. Motor de risco aplica regras da EV1 e regras de comportamento recente.
5. Plataforma persiste transacao, features, decisao e motivos.
6. Cliente consulta a decisao por `transactionId`.
7. Analista consulta decisoes recentes de um cliente.
8. Operador solicita reavaliacao controlada quando necessario.
9. Plataforma registra nova decisao sem apagar a anterior.

## Contratos De Valor

### Avaliacao Idempotente

```http
POST /transactions/evaluate
Idempotency-Key: tx-001
Content-Type: application/json
```

Comportamento esperado:

- Primeira chamada avalia e persiste.
- Repeticao equivalente retorna a decisao ja registrada.
- Repeticao conflitante retorna erro claro de conflito.

### Consulta Por Transacao

```http
GET /transactions/tx-001/decision
```

Response:

```json
{
  "transactionId": "tx-001",
  "decision": "REVIEW",
  "score": 75,
  "rulesVersion": "v2",
  "evaluationType": "ORIGINAL",
  "evaluatedAt": "2026-09-12T14:30:01Z",
  "reasons": [
    {
      "code": "HIGH_AMOUNT",
      "description": "Transaction amount is above the configured threshold.",
      "scoreImpact": 30
    }
  ],
  "features": {
    "transactionsLast10Minutes": 3,
    "amountLast10Minutes": 12800.00,
    "newBeneficiariesLast24Hours": 2
  }
}
```

### Historico Por Cliente

```http
GET /customers/cus-123/risk-decisions
```

Comportamento esperado:

- Retorna decisoes recentes do cliente em ordem decrescente de avaliacao.
- Suporta limite simples por query parameter.
- Nao expoe payloads sensiveis completos.

### Reavaliacao Controlada

```http
POST /transactions/tx-001/reevaluate
```

Comportamento esperado:

- Cria nova decisao vinculada a transacao original.
- Mantem decisao anterior preservada.
- Marca `evaluationType` como `REEVALUATION`.
- Usa a versao atual das regras.

## Novas Regras Da EV2

### `HIGH_FREQUENCY_TRANSACTIONS`

- Condicao: cliente realizou muitas transacoes em janela curta.
- Janela sugerida: 10 minutos.
- Impacto sugerido: 20.

### `HIGH_RECENT_AMOUNT`

- Condicao: soma transacionada pelo cliente em janela curta ultrapassa limite.
- Janela sugerida: 10 minutos.
- Impacto sugerido: 25.

### `MULTIPLE_NEW_BENEFICIARIES`

- Condicao: cliente transacionou com varios beneficiarios novos em janela recente.
- Janela sugerida: 24 horas.
- Impacto sugerido: 20.

### `REPEATED_DENIED_ATTEMPTS`

- Condicao: cliente teve multiplas decisoes `DENY` recentes.
- Janela sugerida: 24 horas.
- Impacto sugerido: 30.

## Politica De Decisao

A EV2 deve manter a politica de decisao da EV1 para preservar previsibilidade:

```text
0  - 29  -> APPROVE
30 - 59  -> CHALLENGE
60 - 89  -> REVIEW
90+      -> DENY
```

Se os novos sinais tornarem os scores altos demais, ajuste impactos das regras, nao os thresholds, salvo decisao explicita de produto.

## Fases De Entrega

Detailed phase documents:

- `docs/ev2-phases/01-idempotency-contracts.md`
- `docs/ev2-phases/02-query-apis.md`
- `docs/ev2-phases/03-feature-snapshots.md`
- `docs/ev2-phases/04-velocity-rules.md`
- `docs/ev2-phases/05-reevaluation-audit.md`
- `docs/ev2-phases/06-validation-documentation.md`

### Fase 1 - Idempotencia E Contratos

- Definir comportamento idempotente.
- Padronizar erros.
- Preparar modelo para distinguir request repetido equivalente de request conflitante.

### Fase 2 - APIs De Consulta

- Adicionar consulta por transacao.
- Adicionar historico basico por cliente.
- Evitar exposicao de payload sensivel completo.

### Fase 3 - Snapshot De Features

- Persistir as features usadas pela decisao.
- Tornar a explicabilidade reproduzivel mesmo quando o contexto muda.

### Fase 4 - Regras De Comportamento Recente

- Calcular sinais temporais em PostgreSQL.
- Adicionar novas regras deterministicamente testaveis.

### Fase 5 - Reavaliacao Auditavel

- Permitir reavaliar transacao existente.
- Criar nova decisao sem sobrescrever historico.

### Fase 6 - Validacao E Documentacao

- Cobrir fluxo com testes unitarios e integracao.
- Atualizar README e exemplos.
- Documentar limitacoes da EV2.

## Cenarios De Demonstracao

### Cenario 1 - Retry Seguro

Entrada:

- Mesma transacao enviada duas vezes com o mesmo payload.

Resultado esperado:

- Segunda chamada retorna a decisao existente.
- Nao cria segunda decisao original.

### Cenario 2 - Retry Conflitante

Entrada:

- Mesmo `transactionId` enviado com payload diferente.

Resultado esperado:

- API retorna erro de conflito.
- Auditoria original permanece intacta.

### Cenario 3 - Consulta De Decisao

Entrada:

- `GET /transactions/{transactionId}/decision` para transacao avaliada.

Resultado esperado:

- API retorna decisao, score, motivos, versao de regras e features.

### Cenario 4 - Risco Por Comportamento Recente

Entrada:

- Cliente com varias transacoes recentes.

Resultado esperado:

- Nova regra de frequencia ou valor recente aumenta o score.

### Cenario 5 - Reavaliacao

Entrada:

- `POST /transactions/{transactionId}/reevaluate` para transacao existente.

Resultado esperado:

- Nova decisao e persistida como reavaliacao.
- Decisao original permanece consultavel no historico.

## Requisitos Nao Funcionais Da EV2

- Idempotencia deterministica.
- Consultas com paginacao ou limite simples.
- Auditoria preservada em reavaliacoes.
- Erros sem detalhes internos.
- Logs sem payload sensivel completo.
- Regras novas testaveis sem depender de Spring.
- Queries de historico com indices adequados.

## Metricas De Pronto

A EV2 pode ser considerada entregue quando:

- Repetir uma avaliacao equivalente e seguro.
- Repetir uma avaliacao conflitante retorna erro claro.
- Decisoes podem ser consultadas por transacao.
- Historico por cliente esta disponivel.
- Features usadas na decisao sao persistidas.
- Reavaliacoes criam novas decisoes auditaveis.
- Novas regras de comportamento recente estao cobertas por testes.
- README explica os novos fluxos.

## Riscos E Mitigacoes

### Risco: Idempotencia ambigua

Mitigacao:

- Definir hash ou fingerprint canonico do request normalizado.
- Documentar comportamento para retries equivalentes e conflitantes.

### Risco: Reavaliacao sobrescrever auditoria

Mitigacao:

- Modelar decisoes como registros historicos.
- Usar `evaluationType` e timestamps para distinguir versoes.

### Risco: Velocity checks ficarem complexos demais

Mitigacao:

- Usar queries PostgreSQL simples.
- Manter Redis fora da EV2.

### Risco: APIs de consulta exporem dados sensiveis

Mitigacao:

- Retornar decisao, motivos e features agregadas.
- Evitar payload completo por padrao.

## Marco Final

A EV2 termina quando o FraudShield passa a operar alem da decisao imediata: ele consegue lidar com retries, consultar decisoes, preservar snapshots de features, usar historico recente como sinal de risco e reavaliar sem perder auditabilidade.

Depois da EV2, a plataforma pode evoluir para case management, autenticacao, eventos, observabilidade operacional e mecanismos de escala como Redis ou Kafka.
