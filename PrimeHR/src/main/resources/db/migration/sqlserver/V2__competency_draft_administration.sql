ALTER TABLE [${primehrSchema}].prime_competency_category ADD
    status VARCHAR(30) NOT NULL CONSTRAINT df_prime_category_status DEFAULT ('ACTIVE'),
    definition_version INT NOT NULL CONSTRAINT df_prime_category_definition_version DEFAULT (1),
    supersedes_id VARCHAR(36) NULL;

ALTER TABLE [${primehrSchema}].prime_proficiency_scale ADD
    status VARCHAR(30) NOT NULL CONSTRAINT df_prime_scale_status DEFAULT ('ACTIVE'),
    definition_version INT NOT NULL CONSTRAINT df_prime_scale_definition_version DEFAULT (1),
    supersedes_id VARCHAR(36) NULL;

ALTER TABLE [${primehrSchema}].prime_competency ADD
    definition_version INT NOT NULL CONSTRAINT df_prime_competency_definition_version DEFAULT (1),
    supersedes_id VARCHAR(36) NULL;

UPDATE [${primehrSchema}].prime_competency SET status = 'ACTIVE';

ALTER TABLE [${primehrSchema}].prime_competency_category DROP CONSTRAINT uk_prime_category_agency_code;
ALTER TABLE [${primehrSchema}].prime_competency_category ADD
    CONSTRAINT uk_prime_category_agency_code_version UNIQUE (agency_id, code, definition_version),
    CONSTRAINT ck_prime_category_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
    CONSTRAINT ck_prime_category_definition_version CHECK (definition_version >= 1),
    CONSTRAINT fk_prime_category_supersedes FOREIGN KEY (supersedes_id)
        REFERENCES [${primehrSchema}].prime_competency_category (id);

ALTER TABLE [${primehrSchema}].prime_proficiency_scale DROP CONSTRAINT uk_prime_scale_agency_code;
ALTER TABLE [${primehrSchema}].prime_proficiency_scale ADD
    CONSTRAINT uk_prime_scale_agency_code_version UNIQUE (agency_id, code, definition_version),
    CONSTRAINT ck_prime_scale_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
    CONSTRAINT ck_prime_scale_definition_version CHECK (definition_version >= 1),
    CONSTRAINT fk_prime_scale_supersedes FOREIGN KEY (supersedes_id)
        REFERENCES [${primehrSchema}].prime_proficiency_scale (id);

ALTER TABLE [${primehrSchema}].prime_competency DROP CONSTRAINT uk_prime_competency_agency_code;
ALTER TABLE [${primehrSchema}].prime_competency ADD
    CONSTRAINT uk_prime_competency_agency_code_version UNIQUE (agency_id, code, definition_version),
    CONSTRAINT ck_prime_competency_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
    CONSTRAINT ck_prime_competency_definition_version CHECK (definition_version >= 1),
    CONSTRAINT fk_prime_competency_supersedes FOREIGN KEY (supersedes_id)
        REFERENCES [${primehrSchema}].prime_competency (id);

CREATE TABLE [${primehrSchema}].prime_audit_event (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_audit_event PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(36) NOT NULL,
    business_version INT NULL,
    record_version BIGINT NULL,
    occurred_at DATETIMEOFFSET NOT NULL,
    previous_state VARCHAR(MAX) NULL,
    new_state VARCHAR(MAX) NULL,
    reason NVARCHAR(1000) NULL,
    source_module VARCHAR(50) NOT NULL,
    correlation_id VARCHAR(100) NULL,
    CONSTRAINT ck_prime_audit_business_version CHECK (business_version IS NULL OR business_version >= 1)
);

CREATE INDEX ix_prime_audit_aggregate
    ON [${primehrSchema}].prime_audit_event (agency_id, aggregate_type, aggregate_id, occurred_at);
CREATE INDEX ix_prime_audit_actor_time
    ON [${primehrSchema}].prime_audit_event (agency_id, actor, occurred_at);
