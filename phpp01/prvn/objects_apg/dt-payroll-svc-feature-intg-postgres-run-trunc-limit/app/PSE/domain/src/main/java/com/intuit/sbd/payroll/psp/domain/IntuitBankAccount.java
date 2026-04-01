package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Hand-written business logic
 */
public class IntuitBankAccount extends BaseIntuitBankAccount {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static IntuitBankAccount findIntuitBankAccount(TransactionType pTransactionType, CreditDebitCode pCreditDebitCd) {
        IntuitBankAccountTransactionType intuitBankAccountTransactionType = IntuitBankAccountTransactionType.findIntuitBankAccountTransactionType(pTransactionType, pCreditDebitCd, PSPDate.getPSPTime());

        if (intuitBankAccountTransactionType != null) {
            return intuitBankAccountTransactionType.getIntuitBankAccount();
        }
        return null;
    }

    public static IntuitBankAccount findIntuitBankAccount(BankAccount pBankAccount) {
        for (IntuitBankAccount intuitBankAccount : findIntuitBankAccounts()) {
            if (intuitBankAccount.getBankAccount().equals(pBankAccount)) {
                return intuitBankAccount;
            }
        }

        return null;
    }

    public static IntuitBankAccount findIntuitBankAccount(TransactionTypeCode pTransactionType, CreditDebitCode pCreditDebitCd) {
        return findIntuitBankAccount(Application.<TransactionType>findById(TransactionType.class, pTransactionType), pCreditDebitCd);
    }

    public static DomainEntitySet<IntuitBankAccount> findIntuitBankAccounts() {
        /*
        We can cache this because IntuitBankAccounts are static (they are not added to at runtime)
         */
        DomainEntitySet<IntuitBankAccount> intuitBankAccounts = Application.getSessionCache().getEntityCollection(IntuitBankAccount.class, "intuitBankAccounts");
        if (intuitBankAccounts == null) {
            intuitBankAccounts = Application.find(IntuitBankAccount.class);
            Application.getSessionCache().addEntityCollection(IntuitBankAccount.class, "intuitBankAccounts", intuitBankAccounts);
        }

        return intuitBankAccounts;
    }

    public static IntuitBankAccount findIntuitBankAccountByName(Name pName) {
        DomainEntitySet<IntuitBankAccount> intuitBankAccounts =
                findIntuitBankAccounts().find(IntuitBankAccount.Description().equalTo(pName.toString())
                        .And(IntuitBankAccount.ExpDttm().isNull().Or(IntuitBankAccount.ExpDttm().greaterThan(PSPDate.getPSPTime()))));
        if(intuitBankAccounts.size() == 0) {
            throw new RuntimeException("Intuit bank account with description '" + pName.toString() + "' does not exist or is expired.");
        }

        return intuitBankAccounts.get(0);
    }

    public static IntuitBankAccount findIntuitBankAccountByName(String pName) {
        DomainEntitySet<IntuitBankAccount> intuitBankAccounts =
                findIntuitBankAccounts().find(IntuitBankAccount.Description().equalTo(pName));
        if(intuitBankAccounts.size() == 0) {
            throw new RuntimeException("Intuit bank account with description '" + pName + "' does not exist.");
        }

        return intuitBankAccounts.get(0);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public IntuitBankAccount()
	{
		super();
	}

    public enum Name {
        INTUIT_CHECK("INTUIT CHECK"),
        INTUIT_FEE("INTUIT FEE"),
        INTUIT_EE_RETURN("INTUIT EE RETURN"),
        INTUIT_ER_RETURN("INTUIT ER RETURN"),
        INTUIT_DD("INTUIT DD"),
        INTUIT_TAX("INTUIT TAX");

        Name(String pName) {
            mStringName = pName;
        }

        private String mStringName;

        @Override
        public String toString() {
            return mStringName;
        }
    }
}
