package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.managers.IPayrollManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author achaves
 *         Date: Nov 7, 2007
 *         Time: 10:19:33 PM
 */
class PayrollManager implements IPayrollManager {
    // Payroll Transactions

    public ProcessResult<PayrollRun> submitPayroll(
            SourceSystemCode pSourceSystemCode, String pSourceCompanyId, PayrollRunDTO pPayrollRun) {
        return submitPayroll(pSourceSystemCode, pSourceCompanyId, pPayrollRun, null);
    }

    public ProcessResult<PayrollRun> submitPayroll(
            SourceSystemCode pSourceSystemCode, String pSourceCompanyId, PayrollRunDTO pPayrollRun, String pTransmissionId) {
        ProcessResult<PayrollRun> processResult;
        PayrollSubmitCore processCore = new PayrollSubmitCore(pSourceSystemCode, pSourceCompanyId, pPayrollRun, pTransmissionId);
        processResult = processCore.execute();
        processResult.setResult(processCore.getPayrollRun());
        return processResult;
    }

    public ProcessResult changePaycheckSourceIds(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourcePayrollId) {
        return new ChangePaycheckSourceIdsCore(pSourceSystemCode, pSourceCompanyId, pSourcePayrollId).execute();
    }

    public ProcessResult<PayrollRun> updatePayroll(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, PayrollRun pPayrollRun, Collection<PaycheckDTO> pPaychecks) {
        return updatePayroll(pSourceSystemCd, pSourceCompanyId, pPayrollRun, pPaychecks, null);
    }

    public ProcessResult<PayrollRun> updatePayroll(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, PayrollRun pPayrollRun, Collection<PaycheckDTO> pPaychecks, String pTransmissionId) {
        UpdatePayrollCore processCore = new UpdatePayrollCore(pSourceSystemCd, pSourceCompanyId, pPayrollRun, pPaychecks, pTransmissionId);
        ProcessResult processResult = processCore.execute();
        return processResult;
    }

    public ProcessResult reverseTransaction(SourceSystemCode pSourceSystemCd, String pCompanyId, TransactionReverseDTO pDto) {
        return new TransactionReverseCore(pSourceSystemCd, pCompanyId, pDto).execute();
    }

    public ProcessResult<TransactionResponse> cancelEmployeeTransaction(
            SourceSystemCode pSourceSystem, String pSourceCompanyId, TransactionCancelEEDTO pTransactionRecall) {
        CancelEETransactionsCore processCore = new CancelEETransactionsCore(pSourceSystem, pSourceCompanyId, pTransactionRecall);

        ProcessResult<TransactionResponse> processResult = processCore.execute();

        processResult.setResult(processCore.getTransactionResponse());

        return processResult;

    }

    public ProcessResult<DomainEntitySet<TransactionResponse>> syncTransactions(
            SourceSystemCode pSourceSystem, String pSourceCompanyId, long pToken) {
        TransactionSyncCore processCore = new TransactionSyncCore(pSourceSystem, pSourceCompanyId, pToken);

        ProcessResult<DomainEntitySet<TransactionResponse>> processResult = processCore.execute();

        processResult.setResult(processCore.getTransactionResponses());

        return processResult;
    }

    public ProcessResult<DomainEntitySet<SourcePayrollParameter>> updateSourcePayrollParameter(
            SourceSystemCode pSourceSystemCode, List<SourcePayrollParameterDTO> pParams) {
        UpdateSourcePayrollParameterCore processCore = new UpdateSourcePayrollParameterCore(pSourceSystemCode, pParams);

        ProcessResult<DomainEntitySet<SourcePayrollParameter>> processResult = processCore.execute();

        return processResult;
    }

    public ProcessResult<SourcePayrollParameter> updateSourcePayrollParameter(SourceSystemCode pSourceSystemCode, SourcePayrollParameterCode pParameterCode, String pValue) {
        List<SourcePayrollParameterDTO> params = new ArrayList<SourcePayrollParameterDTO>();
        SourcePayrollParameterDTO param = new SourcePayrollParameterDTO(pSourceSystemCode, pParameterCode, pValue);
        params.add(param);
        UpdateSourcePayrollParameterCore processCore = new UpdateSourcePayrollParameterCore(pSourceSystemCode, params);
        ProcessResult<DomainEntitySet<SourcePayrollParameter>> pr = (ProcessResult<DomainEntitySet<SourcePayrollParameter>>) processCore.process();

        ProcessResult<SourcePayrollParameter> result = new ProcessResult<SourcePayrollParameter>();
        pr.merge(pr);
        if (pr.isSuccess()) {
            result.setResult(pr.getResult().findEntity(SourcePayrollParameter.SourceSystemCd().equalTo(pSourceSystemCode)
                                                       .And(SourcePayrollParameter.ParameterCd().equalTo(pParameterCode))));
        }
        return result;
    }

    public ProcessResult<SourcePayrollParameter> updateDDAutoLimitIncreaseTiers(String pLimitRuleId, DDAutoLimitIncreaseTierDTO[] pAutoLimitTiers) {
        UpdateDDAutoLimitIncreaseTiersCore processCore = new UpdateDDAutoLimitIncreaseTiersCore(pLimitRuleId, pAutoLimitTiers);
        ProcessResult processResult = processCore.execute();
        return processResult;
    }

    public ProcessResult<Paycheck> updateVoidedAfterOffload(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pSourcePaycheckId, Boolean pVoidedAfterOffload, String pTransmissionId) {
        IProcess processCore = new UpdateVoidedAfterOffloadCore(pSourceSystemCd, pSourceCompanyId, pSourcePaycheckId, pVoidedAfterOffload, pTransmissionId);
        ProcessResult<Paycheck> processResult = processCore.execute();
        return processResult;
    }

    public ProcessResult<NACHAFile> confirmNACHAFile(String pNACHAFileId, String pConfirmationCode) {
        ConfirmNACHAFileCore processCore = new ConfirmNACHAFileCore(pNACHAFileId, pConfirmationCode);
        ProcessResult<NACHAFile> processResult = processCore.execute();
        processResult.setResult(processCore.getNACHAFile());

        return processResult;
    }

    public ProcessResult modifyWireExpectedDate(SourceSystemCode pSourceSystem, String pSourceCompanyId, ModifyWireExpectedDTO pModifyWireExpectedDTO) {
        ModifyWireExpectedDateCore processCore =
                new ModifyWireExpectedDateCore(pSourceSystem, pSourceCompanyId, pModifyWireExpectedDTO);
        ProcessResult processResult = processCore.execute();
        return processResult;
    }

    public ProcessResult<CompanyAdjustmentSubmission> voidPayroll(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, VoidPayrollDTO pVoidPayrollDTO) {
        return voidPayroll(pSourceSystemCode, pSourceCompanyId, pVoidPayrollDTO, null);
    }

    public ProcessResult<CompanyAdjustmentSubmission> voidPayroll(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, VoidPayrollDTO pVoidPayrollDTO, String pTransmissionId) {
        VoidPayrollCore voidPayroll = new VoidPayrollCore(pSourceSystemCode, pSourceCompanyId, pVoidPayrollDTO, pTransmissionId);
        ProcessResult<CompanyAdjustmentSubmission> processResult = voidPayroll.execute();
        processResult.setResult(voidPayroll.getCompanyVoid());
        return processResult;
    }


    public ProcessResult deletePaycheck(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String sourcePaycheckId, String transmissionId) {
        DeletePaycheckCore deletePaycheck = new DeletePaycheckCore(pSourceSystemCode, pSourceCompanyId, sourcePaycheckId, transmissionId);
        ProcessResult processResult = deletePaycheck.execute();
        return processResult;
    }

    public ProcessResult<PayrollRun> updateQBPayrollInfo(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, PayrollRunDTO pPayrollRunDTO) {
        return updateQBPayrollInfo(pSourceSystemCd, pSourceCompanyId, pPayrollRunDTO, false);
    }

    public ProcessResult<PayrollRun> updateQBPayrollInfo(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, PayrollRunDTO pPayrollRunDTO, boolean pPaycheckOnly) {
        UpdateQBPayrollInfoCore updateQBPayrollInfoCore = new UpdateQBPayrollInfoCore(pSourceSystemCd, pSourceCompanyId, pPayrollRunDTO, pPaycheckOnly);
        ProcessResult processResult = updateQBPayrollInfoCore.execute();
        return processResult;
    }

    public ProcessResult<QbdtTransactionInfo> updateQBTransactionInfo(QBDTTransactionInfoDTO pQBDTTransactionInfoDTO) {
        UpdateQBDTTransactionInfoCore updateQBPayrollInfoCore = new UpdateQBDTTransactionInfoCore(pQBDTTransactionInfoDTO);
        ProcessResult processResult = updateQBPayrollInfoCore.execute();
        return processResult;
    }

    public ProcessResult<CompanyAdjustmentSubmission> updateQBLiabilityAdjustmentInfo(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO) {
        UpdateQBLiabilityAdjustmentInfo updateQBLiabilityAdjustmentInfo = new UpdateQBLiabilityAdjustmentInfo(pSourceSystemCd, pSourceCompanyId, pCompanyAdjustmentSubmissionDTO);
        return updateQBLiabilityAdjustmentInfo.execute();
    }

    public ProcessResult<Collection<CompanyAdjustmentSubmission>> voidLiabilityAdjustments(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, Collection<String> pCompanyAdjustmentSubmissionSourceIds, boolean pRecall) {
        VoidLiabilityAdjustments voidLiabilityAdjustments = new VoidLiabilityAdjustments(pSourceSystemCd, pSourceCompanyId, pCompanyAdjustmentSubmissionSourceIds, pRecall);
        return voidLiabilityAdjustments.execute();
    }

    public ProcessResult<CompanyAdjustmentSubmission> addLiabilityAdjustments(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPayrollRunId, CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO, DateDTO pLiabilityAdjustmentDate, LiabilityAdjustmentOptionsDTO pLiabilityAdjustmentOptionsDTO) {
        return addLiabilityAdjustments(pSourceSystemCd, pSourceCompanyId, pPayrollRunId, pCompanyAdjustmentSubmissionDTO, pLiabilityAdjustmentDate, pLiabilityAdjustmentOptionsDTO, null);
    }
    public ProcessResult<CompanyAdjustmentSubmission> addLiabilityAdjustments(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPayrollRunId, CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO, DateDTO pLiabilityAdjustmentDate, LiabilityAdjustmentOptionsDTO pLiabilityAdjustmentOptionsDTO, String pTransmissionId) {
        AddLiabilityAdjustmentsCore addLiabilityAdjustmentsCore = new AddLiabilityAdjustmentsCore(pSourceSystemCd, pSourceCompanyId, pPayrollRunId, pCompanyAdjustmentSubmissionDTO, pLiabilityAdjustmentDate, pLiabilityAdjustmentOptionsDTO, pTransmissionId);
        return addLiabilityAdjustmentsCore.execute();
    }

    public ProcessResult<CompanyAdjustmentSubmission> addLiabilityAdjustments(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPayrollRunId, CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO, DateDTO pLiabilityAdjustmentDate, LiabilityAdjustmentOptionsDTO pLiabilityAdjustmentOptionsDTO, String pTransmissionId, boolean pIsNotPartOfPayrollSubmission) {
        AddLiabilityAdjustmentsCore addLiabilityAdjustmentsCore = new AddLiabilityAdjustmentsCore(pSourceSystemCd, pSourceCompanyId, pPayrollRunId, pCompanyAdjustmentSubmissionDTO, pLiabilityAdjustmentDate, pLiabilityAdjustmentOptionsDTO, pTransmissionId, pIsNotPartOfPayrollSubmission);
        return addLiabilityAdjustmentsCore.execute();
    }

    public ProcessResult<PayrollRun> addCustomerTaxPayment(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, CustomerTaxPaymentDTO pCustomerTaxPaymentDTO) {
        AddCustomerTaxPaymentCore addCustomerTaxPaymentCore = new AddCustomerTaxPaymentCore(pSourceSystemCd, pSourceCompanyId, pCustomerTaxPaymentDTO);
        return addCustomerTaxPaymentCore.execute();
    }

    public ProcessResult voidPayrollTaxPayment(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPayrollRunId) {
        return new VoidPayrollTaxPayment(pSourceSystemCode, pSourceCompanyId, pPayrollRunId).execute();
    }

    public ProcessResult reissuePayrollTaxPayment(SourceSystemCode sourceSystemCode, String sourceCompanyId, String sourcePayrollRunId, String transferTransactionId) {
        return new ReissuePayrollTaxPayment(sourceSystemCode, sourceCompanyId, sourcePayrollRunId, transferTransactionId).execute();
    }

    public ProcessResult applyERPayableToBalanceDue(SourceSystemCode sourceSystemCode, String sourceCompanyId, String payrollRunId, SpcfDecimal amountToApply) {
        return new ApplyERPayableToBalanceDue(sourceSystemCode, sourceCompanyId, payrollRunId, amountToApply).execute();
    }
}

