package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.*;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.SAPTranslator;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

import java.util.*;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
public class PSPToDISTransformer {

    /***
     *
     * @param pSAPCompanyStatus
     * @return
     * @throws Exception
     */
    public static SAPCompanyStatusDISDTO createSAPCompanyStatus(SAPCompanyStatus pSAPCompanyStatus) throws Exception {
        SAPCompanyStatusDISDTO sapCompanyStatusDISDTO = new SAPCompanyStatusDISDTO();
        sapCompanyStatusDISDTO.setAvailableServices(pSAPCompanyStatus.getAvailableServices());
        sapCompanyStatusDISDTO.setFlaggedForFraud(pSAPCompanyStatus.isFlaggedForFraud());
        if (pSAPCompanyStatus.getServiceStatusCollection() != null) {
            sapCompanyStatusDISDTO.setServiceStatusCollection(createSAPCompanyServiceStatusDISDTOList(pSAPCompanyStatus.getServiceStatusCollection()));
        }
        return sapCompanyStatusDISDTO;
    }

    /***
     *
     * @param pSAPCompanyServiceStatus
     * @return
     * @throws Exception
     */
    public static ArrayList<SAPCompanyServiceStatusDISDTO> createSAPCompanyServiceStatusDISDTOList(ArrayList<SAPCompanyServiceStatus> pSAPCompanyServiceStatus) throws Exception {
        ArrayList<SAPCompanyServiceStatusDISDTO> sapCompanyServiceStatusDISDTOList = new ArrayList<SAPCompanyServiceStatusDISDTO>();
        for (SAPCompanyServiceStatus sapCompanyServiceStatus : pSAPCompanyServiceStatus) {
            sapCompanyServiceStatusDISDTOList.add(createSAPCompanyServiceStatusDISDTO(sapCompanyServiceStatus));
        }
        return sapCompanyServiceStatusDISDTOList;
    }

    /***
     *
     * @param pSAPCompanyServiceStatus
     * @return
     * @throws Exception
     */
    public static SAPCompanyServiceStatusDISDTO createSAPCompanyServiceStatusDISDTO(SAPCompanyServiceStatus pSAPCompanyServiceStatus) throws Exception {
        SAPCompanyServiceStatusDISDTO sapCompanyServiceStatusDISDTO = new SAPCompanyServiceStatusDISDTO();
        if (pSAPCompanyServiceStatus.getAllowedTransitions() != null) {
            sapCompanyServiceStatusDISDTO.setAllowedTransitions(createSAPServiceStatusDISDTOList(pSAPCompanyServiceStatus.getAllowedTransitions()));
        }
        if (pSAPCompanyServiceStatus.getBillPaymentAdditionalInfo() != null) {
            sapCompanyServiceStatusDISDTO.setBillPaymentAdditionalInfo(createBillPaymentAdditionalInfoDISDTO(pSAPCompanyServiceStatus.getBillPaymentAdditionalInfo()));
        }
        sapCompanyServiceStatusDISDTO.setCanEditOffer(pSAPCompanyServiceStatus.getCanEditOffer());
        sapCompanyServiceStatusDISDTO.setCanEditOffering(pSAPCompanyServiceStatus.getCanEditOffer());
        sapCompanyServiceStatusDISDTO.setCanUpdateStatus(pSAPCompanyServiceStatus.getCanUpdateStatus());
        sapCompanyServiceStatusDISDTO.setCustodialId(pSAPCompanyServiceStatus.getCustodialId());
        if (pSAPCompanyServiceStatus.getDdLimits() != null) {
            sapCompanyServiceStatusDISDTO.setDdLimits(createSAPCompanyDdLimitsDISDTO(pSAPCompanyServiceStatus.getDdLimits()));
        }
        if (pSAPCompanyServiceStatus.getDirectDepositAdditionalInfo() != null) {
            sapCompanyServiceStatusDISDTO.setDirectDepositAdditionalInfo(createSAPDirectDepositServiceInformationDISDTO(pSAPCompanyServiceStatus.getDirectDepositAdditionalInfo()));
        }
        if (pSAPCompanyServiceStatus.getDisplayStatus() != null) {
            sapCompanyServiceStatusDISDTO.setDisplayStatus(createSAPDisplayStatusDISDTO(pSAPCompanyServiceStatus.getDisplayStatus()));
        }
        sapCompanyServiceStatusDISDTO.setFundingModelCd(pSAPCompanyServiceStatus.getFundingModelCd());
        sapCompanyServiceStatusDISDTO.setHasSignatureFile(pSAPCompanyServiceStatus.getHasSignatureFile());
        sapCompanyServiceStatusDISDTO.setOffer(pSAPCompanyServiceStatus.getOffer());
        sapCompanyServiceStatusDISDTO.setOffering(pSAPCompanyServiceStatus.getOffering());
        sapCompanyServiceStatusDISDTO.setSafeHarbor(pSAPCompanyServiceStatus.getIsSafeHarbor());
        sapCompanyServiceStatusDISDTO.setServiceCd(pSAPCompanyServiceStatus.getServiceCd());
        sapCompanyServiceStatusDISDTO.setServiceStartDate(pSAPCompanyServiceStatus.getServiceStartDate());
        if (pSAPCompanyServiceStatus.getStatus() != null) {
            sapCompanyServiceStatusDISDTO.setStatus(createSAPServiceStatusDISDTO(pSAPCompanyServiceStatus.getStatus()));
        }
        return sapCompanyServiceStatusDISDTO;
    }

    /***
     *
     * @param pDisplayStatus
     * @return
     */
    private static SAPDisplayStatusDISDTO createSAPDisplayStatusDISDTO(SAPDisplayStatus pDisplayStatus) {
        SAPDisplayStatusDISDTO sapDisplayStatus = new SAPDisplayStatusDISDTO();
        sapDisplayStatus.setDisplayDetails(pDisplayStatus.getDisplayDetails());
        sapDisplayStatus.setDisplayStatus(pDisplayStatus.getDisplayStatus());
        sapDisplayStatus.setDisplaySubStatus(pDisplayStatus.getDisplaySubStatus());
        return sapDisplayStatus;
    }

    /***
     *
     * @param pDirectDepositAdditionalInfo
     * @return
     */
    private static SAPDirectDepositServiceInformationDISDTO createSAPDirectDepositServiceInformationDISDTO(SAPDirectDepositServiceInformation pDirectDepositAdditionalInfo) {
        SAPDirectDepositServiceInformationDISDTO sapDirectDepositServiceInformationDISDTO = new SAPDirectDepositServiceInformationDISDTO();
        sapDirectDepositServiceInformationDISDTO.setConsecutiveLimitVoilationCount(pDirectDepositAdditionalInfo.getConsecutiveLimitVoilationCount());
        sapDirectDepositServiceInformationDISDTO.setTotalLimitVoilationCount(pDirectDepositAdditionalInfo.getTotalLimitVoilationCount());
        return sapDirectDepositServiceInformationDISDTO;
    }

    /***
     *
     * @param pDDLimits
     * @return
     */
    private static SAPCompanyDdLimitsDISDTO createSAPCompanyDdLimitsDISDTO(SAPCompanyDdLimits pDDLimits) {
        SAPCompanyDdLimitsDISDTO sapCompanyDdLimitsDISDTO = new SAPCompanyDdLimitsDISDTO();
        sapCompanyDdLimitsDISDTO.setPerEmployeeLimit(pDDLimits.getPerEmployeeLimit());
        sapCompanyDdLimitsDISDTO.setPerPayrollLimit(pDDLimits.getPerPayrollLimit());
        return sapCompanyDdLimitsDISDTO;
    }

    /***
     *
     * @param pBillPaymentAdditionalInfo
     * @return
     */
    private static SAPBillPaymentServiceInformationDISDTO createBillPaymentAdditionalInfoDISDTO(SAPBillPaymentServiceInformation pBillPaymentAdditionalInfo) {
        SAPBillPaymentServiceInformationDISDTO sapBillPaymentServiceInformationDISDTO = new SAPBillPaymentServiceInformationDISDTO();
        sapBillPaymentServiceInformationDISDTO.setConsecutiveLimitVoilationCount(pBillPaymentAdditionalInfo.getConsecutiveLimitVoilationCount());
        sapBillPaymentServiceInformationDISDTO.setTotalLimitVoilationCount(pBillPaymentAdditionalInfo.getTotalLimitVoilationCount());
        return sapBillPaymentServiceInformationDISDTO;
    }

    /***
     *
     * @param allowedTransitions
     * @return
     */
    private static ArrayList<SAPServiceStatusDISDTO> createSAPServiceStatusDISDTOList(ArrayList<SAPServiceStatus> allowedTransitions) {
        ArrayList<SAPServiceStatusDISDTO> sapServiceStatusDISDTOList = new ArrayList<SAPServiceStatusDISDTO>();
        for (SAPServiceStatus sapServiceStatus : allowedTransitions) {
            sapServiceStatusDISDTOList.add(createSAPServiceStatusDISDTO(sapServiceStatus));
        }
        return sapServiceStatusDISDTOList;
    }

    /***
     *
     * @param pSAPServiceStatus
     * @return
     */
    private static SAPServiceStatusDISDTO createSAPServiceStatusDISDTO(SAPServiceStatus pSAPServiceStatus) {
        SAPServiceStatusDISDTO sapServiceStatusDISDTO = new SAPServiceStatusDISDTO();
        sapServiceStatusDISDTO.setServiceStatusCd(pSAPServiceStatus.getServiceStatusCd());
        sapServiceStatusDISDTO.setServiceStatusDescription(pSAPServiceStatus.getServiceStatusDescription());
        sapServiceStatusDISDTO.setServiceStatusName(pSAPServiceStatus.getServiceStatusName());
        if (pSAPServiceStatus.getServiceSubStatusList() != null) {
            sapServiceStatusDISDTO.setServiceSubStatusList(createSAPServiceSubStatusList(pSAPServiceStatus.getServiceSubStatusList()));
        }
        return sapServiceStatusDISDTO;
    }

    /***
     *
     * @param pServiceSubStatusList
     * @return
     */
    private static ArrayList<SAPServiceSubStatusDISDTO> createSAPServiceSubStatusList(ArrayList<SAPServiceSubStatus> pServiceSubStatusList) {
        ArrayList<SAPServiceSubStatusDISDTO> sapServiceSubStatusDISDTOList = new ArrayList<SAPServiceSubStatusDISDTO>();
        for (SAPServiceSubStatus sapServiceSubStatus : pServiceSubStatusList) {
            sapServiceSubStatusDISDTOList.add(createSAPServiceSubStatusDISDTO(sapServiceSubStatus));
        }
        return sapServiceSubStatusDISDTOList;
    }

    /***
     *
     * @param pSAPServiceSubStatus
     * @return
     */
    private static SAPServiceSubStatusDISDTO createSAPServiceSubStatusDISDTO(SAPServiceSubStatus pSAPServiceSubStatus) {
        SAPServiceSubStatusDISDTO sapServiceSubStatusDISDTO = new SAPServiceSubStatusDISDTO();
        sapServiceSubStatusDISDTO.setManuallyUpdatable(pSAPServiceSubStatus.isManuallyUpdatable());
        sapServiceSubStatusDISDTO.setSubStatusCd(pSAPServiceSubStatus.getSubStatusCd());
        sapServiceSubStatusDISDTO.setSubStatusDescription(pSAPServiceSubStatus.getSubStatusDescription());
        sapServiceSubStatusDISDTO.setSubStatusName(pSAPServiceSubStatus.getSubStatusName());
        sapServiceSubStatusDISDTO.setSubStatusType(pSAPServiceSubStatus.getSubStatusType());
        return sapServiceSubStatusDISDTO;
    }


    /**
     * @param pSAPCompany
     * @throws Exception
     */
    public static SAPCompanyDISDTO createCompanySAPDISDTO(SAPCompany pSAPCompany) throws Exception {
        SAPCompanyDISDTO rtnCo = new SAPCompanyDISDTO();
        rtnCo.setAssisted(pSAPCompany.getIsAssisted());
        rtnCo.setCompanyId(pSAPCompany.getCompanyId());
        rtnCo.setDBA(pSAPCompany.getDBA());
        rtnCo.setDebugLogging(false); //todo this can't be useful to you...
        rtnCo.setDoesPSPMoveMoneyFor(true); //todo true for DIS companies, but probably not used
        rtnCo.setDoesPSPProvideCustomerService(true); //todo true for DIS companies, but probably not used
        rtnCo.setEditable(pSAPCompany.getIsEditable());
        rtnCo.setFein(pSAPCompany.getFein());
        rtnCo.setGseq(pSAPCompany.getGseq());
        rtnCo.setHasChecklist(false); //todo this is an old gemini value
        rtnCo.setLegalName(pSAPCompany.getLegalName());
//        rtnCo.setMigrationStatus(pSAPCompany.getMigrationStatus());
        rtnCo.setNotificationEmail(pSAPCompany.getNotificationEmail());
        rtnCo.setOffloadGrp(null); //todo is this needed?
        rtnCo.setPSID(pSAPCompany.getCompanyId());
        rtnCo.setRealmId(pSAPCompany.getIamRealmId());
        rtnCo.setSourceSystemCd(SourceSystemEnum.valueOf(pSAPCompany.getSourceSystemCd()));

        //@TODO I asked David why tax exempt was pulled from SAP Company post 10.1 and here was response:
        //        Weinberg, David [10:31 AM]:
        //        SAP uses it, but primarily it is used for just setting these values
        //        Weinberg, David [10:32 AM]:
        //        the adapter doesn't care too much about whather or not a company is exempt or not at this moment
        // Need to investigate if we need this field in Case 360.  Getting values from Company for now.
        Company pspCo = Company.findCompany(pSAPCompany.getCompanyId(),SourceSystemCode.valueOf(pSAPCompany.getSourceSystemCd()));
        if (pspCo != null) {
            rtnCo.setTaxExempt(pspCo.isTaxExempt());
            if (pspCo.getTaxExemptExpirationDate() != null) {
                Calendar expirationDate = CalendarUtils.convertToCalendar(pspCo.getTaxExemptExpirationDate());
                rtnCo.setTaxExemptExpirationDate(expirationDate.getTime());
            }
        }

        if (pSAPCompany.getTaxService() != null) {
            rtnCo.setTaxService(createSAPTaxCompanyServiceInfoDISDTO(pSAPCompany.getTaxService()));
        }

        return rtnCo;
    }

    /***
     *
     * @param pQuickbooksInfo
     * @return
     */
    public static SAPQuickbooksInfoDISDTO createSAPQuickbooksInfoDISDTO(SAPQuickbooksInfo pQuickbooksInfo) {
        SAPQuickbooksInfoDISDTO rtnSAPQuickbooksInfoDISDTO = new SAPQuickbooksInfoDISDTO();
        rtnSAPQuickbooksInfoDISDTO.setApplicationVersion(pQuickbooksInfo.getApplicationVersion());
        rtnSAPQuickbooksInfoDISDTO.setCoaFeeAccountName(pQuickbooksInfo.getCoaFeeAccountName());
        rtnSAPQuickbooksInfoDISDTO.setCoaSalesTaxAccountName(pQuickbooksInfo.getCoaSalesTaxAccountName());
        rtnSAPQuickbooksInfoDISDTO.setLicenseNumber(pQuickbooksInfo.getLicenseNumber());
        rtnSAPQuickbooksInfoDISDTO.setTaxTable(pQuickbooksInfo.getTaxTable());
        return rtnSAPQuickbooksInfoDISDTO;
    }

    /***
     *
     * @param pAgreementInfo
     * @return
     */
    public static SAPAgreementInfoDISDTO createSAPAgreementInfoDISDTO(SAPAgreementInfo pAgreementInfo) {
        SAPAgreementInfoDISDTO rtnSAPAgreementInfoDISDTO = new SAPAgreementInfoDISDTO();
        rtnSAPAgreementInfoDISDTO.setAgreementSubType(pAgreementInfo.getAgreementSubType());
        rtnSAPAgreementInfoDISDTO.setName(pAgreementInfo.getName());
        rtnSAPAgreementInfoDISDTO.setServiceKey(pAgreementInfo.getServiceKey());
        rtnSAPAgreementInfoDISDTO.setServiceType(pAgreementInfo.getServiceType());
        rtnSAPAgreementInfoDISDTO.setSubscriptionNumber(pAgreementInfo.getSubscriptionNumber());
        return rtnSAPAgreementInfoDISDTO;
    }

    /***
     *
     * @param pActiveBankAccount
     * @return
     */
    public static SAPCompanyBankAccountDISDTO createSAPCompanyBankAccountDISDTO(SAPCompanyBankAccount pActiveBankAccount) {
        SAPCompanyBankAccountDISDTO rtnSAPCompanyBankAccountDISDTO = new SAPCompanyBankAccountDISDTO();
        rtnSAPCompanyBankAccountDISDTO.setAccountNumber(pActiveBankAccount.getAccountNumber());
        rtnSAPCompanyBankAccountDISDTO.setBankAccountStatus(pActiveBankAccount.getBankAccountStatusCd());
        rtnSAPCompanyBankAccountDISDTO.setBankAccountType(pActiveBankAccount.getAccountType());
        rtnSAPCompanyBankAccountDISDTO.setBankName(pActiveBankAccount.getBankName());
        rtnSAPCompanyBankAccountDISDTO.setRoutingNumber(pActiveBankAccount.getRoutingNumber());
        rtnSAPCompanyBankAccountDISDTO.setSourceBankAccountId(pActiveBankAccount.getSourceBankAccountId());
        rtnSAPCompanyBankAccountDISDTO.setSourceBankAccountName(pActiveBankAccount.getSourceBankAccountName());
        return rtnSAPCompanyBankAccountDISDTO;

    }

    /***
     *
     * @param pTaxService
     * @return
     */
    private static SAPTaxCompanyServiceInfoDISDTO createSAPTaxCompanyServiceInfoDISDTO(SAPTaxCompanyServiceInfo pTaxService) {
        SAPTaxCompanyServiceInfoDISDTO rtnSAPTaxCompanyServiceInfoDISDTO = new SAPTaxCompanyServiceInfoDISDTO();
        rtnSAPTaxCompanyServiceInfoDISDTO.setServiceStatusCd(pTaxService.getServiceStatusCd());
        rtnSAPTaxCompanyServiceInfoDISDTO.setServiceSubStatusCd(pTaxService.getServiceSubStatusCd());
        return rtnSAPTaxCompanyServiceInfoDISDTO;
    }

    /***
     *
     * @param contacts
     * @return
     */
    public static ArrayList<SAPContactDISDTO> createSAPContact(List<SAPContact> contacts) {
        ArrayList<SAPContactDISDTO> rtnContactList = new ArrayList<SAPContactDISDTO>();
        for (SAPContact sapContact : contacts) {
            rtnContactList.add(createSAPContactDISDTO(sapContact));
        }
        return rtnContactList;
    }

    /***
     *
     * @param sapContact
     * @return
     */
    private static SAPContactDISDTO createSAPContactDISDTO(SAPContact sapContact) {
        SAPContactDISDTO rtnSAPContactDISDTO = new SAPContactDISDTO();
        rtnSAPContactDISDTO.setAccountSignatory(sapContact.getAccountSignatory());
        if (sapContact.getAddress() != null) {
            rtnSAPContactDISDTO.setAddress(createSAPAddressDISDTO(sapContact.getAddress()));
        }
        rtnSAPContactDISDTO.setCommunicationTypeCd(sapContact.getCommunicationTypeCd());
        rtnSAPContactDISDTO.setContactId(sapContact.getContactId());
        rtnSAPContactDISDTO.setContactRoleCd(sapContact.getContactRoleCd());
        rtnSAPContactDISDTO.setEmail(sapContact.getEmail());
        rtnSAPContactDISDTO.setFaxNumber(sapContact.getFaxNumber());
        rtnSAPContactDISDTO.setFirstName(sapContact.getFirstName());
        rtnSAPContactDISDTO.setJobTitle(sapContact.getJobTitle());
        rtnSAPContactDISDTO.setLastName(sapContact.getLastName());
        rtnSAPContactDISDTO.setMiddleName(sapContact.getMiddleName());
        rtnSAPContactDISDTO.setPhoneNumber(SAPTranslator.sanitizePhoneNumber(sapContact.getPhoneNumber()));
        rtnSAPContactDISDTO.setPrefix(sapContact.getPrefix());
        rtnSAPContactDISDTO.setSuffix(sapContact.getSuffix());
        return rtnSAPContactDISDTO;
    }

    /***
     *
     * @param pSAPAddress
     * @return
     */
    public static SAPAddressDISDTO createSAPAddressDISDTO(SAPAddress pSAPAddress) {
        SAPAddressDISDTO sapAddressDISDTO = new SAPAddressDISDTO();
        sapAddressDISDTO.setAddressLine1(pSAPAddress.getAddressLine1());
        sapAddressDISDTO.setAddressLine2(pSAPAddress.getAddressLine2());
        sapAddressDISDTO.setAddressLine3(pSAPAddress.getAddressLine3());
        sapAddressDISDTO.setCity(pSAPAddress.getCity());
        sapAddressDISDTO.setCountry(pSAPAddress.getCountry());
        sapAddressDISDTO.setState(pSAPAddress.getState());
        sapAddressDISDTO.setZip(pSAPAddress.getZipCode());
        sapAddressDISDTO.setZipCodeExtension(pSAPAddress.getZipCodeExtension());
        return sapAddressDISDTO;
    }

    /**
     * Create the WS response DTO with the data in the PSP Address
     *
     * @param pCompanyEvent - PSP Address to extract from
     * @return - AddressDISDTO WS DTO to be returned
     */
    public static CompanyEventDISDTO createCompanyEventDISDTO(CompanyEvent pCompanyEvent) {
        CompanyEventDISDTO rtnCompanyEvent = new CompanyEventDISDTO();
        rtnCompanyEvent.setCreatorId(pCompanyEvent.getCreatorId());
        rtnCompanyEvent.setEventTypeCode(pCompanyEvent.getEventTypeCd().toString());
        if (pCompanyEvent.getEventTimeStamp() != null) {
            rtnCompanyEvent.setEventTimeStamp(new Date(pCompanyEvent.getEventTimeStamp().getTimeInMilliseconds()));
        }
        rtnCompanyEvent.setStatusCd(pCompanyEvent.getStatusCd());
        if (pCompanyEvent.getStatusEffectiveDate() != null) {
            rtnCompanyEvent.setStatusEffectiveDate(new Date(pCompanyEvent.getStatusEffectiveDate().getTimeInMilliseconds()));
        }
        EventType eventType = PayrollServices.entityFinder.findById(EventType.class, pCompanyEvent.getEventTypeCd());
        rtnCompanyEvent.setName(eventType.getName());
        HashMap<EventDetailTypeCode, String> pspEventDetails = pCompanyEvent.getEventDetailInfo();
        ArrayList<CompanyEventDetailDISDTO> eventDetails = new ArrayList<CompanyEventDetailDISDTO>() ;
        for (EventDetailTypeCode pspEventDetailTypeCode : pspEventDetails.keySet()) {
            CompanyEventDetailDISDTO eventDetailDISDTO = new CompanyEventDetailDISDTO();
            eventDetailDISDTO.setEventDetailTypeCode(pspEventDetailTypeCode.toString());
            eventDetailDISDTO.setEventDetailValue(pspEventDetails.get(pspEventDetailTypeCode));
            eventDetails.add(eventDetailDISDTO);
        }
        rtnCompanyEvent.setEventDetails(eventDetails);
        return rtnCompanyEvent;
    }

}
