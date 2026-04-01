package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "ewsBankAccountResponse",
        "sourceCode",
        "priceCode",
        "mostCurrentTaxYear",
        "monthlyFee"
})
public class EwsAssistedServiceResponse extends EwsBaseServiceResponse implements Cloneable {

    @XmlElement(name = "SourceCode", required = false)
    protected String sourceCode;

    @XmlElement(name = "PriceCode", required = false)
    protected String priceCode;

    @XmlElement(name = "MostCurrentTaxYear", required = false)
    protected String mostCurrentTaxYear;

    @XmlElement(name = "BankAccountResponse", required = false)
    protected EwsBankAccountResponse ewsBankAccountResponse;

    @XmlElement(name = "MonthlyFee", required = false)
    protected String monthlyFee;

    public EwsAssistedServiceResponse() {
        super();
    }

    public EwsAssistedServiceResponse clone() throws CloneNotSupportedException {
        EwsAssistedServiceResponse clone = (EwsAssistedServiceResponse) super.clone();

        if (ewsBankAccountResponse != null) {
            clone.setEwsBankAccountResponse(ewsBankAccountResponse.clone());
        }

        return clone;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getPriceCode() {
        return priceCode;
    }

    public void setPriceCode(String priceCode) {
        this.priceCode = priceCode;
    }

    public String getMostCurrentTaxYear() {
        return mostCurrentTaxYear;
    }

    public void setMostCurrentTaxYear(String mostCurrentTaxYear) {
        this.mostCurrentTaxYear = mostCurrentTaxYear;
    }

    public EwsBankAccountResponse getEwsBankAccountResponse() {
        return ewsBankAccountResponse;
    }

    public void setEwsBankAccountResponse(EwsBankAccountResponse ewsBankAccountResponse) {
        this.ewsBankAccountResponse = ewsBankAccountResponse;
    }

    public String getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(String monthlyFee) {
        this.monthlyFee = monthlyFee;
    }
}
