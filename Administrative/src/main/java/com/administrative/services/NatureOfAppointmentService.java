package com.administrative.services;

import com.administrative.dtos.NatureOfAppointmentDTO;

import java.util.List;

public interface NatureOfAppointmentService {

    NatureOfAppointmentDTO createNatureOfAppointment(NatureOfAppointmentDTO natureOfAppointmentDTO) throws Exception;

    List<NatureOfAppointmentDTO> getAllNatureOfAppointment() throws Exception;

    NatureOfAppointmentDTO getNatureOfAppointmentById(Long natureOfAppointmentId) throws Exception;

    NatureOfAppointmentDTO updateNatureOfAppointment(Long natureOfAppointmentId, NatureOfAppointmentDTO natureOfAppointmentDTO) throws Exception;

    Boolean deleteNatureOfAppointment(Long natureOfAppointmentId) throws Exception;

}
