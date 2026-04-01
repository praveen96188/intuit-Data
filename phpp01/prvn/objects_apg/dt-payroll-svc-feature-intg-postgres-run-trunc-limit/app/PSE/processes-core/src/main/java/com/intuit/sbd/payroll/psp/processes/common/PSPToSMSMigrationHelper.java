package com.intuit.sbd.payroll.psp.processes.common;

import com.intuit.money.account.model.AccountHolder;
import com.intuit.money.account.model.Bank;
import com.intuit.money.account.model.FundingInstrument;
import com.intuit.money.account.model.ProfileMigrationRequest;
import com.intuit.money.common.model.Address;
import com.intuit.money.common.model.GovernmentId;
import com.intuit.payments.cdm.v2.client.enums.BankAccountTypeEnum;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPStringUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
public class PSPToSMSMigrationHelper {

    private static String tid;
    private static String realmId;

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
    private static final String BANKROUTINGNUMBER = "bankRoutingNumber";
    private static final String BANKACCOUNTNUMBER = "bankAccountNumber";
    private static final int SIC_STANDARD_LENGTH = 4;
    private static final String REGEX = "[^0-9]";
    private static boolean isDebugLogEnabled;

    /**
     * creates migration request object to be sent as payload in migrate call.
     *
     * @param company
     * @param psId
     * @return
     */

    public static ProfileMigrationRequest createProfileMigrationRequest(Company company, String psId, String requestId, boolean debugEnabled) {
        try {
            tid = requestId;
            isDebugLogEnabled = debugEnabled;
            realmId = company.getIAMRealmId();
            ProfileMigrationRequest reqObj = new ProfileMigrationRequest();
            DomainEntitySet<Contact> contacts = company.getContactCollection();
            Optional<Contact> primaryPrincipalOp = contacts.stream().filter(contact -> contact.getContactRoleCd() == ContactRole.PrimaryPrincipal).findFirst();
            Contact primaryPrincipalContact = primaryPrincipalOp.get();
            reqObj.setId(company.getIAMRealmId());
            reqObj.setIntent(MONEYOUT);
            reqObj.setSource(QBDT);
            reqObj.setOnboardingDate(getOnboardingDate(company));
            reqObj.setCompany(getCompanyDetails(company, primaryPrincipalContact));
            reqObj.setFundingInstruments(getFundingInstruments(company));
            reqObj.setAccountHolders(getAccountHolders(primaryPrincipalContact));
            reqObj.setAccountType(BUSINESS);
            return reqObj;

        } catch (Exception e) {
            log.error("job=PSPtoSMSMigration, action=createMigratePSPtoASRequest, status=Error, psId={}, tid={}, realmId={}, errType=createRequestFailed, errorMsg={}", psId, tid, realmId, e.getMessage(), e);
            return null;
        }
    }

    public static OffsetDateTime getOnboardingDate(Company company) throws Exception {
        CompanyService ddService = company.getCompanyService(ServiceCode.DirectDeposit);
        try {

            SpcfCalendar serviceStartDate = ddService.getCreatedDate();
            if (serviceStartDate != null) {
                OffsetDateTime onboardingDate = OffsetDateTime.parse(serviceStartDate.toISO8601());
                return onboardingDate;
            } else {
                log.error("job=PSPtoSMSMigration, action=getOnboardingDate, status=Error, errType=onboardingDateRetrievalFailed, tid={}, realmId={}", tid, realmId);
                throw new Exception("error getting onboarding date");
            }
        } catch (Exception e) {
            log.error("job=PSPtoSMSMigration, action=getOnboardingDate, status=Error, errType=onboardingDateRetrievalException, tid={}, realmId={}", tid, realmId, e);
            throw new Exception("error getting onboarding date");
        }
    }

    public static com.intuit.money.account.model.Company getCompanyDetails(Company company, Contact contact) {
        try {

            com.intuit.money.account.model.Company mCompany = new com.intuit.money.account.model.Company();
            String phone = contact.getPhone();
            mCompany.setLegalName(company.getLegalName());
            mCompany.setBusinessName(company.getDbaName());
            mCompany.setAddress(getCompanyAddress(company, LOCATION));
            mCompany.setEmail(contact.getEmail());
            mCompany.setGovernmentIds(getGovernmentIds(company));
            mCompany.setOwnershipType(company.getCompanyAdditionalInfo().getOwnershipType().getOwnership());
            mCompany.setIndustryCode(getCompanyIndustryCode(company));
            mCompany.setPhone(checkAndReplaceFieldForSplChar(PHONE, phone, true));

            return mCompany;
        } catch (Exception e) {
            log.error("job=PSPtoSMSMigration, action=getCompanyDetails, status=Error, errType=getCompanyDetailsFailed, tid={}, realmId={}", tid, realmId, e);
            throw e;
        }

    }


    public static Map<String, String> getCompanyIndustryCode(Company company) {
        try {

            Map<String, String> industryCode = new HashMap<>();
            String standardIndustryCode = company.getCompanyAdditionalInfo().getIndustryType().getStandardIndustryCode();
            String paddedSIC = standardIndustryCode;

            if (StringUtils.isNotEmpty(standardIndustryCode) && standardIndustryCode.length() < SIC_STANDARD_LENGTH) {
                paddedSIC = StringUtils.leftPad(standardIndustryCode, SIC_STANDARD_LENGTH, '0');
            }
            industryCode.put(SIC, paddedSIC);

            return industryCode;

        } catch (Exception e) {
            log.error("job=PSPtoSMSMigration, action=getCompanyIndustryCode, status=Error, errType=getCompanyIndustryCodeFailed, tid={}, realmId={}", tid, realmId, e);
            throw e;
        }
    }

    public static List<Address> getCompanyAddress(Company company, String type) {

        List<Address> addressList = new ArrayList<>();
        Address address = getAddress(company.getComplianceAddress(), type);
        addressList.add(address);
        return addressList;

    }

    public static List<Address> getCompanyAddressViaLegalAddress(Company company, String type) {

        List<Address> addressList = new ArrayList<>();
        Address address = getAddress(company.getLegalAddress(), type);
        addressList.add(address);
        return addressList;

    }

    public static Address getAddress(com.intuit.sbd.payroll.psp.domain.Address pAddress, String type) {
        try {
            Address address = new Address();
            address.setStreetAddressLine1(pAddress.getAddressLine1());
            address.setStreetAddressLine2(pAddress.getAddressLine2());
            address.setCity(pAddress.getCity());
            address.setRegion(pAddress.getState());
            address.setPostalCode(pAddress.getFullZipCode());
            address.setCountry(validateCountry(pAddress.getCountry()));
            address.setType(type);
            return address;
        } catch (Exception e) {
            log.error("job=PSPtoSMSMigration, action=getAddress, status=Error, errType=getAddressFailed, type={}, tid={}, realmId={}",
                    type, tid, realmId, e);
            throw e;
        }
    }

    public static List<GovernmentId> getGovernmentIds(Company company) {
        try {

            List<GovernmentId> governmentIds = new ArrayList<>();
            GovernmentId governmentId = new GovernmentId();
            governmentId.setIdType(TAX_ID);
            governmentId.setGovernmentId(company.getFedTaxId());

            governmentIds.add(governmentId);
            return governmentIds;
        } catch (Exception e) {
            log.error("job=PSPtoSMSMigration, action=getGovernmentIds, status=Error, errType=getGovernmentIdsFailed, tid={}, realmId={}", tid, realmId, e);
            throw e;
        }
    }

    public static List<FundingInstrument> getFundingInstruments(Company company) {
        try {

            List<FundingInstrument> fundingInstruments = new ArrayList<>();
            FundingInstrument fundingInstrument = new FundingInstrument();

            fundingInstrument.setBank(getBankDetails(company));
            fundingInstrument.setVerifiedBankDepositStatus(VERIFIED);
            fundingInstruments.add(fundingInstrument);
            return fundingInstruments;
        } catch (Exception e) {
            log.error("job=PSPtoSMSMigration, action=getFundingInstruments, status=Error, errType=getFundingInstrumentsFailed, tid={} realmId={}", tid, realmId, e);
            throw e;
        }
    }

    public static Bank getBankDetails(Company company) {
        try {
            Bank bank = new Bank();
            CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
            if (Objects.isNull(companyBankAccount)) {
                throw new RuntimeException("CompanyBankAccount_Not_Found");
            }
            String bankRoutingNumber = companyBankAccount.getBankAccount().getRoutingNumber();
            String bankAccountNumber = companyBankAccount.getBankAccount().getAccountNumber();

            bank.setBankCode(checkAndReplaceFieldForSplChar(BANKROUTINGNUMBER, bankRoutingNumber, true));
            // for below change, please refer to JIRA - https://jira.intuit.com/browse/PSP-24656 for more details
            bank.setAccountNumber(checkAndReplaceFieldForSplChar(BANKACCOUNTNUMBER, bankAccountNumber, false));
            BankAccountType bankAccountType = companyBankAccount.getBankAccount().getAccountTypeCd();
            bank.setAccountType((bankAccountType == BankAccountType.Savings) ? BankAccountTypeEnum.SAVINGS.value() :
                    BankAccountTypeEnum.CHECKING.value());
            return bank;
        } catch (Exception e) {
            String psid = company == null ? StringUtils.EMPTY : company.getSourceCompanyId();
            log.error("job=PSPtoSMSMigration, action=getBankDetails, status=Error, errType=getBankDetailsFailed, tid={}, psid={}, realmId={}",
                      tid, psid, realmId, e);
            throw e;
        }
    }

    public static List<AccountHolder> getAccountHolders(Contact contact) {
        try {

            List<AccountHolder> accountHolders = new ArrayList<>();
            String phone = contact.getPhone();
            AccountHolder accountHolder = new AccountHolder();
            accountHolder.setType(PRINCIPAL);
            accountHolder.setFirstName(contact.getFirstName());
            accountHolder.setLastName(contact.getLastName());
            LocalDate localDate = LocalDate.parse(contact.getDateOfBirth().toLocalDate().toString());
            accountHolder.setDateOfBirth(localDate);
            accountHolder.setGovernmentIds(getContactGovernmentIds(contact));
            accountHolder.setAddress(getAddress(contact.getMailingAddress(), RESIDENCE));
            accountHolder.setPhone(checkAndReplaceFieldForSplChar(PHONE, phone, true));
            accountHolders.add(accountHolder);
            return accountHolders;
        } catch (Exception e) {
            log.error("job=PSPtoSMSMigration, action=getAccountHolders, status=Error, errType=getAccountHoldersFailed, tid={}, realmId={}", tid, realmId, e);
            throw e;
        }

    }

    public static List<GovernmentId> getContactGovernmentIds(Contact contact) {
        try {
            List<GovernmentId> contactGovernmentIds = new ArrayList<>();
            GovernmentId contactGovernmentId = new GovernmentId();
            contactGovernmentId.setIdType(SSN);
            contactGovernmentId.setGovernmentId(contact.getSocialSecurityNumber());
            contactGovernmentIds.add(contactGovernmentId);
            return contactGovernmentIds;
        } catch (Exception e) {
            log.error("job=PSPtoSMSMigration, action=getContactGovernmentIds, status=Error, errType=getContactGovernmentIdsFailed, tid={}, realmId={}", tid, realmId, e);
            throw e;
        }
    }

    public static String checkAndReplaceFieldForSplChar(String fieldName, String fieldValue, boolean replace) {

        String mFieldValue = fieldValue;

        if (PSPStringUtils.isOnlyDigits(mFieldValue)) {
            log.info("job=PSPtoSMSMigration, action=checkAndReplaceFieldForSplChar, status=specialCharacterFound, fieldName={}, realmId={}", fieldName, realmId);
            if (replace) {
                mFieldValue = mFieldValue.replaceAll(REGEX, "");
            }
        }
        return mFieldValue;
    }

    private static String validateCountry(String country) {

        String mCountry;

        if (StringUtils.isNotBlank(country)) {
            mCountry = country;
        } else {
            mCountry = COUNTRY_US;
        }

        return mCountry;
    }

}
