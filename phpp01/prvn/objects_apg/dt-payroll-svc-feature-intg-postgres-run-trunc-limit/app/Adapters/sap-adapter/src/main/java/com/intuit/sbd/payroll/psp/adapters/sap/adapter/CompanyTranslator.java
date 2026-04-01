/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/CompanyTranslator.java#14 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.iep.customeraccount.customeraccountbase.v1.CustomerAccountContactType;
import com.intuit.iep.customeraccount.customeraccountbase.v1.PersonType;
import com.intuit.iep.customeraccount.customeraccountbase.v1.PostalAddressType;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.SyncCustomerAssetDataAreaType;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsAddress;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsContact;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.amo.AMOMessageProcessing;
import com.intuit.sbd.payroll.psp.batchjobs.amo.PostalAddressDescription;
import com.intuit.sbd.payroll.psp.batchjobs.amo.ProcessSavedAMOMessages;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.DateFormatUtils;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.domain.util.PIIMask;
import com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.gateways.amo.SyncCustomerAssetDataAreaTypeDTO;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CompanyTranslator -- this class is used by the SAP adapter to convert the returned SPCF generated domain entities
 * related to companies into SAP DTOs for LCDS, and the returned SAP DTOs into PSP DTOs for updates, creates, etc.
 *
 * @author Joe Warmelink
 */
public class CompanyTranslator {

    private static Map<String, PayrollFrequencyDTO> payrollFrequencyDTOMap = new HashMap<String, PayrollFrequencyDTO>();
    private static SpcfLogger logger = PayrollServices.getLogger(CompanyTranslator.class);
    static {
        payrollFrequencyDTOMap.put(PayrollFrequency.Codes.DAILY_MISC, PayrollFrequencyDTO.Daily);
        payrollFrequencyDTOMap.put(PayrollFrequency.Codes.WEEKLY, PayrollFrequencyDTO.Weekly);
        payrollFrequencyDTOMap.put(PayrollFrequency.Codes.SEMI_MONTHLY, PayrollFrequencyDTO.SemiMonthly);
        payrollFrequencyDTOMap.put(PayrollFrequency.Codes.MONTHLY, PayrollFrequencyDTO.Monthly);
        payrollFrequencyDTOMap.put(PayrollFrequency.Codes.QUARTERLY, PayrollFrequencyDTO.Quarterly);
        payrollFrequencyDTOMap.put(PayrollFrequency.Codes.SEMI_ANNUALLY, PayrollFrequencyDTO.SemiAnnual);
        payrollFrequencyDTOMap.put(PayrollFrequency.Codes.ANNUAL, PayrollFrequencyDTO.Annual);
        payrollFrequencyDTOMap.put(PayrollFrequency.Codes.BI_WEEKLY, PayrollFrequencyDTO.BiWeekly);
    }

    public static CompanyAdditionalInfoDTO getCompanyAdditionalInfoFromSAPLegalInfo(SAPCompanyLegalInfo sapCompanyLegalInfo){
        if(sapCompanyLegalInfo.getIndustryType()!=null){
            CompanyAdditionalInfoDTO companyAdditionalInfoDTO = new CompanyAdditionalInfoDTO();
            companyAdditionalInfoDTO.setIndustry(sapCompanyLegalInfo.getIndustryType());
            return companyAdditionalInfoDTO;
        }
        return null;
    }

    public static AddressDTO getAddressDTOFromSAPAddress(SAPAddress sapAddr) {
        if (sapAddr != null) {
            AddressDTO addr = new AddressDTO();
            addr.setAddressLine1(sapAddr.getAddressLine1());
            addr.setAddressLine2(sapAddr.getAddressLine2());
            addr.setAddressLine3(sapAddr.getAddressLine3());
            addr.setCity(sapAddr.getCity());
            addr.setCountry(sapAddr.getCountry());
            addr.setState(sapAddr.getState());
            addr.setZipCode(sapAddr.getZipCode());
            addr.setZipCodeExtension(sapAddr.getZipCodeExtension());
            return addr;
        }
        return null;
    }

    public static ContactDTO getContactDTOFromSAPContact(SAPContact sapContact) {
        ContactDTO contact = new ContactDTO();
        contact.setAccountSignatory(sapContact.getContactRoleCd().equals(ContactRole.PrimaryPrincipal) || sapContact.getContactRoleCd().equals(ContactRole.SecondaryPrincipal));
        contact.setAddress(getAddressDTOFromSAPAddress(sapContact.getAddress()));
        contact.setCommunicationTypeCd(sapContact.getCommunicationTypeCd());
        contact.setContactRoleCd(sapContact.getContactRoleCd());
        contact.setEmail(sapContact.getEmail());
        contact.setFirstName(sapContact.getFirstName());
        contact.setLastName(sapContact.getLastName());
        contact.setMiddleName(sapContact.getMiddleName());
        contact.setPhoneNumber(SAPTranslator.sanitizePhoneNumber(sapContact.getPhoneNumber()));
        contact.setFaxNumber(sapContact.getFaxNumber());
        contact.setTitle(sapContact.getPrefix());
        contact.setTitleSuffix(sapContact.getSuffix());
        contact.setJobTitle(sapContact.getJobTitle());
        contact.setSocialSecurityNumber(sapContact.getSocialSecurityNumber());
        if(sapContact.getDateOfBirth() != null){
            contact.setDateOfBirth(new DateDTO(sapContact.getDateOfBirth()));
        }

        // must be done last
        if (sapContact.getContactId() != null) {
            contact.setContactId(sapContact.getContactId());
        } else {
            contact.setContactId(SpcfUniqueId.generateRandomUniqueIdString());
        }
        return contact;
    }

    public static SAPFundingModel getSAPFundingModelFromDomainEntity(FundingModel pModel) {
        SAPFundingModel sapFundingModel = new SAPFundingModel();
        sapFundingModel.setDescription(pModel.getDescription());
        sapFundingModel.setName(pModel.getName());
        sapFundingModel.setFundingModelCd(pModel.getFundingModelCd());

        return sapFundingModel;
    }

    public static SAPSourceSystem getSAPSourceSystemFromDomainEntity(SourceSystem pSourceSystem) {
        SAPSourceSystem sapSourceSystem = new SAPSourceSystem();
        sapSourceSystem.setDescription(pSourceSystem.getDescription());
        sapSourceSystem.setName(pSourceSystem.getName());
        sapSourceSystem.setSourceSystemCd(pSourceSystem.getSourceSystemCd().toString());

        return sapSourceSystem;
    }

    public static SAPCompanyNote getSAPCompanyNoteFromDomainEntity(CompanyNote pNote) {
        SAPCompanyNote sapCompanyNote = new SAPCompanyNote();

        sapCompanyNote.setId(pNote.getId().toString());
        sapCompanyNote.setNotes(pNote.getNotes());
        sapCompanyNote.setInsertUserId(SAPTranslator.getUserNameFromUserID(pNote.getInsertUserId()));
        sapCompanyNote.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(pNote.getCreatedDate()));
        sapCompanyNote.setAlert(pNote.getAlert());
        sapCompanyNote.setEventId(pNote.getCompanyEvent().getId().toString());

        return sapCompanyNote;
    }


    public static SAPCompany getSAPCompanyFromDomainEntity(Company pCompany) {
        SAPCompany sapCompany = new SAPCompany();
        sapCompany.setGseq(pCompany.getId().toString());
        sapCompany.setSourceSystemCd(pCompany.getSourceSystemCd().toString());
        sapCompany.setCompanyId(pCompany.getSourceCompanyId());
        sapCompany.setFein(pCompany.getFedTaxId());
        sapCompany.setLegalName(pCompany.getLegalName());
        sapCompany.setDBA(pCompany.getDbaName());
        sapCompany.setIamRealmId(pCompany.getIAMRealmId());
        sapCompany.setIsMoneyMovementOnboardingEnabled(pCompany.isMoneyMovementOnboardingEnabled());

        sapCompany.setNotificationEmail(pCompany.getNotificationEmail());

        String frequencyCode = "1";
        if (pCompany.getPayrollFrequency() != null) {
            frequencyCode = pCompany.getPayrollFrequency().getPayrollFreqCd();
        }

        PayrollFrequencyDTO payrollFrequencyDTO = payrollFrequencyDTOMap.get(frequencyCode);

        sapCompany.setPayrollFrequencyCd(payrollFrequencyDTO);

        boolean isEditable = pCompany.getSourceSystemCd() == SourceSystemCode.QBDT;

        sapCompany.setIsEditable(isEditable);
        sapCompany.setIsAssisted(pCompany.isCompanyOnService(ServiceCode.Tax));
        if (!sapCompany.getIsAssisted()){
            sapCompany.setIsDIY(pCompany.isCompanyOnService(ServiceCode.Cloud));
        }
        sapCompany.setIsAssistedServiceCancelled(pCompany.hasCancelledService(ServiceCode.Tax));
        sapCompany.setIsVmp(pCompany.hasService(ServiceCode.ViewMyPaycheck));

        sapCompany.setCompanyServiceStateCd(getSapCompanyServiceState(pCompany));

        EntitlementUnit activePrimaryEntitlementUnit = pCompany.getActivePrimaryEntitlementUnit();
        boolean assistedEntitlement = activePrimaryEntitlementUnit != null && activePrimaryEntitlementUnit.getEntitlement().getEntitlementCode().isAssisted();
        if (activePrimaryEntitlementUnit != null && activePrimaryEntitlementUnit.getEntitlement() != null) {
            sapCompany.setCustomerId(pCompany.getActivePrimaryEntitlementUnit().getEntitlement().getCustomerId());
        }
        sapCompany.setCanChangePriceType(assistedEntitlement && !pCompany.hasService(ServiceCode.Tax));
        sapCompany.setHasCompanyAgencies(pCompany.getCompanyAgencyCollection().size() > 0);

        return sapCompany;
    }

    public static SAPPINInfo getPINInfo(Company pCompany) {
        SAPPINInfo info = new SAPPINInfo();

        SpcfCalendar dateToCompare = PSPDate.getPSPTime();
        CalendarUtils.clearTime(dateToCompare);

        info.setPinCreated(pCompany.isPINCreated());
        if (pCompany.getAccountLockedUntil() != null) {
            info.setPinLocked(dateToCompare.compareTo(pCompany.getAccountLockedUntil()) <= 0);
        } else {
            info.setPinLocked(false);
        }

        return info;
    }

    public static SAPCompanyServiceState getSapCompanyServiceState(Company company) {
        if (company.isCompanyOnService(ServiceCode.Tax)) {
            CompanyService taxService = company.getService(ServiceCode.Tax);
            ServiceStatusCode serviceStatusCode = CompanyService.getServiceStatus(taxService.getStatusCd());
            if (serviceStatusCode == ServiceStatusCode.PendingActivation) {
                return SAPCompanyServiceState.AssistedPending;
            } else {
                return SAPCompanyServiceState.AssistedActive; //in this universe, anything not pending is "active"
            }
        } else if (company.isCompanyOnService(ServiceCode.DirectDeposit)) {
            return SAPCompanyServiceState.DIYDD;
        } else {
            return SAPCompanyServiceState.DIYOnly;
        }
    }

    public static SAPTaxCompanyServiceInfo getSAPTaxCompanyServiceInfo(TaxCompanyServiceInfo taxCompanyServiceInfo) {
        SAPTaxCompanyServiceInfo sapTaxCompanyServiceInfo = new SAPTaxCompanyServiceInfo();

        sapTaxCompanyServiceInfo.setLastTaxQuarter(String.valueOf(taxCompanyServiceInfo.getLastQuarterToFile()));
        sapTaxCompanyServiceInfo.setFileAnnualReturns(taxCompanyServiceInfo.getFileAnnualReturns());
        sapTaxCompanyServiceInfo.setIsFinal(taxCompanyServiceInfo.getFinalAnnualReturns());
        sapTaxCompanyServiceInfo.setLastPayrollDate(SAPTranslator.getDateFromSpcfCalendar(taxCompanyServiceInfo.getLastPayrollDate()));

        return sapTaxCompanyServiceInfo;
    }

    public static double getCompanyBalanceDueFromDomainEntity(Company pCompany) {
        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(
                pCompany, LedgerAccountCode.ERReturnReceivable));

        LedgerAccount ledgerAccount = PayrollServices.entityFinder.findById(
                LedgerAccount.class, LedgerAccountCode.ERReturnReceivable);

        return PayrollRunTranslator.getAccountBalanceFromDomainEntity(balance, ledgerAccount);
    }

    public static SAPCompanyStrike getSAPCompanyStrikeFromDomainEntity(CompanyEvent strikeEvent, Map<String, String> userCache) {
        SAPCompanyStrike sapCompanyStrike = new SAPCompanyStrike();
        sapCompanyStrike.setCancelled(strikeEvent.getStatusCd() == CompanyEventStatus.Inactive);
        sapCompanyStrike.setNewStrike(false);
        sapCompanyStrike.setSpcfUniqueId(strikeEvent.getId().toString());
        sapCompanyStrike.setStatusEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(strikeEvent.getStatusEffectiveDate()));
        sapCompanyStrike.setStrikeDate(SAPTranslator.getDateFromSpcfCalendar(strikeEvent.getEventTimeStamp()));

        sapCompanyStrike.setCreatedByUserId(SAPTranslator.getUserNameFromUserID(strikeEvent.getCreatorId()));
        sapCompanyStrike.setCancelledByUserId(
                sapCompanyStrike.isCancelled() ? SAPTranslator.getUserNameFromUserID(strikeEvent.getModifierId()) : "");
        sapCompanyStrike.setStrikeReason(EnumUtils.getEnumForReadableName(StrikeReason.class, strikeEvent.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason)));
        if (sapCompanyStrike.getStrikeReason() == StrikeReason.Manual) {
            sapCompanyStrike.setManualDescription(strikeEvent.getCompanyEventDetailValue(EventDetailTypeCode.ManualStrikeReasonDescription));
        } else {
            sapCompanyStrike.setManualDescription("");
        }

        if (strikeEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId) != null) {
            sapCompanyStrike.setFinancialTransactionId(strikeEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId));
        } else {
            sapCompanyStrike.setFinancialTransactionId("");
        }

        fillBasicCompanyEventFields(sapCompanyStrike, strikeEvent, userCache);

        return sapCompanyStrike;
    }

    public static SAPCompanyContacts getSAPCompanyContactsFromDomainEntity(Company pCompany) {
        SAPCompanyContacts companyContacts = new SAPCompanyContacts();
        companyContacts.setSourceSystemCd(pCompany.getSourceSystemCd().toString());
        companyContacts.setCompanyId(pCompany.getSourceCompanyId());

        for (Contact contactEntity : pCompany.getContactCollection())
            companyContacts.getContacts().add(getSAPContactFromDomainEntity(contactEntity));

        return companyContacts;
    }

    public static SAPContact getSAPContactFromDomainEntity(Contact spcfContact) {
        SAPContact contact = new SAPContact();
        contact.setAddress(getSAPAddressFromDomainEntity(spcfContact.getMailingAddress()));
        contact.setFirstName(spcfContact.getFirstName());
        contact.setLastName(spcfContact.getLastName());
        contact.setEmail(spcfContact.getEmail());
        contact.setMiddleName(spcfContact.getMiddleName());
        contact.setPhoneNumber(spcfContact.getPhone());
        contact.setFaxNumber(spcfContact.getFax());
        contact.setPrefix(spcfContact.getTitle());
        contact.setSuffix(spcfContact.getSuffix());
        contact.setJobTitle(spcfContact.getJobTitle());
        contact.setHasInvalidEmail(spcfContact.getHasInvalidEmail());
        contact.setSocialSecurityNumber(spcfContact.getSocialSecurityNumberPlainText());
        if(spcfContact.getDateOfBirth() != null){
            contact.setDateOfBirth(SAPTranslator.getDateFromSpcfCalendar(spcfContact.getDateOfBirth()));
        }


        //Set contact role code
        contact.setContactRoleCd(ContactRole.valueOf(spcfContact.getContactRoleCd().toString()));

        contact.setCommunicationTypeCd(
                CommunicationType.valueOf(spcfContact.getCommunicationTypePreference().toString()));



        //Is account signer?
        contact.setAccountSignatory(spcfContact.getAuthSignerYnInd());

        // must be done last
        contact.setContactId((spcfContact.getSourceContactId()));

        return contact;
    }

    public static SAPAddress getSAPAddressFromDomainEntity(Address spcfAddress) {
        if (spcfAddress != null) {
            SAPAddress address = new SAPAddress();
            address.setAddressLine1(spcfAddress.getAddressLine1());
            address.setAddressLine2(spcfAddress.getAddressLine2());
            address.setAddressLine3(spcfAddress.getAddressLine3());

            address.setCity(spcfAddress.getCity());
            address.setState(spcfAddress.getState());
            address.setZipCode(spcfAddress.getZipCode());
            address.setZipCodeExtension(spcfAddress.getZipCodeExtension());
            address.setCountry(spcfAddress.getCountry());

            return address;
        }
        return null;
    }

    public static SAPServiceStatus getSAPServiceStatusFromDomainEntity(ServiceStatus serviceStatus) {
        SAPServiceStatus sapServiceStatus = new SAPServiceStatus();
        sapServiceStatus.setServiceStatusCd(serviceStatus.getServiceStatusCd().toString());
        sapServiceStatus.setServiceStatusName(serviceStatus.getName());
        sapServiceStatus.setServiceStatusDescription(serviceStatus.getDescription());

        ArrayList<SAPServiceSubStatus> serviceSubStatusList = new ArrayList<SAPServiceSubStatus>();
        for (ServiceSubStatus serviceSubStatus : serviceStatus.getServiceSubStatusCollection()) {
            serviceSubStatusList.add(getSAPServiceSubStatusFromDomainEntity(serviceSubStatus));
        }
        sapServiceStatus.setServiceSubStatusList(serviceSubStatusList);
        return sapServiceStatus;
    }

    public static SAPServiceStatus getSAPServiceStatusFromDomainEntity(ServiceStatus serviceStatus, Collection<ServiceSubStatus> subStatuses, boolean isCurrentValues) {
        SAPServiceStatus sapServiceStatus = new SAPServiceStatus();
        sapServiceStatus.setServiceStatusCd(serviceStatus.getServiceStatusCd().toString());
        sapServiceStatus.setServiceStatusName(serviceStatus.getName());
        sapServiceStatus.setServiceStatusDescription(serviceStatus.getDescription());

        ArrayList<SAPServiceSubStatus> sapServiceSubStatuses = new ArrayList<SAPServiceSubStatus>(subStatuses.size());
        for (ServiceSubStatus serviceSubStatus : subStatuses) {
            if (serviceSubStatus.getServiceStatus().getServiceStatusCd() != serviceStatus.getServiceStatusCd())
                throw new IllegalArgumentException("serviceSubStatus '" + serviceSubStatus.getName() + "' does not belong to serviceStatus '" + serviceStatus.getName() + "'");
            sapServiceSubStatuses.add(getSAPServiceSubStatusFromDomainEntity(serviceSubStatus, isCurrentValues));
        }
        sapServiceStatus.setServiceSubStatusList(sapServiceSubStatuses);
        return sapServiceStatus;
    }

    public static SAPServiceStatus getSAPServiceStatusFromDomainEntity(ServiceSubStatus serviceSubStatus) {
        ServiceStatus serviceStatus = serviceSubStatus.getServiceStatus();
        SAPServiceStatus sapServiceStatus = new SAPServiceStatus();
        sapServiceStatus.setServiceStatusCd(serviceStatus.getServiceStatusCd().toString());
        sapServiceStatus.setServiceStatusName(serviceStatus.getName());
        sapServiceStatus.setServiceStatusDescription(serviceStatus.getDescription());

        ArrayList<SAPServiceSubStatus> serviceSubStatusList = new ArrayList<SAPServiceSubStatus>();
        serviceSubStatusList.add(getSAPServiceSubStatusFromDomainEntity(serviceSubStatus));
        sapServiceStatus.setServiceSubStatusList(serviceSubStatusList);
        return sapServiceStatus;
    }

    public static SAPServiceSubStatus getSAPServiceSubStatusFromDomainEntity(ServiceSubStatus serviceSubStatus) {
        SAPServiceSubStatus sapServiceSubStatus = new SAPServiceSubStatus();
        sapServiceSubStatus.setSubStatusName(serviceSubStatus.getName());
        sapServiceSubStatus.setSubStatusDescription(serviceSubStatus.getDescription());
        sapServiceSubStatus.setSubStatusCd(serviceSubStatus.getServiceSubStatusCd());
        sapServiceSubStatus.setSubStatusType(serviceSubStatus.getServiceStatus().getName());
        sapServiceSubStatus.setManuallyUpdatable(serviceSubStatus.getIsSetManually());
        return sapServiceSubStatus;
    }


    public static SAPServiceSubStatus getSAPServiceSubStatusFromDomainEntity(ServiceSubStatus serviceSubStatus, boolean isCurrentValues) {
        SAPServiceSubStatus sapServiceSubStatus = new SAPServiceSubStatus();
        sapServiceSubStatus.setSubStatusName(serviceSubStatus.getName());
        sapServiceSubStatus.setSubStatusDescription(serviceSubStatus.getDescription());
        sapServiceSubStatus.setSubStatusCd(serviceSubStatus.getServiceSubStatusCd());
        sapServiceSubStatus.setSubStatusType(serviceSubStatus.getServiceStatus().getName());
        if (!isCurrentValues)
            sapServiceSubStatus.setManuallyUpdatable(serviceSubStatus.getIsSetManually());
        else
            sapServiceSubStatus.setManuallyUpdatable(serviceSubStatus.getIsRemovedManually());

        return sapServiceSubStatus;
    }

    public static SAPCompanyEvent getSAPCompanyEventFromDomainEntity(CompanyEvent pCompanyEvent, EventType pEventType, Map<String, String> userCache) {
        SAPCompanyEvent companyEvent = CompanyTranslator.getSAPCompanyEventFromDomainEntity(pCompanyEvent, userCache);

        if (pEventType != null) {
            companyEvent.setEventGroupCode(pEventType.getEventGroupCd());
        }

        return companyEvent;
    }

    public static SAPCompanyEvent getSAPCompanyEventForTransactionResponse(String pTransmissionId, DomainEntitySet<CompanyEvent> pCompanyEvents) {
        SAPCompanyEvent sapCompanyEvent = new SAPCompanyEvent();

        SpcfCalendar eventDate = null;
        SpcfCalendar statusEffectiveDate = null;
        SpcfCalendar lastNoteDate = null;
        CompanyEventStatus eventStatus = CompanyEventStatus.Inactive;
        String creator = null;
        StringBuilder eventMessage = new StringBuilder();
        for (CompanyEvent companyEvent : pCompanyEvents) {
            EventType eventType = PayrollServices.entityFinder.findById(EventType.class, companyEvent.getEventTypeCd());
            if (eventDate == null) {
                eventDate = companyEvent.getEventTimeStamp();
            }

            if (eventStatus != CompanyEventStatus.Active && companyEvent.getStatusCd() == CompanyEventStatus.Active) {
                eventStatus = CompanyEventStatus.Active;
            }

            if (statusEffectiveDate == null) {
                statusEffectiveDate = companyEvent.getStatusEffectiveDate();
            } else if (companyEvent.getStatusEffectiveDate().after(statusEffectiveDate)) {
                statusEffectiveDate = companyEvent.getStatusEffectiveDate();
            }

            if (lastNoteDate == null) {
                lastNoteDate = companyEvent.getNoteLastUpdatedDate();
            } else if (companyEvent.getNoteLastUpdatedDate() != null && companyEvent.getNoteLastUpdatedDate().after(lastNoteDate)) {
                lastNoteDate = companyEvent.getNoteLastUpdatedDate();
            }

            if (creator == null) {
                creator = SAPTranslator.getUserNameFromUserID(companyEvent.getCreatorId());
            }

            eventMessage.append(eventType.getName()).append(" : ").append(eventType.getDescription()).append(" - ").append(companyEvent.getStatusCd()).append("\n");
            DomainEntitySet<CompanyEventDetail> companyEventDetails = companyEvent.getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());
            for (CompanyEventDetail companyEventDetail : companyEventDetails) {
                EventDetailType eventDetailType = PayrollServices.entityFinder.findById(EventDetailType.class, companyEventDetail.getEventDetailTypeCd());
                eventMessage.append("\t").append(eventDetailType.getName()).append(" - ").append(companyEventDetail.getValue()).append("\n");
            }
        }

        sapCompanyEvent.setOverrideMessage(eventMessage.toString());

        sapCompanyEvent.setEventDate(SAPTranslator.getDateFromSpcfCalendar(eventDate));

        EventType eventType = PayrollServices.entityFinder.findById(EventType.class, EventTypeCode.CloudResponse);
        sapCompanyEvent.setEventTypeCd(eventType.getEventTypeCd());
        sapCompanyEvent.setEventTypeName(eventType.getName());
        sapCompanyEvent.setEventTypeDescription(eventType.getDescription());
        sapCompanyEvent.setStatusCd(eventStatus);
        sapCompanyEvent.setStatusEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(statusEffectiveDate));
        sapCompanyEvent.setCreatorId(creator);
        sapCompanyEvent.setLastNoteDate(SAPTranslator.getDateFromSpcfCalendar(lastNoteDate));
        sapCompanyEvent.setTransmissionId(pTransmissionId);

        return sapCompanyEvent;
    }

    public static SAPCompanyEvent getSAPCompanyEventFromDomainEntity(CompanyEvent pCompanyEvent, Map<String, String> userCache) {
        SAPCompanyEvent eventReturnValue = new SAPCompanyEvent();

        // service status with onhold reasons are special the new status is "active current"
        // we change it to "on hold"
        if (pCompanyEvent.getEventTypeCd() == EventTypeCode.ServiceStatusChange) {
            deriveStatusDetails(pCompanyEvent);
        }

        // add the event details
        for (CompanyEventDetail domainEventDetail : pCompanyEvent.getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd())) {
            eventReturnValue.getCompanyEventDetails().add(getSAPCompanyEventDetail(domainEventDetail));
        }

        // add the related email info to the event
        for (CompanyEventEmail email : pCompanyEvent.getCompanyEventEmailCollection()) {
            eventReturnValue.getCompanyEventEmails().add(getSAPCompanyEventEmail(email));
        }

        // add the as/400 syncs to the event
        if (pCompanyEvent.getEventAs400Sync() != null) {
            eventReturnValue.getCompanyEventAs400Syncs().add(getSAPEventAs400SyncFromDomain(pCompanyEvent.getEventAs400Sync()));
        }

        fillBasicCompanyEventFields(eventReturnValue, pCompanyEvent, userCache);

        return eventReturnValue;
    }

    /**
     * This function is used to derive the service status to include "on hold" which is not a true status
     * NOTE: The company event details are altered
     *
     * @param pCompanyEvent - a company event
     * @return
     */
    private static void deriveStatusDetails(CompanyEvent pCompanyEvent) {
        CompanyEventDetail newStatusDetail = null;
        CompanyEventDetail oldStatusDetail = null;
        boolean hasNewOnHoldReason = false;
        boolean hasOldOnHoldReason = false;
        for (CompanyEventDetail domainEventDetail : pCompanyEvent.getCompanyEventDetailCollection()) {
            if (domainEventDetail.getEventDetailTypeCd() == EventDetailTypeCode.NewOnHoldReason) {
                hasNewOnHoldReason = true;
            }
            if (domainEventDetail.getEventDetailTypeCd() == EventDetailTypeCode.OldOnHoldReason) {
                hasOldOnHoldReason = true;
            }
            if (domainEventDetail.getEventDetailTypeCd() == EventDetailTypeCode.NewServiceStatus) {
                newStatusDetail = domainEventDetail;
            }
            if (domainEventDetail.getEventDetailTypeCd() == EventDetailTypeCode.OldServiceStatus) {
                oldStatusDetail = domainEventDetail;
            }
        }
        // if the event has an on hold reason change the status to "onHold"
        if (hasNewOnHoldReason && newStatusDetail != null) {
            newStatusDetail.setValue("On Hold");
        }
        if (hasOldOnHoldReason && oldStatusDetail != null) {
            oldStatusDetail.setValue("On Hold");
        }
    }

    public static SAPCompanyEventEmail getSAPCompanyEventEmail(final CompanyEventEmail pEventEmail) {
        SAPCompanyEventEmail sapEventEmail = new SAPCompanyEventEmail();

        sapEventEmail.setId(pEventEmail.getId().toString());
        sapEventEmail.setTemplateType(pEventEmail.getEmailTemplateTypeCd().toString());
        sapEventEmail.setStatus(pEventEmail.getStatusCd().toString());
        sapEventEmail.setEffectiveDate(pEventEmail.getStatusEffectiveDate().toLocal().format("MM/dd/yyyy hh:mm a"));

        for (CompanyEventEmailParam param : pEventEmail.getEmailParamsForEmailEvent()) {
            sapEventEmail.getEmailParams().add(getSAPCompanyEventEmailParam(param));
        }

        // Set if the event is MTL Supported or not
        if(CompanyEventEmail.isEventMtlCompliant(pEventEmail)) {
            sapEventEmail.setMtlEnabled(true);
        } else {
            sapEventEmail.setMtlEnabled(false);
        }

        return sapEventEmail;
    }


    public static SAPCompanyEventEmailParam getSAPCompanyEventEmailParam(final CompanyEventEmailParam pEventEmailParam) {
        SAPCompanyEventEmailParam sapEventEmailParam = new SAPCompanyEventEmailParam();

        sapEventEmailParam.setParamType(pEventEmailParam.getParamTypeCd().toString());

        String paramValue = pEventEmailParam.getValue();

        if (paramValue == null) {
            paramValue = "";
        }

        // trim off any html line break to keep display of details neat in UI
        if (paramValue.toLowerCase().endsWith("<br>")) {
            paramValue = paramValue.substring(0, paramValue.length() - 4);
        }

        sapEventEmailParam.setParamValue(paramValue);

        return sapEventEmailParam;
    }

    public static SAPCompanyEventDetail getSAPCompanyEventDetail(final CompanyEventDetail pCompanyEventDetail) {
        SAPCompanyEventDetail sapEventDetail = new SAPCompanyEventDetail();
        EventDetailType eventDetailType = PayrollServices.entityFinder.findById(EventDetailType.class, pCompanyEventDetail.getEventDetailTypeCd());
        sapEventDetail.setEventDetailTypeCd(pCompanyEventDetail.getEventDetailTypeCd());
        sapEventDetail.setName(eventDetailType.getName());
        sapEventDetail.setValue(pCompanyEventDetail.getValue());
        sapEventDetail.setValueClassName(eventDetailType.getValueClassName());
        return sapEventDetail;
    }

    public static SAPEventAs400Sync getSAPEventAs400SyncFromDomain(EventAs400Sync pEventAs400Sync) {
        SAPEventAs400Sync sapEventSync = new SAPEventAs400Sync();
        sapEventSync.setId(pEventAs400Sync.getId().toString());
        sapEventSync.setStatus(pEventAs400Sync.getStatusCd().toString());
        sapEventSync.setRetryCount(pEventAs400Sync.getRetryCount());
        return sapEventSync;
    }


    private static void fillBasicCompanyEventFields(SAPCompanyEvent pSAPCompanyEvent, CompanyEvent pCompanyEvent, Map<String, String> userCache) {
        pSAPCompanyEvent.setEventDate(SAPTranslator.getDateFromSpcfCalendar(pCompanyEvent.getEventTimeStamp()));
        pSAPCompanyEvent.setEventTypeCd(pCompanyEvent.getEventTypeCd());
        EventType eventType = PayrollServices.entityFinder.findById(EventType.class, pCompanyEvent.getEventTypeCd());
        pSAPCompanyEvent.setEventTypeName(eventType.getName());
        pSAPCompanyEvent.setEventTypeDescription(eventType.getDescription());
        pSAPCompanyEvent.setStatusCd(pCompanyEvent.getStatusCd());
        pSAPCompanyEvent.setStatusEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(pCompanyEvent.getStatusEffectiveDate()));
        pSAPCompanyEvent.setCreatorId(SAPTranslator.getUserNameFromUserID(pCompanyEvent.getCreatorId(), userCache));
        pSAPCompanyEvent.setLastNoteDate(SAPTranslator.getDateFromSpcfCalendar(pCompanyEvent.getNoteLastUpdatedDate()));
        pSAPCompanyEvent.setId(pCompanyEvent.getId().toString());
    }


    public static SAPCompanyBankAccountHistory getSAPCompanyBankAccountHistoryFromDomainEntities(CompanyBankAccount pCompanyBankAccount, DomainEntitySet<PropertyAudit> propertyAudits, boolean canViewFullBankAccountNumbers) {
        SAPCompanyBankAccountHistory sapCompanyBankAccountHistory = getSAPCompanyBankAccountHistoryFromDomainEntity(pCompanyBankAccount, canViewFullBankAccountNumbers);
        ArrayList<SAPPropertyAudit> auditList = new ArrayList<SAPPropertyAudit>();
        for (PropertyAudit propertyAudit : propertyAudits) {
            // skip status effective date
            if (!propertyAudit.getPropertyName().equals(CompanyBankAccount.StatusEffectiveDate().getPropertyName())) {
                auditList.add(PropertyAuditTranslator.getSAPPropertyAuditFromDomainEntity(propertyAudit));
            }
        }
        sapCompanyBankAccountHistory.setPropertyAudit(auditList);
        return sapCompanyBankAccountHistory;
    }

    public static SAPCompanyBankAccount getSAPCompanyBankAccountFromDomainEntity(CompanyBankAccount pBankAccount, boolean canViewFullBankAccount) {
        SAPCompanyBankAccount account = getSAPCompanyBankAccountFromDomainEntity(pBankAccount.getBankAccount(), canViewFullBankAccount);
        account.setAccountId(pBankAccount.getSourceBankAccountId());
        account.setBankAccountStatusCd(pBankAccount.getStatusCd());
        account.setVerifyRetryCount(pBankAccount.getVerifyRetryCount());
        account.setSourceBankAccountName(pBankAccount.getSourceBankAccountName());
        account.setSourceBankAccountId(pBankAccount.getSourceBankAccountId());
        return account;
    }

    public static SAPCompanyBankAccount getSAPCompanyBankAccountFromDomainEntity(BankAccount pBankAccount, boolean canViewFullBankAccount) {
        SAPCompanyBankAccount account = new SAPCompanyBankAccount();
        if (pBankAccount != null) {
            account.setAccountNumber(PIIMask.maskText(pBankAccount.getAccountNumber(), !canViewFullBankAccount));
            account.setRoutingNumber(pBankAccount.getRoutingNumber());
            account.setBankName(pBankAccount.getBankName());
            account.setAccountType(pBankAccount.getAccountTypeCd());
        }
        return account;
    }


    public static SAPCompanyBankAccountHistory getSAPCompanyBankAccountHistoryFromDomainEntity(CompanyBankAccount pBankAccount, boolean canViewFullBankAccountNumbers) {
        SAPCompanyBankAccountHistory account = new SAPCompanyBankAccountHistory();
        account.setAccountId(pBankAccount.getSourceBankAccountId());
        account.setAccountNumber(PIIMask.maskText(pBankAccount.getBankAccount().getAccountNumber(), !canViewFullBankAccountNumbers));
        account.setRoutingNumber(pBankAccount.getBankAccount().getRoutingNumber());
        account.setBankName(pBankAccount.getBankAccount().getBankName());
        account.setAccountType(pBankAccount.getBankAccount().getAccountTypeCd());
        account.setBankAccountStatusCd(pBankAccount.getStatusCd());
        account.setVerifyRetryCount(pBankAccount.getVerifyRetryCount());
        account.setSourceBankAccountName(pBankAccount.getSourceBankAccountName());
        account.setSourceBankAccountId(pBankAccount.getSourceBankAccountId());
        account.setStatusEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(pBankAccount.getStatusEffectiveDate()));
        return account;
    }

    public static SAPEmployeeInfo getSAPEmployeeInfoFromDomainEntity(Employee pEmployee, SpcfCalendar lastPayDate, boolean canViewSSN) {
        SAPEmployeeInfo sapEmployee = new SAPEmployeeInfo();
        sapEmployee.setEmployeeId(pEmployee.getSourceEmployeeId());
        sapEmployee.setFirstName(pEmployee.getFirstName());
        sapEmployee.setLastName(pEmployee.getLastName());
        sapEmployee.setMiddleName(pEmployee.getMiddleName());
        sapEmployee.setMailingAddress(getSAPAddressFromDomainEntity(pEmployee.getMailingAddress()));
        sapEmployee.setSocialSecurityNumber(PIIMask.maskText(pEmployee.getTaxId(), !canViewSSN));
        sapEmployee.setLastPayDate(SAPTranslator.getDateFromSpcfCalendar(lastPayDate));
        sapEmployee.setFirstPayDate(SAPTranslator.getDateFromSpcfCalendar(pEmployee.getFirstPayrollReceivedDate()));
        sapEmployee.setBirthDate(SAPTranslator.getDateFromSpcfCalendar(pEmployee.getBirthDate()));

        sapEmployee.setHireDate(SAPTranslator.getDateFromSpcfCalendar(pEmployee.getHireDate()));
        sapEmployee.setRehireDate(SAPTranslator.getDateFromSpcfCalendar(pEmployee.getReHireDate()));
        sapEmployee.setTermDate(SAPTranslator.getDateFromSpcfCalendar(pEmployee.getTerminationDate()));
        sapEmployee.setStatus(pEmployee.getStatusCd().toString());
        sapEmployee.setEmployeeGseq(pEmployee.getId().toString());

        if (pEmployee.getQbdtEmployeeInfo() != null) {
            sapEmployee.setEnforceSubjectTo(pEmployee.getQbdtEmployeeInfo().getEnforceSubjectTo());
                if(pEmployee.getQbdtEmployeeInfo().getEmployeeSeasonal()!=null){
                    if(pEmployee.getQbdtEmployeeInfo().getEmployeeSeasonal().toString().equalsIgnoreCase("SEASONAL")){
                        sapEmployee.setIsSeasonal("Y");
                    }else if(pEmployee.getQbdtEmployeeInfo().getEmployeeSeasonal().toString().equalsIgnoreCase("NONSEASONAL")){
                        sapEmployee.setIsSeasonal("N");
                    }else{
                        sapEmployee.setIsSeasonal("");
                    }
                logger.info("getEmployeeSeasonal value ="+pEmployee.getQbdtEmployeeInfo().getEmployeeSeasonal().toString());
            }
        }
        boolean employeeHasABankAccount = pEmployee.getEmployeeBankAccountCollection().size() > 0;
        sapEmployee.setDd(employeeHasABankAccount);
        sapEmployee.setStateLive(pEmployee.getLiveState());
        sapEmployee.setStateWork(pEmployee.getWorkState());
        return sapEmployee;
    }

    public static SAPVendorInfo getSAPVendorInfoFromDomainEntity(Payee pPayee) {
        SAPVendorInfo vendorInfo = new SAPVendorInfo();
        vendorInfo.setName(pPayee.getName());
        vendorInfo.setEmail(pPayee.getEmail());
        vendorInfo.setPhone(pPayee.getPhone());
        vendorInfo.setSourceId(pPayee.getSourcePayeeId());
        return vendorInfo;
    }

    public ArrayList<SAPEmployeeBankAccountHistory> setSAPEmployeeBankAccountHistory(DomainEntitySet<EmployeeBankAccount> pEmployeeBankAccountList, Company pCompany, boolean canViewFullBankAccountNumbers) {
        ArrayList<String> employeeBankAccountIds = new ArrayList<String>();
        ArrayList<SAPEmployeeBankAccountHistory> sapEmployeeBankAccountHistory = new ArrayList<SAPEmployeeBankAccountHistory>();

        for (EmployeeBankAccount employeeBankAccount : pEmployeeBankAccountList) {
            String employeeBankAccountId = employeeBankAccount.getSourceBankAccountId();
            if (!employeeBankAccountIds.contains(employeeBankAccountId)) {
                employeeBankAccountIds.add(employeeBankAccountId);
                sapEmployeeBankAccountHistory.add(getSAPEmployeeBankAccountHistory(employeeBankAccountId, setSAPEmployeeBankAccountHistoryItems(pEmployeeBankAccountList, employeeBankAccountId, employeeBankAccount.getId().toString(), pCompany, canViewFullBankAccountNumbers)));
            }
        }


        return sapEmployeeBankAccountHistory;
    }

    public ArrayList<SAPEmployeeBankAccountHistory> setSAPVendorBankAccountHistory(DomainEntitySet<PayeeBankAccount> pPayeeBankAccounts, Company pCompany, boolean canViewFullBankAccountNumbers) {
        ArrayList<String> employeeBankAccountIds = new ArrayList<String>();
        ArrayList<SAPEmployeeBankAccountHistory> sapEmployeeBankAccountHistory = new ArrayList<SAPEmployeeBankAccountHistory>();

        for (PayeeBankAccount payeeBankAccount : pPayeeBankAccounts) {
            String employeeBankAccountId = payeeBankAccount.getSourceBankAccountId();
            if (!employeeBankAccountIds.contains(employeeBankAccountId)) {
                employeeBankAccountIds.add(employeeBankAccountId);
                sapEmployeeBankAccountHistory.add(getSAPEmployeeBankAccountHistory(employeeBankAccountId, setSAPVendorBankAccountHistoryItems(pPayeeBankAccounts, employeeBankAccountId, payeeBankAccount.getId().toString(), pCompany, canViewFullBankAccountNumbers)));
            }
        }


        return sapEmployeeBankAccountHistory;
    }

    private static SAPEmployeeBankAccountHistory getSAPEmployeeBankAccountHistory(String pBankAccountId, ArrayList<SAPEmployeeBankAccountHistoryItem> pEmployeeBankAccountList) {
        SAPEmployeeBankAccountHistory sapEmployeeBankAccountHistory = new SAPEmployeeBankAccountHistory();
        sapEmployeeBankAccountHistory.setAccountId(pBankAccountId);
        sapEmployeeBankAccountHistory.setEmployeeBankAccountHistoryItems(pEmployeeBankAccountList);
        return sapEmployeeBankAccountHistory;
    }

    public ArrayList<SAPEmployeeBankAccountHistoryItem> setSAPEmployeeBankAccountHistoryItems(DomainEntitySet<EmployeeBankAccount> pEmployeeBankAccountList, String pBankAccountId, String pBankAccountGUID, Company pCompany, boolean canViewFullBankAccountNumbers) {
        ArrayList<SAPEmployeeBankAccountHistoryItem> sapEmployeeBankAccountHistoryList = new ArrayList<SAPEmployeeBankAccountHistoryItem>();
        EmployeeBankAccount lastEmployeeBankAccount = null;
        for (EmployeeBankAccount employeeBankAccountWithAccountId : pEmployeeBankAccountList) {
            if (employeeBankAccountWithAccountId.getSourceBankAccountId().equals(pBankAccountId)) {
                sapEmployeeBankAccountHistoryList.add(getEmployeeBankAccountHistoryItem((lastEmployeeBankAccount != null) ? lastEmployeeBankAccount.getBankAccount() : null, employeeBankAccountWithAccountId.getBankAccount(), canViewFullBankAccountNumbers));
                lastEmployeeBankAccount = employeeBankAccountWithAccountId;
            }
        }

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(pCompany, EventDetailTypeCode.EmployeeBankAccountId, pBankAccountGUID);
        for (CompanyEvent event : companyEvents) {
            SAPEmployeeBankAccountHistoryItem sapEmployeeBankAccountHistoryItem = new SAPEmployeeBankAccountHistoryItem();
            sapEmployeeBankAccountHistoryItem.setChangeDate(SAPTranslator.getDateFromSpcfCalendar(event.getEventTimeStamp()));
            sapEmployeeBankAccountHistoryItem.setChangedBy(SAPTranslator.getUserNameFromUserID(event.getCreatorId()));
            // exclude fraud events
            boolean isChangeEvent = false;
            for (CompanyEventDetail detail : event.getCompanyEventDetailCollection()) {
                if (detail.getEventDetailTypeCd() == EventDetailTypeCode.OldAccountNumber && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setOldAccountNumber(detail.getValue());
                    isChangeEvent = true;
                } else if (detail.getEventDetailTypeCd() == EventDetailTypeCode.NewAccountNumber && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setAccountNumber(detail.getValue());
                    isChangeEvent = true;
                } else if (detail.getEventDetailTypeCd() == EventDetailTypeCode.OldRoutingNumber && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setOldRoutingNumber(detail.getValue());
                    isChangeEvent = true;
                } else if (detail.getEventDetailTypeCd() == EventDetailTypeCode.NewRoutingNumber && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setRoutingNumber(detail.getValue());
                    isChangeEvent = true;
                } else if (detail.getEventDetailTypeCd() == EventDetailTypeCode.OldAccountType && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setOldAccountTypeCd(BankAccountType.valueOf(detail.getValue()));
                    isChangeEvent = true;
                } else if (detail.getEventDetailTypeCd() == EventDetailTypeCode.NewAccountType && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setAccountTypeCd(BankAccountType.valueOf(detail.getValue()));
                    isChangeEvent = true;
                }
            }
            if (isChangeEvent) {
                sapEmployeeBankAccountHistoryList.add(sapEmployeeBankAccountHistoryItem);
            }
        }

        //Reverse collection
        Collections.reverse(sapEmployeeBankAccountHistoryList);

        return sapEmployeeBankAccountHistoryList;
    }

    public ArrayList<SAPEmployeeBankAccountHistoryItem> setSAPVendorBankAccountHistoryItems(DomainEntitySet<PayeeBankAccount> pPayeeBankAccounts, String pBankAccountId, String pBankAccountGUID, Company pCompany, boolean canViewFullBankAccountNumbers) {
        ArrayList<SAPEmployeeBankAccountHistoryItem> sapEmployeeBankAccountHistoryList = new ArrayList<SAPEmployeeBankAccountHistoryItem>();
        PayeeBankAccount lastPayeeBankAccount = null;
        for (PayeeBankAccount payeeBankAccount : pPayeeBankAccounts) {
            if (payeeBankAccount.getSourceBankAccountId().equals(pBankAccountId)) {
                sapEmployeeBankAccountHistoryList.add(getEmployeeBankAccountHistoryItem((lastPayeeBankAccount != null) ? lastPayeeBankAccount.getBankAccount() : null, payeeBankAccount.getBankAccount(), canViewFullBankAccountNumbers));
                lastPayeeBankAccount = payeeBankAccount;
            }
        }

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(pCompany, EventDetailTypeCode.PayeeBankAccountId, pBankAccountGUID);
        for (CompanyEvent event : companyEvents) {
            SAPEmployeeBankAccountHistoryItem sapEmployeeBankAccountHistoryItem = new SAPEmployeeBankAccountHistoryItem();
            sapEmployeeBankAccountHistoryItem.setChangeDate(SAPTranslator.getDateFromSpcfCalendar(event.getEventTimeStamp()));
            sapEmployeeBankAccountHistoryItem.setChangedBy(SAPTranslator.getUserNameFromUserID(event.getCreatorId()));
            for (CompanyEventDetail detail : event.getCompanyEventDetailCollection()) {
                if (detail.getEventDetailTypeCd() == EventDetailTypeCode.OldAccountNumber && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setOldAccountNumber(detail.getValue());
                } else if (detail.getEventDetailTypeCd() == EventDetailTypeCode.NewAccountNumber && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setAccountNumber(detail.getValue());
                } else if (detail.getEventDetailTypeCd() == EventDetailTypeCode.OldRoutingNumber && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setOldRoutingNumber(detail.getValue());
                } else if (detail.getEventDetailTypeCd() == EventDetailTypeCode.NewRoutingNumber && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setRoutingNumber(detail.getValue());
                } else if (detail.getEventDetailTypeCd() == EventDetailTypeCode.OldAccountType && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setOldAccountTypeCd(BankAccountType.valueOf(detail.getValue()));
                } else if (detail.getEventDetailTypeCd() == EventDetailTypeCode.NewAccountType && detail.getValue() != null) {
                    sapEmployeeBankAccountHistoryItem.setAccountTypeCd(BankAccountType.valueOf(detail.getValue()));
                }
            }
            sapEmployeeBankAccountHistoryList.add(sapEmployeeBankAccountHistoryItem);
        }

        //Reverse collection
        Collections.reverse(sapEmployeeBankAccountHistoryList);

        return sapEmployeeBankAccountHistoryList;
    }

    public static SAPCompanyEventGroup getSAPCompanyEventGroup(EventGroup eventGroup) {
        SAPCompanyEventGroup sapCompanyEventGroup = new SAPCompanyEventGroup();
        sapCompanyEventGroup.setEventGroupCode(eventGroup.toString());
        sapCompanyEventGroup.setName(eventGroup.name());
        sapCompanyEventGroup.setChildren(new ArrayList<SAPCompanyEventGroupItem>());
        return sapCompanyEventGroup;
    }

    public static SAPCompanyEventGroupItem getSAPCompanyEventGroupItem(EventType eventType) {
        SAPCompanyEventGroupItem eventGroupItem = new SAPCompanyEventGroupItem();
        eventGroupItem.setEventTypeCd(eventType.getEventTypeCd().toString());
        eventGroupItem.setEventTypeName(eventType.getName());
        return eventGroupItem;
    }

    public static SAPEmployeeBankAccountHistoryItem getEmployeeBankAccountHistoryItem(BankAccount lastBankAccount, BankAccount bankAccount, boolean canViewFullBankAccountNumbers) {
        SAPEmployeeBankAccountHistoryItem sapEmployeeBankAccountHistoryItem = new SAPEmployeeBankAccountHistoryItem();
        if (lastBankAccount != null) {
            sapEmployeeBankAccountHistoryItem.setOldAccountNumber(PIIMask.maskText(lastBankAccount.getAccountNumber(), !canViewFullBankAccountNumbers));
            sapEmployeeBankAccountHistoryItem.setOldRoutingNumber(lastBankAccount.getRoutingNumber());
            sapEmployeeBankAccountHistoryItem.setOldAccountTypeCd(lastBankAccount.getAccountTypeCd());
        }

        sapEmployeeBankAccountHistoryItem.setAccountNumber(PIIMask.maskText(bankAccount.getAccountNumber(), !canViewFullBankAccountNumbers));
        sapEmployeeBankAccountHistoryItem.setRoutingNumber(bankAccount.getRoutingNumber());
        sapEmployeeBankAccountHistoryItem.setAccountTypeCd(bankAccount.getAccountTypeCd());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(bankAccount.getCreatedDate().toLocal().getTimeInMilliseconds());
        sapEmployeeBankAccountHistoryItem.setChangeDate(calendar.getTime());

        sapEmployeeBankAccountHistoryItem.setChangedBy(SAPTranslator.getUserNameFromUserID(bankAccount.getCreatorId()));

        return sapEmployeeBankAccountHistoryItem;
    }


    @Deprecated
    public static SAPAgreementInfo getAgreementInfoFromDomainEntity(QuickbooksInfo agreementInfo) {
        if (agreementInfo != null) {
            SAPAgreementInfo sapAgreementInfo = new SAPAgreementInfo();
            sapAgreementInfo.setName(null);
            sapAgreementInfo.setSubscriptionNumber(null);
            sapAgreementInfo.setServiceKey(null);
            sapAgreementInfo.setServiceType(null);
            sapAgreementInfo.setAgreementSubType(null);
            return sapAgreementInfo;
        }
        return null;
    }

    public static SAPCompanyServiceStatusHistoryItem getSAPCompanyStatusHistoryItemFromDomainEntity(String serviceCd, String newStatus, String oldStatus, SpcfCalendar changeDate, String changedBy, Collection<String> newOnHoldReasons, Collection<String> oldOnHoldReasons) {
        SAPCompanyServiceStatusHistoryItem serviceStatusHistoryItem = new SAPCompanyServiceStatusHistoryItem();

        serviceStatusHistoryItem.setServiceCd(serviceCd);
        serviceStatusHistoryItem.setChangeDate(SAPTranslator.getDateFromSpcfCalendar(changeDate));
        serviceStatusHistoryItem.setChangedBy(SAPTranslator.getUserNameFromUserID(changedBy));
        serviceStatusHistoryItem.setNewServiceStatus(newStatus);
        serviceStatusHistoryItem.setOldServiceStatus(oldStatus);
        serviceStatusHistoryItem.setNewSubStatuses(newOnHoldReasons);
        serviceStatusHistoryItem.setOldSubStatuses(oldOnHoldReasons);

        return serviceStatusHistoryItem;
    }


    public static SAPRandomDebit getSAPRandomDebitsFromDomainEntities(DomainEntitySet<FinancialTransaction> verifyTransactionList) throws Exception {

        SAPRandomDebit sapRandomDebit = new SAPRandomDebit();

        int txn_count = 1;
        for (FinancialTransaction txn : verifyTransactionList) {
            if (txn_count == 1) {
                if (txn.getSettlementDate() != null) {
                    sapRandomDebit.setSettlementDate(SAPTranslator.getDateFromSpcfCalendar(txn.getSettlementDate()));
                }

                if (txn.getMoneyMovementTransaction() != null && txn.getMoneyMovementTransaction().getOffloadBatch() != null) {
                    sapRandomDebit.setOffloadedDate(SAPTranslator.getDateFromSpcfCalendar(txn.getMoneyMovementTransaction().getOffloadBatch().getOffloadDate()));
                }

                if (txn.getFinancialTransactionAmount() != null) {
                    sapRandomDebit.setAmount1(txn.getFinancialTransactionAmount().toString());
                }
            }
            if (txn_count == 2) {
                if (txn.getFinancialTransactionAmount() != null) {
                    sapRandomDebit.setAmount2(txn.getFinancialTransactionAmount().toString());
                }
            }
            if (txn_count == 3) {
                throw new Exception("More than 2 verification debits found for one date set");
            }
            txn_count++;
        }
        return sapRandomDebit;
    }

    public static SAPQuickbooksInfo getQuickbooksInfoFromDomainEntity(QuickbooksInfo quickbooksInfo) {
        if (quickbooksInfo != null) {
            SAPQuickbooksInfo sapQuickbooksInfo = new SAPQuickbooksInfo();
            sapQuickbooksInfo.setApplicationVersion(quickbooksInfo.getApplicationVersion());
            sapQuickbooksInfo.setTaxTable(quickbooksInfo.getTaxTableId());
            sapQuickbooksInfo.setCoaFeeAccountName(quickbooksInfo.getCoaFeeAccountName());
            sapQuickbooksInfo.setCoaSalesTaxAccountName(quickbooksInfo.getCoaSalesTaxAccountName());
            sapQuickbooksInfo.setLicenseNumber(quickbooksInfo.getLicenseNumber());
            sapQuickbooksInfo.setProcessTransmissions(quickbooksInfo.getProcessTransmissions());
            sapQuickbooksInfo.setAllowTransmissions(quickbooksInfo.getAllowTransmissions());
            sapQuickbooksInfo.setFileId(StringUtils.defaultIfEmpty(quickbooksInfo.getFileId(), ""));
            return sapQuickbooksInfo;
        }
        return null;
    }

    public static CompanyBankAccountDTO createCompanyBankAccountDTO(String pCompanyBankAccountID,
                                                                    String pSourceBankAccountName,
                                                                    String pAccountNumber,
                                                                    String pRoutingNumber,
                                                                    String pAccountType,
                                                                    String pBankName) {
        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber(pAccountNumber);
        bankAccountDTO.setAccountType(BankAccountType.valueOf(pAccountType));
        bankAccountDTO.setBankName(pBankName);
        bankAccountDTO.setRoutingNumber(pRoutingNumber);

        CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();
        companyBankAccountDTO.setBankAccountDTO(bankAccountDTO);
        companyBankAccountDTO.setSourceBankAccountName(pSourceBankAccountName);
        companyBankAccountDTO.setCompanyBankAccountID(pCompanyBankAccountID);

        return companyBankAccountDTO;
    }

    public static SAPTransmission getTransmissionFromDomainEntity(SourceSystemTransmission transmission, boolean includeDocuments) {
        try {
            SAPTransmission sapTransmission = new SAPTransmission();

            // time in seconds
            if (transmission.getFinalizeDateTime() != null && transmission.getInitializeDateTime() != null) {
                sapTransmission.setConnectionTime((transmission.getFinalizeDateTime().getTimeInMilliseconds() - transmission.getInitializeDateTime().getTimeInMilliseconds()) / 1000.00);
            } else {
                sapTransmission.setConnectionTime(-1);
            }
            sapTransmission.setDescription(transmission.getDescription());
            sapTransmission.setInitializeDateTime(SAPTranslator.getDateFromSpcfCalendar(transmission.getInitializeDateTime()));
            sapTransmission.setRequestToken(transmission.getRequestToken());
            sapTransmission.setResponseToken(transmission.getResponseToken());
            sapTransmission.setTransmissionIdentifier(transmission.getId().toString());
            sapTransmission.setIpAddress(transmission.getIPAddress());
            sapTransmission.setCompanyName(transmission.getCompanyName());
            sapTransmission.setCompanyKey(new SAPCompanyKey(transmission.getCompany()));

            // these documents are very heavy so they are not always included
            if (includeDocuments) {
                AuthUser foundUser = AuthUser.findUser(Application.getCurrentPrincipal().getId());
                Boolean canViewFullBankAccountNumber = foundUser.hasOperation(OperationId.ViewFullBankAccountNumbers);
                Boolean canViewEEPII = foundUser.hasOperation(OperationId.ViewEEPII);
                DomainEntityChangeManager.setDomainEntityChangeModelContext(transmission.getClass(), transmission);
                if (transmission.getRequestDocument() != null) {
                    int logLimitInBytes = 1048576;
                    String requestString = transmission.getRequestDocument();
                    sapTransmission.setRequestDocument(PIIMask.getMaskedString(
                            requestString.length() > logLimitInBytes ? requestString.substring(0, logLimitInBytes) : requestString,
                            canViewFullBankAccountNumber, canViewEEPII));
                    sapTransmission.setLargerLog(requestString.length() > logLimitInBytes);
                }
                if (transmission.getResponseDocument() != null) {
                    sapTransmission.setResponseDocument(PIIMask.getMaskedString(transmission.getResponseDocument(), canViewFullBankAccountNumber, canViewEEPII));
                }
            }

            return sapTransmission;
        } finally {
            DomainEntityChangeManager.removeDomainEntityChangeModel();
        }
    }

    public static SAPTransmission getTransmissionFromDomainEntity(EntitlementMessage transmission, boolean includeDocuments) {
        SAPTransmission sapTransmission = new SAPTransmission();

        sapTransmission.setConnectionTime(-1);
        sapTransmission.setDescription(transmission.getEventReason());
        sapTransmission.setInitializeDateTime(SAPTranslator.getDateFromSpcfCalendar(transmission.getCreatedDate()));
        sapTransmission.setRequestToken(0);
        sapTransmission.setResponseToken(0);
        sapTransmission.setTransmissionIdentifier(transmission.getId().toString());

        // these documents are very heavy so they are not always included
        if (includeDocuments) {
            AuthUser foundUser = AuthUser.findUser(Application.getCurrentPrincipal().getId());
            Boolean canViewFullBankAccountNumber = false;//foundUser.hasOperation(OperationId.ViewFullBankAccountNumbers);
            Boolean canViewEEPII = foundUser.hasOperation(OperationId.ViewEEPII);
            int logLimitInBytes = 1048576;

            if (transmission.getMessage() != null) {
                String requestString = transmission.getMessage();
                sapTransmission.setRequestDocument(PIIMask.getMaskedString(
                        requestString.length() > logLimitInBytes ? requestString.substring(0, logLimitInBytes) : requestString,
                        canViewFullBankAccountNumber, canViewEEPII));
            }
            String response = "Status: " + transmission.getStatus() + "\n";
            if (transmission.getLastFailureMessage() != null) {
                response += "Error Message: " + transmission.getLastFailureMessage();
            }
            sapTransmission.setResponseDocument(response);
        }

        return sapTransmission;
    }

    public static SAPTransmission getSAPTransmissionByIPAndDate(Object[] transmission) {
        SAPTransmission sapTransmission = new SAPTransmission();
        if(transmission == null) {
            return null;
        }
        sapTransmission.setPsid(transmission[3].toString());
        sapTransmission.setCompanyName(transmission[1].toString());
        sapTransmission.setIpAddress(transmission[2].toString());
        sapTransmission.setLoginTime(transmission[0].toString());
        sapTransmission.setCompanyKey(new SAPCompanyKey(transmission[4].toString(),transmission[3].toString()));


        return sapTransmission;
    }

    public static SAPFraudEvent getSAPFraudEvent(FraudEvent pFraudEvent) {
        SAPFraudEvent sapEvent = new SAPFraudEvent();

        // these properties should exist for every sapEvent
        sapEvent.setFraudIndicator(pFraudEvent.getFraudCategory());
        sapEvent.setCompanyEin(pFraudEvent.getCompany().getFedTaxId());
        sapEvent.setCompanyId(pFraudEvent.getCompany().getSourceCompanyId());
        sapEvent.setCompanyName(pFraudEvent.getCompany().getLegalName());
        sapEvent.setSourceSystemCd(pFraudEvent.getCompany().getSourceSystemCd().name());
        // truncate really long details they mess up the ui
        if (pFraudEvent.getFraudTriggerDetail() != null && pFraudEvent.getFraudTriggerDetail().length() > 200) {
            sapEvent.setDetails(pFraudEvent.getFraudTriggerDetail().substring(0, 200) + "...");
        } else {
            sapEvent.setDetails(pFraudEvent.getFraudTriggerDetail());
        }
        sapEvent.setFraudFlagSet(pFraudEvent.getCompany().getIsFlaggedForFraud());
        sapEvent.setEventTimeStamp(SAPTranslator.getDateFromSpcfCalendar(pFraudEvent.getEventTimeStamp()));

        if (pFraudEvent.getPayrollRun() != null) {
            sapEvent.setPayrollAmount(SAPTranslator.getDoubleFromSpcfMoney(pFraudEvent.getPayrollRun().getPayrollDirectDepositAmount()));
            sapEvent.setSourcePayRunId(pFraudEvent.getPayrollRun().getSourcePayRunId());
            sapEvent.setPayrollRunDate(SAPTranslator.getDateFromSpcfCalendar(pFraudEvent.getPayrollRun().getPayrollRunDate()));
            sapEvent.setPayrollCheckDate(SAPTranslator.getDateFromSpcfCalendar(pFraudEvent.getPayrollRun().getPaycheckDate()));
            sapEvent.setPayrollRunStatus(pFraudEvent.getPayrollRun().getPayrollRunStatus().toString());
        } else {
            // special double that tells flex that this should be null
            sapEvent.setPayrollAmount(-1);
        }
        if (pFraudEvent.getEmployee() != null) {
            sapEvent.setEmployeeName(SAPTranslator.getEmployeeFullName(pFraudEvent.getEmployee()));
        }
        return sapEvent;
    }

    public static SAPCompanyEventType getCompanyEventTypeFromDomainEntity(EventType eventType) {
        SAPCompanyEventType sapEventType = new SAPCompanyEventType();
        sapEventType.setEventTypeCode(eventType.getEventTypeCd().toString());
        sapEventType.setEventTypeName(eventType.getName());
        return sapEventType;
    }

    public static SAPCompanyLegalInfo getCompanyLegalInfoFromDomainEntity(Company pCompany, EntityChange pEntityChange) {
        SAPCompanyLegalInfo legalInfo = new SAPCompanyLegalInfo();
        legalInfo.setLegalName(pCompany.getLegalName());
        legalInfo.setDoingBusinessAs(pCompany.getDbaName());
        legalInfo.setAddress(getSAPAddressFromDomainEntity(pCompany.getLegalAddress()));
        legalInfo.setEin(pCompany.getFedTaxId());
        legalInfo.setPsid(pCompany.getSourceCompanyId());

        if(pCompany.getCompanyAdditionalInfo() != null && pCompany.getCompanyAdditionalInfo().getIndustryType() != null){
            legalInfo.setIndustryType(pCompany.getCompanyAdditionalInfo().getIndustryType().getIndustry());
        }
        if (pEntityChange != null && pEntityChange.getEffectiveDate() != null) {
            legalInfo.setEinEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(pEntityChange.getEffectiveDate()));
        }
        return legalInfo;
    }

    public static SAPCheckPrintingBatch getSAPCheckPrintingBatchFromCheckPrintBatch(CompanyPaycheckBatch pCheckPrintBatch) {
        SAPCheckPrintingBatch sapCheckPrintingBatch = new SAPCheckPrintingBatch();
        Company company = pCheckPrintBatch.getCompany();
        sapCheckPrintingBatch.setCompanyKey(new SAPCompanyKey(company.getSourceSystemCd().toString(), company.getSourceCompanyId()));
        sapCheckPrintingBatch.setEin(company.getFedTaxId());
        sapCheckPrintingBatch.setPsid(company.getSourceCompanyId());
        sapCheckPrintingBatch.setLegalName(company.getLegalName());
        sapCheckPrintingBatch.setMaxPaycheckId(pCheckPrintBatch.findMinMaxPaycheckId(false));
        sapCheckPrintingBatch.setMinPaycheckId(pCheckPrintBatch.findMinMaxPaycheckId(true));
        sapCheckPrintingBatch.setPaycheckCount(pCheckPrintBatch.getNumberOfChecks());
        sapCheckPrintingBatch.setPaycheckDate(SAPTranslator.getDateFromSpcfCalendar(pCheckPrintBatch.getPaycheckDate()));
        sapCheckPrintingBatch.setPrintBatchId(pCheckPrintBatch.getId().toString());
        sapCheckPrintingBatch.setPrintMessage(pCheckPrintBatch.getCheckPrintBatchMessage());
        sapCheckPrintingBatch.setPrintStatus(pCheckPrintBatch.getCheckPrintBatchStatusCode().toString());
        sapCheckPrintingBatch.setSentToPrinterDate(SAPTranslator.getDateFromSpcfCalendar(pCheckPrintBatch.getSentToPrinter()));
        return sapCheckPrintingBatch;
    }

    public static SAPAgencyCheckBatch getSAPAgencyCheckPrinting(AgencyCheckBatch agencyCheckBatch) {
        SAPAgencyCheckBatch sapBatch = new SAPAgencyCheckBatch();
        sapBatch.setLegalName(agencyCheckBatch.getPaymentTemplate().getPaymentTemplateCd());
        sapBatch.setTemplateNameLine1(agencyCheckBatch.getPaymentTemplate().getPaymentTemplateCd());
        sapBatch.setTemplateNameLine2(agencyCheckBatch.getPaymentTemplate().getAgency().getName());
        sapBatch.setPaycheckCount(agencyCheckBatch.getNumberOfChecks());
        sapBatch.setSentToPrinterDate(SpcfUtils.convertSpcfCalendarToDate(agencyCheckBatch.getSentToPrinter()));
        sapBatch.setPrintStatus(agencyCheckBatch.getCheckPrintBatchStatusCode().toString());
        sapBatch.setPrintBatchId(agencyCheckBatch.getId().toString());
        sapBatch.setIsSuperCheck(agencyCheckBatch.getSuperCheck());
        if (!agencyCheckBatch.getPaymentBatchAssocCollection().isEmpty()) {
            PaymentBatchAssoc paymentBatchAssoc = agencyCheckBatch.getPaymentBatchAssocCollection().get(0);
            PSPRequestContextManager pspRequestContextManager = PSPRequestContextManagerHelper.getPSPRequestContextManager();
            try {
                pspRequestContextManager.setRequestContextCompany(paymentBatchAssoc.getCompany());
                sapBatch.setInitiationDate(SpcfUtils.convertSpcfCalendarToDate(paymentBatchAssoc.getMoneyMovementTransaction().getInitiationDate()));
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }
        return sapBatch;
    }

    /*
     [0] Bank Account Seq
     [1] Source System Cd
     [2] Source Company Id
     [3] Legal Name
     [4] Account Type
     [5] Account Owner Name
     [6] Account Status
     */
    public static SAPBankAccountSearchResult getSAPBankAccountSearchResultFromDomainEntities(Object[] pObjects) {
        SAPBankAccountSearchResult sapBankAccountSearchResult = new SAPBankAccountSearchResult();

        sapBankAccountSearchResult.setAccountOwnerName((String) pObjects[5]);
        sapBankAccountSearchResult.setAccountStatus((String) pObjects[6]);
        sapBankAccountSearchResult.setAccountType((String) pObjects[4]);
        sapBankAccountSearchResult.setCompanyKey(new SAPCompanyKey((String) pObjects[1], (String) pObjects[2]));
        sapBankAccountSearchResult.setCompanyLegalName((String) pObjects[3]);

        return sapBankAccountSearchResult;
    }

    public static SAPEntitlementSearchResult getSAPEntitlementSearchResultFromDomainEntity(EntitlementUnit entitlementUnit) {
        SAPEntitlementSearchResult searchResult = new SAPEntitlementSearchResult();

        searchResult.setId(entitlementUnit.getId().toString());
        searchResult.setEntitlementStatus(entitlementUnit.getEntitlement().getEntitlementState().name());

        searchResult.setLicenseNumber(entitlementUnit.getEntitlement().getLicenseNumber());
        searchResult.setEoc(entitlementUnit.getEntitlement().getEntitlementOfferingCode());

        Company company = entitlementUnit.getCompany();
        searchResult.setFein(entitlementUnit.getFedTaxId());
        searchResult.setKey(getCompanyKey(company));
        searchResult.setPSID(company.getSourceCompanyId());
        searchResult.setLegalName(company.getLegalName());

        searchResult.setEntitlementUnitStatus(entitlementUnit.getEntitlementUnitStatus().toString());
        searchResult.setServiceKey(entitlementUnit.getFullServiceKey());

        searchResult.setAssetInfo(getSAPAssetInfoFromDomainEntity(entitlementUnit.getEntitlement().getEntitlementCode()));

        searchResult.setCompanyServiceStateCd(getSapCompanyServiceState(company));
        searchResult.setSubtypeDescription(entitlementUnit.getEntitlement().getEntitlementCode().getSubtypeDescription());

        return searchResult;
    }

    public static SAPAssetInfo getSAPAssetInfoFromDomainEntity(EntitlementCode code) {
        SAPAssetInfo info = new SAPAssetInfo();
        info.setAssisted(code.isAssisted());
        info.setPrimary(code.getIsPrimary());
        info.setAssetCode(code.getAssetItemCd().toString());
        if (code.isDiamondAssisted()) {
            info.setAssistedSubType("Diamond");
        }
        return info;
    }

    public static SAPEntitlementSearchResult getSAPEntitlementSearchResultFromCompany(Company company) {
        SAPEntitlementSearchResult searchResult = new SAPEntitlementSearchResult();

        searchResult.setFein(company.getFedTaxId());
        searchResult.setKey(getCompanyKey(company));
        searchResult.setPSID(company.getSourceCompanyId());
        searchResult.setLegalName(company.getLegalName());

        searchResult.setCompanyServiceStateCd(getSapCompanyServiceState(company));

        return searchResult;
    }

    public static SAPEntitlementInfo getSAPEntitlementInfoFromDomainEntity(Entitlement pEntitlement) {
        SAPEntitlementInfo entitlementInfo = new SAPEntitlementInfo();

        entitlementInfo.setId(pEntitlement.getId().toString());
        entitlementInfo.setLicenseNumber(pEntitlement.getLicenseNumber());
        entitlementInfo.setEoc(pEntitlement.getEntitlementOfferingCode());
        entitlementInfo.setOrderNumber(pEntitlement.getOrderNumber());
        entitlementInfo.setCustomerId(pEntitlement.getCustomerId());
        entitlementInfo.setContactEmail(pEntitlement.getContactEmail());
        entitlementInfo.setNextChargeDate(SAPTranslator.getDateFromSpcfCalendarNoToLocalNoTime(pEntitlement.getNextChargeDate()));
        entitlementInfo.setOrderSourceCode(ObjectUtils.toString(pEntitlement.getOrderSourceCd()));
        entitlementInfo.setSubscriptionNumber(ObjectUtils.toString(pEntitlement.getSubscriptionNumber()));
        entitlementInfo.setStatus(pEntitlement.getEntitlementState().toString());
        entitlementInfo.setBillingZipCode(pEntitlement.getBillingZipCode());
        entitlementInfo.setSubscriptionStartDate(SAPTranslator.getDateFromSpcfCalendar(pEntitlement.getSubscriptionStartDate()));
        entitlementInfo.setSubscriptionEndDate(SAPTranslator.getDateFromSpcfCalendar(pEntitlement.getSubscriptionEndDate()));
        entitlementInfo.setRetail(pEntitlement.getRetail());
        entitlementInfo.setEntitlementCodeInfo(getSAPEntitlementCodeInfoFromDomainEntity(pEntitlement.getEntitlementCode()));

        // Duplicate field, added to capture asset item number if EntitlementCodeInfo is not present
        entitlementInfo.setAssetItemNumber(pEntitlement.getEntitlementCode().getAssetItemNumber());

        return entitlementInfo;
    }

    public static SAPEntitlementUnit getSAPEntitlementUnitFromDomainEntity(EntitlementUnit pEntitlementUnit) {
        SAPEntitlementUnit entitlementUnit = new SAPEntitlementUnit();
        entitlementUnit.setId(pEntitlementUnit.getId().toString());
        entitlementUnit.setServiceKey(pEntitlementUnit.getServiceKey());
        entitlementUnit.setExtensionKey(pEntitlementUnit.getExtensionKey());
        entitlementUnit.setLastValidationDate(SAPTranslator.getDateFromSpcfCalendar(pEntitlementUnit.getLastValidationDate()));
        entitlementUnit.setStatus(pEntitlementUnit.getEntitlementUnitStatus().name());
        entitlementUnit.setEntitlement(getSAPEntitlementInfoFromDomainEntity(pEntitlementUnit.getEntitlement()));
        return entitlementUnit;
    }

    public static SAPEntitlementInfo getSAPEntitlementInfoFromPendingMessage(DomainEntitySet<EntitlementMessage> messages) throws Throwable {
        SAPEntitlementInfo entitlementInfo = new SAPEntitlementInfo();


        String licenseNumber = messages.get(0).getLicenseNumber();
        String eoc = messages.get(0).getEntitlementOfferingCode();
        String orderNumber = messages.get(0).getOrderNumber();

        //set basic info on return val
        entitlementInfo.setLicenseNumber(licenseNumber);
        entitlementInfo.setEoc(eoc);
        entitlementInfo.setOrderNumber(orderNumber);

        //build initial DTO
        EntitlementDTO entitlementDTO = new EntitlementDTO();
        entitlementDTO.setLicenseNumber(licenseNumber);
        entitlementDTO.setEntitlementOfferingCode(eoc);
        entitlementDTO.setOrderNumber(orderNumber);

        //process every message, adding onto the information
        for (EntitlementMessage message : messages) {

            SyncCustomerAssetDataAreaType syncCustomerAssetDataAreaType = (SyncCustomerAssetDataAreaType) ProcessSavedAMOMessages.createUnmarshaller().unmarshal(
                    new InputSource(new StringReader(message.getMessage())));

            AMOMessageProcessing.processAssetUpdates(entitlementDTO, new SyncCustomerAssetDataAreaTypeDTO(syncCustomerAssetDataAreaType));

        }

        boolean isRetail = false;
        if (entitlementDTO.getSubscriptionStartDate() != null ) {
            // assume today as entitlement created date as entitlement is not in DB yet
            SpcfCalendar createdDate = PSPDate.getPSPTime();
            isRetail = entitlementDTO.getSubscriptionStartDate().getYear() > createdDate.getYear() &&
                    entitlementDTO.getSubscriptionStartDate().getDayOfYear() >= createdDate.getDayOfYear();
        }
        // Change the value only if the retail flag was not set earlier.
        // DO not update the retail flag if the message does not contain the subscription start date,
        // When it was already set to true by a earlier message
        isRetail = entitlementDTO.isRetail() ? true: isRetail;
        entitlementDTO.setRetail(isRetail);

        //read the final result
        entitlementInfo.setCustomerId(entitlementDTO.getCustomerId());
        entitlementInfo.setContactEmail(entitlementDTO.getContactEmail());
        entitlementInfo.setNextChargeDate(SAPTranslator.getDateFromSpcfCalendar(entitlementDTO.getNextChargeDate()));

        EntitlementCode entitlementCode = EntitlementCode.findEntitlementCode(entitlementDTO.getAssetItemNumber(),
                entitlementDTO.getEditionType(),
                entitlementDTO.getNumberOfEmployeesType(),
                AssetTypeCode.Payroll);

        entitlementInfo.setEntitlementCodeInfo(getSAPEntitlementCodeInfoFromDomainEntity(entitlementCode));


        // Duplicate field, added to capture asset item number if EntitlementCodeInfo is not present
        entitlementInfo.setAssetItemNumber(entitlementDTO.getAssetItemNumber());

        return entitlementInfo;
    }

    public static SAPEntitlementCodeInfo getSAPEntitlementCodeInfoFromDomainEntity(EntitlementCode pEntitlementCode) {
        SAPEntitlementCodeInfo entitlementCodeInfo = new SAPEntitlementCodeInfo();
        entitlementCodeInfo.setAssetItemCode(pEntitlementCode.getAssetItemCd().toString());
        entitlementCodeInfo.setAssetItemNumber(pEntitlementCode.getAssetItemNumber());
        entitlementCodeInfo.setEdition(pEntitlementCode.getEditionType() != null ? pEntitlementCode.getEditionType().toString() : null);
        entitlementCodeInfo.setNumberOfEmployees(pEntitlementCode.getNumberOfEmployeesType() != null ? pEntitlementCode.getNumberOfEmployeesType().toString() : null);
        entitlementCodeInfo.setQuickBooksSubtype(Long.toString(pEntitlementCode.getQuickBooksSubtype()));
        entitlementCodeInfo.setSubtypeDescription(pEntitlementCode.getSubtypeDescription());
        return entitlementCodeInfo;
    }


    public static ArrayList<SAPContact> getSAPContactsFromMessages(DomainEntitySet<EntitlementMessage> messages) throws Throwable {

        Map<String, CustomerAccountContactType> contactMap = new HashMap<String, CustomerAccountContactType>();

        for (EntitlementMessage message : messages) {

            SyncCustomerAssetDataAreaType syncCustomerAssetDataAreaType = (SyncCustomerAssetDataAreaType) ProcessSavedAMOMessages.createUnmarshaller().unmarshal(
                    new InputSource(new StringReader(message.getMessage())));

            AMOMessageProcessing.addAllContacts(contactMap, syncCustomerAssetDataAreaType);
        }

        ArrayList<SAPContact> sapContacts = new ArrayList<SAPContact>();
        for (CustomerAccountContactType contact : contactMap.values()) {
            sapContacts.add(getSAPContactFromAMOContact(contact));
        }
        return sapContacts;
    }

    public static ArrayList<SAPAddress> getSAPAddressesFromMessages(DomainEntitySet<EntitlementMessage> messages) throws Throwable {

        Map<String, PostalAddressDescription> addressMap = new HashMap<String, PostalAddressDescription>();

        for (EntitlementMessage message : messages) {

            SyncCustomerAssetDataAreaType syncCustomerAssetDataAreaType = (SyncCustomerAssetDataAreaType) ProcessSavedAMOMessages.createUnmarshaller().unmarshal(
                    new InputSource(new StringReader(message.getMessage())));

            AMOMessageProcessing.addAllAddresses(addressMap, syncCustomerAssetDataAreaType);
        }

        ArrayList<SAPAddress> sapAddresses = new ArrayList<SAPAddress>();
        for (PostalAddressDescription address : addressMap.values()) {
            SAPAddress sapAddress = getSAPAddressFromAMOAddress(address.getAddress());
            sapAddress.setDescription(address.getDescription());
            sapAddresses.add(sapAddress);
        }
        return sapAddresses;
    }


    private static SAPContact getSAPContactFromAMOContact(CustomerAccountContactType contact) {
        SAPContact sapContact = new SAPContact();

        if (contact.getContactRole().size() > 0) {
            sapContact.setDescription("Siebel " + contact.getContactRole().get(0).getRoleName());
        } else {
            sapContact.setDescription("Siebel Contact");
        }

        PersonType person = contact.getContactPerson().getPerson();

        if (person.getAddress().size() > 0) {
            sapContact.setAddress(getSAPAddressFromAMOAddress(person.getAddress().get(0).getPostalAddress()));
        }

        if (person.getEmailAddress() != null) {
            sapContact.setEmail(person.getEmailAddress().getMainEmailAddress());
        }

        sapContact.setFirstName(person.getGivenName());
        sapContact.setLastName(person.getFamilyName());
        sapContact.setMiddleName(person.getMiddleName());

        if (person.getTelephone() != null) {
            sapContact.setFaxNumber(person.getTelephone().getFaxNumber());
            if (!StringUtils.isEmpty(person.getTelephone().getWorkNumber())) {
                sapContact.setPhoneNumber(person.getTelephone().getWorkNumber());
            } else if (!StringUtils.isEmpty(person.getTelephone().getMobileNumber())) {
                sapContact.setPhoneNumber(person.getTelephone().getMobileNumber());
            } else if (!StringUtils.isEmpty(person.getTelephone().getAlternateNumber())) {
                sapContact.setPhoneNumber(person.getTelephone().getAlternateNumber());
            } else if (!StringUtils.isEmpty(person.getTelephone().getHomeNumber())) {
                sapContact.setPhoneNumber(person.getTelephone().getHomeNumber());
            }
        }

        return sapContact;
    }

    private static SAPAddress getSAPAddressFromAMOAddress(PostalAddressType address) {
        SAPAddress sapAddress = new SAPAddress();

        if (address.getAddressLine().size() > 0) {
            sapAddress.setAddressLine1(address.getAddressLine().get(0));
        }
        if (address.getAddressLine().size() > 1) {
            sapAddress.setAddressLine2(address.getAddressLine().get(1));
        }
        if (address.getAddressLine().size() > 2) {
            sapAddress.setAddressLine3(address.getAddressLine().get(2));
        }

        sapAddress.setCity(address.getCity());
        sapAddress.setState(address.getStateOrProvince());

        Pattern p = Pattern.compile("(\\d{5})-?(\\d{0,4})");
        Matcher m = p.matcher(address.getPostalCode());
        if (m.find()) {
            sapAddress.setZipCode(m.group(1));
            if (m.groupCount() >= 2) {
                sapAddress.setZipCodeExtension(m.group(2));
            }
        }

        return sapAddress;
    }

    public static SAPCompanyKey getCompanyKey(Company company) {
        return new SAPCompanyKey(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
    }

    public static EwsContact getEWSContactFromSAPContact(SAPContact sapContact) {
        EwsContact ewsContact = new EwsContact();
        ewsContact.setAddress(getEWSAddressFromSAPAddress(EwsAddress.class, sapContact.getAddress()));
        ewsContact.seteMail(sapContact.getEmail());
        ewsContact.setFirstName(sapContact.getFirstName());
        ewsContact.setLastName(sapContact.getLastName());
        ewsContact.setMiddleName(sapContact.getMiddleName());
        ewsContact.setWorkPhone(SAPTranslator.sanitizePhoneNumber(sapContact.getPhoneNumber()));
        ewsContact.setJobTitle(sapContact.getJobTitle());
        ewsContact.setTitle(sapContact.getPrefix());
        ewsContact.setTitleSuffix(sapContact.getSuffix());
        return ewsContact;
    }

    public static <T extends EwsAddress> T getEWSAddressFromSAPAddress(Class<T> clazz, SAPAddress sapAddress) {
        if (StringUtils.isEmpty(sapAddress.getAddressLine1())
                && StringUtils.isEmpty(sapAddress.getAddressLine2())
                && StringUtils.isEmpty(sapAddress.getCity())
                && StringUtils.isEmpty(sapAddress.getState())
                && StringUtils.isEmpty(sapAddress.getZipCode())
                && StringUtils.isEmpty(sapAddress.getZipCodeExtension())
        ) {
            return null;
        }

        T ewsAddress = null;
        try {
            ewsAddress = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ewsAddress.setAddressLine1(sapAddress.getAddressLine1());
        ewsAddress.setAddressLine2(sapAddress.getAddressLine2());
        ewsAddress.setCity(sapAddress.getCity());
        ewsAddress.setState(sapAddress.getState());
        String zip = sapAddress.getZipCode();
        if (sapAddress.getZipCodeExtension() != null && !sapAddress.getZipCodeExtension().equals("")) {
            zip = zip + "-" + sapAddress.getZipCodeExtension();
        }
        ewsAddress.setZip(zip);

        return ewsAddress;
    }

    public static String getRoutingNumberPayCardDisplayText(BankAccount ba) {
        if (ba.isPayCardAccount()) {
            return ba.getRoutingNumber() + " (PayCard)";
        } else {
            return ba.getRoutingNumber();
        }
    }

    public static SAPOfferingServiceChargePrice getSAPOfferingServiceChargePrice(OfferingServiceChargeGroup group, SpcfMoney price) {
        SAPOfferingServiceChargePrice sapOfferingServiceChargePrice = new SAPOfferingServiceChargePrice();
        sapOfferingServiceChargePrice.setServiceChargeTypeCode(group.getAppliesTo().name());
        sapOfferingServiceChargePrice.setPrice(SAPTranslator.getDoubleFromSpcfMoney(price));
        switch (group.getAppliesTo()) {
            case DebitReturnFee:
                sapOfferingServiceChargePrice.setDisplayName("ER Return Fee");
                break;
            case ReversalFee:
                sapOfferingServiceChargePrice.setDisplayName("ER Reversal Fee");
                break;
            case PaymentArrangementFee:
                sapOfferingServiceChargePrice.setDisplayName("Intuit Assessment Fee");
                break;
            case Amendments:
                sapOfferingServiceChargePrice.setDisplayName("Amendment - Wage and Tax Fee");
                break;
            case AmendedSSN:
                sapOfferingServiceChargePrice.setDisplayName("Amendment - Employee Fee");
                break;
            default:
                sapOfferingServiceChargePrice.setDisplayName(group.getName());
        }
        return sapOfferingServiceChargePrice;
    }
}
