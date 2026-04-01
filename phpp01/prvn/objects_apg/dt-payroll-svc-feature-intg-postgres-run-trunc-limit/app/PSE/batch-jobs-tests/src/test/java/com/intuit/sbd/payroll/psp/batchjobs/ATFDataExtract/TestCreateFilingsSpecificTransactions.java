package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedUnprocessedRequestTests;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.agency.eftps.EftpsDataLoader;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.utils.ATFDataExtractCompare;
import com.intuit.sbd.payroll.psp.batchjobs.utils.CompareResults;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import intuit.osp.common.utils.FileUtils;
import org.junit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

public class TestCreateFilingsSpecificTransactions {

    private static final ArrayList<String> supportedPaymentTemplates = new ArrayList<String>();
    private int achTaxOffloadOffset;
    
    static {
        supportedPaymentTemplates.add("IRS-940-PAYMENT");
        supportedPaymentTemplates.add("IRS-941-PAYMENT");
        supportedPaymentTemplates.add("CA-PITSDI-PAYMENT");
        supportedPaymentTemplates.add("CA-UIETT-PAYMENT");
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();

        //Set the last time we ran the batch job back to the beginning of time
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.FILING_SPECIFIC_TRANSACTIONS_TOKEN, Long.toString(SpcfCalendar.createInstance(2005, 1, 1).getTimeInMilliseconds()));
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = Application.refresh(achTaxOffloadOffsetParam);
        achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue());
        PayrollServices.commitUnitOfWork();

        for (String agencyId : supportedPaymentTemplates) {
            DataLoadServices.updatePaymentTemplateSupportedDate(agencyId, SpcfCalendar.createInstance(2005, 1, 1));
        }
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void testHappyPath() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 12, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String companyPSID = "999061606";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.enrollEFTPS(company);

        //******************3/7/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE999061606.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);        
        
        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setReadyToSend().find();
        MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("158"))).get(0);
        ProcessResult updateInitDateProcess = PayrollServices.paymentManager.updateInitiationDate(moneyMovementTransaction.getId().toString(), SpcfCalendar.createInstance(2011, 3, 7, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(updateInitDateProcess);
        PayrollServices.commitUnitOfWork();
        
        sendPaymentsAndSimulateSuccess();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData + " 2011 1", "atfextract/expected/test_happyPath_0307_LIA_expected", "atfextract/expected/test_happyPath_0307_PAY_expected");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();
        assertEquals("Partial CDLs on 3/7", 6, cdls.size());

        //******************3/15/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 15, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        sendPaymentsAndSimulateSuccess();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        new CreateFilingsSpecificTransactions().updateCompanyDailyLiabilities();
        cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();
        assertEquals("Partial CDLs on 3/15", 7, cdls.size());

        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_0315_LIA_expected", "atfextract/expected/test_happyPath_0315_PAY_expected");

        //******************3/18/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 18, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/PayrollFor999061606.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        sendPaymentsAndSimulateSuccess();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();

        // Setting the time forward two hours.
        // TODO: TLD - If we are generating identical PSP Time in Java and Stored Procedures for past dates that may
        // TODO: (or may not) be in a different DST window, this should not be necessary.
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 18, 15, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        new CreateFilingsSpecificTransactions().updateCompanyDailyLiabilities();
        cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();
        assertEquals("All CDLs on 3/18", 13, cdls.size());

        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_0318_LIA_expected", "atfextract/expected/test_happyPath_0318_PAY_expected");

        // Running a subsequent extract should yield empty results.
        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_empty_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");

        //******************3/22/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110322070000");
        PayrollServices.commitUnitOfWork();

        sendPaymentsAndSimulateSuccess();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_0322_LIA_expected", "atfextract/expected/test_happyPath_0322_PAY_expected");

        //******************3/24/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110324070000");
        new CreateFilingsSpecificTransactions().updateCompanyDailyLiabilities();
        cdls = Application.find(CompanyDailyLiability.class);
        assertEquals("All CDLs on 3/24", 13, cdls.size());
        PayrollServices.commitUnitOfWork();

        sendPaymentsAndSimulateSuccess();

        //override status as completed (i.e by passing FTP). This step is not seen in production.
        PayrollServices.beginUnitOfWork();
        EftpsFile eftFile = EftpsFile.getPendingTransmissionEftpsFiles().get(0);
        Assert.assertNotNull(eftFile);
        eftFile.setStatusCd(EdiFileStatus.Completed);
        Application.save(eftFile);
        PayrollServices.commitUnitOfWork();

        EftpsDataLoader.callSimulator();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_0324_LIA_expected", "atfextract/expected/test_happyPath_0324_PAY_expected");

        //******************3/30/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110330070000");
        PayrollServices.commitUnitOfWork();

        sendPaymentsAndSimulateSuccess();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        new CreateFilingsSpecificTransactions().updateCompanyDailyLiabilities();
        cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 3/31", 13, cdls.size());

        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_0330_LIA_expected", "atfextract/expected/test_happyPath_0330_PAY_expected");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110331070000");
        company = Company.findCompany(companyPSID, SourceSystemCode.QBDT);
        ArrayList<String> checksToCancel = new ArrayList<String>();
        checksToCancel.add("1");

        //void payroll
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        Paycheck check = Paycheck.findPaycheck(company, "1");
        voidPayrollDTO.setSourcePayrollRunId(check.getPayrollRun().getSourcePayRunId());
        voidPayrollDTO.setPaycheckIdList(checksToCancel);
        ProcessResult procResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, companyPSID, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(procResult);

        DataLoadServices.runOffload(PSPDate.getPSPTime());
        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_0331_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        
        PayrollServices.beginUnitOfWork();
        int countOfCompletelyZeroedCDLs=0;
        cdls = Application.find(CompanyDailyLiability.class);
        for (CompanyDailyLiability currCDL : cdls) {
            if (currCDL.getTaxableWages().equals(SpcfMoney.ZERO) &&
                    currCDL.getTotalWages().equals(SpcfMoney.ZERO) &&
                    currCDL.getTaxAmount().equals(SpcfMoney.ZERO)) {
                countOfCompletelyZeroedCDLs++;
            }
        }

        PayrollServices.commitUnitOfWork();

        //Make sure we've got the voided ones here but that their amounts are 0
        assertEquals("All CDLs", 13, cdls.size());
        assertEquals("Number of 0'd CDLs: ", 6, countOfCompletelyZeroedCDLs);


        //Make sure nothing changes
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110420070000");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        countOfCompletelyZeroedCDLs=0;
        new CreateFilingsSpecificTransactions().updateCompanyDailyLiabilities();
        cdls = Application.find(CompanyDailyLiability.class);
        for (CompanyDailyLiability currCDL : cdls) {
            if (currCDL.getTaxableWages().equals(SpcfMoney.ZERO) &&
                    currCDL.getTotalWages().equals(SpcfMoney.ZERO) &&
                    currCDL.getTaxAmount().equals(SpcfMoney.ZERO)) {
                countOfCompletelyZeroedCDLs++;
            }
        }

        PayrollServices.commitUnitOfWork();

        //Make sure we've got the voided ones here but that their amounts are 0
        assertEquals("All CDLs", 13, cdls.size());
        assertEquals("Number of 0'd CDLs: ", 6, countOfCompletelyZeroedCDLs);

        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_0420_LIA_expected", "atfextract/expected/test_happyPath_0420_PAY_expected");
    }

    @Test
    public void testHappyPathSameDay() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 12, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String companyPSID = "999061606";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.enrollEFTPS(company);

        //******************3/7/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 13, 15, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE999061606.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setReadyToSend().find();
        MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("158"))).get(0);
        ProcessResult updateInitDateProcess = PayrollServices.paymentManager.updateInitiationDate(moneyMovementTransaction.getId().toString(), SpcfCalendar.createInstance(2011, 3, 7, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(updateInitDateProcess);
        PayrollServices.commitUnitOfWork();

        sendPaymentsAndSimulateSuccess();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 17, 15, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runOffload(PSPDate.getPSPTime());

        runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData + " 2011 1", "atfextract/expected/test_happyPath_0307_LIA_expected", "atfextract/expected/test_happyPath_0307_PAY_expected");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();
        assertEquals("Partial CDLs on 3/7", 6, cdls.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 18, 15, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        company = Company.findCompany(companyPSID, SourceSystemCode.QBDT);
        ArrayList<String> checksToCancel = new ArrayList<String>();
        checksToCancel.add("1");

        //void payroll
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        Paycheck check = Paycheck.findPaycheck(company, "1");
        voidPayrollDTO.setSourcePayrollRunId(check.getPayrollRun().getSourcePayRunId());
        voidPayrollDTO.setPaycheckIdList(checksToCancel);
        ProcessResult procResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, companyPSID, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(procResult);

        DataLoadServices.runOffload(PSPDate.getPSPTime());
        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_sameday_0407_LIA_expected", "atfextract/expected/test_happyPath_0307_PAY_expected");
    }

    private void sendPaymentsAndSimulateSuccess() {
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        //Simulate that sent payments are acknowledged
        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Where(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.SentToAgency)
                        .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Executed))
                        .And((MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)).Or(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHCredit))));

        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, query);

        for (MoneyMovementTransaction mmt : mmts) {
            mmt.setTaxPaymentStatus(TaxPaymentStatus.AcknowledgedByAgency);
            Application.save(mmt);
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRecreateAllCDLsForQuarter() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 12, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String companyPSID = "999061606";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.enrollEFTPS(company);

        //******************3/7/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE999061606.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setReadyToSend().find();
        MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("158"))).get(0);
        ProcessResult updateInitDateProcess = PayrollServices.paymentManager.updateInitiationDate(moneyMovementTransaction.getId().toString(), SpcfCalendar.createInstance(2011, 3, 7, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(updateInitDateProcess);
        PayrollServices.commitUnitOfWork();

        sendPaymentsAndSimulateSuccess();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        //******************3/15/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 15, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        sendPaymentsAndSimulateSuccess();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        new CreateFilingsSpecificTransactions().processCompanyDailyLiabilities("ALL", CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime()), CalendarUtils.getLastDayOfQuarter(PSPDate.getPSPTime()));
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();
        assertEquals("Partial CDLs on 3/15", 7, cdls.size());

        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_0315_LIA_expected", "atfextract/expected/test_regenerateAll_0315_PAY_expected");
    }

    @Test
    public void testHPDE999062181OFX() throws Exception {
        String companyPSID = "999062181";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 5, 3, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE999062181.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 5, 3, 15, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/PayrollFor999062181.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 2", "atfextract/expected/test_tips_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
    }

    @Test
    public void testHPDE504010155OFX_AZSupported() throws Exception {

        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        try {
            String companyPSID = "504010155";
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
            DataLoadServices.activateTaxServiceExceptBalanceFile(company);

            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 7, 13, 9, 51, 0, 0, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE504010155.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            company = DataLoadServices.refreshCompany(company);
            ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
            QBDTTestHelper.submitQBDTRequest(ofx);

            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 7, 18, 15, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/PayrollFor504010155.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            company = DataLoadServices.refreshCompany(company);
            ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
            QBDTTestHelper.submitQBDTRequest(ofx);

            DataLoadServices.runOffload(PSPDate.getPSPTime());

            runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 2", "atfextract/expected/test_ACHCredit_lia_expected", "atfextract/expected/test_ACHCredit_pay_expected");
        } finally {
            DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", null);
        }
    }

    @Test
    public void testHPDE504010155OFX_ACHCredit() throws Exception {

        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        try {
            String companyPSID = "504010155";
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
            DataLoadServices.activateTaxServiceExceptBalanceFile(company);

            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 7, 13, 9, 51, 0, 0, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE504010155.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            company = DataLoadServices.refreshCompany(company);
            ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
            QBDTTestHelper.submitQBDTRequest(ofx);

            DataLoadServices.updateAgencyTaxpayerId(company, "AZ-A1-PAYMENT", company.getFedTaxId());

            PayrollServices.beginUnitOfWork();
            assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(),"AZ-A1-PAYMENT", PaymentMethod.ACHCredit, true));
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 7, 18, 15, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/PayrollFor504010155.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            company = DataLoadServices.refreshCompany(company);
            ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
            QBDTTestHelper.submitQBDTRequest(ofx);

            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20110720000000");
            PayrollServices.commitUnitOfWork();

            DataLoadServices.runOffload(PSPDate.getPSPTime());

            PayrollServices.beginUnitOfWork();
            String taxOffloadTime = achTaxOffloadOffset == 1? "20110726000000" : "20110725000000";
            PSPDate.setPSPTime(taxOffloadTime);
            PayrollServices.commitUnitOfWork();

            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

            sendPaymentsAndSimulateSuccess();

            runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 3", "atfextract/expected/test_ACHCredit_currentqtr_lia_expected", "atfextract/expected/test_ACHCredit_currentqtr_pay_expected");
        } finally {
            DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", null);
        }
    }

    @Test
    public void testHPDE504010155OFX() throws Exception {
        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));

        try {
            String companyPSID = "504010155";
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
            DataLoadServices.activateTaxServiceExceptBalanceFile(company);

            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 7, 13, 9, 51, 0, 0, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE504010155.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            company = DataLoadServices.refreshCompany(company);
            ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
            QBDTTestHelper.submitQBDTRequest(ofx);

            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 7, 18, 15, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/PayrollFor504010155.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            company = DataLoadServices.refreshCompany(company);
            ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
            QBDTTestHelper.submitQBDTRequest(ofx);

            DataLoadServices.runOffload(PSPDate.getPSPTime());

            runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 2", "atfextract/expected/test_HPDE_unsupportedAgency_lia", "atfextract/expected/test_HPDE_unsupportedAgency_pay");
        } finally {
            DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", null);
        }
    }

    @Test
    public void testHPDE999062144OFX() throws Exception {
        String companyPSID = "999062144";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 25, 17, 40, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE999062144.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 25, 17, 46, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/PayrollFor999062144.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 25, 17, 50, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Payroll2For999062144.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 26, 17, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        
        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 5, 4, 16, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        SpcfCalendar firstDayOfQtr = CalendarUtils.getFirstDayOfQuarter(2011, 2);
        SpcfCalendar lastDayOfQtr = CalendarUtils.getLastDayOfQuarter(2011, 2);

        new CreateFilingsSpecificTransactions().processCompanyDailyLiabilities(CreateFilingsSpecificTransactions.ALL, firstDayOfQtr, lastDayOfQtr);
        PayrollServices.commitUnitOfWork();

//        runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 2", "atfextract/expected/test_tips_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
    }

    @Test
    public void testMultipleCompanies() throws Exception {
        //******************3/7/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Company 1
        String companyPSID = "999061606";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.enrollEFTPS(company);
        OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE999061606.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        //Company 2
        companyPSID = "999061349";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.enrollEFTPS(company);
        ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE999061349.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        //Company 3
        companyPSID = "999061589";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.enrollEFTPS(company);
        ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE999061589.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);
        
        sendPaymentsAndSimulateSuccess();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        new CreateFilingsSpecificTransactions().updateCompanyDailyLiabilities();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();
        assertEquals("Partial CDLs on 3/7", 55, cdls.size());

        //******************3/15/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 15, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        sendPaymentsAndSimulateSuccess();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        new CreateFilingsSpecificTransactions().updateCompanyDailyLiabilities();
        cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();
        assertEquals("All CDLs on 3/15", 56, cdls.size());


//        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_0315_LIA_expected", "atfextract/expected/test_regenerateAll_0315_PAY_expected");
    }

    public static void runExtractAndValidateFiles(String pExtractType, String pLiabilitiesFile, String pPaymentsFile) throws Exception {
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                pExtractType);
        processor.validateRuntimeParameters();
        processor.execute();

        String extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());

        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }

        ATFDataExtractFile companyLiabilitiesExtract = null;
        DomainEntitySet<ATFDataExtractFile> extractFiles =
                Application.find(ATFDataExtractFile.class,
                        ATFDataExtractFile.ATFDataExtractBatch().equalTo(extractBatch)
                                .And(ATFDataExtractFile.FileType().equalTo(ATFDataExtractFileType.CompanyLiabilitiesInfo)));

        if (extractFiles != null) {
            companyLiabilitiesExtract = extractFiles.get(0);
        }

        ATFDataExtractFile companyPaymentsExtract = null;
        DomainEntitySet<ATFDataExtractFile> paymentsExtractFiles =
                Application.find(ATFDataExtractFile.class,
                        ATFDataExtractFile.ATFDataExtractBatch().equalTo(extractBatch)
                                .And(ATFDataExtractFile.FileType().equalTo(ATFDataExtractFileType.CompanyPaymentsInfo)));

        if (extractFiles != null) {
            companyPaymentsExtract = paymentsExtractFiles.get(0);
        }

        PayrollServices.commitUnitOfWork();

        //Make sure the generated files are correct
        validateFile(
                Application.findFileOnClassPath(pLiabilitiesFile),
                companyLiabilitiesExtract);

        validateFile(
                Application.findFileOnClassPath(pPaymentsFile),
                companyPaymentsExtract);
    }

    /** Validates files for given expectedFile & ExtractBatchID */
    public static void validateFile(String expectedFileName, String extractBatchId, ATFDataExtractFileType fileType) throws Exception {
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
        
        DomainEntitySet<ATFDataExtractFile> paymentsExtractFiles =
                Application.find(ATFDataExtractFile.class,
                        ATFDataExtractFile.ATFDataExtractBatch().equalTo(extractBatch)
                                .And(ATFDataExtractFile.FileType().equalTo(fileType)));
        
        validateFile(Application.findFileOnClassPath("atfextract/expected/" + expectedFileName), paymentsExtractFiles.get(0));
    }
    
    private static void validateFile(String pExpectedFileName, ATFDataExtractFile pExtractFile) throws Exception {
        String createdFileName = extractFile(pExtractFile);

        BufferedReader expectedReader = new BufferedReader(new FileReader(pExpectedFileName));
        BufferedReader compareReader = new BufferedReader(new FileReader(createdFileName));

        ATFDataExtractCompare compare = new ATFDataExtractCompare();
        CompareResults compareResults = compare.compareATFDataExtractFile(expectedReader, compareReader);

        String expectedFileStr = org.apache.commons.io.FileUtils.readFileToString(new File(pExpectedFileName));
        System.out.println("Expected file: " + expectedFileStr);
        System.out.println("Expected file size: " + expectedFileStr.length());

        String createdFileStr = org.apache.commons.io.FileUtils.readFileToString(new File(createdFileName));
        System.out.println("Generated file: " + createdFileStr);
        System.out.println("Generated file size: " + createdFileStr.length());

        if (!compareResults.getStatus()) {
            System.out.println(compareResults.toString());
        }
        assertEquals("File " + createdFileName + " matches expected file " + pExpectedFileName, true, compareResults.getStatus());
    }

    private static String extractFile(ATFDataExtractFile pExtractFile) throws Exception {
        return FileUtils.gUnZip(pExtractFile.getFileName());
    }
}
