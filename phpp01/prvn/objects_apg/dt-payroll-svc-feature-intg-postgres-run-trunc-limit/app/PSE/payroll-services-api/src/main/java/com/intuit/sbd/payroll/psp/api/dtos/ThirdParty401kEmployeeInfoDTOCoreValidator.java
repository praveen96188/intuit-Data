package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.Validator;

/**
 * User: rnorian
 * Date: Mar 26, 2010
 * Time: 1:29:11 PM
 *
 * These 'validator' classes (EmployeeDTOValidator, ThirdParty401kEmployeeInfoDTOCoreValidator) are abominations and
 * neeed to be removed w/the validations moved to the core processes once the OFX is eliminated so there are not
 * 2 routes of adding employees.
 */
public class ThirdParty401kEmployeeInfoDTOCoreValidator {
    public ProcessResult validate(EmployeeDTO employeeDTO, ThirdParty401kEmployeeInfoDTO p401KInfo) {
        ProcessResult validationResult = new ProcessResult();

        String phoneNumber = null;
        DateDTO birthDate = null;
        DateDTO terminationDate = null;
        birthDate = employeeDTO.getBirthDate();
        terminationDate = employeeDTO.getTerminationDate();

        if (p401KInfo != null) {
            phoneNumber = p401KInfo.getPhoneNumber();
        }

        if (!Validator.isValidLength(phoneNumber, 1, 80)) {
            validationResult.getMessages().InvalidLength(EntityName.Employee, employeeDTO.getEmployeeId(), "phone number", 80, MessageInfo.MessageLevel.ERROR);
        }

        // use the 401k validator b/c nothing 401K specific in there, just better error messaging
        if (birthDate != null)
            validationResult.merge(birthDate.validate(new DateDTO401kValidator(EntityName.Employee, employeeDTO.getEmployeeId(), "birth date")));

        if (terminationDate != null)
            validationResult.merge(terminationDate.validate(new DateDTO401kValidator(EntityName.Employee, employeeDTO.getEmployeeId(), "termination date", MessageInfo.MessageLevel.ERROR)));

        return validationResult;

    }
}
