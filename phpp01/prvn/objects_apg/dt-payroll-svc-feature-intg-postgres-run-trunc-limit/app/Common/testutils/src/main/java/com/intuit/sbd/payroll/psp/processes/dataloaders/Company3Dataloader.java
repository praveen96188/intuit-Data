/*
 * $Id: //psp/dev/Common/TestUtils/src/com/intuit/sbd/payroll/psp/processes/dataloaders/Company3Dataloader.java#4 $
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
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Company3Dataloader {
    private Company company;
    private CompanyBankAccount bankAccount1;
    private EmployeeBankAccount eeba1;
    private EmployeeBankAccount eeba2;
    public Employee employee1;
    public Employee employee2;
    private String mRoutingNumber;

    private DataLoader dataloader = new DataLoader();

    public Company3Dataloader(){
        mRoutingNumber = null;
    }

    public Company3Dataloader(String pRoutingNumber){
        mRoutingNumber = pRoutingNumber;
    }

    public Company persistCompany3() {
        // Create Company and CompanyBankAccount
        CompanyDTO company1 = getCompany1();
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");

        company = dataloader.persistCompany(company1);

        dataloader.persistCompanyService(company, getCompany1Service());

        bankAccount1 = persistCompanyBankAccount(company, getCompany1BankAccount());

        persistCompanyPIN();

        employee1 = persistEmployee(getEmployee1(company));
        eeba1 = persistEEBA(company, employee1, getEmployee1BankAccount(employee1));

        employee2 = persistEmployee(getEmployee2(company));
        eeba2 = persistEEBA(company, employee2, getEmployee2BankAccount(employee2));

        return company;
    }

    public void persistCompanyPIN() {
        ProcessResult<HashMap<String,String>> procResult =
                PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.QBDT, "8574536", "1234567a");

        PayrollServicesTest.assertSuccess("createPINResult", procResult);
    }

    public void persistEmployee2BankAccount2() {
        //Get employee 1's bank account info, and add EE2 to it, just with a different source bank account id
        EmployeeBankAccountDTO eebaDTO2 = getEmployee1BankAccount(employee2);

        eebaDTO2.setEmployeeBankAccountId("EEBA2_2");

        persistEEBA(company, employee2, eebaDTO2);
    }

    public PayrollRun persistPayrollRun(PayrollRunDTO pPayrollRunDTO) {
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "8574536", pPayrollRunDTO);

        PayrollServicesTest.assertSuccess("submitPayroll", submitPayrollResult);

        return submitPayrollResult.getResult();
    }

    public PayrollRunDTO getCompany3PR_DoesNotExceedLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest87");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("212.23"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8388.88"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        employee2.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany3PR_CrossesDST(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest97");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("212.23"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8388.88"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        employee2.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany3PR2_DoesNotExceedLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest002");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("312.23"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("188.88"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        employee2.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO getCompanyPR_DoesNotExceedLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest87");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("765.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("343.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        employee2.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO get2ndCompany2PR_DoesNotExceedLimits() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.OCTOBER, 10);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
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

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("333.33"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        employee2.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO get3rdCompany2PR_DoesNotExceedLimits() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.NOVEMBER, 15);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
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

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8888.88"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        employee2.getSourceEmployeeId(),
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

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("199999749.98"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        employee2.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_MultiplePaychecksSameEE() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.OCTOBER, 2);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
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

        paychecks.add(createPaycheckDTO(ee1TxnsForPaycheck1,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee1Paycheck2 = new DDTransactionDTO();
        ee1Paycheck2.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1Paycheck2.setDDTransactionId("EEBA2PS6");
        ee1Paycheck2.setDDTransactionAmount(new BigDecimal("8000.00"));
        ee1TxnsForPaycheck2.add(ee1Paycheck2);

        paychecks.add(createPaycheckDTO(ee1TxnsForPaycheck2,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_MultiplePaycheckSplitsSameEE() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.OCTOBER, 2);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest06");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee2PaycheckSplit1.setDDTransactionId("EEBA1PS6");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8001.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        DDTransactionDTO ee2PaycheckSplit2 = new DDTransactionDTO();
        ee2PaycheckSplit2.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit2.setDDTransactionId("EEBA1PS7");
        ee2PaycheckSplit2.setDDTransactionAmount(new BigDecimal("8000.00"));
        ee2Txns.add(ee2PaycheckSplit2);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        employee2.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PayrollRunDTO() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.OCTOBER, 2);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
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

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("150.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        employee2.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

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

        paycheckDTO.setPaycheckNetAmount(SpcfMoney.ZERO);

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
                                                                       pEmployee.getSourceEmployeeId(),
                                                                       pEEBA);

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

        return persistEmployee(pEmployee.getCompany(), employeeDTO);
    }

    private Employee persistEmployee(Company pCompany, EmployeeDTO pEmployee) {
        return persistEmployee(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pEmployee);
    }

    private Employee persistEmployee(SourceSystemCode pSourceSystemCd, String pSourceEmployeeId, EmployeeDTO pEmployee) {
        ProcessResult<Employee> procResult = PayrollServices.employeeManager.addEmployee(pSourceSystemCd, pSourceEmployeeId, pEmployee);

        PayrollServicesTest.assertSuccess("addEmployee", procResult);

        return procResult.getResult();
    }

    private CompanyBankAccount persistCompanyBankAccount(Company pCompany, CompanyBankAccountDTO pCompanyBankAccount) {
        ProcessResult<CompanyBankAccount> processResult =
                PayrollServices.companyManager.addCompanyBankAccount(pCompany.getSourceSystemCd(),
                                                                     pCompany.getSourceCompanyId(),
                                                                     pCompanyBankAccount, true, true);

        Assert.assertEquals(0, processResult.getMessages().size());

        CompanyBankAccount companyBankAccount = processResult.getResult();

        company = Company.findCompany("8574536", SourceSystemCode.QBDT);

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions =
                companyBankAccount.getVerificationTransactions();

        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }

        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);

        Application.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();

        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);

        ProcessResult<CompanyBankAccount> processResult2 =
                PayrollServices.companyManager.verifyCompanyBankAccount(pCompany.getSourceSystemCd(),
                                                                        pCompany.getSourceCompanyId(),
                                                                        companyBankAccount.getSourceBankAccountId(),
                                                                        amountsToVerify.get(0),
                                                                        amountsToVerify.get(1), false);

        assertSuccess("verifyCompanyBankAccount", processResult2);

        Application.commitUnitOfWork();
        Application.beginUnitOfWork();

        return companyBankAccount;
    }

    public CompanyDTO getCompany1() {
        QuickbooksInfoDTO qbInfo = new QuickbooksInfoDTO();
        qbInfo.setApplicationId("Thunder");
        qbInfo.setApplicationVersion("17.00.R.9/20716#pro");
        qbInfo.setLicenseNumber("6487-4844-4441-476");
        qbInfo.setTaxTableId("848484");

        CompanyDTO comp = new CompanyDTO();
        comp.setDBA("QB Desktop Company 3");
        comp.setFein("242335465");
        comp.setLegalAddress(getTestLegalAddress());
        comp.setLegalName("QB Desktop 3");
        comp.setMailingAddress(getTestMailingAddress());
        comp.setNotificationEmail("notifications3@intuit.com");
        comp.setCompanyId("8574536");
        comp.setSourceSystemCd(SourceSystemCode.QBDT);
        comp.setContacts(getTestContacts());
        comp.setQuickBooksInfo(qbInfo);
        comp.setPriceType("Standard");

        return comp;
    }

    private AddressDTO getTestLegalAddress() {
        AddressDTO legalAddress = new AddressDTO();

        legalAddress.setAddressLine1("7789 Sierra Cnt Pkwy");
        legalAddress.setCity("Reno");
        legalAddress.setZipCode("89511");
        legalAddress.setState("NV");

        return legalAddress;
    }

    private AddressDTO getTestMailingAddress() {
        AddressDTO mailingAddress = new AddressDTO();

        mailingAddress.setAddressLine1("6887 Sierra Center Parkway");
        mailingAddress.setAddressLine2("Suite 48");
        mailingAddress.setAddressLine3("test line 3");
        mailingAddress.setCity("Reno");
        mailingAddress.setZipCode("89521");
        mailingAddress.setState("NV");

        return mailingAddress;
    }

    public Collection<ContactDTO> getTestContacts2() {
        Collection<ContactDTO> contacts = new ArrayList<ContactDTO>();

        // set up common contact address
        AddressDTO contactAddr = new AddressDTO();
        contactAddr.setAddressLine1("1244 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");

        // set up primary principal contact
        ContactDTO contact = new ContactDTO();
        contact.setFirstName("Steve");
        contact.setMiddleName("Q");
        contact.setLastName("PrimaryPrincipal2");
        contact.setPhoneNumber("(775) 111-1911");
        contact.setContactRoleCd(ContactRole.PrimaryPrincipal);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("PrimaryPrincipal2@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 1");
        contact.setFaxNumber("(775) 101-1901");
        contact.setSecondPhoneNumber("(775) 019-0110");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        // set up secondary principal contact
        contact = new ContactDTO();
        contact.setFirstName("Mark");
        contact.setMiddleName("Q");
        contact.setLastName("SecondaryPrincipal2");
        contact.setPhoneNumber("(775) 222-2292");
        contact.setContactRoleCd(ContactRole.SecondaryPrincipal);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("SecondaryPrincipal2@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 2");
        contact.setFaxNumber("(775) 292-2002");
        contact.setSecondPhoneNumber("(775) 920-0220");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        // set up payroll admin contact
        contact = new ContactDTO();
        contact.setFirstName("Frank");
        contact.setMiddleName("Q");
        contact.setLastName("PayrollAdmin2");
        contact.setPhoneNumber("(775) 379-3393");
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAccountSignatory(Boolean.FALSE);
        contact.setEmail("PayrollAdmin2@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 3");
        contact.setFaxNumber("(775) 303-3093");
        contact.setSecondPhoneNumber("(775) 090-0330");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        // set up 'other' contact
        contact = new ContactDTO();
        contact.setFirstName("Todd");
        contact.setMiddleName("Q");
        contact.setLastName("Other2");
        contact.setPhoneNumber("(775) 494-4944");
        contact.setContactRoleCd(ContactRole.Other);
        contact.setAccountSignatory(Boolean.FALSE);
        contact.setEmail("Other2@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 4");
        contact.setFaxNumber("(775) 494-4004");
        contact.setSecondPhoneNumber("(775) 090-0440");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        return contacts;
    }

    public Collection<ContactDTO> getTestContacts() {
        Collection<ContactDTO> contacts = new ArrayList<ContactDTO>();

        // set up common contact address
        AddressDTO contactAddr = new AddressDTO();
        contactAddr.setAddressLine1("1234 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");

        // set up primary principal contact
        ContactDTO contact = new ContactDTO();
        contact.setFirstName("Johnny");
        contact.setMiddleName("Q");
        contact.setLastName("PrimaryPrincipal");
        contact.setPhoneNumber("(775) 111-1111");
        contact.setContactRoleCd(ContactRole.PrimaryPrincipal);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("PrimaryPrincipal@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 1");
        contact.setFaxNumber("(775) 101-1001");
        contact.setSecondPhoneNumber("(775) 010-0110");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        // set up secondary principal contact
        contact = new ContactDTO();
        contact.setFirstName("Johnny");
        contact.setMiddleName("Q");
        contact.setLastName("SecondaryPrincipal");
        contact.setPhoneNumber("(775) 222-2222");
        contact.setContactRoleCd(ContactRole.SecondaryPrincipal);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("SecondaryPrincipal@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 2");
        contact.setFaxNumber("(775) 202-2002");
        contact.setSecondPhoneNumber("(775) 020-0220");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        // set up payroll admin contact
        contact = new ContactDTO();
        contact.setFirstName("Johnny");
        contact.setMiddleName("Q");
        contact.setLastName("PayrollAdmin");
        contact.setPhoneNumber("(775) 333-3333");
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAccountSignatory(Boolean.FALSE);
        contact.setEmail("PayrollAdmin@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 3");
        contact.setFaxNumber("(775) 303-3003");
        contact.setSecondPhoneNumber("(775) 030-0330");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        // set up 'other' contact
        contact = new ContactDTO();
        contact.setFirstName("Johnny");
        contact.setMiddleName("Q");
        contact.setLastName("Other");
        contact.setPhoneNumber("(775) 444-4444");
        contact.setContactRoleCd(ContactRole.Other);
        contact.setAccountSignatory(Boolean.FALSE);
        contact.setEmail("Other@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 4");
        contact.setFaxNumber("(775) 404-4004");
        contact.setSecondPhoneNumber("(775) 040-0440");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        return contacts;
    }

    public DDServiceInfoDTO getCompany1Service() {
        DDServiceInfoDTO ddCompanyService = new DDServiceInfoDTO();

        ddCompanyService.setAveragePayrollAmount(new BigDecimal("151.00"));
        ddCompanyService.setHighAnnualPayrollAmount(new BigDecimal("252.00"));

        return ddCompanyService;
    }

    public CompanyBankAccountDTO getCompany1BankAccount() {
        CompanyBankAccountDTO retBA = new CompanyBankAccountDTO();
        retBA.setCompanyBankAccountID("C1BA1");
        retBA.setSourceBankAccountName("BofA");

        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber("4847474747");
        bankAccountDTO.setAccountType(BankAccountType.Checking);
        bankAccountDTO.setBankName("Bank of America");

        if(mRoutingNumber != null)
            bankAccountDTO.setRoutingNumber(mRoutingNumber);
        else
            bankAccountDTO.setRoutingNumber("111000025");

        retBA.setBankAccountDTO(bankAccountDTO);

        return retBA;
    }

    public Employee getEmployee1(Company pCompany) {
        Employee incEmployee = new Employee();

        incEmployee.setSourceEmployeeId("EE1_1");
        incEmployee.setFirstName("ThirdCompEEFirst");
        incEmployee.setLastName("ThirdCompEELast");
        incEmployee.setMiddleName("TMI");
        incEmployee.setEmail("test3@testemail.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8015551212");
        incEmployee.setTaxId("111225333");
        incEmployee.setStatusCd(EmployeeStatus.Active);
        incEmployee.setStatusEffectiveDate(PSPDate.getPSPTime());
        incEmployee.setCompany(pCompany);

        return incEmployee;
    }

    public Employee getEmployee2(Company pCompany) {
        Employee incEmployee = new Employee();

        incEmployee.setSourceEmployeeId("EE2_1");
        incEmployee.setFirstName("ThirdCompEEFirstTwo");
        incEmployee.setLastName("ThirdCompEELastTwo");
        incEmployee.setMiddleName("TMI2");
        incEmployee.setEmail("test3@test32email.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8102551212");
        incEmployee.setTaxId("212223333");
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
        ProcessResult procResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBDT, company.getSourceCompanyId(), twoDay);
        PayrollServicesTest.assertSuccess("updateCompanyFundingModel", procResult);
    }

    public void updateTo5DayFundingModel() {
        FundingModel fiveDay = Application.findById(FundingModel.class, FundingModel.Codes.FIVE_DAY);
        ProcessResult procResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBDT, company.getSourceCompanyId(), fiveDay);
        PayrollServicesTest.assertSuccess("updateCompanyFundingModel", procResult);
    }

    public void updateLimits(SpcfMoney pNewLimit) {
        ProcessResult procResult = PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBDT, company.getSourceCompanyId(), pNewLimit, pNewLimit);
        PayrollServicesTest.assertSuccess("updateDDLimits", procResult);
    }

    public PayrollRunDTO get4thCompany2PR_DoesNotExceedLimits() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.OCTOBER, 15);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
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

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("800.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        employee2.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany3PR_ExceedsLimitsQualifiesForIncrease(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest06");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS6");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("16001.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        employee1.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS6");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("150.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        employee2.getSourceEmployeeId(),
                                        SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public Company getCompany() {
        return company;
    }
}
