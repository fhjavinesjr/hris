package com.primehr.rsp.applicant.storage;

import com.primehr.config.PrimeHrProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.io.*;import java.nio.file.*;

@Component @ConditionalOnProperty(name="primehr.storage.provider",havingValue="local",matchIfMissing=true)
public class LocalDocumentStorage implements DocumentStorage {
    private final Path root;
    public LocalDocumentStorage(PrimeHrProperties properties){if(properties.storage().enabled()&&properties.storage().localRoot().isBlank())throw new IllegalStateException("PRIMEHR_DOCUMENT_ROOT is required when local document storage is enabled");root=properties.storage().localRoot().isBlank()?Path.of(System.getProperty("java.io.tmpdir"),"primehr-disabled"):Path.of(properties.storage().localRoot()).toAbsolutePath().normalize();}
    public String provider(){return "local";}
    public void put(String key,byte[] content,String media){Path target=resolve(key);try{Files.createDirectories(target.getParent());Files.write(target,content,StandardOpenOption.CREATE_NEW);}catch(IOException e){throw new IllegalStateException("Document could not be stored",e);}}
    public InputStream get(String key){try{return Files.newInputStream(resolve(key));}catch(IOException e){throw new IllegalStateException("Document could not be read",e);}}
    public void delete(String key){try{Files.deleteIfExists(resolve(key));}catch(IOException e){throw new IllegalStateException("Document could not be removed",e);}}
    private Path resolve(String key){if(key==null||!key.matches("[A-Za-z0-9/_-]+"))throw new IllegalArgumentException("Invalid storage object key");Path target=root.resolve(key).normalize();if(!target.startsWith(root))throw new IllegalArgumentException("Storage path escapes configured root");return target;}
}
