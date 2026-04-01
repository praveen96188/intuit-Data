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
@XmlType(name = "CompanyLawItemDISDTO",propOrder = {"lawName","lawId"
        ,"sourceLawDescription","currentLawRate","futureLawRate"})
public class CompanyLawItemDISDTO {

    @XmlElement(name = "LawName")
    private String lawName;

    @XmlElement(name = "LawId")
    private String lawId;

    @XmlElement(name = "SourceLawDescription")
    private String sourceLawDescription;

    @XmlElement(name = "CurrentLawRate")
    private CompanyLawRateDetailDISDTO currentLawRate;

    @XmlElement(name = "FutureLawRate")
    private CompanyLawRateDetailDISDTO futureLawRate;

    public String getLawName() {
        return lawName;
    }

    public void setLawName(String lawName) {
        this.lawName = lawName;
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

    public CompanyLawRateDetailDISDTO getCurrentLawRate() {
        return currentLawRate;
    }

    public void setCurrentLawRate(CompanyLawRateDetailDISDTO currentLawRate) {
        this.currentLawRate = currentLawRate;
    }

    public CompanyLawRateDetailDISDTO getFutureLawRate() {
        return futureLawRate;
    }

    public void setFutureLawRate(CompanyLawRateDetailDISDTO futureLawRate) {
        this.futureLawRate = futureLawRate;
    }
}
