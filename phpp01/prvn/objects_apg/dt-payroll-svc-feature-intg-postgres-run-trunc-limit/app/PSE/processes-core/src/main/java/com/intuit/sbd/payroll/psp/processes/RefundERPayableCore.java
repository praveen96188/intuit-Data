package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.DDProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: dweinberg
 * Date: 2/7/11
 * Time: 9:22 AM
 */
public class RefundERPayableCore extends Process {

    private SourceSystemCode sourceSystemCode;
    private String sourceCompanyId;
    private SettlementTypeDTO settlementTypeDTO;
    private SpcfMoney amount;

    private Company company;
    private CompanyBankAccount cba;
    private SpcfDecimal erPayableBalance;
    private SettlementType settlementType;


    public RefundERPayableCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, SettlementTypeDTO pSettlementTypeDTO, SpcfMoney pAmount) {
        sourceSystemCode = pSourceSystemCode;
        sourceCompanyId = pSourceCompanyId;
        settlementTypeDTO = pSettlementTypeDTO;
        amount = pAmount;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCode, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        company = Company.findCompany(sourceCompanyId, sourceSystemCode);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCode.toString(), sourceCompanyId);
            return validationResult;
        }


        settlementType = DDProcessesToDTO.getDomainSettlementType(settlementTypeDTO);

        if (settlementType == null) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "SettlementType");
            return validationResult;
        }

        erPayableBalance = LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERPayable);
        if (!erPayableBalance.isGreaterThan(SpcfMoney.ZERO)) {
            validationResult.getMessages().GenericError(EntityName.Company, sourceCompanyId, "ERPayable balance must be positive.");
            return validationResult;
        }

        //if there is anything (a debit) in return receivable, they owe us money so shouldn't be paid a refund
        if (LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERReturnReceivable).isGreaterThan(SpcfMoney.ZERO)) {
            validationResult.getMessages().GenericError(EntityName.Company, sourceCompanyId, "Refund cannot be made with balance in ERReturnReceivable.  Apply ERPayable to balance due instead.");
            return validationResult;
        }

        if (amount.isGreaterThan(erPayableBalance)) {
            validationResult.getMessages().GenericError(EntityName.Company, sourceCompanyId, "Refund cannot be made with a greater amount than ER Payable Amount");
        }

        cba = CompanyBankAccount.findActiveCompanyBankAccount(company);

        if (cba == null) {
            validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                    sourceCompanyId, sourceSystemCode.toString(), sourceCompanyId);
            return validationResult;
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult<FinancialTransaction> processResult = new ProcessResult<FinancialTransaction>();

        IntuitBankAccount iba = IntuitBankAccount.findIntuitBankAccount(
                TransactionType.findTransactionType(TransactionTypeCode.EmployerTaxCredit),
                CreditDebitCode.Debit);


        SpcfCalendar refundSettlementDate = PayrollTaxHelper.getRefundSettlementDate(company);
        CalendarUtils.clearTime(refundSettlementDate);

        FinancialTransaction ft = FinancialTransaction.createFinancialTransaction(company,
                null,
                null,
                cba.getBankAccount(),
                iba.getBankAccount(),
                BankAccountOwnerType.Company,
                BankAccountOwnerType.Intuit,
                TransactionTypeCode.EmployerTaxCredit,
                new SpcfMoney(amount),
                settlementType,
                refundSettlementDate);

        if (settlementType != SettlementType.ACH) {
            ft.updateFinancialTransactionState(TransactionStateCode.Executed);
            ft.updateFinancialTransactionState(TransactionStateCode.Completed);
        }

        CompanyEvent.createERPayableRefundCreatedEvent(company, ft);

        processResult.setResult(ft);
        return processResult;
    }

}
