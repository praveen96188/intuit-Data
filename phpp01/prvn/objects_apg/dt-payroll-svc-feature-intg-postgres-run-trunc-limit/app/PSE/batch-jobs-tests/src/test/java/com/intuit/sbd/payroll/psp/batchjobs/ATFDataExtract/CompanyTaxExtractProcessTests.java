package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAgencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAgencyPaymentTemplateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyLawDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.*;

import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * User: ihannur
 * Date: 8/17/12
 * Time: 4:13 PM
 */
public class CompanyTaxExtractProcessTests {
    static boolean extract = false;

    @BeforeClass
    public static void beforeClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        PayrollServices.beginUnitOfWork();
        extract = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_EXTRACT);
        PayrollServices.commitUnitOfWork();
        if (!extract) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_EXTRACT, "true");
        }

    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        if (!extract) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_EXTRACT, "false");
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
        DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath"));
    }

    @Test
    public void testMultipleCompanyLawsForSameLaw() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        // When duplicate company laws are added old company laws are updated with additional_company_fk.
        // Extract will extract only if additional_company_fk is null and is_deleted is false.
        // If is_deleted is set to false on new company laws old and new both will not extract.
        List<CompanyLaw> companyLaws1 = DataLoadServices.addCompanyLaws(company, "116", "124", "914", "904");
        List<CompanyLaw> companyLaws2 = DataLoadServices.addCompanyLaws(company, "116", "124", "914", "904");


        PayrollServices.beginUnitOfWork();
        for (CompanyLaw companyLaw : companyLaws1) {
            Application.refresh(companyLaw);
            //Updating to validate that we extract latest source Id company law - Y, Y
            companyLaw.setFilingStatus(PayrollItemStatus.Inactive);
            companyLaw.setExemptionStatus(LawStatus.Exempt);
            //Deleting latest 116 law to validate we extract old with N,N
            if(companyLaw.getLaw().getLawId().equals("116")) {
                companyLaw.getQbdtPayrollItemInfo().setIsDeleted(true);
            }
            Application.save(companyLaw);
        }
        for (CompanyLaw companyLaw : companyLaws2) {
            Application.refresh(companyLaw);
            //Updating to validate that we extract latest source Id company law  -  N, Y
            companyLaw.setFilingStatus(PayrollItemStatus.Active);
            companyLaw.setExemptionStatus(LawStatus.Exempt);
            //Deleting latest 116 law to validate we extract first one (least source Id) with N,N.
            // 124 - Extract the source Id with second highest - Y, Y
            // 914, 904 - Extract with max source Id with N, Y
            if(companyLaw.getLaw().getLawId().equals("116") || companyLaw.getLaw().getLawId().equals("124")) {
                companyLaw.getQbdtPayrollItemInfo().setIsDeleted(true);
            }
            Application.save(companyLaw);
        }

        /* Based max(SourceId) and non-deleted, Output file needs to have below values.
        NV SUI_ER    116 - N, N ==> New Company law is deleted, will not get extracted
        SC SUI_ER    124 - Y, Y ==> New Company law is deleted, will not get extracted
        CO SUI_EE    904 - N, Y
        MI SUI_EE    914 - N, Y*/
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_MultipleCompanyLawsForSameLaw"));
    }

    @Test
    public void testMultipleCompanies() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        DataLoadPalette.setupTaxCompany();
        Company company2 = DataLoadPalette.setupTaxCompany();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company2);
        for (CompanyAgency companyAgency : company2.getCompanyAgencyCollection()) {
            for (CompanyLaw companyLaw : companyAgency.getCompanyLawCollection()) {
                CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
                companyLawDTO.setFilingStatus(PayrollItemStatus.Inactive);
                companyLawDTO.setExemptionStatus(LawStatus.Exempt);
                assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company2.getSourceSystemCd(), company2.getSourceCompanyId(), companyLawDTO));
            }
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_multipleCompanies"));
    }

    @Test
    public void testMultipleCompaniesWithDGDeletedData() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        DataLoadPalette.setupTaxCompany();
        Company company2 = DataLoadPalette.setupTaxCompany();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company2);
        for (CompanyAgency companyAgency : company2.getCompanyAgencyCollection()) {
            for (CompanyLaw companyLaw : companyAgency.getCompanyLawCollection()) {
                CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
                companyLawDTO.setFilingStatus(PayrollItemStatus.Inactive);
                companyLawDTO.setExemptionStatus(LawStatus.Exempt);
                assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company2.getSourceSystemCd(), company2.getSourceCompanyId(), companyLawDTO));
            }
        }
        company2.setIsDgDisassociated(true);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 5);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedDGData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_multipleCompanies"));
    }

    @Test
    public void testTerminatedCompany() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Terminated));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertEquals(ServiceSubStatusCode.Terminated, company.getCompanyService(ServiceCode.Tax).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath"));
    }

    @Test
    public void testInvalidatedAdditionalFilingAmountNotExtracted() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

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
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_noAdditionalRates"));
    }

    @Test
    public void testSecondExtractAfterNewAdditionalFilingAmount() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        Application.beginUnitOfWork();
        CompanyFilingAmount companyFilingAmount = assertOne(Application.find(CompanyFilingAmount.class, CompanyFilingAmount.Name().in("SC SUI Credit")));
        Application.delete(companyFilingAmount);
        Application.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_addFilingAmount_before"));

        DataLoadServices.setPSPDate(2011, 1, 6);
        DataLoadServices.addAdditionalFilingAmounts(company, null, 3.14, SpcfCalendar.createInstance(2011, 1, 1), false);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_addFilingAmount_after"));
    }

    @Test
    public void testSecondExtractAfterUpdatingAgencyID() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath"));

        DataLoadServices.setPSPDate(2011, 1, 6);
        DataLoadServices.updateAgencyTaxpayerId(company, "MI-MW106-PAYMENT", "IDEgoSuperEgo");

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_updateAgencyId"));
    }

    @Test
    public void testSecondExtractAfterUpdatingFilingStatus() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath"));

        DataLoadServices.setPSPDate(2011, 1, 6);
        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = assertOne(Application.find(CompanyLaw.class, CompanyLaw.Law().LawId().equalTo("161")));
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
        companyLawDTO.setFilingStatus(PayrollItemStatus.Inactive);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_updateFilingStatus"));
    }

    @Test
    public void testNoSecondExtractAfterUpdatingLegalName() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2011, 1, 5);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath"));

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setLegalName("David's Extreme Knife and Contract Bridge Supply Store, LLC.");
        assertSuccess(PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertEquals("David's Extreme Knife and Contract Bridge Supply Store, LLC.", company.getLegalName());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 6);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_empty"));
    }

    @Test
    /**
     * Creates 2 companies, one is PendingSetup Tax service and other in ActiveCurrent.
     * After the job is run, the extract file should contain info about the Active company alone.
     */
    public void testCompanyTaxDataBeforeServiceActivation() {

        DataLoadServices.setPSPDate(2017, 1, 1);
        Company companyInPendingSetup = DataLoadPalette.setupTaxCompany(Boolean.FALSE);
        Company companyInActiveCurrent = DataLoadPalette.setupTaxCompany();
        Company companyInCancelled = DataLoadPalette.setupTaxCompany();
        DataLoadServices.cancelService(companyInCancelled, ServiceCode.Tax);
        DataLoadServices.setPSPDate(2017, 1, 5);

        try {
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyTaxInfo, "atfextract/expected/test_CompanyTax_happypath_NonActive"));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
