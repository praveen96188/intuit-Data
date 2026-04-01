package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: rnorian
 * Date: Sep 25, 2009
 * Time: 2:48:19 PM
 */
public class SAPACHOffloadJobStepLogEntry {
    private String jobName;
    private String stepName;
    private Date stepBeginDateTime;
    private String status;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public Date getStepBeginDateTime() {
        return stepBeginDateTime;
    }

    public void setStepBeginDateTime(Date stepBeginDateTime) {
        this.stepBeginDateTime = stepBeginDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SAPACHOffloadJobStepLogEntry{" +
                "jobName='" + jobName + '\'' +
                ", stepName='" + stepName + '\'' +
                ", stepBeginDateTime=" + stepBeginDateTime +
                ", status='" + status + '\'' +
                '}';
    }
}
