# Phase 11 - Integration Hardening Discovery and Proposed Scope

Date: 2026-11-06  
Status: Discovery complete; implementation scope awaits approval  
Authority: ISOFT PRIME-HRM Master Plan V2 Integration Hardening, completed repository Phases 1-10,
and the current checked-out repositories

## 1. Purpose and numbering

Master Plan V2 called Integration Hardening Phase 10. Repository Phase 9 was used by the separately
approved Career and Succession Planning capability, so Evidence Center became repository Phase 10
and Integration Hardening is repository **Phase 11**.

Phase 11 does not add another PrimeHR business domain. It makes the completed Competency, RSP,
Performance Management, L&D, Rewards and Recognition, Succession, and Evidence Center capabilities
deployable, diagnosable, recoverable, and supportable without weakening their ownership, permission,
privacy, portability, or audit boundaries.

This document records discovery and a proposed staged implementation. It does not authorize the
implementation units by itself.

## 2. Repository evidence inspected

Discovery covered:

- the Maven reactor, `HRISApp`, standalone PrimeHR application, root Dockerfile, datasource profiles,
  Flyway V1-V55, and PostgreSQL/SQL Server provider tests;
- PrimeHR security, applicant security, Administrative authorization, agency resolution, CORS,
  exception handling, audit records, Actuator health, storage providers, and report configuration;
- synchronous Administrative, HRM, Employee Portal, appointment-handoff, runtime-configuration,
  and frontend SSO boundaries;
- the three OpenAPI contracts, integration clients, timeout behavior, source-version/fingerprint
  checks, idempotency controls, and existing handoff reconciliation;
- production frontend packaging/runtime configuration and packaged Playwright patterns;
- the progress ledger and discovery/review documents through Phase 10D.

The working trees already contain the user's uncommitted Phase 6F-10 work. Discovery did not reset,
reformat, or overwrite that work.

## 3. Current deployment topology

| Component | Current implementation | Discovery conclusion |
|---|---|---|
| CoreHR backend | `HRISApp` combines Administrative, HRM, Employee Portal, Timekeeping, Payroll and Common on port 9090 | Remains the combined CoreHR deployment |
| PrimeHR backend | Standalone Spring Boot application on port 8086 with its own datasource, Flyway locations and schema | Preserve as a separate bounded service and database |
| Root backend Dockerfile | Builds and packages `HRISApp`; it neither copies `PrimeHR/pom.xml` for the offline dependency layer nor runs the PrimeHR JAR | It is not a PrimeHR production artifact |
| `HRISApp` dependency/scan | Does not depend on or scan `PrimeHR` | PrimeHR is not currently hosted by the combined application |
| Frontends | Separately packaged Next.js applications; Administrative supplies public runtime URLs and PrimeHR resolves Administrative, HRM, PrimeHR and Employee Portal URLs | Retain runtime configuration; remove unsafe production fallback behavior in the hardening unit |
| Database | PrimeHR V1-V55 has paired PostgreSQL/SQL Server migrations and `ddl-auto=validate`; `HRISApp` still uses `ddl-auto=update` | Do not merge PrimeHR tables into the CoreHR datasource; harden each deployment profile deliberately |
| Integration transport | Versioned synchronous REST with browser-user context for scoped reads and a short-lived service JWT for appointment handoff | Keep REST first; no broker is justified by current workflows |
| Files | Local and S3-compatible implementations behind `DocumentStorage`, currently packaged under the RSP applicant namespace and reused by other domains | Preserve the interface; compatibility-safe relocation is optional cleanup, not a prerequisite |

### Deployment decision proposed

The supported target should be:

1. `HRISApp` for CoreHR;
2. a separately built/deployed PrimeHR service and PrimeHR database/schema;
3. separately packaged frontends using Administrative public runtime configuration;
4. authenticated REST contracts between the bounded services.

Adding PrimeHR to `HRISApp` is excluded unless the user separately chooses a single-process topology
and approves its datasource, Flyway, bean/security-chain, and failure-isolation consequences.

## 4. Material findings

| Priority | Finding | Evidence and consequence | Required treatment |
|---|---|---|---|
| Critical | PrimeHR has no repository production deployment artifact | Root Dockerfile packages only `HRISApp`; `HRISApp` excludes PrimeHR | Add an explicit standalone PrimeHR production container/package and acceptance gate |
| Critical | Standalone PrimeHR default configuration contains development credentials and localhost dependencies | Default JWT secret, SQL Server username/password, datasource and service URLs are present in `application.properties` | Production profile must require environment secrets/URLs, reject known defaults, and never silently use localhost |
| High | Combined CoreHR cloud/local profiles use `ddl-auto=update` and SQL logging | Schema mutation and sensitive SQL output are unsafe production defaults | Change only through a reviewed CoreHR deployment-hardening unit; PrimeHR must remain `validate` plus Flyway |
| High | Correlation is optional and inconsistent | Many controllers accept `X-Correlation-Id`, but no ingress filter generates/validates it; errors and logs do not consistently return it | Add one correlation boundary, response header, MDC propagation and safe error correlation field |
| High | REST client behavior is duplicated | Each client constructs its own `RestClient`, timeout and error mapping; safe reads have no common bounded retry policy | Centralize construction/policy without changing endpoint DTOs or retrying mutations |
| High | Operational health is too shallow | Only detail-free `/actuator/health` is exposed; no liveness/readiness groups or storage/migration/dependency visibility exists | Add safe liveness/readiness and protected diagnostic indicators without leaking secrets |
| High | No backup/restore or disaster-recovery evidence exists | No provider runbook, manifest, restore drill or recovery acceptance was found | Add PostgreSQL and SQL Server runbooks/scripts and a representative restore verification gate |
| High | No general reconciliation facility exists | Handoff has receipt reconciliation; other source snapshots detect freshness only during workflows | Add read-only, auditable drift reconciliation; never overwrite historical snapshots or source systems |
| High | Retention tags are data only | Multiple evidence types store retention tags, but no policy registry, hold, eligibility report or safe purge workflow exists | Start with policy/version and report-only eligibility; no automatic deletion in Phase 11 |
| Medium | Audit JSON can contain broad before/after objects | Audit is comprehensive but has no central redaction/size/retention policy | Define protected-field redaction, payload limits and retention/hold rules without weakening audit lineage |
| Medium | Contract and release checks are mostly local/manual | OpenAPI and extensive provider tests exist, but no repository CI/release workflow or integrated topology smoke gate was found | Add deterministic release scripts/checklists and CI-ready commands without embedding credentials |
| Medium | Runtime URL ownership is split between environment and Administrative data | Packaging is sound, but production fallback and completeness validation are not uniform | Validate required keys, schemes and service reachability while keeping public config non-secret |

## 5. Source ownership and non-negotiable rules

| Concern | Authoritative owner | Phase 11 rule |
|---|---|---|
| Employees, appointments, PDS and service history | HRM/CoreHR | PrimeHR reads versioned contracts; never writes tables directly |
| Organization, masters, permissions, runtime public URLs | Administrative/CoreHR | PrimeHR consumes authenticated contracts; commands fail closed when authority cannot be verified |
| Attendance and payroll | Timekeeping and Payroll | No new operational mutation or cross-database query |
| PrimeHR transactions and audit | PrimeHR | Remain in the PrimeHR datasource with Flyway and immutable historical references |
| Uploaded evidence objects | Configured `DocumentStorage` provider | Database metadata and object lifecycle must reconcile; no unreviewed destructive purge |
| Credentials and private endpoints | Deployment secret/config provider | Never store in source, public runtime configuration, logs, reports or audit snapshots |
| Integration history | Owning command/service | Correlation, source version/fingerprint, attempt and outcome stay auditable |

## 6. Proposed staged delivery

| Unit | Precise implementation scope | Migration | Verification gate | Stop boundary |
|---|---|---|---|---|
| **11A - Deployment, configuration and security baseline** | Declare standalone PrimeHR topology; add its production container/package; provider-specific production configuration with required environment values; startup validation against blank/known development secrets and production localhost dependencies; `ddl-auto=validate`; safe CORS; liveness/readiness groups; sanitized build/version info; bootstrap/install endpoint review; deployment and rollback runbook | None expected | Standalone JAR/container smoke; production-config negative tests; public health leaks no details; authenticated API; PostgreSQL and SQL Server startup/migration validation where endpoints are available | No HRISApp/PrimeHR merge, business workflow change, broker, gateway or CoreHR schema rewrite |
| **11B - Synchronous integration reliability and traceability** | Shared typed REST client construction; connect/read timeout policy; bounded retry only for idempotent reads and explicitly idempotent delivery; normalized dependency errors; generated/validated correlation ID, response header and MDC; downstream correlation propagation; contract drift tests; outage/timeout/stale-source acceptance; retain user-context authorization for user-scoped reads and service JWT for appointment handoff | None expected | Client tests for 401/403/404/409/429/5xx/timeout classification; no mutation retry; correlation visible end-to-end; existing OpenAPI and full regression | No generic service impersonation, browser-token logging, event bus, outbox or cross-domain database access |
| **11C - Reconciliation, retention and recovery controls** | Read-only reconciliation runs/findings for authoritative source IDs, versions and fingerprints; manual and disabled-by-default scheduled execution; resolve/acknowledge workflow with audit; storage metadata/object consistency reporting where the provider supports it; versioned retention policy and legal-hold-aware eligibility report; PostgreSQL and SQL Server backup/restore runbooks and restore verification manifest | Proposed paired **V56** only if persisted run/finding/policy records are approved | Migration parity/populated upgrade/fresh-schema checks; drift/missing/unavailable cases; no historical overwrite; representative backup/restore; object checks never expose content | No automatic deletion, source correction, CoreHR mutation, event publication, or provider-specific command in shared application code |
| **11D - Operations workspace, observability and release acceptance** | Exact Administrative operations permission; protected PrimeHR operations workspace for health, dependency status, reconciliation and retention reports; metrics/counters/timers with no PII; structured safe logging; release checklist; production frontend builds; packaged topology smoke and Playwright; provider/report/security regression; support and incident runbooks | None expected beyond approved 11C structures | Permission denial/direct-route tests; metrics/log redaction; packaged frontend/backend smoke; dependency outage/recovery; full Maven/Next.js regression; PostgreSQL/SQL Server evidence disclosed accurately | No public Actuator details, employee ranking, Evidence Center portal exposure, official CSC submission, alert vendor, or destructive maintenance action |

## 7. Detailed acceptance requirements

### Configuration and deployment

- Production cannot start with the development JWT value, blank applicant/handoff secrets when their
  features are enabled, blank datasource credentials, or a localhost dependency URL.
- Local development remains straightforward through an explicit local profile; production safety is
  not achieved by removing local support.
- PostgreSQL and SQL Server select their own driver, Flyway location, schema and Hibernate dialect.
- PrimeHR uses Flyway and schema validation only. Applied migrations are never edited or
  automatically rolled back.
- Container/runtime health checks exercise the packaged artifact, not only MockMvc.

### Integration behavior

- Every inbound request receives one valid correlation ID; the same value is returned and propagated.
- Correlation IDs, tokens and personal data are not accepted unsafely into log text.
- Retries are bounded, observable and limited to safe reads or an operation whose idempotency contract
  is already enforced. POST/PUT workflow commands are never blindly retried.
- Dependency failure produces stable `503`/conflict semantics with an actionable code and correlation
  ID; it never becomes an authorization grant or a partially committed command.
- Existing OpenAPI paths and response DTOs remain backward compatible.

### Reconciliation and recovery

- Reconciliation compares current authoritative facts with stored references and snapshots; it does
  not alter either side.
- Findings distinguish `MATCHED`, `DRIFTED`, `MISSING`, `UNAVAILABLE`, and `UNAUTHORIZED` and retain
  source/version/fingerprint evidence without copying sensitive source payloads.
- Backup artifacts and commands remain outside source control. Runbooks contain placeholders, not
  credentials.
- Restore verification checks Flyway version, table/constraint presence, representative row counts,
  foreign keys, audit continuity and storage references before acceptance.
- Retention begins as a report-only workflow. A future purge requires a separately approved legal,
  records-management and recoverability policy.

### Observability and privacy

- Public health stays detail-free. Dependency names/status may be visible only to an exact operations
  permission or protected management network.
- Metrics use bounded labels and never employee numbers, applicant IDs, names, filenames, tokens,
  report parameters or free-text reasons.
- Logs and API errors never return stack traces, SQL, secrets, tokens or document content.
- Audit redaction preserves actor/action/aggregate/version/reason/correlation lineage.

## 8. Explicit exclusions

Phase 11 excludes:

- RabbitMQ or another broker, transactional outbox, event replay platform or dead-letter queue;
- a gateway, service registry, Kubernetes requirement or vendor-specific monitoring platform;
- merging PrimeHR into `HRISApp` or sharing the CoreHR datasource;
- direct cross-module repository/database access;
- operational writes to HRM, Administrative, Timekeeping or Payroll;
- automatic retention deletion, bulk document purge or destructive migration rollback;
- Employee Portal Evidence Center exposure;
- official CSC PRIME-HRM/PRIME-ESG submission, scoring, certification or Governance Level claim;
- employee ranking, prediction, promotion, appointment, payroll or disciplinary automation;
- broad rewrites of completed business modules merely to adopt a new framework.

## 9. Decisions proposed for approval

The recommended approval is:

1. preserve PrimeHR as a standalone service/database;
2. implement synchronous REST hardening before considering events;
3. treat backup/restore as provider-specific operational tooling and verified runbooks, not an API;
4. make reconciliation read-only and auditable;
5. make retention report-only in Phase 11;
6. implement sequentially as 11A, 11B, 11C and 11D, with a stop/review gate after each unit.

If approved, implementation begins with **Phase 11A - Deployment, configuration and security
baseline** and must stop before 11B.
