package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Calendar;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/CompanyLawRateDetailDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompanyLawRateDetailDISDTO",propOrder = {"lawName","rate","active"
        ,"effectiveDate","agencyId","exempt","sourceLawId","lawId","sourceLawDescription","modifiedBy","modifiedDate"})
public class CompanyLawRateDetailDISDTO {

    @XmlElement(name = "LawName")
    private String lawName;

    @XmlElement(name = "Rate")
    private Double rate;

    @XmlElement(name = "EffectiveDate")
    private Calendar effectiveDate;

    @XmlElement(name = "AgencyId")
    private String agencyId;

    @XmlElement(name = "Exempt")
    private Boolean exempt;

    @XmlElement(name = "Active")
    private Boolean active;

    @XmlElement(name = "SourceLawId")
    private String sourceLawId;

    @XmlElement(name = "LawId")
    private String lawId;

    @XmlElement(name = "SourceLawDescription")
    private String sourceLawDescription;

    @XmlElement(name = "ModifiedBy")
    private String modifiedBy;

    @XmlElement(name = "ModifiedDate")
    private Calendar modifiedDate;

    public String getLawName() {
        return lawName;
    }

    public void setLawName(String lawName) {
        this.lawName = lawName;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public Boolean getExempt() {
        return exempt;
    }

    public void setExempt(Boolean exempt) {
        this.exempt = exempt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getSourceLawId() {
        return sourceLawId;
    }

    public void setSourceLawId(String sourceLawID) {
        this.sourceLawId = sourceLawID;
    }

    public String getLawId() {
        return lawId;
    }

    public void setLawId(String lawId) {
        this.lawId = lawId;
    }

    public String getSourceLawDescription() {
        return sourceLawDescription;
    }

    public void setSourceLawDescription(String sourceLawDescription) {
        this.sourceLawDescription = sourceLawDescription;
    }

    public Calendar getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Calendar effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Calendar getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Calendar modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
