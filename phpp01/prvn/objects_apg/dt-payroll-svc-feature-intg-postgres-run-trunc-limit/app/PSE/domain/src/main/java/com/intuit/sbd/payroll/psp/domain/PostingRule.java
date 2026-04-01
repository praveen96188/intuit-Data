package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hand-written business logic
 */
public class PostingRule extends BasePostingRule {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static Map<NaturalKey,PostingRule> cacheAllPostingRules() {
        Expression<PostingRule> postingRuleQuery =
                new Query<PostingRule>().EagerLoad(PostingRule.LedgerAccount(), PostingRule.TransactionState(), PostingRule.TransactionType());
        DomainEntitySet<PostingRule> postingRules = Application.find(PostingRule.class, postingRuleQuery);
        HashMap<NaturalKey,PostingRule> rulesMap = new HashMap<NaturalKey,PostingRule>();
        for (PostingRule postingRule : postingRules) {
            NaturalKey naturalKey = postingRule.getNaturalKey();
            if (!rulesMap.containsKey(naturalKey)) {
                rulesMap.put(naturalKey, postingRule);
            } else {
                // handle bugs in our static data where two posting rules exist -- a debit/credit to the same account
                // that would cancel each other out; returning no posting rule has the same effect
                rulesMap.remove(naturalKey);
            }
        }
        Application.getSessionCache().addNonHibernateObject("PostingRulesMap", rulesMap);
        return rulesMap;

        // types w/2 posting rules:
        //  'ERPayable';'EmployerCreditBalanceCarryForwardCredit';'Created'
        //  'ERPayable';'EmployerCreditBalanceCarryForwardCredit';'Cancelled'
        //  'ERReturnCash';'UntimelyReturnPostWriteOff';'Voided'
        //  'ERReturnCash';'UntimelyReturnPostWriteOff';'Executed'        
    }

    public static PostingRule findPostingRule(LedgerAccountCode pLedgerAccountCode, TransactionType pTransactionType, TransactionState pTransactionState) {

        Map<NaturalKey, PostingRule> rulesMap = Application.getSessionCache().getNonHibernateObject("PostingRulesMap");
        if (rulesMap == null) {
            rulesMap = cacheAllPostingRules();
        }

        return rulesMap.get(new NaturalKey(PostingRule.class,
                                           pLedgerAccountCode,
                                           pTransactionType.getTransactionTypeCd(),
                                           pTransactionState.getTransactionStateCd()));
    }

    public static List<TransactionTypeCode> findPostingRuleTransactionTypesByLedgerAccount(LedgerAccountCode pLedgerAccountCode) {
        Map<NaturalKey, PostingRule> rulesMap = Application.getSessionCache().getNonHibernateObject("PostingRulesMap");
        if (rulesMap == null) {
            rulesMap = cacheAllPostingRules();
        }

        List<TransactionTypeCode> transactionTypeCodes = new ArrayList<TransactionTypeCode>();
        for (PostingRule postingRule : rulesMap.values()) {
            if(postingRule.getLedgerAccount().getLedgerAccountCd() == pLedgerAccountCode &&
                    !transactionTypeCodes.contains(postingRule.getTransactionType().getTransactionTypeCd())) {
                transactionTypeCodes.add(postingRule.getTransactionType().getTransactionTypeCd());
            }
        }

        return transactionTypeCodes;
    }

    public static DomainEntitySet<PostingRule> findPostingRuleByFinancialTransaction(FinancialTransaction pFinancialTransaction,
                                                                       LedgerAccountCode pLedgerAccountCode) {

        String[] paramNames = new String[2];
         paramNames[0] = "finTxnId";
         paramNames[1] = "ledgerAccountCode";

         Object[] paramValues = new Object[2];
         paramValues[0] = pFinancialTransaction.getId();
         paramValues[1] = pLedgerAccountCode;

         DomainEntitySet<PostingRule> postingRules =
                 Application.findByNamedQuery("findPostingRuleByFinancialTransaction", paramNames, paramValues);

        return postingRules;
    }

    /**
     * Function to get the eligible Transaction Types for the GEMS Upload based on the Reporting Frequency
     * @param pReportingFrequency ReportingFrequency (Daily/Monthly)
     * @return DomainEntitySet<PostingRule>
     */
    public static DomainEntitySet<PostingRule> findEligibleGemsTransactionTypes(ReportingFrequency pReportingFrequency) {

        String[] paramNames = new String[1];
         paramNames[0] = "reportingFrequency";

         Object[] paramValues = new Object[1];
         paramValues[0] = pReportingFrequency;

         DomainEntitySet<PostingRule> postingRules =
                 Application.findByNamedQuery("findGEMSPostingRules", paramNames, paramValues);

        return postingRules;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public PostingRule()
	{
		super();
	}

    public NaturalKey getNaturalKey() {
        return new NaturalKey(PostingRule.class,
                              getLedgerAccount().getLedgerAccountCd(),
                              getTransactionType().getTransactionTypeCd(),
                              getTransactionState().getTransactionStateCd());
    }
}