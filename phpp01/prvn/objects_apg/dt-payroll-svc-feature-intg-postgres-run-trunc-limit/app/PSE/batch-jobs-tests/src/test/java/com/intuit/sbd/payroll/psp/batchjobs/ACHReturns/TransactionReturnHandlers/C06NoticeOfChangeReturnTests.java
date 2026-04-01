package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 29, 2008
 * Time: 2:35:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class C06NoticeOfChangeReturnTests {
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
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange(false, "C06", "", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        Application.commitUnitOfWork();
        assertEquals("Returned Handler", C06NoticeOfChangeReturn.class, returnHandler.getClass());

    }

    @Test
    public void testProcessC03NoticeOfChangeCompanyBankAccountNumber() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange(false, "C06", "", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);
        transactionReturn = PayrollServices.entityFinder.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        ACHBankAccountType achBankAccountType = NoticeOfChangeUtils.getCorrectedBankAccountTypeCode(transactionReturn);
        BankAccountType bankAccountType = NoticeOfChangeUtils.getBankAccountType(achBankAccountType);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                transactionReturn.getReturnStatusCd());

        //Update Bank Account Number Rule
        assertEquals("Bank Account Number", bankAccount.getAccountNumber(), accountNumber);

        //Update Bank Account Type Cd Rule
        assertEquals("Bank Account type", bankAccount.getACHAccountTypeCd(), achBankAccountType);
        assertEquals("Bank Account type", bankAccount.getAccountTypeCd(), bankAccountType);

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Number", oldAccountNumber,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));

            assertEquals("New Account Number", bankAccount.getAccountNumber(),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountNumber));

            String cbaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.CompanyBankAccountId);
            CompanyBankAccount companyBankAccount = Application.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaAccountId));

            assertEquals("Company Bank Account", bankAccount, companyBankAccount.getBankAccount());

            assertEquals("Old Account Type", EnumUtils.getReadableName(BankAccountType.Checking),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(BankAccountType.Savings),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC06NoticeOfChangeCBAInActive() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange(false, "C06", "", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);
        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();
        BankAccountType oldBankAccountType = bankAccount.getAccountTypeCd();
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

        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Verify Transaction return is resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                transactionReturn.getReturnStatusCd());

        //verify bank account number and type are not updated
        assertEquals("Bank Account Number", oldAccountNumber, bankAccount.getAccountNumber());
        assertEquals("Bank Account Type", oldBankAccountType, bankAccount.getAccountTypeCd());

        // verify no company events are created
        assertEquals("Company Events", 0, companyEventsList.size());

        Application.commitUnitOfWork();
    }

    //  test : first change cba and then offload
    @Test
    public void testC06NoticeOfChange_CBAChangedBeforeOffload() {
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

        DomainEntitySet<TransactionReturn> returnList = loader.createNoticeOfChange(false, "C06", "");
        PayrollServices.commitUnitOfWork();


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);
        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        assertEquals("Debit bank account", result.getResult().getBankAccount(), bankAccount);
        String oldAccountNumber = bankAccount.getAccountNumber();
        String oldRoutingNumber = bankAccount.getRoutingNumber();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
//        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = Application.findById(FinancialTransaction.class, finTxn.getId());

        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        ACHBankAccountType achBankAccountType = NoticeOfChangeUtils.getCorrectedBankAccountTypeCode(transactionReturn);
        BankAccountType bankAccountType = NoticeOfChangeUtils.getBankAccountType(achBankAccountType);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                transactionReturn.getReturnStatusCd());

        //Update Bank Account Number Rule
        assertEquals("Bank Account Number", bankAccount.getAccountNumber(), accountNumber);

        //Update Bank Account Type Cd Rule
        assertEquals("Bank Account type", bankAccount.getACHAccountTypeCd(), achBankAccountType);
        assertEquals("Bank Account type", bankAccount.getAccountTypeCd(), bankAccountType);

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Number", oldAccountNumber,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));

            assertEquals("New Account Number", bankAccount.getAccountNumber(),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountNumber));

            String cbaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.CompanyBankAccountId);
            CompanyBankAccount companyBankAccount = Application.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaAccountId));

            assertEquals("Company Bank Account", bankAccount, companyBankAccount.getBankAccount());

            assertEquals("Old Account Type", EnumUtils.getReadableName(BankAccountType.Checking),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(BankAccountType.Savings),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC06NoticeOfChangeEmployeeBankAccountNumber() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangeEmployeeBankAccount("C06", "");
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);
        transactionReturn = PayrollServices.entityFinder.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        ACHBankAccountType achBankAccountType = NoticeOfChangeUtils.getCorrectedBankAccountTypeCode(transactionReturn);
        BankAccountType bankAccountType = NoticeOfChangeUtils.getBankAccountType(achBankAccountType);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                transactionReturn.getReturnStatusCd());

        //Update Bank Account Number Rule
        assertEquals("Bank Account Number", bankAccount.getAccountNumber(), accountNumber);

        //Update Bank Account Type Cd Rule
        assertEquals("Bank Account Type Code", bankAccount.getACHAccountTypeCd(), achBankAccountType);
        assertEquals("Bank Account Type Code", bankAccount.getAccountTypeCd(), bankAccountType);

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {

            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Number", oldAccountNumber,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));

            assertEquals("New Account Number", bankAccount.getAccountNumber(),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountNumber));

            String employeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            EmployeeBankAccount employeeBankAccount = Application.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());

            assertEquals("Old Account Type", EnumUtils.getReadableName(BankAccountType.Checking),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(BankAccountType.Savings),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC06NoticeOfChangePayeeBankAccountNumber() {

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangePayeeBankAccount("C06", "");


        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

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

            assertEquals("Old Account Number", oldAccountNumber,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));

            // assertEquals("New Account Number", bankAccount.getAccountNumber(),
            //         companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountNumber));

            assertEquals("Old Account Type", EnumUtils.getReadableName(BankAccountType.Checking),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(BankAccountType.Savings),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));
            String payeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            PayeeBankAccount payeeBankAccount = PayrollServices.entityFinder.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(payeeBankAccountId));

            assertEquals("Payee Bank Account", bankAccount, payeeBankAccount.getBankAccount());
        }
        Application.commitUnitOfWork();
    }

    @Test
    public void testInvalidCorrectedFieldCompanyNoteRule() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange(true, "C06", "", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();
        BankAccountType oldAccountTypeCd = bankAccount.getAccountTypeCd();
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        bankAccount = finTxn.getNonIntuitBankAccount();
        CompanyBankAccount companyBankAccount = finTxn.getCompanyBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Number", oldAccountNumber,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));

            assertEquals("New Account Number", "INVALID",
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountNumber));

            String cbaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.CompanyBankAccountId);
            companyBankAccount = Application.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaAccountId));

            assertEquals("Company Bank Account", bankAccount, companyBankAccount.getBankAccount());

            assertEquals("Old Account Type", EnumUtils.getReadableName(oldAccountTypeCd),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(oldAccountTypeCd),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));
        }

        assertEquals("Company Notes ", 1, finTxn.getCompany().getCompanyNoteCollection().size());

        for (CompanyNote domainNote : finTxn.getCompany().getCompanyNoteCollection()) {
            assertEquals("Company Note:", "An erroneous NOC with change code 'C06' was received for company " +
                    "bank account id '" + companyBankAccount.getSourceBankAccountId() + "'.  The provided bank account number is invalid." +
                    " The current bank account type code is '" +
                    bankAccount.getAccountTypeCd() + "', the corrected value provided for this field in the NOC was " +
                    "null, which is invalid. The NOC was left unresolved and no action was taken against the " +
                    "affected company bank account by the system.", domainNote.getNotes());
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testERLedgerDebitNOC() throws Exception {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange("C06", "4847474748          42", SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        BankAccountType oldAccountType = bankAccount.getAccountTypeCd();
        ACHBankAccountType oldAchAccountType = bankAccount.getACHAccountTypeCd();
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        assertEquals(TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());

        bankAccount = finTxn.getNonIntuitBankAccount();
        CompanyBankAccount companyBankAccount = finTxn.getCompanyBankAccount();

        assertEquals(ACHBankAccountType.Ledger, companyBankAccount.getBankAccount().getACHAccountTypeCd());
        assertEquals(BankAccountType.Checking, companyBankAccount.getBankAccount().getAccountTypeCd());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, null, null, null);

        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals(CompanyEventStatus.Active, companyEvent.getStatusCd());

            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));

            assertEquals("Old ACH Account Type", EnumUtils.getReadableName(oldAchAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAchAccountType));

            assertEquals("New ACH Account Type", EnumUtils.getReadableName(ACHBankAccountType.Ledger),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAchAccountType));

            String cbaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.CompanyBankAccountId);
            companyBankAccount = Application.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaAccountId));

            assertEquals("Company Bank Account", bankAccount, companyBankAccount.getBankAccount());
            assertEquals(1, companyEvent.getCompanyEventEmailCollection().size());
        }
        assertEquals("Company Notes ", 0, finTxn.getCompany().getCompanyNoteCollection().size());

        Application.commitUnitOfWork();
    }

    @Test
    public void testERLoanDebitNOC() throws Exception {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange("C06", "4847474748          52", SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        BankAccountType oldAccountType = bankAccount.getAccountTypeCd();
        ACHBankAccountType oldAchAccountType = bankAccount.getACHAccountTypeCd();
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        assertEquals(TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());

        Company company = finTxn.getCompany();
        assertTrue(company.isCompanyOnHold());
        assertEquals(ServiceSubStatusCode.RiskAssessment, company.getOnHoldReasonCollection().getFirst().getOnHoldReasonCd());

        bankAccount = finTxn.getNonIntuitBankAccount();
        CompanyBankAccount companyBankAccount = finTxn.getCompanyBankAccount();

        assertEquals(ACHBankAccountType.Loan, companyBankAccount.getBankAccount().getACHAccountTypeCd());
        assertEquals(BankAccountType.Checking, companyBankAccount.getBankAccount().getAccountTypeCd());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, null, null, null);
        assertEquals("Company Events", 0, companyEventsList.size());

        companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.ERLoanNOC, null, null, null);
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals(CompanyEventStatus.Active, companyEvent.getStatusCd());

            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));

            assertEquals("Old ACH Account Type", EnumUtils.getReadableName(oldAchAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAchAccountType));

            assertEquals("New ACH Account Type", EnumUtils.getReadableName(ACHBankAccountType.Loan),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAchAccountType));

            String cbaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.CompanyBankAccountId);
            companyBankAccount = Application.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaAccountId));

            assertEquals("Company Bank Account", bankAccount, companyBankAccount.getBankAccount());
            assertEquals(1, companyEvent.getCompanyEventEmailCollection().size());
        }
        assertEquals("Company Notes ", 0, finTxn.getCompany().getCompanyNoteCollection().size());

        Application.commitUnitOfWork();
    }

    @Test
    public void testEEInvalidCreditNOC() throws Exception {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        TransactionReturn transactionReturn = loader.loadQBDTDataWithOneNOCReturn("C06", null, "EE1_1", "12346               85");
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        transactionReturn = Application.refresh(transactionReturn);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        BankAccountType oldAccountType = bankAccount.getAccountTypeCd();
        ACHBankAccountType oldAchAccountType = bankAccount.getACHAccountTypeCd();
        String oldAccountNumber = bankAccount.getAccountNumber();
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        assertEquals(TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());

        bankAccount = finTxn.getNonIntuitBankAccount();
        EmployeeBankAccount employeeBankAccount = finTxn.getEmployeeBankAccount();

        assertEquals(ACHBankAccountType.Checking, employeeBankAccount.getBankAccount().getACHAccountTypeCd());
        assertEquals(BankAccountType.Checking, employeeBankAccount.getBankAccount().getAccountTypeCd());
        assertEquals(oldAccountNumber, employeeBankAccount.getBankAccount().getAccountNumber());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, null, null, null);

        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals(CompanyEventStatus.Active, companyEvent.getStatusCd());

            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));

            assertEquals("Old ACH Account Type", EnumUtils.getReadableName(oldAchAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAchAccountType));

            assertNull("New ACH Account Type",
                       companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAchAccountType));

            String ebaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            employeeBankAccount = Application.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(ebaAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());
            assertEquals(1, companyEvent.getCompanyEventEmailCollection().size());
        }
        assertEquals("Company Notes ", 1, finTxn.getCompany().getCompanyNoteCollection().size());

        Application.commitUnitOfWork();
    }

    @Test
    public void testEELedgerCreditNOC() throws Exception {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        TransactionReturn transactionReturn = loader.loadQBDTDataWithOneNOCReturn("C06", null, "EE1_1", "12346               42");
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        transactionReturn = Application.refresh(transactionReturn);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        BankAccountType oldAccountType = bankAccount.getAccountTypeCd();
        ACHBankAccountType oldAchAccountType = bankAccount.getACHAccountTypeCd();
        String oldAccountNumber = bankAccount.getAccountNumber();
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        assertEquals(TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());

        bankAccount = finTxn.getNonIntuitBankAccount();
        EmployeeBankAccount employeeBankAccount = finTxn.getEmployeeBankAccount();

        assertEquals(ACHBankAccountType.Ledger, employeeBankAccount.getBankAccount().getACHAccountTypeCd());
        assertEquals(BankAccountType.Checking, employeeBankAccount.getBankAccount().getAccountTypeCd());
        assertEquals(oldAccountNumber, employeeBankAccount.getBankAccount().getAccountNumber());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, null, null, null);

        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals(CompanyEventStatus.Active, companyEvent.getStatusCd());

            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));

            assertEquals("Old ACH Account Type", EnumUtils.getReadableName(oldAchAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAchAccountType));

            assertEquals("New ACH Account Type", EnumUtils.getReadableName(ACHBankAccountType.Ledger),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAchAccountType));

            String ebaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            employeeBankAccount = Application.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(ebaAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());
            assertEquals(1, companyEvent.getCompanyEventEmailCollection().size());
        }
        assertEquals("Company Notes ", 0, finTxn.getCompany().getCompanyNoteCollection().size());

        Application.commitUnitOfWork();
    }

    @Test
    public void testEELoanCreditNOC() throws Exception {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        TransactionReturn transactionReturn = loader.loadQBDTDataWithOneNOCReturn("C06", null, "EE1_1", "12346               52");
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        transactionReturn = Application.refresh(transactionReturn);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        BankAccountType oldAccountType = bankAccount.getAccountTypeCd();
        ACHBankAccountType oldAchAccountType = bankAccount.getACHAccountTypeCd();
        String oldAccountNumber = bankAccount.getAccountNumber();
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        assertEquals(TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());

        bankAccount = finTxn.getNonIntuitBankAccount();
        EmployeeBankAccount employeeBankAccount = finTxn.getEmployeeBankAccount();

        assertEquals(ACHBankAccountType.Loan, employeeBankAccount.getBankAccount().getACHAccountTypeCd());
        assertEquals(BankAccountType.Checking, employeeBankAccount.getBankAccount().getAccountTypeCd());
        assertEquals(oldAccountNumber, employeeBankAccount.getBankAccount().getAccountNumber());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, null, null, null);
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals(CompanyEventStatus.Active, companyEvent.getStatusCd());

            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));

            assertEquals("Old ACH Account Type", EnumUtils.getReadableName(oldAchAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAchAccountType));

            assertEquals("New ACH Account Type", EnumUtils.getReadableName(ACHBankAccountType.Loan),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAchAccountType));

            String ebaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            employeeBankAccount = Application.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(ebaAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());
            assertEquals(1, companyEvent.getCompanyEventEmailCollection().size());
        }
        assertEquals("Company Notes ", 0, finTxn.getCompany().getCompanyNoteCollection().size());

        Application.commitUnitOfWork();
    }

    @Test
    public void testPayeeLedgerCreditNOC() throws Exception {

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        TransactionReturn transactionReturn = loader.loadDataForSingleNoticeOfChangePayeeBankAccount("C06", null, "12346               42").getFirst();

        PayrollServices.beginUnitOfWork();
        transactionReturn = Application.refresh(transactionReturn);

        Company company = transactionReturn.getCompany();
        long oldTokenValue = company.getCurrentToken();

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        BankAccountType oldAccountType = bankAccount.getAccountTypeCd();
        ACHBankAccountType oldAchAccountType = bankAccount.getACHAccountTypeCd();
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        company = finTxn.getCompany();
        assertEquals(oldTokenValue, company.getCurrentToken());

        assertEquals(TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());

        bankAccount = finTxn.getNonIntuitBankAccount();
        PayeeBankAccount payeeBankAccount = finTxn.getPayeeBankAccount();

        assertEquals(ACHBankAccountType.Ledger, payeeBankAccount.getBankAccount().getACHAccountTypeCd());
        assertEquals(BankAccountType.Checking, payeeBankAccount.getBankAccount().getAccountTypeCd());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, null, null, null);

        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals(CompanyEventStatus.Active, companyEvent.getStatusCd());

            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));

            assertEquals("Old ACH Account Type", EnumUtils.getReadableName(oldAchAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAchAccountType));

            assertEquals("New ACH Account Type", EnumUtils.getReadableName(ACHBankAccountType.Ledger),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAchAccountType));

            String ebaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            payeeBankAccount = Application.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(ebaAccountId));

            assertEquals("Employee Bank Account", bankAccount, payeeBankAccount.getBankAccount());
            assertEquals(1, companyEvent.getCompanyEventEmailCollection().size());
        }
        assertEquals("Company Notes ", 0, finTxn.getCompany().getCompanyNoteCollection().size());

        Application.commitUnitOfWork();
    }

    @Test
    public void testPayeeLoanCreditNOC() throws Exception {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        TransactionReturn transactionReturn = loader.loadDataForSingleNoticeOfChangePayeeBankAccount("C06", null, "12346               52").getFirst();

        Application.beginUnitOfWork();

        transactionReturn = Application.refresh(transactionReturn);

        Company company = transactionReturn.getCompany();
        long oldTokenValue = company.getCurrentToken();

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        BankAccountType oldAccountType = bankAccount.getAccountTypeCd();
        ACHBankAccountType oldAchAccountType = bankAccount.getACHAccountTypeCd();
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);

        company = finTxn.getCompany();
        assertEquals(oldTokenValue, company.getCurrentToken());

        assertEquals(TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());

        assertFalse(company.isCompanyOnHold());

        bankAccount = finTxn.getNonIntuitBankAccount();
        PayeeBankAccount payeeBankAccount = finTxn.getPayeeBankAccount();

        assertEquals(ACHBankAccountType.Loan, payeeBankAccount.getBankAccount().getACHAccountTypeCd());
        assertEquals(BankAccountType.Checking, payeeBankAccount.getBankAccount().getAccountTypeCd());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, null, null, null);
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals(CompanyEventStatus.Active, companyEvent.getStatusCd());

            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType));

            assertEquals("New Account Type", EnumUtils.getReadableName(oldAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType));

            assertEquals("Old ACH Account Type", EnumUtils.getReadableName(oldAchAccountType),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAchAccountType));

            assertEquals("New ACH Account Type", EnumUtils.getReadableName(ACHBankAccountType.Loan),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAchAccountType));

            String ebaAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            payeeBankAccount = Application.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(ebaAccountId));

            assertEquals("Employee Bank Account", bankAccount, payeeBankAccount.getBankAccount());
            assertEquals(1, companyEvent.getCompanyEventEmailCollection().size());
        }
        assertEquals("Company Notes ", 0, finTxn.getCompany().getCompanyNoteCollection().size());

        Application.commitUnitOfWork();
    }

}
