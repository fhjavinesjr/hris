package com.primehr.rsp.report;

import com.primehr.reports.JasperReportRegistry;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.*;

@Component
public class RspProcessReportRenderer {
    public byte[] register(RspProcessReportData.RegisterPage data,String generated){Map<String,Object> p=base(data.agencyId(),data.from().toString(),data.to().toString(),data.dateBasis(),generated);
        p.put("historyMode",data.includeHistory()?"CURRENT + EXPLICIT REVISION HISTORY":"CURRENT/LATEST REVISIONS ONLY");p.put("filters",data.filterSummary());p.put("pageScope","Publication page "+(data.page()+1)+" of "+Math.max(1,data.totalPages())+"; "+data.totalElements()+" matching publications");return render("reports/rsp_register.jrxml",p,data.rows());}
    public byte[] analytics(RspProcessReportData.Analytics data,String generated){Map<String,Object> p=base(data.agencyId(),data.from().toString(),data.to().toString(),data.dateBasis(),generated);p.put("historyMode",data.includeHistory()?"CURRENT TOTALS + SEPARATELY LABELLED HISTORY":"CURRENT/LATEST REVISIONS ONLY");p.put("filters",data.filterSummary());p.put("pageScope","Counts reconcile within the selected cohort; zero-denominator rates are 0.00%.");return render("reports/rsp_process_analytics.jrxml",p,data.metrics());}
    private static Map<String,Object> base(String agency,String from,String to,String basis,String generated){Map<String,Object> p=new LinkedHashMap<>();p.put("agency",agency);p.put("from",from);p.put("to",to);p.put("dateBasis",basis);p.put("timezone",RspProcessReportServiceImpl.TIMEZONE);p.put("generated",generated);p.put("templateVersion",RspProcessReportServiceImpl.TEMPLATE_VERSION);return p;}
    private static byte[] render(String template,Map<String,Object> parameters,List<?> rows){try{JasperReport report=JasperReportRegistry.get(template);JasperPrint print=JasperFillManager.fillReport(report,parameters,new JRBeanCollectionDataSource(rows));return JasperExportManager.exportReportToPdf(print);}catch(Exception e){throw new IllegalStateException("Unable to generate RSP process report",e);}}
}
