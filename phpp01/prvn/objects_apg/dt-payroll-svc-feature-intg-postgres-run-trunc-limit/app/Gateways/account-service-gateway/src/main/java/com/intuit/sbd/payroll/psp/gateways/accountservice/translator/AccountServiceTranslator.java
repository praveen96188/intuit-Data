package com.intuit.sbd.payroll.psp.gateways.accountservice.translator;

import com.intuit.payments.cdm.v2.client.BankAccount;
import com.intuit.payments.cdm.v2.client.*;
import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;
import com.intuit.payments.cdm.v2.client.enums.BankAccountTypeEnum;
import com.intuit.payments.cdm.v2.client.enums.BankAccountUsageTypeEnum;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGatewayImpl;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intuit.v4.payments.definitions.PaymentsBankAccountType;
import com.intuit.v4.payments.definitions.PaymentsBankAccountTypeEnum;
import org.apache.commons.lang.StringUtils;


public class AccountServiceTranslator {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(AccountServiceGatewayImpl.class);

    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("((\\d){5})(\\-)?((\\d){4})?$");
    private static final String ADDRESS_SPLIT_REGEX = "\\r?\\n|\\r";
    private FeatureFlagLazyLoader featureFlagLazyLoader = FeatureFlagLazyLoader.getInstance();

    public CompanyDTO getUpdatedPSPCompanyDTO(PaymentsAccount paymentsAccount, Company pspCompany) {

        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(pspCompany);

        updateCompanyDTO(paymentsAccount, companyDTO);

        return companyDTO;

    }

    private void updateCompanyDTO(PaymentsAccount paymentsAccount, CompanyDTO companyDTO) {

        BusinessOwner paymentsPrimaryPrinciple = getPaymentsPrimaryPrincipal(paymentsAccount);
        String paymentsBusinessEmail = getBusinessEmail(paymentsAccount);


        //Update Primary Principal
        ContactDTO pspPrimaryPrincipleContact = updateContactDTO(companyDTO, paymentsPrimaryPrinciple, ContactRole.PrimaryPrincipal, paymentsBusinessEmail);
        updateAddressDTO(paymentsPrimaryPrinciple.getAddress(), pspPrimaryPrincipleContact.getAddress());

        companyDTO.setNotificationEmail(pspPrimaryPrincipleContact.getEmail());

        companyDTO.setLegalName(getLegalName(paymentsAccount));

        companyDTO.setDBA(getBusinessName(paymentsAccount));


        companyDTO.setIAMRealmId(paymentsAccount.getRealmId());

        companyDTO.setMailingAddress(updateAddressDTO(getBusinessAddress(paymentsAccount), companyDTO.getMailingAddress()));

        if (isSMSAddressFixEnabled()) {
            companyDTO.setComplianceAddress(updateAddressDTO(getBusinessAddress(paymentsAccount), companyDTO.getComplianceAddress()));
        } else {
            companyDTO.setLegalAddress(updateAddressDTO(getBusinessAddress(paymentsAccount), companyDTO.getLegalAddress()));
        }

        if(Objects.nonNull(paymentsAccount.getBusinessInfo().getDescription()) || Objects.nonNull(paymentsAccount.getBusinessInfo().getOwnershipType())){



            String industryName= null;
            String ownershipName = null;
            if(Objects.nonNull(paymentsAccount.getBusinessInfo().getDescription())) {
                industryName = IndustryType.getCaseSensitiveIndustry(paymentsAccount.getBusinessInfo().getDescription());
            }

            if(Objects.nonNull(paymentsAccount.getBusinessInfo().getOwnershipType())) {
                ownershipName = OwnershipType.findOwnership(paymentsAccount.getBusinessInfo().getOwnershipType().getValue());
            }

            if(StringUtils.isNotBlank(industryName) || StringUtils.isNotBlank(ownershipName)){
                CompanyAdditionalInfoDTO companyAdditionalInfoDTO = new CompanyAdditionalInfoDTO();
                if(StringUtils.isNotBlank(industryName)) {
                    companyAdditionalInfoDTO.setIndustry(industryName);
                }
                if(StringUtils.isNotBlank(ownershipName)) {
                    logger.info("companyAdditionalInfoDTO ownershipType set to " + ownershipName + " for companyId: " + companyDTO.getCompanyId());
                    companyAdditionalInfoDTO.setOwnership(ownershipName);
                }
                companyDTO.setCompanyAdditionalInfo(companyAdditionalInfoDTO);
            }
        }



    }


    public ContactDTO getContactDTO(CompanyDTO companyDTO, ContactRole pContactRole){
        Optional<ContactDTO> optionalPSPContactDTO = companyDTO.getContacts().stream().filter(contactDTO -> contactDTO.getContactRoleCd().equals(pContactRole)).findFirst();
        return optionalPSPContactDTO.isPresent() ? optionalPSPContactDTO.get() : null;
    }

    public boolean isSMSAddressFixEnabled(){
        return featureFlagLazyLoader.getFeatureFlagValue(FeatureFlags.Key.PSP_SMS_ADDRESS_FIX_ENABLE);
    }

    public ContactDTO updateContactDTO(CompanyDTO companyDTO, BusinessOwner owner, ContactRole pContactRole, String paymentsBusinessEmail) {

        ContactDTO pspContactDTO = getContactDTO(companyDTO,pContactRole);
        if (Objects.isNull(pspContactDTO)) {
            pspContactDTO = new ContactDTO();
            String guid = SpcfUniqueId.generateRandomUniqueIdString();
            pspContactDTO.setContactId(guid);
            companyDTO.getContacts().add(pspContactDTO);
        }

        pspContactDTO.setFirstName(owner.getFirstName());
        pspContactDTO.setMiddleName(null);

        pspContactDTO.setLastName(owner.getLastName());
        pspContactDTO.setEmail(paymentsBusinessEmail);
        pspContactDTO.setPhoneNumber(owner.getPhone());
        if (owner.getSsn() != null)
            pspContactDTO.setSocialSecurityNumber(owner.getSsn());

        pspContactDTO.setDateOfBirth(new DateDTO(owner.getDateOfBirth()));


        switch (pContactRole) {
            case PayrollAdmin:
                pspContactDTO.setContactRoleCd(pContactRole);
                pspContactDTO.setAccountSignatory(false);
                break;
            case PrimaryPrincipal:
                pspContactDTO.setContactRoleCd(pContactRole);
                pspContactDTO.setAccountSignatory(true);
                break;
            case SecondaryPrincipal:
                pspContactDTO.setContactRoleCd(pContactRole);
                pspContactDTO.setAccountSignatory(true);
        }

        if (Objects.isNull(pspContactDTO.getAddress())) {
            pspContactDTO.setAddress(new AddressDTO());
        }

        return pspContactDTO;
    }

    public void updateBusinessOwner(BusinessOwner businessOwner, CompanyDTO pDtoCompany) {
        ContactDTO contact = getContactDTO(pDtoCompany,ContactRole.PrimaryPrincipal);
        if (Objects.nonNull(contact)){
            businessOwner.setFirstName(contact.getFirstName());
            businessOwner.setLastName(contact.getLastName());
            businessOwner.setEmail(contact.getEmail());
            businessOwner.setPhone(Objects.nonNull(contact.getPhoneNumber())
                    ?contact.getPhoneNumber().replaceAll("\\D+","")
                    :null);
            businessOwner.setSsn(contact.getSocialSecurityNumber());
            if (Objects.nonNull(contact.getDateOfBirth())) {
                businessOwner.setDateOfBirth(CalendarUtils.convertToDate(contact.getDateOfBirth().toSpcfCalendar()));
            }
            businessOwner.setAddress(createPhysicalAddress(contact.getAddress(),AddressTypeEnum.RESIDENCE));
        }
    }

    public AddressDTO updateAddressDTO(PhysicalAddress paymentsAddress, AddressDTO pspAddressDTO) {


        if (pspAddressDTO == null && paymentsAddress != null) {
            pspAddressDTO = new AddressDTO();
        }

        if (paymentsAddress != null && pspAddressDTO != null) {
            pspAddressDTO.setAddressLine1(null);
            pspAddressDTO.setAddressLine2(null);
            pspAddressDTO.setAddressLine3(null);
            splitPaymentsAddress(paymentsAddress, pspAddressDTO);
            pspAddressDTO.setCity(paymentsAddress.getCity());
            pspAddressDTO.setState(paymentsAddress.getRegion());

            Matcher matcher = ZIP_CODE_PATTERN.matcher(paymentsAddress.getPostalCode());
            if (matcher.matches()) {
                pspAddressDTO.setZipCode(matcher.group(1));
                pspAddressDTO.setZipCodeExtension(matcher.group(4));
            }
            pspAddressDTO.setCountry(paymentsAddress.getCountry());
        }
        return pspAddressDTO;
    }

    public void splitPaymentsAddress(PhysicalAddress paymentsAddress, AddressDTO pspAddressDTO) {
        String[] addressArray = paymentsAddress.getStreetAddress().split(ADDRESS_SPLIT_REGEX);
        if (addressArray.length > 0)
            pspAddressDTO.setAddressLine1(addressArray[0]);
        if (addressArray.length > 1)
            pspAddressDTO.setAddressLine2(addressArray[1]);
    }


    public BankAccount getPaymentBank(PaymentsAccount paymentsAccount) {
        Optional<BankAccount> paymentsBankaccount = paymentsAccount.getBankAccounts().stream().filter(bankacount -> bankacount.getUsageType().equals(BankAccountUsageTypeEnum.MONEY_OUT)).findFirst();
        return paymentsBankaccount.isPresent() ? paymentsBankaccount.get() : null;
    }

    /**
     * PSP To AMS Mapping
     * PSP (Checking) <- AMS (Checking,BusinessChecking,PersonalChecking)
     * PSP (Savings) <- AMS (Savings,BusinessSavings,PersonalSavings)
     *
     * @param paymentsBankAccount BankAccount
     * @return BankAccountType
     */
    public BankAccountType getBankAccountType(BankAccount paymentsBankAccount) {
        BankAccountType bankAccountType = BankAccountType.Checking;
        if (Objects.isNull(paymentsBankAccount) || Objects.isNull(paymentsBankAccount.getType())) {
            return bankAccountType;
        }
        switch (paymentsBankAccount.getType()) {
            case SAVINGS:
            case BUSINESS_SAVINGS:
            case PERSONAL_SAVINGS:
                bankAccountType = BankAccountType.Savings;
                break;
            case CHECKING:
            case BUSINESS_CHECKING:
            case PERSONAL_CHECKING:
                bankAccountType = BankAccountType.Checking;
                break;
        }
        return bankAccountType;
    }

    public BusinessOwner getPaymentsPrimaryPrincipal(PaymentsAccount paymentsAccount) {
        Optional<BusinessOwner> primaryPrincipleOptional = paymentsAccount.getBusinessOwners().stream().filter(businessOwner -> businessOwner.getPrincipalOwner()).findFirst();
        if(primaryPrincipleOptional.isPresent()){
             return primaryPrincipleOptional.get();
        }
        return null;
    }

    public CompanyBankAccount getCompanyBankAccountByAccountNumber(BankAccount paymentsBankAccount, Company company) {
        // Fetching PSP CompanyBankAccount for the corresponding Payments Bank Account.
        BankAccountType bankAccountType = getBankAccountType(paymentsBankAccount);
        return CompanyBankAccount.findCompanyBankAccountByAccountNumber(company, paymentsBankAccount.getAccountNumber(), paymentsBankAccount.getRoutingNumber(), bankAccountType);
    }

    public String getBusinessEmail(PaymentsAccount paymentsAccount) {
        return getBusinessInfo(paymentsAccount).getEmail();
    }

    public String getLegalName(PaymentsAccount paymentsAccount) {
        return getBusinessInfo(paymentsAccount).getLegalName();
    }

    public PhysicalAddress getBusinessAddress(PaymentsAccount paymentsAccount) {
        return getBusinessInfo(paymentsAccount).getAddress();
    }

    public String getBusinessName(PaymentsAccount paymentsAccount) {
        return getBusinessInfo(paymentsAccount).getBusinessName();
    }

    public PrimaryBusiness getBusinessInfo(PaymentsAccount paymentsAccount) {
        return paymentsAccount.getBusinessInfo();
    }

    public void updatePrimaryBusiness(PrimaryBusiness primaryBusiness, CompanyDTO pDtoCompany) {
        // Removed so we will not update the EIN to account service
        //primaryBusiness.setEin(pDtoCompany.getFein());
        primaryBusiness.setLegalName(pDtoCompany.getLegalName());
        primaryBusiness.setBusinessName(pDtoCompany.getDBA());
        primaryBusiness.setAddress(createPhysicalAddress(pDtoCompany.getLegalAddress(), AddressTypeEnum.LOCATION));
        primaryBusiness.setMailingAddress(createPhysicalAddress(pDtoCompany.getLegalAddress(), AddressTypeEnum.MAILING));
        primaryBusiness.setNaics(null);
        if (Objects.nonNull(pDtoCompany.getCompanyAdditionalInfo())
                && Objects.nonNull(pDtoCompany.getCompanyAdditionalInfo().getIndustry())) {
            IndustryType industry = IndustryType.findIndustryType(pDtoCompany.getCompanyAdditionalInfo().getIndustry());
            if (Objects.nonNull(industry)) {
                primaryBusiness.setSic(industry.getStandardIndustryCode());
                primaryBusiness.setDescription(pDtoCompany.getCompanyAdditionalInfo().getIndustry());
            }
        }
    }

    public PhysicalAddress createPhysicalAddress(AddressDTO address, AddressTypeEnum addressTypeEnum) {
        if (Objects.isNull(address))
            return null;
        PhysicalAddress result = new PhysicalAddress();
        result.setStreetAddress(getStreetAddress(address));
        result.setCity(address.getCity());
        result.setRegion(address.getState());
        result.setType(addressTypeEnum);
        result.setCountry(Objects.nonNull(address.getCountry())? address.getCountry() : "USA" );
        result.setPostalCode(getFullZipCode(address));
        return result;
    }

    public String getFullZipCode(AddressDTO address) {
        String zipCode = "";
        if(Objects.nonNull(address.getZipCode())) {
            zipCode += address.getZipCode();
        }

        if(Objects.nonNull(address.getZipCodeExtension())) {
            zipCode += address.getZipCodeExtension();
        }

        return zipCode;
    }



    private String getStreetAddress(AddressDTO address) {
        return address.getAddressLine1() != null ? address.getAddressLine1() : ""
                + address.getAddressLine2() != null ? "\\n" + address.getAddressLine2() : ""
                + address.getAddressLine3() != null ? "\\n" + address.getAddressLine3() : "";
    }

    public CompanyBankAccountDTO getUpdatedCompanyBankAccountDTO(PaymentsAccount paymentsAccount,
                                                                 CompanyBankAccount pCompanyBankAccount)  {

        BankAccount paymentsBankaccount = getPaymentBank(paymentsAccount);

        CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(pCompanyBankAccount);
        BankAccountDTO bankAccountDTO = companyBankAccountDTO.getBankAccountDTO();


        bankAccountDTO.setBankName(paymentsBankaccount.getName());
        bankAccountDTO.setAccountNumber(paymentsBankaccount.getAccountNumber());
        bankAccountDTO.setRoutingNumber(paymentsBankaccount.getRoutingNumber());
        bankAccountDTO.setAccountType(getBankAccountType(paymentsBankaccount));
        return companyBankAccountDTO;
    }

    public BankAccount findMoneyOutBankAccount(PaymentsAccount paymentsAccount){
        // TODO need to use the common function
        return Objects.nonNull(paymentsAccount) && Objects.nonNull(paymentsAccount.getBankAccounts())
                ? paymentsAccount.getBankAccounts().stream()
                .filter(bankAccount -> BankAccountUsageTypeEnum.MONEY_OUT.equals(bankAccount.getUsageType()))
                .findAny().orElse(null)
                : null;
    }

    public PaymentsAccount createPaymentsAccount(PrimaryBusiness primaryBusiness, BusinessOwner businessOwner, CompanyDTO pDtoCompany) {
        if (Objects.isNull(primaryBusiness) || Objects.isNull(businessOwner) || Objects.isNull(pDtoCompany))
            return null;

        logger.info("Stated translating the DomainCompany to primaryBusiness");
        updatePrimaryBusiness(primaryBusiness, pDtoCompany);
        updateBusinessOwner(businessOwner, pDtoCompany);
        primaryBusiness.setEmail(businessOwner.getEmail());
        logger.info("Completed translating the DomainCompany to primaryBusiness");
        // Updating account service the data set in primaryBuisness
        PaymentsAccount paymentsAccount = new PaymentsAccount();
        paymentsAccount.setBusinessInfo(primaryBusiness);
        paymentsAccount.setBusinessOwners(Collections.singletonList(businessOwner));
        return paymentsAccount;
    }

    public PaymentsBankAccountType getV4BankAccountType(BankAccount bankAccount, CompanyBankAccount companyBankAccount,Company mDomainCompany) {

        PaymentsBankAccountType bankRequest=new PaymentsBankAccountType();
        bankRequest.setBankName(companyBankAccount.getBankAccount().getBankName());
        bankRequest.setBankCode(companyBankAccount.getBankAccount().getRoutingNumber());
        bankRequest.setAccountNumber(companyBankAccount.getBankAccount().getAccountNumber());
        bankRequest.setTypeId(bankAccount.getId());


        bankRequest.setAccountType(PaymentsBankAccountTypeEnum.CHECKING);
      /*  if(companyBankAccount.getBankAccount().getAccountTypeCd() == BankAccountType.Savings){
            bankRequest.setAccountType(PaymentsBankAccountTypeEnum.SAVINGS);
        }*/

        return bankRequest;

    }

}
