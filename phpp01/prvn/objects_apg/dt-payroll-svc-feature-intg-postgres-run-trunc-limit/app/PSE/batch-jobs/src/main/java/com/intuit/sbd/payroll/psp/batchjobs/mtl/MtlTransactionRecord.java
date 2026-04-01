package com.intuit.sbd.payroll.psp.batchjobs.mtl;

import org.apache.commons.lang3.StringUtils;

/**
 * @author kmuthurangam
 */
public class MtlTransactionRecord {

    public static final String FIELD_NOT_AVAILABLE = StringUtils.EMPTY;

    private String transactionNumber;

    private String transactionDate;

    private String transactionTime;

    private String transactionAmount;

    private String amountAndCurrencyType;

    private String exchangeRate;

    private String transactionFee;

    private String customerCompanyId;

    private String customerName;

    private String customerAddress;

    private String customerAddress2;

    private String customerCity;

    private String customerZipCode;

    private String customerState;

    private String customerCountry;

    private String customerTelephone;

    private String customerEin;

    private String customerPassportNum;

    private String customerPassportCountry;

    private String customerPhotoIdNum;

    private String customerPhotoIdType;

    private String primaryPrincipalOfficerName;

    private String primaryPrincipalOfficerAddress;

    private String primaryPrincipalOfficerSsn;

    private String primaryPrincipalOfficerDob;

    private String beneficiaryType;

    private String beneficiaryCompanyId;

    private String beneficiaryCountry;

    private String beneficiaryName;

    private String beneficiaryAddress;

    private String beneficiaryPhone;

    private String beneficiaryBank;

    private String beneficiaryBankAccountNumber;

    private String senderName;

    private String senderAddress;

    private String senderPhone;

    private String officeLocation;

    private String paymentMethod;

    private String employeeInitials;

    private String comments;

    private String product;

    private String rails;

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getAmountAndCurrencyType() {
        return amountAndCurrencyType;
    }

    public void setAmountAndCurrencyType(String amountAndCurrencyType) {
        this.amountAndCurrencyType = amountAndCurrencyType;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(String transactionFee) {
        this.transactionFee = transactionFee;
    }

    public String getCustomerCompanyId() {
        return customerCompanyId;
    }

    public void setCustomerCompanyId(String customerCompanyId) {
        this.customerCompanyId = customerCompanyId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerAddress2() {
        return customerAddress2;
    }

    public void setCustomerAddress2(String customerAddress2) {
        this.customerAddress2 = customerAddress2;
    }

    public String getCustomerCity() {
        return customerCity;
    }

    public void setCustomerCity(String customerCity) {
        this.customerCity = customerCity;
    }

    public String getCustomerZipCode() {
        return customerZipCode;
    }

    public void setCustomerZipCode(String customerZipCode) {
        this.customerZipCode = customerZipCode;
    }

    public String getCustomerState() {
        return customerState;
    }

    public void setCustomerState(String customerState) {
        this.customerState = customerState;
    }

    public String getCustomerCountry() {
        return customerCountry;
    }

    public void setCustomerCountry(String customerCountry) {
        this.customerCountry = customerCountry;
    }

    public String getCustomerTelephone() {
        return customerTelephone;
    }

    public void setCustomerTelephone(String customerTelephone) {
        this.customerTelephone = customerTelephone;
    }

    public String getCustomerEin() {
        return customerEin;
    }

    public void setCustomerEin(String customerEin) {
        this.customerEin = customerEin;
    }

    public String getCustomerPassportNum() {
        return customerPassportNum;
    }

    public void setCustomerPassportNum(String customerPassportNum) {
        this.customerPassportNum = customerPassportNum;
    }

    public String getCustomerPassportCountry() {
        return customerPassportCountry;
    }

    public void setCustomerPassportCountry(String customerPassportCountry) {
        this.customerPassportCountry = customerPassportCountry;
    }

    public String getCustomerPhotoIdNum() {
        return customerPhotoIdNum;
    }

    public void setCustomerPhotoIdNum(String customerPhotoIdNum) {
        this.customerPhotoIdNum = customerPhotoIdNum;
    }

    public String getCustomerPhotoIdType() {
        return customerPhotoIdType;
    }

    public void setCustomerPhotoIdType(String customerPhotoIdType) {
        this.customerPhotoIdType = customerPhotoIdType;
    }

    public String getPrimaryPrincipalOfficerName() {
        return primaryPrincipalOfficerName;
    }

    public void setPrimaryPrincipalOfficerName(String primaryPrincipalOfficerName) {
        this.primaryPrincipalOfficerName = primaryPrincipalOfficerName;
    }

    public String getPrimaryPrincipalOfficerAddress() {
        return primaryPrincipalOfficerAddress;
    }

    public void setPrimaryPrincipalOfficerAddress(String primaryPrincipalOfficerAddress) {
        this.primaryPrincipalOfficerAddress = primaryPrincipalOfficerAddress;
    }

    public String getPrimaryPrincipalOfficerSsn() {
        return primaryPrincipalOfficerSsn;
    }

    public void setPrimaryPrincipalOfficerSsn(String primaryPrincipalOfficerSsn) {
        this.primaryPrincipalOfficerSsn = primaryPrincipalOfficerSsn;
    }

    public String getPrimaryPrincipalOfficerDob() {
        return primaryPrincipalOfficerDob;
    }

    public void setPrimaryPrincipalOfficerDob(String primaryPrincipalOfficerDob) {
        this.primaryPrincipalOfficerDob = primaryPrincipalOfficerDob;
    }

    public String getBeneficiaryType() {
        return beneficiaryType;
    }

    public void setBeneficiaryType(String beneficiaryType) {
        this.beneficiaryType = beneficiaryType;
    }

    public String getBeneficiaryCompanyId() {
        return beneficiaryCompanyId;
    }

    public void setBeneficiaryCompanyId(String beneficiaryCompanyId) {
        this.beneficiaryCompanyId = beneficiaryCompanyId;
    }

    public String getBeneficiaryCountry() {
        return beneficiaryCountry;
    }

    public void setBeneficiaryCountry(String beneficiaryCountry) {
        this.beneficiaryCountry = beneficiaryCountry;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getBeneficiaryAddress() {
        return beneficiaryAddress;
    }

    public void setBeneficiaryAddress(String beneficiaryAddress) {
        this.beneficiaryAddress = beneficiaryAddress;
    }

    public String getBeneficiaryPhone() {
        return beneficiaryPhone;
    }

    public void setBeneficiaryPhone(String beneficiaryPhone) {
        this.beneficiaryPhone = beneficiaryPhone;
    }

    public String getBeneficiaryBank() {
        return beneficiaryBank;
    }

    public void setBeneficiaryBank(String beneficiaryBank) {
        this.beneficiaryBank = beneficiaryBank;
    }

    public String getBeneficiaryBankAccountNumber() {
        return beneficiaryBankAccountNumber;
    }

    public void setBeneficiaryBankAccountNumber(String beneficiaryBankAccountNumber) {
        this.beneficiaryBankAccountNumber = beneficiaryBankAccountNumber;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getEmployeeInitials() {
        return employeeInitials;
    }

    public void setEmployeeInitials(String employeeInitials) {
        this.employeeInitials = employeeInitials;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getRails() {
        return rails;
    }

    public void setRails(String rails) {
        this.rails = rails;
    }

    public boolean isValid() {
        if (StringUtils.isEmpty(getTransactionNumber())) {
            return false;
        }

        if (StringUtils.isEmpty(getBeneficiaryType())) {
            return false;
        }

        return true;
    }
}
