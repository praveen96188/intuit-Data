package com.intuit.sbd.payroll.psp.domain;

import com.intuit.payroll.agency.api.*;
import com.intuit.payroll.agency.dao.FrequencyData;
import com.intuit.payroll.agency.dao.mnemonics.MnemonicPeriod;
import com.intuit.payroll.agency.impl.UpperLimit;
import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.domain.util.TransactionSummary;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;

import java.util.*;

/**
 * Hand-written business logic
 */
public class MoneyMovementTransaction extends BaseMoneyMovementTransaction implements IUpdatable {
    private static final SpcfMoney ZERO = new SpcfMoney("0.00");
    public static final SpcfLogger logger = Application.getLogger(MoneyMovementTransaction.class);
    private transient boolean cachedForSUIAdjustment = false;

    public static final String ACHDEBIT_EXCEPTIONAL_CASE = "NY-1MN-PAYMENT";
    public static final String IS_OVEERIDE_BACKDATE_HOLD_FOR_BULK_DEBIT = "Cache:IS_OVEERIDE_BACKDATE_HOLD_FOR_BULK_DEBIT";
    public static String AgencyTaxPayerIdKeyName="MMT_ATaxPayerId";

    static ArrayList<HashMap<TransactionTypeCode, SkuType>> allowedCombinations;
    private static List<String> backdateInitDatePaymentTemplatesToCheckRules = null;
    private static final List<TransactionTypeCode> transactionTypesToCombineMMTWithNoPayrollRun = Arrays.asList(TransactionTypeCode.EmployerInterestRefundCredit, TransactionTypeCode.EmployerPenaltiesRefundCredit);

    static {
        MoneyMovementTransaction.allowedCombinations = new ArrayList<HashMap<TransactionTypeCode, SkuType>>();

        HashMap<TransactionTypeCode, SkuType> types = new HashMap<TransactionTypeCode, SkuType>();
        types.put(TransactionTypeCode.EmployerDdDebit, null);
        types.put(TransactionTypeCode.EmployerFeeDebit, SkuType.Payroll);
        types.put(TransactionTypeCode.ServiceSalesAndUseTax, SkuType.Payroll);
        types.put(TransactionTypeCode.EmployerTaxDebit, null);
        types.put(TransactionTypeCode.EmployerTaxCreditApplied, null);
        types.put(TransactionTypeCode.EmployerTaxOverpaymentApplied, null);
        types.put(TransactionTypeCode.EmployerDdRedebit, null);
        types.put(TransactionTypeCode.EmployerFeeRedebit, SkuType.Payroll);
        types.put(TransactionTypeCode.ServiceSalesAndUseTaxRedebit, SkuType.Payroll);
        types.put(TransactionTypeCode.EmployerTaxRedebit, null);

        types.put(TransactionTypeCode.EmployerDoublePaymentRefundCredit, SkuType.Payroll);
        types.put(TransactionTypeCode.EmployerTaxDoublePaymentRefundCredit, SkuType.Payroll);
        types.put(TransactionTypeCode.EmployerDdRefundCredit, SkuType.Payroll);
        types.put(TransactionTypeCode.EmployerDdReturnedRefundCredit, SkuType.Payroll);
        types.put(TransactionTypeCode.EmployerTaxRefundCredit, null);
        types.put(TransactionTypeCode.EmployerFeeRefundCredit, SkuType.Payroll);
        types.put(TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit, SkuType.Payroll);
        types.put(TransactionTypeCode.EmployerFeeReturnedRefundCredit, SkuType.Payroll);
        types.put(TransactionTypeCode.ServiceSalesAndUseTaxReturnedRefundCredit, SkuType.Payroll);
        types.put(TransactionTypeCode.EmployerTaxReturnedRefundCredit, null);

        // todo: Erika to confirm that these go here
        types.put(TransactionTypeCode.EmployerTaxCredit, null);
        types.put(TransactionTypeCode.EmployerTaxReturnedCredit, null);
        types.put(TransactionTypeCode.EmployerCreditBalanceCarryForwardCredit, null);
        MoneyMovementTransaction.allowedCombinations.add(types);

        types = new HashMap<TransactionTypeCode, SkuType>();
        types.put(TransactionTypeCode.EmployerFeeDebit, SkuType.NonPayroll);
        types.put(TransactionTypeCode.ServiceSalesAndUseTax, SkuType.NonPayroll);
        types.put(TransactionTypeCode.EmployerFeeRedebit, SkuType.NonPayroll);
        types.put(TransactionTypeCode.ServiceSalesAndUseTaxRedebit, SkuType.NonPayroll);
        types.put(TransactionTypeCode.EmployerFeeRefundCredit, SkuType.NonPayroll);
        types.put(TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit, SkuType.NonPayroll);
        types.put(TransactionTypeCode.EmployerFeeReturnedRefundCredit, SkuType.NonPayroll);
        types.put(TransactionTypeCode.ServiceSalesAndUseTaxReturnedRefundCredit, SkuType.NonPayroll);
        MoneyMovementTransaction.allowedCombinations.add(types);

        types = new HashMap<TransactionTypeCode, SkuType>();
        types.put(TransactionTypeCode.EmployerFraudOrEscalationRefundCredit, null);
        types.put(TransactionTypeCode.EmployerTaxFraudOrEscalationRefundCredit, null);
        MoneyMovementTransaction.allowedCombinations.add(types);

        types = new HashMap<TransactionTypeCode, SkuType>();
        types.put(TransactionTypeCode.EmployerInterestRefundCredit, null);
        types.put(TransactionTypeCode.EmployerPenaltiesRefundCredit, null);
        MoneyMovementTransaction.allowedCombinations.add(types);

     }

    private static final String MMTS_IN_MEMORY_CACHE_KEY = "MMTInMemory";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static DomainEntitySet<MoneyMovementTransaction> findMissedMMTransactions(SpcfCalendar pOffloadDate) {
        SpcfCalendar offloadDateMinus30 = pOffloadDate.copy();
        offloadDateMinus30.addDays(-30);

        //
        // Important note: It is very important that we use pOffloadDate - 1 so that we do not inadvertently cancel today's
        //                 transactions if the MTP accidentally gets run early for the current business day.
        //
        SpcfCalendar offloadDateMinus1 = pOffloadDate.copy();
        offloadDateMinus1.addDays(-1);

        int i = 0;
        String[] paramNames = new String[4];
        paramNames[i++] = "txnState";
        paramNames[i++] = "settlementType";
        paramNames[i++] = "offloadDateMinus30";
        paramNames[i++] = "offloadDateMinus1";

        i = 0;
        Object[] paramValues = new Object[4];
        paramValues[i++] = TransactionStateCode.Created;
        paramValues[i++] = SettlementType.ACH;
        paramValues[i++] = offloadDateMinus30;
        paramValues[i++] = offloadDateMinus1;

        return Application.findByNamedQueryUsingCache(MoneyMovementTransaction.class, "findMissedMMTransactions", paramNames, paramValues);
    }

    public static DomainEntitySet<MoneyMovementTransaction> findMissedMMTransactionsCriteria(SpcfCalendar pOffloadDate) {
        //TODO: Added Parallel Hint here, need to optimise this query
        SpcfCalendar offloadDateMinus30 = pOffloadDate.copy();
        offloadDateMinus30.addDays(-30);

        //
        // Important note: It is very important that we use pOffloadDate - 1 so that we do not inadvertently cancel today's
        //                 transactions if the MTP accidentally gets run early for the current business day.
        //
        SpcfCalendar offloadDateMinus1 = pOffloadDate.copy();
        offloadDateMinus1.addDays(-1);

        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>().QueryHint("PARALLEL(8)")
                .Where(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold)
                        .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDirectDeposit))
                        .And(MoneyMovementTransaction.PaymentTemplate().isNull())
                        .And(MoneyMovementTransaction.FinancialTransactionSet().Filter().SettlementTypeCd().equalTo(SettlementType.ACH))
                        .And(MoneyMovementTransaction.FinancialTransactionSet().Filter().CurrentTransactionState().equalTo(TransactionState.findTransactionState(TransactionStateCode.Created)))
                        .And(MoneyMovementTransaction.InitiationDate().between(offloadDateMinus30, offloadDateMinus1))
                ).OrderBy(MoneyMovementTransaction.Company().SourceCompanyId())
                .EagerLoad(MoneyMovementTransaction.QbdtTransactionInfo().Company().equalTo(MoneyMovementTransaction.Company()))
                .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company().equalTo(MoneyMovementTransaction.Company()))
                .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().QbdtTransactionInfo().Company().equalTo(MoneyMovementTransaction.Company()));
        return Application.find(MoneyMovementTransaction.class, query);
    }

    public boolean hasTaxCredits() {
        return (this.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit, TransactionTypeCode.AgencyDirectCredit)).size() > 0);
    }

    public boolean isTaxPayment() {
        return getTaxPaymentStatus() != null && getTaxPaymentStatus() != TaxPaymentStatus.None;
    }

    public boolean isPendingTaxPayment() {
        return (getTaxPaymentStatus() == TaxPaymentStatus.OnHold || getTaxPaymentStatus() == TaxPaymentStatus.ReadyToSend || getTaxPaymentStatus() == TaxPaymentStatus.Ignore)
                && isPendingMMT();
    }

    public boolean isPendingMMT() {
        return (isOffloadPending() && (getStatus() == PaymentStatus.Created || getStatus() == PaymentStatus.OnHold));
    }

    public boolean isOffloadPending() {
        return getOffloadBatch() == null ||
                (getOffloadBatch().getStatusCd() != OffloadBatchStatus.Completed &&
                        PSPDate.getPSPTime().before(getOffloadBatch().getOffloadGroup().getCalendarForCutoffTime(getInitiationDate().toLocal())));
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Calculate/Recalculate MoneyMovementTransaction
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * If the given settlement type requires creation of a moneymovement transaction
     *
     * @param pSettlementType
     * @return
     */
    public static boolean isSettlementTypeForMMTxn(SettlementType pSettlementType) {
        return pSettlementType.equals(SettlementType.ACH);
    }


    /**
     * Adds a agency tax credit financial transaction to a MMT
     * If a suitable MMT exists, adds the FT to it.  Otherwise, creates a new MMT for the FT.
     * <p/>
     *
     * @param pFT FinancialTransaction
     */
    public static void addFinancialTransactionToTaxPaymentMMT(FinancialTransaction pFT) {
        // Find Existing MMT using Payment Template and Payment Period
        PaymentTemplate paymentTemplate = pFT.getLaw().getPaymentTemplate();

        //Is the Tax payment belongs to a payroll run with Completed ERTaxDebit transaction
        boolean isAmountCollected = false;

        //Get CompanyAgencyPaymentTemplate to find the  Effective Deposit Frequency based on the Paycheck Date
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pFT.getCompany(), paymentTemplate);
        if (companyAgencyPaymentTemplate == null) {
            throw new RuntimeException("No Company Agency Payment Template Found for " + paymentTemplate.getPaymentTemplateAbbrev() + " FT:" + pFT.getId());
        }

        DepositFrequencyCode depositFrequencyCode;
        if (pFT.getPayrollRun() != null) {
            depositFrequencyCode = paymentTemplate.getEffectiveDepositFrequency(pFT.getPayrollRun());
            isAmountCollected = pFT.getPayrollRun().isERTaxDebitCollected();
        } else {
            depositFrequencyCode = paymentTemplate.getEffectiveDepositFrequency(pFT.getCompany(), pFT.getSettlementDate());
        }
        if (depositFrequencyCode == null) {
            throw new RuntimeException("No Effective Deposit Frequency Found For Paycheck Date " + pFT.getPayrollRun().getPaycheckDate().toISO8601() + " and Payment template " + companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateAbbrev() + " FT:" + pFT.getId());
        }

        TransactionTypeCode transactionType = pFT.getTransactionType().getTransactionTypeCd();

        // Call Due Date Calculator to get Payment Period for the Effective Deposit Frequency
        IPaymentPeriod paymentPeriod = null;
        if (pFT.getPayrollRun() != null) {
            paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(paymentTemplate.getPaymentTemplateCd(), depositFrequencyCode.toString(), CalendarUtils.convertToRulesCalendar(pFT.getPayrollRun().getPaycheckDate()));
        } else {
            paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(paymentTemplate.getPaymentTemplateCd(), depositFrequencyCode.toString(), CalendarUtils.convertToRulesCalendar(pFT.getSettlementDate()));
        }

        // If it is a Direct Debit MMT we can't use the regular payment period
        if (transactionType == TransactionTypeCode.AgencyDirectCredit || transactionType == TransactionTypeCode.AgencyDirectDebit) {
            UpperLimit upperLimit = paymentTemplate.getThresholdInfo(paymentTemplate.getPaymentTemplateCd(), depositFrequencyCode.toString());
            paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(paymentTemplate.getPaymentTemplateCd(), upperLimit.rollOverFrequency, CalendarUtils.convertToRulesCalendar(pFT.getPayrollRun().getPaycheckDate()));
        }


        PaymentMethod paymentMethod = pFT.findPaymentMethod();
        SpcfCalendar initDate;

        if (pFT.getPayrollRun() != null) {
            SpcfCalendar debitInitiationDate = pFT.getPayrollRun().getEmployerTaxDebitTransaction() != null ? pFT.getPayrollRun().getEmployerTaxDebitTransaction().getInitiationDate() : null;
            initDate = MoneyMovementTransaction.getPaymentInitDate(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getDueDate()), paymentMethod, depositFrequencyCode, paymentTemplate, pFT.getPayrollRun().isBackDated() && !pFT.getPayrollRun().getCreatorId().equals(SystemPrincipal.LedgerOperationsBatchJob.getId()), debitInitiationDate, pFT.isEFTPS_ACHPaymentTransaction());
        } else {
            initDate = MoneyMovementTransaction.getPaymentInitDate(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getDueDate()), paymentMethod, depositFrequencyCode, paymentTemplate, false, null, false);
        }

        MoneyMovementTransaction existingMMT = null;

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = new DomainEntitySet<MoneyMovementTransaction>();

        //AdjustSUITaxPayment unfinalize the finalized payment so more than one pending MMTs are present.
        //SUI Adjustment MMT is cached with transient value cachedForSHUIAdjustment = true to identify SUIAdjustment MMT. First look at the cache with this special indicator
        DomainEntitySet<MoneyMovementTransaction> mmtsFromCache = CandidateMMTCache.get(
                pFT.getCompany()).find(getCriteriaToFindMatchingMMT(paymentTemplate, transactionType, paymentPeriod,
                                                                    pFT.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyTaxDebit ? pFT.getPayrollRun().getPaycheckDate() : initDate, isAmountCollected));

        for (MoneyMovementTransaction moneyMovementTransaction : mmtsFromCache) {
            if (moneyMovementTransaction.cachedForSUIAdjustment) {
                existingMMT = moneyMovementTransaction;
                break;
            }
        }

        if (existingMMT == null) {
            if (pFT.getPayrollRun() != null) {
                DomainEntitySet<FinancialTransaction> financialTransactions =
                        pFT.getPayrollRun().getFinancialTransactionCollection()
                           .find(FinancialTransaction.MoneyMovementTransaction().PaymentTemplate().equalTo(paymentTemplate)
                                                     .And(FinancialTransaction.TransactionType().TransactionCategory().equalTo(TransactionCategory.Agency))
                                                     .And(FinancialTransaction.MoneyMovementTransaction().Status().in(PaymentStatus.Created, PaymentStatus.OnHold)));
                for (FinancialTransaction financialTransaction : financialTransactions) {
                    if (isAmountCollected) {
                        if (financialTransaction.getMoneyMovementTransaction().getTaxPaymentStatus() != TaxPaymentStatus.OnHold) {
                            moneyMovementTransactions.add(financialTransaction.getMoneyMovementTransaction());
                        }
                    } else {
                        moneyMovementTransactions.add(financialTransaction.getMoneyMovementTransaction());
                    }
                }
            }

            if (moneyMovementTransactions.isEmpty()) {
                moneyMovementTransactions.addAll(findMoneyMovementTransactions(pFT.getCompany(), paymentTemplate, transactionType, paymentPeriod,
                                                                               pFT.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyTaxDebit ? pFT.getPayrollRun().getPaycheckDate() : initDate,
                                                                               isAmountCollected));
            }

            // make sure the mmt is not deleted
            if (moneyMovementTransactions.size() > 0) {
                int mmtIndex = 0;
                MoneyMovementTransaction readyToSendMMT = null;
                for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
                    if (Application.getHibernateSession().contains(mmt)) {
                        existingMMT = moneyMovementTransactions.get(mmtIndex);
                        if (isAmountCollected && moneyMovementTransactions.get(mmtIndex).getTaxPaymentStatus() != TaxPaymentStatus.OnHold) {
                            readyToSendMMT = moneyMovementTransactions.get(mmtIndex);
                        }
                    } else {
                        mmtIndex++;
                    }
                }

                if (readyToSendMMT != null) {
                    existingMMT = readyToSendMMT;
                }

            }
        }

        if (existingMMT == null && pFT.getPayrollRun() != null) {
            existingMMT = paymentTemplate.getTaxPayment(pFT.getPayrollRun(), initDate, !isAmountCollected); // If ER Tax Debit amount is collected, do not add to OnHold payments.
        }

        // make sure the payment has not been deleted in this session
        if (existingMMT != null && (!existingMMT.isPendingTaxPayment() || !Application.getHibernateSession().contains(existingMMT))) {
            existingMMT = null;
        }

        // If the existing MMT is ATF Finalized we can't add transactions to it - a new one will be created with an Agent Hold
        boolean addBackDateHold = false;
        if (existingMMT == null && DepositFrequencyCode.QUARTERLY.equals(depositFrequencyCode)) {
            if (moneyMovementTransactions.isEmpty()) {
                DomainEntitySet<MoneyMovementTransaction> mmtsWithSamePeriod = findMoneyMovementTransactionsByPaymentPeriodHoldInformation(pFT.getCompany(), paymentTemplate, paymentPeriod, isAmountCollected, transactionType);
                for (Iterator<MoneyMovementTransaction> iterator = mmtsWithSamePeriod.iterator(); iterator.hasNext(); ) {
                    MoneyMovementTransaction moneyMovementTransaction = iterator.next();
                    if (moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.ATFFinalized)) {
                        addBackDateHold = true;
                        iterator.remove();
                    } else if (initDate.after(moneyMovementTransaction.getInitiationDate())) {
                        iterator.remove();
                    }
                }
                moneyMovementTransactions.addAll(mmtsWithSamePeriod);
            }
            if (moneyMovementTransactions.size() > 0) {
                int mmtIndex = 0;
                MoneyMovementTransaction readyToSendMMT = null;
                for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
                    if (Application.getHibernateSession().contains(mmt)) {
                        existingMMT = moneyMovementTransactions.get(mmtIndex);
                        if (isAmountCollected && moneyMovementTransactions.get(mmtIndex).getTaxPaymentStatus() != TaxPaymentStatus.OnHold) {
                            readyToSendMMT = moneyMovementTransactions.get(mmtIndex);
                        }
                    } else {
                        mmtIndex++;
                    }
                }
                if (readyToSendMMT != null) {
                    existingMMT = readyToSendMMT;
                }
            }
        }

        if (existingMMT != null && (existingMMT.getTaxPaymentStatus().equals(TaxPaymentStatus.ATFFinalized))) {
            addBackDateHold = true;
            existingMMT = null;
        }

        //check MMTs in cache
        if (existingMMT == null) {
            existingMMT = CandidateMMTCache.get(pFT.getCompany()).find(getCriteriaToFindMatchingMMT(paymentTemplate, transactionType, paymentPeriod,
                                                                                                    pFT.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyTaxDebit ? pFT.getPayrollRun().getPaycheckDate() : initDate, isAmountCollected)).getFirst();
        }

        // make sure the payment has not been deleted in this session
        if (existingMMT != null && (!existingMMT.isPendingTaxPayment() || !Application.getHibernateSession().contains(existingMMT))) {
            existingMMT = null;
        }

        MoneyMovementTransaction newMoneyMovementTransaction = null;
        if (existingMMT != null &&
                (pFT.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.AgencyDirectCredit &&
                        pFT.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.AgencyDirectDebit)) {
            if (TransactionType.subtractsFromPayment(pFT.getTransactionType().getTransactionTypeCd()) &&
                    existingMMT.getMoneyMovementTransactionAmount().isLessThan(pFT.getFinancialTransactionAmount())) {
                if (pFT.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.AgencyTaxDebit)) {
                    if (pFT.getRelatedTransactionsCollection() == null || pFT.getRelatedTransactionsCollection().size() == 0) {
                        pFT = pFT.cancelFinancialTransaction();
                        if (pFT.getSettlementDate() == null) {
                            pFT.setSettlementDate(existingMMT.getSettlementDate());
                        }
                        FinancialTransaction.createAgencyTaxOverpaymentTransaction(pFT.getPayrollRun(),
                                                                                   pFT.getLaw(),
                                                                                   pFT.getDebitBankAccount(),
                                                                                   pFT.getFinancialTransactionAmount());
                    }
                } else if (pFT.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.AgencyTaxOverpaymentApplied)) {
                    pFT.cancelFinancialTransaction();
                } else {
                    updateExistingMMT(pFT, existingMMT);
                }
            } else {
                updateExistingMMT(pFT, existingMMT);
            }
        } else if (pFT.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyCreditBalanceCarryForwardDebit) {
            throw new RuntimeException("carry forward agency debit without the offsetting agency credit  FT:" + pFT.getId());
        } else {
            if ((pFT.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyDirectCredit ||
                    pFT.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyDirectDebit) ||
                    (paymentTemplate.getAgencyTaxDirectCredits(pFT.getPayrollRun(), TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Completed).size() > 0)) {
                existingMMT = paymentTemplate.getDirectDebitTaxPayment(pFT.getPayrollRun(), initDate);
                if (existingMMT == null && paymentTemplate.getAgencyTaxDirectCredits(pFT.getPayrollRun(), TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Completed).size() > 0) {
                    existingMMT = paymentTemplate.getfOffloadedDirectDebitTaxPayment(pFT.getPayrollRun());
                }
            }

            if (existingMMT != null) {
                updateExistingMMT(pFT, existingMMT);
            } else if (TransactionType.subtractsFromPayment(pFT.getTransactionType().getTransactionTypeCd())) {
                if (pFT.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.AgencyTaxDebit)) {
                    if (pFT.getRelatedTransactionsCollection() == null || pFT.getRelatedTransactionsCollection().size() == 0) {
                        pFT = pFT.cancelFinancialTransaction();
                        if (pFT.getSettlementDate() == null) {
                            SpcfCalendar settlementDate = MoneyMovementTransaction.getNextInitiationDate(paymentMethod);
                            CalendarUtils.addBusinessDays(settlementDate, 1);
                            pFT.setSettlementDate(settlementDate);
                        }
                        FinancialTransaction.createAgencyTaxOverpaymentTransaction(pFT.getPayrollRun(),
                                                                                   pFT.getLaw(),
                                                                                   pFT.getDebitBankAccount(),
                                                                                   pFT.getFinancialTransactionAmount());
                    }
                } else if (pFT.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.AgencyTaxOverpaymentApplied)) {
                    pFT.cancelFinancialTransaction();
                } else {
                    newMoneyMovementTransaction = MoneyMovementTransaction.createTaxMoneyMovementTransaction(pFT, paymentPeriod, paymentTemplate, depositFrequencyCode);
                }
            } else {
                UpperLimit upperLimit = paymentTemplate.getThresholdInfo(paymentTemplate.getPaymentTemplateCd(), depositFrequencyCode.toString());
                if (pFT.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyDirectCredit) {
                    paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(paymentTemplate.getPaymentTemplateCd(), upperLimit.rollOverFrequency, CalendarUtils.convertToRulesCalendar(pFT.getPayrollRun().getPaycheckDate()));
                }
                // Create MMT transaction
                newMoneyMovementTransaction = MoneyMovementTransaction.createTaxMoneyMovementTransaction(pFT, paymentPeriod, paymentTemplate, depositFrequencyCode);
            }
        }

        if (newMoneyMovementTransaction != null && paymentTemplate.getCategory() == PaymentTemplateCategory.SUI) {
            if (!addBackDateHold) {
                SpcfCalendar todayMinus31 = PSPDate.getPSPTime().copy();
                todayMinus31.addDays(-31);
                // short cut to avoid db query for back dates more than 2 months after the end of the quarter
                if (newMoneyMovementTransaction.getPaymentPeriodEnd().before(todayMinus31)) {
                    addBackDateHold = true;
                } else {
                    DomainEntitySet<MoneyMovementTransaction> existingSUIPayments =
                            Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate)
                                                                                                     .And(MoneyMovementTransaction.Company().equalTo(newMoneyMovementTransaction.getCompany()))
                                                                                                     .And(MoneyMovementTransaction.PaymentPeriodBegin().equalTo(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getFromAccrualDate())))
                                                                                                     .And(MoneyMovementTransaction.PaymentPeriodEnd().equalTo(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getToAccrualDate())))
                                                                                                     .And(MoneyMovementTransaction.Status().in(PaymentStatus.Executed, PaymentStatus.InProcess)));
                    if (existingSUIPayments.size() > 0) {
                        addBackDateHold = true;
                    }
                }
            }

            if (addBackDateHold) {
                Boolean isOverrideBulkDebit = (Boolean) Application.getSessionCache().getNonHibernateObject(IS_OVEERIDE_BACKDATE_HOLD_FOR_BULK_DEBIT);
                if (isOverrideBulkDebit == null || !isOverrideBulkDebit) {
                    newMoneyMovementTransaction.addTaxPaymentOnHoldReason(PaymentOnHoldReason.BackDate);
                }
            }
        }

        if (pFT.getMoneyMovementTransaction() != null) {
            SpcfCalendar newSettlementDate = pFT.getMoneyMovementTransaction().generatePaymentSettlementDate();
            pFT.setSettlementDate(newSettlementDate);
            pFT.setOriginalSettlementDate(newSettlementDate);

            // Recalculate MMT status based on current state
            pFT.getMoneyMovementTransaction().recalculatePaymentStatus();
        }
    }

    public static Criterion<MoneyMovementTransaction> getCriteriaToFindMatchingMMT(PaymentTemplate paymentTemplate, TransactionTypeCode transactionType, IPaymentPeriod paymentPeriod, SpcfCalendar initDate, boolean pIsERTaxDebitCollected) {
        // The criteria for finding the MMT has to include the payment period
        // If transaction is AgencyTaxOverpayment we are looking for an MMT with a status of Executed, otherwise Created

        Criterion<MoneyMovementTransaction> mmtCriteria;

        mmtCriteria = PaymentTemplate().equalTo(paymentTemplate);

        // do not apply ATDs to direct debit payments
        if (transactionType == TransactionTypeCode.AgencyTaxDebit) {
            mmtCriteria = mmtCriteria.And(MoneyMovementPaymentMethod().notEqualTo(PaymentMethod.EFTPSDirectDebit).Or(MoneyMovementPaymentMethod().isNull()))
                                     .And(InitiationDate().greaterOrEqualThan(initDate));
        } else {
            mmtCriteria = mmtCriteria.And(InitiationDate().equalTo(initDate));
        }

        mmtCriteria = mmtCriteria.And(PaymentPeriodBegin().equalTo(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getFromAccrualDate()))
                                              .And(PaymentPeriodEnd().equalTo(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getToAccrualDate()))))
                                 .And(Status().in(PaymentStatus.Created, PaymentStatus.OnHold)
                                              .And(TaxPaymentStatus().notEqualTo(TaxPaymentStatus.ATFFinalized)));

        //We can combine an FT that subtracts from a payment with an on-hold payment, even if the associated payroll funds are collected.  Still give preference to ReadyToSend
        if (pIsERTaxDebitCollected && !TransactionType.subtractsFromPayment(transactionType)) {
            mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.TaxPaymentStatus().notEqualTo(TaxPaymentStatus.OnHold));
        }

        return mmtCriteria;
    }

    public static DomainEntitySet<MoneyMovementTransaction> findMoneyMovementTransactions(Company company, PaymentTemplate paymentTemplate, TransactionTypeCode transactionType, IPaymentPeriod paymentPeriod, SpcfCalendar initDate, boolean pIsERTaxDebitCollected) {

        Criterion<MoneyMovementTransaction> mmtCriteria = getCriteriaToFindMatchingMMT(paymentTemplate, transactionType, paymentPeriod, initDate, pIsERTaxDebitCollected);

        mmtCriteria = mmtCriteria.And(Company().equalTo(company));
        Expression<MoneyMovementTransaction> mmtQuery =
                new Query<MoneyMovementTransaction>()
                        .Where(mmtCriteria)
                        .OrderBy(InitiationDate());

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, mmtQuery);

        // it is possible that some of the transactions found have changed in memory, filter them out if they don't match the criteria anymore
        moneyMovementTransactions = moneyMovementTransactions.find(mmtCriteria);

        return moneyMovementTransactions;
    }

    public static DomainEntitySet<MoneyMovementTransaction> findMoneyMovementTransactionsByPaymentPeriodHoldInformation(Company company, PaymentTemplate paymentTemplate, IPaymentPeriod paymentPeriod, Boolean isAmountCollected, TransactionTypeCode pTransactionTypeCode) {
        return (findMoneyMovementTransactionsByPaymentPeriodWithOptionalHoldInformation(company, paymentTemplate, paymentPeriod, isAmountCollected, pTransactionTypeCode));
    }

    //PSP-13621 - changes are done for ACHZeroPayment job.
    public static DomainEntitySet<MoneyMovementTransaction> findMoneyMovementTransactionsByPaymentPeriod(Company company, PaymentTemplate paymentTemplate, IPaymentPeriod paymentPeriod) {

        Criterion<MoneyMovementTransaction> mmtCriteria;

        mmtCriteria = PaymentTemplate().equalTo(paymentTemplate)
                .And(Company().equalTo(company));

        mmtCriteria = mmtCriteria.And(PaymentPeriodBegin().equalTo(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getFromAccrualDate()))
                .And(PaymentPeriodEnd().equalTo(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getToAccrualDate()))))
                .And(Status().in(PaymentStatus.Created, PaymentStatus.OnHold));
        Query<MoneyMovementTransaction> mmtQuery = new Query<MoneyMovementTransaction>();

        mmtQuery.Where(mmtCriteria)
                .OrderBy(InitiationDate());

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, mmtQuery);

        // filter again to pick up any updates in memory
        return moneyMovementTransactions.find(mmtQuery);
    }


    private static DomainEntitySet<MoneyMovementTransaction> findMoneyMovementTransactionsByPaymentPeriodWithOptionalHoldInformation(Company company, PaymentTemplate paymentTemplate, IPaymentPeriod paymentPeriod, Boolean isAmountCollected, TransactionTypeCode pTransactionTypeCode) {
        Criterion<MoneyMovementTransaction> mmtCriteria;

        mmtCriteria = PaymentTemplate().equalTo(paymentTemplate)
                .And(Company().equalTo(company));

        mmtCriteria = mmtCriteria.And(PaymentPeriodBegin().equalTo(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getFromAccrualDate()))
                                              .And(PaymentPeriodEnd().equalTo(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getToAccrualDate()))))
                                 .And(DueDate().equalTo(CalendarUtils.convertToSpcfCalendar(paymentPeriod.getDueDate())))
                                 .And(Status().in(PaymentStatus.Created, PaymentStatus.OnHold));
        Query<MoneyMovementTransaction> mmtQuery = new Query<MoneyMovementTransaction>();

        if (isAmountCollected != null && isAmountCollected && pTransactionTypeCode != null && !TransactionType.subtractsFromPayment(pTransactionTypeCode)) {
            mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.TaxPaymentStatus().notEqualTo(TaxPaymentStatus.OnHold));
        }

        mmtQuery.Where(mmtCriteria)
                .OrderBy(InitiationDate());

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, mmtQuery);

        // filter again to pick up any updates in memory
        return moneyMovementTransactions.find(mmtQuery);
    }

    public static void updateExistingMMT(FinancialTransaction pFT, MoneyMovementTransaction existingMMT) {
        existingMMT.addAgencyFinancialTransaction(pFT);
    }

    public static SpcfCalendar getPaymentInitDate(SpcfCalendar dueDate, PaymentMethod pPaymentMethod, DepositFrequencyCode pDepositFrequencyCode, PaymentTemplate pPaymentTemplate, boolean isBackDated, SpcfCalendar debitInitiationDate, boolean isTaxPaymentTransaction) {
        SpcfCalendar dueDateCopy = dueDate.copy();

        SpcfCalendar initDate = dueDateCopy.copy();
        CalendarUtils.clearTime(initDate);

        int offset = getPaymentMethodDayOffset(pPaymentMethod,pPaymentTemplate);
        CalendarUtils.addBusinessDays(initDate, offset * -1);

        SpcfCalendar nextValidInitDate = MoneyMovementTransaction.getNextInitiationDate(pPaymentMethod);
        if (initDate.before(nextValidInitDate)) {
            initDate = nextValidInitDate;
            CalendarUtils.clearTime(initDate);
        }

        if (isBackDated && isTaxPaymentTransaction) {
            int backDatedPayrollPaymentOffset = SystemParameter.findIntValue(SystemParameter.Code.BACKDATED_PAYROLL_INITIATION_DATE_OFFSET);
            //Check to see if due date is within next few days, then move original initiation date
            CalendarUtils.addBusinessDays(dueDateCopy, (backDatedPayrollPaymentOffset) * -1);
            SpcfCalendar pspTime = PSPDate.getPSPTime();
            CalendarUtils.clearTime(pspTime);
            if (dueDateCopy.before(pspTime)) {

                SpcfCalendar debitInitiationDatePlusOffset = null;
                if (debitInitiationDate != null) {
                    debitInitiationDatePlusOffset = debitInitiationDate.copy();
                    CalendarUtils.addBusinessDays(debitInitiationDatePlusOffset, backDatedPayrollPaymentOffset);

                    if (debitInitiationDatePlusOffset.before(nextValidInitDate)) {
                        initDate = nextValidInitDate;
                    } else {
                        initDate = debitInitiationDatePlusOffset;
                    }
                } else {
                    initDate = nextValidInitDate;
                    CalendarUtils.addBusinessDays(initDate, backDatedPayrollPaymentOffset); // Init date is set with next possible Init date + BACKDATED_PAYROLL_INITIATION_DATE_OFFSET value
                }
                CalendarUtils.clearTime(initDate);

                //Check if any payment templates to apply rules from - SystemParameter.Code.BACKDATED_PAYROLL_INIT_DATE_PAYMENT_TEMPLATES_TO_CHECK_DAY_RULES
                if (pDepositFrequencyCode != null && pPaymentTemplate != null && getBackDateInitDatePaymentTemplatesToCheckRules().contains(pPaymentTemplate.getPaymentTemplateCd())) {
                    FrequencyData frequencyData = getFrequencyDataFromAgencyRules(pPaymentTemplate.getPaymentTemplateCd(), pDepositFrequencyCode.toString());
                    if (frequencyData != null && !frequencyData.getMnemonicPeriods().isEmpty()) {
                        MnemonicPeriod mnemonicPeriod = frequencyData.getMnemonicPeriods().get(0);
                        int dueOnDayOfWeek = mnemonicPeriod.getDueOnDayOfWeek();
                        if (dueOnDayOfWeek > -1) {
                            SpcfCalendar settlementDate = initDate.copy();
                            CalendarUtils.addBusinessDays(settlementDate, offset); // Calculating settlement date from calculated Backdate payment Init date by adding offset

                            while (settlementDate.getDayOfWeek() != dueOnDayOfWeek) {
                                CalendarUtils.addBusinessDays(initDate, 1);
                                CalendarUtils.addBusinessDays(settlementDate, 1);
                            }
                        }
                    }
                }
            }
        }
        return initDate;
    }

    public void addAgencyFinancialTransaction(FinancialTransaction pFT) {
        if (getFinancialTransactionCollection().contains(pFT)) {
            return;
        }

        if (pFT.getCurrentFinancialTransactionState() != null &&
                pFT.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Created &&
                !isPendingTaxPayment()) {
            throw new RuntimeException("A created transaction is being added to a payment that is no longer pending.  " +
                                               "Trying to add to FT:" + pFT.getId());
        }

        // associate the FT and the MMT
        pFT.setMoneyMovementTransaction(this);
        addFinancialTransaction(pFT);

        SpcfMoney newAmount = getMoneyMovementTransactionAmount();
        if (pFT.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Created) {
            if (TransactionType.subtractsFromPayment(pFT.getTransactionType().getTransactionTypeCd())) {
                newAmount = new SpcfMoney(newAmount.subtract(pFT.getFinancialTransactionAmount()));
            } else if (TransactionType.addsToPayment(pFT.getTransactionType().getTransactionTypeCd())) {
                newAmount = new SpcfMoney(newAmount.add(pFT.getFinancialTransactionAmount()));
            }
            updateTaxMoneyMovementTransactionAmount(newAmount);

            // If the MMT Amount = ZERO, we need to set the status to Ignore if the agency does not require a zero payment. This will cause this
            // payment not to be offloaded
            recalculateTaxPaymentStatus();
        }

        if (pFT.getSettlementDate() == null) {
            SpcfCalendar settlementDate = generatePaymentSettlementDate();
            pFT.setSettlementDate(settlementDate);
            pFT.setOriginalSettlementDate(settlementDate);
        }

        MoneyMovementTransaction mmt = this;
        if (PaymentMethod.ACHCredit.equals(this.getMoneyMovementPaymentMethod())) {
            mmt = recreateEntryDetailRecords(this);

        }
        Application.save(mmt);
        if (isPendingMMT() && !(getMoneyMovementTransactionAmount().compareTo(getMMTBalance()) == 0)) {
            throw new RuntimeException("Invalid Money Movement Transaction Amount isPendingMMT:" + isPendingMMT() +
                                               " MMT Amount:" + getMoneyMovementTransactionAmount() + " FT Total:" + getMMTBalance() + " MMT:" + getId());
        }


    }

    public MoneyMovementTransaction adjustMMTInitiationDate() {
        if (this.getMoneyMovementPaymentMethod().equals(PaymentMethod.EFTPS) && this.getMoneyMovementTransactionAmount().compareTo(ZERO) > 0) {
            SpcfCalendar nextInitiationDate = getNextEFTPSInitiationDate();
            if (getInitiationDate().before(nextInitiationDate)) {
                this.updateTaxInitiationDate(nextInitiationDate);
            }
        }
        return Application.save(this);
    }

    public void cancelAgencyTaxOverpaymentAppliedTransactions() {
        DomainEntitySet<FinancialTransaction> financialTransactions = getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxOverpaymentApplied));
        for (FinancialTransaction financialTransaction : financialTransactions) {
            financialTransaction.cancelFinancialTransaction();
        }
    }

    /**
     * Determines whether the given FT may be combined with others. Returns null if it can't, the allowed group if it can
     * <p/>
     * TODO v2: This method will work when each transaction-type/sku-type appears in only one of the allowedCombination... but
     * TODO v2: if it appears in two, and if the MMT's existing FTs fit only in the second of the two, this returns false incorrectly
     *
     * @param pFT the FT in need of an MMT
     * @return
     */
    private static HashMap<TransactionTypeCode, SkuType> getAllowedGroup
    (FinancialTransaction
             pFT) {
        HashMap<TransactionTypeCode, SkuType> allowedGroup = null;
        SkuType ftSkuType = getSkuTypeForFT(pFT);
        TransactionTypeCode ftTypeCd = pFT.getTransactionType().getTransactionTypeCd();

        for (HashMap<TransactionTypeCode, SkuType> combination : allowedCombinations) {
            if (combination.containsKey(ftTypeCd)) {
                SkuType allowedSkuType = combination.get(ftTypeCd);
                if (allowedSkuType == null || allowedSkuType == ftSkuType) {
                    allowedGroup = combination;
                }
            }
        }
        return allowedGroup;
    }

    /**
     * Determines whether the give FT may be added into the existing MMT.  Suitability is based on MMT initiation
     * and settlement dates, and the types and other properties of the new FT and the MMT's existing FTs.
     * <p/>
     *
     * @param pFT  the FT in need of an MMT
     * @param pMMT an existing MMT to be tested for suitability
     * @return
     */

    private static boolean ftIsCompatible(FinancialTransaction pFT, HashMap<TransactionTypeCode, SkuType> pAllowedGroup, MoneyMovementTransaction pMMT) {
        // the FT must have dates that would agree with this MMT's initiation and due dates
        SpcfCalendar ftInitDate = pFT.getInitiationDate();
        SpcfCalendar ftSettlementDate = pFT.getSettlementDate().toLocal();
        SpcfCalendar mmtInitDate = pMMT.getInitiationDate().toLocal();
        SpcfCalendar mmtDueDate = pMMT.getDueDate().toLocal();
        if (!(mmtInitDate.equals(ftInitDate) && mmtDueDate.equals(ftSettlementDate))) {
            return false;
        }

        for (FinancialTransaction ftExisting : pMMT.getFinancialTransactionCollection()) {
            // skip checking for tax credit transactions
            if (ftExisting.getSettlementTypeCd() == SettlementType.ApplyForward) {
                continue;
            }

            //Find the allowed group, if any, for the financial transaction we're currently examining
            HashMap<TransactionTypeCode, SkuType> currAllowedGroup = getAllowedGroup(ftExisting);

            //If it can't be combined or it can't be combined in the same group as the passed transaction, return false
            if (currAllowedGroup == null || currAllowedGroup != pAllowedGroup) {
                return false;
            }

            // they must have the same PayrollRun (and it can't be null) or special case: they are both null and have specific types
            PayrollRun prExisting = ftExisting.getPayrollRun();
            PayrollRun prNew = pFT.getPayrollRun();
            if (!((prExisting != null && prNew != null && prExisting.getId().equals(prNew.getId())) ||
                    (prExisting == null && prNew == null && transactionTypesToCombineMMTWithNoPayrollRun.contains(ftExisting.getTransactionType().getTransactionTypeCd())
                            && transactionTypesToCombineMMTWithNoPayrollRun.contains(pFT.getTransactionType().getTransactionTypeCd())))) {
                return false;
            }


            if (!ftExisting.getSettlementTypeCd().equals(pFT.getSettlementTypeCd())) {
                return false;
            }

            if (!ftExisting.getSettlementDate().toLocal().equals(pFT.getSettlementDate().toLocal())) {
                return false;
            }

            // they must both credit or debit a Company bank account, and it must be the same one
            CompanyBankAccount cbaExisting = ftExisting.getCompanyBankAccount();
            CompanyBankAccount cbaNew = pFT.getCompanyBankAccount();
            if (!(cbaExisting != null && cbaNew != null && cbaExisting.getId().equals(cbaNew.getId()))) {
                return false;
            }
        }

        return true;
    }

    private static SkuType getSkuTypeForFT(FinancialTransaction pFT) {
        String sku = pFT.getSku();
        SkuType skuType = null;
        if (sku != null) {
            OfferingServiceCharge ofc = OfferingServiceCharge.findBySKU(sku);
            if (ofc == null) {
                return null;
            }
            skuType = ofc.getSkuType();
        }
        return skuType;
    }

    /**
     * Function to remove the Cancelled Financial Transaction amount from the MoneyMovementTransaction and
     * if no more Financial Transactions remain for the MoneyMovementTransaction deletes the MMT
     *
     * @param pFinancialTransaction FinancialTransaction
     */
    public static void subtractFinancialTransaction(FinancialTransaction pFinancialTransaction) {
        MoneyMovementTransaction mmt = pFinancialTransaction.getMoneyMovementTransaction();
        if (mmt != null) {


            mmt.removeFinancialTransaction(pFinancialTransaction);
            pFinancialTransaction.setMoneyMovementTransaction(null);

            mmt = Application.save(mmt);

            if (mmt.getFinancialTransactionCollection().size() == 0) {
                MoneyMovementTransaction.deleteMoneyMovementTransaction(mmt);
            } else {
                MoneyMovementTransaction.recreateEntryDetailRecords(mmt);

                // Recalculate MMT status based on current state
                mmt.recalculatePaymentStatus();
            }
        }
    }

    /**
     * Function to remove the Cancelled Financial Transaction amount from the Tax MoneyMovementTransaction and
     * if no more Financial Transactions remain for the MoneyMovementTransaction deletes the MMT
     *
     * @param pFinancialTransaction FinancialTransaction
     */
    public static void removeFinancialTransactionFromTaxPaymentMMT(FinancialTransaction pFinancialTransaction) {
        MoneyMovementTransaction mmt = pFinancialTransaction.getMoneyMovementTransaction();
        if (mmt != null) {
            mmt.removeAgencyFinancialTransaction(pFinancialTransaction);
        }
    }

    private void removeAgencyFinancialTransaction(FinancialTransaction pFinancialTransaction) {
        if (!isPendingMMT() && pFinancialTransaction.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.AgencyTaxOverpayment) {
            throw new RuntimeException("Cannot remove a financial transaction from an MMT in non-Created state   "
                                               + "MMT: " + getId() + ":" + getStatus()
                                               + "   - FT: " + pFinancialTransaction.getId()
                                               + ":" + pFinancialTransaction.getTransactionType()
                                               + ":" + pFinancialTransaction.getCurrentFinancialTransactionState().getTransactionState().getName()
                                               + ":" + pFinancialTransaction.getFinancialTransactionAmount());
        }

        if (!getFinancialTransactionCollection().contains(pFinancialTransaction)) {
            return;
        }

        removeFinancialTransaction(pFinancialTransaction);
        pFinancialTransaction.setMoneyMovementTransaction(null);

        SpcfMoney newAmount = getMoneyMovementTransactionAmount();
        if (TransactionType.subtractsFromPayment(pFinancialTransaction.getTransactionType().getTransactionTypeCd())) {
            newAmount = new SpcfMoney(newAmount.add(pFinancialTransaction.getFinancialTransactionAmount()));
        } else if (TransactionType.addsToPayment(pFinancialTransaction.getTransactionType().getTransactionTypeCd())) {
            newAmount = new SpcfMoney(newAmount.subtract(pFinancialTransaction.getFinancialTransactionAmount()));
        }

        updateTaxMoneyMovementTransactionAmount(newAmount);

        // If the MMT Amount = ZERO, we need to set the status to Ignore if the agency does not require a zero payment. This will cause this
        // payment not to be offloaded
        recalculateTaxPaymentStatus();

        Application.save(this);

        if (getFinancialTransactionCollection().size() == 0) {
            for (TaxPaymentOnHoldReason taxPaymentOnHoldReason : getTaxPaymentOnHoldReasonCollection()) {
                Application.delete(taxPaymentOnHoldReason);
            }
            MoneyMovementTransaction.deleteMoneyMovementTransaction(this);
        } else {
            // Recalculate MMT status based on current state
            recalculatePaymentStatus();
        }
        if (!(getMoneyMovementTransactionAmount().compareTo(getMMTBalance()) == 0)) {
            throw new RuntimeException("Invalid Money Movement Transaction Amount MMT Amount:" +
                                               getMoneyMovementTransactionAmount() + " FT Total:" + getMMTBalance() + " MMT:" + getId());
        }

        if (isTaxPayment() && getMoneyMovementTransactionAmount().isLessThan(SpcfMoney.ZERO)) {
            cancelAgencyTaxOverpaymentAppliedTransactions();
        }
    }

    public void delete() {
        MoneyMovementTransaction.deleteMoneyMovementTransaction(this);
    }

    /**
     * For the given ACH financial transaction, delete any associated money movement transactions
     *
     * @param pMMT Financial Transaction for which to delete money movement transactions
     */
    public static void deleteMoneyMovementTransaction(MoneyMovementTransaction pMMT) {
        while (pMMT.getEntryDetailRecordCollection().size() > 0) {
            EntryDetailRecord currEntryDetailRec = pMMT.getEntryDetailRecordCollection().get(0);
            pMMT.removeEntryDetailRecord(currEntryDetailRec);
            Application.delete(currEntryDetailRec);
        }

        // remove it from the cache, if it's there
        CandidateMMTCache.remove(pMMT);

        // BIN/BIO is interested in knowing which records were deleted
        if (SystemParameter.findBooleanValue(SystemParameter.Code.TRACK_MMT_DELETES, false)) {
            DeletedRecord deletedRecord = new DeletedRecord();
            deletedRecord.setRecordIdentifier(pMMT.getId().toString());
            deletedRecord.setTableName("PSP_MONEY_MOVEMENT_TRANSACTION");
            Application.save(deletedRecord);
        }

        // delete it
        Application.delete(pMMT);
    }

    /**
     * Add a money movement transaction and any other required entities (e.g. EntryDetailRecord) for a single
     * financial transaction
     *
     * @param pFinancialTransaction Financial transaction to add a money movement transaction for
     * @return the new MoneyMovementTransaction
     */
    public static MoneyMovementTransaction createMoneyMovementTransaction(FinancialTransaction pFinancialTransaction) {
        MoneyMovementTransaction mmTxn = new MoneyMovementTransaction();
        mmTxn.setCompany(pFinancialTransaction.getCompany());

        mmTxn.setPaymentFrequency(null);
        //The due date is the same as the settlement date
        mmTxn.setDueDate(pFinancialTransaction.getSettlementDate().toLocal());

        // verify initiation date is in future
        SpcfCalendar initDate = pFinancialTransaction.getInitiationDate();

        if (!initDate.after(PSPDate.getPSPTime())) {
            SpcfCalendar today = PSPDate.getPSPTime();

            CalendarUtils.clearTime(today);

            if (initDate.before(today)) {
                logger.error(
                        "MoneyMovementTransaction is inserted with an initiation date in the past for the Financial " +
                                "Transaction " + pFinancialTransaction.getId());
            } else {
                // if initiation date is today verify we are doing this before offload cutoff time
                // (PSRV001796: accounting for potentially scheduled second offload)

                OffloadGroup offloadGroup = pFinancialTransaction.getCompany().getOffloadGroup();
                SpcfCalendar initDateCopy = initDate.copy();

                CalendarUtils.clearTime(initDateCopy);

                if (initDateCopy.equals(today) && !offloadGroup.isBeforeActualCutoffTime(PSPDate.getPSPTime())) {
                    logger.error(
                            "MoneyMovementTransaction is inserted with an initiation date as today but after today's " +
                                    "offload cutoff time, for the Financial Transaction " + pFinancialTransaction.getId());
                }
            }
        }

        mmTxn.setInitiationDate(initDate);
        mmTxn.setOriginalInitiationDate(initDate);
        mmTxn.setMoneyMovementPaymentMethod(pFinancialTransaction.findPaymentMethod());
        mmTxn.setMoneyMovementTransactionAmount(pFinancialTransaction.getFinancialTransactionAmount());
        mmTxn.setStatus(PaymentStatus.Created);
        mmTxn.setTransactionNumber(getNextTransactionNumber());

        //Bi-directional, so set the association both ways
        pFinancialTransaction.setMoneyMovementTransaction(mmTxn);
        mmTxn.addFinancialTransaction(pFinancialTransaction);

        //Save the money movement transaction so that fin txn and entry detail are working with the saved object
        mmTxn = Application.save(mmTxn);

        if (PaymentMethod.ACHDirectDeposit.equals(mmTxn.getMoneyMovementPaymentMethod())) {
            mmTxn = recreateEntryDetailRecords(mmTxn);
        }

        return mmTxn;
    }

    /**
     * If a suitable MMT exists, adds the FT to it.  Otherwise, creates a new MMT for the FT.
     * <p/>
     * This method has a significant effect on performance of key processes (like SubmitPayroll) because
     * it may get called hundreds of times for a large payroll
     * A characteristic is that ee paychecks are the most common financial transaction processed and they are
     * never combined with others. That is why the "allowedGroup" test is done first - it is inexpensive and there
     * is no need to look for a money movement transaction to combine
     *
     * @param pFT FinancialTransaction
     */
    public static void addFinancialTransactionToMMT(FinancialTransaction pFT) {
        MoneyMovementTransaction existingMMT = null;

        HashMap<TransactionTypeCode, SkuType> allowedGroup = getAllowedGroup(pFT);
        //For Bill Payments: we do not allow combining other debits in one MMT, so set allowedGroup = null, but
        //we combine the fee and tax FT in one MMT.
        if (pFT.getPayrollRun() != null && pFT.getPayrollRun().getPayrollRunType().equals(PayrollType.BillPayment)) {
            if (!pFT.isSalesTaxTransaction() && !pFT.isFeeTransaction()) {
                allowedGroup = null;
            }
        }

        if (allowedGroup != null) {
            // find existing MMTs that might be able to accept the new FT
            DomainEntitySet<MoneyMovementTransaction> candidates = CandidateMMTCache.get(pFT.getCompany(), pFT.getInitiationDate(), PaymentMethod.ACHDirectDeposit);

            // check some additional criteria on each
            for (MoneyMovementTransaction mmt : candidates) {
                if (ftIsCompatible(pFT, allowedGroup, mmt)) {
                    existingMMT = mmt;
                    break;
                }
            }
        }

        if (existingMMT != null) {
            // associate the FT and the MMT
            pFT.setMoneyMovementTransaction(existingMMT);
            existingMMT.addFinancialTransaction(pFT);

            // persist the changes
            existingMMT = Application.save(existingMMT);

            // recreate entry detail records for the modified MMT -- also assigns new "net" MMT amount
            existingMMT = recreateEntryDetailRecords(existingMMT);
        } else {
            switch (pFT.getTransactionType().getTransactionTypeCd()) {
                case EmployerCreditBalanceCarryForwardCredit:
                case AgencyCreditBalanceCarryForwardDebit:
                    // these financial transaction are always combined with EmployerTaxDebit and AgencyTaxCredit
                    // respectively if the settlement type is ACH or EFE
                    // there should not be any MMT for settlement type HPDE
                    return;
            }
            // Create MM transaction and add it to list of candidate money movement transactions (for use
            // when creating other financial transactions in the same process
            MoneyMovementTransaction newMMT = createMoneyMovementTransaction(pFT);

            if (allowedGroup != null) {
                CandidateMMTCache.add(newMMT);
            }
        }

        if (pFT.getMoneyMovementTransaction() != null) {
            pFT.getMoneyMovementTransaction().recalculatePaymentStatus();
        }
    }

    /**
     * Calculates and returns the balances for each law for the MMT
     * The balance is calculated by adding the amounts of AgencyTaxCredit/AgencyTaxDirectCredit financial transactions
     * and subtracting the amounts of AgencyTaxDebit and AgencyTaxOverpaymentApplied financial transactions
     *
     * @return
     */
    public HashMap<Law, SpcfMoney> getLiabilityBalances() {
        HashMap<Law, SpcfMoney> liabilityBalances = new HashMap<Law, SpcfMoney>();
        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit,
                                                                                                       TransactionTypeCode.AgencyDirectCredit,
                                                                                                       TransactionTypeCode.AgencyTaxDebit,
                                                                                                       TransactionTypeCode.AgencyDirectDebit,
                                                                                                       TransactionTypeCode.AgencyTaxOverpaymentApplied));
        SpcfMoney lawAmount;

        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection().find(where)) {
            Law law = financialTransaction.getLaw();

            if (liabilityBalances.containsKey(law)) {
                lawAmount = liabilityBalances.get(law);
            } else {
                lawAmount = new SpcfMoney("0.00");
            }

            if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                lawAmount = (SpcfMoney) lawAmount.add(financialTransaction.getFinancialTransactionAmount());
            } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                lawAmount = (SpcfMoney) lawAmount.subtract(financialTransaction.getFinancialTransactionAmount());
            }

            liabilityBalances.put(law, lawAmount);
        }

        return liabilityBalances;
    }

    /**
     * Calculates and returns the balance for the MMT
     * The balance is calculated by adding the amounts of AgencyTaxCredit/AgencyTaxDirectCredit financial transactions
     * and subtracting the amounts of AgencyTaxDebit and AgencyTaxOverpaymentApplied financial transactions
     *
     * @return
     */
    public SpcfMoney getMMTBalance() {
        SpcfMoney liabilityBalance = new SpcfMoney(ZERO);
        TransactionState cancelledState = Application.findById(TransactionState.class, TransactionStateCode.Cancelled);
        Criterion<FinancialTransaction> where = FinancialTransaction.CurrentTransactionState().notEqualTo(cancelledState);

        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection().find(where)) {
            if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                liabilityBalance = (SpcfMoney) liabilityBalance.add(financialTransaction.getFinancialTransactionAmount());
            } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                liabilityBalance = (SpcfMoney) liabilityBalance.subtract(financialTransaction.getFinancialTransactionAmount());
            }
        }

        return liabilityBalance;
    }

    public BankAccount getLiabilityDebitBankAccount() {
        BankAccount debitBankAccount = null;
        Criterion<FinancialTransaction> where = FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit,
                    TransactionTypeCode.AgencyDirectCredit);

        for (FinancialTransaction ft : getFinancialTransactionCollection().find(where)) {
            if (ft.getDebitBankAccount() != null) {
                debitBankAccount = ft.getDebitBankAccount();
                break; // we have the account, so we're done
            }
        }

        return debitBankAccount;
    }

    public static MoneyMovementTransaction recreateEntryDetailRecords(MoneyMovementTransaction pMMT) {

        // delete existing ones
        while (pMMT.getEntryDetailRecordCollection().size() > 0) {
            EntryDetailRecord entryDetailRec = pMMT.getEntryDetailRecordCollection().get(0);
            pMMT.removeEntryDetailRecord(entryDetailRec);
            Application.delete(entryDetailRec);
        }

        if (pMMT.getMoneyMovementPaymentMethod() == null || pMMT.getMoneyMovementPaymentMethod().notIn(PaymentMethod.ACHCredit, PaymentMethod.ACHDirectDeposit)) {
            return Application.save(pMMT);
        }

        // create new ones and save the new net amount
        if (pMMT.isTaxPayment()) {
            pMMT.createTaxPaymentEntryDetailRecords();
        } else {
            pMMT.createEntryDetailRecords();
        }

        // assign the grand total to the MMT based on signed EntryDetailRecord amounts
        SpcfDecimal grandTotal = SpcfDecimal.createInstance(0);
        for (EntryDetailRecord edr : pMMT.getEntryDetailRecordCollection()) {
            if (edr.getCreditDebitIndicator() == CreditDebitCode.Debit) { // either side will work, but only one side...
                grandTotal = grandTotal.add(edr.getAmount());
            }
        }

        if (pMMT.isTaxPayment()) {
            if (grandTotal.isGreaterThan(SpcfMoney.ZERO)) {
                pMMT.updateTaxMoneyMovementTransactionAmount(new SpcfMoney(grandTotal));
            } else {
                pMMT.recalculateTaxPaymentStatus();
            }
        } else {
            pMMT.setMoneyMovementTransactionAmount(new SpcfMoney(grandTotal));
        }

        return Application.save(pMMT);
    }

    public static IPaymentPeriod getPaymentPeriod(String pTemplateId, String pFrequency,
                                                  RulesCalendar pInputDate) {
        IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();
        IPaymentPeriodRequest ppr = RulesObjectBroker.getInstance().createPaymentPeriodRequest();
        ppr.setPaymentTemplateId(pTemplateId);
        ppr.setFrequencyId(pFrequency);
        // Use PRINT payment method because we don't use payment methods from the Agency Rules
        ppr.setSubmitMethodId("PRINT");
        ppr.setAccrualDate(pInputDate);
        ppr.setRequestType(PaymentPeriodRequestType.RulesBased);

        IRulesPaymentTemplate paymentTemplate = rulesInfo.getPaymentTemplate(pTemplateId);

        // Check if the frequency is supported by the Agency Rules
        // if not we have to ignore this frequency as we will get an exception from the API
        // There is no possible calculation of a payment period for a frequency that is not in the Agency Rules
        FrequencyData freq = (FrequencyData) paymentTemplate.getPaymentFrequency(pFrequency);
        IPaymentPeriod paymentPeriod = null;
        if (freq != null) {
            paymentPeriod = paymentTemplate.getPaymentPeriod(ppr);
        } else {
            logger.error("Payment Period is not found for: Frequency -" + pFrequency + " Template -" + pTemplateId + " Date -" + pInputDate.toString());
        }

        return paymentPeriod;
    }

    public static FrequencyData getFrequencyDataFromAgencyRules(String pTemplateId, String pFrequency) {
        IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();
        IPaymentPeriodRequest ppr = RulesObjectBroker.getInstance().createPaymentPeriodRequest();
        ppr.setPaymentTemplateId(pTemplateId);
        ppr.setFrequencyId(pFrequency);
        // Use PRINT payment method because we don't use payment methods from the Agency Rules
        ppr.setSubmitMethodId("PRINT");
        ppr.setRequestType(PaymentPeriodRequestType.RulesBased);

        IRulesPaymentTemplate paymentTemplate = rulesInfo.getPaymentTemplate(pTemplateId);

        return (FrequencyData) paymentTemplate.getPaymentFrequency(pFrequency);

    }

    public static List<String> getBackDateInitDatePaymentTemplatesToCheckRules() {
        if (backdateInitDatePaymentTemplatesToCheckRules == null) {
            //Extract SystemParameter.Code.BACKDATED_PAYROLL_INIT_DATE_PAYMENT_TEMPLATES_TO_CHECK_DAY_RULES values in to backdatePaymentInitDateRules, Value Ex - NJ-NJ927PWH-PAYMENT,NJ-NJ927PWH-PAYMENT
            String paymentTemplatesToCheck = SystemParameter.findStringValue(SystemParameter.Code.BACKDATED_PAYROLL_INIT_DATE_PAYMENT_TEMPLATES_TO_CHECK_DAY_RULES);
            if (paymentTemplatesToCheck != null) {
                backdateInitDatePaymentTemplatesToCheckRules = Arrays.asList(paymentTemplatesToCheck.split(","));
            }
        }
        return backdateInitDatePaymentTemplatesToCheckRules;
    }

    public DomainEntitySet<TaxPaymentOnHoldReason> getActiveOnHoldReasons() {
        return getTaxPaymentOnHoldReasonCollection().find(TaxPaymentOnHoldReason.ExpirationDate().isNull());
    }

    public boolean hasActiveOnHoldReasons() {
        return getActiveOnHoldReasons().size() > 0;
    }

    public boolean hasActiveOnHoldReason(PaymentOnHoldReason pPaymentOnHoldReason) {
        return getActiveOnHoldReason(pPaymentOnHoldReason) != null;
    }

    public TaxPaymentOnHoldReason getActiveOnHoldReason(PaymentOnHoldReason pPaymentOnHoldReason) {
        return getTaxPaymentOnHoldReasonCollection().findEntity(
                TaxPaymentOnHoldReason.ExpirationDate().isNull()
                                      .And(TaxPaymentOnHoldReason.OnHoldReasonCd().equalTo(pPaymentOnHoldReason)));
    }

    public TaxPaymentOnHoldReason addTaxPaymentOnHoldReason(PaymentOnHoldReason pPaymentOnHoldReason) {
        return addTaxPaymentOnHoldReason(pPaymentOnHoldReason, null);
    }

    public TaxPaymentOnHoldReason addTaxPaymentOnHoldReason(PaymentOnHoldReason pPaymentOnHoldReason, String pNote) {
        if (getTaxPaymentStatus() == TaxPaymentStatus.None)
            throw new RuntimeException("Cannot add PaymentOnHoldReason (" + pPaymentOnHoldReason + ") to a non-TaxPayment MoneyMovementTransaction: " + getCompany() + "  " + this);

        if (getTaxPaymentStatus().notIn(TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.OnHold, TaxPaymentStatus.Ignore)) {
            throw new RuntimeException("Cannot add a PaymentOnHoldReason (" + pPaymentOnHoldReason + ") to a non-pending TaxPayment MoneyMovementTransaction: " + getCompany() + "  " + this);
        }

        if (pPaymentOnHoldReason.equals(PaymentOnHoldReason.Company) && !hasActiveOnHoldReasons() && getTaxImpoundCompletedPayrollRuns().size() > 0) {
            throw new RuntimeException("Cannot add a PaymentOnHoldReason (" + pPaymentOnHoldReason + ") to a MoneyMovementTransaction with completed tax impounds (MMT must be split first): " + getCompany() + "  " + this);
        }
        //added for PSP-10899,
        if(isCompanyOnDDLimitHold(getCompany())){
            logger.log(OffloadBatch.getOffloadBatchChangeLogLevel(),"Company is On DirectDepositLimit hold, so MoneyMovementTransaction should not go on hold, returning from method");
            return null;
        }

        TaxPaymentOnHoldReason existingOnHoldReason = getActiveOnHoldReason(pPaymentOnHoldReason);
        if (existingOnHoldReason != null) {
            return existingOnHoldReason;
        }

        setTaxPaymentStatus(TaxPaymentStatus.OnHold);
        setTaxPaymentStatusEffectiveDate(PSPDate.getPSPTime());

        TaxPaymentOnHoldReason taxPaymentOnHoldReason = new TaxPaymentOnHoldReason();
        taxPaymentOnHoldReason.setCompany(getCompany());
        taxPaymentOnHoldReason.setEffectiveDate(PSPDate.getPSPTime());
        taxPaymentOnHoldReason.setOnHoldReasonCd(pPaymentOnHoldReason);
        taxPaymentOnHoldReason.setNote(pNote);
        Application.save(taxPaymentOnHoldReason);

        taxPaymentOnHoldReason.setMoneyMovementTransaction(this);
        getTaxPaymentOnHoldReasonCollection().add(taxPaymentOnHoldReason);

        Application.save(this);

        return taxPaymentOnHoldReason;
    }

    public boolean hasPendingERTaxDebits() {
        return getPendingERTaxDebits().size() > 0;
    }

    public DomainEntitySet<FinancialTransaction> getPendingERTaxDebits() {
        DomainEntitySet<FinancialTransaction> pendingERTaxDebitTxns = new DomainEntitySet<FinancialTransaction>();

        List<String> payrollRunsProcessed = new ArrayList<String>();
        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection()) {
            PayrollRun payrollRun = financialTransaction.getPayrollRun();
            if (payrollRun != null && !payrollRunsProcessed.contains(payrollRun.getSourcePayRunId())) {
                DomainEntitySet<FinancialTransaction> erTaxDebitTxns = payrollRun.getEmployerTaxDebitTransactions();
                // no ERTaxDebit when company had a credit and liability amount was less than credit
                if (erTaxDebitTxns.size() > 0) {
                    for (FinancialTransaction erTaxDebitTxn : erTaxDebitTxns) {
                        if (erTaxDebitTxn.isPending() || erTaxDebitTxn.isOpen()) {
                            //Verify if there is a redebit that is complete
                            boolean completedRedebit = false;
                            for (FinancialTransaction assocTransaction : erTaxDebitTxn.getAssociatedTransactionsCollection()) {
                                if (assocTransaction.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerTaxRedebit) &&
                                        assocTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd().equals(TransactionStateCode.Completed))
                                    completedRedebit = true;
                                continue;
                            }
                            if (!completedRedebit) {
                                pendingERTaxDebitTxns.add(erTaxDebitTxn);
                            }
                        }
                    }
                }
                payrollRunsProcessed.add(payrollRun.getSourcePayRunId());
            }
        }

        return pendingERTaxDebitTxns;
    }

    public boolean hasCompletedERTaxDebits() {
        return getCompletedERTaxDebits().size() > 0;
    }

    public DomainEntitySet<FinancialTransaction> getCompletedERTaxDebits() {
        DomainEntitySet<FinancialTransaction> completedERTaxDebits = new DomainEntitySet<FinancialTransaction>();

        List<String> payrollRunsProcessed = new ArrayList<String>();
        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection()) {
            PayrollRun payrollRun = financialTransaction.getPayrollRun();
            if (payrollRun != null && !payrollRunsProcessed.contains(payrollRun.getSourcePayRunId())) {
                DomainEntitySet<FinancialTransaction> erTaxDebitTxns =
                        payrollRun.getEmployerTaxDebitTransactions()
                                  .find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Completed));
                // no ERTaxDebit when company had a credit and liability amount was less than credit
                completedERTaxDebits.addAll(erTaxDebitTxns);
                payrollRunsProcessed.add(payrollRun.getSourcePayRunId());
            }
        }

        return completedERTaxDebits;
    }

    /**
     * Find all payroll runs where the tax impound is complete
     * an ERTaxDebit or ERTaxRedebit that has a Completed status
     * no ERTaxDebit associated w/PayrollRun (i.e. all liability was covered by an existing credit)
     *
     * @return all payroll runs where the tax impound is completed or does not exist due to a credit
     */
    public DomainEntitySet<PayrollRun> getTaxImpoundCompletedPayrollRuns() {
        DomainEntitySet<PayrollRun> completedPayrollRuns = new DomainEntitySet<PayrollRun>();

        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection()) {
            PayrollRun payrollRun = financialTransaction.getPayrollRun();
            if (payrollRun != null && !completedPayrollRuns.contains(payrollRun)) {
                DomainEntitySet<FinancialTransaction> erTaxDebitTxns = payrollRun.getEmployerTaxDebitTransactions();
                boolean allCompleted = true;
                for (FinancialTransaction erTaxDebitTxn : erTaxDebitTxns) {
                    allCompleted = erTaxDebitTxn.isClosed();
                    if (!allCompleted) break;
                }
                if (allCompleted) {
                    completedPayrollRuns.add(payrollRun);
                }
            }
        }

        return completedPayrollRuns;
    }


    public DomainEntitySet<FinancialTransaction> getTransactions(TransactionTypeCode pTransactionTypeCode, TransactionStateCode... pTransactionStates) {

        TransactionType agencyTaxCreditType = Application.findById(TransactionType.class, pTransactionTypeCode);

        return getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().equalTo(agencyTaxCreditType)
                                                                            .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(pTransactionStates)));
    }

    public static void removeAndAddAgencyTransactions(DomainEntitySet<FinancialTransaction> debitTransactions, DomainEntitySet<FinancialTransaction> creditTransactions) {
        for (FinancialTransaction agencyTaxDebit : debitTransactions) {
            MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(agencyTaxDebit);
        }

        for (FinancialTransaction agencyTaxCredit : creditTransactions) {
            MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(agencyTaxCredit);
        }

        for (FinancialTransaction agencyTaxCredit : creditTransactions) {
            MoneyMovementTransaction.addFinancialTransactionToTaxPaymentMMT(agencyTaxCredit);
        }

        for (FinancialTransaction agencyTaxDebit : debitTransactions) {
            MoneyMovementTransaction.addFinancialTransactionToTaxPaymentMMT(agencyTaxDebit);
        }
    }

    public static void removeAndAddAgencyTransactions(DomainEntitySet<FinancialTransaction> pFinancialTransactions) {
        // copy the ft collection
        DomainEntitySet<FinancialTransaction> positiveFinancialTransactions = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<FinancialTransaction> negativeFinancialTransactions = new DomainEntitySet<FinancialTransaction>();
        for (FinancialTransaction financialTransaction : pFinancialTransactions) {
            if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                positiveFinancialTransactions.add(financialTransaction);
            } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                negativeFinancialTransactions.add(financialTransaction);
            }
        }

        removeAndAddAgencyTransactions(negativeFinancialTransactions, positiveFinancialTransactions);
    }

    public void combinePayment(MoneyMovementTransaction pMMT) {
        if (!isPendingMMT() || !pMMT.isPendingMMT() || !getTaxPaymentStatus().equals(pMMT.getTaxPaymentStatus())) {
            throw new IllegalStateException("cannot combine MMT (" + this + ") with other MMT (" + pMMT + ")");
        }
        //todo_rhn an event for when MMTs are combined?

        // copy the ft collection
        DomainEntitySet<FinancialTransaction> positiveFinancialTransactions = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<FinancialTransaction> negativeFinancialTransactions = new DomainEntitySet<FinancialTransaction>();
        for (FinancialTransaction financialTransaction : pMMT.getFinancialTransactionCollection()) {
            if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                positiveFinancialTransactions.add(financialTransaction);
            } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                negativeFinancialTransactions.add(financialTransaction);
            }
        }

        // remove negative transactions first
        for (FinancialTransaction financialTransaction : negativeFinancialTransactions) {
            pMMT.removeAgencyFinancialTransaction(financialTransaction);
        }
        // then remove positive transactions
        for (FinancialTransaction financialTransaction : positiveFinancialTransactions) {
            pMMT.removeAgencyFinancialTransaction(financialTransaction);
        }

        // add positive transactions first
        for (FinancialTransaction financialTransaction : positiveFinancialTransactions) {
            addAgencyFinancialTransaction(financialTransaction);
        }
        // then add negative transactions
        for (FinancialTransaction financialTransaction : negativeFinancialTransactions) {
            addAgencyFinancialTransaction(financialTransaction);
        }
    }

    /**
     * Move all AgencyCredit/AgencyDebit transactions associated with PayrollRuns that have ERTaxDebit in a non-closed
     * state to a new MMT.
     *
     * @return the created MMT with the Agency transactions associated w/Payrolls that have ERTaxDebit in a non-closed state
     */
    public MoneyMovementTransaction splitOutHoldPayment() {
        if (getTaxPaymentStatus() != TaxPaymentStatus.ReadyToSend || !isPendingMMT()) {
            throw new IllegalStateException("cannot split MMT (" + this + ") - TaxPaymentStatus -" + getTaxPaymentStatus() + " Payment Status-" + getStatus().toString());
        }
        MoneyMovementTransaction mmtHold = splitPayment(getPendingERTaxDebits());

        // copy hold reason onto new mmt
        for (TaxPaymentOnHoldReason taxPaymentOnHoldReason : getActiveOnHoldReasons()) {
            mmtHold.addTaxPaymentOnHoldReason(taxPaymentOnHoldReason.getOnHoldReasonCd());
        }

        return mmtHold;
    }

    /**
     * Move all AgencyCredit/AgencyDebit transactions associated with PayrollRuns that have ERTaxDebit in a closed
     * state to a new MMT.
     *
     * @return the created MMT with the Agency transactions associated w/Payrolls that have ERTaxDebit in a closed state
     */
    public MoneyMovementTransaction splitOutReadyPayment() {
        if (getTaxPaymentStatus() != TaxPaymentStatus.OnHold || !isPendingMMT()) {
            throw new IllegalStateException("cannot split MMT (" + this + ") - TaxPaymentStatus -" + getTaxPaymentStatus() + " Payment Status-" + getStatus().toString());
        }
        MoneyMovementTransaction mmtReady = splitPayment(getCompletedERTaxDebits());

        // copy over any Agent or Enrollment hold (do NOT copy Company holds)
        for (TaxPaymentOnHoldReason onHoldReason : getActiveOnHoldReasons()) {
            if (!onHoldReason.getOnHoldReasonCd().equals(PaymentOnHoldReason.Company)) {
                mmtReady.addTaxPaymentOnHoldReason(onHoldReason.getOnHoldReasonCd());
            }
        }

        mmtReady.setTaxPaymentStatus(mmtReady.hasActiveOnHoldReasons() ? TaxPaymentStatus.OnHold : TaxPaymentStatus.ReadyToSend);
        return mmtReady;
    }

    private MoneyMovementTransaction splitPayment(Set<FinancialTransaction> pTaxDebitTransactions) {
        // move the agency transactions associated with the payroll run w/the non-completed ERTaxDebit to the new MMT
        DomainEntitySet<FinancialTransaction> agencyTransactions = new DomainEntitySet<FinancialTransaction>();

        Criterion agencyTxnsCriteria =
                FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit, TransactionTypeCode.AgencyTaxDebit)
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created))
                                    .And(FinancialTransaction.Law().PaymentTemplate().equalTo(getPaymentTemplate()))
                                    .And(FinancialTransaction.MoneyMovementTransaction().equalTo(this));
        for (FinancialTransaction erTaxDebit : pTaxDebitTransactions) {
            agencyTransactions.addAll(erTaxDebit.getPayrollRun().getFinancialTransactionCollection().find(agencyTxnsCriteria));
        }

        return splitOutAgencyTransactions(agencyTransactions);
    }

    private MoneyMovementTransaction splitOutAgencyTransactions(DomainEntitySet<FinancialTransaction> agencyTransactions) {
        if (!isPendingMMT()) {
            throw new IllegalStateException("cannot split MMT (" + this + ") - Payment Status -" + getStatus());
        }

        if (agencyTransactions.size() == 0) {
            throw new IllegalStateException("cannot split MMT (" + this + ") - no agency transactions specified");
        }

        MoneyMovementTransaction mmt = new MoneyMovementTransaction();
        mmt.setCompany(getCompany());
        mmt.setMoneyMovementPaymentMethod(null);
        //mmt.setOriginalTransaction(this);
        mmt.setTaxPaymentStatus(getTaxPaymentStatus());
        mmt.setAgencyTaxpayerId(getAgencyTaxpayerId());
        mmt.setDepositFrequencyFk(getDepositFrequencyFk());
        mmt.setDueDate(getDueDate());
        mmt.setInitiationDate(getInitiationDate());
        mmt.setTransactionNumber(getNextTransactionNumber());
        mmt.setManualPaymentStatus(getManualPaymentStatus());
        mmt.setPaymentFrequency(getPaymentFrequency());
        mmt.setPaymentPeriodBegin(getPaymentPeriodBegin());
        mmt.setPaymentPeriodEnd(getPaymentPeriodEnd());
        mmt.setPaymentTemplate(getPaymentTemplate());
        mmt.setStatus(PaymentStatus.Created);
        mmt.setMoneyMovementPaymentMethod(getMoneyMovementPaymentMethod());
        mmt = Application.save(mmt);

        for (FinancialTransaction payrollAgencyTxn : agencyTransactions) {
            if (!payrollAgencyTxn.getMoneyMovementTransaction().equals(this)) {
                throw new RuntimeException("Can't split from different MMT");
            }
            if (payrollAgencyTxn.getTransactionType().getTransactionTypeCd().notIn(TransactionTypeCode.AgencyTaxCredit, TransactionTypeCode.AgencyTaxDebit)) {
                throw new RuntimeException("Illegal FT type: " + payrollAgencyTxn.getTransactionType().getTransactionTypeCd());
            }
        }
        moveAgencyFinancialTransactions(agencyTransactions, mmt);

        mmt.setMoneyMovementPaymentMethod(getMoneyMovementPaymentMethod());
        mmt.recalculatePaymentMethod();
        return mmt;
    }

    //if toMMT is null, will add using normal logic
    public static void moveAgencyFinancialTransactions(DomainEntitySet<FinancialTransaction> pFinancialTransactions, MoneyMovementTransaction toMMT) {
        // remove negative transactions first, so we do not create negative payments
        for (FinancialTransaction fT : pFinancialTransactions) {
            if (TransactionType.subtractsFromPayment(fT.getTransactionType().getTransactionTypeCd())) {
                MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(fT);
            }
        }
        // then remove the positive transactions
        for (FinancialTransaction fT : pFinancialTransactions) {
            if (TransactionType.addsToPayment(fT.getTransactionType().getTransactionTypeCd())) {
                MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(fT);
            }
        }

        // add positive transactions first, so we do not create negative payments
        for (FinancialTransaction fT : pFinancialTransactions) {
            if (TransactionType.addsToPayment(fT.getTransactionType().getTransactionTypeCd())) {
                if (toMMT != null) {
                    toMMT.addAgencyFinancialTransaction(fT);
                } else {
                    MoneyMovementTransaction.addFinancialTransactionToTaxPaymentMMT(fT);
                }
            }
        }
        // then add the negative transactions
        for (FinancialTransaction fT : pFinancialTransactions) {
            if (TransactionType.subtractsFromPayment(fT.getTransactionType().getTransactionTypeCd())) {
                if (toMMT != null) {
                    toMMT.addAgencyFinancialTransaction(fT);
                } else {
                    MoneyMovementTransaction.addFinancialTransactionToTaxPaymentMMT(fT);
                }
            }
        }
    }

    public TaxPaymentOnHoldReason expireTaxPaymentOnHoldReason(PaymentOnHoldReason pPaymentOnHoldReason) {
        TaxPaymentOnHoldReason existingOnHoldReason = getActiveOnHoldReason(pPaymentOnHoldReason);
        if (existingOnHoldReason == null) {
            return null;
        }

        existingOnHoldReason.setExpirationDate(PSPDate.getPSPTime());

        if (!hasActiveOnHoldReasons() && getTaxPaymentStatus() == TaxPaymentStatus.OnHold) {
            //todo_rhn probably should create event when a TaxPayment goes off hold
            updateTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
        }

        return existingOnHoldReason;
    }

    public SpcfDecimal getTaxPaymentAmountCollected() {
        // this is used so we don't make extra calls to the db
        Map<String, Boolean> payrollOffloadedMap = new HashMap<String, Boolean>();
        SpcfDecimal suiTaxRecievableAmount = SpcfMoney.ZERO;

        Map<String, SpcfDecimal> offloadedAmountMap = new HashMap<String, SpcfDecimal>();
        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection()) {
            if (financialTransaction.getPayrollRun() != null) {
                String payrollRunId = financialTransaction.getPayrollRun().getId().toString();

                if (!offloadedAmountMap.containsKey(payrollRunId)) {
                    offloadedAmountMap.put(payrollRunId, SpcfMoney.ZERO);
                }

                if (!payrollOffloadedMap.containsKey(payrollRunId)) {
                    payrollOffloadedMap.put(payrollRunId, financialTransaction.getPayrollRun().hasTaxImpoundOffloaded(false));
                }

                FinancialTransaction relatableTransaction = financialTransaction.getRelatableTransaction();
                if (relatableTransaction != null && relatableTransaction.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerSUITaxReceivable)) {
                    suiTaxRecievableAmount = suiTaxRecievableAmount.add(relatableTransaction.getFinancialTransactionAmount());
                }

                if (payrollOffloadedMap.get(payrollRunId)) {
                    if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd()) && payrollOffloadedMap.get(payrollRunId)) {
                        offloadedAmountMap.put(payrollRunId, offloadedAmountMap.get(payrollRunId).add(financialTransaction.getFinancialTransactionAmount()));
                    } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd()) && !financialTransaction.getLaw().isCOBRA()) {
                        offloadedAmountMap.put(payrollRunId, offloadedAmountMap.get(payrollRunId).subtract(financialTransaction.getFinancialTransactionAmount()));
                    }
                }
            }
        }

        SpcfDecimal totalAmount = SpcfMoney.ZERO;
        for (SpcfDecimal spcfDecimal : offloadedAmountMap.values()) {
            totalAmount = totalAmount.add(spcfDecimal);
        }
        totalAmount = totalAmount.add(suiTaxRecievableAmount);

        if (getMoneyMovementTransactionAmount().isGreaterThanEqualTo(SpcfMoney.ZERO) && totalAmount.isGreaterThan(getMoneyMovementTransactionAmount())) {
            return getMoneyMovementTransactionAmount();
        } else if (totalAmount.isLessThan(SpcfMoney.ZERO)) {
            return SpcfMoney.ZERO;
        }
        return new SpcfMoney(totalAmount);
    }

    public static DomainEntitySet<MoneyMovementTransaction> findAchDirectDepositCreatedMoneyMovementTransactions(Company company) {
        return CandidateMMTCache.get(company);
    }

    public static DomainEntitySet<MoneyMovementTransaction> getMMTsInMemory(Company pCompany, PaymentTemplate pPaymentTemplate) {
        DomainEntitySet<MoneyMovementTransaction> mmtsInMemory = Application.getSessionCache().getNonHibernateObject(MMTS_IN_MEMORY_CACHE_KEY + ":" + pCompany.getSourceCompanyId().trim() + pPaymentTemplate.getPaymentTemplateCd().trim());

        if (mmtsInMemory == null) {
            mmtsInMemory = new DomainEntitySet<MoneyMovementTransaction>();
            Application.getSessionCache().addNonHibernateObject(MMTS_IN_MEMORY_CACHE_KEY + ":" + pCompany.getId(), mmtsInMemory);
        }

        return mmtsInMemory;
    }

    public static void addMMTsToCandidateMMTCache(DomainEntitySet<MoneyMovementTransaction> pMoneyMoneyMovementTransactions) {
        for (MoneyMovementTransaction moneyMoneyMovementTransaction : pMoneyMoneyMovementTransactions) {
            CandidateMMTCache.add(moneyMoneyMovementTransaction);
        }

    }

    public static class CandidateMMTCache {
        public static void add(MoneyMovementTransaction pMMT) {
            // Guarantee it is cached
            if (!isCached(pMMT.getCompany())) {
                get(pMMT.getCompany(), pMMT.getInitiationDate(), PaymentMethod.ACHDirectDeposit);
            }

            // Add to cache
            Application.getSessionCache().addEntity(MoneyMovementTransaction.class, getKey(pMMT.getCompany()), pMMT);
        }

        private static boolean isCached(Company pCompany) {
            return Application.getSessionCache().isEntityCollectionCached(MoneyMovementTransaction.class, getKey(pCompany));
        }

        public static void remove(MoneyMovementTransaction pMMT) {
            // Guarantee it is cached
            if (!isCached(pMMT.getCompany())) {
                get(pMMT.getCompany(), pMMT.getInitiationDate(), PaymentMethod.ACHDirectDeposit);
            }

            // Remove from cache
            Application.getSessionCache().removeEntity(MoneyMovementTransaction.class, getKey(pMMT.getCompany()), pMMT);
        }

        public static DomainEntitySet<MoneyMovementTransaction> get(Company pCompany, SpcfCalendar initiationDate, PaymentMethod paymentMethod) {
            return findMoneyMovementTransactions(pCompany, initiationDate, paymentMethod);
        }

        private static String getKey(Company pCompany) {
            return "candidateMoneyMovementTransactions:" + pCompany.getId() + ":" + PaymentStatus.Created.toString() + "or" + PaymentStatus.OnHold.toString();
        }

        /**
         * Function to get the MoneyMovementTransactions based on the given FinancialTransaction
         *
         * @return DomainEntitySet<MoneyMovementTransaction>
         */
        public static DomainEntitySet<MoneyMovementTransaction> findMoneyMovementTransactions(Company pCompany,
                                                                                              SpcfCalendar initiationDate,
                                                                                              PaymentMethod paymentMethod) {
            String cacheKey = getKey(pCompany);
            DomainEntitySet<MoneyMovementTransaction> candidateMoneyMovementTransactions = Application.getSessionCache().getEntityCollection(MoneyMovementTransaction.class, cacheKey);
            if (candidateMoneyMovementTransactions == null) {
                candidateMoneyMovementTransactions = new DomainEntitySet<MoneyMovementTransaction>();
                Application.getSessionCache().addEntityCollection(MoneyMovementTransaction.class, cacheKey, candidateMoneyMovementTransactions);
            }

            // Have we gone to the database for the passed initiationDate and paymentMethod? If not, go
            String queryKey = pCompany.getId() + ":ReadInitiationDate:" + initiationDate.toString() + ":PaymentMethod:" + paymentMethod.toString();
            if (Application.getSessionCache().getNonHibernateObject(queryKey) == null) {
                Application.getSessionCache().addNonHibernateObject(queryKey, null);
                Criterion<MoneyMovementTransaction> where = MoneyMovementTransaction.Company().equalTo(pCompany)
                                                                                    .And(MoneyMovementTransaction.InitiationDate().equalTo(initiationDate))
                                                                                    .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold)
                                                                                                                 .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(paymentMethod)));
                for (MoneyMovementTransaction mmt : Application.find(MoneyMovementTransaction.class, where)) {
                    Application.getSessionCache().addEntity(MoneyMovementTransaction.class, cacheKey, mmt);
                }

                candidateMoneyMovementTransactions = Application.getSessionCache().getEntityCollection(MoneyMovementTransaction.class, cacheKey);
            }

            // Only return MMTs in cache that match passed paymentMethod and initiationDate
            DomainEntitySet<MoneyMovementTransaction> matchingMMTs = new DomainEntitySet<MoneyMovementTransaction>();
            for (MoneyMovementTransaction mmt : candidateMoneyMovementTransactions) {
                if (mmt.getMoneyMovementPaymentMethod() == paymentMethod &&
                        mmt.getInitiationDate().compareTo(initiationDate) == 0) {
                    matchingMMTs.add(mmt);
                }
            }
            return matchingMMTs;
        }

        public static DomainEntitySet<MoneyMovementTransaction> get(Company company) {
            String cacheKey = getKey(company);
            DomainEntitySet<MoneyMovementTransaction> candidateMoneyMovementTransactions = Application.getSessionCache().getEntityCollection(MoneyMovementTransaction.class, cacheKey);
            if (candidateMoneyMovementTransactions == null) {
                Criterion<MoneyMovementTransaction> where = MoneyMovementTransaction.Company().equalTo(company)
                                                                                    .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold)
                                                                                                                 .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDirectDeposit)));
                candidateMoneyMovementTransactions = Application.find(MoneyMovementTransaction.class, where);
                Application.getSessionCache().addEntityCollection(MoneyMovementTransaction.class, cacheKey, candidateMoneyMovementTransactions);
            }
            return Application.getSessionCache().getEntityCollection(MoneyMovementTransaction.class, cacheKey);
        }
    }

    /**
     * Add a money movement transaction for agency tax transactions
     *
     * @param pFinancialTransaction
     * @param pPaymentPeriod
     * @param pPaymentTemplate
     * @param pDepositFrequencyCode
     * @return
     */
    public static MoneyMovementTransaction createTaxMoneyMovementTransaction(FinancialTransaction pFinancialTransaction, IPaymentPeriod pPaymentPeriod, PaymentTemplate pPaymentTemplate, DepositFrequencyCode pDepositFrequencyCode) {

        MoneyMovementTransaction mmTxn = new MoneyMovementTransaction();
        mmTxn.setCompany(pFinancialTransaction.getCompany());
        mmTxn.setMoneyMovementPaymentMethod(null);
        mmTxn.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
        mmTxn.setStatus(PaymentStatus.Created);

        PaymentTemplateFrequency paymentTemplateFrequency = pPaymentTemplate.getPaymentTemplateFrequency(pDepositFrequencyCode.toString());
        mmTxn.setPaymentFrequency(paymentTemplateFrequency);
        mmTxn.setDueDate(CalendarUtils.convertToSpcfCalendar(pPaymentPeriod.getDueDate()));
        mmTxn.setPaymentTemplate(pPaymentTemplate);
        mmTxn.setPaymentPeriodBegin(CalendarUtils.convertToSpcfCalendar(pPaymentPeriod.getFromAccrualDate()));
        mmTxn.setPaymentPeriodEnd(CalendarUtils.convertToSpcfCalendar(pPaymentPeriod.getToAccrualDate()));
        mmTxn.setTransactionNumber(getNextTransactionNumber());

        PaymentTemplate paymentTemplate = pFinancialTransaction.getLaw().getPaymentTemplate();

        // save the current AgencyTaxpayerId on the MMT... in case it changes later and we need to know what it was when this MMT was created
        CompanyAgencyPaymentTemplate capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pFinancialTransaction.getCompany(), paymentTemplate);
        mmTxn.setAgencyTaxpayerId(capt.getAgencyTaxpayerId());

        PaymentMethod paymentMethod = pFinancialTransaction.findPaymentMethod();
        SpcfCalendar initDate;
        if (pFinancialTransaction.getPayrollRun() != null) {
            SpcfCalendar debitInitiationDate = pFinancialTransaction.getPayrollRun().getEmployerTaxDebitTransaction() != null ? pFinancialTransaction.getPayrollRun().getEmployerTaxDebitTransaction().getInitiationDate() : null;
            initDate = MoneyMovementTransaction.getPaymentInitDate(CalendarUtils.convertToSpcfCalendar(pPaymentPeriod.getDueDate()), paymentMethod, pDepositFrequencyCode, pPaymentTemplate, pFinancialTransaction.getPayrollRun().isBackDated(), debitInitiationDate, pFinancialTransaction.isEFTPS_ACHPaymentTransaction());
        } else {
            initDate = MoneyMovementTransaction.getPaymentInitDate(CalendarUtils.convertToSpcfCalendar(pPaymentPeriod.getDueDate()), paymentMethod, pDepositFrequencyCode, pPaymentTemplate, false, null, false);
        }

        mmTxn.updateTaxInitiationDate(initDate);

        // The amount of the new MMT is determined by the Transaction Type
        // Agency Tax Credit and Agency Direct Credit transactions will set a positive amount
        // Agency tax Debit transactions will set a negative amount
        // Agency Tax Overpayment transactions will set the MMT amount to zero - these transactions cannot affect the MMT amount

        switch (pFinancialTransaction.getTransactionType().getTransactionTypeCd()) {
            case AgencyTaxCredit:
            case AgencyDirectCredit:
                mmTxn.setMoneyMovementTransactionAmount(pFinancialTransaction.getFinancialTransactionAmount());
                break;
            case AgencyTaxDebit:
            case AgencyDirectDebit:
            case AgencyTaxOverpaymentApplied:
                mmTxn.setMoneyMovementTransactionAmount(new SpcfMoney(pFinancialTransaction.getFinancialTransactionAmount().negate()));
                break;
            case AgencyTaxOverpayment:
            case AgencyDirectOverpayment:
                mmTxn.setMoneyMovementTransactionAmount(SpcfMoney.ZERO);
                break;
            default:
                mmTxn.setMoneyMovementTransactionAmount(pFinancialTransaction.getFinancialTransactionAmount());
                break;
        }

        //Bi-directional, so set the association both ways
        pFinancialTransaction.setMoneyMovementTransaction(mmTxn);
        mmTxn.addFinancialTransaction(pFinancialTransaction);

        //Offload batch is always null for Agency Tax Credits
        mmTxn.setOffloadBatch(null);
        mmTxn.setOriginalInitiationDate(mmTxn.getInitiationDate());

        mmTxn.setMoneyMovementPaymentMethod(paymentMethod);
        //Save the money movement transaction so that fin txn and entry detail are working with the saved object
        mmTxn = Application.save(mmTxn);

        if (!(mmTxn.getMoneyMovementTransactionAmount().compareTo(mmTxn.getMMTBalance()) == 0)) {
            throw new RuntimeException("Invalid Money Movement Transaction Amount MMT Amount:" +
                                               mmTxn.getMoneyMovementTransactionAmount() + " FT Total:" + mmTxn.getMMTBalance() + " MMT:" + mmTxn.getId());
        }

        SpcfCalendar originalInitDate = mmTxn.getDueDate().copy();
        CalendarUtils.addBusinessDays(originalInitDate, -1);

        if (originalInitDate.compareTo(initDate) != 0) {
            //Add history record
            String userId = Application.getCurrentPrincipal() == null ? "" : Application.getCurrentPrincipal().getId();
            PropertyAudit.createPropertyAudit(MoneyMovementTransaction.class.getSimpleName(), mmTxn.getCompany(), MoneyMovementTransaction.InitiationDate().getPropertyName(), CalendarUtils.toAuditDateFormat(originalInitDate), CalendarUtils.toAuditDateFormat(initDate), userId, mmTxn.getId().toString());
        }

        mmTxn.recalculatePaymentMethod();

        // If the MMT Amount = ZERO, we need to set the status to Ignore if the agency does not require a zero payment. This will cause this
        // payment not to be offloaded
        mmTxn.recalculateTaxPaymentStatus();
        CandidateMMTCache.add(mmTxn);

        return mmTxn;
    }

    public static MoneyMovementTransaction createHPDEMoneyMovementTransaction(Company pCompany, PriorPaymentSubmission pps, PaymentTemplate paymentTemplate, SpcfCalendar periodEndDate, SpcfCalendar paymentDate, SpcfMoney amount, boolean isRefund, QbdtTransactionInfo qbdtTransactionInfo, boolean includeInCalculations) {
        MoneyMovementTransaction mmTxn = new MoneyMovementTransaction();

        mmTxn.setCompany(pCompany);
        //Save the money movement transaction so that fin txn and entry detail are working with the saved object
        Application.save(mmTxn);

        mmTxn.setPaymentTemplate(paymentTemplate);

        mmTxn.setPaymentPeriodBegin(CalendarUtils.getFirstDayOfQuarter(periodEndDate));
        mmTxn.setPaymentPeriodEnd(periodEndDate);

        mmTxn.setInitiationDate(paymentDate);
        mmTxn.setDueDate(paymentDate);

        PaymentMethod paymentMethod;
        if (includeInCalculations) {
            paymentMethod = isRefund ? PaymentMethod.HPDERefund : PaymentMethod.HPDE;
        } else {
            paymentMethod = isRefund ? PaymentMethod.PostBalfHPDERefund : PaymentMethod.PostBalfHPDE;
        }

        mmTxn.setMoneyMovementPaymentMethod(paymentMethod);
        mmTxn.setMoneyMovementTransactionAmount(amount);
        mmTxn.setTransactionNumber(getNextTransactionNumber());

        mmTxn.setTaxPaymentStatus(TaxPaymentStatus.None);
        mmTxn.setStatus(PaymentStatus.Executed);
        mmTxn.setManualPaymentStatus(ManualPaymentStatus.PriorPayment);

        //Offload batch is always null for HPDE
        mmTxn.setOffloadBatch(null);
        mmTxn.setOriginalInitiationDate(mmTxn.getInitiationDate());

        if (qbdtTransactionInfo != null) {
            qbdtTransactionInfo.setMoneyMovementTransaction(mmTxn);
            qbdtTransactionInfo.setPriorPaymentSubmission(pps);
            pps.addQbdtTransactionInfo(qbdtTransactionInfo);
        }

        CandidateMMTCache.add(mmTxn);

        return mmTxn;
    }

    @SuppressWarnings("unchecked")
    public static MoneyMovementTransaction findPriorPayment(Company company, String sourceId, PaymentTemplate paymentTemplate) {
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class,
                                                                          MoneyMovementTransaction.Company().equalTo(company)
                                                                                                  .And(MoneyMovementTransaction.QbdtTransactionInfo().PriorPaymentSubmission().SourceId().equalTo(sourceId))
                                                                                                  .And(MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate)));
        if (mmts.size() > 1) {
            StringBuilder mmtsList = new StringBuilder();

            for (MoneyMovementTransaction mmt : mmts) {
                mmtsList.append(mmt.getId()).append(", ");
            }

            throw new RuntimeException("Multiple MMTs for company/source Id.  MMTs List:" + mmtsList);
        } else if (mmts.size() == 1) {
            return mmts.get(0);
        } else {
            return null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public MoneyMovementTransaction() {
        super();
    }

    /**
     * Creates EntryDetailRecords for the given MMT and associates them (bidirectionally) with the MMT.  The
     * EntryDetailRecords are saved.  The MMT is not.
     *
     * @return the created EntryDetailRecords in an SpcfList
     */
    public DomainEntitySet<EntryDetailRecord> createEntryDetailRecords() {
        // we need per-account net amounts across FTs on this MMT...
        // (positive for net debit, negative for net credit to the account)
        HashMap<BankAccount, SpcfDecimal> acctTotals = new HashMap<BankAccount, SpcfDecimal>();
        HashMap<BankAccount, FinancialTransaction> acctDebitFTs = new HashMap<BankAccount, FinancialTransaction>();
        HashMap<BankAccount, FinancialTransaction> acctCreditFTs = new HashMap<BankAccount, FinancialTransaction>();
        Criterion<FinancialTransaction> where =
                FinancialTransaction.TransactionType().TransactionTypeCd().notIn(TransactionTypeCode.EmployerTaxCreditApplied,
                                                                                 TransactionTypeCode.EmployerTaxOverpaymentApplied);
        DomainEntitySet<FinancialTransaction> ftList = getFinancialTransactionCollection().find(where);
        FinancialTransaction firstFt = null;
        for (FinancialTransaction ft : ftList) {
            // Employer Applied Transactions should not create Entry Detail Records
            // Also the bank accounts cannot be null - if they are null  this ft cannot be part of the entry detail record
            if (!ft.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerTaxCreditApplied) &&
                    !ft.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerTaxOverpaymentApplied) &&
                    ft.getDebitBankAccount() != null && ft.getCreditBankAccount() != null) {
                SpcfDecimal ftAmount = ft.getFinancialTransactionAmount();
                firstFt = ft;

                acctDebitFTs.put(ft.getDebitBankAccount(), ft);
                acctCreditFTs.put(ft.getCreditBankAccount(), ft);

                // add this amount to the debit-total for the account being debited
                SpcfDecimal sum = acctTotals.get(ft.getDebitBankAccount());
                if (sum == null) {
                    sum = ftAmount;
                } else {
                    sum = sum.add(ftAmount);
                }
                acctTotals.put(ft.getDebitBankAccount(), sum);

                // subtract this amount from the debit-total for the account being credited
                sum = acctTotals.get(ft.getCreditBankAccount());
                if (sum == null) {
                    sum = ftAmount.negate();
                } else {
                    sum = sum.subtract(ftAmount);
                }
                acctTotals.put(ft.getCreditBankAccount(), sum);
            }
        }

        // now create EntryDetailRecords for each account with a non-zero total
        DomainEntitySet<EntryDetailRecord> newEDRs = new DomainEntitySet<EntryDetailRecord>();
        for (Map.Entry<BankAccount, SpcfDecimal> entry : acctTotals.entrySet()) {
            BankAccount account = entry.getKey();
            SpcfDecimal acctTotal = entry.getValue();

            CreditDebitCode direction;
            boolean createZeroEntry = false;


            int sign = acctTotal.compareTo(SpcfDecimal.createInstance(0));
            if (sign > 0) {
                // positive total means a net debit
                direction = CreditDebitCode.Debit;
            } else if (sign < 0) {
                // negative total means a net credit
                direction = CreditDebitCode.Credit;
            } else {

                if (firstFt.getCreditBankAccountType().equals(BankAccountOwnerType.TaxAgency)) {
                    direction = CreditDebitCode.Credit;
                    createZeroEntry = true;
                } else {
                    // zero total means no net impact to this bank account, so don't create a record for it
                    continue;
                }
            }

            // if this account is an INTU account, we'll need the IntuitBankAccount entity for it
            // (if it's not, this will be null, which is what we want)
            IntuitBankAccount iba = IntuitBankAccount.findIntuitBankAccount(account);

            SpcfDecimal remainingAmount = acctTotal.abs();
            while (remainingAmount.compareTo(EntryDetailRecord.SPCF_MONEY_ZERO) > 0 || createZeroEntry) {
                // the amount for this record is the maximum of the remaining amount and the NACHA-imposed limit
                SpcfDecimal recordAmount = remainingAmount; // amount is unsigned on the record
                if (recordAmount.compareTo(EntryDetailRecord.NACHA_MAX_ENTRY_DETAIL_AMOUNT) > 0) {
                    recordAmount = EntryDetailRecord.NACHA_MAX_ENTRY_DETAIL_AMOUNT;
                }

                // create the entity
                EntryDetailRecord edr = new EntryDetailRecord();
                edr.setCreditDebitIndicator(direction);
                edr.setAmount(new SpcfMoney(recordAmount));
                edr.setCompany(getCompany());
                edr.setIntuitBankAccount(iba);
                edr.setInitiationDate(getInitiationDate());
                edr.setTraceNumber(null);
                edr.setLegalName(getCompany().getLegalName());
                edr.setNACHABatchType(firstFt.getTransactionType().getNACHABatchType());

                if (firstFt.getCreditBankAccountType().equals(BankAccountOwnerType.TaxAgency)) {
                    edr.setNACHAFileType(NACHAFileType.CCDPlus);
                    edr.setSettlementDate(getTaxEDRSettlementDate());
                } else {
                    edr.setSettlementDate(getEDRSettlementDate());
                    if (firstFt.getDebitBankAccountType().equals(BankAccountOwnerType.Employee) ||
                            firstFt.getCreditBankAccountType().equals(BankAccountOwnerType.Employee)) {
                        edr.setNACHAFileType(NACHAFileType.PPD);
                        if (firstFt.getPayeeBankAccount() != null) {
                            BankAccount ba = firstFt.getPayeeBankAccount().getBankAccount();
                            if (ba.getACHEntryClass().equals(EntryClassCode.CCD)) {
                                edr.setNACHAFileType(NACHAFileType.CCD);
                            }
                        }
                    } else if (firstFt.getDebitBankAccountType().equals(BankAccountOwnerType.Company) ||
                            firstFt.getCreditBankAccountType().equals(BankAccountOwnerType.Company)) {
                        edr.setNACHAFileType(NACHAFileType.CCD);
                    } else if (firstFt.getDebitBankAccountType().equals(BankAccountOwnerType.Intuit) &&
                            firstFt.getCreditBankAccountType().equals(BankAccountOwnerType.Intuit)) {
                        edr.setNACHAFileType(NACHAFileType.CCD);
                    }
                }

                edr.setMoneyMovementTransaction(this);

                //
                // Create (if appropriate) the EDR data last since it will also create the TXP record,
                // which needs the above data fields set in order to properly create the TXP record.
                //

                // if this account is NOT an INTU account, then we'll need to generate record data
                if (iba == null) {
                    FinancialTransaction ftWithThisAccount;

                    if (direction == CreditDebitCode.Debit) {
                        ftWithThisAccount = acctDebitFTs.get(account);
                    } else {
                        ftWithThisAccount = acctCreditFTs.get(account);
                    }

                    edr.createRecordData(ftWithThisAccount, direction);
                }

                edr = Application.save(edr);

                addEntryDetailRecord(edr);

                // add it to the return collection
                newEDRs.add(edr);

                // deduct this record's amount from the remaining amount
                remainingAmount = remainingAmount.subtract(recordAmount);
                createZeroEntry = false;
            }
        }

        return newEDRs;
    }

    /**
     * Get EDR Settlement Date for PPD & CCD
     * @return {@link SpcfCalendar}
     */
    public SpcfCalendar getEDRSettlementDate() {
        SpcfCalendar settlementDate = getFirstFTSettlementDate();
        return settlementDate;
    }

    /**
     * Get EDR Settlement Date for CCDPlus
     * @return {@link SpcfCalendar}
     */
    public SpcfCalendar getTaxEDRSettlementDate() {
        SpcfCalendar settlementDate = getFirstFTSettlementDate();
        if (settlementDate != null) {
            return settlementDate;
        }
        //If 1st FT's settlement date is not yet populated then generate settlement date from MMT's initiation date.
        settlementDate = generatePaymentSettlementDate();
        return settlementDate;
    }

    private SpcfCalendar getFirstFTSettlementDate() {
        FinancialTransaction firstFT = this.getFirstFinancialTransaction();
        if (firstFT != null) {
            return firstFT.getSettlementDate();
        }
        return null;
    }

    private void logEmptySettlementDate(FinancialTransaction firstFT) {

    }

    /**
     * Creates EntryDetailRecords for the given MMT and associates them (bidirectionally) with the MMT.  The
     * EntryDetailRecords are saved.  The MMT is not.
     *
     * @return the created EntryDetailRecords in an SpcfList
     */
    private DomainEntitySet<EntryDetailRecord> createTaxPaymentEntryDetailRecords() {
        SpcfDecimal accountTotals = SpcfMoney.ZERO;
        SpcfDecimal dbtTotals = SpcfMoney.ZERO;
        SpcfDecimal cdtTotals = SpcfMoney.ZERO;

        // now create EntryDetailRecords for each account
        DomainEntitySet<EntryDetailRecord> newEDRs = new DomainEntitySet<EntryDetailRecord>();

        BankAccount dbtAccount = null;
        BankAccount cdtAccount = null;

        TransactionState cancelledState = Application.findById(TransactionState.class, TransactionStateCode.Cancelled);
        // Employer Applied, EmployerSUITaxReceivable, EmployerSUITaxPayable Transactions should not create Entry Detail Records
        Criterion<FinancialTransaction> where =
                FinancialTransaction.TransactionType().TransactionTypeCd().notIn(TransactionTypeCode.EmployerTaxCreditApplied, TransactionTypeCode.EmployerTaxOverpaymentApplied,
                                                                                 TransactionTypeCode.EmployerSUITaxReceivable, TransactionTypeCode.EmployerSUITaxPayable)
                                    .And(FinancialTransaction.CurrentTransactionState().notEqualTo(cancelledState));
        DomainEntitySet<FinancialTransaction> ftList = getFinancialTransactionCollection().find(where);

        FinancialTransaction dbtFt = null;
        FinancialTransaction cdtFt = null;
        for (FinancialTransaction ft : ftList) {
            // Also the bank accounts cannot be null - if they are null  this ft cannot be part of the entry detail record
            SpcfDecimal ftAmount = ft.getFinancialTransactionAmount();
            if (TransactionType.addsToPayment(ft.getTransactionType().getTransactionTypeCd())) {
                dbtTotals = dbtTotals.add(ftAmount);
                accountTotals = accountTotals.add(ftAmount);
                if (ft.getDebitBankAccount() != null) {
                    dbtAccount = ft.getDebitBankAccount();
                    dbtFt = ft;
                }
            } else {
                cdtTotals = cdtTotals.add(ftAmount);
                accountTotals = accountTotals.subtract(ftAmount);
                if (ft.getCreditBankAccount() != null) {
                    cdtAccount = ft.getCreditBankAccount();
                    cdtFt = ft;
                }
            }
        }

        if (accountTotals.compareTo(EntryDetailRecord.SPCF_MONEY_ZERO) >= 0 && ftList.size() > 0) {
            if (accountTotals.compareTo(EntryDetailRecord.NACHA_MAX_ENTRY_DETAIL_AMOUNT) > 0) {
                accountTotals = EntryDetailRecord.NACHA_MAX_ENTRY_DETAIL_AMOUNT;
            }
            newEDRs.add(EntryDetailRecord.createEntryDetailRecord(CreditDebitCode.Credit, accountTotals, IntuitBankAccount.findIntuitBankAccount(cdtAccount), this, dbtFt));
            newEDRs.add(EntryDetailRecord.createEntryDetailRecord(CreditDebitCode.Debit, accountTotals, IntuitBankAccount.findIntuitBankAccount(dbtAccount), this, cdtFt));
        }

        this.getEntryDetailRecordCollection().addAll(newEDRs);
        return newEDRs;
    }

    public boolean amountResolvesMMT(SpcfMoney pNewAmount) {
        SpcfMoney originalMMTAmount = getMoneyMovementTransactionAmount();
        SpcfMoney pendingAmountForMMT = new SpcfMoney("0.00");
        for (FinancialTransaction currFT : getFinancialTransactionCollection()) {
            TransactionSummary currSummary = currFT.summarizeRelatedTransactions();
            pendingAmountForMMT = new SpcfMoney(pendingAmountForMMT.add(currSummary.amtPending));
            pendingAmountForMMT = new SpcfMoney(pendingAmountForMMT.add(currSummary.amtCollected));
        }

        SpcfMoney newAmountForMMT = new SpcfMoney(pendingAmountForMMT.add(pNewAmount));

        if (newAmountForMMT.compareTo(originalMMTAmount) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public void advanceInitiationDate() {
        SpcfCalendar initiationDate = getInitiationDate().toLocal();

        if (PSPDate.getPSPTime().after(initiationDate)) {
            initiationDate = PSPDate.getPSPTime();
            CalendarUtils.clearTime(initiationDate);
        }

        CalendarUtils.addBusinessDays(initiationDate, 1);

        if (isTaxPayment()) {
            updateTaxInitiationDate(initiationDate);
        } else {
            updateInitiationDate(initiationDate);
        }
    }

    public void updateInitiationDate(SpcfCalendar pNewInitiationDate) {
        setInitiationDate(pNewInitiationDate);
        Application.save(this);
        //Add one business day
        SpcfCalendar settlementDate = calculateSettlementDate(getFirstFinancialTransaction());
        //Update the Initiation Date for the EntryDetailRecords associated with the MMT
        DomainEntitySet<EntryDetailRecord> entryDetailRecords = getEntryDetailRecordCollection();
        for (EntryDetailRecord entryDetailRecord : entryDetailRecords) {
            entryDetailRecord.setInitiationDate(pNewInitiationDate);
            entryDetailRecord.setSettlementDate(settlementDate);
            Application.save(entryDetailRecord);
        }

        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection()) {
            financialTransaction.setSettlementDate(settlementDate);
            Application.save(financialTransaction);
        }
    }

    public SpcfCalendar calculateSettlementDate(FinancialTransaction pFinancialTransaction) {
        switch (pFinancialTransaction.getTransactionType().getTransactionTypeCd()) {
            case EmployeeDdCredit:
                SpcfCalendar newEECreditSettlementDate;
                if (pFinancialTransaction.getPayrollRun() != null &&
                        !pFinancialTransaction.getPayrollRun().hasImpoundOffloaded() &&
                        pFinancialTransaction.getPayrollRun().getImpoundSettlementDate() != null) {
                    newEECreditSettlementDate = pFinancialTransaction.getPayrollRun().getImpoundSettlementDate().copy();
                    int fundingModelDays = pFinancialTransaction.getCompany().getFundingModel().getNumberOfFundingDays();
                    CalendarUtils.addBusinessDays(newEECreditSettlementDate, fundingModelDays - 1);
                } else {
                    newEECreditSettlementDate = getInitiationDate().copy();
                    CalendarUtils.addBusinessDays(newEECreditSettlementDate, 1);
                }
                return newEECreditSettlementDate;
            default:
                SpcfCalendar newSettlementDate = getInitiationDate().copy();
                CalendarUtils.addBusinessDays(newSettlementDate, 1);
                return newSettlementDate;
        }
    }

    public Agency getAgencyForMoneyMovementTransaction() {
        //All financial transactions for one mmt belong to the same agency, so just get the first one.  Some may not have Law, though (like ERDirectDebit)
        DomainEntitySet<FinancialTransaction> financialTransactions = getFinancialTransactionCollection().find(FinancialTransaction.Law().isNotNull());
        if (financialTransactions.size() > 0) {
            return financialTransactions.get(0).getLaw().getPaymentTemplate().getAgency();
        }
        return null;
    }


    public static SpcfCalendar getNextInitiationDate(PaymentMethod pPaymentMethod) {
        SystemParameter.Code systemParameter = SystemParameter.Code.EFTPS_PAYMENT_CUTOFF;
        if (pPaymentMethod != null) {
            switch (pPaymentMethod) {
                case EFTPS:
                case EFTPSDirectDebit:
                    systemParameter = SystemParameter.Code.EFTPS_PAYMENT_CUTOFF;
                    break;
                case ACHCredit:
                    systemParameter = SystemParameter.Code.ACH_TAX_PAYMENT_CUTOFF;
                    break;
                case ACHDebit:
                    systemParameter = SystemParameter.Code.ACH_DEBIT_TAX_PAYMENT_CUTOFF;
                    break;
                case CheckPayment:
                case SuperCheck:
                    systemParameter = SystemParameter.Code.CHECK_PAYMENT_CUTOFF;
                    break;
                case EDI:
                    systemParameter = SystemParameter.Code.EDI_TAX_PAYMENT_CUTOFF;
                    break;
                default:
                    systemParameter = SystemParameter.Code.EFTPS_PAYMENT_CUTOFF;
                    break;
            }
        }

        SpcfCalendar now = PSPDate.getPSPTime();
        String cutoffTimeParam = SystemParameter.findStringValue(systemParameter);
        String[] hmsParts = cutoffTimeParam.split(":");
        int cutoffHrs = Integer.parseInt(hmsParts[0]);
        int cutoffMin = Integer.parseInt(hmsParts[1]);
        int cutoffSec = Integer.parseInt(hmsParts[2]);

        SpcfCalendar cutoffTime = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        cutoffTime.setValues(now.getYear(), now.getMonth(), now.getDay(), cutoffHrs, cutoffMin, cutoffSec, 0);

        //
        // Check to see if we're before or after the cutoff time to send  payments. If before, make sure it's
        // not a weekend or holiday. Always bump by business days since we're dealing with a bank.
        //
        if (now.before(cutoffTime)) {
            while (CalendarUtils.isWeekendOrHoliday(now)) {
                CalendarUtils.addBusinessDays(now, 1);
            }
        } else {
            CalendarUtils.addBusinessDays(now, 1);
        }

        now.setValues(now.getYear(), now.getMonth(), now.getDay(), 0, 0, 0, 0);

        return now;
    }

    public static SpcfCalendar getNextEFTPSInitiationDate() {
        return getNextInitiationDate(PaymentMethod.EFTPS);
    }

    public void resolveMMTAndRelatedMMTs() {
        DomainEntitySet<FinancialTransaction> modifiableFinTxns = getRelatedTransactionsToMMT();

        //Resolve all the returns
        for (FinancialTransaction financialTxn : modifiableFinTxns) {
            financialTxn.resolveTransactionReturns();
        }
    }

    public void unresolveMMTAndRelatedTransactionReturns() {
        DomainEntitySet<FinancialTransaction> modifiableFinTxns = getRelatedTransactionsToMMT();

        //UN-Resolve all the returns
        for (FinancialTransaction financialTxn : modifiableFinTxns) {
            financialTxn.unResolveTransactionReturns();
        }
    }

    public DomainEntitySet<FinancialTransaction> getRelatedTransactionsToMMT() {
        DomainEntitySet<FinancialTransaction> modifiableFinTxns = new DomainEntitySet<FinancialTransaction>();

        for (FinancialTransaction currTxn : getFinancialTransactionCollection()) {
            modifiableFinTxns.add(currTxn);

            //Get the releated transactions
            DomainEntitySet<FinancialTransaction> currList = new DomainEntitySet<FinancialTransaction>();
            currTxn.getRelatedTransactions(currList);
            modifiableFinTxns.addAll(currList);
        }

        return modifiableFinTxns;
    }

    public void updateFinancialTransactionStates(TransactionStateCode pFromState, TransactionStateCode pToState) {
        TransactionState fromState = Application.findById(TransactionState.class, pFromState);
        Criterion<FinancialTransaction> where = FinancialTransaction.CurrentTransactionState().equalTo(fromState);

        for (FinancialTransaction ft : getFinancialTransactionCollection().find(where)) {
            ft.updateFinancialTransactionState(pToState);

        }
    }

    public MoneyMovementTransaction updateTaxPaymentStatus(TaxPaymentStatus pNewStatus) {
        return updateTaxPaymentStatus(pNewStatus, false, true);
    }

    /**
     * @param pNewStatus                status to update to
     * @param pCompleteAcknowledged     whether to mark the MMT as
     * @param pUpdateFinancialTxnStates this should only be set to false when called by the EftpsPayment batch jobs; otherwise it should always be true
     * @return
     */
    public MoneyMovementTransaction updateTaxPaymentStatus(TaxPaymentStatus pNewStatus, boolean pCompleteAcknowledged, boolean pUpdateFinancialTxnStates) {
        if (pNewStatus == null) {
            return this;
        }

        switch (pNewStatus) {
            case None:
            case OnHold:
            case ReturnedTaxPaid:
            case Ignore:
                break;
            case ReadyToSend:
                SpcfCalendar nextInitiationDate = getNextInitiationDate(getMoneyMovementPaymentMethod());
                // if MMT has been held and initiation has passed, update to next available initiation date
                if (getInitiationDate().before(nextInitiationDate)) {
                    updateTaxInitiationDate(nextInitiationDate);
                }
                break;
            case SentToAgency:
                setStatus(PaymentStatus.Executed);
                if (pUpdateFinancialTxnStates) {
                    updateFinancialTransactionStates(TransactionStateCode.Created, TransactionStateCode.Executed);
                }
                break;
            case RejectedByAgency:
            case ReturnedTaxNotPaid:
                if (pUpdateFinancialTxnStates) {
                    updateFinancialTransactionStates(TransactionStateCode.Executed, TransactionStateCode.Returned);
                }
                break;
            case AcknowledgedByAgency:
                // Complete FTs once the payment is acknowledged by the TFA (only if allowed via pCompleteAcknowledged)
                if (pCompleteAcknowledged && pUpdateFinancialTxnStates) {
                    updateFinancialTransactionStates(TransactionStateCode.Executed, TransactionStateCode.Completed);
                }
                break;
        }

        if (!pNewStatus.equals(getTaxPaymentStatus())) {
            setTaxPaymentStatus(pNewStatus);
            setTaxPaymentStatusEffectiveDate(PSPDate.getPSPTime());
        }

        return Application.save(this);
    }

    public static int markPaymentsInProcessForDate(PaymentMethod pPaymentMethod, SpcfCalendar pDate, int pMaxRows) {
        return markPaymentsInProcessForDate(pPaymentMethod, pDate, null, pMaxRows, TaxPaymentStatus.ReadyToSend);
    }

    public static int markPaymentsInProcessForDate(PaymentMethod pPaymentMethod, SpcfCalendar pDate, SpcfMoney pTransactionAmount, int pMaxRows) {
        return markPaymentsInProcessForDate(pPaymentMethod, pDate, pTransactionAmount, pMaxRows, TaxPaymentStatus.ReadyToSend);
    }

    public static int markPaymentsInProcessForDate(PaymentMethod pPaymentMethod, SpcfCalendar pDate, SpcfMoney pTransactionAmount,
                                                   int pMaxRows, TaxPaymentStatus... pTaxPaymentStatuses) {
        StringBuilder builder = new StringBuilder();
        // 'versioned' keyword will increment version column in PSP
        builder.append(" update versioned com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction mmt")
               .append(" set mmt.Status = :newPaymentStatus, mmt.ModifierId = :modifiedBy, mmt.ModifiedDate = :modifiedDate")
               .append(" where mmt.Status in ('Created', 'OnHold')")
               .append(" and mmt.TaxPaymentStatus in (:taxPaymentStatuses)")
               .append(" and mmt.InitiationDate = :initiationDate")
               .append(" and mmt.MoneyMovementPaymentMethod = :paymentMethod");
        if (pTransactionAmount != null) {
            builder.append(" and mmt.MoneyMovementTransactionAmount = :transactionAmount");
        }

        SpcfCalendar day = pDate.copy();
        CalendarUtils.clearTime(day); // clear time from date.

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());
        query.setParameter("newPaymentStatus", PaymentStatus.InProcess);
        query.setParameter("modifiedBy", Application.getCurrentPrincipal().getId());
        query.setParameter("modifiedDate", PSPDate.getPSPTime());
        query.setParameterList("taxPaymentStatuses", pTaxPaymentStatuses);
        query.setParameter("initiationDate", day);
        query.setParameter("paymentMethod", pPaymentMethod);
        if (pTransactionAmount != null) {
            query.setParameter("transactionAmount", pTransactionAmount);
        }
        if (pMaxRows > 0) {
            query.setMaxResults(pMaxRows);
        }
        return query.executeUpdate();
    }

    public static DomainEntitySet<MoneyMovementTransaction> getPendingTaxPaymentsForDate(PaymentMethod pPaymentMethod,
                                                                                         SpcfCalendar pDate,
                                                                                         int pMaxRows) {
        SpcfCalendar day = pDate.copy();
        CalendarUtils.clearTime(day); // clear time from date.
        HqlBuilder hql = new HqlBuilder(" select mmt\n" +
                                                " from com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction mmt\n" +
                                                "   join fetch mmt.Company\n" +
                                                "   join fetch mmt.FinancialTransactionSet ftSet\n" +
                                                " where mmt.Status = 'InProcess'\n" +
                                                "   and mmt.TaxPaymentStatus = 'ReadyToSend'\n" +
                                                "   and mmt.InitiationDate = :initiationDate\n" +
                                                "   and mmt.MoneyMovementPaymentMethod = :paymentMethod\n" +
                                                "   and ftSet.SettlementDate >= mmt.InitiationDate\n" +
                                                "   and ftSet.Company.Id = mmt.Company.Id\n" +
                                                "   and " +
                                                Application.getTruncFunctionString("ftSet.SettlementDate") +
                                                " >= " +
                                                Application.getTruncFunctionString("mmt.InitiationDate") +
                                                "\n " +
                                                " order by mmt.CreatedDate");


        hql.setParameter("initiationDate", day);
        hql.setParameter("paymentMethod", pPaymentMethod);
        return hql.find(0, pMaxRows);
    }

    public static Long getPendingTaxPaymentCountForDate(PaymentMethod pPaymentMethod, SpcfCalendar pDate) {
        SpcfCalendar initiationDate = pDate.copy();
        CalendarUtils.clearTime(initiationDate); // clear time from date.

        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Select(MoneyMovementTransaction.Id().Count())
                .Where(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created)
                                               .And(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend))
                                               .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(pPaymentMethod))
                                               .And(MoneyMovementTransaction.InitiationDate().equalTo(initiationDate)));

        return Application.executeScalarAggQuery(MoneyMovementTransaction.class, query);
    }

    /**
     * Determines if a payment can be split into multiple payments
     *
     * @return true iff
     *         -there are no non-direct payments that share the same Company, PaymentTemplate, PaymentPeriodBegin, PaymentPeriodEnd(), and InitiationDate() as this payment;
     *         -this payment has transactions from a payroll that has either a created ERTaxDebit or created/executed ERTaxRedebit;
     *         -this payment has transactions from a payroll that has no non-complete ERTaxDebits or ERTaxRedebits.
     */
    public boolean canSplit() {
        DomainEntitySet<MoneyMovementTransaction> onHoldMMTs = findTaxPayments().setSiblingTo(this).setNonDirect().setOnHold().find();
        if (onHoldMMTs.size() == 0) {
            DomainEntitySet<FinancialTransaction> pendingERTaxDebitTxns = getPendingERTaxDebits();
            if (pendingERTaxDebitTxns.size() > 0) {
                return (getTaxImpoundCompletedPayrollRuns().size() > 0);
            }
        }

        return false;
    }


    /**
     * Gets the Filing Form associated with the MMT by using the Payment Template and the Due Date
     *
     * @return
     */
    //todo can this use CompanyAgency.getFilingForm?
    public FormTemplate getFilingForm() {
        if (this.getPaymentTemplate() != null) {
            CompanyAgency ca = CompanyAgency.findCompanyAgency(this.getCompany(), this.getPaymentTemplate().getAgency().getAgencyId());
            DomainEntitySet<CompanyAgencyFormTemplate> formTemplates = ca.getCompanyAgencyFormTemplateCollection()
                                                                         .find(CompanyAgencyFormTemplate.FormTemplate().PaymentTemplate().equalTo(this.getPaymentTemplate())
                                                                                                        .And(CompanyAgencyFormTemplate.InvalidDate().isNull()))
                                                                         .sort(CompanyAgencyFormTemplate.EffectiveDate().Descending());

            DomainEntitySet<CompanyAgencyFormTemplate> effectiveFormTemplates = formTemplates.find(CompanyAgencyFormTemplate.EffectiveDate().lessOrEqualThan(this.getPaymentPeriodEnd()));
            if (effectiveFormTemplates.isNotEmpty()) {
                return effectiveFormTemplates.getFirst().getFormTemplate();
            } else if (formTemplates.isNotEmpty()) {
                //if we couldn't find anything that was effective at the pay period end, then find one earlier than that instead.
                return formTemplates.getFirst().getFormTemplate();
            }
        }
        return null;
    }

    public FinancialTransaction getFirstFinancialTransaction() {
        DomainEntitySet<FinancialTransaction> fts = getFinancialTransactionCollection();
        if (fts.size() == 0) {
            return null;
        } else {
            return fts.get(0);
        }
    }

    //I'm not sure if this is really the MMT's Settlement Date (like a property), but I'm treating it as such

    public SpcfCalendar getSettlementDate() {
        Criterion<EftpsPaymentDetail> where = EftpsPaymentDetail.MoneyMovementTransaction().equalTo(this).And(EftpsPaymentDetail.Company().equalTo(this.getCompany()));
        DomainEntitySet<EftpsPaymentDetail> paymentDetails = Application.find(EftpsPaymentDetail.class, where);
        if (paymentDetails.size() == 1) {
            EftpsPaymentDetail detail = paymentDetails.get(0);
            if (detail.getPaymentSettlementDate() != null) {
                return detail.getPaymentSettlementDate();
            }
        }

        FinancialTransaction ft = getFirstFinancialTransaction();
        if (ft != null) {
            return ft.getSettlementDate();
        }

        return getDueDate();
    }

    public boolean isReExecutingTaxPayment() {
        if (getOriginalTransaction() == null) {
            return false;
        }
        TaxPaymentStatus originalStatus = getOriginalTransaction().getTaxPaymentStatus();
        return originalStatus == TaxPaymentStatus.RejectedByAgency || originalStatus == TaxPaymentStatus.ReturnedTaxNotPaid || getOriginalTransaction().isReExecutingTaxPayment();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MoneyMovementTransaction: ").append(getId())
               .append("  PaymentMethod: ").append(getMoneyMovementPaymentMethodString())
               .append("  Status: ").append(getStatus())
               .append("  TaxStatus: ").append(getTaxPaymentStatus().name())
               .append("  InitiationDate: ").append(getInitiationDate())
               .append("  Due Date: ").append(getDueDate())
               .append("  Amount: ").append(getMoneyMovementTransactionAmount());
        return builder.toString();
    }


    //Updates Initiation date

    public void updateTaxInitiationDate(SpcfCalendar pNewInitiationDate) {
        if (!isPendingMMT()) {
            throw new RuntimeException("Cannot adjust a TaxPayment's initiation date when the payment is not in the Created state\n" + getId() + " : " + getStatus().name());
        }

        CalendarUtils.clearTime(pNewInitiationDate);
        setInitiationDate(pNewInitiationDate);
        Application.save(this);

        //Update the Initiation Date for the EntryDetailRecords associated with the MMT
        DomainEntitySet<EntryDetailRecord> entryDetailRecords = getEntryDetailRecordCollection();
        SpcfCalendar settlementDate = this.generatePaymentSettlementDate();
        for (EntryDetailRecord entryDetailRecord : entryDetailRecords) {
            entryDetailRecord.setInitiationDate(pNewInitiationDate);
            entryDetailRecord.setSettlementDate(settlementDate);
            Application.save(entryDetailRecord);
        }

        //Add one business day
        updateFTSettlementDatesFromInitiationDate();
    }

    private void updateFTSettlementDatesFromInitiationDate() {
        SpcfCalendar newSettlementDate = this.generatePaymentSettlementDate();
        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection()) {
            financialTransaction.setSettlementDate(newSettlementDate);
            Application.save(financialTransaction);
        }
    }

    public SpcfCalendar generatePaymentSettlementDate() {

        int dayOffset = getPaymentMethodDayOffset(this.getMoneyMovementPaymentMethod(),this.getPaymentTemplate());

        if (getInitiationDate() != null) {
            SpcfCalendar newSettlementDate = getInitiationDate().copy();
            CalendarUtils.addBusinessDays(newSettlementDate, dayOffset);
            return newSettlementDate;
        }
        return null;

    }

    public CompanyEventDetail getManualAdjustmentNote() {
        return CompanyEvent.getManualAdjustmentNote(getCompany(), EventDetailTypeCode.UniqueIdentifier, getId().toString());
    }

    public CompanyEventDetail getFullRefundNote() {
        return CompanyEvent.getFullRefundNote(getCompany(), EventDetailTypeCode.UniqueIdentifier, getId().toString());
    }

    public CompanyPaymentTemplatePaymentMethod getCompanyPaymentMethod() {
        return CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(getCompany(), getPaymentTemplate())
                                           .getCompanyPaymentTemplatePaymentMethod(getMoneyMovementPaymentMethod());
    }

    public static void removeTaxPaymentOnHoldReason(MoneyMovementTransaction taxPaymentMMT, PaymentOnHoldReason paymentOnHoldReason) {
        if (!taxPaymentMMT.hasActiveOnHoldReason(paymentOnHoldReason)) {
            return;
        }

        // if this is the last reason, expiring it will transition taxPaymentMMT to ReadyToSend
        // initiation date will be updated internally in MMT when status transitions to ReadyToSend
        taxPaymentMMT.expireTaxPaymentOnHoldReason(paymentOnHoldReason);

        if (!taxPaymentMMT.hasActiveOnHoldReasons()) {
            MoneyMovementTransaction readyMMT = findBestTaxPaymentMatch(taxPaymentMMT);
            if (readyMMT != null) {
                // collapse hold onto ready (combine deletes the taxPaymentMMT when finished)
                readyMMT.combinePayment(taxPaymentMMT);
                //todo_rhn verify w/business that if the holdMMT is deleted, the hold history will be lost.  OK?
            }
        }

        // scenarios where the only TaxPaymentOnHoldReason left is PaymentOnHoldReason.Company
        //   move the AgencyTxn from PayrollRuns w/completed ERTaxDebits to (in priority order):
        //   1. an existing ReadyToSend MMT
        //   2. an MMT w/out a hold of PaymentOnHoldReason.Company
        //   3. a new MMT
        if (taxPaymentMMT.hasActiveOnHoldReason(PaymentOnHoldReason.Company) && taxPaymentMMT.getActiveOnHoldReasons().size() == 1) {
            // holdMMT will have all AgencyTxn associated w/PayrollRuns that have pending ERTaxDebits
            if (taxPaymentMMT.hasCompletedERTaxDebits()) {
                MoneyMovementTransaction readyMMT = taxPaymentMMT.splitOutReadyPayment();
                MoneyMovementTransaction targetMMT = findBestTaxPaymentMatch(taxPaymentMMT);
                if (targetMMT != null) {
                    targetMMT.combinePayment(readyMMT);
                } else {
                    Application.save(readyMMT);
                }
            }

            if (taxPaymentMMT.hasPendingERTaxDebits()) {
                Application.save(taxPaymentMMT);
            }
        }
    }

    private static MoneyMovementTransaction findBestTaxPaymentMatch(MoneyMovementTransaction taxPaymentMMT) {
        // first look for TaxPayments in a ReadyToSend status
        // next look for TaxPayments OnHold but no on hold reason of PaymentOnHoldReason.Company

        TaxPaymentsFinder finder = MoneyMovementTransaction.findTaxPayments()
                                                           .setSiblingTo(taxPaymentMMT)
                                                           .setNonDirect();

        MoneyMovementTransaction targetMMT = finder
                .setReadyToSend()
                .find()
                .getFirst();

        if (targetMMT == null && taxPaymentMMT.getTaxPaymentStatus().equals(TaxPaymentStatus.OnHold)) {
            for (MoneyMovementTransaction holdMMT : finder.setOnHold().find()) {
                if (!holdMMT.hasActiveOnHoldReason(PaymentOnHoldReason.Company)) {
                    targetMMT = holdMMT;
                    break;
                }
            }
        }

        return targetMMT;
    }

    public MoneyMovementTransaction findFinalizedMMTMatch(MoneyMovementTransaction mmt) {
        PaymentMethod[] paymentMethods = new PaymentMethod[]{mmt.getMoneyMovementPaymentMethod()};
        TaxPaymentsFinder finder = MoneyMovementTransaction.findTaxPayments()
                                                           .setNonDirect()
                                                           .setPaymentTemplate(mmt.getPaymentTemplate())
                                                           .setPaymentMethods(paymentMethods)
                                                           .setATFFinalized()
                                                           .setSiblingToNoInitiationDateMatch(mmt);


        MoneyMovementTransaction targetMMT = finder.find().getFirst();


        return targetMMT;
    }

    public static void addTaxPaymentOnHoldReason(MoneyMovementTransaction taxPaymentMMT, PaymentOnHoldReason paymentOnHoldReason) {
        addTaxPaymentOnHoldReason(taxPaymentMMT, paymentOnHoldReason, null);
    }

    public static void addTaxPaymentOnHoldReason(MoneyMovementTransaction taxPaymentMMT, PaymentOnHoldReason paymentOnHoldReason, String pNote) {
        // only add an on hold reason to the MMT if there are pending ERTaxDebits
        // do not put MMT on hold if all ERTaxDebits are closed
        // do not create new MMT if all ERTaxDebits are closed; no new Payrolls will be accepted when company is on hold
        if (taxPaymentMMT.hasActiveOnHoldReason(paymentOnHoldReason)) {
            return;
        }

        // do not put direct debits on hold when the on hold reason equals company
        if (PaymentMethod.EFTPSDirectDebit.equals(taxPaymentMMT.getMoneyMovementPaymentMethod()) && PaymentOnHoldReason.Company.equals(paymentOnHoldReason)) {
            return;
        }

        // default MMT to hold to the current MMT
        MoneyMovementTransaction holdMMT = taxPaymentMMT;

        // only company holds can cause an MMT split and they only cause a split when the MMT is not
        // already on an Enrollment or Agent hold
        if (paymentOnHoldReason == PaymentOnHoldReason.Company && !taxPaymentMMT.hasActiveOnHoldReasons()) {
            if (taxPaymentMMT.canSplit()) {
                holdMMT = taxPaymentMMT.splitOutHoldPayment();
                Application.save(holdMMT);
            }
        }

        holdMMT.addTaxPaymentOnHoldReason(paymentOnHoldReason);
    }

    public void addOrRemoveEnrollmentHold() {
        if (getMoneyMovementPaymentMethod() != null) {
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(getCompany(), getPaymentTemplate());
            CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod = companyAgencyPaymentTemplate.getCompanyPaymentTemplatePaymentMethodCollection().find(CompanyPaymentTemplatePaymentMethod.PaymentMethod().equalTo(getMoneyMovementPaymentMethod())).getFirst();
            if (companyPaymentTemplatePaymentMethod.getEnabled() && hasActiveOnHoldReason(PaymentOnHoldReason.Enrollment)) {
                MoneyMovementTransaction.removeTaxPaymentOnHoldReason(this, PaymentOnHoldReason.Enrollment);
            } else if (!companyPaymentTemplatePaymentMethod.getEnabled() && !hasActiveOnHoldReason(PaymentOnHoldReason.Enrollment)) {
                MoneyMovementTransaction.addTaxPaymentOnHoldReason(this, PaymentOnHoldReason.Enrollment);
            }
        } else {
            if (!hasActiveOnHoldReason(PaymentOnHoldReason.Enrollment)) {
                MoneyMovementTransaction.addTaxPaymentOnHoldReason(this, PaymentOnHoldReason.Enrollment);
            }
        }
    }

    public static int getPaymentMethodDayOffset(PaymentMethod pPaymentMethod,PaymentTemplate pPaymentTemplate) {
        if (pPaymentMethod == null) {
            return 0;
        }
        int dayOffset;
        switch (pPaymentMethod) {
            case EFTPS:
            case EFTPSDirectDebit:
                dayOffset = 1;
                break;
            case ACHCredit:
                dayOffset = SystemParameter.findIntValue(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
                break;
            case CheckPayment:
            case SuperCheck:
                dayOffset = 2;
                break;
            case EDI:
                dayOffset = 2;
                break;
            case ACHDebit:
                if(pPaymentTemplate != null && ACHDEBIT_EXCEPTIONAL_CASE.equals(pPaymentTemplate.getPaymentTemplateCd())) {
                    dayOffset = 2;
                }else {
                    dayOffset = 1;
                }
                break;
            default:
                dayOffset = 1;
        }
        return dayOffset;
    }

    public static int getPaymentMethodDayOffset(PaymentMethod pPaymentMethod){
           return getPaymentMethodDayOffset(pPaymentMethod, null);
    }

    public static TaxPaymentsFinder findTaxPayments() {
        return new TaxPaymentsFinder();
    }

    public static class TaxPaymentsFinder {
        private Company company;
        private String paymentTemplateCd;
        private SpcfCalendar periodBeginDate;
        private SpcfCalendar periodEndDate;
        private SpcfCalendar periodEndDateBegin;
        private SpcfCalendar dueDate;
        private SpcfCalendar initiationDate;
        private SpcfCalendar paycheckDate;
        private PaymentMethod[] paymentMethods;
        private boolean includeNullMethod;
        private TaxPaymentStatus[] taxPaymentStatuses;
        private PaymentStatus[] paymentStatuses;
        private SpcfMoney transactionAmount;
        private MoneyMovementTransaction siblingTo;

        private TaxPaymentsFinder() {
        }

        public TaxPaymentsFinder setCompany(Company company) {
            this.company = company;
            return this;
        }

        public TaxPaymentsFinder setPaymentTemplateCd(String paymentTemplateCd) {
            this.paymentTemplateCd = paymentTemplateCd;
            return this;
        }

        public TaxPaymentsFinder setPaymentTemplate(PaymentTemplate paymentTemplate) {
            if (paymentTemplate != null) {
                this.paymentTemplateCd = paymentTemplate.getPaymentTemplateCd();
            } else {
                this.paymentTemplateCd = null;
            }
            return this;
        }

        public TaxPaymentsFinder set941() {
            this.paymentTemplateCd = "IRS-941-PAYMENT";
            return this;
        }

        public TaxPaymentsFinder set940() {
            this.paymentTemplateCd = "IRS-940-PAYMENT";
            return this;
        }

        public TaxPaymentsFinder setDueDate(SpcfCalendar dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public TaxPaymentsFinder setPeriodBeginDate(SpcfCalendar periodBeginDate) {
            this.periodBeginDate = periodBeginDate;
            return this;
        }

        public TaxPaymentsFinder setPeriodEndDate(SpcfCalendar periodEndDate) {
            this.periodEndDate = periodEndDate;
            return this;
        }

        public TaxPaymentsFinder setPeriodEndDateBegin(SpcfCalendar pPeriodEndDateBegin) {
            periodEndDateBegin = pPeriodEndDateBegin;
            return this;
        }

        public TaxPaymentsFinder setQuarter(int year, int quarter) {
            setPeriodEndDateBegin(CalendarUtils.getFirstDayOfQuarter(year, quarter));
            setPeriodEndDate(CalendarUtils.getLastDayOfQuarter(year, quarter));
            return this;
        }

        public TaxPaymentsFinder setInitiationDate(SpcfCalendar initiationDate) {
            this.initiationDate = initiationDate;
            return this;
        }

        public TaxPaymentsFinder setPaycheckDate(SpcfCalendar paycheckDate) {
            this.paycheckDate = paycheckDate;
            return this;
        }

        public TaxPaymentsFinder setTransactionAmount(SpcfMoney transactionAmount) {
            this.transactionAmount = transactionAmount;
            return this;
        }

        public TaxPaymentsFinder setPaymentMethods(PaymentMethod[] paymentMethods) {
            this.paymentMethods = paymentMethods;
            return this;
        }

        public TaxPaymentsFinder setNonDirect() {
            this.paymentMethods = new PaymentMethod[]{PaymentMethod.EFTPS, PaymentMethod.ACHCredit, PaymentMethod.ACHDebit, PaymentMethod.SuperCheck, PaymentMethod.CheckPayment, PaymentMethod.EDI};
            includeNullMethod = true;
            return this;
        }

        public TaxPaymentsFinder setNonDirectEFTPS() {
            this.paymentMethods = new PaymentMethod[]{PaymentMethod.EFTPS};
            return this;
        }

        public TaxPaymentsFinder setDirect() {
            this.paymentMethods = new PaymentMethod[]{PaymentMethod.EFTPSDirectDebit};
            return this;
        }

        public TaxPaymentsFinder setNonHPDE() {
            this.paymentMethods = new PaymentMethod[]{PaymentMethod.EFTPS, PaymentMethod.EFTPSDirectDebit, PaymentMethod.ACHCredit, PaymentMethod.ACHDebit, PaymentMethod.CheckPayment, PaymentMethod.SuperCheck, PaymentMethod.EDI};
            includeNullMethod = true;
            return this;
        }

        public TaxPaymentsFinder setTaxPaymentStatuses(TaxPaymentStatus... taxPaymentStatuses) {
            this.taxPaymentStatuses = taxPaymentStatuses;
            return this;
        }

        public TaxPaymentsFinder setPending() {
            this.taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.OnHold};
            this.paymentStatuses = new PaymentStatus[]{PaymentStatus.Created, PaymentStatus.OnHold};
            return this;
        }

        public TaxPaymentsFinder setPendingOrFinalized() {
            this.taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.OnHold, TaxPaymentStatus.ATFFinalized};
            return this;
        }

        public TaxPaymentsFinder setPendingOrIgnore() {
            this.taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.OnHold, TaxPaymentStatus.Ignore};
            this.paymentStatuses = new PaymentStatus[]{PaymentStatus.Created, PaymentStatus.OnHold};
            return this;
        }

        public TaxPaymentsFinder setOnHoldAndAchDebit() {
            this.taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.OnHold};
            this.paymentMethods = new PaymentMethod[]{PaymentMethod.ACHDebit};
            return this;
        }

        public TaxPaymentsFinder setOnHold() {
            this.taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.OnHold};
            return this;
        }

        public TaxPaymentsFinder setReadyToSend() {
            this.taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend};
            return this;
        }

        public TaxPaymentsFinder setATFFinalized() {
            this.taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
            return this;
        }

        public TaxPaymentsFinder setExecutedOrSuccessful() {
            this.taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.AcknowledgedByAgency, TaxPaymentStatus.ReturnedTaxPaid, TaxPaymentStatus.SentToAgency};
            return this;
        }

        public TaxPaymentsFinder setRejectedOrReturned() {
            this.taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.RejectedByAgency, TaxPaymentStatus.ReturnedTaxNotPaid};
            this.paymentStatuses = new PaymentStatus[]{PaymentStatus.Executed};
            return this;
        }

        public TaxPaymentsFinder setSiblingTo(MoneyMovementTransaction siblingTo) {
            this.siblingTo = siblingTo;
            this.company = siblingTo.getCompany();
            this.paymentTemplateCd = siblingTo.getPaymentTemplate().getPaymentTemplateCd();
            this.periodBeginDate = siblingTo.getPaymentPeriodBegin();
            this.periodEndDate = siblingTo.getPaymentPeriodEnd();
            this.initiationDate = siblingTo.getInitiationDate();
            return this;
        }

        public TaxPaymentsFinder setSiblingToNoInitiationDateMatch(MoneyMovementTransaction siblingTo) {
            this.siblingTo = siblingTo;
            this.company = siblingTo.getCompany();
            this.paymentTemplateCd = siblingTo.getPaymentTemplate().getPaymentTemplateCd();
            this.periodBeginDate = siblingTo.getPaymentPeriodBegin();
            this.periodEndDate = siblingTo.getPaymentPeriodEnd();
            return this;
        }

        public DomainEntitySet<MoneyMovementTransaction> find() {
            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, new Query<MoneyMovementTransaction>().QueryHint("INDEX(this_, PSP_MM_TRANSACTION_I3)")
                    .Where(getCriterion()));
            // remove deleted payments
            for (Iterator<MoneyMovementTransaction> iterator = moneyMovementTransactions.iterator(); iterator.hasNext(); ) {
                MoneyMovementTransaction moneyMovementTransaction = iterator.next();
                if (!Application.getHibernateSession().contains(moneyMovementTransaction)) {
                    iterator.remove();
                }
            }

            return moneyMovementTransactions;
        }

        public Criterion<MoneyMovementTransaction> getCriterion() {
            Criterion<MoneyMovementTransaction> whereClause;
            if (paymentTemplateCd != null) {
                whereClause = MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo(paymentTemplateCd);
            } else {
                whereClause = MoneyMovementTransaction.PaymentTemplate().isNotNull();
            }
            if (company != null) {
                whereClause = whereClause.And(MoneyMovementTransaction.Company().equalTo(company));
            }
            if (periodBeginDate != null) {
                whereClause = whereClause.And(MoneyMovementTransaction.PaymentPeriodBegin().greaterOrEqualThan(periodBeginDate));
            }
            if (periodEndDate != null) {
                whereClause = whereClause.And(MoneyMovementTransaction.PaymentPeriodEnd().lessOrEqualThan(periodEndDate));
            }
            if (periodEndDateBegin != null) {
                whereClause = whereClause.And(MoneyMovementTransaction.PaymentPeriodEnd().greaterOrEqualThan(periodEndDateBegin));
            }
            if (paycheckDate != null) {
                whereClause = whereClause.And(PaymentPeriodBegin().lessOrEqualThan(paycheckDate))
                                         .And(PaymentPeriodEnd().greaterOrEqualThan(paycheckDate));
            }
            if (transactionAmount != null) {
                whereClause = whereClause.And(MoneyMovementTransactionAmount().equalTo(transactionAmount));
            }
            if (initiationDate != null) {
                whereClause = whereClause.And(MoneyMovementTransaction.InitiationDate().equalTo(initiationDate));
            }
            if (dueDate != null) {
                whereClause = whereClause.And(MoneyMovementTransaction.DueDate().equalTo(dueDate));
            }
            if (taxPaymentStatuses != null && taxPaymentStatuses.length > 0) {
                whereClause = whereClause.And(MoneyMovementTransaction.TaxPaymentStatus().in(taxPaymentStatuses));
            }
            if (paymentStatuses != null) {
                whereClause = whereClause.And(MoneyMovementTransaction.Status().in(paymentStatuses));
            }
            if (paymentMethods != null && paymentMethods.length > 0) {
                if (includeNullMethod) {
                    whereClause = whereClause.And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(paymentMethods)
                                                                          .Or(MoneyMovementTransaction.MoneyMovementPaymentMethod().isNull()));
                } else {
                    whereClause = whereClause.And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(paymentMethods));
                }

            }
            if (siblingTo != null) {
                whereClause = whereClause.And(MoneyMovementTransaction.<DomainEntity>Id().notEqualTo(siblingTo.getId()));
            }

            return whereClause;
        }
    }

    public BankAccount findIntuitDebitAccount() {
        DomainEntitySet<FinancialTransaction> creditTransactions =
                getFinancialTransactionCollection()
                        .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit)
                                                  .And(FinancialTransaction.DebitBankAccountType().equalTo(BankAccountOwnerType.Intuit)));
        if (creditTransactions.size() > 0) {
            return creditTransactions.get(0).getDebitBankAccount();
        }

        return null;
    }

    public String getMoneyMovementPaymentMethodString() {
        return getMoneyMovementPaymentMethod() != null ? getMoneyMovementPaymentMethod().toString() : "None";
    }

    public AgencyCheckBatch getAgencyCheckBatch() {
        DomainEntitySet<PaymentBatchAssoc> paymentBatches = Application.find(PaymentBatchAssoc.class, PaymentBatchAssoc.MoneyMovementTransaction().equalTo(this));
        if (paymentBatches.size() > 0) {
            return paymentBatches.getFirst().getAgencyCheckBatch();
        }
        return null;
    }

    //recalculate the payment method based ONLY on the payment-centric requirements

    public void updatePaymentFrequency(PaymentTemplateFrequency pPaymentFrequency) {
        setPaymentFrequency(pPaymentFrequency);
        recalculatePaymentMethod();
    }

    public void updateTaxMoneyMovementTransactionAmount(SpcfMoney pMoneyMovementTransactionAmount) {
        if (!getMoneyMovementTransactionAmount().equals(pMoneyMovementTransactionAmount)) {
            setMoneyMovementTransactionAmount(pMoneyMovementTransactionAmount);
            recalculatePaymentMethod();
            if (getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit) {
                recreateEntryDetailRecords(this);
            }
        }
    }


    public void updateDueDate(SpcfCalendar pDueDate) {
        CalendarUtils.clearTime(pDueDate);
        setDueDate(pDueDate);

        DomainEntitySet<FinancialTransaction> timelyTransactions = new DomainEntitySet<FinancialTransaction>();
        SortedMap<SpcfCalendar, DomainEntitySet<FinancialTransaction>> backdatedTransactions = new TreeMap<SpcfCalendar, DomainEntitySet<FinancialTransaction>>();
        getBackdatedAndTimelyTransaction(timelyTransactions,backdatedTransactions);
        if (backdatedTransactions.isEmpty()) {
            updateTaxInitiationDate(getPaymentInitDate(getDueDate(), getMoneyMovementPaymentMethod(), getPaymentFrequency().getPaymentFrequencyId(), getPaymentTemplate(), false, null, isTaxPayment()));
        } else if (backdatedTransactions.size() == 1 && timelyTransactions.isEmpty()) {
            updateTaxInitiationDate(getPaymentInitDate(getDueDate(), getMoneyMovementPaymentMethod(), getPaymentFrequency().getPaymentFrequencyId(), getPaymentTemplate(), true, backdatedTransactions.firstKey(), isTaxPayment()));
        } else {
            if (timelyTransactions.isEmpty()) {
                //pick the earliest to leave as the "current" one and split the rest from it
                backdatedTransactions.remove(backdatedTransactions.firstKey());
            }

            for (DomainEntitySet<FinancialTransaction> backdatedTransactionsByInitiationDate : backdatedTransactions.values()) {
                MoneyMovementTransaction splitPayment = splitOutAgencyTransactions(backdatedTransactionsByInitiationDate);
                splitPayment.updateDueDate(pDueDate);
            }

            updateTaxInitiationDate(getPaymentInitDate(getDueDate(), getMoneyMovementPaymentMethod(), getPaymentFrequency().getPaymentFrequencyId(), getPaymentTemplate(), false, null, isTaxPayment()));
        }

    }

    public void updateTaxPaymentMethod(PaymentMethod newPaymentMethod) {
        updateTaxPaymentMethod(newPaymentMethod, false);
    }

    /*
        This is called only when an existing tax payment is changing payment methods.
        Otherwise, call setMoneyMovementPaymentMethod.
        Note that this method does things in a strange order and may seem to repeat itself.  This is because of timing issues
        around work that is done in various setters.

        When manualChange is set to true, this will be denoted in the event and that field will be examined when automatically changing in the future.
     */
    public void updateTaxPaymentMethod(PaymentMethod newPaymentMethod, boolean manualChange) {
        if (!isTaxPayment() || getMoneyMovementPaymentMethod() == PaymentMethod.EFTPS || getMoneyMovementPaymentMethod() == PaymentMethod.EFTPSDirectDebit) {
            throw new RuntimeException(getMoneyMovementPaymentMethodString() + " is not a supported payment method to change from  MMT:" + getId());
        }

        if (newPaymentMethod != null) {
            switch (newPaymentMethod) {
                case ACHDebit:
                case ACHCredit:
                case CheckPayment:
                case EDI:
                case SuperCheck:
                    break;
                default:
                    throw new RuntimeException(newPaymentMethod.toString() + " is not a supported payment method to change to  MMT:" + getId());
            }
        }

        PaymentMethod oldPaymentMethod = getMoneyMovementPaymentMethod();

        /*
        Do not change initiation date unless either
            a) new payment method has different offset than old payment method (i.e. update init date to keep settlement date constant)
            b) initiation date would be missed (after cutoff)
         */
        int offsetOffset = getPaymentMethodDayOffset(getMoneyMovementPaymentMethod(), getPaymentTemplate()) - getPaymentMethodDayOffset(newPaymentMethod,getPaymentTemplate());
        SpcfCalendar newInitiationDate = getInitiationDate().copy().toLocal();
        CalendarUtils.addBusinessDays(newInitiationDate, offsetOffset);

        SpcfCalendar nextValidInitDate = MoneyMovementTransaction.getNextInitiationDate(newPaymentMethod);
        if (newInitiationDate.before(nextValidInitDate)) {
            newInitiationDate = nextValidInitDate;
            CalendarUtils.clearTime(newInitiationDate);
        }

        updateTaxInitiationDate(newInitiationDate);

        DomainEntitySet<FinancialTransaction> financialTransactionCollection =
                getFinancialTransactionCollection()
                        .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit));

        for (FinancialTransaction financialTransaction : financialTransactionCollection) {
            financialTransaction.updateSettlementType(newPaymentMethod);
            Application.save(financialTransaction);
        }

        setMoneyMovementPaymentMethod(newPaymentMethod);
        updateFTSettlementDatesFromInitiationDate();

        CompanyEvent event = CompanyEvent.createCompanyEvent(getCompany(), EventTypeCode.PaymentMethodChanged);
        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier, getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentMethod, newPaymentMethod != null ? newPaymentMethod.toString() : "None");
        if (manualChange) {
            event.addCompanyEventDetail(EventDetailTypeCode.UserId, Application.getCurrentPrincipal().getId());
        }


        processOverrideDepositFrequency(oldPaymentMethod);

    }

    /*
     * Removes FTs from this MMT and re-adds if a payment method is changing and this template has rules per payment method for deposit frequencies
     */
    public void processOverrideDepositFrequency(PaymentMethod oldPaymentMethod) {
        DomainEntitySet<FinancialTransaction> creditsToRemove = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<FinancialTransaction> debitsToRemove = new DomainEntitySet<FinancialTransaction>();

        DepositFrequencyCode oldOverrideFrequency =
                getPaymentTemplate().getOverrideDepositFrequency(getCompany(), oldPaymentMethod);
        DepositFrequencyCode changeMethodToOverrideFrequency =
                getPaymentTemplate().getOverrideDepositFrequency(getCompany(), getMoneyMovementPaymentMethod());
        if (oldOverrideFrequency != null &&
                oldOverrideFrequency != changeMethodToOverrideFrequency) {
            creditsToRemove.addAll(getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit)));
            debitsToRemove.addAll(getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxDebit)));
        }

        // remove and re-add transactions to pickup correct deposit frequency
        // remove transactions
        // put the payrolls in memory also
        for (FinancialTransaction financialTransaction : debitsToRemove) {
            MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(financialTransaction);
            if (financialTransaction.getPayrollRun() != null) {
                PayrollRun.getPayrollsInMemory(financialTransaction.getCompany()).add(financialTransaction.getPayrollRun());
            }
        }
        for (FinancialTransaction financialTransaction : creditsToRemove) {
            MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(financialTransaction);
            if (financialTransaction.getPayrollRun() != null) {
                PayrollRun.getPayrollsInMemory(financialTransaction.getCompany()).add(financialTransaction.getPayrollRun());
            }
        }

        // add transactions
        for (FinancialTransaction financialTransaction : creditsToRemove) {
            MoneyMovementTransaction.addFinancialTransactionToTaxPaymentMMT(financialTransaction);
        }
        for (FinancialTransaction financialTransaction : debitsToRemove) {
            MoneyMovementTransaction.addFinancialTransactionToTaxPaymentMMT(financialTransaction);
        }
    }

    public void updatePaymentStatus(PaymentStatus newStatus) {
        if (newStatus != getStatus()) {
            switch (newStatus) {
                case Created:
                    if (getStatus() != PaymentStatus.OnHold) {
                        throw new RuntimeException("PaymentStatus can only change back to Created status if current status is OnHold, but mmt " + getId().toString() + " has a status of " + getStatus().toString());
                    }
                    if (isTaxPayment()) {
                        // Reset the initiation date if the mmt has missed its initiation date
                        SpcfCalendar nextInitiationDate = MoneyMovementTransaction.getNextInitiationDate(getMoneyMovementPaymentMethod());
                        if (nextInitiationDate.after(getInitiationDate())) {

                            updateTaxInitiationDate(nextInitiationDate);
                        }
                    } else {
                        SpcfCalendar nextInitiationDate = PSPDate.getPSPTime();
                        boolean beforeCutoff = this.getCompany().getOffloadGroup().isBeforeActualCutoffTime();
                        if (beforeCutoff) {
                            while (CalendarUtils.isWeekendOrHoliday(nextInitiationDate)) {
                                CalendarUtils.addBusinessDays(nextInitiationDate, 1);
                            }
                        } else {
                            CalendarUtils.addBusinessDays(nextInitiationDate, 1);
                        }
                        nextInitiationDate.setValues(nextInitiationDate.getYear(), nextInitiationDate.getMonth(), nextInitiationDate.getDay(), 0, 0, 0, 0);
                        if (nextInitiationDate.after(this.getInitiationDate().toLocal())) {
                            updateInitiationDate(nextInitiationDate);
                        }
                    }

                    break;
                case OnHold:
                    if (getStatus() != PaymentStatus.Created) {
                        throw new RuntimeException("PaymentStatus can only change to OnHold status if current status is Created, but mmt " + getId().toString() + " has a status of " + getStatus().toString());
                    }

                    break;
                default:
                    if (getStatus() != PaymentStatus.Created || getStatus() != PaymentStatus.OnHold) {
                        throw new RuntimeException("PaymentStatus can only change to " + newStatus.toString() + " if current status is Created or OnHold, but mmt " + getId().toString() + " has a status of " + getStatus().toString());
                    }

                    break;
            }

            setStatus(newStatus);
        }
    }

    public boolean recalculatePaymentMethod() {
        return CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(getCompany(), getPaymentTemplate()).recalculatePaymentMethods(this);
    }

    public void recalculateTaxPaymentStatus() {
        if (getMoneyMovementTransactionAmount().equals(SpcfMoney.ZERO)) {
            boolean shouldPaymentBeSentToAgency;
            if (getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit) {
                IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();

                IRulesPaymentTemplate paymentTemplate = rulesInfo.getPaymentTemplate(this.getPaymentTemplate().getPaymentTemplateCd());
                DepositFrequencyCode depositFrequencyCode = this.getPaymentFrequency().getPaymentFrequencyId();

                IPaymentFrequency paymentFrequency = paymentTemplate.getPaymentFrequency(depositFrequencyCode.toString());

                FrequencyData freq = (FrequencyData) paymentFrequency;

                shouldPaymentBeSentToAgency = (freq == null || freq.isZeroPaymentRequired());
            } else {
                shouldPaymentBeSentToAgency = false; //all other payment methods auto-execute $0
            }

            if (!shouldPaymentBeSentToAgency) {
                if (getTaxPaymentStatus() == TaxPaymentStatus.OnHold) {
                    //On Hold payments go off hold when zero
                    for (TaxPaymentOnHoldReason taxPaymentOnHoldReason : getActiveOnHoldReasons()) {
                        removeTaxPaymentOnHoldReason(this, taxPaymentOnHoldReason.getOnHoldReasonCd());
                    }
                }

                if (getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit) {
                    setTaxPaymentStatus(TaxPaymentStatus.Ignore);
                    setTaxPaymentStatusEffectiveDate(PSPDate.getPSPTime());
                } else if (getMoneyMovementPaymentMethod() == null) {
                    //nothing picks up null payment method, so execute immediately
                    updateTaxPaymentStatus(TaxPaymentStatus.SentToAgency, false, true);
                    updateTaxPaymentStatus(TaxPaymentStatus.AcknowledgedByAgency, true, true);
                }

            }
        } else if (getTaxPaymentStatus() == TaxPaymentStatus.Ignore) {
            setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
            setTaxPaymentStatusEffectiveDate(PSPDate.getPSPTime());
        }
    }

    public void recalculateOffloadBatch() {
        recalculateOffloadBatch(getCompany(), getMoneyMovementPaymentMethod(), getInitiationDate(), getStatus(), getTaxPaymentStatus());
    }

    private void recalculateOffloadBatch(Company company, PaymentMethod moneyMovementPaymentMethod, SpcfCalendar initiationDate, PaymentStatus status, TaxPaymentStatus taxPaymentStatus) {
        if (moneyMovementPaymentMethod == null) return;

        OffloadGroup offloadGroup;
        switch (moneyMovementPaymentMethod) {
            case ACHDirectDeposit:
                switch (status) {
                    case Created:
                        if (getCompany() == null || company.getOffloadGroup() == null) {
                            offloadGroup = Application.find(OffloadGroup.class, OffloadGroup.OffloadGroupCd().equalTo("STD")).get(0);
                        } else {
                            offloadGroup = company.getOffloadGroup();
                        }

                        OffloadBatch pendingOffloadBatch = OffloadBatch.findPendingOffloadBatch(offloadGroup, initiationDate);

                        if (pendingOffloadBatch == null) {
                            if (SystemParameter.findBooleanValue(SystemParameter.Code.CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY)) {
                            	 logger.log(OffloadBatch.getOffloadBatchChangeLogLevel(), "New offload batch created on-the-fly for mmt " + getId() + " because none was found for the date " + initiationDate.toString() + " or it was completed already");                                 pendingOffloadBatch = offloadGroup.createOffloadBatchAndNachaFileRecords(initiationDate);
                            } else {
                                throw new RuntimeException("Could not find a pending offload batch for initiation date " + initiationDate.toString() + " (likely cause: payroll submission started right before offload time and offload finished before commit");
                            }
                        }

                        setOffloadBatch(pendingOffloadBatch);
                        break;
                    case OnHold:
                    case Canceled:
                        setOffloadBatch(null);
                        break;
                    default:
                        break;
                }

                break;
            case ACHCredit:
                switch (taxPaymentStatus) {
                    case ReadyToSend:
                    case ATFFinalized:
                    case Ignore:
                        offloadGroup = Application.find(OffloadGroup.class, OffloadGroup.OffloadGroupCd().equalTo("TXP")).get(0);

                        OffloadBatch pendingOffloadBatch = OffloadBatch.findPendingOffloadBatch(offloadGroup, initiationDate);

                        if (pendingOffloadBatch == null) {
                            if (SystemParameter.findBooleanValue(SystemParameter.Code.CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY)) {
                                logger.log(OffloadBatch.getOffloadBatchChangeLogLevel(), "New offload batch created on-the-fly for mmt " + getId() + " because none was found for the date " + initiationDate.toString() + " or it was completed already");
                                pendingOffloadBatch = offloadGroup.createOffloadBatchAndNachaFileRecords(initiationDate);
                            } else {
                                throw new RuntimeException("Could not find a pending offload batch for initiation date " + initiationDate.toString() + " (likely cause: payroll submission started right before offload time and offload finished before commit MMT:" + getId());
                            }
                        }

                        setOffloadBatch(pendingOffloadBatch);
                        break;
                    case OnHold:
                        setOffloadBatch(null);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Determines the MMT PaymentStatus based on the associated fts
     *
     * @return
     */
    public void recalculatePaymentStatus() {
        switch (getStatus()) {
            case Created:
                // We need to set mmt to OnHold if any are on hold
                if (getFinancialTransactionCollection().find(FinancialTransaction.OnHold().equalTo(true)).size() > 0) {
                    updatePaymentStatus(PaymentStatus.OnHold);
                }
                break;
            case OnHold:
                // We need to set mmt back to Created if there all are off hold
                if (getFinancialTransactionCollection().find(FinancialTransaction.OnHold().equalTo(true)).size() == 0) {
                    updatePaymentStatus(PaymentStatus.Created);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void setInitiationDate(SpcfCalendar initiationDate) {
        if (initiationDate != null && !CalendarUtils.isTimeClear(initiationDate)) {
            throw new RuntimeException("InitiationDate being set to wrong time: " + initiationDate.toString() + " MMT:" + getId());
        }

        if (isQBDTSpecific() && !ObjectUtils.equals(getInitiationDate(), initiationDate)) {
            onUpdate();
        }

        if (!ObjectUtils.equals(getInitiationDate(), initiationDate)) {
            recalculateOffloadBatch(getCompany(), getMoneyMovementPaymentMethod(), initiationDate, getStatus(), getTaxPaymentStatus());
        }

        super.setInitiationDate(initiationDate);
    }

    @Override
    public void setStatus(PaymentStatus pStatus) {
        if (!ObjectUtils.equals(getStatus(), pStatus)) {
            recalculateOffloadBatch(getCompany(), getMoneyMovementPaymentMethod(), getInitiationDate(), pStatus, getTaxPaymentStatus());
        }

        super.setStatus(pStatus);
    }

    @Override
    public void setMoneyMovementPaymentMethod(PaymentMethod pPaymentMethod) {
        boolean paymentMethodUpdated = !ObjectUtils.equals(getMoneyMovementPaymentMethod(), pPaymentMethod);
        if (paymentMethodUpdated && getCompany() != null) {
            recalculateOffloadBatch(getCompany(), pPaymentMethod, getInitiationDate(), getStatus(), getTaxPaymentStatus());
        }

        super.setMoneyMovementPaymentMethod(pPaymentMethod);

        recalculateATFPayments(this);

        //handle any logic specific to a particular payment method or the absence of that method
        if (paymentMethodUpdated) {
            for (FinancialTransaction financialTransaction : this.getFinancialTransactionCollection()) {
                financialTransaction.updateSettlementType(pPaymentMethod);
                Application.save(financialTransaction);
            }
            boolean notACHCredit = false;
            if (pPaymentMethod == null) {
                notACHCredit = true;
            } else {
                switch (pPaymentMethod) {
                    case ACHCredit:
                        MoneyMovementTransaction.recreateEntryDetailRecords(this);
                        break;
                    case ACHDebit:
                    case CheckPayment:
                    case SuperCheck:
                    case EDI:
                    case EFTPS:
                    case EFTPSDirectDebit:
                        notACHCredit = true;
                        break;
                    default:
                        //do nothing for non-tax payments [refactor candidate]
                }
            }

            if (notACHCredit) {
                while (getEntryDetailRecordCollection().size() > 0) {
                    EntryDetailRecord entryDetailRec = getEntryDetailRecordCollection().get(0);
                    removeEntryDetailRecord(entryDetailRec);
                    Application.delete(entryDetailRec);
                }
                setOffloadBatch(null);
            }
        }

    }

    @Override
    public void setTaxPaymentStatus(TaxPaymentStatus pTaxPaymentStatus) {
        if (!ObjectUtils.equals(getTaxPaymentStatus(), pTaxPaymentStatus)) {
            recalculateOffloadBatch(getCompany(), getMoneyMovementPaymentMethod(), getInitiationDate(), getStatus(), pTaxPaymentStatus);
        }

        super.setTaxPaymentStatus(pTaxPaymentStatus);

        recalculateATFPayments(this);
    }

    public static void recalculateATFPayments(MoneyMovementTransaction mmt) {
        PaymentMethod payMethod = mmt.getMoneyMovementPaymentMethod();
        TaxPaymentStatus payStatus = mmt.getTaxPaymentStatus();

        if (payMethod != null && payStatus != null && mmt != null && mmt.getPaymentTemplate() != null) {

            if ((payMethod.equals(PaymentMethod.EFTPS) || payMethod.equals(PaymentMethod.EFTPSDirectDebit) ||
                    payMethod.equals(PaymentMethod.HPDE) || payMethod.equals(PaymentMethod.HPDERefund) ||
                    payMethod.equals(PaymentMethod.CheckPayment) || payMethod.equals(PaymentMethod.ACHCredit) ||
                    payMethod.equals(PaymentMethod.ACHDebit) || payMethod.equals(PaymentMethod.EDI) ||
                    payMethod.equals(PaymentMethod.SuperCheck)) &&
                    (payStatus.equals(TaxPaymentStatus.AcknowledgedByAgency) || payStatus.equals(TaxPaymentStatus.ReturnedTaxPaid) ||
                            payStatus.equals(TaxPaymentStatus.RejectedByAgency) || payStatus.equals(TaxPaymentStatus.ReturnedTaxNotPaid) || payStatus.equals(TaxPaymentStatus.None)) &&
                    (mmt.getPaymentTemplate().isSupportedAsOfDate(mmt.getPaymentPeriodEnd()))) {

                // Delete any existing ATF Payment records for this MMT.
                DomainEntitySet<ATFPaymentsToProcess> paymentsToDelete = Application.find(ATFPaymentsToProcess.class, ATFPaymentsToProcess.MoneyMovementTransaction().equalTo(mmt)
                        .And(ATFPaymentsToProcess.Company().equalTo(mmt.getCompany())));
                for (ATFPaymentsToProcess payment : paymentsToDelete) {
                    Application.delete(payment);
                }

                // Rejected by Agency will be treated as 0.
                boolean treatAsZero = payStatus.in(TaxPaymentStatus.RejectedByAgency, TaxPaymentStatus.ReturnedTaxNotPaid);

                for (Law law : mmt.getPaymentTemplate().getLawCollection()) {

                    ATFPaymentsToProcess paymentRecord = new ATFPaymentsToProcess();
                    paymentRecord.setMoneyMovementTransaction(mmt);
                    paymentRecord.setCompany(mmt.getCompany());
                    paymentRecord.setLaw(law);

                    SpcfMoney amount = new SpcfMoney();
                    for (FinancialTransaction trans : mmt.getFinancialTransactionCollection().find(FinancialTransaction.Law().equalTo(law))) {

                        //We may be setting this to the same value multiple times...
                        paymentRecord.setPaymentDate(trans.getSettlementDate());

                        //Canceled or Voided transactions will be treated as 0's.
                        if ((trans.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Cancelled)) ||
                                (trans.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Voided))) {
                            treatAsZero = true;
                        }

                        //These two transaction types will be treated as 0's.
                        TransactionTypeCode typeCode = trans.getTransactionType().getTransactionTypeCd();
                        if (typeCode.equals(TransactionTypeCode.AgencyPostBALFHPDETaxPayment) ||
                                typeCode.equals(TransactionTypeCode.AgencyPostBALFHPDETaxRefund)) {
                            treatAsZero = true;
                        }

                        //If we are not treating the amount as 0, add or subtract as needed.
                        if (!treatAsZero) {
                            if (TransactionType.addsToPayment(trans.getTransactionType().getTransactionTypeCd())) {
                                amount = (SpcfMoney) amount.add(trans.getFinancialTransactionAmount());
                            } else if (TransactionType.subtractsFromPayment(trans.getTransactionType().getTransactionTypeCd())) {
                                amount = (SpcfMoney) amount.subtract(trans.getFinancialTransactionAmount());
                            }
                        }
                    }

                    // Only save the record if there was at least one FT.
                    if (paymentRecord.getPaymentDate() != null) {
                        paymentRecord.setAmount(treatAsZero ? SpcfMoney.ZERO : amount);
                        paymentRecord.setQuarterEndDate(CalendarUtils.getLastDayOfQuarter(mmt.getPaymentPeriodEnd()));

                        Application.save(paymentRecord);
                    }
                }
            }
        }
    }

    @Override
    public void setOffloadBatch(OffloadBatch pOffloadBatch) {
        Boolean wasModified = !ObjectUtils.equals(getOffloadBatch(), pOffloadBatch);

        if (wasModified &&
                pOffloadBatch != null &&
                pOffloadBatch.getStatusCd() != OffloadBatchStatus.InProcess) {
            logger.log(OffloadBatch.getOffloadBatchChangeLogLevel(), "Offload batch for mmt " + getId() + " changed with an offload batch that is completed already: " + pOffloadBatch.toString());
        }

        super.setOffloadBatch(pOffloadBatch);

        if (wasModified) {
            for (EntryDetailRecord edr : getEntryDetailRecordCollection()) {
                edr.recalculateNachaFile();
            }
        }
    }

    public void setCachedForSUIAdjustment(boolean pCachedForSUIAdjustment) {
        cachedForSUIAdjustment = pCachedForSUIAdjustment;
    }

    // ----- QBDT Token overrides -----
    private boolean isQBDTSpecific() {
        return getQbdtTransactionInfo() != null;
    }

    @Override
    public void setMoneyMovementTransactionAmount(SpcfMoney pMoneyMovementTransactionAmount) {
        if (isQBDTSpecific() && !ObjectUtils.equals(getMoneyMovementTransactionAmount(), pMoneyMovementTransactionAmount)) {
            onUpdate();
        }

        if (pMoneyMovementTransactionAmount.isLessThan(ZERO) && (getMoneyMovementPaymentMethod() == null || getMoneyMovementPaymentMethod().notIn(PaymentMethod.HPDE, PaymentMethod.HPDERefund, PaymentMethod.PostBalfHPDE, PaymentMethod.PostBalfHPDERefund))) {
            //PSP-12357
            logger.error("Negative MMT ID: "+getId()+", amount: "+pMoneyMovementTransactionAmount+ ", payment method: "+getMoneyMovementPaymentMethodString()+", PSID: "+getCompany().getSourceSystemCompanyId(), new RuntimeException());  //log the stacktrace even if we're not blocking the submission
            if (!SystemParameter.findBooleanValue(SystemParameter.Code.ALLOW_NEGATIVE_MMT, false)) {
                throw new RuntimeException("Attempting to assign a negative amount to a MMT");
            }
        }

        super.setMoneyMovementTransactionAmount(pMoneyMovementTransactionAmount);
    }

    @Override
    public void setPaymentPeriodBegin(SpcfCalendar pPaymentPeriodBegin) {
        if (isQBDTSpecific() && !ObjectUtils.equals(getPaymentPeriodBegin(), pPaymentPeriodBegin)) {
            onUpdate();
        }
        super.setPaymentPeriodBegin(pPaymentPeriodBegin);
    }

    @Override
    public void setPaymentPeriodEnd(SpcfCalendar pPaymentPeriodEnd) {
        if (isQBDTSpecific() && !ObjectUtils.equals(getPaymentPeriodEnd(), pPaymentPeriodEnd)) {
            onUpdate();
        }
        super.setPaymentPeriodEnd(pPaymentPeriodEnd);
    }

    @Override
    public void setReferenceNumber(String pReferenceNumber) {
        if (isQBDTSpecific() && !ObjectUtils.equals(getReferenceNumber(), pReferenceNumber)) {
            onUpdate();
        }
        super.setReferenceNumber(pReferenceNumber);
    }

    public static String getNextTransactionNumber() {
        Long nextTransactionNumber = Application.nextSequenceValue(SequenceId.SEQ_TRANSACTION_NUMBER, Long.class);
        return Objects.isNull(nextTransactionNumber) ? null : nextTransactionNumber.toString();
    }

    @Override
    public void setQbdtTransactionInfo(QbdtTransactionInfo pQbdtTransactionInfo) {
        if (!ObjectUtils.equals(getQbdtTransactionInfo(), pQbdtTransactionInfo)) {
            onUpdate();
        }
        super.setQbdtTransactionInfo(pQbdtTransactionInfo);
    }

    public void onUpdate() {
        if (getQbdtTransactionInfo() != null) {
            getQbdtTransactionInfo().onUpdate();
        }
    }

    /*
    @Override
    public DomainEntitySet<EntryDetailRecord> getEntryDetailRecordCollection() {
        if (mEntryDetailRecordSet.size() == 0 ) {
            mEntryDetailRecordSet = Application.find(EntryDetailRecord.class, EntryDetailRecord.InitiationDate().equalTo(getInitiationDate()).And(EntryDetailRecord.MoneyMovementTransaction().equalTo(this)));
        }
        return mEntryDetailRecordSet;
    }
    */

    /**
     *
     * @param timelyTransactions
     * @param backdatedTransactions
     */
    private void getBackdatedAndTimelyTransaction(DomainEntitySet<FinancialTransaction> timelyTransactions,SortedMap<SpcfCalendar, DomainEntitySet<FinancialTransaction>> backdatedTransactions) {
        if (timelyTransactions == null) {
            timelyTransactions = new DomainEntitySet<FinancialTransaction>();
        }
        if (backdatedTransactions == null) {
            backdatedTransactions = new TreeMap<SpcfCalendar, DomainEntitySet<FinancialTransaction>>();
        }
        if (getListOfPaymentTemplatesForETDNullTaxSplits().contains(getPaymentTemplate().getPaymentTemplateCd())) {
            for (FinancialTransaction financialTransaction : getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit))) {
                PayrollRun payrollRun = financialTransaction.getPayrollRun();
                if (payrollRun != null && payrollRun.isBackDated()) {
                    FinancialTransaction etdfinancialTransaction = payrollRun.getEmployerTaxDebitTransaction();
                    SpcfCalendar initiationDate = null;
                    if (etdfinancialTransaction == null) {

                        SpcfCalendar etdSettlementDate = FinancialTransaction.calculateSettlementDate(payrollRun.getCompany().getService(ServiceCode.Tax), payrollRun.getPaycheckSettlementDate());
                        initiationDate = FinancialTransaction.getInitiationDate(etdSettlementDate, TransactionTypeCode.EmployerTaxDebit);
                        logger.info("ETD is null for FT " + financialTransaction.getId() + "for company " + financialTransaction.getCompany().getLegalName() + ",calculated initiationdate is " + initiationDate + " settlementdate is " + etdSettlementDate);
                    } else {
                        initiationDate = etdfinancialTransaction.getInitiationDate();
                    }

                    if (!backdatedTransactions.containsKey(initiationDate)) {
                        backdatedTransactions.put(initiationDate, new DomainEntitySet<FinancialTransaction>());
                    }
                    backdatedTransactions.get(initiationDate).add(financialTransaction);
                } else {
                    timelyTransactions.add(financialTransaction);
                }
            }
            for (FinancialTransaction financialTransaction : getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxDebit))) {
                PayrollRun payrollRun = financialTransaction.getPayrollRun();
                if (payrollRun != null && payrollRun.isBackDated()) {
                    FinancialTransaction etdfinancialTransaction = payrollRun.getEmployerTaxDebitTransaction();
                    SpcfCalendar initiationDate = null;
                    if (etdfinancialTransaction == null) {
                        SpcfCalendar etdSettlementDate = FinancialTransaction.calculateSettlementDate(payrollRun.getCompany().getService(ServiceCode.Tax), payrollRun.getPaycheckSettlementDate());
                        initiationDate = FinancialTransaction.getInitiationDate(etdSettlementDate, TransactionTypeCode.EmployerTaxDebit);
                        logger.info("ETD is null for FT " + financialTransaction.getId() + "for company " + financialTransaction.getCompany().getLegalName() + ",calculated initiationdate is " + initiationDate + " settlementdate is " + etdSettlementDate);
                    } else {
                        initiationDate = etdfinancialTransaction.getInitiationDate();
                    }

                    if (!backdatedTransactions.containsKey(initiationDate)) {
                        continue;
                    }
                    backdatedTransactions.get(initiationDate).add(financialTransaction);
                } else {
                    timelyTransactions.add(financialTransaction);
                }
            }

        } else {
            for (FinancialTransaction financialTransaction : getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit, TransactionTypeCode.AgencyTaxDebit))) {
                if (financialTransaction.getPayrollRun() != null && financialTransaction.getPayrollRun().isBackDated()) {
                    FinancialTransaction etdfinancialTransaction = financialTransaction.getPayrollRun().getEmployerTaxDebitTransaction();
                    if (etdfinancialTransaction == null) {
                        throw new RuntimeException("No EmployerTaxDebit record for source payrollrun id :" + financialTransaction.getPayrollRun().getSourcePayRunId() +
                                                           " having paycheck date: " + financialTransaction.getPayrollRun().getPaycheckDate() +
                                                           " Created on: " + financialTransaction.getPayrollRun().getCreatedDate());
                    }
                    SpcfCalendar initiationDate = etdfinancialTransaction.getInitiationDate();
                    if (!backdatedTransactions.containsKey(initiationDate)) {
                        backdatedTransactions.put(initiationDate, new DomainEntitySet<FinancialTransaction>());
                    }
                    backdatedTransactions.get(initiationDate).add(financialTransaction);
                } else {
                    timelyTransactions.add(financialTransaction);
                }
            }
        }

    }

    public static List<String> getListOfPaymentTemplatesForETDNullTaxSplits() {
        List<String> paymentTemplatesForBackDateHoldOverridden = new ArrayList<String>();
        try {
            String paymentTemplates = SystemParameter.findStringValue(SystemParameter.Code.PMT_TMPLT_SPLIT_ETD_NULL_BACKDATED_AND_TIMELY_TAXES);
            if (paymentTemplates != null) {
                paymentTemplatesForBackDateHoldOverridden = Arrays.asList(paymentTemplates.split(","));
            }
        } catch (Exception e) {
            paymentTemplatesForBackDateHoldOverridden = new ArrayList<String>();
        }
        return paymentTemplatesForBackDateHoldOverridden;
    }

    private Boolean isCompanyOnDDLimitHold(Company pCompany) {
        DomainEntitySet<OnHoldReason> companyOnHoldReasonCollection = pCompany.getOnHoldReasonCollection();
        if (companyOnHoldReasonCollection.find(OnHoldReason.ExpirationDate().isNull().And(OnHoldReason.OnHoldReasonCd().in(ServiceSubStatusCode.DirectDepositLimit))).isNotEmpty()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public void setAgencyTaxpayerId(String pAgencyTaxpayerId) {
        super.setAgencyTaxpayerIdEnc(EncryptionUtils.deterministicEncrypt(AgencyTaxPayerIdKeyName,pAgencyTaxpayerId));
    }


    public String getAgencyTaxpayerId() {
        return EncryptionUtils.deterministicDecrypt(AgencyTaxPayerIdKeyName,getAgencyTaxpayerIdEnc());
    }


}
