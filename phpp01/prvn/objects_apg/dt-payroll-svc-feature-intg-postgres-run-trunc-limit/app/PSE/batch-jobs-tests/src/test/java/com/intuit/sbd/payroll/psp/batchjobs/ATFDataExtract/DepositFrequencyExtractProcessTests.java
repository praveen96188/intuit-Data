package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedUnprocessedRequestTests;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXRequestGenerator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPITEM;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 10/19/12
 * Time: 1:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class DepositFrequencyExtractProcessTests {

    static boolean extractCompanyInfo = false;

    @BeforeClass
    public static void beforeClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        PayrollServices.beginUnitOfWork();
        extractCompanyInfo = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_DEPOSIT_FREQUENCY_EXTRACT);
        PayrollServices.commitUnitOfWork();
        if (!extractCompanyInfo) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_DEPOSIT_FREQUENCY_EXTRACT, "true");
        }

    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        if (!extractCompanyInfo) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_DEPOSIT_FREQUENCY_EXTRACT, "false");
        }
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.reinitialize();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testHappyPath() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{"116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(6.1 * i));
            lawAmounts.put("62", String.valueOf(6.2 * i));
            lawAmounts.put("63", String.valueOf(6.3 * i));
            lawAmounts.put("64", String.valueOf(6.4 * i));
            lawAmounts.put("1", String.valueOf(1.5 * i));
            lawAmounts.put("131", String.valueOf(13.1 * i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(13.0 * i)); // VT SUI-ER
            lawAmounts.put("116", String.valueOf(11.6 * i)); // NV SUI-ER


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

            companyPayrollItemSourceId++;
            companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            lawIds = new ArrayList<String>();
            companyLaw = CompanyLaw.findCompanyLaw(company, "131");
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

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);
            PayrollServices.commitUnitOfWork();
        }

        System.out.println(ATFDataExtractFileType.CompanyDepFreqInfo);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_DepositFrequency_HappyPath"));
    }

    @Test
    public void testHappyPathWithDGDeletedCompany() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{"116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(6.1 * i));
            lawAmounts.put("62", String.valueOf(6.2 * i));
            lawAmounts.put("63", String.valueOf(6.3 * i));
            lawAmounts.put("64", String.valueOf(6.4 * i));
            lawAmounts.put("1", String.valueOf(1.5 * i));
            lawAmounts.put("131", String.valueOf(13.1 * i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(13.0 * i)); // VT SUI-ER
            lawAmounts.put("116", String.valueOf(11.6 * i)); // NV SUI-ER


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

            companyPayrollItemSourceId++;
            companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            lawIds = new ArrayList<String>();
            companyLaw = CompanyLaw.findCompanyLaw(company, "131");
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

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);
            PayrollServices.commitUnitOfWork();
        }

        Application.beginUnitOfWork();
        Company company = Company.findCompany("158905", SourceSystemCode.QBDT);
        company.setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedDGData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_DepositFrequency_HappyPath"));
    }

    @Test
    public void testNoUpdates() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{"116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(6.1 * i));
            lawAmounts.put("62", String.valueOf(6.2 * i));
            lawAmounts.put("63", String.valueOf(6.3 * i));
            lawAmounts.put("64", String.valueOf(6.4 * i));
            lawAmounts.put("1", String.valueOf(1.5 * i));
            lawAmounts.put("131", String.valueOf(13.1 * i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(13.0 * i)); // VT SUI-ER
            lawAmounts.put("116", String.valueOf(11.6 * i)); // NV SUI-ER


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

            companyPayrollItemSourceId++;
            companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            lawIds = new ArrayList<String>();
            companyLaw = CompanyLaw.findCompanyLaw(company, "131");
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

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);
            PayrollServices.commitUnitOfWork();
        }

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_DepositFrequency_HappyPath"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_DepositFrequency_NoUpdates"));
    }

    @Test
    public void testIRSAgencyButNoFIT() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        String companyPSID = "999079113";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_CompanyDepFreq_AgencyOnly"));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 1, 9, 10, 3, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE999079113.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_CompanyDepFreq_AfterAddingFIT"));
    }

    @Test
    public void testUpdatedFIT() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{"116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            PaymentTemplate paymentTemplate = PaymentTemplate.getIRS_941();

            EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
            SpcfCalendar newEffectiveDate = SpcfCalendar.createInstance(2012, 4, 1, SpcfTimeZone.getLocalTimeZone());

            dto.setAgencyId("IRS");
            dto.setEffectiveDate(newEffectiveDate);
            dto.setPaymentFrequencyId(DepositFrequencyCode.ANNUAL);
            dto.setPaymentTemplateCd(paymentTemplate.getPaymentTemplateCd());

            PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, company.getSourceCompanyId(), dto);
            PayrollServices.commitUnitOfWork();
        }

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_DepositFrequency_Updated941"));
    }

    @Test
    public void testIowaBusinessTaxId() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{"116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Agency> agencies = Application.find(Agency.class, Agency.AgencyId().equalTo("IADOR"));
        assertEquals("Too many agencies found", 1, agencies.size());

        Agency iaAgency = agencies.get(0);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                "IA-44105-PAYMENT", DepositFrequencyCode.MONTHLY);

        DomainEntitySet<CompanyAgency> companyAgencies = Application.find(CompanyAgency.class, CompanyAgency.Agency().equalTo(iaAgency));

        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = null;
        for (CompanyAgency companyAgency : companyAgencies) {
            DomainEntitySet<CompanyAgencyPaymentTemplate> companyAgencyPaymentTemplates = Application.find(CompanyAgencyPaymentTemplate.class,
                                                                                                           CompanyAgencyPaymentTemplate.CompanyAgency().equalTo(companyAgency)
                                                                                                                                       .And(CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(paymentTemplateFrequency.getPaymentTemplate())));

            assertEquals("Too many CompanyAgencyPaymentTemplates found", 1, companyAgencyPaymentTemplates.size());

            companyAgencyPaymentTemplate = companyAgencyPaymentTemplates.get(0);

            CompanyPaymentTemplateAgencyId companyPaymentTemplateAgencyId = new CompanyPaymentTemplateAgencyId();
            companyPaymentTemplateAgencyId.setName("BEN Number");
            companyPaymentTemplateAgencyId.setAgencyTaxpayerId("12345678");
            companyPaymentTemplateAgencyId.setCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate);

            Application.save(companyPaymentTemplateAgencyId);
        }
        PayrollServices.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_DepositFrequency_IowaBusinessTaxId"));
    }

    @Test
    public void testUpdatedSIT() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{"116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("IA-44105-PAYMENT");

            EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
            SpcfCalendar newEffectiveDate = SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone());

            dto.setAgencyId("IADOR");
            dto.setEffectiveDate(newEffectiveDate);
            dto.setPaymentFrequencyId(DepositFrequencyCode.SEMIMONTHLY);
            dto.setPaymentTemplateCd(paymentTemplate.getPaymentTemplateCd());

            PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, company.getSourceCompanyId(), dto);
            PayrollServices.commitUnitOfWork();
        }

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_DepositFrequency_UpdatedIA"));
    }


    @Test
    public void testExtractNonActiveCompany941Filing() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        String companyPSID = "632000001";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        //DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        //Company company = Company.findCompany(companyPSID, SourceSystemCode.QBDT);
        //DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_CompanyDepFreq_Extract941FilingNonActiveCompany"));
     }

    @Test
    public void testExtractNonActiveCompany941Filing_withActiveDD() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        String companyPSID = "632000001";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        //Company company = Company.findCompany(companyPSID, SourceSystemCode.QBDT);
        //DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_CompanyDepFreq_Extract941FilingNonActiveCompany"));
    }
    @Test
    public void testExtractActiveCompany941Filing() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        String companyPSID = "632000001";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        sendBalancedFileFromQB(companyPSID);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_CompanyDepFreq_Extract941FilingActiveCompany"));

    }
    @Test
    public void testExtractCompleteCompany941Filing() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        String companyPSID = "632000001";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_CompanyDepFreq_Extract941FilingNonActiveCompany"));
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        sendBalancedFileFromQB(companyPSID);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_CompanyDepFreq_Extract941FilingActiveCompany"));

    }

    @Test
    public void testExtractNonActiveCompany944Filing() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        String companyPSID = "632000001";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.updateFilerType(company,"944");
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_CompanyDepFreq_Extract944FilingNonActiveCompany"));
    }

    @Test
    public void testExtractActiveCompany944Filing() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        String companyPSID = "632000001";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        sendBalancedFileFromQB(companyPSID);
        DataLoadServices.updateFilerType(company,"944");


        //Company company = Company.findCompany(companyPSID, SourceSystemCode.QBDT);
        //DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_CompanyDepFreq_Extract944FilingActiveCompany"));

    }


    @Test
    public void testExtractCompleteCompany944Filing() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        String companyPSID = "632000001";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_CompanyDepFreq_Extract944FilingNonActiveCompany"));
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        sendBalancedFileFromQB(companyPSID);
        DataLoadServices.updateFilerType(company,"944");


        //Company company = Company.findCompany(companyPSID, SourceSystemCode.QBDT);
        //DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_CompanyDepFreq_Extract944FilingActiveCompany"));

    }

    @Test
    public void testDFExtractMultipleActiveCompanies() throws Exception {
        //PSP-11806
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        String companyPSID = "632000004";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        sendBalancedFileFromQB(companyPSID);

        String companyPSID2 = "632000002";
        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID2, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company2);
        sendBalancedFileFromQB(companyPSID2);

        String companyPSID3 = "632000001";
        Company company3 = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID3, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company3);
        sendBalancedFileFromQB(companyPSID3);

        String companyPSID4 = "632000003";
        Company company4 = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID4, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company4);
        sendBalancedFileFromQB(companyPSID4);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_DepFreq_Extract_MultipleActiveCompanies"));

    }


   //Helper method

    public void sendBalancedFileFromQB(String psid) throws Exception {
        if (psid == null) {
            return;
        }
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        // DataLoadServices.updateIRSPaymentTemplateSupportDate(null);
        // DataLoadServices.updateCAEDDPaymentTemplateSupportDate(null);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true, true, true, false, false);
        for (IPAYROLLTX ipayrolltx : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX()) {
            ipayrolltx.getITXLINE().clear();
            int lineCount = 0;
            int transactionTotal = 0;
            for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
                int amount = lineCount++;
                transactionTotal = amount;
                ipayrolltx.getITXLINE().add(OFXRequestGenerator.generateTransactionLine(null,
                                                                                        new SpcfMoney(SpcfDecimal.createInstance(amount)),
                                                                                        "Class" + lineCount,
                                                                                        false,
                                                                                        "Memo" + lineCount,
                                                                                        ipitem.getIPITEMID(),
                                                                                        null,
                                                                                        null));
                lineCount++;
            }
            ipayrolltx.setIAMT("$-" + transactionTotal + ".00");
        }


        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

    }

    @Test
    public void testDeletedPItem() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{"116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(6.1 * i));
            lawAmounts.put("62", String.valueOf(6.2 * i));
            lawAmounts.put("63", String.valueOf(6.3 * i));
            lawAmounts.put("64", String.valueOf(6.4 * i));
            lawAmounts.put("1", String.valueOf(1.5 * i));
            lawAmounts.put("131", String.valueOf(13.1 * i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(13.0 * i)); // VT SUI-ER
            lawAmounts.put("116", String.valueOf(11.6 * i)); // NV SUI-ER


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

            // Mark deleted for Law Id = 14, OR IA-44105
            PayrollServices.beginUnitOfWork();
            companyLaw = CompanyLaw.findCompanyLaw(company, "116");
            CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
            companyLawDTO.getQBDTPayrollItemInfoDTO().setIsDeleted(true);
            assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
            PayrollServices.commitUnitOfWork();

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);
            PayrollServices.commitUnitOfWork();
        }

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyDepFreqInfo, "atfextract/expected/test_DepositFrequency_DeletedPItem_Expected"));
    }

}
