package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.Validator;

/**
 * User: rnorian
 * Date: Jan 21, 2010
 * Time: 10:02:54 PM
 */
public class EmployeeDTO401kValidator extends EmployeeDTOValidator {
    public ProcessResult validate(EmployeeDTO employeeDTO) {
        ProcessResult validationResult = new ProcessResult();

        // REJECT SAVING - ERROR if bad source employeeId
        if ((employeeDTO.getEmployeeId() == null) || (employeeDTO.getEmployeeId().trim().equals(""))) {
            validationResult.getMessages().EmployeeIdNotSpecified(EntityName.Employee, null);
        } else if (!Validator.isValidLength(employeeDTO.getEmployeeId(), 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.Employee, employeeDTO.getEmployeeId(), "EmployeeId");
        }

        // WARNINGS if required fields missing or validation failures
        if (employeeDTO.getSocialSecurityNumber() == null) {
            validationResult.getMessages().RequiredAttribute(EntityName.Employee, employeeDTO.getEmployeeId(), "social security number", MessageInfo.MessageLevel.WARNING);
        } else if (!Validator.isMatchingPattern(employeeDTO.getSocialSecurityNumber(), "^[0-9]{9}$")) {
            validationResult.getMessages().PatternValidationFailure(EntityName.Employee, employeeDTO.getEmployeeId(), "social security number", "all numbers with no dashes", MessageInfo.MessageLevel.ERROR);
        }

        if (employeeDTO.getFirstName() == null || employeeDTO.getFirstName().trim().length() == 0) {
            validationResult.getMessages().RequiredAttribute(EntityName.Employee, employeeDTO.getEmployeeId(), "first name", MessageInfo.MessageLevel.WARNING);
            employeeDTO.setFirstName("");
        } else if (!Validator.isValidLength(employeeDTO.getFirstName(), 1, 80)) {
            // TODO: either warn or truncate, not both
            validationResult.getMessages().InvalidLength(EntityName.Employee, employeeDTO.getEmployeeId(), "first name", 80, MessageInfo.MessageLevel.ERROR);
        } else if (!Validator.isValidLength(employeeDTO.getFirstName(), 1, 20)) {
            validationResult.getMessages().InvalidLength(EntityName.Employee, employeeDTO.getEmployeeId(), "first name", 20, MessageInfo.MessageLevel.INFO);
        }

        if (employeeDTO.getMiddleName() != null && employeeDTO.getMiddleName().length() > 80) {
            validationResult.getMessages().InvalidLength(EntityName.Employee, employeeDTO.getEmployeeId(), "middle name", 80, MessageInfo.MessageLevel.ERROR);
        } else if (!Validator.isValidLength(employeeDTO.getMiddleName(), 0, 20)) {
            validationResult.getMessages().InvalidLength(EntityName.Employee, employeeDTO.getEmployeeId(), "middle name", 20, MessageInfo.MessageLevel.INFO);
        }

        if (employeeDTO.getLastName() == null || employeeDTO.getLastName().trim().length() == 0) {
            validationResult.getMessages().RequiredAttribute(EntityName.Employee, employeeDTO.getEmployeeId(), "last name", MessageInfo.MessageLevel.WARNING);
            employeeDTO.setLastName("");
        } else if (!Validator.isValidLength(employeeDTO.getLastName(), 1, 80)) {
            validationResult.getMessages().InvalidLength(EntityName.Employee, employeeDTO.getEmployeeId(), "last name", 80, MessageInfo.MessageLevel.ERROR);
        } else if (!Validator.isValidLength(employeeDTO.getLastName(), 1, 20)) {
            validationResult.getMessages().InvalidLength(EntityName.Employee, employeeDTO.getEmployeeId(), "last name", 20, MessageInfo.MessageLevel.INFO);
        }

        if (!Validator.isValidLength(employeeDTO.getSuffix(), 0, 20)) {
            validationResult.getMessages().InvalidLength(EntityName.Employee, employeeDTO.getEmployeeId(), "suffix", 20, MessageInfo.MessageLevel.ERROR);
        } else if (!Validator.isValidLength(employeeDTO.getSuffix(), 1, 20)) {
            validationResult.getMessages().InvalidLength(EntityName.Employee, employeeDTO.getEmployeeId(), "suffix", 20, MessageInfo.MessageLevel.INFO);
        }

        if (employeeDTO.getLiveAddress() == null) {
            validationResult.getMessages().RequiredAttribute(EntityName.Employee, employeeDTO.getEmployeeId(), "address", MessageInfo.MessageLevel.INFO);
        } else {
            employeeDTO.getLiveAddress().setValidator(new AddressDTO401kValidator(employeeDTO.getEmployeeId(), "address"));
            validationResult.merge(employeeDTO.getLiveAddress().validateAddressDTO());
        }

        if (employeeDTO.getHireDate() == null) {
            validationResult.getMessages().RequiredAttribute(EntityName.Employee, employeeDTO.getEmployeeId(), "hire date", MessageInfo.MessageLevel.WARNING);
        } else {
            validationResult.merge(employeeDTO.getHireDate().validate(new DateDTO401kValidator(EntityName.Employee, employeeDTO.getEmployeeId(), "hire date")));
        }

        if (employeeDTO.getFedAllowances() < 0) {
            validationResult.getMessages().RangeValidationFailure(EntityName.Employee, employeeDTO.getEmployeeId(), "federal allowances", 0);
        }

        if (employeeDTO.getEmployee401kInfo() != null) {
            employeeDTO.getEmployee401kInfo().setValidator(new ThirdParty401kEmployeeInfoDTO401KValidator());
            validationResult.merge(employeeDTO.getEmployee401kInfo().validate(employeeDTO));
        }

        return validationResult;
    }
}
