/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/PayrollSubmit401k.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.domain.*;

import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Collection;

/**
 * Core process for submitting a 401k payroll
 *
 * @author Dawn Martens
 */

public class PayrollSubmit401k extends Process implements IProcess {
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private PayrollRunDTO payrollRunDTO;
    private Company company;
    private SpcfCalendar payrollRunDate;
    private String transmissionId = null;

    public PayrollSubmit401k(SourceSystemCode pSourceSystemCd, String pCompanyId, PayrollRunDTO pPayrollRunDTO, SpcfCalendar pPayrollRunDate, String pTransmissionId) {
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pCompanyId;
        payrollRunDTO = pPayrollRunDTO;
        payrollRunDate = pPayrollRunDate;
        transmissionId = pTransmissionId;
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

        ThirdParty401kCompanyServiceInfo tp401kInfo = (ThirdParty401kCompanyServiceInfo) CompanyService.findCompanyService(company, ServiceCode.ThirdParty401k);

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

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {   
            return validationResult;
        }

        // Check if Company Exists

        company = Company.findCompany(sourceCompanyId, sourceSystemCd);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        boolean isBeforeTOKCutoff = PayrollRun.isBeforeTOKCutoff(DateDTO.convertToSpcfCalendar(payrollRunDTO.getTargetPayrollTXDate()), payrollRunDate);

        if (payrollRunDTO.getPaychecks()!=null && payrollRunDTO.getPaychecks().size()>0) {
            Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();
            for (PaycheckDTO paycheck : paychecks) {
                ProcessResult paycheckValidationResult = new ProcessResult();
                paycheckValidationResult.merge(PayrollSubmitHelper.validatePaycheckDTOFor401k(company, paycheck));
                paycheckValidationResult.merge(PayrollSubmitHelper.validateLineItems(company, paycheck));

                if (!isBeforeTOKCutoff) {
                    paycheckValidationResult.getMessages().PaycheckDateAfter401kTransmittalDate(EntityName.PayrollRun, payrollRunDTO.getPayrollTXBatchId(), payrollRunDTO.getTargetPayrollTXDate().getMMDDYYYY());
                }

                CompanyEvent.updateInvalidPaycheckInformationEvents(company, paycheck.getEmployeeId(), paycheck.getPaycheckId(), transmissionId, paycheckValidationResult);
                validationResult.merge(paycheckValidationResult);
            }
        }

        return validationResult;
    }
}