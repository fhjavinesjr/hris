# PRIME-HRM Support and Incident Runbook

## Safe intake

Record timestamp, environment, affected operation, HTTP status/error code, and correlation ID. Do not request or
record passwords, JWTs, datasource URLs, full PDS/payroll records, filenames, uploaded content, or free-text evidence.
Use the protected Operations workspace only with the agency-wide `primehr.operations` permission.

## Triage

1. Check public liveness/readiness for aggregate service state.
2. Check the protected dependency table for Administrative, Human Resource, and document-storage state.
3. Correlate bounded operational metrics and structured logs using time and correlation ID.
4. Inspect the latest reconciliation run and finding type without opening stored document content.
5. Check Flyway history, datasource capacity, storage-provider health, and configured runtime values through the
   approved deployment platform. Never copy their secrets into the incident record.

| Condition | Immediate action | Recovery evidence |
|---|---|---|
| PRIME-HRM down | Stop new commands, inspect readiness/database capacity, restart only the packaged artifact | readiness UP, authenticated smoke, no failed migration |
| Administrative or HRM unavailable | Keep commands fail-closed; do not bypass permission/source checks | dependency UP and previously failing safe read succeeds |
| Unauthorized dependency probe | Confirm health endpoint protection and service topology; do not weaken business API security | approved protected-network/health configuration documented |
| Source drift or missing source | Assign the finding to the authoritative data owner; never rewrite the immutable snapshot | finding acknowledged/resolved with note and audit lineage |
| Storage missing/size mismatch | Preserve metadata and object store; quarantine operational changes until storage owner verifies | metadata/object consistency restored and a new run confirms it |
| Retention dispute | Place or retain a legal hold; do not purge | records/legal approval and eligibility report retained |
| Suspected secret or personal-data exposure | Revoke/rotate affected secrets, restrict access, preserve logs, notify the privacy/security authority | rotation and access review completed without copying exposed data |

## Escalation and closure

Escalate database corruption or loss to the database owner and execute only the applicable PostgreSQL or SQL Server
restore runbook against an isolated target. A restore is accepted only when the signed manifest verifies Flyway version,
constraints, representative counts, audit continuity, authorization, and storage references.

Close an incident only after the original failure is reproducibly absent, permission behavior remains fail-closed,
reconciliation/audit history is intact, and no destructive workaround or hidden data correction was used.

