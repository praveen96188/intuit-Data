package com.intuit.sbd.payroll.psp.adapters.sap.dtos;
/**
 * Created by: smodgil on 01/18/20.
 * Description: This is a dto class meant for Report job.
 */
public class SAPReportJob {
    private String reportName;
    private String shortDescription;
    private String description;


    public SAPReportJob(String pReportName, String pReportDescription, String pDescription) {
        reportName = pReportName;
        shortDescription = pReportDescription;
        description = pDescription;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String pReportName) {
        reportName = pReportName;
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
