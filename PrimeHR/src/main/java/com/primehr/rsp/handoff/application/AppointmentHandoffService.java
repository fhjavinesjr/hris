package com.primehr.rsp.handoff.application;
import com.primehr.rsp.handoff.api.AppointmentHandoffDtos.*;
public interface AppointmentHandoffService{Response create(String agency,String selectionId,String actor,String correlation);Response deliver(String agency,String id,long version,String actor,String action);Response get(String agency,String id);}
