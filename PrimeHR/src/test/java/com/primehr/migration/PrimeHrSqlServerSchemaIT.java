package com.primehr.migration;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "primehr.applicant.enabled=false")
class PrimeHrSqlServerSchemaIT extends AbstractPrimeHrProviderIntegration {
}
