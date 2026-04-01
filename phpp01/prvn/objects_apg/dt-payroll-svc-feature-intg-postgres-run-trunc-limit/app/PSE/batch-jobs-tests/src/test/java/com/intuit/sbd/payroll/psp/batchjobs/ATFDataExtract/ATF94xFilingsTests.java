package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXAssert;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXRequestGenerator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.agency.eftps.EftpsDataLoader;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedPayrolls;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLRS;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Apr 13, 2011
 * Time: 1:33:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ATF94xFilingsTests {

    private static final String FIT = "1";
    private static final String FICA = "61";
    private static final String FUTA = "66";
    private int achTaxOffloadOffset;

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = Application.refresh(achTaxOffloadOffsetParam);
        achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue());
        //Set the last time we ran the ATF batch job back to the beginning of time
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.FILING_SPECIFIC_TRANSACTIONS_TOKEN, Long.toString(SpcfCalendar.createInstance(2005, 1, 1).getTimeInMilliseconds()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Ignore
    @Test
    public void getDate() {
        String lastEventProcessedTimestamp = "1306973701156";
        SpcfCalendar begin = SpcfCalendar.createInstance(Long.parseLong(lastEventProcessedTimestamp), SpcfTimeZone.getLocalTimeZone());
        System.out.println("Date: "+begin);
    }

    @Test
    public void testNoDebitPayrollGreaterThanTwoDaysAway() throws Exception {
        String psid="123456789";

        ArrayList<String> supportedPaymentTemplates = new ArrayList<String>();
        supportedPaymentTemplates.add("IRS-940-PAYMENT");
        supportedPaymentTemplates.add("IRS-941-PAYMENT");
        supportedPaymentTemplates.add("CA-PITSDI-PAYMENT");
        supportedPaymentTemplates.add("CA-UIETT-PAYMENT");

        for (String agencyId : supportedPaymentTemplates) {
            DataLoadServices.updatePaymentTemplateSupportedDate(agencyId, SpcfCalendar.createInstance(2005, 1, 1));
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(2011, 1, 5));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, "1", null, new DateDTO(2011, 1, 5), new SpcfMoney("27.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(2011, 1, 5), new SpcfMoney("200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(false);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO(2011, 1, 5), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmission companyAdjustmentSubmission = Application.findById(CompanyAdjustmentSubmission.class, processResult.getResult().getId());
        assertNotNull("Company Adjustment Submission", companyAdjustmentSubmission);
        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = companyAdjustmentSubmission.getLiabilityAdjustmentCollection();
        Assert.assertEquals("Liability Adjustments", 2, liabilityAdjustments.size());

        PayrollRun payrollRun = null;
        for (LiabilityAdjustment liabilityAdjustment : companyAdjustmentSubmission.getLiabilityAdjustmentCollection()) {
            if (liabilityAdjustment.getLaw().isFIT()) {
                Assert.assertEquals("Amount", "27.20", liabilityAdjustment.getAmount().toString());
            }
            if (liabilityAdjustment.getLaw().isFICA()) {
                Assert.assertEquals("Amount", "200.27", liabilityAdjustment.getAmount().toString());
            }
            Assert.assertEquals("Taxable Wages", "0.00", liabilityAdjustment.getTaxableWages().toString());
            Assert.assertEquals("Total Wages", "0.00", liabilityAdjustment.getTotalWages().toString());
            if (payrollRun == null) {
                payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, liabilityAdjustment.getPayrollRun().getId());
            }
            assertNotNull("Payroll Run", payrollRun);
        }
        Assert.assertEquals("Payroll Run Type", PayrollType.Adjustment, payrollRun.getPayrollRunType());

        // Make sure the right financial transactions were created for this Payroll Run

        DomainEntitySet<FinancialTransaction> taxPaymentTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit));
        Assert.assertEquals("Agency Tax Credit Transaction Count", 2, taxPaymentTransactions.size());
        PayrollServices.rollbackUnitOfWork();

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString() + " 2011 1", "atfextract/expected/test_NoDebitPayroll2DaysAway_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");


    }

    @Test
    public void testATF94xPayroll_happyPath() throws Exception {
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"), false);

        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 4, 12, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 12, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 13, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_empty_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        assertEquals("Updated CDLs on 4/13", 7, cdls.size());
        PayrollServices.commitUnitOfWork();

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString() + " 2011 2", "atfextract/expected/test_happyPath_0413_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        cdls = Application.find(CompanyDailyLiability.class);
        assertEquals("All CDLs on 4/13", 7, cdls.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testATF94xPayroll_backdated() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-07-01"), new DateDTO("2011-04-15"), false);

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_0413_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        assertEquals("Updated CDLs on 7/1", 7, cdls.size());
        PayrollServices.commitUnitOfWork();

        // Advance 7 days, run the ACH Transaction processor and make sure we do not extract liability data again.
        DataLoadServices.runACHTransactionProcessor();  // Advances PSP Time by 7 days.
        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_empty_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString() + " 2011 2", "atfextract/expected/test_happyPath_0413_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        cdls = Application.find(CompanyDailyLiability.class);
        assertEquals("All CDLs on 7/1", 7, cdls.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testATF94xPayroll_recallEntirePayroll_happyPath() throws Exception {
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"), false);

        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 4, 12, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 12, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

         //Recall one paycheck from the payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        Assert.assertEquals("PayrollRun status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();
        assertSuccess(recallProcessResult);

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 13, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_empty_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        // Even though the payroll has been recalled, the CDLs are created, but with 0 values.
        assertEquals("All CDLs on 4/13", 7, cdls.size());
        for ( CompanyDailyLiability cdl : cdls ) {
            assertEquals("CDL Taxable Wages", new SpcfMoney(), cdl.getTaxableWages());
            assertEquals("CDL Total Wages", new SpcfMoney(), cdl.getTotalWages());
            assertEquals("CDL Total Tips Amount", new SpcfMoney(), cdl.getTotalTipsAmount());
            assertEquals("CDL Tax Amount", new SpcfMoney(), cdl.getTaxAmount());
        }

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString() + " 2011 2", "atfextract/expected/test_RecallEntirePayroll_AllZeroes_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");

    }

    @Test
    public void testSubmitBALFWithLiabilities() throws Exception {
        String psid = "123456789";
        Date quarterToStart = new Date("01/01/2011");
        List<String> lawIds = new ArrayList<String>();
        lawIds.add("61");
        lawIds.add("62");
        lawIds.add("63");
        lawIds.add("64");
        lawIds.add("1");
        lawIds.add("142");
        lawIds.add("66");
        lawIds.add("67");
        lawIds.add("87");
        List<String> overPaymentLawIds = new ArrayList<String>();
        overPaymentLawIds.add("61");
        overPaymentLawIds.add("62");
        List<String> refundLawIds = new ArrayList<String>();
        refundLawIds.add("66");
        refundLawIds.add("64");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008,1,1));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 5, 20, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, true);

        OFX overpaymentBalanceFile = new OFX();
        overpaymentBalanceFile.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));

        PayrollServices.beginUnitOfWork();
        List<IEMP> employees = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP();
        List<IPITEM> payrollItems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        int payrollItemId = 1;
        for (Iterator<IPITEM> iterator = payrollItems.iterator(); iterator.hasNext();) {
            IPITEM ipitem = iterator.next();
            com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem payrollItem = new com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem(ipitem);
            if (payrollItem.getSourceLawId() != null) {
                Law law = SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.QBDT, payrollItem.getSourceLawId());
                if(lawIds.contains(law.getLawId())) {
                    ipitem.setIPITEMID(payrollItemId++ + "");
                } else {
                    iterator.remove();
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();

        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(employees,
                payrollItems,
                new Date("05/20/2011"),
                new Date("05/01/2011"),
                new Date("05/31/2011"),
                true));

        // updated the tax amounts to match the payroll item id
        for (IPAYROLLRUN ipayrollrun : payrollRuns) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                for (ITAXLINE itaxline : ipaychk.getITAXLINE()) {
                    int itemId = Integer.parseInt(itaxline.getIPITEMID());
                    if(itaxline.getIAMT().contains("-")) {
                        itaxline.setIAMT("$-" + itemId + ".00");
                    } else {
                        itaxline.setIAMT("$" + itemId + ".00");
                    }
                }
            }
        }

        List<IPAYROLLTX> payrollTransactions = new ArrayList<IPAYROLLTX>();
        //Prior Payments tax lines
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        int priorPaymentTotal = 0;
        for (IPITEM payrollItem : payrollItems) {
            int amount = 0;
            com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem payroll = new com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem(payrollItem);

            if (payroll.getSourceLawId() != null) {
            Law law = SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.QBDT, payroll.getSourceLawId());
            if(overPaymentLawIds.contains(law.getLawId())){
                amount = Integer.parseInt(payrollItem.getIPITEMID())*5 + 1;
            }
            if(amount > 0){
                priorPaymentTotal += amount;
                itxlines.add(OFXRequestGenerator.generateTransactionLine(null,
                        new SpcfMoney(SpcfDecimal.createInstance(amount)),
                        null,
                        false,
                        null,
                        payrollItem.getIPITEMID(),
                        null,
                        null));
            }
        }
        }

        payrollTransactions.add(OFXRequestGenerator.generatePayrollTransaction(null,
                new SpcfMoney(SpcfDecimal.createInstance(-priorPaymentTotal)),
                null,
                new Date("05/31/2011"),
                new Date("05/20/2011"),
                null,
                null,
                null,
                false,
                QBOFX.OFXPayrollTransactionTransactionType.PRIORPMT,
                null,
                false,
                itxlines));

        //Refund tax lines
        List<ITXLINE> refundItxlines = new ArrayList<ITXLINE>();
        //split into two submissions so dates can be different so sort does not have to rely on FK
        List<ITXLINE> refundItxlines2 = new ArrayList<ITXLINE>();
        int refundPaymentTotal = 0;
        for (IPITEM payrollItem : payrollItems) {
            int amount = 0;
            com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem payroll = new com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem(payrollItem);
            if (payroll.getSourceLawId() != null) {
            Law law = SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.QBDT, payroll.getSourceLawId());
            if(refundLawIds.contains(law.getLawId())){
                amount = Integer.parseInt(payrollItem.getIPITEMID())*5 - 1;
            }
            if(amount > 0){
                refundPaymentTotal += amount;
                ITXLINE itxline = OFXRequestGenerator.generateTransactionLine(null,
                        new SpcfMoney(SpcfDecimal.createInstance(amount * -1)),
                        null,
                        false,
                        null,
                        payrollItem.getIPITEMID(),
                        null,
                        null);
                if (law.getLawId().equals("66")) {
                    refundItxlines.add(itxline);
                } else {
                    refundItxlines2.add(itxline);
                }

            }
        }
        }

        payrollTransactions.add(OFXRequestGenerator.generatePayrollTransaction(null,
                new SpcfMoney(SpcfDecimal.createInstance(refundPaymentTotal)),
                null,
                new Date("05/19/2011"),
                new Date("05/19/2011"),
                null,
                null,
                null,
                false,
                QBOFX.OFXPayrollTransactionTransactionType.REFUND,
                null,
                false,
                refundItxlines));

        payrollTransactions.add(OFXRequestGenerator.generatePayrollTransaction(null,
                new SpcfMoney(SpcfDecimal.createInstance(refundPaymentTotal)),
                null,
                new Date("05/18/2011"),
                new Date("05/18/2011"),
                null,
                null,
                null,
                false,
                QBOFX.OFXPayrollTransactionTransactionType.REFUND,
                null,
                false,
                refundItxlines2));


        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(true,
                quarterToStart,
                employees,
                null,
                null,
                payrollItems,
                null,
                null,
                payrollTransactions,
                null,
                null,
                payrollRuns);
        overpaymentBalanceFile.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(overpaymentBalanceFile);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull("Payroll response", ipayrollrs);

        // verify the liab check
        Assert.assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTX().size());
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());

        //Run offload on 5/20
        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        Assert.assertEquals("Number of PayrollRuns",2, PayrollRun.findPayrollRuns(company).size());
        DomainEntitySet<PayrollRun> adjustmentPayrollRuns = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.Adjustment);
        Assert.assertEquals("Number of PayrollRuns",1, adjustmentPayrollRuns.size());
        DomainEntitySet<FinancialTransaction> eRFinancialTransactions = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxDebit);
        Assert.assertEquals("ER Tax debits",1, eRFinancialTransactions.size());
        Assert.assertEquals("ER Tax debits",new SpcfMoney("50.00"), eRFinancialTransactions.get(0).getFinancialTransactionAmount());
        DomainEntitySet<FinancialTransaction> agencyTaxCreditFinancialTransactions = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxCredit);
        Assert.assertEquals("Number of AgencyTaxCredit Financial Transactions", 4, agencyTaxCreditFinancialTransactions.size());

        Assert.assertEquals("Number of HPDE Financial Transactions", 2, FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyHPDETaxPayment).size());
        Assert.assertEquals("Number of Refund Financial Transactions", 2, FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyHPDETaxRefund).size());
        DomainEntitySet<MoneyMovementTransaction> eftpsMoneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setOnHold().find();
        Assert.assertEquals("Number of EFTPS MMTs", 2, eftpsMoneyMovementTransactions.size());
        DomainEntitySet<MoneyMovementTransaction> hpdeMMTs = Application.find(MoneyMovementTransaction.class,  MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.HPDE));
        Assert.assertEquals("Number of HPDE MMTs", 1, hpdeMMTs.size());
        DomainEntitySet<MoneyMovementTransaction> hpdeRefundMMTs = Application.find(MoneyMovementTransaction.class,  MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.HPDERefund));
        Assert.assertEquals("Number of HPDE refund MMTs", 2, hpdeRefundMMTs.size());
        PayrollServices.commitUnitOfWork();

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_BALF_LIA_expected", "atfextract/expected/test_happyPath_BALF_PAY_expected");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 4/20", 6, cdls.size());
    }


    @Test
    public void testATF94xPayroll_VoidEntirePayroll() throws Exception {
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"), false);

        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 4, 14, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110413000000");
        PayrollServices.commitUnitOfWork();

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 2", "atfextract/expected/test_happyPath_0413_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 18, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Void one paycheck
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(voidProcessResult);

        // verify all Paychecks are voided
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        for (Paycheck paycheck : payroll.getPaycheckCollection()) {
            Assert.assertEquals("Paycheck voided", true, paycheck.isVoided());
        }
        PayrollServices.commitUnitOfWork();

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 2", "atfextract/expected/test_completelyVoided_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
    }

    @Test
    public void testATF94xPayroll_VoidPartialPayroll() throws Exception {
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"), false);

        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 4, 14, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110413000000");
        PayrollServices.commitUnitOfWork();

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 2", "atfextract/expected/test_happyPath_0413_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 18, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(Company.findCompany(psid, SourceSystemCode.QBDT), payrollRunDTO.getPayrollTXBatchId());
        String voidedPaycheckId = payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId();
        PayrollServices.commitUnitOfWork();

        //Void one paycheck
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        List<String> payCheckIds = new ArrayList<String>();
        payCheckIds.add(voidedPaycheckId);
        voidPayrollDTO.setPaycheckIdList(payCheckIds);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(voidProcessResult);

        // verify just the one Paycheck is voided
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        for (Paycheck paycheck : payroll.getPaycheckCollection()) {
            if(voidedPaycheckId.equals(paycheck.getSourcePaycheckId())){
                Assert.assertEquals("Paycheck voided", true, paycheck.isVoided());
            } else {
                Assert.assertEquals("Paycheck voided", false, paycheck.isVoided());
            }

        }
        PayrollServices.commitUnitOfWork();

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 2", "atfextract/expected/test_partiallyVoided_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
    }


    @Test
    public void testATF94xPayroll_LiabilityAdjustments() throws Exception {
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"), false);
        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 4, 19, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_empty_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, "1", null, new DateDTO("2011-04-01"), new SpcfMoney("178.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO("2011-04-01"), new SpcfMoney("150.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FUTA, FUTA, null, new DateDTO("2011-04-01"), new SpcfMoney("125.75"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO("2011-04-01"), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_empty_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 2", "atfextract/expected/test_adjustments_0418_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
    }

    @Test
    public void testATF94xBackDatedPayroll() throws Exception {
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-23"), new DateDTO("2011-04-16"), false);
        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 4, 26, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }

        DataLoadServices.runOffload(PSPDate.getPSPTime());
        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_empty_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 4/25", 7, cdls.size());

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 2", "atfextract/expected/test_backdated_0401_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");

        PayrollServices.beginUnitOfWork();
        cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 4/25", 7, cdls.size());
    }

    @Test
    public void testATF94xCurrentQuarterPayroll() throws Exception {
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-06-01"), new DateDTO("2011-05-25"), false);
        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 6, 2, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }

        DataLoadServices.runOffload(PSPDate.getPSPTime());
        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_currentquarter_backdate_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 6/1", 7, cdls.size());

        // Advance 7 days, run the ACH Transaction processor and make sure we do not extract liability data again.
        DataLoadServices.runACHTransactionProcessor();  // Advances PSP Time by 7 days.
        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_empty_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
    }

    @Test
    public void testATF94x_emptyOnNextUpdate() throws Exception {
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-06-01"), new DateDTO("2011-05-25"), false);
        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 6, 2, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 6, 1, 17, 15, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());
        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_currentquarter_backdate_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 6/1", 7, cdls.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 6, 2, 17, 15, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());
        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_empty_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 6/2", 7, cdls.size());

    }

    //todo Dawn add asserts
    @Test
    public void testATF94xPayrolls() throws Exception {
        String psid = "123456789";
        //set up company and create first payroll on 2011-04-01
        PayrollRunDTO payrollRunDTO1 = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"), false);
        PayrollServices.beginUnitOfWork();
       Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        List<Employee> emps = new ArrayList(employees);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addCOBRACompanyLaw(company);
        //submit second payroll on same day with cobra -
        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("61","6.1");
        lawAmounts.put("62","6.2");
        lawAmounts.put("63","6.3");
        lawAmounts.put("64","6.4");
        lawAmounts.put("66","6.6");
        lawAmounts.put("143","14.3");
        lawAmounts.put("1","10");
        lawAmounts.put("6","6");
        lawAmounts.put("67","6.7");
        lawAmounts.put("87","8.7");
        lawAmounts.put("142","14.2");
        lawAmounts.put("196","-19.6");

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRun(payrollRunDTO2, company, new DateDTO("2011-04-15"), emps, lawAmounts);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        //Run offload on 4/1
        DataLoadServices.runOffload(PSPDate.getPSPTime());

        //Submit liability adjustments on effective from 2011-04-01 on 2011-04-02
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 2, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, "1", null, new DateDTO("2011-04-01"), new SpcfMoney("179.10"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO("2011-04-01"), new SpcfMoney("150.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FUTA, FUTA, null, new DateDTO("2011-04-01"), new SpcfMoney("125.75"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult1 = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO("2011-04-01"), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, payrollRunDTO2.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO2, payrollRun2);
        PayrollServices.rollbackUnitOfWork();

        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 4, 5, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }
        //Submit third payroll for 2011-04-20 on 2011-04-05
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 = DataLoadServices.createPayrollRun(payrollRunDTO3, company, new DateDTO("2011-04-20"), emps, lawAmounts);

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun3 = PayrollRun.findPayrollRun(company, payrollRunDTO3.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO3, payrollRun3);
        PayrollServices.rollbackUnitOfWork();

        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 4, 10, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }
        //Submit fourth payroll for 2011-04-22 on 2011-04-10
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 10, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO4 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO4);
        PayrollRunDTO payrollDTO4 = DataLoadServices.createPayrollRun(payrollRunDTO4, company, new DateDTO("2011-04-22"), emps, lawAmounts);

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO4);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun4 = PayrollRun.findPayrollRun(company, payrollRunDTO4.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO4, payrollRun4);
        PayrollServices.rollbackUnitOfWork();

        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 4, 22, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }
        //Submit liability adjustments on effective from on fourth payroll run date 2011-04-22
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO2 = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs2 = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO2 = DataLoadServices.createLiabilityAdjustmentDTO(FIT, "1", null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-178.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs2.add(liabilityAdjustmentDTO2);
        liabilityAdjustmentDTO2 = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("15.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs2.add(liabilityAdjustmentDTO2);
        liabilityAdjustmentDTO2 = DataLoadServices.createLiabilityAdjustmentDTO(FUTA, FUTA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("15.75"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs2.add(liabilityAdjustmentDTO2);
        companyAdjustmentSubmissionDTO2.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs2);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO2 = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO2.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO2.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO2.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult2 = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO2, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO2);
        assertSuccess(processResult2);
        PayrollServices.commitUnitOfWork();
        //Run offload on 4/22
        DataLoadServices.runOffload(PSPDate.getPSPTime());

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString() + " 2011 2", "atfextract/expected/test_manyTypes_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 4/22", 27, cdls.size());

    }

    @Test
    public void testATF94xPayroll_hold_partialRecall() throws Exception {
        String psid = "123456789";
        //set up company and create first payroll on 2011-04-01
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"), false);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 4, 11, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }

        //Add hold on 4/11
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.FraudReview));
        assertEquals("Hold MMTs", 2, MoneyMovementTransaction.findTaxPayments().setCompany(company).setOnHold().setNonDirect().find().size());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process(null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 12, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        new CreateFilingsSpecificTransactions().updateCompanyDailyLiabilities();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 4/12", 0, cdls.size());

        PayrollServices.beginUnitOfWork();
        process = new ProcessMissedPayrolls();
        process.process(null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 13, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        new CreateFilingsSpecificTransactions().updateCompanyDailyLiabilities();
        cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        //Nothing in CDL
        assertEquals("All CDLs on 4/13", 0, cdls.size());

        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 4, 20, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(initdate);
            process = new ProcessMissedPayrolls();
            process.process(null);
            PayrollServices.commitUnitOfWork();
        }

        //on 4/20 recall partial payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 20, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(Company.findCompany(psid, SourceSystemCode.QBDT), payrollRunDTO.getPayrollTXBatchId());
        String recallPaycheckId = payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId();
        //Recall one paycheck from the payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(recallPaycheckId);
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        new CreateFilingsSpecificTransactions().updateCompanyDailyLiabilities();
        cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 4/20", 0, cdls.size());

        PayrollServices.beginUnitOfWork();
        process = new ProcessMissedPayrolls();
        process.process(null);
        PayrollServices.commitUnitOfWork();

        //On 4/21, come off hold, at which point we need to send to ATF
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 21, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.FraudReview));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());
        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_empty_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 4/21", 7, cdls.size());

        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString()+" 2011 2", "atfextract/expected/test_partialrecall_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
    }

    @Test
    public void testPayrollWithAcknowledgedByAgency() throws Exception {
        String psid = "123456789";
        SpcfCalendar payrollRunDate = SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(payrollRunDate);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-940-PAYMENT", DepositFrequencyCode.QUARTERLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("61","6.1");
        lawAmounts.put("62","6.2");
        lawAmounts.put("63","6.3");
        lawAmounts.put("64","6.4");
        lawAmounts.put("66","6.6");
        lawAmounts.put("1","10");
        lawAmounts.put("6","6");
        lawAmounts.put("67","6.7");
        lawAmounts.put("87","8.7");
        lawAmounts.put("142","14.2");

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-06-03"), emps, lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());
        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_ackedpayment_LIA_expected", "atfextract/expected/test_happyPath_empty_PAY_expected");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyDailyLiability> cdls = Application.find(CompanyDailyLiability.class);
        PayrollServices.commitUnitOfWork();

        assertEquals("All CDLs on 6/1", 6, cdls.size());

        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 6, 6, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 6, 7, 2, 15, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2011, 7, 28, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 7, 29, 2, 15, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        Assert.assertEquals("EftpsFile record not found.", 2, eftpsFiles.size());
        Assert.assertEquals("EftpsFile record not in PendingTransmission state.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());

        DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
        Assert.assertEquals("Payment Detail ", 2, payDetails.size());
        Assert.assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetails.get(0).getStatusCd());
        Assert.assertEquals("", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
        DomainEntitySet<FinancialTransaction> financialTransactions = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            Assert.assertEquals("FT State", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }
        PayrollServices.rollbackUnitOfWork();

        //override status as completed (i.e by passing FTP). This step is not seen in production.
        PayrollServices.beginUnitOfWork();
        EftpsFile eftFile = EftpsFile.getPendingTransmissionEftpsFiles().get(0);
        Assert.assertNotNull(eftFile);
        eftFile.setStatusCd(EdiFileStatus.Completed);
        Application.save(eftFile);
        PayrollServices.commitUnitOfWork();

        EftpsDataLoader.callSimulator();

        //Archive file. Only payment file(813) will be in completed status.
        EdiManager.archiveFiles();

        //process awaiting response file. i.e processing the files in TFA directory. this step will process responses/acknowledgements from TFA.
        EdiManager.processWaitingResponseFiles();

        //ARCHIVE COMPLETED RECORDS.
        EdiManager.archiveFiles();

        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        Assert.assertEquals("Pending payment files.", 2, eftpsFiles.size());

        //Check archived files.
        PayrollServices.beginUnitOfWork();
        Expression<EftpsFile> query = new Query<EftpsFile>()
                .Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.Archived))
                .OrderBy(EftpsFile.CreatedDate().Descending());
        eftpsFiles = Application.find(EftpsFile.class, query);

        Assert.assertEquals("No payment files.", 3, eftpsFiles.size());

        payDetails = EftpsPaymentDetail.findAllPaymentDetails().sort(EftpsPaymentDetail.StatusCd());
        Assert.assertEquals("Payment Details ", 2, payDetails.size());
        Assert.assertEquals("Payment status ", TaxPaymentStatus.AcknowledgedByAgency, payDetails.get(0).getStatusCd());
        Assert.assertEquals("", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
        financialTransactions = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            Assert.assertEquals("FT State", TransactionStateCode.Completed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }
        PayrollServices.commitUnitOfWork();


        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData.toString(), "atfextract/expected/test_happyPath_empty_LIA_expected", "atfextract/expected/test_ackedpayment_PAY_expected");
    }
    
    @Test
    public void testAchStatePaymentExtractAfterOffload() throws Exception {
        String[] states = {"GA", "IL"};
        DataLoadServices.setPSPDate(2012, 1, 1);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());


        List<Company> companies = DataLoadServices.setupCompany(1l, 1, states, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("61","6.1");
        lawAmounts.put("62","6.2");
        lawAmounts.put("63","6.3");
        lawAmounts.put("64","6.4");
        lawAmounts.put("66","6.6");
        lawAmounts.put("1","10");

        DataLoadServices.setPSPDate(2012, 4, 2);
        for (Company company : companies) {
            DataLoadServices.enrollEFTPS(company);
            DataLoadServices.runPayrollRun(company, states, lawAmounts, new DateDTO("2012-04-05"));
        }

        DataLoadServices.setPSPDate(2012, 4, 3);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 4, 10);
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        SpcfCalendar initiationDate = SpcfCalendar.createInstance(2012, 5, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(initiationDate, - achTaxOffloadOffset);
        DataLoadServices.setPSPDate(2012, 5, initiationDate.getDay());
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2012, 5, 30);
        TestCreateFilingsSpecificTransactions.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData.toString() + " 2012 2", "atfextract/expected/test_happyPath_03242012_LIA_expected", "atfextract/expected/test_happyPath_State_payments_05302012_expected");
    }
    
}
