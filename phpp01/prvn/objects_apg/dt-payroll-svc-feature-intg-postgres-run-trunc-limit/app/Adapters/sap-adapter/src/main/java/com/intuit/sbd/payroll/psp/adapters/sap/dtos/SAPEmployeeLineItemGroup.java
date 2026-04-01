package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: cyoder
 * Date: Jun 23, 2009
 * Time: 9:33:40 AM
 */
public class SAPEmployeeLineItemGroup {

    private ArrayList<SAPLineItemValue> compensations;
    private ArrayList<SAPLineItemValue> preTaxDeductions;
    private ArrayList<SAPLineItemValue> employeeTaxes;
    private ArrayList<SAPLineItemValue> postTaxDeductions;
    private ArrayList<SAPLineItemValue> employerTaxes;
    private ArrayList<SAPLineItemValue> taxableEmployerContributions;
    private ArrayList<SAPLineItemValue> noTaxAffectEmployerContributions;
    private ArrayList<SAPLineItemValue> directDeposits;
    private ArrayList<SAPLineItemValue> taxableAdditions;
    private ArrayList<SAPLineItemValue> noTaxAffectAdditions;
    private String netPay;

    public ArrayList<SAPLineItemValue> getCompensations() {
        return compensations;
    }

    public void setCompensations(ArrayList<SAPLineItemValue> compensations) {
        this.compensations = compensations;
    }

    public ArrayList<SAPLineItemValue> getPreTaxDeductions() {
        return preTaxDeductions;
    }

    public void setPreTaxDeductions(ArrayList<SAPLineItemValue> preTaxDeductions) {
        this.preTaxDeductions = preTaxDeductions;
    }

    public ArrayList<SAPLineItemValue> getEmployeeTaxes() {
        return employeeTaxes;
    }

    public void setEmployeeTaxes(ArrayList<SAPLineItemValue> employeeTaxes) {
        this.employeeTaxes = employeeTaxes;
    }

    public ArrayList<SAPLineItemValue> getPostTaxDeductions() {
        return postTaxDeductions;
    }

    public void setPostTaxDeductions(ArrayList<SAPLineItemValue> postTaxDeductions) {
        this.postTaxDeductions = postTaxDeductions;
    }

    public ArrayList<SAPLineItemValue> getEmployerTaxes() {
        return employerTaxes;
    }

    public void setEmployerTaxes(ArrayList<SAPLineItemValue> employerTaxes) {
        this.employerTaxes = employerTaxes;
    }

    public String getNetPay() {
        return netPay;
    }

    public void setNetPay(String netPay) {
        this.netPay = netPay;
    }

    public ArrayList<SAPLineItemValue> getDirectDeposits() {
        return directDeposits;
    }

    public void setDirectDeposits(ArrayList<SAPLineItemValue> pDirectDeposits) {
        directDeposits = pDirectDeposits;
    }

    public ArrayList<SAPLineItemValue> getTaxableAdditions() {
        return taxableAdditions;
    }

    public void setTaxableAdditions(ArrayList<SAPLineItemValue> pTaxableAdditions) {
        taxableAdditions = pTaxableAdditions;
    }

    public ArrayList<SAPLineItemValue> getNoTaxAffectAdditions() {
        return noTaxAffectAdditions;
    }

    public void setNoTaxAffectAdditions(ArrayList<SAPLineItemValue> pNoTaxAffectAdditions) {
        noTaxAffectAdditions = pNoTaxAffectAdditions;
    }

    public ArrayList<SAPLineItemValue> getTaxableEmployerContributions() {
        return taxableEmployerContributions;
    }

    public void setTaxableEmployerContributions(ArrayList<SAPLineItemValue> pTaxableEmployerContributions) {
        taxableEmployerContributions = pTaxableEmployerContributions;
    }

    public ArrayList<SAPLineItemValue> getNoTaxAffectEmployerContributions() {
        return noTaxAffectEmployerContributions;
    }

    public void setNoTaxAffectEmployerContributions(ArrayList<SAPLineItemValue> pNoTaxAffectEmployerContributions) {
        noTaxAffectEmployerContributions = pNoTaxAffectEmployerContributions;
    }
}
