package com.primehr.rsp.applicant.application;

import com.primehr.rsp.applicant.api.ApplicantDtos.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;import java.util.List;

public interface ApplicantFoundationService {
    Notice currentNotice(); Session register(Register request,String ip,String userAgent); Session login(Login request);
    Account me(String applicantId); Account saveAccount(String applicantId,UpdateAccount request); Profile profile(String applicantId); Profile saveProfile(String applicantId,SaveProfile request);
    void acceptCurrentNotice(String applicantId,String ip,String userAgent); List<Document> documents(String applicantId);
    Document upload(String applicantId,String type,String classification,MultipartFile file,String replacesId);
    DocumentContent download(String applicantId,String documentId); void deactivateDocument(String applicantId,String documentId,long version);
    List<PublicVacancy> publicVacancies(); PublicVacancy publicVacancy(String id);
    record DocumentContent(String filename,String mediaType,long size,InputStream stream){}
}
