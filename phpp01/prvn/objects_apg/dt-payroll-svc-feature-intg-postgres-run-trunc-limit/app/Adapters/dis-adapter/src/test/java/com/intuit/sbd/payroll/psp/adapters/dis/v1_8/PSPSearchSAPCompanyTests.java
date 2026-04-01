package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.*;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.SearchSAPCompanyRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.SearchSAPCompanyResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEntitlementInfo;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEntitlementSearchResult;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntityChangeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPSearchSAPCompanyTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPSearchSAPCompanyTests {
    private String psid = null;
    private String entitlementLicenseNumber = "lic_123456789";
    private String entitlementEoc = "eoc_123456789";

    @Before
    public void loadDataHappyPath() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        psid = "123456789";
        DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"));

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<Company> companies = Application.find(Company.class,
                new Query<Company>().Where(Company.SourceSystemCd()
                        .equalTo(SourceSystemCode.QBDT)));
        Company company = companies.get(0);
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.addAssistedEntitlementUnit(company, entitlementLicenseNumber, entitlementEoc, true);

    }

    @Test
    public void testCompanyWhenDgDisassociated() {
        Company dgDisassociatedCompany = null;
        String ein = "";
        try {
            PayrollServices.beginUnitOfWork();
            dgDisassociatedCompany = Company.findCompany(psid, SourceSystemCode.QBDT);
            ein = dgDisassociatedCompany.getFedTaxId();
            dgDisassociatedCompany.setIsDgDisassociated(Boolean.TRUE);
        } finally {
            PayrollServices.commitUnitOfWork();
        }

        try {
            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setEin(ein);
            searchSAPCompanyRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            Assert.assertEquals(0, searchSAPCompanyResponseDISDTO.getCompanies().size());
            TestHelper.verifySuccess(searchSAPCompanyResponseDISDTO.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

//    @Test
//    public void emptyTest() {
//    }

    @Test
    public void testCompanyHappyPathEIN() {
        String ein = "";
        try {
            PayrollServices.beginUnitOfWork();
            Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
            ein = company1.getFedTaxId();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        try {
            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setEin(ein);
            searchSAPCompanyRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            Assert.assertEquals(1, searchSAPCompanyResponseDISDTO.getCompanies().size());
            TestHelper.verifySuccess(searchSAPCompanyResponseDISDTO.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testCompanySearchAllFields() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setSourceCompanyId(psid);
            searchSAPCompanyRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            Assert.assertEquals(1, searchSAPCompanyResponseDISDTO.getCompanies().size());
            SAPCompanyDISDTO co = searchSAPCompanyResponseDISDTO.getCompanies().get(0);

            PayrollServices.beginUnitOfWork();
            Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);

            Assert.assertEquals(company1.getSourceCompanyId(), co.getCompanyId());
            Assert.assertEquals(company1.getDbaName(), co.getDBA());

            SAPAgreementInfoDISDTO sapAgreementInfoDISDTO = co.getAgreementInfo();
//            assertEquals(company1.geta(),co.getAgreementInfo());
//            assertEquals(company1.xxx(),co.getBankReturnTransactionCount());
//            assertEquals(company1.xxx(),co.getContacts());
            Assert.assertEquals(company1.getFedTaxId(), co.getFein());
            Assert.assertEquals(company1.getFundingModel().getFundingModelCd(), co.getFundingModelCd());
//            assertEquals(company1.xxx(),co.getLastTransactionResponseToken());

            Assert.assertEquals(company1.getLegalName(), co.getLegalName());
            Address mailingAfddress = company1.getMailingAddress();
            Assert.assertEquals(mailingAfddress.getAddressLine1(), co.getMailingAddress().getAddressLine1());
            Assert.assertEquals(mailingAfddress.getAddressLine2(), co.getMailingAddress().getAddressLine2());
            Assert.assertEquals(mailingAfddress.getAddressLine3(), co.getMailingAddress().getAddressLine3());
            Assert.assertEquals(mailingAfddress.getCity(), co.getMailingAddress().getCity());
            Assert.assertEquals(mailingAfddress.getState(), co.getMailingAddress().getState());
            Assert.assertEquals(mailingAfddress.getZipCode(), co.getMailingAddress().getZipCode());
            Assert.assertEquals(mailingAfddress.getZipCodeExtension(), co.getMailingAddress().getZipExtension());

            Address legalAddress = company1.getLegalAddress();
            Assert.assertEquals(legalAddress.getAddressLine1(), co.getLegalAddress().getAddressLine1());
            Assert.assertEquals(legalAddress.getAddressLine2(), co.getLegalAddress().getAddressLine2());
            Assert.assertEquals(legalAddress.getAddressLine3(), co.getLegalAddress().getAddressLine3());
            Assert.assertEquals(legalAddress.getCity(), co.getLegalAddress().getCity());
            Assert.assertEquals(legalAddress.getState(), co.getLegalAddress().getState());
            Assert.assertEquals(legalAddress.getZipCode(), co.getLegalAddress().getZipCode());
            Assert.assertEquals(legalAddress.getZipCodeExtension(), co.getLegalAddress().getZipExtension());

//            Assert.assertEquals(company1.getMigrationStatus().toString(), co.getMigrationStatus().toString());
            Assert.assertEquals(company1.getNextPaycheckId(), co.getNextPaycheckId());
//            Assert.assertEquals(company1.getNextPaylineTransactionId(), co.getNextPayrollTransactionId());
            Assert.assertEquals(company1.getNotificationEmail(), co.getNotificationEmail());
           // Assert.assertEquals(company1.getOffloadGroup().getOffloadGroupCd(), co.getOffloadGrp().toString()); //todo remove??
            Assert.assertEquals(company1.getPayrollFrequency(), co.getPayrollFrequencyCd());
            Assert.assertEquals(company1.getPayrollCount(), co.getPayrollRunCount());
            Assert.assertEquals(company1.getSourceCompanyId(), co.getPSID());
            SAPQuickbooksInfoDISDTO sapQuickbooksInfoDISDTO = co.getQuickbooksInfo();
            Assert.assertEquals(company1.getQuickbooksInfo().getApplicationVersion(), sapQuickbooksInfoDISDTO.getApplicationVersion());
            Assert.assertEquals(company1.getQuickbooksInfo().getCoaFeeAccountName(), sapQuickbooksInfoDISDTO.getCoaFeeAccountName());
            Assert.assertEquals(company1.getQuickbooksInfo().getTaxTableId(), sapQuickbooksInfoDISDTO.getTaxTable());
            Assert.assertEquals(company1.getQuickbooksInfo().getCoaSalesTaxAccountName(), sapQuickbooksInfoDISDTO.getCoaSalesTaxAccountName());
            Assert.assertEquals(company1.getQuickbooksInfo().getLicenseNumber(), sapQuickbooksInfoDISDTO.getLicenseNumber());

            Assert.assertEquals(company1.getSourceSystemCd().toString(), co.getSourceSystemEnum().toString());
            Assert.assertEquals(company1.getTaxExemptExpirationDate(), co.getTaxExemptExpirationDate());

            DomainEntitySet<CompanyEvent> strikeEventsList = CompanyEvent.findCompanyEvents(company1,
                    EventTypeCode.Strike, CompanyEventStatus.Active, null, null);

            Assert.assertEquals(strikeEventsList.size(), co.getStrikeCount());


            com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(entitlementLicenseNumber, entitlementEoc);
            Assert.assertEquals(domainEntitlement.getCustomerId(), co.getAssistedCustomerAccountNumber());

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @Test
    public void testCompanySearchStrikes() {
        try {

            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 23, SpcfTimeZone.getLocalTimeZone()));
            ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(), "Strike Reason",
                    SpcfCalendar.createInstance(2007, 8, 23, SpcfTimeZone.getLocalTimeZone()));

            PayrollServices.commitUnitOfWork();

            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setSourceCompanyId(psid);
            searchSAPCompanyRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            Assert.assertEquals(1, searchSAPCompanyResponseDISDTO.getCompanies().size());
            SAPCompanyDISDTO co = searchSAPCompanyResponseDISDTO.getCompanies().get(0);

            PayrollServices.beginUnitOfWork();
            Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);

            DomainEntitySet<CompanyEvent> strikeEventsList = CompanyEvent.findCompanyEvents(company1,
                    EventTypeCode.Strike, CompanyEventStatus.Active, null, null);

            Assert.assertEquals(strikeEventsList.size(), co.getStrikeCount());

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testNoCompaniesFound() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setEin("123");
            searchSAPCompanyRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            Assert.assertEquals(0, searchSAPCompanyResponseDISDTO.getCompanies().size());
            TestHelper.verifySuccess(searchSAPCompanyResponseDISDTO.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testMigratedCompanyWithDDAndAssistedPayrolls() {
        psid = DISCompanyDataloader.setupMigratedCompanyWithDDPayrollAndAssistedPayroll().getSourceCompanyId();
        try {
            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setSourceCompanyId(psid);
            searchSAPCompanyRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            TestHelper.verifySuccess(searchSAPCompanyResponseDISDTO.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testAssistedAdvCompany() {
        String psid = "123456788";

        String licenseNumber = "12345678901234567890";
        String entitlementOfferingCode = "09876543210987654321";

        DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setPriceType("Standard");
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        EntitlementUnit entitlementUnit = DataLoadServices.addAssistedAdvantageEntitlementUnit(company, licenseNumber, entitlementOfferingCode);
        String can = entitlementUnit.getEntitlement().getCustomerId();

        DataLoadServices.addTaxService(company);

        DISAdapter disAdapter = new DISAdapter();
        SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
        searchSAPCompanyRequestDISDTO.setSourceCompanyId(psid);
        searchSAPCompanyRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

        SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
        TestHelper.verifySuccess(searchSAPCompanyResponseDISDTO.getDisResponse());
        SAPCompanyDISDTO co = searchSAPCompanyResponseDISDTO.getCompanies().get(0);
        Assert.assertEquals(can, co.getAssistedCustomerAccountNumber());
    }

    @Test
    // JPC 11/5/2012 - Fixing DE1042 DAS - CAN Number - agent reporting incorrect CAN pulled for the company
    public void testDifferentCANForSameEIN() {
        try {
            String company1CAN = null;
            String company2CAN = null;
            String ein = "";
            Company company1 = null;
            try {
                PayrollServices.beginUnitOfWork();
                company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
                ein = company1.getFedTaxId();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            DataLoadServices.cancelService(company1,ServiceCode.Tax);
            DataLoadServices.cancelService(company1,ServiceCode.DirectDeposit);
            Company company2 = DISCompanyDataloader.setupCompany(company1.getFedTaxId(),"111222333");
            DataLoadServices.addAssistedEntitlementUnit(company2, entitlementLicenseNumber, entitlementEoc, true);

            company1CAN = getCompanyAssistedCAN(company1);
            company2CAN = getCompanyAssistedCAN(company2);
            assertNotSame(company1CAN, company2CAN);

            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setEin(ein);
            searchSAPCompanyRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            Assert.assertEquals(2, searchSAPCompanyResponseDISDTO.getCompanies().size());
            TestHelper.verifySuccess(searchSAPCompanyResponseDISDTO.getDisResponse());
            SAPCompanyDISDTO responseCo1 = searchSAPCompanyResponseDISDTO.getCompanies().get(0);
            SAPCompanyDISDTO responseCo2 = searchSAPCompanyResponseDISDTO.getCompanies().get(1);

            boolean testResponseCo1CAN = false;
            if (responseCo1.getPSID().equals(company1.getSourceCompanyId())) {
                assertEquals(company1CAN,responseCo1.getAssistedCustomerAccountNumber());
                testResponseCo1CAN=true;
            }

            if (responseCo1.getPSID().equals(company2.getSourceCompanyId())) {
                assertEquals(company2CAN,responseCo1.getAssistedCustomerAccountNumber());
                testResponseCo1CAN=true;
            }
            Assert.assertTrue(testResponseCo1CAN);

            boolean testResponseCo2CAN = false;
            if (responseCo2.getPSID().equals(company1.getSourceCompanyId())) {
                assertEquals(company1CAN,responseCo2.getAssistedCustomerAccountNumber());
                testResponseCo2CAN=true;
            }
            if (responseCo2.getPSID().equals(company2.getSourceCompanyId())) {
                assertEquals(company2CAN,responseCo2.getAssistedCustomerAccountNumber());
                testResponseCo2CAN=true;
            }
            Assert.assertTrue(testResponseCo2CAN);

        } catch (Throwable t) {
            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(t.getMessage());
        }
    }

    @Test
    public void testEntityChangeHistory() {
        Company company = DISCompanyDataloader.setupCompany();

        PayrollServices.beginUnitOfWork();

        String originalEIN = company.getFedTaxId();
        String firstEINChangedTo = "111222333";

        int firstEINChangeMonth = 1;
        int firstEINChangeDay = 1;
        int firstEINChangeYear = 2011;
        PSPDate.setPSPTime(SpcfCalendar.createInstance(firstEINChangeYear, firstEINChangeMonth, firstEINChangeDay, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        changeEIN(company,firstEINChangedTo,firstEINChangeDay,firstEINChangeMonth,firstEINChangeYear);

        try {
            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setEin(firstEINChangedTo);
            searchSAPCompanyRequestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            Assert.assertEquals(1, searchSAPCompanyResponseDISDTO.getCompanies().size());
            TestHelper.verifySuccess(searchSAPCompanyResponseDISDTO.getDisResponse());
            SAPCompanyDISDTO companyDISDTO = searchSAPCompanyResponseDISDTO.getCompanies().get(0);

            assertEquals(1,companyDISDTO.getEntityChange().size());

            EntityChangeDISDTO entityChange1 = companyDISDTO.getEntityChange().get(0);

            assertEquals(firstEINChangedTo,entityChange1.getNewEin());
            assertEquals(originalEIN, entityChange1.getOldEin());
            assertEquals(firstEINChangeDay, entityChange1.getEffectiveDate().get(Calendar.DAY_OF_MONTH));
            // Gregorian calendar returns zero based month
            assertEquals(firstEINChangeMonth, entityChange1.getEffectiveDate().get(Calendar.MONTH) + 1);
            assertEquals(firstEINChangeYear, entityChange1.getEffectiveDate().get(Calendar.YEAR));

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEntityChangeOldEinReturned() {
        Company originalCompany = DISCompanyDataloader.setupCompany();
        String originalEIN = originalCompany.getFedTaxId();

        DataLoadServices.cancelService(originalCompany, ServiceCode.Tax);
        DataLoadServices.cancelService(originalCompany,ServiceCode.DirectDeposit);

        Company newCompany = DISCompanyDataloader.setupCompany(originalEIN,"PSID123");

        PayrollServices.beginUnitOfWork();

        String firstEINChangedTo = "111222333";

        int firstEINChangeMonth = 1;
        int firstEINChangeDay = 1;
        int firstEINChangeYear = 2011;
        PSPDate.setPSPTime(SpcfCalendar.createInstance(firstEINChangeYear, firstEINChangeMonth, firstEINChangeDay, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        changeEIN(newCompany,firstEINChangedTo,firstEINChangeDay,firstEINChangeMonth,firstEINChangeYear);

        try {
            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setEin(originalEIN);
            searchSAPCompanyRequestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            Assert.assertEquals(2, searchSAPCompanyResponseDISDTO.getCompanies().size());
            TestHelper.verifySuccess(searchSAPCompanyResponseDISDTO.getDisResponse());

            SAPCompanyDISDTO company1DISDTO = searchSAPCompanyResponseDISDTO.getCompanies().get(0);
            String company1DISDTOEIN = company1DISDTO.getFein();

            SAPCompanyDISDTO company2DISDTO = searchSAPCompanyResponseDISDTO.getCompanies().get(1);
            String company2DISDTOEIN = company2DISDTO.getFein();

            assertTrue(company1DISDTOEIN.equals(originalEIN) && company2DISDTOEIN.equals(firstEINChangedTo)
                    || company2DISDTOEIN.equals(originalEIN) && company1DISDTOEIN.equals(firstEINChangedTo));

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testStartAndEndDateForCancelledCompany() {
        Company company = DISCompanyDataloader.setupCompany();
        SpcfCalendar companySetupSC = PSPDate.getPSPTime();
        CalendarUtils.clearTime(companySetupSC);
        Date expectedServiceStartDate = CalendarUtils.convertToDate(companySetupSC);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar oneYearOutSpcfCalendar = companySetupSC.copy();
        oneYearOutSpcfCalendar.addYears(1);
        CalendarUtils.clearTime(oneYearOutSpcfCalendar);
        PSPDate.setPSPTime(oneYearOutSpcfCalendar);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.cancelService(company, ServiceCode.Tax);
        Date expectedTaxEndServiceDate = CalendarUtils.convertToDate(oneYearOutSpcfCalendar);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar twoYearsOutSpcfCalendar = companySetupSC.copy();
        twoYearsOutSpcfCalendar.addYears(2);
        CalendarUtils.clearTime(twoYearsOutSpcfCalendar);
        PSPDate.setPSPTime(twoYearsOutSpcfCalendar);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.cancelService(company, ServiceCode.DirectDeposit);
        Date expectedDDServiceDate = CalendarUtils.convertToDate(twoYearsOutSpcfCalendar);

        try {
            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setEin(company.getFedTaxId());
            searchSAPCompanyRequestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            TestHelper.verifySuccess(searchSAPCompanyResponseDISDTO.getDisResponse());

            Assert.assertEquals(1, searchSAPCompanyResponseDISDTO.getCompanies().size());
            SAPCompanyDISDTO companyDISDTO = searchSAPCompanyResponseDISDTO.getCompanies().get(0);
            TestCase.assertEquals(3,companyDISDTO.getSapCompanyStatus().getServiceStatusCollection().size());

            SAPCompanyServiceStatusDISDTO sapCompanyServiceStatusDISDTO1 = companyDISDTO.getSapCompanyStatus().getServiceStatusCollection().get(0);
            SAPCompanyServiceStatusDISDTO sapCompanyServiceStatusDISDTO2 = companyDISDTO.getSapCompanyStatus().getServiceStatusCollection().get(1);
            SAPCompanyServiceStatusDISDTO sapCompanyServiceStatusDISDTO3 = companyDISDTO.getSapCompanyStatus().getServiceStatusCollection().get(2);

            Date taxServiceEndDate = null;
            Date taxServiceStartDate = null;
            if (sapCompanyServiceStatusDISDTO1.getServiceCd().equals(ServiceCode.Tax.toString())) {
                taxServiceEndDate = sapCompanyServiceStatusDISDTO1.getServiceEndDate();
                taxServiceStartDate = sapCompanyServiceStatusDISDTO1.getServiceStartDate();
            }

            if (sapCompanyServiceStatusDISDTO2.getServiceCd().equals(ServiceCode.Tax.toString())) {
                taxServiceEndDate = sapCompanyServiceStatusDISDTO2.getServiceEndDate();
                taxServiceStartDate = sapCompanyServiceStatusDISDTO2.getServiceStartDate();
            }

            if (sapCompanyServiceStatusDISDTO3.getServiceCd().equals(ServiceCode.Tax.toString())) {
                taxServiceEndDate = sapCompanyServiceStatusDISDTO3.getServiceEndDate();
                taxServiceStartDate = sapCompanyServiceStatusDISDTO3.getServiceStartDate();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            // Actual commit of service end date will be a couple seconds after PSP time, so clear the minutes, seconds and millis.
            taxServiceEndDate.setSeconds(0);
            taxServiceEndDate.setMinutes(0);
            taxServiceEndDate.setTime((taxServiceEndDate.getTime()/ 1000) * 1000);
            assertEquals(expectedTaxEndServiceDate,taxServiceEndDate);

            taxServiceStartDate.setSeconds(0);
            taxServiceStartDate.setMinutes(0);
            taxServiceStartDate.setTime((taxServiceStartDate.getTime()/ 1000) * 1000);
            assertEquals(expectedServiceStartDate,taxServiceStartDate);

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCompanySearchByRealmIdHappyPath() {
        String realm = "987654321012345";
        Company company1 = null;
        try {
            PayrollServices.beginUnitOfWork();
            company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
            company1.setIAMRealmId(realm);
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        // Happypath
        try {
            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setEin(company1.getFedTaxId());
            searchSAPCompanyRequestDISDTO.setRealmId(realm);
            searchSAPCompanyRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            Assert.assertEquals(1, searchSAPCompanyResponseDISDTO.getCompanies().size());
            TestHelper.verifySuccess(searchSAPCompanyResponseDISDTO.getDisResponse());
            Assert.assertNotNull("No Companies found in Response", searchSAPCompanyResponseDISDTO.getCompanies() == null || searchSAPCompanyResponseDISDTO.getCompanies().size() == 0);
            Assert.assertEquals("Realm updation has failed", realm, searchSAPCompanyResponseDISDTO.getCompanies().get(0).getRealmId());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testCompanySearchByRealmId_RealmNotFound() {
        String realm = "987654321012345";
        Company company1 = null;
        try {
            PayrollServices.beginUnitOfWork();
            company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
            company1.setIAMRealmId(realm);
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        //Realm not found case
        try {
            realm = "66764377";
            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setRealmId(realm);
            searchSAPCompanyRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            Assert.assertEquals(0, searchSAPCompanyResponseDISDTO.getCompanies().size());
            TestHelper.verifySuccess(searchSAPCompanyResponseDISDTO.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testCompanySearchByRealmId_NoMatchError() {
        String realm = "987654321012345";
        Company company1 = null;
        try {
            PayrollServices.beginUnitOfWork();
            company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
            company1.setIAMRealmId(realm);
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        // Error case when no input is passed
        SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO = null;
        try {
            DISAdapter disAdapter = new DISAdapter();
            SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO = new SearchSAPCompanyRequestDISDTO();
            searchSAPCompanyRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            searchSAPCompanyResponseDISDTO = disAdapter.Query_SAPCompany(searchSAPCompanyRequestDISDTO);
            Assert.assertNull("Found company when none was expected",searchSAPCompanyResponseDISDTO.getCompanies());
            TestHelper.verifyFailure(searchSAPCompanyResponseDISDTO.getDisResponse());
            assertEquals("Error message code does not match", "DIS-30001", searchSAPCompanyResponseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getCode());
            assertEquals("Error message does not match", "PSP Error: com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISException", searchSAPCompanyResponseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }


    // Helper Methods


    public void changeEIN(Company pCompany,String pNewEin,int pEffectiveDateDay,int pEffectiveDateMonth,int pEffectiveDateYear) {

        PayrollServices.beginUnitOfWork();
        String originalEIN = pCompany.getFedTaxId();
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(pCompany);
        companyDTO.setFein(pNewEin);
        ProcessResult pr = PayrollServices.companyManager.updateCompany(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), companyDTO);
        PSP_PRAssert.assertSuccess("update company", pr);

        DateDTO date = new DateDTO(pEffectiveDateYear,pEffectiveDateMonth,pEffectiveDateDay);
        addEntityChange(pCompany,originalEIN,pNewEin,date);
        PayrollServices.commitUnitOfWork();
    }

    public static String getCompanyAssistedCAN(Company pCompany) throws Throwable {
        CompanyAdapter companyAdapter = new CompanyAdapter();
        ArrayList<SAPEntitlementSearchResult> sapEntitlementSearchResults = companyAdapter.findEntitlementUnits(pCompany.getFedTaxId());
        for (SAPEntitlementSearchResult sapEntitlementSearchResult : sapEntitlementSearchResults) {
            if (sapEntitlementSearchResult.getPSID().equals(pCompany.getSourceCompanyId())) {
                String subtypeDescription = sapEntitlementSearchResult.getSubtypeDescription();
                if (SAPCompanyDISDTO.assistedPayrollSubtypes.contains(subtypeDescription)) {
                    SAPEntitlementInfo sapEntitlementInfo = companyAdapter.getEntitlementInfo(sapEntitlementSearchResult.getLicenseNumber(), sapEntitlementSearchResult.getEoc());
                    if (sapEntitlementInfo != null) {
                        return sapEntitlementInfo.getCustomerId();
                    }
                }
            }
        }
        return null;
    }

    public void addEntityChange(Company pCompany, String pOldEin,String pNewEin,DateDTO pEffectiveDate) {
        EntityChangeDTO entityChangeDTO = new EntityChangeDTO();
        entityChangeDTO.setNewEIN(pNewEin);
        entityChangeDTO.setOldEIN(pOldEin);
        entityChangeDTO.setUserId("testUser");
        entityChangeDTO.setEffectiveDate(pEffectiveDate);

        ProcessResult<EntityChange> entityChangeProcessResult = PayrollServices.companyManager.addOrUpdateEntityChange(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), entityChangeDTO);
        if (!entityChangeProcessResult.isSuccess()) {
            TestCase.fail(entityChangeProcessResult.getErrorMessages().get(0).getMessage());
        }
    }

}
