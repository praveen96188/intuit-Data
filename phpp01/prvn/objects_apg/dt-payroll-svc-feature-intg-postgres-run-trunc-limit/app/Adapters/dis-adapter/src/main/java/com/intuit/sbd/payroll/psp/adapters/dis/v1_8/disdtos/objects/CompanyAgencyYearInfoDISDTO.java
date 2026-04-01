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
@XmlType(name = "CompanyAgencyYearInfo")
public class CompanyAgencyYearInfoDISDTO {

    @XmlElement(name = "AgencyId")
    private String agencyId;

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    @XmlElement(name = "AgencyName")
    private String agencyName;

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    @XmlElement(name = "PaymentTemplateYearPayment")
    List<PaymentTemplateYearPaymentDISDTO> paymentTemplateYearPayment;

    public List<PaymentTemplateYearPaymentDISDTO> getPaymentTemplateYearPayment() {
        return paymentTemplateYearPayment;
    }

    public void setPaymentTemplateYearPayment(List<PaymentTemplateYearPaymentDISDTO> paymentTemplateYearPayment) {
        this.paymentTemplateYearPayment = paymentTemplateYearPayment;
    }
}
