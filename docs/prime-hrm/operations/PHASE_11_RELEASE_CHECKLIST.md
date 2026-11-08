# PRIME-HRM Phase 11 Release Checklist

Use this checklist for each standalone PRIME-HRM promotion. Record evidence in the change ticket; never
paste credentials, JWTs, datasource URLs, employee records, filenames, or document contents into it.

## Before deployment

- [ ] Identify the exact backend and frontend commits/artifacts and retain their checksums.
- [ ] Confirm the target profile is PostgreSQL or SQL Server and matches its JDBC driver and Flyway location.
- [ ] Confirm an accepted backup exists and the provider-specific restore runbook has a current drill record.
- [ ] Confirm Flyway history is valid through V56; do not edit an applied migration.
- [ ] Supply production JWT, applicant/handoff secrets, datasource credentials, storage settings, CORS origins,
      Administrative/HRM URLs, and any scheduled-reconciliation service token through the secret/config provider.
- [ ] Confirm no production dependency URL resolves to localhost and no development secret is active.
- [ ] Confirm `primehr.operations` is granted only to approved agency-wide operators; verify Access/Add/Edit/Delete
      independently. Delete means legal-hold release only and does not grant data deletion.
- [ ] Build and test the Administrative and PrimeHR backend modules and both production frontend packages.
- [ ] Run packaged Playwright permission/direct-route, status, reconciliation, and retention acceptance.

## Deployment and smoke

- [ ] Apply migrations through the packaged application startup and confirm schema validation succeeds.
- [ ] Confirm public liveness/readiness returns aggregate status only and exposes no component details.
- [ ] Confirm authenticated API access and correlation response headers.
- [ ] Confirm the protected Operations workspace reports PRIME-HRM, Administrative, HRM, and storage status
      without showing dependency URLs, secrets, employee identifiers, filenames, or document content.
- [ ] Run one read-only reconciliation and confirm its run/findings are audited without changing source snapshots.
- [ ] Generate the retention eligibility report and confirm active legal holds prevent eligibility.
- [ ] Confirm there is no purge, automatic correction, source mutation, CoreHR mutation, or destructive maintenance action.

## Observation and rollback

- [ ] Observe bounded `primehr.operations.*` metrics and structured operational logs; verify labels contain no PII.
- [ ] Check dependency failure and recovery without granting authorization or partially committing a command.
- [ ] If rollback is required, stop traffic, deploy the previous application artifacts, and preserve the forward-compatible
      V56 schema. Do not reverse or delete reconciliation, finding, policy, hold, or audit history.
- [ ] If schema compatibility prevents application rollback, restore only through the approved isolated recovery process
      and the signed provider restore-verification manifest.

