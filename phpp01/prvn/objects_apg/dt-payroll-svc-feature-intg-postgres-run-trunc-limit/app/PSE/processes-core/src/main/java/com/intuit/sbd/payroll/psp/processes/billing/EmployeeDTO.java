package com.intuit.sbd.payroll.psp.processes.billing;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/16/12
 * Time: 5:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmployeeDTO {
	private String employeeName;
	private String mEmployeeListId;
	private String employeeRecordNumber;

	public EmployeeDTO(String pEmployeeName, String pEmployeeListId, String pEmployeeRecordNumber) {
		employeeName = pEmployeeName;
        mEmployeeListId = pEmployeeListId;
		employeeRecordNumber = pEmployeeRecordNumber;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String pEmployeeName) {
		employeeName = pEmployeeName;
	}

    public String getEmployeeListId() {
        return mEmployeeListId;
    }

    public void setEmployeeListId(String pEmployeeListId) {
        mEmployeeListId = pEmployeeListId;
    }

    public String getEmployeeRecordNumber() {
		return employeeRecordNumber;
	}

	public void setEmployeeRecordNumber(String pEmployeeRecordNumber) {
		employeeRecordNumber = pEmployeeRecordNumber;
	}

	// to do: validate all fields
	public ProcessResult validate() {
		ProcessResult validationResult = new ProcessResult();

		if ((employeeName == null) || employeeName.trim().equals("")) {
			validationResult.getMessages().InvalidValue(EntityName.Employee, "EmployeeDTO", "employeeName");
		}

		if ((mEmployeeListId == null) ||
                mEmployeeListId.trim().equals("") ||
				!(Validator.isValidLength(mEmployeeListId, 1, 50))) {
			validationResult.getMessages().InvalidValue(EntityName.Employee, "EmployeeDTO", "employeeID");
		}

		if ((employeeRecordNumber == null) ||
				employeeRecordNumber.trim().equals("") ||
				!(Validator.isValidLength(employeeRecordNumber, 1, 50))) {
			validationResult.getMessages().InvalidValue(EntityName.Employee, "EmployeeDTO", "employeeRecordNumber");
		}

		return validationResult;
	}
}
