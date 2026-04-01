/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Core process for adding a new employee.
 *
 * @author Sean Barenz
 */
public class AddEmployeeCore extends Process implements IProcess {
    private Company company;

    private EmployeeDTO employeeDTO;
    private Employee employee;

    private String sourceCompanyId;
    private SourceSystemCode sourceSystemCd;

    private AddEmployee401k addEmployee401kProcess;
    private String transmissionId;
    private boolean ignoreDuplicates;

    /**
     * Constructor for AddEmployeeCore
     *
     * @param pSourceSystemCd  Source System Code
     * @param pSourceCompanyId Source Company ID
     * @param pEmployeeDTO     Employee data transfer object to add
     */
    public AddEmployeeCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, EmployeeDTO pEmployeeDTO, String pTransmissionId, boolean pIgnoreDuplicates) {
        employeeDTO = pEmployeeDTO;
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;
        transmissionId = pTransmissionId;
        ignoreDuplicates = pIgnoreDuplicates;
    }

    /**
     * Obtains the employee attached to the core process
     *
     * @return Employee domain object
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * Saves the employee to the database. The system will first check if the
     * employee already exists. If the employee does exist, that employee will
     * be updated to active, otherwise, the employee is inserted into the
     * database. The system assumes the <code>validate</code> method was
     * already executed
     */
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        // Employee doesn't exist, instantiate a new object and set the company.
        // Otherwise it is assumed that we have a fully realized object ready to
        // be reactivated, and then all needs done is to set the fields that
        // were passed in from the DTO
        //We'll only update the employee info with the dto info if the employee does not already exist with an active status,
        //so just return if the employee is already active
        if (employee == null) {
            employee = new Employee();
            employee.setCompany(company);
        } else if (employee.getStatusCd().equals(EmployeeStatus.Active)) {
            processResult.setResult(employee);
            return processResult;
        }

        //We'll only update the employee info with the dto info if the employee does not already exist with an active status
        if(employeeDTO.getFirstName() != null){
            employee.setFirstName(employeeDTO.getFirstName().trim());
        }
        if(employeeDTO.getLastName() != null){
            employee.setLastName(employeeDTO.getLastName().trim());
        }        
        if ((employeeDTO.getMiddleName() != null) && !employeeDTO.getMiddleName().equals("")) {
            employee.setMiddleName(employeeDTO.getMiddleName().trim());
        }
        employee.setSourceEmployeeId(employeeDTO.getEmployeeId().trim());
        employee.setTaxId(employeeDTO.getSocialSecurityNumber());
        
        if (employeeDTO.getStatusCd()==null) {
            employee.setStatusCd(EmployeeStatus.Active);
        } else {
            employee.setStatusCd(employeeDTO.getStatusCd());
        }

        employee.setStatusEffectiveDate(PSPDate.getPSPTime());
        employee.setSuffix(employeeDTO.getSuffix());

        employee.setWorkState(employeeDTO.getWorkState());
        employee.setLiveState(employeeDTO.getLiveState());
        employee.setHireDate(DateDTO.convertToSpcfCalendar(employeeDTO.getHireDate()));
        employee.setFedFilingStatus(employeeDTO.getFedFilingStatus());
        employee.setFedAllowances(employeeDTO.getFedAllowances());
        employee.setFedExtraWithholding(employeeDTO.getFedExtraWithholding());
        employee.setFedClaimDependents(employeeDTO.getFedClaimDependents());
        employee.setFedOtherIncome(employeeDTO.getFedOtherIncome());
        employee.setFedDeductions(employeeDTO.getFedDeduction());
        employee.setFedMultipleJobs(employeeDTO.isFedMultipleJobs());
        employee.setFedW4EmployeePref(employeeDTO.getFedW4EmployeePref());

        if (employeeDTO.getLiveAddress() != null) {
            employee.setMailingAddress(createDomainAddressFromDTO(employeeDTO.getLiveAddress()));
        }

        employee.setHasRetirementPlan(employeeDTO.getHasRetirementPlan());
        employee.setHasThirdPartySickPay(employeeDTO.getHasRetirementPlan());
        employee.setIsStatutory(employeeDTO.getIsStatutory());

        if (employeeDTO.getBirthDate() != null) {
            employee.setBirthDate(DateDTO.convertToSpcfCalendar(employeeDTO.getBirthDate()));
        }

        if (employeeDTO.getTerminationDate()!= null) {
            employee.setTerminationDate(DateDTO.convertToSpcfCalendar(employeeDTO.getTerminationDate()));
        }

        employee.setEmail(employeeDTO.getEmail());
        employee.setPhone(employeeDTO.getPhoneNumber());
        employee.setGenderCd(employeeDTO.getGender());
        employee.setIsDeceased(employeeDTO.isDeceased());
        employee.setPayPeriod(employeeDTO.getPayPeriod());
        employee.setQualifiesForAeic(employeeDTO.qualifiesForAEIC());

        // Save the employee
        employee = Application.save(employee);

        QBDTEmployeeInfoDTO qbdtEmployeeInfoDTO = employeeDTO.getQBDTEmployeeInfoDTO();
        if(qbdtEmployeeInfoDTO != null) {
            QbdtEmployeeInfo qbdtEmployeeInfo = new QbdtEmployeeInfo();
            qbdtEmployeeInfo.setListId(qbdtEmployeeInfoDTO.getListId());
            qbdtEmployeeInfo.setAltPhone(qbdtEmployeeInfoDTO.getAltPhone());
            qbdtEmployeeInfo.setBillPayAccount(qbdtEmployeeInfoDTO.getBillPayAccount());
            qbdtEmployeeInfo.setEmployeeType(qbdtEmployeeInfoDTO.getQBDTEmployeeType());
            qbdtEmployeeInfo.setEnforceSubjectTo(qbdtEmployeeInfoDTO.isEnforceSubjectTo());
            qbdtEmployeeInfo.setInitials(qbdtEmployeeInfoDTO.getInitials());
            qbdtEmployeeInfo.setIsDeleted(qbdtEmployeeInfoDTO.isDeleted());
            qbdtEmployeeInfo.setPrintAsName(qbdtEmployeeInfoDTO.getPrintAsName());
            qbdtEmployeeInfo.setTitle(qbdtEmployeeInfoDTO.getTitle());
            qbdtEmployeeInfo.setTrackingClass(qbdtEmployeeInfoDTO.getTrackingClass());
            qbdtEmployeeInfo.setUseDD(qbdtEmployeeInfoDTO.isUseDD());
            qbdtEmployeeInfo.setUseTime(qbdtEmployeeInfoDTO.isUseTime());
            qbdtEmployeeInfo.setEmployee(employee);
            qbdtEmployeeInfo.setCompany(employee.getCompany());
            qbdtEmployeeInfo.setIsAssisted(qbdtEmployeeInfoDTO.getIsAssisted());
            qbdtEmployeeInfo.setEmployeeSeasonal(qbdtEmployeeInfoDTO.isSeasonal());
            Application.save(qbdtEmployeeInfo);
            employee.setQbdtEmployeeInfo(qbdtEmployeeInfo);
        }

        for (WagePlanDTO wagePlanDTO : employeeDTO.getWagePlanDTOs()) {                            
            EmployeeWagePlan wagePlan = new EmployeeWagePlan();
            wagePlan.setName(wagePlanDTO.getName());
            wagePlan.setState(wagePlanDTO.getState());
            wagePlan.setWagePlanDomain(wagePlanDTO.getDomainCode());
            wagePlan.setWagePlanValue(wagePlanDTO.getWagePlanValue());
            wagePlan.setDescription(wagePlanDTO.getDescription());
            wagePlan.setRulesVersion(wagePlanDTO.getRulesVersion());            
            wagePlan.setEmployee(employee);
            wagePlan = Application.save(wagePlan);
            employee.getEmployeeWagePlanCollection().add(wagePlan);
        }

        for (EmployeeAccrualDTO employeeAccrualDTO : employeeDTO.getEmployeeAccrualDTOs()) {
            EmployeeAccrual employeeAccrual = new EmployeeAccrual();
            employeeAccrual.setAccrualPeriod(employeeAccrualDTO.getAccrualPeriod());
            employeeAccrual.setAccrualType(employeeAccrualDTO.getAccrualType());
            employeeAccrual.setEmployee(employee);
            employeeAccrual.setHours(employeeAccrualDTO.getHours());
            employeeAccrual.setHoursPerPeriod(employeeAccrualDTO.getHoursPerPeriod());
            employeeAccrual.setMaxHours(employeeAccrualDTO.getMaxHours());
            employeeAccrual.setNewYearReset(employeeAccrualDTO.isNewYearReset());
            employeeAccrual = Application.save(employeeAccrual);
            employee.getEmployeeAccrualCollection().add(employeeAccrual);
        }

        for (EmployeePayrollItemDTO employeePayrollItemDTO : employeeDTO.getEmployeePayrollItemDTOs()) {
            EmployeePayrollItem employeePayrollItem = new EmployeePayrollItem();
            employeePayrollItem.setAmount(employeePayrollItemDTO.getAmount());
            employeePayrollItem.setAmountType(employeePayrollItemDTO.getAmountType());
            employeePayrollItem.setItemOrder(employeePayrollItemDTO.getOrder());
            employeePayrollItem.setCompanyPayrollItem(CompanyPayrollItem.findItemForSourcePayrollItemId(company, employeePayrollItemDTO.getPayrollItemId()));
            employeePayrollItem.setEmployee(employee);
            employeePayrollItem.setItemLimit(employeePayrollItemDTO.getItemLimit());
            employeePayrollItem.setLimitType(employeePayrollItemDTO.getLimitType());
            employeePayrollItem.setType(employeePayrollItemDTO.getPaylineType());
            employeePayrollItem = Application.save(employeePayrollItem);
            employee.getEmployeePayrollItemCollection().add(employeePayrollItem);
        }

        for (EmployeeTaxDTO employeeTaxDTO : employeeDTO.getEmployeeTaxDTOs()) {
            EmployeeTax employeeTax = new EmployeeTax();
            employeeTax.setAllowances(employeeTaxDTO.getAllowances());
            employeeTax.setCompanyLaw(CompanyLaw.findCompanyLawBySourceId(company, employeeTaxDTO.getCompanyLawId()));
            employeeTax.setEmployee(employee);
            employeeTax.setExtraWithholding(employeeTaxDTO.getExtraWithholding());
            employeeTax.setExtraWithholdingType(employeeTaxDTO.getExtraWithholdingType());
            employeeTax.setFilingStatus(employeeTaxDTO.getFilingStatus());
            employeeTax.setState(employeeTaxDTO.getState());
            employeeTax.setSubjectTo(employeeTaxDTO.isSubjectTo());
            employeeTax.setTaxLawVersion(employeeTaxDTO.getTaxLawVersion());
            employeeTax.setTaxType(employeeTaxDTO.getTaxType());
            employeeTax.setW2Name(employeeTaxDTO.getW2Name());
            employeeTax.setTaxOrder(employeeTaxDTO.getOrder());
            employeeTax = Application.save(employeeTax);

            for (Integer order : employeeTaxDTO.getTaxTableMiscData().keySet()) {
                TaxTableMiscData taxTableMiscData = new TaxTableMiscData();
                taxTableMiscData.setMiscDataOrder(order);
                taxTableMiscData.setValue(employeeTaxDTO.getTaxTableMiscData().get(order));
                taxTableMiscData.setEmployeeTax(employeeTax);
                taxTableMiscData.setCompany(company);
                taxTableMiscData = Application.save(taxTableMiscData);
                employeeTax.getTaxTableMiscDataCollection().add(taxTableMiscData);
            }

            employee.getEmployeeTaxCollection().add(employeeTax);
        }

        if(employeeDTO.getEmployeeCustomFields() != null) {
            for (EmployeeCustomFieldDTO employeeCustomFieldDTO : employeeDTO.getEmployeeCustomFields()) {
                EmployeeCustomField employeeCustomField = new EmployeeCustomField();
                employeeCustomField.setName(employeeCustomFieldDTO.getName());
                employeeCustomField.setValue(employeeCustomFieldDTO.getValue());
                employeeCustomField.setFieldOrder(employeeCustomFieldDTO.getOrder());
                employeeCustomField.setEmployee(employee);
                employeeCustomField = Application.save(employeeCustomField);
                employee.getEmployeeCustomFieldCollection().add(employeeCustomField);
            }
        }

        if(company.isCompanyOnActiveService(ServiceCode.ViewMyPaycheck)){
            DomainEntitySet<VmpEmployeeInfo> vmpEmployeesInfoSet = VmpEmployeeInfo.findVmpEmployeesInfo(company);
            if(vmpEmployeesInfoSet.isNotEmpty() && employeeDTO.getQBDTEmployeeInfoDTO() != null && employeeDTO.getQBDTEmployeeInfoDTO().getListId() != null){
                List<VmpEmployeeInfo> vmpEmployeeInfos = vmpEmployeesInfoSet.stream()
                        .filter(vmpEmployee -> employeeDTO.getQBDTEmployeeInfoDTO().getListId().startsWith(vmpEmployee.getEmployeeRecnum()))
                        .collect(Collectors.toList());
                if(vmpEmployeeInfos != null && vmpEmployeeInfos.size() == 1){
                    VmpEmployeeInfo vmpEmployeeInfo = vmpEmployeeInfos.get(0);
                    employee.setPersonaId(vmpEmployeeInfo.getPersonaId());
                    employee.setEmail(vmpEmployeeInfo.getEmail());
                    if(vmpEmployeeInfo.getConsumerRealmId() != null) {
                        employee.setConsumerRealmId(vmpEmployeeInfo.getConsumerRealmId());
                    }
                    Application.delete(vmpEmployeeInfo);
                }
            }
        }

        for (CompanyService companyService : company.getCompanyServiceCollection()) {
            if (companyService.getService().getServiceCd() == ServiceCode.ThirdParty401k
                    && employeeDTO.getEmployee401kInfo() != null) {
                addEmployee401kProcess.setEmployee(employee);
                ProcessResult<Employee> add401kEeProcResult = addEmployee401kProcess.process();
                processResult.merge(add401kEeProcResult);
                employee = addEmployee401kProcess.getEmployee();
            }
        }

        if (addEmployee401kProcess == null) {
            employee.setThirdParty401kInfo(null);
        }

        if(employeeDTO.getEmployeeBankAccountDTOs() != null) {
            for (EmployeeBankAccountDTO employeeBankAccountDTO : employeeDTO.getEmployeeBankAccountDTOs()) {
                AddEmployeeBankAccountCore addEmployeeBankAccountCore = new AddEmployeeBankAccountCore(sourceSystemCd, sourceCompanyId, employee, employeeBankAccountDTO);
                processResult.merge(addEmployeeBankAccountCore.validate());
                if(processResult.isSuccess()) {
                    ProcessResult<EmployeeBankAccount> addEmployeeBankAccountPR = addEmployeeBankAccountCore.process();
                    processResult.merge(addEmployeeBankAccountPR);
                    EmployeeBankAccount employeeBankAccount = addEmployeeBankAccountPR.getResult();
                    if(employeeBankAccount != null) {
                        employee.addEmployeeBankAccount(employeeBankAccount);
                    }
                }
            }
        }

        processResult.setResult(employee);
        if (processResult.isSuccess()) {
            employee.cache();
            if(company.isCompanyRequiredForOFACScreening()) {
                CompanyEvent.createChangeEmployeeCompanyEvent(company, EventTypeCode.EmployeeAdded, employee.getId().toString());
            }
        }

        return processResult;
    }

    /**
     * Validation stage of the <b>AddEmployee</b> process flow. <p/> 1.
     * Validate the Employee object is not null. <br/> 2. Validate the Company
     * exists <br/> 3. Validate the Company is active <br/> 4. Is
     * Employee/IndividualBE active <br/> 5. Validate if the Employee exists
     * <br/> 6. If Employee exists, is it active <br/>
     *
     * @return ProcessResult Empty if successful, otherwise, will contain error
     *         messages
     */
    public ProcessResult validate() {
        // Validate inputs from DTO
        ProcessResult validationResult = Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }
        // Validate Employee DTO
        if (employeeDTO == null) {
            validationResult.getMessages().EmployeeNotSpecified(EntityName.Employee, "EmployeeDTO");
            return validationResult;
        }

        // Validate Company Exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                                                               sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        validationResult = employeeDTO.validate();

        ServiceCode serviceCode = null;
        if(company.isCompanyOnService(ServiceCode.Tax) && !company.isMigratingToAssisted()) {
            serviceCode = ServiceCode.Tax;
        } else if(company.isCompanyOnService(ServiceCode.DirectDeposit)){
            serviceCode = ServiceCode.DirectDeposit;
        }

        if (!company.isCloudOnly() && !company.isAllowedCapability(SystemCapabilityCode.ChangeCompanyInfo, serviceCode)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
            createPayrollRejectEvents(validationResult);
        }

        if (!company.passesAdditionalCancelTermValidation(false, true, true, true)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
        }

        if (employeeDTO.getEmployee401kInfo() != null) {
            // TODO: throw an exception if caller has not set validator to be 401k validator?
            addEmployee401kProcess = new AddEmployee401k(sourceSystemCd, sourceCompanyId, employeeDTO.getEmployee401kInfo(), validationResult);
            validationResult=addEmployee401kProcess.validate();
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if(!ignoreDuplicates){
        // Validate Employee Does Not Exist
        employee = Employee.findEmployee(company, employeeDTO.getEmployeeId());
        if (employee != null) {
            if (EmployeeStatus.Active == employee.getStatusCd()) {
                validationResult.getMessages().EmployeeAlreadyExists(EntityName.Employee,
                                                                     employeeDTO.getEmployeeId(), sourceSystemCd.toString(), sourceCompanyId,
                                                                     employeeDTO.getEmployeeId());
                createPayrollRejectEvents(validationResult);
                return validationResult;
            }
        }
        }

        for (EmployeePayrollItemDTO employeePayrollItemDTO : employeeDTO.getEmployeePayrollItemDTOs()) {
            if(CompanyPayrollItem.findItemForSourcePayrollItemId(company, employeePayrollItemDTO.getPayrollItemId()) == null) {
                validationResult.getMessages().PayrollItemDoesNotExist(EntityName.PayrollItem,
                                                                       employeePayrollItemDTO.getPayrollItemId(),
                                                                       sourceSystemCd.toString(),
                                                                       sourceCompanyId,
                                                                       employeePayrollItemDTO.getPayrollItemId());
                return validationResult;
            }
        }

        if(employeeDTO.getEmployeeBankAccountDTOs() != null) {
            for (EmployeeBankAccountDTO employeeBankAccountDTO : employeeDTO.getEmployeeBankAccountDTOs()) {
                validationResult.merge(employeeBankAccountDTO.validateEmployeeBankAccount());
            }
        }
        return validationResult;
    }

    private void createPayrollRejectEvents(ProcessResult pValidationResult) {

        if (pValidationResult == null || pValidationResult.getErrorMessages().size() == 0)
            return;        

        final ProcessResult finalValidationResult = pValidationResult;
        PayrollServices.executeTransactionThread(new TransactionThread() {
            public ProcessResult transaction() {
                Company localCompany = Application.findById(Company.class, company.getId());
                for (Message message : finalValidationResult.getMessages()) {
                    if (message.getLevel().equals(MessageInfo.MessageLevel.ERROR)) {
                        CompanyEvent.createPayrollRejectEvent(localCompany, null, null, message.getMessageCode() + ":" + message.getMessage(),
                                                              null, null);
                    }
                }
                return new ProcessResult();
            }
        });
    }

    private Address createDomainAddressFromDTO(AddressDTO pAddressDTO) {
        Address domainAddress = new Address();
        domainAddress.setAddressLine1(pAddressDTO.getAddressLine1());
        domainAddress.setAddressLine2(pAddressDTO.getAddressLine2());
        domainAddress.setAddressLine3(pAddressDTO.getAddressLine3());
        domainAddress.setCity(pAddressDTO.getCity());
        domainAddress.setCountry(pAddressDTO.getCountry());
        domainAddress.setState(pAddressDTO.getState());
        domainAddress.setZipCode(pAddressDTO.getZipCode());
        domainAddress.setZipCodeExtension(pAddressDTO.getZipCodeExtension());
        return Application.save(domainAddress);
    }
}
