package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.MockSimpleSftpFile;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.processors.NightlyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpAchReturnsFileDownload;
import com.intuit.sbd.payroll.psp.common.utils.SftpFactory;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 29, 2008
 * Time: 3:50:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestACHReturns {
    private static final char ADDENDA_REC_TYPE = '7';


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
    public void testMainProcess() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForFirstNSFReturn();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 1, returnList.size());

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        //Offload EMPLOYER_DD_REDEBIT transactions
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Find the Employer DD Redebit financial transactions
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 ERDDREDB EX txns", 1, finTxnList.size());

        //Get the EntryDetail Record for MonenyMovement Transaction
        DomainEntitySet<EntryDetailRecord> entryDetailRecords =
                finTxnList.get(0).getMoneyMovementTransaction().getEntryDetailRecordCollection();

        EntryDetailRecord companyPortionOfEDR = null;
        com.intuit.sbd.payroll.psp.DomainEntitySet<EntryDetailRecord> aryEntryDetailRecords = entryDetailRecords;
        for (EntryDetailRecord currEDR : aryEntryDetailRecords) {
            if (currEDR.getIntuitBankAccount() == null) {
                companyPortionOfEDR = currEDR;
            }
        }
        assertNotNull("Found company portion of entry detail record", companyPortionOfEDR);

        String actualTraceNumber;
        actualTraceNumber = companyPortionOfEDR.getTraceNumber();

        assertNotNull("Company portion EDR does NOT have a null trace number", actualTraceNumber);

        //Update the Actual Trace number in the ACHReturnFile
        updateTraceNumberInACHReturnFile(actualTraceNumber, "achreturn/AchReturns.txt", "AchReturns1.txt");
        PayrollServices.commitUnitOfWork();

        String absolutePath = Application.findFileOnClassPath("achreturn");

        File returnFile = new File(absolutePath, "AchReturns1.txt");
        BatchJobManager.runJob(BatchJobType.NightlyBatchJobs, "achret", returnFile.getAbsolutePath());

        PayrollServices.beginUnitOfWork();

        FinancialTransaction finTxn = Application.findById(FinancialTransaction.class, finTxnList.get(0).getId());

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRefundCredit,
                        TransactionStateCode.Cancelled);

        //Assertion for Update FinancialTransactionRule
        assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                finTxn.getCurrentTransactionState().getTransactionStateCd());

        //Assertion for Update payrollrun Status rule
        assertEquals("Update PayrollRun Status Rule ", finTxn.getPayrollRun().getPayrollRunStatus(),
                PayrollStatus.ReturnedTwice);

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NSF, CompanyEventStatus.Active, null, null);

        //Assertion for Create NSF System Event Rule - NSFSubType - SecondNSF
        assertEquals("Company Events", 2, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            FinancialTransaction eventFinTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId)));
            if (eventFinTxn.equals(finTxn)) {
                assertEquals("Verification Status", EnumUtils.getReadableName(NSFSubTypeType.SecondNSF), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
                assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
            }
        }

        DomainEntitySet<CompanyEvent> strikeEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.Strike, CompanyEventStatus.Active, null, null);

        //Assertion For Add Company Strike rule -- StrikeReason - NSFAutoRedebit
        assertEquals("Strike Events", 1, strikeEventsList.size());

        for (CompanyEvent returnEvent : strikeEventsList) {
            assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFAutoRedebit), returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testDownload_DontCreateDuplicationTransactionReturnBatch_PSRV004193() {
        SftpFactory.setInstanceClass(MockSimpleSftpFile.class);
        Transporter sftp = BatchUtils.getBankSftpConnection(new SftpAchReturnsFileDownload().getAchReturnsFileDownloadListener());
        assertEquals(MockSimpleSftpFile.class, sftp.getClass());
        try {
        sftp.downloadFile("test");
        DomainEntitySet<TransactionReturnBatch> list = Application.find(TransactionReturnBatch.class);
        assertEquals(list.size(),1);
        sftp.downloadFile("test");
        list = Application.find(TransactionReturnBatch.class);
        assertEquals(list.size(),1);
        sftp.deleteRemoteFile("test");
        list = Application.find(TransactionReturnBatch.class);
        assertEquals(list.size(),1);
        SftpFactory.setInstanceClass(Transporter.class);
        } catch (Exception e) {
            System.out.println("testDownload_DontCreateDuplicationTransactionReturnBatch_PSRV004193 failed");
            e.printStackTrace();
        }
    }


    @Test
    public void testRerunning() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForFirstNSFReturn();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 1, returnList.size());

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        TransactionReturn transactionReturn = Application.findById(TransactionReturn.class, returnList.get(0).getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        //Offload EMPLOYER_DD_REDEBIT transactions
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Find the Employer DD Redebit financial transactions
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                                           TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 ERDDREDB EX txns", 1, finTxnList.size());

        //Get the EntryDetail Record for MonenyMovement Transaction
        DomainEntitySet<EntryDetailRecord> entryDetailRecords =
                finTxnList.get(0).getMoneyMovementTransaction().getEntryDetailRecordCollection();

        EntryDetailRecord companyPortionOfEDR = null;
        com.intuit.sbd.payroll.psp.DomainEntitySet<EntryDetailRecord> aryEntryDetailRecords = entryDetailRecords;
        for (EntryDetailRecord currEDR : aryEntryDetailRecords) {
            if (currEDR.getIntuitBankAccount() == null) {
                companyPortionOfEDR = currEDR;
            }
        }
        assertNotNull("Found company portion of entry detail record", companyPortionOfEDR);

        String actualTraceNumber;
        actualTraceNumber = companyPortionOfEDR.getTraceNumber();

        assertNotNull("Company portion EDR does NOT have a null trace number", actualTraceNumber);

        //Update the Actual Trace number in the ACHReturnFile
        updateTraceNumberInACHReturnFile(actualTraceNumber, "achreturn/AchReturns.txt", "AchReturns1.txt");
        PayrollServices.commitUnitOfWork();

        String absolutePath = Application.findFileOnClassPath("achreturn");

        File returnFile = new File(absolutePath, "AchReturns1.txt");
        BatchJobManager.runJobStep(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.CreateReturnBatch.class, returnFile.getAbsolutePath());
        BatchJobManager.runJobStep(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.PersistReturnsFile.class);
        // rerun intentionally
        BatchJobManager.runJobStep(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.PersistReturnsFile.class);
        BatchJobManager.runJobStep(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.ProcessAchReturnsBatch.class);
        // rerun intentionally
        BatchJobManager.runJobStep(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.ProcessAchReturnsBatch.class);
        BatchJobManager.runJobStep(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.NotifyReturnProcessingResults.class);
        // rerun intentionally
        BatchJobManager.runJobStep(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.NotifyReturnProcessingResults.class);

        PayrollServices.beginUnitOfWork();

        FinancialTransaction finTxn = Application.findById(FinancialTransaction.class, finTxnList.get(0).getId());

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                                                                          findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                                                                                                    TransactionTypeCode.EmployerDdRefundCredit,
                                                                                                    TransactionStateCode.Cancelled);

        //Assertion for Update FinancialTransactionRule
        assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                     finTxn.getCurrentTransactionState().getTransactionStateCd());

        //Assertion for Update payrollrun Status rule
        assertEquals("Update PayrollRun Status Rule ", finTxn.getPayrollRun().getPayrollRunStatus(),
                     PayrollStatus.ReturnedTwice);

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                                                                                         EventTypeCode.NSF, CompanyEventStatus.Active, null, null);

        //Assertion for Create NSF System Event Rule - NSFSubType - SecondNSF
        assertEquals("Company Events", 2, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            FinancialTransaction eventFinTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId)));
            if (eventFinTxn.equals(finTxn)) {
                assertEquals("Verification Status", EnumUtils.getReadableName(NSFSubTypeType.SecondNSF), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
                assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
            }
        }

        DomainEntitySet<CompanyEvent> strikeEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                                                                                        EventTypeCode.Strike, CompanyEventStatus.Active, null, null);

        //Assertion For Add Company Strike rule -- StrikeReason - NSFAutoRedebit
        assertEquals("Strike Events", 1, strikeEventsList.size());

        for (CompanyEvent returnEvent : strikeEventsList) {
            assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFAutoRedebit), returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testErrorLogging() {
        Application.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        loader.loadDataHappyPath();

        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        //Find the Employer DD Debit financial transactions
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        assertEquals("Number of C1 ERDDREDB EX txns", 2, finTxnList.size());

        //Get the EntryDetail Record for MonenyMovement Transaction
        DomainEntitySet<EntryDetailRecord> entryDetailRecords =
                finTxnList.get(0).getMoneyMovementTransaction().getEntryDetailRecordCollection();

        EntryDetailRecord companyPortionOfEDR = null;
        com.intuit.sbd.payroll.psp.DomainEntitySet<EntryDetailRecord> aryEntryDetailRecords = entryDetailRecords;
        for (EntryDetailRecord currEDR : aryEntryDetailRecords) {
            if (currEDR.getIntuitBankAccount() == null) {
                companyPortionOfEDR = currEDR;
            }
        }
        assertNotNull("Found company portion of entry detail record", companyPortionOfEDR);

        String actualTraceNumber;
        actualTraceNumber = companyPortionOfEDR.getTraceNumber();

        assertNotNull("Company portion EDR does NOT have a null trace number", actualTraceNumber);

        //Update the Actual Trace number in the ACHReturnFile
        updateTraceNumberInACHReturnFile(actualTraceNumber, "achreturn/AchReturns.txt", "AchReturns2.txt");
        PayrollServices.commitUnitOfWork();
        // Update the ER DD DB status to cancelled
        PayrollServices.beginUnitOfWork();
        finTxnList = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        // intentionally using the BE instead of process to update the state, since the goal of this testcase is
        // to verify the logging details if an error occurs while processing ACHReturns file
        // so to create a scenario where ReturnsHandler throws a RuntimeException, updating the state to Cancelled 
        finTxnList.get(0).updateFinancialTransactionState(TransactionStateCode.Cancelled);
        PayrollServices.commitUnitOfWork();
        
        String absolutePath = Application.findFileOnClassPath("achreturn");

        try {
            ReturnFileParser returnFileParser = new ReturnFileParser();
            returnFileParser.processFile(new File(absolutePath, "AchReturns2.txt"));
            assertTrue(false);
        } catch(Throwable ex) {
            PayrollServices.rollbackUnitOfWork();
            assertTrue(true);
        }

    }

    @Test
    public void testErrorDoesNotBreakBatchAndCreatesEmail() {
        Application.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        loader.loadDataHappyPath();

        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        //Find the Employer DD Debit financial transactions
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxnList = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                                           TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        assertEquals("Number of C1 ERDDREDB EX txns", 2, finTxnList.size());

        //Get the EntryDetail Record for MonenyMovement Transaction
        DomainEntitySet<EntryDetailRecord> entryDetailRecords =
                finTxnList.get(0).getMoneyMovementTransaction().getEntryDetailRecordCollection();

        EntryDetailRecord companyPortionOfEDR = null;
        com.intuit.sbd.payroll.psp.DomainEntitySet<EntryDetailRecord> aryEntryDetailRecords = entryDetailRecords;
        for (EntryDetailRecord currEDR : aryEntryDetailRecords) {
            if (currEDR.getIntuitBankAccount() == null) {
                companyPortionOfEDR = currEDR;
            }
        }
        assertNotNull("Found company portion of entry detail record", companyPortionOfEDR);

        String actualTraceNumber;
        actualTraceNumber = companyPortionOfEDR.getTraceNumber();

        assertNotNull("Company portion EDR does NOT have a null trace number", actualTraceNumber);

        //Update the Actual Trace number in the ACHReturnFile
        updateTraceNumberInACHReturnFile(actualTraceNumber, "achreturn/AchReturns-InvalidReturnCode.txt", "AchReturns2.txt");
        PayrollServices.commitUnitOfWork();

        String absolutePath = Application.findFileOnClassPath("achreturn");
        File returnFile = new File(absolutePath, "AchReturns2.txt");
        String jobid = null;
        try{
            jobid = BatchJobManager.runJob(BatchJobType.NightlyBatchJobs, "achret", returnFile.getAbsolutePath());
            DomainEntitySet<TransactionReturnBatch> list = Application.find(TransactionReturnBatch.class);
            org.junit.Assert.assertEquals(list.get(0).getStatusCd(),TransactionReturnBatchStatusCode.Completed);
            DomainEntitySet<TransactionReturn> returnList = Application.find(TransactionReturn.class,  TransactionReturn.ReturnBatch().Id().equalTo(list.get(0).getId()));
            assertEquals(returnList.getFirst().getReturnStatusCd(), TransactionReturnStatusCode.Error);
        }catch(Throwable ex){
            assertTrue(false);
        }

    }

    /**
     * Function to update the latest trace number in the ACHReturn file, which is retrieved from the
     * Moneymovement Transaction
     * @param pTraceNumber String
     */
    private void updateTraceNumberInACHReturnFile(String pTraceNumber, String pFilePath, String pFileName){
        String achFileName =Application.findFileOnClassPath(pFilePath);

        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(achFileName));
            String currentLine = bufferedReader.readLine();

            String absolutePath = Application.findFileOnClassPath("achreturn");

            File file = new File(absolutePath + "/"+pFileName);
            file.createNewFile();

            FileWriter f = new FileWriter(file);
            char entryType;
            while (currentLine != null) {
                if (currentLine.length() != 0) {
                    entryType = currentLine.charAt(0);
                    if (entryType == ADDENDA_REC_TYPE) {
                        String orgTraceNumber = currentLine.substring(6, 21);
                        currentLine = currentLine.replaceAll(orgTraceNumber,
                                StringFormatter.formatLong(Long.parseLong(pTraceNumber),15));
                        f.write(currentLine);
                    }else{
                        f.write(currentLine);
                    }
                    f.write("\n");
                }

                currentLine = bufferedReader.readLine();
            }
            f.flush();

    } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.getMessage());
        }
    }
}
