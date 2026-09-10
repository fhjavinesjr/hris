CREATE TABLE ${primehrSchema}.spms_pmt (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL,
 normalized_code VARCHAR(80) NOT NULL, name VARCHAR(200) NOT NULL, mandate_reference VARCHAR(1000) NOT NULL,
 effective_from DATE NOT NULL, effective_to DATE, status VARCHAR(20) NOT NULL, roster_revision INT NOT NULL DEFAULT 0,
 activated_by VARCHAR(100), activated_at DATETIMEOFFSET, deactivated_by VARCHAR(100), deactivated_at DATETIMEOFFSET,
 transition_reason VARCHAR(1000), record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT uk_spms_pmt_code UNIQUE(agency_id,normalized_code),
 CONSTRAINT ck_spms_pmt_dates CHECK(effective_to IS NULL OR effective_to>=effective_from),
 CONSTRAINT ck_spms_pmt_status CHECK(status IN ('DRAFT','ACTIVE','INACTIVE')),
 CONSTRAINT ck_spms_pmt_active CHECK((status='DRAFT' AND activated_by IS NULL AND activated_at IS NULL) OR (status IN ('ACTIVE','INACTIVE') AND activated_by IS NOT NULL AND activated_at IS NOT NULL)),
 CONSTRAINT ck_spms_pmt_inactive CHECK((status<>'INACTIVE' AND deactivated_by IS NULL AND deactivated_at IS NULL) OR (status='INACTIVE' AND deactivated_by IS NOT NULL AND deactivated_at IS NOT NULL))
);
CREATE INDEX ix_spms_pmt_effective ON ${primehrSchema}.spms_pmt(agency_id,status,effective_from,effective_to);

CREATE TABLE ${primehrSchema}.spms_pmt_member (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, pmt_id VARCHAR(36) NOT NULL,
 employee_id BIGINT NOT NULL, employee_no VARCHAR(100) NOT NULL, employee_name_snapshot VARCHAR(300) NOT NULL,
 job_position_id_snapshot BIGINT, plantilla_id_snapshot BIGINT, member_role VARCHAR(30) NOT NULL,
 voting BIT NOT NULL, effective_from DATE NOT NULL, effective_to DATE,
 designation_reference VARCHAR(500), remarks VARCHAR(1000), record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_pmt_member_team FOREIGN KEY(pmt_id) REFERENCES ${primehrSchema}.spms_pmt(id),
 CONSTRAINT uk_spms_pmt_member_period UNIQUE(agency_id,pmt_id,employee_no,effective_from),
 CONSTRAINT ck_spms_pmt_member_dates CHECK(effective_to IS NULL OR effective_to>=effective_from),
 CONSTRAINT ck_spms_pmt_member_role CHECK(member_role IN ('CHAIRPERSON','VICE_CHAIRPERSON','SECRETARIAT','MEMBER','TECHNICAL_SUPPORT')),
 CONSTRAINT ck_spms_pmt_member_vote CHECK((member_role NOT IN ('SECRETARIAT','TECHNICAL_SUPPORT') OR voting=0) AND (member_role<>'CHAIRPERSON' OR voting=1))
);
CREATE INDEX ix_spms_pmt_member_effective ON ${primehrSchema}.spms_pmt_member(agency_id,pmt_id,effective_from,effective_to);
