package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.RedebitImpoundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: Dawn Martens
 * Date: July 2008
 * Time: 3:22:47 PM
 */
public class AddOrEditPayrollRelatedRedebit extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private Company mCompany;
    private PayrollRun mPayrollRun;
    private Collection<RedebitImpoundDTO> mRedebits;
    private List<AddRedebitImpoundTransactionCore> addRedebitProcesses;
    private List<UpdateRedebitImpoundTransactionCore> updateRedebitProcesses;
    private DomainEntitySet<FinancialTransaction> updatedOrCreatedRedebits;

    public DomainEntitySet<FinancialTransaction> getFinancialTransactions() {
        return updatedOrCreatedRedebits;
    }

    public AddOrEditPayrollRelatedRedebit(SourceSystemCode pSourceSystemCode,
                                          String pSourceCompanyId,
                                          Collection<RedebitImpoundDTO> pRedebits) {
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourceSystemCd = pSourceSystemCode;
        this.mRedebits = pRedebits;

        addRedebitProcesses = new ArrayList<AddRedebitImpoundTransactionCore>();
        updateRedebitProcesses = new ArrayList<UpdateRedebitImpoundTransactionCore>();
        updatedOrCreatedRedebits = new DomainEntitySet<FinancialTransaction>();
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

        SpcfMoney totalRedebitAmount = getTotalRedebitAmount(mRedebits);
        if (totalRedebitAmount.compareTo(new SpcfMoney("0.00")) <= 0) {
            validationResult.getMessages().AmountNotPositive(EntityName.FinancialTransaction, totalRedebitAmount.toString());
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

        boolean bFoundUpdate = false;
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

            if (currRedebitImpoundDTO.getSettlementType()!=null && currRedebitImpoundDTO.getSettlementType() != SettlementTypeDTO.ACH) {
                validationResult.getMessages().InvalidSettlementTypeCode(EntityName.SettlementType, currRedebitImpoundDTO.getOriginalFinancialTxId(), currRedebitImpoundDTO.getSettlementType().toString());
                return validationResult;
            }

            //If there is an existing pending redebit for this original transaction, create an add process for it
            // otherwise, create an update process for it
            //If there is NOT an existing pending redebit AND the redebit impound amount is zero, bypass any process creation
            FinancialTransaction existingRedebit = FinancialTransaction.getPendingRedebitTransaction(currRedebitImpoundDTO.getOriginalFinancialTxId());
            if (existingRedebit == null) {
                if (currRedebitImpoundDTO.getAmount().compareTo(new SpcfMoney("0.00"))==0) {
                    continue;
                }
                AddRedebitImpoundTransactionCore currAddRedebitImpound = new AddRedebitImpoundTransactionCore(mSourceSystemCd, mSourceCompanyId, currRedebitImpoundDTO);
                validationResult.merge(currAddRedebitImpound.validate());
                addRedebitProcesses.add(currAddRedebitImpound);
            } else {
                bFoundUpdate = true;
                //We want to edit the existing redebit, so set that as the original financial transaction ID
                String originalTransactionId = currRedebitImpoundDTO.getOriginalFinancialTxId();
                currRedebitImpoundDTO.setOriginalFinancialTxId(existingRedebit.getId().toString());
                UpdateRedebitImpoundTransactionCore currUpdateRedebitImpound = new UpdateRedebitImpoundTransactionCore(mSourceSystemCd, mSourceCompanyId, currRedebitImpoundDTO);
                validationResult.merge(currUpdateRedebitImpound.validate());
                updateRedebitProcesses.add(currUpdateRedebitImpound);
                //Now set it back to the original id for use elsewhere
                currRedebitImpoundDTO.setOriginalFinancialTxId(originalTransactionId);
            }
        }

        //If we are updating any transactions, consider this an edit
        ActionEvent actionEvent = null;
        if (bFoundUpdate) {
            actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.DDRedebitEdit);
        } else {
            actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.DDRedebitAdd);
        }

        //Check if payroll run status is valid for the action (update or add)
        PayrollStatus statusCode = mPayrollRun.getPayrollRunStatus();
        if (!mPayrollRun.validateAction(actionEvent)) {
            validationResult.getMessages().ActionNotValidForPayrollRun(EntityName.PayrollRun,
                    mPayrollRun.getSourcePayRunId(),
                    actionEvent.getCode().toString(), mPayrollRun.getSourcePayRunId(), statusCode.toString());
            return validationResult;
        }

        return validationResult;

    }

    public ProcessResult process() {
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = new ProcessResult<DomainEntitySet<FinancialTransaction>>();

        //Call the process method for all the AddRedebitImpoundTransactionCore processes we created in validate
        for (AddRedebitImpoundTransactionCore currAddProc : addRedebitProcesses) {
            ProcessResult<FinancialTransaction> addProcResult =currAddProc.process();
            processResult.merge(addProcResult);
            updatedOrCreatedRedebits.add(currAddProc.getFinancialTransaction());
        }

        //Call the process method for all the AddRedebitImpoundTransactionCore processes we created in validate
        for (UpdateRedebitImpoundTransactionCore currUpdProc : updateRedebitProcesses) {
            ProcessResult<FinancialTransaction> updProcResult = currUpdProc.process();
            processResult.merge(updProcResult);

            // there won't be an updated redebit FT if the updated redebit amount is zero
            FinancialTransaction updatedRedebitFT = currUpdProc.getFinancialTransaction();
            if (updatedRedebitFT != null) {
                updatedOrCreatedRedebits.add(updatedRedebitFT);
            }
        }

        PayrollStatus existingPayrollStatus = mPayrollRun.getPayrollRunStatus();
        if (PayrollStatus.PendingRedebit != existingPayrollStatus) {
            mPayrollRun.updatePayrollRunStatus(PayrollStatus.PendingRedebit);

            // Creae a new ACHRetutnStatusChange event
            CompanyEvent.createACHReturnStatusChangeEvent(mCompany, mPayrollRun.getId().toString(), existingPayrollStatus,
                mPayrollRun.getPayrollRunStatus());
        }

        // create manual redebit company event for all created or updated redebits
        CompanyEvent.createManualRedebitCreatedEvent(mCompany, updatedOrCreatedRedebits);
        
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