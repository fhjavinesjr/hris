package com.humanresource.impl;

import com.humanresource.entitymodels.LeaveMonetization;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class LeaveProcessMonetizationTest {

    @Test
    void totalsApprovedMonetizationCreditsForCutoffDeduction() {
        LeaveMonetization first = monetization(6.5, 4.0);
        LeaveMonetization second = monetization(3.5, 6.0);

        double[] totals = LeaveProcessServiceImpl.totalMonetizedDays(List.of(first, second));

        assertArrayEquals(new double[]{10.0, 10.0}, totals);
    }

    @Test
    void treatsMissingLeaveTypeAmountsAsZero() {
        LeaveMonetization monetization = monetization(null, 10.0);

        double[] totals = LeaveProcessServiceImpl.totalMonetizedDays(List.of(monetization));

        assertArrayEquals(new double[]{0.0, 10.0}, totals);
    }

    private LeaveMonetization monetization(Double sl, Double vl) {
        LeaveMonetization monetization = new LeaveMonetization();
        monetization.setNoOfDaysSL(sl);
        monetization.setNoOfDaysVL(vl);
        return monetization;
    }
}
