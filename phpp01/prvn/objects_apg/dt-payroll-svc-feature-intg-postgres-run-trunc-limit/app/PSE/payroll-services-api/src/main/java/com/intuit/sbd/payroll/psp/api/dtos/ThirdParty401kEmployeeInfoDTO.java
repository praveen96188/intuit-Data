/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/ThirdParty401kEmployeeInfoDTO.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.Validator;

import java.math.BigDecimal;

/**
 * @author Dawn Martens
 */
public class ThirdParty401kEmployeeInfoDTO {
    private String email;
    private String phoneNumber;
    private Boolean isHighlyCompensatedEmployee;
    private Boolean isFamilyMember;
    private BigDecimal ownershipPercent;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean isHighlyCompensatedEmployee() {
        return isHighlyCompensatedEmployee;
    }

    public void setHighlyCompensatedEmployee(Boolean highlyCompensatedEmployee) {
        isHighlyCompensatedEmployee = highlyCompensatedEmployee;
    }

    public Boolean isFamilyMember() {
        return isFamilyMember;
    }

    public void setFamilyMember(Boolean familyMember) {
        isFamilyMember = familyMember;
    }

    public BigDecimal getOwnershipPercent() {
        return ownershipPercent;
    }

    public void setOwnershipPercent(BigDecimal ownershipPercent) {
        this.ownershipPercent = ownershipPercent;
    }

    private ThirdParty401kEmployeeInfoDTOCoreValidator validator = new ThirdParty401kEmployeeInfoDTOCoreValidator();

    public ThirdParty401kEmployeeInfoDTOCoreValidator getValidator() {
        return validator;
    }

    public void setValidator(ThirdParty401kEmployeeInfoDTOCoreValidator validator) {
        this.validator = validator;
    }

    public ProcessResult validate(EmployeeDTO employeeDTO) {
        return validator.validate(employeeDTO, this);
    }
}