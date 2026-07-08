package com.humanresource.services;

import java.io.OutputStream;

public interface LeaveFormReportService {

    void generateLeaveForm(Long leaveApplicationId, OutputStream out) throws Exception;

    void generateLeaveFormForMonetization(Long leaveMonetizationId, OutputStream out) throws Exception;

    void generateLeaveCard(Long employeeId, Integer year, OutputStream out) throws Exception;
}