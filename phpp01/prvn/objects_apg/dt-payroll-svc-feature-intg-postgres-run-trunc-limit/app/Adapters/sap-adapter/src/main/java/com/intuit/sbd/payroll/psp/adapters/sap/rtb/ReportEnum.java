package com.intuit.sbd.payroll.psp.adapters.sap.rtb;




import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPReportJob;


import java.util.ArrayList;

import java.util.HashSet;

import java.util.List;

import java.util.Set;



/**
 * Created by: smodgil on 01/18/20.
 * Description: This is a Enum class meant for Report job.
 */

public enum ReportEnum {


    REPORT_FILE_DOWNLOAD("ReportFileDownload", "AMLReportProcessor", "It will download AML Report",getSupportedRoles("ReportFileDownload"));


    private String id;//must be unique

    private String shortDescription;//short description which will be visible to end users

    private String description;//details technical description for understanding the developer or analyst

    private Set supportedRoles;//list of roles which can access/perform this RTB action

    //operation names
    private static final String DOWNLOAD_REPORT = "ReportFileDownload";


    //Role Ids
    private static final String RM_REP = "RMRep";
    private static final String RM_SUPERVISOR = "RMSupervisor";
    private static final String RM_MANAGER = "RMManager";

    //OperationIds
    private static final String REPORT_DOWNLOAD = "ReportFileDownload";

    ReportEnum(String pId, String pShortDescription, String pDescription, Set pSupportedRoles) {

        id = pId;

        shortDescription = pShortDescription;

        description = pDescription;

        supportedRoles = pSupportedRoles;

    }



    public String getId() {

        return id;

    }



    public void setId(String pId) {

        id = pId;

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


    public Set getSupportedRoles() {

        return supportedRoles;

    }


    public static List<SAPReportJob> getSAPRTBJobListForRole(List<String> roleIds) {

        List<SAPReportJob> saprtbJobList = new ArrayList<SAPReportJob>();

        for (ReportEnum rtbJob : values()) {

            for (String roleId : roleIds){

                if (rtbJob.getSupportedRoles().contains(roleId)){

                    saprtbJobList.add(new SAPReportJob(rtbJob.getId(), rtbJob.getShortDescription(), rtbJob.getDescription()));

                    break;

                }

            }

        }

        return saprtbJobList;

    }


    public static Set getSupportedRoles(String jobId){

        Set supportedRoles = new HashSet<String>();

        if (REPORT_DOWNLOAD.equalsIgnoreCase(jobId)) {

            supportedRoles.add(RM_MANAGER);
            supportedRoles.add(RM_SUPERVISOR);
            supportedRoles.add(RM_REP);
        }

        return supportedRoles;

    }
    

    @Override

    public String toString() {

        return shortDescription;

    }

}

