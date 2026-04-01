package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: dweinberg
 * Date: 9/10/12
 * Time: 9:26 AM
 */
public class SAPEmployeeLineItemCollection {
    private ArrayList<SAPLineItemValue> compensationItems;
    private ArrayList<SAPLineItemValue> preTaxDeductionItems;
    private ArrayList<SAPLineItemValue> employeeTaxItems;
    private ArrayList<SAPLineItemValue> postTaxDeductionItems;
    private ArrayList<SAPLineItemValue> employerTaxItems;
    private ArrayList<SAPLineItemValue> taxableEmployerContributionItems;
    private ArrayList<SAPLineItemValue> noTaxAffectEmployerContributionItems;
    private ArrayList<SAPLineItemValue> directDepositItems;
    private ArrayList<SAPLineItemValue> taxableAdditionItems;
    private ArrayList<SAPLineItemValue> noTaxAffectAdditionItems;
    private double netPay;

    public ArrayList<SAPLineItemValue> getCompensationItems() {
        return compensationItems;
    }

    public void setCompensationItems(ArrayList<SAPLineItemValue> pCompensationItems) {
        compensationItems = pCompensationItems;
    }

    public ArrayList<SAPLineItemValue> getPreTaxDeductionItems() {
        return preTaxDeductionItems;
    }

    public void setPreTaxDeductionItems(ArrayList<SAPLineItemValue> pPreTaxDeductionItems) {
        preTaxDeductionItems = pPreTaxDeductionItems;
    }

    public ArrayList<SAPLineItemValue> getEmployeeTaxItems() {
        return employeeTaxItems;
    }

    public void setEmployeeTaxItems(ArrayList<SAPLineItemValue> pEmployeeTaxItems) {
        employeeTaxItems = pEmployeeTaxItems;
    }

    public ArrayList<SAPLineItemValue> getPostTaxDeductionItems() {
        return postTaxDeductionItems;
    }

    public void setPostTaxDeductionItems(ArrayList<SAPLineItemValue> pPostTaxDeductionItems) {
        postTaxDeductionItems = pPostTaxDeductionItems;
    }

    public ArrayList<SAPLineItemValue> getEmployerTaxItems() {
        return employerTaxItems;
    }

    public void setEmployerTaxItems(ArrayList<SAPLineItemValue> pEmployerTaxItems) {
        employerTaxItems = pEmployerTaxItems;
    }

    public ArrayList<SAPLineItemValue> getDirectDepositItems() {
        return directDepositItems;
    }

    public void setDirectDepositItems(ArrayList<SAPLineItemValue> pDirectDepositItems) {
        directDepositItems = pDirectDepositItems;
    }

    public ArrayList<SAPLineItemValue> getTaxableAdditionItems() {
        return taxableAdditionItems;
    }

    public void setTaxableAdditionItems(ArrayList<SAPLineItemValue> pTaxableAdditionItems) {
        taxableAdditionItems = pTaxableAdditionItems;
    }

    public ArrayList<SAPLineItemValue> getNoTaxAffectAdditionItems() {
        return noTaxAffectAdditionItems;
    }

    public void setNoTaxAffectAdditionItems(ArrayList<SAPLineItemValue> pNoTaxAffectAdditionItems) {
        noTaxAffectAdditionItems = pNoTaxAffectAdditionItems;
    }

    public ArrayList<SAPLineItemValue> getTaxableEmployerContributionItems() {
        return taxableEmployerContributionItems;
    }

    public void setTaxableEmployerContributionItems(ArrayList<SAPLineItemValue> pTaxableEmployerContributionItems) {
        taxableEmployerContributionItems = pTaxableEmployerContributionItems;
    }

    public ArrayList<SAPLineItemValue> getNoTaxAffectEmployerContributionItems() {
        return noTaxAffectEmployerContributionItems;
    }

    public void setNoTaxAffectEmployerContributionItems(ArrayList<SAPLineItemValue> pNoTaxAffectEmployerContributionItems) {
        noTaxAffectEmployerContributionItems = pNoTaxAffectEmployerContributionItems;
    }

    public double getNetPay() {
        return netPay;
    }

    public void setNetPay(double pNetPay) {
        netPay = pNetPay;
    }
}
