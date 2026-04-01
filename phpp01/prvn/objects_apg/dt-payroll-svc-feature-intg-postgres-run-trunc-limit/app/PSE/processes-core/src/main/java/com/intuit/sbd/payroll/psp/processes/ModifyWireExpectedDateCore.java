package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.ModifyWireExpectedDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jul 2, 2008
 * Time: 9:55:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyWireExpectedDateCore extends Process implements IProcess {
    private SourceSystemCode sourceSystemCode;
    private String sourceCompanyId;
    private ModifyWireExpectedDTO wireExpectedDTO;

    private Company company;
    private PayrollRun payrollRun;
    private ActionEvent actionEvent;
    private ArrayList<CancelTransactionCore> cancelTransactionCoreProcesses;

    public ModifyWireExpectedDateCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                      ModifyWireExpectedDTO pWireExpectedDTO) {
        sourceSystemCode = pSourceSystemCode;
        sourceCompanyId = pSourceCompanyId;
        wireExpectedDTO = pWireExpectedDTO;
        cancelTransactionCoreProcesses = new ArrayList<CancelTransactionCore>();
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCode, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (wireExpectedDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollRun, null, "WireExpectedDTO");
            return validationResult;
        }

        // validate Wire expected date DTO
        validationResult.merge(wireExpectedDTO.validate());
        if (!validationResult.isSuccess()) {
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
        payrollRun = PayrollRun.findPayrollRun(company, wireExpectedDTO.getSourcePayrollRunId());

        if (payrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun,
                    wireExpectedDTO.getSourcePayrollRunId(),
                    wireExpectedDTO.getSourcePayrollRunId(), sourceSystemCode.toString(), sourceCompanyId);

            return validationResult;
        }

        // verify the Wire expected date is a future banking day
        SpcfCalendar wireExpectedDate = DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate());
        SpcfCalendar today = PSPDate.getPSPTime();
        CalendarUtils.clearTime(today);
        if (wireExpectedDate.before(today) || CalendarUtils.isWeekendOrHoliday(wireExpectedDate)) {
            validationResult.getMessages().SettlementDateNotFutureBankingDay(EntityName.Date, null,
                    DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate()).toString(), SettlementType.Wire.toString());
            return validationResult;
        }

        // Check whether this is a valid action
        PayrollStatus statusCode = payrollRun.getPayrollRunStatus();
        actionEvent = Application.findById(ActionEvent.class, wireExpectedDTO.getActionEventCode());
        if (!payrollRun.validateAction(actionEvent)) {
            validationResult.getMessages().ActionNotValidForPayrollRun(EntityName.PayrollRun,
                    wireExpectedDTO.getSourcePayrollRunId(),
                    actionEvent.getCode().toString(), wireExpectedDTO.getSourcePayrollRunId(), statusCode.toString());
            return validationResult;
        }

        //If the payroll is in the PendingReversals state, create a new CancelTransactionCore proces for each EmployeeDDReversal transaction in a Created state
        //The process will be "processed" in this class's process step
        if (PayrollStatus.PendingReversals.equals(payrollRun.getPayrollRunStatus())) {
            TransactionState createdState = Application.findById(TransactionState.class, TransactionStateCode.Created);
            TransactionType eeDDReversal = Application.findById(TransactionType.class, TransactionTypeCode.EmployeeDdReversalDebit);
            DomainEntitySet<FinancialTransaction> reversalFinTxns = payrollRun.getFinancialTransactions(createdState, eeDDReversal);
            for (FinancialTransaction currFinTxn : reversalFinTxns) {
                CancelTransactionCore currCancelTxCore = new CancelTransactionCore(sourceSystemCode, sourceCompanyId, currFinTxn.getId().toString(), true);
                validationResult.merge(currCancelTxCore.validate());
                cancelTransactionCoreProcesses.add(currCancelTxCore);
            }
        }

        //If the payroll is in the PendingAutoRedebit or PendingRedebit state, create a new CancelTransactionCore proces for each redebit transaction in a Created state
        //The process will be "processed" in this class's process step
        if (PayrollStatus.PendingAutoRedebit.equals(payrollRun.getPayrollRunStatus()) ||
                PayrollStatus.PendingRedebit.equals(payrollRun.getPayrollRunStatus())) {
            TransactionState createdState = Application.findById(TransactionState.class, TransactionStateCode.Created);
            DomainEntitySet<FinancialTransaction> redebitFinTxns =
                    payrollRun.getFinancialTransactions(createdState, TransactionAssociationType.Redebit);
            for (FinancialTransaction currFinTxn : redebitFinTxns) {
                CancelTransactionCore currCancelTxCore =
                        new CancelTransactionCore(sourceSystemCode, sourceCompanyId, currFinTxn.getId().toString(), true);
                validationResult.merge(currCancelTxCore.validate());
                cancelTransactionCoreProcesses.add(currCancelTxCore);
            }

            DomainEntitySet<FinancialTransaction> debitReturnFees = FinancialTransaction.findFinancialTransactions(company,
                                                                                                                   payrollRun,
                                                                                                                   TransactionTypeCode.EmployerFeeDebit,
                                                                                                                   OfferingServiceChargeType.DebitReturnFee);
            for (FinancialTransaction debitReturnFee : debitReturnFees) {
                if (debitReturnFee.isPending()) {
                    CancelTransactionCore currCancelTxCore =
                           new CancelTransactionCore(sourceSystemCode, sourceCompanyId, debitReturnFee.getId().toString(), true, true, null);
                    validationResult.merge(currCancelTxCore.validate());
                    cancelTransactionCoreProcesses.add(currCancelTxCore);
                }
            }

            DomainEntitySet<FinancialTransaction> debitReturnSalesTax = FinancialTransaction.findFinancialTransactions(company,
                                                                                                                   payrollRun,
                                                                                                                   TransactionTypeCode.ServiceSalesAndUseTax,
                                                                                                                   OfferingServiceChargeType.DebitReturnFee);
            for (FinancialTransaction debitReturnFee : debitReturnSalesTax) {
                if (debitReturnFee.isPending()) {
                    CancelTransactionCore currCancelTxCore =
                            new CancelTransactionCore(sourceSystemCode, sourceCompanyId, debitReturnFee.getId().toString(), true);
                    validationResult.merge(currCancelTxCore.validate());
                    cancelTransactionCoreProcesses.add(currCancelTxCore);
                }
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        // need to check payroll status here before it's changed below
        if ((actionEvent.getCode().equals(ActionEventCode.DDRedebitEdit)) &&
                (PayrollStatus.PendingAutoRedebit.equals(payrollRun.getPayrollRunStatus()) ||
                        PayrollStatus.PendingRedebit.equals(payrollRun.getPayrollRunStatus()))) {
            CompanyEvent.createChangeRedebitToWireExpectedEvent(payrollRun, wireExpectedDTO.getCollectionStage().getCollectionStageCode(),
                    DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate()));
        } else {
            CompanyEvent.createWireExpectedEvent(company, payrollRun, wireExpectedDTO.getCollectionStage().getCollectionStageCode(),
                    DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate()));
        }

        // create last chance email company event, if required
        if (wireExpectedDTO.getLastChanceEmail()) {
            CompanyEvent.createLastChanceNotifyEvent(payrollRun,
                    DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate()));
        }

        // update the wire expected date of the payroll
        payrollRun.setWireExpectedDate(DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate()));
        if(wireExpectedDTO.getCollectionStage() != null) {
            payrollRun.setCollectionStageCd(wireExpectedDTO.getCollectionStage().getCollectionStageCode());
        }
        payrollRun.updatePayrollRunStatus(PayrollStatus.PendingWire);

        //Call the process method for all the CancelTransactionCore processes we created in validate
        for (CancelTransactionCore currCancelProc : cancelTransactionCoreProcesses) {
            processResult.merge(currCancelProc.process());
        }

        return processResult;
    }

}
