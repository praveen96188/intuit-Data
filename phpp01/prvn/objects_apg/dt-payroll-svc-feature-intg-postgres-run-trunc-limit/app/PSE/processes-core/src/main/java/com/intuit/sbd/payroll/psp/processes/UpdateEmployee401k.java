package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kEmployeeInfoDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: Dawn Martens
 * Date: December 14, 2009
 * Time: 3:55:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateEmployee401k extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private ThirdParty401kEmployeeInfoDTO mEmployee401kDTO;
    private Employee mEmployee;
    private Company mCompany;
    private ProcessResult mValidationResult;

    public UpdateEmployee401k(SourceSystemCode pSourceSystemCode,
                          String pSourceCompanyId,
                          ThirdParty401kEmployeeInfoDTO pEmployee401kDTO) {
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourceSystemCd = pSourceSystemCode;
        this.mEmployee401kDTO = pEmployee401kDTO;
    }

    public Employee getEmployee() {
        return mEmployee;
    }

    public void setEmployee(Employee pEmployee) {
        mEmployee=pEmployee;
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mEmployee401kDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.ThirdParty401k, null,"Employee401kDTO");
            return validationResult;
        }

        // Check if Company Exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        return validationResult;

    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        ThirdParty401kInfo ee401kInfo = mEmployee.getThirdParty401kInfo();

        if (ee401kInfo == null) {
            ee401kInfo = new ThirdParty401kInfo();
            mEmployee.setThirdParty401kInfo(ee401kInfo);
        }

        if (mEmployee401kDTO.getOwnershipPercent()!=null) {
            ee401kInfo.setOwnershipPercentage(mEmployee401kDTO.getOwnershipPercent().doubleValue());
        }
        if (mEmployee401kDTO.isFamilyMember()!=null) {
            ee401kInfo.setIsFamilyMember(mEmployee401kDTO.isFamilyMember());
        }
        if (mEmployee401kDTO.isHighlyCompensatedEmployee()!=null) {
            ee401kInfo.setIsHighlyCompensated(mEmployee401kDTO.isHighlyCompensatedEmployee());
        }

        mEmployee.setPhone(mEmployee401kDTO.getPhoneNumber());
        mEmployee.setEmail(mEmployee401kDTO.getEmail());

        mEmployee= Application.save(mEmployee);

        return processResult;
    }

}
