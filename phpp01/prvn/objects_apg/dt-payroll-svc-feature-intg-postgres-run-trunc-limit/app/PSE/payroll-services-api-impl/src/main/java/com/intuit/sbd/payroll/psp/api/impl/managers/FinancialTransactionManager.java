package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.managers.IFinancialTransactionManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author achaves
 * Date: Jan 17, 2008
 * Time: 8:53:37 AM

 */
class FinancialTransactionManager implements IFinancialTransactionManager {
    public ProcessResult addEmployeeReturnTransferTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                 String pSourcePayrollRunId) {
        AddEmployeeReturnTransferTransaction eeReturnTransfer = new AddEmployeeReturnTransferTransaction(pSourceSystemCode,
                pSourceCompanyId, pSourcePayrollRunId);

        ProcessResult processResult = eeReturnTransfer.execute();
        processResult.setResult(eeReturnTransfer.getTransactionReturns());

        return processResult;

    }

    public ProcessResult refundERPayable(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, SettlementTypeDTO settlementType, SpcfMoney amount) {
        return new RefundERPayableCore(pSourceSystemCode, pSourceCompanyId, settlementType, amount).execute();
    }

    public ProcessResult addRefundTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, RefundDTO pRefundDTO) {

        AddRefundTransaction refundProcess = new AddRefundTransaction(pSourceSystemCode, pSourceCompanyId, pRefundDTO);

        ProcessResult processResult = refundProcess.execute();

        if (pRefundDTO != null) {
            if (SettlementTypeDTO.ACH.equals(pRefundDTO.getSettlementType())) {
                processResult.setResult(refundProcess.getTransactionResponse());
            } else {
                processResult.setResult(refundProcess.getFinancialTransaction());
            }
        }

        return processResult;
    }

    public ProcessResult<DomainEntitySet<FinancialTransaction>> addOrEditPayrollRelatedRedebitImpound(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, Collection<RedebitImpoundDTO> redebitImpoundDTOs) {
        AddOrEditPayrollRelatedRedebit redebitAddOrEditCore =
                new AddOrEditPayrollRelatedRedebit(pSourceSystemCode, pSourceCompanyId, redebitImpoundDTOs);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = redebitAddOrEditCore.execute();
        processResult.setResult(redebitAddOrEditCore.getFinancialTransactions());
        return processResult;
    }

    public ProcessResult<DomainEntitySet<FinancialTransaction>> addFeeTransaction(ERFeeAddDTO... pFeeAddDTO) {
        AddFeeTransactionCore erFeeAdd = new AddFeeTransactionCore(pFeeAddDTO);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = erFeeAdd.execute();

        return processResult;
    }

    public ProcessResult<DomainEntitySet<FinancialTransaction>> createManualFeeTransaction(ERFeeAddDTO... pFeeAddDTO) {
        CreateManualFeeTransactionCore createManualFeeTransactionCore = new CreateManualFeeTransactionCore(pFeeAddDTO);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = createManualFeeTransactionCore.execute();

        return processResult;
    }

    public ProcessResult<DomainEntitySet<BillingDetail>> rebillFeeTransaction(RebillFeeTransactionDTO pRebillDTO) {
        RebillFeeTransactionCore process = new RebillFeeTransactionCore(pRebillDTO);
        ProcessResult<DomainEntitySet<BillingDetail>> result = process.execute();
        return result;
    }

    public ProcessResult addEmployeeReturnRefundTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, RefundDTO pRefundDTO) {

        AddEmployeeReturnRefundTransaction eeReturnRefundProcess = new AddEmployeeReturnRefundTransaction(pSourceSystemCode, pSourceCompanyId,
                pRefundDTO);

        ProcessResult processResult = eeReturnRefundProcess.execute();

        if (pRefundDTO != null) {
            if (SettlementTypeDTO.ACH.equals(pRefundDTO.getSettlementType())) {
                processResult.setResult(eeReturnRefundProcess.getTransactionResponse());
            } else {
                processResult.setResult(eeReturnRefundProcess.getFinancialTransaction());
            }
        }

        return processResult;
    }

    public ProcessResult addEmployerReturnRefundTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, RefundDTO pRefundDTO) {

        AddEmployerReturnRefundTransaction erReturnRefundProcess = new AddEmployerReturnRefundTransaction(pSourceSystemCode, pSourceCompanyId,
                pRefundDTO);

        ProcessResult processResult = erReturnRefundProcess.execute();

        if (pRefundDTO != null) {
            if (SettlementTypeDTO.ACH.equals(pRefundDTO.getSettlementType())) {
                processResult.setResult(erReturnRefundProcess.getTransactionResponse());
            } else {
                processResult.setResult(erReturnRefundProcess.getFinancialTransaction());
            }
        }

        return processResult;
    }

    public ProcessResult<DomainEntitySet<TransactionReturn>> addEmployeeWriteOffBadDebtTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                         String pSourcePayrollRunId) {
        AddEmployeeWriteOffBadDebtTransaction writeOffBadDebtProcess = new AddEmployeeWriteOffBadDebtTransaction(pSourceSystemCode, pSourceCompanyId,
                pSourcePayrollRunId);

        ProcessResult<DomainEntitySet<TransactionReturn>> processResult = writeOffBadDebtProcess.execute();
        processResult.setResult(writeOffBadDebtProcess.getTransactionReturns());
        return processResult;
    }

    public ProcessResult addWriteOffBadDebtTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                         String pSourcePayrollRunId) {
        AddWriteOffBadDebtTransaction writeOffBadDebtProcess = new AddWriteOffBadDebtTransaction(pSourceSystemCode, pSourceCompanyId,
                pSourcePayrollRunId);

        ProcessResult processResult = writeOffBadDebtProcess.execute();
        processResult.setResult(writeOffBadDebtProcess.getTransactionReturns());
        return processResult;
    }

    public ProcessResult cancelTransaction(SourceSystemCode pSourceSystemCode,
                                             String pSourceCompanyId,
                                             String pFinTxId) {
        CancelTransactionCore cancelERTransaction =
                new CancelTransactionCore(pSourceSystemCode, pSourceCompanyId, pFinTxId, false);
        ProcessResult processResult = cancelERTransaction.execute();

        return processResult;
    }

    public ProcessResult<FinancialTransaction> addRecoverBadDebtTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                              BadDebtRecoverDTO pBadDebtRecoverDTO) {
        AddRecoverBadDebtTransaction recoverBadDebtProcess = new AddRecoverBadDebtTransaction(pSourceSystemCode, pSourceCompanyId,
                pBadDebtRecoverDTO);

        ProcessResult<FinancialTransaction> processResult = recoverBadDebtProcess.execute();
        processResult.setResult(recoverBadDebtProcess.getFinancialTransaction());

        return processResult;
    }

    public ProcessResult<FinancialTransaction> addEscalation(SourceSystemCode pSrcSystemCd, String pCompanyId, String pPayrollRunId,
                                                             boolean pIsEmployee, SettlementType pSettlementType,
                                                             BigDecimal pAmount, DateDTO pSettlementDate) {
        AddEscalationCore process = new AddEscalationCore(pSrcSystemCd, pCompanyId, pPayrollRunId, pIsEmployee,
                pSettlementType, pAmount, pSettlementDate);
        ProcessResult result = process.execute();

        if (result.isSuccess()) {
            result.setResult(process.getEscalationTxn());
        }

        return result;
    }

    public ProcessResult<FinancialTransaction> refundEmployerTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                             ERRefundDTO pERRefundDTO) {

        RefundEmployerTransactionCore refundERTransaction =
                new RefundEmployerTransactionCore(pSourceSystemCode, pSourceCompanyId, pERRefundDTO);
        ProcessResult processResult = refundERTransaction.execute();
        processResult.setResult(refundERTransaction.getFinancialTransaction());
        return processResult;
    }

    public ProcessResult voidTransaction(SourceSystemCode pSourceSystemCode,
                                                    String pSourceCompanyId,
                                                    String pFinTxId) {
        VoidTransactionCore voidTransaction =
                new VoidTransactionCore(pSourceSystemCode, pSourceCompanyId, pFinTxId);
        ProcessResult processResult = voidTransaction.execute();

        return processResult;
    }

    public ProcessResult<FinancialTransaction> addFeeTransferTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                           FeeTransferDTO pFeeTransferDTO) {

        AddFeeTransferTransaction feeTransferProcess = new AddFeeTransferTransaction(pSourceSystemCode, pSourceCompanyId,
                pFeeTransferDTO);

        ProcessResult<FinancialTransaction> processResult = feeTransferProcess.execute();
        processResult.setResult(feeTransferProcess.getFinancialTransaction());

        return processResult;
    }

    public ProcessResult<DomainEntitySet<FinancialTransaction>> addIntuit5DayReturnTransferTransaction(SourceSystemCode pSourceSystemCode,
                                                                        String pSourceCompanyId, String pDDTxBatchId) {

        AddIntuit5DayReturnTransferTransaction transferIntuit5DayReturn = new AddIntuit5DayReturnTransferTransaction(
                pSourceSystemCode, pSourceCompanyId, pDDTxBatchId);


        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = transferIntuit5DayReturn.execute();
        processResult.setResult(transferIntuit5DayReturn.getFinancialTransactionList());

        return processResult;
    }

    public ProcessResult updateBankReturnStatus(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPSETransactionId,
                                                                                TransactionReturnStatusCode pBankReturnStatus, String pNote) {
        UpdateBankReturnCore updateBankReturnCore = new UpdateBankReturnCore(pSourceSystemCode, pSourceCompanyId,
                                                                             pPSETransactionId, pBankReturnStatus,
                                                                             pNote);
        ProcessResult processResult = updateBankReturnCore.execute();
        return processResult;
    }

    public ProcessResult<DomainEntitySet<FinancialTransaction>> addPayrollRelatedNonACHRedebit(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, Collection<RedebitImpoundDTO> redebitImpoundDTOs) {
        AddPayrollRelatedNonACHRedebit nonAchRedebitAddCore =
                new AddPayrollRelatedNonACHRedebit(pSourceSystemCode, pSourceCompanyId, redebitImpoundDTOs);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = nonAchRedebitAddCore.execute();
        processResult.setResult(nonAchRedebitAddCore.getFinancialTransactions());
        return processResult;
    }

    public ProcessResult<FinancialTransaction> addEmployerFraudOrEscalationRefund(SourceSystemCode pSrcSystemCd, String pSrcCompanyId, Collection<ERRefundDTO> pDTOs) {
        AddEmployerFraudOrEscalationRefundCore process = new AddEmployerFraudOrEscalationRefundCore(pSrcSystemCd, pSrcCompanyId, pDTOs);
        return process.execute();
    }

    public ProcessResult<DomainEntitySet<FinancialTransaction>> prefundPayroll(SourceSystemCode pSourceSystemCd,
                                                               String pSourceCompanyId,
                                                               String pSourcePayrollRunId,
                                                               SettlementType pSettlementType,
                                                               SpcfCalendar pSettlementDate,
                                                               ArrayList<PrefundPayrollTransactionDTO> pTransactions) {
        PrefundPayrollCore prefundPayrollCore =
                new PrefundPayrollCore(pSourceSystemCd,
                                       pSourceCompanyId,
                                       pSourcePayrollRunId,
                                       pSettlementType,
                                       pSettlementDate,
                                       pTransactions);
        return  prefundPayrollCore.execute();
    }

    public ProcessResult recordCollectionAgencyExpense(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPayrollRunId, SpcfMoney pExpenseAmount, DateDTO pSettlementDate) {
        return new RecordCollectionAgencyExpenseCore(pSourceSystemCode, pSourceCompanyId, pPayrollRunId, pExpenseAmount, pSettlementDate).execute();
    }

    public ProcessResult<FinancialTransaction> addFinancialLedgerAdjustmentTransaction(SourceSystemCode pSourceSystemCode,
                                                                                       String pSourceCompanyId,
                                                                                       LedgerAccountCode pDebitAccount,
                                                                                       LedgerAccountCode pCreditAccount,
                                                                                       SpcfMoney pAmount,
                                                                                       String pPayrollRunId,
                                                                                       String pLawId, String pNoteText) {
        return new AddFinancialLedgerAdjustmentTransactionCore(pSourceSystemCode, pSourceCompanyId, pDebitAccount, pCreditAccount, pAmount, pPayrollRunId, pLawId, pNoteText).execute();
    }

    public ProcessResult<FinancialTransaction> addBookTransferTransaction(String pFromAccount, String pToAccount, SpcfMoney pAmount) {
        return new AddBookTransferTransactionCore(pFromAccount, pToAccount, pAmount).execute();
    }

    public ProcessResult<DomainEntitySet<FinancialTransaction>> addPenaltiesAndInterestRefund(SourceSystemCode pSourceSystemCode,
                                                                                 String pSourceCompanyId,
                                                                                 SpcfMoney pPenaltiesRefundAmount,
                                                                                 SpcfMoney pInterestRefundAmount,
                                                                                 String pNoteText,
                                                                                 SettlementTypeDTO pSettlementType) {
        return new AddCompanyTaxPenaltyAndInterestRefunds(pSourceSystemCode, pSourceCompanyId, pPenaltiesRefundAmount, pInterestRefundAmount, pNoteText, pSettlementType).execute();
    }

    public ProcessResult<FinancialTransaction> addCourtesyFeeRefund(SourceSystemCode pSourceSystemCode,
                                                                                              String pSourceCompanyId,
                                                                                              SpcfMoney pRefundAmount,
                                                                                              String pNoteText,
                                                                                              SettlementTypeDTO pSettlementType) {
        return new AddCourtesyFeeRefund(pSourceSystemCode, pSourceCompanyId, pRefundAmount, pNoteText, pSettlementType).execute();
    }

    public ProcessResult<FinancialTransaction> addRefundDebit(String pFinancialTransactionId, String pNoteText, SettlementTypeDTO pSettlementType) {
        return new AddCompanyTaxRefundDebit(pFinancialTransactionId, pNoteText, pSettlementType).execute();
    }

    public ProcessResult<PayrollRun> addTORTransactions(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPaymentTemplateCd, SpcfCalendar pQuarterEndDate) {
        //noinspection unchecked
        return new AddTORTransactionsCore(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd, pQuarterEndDate).execute();
    }

    public ProcessResult createPendingTaxRefund(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String paymentId, CustomerTaxPaymentDTO customerTaxPaymentDTO) {
        return new CreatePendingTaxRefundCore(pSourceSystemCode, pSourceCompanyId, paymentId,customerTaxPaymentDTO).execute();
    }

}
