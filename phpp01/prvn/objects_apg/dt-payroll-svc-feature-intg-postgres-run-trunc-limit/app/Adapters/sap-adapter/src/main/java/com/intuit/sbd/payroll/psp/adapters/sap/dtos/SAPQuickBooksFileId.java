package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: 10/11/12
 * Time: 9:21 AM
 */
public class SAPQuickBooksFileId {
    private String fileId;
    private Date lastDate;

    public SAPQuickBooksFileId(String pFileId, Date pLastDate) {
        fileId = pFileId;
        lastDate = pLastDate;
    }

    public SAPQuickBooksFileId() {
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String pFileId) {
        fileId = pFileId;
    }

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date pLastDate) {
        lastDate = pLastDate;
    }
}
