package com.primehr.learning.referral.application;

import com.primehr.learning.referral.api.LdReferralDtos.*;
import com.primehr.learning.referral.domain.LdReferralStatus;
import com.primehr.shared.api.PageResponse;

public interface LdReferralService {
    PageResponse<SummaryResponse> list(String agencyId, String employeeNo, LdReferralStatus status, int page, int size);
    Response get(String agencyId, String referralId);
    Response create(String agencyId, CreateRequest request, String correlationId);
    Response update(String agencyId, String referralId, UpdateRequest request, String correlationId);
    Response addItems(String agencyId, String referralId, AddItemsRequest request, String correlationId);
    Response archiveItem(String agencyId, String referralId, String itemId, ItemTransitionRequest request, String correlationId);
    Response submit(String agencyId, String referralId, TransitionRequest request, String correlationId);
    Response archive(String agencyId, String referralId, TransitionRequest request, String correlationId);
}

