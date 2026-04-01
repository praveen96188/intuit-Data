/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/EmployeeDTO.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.EmployeeStatus;
import com.intuit.sbd.payroll.psp.domain.Gender;
import com.intuit.sbd.payroll.psp.domain.PayrollFrequencyCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcela Villani
 */
public class EmployeeDTO {
    private EmployeeDTOValidator validator = new EmployeeDTODDValidator();

    // temporary variable used to track id swapping
    private String mOriginalSourceId = null;
    public String getOriginalSourceId() {
        return mOriginalSourceId;
    }

    public void setOriginalSourceId(String pOriginalSourceId) {
        mOriginalSourceId = pOriginalSourceId;
    }

    //this guid will almost always be null; the only time it will have a value is when we need to swap employee ids
    protected String existingEmployeeGuid;
    protected String employeeId;
    protected String firstName;
    protected String lastName;
    protected String middleName;
    protected String socialSecurityNumber;
    protected String suffix;
    protected AddressDTO liveAddress;
    protected String workState;
    protected String liveState;
    protected DateDTO hireDate;
    protected DateDTO birthDate;
    protected DateDTO terminationDate;
    protected String fedFilingStatus;
    protected int fedAllowances;
    protected SpcfMoney fedExtraWithholding;
    protected SpcfMoney fedClaimDependents;
    protected SpcfMoney fedOtherIncome;
    protected SpcfMoney fedDeduction;
    protected boolean fedMultipleJobs;
    protected  String fedW4EmployeePref;
    protected boolean hasRetirementPlan=false;
    protected boolean hasThirdPartySickPay=false;
    protected boolean isStatutory=false;
    protected List<WagePlanDTO> wagePlanDTOs;
    protected ThirdParty401kEmployeeInfoDTO employee401kInfo;
    protected boolean isDeceased;
    protected boolean qualifiesForAEIC;
    protected PayrollFrequencyCode payPeriod;
    protected EmployeeStatus mStatusCd;
    protected Gender mGender;
    protected String mPhoneNumber;
    protected String mEmail;
    private boolean isAccrualOnlyMod;
    protected String isSeasonal;

    protected List<EmployeePayrollItemDTO> employeePayrollItemDTOs;
    protected List<EmployeeTaxDTO> employeeTaxDTOs;
    protected List<EmployeeAccrualDTO> employeeAccrualDTOs;
    protected List<EmployeeCustomFieldDTO> employeeCustomFields;
    protected QBDTEmployeeInfoDTO QBDTEmployeeInfoDTO;
    protected List<EmployeeBankAccountDTO> employeeBankAccountDTOs;

    public EmployeeDTOValidator getValidator() {
        return validator;
    }

    public void setValidator(EmployeeDTOValidator validator) {
        this.validator = validator;
    }

    public String getExistingEmployeeGuid() {
        return existingEmployeeGuid;
    }

    public void setExistingEmployeeGuid(String existingEmployeeGuid) {
        this.existingEmployeeGuid = existingEmployeeGuid;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String pEmployeeId) {
        if(employeeId != null && !ObjectUtils.equals(employeeId, pEmployeeId)) {
            setOriginalSourceId(employeeId);
        }
        this.employeeId = pEmployeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String pFirstName) {
        this.firstName = pFirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String pLastName) {
        this.lastName = pLastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String pMiddleName) {
        this.middleName = pMiddleName;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String pSocialSecurityNumber) {
        this.socialSecurityNumber = pSocialSecurityNumber;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String pSuffix) {
        this.suffix = pSuffix;
    }

    public AddressDTO getLiveAddress() {
        return liveAddress;
    }

    public void setLiveAddress(AddressDTO liveAddress) {
        this.liveAddress = liveAddress;
    }

    public String getWorkState() {
        return workState;
    }

    public void setWorkState(String workState) {
        this.workState = workState;
    }

    public String getLiveState() {
        return liveState;
    }

    public void setLiveState(String pLiveState) {
        liveState = pLiveState;
    }

    public DateDTO getHireDate() {
        return hireDate;
    }

    public void setHireDate(DateDTO hireDate) {
        this.hireDate = hireDate;
    }

    public String getFedFilingStatus() {
        return fedFilingStatus;
    }

    public void setFedFilingStatus(String fedFilingStatus) {
        this.fedFilingStatus = fedFilingStatus;
    }

    public int getFedAllowances() {
        return fedAllowances;
    }

    public void setFedAllowances(int fedAllowances) {
        this.fedAllowances = fedAllowances;
    }

    public SpcfMoney getFedExtraWithholding() {
        return fedExtraWithholding;
    }

    public void setFedExtraWithholding(SpcfMoney pFedExtraWithholding) {
        fedExtraWithholding = pFedExtraWithholding;
    }

    public SpcfMoney getFedClaimDependents() { return fedClaimDependents; }

    public void setFedClaimDependents(SpcfMoney fedClaimDependents) { this.fedClaimDependents = fedClaimDependents; }

    public SpcfMoney getFedOtherIncome() { return fedOtherIncome; }

    public void setFedOtherIncome(SpcfMoney fedOtherIncome) { this.fedOtherIncome = fedOtherIncome; }

    public SpcfMoney getFedDeduction() { return fedDeduction; }

    public void setFedDeduction(SpcfMoney fedDeduction) { this.fedDeduction = fedDeduction; }

    public boolean isFedMultipleJobs() { return fedMultipleJobs; }

    public void setFedMultipleJobs(boolean fedMultipleJobs) { this.fedMultipleJobs = fedMultipleJobs; }

    public String getFedW4EmployeePref() { return fedW4EmployeePref; }

    public void setFedW4EmployeePref(String fedW4EmployeePref) { this.fedW4EmployeePref = fedW4EmployeePref; }

    public boolean getHasRetirementPlan() {
        return hasRetirementPlan;
    }

    public void setHasRetirementPlan(boolean hasRetirementPlan) {
        this.hasRetirementPlan = hasRetirementPlan;
    }

    public boolean getHasThirdPartySickPay() {
        return hasThirdPartySickPay;
    }

    public void setHasThirdPartySickPay(boolean hasThirdPartySickPay) {
        this.hasThirdPartySickPay = hasThirdPartySickPay;
    }

    public boolean getIsStatutory() {
        return isStatutory;
    }

    public void setStatutory(boolean statutory) {
        isStatutory = statutory;
    }

    public List<WagePlanDTO> getWagePlanDTOs() {
        if(wagePlanDTOs == null) {
            wagePlanDTOs = new ArrayList<WagePlanDTO>();
        }
        return wagePlanDTOs;
    }

    public void setWagePlanDTOs(List<WagePlanDTO> pWagePlanDTOs) {
        wagePlanDTOs = pWagePlanDTOs;
    }

    public ThirdParty401kEmployeeInfoDTO getEmployee401kInfo() {
        return employee401kInfo;
    }

    public void setEmployee401kInfo(ThirdParty401kEmployeeInfoDTO employee401kInfo) {
        this.employee401kInfo = employee401kInfo;
    }

    public DateDTO getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(DateDTO birthDate) {
        this.birthDate = birthDate;
    }

    public DateDTO getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(DateDTO terminationDate) {
        this.terminationDate = terminationDate;
    }


    public boolean isDeceased() {
        return isDeceased;
    }

    public void setDeceased(boolean pDeceased) {
        isDeceased = pDeceased;
    }

    public boolean qualifiesForAEIC() {
        return qualifiesForAEIC;
    }

    public void setQualifiesForAEIC(boolean pQualifiesForAEIC) {
        qualifiesForAEIC = pQualifiesForAEIC;
    }

    public PayrollFrequencyCode getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(PayrollFrequencyCode pPayPeriod) {
        payPeriod = pPayPeriod;
    }

    public EmployeeStatus getStatusCd() {
        return mStatusCd;
    }

    public void setStatusCd(EmployeeStatus pStatusCd) {
        mStatusCd = pStatusCd;
    }

    public Gender getGender() {
        return mGender;
    }

    public void setGender(Gender pGender) {
        mGender = pGender;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String pPhoneNumber) {
        mPhoneNumber = pPhoneNumber;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String pEmail) {
        mEmail = pEmail;
    }

    public List<EmployeePayrollItemDTO> getEmployeePayrollItemDTOs() {
        if(employeePayrollItemDTOs == null) {
            employeePayrollItemDTOs = new ArrayList<EmployeePayrollItemDTO>();
        }
        return employeePayrollItemDTOs;
    }

    public void setEmployeePayrollItemDTOs(List<EmployeePayrollItemDTO> pEmployeePayrollItemDTOs) {
        employeePayrollItemDTOs = pEmployeePayrollItemDTOs;
    }

    public List<EmployeeTaxDTO> getEmployeeTaxDTOs() {
        if(employeeTaxDTOs == null) {
            employeeTaxDTOs = new ArrayList<EmployeeTaxDTO>();
        }
        return employeeTaxDTOs;
    }

    public void setEmployeeTaxDTOs(List<EmployeeTaxDTO> pEmployeeTaxDTOs) {
        employeeTaxDTOs = pEmployeeTaxDTOs;
    }

    public List<EmployeeAccrualDTO> getEmployeeAccrualDTOs() {
        if(employeeAccrualDTOs == null) {
            employeeAccrualDTOs = new ArrayList<EmployeeAccrualDTO>();
        }
        return employeeAccrualDTOs;
    }

    public void setEmployeeAccrualDTOs(List<EmployeeAccrualDTO> pEmployeeAccrualDTOs) {
        employeeAccrualDTOs = pEmployeeAccrualDTOs;
    }

    public List<EmployeeCustomFieldDTO> getEmployeeCustomFields() {
        if(employeeCustomFields == null) {
            employeeCustomFields = new ArrayList<EmployeeCustomFieldDTO>();
        }
        return employeeCustomFields;
    }

    public void setEmployeeCustomFields(List<EmployeeCustomFieldDTO> pEmployeeCustomFields) {
        employeeCustomFields = pEmployeeCustomFields;
    }

    public QBDTEmployeeInfoDTO getQBDTEmployeeInfoDTO() {
        return QBDTEmployeeInfoDTO;
    }

    public void setQBDTEmployeeInfoDTO(QBDTEmployeeInfoDTO pQBDTEmployeeInfoDTO) {
        QBDTEmployeeInfoDTO = pQBDTEmployeeInfoDTO;
    }

    public List<EmployeeBankAccountDTO> getEmployeeBankAccountDTOs() {
        return employeeBankAccountDTOs;
    }

    public void setEmployeeBankAccountDTOs(List<EmployeeBankAccountDTO> pEmployeeBankAccountDTOs) {
        employeeBankAccountDTOs = pEmployeeBankAccountDTOs;
    }

    public ProcessResult validate() {
        return validator.validate(this);
    }

    /**
     * Derived field -- not passed in from an external source
     * @return
     */
    public String getFullName() {
        String fullName =  getFirstName() == null || getFirstName().trim().length() == 0 ? "" : getFirstName().trim();

        if (getMiddleName() != null && getMiddleName().trim().length() > 0) {
            if (!fullName.isEmpty()) {
                fullName += " ";
            }
            fullName += getMiddleName().trim();
        }

        if (getLastName() != null && getLastName().trim().length() > 0) {
            if (!fullName.isEmpty()) {
                fullName += " ";
            }
            fullName += getLastName().trim();
        }

        return fullName;
    }


    public boolean isAccrualOnlyMod() {
        return isAccrualOnlyMod;
    }

    public void setAccrualOnlyMod(boolean pAccrualOnlyMod) {
        isAccrualOnlyMod = pAccrualOnlyMod;
    }

    public String getIsSeasonal() {
        return isSeasonal;
    }

    public void setIsSeasonal(String isSeasonal) {
        this.isSeasonal = isSeasonal;
    }
}
