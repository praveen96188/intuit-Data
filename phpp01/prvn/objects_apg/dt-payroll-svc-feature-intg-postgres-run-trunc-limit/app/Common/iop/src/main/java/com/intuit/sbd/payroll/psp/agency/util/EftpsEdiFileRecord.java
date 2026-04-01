package com.intuit.sbd.payroll.psp.agency.util;

import com.intuit.sbd.payroll.psp.domain.*;
import com.paycycle.ops.eftpsBp.EdiFile;

import java.math.BigDecimal;
import java.util.Date;

/**
 * User: ihannur
 * Date: Oct 21, 2011
 * Time: 11:27:33 AM
 */
public class EftpsEdiFileRecord extends EdiFileRecord {
    
    public EftpsEdiFileRecord(EdiFile pEdiFile) {
        mEdiFile = pEdiFile;
        mEdiTaxFile = new EftpsFile();

        mEdiTaxFile.setFileCode(mEdiFile.getEdiFileType().value());
        mEdiTaxFile.setFileId(mEdiFile.getFileControlNumber());
        mEdiTaxFile.setFileName(mEdiFile.getFileName());
        mEdiTaxFile.setFileType(mEdiFile.getEftpsFileType());
        mEdiTaxFile.setSystemOwner(SystemOwnerType.PSP);

        setRecordStatus(EdiFileStatus.InProcess);

        mAllowDelete = true; // only allow deletion if we've allocated a new EftpsFile record within this instance
    }

    public void setFileSubtype(EftpsFileSubtype pEftpsFileSubtype) {
        ((EftpsFile) mEdiTaxFile).setFileSubtype(pEftpsFileSubtype);
    }

    public EftpsEnrollmentDetail addOrUpdateEnrollmentDetailRecord(EftpsEnrollment pEnrollment, int pTransactionId) {
        if (pEnrollment.getSecondary()) {
            EftpsEnrollmentDetail enrollmentDetail = pEnrollment.findEnrollmentDetail();
            EftpsEnrollmentDetail.updateEnrollmentDetail((EftpsFile) mEdiTaxFile, enrollmentDetail, mEdiFile.getGsControlNumber(),
                                                                mEdiFile.getCurrentStControlNumber(), pTransactionId);
            return enrollmentDetail;
        } else {
            return EftpsEnrollmentDetail.createEnrollmentDetail((EftpsFile) mEdiTaxFile, pEnrollment, mEdiFile.getGsControlNumber(),
                                                                mEdiFile.getCurrentStControlNumber(), pTransactionId);
        }
    }

    public EftpsPaymentDetail addPaymentDetailRecord(MoneyMovementTransaction pMMT, int pTransactionId,
                                                     String pPaymentDetails, String pTaxTypeCode,
                                                     Date pPeriodEndDate, Date pSettlementDate) {
        return EftpsPaymentDetail.createPaymentDetail((EftpsFile) mEdiTaxFile, pMMT, mEdiFile.getGsControlNumber(),
                                                      mEdiFile.getCurrentStControlNumber(), pTransactionId,
                                                      pPaymentDetails, pTaxTypeCode, pPeriodEndDate, pSettlementDate);
    }

    public EftpsPaymentDetail addPaymentDetailRecord(int pTxnId, String pFedTaxId, BigDecimal pPaymentAmount,
                                                     TaxPaymentStatus pPaymentStatus, String pPaymentDetails,
                                                     String pTaxTypeCode, Date pInitiationDate, Date pPaymentDueDate,
                                                     Date pPeriodEndDate, Date pSettlementDate) {
        return EftpsPaymentDetail.createPaymentDetail((EftpsFile) mEdiTaxFile, mEdiFile.getGsControlNumber(),
                                                      mEdiFile.getCurrentStControlNumber(), pTxnId, pFedTaxId,
                                                      pPaymentAmount, pPaymentStatus, pPaymentDetails, pTaxTypeCode,
                                                      pInitiationDate, pPaymentDueDate, pPeriodEndDate, pSettlementDate);
    }

}
