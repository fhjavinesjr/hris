# Phase 8B - Eligibility, Evaluation, Deliberation, and Decision Review

Reviewed: 2026-11-03  
Status: Complete; stopped before Phase 8C

## Delivered

- A submitted nomination enters one agency-scoped evaluation case only during the cycle's evaluation window.
- The case freezes the exact published/effective committee charter version, roster, role, voting status, and deterministic fingerprint.
- Eligibility is an authorized human decision with explicit PASS, FAIL, or NOT_APPLICABLE findings and a reasoned summary. Failed findings cannot be marked eligible, and disqualification requires a failed finding.
- Individual criterion ratings use the exact frozen award-category criteria. Evaluators submit raw values and comments; the server validates bounds and derives weighted totals.
- Evaluator work remains member-owned. Committee members who are nominees or nominated group members are automatically conflicted and recused; other conflicts are reasoned and audited.
- Recused assignments may reference only an active, non-conflicted member of the same frozen roster as an authorized replacement.
- Count and percentage quorum exclude conflicted/replaced members. Committee recommendation, non-recommendation, return, and disqualification require quorum.
- A recommendation cannot pass below the category's configured minimum score. The final decision actor cannot also be an evaluator in the case.
- Committee recommendation, head-of-agency decision, and reconsideration resolution are append-only decision events. A reconsideration issue never overwrites the original decision.
- Assigned Records access is intersected with the frozen case assignment; evaluation-case initialization requires Agency Wide authority.

## Database and contracts

- Paired PostgreSQL and SQL Server `V45__rr_eligibility_evaluation_decision.sql` migrations add evaluation cases, eligibility findings, evaluator assignments, criterion ratings, immutable decision events, and decision issues.
- Fresh V1-to-V45 migration, populated V44-to-V45 upgrade, structural/index parity, foreign keys, and Hibernate schema validation passed.
- OpenAPI exposes case initialization, eligibility, conflict/recusal, replacement, independent scoring, recommendation, final decision, and reconsideration endpoints.
- Administrative recognizes independent `primehr.rr-eligibility`, `primehr.rr-evaluation`, and `primehr.rr-award` permissions.

## Verification

```text
Focused PrimeHR OpenAPI/domain/security/V45 parity and populated-upgrade checks: 44/44 passed
PrimeHrFlywaySchemaIntegrationTest: 9/9 passed
Administrative EffectiveAuthorizationServiceImplTest: 29/29 passed
mvn -pl PrimeHR -am clean verify: 429/429 passed; production JAR built
mvn -pl Administrative -am clean verify: Common 3/3 and Administrative 78/78 passed; production JARs built
```

No live PostgreSQL or SQL Server endpoint was configured. Provider portability was checked through paired migration parity, fresh PostgreSQL-compatible migration, a populated V44-to-V45 upgrade, metadata/foreign-key/index inspection, and Hibernate validation. Phase 8B contains no frontend; Playwright remains scheduled for Phase 8D.

## Stop boundary and next gate

Phase 8B introduced no award/conferment record, citation/certificate, incentive authorization, fulfillment ledger, HAP packet, annual PRAISE report, CoreHR/Payroll/Timekeeping mutation, payment, promotion, or succession behavior.

The next gated unit is Phase 8C: immutable award/conferment records, citation/certificate sources, monetary/non-monetary incentive authorization, fulfillment ledger, controlled correction/revocation, external HAP endorsement packets, and annual PRAISE register/report.
