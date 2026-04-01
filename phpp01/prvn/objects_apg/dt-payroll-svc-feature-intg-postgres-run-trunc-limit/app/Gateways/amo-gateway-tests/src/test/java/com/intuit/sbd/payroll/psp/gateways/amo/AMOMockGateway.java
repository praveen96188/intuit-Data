package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.iep.abcsimpl.commoncomponents.v1.*;
import com.intuit.iep.billingprofile.billingprofileabo.v1.BillingProfileType;
import com.intuit.iep.customeraccount.customeraccountbase.v1.*;
import com.intuit.iep.customeraccount.customeraccountbase.v1.PostalAddressType;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.*;
import com.intuit.iep.entitlement.entitlementservice.xsd.EntitlementUnitStateType;
import com.intuit.iep.entitlement.entitlementservice.xsd.StateType;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessageStatusCode;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 9, 2010
 * Time: 1:32:24 PM
 */
public class AMOMockGateway extends AbstractAMOGateway {
    private static Collection<Message> messages = new ArrayList<Message>();
    private static boolean writeMessagesToFiles = false;
    private static boolean returnUnmarshallableDTO = false;

    SpcfLogger logger = SpcfLogManager.getLogger(AMOMockGateway.class);

    @Override
    public Collection<AMODTO> getMessages(int numberOfMessagesToGet) {
        Map<String, AMODTO> entitlementMessageMap = readMessagesFromFiles();

        try {
            for (Message message : messages) {
                processMessage(entitlementMessageMap, buildMessage(message));
            }
        } finally {
            clearMessages();
        }

        if(writeMessagesToFiles) {
            try{
                writeMessagesToFiles(entitlementMessageMap.values());
            }catch (Throwable t){
                logger.error("Error in uploading AMO file",t);
            }
            return new ArrayList<AMODTO>();
        }

        if(returnUnmarshallableDTO) {
            returnUnmarshallableDTO = false;
            AMODTO amodto = new AMODTO();
            amodto.setLicenseNumber("ERROR_UNMARSHALLING");
            SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO = new SyncCustomerAssetDataAreaTypeDTO("This is not xml");
            syncCustomerAssetDataAreaTypeDTO.setEntitlementMessageStatusCode(EntitlementMessageStatusCode.Error);
            amodto.addMessage(syncCustomerAssetDataAreaTypeDTO);
            entitlementMessageMap.put("ERROR_UNMARSHALLING", amodto);
        }

        return entitlementMessageMap.values();
    }

    public static void clearMessages() {
        messages.clear();
    }

    public static Collection<Message> getMessages() {
        return messages;
    }

    public static void setWriteMessagesToFiles(boolean pWriteMessagesToFiles) {
        writeMessagesToFiles = pWriteMessagesToFiles;
    }

    public static void setReturnUnmarshallableDTO(boolean pReturnUnmarshallableDTO) {
        returnUnmarshallableDTO = pReturnUnmarshallableDTO;

    }

    private SyncCustomerAssetDataAreaType buildMessage(Message pMessage) {
        SyncCustomerAssetDataAreaType syncCustomerAssetDataAreaType = new SyncCustomerAssetDataAreaType();
        syncCustomerAssetDataAreaType.setEvent(new SyncCustomerAssetDataAreaType.Event());
        syncCustomerAssetDataAreaType.getEvent().setEventReason(pMessage.eventReason);

        TransactionInfoType transactionInfoType = new TransactionInfoType();
        try {
            transactionInfoType.setTransactionDatetime(SpcfUtils.convertSpcfCalendarToXmlGregorianCalendar(pMessage.transactionDate));
        } catch (Exception e) {
            // ignore
        }
        syncCustomerAssetDataAreaType.setTransactionInfo(transactionInfoType);

        SyncCustomerAssetType syncCustomerAssetType = new SyncCustomerAssetType();
        syncCustomerAssetDataAreaType.setSyncCustomerAsset(syncCustomerAssetType);
        CustomerAccountType customerAccountType  = null;

        int idCounter = 1;
        for (Entitlement entitlement : pMessage.entitlements) {

            if(entitlement.entitlementTransfer == null) {
                if(customerAccountType == null) {
                    customerAccountType = new CustomerAccountType();
                    syncCustomerAssetType.getCustomerAccount().add(customerAccountType);
                }
                if(entitlement.contact != null) {
                    Contact contact = entitlement.contact;
                    CustomerAccountContactType customerAccountContactType = new CustomerAccountContactType();
                    {
                        ContactIdType contactIdType = new ContactIdType();
                        contactIdType.setSchemeName(ContactIdSchemeType.MDM);
                        contactIdType.setValue("contact" + idCounter);
                        customerAccountContactType.setCustomerAccountContactId(contactIdType);
                    }
                    CustomerAccountContactType.ContactPerson contactPerson = new CustomerAccountContactType.ContactPerson();
                    {
                        PersonType personType = new PersonType();
                        if(contact.getFirstName() != null) {
                            personType.setGivenName(contact.getFirstName());
                        }
                        if(contact.getMiddleName() != null) {
                            personType.setMiddleName(contact.getMiddleName());
                        }
                        if(contact.getLastName() != null) {
                            personType.setFamilyName(contact.getLastName());
                        }
                        if(contact.getEmailAddress() != null) {
                            PersonType.EmailAddress emailAddress = new PersonType.EmailAddress();
                            emailAddress.setMainEmailAddress(contact.getEmailAddress());
                            personType.setEmailAddress(emailAddress);
                        }
                        contactPerson.setPerson(personType);
                    }
                    customerAccountContactType.setContactPerson(contactPerson);
                    customerAccountType.getCustomerAccountContact().add(customerAccountContactType);
                }

                if(entitlement.billingZipCode != null) {
                    CustomerAccountType.CustomerAccount customerAccount = new CustomerAccountType.CustomerAccount();
                    BusinessCustomerAccountType businessCustomerAccountType = new BusinessCustomerAccountType();
                    BusinessType businessType = new BusinessType();
                    BusinessType.Address address = new BusinessType.Address();
                    AddressIdType addressIdType = new AddressIdType();
                    addressIdType.setSchemeName(AddressIdSchemeType.MDM);
                    addressIdType.setValue("billingAddress" + idCounter);
                    PostalAddressType postalAddressType = new PostalAddressType();
                    postalAddressType.setPostalCode(entitlement.billingZipCode);
                    if(entitlement.addressToUse != null && entitlement.addressToUse.equals(Entitlement.PRIMARY_BILL_TO_ADDRESS)) {
                        AccountAddressType accountAddressType = new AccountAddressType();
                        accountAddressType.setPostalAddress(postalAddressType);
                        accountAddressType.setAddressId(addressIdType);
                        address.setPrimaryBillToAddress(accountAddressType);
                    } else if(entitlement.addressToUse != null && entitlement.addressToUse.equals(Entitlement.PRIMARY_ADDRESS)) {
                        AccountAddressType accountAddressType = new AccountAddressType();
                        accountAddressType.setPostalAddress(postalAddressType);
                        accountAddressType.setAddressId(addressIdType);
                        address.setPrimaryAddress(accountAddressType);
                    } else if(entitlement.addressToUse != null && entitlement.addressToUse.equals(Entitlement.PRIMARY_SHIP_TO_ADDRESS)) {
                        AccountAddressType accountAddressType = new AccountAddressType();
                        accountAddressType.setPostalAddress(postalAddressType);
                        accountAddressType.setAddressId(addressIdType);
                        address.setPrimaryShipToAddress(accountAddressType);
                    } else {
                        AddressType addressType = new AddressType();
                        addressType.setUniversalAddressId(addressIdType);
                        addressType.setPostalAddress(postalAddressType);
                        address.getAddress().add(addressType);
                    }
                    businessType.setAddress(address);
                    businessCustomerAccountType.setBusiness(businessType);
                    customerAccount.setBusinessCustomerAccount(businessCustomerAccountType);
                    customerAccountType.setCustomerAccount(customerAccount);
                }

                BillingProfileType billingProfileType = new BillingProfileType();
                {
                    {
                        BillingProfileIdType billingProfileIdType = new BillingProfileIdType();
                        billingProfileIdType.setSchemeName(BillingProfileIdSchemeType.SIEBEL);
                        billingProfileIdType.setValue("billing" + idCounter);
                        billingProfileType.setBillingProfileId(billingProfileIdType);
                        AddressIdType addressIdType = new AddressIdType();
                        addressIdType.setSchemeName(AddressIdSchemeType.MDM);
                        addressIdType.setValue("billingAddress" + idCounter);
                        billingProfileType.setBillingProfileAddressId(addressIdType);
                    }
                    if(entitlement.includeBillingUpdate) {
                        PaymentMethodType paymentMethodType = new PaymentMethodType();
                        /*if(entitlement.CCInfo != null) {
                            CCInfoType ccInfoType = new CCInfoType();
                            ccInfoType.setAccountNumber(entitlement.CCInfo.ccNum);
                            ccInfoType.setAccountType(CreditCardAccountType.valueOf(entitlement.CCInfo.ccType));
                            ccInfoType.setExpirationMonth(entitlement.CCInfo.ccExpMM);
                            ccInfoType.setExpirationYear(entitlement.CCInfo.ccExpYYYY);
                            paymentMethodType.setCCInfo(ccInfoType);
                        } else {
                            EFTInfoType eftInfoType = new EFTInfoType();
                            paymentMethodType.setEFTInfo(eftInfoType);
                        }*/
                        billingProfileType.setPaymentMethod(paymentMethodType);
                    }
                }
                syncCustomerAssetType.getBillingProfile().add(billingProfileType);
            }

            if (pMessage.includePayrollAsset){
                AssetType assetType = new AssetType();
                {
                    AccountNumberType accountNumberType = new AccountNumberType();
                    accountNumberType.setValue(entitlement.customerId != null ? entitlement.customerId : "123");
                    assetType.setServiceAccountNumber(accountNumberType);
                    assetType.setStatus(entitlement.assetStatus);
                }
                if(entitlement.contact != null) {
                    ContactIdType contactIdType = new ContactIdType();
                    contactIdType.setSchemeName(ContactIdSchemeType.MDM);
                    contactIdType.setValue("contact" + idCounter);
                    assetType.setAssetContactId(contactIdType);
                }
                {
                    BillingProfileIdType billingProfileIdType = new BillingProfileIdType();
                    billingProfileIdType.setSchemeName(BillingProfileIdSchemeType.SIEBEL);
                    billingProfileIdType.setValue("billing" + idCounter);
                    assetType.setBillingProfileId(billingProfileIdType);
                }
                {
                    ItemIdType itemIdType = new ItemIdType();
                    ItemIdType.Id id = new ItemIdType.Id();
                    id.setValue(entitlement.itemNumber);
                    itemIdType.setId(id);
                    assetType.setItem(itemIdType);
                }
                for (TransactionAttribute transactionAttribute : entitlement.transactionAttributes) {
                    TransactionAttributeType transactionAttributeType = new TransactionAttributeType();
                    transactionAttributeType.setName(transactionAttribute.name);
                    transactionAttributeType.setValue(transactionAttribute.value);
                    assetType.getTransactionAttributes().add(transactionAttributeType);
                }

                try {
                    JAXBElement<XMLGregorianCalendar> jaxbElement =
                            new JAXBElement<XMLGregorianCalendar>(new QName("http://www.intuit.com/iep/CustomerAsset/IntuitCustomerAssetABO/V1", "NextChargeDate"),XMLGregorianCalendar.class, SpcfUtils.convertSpcfCalendarToXmlGregorianCalendar(entitlement.nextChargeDate));
                    assetType.setNextChargeDate(jaxbElement);

                    assetType.setSubscriptionEndDate(SpcfUtils.convertSpcfCalendarToXmlGregorianCalendar(entitlement.subscriptionEndDate));
                } catch (Exception e) {
                    // don't care
                }

                if(entitlement.entitlementTransfer != null) {
                    AssetType.EntitlementTransfer entitlementTransfer = new AssetType.EntitlementTransfer();
                    com.intuit.iep.entitlement.entitlementservice.xsd.LicenseIdType sourceLicenseIdType = new com.intuit.iep.entitlement.entitlementservice.xsd.LicenseIdType();
                    sourceLicenseIdType.setValue(entitlement.entitlementTransfer.sourceLicenseNumber);
                    sourceLicenseIdType.setSchemeName("ERS");
                    entitlementTransfer.setSource(new EntitlementDescriptorType());
                    entitlementTransfer.getSource().setLicenseId(sourceLicenseIdType);
                    com.intuit.iep.entitlement.entitlementservice.xsd.LicenseIdType targetLicenseIdType = new com.intuit.iep.entitlement.entitlementservice.xsd.LicenseIdType();
                    targetLicenseIdType.setValue(entitlement.entitlementTransfer.targetLicenseNumber);
                    targetLicenseIdType.setSchemeName("ERS");
                    entitlementTransfer.setTarget(new EntitlementDescriptorType());
                    entitlementTransfer.getTarget().setLicenseId(targetLicenseIdType);
                    assetType.setEntitlementTransfer(entitlementTransfer);
                } else {
                    EntitlementType entitlementType = new EntitlementType();
                    if(entitlement.licenseNumber != null) {
                        if(entitlementType == null) {
                            entitlementType = new EntitlementType();
                        }
                        LicenseIdType licenseIdType = new LicenseIdType();
                        licenseIdType.setSchemeName("ERS");
                        licenseIdType.setValue(entitlement.licenseNumber);
                        entitlementType.setLicenseId(licenseIdType);

                        try {
                            assetType.setSubscriptionEndDate(SpcfUtils.convertSpcfCalendarToXmlGregorianCalendar(entitlement.subscriptionEndDate));
                        } catch (Exception e) {
                            // don't care
                        }
                    }
                    if(entitlement.eoc != null) {
                        if(entitlementType == null) {
                            entitlementType = new EntitlementType();
                        }
                        EntitlementIdType entitlementIdType = new EntitlementIdType();
                        entitlementIdType.setSchemeName("ERS");
                        entitlementIdType.setValue(entitlement.eoc);
                        entitlementType.setEntitlementId(entitlementIdType);
                    }
                    if(entitlement.entitlementState != null) {
                        if(entitlementType == null) {
                            entitlementType = new EntitlementType();
                        }
                        entitlementType.setEntitlementState(StateType.fromValue(entitlement.entitlementState));
                    }
                    for (EntitlementUnit entitlementUnit : entitlement.entitlementUnits) {
                        if(entitlementType == null) {
                            entitlementType = new EntitlementType();
                        }
                        EntitlementType.EntitlementUnit entitlementUnitType = new EntitlementType.EntitlementUnit();
                        entitlementUnitType.setIdentifiedResourceValue(entitlementUnit.ein);
                        entitlementUnitType.setUnitState(EntitlementUnitStateType.valueOf(entitlementUnit.status.toUpperCase()));
                        entitlementType.getEntitlementUnit().add(entitlementUnitType);
                    }
                    assetType.setEntitlement(entitlementType);
                }

                if(entitlement.orderNumber != null || entitlement.cancellationReason != null) {
                    OrderInfoType orderInfoType = new OrderInfoType();
                    orderInfoType.setOrderNumber(entitlement.orderNumber);

                    if(entitlement.cancellationReason != null) {
                        OrderLineType orderLineType = new OrderLineType();
                        orderLineType.setActionReason(entitlement.cancellationReason);
                        orderInfoType.setLineItem(orderLineType);
                    }

                    assetType.setOrderInfo(orderInfoType);
                }

                syncCustomerAssetType.getAsset().add(assetType);
            }

            if (pMessage.includeUsageAsset) {
                if (AssetChangeReasonType.PAYMENT_FAILURE_NOTICE.equals(pMessage.eventReason) ||
                        AssetChangeReasonType.PAYMENT_SUCCESS_NOTICE.equals(pMessage.eventReason) ||
                        AssetChangeReasonType.CHARGEBACK_NOTICE.equals(pMessage.eventReason)) {
                    {
                        AssetType assetType = new AssetType();
                        {
                            AccountNumberType accountNumberType = new AccountNumberType();
                            accountNumberType.setValue(entitlement.customerId != null ? entitlement.customerId : "123");
                            assetType.setServiceAccountNumber(accountNumberType);
                            assetType.setStatus(entitlement.assetStatus);
                        }
                        if(entitlement.contact != null) {
                            ContactIdType contactIdType = new ContactIdType();
                            contactIdType.setSchemeName(ContactIdSchemeType.MDM);
                            contactIdType.setValue("contact" + idCounter);
                            assetType.setAssetContactId(contactIdType);
                        }
                        {
                            BillingProfileIdType billingProfileIdType = new BillingProfileIdType();
                            billingProfileIdType.setSchemeName(BillingProfileIdSchemeType.SIEBEL);
                            billingProfileIdType.setValue("billing" + idCounter);
                            assetType.setBillingProfileId(billingProfileIdType);
                        }
                        {
                            ItemIdType itemIdType = new ItemIdType();
                            ItemIdType.Id id = new ItemIdType.Id();
                            id.setSchemeName("PIM");
                            id.setValue("1100522");
                            itemIdType.setId(id);
                            assetType.setItem(itemIdType);
                        }

                        assetType.setItemType(entitlement.getItemType());

                        TransactionAttributeType transactionAttributeType = new TransactionAttributeType();
                        transactionAttributeType.setName("Intuit");
                        transactionAttributeType.setValue("Intuit");
                        assetType.getTransactionAttributes().add(transactionAttributeType);

                        EntitlementType entitlementType = new EntitlementType();
                        if(entitlement.licenseNumber != null) {
                            if(entitlementType == null) {
                                entitlementType = new EntitlementType();
                            }
                            LicenseIdType licenseIdType = new LicenseIdType();
                            licenseIdType.setSchemeName("ERS");
                            licenseIdType.setValue(entitlement.licenseNumber);
                            entitlementType.setLicenseId(licenseIdType);
                        }
                        if(entitlement.eoc != null) {
                            if(entitlementType == null) {
                                entitlementType = new EntitlementType();
                            }
                            EntitlementIdType entitlementIdType = new EntitlementIdType();
                            entitlementIdType.setSchemeName("ERS");
                            entitlementIdType.setValue(entitlement.eoc);
                            entitlementType.setEntitlementId(entitlementIdType);
                        }
                        assetType.setEntitlement(entitlementType);

                        syncCustomerAssetType.getAsset().add(assetType);
                    }
                }
            }

            if (pMessage.isIncludeTrialAsset) {
                AssetType assetType = new AssetType();
                AccountNumberType accountNumberType = new AccountNumberType();
                accountNumberType.setValue(entitlement.customerId != null ? entitlement.customerId : "123");
                assetType.setServiceAccountNumber(accountNumberType);
                assetType.setStatus(entitlement.assetStatus);

                if(entitlement.contact != null) {
                    ContactIdType contactIdType = new ContactIdType();
                    contactIdType.setSchemeName(ContactIdSchemeType.MDM);
                    contactIdType.setValue("contact" + idCounter);
                    assetType.setAssetContactId(contactIdType);
                }

                BillingProfileIdType billingProfileIdType = new BillingProfileIdType();
                billingProfileIdType.setSchemeName(BillingProfileIdSchemeType.SIEBEL);
                billingProfileIdType.setValue("billing" + idCounter);
                assetType.setBillingProfileId(billingProfileIdType);

                ItemIdType itemIdType = new ItemIdType();
                ItemIdType.Id id = new ItemIdType.Id();
                id.setSchemeName("PIM");
                id.setValue("1100523");
                itemIdType.setId(id);
                assetType.setItem(itemIdType);

                assetType.setItemType(entitlement.getItemType());

                TransactionAttributeType transactionAttributeType = new TransactionAttributeType();
                transactionAttributeType.setName("Intuit");
                transactionAttributeType.setValue("Intuit");
                assetType.getTransactionAttributes().add(transactionAttributeType);

                EntitlementType entitlementType = new EntitlementType();
                if(entitlement.licenseNumber != null) {
                    if(entitlementType == null) {
                        entitlementType = new EntitlementType();
                    }
                    LicenseIdType licenseIdType = new LicenseIdType();
                    licenseIdType.setSchemeName("ERS");
                    licenseIdType.setValue(entitlement.licenseNumber);
                    entitlementType.setLicenseId(licenseIdType);
                }

                if(entitlementType == null) {
                    entitlementType = new EntitlementType();
                }
                EntitlementIdType entitlementIdType = new EntitlementIdType();
                entitlementIdType.setSchemeName("ERS");
                entitlementIdType.setValue("545591");
                entitlementType.setEntitlementId(entitlementIdType);

                assetType.setEntitlement(entitlementType);

                syncCustomerAssetType.getAsset().add(assetType);
            }

            idCounter++;
        }

        return syncCustomerAssetDataAreaType;
    }
}
