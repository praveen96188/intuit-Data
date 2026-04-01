package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: dweinberg
 * Date: 11/1/11
 * Time: 12:22 PM
 */
public class SAPDataSyncItems {
    private ArrayList<String> employeeIds;
    private ArrayList<String> payrollItemIds;
    private ArrayList<String> paychecks;
    private ArrayList<String> priorPayments;
    private ArrayList<String> liabilityAdjustments;
    private ArrayList<String> liabilityChecks;
    private ArrayList<String> qbdtPayrollTransactions;

    public ArrayList<String> getEmployeeIds() {
        return employeeIds;
    }

    public void setEmployeeIds(ArrayList<String> employeeIds) {
        this.employeeIds = employeeIds;
    }

    public ArrayList<String> getPayrollItemIds() {
        return payrollItemIds;
    }

    public void setPayrollItemIds(ArrayList<String> payrollItemIds) {
        this.payrollItemIds = payrollItemIds;
    }

    public ArrayList<String> getPaychecks() {
        return paychecks;
    }

    public void setPaychecks(ArrayList<String> paychecks) {
        this.paychecks = paychecks;
    }

    public ArrayList<String> getPriorPayments() {
        return priorPayments;
    }

    public void setPriorPayments(ArrayList<String> priorPayments) {
        this.priorPayments = priorPayments;
    }

    public ArrayList<String> getLiabilityAdjustments() {
        return liabilityAdjustments;
    }

    public void setLiabilityAdjustments(ArrayList<String> liabilityAdjustments) {
        this.liabilityAdjustments = liabilityAdjustments;
    }

    public ArrayList<String> getLiabilityChecks() {
        return liabilityChecks;
    }

    public void setLiabilityChecks(ArrayList<String> liabilityChecks) {
        this.liabilityChecks = liabilityChecks;
    }

    public ArrayList<String> getQbdtPayrollTransactions() {
        return qbdtPayrollTransactions;
    }

    public void setQbdtPayrollTransactions(ArrayList<String> qbdtPayrollTransactions) {
        this.qbdtPayrollTransactions = qbdtPayrollTransactions;
    }
}
