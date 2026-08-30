package com.primehr.migration;

import org.flywaydb.core.Flyway;import org.flywaydb.core.api.MigrationVersion;import org.junit.jupiter.api.Test;import org.springframework.beans.factory.annotation.*;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;import java.sql.*;import java.time.*;import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties={"spring.flyway.target=12","spring.jpa.hibernate.ddl-auto=none","primehr.applicant.enabled=false"})
class PrimeHrV12ToV13UpgradeIT {
 @Autowired DataSource dataSource;@Autowired JdbcTemplate jdbc;@Autowired Flyway initial;
 @Value("${spring.flyway.default-schema}") String schema;@Value("${spring.flyway.locations}") String location;
 @Test void populatedV12DataSurvivesApplicantFoundationMigration() throws Exception {
  assertThat(initial.info().current().getVersion().getVersion()).isEqualTo("12");boolean sqlServer;
  try(var c=dataSource.getConnection()){sqlServer=c.getMetaData().getDatabaseProductName().toLowerCase().contains("microsoft");}
  String p=sqlServer?"["+schema+"].":"\""+schema+"\".";String id=UUID.randomUUID().toString();Timestamp now=Timestamp.from(Instant.now());
  jdbc.update("INSERT INTO "+p+"rsp_recruitment_plan (id,agency_id,code,title,period_start,period_end,description,status,record_version,created_by,created_at,updated_by,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",id,"UPGRADE-AGENCY","V12-PLAN","Existing V12 plan",java.sql.Date.valueOf(LocalDate.of(2029,1,1)),java.sql.Date.valueOf(LocalDate.of(2029,12,31)),null,"DRAFT",0L,"test",now,"test",now);
  Flyway upgraded=Flyway.configure().dataSource(dataSource).locations(location).schemas(schema).defaultSchema(schema).placeholders(Map.of("primehrSchema",schema)).target(MigrationVersion.fromVersion("13")).load();upgraded.migrate();
  assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("13");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM "+p+"rsp_recruitment_plan WHERE id=?",Long.class,id)).isEqualTo(1L);assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM "+p+"rsp_applicant_account",Long.class)).isZero();
 }
}
