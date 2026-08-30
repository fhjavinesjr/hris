CREATE TABLE "${primehrSchema}".rsp_applicant_account (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, normalized_email VARCHAR(320) NOT NULL,
 email VARCHAR(320) NOT NULL, password_hash VARCHAR(100) NOT NULL, display_name VARCHAR(200) NOT NULL,
 status VARCHAR(20) NOT NULL, failed_attempts INTEGER NOT NULL, locked_until TIMESTAMP WITH TIME ZONE,
 last_login_at TIMESTAMP WITH TIME ZONE, record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_rsp_applicant_email UNIQUE(agency_id,normalized_email),
 CONSTRAINT ck_rsp_applicant_status CHECK(status IN ('ACTIVE','LOCKED','DISABLED')), CONSTRAINT ck_rsp_applicant_attempts CHECK(failed_attempts>=0)
);
CREATE TABLE "${primehrSchema}".rsp_privacy_notice (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, title VARCHAR(200) NOT NULL, body VARCHAR(8000) NOT NULL,
 retention_summary VARCHAR(1000) NOT NULL, definition_version INTEGER NOT NULL, effective_from DATE NOT NULL,
 effective_to DATE, status VARCHAR(20) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_rsp_privacy_version UNIQUE(agency_id,definition_version), CONSTRAINT ck_rsp_privacy_status CHECK(status IN ('DRAFT','ACTIVE','ARCHIVED')),
 CONSTRAINT ck_rsp_privacy_dates CHECK(effective_to IS NULL OR effective_to>=effective_from), CONSTRAINT ck_rsp_privacy_version CHECK(definition_version>=1)
);
CREATE TABLE "${primehrSchema}".rsp_applicant_consent (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, applicant_id VARCHAR(36) NOT NULL,
 privacy_notice_id VARCHAR(36) NOT NULL, notice_version INTEGER NOT NULL, accepted_at TIMESTAMP WITH TIME ZONE NOT NULL,
 evidence_ip VARCHAR(64), evidence_user_agent VARCHAR(500), withdrawn_at TIMESTAMP WITH TIME ZONE,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_consent_applicant FOREIGN KEY(applicant_id) REFERENCES "${primehrSchema}".rsp_applicant_account(id),
 CONSTRAINT fk_rsp_consent_notice FOREIGN KEY(privacy_notice_id) REFERENCES "${primehrSchema}".rsp_privacy_notice(id),
 CONSTRAINT uk_rsp_consent_notice UNIQUE(agency_id,applicant_id,privacy_notice_id)
);
CREATE TABLE "${primehrSchema}".rsp_applicant_profile (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, applicant_id VARCHAR(36) NOT NULL,
 given_name VARCHAR(100) NOT NULL, middle_name VARCHAR(100), family_name VARCHAR(100) NOT NULL, suffix VARCHAR(30),
 birth_date DATE, mobile_number VARCHAR(50), address_line VARCHAR(500), city VARCHAR(100), province VARCHAR(100),
 postal_code VARCHAR(20), citizenship VARCHAR(100), declaration_accepted BOOLEAN NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_profile_applicant FOREIGN KEY(applicant_id) REFERENCES "${primehrSchema}".rsp_applicant_account(id),
 CONSTRAINT uk_rsp_applicant_profile UNIQUE(agency_id,applicant_id)
);
CREATE TABLE "${primehrSchema}".rsp_applicant_profile_entry (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, profile_id VARCHAR(36) NOT NULL,
 entry_type VARCHAR(30) NOT NULL, title VARCHAR(300) NOT NULL, organization_name VARCHAR(300), date_from DATE,
 date_to DATE, details VARCHAR(2000), display_order INTEGER NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL, CONSTRAINT fk_rsp_profile_entry FOREIGN KEY(profile_id) REFERENCES "${primehrSchema}".rsp_applicant_profile(id),
 CONSTRAINT uk_rsp_profile_entry_order UNIQUE(agency_id,profile_id,entry_type,display_order),
 CONSTRAINT ck_rsp_profile_entry_type CHECK(entry_type IN ('EDUCATION','WORK_EXPERIENCE','TRAINING','ELIGIBILITY','LICENSE','REFERENCE')),
 CONSTRAINT ck_rsp_profile_entry_dates CHECK(date_to IS NULL OR date_from IS NULL OR date_to>=date_from), CONSTRAINT ck_rsp_profile_entry_order CHECK(display_order>=0)
);
CREATE TABLE "${primehrSchema}".rsp_applicant_document (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, applicant_id VARCHAR(36) NOT NULL,
 document_type VARCHAR(100) NOT NULL, original_filename VARCHAR(255) NOT NULL, storage_provider VARCHAR(20) NOT NULL,
 storage_object_key VARCHAR(500) NOT NULL, media_type VARCHAR(150) NOT NULL, byte_size BIGINT NOT NULL,
 sha256_checksum VARCHAR(64) NOT NULL, classification VARCHAR(30) NOT NULL, scan_status VARCHAR(20) NOT NULL,
 uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL, active BOOLEAN NOT NULL, replaces_document_id VARCHAR(36),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_document_applicant FOREIGN KEY(applicant_id) REFERENCES "${primehrSchema}".rsp_applicant_account(id),
 CONSTRAINT fk_rsp_document_replaces FOREIGN KEY(replaces_document_id) REFERENCES "${primehrSchema}".rsp_applicant_document(id),
 CONSTRAINT uk_rsp_document_object UNIQUE(storage_object_key), CONSTRAINT ck_rsp_document_size CHECK(byte_size>0),
 CONSTRAINT ck_rsp_document_scan CHECK(scan_status IN ('PENDING_SCAN','CLEAN','REJECTED'))
);
CREATE INDEX ix_rsp_privacy_effective ON "${primehrSchema}".rsp_privacy_notice(agency_id,status,effective_from,effective_to);
CREATE INDEX ix_rsp_consent_applicant ON "${primehrSchema}".rsp_applicant_consent(agency_id,applicant_id,accepted_at);
CREATE INDEX ix_rsp_profile_entry ON "${primehrSchema}".rsp_applicant_profile_entry(agency_id,profile_id,entry_type,display_order);
CREATE INDEX ix_rsp_document_owner ON "${primehrSchema}".rsp_applicant_document(agency_id,applicant_id,active);
