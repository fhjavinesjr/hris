package com.payroll.impl;

import com.payroll.dtos.AnnualizationAlphalistEmployeeDTO;
import com.payroll.entitymodels.AnnualizationSnapshotPeriod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Bir1604CDatWriterTest {
    private final Bir1604CDatWriter writer = new Bir1604CDatWriter();

    @Test
    void writesV74Schedule1AndSchedule2WithReconciledControls() throws Exception {
        AnnualizationAlphalistEmployeeDTO schedule1 = record("E001", 1, false);
        AnnualizationAlphalistEmployeeDTO schedule2 = record("E002", 2, true);
        Map<String, Map<String, BigDecimal>> items = Map.of(
                "E001", items(false),
                "E002", items(true));
        AnnualizationSnapshotPeriod mwePeriod = new AnnualizationSnapshotPeriod();
        mwePeriod.setMinimumWageEarner(true);
        mwePeriod.setMinimumWageDailyRate(bd("610"));
        mwePeriod.setMinimumWageMonthlyRate(bd("15905"));

        Bir1604CDatWriter.ExportFile export = writer.write(
                2026, "123-456-789", "0000", List.of(schedule1, schedule2), items,
                Map.of("E001", List.of(), "E002", List.of(mwePeriod)));

        assertThat(export.fileName()).isEqualTo("1234567890000123120261604C.DAT");
        String text = new String(export.content(), StandardCharsets.US_ASCII);
        assertThat(text).endsWith("\r\n");
        String[] lines = text.strip().split("\\r\\n");
        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("H1604C,\"123456789\",\"0000\",12/31/2026");
        assertThat(lines[1]).startsWith("D1,\"1604C\",");
        assertThat(lines[2]).startsWith("C1,\"1604C\",");
        assertThat(lines[3]).startsWith("D2,\"1604C\",");
        assertThat(lines[4]).startsWith("C2,\"1604C\",");
        assertThat(lines[1].split(",", -1)).hasSize(49);
        assertThat(lines[2].split(",", -1)).hasSize(36);
        assertThat(lines[3].split(",", -1)).hasSize(59);
        assertThat(lines[4].split(",", -1)).hasSize(45);
        assertThat(lines[2]).contains("500000.00");
        assertThat(lines[4]).contains("120000.00");

        String validationOutput = System.getProperty("bir.validation.output");
        if (validationOutput != null && !validationOutput.isBlank()) {
            Path output = Path.of(validationOutput).toAbsolutePath().normalize();
            Files.createDirectories(output);
            Files.write(output.resolve(export.fileName()), export.content());
        }
    }

    @Test
    void separatedMweUsesOfficialReasonAndRejectsMissingMapping() {
        AnnualizationSnapshotPeriod mwePeriod = new AnnualizationSnapshotPeriod();
        mwePeriod.setMinimumWageEarner(true);
        mwePeriod.setMinimumWageDailyRate(bd("610"));
        mwePeriod.setMinimumWageMonthlyRate(bd("15905"));
        var separated = record("E002", 2, true, LocalDate.of(2026, 6, 30), "TR");
        String dat = new String(writer.write(2026, "123-456-789", "0000", List.of(separated),
                Map.of("E002", items(true)), Map.of("E002", List.of(mwePeriod))).content(),
                StandardCharsets.US_ASCII);
        assertThat(dat).contains("\"FILIPINO\",\"R\",\"TR\",\"Y\"");
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> writer.write(2026, "123-456-789", "0000",
                        List.of(record("E002", 2, true, LocalDate.of(2026, 6, 30), null)),
                        Map.of("E002", items(true)), Map.of("E002", List.of(mwePeriod))));
    }

    private AnnualizationAlphalistEmployeeDTO record(
            String employeeNo, int schedule, boolean mwe) {
        return record(employeeNo, schedule, mwe, LocalDate.of(2026, 12, 31), null);
    }

    private AnnualizationAlphalistEmployeeDTO record(
            String employeeNo, int schedule, boolean mwe, LocalDate employmentTo, String birReason) {
        return new AnnualizationAlphalistEmployeeDTO(
                1, employeeNo, "JUAN DELA CRUZ", "987654321", "0000",
                "DELA CRUZ", "JUAN", "SANTOS", null, "FILIPINO", "R", "Regular",
                schedule, "NCR", LocalDate.of(2026, 1, 1), employmentTo, birReason,
                LocalDate.of(2026, 1, 15), LocalDate.of(2026, 12, 15),
                mwe ? bd("120000") : bd("500000"),
                mwe ? bd("120000") : bd("100000"),
                mwe ? bd("0") : bd("400000"),
                mwe ? bd("0") : bd("22500"), mwe ? bd("0") : bd("22500"), bd("0"),
                mwe ? bd("0") : bd("22500"), bd("0"), bd("0"), bd("0"),
                mwe ? bd("0") : bd("22500"), mwe ? bd("0") : bd("22500"),
                true, "Reviewed", true, List.of());
    }

    private Map<String, BigDecimal> items(boolean mwe) {
        Map<String, BigDecimal> values = new LinkedHashMap<>();
        for (String item : List.of("29", "30", "31", "32", "33", "34", "35", "36",
                "37", "38", "39", "48", "52")) values.put(item, bd("0"));
        if (mwe) {
            values.put("29", bd("100000"));
            values.put("34", bd("10000"));
            values.put("36", bd("5000"));
            values.put("37", bd("5000"));
            values.put("38", bd("120000"));
        } else {
            values.put("34", bd("50000"));
            values.put("35", bd("20000"));
            values.put("36", bd("30000"));
            values.put("38", bd("100000"));
            values.put("39", bd("350000"));
            values.put("48", bd("50000"));
            values.put("52", bd("400000"));
        }
        return values;
    }

    private BigDecimal bd(String value) { return new BigDecimal(value); }
}
