package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.PrimaryValidations;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.utils.ACHCompare;
import com.intuit.sbd.payroll.psp.batchjobs.utils.CompareResults;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReader;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReaderFactory;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.NACHAFileStatus;
import com.intuit.sbd.payroll.psp.domain.NACHAFileType;
import com.intuit.sbd.payroll.psp.domain.PaymentStatus;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by cmehta1 on 8/13/18.
 */
public class TestPrimaryValidations {


    public static ACHCompare validateFile(String pCreatedFileName, String pExpectedFileName, NACHAFileType pFileType) {

        try {
            String expectedFileName = null;
            CompareResults validatedCompareResults = null;
            ACHCompare validatedComparedACH = null;

            BufferedReader expectedReader = new BufferedReader(new FileReader(pExpectedFileName));
            PgpReader pgpReader = PgpReaderFactory.createInstance();
            pgpReader.open(pCreatedFileName);

            ACHCompare achCompare = new ACHCompare();
            CompareResults compareResults = achCompare.compareACH(expectedReader, pgpReader, pFileType);
            if (compareResults.getStatus()) {
                expectedFileName = pExpectedFileName;
                validatedCompareResults = compareResults;
                validatedComparedACH = achCompare;
            } else {
                System.out.println("Actual:\n" + FileUtils.readFileToString(new File(pCreatedFileName)));
                System.out.println("Expected:\n" + FileUtils.readFileToString(new File(pExpectedFileName)));
                fail(compareResults.toString());
            }
            assertNotNull("File "+pCreatedFileName+" matches expected file "+expectedFileName,validatedCompareResults);
            return validatedComparedACH;
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.getMessage());
        }
        return null;
    }


    public static void verifyMMTxnAndTraceNums(DomainEntitySet<FinancialTransaction> pFinTxns, OffloadBatch pOffloadBatch) {
        for (FinancialTransaction currFinTxn : pFinTxns) {
            assertEquals("Financial transaction is executed", TransactionStateCode.Executed,
                    currFinTxn.getCurrentTransactionState().getTransactionStateCd());
            MoneyMovementTransaction mmTxn = currFinTxn.getMoneyMovementTransaction();
            OffloadBatch associatedOffloadBatch = mmTxn.getOffloadBatch();
            assertEquals("Offload batch for mmTxn", pOffloadBatch, associatedOffloadBatch);
            assertEquals("mmTxn status", PaymentStatus.Executed, mmTxn.getStatus());
            DomainEntitySet<EntryDetailRecord> entryDetailRecords = mmTxn.getEntryDetailRecordCollection();
            assertTrue("Number of entryDetailRecords", entryDetailRecords.size() >= 1);
            for (EntryDetailRecord currRecord : entryDetailRecords) {
                //Ensure that Intuit transactions don't have a trace number and that non-Intuit transactions do have a trace number
                if (currRecord.getRecordData()!=null) {
                    assertNotNull(currRecord.getTraceNumber());
                } else {
                    assertNull(currRecord.getTraceNumber());
                }
            }
        }
    }

    public static void assertYearMonthDayEquals(SpcfCalendar pTimeToCompare, SpcfCalendar pPSPTime) {
        assertEquals(pTimeToCompare.getDay(), pPSPTime.getDay());
        assertEquals(pTimeToCompare.getMonth(), pPSPTime.getMonth());
        assertEquals(pTimeToCompare.getYear(), pPSPTime.getYear());
    }

    public static void validateNACHAFileFinalized(NACHAFile pCreatedFile, NACHAFileType pFileType,
                                                  SpcfMoney pExpectedTotalCredits, SpcfMoney pExpectedTotalDebits) {
        assertNull(pCreatedFile.getConfirmationCode());
        assertNull(pCreatedFile.getConfirmationDate());
        assertNull(pCreatedFile.getTransmissionDate());
        assertEquals("Total debit amount: ", pExpectedTotalDebits, pCreatedFile.getDebitTxnTotalAmount());
        assertEquals("Total credit amount: ", pExpectedTotalCredits, pCreatedFile.getCreditTxnTotalAmount());
        boolean b = Pattern.matches("\\p{Alnum}", pCreatedFile.getFileIDModifier());
        assertTrue("File ID modifier matches regular expression", b);

        assertNotNull("File name", pCreatedFile.getFileName());
        String expectedPath = ConfigurationManager
                .getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir");

        int indexOfLastSeparator = pCreatedFile.getFileName().lastIndexOf(File.separator);
        String actualPath = pCreatedFile.getFileName().substring(0, indexOfLastSeparator);
        String actualName = pCreatedFile.getFileName()
                .substring(indexOfLastSeparator + 1, pCreatedFile.getFileName().length());

        boolean enableEncryption = SystemParameter.findBooleanValue(
                SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
        String fileExt = enableEncryption ? ".pgp" : ".txt";

        assertEquals("Path", expectedPath, actualPath);
        assertEquals("Length of file name", 22, actualName.length());
        assertTrue("File ends with " + pFileType.toString() + fileExt,
                actualName.endsWith(pFileType.toString() + fileExt));
        assertTrue("File begins with d.", actualName.startsWith("d."));
        assertEquals("File Type", pFileType, pCreatedFile.getFileType());
        //assertYearMonthDayEquals(PSPDate.getPSPTime(), pCreatedFile.getFinalizationDate().toLocal());
        assertEquals("File Status", NACHAFileStatus.Finalized, pCreatedFile.getStatus());
        assertYearMonthDayEquals(PSPDate.getPSPTime(), pCreatedFile.getStatusEffectiveDate().toLocal());
    }

    public static void validateFileAndTraceNumbers(String pCreatedFileName, String pExpectedFileName, NACHAFileType pFileType) {
        try {

            ACHCompare achCompare = validateFile(pCreatedFileName, pExpectedFileName, pFileType);
            HashMap<String, String> recordsTraceNums = achCompare.getRecordTraceNumMap();

            for (String currRecord : recordsTraceNums.keySet()) {
                String currTraceNum = recordsTraceNums.get(currRecord);
                EntryDetailRecord entryDetailRecord = EntryDetailRecord.findEntryDetailRecordsWithTraceNumber(Long.parseLong(currTraceNum));
                if (entryDetailRecord != null) {
                    boolean foundAMatch=false;
                    Long expectedTraceNumber = Long.parseLong(currTraceNum);
                    Long actualTraceNumber = Long.parseLong(entryDetailRecord.getTraceNumber());
                    if (actualTraceNumber.equals(expectedTraceNumber)) {
                        foundAMatch=true;
                    }
                    assertTrue("Found a match for "+currTraceNum, foundAMatch);
                }
            }

            //ensure trace numbers are not duplicated in the file
            ArrayList<String> foundTraceNumbers = new ArrayList<String>();

            for (String traceNumber : achCompare.getTraceNumbers()) {
                if (foundTraceNumbers.contains(traceNumber)) {
                    fail("Found duplicate trace number in file: "+pCreatedFileName+" of: "+traceNumber);
                }
                foundTraceNumbers.add(traceNumber);
            }

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.getMessage());
        }

    }
}
