CREATE TABLE "${primehrSchema}".primehr_reconciliation_run (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, trigger_type VARCHAR(20) NOT NULL,
 status VARCHAR(30) NOT NULL, started_by VARCHAR(100) NOT NULL, started_at TIMESTAMP WITH TIME ZONE NOT NULL,
 completed_at TIMESTAMP WITH TIME ZONE, checked_count INTEGER NOT NULL DEFAULT 0,
 finding_count INTEGER NOT NULL DEFAULT 0, failure_summary VARCHAR(1000), record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT ck_reconciliation_counts CHECK (checked_count >= 0 AND finding_count >= 0 AND finding_count <= checked_count)
);
CREATE TABLE "${primehrSchema}".primehr_reconciliation_finding (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, run_id VARCHAR(36) NOT NULL,
 kind VARCHAR(40) NOT NULL, status VARCHAR(20) NOT NULL, record_type VARCHAR(80) NOT NULL,
 record_id VARCHAR(100) NOT NULL, source_id VARCHAR(100), stored_version BIGINT,
 stored_fingerprint VARCHAR(64), observed_version BIGINT, observed_fingerprint VARCHAR(64),
 summary VARCHAR(1000) NOT NULL, acknowledged_by VARCHAR(100), acknowledged_at TIMESTAMP WITH TIME ZONE,
 resolved_by VARCHAR(100), resolved_at TIMESTAMP WITH TIME ZONE, resolution_note VARCHAR(2000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_reconciliation_finding_run FOREIGN KEY (run_id) REFERENCES "${primehrSchema}".primehr_reconciliation_run(id)
);
CREATE TABLE "${primehrSchema}".primehr_retention_policy_version (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, code VARCHAR(100) NOT NULL,
 policy_version INTEGER NOT NULL, retention_tag VARCHAR(100) NOT NULL, retention_days INTEGER NOT NULL,
 effective_from DATE NOT NULL, effective_to DATE, basis VARCHAR(1000) NOT NULL, published BOOLEAN NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_retention_policy_version UNIQUE (agency_id, code, policy_version),
 CONSTRAINT ck_retention_policy_values CHECK (policy_version >= 1 AND retention_days >= 1 AND (effective_to IS NULL OR effective_to >= effective_from))
);
CREATE TABLE "${primehrSchema}".primehr_legal_hold (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, record_type VARCHAR(80) NOT NULL,
 record_id VARCHAR(100) NOT NULL, reason VARCHAR(2000) NOT NULL, reference_no VARCHAR(200) NOT NULL,
 placed_by VARCHAR(100) NOT NULL, placed_at TIMESTAMP WITH TIME ZONE NOT NULL, active BOOLEAN NOT NULL,
 released_by VARCHAR(100), released_at TIMESTAMP WITH TIME ZONE, release_reason VARCHAR(2000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_reconciliation_run_started ON "${primehrSchema}".primehr_reconciliation_run(agency_id, started_at);
CREATE INDEX ix_reconciliation_finding_status ON "${primehrSchema}".primehr_reconciliation_finding(agency_id, status, kind);
CREATE INDEX ix_retention_policy_tag ON "${primehrSchema}".primehr_retention_policy_version(agency_id, retention_tag, effective_from);
CREATE INDEX ix_legal_hold_subject ON "${primehrSchema}".primehr_legal_hold(agency_id, record_type, record_id, active);
