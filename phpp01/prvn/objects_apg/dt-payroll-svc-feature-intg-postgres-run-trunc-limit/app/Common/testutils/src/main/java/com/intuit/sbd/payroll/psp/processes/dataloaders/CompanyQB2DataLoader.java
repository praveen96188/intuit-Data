package com.intuit.sbd.payroll.psp.processes.dataloaders;

/*
 * $Id: //psp/dev/Common/TestUtils/src/com/intuit/sbd/payroll/psp/processes/dataloaders/CompanyQB2DataLoader.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

public class CompanyQB2DataLoader {

    public static final String COMPANY_PSID = "8774536";
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
    private String sourceCoId = null;

    public Company persistQBCompany1() {
        return persistQBCompany1(COMPANY_PSID);
    }

    public Company persistQBCompany1(String pPsid) {

        sourceCoId = pPsid;
        // Create Company and CompanyBankAccount
        CompanyDTO company1 = getCompany1();
        // Set QBDT next ids
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");

        company1.getLegalAddress().setCity("Honolulu");
        company1.getLegalAddress().setState("HI");
        company1.getLegalAddress().setZipCode("96813");

        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();

        qbInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
        qbInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);

        company1.setQuickBooksInfo(qbInfoDTO);
        company = dataloader.persistCompany(company1);
        CompanyService ddCompanyService = dataloader.persistCompanyService(company, getCompany1Service());

        DataLoader dl = new DataLoader();
        bankAccount1 = dl.persistCompanyBankAccount(company, getCompany1BankAccount());
        persistCompanyPIN();
        employee1 = persistEmployee(getEmployee1(company));
        employee2 = persistEmployee(getEmployee2(company));
        company = Company
                .findCompany(sourceCoId, SourceSystemCode.QBDT);
        employee2 = Employee.findEmployee(company, "EE2_1");

        eeba2 = persistEEBA(company, employee2, getEmployee2BankAccount(employee2));

        company = Company
                .findCompany(sourceCoId, SourceSystemCode.QBDT);
        employee1 = Employee.findEmployee(company, "EE1_1");

        eeba1 = persistEEBA(company, employee1, getEmployee1BankAccount(employee1));

        company = Company
                .findCompany(sourceCoId, SourceSystemCode.QBDT);

        return company;
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
        eeba2_2 = persistEEBA(company, employee2, eebaDTO2);
    }

    public void reverseSingleTransactionInPayrollChargeFee(String pPayrollRunID, String pDDTransactionId) {
        ArrayList<String> ddTxnList = new ArrayList<String>();
        ddTxnList.add(pDDTransactionId);

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setSourcePayrollRunId(pPayrollRunID);
        txnReverseDTO.setDdTransactionIdList(ddTxnList);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        txnReverseDTO.setChargeFee(true);

        Application.beginUnitOfWork();
        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txnReverseDTO);
        Application.commitUnitOfWork();
        assertSuccess(result);
    }

    public PayrollRun persistPayrollRun(PayrollRunDTO pPayrollRunDTO) {
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, sourceCoId, pPayrollRunDTO);
        PayrollServicesTest.assertSuccess("submitPayroll", submitPayrollResult);
        return submitPayrollResult.getResult();
    }

    public PayrollRunDTO getCompany3PR_DoesNotExceedLimits() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();


        payrollDate.set(2007, Calendar.NOVEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        //  Set Batch Id
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

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8388.88"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO get2ndCompany2PR_DoesNotExceedLimits() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();


        payrollDate.set(2007, Calendar.OCTOBER, 10);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

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

    public PayrollRunDTO get3rdCompany2PR_DoesNotExceedLimits() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();


        payrollDate.set(2007, Calendar.NOVEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

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

    public PayrollRunDTO get3rdCompany2PR_ExceedsOldLimits() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();


        payrollDate.set(2007, Calendar.NOVEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest10");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("250.00"));
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

    public PayrollRunDTO getCompany1PR_MultiplePaychecksSameEE() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();


        payrollDate.set(2007, Calendar.OCTOBER, 2);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

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

    public PayrollRunDTO getCompany1PR_MultiplePaycheckSplitsSameEE() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();

        payrollDate.set(2007, Calendar.OCTOBER, 2);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

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

    public PayrollRunDTO getCompany1PayrollRunDTO() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();


        payrollDate.set(2007, Calendar.OCTOBER, 2);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

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

    public PayrollRunDTO getPayrollRunNOC_NoBankAccountChange() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();


        payrollDate.set(2007, Calendar.NOVEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId(SpcfUniqueId.generateRandomUniqueIdString());

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(getEmployee1BankAccount(employee1));
        ee1PaycheckSplit1.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("2.23"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(getEmployee2BankAccount(employee2));
        ee2PaycheckSplit1.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8.88"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getPayrollRunNOC_NoEEWithNOC() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();


        payrollDate.set(2007, Calendar.NOVEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId(SpcfUniqueId.generateRandomUniqueIdString());

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();

        Collection<DDTransactionDTO> ee2Txns = new ArrayList();


        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(getEmployee2BankAccount(employee2));
        ee2PaycheckSplit1.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("127.88"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }


    public PayrollRunDTO getPayrollRunNOC_EEBankAccountChanged() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();


        payrollDate.set(2007, Calendar.NOVEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId(SpcfUniqueId.generateRandomUniqueIdString());

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(getEmployee1NewBankAccount());
        ee1PaycheckSplit1.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("122.23"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(getEmployee2NewBankAccount());
        ee2PaycheckSplit1.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("188.88"));
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

     private EmployeeBankAccountDTO getEmployee1NewBankAccount() {
        // Initialize the EmployeeBankAccount
        EmployeeBankAccountDTO eeba = new EmployeeBankAccountDTO();
        BankAccountDTO ba = new BankAccountDTO();

        ba.setAccountNumber("12345678");
        ba.setRoutingNumber("111000025");
        ba.setBankName("abc bank");
        ba.setAccountType(BankAccountType.Checking);

        eeba.setEmployeeBankAccountId("EEBA1");
        eeba.setBankAccount(ba);
        return eeba;
    }

    private EmployeeBankAccountDTO getEmployee2NewBankAccount() {
        // Initialize the EmployeeBankAccount
        EmployeeBankAccountDTO eeba = new EmployeeBankAccountDTO();
        BankAccountDTO ba = new BankAccountDTO();

        ba.setAccountNumber("272727");
        ba.setRoutingNumber("111000025");
        ba.setBankName("abc bank");
        ba.setAccountType(BankAccountType.Checking);

        eeba.setEmployeeBankAccountId("EEBA2");
        eeba.setBankAccount(ba);
        return eeba;
    }

    private EmployeeBankAccount persistEEBA(Company pCompany, Employee pEmployee, EmployeeBankAccountDTO pEEBA) {
        ProcessResult<EmployeeBankAccount> procResult = PayrollServices.employeeManager.addEmployeeBankAccount(
                pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pEmployee.getSourceEmployeeId(), pEEBA);
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

    public CompanyDTO getCompany1() {

        CompanyDTO comp = new CompanyDTO();
        comp.setDBA("QB Desktop Company 2");
        comp.setFein("243335465");
        comp.setLegalAddress(getTestLegalAddress());
        comp.setLegalName("QB Desktop 2");
        comp.setMailingAddress(getTestMailingAddress());
        comp.setNotificationEmail("notifications2@intuit.com");
        comp.setCompanyId("8774536");

        QuickbooksInfoDTO qbInfo = new QuickbooksInfoDTO();
        qbInfo.setApplicationId("Thunder");
        qbInfo.setApplicationVersion("17.00.R.9/20716#pro");
        qbInfo.setLicenseNumber("8487-4844-4441-476");
        qbInfo.setTaxTableId("848484");
        comp.setQuickBooksInfo(qbInfo);

        ContactDTO contact = getTestContact();
        Collection<ContactDTO> allContactsForCompany = new ArrayList();
        allContactsForCompany.add(contact);
        comp.setContacts(allContactsForCompany);
        comp.setSourceSystemCd(SourceSystemCode.QBDT);
        comp.setPriceType("Standard");

        return comp;
    }


    private AddressDTO getTestLegalAddress() {
        AddressDTO legalAddress = new AddressDTO();
        legalAddress.setAddressLine1("7788 Sierra Cnt Pkwy");
        legalAddress.setCity("Reno");
        legalAddress.setZipCode("89511");
        legalAddress.setState("NV");
        return legalAddress;
    }

    private AddressDTO getTestMailingAddress() {
        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("6887 Sierra Center Parkway");
        mailingAddress.setAddressLine2("Suite 49");
        mailingAddress.setAddressLine3("test line 3");
        mailingAddress.setCity("Reno");
        mailingAddress.setZipCode("89521");
        mailingAddress.setState("NV");
        return mailingAddress;
    }

    public ContactDTO getTestContact() {
        ContactDTO contact = new ContactDTO();

        contact.setFirstName("JohnnoTwo");
        contact.setMiddleName("P");
        contact.setLastName("Doeyy");
        contact.setPhoneNumber("(775) 424-9339");
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("someEmail1234_2@aol.com");

        AddressDTO contactAddr = new AddressDTO();
        contactAddr.setAddressLine1("12345 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));
        return contact;
    }

    public DDServiceInfoDTO getCompany1Service() {
        DDServiceInfoDTO ddCompanyService = new DDServiceInfoDTO();

        ddCompanyService.setAveragePayrollAmount(new BigDecimal("151.00"));

        ddCompanyService.setHighAnnualPayrollAmount(new BigDecimal("252.00"));

        return ddCompanyService;
    }

    public CompanyBankAccountDTO getCompany1BankAccount() {
        CompanyBankAccountDTO retBA = new CompanyBankAccountDTO();
        retBA.setCompanyBankAccountID("QBDTC2BA1");
        retBA.setSourceBankAccountName("BankofA");

        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber("5767676878");
        bankAccountDTO.setAccountType(BankAccountType.Checking);
        bankAccountDTO.setBankName("Bank of America");
        bankAccountDTO.setRoutingNumber("111000025");

        retBA.setBankAccountDTO(bankAccountDTO);
        return retBA;
    }

    public Employee getEmployee1(Company pCompany) {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("EE1_1");
        incEmployee.setFirstName("QB2CompEEFirst");
        incEmployee.setLastName("QB2CompEELast");
        incEmployee.setMiddleName("TMI22");
        incEmployee.setEmail("test2_abc@testemail.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8016551212");
        incEmployee.setTaxId("111325333");
        incEmployee.setStatusCd(EmployeeStatus.Active);
        incEmployee.setStatusEffectiveDate(PSPDate.getPSPTime());
        incEmployee.setCompany(pCompany);
        return incEmployee;
    }

    public Employee getEmployee2(Company pCompany) {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("EE2_1");
        incEmployee.setFirstName("QB2CompEEFirstTwo");
        incEmployee.setLastName("QB2CompEELastTwo");
        incEmployee.setMiddleName("TMI222");
        incEmployee.setEmail("test3_def@test32email.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8102551112");
        incEmployee.setTaxId("212223343");
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
        ProcessResult procResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBDT,
                company.getSourceCompanyId(), twoDay);
        PayrollServicesTest.assertSuccess("updateCompanyFundingModel", procResult);
    }

    public void updateTo5DayFundingModel() {
        FundingModel fiveDay = Application.findById(FundingModel.class, FundingModel.Codes.FIVE_DAY);
        ProcessResult procResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBDT,
                company.getSourceCompanyId(), fiveDay);
        PayrollServicesTest.assertSuccess("updateCompanyFundingModel", procResult);
    }

    public void updateLimits(SpcfMoney pNewLimit) {
        ProcessResult procResult = PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBDT,
                company.getSourceCompanyId(), pNewLimit, pNewLimit);
        PayrollServicesTest.assertSuccess("updateDDLimits", procResult);
    }

    public PayrollRunDTO get4thCompany2PR_DoesNotExceedLimits() {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();

        payrollDate.set(2007, Calendar.OCTOBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

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
}