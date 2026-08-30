package com.administrative.services;

import com.administrative.dtos.QualificationStandardDtos.*;
import java.time.LocalDate;
import java.util.List;

public interface QualificationStandardService {
    List<Response> list(Long jobPositionId); Response get(Long id); Response create(Save request,String actor);
    Response update(Long id,Save request,String actor); Response archive(Long id,Transition request,String actor);
    Response successor(Long id,Transition request,String actor); Response publish(Long id,Transition request,String actor);
    Response effective(Long jobPositionId,LocalDate asOf);
}
