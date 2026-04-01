/**
* PaymentReturnFile.java
*
* Copyright (c) 2007 PayCycle, Inc. All Rights Reserved.
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
import com.intuit.sbd.payroll.psp.agency.util.EdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordTemplate;

/**
 * Processes Financial Return files from EFTPS.
 * These are received after Payment Responses, and indicate that
 * a previously confirmed payment has been returned by the
 * financial institution.  For example, for insufficient funds.
 */
public class PaymentReturnFile extends EftpsEDIFile implements RecordListener {	
    protected static final SpcfLogger logger = SpcfLogManager.getLogger(PaymentReturnFile.class);

    protected EDIRecordTemplate mRicRecordTemplate;
    protected EDIRecordTemplate mRefRecordTemplate;
    protected String mErrorCode;
    protected String mPaymentEftNumber;
    protected String mNOCData;
    protected String mReturnCode;
    protected int mReturnCount;
    protected int mReturnPaidCount;
    protected int mReturnUnpaidCount;
	protected AckFile mAckFile = null;
    protected boolean mAs400PaymentReturnsFound = false;

    public PaymentReturnFile() {
        mRicRecordTemplate = getRecordTemplate(RecordId.EDI_827_SEG_RIC);
        mRefRecordTemplate = getRecordTemplate(RecordId.EDI_827_SEG_REF);

        addReadRecordListener(this);
    }

    @Override
    public EftpsEdiType getEdiFileType() {
        return EftpsEdiType.EDI827;
    }

    @Override
    public EdiFileType getEftpsFileType() {
        return EdiFileType.EftpsPaymentReturn;
    }

	public AckFile getAckFile() {
		return mAckFile;
	}

    @Override
    protected int readContent() {
		int count = 0;
        String peekSeg;

        mReturnCount = 0;
        mReturnPaidCount = 0;
        mReturnUnpaidCount = 0;
        mAs400PaymentReturnsFound = false;

        // read the ISA header record
        readRecord(getISAHeaderTemplate());

        // read the GS header record
        readRecord(getGSHeaderTemplate());

        mAckFile = new AckFile(getEdiFileType(), getGSHeaderTemplate().getFieldValue(FieldId.EDI_SEG_GS06));

		while (true) {
            peekSeg = peekNextSegmentCode();

            if (peekSeg.startsWith("GE")) {
                break; // There are no more transaction sets in the file, so we're done.
            }

            if (!peekSeg.startsWith("ST")) {
                throw new EftpsBpRuntimeException("Expecting ST segment, found: " + peekSeg);
            }

            // read the ST header record
            readRecord(getSTHeaderTemplate());

            // read the RIC segment
            readRecord(mRicRecordTemplate);

            // read the REF segment(s)
            while (peekNextSegmentCode().startsWith("REF")) {
                readRecord(mRefRecordTemplate);
            }
			
            // read the SE trailer record (also processes the return and writes the ack info in notify handler)
            readRecord(getSETrailerTemplate());

			++count;
		}

        // read the GE trailer record
        readRecord(getGETrailerTemplate());

        // read the IEA trailer record
        readRecord(getIEATrailerTemplate());

        // finally, write the Ack file
        mAckFile.write();

        // set the parent of the Ack EdiFileRecord to this Response file's EdiFileRecord
        getEdiFileRecord().setAckFile(mAckFile.getEdiFileRecord().getEftpsFile());

        // Changing the File Name to encrypted file because the unencrypted file will be deleted after processing.
        EdiFileRecord ediFileRecord = getEdiFileRecord();
        ediFileRecord.getEftpsFile().setFileName(getEdiFileRecord().getEdiFile().getFileName()+".pgp");

        // complete the EftpsFile record
        ediFileRecord.completeRecord();

		return count;
	}

    public void recordCreated(final RecordTemplate template) {
        if (template == mRicRecordTemplate) {
            mErrorCode = template.getFieldValue(FieldId.EDI_827_SEG_RIC01);
            mPaymentEftNumber = null;
            mReturnCode = null;
            mNOCData = null;
        } else if (template == mRefRecordTemplate) {
            String recType = template.getFieldValue(FieldId.EDI_827_SEG_REF01);

            //
            // Values for recType (REF01):
            //   * 93 = REF02 contains the EFT number
            //   * 1Q = REF02 contains the ACH return code (Rxx or Cxx)
            //   * ZZ = REF02 contains the NOC data that caused the return
            //

            if ("93".equals(recType)) {
                //
                // Get EFT number
                //
                mPaymentEftNumber = template.getFieldValue(FieldId.EDI_827_SEG_REF02);
            } else if ("1Q".equals(recType)) {
                //
                // Get ACH return reason
                //
                mReturnCode = template.getFieldValue(FieldId.EDI_827_SEG_REF02);
            } else if ("ZZ".equals(recType)) {
                //
                // Get NOC data
                //
                mNOCData = template.getFieldValue(FieldId.EDI_827_SEG_REF02);
            }
        } else if (template == getSETrailerTemplate()) {
            processPaymentReturn();

            // since we're being notified about the SE record, we know the ST record has already been read.
            mAckFile.queueAckToWrite(getEdiFileType(), getSTHeaderTemplate().getFieldValue(FieldId.EDI_SEG_ST02));
        }
    }

    private void processPaymentReturn() {
        logger.info(String.format("Looking up EftpsPaymentDetail for Payment Return transaction " +
                                  "(Payment EFT Transaction Id: %s)", mPaymentEftNumber));

        try {
            EftpsPaymentDetail paymentDetail = EftpsPaymentDetail.findPaymentDetailByEFTTransactionId(mPaymentEftNumber);
            TaxPaymentStatus paymentStatus;

            ++mReturnCount;

            //
            // Values for mErrorCode (RIC01):
            //   * 830 = The payment was returned and remains unpaid (tax is still due)
            //   * 011 = The payment was returned but the tax was paid (typical for NOCs)
            //

            if ("830".equals(mErrorCode)) {
                ++mReturnUnpaidCount;
                paymentStatus = TaxPaymentStatus.ReturnedTaxNotPaid;
            } else {
                ++mReturnPaidCount;
                paymentStatus = TaxPaymentStatus.ReturnedTaxPaid;
            }

            ReturnReasonDesc returnReasonDesc = Application.findById(ReturnReasonDesc.class, ACHReturnReason.valueOf(mReturnCode));

            if (returnReasonDesc != null) {
                String reason = returnReasonDesc.getDescription();

                if ((reason == null) || (reason.length() == 0)) {
                    reason = "Unknown";
                }

                if ((mNOCData != null) && (mNOCData.length() > 0)) {
                    reason = String.format("%s (NOC info: %s)", reason, mNOCData);
                }

                paymentDetail.setReason(reason);
                paymentDetail.setReturnCd(returnReasonDesc.getReasonCd());
            }

            paymentDetail.setReturnFile((EftpsFile)getEdiFileRecord().getEftpsFile());

            // update the status last since it will also create the company event
            paymentDetail.cascadePaymentStatus(paymentStatus);

            Application.save(paymentDetail);

            if(!mAs400PaymentReturnsFound && paymentDetail.getParentFile().isAS400File()) {
                mAs400PaymentReturnsFound = true;
            }
        } catch (Throwable t) {
            // This transaction could not be found
            // Log it and keep going since we don't want to interfere with other company responses.
            String txnInfo = String.format("Payment EFT Transaction Id: %s", mPaymentEftNumber);
            String err = String.format("Error processing EFTPS Payment Return (%s) from %s ", txnInfo, getDetailedFileName());
            logger.error(err, t);
            throw new RuntimeException(err,t);
        }
    }

    public boolean as400PaymentReturnsFound() {
        return mAs400PaymentReturnsFound;
    }
}
