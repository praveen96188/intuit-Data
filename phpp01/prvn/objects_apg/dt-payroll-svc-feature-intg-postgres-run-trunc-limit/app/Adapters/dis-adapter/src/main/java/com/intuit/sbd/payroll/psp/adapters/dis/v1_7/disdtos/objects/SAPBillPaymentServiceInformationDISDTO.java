package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/SAPBillPaymentServiceInformationDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPBillPaymentServiceInformation", propOrder = {"consecutiveLimitVoilationCount","totalLimitVoilationCount"})
public class SAPBillPaymentServiceInformationDISDTO {
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
