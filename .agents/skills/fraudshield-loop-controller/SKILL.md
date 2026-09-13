---
name: fraudshield-loop-controller
description: Start the FraudShield Transactional Loop Controller workflow for a requested MVP phase, using real subagents for implementation, QA, functional testing, security review, code review, and final guideline compliance review.
metadata:
  short-description: Run FraudShield phase loop with real subagents
  argument-hint: PHASE=<phase-doc-or-name> [FOCUS="scope"]
---

# FraudShield Loop Controller

Use this skill when the user wants to start or run the FraudShield Transactional multi-agent delivery loop for a specific MVP phase.

The expected user input names a phase document or phase name, and may include an additional focus scope:

```text
PHASE=<phase-doc-or-name> [FOCUS="scope"]
```

## Repository Context

Operate in the current FraudShield Transactional repository. The project goal is a transactional anti-fraud backend that receives financial transaction events, evaluates deterministic risk rules, returns an explainable decision, and persists the audit trail.

Keep MVP 1 scope tight unless the user explicitly expands it: REST transaction evaluation, deterministic in-memory risk rules, PostgreSQL persistence, Flyway migrations, unit tests, integration tests, Docker Compose, and README instructions. Do not introduce Kafka, Redis, ML, dashboards, graph detection, authentication, Kubernetes, or full observability for MVP 1 unless explicitly requested.

## Required Workflow

Start by instantiating a real Loop Controller agent. The Loop Controller is planning and orchestration only; it must not implement code directly.

The Loop Controller must follow `.agents/loop-controller.md` and read these files before planning:

- `.agents/shared-context.md`
- `AGENTS.md`
- `docs/mvp-1-roadmap.md`
- The requested phase document
- Relevant files under `docs/guidelines/`

If the requested phase is a name rather than a path, resolve it against `docs/mvp-1-phases/` and the roadmap.

## Branch Requirement

Every workflow must run on a dedicated Git branch created or selected at the beginning of the orchestration, before Developer starts implementation.

Rules:

- Prefer `feature/*` for new phase work or documentation, `bugfix/*` for defect corrections, and `hotfix/*` only for urgent production-style fixes.
- Derive the branch name from the requested phase and optional focus, using short kebab-case.
- If the current branch already exists for the same orchestration, continue on it and report the branch name.
- If unrelated working-tree changes make branch creation unsafe, pause and ask the user how to proceed.
- Include the branch name in the plan, Developer handoff, shared context update when relevant, and final report.

## Subagents

The Loop Controller must create real subagents, not simulated roles. Report each subagent id, nickname if available, role, and final status.

Create and sequence these subagents. Developer runs first. After Developer reports, QA, Functional Tester, Security Reviewer, and Code Reviewer should run in parallel when their work is read-only or limited to explicitly authorized, non-overlapping test changes. The Loop Controller must wait for all validation and review reports before deciding whether to delegate corrections back to Developer. After that parallel wave is blocker-free, the Loop Controller must run Guideline Compliance Reviewer as the final strict compliance gate against `docs/guidelines/`.

1. Developer subagent
   - Follow `.agents/developer.md`.
   - Implement or correct only the agreed phase scope.
   - Edit files directly only when the implementation handoff authorizes it.
   - Report changed files and validation performed.

2. QA subagent
   - Follow `.agents/qa.md`.
   - Validate behavior and tests in the parallel validation/review wave after Developer finishes.
   - Prefer read/test work.
   - Do not modify production code.
   - Report status as `PASS` or `FAIL`, including commands run and gaps found.

3. Functional Tester subagent
   - Follow `.agents/functional-tester.md`.
   - Validate executable functional flows in the parallel validation/review wave after Developer finishes.
   - Prefer read/test/run work.
   - Do not modify production code.
   - Report status as `PASS`, `FAIL`, or `NOT_APPLICABLE`, including commands run, scenarios verified, and coverage gaps found.

4. Security Reviewer subagent
   - Follow `.agents/security-reviewer.md`.
   - Review MVP-appropriate security risks in the parallel validation/review wave after Developer finishes.
   - Stay read-only.
   - Lead with blocking security findings, then non-blocking findings and security test gaps.

5. Code Reviewer subagent
   - Follow `.agents/code-reviewer.md`.
   - Perform code review in the parallel validation/review wave after Developer finishes.
   - Prefer read-only review.
   - Lead with blocking findings, then non-blocking findings and test gaps.

6. Guideline Compliance Reviewer subagent
   - Follow `.agents/guideline-compliance-reviewer.md`.
   - Run after QA, Functional Tester, Security Reviewer, and Code Reviewer have no blockers.
   - Stay read-only.
   - Strictly verify the final change against `docs/guidelines/`, the current phase document, roadmap scope, and prior subagent reports.
   - Report status as `APPROVED` or `CHANGES_REQUESTED`, including blocking guideline deviations, non-blocking observations, evidence reviewed, and required corrections.

## Loop Rules

- Keep tasks narrow and non-overlapping.
- Avoid concurrent writes to the same files.
- Run Developer before validation and review agents. After Developer completes, run QA, Functional Tester, Security Reviewer, and Code Reviewer in parallel when their scopes are read-only or otherwise non-overlapping.
- Wait for all QA, Functional Tester, Security Reviewer, and Code Reviewer reports before deciding whether the implementation passes.
- If QA, Functional Tester, Security Reviewer, or Code Reviewer finds a blocker, consolidate all blocking findings into one correction handoff to the Developer subagent, then repeat the Developer correction plus parallel validation/review cycle until no blockers remain or the workflow is genuinely blocked.
- Once the parallel validation/review wave has no blockers, run Guideline Compliance Reviewer as the final gate.
- If Guideline Compliance Reviewer finds a blocker, consolidate its findings into a correction handoff to Developer, then repeat Developer correction, parallel validation/review, and final guideline compliance review.
- The Loop Controller may update only `.agents/shared-context.md`, and only at the end of the workflow.
- Respect existing user changes in the working tree. Do not revert unrelated changes.

## Final Report

Return a concise final report with:

- Plan
- Subagent ids, nicknames if available, roles, and final statuses
- Files changed
- Validation commands
- Functional validation status
- Security review status
- Review status
- Guideline compliance status
- Unresolved risks
- Whether the workflow followed official subagent best practices

Include the optional focus scope from the user in the plan and handoffs when supplied.
