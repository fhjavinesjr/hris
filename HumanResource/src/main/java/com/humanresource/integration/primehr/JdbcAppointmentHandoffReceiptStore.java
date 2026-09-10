package com.humanresource.integration.primehr;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
class JdbcAppointmentHandoffReceiptStore implements AppointmentHandoffReceiptStore {
    private static final String COLUMNS = "id, agency_id, handoff_id, schema_version, source_fingerprint, "
            + "selection_id, application_id, applicant_id, state, payload_snapshot, received_at, "
            + "correlation_id, source_actor, record_version, updated_at";

    private final JdbcTemplate jdbc;

    JdbcAppointmentHandoffReceiptStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<AppointmentHandoffReceiptRecord> findByHandoff(String agencyId, String handoffId) {
        return one("SELECT " + COLUMNS + " FROM rsp_appointment_handoff_receipt WHERE agency_id=? AND handoff_id=?",
                agencyId, handoffId);
    }

    @Override
    public Optional<AppointmentHandoffReceiptRecord> findBySelection(String agencyId, String selectionId) {
        return one("SELECT " + COLUMNS + " FROM rsp_appointment_handoff_receipt WHERE agency_id=? AND selection_id=?",
                agencyId, selectionId);
    }

    @Override
    public AppointmentHandoffReceiptRecord insert(AppointmentHandoffReceiptRecord value) {
        jdbc.update("INSERT INTO rsp_appointment_handoff_receipt (" + COLUMNS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                value.id(), value.agencyId(), value.handoffId(), value.schemaVersion(), value.sourceFingerprint(),
                value.selectionId(), value.applicationId(), value.applicantId(), value.state(), value.payloadSnapshot(),
                Timestamp.from(value.receivedAt()), value.correlationId(), value.sourceActor(), value.recordVersion(),
                Timestamp.from(value.updatedAt()));
        return value;
    }

    private Optional<AppointmentHandoffReceiptRecord> one(String sql, Object... arguments) {
        List<AppointmentHandoffReceiptRecord> rows = jdbc.query(sql, this::map, arguments);
        return rows.stream().findFirst();
    }

    private AppointmentHandoffReceiptRecord map(ResultSet rs, int rowNumber) throws SQLException {
        return new AppointmentHandoffReceiptRecord(
                rs.getString("id"), rs.getString("agency_id"), rs.getString("handoff_id"),
                rs.getInt("schema_version"), rs.getString("source_fingerprint"), rs.getString("selection_id"),
                rs.getString("application_id"), rs.getString("applicant_id"), rs.getString("state"),
                rs.getString("payload_snapshot"), instant(rs, "received_at"), rs.getString("correlation_id"),
                rs.getString("source_actor"), rs.getLong("record_version"), instant(rs, "updated_at"));
    }

    private Instant instant(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column).toInstant();
    }
}
