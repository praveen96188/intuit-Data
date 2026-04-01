package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 6, 2010
 * Time: 8:56:48 AM
 */
@XmlRootElement()
@XmlType(name = "LookupResponse")
public class TESLookupResponseWSDTO {
    private Boolean mNotExpire;
    private String mCustomerNumber;
    private Boolean mExempt;
    private String mJurisdiction;
    private XMLGregorianCalendar mExpirationDate;

    @XmlElement(name = "DoesNotExpire", required = false)
    public Boolean getNotExpire() {
        return mNotExpire;
    }

    public void setNotExpire(Boolean pNotExpire) {
        mNotExpire = pNotExpire;
    }

    @XmlElement(name = "CustomerNumber", required = true)
    public String getCustomerNumber() {
        return mCustomerNumber;
    }

    public void setCustomerNumber(String pCustomerNumber) {
        mCustomerNumber = pCustomerNumber;
    }

    @XmlElement(name = "Exempt", required = true)
    public Boolean getExempt() {
        return mExempt;
    }

    public void setExempt(Boolean pExempt) {
        mExempt = pExempt;
    }

    @XmlElement(name = "Jurisdiction", required = false)
    public String getJurisdiction() {
        return mJurisdiction;
    }

    public void setJurisdiction(String pJurisdiction) {
        mJurisdiction = pJurisdiction;
    }

    @XmlElement(name = "ExpirationDate", required = false)
    public XMLGregorianCalendar getExpirationDate() {
        return mExpirationDate;
    }

    public void setExpirationDate(XMLGregorianCalendar pExpirationDate) {
        mExpirationDate = pExpirationDate;
    }
}
