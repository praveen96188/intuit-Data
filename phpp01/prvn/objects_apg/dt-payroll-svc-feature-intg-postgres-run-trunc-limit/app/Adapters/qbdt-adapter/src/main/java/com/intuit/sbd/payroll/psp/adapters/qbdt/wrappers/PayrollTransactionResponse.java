package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 27, 2010
 * Time: 2:14:37 PM
 */
public class PayrollTransactionResponse {
    private QBOFX.OFXPayrollTransactionTransactionType mTransactionType;
    private String mSourceId;
    private boolean mIsVoid = false;
    private String mAccountName;
    private String mCleared;
    private String mMemo;
    private String mAgencyName;
    private String mReferenceNumber;
    private boolean mOnService;
    private SpcfCalendar mTransactionDate;
    private SpcfCalendar mPeriodEndDate;
    private SpcfMoney mAmount;
    private String mEmployeeId;
    private String mEmployeeName;
    private List<TransactionLine> mTransactionLines = new ArrayList<TransactionLine>();
    private SpcfUniqueId mInternalId;

    public PayrollTransactionResponse(LiabilityCheck pLiabilityCheck) {
        mTransactionType = QBOFX.OFXPayrollTransactionTransactionType.LIABCHK;
        mSourceId = pLiabilityCheck.getSourceId();
        mIsVoid = pLiabilityCheck.getIsVoid();
        mTransactionDate = pLiabilityCheck.getTransactionDate();
        mPeriodEndDate = pLiabilityCheck.getPeriodEndDate();
        mAmount = translateTotalAmount(pLiabilityCheck.getAmount());

        setQBDTTransactionInfo(pLiabilityCheck.getQbdtTransactionInfo());

        for (LiabilityCheckLine liabilityCheckLine : pLiabilityCheck.getLiabilityCheckLineCollection()) {
            mTransactionLines.add(new TransactionLine(liabilityCheckLine));
        }
    }

    public PayrollTransactionResponse(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {
        mTransactionType = QBOFX.OFXPayrollTransactionTransactionType.LIABADJ;

        mSourceId = pCompanyAdjustmentSubmission.getSourceId();
        mIsVoid = pCompanyAdjustmentSubmission.isVoid();
        mTransactionDate = pCompanyAdjustmentSubmission.getSubmissionDate();
        mAmount = translateTotalAmount(pCompanyAdjustmentSubmission.getAmount());
        mInternalId = pCompanyAdjustmentSubmission.getId();

        setQBDTTransactionInfo(pCompanyAdjustmentSubmission.getQbdtTransactionInfo());

        // map related submissions
        // payrollItemId -> liabilityAdjustments
        Map<String, List<LiabilityAdjustment>> liabilityAdjustmentMap = new HashMap<String, List<LiabilityAdjustment>>();
        for (CompanyAdjustmentSubmission companyAdjustmentSubmission : pCompanyAdjustmentSubmission.getAssociatedSubmissionCollection()) {
            for (LiabilityAdjustment liabilityAdjustment : companyAdjustmentSubmission.getLiabilityAdjustmentCollection()) {
                if(liabilityAdjustment.getCompanyLaw() != null) {
                    String payrollItemId = liabilityAdjustment.getCompanyLaw().getSourceId();
                    List<LiabilityAdjustment> liabilityAdjustments = liabilityAdjustmentMap.get(payrollItemId);
                    if(liabilityAdjustments == null) {
                        liabilityAdjustments = new ArrayList<LiabilityAdjustment>();
                        liabilityAdjustmentMap.put(payrollItemId, liabilityAdjustments);
                    }
                    liabilityAdjustments.add(liabilityAdjustment);
                }
            }
        }

        for (LiabilityAdjustment liabilityAdjustment : pCompanyAdjustmentSubmission.getLiabilityAdjustmentCollection()) {
            if(mPeriodEndDate == null) {
                mPeriodEndDate = liabilityAdjustment.getPayrollRun().getPaycheckDate();
            }
            if(mEmployeeId == null && liabilityAdjustment.getEmployee() != null) {
                mEmployeeId = liabilityAdjustment.getEmployee().getSourceEmployeeId();
                mEmployeeName = liabilityAdjustment.getEmployee().getFullName();
            }
            if(liabilityAdjustment.getCompanyLaw() != null) {
                String payrollItemId = liabilityAdjustment.getCompanyLaw().getSourceId();
                List<LiabilityAdjustment> relatedAdjustments = liabilityAdjustmentMap.remove(payrollItemId);
                if(relatedAdjustments != null) {
                    for (LiabilityAdjustment relatedAdjustment : relatedAdjustments) {
                        if(liabilityAdjustment.getAmount() != null && relatedAdjustment.getAmount() != null) {
                            liabilityAdjustment.setAmount(new SpcfMoney(liabilityAdjustment.getAmount().add(relatedAdjustment.getAmount())));
                        }
                        if(liabilityAdjustment.getTaxableWages() != null && relatedAdjustment.getTaxableWages() != null) {
                            liabilityAdjustment.setTaxableWages(new SpcfMoney(liabilityAdjustment.getTaxableWages().add(relatedAdjustment.getTaxableWages())));
                        }
                        if(liabilityAdjustment.getTotalWages() != null && relatedAdjustment.getTotalWages() != null) {
                            liabilityAdjustment.setTotalWages(new SpcfMoney(liabilityAdjustment.getTotalWages().add(relatedAdjustment.getTotalWages())));
                        }
                    }
                }
            }
            mTransactionLines.add(new TransactionLine(liabilityAdjustment));
        }

        for (List<LiabilityAdjustment> liabilityAdjustments : liabilityAdjustmentMap.values()) {            
            LiabilityAdjustment sumLiabilityAdjustment = null;
            for (LiabilityAdjustment liabilityAdjustment : liabilityAdjustments) {
                if(sumLiabilityAdjustment == null) {
                    sumLiabilityAdjustment = liabilityAdjustment;
                } else {
                    if(sumLiabilityAdjustment.getAmount() != null && liabilityAdjustment.getAmount() != null) {
                        sumLiabilityAdjustment.setAmount(new SpcfMoney(sumLiabilityAdjustment.getAmount().add(liabilityAdjustment.getAmount())));
                    }
                    if(sumLiabilityAdjustment.getTaxableWages() != null && liabilityAdjustment.getTaxableWages() != null) {
                        sumLiabilityAdjustment.setTaxableWages(new SpcfMoney(sumLiabilityAdjustment.getTaxableWages().add(liabilityAdjustment.getTaxableWages())));
                    }
                    if(sumLiabilityAdjustment.getTotalWages() != null && liabilityAdjustment.getTotalWages() != null) {
                        sumLiabilityAdjustment.setTotalWages(new SpcfMoney(sumLiabilityAdjustment.getTotalWages().add(liabilityAdjustment.getTotalWages())));
                    }                    
                }
            }
            mTransactionLines.add(new TransactionLine(sumLiabilityAdjustment));
        }

        if(pCompanyAdjustmentSubmission.getQbdtPayrollTransaction() != null) {
            QbdtPayrollTransaction qbdtPayrollTransaction = pCompanyAdjustmentSubmission.getQbdtPayrollTransaction();
            if(mPeriodEndDate == null) {
                mPeriodEndDate = qbdtPayrollTransaction.getPeriodEndDate();
            }
            if(mEmployeeId == null && qbdtPayrollTransaction.getEmployee() != null) {
                mEmployeeId = qbdtPayrollTransaction.getEmployee().getSourceEmployeeId();
                mEmployeeName = qbdtPayrollTransaction.getEmployee().getFullName();
            }
            for (QbdtPayrollTransactionLine qbdtPayrollTransactionLine : pCompanyAdjustmentSubmission.getQbdtPayrollTransaction().getQbdtPayrollTransactionLineCollection()) {                
                mTransactionLines.add(new TransactionLine(qbdtPayrollTransactionLine));
            }
        }
    }

    public PayrollTransactionResponse(QbdtPayrollTransaction pQbdtPayrollTransaction) {
        mTransactionType = translateTransactionType(pQbdtPayrollTransaction.getTransactionType());


        mSourceId = pQbdtPayrollTransaction.getSourceId();
        mIsVoid = pQbdtPayrollTransaction.getIsVoided();
        mTransactionDate = pQbdtPayrollTransaction.getTransactionDate();
        mPeriodEndDate = pQbdtPayrollTransaction.getPeriodEndDate();
        mAmount = translateTotalAmount(pQbdtPayrollTransaction.getAmount());
        mInternalId = pQbdtPayrollTransaction.getId();
        mEmployeeName = pQbdtPayrollTransaction.getEmployeeName();
        if(pQbdtPayrollTransaction.getEmployee() != null) {
            mEmployeeId = pQbdtPayrollTransaction.getEmployee().getSourceEmployeeId();
        }

        setQBDTTransactionInfo(pQbdtPayrollTransaction.getQbdtTransactionInfo());

        for (QbdtPayrollTransactionLine qbdtPayrollTransactionLine : pQbdtPayrollTransaction.getQbdtPayrollTransactionLineCollection()) {
            mTransactionLines.add(new TransactionLine(qbdtPayrollTransactionLine));
        }
    }

    private QBOFX.OFXPayrollTransactionTransactionType translateTransactionType(QbdtPayrollTransactionType pQbdtPayrollTransactionType) {
        if(pQbdtPayrollTransactionType != null) {
            switch (pQbdtPayrollTransactionType) {
                case DDReturn:
                    return QBOFX.OFXPayrollTransactionTransactionType.DDRETURN;
                case FundsTransfer:
                    return QBOFX.OFXPayrollTransactionTransactionType.FUNDSTRANSFER;
                case LiabilityAdjustment:
                    return QBOFX.OFXPayrollTransactionTransactionType.LIABADJ;
                case PriorPayment:
                    return QBOFX.OFXPayrollTransactionTransactionType.PRIORPMT;
                case Refund:
                    return QBOFX.OFXPayrollTransactionTransactionType.REFUND;
                case LiabilityCheck:
                    return QBOFX.OFXPayrollTransactionTransactionType.LIABCHK;
            }
        }
        return null;
    }

    public PayrollTransactionResponse(PriorPaymentSubmission pPriorPaymentSubmission) {
        // could be liability check or refund also, transaction type is changed below if so
        mTransactionType = QBOFX.OFXPayrollTransactionTransactionType.PRIORPMT;

        mSourceId = pPriorPaymentSubmission.getSourceId();
        mInternalId = pPriorPaymentSubmission.getId();

        if(pPriorPaymentSubmission.getQbdtPayrollTransaction() != null) {
            QbdtPayrollTransaction qbdtPayrollTransaction = pPriorPaymentSubmission.getQbdtPayrollTransaction();
            mTransactionType = translateTransactionType(qbdtPayrollTransaction.getTransactionType());
            mIsVoid = qbdtPayrollTransaction.getIsVoided();
            mTransactionDate = qbdtPayrollTransaction.getTransactionDate();
            mPeriodEndDate = qbdtPayrollTransaction.getPeriodEndDate();

            setQBDTTransactionInfo(qbdtPayrollTransaction.getQbdtTransactionInfo());

            for (QbdtPayrollTransactionLine qbdtPayrollTransactionLine : qbdtPayrollTransaction.getQbdtPayrollTransactionLineCollection()) {
                mTransactionLines.add(new TransactionLine(qbdtPayrollTransactionLine));
            }

            mAmount = translateTotalAmount(qbdtPayrollTransaction.getAmount());
        } else if(pPriorPaymentSubmission.getQbdtTransactionInfoCollection().size() > 0) {
            QbdtTransactionInfo qbdtTransactionInfo = pPriorPaymentSubmission.getQbdtTransactionInfoCollection().get(0);
            setQBDTTransactionInfo(qbdtTransactionInfo);
        }

        SpcfDecimal totalAmount = SpcfMoney.ZERO;
        for (QbdtTransactionInfo qbdtTransactionInfo : pPriorPaymentSubmission.getQbdtTransactionInfoCollection()) {
            if(qbdtTransactionInfo.getMoneyMovementTransaction() != null) {
                MoneyMovementTransaction moneyMovementTransaction = qbdtTransactionInfo.getMoneyMovementTransaction();
                if(!mIsVoid) {
                    mIsVoid = moneyMovementTransaction.getManualPaymentStatus() == ManualPaymentStatus.Voided;
                }
                if(mTransactionDate == null) {
                    mTransactionDate = moneyMovementTransaction.getInitiationDate();
                }
                if(mPeriodEndDate == null) {
                    mPeriodEndDate = moneyMovementTransaction.getPaymentPeriodEnd();
                }

                totalAmount = totalAmount.add(moneyMovementTransaction.getMoneyMovementTransactionAmount());

                for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                    mTransactionLines.add(new TransactionLine(financialTransaction, mTransactionType));
                }
            }
        }

        if(mAmount == null) {
            if(totalAmount.isLessThan(SpcfMoney.ZERO)) {
                mTransactionType = QBOFX.OFXPayrollTransactionTransactionType.REFUND;
            }
            mAmount = translateTotalAmount(new SpcfMoney(totalAmount));
        }
    }

    private void setQBDTTransactionInfo(QbdtTransactionInfo qbdtTransactionInfo) {
        if(qbdtTransactionInfo == null) {
            return;
        }

        mAccountName = qbdtTransactionInfo.getAccountName();
        mCleared = qbdtTransactionInfo.getCleared();
        mAgencyName = qbdtTransactionInfo.getAgencyName();
        mMemo = qbdtTransactionInfo.getMemo();
        mReferenceNumber = qbdtTransactionInfo.getReferenceNumber();
        mOnService = qbdtTransactionInfo.getOnService();
    }

    public QBOFX.OFXPayrollTransactionTransactionType getTransactionType() {
        return mTransactionType;
    }

    public void setTransactionType(QBOFX.OFXPayrollTransactionTransactionType pTransactionType) {
        mTransactionType = pTransactionType;
    }

    public String getSourceId() {
        return mSourceId;
    }

    public void setSourceId(String pSourceId) {
        mSourceId = pSourceId;
    }

    public boolean isVoid() {
        return mIsVoid;
    }

    public void setIsVoid(boolean pIsVoid) {
        mIsVoid = pIsVoid;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public void setAccountName(String pAccountName) {
        mAccountName = pAccountName;
    }

    public String getCleared() {
        return mCleared;
    }

    public void setCleared(String pCleared) {
        mCleared = pCleared;
    }

    public String getMemo() {
        return mMemo;
    }

    public void setMemo(String pMemo) {
        mMemo = pMemo;
    }

    public String getAgencyName() {
        return mAgencyName;
    }

    public void setAgencyName(String pAgencyName) {
        mAgencyName = pAgencyName;
    }

    public String getReferenceNumber() {
        return mReferenceNumber;
    }

    public void setReferenceNumber(String pReferenceNumber) {
        mReferenceNumber = pReferenceNumber;
    }

    public boolean isOnService() {
        return mOnService;
    }

    public void setOnService(boolean pOnService) {
        mOnService = pOnService;
    }

    public SpcfCalendar getTransactionDate() {
        return mTransactionDate;
    }

    public void setTransactionDate(SpcfCalendar pTransactionDate) {
        mTransactionDate = pTransactionDate;
    }

    public SpcfCalendar getPeriodEndDate() {
        return mPeriodEndDate;
    }

    public void setPeriodEndDate(SpcfCalendar pPeriodEndDate) {
        mPeriodEndDate = pPeriodEndDate;
    }

    public SpcfMoney getAmount() {
        return mAmount;
    }

    public void setAmount(SpcfMoney pAmount) {
        mAmount = translateTotalAmount(pAmount);
    }

    public String getEmployeeId() {
        return mEmployeeId;
    }

    public void setEmployeeId(String pEmployeeId) {
        mEmployeeId = pEmployeeId;
    }

    public List<TransactionLine> getTransactionLines() {
        return mTransactionLines;
    }

    public void setTransactionLines(List<TransactionLine> pTransactionLines) {
        mTransactionLines = pTransactionLines;
    }

    public SpcfUniqueId getInternalId() {
        return mInternalId;
    }

    public void setInternalId(SpcfUniqueId pInternalId) {
        mInternalId = pInternalId;
    }

    public IPAYROLLTX getIPAYROLLTX() {
        IPAYROLLTX ipayrolltx = new IPAYROLLTX();
        ipayrolltx.setIPAYROLLTXID(mSourceId);

        ipayrolltx.setIPAYROLLTXTYPE(mTransactionType.toString());
        ipayrolltx.setIVOID(QBOFX.Y_N(mIsVoid));

        ipayrolltx.setIACCTNAME(QBOFX.convertNullToOFXString(mAccountName));
        ipayrolltx.setICLEARED(mCleared);
        ipayrolltx.setIMEMO(mMemo);
        ipayrolltx.setINAME(QBOFX.convertNullToOFXString(mAgencyName));
        ipayrolltx.setIREFNUM(mReferenceNumber != null ? mReferenceNumber : "");
        ipayrolltx.setIONSERVICE(QBOFX.Y_N(mOnService));

        if(mEmployeeId != null) {
            ipayrolltx.setIEMPID(mEmployeeId);
        }

        if(mEmployeeName != null) {
            ipayrolltx.setIEMPNAME(mEmployeeName);
        }

        // settlement date
        ipayrolltx.setIDTTX(QBOFX.getDTTXResponse(SpcfUtils.convertSpcfCalendarToDate(mTransactionDate)));
        // paycheck date
        ipayrolltx.setIDTPAYPDEND(QBOFX.getDTTXResponse(SpcfUtils.convertSpcfCalendarToDate(mPeriodEndDate)));

        ipayrolltx.setIAMT(mAmount != null ? ("$" + mAmount.toString()) : QBOFX.NULL);

        for (TransactionLine transactionLine : mTransactionLines) {
            transactionLine.setAmount(translateLineAmount(transactionLine.getAmount()));
            ipayrolltx.getITXLINE().add(transactionLine.getITXLINE());
        }

        Collections.sort(ipayrolltx.getITXLINE(), new ITXLINEComparator());

        return ipayrolltx;
    }

    public static class TransactionLine {
        private SpcfMoney mAmount;
        private String mPayrollItemId;
        private String mLawId;
        private String mAccountName;
        private String mMemo;
        private boolean mIsDirectDeposit;
        private String mTrackingClass;
        private SpcfMoney mTaxableWages;
        private SpcfMoney mTotalWages;
        private boolean mSystemGenerated = false;
        private boolean mNegateAmountBeforeResponse = false;

        public TransactionLine(LiabilityCheckLine pLiabilityCheckLine) {
            mAmount = pLiabilityCheckLine.getAmount();
            if(pLiabilityCheckLine.getCompanyLaw() != null) {
                CompanyLaw companyLaw = pLiabilityCheckLine.getCompanyLaw().getLatestCompanyLaw();
                mPayrollItemId = companyLaw.getSourceId();
            } else if(pLiabilityCheckLine.getCompanyPayrollItem() != null) {
                mPayrollItemId = pLiabilityCheckLine.getCompanyPayrollItem().getLatestCompanyPayrollItem().getSourcePayrollItemId();
            }

            setQBDTTransactionInfo(pLiabilityCheckLine.getQbdtTransactionInfo());
        }

        public TransactionLine(LiabilityAdjustment pLiabilityAdjustment) {
            mAmount = pLiabilityAdjustment.getAmount();
            mTaxableWages = pLiabilityAdjustment.getTaxableWages();
            mTotalWages = pLiabilityAdjustment.getTotalWages();

            if(pLiabilityAdjustment.getCompanyLaw() != null) {
                CompanyLaw companyLaw = pLiabilityAdjustment.getCompanyLaw().getLatestCompanyLaw();
                mPayrollItemId = companyLaw.getSourceId();
                mLawId = companyLaw.getLaw().getLawId();
                if(companyLaw.getQbdtPayrollItemInfo() != null && companyLaw.getQbdtPayrollItemInfo().getIsEmployeePaid()) {
                    mNegateAmountBeforeResponse = true;
                }
            }

            setQBDTTransactionInfo(pLiabilityAdjustment.getQbdtTransactionInfo());
        }

        public TransactionLine(QbdtPayrollTransactionLine pQbdtPayrollTransactionLine) {
            mAmount = pQbdtPayrollTransactionLine.getAmount();
            mTaxableWages = pQbdtPayrollTransactionLine.getWageBaseAmount();
            mTotalWages = pQbdtPayrollTransactionLine.getTaxableWageAmount();

            if(pQbdtPayrollTransactionLine.getCompanyPayrollItem() != null) {
                mPayrollItemId = pQbdtPayrollTransactionLine.getCompanyPayrollItem().getLatestCompanyPayrollItem().getSourcePayrollItemId();
            }

            setQBDTTransactionInfo(pQbdtPayrollTransactionLine.getQbdtTransactionInfo());
        }

        public TransactionLine(FinancialTransaction pFinancialTransaction, QBOFX.OFXPayrollTransactionTransactionType pTransactionType) {
            mAmount = pFinancialTransaction.getFinancialTransactionAmount();

            if(pTransactionType == QBOFX.OFXPayrollTransactionTransactionType.REFUND && pFinancialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyHPDETaxPayment){
                mNegateAmountBeforeResponse = true;
            } else if(pTransactionType == QBOFX.OFXPayrollTransactionTransactionType.PRIORPMT && pFinancialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyHPDETaxRefund){
                mNegateAmountBeforeResponse = true;
            }

            if(pFinancialTransaction.getCompanyLaw() != null) {
                CompanyLaw companyLaw = pFinancialTransaction.getCompanyLaw().getLatestCompanyLaw();
                mPayrollItemId = companyLaw.getSourceId();
            }

            setQBDTTransactionInfo(pFinancialTransaction.getQbdtTransactionInfo());
        }

        private void setQBDTTransactionInfo(QbdtTransactionInfo qbdtTransactionInfo) {
            if(qbdtTransactionInfo == null) {
                return;
            }

            mIsDirectDeposit = qbdtTransactionInfo.getIsDirectDeposit();
            mAccountName = qbdtTransactionInfo.getAccountName();
            mMemo = qbdtTransactionInfo.getMemo();
            mTrackingClass = qbdtTransactionInfo.getTrackingClass();
            mSystemGenerated = qbdtTransactionInfo.getSystemGenerated();
        }

        public SpcfMoney getAmount() {
            return mAmount;
        }

        public void setAmount(SpcfMoney pAmount) {
            mAmount = pAmount;
        }

        public String getPayrollItemId() {
            return mPayrollItemId;
        }

        public String getLawId() {
            return mLawId;
        }

        public String getAccountName() {
            return mAccountName;
        }

        public String getMemo() {
            return mMemo;
        }

        public boolean isDirectDeposit() {
            return mIsDirectDeposit;
        }

        public String getTrackingClass() {
            return mTrackingClass;
        }

        public SpcfMoney getTaxableWages() {
            return mTaxableWages;
        }

        public SpcfMoney getTotalWages() {
            return mTotalWages;
        }

        public ITXLINE getITXLINE() {
            ITXLINE itxline = new ITXLINE();
            if(mAmount != null) {
                if(mNegateAmountBeforeResponse) {
                    itxline.setIAMT("$" + mAmount.negate().toString());
                } else {
                    itxline.setIAMT("$" + mAmount.toString());
                }
            } else {
                itxline.setIAMT("");
            }
            itxline.setIACCTNAME(mPayrollItemId == null && !mIsDirectDeposit ? QBOFX.convertNullToOFXString(mAccountName) : mAccountName);
            itxline.setICLASS(mTrackingClass);
            if(mIsDirectDeposit) {
                itxline.setIISDD(QBOFX.Y_N(mIsDirectDeposit));
            }
            itxline.setIMEMO(mMemo);
            itxline.setIPITEMID(mPayrollItemId);
            if(mTotalWages != null) {
                itxline.setITAXABLEWAGE("$" + mTotalWages);
            }
            if(mTaxableWages != null) {
                itxline.setIWB("$" + mTaxableWages);
            }
            return itxline;
        }
    }

    public static class ITXLINEComparator implements Comparator<ITXLINE> {
        public int compare(ITXLINE a, ITXLINE b) {
            // nulls go last
            if(a == null) {
                return 1;
            }else if(b == null) {
                return -1;
            }

            // items with pitem ids go first
            if(a.getIPITEMID() != null && b.getIPITEMID() == null) {
                return -1;
            } else if(b.getIPITEMID() != null && a.getIPITEMID() == null) {
                return 1;
            } else if(a.getIPITEMID() != null && b.getIPITEMID() != null) {
                // sort by pitem id
                int result = Integer.valueOf(a.getIPITEMID()).compareTo(Integer.valueOf(b.getIPITEMID()));
                if(result == 0){
                    return compareAmount(a, b);
                }
                return result;
            } else {
                // fees then dd
                if(a.getIISDD() != null && b.getIISDD() == null) {
                    return 1;
                } else if(a.getIISDD() == null && b.getIISDD() != null) {
                    return -1;
                } else {
                    // dd (memos indicate a void put them last sorted by employee name)
                    return compareMemo(a, b);
                }
            }
        }

        private int compareMemo(ITXLINE a, ITXLINE b) {
            if(a.getIMEMO() != null && b.getIMEMO() == null) {
                return 1;
            } else if(b.getIMEMO() != null && a.getIMEMO() == null) {
                return -1;
            } else if(a.getIMEMO() != null && b.getIMEMO() != null) {
                int result = a.getIMEMO().compareTo(b.getIMEMO());
                if(result == 0) {
                    return compareAmount(a, b);
                }
                return result;
            } else {
                return compareAmount(a, b);
            }
        }

        private int compareAmount(ITXLINE a, ITXLINE b) {
            SpcfMoney aSpcfMoney = QBOFX.mapOFXStringToMoney(a.getIAMT());
            SpcfMoney bSpcfMoney = QBOFX.mapOFXStringToMoney(b.getIAMT());

            if(aSpcfMoney == null && bSpcfMoney == null) {
                return 0;
            }

            if(aSpcfMoney == null) {
                return 1;
            }

            if(bSpcfMoney == null) {
                return -1;
            }

            return aSpcfMoney.compareTo(bSpcfMoney);
        }
    }

    private SpcfMoney translateTotalAmount(SpcfMoney pAmount) {
         if(mTransactionType == QBOFX.OFXPayrollTransactionTransactionType.REFUND ||
                mTransactionType == QBOFX.OFXPayrollTransactionTransactionType.DDRETURN ||
                mTransactionType == QBOFX.OFXPayrollTransactionTransactionType.LIABCHK ||
                mTransactionType == QBOFX.OFXPayrollTransactionTransactionType.FUNDSTRANSFER) {
            return pAmount;
        } else {
            if(pAmount != null) {
                return new SpcfMoney(pAmount.negate());
            }
        }
        return pAmount;
    }

    private SpcfMoney translateLineAmount(SpcfMoney pAmount) {
        if(mTransactionType == QBOFX.OFXPayrollTransactionTransactionType.REFUND && pAmount != null) {
            return new SpcfMoney(pAmount.negate());
        }
        return pAmount;
    }
}
