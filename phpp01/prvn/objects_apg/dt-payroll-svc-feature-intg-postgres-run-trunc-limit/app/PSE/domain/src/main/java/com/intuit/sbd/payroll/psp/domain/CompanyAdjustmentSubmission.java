package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Hand-written business logic
 */
public class CompanyAdjustmentSubmission extends BaseCompanyAdjustmentSubmission implements IUpdatable {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public CompanyAdjustmentSubmission() {
        super();
    }
    /**
     * Return the cache key
     * @return NaturalKey
     */
    public NaturalKey getNaturalKey() {
        return getNaturalKey(getCompany(), getOriginalSubmission(), getVoidSubmission(), getAmount(), getSourceId(), getSubmissionDate());
    }

    /**
     * Cache Key for this entity
     *
     * @param pCompany                     Company associated with this liability adjustment
     * @param pOriginalSubmission          Original Submission for this liability adjustment
     * @param pVoidSubmission              Void Submission for this liability adjustment
     * @param pAmount                      Amount
     * @param pSourceId                    Source Id
     * @param pAmendmentProcessingStatusCd Amendment Processing Status
     * @param pSubmissionDate              Submission Date
     * @return Cache Key
     */
    public static NaturalKey getNaturalKey(Company pCompany,
                                          CompanyAdjustmentSubmission pOriginalSubmission,
                                          CompanyAdjustmentSubmission pVoidSubmission,
                                          SpcfMoney pAmount,
                                          String pSourceId,
                                          SpcfCalendar pSubmissionDate) {
        Object[] keys = new Object[6];
        keys[0] = (pCompany == null) ? "NULL_COMPANY" : pCompany.getId();
        keys[1] = (pOriginalSubmission == null) ? "NULL_ORIGINAL_SUBMISSION" : pOriginalSubmission.getId();
        keys[2] = (pVoidSubmission == null) ? "NULL_VOID_SUBMISSION" : pVoidSubmission.getId();
        keys[3] = (pAmount==null) ? "NULL_AMOUNT" : pAmount;
        keys[4] = (pSourceId==null) ? "NULL_SOURCE_ID" : pSourceId;
        keys[5] = (pSubmissionDate == null) ? "NULL_SUBMISSION_DATE" : pSubmissionDate;
        return new NaturalKey(Company.class, keys);
    }

    public void cache() {
        Application.getSessionCache().addPrimaryKey(getNaturalKey(), getId());
    }

    // returns true if the CAS is not associated with a payroll run or if there is no impound debit or if the impound has offloaded    
    public boolean hasTaxImpoundOffloaded() {
        return getPayrollRun() == null || !getPayrollRun().hasImpoundDebit() || getPayrollRun().hasTaxImpoundOffloaded();
    }
    public boolean isVoid() {
        return getVoidSubmission() != null;
    }

    public DomainEntitySet<Paycheck> getPaychecksForCompanyVoid() {
        Expression<Paycheck> query =
                new Query<Paycheck>()
                        .Where(Paycheck.CompanyAdjustmentSubmission().equalTo(this)).OrderBy(Paycheck.NetAmount().Descending());

        DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class, query);

        return paychecks;
    }

    public PayrollRun getPayrollForCompanyVoid() {
        PayrollRun payrollRun = null;
        DomainEntitySet<Paycheck> paychecks = getPaychecksForCompanyVoid();
        DomainEntitySet<LiabilityAdjustment> payrollTaxes = getLiabilityAdjustmentsForCompanyVoid();

        if (paychecks.size() > 0) {
            //Payroll run will be the same for every paycheck
            payrollRun = paychecks.get(0).getPayrollRun();
        } else if (payrollTaxes.size() > 0) {
            payrollRun = payrollTaxes.get(0).getPayrollRun();
        }

        return payrollRun;
    }

    public PayrollRun getPayrollRun() {
        PayrollRun payrollRun = null;

        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = getLiabilityAdjustmentCollection();
        if (liabilityAdjustments.size() > 0) {
           payrollRun= liabilityAdjustments.get(0).getPayrollRun();

        }
        return payrollRun;
    }

    public static CompanyAdjustmentSubmission findCompanyAdjustmentSubmission(Company pCompany, String pSourceId) {
        CompanyAdjustmentSubmission foundCompanyAdjustmentSubmission = null;

        NaturalKey naturalKey = new NaturalKey(CompanyAdjustmentSubmission.class, pCompany.getId(), pSourceId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            foundCompanyAdjustmentSubmission = Application.findById(CompanyAdjustmentSubmission.class, primaryKey);
        } else {
            Expression<CompanyAdjustmentSubmission> query =
                    new Query<CompanyAdjustmentSubmission>()
                            .Where(CompanyAdjustmentSubmission.Company().equalTo(pCompany)
                                           .And(CompanyAdjustmentSubmission.SourceId().equalTo(pSourceId)));

            DomainEntitySet<CompanyAdjustmentSubmission> companyAdjustmentSubmissions = Application.find(CompanyAdjustmentSubmission.class, query);
            if (companyAdjustmentSubmissions.size() > 0) {
                foundCompanyAdjustmentSubmission = companyAdjustmentSubmissions.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, foundCompanyAdjustmentSubmission.getId());
            }
        }

        return foundCompanyAdjustmentSubmission;
    }

    public SpcfMoney getImmediateRefundAmount() {
        SpcfMoney immediateRefundAmount = new SpcfMoney("0.00");

        DomainEntitySet<FinancialTransaction> erTaxCredits = FinancialTransaction.findVoidImmediateRefundTransactions(this);
        for (FinancialTransaction currFinTxn : erTaxCredits) {
            immediateRefundAmount = new SpcfMoney(immediateRefundAmount.add(currFinTxn.getFinancialTransactionAmount()));
        }
        return immediateRefundAmount;
    }

    public SpcfMoney getApplyForwardRefundAmount() {
        SpcfMoney applyForwardRefundAmount = new SpcfMoney("0.00");

        DomainEntitySet<FinancialTransaction> erTaxCredits = FinancialTransaction.findVoidApplyForwardTransactions(this);
        for (FinancialTransaction currFinTxn : erTaxCredits) {
            applyForwardRefundAmount = new SpcfMoney(applyForwardRefundAmount.add(currFinTxn.getFinancialTransactionAmount()));
        }
        return applyForwardRefundAmount;
    }

    public SpcfMoney getTORRefundAmount() {
        SpcfMoney TORRefundAmount = new SpcfMoney("0.00");
        DomainEntitySet<FinancialTransaction> torTransactions = FinancialTransaction.findTakeOnReturnTransactions(this);
        for (FinancialTransaction currFinTxn : torTransactions) {
            TORRefundAmount = new SpcfMoney(TORRefundAmount.add(currFinTxn.getFinancialTransactionAmount()));
        }
        return TORRefundAmount;
    }

    public Map<Law, SpcfDecimal> findVoidImmediateRefundAmountsByLaw() {
        Map<Law, SpcfDecimal> refundAmountsMap = new HashMap<Law, SpcfDecimal>();
        // add up all of the voided liabilities
        DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class,
                new Query<Paycheck>()
                        .Where(Paycheck.CompanyAdjustmentSubmission().equalTo(this)));
        for (Paycheck paycheck : paychecks) {
            for (Tax tax : paycheck.getTaxCollection()) {
                if (!refundAmountsMap.containsKey(tax.getLaw())) {
                    refundAmountsMap.put(tax.getLaw(), new SpcfMoney("0.00"));
                }
                refundAmountsMap.put(tax.getLaw(), refundAmountsMap.get(tax.getLaw()).add(tax.getTaxLiabilityAmount()));
            }
        }

        // subtract the applied foward amounts
        Map<Law, SpcfDecimal> appliedAmountsMap = findVoidApplyFowardAmountsByLaw();
        for (Law law : appliedAmountsMap.keySet()) {
            if (refundAmountsMap.containsKey(law)) {
                refundAmountsMap.put(law, refundAmountsMap.get(law).subtract(appliedAmountsMap.get(law)));
            }
        }

        // subtract the tor amount
        DomainEntitySet<FinancialTransaction> torTransactions = FinancialTransaction.findTakeOnReturnTransactions(this);
        for (FinancialTransaction torTransaction : torTransactions) {
            if (refundAmountsMap.containsKey(torTransaction.getLaw())) {
                refundAmountsMap.put(torTransaction.getLaw(), refundAmountsMap.get(torTransaction.getLaw()).subtract(appliedAmountsMap.get(torTransaction.getLaw())));
            }
        }

        return refundAmountsMap;
    }

    public Map<Law, SpcfDecimal> findVoidApplyFowardAmountsByLaw() {
        DomainEntitySet<FinancialTransaction> erTaxCredits = FinancialTransaction.findVoidApplyForwardTransactions(this);
        Map<Law, SpcfDecimal> appliedAmountsMap = new HashMap<Law, SpcfDecimal>();
        for (FinancialTransaction erTaxCredit : erTaxCredits) {
            if (!appliedAmountsMap.containsKey(erTaxCredit.getLaw())) {
                appliedAmountsMap.put(erTaxCredit.getLaw(), new SpcfMoney("0.00"));
            }
            appliedAmountsMap.put(erTaxCredit.getLaw(), appliedAmountsMap.get(erTaxCredit.getLaw()).add(erTaxCredit.getFinancialTransactionAmount()));
        }

        return appliedAmountsMap;
    }

    public DomainEntitySet<LiabilityAdjustment> getLiabilityAdjustmentsForCompanyVoid() {
        Expression<LiabilityAdjustment> query =
                new Query<LiabilityAdjustment>()
                        .Where(LiabilityAdjustment.CompanyAdjustmentSubmission().equalTo(this)).OrderBy(LiabilityAdjustment.Amount().Descending());

        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = Application.find(LiabilityAdjustment.class, query);

        return liabilityAdjustments;
    }

 
    public boolean hasIRSCreditLaw() {
        for (LiabilityAdjustment liabilityAdjustment : getLiabilityAdjustmentCollection()) {
            if(liabilityAdjustment.getCompanyLaw() != null && liabilityAdjustment.getCompanyLaw().getLaw().isIRSCreditLaw()) {
                return true;
            }
        }
        return false;
    }
    
    // ----- QBDT Token overrides -----

    @Override
    public void setSubmissionDate(SpcfCalendar pSubmissionDate) {
        if(!ObjectUtils.equals(getSubmissionDate(), pSubmissionDate)) {
            onUpdate();
        }
        super.setSubmissionDate(pSubmissionDate);    
    }

    @Override
    public void setSourceId(String pSourceId) {
        if(!ObjectUtils.equals(getSourceId(), pSourceId)) {
            onUpdate();
        }

        super.setSourceId(pSourceId);

        if(getCompany() != null && getSourceId() != null) {
            getCompany().usedPayrollTransactionId(getSourceId());
        }
    }

    @Override
    public void setAmount(SpcfMoney pAmount) {
        if(!ObjectUtils.equals(getAmount(), pAmount)) {
            onUpdate();
        }
        super.setAmount(pAmount);    
    }

    @Override
    public void setQbdtPayrollTransaction(QbdtPayrollTransaction pQbdtPayrollTransaction) {
        if(!ObjectUtils.equals(getQbdtPayrollTransaction(), pQbdtPayrollTransaction)) {
            onUpdate();
        }
        super.setQbdtPayrollTransaction(pQbdtPayrollTransaction);    
    }

    @Override
    public void setQbdtTransactionInfo(QbdtTransactionInfo pQbdtTransactionInfo) {
        if(!ObjectUtils.equals(getQbdtPayrollTransaction(), pQbdtTransactionInfo)) {
            onUpdate();
        }
        super.setQbdtTransactionInfo(pQbdtTransactionInfo);    
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }

        super.setCompany(pCompany);

        if(getCompany() != null && getSourceId() != null) {
            getCompany().usedPayrollTransactionId(getSourceId());
        }
    }

    @Override
    public void addLiabilityAdjustment(LiabilityAdjustment pLiabilityAdjustment) {
        super.addLiabilityAdjustment(pLiabilityAdjustment);
        onUpdate();
    }

    @Override
    public void removeLiabilityAdjustment(LiabilityAdjustment pLiabilityAdjustment) {
        super.removeLiabilityAdjustment(pLiabilityAdjustment);
        onUpdate();
    }

    public void onUpdate() {
        if(getQbdtTransactionInfo() != null) {
            getQbdtTransactionInfo().onUpdate();
        }

        if(getQbdtPayrollTransaction() != null) {
            getQbdtPayrollTransaction().update();
        }
    }
    /**
     * Find a Company Adjustment Submission DB/cache
     *
     * @param pCompany                     Company associated with this liability adjustment
     * @param pOriginalSubmission          Original Submission for this liability adjustment
     * @param pVoidSubmission              Void Submission for this liability adjustment
     * @param pAmount                      Amount
     * @param pSourceId                    Source Id
     * @param pAmendmentProcessingStatusCd Amendment Processing Status
     * @param pSubmissionDate              Submission Date
     * @return CompanyAdjustmentSubmission entity
     */
    public static CompanyAdjustmentSubmission findCompanyAdjustmentSubmission(Company pCompany,
                                              CompanyAdjustmentSubmission pOriginalSubmission,
                                              CompanyAdjustmentSubmission pVoidSubmission,
                                              SpcfMoney pAmount,
                                              String pSourceId,
                                              SpcfCalendar pSubmissionDate) {
        NaturalKey naturalKey = getNaturalKey(pCompany,
                                              pOriginalSubmission,
                                              pVoidSubmission,
                                              pAmount,
                                              pSourceId,
                                              pSubmissionDate);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);
        CompanyAdjustmentSubmission companyAdjustmentSubmission = null;

        if (primaryKey == null) {
            Expression<CompanyAdjustmentSubmission> query = new Query<CompanyAdjustmentSubmission>()
                    .Where(CompanyAdjustmentSubmission.Company().equalTo(pCompany)
                      .And(CompanyAdjustmentSubmission.OriginalSubmission().equalTo(pOriginalSubmission))
                      .And(CompanyAdjustmentSubmission.VoidSubmission().equalTo(pVoidSubmission))
                      .And(CompanyAdjustmentSubmission.Amount().equalTo(pAmount))
                      .And(CompanyAdjustmentSubmission.SourceId().equalTo(pSourceId)
                      .And(CompanyAdjustmentSubmission.SubmissionDate().equalTo(pSubmissionDate))
                                              ));
            DomainEntitySet<CompanyAdjustmentSubmission> companyAdjustmentSubmissions = Application.find(CompanyAdjustmentSubmission.class, query);
            if (!companyAdjustmentSubmissions.isEmpty()) {
                companyAdjustmentSubmission = companyAdjustmentSubmissions.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, companyAdjustmentSubmission.getId());
            }
        } else {
            companyAdjustmentSubmission = Application.findById(CompanyAdjustmentSubmission.class, primaryKey);
        }

        return companyAdjustmentSubmission;
    }
}