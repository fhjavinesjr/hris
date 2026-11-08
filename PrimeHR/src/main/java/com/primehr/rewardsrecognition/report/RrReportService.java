package com.primehr.rewardsrecognition.report;
import com.primehr.integration.administrative.PermissionDataScope;
public interface RrReportService {byte[] nomination(String agency,String id,String actor,PermissionDataScope scope);byte[] evaluation(String agency,String id,String actor,PermissionDataScope scope);byte[] award(String agency,String id,String actor,PermissionDataScope scope);byte[] annualRegister(String agency,int year,String actor);RrReportData.Analytics analytics(String agency,int year);}
