/**
* PaymentResponseFile.java
* 
* Copyright (c) 1999-2006 PayCycle, Inc. All Rights Reserved.
* 
* This software is the confidential and proprietary information of
* PayCycle, Inc. ("Confidential Information").  You shall not
* disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with PayCycle.
* 
* PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
* SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
* IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
* PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
* SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
* THIS SOFTWARE OR ITS DERIVATIVES.
* 
* CopyrightVersion 1.0
*/

package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.agency.util.EftpsPaymentBusinessException;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordTemplate;

public class PaymentResponseFile extends EftpsEDIFile implements RecordListener {
    protected static final SpcfLogger logger = SpcfLogManager.getLogger(PaymentResponseFile.class);

    protected EDIRecordTemplate mBtaRecordTemplate;
    protected EDIRecordTemplate mBtiRecordTemplate;
    protected EDIRecordTemplate mTfsRecordTemplate;
    protected int mAcceptCount;
    protected int mRejectCount;
    protected int mRecordCount;
    protected int mTransactionSetId;
    protected int mTransactionId;
    protected PaymentAckType mPaymentAckType;
    protected String mPaymentEftNumber;
    protected String mAgencyResponse;
    protected String mResponseCode;
    protected SystemOwnerType mSystemOwner = SystemOwnerType.PSP;
	protected AckFile mAckFile = null;
    protected EftpsFile mPaymentFile = null;

    public enum PaymentAckType {
        //
        // BTA01 Acknowledgement Type Codes:
        // = AD : Used to acknowledge 813 as processed.
        //        Will receive one EFT Number per Tax Transaction
        //        Used for Bulk and 100K payments (813/BPR01 = C or Z)
        // = AC : Used to ID that the 813 was accepted but one or more transactions were rejected.
        //        Will see EFT number or error number for each tax transaction.
        //        Used for Bulk payments only (813/BPR01 = Z)
        // = AH : Used to acknowledge 813 was accepted for FWT/BEPS/same-day payments.
        //        Will return 5 digit Emergency Reference Number in both BTI08 and TFS04
        //        Will receive one 151/TFS segment for entire 813/ST transaction (even if 813/ST contained multiple TFS segments)
        //        Will only see this code for same-day payments (813/BPR01 = I)
        //        826 file will provide actual Agency Payment IDs for payments in 813
        // = RD : Used to identify rejection of single debit transactions.
        //        Will receive one 151/TFS segment since 813/ST transaction only contained one payment
        //        Will only see this code for 100K payments (813/BPR01 = C)
        // = RJ : Used to identify that an entire 813/ST segment was rejected.
        //        Will receive one 151/TFS segment for entire 813/ST transaction (even if 813/ST contained multiple TFS segments)
        //        Will only see this for wire (FWT/BEPS/same-day) payments (813/BPR01 = I)
        //
        // BTA03:
        // = AB when the Debit processed is different from the original 813 debit amount.
        //   Then BTA04 contains the amount reflecting accepted tax transactions.
        //
        // BTA03 & BTA04 are not used when the debit is processed per the original 813.
        //
        // Examples:
        // - All tax transactions processed as received:                  BTA~AD~981125\
        // - Some Tax transactions rejected, Debit adjusted accordingly:  BTA~AC~981125~AB~45000\
        // - Transactions accepted for Same Day payment:                  BTA~AH~981125\
        // - Individual Debit Rejected:                                   BTA~RD~981125\
        // - Entire 813 ST segment rejected (FWT/BEPS/same-day):          BTA~RJ~981125\
        //
        // =================================================================================================
        //
        // Valid EDI151 file payment acknowledgement types for BTA01:
        //
        // When accepting payments:
        //  - If 151 response is for a bulk next-day file (813/BPR01 = Z), valid 151/BTA01 code is: AD
        //  - If 151 response is for a 100k next-day file (813/BPR01 = C), valid 151/BTA01 code is: AD
        //  - If 151 response is for a BEPS/same-day file (813/BPR01 = I), valid 151/BTA01 code is: AH
        //
        // When rejecting payments:
        //  - If 151 response is for a bulk next-day file (813/BPR01 = Z), valid 151/BTA01 code is: AC
        //  - If 151 response is for a 100k next-day file (813/BPR01 = C), valid 151/BTA01 code is: RD
        //  - If 151 response is for a BEPS/same-day file (813/BPR01 = I), valid 151/BTA01 code is: RJ
        //

        AckWithDetailNoChanges("AD", false),    // Acknowledge, With Detail, No Change (Bulk and 100K)
        AckWithDetailHasChanges("AC", false),   // Acknowledge, With Detail and Change (Bulk only)
        AckHoldStatus("AH", true),              // Acknowledge, Hold Status (BEPS/same-day only)
        RejectWithDetail("RD", false),          // Reject, With Detail (100K only)
        RejectNoDetail("RJ", true);             // Rejected, No Detail (BEPS/same-day only)

        private String mAckCode;
        private boolean mSameDay;

        PaymentAckType(String pAckCode, boolean pSameDay) {
            mAckCode = pAckCode;
            mSameDay = pSameDay;
        }

        public boolean isSameDay() {
            return mSameDay;
        }

        static PaymentAckType getValueByAckCode(String pAckCode) {
            for (PaymentAckType ackType : PaymentAckType.values()) {
                if (ackType.mAckCode.equals(pAckCode)) {
                    return ackType;
                }
            }

            throw new EftpsPaymentBusinessException(String.format("Invalid AckCode specified (%s not supported)", pAckCode));
        }
    }

    public PaymentResponseFile() {
        mBtaRecordTemplate = getRecordTemplate(RecordId.EDI_151_SEG_BTA);
        mBtiRecordTemplate = getRecordTemplate(RecordId.EDI_151_SEG_BTI);
        mTfsRecordTemplate = getRecordTemplate(RecordId.EDI_151_SEG_TFS);

        addReadRecordListener(this);
    }

    @Override
    public EftpsEdiType getEdiFileType() {
        return EftpsEdiType.EDI151;
    }

    @Override
    public EdiFileType getEftpsFileType() {
        return EdiFileType.EftpsPaymentResponse;
    }

    public EftpsFile getPaymentFile() {
        return mPaymentFile;
    }

    public AckFile getAckFile() {
		return mAckFile;
	}

    @Override
    protected int readContent() {
		int count = 0;

        mAcceptCount = 0;
        mRejectCount = 0;
        mRecordCount = 0;

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
        } else if (mAckFile != null) {
            // finally, write the Ack file
            mAckFile.write();

            // set the parent of the Ack EdiFileRecord to this Response file's EdiFileRecord
            getEdiFileRecord().setAckFile(mAckFile.getEdiFileRecord().getEftpsFile());
        }

        // Changing the File Name to encrypted file because the unencrypted file will be deleted after processing.
        EdiFileRecord ediFileRecord = getEdiFileRecord();
        ediFileRecord.getEftpsFile().setFileName(getEdiFileRecord().getEdiFile().getFileName()+".pgp");

        // complete the EftpsFile record
        ediFileRecord.completeRecord();
    }

    public void recordCreated(final RecordTemplate template) {
        if (template == mBtaRecordTemplate) {
            mPaymentAckType = PaymentAckType.getValueByAckCode(template.getFieldValue(FieldId.EDI_151_SEG_BTA01));
        } else if (template == mBtiRecordTemplate) {
            mTransactionSetId = template.getFieldInt(FieldId.EDI_151_SEG_BTI02);
            mPaymentEftNumber = template.getFieldValue(FieldId.EDI_151_SEG_BTI08);
        } else if (template == mTfsRecordTemplate) {
            mTransactionId = template.getFieldInt(FieldId.EDI_151_SEG_TFS02);
            mResponseCode = template.getFieldValue(FieldId.EDI_151_SEG_TFS03);
            mAgencyResponse = template.getFieldValue(FieldId.EDI_151_SEG_TFS04);

            //
            // Update the payment status based on the ack type
            //
            switch (mPaymentAckType) {
                case AckWithDetailNoChanges:
                case AckWithDetailHasChanges:
                case RejectWithDetail:
                    //
                    // These ack types represent individual payment details and can be individually updated
                    // (one TFS segment in the 151 file for each TFS segment sent in the 813 file)
                    //
                    updatePaymentStatus();
                    break;

                case AckHoldStatus:  // same-day payment only
                case RejectNoDetail: // same-day payment only
                    //
                    // These ack types only have one detail record and require bulk handling
                    // (one TFS segment in the 151 file for an entire ST segment sent in the 813 file)
                    //
                    bulkUpdatePaymentStatus(); // BEPS/same-day payment response
                    break;
            }
        } else if (template == getSETrailerTemplate()) {
            updateAckFile();
        }
    }

    private void updateAckFile() {
        if (isAS400InitiatedPayment()) {
            return;
        }

        //
        // We only want to create an ack file if the payment response file we're processing is owned by PSP.
        //

        if (mAckFile == null) {
            mAckFile = new AckFile(getEdiFileType(), getGSHeaderTemplate().getFieldValue(FieldId.EDI_SEG_GS06));
        }

        mAckFile.queueAckToWrite(getEdiFileType(), getSTHeaderTemplate().getFieldValue(FieldId.EDI_SEG_ST02));
    }

    private boolean isAS400InitiatedPayment() {
        //
        // mSystemOwner only represents the most recent payment response record read from the file, but we're
        // guaranteed by the TFA that each EDI151 file will only contain payment responses for the EDI813 file
        // in which the payments were submitted.  In other words, there is a one-to-one correspondence between
        // an EDI813 file and an EDI151 file (every record in the EDI151 file will belong to the same system).
        //
        return SystemOwnerType.AS400.equals(mSystemOwner);
    }

    private void bulkUpdatePaymentStatus() {
        String txnInfo = String.format("(TransactionSetId: %d)", mTransactionSetId);
        logger.info(String.format("Looking up bulk EftpsPaymentDetails for Payment Response transaction %s", txnInfo));

        try {
            DomainEntitySet<EftpsPaymentDetail> paymentDetailList =
                    EftpsPaymentDetail.findPaymentDetailsByTransactionSetId(mTransactionSetId);

            //
            // Update all payment MMTs and payment details for the given transaction set
            //
            for (EftpsPaymentDetail paymentDetail : paymentDetailList) {
                updatePaymentStatus(paymentDetail);
            }
        } catch (Throwable t) {
            // Something is up with the file, so stop processing (file can be reprocessed when fixed)
            String err = String.format("Error processing EFTPS Payment Response %s from %s ",
                                       txnInfo, getDetailedFileName());
            throw new RuntimeException(err, t);
        }
    }

    private void updatePaymentStatus() {
        String txnInfo = String.format("(TransactionSetId: %d, TransactionId: %d)", mTransactionSetId, mTransactionId);
        logger.info(String.format("Looking up individual EftpsPaymentDetail for Payment Response transaction %s", txnInfo));

        try {
            EftpsPaymentDetail paymentDetail =
                    EftpsPaymentDetail.findPaymentDetailByTransactionSetIdAndTransactionId(mTransactionSetId,
                                                                                           mTransactionId);

            //
            // Update payment MMT and payment detail for the given transaction set and transaction id
            //
            updatePaymentStatus(paymentDetail);
        } catch (Throwable t) {
            // Something is up with the file, so stop processing (file can be reprocessed when fixed)
            String err = String.format("Error processing EFTPS Payment Response %s from %s ",
                                       txnInfo, getDetailedFileName());
            throw new RuntimeException(err, t);
        }
    }

    private void updatePaymentStatus(EftpsPaymentDetail pPaymentDetail) {
        TaxPaymentStatus paymentStatus;

        mSystemOwner = pPaymentDetail.getParentFile().getSystemOwner();

        //
        // Values for mResponseCode (TFS03):
        //   * 93 = The payment was accepted (mAgencyResponse and mPaymentEftNumber will contain EFT txn id's)
        //   * 4N = The payment was a same-day payment (mAgencyResponse and mPaymentEftNumber will contain same-day auth number)
        //   * 1Q = The payment was rejected (mAgencyResponse will contain the reject code)
        //

        if ("1Q".equals(mResponseCode)) {
            ++mRejectCount;

            String rejectReason = getRejectReasonMessage(Integer.parseInt(mAgencyResponse));

            pPaymentDetail.setRejectCd(mAgencyResponse);
            pPaymentDetail.setReason(rejectReason);
            paymentStatus = TaxPaymentStatus.RejectedByAgency;

            if (mSystemOwner == SystemOwnerType.PSP) {
                logger.warn(String.format("EFTPS payment rejected by IRS - reason: %s    PaymentDetailSeq: %s",
                                          rejectReason,
                                          pPaymentDetail.getId()));
            } else if (isAS400InitiatedPayment()) {
                logger.warn(String.format("AS400 EFTPS payment rejected by IRS (reason: %s)", rejectReason));
            } else {
                logger.warn(String.format("EFTPS payment rejected by IRS (no MMT, reason: %s)", rejectReason));
            }
        } else {
            ++mAcceptCount;

            pPaymentDetail.setAgencyPaymentId(mAgencyResponse);
            pPaymentDetail.setEftTransactionId(mPaymentEftNumber);
            paymentStatus = TaxPaymentStatus.AcknowledgedByAgency;
        }

        if (pPaymentDetail.getParentFile() != null) {
            mPaymentFile = pPaymentDetail.getParentFile();
        }
        pPaymentDetail.setResponseFile((EftpsFile)getEdiFileRecord().getEftpsFile());
        pPaymentDetail.setResponseDate(PSPDate.getPSPTime());

        //
        // For AS400 initiated payments, just set the payment detail status directly (no MMT or company event).
        // For PSP initiated payments, cascade the status update to the associated MMT and create a company event.
        //

        if (isAS400InitiatedPayment()) {
            pPaymentDetail.setStatusCd(paymentStatus);
            pPaymentDetail.setStatusEffectiveDate(PSPDate.getPSPTime());
        } else {
            // update payment detail status
            //   this status is cascaded to MMT and FT/FTS via stored procedure, stored proc also creates events
            pPaymentDetail.updatePaymentStatus(paymentStatus, false);
        }

        Application.save(pPaymentDetail);

        ++mRecordCount;
    }

    public String getRejectReasonMessage(int pReasonCode) {
        switch (pReasonCode) {
            case 1101:
                return "Duplicate 813";
            case 1102:
                return "813 total dollar amount not equal to sum of tax payment dollar amount";
            case 1103:
                return "813 total dollar amount exceeds maximum";
            case 1104:
                return "Tax type not valid";
            case 1105:
                return "One or more tax type subcategory amounts are negative";
            case 1106:
                return "Tax type is not valid for the taxpayer type";
            case 1107:
                return "Maximum number of tax payments per 813 exceeded";
            case 1108:
                return "Settlement date not valid";
            case 1109:
                return "Maximum number of 813 documents per file exceeded";
            case 1110:
                return "Invalid bulk provider";
            case 1111:
                return "Bulk provider not active";
            case 1112:
                return "DFI account information missing or invalid";
            case 1113:
                return "Taxpayer not active";
            case 1114:
                return "Taxpayer not enrolled";
            case 1115:
                return "Taxpayer not enrolled with bulk provider";
            case 1116:
                return "Settlement date not greater than today or exceeds maximum warehouse period";
            case 1117:
                return "Bulk provider not eligible for same day wire payment";
            case 1118:
                return "Funds transfer amount not greater than zero";
            case 1119:
                return "Missing or invalid 8655 indicator";
            case 1120:
                return "The replacement payments original 5-digit reference number not found";
            case 1121:
                return "Emergency 813's reference number, total amount, or settlement date not matched";
            case 1122:
                return "Zero dollar amount on bulk debit transaction";
            case 1123:
                return "Name control required; cannot be blank";
            case 1125:
                return "Invalid TIN";
            case 1126:
                return "File cannot contain business and individual payments";
            case 1127:
                return "Bulk filer is set up for business processing and individual payments were received";
            case 1128:
                return "Bulk filer is set up for individual processing and business payments were received";
            default:
                return "Unknown";
        }
    }
}
