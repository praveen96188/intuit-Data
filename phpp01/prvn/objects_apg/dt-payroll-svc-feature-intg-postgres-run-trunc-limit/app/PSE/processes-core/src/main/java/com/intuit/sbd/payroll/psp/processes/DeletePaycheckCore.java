package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dawn Martens
 * Date: Feb 16, 2010
 * Time: 10:35:48 AM
 */

public class DeletePaycheckCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd = null;
    private String mSourceCompanyId = null;
    private Paycheck mPaycheck = null;
    private Company mCompany = null;
    private String mSourcePaycheckId = null;
    private DeletePaycheck401k deletePaycheck401k = null;
    private String mTransmissionId = null;

    private CancelOrDeletePayrollWorkersComp cancelOrDeletePayrollWorkersComp;


    public DeletePaycheckCore(SourceSystemCode pSourceSystemCd, String pCompanyId, String pSourcePaycheckId, String pTransmissionId) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pCompanyId;
        mSourcePaycheckId = pSourcePaycheckId;
        mTransmissionId = pTransmissionId; 
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

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

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        mPaycheck = Paycheck.findPaycheck(mCompany, mSourcePaycheckId);

        if (mPaycheck == null) {
            validationResult.getMessages().PaycheckDoesNotExist(EntityName.PayrollRun, mSourcePaycheckId, mSourceSystemCd.toString(), mSourceCompanyId, mSourcePaycheckId);
            return validationResult;
        }
              
        if (mPaycheck.getStatus() == PaycheckStatusCode.Inactive || mPaycheck.isVoided()) {
            validationResult.getMessages().PaycheckAlreadyCanceled(EntityName.PayCheck, mSourcePaycheckId, mSourcePaycheckId);
        }

        if (mCompany.isCompanyOnService(ServiceCode.WorkersComp)) {
            List<Paycheck> paychecks = new ArrayList<Paycheck>();
            paychecks.add(mPaycheck);
            paychecks.add(mPaycheck);
            cancelOrDeletePayrollWorkersComp = new CancelOrDeletePayrollWorkersComp(paychecks);
            validationResult.merge(cancelOrDeletePayrollWorkersComp.validate());
        }

        boolean mustProcess401k =
                mCompany.isCompanyOnService(ServiceCode.ThirdParty401k)
                && Application.getCurrentPrincipal().getSystemPrincipal() == SystemPrincipal.QBDTWSAdapter;

        if (mustProcess401k) {
            deletePaycheck401k = new DeletePaycheck401k(mCompany, mPaycheck, mTransmissionId);
            validationResult.merge(deletePaycheck401k.validate());
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        //Ignore already-deleted paychecks
        if (mPaycheck.getStatus()!= PaycheckStatusCode.Deleted) {

            mPaycheck.setStatus(PaycheckStatusCode.Deleted);
            if (cancelOrDeletePayrollWorkersComp != null){
                processResult.merge(cancelOrDeletePayrollWorkersComp.process());
            }
            //todo: create event
        }
        if (deletePaycheck401k!=null) {
            processResult.merge(deletePaycheck401k.process());
        }

        return processResult;
    }


}