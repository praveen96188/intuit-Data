package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class ACHTraceIdFileParserTest {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testHappyPathParseAndPersist() {

        //Create test data
        Application.beginUnitOfWork();
        ACHReturnsDataLoader.loadDataHappyPath2Day();

        DomainEntitySet<EntryDetailRecord> entitySet = Application.find(EntryDetailRecord.class,
                EntryDetailRecord.TraceNumber().isNotNull());

        Long traceNumber = 100010l;
        for (EntryDetailRecord edr : entitySet) {
            edr.setTraceNumber(Long.toString(traceNumber));
            traceNumber++;
        }

        Application.commitUnitOfWork();

        ACHTraceIdFileParser achTraceIdFileParser = new ACHTraceIdFileParser();
        try {
            String file = Application.findFileOnClassPath("achtraceid/SampleTraceFile.txt");
            achTraceIdFileParser.parseAndUpdateTraceId(file);
            Application.beginUnitOfWork();
            EntryDetailRecord edr = EntryDetailRecord.findEntryDetailRecordsWithTraceNumber(Long.parseLong("000000000100010"));
            assertNotNull("EDR for given trace number 110010 not found", edr);
            assertEquals("TraceId for the given ACH record does not match", "021000020517706", edr.getJPMCTraceNumber());

            edr = EntryDetailRecord.findEntryDetailRecordsWithTraceNumber(Long.parseLong("000000000100011"));
            assertNotNull("EDR for given trace number 110010 not found", edr);
            assertEquals("TraceId for the given ACH record does not match", "021000020517705", edr.getJPMCTraceNumber());
            Application.rollbackUnitOfWork();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testEmptyTraceFileParseAndPersist() {

        //Create test data
        Application.beginUnitOfWork();
        ACHReturnsDataLoader.loadDataHappyPath2Day();
        Application.commitUnitOfWork();

        ACHTraceIdFileParser achTraceIdFileParser = new ACHTraceIdFileParser();
        try {
            String file = Application.findFileOnClassPath("achtraceid/EmptyTraceFile.txt");
            achTraceIdFileParser.parseAndUpdateTraceId(file);
            Application.beginUnitOfWork();
            DomainEntitySet<EntryDetailRecord> entitySet = Application.find(EntryDetailRecord.class,
                    EntryDetailRecord.JPMCTraceNumber().isNotNull());
            assertEquals("Found EDRs with JPMC trace Ids", 0, entitySet.size());
            Application.rollbackUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}