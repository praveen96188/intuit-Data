package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: rnorian
 * Date: Sep 29, 2009
 * Time: 2:27:23 PM
 */
public class SAPACHOffloadStatus {
    private int estimatedTransactionCount;
    private int actualTransactionCount;

    private ArrayList<SAPACHOffloadJobLogEntry> jobLogEntries;

    public int getEstimatedTransactionCount() {
        return estimatedTransactionCount;
    }

    public void setEstimatedTransactionCount(int estimatedTransactionCount) {
        this.estimatedTransactionCount = estimatedTransactionCount;
    }

    public int getActualTransactionCount() {
        return actualTransactionCount;
    }

    public void setActualTransactionCount(int actualTransactionCount) {
        this.actualTransactionCount = actualTransactionCount;
    }

    public ArrayList<SAPACHOffloadJobLogEntry> getJobLogEntries() {
        return jobLogEntries;
    }

    public void setJobLogEntries(ArrayList<SAPACHOffloadJobLogEntry> jobLogEntries) {
        this.jobLogEntries = jobLogEntries;
    }
}
