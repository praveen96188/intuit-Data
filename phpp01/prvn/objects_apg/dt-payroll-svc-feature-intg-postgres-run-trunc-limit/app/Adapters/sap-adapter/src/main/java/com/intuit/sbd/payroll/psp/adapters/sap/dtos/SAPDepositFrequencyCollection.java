package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.List;

/**
 * User: dweinberg
 * Date: 3/6/13
 * Time: 4:27 PM
 */
public class SAPDepositFrequencyCollection {
    private List<SAPDepositFrequency> depositFrequencies;

    private List<String> availableFrequencies;
    private String defaultDepositFrequency;

    public List<SAPDepositFrequency> getDepositFrequencies() {
        return depositFrequencies;
    }

    public void setDepositFrequencies(List<SAPDepositFrequency> pDepositFrequencies) {
        depositFrequencies = pDepositFrequencies;
    }

    public String getDefaultDepositFrequency() {
        return defaultDepositFrequency;
    }

    public void setDefaultDepositFrequency(String pDefaultDepositFrequency) {
        defaultDepositFrequency = pDefaultDepositFrequency;
    }

    public List<String> getAvailableFrequencies() {
        return availableFrequencies;
    }

    public void setAvailableFrequencies(List<String> pAvailableFrequencies) {
        availableFrequencies = pAvailableFrequencies;
    }
}
