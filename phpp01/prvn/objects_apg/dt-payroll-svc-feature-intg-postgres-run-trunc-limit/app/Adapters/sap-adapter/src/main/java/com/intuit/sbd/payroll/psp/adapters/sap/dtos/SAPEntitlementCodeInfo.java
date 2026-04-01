package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: Jun 28, 2010
 * Time: 5:00:06 PM
 */
public class SAPEntitlementCodeInfo {
    private String edition;
    private String numberOfEmployees;
    private String quickBooksSubtype;
    private String subtypeDescription;
    private String assetItemCode;
    private String assetItemNumber;

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(String numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    public String getQuickBooksSubtype() {
        return quickBooksSubtype;
    }

    public void setQuickBooksSubtype(String quickBooksSubtype) {
        this.quickBooksSubtype = quickBooksSubtype;
    }

    public String getSubtypeDescription() {
        return subtypeDescription;
    }

    public void setSubtypeDescription(String subtypeDescription) {
        this.subtypeDescription = subtypeDescription;
    }

    public String getAssetItemCode() {
        return assetItemCode;
    }

    public void setAssetItemCode(String assetItemCode) {
        this.assetItemCode = assetItemCode;
    }

    public String getAssetItemNumber() {
        return assetItemNumber;
    }

    public void setAssetItemNumber(String assetItemNumber) {
        this.assetItemNumber = assetItemNumber;
    }
}
