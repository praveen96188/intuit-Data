/**
 * PaymentFile.java
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

import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.agency.util.EftpsFileBoundaryException;
import com.intuit.sbd.payroll.psp.agency.util.EftpsPaymentBusinessException;
import com.intuit.sbd.payroll.psp.agency.util.EftpsPaymentInvalidAmountException;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.util.DateUtil;
import com.paycycle.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The PaymentFile class represents the business-specific EFTPS
 * file with enrollment specifics added
 */
public class PaymentFile extends EftpsEDIFile {
    protected static final SpcfLogger logger = SpcfLogManager.getLogger(PaymentFile.class);
    private static final BigDecimal ZERO = new BigDecimal(0.00);

    // retrieve established file/segment boundaries from system parameter table
    protected final int mMaxSegmentsPerFile = EftpsUtil.getEdi813MaxSegmentsPerFile();
    protected final int mMaxTransactionsPerSegment = EftpsUtil.getEdi813MaxTransactionsPerSegment();
    protected final BigDecimal mMaxAchAmountPerSegment = EftpsUtil.getEdi813MaxAchAmountPerSegment();

    protected EDIRecordTemplate mBtiRecordTemplate;
    protected EDIRecordTemplate mDtmRecordTemplate;
    protected EDIRecordTemplate mBprRecordTemplate;
    protected EDIRecordTemplate mTfsRecordTemplate;
    protected EDIRecordTemplate mFgsRecordTemplate;
    protected EDIRecordTemplate mTiaOuterRecordTemplate;
    protected EDIRecordTemplate mTiaInnerRecordTemplate;
    protected EDIRecordTemplate mRefOuterRecordTemplate;
    protected EDIRecordTemplate mRefInnerRecordTemplate;
    protected PaymentFileMode mFileMode;
    protected String mBepsReferenceNumber;
    protected Date mSettlementDate;
    protected boolean mAllPaymentsProcessed = false;
    protected int mSuccessfulPaymentCount = 0;
    protected int mSkippedPaymentCount = 0;
    protected int mRecordCount = 0;

    public enum TaxTypeCode {
        TTC940("09405", "IRS-940-FILING"), // Employer's Annual Federal Unemployment (FUTA) Tax Return (form 940)
        TTC941("94105", "IRS-941-FILING"), // Employer�s Quarterly Federal Tax Return (form 941)
        TTC944("94405", "IRS-944-FILING"); // Employer�s Annual Federal Tax Return (form 944)

        private String mTaxTypeCode;
        private String mFormTemplateCode;

        TaxTypeCode(String pTaxTypeCode, String pFormTemplateCode) {
            mTaxTypeCode = pTaxTypeCode;
            mFormTemplateCode = pFormTemplateCode;
        }

        @Override
        public String toString() {
            return mTaxTypeCode;
        }

        static TaxTypeCode getValueByFormTemplateCode(String pFormTemplateCode) {
            for (TaxTypeCode code : TaxTypeCode.values()) {
                if (code.mFormTemplateCode.equals(pFormTemplateCode)) {
                    return code;
                }
            }

            throw new EftpsPaymentBusinessException(
                    String.format("Invalid FormTemplateCode specified (%s not supported)",
                                  pFormTemplateCode));
        }
    }

    public enum PaymentFileMode {
        PFM_NEXT_DAY, // Normal next-day payment file (Intuit debit)
        PFM_SAME_DAY, // Emergency same-day payment file (no money movement)
        PFM_100K      // 100k payment file (Client debit)
    }

    public PaymentFile() {
        this(PaymentFileMode.PFM_NEXT_DAY);
    }

    public PaymentFile(PaymentFileMode pFileMode) {
        mBtiRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_BTI);
        mDtmRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_DTM);
        mBprRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_BPR);
        mTfsRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_TFS);
        mFgsRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_FGS);
        mTiaOuterRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_OUTER_TIA);
        mTiaInnerRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_INNER_TIA);
        mRefOuterRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_OUTER_REF);
        mRefInnerRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_INNER_REF);

        //
        // Set the settlement date for tax payments within this file
        // (we use a system parameter to define the business days to offset in case we need to adjust - default is 1)
        //
        SpcfCalendar settlementDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(settlementDate, EftpsUtil.getEftpsSettlementDateOffset());
        mSettlementDate = CalendarUtils.convertToDate(settlementDate);

        setFileMode(pFileMode);

        configureForTransmit();
    }

    @Override
    public void configureForTransmit() {
        super.configureForTransmit();
        // override setting file to PendingTransmission status; the stored procedure prc_eftps_payments_sent is responsible
        if (getEdiFileRecord().getSystemOwner() == SystemOwnerType.PSP) {
            getEdiFileRecord().setCompletionStatus(EdiFileStatus.InProcess);
        }

        EftpsFileSubtype fileSubtype = EftpsFileSubtype.None;
        if (mFileMode == PaymentFileMode.PFM_SAME_DAY) {
            fileSubtype = EftpsFileSubtype.PaymentSameDay;
        } else if (mFileMode == PaymentFileMode.PFM_NEXT_DAY) {
            fileSubtype = EftpsFileSubtype.PaymentNextDay;
        } else if (mFileMode  == PaymentFileMode.PFM_100K) {
            fileSubtype = EftpsFileSubtype.Payment100k;
        }
        getEftpsEdiFileRecord().setFileSubtype(fileSubtype);

    }

    public int getMaxAllowedPaymentsPerSegment() {
        return mMaxTransactionsPerSegment;
    }

    public BigDecimal getMaxDollarAmountPerSegment() {
        return mMaxAchAmountPerSegment;
    }

    protected void setFileMode(final PaymentFileMode pFileMode) {
        mFileMode = pFileMode;
    }

    public String getBepsReferenceNumber() {
        return mBepsReferenceNumber;
    }

    /**
     * A BEPS payment file is used for emergency same-day payments to the TFA
     *
     * @param pBepsReferenceNumber
     * @param pBepsSettlementDate
     */
    public void configureForBepsPayment(String pBepsReferenceNumber, SpcfCalendar pBepsSettlementDate) {
        mBepsReferenceNumber = pBepsReferenceNumber;
        mSettlementDate = CalendarUtils.convertToDate(pBepsSettlementDate);
        setFileMode(PaymentFileMode.PFM_SAME_DAY);
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
        return EdiFileType.EftpsPayment;
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

            EftpsPaymentSegmentManager stSegmentManager = new EftpsPaymentSegmentManager();

            for (DataObject obj : pData) {
                try {
                    if (stSegmentManager.addPayment((MoneyMovementTransaction) obj)) {
                        ++mSuccessfulPaymentCount;
                    } else {
                        ++mSkippedPaymentCount;
                    }
                } catch (EftpsFileBoundaryException e) {
                    //
                    // If we've reached the max number of segments for this payment file, stop processing MMTs
                    // (any missed MMTs will be picked up next time through - see EftpsManager.processPayments)
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
     * This class represents an individual tax payment for a client.
     */
    abstract class EftpsTaxPayment {
        protected TaxTypeCode mTaxTypeCode;
        protected String mCompanyTIN;
        protected Date mPeriodEndReferenceDate;
        protected int mPaymentReferenceNum;

        EftpsTaxPayment(TaxTypeCode pTaxTypeCode, String pCompanyTIN, Date pPeriodEndReferenceDate) {
            mTaxTypeCode = pTaxTypeCode;
            mCompanyTIN = pCompanyTIN;
            mPeriodEndReferenceDate = pPeriodEndReferenceDate;
            mPaymentReferenceNum = EftpsUtil.getNewPaymentReferenceNumber();
        }

        abstract public String getPaymentDetailString();

        public void writePayment() {
            // write TFS segment
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_TFS02, mTaxTypeCode.toString());
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_TFS06, StringUtil.stripNonNumeric(mCompanyTIN));
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_TFS07, EftpsUtil.formatShortDate(getPeriodEndDate()));
            writeTransaction(mTfsRecordTemplate);

            // write (inner) REF segment
            mRefInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_REF02, EftpsUtil.formatRefNum(mPaymentReferenceNum));
            writeTransaction(mRefInnerRecordTemplate);

            // write FGS segment
            writeTransaction(mFgsRecordTemplate);
        }

        public TaxTypeCode getTaxTypeCode() {
            return mTaxTypeCode;
        }

        public int getPaymentReferenceNum() {
            return mPaymentReferenceNum;
        }

        /**
         * Form 941 is quarterly, forms 940 and 944 are annual
         *
         * @return The period end date
         */
        public Date getPeriodEndDate() {
            switch (mTaxTypeCode) {
                case TTC941:
                    return DateUtil.getLastDayOfQuarter(mPeriodEndReferenceDate);

                case TTC940:
                case TTC944:
                    return DateUtil.getLastDayOfYear(mPeriodEndReferenceDate);

                default:
                    throw new EftpsPaymentBusinessException(String.format("Unsupported/Unknown TaxTypeCode: %s",
                                                                          mTaxTypeCode.toString()));
            }
        }
    }

    /**
     * This class represents an individual FIT/FICA/MEDICARE tax payment for a client.
     */
    class EftpsItemizedPayment extends EftpsTaxPayment {
        BigDecimal mFitAmount;
        BigDecimal mFicaAmount;
        BigDecimal mMedAmount;

        EftpsItemizedPayment(TaxTypeCode pTaxTypeCode, String pCompanyEIN, Date pPeriodEndReferenceDate,
                             BigDecimal pFitAmount, BigDecimal pFicaAmount, BigDecimal pMedAmount) {
            super(pTaxTypeCode, pCompanyEIN, pPeriodEndReferenceDate);
            mFitAmount = pFitAmount;
            mFicaAmount = pFicaAmount;
            mMedAmount = pMedAmount;
        }

        public String getPaymentDetailString() {
            StringBuffer detailString = new StringBuffer();
            String sep = "";

            if (mFicaAmount.compareTo(ZERO) > 0) {
                detailString.append(String.format("FICA: $%,.2f", mFicaAmount));
                sep = ", ";
            }

            if (mMedAmount.compareTo(ZERO) > 0) {
                detailString.append(String.format("%sMEDICARE: $%,.2f", sep, mMedAmount));
                sep = ", ";
            }

            if (mFitAmount.compareTo(ZERO) > 0) {
                detailString.append(String.format("%sFIT: $%,.2f", sep, mFitAmount));
            }

            return detailString.toString();
        }

        @Override
        public void writePayment() {
            super.writePayment();

            //
            // Write (inner) TIA segments (we're guaranteed that at least one of these amounts will be > 0)
            //

            // write the FICA tax payment (if > 0)
            if (mFicaAmount.compareTo(ZERO) > 0) {
                mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA01, "1");
                mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA02, EftpsUtil.formatStripDecimal(mFicaAmount));
                writeTransaction(mTiaInnerRecordTemplate);
            }

            // write the MEDICARE tax payment (if > 0)
            if (mMedAmount.compareTo(ZERO) > 0) {
                mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA01, "2");
                mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA02, EftpsUtil.formatStripDecimal(mMedAmount));
                writeTransaction(mTiaInnerRecordTemplate);
            }

            // write the FIT tax payment (if > 0)
            if (mFitAmount.compareTo(ZERO) > 0) {
                mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA01, "3");
                mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA02, EftpsUtil.formatStripDecimal(mFitAmount));
                writeTransaction(mTiaInnerRecordTemplate);
            }
        }
    }

    /**
     * This class represents an individual FUTA/COBRA tax payment for a client.
     */
    class EftpsConsolidatedPayment extends EftpsTaxPayment {
        BigDecimal mTaxAmount;

        EftpsConsolidatedPayment(TaxTypeCode pTaxTypeCode, String pCompanyEIN, Date pPeriodEndReferenceDate, BigDecimal pTaxAmount) {
            super(pTaxTypeCode, pCompanyEIN, pPeriodEndReferenceDate);
            mTaxAmount = pTaxAmount;
        }

        public String getPaymentDetailString() {
            String paymentDetail;

            switch (mTaxTypeCode) {
                case TTC940:
                    paymentDetail = String.format("FUTA: $%,.2f", mTaxAmount);
                    break;

                default:
                    paymentDetail = String.format("FIT/FICA/MED (COBRA): $%,.2f", mTaxAmount);
                    break;
            }

            return paymentDetail;
        }

        @Override
        public void writePayment() {
            super.writePayment();

            //
            // Write (inner) TIA segment
            //

            // write the consolidated tax payment
            mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA01, mTaxTypeCode.toString());
            mTiaInnerRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_INNER_TIA02, EftpsUtil.formatStripDecimal(mTaxAmount));
            writeTransaction(mTiaInnerRecordTemplate);
        }
    }

    /**
     * This helper class manages the creation and population of the ST segments within the 813 file
     */
    class EftpsPaymentSegmentManager {
        private BigDecimal mSegmentTotal;
        private BankAccount mSegmentDebitAccount;
        List<EftpsTaxPayment> mSegmentTaxPayments = new ArrayList<EftpsTaxPayment>();

        EftpsPaymentSegmentManager() {
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
                String segmentTotal = EftpsUtil.formatStripDecimal(mSegmentTotal);
                String settlementDate = EftpsUtil.formatShortDate(mSettlementDate);
                String accountType;

                //
                // Determine the EFTPS Account Number Qualifier
                //
                if (BankAccountType.Savings.equals(mSegmentDebitAccount.getAccountTypeCd())) {
                    accountType = "SG"; // EFTPS Account Number Qualifier for savings account
                } else {
                    accountType = "03"; // EFTPS Account Number Qualifier for checking account
                }

                //
                // write BTI segment
                //
                mBtiRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BTI05, EftpsUtil.formatShortDate(getCreateDate()));
                mBtiRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BTI08, EftpsUtil.getConfigString("psp_eftps_sender_id"));
                writeTransaction(mBtiRecordTemplate);

                //
                // write DTM segment
                //
                mDtmRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_DTM02, EftpsUtil.formatShortDate(getCreateDate()));
                writeTransaction(mDtmRecordTemplate);

                //
                // write (outer) TIA segment
                //
                mTiaOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_TIA02, segmentTotal);
                writeTransaction(mTiaOuterRecordTemplate);

                //
                // write (outer) REF segment
                //
                mRefOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_REF01, "VU"); // Verification Number
                mRefOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_REF02, "8655");
                writeTransaction(mRefOuterRecordTemplate);

                //
                // The BPR segment has different requirements for same-day and next-day/100K files
                //
                switch (mFileMode) {
                    case PFM_SAME_DAY:
                        // write second (outer) REF segment for same-day (requires a second REF segment with CK)
                        mRefOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_REF01, "CK"); // Criticality Designator
                        mRefOuterRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_OUTER_REF02, mBepsReferenceNumber);
                        writeTransaction(mRefOuterRecordTemplate);

                        // write BPR segment (BEPS/same-day, money already moved via FWT)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR01, "I"); // Remittance Information Only (BEPS/emergency same-day payments)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR02, segmentTotal); // sum of tax payments for the ST segment (a/k/a transaction)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR03, "C"); // Credit/Debit Flag (not really used since FWT, but TFA example shows C)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR04, "FWT"); // Federal Reserve Wire Transfer
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR12, ""); // clear field for same-day
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR13, ""); // clear field for same-day
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR14, ""); // clear field for same-day
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR15, ""); // clear field for same-day
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR16, settlementDate); // Settlement Date
                        break;

                    case PFM_100K:
                        // write BPR segment (100K next-day)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR01, "C"); // Payment accompanies Remittance Advice (individual debits - 100K)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR02, segmentTotal); // sum of tax payments for the ST segment (a/k/a transaction)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR03, "D"); // Credit/Debit Flag
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR04, "ACH"); // Automated Clearing House (normal/next-day payments)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR12, "01"); // (DFI) ID Number Qualifier
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR13, mSegmentDebitAccount.getRoutingNumber()); // (DFI) Identification Number (RTN)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR14, accountType); // Account Number Qualifier (checking/savings)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR15, mSegmentDebitAccount.getAccountNumber()); // Account Number
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR16, settlementDate); // ACH Settlement Date
                        break;

                    case PFM_NEXT_DAY:
                    default:
                        // write BPR segment (bulk next-day)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR01, "Z"); // Bulk Debit (normal next-day payments)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR02, segmentTotal); // sum of tax payments for the ST segment (a/k/a transaction)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR03, "D"); // Credit/Debit Flag
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR04, "ACH"); // Automated Clearing House (normal/next-day payments)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR12, "01"); // (DFI) ID Number Qualifier
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR13, mSegmentDebitAccount.getRoutingNumber()); // (DFI) Identification Number (RTN)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR14, accountType); // Account Number Qualifier (checking/savings)
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR15, mSegmentDebitAccount.getAccountNumber()); // Account Number
                        mBprRecordTemplate.setFieldValue(FieldId.EDI_813_SEG_BPR16, settlementDate); // ACH Settlement Date
                        break;
                }

                //
                // write BPR segment
                //
                writeTransaction(mBprRecordTemplate);
            } catch (Throwable t) {
                throw new EftpsBpRuntimeException("Error writing EFTPS segment header. ", t);
            }
        }

        private void writeSegmentPayments() {
            try {
                for (EftpsTaxPayment payment : mSegmentTaxPayments) {
                    payment.writePayment();
                }
            } catch (Throwable t) {
                throw new EftpsBpRuntimeException("Error writing EFTPS segment payments. ", t);
            }
        }

        private TaxTypeCode getTaxTypeCode(MoneyMovementTransaction pMMT) {
            FormTemplate formTemplate = pMMT.getFilingForm();

            if (formTemplate == null) {
                String msg = String.format("Unable to determine payment FormTemplate for company %s:%s (MMT id: %s).",
                                           pMMT.getCompany().getSourceSystemCd(),
                                           pMMT.getCompany().getSourceCompanyId(),
                                           pMMT.getId());
                throw new EftpsPaymentBusinessException(msg);
            }

            return TaxTypeCode.getValueByFormTemplateCode(formTemplate.getFormTemplateCd());
        }

        private void assertSegmentBoundaryConditions(BigDecimal pTaxPaymentAmount) {
            //
            // Ensure that a single payment cannot exceed the max dollar amount for this segment
            //
            if (pTaxPaymentAmount.compareTo(getMaxDollarAmountPerSegment()) == 1) {
                throw new EftpsPaymentBusinessException(
                        String.format("Tax payment amount too large for EFTPS ST segment " +
                                      "(Tax payment amount: $%,.2f, Max allowed: $%,.2f)",
                                      pTaxPaymentAmount, getMaxDollarAmountPerSegment()));

            }

            //
            // Ensure the current file/segment can accommodate the payment
            //
            // The rules for flushing the current segment:
            // - If the current PaymentFileMode is *not* PFM_100K:
            //   - If the number of TFS segments is already at the maximum for this ST segment OR
            //   - If we would exceed the maximum dollar amount for this ST segment
            //   - Then flush the current segment and begin a new one (if appropriate)
            // - Else If the current PaymentFileMode *is* PFM_100K:
            //   - If the mSegmentTaxPayments list is not empty (each segment contains only 1 payment)
            //   - Then flush the current segment and begin a new one (if appropriate)
            //
            if ((PaymentFileMode.PFM_100K.equals(mFileMode) && !mSegmentTaxPayments.isEmpty()) ||
                (mSegmentTaxPayments.size() == getMaxAllowedPaymentsPerSegment()) ||
                (mSegmentTotal.add(pTaxPaymentAmount).compareTo(getMaxDollarAmountPerSegment()) == 1)) {
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
                            String.format("EFTPS spec allows no more than %d ST Segments per %s file.",
                                          mMaxSegmentsPerFile, getEdiFileType().name()));
                }

                //
                // Go ahead and flush the segment
                //
                flushCurrentSegment(true);
            }
        }

        private void updatePaymentStatus(MoneyMovementTransaction pMMT, EftpsTaxPayment pTaxPayment) {
            //
            // Update the eftps payment status (also updates MMT status and associated FT statuses)
            //
            MoneyMovementTransaction mmt = pMMT.updateTaxPaymentStatus(TaxPaymentStatus.SentToAgency, false, false);

            //
            // Create the eftps payment detail
            //
            getEftpsEdiFileRecord().addPaymentDetailRecord(mmt,
                                                        pTaxPayment.getPaymentReferenceNum(),
                                                        pTaxPayment.getPaymentDetailString(),
                                                        pTaxPayment.getTaxTypeCode().toString(),
                                                        pTaxPayment.getPeriodEndDate(),
                                                        mSettlementDate);
        }

        public boolean addPayment(MoneyMovementTransaction pMMT) {
            boolean paymentProcessed = false;
            Company company = pMMT.getCompany();

            try {                
                BigDecimal mmtAmount = SpcfUtils.convertToBigDecimal(pMMT.getMoneyMovementTransactionAmount());

                //
                // Check to ensure MMT amount is a positive dollar amount
                //
                if (mmtAmount.compareTo(ZERO) < 0) {
                    //
                    // If payment is negative dollar amount, log an error (skip payment and move on to next)
                    //
                    String msg = String.format("MoneyMovementTransaction amount for company %s:%s is < $0.00 " +
                                               "(MMT id: %s, MMT amount: $%,.2f).",
                                               company.getSourceSystemCd(),
                                               company.getSourceCompanyId(),
                                               pMMT.getId(),
                                               mmtAmount);

                    throw new EftpsPaymentInvalidAmountException(msg);
                } else if (mmtAmount.compareTo(ZERO) == 0) {
                    //
                    // If payment is zero dollar amount, complete the transaction (and move on to next payment)
                    //
                    String msg = String.format("MoneyMovementTransaction amount for company %s:%s is $0.00 " +
                                               "(MMT id: %s, payment skipped - completing transaction).",
                                               company.getSourceSystemCd(),
                                               company.getSourceCompanyId(),
                                               pMMT.getId());

                    logger.info(msg);

                    //
                    // If MMT amount is zero, complete the MMT/FTs and move on
                    //
                    pMMT.updateTaxPaymentStatus(TaxPaymentStatus.SentToAgency); // execute
                    pMMT.updateTaxPaymentStatus(TaxPaymentStatus.AcknowledgedByAgency, true, true); // complete

                    paymentProcessed = true; // To stop ATOA from being cancelled in finally block

                    return false; // return false since payment is not included in file
                }

                //
                // Determine the tax amount(s) to be reported
                //
                // Rules:
                //
                // If the MMT represents FUTA, then the entire MMT amount will be consolidated to one line item.
                // Else if the MMT contains any COBRA, then the entire MMT amount will be consolidated to one line item.
                // Else the MMT represents FIT/FICA/MEDICARE, which must be itemized to separate line items.
                //
                BigDecimal consolidatedAmount = ZERO;
                BigDecimal disbursementTotal = ZERO;
                BigDecimal fitAmount = ZERO;
                BigDecimal ficaAmount = ZERO;
                BigDecimal medAmount = ZERO;

                for (Map.Entry<Law, SpcfMoney> entry : pMMT.getLiabilityBalances().entrySet()) {
                    disbursementTotal = disbursementTotal.add(SpcfUtils.convertToBigDecimal(entry.getValue()));

                    if (entry.getKey().isFUTA() || (entry.getKey().isIRSCreditLaw() && !entry.getKey().isFICADeferral())) {
                        consolidatedAmount = mmtAmount;
                    } else if (entry.getKey().isFIT()) {
                        fitAmount = fitAmount.add(SpcfUtils.convertToBigDecimal(entry.getValue()));
                    } else if (entry.getKey().isFICA()) {
                        ficaAmount = ficaAmount.add(SpcfUtils.convertToBigDecimal(entry.getValue()));
                    } else if (entry.getKey().isMED()) {
                        medAmount = medAmount.add(SpcfUtils.convertToBigDecimal(entry.getValue()));
                    } else {
                        String msg = String.format("Unsupported EFTPS tax payment type found for company %s:%s " +
                                                   "(MMT id: %s, Law id: %s).",
                                                   company.getSourceSystemCd(),
                                                   company.getSourceCompanyId(),
                                                   pMMT.getId(),
                                                   entry.getKey().getLawId());
                        throw new EftpsPaymentBusinessException(msg);
                    }
                }

                //
                // PSRV002374
                // Check to ensure the disbursements (sum of relevant FT amounts) equals the MMT amount
                // (this is necessary to keep the payment file from going out of balance)
                //
                if (mmtAmount.compareTo(disbursementTotal) != 0) {
                    String msg = String.format("Disbursements not equal to MMT amount for company %s:%s " +
                                               "(MMT id: %s, MMT amount: $%,.2f, Disbursements: $%,.2f).",
                                               company.getSourceSystemCd(),
                                               company.getSourceCompanyId(),
                                               pMMT.getId(),
                                               mmtAmount,
                                               disbursementTotal);

                    throw new EftpsPaymentInvalidAmountException(msg);
                }

                //
                // Get the tax type code for this payment (forms 940/941/944)
                //
                TaxTypeCode taxTypeCode = getTaxTypeCode(pMMT);

                //
                // Note: Everything above can throw an exception as a normal course of processing the payment,
                //       resulting in the individual payment being skipped. We don't want to call
                //       assertSegmentBoundaryConditions until we know the payment is good (i.e. should appear in the
                //       file) since it can adjust the current segment state/count within the file (in other words, we
                //       don't want to prep the segment/file to receive the payment until we know it's a good payment.)
                //

                //
                // Ensure the current file/segment can accommodate the payment
                //
                assertSegmentBoundaryConditions(mmtAmount);

                //
                // If we're itemizing, check to ensure the itemized tax payment amounts are >= 0
                // (if not, consolidate the payment since we cannot report negative tax amounts to IRS)
                //
                // Example:
                //   FIT : $-10.00
                //   FICA: $  0.00
                //   MED : $100.00
                //   Net tax amount: $+90.00 (which is valid, but we cannot report -10.00 as FIT in this case...)
                //
                if (consolidatedAmount.equals(ZERO) && ((fitAmount.compareTo(ZERO) < 0) ||
                                                        (ficaAmount.compareTo(ZERO) < 0) ||
                                                        (medAmount.compareTo(ZERO) < 0))) {
                    consolidatedAmount = mmtAmount;
                }

                //
                // Update the segment info properties (only once per segment)
                //
                if (mSegmentTaxPayments.isEmpty()) {
                    mSegmentDebitAccount = pMMT.getLiabilityDebitBankAccount();
                }

                //
                // Add the payment to the segment
                //
                Date periodEndReferenceDate = CalendarUtils.convertToDate(pMMT.getPaymentPeriodEnd());
                EftpsTaxPayment payment;

                if (!consolidatedAmount.equals(ZERO)) {
                    payment = new EftpsConsolidatedPayment(taxTypeCode, company.getFedTaxId(),
                                                           periodEndReferenceDate, consolidatedAmount);
                } else {
                    payment = new EftpsItemizedPayment(taxTypeCode, company.getFedTaxId(),
                                                       periodEndReferenceDate, fitAmount, ficaAmount, medAmount);
                }

                mSegmentTaxPayments.add(payment);

                //
                // Update the segment total
                //
                mSegmentTotal = mSegmentTotal.add(mmtAmount);

                //
                // Update the payment status (also creates the eftps payment detail record and company event)
                //
                updatePaymentStatus(pMMT, payment);

                paymentProcessed = true;
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
                logger.error(String.format("Error writing tax payment to EFTPS Payment file %s (payment skipped). ",
                                           getDetailedFileName()), e);
            } catch (EftpsPaymentInvalidAmountException e) {
                //
                // For EftpsPaymentInvalidAmountException, we just want to skip this payment and move on to the next.
                //
                logger.error("Tax payment skipped due to invalid MMT amount. ", e);
            } catch (Throwable t) {
                //
                // All other exceptions mean that the file is incomplete/corrupt and we cannot proceed.
                //
                throw new RuntimeException(String.format("Error writing EFTPS Payments to file %s. ",
                                                         getDetailedFileName()), t);
            }finally{
                try{
                    if(!paymentProcessed){
                        pMMT.setStatus(PaymentStatus.Created);
                        pMMT.cancelAgencyTaxOverpaymentAppliedTransactions();
                    }
                }catch(Throwable t){
                    logger.error(String.format("Unable to cancel AgencyTaxOverPaymentApplied for company %s:%s " +
                                               "(MMT id: %s).",
                                               company.getSourceSystemCd(),
                                               company.getSourceCompanyId(),
                                               pMMT.getId()), t);
                }
            }

            return paymentProcessed;
        }
    }
}
