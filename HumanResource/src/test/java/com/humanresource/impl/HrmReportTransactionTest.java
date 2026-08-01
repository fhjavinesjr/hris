package com.humanresource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HrmReportTransactionTest {

    @Test
    void reportsThatLoadHeaderLobsUseReadOnlyTransactions() throws Exception {
        assertReadOnly(CompensatoryOvertimeCreditImpl.class,
                "generateCertificateCoc", Long.class, OutputStream.class);
        assertReadOnly(PassSlipImpl.class,
                "generatePassSlipReport", Long.class, OutputStream.class);
        assertReadOnly(LeaveFormReportServiceImpl.class,
                "generateLeaveCard", Long.class, Integer.class, OutputStream.class);
    }

    private void assertReadOnly(
            Class<?> serviceClass,
            String methodName,
            Class<?>... parameterTypes) throws Exception {
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertNotNull(transactional, serviceClass.getSimpleName() + "." + methodName);
        assertTrue(transactional.readOnly(), serviceClass.getSimpleName() + "." + methodName);
    }
}
