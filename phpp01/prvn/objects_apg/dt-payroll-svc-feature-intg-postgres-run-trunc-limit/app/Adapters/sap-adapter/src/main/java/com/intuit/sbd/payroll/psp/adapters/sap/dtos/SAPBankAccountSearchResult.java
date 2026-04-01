package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 24, 2010
 * Time: 4:02:34 PM
 */
public class SAPBankAccountSearchResult {
    private String companyLegalName;
    private SAPCompanyKey companyKey;
    private String accountOwnerName;
    private String accountType;
    private String accountStatus;

    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String pCompanyLegalName) {
        companyLegalName = pCompanyLegalName;
    }

    public SAPCompanyKey getCompanyKey() {
        return companyKey;
    }

    public void setCompanyKey(SAPCompanyKey pCompanyKey) {
        companyKey = pCompanyKey;
    }

    public String getAccountOwnerName() {
        return accountOwnerName;
    }

    public void setAccountOwnerName(String pAccountOwnerName) {
        accountOwnerName = pAccountOwnerName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String pAccountType) {
        accountType = pAccountType;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String pAccountStatus) {
        accountStatus = pAccountStatus;
    }
}
