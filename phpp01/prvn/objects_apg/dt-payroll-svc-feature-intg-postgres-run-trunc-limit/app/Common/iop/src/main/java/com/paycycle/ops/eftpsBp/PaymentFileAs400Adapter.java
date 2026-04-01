package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.agency.util.EdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.edi813.Edi813StSegment;
import com.intuit.sbd.payroll.psp.agency.util.edi813.Edi813TfsSegment;
import com.intuit.sbd.payroll.psp.agency.util.edi813.Edi813TiaSegmentInner;
import com.intuit.sbd.payroll.psp.domain.EdiFileStatus;
import com.intuit.sbd.payroll.psp.domain.SystemOwnerType;
import com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus;

import java.math.BigDecimal;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 17, 2011
 * Time: 4:35:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentFileAs400Adapter extends PaymentFileReader {
    class TaxPaymentInfo {
        BigDecimal mTaxAmount = new BigDecimal("0.00");
        StringBuffer mPaymentDetails = new StringBuffer();

        TaxPaymentInfo(Stack<Edi813TiaSegmentInner> mTiaStack) {
            for (Edi813TiaSegmentInner tiaSegment : mTiaStack) {
                mTaxAmount = mTaxAmount.add(tiaSegment.getTaxAmount());

                if (mPaymentDetails.length() > 0) {
                    mPaymentDetails.append(", ");
                }

                if ("1".equals(tiaSegment.getTaxInfoIdNum())) {
                    mPaymentDetails.append(String.format("FICA: $%,.2f", tiaSegment.getTaxAmount()));
                } else if ("2".equals(tiaSegment.getTaxInfoIdNum())) {
                    mPaymentDetails.append(String.format("MEDICARE: $%,.2f", tiaSegment.getTaxAmount()));
                } else if ("3".equals(tiaSegment.getTaxInfoIdNum())) {
                    mPaymentDetails.append(String.format("FIT: $%,.2f", tiaSegment.getTaxAmount()));
                } else {
                    mPaymentDetails.append(String.format("%s: $%,.2f", tiaSegment.getTaxInfoIdNum(), tiaSegment.getTaxAmount()));
                }
            }
        }

        public String getPaymentDetailString() {
            return mPaymentDetails.toString();
        }
    }

    @Override
    protected int readContent() {
        int count = super.readContent();

        // create the payment detail records for this AS400 payment file
        generatePaymentDetailRecords();

        // set the system owner to AS400
        getEdiFileRecord().setSystemOwner(SystemOwnerType.AS400);

        // set the completion status to PendingTransmission
        getEdiFileRecord().setCompletionStatus(EdiFileStatus.PendingTransmission);

        // Changing the File Name to encrypted file because the unencrypted file will be deleted after processing.
        EdiFileRecord ediFileRecord = getEdiFileRecord();
        ediFileRecord.getEftpsFile().setFileName(getEdiFileRecord().getEdiFile().getFileName()+".pgp");

        // complete the EftpsFile record
        ediFileRecord.completeRecord();

        return count;
    }

    private void generatePaymentDetailRecords() {
        //
        // This method will create the payment detail records for the given EFTPS EDI813 Payments file.  If there is
        // a problem parsing the file, allow any exception to propagate out to stop the file from being sent to the
        // TFA on the assumption that we will need to fix the problem and attempt to send the file again.
        //

        if (mSegmentList.isEmpty()) {
            throw new RuntimeException(String.format("AS400 EFTPS Payments file %s contains no payments.", getFileName()));
        } else {
            for (Edi813StSegment stSegment : getSegmentList().getStStack()) {
                setCurrentStControlNumber(stSegment.getStControlNumber());

                for (Edi813TfsSegment tfsSegment : stSegment.getTfsStack()) {
                    try {
                        TaxPaymentInfo taxInfo = new TaxPaymentInfo(tfsSegment.getTiaStack());

                        // Create the payment detail record
                        getEftpsEdiFileRecord().addPaymentDetailRecord(tfsSegment.getRefSegment().getTransactionId(),
                                                                    tfsSegment.getFedTaxId(),
                                                                    taxInfo.mTaxAmount,
                                                                    TaxPaymentStatus.SentToAgency,
                                                                    taxInfo.getPaymentDetailString(),
                                                                    tfsSegment.getTaxTypeCode(),
                                                                    stSegment.getDtmSegment().getFileSendDate(),
                                                                    stSegment.getBprSegment().getSettlementDate(),
                                                                    tfsSegment.getTaxPeriodEndDate(),
                                                                    stSegment.getBprSegment().getSettlementDate());
                    } catch (Throwable t) {
                        String detail;

                        if (tfsSegment.getRefSegment() == null) {
                            detail = String.format("EIN: %s, FileId: %d, SegmentId: %d, TransactionId: REF is null",
                                                   tfsSegment.getFedTaxId(), getGsControlNumber(), getCurrentStControlNumber());
                        } else {
                            detail = String.format("EIN: %s, FileId: %d, SegmentId: %d, TransactionId: %d",
                                                   tfsSegment.getFedTaxId(), getGsControlNumber(), getCurrentStControlNumber(),
                                                   tfsSegment.getRefSegment().getTransactionId());
                        }

                        throw new RuntimeException(String.format("Error processing AS400 EFTPS payment detail record for file %s [%s]",
                                                  getFileName(), detail), t);
                    }
                }
            }
        }
    }
}
