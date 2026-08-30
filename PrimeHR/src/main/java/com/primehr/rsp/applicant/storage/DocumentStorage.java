package com.primehr.rsp.applicant.storage;
import java.io.InputStream;
public interface DocumentStorage {
    String provider();
    void put(String objectKey,byte[] content,String mediaType);
    InputStream get(String objectKey);
    void delete(String objectKey);
}
