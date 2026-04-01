package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: Dec 27, 2010
 * Time: 9:08:51 PM
 */
public class SAPPaycheck401k {
    private boolean voidedAfterTOKOffload;
    private boolean deletedAfterTOKOffload;
    private Date dateSentToTOK;
    private String tokStatus;

    public boolean isVoidedAfterTOKOffload() {
        return voidedAfterTOKOffload;
    }

    public void setVoidedAfterTOKOffload(boolean voidedAfterTOKOffload) {
        this.voidedAfterTOKOffload = voidedAfterTOKOffload;
    }

    public boolean isDeletedAfterTOKOffload() {
        return deletedAfterTOKOffload;
    }

    public void setDeletedAfterTOKOffload(boolean deletedAfterTOKOffload) {
        this.deletedAfterTOKOffload = deletedAfterTOKOffload;
    }

    public Date getDateSentToTOK() {
        return dateSentToTOK;
    }

    public void setDateSentToTOK(Date dateSentToTOK) {
        this.dateSentToTOK = dateSentToTOK;
    }

    public String getTokStatus() {
        return tokStatus;
    }

    public void setTokStatus(String tokStatus) {
        this.tokStatus = tokStatus;
    }
}
