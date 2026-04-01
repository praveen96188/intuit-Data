package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeBankAccountFraud;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeComplianceData;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeInfo;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeLineItemPaycheck;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeLineItemQuarter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeLineItemYear;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeePaycheckCollection;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeTaxabilityInfo;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLineItemValue;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaycheck;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPropertyAudit;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSearchResults;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.TaxPeriod;
import com.intuit.sbd.payroll.psp.adapters.sap.lcds.proxy.PSPEntityProxy;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.WagePlanDTO;
import com.intuit.sbd.payroll.psp.context.aspect.CompanyIdentifierType;
import com.intuit.sbd.payroll.psp.context.aspect.TenantId;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyLaw;
import com.intuit.sbd.payroll.psp.domain.CompanyPayrollItem;
import com.intuit.sbd.payroll.psp.domain.Compensation;
import com.intuit.sbd.payroll.psp.domain.Deduction;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.EmployeeTax;
import com.intuit.sbd.payroll.psp.domain.EmployeeWagePlan;
import com.intuit.sbd.payroll.psp.domain.EmployerContribution;
import com.intuit.sbd.payroll.psp.domain.LiabilityAdjustment;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PaycheckSplit;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.PropertyAudit;
import com.intuit.sbd.payroll.psp.domain.QbdtEmployeeInfo;
import com.intuit.sbd.payroll.psp.domain.QbdtEmployeeSeasonal;
import com.intuit.sbd.payroll.psp.domain.QbdtPayrollTransaction;
import com.intuit.sbd.payroll.psp.domain.QbdtPayrollTransactionLine;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.Tax;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import flex.messaging.io.PropertyProxyRegistry;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: dcrossley
 * Date: May 21, 2009
 * Time: 12:48:15 PM
 */
public class EmployeeAdapter {
    private static final SpcfLogger logger = PayrollServices.getLogger(CompanyAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    public EmployeeAdapter() {
        registerProxies();
    }

    private void registerProxies() {
        PSPEntityProxy entityProxy = new PSPEntityProxy();
        PropertyProxyRegistry.getRegistry().register(SAPPaycheck.class, entityProxy);
    }

    @FlexMethod
    public ArrayList<SAPPaycheck> getEmployeePaychecks(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pEmployeeId) throws Throwable {
        ArrayList<SAPPaycheck> sapPaycheckList = new ArrayList<SAPPaycheck>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            Employee employee = Employee.findEmployee(company, pEmployeeId);

            DomainEntitySet<Paycheck> paycheckList = Paycheck.findPaychecksBySourceEmployee(company, employee);

            for (Paycheck paycheck : paycheckList) {
                sapPaycheckList.add(PayrollRunTranslator.getSAPPaycheckFromDomainEntity(paycheck, paycheck.isTOKPaycheck()));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving paychecks for employee", pSourceSystemCd, pCompanyId, "Employee", pEmployeeId, t);
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapPaycheckList;
    }

    @FlexMethod
    public ArrayList<SAPPropertyAudit> getEmployeeHistory(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemId,
            String pEmployeeGseq) throws Throwable {
        ArrayList<SAPPropertyAudit> sapPropertyAudits = new ArrayList<SAPPropertyAudit>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemId));

            DomainEntitySet<PropertyAudit> employeeAudits =
                    PayrollServices.entityFinder.find(PropertyAudit.class,
                            PropertyAudit.Company().equalTo(company)
                                    .And(PropertyAudit.ObjectIdentifier().equalTo(pEmployeeGseq))
                                    .And(PropertyAudit.ClassName().equalTo("Employee"))
                                    .And(PropertyAudit.PropertyName().equalTo(Employee.HireDate().getPropertyName())
                                        .Or(PropertyAudit.PropertyName().equalTo("BirthDate"))
                                        .Or(PropertyAudit.PropertyName().equalTo("TaxId"))
                                        .Or(PropertyAudit.PropertyName().equalTo(Employee.BirthDateEnc().getPropertyName()))
                                        .Or(PropertyAudit.PropertyName().equalTo(Employee.TaxIdEnc().getPropertyName()))
                                        .Or(PropertyAudit.PropertyName().equalTo(Employee.SourceEmployeeId().getPropertyName()))
                                        .Or(PropertyAudit.PropertyName().equalTo(Employee.TerminationDate().getPropertyName()))));

            for(PropertyAudit propertyAudit : employeeAudits) {
                sapPropertyAudits.add(PropertyAuditTranslator.getSAPPropertyAuditFromDomainEntity(propertyAudit));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding employee property audit history", pSourceSystemId, pCompanyId, "Employee", pEmployeeGseq, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapPropertyAudits;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewSignupFraudQueue)
    public ArrayList<SAPEmployeeBankAccountFraud> checkEmployeeBankAccountFraud(String sourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String companyId,String payrollRunId) throws Throwable {
        ArrayList<SAPEmployeeBankAccountFraud> sapEmployeeBankAccountFraud = new ArrayList<SAPEmployeeBankAccountFraud>();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try{
            Company company = Company.findCompany(
                    companyId,
                    SourceSystemCode.valueOf(sourceSystemCd));

            PayrollRun payrollRun = PayrollRun.findPayrollRun(company,payrollRunId);
            if (payrollRun == null) {
                return sapEmployeeBankAccountFraud;
            }

            DomainEntitySet<Paycheck> paychecks = payrollRun.getPaycheckCollection();

             //create map of employees' bank accounts to associated paychecks
            SortedMap<String, ArrayList<Paycheck>> employeePaycheckBankAccounts = new TreeMap<String, ArrayList<Paycheck>>();
            SortedMap<String, String> employeeBankAccountIds = new TreeMap<String,String>();

            for(Paycheck paycheck : paychecks){
                for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()){
                    String bankAccountNumber = paycheckSplit.getEmployeeBankAccount().getBankAccount().getAccountNumber();
                    if(!employeePaycheckBankAccounts.containsKey(bankAccountNumber)) {
                        ArrayList<Paycheck> paycheckArrayList = new ArrayList<Paycheck>();
                        paycheckArrayList.add(paycheck);
                        employeePaycheckBankAccounts.put(bankAccountNumber, paycheckArrayList);
                        employeeBankAccountIds.put(bankAccountNumber,paycheckSplit.getEmployeeBankAccount().getSourceBankAccountId());
                    }
                    else{
                        ArrayList<Paycheck> paycheckArrayList = employeePaycheckBankAccounts.get(bankAccountNumber);
                        boolean sameEmployee = false;
                        //don't add paycheck if same employee
                        for(Paycheck payCheck : paycheckArrayList ){
                            Employee employee = paycheck.getDDEmployee();
                            if(employee.equals(payCheck.getDDEmployee())){
                                sameEmployee = true;
                                break;
                            }
                        }
                        if(!sameEmployee){
                            paycheckArrayList.add(paycheck);
                        }
                    }
                }
            }

            //find any bank accounts associated with multiple paychecks for different employees
            SortedMap<String, ArrayList<Paycheck>> employeeMatchingBankAccounts = new TreeMap<String, ArrayList<Paycheck>>();
            findMatchingBankAccounts(employeeMatchingBankAccounts, employeePaycheckBankAccounts);

            if (employeeMatchingBankAccounts.size() > 0){
                AuthUser foundUser = AuthUser.findUser(Application.getCurrentPrincipal().getId());
                boolean canViewEEPII = foundUser.hasOperation(OperationId.ViewEEPII);

                for (String key : employeeMatchingBankAccounts.keySet()) {
                    ArrayList<Paycheck> paycheckArrayList = employeeMatchingBankAccounts.get(key);
                    String bankId = employeeBankAccountIds.get(key);
                    EmployeeBankAccount employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(paycheckArrayList.get(0).getDDEmployee(), bankId);
                    sapEmployeeBankAccountFraud.add(EmployeeTranslator.getSAPEmployeeBankAccountFraud(paycheckArrayList, employeeBankAccount, canViewEEPII));
                }
            }
        } catch(Throwable t) {
            aeFactory.throwGenericException("Error finding checking for duplicate bank accounts.", sourceSystemCd, companyId, "PayrollRun", payrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapEmployeeBankAccountFraud;
    }

    private void findMatchingBankAccounts(SortedMap<String, ArrayList<Paycheck>> employeeMatchingBankAccounts,SortedMap<String, ArrayList<Paycheck>> employeePaycheckBankAccounts){
        Set<String> set = employeePaycheckBankAccounts.keySet();

        for (String key : set) {
            ArrayList<Paycheck> paycheckArrayList = employeePaycheckBankAccounts.get(key);
            if(paycheckArrayList.size() > 1)
            {
                employeeMatchingBankAccounts.put(key, paycheckArrayList);
            }
        }
    }

    @FlexMethod
    public ArrayList<SAPPropertyAudit> getEmployeeProfileHistory(String sourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String companyId,String employeeId) throws Throwable {
        ArrayList<SAPPropertyAudit> sapPropertyAudits = new ArrayList<SAPPropertyAudit>();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            Company company = Company.findCompany(
                    companyId,
                    SourceSystemCode.valueOf(sourceSystemCd));

            Employee employee = PayrollServices.entityFinder.findById(Employee.class,
                    SpcfUniqueId.createInstance(employeeId));

            if(employee == null)
            {
                throw new Exception("Employee not Found with Id " + employeeId);
            }

            if(employee.getMailingAddress() != null)
            {
                String addressId = employee.getMailingAddress().getId().toString();

                DomainEntitySet<PropertyAudit> addressAudits =
                        PayrollServices.entityFinder.find(PropertyAudit.class,
                                PropertyAudit.Company().equalTo(company)
                                        .And(PropertyAudit.ObjectIdentifier().equalTo(addressId))
                                        .And(PropertyAudit.ClassName().equalTo("Address"))
                                        .And(PropertyAudit.PropertyName().equalTo("AddressLine1")
                                        .Or(PropertyAudit.PropertyName().equalTo("AddressLine2"))
                                        .Or(PropertyAudit.PropertyName().equalTo("AddressLine3"))
                                        .Or(PropertyAudit.PropertyName().equalTo("City"))
                                        .Or(PropertyAudit.PropertyName().equalTo("State"))
                                        .Or(PropertyAudit.PropertyName().equalTo("ZipCode"))));

                for(PropertyAudit propertyAudit : addressAudits)
                {
                    sapPropertyAudits.add(PropertyAuditTranslator.getSAPPropertyAuditFromDomainEntity(propertyAudit));
                }

            }

            DomainEntitySet<PropertyAudit> employeeAudits =
                    PayrollServices.entityFinder.find(PropertyAudit.class,
                            PropertyAudit.Company().equalTo(company)
                                    .And(PropertyAudit.ObjectIdentifier().equalTo(employeeId))
                                    .And(PropertyAudit.ClassName().equalTo("Employee"))
                                    .And(PropertyAudit.PropertyName().equalTo("HireDate")
                                        .Or(PropertyAudit.PropertyName().equalTo("ReHireDate"))
                                        .Or(PropertyAudit.PropertyName().equalTo("BirthDate"))
                                        .Or(PropertyAudit.PropertyName().equalTo("BirthDateEnc"))
                                        .Or(PropertyAudit.PropertyName().equalTo("WorkState"))
                                        .Or(PropertyAudit.PropertyName().equalTo("LiveState"))
                                        .Or(PropertyAudit.PropertyName().equalTo("StatusCd"))
                                        .Or(PropertyAudit.PropertyName().equalTo("TaxId"))
                                        .Or(PropertyAudit.PropertyName().equalTo("TaxIdEnc"))
                                        .Or(PropertyAudit.PropertyName().equalTo("SourceEmployeeId"))
                                        .Or(PropertyAudit.PropertyName().equalTo("FedFilingStatus"))
                                        .Or(PropertyAudit.PropertyName().equalTo("StateFilingStatus"))
                                        .Or(PropertyAudit.PropertyName().equalTo("FedAllowances"))
                                        .Or(PropertyAudit.PropertyName().equalTo("StateAllowances"))
                                        .Or(PropertyAudit.PropertyName().equalTo("TerminationDate"))));

            for(PropertyAudit propertyAudit : employeeAudits)
            {
                sapPropertyAudits.add(PropertyAuditTranslator.getSAPPropertyAuditFromDomainEntity(propertyAudit));
            }

        } catch(Throwable t) {
            aeFactory.throwGenericException("Error finding employee profile history.", sourceSystemCd, companyId, "Employee", employeeId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapPropertyAudits;
    }

    @FlexMethod
    public ArrayList<SAPEmployeeTaxabilityInfo> getEmployeeTaxabilityInfo(String sourceSystemCd, String companyId, String employeeId) throws Throwable {

        ArrayList<SAPEmployeeTaxabilityInfo> sapTaxabilityInfoList = new ArrayList<SAPEmployeeTaxabilityInfo>();
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

        try{
            Employee employee = PayrollServices.entityFinder.findById(Employee.class,
                                                                      SpcfUniqueId.createInstance(employeeId));
            for (EmployeeTax employeeTax : employee.getEmployeeTaxCollection()) {
                sapTaxabilityInfoList.add(EmployeeTranslator.getEmployeeTaxFromDomainEntity(employeeTax, employee));
            }
        } catch (Throwable t){
            aeFactory.throwGenericException("Error finding employee taxability information.", sourceSystemCd, companyId, "Employee", employeeId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapTaxabilityInfoList;
    }

    @FlexMethod
    public SAPEmployeeComplianceData getEmployeeComplianceData(String pWagePlanId) throws Throwable {

        SAPEmployeeComplianceData sapComplianceData = new SAPEmployeeComplianceData();
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

        try{
            EmployeeWagePlan employeeWagePlan = PayrollServices.entityFinder.findById(EmployeeWagePlan.class,
                                                                                      SpcfUniqueId.createInstance(pWagePlanId));
            sapComplianceData = EmployeeTranslator.getEmployeeComplianceFromDomainEntity(employeeWagePlan);
        } catch (Throwable t){
            aeFactory.throwGenericException("Error finding Employee compliance data", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapComplianceData;
    }

    @FlexMethod
    public ArrayList<SAPEmployeeComplianceData> getEmployeeComplianceDataList(String sourceSystemCd, String companyId, String employeeId) throws Throwable {

        ArrayList<SAPEmployeeComplianceData> sapComplianceInfoList = new ArrayList<SAPEmployeeComplianceData>();
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

        try{
            Employee employee = PayrollServices.entityFinder.findById(Employee.class,
                                                                      SpcfUniqueId.createInstance(employeeId));
            for (EmployeeWagePlan employeeWagePlan : employee.getEmployeeWagePlanCollection()) {
                if(employeeWagePlan.getInvalidDate() == null){
                    sapComplianceInfoList.add(EmployeeTranslator.getEmployeeComplianceFromDomainEntity(employeeWagePlan));
                }
            }
        } catch (Throwable t){
            aeFactory.throwGenericException("Error finding employee Compliance information.", sourceSystemCd, companyId, "Employee", employeeId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapComplianceInfoList;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.UpdateComplianceData)
    public ArrayList<SAPEmployeeComplianceData> updateEmployeeComplianceData(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pEmployeeId, SAPEmployeeComplianceData pComplianceData, String pOperation) throws Throwable {

        ArrayList<SAPEmployeeComplianceData> sapComplianceInfoList = new ArrayList<SAPEmployeeComplianceData>();
        PayrollServices.beginUnitOfWork();

        ArrayList<WagePlanDTO> wagePlanDTOArrayList = new ArrayList<WagePlanDTO>();
        ArrayList <ProcessResult> results = new ArrayList<ProcessResult>();

        try{
            Employee employee = PayrollServices.entityFinder.findById(Employee.class,
                                                                      SpcfUniqueId.createInstance(pEmployeeId));
            //need to remove by ID, so cannot use DTOs directly.  Instead, rebuilding DTOs after making changes to collection
            List<EmployeeWagePlan> employeeWagePlanList = new ArrayList<EmployeeWagePlan>(employee.getEmployeeWagePlanCollection());

            if(pOperation.equals("Edit") || pOperation.equals("Remove") ){
                employeeWagePlanList.remove(
                        Application.findById(EmployeeWagePlan.class, SpcfUniqueId.createInstance(pComplianceData.getId())));
            }

            for(EmployeeWagePlan employeeWagePlan : employeeWagePlanList){
                wagePlanDTOArrayList.add(PayrollServices.dtoFactory.create(employeeWagePlan));
            }

            if (pOperation.equals("Add") || pOperation.equals("Edit")){
                WagePlanDTO newWagePlanDTO = EmployeeTranslator.buildWagePlanDTOFromSAPComplianceData(pComplianceData);

                DomainEntitySet<EmployeeWagePlan> allWagePlans = employee.getEmployeeWagePlanCollection();

                if(isSameEmployeeWagePlanValue(allWagePlans,newWagePlanDTO)){
                    aeFactory.throwGenericException("Found matching wage plan value. Can't add same value again");
                }

                wagePlanDTOArrayList.add(newWagePlanDTO);
            }


            EmployeeDTO employeeDTO = PayrollServices.dtoFactory.create(employee);
            employeeDTO.setWagePlanDTOs(wagePlanDTOArrayList);

            results.add(PayrollServices.employeeManager.updateEmployee(SourceSystemCode.valueOf(pSourceSystemCd),
                                                                       pCompanyId,
                                                                       employeeDTO));

            for (ProcessResult result : results) {
                if(!result.isSuccess()){
                    aeFactory.throwGenericException("Error updating employee wage plan", result);
                }
            }

        } catch (Throwable t){
            aeFactory.throwGenericException("Error updating employee wage plan", pSourceSystemCd, pCompanyId, "Employee", pEmployeeId, t);
        } finally {
            PayrollServices.commitUnitOfWork();
        }
        return sapComplianceInfoList;
    }

    public boolean isSameEmployeeWagePlanValue(DomainEntitySet<EmployeeWagePlan> allWagePlans, WagePlanDTO newWagePlanDTO) {

        DomainEntitySet<EmployeeWagePlan> allValidWagePlan = new DomainEntitySet<>();

        for (EmployeeWagePlan employeeWagePlan : allWagePlans) {
            if(employeeWagePlan.getInvalidDate() == null){
                allValidWagePlan.add(employeeWagePlan);
            }
        }

        Criterion<EmployeeWagePlan> employeeWagePlanCriterion = EmployeeWagePlan.State().equalTo(newWagePlanDTO.getState())
                .And(EmployeeWagePlan.Name().equalTo(newWagePlanDTO.getName()))
                .And(EmployeeWagePlan.WagePlanValue().equalTo(newWagePlanDTO.getWagePlanValue()))
                .And(EmployeeWagePlan.WagePlanDomain().equalTo(newWagePlanDTO.getDomainCode()))
                .And(EmployeeWagePlan.Description().equalTo(newWagePlanDTO.getDescription()))
                .And(EmployeeWagePlan.RulesVersion().equalTo(newWagePlanDTO.getRulesVersion()));

        DomainEntitySet<EmployeeWagePlan> matchingEmployeeWagePlans = allValidWagePlan.find(employeeWagePlanCriterion);

        return matchingEmployeeWagePlans.size() > 0;
    }

    /*
        PSP-18296 Starts
    */
    @FlexMethod
    public void updateEmployeeSeasonal(String pSourceCompnay, String pEmployeeId, SAPEmployeeInfo pEmployeeInfo) throws Throwable{
        String modifierId = null;
        logger.debug("Begin update employee seasonal value ");
        try{
            PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();
            if(principal !=null){
                modifierId = principal.getName();
            }
            PayrollServices.beginUnitOfWork();

            Employee employee = PayrollServices.entityFinder.findById(Employee.class,
                    SpcfUniqueId.createInstance(pEmployeeInfo.getEmployeeGseq()));
            QbdtEmployeeInfo qbdtEmployeeInfo = null;

            qbdtEmployeeInfo = employee.getQbdtEmployeeInfo();

            switch (pEmployeeInfo.getIsSeasonal()){
                case "N" :
                    qbdtEmployeeInfo.setEmployeeSeasonal(QbdtEmployeeSeasonal.NONSEASONAL);
                    break;
                case "Y" :
                    qbdtEmployeeInfo.setEmployeeSeasonal(QbdtEmployeeSeasonal.SEASONAL);
                    break;
                default:
                    qbdtEmployeeInfo.setEmployeeSeasonal(QbdtEmployeeSeasonal.UNKNOWN);
            }

            qbdtEmployeeInfo.setModifiedDate(PSPDate.getPSPTime());
            qbdtEmployeeInfo.setModifierId(modifierId);

            Company company = employee.getCompany();

            logger.debug(" Current token : "+company.getCurrentToken()+" Qbdt emp token "+qbdtEmployeeInfo.getToken());

            Application.save(qbdtEmployeeInfo);

        }catch (Throwable t){
            aeFactory.throwGenericException("Error updating employe seasonal value ", "Employee", pEmployeeId, t);
        }finally {
            PayrollServices.commitUnitOfWork();
        }

    }

    /*
        PSP-18296 Ends
    */

    @FlexMethod
    public SAPSearchResults<SAPEmployeeInfo> findEmployeesByCriteria(String sourceSystemCd,
                                                                     @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
                                                              String pSourceEmployeeId,
                                                              String pSsn,
                                                              String pName,
                                                              String pLiveState,
                                                              String pWorkState,
                                                              int pFirstIndex, int pMaxResults, String pSortColumn, Boolean pSortDescending) throws Throwable {
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

        ArrayList<SAPEmployeeInfo> employeeSearchResult  = new ArrayList<SAPEmployeeInfo>();
        List<Employee> employees  = new ArrayList<Employee>();
        try {
            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(sourceSystemCd));
            TaxAdapter taxAdapter = new TaxAdapter();
            boolean hasNonNumericSourceIds = taxAdapter.hasNonNumericSourceIds(company.getSourceSystemCd().toString(), company.getSourceCompanyId());

            HqlBuilder hql = new HqlBuilder(true, "select distinct ee from com.intuit.sbd.payroll.psp.domain.Employee ee join fetch ee.QbdtEmployeeInfoSet qei where ee.Company = :company and qei.IsAssisted = 1 and qei.IsDeleted=false ");
            hql.setParameter("company", company);

            if(StringUtils.isNotEmpty(pSourceEmployeeId)){
                hql.append("and ee.SourceEmployeeId = :sourceEmpId");
                hql.setParameter("sourceEmpId", pSourceEmployeeId);
            }
            if(StringUtils.isNotEmpty(pSsn)){
                List<String> taxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Employee.TaxIdKeyName, pSsn);
                hql.append("and ee.TaxIdEnc in (:taxIdEncList)");
                hql.setParameter("taxIdEncList", taxIdEncList);
            }
            if(StringUtils.isNotEmpty(pName)){
                hql.append("and (lower(COALESCE(cast(ee.FirstName as java.lang.String),'')) like :name or lower(COALESCE(cast(ee.MiddleName as java.lang.String),'')) like :name or lower(COALESCE(cast(ee.LastName as java.lang.String),'')) like :name)");
                hql.setParameter("name", "%"+pName.toLowerCase()+"%");
            }
            if(StringUtils.isNotEmpty(pLiveState)){
                hql.append("and ee.LiveState = :livestate");
                hql.setParameter("livestate", pLiveState);
            }
            if(StringUtils.isNotEmpty(pWorkState)){
                hql.append("and ee.WorkState = :workstate");
                hql.setParameter("workstate", pWorkState);
            }

            String employeeIdOrderBy = hasNonNumericSourceIds ? "ee.SourceEmployeeId" : "cast (ee.SourceEmployeeId as int)";

            hql.append(" order by ");
            //Sorting on PII will not work post encryption. Applying the default sort when trying to sort by PII field
            if (StringUtils.isEmpty(pSortColumn) || StringUtils.equals(pSortColumn, "socialSecurityNumber")) {
                hql.append(employeeIdOrderBy).append(", ee.Id");
            } else {
                if (StringUtils.equals(pSortColumn, "employeeId")) {
                    hql.append(employeeIdOrderBy);
                } else if (StringUtils.equals(pSortColumn, "stateLive")) {
                    hql.append("ee.LiveState");
                } else if (StringUtils.equals(pSortColumn, "stateWork")) {
                    hql.append("ee.WorkState");
                } else if (StringUtils.equals(pSortColumn, "hireDate")) {
                    hql.append("ee.HireDate");
                } else if (StringUtils.equals(pSortColumn, "termDate")) {
                    hql.append("ee.TerminationDate");
                } else if (StringUtils.equals(pSortColumn, "fullName")) {
                    hql.append("ee.LastName");
                    if (pSortDescending) {
                        hql.append(" desc");
                    }
                    hql.append(", ee.FirstName");
                }
                if (pSortDescending) {
                    hql.append(" desc");
                }
                hql.append(", ee.Id");
            }
            employees = hql.list();
            AuthUser foundUser = AuthUser.findUser(Application.getCurrentPrincipal().getId());
            boolean canViewEEPII = foundUser.hasOperation(OperationId.ViewEEPII);

            for (Employee employee : employees.subList(pFirstIndex, Math.min(employees.size(), pFirstIndex + pMaxResults))) {
                SpcfCalendar lastPayDate = employee.getLastPayrollReceivedDate();
                employeeSearchResult.add(CompanyTranslator.getSAPEmployeeInfoFromDomainEntity(employee, lastPayDate, canViewEEPII));
            }
        } catch(Throwable t) {
            aeFactory.throwGenericException("Error finding Employees", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        Collections.sort(employeeSearchResult, (SAPEmployeeInfo a, SAPEmployeeInfo b) -> b.getSocialSecurityNumber().compareTo(a.getSocialSecurityNumber()));
        if(!pSortDescending) {
            Collections.reverse(employeeSearchResult);
        }
        return new SAPSearchResults <SAPEmployeeInfo> (employees.size(), employeeSearchResult);
    }

    @FlexMethod
    public SAPEmployeePaycheckCollection getEmployeeProfilePaycheckDetail(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pEmployeeId,
            Date paycheckFromDate,
            Date paycheckToDate) throws Throwable {

        SAPEmployeePaycheckCollection employeeProfileDetails = new SAPEmployeePaycheckCollection();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            Employee employee = Employee.findEmployee(company, pEmployeeId);
            DomainEntitySet<Paycheck> paychecks = Paycheck.findNonSupersededPaychecksByEmployee(company, employee, SAPTranslator.getSpcfCalendarFromDate_BeginDay(paycheckFromDate), SAPTranslator.getSpcfCalendarFromDate_EndDay(paycheckToDate));

            //suppressing warnings since the get is lazy
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") CompanyPayrollItemHashMap compensationTotal = new CompanyPayrollItemHashMap();
            compensationTotal.put("Paycheck Total", new SAPLineItemValue("compTotal", "Paycheck Total", "Paycheck Total", "", 0.));
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") CompanyPayrollItemHashMap preTaxDeductionTotal = new CompanyPayrollItemHashMap();
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") CompanyPayrollItemHashMap postTaxDeductionTotal = new CompanyPayrollItemHashMap();
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") CompanyPayrollItemHashMap directDepositTotal = new CompanyPayrollItemHashMap();
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") CompanyPayrollItemHashMap employerContributionTaxableTotal = new CompanyPayrollItemHashMap();
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") CompanyPayrollItemHashMap employerContributionNoTaxAffectTotal = new CompanyPayrollItemHashMap();
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") CompanyPayrollItemHashMap taxableAdditionTotal = new CompanyPayrollItemHashMap();
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") CompanyPayrollItemHashMap noTaxAffectAdditionTotal = new CompanyPayrollItemHashMap();
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") LawHashMap employeeTaxTotal = new LawHashMap();
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") LawHashMap employerTaxTotal = new LawHashMap();

            double netPayTotal = 0;

            ArrayList<SAPEmployeeLineItemPaycheck> paycheckDetails = new ArrayList<SAPEmployeeLineItemPaycheck>();

            for (Paycheck paycheck : paychecks) {
                double netPay;

                CompanyPayrollItemHashMap compensationMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap preTaxDeductionMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap postTaxDeductionMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap directDepositMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap employerContributionTaxableMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap employerContributionNoTaxAffectMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap taxableAdditionMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap noTaxAffectAdditionMap = new CompanyPayrollItemHashMap();
                LawHashMap employeeTaxMap = new LawHashMap();
                LawHashMap employerTaxMap = new LawHashMap();

                SpcfCalendar paycheckDate = paycheck.getPayrollRun().getPaycheckDate();

                netPay = SAPTranslator.getDoubleFromSpcfMoneyNullZero(paycheck.getNetAmount());
                if (!paycheck.isVoidedOrRecalled()) {
                    netPayTotal = netPayTotal + netPay;
                }


                double paycheckCompensationTotal = 0.;
                double paycheckHoursTotal = 0.;
                for (Compensation compensation : Application.find(Compensation.class, new Query<Compensation>().Where(Compensation.Paycheck().equalTo(paycheck)).EagerLoad(Compensation.QbdtPaylineInfo(), Compensation.CompanyPayrollItem()))) {
                    CompanyPayrollItem companyPayrollItem = compensation.getCompanyPayrollItem().getLatestCompanyPayrollItem();
                    double compensationAmount = SAPTranslator.getDoubleFromSpcfMoneyNullZero(compensation.getCompensationAmount());
                    double hoursWorked = compensation.getHoursWorked();

                    if (companyPayrollItem.getPayrollItem().isTaxableAddition()) {
                        taxableAdditionMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, compensationAmount));
                        taxableAdditionTotal.get(companyPayrollItem).addAmount(compensationAmount, paycheck.isVoidedOrRecalled());
                    } else if (companyPayrollItem.getPayrollItem().isAdditionNoTaxAffect()) {
                        noTaxAffectAdditionMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, compensationAmount));
                        noTaxAffectAdditionTotal.get(companyPayrollItem).addAmount(compensationAmount, paycheck.isVoidedOrRecalled());
                    } else {
                        compensationMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, compensationAmount, hoursWorked));

                        compensationTotal.get(companyPayrollItem).addAmount(compensationAmount, paycheck.isVoidedOrRecalled());
                        compensationTotal.get(companyPayrollItem).addHoursWorked(hoursWorked, paycheck.isVoidedOrRecalled());

                        paycheckCompensationTotal += compensationAmount;
                        paycheckHoursTotal += hoursWorked;
                    }
                }

                SAPLineItemValue totalLineItemValue = new SAPLineItemValue("compTotal", "Paycheck Total", "Paycheck Total", "", paycheckCompensationTotal);
                totalLineItemValue.setHoursWorked(paycheckHoursTotal);
                compensationMap.put("compTotal", totalLineItemValue);

                compensationTotal.get("Paycheck Total").addAmount(paycheckCompensationTotal, paycheck.isVoidedOrRecalled());
                compensationTotal.get("Paycheck Total").addHoursWorked(paycheckHoursTotal, paycheck.isVoidedOrRecalled());

                for (EmployerContribution employerContribution : Application.find(EmployerContribution.class, new Query<EmployerContribution>().Where(EmployerContribution.Paycheck().equalTo(paycheck)).EagerLoad(EmployerContribution.QbdtPaylineInfo(), EmployerContribution.CompanyPayrollItem()))) {
                    CompanyPayrollItem companyPayrollItem = employerContribution.getCompanyPayrollItem().getLatestCompanyPayrollItem();
                    double amount = SAPTranslator.getDoubleFromSpcfMoneyNullZero(employerContribution.getContributionAmount());

                    if (companyPayrollItem.isPreTax()) {
                        employerContributionTaxableMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, amount));
                        employerContributionTaxableTotal.get(companyPayrollItem).addAmount(amount, paycheck.isVoidedOrRecalled());
                    } else {
                        employerContributionNoTaxAffectMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, amount));
                        employerContributionNoTaxAffectTotal.get(companyPayrollItem).addAmount(amount, paycheck.isVoidedOrRecalled());
                    }

                }

                //DD is no longer stored as a deduction.  We also don't store the p-item the DDLine comes in under, so it will use the company's current.
                for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplits()) {
                    CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findDirectDepositPayrollItem(company,false);
                    if(companyPayrollItem == null){
                        logger.warn("EmployeeAdapter:getEmployeeProfilePaycheckDetail() CompanyPayrollItem is null for psid "+company.getSourceCompanyId()+" while querying paychecks.");
                    }
                    double paycheckSplitAmount = SAPTranslator.getDoubleFromSpcfMoneyNullZero(paycheckSplit.getPaycheckSplitAmount());
                    directDepositMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, paycheckSplitAmount));
                    directDepositTotal.get(companyPayrollItem).addAmount(paycheckSplitAmount, paycheck.isVoidedOrRecalled());
                }

                for (Deduction deduction : Application.find(Deduction.class, new Query<Deduction>().Where(Deduction.Paycheck().equalTo(paycheck)).EagerLoad(Deduction.QbdtPaylineInfo(), Deduction.CompanyPayrollItem()))) {
                    CompanyPayrollItem companyPayrollItem = deduction.getCompanyPayrollItem().getLatestCompanyPayrollItem();
                    double deductionAmount = SAPTranslator.getDoubleFromSpcfMoneyNullZero(deduction.getDeductionAmount());

                    if (companyPayrollItem.getPayrollItem().isTaxableAddition()) {
                        taxableAdditionMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, deductionAmount));
                        taxableAdditionTotal.get(companyPayrollItem).addAmount(deductionAmount, paycheck.isVoidedOrRecalled());
                    } else if (companyPayrollItem.getPayrollItem().isAdditionNoTaxAffect()) {
                        noTaxAffectAdditionMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, deductionAmount));
                        noTaxAffectAdditionTotal.get(companyPayrollItem).addAmount(deductionAmount, paycheck.isVoidedOrRecalled());
                    } else if (companyPayrollItem.getPayrollItem().isDirectDeposit()) {
                        directDepositMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, deductionAmount));
                        directDepositTotal.get(companyPayrollItem).addAmount(deductionAmount, paycheck.isVoidedOrRecalled());
                    } else if (companyPayrollItem.isPreTax()) {
                        preTaxDeductionMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, deductionAmount));
                        preTaxDeductionTotal.get(companyPayrollItem).addAmount(deductionAmount, paycheck.isVoidedOrRecalled());
                    } else {
                        postTaxDeductionMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, deductionAmount));
                        postTaxDeductionTotal.get(companyPayrollItem).addAmount(deductionAmount, paycheck.isVoidedOrRecalled());
                    }
                }


                for (Tax tax : paycheck.getTaxCollection()) {
                    CompanyLaw companyLaw = tax.getCompanyLaw().getLatestCompanyLaw();
                    double taxAmount = SAPTranslator.getDoubleFromSpcfMoneyNullZero(tax.getTaxLiabilityAmount());
                    double totalWages = SAPTranslator.getDoubleFromSpcfMoneyNullZero(tax.getTotalWagesAmount());
                    double taxableWages = SAPTranslator.getDoubleFromSpcfMoneyNullZero(tax.getTaxableWagesAmount());

                    //For Law 177, Check EE paid flag on QbdtPayrollItemInfo

                    if ((tax.getLaw().isLaw177() && !companyLaw.getQbdtPayrollItemInfo().getIsEmployeePaid()) || tax.getLaw().getIsEmployerTax()) {
                        employerTaxMap.put(companyLaw, new SAPLineItemValue(companyLaw, taxAmount, totalWages, taxableWages));

                        employerTaxTotal.get(companyLaw).addAmount(taxAmount, paycheck.isVoidedOrRecalled());
                        employerTaxTotal.get(companyLaw).addTaxableWages(taxableWages, paycheck.isVoidedOrRecalled());
                        employerTaxTotal.get(companyLaw).addTotalWages(totalWages, paycheck.isVoidedOrRecalled());
                    } else {
                        employeeTaxMap.put(companyLaw, new SAPLineItemValue(companyLaw, taxAmount, totalWages, taxableWages));

                        employeeTaxTotal.get(companyLaw).addAmount(taxAmount, paycheck.isVoidedOrRecalled());
                        employeeTaxTotal.get(companyLaw).addTaxableWages(taxableWages, paycheck.isVoidedOrRecalled());
                        employeeTaxTotal.get(companyLaw).addTotalWages(totalWages, paycheck.isVoidedOrRecalled());
                    }
                }


                SAPEmployeeLineItemPaycheck paycheckDetail = new SAPEmployeeLineItemPaycheck();
                paycheckDetail.setPaycheckDate(SAPTranslator.getDateFromSpcfCalendar(paycheckDate));
                paycheckDetail.setCompensations(new ArrayList<SAPLineItemValue>(compensationMap.getSortedValues()));
                paycheckDetail.setEmployeeTaxes(new ArrayList<SAPLineItemValue>(employeeTaxMap.getSortedValues()));
                paycheckDetail.setEmployerTaxes(new ArrayList<SAPLineItemValue>(employerTaxMap.getSortedValues()));
                paycheckDetail.setTaxableEmployerContributions(new ArrayList<SAPLineItemValue>(employerContributionTaxableMap.getSortedValues()));
                paycheckDetail.setNoTaxAffectEmployerContributions(new ArrayList<SAPLineItemValue>(employerContributionNoTaxAffectMap.getSortedValues()));
                paycheckDetail.setPreTaxDeductions(new ArrayList<SAPLineItemValue>(preTaxDeductionMap.getSortedValues()));
                paycheckDetail.setPostTaxDeductions(new ArrayList<SAPLineItemValue>(postTaxDeductionMap.getSortedValues()));
                paycheckDetail.setDirectDeposits(new ArrayList<SAPLineItemValue>(directDepositMap.getSortedValues()));
                paycheckDetail.setTaxableAdditions(new ArrayList<SAPLineItemValue>(taxableAdditionMap.getSortedValues()));
                paycheckDetail.setNoTaxAffectAdditions(new ArrayList<SAPLineItemValue>(noTaxAffectAdditionMap.getSortedValues()));
                paycheckDetail.setNetPay(String.valueOf(netPay));
                paycheckDetail.setIsPaycheckVoid(paycheck.isVoidedOrRecalled());
                paycheckDetail.setIsELA(false);
                paycheckDetail.setSourcePayrollRunId(paycheck.getPayrollRun().getSourcePayRunId());
                paycheckDetails.add(paycheckDetail);
            }

            //real ELAs
            DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = LiabilityAdjustment.findNonSupersededELAsByEmployee(company, employee, SAPTranslator.getSpcfCalendarFromDate_BeginDay(paycheckFromDate), SAPTranslator.getSpcfCalendarFromDate_EndDay(paycheckToDate));
            for (LiabilityAdjustment liabilityAdjustment : liabilityAdjustments) {
                LawHashMap employeeTaxMap = new LawHashMap();
                LawHashMap employerTaxMap = new LawHashMap();
                CompanyLaw companyLaw = liabilityAdjustment.getCompanyLaw().getLatestCompanyLaw();
                double taxAmount = SAPTranslator.getDoubleFromSpcfMoneyNullZero(liabilityAdjustment.getAmount());
                double totalWages = SAPTranslator.getDoubleFromSpcfMoneyNullZero(liabilityAdjustment.getTotalWages());
                double taxableWages = SAPTranslator.getDoubleFromSpcfMoneyNullZero(liabilityAdjustment.getTaxableWages());

                //Check for Law 177
                if ((liabilityAdjustment.getLaw().isLaw177() && !companyLaw.getQbdtPayrollItemInfo().getIsEmployeePaid()) || liabilityAdjustment.getLaw().getIsEmployerTax()) {
                    employerTaxMap.put(companyLaw, new SAPLineItemValue(companyLaw, taxAmount, totalWages, taxableWages));

                    employerTaxTotal.get(companyLaw).addAmount(taxAmount, false);
                    employerTaxTotal.get(companyLaw).addTaxableWages(taxableWages, false);
                    employerTaxTotal.get(companyLaw).addTotalWages(totalWages, false);
                } else {
                    employeeTaxMap.put(companyLaw, new SAPLineItemValue(companyLaw, taxAmount, totalWages, taxableWages));

                    employeeTaxTotal.get(companyLaw).addAmount(taxAmount, false);
                    employeeTaxTotal.get(companyLaw).addTaxableWages(taxableWages, false);
                    employeeTaxTotal.get(companyLaw).addTotalWages(totalWages, false);
                }

                SAPEmployeeLineItemPaycheck elaDetail = new SAPEmployeeLineItemPaycheck();
                elaDetail.setPaycheckDate(SAPTranslator.getDateFromSpcfCalendar(liabilityAdjustment.getPayrollRun().getPaycheckDate()));
                elaDetail.setCompensations(new ArrayList<SAPLineItemValue>());
                elaDetail.setEmployeeTaxes(new ArrayList<SAPLineItemValue>(employeeTaxMap.getSortedValues()));
                elaDetail.setEmployerTaxes(new ArrayList<SAPLineItemValue>(employerTaxMap.getSortedValues()));
                elaDetail.setNoTaxAffectAdditions(new ArrayList<SAPLineItemValue>());
                elaDetail.setTaxableAdditions(new ArrayList<SAPLineItemValue>());
                elaDetail.setDirectDeposits(new ArrayList<SAPLineItemValue>());
                elaDetail.setTaxableEmployerContributions(new ArrayList<SAPLineItemValue>());
                elaDetail.setNoTaxAffectEmployerContributions(new ArrayList<SAPLineItemValue>());
                elaDetail.setPreTaxDeductions(new ArrayList<SAPLineItemValue>());
                elaDetail.setPostTaxDeductions(new ArrayList<SAPLineItemValue>());
                elaDetail.setNetPay(String.valueOf(""));
                elaDetail.setIsPaycheckVoid(false);
                elaDetail.setIsELA(true);
                elaDetail.setSourcePayrollRunId(liabilityAdjustment.getPayrollRun().getSourcePayRunId());
                paycheckDetails.add(elaDetail);
            }

            //stupid fake ELAs
            for (QbdtPayrollTransaction qbdtPayrollTransaction : QbdtPayrollTransaction.findQbdtPayrollTransactionsByEmployee(company, employee, SAPTranslator.getSpcfCalendarFromDate_BeginDay(paycheckFromDate), SAPTranslator.getSpcfCalendarFromDate_EndDay(paycheckToDate))) {
                CompanyPayrollItemHashMap preTaxDeductionMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap postTaxDeductionMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap employerContributionTaxableMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap employerContributionNoTaxAffectMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap taxableAdditionMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap noTaxAffectAdditionMap = new CompanyPayrollItemHashMap();
                CompanyPayrollItemHashMap directDepositMap = new CompanyPayrollItemHashMap();

                for (QbdtPayrollTransactionLine qbdtPayrollTransactionLine : qbdtPayrollTransaction.getQbdtPayrollTransactionLineCollection()) {
                    CompanyPayrollItem companyPayrollItem = qbdtPayrollTransactionLine.getCompanyPayrollItem();
                    if (companyPayrollItem == null) {
                        continue;
                    }
                    companyPayrollItem = companyPayrollItem.getLatestCompanyPayrollItem();

                    SpcfMoney amount = qbdtPayrollTransactionLine.getAmount();
                    if(amount==null){
                        amount = SpcfMoney.ZERO;
                    }
                    switch (companyPayrollItem.getPayrollItem().getPayrollItemType()) {
                        //deductions come over from QB as negated and unlike taxes, the adapter does not fix them.
                        case Deduction:
                            if (companyPayrollItem.getPayrollItem().isTaxableAddition()) {
                                taxableAdditionMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount.negate())));
                                taxableAdditionTotal.get(companyPayrollItem).addAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount.negate()), qbdtPayrollTransaction.getIsVoided());
                            } else if (companyPayrollItem.getPayrollItem().isAdditionNoTaxAffect()) {
                                noTaxAffectAdditionMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount.negate())));
                                noTaxAffectAdditionTotal.get(companyPayrollItem).addAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount.negate()), qbdtPayrollTransaction.getIsVoided());
                            } else if (companyPayrollItem.getPayrollItem().isDirectDeposit()) {
                                directDepositMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount.negate())));
                                directDepositTotal.get(companyPayrollItem).addAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount.negate()), qbdtPayrollTransaction.getIsVoided());
                            } else if (companyPayrollItem.isPreTax()) {
                                preTaxDeductionMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount.negate())));
                                preTaxDeductionTotal.get(companyPayrollItem).addAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount.negate()), qbdtPayrollTransaction.getIsVoided());
                            } else {
                                postTaxDeductionMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount.negate())));
                                postTaxDeductionTotal.get(companyPayrollItem).addAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount.negate()), qbdtPayrollTransaction.getIsVoided());
                            }
                            break;
                        case EmployerContribution:
                            if (companyPayrollItem.isPreTax()) {
                                employerContributionTaxableMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount)));
                                employerContributionTaxableTotal.get(companyPayrollItem).addAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount), qbdtPayrollTransaction.getIsVoided());
                            } else {
                                employerContributionNoTaxAffectMap.put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount)));
                                employerContributionNoTaxAffectTotal.get(companyPayrollItem).addAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount), qbdtPayrollTransaction.getIsVoided());
                            }
                    }
                }

                if (preTaxDeductionMap.isEmpty()
                        && postTaxDeductionMap.isEmpty()
                        && employerContributionTaxableMap.isEmpty()
                        && employerContributionNoTaxAffectMap.isEmpty()
                        && taxableAdditionMap.isEmpty()
                        && noTaxAffectAdditionMap.isEmpty()
                        && directDepositMap.isEmpty()) {
                    continue;
                }

                SAPEmployeeLineItemPaycheck elaDetail = new SAPEmployeeLineItemPaycheck();
                elaDetail.setPaycheckDate(SAPTranslator.getDateFromSpcfCalendar(qbdtPayrollTransaction.getTransactionDate()));
                elaDetail.setCompensations(new ArrayList<SAPLineItemValue>());
                elaDetail.setEmployeeTaxes(new ArrayList<SAPLineItemValue>());
                elaDetail.setEmployerTaxes(new ArrayList<SAPLineItemValue>());
                elaDetail.setNoTaxAffectEmployerContributions(new ArrayList<SAPLineItemValue>(employerContributionNoTaxAffectMap.getSortedValues()));
                elaDetail.setTaxableEmployerContributions(new ArrayList<SAPLineItemValue>(employerContributionTaxableMap.getSortedValues()));
                elaDetail.setPreTaxDeductions(new ArrayList<SAPLineItemValue>(preTaxDeductionMap.getSortedValues()));
                elaDetail.setPostTaxDeductions(new ArrayList<SAPLineItemValue>(postTaxDeductionMap.getSortedValues()));
                elaDetail.setNoTaxAffectAdditions(new ArrayList<SAPLineItemValue>(noTaxAffectAdditionMap.getSortedValues()));
                elaDetail.setTaxableAdditions(new ArrayList<SAPLineItemValue>(taxableAdditionMap.getSortedValues()));
                elaDetail.setDirectDeposits(new ArrayList<SAPLineItemValue>(directDepositMap.getSortedValues()));
                elaDetail.setNetPay(String.valueOf(""));
                elaDetail.setIsPaycheckVoid(qbdtPayrollTransaction.getIsVoided());
                elaDetail.setIsELA(true);
                paycheckDetails.add(elaDetail);
            }


            employeeProfileDetails.setCompensationItems(new ArrayList<SAPLineItemValue>(compensationTotal.getSortedValues()));
            ArrayList<SAPLineItemValue> employeeTaxItems = new ArrayList<SAPLineItemValue>(employeeTaxTotal.getSortedValues());
            Collections.sort(employeeTaxItems, new SAPLineItemValueComparator());
            employeeProfileDetails.setEmployeeTaxItems(employeeTaxItems);
            ArrayList<SAPLineItemValue> employerTaxItems = new ArrayList<SAPLineItemValue>(employerTaxTotal.getSortedValues());
            Collections.sort(employerTaxItems, new SAPLineItemValueComparator());
            employeeProfileDetails.setEmployerTaxItems(employerTaxItems);
            employeeProfileDetails.setNoTaxAffectEmployerContributionItems(new ArrayList<SAPLineItemValue>(employerContributionNoTaxAffectTotal.getSortedValues()));
            employeeProfileDetails.setTaxableEmployerContributionItems(new ArrayList<SAPLineItemValue>(employerContributionTaxableTotal.getSortedValues()));
            employeeProfileDetails.setPostTaxDeductionItems(new ArrayList<SAPLineItemValue>(postTaxDeductionTotal.getSortedValues()));
            employeeProfileDetails.setPreTaxDeductionItems(new ArrayList<SAPLineItemValue>(preTaxDeductionTotal.getSortedValues()));
            employeeProfileDetails.setDirectDepositItems(new ArrayList<SAPLineItemValue>(directDepositTotal.getSortedValues()));
            employeeProfileDetails.setTaxableAdditionItems(new ArrayList<SAPLineItemValue>(taxableAdditionTotal.getSortedValues()));
            employeeProfileDetails.setNoTaxAffectAdditionItems(new ArrayList<SAPLineItemValue>(noTaxAffectAdditionTotal.getSortedValues()));
            employeeProfileDetails.setPaychecks(paycheckDetails);
            employeeProfileDetails.setNetPay(netPayTotal);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding employee paycheck details.", pSourceSystemCd, pCompanyId, "Employee", pEmployeeId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return employeeProfileDetails;

    }

    // Source payroll item ID => amounts
    private static class CompanyPayrollItemHashMap extends HashMap<String, SAPLineItemValue> {
        public SAPLineItemValue get(CompanyPayrollItem companyPayrollItem) {
            if (!containsKey(companyPayrollItem.getSourcePayrollItemId())) {
                put(companyPayrollItem, new SAPLineItemValue(companyPayrollItem, 0));
            }
            return get(companyPayrollItem.getSourcePayrollItemId());
        }

        public SAPLineItemValue put(CompanyPayrollItem companyPayrollItem, SAPLineItemValue value) {
            if (containsKey(companyPayrollItem.getSourcePayrollItemId())) {
                //special case where there are multiple line items with the same payroll item on the same paycheck.  Why not.
                SAPLineItemValue sapLineItemValue = get(companyPayrollItem.getSourcePayrollItemId());
                sapLineItemValue.setAmount(sapLineItemValue.getAmount() + value.getAmount());
                sapLineItemValue.setHoursWorked(sapLineItemValue.getHoursWorked() + value.getHoursWorked());
                sapLineItemValue.setTaxableWages(sapLineItemValue.getTaxableWages() + value.getTaxableWages());
                sapLineItemValue.setTotalWages(sapLineItemValue.getTotalWages() + value.getTotalWages());
                return sapLineItemValue;
            }
            return put(companyPayrollItem.getSourcePayrollItemId(), value);
        }

        public void initializeAll(Iterable<SAPLineItemValue> values) {
            for (SAPLineItemValue sapLineItemValue : values) {
                put(sapLineItemValue.getItemId(), new SAPLineItemValue(sapLineItemValue.getItemId(), sapLineItemValue.getItemId(), sapLineItemValue.getSourceDescription(), sapLineItemValue.getTaxFormLine(), 0));
            }
        }

        public List<SAPLineItemValue> getSortedValues() {
            List<SAPLineItemValue> list = new ArrayList<SAPLineItemValue>(values());
            Collections.sort(list);
            return list;
        }

    }

    // Law ID => amounts
    private static class LawHashMap extends HashMap<String, SAPLineItemValue> {
        public SAPLineItemValue get(CompanyLaw companyLaw) {
            if (!containsKey(companyLaw.getSourceId())) {
                put(companyLaw, new SAPLineItemValue(companyLaw, 0, 0, 0));
            }
            return get(companyLaw.getSourceId());
        }

        public SAPLineItemValue put(CompanyLaw companyLaw, SAPLineItemValue value) {
            return put(companyLaw.getSourceId(), value);
        }

        public void initializeAll(Iterable<SAPLineItemValue> values) {
            for (SAPLineItemValue sapLineItemValue : values) {
                put(sapLineItemValue.getItemId(), new SAPLineItemValue(sapLineItemValue.getItemId(), sapLineItemValue.getItemId(), sapLineItemValue.getSourceDescription(), sapLineItemValue.getTaxFormLine(), 0));
            }
        }

        public List<SAPLineItemValue> getSortedValues() {
            List<SAPLineItemValue> list = new ArrayList<SAPLineItemValue>(values());
            Collections.sort(list);
            return list;
        }
    }

    private static class SAPLineItemValueComparator implements Comparator<SAPLineItemValue> {
        public int compare(SAPLineItemValue o1, SAPLineItemValue o2) {
            if (o1.isIrs() && !o2.isIrs()) {
                return -1;
            }
            if (o2.isIrs() && !o1.isIrs()) {
                return 1;
            }
            return (o1.getItemId().compareTo(o2.getItemId()));
        }
    }


    @FlexMethod
    public List<SAPEmployeeLineItemYear> getEmployeeProfileQTDYTDDetails(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pEmployeeId) throws Throwable {
        SAPEmployeePaycheckCollection employeeProfilePaycheckDetail = getEmployeeProfilePaycheckDetail(pSourceSystemCd, pCompanyId, pEmployeeId, null, null);
        //Modified HashMap to LinkedHashMap to keep the insertion order in map.This will make sure it will show the years in asc order in SAP UI for QTD/YTD search.
        //Since paychecks are already sorted by paycheck date , if we keep insertion order it will solve to keep year in asc order in UI.
        Map<Integer, YearValuesHolder> valueMap = new LinkedHashMap<Integer, YearValuesHolder>();

        for (SAPEmployeeLineItemPaycheck paycheck : employeeProfilePaycheckDetail.getPaychecks()) {
            if (paycheck.getIsPaycheckVoid()) {
                continue;
            }

            Date paycheckDate = paycheck.getPaycheckDate();
            SpcfCalendar paycheckCalendar = SAPTranslator.getSpcfCalendarFromDate(paycheckDate);
            int year = paycheckCalendar.getYear();
            if (!valueMap.containsKey(year)) {
                valueMap.put(year, new YearValuesHolder(employeeProfilePaycheckDetail));
            }
            YearValuesHolder yearValuesHolder = valueMap.get(year);

            int quarter = TaxPeriod.getQuarterNumber(paycheckCalendar);

            for (SAPLineItemValue sapLineItemValue : paycheck.getCompensations()) {
                yearValuesHolder.addCompensation(quarter, sapLineItemValue);
            }
            for (SAPLineItemValue sapLineItemValue : paycheck.getDirectDeposits()) {
                yearValuesHolder.addDirectDeposit(quarter, sapLineItemValue);
            }
            for (SAPLineItemValue sapLineItemValue : paycheck.getPreTaxDeductions()) {
                yearValuesHolder.addPreTaxDeduction(quarter, sapLineItemValue);
            }
            for (SAPLineItemValue sapLineItemValue : paycheck.getPostTaxDeductions()) {
                yearValuesHolder.addPostTaxDeduction(quarter, sapLineItemValue);
            }
            for (SAPLineItemValue sapLineItemValue : paycheck.getTaxableAdditions()) {
                yearValuesHolder.addTaxableAddition(quarter, sapLineItemValue);
            }
            for (SAPLineItemValue sapLineItemValue : paycheck.getNoTaxAffectAdditions()) {
                yearValuesHolder.addNoTaxAffectAddition(quarter, sapLineItemValue);
            }
            for (SAPLineItemValue sapLineItemValue : paycheck.getTaxableEmployerContributions()) {
                yearValuesHolder.addTaxableEmployerContribution(quarter, sapLineItemValue);
            }
            for (SAPLineItemValue sapLineItemValue : paycheck.getNoTaxAffectEmployerContributions()) {
                yearValuesHolder.addNoTaxAffectEmployerContribution(quarter, sapLineItemValue);
            }
            for (SAPLineItemValue sapLineItemValue : paycheck.getEmployeeTaxes()) {
                yearValuesHolder.addEmployeeTax(quarter, sapLineItemValue);
            }
            for (SAPLineItemValue sapLineItemValue : paycheck.getEmployerTaxes()) {
                yearValuesHolder.addEmployerTax(quarter, sapLineItemValue);
            }
            yearValuesHolder.addNetPay(quarter, StringUtils.isNotEmpty((paycheck.getNetPay())) ? Double.parseDouble( paycheck.getNetPay()) : 0);
        }

        List<SAPEmployeeLineItemYear> returnList = new ArrayList<SAPEmployeeLineItemYear>();
        for (Map.Entry<Integer, YearValuesHolder> mapEntry : valueMap.entrySet()) {
            SAPEmployeeLineItemYear yearItem = new SAPEmployeeLineItemYear();
            yearItem.setYear(mapEntry.getKey());

            yearItem.setCompensationItems(employeeProfilePaycheckDetail.getCompensationItems());
            yearItem.setDirectDepositItems(employeeProfilePaycheckDetail.getDirectDepositItems());
            yearItem.setPreTaxDeductionItems(employeeProfilePaycheckDetail.getPreTaxDeductionItems());
            yearItem.setPostTaxDeductionItems(employeeProfilePaycheckDetail.getPostTaxDeductionItems());
            yearItem.setTaxableEmployerContributionItems(employeeProfilePaycheckDetail.getTaxableEmployerContributionItems());
            yearItem.setNoTaxAffectEmployerContributionItems(employeeProfilePaycheckDetail.getNoTaxAffectEmployerContributionItems());
            yearItem.setTaxableAdditionItems(employeeProfilePaycheckDetail.getTaxableAdditionItems());
            yearItem.setNoTaxAffectAdditionItems(employeeProfilePaycheckDetail.getNoTaxAffectAdditionItems());
            yearItem.setEmployeeTaxItems(employeeProfilePaycheckDetail.getEmployeeTaxItems());
            yearItem.setEmployerTaxItems(employeeProfilePaycheckDetail.getEmployerTaxItems());
            yearItem.setNetPay(employeeProfilePaycheckDetail.getNetPay());

            yearItem.setQuarters(new ArrayList<SAPEmployeeLineItemQuarter>());
            for (int i = 1; i <= 5; i++) {
                QuarterValueHolder quarterValue = mapEntry.getValue().getQuarter(i);
                SAPEmployeeLineItemQuarter quarterItem = new SAPEmployeeLineItemQuarter();
                quarterItem.setQuarter(i);
                quarterItem.setCompensations(new ArrayList<SAPLineItemValue>(quarterValue.getCompensationMap().getSortedValues()));
                quarterItem.setDirectDeposits(new ArrayList<SAPLineItemValue>(quarterValue.getDirectDepositMap().getSortedValues()));
                quarterItem.setPreTaxDeductions(new ArrayList<SAPLineItemValue>(quarterValue.getPreTaxDeductionMap().getSortedValues()));
                quarterItem.setPostTaxDeductions(new ArrayList<SAPLineItemValue>(quarterValue.getPostTaxDeductionMap().getSortedValues()));
                quarterItem.setTaxableAdditions(new ArrayList<SAPLineItemValue>(quarterValue.getAdditionTaxableMap().getSortedValues()));
                quarterItem.setNoTaxAffectAdditions(new ArrayList<SAPLineItemValue>(quarterValue.getAdditionNoTaxAffectMap().getSortedValues()));
                quarterItem.setTaxableEmployerContributions(new ArrayList<SAPLineItemValue>(quarterValue.getEmployerContributionTaxableMap().getSortedValues()));
                quarterItem.setNoTaxAffectEmployerContributions(new ArrayList<SAPLineItemValue>(quarterValue.getEmployerContributionNoTaxAffectMap().getSortedValues()));
                quarterItem.setEmployeeTaxes(new ArrayList<SAPLineItemValue>(quarterValue.getEmployeeTaxMap().getSortedValues()));
                quarterItem.setEmployerTaxes(new ArrayList<SAPLineItemValue>(quarterValue.getEmployerTaxMap().getSortedValues()));
                quarterItem.setNetPay(String.valueOf(quarterValue.getNetPay()));
                yearItem.getQuarters().add(quarterItem);
            }
            returnList.add(yearItem);
        }

        return returnList;
    }

    private static class YearValuesHolder {
        private Map<Integer, QuarterValueHolder> quarters;
        private QuarterValueHolder ytd;

        private YearValuesHolder(SAPEmployeePaycheckCollection fullCollection) {
            quarters = new HashMap<Integer, QuarterValueHolder>();
            quarters.put(1, new QuarterValueHolder(fullCollection));
            quarters.put(2, new QuarterValueHolder(fullCollection));
            quarters.put(3, new QuarterValueHolder(fullCollection));
            quarters.put(4, new QuarterValueHolder(fullCollection));
            ytd = new QuarterValueHolder(fullCollection);
        }


        private void addCompensation(int quarter, SAPLineItemValue sapLineItemValue) {
            addCompensation(quarter, sapLineItemValue.getItemId(), sapLineItemValue.getAmount(), sapLineItemValue.getHoursWorked());
        }

        private void addCompensation(int quarter, String payrollItemId, double amount, double hours) {
            quarters.get(quarter).addCompensation(payrollItemId, amount, hours);
            ytd.addCompensation(payrollItemId, amount, hours);
        }

        private void addDirectDeposit(int quarter, SAPLineItemValue sapLineItemValue) {
            addDirectDeposit(quarter, sapLineItemValue.getItemId(), sapLineItemValue.getAmount());
        }

        private void addDirectDeposit(int quarter, String payrollItemId, double amount) {
            quarters.get(quarter).addDirectDeposit(payrollItemId, amount);
            ytd.addDirectDeposit(payrollItemId, amount);
        }


        private void addPreTaxDeduction(int quarter, SAPLineItemValue sapLineItemValue) {
            addPreTaxDeduction(quarter, sapLineItemValue.getItemId(), sapLineItemValue.getAmount());
        }

        private void addPreTaxDeduction(int quarter, String payrollItemId, double amount) {
            quarters.get(quarter).addPreTaxDeduction(payrollItemId, amount);
            ytd.addPreTaxDeduction(payrollItemId, amount);
        }

        private void addPostTaxDeduction(int quarter, SAPLineItemValue sapLineItemValue) {
            addPostTaxDeduction(quarter, sapLineItemValue.getItemId(), sapLineItemValue.getAmount());
        }

        private void addPostTaxDeduction(int quarter, String payrollItemId, double amount) {
            quarters.get(quarter).addPostTaxDeduction(payrollItemId, amount);
            ytd.addPostTaxDeduction(payrollItemId, amount);
        }

        private void addTaxableAddition(int quarter, SAPLineItemValue sapLineItemValue) {
            addTaxableAddition(quarter, sapLineItemValue.getItemId(), sapLineItemValue.getAmount());
        }

        private void addTaxableAddition(int quarter, String payrollItemId, double amount) {
            quarters.get(quarter).addTaxableAddition(payrollItemId, amount);
            ytd.addTaxableAddition(payrollItemId, amount);
        }

        private void addNoTaxAffectAddition(int quarter, SAPLineItemValue sapLineItemValue) {
            addNoTaxAffectAddition(quarter, sapLineItemValue.getItemId(), sapLineItemValue.getAmount());
        }

        private void addNoTaxAffectAddition(int quarter, String payrollItemId, double amount) {
            quarters.get(quarter).addNoTaxAffectAddition(payrollItemId, amount);
            ytd.addNoTaxAffectAddition(payrollItemId, amount);
        }

        private void addTaxableEmployerContribution(int quarter, SAPLineItemValue sapLineItemValue) {
            addTaxableEmployerContribution(quarter, sapLineItemValue.getItemId(), sapLineItemValue.getAmount());
        }

        private void addTaxableEmployerContribution(int quarter, String payrollItemId, double amount) {
            quarters.get(quarter).addTaxableEmployerContribution(payrollItemId, amount);
            ytd.addTaxableEmployerContribution(payrollItemId, amount);
        }

        private void addNoTaxAffectEmployerContribution(int quarter, SAPLineItemValue sapLineItemValue) {
            addNoTaxAffectEmployerContribution(quarter, sapLineItemValue.getItemId(), sapLineItemValue.getAmount());
        }

        private void addNoTaxAffectEmployerContribution(int quarter, String payrollItemId, double amount) {
            quarters.get(quarter).addNoTaxAffectEmployerContribution(payrollItemId, amount);
            ytd.addNoTaxAffectEmployerContribution(payrollItemId, amount);
        }

        private void addEmployeeTax(int quarter, SAPLineItemValue sapLineItemValue) {
            addEmployeeTax(quarter, sapLineItemValue.getItemId(), sapLineItemValue.getAmount(), sapLineItemValue.getTaxableWages(), sapLineItemValue.getTotalWages());
        }

        private void addEmployeeTax(int quarter, String lawId, double amount, double taxableWages, double totalWages) {
            quarters.get(quarter).addEmployeeTax(lawId, amount, taxableWages, totalWages);
            ytd.addEmployeeTax(lawId, amount, taxableWages, totalWages);
        }

        private void addEmployerTax(int quarter, SAPLineItemValue sapLineItemValue) {
            addEmployerTax(quarter, sapLineItemValue.getItemId(), sapLineItemValue.getAmount(), sapLineItemValue.getTaxableWages(), sapLineItemValue.getTotalWages());
        }

        private void addEmployerTax(int quarter, String lawId, double amount, double taxableWages, double totalWages) {
            quarters.get(quarter).addEmployerTax(lawId, amount, taxableWages, totalWages);
            ytd.addEmployerTax(lawId, amount, taxableWages, totalWages);
        }

        private void addNetPay(int quarter, double amount) {
            quarters.get(quarter).addNetPay(amount);
            ytd.addNetPay(amount);
        }

        public QuarterValueHolder getYtd() {
            return ytd;
        }

        public QuarterValueHolder getQuarter(int quarter) {
            if (quarter == 5) {
                return ytd;
            }
            return quarters.get(quarter);
        }

    }

    private static class QuarterValueHolder {
        private CompanyPayrollItemHashMap compensationMap = new CompanyPayrollItemHashMap();
        private CompanyPayrollItemHashMap directDepositMap = new CompanyPayrollItemHashMap();
        private CompanyPayrollItemHashMap preTaxDeductionMap = new CompanyPayrollItemHashMap();
        private CompanyPayrollItemHashMap postTaxDeductionMap = new CompanyPayrollItemHashMap();
        private CompanyPayrollItemHashMap additionTaxableMap = new CompanyPayrollItemHashMap();
        private CompanyPayrollItemHashMap additionNoTaxAffectMap = new CompanyPayrollItemHashMap();
        private CompanyPayrollItemHashMap employerContributionTaxableMap = new CompanyPayrollItemHashMap();
        private CompanyPayrollItemHashMap employerContributionNoTaxAffectMap = new CompanyPayrollItemHashMap();
        private LawHashMap employeeTaxMap = new LawHashMap();
        private LawHashMap employerTaxMap = new LawHashMap();
        private double netPay;

        private QuarterValueHolder(SAPEmployeePaycheckCollection fullCollection) {
            compensationMap.initializeAll(fullCollection.getCompensationItems());
            directDepositMap.initializeAll(fullCollection.getDirectDepositItems());
            preTaxDeductionMap.initializeAll(fullCollection.getPreTaxDeductionItems());
            postTaxDeductionMap.initializeAll(fullCollection.getPostTaxDeductionItems());
            additionTaxableMap.initializeAll(fullCollection.getTaxableAdditionItems());
            additionNoTaxAffectMap.initializeAll(fullCollection.getNoTaxAffectAdditionItems());
            employerContributionTaxableMap.initializeAll(fullCollection.getTaxableEmployerContributionItems());
            employerContributionNoTaxAffectMap.initializeAll(fullCollection.getNoTaxAffectEmployerContributionItems());
            employeeTaxMap.initializeAll(fullCollection.getEmployeeTaxItems());
            employerTaxMap.initializeAll(fullCollection.getEmployerTaxItems());
        }

        private void addCompensation(String payrollItemId, double amount, double hours) {
            compensationMap.get(payrollItemId).addAmount(amount, false);
            compensationMap.get(payrollItemId).addHoursWorked(hours, false);
        }

        private void addDirectDeposit(String payrollItemId, double amount) {
            directDepositMap.get(payrollItemId).addAmount(amount, false);
        }

        private void addPreTaxDeduction(String payrollItemId, double amount) {
            preTaxDeductionMap.get(payrollItemId).addAmount(amount, false);
        }

        private void addPostTaxDeduction(String payrollItemId, double amount) {
            postTaxDeductionMap.get(payrollItemId).addAmount(amount, false);
        }

        private void addTaxableAddition(String payrollItemId, double amount) {
            additionTaxableMap.get(payrollItemId).addAmount(amount, false);
        }

        private void addNoTaxAffectAddition(String payrollItemId, double amount) {
            additionNoTaxAffectMap.get(payrollItemId).addAmount(amount, false);
        }

        private void addTaxableEmployerContribution(String payrollItemId, double amount) {
            employerContributionTaxableMap.get(payrollItemId).addAmount(amount, false);
        }

        private void addNoTaxAffectEmployerContribution(String payrollItemId, double amount) {
            employerContributionNoTaxAffectMap.get(payrollItemId).addAmount(amount, false);
        }

        private void addEmployeeTax(String lawId, double amount, double taxableWages, double totalWages) {
            employeeTaxMap.get(lawId).addAmount(amount, false);
            employeeTaxMap.get(lawId).addTaxableWages(taxableWages, false);
            employeeTaxMap.get(lawId).addTotalWages(totalWages, false);
        }

        private void addEmployerTax(String lawId, double amount, double taxableWages, double totalWages) {
            employerTaxMap.get(lawId).addAmount(amount, false);
            employerTaxMap.get(lawId).addTaxableWages(taxableWages, false);
            employerTaxMap.get(lawId).addTotalWages(totalWages, false);
        }

        private void addNetPay(double amount) {
            netPay += amount;
        }

        public CompanyPayrollItemHashMap getCompensationMap() {
            return compensationMap;
        }

        public CompanyPayrollItemHashMap getPreTaxDeductionMap() {
            return preTaxDeductionMap;
        }

        public CompanyPayrollItemHashMap getPostTaxDeductionMap() {
            return postTaxDeductionMap;
        }

        public CompanyPayrollItemHashMap getDirectDepositMap() {
            return directDepositMap;
        }

        public CompanyPayrollItemHashMap getAdditionTaxableMap() {
            return additionTaxableMap;
        }

        public CompanyPayrollItemHashMap getAdditionNoTaxAffectMap() {
            return additionNoTaxAffectMap;
        }

        public CompanyPayrollItemHashMap getEmployerContributionTaxableMap() {
            return employerContributionTaxableMap;
        }

        public CompanyPayrollItemHashMap getEmployerContributionNoTaxAffectMap() {
            return employerContributionNoTaxAffectMap;
        }

        public LawHashMap getEmployeeTaxMap() {
            return employeeTaxMap;
        }

        public LawHashMap getEmployerTaxMap() {
            return employerTaxMap;
        }

        public double getNetPay() {
            return netPay;
        }
    }








}
