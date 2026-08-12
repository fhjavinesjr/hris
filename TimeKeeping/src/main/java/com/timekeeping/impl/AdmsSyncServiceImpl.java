package com.timekeeping.impl;

import com.timekeeping.dtos.AdmsSyncResultDTO;
import com.timekeeping.entitymodels.AdmsPunchLog;
import com.timekeeping.repositories.AdmsPunchLogRepository;
import com.timekeeping.services.AdmsSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class AdmsSyncServiceImpl implements AdmsSyncService {

    private static final Logger log = LoggerFactory.getLogger(AdmsSyncServiceImpl.class);

    private static final String STATUS_IMPORTED = "IMPORTED";
    private static final String STATUS_UNMAPPED = "UNMAPPED";
    private static final String STATUS_INVALID = "INVALID";
    private static final Pattern SAFE_DATABASE_NAME = Pattern.compile("[A-Za-z0-9_]+");
    private static final Pattern NUMERIC_VALUE = Pattern.compile("[0-9]+");

    private final JdbcTemplate jdbcTemplate;
    private final AdmsPunchLogRepository punchLogRepository;

    @Value("${adms.sync.enabled:false}")
    private boolean syncEnabled;

    @Value("${adms.sync.database-name:adms_db}")
    private String admsDatabaseName;

    @Value("${adms.sync.batch-size:1000}")
    private int configuredBatchSize;

    @Value("${adms.sync.max-records-per-run:10000}")
    private int configuredMaxRecordsPerRun;

    public AdmsSyncServiceImpl(
            JdbcTemplate jdbcTemplate,
            AdmsPunchLogRepository punchLogRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.punchLogRepository = punchLogRepository;
    }

    /**
     * Copies new checkinout rows from adms_db into hrisof and maps
     * userinfo.badgenumber to employee.biometricNo. This background operation
     * stages punches only; DTR reconciliation is triggered by Search.
     */
    @Override
    @Transactional
    public synchronized AdmsSyncResultDTO syncNewPunches() {
        AdmsSyncResultDTO result = new AdmsSyncResultDTO();
        result.setEnabled(syncEnabled);

        if (!syncEnabled) {
            result.setMessage("ADMS synchronization is disabled for this deployment.");
            return result;
        }

        String databaseName = validateDatabaseName(admsDatabaseName);
        int batchSize = clamp(configuredBatchSize, 1, 5000);
        int maxRecords = clamp(configuredMaxRecordsPerRun, batchSize, 100000);

        EmployeeBiometricIndex employeeIndex = loadEmployeeBiometricIndex();

        int totalRead = 0;
        int imported = 0;
        int unmapped = 0;
        int invalid = 0;
        int duplicate = 0;

        while (totalRead < maxRecords) {
            int remaining = maxRecords - totalRead;
            int currentBatchSize = Math.min(batchSize, remaining);
            List<AdmsCheckinoutRow> rows = loadNewCheckinoutRows(databaseName, currentBatchSize);

            if (rows.isEmpty()) {
                break;
            }

            List<AdmsPunchLog> entities = new ArrayList<>();

            for (AdmsCheckinoutRow row : rows) {
                totalRead++;

                if (punchLogRepository.existsByAdmsCheckoutId(row.checkinoutId())) {
                    duplicate++;
                    continue;
                }

                AdmsPunchLog entity = new AdmsPunchLog();
                entity.setAdmsCheckoutId(row.checkinoutId());
                entity.setAdmsUserId(trimToNull(row.userId()));
                entity.setAdmsBadgeNumber(trimToNull(row.badgeNumber()));
                entity.setCheckTime(row.checkTime());
                entity.setCheckType(row.checkType());
                entity.setDeviceSerialNo(trimToNull(row.deviceSerialNo()));
                entity.setImportedAt(LocalDateTime.now());
                entity.setDtrProcessed(false);

                String validationError = validateCheckinoutRow(row);
                if (validationError != null) {
                    entity.setImportStatus(STATUS_INVALID);
                    entity.setImportMessage(validationError);
                    invalid++;
                    entities.add(entity);
                    continue;
                }

                EmployeeMatch employeeMatch = employeeIndex.resolve(row.badgeNumber());
                if (employeeMatch.employeeId() == null) {
                    entity.setImportStatus(STATUS_UNMAPPED);
                    entity.setImportMessage(employeeMatch.message());
                    unmapped++;
                } else {
                    entity.setEmployeeId(employeeMatch.employeeId());
                    entity.setImportStatus(STATUS_IMPORTED);
                    entity.setImportMessage("Matched ADMS userinfo.badgenumber to employee biometricNo.");
                    imported++;
                }

                entities.add(entity);
            }

            if (!entities.isEmpty()) {
                punchLogRepository.saveAll(entities);
                // The next SELECT uses NOT EXISTS against this table, so flush
                // the current batch before asking SQL Server for another batch.
                punchLogRepository.flush();
            }

            if (rows.size() < currentBatchSize) {
                break;
            }
        }

        result.setRecordsRead(totalRead);
        result.setImported(imported);
        result.setUnmapped(unmapped);
        result.setInvalid(invalid);
        result.setDuplicatesSkipped(duplicate);

        // Background synchronization is staging-only:
        // adms_db.dbo.checkinout -> hrisof.dbo.adms_punch_log.
        // DTR creation/rebuilding is intentionally performed only by the
        // employee/date-targeted Search reconciliation endpoint.
        result.setDtrEmployeesReviewed(0);
        result.setDtrSegmentsCreated(0);
        result.setDtrPunchesProcessed(0);
        result.setDtrPendingPunches((int) punchLogRepository
                .countByImportStatusAndDtrProcessedFalse(STATUS_IMPORTED));
        result.setDtrConflicts(0);
        result.setDtrDuplicates(0);

        if (totalRead == 0) {
            result.setMessage("No new ADMS checkinout records were found.");
        } else {
            result.setMessage("ADMS punches were imported into the HRIS staging log. DTR reconciliation runs when Search is clicked.");
        }

        log.info(
                "ADMS staging sync completed recordsRead={} imported={} unmapped={} invalid={} duplicate={} pendingDtrPunches={}",
                totalRead,
                imported,
                unmapped,
                invalid,
                duplicate,
                result.getDtrPendingPunches());

        return result;
    }

    @Override
    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", syncEnabled);
        status.put("databaseName", admsDatabaseName);

        if (!syncEnabled) {
            status.put("totalStaged", 0);
            status.put("imported", 0);
            status.put("unmapped", 0);
            status.put("invalid", 0);
            status.put("lastImportedAt", null);
            return status;
        }

        status.put("totalStaged", punchLogRepository.count());
        status.put("imported", punchLogRepository.countByImportStatus(STATUS_IMPORTED));
        status.put("unmapped", punchLogRepository.countByImportStatus(STATUS_UNMAPPED));
        status.put("invalid", punchLogRepository.countByImportStatus(STATUS_INVALID));
        status.put("pendingDtrPunches", punchLogRepository.countByImportStatusAndDtrProcessedFalse(STATUS_IMPORTED));
        status.put(
                "lastImportedAt",
                punchLogRepository.findTopByOrderByImportedAtDesc()
                        .map(AdmsPunchLog::getImportedAt)
                        .orElse(null)
        );
        return status;
    }

    @Override
    public List<AdmsPunchLog> getUnmappedPunches() {
        if (!syncEnabled) {
            return Collections.emptyList();
        }
        return punchLogRepository.findTop100ByImportStatusOrderByCheckTimeDesc(STATUS_UNMAPPED);
    }

    private List<AdmsCheckinoutRow> loadNewCheckinoutRows(String databaseName, int batchSize) {
        String sql = "SELECT TOP " + batchSize + " " +
                "c.id, c.userid, ui.badgenumber, c.checktime, c.checktype, c.SN " +
                "FROM [" + databaseName + "].dbo.checkinout c " +
                "LEFT JOIN [" + databaseName + "].dbo.userinfo ui ON ui.userid = c.userid " +
                "WHERE NOT EXISTS (" +
                "    SELECT 1 FROM adms_punch_log imported " +
                "    WHERE imported.adms_checkout_id = c.id" +
                ") " +
                "ORDER BY c.id ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp timestamp = rs.getTimestamp("checktime");
            return new AdmsCheckinoutRow(
                    rs.getLong("id"),
                    rs.getString("userid"),
                    rs.getString("badgenumber"),
                    timestamp == null ? null : timestamp.toLocalDateTime(),
                    parseCheckType(rs.getObject("checktype")),
                    rs.getString("SN")
            );
        });
    }

    private EmployeeBiometricIndex loadEmployeeBiometricIndex() {
        String sql = "SELECT CAST(employeeId AS VARCHAR(100)) AS employeeId, biometricNo " +
                "FROM employee " +
                "WHERE biometricNo IS NOT NULL AND LTRIM(RTRIM(biometricNo)) <> ''";

        List<EmployeeBiometricRow> rows = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new EmployeeBiometricRow(
                        rs.getString("employeeId"),
                        rs.getString("biometricNo")
                )
        );

        return EmployeeBiometricIndex.from(rows);
    }

    private String validateCheckinoutRow(AdmsCheckinoutRow row) {
        if (row.checkinoutId() == null || row.checkinoutId() <= 0) {
            return "Missing or invalid ADMS checkinout ID.";
        }
        if (trimToNull(row.userId()) == null) {
            return "ADMS checkinout.userid is empty.";
        }
        if (trimToNull(row.badgeNumber()) == null) {
            return "No ADMS userinfo.badgenumber was found for checkinout.userid " + row.userId() + ".";
        }
        if (row.checkTime() == null) {
            return "ADMS checktime is empty.";
        }
        if (row.checkType() == null || (row.checkType() != 0 && row.checkType() != 1)) {
            return "Unsupported ADMS checktype. Expected 0 (Check In) or 1 (Check Out).";
        }
        return null;
    }

    private Integer parseCheckType(Object value) {
        if (value == null) return null;
        try {
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String validateDatabaseName(String value) {
        String candidate = trimToNull(value);
        if (candidate == null || !SAFE_DATABASE_NAME.matcher(candidate).matches()) {
            throw new IllegalStateException("Invalid adms.sync.database-name configuration.");
        }
        return candidate;
    }

    private int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeBiometricNumber(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) return null;

        if (NUMERIC_VALUE.matcher(trimmed).matches()) {
            // "001", "0001", and "1" all resolve to the same device user ID.
            return new BigInteger(trimmed).toString();
        }

        return trimmed.toLowerCase(Locale.ROOT);
    }

    private record AdmsCheckinoutRow(
            Long checkinoutId,
            String userId,
            String badgeNumber,
            LocalDateTime checkTime,
            Integer checkType,
            String deviceSerialNo) {
    }

    private record EmployeeBiometricRow(String employeeId, String biometricNo) {
    }

    private record EmployeeMatch(String employeeId, String message) {
    }

    private static final class EmployeeBiometricIndex {
        private final Map<String, String> exactMatches;
        private final Map<String, String> normalizedMatches;
        private final Set<String> ambiguousNormalizedValues;

        private EmployeeBiometricIndex(
                Map<String, String> exactMatches,
                Map<String, String> normalizedMatches,
                Set<String> ambiguousNormalizedValues) {
            this.exactMatches = exactMatches;
            this.normalizedMatches = normalizedMatches;
            this.ambiguousNormalizedValues = ambiguousNormalizedValues;
        }

        static EmployeeBiometricIndex from(List<EmployeeBiometricRow> rows) {
            Map<String, String> exact = new HashMap<>();
            Map<String, String> normalized = new HashMap<>();
            Set<String> ambiguous = new HashSet<>();

            for (EmployeeBiometricRow row : rows) {
                String biometricNo = trimToNull(row.biometricNo());
                String employeeId = trimToNull(row.employeeId());
                if (biometricNo == null || employeeId == null) continue;

                exact.putIfAbsent(biometricNo, employeeId);

                String normalizedValue = normalizeBiometricNumber(biometricNo);
                if (normalizedValue == null) continue;

                String previous = normalized.putIfAbsent(normalizedValue, employeeId);
                if (previous != null && !previous.equals(employeeId)) {
                    ambiguous.add(normalizedValue);
                }
            }

            return new EmployeeBiometricIndex(exact, normalized, ambiguous);
        }

        EmployeeMatch resolve(String admsBadgeNumber) {
            String raw = trimToNull(admsBadgeNumber);
            if (raw == null) {
                return new EmployeeMatch(null, "ADMS badgenumber is empty.");
            }

            String exactEmployeeId = exactMatches.get(raw);
            if (exactEmployeeId != null) {
                return new EmployeeMatch(exactEmployeeId, "Exact biometricNo match.");
            }

            String normalizedValue = normalizeBiometricNumber(raw);
            if (normalizedValue == null) {
                return new EmployeeMatch(null, "Unable to normalize ADMS badgenumber.");
            }

            if (ambiguousNormalizedValues.contains(normalizedValue)) {
                return new EmployeeMatch(
                        null,
                        "More than one employee has the same normalized biometric number."
                );
            }

            String normalizedEmployeeId = normalizedMatches.get(normalizedValue);
            if (normalizedEmployeeId == null) {
                return new EmployeeMatch(
                        null,
                        "No HRIS employee matched biometricNo " + raw + "."
                );
            }

            return new EmployeeMatch(normalizedEmployeeId, "Normalized biometricNo match.");
        }
    }
}
