package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.CompanyRealmValidator;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;

/**
 * Contains the unit tests for the UpdateQBCompanyInfoCore class.
 *
 * @author: Marcela Villani
 */
public class UpdateQBCompanyInfoCoreTests {

    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void updateQBDTCompanyCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company1 = c3dl.persistCompany3();
        CompanyDTO company2 = c3dl.getCompany1();


        company2.getQuickBooksInfo().setApplicationId("NewAppId");
        company2.getQuickBooksInfo().setApplicationVersion("NewAppVersion");
        company2.getQuickBooksInfo().setTaxTableId("383838");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateQBCompanyInfo(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(foundCompany);

        assertCompaniesEqual(company2, foundCompany);
        PayrollServices.commitUnitOfWork();


    }

    @Test
    public void updateQBDTInfoCompanyCoreQBRealmAddSuccess() {
        // Disable Launch Darkly Flag
        CompanyRealmValidator.setUseLaunchDarkly(false);
        // Enable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(true);
        String companyRealm = "9123456890";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789");
        DataLoadServices.addDDService(company);

        // Update Company with Realm Id
        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789", company.getFedTaxId());
        companyDTO.getQuickBooksInfo().setIAMRealmId(companyRealm);
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> updatedCompanyProcessResult = PayrollServices.companyManager.updateQBCompanyInfo(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        Company updatedCompany = updatedCompanyProcessResult.getResult();
        PayrollServices.commitUnitOfWork();
        assertEquals(companyRealm, updatedCompany.getIAMRealmId());

        // Disable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }

    @Test
    public void updateQBDTInfoCompanyCoreSameQBRealmUpdateSuccess() {
        // Disable Launch Darkly Flag
        CompanyRealmValidator.setUseLaunchDarkly(false);
        // Enable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(true);
        String companyRealm = "9123456890";

        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        companyDTO.getQuickBooksInfo().setIAMRealmId(companyRealm);
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);
        assertEquals(companyRealm, company.getIAMRealmId());
        assertEquals(company.getQuickbooksInfo().getIAMRealmId(), company.getIAMRealmId());

        // Update Company with Realm Id
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789", company.getFedTaxId());
        companyDTO.getQuickBooksInfo().setIAMRealmId(companyRealm);
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> updatedCompanyProcessResult = PayrollServices.companyManager.updateQBCompanyInfo(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        Company updatedCompany = updatedCompanyProcessResult.getResult();
        assertEquals(companyRealm, updatedCompany.getIAMRealmId());
        assertEquals(updatedCompany.getQuickbooksInfo().getIAMRealmId(), updatedCompany.getIAMRealmId());
        PayrollServices.commitUnitOfWork();

        // Disable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }

    @Test
    public void updateQBDTInfoCompanyCoreDifferentQBRealmUpdateSuccess() {
        // Disable Launch Darkly Flag
        CompanyRealmValidator.setUseLaunchDarkly(false);
        // Enable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(true);
        String companyRealm = "9123456890";

        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        companyDTO.getQuickBooksInfo().setIAMRealmId(companyRealm);
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);
        assertEquals(companyRealm, company.getIAMRealmId());
        assertEquals(company.getQuickbooksInfo().getIAMRealmId(), company.getIAMRealmId());

        // Update Company with Realm Id
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789", company.getFedTaxId());
        companyDTO.getQuickBooksInfo().setIAMRealmId("91234568901");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> updatedCompanyProcessResult = PayrollServices.companyManager.updateQBCompanyInfo(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        Company updatedCompany = updatedCompanyProcessResult.getResult();
        assertEquals(companyRealm, updatedCompany.getIAMRealmId());
        assertEquals(updatedCompany.getQuickbooksInfo().getIAMRealmId(), "91234568901");
        PayrollServices.commitUnitOfWork();

        // Disable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }

    @Test
    public void updateQBDTCompanyCoreQBInfoNullSuccess() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company1 = c3dl.persistCompany3();
        CompanyDTO company2 = c3dl.getCompany1();

        company2.setQuickBooksInfo(null);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateQBCompanyInfo(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(foundCompany);

        assertCompaniesEqual(company2, foundCompany);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateApplicationVersionChanged() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company1 = c3dl.persistCompany3();
        CompanyDTO company2 = c3dl.getCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company2.getQuickBooksInfo().setApplicationVersion("50.00.R.9/20716#pro");

        ProcessResult<Company> result2 = PayrollServices.companyManager.updateQBCompanyInfo(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents
                (foundCompany, EventTypeCode.QuickBooksInfoChanged, CompanyEventStatus.Active, null, null);

        assertEquals("CompanyEvent List Size", 1, companyEvents.size());

        CompanyEvent companyEvent = companyEvents.iterator().next();
        DomainEntitySet<CompanyEventDetail> companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.Details);
        assertEquals("Details List Size", 1, companyEventDetails.size());
        CompanyEventDetail companyEventDetail = companyEventDetails.get(0);
        assertEquals("CompanyEventDetail Value", "Application Version", companyEventDetail.getValue());

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.OldStringValue);
        assertEquals("OldStringValue List Size", 1, companyEventDetails.size());
        companyEventDetail = companyEventDetails.get(0);
        assertEquals("CompanyEventDetail Value", "17.00.R.9/20716#pro", companyEventDetail.getValue());

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewStringValue);
        assertEquals("NewStringValue List Size", 1, companyEventDetails.size());
        companyEventDetail = companyEventDetails.get(0);
        assertEquals("CompanyEventDetail Value", "50.00.R.9/20716#pro", companyEventDetail.getValue());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateLicenseNumberChanged() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company1 = c3dl.persistCompany3();
        CompanyDTO company2 = c3dl.getCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company2.getQuickBooksInfo().setLicenseNumber("6487-4844-4441-477");

        ProcessResult<Company> result2 = PayrollServices.companyManager.updateQBCompanyInfo(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents
                (foundCompany, EventTypeCode.QuickBooksInfoChanged, CompanyEventStatus.Active, null, null);

        assertEquals("CompanyEvent List Size", 1, companyEvents.size());

        CompanyEvent companyEvent = companyEvents.iterator().next();
        DomainEntitySet<CompanyEventDetail> companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.Details);
        assertEquals("Details List Size", 1, companyEventDetails.size());
        CompanyEventDetail companyEventDetail = companyEventDetails.get(0);
        assertEquals("CompanyEventDetail Value", "License Number", companyEventDetail.getValue());

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.OldStringValue);
        assertEquals("OldStringValue List Size", 1, companyEventDetails.size());
        companyEventDetail = companyEventDetails.get(0);
        assertEquals("CompanyEventDetail Value", "6487-4844-4441-476", companyEventDetail.getValue());

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewStringValue);
        assertEquals("NewStringValue List Size", 1, companyEventDetails.size());
        companyEventDetail = companyEventDetails.get(0);
        assertEquals("CompanyEventDetail Value", "6487-4844-4441-477", companyEventDetail.getValue());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateQBDTCompanyCoreQBInfoChanged() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company1 = c3dl.persistCompany3();
        CompanyDTO company2 = c3dl.getCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company2.getQuickBooksInfo().setApplicationVersion("50.00.R.9/20716#pro");
        company2.getQuickBooksInfo().setLicenseNumber("6487-4844-4441-477");

        ProcessResult<Company> result2 = PayrollServices.companyManager.updateQBCompanyInfo(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents
                (foundCompany, EventTypeCode.QuickBooksInfoChanged, CompanyEventStatus.Active, null, null);

        assertEquals("CompanyEvent List Size", 1, companyEvents.size());

        CompanyEvent companyEvent = companyEvents.iterator().next();
        DomainEntitySet<CompanyEventDetail> companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.Details).sort(CompanyEventDetail.Value());
        assertEquals("Details List Size", 2, companyEventDetails.size());
        CompanyEventDetail companyEventDetail = companyEventDetails.get(0);
        assertEquals("CompanyEventDetail Value", "Application Version", companyEventDetail.getValue());
        companyEventDetail = companyEventDetails.get(1);
        assertEquals("CompanyEventDetail Value", "License Number", companyEventDetail.getValue());

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.OldStringValue).sort(CompanyEventDetail.Value());
        assertEquals("OldStringValue List Size", 2, companyEventDetails.size());
        companyEventDetail = companyEventDetails.get(0);
        assertEquals("CompanyEventDetail Value", "17.00.R.9/20716#pro", companyEventDetail.getValue());
        companyEventDetail = companyEventDetails.get(1);
        assertEquals("CompanyEventDetail Value", "6487-4844-4441-476", companyEventDetail.getValue());

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewStringValue).sort(CompanyEventDetail.Value());
        assertEquals("NewStringValue List Size", 2, companyEventDetails.size());
        companyEventDetail = companyEventDetails.get(0);
        assertEquals("CompanyEventDetail Value", "50.00.R.9/20716#pro", companyEventDetail.getValue());
        companyEventDetail = companyEventDetails.get(1);
        assertEquals("CompanyEventDetail Value", "6487-4844-4441-477", companyEventDetail.getValue());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addServiceCoreNullCompanyId() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO companyToCopyFrom = new CompanyDTO();
        ProcessResult<Company> ddServiceAddProcessResult = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBOE, null, companyToCopyFrom);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, ddServiceAddProcessResult.getMessages().size());
        Message errorMessage = ddServiceAddProcessResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void addServiceCoreNullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO companyToCopyFrom = new CompanyDTO();
        ProcessResult<Company> ddServiceAddProcessResult = PayrollServices.companyManager.updateQBCompanyInfo(null, "1234567", companyToCopyFrom);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, ddServiceAddProcessResult.getMessages().size());
        Message errorMessage = ddServiceAddProcessResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void updateQBCompanyInfoCore_CompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO updCompany2 = dataloader.getTestIntuitCompany2();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateQBCompanyInfo(
                SourceSystemCode.valueOf(updCompany2.getSourceSystemCd().toString()), "upd_id_dne", updCompany2);
        PayrollServices.commitUnitOfWork();

        assertEquals(1, result2.getMessages().size());
        assertEquals("169", result2.getMessages().get(0).getMessageCode());
        assertEquals("Company QBOE:upd_id_dne does not exist.", result2.getMessages().get(0).getMessage());
    }

    @Test
    public void updateQBCompanyInfoCore_NullFromCompany() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.commitUnitOfWork();

        CompanyDTO company2 = null;
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateQBCompanyInfo(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), company2);
        PayrollServices.commitUnitOfWork();
        assertEquals(1, result2.getMessages().size());
        assertEquals("141", result2.getMessages().get(0).getMessageCode());
        assertEquals("Company is not specified.", result2.getMessages().get(0).getMessage());
    }

    private void assertCompaniesEqual(CompanyDTO pDTOCompany, Company pDomainCompany) {
        AddCompanyDataLoader.assertCompaniesEqual(pDTOCompany, pDomainCompany);
    }

}