package com.intuit.sbd.payroll.psp.gateways.salestax.dto;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 3, 2008
 * Time: 12:06:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class SalesTaxRequestLine {
    private String SKU;
    private int quantity;
    private BigDecimal amount;

    //We require product class to make call to GST service
    private String productClassForSKU;

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductClassForSKU() {
        return productClassForSKU;
    }

    public void setProductClassForSKU(String productClassForSKU) {
        this.productClassForSKU = productClassForSKU;
    }
}
