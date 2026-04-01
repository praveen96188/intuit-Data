package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: 11/12/12
 * Time: 5:31 PM
 */
public class SAPLedgerOperationJob {
    private String id;
    private Date uploadTime;
    private Date startTime;
    private Date finishTime;
    private String status;
    private String type;
    private int totalRecords;
    private int processedRecords;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date pUploadTime) {
        uploadTime = pUploadTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date pStartTime) {
        startTime = pStartTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date pFinishTime) {
        finishTime = pFinishTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String pStatus) {
        status = pStatus;
    }

    public String getType() {
        return type;
    }

    public void setType(String pType) {
        type = pType;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int pTotalRecords) {
        totalRecords = pTotalRecords;
    }

    public int getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(int pProcessedRecords) {
        processedRecords = pProcessedRecords;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }
}
