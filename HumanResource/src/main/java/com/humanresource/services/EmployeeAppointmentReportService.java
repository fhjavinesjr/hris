package com.humanresource.services;

import java.io.OutputStream;

public interface EmployeeAppointmentReportService {

    void generatePersonnelActionReport(Long employeeAppointmentId, OutputStream outputStream) throws Exception;
}
