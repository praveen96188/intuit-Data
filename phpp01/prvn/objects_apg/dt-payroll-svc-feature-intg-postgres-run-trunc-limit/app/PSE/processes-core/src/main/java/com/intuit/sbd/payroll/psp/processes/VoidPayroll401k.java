package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 20, 2009
 * Time: 2:30:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class VoidPayroll401k extends Process implements IProcess {

    private Company mCompany;
    private List<Paycheck> alreadyOffloadedPaychecks = new ArrayList<Paycheck>();
    private List<Paycheck> mPaychecksToVoid = new ArrayList<Paycheck>();
    private String mTransmissionId;

    public VoidPayroll401k(Company pCompany, List<Paycheck> paychecksToVoid, String pTransmissionId) {
        mCompany = pCompany;
        mPaychecksToVoid = paychecksToVoid;
        mTransmissionId = pTransmissionId;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if (Application.getCurrentPrincipal().getSystemPrincipal() != SystemPrincipal.QBDTWSAdapter) {
            return validationResult;
        }

        if (mCompany == null) {
            validationResult.getMessages().InvalidValue(EntityName.Company, null, "Company missing for VoidPayroll401k");
            return validationResult;
        }

        //Validate paychecks to be voided
        if (mPaychecksToVoid!=null && !mPaychecksToVoid.isEmpty()) {
            for (Paycheck currentPaycheck : mPaychecksToVoid) {
                ProcessResult paycheckValidationResult = new ProcessResult();
                boolean alreadyOffloadedToTOK = currentPaycheck.hasBeenOffloadedTOTOK() ||
                        (currentPaycheck.getThirdParty401kPaycheck() != null &&
                                currentPaycheck.getThirdParty401kPaycheck().getInitiationDate() == null &&
                                PSPDate.getPSPTime().after(ThirdParty401kPaycheck.calculate401kBaseOffloadDate(currentPaycheck.getPayrollRun().getPaycheckDate())));
                if (alreadyOffloadedToTOK && currentPaycheck.getPayrollRun().isBeforeTOKCutoff()) {
                    paycheckValidationResult.getMessages().Voided401kPaycheckAlreadyOffloaded(EntityName.Paycheck, currentPaycheck.getId().toString(), currentPaycheck.getSourcePaycheckId());
                    alreadyOffloadedPaychecks.add(currentPaycheck);
                }
                CompanyEvent.updateInvalidPaycheckInformationEvents(mCompany, currentPaycheck.getSourceEmployee().getSourceEmployeeId(), currentPaycheck.getSourcePaycheckId(), mTransmissionId, paycheckValidationResult);
                validationResult.merge(paycheckValidationResult);
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        if (Application.getCurrentPrincipal().getSystemPrincipal() != SystemPrincipal.QBDTWSAdapter) {
            return processResult;
        }

        if (alreadyOffloadedPaychecks !=null && alreadyOffloadedPaychecks.size()>0) {
            CompanyEvent.createVoidedPaychecksAlreadyOffloadedEvent(mCompany, alreadyOffloadedPaychecks);
        }

        return processResult;
    }
}