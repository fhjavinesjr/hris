# Phase 11A — Standalone Deployment, Configuration, and Security Review

Date: 2026-11-06

## Outcome

Phase 11A keeps PRIME-HRM as a standalone Spring Boot service with its own database. It does not
merge PRIME-HRM into `HRISApp`, read a CoreHR database, or change a business workflow. The unit adds
explicit local and production provider profiles, fail-closed production configuration validation,
a non-root container image, safe health probes, and an authenticated information endpoint.

## Runtime profiles

| Profile | Intended use | Database | Configuration behavior |
|---|---|---|---|
| `local-sqlserver` | local/on-premise development | SQL Server | local defaults are available and may be overridden by environment variables |
| `local-postgresql` | local/cloud-compatible development | PostgreSQL | local defaults are available and may be overridden by environment variables |
| `production-sqlserver` | on-premise production | SQL Server | secrets, provider URL, credentials, agency, CoreHR URLs, and CORS origins are mandatory |
| `production-postgresql` | cloud production | PostgreSQL | secrets, provider URL, credentials, agency, CoreHR URLs, and CORS origins are mandatory |

The default profile is `local-sqlserver` to preserve the established local topology. A production
deployment must set `SPRING_PROFILES_ACTIVE` explicitly. Hibernate remains `validate` and Flyway
remains the schema owner in every non-test runtime profile.

## Mandatory production configuration

The following names identify deployment inputs; values must be provided by the deployment secret or
configuration manager and must not be committed:

- `SPRING_PROFILES_ACTIVE`
- `PRIMEHR_JWT_SECRET`
- `PRIMEHR_HANDOFF_JWT_SECRET`
- `PRIMEHR_AGENCY_ID`
- `PRIMEHR_ADMINISTRATIVE_API_URL`
- `PRIMEHR_HUMAN_RESOURCE_API_URL`
- `PRIMEHR_CORS_ALLOWED_ORIGINS`
- `PRIMEHR_DB_URL`
- `PRIMEHR_DB_USERNAME`
- `PRIMEHR_DB_PASSWORD`
- `PRIMEHR_DB_SCHEMA` when the provider default is not correct
- `PORT` when the platform does not use port `8086`
- `PRIMEHR_RELEASE_VERSION` for traceable runtime metadata

When the applicant portal is enabled, `PRIMEHR_APPLICANT_JWT_SECRET` is mandatory and must differ
from `PRIMEHR_JWT_SECRET`. When document storage is enabled, local storage requires
`PRIMEHR_DOCUMENT_ROOT`; S3 storage requires `PRIMEHR_DOCUMENT_BUCKET` and the deployment's normal
cloud credential mechanism. CORS origin patterns remain optional, but wildcard and local patterns
are rejected in production.

Startup fails before serving traffic when a production secret is missing, shorter than 32
characters, or a known development default; when an upstream URL, CORS origin, or datasource is
local; when the datasource provider does not match the active profile; or when enabled storage is
incomplete. Validation errors identify configuration names and do not print secret values.

## Build and deployment

From the backend repository root:

```powershell
mvn -pl PrimeHR test
mvn -pl PrimeHR -DskipTests package
docker build -f PrimeHR/Dockerfile -t isoft-primehr:<release> .
```

Deploy the immutable image with a non-root runtime, an explicit production profile, the mandatory
configuration above, and provider/network access to the standalone PRIME-HRM database. The image
defaults to `production-postgresql`; an on-premise SQL Server deployment must override it with
`production-sqlserver`.

Flyway applies only the migration set selected by the provider profile. Do not point PRIME-HRM at a
CoreHR schema. Before promotion, back up the standalone database, confirm the target migration
version, and test the selected profile against a staging copy.

## Health, security, and promotion checks

- `GET /actuator/health/liveness` is public and returns only aggregate status.
- `GET /actuator/health/readiness` is public, includes application readiness and database health,
  and returns only aggregate status.
- `GET /actuator/info` requires a valid staff JWT and contains only the configured application name
  and release version.
- All other Actuator endpoints remain unexposed.
- Forwarded headers are interpreted by Spring for reverse-proxy deployments.
- Graceful shutdown allows up to 30 seconds for in-flight requests.
- CORS continues to allow the established methods and now explicitly permits `Idempotency-Key` and
  `X-Correlation-Id`; credentials are allowed only for configured origins.

Repository review found no PRIME-HRM bootstrap or `installAuth` endpoint. CoreHR installation
endpoints are outside the standalone PRIME-HRM service and were not widened or changed by this
phase. PRIME-HRM therefore has no public installation route to exempt from its security chain.

Promotion acceptance requires both health probes to be `UP`, an authenticated information check to
show the intended release, a representative authenticated read, and one controlled write/read-back
in a non-production acceptance environment. A public information request must return `401`.

## Rollback

Phase 11A adds no database migration. If deployment validation or smoke acceptance fails, stop
promotion and redeploy the preceding immutable image with its preceding configuration. If a later
phase includes a migration, never edit or delete an applied migration and never roll the application
back across an incompatible schema; use a reviewed forward repair migration instead.

Do not delete a PRIME-HRM database or document root during rollback. Keep the failed release logs,
configuration-name diagnostics, and release identifier for investigation without copying secrets
into the incident record.

## Verification

- Production configuration validator unit tests cover safe configuration, development secrets,
  local dependencies, missing credentials, applicant-secret separation, and provider mismatch.
- Application smoke acceptance covers detail-free root/liveness/readiness health and protected info.
- Clean complete PRIME-HRM regression passed **525/525** with zero failures, errors, or skips.
- Production JAR packaging passed.
- The Docker client was installed, but Docker Desktop's Linux engine was not running; therefore an
  image build and packaged-container probe could not be executed in this environment.
- No configured live PostgreSQL or SQL Server production endpoint was available. Paired provider
  profile selection and the repository's migration parity tests passed, while live startup remains
  a deployment-environment acceptance check.

## Boundary

Phase 11A does not add retry/backoff, circuit breaking, integration correlation propagation,
reconciliation, retention/deletion, backup automation, alerting, a message broker/outbox, a shared
database, or CoreHR mutation. Those concerns remain gated to Phase 11B–11D.
