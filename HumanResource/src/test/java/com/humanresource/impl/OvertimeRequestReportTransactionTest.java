package com.humanresource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OvertimeRequestReportTransactionTest {

    @Test
    void reportGenerationKeepsPostgresqlLargeObjectReadsInsideAReadOnlyTransaction()
            throws Exception {
        Method method = OvertimeRequestImpl.class.getMethod(
                "generateOvertimeAuthorization",
                Long.class,
                OutputStream.class
        );

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertTrue(transactional.readOnly());
    }
}
