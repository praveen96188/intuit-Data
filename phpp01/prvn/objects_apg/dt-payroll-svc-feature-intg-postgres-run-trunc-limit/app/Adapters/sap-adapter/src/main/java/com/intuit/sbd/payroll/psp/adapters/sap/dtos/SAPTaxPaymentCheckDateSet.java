package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 12, 2009
 * Time: 2:59:13 PM
 */
public class SAPTaxPaymentCheckDateSet {
    private double checkDateTotal;
    private ArrayList<SAPPaymentDetails> agencyTransactions;

    public double getCheckDateTotal() {
        return checkDateTotal;
    }

    public void setCheckDateTotal(double checkDateTotal) {
        this.checkDateTotal = checkDateTotal;
    }

    public ArrayList<SAPPaymentDetails> getAgencyTransactions() {
        return agencyTransactions;
    }

    public void setAgencyTransactions(ArrayList<SAPPaymentDetails> agencyTransactions) {
        this.agencyTransactions = agencyTransactions;
    }
}
