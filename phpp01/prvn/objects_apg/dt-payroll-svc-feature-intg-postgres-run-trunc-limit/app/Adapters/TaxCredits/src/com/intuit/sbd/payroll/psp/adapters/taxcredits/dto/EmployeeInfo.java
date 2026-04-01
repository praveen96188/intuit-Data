package com.intuit.sbd.payroll.psp.adapters.taxcredits.dto;

import com.intuit.sbd.payroll.psp.adapters.taxcredits.adapter.TaxCreditsTranslator;

import java.util.Date;

/**
 * User: dweinberg
 * Date: Jan 22, 2010
 * Time: 12:48:48 PM
 */
public class EmployeeInfo {
    private String firstName;
    private String lastName;
    private String middleInitial;
    private String ssn;
    private Address liveAddress;
    private String telephoneNumber;
    private String telephoneExtension;
    private String email;
    private String dateOfBirthString;
    private String jobOfferDateString;
    private String hireDateString;
    private String startDateString;
    private String startingWage;
    private String position;
    private String workState;
    private String isSeasonalString;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public Address getLiveAddress() {
        return liveAddress;
    }

    public void setLiveAddress(Address liveAddress) {
        this.liveAddress = liveAddress;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getTelephoneExtension() {
        return telephoneExtension;
    }

    public void setTelephoneExtension(String telephoneExtension) {
        this.telephoneExtension = telephoneExtension;
    }

    public String getDateOfBirthString() {
        return dateOfBirthString;
    }

    public void setDateOfBirthString(String dateOfBirthString) {
        this.dateOfBirthString = dateOfBirthString;
    }

    public String getJobOfferDateString() {
        return jobOfferDateString;
    }

    public void setJobOfferDateString(String jobOfferDateString) {
        this.jobOfferDateString = jobOfferDateString;
    }

    public String getHireDateString() {
        return hireDateString;
    }

    public void setHireDateString(String hireDateString) {
        this.hireDateString = hireDateString;
    }

    public String getStartDateString() {
        return startDateString;
    }

    public void setStartDateString(String startDateString) {
        this.startDateString = startDateString;
    }

    public String getStartingWage() {
        return startingWage;
    }

    public void setStartingWage(String startingWage) {
        this.startingWage = startingWage;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getWorkState() {
        return workState;
    }

    public void setWorkState(String workState) {
        this.workState = workState;
    }

    public String getFullName() {
        String fullName = getFirstName();
        if (getMiddleInitial() != null && !getMiddleInitial().equals("")) {
            fullName += " " + getMiddleInitial();
        }
        fullName += " " + getLastName();
        return fullName;
    }

    public String getReverseFullName() {
        String fullName = getLastName();
        fullName += ", " + getFirstName();
        if (getMiddleInitial() != null && !getMiddleInitial().equals("")) {
            fullName += " " + getMiddleInitial();
        }
        return fullName;        
    }

    public Date getHireDate() {
        return TaxCreditsTranslator.parseDate(getHireDateString());
    }

    public Date getDateOfBirth() {
        return TaxCreditsTranslator.parseDate(getDateOfBirthString());
    }

    public Date getJobOfferDate() {
        return TaxCreditsTranslator.parseDate(getJobOfferDateString());
    }

    public Date getStartDate() {
        return TaxCreditsTranslator.parseDate(getStartDateString());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIsSeasonalString() {
        return isSeasonalString;
    }

    public void setIsSeasonalString(String isSeasonalString) {
        this.isSeasonalString = isSeasonalString;
    }

}
