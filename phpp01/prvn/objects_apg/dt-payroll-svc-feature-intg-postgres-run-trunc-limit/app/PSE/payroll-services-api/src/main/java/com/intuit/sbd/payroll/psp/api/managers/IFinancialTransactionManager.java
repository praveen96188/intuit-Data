package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This is the PSP service API that deals directly with financial transactions
 */
public interface IFinancialTransactionManager {
    /**
     * Adds a financial transaction to refund the balance of DD_CURRENT_LIABILITY for the
     * passed payroll run
     *
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pRefundDTO
     * @return
     */
    ProcessResult addRefundTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, RefundDTO pRefundDTO);

    /**
     * Adds or modifies (including deletion) payroll-related redebits 
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pRedebitImpoundDTOs
     * @return
     */
    ProcessResult<DomainEntitySet<FinancialTransaction>> addOrEditPayrollRelatedRedebitImpound(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, Collection<RedebitImpoundDTO> pRedebitImpoundDTOs);

    /**
     * Refunds customer for any outstanding ER Payable balance
     * @param pSourceSystemCode sourceSystemCd
     * @param pSourceCompanyId sourceCompanyId
     * @return new ERTaxCredit transaction
     */
    ProcessResult refundERPayable(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, SettlementTypeDTO settlementType, SpcfMoney amount);

    /**
     * Refunds customer for all balance
     *
     * @param pSourceSystemCode sourceSystemCd
     * @param pSourceCompanyId  sourceCompanyId
     * @return new ERTaxCredit transaction
     */
    ProcessResult createPendingTaxRefund(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String paymentId, CustomerTaxPaymentDTO customerTaxPaymentDTO);

    /**
     * Adds a financial transaction to charge a fee for a given payroll
     *
     * @param pFeeAddDTO
     * @return
     */
    ProcessResult<DomainEntitySet<FinancialTransaction>> addFeeTransaction(ERFeeAddDTO... pFeeAddDTO);

    /**
     * creates a manual fee transaction to charge a fee
     *
     * @param pFeeAddDTO
     * @return
     */
    ProcessResult<DomainEntitySet<FinancialTransaction>> createManualFeeTransaction(ERFeeAddDTO... pFeeAddDTO);

    /**
     * Refunds a fee transaction (and its related tax transaction, if present) and re-bills that same fee.
     * Presumably, the Company has a different offer or tax-exempt status at the time of the rebill than it did when
     * the original fee was charged.
     *
     * @param pRebillDTO
     * @return
     */
    ProcessResult<DomainEntitySet<BillingDetail>> rebillFeeTransaction(RebillFeeTransactionDTO pRebillDTO);


    /**
     * Add a financial transaction to indicate that a bad debt write-off has occurred due to a failed reversal
     *
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pSourcePayrollRunId
     * @return
     */
    ProcessResult<DomainEntitySet<TransactionReturn>> addEmployeeWriteOffBadDebtTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourcePayrollRunId);

    /**
     * Add a financial transaction to indicate that a bad debt write-off has occurred
     *
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pSourcePayrollRunId
     * @return
     */
    ProcessResult addWriteOffBadDebtTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourcePayrollRunId);

    /**
     * Cancels a financial transaction (that is, updates its status to 'canceled')
     *
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pFinTxId
     * @return
     */
    ProcessResult cancelTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pFinTxId);

    /**
     * Add a financial transaction to indicate that a bad debt has been recovered
     *
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pBadDebtRecoverDTO
     * @return
     */
    ProcessResult<FinancialTransaction> addRecoverBadDebtTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, BadDebtRecoverDTO pBadDebtRecoverDTO);

    /**
     * Add a financial transaction to register an escalation event
     *
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pSourcePayrollRunId
     * @param pIsEmployee
     * @param pSettlementType
     * @param pAmount
     * @param pSettlementDate
     * @return
     */
    ProcessResult<FinancialTransaction> addEscalation(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourcePayrollRunId,
                                                      boolean pIsEmployee, SettlementType pSettlementType,
                                                      BigDecimal pAmount, DateDTO pSettlementDate);

    /**
     * Refunds an employer financial transaction
     *
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pERRefundDTO
     * @return
     */
    ProcessResult<FinancialTransaction> refundEmployerTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, ERRefundDTO pERRefundDTO);

    /**
     * Voids a financial transaction (that is, update the status to be 'void' and create the necessary
     * ledger adjustments)
     *
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pFinTxId
     * @return
     */
    ProcessResult voidTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pFinTxId);

    /**
     * Adds a financial transaction to transfer employee return between two Intuit bank accounts
     *
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pSourcePayrollRunId
     * @return
     */
    ProcessResult addEmployeeReturnTransferTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourcePayrollRunId);

    /**
     * Adds a financial transaction to transfer a fee between two Intuit bank accounts
     *
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pFeeTransferDTO
     * @return
     */
    ProcessResult<FinancialTransaction> addFeeTransferTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, FeeTransferDTO pFeeTransferDTO);

    /**
     * Adds a financial transaction to transfer a 5 day return  between two Intuit bank accounts
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pSourcePayrollRunId
     * @return
     */
    ProcessResult<DomainEntitySet<FinancialTransaction>> addIntuit5DayReturnTransferTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourcePayrollRunId);

    /**
     * Adds a financial transaction to refund an employee return
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pRefundDTO
     * @return
     */
    ProcessResult addEmployeeReturnRefundTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, RefundDTO pRefundDTO);

    /**
     * Adds a financial transaction to refund an employer return
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pRefundDTO
     * @return
     */
    ProcessResult addEmployerReturnRefundTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, RefundDTO pRefundDTO);

    /**
     * Update the Transaction Return with the specified status and creates a company note 
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pPSETransactionId
     * @param pBankReturnStatus
     * @param pNote
     * @return
     */
    ProcessResult updateBankReturnStatus(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPSETransactionId,
                                                                                TransactionReturnStatusCode pBankReturnStatus, String pNote);


    /**
     * Adds payroll-related Non ACH redebits 
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pRedebitImpoundDTOs
     * @return
     */
    ProcessResult<DomainEntitySet<FinancialTransaction>> addPayrollRelatedNonACHRedebit(SourceSystemCode pSourceSystemCode,
                                                                                  String pSourceCompanyId,
                                                                                  Collection<RedebitImpoundDTO> pRedebitImpoundDTOs);

    /**
     * Adds a transaction refunding an employer for fraud or escalation.
     * @param pSrcSystemCd
     * @param pSrcCompanyId
     * @param pDTOs
     * @return
     */
    ProcessResult<FinancialTransaction> addEmployerFraudOrEscalationRefund(SourceSystemCode pSrcSystemCd,
                                                                           String pSrcCompanyId,
                                                                           Collection<ERRefundDTO> pDTOs);

    /**
     * Adds prefunding payroll transactions
     * @param pSourceSystemCd
     * @param pSourceCompanyId
     * @param pSourcePayrollRunId
     * @param pSettlementType
     * @param pTransactions
     * @param pSettlementDate
     * @return
     */
    ProcessResult<DomainEntitySet<FinancialTransaction>> prefundPayroll(SourceSystemCode pSourceSystemCd,
                                                                        String pSourceCompanyId,
                                                                        String pSourcePayrollRunId,
                                                                        SettlementType pSettlementType,
                                                                        SpcfCalendar pSettlementDate,
                                                                        ArrayList<PrefundPayrollTransactionDTO> pTransactions);

    ProcessResult recordCollectionAgencyExpense(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPayrollRunId, SpcfMoney pExpenseAmount, DateDTO pSettlementDate);

    ProcessResult<FinancialTransaction> addFinancialLedgerAdjustmentTransaction(SourceSystemCode pSourceSystemCode,
                                                                                String pSourceCompanyId,
                                                                                LedgerAccountCode pDebitAccount,
                                                                                LedgerAccountCode pCreditAccount,
                                                                                SpcfMoney pAmount,
                                                                                String pPayrollRunId,
                                                                                String pLawId, String pNoteText);

    ProcessResult<FinancialTransaction> addBookTransferTransaction(String pFromAccount, String pToAccount, SpcfMoney pAmount);

    ProcessResult<DomainEntitySet<FinancialTransaction>> addPenaltiesAndInterestRefund(SourceSystemCode pSourceSystemCode,
                                                                                 String pSourceCompanyId,
                                                                                 SpcfMoney pPenaltiesRefundAmount,
                                                                                 SpcfMoney pInterestRefundAmount,
                                                                                 String pNoteText,
                                                                                 SettlementTypeDTO pSettlementType);
    ProcessResult<FinancialTransaction> addCourtesyFeeRefund(SourceSystemCode pSourceSystemCode,
                                                                    String pSourceCompanyId,
                                                                    SpcfMoney pRefundAmount,
                                                                    String pNoteText,
                                                                    SettlementTypeDTO pSettlementType);

    ProcessResult<FinancialTransaction> addRefundDebit(String pFinancialTransactionId, String pNoteText, SettlementTypeDTO pSettlementType);

    ProcessResult<PayrollRun> addTORTransactions(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPaymentTemplateCd, SpcfCalendar pQuarterEndDate);
}
