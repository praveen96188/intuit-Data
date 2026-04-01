package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;
import java.util.ArrayList;

/**
 * User: rnorian
 * Date: Sep 25, 2009
 * Time: 1:51:24 PM
 */
public class SAPACHOffloadJobLogEntry {
    private String jobName;
    private Date startDateTime;
    private Date finishDateTime;
    private long estimatedRunTimeInMillis;
    public long actualRunTimeInMillis;
    private ArrayList<SAPACHOffloadJobStepLogEntry> stepLogs;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getFinishDateTime() {
        return finishDateTime;
    }

    public void setFinishDateTime(Date finishDateTime) {
        this.finishDateTime = finishDateTime;
    }

    public long getEstimatedRunTimeInMillis() {
        return estimatedRunTimeInMillis;
    }

    public void setEstimatedRunTimeInMillis(long estimatedRunTimeInMillis) {
        this.estimatedRunTimeInMillis = estimatedRunTimeInMillis;
    }

    public long getActualRunTimeInMillis() {
        return actualRunTimeInMillis;
    }

    public void setActualRunTimeInMillis(long actualRunTimeInMillis) {
        if (actualRunTimeInMillis == 0)
            actualRunTimeInMillis = -1;
        this.actualRunTimeInMillis = actualRunTimeInMillis;
    }

    public ArrayList<SAPACHOffloadJobStepLogEntry> getStepLogs() {
        return stepLogs;
    }

    public void setStepLogs(ArrayList<SAPACHOffloadJobStepLogEntry> stepLogs) {
        this.stepLogs = stepLogs;
    }

    public int indexOfStepLog(String jobAction) {
        for (int i = 0; i < getStepLogs().size(); i++) {
            if (getStepLogs().get(i).getJobName().equals(jobAction))
                return i;
        }

        return -1;
    }

    @Override
    public String toString() {
        return "SAPACHOffloadJobLogEntry{" +
                "jobName='" + jobName + '\'' +
                ", startDateTime=" + startDateTime +
                ", finishDateTime=" + finishDateTime +
                ", estimatedRunTimeInMillis=" + estimatedRunTimeInMillis +
                ", actualRunTimeInMillis=" + actualRunTimeInMillis +
                ", stepLogs=" + stepLogs +
                '}';
    }
}
