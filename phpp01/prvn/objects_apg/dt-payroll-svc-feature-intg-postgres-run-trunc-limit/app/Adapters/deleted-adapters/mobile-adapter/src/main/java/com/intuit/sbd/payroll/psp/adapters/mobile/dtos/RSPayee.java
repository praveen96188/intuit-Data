package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import java.util.ArrayList;
import java.util.List;


/**
 @author Jeff Jones
 */

public class RSPayee implements Comparable<RSPayee> {

    private String id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String name;
    private RSGenderCode gender;
    private String hireDate;
    private String birthDate;
    private String phone;
    private String email;
    private Boolean is1099;
    private String taxId;
    private RSPayeeStatusCode status;
    private RSPayeeType type;
    private RSAddress mailingAddress;
    private String vacation;
    private String sick;
    private String federalFilingStatus;
    private String federalAllowances;
    private String federalAdditionalWithholding;
    private List<RSStateWithholding> stateWithholdings;
    private List<RSBankAccount> bankAccounts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public RSGenderCode getGender() {
        return gender;
    }

    public void setGender(RSGenderCode gender) {
        this.gender = gender;
    }

    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RSPayeeStatusCode getStatus() {
        return status;
    }

    public void setStatus(RSPayeeStatusCode status) {
        this.status = status;
    }

    public RSPayeeType getType() {
        return type;
    }

    public void setType(RSPayeeType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIs1099() {
        return is1099;
    }

    public void setIs1099(Boolean is1099) {
        this.is1099 = is1099;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public RSAddress getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(RSAddress mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    protected String getFullName() {
        String space = " ";
        return getFirstName() + space + getMiddleName() + space + getLastName();
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public List<RSBankAccount> getBankAccounts() {
        if (bankAccounts == null)
            bankAccounts = new ArrayList<RSBankAccount>();

        return bankAccounts;
    }

    public void setBankAccounts(List<RSBankAccount> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    public String getVacation() {
        return vacation;
    }

    public void setVacation(String vacation) {
        this.vacation = vacation;
    }

    public String getSick() {
        return sick;
    }

    public void setSick(String sick) {
        this.sick = sick;
    }

    public String getFederalFilingStatus() {
        return federalFilingStatus;
    }

    public void setFederalFilingStatus(String federalFilingStatus) {
        this.federalFilingStatus = federalFilingStatus;
    }

    public String getFederalAllowances() {
        return federalAllowances;
    }

    public void setFederalAllowances(String federalAllowances) {
        this.federalAllowances = federalAllowances;
    }

    public String getFederalAdditionalWithholding() {
        return federalAdditionalWithholding;
    }

    public void setFederalAdditionalWithholding(String federalAdditionalWithholding) {
        this.federalAdditionalWithholding = federalAdditionalWithholding;
    }

    public List<RSStateWithholding> getStateWithholdings() {
        if (stateWithholdings == null)
            stateWithholdings = new ArrayList<RSStateWithholding>();

        return stateWithholdings;
    }

    public void setStateWithholdings(List<RSStateWithholding> stateWithholdings) {
        this.stateWithholdings = stateWithholdings;
    }

    public int compareTo(RSPayee o) {
        if (getLastName() == null) {
            if (o.getLastName() == null) {
                return getName().compareTo(o.getName());
            } else {
                return getName().compareTo(o.getLastName());
            }
        } else {
            if (o.getLastName() == null) {
                return getLastName().compareTo(o.getName());
            } else {
                return getLastName().compareTo(o.getLastName());
            }
        }
    }
}
