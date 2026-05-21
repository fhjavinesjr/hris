package com.payroll.entitymodels;

public enum PayrollStatus {
    COMPUTED,   // initial computation done
    LOCKED,     // finalized, no further changes
    CANCELLED   // voided / excluded
}
