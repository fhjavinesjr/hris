CREATE TABLE "${primehrSchema}".prime_competency_category (
    id VARCHAR(36) PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    active BOOLEAN NOT NULL,
    display_order INTEGER NOT NULL,
    effective_from DATE,
    effective_to DATE,
    record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_prime_category_agency_code UNIQUE (agency_id, code),
    CONSTRAINT ck_prime_category_code_upper CHECK (code = UPPER(code)),
    CONSTRAINT ck_prime_category_display_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_category_effectivity CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE TABLE "${primehrSchema}".prime_proficiency_scale (
    id VARCHAR(36) PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    active BOOLEAN NOT NULL,
    display_order INTEGER NOT NULL,
    effective_from DATE,
    effective_to DATE,
    record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_prime_scale_agency_code UNIQUE (agency_id, code),
    CONSTRAINT ck_prime_scale_code_upper CHECK (code = UPPER(code)),
    CONSTRAINT ck_prime_scale_display_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_scale_effectivity CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE TABLE "${primehrSchema}".prime_proficiency_level (
    id VARCHAR(36) PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    scale_id VARCHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL,
    label VARCHAR(150) NOT NULL,
    level_order INTEGER NOT NULL,
    description VARCHAR(1000),
    active BOOLEAN NOT NULL,
    display_order INTEGER NOT NULL,
    effective_from DATE,
    effective_to DATE,
    record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_prime_level_scale FOREIGN KEY (scale_id) REFERENCES "${primehrSchema}".prime_proficiency_scale (id),
    CONSTRAINT uk_prime_level_scale_code UNIQUE (scale_id, code),
    CONSTRAINT uk_prime_level_scale_order UNIQUE (scale_id, level_order),
    CONSTRAINT ck_prime_level_code_upper CHECK (code = UPPER(code)),
    CONSTRAINT ck_prime_level_order CHECK (level_order >= 1),
    CONSTRAINT ck_prime_level_display_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_level_effectivity CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE TABLE "${primehrSchema}".prime_competency (
    id VARCHAR(36) PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    definition VARCHAR(4000) NOT NULL,
    status VARCHAR(30) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    proficiency_scale_id VARCHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    display_order INTEGER NOT NULL,
    effective_from DATE,
    effective_to DATE,
    record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_prime_competency_category FOREIGN KEY (category_id) REFERENCES "${primehrSchema}".prime_competency_category (id),
    CONSTRAINT fk_prime_competency_scale FOREIGN KEY (proficiency_scale_id) REFERENCES "${primehrSchema}".prime_proficiency_scale (id),
    CONSTRAINT uk_prime_competency_agency_code UNIQUE (agency_id, code),
    CONSTRAINT ck_prime_competency_code_upper CHECK (code = UPPER(code)),
    CONSTRAINT ck_prime_competency_display_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_competency_effectivity CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE TABLE "${primehrSchema}".prime_behavioral_indicator (
    id VARCHAR(36) PRIMARY KEY,
    agency_id VARCHAR(64) NOT NULL,
    competency_id VARCHAR(36) NOT NULL,
    proficiency_level_id VARCHAR(36) NOT NULL,
    behavior_description VARCHAR(2000) NOT NULL,
    evidence_guidance VARCHAR(2000),
    active BOOLEAN NOT NULL,
    display_order INTEGER NOT NULL,
    effective_from DATE,
    effective_to DATE,
    record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_prime_indicator_competency FOREIGN KEY (competency_id) REFERENCES "${primehrSchema}".prime_competency (id),
    CONSTRAINT fk_prime_indicator_level FOREIGN KEY (proficiency_level_id) REFERENCES "${primehrSchema}".prime_proficiency_level (id),
    CONSTRAINT uk_prime_indicator_order UNIQUE (competency_id, proficiency_level_id, display_order),
    CONSTRAINT ck_prime_indicator_display_order CHECK (display_order >= 0),
    CONSTRAINT ck_prime_indicator_effectivity CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE INDEX ix_prime_category_agency_active ON "${primehrSchema}".prime_competency_category (agency_id, active, display_order);
CREATE INDEX ix_prime_scale_agency_active ON "${primehrSchema}".prime_proficiency_scale (agency_id, active, display_order);
CREATE INDEX ix_prime_level_agency_scale ON "${primehrSchema}".prime_proficiency_level (agency_id, scale_id, level_order);
CREATE INDEX ix_prime_competency_filter ON "${primehrSchema}".prime_competency (agency_id, category_id, active, display_order);
CREATE INDEX ix_prime_indicator_lookup ON "${primehrSchema}".prime_behavioral_indicator (agency_id, competency_id, proficiency_level_id, display_order);
