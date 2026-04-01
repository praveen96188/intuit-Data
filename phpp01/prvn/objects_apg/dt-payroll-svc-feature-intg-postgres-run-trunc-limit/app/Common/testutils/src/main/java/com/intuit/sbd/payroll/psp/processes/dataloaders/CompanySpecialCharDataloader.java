/*
 * $Id: //psp/dev/Common/TestUtils/src/com/intuit/sbd/payroll/psp/processes/dataloaders/CompanySpecialCharDataloader.java#3 $
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
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

public class CompanySpecialCharDataloader {

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

    public Company persistCompany1() {
        // Create Company and CompanyBankAccount
        company = dataloader.persistCompany(getCompany1());

        CompanyService ddCompanyService = dataloader.persistCompanyService(company, getCompany1Service());

        bankAccount1 = dataloader.persistCompanyBankAccount(company, getCompany1BankAccount());

        employee1 = persistEmployee(getEmployee1(company));
        employee2 = persistEmployee(getEmployee2(company));
        company = Company
                .findCompany("1234599", SourceSystemCode.QBOE);
        employee2 = Employee.findEmployee(company, "EE2");

        eeba2 = persistEEBA(company, employee2, getEmployee2BankAccount(employee2));

        company = Company
                .findCompany("1234599", SourceSystemCode.QBOE);
        employee1 = Employee.findEmployee(company, "EE1");

        eeba1 = persistEEBA(company, employee1, getEmployee1BankAccount());

        company = Company
                .findCompany("1234599", SourceSystemCode.QBOE);

        return company;
    }

    public void persistEmployee2BankAccount2() {
        //Get employee 1's bank account info, and add EE2 to it, just with a different source bank account id
        EmployeeBankAccountDTO eebaDTO2 = getEmployee1BankAccount();
        eebaDTO2.setEmployeeBankAccountId("EEBA2_2");
        eeba2_2 = persistEEBA(company, employee2, eebaDTO2);
    }

    public void persistEmployee3BankAccount1() {
        employee3 = persistEmployee(getEmployee3(company));
        company = Company
                .findCompany("1234599", SourceSystemCode.QBOE);
        employee3 = Employee.findEmployee(company, "EE3");

        EmployeeBankAccountDTO eebaDTO3 = getEmployee1BankAccount();
        eebaDTO3.setEmployeeBankAccountId("EEBA3");
        eeba3 = persistEEBA(company, employee3, eebaDTO3);
    }

    public void persistEmployee4BankAccount1() {
        employee4 = persistEmployee(getEmployee4(company));
        company = Company
                .findCompany("1234599", SourceSystemCode.QBOE);
        employee4 = Employee.findEmployee(company, "EE4");

        EmployeeBankAccountDTO eebaDTO4 = getEmployee1BankAccount();
        eebaDTO4.setEmployeeBankAccountId("EEBA4");
        eeba4 = persistEEBA(company, employee4, eebaDTO4);
    }

    public void persistEmployeeBankAccount() {
        employee5 = persistEmployee(getEmployee(company, "5"));
        employee6 = persistEmployee(getEmployee(company, "6"));
        employee7 = persistEmployee(getEmployee(company, "7"));
        employee8 = persistEmployee(getEmployee(company, "8"));
        company = Company
                .findCompany("1234599", SourceSystemCode.QBOE);
        employee5 = Employee.findEmployee(company, "EE5");
        employee6 = Employee.findEmployee(company, "EE6");
        employee7 = Employee.findEmployee(company, "EE7");
        employee8 = Employee.findEmployee(company, "EE8");

        EmployeeBankAccountDTO eebaDTO5 = getEmployee1BankAccount2(employee5);
        EmployeeBankAccountDTO eebaDTO6 = getEmployee1BankAccount2(employee6);
        EmployeeBankAccountDTO eebaDTO7 = getEmployee1BankAccount2(employee7);
        EmployeeBankAccountDTO eebaDTO8 = getEmployee1BankAccount2(employee8);
        eebaDTO5.setEmployeeBankAccountId("EEBA5");
        eebaDTO6.setEmployeeBankAccountId("EEBA6");
        eebaDTO7.setEmployeeBankAccountId("EEBA7");
        eebaDTO8.setEmployeeBankAccountId("EEBA8");

        eeba5 = persistEEBA(company, employee4, eebaDTO5);
        eeba6 = persistEEBA(company, employee4, eebaDTO6);
        eeba7 = persistEEBA(company, employee4, eebaDTO7);
        eeba8 = persistEEBA(company, employee4, eebaDTO8);
    }

    public static PayrollRun persistPayrollRun(PayrollRunDTO pPayrollRunDTO) {
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234599", pPayrollRunDTO);
        PayrollServicesTest.assertSuccess("submit payroll", submitPayrollResult);
        return submitPayrollResult.getResult();
    }

    public void persistStrike_LessThan1YearAgo() {
        company = Company
                .findCompany("1234599", SourceSystemCode.QBOE);

        CompanyEvent strikeEvent = CompanyEvent.createStrikeEvent(company, StrikeReason.Manual, "Manual Strike", PSPDate.getPSPTime(), new DomainEntitySet<FinancialTransaction>());
    }

    public void persistStrike_MoreThan1YearAgo() {
        company = Company
                .findCompany("1234599", SourceSystemCode.QBOE);

        SpcfCalendar strikeDate = PSPDate.getPSPTime().copy();
        strikeDate.addMonths(-13);
        CompanyEvent strikeEvent = CompanyEvent.createStrikeEvent(company, StrikeReason.Manual, "Manual Strike", strikeDate, new DomainEntitySet<FinancialTransaction>());
    }

    public PayrollRunDTO getCompany1PR_FutureExceedsLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest04");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS4");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("1001.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS4");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("150.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_ExistingExceedsLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest03");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS3");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("1001.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS3");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("15000.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_ExistingPR(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest02");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("15000.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("150.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    /**
     * Dataloader to load multiple paycheck splits for same bank accounts on the same date
     *
     * @return
     */
    public PayrollRunDTO getCompany1PR_MultiplePaycheckSplitsDifferentBA(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest00");

        // Create Paychecks and Paycheck Splits for bank account "12345" and routing number "111000025"
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();
        Collection<DDTransactionDTO> ee5Txns = new ArrayList();
        Collection<DDTransactionDTO> ee6Txns = new ArrayList();
        Collection<DDTransactionDTO> ee7Txns = new ArrayList();
        Collection<DDTransactionDTO> ee8Txns = new ArrayList();


        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("15000.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("150.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        // add another four paychecks with other bank account
        DDTransactionDTO ee5PaycheckSplit1 = new DDTransactionDTO();
        ee5PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba5));
        ee5PaycheckSplit1.setDDTransactionId("EEBA5PS1");
        ee5PaycheckSplit1.setDDTransactionAmount(new BigDecimal("1000.00"));
        ee5Txns.add(ee5PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee5Txns, employee5.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee6PaycheckSplit1 = new DDTransactionDTO();
        ee6PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba6));
        ee6PaycheckSplit1.setDDTransactionId("EEBA6PS1");
        ee6PaycheckSplit1.setDDTransactionAmount(new BigDecimal("1100.00"));
        ee6Txns.add(ee6PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee6Txns, employee6.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee7PaycheckSplit1 = new DDTransactionDTO();
        ee7PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba7));
        ee7PaycheckSplit1.setDDTransactionId("EEBA7PS1");
        ee7PaycheckSplit1.setDDTransactionAmount(new BigDecimal("1200.00"));
        ee7Txns.add(ee7PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee7Txns, employee7.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee8PaycheckSplit1 = new DDTransactionDTO();
        ee8PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba8));
        ee8PaycheckSplit1.setDDTransactionId("EEBA8PS1");
        ee8PaycheckSplit1.setDDTransactionAmount(new BigDecimal("1300.00"));
        ee8Txns.add(ee8PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee8Txns, employee8.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_SameBASameCheckDate(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest02");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee3Txns = new ArrayList();
        Collection<DDTransactionDTO> ee4Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2_2));
        ee1PaycheckSplit1.setDDTransactionId("EEBA22PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("14000.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee3PaycheckSplit1 = new DDTransactionDTO();
        ee3PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba3));
        ee3PaycheckSplit1.setDDTransactionId("EEBA3PS2");
        ee3PaycheckSplit1.setDDTransactionAmount(new BigDecimal("150.00"));
        ee3Txns.add(ee3PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee3Txns, employee3.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee4PaycheckSplit1 = new DDTransactionDTO();
        ee4PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba4));
        ee4PaycheckSplit1.setDDTransactionId("EEBA4PS2");
        ee4PaycheckSplit1.setDDTransactionAmount(new BigDecimal("150.00"));
        ee4Txns.add(ee4PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee4Txns, employee4.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_ExceedsLimits(DateDTO pPayrollDate) {
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
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("300000.00"));
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

    public void reverseSingleTransactionInPayroll(String pPayrollRunID, String pDDTransactionId) {
        ArrayList<String> ddTxnList = new ArrayList<String>();
        ddTxnList.add(pDDTransactionId);

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setSourcePayrollRunId(pPayrollRunID);
        txnReverseDTO.setDdTransactionIdList(ddTxnList);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        Application.beginUnitOfWork();
        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), txnReverseDTO);
        Application.commitUnitOfWork();
        assertSuccess(result);
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
        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), txnReverseDTO);
        Application.commitUnitOfWork();
        assertSuccess(result);
    }

    public void reverseEntirePayroll(String pPayrollRunID) {
        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setSourcePayrollRunId(pPayrollRunID);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), txnReverseDTO);
        assertSuccess(result);
    }

    public void reverseEntirePayroll_IntuitInitiated(String pPayrollRunID) {
        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setSourcePayrollRunId(pPayrollRunID);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        txnReverseDTO.setIntuitInitiatedReversals(true);

        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), txnReverseDTO);
        assertSuccess(result);
    }

    public void reverseEntirePayrollReversalsPending(String pPayrollRunID) {
        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setSourcePayrollRunId(pPayrollRunID);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        txnReverseDTO.setIntuitInitiatedReversals(true);

        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), txnReverseDTO);
        assertSuccess(result);
    }

    public PayrollRunDTO getCompany1PR_DoesNotExceedLimits(DateDTO pPayrollDate) {
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
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("30.00"));
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

    public PayrollRunDTO getCompany1PR2_DoesNotExceedLimits(DateDTO pPayrollDate) {
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
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("100.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("150.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_ExceedsLimitsQualifiesForIncrease(DateDTO pPayrollDate) {
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

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS6");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("15001.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS6");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("150.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PayrollRunDTO_MinSuspectPayrollAmount(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("Batch01");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("100.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("149.00"));
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

    public EmployeeBankAccountDTO getEmployee1BankAccount() {
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

    public EmployeeBankAccountDTO getEmployee2BankAccount(Employee pEmployee) {
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

    private EmployeeBankAccountDTO getEmployee1BankAccount2(Employee pEmployee) {
        // Initialize the EmployeeBankAccount
        EmployeeBankAccountDTO eeba = new EmployeeBankAccountDTO();
        BankAccountDTO ba = new BankAccountDTO();

        ba.setAccountNumber("11111");
        ba.setRoutingNumber("111000025");
        ba.setBankName("abc bank2");
        ba.setAccountType(BankAccountType.Checking);

        eeba.setEmployeeBankAccountId("EEBA1");
        eeba.setBankAccount(ba);
        return eeba;
    }

    public EmployeeBankAccount persistEEBA(Company pCompany, Employee pEmployee, EmployeeBankAccountDTO pEEBA) {
        ProcessResult<EmployeeBankAccount> procResult = PayrollServices.employeeManager.addEmployeeBankAccount(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pEmployee.getSourceEmployeeId(), pEEBA);
        PayrollServicesTest.assertSuccess("addEmployeeBankAccount", procResult);
        return procResult.getResult();
    }

    public Employee persistEmployee(Employee pEmployee) {
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
        ProcessResult<Employee> procResult = PayrollServices.employeeManager.addEmployee(
                pSourceSystemCd, pSourceEmployeeId, pEmployee);
        PayrollServicesTest.assertSuccess("addEmployee", procResult);
        return procResult.getResult();
    }

    private CompanyBankAccount persistCompanyBankAccount(Company pCompany, CompanyBankAccountDTO pCompanyBankAccount) {
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pCompanyBankAccount, true, true);
        Assert.assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount accountToActivate = CompanyBankAccount.findCompanyBankAccount(company,
                processResult.getResult().getSourceBankAccountId());
        company = Company
                .findCompany("1234599", SourceSystemCode.QBOE);

        accountToActivate.setStatusCd(BankAccountStatus.Active);

        accountToActivate = PayrollServicesTest.save(accountToActivate);

        return accountToActivate;
    }

    //????????????????????????????????????????????????????????????????
    public CompanyDTO getCompany1() {
        CompanyDTO comp = new CompanyDTO();
        comp.setDBA("DAWN SPECIAL CHAR COMPANY");
        comp.setFein("123456289");
        comp.setLegalAddress(getTestLegalAddress());
        comp.setLegalName("¿¡¬√ƒ≈«»… ÀÃÕŒœ–—“”‘’÷");
        comp.setMailingAddress(getTestMailingAddress());
        comp.setNotificationEmail("notifications@intuit.com");
        comp.setCompanyId("1234599");
        comp.setSignUpDate(new DateDTO("2007-09-03"));

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
        legalAddress.setAddressLine1("6888 Sierra Cnt Pkwy");
        legalAddress.setCity("Reno");
        legalAddress.setZipCode("89511");
        legalAddress.setState("NV");
        return legalAddress;
    }

    private AddressDTO getTestMailingAddress() {
        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("6887 Sierra Center Parkway");
        mailingAddress.setAddressLine2("Suite 45");
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
        contact.setLastName("Doe");
        contact.setPhoneNumber("(775) 424-8339");
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

    //????????????????????????????????????????????????????????????????
    public CompanyBankAccountDTO getCompany1BankAccount() {
        CompanyBankAccountDTO retBA = new CompanyBankAccountDTO();
        retBA.setCompanyBankAccountID("123123");

        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber("12345998901234599");
        bankAccountDTO.setAccountType(BankAccountType.Checking);
        bankAccountDTO.setBankName("Bank of America");
        bankAccountDTO.setRoutingNumber("111000025");

        retBA.setBankAccountDTO(bankAccountDTO);
        return retBA;
    }

    //????????????????????????????????????????????????????????????????
    public Employee getEmployee1(Company pCompany) {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("EE1");
        incEmployee.setFirstName("???????");
        incEmployee.setLastName("»œŒÕÃ–—“”‘‘’?÷Ÿ⁄€‹›˛›");
        incEmployee.setMiddleName("???????????");
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
        incEmployee.setSourceEmployeeId("EE2");
        incEmployee.setFirstName("€‹€‹›˛ﬂ");
        incEmployee.setLastName("?‘€‹›˛›");
        incEmployee.setMiddleName("‹€›S");
        incEmployee.setEmail("test2@test2email.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8002551212");
        incEmployee.setTaxId("222223333");
        incEmployee.setStatusCd(EmployeeStatus.Active);
        incEmployee.setStatusEffectiveDate(SpcfCalendar.getNow());
        incEmployee.setCompany(pCompany);
        return incEmployee;
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

    public Employee getEmployee4(Company pCompany) {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("EE4");
        incEmployee.setFirstName("FirstNameOfEE4");
        incEmployee.setLastName("TestLastName4");
        incEmployee.setMiddleName("TMI4");
        incEmployee.setEmail("test4@test3email.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8002551214");
        incEmployee.setTaxId("222223344");
        incEmployee.setStatusCd(EmployeeStatus.Active);
        incEmployee.setStatusEffectiveDate(SpcfCalendar.getNow());
        incEmployee.setCompany(pCompany);
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

    public ServiceBankAccountDTO createServiceBankAccountDTO(CompanyBankAccountDTO pCompanyBankAccountDTO,
                                                             ServiceCode pServiceCode) {
        ServiceBankAccountDTO serviceBankAccountDTO = new ServiceBankAccountDTO();
        serviceBankAccountDTO.setCompanyBankAccount(pCompanyBankAccountDTO);
        serviceBankAccountDTO.setServiceCode(pServiceCode);
        return serviceBankAccountDTO;
    }

    public PayrollRunDTO getCompany1PR_OneBAExceedsLimits(DateDTO pPayrollDate) {
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

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS1");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8000.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2_2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("9000.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_OneBAExceedsLimitsThreeEEs(DateDTO pPayrollDate) {
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
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8000.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2_2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("9000.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee3PaycheckSplit1 = new DDTransactionDTO();
        ee3PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba3));
        ee3PaycheckSplit1.setDDTransactionId("EEBA3PS1");
        ee3PaycheckSplit1.setDDTransactionAmount(new BigDecimal("9000.00"));
        ee3Txns.add(ee3PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee3Txns, employee3.getSourceEmployeeId(), SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_OneBAFourEEs(DateDTO pPayrollDate) {
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
        Collection<DDTransactionDTO> ee4Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS1");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8000.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2_2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("9000.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee3PaycheckSplit1 = new DDTransactionDTO();
        ee3PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba3));
        ee3PaycheckSplit1.setDDTransactionId("EEBA3PS1");
        ee3PaycheckSplit1.setDDTransactionAmount(new BigDecimal("2000.00"));
        ee3Txns.add(ee3PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee3Txns, employee3.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee4PaycheckSplit1 = new DDTransactionDTO();
        ee4PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba4));
        ee4PaycheckSplit1.setDDTransactionId("EEBA4PS1");
        ee4PaycheckSplit1.setDDTransactionAmount(new BigDecimal("2000.00"));
        ee4Txns.add(ee4PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee4Txns, employee4.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }


    public PayrollRunDTO getCompany1PR_OneBAThreeEEs(DateDTO pPayrollDate) {
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
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8000.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2_2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("9000.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee3PaycheckSplit1 = new DDTransactionDTO();
        ee3PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2_2));
        ee3PaycheckSplit1.setDDTransactionId("EEBA3PS1");
        ee3PaycheckSplit1.setDDTransactionAmount(new BigDecimal("2000.00"));
        ee3Txns.add(ee3PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee3Txns, employee3.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PR_OneBAThreeEEsOneNull(DateDTO pPayrollDate) {
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
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8000.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2_2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("9000.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee3PaycheckSplit1 = new DDTransactionDTO();
        ee3PaycheckSplit1.setDDTransactionId("EEBA3PS1");
        ee3PaycheckSplit1.setDDTransactionAmount(new BigDecimal("2000.00"));
        ee3Txns.add(ee3PaycheckSplit1);
        ee3PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTONoBADTO(eeba3));
        paychecks.add(createPaycheckDTO(ee3Txns, employee3.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public void setCompany1(Company pCompany) {
        company = pCompany;
    }

    public Company getCompany() {
        return company;
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

    public PayrollRunDTO get3rdCompany1PR_DoesNotExceedLimits(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(bankAccount1);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest81");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("765.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns, employee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(eeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("343.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns, employee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public void returnERDDDB(String pPayrollRunID, String pReturnCode) {

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        Company company = Company.findCompany("1234599??????????????", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pPayrollRunID);

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        DomainEntitySet<FinancialTransaction> eeFinancialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(financialTxs,
                pReturnCode,
                "This is an ER Return");

        junit.framework.Assert.assertEquals("Number of txn returns", 1, returnList.size());

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
    }

    public void addFee1() {
        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 11, 3, SpcfTimeZone.getLocalTimeZone()));
        Date txDate = calendar.getTime();
        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(), "BatchTest05",
                SettlementTypeDTO.Wire, txDate, new SpcfMoney("50.00"),
                OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServicesTest.assertSuccess("addFeeTransaction", processResult);
    }

    public void deactivateEE1BA1() {
        ProcessResult result = PayrollServices.employeeManager.deactivateEmployeeBankAccount(SourceSystemCode.QBOE, "1234599??????????????", "EE1", getEmployee1BankAccount());
        assertSuccess("Deactivated EE1BA", result);
    }

    public void addEE1BAMatchesCoBA() {
        EmployeeBankAccountDTO eeba1DTO = getEmployee1BankAccount();
        eeba1DTO.setBankAccount(dataloader.getTestCompanyBankAccount().getBankAccountDTO());
        eeba1 = persistEEBA(company, employee1, eeba1DTO);
    }
}