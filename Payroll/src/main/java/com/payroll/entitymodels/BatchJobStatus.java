package com.payroll.entitymodels;

public enum BatchJobStatus {
    PENDING,        // job created, data fetch starting
    FETCHING_DATA,  // loading bulk data from other services
    COMPUTING,      // parallel employee computation in progress
    SAVING,         // bulk-saving results to DB
    DONE,           // all employees processed successfully
    FAILED          // fatal error — check errorDetail
}
