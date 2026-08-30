package com.primehr.rsp.report;

import com.primehr.rsp.api.RspPublicationDtos.PublicationResponse;

public interface VacancyNoticeReportService {
    byte[] generate(String agencyId, PublicationResponse publication);
}
