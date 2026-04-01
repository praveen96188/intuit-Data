package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Arrays;

/**
 * User: dweinberg
 * Date: 4/26/11
 * Time: 12:50 PM
 */
public class ReissuePayrollTaxPayment extends Process {

    private SourceSystemCode sourceSystemCode;
    private String sourceCompanyId;
    private String sourcePayrollRunId;
    private String transferTransactionId;

    private Company company;
    private PayrollRun payrollRun;
    private FinancialTransaction voidTaxTransfer;

    public ReissuePayrollTaxPayment(SourceSystemCode sourceSystemCode, String sourceCompanyId, String sourcePayrollRunId, String transferTransactionId) {
        this.sourceSystemCode = sourceSystemCode;
        this.sourceCompanyId = sourceCompanyId;
        this.sourcePayrollRunId = sourcePayrollRunId;
        this.transferTransactionId = transferTransactionId;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCode, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (sourcePayrollRunId == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.PayrollRun, sourceCompanyId, "PayrollRunId");
            return validationResult;
        }

        //Check if company exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCode);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCode.toString(), sourceCompanyId);
            return validationResult;
        }

        //Check if payroll run exists
        payrollRun = PayrollRun.findPayrollRun(company,
                sourcePayrollRunId);

        if (payrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun,
                    sourcePayrollRunId, sourcePayrollRunId,
                    sourceSystemCode.toString(), sourceCompanyId);

            return validationResult;
        }

        if (transferTransactionId == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.PayrollRun, sourcePayrollRunId, "transferTransactionId");
            return validationResult;
        }

        voidTaxTransfer = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transferTransactionId));

        if (voidTaxTransfer == null || voidTaxTransfer.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.IntuitTaxVoidTransfer) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.PayrollRun, sourcePayrollRunId, "transferTransactionId");
            return validationResult;
        }

        ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.ReissuePayrollTaxPayment);
        if (! voidTaxTransfer.isValidAction(actionEvent)) {
            validationResult.getMessages().ActionNotValidForFinancialTransaction(
                    EntityName.FinancialTransaction,
                    voidTaxTransfer.getId().toString(),
                    actionEvent.getCode().toString(),
                    voidTaxTransfer.getId().toString(),
                    voidTaxTransfer.getTransactionType().getTransactionTypeCd().toString(),
                    voidTaxTransfer.calculateCurrentTransactionState().getTransactionStateCd().toString());
            return validationResult;
        }




        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        SpcfDecimal totalAmount = SpcfMoney.ZERO;
        SpcfDecimal atdAmount = SpcfMoney.ZERO;
        for (FinancialTransaction atc : FinancialTransaction.findFinTxnNotReissuedForPayroll(
                payrollRun,
                Arrays.asList(TransactionTypeCode.AgencyTaxCredit),
                Arrays.asList(TransactionStateCode.Voided))) {

            totalAmount = totalAmount.add(atc.getFinancialTransactionAmount());

            createFromVoidedTransaction(atc);
        }

        for (FinancialTransaction atd : FinancialTransaction.findFinTxnNotReissuedForPayroll(
                payrollRun,
                Arrays.asList(TransactionTypeCode.AgencyTaxDebit),
                Arrays.asList(TransactionStateCode.Voided))) {

            totalAmount = totalAmount.subtract(atd.getFinancialTransactionAmount());
            atdAmount = atdAmount.add(atd.getFinancialTransactionAmount());

            createFromVoidedTransaction(atd);
        }

        //sanity check amounts
        if (! voidTaxTransfer.getFinancialTransactionAmount().equals(totalAmount)) {
            throw new RuntimeException(String.format("Amounts do not match: transfer: %s; sum: %s", voidTaxTransfer.getFinancialTransactionAmount(), totalAmount));
        }

        IntuitBankAccount creditIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(TransactionTypeCode.ReissueTaxLiabilityTransfer, CreditDebitCode.Credit);
        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(TransactionTypeCode.ReissueTaxLiabilityTransfer, CreditDebitCode.Debit);

        FinancialTransaction.createFinancialTransaction(company, payrollRun, null,
                creditIntuitBankAccount.getBankAccount(), debitIntuitBankAccount.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                TransactionTypeCode.ReissueTaxLiabilityTransfer,
                new SpcfMoney(totalAmount),
                SettlementType.ACH,
                FinancialTransaction.getSettlementDate(TransactionTypeCode.ReissueTaxLiabilityTransfer, company.getOffloadGroup()),
                null,
                voidTaxTransfer,
                0);

        if (atdAmount.isGreaterThan(SpcfMoney.ZERO)) {
            SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
            CalendarUtils.clearTime(settlementDate);

            FinancialTransaction offsetTransaction = FinancialTransaction.createFinancialTransaction(company, payrollRun, null,
                    null, null,
                    null, null,
                    TransactionTypeCode.ReissueAgencyTaxDebitOffset,
                    new SpcfMoney(atdAmount),
                    SettlementType.Other,
                    settlementDate,
                    null,
                    voidTaxTransfer,
                    0);

            offsetTransaction.updateFinancialTransactionState(TransactionStateCode.Executed);
            offsetTransaction.updateFinancialTransactionState(TransactionStateCode.Completed);
        }

        CompanyEvent.createPayrollTaxPaymentReissuedEvent(payrollRun, totalAmount);

        return processResult;
    }

    private FinancialTransaction createFromVoidedTransaction(FinancialTransaction voidedTransaction) {
        FinancialTransaction newTransaction = FinancialTransaction.createFinancialTransaction(
                voidedTransaction.getCompany(),
                voidedTransaction.getPayrollRun(),
                voidedTransaction.getPaycheckSplit(),
                voidedTransaction.getCreditBankAccount(),
                voidedTransaction.getDebitBankAccount(),
                voidedTransaction.getCreditBankAccountType(),
                voidedTransaction.getDebitBankAccountType(),
                voidedTransaction.getTransactionType().getTransactionTypeCd(),
                voidedTransaction.getFinancialTransactionAmount(),
                voidedTransaction.getSettlementTypeCd(),
                voidedTransaction.getSettlementDate(),
                voidedTransaction.getLaw());

        newTransaction.setOriginalTransaction(voidedTransaction);
        voidedTransaction.addAssociatedTransactions(newTransaction);

        return newTransaction;
    }
}
