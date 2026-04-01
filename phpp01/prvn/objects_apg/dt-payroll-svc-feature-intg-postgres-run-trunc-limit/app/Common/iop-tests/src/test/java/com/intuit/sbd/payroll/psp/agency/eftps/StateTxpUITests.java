package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.AgencyIdDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.tools.ComplianceToolkit;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.util.StringUtil;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * Tests for state SUI Payments
 */
public class StateTxpUITests {

    private static final String TXP_PREFIX = "TXP*";
    private static final String MN_UI_TXP_PREFIX = "000*";
    private static final String YYMMDD_DATE_FORMAT = "yyMMdd";
    private static final String VT_UI_TXP_PREFIX = "DOL*";
    private int achTaxOffloadOffset;

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = Application.refresh(achTaxOffloadOffsetParam);
        achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", PSPDate.getPSPTime());
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSuiAL() {
        String ein = "222222223";
        String stateEin = "1224567890";
        String psid = "123272727";

        // Test state Id 10 bytes long and deposit frequency is default i.e. Monthly
        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "AL", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + stateEin + "*" + CalendarUtils.getQuarterAsInt(entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodEnd()) +
                "*" + StateTxpTestsHelper.getEndDateYear(entryDetailRecord) + "\\";

        assertEquals("AL TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiAZ() {
        String stateEin = "1224567 8";
        String fedEin = "4444-555555";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "AZ", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + "1224567" + "*" + StateTxpTestsHelper.getWithoutHyphens(fedEin) + "*0*" +
                StateTxpTestsHelper.getEndDateYear(entryDetailRecord) +
                "*" + CalendarUtils.getQuarterAsInt(entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodEnd()) + "*7265100*\\";

        assertEquals("AZ TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiLA() {
        String stateEin = "1224567";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(null, stateEin, "LA", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + stateEin + "*" + "13000" + "*" +  StateTxpTestsHelper.getEndYearAndQuarter(entryDetailRecord) + "*T*000*T*000*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8)+ "**\\";

        assertEquals("LA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiCA() {
        String stateEin = "122-4567-8";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(null, stateEin, "CA", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + "12245678" + "*01300*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) +
                "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("174.00"), 8) +
                "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("284.00"), 8) +
                "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("458.00"), 8) + "*000000*\\";

        assertEquals("CA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiFL() {
        String stateEin = "1224567-8";

        // Test with an EIN that will reduce to fewer than 7 digits.
        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(null, stateEin, "FL", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + "00000000" + "1224567" + "*05425*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) +
                "*1*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) + "\\";

        assertEquals("FL TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiIA() {
        String fedEin = "4444-555555";
        String stateEin = "00122456";
        String bankAccountNumber = "899999999999";

        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("IA-600103-PAYMENT", "Client Bank Acct", bankAccountNumber);
        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "IA", 1, agencyIdDTO, PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + "00122456" + "*13000*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) + "*****" + StateTxpTestsHelper.getWithoutHyphens(fedEin) + "\\";

        assertEquals("IA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiIL() {
        String fedEin = "4444-555555";
        String stateEin = "1224567-8";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "IL", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + StateTxpTestsHelper.getWithoutHyphens(fedEin) + "*ILUI*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) +
                "*U*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 1) + "\\";

        assertEquals("IL TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiKS() {
        String stateEin = "1224567";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(null, stateEin, "KS", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + "122456000000000" + "*UITAX*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 1) + "\\";

        assertEquals("KS TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiMA() {
        String fedEin = "4444-555555";
        String stateEin = "12245678";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "MA", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + "012245678*444455555*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) +
                "*0000000000000000000000000000000000000000000*\\";

        assertEquals("MA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiMD() {
        String fedEin = "4444-555555";
        String stateEin = "0012245678";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "MD", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + "0012245678*130*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 1) + "\\";

        assertEquals("MD TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiME() {

        String fedEin = "4444-555555";
        String stateEin = "1224567890";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "ME", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + stateEin+"*13055*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) + "\\";
        assertEquals("ME TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        //Change to old format and  run compliance toolkit
        Application.beginUnitOfWork();
        entryDetailRecord.setTxpRecordData(TXP_PREFIX + StateTxpTestsHelper.getWithoutHyphens(fedEin)+"00"+"*13003*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) +
                                                   "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) + "\\");
        Company company = entryDetailRecord.getCompany();
        Application.commitUnitOfWork();
        ComplianceToolkit.main(new String[]{ComplianceToolkit.ToolkitCommand.RecreateEntryDetailRecords.name(),"ME-941C1ME-PAYMENT"});
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> statePayments = DataLoadServices.getReadyToSendTaxPayments(company, PaymentMethod.ACHCredit);

        DomainEntitySet<MoneyMovementTransaction> mmts = statePayments.find(MoneyMovementTransaction.PaymentTemplate().equalTo(PaymentTemplate.findPaymentTemplate("ME-941C1ME-PAYMENT")));
        entryDetailRecord = mmts.getFirst().getEntryDetailRecordCollection().find(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).getFirst() ;
        PayrollServices.rollbackUnitOfWork();
        assertEquals("ME TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiMN() {

        String fedEin = "4444-555555";
        String stateEin = "01224567-0123";
        String psid = "123272727";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "MN", PaymentTemplateCategory.SUI);
        String expected = MN_UI_TXP_PREFIX + "001224567*444455555*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) +
                "*000000000000000000000000000000000000000000*\\";

        assertEquals("MN TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        // Test with an EIN that does not include the leading 0.
        stateEin = "12234567-0123";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 3, 28, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-04-05"), fedEin, stateEin, "MN",
                                                                        DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()), 2, PaymentTemplateCategory.SUI);

        expected = MN_UI_TXP_PREFIX + "012234567*444455555*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) +
                "*000000000000000000000000000000000000000000*\\";
        assertEquals("MN TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        //Test with an 8-digit ein
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        stateEin = "12245678";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 3, 28, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "MN", PaymentTemplateCategory.SUI);

        expected = MN_UI_TXP_PREFIX + "012245678*444455555*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) +
                "*000000000000000000000000000000000000000000*\\";
        assertEquals("MN TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        SpcfCalendar ofloadDate = SpcfCalendar.createInstance(2011,4,28,13,30,00,0,SpcfTimeZone.getLocalTimeZone()) ;
        DataLoadServices.runOffloadTaxPayments(ofloadDate);

    }

    @Test
    public void testSuiNC() {

        String fedEin = "4444-555555";
        String stateEin = "12-24-567 8";
        String psid = "123272727";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "NC", PaymentTemplateCategory.SUI);
        String expected = TXP_PREFIX + "12245678*01300*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) + "*T*" +
                StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) + "\\";
        assertEquals("NC TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // Test with an EIN that will be reduced to 7 digits which should cause a padding space to be added.
        stateEin = "12-24-567";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 3, 28, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-04-05"), fedEin, stateEin, "NC",
                DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()), 2, PaymentTemplateCategory.SUI);
        expected = TXP_PREFIX + "1224567 *01300*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) + "*T*" +
                StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) + "\\";
        assertEquals("NC TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiND() {

        String fedEin = "4444-55555";
        String stateEin = "1224567";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "ND", PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        entryDetailRecord = Application.refresh( entryDetailRecord );
        String legalName = StringUtil.truncate(entryDetailRecord.getCompany().getLegalName(), 15).toUpperCase();
        PayrollServices.rollbackUnitOfWork();

        String expected = "1224567" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) +
                "444455555" + legalName;

        assertEquals("ND TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiNE() {

        String fedEin = "4444-55555";
        String stateEin = "1224567890";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "NE", PaymentTemplateCategory.SUI);

        String endOfQuarterMonth = "" + CalendarUtils.getLastDayOfQuarter(entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodEnd()).getMonth();
        endOfQuarterMonth = StringUtil.leftPad(endOfQuarterMonth, "0", 2);

        PayrollServices.beginUnitOfWork();
        entryDetailRecord = Application.refresh( entryDetailRecord );
        String legalName = StringUtil.truncate(entryDetailRecord.getCompany().getLegalName(), 15).toUpperCase();

        String expected = "444455555*1224567890*" + StateTxpTestsHelper.getEndDateYear(entryDetailRecord) + "*" + endOfQuarterMonth +
                "*" + StringUtil.rightPad(legalName, " ", 30) +
                "*COMPUTING RESOURCE*/";
        PayrollServices.rollbackUnitOfWork();

        assertEquals("NE TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiNJ() {

        String fedEin = "369456218";
        String stateEin = "369456218/134";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "NJ", PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        entryDetailRecord = Application.refresh( entryDetailRecord );

        // First 4 digits of company name "The " removed if present.
        String erName = entryDetailRecord.getCompany().getLegalName().toUpperCase();
        if ( erName.startsWith("THE ")) {
            erName = erName.substring(4);
        }

        String expected = TXP_PREFIX + "B369456218000*13002*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 1) + "*****" +
                StringUtil.truncate(erName, 4) + "\\";
        PayrollServices.rollbackUnitOfWork();

        assertEquals("NJ TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiNV() {

        String fedEin = "4444-55555";
        String stateEin = "122456.78-9";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "NV", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + "012245678*444455555*" +
                StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) +
                "*NVUICR*000000*000000*\\";

        assertEquals("NV TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiNVAgencyIDFormatChange() {

        String fedEin = "4444-55555";
        String stateEin = "022456999";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "NV", PaymentTemplateCategory.SUI);

        String expected = TXP_PREFIX + "022456999*444455555*" +
                StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) +
                "*NVUICR*000000*000000*\\";

        assertEquals("NV TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiNVAgencygetNewFormatFuntion(){

        String actual = EntryDetailRecord.getFormattedAgencyIdForNV("12345680-9");

        String expected = "012345680";
        org.junit.Assert.assertEquals(actual,expected);

        actual = EntryDetailRecord.getFormattedAgencyIdForNV("123456809");

        expected = "012345680";
        org.junit.Assert.assertEquals(actual,expected);

        actual = EntryDetailRecord.getFormattedAgencyIdForNV("113456.80-9");

        expected = "011345680";
        org.junit.Assert.assertEquals(actual,expected);

        actual = EntryDetailRecord.getFormattedAgencyIdForNV("0113456809");

        expected = "001134568";
        org.junit.Assert.assertEquals(actual,expected);


        actual = EntryDetailRecord.getFormattedAgencyIdForNV("011345680");

        expected = "011345680";
        org.junit.Assert.assertEquals(actual,expected);

    }

    @Test
    public void testSuiNVAgencyIDEmpty() {

        String fedEin = "4444-55555";
        String stateEin = "";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "NV", PaymentTemplateCategory.SUI);

        assertNull("EDR generated, Should not be ACH credit", entryDetailRecord);

        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();

        stateEin = null;
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "NV", PaymentTemplateCategory.SUI);

        assertNull("EDR generated, Should not be ACH credit", entryDetailRecord);
    }


    @Test
    public void testSuiNVWrongAgencyID() {
        //Agency ID is invalid. It should not be a sequence of numbers or repetition of same number.

        String fedEin = "4444-55555";
        String stateEin = "111111";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "NV", PaymentTemplateCategory.SUI);

        assertNull("EDR generated, Should not be ACH credit", entryDetailRecord);

        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();

        stateEin = "123456";
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "NV", PaymentTemplateCategory.SUI);

        assertNull("EDR generated, Should not be ACH credit", entryDetailRecord);


        //Should start with zero 0NNNNNNNN

        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();

        stateEin = "133245645";
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "NV", PaymentTemplateCategory.SUI);

        assertNull("EDR generated, Should not be ACH credit", entryDetailRecord);

        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();

        //Correct value
        stateEin = "012344666";
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "NV", PaymentTemplateCategory.SUI);

        assertNotNull("EDR generated, Should  be ACH credit", entryDetailRecord);

        String expected = TXP_PREFIX + "012344666*444455555*" +
                StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) +
                "*NVUICR*000000*000000*\\";

        assertEquals("NV TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiOR() {

        String fedEin = "4444-55555";
        String stateEin = "1224567-8";

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = StateTxpTestsHelper.createEntryDetailRecords(fedEin, stateEin, "OR", PaymentTemplateCategory.SUI);

        assertEquals("The number of OR ERD is not equal to 2", 2, entryDetailRecords.size());

        String[] expected = new String[2];
        expected[0] = TXP_PREFIX + "012245678*01101*" + StateTxpTestsHelper.getQuarterEndDate(entryDetailRecords.get(0), YYMMDD_DATE_FORMAT) +
            "*S*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("240.00"), 1) +
            "*S*0*S*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("700.00"), 1) + "\\";

        expected[1] = TXP_PREFIX + "012245678*01102*" + StateTxpTestsHelper.getQuarterEndDate(entryDetailRecords.get(1), YYMMDD_DATE_FORMAT) +
                "*L*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("344.00"), 1) +
                "*L*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("346.00"), 1) + "\\";

        boolean match = false;
        for (String exp : expected) {
            match = false;
            for (EntryDetailRecord edr : entryDetailRecords) {
                if (exp.equals(edr.getTxpRecordData())) {
                    match = true;
                    break;
                }
            }

            if (!match) {
                break;
            }
        }
        assertEquals("OR TXP output does not match expected output", true, match);

        // extended test for Intuit Batch file
        PayrollServices.beginUnitOfWork();
        String taxOffloadTime = achTaxOffloadOffset == 1? "20110429000000" : "20110428000000"; 
        PSPDate.setPSPTime(taxOffloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split("\r\n");

        System.out.println(output);
    }

    @Test
    public void testSuiTN() {

        String fedEin = "4444-55555";
        String stateEin = "1224-567 8";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "TN", PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        entryDetailRecord = Application.refresh( entryDetailRecord );
        String legalName = StringUtil.truncate(entryDetailRecord.getCompany().getLegalName(), 6).toUpperCase();

        String expected = TXP_PREFIX + "12245678*99999*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) +
             "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 1) + "*444455555*" + legalName + "**\\";
        PayrollServices.rollbackUnitOfWork();

        assertEquals("TN TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiTX() {

        String fedEin = "4444-55555";
        String stateEin = "12-245678-9";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "TX", PaymentTemplateCategory.SUI);

        SpcfCalendar endDate = CalendarUtils.getLastDayOfQuarter(entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodEnd());
        String expected = TXP_PREFIX + "122456789*68307*" + endDate.format(YYMMDD_DATE_FORMAT) +
             "*1*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 1) + "\\";

        assertEquals("TX TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiWI() {

        String fedEin = "4444-55555";
        String stateEin = "123456-A7B-1";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "WI", PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        entryDetailRecord = Application.refresh( entryDetailRecord );

        // First 6 digits of company name "The " removed if present.
        String erName = entryDetailRecord.getCompany().getLegalName().toUpperCase();
        if ( erName.startsWith("THE ")) {
            erName = erName.substring(4);
        }

        String expected = TXP_PREFIX + "1234561*13000*" + StateTxpTestsHelper.getEnd(entryDetailRecord, YYMMDD_DATE_FORMAT) +
             "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 1) +
             "*****" + StringUtil.truncate(erName, 6) + "\\";
        PayrollServices.rollbackUnitOfWork();

        assertEquals("WI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testSuiPA() {

        String fedEin = "4444-55555";
        String stateEin = "3110765";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "PA", PaymentTemplateCategory.SUI);

        SpcfCalendar endDate = CalendarUtils.getLastDayOfQuarter(entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodEnd());
        String expected = TXP_PREFIX + "444455555      *UC001*" + endDate.format(YYMMDD_DATE_FORMAT) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) + "\\";

        String txpRecord = entryDetailRecord.getTxpRecordData().substring(0, entryDetailRecord.getTxpRecordData().indexOf("\\") + 1);
        String randomTransactionId = entryDetailRecord.getTxpRecordData().substring(entryDetailRecord.getTxpRecordData().indexOf("\\") + 1);
        assertEquals("TX TXP output does not match expected output", expected, txpRecord);
        Assert.assertTrue("Length of TransactionId", randomTransactionId.length() > 15);
        Assert.assertTrue("Length of TransactionId", randomTransactionId.length() < 33);
        assertEquals("6th record position 40-55, agency id without dashes (left justified with unused filled with blank)", 39, entryDetailRecord.getRecordData().indexOf("3110765"));
    }

    @Test
    public void testSuiID() {

        String fedEin = "4444-55555";
        String stateEin = "0953110765";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "ID", PaymentTemplateCategory.SUI);

        SpcfCalendar endDate = CalendarUtils.getLastDayOfQuarter(entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodEnd());
        String expected = TXP_PREFIX + "0953110765*13090*" + endDate.format(YYMMDD_DATE_FORMAT) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) + "\\";
        String txpRecord = entryDetailRecord.getTxpRecordData();
        assertEquals("ID TXP output does not match expected output", expected, txpRecord);
    }
    @Test
    public void testSuiMO() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MO-MODES-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MO-941-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.setPSPDate(2016, 6, 1);

        String fedEin = "122456788";
        String stateEin = "12-12345-1-12";
        String psid = "10654125";

        Company company = DataLoadServices.createAssistedCompany("MO",psid,fedEin,stateEin);

        PayrollServices.beginUnitOfWork();
        CompanyAgency ca=  CompanyAgency.findCompanyAgency(company,"MODES") ;
        assertEquals("stateEin",stateEin,ca.getCompanyAgencyPaymentTemplateCollection().getFirst().getAgencyTaxpayerId());
        assertEquals("Current payment method ACHCredit", PaymentMethod.ACHCredit, ca.getCompanyAgencyPaymentTemplateCollection().getFirst().getCurrentPaymentMethod());
        assertNotSame("Current payment method is not ACHDebit", PaymentMethod.ACHDebit, ca.getCompanyAgencyPaymentTemplateCollection().getFirst().getCurrentPaymentMethod());
        assertNotSame("Current payment method is not CheckPayment", PaymentMethod.CheckPayment, ca.getCompanyAgencyPaymentTemplateCollection().getFirst().getCurrentPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
        stateEin = "12-12345-1-1";
        DataLoadServices.updateAgencyTaxpayerId(company,"MO-MODES-PAYMENT",stateEin);
        PayrollServices.beginUnitOfWork();
        ca=  CompanyAgency.findCompanyAgency(company,"MODES") ;
        assertEquals("stateEin",stateEin,ca.getCompanyAgencyPaymentTemplateCollection().getFirst().getAgencyTaxpayerId());
        assertNotSame("Current payment method is not ACHCredit", PaymentMethod.ACHCredit, ca.getCompanyAgencyPaymentTemplateCollection().getFirst().getCurrentPaymentMethod());
        assertNotSame("Current payment method is not ACHDebit", PaymentMethod.ACHDebit, ca.getCompanyAgencyPaymentTemplateCollection().getFirst().getCurrentPaymentMethod());
        assertEquals("Current payment method is CheckPayment", PaymentMethod.CheckPayment, ca.getCompanyAgencyPaymentTemplateCollection().getFirst().getCurrentPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
        stateEin = "12-12345-1-13";
        DataLoadServices.updateAgencyTaxpayerId(company,"MO-MODES-PAYMENT",stateEin);
        PayrollServices.beginUnitOfWork();
        ca=  CompanyAgency.findCompanyAgency(company,"MODES") ;
        assertEquals("stateEin",stateEin,ca.getCompanyAgencyPaymentTemplateCollection().getFirst().getAgencyTaxpayerId());
        assertEquals("Current payment method ACHCredit", PaymentMethod.ACHCredit, ca.getCompanyAgencyPaymentTemplateCollection().getFirst().getCurrentPaymentMethod());
        assertNotSame("Current payment method is not CheckPayment", PaymentMethod.CheckPayment, ca.getCompanyAgencyPaymentTemplateCollection().getFirst().getCurrentPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

         fedEin = "122456789";
         stateEin = "123456-1-123-1234";
         psid = "10654126";

        Company company2 = DataLoadServices.createAssistedCompany("MO",psid,fedEin,stateEin);
        PayrollServices.beginUnitOfWork();
        CompanyAgency ca2=  CompanyAgency.findCompanyAgency(company2,"MODES") ;
        assertEquals("stateEin",stateEin,ca2.getCompanyAgencyPaymentTemplateCollection().getFirst().getAgencyTaxpayerId());
        assertNotSame("Current payment method is not ACHCredit",PaymentMethod.ACHCredit,ca2.getCompanyAgencyPaymentTemplateCollection().getFirst().getCurrentPaymentMethod());
        assertEquals("Current payment method is CheckPayment", PaymentMethod.CheckPayment, ca2.getCompanyAgencyPaymentTemplateCollection().getFirst().getCurrentPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testSuiVT() {

        String fedEin = "4444-55555";
        String stateEin = "333 4444";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "VT", PaymentTemplateCategory.SUI);

        SpcfCalendar endDate = CalendarUtils.getLastDayOfQuarter(entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodEnd());
        String expected = VT_UI_TXP_PREFIX + "019*3334444*01111*" + endDate.format(YYMMDD_DATE_FORMAT) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8) + "*0000000000000000\\*"+StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 8);
        String txpRecord = entryDetailRecord.getTxpRecordData();
        assertEquals("VT TXP output does not match expected output", expected, txpRecord);
    }
    @Test
    public void testSuiGA() {

        String fedEin = "4444-55555";
        String stateEin = "164759-36";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(fedEin, stateEin, "GA", PaymentTemplateCategory.SUI);

        SpcfCalendar endDate = CalendarUtils.getLastDayOfQuarter(entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodEnd());
        String expected = TXP_PREFIX + "16475936*13000*" + String.valueOf(getQuarter(endDate.getMonth())) +"*"+ String.valueOf(endDate.getYear())+ "*"+
                StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), 9) + "*CRI                 \\                     ";
        String txpRecord = entryDetailRecord.getTxpRecordData();
        assertEquals("GA TXP output does not match expected output", expected, txpRecord);
        System.out.println(txpRecord);
    }

    public int getQuarter(int month ){

        if(month>=1 && month <=3){

            return 1;
        }
        else if(month>=4 && month <=6){

            return 2;

        }
        else if(month>=7 && month <=9){

            return 3;

        }

        else if(month>=10 && month <=12){

            return 4;

        }

        return 0;
    }

}
