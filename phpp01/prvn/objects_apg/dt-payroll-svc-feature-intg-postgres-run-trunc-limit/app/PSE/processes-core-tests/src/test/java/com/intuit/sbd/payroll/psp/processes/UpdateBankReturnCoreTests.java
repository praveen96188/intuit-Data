package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.RedebitAddTestDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jan 28, 2008
 * Time: 10:52:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateBankReturnCoreTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testCompanyDoesNotExist() {
        // load data
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txReturnDataLoader =
                new RedebitAddTestDataLoader();
        txReturnDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[] {
                                    TransactionTypeCode.EmployerDdDebit},
                            new TransactionStateCode[] { TransactionStateCode.Returned } );
        // Verify the Tx Response before status update
        assertTrue("transaction return exists: ",
                TransactionReturn.findFirstUnresolvedTransactionReturn(financialTxs.get(0)) != null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult =
                PayrollServices.financialTransactionManager.updateBankReturnStatus(SourceSystemCode.QBOE, "InvalidCompanyId",
                financialTxs.get(0).getId().toString(), TransactionReturnStatusCode.Resolved, "From Created to Resolved");

        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());

    }

    @Test
    public void testInvalidCompanyParameters() {
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult =
                PayrollServices.financialTransactionManager.updateBankReturnStatus(null, "123272727",
                "PSETransactionId", TransactionReturnStatusCode.Resolved, "From Created to Resolved");

        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        PayrollServices.beginUnitOfWork();
        processResult =
                PayrollServices.financialTransactionManager.updateBankReturnStatus(SourceSystemCode.QBOE, null,
                "PSETransactionId", TransactionReturnStatusCode.Resolved, "From Created to Resolved");

        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    @Test
    public void testInvalidPSETransactionId() {
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult =
                PayrollServices.financialTransactionManager.updateBankReturnStatus(SourceSystemCode.QBOE, "123272727",
                "2c915611-1732-d4c4-0117-32d53e810028", TransactionReturnStatusCode.Resolved, "From Created to Resolved");
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "264", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Financial Transaction 2c915611-1732-d4c4-0117-32d53e810028 does not exist for company QBOE:123272727.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    @Test
    public void testNullPSETransactionId() {
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult =
                PayrollServices.financialTransactionManager.updateBankReturnStatus(SourceSystemCode.QBOE, "123272727",
                null, TransactionReturnStatusCode.Resolved, "From Created to Resolved");
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "264", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Financial Transaction null does not exist for company QBOE:123272727.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    @Test
    public void testNullNotes() {
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                            new TransactionStateCode[] {TransactionStateCode.Returned} );
        // Verify the Tx Response before status update
        assertTrue("transaction return exists: ",
                TransactionReturn.findFirstUnresolvedTransactionReturn(financialTxs.get(0)) != null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult =
                PayrollServices.financialTransactionManager.updateBankReturnStatus(SourceSystemCode.QBOE, "123272727",
                financialTxs.get(0).getId().toString(), TransactionReturnStatusCode.Resolved, null);
        PayrollServices.commitUnitOfWork();

        // Verify the transaction return status and company notes
        PayrollServices.beginUnitOfWork();
        TransactionReturn txRet =
                TransactionReturn.findFirstUnresolvedTransactionReturn(financialTxs.get(0));
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        DomainEntitySet<CompanyNote> companyNotes = company.getCompanyNoteCollection();

        assertTrue("No open transaction returns: ", txRet == null);

        assertEquals("Number of company notes:",companyNotes.size(), 1);
        Iterator<CompanyNote> itr = companyNotes.iterator();
        CompanyNote note = null;
        while(itr.hasNext()) {
            note = itr.next();
        }
        assertEquals("Company Note Message:",
                "Bank Return status changed to Resolved.  "
                + "PSE Transaction Id: " + financialTxs.get(0).getId().toString() + ".  "
                , note.getNotes());
        PayrollServices.commitUnitOfWork();

    }


    @Test
    public void testUpdateBankReturn() {
        // load data
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txReturnDataLoader =
                new RedebitAddTestDataLoader();
        txReturnDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                            new TransactionStateCode[] {TransactionStateCode.Returned} );
        // Verify the Tx Response before status update
        assertTrue("transaction return exists: ",
                TransactionReturn.findFirstUnresolvedTransactionReturn(financialTxs.get(0)) != null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.financialTransactionManager.updateBankReturnStatus(SourceSystemCode.QBOE, "123272727",
                financialTxs.get(0).getId().toString(), TransactionReturnStatusCode.Resolved, "From Created to Resolved");

        PayrollServices.commitUnitOfWork();

        // Verify the transaction return status and company notes
        PayrollServices.beginUnitOfWork();
        TransactionReturn txRet =
                TransactionReturn.findFirstUnresolvedTransactionReturn(financialTxs.get(0));
        company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        DomainEntitySet<CompanyNote> companyNotes = company.getCompanyNoteCollection();

        assertTrue("No open transaction returns: ", txRet == null);

        assertEquals("Number of company notes:",companyNotes.size(), 1);
        Iterator<CompanyNote> itr = companyNotes.iterator();
        CompanyNote note = null;
        while(itr.hasNext()) {
            note = itr.next();
        }
        assertEquals("Company Note Message:",
                "Bank Return status changed to Resolved.  "
                + "PSE Transaction Id: " + financialTxs.get(0).getId().toString() + "."
                + "  From Created to Resolved", note.getNotes());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testUpdateBankReturn_Reopen() {
        // load data
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txReturnDataLoader =
                new RedebitAddTestDataLoader();
        txReturnDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                            new TransactionStateCode[] {TransactionStateCode.Returned} );
        // Verify the Tx Response before status update
        assertTrue("transaction return exists: ",
                TransactionReturn.findFirstUnresolvedTransactionReturn(financialTxs.get(0)) != null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.financialTransactionManager.updateBankReturnStatus(SourceSystemCode.QBOE, "123272727",
                financialTxs.get(0).getId().toString(), TransactionReturnStatusCode.Resolved, "From Created to Resolved");

        PayrollServices.commitUnitOfWork();

        // Verify the transaction return status and company notes
        PayrollServices.beginUnitOfWork();
        TransactionReturn txRet =
                TransactionReturn.findFirstUnresolvedTransactionReturn(financialTxs.get(0));
        company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        DomainEntitySet<CompanyNote> companyNotes = company.getCompanyNoteCollection();

        assertTrue("No open transaction returns: ", txRet == null);

        assertEquals("Number of company notes:",companyNotes.size(), 1);
        Iterator<CompanyNote> itr = companyNotes.iterator();
        CompanyNote note = null;
        while(itr.hasNext()) {
            note = itr.next();
        }
        assertEquals("Company Note Message:",
                "Bank Return status changed to Resolved.  "
                + "PSE Transaction Id: " + financialTxs.get(0).getId().toString() + "."
                + "  From Created to Resolved", note.getNotes());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.financialTransactionManager.updateBankReturnStatus(SourceSystemCode.QBOE, "123272727",
                financialTxs.get(0).getId().toString(), TransactionReturnStatusCode.Created, "From Resolved to Created");

        PayrollServices.commitUnitOfWork();

        // Verify the transaction return status and company notes
        PayrollServices.beginUnitOfWork();
        TransactionReturn txRetResolved =
                TransactionReturn.findFirstUnresolvedTransactionReturn(financialTxs.get(0));
        TransactionReturn txRetOpen =
                TransactionReturn.findFirstResolvedTransactionReturn(financialTxs.get(0));
        company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        DomainEntitySet<CompanyNote> companyNotesReopen = company.getCompanyNoteCollection();

        assertFalse("1 open transaction returns: ", txRetResolved == null);
        assertTrue("No closed transaction returns: ", txRetOpen == null);

        assertEquals("Number of company notes:",companyNotesReopen.size(), 2);
        Iterator<CompanyNote> notesItr = companyNotesReopen.iterator();
        CompanyNote noteReopen = null;
        boolean foundIt=false;
        while(notesItr.hasNext()) {
            noteReopen = notesItr.next();
            if (noteReopen.getNotes().equals("Bank Return status changed to Created.  "
                + "PSE Transaction Id: " + financialTxs.get(0).getId().toString() + ".  From Resolved to Created")) {
                foundIt=true;

            }
        }
        assertTrue("Found reopen company note", foundIt);
        PayrollServices.commitUnitOfWork();

    }


}
