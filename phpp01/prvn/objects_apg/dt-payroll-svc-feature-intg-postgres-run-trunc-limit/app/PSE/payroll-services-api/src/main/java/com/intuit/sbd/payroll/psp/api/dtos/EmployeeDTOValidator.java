package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

/**
 * User: rnorian
 * Date: Jan 21, 2010
 * Time: 10:29:36 PM
 */
public class EmployeeDTOValidator {
    public ProcessResult validate(EmployeeDTO employeeDTO) {
        ProcessResult validationResult = new ProcessResult();

        //  Validate the employee id
        if ( (employeeDTO.getEmployeeId() == null) || (employeeDTO.getEmployeeId().trim().equals(""))) {
            validationResult.getMessages().EmployeeIdNotSpecified(EntityName.Employee, null);
        } else if (!Validator.isValidLength(employeeDTO.getEmployeeId(), 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.Employee, employeeDTO.getEmployeeId(), "employeeDTO.getEmployeeId()");
        }

        if (employeeDTO.getEmployee401kInfo() != null) {
            /**
             * These 'validator' classes (EmployeeDTOValidator, ThirdParty401kEmployeeInfoDTOCoreValidator) are abominations and
             * neeed to be removed w/the validations moved to the core processes once the OFX is eliminated so there are not
             * 2 routes of adding employees.
             */
            employeeDTO.getEmployee401kInfo().setValidator(new ThirdParty401kEmployeeInfoDTOCoreValidator());
            validationResult.merge(employeeDTO.getEmployee401kInfo().validate(employeeDTO));
        }

        return validationResult;        
    }
}
