package com.humanresource.saln;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Live-provider acceptance gate. It creates and removes only an isolated SQL Server database. */
class SalnMigrationProviderIT {
    @Test void appliesExactScriptToLiveSqlServer() throws Exception {
        String applicationUrl=System.getProperty("saln.sqlserver.url","jdbc:sqlserver://localhost:1433;database=hrisof;trustServerCertificate=true;encrypt=true");
        String masterUrl=applicationUrl.replaceFirst("(?i)database(Name)?=[^;]+","database=master");
        String user=System.getProperty("saln.sqlserver.user"),password=System.getProperty("saln.sqlserver.password");
        assumeTrue(user!=null&&!user.isBlank()&&password!=null&&!password.isBlank(),"Set saln.sqlserver.user and saln.sqlserver.password to run the live SQL Server gate.");
        String database="saln_validation_"+Long.toUnsignedString(System.nanoTime(),36);assertTrue(database.matches("[a-z0-9_]+"));
        try(Connection c=DriverManager.getConnection(masterUrl,user,password);Statement s=c.createStatement()){s.execute("CREATE DATABASE ["+database+"]");}
        String testUrl=masterUrl.replace("database=master","database="+database);
        try{
            executeScript(testUrl,user,password,"db/manual-migration/saln/sqlserver.sql");
            executeScript(testUrl,user,password,"db/manual-migration/saln/sqlserver.sql");
            try(Connection c=DriverManager.getConnection(testUrl,user,password);Statement s=c.createStatement()){
                assertTableCount(s,"SELECT COUNT(*) FROM sys.tables WHERE name LIKE 'saln%'",11);
                s.executeUpdate("INSERT INTO saln(employee_id,employee_no,filing_type,saln_year,reference_date,as_of_date,due_date,status,source_type,declarant_family_name,declarant_first_name,filing_mode,created_at,created_by,updated_at,updated_by) VALUES(1,'E-1','ANNUAL',2025,'2025-12-31','2025-12-31','2026-04-30','DRAFT','ONLINE','DELA CRUZ','JUAN','NOT_APPLICABLE',SYSDATETIME(),'test',SYSDATETIME(),'test')");
                s.executeUpdate("INSERT INTO saln_real_property(saln_id,row_no,owner_type,description,property_kind,exact_location,acquisition_cost) VALUES(1,1,'DECLARANT','LOT','LAND','MANILA',100.00)");
            }
        }finally{try(Connection c=DriverManager.getConnection(masterUrl,user,password);Statement s=c.createStatement()){s.execute("ALTER DATABASE ["+database+"] SET SINGLE_USER WITH ROLLBACK IMMEDIATE");s.execute("DROP DATABASE ["+database+"]");}}
    }

    @Test void appliesExactScriptToLivePostgreSql() throws Exception {
        String configuredUrl=System.getProperty("saln.postgresql.url","jdbc:postgresql://localhost:15432/saln_validation");String user=System.getProperty("saln.postgresql.user","postgres"),password=System.getProperty("saln.postgresql.password","saln_test_only");
        int queryAt=configuredUrl.indexOf('?'),pathEnd=queryAt<0?configuredUrl.length():queryAt,slash=configuredUrl.lastIndexOf('/',pathEnd-1);String suffix=queryAt<0?"":configuredUrl.substring(queryAt);String adminUrl=configuredUrl.substring(0,slash+1)+"postgres"+suffix;
        String database="saln_validation_"+Long.toUnsignedString(System.nanoTime(),36);assertTrue(database.matches("[a-z0-9_]+"));String url=configuredUrl.substring(0,slash+1)+database+suffix;
        try(Connection c=DriverManager.getConnection(adminUrl,user,password);Statement s=c.createStatement()){s.execute("CREATE DATABASE "+database);}
        try{
            executeScript(url,user,password,"db/manual-migration/saln/postgresql.sql");executeScript(url,user,password,"db/manual-migration/saln/postgresql.sql");
            try(Connection c=DriverManager.getConnection(url,user,password);Statement s=c.createStatement()){
                assertTableCount(s,"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name LIKE 'saln%'",11);
                s.executeUpdate("INSERT INTO saln(employee_id,employee_no,filing_type,saln_year,reference_date,as_of_date,due_date,status,source_type,declarant_family_name,declarant_first_name,filing_mode,created_at,created_by,updated_at,updated_by) VALUES(1,'E-1','ANNUAL',2025,DATE '2025-12-31',DATE '2025-12-31',DATE '2026-04-30','DRAFT','ONLINE','DELA CRUZ','JUAN','NOT_APPLICABLE',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP,'test')");
                s.executeUpdate("INSERT INTO saln_real_property(saln_id,row_no,owner_type,description,property_kind,exact_location,acquisition_cost) VALUES(1,1,'DECLARANT','LOT','LAND','MANILA',100.00)");
            }
        }finally{try(Connection c=DriverManager.getConnection(adminUrl,user,password);Statement s=c.createStatement()){s.execute("DROP DATABASE IF EXISTS "+database+" WITH (FORCE)");}}
    }
    private static void executeScript(String url,String user,String password,String resource)throws Exception{String sql=new String(new ClassPathResource(resource).getInputStream().readAllBytes(),StandardCharsets.UTF_8);try(Connection c=DriverManager.getConnection(url,user,password);Statement s=c.createStatement()){s.execute(sql);}}
    private static void assertTableCount(Statement statement,String sql,int expected)throws Exception{try(ResultSet result=statement.executeQuery(sql)){assertTrue(result.next());assertEquals(expected,result.getInt(1));}}
}
