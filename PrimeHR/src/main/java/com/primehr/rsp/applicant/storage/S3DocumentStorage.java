package com.primehr.rsp.applicant.storage;

import com.primehr.config.PrimeHrProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.io.InputStream;import java.net.URI;

@Component @ConditionalOnProperty(name="primehr.storage.provider",havingValue="s3")
public class S3DocumentStorage implements DocumentStorage {
    private final S3Client client; private final String bucket;
    public S3DocumentStorage(PrimeHrProperties p){bucket=p.storage().s3Bucket();if(!p.storage().enabled()||bucket.isBlank())throw new IllegalStateException("A durable S3 bucket is required when S3 storage is selected");var b=S3Client.builder().region(Region.of(p.storage().s3Region()));if(!p.storage().s3Endpoint().isBlank())b.endpointOverride(URI.create(p.storage().s3Endpoint())).forcePathStyle(true);client=b.build();}
    public String provider(){return "s3";}
    public void put(String key,byte[] content,String media){client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).contentType(media).build(),RequestBody.fromBytes(content));}
    public InputStream get(String key){return client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());}
    public void delete(String key){client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());}
}
