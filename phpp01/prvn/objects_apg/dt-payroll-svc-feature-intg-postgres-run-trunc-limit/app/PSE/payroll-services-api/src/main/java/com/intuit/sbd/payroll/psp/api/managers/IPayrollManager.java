package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;

import java.util.Collection;
import java.util.List;

/**
 * This is the PSP service API that deals with all payroll related information
 * <p>The API includes:
 * <p>Submitting a payroll
 * <p>Canceling/Reversing/Recalling payroll transactions
 * <p>Synchronizing the Source Payroll System with PSP
 */
public interface IPayrollManager {
    /**
     * Payroll submission - paychecks, payckecksplits, liabilities, etc
     *
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pPayrollRun
     * @return
     */
    ProcessResult<PayrollRun> submitPayroll(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, PayrollRunDTO pPayrollRun);
    ProcessResult<PayrollRun> submitPayroll(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, PayrollRunDTO pPayrollRun, String pTransmissionId);
    ProcessResult changePaycheckSourceIds(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourcePayrollId);

    ProcessResult<PayrollRun> updatePayroll(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, PayrollRun pPayrollRun, Collection<PaycheckDTO> pPaychecks);
    ProcessResult<PayrollRun> updatePayroll(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, PayrollRun pPayrollRun, Collection<PaycheckDTO> pPaychecks, String pTransmissionId);

    ProcessResult reverseTransaction(SourceSystemCode pSourceSystemCd, String pCompanyId, TransactionReverseDTO pDto);

    ProcessResult<TransactionResponse> cancelEmployeeTransaction(SourceSystemCode pSourceSystem, String pSourceCompanyId, TransactionCancelEEDTO pTransactionRecall);

    ProcessResult<DomainEntitySet<TransactionResponse>> syncTransactions(SourceSystemCode pSourceSystem, String pSourceCompanyId, long pToken);

    ProcessResult<DomainEntitySet<SourcePayrollParameter>> updateSourcePayrollParameter(SourceSystemCode pSourceSystemCode, List<SourcePayrollParameterDTO> pParams);
    ProcessResult<SourcePayrollParameter> updateSourcePayrollParameter(SourceSystemCode pSourceSystemCode, SourcePayrollParameterCode pParameterCode, String pValue);

    ProcessResult<SourcePayrollParameter> updateDDAutoLimitIncreaseTiers(String pLimitRuleId, DDAutoLimitIncreaseTierDTO[] pAutoLimitTiers);

    /**
     * Updates a Paycheck VoidedAfterOffload Indicator
     * @param pSourceSystemCd
     * @param pSourceCompanyId
     * @param pSourcePaycheckId
     * @param pVoidedAfterOffload
     * @return
     */
    ProcessResult<Paycheck> updateVoidedAfterOffload(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pSourcePaycheckId, Boolean pVoidedAfterOffload, String pTransmissionId);


    ProcessResult<NACHAFile> confirmNACHAFile(String pNACHAFileId, String pConfirmationCode);

    ProcessResult modifyWireExpectedDate(SourceSystemCode pSourceSystem, String pSourceCompanyId, ModifyWireExpectedDTO pModifyWireExpectedDTO);

    ProcessResult<CompanyAdjustmentSubmission> voidPayroll(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, VoidPayrollDTO pVoidPayrollDTO);
    ProcessResult<CompanyAdjustmentSubmission> voidPayroll(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, VoidPayrollDTO pVoidPayrollDTO, String pTransmissionId);
    
    ProcessResult deletePaycheck(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String sourcePaycheckId, String transmissionId);

    ProcessResult<PayrollRun> updateQBPayrollInfo(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, PayrollRunDTO pPayrollRunDTO);
    ProcessResult<PayrollRun> updateQBPayrollInfo(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, PayrollRunDTO pPayrollRunDTO, boolean pPaycheckOnly);

    ProcessResult<QbdtTransactionInfo> updateQBTransactionInfo(QBDTTransactionInfoDTO pQBDTTransactionInfoDTO);

    ProcessResult<CompanyAdjustmentSubmission> updateQBLiabilityAdjustmentInfo(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO);

    ProcessResult<Collection<CompanyAdjustmentSubmission>> voidLiabilityAdjustments(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, Collection<String> pCompanyAdjustmentSubmissionSourceIds, boolean pRecall);

    ProcessResult<CompanyAdjustmentSubmission> addLiabilityAdjustments(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPayrollRunId, CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO, DateDTO pLiabilityAdjustmentDate, LiabilityAdjustmentOptionsDTO pLiabilityAdjustmentOptionsDTO);
    ProcessResult<CompanyAdjustmentSubmission> addLiabilityAdjustments(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPayrollRunId, CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO, DateDTO pLiabilityAdjustmentDate, LiabilityAdjustmentOptionsDTO pLiabilityAdjustmentOptionsDTO, String pTransmissionId);
    ProcessResult<CompanyAdjustmentSubmission> addLiabilityAdjustments(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPayrollRunId, CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO, DateDTO pLiabilityAdjustmentDate, LiabilityAdjustmentOptionsDTO pLiabilityAdjustmentOptionsDTO, String pTransmissionId, boolean pIsNotPartOfPayrollSubmission);

    ProcessResult<PayrollRun> addCustomerTaxPayment(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, CustomerTaxPaymentDTO pCustomerTaxPaymentDTO);

    ProcessResult voidPayrollTaxPayment(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPayrollRunId);
    ProcessResult reissuePayrollTaxPayment(SourceSystemCode sourceSystemCode, String sourceCompanyId, String sourcePayrollRunId, String transferTransactionId);
    ProcessResult applyERPayableToBalanceDue(SourceSystemCode sourceSystemCode, String sourceCompanyId, String payrollRunId, SpcfDecimal amountToApply);
}
