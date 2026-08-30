package com.primehr.rsp.application;

import com.primehr.rsp.api.RspPublicationDtos.CreatePublication;
import com.primehr.rsp.api.RspPublicationDtos.PublicationResponse;
import com.primehr.rsp.api.RspPublicationDtos.PublicationTransition;
import com.primehr.rsp.api.RspPublicationDtos.UpdatePublication;
import com.primehr.shared.api.PageResponse;

public interface RspPublicationService {
    PageResponse<PublicationResponse> list(String agencyId, int page, int size);
    PublicationResponse get(String agencyId, String id);
    PublicationResponse create(String agencyId, CreatePublication request,
                               String token, String correlationId);
    PublicationResponse update(String agencyId, String id, UpdatePublication request,
                               String token, String correlationId);
    PublicationResponse submit(String agencyId, String id, PublicationTransition request,
                               String token, String correlationId);
    PublicationResponse returnSubmission(String agencyId, String id,
                                          PublicationTransition request, String correlationId);
    PublicationResponse approve(String agencyId, String id, PublicationTransition request,
                                String token, boolean administrator, String correlationId);
    PublicationResponse publish(String agencyId, String id, PublicationTransition request,
                                String token, boolean administrator, String correlationId);
    PublicationResponse cancel(String agencyId, String id, PublicationTransition request,
                               String correlationId);
    PublicationResponse close(String agencyId, String id, PublicationTransition request,
                              String correlationId);
}
