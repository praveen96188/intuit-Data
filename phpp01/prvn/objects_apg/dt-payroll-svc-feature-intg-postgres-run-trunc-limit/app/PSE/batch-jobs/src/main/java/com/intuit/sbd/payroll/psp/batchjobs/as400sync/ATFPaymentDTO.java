package com.intuit.sbd.payroll.psp.batchjobs.as400sync;

import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;

import java.math.BigDecimal;

public class ATFPaymentDTO {
    private DateDTO dueDate;
    private BigDecimal amountDue;
    private String regionCd;
    private String taxTypeCd;

    public DateDTO getDueDate() {
        return dueDate;
    }

    public void setDueDate(DateDTO dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(BigDecimal amountDue) {
        this.amountDue = amountDue;
    }

    public String getRegionCd() {
        return regionCd;
    }

    public void setRegionCd(String regionCd) {
        this.regionCd = regionCd;
    }

    public String getTaxTypeCd() {
        return taxTypeCd;
    }

    public void setTaxTypeCd(String taxTypeCd) {
        this.taxTypeCd = taxTypeCd;
    }
}
