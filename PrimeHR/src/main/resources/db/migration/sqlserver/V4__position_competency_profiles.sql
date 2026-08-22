CREATE TABLE [${primehrSchema}].prime_position_profile (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_position_profile PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    target_key VARCHAR(100) NOT NULL,
    job_position_id BIGINT NOT NULL,
    plantilla_id BIGINT NULL,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(2000) NULL,
    status VARCHAR(30) NOT NULL,
    definition_version INT NOT NULL,
    supersedes_id VARCHAR(36) NULL,
    source_job_position_name NVARCHAR(200) NOT NULL,
    source_salary_grade BIGINT NULL,
    source_salary_step BIGINT NULL,
    source_plantilla_name NVARCHAR(200) NULL,
    source_fingerprint VARCHAR(64) NOT NULL,
    source_snapshot_at DATETIMEOFFSET NOT NULL,
    content_revision BIGINT NOT NULL CONSTRAINT df_prime_profile_content_revision DEFAULT 0,
    active BIT NOT NULL,
    display_order INT NOT NULL CONSTRAINT df_prime_profile_display_order DEFAULT 0,
    effective_from DATE NULL,
    effective_to DATE NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_profile_record_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_prime_profile_supersedes FOREIGN KEY (supersedes_id)
        REFERENCES [${primehrSchema}].prime_position_profile (id),
    CONSTRAINT uk_prime_profile_target_version UNIQUE (agency_id, target_key, definition_version),
    CONSTRAINT ck_prime_profile_target_type CHECK (target_type IN ('JOB_POSITION', 'PLANTILLA')),
    CONSTRAINT ck_prime_profile_target_identity CHECK (
        (target_type = 'JOB_POSITION' AND plantilla_id IS NULL
            AND target_key = CONCAT('JOB_POSITION:', CONVERT(VARCHAR(30), job_position_id)))
        OR (target_type = 'PLANTILLA' AND plantilla_id IS NOT NULL
            AND target_key = CONCAT('PLANTILLA:', CONVERT(VARCHAR(30), plantilla_id)))
    ),
    CONSTRAINT ck_prime_profile_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'ACTIVE', 'ARCHIVED')),
    CONSTRAINT ck_prime_profile_definition_version CHECK (definition_version >= 1),
    CONSTRAINT ck_prime_profile_display_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_profile_effectivity CHECK (
        effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE TABLE [${primehrSchema}].prime_position_profile_requirement (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_position_profile_requirement PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    profile_id VARCHAR(36) NOT NULL,
    competency_id VARCHAR(36) NOT NULL,
    required_proficiency_level_id VARCHAR(36) NOT NULL,
    classification VARCHAR(30) NOT NULL,
    criticality_code VARCHAR(50) NULL,
    remarks NVARCHAR(2000) NULL,
    active BIT NOT NULL,
    display_order INT NOT NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_profile_requirement_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_prime_profile_requirement_profile FOREIGN KEY (profile_id)
        REFERENCES [${primehrSchema}].prime_position_profile (id),
    CONSTRAINT fk_prime_profile_requirement_competency FOREIGN KEY (competency_id)
        REFERENCES [${primehrSchema}].prime_competency (id),
    CONSTRAINT fk_prime_profile_requirement_level FOREIGN KEY (required_proficiency_level_id)
        REFERENCES [${primehrSchema}].prime_proficiency_level (id),
    CONSTRAINT uk_prime_profile_requirement_competency UNIQUE (profile_id, competency_id),
    CONSTRAINT ck_prime_profile_requirement_classification CHECK (classification IN ('MANDATORY', 'DESIRABLE')),
    CONSTRAINT ck_prime_profile_requirement_display_order CHECK (display_order >= 0)
);

CREATE INDEX ix_prime_profile_filter
    ON [${primehrSchema}].prime_position_profile (agency_id, status, target_type, effective_from);
CREATE INDEX ix_prime_profile_target_chain
    ON [${primehrSchema}].prime_position_profile (agency_id, target_key, definition_version);
CREATE INDEX ix_prime_profile_requirement_order
    ON [${primehrSchema}].prime_position_profile_requirement (profile_id, active, display_order);
