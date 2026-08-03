package com.primehr.migration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("flyway-h2")
class PrimeHrFlywaySchemaIntegrationTest extends AbstractPrimeHrProviderIntegration {
}
