ALTER TABLE "${primehrSchema}".prime_assessment_cycle
    ADD COLUMN opened_by VARCHAR(100);
ALTER TABLE "${primehrSchema}".prime_assessment_cycle
    ADD COLUMN opened_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE "${primehrSchema}".prime_assessment_cycle
    ADD COLUMN closed_by VARCHAR(100);
ALTER TABLE "${primehrSchema}".prime_assessment_cycle
    ADD COLUMN closed_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE "${primehrSchema}".prime_assessment_tool
    ADD COLUMN published_by VARCHAR(100);
ALTER TABLE "${primehrSchema}".prime_assessment_tool
    ADD COLUMN published_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE "${primehrSchema}".prime_assessment_case
    ADD COLUMN for_validation_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE "${primehrSchema}".prime_assessor_assignment
    ADD COLUMN submitted_by VARCHAR(100);
ALTER TABLE "${primehrSchema}".prime_assessor_assignment
    ADD COLUMN submitted_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE "${primehrSchema}".prime_assessment_rating (
    id VARCHAR(36) PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    assessor_assignment_id VARCHAR(36) NOT NULL
        REFERENCES "${primehrSchema}".prime_assessor_assignment (id),
    competency_id VARCHAR(36) NOT NULL REFERENCES "${primehrSchema}".prime_competency (id),
    attained_proficiency_level_id VARCHAR(36) NOT NULL
        REFERENCES "${primehrSchema}".prime_proficiency_level (id),
    remarks VARCHAR(2000),
    behavioral_notes VARCHAR(4000),
    active BOOLEAN NOT NULL,
    record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_prime_assessment_rating UNIQUE (assessor_assignment_id, competency_id)
);

CREATE TABLE "${primehrSchema}".prime_assessment_evidence (
    id VARCHAR(36) PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    assessment_rating_id VARCHAR(36) NOT NULL
        REFERENCES "${primehrSchema}".prime_assessment_rating (id),
    evidence_type VARCHAR(100) NOT NULL,
    title_reference VARCHAR(500) NOT NULL,
    evidence_date DATE NOT NULL,
    description VARCHAR(4000),
    source_system VARCHAR(100),
    source_reference VARCHAR(500),
    active BOOLEAN NOT NULL,
    record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_prime_assessment_assignment_inbox
    ON "${primehrSchema}".prime_assessor_assignment (agency_id, assessor_employee_no, active, status);
CREATE INDEX ix_prime_assessment_rating_assignment
    ON "${primehrSchema}".prime_assessment_rating (assessor_assignment_id, active);
CREATE INDEX ix_prime_assessment_evidence_rating
    ON "${primehrSchema}".prime_assessment_evidence (assessment_rating_id, active, evidence_date);
