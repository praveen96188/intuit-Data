package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 2/10/11
 * Time: 10:47 AM
 */
public class SAPOfferingServiceChargePrice {
    private String serviceChargeTypeCode;
    private double price;
    private double unitPrice;
    private String displayName;
    private double chargedPrice;
    private boolean checked=true;
    private String memo;

    public String getServiceChargeTypeCode() {
        return serviceChargeTypeCode;
    }

    public void setServiceChargeTypeCode(String serviceChargeTypeCode) {
        this.serviceChargeTypeCode = serviceChargeTypeCode;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public double getChargedPrice() {
        return chargedPrice;
    }

    public void setChargedPrice(double chargedPrice) {
        this.chargedPrice = chargedPrice;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double pUnitPrice) {
        unitPrice = pUnitPrice;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String pMemo) {
        memo = pMemo;
    }
}
