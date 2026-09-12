# Phase 7 - Documentation

## Goal

Document the MVP 1 project so it is easy to understand, run, test, and evaluate as a backend portfolio project.

## Outcomes

- README explains the project clearly.
- Local setup commands are documented.
- API request and response examples are documented.
- Risk rules and decision policy are documented.
- Test commands are documented.
- Known MVP limitations are explicit.

## Scope

Included:

- README.
- API examples.
- Local development instructions.
- Docker Compose instructions.
- Test instructions.
- Short architecture overview.
- MVP limitations and next steps.

Excluded:

- Full ADR catalog.
- Generated OpenAPI docs unless already implemented.
- Production deployment runbook.
- Incident management docs.

## Suggested README Structure

```text
# FraudShield Transactional

## Overview
## MVP 1 Features
## Tech Stack
## Architecture
## Running Locally
## API
## Risk Rules
## Decision Policy
## Running Tests
## Project Structure
## Limitations
## Roadmap
```

## API Example

Document:

- Endpoint URL.
- Request body.
- Response body.
- Possible decision values.
- Validation behavior.

## Architecture Notes

Keep this section short and practical. Explain that the project starts as a modular monolith with domain-oriented packages because MVP 1 benefits from simplicity while still preserving clear boundaries.

## Limitations

Mention clearly:

- Rules are deterministic and in memory.
- No Redis velocity checks yet.
- No Kafka events yet.
- No authentication yet.
- No manual review workflow yet.
- No graph-based detection yet.

## Acceptance Criteria

- A new developer can run the project from the README.
- API examples match implemented behavior.
- Risk rules and thresholds match the code.
- Limitations are honest and aligned with MVP 1.
- References point to the roadmap and phase docs.
