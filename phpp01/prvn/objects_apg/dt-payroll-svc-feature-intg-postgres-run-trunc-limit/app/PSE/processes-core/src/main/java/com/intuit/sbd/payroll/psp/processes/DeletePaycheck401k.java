package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

/**
 * Created by IntelliJ IDEA.
 * User: Dawn Martens
 * Date: Feb 16, 2010
 * Time: 10:35:48 AM
 */

public class DeletePaycheck401k extends Process implements IProcess {
    private Paycheck mPaycheck = null;
    private Company mCompany = null;
    private String mTransmissionId = null;
    private boolean alreadyOffloaded = false;

    public DeletePaycheck401k(Company pCompany, Paycheck pPaycheck, String pTransmissionId) {
        mCompany = pCompany;
        mPaycheck = pPaycheck;
        mTransmissionId = pTransmissionId;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if (Application.getCurrentPrincipal().getSystemPrincipal() != SystemPrincipal.QBDTWSAdapter) {
            return validationResult;
        }

        if (mCompany == null) {
            validationResult.getMessages().InvalidValue(EntityName.Company, null, "Company is null in DeletePaycheck401k");
            return validationResult;
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mPaycheck == null) {
            validationResult.getMessages().InvalidValue(EntityName.Paycheck, null, "Paycheck is null in DeletePaycheck401k");
            return validationResult;
        }

        boolean alreadyOffloadedToTOK = mPaycheck.hasBeenOffloadedTOTOK() ||
                        (mPaycheck.getThirdParty401kPaycheck() != null &&
                                mPaycheck.getThirdParty401kPaycheck().getInitiationDate() == null &&
                                PSPDate.getPSPTime().after(ThirdParty401kPaycheck.calculate401kBaseOffloadDate(mPaycheck.getPayrollRun().getPaycheckDate())));

        if (mPaycheck.getStatus() == PaycheckStatusCode.Active && alreadyOffloadedToTOK && mPaycheck.getPayrollRun().isBeforeTOKCutoff()) {
            validationResult.getMessages().Deleted401kPaycheckAlreadyOffloaded(EntityName.PayCheck, mPaycheck.getSourcePaycheckId(), mPaycheck.getSourcePaycheckId());
            alreadyOffloaded = true;
        }
        
         CompanyEvent.updateInvalidPaycheckInformationEvents(mCompany, mPaycheck.getSourceEmployee().getSourceEmployeeId(), mPaycheck.getSourcePaycheckId(), mTransmissionId, validationResult);

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        if (Application.getCurrentPrincipal().getSystemPrincipal() != SystemPrincipal.QBDTWSAdapter) {
            return processResult;
        }

        if (alreadyOffloaded) {
            CompanyEvent.createDeletedPaycheckAlreadyOffloadedEvent(mCompany, mPaycheck);
        }
        return processResult;
    }


}