package com.primehr.rsp.report;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class RspFormalReportRenderer {
    public byte[] comparative(RspFormalReportData.Comparative data) {
        Map<String,Object> p=base(data.agency(),"CONFIDENTIAL - HRMPSB / AUTHORIZED HR ONLY",data.generated());
        p.put("proceedingId",data.proceedingId());p.put("publicationId",data.publicationId());p.put("vacancy",data.vacancy());
        p.put("assignment",data.assignment());p.put("policy",data.policy());p.put("committee",data.committee());
        p.put("meeting",data.meeting());p.put("sourceFingerprint",data.sourceFingerprint());p.put("finalized",data.finalized());
        return render("reports/rsp_comparative_evaluation.jrxml",p,data.rows());
    }

    public byte[] selection(RspFormalReportData.Selection data) {
        Map<String,Object> p=base(data.agency(),"CONFIDENTIAL - AUTHORIZED HR ONLY",data.generated());
        p.put("selectionId",data.selectionId());p.put("proceedingId",data.proceedingId());p.put("publicationId",data.publicationId());
        p.put("vacancy",data.vacancy());p.put("revision",data.revision());p.put("outcome",data.outcome());
        p.put("authorityDecision",data.authorityDecision());p.put("offer",data.offer());p.put("handoff",data.handoff());
        p.put("sourceFingerprint",data.sourceFingerprint());p.put("finalized",data.finalized());
        return render("reports/rsp_selection_process_record.jrxml",p,data.rows());
    }

    public byte[] evidenceIndex(RspFormalReportData.EvidenceIndex data) {
        Map<String,Object> p=base(data.agency(),"CONFIDENTIAL METADATA INDEX - NO EVIDENCE CONTENT",data.generated());
        p.put("selectionId",data.selectionId());p.put("proceedingId",data.proceedingId());p.put("publicationId",data.publicationId());
        p.put("vacancy",data.vacancy());p.put("sourceFingerprint",data.sourceFingerprint());p.put("terminalStatus",data.terminalStatus());
        return render("reports/rsp_evidence_index.jrxml",p,data.rows());
    }

    private static Map<String,Object> base(String agency,String classification,String generated){Map<String,Object> p=new LinkedHashMap<>();p.put("agency",agency);p.put("classification",classification);p.put("generated",generated);p.put("templateVersion","PHASE-5F.1-v1");return p;}
    private static byte[] render(String template,Map<String,Object> parameters,List<?> rows){
        try(InputStream in=new ClassPathResource(template).getInputStream()){
            JasperReport report=JasperCompileManager.compileReport(in);
            JasperPrint print=JasperFillManager.fillReport(report,parameters,new JRBeanCollectionDataSource(rows));
            return JasperExportManager.exportReportToPdf(print);
        }catch(Exception e){throw new IllegalStateException("Unable to generate formal RSP report",e);}
    }
}
