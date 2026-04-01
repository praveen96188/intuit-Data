package com.intuit.sbd.payroll.psp.domain;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class IntuitBankAccountTransactionType extends BaseIntuitBankAccountTransactionType {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static IntuitBankAccountTransactionType findIntuitBankAccountTransactionType(TransactionType pTransactionType, CreditDebitCode pCreditDebitCd, SpcfCalendar pAsOfDate) {
        String cacheKey = "IntuitBankAccountTransactionTypeByTransactionTypeAndCreditDebitInd:" + pTransactionType.toString() + ";" + pCreditDebitCd.toString();

        DomainEntitySet<IntuitBankAccountTransactionType> intuitBATransactionTypes = Application.getSessionCache().getEntityCollection(IntuitBankAccountTransactionType.class, cacheKey);
        if (intuitBATransactionTypes == null) {
            Expression<IntuitBankAccountTransactionType> query =
                    new Query<IntuitBankAccountTransactionType>()
                          .Where(IntuitBankAccountTransactionType.CreditDebitInd().equalTo(pCreditDebitCd)
                                 .And(IntuitBankAccountTransactionType.TransactionType().equalTo(pTransactionType)))
                          .EagerLoad(IntuitBankAccountTransactionType.IntuitBankAccount());

            intuitBATransactionTypes = Application.find(IntuitBankAccountTransactionType.class, query);

            Application.getSessionCache().addEntityCollection(IntuitBankAccountTransactionType.class, cacheKey, intuitBATransactionTypes);
        }

        for (IntuitBankAccountTransactionType intuitBATransactionType : intuitBATransactionTypes) {
            IntuitBankAccount intuitBankAccount = intuitBATransactionType.getIntuitBankAccount();

            if ((intuitBankAccount.getEffDttm() == null || intuitBankAccount.getEffDttm().before(pAsOfDate)) &&
                (intuitBankAccount.getExpDttm() == null || pAsOfDate.before(intuitBankAccount.getExpDttm()))) {
                return intuitBATransactionType;
            }
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public IntuitBankAccountTransactionType()
	{
		super();
	}
}