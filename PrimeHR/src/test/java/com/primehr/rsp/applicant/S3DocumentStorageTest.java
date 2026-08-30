package com.primehr.rsp.applicant;

import com.primehr.config.PrimeHrProperties;
import com.primehr.rsp.applicant.storage.S3DocumentStorage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3DocumentStorageTest {
    @Test
    void enabledS3StorageFailsClosedWithoutDurableBucket() {
        var properties = new PrimeHrProperties(null, null, null, null, null, null,
                new PrimeHrProperties.Storage(true, "s3", "", 100L,
                        List.of("application/pdf"), "", "ap-southeast-1", ""));

        assertThatThrownBy(() -> new S3DocumentStorage(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("durable S3 bucket");
    }
}
