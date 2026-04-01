package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * User: dweinberg
 * Date: 8/21/12
 * Time: 10:20 AM
 */
public class CompanyFilingAmountDTO {
    private SpcfUniqueId id; //id is null if new and the id of the existing element (no natural key) otherwise
    private String name;
    private DateDTO effectiveDate;
    private double amount;

    public SpcfUniqueId getId() {
        return id;
    }

    public void setId(SpcfUniqueId pId) {
        id = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public DateDTO getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(DateDTO pEffectiveDate) {
        effectiveDate = pEffectiveDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double pAmount) {
        amount = pAmount;
    }
}
