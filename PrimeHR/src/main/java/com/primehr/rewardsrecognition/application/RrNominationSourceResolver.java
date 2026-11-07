package com.primehr.rewardsrecognition.application;

import com.primehr.rewardsrecognition.domain.RrNominationSource;
import java.time.Instant;

public interface RrNominationSourceResolver {
    record Request(RrNominationSource.Type type,String id,long version,String fingerprint,Instant changedAt){}
    record Snapshot(RrNominationSource.Type type,String id,long version,String fingerprint,String label,
                    String employeeNo,Long businessUnitId,Instant changedAt){}
    Snapshot resolve(String agencyId,Request request,String actor,String correlationId);
}
