package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 30, 2010
 * Time: 2:10:58 PM
 */
@XmlType(name = "EntitlementUnit")
public class EntitlementUnitMessageWSDTO {
    private String mEin;
    private String mStatus;

    @XmlElement(name = "Ein", required = false)
    public String getEin() {
        return mEin;
    }

    public void setEin(String pEin) {
        mEin = pEin;
    }

    @XmlElement(name = "Status", required = false)
    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String pStatus) {
        mStatus = pStatus;
    }
}
