package com.humanresource.integration.primehr;

import java.util.Optional;

interface AppointmentHandoffReceiptStore {
    Optional<AppointmentHandoffReceiptRecord> findByHandoff(String agencyId, String handoffId);

    Optional<AppointmentHandoffReceiptRecord> findBySelection(String agencyId, String selectionId);

    AppointmentHandoffReceiptRecord insert(AppointmentHandoffReceiptRecord receipt);
}
