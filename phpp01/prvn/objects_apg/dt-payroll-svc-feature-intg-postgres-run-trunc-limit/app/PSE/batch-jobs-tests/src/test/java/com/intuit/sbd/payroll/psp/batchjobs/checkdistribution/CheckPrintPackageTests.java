package com.intuit.sbd.payroll.psp.batchjobs.checkdistribution;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintAddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckDDINfo;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckEarningLineDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckLineDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.lowagie.text.FontFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: vrepka
 * Date: Jan 19, 2010
 * Time: 9:33:09 PM
 */
@SuppressWarnings("deprecation")
public class CheckPrintPackageTests {
    private DateDTO CHECK_DATE = new DateDTO(2010, 3, 1);
    CheckPrintDTO m_printDTOObj;

    @Before
    public void setUp() {
        FontFactory.register(Application.findFileOnClassPath("checkdistribution/IDAutomationSMICR_for_testing_only.ttf"), "IDAutomationMICR");
        m_printDTOObj = new CheckPrintDTO();
    }

    @After
    public void tearDown() {
        m_printDTOObj = null;
    }

    @Test
    public void testCheckPrintPdfCoverPage() {
        try {
            byte[] pdfFile;
            CheckPrintDTO lCheckPrintDTO = createCoverPageDTO(new CheckPrintDTO());
            pdfFile = getBytesFromFile(new File(Application.findFileOnClassPath("checkdistribution/test_coverpage.pdf")));
            byte[] pdf = CheckPrintPackage.generateCoverPage(lCheckPrintDTO, new Date("02/27/2010")); // normal date would be Calendar.getInstance().getTime()
            assertTrue("pdf is empty", pdf.length > 0);
            assertEquals("pdfs do not match", pdfFile.length, pdf.length);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testCheckPrintPdfTestCoverPage() {
        try {
            byte[] pdfFile;
            CheckPrintDTO lCheckPrintDTO = createTestCoverPage(new CheckPrintDTO());
            pdfFile = getBytesFromFile(new File(Application.findFileOnClassPath("checkdistribution/test_testcoverpage.pdf")));
            byte[] pdf = CheckPrintPackage.generateCoverPage(lCheckPrintDTO, new Date("02/27/2010")); // normal date would be Calendar.getInstance().getTime()
            assertTrue("pdf is empty", pdf.length > 0);
            assertEquals("pdfs do not match", pdfFile.length, pdf.length);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testCheckPrintDeductionsTaxesEarningsOnly() {
        try {
            CheckPrintDTO lCheckPrintDTO = testCreateCheckPrintDeductionsTaxesEarnings(new CheckPrintDTO());
            byte[] pdfFile = getBytesFromFile(new File(Application.findFileOnClassPath("checkdistribution/test_PaycheckDeductionTaxesEarningssOnly.pdf")));
            byte[] pdf = CheckPrintPackage.generatePaychecks(lCheckPrintDTO);
            assertTrue("pdf is empty", pdf.length > 0);
            assertEquals("pdfs do not match", pdfFile.length, pdf.length);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testCheckPrintTaxesEarningsOnly() {
        try {
            CheckPrintDTO lCheckPrintDTO = testCreateCheckPrintTaxesEarnings(new CheckPrintDTO());
            byte[] pdfFile = getBytesFromFile(new File(Application.findFileOnClassPath("checkdistribution/test_PaycheckTaxesEarningssOnly.pdf")));
            byte[] pdf = CheckPrintPackage.generatePaychecks(lCheckPrintDTO);
            assertTrue("pdf is empty", pdf.length > 0);
            assertEquals("pdfs do not match", pdfFile.length, pdf.length);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testCheckPrintDeductionsOnly() {
        try {
            CheckPrintDTO lCheckPrintDTO = testCreateCheckPrintDeductionsOnly(new CheckPrintDTO());
            byte[] pdfFile = getBytesFromFile(new File(Application.findFileOnClassPath("checkdistribution/test_PaycheckDeductionsOnly.pdf")));
            byte[] pdf = CheckPrintPackage.generatePaychecks(lCheckPrintDTO);
            assertTrue("pdf is empty", pdf.length > 0);
            assertEquals("pdfs do not match", pdfFile.length, pdf.length);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    @Test
    public void testCheckPrintAll() {
        try {
            CheckPrintDTO lCheckPrintDTO = testCreateCheckPrintAll(new CheckPrintDTO());
            byte[] pdfFile = getBytesFromFile(new File(Application.findFileOnClassPath("checkdistribution/test_PaycheckAll.pdf")));
            byte[] pdf = CheckPrintPackage.generatePaychecks(lCheckPrintDTO);
            assertTrue("pdf is empty", pdf.length > 0);
            assertEquals("pdfs do not match", pdfFile.length, pdf.length);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testCheckPrintMultipleChecks() {
        try {
            CheckPrintDTO lCheckPrintDTO = testCreateMultipleChecks(new CheckPrintDTO());
            byte[] pdfFile = getBytesFromFile(new File(Application.findFileOnClassPath("checkdistribution/test_MultiplePaychecks.pdf")));
            byte[] pdf = CheckPrintPackage.generatePaychecks(lCheckPrintDTO);
            assertTrue("pdf is empty", pdf.length > 0);
            assertEquals("pdfs do not match", pdfFile.length, pdf.length);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testTooManyItems() {
        try {
            CheckPrintDTO lCheckPrintDTO = testTooManyItems(new CheckPrintDTO());
            byte[] pdfFile = getBytesFromFile(new File(Application.findFileOnClassPath("checkdistribution/test_TooManyItems.pdf")));
            byte[] pdf = CheckPrintPackage.generatePaychecks(lCheckPrintDTO);
            assertTrue("pdf is empty", pdf.length > 0);
            assertEquals("pdfs do not match", pdfFile.length, pdf.length);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private CheckPrintDTO testCreateCheckPrintAll(CheckPrintDTO checkPrintDTO) {
        checkPrintDTO.setCompanyId("123456789");

        CheckPrintAddressDTO companylegalAddress = new CheckPrintAddressDTO();
        //companylegalAddress.setAddressLine1("QA TEST CLIENT SD 2 ");
        companylegalAddress.setAddressLine1("234 QA TEST LANE");
        companylegalAddress.setAddressLine2("234 QA TEST LANE");
        companylegalAddress.setCity("FORT WORTH");
        companylegalAddress.setState("TEXAS");
        companylegalAddress.setZipCode("76112");
        companylegalAddress.setCountry("USA");
        checkPrintDTO.setCompanyLegalAddress(companylegalAddress);
        checkPrintDTO.setCompanyLegalName("QA TEST CLIENT SD 2");
        // test micr font does not have a 5
        checkPrintDTO.setCompanyBankAccountNumber("123466");
        checkPrintDTO.setCompanyBankRoutingNumber("664321");

        RandomAccessFile rf;
        try {
            rf = new RandomAccessFile(Application.findFileOnClassPath("checkdistribution/signature.png"), "r");

            int size = (int) rf.length();
            byte imgData[] = new byte[size];
            rf.readFully(imgData);
            rf.close();
            checkPrintDTO.setCheckSignature(imgData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        List<CheckPrintPaycheckDTO> paychecks = new ArrayList<CheckPrintPaycheckDTO>();
        CheckPrintPaycheckDTO lPayCheckDTO = new CheckPrintPaycheckDTO();
        lPayCheckDTO.setCheckDate(CHECK_DATE);
        lPayCheckDTO.setPeriodStartDate(CHECK_DATE);
        lPayCheckDTO.setPeriodEndDate(CHECK_DATE);

        lPayCheckDTO.setCheckType(CheckPrintPaycheckDTO.PaycheckType.DirectDeposit);
        //lPayCheckDTO.setCheckNetPay(new BigDecimal("2223.01"));
        // test micr font does not have a 5
        lPayCheckDTO.setCheckNumber("6461");

        lPayCheckDTO.setEmployeeId("1500");
        lPayCheckDTO.setEmployeePrintName("CINDI C. RELEASE MACHINE");
        lPayCheckDTO.setEmployeeWorkState("TX");
        lPayCheckDTO.setEmployeeStateFilingStatus("S");
        lPayCheckDTO.setEmployeePaySchedule("SEMIMONTHLY");
        lPayCheckDTO.setEmployeeFedAllowances(1);
        lPayCheckDTO.setEmployeeStateAllowances(1);
        lPayCheckDTO.setEmployeeFedFilingStatus("S");


        CheckPrintAddressDTO employeeAddress = new CheckPrintAddressDTO();
        employeeAddress.setAddressLine1("232 VISTA WAY");
        employeeAddress.setAddressLine2("232 VISTA WAY");
        employeeAddress.setCity("Reno");
        employeeAddress.setState("NV");
        employeeAddress.setZipCode("89504");
        employeeAddress.setCountry("USA");

        lPayCheckDTO.setEmployeeAddress(employeeAddress);

        //deductions start
        Set<CheckPrintPaycheckLineDTO> deductions = new TreeSet<CheckPrintPaycheckLineDTO>();
        CheckPrintPaycheckLineDTO payCheckLineDTO = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO.setPaylineAmount(new BigDecimal("-250.02"));
        payCheckLineDTO.setPaylineDescription("MARDI GRAS1");
        payCheckLineDTO.setYtdAmount(new BigDecimal("-500.04"));
        deductions.add(payCheckLineDTO);

        CheckPrintPaycheckLineDTO payCheckLineDTO1 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO1.setPaylineAmount(new BigDecimal("-150.01"));
        payCheckLineDTO1.setPaylineDescription("MARDI GRAS2");
        payCheckLineDTO1.setYtdAmount(new BigDecimal("-300.02"));
        deductions.add(payCheckLineDTO1);
        //deductions end

        //taxes start
        Set<CheckPrintPaycheckLineDTO> taxes = new TreeSet<CheckPrintPaycheckLineDTO>();
        payCheckLineDTO = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO.setPaylineAmount(new BigDecimal("129.69"));
        payCheckLineDTO.setPaylineDescription("FEDERAL WITHHOLDING1");
        payCheckLineDTO.setYtdAmount(new BigDecimal("259.38"));
        taxes.add(payCheckLineDTO);

        CheckPrintPaycheckLineDTO payCheckLine1 = new CheckPrintPaycheckLineDTO();
        payCheckLine1.setPaylineAmount(new BigDecimal("80.08"));
        payCheckLine1.setPaylineDescription("SOCIAL SECURITY1");
        payCheckLine1.setYtdAmount(new BigDecimal("160.16"));
        taxes.add(payCheckLine1);

        CheckPrintPaycheckLineDTO payCheckLineDTO2 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO2.setPaylineAmount(new BigDecimal("18.73"));
        payCheckLineDTO2.setPaylineDescription("MEDICARE1");
        payCheckLineDTO2.setYtdAmount(new BigDecimal("37.46"));
        taxes.add(payCheckLineDTO2);

        CheckPrintPaycheckLineDTO payCheckLineDTO3 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO3.setPaylineAmount(new BigDecimal("-18.13"));
        payCheckLineDTO3.setPaylineDescription("EARNED INCOME CREDIT1");
        payCheckLineDTO3.setYtdAmount(new BigDecimal("-36.26"));
        taxes.add(payCheckLineDTO3);
        //taxes end

        //pre tax deductions start
        Set<CheckPrintPaycheckLineDTO> preTaxDeductions = new TreeSet<CheckPrintPaycheckLineDTO>();
        payCheckLineDTO = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO.setPaylineAmount(new BigDecimal("-6.20"));
        payCheckLineDTO.setPaylineDescription("Pretax1");
        payCheckLineDTO.setYtdAmount(new BigDecimal("-12.40"));
        preTaxDeductions.add(payCheckLineDTO);

        CheckPrintPaycheckLineDTO payCheckLineDTO11 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO11.setPaylineAmount(new BigDecimal("-6.2"));
        payCheckLineDTO11.setPaylineDescription("Pretax2");
        payCheckLineDTO11.setYtdAmount(new BigDecimal("-12.4"));
        preTaxDeductions.add(payCheckLineDTO11);

        CheckPrintPaycheckLineDTO payCheckLineDTO21 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO21.setPaylineAmount(new BigDecimal("-6.2"));
        payCheckLineDTO21.setPaylineDescription("Pretax3");
        payCheckLineDTO21.setYtdAmount(new BigDecimal("-12.4"));
        preTaxDeductions.add(payCheckLineDTO21);
        //pre tax deductions end

        //earnings start
        Set<CheckPrintPaycheckEarningLineDTO> earnings = new TreeSet<CheckPrintPaycheckEarningLineDTO>();
        CheckPrintPaycheckEarningLineDTO payCheckEarningLineDTO = new CheckPrintPaycheckEarningLineDTO();
        payCheckEarningLineDTO.setHours(new BigDecimal("100"));
        payCheckEarningLineDTO.setRate(new BigDecimal("0.31257"));
        payCheckEarningLineDTO.setRateType("r");
        payCheckEarningLineDTO.setPaylineDescription("earningsDesc");
        payCheckEarningLineDTO.setPaylineAmount(new BigDecimal("100.00"));
        payCheckEarningLineDTO.setYtdAmount(new BigDecimal("200.00"));
        earnings.add(payCheckEarningLineDTO);

        CheckPrintPaycheckEarningLineDTO payCheckEarningLineDTO1 = new CheckPrintPaycheckEarningLineDTO();
        payCheckEarningLineDTO1.setHours(new BigDecimal("100"));
        payCheckEarningLineDTO1.setRate(new BigDecimal("0.31"));
        payCheckEarningLineDTO1.setRateType("r");
        payCheckEarningLineDTO1.setPaylineDescription("earningsDesc");
        payCheckEarningLineDTO1.setPaylineAmount(new BigDecimal("100.00"));
        payCheckEarningLineDTO1.setYtdAmount(new BigDecimal("200.00"));
        earnings.add(payCheckEarningLineDTO1);

        CheckPrintPaycheckEarningLineDTO payCheckEarningLineDTO2 = new CheckPrintPaycheckEarningLineDTO();
        payCheckEarningLineDTO2.setHours(new BigDecimal("101"));
        payCheckEarningLineDTO2.setRate(new BigDecimal("61"));
        payCheckEarningLineDTO2.setPaylineDescription("earningsDesc1");
        payCheckEarningLineDTO2.setPaylineAmount(new BigDecimal("101.00"));
        payCheckEarningLineDTO2.setYtdAmount(new BigDecimal("202.00"));
        earnings.add(payCheckEarningLineDTO2);
        //earnings end

        //ddeposit start
        List<CheckPrintPaycheckDDINfo> dDInfo = new ArrayList<CheckPrintPaycheckDDINfo>();
        CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo = new CheckPrintPaycheckDDINfo();
        checkPrintPaycheckDDINfo.setAccountId("actID");
        checkPrintPaycheckDDINfo.setAccountType("actName");
        checkPrintPaycheckDDINfo.setDDAmount(new BigDecimal("500.05"));
        dDInfo.add(checkPrintPaycheckDDINfo);

        CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo2 = new CheckPrintPaycheckDDINfo();
        checkPrintPaycheckDDINfo2.setAccountId("actID2");
        checkPrintPaycheckDDINfo2.setAccountType("actName2");
        checkPrintPaycheckDDINfo2.setDDAmount(new BigDecimal("555.55"));
        dDInfo.add(checkPrintPaycheckDDINfo2);

        //ddeposit end


        lPayCheckDTO.setDeductions(deductions);
        lPayCheckDTO.setTaxes(taxes);
        lPayCheckDTO.setPreTaxDeductions(preTaxDeductions);
        lPayCheckDTO.setEarnings(earnings);
        lPayCheckDTO.setDirectDeposits(dDInfo);

        paychecks.add(lPayCheckDTO);
        checkPrintDTO.setPaychecks(paychecks);

        return checkPrintDTO;
    }

    private CheckPrintDTO testCreateCheckPrintDeductionsTaxesEarnings(CheckPrintDTO checkPrintDTO) {
        checkPrintDTO.setCompanyId("123456789");

        CheckPrintAddressDTO companylegalAddress = new CheckPrintAddressDTO();
        //companylegalAddress.setAddressLine1("QA TEST CLIENT SD 2 ");
        companylegalAddress.setAddressLine1("234 QA TEST LANE");
        companylegalAddress.setCity("FORT WORTH");
        companylegalAddress.setState("TEXAS");
        companylegalAddress.setZipCode("76112");
        companylegalAddress.setCountry("USA");
        checkPrintDTO.setCompanyLegalAddress(companylegalAddress);
        checkPrintDTO.setCompanyLegalName("QA TEST CLIENT SD 2");
        // test micr font does not have a 5
        checkPrintDTO.setCompanyBankAccountNumber("123466");
        checkPrintDTO.setCompanyBankRoutingNumber("664321");

        RandomAccessFile rf;
        try {
            rf = new RandomAccessFile(Application.findFileOnClassPath("checkdistribution/signature.png"), "r");

            int size = (int) rf.length();
            byte imgData[] = new byte[size];
            rf.readFully(imgData);
            rf.close();
            checkPrintDTO.setCheckSignature(imgData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        List<CheckPrintPaycheckDTO> paychecks = new ArrayList<CheckPrintPaycheckDTO>();
        CheckPrintPaycheckDTO lPayCheckDTO = new CheckPrintPaycheckDTO();
        lPayCheckDTO.setCheckDate(CHECK_DATE);
        lPayCheckDTO.setPeriodStartDate(CHECK_DATE);
        lPayCheckDTO.setPeriodEndDate(CHECK_DATE);

        lPayCheckDTO.setCheckType(CheckPrintPaycheckDTO.PaycheckType.DirectDeposit);

        //lPayCheckDTO.setCheckNetPay(new BigDecimal("2223.01"));
        // test micr font does not have a 5
        lPayCheckDTO.setCheckNumber("6461");


        lPayCheckDTO.setEmployeeId("1500");
        lPayCheckDTO.setEmployeePrintName("CINDI C. RELEASE MACHINE");
        lPayCheckDTO.setEmployeeWorkState("TX");
        lPayCheckDTO.setEmployeeStateFilingStatus("S");
        lPayCheckDTO.setEmployeePaySchedule("SEMIMONTHLY");
        lPayCheckDTO.setEmployeeFedAllowances(1);
        lPayCheckDTO.setEmployeeStateAllowances(1);
        lPayCheckDTO.setEmployeeFedFilingStatus("S");


        CheckPrintAddressDTO employeeAddress = new CheckPrintAddressDTO();
        employeeAddress.setAddressLine1("232 VISTA WAY");
        //companylegalAddress.setAddressLine2("emp Adrs Ln1");
        //companylegalAddress.setAddressLine3("emp Adrs Ln1");
        employeeAddress.setCity("Reno");
        employeeAddress.setState("NV");
        employeeAddress.setZipCode("89504");
        employeeAddress.setCountry("USA");

        lPayCheckDTO.setEmployeeAddress(employeeAddress);

        //deductions start
        Set<CheckPrintPaycheckLineDTO> deductions = new TreeSet<CheckPrintPaycheckLineDTO>();
        CheckPrintPaycheckLineDTO payCheckLineDTO = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO.setPaylineAmount(new BigDecimal("-250.02"));
        payCheckLineDTO.setPaylineDescription("MARDI GRAS1");
        payCheckLineDTO.setYtdAmount(new BigDecimal("-500.04"));
        deductions.add(payCheckLineDTO);

        CheckPrintPaycheckLineDTO payCheckLineDTO1 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO1.setPaylineAmount(new BigDecimal("-150.01"));
        payCheckLineDTO1.setPaylineDescription("MARDI GRAS2");
        payCheckLineDTO1.setYtdAmount(new BigDecimal("-300.02"));
        deductions.add(payCheckLineDTO1);
        //deductions end

        //taxes start
        Set<CheckPrintPaycheckLineDTO> taxes = new TreeSet<CheckPrintPaycheckLineDTO>();
        payCheckLineDTO = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO.setPaylineAmount(new BigDecimal("129.69"));
        payCheckLineDTO.setPaylineDescription("FEDERAL WITHHOLDING1");
        payCheckLineDTO.setYtdAmount(new BigDecimal("259.38"));
        taxes.add(payCheckLineDTO);

        CheckPrintPaycheckLineDTO payCheckLine1 = new CheckPrintPaycheckLineDTO();
        payCheckLine1.setPaylineAmount(new BigDecimal("80.08"));
        payCheckLine1.setPaylineDescription("SOCIAL SECURITY1");
        payCheckLine1.setYtdAmount(new BigDecimal("160.16"));
        taxes.add(payCheckLine1);

        CheckPrintPaycheckLineDTO payCheckLineDTO2 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO2.setPaylineAmount(new BigDecimal("18.73"));
        payCheckLineDTO2.setPaylineDescription("MEDICARE1");
        payCheckLineDTO2.setYtdAmount(new BigDecimal("37.46"));
        taxes.add(payCheckLineDTO2);

        CheckPrintPaycheckLineDTO payCheckLineDTO3 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO3.setPaylineAmount(new BigDecimal("-18.13"));
        payCheckLineDTO3.setPaylineDescription("EARNED INCOME CREDIT1");
        payCheckLineDTO3.setYtdAmount(new BigDecimal("-36.26"));
        taxes.add(payCheckLineDTO3);
        //taxes end

        //earnings start
        Set<CheckPrintPaycheckEarningLineDTO> earnings = new TreeSet<CheckPrintPaycheckEarningLineDTO>();
        CheckPrintPaycheckEarningLineDTO payCheckEarningLineDTO = new CheckPrintPaycheckEarningLineDTO();
        payCheckEarningLineDTO.setHours(new BigDecimal("100"));
        payCheckEarningLineDTO.setRate(new BigDecimal("60"));
        payCheckEarningLineDTO.setPaylineDescription("earningsDesc");
        payCheckEarningLineDTO.setPaylineAmount(new BigDecimal("100.00"));
        payCheckEarningLineDTO.setYtdAmount(new BigDecimal("200.00"));
        earnings.add(payCheckEarningLineDTO);

        CheckPrintPaycheckEarningLineDTO payCheckEarningLineDTO1 = new CheckPrintPaycheckEarningLineDTO();
        payCheckEarningLineDTO1.setHours(new BigDecimal("101"));
        payCheckEarningLineDTO1.setRate(new BigDecimal("61"));
        payCheckEarningLineDTO1.setPaylineDescription("earningsDesc1");
        payCheckEarningLineDTO1.setPaylineAmount(new BigDecimal("101.00"));
        payCheckEarningLineDTO1.setYtdAmount(new BigDecimal("202.00"));
        earnings.add(payCheckEarningLineDTO1);
        //earnings end

        //ddeposit start
        List<CheckPrintPaycheckDDINfo> dDInfo = new ArrayList<CheckPrintPaycheckDDINfo>();
        CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo = new CheckPrintPaycheckDDINfo();
        checkPrintPaycheckDDINfo.setAccountId("actID");
        checkPrintPaycheckDDINfo.setAccountType("actName");
        checkPrintPaycheckDDINfo.setDDAmount(new BigDecimal("500.05"));
        dDInfo.add(checkPrintPaycheckDDINfo);

        CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo2 = new CheckPrintPaycheckDDINfo();
        checkPrintPaycheckDDINfo2.setAccountId("actID2");
        checkPrintPaycheckDDINfo2.setAccountType("actName2");
        checkPrintPaycheckDDINfo2.setDDAmount(new BigDecimal("555.55"));
        dDInfo.add(checkPrintPaycheckDDINfo2);

        //ddeposit end


        lPayCheckDTO.setDeductions(deductions);
        lPayCheckDTO.setTaxes(taxes);
        //lPayCheckDTO.setPreTaxDeductions(preTaxDeductions);
        lPayCheckDTO.setEarnings(earnings);
        lPayCheckDTO.setDirectDeposits(dDInfo);

        paychecks.add(lPayCheckDTO);
        checkPrintDTO.setPaychecks(paychecks);

        return checkPrintDTO;
    }

    private CheckPrintDTO testCreateCheckPrintTaxesEarnings(CheckPrintDTO checkPrintDTO) {
        checkPrintDTO.setCompanyId("123456789");

        CheckPrintAddressDTO companylegalAddress = new CheckPrintAddressDTO();
        //companylegalAddress.setAddressLine1("QA TEST CLIENT SD 2 ");
        companylegalAddress.setAddressLine1("234 QA TEST LANE");
        companylegalAddress.setCity("FORT WORTH");
        companylegalAddress.setState("TEXAS");
        companylegalAddress.setZipCode("76112");
        companylegalAddress.setCountry("USA");
        checkPrintDTO.setCompanyLegalAddress(companylegalAddress);
        checkPrintDTO.setCompanyLegalName("QA TEST CLIENT SD 2");
        // test micr font does not have a 5
        checkPrintDTO.setCompanyBankAccountNumber("123466");
        checkPrintDTO.setCompanyBankRoutingNumber("664321");

        RandomAccessFile rf;
        try {
            rf = new RandomAccessFile(Application.findFileOnClassPath("checkdistribution/signature.png"), "r");

            int size = (int) rf.length();
            byte imgData[] = new byte[size];
            rf.readFully(imgData);
            rf.close();
            checkPrintDTO.setCheckSignature(imgData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        List<CheckPrintPaycheckDTO> paychecks = new ArrayList<CheckPrintPaycheckDTO>();
        CheckPrintPaycheckDTO lPayCheckDTO = new CheckPrintPaycheckDTO();
        lPayCheckDTO.setCheckDate(CHECK_DATE);
        lPayCheckDTO.setPeriodStartDate(CHECK_DATE);
        lPayCheckDTO.setPeriodEndDate(CHECK_DATE);

        lPayCheckDTO.setCheckType(CheckPrintPaycheckDTO.PaycheckType.DirectDeposit);

        //lPayCheckDTO.setCheckNetPay(new BigDecimal("2223.01"));
        // test micr font does not have a 5
        lPayCheckDTO.setCheckNumber("6461");


        lPayCheckDTO.setEmployeeId("1500");
        lPayCheckDTO.setEmployeePrintName("CINDI C. RELEASE MACHINE");
        lPayCheckDTO.setEmployeeWorkState("TX");
        lPayCheckDTO.setEmployeeStateFilingStatus("S");
        lPayCheckDTO.setEmployeePaySchedule("SEMIMONTHLY");
        lPayCheckDTO.setEmployeeFedAllowances(1);
        lPayCheckDTO.setEmployeeStateAllowances(1);
        lPayCheckDTO.setEmployeeFedFilingStatus("S");


        CheckPrintAddressDTO employeeAddress = new CheckPrintAddressDTO();
        employeeAddress.setAddressLine1("232 VISTA WAY");
        //companylegalAddress.setAddressLine2("emp Adrs Ln1");
        //companylegalAddress.setAddressLine3("emp Adrs Ln1");
        employeeAddress.setCity("Reno");
        employeeAddress.setState("NV");
        employeeAddress.setZipCode("89504");
        employeeAddress.setCountry("USA");

        lPayCheckDTO.setEmployeeAddress(employeeAddress);

        //taxes start
        Set<CheckPrintPaycheckLineDTO> taxes = new TreeSet<CheckPrintPaycheckLineDTO>();
        CheckPrintPaycheckLineDTO payCheckLineDTO = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO.setPaylineAmount(new BigDecimal("129.69"));
        payCheckLineDTO.setPaylineDescription("FEDERAL WITHHOLDING1");
        payCheckLineDTO.setYtdAmount(new BigDecimal("259.38"));
        taxes.add(payCheckLineDTO);

        CheckPrintPaycheckLineDTO payCheckLine1 = new CheckPrintPaycheckLineDTO();
        payCheckLine1.setPaylineAmount(new BigDecimal("80.08"));
        payCheckLine1.setPaylineDescription("SOCIAL SECURITY1");
        payCheckLine1.setYtdAmount(new BigDecimal("160.16"));
        taxes.add(payCheckLine1);

        CheckPrintPaycheckLineDTO payCheckLineDTO2 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO2.setPaylineAmount(new BigDecimal("18.73"));
        payCheckLineDTO2.setPaylineDescription("MEDICARE1");
        payCheckLineDTO2.setYtdAmount(new BigDecimal("37.46"));
        taxes.add(payCheckLineDTO2);

        CheckPrintPaycheckLineDTO payCheckLineDTO3 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO3.setPaylineAmount(new BigDecimal("-18.13"));
        payCheckLineDTO3.setPaylineDescription("EARNED INCOME CREDIT1");
        payCheckLineDTO3.setYtdAmount(new BigDecimal("-36.26"));
        taxes.add(payCheckLineDTO3);
        //taxes end

        //earnings start
        Set<CheckPrintPaycheckEarningLineDTO> earnings = new TreeSet<CheckPrintPaycheckEarningLineDTO>();
        CheckPrintPaycheckEarningLineDTO payCheckEarningLineDTO = new CheckPrintPaycheckEarningLineDTO();
        payCheckEarningLineDTO.setHours(new BigDecimal("100"));
        payCheckEarningLineDTO.setRate(new BigDecimal("60"));
        payCheckEarningLineDTO.setPaylineDescription("earningsDesc");
        payCheckEarningLineDTO.setPaylineAmount(new BigDecimal("100.00"));
        payCheckEarningLineDTO.setYtdAmount(new BigDecimal("200.00"));
        earnings.add(payCheckEarningLineDTO);

        CheckPrintPaycheckEarningLineDTO payCheckEarningLineDTO1 = new CheckPrintPaycheckEarningLineDTO();
        payCheckEarningLineDTO1.setHours(new BigDecimal("101"));
        payCheckEarningLineDTO1.setRate(new BigDecimal("61"));
        payCheckEarningLineDTO1.setPaylineDescription("earningsDesc1");
        payCheckEarningLineDTO1.setPaylineAmount(new BigDecimal("101.00"));
        payCheckEarningLineDTO1.setYtdAmount(new BigDecimal("202.00"));
        earnings.add(payCheckEarningLineDTO1);
        //earnings end

        //ddeposit start
        List<CheckPrintPaycheckDDINfo> dDInfo = new ArrayList<CheckPrintPaycheckDDINfo>();
        CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo = new CheckPrintPaycheckDDINfo();
        checkPrintPaycheckDDINfo.setAccountId("actID");
        checkPrintPaycheckDDINfo.setAccountType("actName");
        checkPrintPaycheckDDINfo.setDDAmount(new BigDecimal("500.05"));
        dDInfo.add(checkPrintPaycheckDDINfo);

        CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo2 = new CheckPrintPaycheckDDINfo();
        checkPrintPaycheckDDINfo2.setAccountId("actID2");
        checkPrintPaycheckDDINfo2.setAccountType("actName2");
        checkPrintPaycheckDDINfo2.setDDAmount(new BigDecimal("555.55"));
        dDInfo.add(checkPrintPaycheckDDINfo2);

        //ddeposit end


        //lPayCheckDTO.setDeductions(deductions);
        lPayCheckDTO.setTaxes(taxes);
        //lPayCheckDTO.setPreTaxDeductions(preTaxDeductions);
        lPayCheckDTO.setEarnings(earnings);
        lPayCheckDTO.setDirectDeposits(dDInfo);

        paychecks.add(lPayCheckDTO);
        checkPrintDTO.setPaychecks(paychecks);

        return checkPrintDTO;
    }

    private CheckPrintDTO testCreateCheckPrintDeductionsOnly(CheckPrintDTO checkPrintDTO) {
        checkPrintDTO.setCompanyId("123456789");

        CheckPrintAddressDTO companylegalAddress = new CheckPrintAddressDTO();
        //companylegalAddress.setAddressLine1("QA TEST CLIENT SD 2 ");
        companylegalAddress.setAddressLine1("234 QA TEST LANE");
        companylegalAddress.setCity("FORT WORTH");
        companylegalAddress.setState("TEXAS");
        companylegalAddress.setZipCode("76112");
        companylegalAddress.setCountry("USA");
        checkPrintDTO.setCompanyLegalAddress(companylegalAddress);
        checkPrintDTO.setCompanyLegalName("QA TEST CLIENT SD 2");
        // test micr font does not have a 5
        checkPrintDTO.setCompanyBankAccountNumber("123466");
        checkPrintDTO.setCompanyBankRoutingNumber("664321");

        RandomAccessFile rf;
        try {
            rf = new RandomAccessFile(Application.findFileOnClassPath("checkdistribution/signature.png"), "r");

            int size = (int) rf.length();
            byte imgData[] = new byte[size];
            rf.readFully(imgData);
            rf.close();
            checkPrintDTO.setCheckSignature(imgData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        List<CheckPrintPaycheckDTO> paychecks = new ArrayList<CheckPrintPaycheckDTO>();
        CheckPrintPaycheckDTO lPayCheckDTO = new CheckPrintPaycheckDTO();
        lPayCheckDTO.setCheckDate(CHECK_DATE);
        lPayCheckDTO.setPeriodStartDate(CHECK_DATE);
        lPayCheckDTO.setPeriodEndDate(CHECK_DATE);

        lPayCheckDTO.setCheckType(CheckPrintPaycheckDTO.PaycheckType.DirectDeposit);

        //lPayCheckDTO.setCheckNetPay(new BigDecimal("2223.01"));
        // test micr font does not have a 5
        lPayCheckDTO.setCheckNumber("6461");


        lPayCheckDTO.setEmployeeId("1500");
        lPayCheckDTO.setEmployeePrintName("CINDI C. RELEASE MACHINE");
        lPayCheckDTO.setEmployeeWorkState("TX");
        lPayCheckDTO.setEmployeeStateFilingStatus("S");
        lPayCheckDTO.setEmployeePaySchedule("SEMIMONTHLY");
        lPayCheckDTO.setEmployeeFedAllowances(1);
        lPayCheckDTO.setEmployeeStateAllowances(1);
        lPayCheckDTO.setEmployeeFedFilingStatus("S");


        CheckPrintAddressDTO employeeAddress = new CheckPrintAddressDTO();
        employeeAddress.setAddressLine1("232 VISTA WAY");
        //companylegalAddress.setAddressLine2("emp Adrs Ln1");
        //companylegalAddress.setAddressLine3("emp Adrs Ln1");
        employeeAddress.setCity("Reno");
        employeeAddress.setState("NV");
        employeeAddress.setZipCode("89504");
        employeeAddress.setCountry("USA");

        lPayCheckDTO.setEmployeeAddress(employeeAddress);

        //deductions start
        Set<CheckPrintPaycheckLineDTO> deductions = new TreeSet<CheckPrintPaycheckLineDTO>();
        CheckPrintPaycheckLineDTO payCheckLineDTO = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO.setPaylineAmount(new BigDecimal("-250.02"));
        payCheckLineDTO.setPaylineDescription("MARDI GRAS1");
        payCheckLineDTO.setYtdAmount(new BigDecimal("-500.04"));
        deductions.add(payCheckLineDTO);

        CheckPrintPaycheckLineDTO payCheckLineDTO1 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO1.setPaylineAmount(new BigDecimal("-150.01"));
        payCheckLineDTO1.setPaylineDescription("MARDI GRAS2");
        payCheckLineDTO1.setYtdAmount(new BigDecimal("-300.02"));
        deductions.add(payCheckLineDTO1);
        //deductions end

        //ddeposit start
        List<CheckPrintPaycheckDDINfo> dDInfo = new ArrayList<CheckPrintPaycheckDDINfo>();
        CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo = new CheckPrintPaycheckDDINfo();
        checkPrintPaycheckDDINfo.setAccountId("actID");
        checkPrintPaycheckDDINfo.setAccountType("actName");
        checkPrintPaycheckDDINfo.setDDAmount(new BigDecimal("500.05"));
        dDInfo.add(checkPrintPaycheckDDINfo);

        CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo2 = new CheckPrintPaycheckDDINfo();
        checkPrintPaycheckDDINfo2.setAccountId("actID2");
        checkPrintPaycheckDDINfo2.setAccountType("actName2");
        checkPrintPaycheckDDINfo2.setDDAmount(new BigDecimal("555.55"));
        dDInfo.add(checkPrintPaycheckDDINfo2);

        //ddeposit end


        lPayCheckDTO.setDeductions(deductions);
        //lPayCheckDTO.setTaxes(taxes);
        //lPayCheckDTO.setPreTaxDeductions(preTaxDeductions);
        //lPayCheckDTO.setEarnings(earnings);
        lPayCheckDTO.setDirectDeposits(dDInfo);

        paychecks.add(lPayCheckDTO);
        checkPrintDTO.setPaychecks(paychecks);

        return checkPrintDTO;
    }

    private CheckPrintDTO createCoverPageDTO(CheckPrintDTO checkPrintDTO) {
        checkPrintDTO.setSenderName("Sender Name");
        checkPrintDTO.setCompanyId("123456789");
        checkPrintDTO.setPayrollAdminName("Pay roll admin name");

        CheckPrintAddressDTO senderAddress = new CheckPrintAddressDTO();
        senderAddress.setAddressLine1("Address Line1");
        senderAddress.setAddressLine2("Address Line 2");
        senderAddress.setAddressLine3("Address Line 3");
        senderAddress.setCity("Reno");
        senderAddress.setState("NV");
        senderAddress.setZipCode("89502");
        senderAddress.setCountry("USA");
        checkPrintDTO.setSenderAddress(senderAddress);

        CheckPrintAddressDTO companylegalAddress = new CheckPrintAddressDTO();
        companylegalAddress.setAddressLine1("Address Line1");
        companylegalAddress.setAddressLine2("Address Line 2");
        companylegalAddress.setAddressLine3("Address Line 3");
        companylegalAddress.setCity("Reno");
        companylegalAddress.setState("NV");
        companylegalAddress.setZipCode("89503");
        companylegalAddress.setCountry("USA");
        checkPrintDTO.setCompanyLegalAddress(companylegalAddress);

        return checkPrintDTO;
    }

    private CheckPrintDTO createTestCoverPage(CheckPrintDTO pCheckPrintDTO) {
        pCheckPrintDTO = createCoverPageDTO(pCheckPrintDTO);

        CheckPrintPaycheckDTO checkPrintPaycheckDTO = new CheckPrintPaycheckDTO();
        checkPrintPaycheckDTO.setIsTestCheck(true);
        List<CheckPrintPaycheckDTO> checkPrintPaycheckDTOs = new ArrayList<CheckPrintPaycheckDTO>(1);
        checkPrintPaycheckDTOs.add(checkPrintPaycheckDTO);
        pCheckPrintDTO.setPaychecks(checkPrintPaycheckDTOs);

        return pCheckPrintDTO;
    }

    private CheckPrintDTO testCreateMultipleChecks(CheckPrintDTO checkPrintDTO) {
        checkPrintDTO.setCompanyId("123456789");
        checkPrintDTO.setPayrollAdminName("Jack Russell");

        CheckPrintAddressDTO senderAddress = new CheckPrintAddressDTO();
        senderAddress.setAddressLine1("6888 Sierra Center Parkwy");
        senderAddress.setCity("Reno");
        senderAddress.setState("NV");
        senderAddress.setZipCode("89511");
        senderAddress.setCountry("USA");
        checkPrintDTO.setSenderAddress(senderAddress);

        CheckPrintAddressDTO companylegalAddress = new CheckPrintAddressDTO();
        companylegalAddress.setAddressLine1("234 QA TEST LANE");
        companylegalAddress.setAddressLine2("234 QA TEST LANE");
        companylegalAddress.setCity("FORT WORTH");
        companylegalAddress.setState("TEXAS");
        companylegalAddress.setZipCode("76112");
        companylegalAddress.setCountry("USA");
        checkPrintDTO.setCompanyLegalAddress(companylegalAddress);
        checkPrintDTO.setCompanyLegalName("QA TEST CLIENT SD 2");
        checkPrintDTO.setCompanyBankAccountNumber("123466");
        checkPrintDTO.setCompanyBankRoutingNumber("664321");
        checkPrintDTO.setCompanyBankName("Bank of America");

        RandomAccessFile rf;
        try {
            rf = new RandomAccessFile(Application.findFileOnClassPath("checkdistribution/signature.png"), "r");

            int size = (int) rf.length();
            byte imgData[] = new byte[size];
            rf.readFully(imgData);
            rf.close();
            checkPrintDTO.setCheckSignature(imgData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        List<CheckPrintPaycheckDTO> paychecks = new ArrayList<CheckPrintPaycheckDTO>();
        for(int i = 0; i<10; i++) {
            CheckPrintPaycheckDTO lPayCheckDTO = new CheckPrintPaycheckDTO();
            lPayCheckDTO.setCheckDate(CHECK_DATE);
            lPayCheckDTO.setPeriodStartDate(CHECK_DATE);
            lPayCheckDTO.setPeriodEndDate(CHECK_DATE);

            lPayCheckDTO.setCheckType(CheckPrintPaycheckDTO.PaycheckType.ManualCheck);
            //lPayCheckDTO.setCheckNetPay(new BigDecimal("2223.01"));
            // test micr font does not have a 5
            lPayCheckDTO.setCheckNumber("6666666");


            lPayCheckDTO.setEmployeeId("1500");
            lPayCheckDTO.setEmployeePrintName("1234567890123456789012345678901234567");
            lPayCheckDTO.setEmployeeWorkState("TX");
            lPayCheckDTO.setEmployeeStateFilingStatus("S");
            lPayCheckDTO.setEmployeePaySchedule("SEMIMONTHLY");
            lPayCheckDTO.setEmployeeFedAllowances(1);
            lPayCheckDTO.setEmployeeStateAllowances(1);
            lPayCheckDTO.setEmployeeFedFilingStatus("S");


            CheckPrintAddressDTO employeeAddress = new CheckPrintAddressDTO();
            employeeAddress.setAddressLine1("232 VISTA WAY");
            employeeAddress.setAddressLine2("232 VISTA WAY");
            employeeAddress.setCity("Reno");
            employeeAddress.setState("NV");
            employeeAddress.setZipCode("89504");
            employeeAddress.setCountry("USA");

            lPayCheckDTO.setEmployeeAddress(employeeAddress);

            //deductions start
            Set<CheckPrintPaycheckLineDTO> deductions = new TreeSet<CheckPrintPaycheckLineDTO>();
            CheckPrintPaycheckLineDTO payCheckLineDTO = new CheckPrintPaycheckLineDTO();
            payCheckLineDTO.setPaylineAmount(new BigDecimal("-250.02"));
            payCheckLineDTO.setPaylineDescription("1234567890123456789012345678901");
            payCheckLineDTO.setYtdAmount(new BigDecimal("-500.04"));
            deductions.add(payCheckLineDTO);

            CheckPrintPaycheckLineDTO payCheckLineDTO1 = new CheckPrintPaycheckLineDTO();
            payCheckLineDTO1.setPaylineAmount(new BigDecimal("-150.01"));
            payCheckLineDTO1.setPaylineDescription("MARDI GRAS2");
            payCheckLineDTO1.setYtdAmount(new BigDecimal("-300.02"));
            deductions.add(payCheckLineDTO1);
            //deductions end

            //taxes start
            Set<CheckPrintPaycheckLineDTO> taxes = new TreeSet<CheckPrintPaycheckLineDTO>();
            payCheckLineDTO = new CheckPrintPaycheckLineDTO();
            payCheckLineDTO.setPaylineAmount(new BigDecimal("129.69"));
            payCheckLineDTO.setPaylineDescription("FEDERAL WITHHOLDING1");
            payCheckLineDTO.setYtdAmount(new BigDecimal("259.38"));
            taxes.add(payCheckLineDTO);

            CheckPrintPaycheckLineDTO payCheckLine1 = new CheckPrintPaycheckLineDTO();
            payCheckLine1.setPaylineAmount(new BigDecimal("80.08"));
            payCheckLine1.setPaylineDescription("SOCIAL SECURITY1");
            payCheckLine1.setYtdAmount(new BigDecimal("160.16"));
            taxes.add(payCheckLine1);

            CheckPrintPaycheckLineDTO payCheckLineDTO2 = new CheckPrintPaycheckLineDTO();
            payCheckLineDTO2.setPaylineAmount(new BigDecimal("18.73"));
            payCheckLineDTO2.setPaylineDescription("MEDICARE1");
            payCheckLineDTO2.setYtdAmount(new BigDecimal("37.46"));
            taxes.add(payCheckLineDTO2);

            CheckPrintPaycheckLineDTO payCheckLineDTO3 = new CheckPrintPaycheckLineDTO();
            payCheckLineDTO3.setPaylineAmount(new BigDecimal("-18.13"));
            payCheckLineDTO3.setPaylineDescription("EARNED INCOME CREDIT1");
            payCheckLineDTO3.setYtdAmount(new BigDecimal("-36.26"));
            taxes.add(payCheckLineDTO3);
            //taxes end

            //pre tax deductions start
            Set<CheckPrintPaycheckLineDTO> preTaxDeductions = new TreeSet<CheckPrintPaycheckLineDTO>();
            payCheckLineDTO = new CheckPrintPaycheckLineDTO();
            payCheckLineDTO.setPaylineAmount(new BigDecimal("-6.20"));
            payCheckLineDTO.setPaylineDescription("Pretax1");
            payCheckLineDTO.setYtdAmount(new BigDecimal("-12.40"));
            preTaxDeductions.add(payCheckLineDTO);

            CheckPrintPaycheckLineDTO payCheckLineDTO11 = new CheckPrintPaycheckLineDTO();
            payCheckLineDTO11.setPaylineAmount(new BigDecimal("-6.2"));
            payCheckLineDTO11.setPaylineDescription("Pretax2");
            payCheckLineDTO11.setYtdAmount(new BigDecimal("-12.4"));
            preTaxDeductions.add(payCheckLineDTO11);

            CheckPrintPaycheckLineDTO payCheckLineDTO21 = new CheckPrintPaycheckLineDTO();
            payCheckLineDTO21.setPaylineAmount(new BigDecimal("-6.2"));
            payCheckLineDTO21.setPaylineDescription("Pretax3");
            payCheckLineDTO21.setYtdAmount(new BigDecimal("-12.4"));
            preTaxDeductions.add(payCheckLineDTO21);
            //pre tax deductions end

            //earnings start
            Set<CheckPrintPaycheckEarningLineDTO> earnings = new TreeSet<CheckPrintPaycheckEarningLineDTO>();
            CheckPrintPaycheckEarningLineDTO payCheckEarningLineDTO = new CheckPrintPaycheckEarningLineDTO();
            payCheckEarningLineDTO.setHours(new BigDecimal("12345.12"));
            payCheckEarningLineDTO.setRate(new BigDecimal("1234.12"));
            payCheckEarningLineDTO.setPaylineDescription("1234567890123456789012345678901");
            payCheckEarningLineDTO.setPaylineAmount(new BigDecimal("123456789.12"));
            payCheckEarningLineDTO.setYtdAmount(new BigDecimal("4567890.12"));
            earnings.add(payCheckEarningLineDTO);

            CheckPrintPaycheckEarningLineDTO payCheckEarningLineDTO1 = new CheckPrintPaycheckEarningLineDTO();
            payCheckEarningLineDTO1.setHours(new BigDecimal(0.00));
            payCheckEarningLineDTO1.setRate(new BigDecimal(0.00));
            payCheckEarningLineDTO1.setPaylineDescription("1234567890123456789012345678901");
            payCheckEarningLineDTO1.setPaylineAmount(new BigDecimal("100.12"));
            payCheckEarningLineDTO1.setYtdAmount(new BigDecimal("100.12"));
            earnings.add(payCheckEarningLineDTO1);
            //earnings end

            Set<CheckPrintPaycheckLineDTO> companyNonTaxContributions = new TreeSet<CheckPrintPaycheckLineDTO>();
            CheckPrintPaycheckLineDTO contribution1 = new CheckPrintPaycheckLineDTO();
            contribution1.setPayItemId(123);
            contribution1.setPaylineAmount(new BigDecimal(200.00));
            contribution1.setPaylineDescription("Out of Pocket");
            contribution1.setYtdAmount(new BigDecimal(100.00));
            companyNonTaxContributions.add(contribution1);
            CheckPrintPaycheckLineDTO contribution2 = new CheckPrintPaycheckLineDTO();
            contribution2.setPayItemId(123);
            contribution2.setPaylineAmount(new BigDecimal(300.00));
            contribution2.setPaylineDescription("Lunch Pay");
            contribution2.setYtdAmount(new BigDecimal(500.00));
            companyNonTaxContributions.add(contribution2);

            Set<CheckPrintPaycheckLineDTO> companyContributions = new TreeSet<CheckPrintPaycheckLineDTO>();
            CheckPrintPaycheckLineDTO contribution3 = new CheckPrintPaycheckLineDTO();
            contribution3.setPayItemId(123);
            contribution3.setPaylineAmount(new BigDecimal(700.00));
            contribution3.setPaylineDescription("PUCC");
            contribution3.setYtdAmount(new BigDecimal(600.00));
            companyContributions.add(contribution3);
            CheckPrintPaycheckLineDTO contribution4 = new CheckPrintPaycheckLineDTO();
            contribution4.setPayItemId(123);
            contribution4.setPaylineAmount(new BigDecimal(800.00));
            contribution4.setPaylineDescription("Birthday Bonus");
            contribution4.setYtdAmount(new BigDecimal(900.00));
            companyContributions.add(contribution4);


            //ddeposit start
            List<CheckPrintPaycheckDDINfo> dDInfo = new ArrayList<CheckPrintPaycheckDDINfo>();
            CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo = new CheckPrintPaycheckDDINfo();
            checkPrintPaycheckDDINfo.setAccountId("actID");
            checkPrintPaycheckDDINfo.setAccountType("actName");
            checkPrintPaycheckDDINfo.setDDAmount(new BigDecimal("500.05"));
            dDInfo.add(checkPrintPaycheckDDINfo);

            CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo2 = new CheckPrintPaycheckDDINfo();
            checkPrintPaycheckDDINfo2.setAccountId("actID2");
            checkPrintPaycheckDDINfo2.setAccountType("actName2");
            checkPrintPaycheckDDINfo2.setDDAmount(new BigDecimal("555.55"));
            dDInfo.add(checkPrintPaycheckDDINfo2);

            //ddeposit end


            lPayCheckDTO.setDeductions(deductions);
            lPayCheckDTO.setTaxes(taxes);
            lPayCheckDTO.setPreTaxDeductions(preTaxDeductions);
            lPayCheckDTO.setEarnings(earnings);
            lPayCheckDTO.setDirectDeposits(dDInfo);
            lPayCheckDTO.setCompanyTaxableContributions(companyContributions);
            lPayCheckDTO.setCompanyContributions(companyNonTaxContributions);
            paychecks.add(lPayCheckDTO);
        }
        checkPrintDTO.setPaychecks(paychecks);

        return checkPrintDTO;
    }

    private CheckPrintDTO testTooManyItems(CheckPrintDTO checkPrintDTO) {
        checkPrintDTO.setCompanyId("123456789");
        checkPrintDTO.setPayrollAdminName("Jack Russell");

        CheckPrintAddressDTO senderAddress = new CheckPrintAddressDTO();
        senderAddress.setAddressLine1("6888 Sierra Center Parkwy");
        senderAddress.setCity("Reno");
        senderAddress.setState("NV");
        senderAddress.setZipCode("89511");
        senderAddress.setCountry("USA");
        checkPrintDTO.setSenderAddress(senderAddress);

        CheckPrintAddressDTO companylegalAddress = new CheckPrintAddressDTO();
        companylegalAddress.setAddressLine1("234 QA TEST LANE");
        companylegalAddress.setCity("FORT WORTH");
        companylegalAddress.setState("TEXAS");
        companylegalAddress.setZipCode("76112");
        companylegalAddress.setCountry("USA");
        checkPrintDTO.setCompanyLegalAddress(companylegalAddress);
        checkPrintDTO.setCompanyLegalName("QA TEST CLIENT SD 2");
        checkPrintDTO.setCompanyBankAccountNumber("123466");
        checkPrintDTO.setCompanyBankRoutingNumber("664321");

        RandomAccessFile rf;
        try {
            rf = new RandomAccessFile(Application.findFileOnClassPath("checkdistribution/signature.png"), "r");

            int size = (int) rf.length();
            byte imgData[] = new byte[size];
            rf.readFully(imgData);
            rf.close();
            checkPrintDTO.setCheckSignature(imgData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        List<CheckPrintPaycheckDTO> paychecks = new ArrayList<CheckPrintPaycheckDTO>();
        CheckPrintPaycheckDTO lPayCheckDTO = new CheckPrintPaycheckDTO();
        lPayCheckDTO.setCheckDate(CHECK_DATE);
        lPayCheckDTO.setPeriodStartDate(CHECK_DATE);
        lPayCheckDTO.setPeriodEndDate(CHECK_DATE);

        lPayCheckDTO.setCheckType(CheckPrintPaycheckDTO.PaycheckType.ManualCheck);
        //lPayCheckDTO.setCheckNetPay(new BigDecimal("2223.01"));
        // test micr font does not have a 5
        lPayCheckDTO.setCheckNumber("6461");


        lPayCheckDTO.setEmployeeId("1500");
        lPayCheckDTO.setEmployeePrintName("CINDI C. RELEASE MACHINE");
        lPayCheckDTO.setEmployeeWorkState("TX");
        lPayCheckDTO.setEmployeeStateFilingStatus("S");
        lPayCheckDTO.setEmployeePaySchedule("SEMIMONTHLY");
        lPayCheckDTO.setEmployeeFedAllowances(1);
        lPayCheckDTO.setEmployeeStateAllowances(1);
        lPayCheckDTO.setEmployeeFedFilingStatus("S");


        CheckPrintAddressDTO employeeAddress = new CheckPrintAddressDTO();
        employeeAddress.setAddressLine1("232 VISTA WAY");
        employeeAddress.setCity("Reno");
        employeeAddress.setState("NV");
        employeeAddress.setZipCode("89504");
        employeeAddress.setCountry("USA");

        lPayCheckDTO.setEmployeeAddress(employeeAddress);

        //deductions start
        Set<CheckPrintPaycheckLineDTO> deductions = new TreeSet<CheckPrintPaycheckLineDTO>();
        CheckPrintPaycheckLineDTO payCheckLineDTO = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO.setPaylineAmount(new BigDecimal("-250.02"));
        payCheckLineDTO.setPaylineDescription("MARDI GRAS1");
        payCheckLineDTO.setYtdAmount(new BigDecimal("-500.04"));
        deductions.add(payCheckLineDTO);

        CheckPrintPaycheckLineDTO payCheckLineDTO1 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO1.setPaylineAmount(new BigDecimal("-150.01"));
        payCheckLineDTO1.setPaylineDescription("MARDI GRAS2");
        payCheckLineDTO1.setYtdAmount(new BigDecimal("-300.02"));
        deductions.add(payCheckLineDTO1);
        //deductions end

        //taxes start
        Set<CheckPrintPaycheckLineDTO> taxes = new TreeSet<CheckPrintPaycheckLineDTO>();
        payCheckLineDTO = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO.setPaylineAmount(new BigDecimal("129.69"));
        payCheckLineDTO.setPaylineDescription("FEDERAL WITHHOLDING1");
        payCheckLineDTO.setYtdAmount(new BigDecimal("259.38"));
        taxes.add(payCheckLineDTO);

        CheckPrintPaycheckLineDTO payCheckLine1 = new CheckPrintPaycheckLineDTO();
        payCheckLine1.setPaylineAmount(new BigDecimal("80.08"));
        payCheckLine1.setPaylineDescription("SOCIAL SECURITY1");
        payCheckLine1.setYtdAmount(new BigDecimal("160.16"));
        taxes.add(payCheckLine1);

        CheckPrintPaycheckLineDTO payCheckLineDTO2 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO2.setPaylineAmount(new BigDecimal("18.73"));
        payCheckLineDTO2.setPaylineDescription("MEDICARE1");
        payCheckLineDTO2.setYtdAmount(new BigDecimal("37.46"));
        taxes.add(payCheckLineDTO2);

        for (int i = 0; i < 50; i++) {
            CheckPrintPaycheckLineDTO payCheckLineDTO3 = new CheckPrintPaycheckLineDTO();
            payCheckLineDTO3.setPaylineAmount(new BigDecimal("-18.13"));
            payCheckLineDTO3.setPaylineDescription("EARNED INCOME CREDIT1");
            payCheckLineDTO3.setYtdAmount(new BigDecimal("-36.26"));
            taxes.add(payCheckLineDTO3);
        }

        //taxes end

        //pre tax deductions start
        Set<CheckPrintPaycheckLineDTO> preTaxDeductions = new TreeSet<CheckPrintPaycheckLineDTO>();
        payCheckLineDTO = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO.setPaylineAmount(new BigDecimal("-6.20"));
        payCheckLineDTO.setPaylineDescription("Pretax1");
        payCheckLineDTO.setYtdAmount(new BigDecimal("-12.40"));
        preTaxDeductions.add(payCheckLineDTO);

        CheckPrintPaycheckLineDTO payCheckLineDTO11 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO11.setPaylineAmount(new BigDecimal("-6.2"));
        payCheckLineDTO11.setPaylineDescription("Pretax2");
        payCheckLineDTO11.setYtdAmount(new BigDecimal("-12.4"));
        preTaxDeductions.add(payCheckLineDTO11);

        CheckPrintPaycheckLineDTO payCheckLineDTO21 = new CheckPrintPaycheckLineDTO();
        payCheckLineDTO21.setPaylineAmount(new BigDecimal("-6.2"));
        payCheckLineDTO21.setPaylineDescription("Pretax3");
        payCheckLineDTO21.setYtdAmount(new BigDecimal("-12.4"));
        preTaxDeductions.add(payCheckLineDTO21);
        //pre tax deductions end

        //earnings start
        Set<CheckPrintPaycheckEarningLineDTO> earnings = new TreeSet<CheckPrintPaycheckEarningLineDTO>();
        CheckPrintPaycheckEarningLineDTO payCheckEarningLineDTO = new CheckPrintPaycheckEarningLineDTO();
        payCheckEarningLineDTO.setHours(new BigDecimal("100"));
        payCheckEarningLineDTO.setRate(new BigDecimal("60"));
        payCheckEarningLineDTO.setPaylineDescription("earningsDesc");
        payCheckEarningLineDTO.setPaylineAmount(new BigDecimal("100.00"));
        payCheckEarningLineDTO.setYtdAmount(new BigDecimal("200.00"));
        earnings.add(payCheckEarningLineDTO);

        CheckPrintPaycheckEarningLineDTO payCheckEarningLineDTO1 = new CheckPrintPaycheckEarningLineDTO();
        payCheckEarningLineDTO1.setHours(new BigDecimal("101"));
        payCheckEarningLineDTO1.setRate(new BigDecimal("61"));
        payCheckEarningLineDTO1.setPaylineDescription("earningsDesc1");
        payCheckEarningLineDTO1.setPaylineAmount(new BigDecimal("101.00"));
        payCheckEarningLineDTO1.setYtdAmount(new BigDecimal("202.00"));
        earnings.add(payCheckEarningLineDTO1);
        //earnings end

        //ddeposit start
        List<CheckPrintPaycheckDDINfo> dDInfo = new ArrayList<CheckPrintPaycheckDDINfo>();
        CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo = new CheckPrintPaycheckDDINfo();
        checkPrintPaycheckDDINfo.setAccountId("actID");
        checkPrintPaycheckDDINfo.setAccountType("actName");
        checkPrintPaycheckDDINfo.setDDAmount(new BigDecimal("500.05"));
        dDInfo.add(checkPrintPaycheckDDINfo);

        CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo2 = new CheckPrintPaycheckDDINfo();
        checkPrintPaycheckDDINfo2.setAccountId("actID2");
        checkPrintPaycheckDDINfo2.setAccountType("actName2");
        checkPrintPaycheckDDINfo2.setDDAmount(new BigDecimal("555.55"));
        dDInfo.add(checkPrintPaycheckDDINfo2);

        //ddeposit end


        lPayCheckDTO.setDeductions(deductions);
        lPayCheckDTO.setTaxes(taxes);
        lPayCheckDTO.setPreTaxDeductions(preTaxDeductions);
        lPayCheckDTO.setEarnings(earnings);
        lPayCheckDTO.setDirectDeposits(dDInfo);

        paychecks.add(lPayCheckDTO);
        checkPrintDTO.setPaychecks(paychecks);

        return checkPrintDTO;
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    // only used to generate initial test files for comparison
    /*public static void main(String[] args) {
        try {
            CheckPrintPackageTests checkPrintPackageTests = new CheckPrintPackageTests();
            writeFile("test_coverpage.pdf",
                    CheckPrintPackage.generateCoverPage(checkPrintPackageTests.createCoverPageDTO(new CheckPrintDTO()), new Date("02/27/2010")));

            writeFile("test_testcoverpage.pdf",
                    CheckPrintPackage.generateCoverPage(checkPrintPackageTests.createTestCoverPage(new CheckPrintDTO()), new Date("02/27/2010")));

            FontFactory.register(Application.findFileOnClassPath("checkdistribution/IDAutomationSMICR_for_testing_only.ttf"), "IDAutomationMICR");

            writeFile("test_PaycheckDeductionTaxesEarningssOnly.pdf",
                    CheckPrintPackage.generatePaychecks(checkPrintPackageTests.testCreateCheckPrintDeductionsTaxesEarnings(new CheckPrintDTO())));

            writeFile("test_PaycheckTaxesEarningssOnly.pdf",
                    CheckPrintPackage.generatePaychecks(checkPrintPackageTests.testCreateCheckPrintTaxesEarnings(new CheckPrintDTO())));

            writeFile("test_PaycheckDeductionsOnly.pdf",
                    CheckPrintPackage.generatePaychecks(checkPrintPackageTests.testCreateCheckPrintDeductionsOnly(new CheckPrintDTO())));

            writeFile("test_PaycheckAll.pdf",
                    CheckPrintPackage.generatePaychecks(checkPrintPackageTests.testCreateCheckPrintAll(new CheckPrintDTO())));

            writeFile("test_MultiplePaychecks.pdf",
                    CheckPrintPackage.generatePaychecks(checkPrintPackageTests.testCreateMultipleChecks(new CheckPrintDTO())));

            writeFile("test_TooManyItems.pdf",
                    CheckPrintPackage.generatePaychecks(checkPrintPackageTests.testTooManyItems(new CheckPrintDTO())));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeFile (String fileName, byte[] fileContents) {

        try {
            FileOutputStream file = new FileOutputStream(fileName);
            file.write(fileContents);
            file.close();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
     }*/
}