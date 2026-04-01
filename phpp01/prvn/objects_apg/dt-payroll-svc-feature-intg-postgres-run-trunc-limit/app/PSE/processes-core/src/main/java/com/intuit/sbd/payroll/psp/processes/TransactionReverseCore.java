package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionReverseDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * User: kpaul
 * Date: Jan 6, 2008
 * Time: 1:45:32 AM
 */
public class TransactionReverseCore extends Process implements IProcess {
    private TransactionReverseDTO dto = null;
    private SourceSystemCode sourceSystemCd = null;
    private String companyId = null;
    private Company company = null;
    private PayrollRun payrollRun = null;
    private PayrollStatus payrollStatus = null;
    private CompanyBankAccount companyBankAccount = null;
    private List<FinancialTransaction> txnList = new Vector<FinancialTransaction>(10, 10);
    private ArrayList<CancelTransactionCore> cancelTransactionCoreProcesses;
    private CompanyService companyService = null;

    public TransactionReverseCore(SourceSystemCode pSourceSystemCd, String pCompanyId, TransactionReverseDTO pDto) {
        sourceSystemCd = pSourceSystemCd;
        companyId = pCompanyId;
        dto = pDto;
        cancelTransactionCoreProcesses = new ArrayList<CancelTransactionCore>();
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // validate the source system code
        if (sourceSystemCd == null) {
            validationResult.getMessages().SourceSystemCdNotSpecified(EntityName.SourceSystem, sourceSystemCd.toString());
        }

        // validate the company id
        if ((companyId == null) || !Validator.isValidLength(companyId, 1, 50)) {
            validationResult.getMessages().CompanyIdNotSpecified(EntityName.Company, companyId);
        }

        // verify the dto is not null
        if (dto == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.DTO,
                    "TransactionReverseCore",
                    "TransactionReverseDTO");
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // validate the dto
        validationResult.merge(dto.validate());

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // retrieve the company
        company = Company.findCompany(companyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(
                    EntityName.Company,
                    companyId,
                    sourceSystemCd.toString(),
                    companyId);
            return validationResult;
        }

        // retrieve the company service (for DD)
        companyService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);

        if (companyService == null) {
            validationResult.getMessages().CompanyNotAssociatedWithService(
                    EntityName.CompanyService,
                    ServiceCode.DirectDeposit.toString(),
                    sourceSystemCd.toString(),
                    companyId,
                    ServiceCode.DirectDeposit.toString());
            return validationResult;
        }

        // retrieve the payroll run
        payrollRun = PayrollRun.findPayrollRun(company, dto.getSourcePayrollRunId());
        if (payrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(
                    EntityName.PayrollRun,
                    dto.getSourcePayrollRunId(),
                    dto.getSourcePayrollRunId(),
                    sourceSystemCd.toString(),
                    companyId);
            return validationResult;
        }

        // Determine the type of payroll run and if it is a Bill Payment, check if the company is associated with the BillPayment service
        if (payrollRun.getPayrollRunType().equals(PayrollType.BillPayment)) {
            companyService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);

            if (companyService == null) {
                validationResult.getMessages().CompanyNotAssociatedWithService(
                        EntityName.CompanyService,
                        ServiceCode.BillPayment.toString(),
                        sourceSystemCd.toString(),
                        companyId,
                        ServiceCode.BillPayment.toString());
                return validationResult;
            }
        }
        // Verify the payroll is in a valid state for the txn reversal
        ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.DDTransactionReverse);
        payrollStatus = payrollRun.getPayrollRunStatus();
        if (!payrollRun.validateAction(actionEvent)) {
            validationResult.getMessages().ActionNotValidForPayrollRun(
                    EntityName.PayrollRun,
                    payrollRun.getSourcePayRunId(),
                    actionEvent.getCode().toString(),
                    payrollRun.getSourcePayRunId(),
                    payrollStatus.toString());
            return validationResult;
        }

        // validate the txn date
        validationResult.merge(validateTxDate());

        // If there are any errors here, we cannot proceed, so return immediately.
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // validate the company bank account
        if (dto.isChargeFee()) {
            // Retrieve the active company bank account.
            // (a company can have only one active bank account)
            companyBankAccount = CompanyBankAccount.
                    findActiveCompanyBankAccount(company);

            if (companyBankAccount == null) {
                validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(
                        EntityName.Company,
                        companyId,
                        sourceSystemCd.toString(),
                        companyId);
                return validationResult;
            }
        }

        if (dto.isIntuitInitiatedReversals()) {
            if (dto.getTxSettlementTypeCd() != SettlementTypeDTO.ACH) {
                validationResult.getMessages().SettlementTypeInvalidForIntuitReversal(EntityName.SettlementType,
                        dto.getTxSettlementTypeCd().toString());
                return validationResult;
            }

            if (dto.isChargeFee()) {
                validationResult.getMessages().ReversalFeeCanNotChargedForIntuitReversals(EntityName.Fee,
                        "ChargeFee");
                return validationResult;
            }

            if (!payrollRun.canInitiateIntuitReversals()) {
                validationResult.getMessages().ActionNotValidForPayrollRun(
                        EntityName.PayrollRun,
                        payrollRun.getSourcePayRunId(),
                        actionEvent.getCode().toString(),
                        payrollRun.getSourcePayRunId(),
                        payrollStatus.toString());
                return validationResult;
            }

            // validate full payroll is being reversed
            if ((dto.getDdTransactionIdList() != null) && !dto.getDdTransactionIdList().isEmpty()) {
                List<String> requestedForReversal = dto.getDdTransactionIdList();
                List<String> allTransactionIds = new ArrayList<String>();

                DomainEntitySet<FinancialTransaction> splitTxnList =
                        getSplitTransactionList(payrollRun);

                if (splitTxnList != null) {
                    for (FinancialTransaction txn : splitTxnList) {
                        if (!txn.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Cancelled) &&
                                !txn.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Returned)) {
                            allTransactionIds.add(getTransactionId(txn));
                        }
                    }
                }

                allTransactionIds.removeAll(requestedForReversal);
                if (allTransactionIds != null && allTransactionIds.size() > 0) {
                    validationResult.getMessages().CanNotReversePartialPayrollsForIntuitReversals(EntityName.PayrollRun,
                            payrollRun.getSourcePayRunId());
                    return validationResult;
                }
            }
        }

        // Validate the transaction(s) to be reversed
        validationResult.merge(validateTransactionsToBeReversed());

        if (!validationResult.isSuccess()) {
            return validationResult;
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
                        new CancelTransactionCore(sourceSystemCd, companyId, currFinTxn.getId().toString(), true);
                validationResult.merge(currCancelTxCore.validate());
                cancelTransactionCoreProcesses.add(currCancelTxCore);
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if (!txnList.isEmpty()) {
            // Calculate earliest possible settlement (offload) date.
            // If today is a weekend or a holiday or today is a business day and it's past cutoff time today:
            //      settlement date = today + 2 business days
            // else
            //      settlement date = today + 1 business day.
            SpcfCalendar settlementDate = PSPDate.getPSPTime();
            CalendarUtils.clearTime(settlementDate);
            boolean beforeCutoff = company.getOffloadGroup().isBeforeActualCutoffTime();
            boolean businessDay = !CalendarUtils.isWeekendOrHoliday(settlementDate);

            CalendarUtils.addBusinessDays(settlementDate, (beforeCutoff && businessDay) ? 1 : 2);
            CalendarUtils.clearTime(settlementDate);

            IntuitBankAccount intuitReversalBankAccount =
                    IntuitBankAccount.findIntuitBankAccount(
                            TransactionTypeCode.EmployeeDdReversalDebit,
                            CreditDebitCode.Credit);

            FinancialTransaction reversalTxn;
            for (FinancialTransaction txn : txnList) {
                if (SettlementTypeDTO.ACH.equals(dto.getTxSettlementTypeCd())) {
                    // Process the reversal for each DDTransaction included in the request:
                    // * Create Fin Tx for the EE Reversal
                    //   - Tx Type is EEDDRVDB
                    //   - Tx State is "CR"
                    //   - Amount = same as original EE DD CR (Paycheck being reversed)
                    //   - Settlement date is earliest possible for offload
                    //   - Settlement type is ACH
                    //   - Use same EXACT bank account information as original EE DD CR for the EE Bank Acct
                    //   - Make associated ledger entries
                    //   - Associate with the original payroll
                    //   - Associate with the original financial transaction (identifies the original financial
                    //     transaction as having a reversal attempt against it)
                    reversalTxn = FinancialTransaction.createFinancialTransaction(
                            company,
                            payrollRun,
                            null,                                           // paycheck split
                            intuitReversalBankAccount.getBankAccount(),     // credit bank account
                            txn.getCreditBankAccount(),                     // debit bank account
                            BankAccountOwnerType.Intuit,                    // credit bank account type
                            txn.getCreditBankAccountType(),                 // debit bank account type
                            TransactionTypeCode.EmployeeDdReversalDebit,   // transaction type
                            txn.getFinancialTransactionAmount(),            // transaction amount
                            SettlementType.ACH,                             // settlement type
                            settlementDate,                                 // settlement date
                            txn.getSku(),                                   // same SKU as orig txn
                            txn, 0);                                           // original txn
                } else { // Non-ACH Reveral
                    SpcfCalendar nonACHsettlementDate = CalendarUtils.convertToSpcfCalendar(dto.getTxDate());
                    CalendarUtils.clearTime(nonACHsettlementDate);

                    reversalTxn = FinancialTransaction.createFinancialTransaction(
                            company,
                            payrollRun,
                            null,   // paycheck split
                            null,   // credit bank account
                            null,   // debit bank account
                            null,   // credit bank account type
                            null,   // debit bank account type
                            TransactionTypeCode.EmployeeDdReversalDebit,
                            txn.getFinancialTransactionAmount(),
                            SettlementType.valueOf(dto.getTxSettlementTypeCd().toString()),
                            nonACHsettlementDate,
                            txn.getSku(),
                            txn, 0);

                    // Update the new reversal txn to EXECUTED and then COMPLETED (so the ledger is kept in sync)
                    reversalTxn.updateFinancialTransactionState(TransactionStateCode.Executed);
                    reversalTxn.updateFinancialTransactionState(TransactionStateCode.Completed);
                }

                // Associate new reversal txn with the original txn
                // (identifies the original financial transaction as having a reversal against it)
                reversalTxn.setOriginalTransaction(txn);
                txn.addAssociatedTransactions(reversalTxn);

                CompanyEvent.createReversalRequestedEvent(company, reversalTxn, dto.isIntuitInitiatedReversals(), PSPDate.getPSPTime());

                Application.save(reversalTxn);
            }

            if (dto.isChargeFee()) {
                DomainEntitySet<BillingDetail> billingDetails = BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.ReversalFee, 1, null);

                // Create a new Transaction Response for the Reversal Fee Txs
                //   - RequestID is null
                //   - Associate all ER Reversal Fee txs created.
                Collection<FinancialTransaction> txnResponseList = new DomainEntitySet<FinancialTransaction>();
                for (BillingDetail billingDetail : billingDetails) {
                    txnResponseList.add(billingDetail.getFeeTransaction());
                }
                TransactionResponse.createTransactionResponse(company, txnResponseList, null);
            }

            // if this is a reversal related to a collection...
            if (dto.isIntuitInitiatedReversals()) {
                payrollRun.updatePayrollRunStatus(PayrollStatus.PendingReversals);

                //Call the process method for all the CancelTransactionCore processes we created in validate
                for (CancelTransactionCore currCancelProc : cancelTransactionCoreProcesses) {
                    processResult.merge(currCancelProc.process());
                }

            }
            // Associate with transmission
            if (dto.getTransmissionId() != null) {
                TransmissionPayrollRun transmissionPayrollRun = new TransmissionPayrollRun();
                transmissionPayrollRun.setPayrollRun(payrollRun);
                SourceSystemTransmission transmissionSecondary = SourceSystemTransmission.findSourceSystemTransmissionByIdentifier(dto.getTransmissionId());
                transmissionPayrollRun.setSourceSystemTransmissionId(transmissionSecondary.getId().toString());
                transmissionPayrollRun.setPayrollProcess(PayrollProcessCode.ReverseTransaction);
                transmissionPayrollRun = Application.save(transmissionPayrollRun);

                transmissionSecondary.addTransmissionPayrollRun(transmissionPayrollRun);
                payrollRun.addTransmissionPayrollRun(transmissionPayrollRun);

            }

            payrollRun = Application.save(payrollRun);
        }

        return processResult;
    }

    private ProcessResult validateTransactionsToBeReversed() {
        ProcessResult validationResult = new ProcessResult();

        // If a transaction id list was passed in, verify that the transactions exist and
        // that they belong to the associated payroll run
        if ((dto.getDdTransactionIdList() != null) && !dto.getDdTransactionIdList().isEmpty()) {


            for (String transId : dto.getDdTransactionIdList()) {
                // this query takes the payroll run and the dd transaction id
                FinancialTransaction txn = null;
                if (payrollRun.getPayrollRunType().equals(PayrollType.Regular)) {
                    PaycheckSplit split = PaycheckSplit.findPaycheckSplit(payrollRun, transId);
                    if (split != null) {
                        txn = split.getFinancialTransaction();
                    }
                }
                if (payrollRun.getPayrollRunType().equals(PayrollType.BillPayment)) {
                    BillPaymentSplit split = BillPaymentSplit.findBillPaymentSplit(payrollRun, transId);
                    if (split != null) {
                        txn = split.getFinancialTransaction();
                    }
                }

                if (txn == null) {
                    validationResult.getMessages().TransactionDoesNotExist(
                            EntityName.DDTransaction,
                            transId,
                            transId,
                            sourceSystemCd.toString(),
                            companyId);
                } else {

                    // Check transaction to ensure it is reversible.
                    if (isTxnReversible(txn, validationResult)) {
                        txnList.add(txn);
                    }
                }
            }
        } else {
            // If a transaction id list was not passed in, then we need to reverse the entire payroll.
            // Get the financial transactions for all paycheck splits or bill payment splits for the payroll run.
            DomainEntitySet<FinancialTransaction> splitTxnList = getSplitTransactionList(payrollRun);

            if (splitTxnList != null) {
                for (FinancialTransaction txn : splitTxnList) {
                    // Check transaction to ensure it is reversible.
                    if (isTxnReversible(txn, null)) {
                        txnList.add(txn);
                    }
                }
            }
        }

        return validationResult;
    }

    /**
     * Check to see if the given transaction is reversible according to the business rules.
     * A transaction is not reversible if:
     * * It is in a state of CREATED, CANCELLED or RETURNED
     * * It has already been reversed (unless the reversal has been CANCELLED or VOIDED)
     *
     * @param pTxn              The transaction to check.
     * @param pValidationResult The ProcessResult in which to record the appropriate error (optional)
     * @return Returns true if the given transaction is reversible.
     */
    private boolean isTxnReversible(FinancialTransaction pTxn, ProcessResult pValidationResult) {
        boolean isReversible = true;
        TransactionStateCode txnState = pTxn.calculateCurrentTransactionState().getTransactionStateCd();

        if (TransactionStateCode.Created.equals(txnState)) {
            isReversible = false;
            if (pValidationResult != null) {
                pValidationResult.getMessages().TransactionPendingCannotReverse(
                        EntityName.DDTransaction,
                        getTransactionId(pTxn),
                        getTransactionId(pTxn));
            }
        } else if (TransactionStateCode.Cancelled.equals(txnState)) {
            isReversible = false;
            if (pValidationResult != null) {
                pValidationResult.getMessages().TransactionCancelledCannotReverse(
                        EntityName.DDTransaction,
                        getTransactionId(pTxn),
                        getTransactionId(pTxn));
            }
        } else if (TransactionStateCode.Returned.equals(txnState)) {
            isReversible = false;
            if (pValidationResult != null) {
                pValidationResult.getMessages().TransactionReturnedCannotReverse(
                        EntityName.DDTransaction,
                        getTransactionId(pTxn),
                        getTransactionId(pTxn));
            }
        } else {
            for (FinancialTransaction assocTxn : pTxn.getAssociatedTransactionsCollection()) {
                if (TransactionAssociationType.Reversal.equals(
                        assocTxn.getTransactionType().getAssociationType())) {
                    txnState = assocTxn.calculateCurrentTransactionState().getTransactionStateCd();
                    if (!TransactionStateCode.Cancelled.equals(txnState) &&
                            !TransactionStateCode.Voided.equals(txnState)) {
                        isReversible = false;
                        if (pValidationResult != null) {
                            pValidationResult.getMessages().TransactionReversalAlreadyAttemptedCannotReverse(
                                    EntityName.DDTransaction,
                                    getTransactionId(pTxn),
                                    getTransactionId(pTxn));
                        }
                    }
                }
            }
        }

        return isReversible;
    }

    /**
     * Validates that the transaction date is between 45 calendar days in the past and the current date.
     *
     * @return
     */
    private ProcessResult validateTxDate() {
        ProcessResult validationResult = new ProcessResult();

        // Only need to validate the txn date iff settlement type is non-ACH
        if (!SettlementTypeDTO.ACH.equals(dto.getTxSettlementTypeCd())) {
            SpcfCalendar settlementDateCalendar = CalendarUtils.convertToSpcfCalendar(dto.getTxDate());
            CalendarUtils.clearTime(settlementDateCalendar);

            // Settlement date can be max 45 calendar days in the past
            SpcfCalendar pastCalendar = PSPDate.getPSPTime();
            pastCalendar.addDays(-45);
            CalendarUtils.clearTime(pastCalendar);

            // Settlement date cannot be in the future
            SpcfCalendar currentDateCalendar = PSPDate.getPSPTime();
            CalendarUtils.clearTime(currentDateCalendar);

            // Verify settlement date is valid
            if (settlementDateCalendar.before(pastCalendar)) {
                SimpleDateFormat fDate = new SimpleDateFormat("M/d/yyyy");
                String date = fDate.format(CalendarUtils.convertToCalendar(settlementDateCalendar).getTime());
                validationResult.getMessages().SettlementDateTooFarInPast(EntityName.Date, date, date,
                        dto.getTxSettlementTypeCd().toString());
            } else if (settlementDateCalendar.after(currentDateCalendar)) {
                SimpleDateFormat fDate = new SimpleDateFormat("M/d/yyyy");
                String date = fDate.format(CalendarUtils.convertToCalendar(settlementDateCalendar).getTime());
                validationResult.getMessages().SettlementDateTooFarInFuture(EntityName.Date, date, date,
                        dto.getTxSettlementTypeCd().toString());
            }
        }

        return validationResult;
    }

    private String getTransactionId(FinancialTransaction pFinancialTransaction) {

        if (pFinancialTransaction.getPaycheckSplit() != null) {
            return pFinancialTransaction.getPaycheckSplit().getSourceDdTxnId();
        }

        if (pFinancialTransaction.getBillPaymentSplit() != null) {
            return pFinancialTransaction.getBillPaymentSplit().getSourceId();
        }
        return "";
    }

    private DomainEntitySet<FinancialTransaction> getSplitTransactionList(PayrollRun pPayrollRun) {
        DomainEntitySet<FinancialTransaction> splitTxnList = null;
        switch (pPayrollRun.getPayrollRunType()) {
            case Regular:
                splitTxnList =
                        FinancialTransaction.findPaycheckSplitFinancialTransactions(pPayrollRun, null);
                break;
            case BillPayment:
                splitTxnList =
                        FinancialTransaction.findBillPaymentSplitFinancialTransactions(pPayrollRun, null);
                break;
            default:
                FinancialTransaction.findPaycheckSplitFinancialTransactions(pPayrollRun, null);
                break;
        }
        return splitTxnList;
    }
}
