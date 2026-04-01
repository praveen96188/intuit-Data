package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.employeeTotals.CalculateEmployeeTotalsTestsHelper;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import intuit.osp.common.utils.FileUtils;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: jpatel
 * Date: Jun 1, 2009
 * Time: 1:55:50 PM
 */
public class ATFDataExtractProcessorTests {
    Company1Dataloader data1Load = new Company1Dataloader();
    DataLoader dataLoad = new DataLoader();
    private int achTaxOffloadOffset;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2009, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = Application.refresh(achTaxOffloadOffsetParam);
        achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue());
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    private String extractFile(ATFDataExtractFile pExtractFile) {
        String extractedFileName = "";
        try {
            extractedFileName = FileUtils.gUnZip(pExtractFile.getFileName());
        } catch (Exception ex) {
            ex.printStackTrace();
            TestCase.fail(ex.getMessage());
        }
        return extractedFileName;
    }

    @Test
    public void testWageLimitExtract() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110701000000");

        // Make sure the Wage Limit extract will be executed.
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PERFORM_ATF_WAGE_LIMITS_EXTRACT, "true");

        //WageLimit table locally will have only current year data, now it 2013. inserting data for previous years below
        // Delete any entries for this law except current year
        Application.executeSqlCommand("delete from psp_wage_limit where cast(law_fk as int) = 67 and effective_year_quarter not like (select to_char(current_timestamp, 'yyyy') from dual)||'%'", false);

        // Set the wage limits for CA SDI-EE (Law ID 67) for each quarter from 2011 Q1 through 2012 Q1.
        Law law = Application.findById(Law.class, "67");
        WageLimit wageLimit = new WageLimit();
        wageLimit.setWageLimitId("006720111");
        wageLimit.setLaw(law);
        wageLimit.setAmount(new SpcfMoney("5000.00"));
        wageLimit.setEffectiveYearQuarter("20111");
        Application.getHibernateSession().saveOrUpdate(wageLimit);
        wageLimit = new WageLimit();
        wageLimit.setWageLimitId("006720112");
        wageLimit.setLaw(law);
        wageLimit.setAmount(new SpcfMoney("6000.00"));
        wageLimit.setEffectiveYearQuarter("20112");
        Application.getHibernateSession().saveOrUpdate(wageLimit);
        wageLimit = new WageLimit();
        wageLimit.setWageLimitId("006720113");
        wageLimit.setLaw(law);
        wageLimit.setAmount(new SpcfMoney("7000.00"));
        wageLimit.setEffectiveYearQuarter("20113");
        Application.getHibernateSession().saveOrUpdate(wageLimit);
        wageLimit = new WageLimit();
        wageLimit.setWageLimitId("006720114");
        wageLimit.setLaw(law);
        wageLimit.setAmount(new SpcfMoney("8000.00"));
        wageLimit.setEffectiveYearQuarter("20114");
        Application.getHibernateSession().saveOrUpdate(wageLimit);
        wageLimit = new WageLimit();
        wageLimit.setWageLimitId("006720121");
        wageLimit.setLaw(law);
        wageLimit.setAmount(new SpcfMoney("9000.00"));
        wageLimit.setEffectiveYearQuarter("20121");
        Application.getHibernateSession().saveOrUpdate(wageLimit);
        PayrollServices.commitUnitOfWork();

        //Execute ATADataExtract batch process for Quarterly Data
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.QuarterlyData + " 2011 3");
        processor.executeJob();

        String extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }
        ATFDataExtractFile wageLimitExtract = getDataExtractFileByFileType(ATFDataExtractFileType.WageLimitsInfo, extractBatch);

        PayrollServices.commitUnitOfWork();

        String extractFileName = extractFile(wageLimitExtract);
        List<String> extractLines = getLinesFromFileWithKey(extractFileName, "CA SDI_EE");

        assertEquals(extractLines.get(0), "\"WAGE_LIMIT\",\"2011\",\"\",\"CA SDI_EE\",\"7000.00\"");
        assertEquals(extractLines.get(1), "\"WAGE_LIMIT\",\"2011\",\"2\",\"CA SDI_EE\",\"6000.00\"");
        assertEquals(extractLines.get(2), "\"WAGE_LIMIT\",\"2011\",\"1\",\"CA SDI_EE\",\"5000.00\"");

         // Advance to 2012 Q1 and do it again.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20120101000000");
        PayrollServices.commitUnitOfWork();

        //Execute ATADataExtract batch process for Quarterly Data
        processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.QuarterlyData + " 2012 1");
        processor.executeJob();

        extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }
        wageLimitExtract = getDataExtractFileByFileType(ATFDataExtractFileType.WageLimitsInfo, extractBatch);

        PayrollServices.commitUnitOfWork();

        extractFileName = extractFile(wageLimitExtract);
        extractLines = getLinesFromFileWithKey(extractFileName, "CA SDI_EE");

        assertEquals(extractLines.get(0), "\"WAGE_LIMIT\",\"2012\",\"\",\"CA SDI_EE\",\"9000.00\"");
        assertEquals(extractLines.get(1), "\"WAGE_LIMIT\",\"2011\",\"4\",\"CA SDI_EE\",\"8000.00\"");
        assertEquals(extractLines.get(2), "\"WAGE_LIMIT\",\"2011\",\"3\",\"CA SDI_EE\",\"7000.00\"");
        assertEquals(extractLines.get(3), "\"WAGE_LIMIT\",\"2011\",\"2\",\"CA SDI_EE\",\"6000.00\"");
        assertEquals(extractLines.get(4), "\"WAGE_LIMIT\",\"2011\",\"1\",\"CA SDI_EE\",\"5000.00\"");


        PayrollServices.beginUnitOfWork();
        // Delete any entries for this law except current year i.e. added above
        Application.executeSqlCommand("delete from psp_wage_limit where cast(law_fk as int) = 67 and effective_year_quarter not like (select to_char(current_timestamp, 'yyyy') from dual)||'%'", false);
        PayrollServices.commitUnitOfWork();
    }


    private List<String> getLinesFromFileWithKey( String fileName, String key ) {
        List<String> results = new ArrayList<String>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            while (line != null) {
                if (line.contains( key )) {
                    results.add(line);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException io) {
            io.printStackTrace();
            TestCase.fail(io.getMessage());
        }

        return results;
    }

    private ATFDataExtractFile getDataExtractFileByFileType(ATFDataExtractFileType pFileType, ATFDataExtractBatch pBatch) {
        ATFDataExtractFile extractFile = null;
        DomainEntitySet<ATFDataExtractFile> extractFiles =
                Application.find(ATFDataExtractFile.class,
                        ATFDataExtractFile.ATFDataExtractBatch().equalTo(pBatch)
                                .And(ATFDataExtractFile.FileType().equalTo(pFileType)));


        if (extractFiles != null) {
            extractFile = extractFiles.get(0);
        }

        return extractFile;
    }

    /**
     * The PSP-10972 VT SUI filings pull REG Hourly and REG Salary to the return (excluding Sick and Vacation)
     */
    @Test
    public void testVTSUI_Salary_WorkedHours() throws Exception {
        //Create the Test Data with Hourly-REG, Hourly-VAC, Salary-REG, Salary-VAC for 130
        createTestDataForActualWorkedHours("130", "VT", PaymentMethod.CheckPayment);

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Execute ATFDataExtract batch process for Quarterly Data
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                                                                        BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                                                                        ATFDataExtractRunType.QuarterlyData + " 2012 1");

        processor.executeJob();

        String extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        junit.framework.Assert.assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            junit.framework.Assert.assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }

        ATFDataExtractFile employeeTotalsExtract = getDataExtractFileByFileType(ATFDataExtractFileType.EmployeeTotalsInfo, extractBatch);

        PayrollServices.commitUnitOfWork();

        String extractFileName = extractFile(employeeTotalsExtract);
        System.out.print(extractFileName);
        List<String> extractLines = getLinesFromFileWithKey(extractFileName, "VT SUI_ER");

        /*
        Based on data population
        First_1 M_1 Last_1 - salary-Vac not counted
        First_2 M_2 Last_2 - salary-Reg
        First_3 M_3 Last_3 - hourly-Vac not counted

        "EE_TOT","158903","First_1 M_1 Last_1","1","2012","VT SUI_ER","130.00","130.00","130.00","13.00","0.00","13","N","N","N","0.000000"
        "EE_TOT","158903","First_2 M_2 Last_2","1","2012","VT SUI_ER","130.00","130.00","130.00","13.00","44.00","13","N","N","N","0.000000"
        EE_TOT","158903","First_3 M_3 Last_3","1","2012","VT SUI_ER","130.00","130.00","130.00","13.00","0.00","13","Y","N","N","0.000000"
         */

        assertEquals("\"0.00\"", findHoursWorked(extractLines.get(0)));
        assertEquals("\"44.00\"", findHoursWorked(extractLines.get(1)));
        assertEquals("\"0.00\"", findHoursWorked(extractLines.get(2)));
    }


    /**
     * The WA SUI-ER form 5208A was not showing the Salary hours and only the Hourly hours were included.
     * In order to get the actual worked hours we need to enable the hours worked calculation by updating the metadata table HOURS_WORKED_EXCEPTION
     * Updated the meta-data for including the same.
     * @throws Exception
     */
    @Test
    public void testWASUI_Salary_Hours_Form5208A() throws Exception {

        //Create the Test Data with Hourly-REG, Hourly-VAC, Salary-REG, Salary-VAC for 131
        createTestDataForActualWorkedHours("131", "WA", PaymentMethod.CheckPayment);

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Execute ATFDataExtract batch process for Quarterly Data
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.QuarterlyData + " 2012 1");
        processor.executeJob();

        String extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        junit.framework.Assert.assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            junit.framework.Assert.assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }

        ATFDataExtractFile employeeTotalsExtract = getDataExtractFileByFileType(ATFDataExtractFileType.EmployeeTotalsInfo, extractBatch);

        PayrollServices.commitUnitOfWork();

        String extractFileName = extractFile(employeeTotalsExtract);

        List<String> extractLines = getLinesFromFileWithKey(extractFileName, "WA SUI_ER");

        //Verify the ATF extract file for EmployeeTotals has the correct number for Hours_Worked
        assertEquals("\"44.00\"", findHoursWorked(extractLines.get(0)));
        assertEquals("\"44.00\"", findHoursWorked(extractLines.get(1)));
        assertEquals("\"44.00\"", findHoursWorked(extractLines.get(2)));
    }

    /**
     * Processes the extract line and finds the value for hours_Worked.
     * 11th field in the extract line will be hours_Worked
     * @param extractLine
     * @return
     */
    private String findHoursWorked(String extractLine){
        String hours_Worked="";
        String[] splitOutput = extractLine.split(",");
        hours_Worked = splitOutput[10];
        return hours_Worked;
    }

    @Test
    public void testDCSUI_Actual_WorkedHours() throws Exception {

        //Create the Test Data with Hourly-REG, Hourly-VAC, Salary-REG, Salary-VAC for DC
        createTestDataForActualWorkedHours("90", "DC", PaymentMethod.CheckPayment);

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Execute ATFDataExtract batch process for Quarterly Data
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.QuarterlyData + " 2012 1");
        processor.executeJob();

        String extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        junit.framework.Assert.assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            junit.framework.Assert.assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }

        ATFDataExtractFile employeeTotalsExtract = getDataExtractFileByFileType(ATFDataExtractFileType.EmployeeTotalsInfo, extractBatch);

        PayrollServices.commitUnitOfWork();

        String extractFileName = extractFile(employeeTotalsExtract);

        List<String> extractLines = getLinesFromFileWithKey(extractFileName, "DC SUI_ER");

        //Verify the ATF extract file for EmployeeTotals has the correct number for Hours_Worked
        assertEquals("\"30.00\"", findHoursWorked(extractLines.get(0)));
        assertEquals("\"30.00\"", findHoursWorked(extractLines.get(1)));
        assertEquals("\"31.00\"", findHoursWorked(extractLines.get(2)));
    }

    @Test
    public void testMNSUI_Actual_WorkedHours() throws Exception {

        //Create the Test Data with Hourly-REG, Hourly-VAC, Salary-REG, Salary-VAC for DC
        createTestDataForActualWorkedHours("106", "MN", PaymentMethod.ACHCredit);

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Execute ATFDataExtract batch process for Quarterly Data
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.QuarterlyData + " 2012 1");
        processor.executeJob();

        String extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        junit.framework.Assert.assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            junit.framework.Assert.assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }

        ATFDataExtractFile employeeTotalsExtract = getDataExtractFileByFileType(ATFDataExtractFileType.EmployeeTotalsInfo, extractBatch);

        PayrollServices.commitUnitOfWork();

        String extractFileName = extractFile(employeeTotalsExtract);

        List<String> extractLines = getLinesFromFileWithKey(extractFileName, "MN SUI_ER");

        //Verify the ATF extract file for EmployeeTotals has the correct number for Hours_Worked
        assertEquals("\"35.00\"", findHoursWorked(extractLines.get(0)));
        assertEquals("\"36.00\"", findHoursWorked(extractLines.get(1)));
        assertEquals("\"36.00\"", findHoursWorked(extractLines.get(2)));
    }

    @Test
    public void testMASUI_Actual_WorkedHours() throws Exception {

        //Create the Test Data with Hourly-REG, Hourly-VAC, Salary-REG, Salary-VAC for DC
        createTestDataForActualWorkedHours("102", "MA", PaymentMethod.ACHCredit);

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Execute ATFDataExtract batch process for Quarterly Data
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.QuarterlyData + " 2012 1");
        processor.executeJob();

        String extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        junit.framework.Assert.assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            junit.framework.Assert.assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }

        ATFDataExtractFile employeeTotalsExtract = getDataExtractFileByFileType(ATFDataExtractFileType.EmployeeTotalsInfo, extractBatch);

        PayrollServices.commitUnitOfWork();

        String extractFileName = extractFile(employeeTotalsExtract);

        List<String> extractLines = getLinesFromFileWithKey(extractFileName, "MA SUI_ER");

        //Verify the ATF extract file for EmployeeTotals has the correct number for Hours_Worked
        assertEquals("\"34.00\"", findHoursWorked(extractLines.get(0)));
        assertEquals("\"34.00\"", findHoursWorked(extractLines.get(1)));
        assertEquals("\"35.00\"", findHoursWorked(extractLines.get(2)));
    }

    @Test
    public void testRISUI_Actual_WorkedHours() throws Exception {

        //Create the Test Data with Hourly-REG, Hourly-VAC, Salary-REG, Salary-VAC for DC
        createTestDataForActualWorkedHours("123", "RI", PaymentMethod.CheckPayment);

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Execute ATFDataExtract batch process for Quarterly Data
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.QuarterlyData + " 2012 1");
        processor.executeJob();

        String extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        junit.framework.Assert.assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            junit.framework.Assert.assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }

        ATFDataExtractFile employeeTotalsExtract = getDataExtractFileByFileType(ATFDataExtractFileType.EmployeeTotalsInfo, extractBatch);

        PayrollServices.commitUnitOfWork();

        String extractFileName = extractFile(employeeTotalsExtract);

        List<String> extractLines = getLinesFromFileWithKey(extractFileName, "RI SUI_ER");

        //Verify the ATF extract file for EmployeeTotals has the correct number for Hours_Worked
        assertEquals("\"41.00\"", findHoursWorked(extractLines.get(0)));
        assertEquals("\"41.00\"", findHoursWorked(extractLines.get(1)));
        assertEquals("\"42.00\"", findHoursWorked(extractLines.get(2)));
    }

    @Test
    public void testWYSUI_Actual_WorkedHours() throws Exception {

        //Create the Test Data with Hourly-REG, Hourly-VAC, Salary-REG, Salary-VAC for DC
        createTestDataForActualWorkedHours("134", "WY", PaymentMethod.ACHDebit);

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Execute ATFDataExtract batch process for Quarterly Data
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.QuarterlyData + " 2012 1");
        processor.executeJob();

        String extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        junit.framework.Assert.assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            junit.framework.Assert.assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }

        ATFDataExtractFile employeeTotalsExtract = getDataExtractFileByFileType(ATFDataExtractFileType.EmployeeTotalsInfo, extractBatch);

        PayrollServices.commitUnitOfWork();

        String extractFileName = extractFile(employeeTotalsExtract);

        List<String> extractLines = getLinesFromFileWithKey(extractFileName, "WY SUI_ER");

        //Verify the ATF extract file for EmployeeTotals has the correct number for Hours_Worked
        assertEquals("\"45.00\"", findHoursWorked(extractLines.get(0)));
        assertEquals("\"45.00\"", findHoursWorked(extractLines.get(1)));
        assertEquals("\"45.00\"", findHoursWorked(extractLines.get(2)));
    }
    /**
     * This method creates a Company with Hourly-REG, Hourly-VAC, Salary-REG, Salary-VAC PayrollItemCode, PayType combinations
     * in order to test the actual worked hours for Hourly and Salary payrollItemCodes to be reported on the SUI ER filing
     **/
    private void createTestDataForActualWorkedHours(String law_id, String law_state, PaymentMethod pPaymentMethod){

        double law_amount = Double.valueOf(law_id)/10;
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));
        String[] statesList = new String[]{law_state};
        String[] stateLawIds = new String[]{law_id};
        DataLoadServices.setupCompany(158903L, 1, statesList, PaymentTemplateCategory.SUI, pPaymentMethod);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("158903", SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        double pMultiplier = 1.0;
        int companyPayrollItemSourceId = 1;


        DataLoadServices.addEEs(company, 1);
        lawAmounts.clear();
        lawAmounts.put("61", String.valueOf(6.1 * pMultiplier));
        lawAmounts.put("62", String.valueOf(6.2 * pMultiplier));
        lawAmounts.put("63", String.valueOf(6.3 * pMultiplier));
        lawAmounts.put("64", String.valueOf(6.4 * pMultiplier));
        lawAmounts.put("1", String.valueOf(1.5 * pMultiplier));
        lawAmounts.put(law_id, String.valueOf(law_amount * pMultiplier)); // SUI-ER


        //create Company payroll Items
        List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        List<String> lawIds = new ArrayList<String>();
        CompanyLaw companyLaw;
        for (String stateLawId : stateLawIds) {
            companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
            lawIds.add(companyLaw.getSourceId());
        }
        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
        QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
        companyPayrollItemDTO.setTaxFormLine("OTHER");
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

        DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
        String hourlyReg = String.valueOf(companyPayrollItemSourceId);

        //Adding a CompanyPayrollItem for Hourly-VAC
        companyPayrollItemSourceId++;
        companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        lawIds = new ArrayList<String>();
        companyLaw = CompanyLaw.findCompanyLaw(company, law_id);
        lawIds.add(companyLaw.getSourceId());
        companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
        qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
        companyPayrollItemDTO.setTaxFormLine("OTHER");
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

        DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
        String hourlyVac = String.valueOf(companyPayrollItemSourceId);

        //Adding a CompanyPayrollItem for Salary-VAC
        companyPayrollItemSourceId++;
        companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        lawIds = new ArrayList<String>();
        companyLaw = CompanyLaw.findCompanyLaw(company, law_id);
        lawIds.add(companyLaw.getSourceId());
        companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Salary);
        qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
        companyPayrollItemDTO.setTaxFormLine("OTHER");
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

        DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
        String salaryVac = String.valueOf(companyPayrollItemSourceId);

        //Adding a CompanyPayrollItem for Salary-REG
        companyPayrollItemSourceId++;
        companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        lawIds = new ArrayList<String>();
        companyLaw = CompanyLaw.findCompanyLaw(company, law_id);
        lawIds.add(companyLaw.getSourceId());
        companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Salary);
        qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
        companyPayrollItemDTO.setTaxFormLine("OTHER");
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

        DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
        String salaryReg = String.valueOf(companyPayrollItemSourceId);

        CalculateEmployeeTotalsTestsHelper.createPayrollItems(company, PayrollItemType.Deduction);
        CalculateEmployeeTotalsTestsHelper.createPayrollItems(company, PayrollItemType.EmployerContribution);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

        int k = 1;
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
            firstPaycheckPeriodBeginDate.addDays(15);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

            for (String stateLawId : stateLawIds) {
                CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
                EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();

                employerContributionTransactionDTO.setContributionAmount(new BigDecimal(pMultiplier));
                employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(pMultiplier * 5));
                employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(pMultiplier * 50));
                employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(pMultiplier * 100));

                deductionTransactionDTO.setDeductionAmount(new BigDecimal(pMultiplier));
                deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(pMultiplier * 10));

                compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                if(stateLawId.equals(law_id)&& (k == 1)){
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(salaryVac));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(salaryVac));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(salaryVac));
                } else if ((stateLawId.equals(law_id)) && (k == 2)) {
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(salaryReg));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(salaryReg));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(salaryReg));
                } else if ((stateLawId.equals(law_id)) && (k == 3)) {
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                } else {
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                }
                QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId) * k) / 2);
                compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(pMultiplier)));
                compensationTransactionDTO.setPayStubOrder((long) k);

                paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);

                employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
                employerContributionTransactionDTO.setSourcePayrollItemId("TTT1");
                employerContributionTransactionDTO.setContributionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT1"));
                employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(pMultiplier * 5));
                employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(pMultiplier * 50));
                employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(pMultiplier * 100));

                deductionTransactionDTO = new DeductionTransactionDTO();
                deductionTransactionDTO.setDeductionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT10"));
                deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(pMultiplier * 10));
                deductionTransactionDTO.setSourcePayrollItemId("TTT10");

                paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);
            }
            k++;
        }

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_" + k, new DateDTO("2011-08-14"));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        for (Employee employee : employees) {
            QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
            qbdtPayrollTransactionDTO.setAmount(new SpcfMoney("27.27"));
            qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
            qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 2));
            QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
            qTlDTO.setAmount(new SpcfMoney("27.27"));
            qTlDTO.setPayrollItemId(String.valueOf(companyPayrollItemSourceId));
            qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
            PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
            for (String stateLawId : stateLawIds) {
                SpcfMoney amount = new SpcfMoney(stateLawId);
                liabilityAdjustmentDTOs.add(DataLoadServices.createLiabilityAdjustmentDTO(stateLawId, null, employee.getSourceEmployeeId(), null, amount, (SpcfMoney) amount.multiply(new SpcfMoney("2")), (SpcfMoney) amount.divide(new SpcfMoney("2")), false));

            }
        }

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
    }

    /** Give Date with month of 30days & beginning on Sunday */
    private void runTestPayroll(SpcfCalendar date) {
        int y=date.getYear(), m=date.getMonth(), d=date.getDay();
        String[] states = {"GA", "IL"};
        List<Company> companies = DataLoadServices.setupCompany(date.getTimeInMilliseconds(), 1, states, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);
        DataLoadServices.setPSPDate(date);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("61","6.1");
        lawAmounts.put("62","6.2");
        lawAmounts.put("63","6.3");
        lawAmounts.put("64","6.4");
        lawAmounts.put("66","6.6");
        lawAmounts.put("1","10");

        DataLoadServices.setPSPDate(y, m, d+1);
        for (Company company : companies) {
            DataLoadServices.enrollEFTPS(company);
            DataLoadServices.runPayrollRun(company, states, lawAmounts, new DateDTO(date));
        }

        DataLoadServices.setPSPDate(y, m, d+2);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(y, m, d+9);
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        DataLoadServices.setPSPDate(y, m+1, d+10);
        
        SpcfCalendar dueDate = PSPDate.getPSPTime().copy();
        CalendarUtils.addBusinessDays(dueDate, 2);
        
        SpcfCalendar initDate = dueDate.copy();
        CalendarUtils.addBusinessDays(initDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(initDate);

    }

    @Test
    /** Test company payments by extracting them from past 6 Quarters */
    public void testCoPayWithPrevXQuartersData() throws Exception {

        //Execute ATADataExtract with Updated Data for 2012
        runTestPayroll(SpcfCalendar.createInstance(2012, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(2013, 2, 25);
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.UpdatedData.toString());
        processor.executeJob();
        TestCreateFilingsSpecificTransactions.validateFile("testCoPayPrev6Q_2012", processor.getExtractBatchId(), ATFDataExtractFileType.CompanyPaymentsInfo);

        //Execute ATADataExtract with Updated Data for 2015
        runTestPayroll(SpcfCalendar.createInstance(2015, 11, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(2016, 8, 10);
        processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.UpdatedData.toString());
        processor.executeJob();
        TestCreateFilingsSpecificTransactions.validateFile("testCoPayPrev6Q_2015", processor.getExtractBatchId(), ATFDataExtractFileType.CompanyPaymentsInfo);
    }

    @Ignore
    @Test
    // TODO : Remove this method once verified
    public void testUploadFilesToTFS() throws Exception {

        String server = BatchUtils.getConfigString("psp_tfs_ftp_host");
        String user = BatchUtils.getConfigString("psp_tfs_ftp_username");
        String privateKey = BatchUtils.getConfigString("psp_tfs_ftp_private_key");
        String destDir = BatchUtils.getConfigString("psp_tfs_ftp_destdir");
        int timeout = Integer.parseInt(BatchUtils.getConfigString("psp_tfs_ftp_connection_timeout", "10000"));

        com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter sftp = BatchUtils.getTFSExtractSftpConnection(new JSchAdapter(), server, user, privateKey, timeout);
        sftp.connect();
        sftp.changeRemoteDir(destDir);
        sftp.uploadFile(Application.findFileOnClassPath("atfextract/expected/testCoPayPrev6Q_2015"));
    }

}
