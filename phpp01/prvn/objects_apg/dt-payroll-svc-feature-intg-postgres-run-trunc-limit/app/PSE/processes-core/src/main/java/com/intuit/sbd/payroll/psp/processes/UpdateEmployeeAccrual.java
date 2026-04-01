package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeAccrualDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTEmployeeInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang.ObjectUtils;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 22, 2010
 * Time: 8:13:08 AM
 */
public class UpdateEmployeeAccrual extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private EmployeeDTO mEmployeeDTO;
    private Employee mEmployee;

    public UpdateEmployeeAccrual(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, EmployeeDTO pEmployeeDTO) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mEmployeeDTO = pEmployeeDTO;
    }

    @Override
    public ProcessResult validate() {
        //  Validate inputs from DTO
        ProcessResult validationResult = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // validate company
        Company company = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // validate employee
        if(mEmployeeDTO == null) {
            validationResult.getMessages().InvalidArgument(EntityName.Employee, "null", "EmployeeDTO");
            return validationResult;
        }

        if (mEmployeeDTO.getExistingEmployeeGuid() != null && mEmployeeDTO.getExistingEmployeeGuid().trim().length() != 0) {
            mEmployee = Application.findById(Employee.class, SpcfUniqueId.createInstance(mEmployeeDTO.getExistingEmployeeGuid()));
        } else {
            mEmployee = Employee.findEmployee(company, mEmployeeDTO.getEmployeeId());
        }

        if (mEmployee == null) {
            validationResult.getMessages().EmployeeDoesNotExist(EntityName.Employee,
                                                                mEmployeeDTO.getEmployeeId(), mSourceSystemCd.toString(), mSourceCompanyId,
                                                                mEmployeeDTO.getEmployeeId());
            return validationResult;
        }

        for (EmployeeAccrualDTO employeeAccrualDTO : mEmployeeDTO.getEmployeeAccrualDTOs()) {
            if(employeeAccrualDTO.getAccrualType() == null) {
                validationResult.getMessages().InvalidArgument(EntityName.Employee, "null", "AccrualType");
                return validationResult;
            }
        }
                
        return validationResult;
    }

    @Override
    public ProcessResult<Employee> process() {
        ProcessResult<Employee> processResult = new ProcessResult<Employee>();

        QbdtEmployeeInfo qbdtEmployeeInfo = mEmployee.getQbdtEmployeeInfo();
        QBDTEmployeeInfoDTO qbdtEmployeeInfoDTO = mEmployeeDTO.getQBDTEmployeeInfoDTO();
        if(qbdtEmployeeInfo != null &&  qbdtEmployeeInfoDTO != null) {
            qbdtEmployeeInfo.setIsAssisted(qbdtEmployeeInfoDTO.getIsAssisted());
            if (qbdtEmployeeInfoDTO.getListId() != null) {
                qbdtEmployeeInfo.setListId(qbdtEmployeeInfoDTO.getListId());
            }
            Application.save(qbdtEmployeeInfo);
        }

        for (EmployeeAccrualDTO employeeAccrualDTO : mEmployeeDTO.getEmployeeAccrualDTOs()) {
            EmployeeAccrual employeeAccrual = mEmployee.getEmployeeAccrualCollection().findEntity(EmployeeAccrual.AccrualType().equalTo(employeeAccrualDTO.getAccrualType()));
            if(employeeAccrual == null) {
                employeeAccrual = new EmployeeAccrual();
                employeeAccrual.setAccrualType(employeeAccrualDTO.getAccrualType());
                employeeAccrual.setEmployee(mEmployee);
                employeeAccrual = Application.save(employeeAccrual);
                mEmployee.getEmployeeAccrualCollection().add(employeeAccrual);
            }
            employeeAccrual.setHours(employeeAccrualDTO.getHours());
            Application.save(employeeAccrual);
        }

        processResult.setResult(mEmployee);
        return processResult;
    }

    public Employee getEmployee() {
        return mEmployee;
    }

    public void setEmployee(Employee pEmployee) {
        mEmployee = pEmployee;
    }
}
