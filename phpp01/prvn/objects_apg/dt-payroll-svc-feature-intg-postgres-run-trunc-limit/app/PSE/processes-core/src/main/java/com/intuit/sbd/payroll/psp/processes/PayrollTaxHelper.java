package com.intuit.sbd.payroll.psp.processes;

import com.intuit.payroll.agency.api.IPaymentPeriod;
import com.intuit.payroll.agency.impl.UpperLimit;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Apr 19, 2011
 * Time: 8:34:04 AM
 */
public class PayrollTaxHelper {

    public static ProcessResult createTaxTransactions(PayrollRun pPayrollRun, CreateTaxTransactionsOptions options) {
        return createTaxTransactions(pPayrollRun, options.debitCustomer, options.lawAmountMap, options.alwaysRecordFTs, options.useVarianceAccount, options.creditCustomer, options.SUIAdjustment, options.settlementDate);
    }

    private static ProcessResult createTaxTransactions(PayrollRun pPayrollRun, boolean debitCustomer, Map<Law, SpcfDecimal> lawAmountMap, boolean pAlwaysRecordFTs, boolean pUseVarianceAccount, boolean pCreditCustomer, boolean pSUIAdjustment, DateDTO pSettlementDate) {
        ProcessResult processResult = new ProcessResult();
        if (!pAlwaysRecordFTs && pPayrollRun.isHistoricalPayroll()) {
            return processResult;
        }

        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(pPayrollRun.getCompany());
        BankAccount debitBankAccount = getIntuitTaxBankAccount();

        Map<PaymentTemplate, MoneyMovementTransaction> payrollRunPayments = new HashMap<PaymentTemplate, MoneyMovementTransaction>();

        // remove unsupported laws
        List<Law> unsupportedLaws = findUnsupportedLaws(lawAmountMap.keySet(), pPayrollRun.getPaycheckDate());
        for (Law unsupportedLaw : unsupportedLaws) {
            lawAmountMap.remove(unsupportedLaw);
        }

        if (containsLawsNotOriginallySupportedOnPaycheckDate(lawAmountMap.keySet(), pPayrollRun.getPaycheckDate())) {
            CompanyEvent.createBackdatePriorToProcessingStartEvent(pPayrollRun);
        }

        // create agency tax credits
        SpcfMoney totalAtcAmount = new SpcfMoney(SpcfMoney.ZERO);
        for (Law law : lawAmountMap.keySet()) {
            FinancialTransaction agencyTaxCredit = createTaxCredit(pPayrollRun, law, new SpcfMoney(lawAmountMap.get(law)));
            if (agencyTaxCredit != null) {
                payrollRunPayments.put(law.getPaymentTemplate(), agencyTaxCredit.getMoneyMovementTransaction());
                totalAtcAmount = new SpcfMoney(totalAtcAmount.add(agencyTaxCredit.getFinancialTransactionAmount()));
                if (pUseVarianceAccount) {
                    FinancialTransaction erSUITaxReceivable = FinancialTransaction.createFinancialTransaction(pPayrollRun.getCompany(), pPayrollRun, null, null, null,
                            BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit, TransactionTypeCode.EmployerSUITaxReceivable,
                            agencyTaxCredit.getFinancialTransactionAmount(),
                            SettlementType.ApplyForward,
                            agencyTaxCredit.getSettlementDate(), law);
                    agencyTaxCredit.setRelatableTransaction(erSUITaxReceivable);
                    erSUITaxReceivable.setMoneyMovementTransaction(agencyTaxCredit.getMoneyMovementTransaction());
                    agencyTaxCredit.getMoneyMovementTransaction().addFinancialTransaction(erSUITaxReceivable);
                }
            }
        }

        // create agency tax debit and overpayments
        SpcfMoney totalAtdAmount = new SpcfMoney(SpcfMoney.ZERO);

        //basically a hack to process SUI _last_ so that any overpayment is on the right law
        List<Law> sortedLaws = new ArrayList<Law>(lawAmountMap.keySet());
        Collections.sort(sortedLaws, new Comparator<Law>() {
            public int compare(Law o1, Law o2) {
                if (o1.isSUIER() && !o2.isSUIER()) {
                    return 1;
                } else if (o2.isSUIER() && !o1.isSUIER()) {
                    return -1;
                } else {
                    return o1.getLawId().compareTo(o2.getLawId());
                }
            }
        });

        for (Law law : sortedLaws) {
            SpcfMoney atdAmount = createAgencyTaxDebitsAndOverpayments(pPayrollRun, law, lawAmountMap.get(law), debitBankAccount, payrollRunPayments, pUseVarianceAccount, pCreditCustomer);
            if (atdAmount != null) {
                totalAtdAmount = new SpcfMoney(totalAtdAmount.add(atdAmount));
            }
        }

        // calculate employer debit
        if (debitCustomer) {
            createEmployerTaxDebit(pPayrollRun, companyBankAccount, debitBankAccount, null, payrollRunPayments, pSettlementDate);
        }

        // For SUI Adjustments if the amount of created atds is greater than the amount of catcs created and the agent chooses to not use the variance account and
        // refund immediately, create the refund - EmployerTaxCredit with the net amount of atds - atcs created
        SpcfMoney creditAmount = new SpcfMoney(SpcfMoney.ZERO);
        if (totalAtdAmount.isGreaterThan(SpcfMoney.ZERO)) {
            creditAmount = new SpcfMoney(totalAtdAmount.subtract(totalAtcAmount));
        }

        if (creditAmount.isGreaterThan(SpcfMoney.ZERO) && pCreditCustomer && !pUseVarianceAccount) {
            createEmployerTaxCredit(pPayrollRun, companyBankAccount, debitBankAccount, creditAmount);
        }

        // Verify thresholds for each Payment Template in the Payroll
        processResult.merge(processThreshold(pPayrollRun, lawAmountMap.keySet()));

        if (!processResult.isSuccess()) {
            return processResult;
        }

        //Do not apply if SUI adjustment
        if (!pSUIAdjustment) {
            applyPendingCredits(pPayrollRun.getCompany());
            applyAgencyTaxOverpayments(pPayrollRun.getCompany(), pPayrollRun.getPaycheckDate());
        }

        //If payroll does not have ERTaxDb or ERTaxDirectDb in any of these states Created, Completed, Executed, Returned then update Payroll to Complete if it is Pending
        checkForPayrollCompletion(pPayrollRun);

        combinePayments(pPayrollRun.getCompany());

        return processResult;
    }

    public static class CreateTaxTransactionsOptions {
        private boolean debitCustomer;
        private Map<Law, SpcfDecimal> lawAmountMap;
        private boolean alwaysRecordFTs;
        private boolean useVarianceAccount;
        private boolean creditCustomer;
        private boolean SUIAdjustment;
        private DateDTO settlementDate;

        public CreateTaxTransactionsOptions(boolean pDebitCustomer, Map<Law, SpcfDecimal> pLawAmountMap, boolean pAlwaysRecordFTs, boolean pUseVarianceAccount, boolean pCreditCustomer, boolean pSUIAdjustment, DateDTO pSettlementDate) {
            debitCustomer = pDebitCustomer;
            lawAmountMap = pLawAmountMap;
            alwaysRecordFTs = pAlwaysRecordFTs;
            useVarianceAccount = pUseVarianceAccount;
            creditCustomer = pCreditCustomer;
            SUIAdjustment = pSUIAdjustment;
            settlementDate = pSettlementDate;
        }

        public CreateTaxTransactionsOptions(boolean pDebitCustomer, Map<Law, SpcfDecimal> pLawAmountMap) {
            debitCustomer = pDebitCustomer;
            lawAmountMap = pLawAmountMap;
        }

        public CreateTaxTransactionsOptions(boolean pDebitCustomer, Map<Law, SpcfDecimal> pLawAmountMap, boolean pAlwaysRecordFTs, boolean pCreditCustomer) {
            debitCustomer = pDebitCustomer;
            lawAmountMap = pLawAmountMap;
            alwaysRecordFTs = pAlwaysRecordFTs;
            creditCustomer = pCreditCustomer;
        }


    }

    public static ProcessResult updateTaxTransactions(PayrollRun pPayrollRun, Map<Law, SpcfDecimal> pLiabilityAmountUpdates) {
        ProcessResult processResult = new ProcessResult();
        if (pPayrollRun.isHistoricalPayroll()) {
            return processResult;
        }

        PayrollRun.getPayrollsInMemory(pPayrollRun.getCompany()).add(pPayrollRun);
        boolean taxImpoundHasOffloaded = pPayrollRun.hasTaxImpoundOffloaded();
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(pPayrollRun.getCompany());
        BankAccount intuitTaxAccount = getIntuitTaxBankAccount();

        // remove unsupported laws
        SpcfCalendar paycheckDate = pPayrollRun.getPaycheckDate();
        List<Law> unsupportedLaws = findUnsupportedLaws(pLiabilityAmountUpdates.keySet(), paycheckDate);
        for (Law unsupportedLaw : unsupportedLaws) {
            pLiabilityAmountUpdates.remove(unsupportedLaw);
        }

        // find all of the laws for the templates
        DomainEntitySet<Law> templateLaws = new DomainEntitySet<Law>();
        for (Law law : pLiabilityAmountUpdates.keySet()) {
            templateLaws.addAll(law.getPaymentTemplate().getLawCollection());
        }

        // check for positive liability being added after offload of debit
        if (taxImpoundHasOffloaded) {
            for (SpcfDecimal liabilityAmount : pLiabilityAmountUpdates.values()) {
                if (liabilityAmount.isGreaterThan(SpcfMoney.ZERO)) {
                    processResult.getMessages().GenericError(EntityName.PayrollRun,
                            pPayrollRun.getSourcePayRunId(),
                            "Positive liability cannot be added to a payroll that has already offloaded");
                    return processResult;
                }
            }
        }

        Map<PaymentTemplate, MoneyMovementTransaction> payrollRunPayments = new HashMap<PaymentTemplate, MoneyMovementTransaction>();

        // cancel pending transactions
        if (!taxImpoundHasOffloaded) {
            cancelPendingEmployerTaxCredits(pPayrollRun, templateLaws, null);

            // cancel remaining tax debits
            DomainEntitySet<FinancialTransaction> agencyDebits =
                    pPayrollRun.getFinancialTransactionCollection().find(
                            FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxDebit,
                                    TransactionTypeCode.AgencyDirectOverpayment)
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created))
                                    .And(FinancialTransaction.Law().in(templateLaws.toArray(new Law[templateLaws.size()])))
                                    .And(FinancialTransaction.MoneyMovementTransaction().Status().in(PaymentStatus.Created, PaymentStatus.OnHold)));
            for (FinancialTransaction financialTransaction : agencyDebits) {
                Law law = financialTransaction.getLaw();
                SpcfDecimal liabilityAmount = pLiabilityAmountUpdates.get(law);
                if (liabilityAmount == null) {
                    liabilityAmount = SpcfMoney.ZERO;
                }

                liabilityAmount = liabilityAmount.subtract(financialTransaction.getFinancialTransactionAmount());
                pLiabilityAmountUpdates.put(financialTransaction.getLaw(), liabilityAmount);

                financialTransaction.cancelFinancialTransaction();
            }

            // cancel tax credits
            DomainEntitySet<FinancialTransaction> agencyCredits =
                    pPayrollRun.getFinancialTransactionCollection().find(
                            FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit,
                                    TransactionTypeCode.AgencyDirectCredit)
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created))
                                    .And(FinancialTransaction.Law().in(templateLaws.toArray(new Law[templateLaws.size()])))
                                    .And(FinancialTransaction.MoneyMovementTransaction().Status().in(PaymentStatus.Created, PaymentStatus.OnHold)));
            for (FinancialTransaction financialTransaction : agencyCredits) {
                Law law = financialTransaction.getLaw();
                SpcfDecimal liabilityAmount = pLiabilityAmountUpdates.get(law);
                if (liabilityAmount == null) {
                    liabilityAmount = SpcfMoney.ZERO;
                }

                liabilityAmount = liabilityAmount.add(financialTransaction.getFinancialTransactionAmount());
                pLiabilityAmountUpdates.put(financialTransaction.getLaw(), liabilityAmount);

                financialTransaction.cancelFinancialTransaction();
            }
        }

        // create agency tax credits
        for (Law law : pLiabilityAmountUpdates.keySet()) {
            FinancialTransaction agencyTaxCredit = createTaxCredit(pPayrollRun, law, new SpcfMoney(pLiabilityAmountUpdates.get(law)));
            if (agencyTaxCredit != null) {
                payrollRunPayments.put(law.getPaymentTemplate(), agencyTaxCredit.getMoneyMovementTransaction());
            }
        }

        // if the impound has already offloaded, but the direct debit has not offloaded yet recalc the direct debit do not convert it in
        if (taxImpoundHasOffloaded) {
            FinancialTransaction directDebit = pPayrollRun.getUnsubmittedDirectImpound();
            if (directDebit != null) {
                MoneyMovementTransaction directDebitMMT = directDebit.getMoneyMovementTransaction();
                for (Law law : directDebitMMT.getPaymentTemplate().getLawCollection()) {
                    SpcfDecimal transactionAmount = pLiabilityAmountUpdates.get(law);
                    if (transactionAmount != null && transactionAmount.isLessThan(SpcfMoney.ZERO)) {
                        FinancialTransaction agencyDirectDebit =
                                FinancialTransaction.createFinancialTransaction(pPayrollRun.getCompany(), pPayrollRun, null, null, null, BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit,
                                        TransactionTypeCode.AgencyDirectDebit,
                                        new SpcfMoney(transactionAmount.negate()),
                                        SettlementType.ApplyForward,
                                        null, law);
                        if (agencyDirectDebit.getMoneyMovementTransaction() != null) {
                            MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(agencyDirectDebit);
                        }
                        agencyDirectDebit.setMoneyMovementTransaction(directDebitMMT);
                        directDebitMMT.addAgencyFinancialTransaction(agencyDirectDebit);
                        pLiabilityAmountUpdates.put(law, SpcfMoney.ZERO);
                    }
                }

                // if the payment is negative start canceling overpayment applied transactions, smallest to largest (because I don't have time to refactor the overpayment application code)
                if (directDebitMMT.getMoneyMovementTransactionAmount().isLessThan(SpcfMoney.ZERO)) {
                    DomainEntitySet<FinancialTransaction> overpaymentTransactions = directDebitMMT.getFinancialTransactionCollection()
                            .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDirectOverpaymentApplied));
                    overpaymentTransactions = overpaymentTransactions.sort(FinancialTransaction.FinancialTransactionAmount());
                    for (FinancialTransaction financialTransaction : overpaymentTransactions) {
                        if (directDebitMMT.getMoneyMovementTransactionAmount().isLessThan(SpcfMoney.ZERO)) {
                            financialTransaction.cancelFinancialTransaction();
                            if (financialTransaction.getRelatableTransaction() != null) {
                                financialTransaction.getRelatableTransaction().cancelFinancialTransaction();
                            }
                        }
                    }
                }

                // cancel and recreate the er direct debit transaction
                directDebit.cancelFinancialTransaction();
                FinancialTransaction directDebitTransaction =
                        FinancialTransaction.createERDebitTransaction(pPayrollRun, companyBankAccount,
                                TransactionTypeCode.EmployerTaxDirectDebit, directDebitMMT.getMMTBalance(),
                                SettlementType.EFTPSDirectDebit, directDebitMMT.generatePaymentSettlementDate(), null);
                directDebitTransaction.setMoneyMovementTransaction(directDebitMMT);
                directDebitMMT.addFinancialTransaction(directDebitTransaction);
            }
        }

        // create agency tax debit and overpayments
        for (Law law : pLiabilityAmountUpdates.keySet()) {
            createAgencyTaxDebitsAndOverpayments(pPayrollRun, law, pLiabilityAmountUpdates.get(law), intuitTaxAccount, payrollRunPayments, false, false);
        }

        // calculate employer debit
        if (!taxImpoundHasOffloaded) {
            // cancel all of the pending employer debits
            DomainEntitySet<FinancialTransaction> employerTransactions =
                    pPayrollRun.getFinancialTransactionCollection()
                            .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDebit,
                                    TransactionTypeCode.EmployerTaxDirectDebit)
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));

            FinancialTransaction oldEmployerTaxDebit = null;
            // Get a list of the payment Templates to check if Federal payments are included so we can cancel the Direct Debit
            List<PaymentTemplate> paymentTemplates = getPaymentTemplates(pLiabilityAmountUpdates.keySet());

            for (FinancialTransaction employerTransaction : employerTransactions) {
                // Only Cancel EmployerDirectDebit if Federal Payment templates are included
                if ((employerTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxDirectDebit) &&
                        (paymentTemplates.contains(PaymentTemplate.getIRS_940()) || paymentTemplates.contains(PaymentTemplate.getIRS_941()))) {
                    employerTransaction = employerTransaction.cancelFinancialTransaction();
                }

                // use the old debit transaction for the settlement date
                if (employerTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxDebit) {
                    employerTransaction = employerTransaction.cancelFinancialTransaction();
                    oldEmployerTaxDebit = employerTransaction;
                }
            }

            createEmployerTaxDebit(pPayrollRun, companyBankAccount, intuitTaxAccount, oldEmployerTaxDebit, payrollRunPayments, null);
        }

        // Verify thresholds for each Payment Template in the Payroll

        processResult.merge(processThreshold(pPayrollRun, pLiabilityAmountUpdates.keySet()));
        if (!processResult.isSuccess()) {
            return processResult;
        }

        applyPendingCredits(pPayrollRun.getCompany());

        applyAgencyTaxOverpayments(pPayrollRun.getCompany(), pPayrollRun.getPaycheckDate());

        //If payroll does not have ERTaxDb or ERTaxDirectDb in any of these states Created, Completed, Executed, Returned then update Payroll to Complete if it is Pending
        checkForPayrollCompletion(pPayrollRun);

        combinePayments(pPayrollRun.getCompany());

        return processResult;
    }

    private static void cancelPendingEmployerTaxCredits(PayrollRun pPayrollRun, DomainEntitySet<Law> pTemplateLaws, TransactionTypeCode pTransactionTypeCode) {
        if (pTransactionTypeCode == null || pTransactionTypeCode == TransactionTypeCode.EmployerTaxDebit) {
            // cancel all pending credit applied transactions
            DomainEntitySet<FinancialTransaction> employerCreditTransactions =
                    pPayrollRun.getFinancialTransactionCollection()
                               .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxCreditApplied)
                                                         .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
            for (FinancialTransaction employerCreditTransaction : employerCreditTransactions) {
                employerCreditTransaction.cancelFinancialTransaction();
            }
        }

        TransactionTypeCode[] transactionTypeCodes;
        if(pTransactionTypeCode == TransactionTypeCode.EmployerTaxDebit) {
            transactionTypeCodes = new TransactionTypeCode[] {TransactionTypeCode.EmployerTaxDirectOverpaymentApplied};
        } else if(pTransactionTypeCode == TransactionTypeCode.EmployerTaxDirectDebit) {
            transactionTypeCodes = new TransactionTypeCode[] {TransactionTypeCode.EmployerTaxDirectOverpaymentApplied};
        } else {
            transactionTypeCodes = new TransactionTypeCode[] {TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionTypeCode.EmployerTaxDirectOverpaymentApplied};
        }

        Criterion<FinancialTransaction> overpaymentsCriterion =
                FinancialTransaction.TransactionType().TransactionTypeCd().in(transactionTypeCodes)
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created));
        if(pTemplateLaws != null && pTemplateLaws.size() > 0) {
            overpaymentsCriterion = overpaymentsCriterion.And(FinancialTransaction.Law().in(pTemplateLaws.toArray(new Law[pTemplateLaws.size()])));
        }

        // cancel all of the pending employer transactions for the law -> templates being updated
        if(pTemplateLaws == null || pTemplateLaws.size() > 0) {
            DomainEntitySet<FinancialTransaction> employerOverpaymentTransactions =
                    pPayrollRun.getFinancialTransactionCollection()
                               .find(overpaymentsCriterion);

            for (FinancialTransaction employerTransaction : employerOverpaymentTransactions) {
                // also cancel the related agency tax debit for EmployerTaxOverpaymentApplied transactions
                if (employerTransaction.getRelatableTransaction() != null) {
                    if(employerTransaction.getRelatableTransaction().getMoneyMovementTransaction() == null ||
                            employerTransaction.getRelatableTransaction().getMoneyMovementTransaction().isPendingTaxPayment()) {
                        employerTransaction = employerTransaction.cancelFinancialTransaction();
                        employerTransaction.getRelatableTransaction().cancelFinancialTransaction();
                    }
                } else {
                    employerTransaction.cancelFinancialTransaction();
                }
            }
        }
    }

    public static void checkForPayrollCompletion(PayrollRun pPayrollRun) {
        if (pPayrollRun.getEmployerTaxDebitTransaction() == null &&
                pPayrollRun.getEmployerTaxDirectDebitTransaction() == null &&
                pPayrollRun.getEmployerDirectDepositDebitTransaction() == null &&
                pPayrollRun.getPayrollRunStatus().equals(PayrollStatus.Pending)) {
            pPayrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        }
    }

    /*
    * Ideally we could send the refund immediately.  However, we must guard against the risk that we would be refunding money that could return
    * Therefore, we will set the refund's settlement date to:
    *   Max {earliest possible date, [(settlement date of each executed ERTaxDebit or ERTaxRedebit on a payroll with an AgencyTaxDebit) + 5]}
    */

    public static SpcfCalendar getRefundSettlementDate(Company pCompany) {
        SpcfCalendar latestSettlementDate = null;
        for (FinancialTransaction atd : FinancialTransaction.findAllFinancialTransaction(pCompany, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Completed)) {
            PayrollRun payrollRun = atd.getPayrollRun();
            if (payrollRun != null) {
                DomainEntitySet<FinancialTransaction> impounds =
                        payrollRun.getEmployerTaxDebitTransactions()
                                .find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Executed));
                if (!impounds.isEmpty()) {
                    FinancialTransaction latestImpoundForPayroll = impounds.sort(FinancialTransaction.SettlementDate().Descending()).get(0);
                    if (latestSettlementDate == null || latestImpoundForPayroll.getSettlementDate().after(latestSettlementDate)) {
                        latestSettlementDate = latestImpoundForPayroll.getSettlementDate();
                    }
                }
            }
        }


        SpcfCalendar earliestPossibleSettlementDate = FinancialTransaction.getSettlementDate(pCompany.getOffloadGroup());
        if (latestSettlementDate == null) {
            return earliestPossibleSettlementDate;
        } else {
            SpcfCalendar newSettlementDate;
            newSettlementDate = latestSettlementDate.copy().toLocal();
            CalendarUtils.addBusinessDays(newSettlementDate, 5);
            CalendarUtils.clearTime(newSettlementDate);
            if (newSettlementDate.after(earliestPossibleSettlementDate)) {
                return newSettlementDate;
            } else {
                return earliestPossibleSettlementDate;
            }

        }

    }

    private static ProcessResult processThreshold(PayrollRun pPayrollRun, Set<Law> pLaws) {
        ProcessResult processResult = new ProcessResult();
        List<PaymentTemplate> paymentTemplates = new ArrayList<PaymentTemplate>();
        for (Law law : pLaws) {
            if (!paymentTemplates.contains(law.getPaymentTemplate())) {
                paymentTemplates.add(law.getPaymentTemplate());
                processResult.merge(processThreshold(pPayrollRun.getCompany(), law.getPaymentTemplate(), pPayrollRun));
            }
        }
        return processResult;
    }

    private static List<PaymentTemplate> getPaymentTemplates(Set<Law> pLaws) {
        List<PaymentTemplate> paymentTemplates = new ArrayList<PaymentTemplate>();
        for (Law law : pLaws) {
            if (!paymentTemplates.contains(law.getPaymentTemplate())) {
                paymentTemplates.add(law.getPaymentTemplate());
            }
        }
        return paymentTemplates;
    }


    public static BankAccount getIntuitTaxBankAccount() {
        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(TransactionTypeCode.AgencyTaxCredit, CreditDebitCode.Debit);
        if (debitIntuitBankAccount == null) {
            throw new RuntimeException("Intuit Bank Account not found for AgencyTaxCredit transaction type");
        }
        return debitIntuitBankAccount.getBankAccount();
    }

    private static ProcessResult addEnrollmentHold(MoneyMovementTransaction pMoneyMovementTransaction) {
        ProcessResult processResult = new ProcessResult();

        if ((pMoneyMovementTransaction.getCompanyPaymentMethod() == null || !pMoneyMovementTransaction.getCompanyPaymentMethod().getEnabled()) && pMoneyMovementTransaction.isPendingTaxPayment()) {
            processResult.merge(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(pMoneyMovementTransaction, PaymentOnHoldReason.Enrollment));
        }

        return processResult;
    }

    private static List<Law> findUnsupportedLaws(Set<Law> pLaws, SpcfCalendar pPaycheckDate) {
        List<Law> laws = new ArrayList<Law>();
        for (Law law : pLaws) {
            if (!law.getPaymentTemplate().isSupportedAsOfDate(pPaycheckDate)) {
                laws.add(law);
            }
        }

        return laws;
    }

    private static boolean containsLawsNotOriginallySupportedOnPaycheckDate(Set<Law> pLaws, SpcfCalendar pPaycheckDate) {
        for (Law law : pLaws) {
            if (!law.getPaymentTemplate().wasOriginallySupportedAsOfDate(pPaycheckDate)) {
                return true;
            }
        }
        return false;
    }

    private static FinancialTransaction createTaxCredit(PayrollRun pPayrollRun, Law pLaw, SpcfMoney pAmount) {
        if (pAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
            return null;
        }

        return FinancialTransaction.createFinancialTransaction(pPayrollRun.getCompany(), pPayrollRun, null, null, null,
                BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit, TransactionTypeCode.AgencyTaxCredit,
                pAmount,
                FinancialTransaction.getDefaultTaxSettlementType(pPayrollRun.getCompany(), pLaw.getPaymentTemplate()),
                null, pLaw);
    }

    private static SpcfMoney createAgencyTaxDebitsAndOverpayments(PayrollRun pPayrollRun, Law pLaw, SpcfDecimal pLawAmount, BankAccount pDebitBankAccount, Map<PaymentTemplate, MoneyMovementTransaction> pPayrollRunPayments, boolean pVarianceAccount, boolean pCreditCustomer) {


        if (pLawAmount.isGreaterThanEqualTo(SpcfMoney.ZERO)) {
            return null;
        }

        pLawAmount = pLawAmount.negate();

        PaymentTemplate paymentTemplate = pLaw.getPaymentTemplate();

        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.OnHold};
        if (pCreditCustomer || pVarianceAccount) {
            taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.OnHold, TaxPaymentStatus.ATFFinalized};
        }
        DomainEntitySet<MoneyMovementTransaction> pendingPayments =
                MoneyMovementTransaction.findTaxPayments()
                        .setCompany(pPayrollRun.getCompany())
                        .setPaymentTemplate(pLaw.getPaymentTemplate())
                        .setPaycheckDate(pPayrollRun.getPaycheckDate())
                        .setTaxPaymentStatuses(taxPaymentStatuses)
                        .setNonDirect()
                        .find();

        MoneyMovementTransaction currentPayrollPayment = pPayrollRunPayments.get(paymentTemplate);
        if (currentPayrollPayment != null && !pendingPayments.contains(currentPayrollPayment)) {
            pendingPayments.add(currentPayrollPayment);
        }

        SpcfDecimal amountAvailable = SpcfMoney.ZERO;
        for (MoneyMovementTransaction moneyMovementTransaction : pendingPayments) {
            //Ensure that the MMT really is pending (i.e. it's not in the process of being offloaded)
            if (moneyMovementTransaction.isPendingMMT()) {
                amountAvailable = amountAvailable.add(moneyMovementTransaction.getTaxPaymentAmountCollected());
            }
        }

        SpcfDecimal agencyTaxDebitAmount;
        SpcfDecimal agencyTaxOverpaymentAmount = SpcfMoney.ZERO;

        if (amountAvailable.isGreaterThanEqualTo(pLawAmount)) {
            agencyTaxDebitAmount = pLawAmount;
        } else {
            agencyTaxDebitAmount = amountAvailable;
            agencyTaxOverpaymentAmount = pLawAmount.subtract(amountAvailable);
        }

        if (agencyTaxDebitAmount.isGreaterThan(SpcfMoney.ZERO)) {
            FinancialTransaction agencyTaxDebitTransaction =
                    FinancialTransaction.createFinancialTransaction(pPayrollRun.getCompany(), pPayrollRun, null, null, pDebitBankAccount,
                            BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit,
                            TransactionTypeCode.AgencyTaxDebit,
                            new SpcfMoney(agencyTaxDebitAmount),
                            SettlementType.ApplyForward, null, pLaw);

            if (agencyTaxDebitTransaction.getMoneyMovementTransaction() != null) {
                pPayrollRunPayments.put(paymentTemplate, agencyTaxDebitTransaction.getMoneyMovementTransaction());
                if (pVarianceAccount) {
                    FinancialTransaction erSUITaxPayable = FinancialTransaction.createFinancialTransaction(pPayrollRun.getCompany(), pPayrollRun, null, null, null,
                            BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit, TransactionTypeCode.EmployerSUITaxPayable,
                            agencyTaxDebitTransaction.getFinancialTransactionAmount(),
                            SettlementType.ApplyForward,
                            agencyTaxDebitTransaction.getSettlementDate(), pLaw);
                    agencyTaxDebitTransaction.setRelatableTransaction(erSUITaxPayable);
                    erSUITaxPayable.setMoneyMovementTransaction(agencyTaxDebitTransaction.getMoneyMovementTransaction());
                    agencyTaxDebitTransaction.getMoneyMovementTransaction().addFinancialTransaction(erSUITaxPayable);
                }
            }
        }

        if (agencyTaxOverpaymentAmount.isGreaterThan(SpcfMoney.ZERO)) {
            // if the payroll has prior payment applied transactions do not include in overpayment calculation
            DomainEntitySet<FinancialTransaction> appliedHPDETransactions =
                    pPayrollRun.getFinancialTransactionCollection()
                            .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyHPDEPriorPaymentApplied)
                                    .And(FinancialTransaction.Law().equalTo(pLaw)));
            for (FinancialTransaction appliedHPDETransaction : appliedHPDETransactions) {
                agencyTaxOverpaymentAmount = agencyTaxOverpaymentAmount.subtract(appliedHPDETransaction.getFinancialTransactionAmount());
            }

            if (agencyTaxOverpaymentAmount.isGreaterThan(SpcfMoney.ZERO)) {
                FinancialTransaction.createAgencyTaxOverpaymentTransaction(pPayrollRun, pLaw, pDebitBankAccount, agencyTaxOverpaymentAmount);
            }
        }

        return (agencyTaxDebitAmount != null ? new SpcfMoney(agencyTaxDebitAmount) : SpcfMoney.ZERO);
    }

    private static void createEmployerTaxDebit(PayrollRun pPayrollRun, CompanyBankAccount pCompanyBankAccount, BankAccount pIntuitTaxAccount, FinancialTransaction pOldTransaction, Map<PaymentTemplate, MoneyMovementTransaction> pPayrollRunPayments, DateDTO pSettlementDate) {
        Map<PaymentTemplate, SpcfDecimal> debitAmountPerTemplate = getDebitAmountPerTemplate(pPayrollRun);

        SpcfDecimal erTaxDebitAmount = SpcfMoney.ZERO;
        DomainEntitySet<FinancialTransaction> agencyTaxCredits =
                pPayrollRun.getFinancialTransactionCollection()
                           .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit, TransactionTypeCode.EmployerSUITaxCollection)
                                                     .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Voided)));
        for (FinancialTransaction agencyTaxCredit : agencyTaxCredits) {
            erTaxDebitAmount = erTaxDebitAmount.add(agencyTaxCredit.getFinancialTransactionAmount());
        }

        if (erTaxDebitAmount.isGreaterThan(SpcfMoney.ZERO)) {
            // apply credits and overpayments to payroll
            for (PaymentTemplate paymentTemplate : debitAmountPerTemplate.keySet()) {
                SpcfDecimal maxOverPaymentAmountAvailable = calculateMaxOverpaymentAvailable(pPayrollRun, pPayrollRunPayments, debitAmountPerTemplate, paymentTemplate);
                pPayrollRun.applyPendingOverPayments(paymentTemplate, maxOverPaymentAmountAvailable, pPayrollRun.getPaycheckDate(), false, pIntuitTaxAccount);
            }

            // find and add up applied amounts
            DomainEntitySet<FinancialTransaction> appliedOverpaymentTransactions =
                    pPayrollRun.getFinancialTransactionCollection()
                            .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxOverpaymentApplied)
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
            SpcfDecimal amountApplied = SpcfMoney.ZERO;
            for (FinancialTransaction appliedDebitTransaction : appliedOverpaymentTransactions) {
                amountApplied = amountApplied.add(appliedDebitTransaction.getFinancialTransactionAmount());
            }

            pPayrollRun.applyPendingCredits(erTaxDebitAmount.subtract(amountApplied));

            DomainEntitySet<FinancialTransaction> appliedCreditTransactions =
                    pPayrollRun.getFinancialTransactionCollection()
                            .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxCreditApplied)
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
            for (FinancialTransaction appliedDebitTransaction : appliedCreditTransactions) {
                amountApplied = amountApplied.add(appliedDebitTransaction.getFinancialTransactionAmount());
            }

            erTaxDebitAmount = erTaxDebitAmount.subtract(amountApplied);
            if (erTaxDebitAmount.isLessThan(SpcfMoney.ZERO)) {
                throw new RuntimeException("Employer Tax Debit cannot be negative");
            }

            SpcfCalendar settlementDate;
            if (pSettlementDate != null) {
                settlementDate = pSettlementDate.toSpcfCalendar();
            } else if (pOldTransaction != null) {
                settlementDate = pOldTransaction.getSettlementDate().toLocal().copy();
            } else {
                settlementDate = null;
            }

            // create er debit
            FinancialTransaction erTaxDebitFT = FinancialTransaction.createERDebitTransaction(pPayrollRun, pCompanyBankAccount,
                    TransactionTypeCode.EmployerTaxDebit,
                    new SpcfMoney(erTaxDebitAmount),
                    SettlementType.ACH,
                    settlementDate,
                    pPayrollRun.getCompany().getService(ServiceCode.Tax));

            // If any "Applied" transactions were created, they need to be associated with the same MMT as the debit, so they will be updated to executed together during offload
            for (FinancialTransaction appliedDebitTransaction : appliedOverpaymentTransactions) {
                if(appliedDebitTransaction.getMoneyMovementTransaction() != null) {
                    MoneyMovementTransaction.subtractFinancialTransaction(appliedDebitTransaction);
                }
                appliedDebitTransaction.setMoneyMovementTransaction(erTaxDebitFT.getMoneyMovementTransaction());
                appliedDebitTransaction.setSettlementDate(erTaxDebitFT.getMoneyMovementTransaction().getSettlementDate());
                erTaxDebitFT.getMoneyMovementTransaction().getFinancialTransactionCollection().add(appliedDebitTransaction);
                Application.save(appliedDebitTransaction);
            }
            for (FinancialTransaction appliedDebitTransaction : appliedCreditTransactions) {
                appliedDebitTransaction.setMoneyMovementTransaction(erTaxDebitFT.getMoneyMovementTransaction());
                erTaxDebitFT.getMoneyMovementTransaction().getFinancialTransactionCollection().add(appliedDebitTransaction);
                appliedDebitTransaction.setSettlementDate(erTaxDebitFT.getMoneyMovementTransaction().getSettlementDate());
                Application.save(appliedDebitTransaction);
            }
        }
    }

    private static SpcfDecimal calculateMaxOverpaymentAvailable(PayrollRun pPayrollRun, Map<PaymentTemplate, MoneyMovementTransaction> pPayrollRunPayments, Map<PaymentTemplate, SpcfDecimal> pDebitAmountPerTemplate, PaymentTemplate paymentTemplate) {
        // cannot apply overpayments if there are no positive payments to apply it to
        DomainEntitySet<MoneyMovementTransaction> pendingPayments =
                MoneyMovementTransaction.findTaxPayments()
                        .setCompany(pPayrollRun.getCompany())
                        .setPaymentTemplate(paymentTemplate)
                        .setPaycheckDate(pPayrollRun.getPaycheckDate())
                        .setPending()
                        .setNonDirect()
                        .find();


        MoneyMovementTransaction currentPayrollPayment = pPayrollRunPayments.get(paymentTemplate);
        if (currentPayrollPayment != null && !pendingPayments.contains(currentPayrollPayment) && currentPayrollPayment.isPendingTaxPayment()) {
            pendingPayments.add(currentPayrollPayment);
        }

        SpcfDecimal maxOverPaymentAmountAvailable = SpcfMoney.ZERO;
        for (MoneyMovementTransaction moneyMovementTransaction : pendingPayments) {
            maxOverPaymentAmountAvailable = maxOverPaymentAmountAvailable.add(moneyMovementTransaction.getMoneyMovementTransactionAmount());
        }

        SpcfDecimal maxDebitAmount = pDebitAmountPerTemplate.get(paymentTemplate);
        if(maxDebitAmount == null) {
            maxDebitAmount = SpcfMoney.ZERO;
        }

        if (maxOverPaymentAmountAvailable.isGreaterThan(maxDebitAmount)) {
            // there is no copy or clone for spcf decimal
            maxOverPaymentAmountAvailable = SpcfDecimal.createInstance(maxDebitAmount.toString());
        }
        return maxOverPaymentAmountAvailable;
    }

    private static SpcfDecimal calculateMaxDirectOverpaymentAvailable(PayrollRun pPayrollRun, Map<PaymentTemplate, MoneyMovementTransaction> pPayrollRunPayments, Map<PaymentTemplate, SpcfDecimal> pDebitAmountPerTemplate, PaymentTemplate paymentTemplate) {
        // cannot apply overpayments if there are no positive payments to apply it to
        DomainEntitySet<MoneyMovementTransaction> pendingPayments =
                MoneyMovementTransaction.findTaxPayments()
                                        .setCompany(pPayrollRun.getCompany())
                                        .setPaymentTemplate(paymentTemplate)
                                        .setPaycheckDate(pPayrollRun.getPaycheckDate())
                                        .setPaymentMethods(new PaymentMethod[]{PaymentMethod.EFTPSDirectDebit})
                                        .setPending()
                                        .find();


        MoneyMovementTransaction currentPayrollPayment = pPayrollRunPayments.get(paymentTemplate);
        if (currentPayrollPayment != null && !pendingPayments.contains(currentPayrollPayment) && currentPayrollPayment.isPendingTaxPayment()) {
            pendingPayments.add(currentPayrollPayment);
        }

        SpcfDecimal maxOverPaymentAmountAvailable = SpcfMoney.ZERO;
        for (MoneyMovementTransaction moneyMovementTransaction : pendingPayments) {
            maxOverPaymentAmountAvailable = maxOverPaymentAmountAvailable.add(moneyMovementTransaction.getMoneyMovementTransactionAmount());
        }

//        SpcfDecimal maxDebitAmount = pDebitAmountPerTemplate.get(paymentTemplate);
//        if(maxDebitAmount == null) {
//            maxDebitAmount = SpcfMoney.ZERO;
//        }
//
//        if (maxOverPaymentAmountAvailable.isGreaterThan(maxDebitAmount)) {
//            // there is no copy or clone for spcf decimal
//            maxOverPaymentAmountAvailable = SpcfDecimal.createInstance(maxDebitAmount.toString());
//        }
        return maxOverPaymentAmountAvailable;
    }


    private static Map<PaymentTemplate, SpcfDecimal> getDebitAmountPerTemplate(PayrollRun pPayrollRun) {
        Map<PaymentTemplate, SpcfDecimal> debitAmountPerTemplate = new HashMap<PaymentTemplate, SpcfDecimal>();

        DomainEntitySet<FinancialTransaction> agencyTaxCredits =
                pPayrollRun.getFinancialTransactionCollection()
                        .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit)
                                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Voided)));
        for (FinancialTransaction agencyTaxCredit : agencyTaxCredits) {
            PaymentTemplate paymentTemplate = agencyTaxCredit.getLaw().getPaymentTemplate();
            if (debitAmountPerTemplate.get(paymentTemplate) == null) {
                debitAmountPerTemplate.put(paymentTemplate, SpcfMoney.ZERO);
            }
            debitAmountPerTemplate.put(paymentTemplate, debitAmountPerTemplate.get(paymentTemplate).add(agencyTaxCredit.getFinancialTransactionAmount()));
        }

        // reduce the debit by any laws not included in the update
        DomainEntitySet<FinancialTransaction> appliedTransactions =
                pPayrollRun.getFinancialTransactionCollection()
                        .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxOverpaymentApplied)
                                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        for (FinancialTransaction appliedTransaction : appliedTransactions) {
            PaymentTemplate paymentTemplate = appliedTransaction.getLaw().getPaymentTemplate();
            if (debitAmountPerTemplate.get(paymentTemplate) == null) {
                debitAmountPerTemplate.put(paymentTemplate, SpcfMoney.ZERO);
            }
            debitAmountPerTemplate.put(paymentTemplate, debitAmountPerTemplate.get(paymentTemplate).subtract(appliedTransaction.getFinancialTransactionAmount()));
        }
        return debitAmountPerTemplate;
    }

    private static void createEmployerTaxCredit(PayrollRun pPayrollRun, CompanyBankAccount pCompanyBankAccount, BankAccount pDebitBankAccount, SpcfMoney pAmount) {
        SpcfCalendar refundSettlementDate = getRefundSettlementDate(pPayrollRun.getCompany());
        CalendarUtils.clearTime(refundSettlementDate);

        FinancialTransaction.createFinancialTransaction(pPayrollRun.getCompany(),
                pPayrollRun,
                null,
                pCompanyBankAccount.getBankAccount(),
                pDebitBankAccount,
                BankAccountOwnerType.Company,
                BankAccountOwnerType.Intuit,
                TransactionTypeCode.EmployerTaxCredit,
                pAmount,
                SettlementType.ACH,
                refundSettlementDate);

    }

    public static ProcessResult processPendingPaymentsThreshold(Company pCompany, PaymentTemplate pPaymentTemplate) {
        ProcessResult processResult = new ProcessResult();

        for (PayrollRun payrollRun : PayrollRun.getPayrollsInMemory(pCompany)) {
            processResult.merge(processThreshold(pCompany, pPaymentTemplate, payrollRun));
        }

        combinePayments(pCompany);

        return processResult;
    }

    private static ProcessResult processThreshold(Company pCompany, PaymentTemplate pPaymentTemplate, PayrollRun pPayrollRun) {

        ProcessResult processResult = new ProcessResult();
        EffectiveDepositFrequency effectiveDepositFrequency = pPaymentTemplate.getEffectiveDepositFreq(pCompany, pPayrollRun.getPaycheckDate());
        DepositFrequencyCode effectiveDepositFrequencyCode = effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId();
        PaymentTemplate paymentTemplateUpperLimit = pPaymentTemplate;
        if (pPaymentTemplate.isFollowsFederal() || pPaymentTemplate.followsPaymentTemplateThreshold()) {
            paymentTemplateUpperLimit = pPaymentTemplate.getFollowedPaymentTemplate();
        }
        UpperLimit upperLimit = PaymentTemplate.getThresholdInfo(paymentTemplateUpperLimit.getPaymentTemplateCd(), effectiveDepositFrequencyCode.toString());

        if (upperLimit != null) {

            SpcfMoney thresholdAmount = new SpcfMoney(upperLimit.amount);
            IPaymentPeriod upperLimitPaymentPeriod = MoneyMovementTransaction.getPaymentPeriod(paymentTemplateUpperLimit.getPaymentTemplateCd(), effectiveDepositFrequencyCode.toString(), CalendarUtils.convertToRulesCalendar(pPayrollRun.getPaycheckDate()));            
            SpcfCalendar newAccrualFromDate = effectiveDepositFrequency.getEffectiveDate().after(CalendarUtils.convertToSpcfCalendar(upperLimitPaymentPeriod.getFromAccrualDate())) ? effectiveDepositFrequency.getEffectiveDate() : null;
            DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByTaxPaymentPeriod(pCompany, upperLimitPaymentPeriod, newAccrualFromDate);
            if (!payrollRuns.contains(pPayrollRun)) {
                payrollRuns.add(pPayrollRun);
            }

            DomainEntitySet<PayrollRun> subsequentPayrollRuns = new DomainEntitySet<PayrollRun>();
            SpcfDecimal totalLiability = SpcfMoney.ZERO;
            PayrollRun currentExceedingPayrollRun = null;
            boolean foundExceedingPayrollRun = false;

            for (PayrollRun payrollRun : payrollRuns) {
                if (foundExceedingPayrollRun && currentExceedingPayrollRun != null) {
                    if(payrollRun.getPaycheckDate().after(currentExceedingPayrollRun.getPaycheckDate())) {
                        // reset trackers
                        currentExceedingPayrollRun = null;
                        totalLiability = SpcfMoney.ZERO;
                    } else {
                        continue;
                    }
                }

                MoneyMovementTransaction prMMT = pPaymentTemplate.getTaxPayment(payrollRun);
                if (prMMT == null) {
                    prMMT = pPaymentTemplate.getDirectDebitTaxPayment(payrollRun);
                }

                totalLiability = totalLiability.add(paymentTemplateUpperLimit.getNetLiabilityAmount(payrollRun));

                if (totalLiability.isGreaterThanEqualTo(thresholdAmount) &&
                        prMMT != null &&
                        prMMT.isPendingMMT()) {
                    // reset accumulator
                    subsequentPayrollRuns = new DomainEntitySet<PayrollRun>();

                    currentExceedingPayrollRun = payrollRun;
                    foundExceedingPayrollRun = true;

                    // Need to find all  payrollruns for the same paycheck date or earlier - they should be all processed together
                    // We can only do this processing for payrolls that have not had the debit executed yet
                    DomainEntitySet<PayrollRun> exceedingPayrollRuns = payrollRuns.find(PayrollRun.PaycheckDate().lessOrEqualThan(payrollRun.getPaycheckDate())).sort(PayrollRun.PaycheckDate());

                    // Get the MMT correspondent to the exceeding payroll run
                    // Update the due date and initiation date according to the frequency used for the upper limit
                    for (PayrollRun exceedingPR : exceedingPayrollRuns) {
                        FinancialTransaction erDebit = exceedingPR.getNonCancelledEmployerTaxDebit();
                        if (erDebit != null && erDebit.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Created)) {
                            MoneyMovementTransaction mmt = pPaymentTemplate.getTaxPayment(exceedingPR);
                            if (mmt == null) {
                                mmt = pPaymentTemplate.getDirectDebitTaxPayment(payrollRun);
                            }
                            if (mmt != null) {
                                PaymentMethod paymentMethod = mmt.getMoneyMovementPaymentMethod();
                                // Other than EFTPS, EFTPSDirectDebit (All state payment methods)
                                if (paymentMethod != PaymentMethod.EFTPS &&
                                        paymentMethod != PaymentMethod.EFTPSDirectDebit) {

                                    // do not update NY Metro check payments
                                    if (mmt.getPaymentTemplate().getPaymentTemplateCd().equals(PaymentTemplate.NY_METRO)
                                            && paymentMethod == PaymentMethod.CheckPayment) {
                                        continue;
                                    }

                                    //Call agency rules calculator and Set new Due Date
                                    upperLimitPaymentPeriod = MoneyMovementTransaction.getPaymentPeriod(paymentTemplateUpperLimit.getPaymentTemplateCd(), upperLimit.rollOverFrequency, CalendarUtils.convertToRulesCalendar(exceedingPR.getPaycheckDate()));
                                    DomainEntitySet<PaymentTemplateFrequency> frequencies = pPaymentTemplate.getPaymentTemplateFrequencyCollection().find(PaymentTemplateFrequency.PaymentFrequencyId().equalTo(DepositFrequencyCode.valueOf(upperLimit.rollOverFrequency)));

                                    SpcfCalendar initDateCopy = mmt.getInitiationDate().copy().toLocal();
                                    SpcfCalendar nextInitDate = MoneyMovementTransaction.getNextInitiationDate(mmt.getMoneyMovementPaymentMethod());

                                    SpcfCalendar today = PSPDate.getPSPTime().copy();
                                    CalendarUtils.clearTime(today);

                                    if (!initDateCopy.equals(today) || nextInitDate.equals(initDateCopy)) {
                                        if (frequencies.size() > 0) {
                                            mmt.updatePaymentFrequency(frequencies.get(0));
                                            mmt.updateDueDate(CalendarUtils.convertToSpcfCalendar(upperLimitPaymentPeriod.getDueDate()));
                                        } else if (frequencies.size() == 0) {
                                            throw new RuntimeException("Frequency not supported for this Payment Template");
                                        }
                                    }
                                }

                                // If this is EFTPS we need to create a Direct Debit MMT for the exceeding payrollrun
                                if (mmt != null && mmt.isPendingMMT() &&
                                        paymentMethod == PaymentMethod.EFTPS) {
                                    createEFTPSDirectDebitMMTs(exceedingPR, pPaymentTemplate);
                                }

                                currentExceedingPayrollRun = exceedingPR;
                            }
                        }
                    }

                    if (currentExceedingPayrollRun != null) {

                        MoneyMovementTransaction directDebitMMT = pPaymentTemplate.getDirectDebitTaxPayment(currentExceedingPayrollRun);

                        // UPDATE MMTs FOR PAYMENT TEMPLATES THAT FOLLOW FED
                        // If the Payment Template is 941 - Check for all payments that follow this  and
                        // Adjust the due dates

                        List<PaymentTemplate> followsIRS941PaymentTemplate = new ArrayList<PaymentTemplate>();
                        IPaymentPeriod paymentPeriod;
                        DepositFrequencyCode edfCode;
                        EffectiveDepositFrequency edf;
                        if (pPaymentTemplate.isIRS941()) {
                            followsIRS941PaymentTemplate = processFollowsIRS941(currentExceedingPayrollRun);
                            edf = paymentTemplateUpperLimit.getEffectiveDepositFreq(pCompany, currentExceedingPayrollRun.getPaycheckDate());
                            edfCode = edf.getPaymentTemplateFrequency().getPaymentFrequencyId();
                            paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(pPaymentTemplate.getPaymentTemplateCd(), upperLimit.rollOverFrequency, CalendarUtils.convertToRulesCalendar(currentExceedingPayrollRun.getPaycheckDate()));
                        } else {
                            edf = paymentTemplateUpperLimit.getEffectiveDepositFreq(pCompany, currentExceedingPayrollRun.getPaycheckDate());
                            edfCode = edf.getPaymentTemplateFrequency().getPaymentFrequencyId();
                            paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(paymentTemplateUpperLimit.getPaymentTemplateCd(), edfCode.toString(), CalendarUtils.convertToRulesCalendar(currentExceedingPayrollRun.getPaycheckDate()));
                        }

                        DomainEntitySet<PaymentTemplateFrequency> frequencies = pPaymentTemplate.getPaymentTemplateFrequencyCollection().find(PaymentTemplateFrequency.PaymentFrequencyId().equalTo(DepositFrequencyCode.valueOf(upperLimit.permanentPaymentFrequency)));

                        // Create Threshold Exceeded Event
                        SpcfCalendar thresholdStartDate;
                        if (pPaymentTemplate.isIRS941()) {
                            thresholdStartDate = CalendarUtils.convertToSpcfCalendar(upperLimitPaymentPeriod.getFromAccrualDate());
                        } else {
                            thresholdStartDate = CalendarUtils.convertToSpcfCalendar(paymentPeriod.getFromAccrualDate());
                        }
                        CompanyEvent companyEvent = CompanyEvent.createThresholdExceededEvent(currentExceedingPayrollRun, pPaymentTemplate, thresholdStartDate);

                        if (companyEvent != null) {
                            //Create new effective deposit frequency
                            pPaymentTemplate.createNewEffectiveFrequencyForExceedingThreshold(currentExceedingPayrollRun, frequencies.get(0), companyEvent);
                        }

                        for (PaymentTemplate paymentTemplate : followsIRS941PaymentTemplate) {
                            companyEvent = CompanyEvent.createThresholdExceededEvent(currentExceedingPayrollRun, paymentTemplate, thresholdStartDate);

                            frequencies = paymentTemplate.getPaymentTemplateFrequencyCollection().find(PaymentTemplateFrequency.PaymentFrequencyId().equalTo(DepositFrequencyCode.valueOf(upperLimit.permanentPaymentFrequency)));
                            if (companyEvent != null) {
                                //Create new effective deposit frequency
                                pPaymentTemplate.createNewEffectiveFrequencyForExceedingThreshold(currentExceedingPayrollRun, frequencies.get(0), companyEvent);
                            }
                        }

                        // All pending tax payments for the same payment period, same paycheck date or earlier have to be paid on the same new due date
                        DomainEntitySet<PayrollRun> payrollRunsToUpdate = payrollRuns.find(PayrollRun.PaycheckDate().lessOrEqualThan(currentExceedingPayrollRun.getPaycheckDate()));
                        for (PayrollRun payrollRunToUpdate : payrollRunsToUpdate) {
                            MoneyMovementTransaction mmt = pPaymentTemplate.getTaxPayment(payrollRunToUpdate);
                            if (mmt != null) {
                                updateMMT(mmt, directDebitMMT, upperLimit, payrollRunToUpdate.getPaycheckDate());
                            }
                        }
                    }
                } else if(foundExceedingPayrollRun && currentExceedingPayrollRun == null) {
                    subsequentPayrollRuns.add(payrollRun);
                }
            }

            if(!foundExceedingPayrollRun) {
                if (CompanyEvent.findUnreversedThresholdExceededEventForPayroll(pCompany, pPaymentTemplate, pPayrollRun) != null) {
                    // Check if the threshold has ever been exceeded for the same payment period - if not we don't need to do anything
                    if (pPaymentTemplate.isIRS941()) {
                        // If processing for IRS-941, we have to check for all the payment templates that follows federal
                        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(pCompany, EventTypeCode.ThresholdExceeded);
                        for (CompanyEvent companyEvent : companyEvents) {
                            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
                            if (paymentTemplate.isFollowsFederal() || paymentTemplate.isIRS941()) {
                                processResult.merge(revertTransactionsForThresholdExceeded(pCompany, paymentTemplate, pPayrollRun, payrollRuns));
                            }
                        }
                    } else {
                        processResult.merge(revertTransactionsForThresholdExceeded(pCompany, pPaymentTemplate, pPayrollRun, payrollRuns));
                    }
                }
            }

            // If there are more payrolls in the collection all the subsequent payroll agency tax credit transactions will move to a new MMT
            DomainEntitySet<FinancialTransaction> creditTransactions = new DomainEntitySet<FinancialTransaction>();
            DomainEntitySet<FinancialTransaction> debitTransactions = new DomainEntitySet<FinancialTransaction>();
            for (PayrollRun subsequentPayrollRun : subsequentPayrollRuns) {
                for (FinancialTransaction financialTransaction : pPaymentTemplate.getAgencyTaxDebits(subsequentPayrollRun, TransactionStateCode.Created)) {
                    if (financialTransaction.getMoneyMovementTransaction() != null) {
                        debitTransactions.add(financialTransaction);
                    }
                }

                for (FinancialTransaction financialTransaction : pPaymentTemplate.getAgencyTaxCredits(subsequentPayrollRun, TransactionStateCode.Created)) {
                    if (financialTransaction.getMoneyMovementTransaction() != null) {
                        creditTransactions.add(financialTransaction);
                        // PSP-3890 - In case of any previous payrolls that crossed the threshold value with agency over payment due to voiding of any paychecks,
                        // there will be ATDs on this MMT associated with threshold payroll. Fetching the ATDs from this MMT and adding to ATD list to avoid negative MMT.
                        for(FinancialTransaction agencyDebit: financialTransaction.getMoneyMovementTransaction().getFinancialTransactionCollection()
                                                                                   .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxDebit))){
                            debitTransactions.add(agencyDebit);
                        }
                    }
                }
            }

            MoneyMovementTransaction.removeAndAddAgencyTransactions(debitTransactions, creditTransactions);
        }

        return processResult;
    }

    private static ProcessResult revertTransactionsForThresholdExceeded(Company pCompany, PaymentTemplate pPaymentTemplate, PayrollRun pPayrollRun, DomainEntitySet<PayrollRun> payrollRuns) {
        ProcessResult processResult = new ProcessResult();
        CompanyEvent companyEvent = CompanyEvent.findUnreversedThresholdExceededEventForPayroll(pCompany, pPaymentTemplate, pPayrollRun);

        if (companyEvent == null) {
            return processResult;
        }

        companyEvent.addCompanyEventDetail(EventDetailTypeCode.ThresholdReversed, PSPDate.getPSPTime().toString()); // Marking this event as reversed

        //Validate invalidated Effective deposit frequencies, as we are reverting for threshold changes.
        Collection<String> invalidatedFrequencyIds = companyEvent.getCompanyEventDetailValues(EventDetailTypeCode.InvalidatedDepositFrequencyId);
        PayrollRun thresholdPayrollRun = Application.findById(PayrollRun.class, SpcfUniqueId.createInstance(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));
        if (invalidatedFrequencyIds.size() > 0 && !thresholdPayrollRun.getPaycheckDate().toLocal().before(pPayrollRun.getPaycheckDate())) {
            for (String invalidatedFrequencyId : invalidatedFrequencyIds) {
                EffectiveDepositFrequency effectiveDepositFrequency = Application.findById(EffectiveDepositFrequency.class, SpcfUniqueId.createInstance(invalidatedFrequencyId));
                EffectiveDepositFrequencyDTO effectiveDepositFrequencyDTO = PayrollServices.dtoFactory.create(effectiveDepositFrequency);
                processResult.merge(PayrollServices.paymentManager.updateDepositFrequency(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), effectiveDepositFrequencyDTO));
            }
        }
        //Invalidate effective deposit frequency added when threshold was crossed (PermanentPaymentFrequencyId)
        String permanentPaymentFrequencyId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PermanentPaymentFrequencyId);
        if (permanentPaymentFrequencyId != null) {
            EffectiveDepositFrequency effectiveDepositFrequency = Application.findById(EffectiveDepositFrequency.class, SpcfUniqueId.createInstance(permanentPaymentFrequencyId));
            if (effectiveDepositFrequency != null && effectiveDepositFrequency.getInvalidDate() == null) {
                effectiveDepositFrequency.setInvalidDate(pPayrollRun.getPaycheckDate());
                Application.save(effectiveDepositFrequency);
            }
        }

        // If this payment template has Direct debit Transactions
        // Cancel any Direct Debit Transactions for pending payrolls, the threshold has not been exceeded.
        // Create Agency Tax Credits to replace AgencyTaxDirectCredits
        for (PayrollRun payrollRun : payrollRuns) {
            MoneyMovementTransaction mmt = pPaymentTemplate.getTaxPayment(payrollRun);
            if (mmt == null) {
                mmt = pPaymentTemplate.getDirectDebitTaxPayment(payrollRun);
            }
            if (mmt != null && mmt.isOffloadPending() && mmt.getStatus() == PaymentStatus.Created && (mmt.getTaxPaymentStatus() == TaxPaymentStatus.ReadyToSend || mmt.getTaxPaymentStatus() == TaxPaymentStatus.Ignore)) {
                if (payrollRun.getTaxPaymentTransactions().size() > 0) { //&& mmt.getMoneyMovementPaymentMethod() != PaymentMethod.EFTPSDirectDebit) {
                    // Adjust deposit frequency / due date
                    pPaymentTemplate.adjustDueDateAndFrequency(payrollRun, mmt);

                    MoneyMovementTransaction.removeAndAddAgencyTransactions(mmt.getTransactions(TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created),
                            mmt.getTransactions(TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created));
                }
            }

            boolean taxImpoundHasOffloaded = pPayrollRun.hasTaxImpoundOffloaded();

            if (!taxImpoundHasOffloaded) {
                if (pPaymentTemplate.getAgencyTaxDirectCredits(payrollRun, TransactionStateCode.Created).size() > 0) {
                    // Cancel ERDirectDebit
                    FinancialTransaction employerDirectDebit = payrollRun.getCreatedEmployerTaxDirectDebitTransaction();
                    if (employerDirectDebit != null) {
                        employerDirectDebit.cancelFinancialTransaction();

                        SpcfMoney newDebitAmount = employerDirectDebit.getFinancialTransactionAmount();
                        // If there is another tax debit for this payroll cancel it and consolidate in one single transaction
                        FinancialTransaction employerDebit = payrollRun.getEmployerTaxDebit(TransactionStateCode.Created);
                        if (employerDebit != null) {
                            employerDebit.cancelFinancialTransaction();
                            newDebitAmount = new SpcfMoney(newDebitAmount.add(employerDebit.getFinancialTransactionAmount()));
                        }

                        // Create EmployerTaxDebit to replace Direct Debit
                        CompanyBankAccount companyBankAccount =
                                CompanyBankAccount.findActiveCompanyBankAccount(pCompany);
                        CompanyService service = CompanyService.findCompanyService(pCompany, ServiceCode.Tax);
                        FinancialTransaction.createERDebitTransaction(
                                payrollRun,
                                companyBankAccount,
                                TransactionTypeCode.EmployerTaxDebit,
                                newDebitAmount,
                                employerDirectDebit.getQbdtTransactionInfo() == null ? SettlementType.ACH : SettlementType.Cash,
                                null,
                                service);
                    }
                    // Build a list of the financial transactions to be recreated from the ones we cancel.
                    // This cannot be done in the same loop because the MMT has to be deleted first
                    ArrayList<FinancialTransaction> ftsToRecreate = new ArrayList<FinancialTransaction>();
                    for (FinancialTransaction ft : pPaymentTemplate.getAgencyTaxDirectCredits(payrollRun, TransactionStateCode.Created)) {
                        ft.cancelFinancialTransaction();
                        ftsToRecreate.add(ft);
                    }
                    // Now we can recreate the financial transactions
                    for (FinancialTransaction ft : ftsToRecreate) {
                        createTaxCredit(payrollRun, ft.getLaw(), ft.getFinancialTransactionAmount());
                    }
                }
            }
        }

        return processResult;
    }

    @SuppressWarnings("unchecked")
    private static void createEFTPSDirectDebitMMTs(PayrollRun pPayrollRun, PaymentTemplate pPaymentTemplate) {
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(pPayrollRun.getCompany());

        DomainEntitySet<FinancialTransaction> employerTaxDebits =
                pPayrollRun.getFinancialTransactionCollection()
                        .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit)
                                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        FinancialTransaction employerTaxDebitTransaction = null;
        if (employerTaxDebits.size() > 0) {
            employerTaxDebitTransaction = employerTaxDebits.get(0);
            companyBankAccount = employerTaxDebitTransaction.getCompanyBankAccount();
        }

        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);

        if (employerTaxDebitTransaction != null &&
                employerTaxDebitTransaction.getMoneyMovementTransaction().getInitiationDate().toLocal().equals(today) &&
                !employerTaxDebitTransaction.getCompany().getOffloadGroup().isBeforeActualCutoffTime()) {
            return;
        }

        DomainEntitySet<FinancialTransaction> employerTaxOverpayments =
                pPayrollRun.getFinancialTransactionCollection()
                        .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxOverpaymentApplied,
                                TransactionTypeCode.EmployerTaxDirectOverpaymentApplied)
                                .And(FinancialTransaction.Law().PaymentTemplate().equalTo(pPaymentTemplate))
                                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        for (FinancialTransaction employerTaxCredit : employerTaxOverpayments) {
            employerTaxCredit.cancelFinancialTransaction();

            // also cancel the related agency tax debit for Overpayment Applied transactions
            if (employerTaxCredit.getRelatableTransaction() != null) {
                employerTaxCredit.getRelatableTransaction().cancelFinancialTransaction();
            }
        }

        DomainEntitySet<FinancialTransaction> employerTaxCredits =
                pPayrollRun.getFinancialTransactionCollection()
                        .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxCreditApplied)
                                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        for (FinancialTransaction employerTaxCredit : employerTaxCredits) {
            employerTaxCredit.cancelFinancialTransaction();
        }

        DomainEntitySet<FinancialTransaction> agencyTransactions =
                pPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit,
                        TransactionTypeCode.AgencyDirectCredit)
                        .And(FinancialTransaction.Law().PaymentTemplate().equalTo(pPaymentTemplate))
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        for (FinancialTransaction agencyFinancialTransaction : agencyTransactions) {
            agencyFinancialTransaction.cancelFinancialTransaction();

            FinancialTransaction.createFinancialTransaction(pPayrollRun.getCompany(), pPayrollRun, null, null, companyBankAccount.getBankAccount(),
                    BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Company, TransactionTypeCode.AgencyDirectCredit,
                    new SpcfMoney(agencyFinancialTransaction.getFinancialTransactionAmount()), SettlementType.EFTPSDirectDebit, null, agencyFinancialTransaction.getLaw());
        }

        DomainEntitySet<FinancialTransaction> agencyTaxDebitFTs = pPayrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxDebit)
                        .And(FinancialTransaction.Law().PaymentTemplate().equalTo(pPaymentTemplate))
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));

        BankAccount debitBankAccount = null;
        for (FinancialTransaction agencyTaxDebitFT : agencyTaxDebitFTs) {
            SpcfMoney transactionAmount = agencyTaxDebitFT.getFinancialTransactionAmount();
            agencyTaxDebitFT.cancelFinancialTransaction();
            debitBankAccount = agencyTaxDebitFT.getDebitBankAccount();
            FinancialTransaction.createFinancialTransaction(pPayrollRun.getCompany(), pPayrollRun, null, null, debitBankAccount,
                    BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit,
                    TransactionTypeCode.AgencyDirectOverpayment,
                    transactionAmount,
                    SettlementType.ApplyForward, null, agencyTaxDebitFT.getLaw());
        }

        MoneyMovementTransaction directDebitMMTPayment = pPaymentTemplate.getDirectDebitTaxPayment(pPayrollRun);
        addEnrollmentHold(directDebitMMTPayment);

        createEmployerDirectDebit(pPayrollRun, pPaymentTemplate, companyBankAccount, debitBankAccount, directDebitMMTPayment);

        if (employerTaxDebitTransaction != null) {
            employerTaxDebitTransaction.cancelFinancialTransaction();

            createEmployerTaxDebit(pPayrollRun, companyBankAccount, employerTaxDebitTransaction.getDebitBankAccount(), employerTaxDebitTransaction, new HashMap<PaymentTemplate, MoneyMovementTransaction>(), null);
        }
    }

    private static void createEmployerDirectDebit(PayrollRun pPayrollRun, PaymentTemplate pPaymentTemplate, CompanyBankAccount pCompanyBankAccount, BankAccount pDebitBankAccount, MoneyMovementTransaction pDirectDebitMMTPayment) {
        CompanyService service = CompanyService.findCompanyService(pPayrollRun.getCompany(), ServiceCode.Tax);

        // apply overpayments
        SpcfMoney maxAmount = new SpcfMoney(pDirectDebitMMTPayment.getMMTBalance());
        pPayrollRun.applyPendingOverPayments(pPaymentTemplate, maxAmount, pPayrollRun.getPaycheckDate(), true, pDebitBankAccount);

        DomainEntitySet<FinancialTransaction> appliedCreditTransactions = pPayrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDirectOverpaymentApplied)
                        .And(FinancialTransaction.Law().PaymentTemplate().equalTo(pPaymentTemplate))
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));

        // Associate the applied transactions to the new MMT
        if (appliedCreditTransactions != null) {
            for (FinancialTransaction financialTransaction : appliedCreditTransactions) {
                if (financialTransaction.getSettlementDate() == null) {
                    SpcfCalendar settlementDate = pDirectDebitMMTPayment.generatePaymentSettlementDate();
                    financialTransaction.setSettlementDate(settlementDate);
                    financialTransaction.setOriginalSettlementDate(settlementDate);
                }
                financialTransaction.setMoneyMovementTransaction(pDirectDebitMMTPayment);
                pDirectDebitMMTPayment.addFinancialTransaction(financialTransaction);

                if (financialTransaction.getRelatableTransaction() != null) {
                    financialTransaction.getRelatableTransaction().setMoneyMovementTransaction(pDirectDebitMMTPayment);
                    pDirectDebitMMTPayment.addFinancialTransaction(financialTransaction.getRelatableTransaction());
                }
            }
        }

        FinancialTransaction directDebitTransaction =
                FinancialTransaction.createERDebitTransaction(pPayrollRun, pCompanyBankAccount,
                        TransactionTypeCode.EmployerTaxDirectDebit, pDirectDebitMMTPayment.getMMTBalance(),
                        SettlementType.EFTPSDirectDebit, pDirectDebitMMTPayment.generatePaymentSettlementDate(), service);

        directDebitTransaction.setMoneyMovementTransaction(pDirectDebitMMTPayment);
        pDirectDebitMMTPayment.addFinancialTransaction(directDebitTransaction);
    }

    private static void updateMMT(MoneyMovementTransaction pMMT, MoneyMovementTransaction pDirectDebitMMT, UpperLimit pUpperLimit, SpcfCalendar pDate) {
        if (pMMT != null) {
            PaymentMethod paymentMethod = pMMT.getMoneyMovementPaymentMethod();
            PaymentTemplate paymentTemplate = pMMT.getPaymentTemplate();
            //The NY-1MN-PAYMENT method has ACHDebit which is considered for 3BD if ETD is executed.
            if ((paymentMethod == PaymentMethod.EFTPS || paymentMethod == PaymentMethod.ACHCredit || (paymentTemplate.getPaymentTemplateCd().equals(PaymentTemplate.NY_WH) && paymentMethod == PaymentMethod.ACHDebit))
                    && (pMMT.getTaxPaymentStatus().equals(TaxPaymentStatus.ReadyToSend) || pMMT.getTaxPaymentStatus().equals(TaxPaymentStatus.OnHold))) {

                DomainEntitySet<PaymentTemplateFrequency> frequencies = paymentTemplate.getPaymentTemplateFrequencyCollection().find(PaymentTemplateFrequency.PaymentFrequencyId().equalTo(DepositFrequencyCode.valueOf(pUpperLimit.rollOverFrequency)));

                if (frequencies.size() > 0) {
                    pMMT.updatePaymentFrequency(frequencies.get(0));
                    IPaymentPeriod upperLimitPaymentPeriod = MoneyMovementTransaction.getPaymentPeriod(paymentTemplate.getPaymentTemplateCd(), pUpperLimit.rollOverFrequency, CalendarUtils.convertToRulesCalendar(pDate));

                    SpcfCalendar dueDate = null;
                    if (pDirectDebitMMT != null) {
                        dueDate = pDirectDebitMMT.getDueDate().toLocal();
                    } else if (upperLimitPaymentPeriod != null) {
                        dueDate = CalendarUtils.convertToSpcfCalendar(upperLimitPaymentPeriod.getDueDate());
                    }

                    pMMT.updateDueDate(dueDate);
                    if (paymentMethod == PaymentMethod.ACHCredit) {
                        MoneyMovementTransaction.recreateEntryDetailRecords(pMMT);
                    }

                }
            }
        }
    }

    private static List<PaymentTemplate> processFollowsIRS941(PayrollRun pPayrollRun) {
        PaymentTemplate pTemplate941 = PaymentTemplate.findPaymentTemplate(PaymentTemplate.IRS_941);

        EffectiveDepositFrequency edf = pTemplate941.getEffectiveDepositFreq(pPayrollRun.getCompany(), pPayrollRun.getPaycheckDate());
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(pTemplate941.getPaymentTemplateCd(), edf.getPaymentTemplateFrequency().getPaymentFrequencyId().toString(), CalendarUtils.convertToRulesCalendar(pPayrollRun.getPaycheckDate()));

        SpcfCalendar newDueDate = null;
        MoneyMovementTransaction directDebitMMT = pTemplate941.getDirectDebitTaxPayment(pPayrollRun);
        if (directDebitMMT != null) {
            newDueDate = directDebitMMT.getDueDate().toLocal();
        } else {
            MoneyMovementTransaction mmt = pTemplate941.getTaxPayment(pPayrollRun);
            if (mmt != null) {
                newDueDate = mmt.getDueDate().toLocal();
            }
        }
        DomainEntitySet<MoneyMovementTransaction> pendingMMTs = MoneyMovementTransaction.findTaxPayments()
                .setCompany(pPayrollRun.getCompany())
                .setPending()
                .setNonDirect()
                .setPeriodBeginDate(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getFromAccrualDate()))
                .setPeriodEndDate(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getToAccrualDate()))
                .find();

        List<PaymentTemplate> paymentTemplates = new ArrayList<PaymentTemplate>();
        for (MoneyMovementTransaction mmt : pendingMMTs) {
            PaymentTemplate paymentTemplate = mmt.getPaymentTemplate();
            if (paymentTemplate.isFollowsFederal() && newDueDate != null) {
                mmt.updateDueDate(newDueDate);
            }
        }

        DomainEntitySet<CompanyAgencyPaymentTemplate> companyAgencyPaymentTemplates = CompanyAgencyPaymentTemplate.findSupportedCompanyAgencyPaymentTemplates(pPayrollRun.getCompany());
        for (CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate : companyAgencyPaymentTemplates) {
            if (companyAgencyPaymentTemplate.getPaymentTemplate().isFollowsFederal()) {
                paymentTemplates.add(companyAgencyPaymentTemplate.getPaymentTemplate());
            }
        }

        return paymentTemplates;
    }

    private static void applyPendingCredits(Company pCompany) {
        DomainEntitySet<FinancialTransaction> employerDebitTransactions =
                Application.find(FinancialTransaction.class, FinancialTransaction.Company().equalTo(pCompany)
                                                                                 .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDebit,
                                                                                                                                                    TransactionTypeCode.EmployerTaxDirectDebit))
                                                                                 .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));

        for (PayrollRun payrollRun : PayrollRun.getPayrollsInMemory(pCompany)) {
            employerDebitTransactions.addAll(
                    payrollRun.getFinancialTransactionCollection()
                              .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDebit,
                                                                                                  TransactionTypeCode.EmployerTaxDirectDebit)
                                                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created))));
        }

        // remove zero dollar debits, and debits that have already offloaded        
        for (Iterator<FinancialTransaction> iterator = employerDebitTransactions.iterator(); iterator.hasNext(); ) {
            FinancialTransaction financialTransaction = iterator.next();
            if(financialTransaction.getFinancialTransactionAmount().isZero() ||
                    financialTransaction.getMoneyMovementTransaction() == null ||
                    !financialTransaction.getMoneyMovementTransaction().isPendingMMT()) {
                iterator.remove();
            }
        }
        
        if(employerDebitTransactions.size() > 0) {
            for (FinancialTransaction employerDebitTransaction : employerDebitTransactions.sort(FinancialTransaction.PayrollRun().PaycheckDate())) {
                
                // check for available credits
                Map<PaymentTemplate, MoneyMovementTransaction> payrollPayments = new HashMap<PaymentTemplate, MoneyMovementTransaction>();
                for (FinancialTransaction financialTransaction : employerDebitTransaction.getPayrollRun().getFinancialTransactionCollection()) {
                    if(financialTransaction.getMoneyMovementTransaction() != null &&
                            financialTransaction.getMoneyMovementTransaction().getPaymentTemplate() != null) {
                        payrollPayments.put(financialTransaction.getMoneyMovementTransaction().getPaymentTemplate(),
                                            financialTransaction.getMoneyMovementTransaction());
                    }
                }

                boolean recalculateDebit = false;
                if (!LedgerAccount.getLedgerAccountBalanceIncludingPayrollInMemory(pCompany, LedgerAccountCode.ERPayable).isZero()) {
                    recalculateDebit = true;
                } else {
                    Map<PaymentTemplate, SpcfDecimal> debitAmountPerTemplate = getDebitAmountPerTemplate(employerDebitTransaction.getPayrollRun());
                    Map<Law, SpcfMoney> agencyTaxRefundBalanceMap =
                            LedgerAccount.getLedgerAccountBalanceByPaymentTemplateAndQuarter(LedgerAccountCode.AgencyTaxRefund, null, pCompany, employerDebitTransaction.getPayrollRun().getPaycheckDate());
                    for (Law law : agencyTaxRefundBalanceMap.keySet()) {
                        PaymentTemplate paymentTemplate = law.getPaymentTemplate();
                        if (agencyTaxRefundBalanceMap.get(law).isGreaterThan(SpcfMoney.ZERO)) {
                            if (employerDebitTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxDebit) {
                                if (calculateMaxOverpaymentAvailable(employerDebitTransaction.getPayrollRun(),
                                                                     payrollPayments,
                                                                     debitAmountPerTemplate, paymentTemplate).isGreaterThan(SpcfMoney.ZERO)) {
                                    recalculateDebit = true;
                                    break;
                                }
                            } else {
                                if (calculateMaxDirectOverpaymentAvailable(employerDebitTransaction.getPayrollRun(),
                                                                     payrollPayments,
                                                                     debitAmountPerTemplate, paymentTemplate).isGreaterThan(SpcfMoney.ZERO)) {
                                    recalculateDebit = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                
                if(recalculateDebit) {
                    CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(employerDebitTransaction.getCompany());
                    BankAccount intuitTaxAccount = getIntuitTaxBankAccount();

                    //  cancel the pending tax credits
                    cancelPendingEmployerTaxCredits(employerDebitTransaction.getPayrollRun(), null, employerDebitTransaction.getTransactionType().getTransactionTypeCd());

                    if(employerDebitTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxDebit) {
                        // cancel the debit
                        employerDebitTransaction.cancelFinancialTransaction();

                        // recreate the debit and credits
                        createEmployerTaxDebit(employerDebitTransaction.getPayrollRun(), companyBankAccount, intuitTaxAccount, employerDebitTransaction, payrollPayments, null);
                    } else {
                        MoneyMovementTransaction pendingDirectDebit = employerDebitTransaction.getMoneyMovementTransaction();

                        // cancel the debit
                        employerDebitTransaction.cancelFinancialTransaction();

                        createEmployerDirectDebit(employerDebitTransaction.getPayrollRun(), pendingDirectDebit.getPaymentTemplate(), companyBankAccount, null, pendingDirectDebit);
                    }
                }
            }
        }        
    }

    public static void applyAgencyTaxOverpayments(Company pCompany, SpcfCalendar pProcessingDate) {
        //Check for overpayments for the processing date
        SpcfCalendar quarterDate = pProcessingDate.copy();
        CalendarUtils.clearTime(quarterDate);
        SpcfCalendar quarterStart = CalendarUtils.getFirstDayOfQuarter(quarterDate);
        SpcfCalendar quarterEnd = CalendarUtils.getLastDayOfQuarter(quarterDate);
        CalendarUtils.endOfDay(quarterEnd);

        DomainEntitySet<PayrollRun> payrollInMemory = PayrollRun.getPayrollsInMemory(pCompany);

        SpcfCalendar initiationDate = quarterStart.copy();
        CalendarUtils.addBusinessDays(initiationDate, -45);
        CalendarUtils.clearTime(initiationDate);

        DomainEntitySet<MoneyMovementTransaction> payments =
                Application.find(MoneyMovementTransaction.class,
                        MoneyMovementTransaction.Company().equalTo(pCompany)
                                .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.OnHold))
                                .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold))
                                .And(MoneyMovementTransaction.InitiationDate().greaterOrEqualThan(initiationDate))
                                .And(MoneyMovementTransaction.PaymentPeriodBegin().between(quarterStart, quarterEnd)));

        // add all the payments in memory.
        for (PayrollRun payrollRun : payrollInMemory) {
            for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
                MoneyMovementTransaction moneyMovementTransaction = financialTransaction.getMoneyMovementTransaction();
                if (moneyMovementTransaction != null && moneyMovementTransaction.isPendingTaxPayment() &&
                        moneyMovementTransaction.getPaymentPeriodBegin().between(quarterStart, quarterEnd)) {
                    payments.add(moneyMovementTransaction);
                }
            }
        }

        // filter out direct debit
        payments = payments.find(MoneyMovementTransaction.MoneyMovementPaymentMethod().isNull().Or(MoneyMovementTransaction.MoneyMovementPaymentMethod().notIn(PaymentMethod.EFTPSDirectDebit)));

        // remove deleted payments
        for (Iterator<MoneyMovementTransaction> iterator = payments.iterator(); iterator.hasNext(); ) {
            MoneyMovementTransaction moneyMovementTransaction = iterator.next();
            if (!Application.getHibernateSession().contains(moneyMovementTransaction)
                    || (moneyMovementTransaction.getOffloadBatch() != null && moneyMovementTransaction.getOffloadBatch().getStatusCd() == OffloadBatchStatus.Completed)) {
                iterator.remove();
            }
        }

        payments = payments.sort(MoneyMovementTransaction.DueDate());

        // Calculate max amount for each law in the template. This is done first so that we do not have to keep track of the transactions in the cache.
        List<PaymentTemplate> processedTemplates = new ArrayList<PaymentTemplate>();
        Map<Law, SpcfDecimal> lawOverpaymentTotalMap = new HashMap<Law, SpcfDecimal>();
        for (MoneyMovementTransaction payment : payments) {
            if (processedTemplates.contains(payment.getPaymentTemplate())) {
                // only need to calculate the balance once per template
                continue;
            }

            processedTemplates.add(payment.getPaymentTemplate());

            Map<Law, SpcfMoney> agencyTaxRefundBalanceMap =
                    LedgerAccount.getLedgerAccountBalanceByPaymentTemplateAndQuarter(LedgerAccountCode.AgencyTaxRefund, payment.getPaymentTemplate(), pCompany, quarterDate);

            if (payment.getPaymentTemplate().isRolledUpAnnually()) {
                //special rule: FUTA can be applied across quarters, but not across years
                LedgerAccount.addLedgerBalanceFromPriorQuartersInYear(agencyTaxRefundBalanceMap, LedgerAccountCode.AgencyTaxRefund, pCompany, quarterDate, payment.getPaymentTemplate());
            }

            for (Law law : payment.getPaymentTemplate().getLawCollection()) {
                SpcfDecimal agencyTaxRefundBalance = agencyTaxRefundBalanceMap.get(law);
                if (agencyTaxRefundBalance == null) {
                    agencyTaxRefundBalance = SpcfMoney.ZERO;
                }

                if (agencyTaxRefundBalance.isGreaterThan(SpcfMoney.ZERO)) {
                    lawOverpaymentTotalMap.put(law, agencyTaxRefundBalance);
                }
            }
        }

        for (MoneyMovementTransaction payment : payments) {
            SpcfDecimal paymentTotal = payment.getMMTBalance();
            if (paymentTotal.isLessThanEqualTo(SpcfMoney.ZERO)) {
                continue;
            }

            Map<Law, SpcfMoney> liabilities = payment.getLiabilityBalances();

            // first try to apply amounts based on the liabilities
            for (Law law : payment.getPaymentTemplate().getLawCollection()) {
                SpcfDecimal agencyTaxRefundBalance = lawOverpaymentTotalMap.get(law);
                if (paymentTotal.isLessThanEqualTo(SpcfMoney.ZERO) || agencyTaxRefundBalance == null || agencyTaxRefundBalance.isLessThanEqualTo(SpcfMoney.ZERO)) {
                    continue;
                }

                SpcfMoney liabilityAmount = liabilities.get(law);
                if (liabilityAmount == null || liabilityAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
                    continue;
                }

                SpcfDecimal overpaymentAppliedAmount;
                if (liabilityAmount.isGreaterThanEqualTo(agencyTaxRefundBalance)) {
                    overpaymentAppliedAmount = agencyTaxRefundBalance;
                } else {
                    overpaymentAppliedAmount = liabilityAmount;
                }

                if (overpaymentAppliedAmount.isGreaterThanEqualTo(paymentTotal)) {
                    overpaymentAppliedAmount = paymentTotal;
                }

                if (overpaymentAppliedAmount.isGreaterThan(SpcfMoney.ZERO)) {
                    FinancialTransaction.createAgencyTaxOverpaymentApplied(pCompany, new SpcfMoney(overpaymentAppliedAmount), SettlementType.ApplyForward, payment.generatePaymentSettlementDate(), law, payment);
                    lawOverpaymentTotalMap.put(law, agencyTaxRefundBalance.subtract(overpaymentAppliedAmount));
                    liabilities.put(law, new SpcfMoney(liabilityAmount.subtract(overpaymentAppliedAmount)));
                    paymentTotal = paymentTotal.subtract(overpaymentAppliedAmount);
                }
            }

            // next apply across the template
            for (Law law : liabilities.keySet()) {
                SpcfMoney liabilityAmount = liabilities.get(law);
                if (liabilityAmount == null || liabilityAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
                    continue;
                }

                for (Law templateLaw : payment.getPaymentTemplate().getLawCollection()) {
                    SpcfDecimal agencyTaxRefundBalance = lawOverpaymentTotalMap.get(templateLaw);
                    if (paymentTotal.isLessThanEqualTo(SpcfMoney.ZERO) || agencyTaxRefundBalance == null || agencyTaxRefundBalance.isLessThanEqualTo(SpcfMoney.ZERO)) {
                        continue;
                    }

                    SpcfDecimal overpaymentAppliedAmount;
                    if (liabilityAmount.isGreaterThanEqualTo(agencyTaxRefundBalance)) {
                        overpaymentAppliedAmount = agencyTaxRefundBalance;
                    } else {
                        overpaymentAppliedAmount = liabilityAmount;
                    }

                    if (overpaymentAppliedAmount.isGreaterThanEqualTo(paymentTotal)) {
                        overpaymentAppliedAmount = paymentTotal;
                    }

                    if (overpaymentAppliedAmount.isGreaterThan(SpcfMoney.ZERO)) {

                        FinancialTransaction.createAgencyTaxOverpaymentApplied(pCompany, new SpcfMoney(overpaymentAppliedAmount), SettlementType.ApplyForward, payment.generatePaymentSettlementDate(), templateLaw, payment);
                        lawOverpaymentTotalMap.put(templateLaw, agencyTaxRefundBalance.subtract(overpaymentAppliedAmount));
                        liabilityAmount = new SpcfMoney(liabilityAmount.subtract(overpaymentAppliedAmount));
                        paymentTotal = paymentTotal.subtract(overpaymentAppliedAmount);
                    }
                }
            }
        }
    }

    public static ProcessResult createNegativeLiabilityAdjustments(PayrollRun pPayrollRun, Map<Law, SpcfDecimal> pLiabilityAmountUpdates) {
        ProcessResult processResult = new ProcessResult();

        // Create an adjustment and force the financial transactions to be added to the first MMT of the list
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
        companyAdjustmentSubmissionDTO.setSubmissionDate(new DateDTO(PSPDate.getPSPTime()));
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(new ArrayList<LiabilityAdjustmentDTO>());
        companyAdjustmentSubmissionDTO.setIsVoid(false);

        SpcfMoney total = SpcfMoney.ZERO;
        for (Law law : pLiabilityAmountUpdates.keySet()) {
            SpcfDecimal amount = pLiabilityAmountUpdates.get(law);
            if (amount.isGreaterThan(SpcfMoney.ZERO)) {
                total = new SpcfMoney(total.add(amount));
                LiabilityAdjustmentDTO liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
                liabilityAdjustmentDTO.setAmount(new SpcfMoney(amount.negate()));
                liabilityAdjustmentDTO.setEffectiveDate(new DateDTO(pPayrollRun.getPaycheckDate()));
                liabilityAdjustmentDTO.setLawId(law.getLawId());
                liabilityAdjustmentDTO.setReconcilingAdjustment(false);
                liabilityAdjustmentDTO.setTaxableWages(SpcfMoney.ZERO);
                liabilityAdjustmentDTO.setTotalWages(SpcfMoney.ZERO);
                QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
                qbdtTransactionInfoDTO.setMemo("Negative liability adjustment for void of negative liability");
                qbdtTransactionInfoDTO.setToken(-2L);
                liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
                companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().add(liabilityAdjustmentDTO);
            }
        }
        if (companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().size() > 0) {
            companyAdjustmentSubmissionDTO.setTotalAmount(total);
            LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
            liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
            liabilityAdjustmentOptionsDTO.setDebitCustomer(false);
            liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(false);
            processResult.merge(PayrollServices.payrollManager.addLiabilityAdjustments(pPayrollRun.getCompany().getSourceSystemCd(), pPayrollRun.getCompany().getSourceCompanyId(),
                    pPayrollRun.getSourcePayRunId(), companyAdjustmentSubmissionDTO, new DateDTO(pPayrollRun.getPaycheckDate()), liabilityAdjustmentOptionsDTO));
        }
        return processResult;
    }

    public static ProcessResult createPayrollForPositiveLiability(PayrollRun pPayrollRun, Map<Law, SpcfDecimal> pLiabilityAmountUpdates) {
        ProcessResult processResult = new ProcessResult();

        // Create an adjustment and force the financial transactions to be added to the first MMT of the list
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
        companyAdjustmentSubmissionDTO.setSubmissionDate(new DateDTO(PSPDate.getPSPTime()));
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(new ArrayList<LiabilityAdjustmentDTO>());
        companyAdjustmentSubmissionDTO.setIsVoid(false);

        SpcfMoney total = SpcfMoney.ZERO;
        for (Law law : pLiabilityAmountUpdates.keySet()) {
            SpcfDecimal amount = pLiabilityAmountUpdates.get(law);
            if (amount.isGreaterThan(SpcfMoney.ZERO)) {
                total = new SpcfMoney(total.add(amount));
                LiabilityAdjustmentDTO liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
                liabilityAdjustmentDTO.setAmount(new SpcfMoney(amount));
                liabilityAdjustmentDTO.setEffectiveDate(new DateDTO(pPayrollRun.getPaycheckDate()));
                liabilityAdjustmentDTO.setLawId(law.getLawId());
                liabilityAdjustmentDTO.setReconcilingAdjustment(false);
                liabilityAdjustmentDTO.setTaxableWages(SpcfMoney.ZERO);
                liabilityAdjustmentDTO.setTotalWages(SpcfMoney.ZERO);
                QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
                qbdtTransactionInfoDTO.setMemo("Positive liability adjustment for void of negative liability");
                qbdtTransactionInfoDTO.setToken(-2L);
                liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
                companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().add(liabilityAdjustmentDTO);
            }
        }
        if (companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().size() > 0) {
            companyAdjustmentSubmissionDTO.setTotalAmount(total);
            LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
            liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
            liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
            liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);
            processResult.merge(PayrollServices.payrollManager.addLiabilityAdjustments(pPayrollRun.getCompany().getSourceSystemCd(), pPayrollRun.getCompany().getSourceCompanyId(),
                    null, companyAdjustmentSubmissionDTO, new DateDTO(pPayrollRun.getPaycheckDate()), liabilityAdjustmentOptionsDTO));
        }
        return processResult;
    }

    /*
    Combine eligible payments for this company.  This is necessary because of the order in which payments are added when threshold calculations are made.
    Simply checks eligible payments from database and candidate cache
     */
    public static void combinePayments(Company company) {
        DomainEntitySet<MoneyMovementTransaction> payments =
                MoneyMovementTransaction.findTaxPayments()
                                        .setCompany(company)
                                        .setReadyToSend()
                                        .setNonDirect()
                                        .find()
                                        .find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend)); //in case changed in memory and not in DB
        payments.addAll(MoneyMovementTransaction.CandidateMMTCache.get(company)
                                                .find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend)
                                                                              .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().notEqualTo(PaymentMethod.EFTPSDirectDebit))));
        payments = payments.sort(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd(),
                      MoneyMovementTransaction.DueDate(),
                      MoneyMovementTransaction.InitiationDate(),
                      MoneyMovementTransaction.PaymentPeriodBegin(),
                      MoneyMovementTransaction.PaymentPeriodEnd());
        MoneyMovementTransaction combineWith = null;
        for (MoneyMovementTransaction payment : payments) {
            if (combineWith == null) {
                combineWith = payment;
                continue;
            }
            if (combineWith.getPaymentTemplate().equals(payment.getPaymentTemplate())
                    && combineWith.getDueDate().toLocal().equals(payment.getDueDate().toLocal())
                    && combineWith.getInitiationDate().toLocal().equals(payment.getInitiationDate().toLocal())
                    && combineWith.getPaymentPeriodBegin().toLocal().equals(payment.getPaymentPeriodBegin().toLocal())
                    && combineWith.getPaymentPeriodEnd().toLocal().equals(payment.getPaymentPeriodEnd().toLocal())) {
                combineWith.combinePayment(payment);
            } else {
                combineWith = payment;
            }
        }

    }
}
