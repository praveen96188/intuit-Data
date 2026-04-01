package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_ME_WH;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.AgencyIdDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.NACHAStringEncoder;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;

/**
 * Tests for state withholdings
 */
public class StateTxpWHTests {
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
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
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
    public void testDataLoaderMethod() {
        String[] statesList = new String[]{"AL", "MA", "NC", "ND", "PA", "SC", "UT"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 10, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.updateAgencyTaxpayerId(company,"SC-WH1601-PAYMENT","123456783");
            DataLoadServices.runPayrollRun(company, statesList);
        }
    }

    @Test
    public void testWithholdingAL() {
        String ein = "222222223";
        String stateEin = "1224567890";
        int dollars = 8;
        String date = "yyMMdd";

        //Test state Id 10 bytes long and deposit frequency is default i.e. Monthly
        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "AL", PaymentTemplateCategory.Withholding);

        String expected = "TXP*IW" + stateEin + "*01106*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("AL TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));
        
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));

        //Test state Id 6 bytes long and deposit frequency is default i.e. Quarterly
        String psid = "123272727";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        stateEin = "A123456789";
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-05-05"), ein, stateEin, "AL", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*IW" + stateEin + "*01101*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("AL TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

    }


    @Test
    public void testWithholdingAR() {
        String ein = "222222223";
        String stateEin = "12834567-BAC";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "AR", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "12834567" + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("AR TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingAZ() {
        String ein = "258741369";
        String stateEin = "258741369";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "AZ", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "258741369" + "*01101*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("AZ TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        assertEquals("ACH Credit MMT Payment Frequency payment template", "AZ-A1-PAYMENT", entryDetailRecord.getMoneyMovementTransaction().getPaymentFrequency().getPaymentTemplate().getPaymentTemplateCd());
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testWithholdingCA() {
        String ein = "222222223";
        String stateEin = "122-4567-8";
        int dollars = 8;
        String date = "yyMMdd";
        String psid = "123272727";

        StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "CA", PaymentTemplateCategory.Withholding);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "CA-PITSDI-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHCredit)));
        EntryDetailRecord entryDetailRecord = moneyMovementTransaction.getEntryDetailRecordCollection().findEntity(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit));
        PayrollServices.rollbackUnitOfWork();

        String expected = "TXP*" + "12245678" + "*01104*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("134.00"), dollars) + "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("12.00"), dollars) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*000000*\\";
        assertEquals("CA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 28, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.MONTHLY, PSPDate.getPSPTime());
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-05-05"), ein, stateEin, "CA", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + "12245678" + "*01101*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("134.00"), dollars) + "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("12.00"), dollars) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*000000*\\";
        assertEquals("CA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 6, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 6, 14, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.NEXTBANKINGDAY, PSPDate.getPSPTime());
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-08-05"), ein, stateEin, "CA", DepositFrequencyCode.NEXTBANKINGDAY, SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + "12245678" + "*01102*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("134.00"), dollars) + "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("12.00"), dollars) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*000000*\\";
        assertEquals("CA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 8, 8, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 8, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, PSPDate.getPSPTime());
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-11-05"), ein, stateEin, "CA", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 10, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + "12245678" + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("134.00"), dollars) + "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("12.00"), dollars) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*000000*\\";
        assertEquals("CA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

    }

    @Test
    public void testWithholdingCO() {
        String ein = "222222223";
        String stateEin = "122456789012345";
        String stateEftNumber = "1224567890";
        int dollars = 8;
        String date = "yyMMdd";

        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("CO-DR1094-PAYMENT", "State EFT Number", stateEftNumber);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "CO", agencyIdDTO, PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEftNumber + "*011  *" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("CO TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingCT() {
        String ein = "222222223";
        String stateEin = "12245678-001";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "CT", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "12245678001" + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("CT TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingDC() {
        String ein = "22222-2223";
        String stateEin = "300000023166";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "DC", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + ein + "*00300*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*****" + stateEin + "*\\";
        assertEquals("DC TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingDE() {
        String ein = "22-2222223";
        String stateEin = "1222222223123";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "DE", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "1222222223123" + "*01106*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("DE TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingGA() {
        String ein = "222222223";
        //EDR REcord with cast stateEIN to uppercase refer PSP-4169
        String stateEin = "1234567-AB";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "GA", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEin + "*011*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("GA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingHI_NewFormat() {
        String ein = "222222223";
        String stateEin = "WH-333-333-4444-22";
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "HI", PaymentTemplateCategory.Withholding);

        String expected = "TXP*00000000*01130*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*" + StateTxpTestsHelper.getBegin(entryDetailRecord, date) + "*" + "WH3333334444" + "*22*N*\\";
        assertEquals("HI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingIA_ein() {
        String ein = "451254789";
        String stateEin = "45-1254789001";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "IA", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "000" + ein + "001" + "*00011*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("IA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingIA() {
        String ein = "222222223";
        String stateEin = "22-2222223012";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "IA", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "000" + "222222223012" + "*00011*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("IA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    /* Test case is no longer required because State agency ID is available now
    @Test
    public void testWithholdingIA_stateIdNULL() {
        String ein = "22-2222223";
        String stateEin = null;
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "IA", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "000" + "222222223" + "001" + "*00011*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("IA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }*/


    @Test
    public void testWithholdingID() {
        String ein = "222222223";
        String stateEin = "001234567-890";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "ID", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "001234567890" + "*01109*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("ID TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingIL() {
        String ein = "222222223";
        String stateEin = "2222222230126";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "IL", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "2222222230126" + "*0112 *" + StateTxpTestsHelper.getQuarterEndDate(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("IL TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

    }

    @Test
    public void testWithholdingIL_checkDigit() {
        String ein = "222222223";
        String stateEin = "222222223012";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "IL", PaymentTemplateCategory.Withholding);

        String expectedCheckDigit = "6";
        String expected = "TXP*" + "222222223012" + expectedCheckDigit + "*0112 *" + StateTxpTestsHelper.getQuarterEndDate(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("IL TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

    }

    @Ignore("IN is not supported")
    @Test
    public void testWithholdingIN() {
        String ein = "222222223";
        String stateEin = "362603598000";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "IN", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEin + "8" + "*WTH*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("IN TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingKS() {
        String ein = "222222345";
        String stateEin = "036222222345F01";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "KS", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEin + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*P*0***\\";
        assertEquals("KS TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 1, 25, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));



        String psid = "123272727";
        stateEin = "036222222345F99";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-06-09"), ein, stateEin, "KS", null, SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + stateEin + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars,false) + "*P*0***\\";
        assertEquals("KS TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

    }


    @Test
    public void testWithholdingKY() {
        String ein = "222222223";
        String stateEin = "123456abc789";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "KY", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEin + "*011*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*P*0000000000*I*0000000000*      \\";
        assertEquals("KY TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingLA() {
        String ein = "222222223";
        String stateEin = "1234567-123";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "LA",PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "1234567123" + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("LA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingMA() {
        String ein = "222222223";
        String stateEin = "989999999";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MA",PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        String expected = "TXP*" + ein + "      *0166M*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("MA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone());
        
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123272727";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-05-05"), ein, stateEin, "MA", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        expected = "TXP*" + ein + "      *0166Q*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("MA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-07-05"), ein, stateEin, "MA", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()), 2, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        SpcfCalendar endOfQuarter = CalendarUtils.getLastDayOfQuarter(entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodEnd());
        expected = "TXP*" + ein + "      *0166D*" + endOfQuarter.format(date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("MA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

    }
    @Test
    public void testWithholdingMAWithNullStateEIN() {
        String ein = "222222223";
        String stateEin = "";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MA",PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        String expected = "TXP*" + ein + "      *0166M*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("MA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));
        
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123272727";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-05-05"), ein, stateEin, "MA", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        expected = "TXP*" + ein + "      *0166Q*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("MA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-07-05"), ein, stateEin, "MA", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()), 2, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        SpcfCalendar endOfQuarter = CalendarUtils.getLastDayOfQuarter(entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodEnd());
        expected = "TXP*" + ein + "      *0166D*" + endOfQuarter.format(date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("MA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

    }
    @Test
    public void testWithholdingMAAnnual() {
        String ein = "22222-2223";
        String stateEin = "122456789012345";
        int dollars = 8;
        String date = "yyMMdd";

        String psid = "123272727";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MA", PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        String expected = "TXP*" + "222222223" + "      *0166M*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("MA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));
        
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2012-02-08"), ein, stateEin, "MA", DepositFrequencyCode.ANNUAL, SpcfCalendar.createInstance(2012, 2, 5, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        expected = "TXP*" + "222222223" + "      *0166A*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("MA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testWithholdingMD() {
        String ein = "222222223";
        String stateEin = "12245678";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MD", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEin + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("MD TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingME() {
        String ein = "222222223";
        String stateEin = "22-222222301";
        String stateEinInTxpRecords = Txp_ME_WH.getTaxIDForEPaymentTxpRecord(stateEin);
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "ME", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEinInTxpRecords + "*01104*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("ME TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        String psid = "123272727";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-04-08"), ein, stateEin, "ME", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 4, 5, SpcfTimeZone.getLocalTimeZone()), 2, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + stateEinInTxpRecords + "*01104*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("ME TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingME_ein() {
        String ein = "785641238";
        String stateEin = "78-564123812";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "ME", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + ein + "12" + "*01104*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("ME TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    /* Test case is no longer required because State agency ID is available now
    @Test
    public void testWithholdingME_StateIdNull() {
        String ein = "222222223";
        String stateEin = null;
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "ME", PaymentTemplateCategory.Withholding );

        String expected = "TXP*" + ein + "00" + "*01104*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("ME TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }*/

    @Test
    public void testWithholdingMI_stateTaxId() {
        String ein = "222222223";
        //String stateEin = "123456789";
        String stateEin = "22-2222223";

        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MI", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "22-2222223" + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, true) + "****\\";
        assertEquals("MI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingMISemiWeekly() {
        String ein = "222222223";
        String stateEin = "22-2222223";
        int dollars = 8;
        String date = "yyMMdd";
        String psid = "123272727";
        DataLoadServices.setupCompany(psid);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.enrollEFTPS(company);
       PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = Application.findById(PaymentTemplate.class, "MI-MW106-PAYMENT");
        paymentTemplate.setDefaultDepositFrequency(DepositFrequencyCode.SEMIWEEKLY.toString());
        PayrollServices.commitUnitOfWork();
       EntryDetailRecord entryDetailRecord =StateTxpTestsHelper. createEntryDetailRecord(psid, new DateDTO("2011-01-07"), ein, stateEin, "MI", 1, null,  PaymentTemplateCategory.Withholding);


        String expected = "TXP*" + "22-2222223" + "*01100*" + StateTxpTestsHelper.getMonthEndDate(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, true) + "****\\";
        assertEquals("MI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }
    @Test
    public void testWithholdingMINextBankingDay() {
        {
            String ein = "222222223";

            String stateEin = "22-2222223";

            int dollars = 8;
            String date = "yyMMdd";

            String psid = "123272727";
            DataLoadServices.setupCompany(psid);
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.enrollEFTPS(company);
            PayrollServices.beginUnitOfWork();
            PaymentTemplate paymentTemplate = Application.findById(PaymentTemplate.class, "MI-MW106-PAYMENT");
            paymentTemplate.setDefaultDepositFrequency(DepositFrequencyCode.NEXTBANKINGDAY.toString());
            PayrollServices.commitUnitOfWork();
            EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-01-07"), ein, stateEin, "MI", 1, null, PaymentTemplateCategory.Withholding);


            String expected = "TXP*" + "22-2222223" + "*01100*" + StateTxpTestsHelper.getMonthEndDate(entryDetailRecord, date) +
                    "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, true) + "****\\";
            assertEquals("MI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        }
    }
    @Test
    public void testWithholdingMI_EIN() {
        String ein = "12-5456789";
        String stateEin = "12-5456789";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MI", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEin + "*01100*" + StateTxpTestsHelper.getMonthEndDate(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, true) + "****\\";
        assertEquals("MI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingMN() {
        String ein = "222222223";
        String stateEin = "1224567";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MN",PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEin + "*004*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("MN TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingMO() {
        String ein = "222222223";
        String stateEin = "12245678";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MO", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "12245678" + "*0115A*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*T*0\\";
        assertEquals("MO TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123272727";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-05-08"), ein, stateEin, "MO", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 5, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + "12245678" + "*0115A*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*T*0\\";
        assertEquals("MO TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 5, 12, SpcfTimeZone.getLocalTimeZone()));
        
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-08-08"), ein, stateEin, "MO", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 7, 5, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + "12245678" + "*0115P*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*T*0\\";
        assertEquals("MO TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));
        
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 8, 18, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2012-01-08"), ein, stateEin, "MO", DepositFrequencyCode.ANNUAL, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + "12245678" + "*0115A*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*T*0\\";
        assertEquals("MO TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

    }

    @Test
    public void testWithholdingMT() {
        String ein = "222222223";
        String stateEin = "1234567890WTH";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MT", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "1234567890WTH" + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("MT TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingMT_StateIdWithoutWTH() {
        String ein = "222222223";
        String stateEin = "1224567890";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MT", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "1224567890WTH" + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("MT TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingMT_StateIdWithDashes() {
        String ein = "222222223";
        String stateEin = "1234567-890-WTH";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MT", PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);

        String expected = "TXP*" + "1234567890WTH" + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("MT TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        // Verifying for 6th record (Addenda record)
        expected = "622092900383DOR156041200221  00000056001234567890WTH  "+ StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(entryDetailRecord.getCompany().getLegalName()), 22) + "  1";
        assertEquals("MT Addenda output does not match expected output", expected, entryDetailRecord.getRecordData());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testWithholdingNC() {
        String ein = "222222223";
        String stateEin = "122456789";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NC", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "122456789  " + "*01102*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("NC TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));
        
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123272727";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-05-08"), ein, stateEin, "NC", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 4, 5, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + "122456789  " + "*01101*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("NC TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 5, 13, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 5, 12, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-08-08"), ein, stateEin, "NC", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 7, 5, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + "122456789  " + "*01103*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("NC TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

    }

    @Test
    public void testWithholdingND() {
        String ein = "122456789";
        String stateEin = "12245678901";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "ND",PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + ein + "01" + "*011  *" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("ND TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingNE_ZeroPadding() {
        String ein = "222222223";
        String stateEin = "21-34-56";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NE", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "21000003456" + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("NE TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingNE() {
        String ein = "222222223";
        String stateEin = "21-34-56789012";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NE", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "21345678901" + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("NE TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingNJ() {
        String ein = "369456218";
        String stateEin = "369456218/134";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NJ", PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        String expected = "TXP*B369456218134" + "*01170*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 4).toUpperCase() + "\\";
        assertEquals("NJ TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        // offload payments
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 1, 12, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123272727";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-05-08"), ein, stateEin, "NJ", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        expected = "TXP*B369456218134" + "*01120*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 4).toUpperCase() + "\\";
        assertEquals("NJ TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 5, 12, SpcfTimeZone.getLocalTimeZone()));
        
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 6, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-08-08"), ein, stateEin, "NJ", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        expected = "TXP*B369456218134" + "*01130*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 4).toUpperCase() + "\\";
        assertEquals("NJ TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testWithholdingNJ_EIN() {
        String ein = "369456218";
        String stateEin = "369456218/000";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NJ", PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        String expected = "TXP*B" + ein + "000" + "*01170*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 4).toUpperCase() + "\\";
        assertEquals("NJ TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();
    }

    /* Test case is no longer required because State agency ID is available now
    @Test
    public void testWithholdingNJ_StateIdNull() {
        String ein = "222222223";
        String stateEin = null;
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NJ", PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        String expected = "TXP*B" + ein + "000" + "*01170*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 4).toUpperCase() + "\\";
        assertEquals("NJ TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();
    }*/

    @Test
    public void testWithholdingNM() {
        String ein = "222222223";
        String stateEin = "12-123456-12-1";
        int dollars = 8;
        String date = "yyyyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NM",PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "12123456121" + "*011*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("NM TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingNY() {
        String ein = "258963147";
        String stateEin = "258963147 0";
        int dollars = 8;
        String date = "yyMMdd";
        String stateAccessCode = "122456";

        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NY", agencyIdDTO, PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + ein + "*WT*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*S*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("72.00"), dollars, false) + "*C*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("108.00"), dollars, false) + "*L*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("226.00"), dollars, false) + "*" + stateAccessCode + "\\";
        assertEquals("NY TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        String psid = "123272727";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        List<Employee> employees = Arrays.asList(Employee.findEmployees(company).toArray(new Employee[]{}));
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-04-07"), employees, new String[]{"61", "62", "63", "64", "1", "65", "54"}, new String[]{"6.1", "6.2", "6.3", "6.4", "25", "6.5", "5.4"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        DomainEntitySet<FinancialTransaction> financialTransactions = processResult.getResult().getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit).find(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH));

        assertTrue("State ACH agency tax credit FTs", financialTransactions.size() > 0);

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class, EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCDPlus)
                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).And(EntryDetailRecord.MoneyMovementTransaction().equalTo(financialTransactions.get(0).getMoneyMovementTransaction())));
        assertEquals("State ACH credit payment entry detail", 1, entryDetailRecords.size());
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        expected = "TXP*" + ein + "*WT*" + StateTxpTestsHelper.getEnd(entryDetailRecords.get(0), date) +
                "***C*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("10.80"), dollars, false) + "***" + stateAccessCode + "\\";
        assertEquals("NY TXP output does not match expected output", expected, entryDetailRecords.get(0).getTxpRecordData());

    }

    //Test for NY-1MN-PAYMENT with StateAccessCode null.

    @Test
    public void  testWithholdingNYNoAccessToken(){
        String ein = "222222223";
        String stateEin = "122456789 0";
        int dollars = 8;
        String date = "yyMMdd";
        String stateAccessCode = "";
       // String stateAccessCode = null;

        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NY", agencyIdDTO, PaymentTemplateCategory.Withholding);
        assertNull("EDR should be null",entryDetailRecord);
       /* String expected = "TXP*" + "122456789" + "*WT*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*S*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("72.00"), dollars, false) + "*C*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("108.00"), dollars, false) + "*L*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("226.00"), dollars, false) + "*999999" + "\\";
        assertEquals("NY TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        */
    }

    //Test for NY-1MN-PAYMENT with State Access Code is missing. When State Access Code entered ACH credit is entered  should be automatically set to true.

    @Test
    public void  testWithholdingNY_SetACHCredit(){
        String ein = "258963147";
        String stateEin = "258963147 0";
        int dollars = 8;
        String date = "yyMMdd";
        String stateAccessCode = null;

        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NY", agencyIdDTO, PaymentTemplateCategory.Withholding);

        //Set stateAccessCode then run unit test.
        stateAccessCode = "12457";
        String psid = "123272727";

        agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-01-07"), ein, stateEin, "NY",1, agencyIdDTO, PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + ein + "*WT*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*S*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("144.00"), dollars, false) + "*C*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("216.00"), dollars, false) + "*L*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("452.00"), dollars, false) + "*" + stateAccessCode + "\\";
        assertEquals("NY TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void  testWithholdingNY_SetACHCredit_ChangeAgencyId(){
        String ein = "258963147";
        String stateEin = "258963147 0";
        int dollars = 8;
        String date = "yyMMdd";
        String stateAccessCode = "12457";

        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NY", agencyIdDTO, PaymentTemplateCategory.Withholding);

        //Set stateAccessCode then run unit test.

        stateEin = "258963147 0";
        String psid = "123272727";

        agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-01-07"), ein, stateEin, "NY",1, agencyIdDTO, PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + ein + "*WT*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*S*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("144.00"), dollars, false) + "*C*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("216.00"), dollars, false) + "*L*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("452.00"), dollars, false) + "*" + stateAccessCode + "\\";
        assertEquals("NY TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingNY_Metro() {
        String ein = "258963147";
        String stateEin = "258963147 0";
        int dollars = 8;
        String date = "yyMMdd";
        String stateAccessCode = "122456";

        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-MTA305-PAYMENT", "State Access Code", stateAccessCode);

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NY", 2, agencyIdDTO, PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + ein + "*MT*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*M*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("0.00"), dollars, true) + "*T*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("846.00"), dollars, true) + "*" + stateAccessCode + "\\";
        assertEquals("NY TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

    }

    @Test
    public void testStatePartialRecall() {
        String ein = "258963147";
        String stateEin = "258963147 0";
        int dollars = 8;
        String date = "yyMMdd";
        String stateAccessCode = "122456";

        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NY", agencyIdDTO, PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + ein + "*WT*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*S*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("72.00"), dollars, false) + "*C*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("108.00"), dollars, false) + "*L*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("226.00"), dollars, false) + "*" + stateAccessCode + "\\";
        assertEquals("NY TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        String psid = "123272727";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        List<Employee> employees = Arrays.asList(Employee.findEmployees(company).toArray(new Employee[]{}));
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-04-07"), employees, new String[]{"61", "62", "63", "64", "1", "65", "54"}, new String[]{"6.1", "6.2", "6.3", "6.4", "25", "6.5", "5.4"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollRun payrollRun = processResult.getResult();
        String recallPaycheck = payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId();
        DomainEntitySet<FinancialTransaction> financialTransactions = processResult.getResult().getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit).find(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH));

        assertTrue("State ACH agency tax credit FTs", financialTransactions.size() > 0);

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class, EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCDPlus)
                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).And(EntryDetailRecord.MoneyMovementTransaction().equalTo(financialTransactions.get(0).getMoneyMovementTransaction())));
        assertEquals("State ACH credit payment entry detail", 1, entryDetailRecords.size());
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        expected = "TXP*" + ein + "*WT*" + StateTxpTestsHelper.getEnd(entryDetailRecords.get(0), date) +
                "***C*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("10.80"), dollars, false) + "***" + stateAccessCode + "\\";
        assertEquals("NY TXP output does not match expected output", expected, entryDetailRecords.get(0).getTxpRecordData());


        //Recall one paycheck from the payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(recallPaycheck);
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

    }

    @Test
    public void testStatePartialRecallForMMTWithMoreThanOneFT() {
        String ein = "258963147";
        String stateEin = "258963147 0";
        int dollars = 8;
        String date = "yyMMdd";
        String stateAccessCode = "122456";

        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "NY", agencyIdDTO, PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + ein + "*WT*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*S*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("72.00"), dollars, false) + "*C*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("108.00"), dollars, false) + "*L*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("226.00"), dollars, false) + "*" + stateAccessCode + "\\";
        assertEquals("NY TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        String psid = "123272727";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        List<Employee> employees = Arrays.asList(Employee.findEmployees(company).toArray(new Employee[]{}));
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-04-07"), employees, new String[]{"61", "62", "63", "64", "1", "65", "54"}, new String[]{"6.1", "6.2", "6.3", "6.4", "25", "6.5", "5.4"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollRun payrollRun = processResult.getResult();
        String recallPaycheck = payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId();
        DomainEntitySet<FinancialTransaction> financialTransactions = processResult.getResult().getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit).find(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH));

        assertTrue("State ACH agency tax credit FTs", financialTransactions.size() > 0);

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class, EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCDPlus)
                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).And(EntryDetailRecord.MoneyMovementTransaction().equalTo(financialTransactions.get(0).getMoneyMovementTransaction())));
        assertEquals("State ACH credit payment entry detail", 1, entryDetailRecords.size());
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        expected = "TXP*" + ein + "*WT*" + StateTxpTestsHelper.getEnd(entryDetailRecords.get(0), date) +
                "***C*" + StateTxpTestsHelper.formatAmount(new SpcfMoney("10.80"), dollars, false) + "***" + stateAccessCode + "\\";
        assertEquals("NY TXP output does not match expected output", expected, entryDetailRecords.get(0).getTxpRecordData());

        // Submit another payroll for the same period
        PayrollServices.beginUnitOfWork();
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-04-07"), employees, new String[]{"61", "62", "63", "64", "1", "65", "54"}, new String[]{"6.1", "6.2", "6.3", "6.4", "25", "6.5", "5.4"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Recall one paycheck from the payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        //paycheckList.add(recallPaycheck);
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);
    }

    @Test
    public void testWithholdingOH() {
        String ein = "222222223";
        String stateEin = "122456789";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "OH", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEin + "*01101*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("OH TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingOH_CheckDigit() {
        String ein = "222222223";
        String stateEin = "12245678";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "OH", PaymentTemplateCategory.Withholding);
        //Check digit for 12245678 is 3

        String expected = "TXP*" + "12245678" + "3" + "*01101*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("OH TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingOK() {
        String ein = "222222223";
        String stateEin = "WTH-11144460-02";
        int dollars = 8;
        String date = "yyyyMMdd";                                                                  //   TXP*G*WTH*F999999*01100*110131*T*101215*7600\
        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "OK", PaymentTemplateCategory.Withholding);

        String expected = "TXP*G*WTH*F*" + ein + "*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) + "*RTNPYM*" + PSPDate.getPSPTime().format("yyyyMMdd") + "*"
                + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("OK TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingOR() {
        String ein = "222222223";
        String stateEin = "1234567-1";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "OR", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "012345671" + "*01101*" + StateTxpTestsHelper.getQuarterEndDate(entryDetailRecord, date) +
                "*S*" + "0" + "*S*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) +
                "*S*" + "0" + "\\";
        assertEquals("OR TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }


    @Test
    public void testWithholdingPA() {
        String ein = "222222223";
        String stateEin = "12245678";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "PA", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "12245678" + "*EM340*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("PA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingRI() {
        String ein = "159357852";
        String stateEin = "159357852";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "RI", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEin + "00" + "*01103*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("RI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));
        
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 2, 21, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123272727";

        stateEin = "159357852";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-05-08"), ein, stateEin, "RI", DepositFrequencyCode.NEXTBANKINGDAY, SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + stateEin + "00*01101*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("RI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 5, 9, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 5, 12, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-08-08"), ein, stateEin, "RI", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()), 1, null, PaymentTemplateCategory.Withholding, new SpcfMoney("1.00"));

        expected = "TXP*" + stateEin + "00*01104*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("RI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));
        
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-11-08"), ein, stateEin, "RI", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 10, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding );

        expected = "TXP*" + stateEin + "00*01102*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("RI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 11, 10, SpcfTimeZone.getLocalTimeZone()));
        
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 11, 18, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2012, 1, 30, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 2, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2012-02-08"), ein, stateEin, "RI", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()), 1, null, PaymentTemplateCategory.Withholding, new SpcfMoney("1.00"));

        expected = "TXP*" + stateEin + "00*01104*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("RI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

    }

    @Test
    public void testWithholdingRI_EIN() {
        String ein = "159357852";
        String stateEin = "159357852";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "RI", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + ein + "00*01103*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("RI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    /* Test case is no longer required because State agency ID is available now
    @Test
    public void testWithholdingRI_StateIdNull() {
        String ein = "222222223";
        String stateEin = null;
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "RI", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + ein + "00*01103*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("RI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }*/

    @Test
    public void testWithholdingSC() {
        String ein = "222222223";
        String stateEin = "12245678-9";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "SC", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "122456789" + "*10811*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****\\";
        assertEquals("SC TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }
    @Test
    public void testWithholdingSCWithNewFormat() {
        String ein = "222222221";
        String stateEin = "112345678";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "SC", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "112345678" + "*10811*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****\\";
        assertEquals("SC TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingUT() {
        String ein = "222222223";
        String stateEin = "12245678901";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "UT", PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        String expected = "TXP*" + stateEin + "*0110*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("UT TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testWithholdingVA() {
        String ein = "200328637";
        String stateEin = "30-200328637F-001";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "VA", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "30200328637F001" + "*00011*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "*****\\";
        assertEquals("VA TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingVTWithNewFormat() {
        String ein = "222222221";
        String stateEin = "WHT-12134576";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "VT", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "WHT12134576" + "*01101*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("VT TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));
        
        //21/02 is holiday
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 2, 23, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123272727";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-05-08"), ein, stateEin, "VT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + "WHT12134576" + "*01101*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("VT TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 5, 12, SpcfTimeZone.getLocalTimeZone()));
        
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 7, 25, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-08-08"), ein, stateEin, "VT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        expected = "TXP*" + "WHT12134576" + "*01100*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("VT TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());



    }

    @Test
    public void testWithholdingWI() {
        String ein = "222222223";
        String stateEin = "036-1234567890-01";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "WI", PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        String expected = "TXP*" + "036123456789001" + "*01102*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("WI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));
        
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123272727";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-05-08"), ein, stateEin, "WI", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        expected = "TXP*" + "036123456789001" + "*01103*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("WI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 5, 12, SpcfTimeZone.getLocalTimeZone()));
        
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 6, 30, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2011-08-08"), ein, stateEin, "WI", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        expected = "TXP*" + "036123456789001" + "*01104*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("WI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        // offload payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));
        
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(psid, new DateDTO("2012-01-08"), ein, stateEin, "WI", DepositFrequencyCode.ANNUAL, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()), 1, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        Application.refresh(entryDetailRecord);
        expected = "TXP*" + "036123456789001" + "*01105*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "*****" + entryDetailRecord.getCompany().getLegalName().substring(0, 6).toUpperCase() + "\\";
        assertEquals("WI TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testWithholdingWV() {
        String ein = "222222223";
        String stateEin = "12245678";
        int dollars = 8;
        String date = "yyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "WV", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + stateEin + "*01170*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars) + "\\";
        assertEquals("WV TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }

    @Test
    public void testWithholdingMS() {
        String ein = "222222223";
        String stateEin = "1224-8765";
        int dollars = 8;
        String date = "yyyyMMdd";

        EntryDetailRecord entryDetailRecord = StateTxpTestsHelper.createEntryDetailRecord(ein, stateEin, "MS", PaymentTemplateCategory.Withholding);

        String expected = "TXP*" + "1224-8765" + "*01101*" + StateTxpTestsHelper.getEnd(entryDetailRecord, date) +
                "*T*" + StateTxpTestsHelper.formatAmount(entryDetailRecord.getAmount(), dollars, false) + "\\";
        assertEquals("MS TXP output does not match expected output", expected, entryDetailRecord.getTxpRecordData());
    }
}
