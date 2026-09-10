# Phase 5D.3 Administrative, UI, and Browser Acceptance

## Approved delivery

Phase 5D.3 exposes the already approved Phase 5D.1/5D.2 evaluation workflows. It does not select or appoint an applicant and does not begin Phase 5E.

Administrative permission rules now contain independent agency-wide controls for:

- `primehr.rsp-evaluation-policy`: Access, Add, Edit, Publish
- `primehr.hrmpsb-governance`: Access, Add, Edit, Publish, Finalize
- `primehr.rsp-candidate-evaluation`: Access, Add, Edit, Assess, Submit, Validate, Finalize

PrimeHR staff routes cover policy versions, effective HRMPSB committees, qualified-candidate proceedings, schedules and attendance, assignments and conflict visibility, examination results, member-owned panel ratings, reference checks, comparative calculations, quorum-controlled deliberations, recommendations, and confidential evidence. Action buttons follow the exact permission rather than Access alone; the backend repeats every authorization check.

Careers displays only the applicant's progress and approved assessment schedule information. It does not expose scores, rankings, panel identities or ratings, conflicts, reference notes, minutes, evidence, or recommendations.

## Acceptance evidence

The focused Chromium suite in `e2e/phase5d.spec.ts` passed 3/3 in 1.8 minutes against the local SQL Server environment. It verifies exact denied page/API surfaces, the complete staff reopen workflow (including a deterministic tie, deliberation and evidence), and applicant-safe progress/schedule confidentiality. The final complete Chromium regression passed 35/35 in 4.1 minutes with no skipped tests.

Administrative and PrimeHR strict TypeScript, lint, and production builds passed. The PrimeHR build contains all five Phase 5D routes and produces the standalone production package.

Backend acceptance includes V18-V20, provider migration parity, PostgreSQL-compatible Flyway/Hibernate validation, permission and security guards, immutable snapshots, stale-write protection, result/rating segregation, conflict/recusal enforcement, quorum, deterministic calculations, evidence controls, and applicant-safe response tests. Live PostgreSQL was not run and remains a disclosed non-blocking limitation under the approved policy.

## Corrections discovered through acceptance

Browser acceptance identified and verified fixes for committee-route Spring Security matching, comparative snapshot serialization, and SQL Server's treatment of nullable unique keys. V20 replaces the proceeding constraint with provider-correct unique indexes: PostgreSQL's ordinary unique index permits multiple null terminal records, while SQL Server uses a filtered unique index.

## Boundary

There is no automatic shortlist or selection decision, appointment/employee creation, onboarding, new Jasper report, or Phase 5E behavior. HRMPSB output remains a recommendation for a later authorized selection process. Stop before Phase 5E.
