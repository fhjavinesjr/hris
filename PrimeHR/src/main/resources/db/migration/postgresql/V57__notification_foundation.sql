CREATE TABLE "${primehrSchema}".primehr_notification (
 id VARCHAR(36) PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL,
 recipient_employee_no VARCHAR(100) NOT NULL,
 recipient_key VARCHAR(100) NOT NULL,
 source_domain VARCHAR(40) NOT NULL,
 event_code VARCHAR(40) NOT NULL,
 kind VARCHAR(30) NOT NULL,
 source_aggregate_type VARCHAR(80) NOT NULL,
 source_aggregate_id VARCHAR(100) NOT NULL,
 source_record_version BIGINT NOT NULL,
 event_key VARCHAR(160) NOT NULL,
 route_key VARCHAR(40) NOT NULL,
 title VARCHAR(200) NOT NULL,
 message VARCHAR(500) NOT NULL,
 due_at TIMESTAMP WITH TIME ZONE,
 action_state VARCHAR(20) NOT NULL,
 read_at TIMESTAMP WITH TIME ZONE,
 dismissed_at TIMESTAMP WITH TIME ZONE,
 retention_tag VARCHAR(100) NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_notification_recipient_event UNIQUE (agency_id, recipient_key, event_key),
 CONSTRAINT ck_notification_source_version CHECK (source_record_version >= 0),
 CONSTRAINT ck_notification_kind CHECK (kind IN ('ACTION_REQUIRED','INFORMATIONAL')),
 CONSTRAINT ck_notification_action_state CHECK (action_state IN ('ACTIVE','CLOSED')),
 CONSTRAINT ck_notification_dismissal CHECK (dismissed_at IS NULL OR (kind = 'INFORMATIONAL' AND read_at IS NOT NULL))
);

CREATE INDEX ix_notification_recipient_feed
 ON "${primehrSchema}".primehr_notification(agency_id, recipient_key, created_at);
CREATE INDEX ix_notification_recipient_unread
 ON "${primehrSchema}".primehr_notification(agency_id, recipient_key, read_at, dismissed_at);
CREATE INDEX ix_notification_source
 ON "${primehrSchema}".primehr_notification(agency_id, source_domain, source_aggregate_type, source_aggregate_id);
CREATE INDEX ix_notification_retention
 ON "${primehrSchema}".primehr_notification(agency_id, retention_tag, created_at);
