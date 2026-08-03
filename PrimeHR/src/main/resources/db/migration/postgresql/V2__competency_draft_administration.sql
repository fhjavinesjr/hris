ALTER TABLE "${primehrSchema}".prime_competency_category
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE "${primehrSchema}".prime_competency_category
    ADD COLUMN definition_version INTEGER NOT NULL DEFAULT 1;
ALTER TABLE "${primehrSchema}".prime_competency_category
    ADD COLUMN supersedes_id VARCHAR(36);

ALTER TABLE "${primehrSchema}".prime_proficiency_scale
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE "${primehrSchema}".prime_proficiency_scale
    ADD COLUMN definition_version INTEGER NOT NULL DEFAULT 1;
ALTER TABLE "${primehrSchema}".prime_proficiency_scale
    ADD COLUMN supersedes_id VARCHAR(36);

ALTER TABLE "${primehrSchema}".prime_competency
    ADD COLUMN definition_version INTEGER NOT NULL DEFAULT 1;
ALTER TABLE "${primehrSchema}".prime_competency
    ADD COLUMN supersedes_id VARCHAR(36);

UPDATE "${primehrSchema}".prime_competency
SET status = 'ACTIVE';

ALTER TABLE "${primehrSchema}".prime_competency_category
    DROP CONSTRAINT uk_prime_category_agency_code;
ALTER TABLE "${primehrSchema}".prime_competency_category ADD CONSTRAINT uk_prime_category_agency_code_version
    UNIQUE (agency_id, code, definition_version);
ALTER TABLE "${primehrSchema}".prime_competency_category ADD CONSTRAINT ck_prime_category_status
    CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED'));
ALTER TABLE "${primehrSchema}".prime_competency_category ADD CONSTRAINT ck_prime_category_definition_version
    CHECK (definition_version >= 1);
ALTER TABLE "${primehrSchema}".prime_competency_category ADD CONSTRAINT fk_prime_category_supersedes
    FOREIGN KEY (supersedes_id) REFERENCES "${primehrSchema}".prime_competency_category (id);

ALTER TABLE "${primehrSchema}".prime_proficiency_scale
    DROP CONSTRAINT uk_prime_scale_agency_code;
ALTER TABLE "${primehrSchema}".prime_proficiency_scale ADD CONSTRAINT uk_prime_scale_agency_code_version
    UNIQUE (agency_id, code, definition_version);
ALTER TABLE "${primehrSchema}".prime_proficiency_scale ADD CONSTRAINT ck_prime_scale_status
    CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED'));
ALTER TABLE "${primehrSchema}".prime_proficiency_scale ADD CONSTRAINT ck_prime_scale_definition_version
    CHECK (definition_version >= 1);
ALTER TABLE "${primehrSchema}".prime_proficiency_scale ADD CONSTRAINT fk_prime_scale_supersedes
    FOREIGN KEY (supersedes_id) REFERENCES "${primehrSchema}".prime_proficiency_scale (id);

ALTER TABLE "${primehrSchema}".prime_competency
    DROP CONSTRAINT uk_prime_competency_agency_code;
ALTER TABLE "${primehrSchema}".prime_competency ADD CONSTRAINT uk_prime_competency_agency_code_version
    UNIQUE (agency_id, code, definition_version);
ALTER TABLE "${primehrSchema}".prime_competency ADD CONSTRAINT ck_prime_competency_status
    CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED'));
ALTER TABLE "${primehrSchema}".prime_competency ADD CONSTRAINT ck_prime_competency_definition_version
    CHECK (definition_version >= 1);
ALTER TABLE "${primehrSchema}".prime_competency ADD CONSTRAINT fk_prime_competency_supersedes
    FOREIGN KEY (supersedes_id) REFERENCES "${primehrSchema}".prime_competency (id);

CREATE TABLE "${primehrSchema}".prime_audit_event (
    id VARCHAR(36) PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(36) NOT NULL,
    business_version INTEGER,
    record_version BIGINT,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    previous_state VARCHAR(32000),
    new_state VARCHAR(32000),
    reason VARCHAR(1000),
    source_module VARCHAR(50) NOT NULL,
    correlation_id VARCHAR(100),
    CONSTRAINT ck_prime_audit_business_version CHECK (business_version IS NULL OR business_version >= 1)
);

CREATE INDEX ix_prime_audit_aggregate
    ON "${primehrSchema}".prime_audit_event (agency_id, aggregate_type, aggregate_id, occurred_at);
CREATE INDEX ix_prime_audit_actor_time
    ON "${primehrSchema}".prime_audit_event (agency_id, actor, occurred_at);
