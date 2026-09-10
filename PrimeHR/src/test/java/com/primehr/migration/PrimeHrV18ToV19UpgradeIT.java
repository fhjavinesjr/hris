package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties={"spring.jpa.hibernate.ddl-auto=none","primehr.applicant.enabled=false"})
class PrimeHrV18ToV19UpgradeIT {
    @Autowired DataSource dataSource;@Autowired JdbcTemplate jdbc;
    @Value("${spring.flyway.locations}")String location;

    @Test void populatedEvaluationPolicySurvivesExecutionMigration() throws Exception {
        String schema="primehr_v19_"+UUID.randomUUID().toString().replace("-","");
        Flyway initial=Flyway.configure().dataSource(dataSource).locations(location).schemas(schema).defaultSchema(schema)
                .placeholders(Map.of("primehrSchema",schema)).target(MigrationVersion.fromVersion("18")).cleanDisabled(false).load();
        try {
            initial.migrate();assertThat(initial.info().current().getVersion().getVersion()).isEqualTo("18");
            String prefix="["+schema+"].";String id=UUID.randomUUID().toString();Timestamp now=Timestamp.from(Instant.now());
            jdbc.update("INSERT INTO "+prefix+"rsp_evaluation_policy (id,agency_id,code,normalized_code,name,description,merit_selection_plan_reference,definition_version,supersedes_id,status,effective_from,effective_to,rounding_scale,rounding_mode,tie_rule,aggregation_mode,published_by,published_at,record_version,created_by,created_at,updated_by,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    id,"UPGRADE-19","EVAL-19","EVAL-19","Preserved evaluation policy",null,"MSP-19",1,null,"DRAFT",null,null,2,"HALF_UP","COMPETITION","NORMALIZED_WEIGHTED_SUM",null,null,0L,"test",now,"test",now);
            Flyway upgraded=Flyway.configure().dataSource(dataSource).locations(location).schemas(schema).defaultSchema(schema)
                    .placeholders(Map.of("primehrSchema",schema)).target(MigrationVersion.fromVersion("19")).cleanDisabled(false).load();
            upgraded.migrate();assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("19");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM "+prefix+"rsp_evaluation_policy WHERE id=?",Long.class,id)).isEqualTo(1L);
            for(String table:List.of("rsp_evaluation_session","rsp_evaluation_session_candidate","rsp_evaluation_assignment",
                    "rsp_conflict_declaration","rsp_stage_result","rsp_panel_rating","rsp_panel_rating_item",
                    "rsp_reference_check","rsp_evaluation_evidence","rsp_hrmpsb_meeting","rsp_hrmpsb_attendance",
                    "rsp_hrmpsb_resolution","rsp_comparative_evaluation","rsp_comparative_evaluation_item"))
                assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM "+prefix+table,Long.class)).isZero();
        } finally {initial.clean();}
    }
}
