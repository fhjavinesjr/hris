# PrimeHR SQL Server backup and restore runbook

This runbook applies only to the standalone PrimeHR database. It never targets a CoreHR database.

## Backup

1. Record the deployment version, Flyway version, SQL Server instance identifier, database name, UTC start time, and operator in a new restore-verification manifest.
2. Use the agency-approved SQL Server full-backup job or `BACKUP DATABASE` from an authorized DBA session. Credentials and private storage paths must remain outside source control.
3. Enable backup checksum where supported, run `RESTORE VERIFYONLY ... WITH CHECKSUM`, and record the backup byte size and SHA-256 checksum.
4. Copy the encrypted backup to the approved recovery location with access restricted to authorized operations staff.

## Isolated restore drill

1. Restore to a new isolated database name on a compatible SQL Server instance. Never replace production for a drill.
2. Inspect logical file names first and use reviewed `MOVE` targets belonging to the isolated database.
3. Run `DBCC CHECKDB` on the restored database, then start the matching PrimeHR build with Flyway validation enabled and `ddl-auto=validate`.
4. Complete every check in `RESTORE_VERIFICATION_MANIFEST.template.json`: Flyway version, schema validation, row counts, foreign keys, latest audit/reconciliation timestamps, representative authenticated read, and storage metadata checks.
5. Destroy the isolated environment only after the signed manifest and logs are stored in the approved evidence location.

Recovery is accepted only when all manifest checks pass. A failed drill is an incident finding; it must not be hidden by modifying production history.
