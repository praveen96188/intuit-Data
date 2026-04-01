/*
 * : $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.LedgerAccountCode;

import java.util.ArrayList;

/**
 * SAPCompanyLedgerAccount - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class SAPCompanyLedgerAccount {
    private String name;
    private String description;
    private LedgerAccountCode ledgerAccountCode;
    private double balance;
    private boolean isCredit;
    private boolean creditAddsToBalance;

    public boolean getCreditAddsToBalance() {
        return creditAddsToBalance;
    }

    public void setCreditAddsToBalance(boolean creditAddsToBalance) {
        this.creditAddsToBalance = creditAddsToBalance;
    }

    private ArrayList<SAPActionEvent> actionCollection;

    private boolean requiresQuarterLaw = false;

    public boolean getRequiresQuarterLaw() {
        return requiresQuarterLaw;
    }

    public void setRequiresQuarterLaw(boolean requiresQuarterLaw) {
        this.requiresQuarterLaw = requiresQuarterLaw;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LedgerAccountCode getLedgerAccountCode() {
        return ledgerAccountCode;
    }

    public void setLedgerAccountCode(LedgerAccountCode ledgerAccountCode) {
        this.ledgerAccountCode = ledgerAccountCode;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isCredit() {
        return isCredit;
    }

    public void setCredit(boolean credit) {
        isCredit = credit;
    }

    public ArrayList<SAPActionEvent> getActionCollection() {
        return actionCollection;
    }

    public void setActionCollection(ArrayList<SAPActionEvent> actionCollection) {
        this.actionCollection = actionCollection;
    }
}
