/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/DeactivateEmployeeCore.java#1 $
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
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;

/**
 * Core process for updating a existing employee.
 * 
 * @author Sean Barenz
 */
public class DeactivateEmployeeCore extends Process implements IProcess {
	private Employee employee;
    private Company company;
    private String sourceCompanyId;
    private SourceSystemCode sourceSystemCd;
    private String sourceEmployeeId;
    private SpcfCalendar terminationDate;

    /**
     *
     * @param pSourceSystemCd Source System Code (String)
     * @param pSourceCompanyId Source Company Id (String)
     * @param pSourceEmployeeId Source Employee Id (String)
     */
    public DeactivateEmployeeCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,  String pSourceEmployeeId,
                                  DateDTO pTerminationDate) {
        sourceCompanyId = pSourceCompanyId;
        sourceSystemCd = pSourceSystemCd;
        sourceEmployeeId = pSourceEmployeeId;
        terminationDate = DateDTO.convertToSpcfCalendar(pTerminationDate);
    }

    /**
	 * Obtains the employee attached to the core process
	 * 
	 * @return Employee domain object
	 */
	public Employee getEmployee() {
		return employee;
	}
	
	/**
	 * Updates the employee in the database.
	 */
	public ProcessResult process() {
		ProcessResult processResult;

        //  Deactivate the employee
        employee.setStatusCd(EmployeeStatus.Inactive);
        employee.setStatusEffectiveDate(PSPDate.getPSPTime());
        employee.setTerminationDate(terminationDate);
        //  Deactivate Employee Bank Accounts
        processResult = employee.deactivateBankAccounts();

        // Save the company (Hybernate quirk)
		employee = Application.save(employee);

        processResult.setResult(employee);
        if(company.isCompanyRequiredForOFACScreening()) {
            CompanyEvent.createChangeEmployeeCompanyEvent(company, EventTypeCode.EmployeeDeleted, employee.getId().toString());
        }
        return processResult;
	}

	/**
	 * Validation stage of the <b>UpdateEmployee</b> process flow. <p/> 1.
	 * Validate the Employee object is not null. <br/> 2. Validate the Company
	 * exists <br/> 3. Validate the Company is active <br/> 4. Is
	 * Employee/IndividualBE active <br/> 5. Validate if the Employee exists
	 * <br/> 6. If Employee exists, is it active <br/>
	 * 
	 * @return ProcessResult Empty if successful, otherwise, will contain error
	 *         messages
	 */
	public ProcessResult validate() {

        // Validate Source Company Id
        ProcessResult validationResult = new ProcessResult();
        if (sourceCompanyId == null) {
                validationResult.getMessages().CompanyIdNotSpecified(
                EntityName.Company, sourceCompanyId);
			return validationResult;
        }

        // Validate Source System Cd
        if (sourceSystemCd == null) {
                validationResult.getMessages().SourceSystemCdNotSpecified(
                EntityName.Company, sourceCompanyId);
			return validationResult;
        }

        // Validate Source Employee Id
        if (sourceEmployeeId == null) {
                validationResult.getMessages().EmployeeIdNotSpecified(
                EntityName.Employee, sourceEmployeeId);
			return validationResult;
        }

        // Validate Company Exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
                validationResult.getMessages().CompanyDoesNotExist(
                EntityName.Company, sourceCompanyId,
                sourceSystemCd.toString(), sourceCompanyId);
			return validationResult;
        }

        // Validate Employee Exists
        employee = Employee.findEmployee(company, sourceEmployeeId);
        if (employee == null) {
                validationResult.getMessages().EmployeeDoesNotExist(
                        EntityName.Employee,
                        sourceEmployeeId,
                        sourceSystemCd.toString(),
                        sourceCompanyId,
                        sourceEmployeeId);
                return validationResult;
        }
        else {
            if (EmployeeStatus.Active != employee.getStatusCd()) {
                validationResult.getMessages().EmployeeAlreadyInactive(
                        EntityName.Employee,
                        employee.getSourceEmployeeId(),
                        company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId(),
                        employee.getSourceEmployeeId());
                return validationResult;
            }
        }

        if (! company.isAllowedCapability(SystemCapabilityCode.ChangeCompanyInfo)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
            return validationResult;
        }

        if (!company.passesAdditionalCancelTermValidation(false, true, true)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
        }


        return validationResult;

	}
}
