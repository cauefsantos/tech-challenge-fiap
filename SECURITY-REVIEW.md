# Security Review

**Date:** 2026-10-24
**Scope:** Vulnerability analysis described in `.dev/plans/plano-analise-vulnerabilidades-tech-challenge.md`
**Method:** Read-only review of Spring Security/JWT configuration, application property defaults, Docker build settings, and the API authentication flow.

## Findings

### HIGH — Default JWT signing secret and administrator credentials

**Confidence:** 9/10
**Affected files:**

- `src/main/resources/application.yml:4-5`
- `src/main/resources/application-dev.yml:33-39`
- `src/main/java/br/com/fiap/techchallengefiap/infrastructure/security/SecurityConfiguration.java:33-45`

The application defaults to the `dev` profile and provides fallback values for JWT signing material and the administrator password. When the corresponding environment variables are unset, the application can use a known JWT secret and the built-in `admin`/`admin` credentials. This permits unauthorized authentication where these defaults are active and known.

**Recommended remediation:** Remove embedded default secrets and credentials. Require explicitly configured values for every non-test profile, and fail startup if required JWT or administrator credentials are missing.

## Incomplete checks

- Dependabot configuration was not present/configured as part of the reviewed repository setup.
- OWASP Dependency Check was not executed.
- Trivy image scanning was not executed because the tool was unavailable in the environment.
