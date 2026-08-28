CREATE TABLE [${primehrSchema}].prime_assessment_cycle (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_assessment_cycle PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    code VARCHAR(100) NOT NULL,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(2000) NULL,
    status VARCHAR(30) NOT NULL,
    active BIT NOT NULL,
    display_order INT NOT NULL,
    effective_from DATE NULL,
    effective_to DATE NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_assessment_cycle_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT uk_prime_assessment_cycle_code UNIQUE (agency_id, code),
    CONSTRAINT ck_prime_assessment_cycle_status CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED', 'ARCHIVED')),
    CONSTRAINT ck_prime_assessment_cycle_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_assessment_cycle_dates CHECK (
        effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE TABLE [${primehrSchema}].prime_assessment_tool (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_assessment_tool PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    cycle_id VARCHAR(36) NOT NULL,
    position_profile_id VARCHAR(36) NOT NULL,
    name NVARCHAR(200) NOT NULL,
    instructions NVARCHAR(4000) NULL,
    status VARCHAR(30) NOT NULL,
    profile_definition_version INT NOT NULL,
    profile_content_revision BIGINT NOT NULL,
    profile_target_key VARCHAR(100) NOT NULL,
    profile_name NVARCHAR(200) NOT NULL,
    profile_source_fingerprint VARCHAR(64) NOT NULL,
    active BIT NOT NULL,
    display_order INT NOT NULL,
    effective_from DATE NULL,
    effective_to DATE NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_assessment_tool_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_prime_assessment_tool_cycle FOREIGN KEY (cycle_id)
        REFERENCES [${primehrSchema}].prime_assessment_cycle (id),
    CONSTRAINT fk_prime_assessment_tool_profile FOREIGN KEY (position_profile_id)
        REFERENCES [${primehrSchema}].prime_position_profile (id),
    CONSTRAINT uk_prime_assessment_tool_name UNIQUE (cycle_id, name),
    CONSTRAINT ck_prime_assessment_tool_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT ck_prime_assessment_tool_profile_version CHECK (profile_definition_version >= 1),
    CONSTRAINT ck_prime_assessment_tool_order CHECK (display_order >= 0)
);

CREATE TABLE [${primehrSchema}].prime_assessment_tool_method (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_assessment_tool_method PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    tool_id VARCHAR(36) NOT NULL,
    method_code VARCHAR(50) NOT NULL,
    evidence_required BIT NOT NULL,
    active BIT NOT NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_assessment_tool_method_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_prime_assessment_tool_method_tool FOREIGN KEY (tool_id)
        REFERENCES [${primehrSchema}].prime_assessment_tool (id),
    CONSTRAINT uk_prime_assessment_tool_method UNIQUE (tool_id, method_code),
    CONSTRAINT ck_prime_assessment_method_code CHECK (method_code IN (
        'SELF_ASSESSMENT', 'IMMEDIATE_SUPERVISOR', 'AUTHORIZED_ASSESSOR', 'PANEL',
        'BEHAVIORAL_EVENT_INTERVIEW', 'WRITTEN_PRACTICAL', 'VALIDATED_PRIOR_EVIDENCE'))
);

CREATE TABLE [${primehrSchema}].prime_assessment_case (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_assessment_case PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    tool_id VARCHAR(36) NOT NULL,
    subject_employee_id BIGINT NOT NULL,
    subject_employee_no VARCHAR(100) NOT NULL,
    subject_display_name NVARCHAR(300) NOT NULL,
    appointment_id BIGINT NOT NULL,
    assumption_to_duty_date DATETIME2 NOT NULL,
    job_position_id BIGINT NOT NULL,
    plantilla_id BIGINT NULL,
    subject_source_fingerprint VARCHAR(64) NOT NULL,
    subject_source_updated_at DATETIME2 NULL,
    subject_snapshot_at DATETIMEOFFSET NOT NULL,
    status VARCHAR(30) NOT NULL,
    active BIT NOT NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_assessment_case_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_prime_assessment_case_tool FOREIGN KEY (tool_id)
        REFERENCES [${primehrSchema}].prime_assessment_tool (id),
    CONSTRAINT uk_prime_assessment_case_subject UNIQUE (tool_id, subject_employee_id),
    CONSTRAINT ck_prime_assessment_case_status CHECK (status IN (
        'DRAFT', 'ASSIGNED', 'IN_PROGRESS', 'FOR_VALIDATION', 'RETURNED', 'VALIDATED', 'ARCHIVED'))
);

CREATE TABLE [${primehrSchema}].prime_assessor_assignment (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_assessor_assignment PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    assessment_case_id VARCHAR(36) NOT NULL,
    method_code VARCHAR(50) NOT NULL,
    assessor_employee_id BIGINT NOT NULL,
    assessor_employee_no VARCHAR(100) NOT NULL,
    assessor_display_name NVARCHAR(300) NOT NULL,
    assignment_reason NVARCHAR(1000) NULL,
    assessor_source_fingerprint VARCHAR(64) NOT NULL,
    assessor_snapshot_at DATETIMEOFFSET NOT NULL,
    status VARCHAR(30) NOT NULL,
    active BIT NOT NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_assessor_assignment_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_prime_assessor_assignment_case FOREIGN KEY (assessment_case_id)
        REFERENCES [${primehrSchema}].prime_assessment_case (id),
    CONSTRAINT uk_prime_assessor_assignment UNIQUE
        (assessment_case_id, method_code, assessor_employee_id),
    CONSTRAINT ck_prime_assessor_assignment_status CHECK (status IN (
        'DRAFT', 'ASSIGNED', 'IN_PROGRESS', 'SUBMITTED', 'RETURNED', 'VALIDATED', 'ARCHIVED')),
    CONSTRAINT ck_prime_assessor_method CHECK (method_code IN (
        'SELF_ASSESSMENT', 'IMMEDIATE_SUPERVISOR', 'AUTHORIZED_ASSESSOR', 'PANEL',
        'BEHAVIORAL_EVENT_INTERVIEW', 'WRITTEN_PRACTICAL', 'VALIDATED_PRIOR_EVIDENCE'))
);

CREATE INDEX ix_prime_assessment_cycle_filter
    ON [${primehrSchema}].prime_assessment_cycle (agency_id, status, effective_from, effective_to);
CREATE INDEX ix_prime_assessment_tool_cycle
    ON [${primehrSchema}].prime_assessment_tool (cycle_id, status, display_order);
CREATE INDEX ix_prime_assessment_case_subject
    ON [${primehrSchema}].prime_assessment_case (agency_id, subject_employee_no, status);
CREATE INDEX ix_prime_assessment_case_tool
    ON [${primehrSchema}].prime_assessment_case (tool_id, status);
CREATE INDEX ix_prime_assessor_employee
    ON [${primehrSchema}].prime_assessor_assignment (agency_id, assessor_employee_no, status);
