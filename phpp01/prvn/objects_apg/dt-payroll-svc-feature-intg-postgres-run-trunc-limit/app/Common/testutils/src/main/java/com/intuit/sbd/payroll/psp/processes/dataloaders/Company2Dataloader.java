/*
 * $Id: //psp/dev/Common/TestUtils/src/com/intuit/sbd/payroll/psp/processes/dataloaders/Company2Dataloader.java#4 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Company2Dataloader {

    private Company company;
    private CompanyBankAccount bankAccount1;
    public Employee employee1;
    public Employee employee2;
    private Employee employee3;
    private Employee employee4;
    private EmployeeBankAccount eeba1;
    private EmployeeBankAccount eeba2;
    private EmployeeBankAccount eeba2_2;
    private EmployeeBankAccount eeba3;
    private EmployeeBankAccount eeba4;

    public Employee employee5;
    public Employee employee6;
    private Employee employee7;
    private Employee employee8;
    private EmployeeBankAccount eeba5;
    private EmployeeBankAccount eeba6;
    private EmployeeBankAccount eeba7;
    private EmployeeBankAccount eeba8;

    private DataLoader dataloader = new DataLoader();

    public Company persistCompany2() {
        // Create Company and CompanyBankAccount
        company = dataloader.persistCompany(getCompany1());

        CompanyService ddCompanyService = dataloader.persistCompanyService(company, getCompany1Service());

        bankAccount1 = persistCompanyBankAccount(company, getCompany1BankAccount());

        employee1 = persistEmployee(getEmployee1(company));
        employee2 = persistEmployee(getEmployee2(company));
        company = Company
                .findCompany("2222222", SourceSystemCode.QBOE);
        employee2 = Employee.findEmployee(company, "EE2_1");

        eeba2 = persistEEBA(company, employee2, getEmployee2BankAccount(employee2));

        company = Company
                .findCompany("2222222", SourceSystemCode.QBOE);
        employee1 = Employee.findEmployee(company, "EE1_1");

        eeba1 = persistEEBA(company, employee1, getEmployee1BankAccount(employee1));

        company = Company
                .findCompany("2222222", SourceSystemCode.QBOE);

        return company;
    }

    public void setEmployee3(Employee pEmployee) {
        employee3 = pEmployee;
    }

    public Employee getEmployee3(Company pCompany) {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("EE3");
        incEmployee.setFirstName("FirstNameOfEE3");
        incEmployee.setLastName("TestLastName3");
        incEmployee.setMiddleName("TMI3");
        incEmployee.setEmail("test3@test3email.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8002551213");
        incEmployee.setTaxId("222223334");
        incEmployee.setStatusCd(EmployeeStatus.Active);
        incEmployee.setStatusEffectiveDate(SpcfCalendar.getNow());
        incEmployee.setCompany(pCompany);
        return incEmployee;
    }

    public EmployeeBankAccountDTO getEmployee3BankAccount() {
        // Initialize the EmployeeBankAccount
        EmployeeBankAccountDTO eeba = new EmployeeBankAccountDTO();
        BankAccountDTO ba = new BankAccountDTO();

        ba.setAccountNumber("22345");
        ba.setRoutingNumber("111000025");
        ba.setBankName("abc bank");
        ba.setAccountType(BankAccountType.Checking);

        eeba.setEmployeeBankAccountId("EEBA3");
        eeba.setBankAccount(ba);
        return eeba;
    }

    public void persistEmployee2BankAccount2() {
        //Get employee 1's bank account info, and add EE2 to it, just with a different source bank account id
        EmployeeBankAccountDTO eebaDTO2 = getEmployee1BankAccount(employee2);
        eebaDTO2.setEmployeeBankAccountId("EEBA2_2");
        eeba2_2 = persistEEBA(company, employee2, eebaDTO2);
    }

    public void persistEmployee3BankAccount2() {
        employee3 = persistEmployee(getEmployee3(company));
        EmployeeBankAccountDTO eebaDTO3 = getEmployee3BankAccount();
        eebaDTO3.setEmployeeBankAccountId("EEBA3");
        eeba3 = persistEEBA(company, employee3, eebaDTO3);
    }

    public PayrollRun persistPayrollRun(PayrollRunDTO pPayrollRunDTO) {
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "2222222", pPayrollRunDTO);
        PayrollServicesTest.assertSuccess("submitPayroll", submitPayrollResult);
        return submitPayrollResult.getResult();
    }

    public PayrollRunDTO getCompany2PR_MatchingEmployeeBankAccounts(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTestExceedsBALimits");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();
        Collection<DDTransactionDTO> ee3Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS1");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("500.55"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("600.11"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee3PaycheckSplit1 = new DDTransactionDTO();
        ee3PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba3));
        ee3PaycheckSplit1.setDDTransactionId("EEBA3PS1");
        ee3PaycheckSplit1.setDDTransactionAmount(new BigDecimal("200.20"));
        ee3Txns.add(ee3PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee3Txns, employee3.getSourceEmployeeId(), SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany2PR_ExceedsPaycheckLimit(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest01");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS1");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("10000"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("100.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany2PR_DoesNotExceedLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest05");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS1");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("567.89"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("1234.56"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany2PR2_DoesNotExceedLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest002");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS1");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("600.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("1000.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO get2ndCompany2PR_DoesNotExceedLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest09");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("444.44"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("333.33"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO get3rdCompany2PR_DoesNotExceedLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest10");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("222.23"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8888.88"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO get3rdCompany2PR_ExceedsOldLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest10");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("249.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("199999749.98"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_MultiplePaychecksSameEE(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest06");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1TxnsForPaycheck1 = new ArrayList();
        Collection<DDTransactionDTO> ee1TxnsForPaycheck2 = new ArrayList();

        DDTransactionDTO ee1Paycheck1 = new DDTransactionDTO();
        ee1Paycheck1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1Paycheck1.setDDTransactionId("EEBA1PS6");
        ee1Paycheck1.setDDTransactionAmount(new BigDecimal("8001.00"));
        ee1TxnsForPaycheck1.add(ee1Paycheck1);

        paychecks.add(createPaycheckDTO(ee1TxnsForPaycheck1, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee1Paycheck2 = new DDTransactionDTO();
        ee1Paycheck2.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1Paycheck2.setDDTransactionId("EEBA2PS6");
        ee1Paycheck2.setDDTransactionAmount(new BigDecimal("8000.00"));
        ee1TxnsForPaycheck2.add(ee1Paycheck2);

        paychecks.add(createPaycheckDTO(ee1TxnsForPaycheck2, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_MultiplePaycheckSplitsSameEE(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest06");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee2PaycheckSplit1.setDDTransactionId("EEBA1PS6");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8001.00"));
        ee1Txns.add(ee2PaycheckSplit1);

        DDTransactionDTO ee2PaycheckSplit2 = new DDTransactionDTO();
        ee2PaycheckSplit2.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit2.setDDTransactionId("EEBA1PS7");
        ee2PaycheckSplit2.setDDTransactionAmount(new BigDecimal("8000.00"));
        ee1Txns.add(ee2PaycheckSplit2);

        paychecks.add(createPaycheckDTO(ee1Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PayrollRunDTO(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest01");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS1");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("100.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("150.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PaycheckDTO createPaycheckDTO(Collection<DDTransactionDTO> pDDTransactions, String pEmployeeId,
                                         String pPaycheckId) {
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

    public DDTransactionDTO createDDTransactionDTO(EmployeeBankAccountDTO pEmployeeBankAccountDTO, BigDecimal pAmount) {
        DDTransactionDTO ddTransactionDTO = new DDTransactionDTO();
        ddTransactionDTO.setDDTransactionAmount(pAmount);
        ddTransactionDTO.setDDTransactionId(SpcfUniqueId.createInstance(true).toString());
        ddTransactionDTO.setEmployeeBankAccount(pEmployeeBankAccountDTO);
        return ddTransactionDTO;
    }

    private EmployeeBankAccountDTO getEmployee1BankAccount(Employee pEmployee) {
        // Initialize the EmployeeBankAccount
        EmployeeBankAccountDTO eeba = new EmployeeBankAccountDTO();
        BankAccountDTO ba = new BankAccountDTO();

        ba.setAccountNumber("12345");
        ba.setRoutingNumber("111000025");
        ba.setBankName("abc bank");
        ba.setAccountType(BankAccountType.Checking);

        eeba.setEmployeeBankAccountId("EEBA1");
        eeba.setBankAccount(ba);
        return eeba;
    }

    private EmployeeBankAccountDTO getEmployee2BankAccount(Employee pEmployee) {
        // Initialize the EmployeeBankAccount
        EmployeeBankAccountDTO eeba = new EmployeeBankAccountDTO();
        BankAccountDTO ba = new BankAccountDTO();

        ba.setAccountNumber("22345");
        ba.setRoutingNumber("111000025");
        ba.setBankName("abc bank");
        ba.setAccountType(BankAccountType.Checking);

        eeba.setEmployeeBankAccountId("EEBA2");
        eeba.setBankAccount(ba);
        return eeba;
    }

    private EmployeeBankAccount persistEEBA(Company pCompany, Employee pEmployee, EmployeeBankAccountDTO pEEBA) {
        ProcessResult<EmployeeBankAccount> procResult = PayrollServices.employeeManager.addEmployeeBankAccount(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pEmployee.getSourceEmployeeId(), pEEBA);
        PayrollServicesTest.assertSuccess("addEmployeeBankAccount", procResult);
        return procResult.getResult();
    }

    private Employee persistEmployee(Employee pEmployee) {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setFirstName(pEmployee.getFirstName());
        employeeDTO.setMiddleName(pEmployee.getMiddleName());
        employeeDTO.setLastName(pEmployee.getLastName());
        employeeDTO.setSocialSecurityNumber(pEmployee.getTaxId());
        employeeDTO.setEmployeeId(pEmployee.getSourceEmployeeId());

        Company company = pEmployee.getCompany();

        return persistEmployee(company, employeeDTO);
    }

    private Employee persistEmployee(Company pCompany, EmployeeDTO pEmployee) {
        return persistEmployee(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(),
                pEmployee);
    }

    private Employee persistEmployee(SourceSystemCode pSourceSystemCd, String pSourceEmployeeId, EmployeeDTO pEmployee) {
        ProcessResult<Employee> procResult = PayrollServices.employeeManager.addEmployee(pSourceSystemCd, pSourceEmployeeId, pEmployee);
        PayrollServicesTest.assertSuccess("addEmployee", procResult);
        return procResult.getResult();
    }

    private CompanyBankAccount persistCompanyBankAccount(Company pCompany, CompanyBankAccountDTO pCompanyBankAccount) {
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pCompanyBankAccount, true, true);
        Assert.assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company,
                processResult.getResult().getSourceBankAccountId());
        company = Company
                .findCompany("2222222", SourceSystemCode.QBOE);

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult2 = PayrollServices.companyManager.verifyCompanyBankAccount(
                pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        assertSuccess("verifyCompanyBankAccount", processResult2);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        return companyBankAccount;
    }

    public CompanyDTO getCompany1() {
        CompanyDTO comp = new CompanyDTO();
        comp.setDBA("Dawn Company 2");
        comp.setFein("999999999");
        comp.setLegalAddress(getTestLegalAddress());
        comp.setLegalName("Dawn Company 2");
        comp.setMailingAddress(getTestMailingAddress());
        comp.setNotificationEmail("notifications2@intuit.com");
        comp.setCompanyId("2222222");
        comp.setSignUpDate(new DateDTO("2007-09-03"));

        comp.setCurrentToken(0L);
        comp.setNextEmployeeId("0");
        comp.setNextPaycheckId("0");
        comp.setNextPayrollItemId("0");
        comp.setNextPayrollTransactionId("0");

        ContactDTO contact = getTestContact();
        Collection<ContactDTO> allContactsForCompany = new ArrayList();
        allContactsForCompany.add(contact);
        comp.setContacts(allContactsForCompany);
        comp.setSourceSystemCd(SourceSystemCode.QBOE);
//        comp.setPayrollFrequency(
//                (PayrollFrequency) PayrollServices.entityFinder.findById(PayrollFrequency.class, PayrollFrequencyBE.Codes.MONTHLY));

        return comp;
    }

    private AddressDTO getTestLegalAddress() {
        AddressDTO legalAddress = new AddressDTO();
        legalAddress.setAddressLine1("6889 Sierra Cnt Pkwy");
        legalAddress.setCity("Reno");
        legalAddress.setZipCode("89511");
        legalAddress.setState("NV");
        return legalAddress;
    }

    private AddressDTO getTestMailingAddress() {
        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("6887 Sierra Center Parkway");
        mailingAddress.setAddressLine2("Suite 46");
        mailingAddress.setAddressLine3("test line 3");
        mailingAddress.setCity("Reno");
        mailingAddress.setZipCode("89521");
        mailingAddress.setState("NV");
        return mailingAddress;
    }

    public ContactDTO getTestContact() {
        ContactDTO contact = new ContactDTO();

        contact.setFirstName("John");
        contact.setMiddleName("P");
        contact.setLastName("Doey");
        contact.setPhoneNumber("(775) 424-8309");
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("someEmail@aol.com");

        AddressDTO contactAddr = new AddressDTO();
        contactAddr.setAddressLine1("123 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));
        return contact;
    }

    public DDServiceInfoDTO getCompany1Service() {
        DDServiceInfoDTO ddCompanyService = new DDServiceInfoDTO();

        ddCompanyService.setAveragePayrollAmount(new BigDecimal("150.00"));

        ddCompanyService.setHighAnnualPayrollAmount(new BigDecimal("250.00"));

        return ddCompanyService;
    }

    public CompanyBankAccountDTO getCompany1BankAccount() {
        CompanyBankAccountDTO retBA = new CompanyBankAccountDTO();
        retBA.setCompanyBankAccountID("C1BA1");

        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber("4747474747");
        bankAccountDTO.setAccountType(BankAccountType.Checking);
        bankAccountDTO.setBankName("Bank of America");
        bankAccountDTO.setRoutingNumber("111000025");

        retBA.setBankAccountDTO(bankAccountDTO);
        return retBA;
    }

    public Employee getEmployee1(Company pCompany) {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("EE1_1");
        incEmployee.setFirstName("SecondCompEEFirst");
        incEmployee.setLastName("SecondCompEELast");
        incEmployee.setMiddleName("TMI");
        incEmployee.setEmail("test@testemail.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8005551212");
        incEmployee.setTaxId("111223333");
        incEmployee.setStatusCd(EmployeeStatus.Active);
        incEmployee.setStatusEffectiveDate(PSPDate.getPSPTime());
        incEmployee.setCompany(pCompany);
        return incEmployee;
    }

    public Employee getEmployee2(Company pCompany) {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("EE2_1");
        incEmployee.setFirstName("SecondCompEEFirstTwo");
        incEmployee.setLastName("SecondCompEELastTwo");
        incEmployee.setMiddleName("TMI2");
        incEmployee.setEmail("test2@test2email.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8002551212");
        incEmployee.setTaxId("222223333");
        incEmployee.setStatusCd(EmployeeStatus.Active);
        incEmployee.setStatusEffectiveDate(SpcfCalendar.getNow());
        incEmployee.setCompany(pCompany);
        return incEmployee;
    }

    public Employee getEmployee(Company pCompany, String id) {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("EE"+id);
        incEmployee.setFirstName("FirstNameOfEE"+id);
        incEmployee.setLastName("TestLastName"+id);
        incEmployee.setMiddleName("TMI"+id);
        incEmployee.setEmail("test"+id+"@testemail.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("800255121"+id);
        incEmployee.setTaxId("22222334"+id);
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

    public ServiceBankAccountDTO createServiceBankAccountDTO(CompanyBankAccountDTO pCompanyBankAccountDTO,
                                                             ServiceCode pServiceCode) {
        ServiceBankAccountDTO serviceBankAccountDTO = new ServiceBankAccountDTO();
        serviceBankAccountDTO.setCompanyBankAccount(pCompanyBankAccountDTO);
        serviceBankAccountDTO.setServiceCode(pServiceCode);
        return serviceBankAccountDTO;
    }

    public void setCompany1(Company pCompany) {
        company = pCompany;
    }

    public void updateTo2DayFundingModel() {
        FundingModel twoDay = Application.findById(FundingModel.class, FundingModel.Codes.TWO_DAY);
        ProcessResult procResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBOE, company.getSourceCompanyId(), twoDay);
        PayrollServicesTest.assertSuccess("updateCompanyFundingModel", procResult);
    }

    public void updateTo5DayFundingModel() {
        FundingModel fiveDay = Application.findById(FundingModel.class, FundingModel.Codes.FIVE_DAY);
        ProcessResult procResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBOE, company.getSourceCompanyId(), fiveDay);
        PayrollServicesTest.assertSuccess("updateCompanyFundingModel", procResult);
    }

    public void updateLimits(SpcfMoney pNewLimit) {
        ProcessResult procResult = PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, company.getSourceCompanyId(), pNewLimit, pNewLimit);
        PayrollServicesTest.assertSuccess("updateDDLimits", procResult);
    }

    public PayrollRunDTO get4thCompany2PR_DoesNotExceedLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest80");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("500.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("800.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public Company getCompany() {
        return company;
    }

    public void returnERDDDB(String pPayrollRunID) {
        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany("2222222", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pPayrollRunID);

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[] {TransactionStateCode.Executed} );

        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(financialTxs,
                "R01",
                "This is an ER Return");

        junit.framework.Assert.assertEquals("Number of txn returns", 1, returnList.size());

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);

    }

    public void returnERDDREDB(String pPayrollRunID) {
        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany("2222222", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pPayrollRunID);

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[] { TransactionStateCode.Created } );

        TransactionReturn transactionReturn =  null;
        // make the employer dd debit transaction status returned and
        // create transaction return entries for this transaction
        for (FinancialTransaction financialTransaction : financialTxs) {
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Executed);
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Returned);
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("This is an Employer Debit return transaction");
            transactionReturn.setBankReturnTraceNumber(113L);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10,
                    SpcfTimeZone.getLocalTimeZone()));
            transactionReturn.setMoneyMovementTransaction(financialTransaction.getMoneyMovementTransaction());
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setCompany(financialTransaction.getCompany());

            transactionReturn=Application.save(transactionReturn);
        }
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);

        // also set the payroll run status to Debit Return
        payrollRun.setPayrollRunStatus(PayrollStatus.PendingAutoRedebit);
    }
}
