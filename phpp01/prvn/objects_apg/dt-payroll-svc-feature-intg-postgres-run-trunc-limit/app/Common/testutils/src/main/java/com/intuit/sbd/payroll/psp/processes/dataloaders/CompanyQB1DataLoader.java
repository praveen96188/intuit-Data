package com.intuit.sbd.payroll.psp.processes.dataloaders;

/*
 * $Id: //psp/dev/Common/TestUtils/src/com/intuit/sbd/payroll/psp/processes/dataloaders/CompanyQB1DataLoader.java#4 $
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
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

public class CompanyQB1DataLoader {
    public static final String COMPANY_PSID = "8574536";
    public static final String COMPANY_FEIN = "242335465";

    private Company mCompany;
    private CompanyBankAccount nCba1;
    private Employee mEmployee1;
    private Employee mEmployee2;
    private EmployeeBankAccount mEeba1;
    private EmployeeBankAccount mEeba2;
    private String mFein = COMPANY_FEIN;
    private String mSourceCoId = COMPANY_PSID;
    private String mRoutingNumber = null;

    private DataLoader dataloader = new DataLoader();

    public Company getCompany() {
        return mCompany;
    }

    public void setCompany(Company pCompany) {
        mCompany = pCompany;
    }

    public Company persistQBCompany1() {
        return persistQBCompany1(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }
    public Company persistQBCompanySymphony() {
        return persistQBCompany1(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(OffloadGroup.Codes.PSPOFFLOADS));
    }

    public Company persistQBCompany1(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();
        qbInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
        qbInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);

        // Create Company and CompanyBankAccount
        CompanyDTO companyDTO = getCompany1();
        companyDTO.setNextEmployeeId("1");
        companyDTO.setNextPaycheckId("1");
        companyDTO.setNextPayrollItemId("1");
        companyDTO.setNextPayrollTransactionId("1");
        companyDTO.getLegalAddress().setCity("Honolulu");
        companyDTO.getLegalAddress().setState("HI");
        companyDTO.getLegalAddress().setZipCode("96813");
        companyDTO.setQuickBooksInfo(qbInfoDTO);
        companyDTO.setPriceType("Standard");

        mCompany = dataloader.persistCompany(companyDTO, assetItemNumber, offloadGroup);

        dataloader.persistCompanyService(mCompany, getCompany1Service());

        nCba1 = dataloader.persistCompanyBankAccount(mCompany, getCompany1BankAccount(), offloadGroup);

        persistCompanyPIN();

        mEmployee1 = persistEmployee(getEmployee1(mCompany));
        mEmployee2 = persistEmployee(getEmployee2(mCompany));

        mEeba1 = persistEEBA(mCompany, mEmployee1, getEmployee1BankAccount(mEmployee1));
        mEeba2 = persistEEBA(mCompany, mEmployee2, getEmployee2BankAccount(mEmployee2));

        return mCompany;
    }

    public Company persistQBCompany1(String pPsid) {
        mSourceCoId = pPsid;
        return persistQBCompany1();
    }

    public Company persistQBCompany1(String pPsid, String pFein) {
        mFein = pFein;
        return persistQBCompany1(pPsid);
    }

    public Company persistQBCompany1NoVerify(String pPsid) {
        mSourceCoId = pPsid;

        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();
        qbInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
        qbInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);

        // Create Company and CompanyBankAccount
        CompanyDTO companyDTO = getCompany1();
        companyDTO.setNextEmployeeId("1");
        companyDTO.setNextPaycheckId("1");
        companyDTO.setNextPayrollItemId("1");
        companyDTO.setNextPayrollTransactionId("1");
        companyDTO.getLegalAddress().setCity("Honolulu");
        companyDTO.getLegalAddress().setState("HI");
        companyDTO.getLegalAddress().setZipCode("96813");
        companyDTO.setQuickBooksInfo(qbInfoDTO);

        mCompany = dataloader.persistCompany(companyDTO);

        dataloader.persistCompanyService(mCompany, getCompany1Service());

        Application.commitUnitOfWork();

        nCba1 = dataloader.persistCompanyBankAccountNoVerify(mCompany, getCompany1BankAccount());

        Application.beginUnitOfWork();

        return mCompany;
    }

     public Company persistCompanyCOABlank(String pPsid, String pFeeAcct, String pSalesTaxAcct ) {
        mSourceCoId = pPsid;

        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();

        qbInfoDTO.setCoaFeeAccountName(pFeeAcct);
        qbInfoDTO.setCoaSalesTaxAccountName(pSalesTaxAcct);

        // Create Company and CompanyBankAccount
        CompanyDTO companyDTO = getCompany1();
        companyDTO.setNextEmployeeId("1");
        companyDTO.setNextPaycheckId("1");
        companyDTO.setNextPayrollItemId("1");
        companyDTO.setNextPayrollTransactionId("1");
        companyDTO.getLegalAddress().setCity("Honolulu");
        companyDTO.getLegalAddress().setState("HI");
        companyDTO.getLegalAddress().setZipCode("96813");
        companyDTO.setQuickBooksInfo(qbInfoDTO);

        mCompany = dataloader.persistCompany(companyDTO);

        dataloader.persistCompanyService(mCompany, getCompany1Service());

        nCba1 = dataloader.persistCompanyBankAccount(mCompany, getCompany1BankAccount());

        persistCompanyPIN();

        mEmployee1 = persistEmployee(getEmployee1(mCompany));
        mEmployee2 = persistEmployee(getEmployee2(mCompany));

        mEeba1 = persistEEBA(mCompany, mEmployee1, getEmployee1BankAccount(mEmployee1));
        mEeba2 = persistEEBA(mCompany, mEmployee2, getEmployee2BankAccount(mEmployee2));

        return mCompany;
    }
    public void persistCompanyPIN() {
        ProcessResult<HashMap<String,String>> procResult =
                PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.QBDT, mSourceCoId, "test1234");

        PayrollServicesTest.assertSuccess("createPINResult", procResult);
    }

    public void persistEmployee2BankAccount2() {
        //Get employee 1's bank account info, and add EE2 to it, just with a different source bank account id
        EmployeeBankAccountDTO eebaDTO2 = getEmployee1BankAccount(mEmployee2);

        eebaDTO2.setEmployeeBankAccountId("EEBA2_2");

        persistEEBA(mCompany, mEmployee2, eebaDTO2);
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

        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT,
                                                                                 mCompany.getSourceCompanyId(),
                                                                                 txnReverseDTO);

        Application.commitUnitOfWork();

        assertSuccess(result);
    }

    public PayrollRun persistPayrollRun(PayrollRunDTO pPayrollRunDTO) {
        ProcessResult<PayrollRun> submitPayrollResult =
                PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, mSourceCoId, pPayrollRunDTO);

        PayrollServicesTest.assertSuccess("submitPayroll", submitPayrollResult);

        return submitPayrollResult.getResult();
    }

    public PayrollRunDTO get2ndCompany2PR_DoesNotExceedLimits() {
        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.OCTOBER, 10);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(nCba1);

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList<DDTransactionDTO>();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList<DDTransactionDTO>();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(mEeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("444.44"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        mEmployee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(mEeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("333.33"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        mEmployee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest09");
        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO get3rdCompany2PR_ExceedsOldLimits() {
        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.NOVEMBER, 15);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(nCba1);

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList<DDTransactionDTO>();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList<DDTransactionDTO>();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(mEeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("250.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        mEmployee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(mEeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("199999749.98"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        mEmployee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest10");
        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO getCompany1PayrollRunDTO() {
        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.OCTOBER, 2);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(nCba1);

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList<DDTransactionDTO>();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList<DDTransactionDTO>();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(mEeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS1");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("100.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        mEmployee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(mEeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS1");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("150.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        mEmployee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest01");
        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO getPayrollRunNOC_NoBankAccountChange() {
        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.NOVEMBER, 15);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(nCba1);

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList<DDTransactionDTO>();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList<DDTransactionDTO>();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(getEmployee1BankAccount(mEmployee1));
        ee1PaycheckSplit1.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("2.23"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        mEmployee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(getEmployee2BankAccount(mEmployee2));
        ee2PaycheckSplit1.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("8.88"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        mEmployee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId(SpcfUniqueId.generateRandomUniqueIdString());
        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public PayrollRunDTO getPayrollRunNOC_NoEEWithNOC() {
        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.NOVEMBER, 15);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(nCba1);

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList<DDTransactionDTO>();

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(getEmployee2BankAccount(mEmployee2));
        ee2PaycheckSplit1.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("127.88"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        mEmployee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId(SpcfUniqueId.generateRandomUniqueIdString());
        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }


    public PayrollRunDTO getPayrollRunNOC_EEBankAccountChanged() {
        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.NOVEMBER, 15);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(nCba1);

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList<DDTransactionDTO>();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList<DDTransactionDTO>();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(getEmployee1NewBankAccount());
        ee1PaycheckSplit1.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("122.23"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        mEmployee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(getEmployee2NewBankAccount());
        ee2PaycheckSplit1.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("188.88"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        mEmployee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId(SpcfUniqueId.generateRandomUniqueIdString());
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

     private EmployeeBankAccountDTO getEmployee1NewBankAccount() {
        // Initialize the EmployeeBankAccount
        BankAccountDTO ba = new BankAccountDTO();
        ba.setAccountNumber("12345678");
        ba.setRoutingNumber("111000025");
        ba.setBankName("abc bank");
        ba.setAccountType(BankAccountType.Checking);

        EmployeeBankAccountDTO eeba = new EmployeeBankAccountDTO();
        eeba.setEmployeeBankAccountId("EEBA1");
        eeba.setBankAccount(ba);

        return eeba;
    }

    private EmployeeBankAccountDTO getEmployee2NewBankAccount() {
        // Initialize the EmployeeBankAccount
        BankAccountDTO ba = new BankAccountDTO();
        ba.setAccountNumber("272727");
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
        ProcessResult<Employee> procResult =
                PayrollServices.employeeManager.addEmployee(pSourceSystemCd, pSourceEmployeeId, pEmployee);

        PayrollServicesTest.assertSuccess("addEmployee", procResult);

        return procResult.getResult();
    }

    public CompanyDTO getCompany1() {
        QuickbooksInfoDTO qbInfoDto = new QuickbooksInfoDTO();
        qbInfoDto.setApplicationId("Thunder");
        qbInfoDto.setApplicationVersion("17.00.R.9/20716#pro");
        qbInfoDto.setLicenseNumber("6487-4844-4441-476");
        qbInfoDto.setTaxTableId("848484");

        CompanyDTO compDto = new CompanyDTO();
        compDto.setDBA("QB Desktop Company 3");
        compDto.setFein(mFein);
        compDto.setLegalAddress(getTestLegalAddress());
        compDto.setLegalName("QB Desktop 3");
        compDto.setMailingAddress(getTestMailingAddress());
        compDto.setNotificationEmail("notifications3@intuit.com");
        compDto.setCompanyId(mSourceCoId);
        compDto.setSourceSystemCd(SourceSystemCode.QBDT);
        compDto.setContacts(getTestContacts());
        compDto.setQuickBooksInfo(qbInfoDto);

        return compDto;
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

    public void updateTo2DayFundingModel() {
        FundingModel twoDay = Application.findById(FundingModel.class, FundingModel.Codes.TWO_DAY);
        ProcessResult procResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBDT,
                                                                                            mCompany.getSourceCompanyId(),
                                                                                            twoDay);
        PayrollServicesTest.assertSuccess("updateCompanyFundingModel", procResult);
    }

    public void updateTo5DayFundingModel() {
        FundingModel fiveDay = Application.findById(FundingModel.class, FundingModel.Codes.FIVE_DAY);
        ProcessResult procResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBDT,
                                                                                            mCompany.getSourceCompanyId(),
                                                                                            fiveDay);
        PayrollServicesTest.assertSuccess("updateCompanyFundingModel", procResult);
    }

    public void updateLimits(SpcfMoney pNewLimit) {
        ProcessResult procResult = PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBDT,
                                                                                 mCompany.getSourceCompanyId(),
                                                                                 pNewLimit, pNewLimit);
        PayrollServicesTest.assertSuccess("updateDDLimits", procResult);
    }

    public PayrollRunDTO get4thCompany2PR_DoesNotExceedLimits() {
        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.OCTOBER, 15);

        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(nCba1);

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        Collection<DDTransactionDTO> ee1Txns = new ArrayList<DDTransactionDTO>();
        Collection<DDTransactionDTO> ee2Txns = new ArrayList<DDTransactionDTO>();

        DDTransactionDTO ee1PaycheckSplit1 = new DDTransactionDTO();
        ee1PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(mEeba1));
        ee1PaycheckSplit1.setDDTransactionId("EEBA1PS2");
        ee1PaycheckSplit1.setDDTransactionAmount(new BigDecimal("500.00"));
        ee1Txns.add(ee1PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee1Txns,
                                        mEmployee1.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        DDTransactionDTO ee2PaycheckSplit1 = new DDTransactionDTO();
        ee2PaycheckSplit1.setEmployeeBankAccount(createEmployeeBankAccountDTO(mEeba2));
        ee2PaycheckSplit1.setDDTransactionId("EEBA2PS2");
        ee2PaycheckSplit1.setDDTransactionAmount(new BigDecimal("800.00"));
        ee2Txns.add(ee2PaycheckSplit1);

        paychecks.add(createPaycheckDTO(ee2Txns,
                                        mEmployee2.getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString()));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId("BatchTest80");
        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
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
}
