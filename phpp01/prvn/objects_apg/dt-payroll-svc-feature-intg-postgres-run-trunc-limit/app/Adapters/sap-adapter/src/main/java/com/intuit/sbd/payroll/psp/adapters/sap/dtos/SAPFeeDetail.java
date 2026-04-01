package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 29, 2008
 * Time: 4:33:04 PM
 */
public class SAPFeeDetail {
    private boolean isPayrollFee;
    private String feeName;
    private double units;
    private double unitPrice;
    private double totalPrice;
    private double currentUnitPrice;

    public boolean getIsPayrollFee() {
        return isPayrollFee;
    }

    public void setIsPayrollFee(boolean payrollFee) {
        isPayrollFee = payrollFee;
    }

    public String getFeeName() {
        return feeName;
    }

    public void setFeeName(String feeName) {
        this.feeName = feeName;
    }

    public double getUnits() {
        return units;
    }

    public void setUnits(double units) {
        this.units = units;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getCurrentUnitPrice() {
        return currentUnitPrice;
    }

    public void setCurrentUnitPrice(double currentUnitPrice) {
        this.currentUnitPrice = currentUnitPrice;
    }
}
