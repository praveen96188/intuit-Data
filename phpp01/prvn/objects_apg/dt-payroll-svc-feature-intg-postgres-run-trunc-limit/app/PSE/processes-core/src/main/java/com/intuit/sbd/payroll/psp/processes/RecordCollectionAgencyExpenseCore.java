package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: dweinberg
 * Date: 4/15/11
 * Time: 9:47 AM
 */
public class RecordCollectionAgencyExpenseCore extends Process {

    private SourceSystemCode sourceSystemCode;
    private String sourceCompanyId;
    private String payrollRunId;
    private SpcfMoney expenseAmount;
    private DateDTO settlementDate;

    private Company company;
    private PayrollRun payrollRun;


    public RecordCollectionAgencyExpenseCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPayrollRunId, SpcfMoney pExpenseAmount, DateDTO pSettlementDate) {
        sourceSystemCode = pSourceSystemCode;
        sourceCompanyId = pSourceCompanyId;
        payrollRunId = pPayrollRunId;
        expenseAmount = pExpenseAmount;
        settlementDate = pSettlementDate;
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

        validationResult.merge(settlementDate.validate());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (!expenseAmount.isGreaterThan(SpcfMoney.ZERO)) {
            validationResult.getMessages().AmountNotPositive(EntityName.PayrollRun, payrollRunId);
            return validationResult;
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        FinancialTransaction ft = FinancialTransaction.createFinancialTransaction(company, payrollRun, null,
                null, null,
                null, null,
                TransactionTypeCode.ThirdPartyCollectionExpense, new SpcfMoney(expenseAmount), SettlementType.CheckType,
                settlementDate.toSpcfCalendar());

        ft.updateFinancialTransactionState(TransactionStateCode.Executed);
        ft.updateFinancialTransactionState(TransactionStateCode.Completed);

        return processResult;
    }
}
