package com.humanresource.integration.primehr;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Explicit local-provider gate; run by exact -Dtest name only. */
class HumanResourcePrimeHrIntakeSqlServerIT {
    @Test
    void populatedDatabaseAppliesV3WithoutChangingEmployeesOrAppointments() throws Exception {
        String url=System.getProperty("hrm.sqlserver.url","jdbc:sqlserver://localhost:1433;database=hrisof;trustServerCertificate=true;encrypt=true");
        String user=System.getProperty("hrm.sqlserver.user","admin");String password=System.getProperty("hrm.sqlserver.password","sa");
        long employees=scalar(url,user,password,"SELECT COUNT(*) FROM dbo.employee");
        long appointments=scalar(url,user,password,"SELECT COUNT(*) FROM dbo.employeeappointment");
        Flyway flyway=Flyway.configure().dataSource(url,user,password).schemas("dbo").defaultSchema("dbo")
                .baselineOnMigrate(true).baselineVersion("0")
                .locations("classpath:db/migration/primehr-intake/sqlserver")
                .placeholders(java.util.Map.of("hrmSchema","dbo")).load();
        flyway.migrate();
        assertEquals("3",flyway.info().current().getVersion().getVersion());
        assertEquals(employees,scalar(url,user,password,"SELECT COUNT(*) FROM dbo.employee"));
        assertEquals(appointments,scalar(url,user,password,"SELECT COUNT(*) FROM dbo.employeeappointment"));
        assertEquals(1,scalar(url,user,password,"SELECT COUNT(*) FROM sys.tables WHERE schema_id=SCHEMA_ID('dbo') AND name='rsp_appointment_handoff_receipt'"));
        assertEquals(1,scalar(url,user,password,"SELECT COUNT(*) FROM sys.tables WHERE schema_id=SCHEMA_ID('dbo') AND name='hrm_onboarding_case'"));
        assertEquals(1,scalar(url,user,password,"SELECT COUNT(*) FROM sys.tables WHERE schema_id=SCHEMA_ID('dbo') AND name='hrm_appointment_document'"));
        assertEquals(1,scalar(url,user,password,"SELECT COUNT(*) FROM sys.tables WHERE schema_id=SCHEMA_ID('dbo') AND name='hrm_appointment_document_audit_event'"));
        assertEquals(1,scalar(url,user,password,"SELECT COUNT(*) FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.employeeappointment') AND name='uk_hrm_active_appointment_plantilla'"));
    }
    private long scalar(String url,String user,String password,String sql)throws Exception{try(var connection=DriverManager.getConnection(url,user,password);var statement=connection.createStatement();var result=statement.executeQuery(sql)){result.next();return result.getLong(1);}}
}
