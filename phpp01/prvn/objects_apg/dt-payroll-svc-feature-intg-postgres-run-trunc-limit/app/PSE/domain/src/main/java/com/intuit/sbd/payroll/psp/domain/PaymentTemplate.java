package com.intuit.sbd.payroll.psp.domain;

import com.intuit.payroll.agency.api.IPaymentFrequency;
import com.intuit.payroll.agency.api.IPaymentPeriod;
import com.intuit.payroll.agency.api.IRulesInfo;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
import com.intuit.payroll.agency.api.RulesObjectBroker;
import com.intuit.payroll.agency.dao.FrequencyData;
import com.intuit.payroll.agency.impl.UpperLimit;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.ITxpRecordManager;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written business logic
 */
public class PaymentTemplate extends BasePaymentTemplate {

    public static final String IRS_941 = "IRS-941-PAYMENT";
    public static final String IRS_940 = "IRS-940-PAYMENT";
    public static final String FL_SUI = "FL-UCT6-PAYMENT";

    public static final String NY_WH = "NY-1MN-PAYMENT";
    public static final String PA_WH = "PA-501-PAYMENT";
    public static final String NY_METRO = "NY-MTA305-PAYMENT";

    public static PaymentTemplate findPaymentTemplate(String pPaymentTemplateCd) {
        return Application.findById(PaymentTemplate.class, pPaymentTemplateCd);
    }

    public static PaymentTemplate getIRS_941() {
        return findPaymentTemplate(IRS_941);
    }

    public static PaymentTemplate getIRS_940() {
        return findPaymentTemplate(IRS_940);
    }

    public static UpperLimit getThresholdInfo(String pTemplateId, String pFrequency) {
        IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();

        IRulesPaymentTemplate paymentTemplate = rulesInfo.getPaymentTemplate(pTemplateId);

        // Check if this payment template follows the frequency of another payment template
        String usesFrequencyOf = paymentTemplate.getUsesFrequencyOf();
        if (usesFrequencyOf != null) {
            paymentTemplate = rulesInfo.getPaymentTemplate(usesFrequencyOf);
        }
        IPaymentFrequency paymentFrequency = paymentTemplate.getPaymentFrequency(pFrequency);

        FrequencyData freq = (FrequencyData) paymentFrequency;
        if (freq != null && freq.getUpperLimits().size() > 0) {
            return freq.getUpperLimits().get(0);
        } else {
            paymentTemplate = rulesInfo.getPaymentTemplate(pTemplateId);
            paymentFrequency = paymentTemplate.getPaymentFrequency(pFrequency);

            freq = (FrequencyData) paymentFrequency;
            if (freq != null && freq.getUpperLimits().size() > 0) {
                return freq.getUpperLimits().get(0);
            }
        }

        return null;
    }


    public static String getUsesFrequencyOf(String pTemplateId) {
        IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();

        IRulesPaymentTemplate paymentTemplate = rulesInfo.getPaymentTemplate(pTemplateId);

        // Check if this payment template follows the frequency of another payment template
        String usesFrequencyOf = paymentTemplate.getUsesFrequencyOf();
        if (usesFrequencyOf != null) {
            return rulesInfo.getPaymentTemplate(usesFrequencyOf).getPaymentTemplateID();
        }
        return pTemplateId;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public PaymentTemplate() {
        super();
    }

    public MoneyMovementTransaction getTaxPayment(PayrollRun payrollRun) {

        SpcfCalendar initDate = null;
        return getTaxPayment(payrollRun, initDate, true);
    }

    public MoneyMovementTransaction getTaxPayment(PayrollRun payrollRun, SpcfCalendar initDate, boolean pIncludeHoldPayments) {

        DomainEntitySet<FinancialTransaction> agencyTaxCredits = this.getAgencyTaxCredits(payrollRun, TransactionStateCode.Created);

        for (FinancialTransaction agencyTaxCredit : agencyTaxCredits) {
            if (agencyTaxCredit.getMoneyMovementTransaction() != null && (initDate == null || initDate.equals(agencyTaxCredit.getMoneyMovementTransaction().getInitiationDate()))
                            && (pIncludeHoldPayments || agencyTaxCredit.getMoneyMovementTransaction().getTaxPaymentStatus() != TaxPaymentStatus.OnHold)) {
                return agencyTaxCredit.getMoneyMovementTransaction();
            }
        }

        DomainEntitySet<FinancialTransaction> agencyTaxDebits = this.getAgencyTaxDebits(payrollRun, TransactionStateCode.Created);

        for (FinancialTransaction agencyTaxDebit : agencyTaxDebits) {
            if (agencyTaxDebit.getMoneyMovementTransaction() != null && (initDate == null || initDate.equals(agencyTaxDebit.getMoneyMovementTransaction().getInitiationDate()))
                    && (pIncludeHoldPayments || agencyTaxDebit.getMoneyMovementTransaction().getTaxPaymentStatus() != TaxPaymentStatus.OnHold)) {
                return agencyTaxDebit.getMoneyMovementTransaction();
            }
        }

        return null;
    }


    public MoneyMovementTransaction getDirectDebitTaxPayment(PayrollRun payrollRun) {
        return getDirectDebitTaxPayment(payrollRun, null);
    }

    public MoneyMovementTransaction getDirectDebitTaxPayment(PayrollRun payrollRun, SpcfCalendar initDate) {

        DomainEntitySet<FinancialTransaction> agencyTaxDirectCredits;
        if (initDate == null) {
            agencyTaxDirectCredits = this.getAgencyTaxDirectCredits(payrollRun, TransactionStateCode.Created).find(FinancialTransaction.MoneyMovementTransaction().isNotNull());
        } else {
            agencyTaxDirectCredits = this.getAgencyTaxDirectCredits(payrollRun, TransactionStateCode.Created).find(FinancialTransaction.MoneyMovementTransaction().isNotNull().And(FinancialTransaction.MoneyMovementTransaction().InitiationDate().equalTo(initDate)));
        }

        if (agencyTaxDirectCredits.size() > 0) {
            return agencyTaxDirectCredits.get(0).getMoneyMovementTransaction();
        }
        return null;
    }

    public MoneyMovementTransaction getfOffloadedDirectDebitTaxPayment(PayrollRun payrollRun) {

        DomainEntitySet<FinancialTransaction> agencyTaxDirectCredits = this.getAgencyTaxDirectCredits(payrollRun, TransactionStateCode.Executed);

        if (agencyTaxDirectCredits.size() > 0) {

            return agencyTaxDirectCredits.get(0).getMoneyMovementTransaction();
        }
        return null;
    }


    public DomainEntitySet<FinancialTransaction> getAgencyTaxCredits(PayrollRun pPayrollRun, TransactionStateCode... pTransactionStates) {

        TransactionType agencyTaxCreditType = Application.findById(TransactionType.class, TransactionTypeCode.AgencyTaxCredit);

        Criterion<FinancialTransaction> agencyTaxCreditCriteria = FinancialTransaction.TransactionType().equalTo(agencyTaxCreditType)
                .And(FinancialTransaction.Law().PaymentTemplate().equalTo(this))
                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(pTransactionStates));

        DomainEntitySet<FinancialTransaction> financialTransactions = pPayrollRun.getFinancialTransactionCollection();


        return financialTransactions.find(agencyTaxCreditCriteria);
    }

    public DomainEntitySet<FinancialTransaction> getAgencyTaxDebits(PayrollRun pPayrollRun, TransactionStateCode... pTransactionStates) {

        TransactionType agencyTaxDebitType = Application.findById(TransactionType.class, TransactionTypeCode.AgencyTaxDebit);

        Criterion<FinancialTransaction> agencyTaxDebitCriteria = FinancialTransaction.TransactionType().equalTo(agencyTaxDebitType)
                .And(FinancialTransaction.Law().PaymentTemplate().equalTo(this))
                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(pTransactionStates));

        DomainEntitySet<FinancialTransaction> financialTransactions = pPayrollRun.getFinancialTransactionCollection();


        return financialTransactions.find(agencyTaxDebitCriteria);
    }

    public DomainEntitySet<FinancialTransaction> getAgencyTaxOverpayments(PayrollRun pPayrollRun, TransactionStateCode... pTransactionStates) {

        TransactionType agencyTaxOverpaymentType = Application.findById(TransactionType.class, TransactionTypeCode.AgencyTaxOverpayment);

        Criterion<FinancialTransaction> agencyTaxOverpaymentCriteria = FinancialTransaction.TransactionType().equalTo(agencyTaxOverpaymentType)
                .And(FinancialTransaction.Law().PaymentTemplate().equalTo(this))
                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(pTransactionStates));

        DomainEntitySet<FinancialTransaction> financialTransactions = pPayrollRun.getFinancialTransactionCollection();


        return financialTransactions.find(agencyTaxOverpaymentCriteria);
    }

    public DomainEntitySet<FinancialTransaction> getAgencyTaxDirectCredits(PayrollRun pPayrollRun, TransactionStateCode... pTransactionStates) {

        TransactionType agencyTaxCreditType = Application.findById(TransactionType.class, TransactionTypeCode.AgencyDirectCredit);
        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();

        if (pPayrollRun != null) {
            Criterion<FinancialTransaction> agencyTaxCreditCriteria = FinancialTransaction.TransactionType().equalTo(agencyTaxCreditType)
                    .And(FinancialTransaction.Law().PaymentTemplate().equalTo(this))
                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(pTransactionStates));

            financialTransactions = pPayrollRun.getFinancialTransactionCollection();

            return financialTransactions.find(agencyTaxCreditCriteria);
        }
        return financialTransactions;
    }

    /**
     * Calculates the net liability for a payroll run by adding the amounts for credit transactions
     * and subtracting amounts for debit transactions
     * Excludes COBRA Amounts
     *
     * @param pPayrollRun
     * @return
     */
    public SpcfDecimal getNetLiabilityAmount(PayrollRun pPayrollRun) {
        SpcfDecimal netLiability = SpcfMoney.ZERO;

        // sum all active paycheck tax lines
        DomainEntitySet<Tax> taxLines = new DomainEntitySet<Tax>();
        if (PayrollRun.getPayrollsInMemory(pPayrollRun.getCompany()).contains(pPayrollRun)) {
            for (Paycheck paycheck : pPayrollRun.getPaycheckCollection()) {
                if (paycheck.getStatus() == PaycheckStatusCode.Active) {
                    taxLines.addAll(paycheck.getTaxCollection().find(Tax.Law().PaymentTemplate().equalTo(this)));
                }
            }
        } else {
            taxLines.addAll(Application.find(Tax.class,
                    Tax.Paycheck().Status().equalTo(PaycheckStatusCode.Active)
                            .And(Tax.Paycheck().PayrollRun().equalTo(pPayrollRun))
                            .And(Tax.Law().PaymentTemplate().equalTo(this))));
        }

        // Exclude COBRA Amounts from Net Liability for threshold purposes
        for (Tax taxLine : taxLines) {
            if (taxLine.getTaxLiabilityAmount() != null && !taxLine.getLaw().isIRSCreditLaw()) {
                netLiability = netLiability.add(taxLine.getTaxLiabilityAmount());
            }
        }

        // sum all of the liability adjustments
        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments =
                pPayrollRun.getLiabilityAdjustmentCollection().find(LiabilityAdjustment.Law().PaymentTemplate().equalTo(this));
        for (LiabilityAdjustment liabilityAdjustment : liabilityAdjustments) {
            if (liabilityAdjustment.getAmount() != null && !liabilityAdjustment.getLaw().isIRSCreditLaw()) {
                netLiability = netLiability.add(liabilityAdjustment.getAmount());
            }
        }

        return netLiability;
    }


    public DomainEntitySet<MoneyMovementTransaction> findSiblingMMTs(Company pCompany, SpcfCalendar pDate, TaxPaymentStatus... pTaxPaymentStatuses) {

        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.PaymentTemplate().equalTo(this)
                .And(MoneyMovementTransaction.Company().equalTo(pCompany)
                        .And(MoneyMovementTransaction.PaymentPeriodBegin().lessOrEqualThan(pDate))
                        .And(MoneyMovementTransaction.PaymentPeriodEnd().greaterOrEqualThan(pDate))
                        .And(MoneyMovementTransaction.TaxPaymentStatus().in(pTaxPaymentStatuses)));

        return Application.find(MoneyMovementTransaction.class, mmtCriteria);

    }

    public DomainEntitySet<MoneyMovementTransaction> findSiblingMMTs(Company pCompany, SpcfCalendar pDate, PaymentMethod[] pPaymentMethods, TaxPaymentStatus... pTaxPaymentStatuses) {

        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.PaymentTemplate().equalTo(this)
                .And(MoneyMovementTransaction.Company().equalTo(pCompany)
                        .And(MoneyMovementTransaction.PaymentPeriodBegin().lessOrEqualThan(pDate))
                        .And(MoneyMovementTransaction.PaymentPeriodEnd().greaterOrEqualThan(pDate))
                        .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(pPaymentMethods))
                        .And(MoneyMovementTransaction.TaxPaymentStatus().in(pTaxPaymentStatuses)));

        return Application.find(MoneyMovementTransaction.class, mmtCriteria);

    }

    public DomainEntitySet<MoneyMovementTransaction> findQuarterEFTPSMMTs(Company pCompany, int pYear, int pQuarter, PaymentStatus... pPaymentStatuses) {
        SpcfCalendar firstDayOfQuarter = CalendarUtils.getFirstDayOfQuarter(pYear, pQuarter);
        SpcfCalendar lastDayOfQuarter = CalendarUtils.getLastDayOfQuarter(pYear, pQuarter);

        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.PaymentTemplate().equalTo(this)
                .And(MoneyMovementTransaction.Company().equalTo(pCompany)
                        .And(MoneyMovementTransaction.PaymentPeriodEnd().greaterOrEqualThan(firstDayOfQuarter))
                        .And(MoneyMovementTransaction.PaymentPeriodEnd().lessOrEqualThan(lastDayOfQuarter))
                        .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS))
                        .And(MoneyMovementTransaction.Status().in(pPaymentStatuses)));

        return Application.find(MoneyMovementTransaction.class, mmtCriteria);

    }

    public boolean payrollExceedsThreshold(PayrollRun pPayrollRun) {
        UpperLimit upperLimit = getThresholdInfo(getPaymentTemplateCd(), getEffectiveDepositFrequency(pPayrollRun).toString());
        if (upperLimit != null) {
            SpcfMoney accumulatedAmount = new SpcfMoney("0.0");

            // Get the MMT for the payroll/payment Template and check amount against threshold
            MoneyMovementTransaction mmt = getTaxPayment(pPayrollRun);
            if (mmt != null && mmt.getMoneyMovementTransactionAmount() != null) {
                accumulatedAmount = new SpcfMoney(mmt.getMoneyMovementTransactionAmount().add(accumulatedAmount));

            }

            if (mmt != null) {
                // Check if there is also other direct debit mmts on this payment period
                // Get the payroll runs with a paycheck date within the payment period

                Criterion<PayrollRun> payrollRunCriteria = PayrollRun.PaycheckDate().between(mmt.getPaymentPeriodBegin(), mmt.getPaymentPeriodEnd())
                        .And(PayrollRun.Company().equalTo(pPayrollRun.getCompany()));
                DomainEntitySet<PayrollRun> payrollRuns = Application.find(PayrollRun.class, payrollRunCriteria);

                for (PayrollRun payrollRun : payrollRuns) {
                    if (getDirectDebitTaxPayment(payrollRun) != null) {
                        accumulatedAmount = new SpcfMoney(accumulatedAmount.add(getDirectDebitTaxPayment(payrollRun).getMoneyMovementTransactionAmount()));
                    }
                }
            }


            return (accumulatedAmount.compareTo(new SpcfMoney(upperLimit.amount)) > 0);

        }
        return false;
    }    

    public void createNewEffectiveFrequencyForExceedingThreshold(PayrollRun pPayrollRun, PaymentTemplateFrequency pNewFrequency, CompanyEvent pCompanyEvent) {

        // Find existing deposit frequencies and make them invalid
        SpcfCalendar date = pPayrollRun.getPaycheckDate().copy().toLocal();

        // we are changing this because the threshold was exceeded, the new effective date is paycheckDate + 1 day
        date.addDays(1);

        DomainEntitySet<EffectiveDepositFrequency> frequencies = getEffectiveDepositFrequenciesEffectiveAfter(pPayrollRun.getCompany(), pPayrollRun.getPaycheckDate());
        for (EffectiveDepositFrequency frequency : frequencies) {
            if (frequency.getPaymentTemplateFrequency().getPaymentTemplate().getPaymentTemplateCd().equals(pNewFrequency.getPaymentTemplate().getPaymentTemplateCd())) {
                frequency.setInvalidDate(pPayrollRun.getPaycheckDate());
                pCompanyEvent.addCompanyEventDetail(EventDetailTypeCode.InvalidatedDepositFrequencyId, frequency.getId().toString());
                Application.save(frequency);
            }
        }

        //If the effective deposit frequency already exists do not create another one.
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(pPayrollRun.getCompany(), pNewFrequency.getPaymentTemplate(), date);
        if (effectiveDepositFrequency != null && effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(pNewFrequency.getPaymentFrequencyId())) {
            return;
        }

        effectiveDepositFrequency = new EffectiveDepositFrequency();

        CalendarUtils.clearTime(date);
        effectiveDepositFrequency.setEffectiveDate(date);
        CompanyAgency companyAgency =
                CompanyAgency.findCompanyAgency(pPayrollRun.getCompany().getSourceSystemCd(), pPayrollRun.getCompany().getSourceCompanyId(), getAgency().getAgencyId());

        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate =
                CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(companyAgency, this);
        effectiveDepositFrequency.setCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate);
        effectiveDepositFrequency.setPaymentTemplateFrequency(pNewFrequency);
        companyAgencyPaymentTemplate.addEffectiveDepositFrequency(effectiveDepositFrequency);
        Application.save(effectiveDepositFrequency);

        pCompanyEvent.addCompanyEventDetail(EventDetailTypeCode.PermanentPaymentFrequencyId, effectiveDepositFrequency.getId().toString());
        Application.save(pCompanyEvent);

        companyAgencyPaymentTemplate.createDepositFrequencyChangedEvent(pPayrollRun, date, null, pNewFrequency.getPaymentFrequencyId(), null, true);


    }

    public PaymentTemplateFrequency getPaymentTemplateFrequency(String pFrequencyId) {
        DomainEntitySet<PaymentTemplateFrequency> frequencies = getPaymentTemplateFrequencyCollection().find(PaymentTemplateFrequency.PaymentFrequencyId().equalTo(DepositFrequencyCode.valueOf(pFrequencyId)));
        if (frequencies.size() > 0) {
            return frequencies.get(0);
        }
        return null;
    }

    public DepositFrequencyCode getEffectiveDepositFrequency(PayrollRun pPayrollRun) {
        return getEffectiveDepositFrequency(pPayrollRun.getCompany(), pPayrollRun.getPaycheckDate());
    }

    public DepositFrequencyCode getEffectiveDepositFrequency(Company pCompany, SpcfCalendar pProcessDate) {
        DepositFrequencyCode overrideDepositFrequency = getOverrideDepositFrequency(pCompany, null);
        if(overrideDepositFrequency != null) {
            return overrideDepositFrequency;
        }

        return getEffectiveDepositFreq(pCompany, pProcessDate).getPaymentTemplateFrequency().getPaymentFrequencyId();
    }

    public EffectiveDepositFrequency getEffectiveDepositFreq(Company pCompany, SpcfCalendar pProcessDate) {
        // If this payment template uses the frequency of another payment template, look for that frequency instead
        String paymentTemplateId = getUsesFrequencyOf(this.getPaymentTemplateCd());
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateId);
        if (paymentTemplate != null) {
            //Get Effective Deposit Frequency based on the Paycheck Date
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pCompany, paymentTemplate);

            if(companyAgencyPaymentTemplate == null) {
                return null;
            }

            DomainEntitySet<EffectiveDepositFrequency> effectiveFrequencies = companyAgencyPaymentTemplate
                    .getEffectiveDepositFrequencyCollection()
                    .find(EffectiveDepositFrequency.EffectiveDate().lessOrEqualThan(pProcessDate)
                            .And(EffectiveDepositFrequency.InvalidDate().isNull()))
                    .sort(EffectiveDepositFrequency.EffectiveDate().Descending());

            EffectiveDepositFrequency effectiveDepositFrequency = null;
            if (effectiveFrequencies.size() > 0) {
                effectiveDepositFrequency = effectiveFrequencies.get(0);
            } else {
                effectiveFrequencies = companyAgencyPaymentTemplate.getEffectiveDepositFrequencyCollection().find(EffectiveDepositFrequency.InvalidDate().isNull()).sort(EffectiveDepositFrequency.EffectiveDate().Descending());
                if (effectiveFrequencies.size() > 0) {
                    effectiveDepositFrequency = effectiveFrequencies.get(0);
                } else {
                    throw new RuntimeException("No Effective Deposit Frequency Found");
                }
            }
            return effectiveDepositFrequency;
        } else {
            throw new RuntimeException("No Effective Deposit Frequency Found");
        }
    }

    public EffectiveDepositFrequency getLatestInvalidDepositFrequency(Company pCompany, SpcfCalendar pProcessDate) {

        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pCompany, this);

        DomainEntitySet<EffectiveDepositFrequency> effectiveFrequencies = companyAgencyPaymentTemplate
                .getEffectiveDepositFrequencyCollection()
                .find(EffectiveDepositFrequency.EffectiveDate().lessOrEqualThan(pProcessDate)
                        .And(EffectiveDepositFrequency.InvalidDate().isNotNull()))
                .sort(EffectiveDepositFrequency.EffectiveDate().Descending());

        if (effectiveFrequencies.size() > 0) {
            return effectiveFrequencies.get(0);
        }
        return null;

    }

    public DomainEntitySet<EffectiveDepositFrequency> getEffectiveDepositFrequencies(Company pCompany) {

        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pCompany, this);

        DomainEntitySet<EffectiveDepositFrequency> effectiveFrequencies = companyAgencyPaymentTemplate
                .getEffectiveDepositFrequencyCollection()
                .find(EffectiveDepositFrequency.InvalidDate().isNull())
                .sort(EffectiveDepositFrequency.EffectiveDate().Descending());


        return effectiveFrequencies;

    }

    public DomainEntitySet<EffectiveDepositFrequency> getEffectiveDepositFrequencies(PayrollRun pPayrollRun) {

        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pPayrollRun.getCompany(), this);

        DomainEntitySet<EffectiveDepositFrequency> effectiveFrequencies = companyAgencyPaymentTemplate
                .getEffectiveDepositFrequencyCollection()
                .find(EffectiveDepositFrequency.InvalidDate().isNull()
                        .And(EffectiveDepositFrequency.EffectiveDate().greaterOrEqualThan(pPayrollRun.getPaycheckDate())))
                .sort(EffectiveDepositFrequency.EffectiveDate().Descending());


        return effectiveFrequencies;

    }

    public DomainEntitySet<EffectiveDepositFrequency> getEffectiveDepositFrequenciesEffectiveAfter(Company pCompany, SpcfCalendar pEffectiveAfterDate) {

        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pCompany, this);

        DomainEntitySet<EffectiveDepositFrequency> effectiveFrequencies = companyAgencyPaymentTemplate
                .getEffectiveDepositFrequencyCollection()
                .find(EffectiveDepositFrequency.InvalidDate().isNull()
                        .And(EffectiveDepositFrequency.EffectiveDate().greaterThan(pEffectiveAfterDate)))
                .sort(EffectiveDepositFrequency.EffectiveDate().Descending());


        return effectiveFrequencies;

    }

    public void adjustDueDateAndFrequency(PayrollRun pPayrollRun, MoneyMovementTransaction pMMT) {

        EffectiveDepositFrequency edf = getEffectiveDepositFreq(pPayrollRun.getCompany(), pPayrollRun.getPaycheckDate());

        if (edf != null) {
            DepositFrequencyCode depositFrequencyCode = edf.getPaymentTemplateFrequency().getPaymentFrequencyId();
            IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(getPaymentTemplateCd(), depositFrequencyCode.toString(), CalendarUtils.convertToRulesCalendar(pPayrollRun.getPaycheckDate()));
            // DomainEntitySet<PaymentTemplateFrequency> frequencies = getPaymentTemplateFrequencyCollection().find(PaymentTemplateFrequency.PaymentFrequencyId().equalTo(depositFrequencyCode));

            pMMT.setDueDate(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getDueDate()));
            SpcfCalendar debitInitiationDate = pPayrollRun.getEmployerTaxDebitTransaction() != null ? pPayrollRun.getEmployerTaxDebitTransaction().getInitiationDate() : null;
            SpcfCalendar initDate = MoneyMovementTransaction.getPaymentInitDate(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getDueDate()), pMMT.getMoneyMovementPaymentMethod(), depositFrequencyCode, pMMT.getPaymentTemplate(), pPayrollRun.isBackDated(), debitInitiationDate, pMMT.isTaxPayment());
            pMMT.updateTaxInitiationDate(initDate);
            PaymentTemplateFrequency paymentTemplateFrequency = getPaymentTemplateFrequency(depositFrequencyCode.toString());
            pMMT.updatePaymentFrequency(paymentTemplateFrequency);
            pMMT = Application.save(pMMT);
        }
    }

    public FormTemplate findDefaultFormTemplate() {
        Expression<FormTemplate> query = new Query<FormTemplate>()
                .Where(FormTemplate.PaymentTemplate().PaymentTemplateCd().equalTo(getPaymentTemplateCd())
                        .And(FormTemplate.DefaultFormTemplate().isNotNull()));

        DomainEntitySet<FormTemplate> formTemplateList = Application.find(FormTemplate.class, query);

        if (formTemplateList.isEmpty() || formTemplateList.size() < 1 || formTemplateList.size() > 1) {
            throw new RuntimeException("Found zero or more than one default payment templates found for template: " + getPaymentTemplateCd());
        }
        return formTemplateList.get(0);
    }

    public boolean isSupportedAsOfDate(SpcfCalendar pCompareDate) {
        return compareSupportDate(this.getSupportStartDate(), pCompareDate);
    }

    public boolean wasOriginallySupportedAsOfDate(SpcfCalendar pCompareDate) {
        return compareSupportDate(this.getProcessingStartDate(), pCompareDate);
    }

    private boolean compareSupportDate(SpcfCalendar pSupportDate, SpcfCalendar pCompareDate) {
        if (pSupportDate != null) {
            CalendarUtils.clearTime(pSupportDate);
            SpcfCalendar compareDate = pCompareDate.copy();
            CalendarUtils.clearTime(compareDate);
            return (pSupportDate.compareTo(compareDate) != 1);
        } else {
            return false;
        }
    }

    public boolean existsInFormTemplates() {
        Expression<FormTemplate> query = new Query<FormTemplate>()
                .Where(FormTemplate.PaymentTemplate().PaymentTemplateCd().equalTo(getPaymentTemplateCd()));

        DomainEntitySet<FormTemplate> formTemplateList = Application.find(FormTemplate.class, query);

        return !formTemplateList.isEmpty();
    }

    public void createCompanyAgencyFormTemplate(CompanyAgency companyAgency) {
        if (existsInFormTemplates()) {
            CompanyAgencyFormTemplate companyAgencyFT = new CompanyAgencyFormTemplate();
            SpcfCalendar effectiveDate;

            if (companyAgency.getIntuitResponsibilityStartDate() != null) {
                effectiveDate = companyAgency.getIntuitResponsibilityStartDate();
            } else {
                effectiveDate = this.getSupportStartDate();
            }

            companyAgencyFT.setEffectiveDate(effectiveDate);
            companyAgencyFT.setFormTemplate(findDefaultFormTemplate());
            companyAgencyFT = Application.save(companyAgencyFT);

            companyAgencyFT.setCompanyAgency(companyAgency);
            companyAgency.addCompanyAgencyFormTemplate(companyAgencyFT);

            companyAgency = Application.save(companyAgency);
        }
    }

    public ITxpRecordManager getTxpRecordManager() {
        ITxpRecordManager txpRecordManager = null;
        String txpRecordClass = getTxpRecordClass();

        if ((txpRecordClass != null) && (txpRecordClass.length() > 0)) {
            try {
                Class txpClass = Class.forName(txpRecordClass);
                Object txpInstance = txpClass.newInstance();

                if (txpInstance instanceof ITxpRecordManager) {
                    txpRecordManager = (ITxpRecordManager) txpInstance;
                } else {
                    throw new RuntimeException(String.format("Specified TXP class %s does not implement ITxpRecordManager interface.", txpRecordClass));
                }
            } catch (Throwable t) {
                throw new RuntimeException(String.format("Error instantiating TXP record class %s ", txpRecordClass), t);
            }
        }

        return txpRecordManager;
    }

    public BankAccount getActiveBankAccount() {
        DomainEntitySet<PaymentTemplateBankAccount> paymentTemplateBankAccounts = Application.find(PaymentTemplateBankAccount.class, PaymentTemplateBankAccount.PaymentTemplate().equalTo(this)
                .And(PaymentTemplateBankAccount.StatusCd().equalTo(BankAccountStatus.Active)));
        if (paymentTemplateBankAccounts.size() > 1) {
            throw new RuntimeException("More than one Bank account is associated with Payment Template -" + this.getPaymentTemplateCd());
        }
        if (paymentTemplateBankAccounts.size() > 0) {
            return paymentTemplateBankAccounts.get(0).getBankAccount();
        }
        return null;
    }

    public DomainEntitySet<PaymentTemplateAgencyId> getAgencyIds() {
        return Application.find(PaymentTemplateAgencyId.class, PaymentTemplateAgencyId.PaymentTemplate().equalTo(this));
    }

    private DomainEntitySet<PaymentTemplatePaymentMethod> mPaymentTemplatePaymentMethods = null;

    public DomainEntitySet<PaymentTemplatePaymentMethod> getPaymentTemplatePaymentMethods() {
        if(mPaymentTemplatePaymentMethods == null) {
            mPaymentTemplatePaymentMethods = Application.find(PaymentTemplatePaymentMethod.class, new Query<PaymentTemplatePaymentMethod>()
                    .Where(PaymentTemplatePaymentMethod.PaymentTemplate().equalTo(this))
                    .OrderBy(PaymentTemplatePaymentMethod.PaymentMethodOrder()));
        }
        return mPaymentTemplatePaymentMethods;
    }

    public PaymentTemplatePaymentMethod getPaymentTemplatePaymentMethod(PaymentMethod paymentMethod) {
        return getPaymentTemplatePaymentMethods().find(PaymentTemplatePaymentMethod.PaymentMethod().equalTo(paymentMethod)
                                                               .And(PaymentTemplatePaymentMethod.PaymentTemplate().equalTo(this)))
                .getFirst();
    }

    /**
     *  Returns all the Payment Template for the state which is having payment method as ACHCredit
     *
     * @param state
     */
    public static List<String> getPaymentTemplatesWithACHCreditForState(String state) {
        List<String> paymentTemplateWithACH = new ArrayList<String>();
        DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.PaymentTemplateCd().like(state + "-%"));
        for (PaymentTemplate paymentTemplate : paymentTemplates) {
            for(PaymentTemplatePaymentMethod paymentMethod: paymentTemplate.getPaymentTemplatePaymentMethods()){
                if(paymentMethod.getPaymentMethod() == PaymentMethod.ACHCredit){
                    paymentTemplateWithACH.add(paymentTemplate.getPaymentTemplateCd());
                }
            }
        }

        return paymentTemplateWithACH;
    }

    public DomainEntitySet<CompanyAgencyPaymentTemplate> findActiveCompanies() {
        Criterion<CompanyAgencyPaymentTemplate> captCriteria = CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(this);

        return Application.find(CompanyAgencyPaymentTemplate.class, captCriteria);

    }


    public boolean isFollowsFederal() {
        IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();
        IRulesPaymentTemplate iRulesPaymentTemplate = rulesInfo.getPaymentTemplate(getPaymentTemplateCd());
        if (iRulesPaymentTemplate != null && iRulesPaymentTemplate.getUsesFrequencyOf() != null && iRulesPaymentTemplate.getUsesFrequencyOf().equals(IRS_941)) {
            return true;
        }
        return false;
    }
    public boolean followsPaymentTemplateThreshold() {
        IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();
        IRulesPaymentTemplate iRulesPaymentTemplate = rulesInfo.getPaymentTemplate(getPaymentTemplateCd());
        if (iRulesPaymentTemplate != null && iRulesPaymentTemplate.getUsesFrequencyOf() != null) {
            return true;
        }
        return false;
    }

    public PaymentTemplate getFollowedPaymentTemplate() {
        IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();
        IRulesPaymentTemplate iRulesPaymentTemplate = rulesInfo.getPaymentTemplate(getPaymentTemplateCd());
        if (iRulesPaymentTemplate != null && iRulesPaymentTemplate.getUsesFrequencyOf() != null) {
            return PaymentTemplate.findPaymentTemplate( iRulesPaymentTemplate.getUsesFrequencyOf());
        }
        return null;
    }

    public boolean isIRS941() {
        return this.getPaymentTemplateCd().equals(IRS_941);
    }

    public boolean isIRS940() {
        return this.getPaymentTemplateCd().equals(IRS_940);
    }

    //default is quarterly, but FUTA is filed annually.
    public boolean isRolledUpAnnually() {
        return isIRS940();
    }

    public PaymentTemplateFrequency findSupportedPaymentTemplateFrequency(DepositFrequencyCode pDepositFrequencyCode) {
        for (PaymentTemplateFrequency paymentTemplateFrequency : getPaymentTemplateFrequencyCollection()) {
            if (paymentTemplateFrequency.getPaymentFrequencyId().equals(pDepositFrequencyCode)) {
                return paymentTemplateFrequency;
            }
        }
        return null;
    }

    public static DomainEntitySet<PaymentTemplate> getPaymentTemplatesFromSystemParameter(SpcfCalendar pDate, boolean pCoupon, boolean pRecon) {
        DomainEntitySet<PaymentTemplate> paymentTemplates = new DomainEntitySet<PaymentTemplate>();
        String ptString = "";
        if (pCoupon) {
            ptString = SystemParameter.findStringValue(SystemParameter.Code.ZERO_PAYMENT_COUPON_REPORT_REQUIRED);
        }
        if (pRecon) {
            if (!ptString.isEmpty()) {
                ptString = ptString + ",";
            }
            ptString = ptString + SystemParameter.findStringValue(SystemParameter.Code.ZERO_PAYMENT_RECON_FILE_REQUIRED);
        }
        String[] paymentTemplateCds = ptString.split(",");
        for (String ptCode : paymentTemplateCds) {
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(ptCode.trim());
            if (paymentTemplate != null && paymentTemplate.isSupportedAsOfDate(pDate)) {
                paymentTemplates.add(paymentTemplate);
            }
        }
        return paymentTemplates;
    }
    
    public DepositFrequencyCode getOverrideDepositFrequency(Company pCompany, PaymentMethod pPaymentMethod) {
        // NY Metro should always be quarterly if the payment method is not ach credit
        if(getPaymentTemplateCd().equals(NY_METRO)) {
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pCompany, this);
            if(pPaymentMethod == null && companyAgencyPaymentTemplate != null) {
                pPaymentMethod = companyAgencyPaymentTemplate.getCurrentPaymentMethod();
            }

            if(pPaymentMethod != PaymentMethod.ACHCredit) {
                return DepositFrequencyCode.QUARTERLY;
            }
        }
        
        return null;
    } 
    
    public String getCategoryInShortForm() {
        if(getCategory() == null) {
            return null;
        }

        switch(getCategory()) {
            case SUI:
                return "UI";
            case Withholding:
                return "WH";
            case Other:
                return "Other";
            default:
                return null;
        }
    }

    public Law getPrimarySUILaw() {
        return getLawCollection().findEntity(Law.LawTypeCd().like("%SUI-ER%"));
    }

    public PaymentMethod getAgentEnabledRequiredPaymentMethod() {
        for (PaymentTemplatePaymentMethod paymentTemplatePaymentMethod : getPaymentTemplatePaymentMethods()) {
            for (PaymentMethodRequirement paymentMethodRequirement : paymentTemplatePaymentMethod.getPaymentMethodRequirementCollection()) {
                if (paymentMethodRequirement instanceof ManualRequirement) {
                    return paymentTemplatePaymentMethod.getPaymentMethod();
                }
            }
        }
        return null;
    }

    /**
     * PSP-14128: Add NY MTA305 PAYMENT to the list of payments that can be Finalized
     * Return true if the payment template needs to be finalized
     * @param paymentTemplate
     * @return
     */
    public static boolean isToBeFinalizedNonSUIPaymentTemplate(PaymentTemplate paymentTemplate){
        return paymentTemplate.getPaymentTemplateCd().equals(PaymentTemplate.NY_METRO);
    }

}
