package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 30, 2009
 * Time: 8:49:50 AM
 */
public class SAPSuspectPaycheck {
    private String employeeName;
    private String trigger;
    private double amount;
    private String paycheckId;

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaycheckId() {
        return paycheckId;
    }

    public void setPaycheckId(String paycheckId) {
        this.paycheckId = paycheckId;
    }
}
