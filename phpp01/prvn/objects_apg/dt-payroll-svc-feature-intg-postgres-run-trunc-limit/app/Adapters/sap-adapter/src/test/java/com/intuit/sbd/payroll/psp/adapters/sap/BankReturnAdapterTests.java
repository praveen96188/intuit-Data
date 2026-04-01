package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.BankReturnAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBankReturn;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSearchResults;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.processors.NightlyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: rnorian
 * Date: 6/4/12
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class BankReturnAdapterTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testBankReturnsSearchDoesNotReturnCreatedStateTxnReturns() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForFirstNSFReturn();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 1, returnList.size());

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        TransactionReturn transactionReturn = returnList.get(0);
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

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

        BankReturnAdapter bankReturnAdapter = new BankReturnAdapter();
        try {
            SAPSearchResults<SAPBankReturn> searchResults = bankReturnAdapter.findCompanyBankReturns(null, null, null, true, true, null, null, false, null, -1, null, true, 0, 100, false);
            for (SAPBankReturn sapBankReturn : searchResults.getReturnsList()) {
                assertFalse("Return Status not Created", sapBankReturn.getStatusCd().equalsIgnoreCase(TransactionReturnStatusCode.Created.name()));
            }
        } catch (Throwable pThrowable) {
            fail(pThrowable.getMessage());
        }


        BatchJobManager.runJobStep(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.ProcessAchReturnsBatch.class);

        // test that they can be seen when in open state
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> returns = Application.find(TransactionReturn.class);
        for (TransactionReturn aReturn : returns) {
            aReturn.setReturnStatusCd(TransactionReturnStatusCode.Open);
            Application.save(aReturn);
        }
        PayrollServices.commitUnitOfWork();


        try {
            SAPSearchResults<SAPBankReturn> searchResults = bankReturnAdapter.findCompanyBankReturns(null, null, null, true, true, null, null, false, null, -1, null, true, 0, 100, false);
            assertTrue("returns exist", searchResults.getReturnsList().size() > 0);
            for (SAPBankReturn sapBankReturn : searchResults.getReturnsList()) {
                assertFalse("Return Status not Created", sapBankReturn.getStatusCd().equalsIgnoreCase(TransactionReturnStatusCode.Created.name()));
            }
        } catch (Throwable pThrowable) {
            fail(pThrowable.getMessage());
        }

        // test that they can be seen when in open state
        PayrollServices.beginUnitOfWork();
        returns = Application.find(TransactionReturn.class);
        for (TransactionReturn aReturn : returns) {
            aReturn.setReturnStatusCd(TransactionReturnStatusCode.Resolved);
            Application.save(aReturn);
        }
        PayrollServices.commitUnitOfWork();

        try {
            SAPSearchResults<SAPBankReturn> searchResults = bankReturnAdapter.findCompanyBankReturns(null, null, null, true, true, null, null, false, null, -1, null, true, 0, 100, false);
            assertTrue("returns exist", searchResults.getReturnsList().size() > 0);
            for (SAPBankReturn sapBankReturn : searchResults.getReturnsList()) {
                assertFalse("Return Status not Created", sapBankReturn.getStatusCd().equalsIgnoreCase(TransactionReturnStatusCode.Created.name()));
            }
        } catch (Throwable pThrowable) {
            fail(pThrowable.getMessage());
        }

        BatchJobManager.runJob(BatchJobType.NightlyBatchJobs, "achret");
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
                    if (entryType == '7') { //addenda rec type
                        String orgTraceNumber = currentLine.substring(6, 21);
                        currentLine = currentLine.replaceAll(orgTraceNumber,
                                                             StringFormatter.formatLong(Long.parseLong(pTraceNumber), 15));
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
