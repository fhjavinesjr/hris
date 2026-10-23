package com.humanresource.saln;

import com.humanresource.reports.JasperReportRegistry;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

@Service
public class SalnReportService {
    private static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("MMMM d, uuuu",Locale.ENGLISH);
    private static final int MAIN_REAL_ROWS=8,MAIN_PERSONAL_ROWS=8,MAIN_LIABILITY_ROWS=6,MAIN_BUSINESS_ROWS=5;
    private static final int ADDITIONAL_ROWS_PER_PAGE=24;
    private final SalnService salnService;
    public SalnReportService(SalnService salnService){this.salnService=salnService;}

    public void generateForEmployee(String employeeNo,Long id,OutputStream out) throws Exception{generate(salnService.myGet(employeeNo,id),employeeNo,out);}
    public void generateForAdmin(Long id,String actor,OutputStream out) throws Exception{generate(salnService.adminGet(id),actor,out);}
    private void generate(SalnDtos.Response saln,String actor,OutputStream out) throws Exception{JasperExportManager.exportReportToPdfStream(render(saln),out);salnService.recordPrint(saln.id(),actor);}

    JasperPrint render(SalnDtos.Response s) throws Exception {
        Map<String,Object> p=new HashMap<>();
        p.put("compliance",switch(s.filingType()){case ASSUMPTION->"[X] Assumption of office as of "+DATE.format(s.asOfDate());case ANNUAL->"[X] Annual filing as of December 31, "+s.salnYear();case SEPARATION->"[X] Exit as of "+DATE.format(s.asOfDate());});
        p.put("declarant",s.declarantFamilyName()+", "+s.declarantFirstName()+" "+n(s.declarantMiddleInitial()));p.put("position",n(s.declarantPosition()));p.put("agency",n(s.declarantAgencyOffice()));p.put("officeAddress",n(s.declarantOfficeAddress()));
        p.put("spouse",n(s.spouseFullName()));p.put("spouseDetails",n(s.spousePosition())+" | "+n(s.spouseAgencyOffice())+" | "+n(s.spouseOfficeAddress()));p.put("filingMode",s.filingMode().name().replace('_',' '));p.put("multipleSpouses",n(s.multipleSpouses()));
        p.put("children",rows(s.dependents(),x->x.name()+" | AGE "+x.age()));
        List<String> real=map(s.realProperties(),x->owner(x.ownerType(),x.ownerName())+" | "+x.description()+" | "+x.kind()+" | "+x.exactLocation()+" | ASSESSED "+money(x.assessedValue())+" | FMV "+money(x.fairMarketValue())+" | "+x.acquisitionYear()+" "+n(x.acquisitionMode())+" | COST "+money(x.acquisitionCost()));
        List<String> personal=map(s.personalProperties(),x->owner(x.ownerType(),x.ownerName())+" | "+x.description()+" | "+x.acquisitionYear()+" | "+money(x.acquisitionCost()));
        List<String> liabilities=map(s.liabilities(),x->owner(x.ownerType(),x.ownerName())+" | "+x.nature()+" | "+x.creditorName()+" | "+money(x.outstandingBalance()));
        List<String> business=map(s.businessInterests(),x->owner(x.ownerType(),x.ownerName())+" | "+x.entityName()+" | "+x.businessAddress()+" | "+x.nature()+" | "+(x.dateAcquired()==null?"":DATE.format(x.dateAcquired())));
        p.put("realProperties",numbered(real,0,MAIN_REAL_ROWS));p.put("personalProperties",numbered(personal,0,MAIN_PERSONAL_ROWS));
        p.put("realSubtotal",money(s.totals().realProperties()));p.put("personalSubtotal",money(s.totals().personalProperties()));p.put("totalAssets",money(s.totals().totalAssets()));
        p.put("liabilities",numbered(liabilities,0,MAIN_LIABILITY_ROWS));p.put("totalLiabilities",money(s.totals().totalLiabilities()));p.put("netWorth",money(s.totals().netWorth()));
        p.put("businessInterests",s.businessInterestsNone()?"[X] I/We do not have any business interest or financial connection.":numbered(business,0,MAIN_BUSINESS_ROWS));
        p.put("governmentRelatives",s.relativesInGovernmentNone()?"[X] I/We do not know of any relative/s in government service.":rows(s.governmentRelatives(),x->x.name()+" | "+x.relationship()+" | "+x.position()+" | "+x.agencyOfficeAddress()));
        p.put("governmentId","Government Issued ID: "+n(s.governmentIdType())+"\nID No.: "+n(s.governmentIdNo())+"\nDate Issued: "+(s.governmentIdDateIssued()==null?"":DATE.format(s.governmentIdDateIssued())));p.put("filingDate",s.submissionDate()==null?"":DATE.format(s.submissionDate()));p.put("footer","SALN ID "+s.id()+" | Version "+s.versionNo()+" | Source "+s.sourceType());
        JasperPrint print=JasperFillManager.fillReport(JasperReportRegistry.get("reports/saln/saln_main.jrxml"),p,new JREmptyDataSource(1));
        List<AdditionalRow> overflow=new ArrayList<>();appendOverflow(overflow,"REAL PROPERTY",real,MAIN_REAL_ROWS,i->s.realProperties().get(i).ownerType());appendOverflow(overflow,"PERSONAL PROPERTY",personal,MAIN_PERSONAL_ROWS,i->s.personalProperties().get(i).ownerType());appendOverflow(overflow,"LIABILITY",liabilities,MAIN_LIABILITY_ROWS,i->s.liabilities().get(i).ownerType());appendOverflow(overflow,"BUSINESS INTEREST",business,MAIN_BUSINESS_ROWS,i->s.businessInterests().get(i).ownerType());
        appendSheetGroup(print,p,overflow.stream().filter(x->x.ownerType()==SalnTypes.OwnerType.DECLARANT).map(AdditionalRow::text).toList(),"AS-1","DECLARANT");
        appendSheetGroup(print,p,overflow.stream().filter(x->x.ownerType()!=SalnTypes.OwnerType.DECLARANT).map(AdditionalRow::text).toList(),"AS-2","SPOUSE AND UNMARRIED CHILDREN BELOW 18");
        return print;
    }
    private static void appendOverflow(List<AdditionalRow> target,String section,List<String> values,int from,Function<Integer,SalnTypes.OwnerType> ownerAt){for(int i=from;i<values.size();i++)target.add(new AdditionalRow(ownerAt.apply(i),section+" #"+(i+1)+" | "+values.get(i)));}
    private static void appendSheetGroup(JasperPrint target,Map<String,Object> base,List<String> rows,String code,String heading) throws Exception {for(int from=0,page=1;from<rows.size();from+=ADDITIONAL_ROWS_PER_PAGE,page++){Map<String,Object> p=new HashMap<>();p.put("formCode",code);p.put("declarant",base.get("declarant"));p.put("ownerHeading",heading);p.put("continuationNo",page);p.put("rows",numbered(rows,from,ADDITIONAL_ROWS_PER_PAGE));p.put("footer",base.get("footer"));JasperPrint sheet=JasperFillManager.fillReport(JasperReportRegistry.get("reports/saln/saln_additional_sheet.jrxml"),p,new JREmptyDataSource(1));target.getPages().addAll(sheet.getPages());}}
    private static <T> List<String> map(List<T> values,Function<T,String> mapper){return values==null?List.of():values.stream().map(mapper).toList();}
    private static String numbered(List<String> values,int from,int limit){if(values==null||from>=values.size())return "-- NONE DECLARED --";StringBuilder b=new StringBuilder();for(int i=from;i<Math.min(values.size(),from+limit);i++)b.append(i+1).append(". ").append(values.get(i)).append('\n');return b.toString();}
    private static <T> String rows(List<T> values,Function<T,String> mapper){return numbered(map(values,mapper),0,Integer.MAX_VALUE);}
    private static String owner(SalnTypes.OwnerType type,String name){return type+(type==SalnTypes.OwnerType.DECLARANT?"":" ("+n(name)+")");}
    private static String n(String value){return value==null||value.isBlank()?"N/A":value;}
    private static String money(BigDecimal value){if(value==null)return "N/A";return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH")).format(value);}
    private record AdditionalRow(SalnTypes.OwnerType ownerType,String text){}
}
