package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AgencyRulesAgencyDISDTO")
public class AgencyRulesAgencyDISDTO {
    @XmlElement(name = "AgencyId")
    private String agencyId;

    @XmlElement(name = "UIString")
    private String uiString;

    @XmlElement(name = "AgencyAbbrev")
    private String agencyAbbrev;

    @XmlElement(name = "PaymentTemplates")
    List<AgencyRulesPaymentTemplateDISDTO> paymentTemplates;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "JurisdictionId")
    private String jurisdictionId;

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public String getUiString() {
        return uiString;
    }

    public void setUiString(String uiString) {
        this.uiString = uiString;
    }

    public String getAgencyAbbrev() {
        return agencyAbbrev;
    }

    public void setAgencyAbbrev(String agencyAbbrev) {
        this.agencyAbbrev = agencyAbbrev;
    }

    public List<AgencyRulesPaymentTemplateDISDTO> getPaymentTemplates() {
        return paymentTemplates;
    }

    public void setPaymentTemplates(List<AgencyRulesPaymentTemplateDISDTO> paymentTemplates) {
        this.paymentTemplates = paymentTemplates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }
}
