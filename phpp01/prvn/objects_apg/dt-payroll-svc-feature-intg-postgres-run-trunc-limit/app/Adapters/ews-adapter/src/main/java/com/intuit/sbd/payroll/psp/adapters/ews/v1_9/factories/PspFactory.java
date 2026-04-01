package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsAdapterConst;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsBankAccountType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsDeliveryType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsPaymentMethod;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jeff Jones
 */
public class PspFactory {
    private static final SpcfLogger logger;
    protected static final Pattern zipCodePattern;

    static {
        zipCodePattern = Pattern.compile("((\\d){5})(\\-)?((\\d){4})?$");
        logger = PayrollServices.getLogger(PspFactory.class);
    }

    public static Company findCompany(String pPSID) throws Exception {
        Company company = Company.findCompany(pPSID, SourceSystemCode.QBDT);

        if (company == null) {
            throw new EwsException(EwsMessages.psidDoesNotExistError());
        }

        return company;
    }

    public static DomainEntitySet<Company> findCompaniesByEin(String pEIN) throws Exception {
        Criterion<Company> companyCriterion = null;
        if(pEIN == null){
            companyCriterion = Company.FedTaxIdEnc().isNull();
        } else {
            List<String> einEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pEIN);
            companyCriterion = Company.FedTaxIdEnc().in(einEncList);
        }

        companyCriterion = companyCriterion.And(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));

        //Get Termed Accounts
        Expression<Company> query =
                new Query<Company>()
                        .Where(companyCriterion)
                        .OrderBy(Company.SignUpDate().Descending());

        return Application.find(Company.class, query);
    }

    public static Company findCompanyByEin(String pEIN, String pSubscriptionNumber) throws Exception {
        Criterion<EntitlementUnit> entitlementUnitCriterion = EntitlementUnit.Entitlement().SubscriptionNumber().equalTo(pSubscriptionNumber);
        if(pEIN == null){
            entitlementUnitCriterion = entitlementUnitCriterion.And(EntitlementUnit.FedTaxIdEnc().isNull());
        } else {
            List<String> einEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntitlementUnit.FedTaxIdKeyName, pEIN);
            entitlementUnitCriterion = entitlementUnitCriterion.And(EntitlementUnit.FedTaxIdEnc().in(einEncList));
        }

        if(!AuthUser.hasSAPAdminAccess()) {
            entitlementUnitCriterion = entitlementUnitCriterion.And(EntitlementUnit.Company().IsDgDisassociated().equalTo(Boolean.FALSE));
        }

        Expression<EntitlementUnit> query =
                new Query<EntitlementUnit>()
                        .Where(entitlementUnitCriterion);
        DomainEntitySet<EntitlementUnit> entitlementUnits = Application.find(EntitlementUnit.class, query);

        SpcfUniqueId entitlementId = null;
        List<EntitlementUnit> activeEntitlementUnit = new ArrayList<EntitlementUnit>();
        List<EntitlementUnit> inactiveEntitlementUnit = new ArrayList<EntitlementUnit>();
        for (EntitlementUnit entitlementUnit : entitlementUnits) {
            if (entitlementId == null) {
                entitlementId = entitlementUnit.getEntitlement().getId();
            } else {
                if (!entitlementId.equals(entitlementUnit.getEntitlement().getId())) {
                    throw new EwsException(EwsMessages.duplicateEntitlementsFound());
                }
            }

            if (entitlementUnit.isActivated()) {
                activeEntitlementUnit.add(entitlementUnit);
            } else {
                inactiveEntitlementUnit.add(entitlementUnit);
            }
        }

        if (!activeEntitlementUnit.isEmpty()) {
            if (activeEntitlementUnit.size() > 1) {
                throw new EwsException(EwsMessages.duplicateActiveEntitlementUnitsFound());
            }
            return activeEntitlementUnit.get(0).getCompany();
        }

        if (!inactiveEntitlementUnit.isEmpty()) {
            if (inactiveEntitlementUnit.size() > 1) {
                throw new EwsException(EwsMessages.nonUniqueEntitlementUnitFound());
            }
            return inactiveEntitlementUnit.get(0).getCompany();
        }

        throw new EwsException(EwsMessages.einDoesNotExistError());
    }

    public static Collection<Company> findCompanyByEinAndAuthId(String pEIN, String pAuthId) throws Exception {
        Criterion<Contact> contactCriterion = Contact.IAMAuthenticationId().equalTo(pAuthId)
                .And(Contact.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT));

        if(pEIN == null){
            contactCriterion = contactCriterion.And(Contact.Company().FedTaxIdEnc().isNull());
        } else {
            List<String> einEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pEIN);
            contactCriterion = contactCriterion.And(Contact.Company().FedTaxIdEnc().in(einEncList));
        }
        Expression<Contact> query =
                new Query<Contact>()
                        .Where(contactCriterion)
                        .OrderBy(Contact.Company().SignUpDate().Descending());

        DomainEntitySet<Contact> contacts = Application.find(Contact.class, query);

        if (contacts.isEmpty()) {
            throw new EwsException(EwsMessages.einDoesNotExistError());
        }

        Map<String, Company> responseMap = new HashMap<String, Company>();
        for (Contact contact : contacts) {
            Company company = contact.getCompany();
            if (!responseMap.containsKey(company.getSourceCompanyId())) {
                responseMap.put(company.getSourceCompanyId(), company);
            }
        }

        return responseMap.values();
    }

    public static Company findCompanyByRealmId(String pRealmId) throws Exception {
        Expression<Company> query =
                new Query<Company>()
                        .Where(Company.IAMRealmId().equalTo(pRealmId)
                                .And(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)))
                        .OrderBy(Company.SignUpDate().Descending())
                        .LimitResults(0, 1);

        DomainEntitySet<Company> companies = Application.find(Company.class, query);

        if (companies.isEmpty()) {
            throw new EwsException(EwsMessages.realmIdDoesNotExists());
        }

        return companies.get(0);
    }

    public static Company findCompany(String pEIN, ServiceSubStatusCode pServiceSubStatusCode) throws Exception {
        Criterion<CompanyService> companyServiceCriterion = CompanyService.StatusCd().equalTo(pServiceSubStatusCode);

        if(pEIN == null){
            companyServiceCriterion = companyServiceCriterion.And(CompanyService.Company().FedTaxIdEnc().isNull());
        } else {
            List<String> einEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pEIN);
            companyServiceCriterion = companyServiceCriterion.And(CompanyService.Company().FedTaxIdEnc().in(einEncList));
        }

        if(!AuthUser.hasSAPAdminAccess()) {
            companyServiceCriterion = companyServiceCriterion.And(CompanyService.Company().IsDgDisassociated().equalTo(Boolean.FALSE));
        }

        companyServiceCriterion = companyServiceCriterion.And(CompanyService.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT));

        Expression<CompanyService> query =
                new Query<CompanyService>()
                        .Where(companyServiceCriterion);
        DomainEntitySet<CompanyService> companyServices = Application.find(CompanyService.class, query);

        return companyServices.isEmpty() ? null : companyServices.get(0).getCompany();
    }

    /**
     * @param pCompany
     * @return
     * @throws Exception
     */
    public static CompanyBankAccount findCompanyBankAccount(Company pCompany) throws Exception {
        CompanyBankAccount companyBankAccount = findCompanyBankAccount(pCompany, BankAccountStatus.PendingVerification);
        if (companyBankAccount == null) {
            companyBankAccount = findCompanyBankAccount(pCompany, BankAccountStatus.Active);
            if (companyBankAccount == null) {
                companyBankAccount = findCompanyBankAccount(pCompany, BankAccountStatus.Inactive);
                if (companyBankAccount == null) {
                    throw new EwsException(EwsMessages.noBankAccount());
                }
            }
        }

        return companyBankAccount;
    }


    /**
     * Returns the most recent company bank account for a particular
     * given status
     *
     * @param pCompany
     * @param pStatus
     * @return
     */
    private static CompanyBankAccount findCompanyBankAccount(Company pCompany, BankAccountStatus pStatus) {
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = pCompany.getCompanyBankAccountCollection()
                                                                          .find(CompanyBankAccount.StatusCd().equalTo(pStatus)
                                .And(CompanyBankAccount.StatusCd().equalTo(pStatus)))
                                                                          .sort(CompanyBankAccount.StatusEffectiveDate().Descending());

        return companyBankAccounts.isEmpty() ? null : companyBankAccounts.getFirst();
    }

    public static CompanyOffering findCompanyOffering(Company pCompany, OfferingCode pOfferingCode) {
        Expression<CompanyOffering> query =
                new Query<CompanyOffering>()
                        .Where(CompanyOffering.Company().equalTo(pCompany)
                                .And(CompanyOffering.Offering().OfferingCode().equalTo(pOfferingCode)));

        DomainEntitySet<CompanyOffering> companyOfferings = Application.find(CompanyOffering.class, query);

        return companyOfferings.isEmpty() ? null : companyOfferings.get(0);
    }

    public static FinancialTransaction findFinancialTransaction(Company pCompany,
                                                                TransactionTypeCode pTransactionTypeCode) {
        Expression<FinancialTransaction> query =
                        new Query<FinancialTransaction>()
                                .Where(FinancialTransaction.Company().equalTo(pCompany)
                                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(pTransactionTypeCode)))
                                .OrderBy(FinancialTransaction.CreatedDate().Descending());
        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, query);

        return financialTransactions.isEmpty() ? null : financialTransactions.get(0);
    }

    public static FinancialTransaction findFinancialTransactionExcludingType(CompanyBankAccount pCompanyBankAccount,
                                                                 TransactionTypeCode pTransactionTypeCode,
                                                                 TransactionStateCode pTransactionStateCode) {

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinancialTransactionsExcludedType(
                pCompanyBankAccount.getCompany().getSourceSystemCd(), pCompanyBankAccount.getCompany().getSourceCompanyId(),
                pCompanyBankAccount.getBankAccount(), pTransactionTypeCode, pTransactionStateCode);

        return financialTransactions.isEmpty() ? null : financialTransactions.get(0);
    }

    public static DomainEntitySet<EntitlementUnit> findEntitlementUnitsBySubscriptionNumber(String pSubscriptionNumber) throws Exception {
        int i = 0;
        String[] paramNames = new String[1];
        paramNames[i++] = "subNum";

        i = 0;
        Object[] paramValues = new Object[1];
        paramValues[i++] = pSubscriptionNumber;

        DomainEntitySet<EntitlementUnit> entitlementUnits =
                Application.findByNamedQuery("findDistinctEntitlementUnitsBySubscriptionNumber", paramNames, paramValues);

        if (entitlementUnits.isEmpty()) {
            throw new EwsException(EwsMessages.subscriptionNumberDoesNotExistError());
        }

        return entitlementUnits;
    }

    public static CompanyDTO createCompanyDTO(EwsCompany pEwsCompany) throws Exception {
        CompanyDTO companyDTO = new CompanyDTO();

        companyDTO.setSourceSystemCd(SourceSystemCode.QBDT);
        companyDTO.setCompanyId(PayrollServices.companyManager.createSourceCompanyId(SourceSystemCode.QBDT));
        companyDTO.setFein(pEwsCompany.getEin());

        ContactDTO contactDTO;
        if (pEwsCompany.getPayrollAdmin() != null) {
            contactDTO = createContactDTO(pEwsCompany.getPayrollAdmin(), ContactRole.PayrollAdmin);
            companyDTO.getContacts().add(contactDTO);
            companyDTO.setNotificationEmail(contactDTO.getEmail());
        }

        if (pEwsCompany.getPrimaryPrincipal() != null) {
            contactDTO = createContactDTO(pEwsCompany.getPrimaryPrincipal(), ContactRole.PrimaryPrincipal);
            companyDTO.getContacts().add(contactDTO);
            companyDTO.setNotificationEmail(contactDTO.getEmail());
        }

        if (pEwsCompany.getSecondaryPrincipal() != null) {
            contactDTO = createContactDTO(pEwsCompany.getSecondaryPrincipal(), ContactRole.SecondaryPrincipal);
            companyDTO.getContacts().add(contactDTO);
        }

        companyDTO.setQuickBooksInfo(createQuickbooksInfoDTO(pEwsCompany.getQuickBooks()));

        if (pEwsCompany.getLegalInfo() != null) {
            companyDTO.setLegalName(pEwsCompany.getLegalInfo().getLegalName());
            companyDTO.setLegalAddress(createAddressDTO(pEwsCompany.getLegalInfo()));
        } else {
            companyDTO.setLegalName(pEwsCompany.getDba());
            companyDTO.setLegalAddress(createAddressDTO(pEwsCompany.getMailingAddress()));
        }

        companyDTO.setDBA(pEwsCompany.getDba());

        if (pEwsCompany.getMailingAddress() != null) {
            companyDTO.setMailingAddress(createAddressDTO(pEwsCompany.getMailingAddress()));
        } else {
            companyDTO.setMailingAddress(createAddressDTO(pEwsCompany.getLegalInfo()));
        }

        //Set Next Id's for QBDT Payroll
        Company company = findCompany(pEwsCompany.getEin(), ServiceSubStatusCode.Cancelled);
        if (company == null) {
            companyDTO.setCurrentToken(1L);
            companyDTO.setNextEmployeeId("1");
            companyDTO.setNextPaycheckId("1");
            companyDTO.setNextPayrollItemId("1");
            companyDTO.setNextPayrollTransactionId("1");
        } else {
            companyDTO.setCurrentToken(company.getCurrentToken());
            companyDTO.setNextEmployeeId(company.getNextEmployeeId());
            companyDTO.setNextPaycheckId(company.getNextPaycheckId());
            companyDTO.setNextPayrollItemId(company.getNextPayrollItemId());
            companyDTO.setNextPayrollTransactionId(company.getNextPayrollTransactionId());

            if (companyDTO.getCurrentToken() == 0L) {
                companyDTO.setCurrentToken(1L);
            }

            if (companyDTO.getNextEmployeeId() == null || companyDTO.getNextEmployeeId().equals("")) {
                companyDTO.setNextEmployeeId("1");
            }

            if (companyDTO.getNextPaycheckId() == null || companyDTO.getNextPaycheckId().equals("")) {
                companyDTO.setNextPaycheckId("1");
            }

            if (companyDTO.getNextPayrollItemId() == null || companyDTO.getNextPayrollItemId().equals("")) {
                companyDTO.setNextPayrollItemId("1");
            }

            if (companyDTO.getNextPayrollTransactionId() == null || companyDTO.getNextPayrollTransactionId().equals("")) {
                companyDTO.setNextPayrollTransactionId("1");
            }
        }

        companyDTO.setIAMRealmId(pEwsCompany.getRealmId());
        companyDTO.setPriceType("Standard");

        return companyDTO;
    }

    public static CompanyDTO updateCompanyDTO(EwsCompany pEwsCompany, Company pCompany) throws Exception {
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(pCompany);

        companyDTO.setFein(pEwsCompany.getEin());

        //Update Payroll Admin
        if (pEwsCompany.getPayrollAdmin() != null) {
            ContactDTO contactDTO = findContactDTO(companyDTO.getContacts(), ContactRole.PayrollAdmin);
            if (contactDTO == null) {
                contactDTO = createContactDTO(pEwsCompany.getPayrollAdmin(), ContactRole.PayrollAdmin);
                companyDTO.getContacts().add(contactDTO);
                companyDTO.setNotificationEmail(contactDTO.getEmail());
            } else {
                contactDTO = updateContactDTO(pEwsCompany.getPayrollAdmin(), contactDTO, ContactRole.PayrollAdmin);
                companyDTO.setNotificationEmail(contactDTO.getEmail());
            }
        }

        //Update Primary Principal
        if (pEwsCompany.getPrimaryPrincipal() != null) {
            ContactDTO contactDTO = findContactDTO(companyDTO.getContacts(), ContactRole.PrimaryPrincipal);
            if (contactDTO == null) {
                contactDTO = createContactDTO(pEwsCompany.getPrimaryPrincipal(), ContactRole.PrimaryPrincipal);
                companyDTO.getContacts().add(contactDTO);
                companyDTO.setNotificationEmail(contactDTO.getEmail());
            } else {
                contactDTO = updateContactDTO(pEwsCompany.getPrimaryPrincipal(), contactDTO, ContactRole.PrimaryPrincipal);
                companyDTO.setNotificationEmail(contactDTO.getEmail());
            }
        }

        //Update Secondary Principal
        if (pEwsCompany.getSecondaryPrincipal() != null) {
            ContactDTO contactDTO = findContactDTO(companyDTO.getContacts(), ContactRole.SecondaryPrincipal);
            if (contactDTO == null) {
                contactDTO = createContactDTO(pEwsCompany.getSecondaryPrincipal(), ContactRole.SecondaryPrincipal);
                companyDTO.getContacts().add(contactDTO);
            } else {
                updateContactDTO(pEwsCompany.getSecondaryPrincipal(), contactDTO, ContactRole.SecondaryPrincipal);
            }
        }

        if (companyDTO.getQuickBooksInfo() == null && pEwsCompany.getQuickBooks() != null) {
            companyDTO.setQuickBooksInfo(new QuickbooksInfoDTO());
        }

        updateQuickbooksInfoDTO(pEwsCompany.getQuickBooks(), companyDTO.getQuickBooksInfo());

        if (pEwsCompany.getLegalInfo() != null) {
            companyDTO.setLegalName(pEwsCompany.getLegalInfo().getLegalName());
            companyDTO.setLegalAddress(createAddressDTO(pEwsCompany.getLegalInfo()));
        }

        if (pEwsCompany.getMailingAddress() == null) {
            companyDTO.setMailingAddress(createAddressDTO(pEwsCompany.getLegalInfo()));
        } else {
            if (companyDTO.getMailingAddress() == null && pEwsCompany.getMailingAddress() != null) {
                companyDTO.setMailingAddress(new AddressDTO());
            }
            updateAddressDTO(pEwsCompany.getMailingAddress(), companyDTO.getMailingAddress());
        }

        companyDTO.setDBA(pEwsCompany.getDba());
        companyDTO.setIAMRealmId(pEwsCompany.getRealmId());

        return companyDTO;
    }

    public static ContactDTO createContactDTO(EwsContact pEwsContact, ContactRole pContactRole) {
        ContactDTO contactDTO = new ContactDTO();

        String guid = SpcfUniqueId.generateRandomUniqueIdString();
        contactDTO.setContactId(guid);
        contactDTO.setTitle(pEwsContact.getTitle());
        contactDTO.setTitleSuffix(pEwsContact.getTitleSuffix());
        contactDTO.setFirstName(pEwsContact.getFirstName());
        contactDTO.setMiddleName(pEwsContact.getMiddleName());
        contactDTO.setLastName(pEwsContact.getLastName());
        contactDTO.setJobTitle(pEwsContact.getJobTitle());
        contactDTO.setEmail(pEwsContact.geteMail());
        contactDTO.setPhoneNumber(pEwsContact.getWorkPhone());
        contactDTO.setSecondPhoneNumber(pEwsContact.getHomePhone());
        contactDTO.setIAMAuthenticationId(pEwsContact.getAuthenticationId());

        switch (pContactRole) {
            case PayrollAdmin:
                contactDTO.setContactRoleCd(pContactRole);
                contactDTO.setAccountSignatory(false);
                break;
            case PrimaryPrincipal:
                contactDTO.setContactRoleCd(pContactRole);
                contactDTO.setAccountSignatory(true);
                break;
            case SecondaryPrincipal:
                contactDTO.setContactRoleCd(pContactRole);
                contactDTO.setAccountSignatory(true);
        }

        contactDTO.setAddress(createAddressDTO(pEwsContact.getAddress()));

        return contactDTO;
    }

    public static ContactDTO updateContactDTO(EwsContact pEwsContact, ContactDTO pContactDTO, ContactRole pContactRole) {
        pContactDTO.setTitle(pEwsContact.getTitle());
        pContactDTO.setTitleSuffix(pEwsContact.getTitleSuffix());
        pContactDTO.setFirstName(pEwsContact.getFirstName());
        pContactDTO.setMiddleName(pEwsContact.getMiddleName());
        pContactDTO.setLastName(pEwsContact.getLastName());
        pContactDTO.setJobTitle(pEwsContact.getJobTitle());
        pContactDTO.setEmail(pEwsContact.geteMail());
        pContactDTO.setPhoneNumber(pEwsContact.getWorkPhone());
        pContactDTO.setSecondPhoneNumber(pEwsContact.getHomePhone());
        pContactDTO.setIAMAuthenticationId(pEwsContact.getAuthenticationId());

        switch (pContactRole) {
            case PayrollAdmin:
                pContactDTO.setContactRoleCd(pContactRole);
                pContactDTO.setAccountSignatory(false);
                break;
            case PrimaryPrincipal:
                pContactDTO.setContactRoleCd(pContactRole);
                pContactDTO.setAccountSignatory(true);
                break;
            case SecondaryPrincipal:
                pContactDTO.setContactRoleCd(pContactRole);
                pContactDTO.setAccountSignatory(true);
        }

        if (pContactDTO.getAddress() == null && pEwsContact.getAddress() != null) {
            pContactDTO.setAddress(new AddressDTO());
        }

        updateAddressDTO(pEwsContact.getAddress(), pContactDTO.getAddress());

        return pContactDTO;
    }

    public static AddressDTO createAddressDTO(EwsAddress pEwsAddress) {
        if (pEwsAddress == null) {
            return null;
        }

        AddressDTO addressDTO = new AddressDTO();

        addressDTO.setAddressLine1(pEwsAddress.getAddressLine1());
        addressDTO.setAddressLine2(pEwsAddress.getAddressLine2());
        addressDTO.setCity(pEwsAddress.getCity());
        addressDTO.setState(pEwsAddress.getState());

        Matcher matcher = zipCodePattern.matcher(pEwsAddress.getZip());
        if (matcher.matches()) {
            addressDTO.setZipCode(matcher.group(1));
            addressDTO.setZipCodeExtension(matcher.group(4));
        }

        return addressDTO;
    }

    private static void updateAddressDTO(EwsAddress pEwsAddress, AddressDTO pAddressDTO) {
        if (pEwsAddress != null && pAddressDTO != null) {
            pAddressDTO.setAddressLine1(pEwsAddress.getAddressLine1());
            pAddressDTO.setAddressLine2(pEwsAddress.getAddressLine2());
            pAddressDTO.setCity(pEwsAddress.getCity());
            pAddressDTO.setState(pEwsAddress.getState());

            Matcher matcher = zipCodePattern.matcher(pEwsAddress.getZip());
            if (matcher.matches()) {
                pAddressDTO.setZipCode(matcher.group(1));
                pAddressDTO.setZipCodeExtension(matcher.group(4));
            }
        }
    }

    public static QuickbooksInfoDTO createQuickbooksInfoDTO(EwsQuickBooks pEwsQuickBooks) {

        //defaults for all QBDT companies (agent or in-product)
        QuickbooksInfoDTO quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
        quickbooksInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);

        if (pEwsQuickBooks == null) {
            return quickbooksInfoDTO;
        }

        quickbooksInfoDTO.setLicenseNumber(pEwsQuickBooks.getLicenseNumber());

        OFXAPPVERObject ofxAPPVERObject = new OFXAPPVERObject(pEwsQuickBooks.getAppVersion());
        quickbooksInfoDTO.setApplicationVersion(ofxAPPVERObject.getQBVersionStr());
        quickbooksInfoDTO.setTaxTableId(ofxAPPVERObject.getTaxTableId());
        quickbooksInfoDTO.setQuickbooksSku(ofxAPPVERObject.getFlavorId());

        return quickbooksInfoDTO;
    }

    private static void updateQuickbooksInfoDTO(EwsQuickBooks pEwsQuickBooks, QuickbooksInfoDTO pQuickbooksInfoDTO) {
        if (pEwsQuickBooks != null && pQuickbooksInfoDTO != null) {
            //Presets for the QBTD Adapter
            pQuickbooksInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
            pQuickbooksInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);

            pQuickbooksInfoDTO.setLicenseNumber(pEwsQuickBooks.getLicenseNumber());

            OFXAPPVERObject ofxAPPVERObject = new OFXAPPVERObject(pEwsQuickBooks.getAppVersion());
            pQuickbooksInfoDTO.setApplicationVersion(ofxAPPVERObject.getQBVersionStr());
            pQuickbooksInfoDTO.setQuickbooksSku(ofxAPPVERObject.getFlavorId());
            pQuickbooksInfoDTO.setTaxTableId(ofxAPPVERObject.getTaxTableId());
        }
    }

    public static EntitlementUnitDTO createCompanyEntitlementDTO(EwsEntitlement pEwsEntitlement, String pEIN) {
        if (pEwsEntitlement == null) {
            return null;
        }

        EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();

        entitlementUnitDTO.setFedTaxId(pEIN);
        entitlementUnitDTO.setCustomerId(pEwsEntitlement.getBillingAccountId());
        entitlementUnitDTO.setContactEmail(pEwsEntitlement.getBuyerEmailAddress());
        entitlementUnitDTO.setLicenseNumber(pEwsEntitlement.getLicenseNumber());
        entitlementUnitDTO.setEntitlementOfferingCode(pEwsEntitlement.getEntitlementOfferingCode());
        if (pEwsEntitlement.getEdition() != null) {
            entitlementUnitDTO.setEditionType(EditionType.valueOf(pEwsEntitlement.getEdition().toString()));
        }
        if (pEwsEntitlement.getTier() != null) {
            entitlementUnitDTO.setNumberOfEmployeesType(NumberOfEmployeesType.valueOf(pEwsEntitlement.getTier().toString().toUpperCase()));
        }
        entitlementUnitDTO.setAssetItemNumber(pEwsEntitlement.getAssetItemNumber());
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);

        if (pEwsEntitlement.getEwsBillingDetails() != null) {
            EwsBillingDetails ewsBillingDetails = pEwsEntitlement.getEwsBillingDetails();

            entitlementUnitDTO.setCreditCardNumber(ewsBillingDetails.getCreditCardNumber());
            entitlementUnitDTO.setCreditCardExpiration(ewsBillingDetails.getCreditCardExp());
            if (ewsBillingDetails.getCreditCardType() != null) {
                entitlementUnitDTO.setCreditCardType(ewsBillingDetails.getCreditCardType().toString());    
            }

            if (ewsBillingDetails.getSubscriptionNextBillDate() != null) {
                entitlementUnitDTO.setNextChargeDate(CalendarUtils.convertToSpcfCalendar(ewsBillingDetails.getSubscriptionNextBillDate()));
            }
            if (ewsBillingDetails.getPaymentMethod() != null) {
                if (EwsPaymentMethod.CC.equals(ewsBillingDetails.getPaymentMethod())) {
                    entitlementUnitDTO.setPaymentMethodType(EntitlementPaymentMethodType.CC);
                } else {
                    entitlementUnitDTO.setPaymentMethodType(EntitlementPaymentMethodType.EFT);
                }
            }
        }

        PspPrincipal principal = Application.getCurrentPrincipal();
        if (principal.isAgent()) {
            entitlementUnitDTO.setOrderSourceCd(OrderSourceCode.Siebel);
        } else {
            entitlementUnitDTO.setOrderSourceCd(OrderSourceCode.EStore);
        }

        return entitlementUnitDTO;
    }

    public static CompanyBankAccountDTO createCompanyBankAccountDTO(EwsBankAccount pEwsBankAccount) {
        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();

        bankAccountDTO.setBankName(pEwsBankAccount.getBankName());
        bankAccountDTO.setAccountNumber(pEwsBankAccount.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pEwsBankAccount.getRoutingNumber());
        switch (pEwsBankAccount.getAccountType()) {
            case Checking:
                bankAccountDTO.setAccountType(BankAccountType.Checking);
                break;
            case Savings:
                bankAccountDTO.setAccountType(BankAccountType.Savings);
        }

        String guid = SpcfUniqueId.generateRandomUniqueIdString();
        companyBankAccountDTO.setCompanyBankAccountID(guid);
        companyBankAccountDTO.setBankAccountDTO(bankAccountDTO);
        companyBankAccountDTO.setSourceBankAccountName(pEwsBankAccount.getQuickBooksName());

        return companyBankAccountDTO;
    }

    public static CompanyBankAccountDTO createCompanyBankAccountDTO(EwsBankAccount pEwsBankAccount,
                                                                    CompanyBankAccount pCompanyBankAccount) throws Exception {
        if (pCompanyBankAccount.getStatusCd().equals(BankAccountStatus.Inactive)) {
            throw new EwsException(EwsMessages.noBankAccount());
        }

        BankAccount bankAccount = pCompanyBankAccount.getBankAccount();
        CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(pCompanyBankAccount);
        BankAccountDTO bankAccountDTO = companyBankAccountDTO.getBankAccountDTO();

        if ((bankAccount.getAccountNumber().equals(pEwsBankAccount.getAccountNumber())) &&
                (bankAccount.getRoutingNumber().equals(pEwsBankAccount.getRoutingNumber())) &&
                ((bankAccount.getAccountTypeCd().equals(BankAccountType.Checking)) &&
                        (pEwsBankAccount.getAccountType().equals(EwsBankAccountType.Checking)) ||
                        (bankAccount.getAccountTypeCd().equals(BankAccountType.Savings)) &&
                                (pEwsBankAccount.getAccountType().equals(EwsBankAccountType.Savings)))) {

            companyBankAccountDTO.setSourceBankAccountName(pEwsBankAccount.getQuickBooksName());
            bankAccountDTO.setBankName(pEwsBankAccount.getBankName());
        } else {
            companyBankAccountDTO.setSourceBankAccountName(pEwsBankAccount.getQuickBooksName());
            bankAccountDTO.setBankName(pEwsBankAccount.getBankName());
            bankAccountDTO.setAccountNumber(pEwsBankAccount.getAccountNumber());
            bankAccountDTO.setRoutingNumber(pEwsBankAccount.getRoutingNumber());
            switch (pEwsBankAccount.getAccountType()) {
                case Checking:
                    bankAccountDTO.setAccountType(BankAccountType.Checking);
                    break;
                case Savings:
                    bankAccountDTO.setAccountType(BankAccountType.Savings);
            }
        }

        return companyBankAccountDTO;
    }

    public static ServiceInfoDTO createServiceInfoDTO(EwsBaseService pEwsBaseService,
                                                      ServiceCode pServiceCode,
                                                      String pSubType) throws Exception {
        if (pEwsBaseService == null) {
            return null;
        }

        OfferingInfoDTO offeringInfoDTO;
        ServiceInfoDTO serviceInfoDTO = null;
        switch (pServiceCode) {
            case Cloud:
                offeringInfoDTO = OfferingInfoDTO.CLOUD;
                serviceInfoDTO = new ServiceInfoDTO();
                serviceInfoDTO.setServiceCode(ServiceCode.Cloud);
                serviceInfoDTO.setOfferingCode(offeringInfoDTO.getOfferingCode());
                break;
            case ViewMyPaycheck:
                offeringInfoDTO = OfferingInfoDTO.VIEW_MY_PAYCHECK;
                serviceInfoDTO = new ServiceInfoDTO();
                serviceInfoDTO.setServiceCode(ServiceCode.ViewMyPaycheck);
                serviceInfoDTO.setOfferingCode(offeringInfoDTO.getOfferingCode());
                break;
            case CloudV2:
                offeringInfoDTO = OfferingInfoDTO.CLOUD_V2;
                serviceInfoDTO = new ServiceInfoDTO();
                serviceInfoDTO.setServiceCode(ServiceCode.CloudV2);
                serviceInfoDTO.setOfferingCode(offeringInfoDTO.getOfferingCode());
                break;
            case DirectDeposit:
                serviceInfoDTO = new DDServiceInfoDTO();
                serviceInfoDTO.setOfferingCode(createOfferingInfoDTO(pSubType).getOfferingCode());
                break;
            case Tax:
                offeringInfoDTO = OfferingInfoDTO.TAX;
                serviceInfoDTO = new TaxServiceInfoDTO();
                serviceInfoDTO.setServiceCode(ServiceCode.Tax);
                serviceInfoDTO.setOfferingCode(offeringInfoDTO.getOfferingCode());
                break;
            case BillPayment:
                offeringInfoDTO = OfferingInfoDTO.BILL_PAYMENT_STD3;
                serviceInfoDTO = new ServiceInfoDTO();
                serviceInfoDTO.setServiceCode(ServiceCode.BillPayment);
                serviceInfoDTO.setOfferingCode(offeringInfoDTO.getOfferingCode());
                break;
        }
        return serviceInfoDTO;
    }

    public static OfferingInfoDTO createOfferingInfoDTO(String pSubType) throws Exception {
        OfferingInfoDTO offeringInfoDTO = new OfferingInfoDTO();
        offeringInfoDTO.setPayrollSubTypeCd(createPayrollSubtypeCode(pSubType));
        return offeringInfoDTO;
    }

    private static PayrollSubtypeCode createPayrollSubtypeCode(String pSubType) throws Exception {
        if (EwsAdapterConst.BASIC_LIMITED.equals(pSubType)) {
            return PayrollSubtypeCode.BasicLimited;
        } else if (EwsAdapterConst.BASIC_UNLIMITED.equals(pSubType)) {
            return PayrollSubtypeCode.BasicUnlimited;
        } else if (EwsAdapterConst.ENHANCED.equals(pSubType)) {
            return PayrollSubtypeCode.Enhanced;
        } else if (EwsAdapterConst.ENHANCED_ACCOUNTANT.equals(pSubType)) {
            return PayrollSubtypeCode.EnhancedAccountant;
        } else if (EwsAdapterConst.ENHANCED_UNLIMITED.equals(pSubType)) {
            return PayrollSubtypeCode.EnhancedUnlimited;
        } else if (EwsAdapterConst.NEW_BASIC_UNLIMITED.equals(pSubType)) {
            return PayrollSubtypeCode.NewBasicUnlimited;
        } else if (EwsAdapterConst.STANDARD.equals(pSubType)) {
            return PayrollSubtypeCode.Standard;
        } else if (EwsAdapterConst.BASIC_0_TO_3_EMP.equals(pSubType)) {
            return PayrollSubtypeCode.Basic0to3Emp;
        } else if (EwsAdapterConst.ENHANCED_0_TO_3_EMP.equals(pSubType)) {
            return PayrollSubtypeCode.Enhanced0to3Emp;
        } else if (EwsAdapterConst.PAP_ENH_ACCT.equals(pSubType)) {
            return PayrollSubtypeCode.PAPEnhAcct;
        } else if (EwsAdapterConst.FREE_BASIC_1.equals(pSubType)) {
            return PayrollSubtypeCode.FreeBasic1;
        } else if (EwsAdapterConst.ASSISTED.equals(pSubType)) {
            return PayrollSubtypeCode.Assisted;
        } else if (EwsAdapterConst.ASSISTED_ADV.equals(pSubType)) {
            return PayrollSubtypeCode.AssistedAdv;
        } else {
            throw new EwsException(EwsMessages.subTypeNotFound(pSubType));
        }
    }

    public static boolean isActiveOnService(Company pCompany, ServiceCode pServiceCode) {
        CompanyService companyService = pCompany.getCompanyService(pServiceCode);

        return companyService != null && !companyService.getStatusCd().equals(ServiceSubStatusCode.Cancelled) &&
                !companyService.getStatusCd().equals(ServiceSubStatusCode.Terminated);
    }

    public static ContactDTO findContactDTO(Collection<ContactDTO> pContacts, ContactRole pContactRole) {
        for (ContactDTO contactDTO : pContacts) {
            if (contactDTO.getContactRoleCd().equals(pContactRole)) {
                return contactDTO;
            }
        }
        return null;
    }

    public static DomainEntitySet<TransmissionPayrollRun> getLastTransmissionWithPayroll(Company pCompany) {
        DomainEntitySet<TransmissionPayrollRun> transmissionPayrollRuns = null;

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(pCompany, null, null);
        if (payrollRuns.size() > 0) {
            PayrollRun payrollRun = payrollRuns.get(payrollRuns.size() - 1);
            transmissionPayrollRuns = getTransmissionPayrollRuns(payrollRun, null);
        }
        return transmissionPayrollRuns;
    }

    public static DomainEntitySet<TransmissionPayrollRun> getTransmissionPayrollRuns(PayrollRun pPayrollRun,
                                                                              PayrollProcessCode pPayrollProcessCode) {
        Criterion<TransmissionPayrollRun> where = TransmissionPayrollRun.PayrollRun().equalTo(pPayrollRun);
        if (pPayrollProcessCode != null) {
            where = where.And(TransmissionPayrollRun.PayrollProcess().equalTo(pPayrollProcessCode));
        }

        Expression<TransmissionPayrollRun> query =
                new Query<TransmissionPayrollRun>()
                       .Where(where)
                       .OrderBy(TransmissionPayrollRun.CreatedDate());

        return Application.find(TransmissionPayrollRun.class, query);
    }

    public static DomainEntitySet<FinancialTransaction> getFinancialTransactions(Company pCompany,
                                                                          PayrollRun pPayrollRun,
                                                                          TransactionTypeCode pTransactionTypeCode) {
        TransactionType transactionType = TransactionType.findTransactionType(pTransactionTypeCode);

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                       .Where(FinancialTransaction.Company().equalTo(pCompany)
                              .And(FinancialTransaction.PayrollRun().equalTo(pPayrollRun)
                              .And(FinancialTransaction.TransactionType().equalTo(transactionType))))
                       .OrderBy(FinancialTransaction.CreatedDate().Descending());

        return Application.find(FinancialTransaction.class, query);
    }

    public static String getFailedTransmissionMessage(DomainEntitySet<CompanyEventDetail> pCompanyEventDetails) {
        String message = "";

        if (pCompanyEventDetails != null && pCompanyEventDetails.size() > 0) {
            CompanyEvent companyEvent = pCompanyEventDetails.get(0).getCompanyEvent();

            for (CompanyEventDetail companyEventDetail : companyEvent.getCompanyEventDetailCollection()) {
                if (companyEventDetail.getEventDetailTypeCd().equals(EventDetailTypeCode.ReasonDescription)) {
                    message = companyEventDetail.getValue();
                }
            }
        }
        return message;
    }

    public static void updateEntitlementDTO(EwsBillingDetails pEwsBillingDetails, EntitlementDTO pEntitlementDTO) {

        pEntitlementDTO.setCreditCardNumber(pEwsBillingDetails.getCreditCardNumber());
        pEntitlementDTO.setCreditCardExpiration(pEwsBillingDetails.getCreditCardExp());
        pEntitlementDTO.setCreditCardType(pEwsBillingDetails.getCreditCardType().toString());

        if (pEwsBillingDetails.getSubscriptionNextBillDate() != null) {
            pEntitlementDTO.setNextChargeDate(CalendarUtils.convertToSpcfCalendar(pEwsBillingDetails.getSubscriptionNextBillDate()));
        }

        if (EwsPaymentMethod.CC.equals(pEwsBillingDetails.getPaymentMethod())) {
            pEntitlementDTO.setPaymentMethodType(EntitlementPaymentMethodType.CC);
        } else {
            pEntitlementDTO.setPaymentMethodType(EntitlementPaymentMethodType.EFT);
        }
    }

    public static EntitlementUnitDTO createEntitlementUnitDTO(Company pCompany, EwsEntitlement pEwsEntitlement) {
        EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();

        entitlementUnitDTO.setFedTaxId(pCompany.getFedTaxId());
        entitlementUnitDTO.setCustomerId(pEwsEntitlement.getBillingAccountId());
        entitlementUnitDTO.setContactEmail(pEwsEntitlement.getBuyerEmailAddress());
        entitlementUnitDTO.setLicenseNumber(pEwsEntitlement.getLicenseNumber());
        entitlementUnitDTO.setEntitlementOfferingCode(pEwsEntitlement.getEntitlementOfferingCode());
        if (pEwsEntitlement.getEdition() != null) {
            entitlementUnitDTO.setEditionType(EditionType.valueOf(pEwsEntitlement.getEdition().toString()));
        }
        if (pEwsEntitlement.getTier() != null) {
            entitlementUnitDTO.setNumberOfEmployeesType(NumberOfEmployeesType.valueOf(pEwsEntitlement.getTier().toString().toUpperCase()));
        }
        entitlementUnitDTO.setAssetItemNumber(pEwsEntitlement.getAssetItemNumber());
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);

        if (pEwsEntitlement.getEwsBillingDetails() != null) {
            EwsBillingDetails ewsBillingDetails = pEwsEntitlement.getEwsBillingDetails();

            entitlementUnitDTO.setCreditCardNumber(ewsBillingDetails.getCreditCardNumber());
            entitlementUnitDTO.setCreditCardExpiration(ewsBillingDetails.getCreditCardExp());
            entitlementUnitDTO.setCreditCardType(ewsBillingDetails.getCreditCardType().toString());

            if (ewsBillingDetails.getSubscriptionNextBillDate() != null) {
                entitlementUnitDTO.setNextChargeDate(CalendarUtils.convertToSpcfCalendar(ewsBillingDetails.getSubscriptionNextBillDate()));
            }

            if (EwsPaymentMethod.CC.equals(ewsBillingDetails.getPaymentMethod())) {
                entitlementUnitDTO.setPaymentMethodType(EntitlementPaymentMethodType.CC);
            } else {
                entitlementUnitDTO.setPaymentMethodType(EntitlementPaymentMethodType.EFT);
            }
        }

        return entitlementUnitDTO;
    }

    public static boolean doesPinExists(String pPSID) throws Exception {
        Company company = findCompany(pPSID);
        return company.isPINCreated();
    }

    public static void updateTaxServiceInfo(EwsCompany pEwsCompany, TaxServiceInfoDTO pTaxServiceInfoDTO) {

        if (pEwsCompany.getClientPacketDeliveryPreference() != null) {
            if (EwsDeliveryType.mail.equals(pEwsCompany.getClientPacketDeliveryPreference())) {
                pTaxServiceInfoDTO.setClientPacketDeliveryPreferenceCd(DeliveryPreferenceCode.Mail);
            } else {
                pTaxServiceInfoDTO.setClientPacketDeliveryPreferenceCd(DeliveryPreferenceCode.Electronic);
            }
        }

        if (pEwsCompany.getW2DeliveryPreference() != null) {
            if (EwsDeliveryType.electronic.equals(pEwsCompany.getW2DeliveryPreference())) {
                pTaxServiceInfoDTO.setW2DeliveryPreferenceCd(DeliveryPreferenceCode.Electronic);
            } else {
                pTaxServiceInfoDTO.setW2DeliveryPreferenceCd(DeliveryPreferenceCode.Mail);
            }
        }

    }

}
