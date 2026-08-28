ALTER TABLE [${primehrSchema}].prime_assessment_cycle ADD
    opened_by VARCHAR(100) NULL,
    opened_at DATETIMEOFFSET NULL,
    closed_by VARCHAR(100) NULL,
    closed_at DATETIMEOFFSET NULL;

ALTER TABLE [${primehrSchema}].prime_assessment_tool ADD
    published_by VARCHAR(100) NULL,
    published_at DATETIMEOFFSET NULL;

ALTER TABLE [${primehrSchema}].prime_assessment_case ADD
    for_validation_at DATETIMEOFFSET NULL;

ALTER TABLE [${primehrSchema}].prime_assessor_assignment ADD
    submitted_by VARCHAR(100) NULL,
    submitted_at DATETIMEOFFSET NULL;

CREATE TABLE [${primehrSchema}].prime_assessment_rating (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_assessment_rating PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    assessor_assignment_id VARCHAR(36) NOT NULL,
    competency_id VARCHAR(36) NOT NULL,
    attained_proficiency_level_id VARCHAR(36) NOT NULL,
    remarks NVARCHAR(2000) NULL,
    behavioral_notes NVARCHAR(4000) NULL,
    active BIT NOT NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_assessment_rating_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_prime_assessment_rating_assignment FOREIGN KEY (assessor_assignment_id)
        REFERENCES [${primehrSchema}].prime_assessor_assignment (id),
    CONSTRAINT fk_prime_assessment_rating_competency FOREIGN KEY (competency_id)
        REFERENCES [${primehrSchema}].prime_competency (id),
    CONSTRAINT fk_prime_assessment_rating_level FOREIGN KEY (attained_proficiency_level_id)
        REFERENCES [${primehrSchema}].prime_proficiency_level (id),
    CONSTRAINT uk_prime_assessment_rating UNIQUE (assessor_assignment_id, competency_id)
);

CREATE TABLE [${primehrSchema}].prime_assessment_evidence (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_assessment_evidence PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    assessment_rating_id VARCHAR(36) NOT NULL,
    evidence_type VARCHAR(100) NOT NULL,
    title_reference NVARCHAR(500) NOT NULL,
    evidence_date DATE NOT NULL,
    description NVARCHAR(4000) NULL,
    source_system VARCHAR(100) NULL,
    source_reference NVARCHAR(500) NULL,
    active BIT NOT NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_assessment_evidence_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_prime_assessment_evidence_rating FOREIGN KEY (assessment_rating_id)
        REFERENCES [${primehrSchema}].prime_assessment_rating (id)
);

CREATE INDEX ix_prime_assessment_assignment_inbox
    ON [${primehrSchema}].prime_assessor_assignment (agency_id, assessor_employee_no, active, status);
CREATE INDEX ix_prime_assessment_rating_assignment
    ON [${primehrSchema}].prime_assessment_rating (assessor_assignment_id, active);
CREATE INDEX ix_prime_assessment_evidence_rating
    ON [${primehrSchema}].prime_assessment_evidence (assessment_rating_id, active, evidence_date);
