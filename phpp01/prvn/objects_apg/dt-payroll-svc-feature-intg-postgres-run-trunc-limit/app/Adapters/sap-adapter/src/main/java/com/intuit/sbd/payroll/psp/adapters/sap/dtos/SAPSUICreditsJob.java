package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: 9/27/13
 * Time: 12:27 PM
 */
public class SAPSUICreditsJob {
    private String id;
    private SAPQuarter quarter;
    private String paymentTemplate;
    private String status;
    private Date createdDate;
    private Date modifiedDate;
    private boolean processedFileExists;

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }
    
    public SAPQuarter getQuarter() {
        return quarter;
    }

    public void setQuarter(SAPQuarter pQuarter) {
        quarter = pQuarter;
    }

    public String getPaymentTemplate() {
        return paymentTemplate;
    }

    public void setPaymentTemplate(String pPaymentTemplate) {
        paymentTemplate = pPaymentTemplate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String pStatus) {
        status = pStatus;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date pCreatedDate) {
        createdDate = pCreatedDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date pModifiedDate) {
        modifiedDate = pModifiedDate;
    }

    public boolean isProcessedFileExists() {
        return processedFileExists;
    }
   
    public void setProcessedFileExists(boolean pProcessedFileExists) {
        processedFileExists = pProcessedFileExists;
    }
}

