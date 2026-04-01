package com.intuit.sbd.payroll.psp.agency.util;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EdiFileStatus;
import com.paycycle.ops.eftpsBp.EdiFile;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Nov 20, 2010
 * Time: 12:48:03 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class EdiFileRecord {
    protected EdiFileStatus mCompletionStatus = EdiFileStatus.Completed;
    protected EdiTaxFile mEdiTaxFile;
    protected EdiFile mEdiFile;
    protected boolean mAllowDelete = false;

    public boolean getAllowDelete() {
        return mAllowDelete;
    }

    public void setAllowDelete(final boolean pAllowDelete) {
        mAllowDelete = pAllowDelete;
    }

    public void delete() {
        if (mAllowDelete && (mEdiTaxFile != null)) {
            mEdiTaxFile.cascadeDelete();
            mEdiTaxFile = null;
        }
    }

    public EdiTaxFile getEftpsFile() {
        return mEdiTaxFile;
    }

    public EdiFile getEdiFile() {
        return mEdiFile;
    }

    public void completeRecord() {
        setRecordStatus(mCompletionStatus);
    }

    public void setCompletionStatus(EdiFileStatus pCompletionStatus) {
        mCompletionStatus = pCompletionStatus;
    }

    public void setSystemOwner(SystemOwnerType pOwner) {
        mEdiTaxFile.setSystemOwner(pOwner);
    }

    public void setRecordStatus(EdiFileStatus pStatus) {
        mEdiTaxFile.setRecordStatus(pStatus);
    }

    public void setAckFile(EdiTaxFile pAckFile) {
        mEdiTaxFile.setAcknowledgementFile(pAckFile);
    }

    public SystemOwnerType getSystemOwner() {
        return mEdiTaxFile.getSystemOwner();
    }

}
