/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes.dataloaders.coretests;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.common.ProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;

/**
 */
public class AddCompanyDataLoader {

    private static String qbdtFraudNotesNonMatchedAgree = "This company was not activated because the company bank account matches the company bank account of company QB Desktop 3 (Source System=QBDT Source ID=8574536) with status of Terminated.";

    public static DataLoader dataloader = new DataLoader();

    public static void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void loadBeforeCreatingMultipleCompanies(){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void addQBDTCompanyCoreDiffAgreeFailsFraudControls(String routingNumber) {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader(routingNumber);
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
        ProcessResult<Company> result2 = DataLoader.addCompany(company1Copy);
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

        junit.framework.Assert.assertEquals("Number of messages", 1, addBankAccountResult.getMessages().size());
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
        Collection<OnHoldReason> onHoldReasons = foundCompany.getCurrentOnHoldReasons();
        int numOnHolds = onHoldReasons.size();
        OnHoldReason onHoldReason = null;
        if (numOnHolds == 1) {
            onHoldReason = onHoldReasons.iterator().next();
        }
        PayrollServices.commitUnitOfWork();

        assertNotNull(ddCompServiceInfo);

        PayrollServices.beginUnitOfWork();
        ddCompServiceInfo = (DDCompanyServiceInfo) PayrollServices.entityFinder.findById(DDCompanyServiceInfo.class, ddCompServiceInfo.getId());
        
        SpcfMoney expectedAvgPayRunAmt = new SpcfMoney("150.00");
        SpcfMoney expectedHighPayRunAmt = new SpcfMoney("250.00");

        foundCompany = Company.findCompany(company1Copy.getCompanyId(),
                SourceSystemCode.valueOf(company1Copy.getSourceSystemCd().toString()));

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
        //assertEquals("Number of notes", 1, numNotes);
        //assertEquals("Company note", qbdtFraudNotesNonMatchedAgree, noteContents);

        assertEquals("Number of on hold reasons", 1, numOnHolds);
        assertEquals("On hold reason", ServiceSubStatusCode.FraudReview, onHoldReason.getOnHoldReasonCd());
        assertTrue("Company is on hold", isCompanyOnHold);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                foundCompany,
                EventTypeCode.CompanyMatchesFraudulentCompany, CompanyEventStatus.Active, null, null);

        //Assertion for CompanyMatchesFraudulentCompany Event
        junit.framework.Assert.assertEquals("Company Events", 1, companyEventsList.size());
        junit.framework.Assert.assertEquals("Event Details", qbdtFraudNotesNonMatchedAgree,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));

        PayrollServices.commitUnitOfWork();        
    }

    static public void assertCompaniesEqual(CompanyDTO pDTOCompany, Company pDomainCompany) {
        SourcePayrollParameter defaultFundingModel = SourcePayrollParameter.findSourcePayrollParameter(pDomainCompany.getSourceSystemCd(), SourcePayrollParameterCode.DefaultFundingModel);
        FundingModel expectedFundingModel = PayrollServices.entityFinder.findById(FundingModel.class, defaultFundingModel != null ? defaultFundingModel.getParameterValue() : FundingModel.Codes.FIVE_DAY);

        assertEquals("Source company ID", pDTOCompany.getCompanyId(), pDomainCompany.getSourceCompanyId());
        assertEquals("Source system", pDTOCompany.getSourceSystemCd().toString(),
                pDomainCompany.getSourceSystemCd().toString());
        assertEquals("Company PSID", pDTOCompany.getCompanyId(), pDomainCompany.getSourceCompanyId());

        assertEquals("DBA name", pDTOCompany.getDBA(), pDomainCompany.getDbaName());
        assertEquals("FEIN", pDTOCompany.getFein(), pDomainCompany.getFedTaxId());
        assertEquals("Funding model", expectedFundingModel, pDomainCompany.getFundingModel());
        assertEquals("Legal name", pDTOCompany.getLegalName(), pDomainCompany.getLegalName());
        assertAddressesEqual(pDTOCompany.getLegalAddress(), pDomainCompany.getLegalAddress());
        assertAddressesEqual(pDTOCompany.getMailingAddress(), pDomainCompany.getMailingAddress());
        assertEquals("Notification email", pDTOCompany.getNotificationEmail(), pDomainCompany.getNotificationEmail());
        if (pDTOCompany.getTaxExemptExpirationDate() != null) {
            assertTrue("TaxExemptExpirationDate", CalendarUtils.getDifferenceInDays(DateDTO.convertToSpcfCalendar(pDTOCompany.getTaxExemptExpirationDate()),pDomainCompany.getTaxExemptExpirationDate())==0);
        } else {
            assertNull("TaxExemptExpirationDate", pDomainCompany.getTaxExemptExpirationDate());
        }

        //Persistence test QB Info, if set
        if (pDTOCompany.getQuickBooksInfo() != null) {
            assertNotNull(pDomainCompany.getQuickbooksInfo());
            assertEquals("Application version", pDTOCompany.getQuickBooksInfo().getApplicationVersion(), pDomainCompany.getQuickbooksInfo().getApplicationVersion());
            assertEquals("Application version", pDTOCompany.getQuickBooksInfo().getQuickbooksSku(), pDomainCompany.getQuickbooksInfo().getQuickbooksSku());
            assertEquals("Application id", pDTOCompany.getQuickBooksInfo().getApplicationId(), pDomainCompany.getQuickbooksInfo().getApplicationId());
            assertEquals("License Number", pDTOCompany.getQuickBooksInfo().getLicenseNumber(), pDomainCompany.getQuickbooksInfo().getLicenseNumber());
            assertEquals("Tax table ID", pDTOCompany.getQuickBooksInfo().getTaxTableId(), pDomainCompany.getQuickbooksInfo().getTaxTableId());
        } else {
            if (pDomainCompany.getQuickbooksInfo() != null) {
                assertNull("Application version", pDomainCompany.getQuickbooksInfo().getApplicationVersion());
                assertNull("Application id", pDomainCompany.getQuickbooksInfo().getApplicationId());
                assertNull("License Number", pDomainCompany.getQuickbooksInfo().getApplicationVersion());
                assertNull("Tax table ID", pDomainCompany.getQuickbooksInfo().getTaxTableId());
            }
        }

        if(pDTOCompany.getCompanyAdditionalInfo() != null){
            assertNotNull(pDomainCompany.getCompanyAdditionalInfo());
            assertEquals("Industry Type", pDTOCompany.getCompanyAdditionalInfo().getIndustry(), pDomainCompany.getCompanyAdditionalInfo().getIndustryType().getIndustry());
            assertEquals("Ownership Type", pDTOCompany.getCompanyAdditionalInfo().getOwnership(), pDomainCompany.getCompanyAdditionalInfo().getOwnershipType().getOwnership());

        }

        if (pDTOCompany.getPayrollFrequencyCd() != null) {
            assertNotNull(pDomainCompany.getPayrollFrequency());
            assertEquals("Payroll Frequency",
                    ProcessesToDTO.getDomainPayrollFrequency(pDTOCompany.getPayrollFrequencyCd()).getPayrollFreqCd(),
                    pDomainCompany.getPayrollFrequency().getPayrollFreqCd());
        }
        assertEquals("Phone", null, pDomainCompany.getPhone());
        assertEquals("Number of contacts", pDTOCompany.getContacts().size(), pDomainCompany.getContactCollection().size());

        ArrayList<Contact> modifiableCopyToContacts = new ArrayList<Contact>();

        for (Contact contact : pDomainCompany.getContactCollection()) {
            modifiableCopyToContacts.add(contact);
        }

        for (ContactDTO currDTOContact : pDTOCompany.getContacts()) {
            boolean thisContactMatchesADomainContact = false;
            for (Contact currDomainContact : modifiableCopyToContacts) {
                boolean theseContactsEqual = testContactsEqual(currDTOContact, currDomainContact, pDomainCompany);
                if (theseContactsEqual) {
                    thisContactMatchesADomainContact = true;
                    modifiableCopyToContacts.remove(currDomainContact);
                    break;
                }
            }
            if (!thisContactMatchesADomainContact) {
                TestCase.fail("Contact " + currDTOContact + " does not have a corresponding matching domain contact");
            }
        }

        assertEquals("IS Flagged For Fraud", false, pDomainCompany.getIsFlaggedForFraud());
        if (pDTOCompany.getCurrentToken()!= null) {
            assertEquals("Current Token", pDTOCompany.getCurrentToken().longValue(), pDomainCompany.getCurrentToken());
        }
    }

    static private void assertAddressesEqual(AddressDTO pDTOAddress, Address pDomainAddress) {
        assertEquals("Address line 1", pDTOAddress.getAddressLine1(), pDomainAddress.getAddressLine1());
        assertEquals("Address line 2", pDTOAddress.getAddressLine2(), pDomainAddress.getAddressLine2());
        assertEquals("Address line 3", pDTOAddress.getAddressLine3(), pDomainAddress.getAddressLine3());
        assertEquals("City", pDTOAddress.getCity(), pDomainAddress.getCity());
        assertEquals("State", pDTOAddress.getState(), pDomainAddress.getState());
        assertEquals("Zip code", pDTOAddress.getZipCode(), pDomainAddress.getZipCode());
        assertEquals("Zip code extension", pDTOAddress.getZipCodeExtension(), pDomainAddress.getZipCodeExtension());
        assertEquals("Country", pDTOAddress.getCountry(), pDomainAddress.getCountry());
    }

    static private boolean testAddressesEqual(AddressDTO pDTOAddress, Address pDomainAddress) {
        boolean bAddressLine1Equal = pDTOAddress.getAddressLine1().equals(pDomainAddress.getAddressLine1());
        boolean bAddressLine2Equal = nullableStringsEqual(pDTOAddress.getAddressLine2(), pDomainAddress.getAddressLine2());
        boolean bAddressLine3Equal = nullableStringsEqual(pDTOAddress.getAddressLine3(), pDomainAddress.getAddressLine3());
        boolean bZipExtensionsEqual = nullableStringsEqual(pDTOAddress.getZipCodeExtension(), pDomainAddress.getZipCodeExtension());
        boolean bCountriesEqual = nullableStringsEqual(pDTOAddress.getCountry(), pDomainAddress.getCountry());
        boolean bCitiesEqual = pDTOAddress.getCity().equals(pDomainAddress.getCity());
        boolean bStatesEqual = pDTOAddress.getState().equals(pDomainAddress.getState());
        boolean bZipCodesEqual = pDTOAddress.getZipCode().equals(pDomainAddress.getZipCode());

        if (bAddressLine1Equal && bAddressLine2Equal && bAddressLine3Equal && bCitiesEqual && bStatesEqual && bZipCodesEqual && bZipExtensionsEqual && bCountriesEqual) {
            return true;
        } else {
            return false;
        }
    }

    static private boolean testContactsEqual(ContactDTO pDTOContact, Contact pDomainContact, Company pDomainCompany) {
        boolean bAuthSignersEqual = pDTOContact.getAccountSignatory().equals(pDomainContact.getAuthSignerYnInd());
        boolean bCompaniesEqual = pDomainCompany.equals(pDomainContact.getCompany());
        boolean bContactRolesEqual = pDTOContact.getContactRoleCd().toString().equals(pDomainContact.getContactRoleCd().toString());
        boolean bEmailsEqual = pDTOContact.getEmail().equals(pDomainContact.getEmail());
        boolean bFirstNamesEqual = pDTOContact.getFirstName().equals(pDomainContact.getFirstName());
        boolean bMiddleNamesEqual = nullableStringsEqual(pDTOContact.getMiddleName(), pDomainContact.getMiddleName());
        boolean bLastNamesEqual = pDTOContact.getLastName().equals(pDomainContact.getLastName());
        boolean bPhonesEqual = pDTOContact.getPhoneNumber().equals(pDomainContact.getPhone());
        boolean bSourceContactIdsEqual = pDTOContact.getContactId().equals(pDomainContact.getSourceContactId());
        boolean bTitlesEqual = nullableStringsEqual(pDTOContact.getTitle(), pDomainContact.getTitle());
        boolean bTitleSuffixesEqual = nullableStringsEqual(pDTOContact.getTitleSuffix(), pDomainContact.getSuffix());
        boolean bFaxesEqual = nullableStringsEqual(pDTOContact.getFaxNumber(), pDomainContact.getFax());
        boolean bJobTitlesEqual = nullableStringsEqual(pDTOContact.getJobTitle(), pDomainContact.getJobTitle());
        boolean bSecondPhonesEqual = nullableStringsEqual(pDTOContact.getSecondPhoneNumber(), pDomainContact.getSecondPhone());

        boolean bAddressesEqual = testAddressesEqual(pDTOContact.getAddress(), pDomainContact.getMailingAddress());

        if (bPhonesEqual && bLastNamesEqual && bMiddleNamesEqual && bFirstNamesEqual && bEmailsEqual
                && bContactRolesEqual && bCompaniesEqual && bAuthSignersEqual && bSourceContactIdsEqual
                && bAddressesEqual && bTitlesEqual && bTitleSuffixesEqual && bFaxesEqual && bJobTitlesEqual && bSecondPhonesEqual) {
            return true;
        } else {
            return false;
        }
    }

     private static boolean nullableStringsEqual(String pString1, String pString2) {
        boolean equals = true;
        if (pString1 == null || pString2 == null) {
            if (pString1 != null || pString2 != null) {
                return false;
            }
        } else {
            equals = pString1.equals(pString2);
        }
        return equals;
    }

    public static void loadAddQBDTCompanyCoreDiffAgreeFailsFraudControls() {
        beforeEachTest();
        addQBDTCompanyCoreDiffAgreeFailsFraudControls(null);
    }

    public static void loadAddQBDTCompanyCoreDiffAgreeFailsFraudControls_OnlyDateChangeSetup() {
       String routingNumber = "111000025";
       loadBeforeCreatingMultipleCompanies();
       addQBDTCompanyCoreDiffAgreeFailsFraudControls(routingNumber);
    }
}
