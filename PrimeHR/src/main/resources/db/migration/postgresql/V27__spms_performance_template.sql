CREATE TABLE "${primehrSchema}".spms_template (
 id VARCHAR(36) PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL,
 code VARCHAR(80) NOT NULL,
 normalized_code VARCHAR(80) NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_spms_template_code UNIQUE(agency_id,normalized_code)
);
CREATE TABLE "${primehrSchema}".spms_template_version (
 id VARCHAR(36) PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL,
 template_id VARCHAR(36) NOT NULL,
 policy_version_id VARCHAR(36) NOT NULL,
 rating_scale_version_id VARCHAR(36) NOT NULL,
 definition_version INTEGER NOT NULL,
 supersedes_id VARCHAR(36),
 title VARCHAR(200) NOT NULL,
 description VARCHAR(2000),
 form_type VARCHAR(20) NOT NULL,
 form_label VARCHAR(200) NOT NULL,
 legal_basis VARCHAR(1000) NOT NULL,
 coverage_scope VARCHAR(30) NOT NULL,
 aggregation VARCHAR(30) NOT NULL,
 missing_value_policy VARCHAR(20) NOT NULL,
 status VARCHAR(20) NOT NULL,
 structure_revision INTEGER NOT NULL DEFAULT 0,
 effective_from DATE, effective_to DATE,
 published_by VARCHAR(100), published_at TIMESTAMP WITH TIME ZONE,
 retired_by VARCHAR(100), retired_at TIMESTAMP WITH TIME ZONE, retirement_reason VARCHAR(1000),
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_template_root FOREIGN KEY(template_id) REFERENCES "${primehrSchema}".spms_template(id),
 CONSTRAINT fk_spms_template_policy FOREIGN KEY(policy_version_id) REFERENCES "${primehrSchema}".spms_policy_version(id),
 CONSTRAINT fk_spms_template_scale FOREIGN KEY(rating_scale_version_id) REFERENCES "${primehrSchema}".spms_rating_scale_version(id),
 CONSTRAINT fk_spms_template_prior FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".spms_template_version(id),
 CONSTRAINT uk_spms_template_version UNIQUE(agency_id,template_id,definition_version),
 CONSTRAINT ck_spms_template_form CHECK(form_type IN ('OPCR','DPCR','IPCR','CUSTOM')),
 CONSTRAINT ck_spms_template_scope CHECK(coverage_scope='AGENCY_WIDE'),
 CONSTRAINT ck_spms_template_aggregation CHECK(aggregation='WEIGHTED_AVERAGE'),
 CONSTRAINT ck_spms_template_missing CHECK(missing_value_policy='ERROR'),
 CONSTRAINT ck_spms_template_status CHECK(status IN ('DRAFT','PUBLISHED','RETIRED')),
 CONSTRAINT ck_spms_template_effective CHECK(effective_to IS NULL OR effective_from IS NULL OR effective_to>=effective_from)
);
CREATE TABLE "${primehrSchema}".spms_template_section (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, template_version_id VARCHAR(36) NOT NULL,
 section_type VARCHAR(20) NOT NULL, section_code VARCHAR(80) NOT NULL, title VARCHAR(200) NOT NULL,
 description VARCHAR(2000), weight_percent NUMERIC(7,4) NOT NULL,
 display_order INTEGER NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_template_section FOREIGN KEY(template_version_id) REFERENCES "${primehrSchema}".spms_template_version(id),
 CONSTRAINT uk_spms_template_section_code UNIQUE(agency_id,template_version_id,section_code),
 CONSTRAINT uk_spms_template_section_order UNIQUE(agency_id,template_version_id,display_order),
 CONSTRAINT ck_spms_template_section_type CHECK(section_type IN ('CORE','STRATEGIC','SUPPORT','OTHER')),
 CONSTRAINT ck_spms_template_section_weight CHECK(weight_percent>0)
);
CREATE TABLE "${primehrSchema}".spms_template_item (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, section_id VARCHAR(36) NOT NULL,
 indicator_version_id VARCHAR(36) NOT NULL, label_override VARCHAR(200), weight_percent NUMERIC(7,4) NOT NULL,
 required_item BOOLEAN NOT NULL, evidence_override VARCHAR(2000), display_order INTEGER NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_template_item_section FOREIGN KEY(section_id) REFERENCES "${primehrSchema}".spms_template_section(id),
 CONSTRAINT fk_spms_template_item_indicator FOREIGN KEY(indicator_version_id) REFERENCES "${primehrSchema}".spms_success_indicator_version(id),
 CONSTRAINT uk_spms_template_item_indicator UNIQUE(agency_id,section_id,indicator_version_id),
 CONSTRAINT uk_spms_template_item_order UNIQUE(agency_id,section_id,display_order),
 CONSTRAINT ck_spms_template_item_weight CHECK(weight_percent>0)
);
CREATE INDEX ix_spms_template_status ON "${primehrSchema}".spms_template_version(agency_id,status,form_type);
CREATE INDEX ix_spms_template_section ON "${primehrSchema}".spms_template_section(agency_id,template_version_id,display_order);
CREATE INDEX ix_spms_template_item ON "${primehrSchema}".spms_template_item(agency_id,section_id,display_order);
