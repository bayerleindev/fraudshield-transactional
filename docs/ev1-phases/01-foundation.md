# EV1 Phase 1 - Fundacao Executavel

## Goal

Criar a base tecnica minima para desenvolver, rodar e validar a primeira entrega de valor do FraudShield Transactional.

Esta fase nao entrega comportamento antifraude ainda. Ela entrega o trilho confiavel para que as proximas fases avancem com build, banco, migracoes e estrutura do projeto funcionando.

## Outcomes

- Aplicacao Spring Boot criada.
- Build Gradle configurado.
- PostgreSQL local disponivel via Docker Compose.
- Flyway integrado ao startup da aplicacao.
- Configuracoes locais definidas.
- Estrutura inicial de pacotes criada.

## Scope

Included:

- Java 21.
- Spring Boot 3.x.
- Gradle wrapper.
- Dependencias base para Web, Validation, JPA, Flyway, PostgreSQL e testes.
- `docker-compose.yml` com PostgreSQL.
- `application.yml` com profile local.
- Diretorio de migracoes Flyway.
- Classe principal da aplicacao.
- Pacotes principais do monolito modular.

Excluded:

- Endpoint REST.
- Regras antifraude.
- Entidades JPA completas.
- Testcontainers.
- Kafka, Redis, ML, dashboard, autenticacao, Kubernetes ou observabilidade completa.

## Suggested Files

```text
settings.gradle
build.gradle
gradlew
gradlew.bat
docker-compose.yml
src/main/java/com/fraudshield/transactional/FraudShieldApplication.java
src/main/resources/application.yml
src/main/resources/db/migration/
src/test/java/com/fraudshield/transactional/FraudShieldApplicationTests.java
```

Suggested packages:

```text
src/main/java/com/fraudshield/transactional/
  transaction/
  risk/
  customer/
  device/
  beneficiary/
  audit/
  shared/
```

## Dependencies

Initial dependencies:

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- `flyway-core`
- `flyway-database-postgresql`
- `postgresql`
- `spring-boot-starter-test`

Testcontainers can wait until the validation phase unless repository integration tests become useful earlier.

## Local Database Defaults

```text
database: fraudshield
username: fraudshield
password: fraudshield
host: localhost
port: 5432
```

These credentials are development-only and must not be reused as production-like secrets.

## Configuration Notes

- Keep `spring.jpa.hibernate.ddl-auto=validate`.
- Let Flyway own schema creation.
- Avoid committing local generated data.
- Do not expose PostgreSQL beyond localhost unless explicitly needed.

## Validation

Run:

```bash
./gradlew build
docker compose up -d postgres
./gradlew bootRun
```

Expected result:

- Build completes.
- PostgreSQL container becomes healthy.
- Application starts without database or Flyway errors.

## Acceptance Criteria

- Project compiles with Java 21.
- Application starts locally.
- Docker Compose starts PostgreSQL.
- Flyway is enabled.
- Package skeleton matches the EV1 modular monolith direction.
- No out-of-scope infrastructure is introduced.
