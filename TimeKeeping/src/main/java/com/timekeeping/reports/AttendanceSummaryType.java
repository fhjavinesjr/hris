package com.timekeeping.reports;

import java.util.Arrays;

public enum AttendanceSummaryType {
    TARDINESS("tardiness", "TARDINESS SUMMARY", "tk.reports.tardiness"),
    UNDERTIME("undertime", "UNDERTIME SUMMARY", "tk.reports.undertime"),
    OVERTIME("overtime", "OVERTIME SUMMARY", "tk.reports.overtime"),
    NIGHT_DIFFERENTIAL("night-differential", "NIGHT DIFFERENTIAL WORK SUMMARY",
            "tk.reports.nightDifferential");

    private final String slug;
    private final String title;
    private final String permissionKey;

    AttendanceSummaryType(String slug, String title, String permissionKey) {
        this.slug = slug;
        this.title = title;
        this.permissionKey = permissionKey;
    }

    public String slug() { return slug; }
    public String title() { return title; }
    public String permissionKey() { return permissionKey; }

    public static AttendanceSummaryType fromSlug(String slug) {
        return Arrays.stream(values()).filter(value -> value.slug.equals(slug))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Select a supported attendance summary."));
    }
}
