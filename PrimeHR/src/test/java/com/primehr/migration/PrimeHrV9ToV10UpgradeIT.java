package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Explicit real-provider populated V9-to-V10 upgrade gate. */
@SpringBootTest(properties={"spring.flyway.target=9","spring.jpa.hibernate.ddl-auto=none"})
class PrimeHrV9ToV10UpgradeIT {
    @Autowired DataSource dataSource; @Autowired JdbcTemplate jdbc; @Autowired Flyway initialFlyway;
    @Value("${spring.flyway.default-schema}") String schema;
    @Value("${spring.flyway.locations}") String migrationLocation;

    @Test void populatedV9HistoryUpgradesToV10WithoutMutation() throws Exception {
        assertThat(initialFlyway.info().current().getVersion().getVersion()).isEqualTo("9");
        boolean sqlServer;
        try(var connection=dataSource.getConnection()) {
            sqlServer=connection.getMetaData().getDatabaseProductName().toLowerCase().contains("microsoft");
        }
        String prefix=sqlServer?"["+schema+"].":"\""+schema+"\".";
        String id=UUID.randomUUID().toString(); Timestamp now=Timestamp.from(Instant.now());
        jdbc.update("INSERT INTO "+prefix+"prime_gap_priority_scheme " +
                        "(id,agency_id,code,name,description,status,definition_version,supersedes_id,effective_from,effective_to,active,display_order,published_by,published_at,record_version,created_by,created_at,updated_by,updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                id,"UPGRADE-AGENCY","V9-SCHEME","Existing V9 scheme",null,"DRAFT",1,null,null,null,false,0,
                null,null,0L,"upgrade-test",now,"upgrade-test",now);
        Flyway upgraded=Flyway.configure().dataSource(dataSource).locations(migrationLocation)
                .schemas(schema).defaultSchema(schema).placeholders(Map.of("primehrSchema",schema))
                .target(MigrationVersion.fromVersion("10")).load();
        upgraded.migrate();
        assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("10");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM "+prefix+"prime_gap_priority_scheme WHERE id=?",Long.class,id)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM "+prefix+"prime_ld_referral",Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM "+prefix+"prime_ld_referral_item",Long.class)).isZero();
    }
}
