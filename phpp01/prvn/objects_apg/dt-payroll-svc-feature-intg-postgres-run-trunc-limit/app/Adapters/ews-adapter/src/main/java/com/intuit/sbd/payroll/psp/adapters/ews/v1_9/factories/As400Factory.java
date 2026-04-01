package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.common.utils.ServiceKey;
import com.intuit.sbd.payroll.psp.domain.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jeff Jones
 */
public class As400Factory {

    private static List<String> mAppIdList;

    static {
        mAppIdList = new ArrayList<String>();
        mAppIdList.add("QBW");
        mAppIdList.add("QBWPRO");
    }

    public static PSIMessageWSDTO createCreatePin(EwsBasePin pEwsBasePin, Company pCompany) throws Exception {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        psiMessageWSDTO.setAction(ActionEnum.CREATE_PIN);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        companyWSDTO.setPsid(pEwsBasePin.getPsid());
        companyWSDTO.setPin(pEwsBasePin.getPin().toUpperCase());
        psiMessageWSDTO.setCompany(companyWSDTO);

        FeatureWSDTO featureWSDTO = new FeatureWSDTO();
        psiMessageWSDTO.setFeature(featureWSDTO);

        AgreementWSDTO agreementWSDTO = new AgreementWSDTO();
        featureWSDTO.setAgreement(agreementWSDTO);

        EntitlementUnit entitlementUnit = pCompany.findEnabledEntitlementUnitByAssetItemCd(AssetItemCode.Assisted, AssetItemCode.AssistedAdvantage);
        if (entitlementUnit == null) {
            throw new EwsException(EwsMessages.missingAssistedEntitlement());
        }
        agreementWSDTO.setSubscriptionNum(entitlementUnit.getEntitlement().getSubscriptionNumber());

        return psiMessageWSDTO;
    }

    public static PSIMessageWSDTO createAuthenticatePin(EwsBasePin pEwsBasePin, Company pCompany) throws Exception {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        psiMessageWSDTO.setAction(ActionEnum.AUTHENTICATE_PIN);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        companyWSDTO.setPsid(pEwsBasePin.getPsid());
        companyWSDTO.setPin(pEwsBasePin.getPin().toUpperCase());
        psiMessageWSDTO.setCompany(companyWSDTO);

        FeatureWSDTO featureWSDTO = new FeatureWSDTO();
        psiMessageWSDTO.setFeature(featureWSDTO);

        AgreementWSDTO agreementWSDTO = new AgreementWSDTO();
        featureWSDTO.setAgreement(agreementWSDTO);

        EntitlementUnit entitlementUnit = pCompany.findEnabledEntitlementUnitByAssetItemCd(AssetItemCode.Assisted, AssetItemCode.AssistedAdvantage);
        if (entitlementUnit == null) {
            throw new EwsException(EwsMessages.missingAssistedEntitlement());
        }
        agreementWSDTO.setSubscriptionNum(entitlementUnit.getEntitlement().getSubscriptionNumber());

        return psiMessageWSDTO;
    }

    public static PSIMessageWSDTO createUpdatePin(EwsUpdatePin pEwsUpdatePin, Company pCompany) throws Exception {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        psiMessageWSDTO.setAction(ActionEnum.UPDATE_PIN);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        companyWSDTO.setPsid(pEwsUpdatePin.getPsid());
        companyWSDTO.setPin(pEwsUpdatePin.getPin().toUpperCase());
        companyWSDTO.setOldPIN(pEwsUpdatePin.getOldPin().toUpperCase());
        psiMessageWSDTO.setCompany(companyWSDTO);

        FeatureWSDTO featureWSDTO = new FeatureWSDTO();
        psiMessageWSDTO.setFeature(featureWSDTO);

        AgreementWSDTO agreementWSDTO = new AgreementWSDTO();
        featureWSDTO.setAgreement(agreementWSDTO);

        EntitlementUnit entitlementUnit = pCompany.findEnabledEntitlementUnitByAssetItemCd(AssetItemCode.Assisted, AssetItemCode.AssistedAdvantage);
        if (entitlementUnit == null) {
            throw new EwsException(EwsMessages.missingAssistedEntitlement());
        }
        agreementWSDTO.setSubscriptionNum(entitlementUnit.getEntitlement().getSubscriptionNumber());

        return psiMessageWSDTO;
    }

    public static PSIMessageWSDTO createValidateBank(EwsValidateBank pEwsValidateBank, String pSubscriptionNumber) {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        EwsValidateBankAccount ewsValidateBankAccount =
                pEwsValidateBank.getEwsValidateBankServices().getEwsValidateBankAssistedService().getEwsValidateBankAccount();

        psiMessageWSDTO.setAction(ActionEnum.VALID_BANK);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        companyWSDTO.setPsid(pEwsValidateBank.getPsid());
        psiMessageWSDTO.setCompany(companyWSDTO);

        FeatureWSDTO featureWSDTO = new FeatureWSDTO();
        psiMessageWSDTO.setFeature(featureWSDTO);

        AgreementWSDTO agreementWSDTO = new AgreementWSDTO();
        featureWSDTO.setAgreement(agreementWSDTO);
        agreementWSDTO.setSubscriptionNum(pSubscriptionNumber);

        BankWSDTO bankWSDTO = new BankWSDTO();
        bankWSDTO.setRandomDollar1(ewsValidateBankAccount.getRandomDebit1());
        bankWSDTO.setRandomDollar2(ewsValidateBankAccount.getRandomDebit2());
        featureWSDTO.setBank(bankWSDTO);

        return psiMessageWSDTO;
    }

    public static PSIMessageWSDTO createUpdateBank(EwsUpdateBank pEwsUpdateBank, Company pCompany) throws Exception {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        EwsBankAccount ewsBankAccount = pEwsUpdateBank.getEwsUpdateBankServices().getEwsUpdateBankAssistedService().getEwsBankAccount();

        psiMessageWSDTO.setAction(ActionEnum.UPDATE_BANK);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        companyWSDTO.setPsid(pEwsUpdateBank.getPsid());
        psiMessageWSDTO.setCompany(companyWSDTO);

        FeatureWSDTO featureWSDTO = new FeatureWSDTO();
        psiMessageWSDTO.setFeature(featureWSDTO);

        BankWSDTO bankWSDTO = new BankWSDTO();
        bankWSDTO.setBankAccountNumber(ewsBankAccount.getAccountNumber());
        bankWSDTO.setBankRoutingNumber(ewsBankAccount.getRoutingNumber());
        bankWSDTO.setBankName(ewsBankAccount.getBankName());
        bankWSDTO.setBankAccountQBName(ewsBankAccount.getQuickBooksName());

        switch (ewsBankAccount.getAccountType()) {
            case Checking:
                bankWSDTO.setBankAccountType(BankAccountTypeEnum.CHECKING);
                break;
            case Savings:
                bankWSDTO.setBankAccountType(BankAccountTypeEnum.SAVINGS);
        }

        featureWSDTO.setBank(bankWSDTO);

        AgreementWSDTO agreementWSDTO = new AgreementWSDTO();
        featureWSDTO.setAgreement(agreementWSDTO);

        EntitlementUnit entitlementUnit = pCompany.findEnabledEntitlementUnitByAssetItemCd(AssetItemCode.Assisted, AssetItemCode.AssistedAdvantage);
        if (entitlementUnit == null) {
            throw new EwsException(EwsMessages.missingAssistedEntitlement());
        }
        agreementWSDTO.setSubscriptionNum(entitlementUnit.getEntitlement().getSubscriptionNumber());

        return psiMessageWSDTO;
    }

    public static PSIMessageWSDTO createUpdateAccount(EwsUpdateAccount pEwsUpdateAccount, Company pCompany) throws Exception {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        psiMessageWSDTO.setAction(ActionEnum.UPDATE_ACCOUNT);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        psiMessageWSDTO.setCompany(companyWSDTO);


        //Company Info
        EwsCompany ewsUpdateCompany = pEwsUpdateAccount.getEwsCompany();
        companyWSDTO.setPsid(pEwsUpdateAccount.getPsid());
        companyWSDTO.setEin(ewsUpdateCompany.getEin());
        companyWSDTO.setDba(ewsUpdateCompany.getDba());

        if (ewsUpdateCompany.getClientPacketDeliveryPreference() != null) {
            switch (ewsUpdateCompany.getClientPacketDeliveryPreference()) {
                case electronic:
                    companyWSDTO.setClientPacketDeliveryPref(DeliveryEnum.electronic);
                    break;
                case mail:
                    companyWSDTO.setClientPacketDeliveryPref(DeliveryEnum.mail);
            }
        }

        if (ewsUpdateCompany.getW2DeliveryPreference() != null) {
            switch (ewsUpdateCompany.getW2DeliveryPreference()) {
                case electronic:
                    companyWSDTO.setW2DeliveryPref(DeliveryEnum.electronic);
                    break;
                case mail:
                    companyWSDTO.setW2DeliveryPref(DeliveryEnum.mail);
            }
        }

        companyWSDTO.setPayrollAdmin(createContactWSDTO(ewsUpdateCompany.getPayrollAdmin()));

        EwsLegalInfo ewsLegalInfo = ewsUpdateCompany.getLegalInfo();
        companyWSDTO.setLegalInfo(createLegalWSDTO(ewsLegalInfo));

        if (ewsUpdateCompany.getMailingAddress() != null) {
            companyWSDTO.setMailingAddress(createAddressWSDTO(ewsUpdateCompany.getMailingAddress()));
            companyWSDTO.setShippingAddress(createAddressWSDTO(ewsUpdateCompany.getMailingAddress()));
        } else {
            companyWSDTO.setMailingAddress(createAddressWSDTO(ewsLegalInfo));
            companyWSDTO.setShippingAddress(createAddressWSDTO(ewsLegalInfo));
        }

        companyWSDTO.setQuickBooks(createQuickBooksWSDTO(ewsUpdateCompany.getQuickBooks()));
        if (ewsUpdateCompany.getQuickBooks() != null) {
            companyWSDTO.setLicenceNumber(ewsUpdateCompany.getQuickBooks().getLicenseNumber());
        }        

        //Feature Info
        FeatureWSDTO featureWSDTO = new FeatureWSDTO();
        psiMessageWSDTO.setFeature(featureWSDTO);

        //Agreement Info
        EntitlementUnit entitlementUnit = pCompany.findEnabledEntitlementUnitByAssetItemCd(AssetItemCode.Assisted, AssetItemCode.AssistedAdvantage);

        if (entitlementUnit == null) {
            throw new EwsException(EwsMessages.missingAssistedEntitlement());
        }

        AgreementWSDTO agreementWSDTO = new AgreementWSDTO();
        featureWSDTO.setAgreement(agreementWSDTO);

        //Removed because this is not being set in pre 10.1 prod.
        //agreementWSDTO.setServiceType(String.valueOf(serviceKey.charAt(0)));

        String serviceKey = ServiceKey.getServiceKey(entitlementUnit.getServiceKey());
        agreementWSDTO.setServiceKey(serviceKey);
        agreementWSDTO.setSubscriptionNum(entitlementUnit.getEntitlement().getSubscriptionNumber());

        if (pCompany.getPriceType() != null)
            agreementWSDTO.setPriceType(pCompany.getPriceType());

        agreementWSDTO.setSubType(entitlementUnit.getEntitlement().getEntitlementCode().getSubtypeDescription());
        //Workaround until the AS400 codes their side to accept the new subtypes. Remove ASAP.
        if ("QB Payroll Assisted Refund".equals(agreementWSDTO.getSubType())) {
            agreementWSDTO.setSubType("QB Payroll Assisted");
        } else
        if ("QB Payroll Assisted Adv Refund".equals(agreementWSDTO.getSubType())) {
            agreementWSDTO.setSubType("QB Payroll Assisted Adv");
        }

        agreementWSDTO.setAddEIN(false);

        return psiMessageWSDTO;
    }

    public static PSIMessageWSDTO createCreateAccount(EwsCreateAccount pEwsCreateAccount,
                                                      DomainEntitySet<EntitlementUnit> pEntitlementUnits,
                                                      Company pCompany,
                                                      boolean pAddEin) throws Exception {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        psiMessageWSDTO.setAction(ActionEnum.CREATE_ACCOUNT);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        psiMessageWSDTO.setCompany(companyWSDTO);

        //Company Info
        EwsCompany ewsCompany = pEwsCreateAccount.getEwsCompany();
        companyWSDTO.setEin(ewsCompany.getEin());
        companyWSDTO.setDba(ewsCompany.getDba());

        if (ewsCompany.getClientPacketDeliveryPreference() != null) {
            switch (ewsCompany.getClientPacketDeliveryPreference()) {
                case electronic:
                    companyWSDTO.setClientPacketDeliveryPref(DeliveryEnum.electronic);
                    break;
                case mail:
                    companyWSDTO.setClientPacketDeliveryPref(DeliveryEnum.mail);
            }
        }

        if (ewsCompany.getW2DeliveryPreference() != null) {
            switch (ewsCompany.getW2DeliveryPreference()) {
                case electronic:
                    companyWSDTO.setW2DeliveryPref(DeliveryEnum.electronic);
                    break;
                case mail:
                    companyWSDTO.setW2DeliveryPref(DeliveryEnum.mail);
            }
        }

        companyWSDTO.setPayrollAdmin(createContactWSDTO(ewsCompany.getPayrollAdmin()));
        companyWSDTO.setPrimaryPrincipal(createContactWSDTO(ewsCompany.getPrimaryPrincipal()));
        companyWSDTO.setSecondaryPrincipal(createContactWSDTO(ewsCompany.getSecondaryPrincipal()));

        EwsLegalInfo ewsLegalInfo = ewsCompany.getLegalInfo();
        companyWSDTO.setLegalInfo(createLegalWSDTO(ewsLegalInfo));

        if (ewsCompany.getMailingAddress() != null) {
            companyWSDTO.setMailingAddress(createAddressWSDTO(ewsCompany.getMailingAddress()));
            companyWSDTO.setShippingAddress(createAddressWSDTO(ewsCompany.getMailingAddress()));
        } else {
            companyWSDTO.setMailingAddress(createAddressWSDTO(ewsLegalInfo));
            companyWSDTO.setShippingAddress(createAddressWSDTO(ewsLegalInfo));
        }

        companyWSDTO.setQuickBooks(createQuickBooksWSDTO(ewsCompany.getQuickBooks()));
        companyWSDTO.setLicenceNumber(ewsCompany.getQuickBooks().getLicenseNumber());

        //Feature Info
        FeatureWSDTO featureWSDTO = new FeatureWSDTO();
        psiMessageWSDTO.setFeature(featureWSDTO);

/*        for (CompanyOffer companyOffer : pCompany.getCompanyOffers().sort(CompanyOffer.<CompanyOffer>CreatedDate().Descending())) {
            if (companyOffer.companyOfferIsActive()) {
                featureWSDTO.setOfferCode(companyOffer.getOffer().getOfferCd());
            }
        }*/

        EwsAssistedService ewsAssistedService = pEwsCreateAccount.getEwsServices().getAssistedService();
        EwsBankAccount ewsBankAccount = ewsAssistedService.getEwsBankAccount();
        BankWSDTO bankWSDTO = new BankWSDTO();
        featureWSDTO.setBank(bankWSDTO);

        //Bank Info
        bankWSDTO.setBankAccountNumber(ewsBankAccount.getAccountNumber());
        bankWSDTO.setBankRoutingNumber(ewsBankAccount.getRoutingNumber());
        bankWSDTO.setBankName(ewsBankAccount.getBankName());
        bankWSDTO.setBankAccountQBName(ewsBankAccount.getQuickBooksName());

        switch (ewsBankAccount.getAccountType()) {
            case Checking:
                bankWSDTO.setBankAccountType(BankAccountTypeEnum.CHECKING);
                break;
            case Savings:
                bankWSDTO.setBankAccountType(BankAccountTypeEnum.SAVINGS);
        }

        EntitlementUnit entitlementUnit = null;
        for (EntitlementUnit eu : pEntitlementUnits) {
            if (eu.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.Assisted) ||
                    eu.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.AssistedAdvantage)) {
                entitlementUnit = eu;
                break;
            }
        }

        if (entitlementUnit == null) {
            throw new EwsException(EwsMessages.missingAssistedEntitlement());
        }

        AgreementWSDTO agreementWSDTO = new AgreementWSDTO();
        featureWSDTO.setAgreement(agreementWSDTO);

        //Removed because this is not being set in pre 10.1 prod.
        //agreementWSDTO.setServiceType(String.valueOf(serviceKey.charAt(0)));

        String serviceKey = ServiceKey.getServiceKey(entitlementUnit.getServiceKey());
        agreementWSDTO.setServiceKey(serviceKey);
        agreementWSDTO.setSubscriptionNum(entitlementUnit.getEntitlement().getSubscriptionNumber());

        agreementWSDTO.setSubType(entitlementUnit.getEntitlement().getEntitlementCode().getSubtypeDescription());
        //Workaround until the AS400 codes their side to accept the new subtypes. Remove ASAP.
        if ("QB Payroll Assisted Refund".equals(agreementWSDTO.getSubType())) {
            agreementWSDTO.setSubType("QB Payroll Assisted");
        } else
        if ("QB Payroll Assisted Adv Refund".equals(agreementWSDTO.getSubType())) {
            agreementWSDTO.setSubType("QB Payroll Assisted Adv");
        }

        agreementWSDTO.setAddEIN(pAddEin);

        return psiMessageWSDTO;
    }

    public static PSIMessageWSDTO createCreateAccount(EwsAddService pEwsAddService,
                                                      Company pCompany,
                                                      boolean pAddEin) throws Exception {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        psiMessageWSDTO.setAction(ActionEnum.CREATE_ACCOUNT);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        psiMessageWSDTO.setCompany(companyWSDTO);

        //Company Info
        companyWSDTO.setEin(pCompany.getFedTaxId());
        companyWSDTO.setDba(pCompany.getDbaName());

        for (Contact contact : pCompany.getContactCollection()) {
            if (ContactRole.PayrollAdmin.equals(contact.getContactRoleCd())) {
                companyWSDTO.setPayrollAdmin(createContactWSDTO(contact));
                continue;
            }

            if (ContactRole.PrimaryPrincipal.equals(contact.getContactRoleCd())) {
                companyWSDTO.setPrimaryPrincipal(createContactWSDTO(contact));
                continue;
            }

            if (ContactRole.SecondaryPrincipal.equals(contact.getContactRoleCd())) {
                companyWSDTO.setSecondaryPrincipal(createContactWSDTO(contact));
            }
        }

        if (pCompany.getLegalAddress() != null) {
            companyWSDTO.setLegalInfo(createLegalWSDTO(pCompany.getLegalAddress(), pCompany.getLegalName()));
        } else {
            companyWSDTO.setLegalInfo(createLegalWSDTO(pCompany.getMailingAddress(), pCompany.getLegalName()));
        }

        if (pCompany.getMailingAddress() != null) {
            companyWSDTO.setMailingAddress(createAddressWSDTO(pCompany.getMailingAddress()));
            companyWSDTO.setShippingAddress(createAddressWSDTO(pCompany.getMailingAddress()));
        } else {
            companyWSDTO.setMailingAddress(createAddressWSDTO(pCompany.getLegalAddress()));
            companyWSDTO.setShippingAddress(createAddressWSDTO(pCompany.getLegalAddress()));
        }

        if (pCompany.getQuickbooksInfo() != null) {
            companyWSDTO.setQuickBooks(createQuickBooksWSDTO(pCompany.getQuickbooksInfo()));
            companyWSDTO.setLicenceNumber(pCompany.getQuickbooksInfo().getLicenseNumber());
        }

        //Feature Info
        FeatureWSDTO featureWSDTO = new FeatureWSDTO();
        psiMessageWSDTO.setFeature(featureWSDTO);

/*        for (CompanyOffer companyOffer : pCompany.getCompanyOffers().sort(CompanyOffer.<CompanyOffer>CreatedDate().Descending())) {
            if (companyOffer.companyOfferIsActive()) {
                featureWSDTO.setOfferCode(companyOffer.getOffer().getOfferCd());
            }
        }*/

        EwsAssistedService ewsAssistedService = pEwsAddService.getEwsBaseServices().getAssistedService();
        EwsBankAccount ewsBankAccount = ewsAssistedService.getEwsBankAccount();
        BankWSDTO bankWSDTO = new BankWSDTO();
        featureWSDTO.setBank(bankWSDTO);

        //Bank Info
        bankWSDTO.setBankAccountNumber(ewsBankAccount.getAccountNumber());
        bankWSDTO.setBankRoutingNumber(ewsBankAccount.getRoutingNumber());
        bankWSDTO.setBankName(ewsBankAccount.getBankName());
        bankWSDTO.setBankAccountQBName(ewsBankAccount.getQuickBooksName());

        switch (ewsBankAccount.getAccountType()) {
            case Checking:
                bankWSDTO.setBankAccountType(BankAccountTypeEnum.CHECKING);
                break;
            case Savings:
                bankWSDTO.setBankAccountType(BankAccountTypeEnum.SAVINGS);
        }

        EntitlementUnit entitlementUnit = null;
        for (EntitlementUnit eu : pCompany.getEntitlementUnitCollection()) {
            if (eu.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.Assisted) ||
                    eu.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.AssistedAdvantage)) {
                entitlementUnit = eu;
                break;
            }
        }

        if (entitlementUnit == null) {
            throw new EwsException(EwsMessages.missingAssistedEntitlement());
        }

        AgreementWSDTO agreementWSDTO = new AgreementWSDTO();
        featureWSDTO.setAgreement(agreementWSDTO);

        //Removed because this is not being set in pre 10.1 prod.
        //agreementWSDTO.setServiceType(String.valueOf(serviceKey.charAt(0)));

        String serviceKey = ServiceKey.getServiceKey(entitlementUnit.getServiceKey());
        agreementWSDTO.setServiceKey(serviceKey);
        agreementWSDTO.setSubscriptionNum(entitlementUnit.getEntitlement().getSubscriptionNumber());

        if (pCompany.getPriceType() != null)
            agreementWSDTO.setPriceType(pCompany.getPriceType());

        agreementWSDTO.setSubType(entitlementUnit.getEntitlement().getEntitlementCode().getSubtypeDescription());
        //Workaround until the AS400 codes their side to accept the new subtypes. Remove ASAP.
        if ("QB Payroll Assisted Refund".equals(agreementWSDTO.getSubType())) {
            agreementWSDTO.setSubType("QB Payroll Assisted");
        } else
        if ("QB Payroll Assisted Adv Refund".equals(agreementWSDTO.getSubType())) {
            agreementWSDTO.setSubType("QB Payroll Assisted Adv");
        }

        agreementWSDTO.setAddEIN(pAddEin);

        return psiMessageWSDTO;
    }

    public static PSIMessageWSDTO createMigrateAccount(EwsMigrateAccount pEwsMigrateAccount,
                                                       DomainEntitySet<EntitlementUnit> pEntitlementUnits,
                                                       Company pCompany,
                                                       boolean pAddEin) throws Exception {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        psiMessageWSDTO.setAction(ActionEnum.PSP_MIGRATE_ACCOUNT);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        psiMessageWSDTO.setCompany(companyWSDTO);

        //Company Info
        EwsCompany ewsCompany = pEwsMigrateAccount.getEwsCompany();
        companyWSDTO.setPsid(pEwsMigrateAccount.getPsid());
        companyWSDTO.setEin(ewsCompany.getEin());
        companyWSDTO.setDba(ewsCompany.getDba());

        if (ewsCompany.getClientPacketDeliveryPreference() != null) {
            switch (ewsCompany.getClientPacketDeliveryPreference()) {
                case electronic:
                    companyWSDTO.setClientPacketDeliveryPref(DeliveryEnum.electronic);
                    break;
                case mail:
                    companyWSDTO.setClientPacketDeliveryPref(DeliveryEnum.mail);
            }
        }

        if (ewsCompany.getW2DeliveryPreference() != null) {
            switch (ewsCompany.getW2DeliveryPreference()) {
                case electronic:
                    companyWSDTO.setW2DeliveryPref(DeliveryEnum.electronic);
                    break;
                case mail:
                    companyWSDTO.setW2DeliveryPref(DeliveryEnum.mail);
            }
        }

        companyWSDTO.setPayrollAdmin(createContactWSDTO(ewsCompany.getPayrollAdmin()));
        companyWSDTO.setPrimaryPrincipal(createContactWSDTO(ewsCompany.getPrimaryPrincipal()));
        companyWSDTO.setSecondaryPrincipal(createContactWSDTO(ewsCompany.getSecondaryPrincipal()));

        EwsLegalInfo ewsLegalInfo = ewsCompany.getLegalInfo();
        companyWSDTO.setLegalInfo(createLegalWSDTO(ewsLegalInfo));

        if (ewsCompany.getMailingAddress() != null) {
            companyWSDTO.setMailingAddress(createAddressWSDTO(ewsCompany.getMailingAddress()));
            companyWSDTO.setShippingAddress(createAddressWSDTO(ewsCompany.getMailingAddress()));
        } else {
            companyWSDTO.setMailingAddress(createAddressWSDTO(ewsLegalInfo));
            companyWSDTO.setShippingAddress(createAddressWSDTO(ewsLegalInfo));
        }

        companyWSDTO.setQuickBooks(createQuickBooksWSDTO(ewsCompany.getQuickBooks()));
        companyWSDTO.setLicenceNumber(ewsCompany.getQuickBooks().getLicenseNumber());

        //Feature Info
        FeatureWSDTO featureWSDTO = new FeatureWSDTO();
        psiMessageWSDTO.setFeature(featureWSDTO);

/*        for (CompanyOffer companyOffer : pCompany.getCompanyOffers().sort(CompanyOffer.<CompanyOffer>CreatedDate().Descending())) {
            if (companyOffer.companyOfferIsActive()) {
                featureWSDTO.setOfferCode(companyOffer.getOffer().getOfferCd());
            }
        }*/

        EwsAssistedService ewsAssistedService = pEwsMigrateAccount.getEwsServices().getAssistedService();
        EwsBankAccount ewsBankAccount = ewsAssistedService.getEwsBankAccount();
        BankWSDTO bankWSDTO = new BankWSDTO();
        featureWSDTO.setBank(bankWSDTO);

        //Bank Info
        bankWSDTO.setBankAccountNumber(ewsBankAccount.getAccountNumber());
        bankWSDTO.setBankRoutingNumber(ewsBankAccount.getRoutingNumber());
        bankWSDTO.setBankName(ewsBankAccount.getBankName());
        bankWSDTO.setBankAccountQBName(ewsBankAccount.getQuickBooksName());

        switch (ewsBankAccount.getAccountType()) {
            case Checking:
                bankWSDTO.setBankAccountType(BankAccountTypeEnum.CHECKING);
                break;
            case Savings:
                bankWSDTO.setBankAccountType(BankAccountTypeEnum.SAVINGS);
        }

        EntitlementUnit entitlementUnit = null;
        for (EntitlementUnit eu : pEntitlementUnits) {
            if (eu.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.Assisted) ||
                    eu.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.AssistedAdvantage)) {
                entitlementUnit = eu;
                break;
            }
        }

        if (entitlementUnit == null) {
            throw new EwsException(EwsMessages.missingAssistedEntitlement());
        }

        AgreementWSDTO agreementWSDTO = new AgreementWSDTO();
        featureWSDTO.setAgreement(agreementWSDTO);

        //Removed because this is not being set in pre 10.1 prod.
        //agreementWSDTO.setServiceType(String.valueOf(serviceKey.charAt(0)));

        String serviceKey = ServiceKey.getServiceKey(entitlementUnit.getServiceKey());
        agreementWSDTO.setServiceKey(serviceKey);
        agreementWSDTO.setSubscriptionNum(entitlementUnit.getEntitlement().getSubscriptionNumber());

        if (pCompany.getPriceType() != null)
            agreementWSDTO.setPriceType(pCompany.getPriceType());

        agreementWSDTO.setSubType(entitlementUnit.getEntitlement().getEntitlementCode().getSubtypeDescription());
        //Workaround until the AS400 codes their side to accept the new subtypes. Remove ASAP.
        if ("QB Payroll Assisted Refund".equals(agreementWSDTO.getSubType())) {
            agreementWSDTO.setSubType("QB Payroll Assisted");
        } else
        if ("QB Payroll Assisted Adv Refund".equals(agreementWSDTO.getSubType())) {
            agreementWSDTO.setSubType("QB Payroll Assisted Adv");
        }

        agreementWSDTO.setAddEIN(pAddEin);

        return psiMessageWSDTO;
    }

    public static PSIMessageWSDTO createMigrateAccount(EwsAddService pEwsAddService,
                                                       List<EntitlementUnit> pEntitlementUnits,
                                                       Company pCompany,
                                                       boolean pAddEin) throws Exception {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        psiMessageWSDTO.setAction(ActionEnum.PSP_MIGRATE_ACCOUNT);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        psiMessageWSDTO.setCompany(companyWSDTO);

        //Company Info
        companyWSDTO.setPsid(pEwsAddService.getPsid());
        companyWSDTO.setEin(pCompany.getFedTaxId());
        companyWSDTO.setDba(pCompany.getDbaName());
        companyWSDTO.setClientPacketDeliveryPref(DeliveryEnum.electronic);
        companyWSDTO.setW2DeliveryPref(DeliveryEnum.mail);

        Contact contact = pCompany.getContactByRoleCode(ContactRole.PayrollAdmin);
        companyWSDTO.setPayrollAdmin(createContactWSDTO(contact));

        contact = pCompany.getContactByRoleCode(ContactRole.PrimaryPrincipal);
        companyWSDTO.setPrimaryPrincipal(createContactWSDTO(contact));

        contact = pCompany.getContactByRoleCode(ContactRole.SecondaryPrincipal);
        companyWSDTO.setSecondaryPrincipal(createContactWSDTO(contact));

        companyWSDTO.setLegalInfo(createLegalWSDTO(pCompany.getLegalAddress(), pCompany.getLegalName()));

        if (pCompany.getMailingAddress() != null) {
            companyWSDTO.setMailingAddress(createAddressWSDTO(pCompany.getMailingAddress()));
            companyWSDTO.setShippingAddress(createAddressWSDTO(pCompany.getMailingAddress()));
        } else {
            companyWSDTO.setMailingAddress(createAddressWSDTO(pCompany.getLegalAddress()));
            companyWSDTO.setShippingAddress(createAddressWSDTO(pCompany.getLegalAddress()));
        }

        companyWSDTO.setQuickBooks(createQuickBooksWSDTO(pCompany.getQuickbooksInfo()));
        companyWSDTO.setLicenceNumber(pCompany.getQuickbooksInfo().getLicenseNumber());

        //Feature Info
        FeatureWSDTO featureWSDTO = new FeatureWSDTO();
        psiMessageWSDTO.setFeature(featureWSDTO);

/*        for (CompanyOffer companyOffer : pCompany.getCompanyOffers().sort(CompanyOffer.<CompanyOffer>CreatedDate().Descending())) {
            if (companyOffer.companyOfferIsActive()) {
                featureWSDTO.setOfferCode(companyOffer.getOffer().getOfferCd());
            }
        }*/

        EwsAssistedService ewsAssistedService = pEwsAddService.getEwsBaseServices().getAssistedService();
        EwsBankAccount ewsBankAccount = ewsAssistedService.getEwsBankAccount();
        BankWSDTO bankWSDTO = new BankWSDTO();
        featureWSDTO.setBank(bankWSDTO);

        //Bank Info
        bankWSDTO.setBankAccountNumber(ewsBankAccount.getAccountNumber());
        bankWSDTO.setBankRoutingNumber(ewsBankAccount.getRoutingNumber());
        bankWSDTO.setBankName(ewsBankAccount.getBankName());
        bankWSDTO.setBankAccountQBName(ewsBankAccount.getQuickBooksName());

        switch (ewsBankAccount.getAccountType()) {
            case Checking:
                bankWSDTO.setBankAccountType(BankAccountTypeEnum.CHECKING);
                break;
            case Savings:
                bankWSDTO.setBankAccountType(BankAccountTypeEnum.SAVINGS);
        }

        EntitlementUnit entitlementUnit = null;
        for (EntitlementUnit eu : pEntitlementUnits) {
            if (eu.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.Assisted) ||
                    eu.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.AssistedAdvantage)) {
                entitlementUnit = eu;
                break;
            }
        }

        if (entitlementUnit == null) {
            throw new EwsException(EwsMessages.missingAssistedEntitlement());
        }

        AgreementWSDTO agreementWSDTO = new AgreementWSDTO();
        featureWSDTO.setAgreement(agreementWSDTO);

        //Removed because this is not being set in pre 10.1 prod.
        //agreementWSDTO.setServiceType(String.valueOf(serviceKey.charAt(0)));

        String serviceKey = ServiceKey.getServiceKey(entitlementUnit.getServiceKey());
        agreementWSDTO.setServiceKey(serviceKey);
        agreementWSDTO.setSubscriptionNum(entitlementUnit.getEntitlement().getSubscriptionNumber());

        if (pCompany.getPriceType() != null)
            agreementWSDTO.setPriceType(pCompany.getPriceType());

        agreementWSDTO.setSubType(entitlementUnit.getEntitlement().getEntitlementCode().getSubtypeDescription());
        //Workaround until the AS400 codes their side to accept the new subtypes. Remove ASAP.
        if ("QB Payroll Assisted Refund".equals(agreementWSDTO.getSubType())) {
            agreementWSDTO.setSubType("QB Payroll Assisted");
        } else
        if ("QB Payroll Assisted Adv Refund".equals(agreementWSDTO.getSubType())) {
            agreementWSDTO.setSubType("QB Payroll Assisted Adv");
        }

        agreementWSDTO.setAddEIN(pAddEin);

        return psiMessageWSDTO;
    }

    private static ContactWSDTO createContactWSDTO(Contact pContact) {
        if (pContact == null) {
            return null;
        }

        ContactWSDTO contactWSDTO = new ContactWSDTO();

        contactWSDTO.setTitle(pContact.getTitle());
        contactWSDTO.setFirstName(pContact.getFirstName());
        contactWSDTO.setMiddleInitial(pContact.getMiddleName());
        contactWSDTO.setLastName(pContact.getLastName());
        contactWSDTO.setTitleSuffix(pContact.getSuffix());
        contactWSDTO.setJobTitle(pContact.getJobTitle());
        contactWSDTO.setEmail(pContact.getEmail());
        contactWSDTO.setWorkPhone(pContact.getPhone());
        contactWSDTO.setHomePhone(pContact.getSecondPhone());
        contactWSDTO.setHomeAddress(createAddressWSDTO(pContact.getMailingAddress()));

        return contactWSDTO;
    }

    private static ContactWSDTO createContactWSDTO(EwsContact pEwsContact) {
        if (pEwsContact == null) {
            return null;
        }

        ContactWSDTO contactWSDTO = new ContactWSDTO();

        contactWSDTO.setTitle(pEwsContact.getTitle());
        contactWSDTO.setFirstName(pEwsContact.getFirstName());
        contactWSDTO.setMiddleInitial(pEwsContact.getMiddleName());
        contactWSDTO.setLastName(pEwsContact.getLastName());
        contactWSDTO.setTitleSuffix(pEwsContact.getTitleSuffix());
        contactWSDTO.setJobTitle(pEwsContact.getJobTitle());
        contactWSDTO.setEmail(pEwsContact.geteMail());
        contactWSDTO.setWorkPhone(pEwsContact.getWorkPhone());
        contactWSDTO.setHomePhone(pEwsContact.getHomePhone());
        contactWSDTO.setHomeAddress(createAddressWSDTO(pEwsContact.getAddress()));

        return contactWSDTO;
    }

    private static LegalWSDTO createLegalWSDTO(Address pAddress, String pLegalName) {
        if (pAddress == null) {
            return null;
        }

        LegalWSDTO legalWSDTO = new LegalWSDTO();

        legalWSDTO.setLegalName(pLegalName);
        legalWSDTO.setLegalAddress1(pAddress.getAddressLine1());
        legalWSDTO.setLegalAddress2(pAddress.getAddressLine2());
        legalWSDTO.setLegalCity(pAddress.getCity());
        legalWSDTO.setLegalState(pAddress.getState());
        legalWSDTO.setLegalZip(pAddress.getZipCode());

        return legalWSDTO;
    }

    private static LegalWSDTO createLegalWSDTO(EwsLegalInfo pEwsLegalInfo) {
        if (pEwsLegalInfo == null) {
            return null;
        }

        LegalWSDTO legalWSDTO = new LegalWSDTO();

        legalWSDTO.setLegalName(pEwsLegalInfo.getLegalName());
        legalWSDTO.setLegalAddress1(pEwsLegalInfo.getAddressLine1());
        legalWSDTO.setLegalAddress2(pEwsLegalInfo.getAddressLine2());
        legalWSDTO.setLegalCity(pEwsLegalInfo.getCity());
        legalWSDTO.setLegalState(pEwsLegalInfo.getState());
        legalWSDTO.setLegalZip(pEwsLegalInfo.getZip());

        return legalWSDTO;
    }

    private static AddressWSDTO createAddressWSDTO(Address pAddress) {
        if (pAddress == null) {
            return null;
        }

        AddressWSDTO addressWSDTO = new AddressWSDTO();

        addressWSDTO.setAddress1(pAddress.getAddressLine1());
        addressWSDTO.setAddress2(pAddress.getAddressLine2());
        addressWSDTO.setCity(pAddress.getCity());
        addressWSDTO.setState(pAddress.getState());
        addressWSDTO.setZip(pAddress.getZipCode());

        return addressWSDTO;
    }

    private static AddressWSDTO createAddressWSDTO(EwsAddress pEwsAddress) {
        if (pEwsAddress == null) {
            return null;
        }

        AddressWSDTO addressWSDTO = new AddressWSDTO();

        addressWSDTO.setAddress1(pEwsAddress.getAddressLine1());
        addressWSDTO.setAddress2(pEwsAddress.getAddressLine2());
        addressWSDTO.setCity(pEwsAddress.getCity());
        addressWSDTO.setState(pEwsAddress.getState());
        addressWSDTO.setZip(pEwsAddress.getZip());

        return addressWSDTO;
    }

    public static QuickBooksWSDTO createQuickBooksWSDTO(QuickbooksInfo pQuickbooksInfo) {
        if (pQuickbooksInfo == null) {
            return null;
        }
        QuickBooksWSDTO quickBooksWSDTO = new QuickBooksWSDTO();

        String appVer = "";
        if (pQuickbooksInfo.getApplicationVersion() != null) {
            appVer = pQuickbooksInfo.getApplicationVersion();
            if (appVer.contains("/")) {
                appVer = appVer.substring(0, appVer.indexOf("/"));
            }
        }
        if (pQuickbooksInfo.getTaxTableId() != null) {
            appVer += "/" + pQuickbooksInfo.getTaxTableId();
        }
        quickBooksWSDTO.setAppVer(appVer);

        if (mAppIdList.contains(pQuickbooksInfo.getApplicationId())) {
        quickBooksWSDTO.setAppId(pQuickbooksInfo.getApplicationId());
        }

        return quickBooksWSDTO;
    }

    private static QuickBooksWSDTO createQuickBooksWSDTO(EwsQuickBooks pEwsQuickBooks) {
        if (pEwsQuickBooks == null) {
            return null;
        }

        QuickBooksWSDTO quickBooksWSDTO = new QuickBooksWSDTO();

        quickBooksWSDTO.setAppVer(pEwsQuickBooks.getAppVersion());

        return quickBooksWSDTO;
    }

    public static PSIMessageWSDTO createQueryOffer(String pOfferCode) {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        psiMessageWSDTO.setAction(ActionEnum.QUERY_OFFER);

        OfferWSDTO offerWSDTO = new OfferWSDTO();
        psiMessageWSDTO.setOffer(offerWSDTO);

        offerWSDTO.setOfferCode(pOfferCode);

        return psiMessageWSDTO;
    }

    public static PSIMessageWSDTO createActivateFeature(String pPSID, String pSubscriptionNumber) {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        psiMessageWSDTO.setAction(ActionEnum.ACTIVATE_FEATURE);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        companyWSDTO.setPsid(pPSID);
        psiMessageWSDTO.setCompany(companyWSDTO);

        FeatureWSDTO featureWSDTO = new FeatureWSDTO();
        featureWSDTO.setFeatureName(FeatureEnum.Assisted);
        featureWSDTO.setStatus(StatusEnum.PP);
        featureWSDTO.setOnHold(false);
        psiMessageWSDTO.setFeature(featureWSDTO);

        AgreementWSDTO agreementWSDTO = new AgreementWSDTO();
        agreementWSDTO.setSubscriptionNum(pSubscriptionNumber);
        featureWSDTO.setAgreement(agreementWSDTO);

        return psiMessageWSDTO;
    }

    public static PSIMessageWSDTO createQueryAccount(String pPSID, String pEIN) {
        PSIMessageWSDTO psiMessageWSDTO = new PSIMessageWSDTO();

        psiMessageWSDTO.setAction(ActionEnum.QUERY_ACCT);

        CompanyWSDTO companyWSDTO = new CompanyWSDTO();
        psiMessageWSDTO.setCompany(companyWSDTO);

        if (pPSID != null && pPSID.length() > 0) {
            companyWSDTO.setPsid(pPSID);
        } else
        if (pEIN != null && pEIN.length() > 0) {
            companyWSDTO.setEin(pEIN);
        }

        return psiMessageWSDTO;
    }
}
