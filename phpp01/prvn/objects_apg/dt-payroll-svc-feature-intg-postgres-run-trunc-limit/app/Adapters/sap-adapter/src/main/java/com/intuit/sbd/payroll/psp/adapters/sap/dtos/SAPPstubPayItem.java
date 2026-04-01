package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: ihannur
 * Date: 6/26/13
 * Time: 2:01 PM
 */
public class SAPPstubPayItem {
    private String name;
    private String rate;
    private String quantity;
    private Number currentAmount;
    private Number ytdAmount;

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String pRate) {
        rate = pRate;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String pQuantity) {
        quantity = pQuantity;
    }

    public Number getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(Number pCurrentAmount) {
        currentAmount = pCurrentAmount;
    }

    public Number getYtdAmount() {
        return ytdAmount;
    }

    public void setYtdAmount(Number pYtdAmount) {
        ytdAmount = pYtdAmount;
    }
}
