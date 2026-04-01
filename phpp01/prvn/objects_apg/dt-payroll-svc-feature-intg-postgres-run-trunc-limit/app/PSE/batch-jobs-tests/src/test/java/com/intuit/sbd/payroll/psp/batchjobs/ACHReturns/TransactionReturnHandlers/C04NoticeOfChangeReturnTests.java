package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 29, 2008
 * Time: 12:07:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class C04NoticeOfChangeReturnTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testMeetsCriteria() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange(false, "C04", "", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        Application.commitUnitOfWork();
        assertEquals("Returned Handler", C04NoticeOfChangeReturn.class, returnHandler.getClass());
    }

    @Test
    public void testProcessC04NoticeOfChangeCompanyBankAccountNumber() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange(false, "C04", "", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            String cbaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.CompanyBankAccountId);
            CompanyBankAccount companyBankAccount = Application.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaAccountId));

            assertEquals("Company Bank Account", bankAccount, companyBankAccount.getBankAccount());
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC04NoticeOfChangeCBAInActive() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange(false, "C04", "", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);
        Application.commitUnitOfWork();

        // change company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.changeCompanyBankAccount(
                        SourceSystemCode.QBOE,
                        c1dl.getCompany1().getCompanyId(),
                        companyBankAccountDTO, false, true, true);
        assertTrue("Change company bank account", true == result.isSuccess());
        PayrollServices.commitUnitOfWork();


        Application.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        // verify no company events are created
        assertEquals("Company Events", 0, companyEventsList.size());

        Application.commitUnitOfWork();
    }

    //  test : first change cba and then offload
    @Test
    public void testC04NoticeOfChange_CBAChangedBeforeOffload() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        PayrollServices.beginUnitOfWork();
        loader.loadDataHappyPath();
        PayrollServices.commitUnitOfWork();

        // change company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.changeCompanyBankAccount(
                        SourceSystemCode.QBOE,
                        c1dl.getCompany1().getCompanyId(),
                        companyBankAccountDTO, false, true, true);
        assertTrue("Change company bank account", true == result.isSuccess());
        PayrollServices.commitUnitOfWork();

        // verify financial transactions are moved to new ba
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        assertEquals("Number of employer transacions", 1, c1FinTxns.size());
        assertEquals("Debit bank account", result.getResult().getBankAccount(), c1FinTxns.get(0).getDebitBankAccount());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<TransactionReturn> returnList = loader.createNoticeOfChange(false, "C04", "");
        PayrollServices.commitUnitOfWork();


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);
        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        assertEquals("Debit bank account", result.getResult().getBankAccount(), bankAccount);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        finTxn = Application.findById(FinancialTransaction.class, finTxn.getId());

        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            String cbaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.CompanyBankAccountId);
            CompanyBankAccount companyBankAccount = Application.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaAccountId));

            assertEquals("Company Bank Account", bankAccount, companyBankAccount.getBankAccount());
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC04NoticeOfChangeEmployeeBankAccountNumber() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangeEmployeeBankAccount("C04", "");
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);


        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            String employeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            EmployeeBankAccount employeeBankAccount = Application.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC04NoticeOfChangeEmployeeBankAccountNumberQbdtNonAssisted() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForNoticeOfChangeEmployeeBankAccount("C04", "Incorrect Account Holder Name");
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);

        assertEquals("TransactionReturn Transaction Return Status Code (Open)", TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());
        
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                                                                                         EventTypeCode.NOC,
                                                                                         CompanyEventStatus.Active,
                                                                                         null, null);


        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                         companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            String employeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            EmployeeBankAccount employeeBankAccount = Application.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC04NoticeOfChangeEmployeeBankAccountNumberQbdtAssisted() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForNoticeOfChangeEmployeeBankAccount("C04", "Incorrect Account Holder Name");
        Company company = returnList.get(0).getCompany();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addTaxService(company);
        DataLoadServices.activateTaxService(company);

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);

        // TODO - KP: Check if we need to auto-resolve C04s like we do for other NOCs
        //assertEquals("TransactionReturn Transaction Return Status Code (Resolved)", TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());
        assertEquals("TransactionReturn Transaction Return Status Code (Created)", TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                                                                                         EventTypeCode.NOC,
                                                                                         CompanyEventStatus.Active,
                                                                                         null, null);

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                         companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            String employeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            EmployeeBankAccount employeeBankAccount = Application.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC04NoticeOfChangePayeeBankAccountNumber() {

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangePayeeBankAccount("C04", "");


        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        //Update Bank Account Number Rule
        // assertEquals("Bank Account Number", bankAccount.getAccountNumber(), accountNumber);

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            // assertEquals("New Account Number", bankAccount.getAccountNumber(),
            //         companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountNumber));

            String payeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            PayeeBankAccount payeeBankAccount = PayrollServices.entityFinder.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(payeeBankAccountId));

            assertEquals("Payee Bank Account", bankAccount, payeeBankAccount.getBankAccount());
        }
        Application.commitUnitOfWork();
    }
}
