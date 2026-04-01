package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Map;

/**
 * User: dweinberg
 * Date: 4/27/11
 * Time: 10:05 AM
 * Applies part or all of a company's ER Payable to a single payroll's balance due for tax transactions only
 */
public class ApplyERPayableToBalanceDue extends Process {

    private SourceSystemCode sourceSystemCode;
    private String sourceCompanyId;
    private String payrollRunId;
    private SpcfDecimal amountToApply;

    private Company company;
    private PayrollRun payrollRun;

    public ApplyERPayableToBalanceDue(SourceSystemCode sourceSystemCode, String sourceCompanyId, String payrollRunId, SpcfDecimal amountToApply) {
        this.sourceSystemCode = sourceSystemCode;
        this.sourceCompanyId = sourceCompanyId;
        this.payrollRunId = payrollRunId;
        this.amountToApply = amountToApply;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCode, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (payrollRunId == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.PayrollRun, payrollRunId, "PayrollRunId");
            return validationResult;
        }

        company = Company.findCompany(sourceCompanyId, sourceSystemCode);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCode.toString(), sourceCompanyId);
            return validationResult;
        }

        payrollRun = Application.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        if (payrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun, payrollRunId,
                    payrollRunId, sourceSystemCode.toString(), sourceCompanyId);

            return validationResult;
        }

        if (payrollRun.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.ERPayableAppliedBalanceDue).size() > 0) {
            validationResult.getMessages().CreateTransactionFailurePendingLedgerActivity(EntityName.PayrollRun, payrollRunId);
            return validationResult;
        }

        //validate amountToApply
        if (amountToApply == null || !amountToApply.isGreaterThan(SpcfMoney.ZERO)) {
            validationResult.getMessages().AmountNotPositive(EntityName.PayrollRun, payrollRunId);
        }

        SpcfDecimal erPayableAmount = LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERPayable);

        if (amountToApply.isGreaterThan(erPayableAmount)) {
            validationResult.getMessages().GenericError(EntityName.PayrollRun, payrollRunId, "Amount exceeds ER Payable");
        }

        SpcfDecimal unCollectedAmount = getTransactionSum(payrollRun.getUncollectedTaxAmount());

        if (amountToApply.isGreaterThan(unCollectedAmount)) {
            validationResult.getMessages().GenericError(EntityName.PayrollRun, payrollRunId, "Amount exceeds uncollected amount");
        }

        return validationResult;
    }

    private SpcfDecimal getTransactionSum(Map<FinancialTransaction, SpcfMoney> transactions) {
        SpcfDecimal sum = SpcfMoney.ZERO;
        for (SpcfMoney money : transactions.values()) {
            sum = sum.add(money);
        }
        return sum;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        FinancialTransaction originalDebit = payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit).get(0);

        TransactionType transactionType = TransactionType.findTransactionType(TransactionTypeCode.ERPayableAppliedBalanceDue);

        IntuitBankAccount creditIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Credit);
        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit);

        FinancialTransaction.createFinancialTransaction(company, payrollRun, null,
                creditIntuitBankAccount.getBankAccount(), debitIntuitBankAccount.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                TransactionTypeCode.ERPayableAppliedBalanceDue, new SpcfMoney(amountToApply), SettlementType.ACH,
                FinancialTransaction.getSettlementDate(TransactionTypeCode.ERPayableAppliedBalanceDue,
                        company.getOffloadGroup()),
                null,
                originalDebit,
                0);

        CompanyEvent.createERPayableAppliedToBalanceDueEvent(payrollRun, amountToApply);

        return processResult;
    }
}
