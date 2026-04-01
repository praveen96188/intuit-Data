package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 8, 2010
 * Time: 1:57:40 PM
 */
@XmlRootElement()
@XmlType(name = "EntitlementMessage")
public class EntitlementMessageResponseWSDTO {
    private String mLicenseNumber;
    private String mEOC;
    private String mOrderNumber;
    private String mStatus;
    private String mMessage;

    @XmlElement(name = "LicenseNumber")
    public String getLicenseNumber() {
        return mLicenseNumber;
    }

    public void setLicenseNumber(String pLicenseNumber) {
        mLicenseNumber = pLicenseNumber;
    }

    @XmlElement(name = "EOC")
    public String getEOC() {
        return mEOC;
    }

    public void setEOC(String pEOC) {
        mEOC = pEOC;
    }

    @XmlElement(name = "OrderNumber")
    public String getOrderNumber() {
        return mOrderNumber;
    }

    public void setOrderNumber(String pOrderNumber) {
        mOrderNumber = pOrderNumber;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String pStatus) {
        mStatus = pStatus;
    }

    @XmlElement(name = "Message")
    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String pMessage) {
        mMessage = pMessage;
    }
}
