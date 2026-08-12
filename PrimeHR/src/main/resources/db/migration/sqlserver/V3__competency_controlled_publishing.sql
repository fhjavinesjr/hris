ALTER TABLE [${primehrSchema}].prime_competency_category ADD
    published_at DATETIMEOFFSET NULL,
    published_by VARCHAR(100) NULL;

ALTER TABLE [${primehrSchema}].prime_proficiency_scale ADD
    published_at DATETIMEOFFSET NULL,
    published_by VARCHAR(100) NULL;

ALTER TABLE [${primehrSchema}].prime_competency ADD
    published_at DATETIMEOFFSET NULL,
    published_by VARCHAR(100) NULL;

CREATE INDEX ix_prime_category_publication_chain
    ON [${primehrSchema}].prime_competency_category
    (agency_id, code, status, definition_version, effective_from, effective_to);
CREATE INDEX ix_prime_scale_publication_chain
    ON [${primehrSchema}].prime_proficiency_scale
    (agency_id, code, status, definition_version, effective_from, effective_to);
CREATE INDEX ix_prime_competency_publication_chain
    ON [${primehrSchema}].prime_competency
    (agency_id, code, status, definition_version, effective_from, effective_to);
