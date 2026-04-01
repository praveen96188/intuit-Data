package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Date;

/**
 * User: rnorian
 * Date: May 18, 2009
 * Time: 9:48:39 PM
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SAPCompanyLegalInfo {

    @XmlElement(name = "LegalName", required = true)
    protected String legalName;

    @XmlElement(name = "DoingBusinessAs", required = true)
    protected String doingBusinessAs;

    @XmlElement(name = "Address", required = true)
    protected SAPAddress address;

    @XmlElement(name = "EIN", required = true)
    protected String ein;

    @XmlElement(name = "PSID", required = true)
    protected String psid;

    @XmlElement(name = "EffectiveDate", required = true)
    protected Date einEffectiveDate;

    @XmlElement(name = "isOldEinError", required = true)
    protected Boolean isOldEinError;

    @XmlElement(name = "industryType", required = false)
    protected String industryType;



    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getDoingBusinessAs() {
        return doingBusinessAs;
    }

    public void setDoingBusinessAs(String doingBusinessAs) {
        this.doingBusinessAs = doingBusinessAs;
    }

    public SAPAddress getAddress() {
        return address;
    }

    public void setAddress(SAPAddress address) {
        this.address = address;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public Date getEinEffectiveDate() {
        return einEffectiveDate;
    }

    public void setEinEffectiveDate(Date pEinEffectiveDate) {
        einEffectiveDate = pEinEffectiveDate;
    }

    public Boolean getIsOldEinError() {
        return isOldEinError;
    }

    public void setIsOldEinError(Boolean pOldEinError) {
        isOldEinError = pOldEinError;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String pIndustryType) {
        industryType = pIndustryType;
    }
}
