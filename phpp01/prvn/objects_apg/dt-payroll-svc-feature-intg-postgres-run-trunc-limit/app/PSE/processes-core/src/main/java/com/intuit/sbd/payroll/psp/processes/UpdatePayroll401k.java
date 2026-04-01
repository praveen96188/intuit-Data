/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/UpdatePayroll401k.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

import java.util.Collection;

/**
 * Core process for updating an existing payroll.
 *
 * @author Dawn Martens
 */

public class UpdatePayroll401k extends Process implements IProcess {
    private Collection<PaycheckDTO> mPaychecks;
    private PayrollRun mPayrollRun;
    private String mTransmissionId;

    /**
     * The process parameters
     *
     * @param pPayrollRun - the payroll run to update
     * @param pPaychecks - the paychecks to update
     */
    public UpdatePayroll401k(PayrollRun pPayrollRun, Collection<PaycheckDTO> pPaychecks, String pTransmissionId) {
        this.mPayrollRun = pPayrollRun;
        this.mPaychecks = pPaychecks;
        mTransmissionId = pTransmissionId;
    }

    /**
     * process request
     * @return result - error messages and status if any
     */
    public ProcessResult process() {
        ProcessResult result = new ProcessResult();
        if (Application.getCurrentPrincipal().getSystemPrincipal() != SystemPrincipal.QBDTWSAdapter) {
            return result;
        }

        ThirdParty401kCompanyServiceInfo tp401kInfo = (ThirdParty401kCompanyServiceInfo) CompanyService.findCompanyService(mPayrollRun.getCompany(), ServiceCode.ThirdParty401k);

        if (tp401kInfo.getStatusCd() == ServiceSubStatusCode.PendingFirstPayroll) {
            ServiceSubStatusCode nextServiceSubStatusCd = tp401kInfo.getNextValidServiceStatus(ServiceSubStatusCode.PendingFirstPayroll);            
            tp401kInfo.updateCompanyServiceStatus(nextServiceSubStatusCd);
        }

        return result;
    }

    /**
     * Validate process prarameters.
     *
     * @return ProcessResult - containing any validation errors
     */
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if (Application.getCurrentPrincipal().getSystemPrincipal() != SystemPrincipal.QBDTWSAdapter) {
            return validationResult;
        }

        for (PaycheckDTO paycheck : mPaychecks) {
            ProcessResult paycheckValidationResult = new ProcessResult();
            paycheckValidationResult.merge(PayrollSubmitHelper.validateLineItems(mPayrollRun.getCompany(), paycheck));
            Paycheck domainPaycheck = Paycheck.findPaycheckInStatus(mPayrollRun.getCompany(), paycheck.getPaycheckId(), PaycheckStatusCode.Active, PaycheckStatusCode.Inactive);
            boolean alreadyOffloadedToTOK = domainPaycheck.hasBeenOffloadedTOTOK() ||
                        (domainPaycheck.getThirdParty401kPaycheck() != null &&
                                domainPaycheck.getThirdParty401kPaycheck().getInitiationDate() == null &&
                                PSPDate.getPSPTime().after(ThirdParty401kPaycheck.calculate401kBaseOffloadDate(domainPaycheck.getPayrollRun().getPaycheckDate())));
            boolean isNew401k = !PayrollSubmitHelper.anyLineItemsInPaycheck(domainPaycheck);
            boolean isNowBeforeCutoff = PayrollRun.isBeforeTOKCutoff(domainPaycheck.getPayrollRun().getPaycheckDate(), PSPDate.getPSPTime());
            boolean originallyMissedCutoff = CompanyEvent.doesMissedCutoffEventExistForPaycheck(mPayrollRun.getCompany(), paycheck.getEmployeeId(), paycheck.getPaycheckId(), domainPaycheck.getPayrollRun().getPaycheckDate().format("MM/dd/yyyy"));
            //If it's new and it is currently not before cutoff, PaycheckDateAfter401kTransmittalDate
            //If it's an update and it was originally not before cutoff, PaycheckDateAfter401kTransmittalDate
            //Otherwise, if it's already been sent to TOK, Updated401kPaycheckAlreadyOffloaded
            if (isNew401k && !isNowBeforeCutoff) {
                paycheckValidationResult.getMessages().PaycheckDateAfter401kTransmittalDate(EntityName.PayrollRun, domainPaycheck.getPayrollRun().getSourcePayRunId(), domainPaycheck.getPayrollRun().getPaycheckDate().format("MM/dd/yyyy"));
            } else if (!isNew401k && originallyMissedCutoff) {
                paycheckValidationResult.getMessages().PaycheckDateAfter401kTransmittalDate(EntityName.PayrollRun, domainPaycheck.getPayrollRun().getSourcePayRunId(), domainPaycheck.getPayrollRun().getPaycheckDate().format("MM/dd/yyyy"));
            } else if (alreadyOffloadedToTOK) {
                paycheckValidationResult.getMessages().Updated401kPaycheckAlreadyOffloaded(EntityName.Paycheck, paycheck.getPaycheckId(), paycheck.getPaycheckId());
            }

            CompanyEvent.updateInvalidPaycheckInformationEvents(mPayrollRun.getCompany(), paycheck.getEmployeeId(), paycheck.getPaycheckId(), mTransmissionId, paycheckValidationResult);
            validationResult.merge(paycheckValidationResult);
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        return validationResult;
    }
}