package com.humanresource.integration.primehr;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/** Explicit local-provider gate; creates and removes only its isolated test schema. */
class HumanResourcePrimeHrIntakeFreshSqlServerIT {
    @Test void appliesV1ThroughV3ToAFreshLegacyCompatibleSchema() throws Exception {
        String url=System.getProperty("hrm.sqlserver.url","jdbc:sqlserver://localhost:1433;database=hrisof;trustServerCertificate=true;encrypt=true");String user=System.getProperty("hrm.sqlserver.user","admin"),password=System.getProperty("hrm.sqlserver.password","sa");String schema="hrm5e3_"+Long.toUnsignedString(System.nanoTime(),36);assertTrue(schema.matches("[a-z0-9_]+"));
        try(Connection c=DriverManager.getConnection(url,user,password);Statement s=c.createStatement()){
            s.execute("CREATE SCHEMA ["+schema+"]");
            s.execute("CREATE TABLE ["+schema+"].employee(employeeId BIGINT PRIMARY KEY)");
            s.execute("CREATE TABLE ["+schema+"].employeeappointment(employeeAppointmentId BIGINT IDENTITY PRIMARY KEY,employeeId BIGINT NOT NULL,plantillaId INT NOT NULL,activeAppointment BIT NOT NULL)");
        }
        try{
            Flyway flyway=Flyway.configure().dataSource(url,user,password).schemas(schema).defaultSchema(schema).baselineOnMigrate(true).baselineVersion("0").locations("classpath:db/migration/primehr-intake/sqlserver").placeholders(Map.of("hrmSchema",schema)).load();flyway.migrate();assertEquals("3",flyway.info().current().getVersion().getVersion());try(Connection c=DriverManager.getConnection(url,user,password);Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM sys.tables WHERE schema_id=SCHEMA_ID('"+schema+"') AND name IN ('hrm_onboarding_case','employee_activation_invitation','hrm_appointment_provenance','hrm_appointment_document','hrm_appointment_document_audit_event')")){assertTrue(r.next());assertEquals(5,r.getInt(1));}
        }finally{cleanup(url,user,password,schema);}
    }
    private void cleanup(String url,String user,String password,String schema)throws Exception{String[] tables={"hrm_appointment_document_audit_event","hrm_appointment_document","hrm_onboarding_audit_event","employee_activation_invitation","hrm_appointment_provenance","hrm_onboarding_item","hrm_onboarding_case","hrm_onboarding_template_item","hrm_onboarding_template","rsp_appointment_handoff_receipt","flyway_schema_history","employeeappointment","employee"};try(Connection c=DriverManager.getConnection(url,user,password);Statement s=c.createStatement()){for(String table:tables)s.execute("IF OBJECT_ID('["+schema+"]."+table+"','U') IS NOT NULL DROP TABLE ["+schema+"]."+table);s.execute("DROP SCHEMA ["+schema+"]");}}
}
