package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class AchTransactionCode extends BaseAchTransactionCode {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders & counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static DomainEntitySet<AchTransactionCode> findAchTransactionCode(BankAccountType pBankAccountType, CreditDebitCode pCreditDebitCode, boolean pIsReturnCode) {

        ACHBankAccountType achBankAccountType = ACHBankAccountType.valueOf(pBankAccountType.toString());

        Expression<AchTransactionCode> query =
                new Query<AchTransactionCode>().Where(AchTransactionCode.AchAccountTypeCd().equalTo(achBankAccountType)
                        .And(AchTransactionCode.CreditDebitIndicator().equalTo(pCreditDebitCode)
                        .And(AchTransactionCode.IsReturnCode().equalTo(pIsReturnCode))));

        return Application.find(AchTransactionCode.class, query);
    }

    public static AchTransactionCode findAchTransactionCode(String pTransactionCd) {
        if (pTransactionCd == null) {
            return null;
        }

        return Application.findById(AchTransactionCode.class, pTransactionCd);
    }

    public static DomainEntitySet<AchTransactionCode> findAchTransactionCode(ACHBankAccountType pACHBankAccountType, CreditDebitCode pCreditDebitCode, boolean pIsReturnCode) {
        Expression<AchTransactionCode> query =
                new Query<AchTransactionCode>().Where(AchTransactionCode.AchAccountTypeCd().equalTo(pACHBankAccountType)
                        .And(AchTransactionCode.CreditDebitIndicator().equalTo(pCreditDebitCode)
                        .And(AchTransactionCode.IsReturnCode().equalTo(pIsReturnCode))));

        return Application.find(AchTransactionCode.class, query);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public AchTransactionCode()
	{
		super();
	}


}