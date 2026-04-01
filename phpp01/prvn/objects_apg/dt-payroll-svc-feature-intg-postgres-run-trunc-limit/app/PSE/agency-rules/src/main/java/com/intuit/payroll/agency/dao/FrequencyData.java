/*
 * Copyright Statement:
 * CONFIDENTIAL -- Copyright 2000-2003 Intuit Inc. This material contains
 * certain trade secrets and confidential and proprietary information
 * of Intuit Inc. Use, reproduction, disclosure and distribution by
 * any means are prohibited, except pursuant to a written license from
 * Intuit Inc. Use of copyright notice is precautionary and does not
 * imply publication or disclosure.
 */
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.impl.Frequency;
import com.intuit.payroll.agency.impl.PaymentPeriod;
import com.intuit.payroll.agency.impl.UpperLimit;
import com.intuit.payroll.agency.dao.mnemonics.MnemonicPeriod;
import com.intuit.payroll.agency.dao.mnemonics.MnemonicInterpreterResponse;
import com.intuit.payroll.agency.dao.mnemonics.MnemonicInterpreter;
import com.intuit.payroll.agency.dao.mnemonics.MnemonicInterpreterRequest;
import com.intuit.payroll.agency.dao.mnemonics.InvalidMnemonicException;
import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.payroll.agency.api.NoResultsFoundException;

import java.util.ArrayList;

public class FrequencyData extends Frequency {

    // collection of MnemonicPeriod
    private ArrayList<MnemonicPeriod> mnemonicPeriods = new ArrayList();

    // collection of Payment Period
    private ArrayList<PaymentPeriod> emergencyPeriods = new ArrayList();

    // collection of upper limit
    private ArrayList<UpperLimit> upperLimits = new ArrayList();

    public FrequencyData() {
    }

    public void addPeriod(MnemonicPeriod period) {
        mnemonicPeriods.add(period);
    }

    public void addEmergencyDateOverride(PaymentPeriod period) {
        emergencyPeriods.add(period);
    }

    public void addUpperLimit(UpperLimit upperLimit) {
        upperLimits.add(upperLimit);
    }

    public PaymentPeriod getPaymentPeriod(RulesCalendar accrualDate) {
        PaymentPeriod result = null;
        ArrayList<PaymentPeriod> periods;
        if (!emergencyPeriods.isEmpty()) {
            result = accrualDate.findInPaymentPeriods(emergencyPeriods);
        }
        // if an emergency period works, don't look at the mnemonics
        if (result == null) {
            periods = getMnemonicPaymentPeriods(accrualDate);
            result = accrualDate.findInPaymentPeriods(periods);
        }
        return result;
    }

    public ArrayList<UpperLimit> getUpperLimits() {
        return upperLimits;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Frequency: ");
        buf.append(getPaymentFrequencyID());
        for (Object period : mnemonicPeriods) {
            buf.append(period);
        }
        buf.append("\r\n");
        return buf.toString();
    }

    private ArrayList<PaymentPeriod> getMnemonicPaymentPeriods(RulesCalendar accrualDate) {

        ArrayList<PaymentPeriod> results = new ArrayList<PaymentPeriod>(mnemonicPeriods.size());
        for (MnemonicPeriod mnemonicPeriod : mnemonicPeriods) {
            PaymentPeriod period = new PaymentPeriod();

            MnemonicInterpreterRequest request = new MnemonicInterpreterRequest(mnemonicPeriod.start, mnemonicPeriod.end, mnemonicPeriod.dueOn, accrualDate);
            MnemonicInterpreterResponse response;
            try {
                response = MnemonicInterpreter.parseMnemonicDates(request);
            }
            catch (InvalidMnemonicException ime) {
                // If the rules xml has a wacky, unidentifiable mnemonic, we'll throw
                // a NoResultsFound exception instead.
                throw new NoResultsFoundException("No results found due to an invalid rules encoding.");
            }

            period.setFromAccrualDate(response.getStartDate());
            period.setToAccrualDate(response.getEndDate());
            period.setDueDate(response.getDueDate());
            period.setUIString(mnemonicPeriod.uiString);
            period.parseUIString();
            period.setDueDatePolicy(response.getDueDateAdjustmentPolicy());
            results.add(period);
        }

        return results;
    }

    public ArrayList<MnemonicPeriod> getMnemonicPeriods() {
        return mnemonicPeriods;
    }
}

