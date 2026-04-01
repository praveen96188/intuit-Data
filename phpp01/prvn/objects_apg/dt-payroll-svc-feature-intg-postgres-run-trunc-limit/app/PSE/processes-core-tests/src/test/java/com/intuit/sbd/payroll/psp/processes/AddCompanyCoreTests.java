/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.common.CompanyRealmValidator;
import com.intuit.sbd.payroll.psp.processes.dataloaders.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.Collection;
import java.util.Iterator;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Contains the unit tests for the <CODE>Message</CODE> class.
 *
 * @author: Dawn Martens
 * @version: Jun 20, 2007
 */
public class AddCompanyCoreTests {

    private String qboeFraudNotes = "This company was not activated because the company bank account matches the company bank account of company Intuit (Source System=QBOE Source ID=123456) with status of On Hold (Fraud).";

    private String qbdtFraudNotes = "This company was not activated because the company bank account matches the company bank account of company QB Desktop 3 (Source System=QBDT Source ID=8574536) with status of Terminated.";

    private String qboeFraudNotes_PhoneOnly = "This company was not activated because one or more fields match the company,  Intuit(Source System=QBOE Source ID=123456) with status of On Hold .  The list of fields matched are as follows:\n" +
            "LEGAL NAME: Intuit\n" +
            "DBA NAME: Intuit\n" +
            "COMPANY EMAIL: notifications@intuit.com\n" +
            "Legal: \n" +
            "6888 Sierra Cnt Pkwy \n" +
            "Reno, NV 89511\n" +
            "Mailing: \n" +
            "6887 Sierra Center Parkway \n" +
            "Suite 45\n" +
            "test line 3\n" +
            "Reno, NV 89521\n" +
            "CONTACT PHONE:(775) 561-1111\n";

    private String qbdtFraudNotes_otherSourceSystem = "This company was not activated because the company bank account matches the company bank account of company Intuit (Source System=QBOE Source ID=1234567) with status of Terminated.";

    private String qboeFraudNotesForSameEIN = "This company was not activated because one or more fields " +
            "match the company,  Intuit(Source System=QBDT Source ID=48484848488) with status of On Hold .  " +
            "The list of fields matched are as follows:\n" +
            "FED TAX ID: 847656466\n";

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }
    
    @Before
    public void runBeforeEachTest() {
        AddCompanyDataLoader.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    } 

    @Test
    public void add401kCompanyServiceCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        Company401kDataloader c1401kDL = new Company401kDataloader();
        c1401kDL.persistCompany1();
        PayrollServices.commitUnitOfWork();

         PayrollServices.beginUnitOfWork();
         Company foundCompany = Company.findCompany("1234567", SourceSystemCode.QBDT);
         AddCompanyDataLoader.assertCompaniesEqual(c1401kDL.getCompany1(), foundCompany);
         PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addDDAnd401kCompanyServiceCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        CompanyDDPlus401kDataLoader c1401kDL = new CompanyDDPlus401kDataLoader();
        c1401kDL.persistQBCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany("8575577", SourceSystemCode.QBDT);
        AddCompanyDataLoader.assertCompaniesEqual(c1401kDL.getCompany1(), foundCompany);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void add401kCompanyCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        Company401kDataloader c1401kDL = new Company401kDataloader();
        OfferingInfoDTO offeringInfoDTO;
        offeringInfoDTO = OfferingInfoDTO.THIRD_PARTY_401K;
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(c1401kDL.getCompany1());
        assertSuccess("addCompany", result);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addCompanyCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        AddCompanyDataLoader.assertCompaniesEqual(company1, foundCompany);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addQBDTCompanyCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        CompanyDTO company1 = c3dl.getCompany1();
        company1.setCurrentToken(10L);
        OfferingInfoDTO dtoOfferingInfo = new OfferingInfoDTO();
        //todo:Offerings Come back and fix this to add a service with this offering code
        dtoOfferingInfo.setPayrollSubTypeCd(PayrollSubtypeCode.Enhanced);
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        AddCompanyDataLoader.assertCompaniesEqual(company1, foundCompany);
    }

    @Test
    public void addQBDTCompanyCoreWithRealmIdSuccess() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        CompanyDTO company1 = c3dl.getCompany1();
        company1.setCurrentToken(10L);
        company1.setIAMRealmId("9123456890");
        OfferingInfoDTO dtoOfferingInfo = new OfferingInfoDTO();
        //todo:Offerings Come back and fix this to add a service with this offering code
        dtoOfferingInfo.setPayrollSubTypeCd(PayrollSubtypeCode.Enhanced);
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        AddCompanyDataLoader.assertCompaniesEqual(company1, foundCompany);
        DomainEntitySet realmAddedEvent = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.RealmIdAdded);
        Assert.assertEquals("There shold be one realm added event",realmAddedEvent.size(),1);

    }

    @Test
    public void addQBDTCompanyCoreWithQBDTInfoRealmIdSuccess() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        CompanyDTO company1 = c3dl.getCompany1();
        company1.setCurrentToken(10L);
        company1.getQuickBooksInfo().setIAMRealmId("9123456890");
        OfferingInfoDTO dtoOfferingInfo = new OfferingInfoDTO();
        //todo:Offerings Come back and fix this to add a service with this offering code
        dtoOfferingInfo.setPayrollSubTypeCd(PayrollSubtypeCode.Enhanced);
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        AddCompanyDataLoader.assertCompaniesEqual(company1, foundCompany);
        DomainEntitySet realmAddedEvent = CompanyEvent.findCompanyEvents(foundCompany, EventTypeCode.RealmIdAdded);
        Assert.assertEquals("There shold be one realm added event",realmAddedEvent.size(),1);

    }

    @Test
    public void addQBDTCompanyCoreWithActiveRealmId() {
        // Disable Launch Darkly Flag
        CompanyRealmValidator.setUseLaunchDarkly(false);
        // Enable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(true);
        String companyRealm = "9123456890";

        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        companyDTO.setIAMRealmId(companyRealm);
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);

        // Create other Company with same IAMRealmId
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "223456789");
        companyDTO.setIAMRealmId(companyRealm);
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> prAddCompany = PayrollServices.companyManager.addCompany(companyDTO);
        assertActiveCompanyExistsForRealm(prAddCompany);
        PayrollServices.rollbackUnitOfWork();

        // Disable Realm Validation
        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }

    @Test
    public void addQBDTCompanyCoreWithActiveQBDTInfoRealmId() {
        CompanyRealmValidator.setUseLaunchDarkly(false);
        CompanyRealmValidator.setEnableRealmValidation(true);
        String companyRealm = "9123456890";

        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        companyDTO.getQuickBooksInfo().setIAMRealmId(companyRealm);
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);

        // Create other Company with same IAMRealmId
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "223456789");
        companyDTO.getQuickBooksInfo().setIAMRealmId(companyRealm);
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> prAddCompany = PayrollServices.companyManager.addCompany(companyDTO);
        assertActiveCompanyExistsForRealm(prAddCompany);
        PayrollServices.rollbackUnitOfWork();

        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }

    @Test
    public void addQBDTCompanyCoreWithDuplicateRealmId() {
        CompanyRealmValidator.setUseLaunchDarkly(false);
        CompanyRealmValidator.setEnableRealmValidation(true);
        String companyRealm = "9123456890";

        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        companyDTO.setIAMRealmId(companyRealm);
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);

        CompanyRealmValidator.setEnableRealmValidation(false);
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "223456789");
        companyDTO.setIAMRealmId(companyRealm);
        company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);

        CompanyRealmValidator.setEnableRealmValidation(true);
        // Create other Company with same IAMRealmId
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "333456789");
        companyDTO.setIAMRealmId(companyRealm);
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> prAddCompany = PayrollServices.companyManager.addCompany(companyDTO);
        assertDuplicateCompanyExistsForRealm(prAddCompany);
        PayrollServices.rollbackUnitOfWork();

        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }

    @Test
    public void addQBDTCompanyCoreWithDuplicateQBDTInfoRealmId() {
        CompanyRealmValidator.setUseLaunchDarkly(false);
        CompanyRealmValidator.setEnableRealmValidation(true);
        String companyRealm = "9123456890";

        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        companyDTO.getQuickBooksInfo().setIAMRealmId(companyRealm);
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);

        CompanyRealmValidator.setEnableRealmValidation(false);
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "223456789");
        companyDTO.getQuickBooksInfo().setIAMRealmId(companyRealm);
        company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);

        CompanyRealmValidator.setEnableRealmValidation(true);
        // Create other Company with same IAMRealmId
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "333456789");
        companyDTO.getQuickBooksInfo().setIAMRealmId(companyRealm);
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> prAddCompany = PayrollServices.companyManager.addCompany(companyDTO);
        assertDuplicateCompanyExistsForRealm(prAddCompany);
        PayrollServices.rollbackUnitOfWork();

        CompanyRealmValidator.setEnableRealmValidation(false);
        CompanyRealmValidator.setUseLaunchDarkly(true);
    }


    //Mike Magness wanted to remove these additional fraud checks however there seemed to be a chance that they could be added back in later.
    //@Ignore
    @Test
    public void addCompanyCoreFailsFraudControls() {
        PayrollServices.beginUnitOfWork();
        Company company1 = AddCompanyDataLoader.dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = AddCompanyDataLoader.dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyBankAccount(company1);

        //Add Onhold Reason
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                    "123456",
                                                                                    ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("Add Onhold Reason : Fraud ", result);
        
        PayrollServices.beginUnitOfWork();
        company1 = Application.refresh(company1);

        CompanyDTO company2 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        company2.setCompanyId("48484848488");
        company2.setFein("847656466");
        DDServiceInfoDTO service2 = AddCompanyDataLoader.dataloader.getTestCompanyService();
        ProcessResult<Company> result2 = PayrollServices.companyManager.addCompany(company2);
        assertSuccess(result2);
        assertEquals("Number of messages", 0, result2.getMessages().size());

        //Ensure we can still add the service
        Company domainCompany2 = result2.getResult();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), service2);
        assertTrue(ddServiceAddProcessResult2.isSuccess());

        CompanyBankAccountDTO cbaDTO = PayrollServices.dtoFactory.create(company1.getCompanyBankAccountCollection().getFirst());
        ProcessResult addBankAccountResult = PayrollServices.companyManager.addCompanyBankAccount(company2.getSourceSystemCd(), company2.getCompanyId(), cbaDTO, true, true);
        assertTrue(addBankAccountResult.isSuccess());

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of messages", 1, addBankAccountResult.getMessages().size());
        Message errorMessage = addBankAccountResult.getMessages().get(0);

        Assert.assertEquals("Message code", "1040", errorMessage.getMessageCode());
        Assert.assertEquals("Message text",
                "Company QBOE:48484848488 was added but could not be activated because it matches an existing company that is either on hold or terminated.",
                errorMessage.getMessage().trim());
        Assert.assertEquals("Message level", MessageInfo.MessageLevel.WARNING,
                errorMessage.getLevel());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany("48484848488", company1.getSourceSystemCd());
        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        int numNotes = notes.size();
        String noteContents = "";
        if (numNotes == 1) {
            Iterator<CompanyNote> itrNotes = notes.iterator();
            noteContents = itrNotes.next().getNotes();
        }
        boolean isCompanyOnHold = foundCompany.isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = foundCompany.getOnHoldReasonCollection();
        int numOnHolds = onHoldReasons.size();
        OnHoldReason onHoldReason = null;
        if (numOnHolds == 1) {
            onHoldReason = onHoldReasons.iterator().next();
        }
        PayrollServices.commitUnitOfWork();

        assertNotNull(ddCompServiceInfo);

        PayrollServices.beginUnitOfWork();
        foundCompany = Company.findCompany("48484848488", company1.getSourceSystemCd());
        ddCompServiceInfo = (DDCompanyServiceInfo) PayrollServices.entityFinder.findById(DDCompanyServiceInfo.class, ddCompServiceInfo.getId());

        SpcfMoney expectedAvgPayRunAmt = new SpcfMoney("150.00");
        SpcfMoney expectedHighPayRunAmt = new SpcfMoney("250.00");

        assertEquals("Average payroll run amount", expectedAvgPayRunAmt, ddCompServiceInfo.getAveragePayRunAmount());
        assertEquals("High annual run amount", expectedHighPayRunAmt, ddCompServiceInfo.getHighAnnualPayAmount());
        assertEquals("Offload group", OffloadGroup.Codes.STANDARD,
                foundCompany.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Consecutive limit violations", 0L, ddCompServiceInfo.getConsecutiveLimitViolationCount());

        // the fraud event increments the token by one
        company2.setCurrentToken(company2.getCurrentToken() + 1);
        AddCompanyDataLoader.assertCompaniesEqual(company2, foundCompany);
        PayrollServices.commitUnitOfWork();
        Assert.assertNull("Comp Override", ddCompServiceInfo.getOverrideCompanyLimitAmount());
        Assert.assertNull("Emp Override", ddCompServiceInfo.getOverrideEmployeeLimitAmount());
        assertEquals("Service code", ServiceCode.DirectDeposit, ddCompServiceInfo.getService().getServiceCd());

        assertEquals("Number of on hold reasons", 1, numOnHolds);
        assertEquals("On hold reason", ServiceSubStatusCode.FraudReview, onHoldReason.getOnHoldReasonCd());
        assertTrue("Company is on hold", isCompanyOnHold);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                foundCompany,
                EventTypeCode.CompanyMatchesFraudulentCompany, CompanyEventStatus.Active, null, null);

        //Assertion for CompanyMatchesFraudulentCompany Event
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Event Details", qboeFraudNotes,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.SignUp),
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        PayrollServices.commitUnitOfWork();
    }

    //This fraud check was removed per Mike Magness but there is a change it might be put back in.
    @Ignore
    @Test
    public void addCompanyCoreFailsFraudControls_onlyPhoneDiffers() {
        PayrollServices.beginUnitOfWork();
        Company company1 = AddCompanyDataLoader.dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = AddCompanyDataLoader.dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        //Add Onhold Reason
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                    "123456",
                                                                                    ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("Add Onhold Reason : Fraud ", result);

        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        company2.setCompanyId("48484848488");
        company2.setFein("847656466");
        company2.setContacts(AddCompanyDataLoader.dataloader.getTestContactsPhoneNumberChanged());
        DDServiceInfoDTO service2 = AddCompanyDataLoader.dataloader.getTestCompanyService();
        ProcessResult<Company> result2 = PayrollServices.companyManager.addCompany(company2);
        assertSuccess(result2);
        assertEquals("Number of messages", 0, result2.getMessages().size());

        //Ensure we can still add the service
        Company domainCompany2 = result2.getResult();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), service2);

        PayrollServices.commitUnitOfWork();
        assertTrue(ddServiceAddProcessResult2.isSuccess());

        assertEquals("Number of messages", 1, ddServiceAddProcessResult2.getMessages().size());
        Message errorMessage = ddServiceAddProcessResult2.getMessages().get(0);

        Assert.assertEquals("Message code", "1040", errorMessage.getMessageCode());
        Assert.assertEquals("Message text",
                "Company QBOE:48484848488 was added but could not be activated because it matches an existing company that is either on hold or terminated.",
                errorMessage.getMessage().trim());
        Assert.assertEquals("Message level", MessageInfo.MessageLevel.WARNING,
                errorMessage.getLevel());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany("48484848488", company1.getSourceSystemCd());
        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        int numNotes = notes.size();
        String noteContents = "";
        if (numNotes == 1) {
            Iterator<CompanyNote> itrNotes = notes.iterator();
            noteContents = itrNotes.next().getNotes();
        }
        boolean isCompanyOnHold = foundCompany.isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = foundCompany.getOnHoldReasonCollection();
        int numOnHolds = onHoldReasons.size();
        OnHoldReason onHoldReason = null;
        if (numOnHolds == 1) {
            onHoldReason = onHoldReasons.iterator().next();
        }
        PayrollServices.commitUnitOfWork();

        assertNotNull(ddCompServiceInfo);

        PayrollServices.beginUnitOfWork();
        foundCompany = Company.findCompany("48484848488", company1.getSourceSystemCd());
        ddCompServiceInfo = (DDCompanyServiceInfo) PayrollServices.entityFinder.findById(DDCompanyServiceInfo.class, ddCompServiceInfo.getId());

        SpcfMoney expectedAvgPayRunAmt = new SpcfMoney("150.00");
        SpcfMoney expectedHighPayRunAmt = new SpcfMoney("250.00");

        assertEquals("Average payroll run amount", expectedAvgPayRunAmt, ddCompServiceInfo.getAveragePayRunAmount());
        assertEquals("High annual run amount", expectedHighPayRunAmt, ddCompServiceInfo.getHighAnnualPayAmount());
        assertEquals("Offload group", OffloadGroup.Codes.STANDARD,
                foundCompany.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Consecutive limit violations", 0L, ddCompServiceInfo.getConsecutiveLimitViolationCount());

        // the fraud event increments the token by one
        company2.setCurrentToken(company2.getCurrentToken() + 1);
        AddCompanyDataLoader.assertCompaniesEqual(company2, foundCompany);
        PayrollServices.commitUnitOfWork();
        Assert.assertNull("Comp Override", ddCompServiceInfo.getOverrideCompanyLimitAmount());
        Assert.assertNull("Emp Override", ddCompServiceInfo.getOverrideEmployeeLimitAmount());
        assertEquals("Service code", ServiceCode.DirectDeposit, ddCompServiceInfo.getService().getServiceCd());

        assertEquals("Number of on hold reasons", 1, numOnHolds);
        assertEquals("On hold reason", ServiceSubStatusCode.FraudReview, onHoldReason.getOnHoldReasonCd());
        assertTrue("Company is on hold", isCompanyOnHold);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                foundCompany,
                EventTypeCode.CompanyMatchesFraudulentCompany, CompanyEventStatus.Active, null, null);

        //Assertion for CompanyMatchesFraudulentCompany Event
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Event Details", qboeFraudNotes_PhoneOnly,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.SignUp),
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addCompanyCoreDoesNotFailFraudControls() {
        PayrollServices.beginUnitOfWork();
        Company company1 = AddCompanyDataLoader.dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = AddCompanyDataLoader.dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        //Add Onhold Reason
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                    "123456",
                                                                                    ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("Add Onhold Reason : Fraud ", result);

        PayrollServices.beginUnitOfWork();
        Company2Dataloader c2dl = new Company2Dataloader();
        CompanyDTO company2 = c2dl.getCompany1();
        DDServiceInfoDTO service2 = AddCompanyDataLoader.dataloader.getTestCompanyService();
        ProcessResult<Company> result2 = PayrollServices.companyManager.addCompany(company2);
        assertTrue(result2.isSuccess());
        assertEquals(0, result2.getMessages().size());

        //Ensure we can still add the service
        Company domainCompany2 = result2.getResult();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), service2);

        PayrollServices.commitUnitOfWork();
        assertTrue(ddServiceAddProcessResult2.isSuccess());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company2.getCompanyId(), SourceSystemCode.QBOE);
        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        int numNotes = notes.size();
        boolean bIsCompanyOnHold = foundCompany.isCompanyOnHold();
        PayrollServices.commitUnitOfWork();

        assertNotNull(ddCompServiceInfo);

        PayrollServices.beginUnitOfWork();
        foundCompany = Company.findCompany(company2.getCompanyId(), SourceSystemCode.QBOE);
        ddCompServiceInfo = (DDCompanyServiceInfo) PayrollServices.entityFinder.findById(DDCompanyServiceInfo.class, ddCompServiceInfo.getId());
        
        SpcfMoney expectedAvgPayRunAmt = new SpcfMoney("150.00");
        SpcfMoney expectedHighPayRunAmt = new SpcfMoney("250.00");

        assertEquals("Average payroll run amount", expectedAvgPayRunAmt, ddCompServiceInfo.getAveragePayRunAmount());
        assertEquals("High annual run amount", expectedHighPayRunAmt, ddCompServiceInfo.getHighAnnualPayAmount());
        assertEquals("Offload group", OffloadGroup.Codes.STANDARD,
                foundCompany.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Consecutive limit violations", 0L, ddCompServiceInfo.getConsecutiveLimitViolationCount());


        AddCompanyDataLoader.assertCompaniesEqual(company2, foundCompany);
        PayrollServices.commitUnitOfWork();
        Assert.assertNull("Comp Override", ddCompServiceInfo.getOverrideCompanyLimitAmount());
        Assert.assertNull("Emp Override", ddCompServiceInfo.getOverrideEmployeeLimitAmount());
        assertEquals("Service code", ServiceCode.DirectDeposit, ddCompServiceInfo.getService().getServiceCd());
        assertEquals("Number of notes", 0, numNotes);
        assertFalse("Company is on hold", bIsCompanyOnHold);
    }

    @Test
    public void addQBDTCompanyCoreFailsFraudControls_AcrossSourceSystem() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        Company company = c1dl.persistCompany1();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        ProcessResult pr = PayrollServices.companyManager.terminateService
                (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("terminate service", pr);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO company1Copy = c1dl.getCompany1();
        company1Copy.setCompanyId("48484848488");
        company1Copy.setFein("847656466");
        company1Copy.setSourceSystemCd(SourceSystemCode.QBDT);
        DDServiceInfoDTO service2 = AddCompanyDataLoader.dataloader.getTestCompanyService();
        ProcessResult<Company> result2 = PayrollServices.companyManager.addCompany(company1Copy);
        assertTrue(result2.isSuccess());
        assertEquals(0, result2.getMessages().size());

        PayrollServices.commitUnitOfWork();
        DataLoadServices.addEntitlementUnit(result2.getResult(), "123456", "654321");
        PayrollServices.beginUnitOfWork();

        company = Application.refresh(company);

        //Ensure we can still add the service
        Company domainCompany2 = result2.getResult();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), service2);
        assertTrue(ddServiceAddProcessResult2.isSuccess());

        CompanyBankAccountDTO cbaDTO = PayrollServices.dtoFactory.create(company.getCompanyBankAccountCollection().getFirst());
        ProcessResult addBankAccountResult = PayrollServices.companyManager.addCompanyBankAccount(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), cbaDTO, true, true);
        assertTrue(addBankAccountResult.isSuccess());

        PayrollServices.commitUnitOfWork();

        assertEquals(1, addBankAccountResult.getMessages().size());
        Message errorMessage = addBankAccountResult.getMessages().get(0);
        Assert.assertEquals("Message code", "1040", errorMessage.getMessageCode());
        Assert.assertEquals("Message text",
                "Company QBDT:48484848488 was added but could not be activated because it matches an existing " +
                        "company that is either on hold or terminated.",
                errorMessage.getMessage().trim());
        Assert.assertEquals("Message level", MessageInfo.MessageLevel.WARNING,
                errorMessage.getLevel());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1Copy.getCompanyId(),
                SourceSystemCode.valueOf(company1Copy.getSourceSystemCd().toString()));
        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        int numNotes = notes.size();
        String noteContents = "";
        if (numNotes == 1) {
            Iterator<CompanyNote> itrNotes = notes.iterator();
            noteContents = itrNotes.next().getNotes();
        }
        boolean isCompanyOnHold = foundCompany.isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = foundCompany.getOnHoldReasonCollection();
        int numOnHolds = onHoldReasons.size();
        OnHoldReason onHoldReason = null;
        if (numOnHolds == 1) {
            onHoldReason = onHoldReasons.iterator().next();
        }
        PayrollServices.commitUnitOfWork();

        assertNotNull(ddCompServiceInfo);

        PayrollServices.beginUnitOfWork();
        foundCompany = Company.findCompany(company1Copy.getCompanyId(),
            SourceSystemCode.valueOf(company1Copy.getSourceSystemCd().toString()));

        ddCompServiceInfo = (DDCompanyServiceInfo) PayrollServices.entityFinder.findById(DDCompanyServiceInfo.class, ddCompServiceInfo.getId());

        SpcfMoney expectedAvgPayRunAmt = new SpcfMoney("150.00");
        SpcfMoney expectedHighPayRunAmt = new SpcfMoney("250.00");

        assertEquals("Average payroll run amount", expectedAvgPayRunAmt, ddCompServiceInfo.getAveragePayRunAmount());
        assertEquals("High annual run amount", expectedHighPayRunAmt, ddCompServiceInfo.getHighAnnualPayAmount());
        assertEquals("Offload group", OffloadGroup.Codes.STANDARD,
                foundCompany.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Consecutive limit violations", 0L, ddCompServiceInfo.getConsecutiveLimitViolationCount());

        AddCompanyDataLoader.assertCompaniesEqual(company1Copy, foundCompany);
        PayrollServices.commitUnitOfWork();
        Assert.assertNull("Comp Override", ddCompServiceInfo.getOverrideCompanyLimitAmount());
        Assert.assertNull("Emp Override", ddCompServiceInfo.getOverrideEmployeeLimitAmount());
        assertEquals("Service code", ServiceCode.DirectDeposit, ddCompServiceInfo.getService().getServiceCd());

        assertEquals("Number of on hold reasons", 1, numOnHolds);
        assertEquals("On hold reason", ServiceSubStatusCode.FraudReview, onHoldReason.getOnHoldReasonCd());
        assertTrue("Company is on hold", isCompanyOnHold);

        //assertEquals("Number of notes", 1, numNotes);
        //assertEquals("Company note", qbdtFraudNotes, noteContents);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                foundCompany,
                EventTypeCode.CompanyMatchesFraudulentCompany, CompanyEventStatus.Active, null, null);

        //Assertion for CompanyMatchesFraudulentCompany Event
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Event Details", qbdtFraudNotes_otherSourceSystem,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.SignUp),
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addQBDTCompanyCoreFailsFraudControls() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company = c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        ProcessResult pr = PayrollServices.companyManager.terminateService
                (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("terminate service", pr);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO company1Copy = c3dl.getCompany1();
        company1Copy.setCompanyId("48484848488");
        company1Copy.setFein("847656466");
        DDServiceInfoDTO service2 = AddCompanyDataLoader.dataloader.getTestCompanyService();
        ProcessResult<Company> result2 = PayrollServices.companyManager.addCompany(company1Copy);
        assertSuccess(result2);
        assertEquals(0, result2.getMessages().size());

        PayrollServices.commitUnitOfWork();
        DataLoadServices.addEntitlementUnit(result2.getResult(), "123456", "654321");

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        //Ensure we can still add the service
        Company domainCompany2 = result2.getResult();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), service2);
        assertTrue(ddServiceAddProcessResult2.isSuccess());

        CompanyBankAccountDTO cbaDTO = PayrollServices.dtoFactory.create(company.getCompanyBankAccountCollection().getFirst());
        ProcessResult addBankAccountResult = PayrollServices.companyManager.addCompanyBankAccount(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), cbaDTO, true, true);
        assertTrue(addBankAccountResult.isSuccess());

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of messages", 1, addBankAccountResult.getMessages().size());
        Message errorMessage = addBankAccountResult.getMessages().get(0);

        Assert.assertEquals("Message code", "1040", errorMessage.getMessageCode());
        Assert.assertEquals("Message text",
                "Company QBDT:48484848488 was added but could not be activated because it matches an existing company that is either on hold or terminated.",
                errorMessage.getMessage().trim());
        Assert.assertEquals("Message level", MessageInfo.MessageLevel.WARNING,
                errorMessage.getLevel());


        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1Copy.getCompanyId(),
                SourceSystemCode.valueOf(company1Copy.getSourceSystemCd().toString()));
        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        int numNotes = notes.size();
        String noteContents = "";
        if (numNotes == 1) {
            Iterator<CompanyNote> itrNotes = notes.iterator();
            noteContents = itrNotes.next().getNotes();
        }
        boolean isCompanyOnHold = foundCompany.isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = foundCompany.getOnHoldReasonCollection();
        int numOnHolds = onHoldReasons.size();
        OnHoldReason onHoldReason = null;
        if (numOnHolds == 1) {
            onHoldReason = onHoldReasons.iterator().next();
        }
        PayrollServices.commitUnitOfWork();

        assertNotNull(ddCompServiceInfo);

        PayrollServices.beginUnitOfWork();
        foundCompany = Company.findCompany(company1Copy.getCompanyId(),
                SourceSystemCode.valueOf(company1Copy.getSourceSystemCd().toString()));

        ddCompServiceInfo = (DDCompanyServiceInfo) PayrollServices.entityFinder.findById(DDCompanyServiceInfo.class, ddCompServiceInfo.getId());

        SpcfMoney expectedAvgPayRunAmt = new SpcfMoney("150.00");
        SpcfMoney expectedHighPayRunAmt = new SpcfMoney("250.00");

        assertEquals("Average payroll run amount", expectedAvgPayRunAmt, ddCompServiceInfo.getAveragePayRunAmount());
        assertEquals("High annual run amount", expectedHighPayRunAmt, ddCompServiceInfo.getHighAnnualPayAmount());
        assertEquals("Offload group", OffloadGroup.Codes.STANDARD,
                foundCompany.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Consecutive limit violations", 0L, ddCompServiceInfo.getConsecutiveLimitViolationCount());


        AddCompanyDataLoader.assertCompaniesEqual(company1Copy, foundCompany);
        PayrollServices.commitUnitOfWork();
        Assert.assertNull("Comp Override", ddCompServiceInfo.getOverrideCompanyLimitAmount());
        Assert.assertNull("Emp Override", ddCompServiceInfo.getOverrideEmployeeLimitAmount());
        assertEquals("Service code", ServiceCode.DirectDeposit, ddCompServiceInfo.getService().getServiceCd());

        assertEquals("Number of on hold reasons", 1, numOnHolds);
        assertEquals("On hold reason", ServiceSubStatusCode.FraudReview, onHoldReason.getOnHoldReasonCd());
        assertTrue("Company is on hold", isCompanyOnHold);

        //assertEquals("Number of notes", 1, numNotes);
        //assertEquals("Company note", qbdtFraudNotes, noteContents);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                foundCompany,
                EventTypeCode.CompanyMatchesFraudulentCompany, CompanyEventStatus.Active, null, null);

        //Assertion for CompanyMatchesFraudulentCompany Event
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Event Details", qbdtFraudNotes,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.SignUp),
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addQBDTCloudThenDDCompanyCoreFailsFraudControls() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company = c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        ProcessResult pr = PayrollServices.companyManager.terminateService
                (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("terminate service", pr);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO company1Copy = c3dl.getCompany1();
        company1Copy.setCompanyId("48484848488");
        company1Copy.setFein("847656466");
        DDServiceInfoDTO service2 = AddCompanyDataLoader.dataloader.getTestCompanyService();
        ProcessResult<Company> result2 = PayrollServices.companyManager.addCompany(company1Copy);
        assertSuccess(result2);
        assertEquals(0, result2.getMessages().size());

        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(result2.getResult(), "123456", "654321");

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);

        //Try cloud first
        ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();
        serviceInfoDTO.setServiceCode(ServiceCode.Cloud);
        Company domainCompany2 = result2.getResult();
        ProcessResult<CompanyService> cloudResult = PayrollServices.companyManager.addService(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), serviceInfoDTO);

        assertTrue(cloudResult.isSuccess());
        assertEquals("Number of messages", 0, cloudResult.getMessages().size());        

        //Ensure we can still add the DD service
        domainCompany2 = result2.getResult();

        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), service2);
        assertTrue(ddServiceAddProcessResult2.isSuccess());

        CompanyBankAccountDTO cbaDTO = PayrollServices.dtoFactory.create(company.getCompanyBankAccountCollection().getFirst());
        ProcessResult addBankAccountResult = PayrollServices.companyManager.addCompanyBankAccount(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), cbaDTO, true, true);
        assertTrue(addBankAccountResult.isSuccess());

        assertEquals("Number of messages", 1, addBankAccountResult.getMessages().size());
        Message errorMessage = addBankAccountResult.getMessages().get(0);

        Assert.assertEquals("Message code", "1040", errorMessage.getMessageCode());
        Assert.assertEquals("Message text",
                "Company QBDT:48484848488 was added but could not be activated because it matches an existing company that is either on hold or terminated.",
                errorMessage.getMessage().trim());
        Assert.assertEquals("Message level", MessageInfo.MessageLevel.WARNING,
                errorMessage.getLevel());

        //Now try adding 401k service and making sure the fraud check does not run again
        domainCompany2 = result2.getResult();
        ThirdParty401kServiceInfoDTO tp401kCompanyService = new ThirdParty401kServiceInfoDTO();
        tp401kCompanyService.setCustodialId("12345666");
        tp401kCompanyService.setHasSafeHarbor(false);
        tp401kCompanyService.setServiceStartDate(PSPDate.getPSPTime());
        ProcessResult<CompanyService> tp401kServiceResult = PayrollServices.companyManager.addService(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), tp401kCompanyService);

        PayrollServices.commitUnitOfWork();
        assertSuccess(tp401kServiceResult);
        assertEquals("Number of messages", 0, tp401kServiceResult.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1Copy.getCompanyId(),
                SourceSystemCode.valueOf(company1Copy.getSourceSystemCd().toString()));
        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        int numNotes = notes.size();
        String noteContents = "";
        if (numNotes == 1) {
            Iterator<CompanyNote> itrNotes = notes.iterator();
            noteContents = itrNotes.next().getNotes();
        }
        boolean isCompanyOnHold = foundCompany.isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = foundCompany.getOnHoldReasonCollection();
        int numOnHolds = onHoldReasons.size();
        OnHoldReason onHoldReason = null;
        if (numOnHolds == 1) {
            onHoldReason = onHoldReasons.iterator().next();
        }
        PayrollServices.commitUnitOfWork();

        assertNotNull(ddCompServiceInfo);

        PayrollServices.beginUnitOfWork();
        foundCompany = Company.findCompany(company1Copy.getCompanyId(),
                SourceSystemCode.valueOf(company1Copy.getSourceSystemCd().toString()));

        ddCompServiceInfo = (DDCompanyServiceInfo) PayrollServices.entityFinder.findById(DDCompanyServiceInfo.class, ddCompServiceInfo.getId());

        SpcfMoney expectedAvgPayRunAmt = new SpcfMoney("150.00");
        SpcfMoney expectedHighPayRunAmt = new SpcfMoney("250.00");

        assertEquals("Average payroll run amount", expectedAvgPayRunAmt, ddCompServiceInfo.getAveragePayRunAmount());
        assertEquals("High annual run amount", expectedHighPayRunAmt, ddCompServiceInfo.getHighAnnualPayAmount());
        assertEquals("Offload group", OffloadGroup.Codes.STANDARD,
                foundCompany.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Consecutive limit violations", 0L, ddCompServiceInfo.getConsecutiveLimitViolationCount());


        AddCompanyDataLoader.assertCompaniesEqual(company1Copy, foundCompany);
        PayrollServices.commitUnitOfWork();
        Assert.assertNull("Comp Override", ddCompServiceInfo.getOverrideCompanyLimitAmount());
        Assert.assertNull("Emp Override", ddCompServiceInfo.getOverrideEmployeeLimitAmount());
        assertEquals("Service code", ServiceCode.DirectDeposit, ddCompServiceInfo.getService().getServiceCd());

        assertEquals("Number of on hold reasons", 1, numOnHolds);
        assertEquals("On hold reason", ServiceSubStatusCode.FraudReview, onHoldReason.getOnHoldReasonCd());
        assertTrue("Company is on hold", isCompanyOnHold);

        //assertEquals("Number of notes", 1, numNotes);
        //assertEquals("Company note", qbdtFraudNotes, noteContents);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                foundCompany,
                EventTypeCode.CompanyMatchesFraudulentCompany, CompanyEventStatus.Active, null, null);

        //Assertion for CompanyMatchesFraudulentCompany Event
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Event Details", qbdtFraudNotes,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.SignUp),
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addQBDTCompanyCoreDiffAgreeFailsFraudControls() {
        AddCompanyDataLoader.addQBDTCompanyCoreDiffAgreeFailsFraudControls(null);
    }

    @Test
    public void addCompanyCoreSuccess_WithQuickbooksInfo() {
        PayrollServices.beginUnitOfWork();
//        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        Company3Dataloader c3dl = new Company3Dataloader();
        CompanyDTO company1 = c3dl.getCompany1();
        QuickbooksInfoDTO quickbooksInfo = new QuickbooksInfoDTO();
        quickbooksInfo.setCoaFeeAccountName("CoaFeeAccount");
        quickbooksInfo.setCoaSalesTaxAccountName("CoaSalesTaxAccount");
        company1.setQuickBooksInfo(quickbooksInfo);
        OfferingInfoDTO offeringInfoDTO = new OfferingInfoDTO();
        offeringInfoDTO.setPayrollSubTypeCd(PayrollSubtypeCode.Standard);
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        AddCompanyDataLoader.assertCompaniesEqual(company1, foundCompany);
        // verify Quickbooks info
        assertNotNull(foundCompany.getQuickbooksInfo());
        assertEquals("COA Fee Account", foundCompany.getQuickbooksInfo().getCoaFeeAccountName(), "CoaFeeAccount");
        assertEquals("COA Sales Tax Account", foundCompany.getQuickbooksInfo().getCoaSalesTaxAccountName(), "CoaSalesTaxAccount");
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addCompanyCoreNullCompany() {
        CompanyDTO company1 = null;
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(1, result.getMessages().size());
        assertEquals("141", result.getMessages().get(0).getMessageCode());
        assertEquals("Company is not specified.", result.getMessages().get(0).getMessage());
    }

    @Test
    public void addCompanyCoreInvalidCompany() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        company1.setLegalName(null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(1, result.getMessages().size());
        assertEquals("5001", result.getMessages().get(0).getMessageCode());
        assertEquals("LegalName has invalid value", result.getMessages().get(0).getMessage());
        assertEquals(EntityName.Company, result.getMessages().get(0).getEntityName());
    }

    @Test
    public void addCompanyCore_EINInUse_DiffSrcSystems() {
        DataLoader loader = new DataLoader();

        PayrollServices.beginUnitOfWork();

        loader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBOE);
        Company companyQBOE = loader.persistTestIntuitCompany();
        assertEquals("first company's SourceSystemCode", SourceSystemCode.QBOE, companyQBOE.getSourceSystemCd());
        loader.persistTestCompanyService(companyQBOE);

        loader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        Company companyQBDT = loader.persistTestIntuitCompany();
        assertEquals("second company's SourceSystemCode", SourceSystemCode.QBDT, companyQBDT.getSourceSystemCd());
        loader.persistTestCompanyService(companyQBDT);

        PayrollServices.commitUnitOfWork();

        assertEquals("Companies have same EIN", companyQBOE.getFedTaxId(), companyQBDT.getFedTaxId());
    }

    @Test
    public void addCompanyCore_NoAccountSignatory() {
        CompanyDTO company = AddCompanyDataLoader.dataloader.getTestIntuitCompany_NoAccountSignatory();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company);
        PayrollServices.commitUnitOfWork();
        assertEquals(1, result.getMessages().size());
        assertEquals("Company " + company.getSourceSystemCd() + ":" + company
                .getCompanyId() + " must have at least one Contact who is an Account Signatory.",
                result.getMessages().get(0).getMessage());
    }

    @Ignore
    @Test
    //@Todo:Offerings move this to service
    public void addCompanyCoreMissingOffering() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        Assert.assertTrue(result.isSuccess());
        PayrollServices.commitUnitOfWork();
    }

    @Ignore
    @Test
    //@Todo:Offerings move this to service
    public void addCompanyCoreNoSuchOffering() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        OfferingInfoDTO dtoOfferingInfo = new OfferingInfoDTO();
        dtoOfferingInfo.setSKU("NO_SUCH_OFFERING_SKU");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        Assert.assertTrue(!result.isSuccess());
        Assert.assertTrue(result.getMessages().size() == 1);
        Assert.assertTrue(result.getMessages().get(0).getMessageCode().equals("5001")); // invalid value
    }

    @Test
    public void testFundingModelQBDTCompany() {
        PayrollServices.beginUnitOfWork();
        CompanyQB1DataLoader c1dl = new CompanyQB1DataLoader();
        Company company1 = c1dl.persistQBCompany1();
        SourcePayrollParameter defaultFundingModel = SourcePayrollParameter.findSourcePayrollParameter(company1.getSourceSystemCd(), SourcePayrollParameterCode.DefaultFundingModel);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company1 = Application.refresh(company1);
        FundingModel fundingModel = PayrollServices.entityFinder.findById(FundingModel.class, defaultFundingModel != null ? defaultFundingModel.getParameterValue() : FundingModel.Codes.FIVE_DAY);
        assertEquals("Funding Model:", fundingModel.getName(), company1.getFundingModel().getName());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test case to add a QBOE company with same EIN as QBDT company which is on Fraud Review.
     */
    @Test
    public void addQBOECompanyCoreFailsFraudControls_EINInUser_OnHoldCompany_DiffSrcSys() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        Company company = c1dl.persistCompany1();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        ProcessResult pr = PayrollServices.companyManager.terminateService
                (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("terminate service", pr);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO company1Copy = c1dl.getCompany1();
        company1Copy.setCompanyId("48484848488");
        company1Copy.setFein("847656466");
        company1Copy.setSourceSystemCd(SourceSystemCode.QBDT);
        DDServiceInfoDTO service2 = AddCompanyDataLoader.dataloader.getTestCompanyService();
        ProcessResult<Company> result2 = PayrollServices.companyManager.addCompany(company1Copy);
        assertSuccess(result2);
        assertEquals(0, result2.getMessages().size());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(result2.getResult(), "123456", "654321");

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        //Ensure we can still add the service
        Company domainCompany2 = result2.getResult();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), service2);
        assertSuccess(ddServiceAddProcessResult2);

        CompanyBankAccountDTO cbaDTO = PayrollServices.dtoFactory.create(company.getCompanyBankAccountCollection().getFirst());
        ProcessResult addBankAccountResult = PayrollServices.companyManager.addCompanyBankAccount(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), cbaDTO, true, true);
        assertTrue(addBankAccountResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of messages", 1, addBankAccountResult.getMessages().size());
        Message errorMessage = addBankAccountResult.getMessages().get(0);

        Assert.assertEquals("Message code", "1040", errorMessage.getMessageCode());
        Assert.assertEquals("Message text",
                "Company QBDT:48484848488 was added but could not be activated because it matches an existing company that is either on hold or terminated.",
                errorMessage.getMessage().trim());
        Assert.assertEquals("Message level", MessageInfo.MessageLevel.WARNING,
                errorMessage.getLevel());
                
        PayrollServices.beginUnitOfWork();
        Company2Dataloader c2dl = new Company2Dataloader();
        CompanyDTO company2Copy = c2dl.getCompany1();
        company2Copy.setFein("847656466");
        Collection<ContactDTO> contacts = company2Copy.getContacts();
        for(ContactDTO contact : contacts){
            contact.setEmail("abcd@yahoo.com");
        }
        ProcessResult<Company> result3 = PayrollServices.companyManager.addCompany(company2Copy);
        assertSuccess(result3);

        //Ensure we can still add the service
        Company domainCompany3 = result3.getResult();
        ProcessResult<CompanyService> ddServiceAddProcessResult3 = PayrollServices.companyManager.addService(domainCompany3.getSourceSystemCd(), domainCompany3.getSourceCompanyId(), service2);

        PayrollServices.commitUnitOfWork();
        assertSuccess(ddServiceAddProcessResult3);

        assertEquals("Number of messages", 1, ddServiceAddProcessResult3.getMessages().size());
        errorMessage = ddServiceAddProcessResult3.getMessages().get(0);
        assertEquals("Message code", "1040", errorMessage.getMessageCode());
        assertEquals("Message text",
                "Company QBOE:2222222 was added but could not be activated because it matches an existing company that is either on hold or terminated.",
                errorMessage.getMessage().trim());
        assertEquals("Message level", MessageInfo.MessageLevel.WARNING,
                errorMessage.getLevel());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany("2222222", SourceSystemCode.QBOE);        
        DomainEntitySet<OnHoldReason> onHoldReasons = foundCompany.getOnHoldReasonCollection();
        int numOnHolds = onHoldReasons.size();
        OnHoldReason onHoldReason = null;
        if (numOnHolds == 1) {
            onHoldReason = onHoldReasons.iterator().next();
        }
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of on hold reasons", 1, numOnHolds);
        assertEquals("On hold reason", ServiceSubStatusCode.FraudReview, onHoldReason.getOnHoldReasonCd());
        //assertTrue("Company is on hold", isCompanyOnHold);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                foundCompany,
                EventTypeCode.CompanyMatchesFraudulentCompany, CompanyEventStatus.Active, null, null);

        //Assertion for CompanyMatchesFraudulentCompany Event
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Event Details", qboeFraudNotesForSameEIN,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.SignUp),
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        PayrollServices.commitUnitOfWork();
    }    

    @Test
    public void addTaxCompanySuccess() {
        DataLoadServices.setupCompany("1234567");

        // persistence testing
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany("1234567", SourceSystemCode.QBDT);

        assertNotNull(company);

        CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);

        assertNotNull(taxService);

        assertEquals("Company Service Status", ServiceSubStatusCode.ActiveCurrent, taxService.getStatusCd());
        assertEquals("Company Agencies ", 2, company.getCompanyAgencyCollection().size());

        //Assertion for Company Agencies
        CompanyAgency irsAgency =
                CompanyAgency.findCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), Agency.IRS);

        assertNotNull("Company IRS Agency", irsAgency);

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addCompanyCorePrimaryPrincipalPhoneEmptyString() {
        testAddCompanyCoreEmptyString(ContactRole.PrimaryPrincipal);
    }

    @Test
    public void addCompanyCoreContactPhoneEmptyString() {
        testAddCompanyCoreEmptyString(ContactRole.PayrollAdmin);
        testAddCompanyCoreEmptyString(ContactRole.SecondaryPrincipal);
        testAddCompanyCoreEmptyString(ContactRole.Other);
    }

    private void testAddCompanyCoreEmptyString(ContactRole role) {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        for (ContactDTO contactDTO : company1.getContacts()) {
            if (contactDTO.getContactRoleCd().equals(role)) {
                contactDTO.setPhoneNumber("");
            }
        }
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();

        assertEquals(1, result.getMessages().size());
        Message msg = result.getMessages().get(0);
        assertEquals("5001", msg.getMessageCode());
        assertEquals("Phone has invalid value", msg.getMessage());
    }

    @Test
    public void testNameControlValue(){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        assertNotNull(company.getNameControl());

        //Length should be 4 or less
        String nameControl = company.getNameControl() + "4";
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId("New123456");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> companyProcessResult = PayrollServices.companyManager.addCompany(companyDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals(1, companyProcessResult.getMessages().size());
        Message msg = companyProcessResult.getMessages().get(0);
        assertEquals("191", msg.getMessageCode());
        assertEquals("NameControl "+nameControl+" for company New123456 is not valid.", msg.getMessage());

        //Whitespaces in the Name Control
        nameControl = company.getNameControl().replace("E"," ");
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId("New123456");
        PayrollServices.beginUnitOfWork();
        companyProcessResult = PayrollServices.companyManager.addCompany(companyDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals(1, companyProcessResult.getMessages().size());
        msg = companyProcessResult.getMessages().get(0);
        assertEquals("191", msg.getMessageCode());
        assertEquals("NameControl "+nameControl+" for company New123456 is not valid.", msg.getMessage());

        //Special characters in the Name Control - only - and & are allowed
        nameControl = company.getNameControl().replace("E","!");
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId("New123456");
        PayrollServices.beginUnitOfWork();
        companyProcessResult = PayrollServices.companyManager.addCompany(companyDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals(1, companyProcessResult.getMessages().size());
        msg = companyProcessResult.getMessages().get(0);
        assertEquals("191", msg.getMessageCode());
        assertEquals("NameControl "+nameControl+" for company New123456 is not valid.", msg.getMessage());

        //Valid NameControl values
        //Length less than 4 is valid
        nameControl = company.getNameControl().substring(2);
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId("NewValidCo");
        PayrollServices.beginUnitOfWork();
        companyProcessResult = PayrollServices.companyManager.addCompany(companyDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(companyProcessResult);
        Company company1 = Company.findCompany("NewValidCo", SourceSystemCode.QBDT);

        //Special Characters - &
        nameControl = company.getNameControl().replace("S","&");
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId("NewSplChr");
        PayrollServices.beginUnitOfWork();
        companyProcessResult = PayrollServices.companyManager.addCompany(companyDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(companyProcessResult);
        company1 = Company.findCompany("NewSplChr", SourceSystemCode.QBDT);

        nameControl = company.getNameControl().replace("S","-");
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setNameControl(nameControl);
        companyDTO.setCompanyId("NewSplChr2");
        PayrollServices.beginUnitOfWork();
        companyProcessResult = PayrollServices.companyManager.addCompany(companyDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(companyProcessResult);
        company1 = Company.findCompany("NewSplChr2", SourceSystemCode.QBDT);
    }

    @Test
    public void addCompanyCoreWithIndustryTypeAndOwnershipType() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        CompanyAdditionalInfoDTO companyAdditionalInfoDTO = new CompanyAdditionalInfoDTO();
        companyAdditionalInfoDTO.setIndustry("Accounting, Auditing, and Bookkeeping Services");
        companyAdditionalInfoDTO.setOwnership("Private Limited Company");
        company1.setCompanyAdditionalInfo(companyAdditionalInfoDTO);
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        AddCompanyDataLoader.assertCompaniesEqual(company1, foundCompany);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addCompanyCoreWithoutIndustryType() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        AddCompanyDataLoader.assertCompaniesEqual(company1, foundCompany);
        PayrollServices.commitUnitOfWork();
    }

    private void assertActiveCompanyExistsForRealm(ProcessResult<Company> prAddCompany) {
        assertEquals(1, prAddCompany.getMessages().size());
        Message message = prAddCompany.getMessages().get(0);
        assertEquals(message.getMessageCode(), "10124");
        assertEquals("Active company found for realm 9123456890", message.getMessage());
    }

    private void assertDuplicateCompanyExistsForRealm(ProcessResult<Company> prAddCompany) {
        assertEquals(1, prAddCompany.getMessages().size());
        Message message = prAddCompany.getMessages().get(0);
        assertEquals(message.getMessageCode(), "10123");
        assertEquals("Duplicate active companies found for Realm 9123456890", message.getMessage());
    }
}
