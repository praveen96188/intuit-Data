package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.TransactionSummary;
import com.intuit.sbd.payroll.psp.api.dtos.RedebitImpoundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kEmployeeInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Collection;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: Dawn Martens
 * Date: December 14, 2009
 * Time: 3:55:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddEmployee401k extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private ThirdParty401kEmployeeInfoDTO mEmployee401kDTO;
    private Employee mEmployee;
    private Company mCompany;
    private ProcessResult mValidationResult;

    public AddEmployee401k(SourceSystemCode pSourceSystemCode,
                           String pSourceCompanyId,
                           ThirdParty401kEmployeeInfoDTO pEmployee401kDTO, ProcessResult validationResult) {
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourceSystemCd = pSourceSystemCode;
        this.mEmployee401kDTO = pEmployee401kDTO;
        this.mValidationResult = validationResult;
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
        validationResult.merge(Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
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

        validationResult.merge(mValidationResult);

        validationResult = MessageList.replaceCompanyOperationNotAllowedMessages(validationResult, SystemCapabilityCode.ChangeCompanyInfo, mCompany.getSourceSystemCd().toString(), mCompany.getSourceCompanyId());

        return validationResult;

    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        ThirdParty401kInfo ee401kInfo = new ThirdParty401kInfo();
        
        if (mEmployee401kDTO.getOwnershipPercent()!=null) {
            ee401kInfo.setOwnershipPercentage(mEmployee401kDTO.getOwnershipPercent().doubleValue());
        }

        if (mEmployee401kDTO.isFamilyMember()!=null) {
            ee401kInfo.setIsFamilyMember(mEmployee401kDTO.isFamilyMember());
        }

        if (mEmployee401kDTO.isHighlyCompensatedEmployee()!=null) {
            ee401kInfo.setIsHighlyCompensated(mEmployee401kDTO.isHighlyCompensatedEmployee());
        }

        mEmployee.setThirdParty401kInfo(ee401kInfo);
        mEmployee.setPhone(mEmployee401kDTO.getPhoneNumber());
        mEmployee.setEmail(mEmployee401kDTO.getEmail());
    
        return processResult;
    }
}