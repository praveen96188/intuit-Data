package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 11, 2010
 * Time: 3:29:54 PM
 */
public class SAPDirectDepositServiceInformation {
    private long consecutiveLimitVoilationCount;
    private int totalLimitVoilationCount;
    private String underwritingPlatform;

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

    public String getUnderwritingPlatform() {
        return underwritingPlatform;
    }

    public void setUnderwritingPlatform(String underwritingPlatform) {
        this.underwritingPlatform = underwritingPlatform;
    }
}
