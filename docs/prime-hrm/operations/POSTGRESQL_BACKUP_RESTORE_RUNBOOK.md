# PrimeHR PostgreSQL backup and restore runbook

This runbook applies only to the standalone PrimeHR database. It never targets a CoreHR database.

## Backup

1. Record the deployment version, Flyway version, database host identifier, database name, UTC start time, and operator in a new restore-verification manifest.
2. Use the platform-managed snapshot facility where available. For a logical backup, run `pg_dump` from an approved operations host using environment-provided credentials and the custom format (`--format=custom`). Do not place credentials in the command, script, repository, or manifest.
3. Store the encrypted backup outside the database host, record its byte size and SHA-256 checksum, and restrict access to authorized operations staff.
4. Verify `pg_restore --list` can read the archive before declaring the backup complete.

## Isolated restore drill

1. Provision an empty, isolated PostgreSQL database at the same or a supported newer engine version. Never overwrite production for a drill.
2. Restore roles/ownership according to the deployment platform, then run `pg_restore --clean --if-exists --no-owner` against the isolated database.
3. Start the matching PrimeHR build with Flyway validation enabled and `ddl-auto=validate`.
4. Complete every check in `RESTORE_VERIFICATION_MANIFEST.template.json`: Flyway version, schema validation, row counts, foreign keys, latest audit/reconciliation timestamps, representative authenticated read, and storage metadata checks.
5. Destroy the isolated environment only after the signed manifest and logs are stored in the approved evidence location.

Recovery is accepted only when all manifest checks pass. A failed drill is an incident finding; it must not be hidden by modifying production history.
