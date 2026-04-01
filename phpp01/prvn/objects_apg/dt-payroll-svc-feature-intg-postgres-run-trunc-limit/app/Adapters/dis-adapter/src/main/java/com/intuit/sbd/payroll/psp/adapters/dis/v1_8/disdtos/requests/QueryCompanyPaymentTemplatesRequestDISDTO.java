package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;

import javax.xml.bind.annotation.*;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "QueryTaxLawsRequest")
@XmlType()
public class QueryCompanyPaymentTemplatesRequestDISDTO {
    @XmlElement(name = "SourceSystem", nillable = false, required = true)
    private SourceSystemEnum sourceSystem;

    @XmlElement(name = "SourceCompanyId", nillable = false, required = true)
    private String sourceCompanyId;

    @XmlElement(name = "PaymentTemplateCd", required = false)
    private String paymentTemplateCd;

    public SourceSystemEnum getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(SourceSystemEnum sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String sourceCompanyId) {
        this.sourceCompanyId = sourceCompanyId;
    }

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
    }
}
