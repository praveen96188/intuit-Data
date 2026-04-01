package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.agency.util.EdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 26, 2011
 * Time: 10:06:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class EdiPaymentResponseFile extends StateEDIFile implements RecordListener {

    protected static final SpcfLogger logger = SpcfLogManager.getLogger(EdiPaymentResponseFile.class);

    protected EDIRecordTemplate mBtaRecordTemplate;
    protected EDIRecordTemplate mBtiRecordTemplate;
    protected EDIRecordTemplate mTfsRecordTemplate;
    protected EDIRecordTemplate mRefOuterRecordTemplate;
    protected EDIRecordTemplate mRefInnerRecordTemplate;
    protected EDIRecordTemplate mPbiRecordTemplate;
    protected int mAcceptCount;
    protected int mRejectCount;
    protected int mRecordCountFoundInPsp;
    protected int mTotalRecordCount;
    private int mPbiCounter; 
    protected int mTransactionSetId;
    protected String mTransactionId;
    protected String mErrorCd;
    protected String mErrorMessage;
    protected String mConfirmationNumber;
    private SpcfCalendar mResponseDate; 
    private int mGroupId;
    private String mGroupTxnTime; 
    protected SystemOwnerType mSystemOwner = SystemOwnerType.PSP;
    protected StateEdiTaxFile mPaymentFile = null;

    // BTA01 Acknowledgement Type Codes:
    // = AE : Used to acknowledge 813 as processed.
    //
    // TFS04, REF102 and REF202 are used to identify the unique tax payment
    //
    // TFS04:
    // = Tax account number (TIN)
    //
    // REF102 Transmission Number
    // (ISA13 + GS05) from 813 file, File Id and GS transaction time
    //
    // REF202 Transaction Number
    // (ST02) from 813 file, segment Id
    //
    //PBI segments are used to identify rejections/confirmations
    // PBI02 Action code:
    // = U : Reject
    // = CF : Confirmation
    // = WQ : Accept
    //
    // Examples:
    // - Tax transaction processed successfully :    PBI~000000~CF~Confirmation~12345678\
    // - Tax transaction accepted with error :       PBI~100020~WQ~The Period Begin Date is empty~12345678\
    // - Tax transaction rejected :                  PBI~000001~U~FATAL  ERROR.  No Banking Information In Transmission.  Entire Transmission Rejected~\
    //
    // =================================================================================================
    //

    public EdiPaymentResponseFile() {
        mBtaRecordTemplate = getRecordTemplate(RecordId.EDI_151_SEG_BTA_4010);
        mBtiRecordTemplate = getRecordTemplate(RecordId.EDI_151_SEG_BTI_4010);
        mTfsRecordTemplate = getRecordTemplate(RecordId.EDI_151_SEG_TFS_4010);
        mRefOuterRecordTemplate = getRecordTemplate(RecordId.EDI_151_SEG_OUTER_REF_4010);
        mRefInnerRecordTemplate = getRecordTemplate(RecordId.EDI_151_SEG_INNER_REF_4010);
        mPbiRecordTemplate = getRecordTemplate(RecordId.EDI_151_SEG_PBI_4010);    

        addReadRecordListener(this);
    }

    @Override
    public EftpsEdiType getEdiFileType() {
        return EftpsEdiType.EDI151;
    }

    @Override
    public EdiFileType getEftpsFileType() {
        return EdiFileType.StateEdiPaymentResponse;
    }

    public StateEdiTaxFile getPaymentFile() {
        return mPaymentFile;
    }

    @Override
    protected int readContent() {
		int count = 0;

        mAcceptCount = 0;
        mRejectCount = 0;
        mTotalRecordCount = 0;
        mRecordCountFoundInPsp = 0;

        // read the ISA header record
        readRecord(getISAHeaderTemplate());

        // read the GS header record
        readRecord(getGSHeaderTemplate());

		while (true) {
            String stSeg = peekNextSegmentCode();

            if ((stSeg == null) || (stSeg.startsWith("GE"))) {
                // There are no more transaction sets in the file.
                break;
            }

            if (!stSeg.startsWith("ST")) {
                throw new EftpsBpRuntimeException("Expecting ST segment, found: " + stSeg);
            }

            // read the ST header record
            readRecord(getSTHeaderTemplate());

            // read the BTA segment
            readRecord(mBtaRecordTemplate);

            // read the BTI record
            readRecord(mBtiRecordTemplate);

            while (true) {
                String tfsSeg = peekNextSegmentCode();

                if (tfsSeg == null) {
                    // We hit EOF.  But where's the SE, GE, IEA???
                    // Throw an exception, since the file is incomplete.
                    throw new EftpsBpRuntimeException("Incomplete Payment Response file. " +
                                                      "Expected TFS or SE segment, found EOF.");
                }

                if (tfsSeg.startsWith("SE")) {
                    // read the SE trailer record (also writes the ack info in notify handler)
                    readRecord(getSETrailerTemplate());
                    break;
                }

                // read the TFS record
                count += readRecord(mTfsRecordTemplate);
                readRecord(mRefOuterRecordTemplate);
                readRecord(mRefOuterRecordTemplate);
                readRecord(mRefInnerRecordTemplate);
                readRecord(mRefInnerRecordTemplate);

                while(true) {
                    String pbiSeg = peekNextSegmentCode();

                    if (pbiSeg == null) {
                        // We hit EOF.  But where's the SE, GE, IEA???
                        // Throw an exception, since the file is incomplete.
                        throw new EftpsBpRuntimeException("Incomplete Payment Response file. " +
                                                          "Expected PBI or TFS or SE segment, found EOF.");
                    }

                    if(pbiSeg.startsWith("PBI")) {
                        readRecord(mPbiRecordTemplate);
                    } else {
                        break;
                    }
                }
            }
		}

        // read the GE trailer record
        readRecord(getGETrailerTemplate());

        // read the IEA trailer record
        readRecord(getIEATrailerTemplate());

        // take care of any final details related to this file read operation
        finalizeRead();

		return count;
	}

    private void finalizeRead() {
        if (isAS400InitiatedPayment()) {
            getEdiFileRecord().setSystemOwner(SystemOwnerType.AS400);
            getEdiFileRecord().setCompletionStatus(EdiFileStatus.SendToAS400);
        } else {
            // Changing the File Name to encrypted file because the unencrypted file will be deleted after processing.
            EdiFileRecord ediFileRecord = getEdiFileRecord();
            ediFileRecord.getEftpsFile().setFileName(getEdiFileRecord().getEdiFile().getFileName()+".pgp");

            // complete the EftpsFile record
            ediFileRecord.completeRecord();

            if(mTotalRecordCount > 0 && mRecordCountFoundInPsp > 0 && mTotalRecordCount != mRecordCountFoundInPsp) {
                logger.error(String.format("EDI File %s, has responses %d for the payments sent from PSP and payments %d are not found in PSP", getFileName(), mRecordCountFoundInPsp, mTotalRecordCount - mRecordCountFoundInPsp));
            }
            return;

        }

        // Changing the File Name to encrypted file because the unencrypted file will be deleted after processing.
        EdiFileRecord ediFileRecord = getEdiFileRecord();
        ediFileRecord.getEftpsFile().setFileName(getEdiFileRecord().getEdiFile().getFileName()+".pgp");

        // complete the EftpsFile record
        ediFileRecord.completeRecord();
    }

    private void reset() {
        mTransactionId = null;
        mGroupId = 0;
        mGroupTxnTime = null;
        mTransactionSetId = 0;
        mErrorCd = null;
        mErrorMessage = null;
        mConfirmationNumber = null;
        mPbiCounter = 0;
    }

    public void recordCreated(final RecordTemplate template) {
        if (template == mBtaRecordTemplate) {
            mResponseDate = CalendarUtils.convertToSpcfCalendar(EftpsUtil.getDateFromLongDateString(template.getFieldValue(FieldId.EDI_151_SEG_BTA02)));
        } else if (template == mTfsRecordTemplate) {
            reset();
            mTransactionId = template.getFieldValue(FieldId.EDI_151_SEG_TFS04);
            ++mTotalRecordCount; // Total payment records are counted on TFS records
        } else if (template == mRefOuterRecordTemplate) {
            String referenceQualifier = template.getFieldValue(FieldId.EDI_151_SEG_OUTER_REF01);
            if("FI".equals(referenceQualifier)){
                String fileIdentifier = template.getFieldValue(FieldId.EDI_151_SEG_OUTER_REF02);
                if(fileIdentifier == null || fileIdentifier.length() != 15) {
                    throw new EftpsBpRuntimeException("REF1 expected 15 digit file identifier, whereas file identifier found is : " + fileIdentifier);
                }
                mGroupId = Integer.parseInt(fileIdentifier.substring(0, 9));
                mGroupTxnTime = fileIdentifier.substring(9);
            } else if ("TN".equals(referenceQualifier)) {
                mTransactionSetId = template.getFieldInt(FieldId.EDI_151_SEG_OUTER_REF02);
            }
        } else if(template == mPbiRecordTemplate) {
            mErrorCd = template.getFieldValue(FieldId.EDI_151_SEG_PBI01);
            mErrorMessage = template.getFieldValue(FieldId.EDI_151_SEG_PBI03);
            mConfirmationNumber = template.getFieldValue(FieldId.EDI_151_SEG_PBI04);
            //Update transaction based on PBI02 code
            mPbiCounter++;  // To keep track of multiple PBI segments, payment records counters are updated only for one PBI record in TFS loop 
            updatePaymentStatus(template.getFieldValue(FieldId.EDI_151_SEG_PBI02));
        }
    }

    private boolean isAS400InitiatedPayment() {
        // we're not sure how the responses are included in each EDI151 file, To make sure EDI151 will only contain payment responses for the EDI813 file
        // in which the payments were submitted.  In other words, there is a one-to-one correspondence between
        // an EDI813 file and an EDI151 file (every record in the EDI151 file will belong to the same system).

        if(mTotalRecordCount == mRecordCountFoundInPsp) {
            return false;
        }
        
        if(mTotalRecordCount > 0 && mRecordCountFoundInPsp == 0) {
            return true;
        }

        return false;
    }

    private void updatePaymentStatus(String pActionCode) {
        String txnInfo = String.format("(TransactionSetId: %d, TransactionId: %s, GroupId: %d, GroupTxnTime: %s)", mTransactionSetId, mTransactionId, mGroupId, mGroupTxnTime);
        logger.info(String.format("Looking up individual EdiPaymentDetail for Payment Response transaction %s", txnInfo));

        try {
            EdiPaymentDetail paymentDetail = EdiPaymentDetail.findPaymentDetailByTxnInfo(mTransactionSetId, mTransactionId, mGroupId, mGroupTxnTime);
            if(paymentDetail != null) {

                // Update payment MMT and payment detail for the given transaction set and transaction id
                updatePaymentStatus(pActionCode, paymentDetail);
                if(mPbiCounter == 1) {
                    mRecordCountFoundInPsp++;
                }
            }
        } catch (Throwable t) {
            // Something is up with the file, so stop processing (file can be reprocessed when fixed)
            String err = String.format("Error processing EFTPS Payment Response %s from %s ",
                                       txnInfo, getDetailedFileName());
            throw new RuntimeException(err, t);
        }
    }

    private void updatePaymentStatus(String pActionCode, EdiPaymentDetail pPaymentDetail) {
        TaxPaymentStatus paymentStatus;

        mSystemOwner = pPaymentDetail.getParentFile().getSystemOwner();

        //
        // Values for REF02:
        // PBI02 Action code:
        // = U : Reject
        // = CF : Confirmation
        // = WQ : Accept
        //

        if ("U".equals(pActionCode)) {
            if(mPbiCounter == 1) {
                ++mRejectCount;
            }

            pPaymentDetail.setErrorCd(mErrorCd);
            pPaymentDetail.setErrorMessage(mErrorMessage);
            paymentStatus = TaxPaymentStatus.RejectedByAgency;

            if (mSystemOwner == SystemOwnerType.PSP) {
                logger.warn(String.format("EDI payment rejected by MS - ErrorCd: %s, ErrorMessage: %s,  PaymentDetailSeq: %s",
                                          mErrorCd, mErrorMessage, pPaymentDetail.getId()));
            } else {
                logger.warn(String.format("EDI payment rejected by MS (no MMT, ErrorCd: %s, ErrorMessage: %s)", mErrorCd, mErrorMessage));
            }
        } else {
            if(mPbiCounter == 1) {
                ++mAcceptCount;
            }

            pPaymentDetail.setErrorCd(mErrorCd);
            pPaymentDetail.setErrorMessage(mErrorMessage);
            pPaymentDetail.setConfirmationNumber(mConfirmationNumber);
            if("WQ".equals(pActionCode)) {
                paymentStatus = TaxPaymentStatus.ReturnedTaxPaid;
            } else {
                paymentStatus = TaxPaymentStatus.AcknowledgedByAgency;
            }
        }

        if (pPaymentDetail.getParentFile() != null) {
            mPaymentFile = pPaymentDetail.getParentFile();
        }
        pPaymentDetail.setResponseFile((StateEdiTaxFile)getEdiFileRecord().getEftpsFile());
        pPaymentDetail.setResponseDate(mResponseDate); // PSPDate.getPSPTime() 

        // update payment detail status
        //   this status is cascaded to MMT and FT/FTS via stored procedure, stored proc also creates events
        pPaymentDetail.updatePaymentStatus(paymentStatus, false);

        Application.save(pPaymentDetail);

    }
    
}
