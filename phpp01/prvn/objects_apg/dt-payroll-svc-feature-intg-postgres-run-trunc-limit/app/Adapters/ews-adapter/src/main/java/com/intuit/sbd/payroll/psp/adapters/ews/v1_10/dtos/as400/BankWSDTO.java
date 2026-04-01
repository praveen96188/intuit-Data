/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Calendar;

/**
    @author Jeff Jones
 */
@XmlType(name = "BankType", propOrder = {"bankName", "bankAccountNumber", "bankRoutingNumber",
        "bankAccountQBName", "bankAccountType", "randomDollarDebitRequest", "bankVerificationStatus", "holdReason", "pendingPayrollExists",
        "randomDollar1", "randomDollar2", "noOfRetries", "lastRetryDateTime", "randomDebitDateTime"})
public class BankWSDTO implements Cloneable {
    private String bankName;
    private String bankAccountNumber;
    private String bankRoutingNumber;
    private String bankAccountQBName;
    private BankAccountTypeEnum bankAccountType;
    private Boolean randomDollarDebitRequest;
    private BankVerificationStatusEnum bankVerificationStatus;
    private HoldReasonEnum holdReason;
    private PendingPayrollExistsEnum pendingPayrollExists;
    private String randomDollar1;
    private String randomDollar2;
    private String noOfRetries;
    private Calendar lastRetryDateTime;
    private Calendar randomDebitDateTime;

    public BankWSDTO() {
        this.bankName = null;
        this.bankAccountNumber = null;
        this.bankRoutingNumber = null;
        this.bankAccountQBName = null;
        this.bankAccountType = null;
        this.randomDollarDebitRequest = false;
        this.bankVerificationStatus = null;
        this.holdReason = null;
        this.pendingPayrollExists = null;
        this.randomDollar1 = null;
        this.randomDollar2 = null;
        this.noOfRetries = null;
        this.lastRetryDateTime = null;
        this.randomDebitDateTime = null;
    }

    public BankWSDTO clone() throws CloneNotSupportedException {
        return (BankWSDTO) super.clone();
    }

    @XmlElement(name = "BankName", nillable = false, required = false)
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @XmlElement(name = "BankAccountNumber", nillable = false, required = false)
    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    @XmlElement(name = "BankRoutingNumber", nillable = false, required = false)
    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    public void setBankRoutingNumber(String bankRoutingNumber) {
        this.bankRoutingNumber = bankRoutingNumber;
    }

    @XmlElement(name = "BankAccountQBName", nillable = false, required = false)
    public String getBankAccountQBName() {
        return bankAccountQBName;
    }

    public void setBankAccountQBName(String bankAccountQBName) {
        this.bankAccountQBName = bankAccountQBName;
    }

    @XmlElement(name = "BankAccountType", nillable = false, required = false)
    public BankAccountTypeEnum getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(BankAccountTypeEnum bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    @XmlElement(name = "RandomDollarDebitRequest", nillable = false, required = false)
    public Boolean getRandomDollarDebitRequest() {
        return randomDollarDebitRequest;
    }

    public void setRandomDollarDebitRequest(Boolean randomDollarDebitRequest) {
        this.randomDollarDebitRequest = randomDollarDebitRequest;
    }

    public BankVerificationStatusEnum getBankVerificationStatus() {
        return bankVerificationStatus;
    }

    @XmlElement(name = "BankVerificationStatus", nillable = false, required = false)
    public void setBankVerificationStatus(BankVerificationStatusEnum bankVerificationStatus) {
        this.bankVerificationStatus = bankVerificationStatus;
    }

    @XmlElement(name = "HoldReason", nillable = false, required = false)
    public HoldReasonEnum getHoldReason() {
        return holdReason;
    }

    public void setHoldReason(HoldReasonEnum holdReason) {
        this.holdReason = holdReason;
    }

    @XmlElement(name = "PendingPayrollExists", nillable = false, required = false)
    public PendingPayrollExistsEnum getPendingPayrollExists() {
        return pendingPayrollExists;
    }

    public void setPendingPayrollExists(PendingPayrollExistsEnum pendingPayrollExists) {
        this.pendingPayrollExists = pendingPayrollExists;
    }

    @XmlElement(name = "RandomDollar1", nillable = false, required = false)
    public String getRandomDollar1() {
        return randomDollar1;
    }

    public void setRandomDollar1(String randomDollar1) {
        this.randomDollar1 = randomDollar1;
    }

    @XmlElement(name = "RandomDollar2", nillable = false, required = false)
    public String getRandomDollar2() {
        return randomDollar2;
    }

    public void setRandomDollar2(String randomDollar2) {
        this.randomDollar2 = randomDollar2;
    }

    @XmlElement(name = "NoOfRetries", nillable = false, required = false)
    public String getNoOfRetries() {
        return noOfRetries;
    }

    public void setNoOfRetries(String noOfRetries) {
        this.noOfRetries = noOfRetries;
    }

    @XmlElement(name = "LastRetryDateTime", nillable = false, required = false)
    public Calendar getLastRetryDateTime() {
        return lastRetryDateTime;
    }

    public void setLastRetryDateTime(Calendar lastRetryDateTime) {
        this.lastRetryDateTime = lastRetryDateTime;
    }

    @XmlElement(name = "RandomDebitDateTime", nillable = false, required = false)
    public Calendar getRandomDebitDateTime() {
        return randomDebitDateTime;
    }

    public void setRandomDebitDateTime(Calendar randomDebitDateTime) {
        this.randomDebitDateTime = randomDebitDateTime;
    }

    public void validateBankName() throws Exception {
        if (!Validation.validateValue(this.bankName, false, "^(\\P{M}\\p{M}*){1,255}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("BankName", "Bank"));
        }
    }

    public void validateBankAccountNumber() throws Exception {
        if (!Validation.validateValue(this.bankAccountNumber, false, "^(\\P{M}\\p{M}*){1,17}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("BankAccountNumber", "Bank"));
        }
    }

    public void validateBankRoutingNumber() throws Exception {
        if (!Validation.validateValue(this.bankRoutingNumber, false, "^(\\P{M}\\p{M}*){9,9}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("BankRoutingNumber", "Bank"));
        }
    }

    public void validateBankAccountQBName() throws Exception {
        if (!Validation.validateValue(this.bankAccountQBName, false, "^(\\P{M}\\p{M}*){0,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("BankAccountQBName", "Bank"));
        }
    }

    public void validateBankAccountType() throws Exception {
        if (this.bankAccountType == null) {
            throw new EwsException(EwsMessages.fieldDataNotValid("BankAccountType", "Bank"));
        }
    }

    public void validateHoldReason() throws Exception {
        if (this.holdReason == null) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("HoldReason", "Bank"));
        }
    }

    public void validateRandomDollar1() throws Exception {
        if (!Validation.validateValue(this.randomDollar1, false, "0{0,1}\\.{0,1}\\d{1,2}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("RandomDollar1", "Bank"));
        }
    }

    public void validateRandomDollar2() throws Exception {
        if (!Validation.validateValue(this.randomDollar2, false, "0{0,1}\\.{0,1}\\d{1,2}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("RandomDollar2", "Bank"));
        }
    }
    
}
