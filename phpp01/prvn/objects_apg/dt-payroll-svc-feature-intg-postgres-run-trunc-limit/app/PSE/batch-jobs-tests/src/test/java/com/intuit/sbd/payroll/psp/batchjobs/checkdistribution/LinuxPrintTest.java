package com.intuit.sbd.payroll.psp.batchjobs.checkdistribution;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintAddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckDDINfo;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckEarningLineDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckLineDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.PrintManualChecks;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.CheckDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.LineItemDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.PayeeDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.PayerDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.PdfPrinter;
import org.junit.Ignore;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Ignore
public class LinuxPrintTest {
    public static void main(String[] args) {
        String printerName = args[0];
        String signatureFile = args[1];
        boolean use2AddressLines = Boolean.parseBoolean(args[2]);
        String printType = args[3];
        String bankLogoFile = args[4];
        String logoFile = args[5];

        CheckPrintDTO lCheckPrintDTO;
        if(printType.equalsIgnoreCase("dd")) {
            lCheckPrintDTO = testDDPrintCheck(signatureFile, use2AddressLines);
        }
        else {
            lCheckPrintDTO = testCreateCheckPrintAll(signatureFile, use2AddressLines);
        }

        System.out.println("Begin Test Print");
        byte[] pdf = new byte[0];
        if(!printType.equalsIgnoreCase("tax")) {
            try {
                pdf = CheckPrintPackage.generatePaychecks(lCheckPrintDTO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                pdf = PrintManualChecks.generateManualChecks(testCheckDTOs(signatureFile, bankLogoFile, logoFile, use2AddressLines));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //writeFile("PaycheckAll.pdf", pdf);
        System.out.println("Page generated");
        PdfPrinter pdfPrinter = new PdfPrinter(printerName, "testPrint");
        pdfPrinter.printPdf(pdf, !printType.equalsIgnoreCase("tax"));
        System.out.println("page sent to printer");
    }

    private static List<CheckDTO> testCheckDTOs(String signatureFile, String bankLogoFile, String logoFile, boolean use2AddressLines) throws Exception {
        RandomAccessFile rf = new RandomAccessFile(Application.findFileOnClassPath(signatureFile), "r");
        int size = (int) rf.length();
        byte signature[] = new byte[size];
        rf.readFully(signature);
        rf.close();

        rf = new RandomAccessFile(Application.findFileOnClassPath(bankLogoFile), "r");
        size = (int) rf.length();
        byte[] chaseLogo = new byte[size];
        rf.readFully(chaseLogo);
        rf.close();

        rf = new RandomAccessFile(Application.findFileOnClassPath(logoFile), "r");
        size = (int) rf.length();
        byte[] intuitLogo = new byte[size];
        rf.readFully(intuitLogo);
        rf.close();

        CheckDTO checkDTO = new CheckDTO();
        checkDTO.setCheckAmount(new BigDecimal(234185.63));
        checkDTO.setCheckDate(new Date("04/04/2011"));
        checkDTO.setCheckNumber("01478438");
        checkDTO.setCompanyLegalName("Lenox Hill Invertentional Card Company");
        checkDTO.setMemo("*Refund*");
        checkDTO.setPrintDate(new Date("03/31/2011"));
        checkDTO.setSourceCompanyNumber("336012450");
        checkDTO.setTaxId("201435770 9127");

        PayerDTO payerDTO = new PayerDTO();
        payerDTO.setAddressLine1("6884 Sierra Center Parkway");
        if(use2AddressLines) {
            payerDTO.setAddressLine2("Cube 12G23");
        }
        payerDTO.setNameLine1("Intuit Payroll Services");
        if(use2AddressLines) {
            payerDTO.setNameLine2("Is awesome!");
        }
        payerDTO.setCity("Reno");
        payerDTO.setState("NV");
        payerDTO.setZip("89511-2210");
        checkDTO.setPayerDTO(payerDTO);

        PayeeDTO payeeDTO = new PayeeDTO();
        payeeDTO.setNameLine1("NYS Employement Taxes");
        if(use2AddressLines) {
            payeeDTO.setNameLine2("A second Name");
        }
        payeeDTO.setAddressLine1("33 Lewis Road");
        if(use2AddressLines) {
            payeeDTO.setAddressLine2("Box 258/4");
        }
        payeeDTO.setCity("Binghamton");
        payeeDTO.setState("NY");
        payeeDTO.setZip("13905-1040");
        checkDTO.setPayeeDTO(payeeDTO);

        checkDTO.setBankAccountNumber("401890838");
        checkDTO.setRoutingNumber("011309279");

        checkDTO.getPayerDTO().setSignature(signature);
        checkDTO.getPayerDTO().setBankLogo(chaseLogo);
        checkDTO.getPayerDTO().setLogo(intuitLogo);

        LineItemDTO lineItem = new LineItemDTO();
        lineItem.setAmount(new BigDecimal(174574.43));
        lineItem.setLiabilityQuarter(1);
        lineItem.setLiabilityYear(2011);
        lineItem.setType("ST W/H");
        checkDTO.getLineItems().add(lineItem);

        lineItem = new LineItemDTO();
        lineItem.setAmount(new BigDecimal(5959.71));
        lineItem.setLiabilityQuarter(1);
        lineItem.setLiabilityYear(2011);
        lineItem.setType("NYC RE");
        checkDTO.getLineItems().add(lineItem);

        lineItem = new LineItemDTO();
        lineItem.setAmount(new BigDecimal(4.14));
        lineItem.setLiabilityQuarter(1);
        lineItem.setLiabilityYear(2011);
        lineItem.setType("YONKERS");
        checkDTO.getLineItems().add(lineItem);

        return Arrays.asList(checkDTO);
    }

    private static CheckPrintDTO testDDPrintCheck(String signatureFile, boolean use2AddressLines) {
        CheckPrintDTO checkPrintDTO = new CheckPrintDTO();
        checkPrintDTO.setCompanyId("113-00004");

        CheckPrintAddressDTO companylegalAddress = new CheckPrintAddressDTO();
        companylegalAddress.setAddressLine1("234 QA TEST LANE");
        if (use2AddressLines) {
            companylegalAddress.setAddressLine2("234 QA TEST LANE");
        }
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
            rf = new RandomAccessFile(signatureFile, "r");

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
        lPayCheckDTO.setCheckDate(new DateDTO());
        lPayCheckDTO.setPeriodStartDate(new DateDTO());
        lPayCheckDTO.setPeriodEndDate(new DateDTO());

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
        if (use2AddressLines) {
            employeeAddress.setAddressLine2("232 VISTA WAY");
        }
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

    private static CheckPrintDTO testCreateCheckPrintAll(String signatureFile,  boolean use2AddressLines) {
        CheckPrintDTO checkPrintDTO = new CheckPrintDTO();
        checkPrintDTO.setCompanyId("113-00004");

        CheckPrintAddressDTO companylegalAddress = new CheckPrintAddressDTO();
        companylegalAddress.setAddressLine1("234 QA TEST LANE");
        if (use2AddressLines) {
            companylegalAddress.setAddressLine2("234 QA TEST LANE");
        }
        companylegalAddress.setCity("FORT WORTH");
        companylegalAddress.setState("TEXAS");
        companylegalAddress.setZipCode("76112");
        companylegalAddress.setCountry("USA");
        checkPrintDTO.setCompanyLegalAddress(companylegalAddress);
        checkPrintDTO.setCompanyLegalName("QA TEST CLIENT SD 2");
        checkPrintDTO.setCompanyBankAccountNumber("000790057418");
        checkPrintDTO.setCompanyBankRoutingNumber("122400724");
        checkPrintDTO.setCompanyBankName("Bank of America");

        RandomAccessFile rf = null;
        try {
            rf = new RandomAccessFile(signatureFile, "r");

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
        lPayCheckDTO.setCheckDate(new DateDTO());
        lPayCheckDTO.setPeriodStartDate(new DateDTO());
        lPayCheckDTO.setPeriodEndDate(new DateDTO());
        lPayCheckDTO.setCheckNumber("E2345TN");

        lPayCheckDTO.setCheckType(CheckPrintPaycheckDTO.PaycheckType.ManualCheck);
        lPayCheckDTO.setCheckNetPay(new BigDecimal("2223.01"));
        lPayCheckDTO.setCheckNumber("5451");


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
        if (use2AddressLines) {
            employeeAddress.setAddressLine2("232 VISTA WAY");
        }
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
        checkPrintDTO.setPaychecks(paychecks);

        return checkPrintDTO;
    }

    private static void writeFile (String fileName, byte[] fileContents) {

        try {
            FileOutputStream file = new FileOutputStream(fileName);
            file.write(fileContents);
            file.close();
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }

    }

}