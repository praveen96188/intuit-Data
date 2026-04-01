package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 2/17/12
 * Time: 1:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddBookTransferTransactionCore extends Process {

    private String mFromAccount;
    private String mToAccount;
    private SpcfMoney mAmount;

    private BankAccount mDebitAccount;
    private BankAccount mCreditAccount;


    public AddBookTransferTransactionCore(String pFromAccount, String pToAccount, SpcfMoney pAmount) {
        mFromAccount = pFromAccount;
        mToAccount = pToAccount;
        mAmount = pAmount;
    }

    @Override
    public ProcessResult<FinancialTransaction> process() {
        ProcessResult<FinancialTransaction> processResult = new ProcessResult<FinancialTransaction>();

        Company company = Company.getBookTransferCompany();

        // create the FT
        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(company, null, null, mCreditAccount, mDebitAccount,
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                TransactionTypeCode.GlobalBookTransfer, mAmount, SettlementType.ACH, FinancialTransaction.getSettlementDate(company.getOffloadGroup()));

        processResult.setResult(financialTransaction);

        return processResult;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if (StringUtils.isEmpty(mFromAccount)) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "Debit account is null");
            return validationResult;
        }

        if (StringUtils.isEmpty(mToAccount)) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "Credit account is null");
            return validationResult;
        }

        if (mAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "Transaction Amount");
            return validationResult;
        }

        IntuitBankAccount intuitBankAccount = IntuitBankAccount.findIntuitBankAccountByName(mFromAccount);
        if (intuitBankAccount == null) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "Debit account");
            return validationResult;
        }
        mDebitAccount = intuitBankAccount.getBankAccount();

        intuitBankAccount = IntuitBankAccount.findIntuitBankAccountByName(mToAccount);
        if (intuitBankAccount == null) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "Credit account");
            return validationResult;
        }
        mCreditAccount = intuitBankAccount.getBankAccount();

        return validationResult;
    }
}
