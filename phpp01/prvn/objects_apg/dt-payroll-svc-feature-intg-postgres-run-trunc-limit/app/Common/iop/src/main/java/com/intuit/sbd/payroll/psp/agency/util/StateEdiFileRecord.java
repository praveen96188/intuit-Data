package com.intuit.sbd.payroll.psp.agency.util;

import com.intuit.sbd.payroll.psp.domain.*;
import com.paycycle.ops.eftpsBp.EdiFile;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 21, 2011
 * Time: 11:27:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class StateEdiFileRecord extends EdiFileRecord {

    public StateEdiFileRecord(EdiFile pEdiFile) {
        mEdiFile = pEdiFile;
        mEdiTaxFile = new StateEdiTaxFile();

        mEdiTaxFile.setFileCode(mEdiFile.getEdiFileType().value());
        mEdiTaxFile.setFileId(mEdiFile.getFileControlNumber());
        mEdiTaxFile.setFileName(mEdiFile.getFileName());
        mEdiTaxFile.setFileType(mEdiFile.getEftpsFileType());
        mEdiTaxFile.setSystemOwner(SystemOwnerType.PSP);

        setRecordStatus(EdiFileStatus.InProcess);

        mAllowDelete = true; // only allow deletion if we've allocated a new EftpsFile record within this instance
    }

    public EdiPaymentDetail addPaymentDetailRecord(MoneyMovementTransaction pMMT, int pGroupId, String pGroupTransactionTime, int pTxnSetId, Date pSettlementDate, String pTxnId) {
        if(mEdiTaxFile instanceof EftpsFile) {
            throw new RuntimeException("Invalid EdiTaxFile to create State Edi tax payment details");
        }
        return EdiPaymentDetail.createPaymentDetail((StateEdiTaxFile) mEdiTaxFile, pMMT, pGroupId, pTxnSetId, pGroupTransactionTime, pSettlementDate, pTxnId);
    }

}
