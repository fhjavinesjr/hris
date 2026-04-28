package com.hris.common.utilities;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Shared utility for resolving salary/leave period date boundaries from
 * {@code salary_period_setting} table data.
 * <p>
 * The month offset convention:
 * <ul>
 *   <li>0  → reference month (the month being processed)</li>
 *   <li>-1 → one month before the reference month</li>
 *   <li>-2 → two months before the reference month</li>
 * </ul>
 * Days are automatically clamped to the last valid day of the target month
 * (e.g. day 31 in February resolves to Feb 28 or Feb 29).
 */
public class SalaryPeriodResolver {

    private static final DateTimeFormatter API_FORMATTER =
            DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

    private SalaryPeriodResolver() {}

    /**
     * Resolves the cut-off start date.
     *
     * @param day         day of month from the setting (1-31)
     * @param monthOffset month offset (0, -1, -2)
     * @param reference   the reference YearMonth (usually the current month)
     * @return            clamped LocalDate
     */
    public static LocalDate resolveCutoffStart(int day, int monthOffset, YearMonth reference) {
        YearMonth target = reference.plusMonths(monthOffset);
        int clampedDay = Math.min(day, target.lengthOfMonth());
        return LocalDate.of(target.getYear(), target.getMonth(), clampedDay);
    }

    /**
     * Resolves the cut-off end date.
     *
     * @param day         day of month from the setting (1-31)
     * @param monthOffset month offset (0, -1, -2)
     * @param reference   the reference YearMonth (usually the current month)
     * @return            clamped LocalDate
     */
    public static LocalDate resolveCutoffEnd(int day, int monthOffset, YearMonth reference) {
        YearMonth target = reference.plusMonths(monthOffset);
        int clampedDay = Math.min(day, target.lengthOfMonth());
        return LocalDate.of(target.getYear(), target.getMonth(), clampedDay);
    }

    /**
     * Returns true when {@code date} falls within the period defined by the setting
     * evaluated against {@code referenceMonth}.
     *
     * @param date                   date to test
     * @param cutoffStartDay         day from the setting
     * @param cutoffStartMonthOffset month offset from the setting
     * @param cutoffEndDay           day from the setting
     * @param cutoffEndMonthOffset   month offset from the setting
     * @param referenceMonth         the month used as "current" for offset calculation
     */
    public static boolean isDateInPeriod(
            LocalDate date,
            int cutoffStartDay, int cutoffStartMonthOffset,
            int cutoffEndDay,   int cutoffEndMonthOffset,
            YearMonth referenceMonth) {

        LocalDate start = resolveCutoffStart(cutoffStartDay, cutoffStartMonthOffset, referenceMonth);
        LocalDate end   = resolveCutoffEnd(cutoffEndDay, cutoffEndMonthOffset, referenceMonth);
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Formats a LocalDate to the API date-time string expected throughout HRIS.
     *
     * @param date      the date to format
     * @param endOfDay  when true, formats with 23:59:59; otherwise 00:00:00
     * @return          string like "03-01-2026 00:00:00"
     */
    public static String toApiFormat(LocalDate date, boolean endOfDay) {
        String time = endOfDay ? " 23:59:59" : " 00:00:00";
        return date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")) + time;
    }
}
