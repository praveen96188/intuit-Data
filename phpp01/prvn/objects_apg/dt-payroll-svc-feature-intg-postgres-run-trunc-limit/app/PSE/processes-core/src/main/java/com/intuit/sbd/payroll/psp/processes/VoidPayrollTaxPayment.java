package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: dweinberg
 * Date: 4/11/11
 * Time: 11:40 AM
 * This represents an agent voiding tax so that PSP will never pay the agency.  This is primarily when the customer has not and will not pay Intuit.
 * It could also be for fraud.
 */
public class VoidPayrollTaxPayment extends Process {

    private SourceSystemCode sourceSystemCode;
    private String sourceCompanyId;
    private String payrollRunId;

    private Company company;
    private PayrollRun payrollRun;


    public VoidPayrollTaxPayment(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPayrollRunId) {
        sourceSystemCode = pSourceSystemCode;
        sourceCompanyId = pSourceCompanyId;
        payrollRunId = pPayrollRunId;
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

        if (!payrollRun.validateAction(ActionEventCode.VoidPayrollTaxPayment)) {
            validationResult.getMessages().ActionNotValidForPayrollRunLedgerAccount(EntityName.PayrollRun, payrollRun.getSourcePayRunId(), ActionEventCode.EEReturnTransfer.toString(), payrollRun.getSourcePayRunId());
            return validationResult;
        }

        if (payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ERPayableAppliedBalanceDue, TransactionTypeCode.IntuitTaxVoidTransfer},
                new TransactionStateCode[]{TransactionStateCode.Created}).size() > 0) {
            validationResult.getMessages()
                    .CreateTransactionFailurePendingLedgerActivity(EntityName.FinancialTransaction,
                            payrollRun.getSourcePayRunId());

        }

        if (Application.find(MoneyMovementTransaction.class,
                MoneyMovementTransaction.Company().equalTo(company).
                        And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.InProcess)))
                .size() > 0){
            validationResult.getMessages()
                    .CreateTransactionFailurePendingLedgerActivity(EntityName.FinancialTransaction,
                            payrollRun.getSourcePayRunId());
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        SpcfDecimal agencyTaxAmount = SpcfMoney.ZERO;
        for (FinancialTransaction agencyTaxDebit : payrollRun.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.AgencyTaxDebit)) {
            agencyTaxAmount = agencyTaxAmount.subtract(agencyTaxDebit.getFinancialTransactionAmount());
            agencyTaxDebit.updateFinancialTransactionState(TransactionStateCode.Voided);
        }
        for (FinancialTransaction agencyTaxCredit : payrollRun.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.AgencyTaxCredit)) {
            agencyTaxAmount = agencyTaxAmount.add(agencyTaxCredit.getFinancialTransactionAmount());
            agencyTaxCredit.updateFinancialTransactionState(TransactionStateCode.Voided);
        }

        //sanity check
        if (! agencyTaxAmount.isGreaterThan(SpcfMoney.ZERO)) {
            throw new RuntimeException("Cannot void for non-positive amount");
        }

        TransactionType transactionType = TransactionType.findTransactionType(TransactionTypeCode.IntuitTaxVoidTransfer);

        IntuitBankAccount creditIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Credit);
        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit);

        FinancialTransaction.createFinancialTransaction(company, payrollRun, null,
                creditIntuitBankAccount.getBankAccount(), debitIntuitBankAccount.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                TransactionTypeCode.IntuitTaxVoidTransfer, new SpcfMoney(agencyTaxAmount), SettlementType.ACH,
                FinancialTransaction.getSettlementDate(TransactionTypeCode.IntuitTaxVoidTransfer,
                        company.getOffloadGroup()));

        CompanyEvent.createPayrollTaxPaymentVoidedEvent(payrollRun, agencyTaxAmount);

        return processResult;
    }
}
