package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: cyoder
 * Date: Jun 10, 2008
 * Time: 6:13:43 AM
 */
public class SAPEmployeeInfo {

    private String employeeGseq;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String socialSecurityNumber;
    private String stateLive;
    private String stateWork;
    private Date birthDate;
    private Date firstPayDate;
    private Date lastPayDate;
    private Date hireDate;
    private Date rehireDate;
    private Date termDate;

    private SAPAddress mailingAddress;
    private String status;
    private boolean dd;
    private boolean enforceSubjectTo;
    private String isSeasonal;


    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String pEmployeeId) {
        this.employeeId = pEmployeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String pFirstName) {
        this.firstName = pFirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String pLastName) {
        this.lastName = pLastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String pMiddleName) {
        this.middleName = pMiddleName;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String pSocialSecurityNumber) {
        this.socialSecurityNumber = pSocialSecurityNumber;
    }

    public String getStateLive() {
        return stateLive;
    }

    public void setStateLive(String stateLive) {
        this.stateLive = stateLive;
    }

    public String getStateWork() {
        return stateWork;
    }

    public void setStateWork(String stateWork) {
        this.stateWork = stateWork;
    }

    public boolean isDd() {
        return dd;
    }

    public void setDd(boolean dd) {
        this.dd = dd;
    }

    public Date getFirstPayDate() {
        return firstPayDate;
    }

    public void setFirstPayDate(Date firstPayDate) {
        this.firstPayDate = firstPayDate;
    }

    public Date getLastPayDate() {
        return lastPayDate;
    }

    public void setLastPayDate(Date lastPayDate) {
        this.lastPayDate = lastPayDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmployeeGseq() {
        return employeeGseq;
    }

    public void setEmployeeGseq(String employeeGseq) {
        this.employeeGseq = employeeGseq;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public Date getTermDate() {
        return termDate;
    }

    public void setTermDate(Date termDate) {
        this.termDate = termDate;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getRehireDate() {
        return rehireDate;
    }

    public void setRehireDate(Date rehireDate) {
        this.rehireDate = rehireDate;
    }

    public SAPAddress getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(SAPAddress legalAddress) {
        this.mailingAddress = legalAddress;
    }

    public boolean isEnforceSubjectTo() {
        return enforceSubjectTo;
    }

    public void setEnforceSubjectTo(boolean enforceSubjectTo) {
        this.enforceSubjectTo = enforceSubjectTo;
    }

    public String getIsSeasonal() {
        return isSeasonal;
    }

    public void setIsSeasonal(String isSeasonal) {
        this.isSeasonal = isSeasonal;
    }
}
