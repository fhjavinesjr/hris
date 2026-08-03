CREATE TABLE [${primehrSchema}].prime_competency_category (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_competency_category PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name NVARCHAR(150) NOT NULL,
    description NVARCHAR(1000) NULL,
    active BIT NOT NULL,
    display_order INT NOT NULL,
    effective_from DATE NULL,
    effective_to DATE NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_category_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT uk_prime_category_agency_code UNIQUE (agency_id, code),
    CONSTRAINT ck_prime_category_code_upper CHECK (
        code COLLATE Latin1_General_100_BIN2 = UPPER(code) COLLATE Latin1_General_100_BIN2
    ),
    CONSTRAINT ck_prime_category_display_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_category_effectivity CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE TABLE [${primehrSchema}].prime_proficiency_scale (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_proficiency_scale PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name NVARCHAR(150) NOT NULL,
    description NVARCHAR(1000) NULL,
    active BIT NOT NULL,
    display_order INT NOT NULL,
    effective_from DATE NULL,
    effective_to DATE NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_scale_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT uk_prime_scale_agency_code UNIQUE (agency_id, code),
    CONSTRAINT ck_prime_scale_code_upper CHECK (
        code COLLATE Latin1_General_100_BIN2 = UPPER(code) COLLATE Latin1_General_100_BIN2
    ),
    CONSTRAINT ck_prime_scale_display_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_scale_effectivity CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE TABLE [${primehrSchema}].prime_proficiency_level (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_proficiency_level PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    scale_id VARCHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL,
    label NVARCHAR(150) NOT NULL,
    level_order INT NOT NULL,
    description NVARCHAR(1000) NULL,
    active BIT NOT NULL,
    display_order INT NOT NULL,
    effective_from DATE NULL,
    effective_to DATE NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_level_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_prime_level_scale FOREIGN KEY (scale_id) REFERENCES [${primehrSchema}].prime_proficiency_scale (id),
    CONSTRAINT uk_prime_level_scale_code UNIQUE (scale_id, code),
    CONSTRAINT uk_prime_level_scale_order UNIQUE (scale_id, level_order),
    CONSTRAINT ck_prime_level_code_upper CHECK (
        code COLLATE Latin1_General_100_BIN2 = UPPER(code) COLLATE Latin1_General_100_BIN2
    ),
    CONSTRAINT ck_prime_level_order CHECK (level_order >= 1),
    CONSTRAINT ck_prime_level_display_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_level_effectivity CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE TABLE [${primehrSchema}].prime_competency (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_competency PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name NVARCHAR(200) NOT NULL,
    definition NVARCHAR(4000) NOT NULL,
    status VARCHAR(30) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    proficiency_scale_id VARCHAR(36) NOT NULL,
    active BIT NOT NULL,
    display_order INT NOT NULL,
    effective_from DATE NULL,
    effective_to DATE NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_competency_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_prime_competency_category FOREIGN KEY (category_id) REFERENCES [${primehrSchema}].prime_competency_category (id),
    CONSTRAINT fk_prime_competency_scale FOREIGN KEY (proficiency_scale_id) REFERENCES [${primehrSchema}].prime_proficiency_scale (id),
    CONSTRAINT uk_prime_competency_agency_code UNIQUE (agency_id, code),
    CONSTRAINT ck_prime_competency_code_upper CHECK (
        code COLLATE Latin1_General_100_BIN2 = UPPER(code) COLLATE Latin1_General_100_BIN2
    ),
    CONSTRAINT ck_prime_competency_display_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_competency_effectivity CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE TABLE [${primehrSchema}].prime_behavioral_indicator (
    id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_behavioral_indicator PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    competency_id VARCHAR(36) NOT NULL,
    proficiency_level_id VARCHAR(36) NOT NULL,
    behavior_description NVARCHAR(2000) NOT NULL,
    evidence_guidance NVARCHAR(2000) NULL,
    active BIT NOT NULL,
    display_order INT NOT NULL,
    effective_from DATE NULL,
    effective_to DATE NULL,
    record_version BIGINT NOT NULL CONSTRAINT df_prime_indicator_version DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_prime_indicator_competency FOREIGN KEY (competency_id) REFERENCES [${primehrSchema}].prime_competency (id),
    CONSTRAINT fk_prime_indicator_level FOREIGN KEY (proficiency_level_id) REFERENCES [${primehrSchema}].prime_proficiency_level (id),
    CONSTRAINT uk_prime_indicator_order UNIQUE (competency_id, proficiency_level_id, display_order),
    CONSTRAINT ck_prime_indicator_display_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_indicator_effectivity CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE INDEX ix_prime_category_agency_active ON [${primehrSchema}].prime_competency_category (agency_id, active, display_order);
CREATE INDEX ix_prime_scale_agency_active ON [${primehrSchema}].prime_proficiency_scale (agency_id, active, display_order);
CREATE INDEX ix_prime_level_agency_scale ON [${primehrSchema}].prime_proficiency_level (agency_id, scale_id, level_order);
CREATE INDEX ix_prime_competency_filter ON [${primehrSchema}].prime_competency (agency_id, category_id, active, display_order);
CREATE INDEX ix_prime_indicator_lookup ON [${primehrSchema}].prime_behavioral_indicator (agency_id, competency_id, proficiency_level_id, display_order);
