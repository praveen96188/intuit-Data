package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.common.ofx.response.IDDACCT;
import com.intuit.sbd.payroll.psp.common.ofx.response.IEMP;
import com.intuit.sbd.payroll.psp.common.ofx.response.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 29, 2008
 * Time: 8:43:00 AM
 */
public class C01NoticeOfChangeReturnTests {


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
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange(false, "C01", "", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        PayrollServices.commitUnitOfWork();

        assertEquals("Returned Handler", C01NoticeOfChangeReturn.class, returnHandler.getClass());
    }

    @Test
    public void testProcessC01NoticeOfChangeCompanyBankAccountNumber() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange(false, "C01", "", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

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
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                transactionReturn.getReturnStatusCd());

        //Update Bank Account Number Rule
        assertEquals("Bank Account Number", bankAccount.getAccountNumber(), accountNumber);

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
            CompanyBankAccount companyBankAccount = PayrollServices.entityFinder.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaAccountId));

            assertEquals("Company Bank Account", bankAccount, companyBankAccount.getBankAccount());
        }
        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC01NoticeOfChange_CBAInActive() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange(false, "C01", "", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);
        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();
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

        finTxn = Application.findById(FinancialTransaction.class, finTxn.getId());

        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Verify Transaction return is resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                transactionReturn.getReturnStatusCd());

        //verify bank account number is not updated
        assertEquals("Bank Account Number", oldAccountNumber, bankAccount.getAccountNumber());

        // verify no company events are created
        assertEquals("Company Events", 0, companyEventsList.size());

        Application.commitUnitOfWork();
    }

    //  test : first change cba and then offload
    @Test
    public void testC01NoticeOfChange_CBAChangedBeforeOffload() {
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

        DomainEntitySet<TransactionReturn> returnList = loader.createNoticeOfChange(false, "C01", "");
        PayrollServices.commitUnitOfWork();


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);
        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        assertEquals("Debit bank account", result.getResult().getBankAccount(), bankAccount);
        String oldAccountNumber = bankAccount.getAccountNumber();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        finTxn = Application.findById(FinancialTransaction.class, finTxn.getId());

        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                transactionReturn.getReturnStatusCd());

        //Update Bank Account Number Rule
        assertEquals("Bank Account Number", bankAccount.getAccountNumber(), accountNumber);

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
            CompanyBankAccount companyBankAccount = PayrollServices.entityFinder.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaAccountId));

            assertEquals("Company Bank Account", bankAccount, companyBankAccount.getBankAccount());
        }
        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC01NoticeOfChangeEmployeeBankAccountNumber() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangeEmployeeBankAccount("C01", "");
        PayrollServices.commitUnitOfWork();

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
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                transactionReturn.getReturnStatusCd());

        //Update Bank Account Number Rule
        assertEquals("Bank Account Number", bankAccount.getAccountNumber(), accountNumber);

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
            EmployeeBankAccount employeeBankAccount = PayrollServices.entityFinder.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());
        }
        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC01NoticeOfChangeEmployeeBankAccountNumberQbdtNonAssisted() {
        long beforeToken = 0, afterToken = 0;

        //
        // Load the test date with EE NOCs
        //

        PayrollServices.beginUnitOfWork();

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForNoticeOfChangeEmployeeBankAccount("C01", "Incorrect Bank Account Number");

        beforeToken = returnList.get(0).getCompany().getCurrentToken();

        PayrollServices.commitUnitOfWork();

        //
        // Execute the returns and prep for testing
        //

        Application.beginUnitOfWork();

        for (TransactionReturn transactionReturn : returnList) {
            transactionReturn = Application.refresh(transactionReturn);

            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

            returnHandler.execute(transactionReturn);

            afterToken = transactionReturn.getCompany().getCurrentToken();
        }

        Application.commitUnitOfWork();

        //
        // Test the before and after values
        //

        assertEquals("Token before return", 1, beforeToken);
        assertEquals("Token after return", 1, afterToken);

        Application.beginUnitOfWork();

        for (TransactionReturn transactionReturn : returnList) {
            transactionReturn = Application.refresh(transactionReturn);

            assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());

            DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);
            EmployeeBankAccount eeba = finTxnList.get(0).getEmployeeBankAccount();
            DomainEntitySet<CompanyEvent> companyEventList = CompanyEvent.findCompanyEventWithDetailsEagerLoaded(transactionReturn.getCompany(),
                                                                                                                 EventTypeCode.NOC,
                                                                                                                 EventDetailTypeCode.EmployeeBankAccountId,
                                                                                                                 eeba.getId().toString());

            assertEquals("NOC event count", 1, companyEventList.size());

            CompanyEvent companyEvent = companyEventList.get(0);
            String corAccountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
            String newAccountNumber = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountNumber);
            String oldAccountNumber = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber);
            String curAccountNumber = eeba.getBankAccount().getAccountNumber();

            // ensure the EEBA account number was not updated
            assertEquals("Old Account Number (event detail)", oldAccountNumber, curAccountNumber);

            // ensure the new account number is recorded in the event detail
            assertEquals("New Account Number (event detail)", newAccountNumber, corAccountNumber);
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testProcessC01NoticeOfChangeEmployeeBankAccountNumberQbdtAssisted() {
        long beforeToken = 0, afterToken = 0;

        DataLoadServices.setPSPDate(2012, 1, 1);
        //
        // Load the test date with EE NOCs
        //
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        List<Employee> emps = DataLoadServices.addEEs(company, 1, true, true);

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(2012, 1, 10), emps, new HashMap<String, String>());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        assertSuccess(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, psid,
                                           TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTransactions, "C01", "Incorrect Bank Account Number");
        PayrollServices.commitUnitOfWork();

        //
        // Execute the returns and prep for testing
        //
        company = DataLoadServices.refreshCompany(company);
        beforeToken = company.getCurrentToken();
        Map<String, String> nocChanges = new HashMap<String, String>();
        Application.beginUnitOfWork();

        for (TransactionReturn transactionReturn : returnList) {
            transactionReturn = Application.refresh(transactionReturn);

            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

            String oldAccountNumber = finTxnList.get(0).getNonIntuitBankAccount().getAccountNumber();

            returnHandler.execute(transactionReturn);

            String newAccountNumber = finTxnList.get(0).getEmployeeBankAccount().getEmployee().getEmployeeBankAccountCollection().findEntity(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active)).getBankAccount().getAccountNumber();

            nocChanges.put(oldAccountNumber, newAccountNumber);
        }

        Application.commitUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        afterToken = company.getCurrentToken();
        //
        // Test the before and after values
        //

        assertEquals("Token before return", 5, beforeToken);
        assertEquals("Token after return", 6, afterToken);

        Application.beginUnitOfWork();

        String newAccountNumber = "";
        for (TransactionReturn transactionReturn : returnList) {
            transactionReturn = Application.refresh(transactionReturn);

            DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);
            String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
            EmployeeBankAccount eeba = finTxnList.get(0).getEmployeeBankAccount().getEmployee().getEmployeeBankAccountCollection().findEntity(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active));

            assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());
            assertEquals("Bank Account Number", eeba.getBankAccount().getAccountNumber(), accountNumber);
            
            DomainEntitySet<CompanyEvent> companyEventList = CompanyEvent.findCompanyEventWithDetailsEagerLoaded(transactionReturn.getCompany(),
                                                                                                                 EventTypeCode.NOC,
                                                                                                                 EventDetailTypeCode.EmployeeBankAccountId,
                                                                                                                 finTxnList.get(0).getEmployeeBankAccount().getId().toString());

            assertEquals("NOC event count", 1, companyEventList.size());

            CompanyEvent companyEvent = companyEventList.get(0);
            String oldAccountNumber = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber);
            newAccountNumber = eeba.getBankAccount().getAccountNumber();

            assertTrue("Old Account Number", nocChanges.containsKey(oldAccountNumber));

            assertEquals("New Account Number (event detail)",
                         newAccountNumber,
                         companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountNumber));

            assertEquals("New Account Number (map)",
                         nocChanges.get(oldAccountNumber),
                         companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountNumber));
        }

        Application.commitUnitOfWork();

        OFX response = QBDTTestHelper.submitSyncRequest(company, beforeToken, true);
        IEMP iempmod = assertOne(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIEMPMOD());
        IDDACCT iddacct = assertOne(iempmod.getIEMPDD().getIDDACCT());
        assertEquals("new bank account pushed back to QB", newAccountNumber, iddacct.getBANKACCTTO().getACCTID());
    }

    @Test
    public void testProcessC01NoticeOfChangePayeeBankAccountNumber() {

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangePayeeBankAccount("C01", "");


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
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChange(true, "C01", "", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

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
            companyBankAccount = PayrollServices.entityFinder.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaAccountId));

            assertEquals("Company Bank Account", bankAccount, companyBankAccount.getBankAccount());
        }
        assertEquals("Company Notes ", 1, finTxn.getCompany().getCompanyNoteCollection().size());

        for (CompanyNote domainNote : finTxn.getCompany().getCompanyNoteCollection()) {
            assertEquals("Company Note:", "An erroneous NOC with change code 'C01' was received for company " +
                    "bank account id '" + companyBankAccount.getSourceBankAccountId() + "'.  The provided bank account number is invalid. " +
                    "The NOC was left unresolved and no action was taken against the affected company bank " +
                    "account by the system.", domainNote.getNotes());
        }

        Application.commitUnitOfWork();
    }
}
