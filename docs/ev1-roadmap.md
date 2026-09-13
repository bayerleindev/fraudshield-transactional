# FraudShield Transactional - EV1 Roadmap

## Objetivo Da EV1

Entregar a primeira fatia de valor demonstravel da plataforma: uma API transacional antifraude que recebe uma transacao, calcula risco com regras deterministicas, retorna uma decisao explicavel e persiste o resultado para auditoria.

A EV1 deve provar o ciclo principal do produto:

```text
Receber transacao -> Avaliar risco -> Explicar decisao -> Persistir auditoria
```

## Proposta De Valor

Ao final da EV1, um usuario tecnico, recrutador, stakeholder ou futuro integrador deve conseguir:

- Subir a aplicacao localmente.
- Enviar uma transacao para avaliacao.
- Receber uma decisao `APPROVE`, `CHALLENGE`, `REVIEW` ou `DENY`.
- Entender os motivos que contribuiram para a decisao.
- Confirmar que a transacao, o score, a decisao e os motivos foram persistidos.
- Rodar testes que validam o motor de risco e o endpoint principal.

## Resultado Esperado

A EV1 deve estar pronta quando o FraudShield funcionar como um backend antifraude minimo, mas completo de ponta a ponta.

O foco nao e volume, performance extrema ou automacao operacional. O foco e clareza de dominio, decisao explicavel e confiabilidade basica do fluxo critico.

## Escopo Incluido

- Projeto Spring Boot 3 com Java 21 e Gradle.
- PostgreSQL local via Docker Compose.
- Flyway para criacao do schema.
- Endpoint `POST /transactions/evaluate`.
- DTOs de request e response com validacao.
- Modelo inicial de cliente, dispositivo, beneficiario, transacao, decisao e motivos.
- Motor de risco deterministico em memoria.
- Regras iniciais:
  - `HIGH_AMOUNT`
  - `VERY_HIGH_AMOUNT`
  - `NEW_DEVICE`
  - `UNTRUSTED_DEVICE`
  - `NEW_BENEFICIARY`
  - `RECENT_PASSWORD_CHANGE`
  - `NEW_ACCOUNT`
- Politica de decisao por faixas de score.
- Persistencia da transacao avaliada.
- Persistencia da decisao de risco.
- Persistencia dos motivos da decisao.
- Testes unitarios do motor de risco.
- Testes de integracao do endpoint principal.
- README com comandos de execucao e exemplo de uso.

## Escopo Excluido

Estes itens ficam fora da EV1 para manter a primeira entrega objetiva:

- Kafka ou qualquer mensageria.
- Redis e velocity checks reais em janela temporal.
- Machine learning.
- Dashboard frontend.
- Case management para revisao manual.
- Deteccao por grafos.
- Autenticacao e autorizacao.
- Kubernetes.
- Observabilidade completa com Prometheus, Grafana ou tracing distribuido.
- API administrativa para configuracao dinamica de regras.
- Multi-tenancy.

## Personas Da EV1

### Integrador Tecnico

Quer saber se consegue chamar uma API simples e receber uma decisao antifraude clara.

Necessidade principal:

- Contrato HTTP estavel.
- Validacao previsivel.
- Response objetiva com score e motivos.

### Analista De Risco

Quer entender por que a plataforma tomou determinada decisao.

Necessidade principal:

- Reasons explicaveis.
- Score final.
- Versao das regras.
- Registro de auditoria consultavel.

### Avaliador Do Projeto

Quer verificar maturidade tecnica e clareza arquitetural.

Necessidade principal:

- Codigo organizado por dominio.
- Testes relevantes.
- Setup local reproduzivel.
- Documentacao pratica.

## Jornada Principal

1. Desenvolvedor sobe PostgreSQL com Docker Compose.
2. Desenvolvedor sobe a API Spring Boot.
3. Cliente envia uma transacao para `POST /transactions/evaluate`.
4. API valida o payload.
5. Servico de aplicacao busca contexto de cliente, dispositivo e beneficiario.
6. Motor de risco aplica regras deterministicas.
7. Politica de decisao converte score em decisao.
8. API persiste transacao, decisao e motivos.
9. API retorna resposta explicavel.
10. Desenvolvedor valida persistencia no banco ou por teste de integracao.

## Contrato De Valor

### Request Minimo

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

### Response De Sucesso

```json
{
  "transactionId": "tx-001",
  "decision": "REVIEW",
  "score": 75,
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
      "scoreImpact": 20
    }
  ],
  "rulesVersion": "v1",
  "evaluatedAt": "2026-09-12T14:30:01Z"
}
```

## Decisoes Da EV1

```text
0  - 29  -> APPROVE
30 - 59  -> CHALLENGE
60 - 89  -> REVIEW
90+      -> DENY
```

## Fases De Entrega

Detailed phase documents:

- `docs/ev1-phases/01-foundation.md`
- `docs/ev1-phases/02-domain-contracts.md`
- `docs/ev1-phases/03-persistence-audit.md`
- `docs/ev1-phases/04-risk-engine.md`
- `docs/ev1-phases/05-evaluation-api.md`
- `docs/ev1-phases/06-validation-documentation.md`

### Fase 1 - Fundacao Executavel

Objetivo:

- Criar a base tecnica que permite desenvolver, rodar e testar a aplicacao.

Entregas:

- Projeto Spring Boot.
- Gradle configurado.
- Docker Compose com PostgreSQL.
- Flyway habilitado.
- Configuracao local em `application.yml`.
- Estrutura modular inicial de pacotes.

Criterios de aceite:

- `./gradlew build` executa.
- PostgreSQL sobe via Docker Compose.
- Aplicacao inicia localmente.
- Flyway executa sem erro.

### Fase 2 - Dominio E Contratos

Objetivo:

- Definir a linguagem central da plataforma antes da implementacao do fluxo.

Entregas:

- Enums de decisao e motivos.
- DTOs de request e response.
- Objetos de dominio para avaliacao de risco.
- Validacoes de entrada.
- Politica documentada para payload invalido.

Criterios de aceite:

- Campos obrigatorios sao validados.
- `amount` deve ser maior que zero.
- `currency` aceita apenas `BRL` na EV1.
- Decisoes e motivos nao dependem de strings livres.

### Fase 3 - Persistencia E Auditoria

Objetivo:

- Garantir que toda decisao bem-sucedida deixe rastro auditavel.

Entregas:

- Migracoes Flyway para tabelas iniciais.
- Repositories para entidades principais.
- Constraints e indices basicos para caminhos de consulta do fluxo.
- Dados seed opcionais para cliente, dispositivo e beneficiario de exemplo.

Criterios de aceite:

- Tabelas sao criadas por migracao.
- `spring.jpa.hibernate.ddl-auto=validate`.
- Transacao, decisao e motivos conseguem ser persistidos juntos.

### Fase 4 - Motor De Risco

Objetivo:

- Implementar o nucleo de decisao antifraude da EV1.

Entregas:

- Contrato `RiskRule`.
- Implementacao das regras iniciais.
- `RiskEngine` para consolidar score e reasons.
- `DecisionPolicy` para mapear score em decisao.
- Testes unitarios para regras e limites de decisao.

Criterios de aceite:

- Cada regra possui teste dedicado.
- Thresholds de decisao possuem testes de borda.
- Combinacao de multiplas regras soma score corretamente.
- Ausencia de regras retorna `APPROVE` com score baixo.

### Fase 5 - API De Avaliacao

Objetivo:

- Expor o caso de uso principal por HTTP.

Entregas:

- `TransactionEvaluationController`.
- Servico de aplicacao para orquestrar o fluxo.
- Busca de contexto de cliente, dispositivo e beneficiario.
- Persistencia transacional de transacao, decisao e motivos.
- Response explicavel no contrato da EV1.
- Handler consistente para erros de validacao.

Criterios de aceite:

- `POST /transactions/evaluate` retorna `200 OK` em avaliacao valida.
- Payload invalido retorna `400 Bad Request`.
- Resposta contem `decision`, `score`, `reasons`, `rulesVersion` e `evaluatedAt`.
- Sucesso HTTP so ocorre se auditoria for persistida.

### Fase 6 - Validacao Executavel

Objetivo:

- Provar que a EV1 funciona de ponta a ponta e pode ser demonstrada.

Entregas:

- Testes de integracao com PostgreSQL via Testcontainers.
- Cenarios principais de decisao.
- Validacao de persistencia.
- README com runbook local e exemplos de `curl`.

Criterios de aceite:

- Testes unitarios passam.
- Testes de integracao passam.
- README permite que outra pessoa rode a demo localmente.
- Exemplo de request gera uma decisao explicavel.

## Cenarios De Demonstracao

### Cenario 1 - Baixo Risco

Entrada:

- Valor baixo.
- Dispositivo conhecido e confiavel.
- Beneficiario conhecido.
- Conta antiga.

Resultado esperado:

- `APPROVE`
- Score entre `0` e `29`.
- Nenhum motivo critico.

### Cenario 2 - Risco Moderado

Entrada:

- Valor acima de `5000`.
- Contexto conhecido.

Resultado esperado:

- `CHALLENGE`
- Motivo `HIGH_AMOUNT`.

### Cenario 3 - Risco Alto

Entrada:

- Valor alto.
- Dispositivo novo.
- Beneficiario novo.

Resultado esperado:

- `REVIEW`
- Motivos explicando a composicao do score.

### Cenario 4 - Risco Critico

Entrada:

- Valor muito alto.
- Dispositivo novo.
- Conta nova ou senha alterada recentemente.

Resultado esperado:

- `DENY`
- Score maior ou igual a `90`.
- Auditoria completa persistida.

## Requisitos Nao Funcionais Da EV1

- Setup local reproduzivel em ambiente de desenvolvimento.
- Testes automatizados relevantes para o fluxo critico.
- Erros de API sem stack trace ou detalhes internos.
- Nenhum log de payload sensivel por padrao.
- Dependencias limitadas ao necessario para a EV1.
- Codigo organizado como monolito modular.

## Metricas De Pronto

A EV1 pode ser considerada entregue quando:

- A aplicacao sobe localmente.
- O endpoint principal responde com sucesso.
- Decisoes sao explicaveis.
- Auditoria e persistida.
- Testes principais passam.
- README cobre setup e uso.
- O escopo excluido continua fora da implementacao.

## Riscos E Mitigacoes

### Risco: EV1 crescer demais

Mitigacao:

- Manter Kafka, Redis, ML, dashboard, auth e Kubernetes fora da entrega.

### Risco: Motor de risco acoplado ao banco ou HTTP

Mitigacao:

- Isolar regras e politica de decisao em pacotes de dominio/aplicacao de risco.

### Risco: Auditoria incompleta

Mitigacao:

- Persistir transacao, decisao e motivos na mesma transacao de banco.

### Risco: Demonstracao depender de dados manuais obscuros

Mitigacao:

- Incluir seed, exemplos no README ou testes de integracao com dados auto-contidos.

## Marco Final

A EV1 termina quando o FraudShield deixa de ser apenas uma base tecnica e passa a demonstrar o valor essencial da plataforma: receber uma transacao financeira, tomar uma decisao antifraude explicavel e preservar um registro auditavel dessa decisao.

Depois da EV1, a plataforma pode evoluir para idempotencia robusta, eventos, velocity checks, revisao manual, dashboards, observabilidade e controles de seguranca mais completos.
