package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import com.intuit.sbd.payroll.psp.domain.Compensation;
import com.intuit.sbd.payroll.psp.domain.PaycheckSplit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jeff Jones
 */
public class RSPaycheck {

    private String name;
    private BigDecimal grossAmount;
    private List<RSPaycheckLineItem> compensations;
    private List<RSPaycheckLineItem> preTaxDeductions;
    private List<RSPaycheckLineItem> Taxes;
    private List<RSPaycheckLineItem> afterTaxDeductions;
    private List<RSPaycheckLineItem> employerContributions;
    private BigDecimal netAmount;
    private List<RSPaycheckLineItem> accruals;
    private List<PaycheckSplit> directDepositSplits;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public List<RSPaycheckLineItem> getCompensations() {
        if (compensations== null)
            compensations = new ArrayList<RSPaycheckLineItem>();

        return compensations;
    }

    public void setCompensations(List<RSPaycheckLineItem> compensations) {
        this.compensations = compensations;
    }

    public List<RSPaycheckLineItem> getPreTaxDeductions() {
        return preTaxDeductions;
    }

    public void setPreTaxDeductions(List<RSPaycheckLineItem> preTaxDeductions) {
        this.preTaxDeductions = preTaxDeductions;
    }

    public List<RSPaycheckLineItem> getTaxes() {
        return Taxes;
    }

    public void setTaxes(List<RSPaycheckLineItem> taxes) {
        Taxes = taxes;
    }

    public List<RSPaycheckLineItem> getAfterTaxDeductions() {
        return afterTaxDeductions;
    }

    public void setAfterTaxDeductions(List<RSPaycheckLineItem> afterTaxDeductions) {
        this.afterTaxDeductions = afterTaxDeductions;
    }

    public List<RSPaycheckLineItem> getEmployerContributions() {
        return employerContributions;
    }

    public void setEmployerContributions(List<RSPaycheckLineItem> employerContributions) {
        this.employerContributions = employerContributions;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public List<RSPaycheckLineItem> getAccruals() {
        return accruals;
    }

    public void setAccruals(List<RSPaycheckLineItem> accruals) {
        this.accruals = accruals;
    }

    public List<PaycheckSplit> getDirectDepositSplits() {
        return directDepositSplits;
    }

    public void setDirectDepositSplits(List<PaycheckSplit> directDepositSplits) {
        this.directDepositSplits = directDepositSplits;
    }
}
