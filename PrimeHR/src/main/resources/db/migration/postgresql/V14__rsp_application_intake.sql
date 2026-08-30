CREATE TABLE "${primehrSchema}".rsp_position_application (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, applicant_id VARCHAR(36) NOT NULL,
 vacancy_publication_id VARCHAR(36) NOT NULL, application_version INTEGER NOT NULL,
 status VARCHAR(20) NOT NULL, safe_status VARCHAR(30) NOT NULL, acknowledgment_number VARCHAR(50),
 privacy_notice_id VARCHAR(36), privacy_notice_version INTEGER, vacancy_snapshot VARCHAR(32000),
 qualification_snapshot VARCHAR(32000), competency_snapshot VARCHAR(32000), profile_snapshot VARCHAR(32000),
 draft_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 submitted_at TIMESTAMP WITH TIME ZONE, withdrawn_at TIMESTAMP WITH TIME ZONE, withdrawal_reason VARCHAR(1000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_application_applicant FOREIGN KEY(applicant_id) REFERENCES "${primehrSchema}".rsp_applicant_account(id),
 CONSTRAINT fk_rsp_application_publication FOREIGN KEY(vacancy_publication_id) REFERENCES "${primehrSchema}".rsp_vacancy_publication(id),
 CONSTRAINT fk_rsp_application_notice FOREIGN KEY(privacy_notice_id) REFERENCES "${primehrSchema}".rsp_privacy_notice(id),
 CONSTRAINT uk_rsp_application_version UNIQUE(agency_id,applicant_id,vacancy_publication_id,application_version),
 CONSTRAINT ck_rsp_application_version CHECK(application_version>=1),
 CONSTRAINT ck_rsp_application_status CHECK(status IN ('DRAFT','SUBMITTED','WITHDRAWN')),
 CONSTRAINT ck_rsp_application_safe_status CHECK(safe_status IN ('DRAFT','SUBMITTED','WITHDRAWN')),
 CONSTRAINT ck_rsp_application_submission CHECK(
   (status='DRAFT' AND acknowledgment_number IS NULL AND submitted_at IS NULL)
   OR (status IN ('SUBMITTED','WITHDRAWN') AND acknowledgment_number IS NOT NULL AND submitted_at IS NOT NULL)),
 CONSTRAINT ck_rsp_application_withdrawal CHECK(
   (status<>'WITHDRAWN' AND withdrawn_at IS NULL AND withdrawal_reason IS NULL)
   OR (status='WITHDRAWN' AND withdrawn_at IS NOT NULL AND withdrawal_reason IS NOT NULL))
);
CREATE UNIQUE INDEX uk_rsp_application_acknowledgment ON "${primehrSchema}".rsp_position_application(agency_id,acknowledgment_number);
CREATE INDEX ix_rsp_application_owner ON "${primehrSchema}".rsp_position_application(agency_id,applicant_id,created_at);
CREATE INDEX ix_rsp_application_queue ON "${primehrSchema}".rsp_position_application(agency_id,status,submitted_at);
CREATE INDEX ix_rsp_application_vacancy ON "${primehrSchema}".rsp_position_application(agency_id,vacancy_publication_id,status);

CREATE TABLE "${primehrSchema}".rsp_application_document_snapshot (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, application_id VARCHAR(36) NOT NULL,
 applicant_document_id VARCHAR(36) NOT NULL, document_type VARCHAR(100) NOT NULL,
 original_filename VARCHAR(255) NOT NULL, storage_provider VARCHAR(20) NOT NULL,
 storage_object_key VARCHAR(500) NOT NULL, media_type VARCHAR(150) NOT NULL,
 byte_size BIGINT NOT NULL, sha256_checksum VARCHAR(64) NOT NULL, classification VARCHAR(30) NOT NULL,
 display_order INTEGER NOT NULL, record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_appdoc_application FOREIGN KEY(application_id) REFERENCES "${primehrSchema}".rsp_position_application(id),
 CONSTRAINT fk_rsp_appdoc_document FOREIGN KEY(applicant_document_id) REFERENCES "${primehrSchema}".rsp_applicant_document(id),
 CONSTRAINT uk_rsp_application_document UNIQUE(agency_id,application_id,applicant_document_id),
 CONSTRAINT ck_rsp_appdoc_size CHECK(byte_size>0), CONSTRAINT ck_rsp_appdoc_order CHECK(display_order>=0)
);
CREATE INDEX ix_rsp_appdoc_application ON "${primehrSchema}".rsp_application_document_snapshot(agency_id,application_id,display_order);

CREATE TABLE "${primehrSchema}".rsp_applicant_communication (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, application_id VARCHAR(36) NOT NULL,
 applicant_id VARCHAR(36) NOT NULL, direction VARCHAR(30) NOT NULL, channel VARCHAR(20) NOT NULL,
 subject VARCHAR(300) NOT NULL, body VARCHAR(4000) NOT NULL, actor VARCHAR(100) NOT NULL,
 occurred_at TIMESTAMP WITH TIME ZONE NOT NULL, read_at TIMESTAMP WITH TIME ZONE,
 correlation_id VARCHAR(100), record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_communication_application FOREIGN KEY(application_id) REFERENCES "${primehrSchema}".rsp_position_application(id),
 CONSTRAINT fk_rsp_communication_applicant FOREIGN KEY(applicant_id) REFERENCES "${primehrSchema}".rsp_applicant_account(id),
 CONSTRAINT ck_rsp_communication_direction CHECK(direction IN ('SYSTEM_TO_APPLICANT','STAFF_TO_APPLICANT')),
 CONSTRAINT ck_rsp_communication_channel CHECK(channel='PORTAL')
);
CREATE INDEX ix_rsp_communication_application ON "${primehrSchema}".rsp_applicant_communication(agency_id,application_id,occurred_at);
CREATE INDEX ix_rsp_communication_applicant ON "${primehrSchema}".rsp_applicant_communication(agency_id,applicant_id,occurred_at);
