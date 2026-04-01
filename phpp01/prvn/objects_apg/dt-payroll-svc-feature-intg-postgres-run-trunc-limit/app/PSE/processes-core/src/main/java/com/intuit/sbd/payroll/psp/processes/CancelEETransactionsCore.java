package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 2, 2009
 * Time: 10:35:48 AM
 */

/**
 *  Cancels or Recalls the DD and/or Tax Paychecks. Common validations and processing that is applicable to
 *  both DD and Tax are performed in this core process.
 *  Cancel will be allowed by Agent if the Payroll status is either Pending or OffloadedDebit.
 *  Recall will be called if the client wants to recall the whole or partial Payroll before the Payroll is offloaded,
 *  i.e only when the payroll is in the Pending status.
 */
public class CancelEETransactionsCore extends Process implements IProcess {
    private static final SpcfLogger logger = Application.getLogger(CancelEETransactionsCore.class);

    private SourceSystemCode mSourceSystemCd = null;
    private String mCompanyId = null;
    private TransactionCancelEEDTO mDto = null;
    private Company mCompany = null;
    private PayrollRun mPayrollRun = null;
    private TransactionResponse transactionResponse;
    private CancelEETransactionsDD cancelEETransactionDD;
    private CancelEETransactionsTax cancelEETransactionTax;
    private CancelOrDeletePayrollWorkersComp cancelOrDeletePayrollWorkersComp;
    private DomainEntitySet<Paycheck> paychecks = new DomainEntitySet<Paycheck>();

    public CancelEETransactionsCore(SourceSystemCode pSourceSystemCd, String pCompanyId, TransactionCancelEEDTO pDto) {
        mSourceSystemCd = pSourceSystemCd;
        mCompanyId = pCompanyId;
        mDto = pDto;
    }

    /**
     * Returns any transaction responses obtained after a recall
     *
     * @return response after a recall attempt
     */
    public TransactionResponse getTransactionResponse() {
        return transactionResponse;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // validate the source system code
        if (mSourceSystemCd == null) {
            validationResult.getMessages().SourceSystemCdNotSpecified(EntityName.SourceSystem,
                    mCompanyId);
        }

        // validate the company id
        if ((mCompanyId == null) || !Validator.isValidLength(mCompanyId, 1, 50)) {
            validationResult.getMessages().CompanyIdNotSpecified(EntityName.Company, mCompanyId);
        }

        // verify the dto is not null
        if (mDto == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.DTO,
                    "CancelEETransactionsCore",
                    "TransactionCancelEEDTO");
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // validate the dto
        validationResult.merge(mDto.validate());

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // retrieve the company
        mCompany = Company.findCompany(mCompanyId, mSourceSystemCd);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(
                    EntityName.Company,
                    mCompanyId,
                    mSourceSystemCd.toString(),
                    mCompanyId);
            return validationResult;
        }

        // retrieve the payroll run
        mPayrollRun = PayrollRun.findPayrollRun(mCompany, mDto.getSourcePayrollRunId());
        PayrollRun.getPayrollsInMemory(mCompany).add(mPayrollRun);
        if (mPayrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(
                    EntityName.PayrollRun,
                    mDto.getSourcePayrollRunId(),
                    mDto.getSourcePayrollRunId(),
                    mSourceSystemCd.toString(),
                    mCompanyId);
            return validationResult;
        }

        // Specific validations for Cancel
        // If the payroll status is not PENDING or OFFLOADED_DEBIT then we cannot proceed.
        if (mDto.isAgentCancel()) {
            // agent can only cancel DD
            ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class,
                    ActionEventCode.DDTransactionCancel);
            PayrollStatus payrollStatus = mPayrollRun.getPayrollRunStatus();
            if (!mPayrollRun.validateAction(actionEvent)) {
                validationResult.getMessages().ActionNotValidForPayrollRun(
                        EntityName.PayrollRun,
                        mPayrollRun.getSourcePayRunId(),
                        actionEvent.getCode().toString(),
                        mPayrollRun.getSourcePayRunId(),
                        payrollStatus.toString());
                return validationResult;
            }

        } else { // specific validations for Recall
            if (!mCompany.isAllowedCapability(SystemCapabilityCode.RecallPayroll) || !mCompany.passesAdditionalCancelTermValidation(false, true, true)) {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        mCompany.getSourceSystemCd().toString(),
                        mCompany.getSourceCompanyId(), SystemCapabilityCode.RecallPayroll.toString());
                return validationResult;

            }

            for (FinancialTransaction finTxn : mPayrollRun.getFinancialTransactionCollection()) {
                if (finTxn.calculateCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Executed &&
                        finTxn.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.AgencyTaxOverpayment) {
                    validationResult.getMessages().PayrollRunAlreadyOffloaded(EntityName.PayrollRun,
                            mPayrollRun.getSourcePayRunId(), mPayrollRun.getSourcePayRunId(),
                            mCompany.getSourceSystemCd().toString(), mCompany.getSourceCompanyId());
                    return validationResult;
                }
            }

            // if a prefunding transaction has been added we do not allow a customer to edit the payroll
            DomainEntitySet<FinancialTransaction> nonAchDebits = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>()
                    .Where(FinancialTransaction.PayrollRun().equalTo(mPayrollRun)
                    .And(FinancialTransaction.SettlementTypeCd().notEqualTo(SettlementType.ACH))
                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)
                    .Or(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)))));
            if(nonAchDebits != null && nonAchDebits.size() > 0 && mPayrollRun.getPayrollRunStatus() == PayrollStatus.Pending){
                validationResult.getMessages().RecallTransactionsWithPrefundingRecorded(
                        EntityName.PayrollRun,
                        mPayrollRun.getSourcePayRunId(),
                        mPayrollRun.getSourcePayRunId(),
                        mCompany.getSourceSystemCd().toString(),
                        mCompany.getSourceCompanyId());
                return validationResult;
            }
        }

        boolean hasDDService = mCompany.isCompanyOnService(ServiceCode.DirectDeposit);
        boolean hasTaxService = mCompany.isCompanyOnService(ServiceCode.Tax);
        boolean hasWorkersCompService = mCompany.isCompanyOnService(ServiceCode.WorkersComp);

        if(mDto.getSourcePaycheckIdList() != null && mDto.getSourcePaycheckIdList().size() > 0) {
            // eager load paychecks
            mPayrollRun.eagerLoadPaychecks(mDto.getSourcePaycheckIdList(), hasDDService, hasTaxService);

            Set<String> negativePaychecksIds = new HashSet<String>();
            for(String sourcePaycheckID : mDto.getSourcePaycheckIdList()) {
                Paycheck paycheck = Paycheck.findPaycheck(mCompany, sourcePaycheckID);
                if(paycheck == null){
                    validationResult.getMessages().PaycheckDoesNotExist(EntityName.PayCheck, sourcePaycheckID,
                            mSourceSystemCd.toString(), mCompanyId, sourcePaycheckID);
                } else{
                    paychecks.add(paycheck);
                }

                //noinspection EmptyCatchBlock
                try { negativePaychecksIds.add(Integer.toString(Integer.parseInt(sourcePaycheckID) * -1)); } catch (NumberFormatException e) {}
            }

            for (Paycheck supersededPaycheck : Paycheck.findPaychecks(mCompany, negativePaychecksIds)) {
                logger.error(String.format("Voiding a paycheck (%s) that has a related, superseded paycheck on company %s:%s.  Manual intervention required.",
                        supersededPaycheck.getSourcePaycheckId(),
                        mCompany.getSourceSystemCd().toString(),
                        mCompany.getSourceCompanyId()));
            }
        } else {
            paychecks = mPayrollRun.getPaycheckCollection();
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mPayrollRun.getPayrollRunStatus() == PayrollStatus.Canceled) {
            validationResult.getMessages().PayrollRunAlreadyCanceled(EntityName.PayrollRun,
                    mPayrollRun.getSourcePayRunId(), mPayrollRun.getSourcePayRunId(), mSourceSystemCd.toString(),
                    mCompanyId);
            return validationResult;
        }

        if(hasDDService && (!mPayrollRun.isHistoricalPayroll() || mCompany.isMigratingToAssisted())) {
            cancelEETransactionDD = new CancelEETransactionsDD(mSourceSystemCd, mCompanyId, mDto, paychecks);
            validationResult.merge(cancelEETransactionDD.validate());
        }

        if(hasTaxService && !mDto.isAgentCancel()) {
            //agents cannot directly cancel tax
            cancelEETransactionTax = new CancelEETransactionsTax(mSourceSystemCd, mCompanyId, mDto);
            validationResult.merge(cancelEETransactionTax.validate());
        }

        if(hasWorkersCompService) {
            cancelOrDeletePayrollWorkersComp = new CancelOrDeletePayrollWorkersComp(paychecks);
            validationResult.merge(cancelOrDeletePayrollWorkersComp.validate());
        }

        for (FinancialTransaction finTxn : mPayrollRun.getFinancialTransactionCollection()) {
            if (finTxn.calculateCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Created) {
                continue;
            }

            MoneyMovementTransaction mmTxn = finTxn.getMoneyMovementTransaction();
            if (null == mmTxn) {
                continue;
            }

            SpcfCalendar limitCalendar =
                    mCompany.getOffloadGroup().getCalendarForCutoffTime(
                            mmTxn.getInitiationDate().toLocal());

            // Check to see if it's after the the cutoff date/time
            if (!PSPDate.getPSPTime().before(limitCalendar)) {
                validationResult.getMessages().PayrollRunAlreadyOffloaded(
                        EntityName.PayrollRun,
                        mPayrollRun.getSourcePayRunId(),
                        mPayrollRun.getSourcePayRunId(),
                        mSourceSystemCd.toString(),
                        mCompanyId);
                return validationResult;
            }
        }


        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        List<FinancialTransaction> txnResponseList = new ArrayList<FinancialTransaction>();

        // Cancel or Recall the DD Transactions and calculate the total (DD + Tax) amount cancelled        
        if(cancelEETransactionDD != null){
            processResult.merge(cancelEETransactionDD.process());
            txnResponseList.addAll(cancelEETransactionDD.getFinancialTransactions());
        }

        SpcfDecimal cancelledAmount = SpcfDecimal.createInstance("0.00");
        // Record a Paycheck Recalled Event for each paycheck, if this financial transaction is a paycheck split
        for (Paycheck paycheck:paychecks) {
            if (paycheck.getStatus() == PaycheckStatusCode.Active) {
                if (!mDto.isAgentCancel()) {
                    CompanyEvent.createPaycheckEvent(mCompany, paycheck.getSourcePaycheckId(), paycheck.getId(), EventTypeCode.PaycheckRecalled);
                }
                paycheck.setStatus(PaycheckStatusCode.Inactive);
                Application.save(paycheck);
            }
            cancelledAmount = cancelledAmount.add(paycheck.getNetAmount());
        }

        // Cancel or Recall the Tax Transactions
        if(cancelEETransactionTax != null){
            processResult.merge(cancelEETransactionTax.process());
        }

        if(cancelOrDeletePayrollWorkersComp != null) {
            processResult.merge(cancelOrDeletePayrollWorkersComp.process());
        }
        
        //get all active paychecks
        DomainEntitySet<Paycheck> activePaychecks = mPayrollRun.getPaycheckCollection().find(Paycheck.Status().equalTo(PaycheckStatusCode.Active));

        // Update the PayrollRun to reflect the changes.
        if (activePaychecks.size() == 0) {
            if(mPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created, TransactionStateCode.Completed, TransactionStateCode.Executed)).size() > 0){
                mPayrollRun.setPayrollRunStatus(PayrollStatus.Complete);
                mPayrollRun.setStatusEffectiveDate(PSPDate.getPSPTime());
            } else {
                // If ALL EE DD CR transactions for the payroll run were successfully cancelled,
                // update the status of the payroll run to "Canceled".
                mPayrollRun.setPayrollRunStatus(PayrollStatus.Canceled);
                mPayrollRun.setStatusEffectiveDate(PSPDate.getPSPTime());
            }
            // Record Payroll Recalled Event if we recall entire payroll in a single call
            if (!mDto.isAgentCancel() && ((mDto.getSourcePaycheckIdList() == null) || (mDto.getSourcePaycheckIdList().size() == 0))) {
                CompanyEvent.createPayrollRunEvent(mCompany, mPayrollRun.getSourcePayRunId(), mPayrollRun.getId(), EventTypeCode.PayrollRecalled);
            }
        }

        //todo: review
        // Associate with transmission
        if (mDto.getTransmissionId() != null) {
            TransmissionPayrollRun transmissionPayrollRun = new TransmissionPayrollRun();
            transmissionPayrollRun.setPayrollRun(mPayrollRun);
            SourceSystemTransmission transmissionSecondary = SourceSystemTransmission.findSourceSystemTransmissionByIdentifier(mDto.getTransmissionId());
            transmissionPayrollRun.setSourceSystemTransmissionId(transmissionSecondary.getId().toString());
            if (mDto.isAgentCancel()) {
                transmissionPayrollRun.setPayrollProcess(PayrollProcessCode.CancelTransaction);
            } else {
                transmissionPayrollRun.setPayrollProcess(PayrollProcessCode.RecallTransaction);
            }
            transmissionPayrollRun = Application.save(transmissionPayrollRun);
            transmissionSecondary.addTransmissionPayrollRun(transmissionPayrollRun);
            mPayrollRun.addTransmissionPayrollRun(transmissionPayrollRun);
        }

        mPayrollRun = Application.save(mPayrollRun);

        // Create a new Transaction Response
        // If Payroll Run Status = Pending
        //     Associate all cancelled financial txs and the new financial ER DD Debit tx (if any)
        //     to the Transaction Response.
        // If Payroll Run Status = Offloaded Debit Only
        //     Associate all canceled EE DD CRs and the new ER Fin Tx for the refund.
        // RequestID = null

        if (!txnResponseList.isEmpty()) {
            if (mDto.isAgentCancel()) {
                TransactionResponse.createTransactionResponse(mCompany, txnResponseList, null);
            } else {
                transactionResponse = TransactionResponse.createTransactionResponse(mPayrollRun.getCompany(),
                    txnResponseList, mDto.getRequestId());
            }
        }

        if(mCompany.getSourceSystemCd() == SourceSystemCode.QBDT) {
            if (mDto.getSourcePaycheckIdList() != null && mDto.getSourcePaycheckIdList().size() > 0) {
                for (String paycheckId : mDto.getSourcePaycheckIdList()) {
                    Paycheck paycheck = Paycheck.findPaycheck(mCompany, paycheckId);
                    addQBDTMemo(paycheck);
                }
            } else {
                for (Paycheck paycheck : mPayrollRun.getPaycheckCollection()) {
                    addQBDTMemo(paycheck);
                }
            }
        }

        if(mPayrollRun.updateEETotalsCalculationRequired()) {
            EmpTotalsPayrollRun.insertEmpTotalsPayrollRun(mPayrollRun);
        }

        return processResult;
    }

    private void addQBDTMemo(Paycheck paycheck) {
        if (paycheck.getQbdtPaycheckInfo() != null &&
                ((paycheck.getTaxCollection() != null && paycheck.getTaxCollection().size() > 0) || (paycheck.getPaycheckSplitCollection() != null && !paycheck.getPaycheckSplitCollection().isEmpty()))) {
            String memo = paycheck.getQbdtPaycheckInfo().getMemo();
            if(memo == null) {
                memo = Paycheck.VOID_FUNDS_RECOVERED;
            } else {
                memo += " " + Paycheck.VOID_FUNDS_RECOVERED;
            }
            paycheck.getQbdtPaycheckInfo().setMemo(memo);
            Application.save(paycheck);
        }
    }


}
