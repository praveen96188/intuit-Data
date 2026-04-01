package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.RedebitImpoundDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.FlushMode;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Aug 21, 2008
 * Time: 10:24:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddPayrollRelatedNonACHRedebit extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private List<RedebitImpoundDTO> mRedebits;
    private List<AddRepaymentTransactions> addNonAchRedebitProcesses;
    private List<CancelTransactionCore> mCancelTransactionCoreProcesses;
    private PayrollRun mPayrollRun;
    private DomainEntitySet<FinancialTransaction> createdNonAchRedebits;
    private boolean mAmountCoversPayrollDebt = false;
    private Company mCompany;

    public DomainEntitySet<FinancialTransaction> getFinancialTransactions() {
        return createdNonAchRedebits;
    }

    public AddPayrollRelatedNonACHRedebit(SourceSystemCode pSourceSystemCd,
                                          String pSourceCompanyId,
                                          Collection<RedebitImpoundDTO> pRedebits) {
        this.mSourceSystemCd = pSourceSystemCd;
        this.mSourceCompanyId = pSourceCompanyId;

        // discard DTOs with $0.00 amounts (keep null DTOs and DTOs with any other amounts.. they'll be handled by validate())
        this.mRedebits = new ArrayList<RedebitImpoundDTO>();
        SpcfMoney ZERO = new SpcfMoney("0.00");
        for (RedebitImpoundDTO dto : pRedebits) {
            if (dto==null || dto.getAmount().compareTo(ZERO)!=0) {
                this.mRedebits.add(dto);
            }
        }

        addNonAchRedebitProcesses = new ArrayList<AddRepaymentTransactions>();
        mCancelTransactionCoreProcesses = new ArrayList<CancelTransactionCore>();
        createdNonAchRedebits = new DomainEntitySet<FinancialTransaction>();
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        for (RedebitImpoundDTO currRedebitImpoundDTO : mRedebits) {
            if (currRedebitImpoundDTO != null) {
                validationResult.merge(currRedebitImpoundDTO.validate());
            } else {
                validationResult.getMessages().InvalidArgument(EntityName.FinancialTransaction, null, "RedebitImpoundDTO");
            }
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company Exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        for (RedebitImpoundDTO currRedebitImpoundDTO : mRedebits) {

            //Ensure that the original transaction exists
            FinancialTransaction currRedebitTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class,
                    SpcfUniqueId.createInstance(currRedebitImpoundDTO.getOriginalFinancialTxId()));

            if (currRedebitTxn == null) {
                validationResult.getMessages().FinancialTransactionDoesNotExist(EntityName.FinancialTransaction,
                        currRedebitImpoundDTO.getOriginalFinancialTxId(),
                        currRedebitImpoundDTO.getOriginalFinancialTxId(), mSourceSystemCd.toString(), mSourceCompanyId);
                return validationResult;
            }

            //Ensure all redebits are associated with the same payroll
            PayrollRun currPayrollRun = currRedebitTxn.getPayrollRun();

            if (mPayrollRun == null) {
                mPayrollRun = currPayrollRun;
            } else if (mPayrollRun != currPayrollRun) {
                validationResult.getMessages().AllRedebitsMustBelongToTheSamePayrollRun(EntityName.PayrollRun,
                        mSourceSystemCd.toString(), mSourceCompanyId);
                return validationResult;
            }

            AddRepaymentTransactions currAddRepaymentTransactions = new AddRepaymentTransactions(mSourceSystemCd, mSourceCompanyId, currRedebitImpoundDTO);
            validationResult.merge(currAddRepaymentTransactions.validate());
            addNonAchRedebitProcesses.add(currAddRepaymentTransactions);
        }


        SpcfMoney totalRedebitAmount = getTotalRedebitAmount(mRedebits);
        if (totalRedebitAmount.compareTo(new SpcfMoney("0.00")) <= 0) {
            validationResult.getMessages().AmountNotPositive(EntityName.FinancialTransaction, totalRedebitAmount.toString());
            return validationResult;
        }

        // if the redebit amount covers the entire payroll debt...
        SpcfMoney ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                mPayrollRun.getSourcePayRunId(),
                mPayrollRun.getCompany());

        if (totalRedebitAmount.add(ledgerBalance).compareTo(SpcfDecimal.createInstance(0.00)) >= 0) {
            mAmountCoversPayrollDebt = true;

            PayrollStatus prStatus = mPayrollRun.getPayrollRunStatus();

            if (prStatus == PayrollStatus.PendingAutoRedebit || prStatus == PayrollStatus.PendingRedebit) {
                // if the payroll is in the PendingAutoRedebit or PendingRedebit state, we find all redebits in the
                // Created state for this payroll and create a process to cancel each
                TransactionState createdState = Application.findById(TransactionState.class, TransactionStateCode.Created);
                DomainEntitySet<FinancialTransaction> pendingRedebits = mPayrollRun.getFinancialTransactions(createdState, TransactionAssociationType.Redebit);
                for (FinancialTransaction redebitFT : pendingRedebits) {
                    CancelTransactionCore cancelProcess = new CancelTransactionCore(mSourceSystemCd, mSourceCompanyId,
                            redebitFT.getId().toString(), true);
                    validationResult.merge(cancelProcess.validate());
                    mCancelTransactionCoreProcesses.add(cancelProcess);
                }
            } else if (prStatus == PayrollStatus.PendingReversals) {
                // if the payroll is in the PendingReversals state, we find all EmployeeDDReversals in the Created state
                // for this payroll and create a process to cancel each
                TransactionState createdState = Application.findById(TransactionState.class, TransactionStateCode.Created);
                TransactionType reversalType = Application.findById(TransactionType.class, TransactionTypeCode.EmployeeDdReversalDebit);
                DomainEntitySet<FinancialTransaction> pendingReversals = mPayrollRun.getFinancialTransactions(createdState, reversalType);
                for (FinancialTransaction reversalFT : pendingReversals) {
                    CancelTransactionCore cancelProcess = new CancelTransactionCore(mSourceSystemCd, mSourceCompanyId,
                            reversalFT.getId().toString(), true);
                    validationResult.merge(cancelProcess.validate());
                    mCancelTransactionCoreProcesses.add(cancelProcess);
                }
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = new ProcessResult<DomainEntitySet<FinancialTransaction>>();

        //Call the process method for all the AddRedebitImpoundTransactionCore processes we created in validate
        for (AddRepaymentTransactions currAddProc : addNonAchRedebitProcesses) {
            ProcessResult<FinancialTransaction> addProcResult = currAddProc.process();
            processResult.merge(addProcResult);
            createdNonAchRedebits.add(currAddProc.getFinancialTransaction());
        }

        // if this redebit covers the entire payroll debt, update the PayrollRun status and cancel pending transactions
        if (mAmountCoversPayrollDebt) {
            mPayrollRun.updatePayrollRunStatus(PayrollStatus.Complete);
            for (CancelTransactionCore cancelProcess : mCancelTransactionCoreProcesses) {
                processResult.merge(cancelProcess.process());
            }

            // setting the flush mode to AUTO to make the changes available to the Ledger Balance queries
            FlushMode previousFlushMode = Application.getHibernateSession().getHibernateFlushMode();
            Application.getHibernateSession().setFlushMode(FlushMode.AUTO);

            // also remove company onhold for ACHRejectR1R9/ACHRejectOther
            SpcfDecimal returnLedgerBalance = LedgerAccount.getLedgerAccountBalance(mCompany, LedgerAccountCode.ERReturnReceivable);
            SpcfDecimal badDebtLedgerBalance = LedgerAccount.getLedgerAccountBalance(mCompany, LedgerAccountCode.BadDebt);
            SpcfMoney totalRedebitAmount = getTotalRedebitAmount(mRedebits);

            if ((totalRedebitAmount.compareTo(returnLedgerBalance) >= 0) &&
                    (totalRedebitAmount.compareTo(badDebtLedgerBalance) >= 0)){
                for (ServiceSubStatusCode onHoldReasonCd : mCompany.getCurrentOnHoldReasonCodes()) {
                    if (onHoldReasonCd == ServiceSubStatusCode.AchRejectOther ||
                        onHoldReasonCd == ServiceSubStatusCode.AchRejectR1R9) {
                        PayrollServices.companyManager.removeOnHoldReason(mCompany.getSourceSystemCd(), mSourceCompanyId, onHoldReasonCd);
                    }
                }

               // mCompany.applyRefundsFromVoidedPayrolls();
                Application.getHibernateSession().setFlushMode(previousFlushMode);
            }

        }

        CompanyEvent.createNonAchPaymentReceivedEvent(createdNonAchRedebits);

        return processResult;
    }

    private SpcfMoney getTotalRedebitAmount(Collection<RedebitImpoundDTO> pRedebits) {
        SpcfMoney totalAmount = new SpcfMoney("0.00");

        for (RedebitImpoundDTO currRedebit : pRedebits) {
            totalAmount = new SpcfMoney(totalAmount.add(currRedebit.getAmount()));
        }

        return totalAmount;
    }
}
