package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: mwaqarbaig
 * Date: Jan 13, 2011
 * Time: 5:40:32 PM
 */
public class SAPEnrollmentFile {
    private Date mCreatedDate;

    private String mFileId;

    private String type;

    public Date getCreatedDate() {
        return mCreatedDate;
    }

    public void setCreatedDate(Date pCreatedDate) {
        mCreatedDate = pCreatedDate;
    }

    public String getFileId() {
        return mFileId;
    }

    public void setFileId(String pFileID) {
        mFileId = pFileID;
    }

    public String getType() {
        return type;
    }

    public void setType(String pType) {
        type = pType;
    }
}
