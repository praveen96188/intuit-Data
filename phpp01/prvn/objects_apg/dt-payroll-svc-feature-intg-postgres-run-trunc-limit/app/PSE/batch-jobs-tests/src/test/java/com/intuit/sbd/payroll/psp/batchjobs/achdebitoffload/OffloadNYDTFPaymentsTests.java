package com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload;

import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileInputStream;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.collections.SpcfPair;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ankit on 5/12/2015.
 */
public class OffloadNYDTFPaymentsTests {

    public static final Logger logger = Logger.getLogger("OffloadNYDTFPaymentsTests.class");

    public static final String NY_DTF_HEADER_RECORD = "1HDR000000          010111                      000WT-1   880146711003" +
            "Computing Resources Inc                 6884 Sierra Center Parkway    Reno                     NV89511                                                                                                           ";

    public static final String NY_DTF_HASH_RECORD = "HASH TOTAL 0000000000000  000000" +
            "                                                                                                                                                                                                                                                       ";
    public static final String NY_DTF_TRAILER_RECORD = "1EOF 0000001" +
            "                                                                                                                                                                                                                                                                           ";
    public static final String NY_DTF_DATA_RECORD = "122456789  8                       010711000000000072000000001080000000022600                                 000000000000000004060000000040600      00                                                                                                                                ";

    public static final String NY_1MN_PAYMENT = "NY-1MN-PAYMENT";

    public PaymentTemplate paymentTemplate = Application.findById(PaymentTemplate.class, NY_1MN_PAYMENT);

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", PSPDate.getPSPTime());
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testNYDTFHeaderRecord() throws IOException {
        StringBuilder builder = new StringBuilder();
        (new OffloadNYDTFPayments()).writeHeaderRecord(builder);
        String generatedRecord = builder.toString();
        logger.info(generatedRecord);
        assertEquals("Record size is not correct", 279, generatedRecord.length());
        assertEquals("Header record is not correct", NY_DTF_HEADER_RECORD, builder.toString());
    }

    @Test
    public void testNYDTFHashRecord() throws IOException {
        StringBuilder builder = new StringBuilder();
        OffloadNYDTFPayments offloadNYDTFPayments = new OffloadNYDTFPayments();
        OffloadNYDTFPayments.NY_DTF_DTO dto = offloadNYDTFPayments.new NY_DTF_DTO();
        offloadNYDTFPayments.writeHashRecord(builder, dto);
        String generatedRecord = builder.toString();
        logger.info(generatedRecord);
        assertEquals("Record size is not correct", 279, generatedRecord.length());
        assertEquals("Hash record is not correct", NY_DTF_HASH_RECORD, builder.toString());
    }

    @Test
    public void testNYDTFTrailerRecord() throws IOException {
        StringBuilder builder = new StringBuilder();
        OffloadNYDTFPayments offloadNYDTFPayments = new OffloadNYDTFPayments();
        OffloadNYDTFPayments.NY_DTF_DTO dto = offloadNYDTFPayments.new NY_DTF_DTO();
        offloadNYDTFPayments.writeTrailerRecord(builder, dto);
        String generatedRecord = builder.toString();
        logger.info(generatedRecord);
        assertEquals("Record size is not correct", 279, generatedRecord.length());
        assertEquals("Trailer record is not correct", NY_DTF_TRAILER_RECORD, builder.toString());
    }

    @Test
    public void testNYDTFDataRecord() throws IOException {
        StringBuilder builder = new StringBuilder();
        String[] statesList = new String[]{"NY"};
        String psid = "199210091";
        SpcfDecimal totalRemittancePaid = SpcfDecimal.createInstance("0.00");
        //Company company = StateReportTests.createAssistedCompanyWithRates("NY", psid, "987654321", "767634539 8", statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("122456789 8", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);


        //StateReportTests.setStatePaymentsToSent();

        SpcfCalendar initiationDate = SpcfCalendar.createInstance(2011, 4, 28, SpcfTimeZone.getLocalTimeZone());


        Application.beginUnitOfWork();

        DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class,
                                                                             PaymentTemplate.PaymentTemplateCd().equalTo("NY-1MN-PAYMENT"));
        PaymentTemplate paymentTemplate = paymentTemplates.get(0);
        PaymentTemplateFrequency paymentTemplateFrequency = paymentTemplate.getPaymentTemplateFrequency(DepositFrequencyCode.QUARTERLY.toString());

        HashMap<Company, ArrayList<MoneyMovementTransaction>> companyToMoneyMovementTransactions =
                OffloadNYDTFPayments.getMoneyMovementTransactionsForInitiationDate(paymentTemplate, initiationDate);
        OffloadNYDTFPayments offloadNYDTFPayments = new OffloadNYDTFPayments();
        OffloadNYDTFPayments.NY_DTF_DTO dto = offloadNYDTFPayments.new NY_DTF_DTO();
        offloadNYDTFPayments.writeDataRecords(builder, companyToMoneyMovementTransactions, dto);

        Application.rollbackUnitOfWork();

        String generatedRecord = builder.toString();
        logger.info(generatedRecord);
        assertEquals("Record size is not correct", 279, generatedRecord.length());
        assertEquals("Data record is not correct", NY_DTF_DATA_RECORD, builder.toString());
    }

    /**
     * Overriding the default method because for NY DTF we need only payments which are ACHDebit
     * Gets all MoneyMovementTransactions for a initiation date, payment frequency and template
     *
     * @param paymentTemplateFrequency The frequency of the state report to run
     * @param initiationDate           initiation date of the payments
     * @return All MoneyMovementTransactions for the initiation date and for a company
     */
    public HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> getMoneyMovementTransactionsForInitiationDate(PaymentTemplateFrequency paymentTemplateFrequency,
                                                                                                                                               SpcfCalendar initiationDate) {
        // Since HQL doesn't allow substr, additional check and removal are done after fetching
        // can move this to native SQL if performance is not acceptable
        // Validated with Shiva, we just ReadyToSend
        SpcfCalendar startDate = initiationDate.copy();
        CalendarUtils.clearTime(startDate);
        SpcfCalendar endDate = startDate.copy();
        endDate.addDays(1);
        endDate.addMilliseconds(-1);

        logger.info("Trying to find MMTs for criteria startDate = " + startDate.format("yyyyMMddHHmmssS") + " endDate = " + endDate.format("yyyyMMddHHmmssS") +
                            " for payment template frequency = " + paymentTemplateFrequency.getPaymentFrequencyId());

        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.InitiationDate().between(startDate, endDate)
                                                                                         .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.ACHDebit))
                                                                                         .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend))
                                                                                         .And(MoneyMovementTransaction.PaymentFrequency().equalTo(paymentTemplateFrequency));

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                                                                                                                                    .OrderBy(MoneyMovementTransaction.PaymentPeriodBegin(), MoneyMovementTransaction.Company().LegalName())
                                                                                                                                    .EagerLoad(MoneyMovementTransaction.Company(), MoneyMovementTransaction.PaymentFrequency()));

        HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions = new
                HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>>();
        HashSet<Company> badCompanies = new HashSet<Company>();
        SpcfCalendar now = PSPDate.getPSPTime();

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            Company theCompany = moneyMovementTransaction.getCompany();
            if (companyToMoneyMovementTransactions.containsKey(theCompany)) {
                companyToMoneyMovementTransactions.get(theCompany).getValueItem().add(moneyMovementTransaction);
            } else {
                SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>> pair = new SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>();
                pair.setKeyItem(paymentTemplateFrequency.getPaymentFrequencyId());
                pair.setValueItem(new ArrayList<MoneyMovementTransaction>());
                pair.getValueItem().add(moneyMovementTransaction);
                companyToMoneyMovementTransactions.put(theCompany, pair);
            }
        }

        return companyToMoneyMovementTransactions;
    }

    @Test
    public void prepareStateAgencyIdTests() {
        HashMap<String, String> rawToProcessedAgencyIdMap = new HashMap<String, String>();
        //Matching pattern + having check digit
        //9 digit
        rawToProcessedAgencyIdMap.put("123456789 6", "123456789  ");
        //11 digit
        rawToProcessedAgencyIdMap.put("12345678901 2", "12345678901");
        //Matching pattern + not having check digit
        //9 digit
        rawToProcessedAgencyIdMap.put("123456789", "123456789  ");
        //11 digit
        rawToProcessedAgencyIdMap.put("12345678901", "12345678901");
        //Not matching pattern - For these id ACHDebit should not be enabled, method should be none
        // so ideally we should not even be calling this method but if we do, the id will be blank filled.
        //size smaller
        rawToProcessedAgencyIdMap.put("12345", "12345      ");
        rawToProcessedAgencyIdMap.put("1234", "1234       ");
        //containing spaces
        rawToProcessedAgencyIdMap.put("12345 6", "123456     ");
        rawToProcessedAgencyIdMap.put("12345  6", "123456     ");
        rawToProcessedAgencyIdMap.put("12345 678", "12345678   ");
        rawToProcessedAgencyIdMap.put(" 12345 678 ", "12345678   ");
        //Containing characters
        rawToProcessedAgencyIdMap.put(" 12345 AB678 ", "12345AB678 ");
        rawToProcessedAgencyIdMap.put("A B12345 678 ", "AB12345678 ");
        //greater than max length
        rawToProcessedAgencyIdMap.put("12345678923 35", "12345678923");

        OffloadNYDTFPayments offloadNYDTFPayments = new OffloadNYDTFPayments();
        MoneyMovementTransaction mmt = new MoneyMovementTransaction();

        for (Map.Entry<String, String> entry : rawToProcessedAgencyIdMap.entrySet()) {
            mmt.setAgencyTaxpayerId(entry.getKey());
            String preparedTaxId = offloadNYDTFPayments.prepareStateAgencyId(mmt, 11);
            assertEquals(11, preparedTaxId.length());
            System.out.println(entry.getKey() + " --> " + preparedTaxId);
            assertEquals("Failed for " + mmt.getAgencyTaxpayerId(), entry.getValue(), preparedTaxId);
        }
    }


    /**
     * Runs a happy path on NY's quarterly StateCouponOutput
     */
    @Test
    public void happyPathNYDTF3BDFilenameTest() throws IOException{
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));

        String psid = "199210091";
        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("12234568901 9", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.THREEBANKINGDAY);
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-07"), false);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NY_1MN_PAYMENT, DepositFrequencyCode.THREEBANKINGDAY);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 9, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs = {
                "1HDR000000          010915                      000WT-1   880146711003Computing Resources Inc                 6884 Sierra Center Parkway    Reno                     NV89511                                                                                                           ",
                "122345689019                       010715000000000072000000001080000000022600                                 000000000000000004060000000040600      00                                                                                                                                ",
                "HASH TOTAL 0000000040600  000001                                                                                                                                                                                                                                                       ",
                "1EOF 0000002                                                                                                                                                                                                                                                                           "
        };

        SpcfCalendar initiationDate = PSPDate.getPSPTime();

        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit,paymentTemplate ));
        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        File fileName = new File(filename);
        //checking if file exists in the archived directory
        Boolean exists=fileName.exists() && !fileName.isDirectory();
        assertEquals("Filename with WT1due date ",true,exists);


    }
    @Test
    public void happyPathNYDTFQuarterly() throws IOException {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));

        String psid = "199210091";
        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("123456789 6", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.QUARTERLY);
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-07"), false);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NY_1MN_PAYMENT, DepositFrequencyCode.QUARTERLY);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 4, 28, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs = {
                "1HDR000000          042815                      000WT-1   880146711003Computing Resources Inc                 6884 Sierra Center Parkway    Reno                     NV89511                                                                                                           ",
                "123456789  6                       010715000000000072000000001080000000022600                                 000000000000000004060000000040600      00                                                                                                                                ",
                "HASH TOTAL 0000000040600  000001                                                                                                                                                                                                                                                       ",
                "1EOF 0000002                                                                                                                                                                                                                                                                           "
        };

        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit,paymentTemplate));
        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        runAssertsOnFilesCreated(filename, expectedOutputs);
    }

    /**
     * Runs a happy path on NY's 5BD StateCouponOutput
     */
    @Test
    public void happyPathNYDTF5BD() throws IOException {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));

        String psid = "199210091";
        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("12234568901", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.FIVEBANKINGDAY);
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-07"), false);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NY_1MN_PAYMENT, DepositFrequencyCode.FIVEBANKINGDAY);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 12, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs = {
                "1HDR000000          011215                      000WT-1   880146711003Computing Resources Inc                 6884 Sierra Center Parkway    Reno                     NV89511                                                                                                           ",
                "122345689018                       010715000000000072000000001080000000022600                                 000000000000000004060000000040600      00                                                                                                                                ",
                "HASH TOTAL 0000000040600  000001                                                                                                                                                                                                                                                       ",
                "1EOF 0000002                                                                                                                                                                                                                                                                           "
        };
        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit,paymentTemplate));
        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        runAssertsOnFilesCreated(filename, expectedOutputs);
    }

    /**
     * Runs a happy path on NY's 3BD StateCouponOutput
     */
    @Test
    public void happyPathNYDTF3BD() throws IOException {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));

        String psid = "199210091";
        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("12234568901 9", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.THREEBANKINGDAY);
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-07"), false);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NY_1MN_PAYMENT, DepositFrequencyCode.THREEBANKINGDAY);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 8, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs = {
                "1HDR000000          010815                      000WT-1   880146711003Computing Resources Inc                 6884 Sierra Center Parkway    Reno                     NV89511                                                                                                           ",
                "122345689019                       010715000000000072000000001080000000022600                                 000000000000000004060000000040600      00                                                                                                                                ",
                "HASH TOTAL 0000000040600  000001                                                                                                                                                                                                                                                       ",
                "1EOF 0000002                                                                                                                                                                                                                                                                           "
        };

        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit,paymentTemplate));
        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        runAssertsOnFilesCreated(filename, expectedOutputs);
    }

    /**
     * Backdated on NY's 5BD backdated StateCouponOutput
     */
    @Test
    public void happyPathNYDTF5BD_backDated() throws IOException {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NY_1MN_PAYMENT, DepositFrequencyCode.FIVEBANKINGDAY);
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));

        String psid = "199210091";
        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("1223456890", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.FIVEBANKINGDAY);
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 12, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-05"), false);

        SpcfCalendar expectedInitiationDate = new SpcfCalendarImpl(2015, 1, 20);
        SpcfCalendar begin = expectedInitiationDate.copy();
        CalendarUtils.clearTime(begin);
        SpcfCalendar end = begin.copy();
        end.addDays(1);
        end.addMilliseconds(-1);
        //Verify MMT created for backdate payroll
        Application.beginUnitOfWork();
        PaymentTemplate paymentTemplate = Application.findById(PaymentTemplate.class, NY_1MN_PAYMENT);
        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.InitiationDate().between(begin, end)
                                                                                         .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.ACHDebit))
                                                                                         .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend))
                                                                                         .And(MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate));

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                                                                                                                                    .OrderBy(MoneyMovementTransaction.PaymentPeriodBegin(), MoneyMovementTransaction.Company().LegalName())
                                                                                                                                    .EagerLoad(MoneyMovementTransaction.Company(), MoneyMovementTransaction.PaymentFrequency()));
        assertEquals(1, moneyMovementTransactions.size());

        Application.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 20, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs = {
                "1HDR000000          012015                      000WT-1   880146711003Computing Resources Inc                 6884 Sierra Center Parkway    Reno                     NV89511                                                                                                           ",
                "1223456890 1                       010515000000000072000000001080000000022600                                 000000000000000004060000000040600      00                                                                                                                                ",
                "HASH TOTAL 0000000040600  000001                                                                                                                                                                                                                                                       ",
                "1EOF 0000002                                                                                                                                                                                                                                                                           "
        };

        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit,paymentTemplate));
        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        runAssertsOnFilesCreated(filename, expectedOutputs);
    }

    /**
     * Runs a happy path on NY 1MN mulitple frequencies
     */
    @Test
    public void happyPathNYDTF_multiple_frequencies() throws IOException {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));

        String psid = "199210091";
        String psid2 = "199210092";
        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("1223456890", company1, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company1);
        DataLoadServices.addEEs(company1, 2);
        DataLoadServices.activateTaxService(company1);
        DataLoadServices.addFederalTaxCompanyLaws(company1);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.FIVEBANKINGDAY);
        DataLoadServices.runPayrollRun(company1, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-07"), false);

        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid2, "987654322", false, ServiceCode.Tax);
        DataLoadServices.addCompanyLawsWithAgencyId("1223456891", company2, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company2);
        DataLoadServices.addEEs(company2, 2);
        DataLoadServices.activateTaxService(company2);
        DataLoadServices.addFederalTaxCompanyLaws(company2);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid2, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid2, NY_1MN_PAYMENT, DepositFrequencyCode.QUARTERLY);
        DataLoadServices.runPayrollRun(company2, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-07"), false);

        Application.beginUnitOfWork();
        SpcfCalendar expectedInitiationDate = new SpcfCalendarImpl(2015, 1, 12);
        SpcfCalendar begin = expectedInitiationDate.copy();
        CalendarUtils.clearTime(begin);
        SpcfCalendar end = begin.copy();
        end.addDays(1);
        end.addMilliseconds(-1);
        PaymentTemplate paymentTemplate = Application.findById(PaymentTemplate.class, NY_1MN_PAYMENT);
        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.InitiationDate().between(begin, end)
                                                                                         .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.ACHDebit))
                                                                                         .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend))
                                                                                         .And(MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate));

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                                                                                                                                    .OrderBy(MoneyMovementTransaction.PaymentPeriodBegin(), MoneyMovementTransaction.Company().LegalName())
                                                                                                                                    .EagerLoad(MoneyMovementTransaction.Company(), MoneyMovementTransaction.PaymentFrequency()));
        assertEquals(1, moneyMovementTransactions.size());

        expectedInitiationDate = new SpcfCalendarImpl(2015, 4, 28);
        begin = expectedInitiationDate.copy();
        CalendarUtils.clearTime(begin);
        end = begin.copy();
        end.addDays(1);
        end.addMilliseconds(-1);
        paymentWhereClause = MoneyMovementTransaction.InitiationDate().between(begin, end)
                                                     .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.ACHDebit))
                                                     .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend))
                                                     .And(MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate));

        moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                     new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                                                                                          .OrderBy(MoneyMovementTransaction.PaymentPeriodBegin(), MoneyMovementTransaction.Company().LegalName())
                                                                                          .EagerLoad(MoneyMovementTransaction.Company(), MoneyMovementTransaction.PaymentFrequency()));
        assertEquals(1, moneyMovementTransactions.size());
        Application.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 12, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NY_1MN_PAYMENT, DepositFrequencyCode.FIVEBANKINGDAY);
        String[] expectedOutputs = {
                "1HDR000000          011215                      000WT-1   880146711003Computing Resources Inc                 6884 Sierra Center Parkway    Reno                     NV89511                                                                                                           ",
                "1223456890 1                       010715000000000072000000001080000000022600                                 000000000000000004060000000040600      00                                                                                                                                ",
                "HASH TOTAL 0000000040600  000001                                                                                                                                                                                                                                                       ",
                "1EOF 0000002                                                                                                                                                                                                                                                                           "
        };
        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit,paymentTemplate));
        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        runAssertsOnFilesCreated(filename, expectedOutputs);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 4, 28, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);
        String[] expectedOutputs2 = {
                "1HDR000000          042815                      000WT-1   880146711003Computing Resources Inc                 6884 Sierra Center Parkway    Reno                     NV89511                                                                                                           ",
                "1223456891 9                       010715000000000072000000001080000000022600                                 000000000000000004060000000040600      00                                                                                                                                ",
                "HASH TOTAL 0000000040600  000001                                                                                                                                                                                                                                                       ",
                "1EOF 0000002                                                                                                                                                                                                                                                                           "
        };
        initiationDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit,paymentTemplate));
        filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        runAssertsOnFilesCreated(filename, expectedOutputs2);
    }

    /**
     * Tests roll-over of frequency from quarterly to 3BD
     */
    @Test
    public void happyPathNYDTFRollover() throws IOException {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));

        String psid = "199210091";
        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("122345689", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2, true, true);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.activateDDService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.QUARTERLY);
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-02"), false);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NY_1MN_PAYMENT, DepositFrequencyCode.THREEBANKINGDAY);

        //Exceed Threshold
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-03"), false);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);
        //Verify both payments are clubbed and to be offloaded together with 3 day frequency
        String[] expectedOutputs = {
                "1HDR000000          010515                      000WT-1   880146711003Computing Resources Inc                 6884 Sierra Center Parkway    Reno                     NV89511                                                                                                           ",
                "122345689  1                       010315000000000216000000003240000000067800                                 000000000000000012180000000121800      00                                                                                                                                ",
                "HASH TOTAL 0000000121800  000001                                                                                                                                                                                                                                                       ",
                "1EOF 0000002                                                                                                                                                                                                                                                                           "
        };
        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit,paymentTemplate));
        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        runAssertsOnFilesCreated(filename, expectedOutputs);
    }

    /**
     * Tests negative adjustment limiting to a single law
     */
    @Test
    public void happyPathNYDTFNegativeAdjustmentPriorPayPeriod() throws IOException {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));

        String psid = "199210091";
        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("1223456890", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2, true, true);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.activateDDService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.QUARTERLY);


        HashMap<String, String> lawAmounts = new HashMap();
        //IRS
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");
        //NY-1MN-PAYMENT
        //SIT - 36
        lawAmounts.put("36", "10");
        //NY RES - 54
        lawAmounts.put("54", "11");
        //NY YONKERS RES - 56
        lawAmounts.put("56", "12");
        //NY YONKERS NON-RES - 57
        lawAmounts.put("57", "13");

        //Submit Payroll in current quarter - payroll date 2015-01-03
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-01-03"), Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Send liability adjustment for previous quarter - 2014-12-25
        lawAmounts = new HashMap();
        lawAmounts.put("36", "-20");
        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-01-01"), Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 4, 28, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit,paymentTemplate));
        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        File file = new File(filename);
        FileInputStream fileInputStream=null;
        Key key = IDPSFileStreamManager.newKeyHandleLatest();
        if(StreamUtil.isFileIDPSEncrypted(file)){
            try{
                fileInputStream = new IDPSFileInputStream(file,key);
            }catch (IdpsException e){
                logger.info(e.getMessage());
            }
        }else{
            fileInputStream = new FileInputStream(file);
        }
        String fileString = IOUtils.toString(fileInputStream, "UTF-8");
        List<HashMap<String, String>> fileDataList = NY_DTF_File_Utils.readFileToList(fileString);
        NY_DTF_File_Utils.printFileData(fileDataList);

        //Asserts
        assertEquals(4, fileDataList.size());
        HashMap<String, String> dataRecord = fileDataList.get(1);
        assertEquals(new BigDecimal("75.00"), new BigDecimal(dataRecord.get("Yonkers_Tax_Withheld")));
        assertEquals(new BigDecimal("0.00"), new BigDecimal(dataRecord.get("New_York_State_Tax_Withheld")));
        assertEquals(new BigDecimal("3.00"), new BigDecimal(dataRecord.get("New_York_City_Tax_Withheld")));
        assertEquals(new BigDecimal("78.00"), new BigDecimal(dataRecord.get("Total_Tax_Withheld")));
        assertEquals(new BigDecimal("78.00"), new BigDecimal(dataRecord.get("Total_Remittance_Paid")));

    }

    /**
     * Tests negative adjustment being adjusted across laws
     */
    @Test
    public void happyPathNYDTFNegativeAdjustmentPriorPayPeriod_appliedAcrossLaws() throws IOException {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));

        String psid = "199210091";
        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("1223456890", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2, true, true);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.activateDDService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.QUARTERLY);


        HashMap<String, String> lawAmounts = new HashMap();
        //IRS
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");
        //NY-1MN-PAYMENT
        //SIT - 36
        lawAmounts.put("36", "10");
        //NY RES - 54
        lawAmounts.put("54", "11");
        //NY YONKERS RES - 56
        lawAmounts.put("56", "12");
        //NY YONKERS NON-RES - 57
        lawAmounts.put("57", "13");

        //Submit Payroll in current quarter - payroll date 2015-01-03
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-01-03"), Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Send liability adjustment - 2015-01-01
        lawAmounts = new HashMap();
        lawAmounts.put("36", "-30");
        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-01-01"), Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 4, 28, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit, paymentTemplate));
        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        File file = new File(filename);
        FileInputStream fileInputStream=null;
        Key key = IDPSFileStreamManager.newKeyHandleLatest();
        if(StreamUtil.isFileIDPSEncrypted(file)){
            try{
                fileInputStream = new IDPSFileInputStream(file,key);
            }catch (IdpsException e){
                logger.info(e.getMessage());
            }
        }else{
            fileInputStream = new FileInputStream(file);
        }
        String fileString = IOUtils.toString(fileInputStream, "UTF-8");
        List<HashMap<String, String>> fileDataList = NY_DTF_File_Utils.readFileToList(fileString);
        NY_DTF_File_Utils.printFileData(fileDataList);

        //Asserts
        assertEquals(4, fileDataList.size());
        HashMap<String, String> dataRecord = fileDataList.get(1);
        assertEquals(new BigDecimal("48.00"), new BigDecimal(dataRecord.get("Yonkers_Tax_Withheld")));
        assertEquals(new BigDecimal("0.00"), new BigDecimal(dataRecord.get("New_York_State_Tax_Withheld")));
        assertEquals(new BigDecimal("0.00"), new BigDecimal(dataRecord.get("New_York_City_Tax_Withheld")));
        assertEquals(new BigDecimal("48.00"), new BigDecimal(dataRecord.get("Total_Tax_Withheld")));
        assertEquals(new BigDecimal("48.00"), new BigDecimal(dataRecord.get("Total_Remittance_Paid")));

    }

    /**
     * Tests negative adjustment being adjusted across laws
     */
    @Test
    public void happyPathNYDTFNegativeAdjustmentPriorPayPeriod_adjustmentBiggerThanTotalDue() throws IOException {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));

        String psid = "199210091";
        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("1223456890", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2, true, true);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.activateDDService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.QUARTERLY);


        HashMap<String, String> lawAmounts = new HashMap();
        //IRS
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");
        //NY-1MN-PAYMENT
        //SIT - 36
        lawAmounts.put("36", "10");
        //NY RES - 54
        lawAmounts.put("54", "11");
        //NY YONKERS RES - 56
        lawAmounts.put("56", "12");
        //NY YONKERS NON-RES - 57
        lawAmounts.put("57", "13");

        //Submit Payroll in current quarter - payroll date 2015-01-03
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-01-03"), Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Send liability adjustment - 2015-01-01
        lawAmounts = new HashMap();
        lawAmounts.put("36", "-50");
        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-01-01"), Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 4, 28, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit, paymentTemplate));
        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        File file = new File(filename);
        FileInputStream fileInputStream=null;
        Key key = IDPSFileStreamManager.newKeyHandleLatest();
        if(StreamUtil.isFileIDPSEncrypted(file)){
            try{
                fileInputStream = new IDPSFileInputStream(file,key);
            }catch (IdpsException e){
                logger.info(e.getMessage());
            }
        }else{
            fileInputStream = new FileInputStream(file);
        }
        String fileString = IOUtils.toString(fileInputStream, "UTF-8");
        List<HashMap<String, String>> fileDataList = NY_DTF_File_Utils.readFileToList(fileString);
        NY_DTF_File_Utils.printFileData(fileDataList);

        //Asserts
        assertEquals(4, fileDataList.size());
        HashMap<String, String> dataRecord = fileDataList.get(1);
        assertEquals(new BigDecimal("0.00"), new BigDecimal(dataRecord.get("Yonkers_Tax_Withheld")));
        assertEquals(new BigDecimal("0.00"), new BigDecimal(dataRecord.get("New_York_State_Tax_Withheld")));
        assertEquals(new BigDecimal("0.00"), new BigDecimal(dataRecord.get("New_York_City_Tax_Withheld")));
        assertEquals(new BigDecimal("0.00"), new BigDecimal(dataRecord.get("Total_Tax_Withheld")));
        assertEquals(new BigDecimal("0.00"), new BigDecimal(dataRecord.get("Total_Remittance_Paid")));

    }

    /**
     * Tests ACHDebit cut-off time
     */
    @Test
    public void ACHDebitOffloadTimeTest() throws IOException {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));
        String psid = "199210091";
        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("1223456890", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2, true, true);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.activateDDService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.QUARTERLY);
        //Running first payroll, till now we have not exceeded threshold, so initiation date will be 04/29
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-05"), false);
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NY_1MN_PAYMENT, DepositFrequencyCode.THREEBANKINGDAY);
        //Move to 01/08 and send backdate payroll for 01/06, exceeding threshold thus causing older payment to be due on
        // 01/08
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 7, SpcfTimeZone.getLocalTimeZone()));
        //Before cut off
        assertTrue(PSPDate.getPSPTime().before(SpcfCalendar.createInstance(2015, 1, 7, 13, 55, 0, 0, SpcfTimeZone.getLocalTimeZone())));
        //Exceed Threshold
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-06"), false);

        //Exceeding threshold, so older payment will be modified
        //There should be one mmt for 01/07 initiation because of older payment from payroll 1 - $609.00
        Application.beginUnitOfWork();
        SpcfCalendar initiationDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(initiationDate);
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplateFrequency.getPaymentTemplate())
                                                                                                                       .And(MoneyMovementTransaction.InitiationDate().equalTo(initiationDate))
                                                                                                                       .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDebit)
                                                                                                                                                    .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("609")))));
        assertEquals(1, moneyMovementTransactions.size());
        Application.rollbackUnitOfWork();
        //Run ACHDebit job and verify the file
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);
        //filename with duedate
        CalendarUtils.addBusinessDays(initiationDate,MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit, paymentTemplate));

        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        File file = new File(filename);
        FileInputStream fileInputStream=null;
        Key key = IDPSFileStreamManager.newKeyHandleLatest();
        if(StreamUtil.isFileIDPSEncrypted(file)){
            try{
                fileInputStream = new IDPSFileInputStream(file,key);
            }catch (IdpsException e){
                logger.info(e.getMessage());
            }
        }else{
            fileInputStream = new FileInputStream(file);
        }
        String fileString = IOUtils.toString(fileInputStream, "UTF-8");
        List<HashMap<String, String>> fileDataList = NY_DTF_File_Utils.readFileToList(fileString);
        NY_DTF_File_Utils.printFileData(fileDataList);
        //Asserts
        assertEquals(4, fileDataList.size());
        HashMap<String, String> dataRecord = fileDataList.get(1);
        assertEquals(new BigDecimal("339.00"), new BigDecimal(dataRecord.get("Yonkers_Tax_Withheld")));
        assertEquals(new BigDecimal("108.00"), new BigDecimal(dataRecord.get("New_York_State_Tax_Withheld")));
        assertEquals(new BigDecimal("162.00"), new BigDecimal(dataRecord.get("New_York_City_Tax_Withheld")));
        assertEquals(new BigDecimal("609.00"), new BigDecimal(dataRecord.get("Total_Tax_Withheld")));
        assertEquals(new BigDecimal("609.00"), new BigDecimal(dataRecord.get("Total_Remittance_Paid")));

        //Now lets repeat the same test, this time after the threshold in Q2
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 4, 6, SpcfTimeZone.getLocalTimeZone()));
        //Running first payroll, till now we have not exceeded threshold
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-04-06"), false);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 4, 8, 14, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        //After cut off
        assertTrue(PSPDate.getPSPTime().after(SpcfCalendar.createInstance(2015, 4, 8, 13, 55, 0, 0, SpcfTimeZone.getLocalTimeZone())));
        //Exceed Threshold
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-04-07"), false);
        initiationDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(initiationDate);
        CalendarUtils.addBusinessDays(initiationDate, 1);
        Application.beginUnitOfWork();
        moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                     MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplateFrequency.getPaymentTemplate())
                                                                             .And(MoneyMovementTransaction.InitiationDate().equalTo(initiationDate))
                                                                             .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDebit)
                                                                                                          .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("609")))));
        assertEquals(1, moneyMovementTransactions.size());
        Application.rollbackUnitOfWork();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 4, 9, SpcfTimeZone.getLocalTimeZone()));
        //Run ACHDebit job and verify the file
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);
        CalendarUtils.addBusinessDays(initiationDate, MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit, paymentTemplate));

        filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        file = new File(filename);
        fileInputStream=null;
        if(StreamUtil.isFileIDPSEncrypted(file)){
            try{
                fileInputStream = new IDPSFileInputStream(file,key);
            }catch (IdpsException e){
                logger.info(e.getMessage());
            }
        }else{
            fileInputStream = new FileInputStream(file);
        }
        fileString = IOUtils.toString(fileInputStream, "UTF-8");
        fileDataList = NY_DTF_File_Utils.readFileToList(fileString);
        NY_DTF_File_Utils.printFileData(fileDataList);
        //Asserts
        assertEquals(4, fileDataList.size());
        dataRecord = fileDataList.get(1);
        assertEquals(new BigDecimal("339.00"), new BigDecimal(dataRecord.get("Yonkers_Tax_Withheld")));
        assertEquals(new BigDecimal("108.00"), new BigDecimal(dataRecord.get("New_York_State_Tax_Withheld")));
        assertEquals(new BigDecimal("162.00"), new BigDecimal(dataRecord.get("New_York_City_Tax_Withheld")));
        assertEquals(new BigDecimal("609.00"), new BigDecimal(dataRecord.get("Total_Tax_Withheld")));
        assertEquals(new BigDecimal("609.00"), new BigDecimal(dataRecord.get("Total_Remittance_Paid")));

    }

    public static void runAssertsOnFilesCreated(String filename, String[] expectedOutput) throws IOException {
        InputStreamReader fileReader=null;
        Key key = IDPSFileStreamManager.newKeyHandleLatest();
        if(StreamUtil.isFileIDPSEncrypted(filename)){
            try{
                fileReader = new IDPSFileReader(filename,key);
            }catch (IdpsException e){
                logger.info(e.getMessage());
            }
        }else {
            fileReader = new FileReader(filename);
        }
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String sCurrentLine;
        int lineCounter = 0;
        while ((sCurrentLine = bufferedReader.readLine()) != null) {
            assertEquals(expectedOutput[lineCounter], sCurrentLine);
            lineCounter++;
        }
        assertEquals("Number of lines", expectedOutput.length, lineCounter);
    }


    //Run holidays 2 days differnece falls on holiday
    @Test
    public void happyPathNYDTFOnHoliday() throws IOException {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NY"};
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NY_1MN_PAYMENT, SpcfCalendar.createInstance(2005, 1, 1));

        String psid = "199210091";
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", false, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("NY");
        DataLoadServices.addCompanyLawsWithAgencyId("123456789 6", company, "NY", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, NY_1MN_PAYMENT, DepositFrequencyCode.FIVEBANKINGDAY);
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-06"), false);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NY_1MN_PAYMENT, DepositFrequencyCode.QUARTERLY);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 9, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);
        String[] expectedOutputs = {
                "1HDR000000          010915                      000WT-1   880146711003Computing Resources Inc                 6884 Sierra Center Parkway    Reno                     NV89511                                                                                                           ",
                "123456789  6                       010615000000000072000000001080000000022600                                 000000000000000004060000000040600      00                                                                                                                                ",
                "HASH TOTAL 0000000040600  000001                                                                                                                                                                                                                                                       ",
                "1EOF 0000002                                                                                                                                                                                                                                                                           "
        };
        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initiationDate, MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit, paymentTemplate));
        String filename = OffloadNYDTFPayments.ARCHIVE_DIRECTORY + File.separator + OffloadNYDTFPayments.FILENAME_PREFIX + initiationDate.format("MMdd") + OffloadNYDTFPayments.FILE_EXT;
        runAssertsOnFilesCreated(filename, expectedOutputs);
    }



}
