package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessage;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.*;
import com.intuit.sbd.payroll.psp.common.utils.ServiceKey;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Jeff Jones
 */
public class EwsFactory {

    public static EwsCreateAccount createEwsCreateAccount(EwsAddService pEwsAddService, Company pCompany, EntitlementUnit pEntitlementUnit, boolean pAddBillPayment) {
        EwsCreateAccount ewsCreateAccount = new EwsCreateAccount();

        ewsCreateAccount.setDateTimeStamp(pEwsAddService.getDateTimeStamp());
        ewsCreateAccount.setIpAddress(pEwsAddService.getIpAddress());
        ewsCreateAccount.setForceRandomDollar(pEwsAddService.getForceRandomDollar());

        ewsCreateAccount.setEwsCompany(EwsFactory.createEwsCompany(pCompany));

        EwsServices ewsServices = new EwsServices();
        ewsServices.setCloudService(new EwsBaseService());
        ewsServices.setAssistedService(pEwsAddService.getEwsBaseServices().getAssistedService());

        if (pAddBillPayment) {
            ewsServices.setBillPayment(new EwsBaseService());
        }

        ewsCreateAccount.setEwsServices(ewsServices);

        if (pEntitlementUnit != null) {
            ArrayList<EwsEntitlement> ewsEntitlements = new ArrayList<EwsEntitlement>();
            ewsEntitlements.add(createEwsEntitlement(pEntitlementUnit));
            ewsCreateAccount.setEwsEntitlements(ewsEntitlements);
        }

        return ewsCreateAccount;
    }

    public static EwsEntitlement createEwsEntitlement(EntitlementUnit pEntitlementUnit) {
        EwsEntitlement ewsEntitlement = new EwsEntitlement();

        Entitlement entitlement = pEntitlementUnit.getEntitlement();
        EntitlementCode entitlementCode = entitlement.getEntitlementCode();

        ewsEntitlement.setAssetItemNumber(entitlementCode.getAssetItemNumber());
        if (entitlementCode.getEditionType() != null) {
            switch (entitlementCode.getEditionType()) {
                case Basic:
                    ewsEntitlement.setEdition(EwsEditionType.Basic);
                    break;
                case Enhanced:
                    ewsEntitlement.setEdition(EwsEditionType.Enhanced);
                    break;
                case EnhancedAccountant:
                    ewsEntitlement.setEdition(EwsEditionType.EnhancedAccountant);
                    break;
                case EnhancedAccountantProAdvisor:
                    ewsEntitlement.setEdition(EwsEditionType.EnhancedAccountantProAdvisor);
                    break;
                case Standard:
                    ewsEntitlement.setEdition(EwsEditionType.Standard);
                    break;
            }
        }
        if (entitlementCode.getNumberOfEmployeesType() != null) {
            switch (entitlementCode.getNumberOfEmployeesType()) {
                case ONE:
                    ewsEntitlement.setTier(EwsTierType.One);
                    break;
                case UPTO3:
                    ewsEntitlement.setTier(EwsTierType.UpTo3);
                    break;
                case UNLIMITED:
                    ewsEntitlement.setTier(EwsTierType.Unlimited);
                    break;
            }
        }

        ewsEntitlement.setBillingAccountId(entitlement.getCustomerId());
        ewsEntitlement.setBuyerEmailAddress(entitlement.getContactEmail());
        ewsEntitlement.setEntitlementOfferingCode(entitlement.getEntitlementOfferingCode());
        ewsEntitlement.setLicenseNumber(entitlement.getLicenseNumber());
        ewsEntitlement.setAddEin(false);
        
        EwsBillingDetails ewsBillingDetails = new EwsBillingDetails();
        ewsBillingDetails.setCreditCardExp(entitlement.getCreditCardExpiration());
        ewsBillingDetails.setCreditCardNumber(entitlement.getCreditCardNumber());
        if (entitlement.getNextChargeDate() != null && !entitlement.getEntitlementCode().isAssisted()) {
            ewsBillingDetails.setSubscriptionNextBillDate(CalendarUtils.convertToCalendar(entitlement.getNextChargeDate()));
        }
        if (entitlement.getPaymentMethodType() != null) {
            switch (entitlement.getPaymentMethodType()) {
                case CC:
                    ewsBillingDetails.setPaymentMethod(EwsPaymentMethod.CC);
                    break;
                case EFT:
                    ewsBillingDetails.setPaymentMethod(EwsPaymentMethod.EFT);
                    break;
            }
        }
        if (entitlement.getCreditCardType() != null) {
            if (entitlement.getCreditCardType().equals("Visa")  || entitlement.getCreditCardType().equals("VISA")) {
                ewsBillingDetails.setCreditCardType(EwsCreditCardType.VISA);
            } else if (entitlement.getCreditCardType().equals("MasterCard") || entitlement.getCreditCardType().equals("MC")) {
                ewsBillingDetails.setCreditCardType(EwsCreditCardType.MC);
            } else if (entitlement.getCreditCardType().equals("American Express") || entitlement.getCreditCardType().equals("AMEX")) {
                ewsBillingDetails.setCreditCardType(EwsCreditCardType.AMEX);
            } else if (entitlement.getCreditCardType().equals("Discover") || entitlement.getCreditCardType().equals("DISC")) {
                ewsBillingDetails.setCreditCardType(EwsCreditCardType.DISC);
            }
        }
        ewsEntitlement.setEwsBillingDetails(ewsBillingDetails);

        return ewsEntitlement;
    }

    public static EwsQuerySubscriptionsResponse createEwsQuerySubscriptionsResponse(DomainEntitySet<EntitlementUnit> pEntitlementUnits){
        if (pEntitlementUnits == null || pEntitlementUnits.isEmpty()) {
            return null;
        }

        EwsQuerySubscriptionsResponse ewsQuerySubscriptionsResponse = new EwsQuerySubscriptionsResponse();

        ArrayList<EwsSubscriptionResponse> ewsSubscriptionResponseList = new ArrayList<EwsSubscriptionResponse>();
        ewsQuerySubscriptionsResponse.setEwsSubscriptionResponses(ewsSubscriptionResponseList);

        for (EntitlementUnit entitlementUnit : pEntitlementUnits) {
            Entitlement entitlement = entitlementUnit.getEntitlement();
            EntitlementCode entitlementCode = entitlement.getEntitlementCode();

            EwsSubscriptionResponse ewsSubscriptionResponse = new EwsSubscriptionResponse();

            ewsSubscriptionResponse.setAssetItemNumber(entitlementCode.getAssetItemNumber());
            ewsSubscriptionResponse.setBuyerEmailAddress(entitlement.getContactEmail());
            ewsSubscriptionResponse.setBillingAccountId(entitlement.getCustomerId());
            ewsSubscriptionResponse.setBillingZip(entitlement.getBillingZipCode());
            ewsSubscriptionResponse.setEntitlementOfferingCode(entitlement.getEntitlementOfferingCode());
            ewsSubscriptionResponse.setLicenseNumber(entitlement.getLicenseNumber());
            ewsSubscriptionResponse.setSubscriptionNumber(entitlement.getSubscriptionNumber());
            ewsSubscriptionResponse.setSubType(String.valueOf(entitlementCode.getQuickBooksSubtype()));

            if (entitlementCode.getEditionType() != null) {
                ewsSubscriptionResponse.setEdition(EwsEditionType.valueOf(entitlementCode.getEditionType().toString()));
            }

            if (entitlementCode.getNumberOfEmployeesType() != null) {
                switch (entitlementCode.getNumberOfEmployeesType()) {
                    case ONE:
                        ewsSubscriptionResponse.setTier(EwsTierType.One);
                        break;
                    case UPTO3:
                        ewsSubscriptionResponse.setTier(EwsTierType.UpTo3);
                        break;
                    case UNLIMITED:
                        ewsSubscriptionResponse.setTier(EwsTierType.Unlimited);
                }
            }

            ewsSubscriptionResponseList.add(ewsSubscriptionResponse);
        }

        return ewsQuerySubscriptionsResponse;
    }

    public static EwsCompany createEwsCompany(Company pCompany) {
        EwsCompany ewsCompany = new EwsCompany();

        ewsCompany.setEin(pCompany.getFedTaxId());
        ewsCompany.setLegalInfo(createEwsLegalInfo(pCompany.getLegalName(), pCompany.getLegalAddress()));
        ewsCompany.setDba(pCompany.getDbaName());
        ewsCompany.setMailingAddress(createEwsAddress(pCompany.getMailingAddress()));
        ewsCompany.setRealmId(pCompany.getIAMRealmId());

        if (pCompany.getQuickbooksInfo() != null) {
            EwsQuickBooks ewsQuickBooks = new EwsQuickBooks();

            ewsQuickBooks.setAppVersion(pCompany.getQuickbooksInfo().getApplicationVersion());
            ewsQuickBooks.setLicenseNumber(pCompany.getQuickbooksInfo().getLicenseNumber());

            ewsCompany.setQuickBooks(ewsQuickBooks);
        }

        for (Contact contact : pCompany.getContactCollection()) {
            switch (contact.getContactRoleCd()) {
                case PayrollAdmin:
                    ewsCompany.setPayrollAdmin(createEwsContact(contact));
                    break;
                case PrimaryPrincipal:
                    ewsCompany.setPrimaryPrincipal(createEwsContact(contact));
                    break;
                case SecondaryPrincipal:
                    ewsCompany.setSecondaryPrincipal(createEwsContact(contact));
                    break;
            }
        }

        return ewsCompany;
    }

    public static EwsCompanyResponse createEwsCompanyResponse(Company pCompany) {
        EwsCompanyResponse ewsCompanyResponse = new EwsCompanyResponse();

        ewsCompanyResponse.setEin(pCompany.getFedTaxId());
        ewsCompanyResponse.setLegalInfo(createEwsLegalInfo(pCompany.getLegalName(), pCompany.getLegalAddress()));
        ewsCompanyResponse.setDba(pCompany.getDbaName());
        ewsCompanyResponse.setPinExists(pCompany.isPINCreated());
        ewsCompanyResponse.setMailingAddress(createEwsAddress(pCompany.getMailingAddress()));
        ewsCompanyResponse.setRealmId(pCompany.getIAMRealmId());
        ewsCompanyResponse.setOnHold(pCompany.isCompanyOnHold());

        for (Contact contact : pCompany.getContactCollection()) {
            switch (contact.getContactRoleCd()) {
                case PayrollAdmin:
                    ewsCompanyResponse.setPayrollAdmin(createEwsContact(contact));
                    break;
                case PrimaryPrincipal:
                    ewsCompanyResponse.setPrimaryPrincipal(createEwsContact(contact));
                    break;
                case SecondaryPrincipal:
                    ewsCompanyResponse.setSecondaryPrincipal(createEwsContact(contact));
                    break;
            }
        }

        CompanyService companyService = pCompany.getCompanyService(ServiceCode.Tax);
        if (companyService != null) {
            TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) companyService;

            if (DeliveryPreferenceCode.Electronic.equals(taxCompanyServiceInfo.getW2DeliveryPreferenceCd())) {
                ewsCompanyResponse.setW2DeliveryPreference(EwsDeliveryType.electronic);
            } else {
                ewsCompanyResponse.setW2DeliveryPreference(EwsDeliveryType.mail);
            }

            if (DeliveryPreferenceCode.Mail.equals(taxCompanyServiceInfo.getClientPacketDeliveryPreferenceCd())) {
                ewsCompanyResponse.setClientPacketDeliveryPreference(EwsDeliveryType.mail);
            } else {
                ewsCompanyResponse.setClientPacketDeliveryPreference(EwsDeliveryType.electronic);
            }

        }

        return ewsCompanyResponse;
    }

    public static EwsContact createEwsContact(Contact pContact) {
        EwsContact ewsContact = new EwsContact();

        ewsContact.setTitle(pContact.getTitle());
        ewsContact.setFirstName(pContact.getFirstName());
        ewsContact.setMiddleName(pContact.getMiddleName());
        ewsContact.setLastName(pContact.getLastName());
        ewsContact.setTitleSuffix(pContact.getSuffix());
        ewsContact.setJobTitle(pContact.getJobTitle());
        ewsContact.seteMail(pContact.getEmail());
        ewsContact.setWorkPhone(pContact.getPhone());
        ewsContact.setHomePhone(pContact.getSecondPhone());
        ewsContact.setAuthenticationId(pContact.getIAMAuthenticationId());

        if (pContact.getMailingAddress() != null) {
            ewsContact.setAddress(createEwsAddress(pContact.getMailingAddress()));
        }

        return ewsContact;
    }

    public static EwsContact createEwsContact(ContactWSDTO pContactWSDTO) {
        EwsContact ewsContact = null;

        if (pContactWSDTO != null) {
            ewsContact = new EwsContact();

            ewsContact.setTitle(pContactWSDTO.getTitle());
            ewsContact.setFirstName(pContactWSDTO.getFirstName());
            ewsContact.setMiddleName(pContactWSDTO.getMiddleInitial());
            ewsContact.setLastName(pContactWSDTO.getLastName());
            ewsContact.setTitleSuffix(pContactWSDTO.getTitleSuffix());
            ewsContact.setJobTitle(pContactWSDTO.getJobTitle());
            ewsContact.setHomePhone(pContactWSDTO.getHomePhone());
            ewsContact.setWorkPhone(pContactWSDTO.getWorkPhone());
            ewsContact.seteMail(pContactWSDTO.getEmail());

            if (pContactWSDTO.getHomeAddress() != null) {
                ewsContact.setAddress(createEwsAddress(pContactWSDTO.getHomeAddress()));
            }
        }

        return ewsContact;
    }

    public static EwsAddress createEwsAddress(Address pAddress) {
        EwsAddress ewsAddress = new EwsAddress();

        ewsAddress.setAddressLine1(pAddress.getAddressLine1());
        ewsAddress.setAddressLine2(pAddress.getAddressLine2());
        ewsAddress.setCity(pAddress.getCity());
        ewsAddress.setState(pAddress.getState());

        if (pAddress.getZipCodeExtension() == null) {
            ewsAddress.setZip(pAddress.getZipCode());
        } else {
            ewsAddress.setZip(pAddress.getZipCode() + "-" + pAddress.getZipCodeExtension());
        }

        return ewsAddress;
    }

    public static EwsAddress createEwsAddress(AddressWSDTO pAddressWSDTO) {
        EwsAddress ewsAddress = null;

        if (pAddressWSDTO != null) {
            ewsAddress = new EwsAddress();

            ewsAddress.setAddressLine1(pAddressWSDTO.getAddress1());
            ewsAddress.setAddressLine2(pAddressWSDTO.getAddress2());
            ewsAddress.setCity(pAddressWSDTO.getCity());
            ewsAddress.setState(pAddressWSDTO.getState());
            ewsAddress.setZip(pAddressWSDTO.getZip());
        }

        return ewsAddress;
    }

    public static EwsLegalInfo createEwsLegalInfo(String pLegalName, Address pAddress) {
        EwsLegalInfo ewsLegalInfo = new EwsLegalInfo();

        EwsAddress ewsAddress = createEwsAddress(pAddress);

        ewsLegalInfo.setAddressLine1(ewsAddress.getAddressLine1());
        ewsLegalInfo.setAddressLine2(ewsAddress.getAddressLine2());
        ewsLegalInfo.setCity(ewsAddress.getCity());
        ewsLegalInfo.setState(ewsAddress.getState());
        ewsLegalInfo.setZip(ewsAddress.getZip());

        ewsLegalInfo.setLegalName(pLegalName);
        return ewsLegalInfo;
    }

    public static EwsLegalInfo createEwsLegalInfo(LegalWSDTO pLegalWSDTO) {
        EwsLegalInfo ewsLegalInfo = null;

        if (pLegalWSDTO != null) {
            ewsLegalInfo = new EwsLegalInfo();

            ewsLegalInfo.setLegalName(pLegalWSDTO.getLegalName());
            ewsLegalInfo.setAddressLine1(pLegalWSDTO.getLegalAddress1());
            ewsLegalInfo.setAddressLine2(pLegalWSDTO.getLegalAddress2());
            ewsLegalInfo.setCity(pLegalWSDTO.getLegalCity());
            ewsLegalInfo.setState(pLegalWSDTO.getLegalState());
            ewsLegalInfo.setZip(pLegalWSDTO.getLegalZip());
        }

        return ewsLegalInfo;
    }

    public static EwsPinServicesResponse createEwsPinServicesResponse(Company pCompany) throws Exception {
        EwsPinServicesResponse ewsPinServicesResponse = new EwsPinServicesResponse();

        CompanyService companyService = pCompany.getCompanyService(ServiceCode.Cloud);
        if (companyService != null) {
            EwsBaseServiceResponse ewsBaseServiceResponse = new EwsBaseServiceResponse();
            ewsPinServicesResponse.setCloudResponse(ewsBaseServiceResponse);
            ewsBaseServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
        }

        companyService = pCompany.getCompanyService(ServiceCode.BillPayment);
        if (companyService != null) {
            EwsBaseServiceResponse ewsBaseServiceResponse = new EwsBaseServiceResponse();
            ewsPinServicesResponse.setBillPaymentResponse(ewsBaseServiceResponse);
            ewsBaseServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
        }

        companyService = pCompany.getCompanyService(ServiceCode.CheckDistribution);
        if (companyService != null) {
            EwsBaseServiceResponse ewsBaseServiceResponse = new EwsBaseServiceResponse();
            ewsPinServicesResponse.setCheckDistributionResponse(ewsBaseServiceResponse);
            ewsBaseServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
        }

        companyService = pCompany.getCompanyService(ServiceCode.ThirdParty401k);
        if (companyService != null) {
            EwsBaseServiceResponse ewsBaseServiceResponse = new EwsBaseServiceResponse();
            ewsPinServicesResponse.setThirdParty401kResponse(ewsBaseServiceResponse);
            ewsBaseServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
        }

        if (isCompanyAssisted(pCompany)) {
            CompanyService ddCompanyService = pCompany.getCompanyService(ServiceCode.DirectDeposit);
            CompanyService taxCompanyService = pCompany.getCompanyService(ServiceCode.Tax);

            if (taxCompanyService != null && ddCompanyService != null) {
                EwsAssistedServiceResponse ewsAssistedServiceResponse = new EwsAssistedServiceResponse();
                ewsPinServicesResponse.setAssistedResponse(ewsAssistedServiceResponse);
                ewsAssistedServiceResponse.setStatus(EwsFactory.createEwsServiceStatusForAssisted(ddCompanyService.getStatusCd(), taxCompanyService.getStatusCd()));
            }

            if (isCompanyMigrating(pCompany)) {
                    if (companyService != null) {
                    EwsBaseServiceResponse ewsDirectDepositServiceResponse = new EwsBaseServiceResponse();
                    ewsDirectDepositServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
                        ewsPinServicesResponse.setDirectDepositResponse(ewsDirectDepositServiceResponse);
                }
            }
        } else {
            companyService = pCompany.getCompanyService(ServiceCode.DirectDeposit);
            if (companyService != null) {
                EwsBaseServiceResponse ewsDirectDepositServiceResponse = new EwsBaseServiceResponse();
                ewsDirectDepositServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
                ewsPinServicesResponse.setDirectDepositResponse(ewsDirectDepositServiceResponse);
            }
        }

        return ewsPinServicesResponse;
    }

    public static EwsServicesResponse createEwsServicesResponse(Company pCompany) throws Exception {
        EwsServicesResponse ewsServicesResponse = new EwsServicesResponse();

        CompanyService companyService = pCompany.getCompanyService(ServiceCode.Cloud);
        if (companyService != null) {
            EwsBaseServiceResponse ewsBaseServiceResponse = new EwsBaseServiceResponse();
            ewsServicesResponse.setCloudResponse(ewsBaseServiceResponse);
            ewsBaseServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
        }

        companyService = pCompany.getCompanyService(ServiceCode.BillPayment);
        if (companyService != null) {
            EwsBaseServiceResponse ewsBaseServiceResponse = new EwsBaseServiceResponse();
            ewsServicesResponse.setBillPaymentResponse(ewsBaseServiceResponse);
            ewsBaseServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
        }

        companyService = pCompany.getCompanyService(ServiceCode.CloudV2);
        if(companyService != null) {
            EwsBaseServiceResponse ewsBaseServiceResponse = new EwsAssistedServiceResponse();
            ewsServicesResponse.setCloudV2Response(ewsBaseServiceResponse);
            ewsBaseServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
        }

        companyService = pCompany.getCompanyService(ServiceCode.ViewMyPaycheck);
        if(companyService != null) {
            EwsBaseServiceResponse ewsBaseServiceResponse = new EwsAssistedServiceResponse();
            ewsServicesResponse.setViewMyPaycheckResponse(ewsBaseServiceResponse);
            ewsBaseServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
        }

        companyService = pCompany.getCompanyService(ServiceCode.WorkersComp);
        if(companyService != null) {
            EwsBaseServiceResponse ewsBaseServiceResponse = new EwsAssistedServiceResponse();
            ewsServicesResponse.setWorkersCompResponse(ewsBaseServiceResponse);
            ewsBaseServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
        }

        companyService = pCompany.getCompanyService(ServiceCode.CheckDistribution);
        if (companyService != null) {
            EwsBaseServiceResponse ewsBaseServiceResponse = new EwsBaseServiceResponse();
            ewsServicesResponse.setCheckDistributionResponse(ewsBaseServiceResponse);
            ewsBaseServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
        }

        companyService = pCompany.getCompanyService(ServiceCode.ThirdParty401k);
        if (companyService != null) {
            EwsBaseServiceResponse ewsBaseServiceResponse = new EwsBaseServiceResponse();
            ewsServicesResponse.setThirdParty401kResponse(ewsBaseServiceResponse);
            ewsBaseServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));
        }

        if (isCompanyAssisted(pCompany)) {
            TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) pCompany.getCompanyService(ServiceCode.Tax);
            if (taxCompanyServiceInfo != null) {
                CompanyBankAccount companyBankAccount = PspFactory.findCompanyBankAccount(pCompany);
                EwsAssistedServiceResponse ewsAssistedServiceResponse = EwsFactory.createEwsAssistedServiceResponse(pCompany, taxCompanyServiceInfo, companyBankAccount);
                ewsAssistedServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(taxCompanyServiceInfo.getStatusCd()));
                ewsServicesResponse.setAssistedResponse(ewsAssistedServiceResponse);
            }

            if (isCompanyMigrating(pCompany)) {
                companyService = pCompany.getCompanyService(ServiceCode.DirectDeposit);
                if (companyService != null) {
                    EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = new EwsDirectDepositServiceResponse();
                    ewsServicesResponse.setDirectDepositResponse(ewsDirectDepositServiceResponse);

                    ewsDirectDepositServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));

                    CompanyBankAccount companyBankAccount = PspFactory.findCompanyBankAccount(pCompany);
                    ewsDirectDepositServiceResponse.setEwsBankAccountResponse(EwsFactory.createEwsBankAccountResponse(companyBankAccount));
                }
            }
        } else {
            companyService = pCompany.getCompanyService(ServiceCode.DirectDeposit);
            if (companyService != null) {
                EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = new EwsDirectDepositServiceResponse();
                ewsServicesResponse.setDirectDepositResponse(ewsDirectDepositServiceResponse);

                ewsDirectDepositServiceResponse.setStatus(EwsFactory.createEwsServiceStatus(companyService.getStatusCd()));

                CompanyBankAccount companyBankAccount = PspFactory.findCompanyBankAccount(pCompany);
                ewsDirectDepositServiceResponse.setEwsBankAccountResponse(EwsFactory.createEwsBankAccountResponse(companyBankAccount));
            }
        }

        return ewsServicesResponse;
    }

    public static EwsBaseServiceResponse createEwsBaseServiceResponse(CompanyService pCompanyService) {
        EwsBaseServiceResponse ewsBaseServiceResponse = new EwsBaseServiceResponse();
        ewsBaseServiceResponse.setStatus(createEwsServiceStatus(pCompanyService.getStatusCd()));
        return ewsBaseServiceResponse;
    }

    public static EwsDirectDepositServiceResponse createEwsDirectDepositServiceResponse(CompanyService pCompanyService,
                                                                                        CompanyBankAccount pCompanyBankAccount) {
        EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = new EwsDirectDepositServiceResponse();
        ewsDirectDepositServiceResponse.setStatus(createEwsServiceStatus(pCompanyService.getStatusCd()));
        if (pCompanyBankAccount != null) {
            ewsDirectDepositServiceResponse.setEwsBankAccountResponse(createEwsBankAccountResponse(pCompanyBankAccount));
        }
        return ewsDirectDepositServiceResponse;
    }

    public static EwsAssistedServiceResponse createEwsAssistedServiceResponse(Company pCompany,
                                                                              TaxCompanyServiceInfo pTaxCompanyService,
                                                                              CompanyBankAccount pCompanyBankAccount,
                                                                              EwsCompanyResponse pEwsCompanyResponse) {

        if (DeliveryPreferenceCode.Electronic.equals(pTaxCompanyService.getW2DeliveryPreferenceCd())) {
            pEwsCompanyResponse.setW2DeliveryPreference(EwsDeliveryType.electronic);
        } else {
            pEwsCompanyResponse.setW2DeliveryPreference(EwsDeliveryType.mail);
        }

        if (DeliveryPreferenceCode.Mail.equals(pTaxCompanyService.getClientPacketDeliveryPreferenceCd())) {
            pEwsCompanyResponse.setClientPacketDeliveryPreference(EwsDeliveryType.mail);
        } else {
            pEwsCompanyResponse.setClientPacketDeliveryPreference(EwsDeliveryType.electronic);
        }

        return createEwsAssistedServiceResponse(pCompany, pTaxCompanyService, pCompanyBankAccount);
    }


    public static EwsAssistedServiceResponse createEwsAssistedServiceResponse(Company pCompany,
                                                                              TaxCompanyServiceInfo pTaxCompanyService,
                                                                              CompanyBankAccount pCompanyBankAccount) {
        EwsAssistedServiceResponse ewsAssistedServiceResponse = new EwsAssistedServiceResponse();

        CompanyService ddCompanyService = pCompany.getCompanyService(ServiceCode.DirectDeposit);
        ewsAssistedServiceResponse.setStatus(createEwsServiceStatusForAssisted(ddCompanyService.getStatusCd(), pTaxCompanyService.getStatusCd()));

        ewsAssistedServiceResponse.setEwsBankAccountResponse(createEwsBankAccountResponse(pCompanyBankAccount));

        ewsAssistedServiceResponse.setMostCurrentTaxYear(String.valueOf(pTaxCompanyService.getLastTaxYear()));

        CompanyOffering companyOffering = pCompany.getOffering(ServiceCode.DirectDeposit);
        if (companyOffering != null) {
            DomainEntitySet<CompanyOffer> companyOffers = pCompany.getActiveCompanyOffersForOffering(companyOffering.getOffering().getOfferingCode());

            OfferPrice monthlyFee = null;
            for (CompanyOffer companyOffer : companyOffers) {
                monthlyFee = companyOffer.getOffer().getAlternatePrice(OfferingServiceChargeType.MonthlyFee);
            }

            if (monthlyFee == null) {
                OfferingServiceCharge offeringServiceCharge = companyOffering.getOffering().getCharge(OfferingServiceChargeType.MonthlyFee, 1);
                if (offeringServiceCharge != null) {
                    BigDecimal bigDecimal = SpcfUtils.convertToBigDecimal(offeringServiceCharge.getCurrentPrice().getUnitPrice());
                    ewsAssistedServiceResponse.setMonthlyFee(String.format("%10.2f", bigDecimal).trim());
                } else {
                    ewsAssistedServiceResponse.setMonthlyFee(String.format("%10.2f", 0.00).trim());
                }
            } else {
                BigDecimal bigDecimal = SpcfUtils.convertToBigDecimal(monthlyFee.getAltUnitPrice());
                ewsAssistedServiceResponse.setMonthlyFee(String.format("%10.2f", bigDecimal).trim());
            }
        }

        return ewsAssistedServiceResponse;
    }

    public static EwsServiceStatus createEwsServiceStatus(ServiceSubStatusCode pServiceSubStatusCode) {
        if (pServiceSubStatusCode != null) {
            switch (pServiceSubStatusCode) {
                case Terminated:
                    return EwsServiceStatus.Terminated;
                case Cancelled:
                    return EwsServiceStatus.Cancelled;
                case PendingFirstPayroll:
                    return EwsServiceStatus.PendingFirstPayroll;
                case PendingBalanceFile:
                    return EwsServiceStatus.PendingBalanceFile;
                case PendingBankVerification:
                    return EwsServiceStatus.PendingBankVerification;
                case PendingPinCreation:
                    return EwsServiceStatus.PendingPinCreation;
                case PendingEnrollment:
                    return EwsServiceStatus.PendingEnrollment;
                case PendingSetup:
                    return EwsServiceStatus.PendingActivation;
                default:
                    return EwsServiceStatus.Active;
            }
        }

        return null;
    }

    public static EwsServiceStatus createEwsServiceStatusForAssisted(ServiceSubStatusCode pDDSubStatusCode, ServiceSubStatusCode pTaxSubStatusCode) {

        switch (pDDSubStatusCode) {
            case PendingBankVerification:
            case PendingPinCreation:
                return EwsServiceStatus.PendingActivation;
            case Terminated:
                return EwsServiceStatus.Terminated;
            default:
                switch (pTaxSubStatusCode) {
                    case PendingSetup:
                        return EwsServiceStatus.PendingActivation;
                    case PendingBalanceFile:
                        return EwsServiceStatus.PendingBalanceFile;
                    case PendingFirstPayroll:
                        return EwsServiceStatus.PendingFirstPayroll;
                    case Cancelled:
                        return EwsServiceStatus.Cancelled;
                    case Terminated:
                        return EwsServiceStatus.Terminated;
                    default:
                        return EwsServiceStatus.Active;
                }
        }
    }

    public static ArrayList<EwsEntitlementUnitResponse> createEwsEntitlementUnitResponses(DomainEntitySet<EntitlementUnit> pEntitlementUnits) {
        ArrayList<EwsEntitlementUnitResponse> ewsEntitlementUnitResponses = null;
        if (pEntitlementUnits != null && !pEntitlementUnits.isEmpty()) {
            ewsEntitlementUnitResponses = new ArrayList<EwsEntitlementUnitResponse>();
            for (EntitlementUnit entitlementUnit : pEntitlementUnits) {
                ewsEntitlementUnitResponses.add(createEwsEntitlementUnitResponse(entitlementUnit));
            }
        }
        return ewsEntitlementUnitResponses;
    }

    public static ArrayList<EwsEntitlementUnitResponse> createEwsEntitlementUnitResponses(List<EntitlementUnit> pEntitlementUnits) {
        ArrayList<EwsEntitlementUnitResponse> ewsEntitlementUnitResponses = null;
        if (pEntitlementUnits != null && !pEntitlementUnits.isEmpty()) {
            ewsEntitlementUnitResponses = new ArrayList<EwsEntitlementUnitResponse>();
            for (EntitlementUnit entitlementUnit : pEntitlementUnits) {
                if (!entitlementUnit.isDeactivated()) {
                    ewsEntitlementUnitResponses.add(createEwsEntitlementUnitResponse(entitlementUnit));
                }
            }
        }
        return ewsEntitlementUnitResponses;
    }

    public static EwsEntitlementUnitResponse createEwsEntitlementUnitResponse(EntitlementUnit pEntitlementUnit) {
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = null;
        if (pEntitlementUnit != null) {
            ewsEntitlementUnitResponse = new EwsEntitlementUnitResponse();
            ewsEntitlementUnitResponse.setServiceKey(ServiceKey.getServiceKey(pEntitlementUnit.getServiceKey()));
            ewsEntitlementUnitResponse.setDiskDeliveryKey(pEntitlementUnit.getExtensionKey());
            ewsEntitlementUnitResponse.setStatus(convertEntitlementUnitStatus(pEntitlementUnit.getEntitlementUnitStatus()));

            ewsEntitlementUnitResponse.setEwsEntitlementResponse(createEwsEntitlementResponses(pEntitlementUnit.getEntitlement(), pEntitlementUnit.getFedTaxId()));
        }
        return ewsEntitlementUnitResponse;
    }

    public static EwsEntitlementResponse createEwsEntitlementResponses(Entitlement pEntitlement, String pFedTaxId) {
        EwsEntitlementResponse ewsEntitlementResponse = null;
        if (pEntitlement != null) {
            ewsEntitlementResponse = new EwsEntitlementResponse();

            ewsEntitlementResponse.setSubscriptionNumber(pEntitlement.getSubscriptionNumber());
            ewsEntitlementResponse.setSubType(String.valueOf(pEntitlement.getEntitlementCode().getQuickBooksSubtype()));
            ewsEntitlementResponse.setBillingAccountId(pEntitlement.getCustomerId());
            ewsEntitlementResponse.setBuyerEmailAddress(pEntitlement.getContactEmail());
            ewsEntitlementResponse.setLicenseNumber(pEntitlement.getLicenseNumber());
            ewsEntitlementResponse.setEntitlementOfferingCode(pEntitlement.getEntitlementOfferingCode());
            ewsEntitlementResponse.setCancellationReason(pEntitlement.getCancellationReason());
            ewsEntitlementResponse.setAssetItemNumber(pEntitlement.getEntitlementCode().getAssetItemNumber());

            ewsEntitlementResponse.setHasMultipleActiveEINs(containsMultipleActiveEINs(pFedTaxId, pEntitlement));

            switch (pEntitlement.getEntitlementState()) {
                case Enabled:
                    ewsEntitlementResponse.setState(EwsEntitlementStateCode.Enabled);
                    break;
                case Disabled:
                    ewsEntitlementResponse.setState(EwsEntitlementStateCode.Disabled);
                    break;
            }
        }
        return ewsEntitlementResponse;
    }

    public static boolean containsMultipleActiveEINs(String currentFEIN, Entitlement pEntitlement) {
        List<String> feinList = new ArrayList<String>();
        feinList.add(currentFEIN);

        for (EntitlementUnit entitlementUnit : pEntitlement.getActiveEntitlementUnitCollection()) {
            if (!feinList.contains(entitlementUnit.getFedTaxId()))
                feinList.add(entitlementUnit.getFedTaxId());
        }
        return feinList.size() > 1;
    }

    public static EwsBankAccountResponse createEwsBankAccountResponse(CompanyBankAccount pCompanyBankAccount) {
        EwsBankAccountResponse ewsBankAccountResponse = new EwsBankAccountResponse();

        BankAccount bankAccount = pCompanyBankAccount.getBankAccount();

        ewsBankAccountResponse.setAccountNumber(bankAccount.getAccountNumber());
        ewsBankAccountResponse.setRoutingNumber(bankAccount.getRoutingNumber());
        ewsBankAccountResponse.setBankName(bankAccount.getBankName());
        switch (bankAccount.getAccountTypeCd()) {
            case Checking:
                ewsBankAccountResponse.setAccountType(EwsBankAccountType.Checking);
                break;
            case Savings:
                ewsBankAccountResponse.setAccountType(EwsBankAccountType.Savings);
        }

        ewsBankAccountResponse.setQuickBooksName(pCompanyBankAccount.getSourceBankAccountName());

        SpcfCalendar lastRetryDate = pCompanyBankAccount.getLastRetryDate();
        if (lastRetryDate != null) {
            ewsBankAccountResponse.setLastRetryDateTime(CalendarUtils.convertToCalendar(lastRetryDate.toLocal()));
        }

        switch (pCompanyBankAccount.getStatusCd()) {
            case Inactive:
            case Active:
                ewsBankAccountResponse.setVerificationStatus(EwsBankVerificationStatus.Verified);
                break;
            case PendingVerification:
                ewsBankAccountResponse.setVerificationStatus(EwsBankVerificationStatus.New);
        }

        int verificationAttemptLimit = Integer.parseInt(LimitRule.findLimitRule(pCompanyBankAccount.getCompany(), ServiceCode.DirectDeposit)
                                                 .findLimitValueByName(LimitValueType.CompanyBankAccountVerificationAttemptLimit).getValue());

        if (pCompanyBankAccount.getVerifyRetryCount() >= verificationAttemptLimit) {
            ewsBankAccountResponse.setRetries(false);
        } else {
            ewsBankAccountResponse.setRetries(true);
        }

        FinancialTransaction createdTransaction = PspFactory.findFinancialTransactionExcludingType
                (pCompanyBankAccount, TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created);
        if (createdTransaction != null) {
            ewsBankAccountResponse.setPendingPayrollExists(true);
        } else {
            ewsBankAccountResponse.setPendingPayrollExists(false);
        }

        DomainEntitySet<FinancialTransaction> verificationTransactions = pCompanyBankAccount.getVerificationTransactions();
        if (verificationTransactions.isNotEmpty()) {
            SpcfCalendar createdDate = verificationTransactions.getFirst().getCreatedDate();
            ewsBankAccountResponse.setRandomDebitDateTime(CalendarUtils.convertToCalendar(createdDate.toLocal()));
        }

        ewsBankAccountResponse.setHoldReason(null);

        return ewsBankAccountResponse;
    }

    public static EwsBankAccountResponse createEwsBankAccountResponse(PSIMessageWSDTO pPSIMessageWSDTO) {
        EwsBankAccountResponse ewsBankAccountResponse = new EwsBankAccountResponse();

        BankWSDTO bankWSDTO = pPSIMessageWSDTO.getFeature().getBank();

        ewsBankAccountResponse.setAccountNumber(bankWSDTO.getBankAccountNumber());
        ewsBankAccountResponse.setRoutingNumber(bankWSDTO.getBankRoutingNumber());
        ewsBankAccountResponse.setQuickBooksName(bankWSDTO.getBankAccountQBName());
        ewsBankAccountResponse.setBankName(bankWSDTO.getBankName());

        if (bankWSDTO.getBankAccountType() != null) {
            switch (bankWSDTO.getBankAccountType()) {
                case CHECKING:
                    ewsBankAccountResponse.setAccountType(EwsBankAccountType.Checking);
                    break;
                case SAVINGS:
                    ewsBankAccountResponse.setAccountType(EwsBankAccountType.Savings);
            }
        }

        if (bankWSDTO.getNoOfRetries() != null) {
            switch (bankWSDTO.getBankVerificationStatus()) {
                case Verified:
                    ewsBankAccountResponse.setVerificationStatus(EwsBankVerificationStatus.Verified);
                    break;
                case New:
                    ewsBankAccountResponse.setVerificationStatus(EwsBankVerificationStatus.New);
            }
        }

        LimitRule limitRule = LimitRule.findLimitRule(Company.findCompany(pPSIMessageWSDTO.getCompany().getPsid(), SourceSystemCode.QBDT), ServiceCode.DirectDeposit);
        if(limitRule != null) {
            int verificationAttemptLimit = Integer.parseInt(limitRule.findLimitValueByName(LimitValueType.CompanyBankAccountVerificationAttemptLimit).getValue());
        if (bankWSDTO.getNoOfRetries() != null) {
            if (bankWSDTO.getNoOfRetries() != null) {
                int noOfRetries = Integer.valueOf(bankWSDTO.getNoOfRetries());
                    if (noOfRetries >= verificationAttemptLimit) {
                    ewsBankAccountResponse.setRetries(false);
                } else {
                    ewsBankAccountResponse.setRetries(true);
                }
            }
        }
        } else {
            ewsBankAccountResponse.setRetries(true);
        }

        if (bankWSDTO.getPendingPayrollExists() != null) {
            switch (bankWSDTO.getPendingPayrollExists()) {
                case Y:
                    ewsBankAccountResponse.setPendingPayrollExists(true);
                    break;
                case N:
                    ewsBankAccountResponse.setPendingPayrollExists(false);
            }
        }

        ewsBankAccountResponse.setLastRetryDateTime(bankWSDTO.getLastRetryDateTime());
        ewsBankAccountResponse.setRandomDebitDateTime(bankWSDTO.getRandomDebitDateTime());

        if (bankWSDTO.getHoldReason() != null) {
            ewsBankAccountResponse.setHoldReason(bankWSDTO.getHoldReason().toString());
        }

        return ewsBankAccountResponse;
    }

    public static EwsResetPinResponse createEwsResetPinResponse(Company pCompany, HashMap<String, String> pHashMap) {
        EwsResetPinResponse ewsResetPinResponse = new EwsResetPinResponse();

        ewsResetPinResponse.setPsid(pCompany.getSourceCompanyId());
        ewsResetPinResponse.setPin(pHashMap.get("PIN"));
        ewsResetPinResponse.setPrivateKey(pHashMap.get("PrivateKey"));

        return ewsResetPinResponse;
    }

    public static EwsBasePinResponse createEwsBasePinResponse(Company pCompany, HashMap<String, String> pHashMap) throws Exception {
        EwsBasePinResponse ewsCreatePinResponse = new EwsBasePinResponse();

        ewsCreatePinResponse.setPsid(pCompany.getSourceCompanyId());
        ewsCreatePinResponse.setPrivateKey(pHashMap.get("PrivateKey"));

        ewsCreatePinResponse.setServicesResponse(createEwsPinServicesResponse(pCompany));

        return ewsCreatePinResponse;
    }

    public static EwsBasePinResponse createEwsBasePinResponse(Company pCompany) throws Exception{
        EwsBasePinResponse ewsBasePinResponse = new EwsBasePinResponse();

        ewsBasePinResponse.setPsid(pCompany.getSourceCompanyId());
        ewsBasePinResponse.setPrivateKey(pCompany.getPrivateKey());
        ewsBasePinResponse.setServicesResponse(createEwsPinServicesResponse(pCompany));

        return ewsBasePinResponse;
    }

    public static EwsBankResponse createEwsBankResponse(CompanyBankAccount pCompanyBankAccount) {
        EwsBankResponse ewsBankResponse = new EwsBankResponse();
        Company company = pCompanyBankAccount.getCompany();
        ewsBankResponse.setPsid(company.getSourceCompanyId());

        EwsBankServicesResponse ewsBankServicesResponse = new EwsBankServicesResponse();
        ewsBankResponse.setEwsBankServicesResponse(ewsBankServicesResponse);

        if (isCompanyAssisted(company)) {
            ewsBankServicesResponse.setEwsBankAssistedServiceResponse(createEwsBankAssistedServiceResponse(company, pCompanyBankAccount));
        } else {
            ewsBankServicesResponse.setEwsBankDirectDepositServiceResponse(createEwsBankDirectDepositServiceResponse(company, pCompanyBankAccount));
        }

        return ewsBankResponse;
    }

    public static EwsBankAssistedServiceResponse createEwsBankAssistedServiceResponse(Company pCompany, CompanyBankAccount pCompanyBankAccount) {
        EwsBankAssistedServiceResponse response = new EwsBankAssistedServiceResponse();

        ServiceSubStatusCode ddServiceStatus =  pCompany.getCompanyService(ServiceCode.DirectDeposit).getStatusCd();
        ServiceSubStatusCode taxServiceStatus = pCompany.getCompanyService(ServiceCode.Tax).getStatusCd();
        response.setStatus(createEwsServiceStatusForAssisted(ddServiceStatus, taxServiceStatus));

        response.setEwsBaseBankAccountResponse(createEwsBaseBankAccountResponse(pCompanyBankAccount));

        return response;
    }

    public static EwsBankDirectDepositServiceResponse createEwsBankDirectDepositServiceResponse(Company pCompany, CompanyBankAccount pCompanyBankAccount) {
        EwsBankDirectDepositServiceResponse response = new EwsBankDirectDepositServiceResponse();

        ServiceSubStatusCode ddServiceStatus =  pCompany.getCompanyService(ServiceCode.DirectDeposit).getStatusCd();
        response.setStatus(EwsFactory.createEwsServiceStatus(ddServiceStatus));

        response.setEwsBaseBankAccountResponse(createEwsBaseBankAccountResponse(pCompanyBankAccount));

        return response;
    }

    public static EwsBaseBankAccountResponse createEwsBaseBankAccountResponse(CompanyBankAccount pCompanyBankAccount) {
        EwsBaseBankAccountResponse response = new EwsBaseBankAccountResponse();

        SpcfCalendar lastRetryDate = pCompanyBankAccount.getLastRetryDate();
        if (lastRetryDate != null) {
            response.setLastRetryDateTime(CalendarUtils.convertToCalendar(lastRetryDate.toLocal()));
        }

        switch (pCompanyBankAccount.getStatusCd()) {
            case Inactive:
            case Active:
                response.setVerificationStatus(EwsBankVerificationStatus.Verified);
                break;
            case PendingVerification:
                response.setVerificationStatus(EwsBankVerificationStatus.New);
        }

        LimitRule limitRule = LimitRule.findLimitRule(pCompanyBankAccount.getCompany(), ServiceCode.DirectDeposit);
        if(limitRule != null) {
            int verificationAttemptLimit = Integer.parseInt(limitRule.findLimitValueByName(LimitValueType.CompanyBankAccountVerificationAttemptLimit).getValue());
            if (pCompanyBankAccount.getVerifyRetryCount() >= verificationAttemptLimit) {
                response.setRetries(false);
            } else {
                response.setRetries(true);
            }
        }

        return response;
    }

    public static void updateEwsResponse(EwsMessage pEwsMessage, EwsResponse pEwsResponse) {

        EwsResponseStatus ewsResponseStatus = pEwsResponse.getEwsResponseStatus();
        ewsResponseStatus.setCode(pEwsMessage.getCode());
        ewsResponseStatus.setMessage(pEwsMessage.getMessage());
    }

    public static void updatePayrollInfoWSDTO(EwsMessage pEwsMessage, PayrollInfoWSDTO pResponse) {
        ErrorWSDTO errorWSDTO = new ErrorWSDTO();
        errorWSDTO.setCode(String.valueOf(pEwsMessage.getCode()));
        errorWSDTO.setDescription(pEwsMessage.getMessage());

        pResponse.setPayrollStatusWSDTO(new PayrollStatusWSDTO());
        pResponse.getPayrollStatusWSDTO().setErrorWSDTO(errorWSDTO);
    }

    public static EwsEntitlementUnitStatusCode convertEntitlementUnitStatus(EntitlementUnitStatusCode pEntitlementUnitStatusCode) {
        if (pEntitlementUnitStatusCode != null) {
            switch(pEntitlementUnitStatusCode) {
                case Activated:
                case ErrorActivating:
                case PendingActivation:
                case PendingReactivation:
                case ActivationHold:

                    return EwsEntitlementUnitStatusCode.Activated;

                case Deactivated:
                case ErrorDeactivating:
                case PendingDeactivation:
                case DeactivationHold:
                case Historic:
                    return EwsEntitlementUnitStatusCode.Deactivated;
            }
        }

        return EwsEntitlementUnitStatusCode.Deactivated;
    }

    public static boolean isCompanyAssisted(Company pCompany) {
        CompanyService taxService = pCompany.getCompanyService(ServiceCode.Tax);
        CompanyService ddService = pCompany.getCompanyService(ServiceCode.DirectDeposit);

        if (taxService != null && ddService != null) {
            if (!taxService.isCancelTerm() && !ddService.isCancelTerm()) {
                return true;
            } else
            if (taxService.isCancelTerm() && !ddService.isCancelTerm()) {
                return false;
            } else {
                Offering offering = Offering.findDefaultOffering(pCompany, ServiceCode.DirectDeposit);
                if (offering.getOfferingCode().in(OfferingCode.DIYDDSTD, OfferingCode.DIYDDSTD3,OfferingCode.DIYDDFY14,OfferingCode.DIYDDFY143,OfferingCode.DIYDDFY15,OfferingCode.DIYDDFY153,OfferingCode.DIYDDFY16,OfferingCode.DIYDDFY163)) {
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            return false;
        }
    }

    public static boolean isCompanyMigrating(Company pCompany) {
        CompanyService taxService = pCompany.getCompanyService(ServiceCode.Tax);
        CompanyService ddService = pCompany.getCompanyService(ServiceCode.DirectDeposit);

        if (taxService != null && ddService != null) {
            if (taxService.isPending() && ddService.isActive()) {
                return true;
            }
        }
        return false;
    }
}
