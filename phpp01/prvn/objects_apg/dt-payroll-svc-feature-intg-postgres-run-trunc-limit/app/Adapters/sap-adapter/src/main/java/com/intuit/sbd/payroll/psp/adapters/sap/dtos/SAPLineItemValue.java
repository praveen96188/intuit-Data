package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.CompanyLaw;
import com.intuit.sbd.payroll.psp.domain.CompanyPayrollItem;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User: cyoder
 * Date: Jun 23, 2009
 * Time: 9:33:40 AM
 */
public class SAPLineItemValue implements Comparable<SAPLineItemValue> {

    private String itemId;
    private String itemName;
    private String sourceDescription;
    private String taxFormLine;
    private double amount = 0;
    private double totalWages = 0;
    private double taxableWages = 0;
    private double hoursWorked = 0;

    private boolean isIrs;

    public SAPLineItemValue() {
    }


    public SAPLineItemValue(String itemId, String itemName, String sourceDescription, String taxFormLine, double amount) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.sourceDescription = sourceDescription;
        this.amount = amount;
        this.taxFormLine = taxFormLine;
    }

    //For deductions and Employer Contributions
    public SAPLineItemValue(CompanyPayrollItem companyPayrollItem, double amount) {
        this(companyPayrollItem.getSourcePayrollItemId(), companyPayrollItem.getPayrollItem().getPayrollItemDescription(), companyPayrollItem.getSourcePayrollItemId() + " - " + companyPayrollItem.getSourceDescription(), companyPayrollItem.getTaxFormLine(), amount);
    }

    //For Tax Items
    public SAPLineItemValue(CompanyLaw companyLaw, double amount, double totalWages, double taxableWages) {
        this(companyLaw.getSourceId(), StringUtils.defaultIfEmpty(companyLaw.getLaw().getLawTypeCd(), companyLaw.getLaw().getDescription()) + " (" + companyLaw.getLaw().getLawAbbrev() + ")", companyLaw.getSourceId() + " - " + companyLaw.getSourceDescription(), companyLaw.getTaxFormLine(), amount);
        this.totalWages = totalWages;
        this.taxableWages = taxableWages;
        this.isIrs = companyLaw.getLaw().getPaymentTemplate().getAgency().isIRS();
    }

    // For Compensations
    public SAPLineItemValue(CompanyPayrollItem companyPayrollItem, double amount, double hoursWorked) {
        this(companyPayrollItem.getSourcePayrollItemId(), companyPayrollItem.getPayrollItem().getPayrollItemDescription(), companyPayrollItem.getSourcePayrollItemId() + " - " + companyPayrollItem.getSourceDescription(), companyPayrollItem.getTaxFormLine(), amount);
        this.hoursWorked = hoursWorked;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String pItemId) {
        itemId = pItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String pItemName) {
        itemName = pItemName;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    public void setSourceDescription(String pSourceDescription) {
        sourceDescription = pSourceDescription;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double pAmount) {
        amount = pAmount;
    }

    public double getTotalWages() {
        return totalWages;
    }

    public void setTotalWages(double pTotalWages) {
        totalWages = pTotalWages;
    }

    public double getTaxableWages() {
        return taxableWages;
    }

    public void setTaxableWages(double pTaxableWages) {
        taxableWages = pTaxableWages;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(double pHoursWorked) {
        hoursWorked = pHoursWorked;
    }

    public void addAmount(double amount, boolean voided) {
        if (!voided) this.amount += amount;
    }

    public void addHoursWorked(double hours, boolean voided) {
        if (!voided) this.hoursWorked += hours;
    }

    public void addTaxableWages(double taxableWages, boolean voided) {
        if (!voided) this.taxableWages += taxableWages;
    }

    public void addTotalWages(double totalWages, boolean voided) {
        if (!voided) this.totalWages += totalWages;
    }

    public boolean isIrs() {
        return isIrs;
    }

    public void setIrs(boolean pIrs) {
        isIrs = pIrs;
    }

    public String getTaxFormLine() {
        return taxFormLine;
    }

    public void setTaxFormLine(String pTaxFormLine) {
        taxFormLine = pTaxFormLine;
    }

    public int compareTo(SAPLineItemValue o) {
        String thisSortKey = getItemName();
        String oSortKey = o.getItemName();

        int thisSortPrecedence = sortPrecedenceMap.containsKey(thisSortKey) ? sortPrecedenceMap.get(thisSortKey) : 1000;
        int oSortPrecedence = sortPrecedenceMap.containsKey(oSortKey) ? sortPrecedenceMap.get(oSortKey) : 1000;

        if (thisSortPrecedence != oSortPrecedence) {
            return thisSortPrecedence - oSortPrecedence;
        }

        if (!thisSortKey.equals(oSortKey)) {
            return thisSortKey.compareTo(oSortKey);
        }

        return getSourceDescription().compareTo(o.getSourceDescription());
    }

    private static Map<String, Integer> sortPrecedenceMap = new HashMap<String, Integer>();
    static {
        sortPrecedenceMap.put("Salary", 1);
        sortPrecedenceMap.put("Hourly", 2);
        sortPrecedenceMap.put("Commission", 3);
        sortPrecedenceMap.put("Bonus", 3);
        sortPrecedenceMap.put("Paycheck Total", 999);
    }
}
