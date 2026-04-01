package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPDirectDepositServiceInformation", propOrder = {"consecutiveLimitVoilationCount","totalLimitVoilationCount"})
public class SAPDirectDepositServiceInformationDISDTO {
    @XmlElement(name = "ConsecutiveLimitVoilationCount")
    private long consecutiveLimitVoilationCount;

    public long getConsecutiveLimitVoilationCount() {
        return consecutiveLimitVoilationCount;
    }

    public void setConsecutiveLimitVoilationCount(long pConsecutiveLimitVoilationCount) {
        consecutiveLimitVoilationCount = pConsecutiveLimitVoilationCount;
    }

    @XmlElement(name = "TotalLimitVoilationCount")
    private int totalLimitVoilationCount;

    public int getTotalLimitVoilationCount() {
        return totalLimitVoilationCount;
    }

    public void setTotalLimitVoilationCount(int pTotalLimitVoilationCount) {
        totalLimitVoilationCount = pTotalLimitVoilationCount;
    }

}
