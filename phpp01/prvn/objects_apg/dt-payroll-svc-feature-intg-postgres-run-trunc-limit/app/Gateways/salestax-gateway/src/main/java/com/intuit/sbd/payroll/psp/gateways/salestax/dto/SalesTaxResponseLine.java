package com.intuit.sbd.payroll.psp.gateways.salestax.dto;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 3, 2008
 * Time: 12:14:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class SalesTaxResponseLine {
    private String SKU;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }
}
