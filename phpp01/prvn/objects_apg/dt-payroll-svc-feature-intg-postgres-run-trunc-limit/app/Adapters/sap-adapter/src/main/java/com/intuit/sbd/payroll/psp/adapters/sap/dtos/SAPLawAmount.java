package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.SAPTranslator;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: dweinberg
 * Date: 1/31/12
 * Time: 1:16 PM
 */
public class SAPLawAmount {
    private String law;
    private String lawId;
    private SpcfMoney amountMoney;

    public String getLaw() {
        return law;
    }

    public void setLaw(String law) {
        this.law = law;
    }

    public double getAmount() {
        return (amountMoney == null ? 0 : SAPTranslator.getDoubleFromSpcfMoney(amountMoney));
    }

    public void setAmount(double amount) {
        this.amountMoney = SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(amount);
    }

    public void addAmount(SpcfDecimal amount) {
        amountMoney = new SpcfMoney(amountMoney.add(amount));
    }

    public SAPLawAmount(String law, String lawId) {
        this.law = law;
        this.lawId = lawId;
        this.amountMoney = SpcfMoney.ZERO;
    }

    public String getLawId() {
        return lawId;
    }

    public void setLawId(String lawId) {
        this.lawId = lawId;
    }

    public SAPLawAmount() {
    }
}
