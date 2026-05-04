package com.humanresource.services;

import com.humanresource.dtos.LeaveMonetizationDTO;

import java.util.List;

public interface LeaveMonetizationService {

    /** File a new leave monetization request. Fetches current SL/VL balance and stores it as a snapshot. */
    LeaveMonetizationDTO create(LeaveMonetizationDTO dto) throws Exception;

    /** All records, newest first. */
    List<LeaveMonetizationDTO> getAll() throws Exception;

    /** All records for a specific employee, newest first. */
    List<LeaveMonetizationDTO> getAllByEmployeeId(Long employeeId) throws Exception;

    /** All records with approvalStatus = "Pending" — for HR approval screen. */
    List<LeaveMonetizationDTO> getPending() throws Exception;

    /** Level 1: Recommending authority approves the request. */
    LeaveMonetizationDTO recommend(Long leaveMonetizationId, Long recommendedById, String remarks) throws Exception;

    /** Level 1: Recommending authority disapproves the request. */
    LeaveMonetizationDTO disapproveRecommendation(Long leaveMonetizationId, String remarks) throws Exception;

    /**
     * Level 2: Final authority approves the request.
     * Enforces CSC MC No. 41 s. 1998 rules:
     * - Total days must be >= 10
     * - VL balance after deduction must remain >= 5 days
     * - SL balance after deduction must remain >= 5 days (when SL is being deducted)
     * Records slBalanceAfter, vlBalanceAfter, and sets approvedAt = today.
     */
    LeaveMonetizationDTO approve(Long leaveMonetizationId, Long approvedById, String remarks) throws Exception;

    /** Level 2: Final authority disapproves the request. */
    LeaveMonetizationDTO disapprove(Long leaveMonetizationId, String remarks) throws Exception;

    /** Delete a record — only allowed when approvalStatus is "Pending". */
    Boolean delete(Long leaveMonetizationId) throws Exception;

    /** Update editable fields of a pending leave monetization request. */
    LeaveMonetizationDTO update(Long leaveMonetizationId, LeaveMonetizationDTO dto) throws Exception;
}
