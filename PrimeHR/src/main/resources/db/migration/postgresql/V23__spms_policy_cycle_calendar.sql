CREATE TABLE "${primehrSchema}".spms_policy (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL,
 normalized_code VARCHAR(80) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_spms_policy_code UNIQUE(agency_id,normalized_code)
);

CREATE TABLE "${primehrSchema}".spms_policy_version (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, policy_id VARCHAR(36) NOT NULL,
 definition_version INTEGER NOT NULL, supersedes_id VARCHAR(36), title VARCHAR(200) NOT NULL,
 description VARCHAR(2000), legal_basis VARCHAR(1000) NOT NULL, display_label VARCHAR(120) NOT NULL,
 cycle_frequency VARCHAR(20) NOT NULL, requires_mid_cycle_review BOOLEAN NOT NULL,
 requires_self_assessment BOOLEAN NOT NULL, requires_pmt_calibration BOOLEAN NOT NULL,
 requires_acknowledgment BOOLEAN NOT NULL, requires_appeal BOOLEAN NOT NULL,
 status VARCHAR(20) NOT NULL, effective_from DATE, effective_to DATE,
 published_by VARCHAR(100), published_at TIMESTAMP WITH TIME ZONE,
 retired_by VARCHAR(100), retired_at TIMESTAMP WITH TIME ZONE, retirement_reason VARCHAR(1000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_policy_version_policy FOREIGN KEY(policy_id) REFERENCES "${primehrSchema}".spms_policy(id),
 CONSTRAINT fk_spms_policy_version_prior FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".spms_policy_version(id),
 CONSTRAINT uk_spms_policy_version UNIQUE(agency_id,policy_id,definition_version),
 CONSTRAINT ck_spms_policy_version_number CHECK(definition_version>=1),
 CONSTRAINT ck_spms_policy_frequency CHECK(cycle_frequency IN ('ANNUAL','SEMI_ANNUAL','QUARTERLY','CUSTOM')),
 CONSTRAINT ck_spms_policy_status CHECK(status IN ('DRAFT','PUBLISHED','RETIRED')),
 CONSTRAINT ck_spms_policy_dates CHECK(effective_to IS NULL OR effective_from IS NULL OR effective_to>=effective_from),
 CONSTRAINT ck_spms_policy_lifecycle CHECK((status='DRAFT' AND effective_from IS NULL AND published_by IS NULL AND published_at IS NULL) OR (status IN ('PUBLISHED','RETIRED') AND effective_from IS NOT NULL AND published_by IS NOT NULL AND published_at IS NOT NULL)),
 CONSTRAINT ck_spms_policy_retired CHECK((status<>'RETIRED' AND retired_by IS NULL AND retired_at IS NULL AND retirement_reason IS NULL) OR (status='RETIRED' AND retired_by IS NOT NULL AND retired_at IS NOT NULL AND retirement_reason IS NOT NULL))
);
CREATE INDEX ix_spms_policy_effective ON "${primehrSchema}".spms_policy_version(agency_id,status,effective_from,effective_to);

CREATE TABLE "${primehrSchema}".spms_cycle (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL,
 normalized_code VARCHAR(80) NOT NULL, name VARCHAR(200) NOT NULL, period_start DATE NOT NULL,
 period_end DATE NOT NULL, policy_version_id VARCHAR(36) NOT NULL, timezone VARCHAR(60) NOT NULL,
 coverage_scope VARCHAR(30) NOT NULL, status VARCHAR(20) NOT NULL, calendar_revision INTEGER NOT NULL DEFAULT 0,
 opened_by VARCHAR(100), opened_at TIMESTAMP WITH TIME ZONE,
 closed_by VARCHAR(100), closed_at TIMESTAMP WITH TIME ZONE,
 cancelled_by VARCHAR(100), cancelled_at TIMESTAMP WITH TIME ZONE, transition_reason VARCHAR(1000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_cycle_policy FOREIGN KEY(policy_version_id) REFERENCES "${primehrSchema}".spms_policy_version(id),
 CONSTRAINT uk_spms_cycle_code UNIQUE(agency_id,normalized_code),
 CONSTRAINT ck_spms_cycle_dates CHECK(period_end>=period_start),
 CONSTRAINT ck_spms_cycle_scope CHECK(coverage_scope='AGENCY_WIDE'),
 CONSTRAINT ck_spms_cycle_status CHECK(status IN ('DRAFT','OPEN','CLOSED','CANCELLED'))
);
CREATE INDEX ix_spms_cycle_period ON "${primehrSchema}".spms_cycle(agency_id,status,period_start,period_end);

CREATE TABLE "${primehrSchema}".spms_cycle_milestone (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, cycle_id VARCHAR(36) NOT NULL,
 milestone_type VARCHAR(40) NOT NULL, milestone_key VARCHAR(120) NOT NULL, label VARCHAR(200) NOT NULL,
 starts_at TIMESTAMP WITH TIME ZONE NOT NULL, ends_at TIMESTAMP WITH TIME ZONE,
 required BOOLEAN NOT NULL, display_order INTEGER NOT NULL, instructions VARCHAR(2000),
 post_cycle_closeout BOOLEAN NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_milestone_cycle FOREIGN KEY(cycle_id) REFERENCES "${primehrSchema}".spms_cycle(id),
 CONSTRAINT uk_spms_milestone_order UNIQUE(agency_id,cycle_id,display_order),
 CONSTRAINT uk_spms_milestone_type UNIQUE(agency_id,cycle_id,milestone_key),
 CONSTRAINT ck_spms_milestone_order CHECK(display_order>0),
 CONSTRAINT ck_spms_milestone_dates CHECK(ends_at IS NULL OR ends_at>=starts_at),
 CONSTRAINT ck_spms_milestone_type CHECK(milestone_type IN ('PLANNING_OPEN','PLANNING_DUE','APPROVAL_DUE','MONITORING_START','MID_CYCLE_REVIEW','ACCOMPLISHMENT_DUE','RATING_DUE','CALIBRATION_DUE','FINALIZATION_DUE','ACKNOWLEDGMENT_DUE','APPEAL_DUE','CUSTOM'))
);
CREATE INDEX ix_spms_milestone_cycle ON "${primehrSchema}".spms_cycle_milestone(agency_id,cycle_id,display_order);
