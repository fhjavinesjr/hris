package com.primehr.learningdevelopment.report.application;

import java.util.List;

public final class LdFormalReportData {
    private LdFormalReportData() {}
    public record Header(String title,String subject,String lifecycle,String employee,String organization,
            String period,String sourceReference,String generated) {}
    public record Row(Integer rowNumber,String section,String label,String detail,String status) {}
    public record Document(Header header,List<Row> rows) {}
}
