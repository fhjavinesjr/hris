# Phase 6F.4 - Controls, Reporting Interfaces, and Acceptance Review

Date: 2026-10-24  
Status: Complete and verified; Phase 6F finished

## Delivered behavior

| Surface | Delivered control |
|---|---|
| Administrative | Five exact reporting permission rows with Access and data scope; no default role grant |
| PrimeHR | Permission-filtered Performance Reports workspace for eligible formal PDFs, paginated final-rating register, and privacy-controlled aggregate analytics |
| Employee Portal | My Performance Reports page using authenticated-own sources, no employee selector, and direct-route permission redirection |
| Backend | Formal reports require both workflow and exact report permissions; effective scope is their fail-closed intersection |
| Integration | Performance-outcome feed stays agency-wide, read-only, audited, and API-only |

Formal and monitoring report permissions support Own Records, Assigned Records, and Agency Wide.
Register, analytics, and outcome-feed permissions require Agency Wide at backend enforcement. An
incompatible Own-versus-Assigned intersection cannot broaden access and is rejected.

## Verification

- Administrative focused permission lint passed.
- PrimeHR and Employee Portal full lint and strict TypeScript checks passed.
- Administrative, PrimeHR, and Employee Portal production builds/packages passed.
- Focused backend authorization/report tests passed.
- Clean backend package passed Administrative 71/71 and PrimeHR 354/354 with zero failures,
  errors, or skips; both production JARs were built.
- Packaged-frontend Playwright acceptance passed 4/4, covering denied PrimeHR access, denied portal
  direct-URL redirection, authorized register/analytics use, authenticated-own source retrieval, no
  employee selector, bearer authentication, and PDF media handling.
- The existing `images.domains` deprecation warning remains in the Employee Portal configuration;
  it did not fail build or acceptance.

No database schema changed in Phase 6F.4. The earlier paired V36 PostgreSQL/SQL Server migrations,
fresh migration, upgrade, and Hibernate validation remain the provider gates. No live PostgreSQL or
SQL Server endpoint was configured for this run.

## Boundary and continuation

No outcome-feed UI, downstream write, automatic competency or L&D update, reward nomination, RSP
decision, CoreHR mutation, payroll/timekeeping effect, notification, or new-domain behavior was
added. Phase 6F is complete. Repository discovery and explicit scope approval are required before
starting the next PrimeHR domain phase.
