package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.agency.util.EdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.domain.EdiFileType;
import com.intuit.sbd.payroll.psp.domain.EftpsPaymentDetail;
import com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus;
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
 * User: kpaul
 * Date: Jan 11, 2011
 * Time: 6:10:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentConfirmationFile extends EftpsEDIFile implements RecordListener {
    protected static final SpcfLogger logger = SpcfLogManager.getLogger(PaymentConfirmationFile.class);

    protected EDIRecordTemplate mBtiRecordTemplate;
    protected EDIRecordTemplate mTfsRecordTemplate;
    protected int mRecordCount;
    protected int mTransactionId;
    protected String mAgencyPaymentId;
    protected String mSameDayAckNumber;
    protected AckFile mAckFile = null;

    public PaymentConfirmationFile() {
        mBtiRecordTemplate = getRecordTemplate(RecordId.EDI_826_SEG_BTI);
        mTfsRecordTemplate = getRecordTemplate(RecordId.EDI_826_SEG_TFS);

        addReadRecordListener(this);
    }

    @Override
    public EftpsEdiType getEdiFileType() {
        return EftpsEdiType.EDI826;
    }

    @Override
    public EdiFileType getEftpsFileType() {
        return EdiFileType.EftpsPaymentConfirmation;
    }

    public AckFile getAckFile() {
		return mAckFile;
	}

    @Override
    protected int readContent() {
		int count = 0;

        mRecordCount = 0;

        // read the ISA header record
        readRecord(getISAHeaderTemplate());

        // read the GS header record
        readRecord(getGSHeaderTemplate());

        mAckFile = new AckFile(getEdiFileType(), getGSHeaderTemplate().getFieldValue(FieldId.EDI_SEG_GS06));

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

            // read the BTI record
            readRecord(mBtiRecordTemplate);

            while (true) {
                String tfsSeg = peekNextSegmentCode();

                if (tfsSeg == null) {
                    // We hit EOF.  But where's the SE, GE, IEA???
                    // Throw an exception, since the file is incomplete.
                    throw new EftpsBpRuntimeException("Incomplete Payment Confirmation file. Expected TFS or SE segment, found EOF.");
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

        // finally, write the Ack file
        mAckFile.write();

        // set the parent of the Ack EdiFileRecord to this Confirmation file's EdiFileRecord
        getEdiFileRecord().setAckFile(mAckFile.getEdiFileRecord().getEftpsFile());

        // Changing the File Name to encrypted file because the unencrypted file will be deleted after processing.
        EdiFileRecord ediFileRecord = getEdiFileRecord();
        ediFileRecord.getEftpsFile().setFileName(getEdiFileRecord().getEdiFile().getFileName()+".pgp");

        // complete the EftpsFile record
        ediFileRecord.completeRecord();

		return count;
	}

    public void recordCreated(final RecordTemplate template) {
        if (template == mBtiRecordTemplate) {
            mAgencyPaymentId = template.getFieldValue(FieldId.EDI_826_SEG_BTI02); // TFS04 from 151
        } else if (template == mTfsRecordTemplate) {
            mTransactionId = template.getFieldInt(FieldId.EDI_826_SEG_TFS02); // REF02 from Original Transaction
            mSameDayAckNumber = template.getFieldValue(FieldId.EDI_826_SEG_TFS04);

            updatePaymentStatus();
        } else if (template == getSETrailerTemplate()) {
            // since we're being notified about the SE record, we know the ST record has already been read.
            mAckFile.queueAckToWrite(getEdiFileType(), getSTHeaderTemplate().getFieldValue(FieldId.EDI_SEG_ST02));
        }
    }

    private void updatePaymentStatus() {
        logger.info(String.format("Looking up EftpsPaymentDetail for Payment Confirmation transaction " +
                                  "(TransactionId: %d, AgencyPaymentId %s)", mTransactionId, mAgencyPaymentId));

        try {
            EftpsPaymentDetail paymentDetail =
                    EftpsPaymentDetail.findPaymentDetailByTransactionIdAndAgencyPaymentId(mTransactionId, mAgencyPaymentId);

            ++mRecordCount;

            paymentDetail.setSameDayAckNumber(mSameDayAckNumber);

            // update the status last since it will also create the company event
            paymentDetail.updatePaymentStatus(TaxPaymentStatus.AcknowledgedByAgency, false);

            Application.save(paymentDetail);
        } catch (Throwable t) {
            // This transaction could not be found
            // Log it and keep going since we don't want to interfere with other company responses.
            String txnInfo = String.format("TransactionId: %d, AgencyPaymentId %s", mTransactionId, mAgencyPaymentId);
            logger.error(String.format("Error processing EFTPS Payment Confirmation (%s) from %s ", txnInfo, getDetailedFileName()), t);
        }
    }
}
