package com.intuit.sbd.payroll.psp.processes.common;

import com.intuit.money.account.model.AccountHolder;
import com.intuit.money.account.model.FundingInstrument;
import com.intuit.money.common.model.GovernmentId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;

public class PSPToSMSMigrationHelperTests {

    private static final String MONEYOUT = "MONEYOUT";
    private static final String COUNTRY_US = "US";
    private static final String QBDT = "QBDT";
    private static final String BUSINESS = "Business";
    private static final String LOCATION = "Location";
    private static final String TAX_ID = "TAX_ID";
    private static final String PRINCIPAL = "PRINCIPAL";
    private static final String RESIDENCE = "Residence";
    private static final String VERIFIED = "Verified";
    private static final String SSN = "SSN";
    private static final String SIC = "sic";
    private static final String PHONE = "phone";
    private static final String BANKACCOUNTNUMBER = "bankAccountNumber";
    private static final int SIC_STANDARD_LENGTH = 4;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testGetCompanyAddress() {
        Company company = new Company();
        Address complianceAddress = new Address();

        complianceAddress.setAddressLine1("Address Line 1");
        complianceAddress.setAddressLine2("Address Line 2");
        complianceAddress.setCity("City");
        complianceAddress.setState("CA");
        complianceAddress.setZipCode("56003");
        complianceAddress.setZipCodeExtension("5422");
        complianceAddress.setCountry("US");

        company.setComplianceAddress(complianceAddress);

        com.intuit.money.account.model.Company mCompany = new com.intuit.money.account.model.Company();
        mCompany.setAddress(PSPToSMSMigrationHelper.getCompanyAddress(company, LOCATION));

        List<com.intuit.money.common.model.Address> mAddressList = mCompany.getAddress();
        com.intuit.money.common.model.Address mAddress = mAddressList.get(0);


        Assert.assertEquals(mAddress.getStreetAddressLine1(), complianceAddress.getAddressLine1());
        Assert.assertEquals(mAddress.getStreetAddressLine2(), complianceAddress.getAddressLine2());
        Assert.assertEquals(mAddress.getCity(), complianceAddress.getCity());
        Assert.assertEquals(mAddress.getRegion(), complianceAddress.getState());
        Assert.assertEquals(mAddress.getPostalCode(), complianceAddress.getFullZipCode());
        Assert.assertEquals(mAddress.getCountry(), complianceAddress.getCountry());
        Assert.assertEquals(mAddress.getType(), LOCATION);
    }

    @Test
    public void testGetCompanyAddressWithEmptyCountry() {
        Company company = new Company();
        Address complianceAddress = new Address();

        complianceAddress.setAddressLine1("Address Line 1");
        complianceAddress.setAddressLine2("Address Line 2");
        complianceAddress.setCity("City");
        complianceAddress.setState("CA");
        complianceAddress.setZipCode("56003");
        complianceAddress.setZipCodeExtension("5422");
        complianceAddress.setCountry(null);

        company.setComplianceAddress(complianceAddress);

        com.intuit.money.account.model.Company mCompany = new com.intuit.money.account.model.Company();
        mCompany.setAddress(PSPToSMSMigrationHelper.getCompanyAddress(company, LOCATION));

        List<com.intuit.money.common.model.Address> mAddressList = mCompany.getAddress();
        com.intuit.money.common.model.Address mAddress = mAddressList.get(0);


        Assert.assertEquals(mAddress.getStreetAddressLine1(), complianceAddress.getAddressLine1());
        Assert.assertEquals(mAddress.getStreetAddressLine2(), complianceAddress.getAddressLine2());
        Assert.assertEquals(mAddress.getCity(), complianceAddress.getCity());
        Assert.assertEquals(mAddress.getRegion(), complianceAddress.getState());
        Assert.assertEquals(mAddress.getPostalCode(), complianceAddress.getFullZipCode());
        Assert.assertEquals(mAddress.getCountry(), "US");
        Assert.assertEquals(mAddress.getType(), LOCATION);
    }

    @Test
    public void testGetCompanyAddressWithEmptyZipExtension() {
        Company company = new Company();
        Address complianceAddress = new Address();

        complianceAddress.setAddressLine1("Address Line 1");
        complianceAddress.setAddressLine2("Address Line 2");
        complianceAddress.setCity("City");
        complianceAddress.setState("CA");
        complianceAddress.setZipCode("56003");
        complianceAddress.setCountry("US");

        company.setComplianceAddress(complianceAddress);

        com.intuit.money.account.model.Company mCompany = new com.intuit.money.account.model.Company();
        mCompany.setAddress(PSPToSMSMigrationHelper.getCompanyAddress(company, LOCATION));

        List<com.intuit.money.common.model.Address> mAddressList = mCompany.getAddress();
        com.intuit.money.common.model.Address mAddress  = mAddressList.get(0);


        Assert.assertEquals(mAddress.getStreetAddressLine1(), complianceAddress.getAddressLine1());
        Assert.assertEquals(mAddress.getStreetAddressLine2(), complianceAddress.getAddressLine2());
        Assert.assertEquals(mAddress.getCity(), complianceAddress.getCity());
        Assert.assertEquals(mAddress.getRegion(), complianceAddress.getState());
        Assert.assertEquals(mAddress.getPostalCode(), complianceAddress.getFullZipCode());
        Assert.assertEquals(mAddress.getPostalCode(), complianceAddress.getZipCode());
        Assert.assertEquals(mAddress.getCountry(), "US");
        Assert.assertEquals(mAddress.getType(), LOCATION);
    }

    @Test
    public void testGovernmentIds() {
        Company company = new Company();
        company.setFedTaxId("121231234");

        List<GovernmentId> mGovernmentIds = PSPToSMSMigrationHelper.getGovernmentIds(company);
        GovernmentId mGovernmentId = mGovernmentIds.get(0);

        Assert.assertEquals(mGovernmentId.getIdType(), TAX_ID);
        Assert.assertEquals(mGovernmentId.getGovernmentId(), company.getFedTaxId());
    }

    @Test
    public void testGetFundingInstruments() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, false, true);
        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        // Commit
        PayrollServices.commitUnitOfWork();

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        assertEquals(companies.size(), 1);
        Company company = companies.get(0);
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());
        CompanyService ddService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", companyBankAccountDTO.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.Active,
                companyBankAccount.getStatusCd());

        List<FundingInstrument> mFundingInstrumentList = PSPToSMSMigrationHelper.getFundingInstruments(company);
        FundingInstrument mFundingInstrument = mFundingInstrumentList.get(0);

        assertEquals(mFundingInstrument.getBank().getBankCode(), companyBankAccountDTO.getBankAccountDTO().getRoutingNumber());
        assertEquals(mFundingInstrument.getBank().getAccountNumber(), companyBankAccountDTO.getBankAccountDTO().getAccountNumber());
        assertEquals(mFundingInstrument.getBank().getAccountType(), companyBankAccountDTO.getBankAccountDTO().getAccountType().toString());
        assertEquals(mFundingInstrument.getVerifiedBankDepositStatus(), VERIFIED);

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testGetContactGovernmentIds() {
        Contact contact = new Contact();
        contact.setSocialSecurityNumber("121231234");

        List<GovernmentId> governmentIds = PSPToSMSMigrationHelper.getContactGovernmentIds(contact);

        assertEquals(governmentIds.get(0).getIdType(), SSN);
        assertEquals(governmentIds.get(0).getGovernmentId(), contact.getSocialSecurityNumber());
    }

    @Test
    public void testGetAccountHolders() {
        Contact contact = new Contact();
        contact.setFirstName("First");
        contact.setLastName("Last");
        contact.setPhone("91212133");
        contact.setMailingAddress(new Address());
        contact.setDateOfBirth(SpcfCalendar.getNow());
        List<AccountHolder> accountHolders = PSPToSMSMigrationHelper.getAccountHolders(contact);

        assertEquals(accountHolders.get(0).getType(), PRINCIPAL);
        assertEquals(accountHolders.get(0).getFirstName(), contact.getFirstName());
        assertEquals(accountHolders.get(0).getLastName(), contact.getLastName());
        assertEquals(accountHolders.get(0).getPhone(), contact.getPhone());
        assertEquals(accountHolders.get(0).getDateOfBirth().toString(), contact.getDateOfBirth().toLocalDate().toString());
        assertEquals(accountHolders.get(0).getAddress().getType(), RESIDENCE);
    }

    @Test
    public void testCheckAndReplaceFieldForSplChar() {
        String phone = "+91-899-43-123";
        String resultWithoutSplChar = PSPToSMSMigrationHelper.checkAndReplaceFieldForSplChar("Phone", phone, true);
        Assert.assertEquals(resultWithoutSplChar, "9189943123");
    }

}
