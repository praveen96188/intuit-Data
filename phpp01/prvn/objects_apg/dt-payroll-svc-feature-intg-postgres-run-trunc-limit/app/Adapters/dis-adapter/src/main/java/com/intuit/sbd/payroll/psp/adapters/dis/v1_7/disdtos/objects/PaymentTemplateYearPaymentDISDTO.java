package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/PaymentTemplateYearPaymentDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PaymentTemplateYearPayment")
public class PaymentTemplateYearPaymentDISDTO {
    @XmlElement(name = "TemplateQuarterPayment")
    private List<PaymentTemplateQuarterPaymentDISDTO> templateQuarterPayments;

    public List<PaymentTemplateQuarterPaymentDISDTO> getTemplateQuarterPayments() {
        return templateQuarterPayments;
    }

    public void setTemplateQuarterPayments(List<PaymentTemplateQuarterPaymentDISDTO> templateQuarterPayments) {
        this.templateQuarterPayments = templateQuarterPayments;
    }

    @XmlElement(name = "PendingPaymentsTotal")
    private double pendingPaymentsTotal;

    public double getPendingPaymentsTotal() {
        return pendingPaymentsTotal;
    }

    public void setPendingPaymentsTotal(double pendingPaymentsTotal) {
        this.pendingPaymentsTotal = pendingPaymentsTotal;
    }

    @XmlElement(name = "PaymentsMadeTotal")
    private double paymentsMadeTotal;

    public double getPaymentsMadeTotal() {
        return paymentsMadeTotal;
    }

    public void setPaymentsMadeTotal(double paymentsMadeTotal) {
        this.paymentsMadeTotal = paymentsMadeTotal;
    }

    @XmlElement(name = "YearPaymentsTotal")
    private double yearPaymentsTotal;

    public double getYearPaymentsTotal() {
        return yearPaymentsTotal;
    }

    public void setYearPaymentsTotal(double yearPaymentsTotal) {
        this.yearPaymentsTotal = yearPaymentsTotal;
    }

    @XmlElement(name = "PaymentTemplateCd")
    private String paymentTemplateCd;

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
    }

    @XmlElement(name = "TaxYear")
    private String taxYear;

    public String getTaxYear() {
        return taxYear;
    }

    public void setTaxYear(String taxYear) {
        this.taxYear = taxYear;
    }

    @XmlElement(name = "LawRates")
    List<LawRateDISDTO> lawRates;

    public List<LawRateDISDTO> getLawRates() {
        return lawRates;
    }

    public void setLawRates(List<LawRateDISDTO> lawRates) {
        this.lawRates = lawRates;
    }

    @XmlElement(name = "agencyTaxPayerId")
    String agencyTaxPayerId;

    public String getAgencyTaxPayerId() {
        return agencyTaxPayerId;
    }

    public void setAgencyTaxPayerId(String agencyTaxPayerId) {
        this.agencyTaxPayerId = agencyTaxPayerId;
    }
}
