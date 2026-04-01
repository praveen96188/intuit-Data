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
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccessResult;
import static org.junit.Assert.*;


public class DataLoader {
    public static final AddressDTO TAXABLE_ADDRESS = new AddressDTO();

    static {
        TAXABLE_ADDRESS.setAddressLine1("13433 Wyoming Valley");
        TAXABLE_ADDRESS.setCity("Austin");
        TAXABLE_ADDRESS.setState("TX");
        TAXABLE_ADDRESS.setZipCode("78727");
    }

    public static final AddressDTO TAXABLE_ADDRESS2 = new AddressDTO();

    static {
        TAXABLE_ADDRESS2.setAddressLine1("23433 Wyoming Valley");
        TAXABLE_ADDRESS2.setCity("Austin");
        TAXABLE_ADDRESS2.setState("TX");
        TAXABLE_ADDRESS2.setZipCode("78727");
    }

    public static final AddressDTO NON_TAXABLE_ADDRESS = new AddressDTO();

    static {
        NON_TAXABLE_ADDRESS.setAddressLine1("7555 Torrey Santa Fe Ct");
        NON_TAXABLE_ADDRESS.setCity("San Diego");
        NON_TAXABLE_ADDRESS.setState("CA");
        NON_TAXABLE_ADDRESS.setZipCode("92129");
    }

    private SourceSystemCode srcSystemCodeForNewCompany = SourceSystemCode.QBOE;

    public SourceSystemCode getSrcSystemCodeForNewCompany() {
        return srcSystemCodeForNewCompany;
    }

    public void setSrcSystemCodeForNewCompany(SourceSystemCode pSrcSystemCodeForNewCompany) {
        srcSystemCodeForNewCompany = pSrcSystemCodeForNewCompany;
    }

    /**
     * @deprecated replaced by {@link DataLoadServices#setPrincipalToAgent(com.intuit.sbd.payroll.psp.domain.OperationId...)
     */
    @Deprecated
    public static void setPrincipalIsAgent() {
        AuthUser user = addUser("UnitTestAgent", "First", "Last");
        // Set PSP Principal for the User to make current principal is agent
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName()));
    }

    public static AuthUser addUser(String pUserId, String pFirstName, String pLastName) {
        // Add user
        AuthRole foundRole = AuthRole.findRole("DesktopCareManager");

        ProcessResult<AuthUser> processResult =
                PayrollServices.userManager.addUser(pUserId, Arrays.asList(foundRole.getRoleId()), pFirstName, pLastName);

        assertSuccess("Add User ProcessResult ", processResult);

        return processResult.getResult();
    }

    public Company persistCompany(CompanyDTO pCompanyDTO) {
        return persistCompany(pCompanyDTO, AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public Company persistCompany(CompanyDTO pCompanyDTO, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(pCompanyDTO);

        Company company = assertSuccessResult("addCompany", result);

        if(company.getSourceSystemCd() == SourceSystemCode.QBDT) {
            company.setOffloadGroup(offloadGroup);
            PayrollServices.commitUnitOfWork();
            switch (assetItemNumber) {
                case DIY_USAGE_BILLING_MONTHLY:
                    DataLoadServices.addEntitlementUnit(company, "09876543210987654321", "09876543210987654321", EditionType.Basic, null, assetItemNumber, null);
                    break;
                default:
                    DataLoadServices.addEntitlementUnit(company, "123456", "654321");
                    break;
            }
            PayrollServices.beginUnitOfWork();
            company = Application.refresh(company);
        }

        return company;
    }

    public static ProcessResult<Company> addCompany(CompanyDTO pCompanyDTO) {
        return PayrollServices.companyManager.addCompany(pCompanyDTO);
    }

    public Company persistTestIntuitCompany() {
        return persistCompany(getTestIntuitCompany());
    }
    
    /**
     * Adds a company bank account without verifying it
     *
     * @param pCompany
     * @param pCompanyBankAccountDTO
     * @return
     */
    public CompanyBankAccount persistCompanyBankAccountNoVerify(Company pCompany, CompanyBankAccountDTO pCompanyBankAccountDTO) {
        Application.beginUnitOfWork();

        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(pCompany.getSourceSystemCd(),
                        pCompany.getSourceCompanyId(),
                        pCompanyBankAccountDTO, true, true);

        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);

        Application.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        return addCBAProcResult.getResult();
    }

    public CompanyBankAccount persistCompanyBankAccount(Company pCompany, CompanyBankAccountDTO pCompanyBankAccountDTO) {
        return persistCompanyBankAccount(pCompany, pCompanyBankAccountDTO, pCompany.getOffloadGroup());
    }

    /**
     * Adds a company bank account and verifies it. Moves the PSPDate 10 days ahead
     *
     * @param pCompany
     * @param pCompanyBankAccountDTO
     * @return
     */
    public CompanyBankAccount persistCompanyBankAccount(Company pCompany, CompanyBankAccountDTO pCompanyBankAccountDTO, OffloadGroup offloadGroup) {
        return (persistCompanyBankAccountWithVerifyBankOpt(pCompany, pCompanyBankAccountDTO, true, offloadGroup));
    }

    public CompanyBankAccount persistCompanyBankAccountWithVerifyBankOpt(Company pCompany, CompanyBankAccountDTO pCompanyBankAccountDTO, boolean pVerifyBankAccount) {
        return persistCompanyBankAccountWithVerifyBankOpt(pCompany, pCompanyBankAccountDTO, pVerifyBankAccount, pCompany.getOffloadGroup());
    }


    public CompanyBankAccount persistCompanyBankAccountWithVerifyBankOpt(Company pCompany, CompanyBankAccountDTO pCompanyBankAccountDTO, boolean pVerifyBankAccount, OffloadGroup offloadGroup) {

        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(pCompany.getSourceSystemCd(),
                        pCompany.getSourceCompanyId(),
                        pCompanyBankAccountDTO, true, true);

        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);

        Application.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), PSPDate.getPSPTime());

        Application.beginUnitOfWork();

        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);

        CompanyBankAccount companyBankAccount = Application.findById(CompanyBankAccount.class, addCBAProcResult.getResult().getId());

        if (pVerifyBankAccount) {
            DomainEntitySet<FinancialTransaction> verificationTransactions =
                    companyBankAccount.getVerificationTransactions();

            ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();

            for (FinancialTransaction financialTransaction : verificationTransactions) {
                amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
            }

            ProcessResult<CompanyBankAccount> processResult =
                    PayrollServices.companyManager.verifyCompanyBankAccount(pCompany.getSourceSystemCd(),
                            pCompany.getSourceCompanyId(),
                            companyBankAccount.getSourceBankAccountId(),
                            amountsToVerify.get(0),
                            amountsToVerify.get(1), false);

            assertSuccess("verifyCompanyBankAccount", processResult);
        }

        Application.getHibernateSession().flush();

        return companyBankAccount;
    }

    public Company persistTestIntuitCompany2() {
        return persistCompany(getTestIntuitCompany2());
    }

    public Company persistTestActiveCompany() {
        return persistCompany(getTestActiveCompany());
    }

    public Company persistTestActiveCompany1() {
        return persistCompany(getTestActiveCompany1());
    }

    public Company persistTestActiveCompany(String fein, String psid) {
        return persistCompany(getTestActiveCompany(fein, psid));
    }
    
    public static OfferingInfoDTO getOfferingInfoDTOForPayrollSubTypeCd(PayrollSubtypeCode pPayrollSubtypeCode) {
        OfferingInfoDTO offeringInfoDTO = new OfferingInfoDTO();
        offeringInfoDTO.setPayrollSubTypeCd(pPayrollSubtypeCode);
        return offeringInfoDTO;
    }

    public Company persistTestActiveCompany123123123() {
        return persistCompany(getTestActiveCompany123123123());
    }

    public CompanyService persistCompanyService(Company pCompany, ServiceInfoDTO pCompanyService) {

        ProcessResult<CompanyService> ddServiceAddProcessResult =
                PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(),  pCompanyService);

        assertSuccess("addService", ddServiceAddProcessResult);

        return CompanyService.findCompanyService(pCompany, pCompanyService.getServiceCode());
    }

    public CompanyService persistTestCompanyService(Company pCompany) {
        ProcessResult<CompanyService> ddServiceAddProcessResult =
                PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(),
                        pCompany.getSourceCompanyId(),
                        getTestCompanyService());

        assertSuccess("addService", ddServiceAddProcessResult);


        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        return CompanyService.findCompanyService(pCompany, ServiceCode.DirectDeposit);
    }
    public CompanyService persistTestCompanyTaxService(Company pCompany) {
        {
            String modifyLic = "";
            PayrollServices.beginUnitOfWork();
            pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
            boolean hasAssistedEntitlementUnit = false;
            for (EntitlementUnit entitlementUnit : pCompany.getEntitlementUnitCollection()) {
                if(!entitlementUnit.getEntitlement().getEntitlementCode().isAssisted()) {
                    entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                    Application.save(entitlementUnit);
                    modifyLic = "Y";
                } else {
                    hasAssistedEntitlementUnit = true;
                    if(entitlementUnit.isDeactivated()) {
                        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
                    }
                }
            }
            PayrollServices.commitUnitOfWork();

            if(!hasAssistedEntitlementUnit) {
                DataLoadServices.addAssistedEntitlementUnit(pCompany, DataLoadServices.LIC_PREFIX + pCompany.getSourceCompanyId() + modifyLic, DataLoadServices.EOC_PREFIX + pCompany.getSourceCompanyId(), true);
            }

            TaxServiceInfoDTO taxServiceInfoDTO = new TaxServiceInfoDTO();
            taxServiceInfoDTO.setServiceStartDate(PSPDate.getPSPTime());
            PayrollServices.beginUnitOfWork();
            ProcessResult<CompanyService> pr = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), taxServiceInfoDTO);
            assertSuccess("addTaxService", pr);
            PayrollServices.commitUnitOfWork();

            return pr.getResult();
        }
    }
    public CompanyService persistBillPaymentCompanyService(Company pCompany) {
        ProcessResult<CompanyService> ddServiceAddProcessResult =
                PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), getTestCompanyBillPaymentService());

        assertSuccess("addService", ddServiceAddProcessResult);

        return CompanyService.findCompanyService(pCompany, ServiceCode.DirectDeposit);
    }

    public CompanyDTO getTestIntuitCompany() {
        CompanyDTO company = new CompanyDTO();

        company.setDBA("Intuit");
        company.setFein("123456789");
        company.setLegalAddress(getTestLegalAddress());
        company.setLegalName("Intuit");
        company.setMailingAddress(getTestMailingAddress());
        company.setNotificationEmail("notifications@intuit.com");
        company.setCompanyId("123456");
        company.setSourceSystemCd(srcSystemCodeForNewCompany);
        company.setPayrollFrequencyCd(PayrollFrequencyDTO.Monthly);
        company.setContacts(getTestContacts());
        company.setNextEmployeeId("1");
        company.setNextPaycheckId("1");
        company.setNextPayrollItemId("1");
        company.setNextPayrollTransactionId("1");
        company.setCurrentToken(1L);
        company.setPriceType("Standard");

        return company;
    }

    public CompanyDTO getTestIntuitCompany_ChangedPayrollAdmin() {
        CompanyDTO company = getTestIntuitCompany();

        for (ContactDTO contact : company.getContacts()) {
            if (contact.getContactRoleCd() == ContactRole.PayrollAdmin) {
                contact.setFirstName("NewFirstName");
                contact.setMiddleName("NewMiddleName");
                contact.setLastName("NewLastName");
                break; // we have what we need, so we're done
            }
        }

        return company;
    }

    public CompanyDTO getTestIntuitCompany_Removed1Contact() {
        CompanyDTO company = getTestIntuitCompany();

        ContactDTO oldContacts[] = new ContactDTO[company.getContacts().size()];

        // copy existing contact list into local array
        company.getContacts().toArray(oldContacts);

        // clear old list of contacts
        company.getContacts().clear();

        // re-add all but last contact
        for (int i = 0; i < (oldContacts.length - 1); ++i) {
            company.getContacts().add(oldContacts[i]);
        }

        return company;
    }

    public CompanyDTO getTestIntuitCompany_Changed1Contact() {
        CompanyDTO company = getTestIntuitCompany();

        ContactDTO contact = company.getContacts().iterator().next();
        contact.setEmail("ChangedContactEmailForTestComp@aol.com");
        contact.setSecondPhoneNumber("(775) 999-9999");

        return company;
    }

    private AddressDTO getTestLegalAddress() {
        AddressDTO legalAddress = new AddressDTO();

        legalAddress.setAddressLine1("6888 Sierra Cnt Pkwy");
        legalAddress.setCity("Reno");
        legalAddress.setZipCode("89511");
        legalAddress.setState("NV");

        return legalAddress;
    }

    private AddressDTO getTestMailingAddress() {
        AddressDTO mailingAddress = new AddressDTO();

        mailingAddress.setAddressLine1("6887 Sierra Center Parkway");
        mailingAddress.setAddressLine2("Suite 45");
        mailingAddress.setAddressLine3("test line 3");
        mailingAddress.setCity("Reno");
        mailingAddress.setZipCode("89521");
        mailingAddress.setState("NV");

        return mailingAddress;
    }

    public Collection<ContactDTO> getTestContacts() {
        Collection<ContactDTO> contacts = new ArrayList<ContactDTO>();

        // set up common contact address
        AddressDTO contactAddr = new AddressDTO();
        contactAddr.setAddressLine1("123 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");

        // set up primary principal contact
        ContactDTO contact = new ContactDTO();
        contact.setFirstName("Mike");
        contact.setMiddleName("P");
        contact.setLastName("PrimaryPrincipal2");
        contact.setPhoneNumber("(775) 561-1111");
        contact.setContactRoleCd(ContactRole.PrimaryPrincipal);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("PrimaryPrincipal2@email.com");
        contact.setAddress(contactAddr);
        contact.setContactId(generateContactKey(contact));

        contacts.add(contact);

        // set up secondary principal contact
        contact = new ContactDTO();
        contact.setFirstName("Mike");
        contact.setMiddleName("P");
        contact.setLastName("SecondaryPrincipal2");
        contact.setPhoneNumber("(775) 672-2222");
        contact.setContactRoleCd(ContactRole.SecondaryPrincipal);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("SecondaryPrincipal2@email.com");
        contact.setAddress(contactAddr);
        contact.setContactId(generateContactKey(contact));

        contacts.add(contact);

        // set up payroll admin contact
        contact = new ContactDTO();
        contact.setFirstName("Steve");
        contact.setMiddleName("P");
        contact.setLastName("PayrollAdmin2");
        contact.setPhoneNumber("(775) 493-3333");
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAccountSignatory(Boolean.FALSE);
        contact.setEmail("PayrollAdmin2@email.com");
        contact.setAddress(contactAddr);
        contact.setContactId(generateContactKey(contact));

        contacts.add(contact);

        // set up 'other' contact
        contact = new ContactDTO();
        contact.setFirstName("Mike");
        contact.setMiddleName("P");
        contact.setLastName("Other2");
        contact.setPhoneNumber("(775) 894-4444");
        contact.setContactRoleCd(ContactRole.Other);
        contact.setAccountSignatory(Boolean.FALSE);
        contact.setEmail("Other@email.com");
        contact.setAddress(contactAddr);
        contact.setContactId(generateContactKey(contact));

        contacts.add(contact);

        return contacts;
    }

    public Collection<ContactDTO> getTestContactsPhoneNumberChanged() {
        Collection<ContactDTO> contacts = new ArrayList<ContactDTO>();

        // set up common contact address
        AddressDTO contactAddr = new AddressDTO();
        contactAddr.setAddressLine1("123 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");

        // set up primary principal contact
        ContactDTO contact = new ContactDTO();
        contact.setFirstName("Mike");
        contact.setMiddleName("P");
        contact.setLastName("PrimaryPrincipal3");
        contact.setPhoneNumber("(775) 561-1111");
        contact.setContactRoleCd(ContactRole.PrimaryPrincipal);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("PrimaryPrincipal3@email.com");
        contact.setAddress(contactAddr);
        contact.setContactId(generateContactKey(contact));

        contacts.add(contact);

        return contacts;
    }

    public CompanyDTO getTestIntuitCompany2() {
        CompanyDTO company = new CompanyDTO();

        company.setDBA("Intuit2_update");
        company.setFein("222222222");
        company.setLegalAddress(getTestLegalAddress2());
        company.setLegalName("Intuit2_update");
        company.setMailingAddress(getTestMailingAddress2());
        company.setNotificationEmail("notifications2upd@intuit.com");
        company.setCompanyId("1234562");
        company.setSourceSystemCd(srcSystemCodeForNewCompany);
        company.setContacts(getTestContacts());
        company.setPriceType("Standard");

        return company;
    }

    public CompanyDTO getTestIntuitCompany_NoAccountSignatory() {
        CompanyDTO company = getTestIntuitCompany();

        // clear account signatory flag from all contacts
        for (ContactDTO contact : company.getContacts()) {
            contact.setAccountSignatory(Boolean.FALSE);
        }

        return company;
    }

    public CompanyDTO getTestActiveCompany(String fein, String psid) {
        CompanyDTO company = new CompanyDTO();

        company.setDBA("Dreams Come True");
        company.setFein(fein);
        company.setLegalAddress(getTestLegalAddress2());
        company.setLegalName("Dreams Come True, Inc");
        company.setMailingAddress(getTestMailingAddress2());
        company.setNotificationEmail("notifications@dmcinc.com");
        company.setCompanyId(psid);
        company.setSourceSystemCd(srcSystemCodeForNewCompany);
        company.setContacts(getTestContacts());
        company.setNextEmployeeId("1");
        company.setNextPaycheckId("1");
        company.setNextPayrollItemId("1");
        company.setNextPayrollTransactionId("1");
        company.setCurrentToken(1L);
        company.setPriceType("Standard");

        return company;
    }

    public CompanyDTO getTestActiveCompany() {
        return getTestActiveCompany("222222223", "123272727");
    }

    public CompanyDTO getTestActiveCompany1() {
        return getTestActiveCompany("222222225", "123272728");
    }

    public CompanyDTO getTestActiveCompany123123123() {
        CompanyDTO company = new CompanyDTO();

        company.setDBA("Test Company");
        company.setFein("222222222");
        company.setLegalAddress(getTestLegalAddress3());
        company.setLegalName("Test Company, Inc");
        company.setMailingAddress(getTestMailingAddress3());
        company.setNotificationEmail("mail@testcompany.com");
        company.setCompanyId("123123123");
        company.setSourceSystemCd(srcSystemCodeForNewCompany);
        company.setContacts(getTestContacts());
        company.setPriceType("Standard");

        return company;
    }

    private AddressDTO getTestLegalAddress2() {
        AddressDTO legalAddress = new AddressDTO();

        legalAddress.setAddressLine1("6888 Sierra Cnt Pkwy2upd");
        legalAddress.setCity("Reno2");
        legalAddress.setZipCode("89512");
        legalAddress.setState("NE");
        legalAddress.setCountry("US");

        return legalAddress;
    }

    private AddressDTO getTestMailingAddress2() {
        AddressDTO mailingAddress = new AddressDTO();

        mailingAddress.setAddressLine1("6887 Sierra Center Parkway2upd");
        mailingAddress.setAddressLine2("Suite 452");
        mailingAddress.setAddressLine3("test line 2");
        mailingAddress.setCity("Reno3");
        mailingAddress.setZipCode("89513");
        mailingAddress.setState("NM");

        return mailingAddress;
    }

    private AddressDTO getTestLegalAddress3() {
        AddressDTO legalAddress = new AddressDTO();

        legalAddress.setAddressLine1("6888 Sierra Cnt Pkwy3upd");
        legalAddress.setCity("Reno");
        legalAddress.setZipCode("89511");
        legalAddress.setState("NV");

        return legalAddress;
    }

    private AddressDTO getTestMailingAddress3() {
        AddressDTO mailingAddress = new AddressDTO();

        mailingAddress.setAddressLine1("6888 Sierra Center Parkway3upd");
        mailingAddress.setAddressLine2("Suite 123");
        mailingAddress.setAddressLine3("test line 3");
        mailingAddress.setCity("Reno");
        mailingAddress.setZipCode("89511");
        mailingAddress.setState("NV");

        return mailingAddress;
    }

    public CompanyBankAccountDTO getTestCompanyBankAccount2() {
        // Create Bank Account
        BankAccountDTO bankAccount = new BankAccountDTO();
        bankAccount.setAccountNumber("123123");
        bankAccount.setAccountType(BankAccountType.Checking);
        bankAccount.setBankName("Bank of America");
        bankAccount.setRoutingNumber("263182914");

        // Create Company Bank Account
        CompanyBankAccountDTO companyBankAccount = new CompanyBankAccountDTO();
        companyBankAccount.setBankAccountDTO(bankAccount);
        companyBankAccount.setCompanyBankAccountID("1231232");

        return companyBankAccount;
    }

    public CompanyBankAccountDTO getTestCompanyBankAccount() {
        // Create Bank Account
        BankAccountDTO bankAccount = new BankAccountDTO();
        bankAccount.setAccountNumber("123098");
        bankAccount.setAccountType(BankAccountType.Checking);
        bankAccount.setBankName("Bank of America");
        bankAccount.setRoutingNumber("263182914");

        // Create Company Bank Account
        CompanyBankAccountDTO companyBankAccount = new CompanyBankAccountDTO();
        companyBankAccount.setBankAccountDTO(bankAccount);
        companyBankAccount.setCompanyBankAccountID("123123");
        companyBankAccount.setSourceBankAccountName("BofA");

        return companyBankAccount;
    }

    public CompanyBankAccountDTO getTestCompany123123123BankAccount() {
        // Create Bank Account
        BankAccountDTO bankAccount = new BankAccountDTO();
        bankAccount.setAccountNumber("123123");
        bankAccount.setAccountType(BankAccountType.Checking);
        bankAccount.setBankName("Bank of America");
        bankAccount.setRoutingNumber("263182914");

        // Create Company Bank Account
        CompanyBankAccountDTO companyBankAccount = new CompanyBankAccountDTO();
        companyBankAccount.setBankAccountDTO(bankAccount);
        companyBankAccount.setCompanyBankAccountID("123123");

        return companyBankAccount;
    }

    public DDServiceInfoDTO getTestCompanyService() {
        DDServiceInfoDTO ddCompanyService = new DDServiceInfoDTO();

        ddCompanyService.setServiceCode(ServiceCode.DirectDeposit);

        BigDecimal avgPayrollRunAmount = new BigDecimal("150.00");
        ddCompanyService.setAveragePayrollAmount(avgPayrollRunAmount);

        BigDecimal highPayrollRunAmount = new BigDecimal("250.00");
        ddCompanyService.setHighAnnualPayrollAmount(highPayrollRunAmount);

        return ddCompanyService;
    }

    public ServiceInfoDTO getCloudCompanyService() {
        ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();

        serviceInfoDTO.setServiceCode(ServiceCode.Cloud);

        return serviceInfoDTO;
    }

    public ServiceInfoDTO getTaxCompanyService() {
        TaxServiceInfoDTO taxServiceInfoDTO = new TaxServiceInfoDTO();

        taxServiceInfoDTO.setServiceCode(ServiceCode.Tax);

        return taxServiceInfoDTO;
    }

    public ThirdParty401kServiceInfoDTO get401kCompanyService() {
        ThirdParty401kServiceInfoDTO thirdParty401kServiceInfoDTO = new ThirdParty401kServiceInfoDTO();

        thirdParty401kServiceInfoDTO.setServiceCode(ServiceCode.ThirdParty401k);

        thirdParty401kServiceInfoDTO.setCustodialId("123456");
        thirdParty401kServiceInfoDTO.setHasSafeHarbor(true);

        return thirdParty401kServiceInfoDTO;
    }

    public CheckDistributionServiceInfoDTO getTestCheckDistributionCompanyService(long lastPaycheckId) {
        CheckDistributionServiceInfoDTO companyService = new CheckDistributionServiceInfoDTO();

        companyService.setServiceCode(ServiceCode.CheckDistribution);
        companyService.setLastPaycheckId(lastPaycheckId);

        return companyService;
    }

    public ServiceInfoDTO getTestCompanyBillPaymentService() {
        ServiceInfoDTO billPaymentService = new ServiceInfoDTO();

        billPaymentService.setServiceCode(ServiceCode.BillPayment);
        FundingModel fundingModel = Application.findById(FundingModel.class, FundingModel.Codes.TWO_DAY);
        billPaymentService.setFundingModel(fundingModel);
        return billPaymentService;
    }


    public static String generateContactKey(ContactDTO pContact) {
        String key = null;

        String contactRoleCode = pContact.getContactRoleCd().toString();
        contactRoleCode = (contactRoleCode == null) ? "" : contactRoleCode;

        key = contactRoleCode;

        String firstName = pContact.getFirstName();
        firstName = (firstName == null) ? "" : firstName;
        key += firstName;

        String lastName = pContact.getLastName();
        lastName = (lastName == null) ? "" : lastName;
        key += lastName;

        String middleName = pContact.getMiddleName();
        middleName = (middleName == null) ? "" : middleName;
        key += middleName;

        return key;
    }

    public Company persistTestActiveCompanyWithValidAddress() {
        return persistCompany(getTestActiveCompanyWithValidAddress());
    }

    public Company persistTestActiveCompany2WithValidAddress() {
        return persistCompany(getTestActiveCompany2WithValidAddress());
    }

    public CompanyDTO getTestActiveCompanyWithValidAddress() {
        CompanyDTO company = new CompanyDTO();

        company.setDBA("Dreams Come True");
        company.setFein("222222223");
        company.setLegalAddress(getTestLegalAddress1());
        company.setLegalName("Dreams Come True, Inc");
        company.setMailingAddress(getTestMailingAddress2());
        company.setNotificationEmail("notifications@dmcinc.com");
        company.setCompanyId("123272727");
        company.setSourceSystemCd(srcSystemCodeForNewCompany);
        company.setContacts(getTestContacts());
        company.setNextEmployeeId("1");
        company.setNextPaycheckId("1");
        company.setNextPayrollTransactionId("1");
        company.setNextPayrollItemId("1");
        company.setCurrentToken(1L);
        company.setPriceType("Standard");

        return company;
    }

    public CompanyDTO getTestActiveCompany2WithValidAddress() {
        CompanyDTO company = new CompanyDTO();

        company.setDBA("Dreams Come True2");
        company.setFein("222222233");
        company.setLegalAddress(getTestLegalAddress2());
        company.setLegalName("Dreams Come True2, Inc");
        company.setMailingAddress(getTestMailingAddress());
        company.setNotificationEmail("notifications@dmcinc.com");
        company.setCompanyId("1234567");
        company.setSourceSystemCd(srcSystemCodeForNewCompany);
        company.setContacts(getTestContacts());
        company.setNextEmployeeId("1");
        company.setNextPaycheckId("1");
        company.setNextPayrollTransactionId("1");
        company.setNextPayrollItemId("1");
        company.setCurrentToken(1L);
        company.setPriceType("Standard");

        return company;
    }

    private AddressDTO getTestLegalAddress1() {
        AddressDTO legalAddress = new AddressDTO();

        legalAddress.setAddressLine1("13433 Wyoming Valley");
        legalAddress.setCity("Austin");
        legalAddress.setZipCode("78727");
        legalAddress.setCountry("US");
        legalAddress.setState("TX");

        return legalAddress;
    }

    public void addCloudService(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        ServiceInfoDTO cloudServiceInfoDTO = getCloudCompanyService();

        ProcessResult<CompanyService> cloudServiceAddProcessResult = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), cloudServiceInfoDTO);

        CompanyService companyService = cloudServiceAddProcessResult.getResult();
        companyService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        CompanyService serviceInfo = CompanyService.findCompanyService(foundCompany, ServiceCode.Cloud);

        assertNotNull(serviceInfo);

        assertEquals("Service code", ServiceCode.Cloud, serviceInfo.getService().getServiceCd());
        PayrollServices.rollbackUnitOfWork();
    }

    public void addTaxService(Company pCompany) {
        DataLoadServices.addTaxService(pCompany);
        DataLoadServices.activateTaxService(pCompany);
    }

    public void addDDService(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        ServiceInfoDTO ddServiceInfoDTO = getTestCompanyService();

        ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), ddServiceInfoDTO);

        assertSuccess("Add dd service", ddServiceAddProcessResult);

        CompanyService companyService = ddServiceAddProcessResult.getResult();
        companyService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        CompanyService serviceInfo = CompanyService.findCompanyService(foundCompany, ServiceCode.DirectDeposit);

        assertNotNull(serviceInfo);

        assertEquals("Service code", ServiceCode.DirectDeposit, serviceInfo.getService().getServiceCd());
        PayrollServices.rollbackUnitOfWork();
    }

    public void add401kService(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        ServiceInfoDTO thirdParty401kServiceInfoDTO = get401kCompanyService();

        ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), thirdParty401kServiceInfoDTO);

        assertSuccess("Add 401k service", ddServiceAddProcessResult);

        CompanyService companyService = ddServiceAddProcessResult.getResult();
        companyService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        CompanyService serviceInfo = CompanyService.findCompanyService(foundCompany, ServiceCode.ThirdParty401k);

        assertNotNull(serviceInfo);

        assertEquals("Service code", ServiceCode.ThirdParty401k, serviceInfo.getService().getServiceCd());
        PayrollServices.rollbackUnitOfWork();
    }

    public void addBPService(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        ServiceInfoDTO serviceInfoDTO = getTestCompanyBillPaymentService();

        ProcessResult<CompanyService> bpServiceAddProcessResult = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), serviceInfoDTO);

        assertSuccess("Add bill payment service", bpServiceAddProcessResult);

        CompanyService companyService = bpServiceAddProcessResult.getResult();
        companyService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        CompanyService serviceInfo = CompanyService.findCompanyService(foundCompany, ServiceCode.BillPayment);

        assertNotNull(serviceInfo);

        assertEquals("Service code", ServiceCode.BillPayment, serviceInfo.getService().getServiceCd());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Method to retrieve existing agency support start dates from company agency.
     * This will have to be called before being executed of testcase.
     *
     * @return PaymentTemplate Dates mapped to Payment Template Cds
     */
    public static Map getExistingPaymentTemplateSupportStartDates() {
        Map<String, SpcfCalendar> paymentTemplateDates = new HashMap();

        DomainEntitySet<PaymentTemplate> allPaymentTemplates = Application.findObjects(PaymentTemplate.class);

        for (PaymentTemplate paymentTemplate : allPaymentTemplates) {
            paymentTemplateDates.put(paymentTemplate.getPaymentTemplateCd(), paymentTemplate.getSupportStartDate());
        }
        return paymentTemplateDates;
    }

    /**
     * Method for setting up the min date as support start date on payment template.
     */
    public static void setPaymentTemplateSupportDateToStartDateToMin() {
        DomainEntitySet<PaymentTemplate> allPaymentTemplates = Application.findObjects(PaymentTemplate.class);

        // Set Payment Template support start date to MIN
        for (PaymentTemplate paymentTemplate : allPaymentTemplates) {
            paymentTemplate.setSupportStartDate(SpcfCalendar.MinDate);
        }
    }

    /**
     * Method for resetting the support start date back to original date
     * This will have to be called once the particular test case is executed.
     *
     * @param pPaymentTemplateDates
     */
    public static void resetPaymentTemplateSupportDateToOriginal(HashMap<String, SpcfCalendar> pPaymentTemplateDates) {
        DomainEntitySet<PaymentTemplate> allPaymentTemplates = Application.findObjects(PaymentTemplate.class);
        for (PaymentTemplate paymentTemplate : allPaymentTemplates) {
            paymentTemplate.setSupportStartDate(pPaymentTemplateDates.get(paymentTemplate.getPaymentTemplateCd()));
        }
    }

    public static PaymentTemplate updatePaymentTemplateSupportedDate(String pPaymentTemplateCd, SpcfCalendar pSupportedDate) {
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, pPaymentTemplateCd);
        paymentTemplate.setSupportStartDate(pSupportedDate);
        return paymentTemplate;
    }

    public CompanyDTO getTestIntuitCompany_addSsnAndDob(String ssn,SpcfCalendar dateOfBirth){
        CompanyDTO companyDTO = getTestIntuitCompany();

        for(ContactDTO contact: companyDTO.getContacts()){
            if(contact.getContactRoleCd() == ContactRole.PrimaryPrincipal){
                contact.setSocialSecurityNumber(ssn);
                contact.setDateOfBirth(new DateDTO(dateOfBirth));
                break;
            }

        }
        return companyDTO;
    }

    public CompanyDTO getTestIntuitCompany_addIndustry(String industry){
        CompanyDTO companyDTO = getTestIntuitCompany();
        CompanyAdditionalInfoDTO companyAdditionalInfoDTO = new CompanyAdditionalInfoDTO();
        companyAdditionalInfoDTO.setIndustry(industry);
        companyDTO.setCompanyAdditionalInfo(companyAdditionalInfoDTO);

        return companyDTO;
    }

    public CompanyDTO getTestIntuitCompany_addOwnership(String ownership){
        CompanyDTO companyDTO = getTestIntuitCompany();
        CompanyAdditionalInfoDTO companyAdditionalInfoDTO = new CompanyAdditionalInfoDTO();
        companyAdditionalInfoDTO.setOwnership(ownership);
        companyDTO.setCompanyAdditionalInfo(companyAdditionalInfoDTO);

        return companyDTO;
    }
    
	public CompanyAdjustmentSubmissionDTO createCompanyAdjustmentSubmissionDTO(Company pCompany,
			DateDTO pSubmissionDate, SpcfCalendar pPeriodEndDate) {

		CompanyAdjustmentSubmissionDTO adjustmentSubmissionDTO = DataLoadServices
				.createCompanyAdjustmentSubmissionDTO("29", pSubmissionDate);
		QBDTTransactionInfoDTO qbdtTransactionInfoDTO = DataLoadServices.createQBDTTransactionInfoDTO("29");
		QBDTPayrollTransactionLineDTO qbdtPayrollTransactionLineDTO = DataLoadServices
				.createQBDTPayrollTransactionLineDTO(new SpcfMoney("200.00"), new SpcfMoney("200.00"),
						new SpcfMoney("200.00"), "18");

		QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = DataLoadServices
				.createQBDTPayrollTransactionDTO(getEmployee(pCompany).getSourceEmployeeId(), pPeriodEndDate);

		qbdtPayrollTransactionDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
		qbdtPayrollTransactionLineDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
		adjustmentSubmissionDTO.setQBDTPayrollTransactionDTO(qbdtPayrollTransactionDTO);
		adjustmentSubmissionDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);

		return adjustmentSubmissionDTO;
	}

	public Company createAssistedCompany(String pPSID) {
		Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, pPSID, true, ServiceCode.Cloud,
				ServiceCode.DirectDeposit);
		DataLoadServices.addEEs(company, 3, true, true, true);
		DataLoadServices.addTaxService(company);
		return company;
	}

	private Employee getEmployee(Company pCompany) {
		Employee employee = null;
		DomainEntitySet<Employee> employees = pCompany.getEmployees();
		if (employees != null && employees.size() > 0) {
			employee = employees.get(0);
		}
		return employee;
	}

}
