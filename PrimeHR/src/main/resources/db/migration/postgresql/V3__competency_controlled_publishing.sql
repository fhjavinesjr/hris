ALTER TABLE "${primehrSchema}".prime_competency_category
    ADD COLUMN published_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE "${primehrSchema}".prime_competency_category
    ADD COLUMN published_by VARCHAR(100);

ALTER TABLE "${primehrSchema}".prime_proficiency_scale
    ADD COLUMN published_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE "${primehrSchema}".prime_proficiency_scale
    ADD COLUMN published_by VARCHAR(100);

ALTER TABLE "${primehrSchema}".prime_competency
    ADD COLUMN published_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE "${primehrSchema}".prime_competency
    ADD COLUMN published_by VARCHAR(100);

CREATE INDEX ix_prime_category_publication_chain
    ON "${primehrSchema}".prime_competency_category
    (agency_id, code, status, definition_version, effective_from, effective_to);
CREATE INDEX ix_prime_scale_publication_chain
    ON "${primehrSchema}".prime_proficiency_scale
    (agency_id, code, status, definition_version, effective_from, effective_to);
CREATE INDEX ix_prime_competency_publication_chain
    ON "${primehrSchema}".prime_competency
    (agency_id, code, status, definition_version, effective_from, effective_to);
