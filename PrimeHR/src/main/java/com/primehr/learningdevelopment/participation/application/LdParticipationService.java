package com.primehr.learningdevelopment.participation.application;

import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.learningdevelopment.participation.api.LdParticipationDtos.*;
import java.util.List;

public interface LdParticipationService {
    List<NominationResponse> nominations(String agency,String actor,PermissionDataScope scope);
    NominationResponse nomination(String agency,String id,String actor,PermissionDataScope scope);
    NominationResponse create(String agency,NominationCreate input,String token,String actor,PermissionDataScope scope,String correlation);
    NominationResponse update(String agency,String id,NominationUpdate input,String actor,PermissionDataScope scope,String correlation);
    NominationResponse submit(String agency,String id,Transition input,String token,String actor,PermissionDataScope scope,String correlation);
    NominationResponse recommend(String agency,String id,Transition input,String token,String actor,String correlation);
    NominationResponse returnForChanges(String agency,String id,Transition input,String token,String actor,String correlation);
    NominationResponse approve(String agency,String id,Transition input,String token,String actor,String correlation);
    NominationResponse cancel(String agency,String id,Transition input,String actor,PermissionDataScope scope,String correlation);
    List<EnrollmentResponse> enrollments(String agency,String actor,PermissionDataScope scope);
    EnrollmentResponse enrollment(String agency,String id,String actor,PermissionDataScope scope);
    EnrollmentResponse confirm(String agency,String id,Transition input,String token,String actor,String correlation);
    EnrollmentResponse decline(String agency,String id,Transition input,String actor,String correlation);
    EnrollmentResponse cancelEnrollment(String agency,String id,Transition input,String actor,PermissionDataScope scope,String correlation);
    EnrollmentResponse substitute(String agency,String id,SubstitutionInput input,String actor,PermissionDataScope scope,String correlation);
    TrainingOrderResponse trainingOrder(String agency,String offeringId,String actor,PermissionDataScope scope);
    TrainingOrderResponse finalizeTrainingOrder(String agency,String offeringId,TrainingOrderInput input,String actor,PermissionDataScope scope,String correlation);
}
