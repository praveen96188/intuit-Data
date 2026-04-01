package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by anandp233 on 2/19/14.
 */
public class SAPRTBJob {

    private String jobName;
    private String shortDescription;
    private String description;


    public SAPRTBJob(String pJobName, String pJobDescription, String pDescription) {
        jobName = pJobName;
        shortDescription = pJobDescription;
        description = pDescription;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String pJobName) {
        jobName = pJobName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String pShortDescription) {
        shortDescription = pShortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }
}
