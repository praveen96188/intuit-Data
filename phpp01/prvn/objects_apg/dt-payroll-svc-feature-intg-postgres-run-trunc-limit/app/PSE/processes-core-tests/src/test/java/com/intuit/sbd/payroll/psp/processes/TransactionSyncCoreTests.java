/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/TransactionSyncCoreTests.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company123272727DataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo.MessageLevel;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccessResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Contains the unit tests for the <CODE>TransactionSyncCore</CODE> class.
 *
 * @author: Sean Barenz
 * @version: Jun 20, 2007
 */
public class TransactionSyncCoreTests {
    private static final String COMPANY1 = "123272727";
    private static final String REQUEST_ID1 = "P1";
    private static final String REQUEST_ID2 = "B1";

    @BeforeClass
    public static void initialize() {
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        DataLoader dataloader = new DataLoader();
        dataloader.persistCompany(dataloader.getTestActiveCompany());
        PayrollServices.commitUnitOfWork();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void transactionSyncSourceSystemNotSpecified() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<TransactionResponse>> results = PayrollServices.payrollManager.syncTransactions(null, "IDONTEXIST", 1L);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages Returned:", 1, results.getMessages().size());

        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "137", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Message Text:", "Source System Code is not specified.", message.getMessage());
    }

    @Test
    public void transactionSyncSourceCompanyNotSpecified() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<TransactionResponse>> results = PayrollServices.payrollManager.syncTransactions(SourceSystemCode.QBOE, null, 1L);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages Returned:", 1, results.getMessages().size());

        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "138", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Message Text:", "Source Company ID is not specified.", message.getMessage());
    }

    @Test
    public void transactionSyncCompanyNotExists() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<TransactionResponse>> results = PayrollServices.payrollManager.syncTransactions(SourceSystemCode.QBOE, "IDONTEXIST", 1L);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages Returned:", 1, results.getMessages().size());

        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "169", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Message Text:", "Company QBOE:IDONTEXIST does not exist.", message
                .getMessage());
    }

    @Test
    public void transactionSyncNoResponsesFound() {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
        DataLoader dataloader = new DataLoader();
        dataloader.persistTestCompanyService(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<TransactionResponse>> results = PayrollServices.payrollManager.syncTransactions(SourceSystemCode.QBOE, COMPANY1, 2L);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages Returned:", 1, results.getMessages().size());

        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "10000", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.WARNING, message.getLevel());
        assertEquals("Message Text:", "No new transactions for Company QBOE:" + COMPANY1
                + " beyond token value 2.", message.getMessage());
    }

    @Test
    public void transactionSyncOneExpected() {
        PayrollServices.beginUnitOfWork();
        loadPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<TransactionResponse>> results = PayrollServices.payrollManager.syncTransactions(SourceSystemCode.QBOE, COMPANY1, 1L);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages Returned:", 0, results.getMessages().size());

        DomainEntitySet<TransactionResponse> responses = results.getResult();
        assertEquals("Transaction Responses Returned", 1, responses.size());
        TransactionResponse transactionResponse = responses.get(0);

        transactionResponse = PayrollServices.entityFinder.findById(TransactionResponse.class, transactionResponse.getId());
        verifySingleTransactionResponse(transactionResponse);
    }

    @Test
    public void transactionSyncTwoExpected() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionResponse> responses = assertSuccessResult(PayrollServices.payrollManager.syncTransactions(SourceSystemCode.QBOE, COMPANY1, 0L));
        PayrollServices.commitUnitOfWork();

        assertEquals("Transaction Responses Returned", 2, responses.size());

        TransactionResponse tr = responses.get(0);

        // Verify the response
        assertEquals("Verifying First Token", 2L, tr.getTransactionTokenNumber());
        assertEquals("First Request ID", REQUEST_ID1, tr.getSourceRequestId());

        // Verify the TransactionStates
        FinancialTransactionState fts = tr.getFinancialTransactionStates()
                .get(0);
        assertEquals("First FinancialTransactionState", TransactionStateCode.Created, fts
                .getTransactionState().getTransactionStateCd());

        // verify second item
        tr = responses.get(1);
        assertEquals("Verifying Second Token", 1L, tr.getTransactionTokenNumber());
        assertEquals("Second Request ID", REQUEST_ID2, tr.getSourceRequestId());

        // Verify the TransactionStates
        fts = tr.getFinancialTransactionStates().get(0);
        assertEquals("First FinancialTransactionState", TransactionStateCode.Created, fts
                .getTransactionState().getTransactionStateCd());
    }

    @Test
    public void transactionSyncAfterLastResponse() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<TransactionResponse>> results = PayrollServices.payrollManager.syncTransactions(SourceSystemCode.QBOE, COMPANY1, 7L);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages Returned:", 1, results.getMessages().size());

        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "10000", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.WARNING, message.getLevel());
        assertEquals("Message Text:", "No new transactions for Company QBOE:" + COMPANY1
                + " beyond token value 7.", message.getMessage());
    }

    /**
     * Test error message 1101 - is allowed capability false
     */
    @Test
    public void transactionSyncNotAllowedCapability() {
        //Set company status to "Inactive"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.terminateService(SourceSystemCode.QBOE, COMPANY1, ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<TransactionResponse>> processResult = PayrollServices.payrollManager.syncTransactions(SourceSystemCode.QBOE, COMPANY1, 1L);
        PayrollServices.commitUnitOfWork();

       // validate error count
        assertEquals("Number of Errors:", 0, processResult.getMessages().size());
    }

    private void verifySingleTransactionResponse(TransactionResponse tr) {
        // Verify the response
        assertEquals("Verifying Token", 2L, tr.getTransactionTokenNumber());
        assertNotNull("Request ID", tr.getSourceRequestId());

        // Verify the Transactions
        for (FinancialTransactionState state : tr
                .getFinancialTransactionStates()) {
            assertEquals("FinancialTransactionState", TransactionStateCode.Created, state
                    .getTransactionState().getTransactionStateCd());
        }
        assertEquals("Transaction Count:", 4, tr.getFinancialTransactionStates(
        ).size());
    }

    /**
     * Reloads data to start back off with a fresh data state
     */
    private void loadPayrolls() {
        SpcfCalendar sysDate = SpcfCalendar.createInstance(2007, SpcfCalendar.September, 25, 16, 59, 0, 0,
                SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(sysDate);

        PayrollServicesTest.truncateTables();
        Company123272727DataLoader dataloader2 = new Company123272727DataLoader();

        // Load Company
        dataloader2.setupTestCompany();

        dataloader2.savePayroll(dataloader2.loadBackgroundPayroll1(), 1L, "B1");
        dataloader2.savePayroll(dataloader2.loadPayroll(), 2L, "P1");
    }
}
