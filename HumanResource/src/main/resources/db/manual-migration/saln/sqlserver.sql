BEGIN TRANSACTION;
IF OBJECT_ID('saln','U') IS NULL CREATE TABLE saln (
 saln_id BIGINT IDENTITY(1,1) PRIMARY KEY, employee_id BIGINT NOT NULL, employee_no VARCHAR(100) NOT NULL,
 filing_type VARCHAR(20) NOT NULL, saln_year INT NOT NULL, reference_date DATE NOT NULL, as_of_date DATE NOT NULL,
 due_date DATE NOT NULL, submission_date DATE NULL, status VARCHAR(24) NOT NULL, source_type VARCHAR(24) NOT NULL,
 version_no INT NOT NULL DEFAULT 0, declarant_family_name VARCHAR(150) NOT NULL, declarant_first_name VARCHAR(150) NOT NULL,
 declarant_middle_initial VARCHAR(20) NULL, declarant_position VARCHAR(250) NULL, declarant_agency_office VARCHAR(300) NULL,
 declarant_office_address VARCHAR(500) NULL, spouse_full_name VARCHAR(350) NULL, spouse_position VARCHAR(250) NULL,
 spouse_agency_office VARCHAR(300) NULL, spouse_office_address VARCHAR(500) NULL, filing_mode VARCHAR(24) NOT NULL,
 multiple_spouses VARCHAR(1000) NULL, business_interests_none BIT NOT NULL DEFAULT 0,
 relatives_in_government_none BIT NOT NULL DEFAULT 0, certification_accepted BIT NOT NULL DEFAULT 0,
 government_id_type VARCHAR(100) NULL, government_id_no VARCHAR(150) NULL, government_id_date_issued DATE NULL,
 created_at DATETIME2 NOT NULL, created_by VARCHAR(100) NOT NULL, updated_at DATETIME2 NOT NULL, updated_by VARCHAR(100) NOT NULL,
 submitted_at DATETIME2 NULL, submitted_by VARCHAR(100) NULL, reviewed_at DATETIME2 NULL, reviewed_by VARCHAR(100) NULL,
 compliant_at DATETIME2 NULL, compliant_by VARCHAR(100) NULL, locked_at DATETIME2 NULL, locked_by VARCHAR(100) NULL,
 voided_at DATETIME2 NULL, voided_by VARCHAR(100) NULL, void_reason VARCHAR(1000) NULL, replacement_saln_id BIGINT NULL,
 source_document_reference VARCHAR(500) NULL, remarks VARCHAR(2000) NULL, repository_agency VARCHAR(300) NULL,
 repository_submitted_at DATETIME2 NULL, repository_submitted_by VARCHAR(100) NULL, repository_reference_no VARCHAR(200) NULL,
 repository_remarks VARCHAR(1000) NULL,
 CONSTRAINT ck_saln_status CHECK(status IN ('DRAFT','SUBMITTED','UNDER_REVIEW','FOR_CORRECTION','RESUBMITTED','COMPLIANT','LOCKED','VOIDED')),
 CONSTRAINT ck_saln_type CHECK(filing_type IN ('ASSUMPTION','ANNUAL','SEPARATION'))
);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='ix_saln_employee_year') CREATE INDEX ix_saln_employee_year ON saln(employee_id,saln_year);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='ix_saln_status_due') CREATE INDEX ix_saln_status_due ON saln(status,due_date);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='uq_saln_active_filing') CREATE UNIQUE INDEX uq_saln_active_filing ON saln(employee_id,filing_type,saln_year,as_of_date) WHERE status <> 'VOIDED';
IF OBJECT_ID('saln_dependent','U') IS NULL CREATE TABLE saln_dependent (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INT NOT NULL,dependent_name VARCHAR(350) NOT NULL,relationship VARCHAR(50) NOT NULL,age INT NULL,PRIMARY KEY(saln_id,row_no));
IF OBJECT_ID('saln_real_property','U') IS NULL CREATE TABLE saln_real_property (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INT NOT NULL,owner_type VARCHAR(20) NOT NULL,owner_name VARCHAR(350) NULL,description VARCHAR(500) NOT NULL,property_kind VARCHAR(150) NOT NULL,exact_location VARCHAR(500) NOT NULL,assessed_value DECIMAL(19,2) NULL,fair_market_value DECIMAL(19,2) NULL,acquisition_year INT NULL,acquisition_mode VARCHAR(150) NULL,acquisition_cost DECIMAL(19,2) NOT NULL,PRIMARY KEY(saln_id,row_no));
IF OBJECT_ID('saln_personal_property','U') IS NULL CREATE TABLE saln_personal_property (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INT NOT NULL,owner_type VARCHAR(20) NOT NULL,owner_name VARCHAR(350) NULL,description VARCHAR(500) NOT NULL,acquisition_year INT NULL,acquisition_cost DECIMAL(19,2) NOT NULL,PRIMARY KEY(saln_id,row_no));
IF OBJECT_ID('saln_liability','U') IS NULL CREATE TABLE saln_liability (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INT NOT NULL,owner_type VARCHAR(20) NOT NULL,owner_name VARCHAR(350) NULL,nature VARCHAR(500) NOT NULL,creditor_name VARCHAR(350) NOT NULL,outstanding_balance DECIMAL(19,2) NOT NULL,PRIMARY KEY(saln_id,row_no));
IF OBJECT_ID('saln_business_interest','U') IS NULL CREATE TABLE saln_business_interest (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INT NOT NULL,owner_type VARCHAR(20) NOT NULL,owner_name VARCHAR(350) NULL,entity_name VARCHAR(500) NOT NULL,business_address VARCHAR(500) NOT NULL,nature VARCHAR(500) NOT NULL,date_acquired DATE NULL,PRIMARY KEY(saln_id,row_no));
IF OBJECT_ID('saln_government_relative','U') IS NULL CREATE TABLE saln_government_relative (saln_id BIGINT NOT NULL REFERENCES saln(saln_id),row_no INT NOT NULL,relative_name VARCHAR(350) NOT NULL,relationship VARCHAR(150) NOT NULL,position_title VARCHAR(250) NOT NULL,agency_office_address VARCHAR(600) NOT NULL,PRIMARY KEY(saln_id,row_no));
IF OBJECT_ID('saln_version','U') IS NULL CREATE TABLE saln_version (saln_version_id BIGINT IDENTITY(1,1) PRIMARY KEY,saln_id BIGINT NOT NULL REFERENCES saln(saln_id),version_no INT NOT NULL,status VARCHAR(24) NOT NULL,snapshot_json NVARCHAR(MAX) NOT NULL,submitted_at DATETIME2 NOT NULL,submitted_by VARCHAR(100) NOT NULL,CONSTRAINT uq_saln_version UNIQUE(saln_id,version_no));
IF OBJECT_ID('saln_audit','U') IS NULL CREATE TABLE saln_audit (audit_id BIGINT IDENTITY(1,1) PRIMARY KEY,saln_id BIGINT NOT NULL REFERENCES saln(saln_id),employee_id BIGINT NOT NULL,action VARCHAR(80) NOT NULL,performed_by VARCHAR(100) NOT NULL,performed_at DATETIME2 NOT NULL,old_status VARCHAR(24) NULL,new_status VARCHAR(24) NULL,remarks VARCHAR(2000) NULL,version_no INT NOT NULL);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='ix_saln_audit_saln_time') CREATE INDEX ix_saln_audit_saln_time ON saln_audit(saln_id,performed_at);
IF OBJECT_ID('saln_correction','U') IS NULL CREATE TABLE saln_correction (correction_id BIGINT IDENTITY(1,1) PRIMARY KEY,saln_id BIGINT NOT NULL REFERENCES saln(saln_id),version_no INT NOT NULL,section_name VARCHAR(150) NULL,field_name VARCHAR(150) NULL,message VARCHAR(2000) NOT NULL,requested_by VARCHAR(100) NOT NULL,requested_at DATETIME2 NOT NULL,resolved BIT NOT NULL DEFAULT 0,resolved_at DATETIME2 NULL);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='ix_saln_correction_saln') CREATE INDEX ix_saln_correction_saln ON saln_correction(saln_id,resolved);
IF OBJECT_ID('saln_filing_requirement','U') IS NULL CREATE TABLE saln_filing_requirement (requirement_id BIGINT IDENTITY(1,1) PRIMARY KEY,employee_id BIGINT NOT NULL,employee_no VARCHAR(100) NOT NULL,filing_type VARCHAR(20) NOT NULL,reference_date DATE NOT NULL,saln_year INT NOT NULL,due_date DATE NOT NULL,status VARCHAR(20) NOT NULL,generated_by VARCHAR(100) NOT NULL,generated_at DATETIME2 NOT NULL,linked_saln_id BIGINT NULL REFERENCES saln(saln_id),CONSTRAINT uq_saln_requirement UNIQUE(employee_id,filing_type,reference_date));
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='ix_saln_requirement_due') CREATE INDEX ix_saln_requirement_due ON saln_filing_requirement(status,due_date);
COMMIT TRANSACTION;
