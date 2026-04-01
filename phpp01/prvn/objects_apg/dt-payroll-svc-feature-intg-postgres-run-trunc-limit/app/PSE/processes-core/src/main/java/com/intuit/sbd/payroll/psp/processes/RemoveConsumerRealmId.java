package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang.StringUtils;

/**
 * User: ihannur
 * Date: 6/12/13
 * Time: 1:50 PM
 */
public class RemoveConsumerRealmId extends Process implements IProcess {

    private String mEmpId;
    private Employee mEmployee;

    public RemoveConsumerRealmId(String pEmpId) {
        mEmpId = pEmpId;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if(StringUtils.isBlank(mEmpId)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.Employee, "Employee Id", "Employee Id");
            return validationResult;
        }

        mEmployee = Application.findById(Employee.class, SpcfUniqueId.createInstance(mEmpId));
        if(mEmployee == null) {
            validationResult.getMessages().NoEntityWithGivenId("Employee", mEmpId);
        }

        return validationResult;
    }

    @Override
    public ProcessResult<Employee> process() {
        ProcessResult<Employee> result = new ProcessResult<Employee>();
        mEmployee.setConsumerRealmId(null);
        Application.save(mEmployee);
        result.setResult(mEmployee);
        return result;
    }
}
