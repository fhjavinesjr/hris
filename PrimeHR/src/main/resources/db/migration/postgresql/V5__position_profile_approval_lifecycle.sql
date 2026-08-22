ALTER TABLE "${primehrSchema}".prime_position_profile
    ADD COLUMN submitted_by VARCHAR(100);

ALTER TABLE "${primehrSchema}".prime_position_profile
    ADD COLUMN submitted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE "${primehrSchema}".prime_position_profile
    ADD COLUMN approved_by VARCHAR(100);

ALTER TABLE "${primehrSchema}".prime_position_profile
    ADD COLUMN approved_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE "${primehrSchema}".prime_position_profile
    ADD CONSTRAINT ck_prime_profile_lifecycle_metadata CHECK (
        (status IN ('DRAFT', 'ARCHIVED')
            AND submitted_by IS NULL AND submitted_at IS NULL
            AND approved_by IS NULL AND approved_at IS NULL)
        OR (status = 'SUBMITTED'
            AND submitted_by IS NOT NULL AND submitted_at IS NOT NULL
            AND approved_by IS NULL AND approved_at IS NULL)
        OR (status = 'ACTIVE'
            AND submitted_by IS NOT NULL AND submitted_at IS NOT NULL
            AND approved_by IS NOT NULL AND approved_at IS NOT NULL)
    );

CREATE INDEX ix_prime_profile_effective_resolution
    ON "${primehrSchema}".prime_position_profile
        (agency_id, target_type, job_position_id, plantilla_id, status, effective_from, effective_to);
