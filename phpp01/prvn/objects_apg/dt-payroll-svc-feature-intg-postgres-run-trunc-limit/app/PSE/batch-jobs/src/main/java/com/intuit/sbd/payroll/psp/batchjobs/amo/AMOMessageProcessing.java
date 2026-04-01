package com.intuit.sbd.payroll.psp.batchjobs.amo;

import com.intuit.iep.abcsimpl.commoncomponents.v1.CCInfoType;
import com.intuit.iep.abcsimpl.commoncomponents.v1.EFTInfoType;
import com.intuit.iep.abcsimpl.commoncomponents.v1.TransactionAttributeType;
import com.intuit.iep.billingprofile.billingprofileabo.v1.BillingProfileType;
import com.intuit.iep.customeraccount.customeraccountbase.v1.*;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.amo.GetCustomerAssetResponseTypeDTO;
import com.intuit.sbd.payroll.psp.gateways.amo.SyncCustomerAssetDataAreaTypeDTO;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 7, 2010
 * Time: 2:58:12 PM
 */
public class AMOMessageProcessing {
    private static final String ONE_MONTH_ASSET_ITEM_NUMBER = "1101754";

    private static SpcfLogger logger = Application.getLogger(AMOMessageProcessing.class);

    public static void processAssetUpdates(EntitlementDTO pEntitlementDTO, SyncCustomerAssetDataAreaTypeDTO pSyncCustomerAssetDataAreaTypeDTO) throws Throwable {

        // skip messages with a transaction date after the last message timestamp - grace period
        // **Note: if there is no transaction time the message will be processed
        if(pSyncCustomerAssetDataAreaTypeDTO.getTransactionDatetime() != null) {
            if(pEntitlementDTO.getLastMessageTimestamp() == null) {
                pEntitlementDTO.setLastMessageTimestamp(pSyncCustomerAssetDataAreaTypeDTO.getTransactionDatetime());
            } else {
                // the grace period allows for messages that may have been placed on the queue out of order, with in reason
                int gracePeriod = SystemParameter.findIntValue(SystemParameter.Code.AMO_MESSAGE_TIMESTAMP_GRACE_PERIOD, 10);
                SpcfCalendar lastMessageTimestamp = pEntitlementDTO.getLastMessageTimestamp().copy();
                lastMessageTimestamp.addSeconds(-gracePeriod);
                if(lastMessageTimestamp.before(pSyncCustomerAssetDataAreaTypeDTO.getTransactionDatetime())) {
                    pEntitlementDTO.setLastMessageTimestamp(pSyncCustomerAssetDataAreaTypeDTO.getTransactionDatetime());
                } else {
                    pSyncCustomerAssetDataAreaTypeDTO.setEntitlementMessageStatusCode(EntitlementMessageStatusCode.SkippedOldTimestamp);
                    return;
                }
            }
        }

        AssetChangeReasonType assetChangeReasonType = null;
        try {
            assetChangeReasonType = AssetChangeReasonType.valueOf(pSyncCustomerAssetDataAreaTypeDTO.getEventReason());
        } catch (Exception e) {
            //Unknown Event Reason, Don't throw an error
        }

        SyncCustomerAssetDataAreaType syncCustomerAssetDataAreaType = pSyncCustomerAssetDataAreaTypeDTO.getSyncCustomerAssetDataAreaType();

        for (AssetType assetType : syncCustomerAssetDataAreaType.getSyncCustomerAsset().getAsset()) {
            if (assetType.getItem() != null && assetType.getItem().getId().getValue() != null) {
                EntitlementCode entitlementCode = EntitlementCode.findEntitlementCode(assetType.getItem().getId().getValue());
                if (entitlementCode != null && AssetTypeCode.Trial.equals(entitlementCode.getAssetTypeCd())) {
                    pEntitlementDTO.setTrialAssociated(true);
                }
            }
        }

        for (AssetType assetType : syncCustomerAssetDataAreaType.getSyncCustomerAsset().getAsset()) {

            String eoc = pEntitlementDTO.getEntitlementOfferingCode();
            String license = pEntitlementDTO.getLicenseNumber();

            if(assetType.getEntitlement() != null){
                if(assetMatches(assetType, license, eoc)) {
                    //Skip assets with asset item numbers that are not in PSP
                    if(assetType.getItem() != null && assetType.getItem().getId().getValue() != null) {
                        String assetItemNumber = assetType.getItem().getId().getValue();
                        EntitlementCode entitlementCode = EntitlementCode.findEntitlementCode(assetItemNumber);
                        if (entitlementCode != null) {
                            switch (entitlementCode.getAssetTypeCd()) {
                                case Usage:
                                    processUsageAssetUpdates(pEntitlementDTO, assetChangeReasonType, assetType);
                                    break;
                                default:
                                    processPayrollAssetUpdates(pEntitlementDTO, assetChangeReasonType, syncCustomerAssetDataAreaType, assetType);
                            }
                        }
                    }
                }
            }
        }

        pSyncCustomerAssetDataAreaTypeDTO.setEntitlementMessageStatusCode(EntitlementMessageStatusCode.Processed);
    }

    private static void processPayrollAssetUpdates(EntitlementDTO pEntitlementDTO,
                                                   AssetChangeReasonType pAssetChangeReasonType,
                                                   SyncCustomerAssetDataAreaType pSyncCustomerAssetDataAreaType,
                                                   AssetType pAssetType) {

        Map<String, EditionType> stringToEditionType = EntitlementDTO.getEditionValues();
        Map<String, NumberOfEmployeesType> stringToNumberOfEmployeesType = EntitlementDTO.getNumberOfEmployeesValues();

        // find the corresponding profile ids for the asset
        String billingProfileId = null;
        String assetContactId = null;

        if(pAssetType.getStatus() == AssetStatusType.CANCELLED) {
            pEntitlementDTO.setEntitlementState(EntitlementStateCode.Disabled);
        } else if(pAssetType.getStatus() == AssetStatusType.ACTIVE) {
            pEntitlementDTO.setEntitlementState(EntitlementStateCode.Enabled);
        }

        if(pAssetType.getBillingProfileId() != null && pAssetType.getBillingProfileId().getValue() != null) {
            billingProfileId = pAssetType.getBillingProfileId().getValue();
        }

        if(pAssetType.getAssetContactId() != null && pAssetType.getAssetContactId().getValue() != null) {
            //PSP-17076 Get valid asset contact id
            assetContactId = GetCustomerAssetResponseTypeDTO.getValidAssetContactId(pAssetType.getAssetContactId().getValue(),pSyncCustomerAssetDataAreaType.getSyncCustomerAsset().getCustomerAccount());
        }

        if(pAssetType.getEntitlement().getEntitlementState() != null) {
            pEntitlementDTO.setEntitlementState(EntitlementStateCode.valueOf(pAssetType.getEntitlement().getEntitlementState().value()));
        }

        //The asset should not change unless
        if(pAssetType.getItem() != null && pAssetType.getItem().getId().getValue() != null) {
            //if (pEntitlementDTO.getAssetItemNumber() == null) {
            pEntitlementDTO.setAssetItemNumber(pAssetType.getItem().getId().getValue());
            //}
        }

        if(pAssetType.getEntitlement().getEntitlementStartDate() != null) {
            pEntitlementDTO.setSubscriptionStartDate(CalendarUtils.createInstanceFromXMLGregorianCalendar(pAssetType.getEntitlement().getEntitlementStartDate()));
        }

        if(pAssetType.getOrderInfo() != null) {
            OrderInfoType orderInfoType = pAssetType.getOrderInfo();
            if(orderInfoType != null) {
                pEntitlementDTO.setOrderNumber(orderInfoType.getOrderNumber());
                if(orderInfoType.getLineItem() != null && orderInfoType.getLineItem().getActionReason() != null) {
                    pEntitlementDTO.setCancellationReason(orderInfoType.getLineItem().getActionReason());
                }
            }
        }

        //AMO will create the element set its value to nil when we need to set the next charge date to null
        if(pAssetType.getNextChargeDate() != null && pAssetType.getNextChargeDate().getValue() != null) {
            pEntitlementDTO.setNextChargeDate(CalendarUtils.createInstanceFromXMLGregorianCalendar(pAssetType.getNextChargeDate().getValue()));
        }

        // AMO sends the customer id as blank sometimes
        if(pAssetType.getServiceAccountNumber() != null &&
                pAssetType.getServiceAccountNumber().getValue() != null &&
                !pAssetType.getServiceAccountNumber().getValue().trim().equals("")) {
            pEntitlementDTO.setCustomerId(pAssetType.getServiceAccountNumber().getValue());
        }

        for (TransactionAttributeType transactionAttributeType : pAssetType.getTransactionAttributes()) {
            String transactionAttributeValue = transactionAttributeType.getValue();
            if(transactionAttributeValue != null && transactionAttributeValue.length() > 0) {
                if(editionElementName().equals(transactionAttributeType.getName())) {
                    pEntitlementDTO.setEditionType(stringToEditionType.get(transactionAttributeValue));
                } else if(numberOfEmployeesElementName().equals(transactionAttributeType.getName())) {
                    pEntitlementDTO.setNumberOfEmployeesType(stringToNumberOfEmployeesType.get(transactionAttributeValue));
                }
            }
        }

        for (EntitlementType.EntitlementUnit entitlementUnit : pAssetType.getEntitlement().getEntitlementUnit()) {
            EntitlementUnitStatusCode entitlementUnitStatusCode = null;
            try {
                entitlementUnitStatusCode = EntitlementUnitStatusCode.valueOf(entitlementUnit.getUnitState().value());
            } catch (IllegalArgumentException e) {
                logger.error("Skipped invalid entitlement unit status. Status:" + entitlementUnit.getUnitState().value() + " Lic/EOC: " + pEntitlementDTO.getLicenseNumber() + "/" + pEntitlementDTO.getEntitlementOfferingCode());
            }

            if(entitlementUnitStatusCode != null) {
                pEntitlementDTO.getEntitlementUnitStatuses().put(entitlementUnit.getIdentifiedResourceValue(), EntitlementUnitStatusCode.valueOf(entitlementUnit.getUnitState().value()));
            }
        }

        if (pAssetChangeReasonType != null) {
            switch (pAssetChangeReasonType) {

                //Set subscription end date to PSPDate for payment failures and disabled entitlements to trigger OSCAR.
                case CHARGEBACK_NOTICE:
                case PAYMENT_FAILURE_NOTICE:
                case ENTITLEMENT_DISABLEMENT:
                    if (pEntitlementDTO.getSubscriptionEndDate() == null) {
                        pEntitlementDTO.setSubscriptionEndDate(PSPDate.getPSPTime());
                    }
                    break;

                //Set subscription end date to null for successful payments and enabled entitlements to disable OSCAR.
                case PAYMENT_SUCCESS_NOTICE:
                case ENTITLEMENT_ENABLEMENT:
                    pEntitlementDTO.setSubscriptionEndDate(null);
                    break;

                case ENTITLEMENT_UNIT_ACTIVATION:
                    Entitlement entitlement = Entitlement.findEntitlement(pEntitlementDTO.getLicenseNumber(), pEntitlementDTO.getEntitlementOfferingCode());

                    if (entitlement.getEntitlementCode().getIsUsageBilling()) {
                        for (EntitlementUnit entitlementUnit : entitlement.getActiveEntitlementUnitCollection()) {
                            DomainEntitySet<CompanyEventEmail> welcomeEmails = CompanyEventEmail.findEmailEventsByCompanyAndTemplate(entitlementUnit.getCompany(),
                                    EventEmailTemplateTypeCode.SymphonyWelcomeFreeTrial,
                                    EventEmailTemplateTypeCode.SymphonyWelcomeNoTrial,
                                    EventEmailTemplateTypeCode.SymphonyWelcomeOneMonthReactivation);
                            //Prevent multiple emails from going out and sending emails to older entitlement units
                            SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
                            spcfCalendar.addDays(-7);
                            if (welcomeEmails.isEmpty() && entitlementUnit.getCreatedDate().after(spcfCalendar)) {
                                if (ONE_MONTH_ASSET_ITEM_NUMBER.equals(pEntitlementDTO.getAssetItemNumber())) {
                                    //30 day welcome
                                    CompanyEvent.createWelcomeEmailEvent(entitlementUnit, EventEmailTemplateTypeCode.SymphonyWelcomeOneMonthReactivation);
                                } else if (pEntitlementDTO.getTrialAssociated() && pEntitlementDTO.getSubscriptionStartDate() != null && PSPDate.getPSPTime().before(pEntitlementDTO.getSubscriptionStartDate())) {
                                    //Trial Welcome
                                    CompanyEvent.createWelcomeEmailEvent(entitlementUnit, EventEmailTemplateTypeCode.SymphonyWelcomeFreeTrial);
                                } else {
                                    //Normal Welcome
                                    CompanyEvent.createWelcomeEmailEvent(entitlementUnit, EventEmailTemplateTypeCode.SymphonyWelcomeNoTrial);
                                }
                            }
                        }
                    }
                    break;
                default:
                    //Do Nothing
            }
        }

        String billingAddressId = processAssetBillingUpdates(pEntitlementDTO, pSyncCustomerAssetDataAreaType.getSyncCustomerAsset().getBillingProfile(), billingProfileId);

        processAssetContactAndAddressUpdates(pEntitlementDTO, pSyncCustomerAssetDataAreaType.getSyncCustomerAsset().getCustomerAccount(), assetContactId, billingAddressId);
    }


    private static void processUsageAssetUpdates(EntitlementDTO pEntitlementDTO, AssetChangeReasonType pAssetChangeReasonType, AssetType pAssetType) {
        if (pAssetChangeReasonType != null) {
            switch (pAssetChangeReasonType) {

                //Set subscription end date to PSPDate for payment failures and disabled entitlements to trigger OSCAR.
                case CHARGEBACK_NOTICE:
                case PAYMENT_FAILURE_NOTICE:
                case ENTITLEMENT_DISABLEMENT:
                    SpcfCalendar calendar = PSPDate.getPSPTime();
                    if (ItemType.USAGE.equals(pAssetType.getItemType())) {
                        //Set the date to -30 days to trigger OSCAR without a grace period
                        calendar.addDays(-30);
                    }
                    pEntitlementDTO.setSubscriptionEndDate(calendar);
                    break;

                //Set subscription end date to null for successful payments and enabled entitlements to disable OSCAR.
                case PAYMENT_SUCCESS_NOTICE:
                case ENTITLEMENT_ENABLEMENT:
                    pEntitlementDTO.setSubscriptionEndDate(null);
                    break;
                default:
                    //Do Nothing
            }
        }
    }

    /**
     * Update all of the asset's billing related information including:
     * payment type, credit card number, credit card type, and credit card expiration
     *
     * Returns billing profile address id which is used to find and populate the billing zip code
     *
     * @param pEntitlementDTO - domain update dto
     * @param pBillingProfiles - billing profiles
     * @param pBillingProfileId - billing profile id
     * @return Billing profile address id
     */
    private static String processAssetBillingUpdates(EntitlementDTO pEntitlementDTO, List<BillingProfileType> pBillingProfiles, String pBillingProfileId) {
        if(pBillingProfileId == null) {
            return null;
        }

        for (BillingProfileType billingProfileType : pBillingProfiles) {
            if(billingProfileType.getBillingProfileId() != null && pBillingProfileId.equals(billingProfileType.getBillingProfileId().getValue())) {
                boolean isWalletEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.NGP_WALLET, false);
                if (!isWalletEnabled){
                    if (billingProfileType.getPaymentMethod() != null) {
                        CCInfoType ccInfoType = billingProfileType.getPaymentMethod().getCCInfo();
                        EFTInfoType eftInfoType = billingProfileType.getPaymentMethod().getEFTInfo();

                        // process credit card information
                        if (ccInfoType != null) {
                            logger.info("settingPaymentMethType :" + EntitlementPaymentMethodType.CC);
                            pEntitlementDTO.setPaymentMethodType(EntitlementPaymentMethodType.CC);
                            pEntitlementDTO.setCreditCardExpiration((ccInfoType.getExpirationMonth().intValue() >= 10 ? "" : "0") + ccInfoType.getExpirationMonth() + "/" + ccInfoType.getExpirationYear());

                            if (ccInfoType.getAccountType() != null) {
                                logger.info("settingCreditType :" + ccInfoType.getAccountType().toString());
                                pEntitlementDTO.setCreditCardType(ccInfoType.getAccountType().toString());
                            } else {
                                pEntitlementDTO.setCreditCardType(null);
                            }

                            String accountNumber = ccInfoType.getAccountNumber();
                            if (accountNumber != null && accountNumber.length() > 4) {
                                accountNumber = accountNumber.substring(accountNumber.length() - 4, accountNumber.length());
                                pEntitlementDTO.setCreditCardNumber(accountNumber);
                            } else {
                                pEntitlementDTO.setCreditCardNumber(null);
                            }
                        } else if (eftInfoType != null) {
                            pEntitlementDTO.setPaymentMethodType(EntitlementPaymentMethodType.EFT);

                            // clear out credit card information
                            pEntitlementDTO.setCreditCardExpiration(null);
                            pEntitlementDTO.setCreditCardNumber(null);
                            pEntitlementDTO.setCreditCardType(null);
                        }
                    }
                }
                pEntitlementDTO.setBillingProfileId(pBillingProfileId);

                if (billingProfileType.getBillingDayOfMonth() != null) {
                    pEntitlementDTO.setBillingDayOfMonth(billingProfileType.getBillingDayOfMonth().intValue());
                }

                if(billingProfileType.getBillingProfileAddressId() != null &&
                        billingProfileType.getBillingProfileAddressId().getValue() != null) {
                    return billingProfileType.getBillingProfileAddressId().getValue();
                }
            }
        }

        return null;
    }

    /**
     * Update all of the asset's contact related information including:
     * contact email address
     *
     * Update the billing profile zip code
     *
     * @param pEntitlementDTO - domain update dto
     * @param pCustomerAccount - customer account information
     * @param pAssetContactId - asset contact id
     * @param pBillingProfileAddressId - billing profile address id
     */
    private static void processAssetContactAndAddressUpdates(EntitlementDTO pEntitlementDTO, List<CustomerAccountType> pCustomerAccount, String pAssetContactId, String pBillingProfileAddressId) {
        if(pAssetContactId == null || pCustomerAccount.size() == 0) {
            return;
        }

        // Amo documentation -> "CustomerAccount - In 10.1, there should be only ONE"
        for (CustomerAccountType customerAccountType : pCustomerAccount) {
            // find asset contact
            for (CustomerAccountContactType customerAccountContactType : customerAccountType.getCustomerAccountContact()) {
                if(customerAccountContactType.getCustomerAccountContactId() != null &&
                        pAssetContactId.equals(customerAccountContactType.getCustomerAccountContactId().getValue())) {

                    PersonType personType = customerAccountContactType.getContactPerson().getPerson();

                    //contact name
                    String contactName = "";
                    if(personType.getGivenName() != null){
                        contactName += personType.getGivenName() + " ";
                    }
                    if(personType.getMiddleName() != null){
                        contactName += personType.getMiddleName() + " ";
                    }
                    if(personType.getFamilyName() != null){
                        contactName += personType.getFamilyName();
                    }
                    contactName = contactName.trim();
                    pEntitlementDTO.setContactName(contactName.length() > 0 ? contactName : null);

                    // contact email
                    PersonType.EmailAddress emailAddress = personType.getEmailAddress();
                    if(emailAddress != null) {
                        if(emailAddress.getMainEmailAddress() != null) {
                            pEntitlementDTO.setContactEmail(emailAddress.getMainEmailAddress());
                        } else if(emailAddress.getAlternateEmailAddress() != null) {
                            pEntitlementDTO.setContactEmail(emailAddress.getAlternateEmailAddress());
                        }
                    }
                }
            }

            if(pBillingProfileAddressId != null) {
                BusinessType.Address address = customerAccountType.getCustomerAccount().getBusinessCustomerAccount().getBusiness().getAddress();
                if(address.getPrimaryBillToAddress() != null && address.getPrimaryBillToAddress().getAddressId() != null && pBillingProfileAddressId.equals(address.getPrimaryBillToAddress().getAddressId().getValue())) {
                    pEntitlementDTO.setBillingZipCode(address.getPrimaryBillToAddress().getPostalAddress().getPostalCode());
                } else if(address.getPrimaryAddress() != null && address.getPrimaryAddress().getAddressId() != null && pBillingProfileAddressId.equals(address.getPrimaryAddress().getAddressId().getValue())) {
                    pEntitlementDTO.setBillingZipCode(address.getPrimaryAddress().getPostalAddress().getPostalCode());
                } else if(address.getPrimaryShipToAddress() != null && address.getPrimaryShipToAddress().getAddressId() != null && pBillingProfileAddressId.equals(address.getPrimaryShipToAddress().getAddressId().getValue())) {
                    pEntitlementDTO.setBillingZipCode(address.getPrimaryShipToAddress().getPostalAddress().getPostalCode());
                } else if(address.getAddress() != null) {
                    for (AddressType addressType : address.getAddress()) {
                        if(addressType.getUniversalAddressId() != null && pBillingProfileAddressId.equals(addressType.getUniversalAddressId().getValue())) {
                            pEntitlementDTO.setBillingZipCode(addressType.getPostalAddress().getPostalCode());
                            break;
                        }
                    }
                }
            }
        }
    }


    private static boolean assetMatches(AssetType pAssetType, String pLicenseNumber, String pEOC) {
        if (pAssetType.getEntitlement().getLicenseId() == null || pAssetType.getEntitlement().getEntitlementId() == null) {
            return false;
        } else {
            return pAssetType.getEntitlement().getLicenseId().getValue().equals(pLicenseNumber) &&
                    pAssetType.getEntitlement().getEntitlementId().getValue().equals(pEOC);
        }
    }

    //CustomerAccountContactId -> contact
    public static void addAllContacts(Map<String, CustomerAccountContactType> contacts, SyncCustomerAssetDataAreaType pSyncCustomerAssetDataAreaType) {
        for (CustomerAccountType customerAccount : pSyncCustomerAssetDataAreaType.getSyncCustomerAsset().getCustomerAccount()) {
            for (CustomerAccountContactType customerAccountContact : customerAccount.getCustomerAccountContact()) {
                String contactKey = customerAccountContact.getCustomerAccountContactId().getValue();
                if (! contacts.containsKey(contactKey)) {
                    contacts.put(contactKey, customerAccountContact);
                }

            }
        }
    }


    public static void addAllAddresses(Map<String, PostalAddressDescription> addresses, SyncCustomerAssetDataAreaType pSyncCustomerAssetDataAreaType) {

        for (CustomerAccountType customerAccount : pSyncCustomerAssetDataAreaType.getSyncCustomerAsset().getCustomerAccount()) {
            //business addresses
            BusinessType.Address address = customerAccount.getCustomerAccount().getBusinessCustomerAccount().getBusiness().getAddress();

            addAddress(addresses, address.getPrimaryAddress(), "Siebel Primary Business Address");
            addAddress(addresses, address.getPrimaryBillToAddress(), "Siebel Primary Billing Address");
            addAddress(addresses, address.getPrimaryShipToAddress(), "Siebel Primary Shipping Address");

            int i=0;
            for (AddressType otherAddress : address.getAddress()) {
                String addressKey = new PostalAddressDescription(otherAddress.getPostalAddress()).getAddressKey();
                if (!addresses.containsKey(addressKey)) {
                    String description;
                    if (otherAddress.getContactMethodRole().size() > 0) {
                        description = String.format("Siebel Other %s address #%d", otherAddress.getContactMethodRole().get(0).value(), ++i);
                    } else {
                        description = String.format("Siebel Other address #%d", ++i);
                    }

                    addresses.put(addressKey, new PostalAddressDescription(otherAddress.getPostalAddress(), description));
                }
            }

            //contact addresses
            for (CustomerAccountContactType customerAccountContact : customerAccount.getCustomerAccountContact()) {
                PersonType person = customerAccountContact.getContactPerson().getPerson();
                int j=0;
                for (ContactAddressType contactAddress : customerAccountContact.getContactPerson().getPerson().getAddress()) {
                    String addressKey = contactAddress.getAddressId().getValue();
                    if (! addresses.containsKey(addressKey)) {
                        String description = String.format("Address #%d for Siebel %s %s", ++j, person.getGivenName(), person.getFamilyName());
                        addresses.put(addressKey, new PostalAddressDescription(contactAddress.getPostalAddress(), description));
                    }
                }

            }

        }

    }

    private static void addAddress(Map<String, PostalAddressDescription> addresses, AccountAddressType address, String description) {
        if (address != null) {
            String addressKey = new PostalAddressDescription(address.getPostalAddress()).getAddressKey();
            if (!addresses.containsKey(addressKey)) {
                addresses.put(addressKey, new PostalAddressDescription(address.getPostalAddress(), description));
            }
        }
    }

    public static String editionElementName() {
        return SystemParameter.findStringValue(SystemParameter.Code.AMO_EDITION_ELEMENT_NAME, "Edition");
    }

    public static String numberOfEmployeesElementName() {
        return SystemParameter.findStringValue(SystemParameter.Code.AMO_NUMBER_OF_EMPLOYEES_ELEMENT_NAME, "Number of Employees");
    }

}
