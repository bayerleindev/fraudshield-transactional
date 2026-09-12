# Phase 1 - Bootstrap

## Goal

Create the base Spring Boot project and local infrastructure needed to build the MVP 1 Risk Decision API.

## Outcomes

- Spring Boot application starts locally.
- Gradle build is configured.
- PostgreSQL runs with Docker Compose.
- Flyway is wired into application startup.
- Base package structure exists.
- Initial configuration profiles are available.

## Scope

Included:

- Java 21 project setup.
- Spring Boot 3.x setup.
- Gradle wrapper and build configuration.
- Docker Compose with PostgreSQL.
- `application.yml` for local database configuration.
- Flyway dependency and migration directory.
- Empty modular package structure.

Excluded:

- Business rules.
- Database schema beyond optional smoke migration.
- REST endpoint implementation.
- Testcontainers setup.
- Kafka, Redis, authentication, dashboard, or observability stack.

## Suggested Files

```text
build.gradle
settings.gradle
docker-compose.yml
src/main/java/com/fraudshield/transactional/FraudShieldApplication.java
src/main/resources/application.yml
src/main/resources/db/migration/
```

Initial packages:

```text
transaction/
risk/
customer/
device/
beneficiary/
audit/
shared/
```

## Dependencies

Start with:

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- `flyway-core`
- `flyway-database-postgresql`
- `postgresql`
- `spring-boot-starter-test`

Testcontainers can be added in Phase 6 unless useful earlier.

## Local Database

PostgreSQL defaults:

```text
database: fraudshield
username: fraudshield
password: fraudshield
port: 5432
```

## Validation

Run:

```bash
./gradlew build
docker compose up -d postgres
./gradlew bootRun
```

The application should start without database connection or migration errors.

## Acceptance Criteria

- Project compiles.
- Application starts.
- PostgreSQL container is healthy.
- Flyway is enabled.
- Package skeleton matches the architecture direction.
- README can later reference the exact commands used here.
