package com.primehr.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationV57MigrationParityTest {
    @Test
    void pairedMigrationsHaveMatchingNotificationStructuresAndNoDestructiveStatements() throws Exception {
        String postgres = read("db/migration/postgresql/V57__notification_foundation.sql").toLowerCase();
        String sqlServer = read("db/migration/sqlserver/V57__notification_foundation.sql").toLowerCase();
        for (String migration : Set.of(postgres, sqlServer)) {
            assertThat(migration).contains("primehr_notification", "uk_notification_recipient_event",
                    "ix_notification_recipient_feed", "ix_notification_recipient_unread",
                    "ix_notification_source", "ix_notification_retention", "recipient_key",
                    "source_record_version", "retention_tag");
            assertThat(migration).doesNotContain("delete from", "drop table", "employeeappointment",
                    "payroll_transaction", "http://", "https://");
        }
    }

    private String read(String path) throws IOException {
        try (var input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input == null) throw new IOException(path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
