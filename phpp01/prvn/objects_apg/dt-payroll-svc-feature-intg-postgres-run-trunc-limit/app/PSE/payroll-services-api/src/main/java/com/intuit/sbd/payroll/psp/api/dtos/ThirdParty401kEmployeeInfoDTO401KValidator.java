package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.Validator;

/**
 * User: rnorian
 * Date: Mar 26, 2010
 * Time: 1:29:33 PM
 */
public class ThirdParty401kEmployeeInfoDTO401KValidator extends ThirdParty401kEmployeeInfoDTOCoreValidator {
    @Override
    public ProcessResult validate(EmployeeDTO employeeDTO, ThirdParty401kEmployeeInfoDTO p401KInfo) {
        ProcessResult validationResult = super.validate(employeeDTO, p401KInfo);

        String email = null;
        String phoneNumber = null;
        DateDTO birthDate = null;
        birthDate = employeeDTO.getBirthDate();

        if (p401KInfo != null) {
            email = p401KInfo.getEmail();
            phoneNumber = p401KInfo.getPhoneNumber();
        }

        if (email == null || email.trim().length() == 0) {
            validationResult.getMessages().RequiredAttribute(EntityName.Employee,  employeeDTO.getEmployeeId(), "Email", MessageInfo.MessageLevel.INFO);
        } else if (!Validator.isValidEmail(email)) {
            validationResult.getMessages().PatternValidationFailure(EntityName.Employee, employeeDTO.getEmployeeId(), "Email", "userid@domain.com", MessageInfo.MessageLevel.INFO);
        }

        if (phoneNumber == null || phoneNumber.trim().length() == 0) {
            validationResult.getMessages().RequiredAttribute(EntityName.Employee,  employeeDTO.getEmployeeId(), "phone number", MessageInfo.MessageLevel.INFO);
        } else if (!Validator.isValidPhone(phoneNumber)) {
            validationResult.getMessages().PatternValidationFailure(EntityName.Employee, employeeDTO.getEmployeeId(), "phone number", "###-###-#### or ########## or (###) ###-####  (area code must be included)", MessageInfo.MessageLevel.INFO);
        }

        if (birthDate == null) {
            validationResult.getMessages().RequiredAttribute(EntityName.Employee, employeeDTO.getEmployeeId(), "birth date", MessageInfo.MessageLevel.WARNING);
        }


        // don't perform validations, these values are never provided
        /*
        if (isHighlyCompensatedEmployee == null) {
            validationResult.getMessages().RequiredAttribute(EntityName.Employee, employeeDTO.getEmployeeId(), "highly compensated employee (HCE)", MessageInfo.MessageLevel.WARNING);
        }

        if (isFamilyMember == null) {
            validationResult.getMessages().RequiredAttribute(EntityName.Employee, employeeDTO.getEmployeeId(), "family member", MessageInfo.MessageLevel.WARNING);
        }

        if (ownershipPercent == null) {
            validationResult.getMessages().RequiredAttribute(EntityName.Employee, employeeDTO.getEmployeeId(), "ownership percent", MessageInfo.MessageLevel.WARNING);
        }
        */

        return validationResult;
    }
}
