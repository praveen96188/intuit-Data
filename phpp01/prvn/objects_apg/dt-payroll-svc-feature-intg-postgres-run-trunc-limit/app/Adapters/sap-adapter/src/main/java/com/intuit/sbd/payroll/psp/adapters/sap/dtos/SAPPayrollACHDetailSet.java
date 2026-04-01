package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 30, 2009
 * Time: 4:35:19 PM
 */
public class SAPPayrollACHDetailSet {

    public SAPPayrollACHDetailSet(){
        feeTransactions = new ArrayList<SAPPayrollTransaction>();
        taxTransactions = new ArrayList<SAPAgencyTransaction>();
        taxCreditTransactions = new ArrayList<SAPAgencyTransaction>();
        ddTransactions = new ArrayList<SAPPayrollEmployeeTransaction>();

        feeTransactionsTotal = 0.00;
        taxTransactionsTotal = 0.00;
        taxCreditTransactionsTotal = 0.00;
        taxesTotal = 0.00;
        ddTransactionsTotal = 0.00;
    }

    private ArrayList<SAPPayrollTransaction> feeTransactions;
    private ArrayList<SAPAgencyTransaction> taxTransactions;
    private ArrayList<SAPAgencyTransaction> taxCreditTransactions;
    private ArrayList<SAPPayrollEmployeeTransaction> ddTransactions;

    private double feeTransactionsTotal;
    private double taxTransactionsTotal;
    private double taxCreditTransactionsTotal;
    private double taxesTotal;
    private double ddTransactionsTotal;

    public ArrayList<SAPPayrollTransaction> getFeeTransactions() {
        return feeTransactions;
    }

    public void setFeeTransactions(ArrayList<SAPPayrollTransaction> feeTransactions) {
        this.feeTransactions = feeTransactions;
    }

    public ArrayList<SAPAgencyTransaction> getTaxTransactions() {
        return taxTransactions;
    }

    public void setTaxTransactions(ArrayList<SAPAgencyTransaction> taxTransactions) {
        this.taxTransactions = taxTransactions;
    }

    public ArrayList<SAPAgencyTransaction> getTaxCreditTransactions() {
        return taxCreditTransactions;
    }

    public void setTaxCreditTransactions(ArrayList<SAPAgencyTransaction> taxCreditTransactions) {
        this.taxCreditTransactions = taxCreditTransactions;
    }

    public ArrayList<SAPPayrollEmployeeTransaction> getDdTransactions() {
        return ddTransactions;
    }

    public void setDdTransactions(ArrayList<SAPPayrollEmployeeTransaction> ddTransactions) {
        this.ddTransactions = ddTransactions;
    }

    public double getFeeTransactionsTotal() {
        return feeTransactionsTotal;
    }

    public void setFeeTransactionsTotal(double feeTransactionsTotal) {
        this.feeTransactionsTotal = feeTransactionsTotal;
    }

    public double getTaxTransactionsTotal() {
        return taxTransactionsTotal;
    }

    public void setTaxTransactionsTotal(double taxTransactionsTotal) {
        this.taxTransactionsTotal = taxTransactionsTotal;
    }

    public double getTaxCreditTransactionsTotal() {
        return taxCreditTransactionsTotal;
    }

    public void setTaxCreditTransactionsTotal(double taxCreditTransactionsTotal) {
        this.taxCreditTransactionsTotal = taxCreditTransactionsTotal;
    }

    public double getTaxesTotal() {
        return taxesTotal;
    }

    public void setTaxesTotal(double taxesTotal) {
        this.taxesTotal = taxesTotal;
    }

    public double getDdTransactionsTotal() {
        return ddTransactionsTotal;
    }

    public void setDdTransactionsTotal(double ddTransactionsTotal) {
        this.ddTransactionsTotal = ddTransactionsTotal;
    }
}
