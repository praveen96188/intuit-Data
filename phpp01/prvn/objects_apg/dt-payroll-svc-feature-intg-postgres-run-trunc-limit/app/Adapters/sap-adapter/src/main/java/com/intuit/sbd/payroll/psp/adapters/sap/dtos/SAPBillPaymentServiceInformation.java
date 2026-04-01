package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 11, 2010
 * Time: 3:33:36 PM
 */
public class SAPBillPaymentServiceInformation {
    private long consecutiveLimitVoilationCount;
    private int totalLimitVoilationCount;

    public long getConsecutiveLimitVoilationCount() {
        return consecutiveLimitVoilationCount;
    }

    public void setConsecutiveLimitVoilationCount(long pConsecutiveLimitVoilationCount) {
        consecutiveLimitVoilationCount = pConsecutiveLimitVoilationCount;
    }

    public int getTotalLimitVoilationCount() {
        return totalLimitVoilationCount;
    }

    public void setTotalLimitVoilationCount(int pTotalLimitVoilationCount) {
        totalLimitVoilationCount = pTotalLimitVoilationCount;
    }
}
