package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 21, 2010
 * Time: 12:47:51 PM
 */
@XmlType(name = "EntitlementUnit")
public class EntitlementUnitWSDTO {
    private String mLastValidationDate;
    private String mEntitlementUnitStatus;
    private String mServiceKey;
    private String mExtensionKey;
    private String mFEIN;

    @XmlElement(name = "EntitlementUnitStatus", required = false)
    public String getEntitlementUnitStatus() {
        return mEntitlementUnitStatus;
    }

    public void setEntitlementUnitStatus(String pEntitlementUnitStatus) {
        mEntitlementUnitStatus = pEntitlementUnitStatus;
    }

    @XmlElement(name = "ServiceKey", required = false)
    public String getServiceKey() {
        return mServiceKey;
    }

    public void setServiceKey(String pServiceKey) {
        mServiceKey = pServiceKey;
    }

    @XmlElement(name = "ExtensionKey", required = false)
    public String getExtensionKey() {
        return mExtensionKey;
    }

    public void setExtensionKey(String pExtensionKey) {
        mExtensionKey = pExtensionKey;
    }

    @XmlElement(name = "Ein", required = false)
    public String getFEIN() {
        return mFEIN;
    }

    public void setFEIN(String pFEIN) {
        mFEIN = pFEIN;
    }

    public String getLastValidationDate() {
        return mLastValidationDate;
    }

    public void setLastValidationDate(String pLastValidationDate) {
        mLastValidationDate = pLastValidationDate;
    }
}
