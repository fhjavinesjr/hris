package com.primehr.performancemanagement.report.application;

import com.primehr.performancemanagement.report.application.PerformanceFormalReportData.*;
import com.primehr.reports.JasperReportRegistry;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PerformanceFormalReportRenderer {
    public byte[] form(Form data) {
        Map<String,Object> parameters=header(data.header());
        parameters.put("finalScore",data.finalScore());
        parameters.put("ratingLabel",data.ratingLabel());
        return render("reports/spms_performance_form.jrxml",parameters,data.rows());
    }
    public byte[] monitoring(Monitoring data) {
        return render("reports/spms_monitoring_report.jrxml",header(data.header()),data.rows());
    }
    public byte[] coaching(Coaching data) {
        Map<String,Object> parameters=header(data.header());
        parameters.put("agenda",data.agenda());parameters.put("goal",data.goal());
        parameters.put("observedIssue",data.observedIssue());parameters.put("agreedAction",data.agreedAction());
        parameters.put("resourcesSupport",data.resourcesSupport());parameters.put("actionDueDate",data.actionDueDate());
        parameters.put("nextMeeting",data.nextMeeting());parameters.put("employeeVisibleFeedback",data.employeeVisibleFeedback());
        parameters.put("employeeResponse",data.employeeResponse());
        return render("reports/spms_coaching_report.jrxml",parameters,data.rows());
    }
    public byte[] intervention(Intervention data) {
        Map<String,Object> parameters=header(data.header());
        parameters.put("reportPurpose",data.reportPurpose());parameters.put("action",data.action());
        parameters.put("objectives",data.objectives());parameters.put("supportPlan",data.supportPlan());
        parameters.put("accountableEmployeeNo",data.accountableEmployeeNo());parameters.put("dateRange",data.dateRange());
        parameters.put("employeeResponse",data.employeeResponse());parameters.put("closure",data.closure());
        return render("reports/spms_intervention_report.jrxml",parameters,data.rows());
    }
    private static Map<String,Object> header(Header h){Map<String,Object> p=new LinkedHashMap<>();p.put("title",h.title());p.put("formType",h.formType());p.put("lifecycleState",h.lifecycleState());p.put("agencyName",h.agencyName());p.put("agencyAddress",h.agencyAddress());p.put("employeeNo",h.employeeNo());p.put("employeeName",h.employeeName());p.put("positionName",h.positionName());p.put("organizationName",h.organizationName());p.put("period",h.period());p.put("supervisor",h.supervisor());p.put("approver",h.approver());p.put("sourceReference",h.sourceReference());p.put("metadataReference",h.metadataReference());p.put("generated",h.generated());p.put("templateVersion","PHASE-6F.2-v1");return p;}
    private static byte[] render(String template,Map<String,Object> parameters,List<?> rows){try{JasperPrint print=JasperFillManager.fillReport(JasperReportRegistry.get(template),parameters,new JRBeanCollectionDataSource(rows));return JasperExportManager.exportReportToPdf(print);}catch(Exception e){throw new IllegalStateException("Unable to generate formal SPMS report",e);}}
}
