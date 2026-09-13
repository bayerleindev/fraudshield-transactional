# Git Workflow Rules

## Goal

Keep every development cycle reviewable, traceable, and recoverable through a pushed branch.

## Branch Rules

- Every development cycle must happen on its own branch.
- Use `feature/*` for new behavior or planned phase work.
- Use `bugfix/*` for defect corrections.
- Use `hotfix/*` for urgent production-style fixes.
- Keep branch names short, descriptive, and tied to the work scope.

## Push Rules

- Push the branch to the remote after implementation and validation.
- Do not hand off a development cycle only as local unpushed changes.
- Include known validation limitations in the handoff when a required command cannot run locally.
- Keep unrelated work out of the branch whenever possible.

## Validation Before Push

- Run the validation commands relevant to the change before pushing.
- For MVP phase work, run the commands required by the phase document when the environment allows it.
- If validation is blocked by local infrastructure, document the blocker and the command output summary before handoff.

## Review Handoff

- Mention the branch name.
- Mention the validation commands run.
- Mention whether the branch is `feature/*`, `bugfix/*`, or `hotfix/*`.
- Mention any unresolved risk, skipped validation, or local environment blocker.
