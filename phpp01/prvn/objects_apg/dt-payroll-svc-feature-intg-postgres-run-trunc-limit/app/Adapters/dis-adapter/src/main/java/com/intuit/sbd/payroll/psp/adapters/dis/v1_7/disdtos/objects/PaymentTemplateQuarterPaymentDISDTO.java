package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/PaymentTemplateQuarterPaymentDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PaymentTemplateQuarterPayment")
public class PaymentTemplateQuarterPaymentDISDTO {
    @XmlElement(name = "Quarter")
    private String quarter;

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    @XmlElement(name = "Year")
    private String year;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @XmlElement(name = "PaymentTemplateName")
    private String paymentTemplateName;

    public String getPaymentTemplateName() {
        return paymentTemplateName;
    }

    public void setPaymentTemplateName(String paymentTemplateName) {
        this.paymentTemplateName = paymentTemplateName;
    }

    @XmlElement(name = "PaymentTemplateCd")
    private String paymentTemplateCd;

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
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

    @XmlElement(name = "QuarterPaymentsTotal")
    private double quarterPaymentsTotal;

    public double getQuarterPaymentsTotal() {
        return quarterPaymentsTotal;
    }

    public void setQuarterPaymentsTotal(double quarterPaymentsTotal) {
        this.quarterPaymentsTotal = quarterPaymentsTotal;
    }

    @XmlElement(name = "NotStarted")
    private boolean notStarted;

    public boolean isNotStarted() {
        return notStarted;
    }

    public void setNotStarted(boolean notStarted) {
        this.notStarted = notStarted;
    }
}
