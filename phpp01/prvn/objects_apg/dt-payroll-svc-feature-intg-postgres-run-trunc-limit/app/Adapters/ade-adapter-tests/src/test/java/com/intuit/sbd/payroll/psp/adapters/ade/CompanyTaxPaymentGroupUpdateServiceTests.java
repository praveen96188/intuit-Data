package com.intuit.sbd.payroll.psp.adapters.ade;

import com.intuit.ems.cep.api.ResourceNameEnum;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.api.messages.Message;
import com.intuit.ems.cep.api.messages.MessageCode;
import com.intuit.ems.cep.company.v1.service.Expand;
import com.intuit.ems.cep.company.v1.service.ShowHistory;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxPaymentGroupUpdateServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxSetupGetServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.ServiceFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.AdditionalFilingIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.FrequencyMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.RateConverterFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.TaxPaymentGroupIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.DateUtil;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXRequestGenerator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLRS;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.MoneyMovementControlUtil;
import com.intuit.schema.payroll.v3.common.FrequencyEnum;
import com.intuit.schema.payroll.v3.company.AdditionalFilingAmount;
import com.intuit.schema.payroll.v3.company.TaxDepositFrequency;
import com.intuit.schema.payroll.v3.company.TaxPaymentGroup;
import com.intuit.schema.payroll.v3.company.TaxSetup;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: shivanandad069
 * Date: 11/6/13
 */
public class CompanyTaxPaymentGroupUpdateServiceTests {
    final static String COMPANY_ID = "19670404";
    final static String COMPANY_NAME = "SHIVA TEST ADE";

    private static final ArrayList<String> supportedPaymentTemplates = new ArrayList<String>();

    static {
        supportedPaymentTemplates.add("IRS-940-PAYMENT");
        supportedPaymentTemplates.add("IRS-941-PAYMENT");
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 1, 20, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        for (String agencyId : supportedPaymentTemplates) {
            DataLoadServices.updatePaymentTemplateSupportedDate(agencyId, SpcfCalendar.createInstance(2005, 1, 1));
        }
    }

    @BeforeClass
    public static void beforeClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void test_CO_DFTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("CO", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_CO_DR1094_PAYMENT", "US_CO", "US_CO_DOR", "CO-DR1094-PAYMENT", FrequencyEnum.QUARTERLY);
        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_CO_DFTest_ObsoleteDf() throws Throwable {
        createAssistedCompanyWithRates("CO", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_CO_DR1094_PAYMENT", "US_CO", "US_CO_DOR", "CO-DR1094-PAYMENT", FrequencyEnum.QUARTER_MONTHLY);
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_CO_Invalid_DFTest() throws Throwable {
        createCOCompanyWithInactiveDF();
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_CO_DR1094_PAYMENT", "US_CO", "US_CO_DOR", "CO-DR1094-PAYMENT", FrequencyEnum.QUARTERLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(COMPANY_ID, SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void test_CT_DFTest_ObsoleteDf() throws Throwable {
        createAssistedCompanyWithRates("CT", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_CT_CTWH_PAYMENT", "US_CT", "US_CT_DRS", "CT-CTWH-PAYMENT", FrequencyEnum.ANNUAL);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_CT_DFTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("CT", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_CT_CTWH_PAYMENT", "US_CT", "US_CT_DRS", "CT-CTWH-PAYMENT", FrequencyEnum.MONTHLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void test_GA_DFTest_ObsoleteDf() throws Throwable {
        createAssistedCompanyWithRates("GA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_GA_GAV_PAYMENT", "US_GA", "US_GA_DOR", "GA-GAV-PAYMENT", FrequencyEnum.SEMI_MONTHLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_GA_DFTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("GA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_GA_GAV_PAYMENT", "US_GA", "US_GA_DOR", "GA-GAV-PAYMENT", FrequencyEnum.QUARTERLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_GA_DFTest_Threshold_LessFrequent() throws Throwable {
        try {
            MoneyMovementControlUtil.setSkipValidation(true);
            Company company = createAssistedCompanyWithRatesWithDD("GA", COMPANY_ID, "987654332", "311076-05-1");
            DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
            DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
            updateGAPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

            DataLoadServices.activateDDService(company);
            DataLoadServices.activateTaxServiceExceptBalanceFile(company);
            DataLoadServices.setPSPDate(2014, 1, 1);
            company = sendBalanceFileAndRunPayroll(company, false, true);
            DataLoadServices.setPSPDate(2014, 1, 2);
            TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroupWithPSPDate("US_GA_GAV_PAYMENT", "US_GA", "US_GA_DOR", "GA-GAV-PAYMENT", FrequencyEnum.MONTHLY);

            SpcfCalendar eventStartDate = PSPDate.getPSPTime();
            ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
            assertNotNull(taxPaymentGroupResult);
            assertFalse(taxPaymentGroupResult.isSuccess());
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
            assertEquals("Company Events", 0, companyEventsList.size());
            assertEquals("Resposne taxPaymentGroup", null, taxPaymentGroupResult.getResult());
            assertNotSame("Error messages", null, taxPaymentGroupResult.getErrorMessages());
            boolean thresholdMessageReceived = false;
            for (int i = 0; i < taxPaymentGroupResult.getErrorMessages().size(); i++) {
                if ("Skip - Threshold Met".equals(taxPaymentGroupResult.getErrorMessages().get(i).getMessage())) {
                    thresholdMessageReceived = true;
                    break;
                }
            }
            assertTrue("Threshold message not sent in response", thresholdMessageReceived);
            PayrollServices.rollbackUnitOfWork();
        } finally {
            MoneyMovementControlUtil.setSkipValidation(false);
        }
    }

    @Test
    public void test_GA_DFTest_Threshold_MoreFrequent() throws Throwable {
        Company company = createAssistedCompanyWithRatesWithDD("GA", COMPANY_ID, "987654332", "311076-05-1");
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        updateGAPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.setPSPDate(2014, 1, 1);
        company = sendBalanceFileAndRunPayroll(company, false, true);
        DataLoadServices.setPSPDate(2014, 1, 2);
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroupWithPSPDate("US_GA_GAV_PAYMENT", "US_GA", "US_GA_DOR", "GA-GAV-PAYMENT", FrequencyEnum.NEXT_BANKING_DAY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_GA_DFTest_Threshold_Voided() throws Throwable {
        DataLoadServices.setPSPDate(2014, 1, 1);
        String statesList[] = new String[]{"GA"};
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("12", "50001");
        List<Company> companies = DataLoadServices.setupCompany(Long.parseLong(COMPANY_ID), 1, statesList, PaymentTemplateCategory.Withholding);
        Company company = companies.get(0);
        testPayrollsOverStateThreshold(statesList[0], company, lawAmounts);
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        lawAmounts.put("12", "51");
        testPayrollsOverStateThreshold(statesList[0], company, lawAmounts);
        DataLoadServices.setPSPDate(2014, 1, 4);
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroupWithPSPDate("US_GA_GAV_PAYMENT", "US_GA", "US_GA_DOR", "GA-GAV-PAYMENT", FrequencyEnum.SEMI_WEEKLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.rollbackUnitOfWork();
    }

    //Currently its created for GA and IRS
    private Company sendBalanceFileAndRunPayroll(Company pCompany, boolean runOnlyPayroll, boolean runToExceedThreshold) {
        if (pCompany == null) {
            return null;
        }
        String pPsid = pCompany.getSourceCompanyId();
        OFX ofx = OFXRequestGenerator.generateBalanceFile(pPsid, false, false, false, false, false, "GA");
        if (!runOnlyPayroll) {
            QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
            pCompany = DataLoadServices.refreshCompany(pCompany);
        }


        Date chekDate = new Date(PSPDate.getPSPTime().getTimeInMilliseconds());
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               chekDate,
                                                               chekDate,
                                                               chekDate,
                                                               false));
        if (runToExceedThreshold) {
            for (IPAYROLLRUN payrollRun : payrollRuns) {
                for (IPAYCHK ipaychk : payrollRun.getIPAYCHK()) {
                    for (ITAXLINE itaxline : ipaychk.getITAXLINE()) {
                        if (itaxline.getIAMT().contains("-")) {
                            itaxline.setIAMT("$-" + itaxline.getIPITEMID() + "0000.00");
                        } else {
                            itaxline.setIAMT("$" + itaxline.getIPITEMID() + "0000.00");
                        }
                    }
                }
            }
        }


        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(pPsid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(pCompany, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        return pCompany;
    }

    @Test
    public void test_IA_DFTest_ObsoleteDf() throws Throwable {
        createAssistedCompanyWithRates("IA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_IA_44105_PAYMENT", "US_IA", "US_IA_DOR", "IA-44105-PAYMENT", FrequencyEnum.SEMI_WEEKLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));

        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_IA_DFTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("IA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_IA_44105_PAYMENT", "US_IA", "US_IA_DOR", "IA-44105-PAYMENT", FrequencyEnum.QUARTERLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));

        assertNotNull(taxPaymentGroupResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_IL_DFTest_ObsoleteDf() throws Throwable {
        createAssistedCompanyWithRates("IL", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_IL_501_PAYMENT", "US_IL", "US_IL_DOR", "IL-501-PAYMENT", FrequencyEnum.SEMI_MONTHLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));

        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_IL_DFTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("IL", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_IL_501_PAYMENT", "US_IL", "US_IL_DOR", "IL-501-PAYMENT", FrequencyEnum.SEMI_WEEKLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertTrue(taxPaymentGroupResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_KS_DFTest_ObsoleteDf() throws Throwable {
        createAssistedCompanyWithRates("KS", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_KS_KW5_PAYMENT", "US_KS", "US_KS_DOR", "KS-KW5-PAYMENT", FrequencyEnum.WEEKLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));

        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_KS_DFTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("KS", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_KS_KW5_PAYMENT", "US_KS", "US_KS_DOR", "KS-KW5-PAYMENT", FrequencyEnum.MONTHLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));

        assertNotNull(taxPaymentGroupResult);
        assertTrue(taxPaymentGroupResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_MA_DFTest_ObsoleteDf() throws Throwable {
        createAssistedCompanyWithRates("MA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_MA_M941_PAYMENT", "US_MA", "US_MA_DOR", "MA-M941-PAYMENT", FrequencyEnum.WEEKLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));

        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_MA_DFTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("MA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_MA_M941_PAYMENT", "US_MA", "US_MA_DOR", "MA-M941-PAYMENT", FrequencyEnum.QUARTERLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertTrue(taxPaymentGroupResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_MO_DFTest_NotExists() throws Throwable {
        createAssistedCompanyWithRates("MO", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_MO_941_PAYMENT", "US_MO", "US_MO_DOR", "MO-941-PAYMENT", FrequencyEnum.SEMI_MONTHLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));

        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_MO_DFTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("MO", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_MO_941_PAYMENT", "US_MO", "US_MO_DOR", "MO-941-PAYMENT", FrequencyEnum.QUARTERLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertTrue(taxPaymentGroupResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_OH_DFTest_ObsoleteDf() throws Throwable {
        createAssistedCompanyWithRates("OH", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_OH_IT501_PAYMENT", "US_OH", "US_OH_DOT", "OH-IT501-PAYMENT", FrequencyEnum.SEMI_MONTHLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_OH_DFTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("OH", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_OH_IT501_PAYMENT", "US_OH", "US_OH_DOT", "OH-IT501-PAYMENT", FrequencyEnum.QUARTERLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertTrue(taxPaymentGroupResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_WI_DFTest_NotExists() throws Throwable {
        createAssistedCompanyWithRates("WI", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_WI_WT6_PAYMENT", "US_WI", "US_WI_DOR", "WI-WT6-PAYMENT", FrequencyEnum.SEMI_WEEKLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_WI_DFTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("WI", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_WI_WT6_PAYMENT", "US_WI", "US_WI_DOR", "WI-WT6-PAYMENT", FrequencyEnum.ANNUAL);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertTrue(taxPaymentGroupResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    // Note - Jurisdiction Id is not used to update DF, so no need to validate it
    @Test
    public void test_VA_DFTest_InvalidJurisdictionId() throws Throwable {
        Company company = createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA_INVALID", "US_VA_DOT", "VA-VA15-PAYMENT", FrequencyEnum.MONTHLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertTrue(taxPaymentGroupResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_VA_DFTest_NonSupportedFrequency() throws Throwable {
        createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA", "US_VA_DOT", "VA-VA15-PAYMENT", FrequencyEnum.WEEKLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_VA_DFTest_ObsoleteDf() throws Throwable {
        createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA", "US_VA_DOT", "VA-VA15-PAYMENT", FrequencyEnum.SEMI_MONTHLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_VA_DFTest_AlreadyExistsFrequency() throws Throwable {
        Company company = createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("VA-VA15-PAYMENT");
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, PSPDate.getPSPTime());
        assertEquals("Existing DF", DepositFrequencyCode.SEMIWEEKLY, effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
        PayrollServices.rollbackUnitOfWork();

        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA", "US_VA_DOT", "VA-VA15-PAYMENT", FrequencyEnum.SEMI_WEEKLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertTrue(taxPaymentGroupResult.isSuccess());  // Return success response without adding new DF

        PayrollServices.beginUnitOfWork();
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, PSPDate.getPSPTime());
        assertEquals("Existing DF", DepositFrequencyCode.SEMIWEEKLY, effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_VA_DFTest_InvalidPaymentTemplateId() throws Throwable {
        createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT_INVALID", "US_VA", "US_VA_DOT", "VA-VA15-PAYMENT", FrequencyEnum.SEMI_WEEKLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.InvalidValue.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_VA_DFTest_PaymentTemplateNotExist() throws Throwable {
        createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_CO_DR1094_PAYMENT", "US_VA", "US_CO_DOR", "VA-VA15-PAYMENT", FrequencyEnum.SEMI_WEEKLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    //Testing for different agency id other that what payment template belongs too, setting up other state
    @Test
    public void test_VA_DFTest_DifferentAgencyId() throws Throwable {
        Company company = createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        DataLoadServices.addCompanyLawsWithAgencyId("123456", company, "CO");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA", "US_CO_DOR", "VA-VA15-PAYMENT", FrequencyEnum.SEMI_WEEKLY);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.InvalidValue.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_VA_DFTest_NoAgencyId() throws Throwable {
        Company company = createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA", null, "VA-VA15-PAYMENT", FrequencyEnum.MONTHLY);
        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertTrue(taxPaymentGroupResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_VA_DFTest_NoEffectiveDate() throws Throwable {
        createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA", "US_VA_DOT", "VA-VA15-PAYMENT", FrequencyEnum.SEMI_WEEKLY);
        java.util.List<com.intuit.schema.payroll.v3.company.TaxDepositFrequency> dfList = taxPaymentGroup.getDepositFrequencies();
        TaxDepositFrequency TaxDepositFrequency = dfList.get(0);
        TaxDepositFrequency.setStartDate(null);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.NullProperty.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_VA_DFTest_NoFrequency() throws Throwable {
        createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA", "US_VA_DOT", "VA-VA15-PAYMENT", FrequencyEnum.SEMI_WEEKLY);
        java.util.List<com.intuit.schema.payroll.v3.company.TaxDepositFrequency> dfList = taxPaymentGroup.getDepositFrequencies();
        TaxDepositFrequency TaxDepositFrequency = dfList.get(0);
        TaxDepositFrequency.setFrequency(null);

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.NullProperty.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void test_VA_DFTest_InActiveLaw() throws Throwable {
        Company company = createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "VADOT");
        for (CompanyLaw claw : companyAgency.getCompanyLawCollection()) {
            // Set VA SWT to Inactive.
            if (claw.getLaw().getLawId().equals("48")) {
                Application.refresh(claw);
                claw.setStatus(PayrollItemStatus.Inactive);
                claw.setFilingStatus(PayrollItemStatus.Inactive);
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA", "US_VA_DOT", "VA-VA15-PAYMENT", FrequencyEnum.MONTHLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());

        boolean inActiveLawMessageReceived = false;
        for (int i = 0; i < taxPaymentGroupResult.getErrorMessages().size(); i++) {
            if ("Skip - No active Laws".equals(taxPaymentGroupResult.getErrorMessages().get(i).getMessage())) {
                inActiveLawMessageReceived = true;
                break;
            }
        }
        assertTrue("No active Laws message not sent in response", inActiveLawMessageReceived);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 0, companyEventsList.size());
        assertEquals("Resposne taxPaymentGroup", null, taxPaymentGroupResult.getResult());
        assertNotSame("Error messages", null, taxPaymentGroupResult.getErrorMessages());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_VA_DFTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA", "US_VA_DOT", "VA-VA15-PAYMENT", FrequencyEnum.MONTHLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertTrue(taxPaymentGroupResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_VA_DFTest_TimeZoneTest() throws Throwable {
        Company company = createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA", "US_VA_DOT", "VA-VA15-PAYMENT", FrequencyEnum.MONTHLY);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertTrue(taxPaymentGroupResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroup.getDepositFrequencies().get(0).getStartDate();

        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()));
        EffectiveDepositFrequency mExistingEffectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, SpcfCalendar.createInstance(startDate.getTime(), SpcfTimeZone.getLocalTimeZone()));
        SpcfCalendar spcfStartDate = CalendarUtils.convertToSpcfCalendar(startDate);
        Date effectiveDateUpdated = new Date(spcfStartDate.getTimeInMilliseconds());
        SpcfCalendar spcfeffectiveDateUpdated = CalendarUtils.convertToSpcfCalendar(effectiveDateUpdated);
        assertEquals("Effective Date updated ", spcfStartDate, spcfeffectiveDateUpdated);
        assertEquals("Deposit Frequency updated ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     mExistingEffectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().toString());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date in event", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency in event", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroup.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_VA_DFThreshold() throws Throwable {
        Company company = createAssistedCompanyWithRates("VA", COMPANY_ID, "987654332", "311076-05-1");
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "VADOT");
        PaymentTemplate paymentTemplate = null;
        for (CompanyLaw claw : companyAgency.getCompanyLawCollection()) {
            // Set CA SUI_ER to Exempt.
            if (claw.getLaw().getLawId().equals("48")) {
                paymentTemplate = claw.getLaw().getPaymentTemplate();
            }
        }
        //Create threshold event
        CompanyEvent.createThresholdExceededEvent(createPayrollRun(company), paymentTemplate, PSPDate.getPSPTime());

        PayrollServices.commitUnitOfWork();
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_VA_VA15_PAYMENT", "US_VA", "US_VA_DOT", "VA-VA15-PAYMENT", FrequencyEnum.MONTHLY);
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.GenericValidationMessage.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_MO_Fed_Assessment_Happy_Path() throws Throwable {
        DataLoadServices.setPSPDate(2014, 5, 2);
        Company company = createAssistedCompanyWithRates("MO", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = createTaxPaymentGroup("US_MO_MODES_PAYMENT", "US_MO", "US_MO_DES", "MO-MODES-PAYMENT");

        com.intuit.schema.payroll.v3.company.AdditionalFilingAmount additionalFilingAmount = new AdditionalFilingAmount();
        additionalFilingAmount.setAmount(new BigDecimal(100));
        additionalFilingAmount.setId("US_MO_SC_ER_FIA");
        additionalFilingAmount.setEffectiveDate(new Date(SpcfCalendar.createInstance(2014, 4, 1, SpcfTimeZone.getLocalTimeZone()).getTimeInMilliseconds()));
        taxPaymentGroup.getAdditionalFilingAmounts().add(additionalFilingAmount);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult<TaxPaymentGroup> taxPaymentGroupResult = ServiceFactory.getInstance().<TaxPaymentGroup, TaxPaymentGroupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertTrue(taxPaymentGroupResult.toString(), taxPaymentGroupResult.isSuccess());
        assertNotNull(taxPaymentGroupResult.getResult());

        TaxPaymentGroup responseGroup = taxPaymentGroupResult.getResult();
        assertEquals(1, responseGroup.getAdditionalFilingAmounts().size());
        for (AdditionalFilingAmount filingAmount : responseGroup.getAdditionalFilingAmounts()) {
            assertEquals(additionalFilingAmount.getId(), filingAmount.getId());
            assertEquals(additionalFilingAmount.getAmount().setScale(2, RoundingMode.HALF_UP), filingAmount.getAmount().setScale(2, RoundingMode.HALF_UP));
            assertEquals(additionalFilingAmount.getEffectiveDate(), filingAmount.getEffectiveDate());
        }

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.AdditionalFilingAmount, null, eventStartDate, PSPDate.getPSPTime()));
        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));

        DomainEntitySet<CompanyFilingAmount> companyFilingAmounts = Application.find(CompanyFilingAmount.class);
        assertEquals(2, companyFilingAmounts.size());

        for (CompanyFilingAmount companyFilingAmount : companyFilingAmounts) {
            assertEquals(AdditionalFilingIdMapper.getPspAtfLawIdByComplianceAdditionalId(additionalFilingAmount.getId()), companyFilingAmount.getAdditionalFilingAmount().getATFLawId());
            if (companyFilingAmount.getEffectiveDate().compareTo(SpcfCalendar.createInstance(2014, 4, 1, SpcfTimeZone.getLocalTimeZone())) == 0) {
                assertEquals(new BigDecimal(100), new BigDecimal(companyFilingAmount.getAmount()));
            } else if (companyFilingAmount.getEffectiveDate().compareTo(SpcfCalendar.createInstance(2014, 7, 1, SpcfTimeZone.getLocalTimeZone())) == 0) {
                assertEquals(BigDecimal.ZERO, new BigDecimal(companyFilingAmount.getAmount()));
            } else {
                fail("Invalid company filing amount. " + companyFilingAmount);
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_COSUICredit_HappyPath() throws Throwable {
        DataLoadServices.setPSPDate(2014, 5, 2);
        Company company = createAssistedCompanyWithRates("CO", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = createTaxPaymentGroup("US_CO_UITR1_PAYMENT", "US_CO", "US_CO_DLE", "CO-UITR1-PAYMENT");

        com.intuit.schema.payroll.v3.company.AdditionalFilingAmount additionalFilingAmount = new AdditionalFilingAmount();
        additionalFilingAmount.setAmount(new BigDecimal(100));
        additionalFilingAmount.setId("US_CO_SUI_Credit");
        additionalFilingAmount.setEffectiveDate(new Date(SpcfCalendar.createInstance(2014, 4, 1, SpcfTimeZone.getLocalTimeZone()).getTimeInMilliseconds()));
        taxPaymentGroup.getAdditionalFilingAmounts().add(additionalFilingAmount);

        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult<TaxPaymentGroup> taxPaymentGroupResult = ServiceFactory.getInstance().<TaxPaymentGroup, TaxPaymentGroupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertTrue(taxPaymentGroupResult.toString(), taxPaymentGroupResult.isSuccess());
        assertNotNull(taxPaymentGroupResult.getResult());

        TaxPaymentGroup responseGroup = taxPaymentGroupResult.getResult();
        assertEquals(1, responseGroup.getAdditionalFilingAmounts().size());
        for (AdditionalFilingAmount filingAmount : responseGroup.getAdditionalFilingAmounts()) {
            assertEquals(additionalFilingAmount.getId(), filingAmount.getId());
            assertEquals(additionalFilingAmount.getAmount().setScale(2, RoundingMode.HALF_UP), filingAmount.getAmount().setScale(2, RoundingMode.HALF_UP));
            assertEquals(additionalFilingAmount.getEffectiveDate(), filingAmount.getEffectiveDate());
        }

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.AdditionalFilingAmount, null, eventStartDate, PSPDate.getPSPTime()));
        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroup.getId()),
                     companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));

        DomainEntitySet<CompanyFilingAmount> companyFilingAmounts = Application.find(CompanyFilingAmount.class);
        assertEquals(2, companyFilingAmounts.size());

        for (CompanyFilingAmount companyFilingAmount : companyFilingAmounts) {
            assertEquals(AdditionalFilingIdMapper.getPspAtfLawIdByComplianceAdditionalId(additionalFilingAmount.getId()), companyFilingAmount.getAdditionalFilingAmount().getATFLawId());
            if (companyFilingAmount.getEffectiveDate().compareTo(SpcfCalendar.createInstance(2014, 4, 1, SpcfTimeZone.getLocalTimeZone())) == 0) {
                assertEquals(new BigDecimal(100), new BigDecimal(companyFilingAmount.getAmount()));
            } else if (companyFilingAmount.getEffectiveDate().compareTo(SpcfCalendar.createInstance(2014, 7, 1, SpcfTimeZone.getLocalTimeZone())) == 0) {
                assertEquals(BigDecimal.ZERO, new BigDecimal(companyFilingAmount.getAmount()));
            } else {
                fail("Invalid company filing amount. " + companyFilingAmount);
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_AdditionalAmountValidation() throws Throwable {
        DataLoadServices.setPSPDate(2014, 5, 2);
        createAssistedCompanyWithRates("MO", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = createTaxPaymentGroup("US_MO_MODES_PAYMENT", "US_MO", "US_MO_DES", "MO-MODES-PAYMENT");

        // null id
        com.intuit.schema.payroll.v3.company.AdditionalFilingAmount additionalFilingAmount = new AdditionalFilingAmount();
        additionalFilingAmount.setAmount(new BigDecimal(100));
        additionalFilingAmount.setId(null);
        additionalFilingAmount.setEffectiveDate(new Date(SpcfCalendar.createInstance(2014, 4, 1, SpcfTimeZone.getLocalTimeZone()).getTimeInMilliseconds()));
        taxPaymentGroup.getAdditionalFilingAmounts().add(additionalFilingAmount);

        ServiceResult<TaxPaymentGroup> taxPaymentGroupResult = ServiceFactory.getInstance().<TaxPaymentGroup, TaxPaymentGroupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());

        assertEquals(1, taxPaymentGroupResult.getMessages().size());

        Message message = taxPaymentGroupResult.getMessages().get(0);
        assertEquals("Property Id cannot be null.", message.getMessage());

        // invalid id
        taxPaymentGroup.getAdditionalFilingAmounts().clear();
        additionalFilingAmount = new AdditionalFilingAmount();
        additionalFilingAmount.setAmount(new BigDecimal(100));
        additionalFilingAmount.setId("Invalid");
        additionalFilingAmount.setEffectiveDate(new Date(SpcfCalendar.createInstance(2014, 4, 1, SpcfTimeZone.getLocalTimeZone()).getTimeInMilliseconds()));
        taxPaymentGroup.getAdditionalFilingAmounts().add(additionalFilingAmount);

        taxPaymentGroupResult = ServiceFactory.getInstance().<TaxPaymentGroup, TaxPaymentGroupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());

        assertEquals(1, taxPaymentGroupResult.getMessages().size());

        message = taxPaymentGroupResult.getMessages().get(0);
        assertEquals("Invalid is not a valid value for property Id", message.getMessage());

        // null amount
        taxPaymentGroup.getAdditionalFilingAmounts().clear();
        additionalFilingAmount = new AdditionalFilingAmount();
        additionalFilingAmount.setAmount(null);
        additionalFilingAmount.setId("US_MO_SC_ER_FIA");
        additionalFilingAmount.setEffectiveDate(new Date(SpcfCalendar.createInstance(2014, 4, 1, SpcfTimeZone.getLocalTimeZone()).getTimeInMilliseconds()));
        taxPaymentGroup.getAdditionalFilingAmounts().add(additionalFilingAmount);

        taxPaymentGroupResult = ServiceFactory.getInstance().<TaxPaymentGroup, TaxPaymentGroupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());

        assertEquals(1, taxPaymentGroupResult.getMessages().size());

        message = taxPaymentGroupResult.getMessages().get(0);
        assertEquals("Property Amount cannot be null.", message.getMessage());

        // negative amount
        taxPaymentGroup.getAdditionalFilingAmounts().clear();
        additionalFilingAmount = new AdditionalFilingAmount();
        additionalFilingAmount.setAmount(new BigDecimal(-100));
        additionalFilingAmount.setId("US_MO_SC_ER_FIA");
        additionalFilingAmount.setEffectiveDate(new Date(SpcfCalendar.createInstance(2014, 4, 1, SpcfTimeZone.getLocalTimeZone()).getTimeInMilliseconds()));
        taxPaymentGroup.getAdditionalFilingAmounts().add(additionalFilingAmount);

        taxPaymentGroupResult = ServiceFactory.getInstance().<TaxPaymentGroup, TaxPaymentGroupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());

        assertEquals(1, taxPaymentGroupResult.getMessages().size());

        message = taxPaymentGroupResult.getMessages().get(0);
        assertEquals("-100 is not a valid value for property Amount", message.getMessage());

        // null effective date
        taxPaymentGroup.getAdditionalFilingAmounts().clear();
        additionalFilingAmount = new AdditionalFilingAmount();
        additionalFilingAmount.setAmount(new BigDecimal(100));
        additionalFilingAmount.setId("US_MO_SC_ER_FIA");
        additionalFilingAmount.setEffectiveDate(null);
        taxPaymentGroup.getAdditionalFilingAmounts().add(additionalFilingAmount);

        taxPaymentGroupResult = ServiceFactory.getInstance().<TaxPaymentGroup, TaxPaymentGroupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());

        assertEquals(1, taxPaymentGroupResult.getMessages().size());

        message = taxPaymentGroupResult.getMessages().get(0);
        assertEquals("Property EffectiveDate cannot be null.", message.getMessage());

        // invalid effective date
        taxPaymentGroup.getAdditionalFilingAmounts().clear();
        additionalFilingAmount = new AdditionalFilingAmount();
        additionalFilingAmount.setAmount(new BigDecimal(100));
        additionalFilingAmount.setId("US_MO_SC_ER_FIA");
        additionalFilingAmount.setEffectiveDate(new Date(SpcfCalendar.createInstance(2014, 5, 1, SpcfTimeZone.getLocalTimeZone()).getTimeInMilliseconds()));
        taxPaymentGroup.getAdditionalFilingAmounts().add(additionalFilingAmount);

        taxPaymentGroupResult = ServiceFactory.getInstance().<TaxPaymentGroup, TaxPaymentGroupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());

        assertEquals(1, taxPaymentGroupResult.getMessages().size());

        message = taxPaymentGroupResult.getMessages().get(0);
        assertEquals("Effective date must be the first day of a quarter", message.getMessage());
    }

     // Test AZ tax payment group DF update is not allowed because AZ tax payment group that follows federal
    @Test
    public void test_AZ_DFUpdateTest() throws Throwable {
        String[] states = {"AZ"};
        assertOne(DataLoadServices.setupCompany(Long.parseLong(COMPANY_ID), 1, states, PaymentTemplateCategory.Withholding));

        TaxSetupGetServiceParams taxSetupGetServiceParams = new TaxSetupGetServiceParams();
        taxSetupGetServiceParams.setCompanyId(COMPANY_ID);
        taxSetupGetServiceParams.setExpand(Expand.TAXSETUP.toString());
        TaxSetup taxSetup = (TaxSetup) ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetupGetServiceParams).getResult();
        assertNotNull(taxSetup);
        assertEquals("Tax Jurisdictions", 3, taxSetup.getTaxJurisdictions().size());
        assertEquals("Tax Payment groups", 4, taxSetup.getTaxPaymentGroups().size());
        for (TaxPaymentGroup taxPaymentGroup : taxSetup.getTaxPaymentGroups()) {
            if (taxPaymentGroup.getId().equals("US_IRS_941_PAYMENT") || taxPaymentGroup.getId().equals("US_AZ_A1_PAYMENT")) {
                assertEquals("Deposit frequencies", 1, taxPaymentGroup.getDepositFrequencies().size());
                assertEquals("Deposit frequency", FrequencyEnum.QUARTERLY, taxPaymentGroup.getDepositFrequencies().get(0).getFrequency());
            } else if (taxPaymentGroup.getId().equals("US_IRS_940_PAYMENT")) {
                assertEquals("Deposit frequencies", 1, taxPaymentGroup.getDepositFrequencies().size());
                assertEquals("Deposit frequency", FrequencyEnum.QUARTERLY, taxPaymentGroup.getDepositFrequencies().get(0).getFrequency());
            }
        }
        TaxPaymentGroup taxPaymentGroupToUpdate = getTaxPaymentGroup("US_AZ_A1_PAYMENT", "US_AZ", "US_AZ_DOR", "AZ-A1-PAYMENT", FrequencyEnum.MONTHLY);
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroupToUpdate, getServiceParams(COMPANY_ID));
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertNull(taxPaymentGroupResult.getResult());
        assertEquals("Errors", 1, taxPaymentGroupResult.getMessages().size());
        assertEquals("Error message code", MessageCode.GenericValidationMessage.getMessageCode(), taxPaymentGroupResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Error message", "Deposit frequency update is not allowed on TaxPaymentGroup: US_AZ_A1_PAYMENT because this uses the frequency of TaxPaymentGroup: US_IRS_941_PAYMENT", taxPaymentGroupResult.getErrorMessages().get(0).getMessage());

    }

    // Test Federal deposit frequency update and also verify deposit frequency of tax payment group that follows federal
    @Test
    public void test_Federal_AZ_DFTest() throws Throwable {
        String[] states = {"AZ"};
        Company company = assertOne(DataLoadServices.setupCompany(Long.parseLong(COMPANY_ID), 1, states, PaymentTemplateCategory.Withholding));

        TaxSetupGetServiceParams taxSetupGetServiceParams = new TaxSetupGetServiceParams();
        taxSetupGetServiceParams.setCompanyId(COMPANY_ID);
        taxSetupGetServiceParams.setExpand(Expand.TAXSETUP.toString());
        TaxSetup taxSetup = (TaxSetup) ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetupGetServiceParams).getResult();
        assertNotNull(taxSetup);
        assertEquals("Tax Jurisdictions", 3, taxSetup.getTaxJurisdictions().size());
        assertEquals("Tax Payment groups", 4, taxSetup.getTaxPaymentGroups().size());
        for (TaxPaymentGroup taxPaymentGroup : taxSetup.getTaxPaymentGroups()) {
            if (taxPaymentGroup.getId().equals("US_IRS_941_PAYMENT") || taxPaymentGroup.getId().equals("US_AZ_A1_PAYMENT")) {
                assertEquals("Deposit frequencies", 1, taxPaymentGroup.getDepositFrequencies().size());
                assertEquals("Deposit frequency", FrequencyEnum.QUARTERLY, taxPaymentGroup.getDepositFrequencies().get(0).getFrequency());
            } else if (taxPaymentGroup.getId().equals("US_IRS_940_PAYMENT")) {
                assertEquals("Deposit frequencies", 1, taxPaymentGroup.getDepositFrequencies().size());
                assertEquals("Deposit frequency", FrequencyEnum.QUARTERLY, taxPaymentGroup.getDepositFrequencies().get(0).getFrequency());
            }
        }
        TaxPaymentGroup taxPaymentGroupToUpdate = getTaxPaymentGroup("US_IRS_941_PAYMENT", "US_FEDERAL", "US_FEDERAL_IRS", "IRS-941-PAYMENT", FrequencyEnum.MONTHLY);
        SpcfCalendar eventStartDate = PSPDate.getPSPTime();
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroupToUpdate, getServiceParams(COMPANY_ID));
        assertTrue(taxPaymentGroupResult.isSuccess());
        TaxPaymentGroup updatedTaxPaymentGroup = (TaxPaymentGroup) taxPaymentGroupResult.getResult();
        assertEquals("US_IRS_941_PAYMENT", updatedTaxPaymentGroup.getId());
        assertEquals("Deposit frequencies", 1, updatedTaxPaymentGroup.getDepositFrequencies().size());
        assertEquals("Deposit frequency", FrequencyEnum.MONTHLY, updatedTaxPaymentGroup.getDepositFrequencies().get(0).getFrequency());

        taxSetup = (TaxSetup) ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetupGetServiceParams).getResult();
        assertNotNull(taxSetup);
        assertEquals("Tax Jurisdictions", 3, taxSetup.getTaxJurisdictions().size());
        assertEquals("Tax Payment groups", 4, taxSetup.getTaxPaymentGroups().size());
        for (TaxPaymentGroup taxPaymentGroup : taxSetup.getTaxPaymentGroups()) {
            if (taxPaymentGroup.getId().equals("US_IRS_941_PAYMENT") || taxPaymentGroup.getId().equals("US_AZ_A1_PAYMENT")) {
                assertEquals("Deposit frequencies", 1, taxPaymentGroup.getDepositFrequencies().size());
                assertEquals("Deposit frequency", FrequencyEnum.MONTHLY, taxPaymentGroup.getDepositFrequencies().get(0).getFrequency());
            } else if (taxPaymentGroup.getId().equals("US_IRS_940_PAYMENT")) {
                assertEquals("Deposit frequencies", 1, taxPaymentGroup.getDepositFrequencies().size());
                assertEquals("Deposit frequency", FrequencyEnum.QUARTERLY, taxPaymentGroup.getDepositFrequencies().get(0).getFrequency());
            }
        }

        // Get Tax setup with deposit frequency history
        taxSetupGetServiceParams.setShowAllParam(ShowHistory.TAXDEPOSITFREQUENCY.name() + "," + ShowHistory.TAXRATE.name());
        taxSetup = (TaxSetup) ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetupGetServiceParams).getResult();
        assertNotNull(taxSetup);
        assertEquals("Tax Jurisdictions", 3, taxSetup.getTaxJurisdictions().size());
        assertEquals("Tax Payment groups", 4, taxSetup.getTaxPaymentGroups().size());
        for (TaxPaymentGroup taxPaymentGroup : taxSetup.getTaxPaymentGroups()) {
            if (taxPaymentGroup.getId().equals("US_IRS_941_PAYMENT") || taxPaymentGroup.getId().equals("US_AZ_A1_PAYMENT")) {
                assertEquals("Deposit frequencies", 3, taxPaymentGroup.getDepositFrequencies().size());
            } else if (taxPaymentGroup.getId().equals("US_IRS_940_PAYMENT")) {
                assertEquals("Deposit frequencies", 1, taxPaymentGroup.getDepositFrequencies().size());
                assertEquals("Deposit frequency", FrequencyEnum.QUARTERLY, taxPaymentGroup.getDepositFrequencies().get(0).getFrequency());
            }
        }

        PayrollServices.beginUnitOfWork();
        // Checking AZ-A1-PAYMENT deposit frequency in DB
        assertEquals("Effective dep frequencies", 1, EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, PaymentTemplate.findPaymentTemplate("AZ-A1-PAYMENT"), null).size());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DepositFrequencyChanged, null, eventStartDate, PSPDate.getPSPTime());
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent event = companyEventsList.get(0);
        Date startDate = taxPaymentGroupToUpdate.getDepositFrequencies().get(0).getStartDate();
        SpcfCalendar spcfStartDate = SpcfCalendar.createInstance(startDate.getTime());
        CalendarUtils.clearTime(spcfStartDate);
        assertEquals("Effective Date ", spcfStartDate.toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(taxPaymentGroupToUpdate.getDepositFrequencies().get(0).getFrequency()).toString(),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(taxPaymentGroupToUpdate.getId()),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        PayrollServices.rollbackUnitOfWork();
    }

    // Test Federal deposit frequency update after threshold is met with less frequent frequency
    @Test
    public void test_Federal_Threshold_DFTest() throws Throwable {
        // Setup company with IRS-941 100K Payroll and state AZ that follows federal
        DataLoadServices.setupCompanyAndSubmit100KPayrollWithOneState(COMPANY_ID, "AZ", SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-08-12"));
        // Moving after threshold hit date, to get the latest DF.
        DataLoadServices.setPSPDate(2011, 8, 14);

        TaxSetupGetServiceParams taxSetupGetServiceParams = new TaxSetupGetServiceParams();
        taxSetupGetServiceParams.setCompanyId(COMPANY_ID);
        taxSetupGetServiceParams.setExpand(Expand.TAXSETUP.toString());
        TaxSetup taxSetup = (TaxSetup) ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetupGetServiceParams).getResult();
        assertNotNull(taxSetup);
        assertEquals("Tax Jurisdictions", 3, taxSetup.getTaxJurisdictions().size());
        assertEquals("Tax Payment groups", 4, taxSetup.getTaxPaymentGroups().size());
        for (TaxPaymentGroup taxPaymentGroup : taxSetup.getTaxPaymentGroups()) {
            if (taxPaymentGroup.getId().equals("US_IRS_941_PAYMENT") || taxPaymentGroup.getId().equals("US_AZ_A1_PAYMENT")) {
                assertEquals("Deposit frequencies", 1, taxPaymentGroup.getDepositFrequencies().size());
                assertEquals("Deposit frequency", FrequencyEnum.SEMI_WEEKLY, taxPaymentGroup.getDepositFrequencies().get(0).getFrequency());
            } else if (taxPaymentGroup.getId().equals("US_IRS_940_PAYMENT")) {
                assertEquals("Deposit frequencies", 1, taxPaymentGroup.getDepositFrequencies().size());
                assertEquals("Deposit frequency", FrequencyEnum.QUARTERLY, taxPaymentGroup.getDepositFrequencies().get(0).getFrequency());
            }
        }

        TaxPaymentGroup taxPaymentGroupToUpdate = getTaxPaymentGroup("US_IRS_941_PAYMENT", "US_FEDERAL", "US_FEDERAL_IRS", "IRS-941-PAYMENT", FrequencyEnum.MONTHLY); // this is setting Df start date as current quarter start date
        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroupToUpdate, getServiceParams(COMPANY_ID));
        assertFalse("Threshold met error", taxPaymentGroupResult.isSuccess());
        assertNull(taxPaymentGroupResult.getResult()); // Result is null from Tax Payment group update service
        assertEquals("Error messages", 1, taxPaymentGroupResult.getErrorMessages().size());
        assertEquals("Error message Code", MessageCode.GenericValidationMessage.getMessageCode(), taxPaymentGroupResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Error message ", "Skip - Threshold Met", taxPaymentGroupResult.getErrorMessages().get(0).getMessage());
    }

    @Test
    public void test_DGDeletedCompany() throws Throwable {
        Company company = createAssistedCompanyWithRates("CO", COMPANY_ID, "987654332", "311076-05-1");
        TaxPaymentGroup taxPaymentGroup = getTaxPaymentGroup("US_CO_DR1094_PAYMENT", "US_CO", "US_CO_DOR", "CO-DR1094-PAYMENT", FrequencyEnum.QUARTERLY);
        SpcfCalendar eventStartDate = PSPDate.getPSPTime();

        Application.beginUnitOfWork();
        Company company1 = Application.findById(Company.class, company.getId());
        company1.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company1);
        Application.commitUnitOfWork();

        ServiceResult taxPaymentGroupResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, getServiceParams(COMPANY_ID));
        assertNotNull(taxPaymentGroupResult);
        assertFalse(taxPaymentGroupResult.isSuccess());
        assertEquals(MessageCode.EntityDoesNotExist.getMessageCode(), taxPaymentGroupResult.getMessages().get(0).getMessageCode());


    }

    public static PayrollRun createPayrollRun(Company company) {
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setCompany(company);
        payrollRun.setSourcePayRunId("payroll run " + 0);
        payrollRun.setPaycheckDate(SpcfCalendar.createInstance(2013, 11, 20, SpcfTimeZone.getLocalTimeZone()));
        Application.save(payrollRun);
        return payrollRun;
    }

    public void createCOCompanyWithInactiveDF() {
        createAssistedCompanyWithRates("CO", COMPANY_ID, "987654332", "311076-05-1");

        //Get the latest deposit frequency for the payment template
        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequencyDTO dto = getDto("CODOR", "CO-DR1094-PAYMENT");
        com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(COMPANY_ID, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, dto.getPaymentTemplateCd());
        EffectiveDepositFrequency existingLatestDepositFrequency =
                EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate,
                                                                              dto.getEffectiveDate());
        existingLatestDepositFrequency.setInvalidDate(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();


    }

    private static Company createAssistedCompanyWithRates(String state, String psid, String ein, String aid) {
        // Company with laws, rates, etc.
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.Tax);
        try {
            ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds(state);
            // / Fix the company name so we can always compare correctly.
            PayrollServices.beginUnitOfWork();
            // Vertical bars should be removed during Master File creation which uses pipes/bars as
            // a field separator.
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(Company.findCompany(psid, SourceSystemCode.QBDT));
            companyDTO.setLegalName(COMPANY_NAME);
            companyDTO.setDBA(company.getLegalName());
            assertSuccess(PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, psid, companyDTO));
            PayrollServices.commitUnitOfWork();

            lawIds.removeAll(Arrays.asList(RateConverterFactory.INACTIVE_LAW_IDS_FOR_SUI_RATE_EXCHANGE));  //remove all inactive laws for SUI rate exchange
            DataLoadServices.addCompanyLawsWithAgencyId(aid, company, state, lawIds);
            DataLoadServices.addCompanyLawRates(company);

            PayrollServices.beginUnitOfWork();
            company = Application.refresh(company);
            PayrollServices.rollbackUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return company;
    }

    private static Company createAssistedCompanyWithRatesWithDD(String state, String psid, String ein, String aid) {
        // Company with laws, rates, etc.
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds(state);
        // / Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        company.setLegalName(COMPANY_NAME);
        company.setDbaName(company.getLegalName());
        PayrollServices.commitUnitOfWork();
        lawIds.removeAll(Arrays.asList(RateConverterFactory.INACTIVE_LAW_IDS_FOR_SUI_RATE_EXCHANGE));  //remove all inactive laws for SUI rate exchange
        DataLoadServices.addCompanyLawsWithAgencyId(aid, company, state, lawIds);
        DataLoadServices.addCompanyLawRates(company);
        PayrollServices.rollbackUnitOfWork();
        return company;
    }

    private EffectiveDepositFrequencyDTO getDto(String agencyId, String paymentTemplateCd) {
        EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
        dto.setAgencyId(agencyId);
        SpcfCalendar newEffectiveDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(newEffectiveDate, 5);
        dto.setPaymentTemplateCd(paymentTemplateCd);
        dto.setEffectiveDate(newEffectiveDate);
        dto.setPaymentFrequencyId(DepositFrequencyCode.QUARTERLY);
        return dto;
    }

    public TaxPaymentGroupUpdateServiceParams getServiceParams(String companyId) {
        TaxPaymentGroupUpdateServiceParams taxPaymentGroupUpdateServiceParams = new TaxPaymentGroupUpdateServiceParams();
        taxPaymentGroupUpdateServiceParams.setCompanyId(companyId);
        taxPaymentGroupUpdateServiceParams.setSendEmail(false);
        return taxPaymentGroupUpdateServiceParams;
    }

    public static TaxPaymentGroup getTaxPaymentGroupWithPSPDate(String paymentTemplateId, String jurisdiction, String agencyId, String name, FrequencyEnum frequency) {
        TaxPaymentGroup taxPaymentGroup = createTaxPaymentGroup(paymentTemplateId, jurisdiction, agencyId, name);
        List<TaxDepositFrequency> taxDepositFrequencyList = new ArrayList<TaxDepositFrequency>();
        TaxDepositFrequency taxDepositFrequency = new TaxDepositFrequency();
        taxDepositFrequency.setFrequency(frequency);
        Date startDate = new Date(PSPDate.getPSPTime().getTimeInMilliseconds());
        taxDepositFrequency.setStartDate(startDate);
        taxDepositFrequencyList.add(taxDepositFrequency);
        taxPaymentGroup.setDepositFrequencies(taxDepositFrequencyList);
        return taxPaymentGroup;
    }

    public static TaxPaymentGroup getTaxPaymentGroup(String paymentTemplateId, String jurisdiction, String agencyId, String name, FrequencyEnum frequency) {
        TaxPaymentGroup taxPaymentGroup = createTaxPaymentGroup(paymentTemplateId, jurisdiction, agencyId, name);
        List<TaxDepositFrequency> taxDepositFrequencyList = new ArrayList<TaxDepositFrequency>();
        TaxDepositFrequency taxDepositFrequency = new TaxDepositFrequency();
        taxDepositFrequency.setFrequency(frequency);
        Date startDate = DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
        taxDepositFrequency.setStartDate(startDate);
        taxDepositFrequencyList.add(taxDepositFrequency);
        taxPaymentGroup.setDepositFrequencies(taxDepositFrequencyList);
        return taxPaymentGroup;
    }

    private static TaxPaymentGroup createTaxPaymentGroup(String paymentTemplateId, String jurisdiction, String agencyId, String name) {
        TaxPaymentGroup taxPaymentGroup = new TaxPaymentGroup();
        taxPaymentGroup.setId(paymentTemplateId);
        taxPaymentGroup.setJurisdictionId(jurisdiction);
        taxPaymentGroup.setAgencyId(agencyId);
        taxPaymentGroup.setName(name);
        return taxPaymentGroup;
    }

    public static void updateGAPaymentTemplateSupportDate(SpcfCalendar pSupportedDate) {
        DataLoadServices.updatePaymentTemplateSupportedDate("GA-DOL4-PAYMENT", pSupportedDate);
        DataLoadServices.updatePaymentTemplateSupportedDate("GA-GAV-PAYMENT", pSupportedDate);
    }


    public void testPayrollsOverStateThreshold(String state, Company company, HashMap<String, String> lawAmounts) throws Exception {

        SpcfCalendar beginDate = PSPDate.getPSPTime();
        DataLoadServices.setPSPDate(beginDate);

        SpcfMoney statePaymentAmount = SpcfMoney.ZERO;
        for (String lawId : lawAmounts.keySet()) {
            statePaymentAmount = (SpcfMoney) statePaymentAmount.add(new SpcfMoney(lawAmounts.get(lawId)));
        }

        String[] statesList = new String[]{state};
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");


        DataLoadServices.runPayrollRun(company, statesList, beginDate, new DateDTO("2014-01-02"), false, lawAmounts, PaymentTemplateCategory.Withholding);

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, new DateDTO("2014-01-11"));


    }

}
