package com.intuit.sbd.payroll.psp.processes.dataloaders;

/*
 * $Id: //psp/dev/Common/TestUtils/src/com/intuit/sbd/payroll/psp/processes/dataloaders/CompanyDDPlus401kDataLoader.java#3 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyPayrollItemDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompensationTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DDServiceInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DDTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DeductionTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO401kValidator;
import com.intuit.sbd.payroll.psp.api.dtos.EmployerContributionTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QuickbooksInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kEmployeeInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kServiceInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class CompanyDDPlus401kDataLoader {
    public static final String COMPANY_PSID = "8575577";

    private Company company;
    private CompanyBankAccount cba1;
    private Employee employee1;
    private Employee employee2;
    private Employee employee401k1;
    private Employee employee401k2;    
    private EmployeeBankAccount eeba1;
    private EmployeeBankAccount eeba2;

    private DataLoader dataloader = new DataLoader();
    private String sourceCoId = null;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company pCompany) {
        company = pCompany;
    }

    public Company persistQBCompany1() {
        company = persistQBCompany1(COMPANY_PSID);
        CompanyService tp401kService = add401kService(company);
        persist401kEmployees();
        assertEquals("Status after tp 401k create", ServiceSubStatusCode.PendingFirstPayroll, tp401kService.getStatusCd());

        return company;
    }

    private void persist401kEmployees() {
        employee401k1 = persistEmployee(company, getEmployee1());
        employee401k2 = persistEmployee(company, getEmployee2());
        persistEmployee(company, getEmployee3());
    }

    public Company persistQBCompany1Without401k() {
        company = persistQBCompany1(COMPANY_PSID);
        persist401kEmployees();
        return company;
    }

    public Company persistQBCompany1(String pPsid) {
        sourceCoId = pPsid;

        // Create Company and CompanyBankAccount
        CompanyDTO companyDTO = getCompany1();

        // choose the Offering based on the source system code
        OfferingInfoDTO offeringInfoDTO;
        offeringInfoDTO = new OfferingInfoDTO();
        offeringInfoDTO.setPayrollSubTypeCd(PayrollSubtypeCode.Standard);
        
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(companyDTO);

        assertSuccess("addCompany", result);

        company =  result.getResult();

        PayrollServices.commitUnitOfWork();
        DataLoadServices.addEntitlementUnit(company, "123456", "654321");
        PayrollServices.beginUnitOfWork();

        CompanyService ddService = dataloader.persistCompanyService(company, getCompany1Service());

        cba1 = dataloader.persistCompanyBankAccount(company, getCompany1BankAccount());
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        ddService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
        assertEquals("Status before PIN create", ServiceSubStatusCode.PendingPinCreation, ddService.getStatusCd());
        persistCompanyPIN();
        assertEquals("Status after PIN create", ServiceSubStatusCode.PendingFirstPayroll, ddService.getStatusCd());

        employee1 = persistEmployee(company,getDDEmployee1DTO());
        employee2 = persistEmployee(company,getDDEmployee2DTO());

        eeba1 = persistEEBA(company, employee1, getEmployee1BankAccount(employee1));
        eeba2 = persistEEBA(company, employee2, getEmployee2BankAccount(employee2));

        return company;
    }

    public CompanyService add401kService(Company pCompany) {
        //Now add 401k to this company
        ThirdParty401kServiceInfoDTO tp401kCompanyService = new ThirdParty401kServiceInfoDTO();

        tp401kCompanyService.setCustodialId("122222222");
        tp401kCompanyService.setHasSafeHarbor(true);

        SpcfCalendar startDate = SpcfCalendar.createInstance(2000, 1, 1);
        tp401kCompanyService.setServiceStartDate(startDate);
        tp401kCompanyService.setOfferingCode(OfferingCode.ThirdParty401k);

        CompanyService tp401kDomainService = dataloader.persistCompanyService(pCompany, tp401kCompanyService);

        return tp401kDomainService;
    }

    public EmployeeDTO getEmployee1() {
        EmployeeDTO employee401k = Company401kDataloader.getOptionalElementsEmployeeDTO();
        employee401k.setEmployeeId("401kEeId");
        return employee401k;
    }

    public EmployeeDTO getEmployee2() {
        EmployeeDTO employee401k = Company401kDataloader.getEmployeeDTO();
        employee401k.setEmployeeId("401kEe2Id");
        return employee401k;
    }

    public EmployeeDTO getEmployee3() {
        EmployeeDTO employee401k = Company401kDataloader.getWarningEmployeeDTO();
        employee401k.setEmployeeId("401kEe3Id");
        return employee401k;
    }

    public void persistCompanyPIN() {
        ProcessResult<HashMap<String,String>> procResult =
                PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.QBDT, COMPANY_PSID, "test1234");

        PayrollServicesTest.assertSuccess("createPINResult", procResult);
    }

    public void persistEmployee2BankAccount2() {
        //Get employee 1's bank account info, and add EE2 to it, just with a different source bank account id
        EmployeeBankAccountDTO eebaDTO2 = getEmployee1BankAccount(employee2);

        eebaDTO2.setEmployeeBankAccountId("EEBA2_2");

        persistEEBA(company, employee2, eebaDTO2);
    }



    private EmployeeBankAccountDTO getEmployee1BankAccount(Employee pEmployee) {
        // Initialize the EmployeeBankAccount
        BankAccountDTO ba = new BankAccountDTO();
        ba.setAccountNumber("12345");
        ba.setRoutingNumber("111000025");
        ba.setBankName("abc bank");
        ba.setAccountType(BankAccountType.Checking);

        EmployeeBankAccountDTO eeba = new EmployeeBankAccountDTO();
        eeba.setEmployeeBankAccountId("EEBA1");
        eeba.setBankAccount(ba);

        return eeba;
    }

    private EmployeeBankAccountDTO getEmployee2BankAccount(Employee pEmployee) {
        // Initialize the EmployeeBankAccount
        BankAccountDTO ba = new BankAccountDTO();
        ba.setAccountNumber("22345");
        ba.setRoutingNumber("111000025");
        ba.setBankName("abc bank");
        ba.setAccountType(BankAccountType.Checking);

        EmployeeBankAccountDTO eeba = new EmployeeBankAccountDTO();
        eeba.setEmployeeBankAccountId("EEBA2");
        eeba.setBankAccount(ba);

        return eeba;
    }

    private EmployeeBankAccount persistEEBA(Company pCompany, Employee pEmployee, EmployeeBankAccountDTO pEEBA) {
        ProcessResult<EmployeeBankAccount> procResult =
                PayrollServices.employeeManager.addEmployeeBankAccount(pCompany.getSourceSystemCd(),
                                                                       pCompany.getSourceCompanyId(),
                                                                       pEmployee.getSourceEmployeeId(), pEEBA);

        PayrollServicesTest.assertSuccess("addEmployeeBankAccount", procResult);

        return procResult.getResult();
    }

    private Employee persistEmployee(Company pCompany, EmployeeDTO pEmployee) {
        pEmployee.setValidator(new EmployeeDTO401kValidator());
        return persistEmployee(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pEmployee);
    }

    private Employee persistEmployee(SourceSystemCode pSourceSystemCd, String pSourceEmployeeId, EmployeeDTO pEmployee) {
        ProcessResult<Employee> procResult =
                PayrollServices.employeeManager.addEmployee(pSourceSystemCd, pSourceEmployeeId, pEmployee);

        PayrollServicesTest.assertSuccess("addEmployee", procResult);

        return procResult.getResult();
    }

    public CompanyDTO getCompany1() {
        CompanyDTO companyDTO = new Company3Dataloader().getCompany1();
        companyDTO.setCompanyId(COMPANY_PSID);
        companyDTO.setNextEmployeeId("1");
        companyDTO.setNextPaycheckId("1");
        companyDTO.setNextPayrollItemId("1");
        companyDTO.setNextPayrollTransactionId("1");
        companyDTO.getLegalAddress().setCity("Honolulu");
        companyDTO.getLegalAddress().setState("HI");
        companyDTO.getLegalAddress().setZipCode("96813");
        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();
        qbInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
        qbInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);
        
        companyDTO.setQuickBooksInfo(qbInfoDTO);

        return companyDTO;
    }

    public DDServiceInfoDTO getCompany1Service() {
        DDServiceInfoDTO ddCompanyService = new DDServiceInfoDTO();

        ddCompanyService.setAveragePayrollAmount(new BigDecimal("151.00"));
        ddCompanyService.setHighAnnualPayrollAmount(new BigDecimal("252.00"));

        return ddCompanyService;
    }

    public CompanyBankAccountDTO getCompany1BankAccount() {
        return new Company3Dataloader().getCompany1BankAccount();
    }

    public EmployeeDTO getDDEmployee1DTO() {
        EmployeeDTO incEmployee = new EmployeeDTO();
        incEmployee.setEmployeeId("EE1_1");
        incEmployee.setFirstName("ThirdCompEEFirst");
        incEmployee.setLastName("ThirdCompEELast");
        incEmployee.setMiddleName("TMI");
        incEmployee.setSocialSecurityNumber("111225333");
        return incEmployee;
    }

    public EmployeeDTO getDDEmployee2DTO() {
        EmployeeDTO incEmployee = new EmployeeDTO();

        incEmployee.setEmployeeId("EE2_1");
        incEmployee.setFirstName("ThirdCompEEFirstTwo");
        incEmployee.setLastName("ThirdCompEELastTwo");
        incEmployee.setMiddleName("TMI2");
        incEmployee.setSocialSecurityNumber("212223333");

        return incEmployee;
    }

    public Employee getEmployee(Company pCompany, String id) {
        Employee incEmployee = new Employee();

        incEmployee.setSourceEmployeeId("EE" + id);
        incEmployee.setFirstName("FirstNameOfEE" + id);
        incEmployee.setLastName("TestLastName" + id);
        incEmployee.setMiddleName("TMI" + id);
        incEmployee.setEmail("test" + id + "@testemail.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("800255121" + id);
        incEmployee.setTaxId("22222334" + id);
        incEmployee.setStatusCd(EmployeeStatus.Active);
        incEmployee.setStatusEffectiveDate(SpcfCalendar.getNow());
        incEmployee.setCompany(pCompany);

        return incEmployee;
    }

    public CompanyBankAccountDTO createCompanyBankAccountDTO(CompanyBankAccount pCompanyBankAccount) {
        CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();

        companyBankAccountDTO.setCompanyBankAccountID(pCompanyBankAccount.getSourceBankAccountId());
        companyBankAccountDTO.setBankAccountDTO(createBankAccountDTO(pCompanyBankAccount.getBankAccount()));

        return companyBankAccountDTO;
    }

    public EmployeeBankAccountDTO createEmployeeBankAccountDTONoBADTO(EmployeeBankAccount pEmployeeBankAccount) {
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();

        employeeBankAccountDTO.setEmployeeBankAccountId(pEmployeeBankAccount.getSourceBankAccountId());

        return employeeBankAccountDTO;
    }

    public EmployeeBankAccountDTO createEmployeeBankAccountDTO(EmployeeBankAccount pEmployeeBankAccount) {
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();

        employeeBankAccountDTO.setBankAccount(createBankAccountDTO(pEmployeeBankAccount.getBankAccount()));
        employeeBankAccountDTO.setEmployeeBankAccountId(pEmployeeBankAccount.getSourceBankAccountId());

        return employeeBankAccountDTO;
    }

    public BankAccountDTO createBankAccountDTO(BankAccount pBankAccount) {
        BankAccountDTO bankAccountDTO = new BankAccountDTO();

        bankAccountDTO.setAccountNumber(pBankAccount.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pBankAccount.getRoutingNumber());
        bankAccountDTO.setBankName(pBankAccount.getBankName());
        bankAccountDTO.setAccountType(BankAccountType.valueOf(pBankAccount.getAccountTypeCd().toString()));

        return bankAccountDTO;
    }

    public ServiceBankAccountDTO createServiceBankAccountDTO(CompanyBankAccountDTO pCompanyBankAccountDTO, ServiceCode pServiceCode) {
        ServiceBankAccountDTO serviceBankAccountDTO = new ServiceBankAccountDTO();

        serviceBankAccountDTO.setCompanyBankAccount(pCompanyBankAccountDTO);
        serviceBankAccountDTO.setServiceCode(pServiceCode);

        return serviceBankAccountDTO;
    }

    public PayrollRunDTO getPayrollRunDTO() {
        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2010, Calendar.FEBRUARY, 18);

        cba1 = Application.findById(CompanyBankAccount.class, cba1.getId());
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(cba1);

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList<DDTransactionDTO>();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList<DDTransactionDTO>();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("444.44"));
        ee1Txns.add(ee1PaycheckSplit1);    

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(), "2234567789"));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("333.33"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(), "1234567789"));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest09");
        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO get401kPayrollRunDTO() {
        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2010, Calendar.FEBRUARY, 18);

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest09");
        PaycheckDTO paycheck1 = new PaycheckDTO();

        ArrayList<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
        compensationTransactionDTO.setSourcePayrollItemId("Salary");
        compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance("10.00"));
        compensationTransactionDTO.setCompensationAmount(new SpcfMoney("900.00"));
        compensationTransactions.add(compensationTransactionDTO);
        paycheck1.setCompensationTransactions(compensationTransactions);
        paycheck1.setPaycheckId("2234567789");

        ArrayList<DeductionTransactionDTO> deductionTransactions = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
        deductionTransactionDTO.setSourcePayrollItemId("EmployeeDeferralItem");
        deductionTransactionDTO.setDeductionAmount(new BigDecimal("800.00"));
        deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal("1800.00"));        
        deductionTransactions.add(deductionTransactionDTO);

        DeductionTransactionDTO deductionTransactionDTOLoan = new DeductionTransactionDTO();
        deductionTransactionDTOLoan.setSourcePayrollItemId("MyCompanyLoanPaymentItem");
        deductionTransactionDTOLoan.setDeductionAmount(new BigDecimal("500.00"));
        deductionTransactions.add(deductionTransactionDTOLoan);

        paycheck1.setDeductionTransactions(deductionTransactions);

        ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions = new ArrayList<EmployerContributionTransactionDTO>();
        EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO.setSourcePayrollItemId("MyCompanysERMatch");
        employerContributionTransactionDTO.setContributionAmount(new BigDecimal("700.00"));
        employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal("88.88"));
        employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal("55.55"));
        employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal("56.78"));
        employerContributionTransactions.add(employerContributionTransactionDTO);

        EmployerContributionTransactionDTO employerContributionProfitSharingDTO = new EmployerContributionTransactionDTO();
        employerContributionProfitSharingDTO.setSourcePayrollItemId("MyCompanyProfitSharing");
        employerContributionProfitSharingDTO.setContributionAmount(new BigDecimal("505.00"));
        employerContributionProfitSharingDTO.setContributionYTDAmount(new BigDecimal("85.88"));
        employerContributionProfitSharingDTO.setTaxableWagesAmount(new BigDecimal("55.57"));
        employerContributionProfitSharingDTO.setTotalWagesAmount(new BigDecimal("56.79"));
        employerContributionTransactions.add(employerContributionProfitSharingDTO);

        paycheck1.setEmployerContributionTransactions(employerContributionTransactions);
        paycheck1.setEmployeeId("401kEeId");
        paycheck1.setPaycheckGrossAmount(new SpcfMoney("342.22"));
        paycheck1.setPaycheckNetAmount(new SpcfMoney("333.33"));
        paycheck1.setPayPeriodBeginDate(new DateDTO("2010-02-02"));
        paycheck1.setPayPeriodEndDate(new DateDTO("2010-02-16"));
        
        PaycheckDTO paycheck2 = new PaycheckDTO();
        ArrayList<CompensationTransactionDTO> compensationTransactions2 = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationTransactionDTO2 = new CompensationTransactionDTO();
        compensationTransactionDTO2.setSourcePayrollItemId("Salary");
        compensationTransactionDTO2.setHoursWorked(SpcfDecimal.createInstance("10.00"));
        compensationTransactionDTO2.setCompensationAmount(new SpcfMoney("900.00"));
        compensationTransactionDTO2.setCompensationYTDAmount(new SpcfMoney("10900.00"));
        compensationTransactions2.add(compensationTransactionDTO2);
        paycheck2.setCompensationTransactions(compensationTransactions2);

        ArrayList<DeductionTransactionDTO> deductionTransactions2 = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionTransactionDTO2 = new DeductionTransactionDTO();
        deductionTransactionDTO2.setSourcePayrollItemId("EmployeeDeferralItem");
        deductionTransactionDTO2.setDeductionAmount(new BigDecimal("800.00"));
        deductionTransactions2.add(deductionTransactionDTO2);

        DeductionTransactionDTO deductionTransactionDTOLoan2 = new DeductionTransactionDTO();
        deductionTransactionDTOLoan2.setSourcePayrollItemId("MyCompanyLoanPaymentItem");
        deductionTransactionDTOLoan2.setDeductionAmount(new BigDecimal("500.00"));
        deductionTransactions2.add(deductionTransactionDTOLoan2);

        paycheck2.setDeductionTransactions(deductionTransactions2);

        ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions2 = new ArrayList<EmployerContributionTransactionDTO>();
        EmployerContributionTransactionDTO employerContributionTransactionDTO2 = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO2.setSourcePayrollItemId("MyCompanysERMatch");
        employerContributionTransactionDTO2.setContributionAmount(new BigDecimal("700.00"));
        employerContributionTransactionDTO2.setContributionYTDAmount(new BigDecimal("88.88"));
        employerContributionTransactionDTO2.setTaxableWagesAmount(new BigDecimal("55.55"));
        employerContributionTransactionDTO2.setTotalWagesAmount(new BigDecimal("56.78"));
        employerContributionTransactions2.add(employerContributionTransactionDTO2);

        EmployerContributionTransactionDTO employerContributionProfitSharingDTO2 = new EmployerContributionTransactionDTO();
        employerContributionProfitSharingDTO2.setSourcePayrollItemId("MyCompanyProfitSharing");
        employerContributionProfitSharingDTO2.setContributionAmount(new BigDecimal("505.00"));
        employerContributionProfitSharingDTO2.setContributionYTDAmount(new BigDecimal("85.88"));
        employerContributionProfitSharingDTO2.setTotalWagesAmount(new BigDecimal("56.79"));
        employerContributionTransactions2.add(employerContributionProfitSharingDTO2);

        paycheck2.setEmployerContributionTransactions(employerContributionTransactions2);
        paycheck2.setEmployeeId("401kEe2Id");
        paycheck2.setPaycheckId("1234567789");
        paycheck2.setPaycheckGrossAmount(new SpcfMoney("542.22"));
        paycheck2.setPaycheckYTDGrossAmount(new SpcfMoney("1042.22"));
        paycheck2.setPaycheckYTDNetAmount(new SpcfMoney("1042.22"));
        paycheck2.setPayPeriodBeginDate(new DateDTO("2010-02-02"));
        paycheck2.setPayPeriodEndDate(new DateDTO("2010-02-16"));
        paycheck2.setPaycheckNetAmount(new SpcfMoney("444.44"));

        paychecks.add(paycheck2);
        paychecks.add(paycheck1);
        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getModified401kPayrollRunDTO() {
        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2010, Calendar.FEBRUARY, 18);

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest09");
        PaycheckDTO paycheck1 = new PaycheckDTO();

        ArrayList<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
        compensationTransactionDTO.setSourcePayrollItemId("Salary");
        compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance("21.00"));
        compensationTransactionDTO.setCompensationAmount(new SpcfMoney("1801.00"));
        compensationTransactions.add(compensationTransactionDTO);
        paycheck1.setCompensationTransactions(compensationTransactions);
        paycheck1.setPaycheckId("2234567789");

        ArrayList<DeductionTransactionDTO> deductionTransactions = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
        deductionTransactionDTO.setSourcePayrollItemId("EmployeeDeferralItem");
        deductionTransactionDTO.setDeductionAmount(new BigDecimal("1901.00"));
        deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal("3601.00"));
        deductionTransactions.add(deductionTransactionDTO);

        DeductionTransactionDTO deductionTransactionDTOLoan = new DeductionTransactionDTO();
        deductionTransactionDTOLoan.setSourcePayrollItemId("MyCompanyLoanPaymentItem");
        deductionTransactionDTOLoan.setDeductionAmount(new BigDecimal("1001.00"));
        deductionTransactions.add(deductionTransactionDTOLoan);

        paycheck1.setDeductionTransactions(deductionTransactions);

        ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions = new ArrayList<EmployerContributionTransactionDTO>();
        EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO.setSourcePayrollItemId("MyCompanysERMatch");
        employerContributionTransactionDTO.setContributionAmount(new BigDecimal("1400.00"));
        employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal("166.66"));
        employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal("110.66"));
        employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal("114.88"));
        employerContributionTransactions.add(employerContributionTransactionDTO);

        EmployerContributionTransactionDTO employerContributionProfitSharingDTO = new EmployerContributionTransactionDTO();
        employerContributionProfitSharingDTO.setSourcePayrollItemId("MyCompanyProfitSharing");
        employerContributionProfitSharingDTO.setContributionAmount(new BigDecimal("1110.00"));
        employerContributionProfitSharingDTO.setContributionYTDAmount(new BigDecimal("170.22"));
        employerContributionProfitSharingDTO.setTaxableWagesAmount(new BigDecimal("157.77"));
        employerContributionProfitSharingDTO.setTotalWagesAmount(new BigDecimal("88.88"));
        employerContributionTransactions.add(employerContributionProfitSharingDTO);

        paycheck1.setEmployerContributionTransactions(employerContributionTransactions);
        paycheck1.setEmployeeId("401kEeId");
        paycheck1.setPaycheckGrossAmount(new SpcfMoney("663.33"));
        paycheck1.setPaycheckNetAmount(new SpcfMoney("777.77"));
        paycheck1.setPayPeriodBeginDate(new DateDTO("2010-03-02"));
        paycheck1.setPayPeriodEndDate(new DateDTO("2010-03-16"));

        PaycheckDTO paycheck2 = new PaycheckDTO();
        ArrayList<CompensationTransactionDTO> compensationTransactions2 = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationTransactionDTO2 = new CompensationTransactionDTO();
        compensationTransactionDTO2.setSourcePayrollItemId("Salary");
        compensationTransactionDTO2.setHoursWorked(SpcfDecimal.createInstance("22.22"));
        compensationTransactionDTO2.setCompensationAmount(new SpcfMoney("555.55"));
        compensationTransactionDTO2.setCompensationYTDAmount(new SpcfMoney("333333.33"));
        compensationTransactions2.add(compensationTransactionDTO2);
        paycheck2.setCompensationTransactions(compensationTransactions2);

        ArrayList<DeductionTransactionDTO> deductionTransactions2 = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionTransactionDTO2 = new DeductionTransactionDTO();
        deductionTransactionDTO2.setSourcePayrollItemId("EmployeeDeferralItem");
        deductionTransactionDTO2.setDeductionAmount(new BigDecimal("822200.02"));
        deductionTransactions2.add(deductionTransactionDTO2);

        DeductionTransactionDTO deductionTransactionDTOLoan2 = new DeductionTransactionDTO();
        deductionTransactionDTOLoan2.setSourcePayrollItemId("MyCompanyLoanPaymentItem");
        deductionTransactionDTOLoan2.setDeductionAmount(new BigDecimal("11111.00"));
        deductionTransactions2.add(deductionTransactionDTOLoan2);

        paycheck2.setDeductionTransactions(deductionTransactions2);

        ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions2 = new ArrayList<EmployerContributionTransactionDTO>();
        EmployerContributionTransactionDTO employerContributionTransactionDTO2 = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO2.setSourcePayrollItemId("MyCompanysERMatch");
        employerContributionTransactionDTO2.setContributionAmount(new BigDecimal("1100.00"));
        employerContributionTransactionDTO2.setContributionYTDAmount(new BigDecimal("99.99"));
        employerContributionTransactionDTO2.setTaxableWagesAmount(new BigDecimal("77.77"));
        employerContributionTransactionDTO2.setTotalWagesAmount(new BigDecimal("11.11"));
        employerContributionTransactions2.add(employerContributionTransactionDTO2);

        EmployerContributionTransactionDTO employerContributionProfitSharingDTO2 = new EmployerContributionTransactionDTO();
        employerContributionProfitSharingDTO2.setSourcePayrollItemId("MyCompanyProfitSharing");
        employerContributionProfitSharingDTO2.setContributionAmount(new BigDecimal("8888.88"));
        employerContributionProfitSharingDTO2.setContributionYTDAmount(new BigDecimal("66.77"));
        employerContributionProfitSharingDTO2.setTotalWagesAmount(new BigDecimal("88.99"));
        employerContributionTransactions2.add(employerContributionProfitSharingDTO2);

        paycheck2.setEmployerContributionTransactions(employerContributionTransactions2);
        paycheck2.setEmployeeId("401kEe2Id");
        paycheck2.setPaycheckId("1234567789");
        paycheck2.setPaycheckGrossAmount(new SpcfMoney("2222.22"));
        paycheck2.setPaycheckYTDGrossAmount(new SpcfMoney("34333.33"));
        paycheck2.setPaycheckYTDNetAmount(new SpcfMoney("4444.44"));
        paycheck2.setPayPeriodBeginDate(new DateDTO("2010-03-02"));
        paycheck2.setPayPeriodEndDate(new DateDTO("2010-03-16"));
        paycheck2.setPaycheckNetAmount(new SpcfMoney("455.44"));

        paychecks.add(paycheck2);
        paychecks.add(paycheck1);
        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO get401kNativePayrollRunDTO() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2010, Calendar.FEBRUARY, 18);
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId("FailedDDBatch");

        PaycheckDTO paycheck1 = new PaycheckDTO();
        paycheck1.setPaycheckId("NativePaycheckId1");

        ArrayList<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
        compensationTransactionDTO.setSourcePayrollItemId("Salary");
        compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance("10.00"));
        compensationTransactionDTO.setCompensationAmount(new SpcfMoney("900.00"));
        compensationTransactions.add(compensationTransactionDTO);
        paycheck1.setCompensationTransactions(compensationTransactions);

        ArrayList<DeductionTransactionDTO> deductionTransactions = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
        deductionTransactionDTO.setSourcePayrollItemId("EmployeeDeferralItem");
        deductionTransactionDTO.setDeductionAmount(new BigDecimal("800.00"));
        deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal("1800.00"));
        deductionTransactions.add(deductionTransactionDTO);

        DeductionTransactionDTO deductionTransactionDTOLoan = new DeductionTransactionDTO();
        deductionTransactionDTOLoan.setSourcePayrollItemId("MyCompanyLoanPaymentItem");
        deductionTransactionDTOLoan.setDeductionAmount(new BigDecimal("500.00"));
        deductionTransactions.add(deductionTransactionDTOLoan);

        paycheck1.setDeductionTransactions(deductionTransactions);

        ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions = new ArrayList<EmployerContributionTransactionDTO>();
        EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO.setSourcePayrollItemId("MyCompanysERMatch");
        employerContributionTransactionDTO.setContributionAmount(new BigDecimal("700.00"));
        employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal("88.88"));
        employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal("55.55"));
        employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal("56.78"));
        employerContributionTransactions.add(employerContributionTransactionDTO);

        EmployerContributionTransactionDTO employerContributionProfitSharingDTO = new EmployerContributionTransactionDTO();
        employerContributionProfitSharingDTO.setSourcePayrollItemId("MyCompanyProfitSharing");
        employerContributionProfitSharingDTO.setContributionAmount(new BigDecimal("505.00"));
        employerContributionProfitSharingDTO.setContributionYTDAmount(new BigDecimal("85.88"));
        employerContributionProfitSharingDTO.setTaxableWagesAmount(new BigDecimal("55.57"));
        employerContributionProfitSharingDTO.setTotalWagesAmount(new BigDecimal("56.79"));
        employerContributionTransactions.add(employerContributionProfitSharingDTO);

        paycheck1.setEmployerContributionTransactions(employerContributionTransactions);
        paycheck1.setEmployeeId("401kEeId");
        paycheck1.setPaycheckGrossAmount(new SpcfMoney("342.22"));
        paycheck1.setPaycheckNetAmount(new SpcfMoney("444.44"));
        paychecks.add(paycheck1);

        PaycheckDTO paycheck2 = new PaycheckDTO();
        paycheck2.setPaycheckId("NativePaycheckId2");
        ArrayList<CompensationTransactionDTO> compensationTransactions2 = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationTransactionDTO2 = new CompensationTransactionDTO();
        compensationTransactionDTO2.setSourcePayrollItemId("Salary");
        compensationTransactionDTO2.setHoursWorked(SpcfDecimal.createInstance("10.00"));
        compensationTransactionDTO2.setCompensationAmount(new SpcfMoney("900.00"));
        compensationTransactionDTO2.setCompensationYTDAmount(new SpcfMoney("10900.00"));
        compensationTransactions2.add(compensationTransactionDTO2);
        paycheck2.setCompensationTransactions(compensationTransactions2);

        ArrayList<DeductionTransactionDTO> deductionTransactions2 = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionTransactionDTO2 = new DeductionTransactionDTO();
        deductionTransactionDTO2.setSourcePayrollItemId("EmployeeDeferralItem");
        deductionTransactionDTO2.setDeductionAmount(new BigDecimal("800.00"));
        deductionTransactions2.add(deductionTransactionDTO2);

        DeductionTransactionDTO deductionTransactionDTOLoan2 = new DeductionTransactionDTO();
        deductionTransactionDTOLoan2.setSourcePayrollItemId("MyCompanyLoanPaymentItem");
        deductionTransactionDTOLoan2.setDeductionAmount(new BigDecimal("500.00"));
        deductionTransactions2.add(deductionTransactionDTOLoan2);

        paycheck2.setDeductionTransactions(deductionTransactions2);

        ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions2 = new ArrayList<EmployerContributionTransactionDTO>();
        EmployerContributionTransactionDTO employerContributionTransactionDTO2 = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO2.setSourcePayrollItemId("MyCompanysERMatch");
        employerContributionTransactionDTO2.setContributionAmount(new BigDecimal("700.00"));
        employerContributionTransactionDTO2.setContributionYTDAmount(new BigDecimal("88.88"));
        employerContributionTransactionDTO2.setTaxableWagesAmount(new BigDecimal("55.55"));
        employerContributionTransactionDTO2.setTotalWagesAmount(new BigDecimal("56.78"));
        employerContributionTransactions2.add(employerContributionTransactionDTO2);

        EmployerContributionTransactionDTO employerContributionProfitSharingDTO2 = new EmployerContributionTransactionDTO();
        employerContributionProfitSharingDTO2.setSourcePayrollItemId("MyCompanyProfitSharing");
        employerContributionProfitSharingDTO2.setContributionAmount(new BigDecimal("505.00"));
        employerContributionProfitSharingDTO2.setContributionYTDAmount(new BigDecimal("85.88"));
        employerContributionProfitSharingDTO2.setTotalWagesAmount(new BigDecimal("56.79"));
        employerContributionTransactions2.add(employerContributionProfitSharingDTO2);

        paycheck2.setEmployerContributionTransactions(employerContributionTransactions2);
        paycheck2.setEmployeeId("401kEe2Id");
        paycheck2.setPaycheckGrossAmount(new SpcfMoney("542.22"));
        paycheck2.setPaycheckYTDGrossAmount(new SpcfMoney("1042.22"));
        paycheck2.setPaycheckYTDNetAmount(new SpcfMoney("1042.22"));
        paycheck2.setPayPeriodBeginDate(new DateDTO("2010-02-02"));
        paycheck2.setPayPeriodEndDate(new DateDTO("2010-02-16"));
        paycheck2.setPaycheckNetAmount(new SpcfMoney("333.33"));
        paychecks.add(paycheck2);
        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO get401kNonDDPayrollRunDTO() {
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2010, Calendar.FEBRUARY, 18);
        
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setPayrollTXBatchId("SomeNewId");
        payrollRunDTO.setSettlementDate(payrollDate);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        ArrayList<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();

        PaycheckDTO paycheck1 = new PaycheckDTO();
        paycheck1.setPaycheckGrossAmount(new SpcfMoney("22.22"));
        paycheck1.setPaycheckNetAmount(new SpcfMoney("43.21"));
        paycheck1.setPayPeriodBeginDate(payrollDate);
        paycheck1.setPayPeriodEndDate(payrollDate);
        paycheck1.setPaycheckId("NonDDPaycheckId");
        paycheck1.setEmployeeId("401kEe3Id");

        ArrayList<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
        compensationTransactionDTO.setSourcePayrollItemId("Salary");
        compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance("10.00"));
        compensationTransactionDTO.setCompensationAmount(new SpcfMoney("900.00"));
        compensationTransactions.add(compensationTransactionDTO);
        paycheck1.setCompensationTransactions(compensationTransactions);

        ArrayList<DeductionTransactionDTO> deductionTransactions = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
        deductionTransactionDTO.setSourcePayrollItemId("EmployeeDeferralItem");
        deductionTransactionDTO.setDeductionAmount(new BigDecimal("800.00"));
        deductionTransactions.add(deductionTransactionDTO);
        paycheck1.setDeductionTransactions(deductionTransactions);

        ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions = new ArrayList<EmployerContributionTransactionDTO>();
        EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO.setSourcePayrollItemId("MyCompanysERMatch");
        employerContributionTransactionDTO.setContributionAmount(new BigDecimal("700.00"));
        employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal("88.88"));
        employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal("55.55"));
        employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal("56.78"));
        employerContributionTransactions.add(employerContributionTransactionDTO);
        paycheck1.setEmployerContributionTransactions(employerContributionTransactions);

        paychecks.add(paycheck1);

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO get401kNonDDPayrollRunDTOPaycheckDNE() {
        PayrollRunDTO payrollRunDTO = get401kWarningCloudPayrollRunDTO();
        Object[] paycheckArray = payrollRunDTO.getPaychecks().toArray();
        PaycheckDTO paycheck1 = (PaycheckDTO) paycheckArray[0];
        paycheck1.setPaycheckId("MissingPaycheckId");
        return payrollRunDTO;
    }

    public PayrollRunDTO get401kWarningCloudPayrollRunDTO() {
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2010, Calendar.FEBRUARY, 18);
        DateDTO payrollDate2 = new DateDTO();
        payrollDate2.set(2010, Calendar.FEBRUARY, 26);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setPayrollTXBatchId("SomeOtherNewId");
        payrollRunDTO.setSettlementDate(payrollDate);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        ArrayList<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();

        PaycheckDTO paycheck1 = new PaycheckDTO();
        paycheck1.setPaycheckGrossAmount(new SpcfMoney("24.22"));
        paycheck1.setPaycheckNetAmount(new SpcfMoney("44.21"));
        paycheck1.setPayPeriodBeginDate(payrollDate);
        paycheck1.setPayPeriodEndDate(payrollDate2);
        paycheck1.setPaycheckId("NonDDPaycheckId2");
        paycheck1.setEmployeeId("401kEe3Id");

        ArrayList<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
        compensationTransactionDTO.setSourcePayrollItemId("Salary");
        compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance("10.00"));
        compensationTransactionDTO.setCompensationAmount(new SpcfMoney("-900.00"));
        compensationTransactions.add(compensationTransactionDTO);
        paycheck1.setCompensationTransactions(compensationTransactions);

        ArrayList<DeductionTransactionDTO> deductionTransactions = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
        deductionTransactionDTO.setSourcePayrollItemId("EmployeeDeferralItem");
        deductionTransactionDTO.setDeductionAmount(new BigDecimal("-800.00"));
        deductionTransactions.add(deductionTransactionDTO);

        DeductionTransactionDTO deductionTransactionDTORoth = new DeductionTransactionDTO();
        deductionTransactionDTORoth.setSourcePayrollItemId("RothItemForMyCompany");
        deductionTransactionDTORoth.setDeductionAmount(new BigDecimal("-800.00"));
        deductionTransactions.add(deductionTransactionDTORoth);

        DeductionTransactionDTO deductionTransactionDTOLoan = new DeductionTransactionDTO();
        deductionTransactionDTOLoan.setSourcePayrollItemId("MyCompanyLoanPaymentItem");
        deductionTransactionDTOLoan.setDeductionAmount(new BigDecimal("-500.00"));
        deductionTransactions.add(deductionTransactionDTOLoan);
            
        paycheck1.setDeductionTransactions(deductionTransactions);

        ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions = new ArrayList<EmployerContributionTransactionDTO>();
        EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO.setSourcePayrollItemId("MyCompanysERMatch");
        employerContributionTransactionDTO.setContributionAmount(new BigDecimal("-700.00"));
        employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal("88.88"));
        employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal("55.55"));
        employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal("56.78"));
        employerContributionTransactions.add(employerContributionTransactionDTO);

        EmployerContributionTransactionDTO employerContributionProfitSharingDTO = new EmployerContributionTransactionDTO();
        employerContributionProfitSharingDTO.setSourcePayrollItemId("MyCompanyProfitSharing");
        employerContributionProfitSharingDTO.setContributionAmount(new BigDecimal("-505.00"));
        employerContributionProfitSharingDTO.setContributionYTDAmount(new BigDecimal("85.88"));
        employerContributionProfitSharingDTO.setTaxableWagesAmount(new BigDecimal("55.57"));
        employerContributionProfitSharingDTO.setTotalWagesAmount(new BigDecimal("56.79"));
        employerContributionTransactions.add(employerContributionProfitSharingDTO);

        paycheck1.setEmployerContributionTransactions(employerContributionTransactions);
        paychecks.add(paycheck1);

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO getFixed401kWarningCloudPayrollRunDTO() {
        DateDTO payrollDate = new DateDTO();
        DateDTO payrollDate2 = new DateDTO();
        payrollDate.set(2010, Calendar.FEBRUARY, 18);
        payrollDate2.set(2010, Calendar.FEBRUARY, 26);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setPayrollTXBatchId("SomeOtherNewId");
        payrollRunDTO.setSettlementDate(payrollDate);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        ArrayList<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();

        PaycheckDTO paycheck1 = new PaycheckDTO();
        paycheck1.setPaycheckGrossAmount(new SpcfMoney("24.22"));
        paycheck1.setPaycheckNetAmount(new SpcfMoney("44.21"));
        paycheck1.setPayPeriodBeginDate(payrollDate);
        paycheck1.setPayPeriodEndDate(payrollDate2);
        paycheck1.setPaycheckId("NonDDPaycheckId2");
        paycheck1.setEmployeeId("401kEe3Id");

        ArrayList<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
        compensationTransactionDTO.setSourcePayrollItemId("Salary");
        compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance("10.00"));
        compensationTransactionDTO.setCompensationAmount(new SpcfMoney("900.00"));
        compensationTransactions.add(compensationTransactionDTO);
        paycheck1.setCompensationTransactions(compensationTransactions);

        ArrayList<DeductionTransactionDTO> deductionTransactions = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
        deductionTransactionDTO.setSourcePayrollItemId("EmployeeDeferralItem");
        deductionTransactionDTO.setDeductionAmount(new BigDecimal("800.00"));
        deductionTransactions.add(deductionTransactionDTO);

        DeductionTransactionDTO deductionTransactionDTOLoan = new DeductionTransactionDTO();
        deductionTransactionDTOLoan.setSourcePayrollItemId("MyCompanyLoanPaymentItem");
        deductionTransactionDTOLoan.setDeductionAmount(new BigDecimal("500.00"));
        deductionTransactions.add(deductionTransactionDTOLoan);

        paycheck1.setDeductionTransactions(deductionTransactions);

        ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions = new ArrayList<EmployerContributionTransactionDTO>();
        EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO.setSourcePayrollItemId("MyCompanysERMatch");
        employerContributionTransactionDTO.setContributionAmount(new BigDecimal("700.00"));
        employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal("88.88"));
        employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal("55.55"));
        employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal("56.78"));
        employerContributionTransactions.add(employerContributionTransactionDTO);

        EmployerContributionTransactionDTO employerContributionProfitSharingDTO = new EmployerContributionTransactionDTO();
        employerContributionProfitSharingDTO.setSourcePayrollItemId("MyCompanyProfitSharing");
        employerContributionProfitSharingDTO.setContributionAmount(new BigDecimal("505.00"));
        employerContributionProfitSharingDTO.setContributionYTDAmount(new BigDecimal("85.88"));
        employerContributionProfitSharingDTO.setTaxableWagesAmount(new BigDecimal("55.57"));
        employerContributionProfitSharingDTO.setTotalWagesAmount(new BigDecimal("56.79"));
        employerContributionTransactions.add(employerContributionProfitSharingDTO);

        paycheck1.setEmployerContributionTransactions(employerContributionTransactions);
        paychecks.add(paycheck1);

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO get401kErrorCloudPayrollRunDTO() {
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2010, Calendar.FEBRUARY, 18);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setPayrollTXBatchId("SomeErrorId");
        payrollRunDTO.setSettlementDate(payrollDate);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        ArrayList<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();

        PaycheckDTO paycheck1 = new PaycheckDTO();
        paycheck1.setPaycheckGrossAmount(new SpcfMoney("24.22"));
        paycheck1.setPaycheckNetAmount(new SpcfMoney("44.21"));
        paycheck1.setPayPeriodBeginDate(payrollDate);
        paycheck1.setPayPeriodEndDate(payrollDate);
        paycheck1.setPaycheckId("NonDDPaycheckId2");
        paycheck1.setEmployeeId("401kEe3Id");

        ArrayList<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
        compensationTransactionDTO.setSourcePayrollItemId("Salary");
        compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance("10.00"));
        compensationTransactionDTO.setCompensationAmount(new SpcfMoney("-900.00"));
        compensationTransactions.add(compensationTransactionDTO);
        paycheck1.setCompensationTransactions(compensationTransactions);

        ArrayList<DeductionTransactionDTO> deductionTransactions = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
        deductionTransactionDTO.setSourcePayrollItemId("EmployeeDeferralItem2");
        deductionTransactionDTO.setDeductionAmount(new BigDecimal("-800.00"));
        deductionTransactions.add(deductionTransactionDTO);

        DeductionTransactionDTO deductionTransactionDTORoth = new DeductionTransactionDTO();
        deductionTransactionDTORoth.setSourcePayrollItemId("RothItemForMyCompany");
        deductionTransactionDTORoth.setDeductionAmount(new BigDecimal("-800.00"));
        deductionTransactions.add(deductionTransactionDTORoth);

        DeductionTransactionDTO deductionTransactionDTOLoan = new DeductionTransactionDTO();
        deductionTransactionDTOLoan.setSourcePayrollItemId("MyCompanyLoanPaymentItem2");
        deductionTransactionDTOLoan.setDeductionAmount(new BigDecimal("-500.00"));
        deductionTransactions.add(deductionTransactionDTOLoan);

        paycheck1.setDeductionTransactions(deductionTransactions);

        ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions = new ArrayList<EmployerContributionTransactionDTO>();
        EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO.setSourcePayrollItemId("MyCompanysERMatch");
        employerContributionTransactionDTO.setContributionAmount(new BigDecimal("-700.00"));
        employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal("88.88"));
        employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal("55.55"));
        employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal("56.78"));
        employerContributionTransactions.add(employerContributionTransactionDTO);

        EmployerContributionTransactionDTO employerContributionProfitSharingDTO = new EmployerContributionTransactionDTO();
        employerContributionProfitSharingDTO.setSourcePayrollItemId("MyCompanyProfitSharing2");
        employerContributionProfitSharingDTO.setContributionAmount(new BigDecimal("-505.00"));
        employerContributionProfitSharingDTO.setContributionYTDAmount(new BigDecimal("85.88"));
        employerContributionProfitSharingDTO.setTaxableWagesAmount(new BigDecimal("55.57"));
        employerContributionProfitSharingDTO.setTotalWagesAmount(new BigDecimal("56.79"));
        employerContributionTransactions.add(employerContributionProfitSharingDTO);

        paycheck1.setEmployerContributionTransactions(employerContributionTransactions);
        paychecks.add(paycheck1);

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }


    public PaycheckDTO createPaycheckDTO(Collection<DDTransactionDTO> pDDTransactions, String pEmployeeId, String pPaycheckId) {
        PaycheckDTO paycheckDTO = new PaycheckDTO();

        paycheckDTO.setDdTransactions((List<DDTransactionDTO>)pDDTransactions);
        paycheckDTO.setEmployeeId(pEmployeeId);
        paycheckDTO.setPaycheckId(pPaycheckId);

        SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
        for (DDTransactionDTO currDDTxn : pDDTransactions) {
            SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
            totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
        }
        paycheckDTO.setPaycheckNetAmount(totalPaycheckNetAmount);

        return paycheckDTO;
    }

    public void loadPayrollItems() {

        CompanyPayrollItemDTO salaryPayrollItemDTO = new CompanyPayrollItemDTO();
        salaryPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Compensation);
        salaryPayrollItemDTO.setSourcePayrollItemDescription("My description!");
        salaryPayrollItemDTO.setSourcePayrollItemId("Salary");

        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Tp401kEmployeeDeferral);
        companyPayrollItemDTO.setSourcePayrollItemId("EmployeeDeferralItem");

        CompanyPayrollItemDTO rothPayrollItemDTO = new CompanyPayrollItemDTO();
        rothPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Tp401kRoth);
        rothPayrollItemDTO.setSourcePayrollItemId("RothItemForMyCompany");

        CompanyPayrollItemDTO erMatchPayrollItemDTO = new CompanyPayrollItemDTO();
        erMatchPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Tp401kEmployerMatch);
        erMatchPayrollItemDTO.setSourcePayrollItemId("MyCompanysERMatch");

        CompanyPayrollItemDTO loanPaymentPayrollItemDTO = new CompanyPayrollItemDTO();
        loanPaymentPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Tp401kLoanPayment);
        loanPaymentPayrollItemDTO.setSourcePayrollItemId("MyCompanyLoanPaymentItem");

        CompanyPayrollItemDTO profitSharingPayrollItemDTO = new CompanyPayrollItemDTO();
        profitSharingPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Tp401kProfitSharing);
        profitSharingPayrollItemDTO.setSourcePayrollItemId("MyCompanyProfitSharing");

        ProcessResult addPayrollItemProcResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(SourceSystemCode.QBDT, "8575577", salaryPayrollItemDTO);
        assertSuccess("addItem", addPayrollItemProcResult);

        addPayrollItemProcResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(SourceSystemCode.QBDT, "8575577", companyPayrollItemDTO);
        assertSuccess("addItem", addPayrollItemProcResult);

        addPayrollItemProcResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(SourceSystemCode.QBDT, "8575577", rothPayrollItemDTO);
        assertSuccess("addItem", addPayrollItemProcResult);

        addPayrollItemProcResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(SourceSystemCode.QBDT, "8575577", erMatchPayrollItemDTO);
        assertSuccess("addItem", addPayrollItemProcResult);

        addPayrollItemProcResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(SourceSystemCode.QBDT, "8575577", loanPaymentPayrollItemDTO);
        assertSuccess("addItem", addPayrollItemProcResult);

        addPayrollItemProcResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(SourceSystemCode.QBDT, "8575577", profitSharingPayrollItemDTO);
        assertSuccess("addItem", addPayrollItemProcResult);

    }

    public void deleteAfterOffload() {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        ProcessResult procResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "8575577", get401kWarningCloudPayrollRunDTO());
        assertSuccess(procResult2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "SomeOtherNewId");
        ProcessResult<PayrollRun> procResult3 = PayrollServices.payrollManager.updatePayroll(SourceSystemCode.QBDT, "8575577", payrollRun, getFixed401kWarningCloudPayrollRunDTO().getPaychecks());
        assertSuccess(procResult3);

        addAndOffloadTP401K(procResult3.getResult(), true);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EmployeeDTO waningEE = Company401kDataloader.getWarningEmployeeDTO();
        ThirdParty401kEmployeeInfoDTO eeInfo = new ThirdParty401kEmployeeInfoDTO();
        waningEE.setBirthDate(new DateDTO(PSPDate.getPSPTime()));
        waningEE.setEmployee401kInfo(eeInfo);
        waningEE.setHireDate(new DateDTO(PSPDate.getPSPTime()));
        waningEE.setSocialSecurityNumber("222222222");
        waningEE.setEmployeeId("401kEe3Id");
        ProcessResult procResult4 = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, "8575577", waningEE);
        assertSuccess(procResult4);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal currentPrincipal = Application.getCurrentPrincipal();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.QBDTWSAdapter);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, Calendar.JUNE, 28, SpcfTimeZone.getLocalTimeZone()));
        ProcessResult procresult = PayrollServices.payrollManager.deletePaycheck(SourceSystemCode.QBDT, "8575577", "NonDDPaycheckId2", null);
        PayrollServices.setCurrentPrincipal(currentPrincipal);
        assertSuccess(procresult);
        assertEquals("number of messages", 1, procresult.getMessages().size());
    }

    public void deleteAfterOffloadMissedCutoff() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, Calendar.JUNE, 28, SpcfTimeZone.getLocalTimeZone()));
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        ProcessResult procResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "8575577", get401kWarningCloudPayrollRunDTO());
        assertSuccess(procResult2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "SomeOtherNewId");
        ProcessResult procResult3 = PayrollServices.payrollManager.updatePayroll(SourceSystemCode.QBDT, "8575577", payrollRun, getFixed401kWarningCloudPayrollRunDTO().getPaychecks());
        assertSuccess(procResult3);
        assertEquals("number of messages", 1, procResult3.getMessages().size());
        MessageList infoMessages = procResult3.getInfoMessages();
        assertEquals("number of info messages", 1, infoMessages.size());
        Message infoMessage = infoMessages.get(0);
        assertEquals("Message #", "10069", infoMessage.getMessageCode());
        assertEquals("Message text", "The transmission date for the paycheck dated 02/18/2010 has passed.  You will need to manually enter the paycheck on your Intuit 401(k) sponsor portal to ensure that it has been recorded properly.", infoMessage.getMessage());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EmployeeDTO waningEE = Company401kDataloader.getWarningEmployeeDTO();
        ThirdParty401kEmployeeInfoDTO eeInfo = new ThirdParty401kEmployeeInfoDTO();
        waningEE.setBirthDate(new DateDTO(PSPDate.getPSPTime()));
        waningEE.setEmployee401kInfo(eeInfo);
        waningEE.setHireDate(new DateDTO(PSPDate.getPSPTime()));
        waningEE.setSocialSecurityNumber("222222222");
        waningEE.setEmployeeId("401kEe3Id");
        ProcessResult procResult4 = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, "8575577", waningEE);
        assertSuccess(procResult4);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult procresult = PayrollServices.payrollManager.deletePaycheck(SourceSystemCode.QBDT, "8575577", "NonDDPaycheckId2", null);
        assertSuccess(procresult);
        assertEquals("number of messages", 0, procresult.getMessages().size());
    }

    public void makePaychecksOffloadable() {
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "SomeOtherNewId");
        ProcessResult<PayrollRun> procResult3 = PayrollServices.payrollManager.updatePayroll(SourceSystemCode.QBDT, "8575577", payrollRun, getFixed401kWarningCloudPayrollRunDTO().getPaychecks());
        assertSuccess(procResult3);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EmployeeDTO waningEE = Company401kDataloader.getWarningEmployeeDTO();
        ThirdParty401kEmployeeInfoDTO eeInfo = new ThirdParty401kEmployeeInfoDTO();
        waningEE.setBirthDate(new DateDTO(PSPDate.getPSPTime()));
        waningEE.setEmployee401kInfo(eeInfo);
        waningEE.setHireDate(new DateDTO(PSPDate.getPSPTime()));
        waningEE.setSocialSecurityNumber("222222222");
        waningEE.setEmployeeId("401kEe3Id");
        ProcessResult procResult4 = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, "8575577", waningEE);
        assertSuccess(procResult4);

        addAndOffloadTP401K(procResult3.getResult(), true);

        PayrollServices.commitUnitOfWork();
    }
    
    public void voidAfterOffloadMissedCutoff() {
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidDTO = new VoidPayrollDTO();
        voidDTO.setSourcePayrollRunId("SomeOtherNewId");
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add("NonDDPaycheckId2");
        voidDTO.setPaycheckIdList(paycheckList);
        ProcessResult procresult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, "8575577", voidDTO);
        assertSuccess(procresult);
        assertEquals("number of messages", 0, procresult.getMessages().size());
        //todo test for event(s)
        PayrollServices.commitUnitOfWork();
    }

    public void voidAfterOffload() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, Calendar.JUNE, 28, SpcfTimeZone.getLocalTimeZone()));
        VoidPayrollDTO voidDTO = new VoidPayrollDTO();
        voidDTO.setSourcePayrollRunId("SomeOtherNewId");
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add("NonDDPaycheckId2");
        voidDTO.setPaycheckIdList(paycheckList);
        PayrollServices.setCurrentPrincipal(SystemPrincipal.QBDTWSAdapter);
        ProcessResult procresult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, "8575577", voidDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertSuccess(procresult);
        assertEquals("number of messages", 1, procresult.getMessages().size());
        Paycheck paycheck = Paycheck.findPaycheck(company, "NonDDPaycheckId2");
        assertEquals("paycheck status", PaycheckStatusCode.Inactive, paycheck.getStatus());
        assertNotNull("adjustment submission", paycheck.getCompanyAdjustmentSubmission());        
        DomainEntitySet<CompanyEventDetail> paycheckEventDetails = 
                CompanyEvent.findCompanyEventDetails(company, EventTypeCode.InvalidPaycheckInformation, EventDetailTypeCode.SourcePaycheckId, "NonDDPaycheckId2");
        assertEquals("company event details", 1, paycheckEventDetails.size());
        assertEquals("company event type",
                     EventTypeCode.InvalidPaycheckInformation,
                     paycheckEventDetails.get(0).getCompanyEvent().getEventTypeCd());
        assertEquals("company event message level",
                     MessageInfo.MessageLevel.INFO,
                     MessageInfo.MessageLevel.valueOf(paycheckEventDetails.get(0).getCompanyEvent().getEventDetailInfo().get(EventDetailTypeCode.MessageLevel)));
        PayrollServices.commitUnitOfWork();
    }

    public void persistHappyPathPayrolls() {
        persistHappyPathPayrolls(false, false);
    }

    public void persistHappyPathPayrolls(boolean create401KObjects, boolean markAsSent) {
        PayrollServices.beginUnitOfWork();
        persistQBCompany1();
        PayrollServices.commitUnitOfWork();

        //Mimic OFX submission
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = getPayrollRunDTO();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, "8575577", payrollRunDTO);
        assertSuccess("submitPayroll", processResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        PayrollRun ddPayrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        assertNotNull("DD Payroll", ddPayrollRun);
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, ddPayrollRun);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Mimic cloud submission for paychecks that already exists for DD
        loadPayrollItems();
        PayrollRunDTO updatedPayrollRun = get401kPayrollRunDTO();
        company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        ProcessResult<PayrollRun> submit401kProcessResult = PayrollServices.payrollManager
                .updatePayroll(SourceSystemCode.QBDT, "8575577", payrollRun, updatedPayrollRun.getPaychecks());
        assertSuccess("submit401kProcessResult", submit401kProcessResult);
        //Mimic cloud submission for a paycheck that doesn't exist for DD
        PayrollRunDTO nonDDOnlyPayrollRunDTO = get401kNonDDPayrollRunDTO();
        company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        ProcessResult<PayrollRun> submit401kNonDDProcessResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, "8575577", nonDDOnlyPayrollRunDTO);
        assertSuccess("submit401kNonDDProcessResult", submit401kNonDDProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        PayrollRun fourOhOnekPayrollRun = PayrollRun.findPayrollRun(company, nonDDOnlyPayrollRunDTO.getPayrollTXBatchId());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        assertNotNull("DD Payroll", ddPayrollRun);
        DomainEntitySet<CompanyEvent> paycheckInvalidEvents = CompanyEvent.findCompanyEventsByTypes(company, new EventTypeCode[] {EventTypeCode.InvalidPaycheckInformation}, null, null, null, 501);
        assertEquals("Number of invalid events", 0, paycheckInvalidEvents.size());
        DataLoadServices.assertPayrollsEqual(updatedPayrollRun, payrollRun);
        DataLoadServices.assertPayrollsEqual(nonDDOnlyPayrollRunDTO, PayrollStatus.Complete, fourOhOnekPayrollRun);
        PayrollServices.rollbackUnitOfWork();

        if (create401KObjects == true) {
            PayrollServices.beginUnitOfWork();
            addAndOffloadTP401K(processResult.getResult(), markAsSent);
            PayrollServices.commitUnitOfWork();
        }
    }

    /**
     * This method is a hack to add TP401K objects directly to all paychecks in a PayrollRun.  After adding the TP401K
     * objects, it changes the state to sent.
     * @param incomingPayrollRun The PayrollRun to add the TP401K objects to
     * @param markAsSent Should the TP401K objects be marked as sent
     */
    public void addAndOffloadTP401K(PayrollRun incomingPayrollRun, boolean markAsSent) {
        // Get payroll run again because it could be coming from another session
        PayrollRun payrollRun = PayrollRun.findPayrollRun(incomingPayrollRun.getCompany(), incomingPayrollRun.getSourcePayRunId());

        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            ThirdParty401kPaycheck.addTP401K(paycheck);

            if (markAsSent) {
                paycheck.getThirdParty401kPaycheck().markAsSent();
            }
        }
    }
}