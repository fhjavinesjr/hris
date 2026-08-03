package com.primehr.migration;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * Activated only by an explicit Surefire -Dtest=PrimeHrRealDatabaseIT command.
 * The active Spring profile and datasource must point to a disposable
 * real-provider schema.
 */
@SpringBootTest
class PrimeHrRealDatabaseIT extends AbstractPrimeHrProviderIntegration {
}
