package com.primehr.rewardsrecognition.application;

import com.primehr.rewardsrecognition.api.RrFoundationDtos.*;

import java.time.LocalDate;
import java.util.List;

public interface RrFoundationService {
    List<PolicyResponse> policies(String agencyId);
    PolicyResponse policy(String agencyId, String id);
    PolicyResponse effectivePolicy(String agencyId, LocalDate on);
    PolicyResponse createPolicy(String agencyId, PolicyInput input, String correlationId);
    PolicyResponse updatePolicy(String agencyId, String id, PolicyInput input, String correlationId);
    PolicyResponse revisePolicy(String agencyId, String id, DefinitionTransition input, String correlationId);
    PolicyResponse publishPolicy(String agencyId, String id, PublishTransition input, String correlationId);
    PolicyResponse retirePolicy(String agencyId, String id, DefinitionTransition input, String correlationId);

    List<CategoryResponse> categories(String agencyId);
    CategoryResponse category(String agencyId, String id);
    CategoryResponse createCategory(String agencyId, CategoryInput input, String correlationId);
    CategoryResponse updateCategory(String agencyId, String id, CategoryInput input, String correlationId);
    CategoryResponse replaceCriteria(String agencyId, String id, CriteriaInput input, String correlationId);
    CategoryResponse reviseCategory(String agencyId, String id, DefinitionTransition input, String correlationId);
    CategoryResponse publishCategory(String agencyId, String id, PublishTransition input, String correlationId);
    CategoryResponse retireCategory(String agencyId, String id, DefinitionTransition input, String correlationId);

    List<CommitteeCharterResponse> charters(String agencyId);
    CommitteeCharterResponse charter(String agencyId, String id);
    CommitteeCharterResponse createCharter(String agencyId, CommitteeCharterInput input, String correlationId);
    CommitteeCharterResponse updateCharter(String agencyId, String id, CommitteeCharterInput input, String correlationId);
    CommitteeCharterResponse replaceRoster(String agencyId, String id, CommitteeRosterInput input, String correlationId);
    CommitteeCharterResponse reviseCharter(String agencyId, String id, DefinitionTransition input, String correlationId);
    CommitteeCharterResponse publishCharter(String agencyId, String id, PublishTransition input, String correlationId);
    CommitteeCharterResponse retireCharter(String agencyId, String id, DefinitionTransition input, String correlationId);
}
