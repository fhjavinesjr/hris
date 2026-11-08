CREATE TABLE "${primehrSchema}".rr_governance_policy (
    id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL,
    normalized_code VARCHAR(80) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_rr_policy_code UNIQUE (agency_id, normalized_code)
);
CREATE TABLE "${primehrSchema}".rr_governance_policy_version (
    id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, policy_id VARCHAR(36) NOT NULL,
    definition_version INTEGER NOT NULL, supersedes_id VARCHAR(36), title VARCHAR(200) NOT NULL,
    description VARCHAR(2000), legal_basis VARCHAR(2000) NOT NULL, policy_reference VARCHAR(250) NOT NULL,
    committee_required BOOLEAN NOT NULL, allows_individual_awards BOOLEAN NOT NULL,
    allows_group_awards BOOLEAN NOT NULL, allows_organization_awards BOOLEAN NOT NULL,
    allows_monetary_incentives BOOLEAN NOT NULL, allows_nonmonetary_incentives BOOLEAN NOT NULL,
    requires_agency_head_approval BOOLEAN NOT NULL, annual_report_required BOOLEAN NOT NULL,
    status VARCHAR(20) NOT NULL, effective_from DATE, effective_to DATE, published_by VARCHAR(100),
    published_at TIMESTAMP WITH TIME ZONE, retired_by VARCHAR(100), retired_at TIMESTAMP WITH TIME ZONE,
    retirement_reason VARCHAR(1000), record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_rr_policy_version_root FOREIGN KEY (policy_id) REFERENCES "${primehrSchema}".rr_governance_policy(id),
    CONSTRAINT fk_rr_policy_version_prior FOREIGN KEY (supersedes_id) REFERENCES "${primehrSchema}".rr_governance_policy_version(id),
    CONSTRAINT uk_rr_policy_version UNIQUE (agency_id, policy_id, definition_version),
    CONSTRAINT ck_rr_policy_version CHECK (definition_version >= 1),
    CONSTRAINT ck_rr_policy_award_types CHECK (allows_individual_awards OR allows_group_awards OR allows_organization_awards),
    CONSTRAINT ck_rr_policy_incentives CHECK (allows_monetary_incentives OR allows_nonmonetary_incentives),
    CONSTRAINT ck_rr_policy_status CHECK (status IN ('DRAFT','PUBLISHED','RETIRED')),
    CONSTRAINT ck_rr_policy_dates CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from),
    CONSTRAINT ck_rr_policy_lifecycle CHECK ((status='DRAFT' AND effective_from IS NULL AND published_by IS NULL AND published_at IS NULL) OR (status IN ('PUBLISHED','RETIRED') AND effective_from IS NOT NULL AND published_by IS NOT NULL AND published_at IS NOT NULL))
);
CREATE INDEX ix_rr_policy_effective ON "${primehrSchema}".rr_governance_policy_version(agency_id,status,effective_from,effective_to);

CREATE TABLE "${primehrSchema}".rr_award_category (
    id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL,
    normalized_code VARCHAR(80) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_rr_category_code UNIQUE (agency_id, normalized_code)
);
CREATE TABLE "${primehrSchema}".rr_award_category_version (
    id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, category_id VARCHAR(36) NOT NULL,
    definition_version INTEGER NOT NULL, supersedes_id VARCHAR(36), title VARCHAR(200) NOT NULL,
    description VARCHAR(2000), nominee_type VARCHAR(30) NOT NULL, incentive_type VARCHAR(30) NOT NULL,
    eligibility_guidance VARCHAR(4000) NOT NULL, evidence_requirements VARCHAR(4000) NOT NULL,
    minimum_score NUMERIC(10,2), criteria_revision INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL, effective_from DATE, effective_to DATE, published_by VARCHAR(100),
    published_at TIMESTAMP WITH TIME ZONE, retired_by VARCHAR(100), retired_at TIMESTAMP WITH TIME ZONE,
    retirement_reason VARCHAR(1000), record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_rr_category_version_root FOREIGN KEY (category_id) REFERENCES "${primehrSchema}".rr_award_category(id),
    CONSTRAINT fk_rr_category_version_prior FOREIGN KEY (supersedes_id) REFERENCES "${primehrSchema}".rr_award_category_version(id),
    CONSTRAINT uk_rr_category_version UNIQUE (agency_id, category_id, definition_version),
    CONSTRAINT ck_rr_category_version CHECK (definition_version >= 1),
    CONSTRAINT ck_rr_category_nominee CHECK (nominee_type IN ('INDIVIDUAL','GROUP','ORGANIZATION_UNIT')),
    CONSTRAINT ck_rr_category_incentive CHECK (incentive_type IN ('MONETARY','NON_MONETARY','BOTH')),
    CONSTRAINT ck_rr_category_score CHECK (minimum_score IS NULL OR minimum_score >= 0),
    CONSTRAINT ck_rr_category_status CHECK (status IN ('DRAFT','PUBLISHED','RETIRED')),
    CONSTRAINT ck_rr_category_dates CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from),
    CONSTRAINT ck_rr_category_lifecycle CHECK ((status='DRAFT' AND effective_from IS NULL AND published_by IS NULL AND published_at IS NULL) OR (status IN ('PUBLISHED','RETIRED') AND effective_from IS NOT NULL AND published_by IS NOT NULL AND published_at IS NOT NULL))
);
CREATE INDEX ix_rr_category_effective ON "${primehrSchema}".rr_award_category_version(agency_id,category_id,status,effective_from,effective_to);
CREATE TABLE "${primehrSchema}".rr_award_criterion (
    id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, category_version_id VARCHAR(36) NOT NULL,
    code VARCHAR(80) NOT NULL, normalized_code VARCHAR(80) NOT NULL, title VARCHAR(200) NOT NULL,
    description VARCHAR(2000), weight_percent NUMERIC(7,2) NOT NULL, maximum_score NUMERIC(10,2) NOT NULL,
    evidence_guidance VARCHAR(3000) NOT NULL, display_order INTEGER NOT NULL,
    record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_rr_criterion_category FOREIGN KEY (category_version_id) REFERENCES "${primehrSchema}".rr_award_category_version(id),
    CONSTRAINT uk_rr_criterion_code UNIQUE (agency_id,category_version_id,normalized_code),
    CONSTRAINT uk_rr_criterion_order UNIQUE (agency_id,category_version_id,display_order),
    CONSTRAINT ck_rr_criterion_values CHECK (weight_percent > 0 AND maximum_score > 0 AND display_order >= 1)
);
CREATE INDEX ix_rr_criterion_category ON "${primehrSchema}".rr_award_criterion(agency_id,category_version_id,display_order);

CREATE TABLE "${primehrSchema}".rr_committee_charter (
    id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL,
    normalized_code VARCHAR(80) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_rr_charter_code UNIQUE (agency_id, normalized_code)
);
CREATE TABLE "${primehrSchema}".rr_committee_charter_version (
    id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, charter_id VARCHAR(36) NOT NULL,
    definition_version INTEGER NOT NULL, supersedes_id VARCHAR(36), title VARCHAR(200) NOT NULL,
    description VARCHAR(2000), legal_basis VARCHAR(2000) NOT NULL, quorum_type VARCHAR(20) NOT NULL,
    quorum_value NUMERIC(7,2) NOT NULL, requires_secretariat BOOLEAN NOT NULL,
    requires_agency_head_decision BOOLEAN NOT NULL, conflict_declaration_required BOOLEAN NOT NULL,
    roster_revision INTEGER NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL,
    effective_from DATE, effective_to DATE, published_by VARCHAR(100), published_at TIMESTAMP WITH TIME ZONE,
    retired_by VARCHAR(100), retired_at TIMESTAMP WITH TIME ZONE, retirement_reason VARCHAR(1000),
    record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_rr_charter_version_root FOREIGN KEY (charter_id) REFERENCES "${primehrSchema}".rr_committee_charter(id),
    CONSTRAINT fk_rr_charter_version_prior FOREIGN KEY (supersedes_id) REFERENCES "${primehrSchema}".rr_committee_charter_version(id),
    CONSTRAINT uk_rr_charter_version UNIQUE (agency_id,charter_id,definition_version),
    CONSTRAINT ck_rr_charter_version CHECK (definition_version >= 1),
    CONSTRAINT ck_rr_charter_quorum_type CHECK (quorum_type IN ('COUNT','PERCENTAGE')),
    CONSTRAINT ck_rr_charter_quorum CHECK (quorum_value > 0 AND (quorum_type <> 'PERCENTAGE' OR quorum_value <= 100)),
    CONSTRAINT ck_rr_charter_status CHECK (status IN ('DRAFT','PUBLISHED','RETIRED')),
    CONSTRAINT ck_rr_charter_dates CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from),
    CONSTRAINT ck_rr_charter_lifecycle CHECK ((status='DRAFT' AND effective_from IS NULL AND published_by IS NULL AND published_at IS NULL) OR (status IN ('PUBLISHED','RETIRED') AND effective_from IS NOT NULL AND published_by IS NOT NULL AND published_at IS NOT NULL))
);
CREATE INDEX ix_rr_charter_effective ON "${primehrSchema}".rr_committee_charter_version(agency_id,charter_id,status,effective_from,effective_to);
CREATE TABLE "${primehrSchema}".rr_committee_member (
    id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, charter_version_id VARCHAR(36) NOT NULL,
    employee_no VARCHAR(100) NOT NULL, display_name VARCHAR(250) NOT NULL, position_title VARCHAR(250) NOT NULL,
    member_role VARCHAR(30) NOT NULL, voting_member BOOLEAN NOT NULL, effective_from DATE NOT NULL,
    effective_to DATE, source_version VARCHAR(120), record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_rr_member_charter FOREIGN KEY (charter_version_id) REFERENCES "${primehrSchema}".rr_committee_charter_version(id),
    CONSTRAINT uk_rr_member_employee UNIQUE (agency_id,charter_version_id,employee_no),
    CONSTRAINT ck_rr_member_role CHECK (member_role IN ('CHAIR','VICE_CHAIR','MEMBER','SECRETARIAT')),
    CONSTRAINT ck_rr_member_secretariat CHECK (member_role <> 'SECRETARIAT' OR voting_member = FALSE),
    CONSTRAINT ck_rr_member_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);
CREATE INDEX ix_rr_member_charter ON "${primehrSchema}".rr_committee_member(agency_id,charter_version_id,member_role);
