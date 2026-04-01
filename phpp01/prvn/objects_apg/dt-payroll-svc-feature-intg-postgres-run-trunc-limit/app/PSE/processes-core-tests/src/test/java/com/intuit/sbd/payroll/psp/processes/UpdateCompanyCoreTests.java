package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.common.CompanyRealmValidator;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Contains the unit tests for the <CODE>Message</CODE> class.
 *
 * @author: chetzler
 * @version: Jun 20, 2007
 */
public class UpdateCompanyCoreTests {

    private DataLoader dataloader = new DataLoader();
    private String fraudNotes = "This company was not activated because one or more fields match the company,  Intuit(Source System=QBOE Source ID=123456) with status of On Hold (Fraud).  The list of fields matched are as follows:\n" +
            "COMPANY EMAIL: notifications@intuit.com\n";


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
    public void updateCompanyCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        CompanyDTO company2 = dataloader.getTestIntuitCompany2();
        company2.setSourceSystemCd(company1.getSourceSystemCd());
        company2.setCompanyId(company1.getSourceCompanyId());
        company2.setFein("384757575");
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addMonths(1);
        company2.setTaxExemptExpirationDate(new DateDTO(taxExemptExpirationDate));
        company2.setTaxExemptStatus(TaxExemptStatusCode.Exempt);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(foundCompany);
        assertCompaniesEqual(company2, foundCompany);
        CompanyEvent event = null;
        if (companyEvents.size() == 1) {
            event = companyEvents.get(0);
        }
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of EINChanged events", 1, CompanyEvent.getEventCountByType(foundCompany, EventTypeCode.EINChanged));
    }

    @Test
    public void updateCompanyCoreSuccessWithRealmIdSuccess() {
        // Disable Launch Darkly Flag
        CompanyRealmValidator.setUseLaunchDarkly(false);
        // Enable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(true);
        String companyRealm = "9123456890";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789");
        DataLoadServices.addDDService(company);

        // Update Company with Realm Id
        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789", company.getFedTaxId());
        companyDTO.setIAMRealmId(companyRealm);
        Company updatedCompany = DataLoadServices.updateCompany(SourceSystemCode.QBDT, "123456789", companyDTO);
        assertEquals(companyRealm, updatedCompany.getIAMRealmId());

        // Disable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }

    @Test
    public void updateCompanyCoreSuccessWithQuickBooksRealmIdSuccess() {
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
        Company updatedCompany = DataLoadServices.updateCompany(SourceSystemCode.QBDT, "123456789", companyDTO);
        assertEquals(companyRealm, updatedCompany.getIAMRealmId());

        // Disable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }

    @Test
    public void updateCompanyCoreSuccessWithSameRealmId() {
        // Disable Launch Darkly Flag
        CompanyRealmValidator.setUseLaunchDarkly(false);
        CompanyRealmValidator.setEnableRealmValidation(false);
        String companyRealm = "9123456890";

        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);

        // Update Company with Realm Id
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789", company.getFedTaxId());
        companyDTO.setIAMRealmId(companyRealm);
        Company updatedCompany = DataLoadServices.updateCompany(SourceSystemCode.QBDT, "123456789", companyDTO);
        assertEquals(companyRealm, updatedCompany.getIAMRealmId());

        // Enable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(true);
        // Create other Company with same IAMRealmId
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789", company.getFedTaxId());
        companyDTO.setIAMRealmId(companyRealm);
        updatedCompany = DataLoadServices.updateCompany(SourceSystemCode.QBDT, "123456789", companyDTO);
        assertEquals(companyRealm, updatedCompany.getIAMRealmId());

        // Disable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }

    @Test
    public void updateCompanyCoreSuccessWithSameQuickBooksRealmId() {
        // Disable Launch Darkly Flag
        CompanyRealmValidator.setUseLaunchDarkly(false);
        CompanyRealmValidator.setEnableRealmValidation(false);
        String companyRealm = "9123456890";

        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);

        // Update Company with Realm Id
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789", company.getFedTaxId());
        companyDTO.getQuickBooksInfo().setIAMRealmId(companyRealm);
        Company updatedCompany = DataLoadServices.updateCompany(SourceSystemCode.QBDT, "123456789", companyDTO);
        assertEquals(companyRealm, updatedCompany.getIAMRealmId());

        // Enable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(true);
        // Create other Company with same IAMRealmId
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789", company.getFedTaxId());
        companyDTO.setIAMRealmId(companyRealm);
        updatedCompany = DataLoadServices.updateCompany(SourceSystemCode.QBDT, "123456789", companyDTO);
        assertEquals(companyRealm, updatedCompany.getIAMRealmId());

        // Disable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }

    @Test
    public void updateCompanyCoreSuccessWithDifferentRealmId() {
        // Disable Launch Darkly Flag
        CompanyRealmValidator.setUseLaunchDarkly(false);
        CompanyRealmValidator.setEnableRealmValidation(false);
        String companyRealm = "9123456890";

        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);

        // Update Company with Realm Id
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789", company.getFedTaxId());
        companyDTO.getQuickBooksInfo().setIAMRealmId(companyRealm);
        Company updatedCompany = DataLoadServices.updateCompany(SourceSystemCode.QBDT, "123456789", companyDTO);
        assertEquals(companyRealm, updatedCompany.getIAMRealmId());

        // Enable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(true);
        // Create other Company with same IAMRealmId
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789", company.getFedTaxId());
        companyDTO.setIAMRealmId("91234568901");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> prAddCompany = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, "123456789", companyDTO);
        assertRealmUpdateNotAllowed(prAddCompany);
        PayrollServices.rollbackUnitOfWork();

        // Disable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }

    @Test
    public void updateCompanyCoreSuccessChangeAddress() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        CompanyDTO company2 = dataloader.getTestIntuitCompany2();
        company2.setSourceSystemCd(company1.getSourceSystemCd());
        company2.setCompanyId(company1.getSourceCompanyId());
        company2.setFein("123456789");
        AddressDTO newAddressDTO = new AddressDTO();
        newAddressDTO.setAddressLine1("new line 1");
        newAddressDTO.setAddressLine2("new line 2");
        newAddressDTO.setAddressLine3("new line 3");
        newAddressDTO.setCity("cityNew");
        newAddressDTO.setState("OH");
        newAddressDTO.setCountry("USANEW");
        newAddressDTO.setZipCode("89521");
        newAddressDTO.setZipCodeExtension("5533");
        company2.setLegalAddress(newAddressDTO);
        SpcfCalendar taxExpirationDate = PSPDate.getPSPTime();
        taxExpirationDate.addMonths(1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());

        assertCompaniesEqual(company2, foundCompany);
        PayrollServices.commitUnitOfWork();
        // Both Legal Name and Address have changed, so 2 events have to be created
        assertEquals("Number of LegalName Changed events", 1, CompanyEvent.getEventCountByType(foundCompany, EventTypeCode.LegalNameChanged));
        assertEquals("Number of LegalAddress Changed events", 1, CompanyEvent.getEventCountByType(foundCompany, EventTypeCode.LegalAddressChanged));
    }

    @Test
    public void updateAssistedCompanyCoreSuccess_ChangeEIN() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Tax, ServiceCode.Cloud);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, Agency.IRS);
        assertNotNull(companyAgency);
       // assertEquals(company.getFedTaxId(), companyAgency.getAgencyTaxpayerId());

        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("333333333");
        ProcessResult pr = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PSP_PRAssert.assertSuccess("update company", pr);       
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals(companyDTO.getFein(), company.getFedTaxId());
        companyAgency = CompanyAgency.findCompanyAgency(company, Agency.IRS);
        assertNotNull(companyAgency);

     //   assertEquals(company.getFedTaxId(), companyAgency.getAgencyTaxpayerId());
        assertEquals("Number of EIN Changed events", 1, CompanyEvent.getEventCountByType(company, EventTypeCode.EINChanged));
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void updateQBDTCompanyCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company1 = c3dl.persistCompany3();
        CompanyDTO company2 = c3dl.getCompany1();

        company2.getQuickBooksInfo().setApplicationId("NewAppId");
        company2.getQuickBooksInfo().setApplicationVersion("NewAppVersion");
        company2.getQuickBooksInfo().setLicenseNumber("NewLicNum");
        company2.getQuickBooksInfo().setTaxTableId("383838");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(foundCompany);
        boolean hasEINChangedEvent = false;
        for (CompanyEvent currEvent : companyEvents) {
            if (currEvent.getEventTypeCd() == EventTypeCode.EINChanged) {
                hasEINChangedEvent = true;
            }
        }
        assertCompaniesEqual(company2, foundCompany);
        PayrollServices.commitUnitOfWork();

        //ensure that there was not an EIN change event created
        assertFalse("Company does not have an EIN changed event", hasEINChangedEvent);
    }

    @Test
    public void updateApplicationVersionChanged() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company1 = c3dl.persistCompany3();
        CompanyDTO company2 = c3dl.getCompany1();

        company2.getQuickBooksInfo().setApplicationId("NewAppId");
        company2.getQuickBooksInfo().setApplicationVersion("50.00.R.9/20716#pro");
        company2.getQuickBooksInfo().setTaxTableId("383838");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
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

        company2.getQuickBooksInfo().setApplicationVersion("22.00.R.8/383838#pro");
        company2.getQuickBooksInfo().setQuickbooksSku("pro");
        company2.getQuickBooksInfo().setApplicationId("NewAppId");
        company2.getQuickBooksInfo().setLicenseNumber("6487-4844-4441-477");
        company2.getQuickBooksInfo().setTaxTableId("383838");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
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
        assertEquals("Details List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "License Number");

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.OldStringValue);
        assertEquals("OldStringValue List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "6487-4844-4441-476");

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewStringValue);
        assertEquals("NewStringValue List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "6487-4844-4441-477");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company2 = c3dl.getCompany1();

        company2.getQuickBooksInfo().setApplicationVersion("22.00.R.8/383838#pro");
        company2.getQuickBooksInfo().setQuickbooksSku("pro");
        company2.getQuickBooksInfo().setApplicationId("ChangedAppId");
        company2.getQuickBooksInfo().setLicenseNumber("6487-4844-4441-478");
        company2.getQuickBooksInfo().setTaxTableId("383838");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        companyEvents = CompanyEvent.findCompanyEvents
                (foundCompany, EventTypeCode.QuickBooksInfoChanged, CompanyEventStatus.Active, null, null);

        // The licensenumber or app version has not been changed since it is from client to client
        assertEquals("CompanyEvent List Size", 1, companyEvents.size());

        company2 = c3dl.getCompany1();

        company2.getQuickBooksInfo().setApplicationVersion("23.00.R.8/383838#pro");
        company2.getQuickBooksInfo().setQuickbooksSku("pro");
        company2.getQuickBooksInfo().setApplicationId("ChangedAppId");
        company2.getQuickBooksInfo().setLicenseNumber("6487-4844-4441-479");
        company2.getQuickBooksInfo().setTaxTableId("383838");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        companyEvents = CompanyEvent.findCompanyEvents
                (foundCompany, EventTypeCode.QuickBooksInfoChanged, CompanyEventStatus.Active, null, null);

        // The licensenumber and app version has been changed since it is a higher version
        assertEquals("CompanyEvent List Size", 2, companyEvents.size());
        companyEvent = companyEvents.get(1);
        if(companyEvents.get(0).getEventTimeStamp().getTimeInMilliseconds()>companyEvent.getEventTimeStamp().getTimeInMilliseconds()){
            companyEvent=companyEvents.get(0);
        }
        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.Details);
        assertEquals("Details List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "License Number");

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.OldStringValue);
        assertEquals("OldStringValue List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "6487-4844-4441-477");

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewStringValue);
        assertEquals("NewStringValue List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "6487-4844-4441-479");

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void updateLicenseNumberChangedForAcct() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company1 = c3dl.persistCompany3();
        CompanyDTO company2 = c3dl.getCompany1();

        company2.getQuickBooksInfo().setApplicationVersion("22.00.R.8/383838#acct");
        company2.getQuickBooksInfo().setQuickbooksSku("belacct");
        company2.getQuickBooksInfo().setApplicationId("NewAppId");
        company2.getQuickBooksInfo().setLicenseNumber("6487-4844-4441-477");
        company2.getQuickBooksInfo().setTaxTableId("383838");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
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
        assertEquals("Details List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "License Number");

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.OldStringValue);
        assertEquals("OldStringValue List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "6487-4844-4441-476");

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewStringValue);
        assertEquals("NewStringValue List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "6487-4844-4441-477");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company2 = c3dl.getCompany1();

        company2.getQuickBooksInfo().setApplicationVersion("22.00.R.8/383838#pro");
        company2.getQuickBooksInfo().setQuickbooksSku("pro");
        company2.getQuickBooksInfo().setApplicationId("ChangedAppId");
        company2.getQuickBooksInfo().setLicenseNumber("6487-4844-4441-478");
        company2.getQuickBooksInfo().setTaxTableId("383838");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        companyEvents = CompanyEvent.findCompanyEvents
                (foundCompany, EventTypeCode.QuickBooksInfoChanged, CompanyEventStatus.Active, null, null);

        // The licensenumber and app version has been changed since it is from accountant to client, even though its the same version
        assertEquals("CompanyEvent List Size", 2, companyEvents.size());
        companyEvent = companyEvents.get(1);
        if(companyEvents.get(0).getEventTimeStamp().getTimeInMilliseconds()>companyEvent.getEventTimeStamp().getTimeInMilliseconds()){
            companyEvent=companyEvents.get(0);
        }
        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.Details);
        assertEquals("Details List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "License Number");

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.OldStringValue);
        assertEquals("OldStringValue List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "6487-4844-4441-477");

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewStringValue);
        assertEquals("NewStringValue List Size", 2, companyEventDetails.size());
        assertCompanyDetail(companyEventDetails, "6487-4844-4441-478");

        PayrollServices.commitUnitOfWork();

    }


    @Test
    public void updateQuickBooksInfoChanged() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company1 = c3dl.persistCompany3();
        CompanyDTO company2 = c3dl.getCompany1();

        company2.getQuickBooksInfo().setApplicationId("NewAppId");
        company2.getQuickBooksInfo().setApplicationVersion("50.00.R.9/20716#pro");
        company2.getQuickBooksInfo().setLicenseNumber("6487-4844-4441-477");
        company2.getQuickBooksInfo().setTaxTableId("383838");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
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
    public void updateCompanyCore_RemovedAContact() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistCompany(dataloader.getTestIntuitCompany());
        dataloader.persistTestCompanyService(company);
        PayrollServices.commitUnitOfWork();

        CompanyDTO companyDTO = dataloader.getTestIntuitCompany_Removed1Contact();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result);
    }


    @Test
    public void updateCompanyCore_ChangedPayrollAdmin() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistCompany(dataloader.getTestIntuitCompany());
        dataloader.persistTestCompanyService(company);
        PayrollServices.commitUnitOfWork();

        CompanyDTO companyDTO = dataloader.getTestIntuitCompany_ChangedPayrollAdmin();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result);
    }

    @Test
    public void updateCompanyCore_ChangedContact() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistCompany(dataloader.getTestIntuitCompany());
        dataloader.persistTestCompanyService(company);
        PayrollServices.commitUnitOfWork();

        CompanyDTO companyDTO = dataloader.getTestIntuitCompany_Changed1Contact();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result);
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
        ProcessResult<Company> ddServiceAddProcessResult = PayrollServices.companyManager.updateCompany(null, "1234567", companyToCopyFrom);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, ddServiceAddProcessResult.getMessages().size());
        Message errorMessage = ddServiceAddProcessResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void updateCompanyCore_CompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO updCompany2 = dataloader.getTestIntuitCompany2();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
                SourceSystemCode.valueOf(updCompany2.getSourceSystemCd().toString()), "upd_id_dne", updCompany2);
        PayrollServices.commitUnitOfWork();

        assertEquals(1, result2.getMessages().size());
        assertEquals("169", result2.getMessages().get(0).getMessageCode());
        assertEquals("Company QBOE:upd_id_dne does not exist.", result2.getMessages().get(0).getMessage());
    }

    @Test
    public void updateCompanyCore_NullFromCompany() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.commitUnitOfWork();

        CompanyDTO company2 = null;
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), company2);
        PayrollServices.commitUnitOfWork();
        assertEquals(1, result2.getMessages().size());
        assertEquals("141", result2.getMessages().get(0).getMessageCode());
        assertEquals("Company is not specified.", result2.getMessages().get(0).getMessage());
    }

    @Test
    public void updateCompanyCore_NoAccountSignatory() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        CompanyDTO company2 = dataloader.getTestIntuitCompany_NoAccountSignatory();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(company1.getSourceSystemCd(),
                                                                                     company1.getSourceCompanyId(),
                                                                                     company2);
        PayrollServices.commitUnitOfWork();

        System.out.println(result);

        assertEquals(1, result.getMessages().size());
        assertEquals("207", result.getMessages().get(0).getMessageCode());
        assertEquals("Company QBOE:123456 must have at least one Contact who is an Account Signatory.",
                     result.getMessages().get(0).getMessage());
    }

    @Test
    public void updateCompanyCore_CompanyNotActive() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);

        //Make the company inactive
        CompanyService companyService = CompanyService.findCompanyService(company1, ServiceCode.DirectDeposit);
        companyService.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        company1 = PayrollServicesTest.save(company1);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPrincipalToAgent();

        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = dataloader.getTestIntuitCompany2();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();
        System.out.println(result2);

        assertEquals(0, result2.getMessages().size());
    }

    @Test
    //Create company 1 with a service
    //Term company 1
    //Create company 2 with a service
    //Set company 2 to be inactive
    //Set company 2's EIN to be company 1's ein
    public void updateCompanyCore_EINTerminated() {
        //Setup
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);

        //Terminate the company
        ProcessResult<CompanyService> terminateProcess = PayrollServices.companyManager.terminateService
                (company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        assertSuccess(terminateProcess);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company2 = dataloader.persistTestIntuitCompany2();
        CompanyService ddCompanyService2 = dataloader.persistTestCompanyService(company2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO companyToUpdate = dataloader.getTestIntuitCompany2();
        companyToUpdate.setFein(dataloader.getTestIntuitCompany().getFein());
        ProcessResult<Company> updProcresult = PayrollServices.companyManager.updateCompany(
                company2.getSourceSystemCd(),
                company2.getSourceCompanyId(),
                companyToUpdate);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(1, updProcresult.getMessages().size());
        Assert.assertEquals("1045", updProcresult.getMessages().get(0).getMessageCode());
        Assert.assertEquals(
                "Company " + company2.getSourceSystemCd() + ":" + company2
                        .getSourceCompanyId() + " could not be updated because the EIN " + company1
                        .getFedTaxId() + " was previously used in a company that was Terminated.",
                updProcresult.getMessages().get(0).getMessage());
    }

    @Test
    public void updateCompanyCore_EIN_InUse_DiffSrcSystems() {
        //Setup
        PayrollServices.beginUnitOfWork();
        dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBOE);
        Company company1qboe = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1qboe);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        Company company2qbdt = dataloader.persistTestIntuitCompany2();
        dataloader.persistTestCompanyService(company2qbdt);
        PayrollServices.commitUnitOfWork();

        Assert.assertTrue(company2qbdt.getSourceSystemCd() != company1qboe.getSourceSystemCd());
        Assert.assertTrue(! company2qbdt.getFedTaxId().equals(company1qboe.getFedTaxId()));

        // now update the QBDT company so it has the same EIN as the QBOE company
        CompanyDTO dtoUpdate = dataloader.getTestIntuitCompany2();
        dtoUpdate.setFein(company1qboe.getFedTaxId());
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                company2qbdt.getSourceSystemCd(),
                company2qbdt.getSourceCompanyId(),
                dtoUpdate);
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess("update succeeded", result);
        Company updatedCompany2qbdt = result.getResult();
        Assert.assertTrue(updatedCompany2qbdt.getFedTaxId().equals(company1qboe.getFedTaxId()));
    }

    @Test
    //Create company 1 with a service
    //Create company 2 with a service
    // Attempt to update company 2 to have the same ein as company 1
    public void updateCompanyCore_EIN_InUse_SameSourceSystems() {
        //Setup
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company2 = dataloader.persistTestIntuitCompany2();
        CompanyService ddCompanyService2 = dataloader.persistTestCompanyService(company2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO companyToUpdate = dataloader.getTestIntuitCompany2();
        companyToUpdate.setFein(dataloader.getTestIntuitCompany().getFein());
        ProcessResult<Company> updProcresult = PayrollServices.companyManager.updateCompany(
                company2.getSourceSystemCd(),
                company2.getSourceCompanyId(),
                companyToUpdate);
        PayrollServices.commitUnitOfWork();

        //Verifications
        Assert.assertEquals(1, updProcresult.getMessages().size());
        Assert.assertEquals("1044", updProcresult.getMessages().get(0).getMessageCode());
        Assert.assertEquals(
                "Company " + company2.getSourceSystemCd() + ":" + company2
                        .getSourceCompanyId() + " could not be updated because the EIN " + company1
                        .getFedTaxId() + " is already in use.",
                updProcresult.getMessages().get(0).getMessage());
    }

    //TODO:OFFERINGS MOVE TO UPDATESERVICECORETESTS
//    @Test
//    public void updateCompanyCoreNoSuchOffering() {
//        // add a company we can update
//        PayrollServices.beginUnitOfWork();
//        CompanyDTO company1 = dataloader.getTestIntuitCompany();
//        ProcessResult<Company> resultCompany = DataLoader.addCompany(company1);
//        Assert.assertTrue(resultCompany.isSuccess());
//
//        // get the offering, for later verification
//        Offering origOffering = resultCompany.getResult().getOffering();
//        Assert.assertTrue(origOffering != null);
//
//        // add the DD service
//        DDServiceInfoDTO dtoServiceInfo = new DDServiceInfoDTO();
//        dtoServiceInfo.setServiceCode(ServiceCode.DirectDeposit);
//        dtoServiceInfo.setAveragePayrollAmount(new BigDecimal("150.00"));
//        dtoServiceInfo.setHighAnnualPayrollAmount(new BigDecimal("250.00"));
//        ProcessResult resultService = PayrollServices.companyManager.addService(
//                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dtoServiceInfo);
//        Assert.assertTrue(resultService.isSuccess());
//
//        // query back the CompanyService for later comparison of its status
//        CompanyService csOrig = CompanyService.findCompanyService(resultCompany.getResult(), ServiceCode.DirectDeposit);
//        Assert.assertTrue(csOrig != null);
//        Assert.assertTrue(csOrig.getStatusCd() == ServiceSubStatusCode.PendingBankVerification);
//
//        // update the company with an offering that doesn't exist
//        OfferingInfoDTO dtoOfferingInfo = new OfferingInfoDTO();
//        dtoOfferingInfo.setSKU("NO_SUCH_OFFERING_SKU");
//        company1.setLegalName(company1.getLegalName() + " Again");
//        ProcessResult<Company> resultUpdate = PayrollServices.companyManager.updateCompany(
//                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), company1, dtoOfferingInfo);
//        Assert.assertTrue(!resultUpdate.isSuccess());
//        Assert.assertTrue(resultUpdate.getMessages().size() == 1);
//        Assert.assertTrue(resultUpdate.getMessages().get(0).getMessageCode().equals("5001")); // invalid value
//
//        // make sure the CompanyService status hasn't changed
//        CompanyService csAfterUpdate = CompanyService.findCompanyService(resultUpdate.getResult(), ServiceCode.DirectDeposit);
//        Assert.assertTrue(csAfterUpdate != null);
//        Assert.assertTrue(csAfterUpdate.getStatusCd() == ServiceSubStatusCode.PendingBankVerification);
//
//        PayrollServices.commitUnitOfWork();
//    }

        //TODO:OFFERINGS MOVE TO UPDATESERVICECORETESTS
//    @Test
//    public void updateCompanyCoreSameOffering() {
//        // add a company we can update
//        PayrollServices.beginUnitOfWork();
//        CompanyDTO company1 = dataloader.getTestIntuitCompany();
//        ProcessResult<Company> resultCompany = DataLoader.addCompany(company1);
//        Assert.assertTrue(resultCompany.isSuccess());
//
//        // get the offering, for later verification
//        Offering origOffering = resultCompany.getResult().getOffering();
//        Assert.assertTrue(origOffering != null);
//
//        // add the DD service
//        DDServiceInfoDTO dtoServiceInfo = new DDServiceInfoDTO();
//        dtoServiceInfo.setServiceCode(ServiceCode.DirectDeposit);
//        dtoServiceInfo.setAveragePayrollAmount(new BigDecimal("150.00"));
//        dtoServiceInfo.setHighAnnualPayrollAmount(new BigDecimal("250.00"));
//        ProcessResult resultService = PayrollServices.companyManager.addService(
//                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dtoServiceInfo);
//        Assert.assertTrue(resultService.isSuccess());
//
//        // query back the CompanyService for later comparison of its status
//        CompanyService csOrig = CompanyService.findCompanyService(resultCompany.getResult(), ServiceCode.DirectDeposit);
//        Assert.assertTrue(csOrig != null);
//        Assert.assertTrue(csOrig.getStatusCd() == ServiceSubStatusCode.PendingBankVerification);
//
//        // update the company with the same offering
//        company1.setLegalName(company1.getLegalName() + " Again");
//        ProcessResult<Company> resultUpdate = PayrollServices.companyManager.updateCompany(
//                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), company1, OfferingInfoDTO.DIY_WITH_DD);
//        Assert.assertTrue(resultUpdate.isSuccess());
//        Assert.assertTrue(resultUpdate.getResult().getOffering().equals(origOffering));
//
//        // make sure the CompanyService status hasn't changed
//        CompanyService csAfterUpdate = CompanyService.findCompanyService(resultUpdate.getResult(), ServiceCode.DirectDeposit);
//        Assert.assertTrue(csAfterUpdate != null);
//        Assert.assertTrue(csAfterUpdate.getStatusCd() == ServiceSubStatusCode.PendingBankVerification);
//
//        PayrollServices.commitUnitOfWork();
//    }
//
//    @Test
//    public void updateCompanyCoreNewOffering() {
//        String offeringSKU = "WITHOUT_ANY_SERVICES";
//
//        // first, make a second Offering that we'll assign to the company when we update it
//        Offering offeringForUpdate = Offering.findBySKU(offeringSKU);
//
//        if (offeringForUpdate == null) {
//            ProcessResult<Offering> resultOffering = OfferingTests.createOffering(offeringSKU, "An offering without any services", "blah blah blah", null);
//            Assert.assertTrue(resultOffering.isSuccess());
//            offeringForUpdate = resultOffering.getResult();
//        }
//
//        PayrollServices.beginUnitOfWork();
//        // create a company that we can update
//        CompanyDTO company1 = dataloader.getTestIntuitCompany();
//        ProcessResult<Company> resultCompany = DataLoader.addCompany(company1);
//        Assert.assertTrue(resultCompany.isSuccess());
//        Assert.assertTrue(!resultCompany.getResult().getOffering().equals(offeringForUpdate));
//
//        // add the DD service
//        DDServiceInfoDTO dtoServiceInfo = new DDServiceInfoDTO();
//        dtoServiceInfo.setServiceCode(ServiceCode.DirectDeposit);
//        dtoServiceInfo.setAveragePayrollAmount(new BigDecimal("150.00"));
//        dtoServiceInfo.setHighAnnualPayrollAmount(new BigDecimal("250.00"));
//        ProcessResult resultService = PayrollServices.companyManager.addService(
//                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dtoServiceInfo);
//        Assert.assertTrue(resultService.isSuccess());
//
//        // query back the CompanyService for later comparison of its status
//        CompanyService csOrig = CompanyService.findCompanyService(resultCompany.getResult(), ServiceCode.DirectDeposit);
//        Assert.assertTrue(csOrig != null);
//        Assert.assertTrue(csOrig.getStatusCd() == ServiceSubStatusCode.PendingBankVerification);
//
//        // update the company with the new offering
//        OfferingInfoDTO dtoNewOffering = new OfferingInfoDTO();
//        dtoNewOffering.setSKU(offeringSKU);
//        ProcessResult<Company> resultUpdate = PayrollServices.companyManager.updateCompany(
//                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), company1, dtoNewOffering);
//        assertSuccess(resultUpdate);
//        Assert.assertTrue(resultUpdate.getResult().getOffering().equals(offeringForUpdate));
//
//        // make sure the CompanyService status has changed, because the new offering doesn't include that service
//        CompanyService csUpdated = CompanyService.findCompanyService(resultUpdate.getResult(), ServiceCode.DirectDeposit);
//        PayrollServices.commitUnitOfWork();
//        Assert.assertTrue(csUpdated != null);
//        Assert.assertTrue(csUpdated.getStatusCd() == ServiceSubStatusCode.Cancelled);
//        // todo: the process sets it to PendingTermination, but what should it really be?
//    }

    private void assertCompaniesEqual(CompanyDTO pDTOCompany, Company pDomainCompany) {
        AddCompanyDataLoader.assertCompaniesEqual(pDTOCompany, pDomainCompany);
    }

    //Add company 1
    //Put company 1 on fraud review
    //Add company 2
    //Update company 2 email to match company 1 email
    @Test
    public void updateCompanyCoreFailsFraudControls() {
        //Setup
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);

        PayrollServices.companyManager.addOnHoldReason(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceSubStatusCode.Fraud);
        PayrollServicesTest.save(company1);

        CompanyDTO company2 = dataloader.getTestIntuitCompany2();
        company2.setCompanyId("844447464");

        Company domainCompany2 = dataloader.persistCompany(company2);
        CompanyService company2DDService = dataloader.persistTestCompanyService(domainCompany2);
        PayrollServices.commitUnitOfWork();

        company2 = dataloader.getTestIntuitCompany2();
        company2.setCompanyId("844447464");
        company2.setFein("847656466");
        company2.setNotificationEmail(company1.getNotificationEmail());

        //Test
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> updateCompanyProcResult =
                PayrollServices.companyManager.updateCompany(
                        SourceSystemCode.valueOf(company2.getSourceSystemCd().toString()),
                        company2.getCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());
        boolean isOnHold = foundCompany.isCompanyOnHold();

        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        int numNotes = notes.size();
        String noteContents = "";
        if (numNotes == 1) {
            Iterator<CompanyNote> itrNotes = notes.iterator();
            noteContents = itrNotes.next().getNotes();
        }
        PayrollServices.commitUnitOfWork();

        //Verifications
        assertSuccess("updateCompany", updateCompanyProcResult);
    }

    @Test
    public void updateCompanyCoreChangeCOAAccounts_Null2ndTime() {
        PspPrincipal principal = Application.getCurrentPrincipal();
        boolean isAgentFlag = principal.isAgent();
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Change the COA Fee Account Name
        QuickbooksInfoDTO quickBooksInfo = new QuickbooksInfoDTO();
        quickBooksInfo.setCoaFeeAccountName("NewCOAFeeAccount");
        quickBooksInfo.setCoaSalesTaxAccountName("NewCOASalesTaxAccount");
        CompanyDTO updatedCompany = dataloader.getTestIntuitCompany();
        updatedCompany.setQuickBooksInfo(quickBooksInfo);
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), updatedCompany);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(foundCompany);
        assertCompaniesEqual(dataloader.getTestIntuitCompany(), foundCompany);
        // verify the new COA Account values
        assertEquals("COA Fee Account", foundCompany.getQuickbooksInfo().getCoaFeeAccountName(), "NewCOAFeeAccount");
        assertEquals("COA Sales Tax Account", foundCompany.getQuickbooksInfo().getCoaSalesTaxAccountName(), "NewCOASalesTaxAccount");

        assertTrue("Number of company events >= 2", companyEvents.size() >= 2);
        DomainEntitySet<CompanyEvent> coaFeeAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaFeeAccountChange, null, null, null);
        DomainEntitySet<CompanyEvent> coaSalesTaxAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaSalesTaxAccountChange, null, null, null);
        assertEquals("Number of COA Fee Account Changed events", 1, coaFeeAccountChangeEvents.size());
        assertEquals("Number of COA Sales Tax Account Changed events", 1, coaSalesTaxAccountChangeEvents.size());

        // verify the event details
        CompanyEvent feeAccountChangeEvent = coaFeeAccountChangeEvents.get(0);
        DomainEntitySet<CompanyEventDetail> eventDetails = feeAccountChangeEvent.getCompanyEventDetailCollection();
        DomainEntitySet<CompanyEventDetail> newCoaNameEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.NewCoaName));
        DomainEntitySet<CompanyEventDetail> oldCoaNameEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.OldCoaName));
        DomainEntitySet<CompanyEventDetail> coaNameChangeByAgentEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.CoaNameChangeByAgent));

        assertEquals(1, newCoaNameEvents.size());
        assertEquals("Event Detail Value", "NewCOAFeeAccount", newCoaNameEvents.get(0).getValue());
        assertEquals(1, oldCoaNameEvents.size());
        assertEquals("Event Detail Value", null, oldCoaNameEvents.get(0).getValue());
        assertEquals(1, coaNameChangeByAgentEvents.size());
        assertEquals("Event Detail Value", Boolean.toString(isAgentFlag), coaNameChangeByAgentEvents.get(0).getValue());

        CompanyEvent salesTaxAccountChangeEvent = coaSalesTaxAccountChangeEvents.get(0);
        eventDetails = salesTaxAccountChangeEvent.getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());
        newCoaNameEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.NewCoaName));
        oldCoaNameEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.OldCoaName));
        coaNameChangeByAgentEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.CoaNameChangeByAgent));

        assertEquals(1, newCoaNameEvents.size());
        assertEquals("Event Detail Value", "NewCOASalesTaxAccount", newCoaNameEvents.get(0).getValue());
        assertEquals(1, oldCoaNameEvents.size());
        assertEquals("Event Detail Value", null, oldCoaNameEvents.get(0).getValue());
        assertEquals(1, coaNameChangeByAgentEvents.size());
        assertEquals("Event Detail Value", Boolean.toString(isAgentFlag), coaNameChangeByAgentEvents.get(0).getValue());
        PayrollServices.commitUnitOfWork();

        // Again update with null QuickbooksInfoDTO
        PayrollServices.beginUnitOfWork();
        result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), dataloader.getTestIntuitCompany());
        PayrollServices.commitUnitOfWork();

        // Verify the Quickbooks info is not changed and no new events are generated
        PayrollServices.beginUnitOfWork();
        foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        companyEvents = CompanyEvent.findCompanyEvents(foundCompany);
        assertCompaniesEqual(dataloader.getTestIntuitCompany(), foundCompany);
        // verify the new COA Account values
        assertEquals("Quick books info", foundCompany.getQuickbooksInfo().getApplicationId(), null);
        assertEquals("Quick books info", foundCompany.getQuickbooksInfo().getApplicationVersion(), null);
        assertEquals("Quick books info", foundCompany.getQuickbooksInfo().getCoaFeeAccountName(), null);
        assertEquals("Quick books info", foundCompany.getQuickbooksInfo().getCoaSalesTaxAccountName(), null);
        assertEquals("Quick books info", foundCompany.getQuickbooksInfo().getLicenseNumber(), null);
        assertEquals("Quick books info", foundCompany.getQuickbooksInfo().getTaxTableId(), null);

        // Both Legal Name and Address have changed, so 2 events have to be created
        assertTrue("Number of company events >= 4", companyEvents.size() >= 4);
        coaFeeAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaFeeAccountChange, null, null, null);
        coaSalesTaxAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaSalesTaxAccountChange, null, null, null);
        assertEquals("Number of COA Fee Account Changed events", 2, coaFeeAccountChangeEvents.size());
        assertEquals("Number of COA Sales Tax Account Changed events", 2, coaSalesTaxAccountChangeEvents.size());
        DomainEntitySet<CompanyEvent> coaFeeCompanyEventsList = coaFeeAccountChangeEvents.sort(CompanyEvent.EventTimeStamp());

        DomainEntitySet<CompanyEvent> coaSalesTaxCompanyEventsList = coaSalesTaxAccountChangeEvents.sort(CompanyEvent.EventTimeStamp());

        // verify the event details
        feeAccountChangeEvent = coaFeeCompanyEventsList.get(1);
        eventDetails = feeAccountChangeEvent.getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());
        newCoaNameEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.NewCoaName));
        oldCoaNameEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.OldCoaName));
        coaNameChangeByAgentEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.CoaNameChangeByAgent));

        assertEquals(1, newCoaNameEvents.size());
        assertEquals("Event Detail Value", null, newCoaNameEvents.get(0).getValue());
        assertEquals(1, oldCoaNameEvents.size());
        assertEquals("Event Detail Value", "NewCOAFeeAccount", oldCoaNameEvents.get(0).getValue());
        assertEquals(1, coaNameChangeByAgentEvents.size());
        assertEquals("Event Detail Value", Boolean.toString(isAgentFlag), coaNameChangeByAgentEvents.get(0).getValue());

        salesTaxAccountChangeEvent = coaSalesTaxCompanyEventsList.get(1);
        eventDetails = salesTaxAccountChangeEvent.getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());
        newCoaNameEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.NewCoaName));
        oldCoaNameEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.OldCoaName));
        coaNameChangeByAgentEvents = eventDetails.find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.CoaNameChangeByAgent));

        assertEquals(1, newCoaNameEvents.size());
        assertEquals("Event Detail Value", null, newCoaNameEvents.get(0).getValue());
        assertEquals(1, oldCoaNameEvents.size());
        assertEquals("Event Detail Value", "NewCOASalesTaxAccount", oldCoaNameEvents.get(0).getValue());
        assertEquals(1, coaNameChangeByAgentEvents.size());
        assertEquals("Event Detail Value", Boolean.toString(isAgentFlag), coaNameChangeByAgentEvents.get(0).getValue());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateCompanyCore_ChangeCOAAccountsTwice() {
        PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();
        boolean isAgentFlag = principal.isAgent();

        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Change the COA Fee Account Name
        QuickbooksInfoDTO quickBooksInfo = new QuickbooksInfoDTO();
        quickBooksInfo.setCoaFeeAccountName("NewCOAFeeAccount");
        quickBooksInfo.setCoaSalesTaxAccountName("NewCOASalesTaxAccount");
        CompanyDTO updatedCompany = dataloader.getTestIntuitCompany();
        updatedCompany.setQuickBooksInfo(quickBooksInfo);
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), updatedCompany);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(foundCompany);
        assertCompaniesEqual(dataloader.getTestIntuitCompany(), foundCompany);
        // verify the new COA Account values
        assertEquals("COA Fee Account", foundCompany.getQuickbooksInfo().getCoaFeeAccountName(), "NewCOAFeeAccount");
        assertEquals("COA Sales Tax Account", foundCompany.getQuickbooksInfo().getCoaSalesTaxAccountName(), "NewCOASalesTaxAccount");

        // Both Legal Name and Address have changed, so 2 events have to be created
        assertTrue("Number of company events >= 2", companyEvents.size() >= 2);
        DomainEntitySet<CompanyEvent> coaFeeAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaFeeAccountChange, null, null, null);
        DomainEntitySet<CompanyEvent> coaSalesTaxAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaSalesTaxAccountChange, null, null, null);
        assertEquals("Number of COA Fee Account Changed events", 1, coaFeeAccountChangeEvents.size());
        assertEquals("Number of COA Sales Tax Account Changed events", 1, coaSalesTaxAccountChangeEvents.size());

        // verify the event details
        CompanyEvent feeAccountChangeEvent = coaFeeAccountChangeEvents.get(0);
        DomainEntitySet<CompanyEventDetail> eventDetails = feeAccountChangeEvent.getCompanyEventDetailCollection();
        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

        assertEquals("Event Detail Code", EventDetailTypeCode.NewCoaName, eventDetails.get(1).getEventDetailTypeCd());
        assertEquals("Event Detail Value", "NewCOAFeeAccount", eventDetails.get(1).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.OldCoaName, eventDetails.get(2).getEventDetailTypeCd());
        assertEquals("Event Detail Value", null, eventDetails.get(2).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.CoaNameChangeByAgent, eventDetails.get(0).getEventDetailTypeCd());
        assertEquals("Event Detail Value", Boolean.toString(isAgentFlag), eventDetails.get(0).getValue());
        CompanyEvent salesTaxAccountChangeEvent = coaSalesTaxAccountChangeEvents.get(0);
        eventDetails = salesTaxAccountChangeEvent.getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());
        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

        assertEquals("Event Detail Code", EventDetailTypeCode.NewCoaName, eventDetails.get(1).getEventDetailTypeCd());
        assertEquals("Event Detail Value", "NewCOASalesTaxAccount", eventDetails.get(1).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.OldCoaName, eventDetails.get(2).getEventDetailTypeCd());
        assertEquals("Event Detail Value", null, eventDetails.get(2).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.CoaNameChangeByAgent, eventDetails.get(0).getEventDetailTypeCd());
        assertEquals("Event Detail Value", Boolean.toString(isAgentFlag), eventDetails.get(0).getValue());
        PayrollServices.commitUnitOfWork();

        // Again update with different values
        PayrollServices.beginUnitOfWork();
        quickBooksInfo = new QuickbooksInfoDTO();
        quickBooksInfo.setCoaFeeAccountName("NewCOAFeeAccount2");
        quickBooksInfo.setCoaSalesTaxAccountName("NewCOASalesTaxAccount2");
        updatedCompany = dataloader.getTestIntuitCompany();
        updatedCompany.setQuickBooksInfo(quickBooksInfo);
        result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), updatedCompany);
        PayrollServices.commitUnitOfWork();

        // Verify the Quickbooks info is not changed and no new events are generated
        PayrollServices.beginUnitOfWork();
        foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        companyEvents = CompanyEvent.findCompanyEvents(foundCompany);
        assertCompaniesEqual(dataloader.getTestIntuitCompany(), foundCompany);
        // verify the new COA Account values
        assertEquals("COA Fee Account", foundCompany.getQuickbooksInfo().getCoaFeeAccountName(), "NewCOAFeeAccount2");
        assertEquals("COA Sales Tax Account", foundCompany.getQuickbooksInfo().getCoaSalesTaxAccountName(), "NewCOASalesTaxAccount2");

        // Both Legal Name and Address have changed, so 2 events have to be created
        assertTrue("Number of company events >= 4", companyEvents.size() >= 4);
        coaFeeAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaFeeAccountChange, null, null, null);
        coaSalesTaxAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaSalesTaxAccountChange, null, null, null);
        assertEquals("Number of COA Fee Account Changed events", 2, coaFeeAccountChangeEvents.size());
        assertEquals("Number of COA Sales Tax Account Changed events", 2, coaSalesTaxAccountChangeEvents.size());
        DomainEntitySet<CompanyEvent> coaFeeCompanyEventsList = coaFeeAccountChangeEvents.sort(CompanyEvent.EventTimeStamp());
        DomainEntitySet<CompanyEvent> coaSalesTaxCompanyEventsList = coaSalesTaxAccountChangeEvents.sort(CompanyEvent.EventTimeStamp());

        // verify the event details
        feeAccountChangeEvent = coaFeeCompanyEventsList.get(1);
        eventDetails = feeAccountChangeEvent.getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());
        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

        assertEquals("Event Detail Code", EventDetailTypeCode.NewCoaName, eventDetails.get(1).getEventDetailTypeCd());
        assertEquals("Event Detail Value", "NewCOAFeeAccount2", eventDetails.get(1).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.OldCoaName, eventDetails.get(2).getEventDetailTypeCd());
        assertEquals("Event Detail Value", "NewCOAFeeAccount", eventDetails.get(2).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.CoaNameChangeByAgent, eventDetails.get(0).getEventDetailTypeCd());
        assertEquals("Event Detail Value", Boolean.toString(isAgentFlag), eventDetails.get(0).getValue());
        salesTaxAccountChangeEvent = coaSalesTaxCompanyEventsList.get(1);
        eventDetails = salesTaxAccountChangeEvent.getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());

        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

        assertEquals("Event Detail Code", EventDetailTypeCode.NewCoaName, eventDetails.get(1).getEventDetailTypeCd());
        assertEquals("Event Detail Value", "NewCOASalesTaxAccount2", eventDetails.get(1).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.OldCoaName, eventDetails.get(2).getEventDetailTypeCd());
        assertEquals("Event Detail Value", "NewCOASalesTaxAccount", eventDetails.get(2).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.CoaNameChangeByAgent, eventDetails.get(0).getEventDetailTypeCd());
        assertEquals("Event Detail Value", Boolean.toString(isAgentFlag), eventDetails.get(0).getValue());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateCompanyCore_ChangeCOAAccounts_PartialNulls() {
        PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();
        boolean isAgentFlag = principal.isAgent();

        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Change the COA Fee Account Name
        QuickbooksInfoDTO quickBooksInfo = new QuickbooksInfoDTO();
        quickBooksInfo.setCoaFeeAccountName(null);
        quickBooksInfo.setCoaSalesTaxAccountName("NewCOASalesTaxAccount");
        CompanyDTO updatedCompany = dataloader.getTestIntuitCompany();
        updatedCompany.setQuickBooksInfo(quickBooksInfo);
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), updatedCompany);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(foundCompany);
        assertCompaniesEqual(dataloader.getTestIntuitCompany(), foundCompany);
        // verify the new COA Account values
        assertEquals("COA Fee Account", foundCompany.getQuickbooksInfo().getCoaFeeAccountName(), null);
        assertEquals("COA Sales Tax Account", foundCompany.getQuickbooksInfo().getCoaSalesTaxAccountName(), "NewCOASalesTaxAccount");

        // Both Legal Name and Address have changed, so 2 events have to be created
        assertTrue("Number of company events >= 1", companyEvents.size() >= 1);
        DomainEntitySet<CompanyEvent> coaFeeAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaFeeAccountChange, null, null, null);
        DomainEntitySet<CompanyEvent> coaSalesTaxAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaSalesTaxAccountChange, null, null, null);
        assertEquals("Number of COA Fee Account Changed events", 0, coaFeeAccountChangeEvents.size());
        assertEquals("Number of COA Sales Tax Account Changed events", 1, coaSalesTaxAccountChangeEvents.size());

        CompanyEvent salesTaxAccountChangeEvent = coaSalesTaxAccountChangeEvents.get(0);
        DomainEntitySet<CompanyEventDetail> eventDetails = salesTaxAccountChangeEvent.getCompanyEventDetailCollection();
        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());
        assertEquals("Event Detail Code", EventDetailTypeCode.NewCoaName, eventDetails.get(1).getEventDetailTypeCd());
        assertEquals("Event Detail Value", "NewCOASalesTaxAccount", eventDetails.get(1).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.OldCoaName, eventDetails.get(2).getEventDetailTypeCd());
        assertEquals("Event Detail Value", null, eventDetails.get(2).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.CoaNameChangeByAgent, eventDetails.get(0).getEventDetailTypeCd());
        assertEquals("Event Detail Value", Boolean.toString(isAgentFlag), eventDetails.get(0).getValue());
        PayrollServices.commitUnitOfWork();

        // Again update with different values
        PayrollServices.beginUnitOfWork();
        quickBooksInfo = new QuickbooksInfoDTO();
        quickBooksInfo.setCoaFeeAccountName("NewCOAFeeAccount");
        quickBooksInfo.setCoaSalesTaxAccountName(null);
        updatedCompany = dataloader.getTestIntuitCompany();
        updatedCompany.setQuickBooksInfo(quickBooksInfo);
        result2 = PayrollServices.companyManager.updateCompany(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), updatedCompany);
        PayrollServices.commitUnitOfWork();

        // Verify the Quickbooks info is not changed and no new events are generated
        PayrollServices.beginUnitOfWork();
        foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        companyEvents = CompanyEvent.findCompanyEvents(foundCompany);
        assertCompaniesEqual(dataloader.getTestIntuitCompany(), foundCompany);
        // verify the new COA Account values
        assertEquals("COA Fee Account", foundCompany.getQuickbooksInfo().getCoaFeeAccountName(), "NewCOAFeeAccount");
        assertEquals("COA Sales Tax Account", foundCompany.getQuickbooksInfo().getCoaSalesTaxAccountName(), null);

        // Both Legal Name and Address have changed, so 2 events have to be created
        assertTrue("Number of company events >= 3", companyEvents.size() >= 3);
        coaFeeAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaFeeAccountChange, null, null, null);
        coaSalesTaxAccountChangeEvents = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.CoaSalesTaxAccountChange, null, null, null);
        assertEquals("Number of COA Fee Account Changed events", 1, coaFeeAccountChangeEvents.size());
        assertEquals("Number of COA Sales Tax Account Changed events", 2, coaSalesTaxAccountChangeEvents.size());

        DomainEntitySet<CompanyEvent> coaSalesTaxCompanyEventsList = coaSalesTaxAccountChangeEvents.sort(CompanyEvent.EventTimeStamp());

        // verify the event details
        CompanyEvent feeAccountChangeEvent = coaFeeAccountChangeEvents.get(0);
        eventDetails = feeAccountChangeEvent.getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());
        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

        assertEquals("Event Detail Code", EventDetailTypeCode.NewCoaName, eventDetails.get(1).getEventDetailTypeCd());
        assertEquals("Event Detail Value", "NewCOAFeeAccount", eventDetails.get(1).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.OldCoaName, eventDetails.get(2).getEventDetailTypeCd());
        assertEquals("Event Detail Value", null, eventDetails.get(2).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.CoaNameChangeByAgent, eventDetails.get(0).getEventDetailTypeCd());
        assertEquals("Event Detail Value", Boolean.toString(isAgentFlag), eventDetails.get(0).getValue());
        salesTaxAccountChangeEvent = coaSalesTaxCompanyEventsList.get(1);
        eventDetails = salesTaxAccountChangeEvent.getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());
        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

        assertEquals("Event Detail Code", EventDetailTypeCode.NewCoaName, eventDetails.get(1).getEventDetailTypeCd());
        assertEquals("Event Detail Value", null, eventDetails.get(1).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.OldCoaName, eventDetails.get(2).getEventDetailTypeCd());
        assertEquals("Event Detail Value", "NewCOASalesTaxAccount", eventDetails.get(2).getValue());
        assertEquals("Event Detail Code", EventDetailTypeCode.CoaNameChangeByAgent, eventDetails.get(0).getEventDetailTypeCd());
        assertEquals("Event Detail Value", Boolean.toString(isAgentFlag), eventDetails.get(0).getValue());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateQuickBooksInfoChanged_Cloud() {
        DTOFactory dtoFactory = new DTOFactory();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment, ServiceCode.ThirdParty401k);        

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyDTO companyDTO = dtoFactory.create(company);

        companyDTO.getQuickBooksInfo().setApplicationId("NewAppId");
        companyDTO.getQuickBooksInfo().setApplicationVersion("50.00.R.9/20716#pro");
        companyDTO.getQuickBooksInfo().setLicenseNumber("6487-4844-4441-477");
        companyDTO.getQuickBooksInfo().setTaxTableId("383838");

        ProcessResult<Company> updateCompanyResult = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PSP_PRAssert.assertSuccess("update company", updateCompanyResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents
                (company, EventTypeCode.QuickBooksInfoChanged, CompanyEventStatus.Active, null, null);

        assertEquals("CompanyEvent List Size", 1, companyEvents.size());

        CompanyEvent companyEvent = companyEvents.iterator().next();
        DomainEntitySet<CompanyEventDetail> companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.Details).sort(CompanyEventDetail.Value());
        assertEquals("Details List Size", 2, companyEventDetails.size());
        CompanyEventDetail companyEventDetail = companyEventDetails.get(0);
        assertEquals("CompanyEventDetail Value", "Application Version", companyEventDetail.getValue());
        companyEventDetail = companyEventDetails.get(1);
        assertEquals("CompanyEventDetail Value", "License Number", companyEventDetail.getValue());

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewStringValue).sort(CompanyEventDetail.Value());;
        assertEquals("NewStringValue List Size", 2, companyEventDetails.size());
        companyEventDetail = companyEventDetails.get(0);
        assertEquals("CompanyEventDetail Value", "50.00.R.9/20716#pro", companyEventDetail.getValue());
        companyEventDetail = companyEventDetails.get(1);
        assertEquals("CompanyEventDetail Value", "6487-4844-4441-477", companyEventDetail.getValue());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void updateCompanyCoreChangeAddress_CloudDDBP401k() {
        DTOFactory dtoFactory = new DTOFactory();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyDTO companyDTO = dtoFactory.create(company);
        companyDTO.setLegalName("New Company Name");
        companyDTO.setFein("123456789");
        AddressDTO newAddressDTO = new AddressDTO();
        newAddressDTO.setAddressLine1("new line 1");
        newAddressDTO.setAddressLine2("new line 2");
        newAddressDTO.setAddressLine3("new line 3");
        newAddressDTO.setCity("cityNew");
        newAddressDTO.setState("OH");
        newAddressDTO.setCountry("USANEW");
        newAddressDTO.setZipCode("89521");
        newAddressDTO.setZipCodeExtension("5533");
        companyDTO.setLegalAddress(newAddressDTO);
        ProcessResult<Company> updateCompanyResult = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PSP_PRAssert.assertSuccess("update company", updateCompanyResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        assertCompaniesEqual(companyDTO, foundCompany);

        // Both Legal Name and Address have changed, so 2 events have to be created
        assertEquals("Number of LegalName Changed events", 1, CompanyEvent.getEventCountByType(foundCompany, EventTypeCode.LegalNameChanged));
        assertEquals("Number of LegalAddress Changed events", 1, CompanyEvent.getEventCountByType(foundCompany, EventTypeCode.LegalAddressChanged));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void updatePayrollAdmin_CloudDDBP401k() {
        DTOFactory dtoFactory = new DTOFactory();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyDTO companyDTO = dtoFactory.create(company);

        ContactDTO contactToRemove = null;
        ContactDTO contactToAdd = null;
        for (ContactDTO contactDTO : companyDTO.getContacts()) {
            if(contactDTO.getContactRoleCd() == ContactRole.PayrollAdmin) {
                contactToRemove = contactDTO;
                contactToAdd = new ContactDTO();
                contactToAdd.setAccountSignatory(contactDTO.getAccountSignatory());
                contactToAdd.setAddress(contactDTO.getAddress());
                contactToAdd.setCommunicationTypeCd(contactDTO.getCommunicationTypeCd());
                contactToAdd.setContactRoleCd(contactDTO.getContactRoleCd());
                contactToAdd.setEmail(contactDTO.getEmail());
                contactToAdd.setFirstName("New");
                contactToAdd.setLastName("Contact");
                contactToAdd.setContactId("NewContact");
                contactToAdd.setPhoneNumber(contactDTO.getPhoneNumber());
                contactToAdd.setJobTitle(contactDTO.getJobTitle());
                contactToAdd.setTitle(contactDTO.getTitle());
            }
        }

        companyDTO.getContacts().remove(contactToRemove);
        companyDTO.getContacts().add(contactToAdd);
        
        ProcessResult<Company> updateCompanyResult = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PSP_PRAssert.assertSuccess("update company", updateCompanyResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertNotNull("Contact", company.getContact("NewContact"));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testFEINChangeEntitlementUpdates() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("123456789");
        ProcessResult processResult = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PSP_PRAssert.assertSuccess("update company ProcessResult", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("entitlement units", 2, company.getEntitlementUnitCollection().size());
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
            if(entitlementUnit.getFedTaxId().equals("123456789")) {
                assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
            } else { //todo this should be never activated when it is added
                assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
            }
        }
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testFEINChangeEntitlementUpdates_SameEIN() {
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company1, "123456", "456789");

        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company2, "123456", "456789");

        PayrollServices.beginUnitOfWork();
        company2 = Company.findCompany(company2.getSourceCompanyId(), company2.getSourceSystemCd());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company2);
        companyDTO.setFein(company1.getFedTaxId());
        ProcessResult processResult = PayrollServices.companyManager.updateCompany(company2.getSourceSystemCd(), company2.getSourceCompanyId(), companyDTO);
        PSP_PRAssert.assertContains("update company", 319, MessageInfo.MessageLevel.ERROR, processResult);
        PayrollServices.rollbackUnitOfWork();        
    }

    @Test
    public void testUpgradeDownGradeWithoutSubmission() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Cloud, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyService assisted = company.getService(ServiceCode.Tax);
        assertNotNull(assisted);
        assertEquals(ServiceSubStatusCode.PendingSetup, assisted.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        // downgrade
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.addDDService(company);

        // try to update some company information
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNameControlValue(){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        assertNotNull(company.getNameControl());
        String psid = company.getSourceCompanyId();

        //Length should be 4 or less
        String nameControl = company.getNameControl() + "4";
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId(psid);
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> companyProcessResult = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, companyDTO.getCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals(1, companyProcessResult.getMessages().size());
        Message msg = companyProcessResult.getMessages().get(0);
        assertEquals("191", msg.getMessageCode());
        assertEquals("NameControl "+nameControl+" for company "+psid+" is not valid.", msg.getMessage());

        //Whitespaces in the Name Control
        nameControl = company.getNameControl().replace("E"," ");
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId(psid);
        PayrollServices.beginUnitOfWork();
        companyProcessResult = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, companyDTO.getCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals(1, companyProcessResult.getMessages().size());
        msg = companyProcessResult.getMessages().get(0);
        assertEquals("191", msg.getMessageCode());
        assertEquals("NameControl "+nameControl+" for company "+psid+" is not valid.", msg.getMessage());

        //Special characters in the Name Control - only - and & are allowed
        nameControl = company.getNameControl().replace("E","!");
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId(psid);
        PayrollServices.beginUnitOfWork();
        companyProcessResult = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, companyDTO.getCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals(1, companyProcessResult.getMessages().size());
        msg = companyProcessResult.getMessages().get(0);
        assertEquals("191", msg.getMessageCode());
        assertEquals("NameControl "+nameControl+" for company "+psid+" is not valid.", msg.getMessage());

        //Valid NameControl values
        //Length less than 4 is valid
        nameControl = company.getNameControl().substring(2);
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId(psid);
        PayrollServices.beginUnitOfWork();
        companyProcessResult = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, companyDTO.getCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(companyProcessResult);
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);

        //Special Characters - &
        nameControl = company.getNameControl().replace("S","&");
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId(psid);
        PayrollServices.beginUnitOfWork();
        companyProcessResult = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, companyDTO.getCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(companyProcessResult);
        company1 = Company.findCompany(psid, SourceSystemCode.QBDT);

        nameControl = company.getNameControl().replace("S","-");
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId(psid);
        PayrollServices.beginUnitOfWork();
        companyProcessResult = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, companyDTO.getCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(companyProcessResult);
        company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
    }

    @Test
    public void updateCompanyCore_addSsnAndDateOfBirth() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud);
        dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);

        SpcfCalendar todayDate = PSPDate.getPSPTime();
        CalendarUtils.clearTime(todayDate);
        CompanyDTO companyDTO = dataloader.getTestIntuitCompany_addSsnAndDob("999999999",todayDate);

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result);
        Contact primaryPrincipal = result.getResult().getContactByRoleCode(ContactRole.PrimaryPrincipal);
        assertEquals("999999999" , primaryPrincipal.getSocialSecurityNumberPlainText());
        assertEquals(todayDate.toUtc(), primaryPrincipal.getDateOfBirth());
    }

    @Test
    public void updateCompanyCore_addIndustryType() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistCompany(dataloader.getTestIntuitCompany());
        dataloader.persistTestCompanyService(company);
        PayrollServices.commitUnitOfWork();

        CompanyDTO companyDTO = dataloader.getTestIntuitCompany_addIndustry("Hair Salon, Beauty Salon, or Barber Shop");

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result);
        junit.framework.Assert.assertNotNull(result.getResult().getCompanyAdditionalInfo());
        assertEquals("Beauty and Barber Shops", result.getResult().getCompanyAdditionalInfo().getIndustryType().getIndustry());
    }

    @Test
    public void updateCompanyCore_addOwnershipType() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistCompany(dataloader.getTestIntuitCompany());
        dataloader.persistTestCompanyService(company);
        PayrollServices.commitUnitOfWork();

        CompanyDTO companyDTO = dataloader.getTestIntuitCompany_addOwnership("Non-Profit Organization");

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result);
        assertNotNull(result.getResult().getCompanyAdditionalInfo());
        assertEquals("Non-Profit Organization", result.getResult().getCompanyAdditionalInfo().getOwnershipType().getOwnership());
    }


    @Test
    public void updateCompanyCore_changeSsnAndDateOfBirth() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistCompany(dataloader.getTestIntuitCompany());
        dataloader.persistTestCompanyService(company);
        PayrollServices.commitUnitOfWork();

        SpcfCalendar todayDate = PSPDate.getPSPTime();
        CompanyDTO companyDTO = dataloader.getTestIntuitCompany_addSsnAndDob("999999999",todayDate);

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result);
        Contact primaryPrincipal = result.getResult().getContactByRoleCode(ContactRole.PrimaryPrincipal);
        assertEquals("999999999" , primaryPrincipal.getSocialSecurityNumberPlainText());

        companyDTO = dataloader.getTestIntuitCompany_addSsnAndDob("888888889",todayDate);
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result);
        primaryPrincipal = result.getResult().getContactByRoleCode(ContactRole.PrimaryPrincipal);
        assertEquals("888888889" , primaryPrincipal.getSocialSecurityNumberPlainText());
        SpcfCalendar dateOfBirth= primaryPrincipal.getDateOfBirth();
        CalendarUtils.clearTime(todayDate);
        CalendarUtils.clearTime(dateOfBirth);

        Assert.assertEquals(todayDate.getYear(),dateOfBirth.getYear());
        Assert.assertEquals(todayDate.getMonth(),dateOfBirth.getMonth());
        Assert.assertEquals(todayDate.getDay(),dateOfBirth.getDay());

     }

    @Test
    public void updateCompanyCore_changeIndustryType() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistCompany(dataloader.getTestIntuitCompany());
        dataloader.persistTestCompanyService(company);
        PayrollServices.commitUnitOfWork();

        CompanyDTO companyDTO = dataloader.getTestIntuitCompany_addIndustry("Agriculture and Farming");

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        companyDTO = dataloader.getTestIntuitCompany_addIndustry("Design, Architecture, Engineering");

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result);
        junit.framework.Assert.assertNotNull(result.getResult().getCompanyAdditionalInfo());
        assertEquals("Architectural, Engineering, and Surveying Services", result.getResult().getCompanyAdditionalInfo().getIndustryType().getIndustry());
    }

    @Test
    public void updateCompanyCore_changeOwnershipType() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistCompany(dataloader.getTestIntuitCompany());
        dataloader.persistTestCompanyService(company);
        PayrollServices.commitUnitOfWork();

        CompanyDTO companyDTO = dataloader.getTestIntuitCompany_addOwnership("Limited Liability Corp");

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        companyDTO = dataloader.getTestIntuitCompany_addOwnership("Foreign Company");

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result);
        assertNotNull(result.getResult().getCompanyAdditionalInfo());
        assertEquals("Foreign Company", result.getResult().getCompanyAdditionalInfo().getOwnershipType().getOwnership());
    }

    private void assertCompanyDetail(DomainEntitySet<CompanyEventDetail> pCompanyEventDetails, String pSearchValue){
        CompanyEventDetail companyEventDetail = null;
        for(CompanyEventDetail companyEventDetail1 : pCompanyEventDetails) {
            if(companyEventDetail1.getValue().contains(pSearchValue)){
                companyEventDetail = companyEventDetail1;
                break;
            }
        }
        assertNotNull(companyEventDetail);
    }

    private void assertRealmUpdateNotAllowed(ProcessResult<Company> prAddCompany) {
        assertEquals(1, prAddCompany.getMessages().size());
        Message message = prAddCompany.getMessages().get(0);
        assertEquals(message.getMessageCode(), "10125");
        assertEquals("Realm update not allowed found for realm 91234568901", message.getMessage());
    }
}
