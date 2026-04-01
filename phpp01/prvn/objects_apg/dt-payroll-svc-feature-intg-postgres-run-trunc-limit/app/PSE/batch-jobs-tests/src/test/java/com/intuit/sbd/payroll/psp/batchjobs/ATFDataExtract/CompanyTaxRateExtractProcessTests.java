package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * User: ihannur
 * Date: 8/17/12
 * Time: 4:13 PM
 */
public class CompanyTaxRateExtractProcessTests {
    static boolean extractTax = false;
    static boolean extractTaxRate = false;

    @BeforeClass
    public static void beforeClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        PayrollServices.beginUnitOfWork();
        extractTaxRate = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_RATE_EXTRACT);
        extractTax = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_EXTRACT);
        PayrollServices.commitUnitOfWork();
        if (!extractTaxRate) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_RATE_EXTRACT, "true");
        }
        if (!extractTax) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_EXTRACT, "true");
        }

    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        if (!extractTax) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_EXTRACT, "false");
        }
        if (!extractTaxRate) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_RATE_EXTRACT, "false");
        }
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MI-UIA1020-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MI-MW106-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CO-DR1094-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CO-UITR1-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("SC-WH1601-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("SC-UCE120-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WY-WYO056-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IL-UI340-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.reinitialize();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void testHappyPath() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        //Law Id = 61, SS_EE, This is Employee paid
        //Law Id = 63, MED_EE, This is also Employee paid
        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "61");
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
        companyLawDTO.getQBDTPayrollItemInfoDTO().setIsEmployeePaid(true);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));

        companyLaw = CompanyLaw.findCompanyLaw(company, "63");
        companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
        companyLawDTO.getQBDTPayrollItemInfoDTO().setIsEmployeePaid(true);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyLawRates(company);

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath"),
                                                                         new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_happypath_WithNegativeEEPaid"));

    }

    @Test
    public void testHappyPathWithDGDeletedData() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        //Law Id = 61, SS_EE, This is Employee paid
        //Law Id = 63, MED_EE, This is also Employee paid
        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "61");
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
        companyLawDTO.getQBDTPayrollItemInfoDTO().setIsEmployeePaid(true);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));

        companyLaw = CompanyLaw.findCompanyLaw(company, "63");
        companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
        companyLawDTO.getQBDTPayrollItemInfoDTO().setIsEmployeePaid(true);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyLawRates(company);

        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        Application.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 5);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedDGData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath"),
                new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_happypath_WithNegativeEEPaid"));

    }

    @Test
    public void testMultipleRates() throws Exception {

        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLawRates(company);

        // Invalidating the earlier rate and adding new one with different rates, effective date to make sure we extract valid rate
        DataLoadServices.addAdditionalFilingAmounts(company, "MIDLEG", 15.5, SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), true);
        DataLoadServices.addAdditionalFilingAmounts(company, "MIDLEG", 5.5, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()), false);
        DataLoadServices.addAdditionalFilingAmounts(company, "MIDLEG", 4.5, SpcfCalendar.createInstance(2011, 9, 1, SpcfTimeZone.getLocalTimeZone()), false);

        DataLoadServices.addAdditionalFilingAmounts(company, "CODLE", 6.8, SpcfCalendar.createInstance(2011, 2, 1, SpcfTimeZone.getLocalTimeZone()), true);
        DataLoadServices.addAdditionalFilingAmounts(company, "CODLE", 1.25, SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()), false);
        DataLoadServices.addAdditionalFilingAmounts(company, "CODLE", 2.25, SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), false);

        DataLoadServices.addAdditionalFilingAmounts(company, "SCESC", 11.11, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()), true);
        DataLoadServices.addAdditionalFilingAmounts(company, "SCESC", 10.50, SpcfCalendar.createInstance(2011, 10, 1, SpcfTimeZone.getLocalTimeZone()), false);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);

        DomainEntitySet<CompanyAgency> companyAgencyCollection = company.getCompanyAgencyCollection().find(CompanyAgency.Agency().AgencyId().equalTo("MAWUA"));
        CompanyAgency companyAgency = companyAgencyCollection.get(0);

        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = companyAgency.getCompanyAgencyPaymentTemplateCollection().find(CompanyAgencyPaymentTemplate.PaymentTemplate().PaymentTemplateCd().equalTo("MA-1700HI-PAYMENT")).get(0);

        CompanyFilingAmountDTO filingAmountDTO = new CompanyFilingAmountDTO();
        filingAmountDTO.setName("MA Unemployment Health Insurance Rate");
        filingAmountDTO.setAmount(0.0048);
        filingAmountDTO.setEffectiveDate(new DateDTO(SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone())));
        companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).getCompanyFilingAmountDTOs().add(filingAmountDTO);

        filingAmountDTO = new CompanyFilingAmountDTO();
        filingAmountDTO.setName("MA UHI Credit");
        filingAmountDTO.setAmount(113.13);
        filingAmountDTO.setEffectiveDate(new DateDTO(SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone())));
        companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).getCompanyFilingAmountDTOs().add(filingAmountDTO);

        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));

        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyLawRates(company,
                new DataLoadServices.EffectiveRate(SpcfCalendar.createInstance(2010, 10, 1), 0.5),
                new DataLoadServices.EffectiveRate(SpcfCalendar.createInstance(2011, 1, 1), 0.7),
                new DataLoadServices.EffectiveRate(SpcfCalendar.createInstance(2011, 10, 1), -0.6));

        DataLoadServices.setPSPDate(2011, 10, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath_addlFiling"),
                new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_happypath_multipleRates"));
    }

    @Test
    public void testMultipleRates_2014_2() throws Exception {

        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2014, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLawRates(company);

        // Invalidating the earlier rate and adding new one with different rates, effective date to make sure we extract valid rate
        DataLoadServices.addAdditionalFilingAmounts(company, "MIDLEG", 15.5, SpcfCalendar.createInstance(2014, 1, 1, SpcfTimeZone.getLocalTimeZone()), true);
        DataLoadServices.addAdditionalFilingAmounts(company, "MIDLEG", 5.5, SpcfCalendar.createInstance(2014, 3, 1, SpcfTimeZone.getLocalTimeZone()), false);
        DataLoadServices.addAdditionalFilingAmounts(company, "MIDLEG", 4.5, SpcfCalendar.createInstance(2014, 9, 1, SpcfTimeZone.getLocalTimeZone()), false);

        DataLoadServices.addAdditionalFilingAmounts(company, "CODLE", 6.8, SpcfCalendar.createInstance(2014, 2, 1, SpcfTimeZone.getLocalTimeZone()), true);
        DataLoadServices.addAdditionalFilingAmounts(company, "CODLE", 1.25, SpcfCalendar.createInstance(2014, 4, 1, SpcfTimeZone.getLocalTimeZone()), false);
        DataLoadServices.addAdditionalFilingAmounts(company, "CODLE", 2.25, SpcfCalendar.createInstance(2014, 11, 1, SpcfTimeZone.getLocalTimeZone()), false);

        DataLoadServices.addAdditionalFilingAmounts(company, "SCESC", 11.11, SpcfCalendar.createInstance(2014, 3, 1, SpcfTimeZone.getLocalTimeZone()), true);
        DataLoadServices.addAdditionalFilingAmounts(company, "SCESC", 10.50, SpcfCalendar.createInstance(2014, 10, 1, SpcfTimeZone.getLocalTimeZone()), false);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);

        DomainEntitySet<CompanyAgency> companyAgencyCollection = company.getCompanyAgencyCollection().find(CompanyAgency.Agency().AgencyId().equalTo("MAWUA"));
        CompanyAgency companyAgency = companyAgencyCollection.get(0);

        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = companyAgency.getCompanyAgencyPaymentTemplateCollection().find(CompanyAgencyPaymentTemplate.PaymentTemplate().PaymentTemplateCd().equalTo("MA-1700HI-PAYMENT")).get(0);

        CompanyFilingAmountDTO filingAmountDTO = new CompanyFilingAmountDTO();
        filingAmountDTO.setName("MA Unemployment Health Insurance Rate");
        filingAmountDTO.setAmount(0.0048);
        filingAmountDTO.setEffectiveDate(new DateDTO(SpcfCalendar.createInstance(2014, 3, 1, SpcfTimeZone.getLocalTimeZone())));
        companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).getCompanyFilingAmountDTOs().add(filingAmountDTO);

        filingAmountDTO = new CompanyFilingAmountDTO();
        filingAmountDTO.setName("MA Er Medical Assistance Contribution");
        filingAmountDTO.setAmount(0.0036);
        filingAmountDTO.setEffectiveDate(new DateDTO(SpcfCalendar.createInstance(2014, 3, 1, SpcfTimeZone.getLocalTimeZone())));
        companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).getCompanyFilingAmountDTOs().add(filingAmountDTO);


        filingAmountDTO = new CompanyFilingAmountDTO();
        filingAmountDTO.setName("MA UHI Credit");
        filingAmountDTO.setAmount(113.13);
        filingAmountDTO.setEffectiveDate(new DateDTO(SpcfCalendar.createInstance(2014, 7, 1, SpcfTimeZone.getLocalTimeZone())));
        companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).getCompanyFilingAmountDTOs().add(filingAmountDTO);

        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));

        //Invalidate MA Unemployment Health Insurance Rate
        DomainEntitySet<CompanyFilingAmount> cfas = Application.find(CompanyFilingAmount.class,
                                                                     CompanyFilingAmount.CompanyAgencyPaymentTemplate().equalTo(companyAgencyPaymentTemplate)
                                                                                        .And(CompanyFilingAmount.Name().equalTo("MA Unemployment Health Insurance Rate"))
                                                                                        .And(CompanyFilingAmount.InvalidDate().isNull()));
        for (CompanyFilingAmount cfa : cfas) {
            cfa.setInvalidDate(PSPDate.getPSPTime());
        }

        PayrollServices.commitUnitOfWork();



        DataLoadServices.addCompanyLawRates(company,
                                            new DataLoadServices.EffectiveRate(SpcfCalendar.createInstance(2013, 10, 1), 0.5),
                                            new DataLoadServices.EffectiveRate(SpcfCalendar.createInstance(2014, 1, 1), 0.7),
                                            new DataLoadServices.EffectiveRate(SpcfCalendar.createInstance(2014, 10, 1), -0.6));

        DataLoadServices.setPSPDate(2014, 10, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath_addlFiling_2014_2"),
                                                                         new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_happypath_multipleRates_2014_2"));
    }

    @Test
    public void testNoEffectiveDate() throws Throwable {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLawRates(company);

        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "161");
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
        companyLawDTO.getRateDTOs().clear();

        CompanyLawRateDTO companyLawRateDTO = new CompanyLawRateDTO();
        companyLawRateDTO.setRate(3.14);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO);

        CompanyLawRateDTO companyLawRateDTO2 = new CompanyLawRateDTO();
        companyLawRateDTO2.setEffectiveDate(new DateDTO("2014-01-01"));
        companyLawRateDTO2.setRate(1.11);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO2);

        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath"),
                                                                         new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_noEffectiveDate"));
    }

    @Test
    public void testMultipleCompanies() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLawRates(company);

        Company company2 = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLawRates(company2);

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_multipleActiveCompanies"),
                                                                         new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_multipleCompanies"));
    }

    @Test
    public void testInvalidatedAdditionalFilingAmountNotExtracted() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLawRates(company);

        PayrollServices.beginUnitOfWork();
        for (CompanyAgency companyAgency : Application.refresh(company).getCompanyAgencyCollection()) {
            CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
            for (CompanyAgencyPaymentTemplateDTO companyAgencyPaymentTemplateDTO : companyAgencyDTO.getCompanyAgencyPaymentTemplateDTOList()) {
                companyAgencyPaymentTemplateDTO.getCompanyFilingAmountDTOs().clear();
            }
            assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_noAdditionalRates"),
                                                                         new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_noAdditionalRates"));
    }

    @Test
    public void testTerminatedCompany() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLawRates(company);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Terminated));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertEquals(ServiceSubStatusCode.Terminated, company.getCompanyService(ServiceCode.Tax).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath"),
                                                                         new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_happypath"));
    }

    @Test
    public void testSecondExtractAfterAdditionalAdditionalFilingAmount() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company1 = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLawRates(company1);

        String[] states = {"CO"};
        double rate = 2.51;
        DataLoadServices.setupCompany(2l, 2, states, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP)).sort(Company.SourceCompanyId());

        for (Company company : companies) {
            DataLoadServices.addCompanyLawRates(company);
            DataLoadServices.addAdditionalFilingAmounts(company, "CODLE", rate++, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()), false);
        }

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath_AdditionalFiling_1"),
                new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_happypath_AdditionalFiling_1"));
        DataLoadServices.setPSPDate(2011, 1, 5);

        DataLoadServices.setPSPDate(2011, 1, 15);
        DataLoadServices.addAdditionalFilingAmounts(company1, "MIDLEG", 4.5, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()), false);
        DataLoadServices.addAdditionalFilingAmounts(company1, "SCESC", 10.50, SpcfCalendar.createInstance(2012, 7, 1, SpcfTimeZone.getLocalTimeZone()), false);
        DataLoadServices.addAdditionalFilingAmounts(company1, "NVETR", 18.75, SpcfCalendar.createInstance(2012, 10, 1, SpcfTimeZone.getLocalTimeZone()), false);

        for (Company company : companies) {
            DataLoadServices.addAdditionalFilingAmounts(company, "CODLE", rate, SpcfCalendar.createInstance(2012, 4, 1, SpcfTimeZone.getLocalTimeZone()), false);
            DataLoadServices.addAdditionalFilingAmounts(company, "CODLE", rate++, SpcfCalendar.createInstance(2012, 7, 1, SpcfTimeZone.getLocalTimeZone()), false);
            rate +=1;
        }

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_addFilingAmount_additionalFiling"),
                                                                         new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_AfterAdditionalAdditionalFilingAmount"));
    }

    @Test
    public void testNoSecondExtractAfterUpdatingAIDOrLegalName() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLawRates(company);

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath"),
                                                                         new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_happypath"));

        DataLoadServices.setPSPDate(2011, 1, 6);
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setLegalName("Ken Rexford's Variable Key Card Blackwood.");
        assertSuccess(PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateAgencyTaxpayerId(company, "MI-MW106-PAYMENT", "IDEgoSuperEgo");

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_updateAgencyId"),
                new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_empty"));

    }

    @Test
    /** Test for deleted QBDT payroll item */
    public void testDeletedPItems() throws Exception {
        DataLoadServices.setPSPDate(2016, 10, 10);
        Company company = DataLoadPalette.setupTaxCompany();

        // Mark deleted for Law Id = 61, SS_EE
        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "61");
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
        companyLawDTO.getQBDTPayrollItemInfoDTO().setIsDeleted(true);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));

        // Mark deleted for Law Id = 62, SS_ER
        companyLaw = CompanyLaw.findCompanyLaw(company, "62");
        companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
        companyLawDTO.getQBDTPayrollItemInfoDTO().setIsDeleted(true);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyLawRates(company);

        ATFDataExtractTestsUtil.runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData, 0, 0, new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_DeletedPItems_Expected"),
                new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_DeletedPItems_Expected"));

        ATFDataExtractTestsUtil.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData, 2016, 4, new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_DeletedPItems_Expected"),
                new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxRateInfo, "atfextract/expected/test_CompanyTaxRate_DeletedPItems_Expected"));
    }

}
