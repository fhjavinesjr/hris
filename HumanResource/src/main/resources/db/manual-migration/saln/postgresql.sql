BEGIN;
CREATE TABLE IF NOT EXISTS saln (
 saln_id BIGSERIAL PRIMARY KEY, employee_id BIGINT NOT NULL, employee_no VARCHAR(100) NOT NULL,
 filing_type VARCHAR(20) NOT NULL, saln_year INTEGER NOT NULL, reference_date DATE NOT NULL,
 as_of_date DATE NOT NULL, due_date DATE NOT NULL, submission_date DATE, status VARCHAR(24) NOT NULL,
 source_type VARCHAR(24) NOT NULL, version_no INTEGER NOT NULL DEFAULT 0,
 declarant_family_name VARCHAR(150) NOT NULL, declarant_first_name VARCHAR(150) NOT NULL,
 declarant_middle_initial VARCHAR(20), declarant_position VARCHAR(250), declarant_agency_office VARCHAR(300),
 declarant_office_address VARCHAR(500), spouse_full_name VARCHAR(350), spouse_position VARCHAR(250),
 spouse_agency_office VARCHAR(300), spouse_office_address VARCHAR(500), filing_mode VARCHAR(24) NOT NULL,
 multiple_spouses VARCHAR(1000), business_interests_none BOOLEAN NOT NULL DEFAULT FALSE,
 relatives_in_government_none BOOLEAN NOT NULL DEFAULT FALSE, certification_accepted BOOLEAN NOT NULL DEFAULT FALSE,
 government_id_type VARCHAR(100), government_id_no VARCHAR(150), government_id_date_issued DATE,
 created_at TIMESTAMP NOT NULL, created_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP NOT NULL,
 updated_by VARCHAR(100) NOT NULL, submitted_at TIMESTAMP, submitted_by VARCHAR(100), reviewed_at TIMESTAMP,
 reviewed_by VARCHAR(100), compliant_at TIMESTAMP, compliant_by VARCHAR(100), locked_at TIMESTAMP,
 locked_by VARCHAR(100), voided_at TIMESTAMP, voided_by VARCHAR(100), void_reason VARCHAR(1000),
 replacement_saln_id BIGINT, source_document_reference VARCHAR(500), remarks VARCHAR(2000),
 repository_agency VARCHAR(300), repository_submitted_at TIMESTAMP, repository_submitted_by VARCHAR(100),
 repository_reference_no VARCHAR(200), repository_remarks VARCHAR(1000),
 CONSTRAINT ck_saln_status CHECK(status IN ('DRAFT','SUBMITTED','UNDER_REVIEW','FOR_CORRECTION','RESUBMITTED','COMPLIANT','LOCKED','VOIDED')),
 CONSTRAINT ck_saln_type CHECK(filing_type IN ('ASSUMPTION','ANNUAL','SEPARATION'))
);
CREATE INDEX IF NOT EXISTS ix_saln_employee_year ON saln(employee_id,saln_year);
CREATE INDEX IF NOT EXISTS ix_saln_status_due ON saln(status,due_date);
CREATE UNIQUE INDEX IF NOT EXISTS uq_saln_active_filing ON saln(employee_id,filing_type,saln_year,as_of_date) WHERE status <> 'VOIDED';

CREATE TABLE IF NOT EXISTS saln_dependent (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INTEGER NOT NULL,dependent_name VARCHAR(350) NOT NULL,relationship VARCHAR(50) NOT NULL,age INTEGER,PRIMARY KEY(saln_id,row_no));
CREATE TABLE IF NOT EXISTS saln_real_property (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INTEGER NOT NULL,owner_type VARCHAR(20) NOT NULL,owner_name VARCHAR(350),description VARCHAR(500) NOT NULL,property_kind VARCHAR(150) NOT NULL,exact_location VARCHAR(500) NOT NULL,assessed_value NUMERIC(19,2),fair_market_value NUMERIC(19,2),acquisition_year INTEGER,acquisition_mode VARCHAR(150),acquisition_cost NUMERIC(19,2) NOT NULL,PRIMARY KEY(saln_id,row_no));
CREATE TABLE IF NOT EXISTS saln_personal_property (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INTEGER NOT NULL,owner_type VARCHAR(20) NOT NULL,owner_name VARCHAR(350),description VARCHAR(500) NOT NULL,acquisition_year INTEGER,acquisition_cost NUMERIC(19,2) NOT NULL,PRIMARY KEY(saln_id,row_no));
CREATE TABLE IF NOT EXISTS saln_liability (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INTEGER NOT NULL,owner_type VARCHAR(20) NOT NULL,owner_name VARCHAR(350),nature VARCHAR(500) NOT NULL,creditor_name VARCHAR(350) NOT NULL,outstanding_balance NUMERIC(19,2) NOT NULL,PRIMARY KEY(saln_id,row_no));
CREATE TABLE IF NOT EXISTS saln_business_interest (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INTEGER NOT NULL,owner_type VARCHAR(20) NOT NULL,owner_name VARCHAR(350),entity_name VARCHAR(500) NOT NULL,business_address VARCHAR(500) NOT NULL,nature VARCHAR(500) NOT NULL,date_acquired DATE,PRIMARY KEY(saln_id,row_no));
CREATE TABLE IF NOT EXISTS saln_government_relative (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INTEGER NOT NULL,relative_name VARCHAR(350) NOT NULL,relationship VARCHAR(150) NOT NULL,position_title VARCHAR(250) NOT NULL,agency_office_address VARCHAR(600) NOT NULL,PRIMARY KEY(saln_id,row_no));
CREATE TABLE IF NOT EXISTS saln_version (saln_version_id BIGSERIAL PRIMARY KEY,saln_id BIGINT NOT NULL REFERENCES saln(saln_id),version_no INTEGER NOT NULL,status VARCHAR(24) NOT NULL,snapshot_json TEXT NOT NULL,submitted_at TIMESTAMP NOT NULL,submitted_by VARCHAR(100) NOT NULL,CONSTRAINT uq_saln_version UNIQUE(saln_id,version_no));
CREATE TABLE IF NOT EXISTS saln_audit (audit_id BIGSERIAL PRIMARY KEY,saln_id BIGINT NOT NULL REFERENCES saln(saln_id),employee_id BIGINT NOT NULL,action VARCHAR(80) NOT NULL,performed_by VARCHAR(100) NOT NULL,performed_at TIMESTAMP NOT NULL,old_status VARCHAR(24),new_status VARCHAR(24),remarks VARCHAR(2000),version_no INTEGER NOT NULL);
CREATE INDEX IF NOT EXISTS ix_saln_audit_saln_time ON saln_audit(saln_id,performed_at);
CREATE TABLE IF NOT EXISTS saln_correction (correction_id BIGSERIAL PRIMARY KEY,saln_id BIGINT NOT NULL REFERENCES saln(saln_id),version_no INTEGER NOT NULL,section_name VARCHAR(150),field_name VARCHAR(150),message VARCHAR(2000) NOT NULL,requested_by VARCHAR(100) NOT NULL,requested_at TIMESTAMP NOT NULL,resolved BOOLEAN NOT NULL DEFAULT FALSE,resolved_at TIMESTAMP);
CREATE INDEX IF NOT EXISTS ix_saln_correction_saln ON saln_correction(saln_id,resolved);
CREATE TABLE IF NOT EXISTS saln_filing_requirement (requirement_id BIGSERIAL PRIMARY KEY,employee_id BIGINT NOT NULL,employee_no VARCHAR(100) NOT NULL,filing_type VARCHAR(20) NOT NULL,reference_date DATE NOT NULL,saln_year INTEGER NOT NULL,due_date DATE NOT NULL,status VARCHAR(20) NOT NULL,generated_by VARCHAR(100) NOT NULL,generated_at TIMESTAMP NOT NULL,linked_saln_id BIGINT REFERENCES saln(saln_id),CONSTRAINT uq_saln_requirement UNIQUE(employee_id,filing_type,reference_date));
CREATE INDEX IF NOT EXISTS ix_saln_requirement_due ON saln_filing_requirement(status,due_date);
COMMIT;
