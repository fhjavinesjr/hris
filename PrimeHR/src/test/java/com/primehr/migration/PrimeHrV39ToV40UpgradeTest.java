package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class PrimeHrV39ToV40UpgradeTest {
 @Test void populatedLearningNeedSurvivesParticipationMigration()throws Exception{
  String database="primehr_v40_"+UUID.randomUUID().toString().replace("-","");String url="jdbc:h2:mem:"+database+";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";Map<String,String> placeholders=Map.of("primehrSchema","PUBLIC");Flyway initial=flyway(url,placeholders,"39");initial.migrate();String id=UUID.randomUUID().toString();
  try{try(var c=DriverManager.getConnection(url,"sa","");var s=c.createStatement()){s.executeUpdate("INSERT INTO PUBLIC.ld_learning_need (id,agency_id,employee_id,employee_no,employee_name,appointment_id,employee_fingerprint,source_type,development_objective,current_state,target_state,success_measure,priority,status,record_version,created_by,created_at,updated_by,updated_at) VALUES ('"+id+"','UPGRADE-40',1,'E-1','Employee',1,'fingerprint','ORGANIZATIONAL_NEED','Objective','Current','Target','Measure','HIGH','OPEN',0,'test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP)");}Flyway upgraded=flyway(url,placeholders,"40");upgraded.migrate();assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("40");try(var c=DriverManager.getConnection(url,"sa","");var rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM PUBLIC.ld_learning_need WHERE id='"+id+"'")){rs.next();assertThat(rs.getLong(1)).isOne();}try(var c=DriverManager.getConnection(url,"sa","");var rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM PUBLIC.ld_nomination")){rs.next();assertThat(rs.getLong(1)).isZero();}}finally{initial.clean();}
 }
 private Flyway flyway(String url,Map<String,String> placeholders,String target){return Flyway.configure().dataSource(url,"sa","").locations("classpath:db/migration/postgresql").schemas("PUBLIC").defaultSchema("PUBLIC").placeholders(placeholders).target(MigrationVersion.fromVersion(target)).cleanDisabled(false).load();}
}
