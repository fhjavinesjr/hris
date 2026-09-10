package com.primehr.performancemanagement.application;
import com.primehr.performancemanagement.api.PerformanceRatingScaleDtos.*;
import java.util.List;
public interface PerformanceRatingScaleService{
 List<ScaleResponse> list(String agencyId); ScaleResponse get(String agencyId,String versionId);
 ScaleResponse create(String agencyId,ScaleInput request,String correlationId);
 ScaleResponse update(String agencyId,String versionId,ScaleInput request,String correlationId);
 ScaleResponse bands(String agencyId,String versionId,BandsInput request,String correlationId);
 ScaleResponse revision(String agencyId,String versionId,Transition request,String correlationId);
 ScaleResponse publish(String agencyId,String versionId,PublishTransition request,String correlationId);
 ScaleResponse retire(String agencyId,String versionId,Transition request,String correlationId);
 PreviewResponse preview(String agencyId,String versionId,PreviewInput request);
}
