package com.intuit.sbd.payroll.psp.adapters.ade;

import com.intuit.ems.cep.api.ResourceNameEnum;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.api.messages.MessageCode;
import com.intuit.ems.cep.company.v1.service.Expand;
import com.intuit.ems.cep.company.v1.service.params.CompanyGetListServiceParams;
import com.intuit.ems.cep.company.v1.service.params.CompanyServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxSetupGetServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxSetupUpdateServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.ServiceFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.RateConverterFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.DateUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.schema.payroll.v3.company.Agency;
import com.intuit.schema.payroll.v3.company.Company;
import com.intuit.schema.payroll.v3.company.*;
import com.intuit.schema.payroll.v3.compliance.FilingTypeEnum;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 10/2/13
 * Time: 4:42 AM
 * To change this template use File | Settings | File Templates.
 */

// Todo - review and update this test after implementing changes for PSP-5274

public class CompanyServiceGetAndGetListTests {
    final static String COMPANY_ID = "19670404";
    final static String COMPANY_NAME = "SHIVA TEST ADE";
    final static String COMPANY_FEIN = "223456789";
    final static String CA_SUI_LAW_ID = "87";
    final static String CA_SUI_ETT_LAW_ID = "142";

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 10, 2, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    // Todo - Check this after updating getCompany details
    @SuppressWarnings("unchecked")
    @Test
    public void testCompanyListInactiveCompany() {
        createCompleteNonActiveCompany();
        ServiceResult companyListResult = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyGetListServiceParams(ServiceType.ASSISTED, "US_CA", null));
        assertTrue(companyListResult.isSuccess());
        assertNotNull(companyListResult.getResult());
        List<Company> companies = (List<Company>) companyListResult.getResult();
        assertEquals("Number of companies in list", 0, companies.size());
    }

    @Test
    public void testCompanyListInvalidServiceType() {
        createCompanyWithLaws();
        ServiceResult companyListResult = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyGetListServiceParams(ServiceType.FS, "US_CA", null));
        assertFalse(companyListResult.isSuccess());
        assertNull(companyListResult.getResult());
        assertEquals("Error messages", 1, companyListResult.getMessages().size());
        assertEquals("Error message code", MessageCode.InvalidProperty.getMessageCode(), companyListResult.getMessages().get(0).getMessageCode());
        assertEquals("Error message", "FS is not a valid value for serviceType", companyListResult.getMessages().get(0).getMessage());
    }

    @Test
    public void testCompanyListInvalidJurisdiction() {
        createCompanyWithLaws();
        ServiceResult companyListResult = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyGetListServiceParams(ServiceType.ASSISTED, "US_CA1", null));
        assertFalse(companyListResult.isSuccess());
        assertNull(companyListResult.getResult());
        assertEquals("Error messages", 1, companyListResult.getMessages().size());
        assertEquals("Error message code", MessageCode.InvalidProperty.getMessageCode(), companyListResult.getMessages().get(0).getMessageCode());
        assertEquals("Error message", "US_CA1 is not a valid value for jurisdictionId", companyListResult.getMessages().get(0).getMessage());
    }

    @Test
    public void testCompanyListNonExistingTaxId() {
        createCompanyWithLaws();
        ServiceResult companyListResult = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyGetListServiceParams(ServiceType.ASSISTED, "US_CA", "123456789"));
        assertTrue(companyListResult.isSuccess());
        assertNotNull(companyListResult.getResult());
        assertEquals("Number of companies", 0, ((List) companyListResult.getResult()).size());
    }

    @Test
    public void testCompanyListOnNonActiveCompany() {
        createNonActiveCompany();
        ServiceResult companyListResult = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyGetListServiceParams(ServiceType.ASSISTED, "US_CA", null));
        assertTrue(companyListResult.isSuccess());
        assertNotNull(companyListResult.getResult());
        assertEquals("Number of companies", 0, ((List) companyListResult.getResult()).size());
    }

    @Test
    public void testCompanyListExemptedCompany() {
        createExemptCompany(LawStatus.Exempt, null, null);
        ServiceResult companyListResult = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyGetListServiceParams(ServiceType.ASSISTED, "US_CA", null));
        assertTrue(companyListResult.isSuccess());

        // Company exists in list.
        assertNotNull(companyListResult.getResult());
        assertEquals("Number of companies", 1, ((List) companyListResult.getResult()).size());

        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull("TaxSetup", company.getTaxSetup());

        // Exempt and Reimbursable are treated the same.
        assertTrue(lawIsReimbursable(CA_SUI_LAW_ID, company.getTaxSetup().getTaxItems()));
    }

    @Test
    public void testCompanyListInactiveFilingCompany() {
        createExemptCompany(null, null, PayrollItemStatus.Inactive);
        ServiceResult companyListResult = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyGetListServiceParams(ServiceType.ASSISTED, "US_CA", null));
        assertTrue(companyListResult.isSuccess());

        // Company exists in list.
        assertNotNull(companyListResult.getResult());
        assertEquals("Number of companies", 1, ((List) companyListResult.getResult()).size());

        // Tax Item should not be returned from Tax Setup expansion.
        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull("TaxSetup", company.getTaxSetup());
        assertTrue(lawNotInTaxItems(CA_SUI_LAW_ID, company.getTaxSetup().getTaxItems()));
    }

    @Test
    public void testCompanyListReimbursableCompany() {
        createExemptCompany(null, ReimbursableStatus.Reimbursable, null);
        ServiceResult companyListResult = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyGetListServiceParams(ServiceType.ASSISTED, "US_CA", null));
        assertTrue(companyListResult.isSuccess());

        // Company exists in list.
        assertNotNull(companyListResult.getResult());
        assertEquals("Number of companies", 1, ((List) companyListResult.getResult()).size());

        // Tax Item should not be returned from Tax Setup expansion.
        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull("TaxSetup", company.getTaxSetup());
        assertTrue(lawIsReimbursable(CA_SUI_LAW_ID, company.getTaxSetup().getTaxItems()));
    }

    @Test
    public void testCompanyListDeletedLaws() {
        createCompanyWithLaws();

        // Set QBDT payroll item info to deleted for CA SUI laws.
        Application.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(SourceSystemCode.QBDT, COMPANY_ID, "CAEDD");
        DomainEntitySet<CompanyLaw> companyLaws = companyAgency.getCompanyLawCollection();
        for (CompanyLaw companyLaw : companyLaws) {
            if (companyLaw.getLaw().getLawId().equals(CA_SUI_LAW_ID) || companyLaw.getLaw().getLawId().equals(CA_SUI_ETT_LAW_ID)) {
                QbdtPayrollItemInfo payrollItemInfo = companyLaw.getQbdtPayrollItemInfo();
                payrollItemInfo.setIsDeleted(true);
            }
        }
        Application.commitUnitOfWork();

        ServiceResult companyListResult = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyGetListServiceParams(ServiceType.ASSISTED, "US_CA", null));
        assertTrue(companyListResult.isSuccess());

        // Company exists in list.
        assertNotNull(companyListResult.getResult());
        assertEquals("Number of companies", 1, ((List) companyListResult.getResult()).size());

        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull("TaxSetup", company.getTaxSetup());

        // Tax Payment Group for CA SUI should be inactive.
        for (TaxPaymentGroup taxPaymentGroup : company.getTaxSetup().getTaxPaymentGroups()) {
            if (taxPaymentGroup.getId().equals("US_CA_UIETT_PAYMENT")) {
                assertFalse("isActive should be false", taxPaymentGroup.getIsActive());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCompanyList() {
        createCompanyWithLaws();
        ServiceResult companyListResult = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyGetListServiceParams(ServiceType.ASSISTED, "US_CA", null));
        assertTrue(companyListResult.isSuccess());
        assertNotNull(companyListResult.getResult());
        List<Company> companies = (List<Company>) companyListResult.getResult();
        assertEquals("Number of companies in list", 1, companies.size());
        assertEquals(COMPANY_ID, companies.get(0).getId());
    }

    @Test
    public void testGetCompanyBasicDetails() {
        createCompanyWithLaws();
        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, null));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNull("TaxSetup", company.getTaxSetup());
    }

    @Test
    public void testGetCompanyDetailsWithTaxSetup() {
        createCompanyWithLaws();

        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull("TaxSetup", company.getTaxSetup());
        assertNull("Contacts", company.getContacts());
    }

    @Test
    public void testGetCompanyDetailsWithTaxSetupFilerType941() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2015, 2, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company companyPsp=createCompanyWithLaws();

        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();

        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull("TaxFilingTypes", company.getTaxSetup().getTaxFilingTypes());
        TaxFilingType filingType=company.getTaxSetup().getTaxFilingTypes().get(0);
        assertEquals(FilingTypeEnum.form941, filingType.getFilingType());
        assertNull("FilingType endDate should be empty for active filer type",filingType.getEndDate());
    }
    @Test
    public void testGetCompanyDetailsWithTaxSetupFilerType944() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2015, 2, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company companyPsp=createCompanyWithLaws();

        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();

        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull("TaxFilingTypes", company.getTaxSetup().getTaxFilingTypes());
        TaxFilingType filingType=company.getTaxSetup().getTaxFilingTypes().get(0);
        assertEquals(FilingTypeEnum.form941, filingType.getFilingType());
        assertNull("FilingType endDate should be empty for active filer type",filingType.getEndDate());
        DataLoadServices.refreshCompany(companyPsp);
        DataLoadServices.updateFilerType(companyPsp,"944");
        companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        company = (Company) companyGetResult.getResult();

        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull("TaxFilingTypes", company.getTaxSetup().getTaxFilingTypes());
        filingType=company.getTaxSetup().getTaxFilingTypes().get(0);
        assertEquals(FilingTypeEnum.form944, filingType.getFilingType());
    }
    @Test
    public void testGetCompanyDetailsWithTaxSetupFilerTypePastAndFuture() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 2, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company companyPsp=createCompanyWithLaws();

        DataLoadServices.updateFilerType(companyPsp.getSourceSystemCd().name(),companyPsp.getSourceCompanyId(),"944",SpcfCalendar.createInstance(2015, 2, 25, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateFilerType(companyPsp.getSourceSystemCd().name(),companyPsp.getSourceCompanyId(),"941",SpcfCalendar.createInstance(2015, 6, 25, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateFilerType(companyPsp.getSourceSystemCd().name(),companyPsp.getSourceCompanyId(),"944",SpcfCalendar.createInstance(2015, 10, 25, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2015, 2, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();


        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();

        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull("TaxFilingTypes", company.getTaxSetup().getTaxFilingTypes());
        TaxFilingType filingType=company.getTaxSetup().getTaxFilingTypes().get(0);
        assertEquals(FilingTypeEnum.form944, filingType.getFilingType());
        assertNull("FilingType endDate should be empty for active filer type",filingType.getEndDate());

    }

    @Test
    public void testGetCompanyDetailsWith2SecondaryContacts() {
        com.intuit.sbd.payroll.psp.domain.Company comp = createCompanyWithLaws();

        // Making rest call API with 1 Secondary Principal
        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.CONTACTS.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNull("TaxSetup", company.getTaxSetup());
        assertNotNull("Contacts", company.getContacts());
        assertEquals(3, company.getContacts().size());

        // Adding 2nd secondary contact
        PayrollServices.beginUnitOfWork();
        Application.refresh(comp);
        Contact contact = new Contact();
        contact.setFirstName("Ravi");
        contact.setMiddleName("VL");
        contact.setLastName("SecondaryPrincipal2");
        contact.setPhone("(775) 222-2255");
        contact.setContactRoleCd(ContactRole.SecondaryPrincipal);
        contact.setAuthSignerYnInd(Boolean.TRUE);
        contact.setEmail("SecondaryPrincipal2@intuit.com");
        contact.setTitle("Mr.");
        contact.setJobTitle("Payroll Accountant S2");
        contact.setFax("(775) 202-2055");
        contact.setSecondPhone("(775) 020-0255");
        contact.setMailingAddress(comp.getMailingAddress());
        contact.setCompany(comp);
        comp.addContact(contact);
        Application.save(contact);
        PayrollServices.commitUnitOfWork();

        // Making rest call API with 2 Secondary Principal
        companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.CONTACTS.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNull("TaxSetup", company.getTaxSetup());
        assertNotNull("Contacts", company.getContacts());
        assertEquals(4, company.getContacts().size());

    }

    @Test
    public void testGetCompanyDetailsWithContacts() {
        createCompanyWithLaws();

        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.CONTACTS.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNull("TaxSetup", company.getTaxSetup());
        assertNotNull("Contacts", company.getContacts());
    }

    @Test
    public void testGetCompanyDetailsWithTaxSetupAndContacts() {
        createCompanyWithLaws();

        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.CONTACTS.toString() + Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNull("TaxSetup", company.getTaxSetup());
        assertNull("Contacts", company.getContacts());
    }

    @Test
    public void testGetCompanyDetailsWithTaxSetupTaxPaymentGroupWithNoActiveCompany() {
        createCompleteNonActiveCompany();
        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.CONTACTS.toString() + "," + Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull(company.getTaxSetup());
        assertNotNull(company.getContacts());
        assertNotNull(company.getTaxSetup().getTaxPaymentGroups());
        List<TaxPaymentGroup> taxPaymentGroupList = company.getTaxSetup().getTaxPaymentGroups();
        assertTrue(taxPaymentGroupList.size() == 2);
    }


    @Test
    public void testGetCompanyDetailsWithTaxSetupTaxPaymentGroupWithNoStateTemplates() {
        createNonActiveCompany();
        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.CONTACTS.toString() + "," + Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull(company.getTaxSetup());
        assertNotNull(company.getContacts());
        assertNotNull(company.getTaxSetup().getTaxPaymentGroups());
        List<TaxPaymentGroup> taxPaymentGroupList = company.getTaxSetup().getTaxPaymentGroups();
        assertTrue(taxPaymentGroupList.size() == 2);
    }

    @Test
    public void testGetCompanyDetailsWithTaxSetupTaxPaymentGroup() {
        createCompanyWithLaws();
        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.CONTACTS.toString() + "," + Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNotNull(company.getTaxSetup());
        assertNotNull(company.getContacts());
        assertNotNull(company.getTaxSetup().getTaxPaymentGroups());
        List<TaxPaymentGroup> taxPaymentGroupList = company.getTaxSetup().getTaxPaymentGroups();
        assertTrue(taxPaymentGroupList.size() == 4);
    }

    @Test
    public void testNV_Round_TripWithNoBondRate() {
        createAssistedCompanyWithRates("NV", COMPANY_ID, "987654321", "4595533-0");

        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.TAXSETUP.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertNotNull(company.getTaxSetup());
        // assert Agencies
        assertEquals("Agencies", 2, company.getTaxSetup().getAgencies().size());
        for (Agency agency : company.getTaxSetup().getAgencies()) {
            if(agency.getId().equals("US_FEDERAL_IRS")) {
                assertEquals("Jurisdiction Id for IRS", "US_FEDERAL", agency.getJurisdictionId());
            } else if(agency.getId().equals("US_NV_ETR")) {
                assertEquals("Jurisdiction Id for NV ETR", "US_NV", agency.getJurisdictionId());
            } else {
                assertFalse("Unexpected Agency Id is found", true);
            }
        }
        // assert payment groups
        assertEquals("Payment groups IRS-941, IRS-940 and NV_NUCS4072", 3, company.getTaxSetup().getTaxPaymentGroups().size());

        java.util.List<com.intuit.schema.payroll.v3.company.TaxItem> taxItems = company.getTaxSetup().getTaxItems();
        // 2 Company Laws and 1 company filing amount
        assertEquals("Tax Items", 3, taxItems.size());
        List<String> expectedTaxItemIds = new ArrayList<String>();
        expectedTaxItemIds.add("US_NV_SUI_ER_UI");
        expectedTaxItemIds.add("US_NV_SC_ER_CEP");
        expectedTaxItemIds.add("US_NV_SC_ER_SBC");
        for (TaxItem taxitem : taxItems) {
            assertTrue("unexpected tax Item is present, Tax Item Id: " + taxitem.getId(), expectedTaxItemIds.contains(taxitem.getId()));
            if(taxitem.getId().equals("US_NV_SUI_ER_UI")) {
                assertEquals("Rates for US_NV_SUI_ER_UI", 1, taxitem.getTaxRates().size());
                assertEquals("amount", new BigDecimal("3.0"), taxitem.getTaxRates().get(0).getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), taxitem.getTaxRates().get(0).getStartDate());
                assertNull("rate end date ", taxitem.getTaxRates().get(0).getEndDate());
            } else if(taxitem.getId().equals("US_NV_SC_ER_CEP")) {
                assertEquals("Rates for US_NV_SC_ER_CEP", 1, taxitem.getTaxRates().size());
                assertEquals("amount", new BigDecimal("3.0"), taxitem.getTaxRates().get(0).getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), taxitem.getTaxRates().get(0).getStartDate());
                assertNull("rate end date ", taxitem.getTaxRates().get(0).getEndDate());
            } else if(taxitem.getId().equals("US_NV_SC_ER_SBC")) {
                assertEquals("Rates for company filing amount (US_NV_SC_ER_SBC)", 1, taxitem.getTaxRates().size());
                assertEquals("amount", new BigDecimal("1234.0"), taxitem.getTaxRates().get(0).getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), taxitem.getTaxRates().get(0).getStartDate());
                assertNull("rate end date ", taxitem.getTaxRates().get(0).getEndDate());
            }
        }

        // Updating TaxSetup
        TaxSetupUpdateServiceParams taxSetupUpdateServiceParams = new TaxSetupUpdateServiceParams();
        taxSetupUpdateServiceParams.setCompanyId(COMPANY_ID);
        taxSetupUpdateServiceParams.setExpand(null);
        taxSetupUpdateServiceParams.setSendEmail(false);

        ServiceResult serviceResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                                                    .service(PayloadHelper.getTaxSetupNV("3.725", "0.05", "0.36"), taxSetupUpdateServiceParams);
        assertTrue(serviceResult.isSuccess());
        TaxSetup taxSetup = (TaxSetup) serviceResult.getResult();
        assertNotNull(taxSetup);
        taxItems = taxSetup.getTaxItems();
        // 2 Company laws and 1 company filing amount
        assertEquals("Tax Items", 3, taxItems.size());
        for (TaxItem taxitem : taxItems) {
            assertTrue("unexpected tax Item is present, Tax Item Id: " + taxitem.getId(), expectedTaxItemIds.contains(taxitem.getId()));
            if(taxitem.getId().equals("US_NV_SC_ER_SBC")) {
                assertEquals("Rates for company filing amount (US_NV_SC_ER_SBC)", 1, taxitem.getTaxRates().size());
                assertEquals("amount", new BigDecimal("0.36"), taxitem.getTaxRates().get(0).getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), taxitem.getTaxRates().get(0).getStartDate());
                assertNull("rate end date ", taxitem.getTaxRates().get(0).getEndDate());
            } else if(taxitem.getId().equals("US_NV_SUI_ER_UI")) {
                assertEquals("Rates for US_NV_SUI_ER_UI", 1, taxitem.getTaxRates().size());
                assertEquals("amount", new BigDecimal("3.725"), taxitem.getTaxRates().get(0).getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), taxitem.getTaxRates().get(0).getStartDate());
                assertNull("rate end date ", taxitem.getTaxRates().get(0).getEndDate());
            } else if(taxitem.getId().equals("US_NV_SC_ER_CEP")) {
                assertEquals("Rates for US_NV_SC_ER_CEP", 1, taxitem.getTaxRates().size());
                assertEquals("amount", new BigDecimal("0.05"), taxitem.getTaxRates().get(0).getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), taxitem.getTaxRates().get(0).getStartDate());
                assertNull("rate end date ", taxitem.getTaxRates().get(0).getEndDate());
            }
        }

        // Testing Get TaxSetup with all the rates
        TaxSetupGetServiceParams taxSetupGetServiceParams = new TaxSetupGetServiceParams();
        taxSetupGetServiceParams.setCompanyId(COMPANY_ID);
        taxSetupGetServiceParams.setShowAllParam("TAXRATE");
        taxSetupGetServiceParams.setExpand(Expand.TAXSETUP.toString());

        // Testing GetSetup with rates history
        ServiceResult setupServiceResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetupGetServiceParams);
        assertTrue(setupServiceResult.isSuccess());
        assertNotNull(setupServiceResult.getResult());
        taxSetup = (TaxSetup) setupServiceResult.getResult();
        assertNotNull(taxSetup);
        taxItems = taxSetup.getTaxItems();
        // assert Agencies
        assertEquals("Agencies", 2, taxSetup.getAgencies().size());

        // assert payment groups
        assertEquals("Payment groups IRS-941, IRS-940 and NV_NUCS4072", 3, taxSetup.getTaxPaymentGroups().size());

        // 2 Company Laws and 1 company filing amount
        assertEquals("Tax Items", 3, taxItems.size());
        for (TaxItem taxitem : taxItems) {
            assertTrue("unexpected tax Item is present, Tax Item Id: " + taxitem.getId(), expectedTaxItemIds.contains(taxitem.getId()));
            if(taxitem.getId().equals("US_NV_SC_ER_SBC")) {
                assertEquals("Rates for company filing amount (US_NV_SC_ER_SBC)", 2, taxitem.getTaxRates().size());
                TaxRate oldTaxRate, newTaxRate;
                if(taxitem.getTaxRates().get(0).getEndDate() == null) {
                    newTaxRate = taxitem.getTaxRates().get(0);
                    oldTaxRate = taxitem.getTaxRates().get(1);
                } else {
                    newTaxRate = taxitem.getTaxRates().get(1);
                    oldTaxRate = taxitem.getTaxRates().get(0);
                }
                assertEquals("amount", new BigDecimal("0.36"), newTaxRate.getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), newTaxRate.getStartDate());
                assertNull("rate end date ", newTaxRate.getEndDate());

                assertEquals("amount", new BigDecimal("1234.0"), oldTaxRate.getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), oldTaxRate.getStartDate());
                assertNotNull("rate end date ", oldTaxRate.getEndDate());

            } else if(taxitem.getId().equals("US_NV_SUI_ER_UI")) {
                assertEquals("Rates for US_NV_SUI_ER_UI", 2, taxitem.getTaxRates().size());
                TaxRate oldTaxRate, newTaxRate;
                if(taxitem.getTaxRates().get(0).getEndDate() == null) {
                    newTaxRate = taxitem.getTaxRates().get(0);
                    oldTaxRate = taxitem.getTaxRates().get(1);
                } else {
                    newTaxRate = taxitem.getTaxRates().get(1);
                    oldTaxRate = taxitem.getTaxRates().get(0);
                }
                assertEquals("amount", new BigDecimal("3.725"), newTaxRate.getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), newTaxRate.getStartDate());
                assertNull("rate end date ", newTaxRate.getEndDate());

                assertEquals("amount", new BigDecimal("3.0"), oldTaxRate.getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), oldTaxRate.getStartDate());
                assertNotNull("rate end date ", oldTaxRate.getEndDate());

            } else if(taxitem.getId().equals("US_NV_SC_ER_CEP")) {
                assertEquals("Rates for US_NV_SC_ER_CEP", 2, taxitem.getTaxRates().size());
                TaxRate oldTaxRate, newTaxRate;
                if(taxitem.getTaxRates().get(0).getEndDate() == null) {
                    newTaxRate = taxitem.getTaxRates().get(0);
                    oldTaxRate = taxitem.getTaxRates().get(1);
                } else {
                    newTaxRate = taxitem.getTaxRates().get(1);
                    oldTaxRate = taxitem.getTaxRates().get(0);
                }
                assertEquals("amount", new BigDecimal("0.05"), newTaxRate.getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), newTaxRate.getStartDate());
                assertNull("rate end date ", newTaxRate.getEndDate());

                assertEquals("amount", new BigDecimal("3.0"), oldTaxRate.getRate());
                assertEquals("rate state date ", DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), oldTaxRate.getStartDate());
                assertNotNull("rate end date ", oldTaxRate.getEndDate());
            }
        }
    }

    @Test
    public void testGetCompanyDetailsForDGDeletedCompany() {
        com.intuit.sbd.payroll.psp.domain.Company companyWithLaws = createCompanyWithLaws();

        ServiceResult companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.CONTACTS.toString()));
        assertTrue(companyGetResult.isSuccess());
        assertNotNull(companyGetResult.getResult());
        Company company = (Company) companyGetResult.getResult();
        assertEquals(COMPANY_ID, company.getId());
        assertEquals(COMPANY_NAME, company.getCompanyLegalName());
        assertNull("TaxSetup", company.getTaxSetup());
        assertNotNull("Contacts", company.getContacts());

        Application.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company1 = Application.findById(com.intuit.sbd.payroll.psp.domain.Company.class, companyWithLaws.getId());
        company1.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company1);
        Application.commitUnitOfWork();

        companyGetResult = ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(getCompanyServiceParams(COMPANY_ID, Expand.CONTACTS.toString()));
        assertFalse(companyGetResult.isSuccess());
        assertNull(companyGetResult.getResult());
    }

    public static com.intuit.sbd.payroll.psp.domain.Company createAssistedCompanyWithRates(String state, String psid, String ein, String aid) {
        // Company with laws, rates, etc.
        com.intuit.sbd.payroll.psp.domain.Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds(state);
        lawIds.removeAll(Arrays.asList(RateConverterFactory.INACTIVE_LAW_IDS_FOR_SUI_RATE_EXCHANGE));  //remove all inactive laws for SUI rate exchange
        DataLoadServices.addCompanyLawsWithAgencyId(aid, company, state, lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addAdditionalFilingAmounts(company);
        return company;
    }

    private com.intuit.sbd.payroll.psp.domain.Company createCompanyWithLaws() {
        // CA Company with laws, rates, etc.
        com.intuit.sbd.payroll.psp.domain.Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, COMPANY_ID, COMPANY_FEIN, true, ServiceCode.Tax);
        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        company.setLegalName(COMPANY_NAME);
        company.setDbaName(company.getLegalName());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.addCompanyLawsWithAgencyId("311-0765-1", company, "CA");
        DataLoadServices.addCompanyLawRates(company);
        return company;
    }

    private void createNonActiveCompany() {
        // CA Company with laws, rates, etc. that is on hold.
        com.intuit.sbd.payroll.psp.domain.Company onHoldCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, COMPANY_ID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(onHoldCompany);
        // / Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(onHoldCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        onHoldCompany.setLegalName(COMPANY_NAME);
        onHoldCompany.setDbaName(onHoldCompany.getLegalName());
        PayrollServices.commitUnitOfWork();
    }

    private void createCompleteNonActiveCompany() {
        // CA Company with laws, rates, etc. that is on hold.
        com.intuit.sbd.payroll.psp.domain.Company onHoldCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, COMPANY_ID, false, ServiceCode.Tax);
        // / Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(onHoldCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        onHoldCompany.setLegalName(COMPANY_NAME);
        onHoldCompany.setDbaName(onHoldCompany.getLegalName());
        PayrollServices.commitUnitOfWork();
    }

    private void createExemptCompany(LawStatus exemptionStatus, ReimbursableStatus reimbursableStatus, PayrollItemStatus filingStatus) {
        // CA Company with laws, rates, etc. that is Exempt.
        com.intuit.sbd.payroll.psp.domain.Company exemptCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, COMPANY_ID, true, ServiceCode.Tax);
        List<CompanyLaw> claws = DataLoadServices.addCompanyLawsWithAgencyId("123-4567-9", exemptCompany, "CA");
        DataLoadServices.addCompanyLawRates(exemptCompany);
        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(exemptCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        exemptCompany.setLegalName(COMPANY_NAME);
        exemptCompany.setDbaName(exemptCompany.getLegalName());
        for (CompanyLaw claw : claws) {
            // Set CA SUI_ER to Exempt.
            if (claw.getLaw().getLawId().equals(CA_SUI_LAW_ID)) {
                Application.refresh(claw);
                if (exemptionStatus != null) {
                    claw.setExemptionStatus(exemptionStatus);
                }
                if (reimbursableStatus != null) {
                    claw.setReimbursableStatus(reimbursableStatus);
                }
                if (filingStatus != null) {
                    claw.setFilingStatus(filingStatus);
                }
                break;
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    public boolean lawNotInTaxItems(String lawId, List<TaxItem> taxItems) {
        Law law = Application.findById(Law.class, lawId);

        for (TaxItem taxItem : taxItems) {
            if (law.getDescription().equals(taxItem.getName())) {
                return false;
            }
        }

        return true;
    }

    public boolean lawIsReimbursable(String lawId, List<TaxItem> taxItems) {
        Law law = Application.findById(Law.class, lawId);

        for (TaxItem taxItem : taxItems) {
            if (law.getDescription().equals(taxItem.getName())) {
                return taxItem.getIsReimbursable();
            }
        }

        return false;
    }

    public CompanyGetListServiceParams getCompanyGetListServiceParams(ServiceType serviceType, String jurisdiction, String taxId) {
        CompanyGetListServiceParams companyGetListServiceParams = new CompanyGetListServiceParams();
        companyGetListServiceParams.setServiceType(serviceType.toString());
        companyGetListServiceParams.setJurisdiction(jurisdiction);
        companyGetListServiceParams.setTaxId(taxId);
        return companyGetListServiceParams;
    }

    public CompanyServiceParams getCompanyServiceParams(String companyId, String expandItem) {
        CompanyServiceParams companyServiceParams = new CompanyServiceParams();
        companyServiceParams.setCompanyId(companyId);
        companyServiceParams.setExpand(expandItem);
        return companyServiceParams;
    }
}
