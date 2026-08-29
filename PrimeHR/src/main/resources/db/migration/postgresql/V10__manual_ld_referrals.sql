CREATE TABLE "${primehrSchema}".prime_ld_referral (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 analysis_id VARCHAR(36) NOT NULL, subject_employee_id BIGINT NOT NULL,
 subject_employee_no VARCHAR(100) NOT NULL, subject_display_name VARCHAR(300) NOT NULL,
 analysis_date DATE NOT NULL, position_name VARCHAR(200) NOT NULL,
 status VARCHAR(30) NOT NULL, development_need VARCHAR(4000) NOT NULL,
 recommended_intervention VARCHAR(4000) NOT NULL, target_completion_date DATE,
 referral_reason VARCHAR(1000), remarks VARCHAR(2000),
 referred_by VARCHAR(100), referred_at TIMESTAMP WITH TIME ZONE,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_prime_ld_referral_analysis FOREIGN KEY(analysis_id)
  REFERENCES "${primehrSchema}".prime_competency_gap_analysis(id),
 CONSTRAINT ck_prime_ld_referral_status CHECK(status IN ('DRAFT','REFERRED','ARCHIVED')),
 CONSTRAINT ck_prime_ld_referral_submission CHECK(
  (status='REFERRED' AND referred_by IS NOT NULL AND referred_at IS NOT NULL)
  OR (status IN ('DRAFT','ARCHIVED') AND referred_by IS NULL AND referred_at IS NULL))
);

CREATE TABLE "${primehrSchema}".prime_ld_referral_item (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 referral_id VARCHAR(36) NOT NULL, analysis_id VARCHAR(36) NOT NULL,
 gap_item_id VARCHAR(36) NOT NULL, active BOOLEAN NOT NULL,
 competency_code VARCHAR(50) NOT NULL, competency_name VARCHAR(200) NOT NULL,
 gap_classification VARCHAR(30) NOT NULL, not_assessed_reason VARCHAR(40),
 gap_value INTEGER, priority_code VARCHAR(50), priority_label VARCHAR(150),
 priority_rank INTEGER, display_order INTEGER NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_prime_ld_referral_item_referral FOREIGN KEY(referral_id)
  REFERENCES "${primehrSchema}".prime_ld_referral(id),
 CONSTRAINT fk_prime_ld_referral_item_analysis FOREIGN KEY(analysis_id)
  REFERENCES "${primehrSchema}".prime_competency_gap_analysis(id),
 CONSTRAINT fk_prime_ld_referral_item_gap FOREIGN KEY(gap_item_id)
  REFERENCES "${primehrSchema}".prime_competency_gap_item(id),
 CONSTRAINT uk_prime_ld_referral_item UNIQUE(referral_id,gap_item_id),
 CONSTRAINT ck_prime_ld_referral_item_class CHECK(gap_classification IN ('BELOW','NOT_ASSESSED')),
 CONSTRAINT ck_prime_ld_referral_item_reason CHECK(not_assessed_reason IS NULL OR not_assessed_reason IN ('NO_RESULT','VERSION_NOT_COMPARABLE')),
 CONSTRAINT ck_prime_ld_referral_item_values CHECK(
  (gap_classification='NOT_ASSESSED' AND gap_value IS NULL AND not_assessed_reason IS NOT NULL)
  OR (gap_classification='BELOW' AND gap_value IS NOT NULL AND gap_value>0 AND not_assessed_reason IS NULL)),
 CONSTRAINT ck_prime_ld_referral_item_order CHECK(display_order>=0)
);

CREATE INDEX ix_prime_ld_referral_employee ON "${primehrSchema}".prime_ld_referral(agency_id,subject_employee_no,status);
CREATE INDEX ix_prime_ld_referral_analysis ON "${primehrSchema}".prime_ld_referral(agency_id,analysis_id,status);
CREATE INDEX ix_prime_ld_referral_item_gap ON "${primehrSchema}".prime_ld_referral_item(agency_id,analysis_id,gap_item_id,active);

