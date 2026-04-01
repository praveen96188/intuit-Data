package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.payroll.authorization.utils.RequestSourceIdentifier;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jun 3, 2008
 * Time: 10:41:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class OverrideVerificationTransactionAmounts extends Process implements IProcess {
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private String sourceCompanyBankAccountId;
    private SpcfMoney amount1;
    private SpcfMoney amount2;
    private SpcfCalendar settlementDate;
    private Company company;
    private CompanyBankAccount companyBankAccount;
    DomainEntitySet<FinancialTransaction> verificationTransactions;
    private RequestSourceIdentifier requestSourceIdentifier;


    public OverrideVerificationTransactionAmounts(SourceSystemCode pSourceSystemCode,
                                                 String pSourceCompanyId,
                                                 String pSourceCompanyBankAccountId,
                                                 SpcfMoney pAmount1,
                                                 SpcfMoney pAmount2,
                                                 SpcfCalendar pSettlementDate) {

        sourceSystemCd = pSourceSystemCode;
        sourceCompanyId = pSourceCompanyId;
        sourceCompanyBankAccountId = pSourceCompanyBankAccountId;
        amount1 = pAmount1;
        amount2 = pAmount2;
        settlementDate = pSettlementDate;
        this.requestSourceIdentifier = PayrollApplicationBeanFactory.getBean(RequestSourceIdentifier.class);

    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        company = Company.findCompany(sourceCompanyId, sourceSystemCd);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, sourceCompanyBankAccountId);
        if (companyBankAccount == null) {
            validationResult.getMessages().CompanyBankAccountDoesNotExist(EntityName.CompanyBankAccount,
                    sourceCompanyBankAccountId, sourceCompanyBankAccountId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        // Verify that company bank account has a status of Pending Verification
        if (companyBankAccount.getStatusCd() != BankAccountStatus.PendingVerification) {
            validationResult.getMessages().CompanyBankAccountStatusNotPendingVerification(
                    EntityName.CompanyBankAccount, sourceCompanyBankAccountId, sourceCompanyBankAccountId,
                    sourceSystemCd.toString(), sourceCompanyId,
                    companyBankAccount.getStatusCd().toString());
            return validationResult;
        }

        // verify amounts are less than 1.00 and greater than 0
        float amt1 = SpcfUtils.convertToBigDecimal(amount1).floatValue();
        float amt2 = SpcfUtils.convertToBigDecimal(amount2).floatValue();
        if (amt1 <= 0 || amt1 >= 1  || amt2 <= 0 || amt2 >= 1) {
            validationResult.getMessages().InvalidValue(EntityName.CompanyBankAccount, sourceCompanyBankAccountId, "VerificationAmount");
            return validationResult;
        }

        
        verificationTransactions = companyBankAccount.getVerificationTransactions();

        if(isMoneyMovementOnboardingEnabled()){
            return validationResult;
        }
        if (verificationTransactions.size() < 2) {
            validationResult.getMessages().CompanyBankAccountNoRecentVerificationTxns(EntityName.CompanyBankAccount,
                        sourceCompanyBankAccountId, sourceCompanyBankAccountId,
                        sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }
        if (verificationTransactions.get(0).getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Created ||
                verificationTransactions.get(1).getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Created) {
            validationResult.getMessages().CompanyBankAccountNoRecentVerificationTxns(EntityName.CompanyBankAccount,
                        sourceCompanyBankAccountId, sourceCompanyBankAccountId,
                        sourceSystemCd.toString(), sourceCompanyId);
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if(isMoneyMovementOnboardingEnabled()){
            return processResult;
        }

        FinancialTransaction verificationTx1 = verificationTransactions.get(0);
        FinancialTransaction verificationTx2 = verificationTransactions.get(1);

        TransactionState executedState = PayrollServices.entityFinder
                .findById(TransactionState.class, TransactionStateCode.Executed);        

        verificationTx1.setFinancialTransactionAmount(amount1);
        verificationTx1.setSettlementDate(settlementDate);
        verificationTx1.updateFinancialTransactionState(TransactionStateCode.Executed);

        verificationTx2.setFinancialTransactionAmount(amount2);
        verificationTx2.setSettlementDate(settlementDate);
        verificationTx2.updateFinancialTransactionState(TransactionStateCode.Executed);

        // update money movement transactions and add entry details
        updateFinancialTransaction(verificationTx1);
        updateFinancialTransaction(verificationTx2);

        return processResult;
    }

    private static void updateFinancialTransaction(FinancialTransaction pFinTxn) {
        MoneyMovementTransaction moneyMovementTxn = pFinTxn.getMoneyMovementTransaction();
        // delete existing money movement transaction
        if (moneyMovementTxn != null) {
            pFinTxn.setMoneyMovementTransaction(null);
            MoneyMovementTransaction.deleteMoneyMovementTransaction(moneyMovementTxn);
        }
        // add new money movement transaction
        MoneyMovementTransaction.createMoneyMovementTransaction(pFinTxn);
    }

    private boolean isMoneyMovementOnboardingEnabled() {
        return requestSourceIdentifier.isPayrollPlugin() || company.isMoneyMovementOnboardingEnabled();
    }

}
