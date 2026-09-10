package com.primehr.rsp.report;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class RspProcessReportRendererTest {
    private final RspProcessReportRenderer renderer=new RspProcessReportRenderer();
    @Test void registerAndAnalyticsTemplatesProduceMultiPagePdfAndHandleNoData() throws Exception {
        List<RspProcessReportData.RegisterRow> rows=new ArrayList<>();for(int i=1;i<=35;i++)rows.add(new RspProcessReportData.RegisterRow("publication-"+i,"Medical Officer III / Plantilla "+i,"Clinical Division","2026-08-31","CLOSED",12,1,8,3,2,"FINALIZED","selection-"+i,"v2","FINALIZED","SELECTED","ACCEPTED","CLOSED","COMPLETED",1,"2026-09-07 10:00 PST"));
        var page=new RspProcessReportData.RegisterPage("ISOFT TEST AGENCY",LocalDate.parse("2026-01-01"),LocalDate.parse("2026-12-31"),"PUBLICATION_CLOSING_DATE",true,"Filters: publication=CLOSED","Asia/Manila",0,100,35,1,rows);
        byte[] register=renderer.register(page,"tester at 2026-09-07 10:00 PST");
        byte[] empty=renderer.register(new RspProcessReportData.RegisterPage("ISOFT TEST AGENCY",page.from(),page.to(),page.dateBasis(),false,"No additional filters",page.timezone(),0,25,0,0,List.of()),"tester");
        List<RspProcessReportData.Metric> metrics=List.of(new RspProcessReportData.Metric("SCREENING_FINALIZED","Screening completion rate",8,10L,new BigDecimal("80.00"),"PERCENT","Current finalized screening cases / current applications; null records excluded."));
        byte[] analytics=renderer.analytics(new RspProcessReportData.Analytics("ISOFT TEST AGENCY",page.from(),page.to(),page.dateBasis(),page.timezone(),false,page.filterSummary(),metrics),"tester");
        Files.write(Path.of("target","phase5f3-rsp-register.pdf"),register);Files.write(Path.of("target","phase5f3-rsp-analytics.pdf"),analytics);
        assertThat(register).startsWith("%PDF".getBytes()).hasSizeGreaterThan(3_000);assertThat(empty).startsWith("%PDF".getBytes());assertThat(analytics).startsWith("%PDF".getBytes()).hasSizeGreaterThan(2_000);
    }
}
