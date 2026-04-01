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
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.processes.wallet.WalletCreateCore;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Core process for updating a existing employee.
 *
 * @author Sean Barenz
 */
public class UpdateEmployeeCore extends Process implements IProcess {
    private static final Logger logger = LoggerFactory.getLogger(UpdateEmployeeCore.class);

    private Company company;

    private EmployeeDTO employeeDTO;
    private Employee employee;

    private String sourceCompanyId;
    private SourceSystemCode sourceSystemCd;

    private UpdateEmployee401k updateEmployee401kProcess;
    private String transmissionId;

    /**
     * Default constructor for update employee core
     *
     * @param pSourceSystemCd  Source System Code
     * @param pSourceCompanyId Source Company Id
     * @param pEmployeeDTO     Employee DTO
     */
    public UpdateEmployeeCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                              EmployeeDTO pEmployeeDTO, String pTransmissionId) {
        employeeDTO = pEmployeeDTO;
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;
        transmissionId = pTransmissionId;
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
     * Updates the employee in the database. The system will first check if the
     * employee already exists.
     */
    public ProcessResult process() {

        ProcessResult processResult = new ProcessResult();
        String eventTypeCheck="N";
        boolean createEmployeeUpdateEvent=false;
        if(employee.getStatusCd()==EmployeeStatus.Inactive && (employeeDTO.getStatusCd()!=null && employeeDTO.getStatusCd()==EmployeeStatus.Active)) {
            eventTypeCheck="A";
        }
        else if(employee.getStatusCd()==EmployeeStatus.Active && (employeeDTO.getStatusCd()!=null && employeeDTO.getStatusCd()==EmployeeStatus.Inactive)) {
            eventTypeCheck="D";
        }
        else {
            eventTypeCheck="U";
            createEmployeeUpdateEvent=isCreateEmployeeUpdateEvent(employee, employeeDTO);
        }
        // Update the employee information from incoming 'DTO' (note these are
        // the only allowed incoming fields)
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setMiddleName(employeeDTO.getMiddleName());
        employee.setSourceEmployeeId(employeeDTO.getEmployeeId());
        employee.setTaxId(employeeDTO.getSocialSecurityNumber());
        employee.setSuffix(employeeDTO.getSuffix());
        
        employee.setWorkState(employeeDTO.getWorkState());
        employee.setLiveState(employeeDTO.getLiveState());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPhone(employeeDTO.getPhoneNumber());
        employee.setPayPeriod(employeeDTO.getPayPeriod());
        employee.setGenderCd(employeeDTO.getGender());
        employee.setIsDeceased(employeeDTO.isDeceased());
        employee.setQualifiesForAeic(employeeDTO.qualifiesForAEIC());

        if(employeeDTO.getStatusCd() != null && employee.getStatusCd() != employeeDTO.getStatusCd()) {
            employee.setStatusCd(employeeDTO.getStatusCd());
            employee.setStatusEffectiveDate(PSPDate.getPSPTime());
        }

        QbdtEmployeeInfo qbdtEmployeeInfo = null;
        if(employee.getQbdtEmployeeInfo() == null && employeeDTO.getQBDTEmployeeInfoDTO() != null) {
            qbdtEmployeeInfo = new QbdtEmployeeInfo();
            employee.setQbdtEmployeeInfo(qbdtEmployeeInfo);
            qbdtEmployeeInfo.setEmployee(employee);
            qbdtEmployeeInfo.setCompany(employee.getCompany());
        } else if(employee.getQbdtEmployeeInfo() != null && employeeDTO.getQBDTEmployeeInfoDTO() == null) {
            employee.setQbdtEmployeeInfo(null);
        } else {
            qbdtEmployeeInfo = employee.getQbdtEmployeeInfo();
        }

        if(qbdtEmployeeInfo != null) {
            QBDTEmployeeInfoDTO qbdtEmployeeInfoDTO = employeeDTO.getQBDTEmployeeInfoDTO();
            if(qbdtEmployeeInfoDTO.getListId() != null) {
                qbdtEmployeeInfo.setListId(qbdtEmployeeInfoDTO.getListId());
            }
            qbdtEmployeeInfo.setAltPhone(qbdtEmployeeInfoDTO.getAltPhone());
            qbdtEmployeeInfo.setBillPayAccount(qbdtEmployeeInfoDTO.getBillPayAccount());
            qbdtEmployeeInfo.setEmployeeType(qbdtEmployeeInfoDTO.getQBDTEmployeeType());
            qbdtEmployeeInfo.setEnforceSubjectTo(qbdtEmployeeInfoDTO.isEnforceSubjectTo());
            qbdtEmployeeInfo.setInitials(qbdtEmployeeInfoDTO.getInitials());
            if(qbdtEmployeeInfo.getIsDeleted()!=qbdtEmployeeInfoDTO.isDeleted() && qbdtEmployeeInfoDTO.isDeleted()==true) {
                eventTypeCheck="D";
            }
            qbdtEmployeeInfo.setIsDeleted(qbdtEmployeeInfoDTO.isDeleted());
            qbdtEmployeeInfo.setPrintAsName(qbdtEmployeeInfoDTO.getPrintAsName());
            qbdtEmployeeInfo.setTitle(qbdtEmployeeInfoDTO.getTitle());
            qbdtEmployeeInfo.setTrackingClass(qbdtEmployeeInfoDTO.getTrackingClass());
            qbdtEmployeeInfo.setUseDD(qbdtEmployeeInfoDTO.isUseDD());
            qbdtEmployeeInfo.setUseTime(qbdtEmployeeInfoDTO.isUseTime());
            qbdtEmployeeInfo.setIsAssisted(qbdtEmployeeInfoDTO.getIsAssisted());
            qbdtEmployeeInfo.setEmployeeSeasonal(qbdtEmployeeInfoDTO.isSeasonal());
            Application.save(qbdtEmployeeInfo);
        }

        SpcfCalendar hireDate = null;
        if (employeeDTO.getHireDate() != null) {
            hireDate = DateDTO.convertToSpcfCalendar(employeeDTO.getHireDate()).toUtc();
        }
        employee.setHireDate(hireDate);

        employee.setFedFilingStatus(employeeDTO.getFedFilingStatus());
        employee.setFedAllowances(employeeDTO.getFedAllowances());
        employee.setFedExtraWithholding(employeeDTO.getFedExtraWithholding());
        employee.setFedClaimDependents(employeeDTO.getFedClaimDependents());
        employee.setFedOtherIncome(employeeDTO.getFedOtherIncome());
        employee.setFedDeductions(employeeDTO.getFedDeduction());
        employee.setFedMultipleJobs(employeeDTO.isFedMultipleJobs());
        employee.setFedW4EmployeePref(employeeDTO.getFedW4EmployeePref());

        Address mailingAddress = null;
        if (employeeDTO.getLiveAddress() != null) {
            mailingAddress = createDomainAddressFromDTO(employeeDTO.getLiveAddress(), employee.getMailingAddress());
        }
        employee.setMailingAddress(mailingAddress);


        employee.setHasRetirementPlan(employeeDTO.getHasRetirementPlan());
        employee.setHasThirdPartySickPay(employeeDTO.getHasRetirementPlan());
        employee.setIsStatutory(employeeDTO.getIsStatutory());

        SpcfCalendar birthDate = null;
        if (employeeDTO.getBirthDate() != null)
            birthDate = DateDTO.convertToSpcfCalendar(employeeDTO.getBirthDate()).toUtc();
        employee.setBirthDate(birthDate);

        SpcfCalendar terminationDate = null;
        if (employeeDTO.getTerminationDate()!=null) {
            terminationDate = DateDTO.convertToSpcfCalendar(employeeDTO.getTerminationDate()).toUtc();
        }
        employee.setTerminationDate(terminationDate);

        DomainEntitySet<EmployeeWagePlan> currentWagePlans = new  DomainEntitySet<EmployeeWagePlan>(employee.getEmployeeWagePlanCollection());

        // find wage plans that already exist and remove them from both the DTO list and the wage plan list- no change necessary
        for (Iterator<WagePlanDTO> iterator = employeeDTO.getWagePlanDTOs().iterator(); iterator.hasNext() && currentWagePlans.size() > 0; ) {
            WagePlanDTO wagePlanDTO = iterator.next();

            if (wagePlanDTO.getState()!=null) {
                Criterion<EmployeeWagePlan> employeeWagePlanCriterion = EmployeeWagePlan.State().equalTo(wagePlanDTO.getState());

                if(wagePlanDTO.getName() != null) {
                    employeeWagePlanCriterion = employeeWagePlanCriterion.And(EmployeeWagePlan.Name().equalTo(wagePlanDTO.getName()));
                } else {
                    employeeWagePlanCriterion = employeeWagePlanCriterion.And(EmployeeWagePlan.Name().isNull());
                }

                if(wagePlanDTO.getWagePlanValue() != null) {
                    employeeWagePlanCriterion = employeeWagePlanCriterion.And(EmployeeWagePlan.WagePlanValue().equalTo(wagePlanDTO.getWagePlanValue()));
                } else {
                    employeeWagePlanCriterion = employeeWagePlanCriterion.And(EmployeeWagePlan.WagePlanValue().isNull());
                }

                if(wagePlanDTO.getDomainCode() != null) {
                    employeeWagePlanCriterion = employeeWagePlanCriterion.And(EmployeeWagePlan.WagePlanDomain().equalTo(wagePlanDTO.getDomainCode()));
                } else {
                    employeeWagePlanCriterion = employeeWagePlanCriterion.And(EmployeeWagePlan.WagePlanDomain().isNull());
                }

                if(wagePlanDTO.getDescription() != null) {
                    employeeWagePlanCriterion = employeeWagePlanCriterion.And(EmployeeWagePlan.Description().equalTo(wagePlanDTO.getDescription()));
                } else {
                    employeeWagePlanCriterion = employeeWagePlanCriterion.And(EmployeeWagePlan.Description().isNull());
                }

                if(wagePlanDTO.getRulesVersion() != null) {
                    employeeWagePlanCriterion = employeeWagePlanCriterion.And(EmployeeWagePlan.RulesVersion().equalTo(wagePlanDTO.getRulesVersion()));
                } else {
                    employeeWagePlanCriterion = employeeWagePlanCriterion.And(EmployeeWagePlan.RulesVersion().isNull());
                }

                DomainEntitySet<EmployeeWagePlan> matchingEmployeeWagePlans = currentWagePlans.find(employeeWagePlanCriterion);
                if(matchingEmployeeWagePlans.size() > 0) {
                    currentWagePlans.remove(matchingEmployeeWagePlans.getFirst());
                    iterator.remove();
                }
            }
        }

        // invalidate any current employee wage plans that do not match
        for (EmployeeWagePlan currentWagePlan : currentWagePlans) {
            currentWagePlan.setInvalidDate(PSPDate.getPSPTime());
            Application.save(currentWagePlan);
        }

        //Create new wage plans from the remaining DTOs- these are wage plans that do not currently exist
        for (WagePlanDTO wagePlanDTO : employeeDTO.getWagePlanDTOs()) {
            EmployeeWagePlan wagePlan = new EmployeeWagePlan();
            wagePlan.setDescription(wagePlanDTO.getDescription());
            wagePlan.setName(wagePlanDTO.getName());
            wagePlan.setRulesVersion(wagePlanDTO.getRulesVersion());
            wagePlan.setState(wagePlanDTO.getState());
            wagePlan.setWagePlanDomain(wagePlanDTO.getDomainCode());
            wagePlan.setWagePlanValue(wagePlanDTO.getWagePlanValue());
            wagePlan.setEmployee(employee);
            employee.getEmployeeWagePlanCollection().add(wagePlan);
            Application.save(wagePlan);
        }

        if(employee.isCreatedInCurrentSession()) {
            for(EmployeePayrollItem employeePayrollItem : employee.getEmployeePayrollItemCollection()){
                Application.delete(employeePayrollItem);
            }
        } else {
            String payrollItemDelete = " Delete from com.intuit.sbd.payroll.psp.domain.EmployeePayrollItem epi" +
                    " where epi.Employee = :employee";
            org.hibernate.Query query = Application.createHibernateQuery(payrollItemDelete);
            query.setParameter("employee", employee);
            query.executeUpdate();
        }

        for (EmployeePayrollItemDTO employeePayrollItemDTO : employeeDTO.getEmployeePayrollItemDTOs()) {
            EmployeePayrollItem employeePayrollItem = new EmployeePayrollItem();
            employeePayrollItem.setAmount(employeePayrollItemDTO.getAmount());
            employeePayrollItem.setAmountType(employeePayrollItemDTO.getAmountType());
            employeePayrollItem.setItemOrder(employeePayrollItemDTO.getOrder());
            employeePayrollItem.setCompanyPayrollItem(CompanyPayrollItem.findItemForSourcePayrollItemId(company, employeePayrollItemDTO.getPayrollItemId()));
            employeePayrollItem.setItemLimit(employeePayrollItemDTO.getItemLimit());
            employeePayrollItem.setLimitType(employeePayrollItemDTO.getLimitType());
            employeePayrollItem.setType(employeePayrollItemDTO.getPaylineType());
            employeePayrollItem.setEmployee(employee);
            Application.save(employeePayrollItem);
        }

        if(employee.isCreatedInCurrentSession()) {
            for(EmployeeTax employeePayrollItem : employee.getEmployeeTaxCollection()){
                for (TaxTableMiscData taxTableMiscData : employeePayrollItem.getTaxTableMiscDataCollection()) {
                    Application.delete(taxTableMiscData);
                }
                Application.delete(employeePayrollItem);
            }
        } else {
            String employeeTaxMiscDelete = " Delete from com.intuit.sbd.payroll.psp.domain.TaxTableMiscData misc" +
                    " where misc.Company = :company" +
                    " and misc.EmployeeTax in " +
                    " (select et " +
                    "  from com.intuit.sbd.payroll.psp.domain.EmployeeTax et " +
                    "  where et.Employee = :employee )";
            org.hibernate.Query query = Application.createHibernateQuery(employeeTaxMiscDelete);
            query.setParameter("employee", employee);
            query.setParameter("company", company);
            query.executeUpdate();

            String employeeTaxDelete = " Delete from com.intuit.sbd.payroll.psp.domain.EmployeeTax et" +
                    " where et.Employee = :employee";
            query = Application.createHibernateQuery(employeeTaxDelete);
            query.setParameter("employee", employee);
            query.executeUpdate();
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
        }

        if(employee.isCreatedInCurrentSession()) {
            for(EmployeeCustomField employeeCustomField : employee.getEmployeeCustomFieldCollection()){
                Application.delete(employeeCustomField);
            }
        } else {
            String customFieldDelete = " Delete from com.intuit.sbd.payroll.psp.domain.EmployeeCustomField ecf" +
                    " where ecf.Employee = :employee";
            org.hibernate.Query query = Application.createHibernateQuery(customFieldDelete);
            query.setParameter("employee", employee);
            query.executeUpdate();
        }

        if(employeeDTO.getEmployeeCustomFields() != null) {
            for (EmployeeCustomFieldDTO employeeCustomFieldDTO : employeeDTO.getEmployeeCustomFields()) {
                EmployeeCustomField employeeCustomField = new EmployeeCustomField();
                employeeCustomField.setName(employeeCustomFieldDTO.getName());
                employeeCustomField.setValue(employeeCustomFieldDTO.getValue());
                employeeCustomField.setFieldOrder(employeeCustomFieldDTO.getOrder());
                employeeCustomField.setEmployee(employee);
                Application.save(employeeCustomField);
            }
        }

        if(employeeDTO.getEmployeeAccrualDTOs() != null && employeeDTO.getEmployeeAccrualDTOs().size() > 0) {
            for (EmployeeAccrualDTO employeeAccrualDTO : employeeDTO.getEmployeeAccrualDTOs()) {
                EmployeeAccrual employeeAccrual = employee.getEmployeeAccrualCollection().findEntity(EmployeeAccrual.AccrualType().equalTo(employeeAccrualDTO.getAccrualType()));
                if(employeeAccrual == null) {
                    employeeAccrual = new EmployeeAccrual();
                    employeeAccrual.setAccrualType(employeeAccrualDTO.getAccrualType());
                    employeeAccrual.setEmployee(employee);
                    employeeAccrual = Application.save(employeeAccrual);
                    employee.getEmployeeAccrualCollection().add(employeeAccrual);
                }
                employeeAccrual.setAccrualPeriod(employeeAccrualDTO.getAccrualPeriod());
                employeeAccrual.setHours(employeeAccrualDTO.getHours());
                employeeAccrual.setHoursPerPeriod(employeeAccrualDTO.getHoursPerPeriod());
                employeeAccrual.setMaxHours(employeeAccrualDTO.getMaxHours());
                employeeAccrual.setNewYearReset(employeeAccrualDTO.isNewYearReset());
                Application.save(employeeAccrual);
            }
        }

        // For DIY DD Employees, EMPMOD OFX tag doesnt contain the details of bank accounts
        // So it is not possible to create wallet for existing bank accounts of Employee which doesnt have wallet
        // Wallet Creation for Existing Bank Accounts has been handled by OneTime but few can be left due to corner cases
        // 1. Inactive -> Active Employee, Company
        // 2. RealmId Added scenarios
        // This code will handle these situations
        String realmId = employee.getCompany().getIAMRealmId();
        String psId = employee.getCompany().getSourceCompanyId();
        if (!StringUtil.isNullOrEmpty(realmId)) {
            if (FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_WALLET_CREATION_EXISTING_BA, true)) {
                if (employeeDTO.getEmployeeBankAccountDTOs() == null) {
                    List<EmployeeBankAccount> employeeBankAccountWithoutWallet = EmployeeBankAccount.getActiveBankAccountsForWalletIdCriteria(employee, true);
                    if (Objects.nonNull(employeeBankAccountWithoutWallet) && !employeeBankAccountWithoutWallet.isEmpty()) {
                        for (EmployeeBankAccount employeeBankAccount : employeeBankAccountWithoutWallet) {
                            try {
                                ProcessResult createWalletResult = new WalletCreateCore(employeeBankAccount).execute();
                            } catch (Exception e) {
                                logger.error("Wallet Creation Exception EmployeeId={} Realm={} PSID={}", employee.getId(), realmId, psId, e);
                            }
                        }
                    }
                }
            } else {
                logger.info("Wallet Creation Existing BA Feature Flag is disabled Realm={} PSID={}", realmId, psId);
            }
        }

        if(employeeDTO.getEmployeeBankAccountDTOs() != null && employeeDTO.getEmployeeBankAccountDTOs().size() > 0) {
            DomainEntitySet<EmployeeBankAccount> activeEmployeeBankAccounts =
                    employee.getEmployeeBankAccountCollection().find(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active));
            // add or update accounts
            for (EmployeeBankAccountDTO employeeBankAccountDTO : employeeDTO.getEmployeeBankAccountDTOs()) {
                EmployeeBankAccount employeeBankAccount =
                        activeEmployeeBankAccounts.findEntity(EmployeeBankAccount.SourceBankAccountId().equalTo(employeeBankAccountDTO.getEmployeeBankAccountId()));
                if(employeeBankAccount != null) {
                    activeEmployeeBankAccounts.remove(employeeBankAccount);
                    UpdateEmployeeBankAccountCore updateEmployeeBankAccountCore = new UpdateEmployeeBankAccountCore(sourceSystemCd, sourceCompanyId, employee, employeeBankAccountDTO);
                    processResult.merge(updateEmployeeBankAccountCore.validate());
                    if(processResult.isSuccess()) {
                        processResult.merge(updateEmployeeBankAccountCore.process());
                    }
                } else {
                    AddEmployeeBankAccountCore addEmployeeBankAccountCore = new AddEmployeeBankAccountCore(sourceSystemCd, sourceCompanyId, employee, employeeBankAccountDTO);
                    processResult.merge(addEmployeeBankAccountCore.validate());
                    if(processResult.isSuccess()) {
                        ProcessResult<EmployeeBankAccount> addEmployeeBankAccountPR = addEmployeeBankAccountCore.process();
                        processResult.merge(addEmployeeBankAccountPR);
                        if(addEmployeeBankAccountPR.getResult() != null) {
                            employee.addEmployeeBankAccount(addEmployeeBankAccountPR.getResult());
                        }
                    }
                }
            }

            EmployeeBankAccount.deactivateActiveEmployeeBankAccounts(employee, activeEmployeeBankAccounts, true);
        } else if(employee.canBeRecoveredByQB()) {
            // deactivate all of the accounts
            for (EmployeeBankAccount activeEmployeeBankAccount : employee.getEmployeeBankAccountCollection().find(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active))) {
                activeEmployeeBankAccount.expireEmployeeBankAccount();
            }
        }

        if (updateEmployee401kProcess !=null) {
            updateEmployee401kProcess.setEmployee(employee);
            ProcessResult<Employee> upd401kEeProcResult = updateEmployee401kProcess.process();
            processResult.merge(upd401kEeProcResult);
            employee=updateEmployee401kProcess.getEmployee();
        }

        // Save the information to the database (note that because of the way
        // Hibernate works, we must save
        // via the topmost parent record, which in this case is going to be the
        // company
        employee = Application.save(employee);
        employee.cache();
        
        processResult.setResult(employee);
        if(company.isCompanyRequiredForOFACScreening()) {
            if (eventTypeCheck.equals("A")) {
                CompanyEvent.createChangeEmployeeCompanyEvent(company, EventTypeCode.EmployeeAdded, employee.getId().toString());
            } else if (eventTypeCheck.equals("D")) {
                CompanyEvent.createChangeEmployeeCompanyEvent(company, EventTypeCode.EmployeeDeleted, employee.getId().toString());
            } else if (eventTypeCheck.equals("U") && createEmployeeUpdateEvent) {
                CompanyEvent.createChangeEmployeeCompanyEvent(company, EventTypeCode.EmployeeUpdated, employee.getId().toString());
            }
        }
        return processResult;


    }

    /**
     * Validation stage of the <b>UpdateEmployee</b> process flow. <p/> 1.
     * Validate the Employee object is not null. <br/> 2. Validate the Company
     * exists <br/> 3. Validate the Company is active <br/> 4. Is
     * Employee/IndividualBE active <br/> 5. Validate if the Employee exists
     * <br/> 6. If Employee exists, is it active <br/>
     *
     * @return ProcessResult Empty if successful, otherwise, will contain error
     *         messages
     */
    public ProcessResult validate() {
        //  Validate inputs from DTO
        ProcessResult validationResult = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // 	Validate Employee DTO
        if (employeeDTO == null) {
            validationResult.getMessages().EmployeeNotSpecified(EntityName.Employee, "EmployeeDTO");
            return validationResult;
        }

        // Validate Company Active
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (!company.isCloudOnly() && !company.isAllowedCapability(SystemCapabilityCode.ChangeCompanyInfo)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
        }

        if (!company.passesAdditionalCancelTermValidation(false, true, true, true)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
        }


        ProcessResult employeeValidationResult = employeeDTO.validate();
        if (employeeDTO.getEmployee401kInfo()!=null) {
            updateEmployee401kProcess = new UpdateEmployee401k(sourceSystemCd, sourceCompanyId, employeeDTO.getEmployee401kInfo());
            employeeValidationResult.merge(updateEmployee401kProcess.validate());
        }

        // Validate Employee Exists
        if (employeeDTO.getExistingEmployeeGuid()!=null && employeeDTO.getExistingEmployeeGuid().trim().length()!=0) {
            employee = Application.findById(Employee.class, SpcfUniqueId.createInstance(employeeDTO.getExistingEmployeeGuid()));
        } else if(employeeDTO.getQBDTEmployeeInfoDTO() != null && employeeDTO.getQBDTEmployeeInfoDTO().getListId() != null){
            employee = Employee.findEmployeeByQBListId(company, employeeDTO.getQBDTEmployeeInfoDTO().getListId());
        }
        if(employee == null){
            employee = Employee.findEmployee(company, employeeDTO.getEmployeeId());
        }
        if (employee == null) {
            validationResult.getMessages().EmployeeDoesNotExist(EntityName.Employee,
                    employeeDTO.getEmployeeId(), sourceSystemCd.toString(), sourceCompanyId,
                    employeeDTO.getEmployeeId());
            createPayrollRejectEvents(validationResult);
            return validationResult;
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

        for (EmployeeAccrualDTO employeeAccrualDTO : employeeDTO.getEmployeeAccrualDTOs()) {
            if(employeeAccrualDTO.getAccrualType() == null) {
                validationResult.getMessages().InvalidArgument(EntityName.Employee, "null", "AccrualType");
                return validationResult;
            }
        }

        if(employeeDTO.getEmployeeBankAccountDTOs() != null) {
            for (EmployeeBankAccountDTO employeeBankAccountDTO : employeeDTO.getEmployeeBankAccountDTOs()) {
                validationResult.merge(employeeBankAccountDTO.validateEmployeeBankAccount());
            }
        }

        if (!employeeValidationResult.isSuccess()) {
            return employeeValidationResult;
        }

        createPayrollRejectEvents(validationResult);
        validationResult.merge(employeeValidationResult);
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

    private Address createDomainAddressFromDTO(AddressDTO pAddressDTO, Address pDomainAddress) {
        if(pDomainAddress == null){
            pDomainAddress = new Address();
        }
        pDomainAddress.setAddressLine1(pAddressDTO.getAddressLine1());
        pDomainAddress.setAddressLine2(pAddressDTO.getAddressLine2());
        pDomainAddress.setAddressLine3(pAddressDTO.getAddressLine3());
        pDomainAddress.setCity(pAddressDTO.getCity());
        pDomainAddress.setCountry(pAddressDTO.getCountry());
        pDomainAddress.setState(pAddressDTO.getState());
        pDomainAddress.setZipCode(pAddressDTO.getZipCode());
        pDomainAddress.setZipCodeExtension(pAddressDTO.getZipCodeExtension());
        return Application.save(pDomainAddress);
    }

    private boolean isCreateEmployeeUpdateEvent(Employee pEmployee, EmployeeDTO pEmployeeDTO) {
        if(!isSameString(pEmployee.getFirstName(), pEmployeeDTO.getFirstName()) || !isSameString(pEmployee.getLastName(), pEmployeeDTO.getLastName()) || !isSameString(pEmployee.getMiddleName(), pEmployeeDTO.getMiddleName())) {
            return true;
        }
        if (pEmployeeDTO.getBirthDate() == null) {
            if(pEmployee.getBirthDate() !=null) {
                return true;
            }
        }
        else if (pEmployee.getBirthDate()==null || !CalendarUtils.compareSpcfCalendarDate(pEmployee.getBirthDate(), DateDTO.convertToSpcfCalendar(pEmployeeDTO.getBirthDate()).toUtc())) {
            return true;
        }
        if (pEmployeeDTO.getLiveAddress() == null) {
            if(pEmployee.getMailingAddress() !=null) {
                return true;
            }
        }
        else if (pEmployee.getMailingAddress()==null || !isSameAddress(pEmployeeDTO.getLiveAddress(), pEmployee.getMailingAddress())) {
            return true;
        }
        return false;
    }

    private boolean isSameAddress(AddressDTO pLiveAddress, Address pMailingAddress) {
        return (isSameString(pLiveAddress.getAddressLine1(), pMailingAddress.getAddressLine1())
                && isSameString(pLiveAddress.getAddressLine2(), pMailingAddress.getAddressLine2())
                && isSameString(pLiveAddress.getAddressLine3(), pMailingAddress.getAddressLine3())
                && isSameString(pLiveAddress.getCity(), pMailingAddress.getCity())
                && isSameString(pLiveAddress.getState(), pMailingAddress.getState())
                && isSameString(pLiveAddress.getCountry(), pMailingAddress.getCountry())
                && isSameString(pLiveAddress.getZipCode(), pMailingAddress.getZipCode())
                && isSameString(pLiveAddress.getZipCodeExtension(), pMailingAddress.getZipCodeExtension()));
    }    

    private boolean isSameString(String str1, String str2) {
        if(str1==null && str2==null) {
            return true;
        }
        else if(str1==null || str2==null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }
}
