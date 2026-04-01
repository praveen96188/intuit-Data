package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.agency.util.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 17, 2011
 * Time: 11:28:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class EdiPaymentFile extends StateEDIFile {
    protected static final SpcfLogger logger = SpcfLogManager.getLogger(PaymentFile.class);
    private static final BigDecimal ZERO = new BigDecimal(0.00);

    // retrieve established file/segment boundaries from system parameter table
    protected final int mMaxSegmentsPerFile = EftpsUtil.getEdi813MaxSegmentsPerFile(); //Using this maximum limit from Eftps
    protected final int mMaxTransactionsPerSegment = EftpsUtil.getEdi813MaxTransactionsPerSegment4010();

    protected EDIRecordTemplate mBtiRecordTemplate;
    protected EDIRecordTemplate mDtmRecordTemplate;
    protected EDIRecordTemplate mBprRecordTemplate;

    protected EDIRecordTemplate mN1RecordTemplate;
    protected EDIRecordTemplate mN2RecordTemplate;
    protected EDIRecordTemplate mN3RecordTemplate;
    protected EDIRecordTemplate mN4RecordTemplate;


    protected EDIRecordTemplate mTfsRecordTemplate;

    protected EDIRecordTemplate mRefRecordTemplate;
    protected EDIRecordTemplate mTiaOuterRecordTemplate;
    protected EDIRecordTemplate mTiaInnerRecordTemplate;

    protected Date mSettlementDate;
    protected boolean mAllPaymentsProcessed = false;
    protected int mSuccessfulPaymentCount = 0;
    protected int mSkippedPaymentCount = 0;
    protected int mRecordCount = 0;

    public EdiPaymentFile() {
        mBtiRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_BTI_4010);
        mDtmRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_DTM_4010);
        mBprRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_BPR_4010);
        mTfsRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_TFS_4010);
        mN1RecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_N1_4010);
        mN2RecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_N2_4010);
        mN3RecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_N3_4010);
        mN4RecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_N4_4010);
        mRefRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_REF_4010);
        mTiaOuterRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_OUTER_TIA_4010);
        mTiaInnerRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_INNER_TIA_4010);

        configureForTransmit();

    }

    public int getMaxAllowedPaymentsPerSegment() {
        return mMaxTransactionsPerSegment;
    }

    public int getSuccessfulPaymentCount() {
        return mSuccessfulPaymentCount;
    }

    public int getSkippedPaymentCount() {
        return mSkippedPaymentCount;
    }

    public int getRecordCount() {
        return mRecordCount;
    }

    public boolean allPaymentsProcessed() {
        return mAllPaymentsProcessed;
    }

    @Override
    public EftpsEdiType getEdiFileType() {
        return EftpsEdiType.EDI813;
    }

    @Override
    public EdiFileType getEftpsFileType() {
        return EdiFileType.StateEdiPayment;
    }
   
    /**
     * Generate employer payment records.  Provided DomainEntitySet must be a set of MoneyMovementTransaction.
     */
    @Override
    protected <T extends DataObject> int writeContent(DomainEntitySet<T> pData) {
        mAllPaymentsProcessed = false;
        mSuccessfulPaymentCount = 0;
        mSkippedPaymentCount = 0;
        mRecordCount = 0;

        if (pData != null) {
            startFile();

            EdiPaymentSegmentManager stSegmentManager = new EdiPaymentSegmentManager();
            if(pData.size() > 0) {
                mSettlementDate = CalendarUtils.convertToDate(((MoneyMovementTransaction)pData.get(0)).getSettlementDate());
            }
            ((DomainEntitySet<MoneyMovementTransaction>)pData).sort(MoneyMovementTransaction.DueDate());

            SpcfCalendar segmentDueDate = null;
            for (DataObject obj : pData) {
                try {
                    MoneyMovementTransaction moneyMovementTransaction = (MoneyMovementTransaction) obj;
                    if(segmentDueDate == null) {
                        segmentDueDate = moneyMovementTransaction.getDueDate();
                    } else if(!segmentDueDate.equals(moneyMovementTransaction.getDueDate())) {
                        segmentDueDate = moneyMovementTransaction.getDueDate();
                        stSegmentManager.flushCurrentSegment(false); // flush previous segment and create new segment
                        stSegmentManager = new EdiPaymentSegmentManager();
                    }
                    if (stSegmentManager.addPayment((MoneyMovementTransaction) obj)) {
                        ++mSuccessfulPaymentCount;
                    } else {
                        ++mSkippedPaymentCount;
                    }
                } catch (EftpsFileBoundaryException e) {
                    //
                    // If we've reached the max number of segments for this payment file, stop processing MMTs
                    // (any missed MMTs will be picked up next time through - see EftpsManager.processEDIPayments)
                    //
                    logger.info(String.format("Spec boundary conditions for file %s have been reached " +
                                              "(nominal, finalizing file).", getDetailedFileName()), e);

                    break; // we've maxed this file, so we're done.
                }

                ++mRecordCount;
            }

            //
            // Determine if all payments submitted to this method were processed (skipped records count as processed)
            // (this should only be false if we hit a boundary exception)
            //
            mAllPaymentsProcessed = (mRecordCount == pData.size());

            // Flush the current segment to ensure last segment is written to file
            stSegmentManager.flushCurrentSegment(false);

            endFile();
        }

        return mSuccessfulPaymentCount;
    }

    /*****************************************************************************************************************/
    /*                                                                                                               */
    /* Helper classes to write the payment segment(s)                                                                */
    /*                                                                                                               */
    /*****************************************************************************************************************/

    /**
     * This helper class manages the creation and population of the ST segments within the 813 file - 004010
     */
    class EdiPaymentSegmentManager {
        private BigDecimal mSegmentTotal;
        private BankAccount mSegmentDebitAccount;

        List<MoneyMovementTransaction> mSegmentTaxPayments = new ArrayList<MoneyMovementTransaction>();

        EdiPaymentSegmentManager() {
            reset();
        }

        public void reset() {
            mSegmentTotal = ZERO;
            mSegmentDebitAccount = null;
            mSegmentTaxPayments.clear();

            // need to set the ST control number here so the payment detail record will have it available
            setCurrentStControlNumber(EftpsUtil.getNewSegmentControlNumber());
        }

        public void flushCurrentSegment(boolean pPrepForNextSegment) {
            writePaymentSegment();

            if (pPrepForNextSegment) {
                reset();
            }
        }

        private void writePaymentSegment() {
            //
            // If we have no payments to write, just return
            //
            if (mSegmentTaxPayments.isEmpty()) {
                return;
            }

            startTransactionSet(getCurrentStControlNumber());
            writeSegmentHeader();
            writeSegmentPayments();
            endTransactionSet();
        }

        private void writeSegmentHeader() {
            //
            // Write the ST segment's payment header
            //
            try {
                String segmentTotal = mSegmentTotal.toString();
                String settlementDate = EftpsUtil.formatLongDate(mSettlementDate);

                // write BTI segment
                mBtiRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BTI05, EftpsUtil.formatLongDate(getCreateDate()));
                mBtiRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BTI08, EftpsUtil.getConfigString("psp_edi_sender_tin")); // Intuit TIN   
                mBtiRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BTI10, EftpsUtil.getConfigString("psp_edi_account_number"));
                writeTransaction(mBtiRecordTemplate);

                // write DTM segment
                mDtmRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_DTM01, "174");  //Month ending
                SpcfCalendar lastDayOfTheQuarter = CalendarUtils.getLastDayOfQuarter(CalendarUtils.convertToSpcfCalendar(mSettlementDate));
                mDtmRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_DTM02, EftpsUtil.formatLongDate(new Date(lastDayOfTheQuarter.getTimeInMilliseconds())));
                writeTransaction(mDtmRecordTemplate);

                // write BPR segment
                mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR02, segmentTotal); // sum of tax payments for the ST segment (a/k/a transaction)
                mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR12, "01"); // (DFI) ID Number Qualifier
                mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR13, mSegmentDebitAccount.getRoutingNumber()); // (DFI) Identification Number (RTN)
                mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR14, "DA"); // Account Number Qualifier (checking/savings)
                mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR15, mSegmentDebitAccount.getAccountNumber()); // Account Number
                mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR16, settlementDate); // ACH Settlement Date
                writeTransaction(mBprRecordTemplate);

                //Write N1 segment
                mN1RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N102, EftpsUtil.getConfigString("psp_edi_sender_name"));
                writeTransaction(mN1RecordTemplate);

                // If first line address is more than 35, continue remaining address to N2
                //Write N2 segment

                //Write N3 segment
                String addressLine = EftpsUtil.getConfigString("psp_edi_sender_addressLine1");//
                if(addressLine.length() > 35) {
                    mN3RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N301, addressLine.substring(0, 35)); // if length is more than 35, continue to EDI_813_SEG_N302 field
                    mN3RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N302, addressLine.substring(35, addressLine.length() > 70 ? 70 : addressLine.length()));
                } else {
                    mN3RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N301, addressLine);
                }
                writeTransaction(mN3RecordTemplate);

                //Write N4 segment
                mN4RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N401, EftpsUtil.getConfigString("psp_edi_sender_city"));
                mN4RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N402, EftpsUtil.getConfigString("psp_edi_sender_state"));
                String zipCode = EftpsUtil.getConfigString("psp_edi_sender_zip");
                if(zipCode.length() == 5) {
                    zipCode = zipCode + "0000";
                }
                mN4RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N403, zipCode);
                writeTransaction(mN4RecordTemplate);

            } catch (Throwable t) {
                throw new EftpsBpRuntimeException("Error writing EDI segment header. ", t);
            }
        }

        private void writeSegmentPayments() {
            try {
                for (MoneyMovementTransaction payment : mSegmentTaxPayments) {
                    //Write TFS segment
                    mTfsRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_TFS02, payment.getPaymentFrequency().getTaxCodeId()); // Tax Type code
                    mTfsRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_TFS04, StringUtil.stripNonNumeric(payment.getCompany().getFedTaxId()));    // FEIN
                    writeTransaction(mTfsRecordTemplate);

                    //Write REF Segment - 3
                    mRefRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_REF01, "9X");
                    String depositFreqCd = null;
                    if(payment.getPaymentFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY)) {
                        depositFreqCd = "M";
                    } else if(payment.getPaymentFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY)) {
                        depositFreqCd = "Q";
                    }
                    mRefRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_REF02, depositFreqCd);
                    writeTransaction(mRefRecordTemplate);

                    mRefRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_REF01, "FI");
                    mRefRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_REF02, "E01");
                    writeTransaction(mRefRecordTemplate);

                    mRefRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_REF01, "ZZ");
                    mRefRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_REF02, "N"); // Check this if resubmitting
                    writeTransaction(mRefRecordTemplate);
                    
                    //Write DTM Segment - 2
                    mDtmRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_DTM01, "193");  //Period beginning
                    mDtmRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_DTM02, EftpsUtil.formatLongDate(CalendarUtils.convertToDate(payment.getPaymentPeriodBegin())));
                    writeTransaction(mDtmRecordTemplate);

                    mDtmRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_DTM01, "194");  //Period ending
                    mDtmRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_DTM02, EftpsUtil.formatLongDate(CalendarUtils.convertToDate(payment.getPaymentPeriodEnd())));
                    writeTransaction(mDtmRecordTemplate);

                    //Write N1 segment
                    String companyLegalName = payment.getCompany().getLegalName();
                    if(companyLegalName.length() > 35) {
                        mN1RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N102, companyLegalName.substring(0, 35));
                        writeTransaction(mN1RecordTemplate);

                        // If first line address is more than 35, continue remaining address to N2
                        //Write N2 segment
                        mN2RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N201, companyLegalName.substring(35, companyLegalName.length() > 70 ? 70 : companyLegalName.length()));
                        writeTransaction(mN2RecordTemplate);
                    } else {
                        mN1RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N102, companyLegalName);
                        writeTransaction(mN1RecordTemplate);
                    }

                    //Write N3 segment
                    String addressLine = payment.getCompany().getLegalAddress().getAddressLine1();
                    if(addressLine.length() > 35) {
                        mN3RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N301, addressLine.substring(0, 35)); // if length is more than 35, continue to EDI_813_SEG_N302 field
                        mN3RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N302, addressLine.substring(35, addressLine.length() > 70 ? 70 : addressLine.length()));
                    } else {
                        mN3RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N301, addressLine);
                    }
                    writeTransaction(mN3RecordTemplate);

                    //Write N4 segment
                    mN4RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N401, payment.getCompany().getLegalAddress().getCity());
                    mN4RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N402, payment.getCompany().getLegalAddress().getState());
                    String zipCode = payment.getCompany().getLegalAddress().getFullZipCode().replace("-","");
                    if(zipCode.length() == 5) {
                        zipCode = zipCode + "0000";
                    }
                    mN4RecordTemplate.setFieldValue(FieldId.EDI_813_SEG_N403, zipCode);
                    writeTransaction(mN4RecordTemplate);

                    //Write TIA Segments - 10
                    mTiaOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_TIA01, "3");
                    mTiaOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_TIA03, "N"); //Address change indicator
                    writeTransaction(mTiaOuterRecordTemplate);

                    mTiaOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_TIA01, "4");
                    mTiaOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_TIA03, "N"); //Final return indicator
                    writeTransaction(mTiaOuterRecordTemplate);

                    mTiaOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_TIA01, "89");
                    mTiaOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_TIA03, "N"); //Additional return indicator
                    writeTransaction(mTiaOuterRecordTemplate);

                    mTiaOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_TIA01, "91");
                    mTiaOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_TIA03, "N"); //Amended Return indicator
                    writeTransaction(mTiaOuterRecordTemplate);

                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA01, "9");
                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA02, payment.getMoneyMovementTransactionAmount().toString()); //Income tax withheld 1
                    writeTransaction(mTiaInnerRecordTemplate);

                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA01, "97");
                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA02, "0.00"); //Credit Line - 2
                    writeTransaction(mTiaInnerRecordTemplate);

                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA01, "31");
                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA02, payment.getMoneyMovementTransactionAmount().toString()); //Net tax due
                    writeTransaction(mTiaInnerRecordTemplate);

                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA01, "76");
                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA02, "0.00"); //Penalty - Line 4
                    writeTransaction(mTiaInnerRecordTemplate);

                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA01, "77");
                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA02, "0.00"); //Interest - Line 5
                    writeTransaction(mTiaInnerRecordTemplate);

                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA01, "81");
                    mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA02, payment.getMoneyMovementTransactionAmount().toString()); //Total Due - Line 6
                    writeTransaction(mTiaInnerRecordTemplate);

                }
            } catch (Throwable t) {
                throw new EftpsBpRuntimeException("Error writing EDI segment payments. ", t);
            }
        }

        private void assertSegmentBoundaryConditions() {

            // Ensure the current file/segment can accommodate the payment
            //
            // The rules for flushing the current segment:
            //   - If the number of TFS segments is already at the maximum for this ST segment
            //   - Then flush the current segment and begin a new one (if appropriate)

            if (mSegmentTaxPayments.size() == getMaxAllowedPaymentsPerSegment()) {
                //
                // Since we're flushing the current segment (and attempting to add a new one):
                // If the segment being flushed is the last segment for this file due to the boundary condition
                // mMaxSegmentsPerFile, throw an EftpsFileBoundaryException exception (caller should handle this
                // exception by flushing the current segment and ending the file).
                //
                // We want to throw this exception prior to flushing the current segment since the caller
                // should be performing the final flush.
                //
                // Use (mMaxSegmentsPerFile - 1) as the limit check since the final flush will be flushing the
                // boundary segment (e.g. after which getCurrentTransactionSetCount() == mMaxSegmentsPerFile).
                //
                if (getCurrentTransactionSetCount() == (mMaxSegmentsPerFile - 1)) {
                    throw new EftpsFileBoundaryException(
                            String.format("EDI spec allows no more than %d ST Segments per %s file.",
                                          mMaxSegmentsPerFile, getEdiFileType().name()));
                }

                //
                // Go ahead and flush the segment
                //
                flushCurrentSegment(true);
            }
        }

        private void updatePaymentStatus(MoneyMovementTransaction pMMT) {
            // Update the edi payment status (also updates MMT status and associated FT statuses)
            MoneyMovementTransaction mmt = pMMT.updateTaxPaymentStatus(TaxPaymentStatus.SentToAgency, false, false);

            // Create the edi payment detail
            getStateEdiFileRecord().addPaymentDetailRecord(mmt, getGsControlNumber(), getGroupTxnTime(), getCurrentStControlNumber(), mSettlementDate, StringUtil.stripNonNumeric(pMMT.getCompany().getFedTaxId()));
        }

        public boolean addPayment(MoneyMovementTransaction pMMT) {
            Company company = pMMT.getCompany();

            try {
                BigDecimal mmtAmount = SpcfUtils.convertToBigDecimal(pMMT.getMoneyMovementTransactionAmount());

                // Check to ensure MMT amount is a positive dollar amount
                if (mmtAmount.compareTo(ZERO) < 0) {

                    // If payment is negative dollar amount, log an error (skip payment and move on to next)
                    String msg = String.format("MoneyMovementTransaction amount for company %s:%s is < $0.00 " +
                                               "(MMT id: %s, MMT amount: $%,.2f).",
                                               company.getSourceSystemCd(),
                                               company.getSourceCompanyId(),
                                               pMMT.getId(),
                                               mmtAmount);

                    throw new EftpsPaymentInvalidAmountException(msg);
                }

                //
                // Ensure the current file/segment can accommodate the payment
                //
                assertSegmentBoundaryConditions();


                //
                // Update the segment info properties (only once per segment)
                //
                if (mSegmentTaxPayments.isEmpty()) {
                    mSegmentDebitAccount = pMMT.getLiabilityDebitBankAccount();
                }

                mSegmentTaxPayments.add(pMMT);

                //
                // Update the segment total
                //
                mSegmentTotal = mSegmentTotal.add(mmtAmount);

                //
                // Update the payment status (also creates the eftps payment detail record and company event)
                //
                updatePaymentStatus(pMMT);

                return true;

            } catch (EftpsFileBoundaryException e) {
                //
                // For EftpsFileBoundaryExceptions, we can't write any more payments to this file.
                // Just re-throw this exception to allow the caller to deal with the condition.
                //
                throw e;
            } catch (EftpsPaymentBusinessException e) {
                //
                // For EftpsPaymentBusinessExceptions, we just want to skip this payment and move on to the next.
                //
                logger.error(String.format("Error writing tax payment to EDI Payment file %s (payment skipped). ",
                                           getDetailedFileName()), e);
            } catch (EftpsPaymentInvalidAmountException e) {
                //
                // For EftpsPaymentInvalidAmountException, we just want to skip this payment and move on to the next.
                //
                logger.error("Tax payment skipped due to invalid MMT amount. ", e);

                //Cancel ATOA FTs if we could not process MMT because amount < 0
                try{
                    pMMT.setStatus(PaymentStatus.Created);
                    pMMT.cancelAgencyTaxOverpaymentAppliedTransactions();
                }catch(Throwable t){
                    logger.error(String.format("Unable to cancel AgencyTaxOverPaymentApplied for company %s:%s " +
                                               "(MMT id: %s).",
                                               company.getSourceSystemCd(),
                                               company.getSourceCompanyId(),
                                               pMMT.getId()), t);
                }

            } catch (Throwable t) {
                //
                // All other exceptions mean that the file is incomplete/corrupt and we cannot proceed.
                //
                throw new RuntimeException(String.format("Error writing EDI Payments to file %s. ",
                                                         getDetailedFileName()), t);
            }

            return false;
        }
    }

}
