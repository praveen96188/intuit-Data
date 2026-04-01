package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/AgencyRulesPaymentTemplateDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AgencyRulesPaymentTemplateDISDTO")
public class AgencyRulesPaymentTemplateDISDTO {

    @XmlElement(name = "PaymentTemplateId")
    private String paymentTemplateId;

    @XmlElement(name = "PaymentTemplateAbbrev")
    private String paymentTemplateAbbrev;

    @XmlElement(name = "Description")
    private String description;

    @XmlElement(name = "PaymentFrequencies")
    private List<AgencyRulesPaymentFrequencyDISDTO> paymentFrequencies;

    @XmlElement(name = "Laws")
    private List<AgencyRulesLawDISDTO> laws;

    @XmlElement(name = "AgencyIDFormats")
    private List<AgencyRulesAgencyIDFormatDISDTO> agencyIDFormats;

    @XmlElement(name = "DefaultPaymentFrequency")
    private AgencyRulesPaymentFrequencyDISDTO defaultPaymentFrequency;

    @XmlElement(name = "UsesFrequencyOf")
    private String usesFrequencyOf;

    public String getPaymentTemplateId() {
        return paymentTemplateId;
    }

    public void setPaymentTemplateId(String paymentTemplateId) {
        this.paymentTemplateId = paymentTemplateId;
    }

    public String getPaymentTemplateAbbrev() {
        return paymentTemplateAbbrev;
    }

    public void setPaymentTemplateAbbrev(String paymentTemplateAbbrev) {
        this.paymentTemplateAbbrev = paymentTemplateAbbrev;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AgencyRulesPaymentFrequencyDISDTO> getPaymentFrequencies() {
        return paymentFrequencies;
    }

    public void setPaymentFrequencies(List<AgencyRulesPaymentFrequencyDISDTO> paymentFrequencies) {
        this.paymentFrequencies = paymentFrequencies;
    }

    public List<AgencyRulesLawDISDTO> getLaws() {
        return laws;
    }

    public void setLaws(List<AgencyRulesLawDISDTO> laws) {
        this.laws = laws;
    }

    public List<AgencyRulesAgencyIDFormatDISDTO> getAgencyIDFormats() {
        return agencyIDFormats;
    }

    public void setAgencyIDFormats(List<AgencyRulesAgencyIDFormatDISDTO> agencyIDFormats) {
        this.agencyIDFormats = agencyIDFormats;
    }

    public AgencyRulesPaymentFrequencyDISDTO getDefaultPaymentFrequency() {
        return defaultPaymentFrequency;
    }

    public void setDefaultPaymentFrequency(AgencyRulesPaymentFrequencyDISDTO defaultPaymentFrequency) {
        this.defaultPaymentFrequency = defaultPaymentFrequency;
    }

    public String getUsesFrequencyOf() {
        return usesFrequencyOf;
    }

    public void setUsesFrequencyOf(String usesFrequencyOf) {
        this.usesFrequencyOf = usesFrequencyOf;
    }
}