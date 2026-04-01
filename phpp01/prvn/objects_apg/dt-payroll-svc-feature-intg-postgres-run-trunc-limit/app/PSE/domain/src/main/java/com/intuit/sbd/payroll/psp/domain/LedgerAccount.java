package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.hibernate.FlushMode;
import org.hibernate.Query;

import java.util.*;

/**
 * Hand-written business logic
 */
public class LedgerAccount extends BaseLedgerAccount {
    private static final String CREDIT_INDICATOR = "C";
    private static final String DEBIT_INDICATOR = "D";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static SpcfMoney getLedgerAccountBalanceByPayrollFinTxnCollection(LedgerAccountCode pLedgerAccountCode,
                                                                             String pSourcePayrollRunId,
                                                                             Company pCompany) {


        PayrollRun payrollRun = PayrollRun.findPayrollRun(pCompany, pSourcePayrollRunId);
        if (payrollRun == null) {
            throw new RuntimeException("No payroll run found for SourcePayrollRunId = " + pSourcePayrollRunId);
        }

        // work from in memory collection and not DB to account for un-flushed updates to transactions
        SpcfDecimal creditSum = SpcfMoney.createInstance(0);
        SpcfDecimal debitSum = SpcfMoney.createInstance(0);
        for (FinancialTransaction finTxn : payrollRun.getFinancialTransactionCollection()) {
            for (FinancialTransactionState finTxnState : finTxn.getFinancialTransactionStates()) {
                PostingRule postingRule = PostingRule.findPostingRule(pLedgerAccountCode,
                        finTxn.getTransactionType(),
                        finTxnState.getTransactionState());
                if (postingRule != null) {
                    if (postingRule.getCreditDebitInd().equals(CREDIT_INDICATOR)) {
                        creditSum = creditSum.add(finTxn.getFinancialTransactionAmount());
                    } else {
                        debitSum = debitSum.add(finTxn.getFinancialTransactionAmount());
                    }
                }
            }
        }

        return new SpcfMoney(creditSum.subtract(debitSum));
    }

    /**
     * Returns the credit balance for a company based on the ledgerAccountCode and
     * payrollRunId.
     * First calculates the sum of all <b>credit</b> ledger amounts for the company having
     * the ledgerAccountCode and associated with the specified parollRunId
     * Next calculates the sum of all <b>debit</b> ledger amounts for the same company having
     * the same ledgerAccountCode and payrollRunId.
     * Returns the subtraction of debits from credits.
     *
     * @param pLedgerAccountCode
     * @param pSourcePayrollRunId
     * @param pCompany
     * @return
     * @throws Exception
     */
    public static SpcfMoney getLedgerAccountBalanceByPayroll(LedgerAccountCode pLedgerAccountCode,
                                                             String pSourcePayrollRunId,
                                                             Company pCompany,
                                                             TransactionAssociationType pExcludedTxnAssocType,
                                                             boolean pIncludePendingTransactions) {
        Map<String, SpcfMoney> result =
                findLedgerEntriesSumByAccountCodeAndPayroll(pCompany, pLedgerAccountCode, pSourcePayrollRunId, null,
                        pExcludedTxnAssocType);

        SpcfDecimal credit = result.get(CREDIT_INDICATOR);
        SpcfDecimal debit = result.get(DEBIT_INDICATOR);

        if (pIncludePendingTransactions) {
            credit = credit.add(getPendingTransactionTotal(LedgerAccountCode.ERReturnReceivable, pCompany, pSourcePayrollRunId, CreditDebitCode.Credit, null));
            debit = debit.add(getPendingTransactionTotal(LedgerAccountCode.ERReturnReceivable, pCompany, pSourcePayrollRunId, CreditDebitCode.Debit, null));
        }


        return new SpcfMoney(credit.subtract(debit));

    }

    //See above
    //when pTax is true, returns only balance associated with posting rules associated with txns that have ONLY the tax service
    //when pTax is false, returns only balance associated with posting rules associated with txns that have no tax service or multiple services that may include tax
    //getLedgerAccountBalanceByPayrollTaxSeparate(...false) + getLedgerAccountBalanceByPayrollTaxSeparate(...true) = getLedgerAccountBalanceByPayroll(...)
    public static SpcfMoney getLedgerAccountBalanceByPayrollTaxSeparate(LedgerAccountCode pLedgerAccountCode,
                                                             String pSourcePayrollRunId,
                                                             Company pCompany,
                                                             TransactionAssociationType pExcludedTxnAssocType,
                                                             boolean pTax,
                                                             boolean pIncludePendingTransactions) {
        Map<String, SpcfMoney> result =
                findLedgerEntriesSumByAccountCodeAndPayroll(pCompany, pLedgerAccountCode, pSourcePayrollRunId, pTax,
                        pExcludedTxnAssocType);

        SpcfDecimal credit = result.get(CREDIT_INDICATOR);
        SpcfDecimal debit = result.get(DEBIT_INDICATOR);

        if (pIncludePendingTransactions) {
            credit = credit.add(getPendingTransactionTotal(LedgerAccountCode.ERReturnReceivable, pCompany, pSourcePayrollRunId, CreditDebitCode.Credit, pTax));
            debit = debit.add(getPendingTransactionTotal(LedgerAccountCode.ERReturnReceivable, pCompany, pSourcePayrollRunId, CreditDebitCode.Debit, pTax));
        }


        return new SpcfMoney(credit.subtract(debit));

    }

    //returns balances for the entire company/template broken down by quarter and law
    public static Map<SpcfCalendar, Map<Law, SpcfMoney>> getLedgerAccountBalanceByTemplate(LedgerAccountCode pLedgerAccountCode,
                                                                                           PaymentTemplate pPaymentTemplate,
                                                                                           Company pCompany) {
        ArrayList<Object[]> ledgerAccountBalanceByTemplate = Application.executeNamedQuery(
                Application.getQueryName("getLedgerAccountBalanceByTemplate"),
                                                                                            new String[]{"company_fk", "ledger_account_fk", "payment_template_fk"},
                                                                                            new Object[]{pCompany.getId().toString(), pLedgerAccountCode.name(), pPaymentTemplate.getPaymentTemplateCd()});

        Map<SpcfCalendar, Map<Law, SpcfMoney>> quarterMap = new HashMap<SpcfCalendar, Map<Law, SpcfMoney>>();
        for (Object[] ledgerAccountBalance : ledgerAccountBalanceByTemplate) {
            String lawId = (String) ledgerAccountBalance[0];
            SpcfMoney amount = (SpcfMoney) ledgerAccountBalance[1];
            int year = (Integer) ledgerAccountBalance[2];
            int quarter = (Integer) ledgerAccountBalance[3];

            SpcfCalendar effectiveQuarter = CalendarUtils.getFirstDayOfQuarter(year, quarter);
            if (!quarterMap.containsKey(effectiveQuarter)) {
                quarterMap.put(effectiveQuarter, new HashMap<Law, SpcfMoney>());
            }
            Law law = Application.findById(Law.class, lawId);
            quarterMap.get(effectiveQuarter).put(law, amount);
        }

        return quarterMap;
    }

    public static Map<SpcfCalendar, Map<PaymentTemplate, Map<Law, SpcfMoney>>> getLedgerAccountBalanceForAllTemplates(LedgerAccountCode pLedgerAccountCode,
                                                                                           Company pCompany) {
        ArrayList<Object[]> ledgerAccountBalanceByTemplate = Application.executeNamedQuery(
                Application.getQueryName("getLedgerAccountBalanceForAllTemplates"),
                                                                                           new String[]{"company_fk", "ledger_account_fk"},
                                                                                           new Object[]{pCompany.getId().toString(), pLedgerAccountCode.name()});

        Map<SpcfCalendar, Map<PaymentTemplate, Map<Law, SpcfMoney>>> quarterMap = new HashMap<SpcfCalendar, Map<PaymentTemplate, Map<Law, SpcfMoney>>>();
        for (Object[] ledgerAccountBalance : ledgerAccountBalanceByTemplate) {
            String lawId = (String) ledgerAccountBalance[0];
            SpcfMoney amount = (SpcfMoney) ledgerAccountBalance[1];
            int year = (Integer) ledgerAccountBalance[2];
            int quarter = (Integer) ledgerAccountBalance[3];

            SpcfCalendar effectiveQuarter = CalendarUtils.getFirstDayOfQuarter(year, quarter);
            if (!quarterMap.containsKey(effectiveQuarter)) {
                quarterMap.put(effectiveQuarter, new HashMap<PaymentTemplate, Map<Law, SpcfMoney>>());
            }

            Law law = Application.findById(Law.class, lawId);

            if (!quarterMap.get(effectiveQuarter).containsKey(law.getPaymentTemplate())) {
                quarterMap.get(effectiveQuarter).put(law.getPaymentTemplate(), new HashMap<Law, SpcfMoney>());
            }

            quarterMap.get(effectiveQuarter).get(law.getPaymentTemplate()).put(law, amount);
        }

        return quarterMap;
    }

    public static Map<Law, SpcfMoney> getLedgerAccountBalanceByPaymentTemplateAndQuarter(LedgerAccountCode pLedgerAccountCode,
                                                         PaymentTemplate pPaymentTemplate,
                                                         Company pCompany,
                                                         SpcfCalendar pCheckDate) {

        Integer quarter = null;
        Integer year = null;
        if(pCheckDate != null) {
            quarter = CalendarUtils.getQuarterAsInt(pCheckDate);
            year = pCheckDate.getYear();
        }
        Map<Law, Map<String,SpcfMoney>> creditDebitValuesMap =
                findLedgerEntriesSumByAccountCodeAndPaymentTemplateAndQuarter(pCompany, pLedgerAccountCode, pPaymentTemplate, quarter, year);

        if (Application.getSessionCache().isEntityCollectionCached(FinancialTransaction.class, FinancialTransaction.ATO_CACHE_KEY)) {
            DomainEntitySet<FinancialTransaction> financialTransactions = Application.getSessionCache().getEntityCollection(FinancialTransaction.class, FinancialTransaction.ATO_CACHE_KEY);
            if(pPaymentTemplate != null) {
                DomainEntitySet<Law> laws = pPaymentTemplate.getLawCollection();
                financialTransactions = financialTransactions.find(FinancialTransaction.Law().in(laws.toArray(new Law[laws.size()])));
            }

            for (FinancialTransaction financialTransaction : financialTransactions) {
                if(CalendarUtils.getFirstDayOfQuarter(year, quarter).compareTo(financialTransaction.getMoneyMovementTransaction().getPaymentPeriodBegin()) != 1 &&
                        CalendarUtils.getLastDayOfQuarter(year, quarter).compareTo(financialTransaction.getMoneyMovementTransaction().getPaymentPeriodEnd()) != -1) {
                    Law law = financialTransaction.getLaw();
                    if(creditDebitValuesMap.get(law) == null) {
                        Map<String, SpcfMoney> amountMap = new HashMap<String, SpcfMoney>();
                        amountMap.put(CREDIT_INDICATOR, SpcfMoney.ZERO);
                        amountMap.put(DEBIT_INDICATOR, SpcfMoney.ZERO);
                        creditDebitValuesMap.put(law, amountMap);
                    }
                    SpcfMoney debit = creditDebitValuesMap.get(law).get(DEBIT_INDICATOR);

                    debit = (SpcfMoney) debit.add(financialTransaction.getFinancialTransactionAmount());
                    creditDebitValuesMap.get(law).put(DEBIT_INDICATOR, debit);
                }
            }
        }

        Map<Law, SpcfMoney> resultMap = new HashMap<Law, SpcfMoney>();
        for (Law law : creditDebitValuesMap.keySet()) {
            Map<String, SpcfMoney> lawMap = creditDebitValuesMap.get(law);
            SpcfDecimal total = SpcfMoney.ZERO;
            if(resultMap.containsKey(law)) {
                total = resultMap.get(law);
            }
            total = total.add(lawMap.get(CREDIT_INDICATOR)).subtract(lawMap.get(DEBIT_INDICATOR));
            resultMap.put(law, new SpcfMoney(total));
        }
        return resultMap;
    }

    public static void addLedgerBalanceFromPriorQuartersInYear(Map<Law, SpcfMoney> currentQuarterBalances,
                                                               LedgerAccountCode pLedgerAccountCode,
                                                               Company pCompany,
                                                               SpcfCalendar pCheckDate,
                                                               PaymentTemplate pPaymentTemplate) {

        SpcfCalendar specifiedQuarter = pCheckDate.copy();

        for (int quarter = CalendarUtils.getQuarterAsInt(specifiedQuarter) - 1; quarter > 0; quarter--) {
            specifiedQuarter.addMonths(-3);
            Map<Law, SpcfMoney> agencyTaxRefundBalanceMap =
                    LedgerAccount.getLedgerAccountBalanceByPaymentTemplateAndQuarter(pLedgerAccountCode, pPaymentTemplate, pCompany, specifiedQuarter);

            for (Map.Entry<Law, SpcfMoney> lawAmountEntry : agencyTaxRefundBalanceMap.entrySet()) {
                SpcfMoney currentQuarterAmount = currentQuarterBalances.get(lawAmountEntry.getKey());
                if (currentQuarterAmount == null) {
                    currentQuarterAmount = SpcfMoney.ZERO;
                }
                SpcfMoney priorQuarterAmount = lawAmountEntry.getValue();
                if (priorQuarterAmount == null) {
                    priorQuarterAmount = SpcfMoney.ZERO;
                }
                currentQuarterBalances.put(lawAmountEntry.getKey(), new SpcfMoney(currentQuarterAmount.add(priorQuarterAmount)));
            }
        }
    }

    public static SpcfMoney getLedgerAccountBalanceByPayrollAndLaw(LedgerAccountCode pLedgerAccountCode,
                                                                   Law pLaw,
                                                                   PayrollRun pPayrollRun) {
        SpcfDecimal credit =
                findLedgerEntriesSumByAccountCodePayrollAndLaw(pLedgerAccountCode, true, pPayrollRun, pLaw);

        SpcfDecimal debit =
                findLedgerEntriesSumByAccountCodePayrollAndLaw(pLedgerAccountCode, false, pPayrollRun, pLaw);

        if (credit == null) {
            credit = new SpcfMoney("0.00");
        }

        if (debit == null) {
            debit = new SpcfMoney("0.00");
        }


        return new SpcfMoney(credit.subtract(debit));

    }

    /**
     * @param pCompany              company
     * @param ledgerAccountCode     ledger
     * @param pSourcePayrollRunId   payrollRun
     * @param pExcludedTxnAssocType OPTIONAL
     * @param pIncludeTaxOnly null, do nothing; true, only tax, false, only not tax, @See getLedgerAccountBalanceByPayrollTaxSeparate
     * @return sum
     */
    public static Map<String, SpcfMoney> findLedgerEntriesSumByAccountCodeAndPayroll(
            Company pCompany,
            LedgerAccountCode ledgerAccountCode,
            String pSourcePayrollRunId,
            Boolean pIncludeTaxOnly,
            TransactionAssociationType pExcludedTxnAssocType) {

        String[] paramNames = new String[4];
        int i = 0;
        paramNames[i++] = "company";
        paramNames[i++] = "sourcePayrollRunId";
        paramNames[i++] = "ledgerAccountCode";
        paramNames[i++] = "excludedTransactionAssociationType";

        Object[] paramValues = new Object[4];
        i = 0;
        paramValues[i++] = pCompany;
        paramValues[i++] = pSourcePayrollRunId;
        paramValues[i++] = ledgerAccountCode;
        paramValues[i++] = pExcludedTxnAssocType;


        String query = " select " +
                " sum(case when postingRule.CreditDebitInd = 'C' then finTxn.FinancialTransactionAmount else 0 end) as CreditAmt," +
                " sum(case when postingRule.CreditDebitInd = 'D' then finTxn.FinancialTransactionAmount else 0 end) as DebitAmt" +
                " from  com.intuit.sbd.payroll.psp.domain.PayrollRun as payrollRun," +
                "       com.intuit.sbd.payroll.psp.domain.FinancialTransaction finTxn," +
                "       com.intuit.sbd.payroll.psp.domain.FinancialTransactionState finTxnState," +
                "       com.intuit.sbd.payroll.psp.domain.PostingRule as postingRule" +
                " where payrollRun.Company = :company" +
                "   and payrollRun.SourcePayRunId = :sourcePayrollRunId" +
                "   and finTxn.PayrollRun.Id = payrollRun.Id" +
                "   and finTxn.Company = payrollRun.Company"+
                "   and finTxnState.Company = finTxn.Company" +
                "   and finTxnState.FinancialTransaction.Id = finTxn.Id" +
                "   and finTxnState.TransactionStateEffectiveDate >= " +
                "FN_DATE_ADD(finTxn.CreatedDate, -2 , 'day')" +
                "   and finTxn.TransactionType.TransactionTypeCd=postingRule.TransactionType.TransactionTypeCd" +
                "   and finTxn.SettlementDate >= " +
                "FN_DATE_ADD(payrollRun.CreatedDate, -2 , 'day')" +
                "   and finTxnState.TransactionState.TransactionStateCd=postingRule.TransactionState.TransactionStateCd" +
                "   and postingRule.LedgerAccount.LedgerAccountCd = :ledgerAccountCode";

        if (pExcludedTxnAssocType != null) {
            query += " and finTxn.TransactionType.AssociationType != :excludedTransactionAssociationType";
        }

        if (pIncludeTaxOnly != null && pIncludeTaxOnly) {
            query += " and 'Tax' = all elements (finTxn.TransactionType.ServiceSet)";
        } else if (pIncludeTaxOnly != null && !pIncludeTaxOnly) {
            query += " and 'Tax' != any elements (finTxn.TransactionType.ServiceSet)";
        }
        org.hibernate.Query hibernateQuery = Application.createHibernateQuery(query);

        // set the parameters where the user supplied an input value
        for (int j = 0; j < 3; j++) {
            hibernateQuery.setParameter(paramNames[j], paramValues[j]);
        }
        if (pExcludedTxnAssocType != null) {
            hibernateQuery.setParameter(paramNames[3], paramValues[3]);
        }

        Map<String,SpcfMoney> result = new HashMap<String,SpcfMoney>();
        result.put(CREDIT_INDICATOR, SpcfMoney.ZERO);
        result.put(DEBIT_INDICATOR, SpcfMoney.ZERO);

        @SuppressWarnings("unchecked")
        List<Object[]> results = (List<Object[]>) hibernateQuery.list();
        if(results.get(0)[0] != null) {
            result.put(CREDIT_INDICATOR, new SpcfMoney(result.get(CREDIT_INDICATOR).add((SpcfMoney)results.get(0)[0])));
        }
        if(results.get(0)[1] != null) {
            result.put(DEBIT_INDICATOR, new SpcfMoney(result.get(DEBIT_INDICATOR).add((SpcfMoney)results.get(0)[1])));
        }

        return result;
    }

    /**
     * @param ledgerAccountCode ledger
     * @param credit            credit/debit
     * @param pPayrollRun       payrollRun
     * @return sum
     */
    public static SpcfMoney findLedgerEntriesSumByAccountCodePayrollAndLaw(LedgerAccountCode ledgerAccountCode,
                                                                           boolean credit,
                                                                           PayrollRun pPayrollRun,
                                                                           Law law) {

        String creditDebitInd = credit ? CREDIT_INDICATOR : DEBIT_INDICATOR;

        DomainEntitySet<FinancialTransaction> financialTransactions;
        if(law == null) {
            financialTransactions = pPayrollRun.getFinancialTransactionCollection();
        } else {
            financialTransactions = pPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.Law().equalTo(law));
        }

        SpcfDecimal amount = new SpcfMoney("0.0");
        for (FinancialTransaction financialTransaction : financialTransactions) {

            DomainEntitySet<FinancialTransactionState> finTxnStates = financialTransaction.getFinancialTransactionStates();
            for (FinancialTransactionState finTxnState : finTxnStates) {
                PostingRule postingRule = PostingRule.findPostingRule(ledgerAccountCode, financialTransaction.getTransactionType(), finTxnState.getTransactionState());
                if(postingRule != null && postingRule.getCreditDebitInd().equals(creditDebitInd)) {
                    amount = amount.add(financialTransaction.getFinancialTransactionAmount());
                }
            }
        }

        return new SpcfMoney(amount);

    }

    private static SpcfMoney sumNewStateLedgerEntriesByAccountCodeAndPayroll(LedgerAccountCode ledgerAccountCode,
                                                                             boolean credit,
                                                                             PayrollRun pPayrollRun) {

        String creditDebitInd = credit ? CREDIT_INDICATOR : DEBIT_INDICATOR;

        SpcfDecimal amount = SpcfMoney.ZERO;
        for (FinancialTransaction financialTransaction : pPayrollRun.getFinancialTransactionCollection()) {
            DomainEntitySet<FinancialTransactionState> finTxnStates = financialTransaction.getFinancialTransactionStates();
            for (FinancialTransactionState finTxnState : finTxnStates) {
                // only processing states that have not been committed yet
                if(finTxnState.isCreatedInCurrentSession()) {
                    PostingRule postingRule = PostingRule.findPostingRule(ledgerAccountCode, financialTransaction.getTransactionType(), finTxnState.getTransactionState());
                    if(postingRule != null && postingRule.getCreditDebitInd().equals(creditDebitInd)) {
                        amount = amount.add(financialTransaction.getFinancialTransactionAmount());
                    }
                }
            }
        }

        return new SpcfMoney(amount);
    }

    private static SpcfMoney findLedgerEntriesSumByAccountCodePayrollAndDate(LedgerAccountCode ledgerAccountCode,
                                                                             boolean credit,
                                                                             PayrollRun pPayrollRun,
                                                                             SpcfCalendar pBeginDate) {

        String creditDebitInd = credit ? CREDIT_INDICATOR : DEBIT_INDICATOR;

        SpcfDecimal amount = SpcfMoney.ZERO;
        for (FinancialTransaction financialTransaction : pPayrollRun.getFinancialTransactionCollection()) {
            DomainEntitySet<FinancialTransactionState> finTxnStates = financialTransaction.getFinancialTransactionStates();
            for (FinancialTransactionState finTxnState : finTxnStates) {
                PostingRule postingRule = PostingRule.findPostingRule(ledgerAccountCode, financialTransaction.getTransactionType(), finTxnState.getTransactionState());
                if(postingRule != null && postingRule.getCreditDebitInd().equals(creditDebitInd) && finTxnState.getTransactionStateEffectiveDate().after(pBeginDate)) {
                    amount = amount.add(financialTransaction.getFinancialTransactionAmount());
                }
            }
        }

        return new SpcfMoney(amount);
    }

    private static Map<Law, Map<String, SpcfMoney>> findLedgerEntriesSumByAccountCodeAndPaymentTemplateAndQuarter(
            Company pCompany,
            LedgerAccountCode ledgerAccountCode,
            PaymentTemplate pPaymentTemplate,
            Integer pQuarter,
            Integer pYear) {

        DomainEntitySet<PayrollRun> payrollRunsToExclude = PayrollRun.getPayrollsInMemory(pCompany);
        List<TransactionTypeCode> postingTransactionTypeCodes = PostingRule.findPostingRuleTransactionTypesByLedgerAccount(ledgerAccountCode);

        NaturalKey naturalKey = null;
        Map<Law, Map<String,SpcfMoney>> cachedMap = null;
        if(pQuarter != null && pYear != null) {
            StringBuilder payrollRunIds = new StringBuilder();
            for (PayrollRun payrollRun : payrollRunsToExclude) {
                if(!payrollRun.isNew()) {
                    payrollRunIds.append(payrollRun.getId().toString());
                }
            }

            int payrollRunHash = payrollRunIds.length() > 0 ? payrollRunIds.toString().hashCode() : 0;
            naturalKey = new NaturalKey(SpcfMoney.class, pCompany.getId(), ledgerAccountCode, pPaymentTemplate == null ? "None" : pPaymentTemplate.getPaymentTemplateCd(), pQuarter, pYear, payrollRunHash);
            cachedMap = Application.getSessionCache().getNonHibernateObject(naturalKey);
        }


        if(cachedMap == null) {
            cachedMap = new HashMap<Law, Map<String,SpcfMoney>>();

            // ATOA transaction is special in that it is not assiciated with a payroll
            if(postingTransactionTypeCodes.contains(TransactionTypeCode.AgencyTaxOverpaymentApplied)) {
                String select =
                        " select law.LawId, " +
                                " sum(case when postingRule.CreditDebitInd = 'C' then finTxn.FinancialTransactionAmount else 0 end) as CreditAmt," +
                                " sum(case when postingRule.CreditDebitInd = 'D' then finTxn.FinancialTransactionAmount else 0 end) as DebitAmt" +
                                " from  com.intuit.sbd.payroll.psp.domain.FinancialTransaction finTxn," +
                                "       com.intuit.sbd.payroll.psp.domain.FinancialTransactionState finTxnState," +
                                "       com.intuit.sbd.payroll.psp.domain.PostingRule as postingRule," +
                                "       com.intuit.sbd.payroll.psp.domain.Law as law";
                String where =
                        " where finTxn.Company = :company" +
                                "   and finTxnState.FinancialTransaction = finTxn" +
                                "   and finTxnState.Company = :company" +
                                "   and finTxn.TransactionType.TransactionTypeCd=postingRule.TransactionType.TransactionTypeCd" +
                                "   and finTxnState.TransactionState.TransactionStateCd=postingRule.TransactionState.TransactionStateCd" +
                                "   and finTxnState.TransactionStateEffectiveDate >= " +
                                "FN_DATE_ADD(" + Application.getTruncFunctionString("finTxn.CreatedDate") + ", -2 , 'day')" +
                                "   and " +
                                Application.getTruncFunctionString("finTxnState.TransactionStateEffectiveDate") +
                                " >= " +
                                "FN_DATE_ADD(" + Application.getTruncFunctionString("finTxn.CreatedDate") + ", -2 , 'day')" +
                                "   and postingRule.LedgerAccount.LedgerAccountCd = :ledgerAccountCode" +
                                "   and finTxn.TransactionType.TransactionTypeCd = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.AgencyTaxOverpaymentApplied" +
                                "   and finTxn.PayrollRun is null" +
                                "   and finTxn.Law = law";

                if(pPaymentTemplate != null) {
                    where += "   and law.PaymentTemplate = :paymentTemplate";
                }

                if(pQuarter != null && pYear != null) {
                    select += "       , com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction mmt";
                    where +=  " and finTxn.MoneyMovementTransaction.Id = mmt.Id" +
                             " and mmt.Company = :company" +
                            " and mmt.PaymentPeriodBegin between :quarterStart and :quarterEnd";
                }

                String groupBy = " group by law.LawId";

                org.hibernate.Query hibernateQuery = Application.createHibernateQuery(select + where + groupBy);

                hibernateQuery.setParameter("company", pCompany);
                hibernateQuery.setParameter("ledgerAccountCode", ledgerAccountCode);

                if(pPaymentTemplate != null) {
                    hibernateQuery.setParameter("paymentTemplate", pPaymentTemplate);
                }

                if(pQuarter != null && pYear != null) {
                    hibernateQuery.setParameter("quarterStart", CalendarUtils.getFirstDayOfQuarter(pYear, pQuarter));
                    SpcfCalendar quarterEnd = CalendarUtils.getLastDayOfQuarter(pYear, pQuarter);
                    CalendarUtils.endOfDay(quarterEnd);
                    hibernateQuery.setParameter("quarterEnd", quarterEnd);
                }

                @SuppressWarnings({"unchecked"})
                List<Object[]> results = (List<Object[]>) hibernateQuery.list();

                for (Object[] result : results) {
                    Law law = Application.findById(Law.class, result[0]);
                    Map<String, SpcfMoney> amountMap = cachedMap.get(law);
                    if(amountMap == null) {
                        amountMap = new HashMap<String, SpcfMoney>();
                        amountMap.put(CREDIT_INDICATOR, SpcfMoney.ZERO);
                        amountMap.put(DEBIT_INDICATOR, SpcfMoney.ZERO);
                        cachedMap.put(law, amountMap);
                    }


                    if(result[1] != null) {
                        amountMap.put(CREDIT_INDICATOR, new SpcfMoney(amountMap.get(CREDIT_INDICATOR).add((SpcfMoney)result[1])));
                    }
                    if(result[2] != null) {
                        amountMap.put(DEBIT_INDICATOR, new SpcfMoney(amountMap.get(DEBIT_INDICATOR).add((SpcfMoney)result[2])));
                    }
                }
            }

            String select =
                    " select law.LawId," +
                            " sum(case when postingRule.CreditDebitInd = 'C' then finTxn.FinancialTransactionAmount else 0 end) as CreditAmt," +
                            " sum(case when postingRule.CreditDebitInd = 'D' then finTxn.FinancialTransactionAmount else 0 end) as DebitAmt" +
                            " from  com.intuit.sbd.payroll.psp.domain.FinancialTransaction finTxn," +
                            "       com.intuit.sbd.payroll.psp.domain.FinancialTransactionState finTxnState," +
                            "       com.intuit.sbd.payroll.psp.domain.PostingRule postingRule," +
                            "       com.intuit.sbd.payroll.psp.domain.Law as law";
            String where =
                    " where finTxn.Company = :company" +
                            "   and finTxnState.Company = :company" +
                            "   and finTxnState.FinancialTransaction = finTxn" +
                            "   and finTxn.TransactionType.TransactionTypeCd=postingRule.TransactionType.TransactionTypeCd" +
                            "   and finTxnState.TransactionState.TransactionStateCd=postingRule.TransactionState.TransactionStateCd" +
                            "   and finTxnState.TransactionStateEffectiveDate >= " +
                            "FN_DATE_ADD(" + Application.getTruncFunctionString("finTxn.CreatedDate") + ", -2 , 'day')" +
                            "   and " +
                            Application.getTruncFunctionString("finTxnState.TransactionStateEffectiveDate") +
                            " >= " +
                            "FN_DATE_ADD(" + Application.getTruncFunctionString("finTxn.CreatedDate") + ", -2 , 'day')" +
                            "   and postingRule.LedgerAccount.LedgerAccountCd = :ledgerAccountCode" +
                            "   and finTxn.TransactionType.TransactionTypeCd in (:transactionTypes)" +
                            "   and finTxn.Law = law";

            if(pPaymentTemplate != null) {
                where += "   and law.PaymentTemplate = :paymentTemplate";
            }


            if(payrollRunsToExclude.size() > 0) {
                where += "   and finTxn.PayrollRun.Company=:company" +
                        "   and finTxn.PayrollRun not in (:excludedPayrollRuns)";
            }

            if(pQuarter != null && pYear != null) {
                select += " , com.intuit.sbd.payroll.psp.domain.PayrollRun pr";
                where += " and finTxn.PayrollRun = pr" +
                        " and pr.Company = :company" +
                        " and pr.PaycheckDate between :quarterStart and :quarterEnd";
            }

            String groupBy = " group by law.LawId";

            org.hibernate.Query hibernateQuery = Application.createHibernateQuery(select + where + groupBy);

            hibernateQuery.setParameter("company", pCompany);
            hibernateQuery.setParameter("ledgerAccountCode", ledgerAccountCode);

            if(pPaymentTemplate != null) {
                hibernateQuery.setParameter("paymentTemplate", pPaymentTemplate);
            }

            postingTransactionTypeCodes.remove(TransactionTypeCode.AgencyTaxOverpaymentApplied);
            hibernateQuery.setParameterList("transactionTypes", postingTransactionTypeCodes);

            if(payrollRunsToExclude.size() > 0) {
                hibernateQuery.setParameterList("excludedPayrollRuns", payrollRunsToExclude);
            }

            if(pQuarter != null && pYear != null) {
                hibernateQuery.setParameter("quarterStart", CalendarUtils.getFirstDayOfQuarter(pYear, pQuarter));
                SpcfCalendar quarterEnd = CalendarUtils.getLastDayOfQuarter(pYear, pQuarter);
                CalendarUtils.endOfDay(quarterEnd);
                hibernateQuery.setParameter("quarterEnd", quarterEnd);
            }

            @SuppressWarnings({"unchecked"})
            List<Object[]> results = (List<Object[]>) hibernateQuery.list();

            for (Object[] result : results) {
                Law law = Application.findById(Law.class, result[0]);
                Map<String, SpcfMoney> amountMap = cachedMap.get(law);
                if(amountMap == null) {
                    amountMap = new HashMap<String, SpcfMoney>();
                    amountMap.put(CREDIT_INDICATOR, SpcfMoney.ZERO);
                    amountMap.put(DEBIT_INDICATOR, SpcfMoney.ZERO);
                    cachedMap.put(law, amountMap);
                }


                if(result[1] != null) {
                    amountMap.put(CREDIT_INDICATOR, new SpcfMoney(amountMap.get(CREDIT_INDICATOR).add((SpcfMoney)result[1])));
                }
                if(result[2] != null) {
                    amountMap.put(DEBIT_INDICATOR, new SpcfMoney(amountMap.get(DEBIT_INDICATOR).add((SpcfMoney)result[2])));
                }
            }

            if(naturalKey != null) {
                Application.getSessionCache().addNonHibernateObject(naturalKey, cachedMap);
            }
        }

        // deep copy the cached map so that we don't cache the result map
        Map<Law, Map<String,SpcfMoney>> resultMap = new HashMap<Law, Map<String, SpcfMoney>>();
        for (Law law : cachedMap.keySet()) {
            Map<String, SpcfMoney> amountMap = new HashMap<String, SpcfMoney>();
            amountMap.put(CREDIT_INDICATOR, new SpcfMoney(cachedMap.get(law).get(CREDIT_INDICATOR)));
            amountMap.put(DEBIT_INDICATOR, new SpcfMoney(cachedMap.get(law).get(DEBIT_INDICATOR)));
            resultMap.put(law, amountMap);
        }

        // add transaction amounts for payrolls in memory
        for (PayrollRun payrollRun : payrollRunsToExclude) {
            Map<Law, Map<String, SpcfMoney>> payrollBalances =
                    findLedgerEntriesSumByAccountCodePaymentTemplateQuarterAndPayroll(ledgerAccountCode, payrollRun, pPaymentTemplate, pQuarter, pYear);
            for (Law law : payrollBalances.keySet()) {

                Map<String, SpcfMoney> payrollAmountMap = payrollBalances.get(law);
                if(payrollAmountMap == null) {
                    continue;
                }

                Map<String, SpcfMoney> amountMap = resultMap.get(law);
                if(amountMap == null) {
                    amountMap = new HashMap<String, SpcfMoney>();
                    amountMap.put(CREDIT_INDICATOR, SpcfMoney.ZERO);
                    amountMap.put(DEBIT_INDICATOR, SpcfMoney.ZERO);
                    resultMap.put(law, amountMap);
                }

                amountMap.put(CREDIT_INDICATOR, new SpcfMoney(amountMap.get(CREDIT_INDICATOR).add(payrollAmountMap.get(CREDIT_INDICATOR))));
                amountMap.put(DEBIT_INDICATOR, new SpcfMoney(amountMap.get(DEBIT_INDICATOR).add(payrollAmountMap.get(DEBIT_INDICATOR))));
            }
        }

        return resultMap;
    }

    public static Map<Law, Map<String, SpcfMoney>> findLedgerEntriesSumByAccountCodePaymentTemplateQuarterAndPayroll(LedgerAccountCode ledgerAccountCode,
                                                                                                                     PayrollRun pPayrollRun,
                                                                                                                     PaymentTemplate pPaymentTemplate,
                                                                                                                     Integer pQuarter,
                                                                                                                     Integer pYear) {
        SpcfCalendar checkDate = pPayrollRun.getPaycheckDate();
        if(checkDate == null ||
                checkDate.getYear() != pYear ||
                CalendarUtils.getQuarterAsInt(checkDate) != pQuarter){
            return new HashMap<Law, Map<String, SpcfMoney>>();
        }

        DomainEntitySet<Law> templateLaws = null;
        if(pPaymentTemplate != null) {
            templateLaws = pPaymentTemplate.getLawCollection();
        }
        Map<Law, Map<String, SpcfMoney>> balances = new HashMap<Law, Map<String, SpcfMoney>>();
        for (FinancialTransaction financialTransaction : pPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.Law().isNotNull())) {
            Law law = financialTransaction.getLaw();
            if(templateLaws== null || templateLaws.contains(law)) {
                DomainEntitySet<FinancialTransactionState> finTxnStates = financialTransaction.getFinancialTransactionStates();
                Map<String, SpcfMoney> amountMap = balances.get(law);
                if(amountMap == null) {
                    amountMap = new HashMap<String, SpcfMoney>();
                    amountMap.put(CREDIT_INDICATOR, SpcfMoney.ZERO);
                    amountMap.put(DEBIT_INDICATOR, SpcfMoney.ZERO);
                    balances.put(law, amountMap);
                }

                for (FinancialTransactionState finTxnState : finTxnStates) {
                    PostingRule postingRule = PostingRule.findPostingRule(ledgerAccountCode, financialTransaction.getTransactionType(), finTxnState.getTransactionState());
                    if(postingRule != null) {
                        if(postingRule.getCreditDebitInd().equals(CREDIT_INDICATOR)) {
                            amountMap.put(CREDIT_INDICATOR, new SpcfMoney(amountMap.get(CREDIT_INDICATOR).add(financialTransaction.getFinancialTransactionAmount())));
                        } else {
                            amountMap.put(DEBIT_INDICATOR, new SpcfMoney(amountMap.get(DEBIT_INDICATOR).add(financialTransaction.getFinancialTransactionAmount())));
                        }
                    }
                }
            }
        }

        return balances;
    }

    //wrote this, turned out not to need it, but could come in useful later, so leaving it in for now

    public static Map<PayrollRun, SpcfMoney> findPayrollRunsWithLedgerBalance(LedgerAccountCode pLedgerAcctCd, Company pCompany) {
        String[] paramNames = new String[2];
        paramNames[0] = "company_id";
        paramNames[1] = "ledgerAccountCode";
        String[] paramValues = new String[2];
        paramValues[0] = pCompany.getId().toString();
        paramValues[1] = pLedgerAcctCd.toString();

        Collection<Object[]> payrollRunBalances = Application.executeNamedQuery("findPayrollRunsWithLedgerBalance", paramNames, paramValues);

        Map<PayrollRun, SpcfMoney> payrollRunBalanceMap = new HashMap<PayrollRun, SpcfMoney>();
        for (Object[] payrollRunBalance : payrollRunBalances) {
            payrollRunBalanceMap.put((PayrollRun) payrollRunBalance[0], new SpcfMoney(SpcfMoney.createInstance((Long) payrollRunBalance[1])));
        }
        return payrollRunBalanceMap;
    }


    public static SpcfMoney getPendingTransactionTotal(LedgerAccountCode pLedgerAcctCd, Company pCompany, String pSourcePayrollRunId, CreditDebitCode pDirection, Boolean pIncludeTaxOnly) {
        String[] paramNames = new String[4];
        paramNames[0] = "company";
        paramNames[1] = "sourcePayrollRunId";
        paramNames[2] = "ledgerAccountCode";
        paramNames[3] = "creditDebitInd";

        Object[] paramValues = new Object[4];
        paramValues[0] = pCompany;
        paramValues[1] = pSourcePayrollRunId;
        paramValues[2] = pLedgerAcctCd;
        paramValues[3] = (pDirection == CreditDebitCode.Credit ? CREDIT_INDICATOR : DEBIT_INDICATOR); //TODO:v2 use enumeration

        String queryName;
        if (pIncludeTaxOnly == null) {
            queryName = "findPendingTransactionSumByAccountCodeAndPayroll";
        } else if (pIncludeTaxOnly) {
            queryName = "findPendingTaxTransactionSumByAccountCodeAndPayroll";
        } else {
            queryName = "findPendingNotTaxTransactionSumByAccountCodeAndPayroll";
        }

        SpcfDecimal sum = new SpcfMoney("0.00");
        DomainEntitySet<FinancialTransaction> rows = Application.findByNamedQuery(queryName, paramNames, paramValues);
        for (FinancialTransaction ft : rows) {
            sum = sum.add(ft.getFinancialTransactionAmount());
        }
        return new SpcfMoney(sum);
    }

    public static SpcfMoney getPendingTransactionTotalByLaw(LedgerAccountCode pLedgerAcctCd, Company pCompany, String pLawId, CreditDebitCode pDirection) {
        String[] paramNames = new String[4];
        paramNames[0] = "company";
        paramNames[1] = "lawId";
        paramNames[2] = "ledgerAccountCode";
        paramNames[3] = "creditDebitInd";

        Object[] paramValues = new Object[4];
        paramValues[0] = pCompany;
        paramValues[1] = pLawId;
        paramValues[2] = pLedgerAcctCd;
        paramValues[3] = (pDirection == CreditDebitCode.Credit ? CREDIT_INDICATOR : DEBIT_INDICATOR); //TODO:v2 use enumeration

        SpcfDecimal sum = new SpcfMoney("0.00");
        DomainEntitySet<FinancialTransaction> rows = Application.findByNamedQuery("findPendingTransactionSumByAccountCodeAndLaw", paramNames, paramValues);
        for (FinancialTransaction ft : rows) {
            sum = sum.add(ft.getFinancialTransactionAmount());
        }
        return new SpcfMoney(sum);
    }

    public static SpcfMoney getLedgerAccountBalanceByPayroll(LedgerAccountCode pLedgerAccountCode,
                                                             String pSourcePayrollRunId, Company pCompany,
                                                             TransactionAssociationType pExcludedTxnAssocType) {
        return getLedgerAccountBalanceByPayroll(pLedgerAccountCode, pSourcePayrollRunId, pCompany, pExcludedTxnAssocType, false);
    }

    /**
     * Returns the credit balance for a company based on the ledgerAccountCode and
     * payrollRunId.
     * First calculates the sum of all <b>credit</b> ledger amounts for the company having
     * the ledgerAccountCode and associated with the specified parollRunId
     * Next calculates the sum of all <b>debit</b> ledger amounts for the same company having
     * the same ledgerAccountCode and payrollRunId.
     * Returns the subtraction of debits from credits.
     *
     * @param pLedgerAccountCode
     * @param pSourcePayrollRunId
     * @param pCompany
     * @return
     * @throws Exception
     */
    public static SpcfMoney getLedgerAccountBalanceByPayroll(LedgerAccountCode pLedgerAccountCode,
                                                             String pSourcePayrollRunId,
                                                             Company pCompany) {
        return getLedgerAccountBalanceByPayroll(pLedgerAccountCode, pSourcePayrollRunId, pCompany, null);
    }


    /**
     * Returns the credit balance for a company based on the ledgerAccountCode.
     * First calculates the sum of all <b>credit</b> ledger amounts for the company
     * having the specified ledgerAccountCode
     * Next calculates the sum of all <b>debit</b> ledger amounts for the same company and
     * ledgerAccountCode
     * Returns the subtraction of debits from credits.
     *
     * @param pCompany
     * @param pLedgerAccountCode
     * @return
     * @throws Exception
     */
    public static SpcfDecimal getLedgerAccountBalance(Company pCompany,
                                                      LedgerAccountCode pLedgerAccountCode) {
        SpcfDecimal total = SpcfMoney.ZERO;

        String[] paramNames = new String[2];
        paramNames[0] = "company";
        paramNames[1] = "ledger";

        String[] paramValues = new String[2];
        paramValues[0] = pCompany.getId().toString();
        paramValues[1] = pLedgerAccountCode.name();

        ArrayList<Double> arrayList = Application.executeNamedQuery("findLedgerEntriesBalanceByAccountCode", paramNames, paramValues);
        if (arrayList != null && !arrayList.isEmpty()) {
            for (Double amount : arrayList) {
                total = total.add(SpcfDecimal.createInstance(amount));
            }
        }

        return total;
    }

    public static SpcfDecimal getLedgerAccountBalanceIncludingPayrollInMemory(Company pCompany,
                                                                              LedgerAccountCode pLedgerAccountCode) {
        //PSP-3027 short-circuit this expensive query if there are no payrolls in memory that would impact the ledger account
        boolean payrollRunsInMemoryImpactLedgerAccount = false;
        List<TransactionTypeCode> transactionTypesImpactingLedgerAccount = PostingRule.findPostingRuleTransactionTypesByLedgerAccount(pLedgerAccountCode);
        for (PayrollRun payrollRun : PayrollRun.getPayrollsInMemory(pCompany)) {
            for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
                if (transactionTypesImpactingLedgerAccount.contains(financialTransaction.getTransactionType().getTransactionTypeCd()))  {
                    payrollRunsInMemoryImpactLedgerAccount = true;
                    break;
                }
            }
        }

        if (!payrollRunsInMemoryImpactLedgerAccount) {
            return getLedgerAccountBalance(pCompany, pLedgerAccountCode);
        }


        boolean isManualFlush = Application.getHibernateSession().getHibernateFlushMode() == FlushMode.MANUAL;

        // if the fluh mode is manual we can assume that none of the new transaction states have been committed
        if(isManualFlush) {
            return findLedgerAccountBalanceIncludingPayrollsInMemoryManualFlush(pCompany, pLedgerAccountCode);
        } else {
            return findLedgerAccountBalanceIncludingPayrollInMemoryAutoFlush(pCompany, pLedgerAccountCode);
        }
    }

    private static SpcfDecimal findLedgerAccountBalanceIncludingPayrollInMemoryAutoFlush(Company pCompany, LedgerAccountCode pLedgerAccountCode) {
        DomainEntitySet<PayrollRun> payrollsInMemory = PayrollRun.getPayrollsInMemory(pCompany);
        StringBuilder payrollRunIds = new StringBuilder();
        for (PayrollRun payrollRun : payrollsInMemory) {
            if(!payrollRun.isNew()) {
                payrollRunIds.append(payrollRun.getId().toString());
            }
        }

        int payrollRunHash = payrollRunIds.length() > 0 ? payrollRunIds.toString().hashCode() : 0;
        NaturalKey naturalKey = new NaturalKey(LedgerAccount.class, pCompany.getId(), pLedgerAccountCode, payrollRunHash);
        SpcfDecimal cachedAccountBalance = Application.getSessionCache().getNonHibernateObject(naturalKey);
        if(cachedAccountBalance == null) {
            SpcfDecimal balance = null;
            SpcfCalendar balanceDate = null;

            List<Object[]> ledgerBalance = findLedgerBalanceDateBalanceAmountByCompanyAndAccountCode(pCompany, pLedgerAccountCode);

            if(!ledgerBalance.isEmpty() && ledgerBalance.size() > 0) {
                balanceDate = (SpcfCalendar)ledgerBalance.get(0)[0];
                balance = (SpcfMoney)ledgerBalance.get(0)[1];
            }

            if(balance == null) {
                balance = SpcfMoney.ZERO;
            }
            if(balanceDate == null) {
                balanceDate = SpcfCalendar.createInstance(1970, 1, 1);
            }

            CalendarUtils.clearTime(balanceDate);

            // add the transactions that have happened since the balance was calculated for payrolls not in memory
            String query =
                    " select " +
                            " sum(case when postingRule.CreditDebitInd = 'C' then finTxn.FinancialTransactionAmount else 0 end) as CreditAmt," +
                            " sum(case when postingRule.CreditDebitInd = 'D' then finTxn.FinancialTransactionAmount else 0 end) as DebitAmt" +
                            " from  com.intuit.sbd.payroll.psp.domain.FinancialTransaction finTxn," +
                            "       com.intuit.sbd.payroll.psp.domain.FinancialTransactionState finTxnState," +
                            "       com.intuit.sbd.payroll.psp.domain.PostingRule postingRule" +
                            " where finTxnState.Company = :company" +
                            "   and finTxnState.FinancialTransaction.Id = finTxn.Id" +
                            "   and finTxn.TransactionType.TransactionTypeCd = postingRule.TransactionType.TransactionTypeCd" +
                            "   and finTxnState.TransactionState.TransactionStateCd = postingRule.TransactionState.TransactionStateCd" +
                            "   and finTxnState.TransactionStateEffectiveDate > :balanceDate" +
                            "   and " +
                            Application.getTruncFunctionString("finTxnState.TransactionStateEffectiveDate") +
                            " > :balanceDate" +
                            "   and postingRule.LedgerAccount.LedgerAccountCd = :ledgerAccountCode";

            if(payrollsInMemory.size() > 0) {
                query += "   and (finTxn.PayrollRun is null or finTxn.PayrollRun not in (:excludedPayrollRuns))";
            }

            Query hibernateQuery = Application.createHibernateQuery(query);

            hibernateQuery.setParameter("company", pCompany);
            hibernateQuery.setParameter("balanceDate", balanceDate);
            hibernateQuery.setParameter("ledgerAccountCode", pLedgerAccountCode);

            if(payrollsInMemory.size() > 0) {
                hibernateQuery.setParameterList("excludedPayrollRuns", payrollsInMemory);
            }

            @SuppressWarnings("unchecked")
            List<Object[]> results = (List<Object[]>) hibernateQuery.list();
            if(!results.isEmpty() && results.size() > 0) {
                // add credits
                balance = balance.add((results.get(0)[0] != null ? (SpcfMoney) results.get(0)[0] : SpcfMoney.ZERO));
                // subtract debits
                balance = balance.subtract((results.get(0)[1] != null ? (SpcfMoney) results.get(0)[1] : SpcfMoney.ZERO));
            }

            // add transaction amounts for payrolls in memory
            for (PayrollRun payrollRun : payrollsInMemory) {
                cachedAccountBalance = cachedAccountBalance.add(
                        findLedgerEntriesSumByAccountCodePayrollAndDate(pLedgerAccountCode, true, payrollRun, balanceDate));

                cachedAccountBalance = cachedAccountBalance.subtract(
                        findLedgerEntriesSumByAccountCodePayrollAndDate(pLedgerAccountCode, false, payrollRun, balanceDate));
            }

            cachedAccountBalance = balance;
            Application.getSessionCache().addNonHibernateObject(naturalKey, cachedAccountBalance);
        }
        return cachedAccountBalance;
    }

    private static SpcfDecimal findLedgerAccountBalanceIncludingPayrollsInMemoryManualFlush(Company pCompany, LedgerAccountCode pLedgerAccountCode) {
        NaturalKey naturalKey = new NaturalKey(LedgerAccount.class, pCompany.getId(), pLedgerAccountCode);
        SpcfDecimal cachedAccountBalance = Application.getSessionCache().getNonHibernateObject(naturalKey);
        if(cachedAccountBalance == null) {
            SpcfDecimal balance = null;
            SpcfCalendar balanceDate = null;

            List<Object[]> ledgerBalance = findLedgerBalanceDateBalanceAmountByCompanyAndAccountCode(pCompany, pLedgerAccountCode);

            if(!ledgerBalance.isEmpty() && ledgerBalance.size() > 0) {
                balanceDate = (SpcfCalendar)ledgerBalance.get(0)[0];
                balance = (SpcfMoney)ledgerBalance.get(0)[1];
            }

            if(balance == null) {
                balance = SpcfMoney.ZERO;
            }
            if(balanceDate == null) {
                balanceDate = SpcfCalendar.createInstance(1970, 1, 1);
            }

            CalendarUtils.clearTime(balanceDate);

            // add the transactions that have happened since the balance was calculated for payrolls not in memory
            String query =
                    " select " +
                            " sum(case when postingRule.CreditDebitInd = 'C' then finTxn.FinancialTransactionAmount else 0 end) as CreditAmt," +
                            " sum(case when postingRule.CreditDebitInd = 'D' then finTxn.FinancialTransactionAmount else 0 end) as DebitAmt" +
                            " from  com.intuit.sbd.payroll.psp.domain.FinancialTransaction finTxn," +
                            "       com.intuit.sbd.payroll.psp.domain.FinancialTransactionState finTxnState," +
                            "       com.intuit.sbd.payroll.psp.domain.PostingRule postingRule" +
                            " where finTxnState.Company = :company" +
                            "   and finTxnState.FinancialTransaction.Id = finTxn.Id" +
                            "   and finTxn.TransactionType.TransactionTypeCd = postingRule.TransactionType.TransactionTypeCd" +
                            "   and finTxnState.TransactionState.TransactionStateCd = postingRule.TransactionState.TransactionStateCd" +
                            "   and finTxnState.TransactionStateEffectiveDate > :balanceDate" +
                            "   and " +
                            Application.getTruncFunctionString("finTxnState.TransactionStateEffectiveDate") +
                            " > :balanceDate" +
                            "   and postingRule.LedgerAccount.LedgerAccountCd = :ledgerAccountCode";

            Query hibernateQuery = Application.createHibernateQuery(query);

            hibernateQuery.setParameter("company", pCompany);
            hibernateQuery.setParameter("balanceDate", balanceDate);
            hibernateQuery.setParameter("ledgerAccountCode", pLedgerAccountCode);

            @SuppressWarnings("unchecked")
            List<Object[]> results = (List<Object[]>) hibernateQuery.list();
            if(!results.isEmpty() && results.size() > 0) {
                // add credits
                balance = balance.add((results.get(0)[0] != null ? (SpcfMoney) results.get(0)[0] : SpcfMoney.ZERO));
                // subtract debits
                balance = balance.subtract((results.get(0)[1] != null ? (SpcfMoney) results.get(0)[1] : SpcfMoney.ZERO));
            }

            cachedAccountBalance = balance;
            Application.getSessionCache().addNonHibernateObject(naturalKey, cachedAccountBalance);
        }

        // add transaction amounts for payrolls in memory
        DomainEntitySet<PayrollRun> payrollsInMemory = PayrollRun.getPayrollsInMemory(pCompany);
        for (PayrollRun payrollRun : payrollsInMemory) {
            cachedAccountBalance = cachedAccountBalance.add(
                    sumNewStateLedgerEntriesByAccountCodeAndPayroll(pLedgerAccountCode, true, payrollRun));

            cachedAccountBalance = cachedAccountBalance.subtract(
                    sumNewStateLedgerEntriesByAccountCodeAndPayroll(pLedgerAccountCode, false, payrollRun));
        }
        return cachedAccountBalance;
    }

    private static List<Object[]> findLedgerBalanceDateBalanceAmountByCompanyAndAccountCode(Company pCompany, LedgerAccountCode pLedgerAccountCode) {
        String hqlQuery = " select lb.BalanceDate, lb.BalanceAmount " +
                "           from com.intuit.sbd.payroll.psp.domain.LedgerBalance as lb " +
                "           where lb.Company = :company " +
                "           and   lb.LedgerAccount.LedgerAccountCd = :accountCode" +
                "           and   " +
                Application.getTruncFunctionString("lb.BalanceDate") +
                " = (select " +
                Application.getTruncFunctionString("max(lb2.BalanceDate)") +
                "                                          from com.intuit.sbd.payroll.psp.domain.LedgerBalance as lb2" +
                "                                          where lb2.Company = :company" +
                "                                          and   lb2.LedgerAccount.LedgerAccountCd = :accountCode)";

        Query hibernateQuery = Application.createHibernateQuery(hqlQuery);

        hibernateQuery.setParameter("company", pCompany);
        hibernateQuery.setParameter("accountCode", pLedgerAccountCode);
        return hibernateQuery.list();
    }

    public static HashMap<LedgerAccountCode, SpcfMoney> getLedgerAccountBalances(Company pCompany, PayrollRun pPayrollRun, ArrayList<LedgerAccountCode> pAccounts) {
        HashMap<LedgerAccountCode, SpcfMoney> balances = new HashMap<LedgerAccountCode, SpcfMoney>();

        for (LedgerAccountCode ledgerAccount : pAccounts) {
            if (pPayrollRun != null) {
                balances.put(ledgerAccount, getLedgerAccountBalanceByPayroll(ledgerAccount, pPayrollRun.getSourcePayRunId(), pCompany));
            } else {
                balances.put(ledgerAccount, new SpcfMoney(getLedgerAccountBalance(pCompany, ledgerAccount)));
            }
        }

        return balances;
    }


    public static HashMap<LedgerAccountCode, SpcfMoney> getLedgerAccountBalances(Company pCompany, ArrayList<PayrollRun> pPayrollRuns, ArrayList<LedgerAccountCode> pAccounts) {
        HashMap<LedgerAccountCode, SpcfMoney> balances = new HashMap<LedgerAccountCode, SpcfMoney>();

        for (LedgerAccountCode ledgerAccount : pAccounts) {
            if (pPayrollRuns.size() > 0) {
                for (PayrollRun payrollRun : pPayrollRuns) {
                    SpcfMoney currentBalance = balances.get(ledgerAccount) != null ? balances.get(ledgerAccount) : new SpcfMoney("0.00");
                    balances.put(ledgerAccount, new SpcfMoney(currentBalance.add(getLedgerAccountBalanceByPayroll(ledgerAccount, payrollRun.getSourcePayRunId(), pCompany))));
                }
            } else {
                balances.put(ledgerAccount, new SpcfMoney(getLedgerAccountBalance(pCompany, ledgerAccount)));
            }
        }

        return balances;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public LedgerAccount() {
        super();
    }

    public CreditDebitCode getLedgerBalanceAmountTypeIndicator(SpcfMoney pBalanceAmount) {
        if (getBalanceCalculationRule().equals(LedgerBalanceCalculationRuleEnum.CreditAddsToBalance)) {
            if (pBalanceAmount.getSign() >= 0) {
                return CreditDebitCode.Credit;
            } else {
                return CreditDebitCode.Debit;
            }
        } else if (getBalanceCalculationRule().equals(LedgerBalanceCalculationRuleEnum.DebitAddsToBalance)) {
            if (pBalanceAmount.getSign() >= 0) {
                return CreditDebitCode.Debit;
            } else {
                return CreditDebitCode.Credit;
            }
        }

        return null;
    }

}
