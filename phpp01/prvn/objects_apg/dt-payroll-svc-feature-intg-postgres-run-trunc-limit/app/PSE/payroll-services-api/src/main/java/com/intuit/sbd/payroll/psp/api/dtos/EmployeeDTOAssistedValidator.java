package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 1, 2010
 * Time: 5:55:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmployeeDTOAssistedValidator extends EmployeeDTOValidator {
    private static final Pattern DIGIT_PATTERN = Pattern.compile("^[0-9]+$");

    private Company mCompany;

    public EmployeeDTOAssistedValidator(Company pCompany) {
        mCompany = pCompany;
    }

    public ProcessResult validate(EmployeeDTO employeeDTO) {
        ProcessResult validationResult = new ProcessResult();
        //  Validate the employee id
        if ( (employeeDTO.getEmployeeId() == null) || (employeeDTO.getEmployeeId().trim().equals(""))) {
            validationResult.getMessages().EmployeeIdNotSpecified(EntityName.Employee, null);
        } else if (!Validator.isValidLength(employeeDTO.getEmployeeId(), 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.Employee, employeeDTO.getEmployeeId(), "Source Employee ID");
        } else if(employeeDTO.getEmployeeId().equals("0") || !DIGIT_PATTERN.matcher(employeeDTO.getEmployeeId()).matches()) {
            String sourceSystemCd = (mCompany != null) ? mCompany.getSourceSystemCd().toString() : "";
            String sourceCompanyId = (mCompany != null) ? mCompany.getSourceCompanyId() : "";
            validationResult.getMessages().EmployeeNotExist(EntityName.Employee, employeeDTO.getEmployeeId(), sourceSystemCd, sourceCompanyId, employeeDTO.getEmployeeId());
        }

        if (employeeDTO.getHireDate() != null) {
            validationResult.merge(employeeDTO.getHireDate().validate());
        }
        if (employeeDTO.getBirthDate() != null) {
            validationResult.merge(employeeDTO.getBirthDate().validate());
        }
        if (employeeDTO.getTerminationDate() != null) {
            validationResult.merge(employeeDTO.getTerminationDate().validate());
        }

        if ((employeeDTO.getFirstName() == null || !Validator.isValidLength(employeeDTO.getFirstName().trim(), 1, 80))
                && (employeeDTO.getLastName() == null || !(Validator.isValidLength(employeeDTO.getLastName().trim(), 1, 80)))) {
            validationResult.getMessages().InvalidValue(EntityName.Employee, employeeDTO.getEmployeeId(), "Employee Name");
        }

        // Add warnings
        if (validationResult.isSuccess()) {
            if (mCompany != null && mCompany.isCompanyOnService(ServiceCode.WorkersComp)) {
                if (employeeDTO.getFirstName() == null || !Validator.isValidLength(employeeDTO.getFirstName().trim(), 1, 80)) {
                    validationResult.addWarings(ServiceCode.WorkersComp, "<li>For employee " + employeeDTO.getLastName() + ", the first name is missing or too long.</li>");
                }

                if (employeeDTO.getLastName() == null || !Validator.isValidLength(employeeDTO.getLastName().trim(), 1, 80)) {
                    validationResult.addWarings(ServiceCode.WorkersComp, "<li>For employee " + employeeDTO.getFirstName() + ", the last name is missing or too long.</li>");
                }

                if (employeeDTO.getWorkState() ==  null || !Validator.isValidLength(employeeDTO.getWorkState().trim(), 2, 20)) {
                    validationResult.addWarings(ServiceCode.WorkersComp, "<li>For employee " + employeeDTO.getFullName() + ", the work state is missing or too long.</li>");
                }
            }
        }

        return validationResult;
    }
}
