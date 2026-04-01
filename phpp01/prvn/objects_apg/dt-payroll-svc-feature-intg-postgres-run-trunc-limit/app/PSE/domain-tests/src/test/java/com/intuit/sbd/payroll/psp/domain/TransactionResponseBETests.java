package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company123272727DataLoader;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;
import static org.junit.Assert.*;

import java.util.Collection;

/**
 * Unit Tests for the EmployeeBE
 * 
 * User: Sean Barenz Date: Aug 15, 2007
 */
public class TransactionResponseBETests {
	private static final String COMPANY1 = "123272727";
	private static final String REQUEST_ID1 = "P1";

	@BeforeClass
	public static void initialize() {
        PayrollServices.beginUnitOfWork();
        Application.truncateTables();
		ApplicationSecondary.truncateTables();
		PayrollServices.setCurrentPrincipal(SystemPrincipal.UnitTest);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        Company123272727DataLoader dataloader2 = new Company123272727DataLoader();

		// Load Company
		dataloader2.setupTestCompany();
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
	public void findTransactionResponsesExpectZero() {
		Application.beginUnitOfWork();
		Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(
				company, 0L);
		assertEquals("Number of Responses:", 0, responses.size());
		Application.commitUnitOfWork();
	}

	@Test
	public void findTransactionResponsesExpectOne() {
        Application.beginUnitOfWork();
        loadPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(
				company, 1L);
		assertEquals("Number of Responses:", 1, responses.size());

		// Verify the response
		TransactionResponse tr = responses.get(0);
		verifySingleTransactionResponse(tr);
		Application.commitUnitOfWork();
	}

	@Test
	public void findTransactionResponsesUsingNullToken() {
		Application.beginUnitOfWork();
		Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(
				company, (Long) null);
		assertEquals("Number of Responses:", 2, responses.size());

		// Verify the response
		TransactionResponse tr = responses.get(0);
		verifySingleTransactionResponse(tr);
		Application.commitUnitOfWork();
	}

	@Test
	public void findTransactionResponsesTokenEqualMax() {
		Application.beginUnitOfWork();
		Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(
				company, 2L);
		assertEquals("Number of Responses:", 0, responses.size());
		Application.commitUnitOfWork();
	}

	@Ignore
	@Test
	public void findTransactionResponseRequestIdValid() {
		Application.beginUnitOfWork();
		Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		TransactionResponse response = TransactionResponse.findTransactionResponses(
				company, REQUEST_ID1);
		assertNotNull("TransactionResponse:", response);
		verifySingleTransactionResponse(response);
		Application.commitUnitOfWork();
	}

	@Test
	public void findTransactionResponseRequestIdNotExists() {
		Application.beginUnitOfWork();
		Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		TransactionResponse response = TransactionResponse.findTransactionResponses(
				company, "BAH");
		assertNull("TransactionResponse:", response);
		Application.commitUnitOfWork();
	}

	@Test
	public void findTransactionResponsesTokenGreaterMax() {
		Application.beginUnitOfWork();
		Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(
				company, 3L);
		assertEquals("Number of Responses:", 0, responses.size());
		Application.commitUnitOfWork();
	}

	@Ignore
	@Test
	public void findTransactionResponsesMultipleExpectTwo() {
		Application.beginUnitOfWork();
		Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(
				company, 0L);
		assertEquals("Number of Responses:", 2, responses.size());
		TransactionResponse firstResponse = responses.get(0);
		TransactionResponse secondResponse = responses.get(1);

		// Validate first token is a 1
		assertEquals("First Transaction Token", 2L, firstResponse.getTransactionTokenNumber());
		assertEquals("Second Transaction Token", 1L, secondResponse.getTransactionTokenNumber());
		Application.commitUnitOfWork();
	}

	@Ignore
	@Test
	public void findTransactionResponsesMultipleExpectOne() {
		Application.beginUnitOfWork();
		Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(
				company, 1L);
		assertEquals("Number of Responses:", 1, responses.size());
		Application.commitUnitOfWork();
	}

	@Test
	public void findTransactionResponsesMultipleExpectZero() {
		Application.beginUnitOfWork();
		Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(
				company, 6L);
		assertEquals("Number of Responses:", 0, responses.size());
		Application.commitUnitOfWork();
	}

	@Test
	public void createTransactionResponseDefaultToken() {
		Application.beginUnitOfWork();
		loadPayrollNoTransactionResponse();
		Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		PayrollRun payroll = PayrollRun.findPayrollRun(company,
				"PAYROLLP1");

		assertNotNull(payroll);
		TransactionResponse transactionResponse = TransactionResponse
				.createTransactionResponseForPayroll(payroll, "P1");
		Application.commitUnitOfWork();

		TransactionResponse persistedResponse = TransactionResponse
				.findTransactionResponses(company, "P1");
		assertEquals("Token", transactionResponse.getTransactionTokenNumber(), persistedResponse
				.getTransactionTokenNumber());
		assertEquals("Request ID", transactionResponse.getSourceRequestId(), persistedResponse
				.getSourceRequestId());
	}

	@Test
	public void createTransactionResponseTxCollection() {
		Application.beginUnitOfWork();
		loadPayrollNoTransactionResponse();
		Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		PayrollRun payroll = PayrollRun.findPayrollRun(company,
				"PAYROLLP1");

		assertNotNull(payroll);
		// FinancialTransactionBE.find
		DomainEntitySet<FinancialTransaction> transactions = FinancialTransaction
		.findFinancialTransactions(company.getSourceSystemCd(), company
				.getSourceCompanyId(), TransactionTypeCode.EmployeeDdCredit,
				TransactionStateCode.Created);
		
		Collection<FinancialTransaction> transactionsCollection = new DomainEntitySet<FinancialTransaction>();
		for (FinancialTransaction transaction: transactions) {
			transactionsCollection.add(transaction);
		}

		TransactionResponse transactionResponse = TransactionResponse.createTransactionResponse(company, transactionsCollection, REQUEST_ID1);
		Application.commitUnitOfWork();

		TransactionResponse persistedResponse = TransactionResponse
				.findTransactionResponses(company, REQUEST_ID1);
		assertEquals("Token", transactionResponse.getTransactionTokenNumber(), persistedResponse
				.getTransactionTokenNumber());
		assertEquals("Request ID", transactionResponse.getSourceRequestId(), persistedResponse
				.getSourceRequestId());
	}

	private void verifySingleTransactionResponse(TransactionResponse tr) {
		// Verify the response
		assertEquals("Verifying Token", 2L, tr.getTransactionTokenNumber());
		assertEquals("Request ID", REQUEST_ID1, tr.getSourceRequestId());

		// Verify the TransactionStates
		FinancialTransactionState fts = tr.getFinancialTransactionStates()
				.get(0);
		assertEquals("FinancialTransactionState", TransactionStateCode.Created, fts
				.getTransactionState().getTransactionStateCd());
	}

	@Test
	// Assert that both tokens are non-null, greater than zero, and that the
	// second is larger than the first
	public void testGetToken() {
		Application.beginUnitOfWork();
		Long firstTxnResponse = TransactionResponse.getNextTxnResponseToken();
		Long secondTxnResponse = TransactionResponse.getNextTxnResponseToken();

		assertNotNull(firstTxnResponse);
		assertNotNull(secondTxnResponse);

		assertTrue(firstTxnResponse.compareTo(0L) > 0);
		assertTrue(secondTxnResponse.compareTo(0L) > 0);

		assertTrue(secondTxnResponse.compareTo(firstTxnResponse) > 0);
		Application.commitUnitOfWork();
	}

	/**
	 * Reloads data to start back off with a fresh data state
	 */
	private void loadPayrolls() {
    	// Set System to 1 minute before cuttoff time for september 25
        SpcfCalendar sysDate = SpcfCalendar.createInstance(2007, SpcfCalendar.September, 25, 16, 59, 0, 0,
				SpcfTimeZone.getLocalTimeZone());
		PSPDate.setPSPTime(sysDate);
		Company123272727DataLoader dataloader2 = new Company123272727DataLoader();

		// Load Company
		dataloader2.setupTestCompany();

		dataloader2.savePayroll(dataloader2.loadBackgroundPayroll1(), 1L, "B1");
		dataloader2.savePayroll(dataloader2.loadPayroll(), 2L, "P1");
	}

	private void loadPayrollNoTransactionResponse() {
		// Set System to 1 minute before cuttoff time for september 25
        SpcfCalendar sysDate = SpcfCalendar.createInstance(2007, SpcfCalendar.September, 25, 16, 59, 0, 0,
				SpcfTimeZone.getLocalTimeZone());
		PSPDate.setPSPTime(sysDate);

		Company123272727DataLoader dataloader = new Company123272727DataLoader();

		// Load Company
		dataloader.setupTestCompany();

        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, COMPANY1, dataloader.loadPayroll());
	}

    @Test
	public void findTransactionResponsesByTxId() {
        PayrollServices.beginUnitOfWork();
        loadPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBOE);
		DomainEntitySet<FinancialTransaction> finTxs = FinancialTransaction.findFinancialTransactions(
                                SourceSystemCode.QBOE,
                                COMPANY1, TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(
				finTxs.get(0));
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of Responses:", 1, responses.size());

		// Verify the response
		TransactionResponse tr = responses.get(0);

		assertEquals("Verifying Token", 1L, tr.getTransactionTokenNumber());
		assertEquals("Request ID", "B1", tr.getSourceRequestId());

		// Verify the TransactionStates
		FinancialTransactionState fts = tr.getFinancialTransactionStates()
				.get(0);
		assertEquals("FinancialTransactionState", TransactionStateCode.Created, fts
				.getTransactionState().getTransactionStateCd());
	}
}
