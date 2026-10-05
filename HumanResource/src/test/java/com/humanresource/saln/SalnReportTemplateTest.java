package com.humanresource.saln;

import com.humanresource.reports.JasperReportRegistry;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JRPrintText;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SalnReportTemplateTest {
    @Test void generatesRepresentativeTwoPagePdf() throws Exception {
        Map<String,Object> p=new HashMap<>();
        List.of("compliance","declarant","position","agency","officeAddress","spouse","spouseDetails","filingMode",
                "multipleSpouses","children","realProperties","personalProperties","realSubtotal","personalSubtotal",
                "totalAssets","liabilities","totalLiabilities","netWorth","businessInterests","governmentRelatives",
                "governmentId","filingDate","footer").forEach(key->p.put(key,"Representative "+key));
        var print=JasperFillManager.fillReport(JasperReportRegistry.get("reports/saln/saln_main.jrxml"),p,new JREmptyDataSource(1));
        assertEquals(2,print.getPages().size());
        byte[] pdf=JasperExportManager.exportReportToPdf(print);
        assertTrue(pdf.length>5_000);assertArrayEquals(new byte[]{'%', 'P', 'D', 'F'},java.util.Arrays.copyOf(pdf,4));
        Path output=Path.of("target","saln-sample.pdf");Files.createDirectories(output.getParent());Files.write(output,pdf);
    }

    @Test void stressPaginationCreatesAs1AndAs2SheetsWithoutDroppingOverflowRows() throws Exception {
        SalnDtos.Response saln=mock(SalnDtos.Response.class);
        when(saln.id()).thenReturn(71L);when(saln.filingType()).thenReturn(SalnTypes.FilingType.ANNUAL);when(saln.salnYear()).thenReturn(2025);
        when(saln.asOfDate()).thenReturn(java.time.LocalDate.of(2025,12,31));when(saln.declarantFamilyName()).thenReturn("DELA CRUZ");when(saln.declarantFirstName()).thenReturn("JUAN");
        when(saln.filingMode()).thenReturn(SalnTypes.FilingMode.JOINT);when(saln.sourceType()).thenReturn(SalnTypes.SourceType.PAPER);when(saln.versionNo()).thenReturn(1);
        when(saln.dependents()).thenReturn(List.of());when(saln.governmentRelatives()).thenReturn(List.of());when(saln.businessInterestsNone()).thenReturn(false);when(saln.relativesInGovernmentNone()).thenReturn(true);
        when(saln.totals()).thenReturn(new SalnDtos.Totals(java.math.BigDecimal.TEN,java.math.BigDecimal.TEN,java.math.BigDecimal.valueOf(20),java.math.BigDecimal.ONE,java.math.BigDecimal.valueOf(19)));
        when(saln.realProperties()).thenReturn(java.util.stream.IntStream.range(0,70).mapToObj(i->new SalnDtos.RealPropertyItem(i%2==0?SalnTypes.OwnerType.DECLARANT:SalnTypes.OwnerType.SPOUSE,i%2==0?null:"SPOUSE","REAL-MARKER-"+i,"LAND","LOCATION",java.math.BigDecimal.ONE,java.math.BigDecimal.ONE,2020,"PURCHASE",java.math.BigDecimal.TEN)).toList());
        when(saln.personalProperties()).thenReturn(java.util.stream.IntStream.range(0,60).mapToObj(i->new SalnDtos.PersonalPropertyItem(i%3==0?SalnTypes.OwnerType.CHILD:SalnTypes.OwnerType.DECLARANT,i%3==0?"CHILD":"","PERSONAL-MARKER-"+i,2021,java.math.BigDecimal.TEN)).toList());
        when(saln.liabilities()).thenReturn(java.util.stream.IntStream.range(0,50).mapToObj(i->new SalnDtos.LiabilityItem(i%2==0?SalnTypes.OwnerType.DECLARANT:SalnTypes.OwnerType.SPOUSE,i%2==0?null:"SPOUSE","LIABILITY-MARKER-"+i,"CREDITOR",java.math.BigDecimal.TEN)).toList());
        when(saln.businessInterests()).thenReturn(java.util.stream.IntStream.range(0,40).mapToObj(i->new SalnDtos.BusinessInterestItem(i%2==0?SalnTypes.OwnerType.DECLARANT:SalnTypes.OwnerType.CHILD,i%2==0?null:"CHILD","BUSINESS-MARKER-"+i,"ADDRESS","NATURE",java.time.LocalDate.of(2020,1,1))).toList());
        var print=new SalnReportService(mock(SalnService.class)).render(saln);
        assertEquals(11,print.getPages().size(),"two main pages plus five AS-1 and four AS-2 sheets");
        String text=print.getPages().stream().flatMap(page->page.getElements().stream()).filter(JRPrintText.class::isInstance).map(JRPrintText.class::cast).map(JRPrintText::getFullText).reduce("",(a,b)->a+"\n"+b);
        assertTrue(text.contains("AS-1 ADDITIONAL SHEET"));assertTrue(text.contains("AS-2 ADDITIONAL SHEET"));
        assertTrue(text.contains("REAL-MARKER-69"));assertTrue(text.contains("PERSONAL-MARKER-59"));assertTrue(text.contains("LIABILITY-MARKER-49"));assertTrue(text.contains("BUSINESS-MARKER-39"));
        byte[] pdf=JasperExportManager.exportReportToPdf(print);assertTrue(pdf.length>25_000);Files.write(Path.of("target","saln-pagination-stress.pdf"),pdf);
    }
}
