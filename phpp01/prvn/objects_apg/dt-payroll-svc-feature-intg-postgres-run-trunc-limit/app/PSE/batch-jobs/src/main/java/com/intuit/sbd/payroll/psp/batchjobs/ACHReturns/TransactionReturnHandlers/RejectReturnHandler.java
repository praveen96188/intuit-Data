package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.ERFeeAddDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTPayrollTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTPayrollTransactionLineDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTTransactionInfoDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;

/**
 * This class handles all "R" TransactionReturns.  It loops over the FinanacialTransactions related to the return
 * calling an appropriate FTRejectHandler to perform FT-specific handling.  Finally, this class performs any remaining
 * processing on the TransactionReturn.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Jun 6, 2008
 * Time: 12:22:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class RejectReturnHandler extends TransactionReturnHandler {

    private static SpcfLogger logger = Application.getLogger(RejectReturnHandler.class);

    /**
     * Returns true if TransactionReturn is any "R" return reason, else false.
     *
     * @param pTxnReturn the TransactionReturn
     * @return whether this instance can handle the given TransactionReturn
     */
    public boolean meetsCriteria(TransactionReturn pTxnReturn) {
        return pTxnReturn.getBankReturnCd().startsWith("R");
    }

    /**
     * Handles a TransactionReturn with an "R" bank return code.
     *
     * @param pTxnReturn the TransactionReturn
     * @return the possibly-updated TransactionReturn
     */
    public TransactionReturn execute(TransactionReturn pTxnReturn) {
        // if all FT-specific handlers approve, we'll resolve the return... if even one says "no", we won't
        // we accumulate those decisions here
        boolean bOkToResolve = true;
        boolean bIsNSF = pTxnReturn.isNSF();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(pTxnReturn);
        FinancialTransaction singleReturnedTransaction = null;

        if (returnedFTs.size() == 1) {
            singleReturnedTransaction = returnedFTs.get(0);
        }

        if (bIsNSF) {
            if (isConsolidatedDebit(returnedFTs)) {
                bOkToResolve = handleConsolidatedDebitNSF(returnedFTs, pTxnReturn);
            } else if (singleReturnedTransaction != null && TransactionType.isImpoundTransactionType(singleReturnedTransaction.getTransactionType().getTransactionTypeCd())) {
                bOkToResolve = handleDebitOnlyNSF(singleReturnedTransaction, pTxnReturn);
            } else if (isConsolidatedRedebit(returnedFTs)) {
                bOkToResolve = handleConsolidatedRedebitNSF(returnedFTs, pTxnReturn);
            } else if (singleReturnedTransaction != null && isErRedebitTransaction(singleReturnedTransaction)) {
                bOkToResolve = handleRedebitOnlyNSF(singleReturnedTransaction, pTxnReturn);
            } else if (isFeeAndOrTaxDebitRedebitOnly(returnedFTs)) {
                bOkToResolve = handleFeeAndOrTaxDebitRedebitOnly(returnedFTs, pTxnReturn, bIsNSF);
            } else if (singleReturnedTransaction != null && TransactionTypeCode.EmployeeDdReversalDebit == singleReturnedTransaction.getTransactionType().getTransactionTypeCd()) {
                bOkToResolve = handleEmployeeReversalDebit(singleReturnedTransaction, pTxnReturn);
            } else if (singleReturnedTransaction != null && TransactionTypeCode.EmployeeDdCredit == singleReturnedTransaction.getTransactionType().getTransactionTypeCd()) {
                bOkToResolve = handleEmployeeCredit(singleReturnedTransaction, pTxnReturn, bIsNSF);
            } else if (singleReturnedTransaction != null && TransactionTypeCode.EmployerVerificationDebit == singleReturnedTransaction.getTransactionType().getTransactionTypeCd()) {
                bOkToResolve = handleBankVerificationDebit(singleReturnedTransaction, pTxnReturn);
            } else if (isCombinedRefundAndDebitNetDebit(returnedFTs)) {
                bOkToResolve = handleCombinedRefundAndDebitNetDebit(returnedFTs, pTxnReturn);
            } else if (singleReturnedTransaction != null && TransactionAssociationType.Refund == singleReturnedTransaction.getTransactionType().getAssociationType()) {
                bOkToResolve = handleRefundOnly(singleReturnedTransaction, pTxnReturn);
            } else if (singleReturnedTransaction != null && TransactionTypeCode.EmployerVerificationCredit == singleReturnedTransaction.getTransactionType().getTransactionTypeCd()) {
                bOkToResolve = handleBankVerificationCredit(singleReturnedTransaction, pTxnReturn);
            } else {
                bOkToResolve = handleDefaultCase(returnedFTs, pTxnReturn);
                sendEmail(returnedFTs.find(FinancialTransaction.CurrentTransactionState().notEqualTo(TransactionState.findTransactionState(TransactionStateCode.Returned))));
            }
        } else {
            if (isConsolidatedDebit(returnedFTs) || isConsolidatedRedebit(returnedFTs)) {
                bOkToResolve = handleConsolidatedDebitRedebitNonNSF(returnedFTs, pTxnReturn);
            } else if (singleReturnedTransaction != null && (TransactionType.isImpoundTransactionType(singleReturnedTransaction.getTransactionType().getTransactionTypeCd()) || isErRedebitTransaction(singleReturnedTransaction))) {
                bOkToResolve = handleDebitRedebitOnlyNonNSF(singleReturnedTransaction, pTxnReturn);
            } else if (isFeeAndOrTaxDebitRedebitOnly(returnedFTs)) {
                bOkToResolve = handleFeeAndOrTaxDebitRedebitOnly(returnedFTs, pTxnReturn, bIsNSF);
            } else if (singleReturnedTransaction != null && TransactionTypeCode.EmployeeDdCredit == singleReturnedTransaction.getTransactionType().getTransactionTypeCd()) {
                bOkToResolve = handleEmployeeCredit(singleReturnedTransaction, pTxnReturn, bIsNSF);
            } else if (singleReturnedTransaction != null && TransactionTypeCode.EmployeeDdReversalDebit == singleReturnedTransaction.getTransactionType().getTransactionTypeCd()) {
                bOkToResolve = handleEmployeeReversalDebit(singleReturnedTransaction, pTxnReturn);
            } else if (singleReturnedTransaction != null && TransactionTypeCode.EmployerVerificationDebit == singleReturnedTransaction.getTransactionType().getTransactionTypeCd()) {
                bOkToResolve = handleBankVerificationDebit(singleReturnedTransaction, pTxnReturn);
            } else if (isCombinedRefundAndDebitNetDebit(returnedFTs)) {
                bOkToResolve = handleCombinedRefundAndDebitNetDebit(returnedFTs, pTxnReturn);
            } else if (isCombinedRefundAndDebitNetCredit(returnedFTs)) {
                bOkToResolve = handleCombinedRefundAndDebitNetCredit(returnedFTs, pTxnReturn);
            } else if (singleReturnedTransaction != null && TransactionAssociationType.Refund == singleReturnedTransaction.getTransactionType().getAssociationType()) {
                bOkToResolve = handleRefundOnly(singleReturnedTransaction, pTxnReturn);
            }  else if (singleReturnedTransaction != null && TransactionTypeCode.EmployerVerificationCredit == singleReturnedTransaction.getTransactionType().getTransactionTypeCd()) {
                bOkToResolve = handleBankVerificationCredit(singleReturnedTransaction, pTxnReturn);
            } else {
                bOkToResolve = handleDefaultCase(returnedFTs, pTxnReturn);
                sendEmail(returnedFTs.find(FinancialTransaction.CurrentTransactionState().notEqualTo(TransactionState.findTransactionState(TransactionStateCode.Returned))));
            }
        }

        // requery the freshest version of the return
        TransactionReturn txnReturn = Application.findById(TransactionReturn.class, pTxnReturn.getId());

        // if the handler said it was OK, we resolve the return
        if (bOkToResolve) {
            // now resolve it
            txnReturn = resolveACHReturn(txnReturn);
        } else {
            txnReturn.updateTransactionReturnStatus(TransactionReturnStatusCode.Open);
        }

        return txnReturn;
    }

    private FinancialTransaction returnFinancialTransaction(TransactionReturn pTxnReturn, FinancialTransaction pFinTxn, boolean pCreateTxnResponse) {
        //
        // Set the FT state to Returned
        //
        FinancialTransaction ft = pFinTxn.updateFinancialTransactionState(TransactionStateCode.Returned);

        //
        // Create Transaction Response (if requested)
        //
        if (pCreateTxnResponse) {
            TransactionResponse.createTransactionResponseForFinancialTx(ft);
        }

        //
        // PSRV003387
        // Create a QbdtPayrollTransaction to return to client (if appropriate)
        //
        createQbdtPayrollTransactionIfMeetsCriteria(pTxnReturn, ft);

        return ft;
    }

    private void createQbdtPayrollTransactionIfMeetsCriteria(TransactionReturn pTxnReturn, FinancialTransaction pFT) {
        Company company = pTxnReturn.getCompany();

        if ((company == null) || !SourceSystemCode.QBDT.equals(company.getSourceSystemCd())) {
            return;
        }

        //
        // Only want EmployeeDdCredit transactions
        //

        if (!TransactionTypeCode.EmployeeDdCredit.equals(pFT.getTransactionType().getTransactionTypeCd())) {
            return;
        }

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        EmployeeBankAccount eeba = pFT.getEmployeeBankAccount();

        //
        // If either the company or employee bank accounts are invalid, we can't continue...
        //

        if ((cba == null) || (eeba == null)) {
            return;
        }

        //
        // Create the qbdt payroll transaction line dto
        //
        QBDTTransactionInfoDTO qbdtPayrollTxLineInfo = new QBDTTransactionInfoDTO();
        qbdtPayrollTxLineInfo.setMemo(ReturnReasonDesc.findReturnDescription(pTxnReturn.getBankReturnCd()));
        qbdtPayrollTxLineInfo.setIsDirectDeposit(true);

        QBDTPayrollTransactionLineDTO qbdtPayrollTxLine = new QBDTPayrollTransactionLineDTO();
        qbdtPayrollTxLine.setQBDTTransactionInfoDTO(qbdtPayrollTxLineInfo);
        qbdtPayrollTxLine.setAmount((SpcfMoney) pFT.getFinancialTransactionAmount().negate());

        //
        // Create the qbdt payroll transaction dto
        //

        QBDTTransactionInfoDTO qbdtPayrollTxInfo = new QBDTTransactionInfoDTO();
        qbdtPayrollTxInfo.setAgencyName(QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE);
        qbdtPayrollTxInfo.setAccountName(cba.getSourceBankAccountName());
        qbdtPayrollTxInfo.setOnService(true);
        qbdtPayrollTxInfo.setCleared(QBOFX.DEFAULT_CLEARED_RESPONSE_STR);

        QBDTPayrollTransactionDTO qbdtPayrollTx = new QBDTPayrollTransactionDTO();
        qbdtPayrollTx.getQBDTPayrollTransactionLineDTOs().add(qbdtPayrollTxLine);
        qbdtPayrollTx.setQBDTTransactionInfoDTO(qbdtPayrollTxInfo);
        qbdtPayrollTx.setTransactionType(QbdtPayrollTransactionType.DDReturn);
        qbdtPayrollTx.setAmount(pFT.getFinancialTransactionAmount());
        qbdtPayrollTx.setPeriodEndDate(pFT.getSettlementDate());
        qbdtPayrollTx.setIsVoided(false);
        qbdtPayrollTx.setTransactionDate(pTxnReturn.getCreatedDate());

        try {
            long eeSourceId = Long.parseLong(eeba.getEmployee().getSourceEmployeeId());
            qbdtPayrollTx.setEmployeeSourceId(Long.toString(eeSourceId));
        } catch (NumberFormatException e) {
            qbdtPayrollTx.setEmployeeName(eeba.getEmployee().getFullName());
        }

        //
        // Call the core process to persist the qbdt payroll transaction information
        //

        ProcessResult<QbdtPayrollTransaction> result = PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(company.getSourceSystemCd(),
                                                                                                                        company.getSourceCompanyId(),
                                                                                                                        qbdtPayrollTx);

        if (!result.isSuccess()) {
            logger.error(String.format("Error processing ACH return - could not add QBDT Payroll Transaction for Company %s, EE %s [reason: %s]",
                                       company.getSourceSystemCompanyId(), eeba.getEmployee().getFullName(), result.toString()));
        }
    }

    private boolean handleRefundOnly(FinancialTransaction pReturnedFt, TransactionReturn pTxnReturn) {
        logger.info("handleRefundOnly");
        // set the FT state to "Returned" and create a TransactionResponse for it
        returnFinancialTransaction(pTxnReturn, pReturnedFt, true);

        // create a company event
        CompanyEvent.createERRefundReturnEvent(pReturnedFt.getCompany(), pReturnedFt, pTxnReturn.getBankReturnCd());

        if (pReturnedFt.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxCredit) {
            createERTaxCreditRefundReturnTransfer(pReturnedFt);
        }

        // this one should not be Resolved
        return false;
    }

    private void createERTaxCreditRefundReturnTransfer(FinancialTransaction pReturnedFt) {
        IntuitBankAccount creditIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(TransactionTypeCode.EmployerTaxCreditReturnedTransfer, CreditDebitCode.Credit);
        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(TransactionTypeCode.EmployerTaxCreditReturnedTransfer, CreditDebitCode.Debit);

        FinancialTransaction.createFinancialTransaction(pReturnedFt.getCompany(), null, null,
                creditIntuitBankAccount.getBankAccount(), debitIntuitBankAccount.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                TransactionTypeCode.EmployerTaxCreditReturnedTransfer,
                new SpcfMoney(pReturnedFt.getFinancialTransactionAmount()),
                SettlementType.ACH,
                FinancialTransaction.getSettlementDate(TransactionTypeCode.EmployerTaxCreditReturnedTransfer, pReturnedFt.getCompany().getOffloadGroup()),
                null,
                pReturnedFt,
                0);

    }

    private boolean handleDebitRedebitOnlyNonNSF(FinancialTransaction pReturnedERTxn, TransactionReturn pTxnReturn) {
        logger.info("handleDebitRedebitOnlyNonNSF");

        boolean bOkToResolve = false;
        boolean cancelRefunds = true;
        boolean createEvent = true;

        Company company = pReturnedERTxn.getCompany();
        PayrollRun payrollRun = pReturnedERTxn.getPayrollRun();
        PayrollStatus payrollStatus = payrollRun.getPayrollRunStatus();

        // set the FT state to "Returned" and create a TransactionResponse for it
        returnFinancialTransaction(pTxnReturn, pReturnedERTxn, true);

        if (payrollStatus.equals(PayrollStatus.OffloadedDebit)) {
            // add a DebitReturnedCanceled strike
            company.addStrikeEvent(StrikeReason.DebitReturnedCanceled, "", PSPDate.getPSPTime(), pReturnedERTxn);

            // cancel any EE DD Credit FTs related to this payroll
            payrollRun.cancelPayrollFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit});

            // update the PayrollRun status
            payrollRun.updatePayrollRunStatus(PayrollStatus.DebitReturnedCanceled);

            // create a DebitReturnedCanceled company event
            CompanyEvent.createDDDebitReturnEvent(company, pReturnedERTxn, pTxnReturn.getBankReturnCd(), payrollStatus, PayrollStatus.DebitReturnedCanceled, PSPDate.getPSPTime());

            // create an Intuit5DayReturnTransaction FT
            ProcessResult prUnhandled = PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId());

            if (!prUnhandled.isSuccess()) {
                logger.error("Unhandled ProcessResult failure from FinancialTransactionManager.addIntuit5DayReturnTransferTransaction(): " + prUnhandled.toString());
            }

            bOkToResolve = true;
        } else {
            PayrollStatus newPayrollStatus = null;

            //Update payrollrun Status rule...if we've returned a redebit, set the status to ReturnedTwice
            //If this is the return on the first debit, set the status to DebitReturned
            if (TransactionTypeCode.EmployerDdRedebit == pReturnedERTxn.getTransactionType().getTransactionTypeCd()) {
                // update the PayrollRun status if the company has a balance due for the payroll
                if (!isCompanyBalanceDueCoveredForPayroll(payrollRun)) {
                    newPayrollStatus = PayrollStatus.ReturnedTwice;

                    // add an on-hold reason
                    PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectOther);

                    bOkToResolve = false;
                } else {
                    bOkToResolve = true;
                    cancelRefunds = false;
                    createEvent = false;
                }
            } else {
                newPayrollStatus = PayrollStatus.DebitReturned;

                // add an on-hold reason
                PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectOther);

                // add a DebitReturned strike
                company.addStrikeEvent(StrikeReason.DebitReturned, "", PSPDate.getPSPTime(), pReturnedERTxn);

                bOkToResolve = false;
            }

            if (newPayrollStatus != null) {
                payrollRun.updatePayrollRunStatus(newPayrollStatus);
            } else {
                //todo update once Shannon updates returns doc
                //just setting this for the event
                newPayrollStatus = PayrollStatus.DebitReturned;
            }

            if (createEvent) {
                // create a DebitReturned company event
                CompanyEvent.createDDDebitReturnEvent(company, pReturnedERTxn, pTxnReturn.getBankReturnCd(), payrollStatus, newPayrollStatus, PSPDate.getPSPTime());
            }
        }

        if (cancelRefunds) {
            // cancel any pending DD Refund FTs related to this payroll
            payrollRun.cancelPayrollFinancialTransactions(TransactionType.getRefundTypesForService((Service) Application.findById(Service.class, ServiceCode.DirectDeposit)));
        }

        // todo: cancel AgencyTaxDebits?

        addRiskAssessmentHoldIfMeetsCriteria(company);

        return bOkToResolve;
    }

    private boolean handleDebitOnlyNSF(FinancialTransaction pSingleReturnedTransaction, TransactionReturn pTxnReturn) {
        boolean bOkToResolve = false;

        logger.info("Debit Only NSF");

        Company company = pSingleReturnedTransaction.getCompany();
        PayrollRun payrollRun = pSingleReturnedTransaction.getPayrollRun();
        PayrollStatus payrollStatus = payrollRun.getPayrollRunStatus();

        // set the FT state to "Returned" and create a TransactionResponse for it
        returnFinancialTransaction(pTxnReturn, pSingleReturnedTransaction, true);

        // cancel any pending DD Refund FTs related to this payroll
        payrollRun.cancelPayrollFinancialTransactions(TransactionType.getRefundTypesForService((Service) Application.findById(Service.class, ServiceCode.DirectDeposit)));

        if (payrollRun.getPayrollRunType() == PayrollType.BillPayment && ((payrollStatus == PayrollStatus.PendingAutoRedebit) || (payrollStatus == PayrollStatus.DebitReturned))) {
            payrollStatus = PayrollStatus.OffloadedAll;
        }

        if (pSingleReturnedTransaction.getFinancialTransactionAmount().compareTo(new SpcfMoney("0.00")) > 0) {
            if (payrollStatus == PayrollStatus.OffloadedAll) {
                // add an NSFAutoRedebit strike (create Strike event before NSF return event so email will see the strike)
                company.addStrikeEvent(StrikeReason.NSFAutoRedebit, "", PSPDate.getPSPTime(), pSingleReturnedTransaction);

                //Only create the redebits if the company has an active bank account
                CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
                if (cba != null) {
                    boolean hasDebitReturnFee = false;
                    for (BillingDetail billingDetail : payrollRun.getBillingDetailCollection()) {
                        if (billingDetail.getOfferingServiceChargeType().equals(OfferingServiceChargeType.DebitReturnFee)) {
                            hasDebitReturnFee = true;
                            break;
                        }
                    }

                    if (!hasDebitReturnFee) {
                        // create an NSF fee debit -- charge the company for this NSF return
                        createDebitReturnFee(payrollRun);
                    }

                    createRedebitFT(pSingleReturnedTransaction, cba);

                    payrollRun.updatePayrollRunStatus(PayrollStatus.PendingAutoRedebit);

                    CompanyEvent.createNSFReturnEvent(company, pSingleReturnedTransaction, pTxnReturn.getBankReturnCd(), NSFSubTypeType.NSFAutoRedebit);

                    bOkToResolve = true;
                } else {
                    PayrollStatus oldPayrollStatus = payrollRun.getPayrollRunStatus();

                    payrollRun.updatePayrollRunStatus(PayrollStatus.DebitReturned);

                    // create a DebitReturned company event
                    CompanyEvent.createDDDebitReturnEvent(company, pSingleReturnedTransaction, pTxnReturn.getBankReturnCd(), oldPayrollStatus, PayrollStatus.DebitReturned, PSPDate.getPSPTime());

                    bOkToResolve = false;
                }

                // add a on-hold reason
                PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectR1R9);
            } else if (payrollStatus == PayrollStatus.OffloadedDebit) { /* OffloadedDebit or Canceled or Completed */
                // cancel pending EE DD Credit FTs
                payrollRun.cancelPayrollFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit});

                // create an NSFReturn/NSFPayrollCancelled company event
                CompanyEvent.createNSFReturnEvent(company, pSingleReturnedTransaction, pTxnReturn.getBankReturnCd(), NSFSubTypeType.NSFPayrollCancelled);

                // create an Intuit5DayReturnTransfer FT
                ProcessResult prUnhandled = PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId());

                if (!prUnhandled.isSuccess()) {
                    logger.error("Unhandled ProcessResult failure from FinancialTransactionManager.addIntuit5DayReturnTransferTransaction(): " + prUnhandled.toString());
                }

                // update the payroll run status
                payrollRun.updatePayrollRunStatus(PayrollStatus.NSFCanceled);

                // add an NSFPayrollCancelled strike
                company.addStrikeEvent(StrikeReason.NSFPayrollCancelled, "", PSPDate.getPSPTime(), pSingleReturnedTransaction);

                bOkToResolve = true;
            } else if (payrollStatus == PayrollStatus.Canceled) {
                // update the payroll run status
                payrollRun.updatePayrollRunStatus(PayrollStatus.NSFCanceled);

                // add an NSFPayrollCancelled strike
                company.addStrikeEvent(StrikeReason.NSFPayrollCancelled, "", PSPDate.getPSPTime(), pSingleReturnedTransaction);

                bOkToResolve = true;
            } else { // Completed
                PayrollStatus oldPayrollStatus = payrollRun.getPayrollRunStatus();

                payrollRun.updatePayrollRunStatus(PayrollStatus.DebitReturned);

                // add an NSFPayrollCancelled strike (create Strike event before DDDebit return event so email will see the strike)
                company.addStrikeEvent(StrikeReason.DebitReturned, "", PSPDate.getPSPTime(), pSingleReturnedTransaction);

                // create a DebitReturned company event
                CompanyEvent.createDDDebitReturnEvent(company, pSingleReturnedTransaction, pTxnReturn.getBankReturnCd(), oldPayrollStatus, PayrollStatus.DebitReturned, PSPDate.getPSPTime());

                PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectR1R9);

                bOkToResolve = false;
            }

            addRiskAssessmentHoldIfMeetsCriteria(company);
        }

        return bOkToResolve;
    }

    private void createRedebitFT(FinancialTransaction pDebitFT, CompanyBankAccount pComapanyBankAccount) {
        logger.info("createRedebitFT() ");

        Company company = pDebitFT.getCompany();

        TransactionTypeCode debitTypeCd = pDebitFT.getTransactionType().getTransactionTypeCd();

        TransactionType redebitType;
        switch (debitTypeCd) {
            case EmployerDdDebit:
                redebitType = TransactionType.findTransactionType(TransactionTypeCode.EmployerDdRedebit);
                break;

            case EmployerTaxDebit:
                redebitType = TransactionType.findTransactionType(TransactionTypeCode.EmployerTaxRedebit);
                break;

            case EmployerFeeDebit:
                redebitType = TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeRedebit);
                break;

            case ServiceSalesAndUseTax:
                redebitType = TransactionType.findTransactionType(TransactionTypeCode.ServiceSalesAndUseTaxRedebit);
                break;

            default:
                throw new RuntimeException("Unexpected TransactionTypeCode \"" + debitTypeCd + "\"");
        }

        IntuitBankAccount iba = IntuitBankAccount.findIntuitBankAccount(redebitType, CreditDebitCode.Credit);

        // compute the settlement date based on the company's offload group
        SpcfCalendar settlementDate = FinancialTransaction.getSettlementDate(redebitType.getTransactionTypeCd(), company.getOffloadGroup());

        // create the redebit
        FinancialTransaction redebitFT;
        redebitFT = FinancialTransaction.createFinancialTransaction(company,
                pDebitFT.getPayrollRun(),        //Payroll Run
                null,                            //Paycheck Split
                iba.getBankAccount(),            //Credit BankAccount
                pComapanyBankAccount.getBankAccount(), //Debit BankAccount
                BankAccountOwnerType.Intuit,     //credit bank account type
                BankAccountOwnerType.Company,    //debit bank account type
                redebitType.getTransactionTypeCd(),
                pDebitFT.getFinancialTransactionAmount(),
                SettlementType.ACH,
                settlementDate,
                pDebitFT.getSku(),               // sku
                pDebitFT,                        // orig txn
                pDebitFT.getSkuQuantity());      // quantity

        // create a TransactionResponse for it
        TransactionResponse.createTransactionResponseForFinancialTx(redebitFT);
    }

    private boolean handleEmployeeCredit(FinancialTransaction pSingleReturnedTransaction, TransactionReturn pTxnReturn, boolean isNsf) {
        logger.info("handleEmployeeCredit");

        boolean bOkToResolve = false;
        boolean isBillPayment;

        // set the FT's state to Returned and create a TransactionResponse for it
        returnFinancialTransaction(pTxnReturn, pSingleReturnedTransaction, true);

        Company company = pSingleReturnedTransaction.getCompany();
        DomainEntitySet<FinancialTransaction> employerDdDebitFTs;

        employerDdDebitFTs = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(pSingleReturnedTransaction.getPayrollRun(), TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Cancelled);

        isBillPayment = PayrollType.BillPayment.equals(pSingleReturnedTransaction.getPayrollRun().getPayrollRunType());

        FinancialTransaction firstErDdDebitFT;
        if (isBillPayment && employerDdDebitFTs.size() > 1) {
            firstErDdDebitFT = pSingleReturnedTransaction.getRelatableTransaction();
        } else {
            //Most of the time there will only ever be one
            firstErDdDebitFT = employerDdDebitFTs.get(0);
        }

        //The refund will go to the active CBA if the company is in the right state
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);

        // if we have an Active CBA, and if the company is in an acceptable state, refund the money
        boolean bBankAccountOK = (cba != null);
        boolean bCompanyOK = (!company.isCompanyCancelled() && !company.isCompanyOnHold() && !company.isCompanyTerminated());
        if (bBankAccountOK && bCompanyOK && !company.isCompanyInDebtToIntuit()) {
            //Only resolve if the process was successful
            createDirectDepositRefundTransaction(pSingleReturnedTransaction, cba, firstErDdDebitFT);
            
            // Only resolve if this is an NSF (R01/R09).  Would we ever have an NSF for an Employee Credit?
            if (isNsf) {
                bOkToResolve = true;
            }

        } else {
            bOkToResolve = false;
        }

        //Create DDReject System Event Rule
        createDirectDepositRejectEvent(cba, pSingleReturnedTransaction, pTxnReturn.getBankReturnCd(), isBillPayment);

        return bOkToResolve;
    }

    private void createDirectDepositRefundTransaction(FinancialTransaction financialTransaction, CompanyBankAccount cba, FinancialTransaction erDB) {
        Company company = financialTransaction.getCompany();
        PayrollRun payrollRun = financialTransaction.getPayrollRun();

        TransactionType ftType;
        ftType = TransactionType.findTransactionType(TransactionTypeCode.EmployerDdRejectRefundCredit);

        IntuitBankAccount iba = IntuitBankAccount.findIntuitBankAccount(ftType, CreditDebitCode.Debit);

        FinancialTransaction finTxn = FinancialTransaction.createFinancialTransaction(company,
                payrollRun,                 //Payroll Run
                null,                       //Paycheck Split
                cba.getBankAccount(),       //Credit BankAccount
                iba.getBankAccount(),       //Debit BankAccount
                BankAccountOwnerType.Company,
                BankAccountOwnerType.Intuit,
                ftType.getTransactionTypeCd(),
                financialTransaction.getFinancialTransactionAmount(),
                SettlementType.ACH,
                erDB.getRefundTransactionSettlementDate(),
                null,                       // SKU
                financialTransaction,       // original transactions
                0);                         // quantity

        // create a TransactionResponse for it
        TransactionResponse.createTransactionResponseForFinancialTx(finTxn);
    }

    private void createDirectDepositRejectEvent(CompanyBankAccount pCompanyBankAccount, FinancialTransaction pReturnedFT, String pBankReturnCd, boolean isBillPayment) {

        Company company = pReturnedFT.getCompany();

        RefundStatusType refundStatusType = RefundStatusType.Issued;
        RefundStatusReasonType refundStatusReasonType = null;

        if ((pCompanyBankAccount == null || pCompanyBankAccount.getStatusCd() != BankAccountStatus.Active) || company.isCompanyOnHold() || company.isCompanyCancelled() || company.isCompanyTerminated()) {

            refundStatusType = RefundStatusType.NotIssued;

            if (company.isCompanyOnHold()) {
                refundStatusReasonType = RefundStatusReasonType.CompanyOnHold;
            } else if (company.isCompanyTerminated()) {
                refundStatusReasonType = RefundStatusReasonType.CompanyTerminated;
            } else if (company.isCompanyCancelled()) {
                refundStatusReasonType = RefundStatusReasonType.CompanyCancelled;
            } else if (pCompanyBankAccount == null || pCompanyBankAccount.getStatusCd() != BankAccountStatus.Active) {
                refundStatusReasonType = RefundStatusReasonType.BankAccountInactive;
            }
        }

        // create a company event
        CompanyEvent.createDDRejectEvent(pReturnedFT.getCompany(), pReturnedFT, pBankReturnCd, refundStatusType,
                                         refundStatusReasonType, pReturnedFT.getSettlementDate().toLocal(), isBillPayment);
    }

    private boolean handleEmployeeReversalDebit(FinancialTransaction pSingleReturnedTransaction, TransactionReturn pTxnReturn) {
        logger.info("handleEmployeeReversalDebit");

        PayrollRun payrollRun = pSingleReturnedTransaction.getPayrollRun();
        PayrollStatus payrollStatus = payrollRun.getPayrollRunStatus();

        // set the FT state to "Returned" -- no TransactionResponse though
        returnFinancialTransaction(pTxnReturn, pSingleReturnedTransaction, true);

        // create a company event
        CompanyEvent.createReversalReturnEvent(pSingleReturnedTransaction.getCompany(), pSingleReturnedTransaction, pTxnReturn.getBankReturnCd());

        // if the reversal was Intuit-initiated, then we might need to update the payroll status...
        if (!pSingleReturnedTransaction.isReversalClientRequested()) {
            // If the current status is Complete or WrittenOff, update the payroll status to reversals finished and
            // put the company on hold for Risk Collections
            if (PayrollStatus.Complete.equals(payrollStatus) || PayrollStatus.WrittenOff.equals(payrollStatus)) {
                payrollRun.updatePayrollRunStatus(PayrollStatus.ReversalsFinished);
            }
        }

        // as far as this handler is concerned, it's OK to resolve this TransactionReturn
        return true;
    }

    private boolean handleBankVerificationDebit(FinancialTransaction pSingleReturnedTransaction, TransactionReturn pTxnReturn) {
        logger.info("handleBankVerificationDebit");

        // set the FT state to Returned - no TransactionResponse though
        pSingleReturnedTransaction = returnFinancialTransaction(pTxnReturn, pSingleReturnedTransaction, false);

        //Get the company associated with the financial transaction
        Company company = pSingleReturnedTransaction.getCompany();

        //Get the bank account associated with the financial transaction
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, pSingleReturnedTransaction.getDebitBankAccount());

        if (companyBankAccount != null) {
            //If the bank account is active, put the company on hold for fraud review
            if (BankAccountStatus.Active.equals(companyBankAccount.getStatusCd())) {
                PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.Fraud);
            } else if (BankAccountStatus.PendingVerification.equals(companyBankAccount.getStatusCd())) {
                //If the company's source system requires it, deactivate the bank account
                SourcePayrollParameter deactivateOnReturnedERVERDB = SourcePayrollParameter.findSourcePayrollParameter(company.getSourceSystemCd(), SourcePayrollParameterCode.DeactiveBankAccountOnReturnedVerificationDebit);
                if ("1".equals(deactivateOnReturnedERVERDB.getParameterValue())) {
                    //Ignore pending transactions.  Since the bank account is pending verification, the only pending transaction
                    // there may be is another unresolved bank return for an ERVERDB
                    ProcessResult prUnhandled = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), true, true);
                    if (!prUnhandled.isSuccess()) {
                        logger.error("Unhandled ProcessResult failure from CompanyManager.deactivateCompanyBankAccount(): " + prUnhandled.toString());
                    }
                }
            }
        }

        // create Intuit Employer Verification Transfer FT
        createXferFT(pSingleReturnedTransaction);

        VerificationStatusType cbaVerificationStatus = null;
        if (companyBankAccount == null) {
            cbaVerificationStatus = VerificationStatusType.CBADeactivated;
        } else {
            switch (companyBankAccount.getStatusCd()) {
                case Active: // TODO v2 WK: Refactor CBAVerificationReturnEvent to include bank account status instead of VerificationStatus
                case PendingVerification:
                    cbaVerificationStatus = VerificationStatusType.PendingVerification;
                    break;

                case Inactive:
                    cbaVerificationStatus = VerificationStatusType.CBADeactivated;
                    break;

                default:
                    throw new RuntimeException("Invalid bank account status: " + companyBankAccount.getStatusCd());
            }
        }

        // create a company event
        CompanyEvent.createCBAVerificationReturnEvent(company, pSingleReturnedTransaction, pTxnReturn.getBankReturnCd(), cbaVerificationStatus, PSPDate.getPSPTime());


        return true; // as far as this handler is concerned, it's OK to resolve this TransactionReturn
    }

    private boolean handleBankVerificationCredit(FinancialTransaction pSingleReturnedTransaction, TransactionReturn pTxnReturn) {
        logger.info("handleBankVerificationCredit");

        // set the FT state to Returned - no TransactionResponse though
        pSingleReturnedTransaction = returnFinancialTransaction(pTxnReturn, pSingleReturnedTransaction, false);
        MoneyMovementTransaction moneyMovementTransaction = pSingleReturnedTransaction.getMoneyMovementTransaction();
        MoneyMovementTransaction newMmt = null;
        newMmt = new MoneyMovementTransaction();
        newMmt.setMoneyMovementPaymentMethod(null);
        newMmt.setStatus(PaymentStatus.Created);
        newMmt.setDueDate(moneyMovementTransaction.getDueDate());
        newMmt.setMoneyMovementTransactionAmount(moneyMovementTransaction.getMoneyMovementTransactionAmount());
        newMmt.setPaymentFrequency(moneyMovementTransaction.getPaymentFrequency());
        newMmt.setPaymentTemplate(moneyMovementTransaction.getPaymentTemplate());
        newMmt.setPaymentPeriodBegin(moneyMovementTransaction.getPaymentPeriodBegin());
        newMmt.setPaymentPeriodEnd(moneyMovementTransaction.getPaymentPeriodEnd());
        newMmt.setCompany(moneyMovementTransaction.getCompany());
        SpcfCalendar nextInitiationDate = MoneyMovementTransaction.getNextInitiationDate(moneyMovementTransaction.getMoneyMovementPaymentMethod());
        newMmt.setInitiationDate(nextInitiationDate);
        newMmt.setMoneyMovementPaymentMethod(moneyMovementTransaction.getMoneyMovementPaymentMethod());
        newMmt.setOriginalInitiationDate(moneyMovementTransaction.getOriginalInitiationDate());
        newMmt.setTaxPaymentStatus(moneyMovementTransaction.getTaxPaymentStatus());
        newMmt.setAgencyTaxpayerId(moneyMovementTransaction.getAgencyTaxpayerId());
        newMmt.setOriginalTransaction(moneyMovementTransaction);
        newMmt.setTaxPaymentStatusEffectiveDate(PSPDate.getPSPTime());
        newMmt.setReferenceNumber(moneyMovementTransaction.getReferenceNumber());
        newMmt.setTransactionNumber(moneyMovementTransaction.getNextTransactionNumber());
        newMmt.setManualPaymentStatus(moneyMovementTransaction.getManualPaymentStatus());
        newMmt.setDepositFrequencyFk(moneyMovementTransaction.getDepositFrequencyFk());
        Application.save(newMmt);


        FinancialTransaction financialTransaction = new FinancialTransaction();
        Company company = pSingleReturnedTransaction.getCompany();

        financialTransaction.setCompany(company);
        financialTransaction.setSku(pSingleReturnedTransaction.getSku());
        financialTransaction.setSkuQuantity(pSingleReturnedTransaction.getSkuQuantity());

        TransactionType transactionType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerVerificationCreditReturnTransfer);
        financialTransaction.setTransactionType(transactionType);

        financialTransaction.setDebitBankAccountType(BankAccountOwnerType.Intuit);
        financialTransaction.setDebitBankAccount(IntuitBankAccount.findIntuitBankAccountByName(IntuitBankAccount.Name.INTUIT_ER_RETURN).getBankAccount());

        financialTransaction.setCreditBankAccountType(BankAccountOwnerType.Intuit);
        financialTransaction.setCreditBankAccount(IntuitBankAccount.findIntuitBankAccountByName(IntuitBankAccount.Name.INTUIT_FEE).getBankAccount());

        // Settlement type is ACH
        financialTransaction.setSettlementTypeCd(SettlementType.ACH);

        // Settlement Date
        OffloadGroup offloadGroup = company.getOffloadGroup();
        SpcfCalendar settlementDate = FinancialTransaction.getSettlementDate(TransactionTypeCode.EmployerVerificationCredit, offloadGroup);
        financialTransaction.setSettlementDate(settlementDate);
        financialTransaction.setOriginalSettlementDate(settlementDate);


        financialTransaction.setFinancialTransactionAmount(pSingleReturnedTransaction.getFinancialTransactionAmount());
        financialTransaction.setMoneyMovementTransaction(newMmt);

        // Add the FinancialTransactionState object for the current State
        TransactionState currentTransactionState = Application.findById(TransactionState.class, TransactionStateCode.Created);
        financialTransaction = Application.save(financialTransaction);
        financialTransaction.addTransactionState(currentTransactionState);
        financialTransaction = Application.save(financialTransaction);

        financialTransaction.validateCanCreateFinancialTransaction();
        return true; // as far as this handler is concerned, it's OK to resolve this TransactionReturn

    }

    private void createXferFT(FinancialTransaction pFT) {
        logger.info("createXferFT()");

        TransactionType ftType = TransactionType.findTransactionType(TransactionTypeCode.IntuitEmployerVerificationReturnTransfer);

        // get the credit and debit bank accounts for this FT type
        IntuitBankAccount baCredit = IntuitBankAccount.findIntuitBankAccount(ftType, CreditDebitCode.Credit);
        IntuitBankAccount baDebit = IntuitBankAccount.findIntuitBankAccount(ftType, CreditDebitCode.Debit);

        // figure out the settlement date based on the offload-group for this company
        SpcfCalendar settlementDate = FinancialTransaction.getSettlementDate(pFT.getCompany().getOffloadGroup());
        FinancialTransaction.createFinancialTransaction(pFT.getCompany(),
                null,                          //Payroll Run
                null,                          //Paycheck Split
                baCredit.getBankAccount(),
                baDebit.getBankAccount(),
                BankAccountOwnerType.Intuit,
                BankAccountOwnerType.Intuit,
                ftType.getTransactionTypeCd(),
                pFT.getFinancialTransactionAmount(),
                SettlementType.ACH,
                settlementDate,
                pFT.getSku(),  // sku
                pFT,  // original transaction
                1);   // sku quantity
    }

    private boolean handleRedebitOnlyNSF(FinancialTransaction pSingleReturnedTransaction, TransactionReturn pTxnReturn) {
        logger.info("handleRedebitOnlyNSF");
        boolean resolveReturn = true;

        returnFinancialTransaction(pTxnReturn, pSingleReturnedTransaction, true);

        Company company = pSingleReturnedTransaction.getCompany();
        PayrollRun payrollRun = pSingleReturnedTransaction.getPayrollRun();

        // update the PayrollRun status if the company has a balance due for the payroll
        if (!isCompanyBalanceDueCoveredForPayroll(payrollRun)) {
            // cancel any pending DD Refund FTs related to this payroll
            payrollRun.cancelPayrollFinancialTransactions(TransactionType.getRefundTypesForService((Service) Application.findById(Service.class, ServiceCode.DirectDeposit)));

            payrollRun.updatePayrollRunStatus(PayrollStatus.ReturnedTwice);

            // add an on-hold reason
            PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectR1R9);

            resolveReturn = false;

            // create a company event
            CompanyEvent.createNSFReturnEvent(company, pSingleReturnedTransaction, pTxnReturn.getBankReturnCd(), NSFSubTypeType.SecondNSF);
        }

        return resolveReturn;
    }

    private boolean handleConsolidatedRedebitNSF(DomainEntitySet<FinancialTransaction> pReturnedFTs, TransactionReturn pTxnReturn) {
        logger.info("handleConsolidatedRedebitNSF");
        boolean resolveReturn = true;

        for (FinancialTransaction currTransaction : pReturnedFTs) {
            returnFinancialTransaction(pTxnReturn, currTransaction, true);
        }

        FinancialTransaction firstTransaction = pReturnedFTs.get(0);
        Company company = firstTransaction.getCompany();
        PayrollRun payrollRun = firstTransaction.getPayrollRun();

        // update the PayrollRun status if the company still owes Intuit $ for the payroll
        if (!isCompanyBalanceDueCoveredForPayroll(payrollRun)) {
            // cancel any pending DD Refund FTs related to this payroll
            payrollRun.cancelPayrollFinancialTransactions(TransactionType.getRefundTypesForService((Service) Application.findById(Service.class, ServiceCode.DirectDeposit)));

            payrollRun.updatePayrollRunStatus(PayrollStatus.ReturnedTwice);

            // add an on-hold reason
            PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectR1R9);

            resolveReturn = false;

            // create a company event
            CompanyEvent.createNSFReturnEvent(company, pReturnedFTs, pTxnReturn.getBankReturnCd(), NSFSubTypeType.SecondNSF);
        }

        return resolveReturn;
    }

    private boolean handleCombinedRefundAndDebitNetDebit(DomainEntitySet<FinancialTransaction> pReturnedFTs, TransactionReturn pTxnReturn) {
        logger.info("handleCombinedRefundAndDebitNetDebit");

        FinancialTransaction firstFinancialTransaction = pReturnedFTs.get(0);
        Company company = firstFinancialTransaction.getCompany();
        PayrollRun payrollRun = firstFinancialTransaction.getPayrollRun();
        PayrollStatus oldPayrollStatus = payrollRun.getPayrollRunStatus();

        for (FinancialTransaction currTransaction : pReturnedFTs) {
            returnFinancialTransaction(pTxnReturn, currTransaction, true);
        }

        payrollRun.updatePayrollRunStatus(PayrollStatus.DebitReturned);

        CompanyEvent.createDDDebitReturnEvent(company, pReturnedFTs, pTxnReturn.getBankReturnCd(), oldPayrollStatus, PayrollStatus.DebitReturned, PSPDate.getPSPTime());

        return false;
    }

    private boolean handleCombinedRefundAndDebitNetCredit(DomainEntitySet<FinancialTransaction> pReturnedFTs, TransactionReturn pTxnReturn) {
        logger.info("handleCombinedRefundAndDebitNetCredit");
        // set the FT state to "Returned" and create a TransactionResponse for it
        FinancialTransaction firstTransaction = pReturnedFTs.get(0);
        Company company = firstTransaction.getCompany();

        for (FinancialTransaction currTransaction : pReturnedFTs) {
            returnFinancialTransaction(pTxnReturn, currTransaction, true);
        }

        // create a company event
        CompanyEvent.createERRefundReturnEvent(company, pReturnedFTs, pTxnReturn.getBankReturnCd());

        // this one should not be Resolved
        return false;
    }

    private boolean handleDefaultCase(DomainEntitySet<FinancialTransaction> pReturnedFTs, TransactionReturn pTxnReturn) {
        logger.info("handleDefaultCase");

        FinancialTransaction firstFinancialTransaction = pReturnedFTs.get(0);

        if (!firstFinancialTransaction.isTaxPaymentTransaction()) {
            for (FinancialTransaction currTransaction : pReturnedFTs) {
                returnFinancialTransaction(pTxnReturn, currTransaction, true);
            }
        }

        return false;
    }

    private boolean canRedebitFeeTransaction(FinancialTransaction pFinancialTransaction) {
        boolean canRedebit = false;

        if (pFinancialTransaction != null) {
            if (!TransactionType.isRedebitTransactionType(pFinancialTransaction.getTransactionType().getTransactionTypeCd())) {
                PayrollRun payrollRun = pFinancialTransaction.getPayrollRun();

                if (payrollRun != null) {
                    switch (payrollRun.getPayrollRunType()) {
                        case BillPayment:
                            canRedebit = true;
                            break;

                        case FeeOnly:
                            canRedebit = pFinancialTransaction.isPayrollSkuType() || pFinancialTransaction.isW2FeeChargeType();
                            break;
                    }
                }
            }
        }

        return canRedebit;
    }

    private boolean handleFeeAndOrTaxDebitRedebitOnly(DomainEntitySet<FinancialTransaction> pReturnedFTs, TransactionReturn pTxnReturn, boolean isNsf) {
        logger.info("handleFeeAndOrTaxDebitRedebitOnly");
        FinancialTransaction firstTransaction = pReturnedFTs.get(0);
        Company company = firstTransaction.getCompany();
        PayrollRun payrollRun = firstTransaction.getPayrollRun();
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        boolean cancelRefunds = true;
        boolean foundRedebit = false;
        boolean createdRedebit = false;
        boolean resolveReturn = true;

        // Only assess a fee if this was a Debit NSF, the payroll run is of type FeeOnly, and the transaction types are "System Generated".
        if(cba != null && isNsf && firstTransaction.getPayrollRun().getPayrollRunType().equals(PayrollType.FeeOnly) && FinancialTransaction.isSystemGeneratedFeesOnly(pReturnedFTs)) {
            Fee feeOnlyNsfFee = Application.find(Fee.class, Fee.FeeCd().equalTo(FeeTypeCode.FeeOnlyNSFFee)).getFirst();
            createDebitReturnFee(pReturnedFTs.getFirst().getPayrollRun(), feeOnlyNsfFee.getAmount());
        }

        for (FinancialTransaction currTransaction : pReturnedFTs) {
            returnFinancialTransaction(pTxnReturn, currTransaction, true);

            if (currTransaction.getTransactionType().getFeeInd()) {
                //Create fee debit return event
                CompanyEvent.createFeeDebitReturnEvent(company, currTransaction, pTxnReturn.getBankReturnCd());
            } else {
                //Create sales tax debit return event
                CompanyEvent.createSalesTaxDebitReturnEvent(company, currTransaction, pTxnReturn.getBankReturnCd());
            }

            if (TransactionType.isRedebitTransactionType(currTransaction.getTransactionType().getTransactionTypeCd())) {
                foundRedebit = true;
            }

            // Only ReDebit if we have a Company Bank Account, this is an NSF, and this is an appropriate transaction for ReDebit.
            if ((cba != null) && isNsf && canRedebitFeeTransaction(currTransaction)) {
                createRedebitFT(currTransaction, cba);
                createdRedebit = true;
            }
        }

        // PSRV003941 - Put a hold on Debit OR Redebit return so that company is unable to continue submitting payrolls until resolved.
        if (isNsf) {
            PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectR1R9);
        } else {
            PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectOther);
        }

        if (foundRedebit) {
            // update the PayrollRun status if the company has a balance due for the payroll
            if (!isCompanyBalanceDueCoveredForPayroll(payrollRun)) {
                if (payrollRun.getPayrollRunType() != PayrollType.BillPayment) {
                    payrollRun.updatePayrollRunStatus(PayrollStatus.ReturnedTwice);
                }
                resolveReturn = false;
                CompanyEvent.createNSFReturnEvent(company, pReturnedFTs, pTxnReturn.getBankReturnCd(), NSFSubTypeType.SecondNSF);
            } else {
                cancelRefunds = false;
            }
        } else if (createdRedebit) {
            payrollRun.updatePayrollRunStatus(PayrollStatus.PendingAutoRedebit);
            CompanyEvent.createNSFReturnEvent(company, pReturnedFTs, pTxnReturn.getBankReturnCd(), NSFSubTypeType.NSFAutoRedebit);
        } else {
            if (payrollRun.getPayrollRunType() != PayrollType.BillPayment) {
                CompanyEvent.createDDDebitReturnEvent(company, pReturnedFTs, pTxnReturn.getBankReturnCd(),
                                                      payrollRun.getPayrollRunStatus(), PayrollStatus.DebitReturned,
                                                      PSPDate.getPSPTime());
                payrollRun.updatePayrollRunStatus(PayrollStatus.DebitReturned);
            }
        }
        
        if (cancelRefunds) {
            // cancel any pending Refund FTs related to this payroll for DD only
            payrollRun.cancelPayrollFinancialTransactions(TransactionType.getRefundTypesForService(Application.findById(Service.class, ServiceCode.DirectDeposit)));
        }

        addRiskAssessmentHoldIfMeetsCriteria(company);

        return resolveReturn;
    }

    private boolean isCombinedRefundAndDebitNetDebit(DomainEntitySet<FinancialTransaction> finTxns) {
        return isCombinedRefundAndDebit(finTxns, CreditDebitCode.Debit);
    }

    private boolean isCombinedRefundAndDebitNetCredit(DomainEntitySet<FinancialTransaction> finTxns) {
        return isCombinedRefundAndDebit(finTxns, CreditDebitCode.Credit);
    }

    private boolean isCombinedRefundAndDebit(DomainEntitySet<FinancialTransaction> finTxns, CreditDebitCode pCreditOrDebit) {
        boolean bFoundRefund = false;
        boolean bFoundDebit = false;
        boolean bFoundOtherType = false;
        SpcfMoney totalRefund = new SpcfMoney("0.00");
        SpcfMoney totalDebit = new SpcfMoney("0.00");

        if (finTxns.size() < 2) {
            return false;
        }

        for (FinancialTransaction currTxn : finTxns) {
            boolean transactionIsRefund = TransactionAssociationType.Refund == currTxn.getTransactionType().getAssociationType();
            boolean transactionIsDebit = (TransactionTypeGroupCode.Debit == currTxn.getTransactionType().getTransactionTypeGroupCd()) ||
                    (TransactionTypeGroupCode.Redebit == currTxn.getTransactionType().getTransactionTypeGroupCd());

            if (transactionIsRefund) {
                bFoundRefund = true;
                totalRefund = new SpcfMoney(totalRefund.add(currTxn.getFinancialTransactionAmount()));
            } else if (transactionIsDebit) {
                bFoundDebit = true;
                totalDebit = new SpcfMoney(totalDebit.add(currTxn.getFinancialTransactionAmount()));
            } else {
                bFoundOtherType = true;
            }
        }

        boolean bIsNetDebit = totalRefund.compareTo(totalDebit) < 0;
        boolean bIsNetCredit = totalRefund.compareTo(totalDebit) > 0;

        if (CreditDebitCode.Credit == pCreditOrDebit) {
            return bFoundRefund && bFoundDebit && !bFoundOtherType && bIsNetCredit;
        } else {
            return bFoundRefund && bFoundDebit && !bFoundOtherType && bIsNetDebit;
        }


    }

    public static boolean isFeeAndOrTaxDebitRedebitOnly(DomainEntitySet<FinancialTransaction> finTxns) {

        if (finTxns.size() == 0) {
            return false;
        }

        ArrayList<TransactionTypeCode> FT_TYPES = new ArrayList<TransactionTypeCode>();
        FT_TYPES.add(TransactionTypeCode.EmployerFeeDebit);
        FT_TYPES.add(TransactionTypeCode.EmployerFeeRedebit);
        FT_TYPES.add(TransactionTypeCode.ServiceSalesAndUseTax);
        FT_TYPES.add(TransactionTypeCode.ServiceSalesAndUseTaxRedebit);

        //If any of the transactions are a redebit, it is a consolidated redebit
        for (FinancialTransaction currTxn : finTxns) {
            if (!FT_TYPES.contains(currTxn.getTransactionType().getTransactionTypeCd())) {
                return false;
            }
        }

        return true;
    }

    private boolean isConsolidatedRedebit(DomainEntitySet<FinancialTransaction> finTxns) {
        //If there's only one transaction, it's not consolidated
        if (finTxns.size() <= 1) {
            return false;
        }

        //If any of the transactions are a redebit, it is a consolidated redebit
        for (FinancialTransaction currTxn : finTxns) {
            if (isErRedebitTransaction(currTxn)) {
                return true;
            }
        }

        return false;
    }

    private boolean handleConsolidatedDebitRedebitNonNSF(DomainEntitySet<FinancialTransaction> returnedFTs, TransactionReturn pTxnReturn) {
        logger.info("handleConsolidatedDebitRedebitNonNSF");

        boolean bOkToResolve = false;
        boolean createEvent = true;
        boolean cancelRefunds = true;
        boolean isErDebitFound = false;

        FinancialTransaction firstTransaction = returnedFTs.get(0);
        Company company = firstTransaction.getCompany();
        PayrollRun payrollRun = firstTransaction.getPayrollRun();
        PayrollStatus payrollStatus = payrollRun.getPayrollRunStatus();

        for (FinancialTransaction currTransaction : returnedFTs) {
            returnFinancialTransaction(pTxnReturn, currTransaction, true);

            if (TransactionType.isImpoundTransactionType(currTransaction.getTransactionType().getTransactionTypeCd())) {
                isErDebitFound = true;
            }
        }

        if (payrollStatus.equals(PayrollStatus.OffloadedDebit)) {
            // add a DebitReturnedCanceled strike
            company.addStrikeEvent(StrikeReason.DebitReturnedCanceled, "", PSPDate.getPSPTime(), returnedFTs);

            // cancel any EE DD Credit FTs related to this payroll
            payrollRun.cancelPayrollFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit});

            // update the PayrollRun status
            payrollRun.updatePayrollRunStatus(PayrollStatus.DebitReturnedCanceled);

            // create a DebitReturnedCanceled company event
            CompanyEvent.createDDDebitReturnEvent(company, returnedFTs, pTxnReturn.getBankReturnCd(), payrollStatus, PayrollStatus.DebitReturnedCanceled, PSPDate.getPSPTime());

            // create an Intuit5DayReturnTransaction FT
            ProcessResult prUnhandled = PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId());

            if (!prUnhandled.isSuccess()) {
                logger.error("Unhandled ProcessResult failure from FinancialTransactionManager.addIntuit5DayReturnTransferTransaction(): " + prUnhandled.toString());
            }

            bOkToResolve = true;
        } else {
            PayrollStatus newPayrollStatus = null;

            //Update payrollrun Status rule
            if (isErDebitFound) {
                newPayrollStatus = PayrollStatus.DebitReturned;

                // add an on-hold reason
                PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectOther);

                // add a DebitReturned strike
                company.addStrikeEvent(StrikeReason.DebitReturned, "", PSPDate.getPSPTime(), returnedFTs);

                bOkToResolve = false;
            } else {
                //
                // update the PayrollRun status & don't resolve return if the company has a balance due for the payroll
                //

                if (!isCompanyBalanceDueCoveredForPayroll(payrollRun)) {
                    newPayrollStatus = PayrollStatus.ReturnedTwice;

                    // add an on-hold reason
                    PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectOther);

                    bOkToResolve = false;
                } else {
                    bOkToResolve = true;
                    cancelRefunds = false;
                    createEvent = false;
                }
            }

            if (newPayrollStatus != null) {
                payrollRun.updatePayrollRunStatus(newPayrollStatus);
            } else {
                //todo udpate once Shannon updates returns doc
                //just setting this for the event
                newPayrollStatus = PayrollStatus.DebitReturned;
            }

            if (createEvent) {
                // create a DebitReturned company event
                CompanyEvent.createDDDebitReturnEvent(company, returnedFTs, pTxnReturn.getBankReturnCd(), payrollStatus, newPayrollStatus, PSPDate.getPSPTime());
            }
        }


        if (cancelRefunds) {
            // cancel any pending Refund FTs related to this payroll
            payrollRun.cancelPayrollFinancialTransactions(TransactionType.getRefundTypesForService((Service) Application.findById(Service.class, ServiceCode.DirectDeposit)));
        }

        // todo: cancel AgencyTaxDebits?

        addRiskAssessmentHoldIfMeetsCriteria(company);

        return bOkToResolve;
    }

    private boolean handleConsolidatedDebitNSF(DomainEntitySet<FinancialTransaction> returnedFTs, TransactionReturn pTxnReturn) {
        boolean bOkToResolve;

        logger.info("Consolidated Debit NSF");

        FinancialTransaction firstTransaction = returnedFTs.get(0);
        Company company = firstTransaction.getCompany();
        PayrollRun payrollRun = firstTransaction.getPayrollRun();
        PayrollStatus payrollStatus = payrollRun.getPayrollRunStatus();

        DomainEntitySet<FinancialTransaction> erAppliedTransactions = new DomainEntitySet<FinancialTransaction>();
        for (FinancialTransaction currTransaction : returnedFTs) {
            returnFinancialTransaction(pTxnReturn, currTransaction, true);

            if (currTransaction.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerTaxCreditApplied) ||
                    currTransaction.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerTaxOverpaymentApplied)) {
                erAppliedTransactions.add(currTransaction);
            }
        }

        returnedFTs.removeAll(erAppliedTransactions);

        // cancel any pending DD Refund FTs related to this payroll
        payrollRun.cancelPayrollFinancialTransactions(TransactionType.getRefundTypesForService(Application.findById(Service.class, ServiceCode.DirectDeposit)));

        if (payrollStatus == PayrollStatus.OffloadedAll) {
            // add an NSFAutoRedebit strike (create Strike event before NSF return event so email will see the strike)
            company.addStrikeEvent(StrikeReason.NSFAutoRedebit, "", PSPDate.getPSPTime(), returnedFTs);

            //Only create the redebits if the company has an active bank account
            CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
            if (cba != null) {
                boolean hasDebitReturnFee = false;
                for (BillingDetail billingDetail : payrollRun.getBillingDetailCollection()) {
                    if (billingDetail.getOfferingServiceChargeType().equals(OfferingServiceChargeType.DebitReturnFee)) {
                        hasDebitReturnFee = true;
                        break;
                    }
                }

                if (!hasDebitReturnFee) {
                    createDebitReturnFee(payrollRun);
                }

                //verify if this return is a consolidation of tax debit and tax refund
                for (FinancialTransaction currTransaction : returnedFTs) {
                    createRedebitFT(currTransaction, cba);
                }

                payrollRun.updatePayrollRunStatus(PayrollStatus.PendingAutoRedebit);

                CompanyEvent.createNSFReturnEvent(company, returnedFTs, pTxnReturn.getBankReturnCd(), NSFSubTypeType.NSFAutoRedebit);

                bOkToResolve = true;
            } else {
                PayrollStatus oldPayrollStatus = payrollRun.getPayrollRunStatus();

                payrollRun.updatePayrollRunStatus(PayrollStatus.DebitReturned);

                // create a DebitReturned company event
                CompanyEvent.createDDDebitReturnEvent(company, returnedFTs, pTxnReturn.getBankReturnCd(), oldPayrollStatus, PayrollStatus.DebitReturned, PSPDate.getPSPTime());

                bOkToResolve = false;
            }

            // add a on-hold reason
            PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectR1R9);
        } else if (payrollStatus == PayrollStatus.OffloadedDebit) { /* OffloadedDebit or Canceled or Completed */
            // cancel pending EE DD Credit FTs
            payrollRun.cancelPayrollFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit});

            // create an NSFReturn/NSFPayrollCancelled company event
            CompanyEvent.createNSFReturnEvent(company, returnedFTs, pTxnReturn.getBankReturnCd(), NSFSubTypeType.NSFPayrollCancelled);

            // create an Intuit5DayReturnTransfer FT
            ProcessResult prUnhandled = PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId());

            if (!prUnhandled.isSuccess()) {
                logger.error("Unhandled ProcessResult failure from FinancialTransactionManager.addIntuit5DayReturnTransferTransaction(): " + prUnhandled.toString());
            }

            // update the payroll run status
            payrollRun.updatePayrollRunStatus(PayrollStatus.NSFCanceled);

            // add an NSFPayrollCancelled strike
            company.addStrikeEvent(StrikeReason.NSFPayrollCancelled, "", PSPDate.getPSPTime(), returnedFTs);

            bOkToResolve = true;
        } else if (payrollStatus == PayrollStatus.Canceled) {
            // update the payroll run status
            payrollRun.updatePayrollRunStatus(PayrollStatus.NSFCanceled);

            // add an NSFPayrollCancelled strike
            company.addStrikeEvent(StrikeReason.NSFPayrollCancelled, "", PSPDate.getPSPTime(), returnedFTs);

            bOkToResolve = true;
        } else { // Completed
            PayrollStatus oldPayrollStatus = payrollRun.getPayrollRunStatus();
            payrollRun.updatePayrollRunStatus(PayrollStatus.DebitReturned);

            // add an NSFPayrollCancelled strike (create Strike event before DDDebit return event so email will see the strike)
            company.addStrikeEvent(StrikeReason.DebitReturned, "", PSPDate.getPSPTime(), returnedFTs);

            // create a DebitReturned company event
            CompanyEvent.createDDDebitReturnEvent(company, returnedFTs, pTxnReturn.getBankReturnCd(), oldPayrollStatus, PayrollStatus.DebitReturned, PSPDate.getPSPTime());

            PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectR1R9);

            bOkToResolve = false;
        }

        addRiskAssessmentHoldIfMeetsCriteria(company);

        return bOkToResolve;
    }

    private void addRiskAssessmentHoldIfMeetsCriteria(Company pCompany) {
        DomainEntitySet<CompanyEvent> strikes = pCompany.getCurrentStrikeEvents();
        int successfulPayrollCount = PayrollRun.findPayrollRunsByState(pCompany, PayrollStatus.Complete).size();

        Long as400PayrollCount = 0L;
        if (pCompany.getQuickbooksInfo() != null) {
            as400PayrollCount = pCompany.getQuickbooksInfo().getAS400PayrollCount();
        }

        if (strikes.size() == 4) {
            // TODO: Do we need to call CompanyEvent.createLastChanceNotifyEvent here instead? If so, where do we get wire expected date?
            //Create LastChanceNotify event
            CompanyEvent.createCompanyEvent(pCompany, EventTypeCode.LastChanceNotify);
        }
    }

    private void createDebitReturnFee(PayrollRun pPayrollRun) {
        createDebitReturnFee(pPayrollRun, null);
    }
    
    private void createDebitReturnFee(PayrollRun pPayrollRun, SpcfMoney overrideFeeAmount) {
        logger.info("createDebitReturnFee()");

        Company company = pPayrollRun.getCompany();

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);

        ERFeeAddDTO erFeeAddDTO = new ERFeeAddDTO();
        erFeeAddDTO.setFeeTypeCode(OfferingServiceChargeType.DebitReturnFee);
        erFeeAddDTO.setSettlementTypeCode(SettlementTypeDTO.ACH);
        erFeeAddDTO.setSourceCompanyId(company.getSourceCompanyId());
        erFeeAddDTO.setSourcePayrollRunId(pPayrollRun.getSourcePayRunId());
        erFeeAddDTO.setSourceSystemCd(company.getSourceSystemCd());

        if (overrideFeeAmount != null) {
            erFeeAddDTO.setAmount(overrideFeeAmount);
        }

        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(erFeeAddDTO);
        if (!processResult.isSuccess()) {
            logger.error("Unhandled ProcessResult failure from FinancialTransactionManager.addFeeTransaction(): " + processResult.toString());
        }

        // charge the fee
        BillingDetail detail = processResult.getResult().getFirst().getBillingDetail();

        // create a list of transactions for which to create a TxnResponse
        DomainEntitySet<FinancialTransaction> responseFTs = new DomainEntitySet<FinancialTransaction>();
        detail = Application.findById(BillingDetail.class, detail.getId()); // get fresh instance of this BillingDetail
        if (detail.getFeeTransaction() != null) {
            responseFTs.add(detail.getFeeTransaction());
        }
        if (detail.getTaxTransaction() != null) {
            // since tax was charged, include that FT in the response
            responseFTs.add(detail.getTaxTransaction());
        }

        TransactionResponse.createTransactionResponse(company, responseFTs, null);
    }

    /**
     * Checks to see if the FTs returned are an EmployerDDDebit consolidated with any other transactions (e.g. debit + fee + sales tax or debit + fee)
     *
     * @param finTxns
     * @return
     */
    private boolean isConsolidatedDebit(DomainEntitySet<FinancialTransaction> finTxns) {
        //If there's only one transaction, it's not consolidated
        if (finTxns.size() <= 1) {
            return false;
        }

        //If any of the transactions are a debit, it is a consolidated debit
        for (FinancialTransaction currTxn : finTxns) {
            if (TransactionType.isImpoundTransactionType(currTxn.getTransactionType().getTransactionTypeCd())) {
                return true;
            }
        }

        return false;
    }

    private boolean isCompanyBalanceDueCoveredForPayroll(PayrollRun pPayrollRun) {
        SpcfMoney ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayrollFinTxnCollection(LedgerAccountCode.ERReturnReceivable, pPayrollRun.getSourcePayRunId(), pPayrollRun.getCompany());

        return ledgerBalance.compareTo(SpcfDecimal.createInstance("0.00")) >= 0;
    }

    private boolean isErRedebitTransaction(FinancialTransaction pFinancialTransaction) {

        if (TransactionType.isRedebitTransactionType(pFinancialTransaction.getTransactionType().getTransactionTypeCd()) && pFinancialTransaction.getOriginalTransaction() != null) {
            if (TransactionType.isImpoundTransactionType(pFinancialTransaction.getOriginalTransaction().getTransactionType().getTransactionTypeCd())) {
                return true;
            }
        }

        return false;
    }

    private static void sendEmail(DomainEntitySet<FinancialTransaction> pFinancialTransactions) {
        if (!pFinancialTransactions.isEmpty()) {
            FinancialTransaction financialTransaction = pFinancialTransactions.get(0);
            Company company = financialTransaction.getCompany();
            StringBuilder messageBody = new StringBuilder();
            String crlf = "\r\n";

            String messageHeader = "ACH Returned Tax Payment Notification";
            // Build the message body
            messageBody.append(messageHeader).append(crlf);

            for (int i = messageHeader.length(); i > 0; --i) {
                messageBody.append("-");
            }
            messageBody.append(crlf);

            messageBody.append(crlf); // add empty line for separation
            messageBody.append("FEIN             : ").append(company.getFedTaxId()).append(crlf);
            messageBody.append("PSID             : ").append(company.getSourceCompanyId()).append(crlf);
            messageBody.append("Company Name     : ").append(company.getLegalName()).append(crlf);
            messageBody.append("Payment Template : ").append(financialTransaction.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd()).append(crlf);
            messageBody.append("Payment Amount   : ").append(financialTransaction.getMoneyMovementTransaction().getMoneyMovementTransactionAmount()).append(crlf);
            messageBody.append("Settlement Date  : ").append(financialTransaction.getMoneyMovementTransaction().getDueDate().format("yyyyMMdd")).append(crlf);

            // Send the email
            BatchUtils.sendStateTaxPaymentReturnNotification(messageBody);
        }
    }
}
