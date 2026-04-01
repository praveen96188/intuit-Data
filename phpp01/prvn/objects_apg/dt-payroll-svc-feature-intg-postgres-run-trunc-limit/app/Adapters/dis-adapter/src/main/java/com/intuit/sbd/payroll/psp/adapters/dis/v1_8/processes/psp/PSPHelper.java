package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessage;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.SAPCompanyDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.SAPCompanyStatusDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.SearchSAPCompanyRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.sap.SAPAuthHelper;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyTranslator;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
public class PSPHelper {

    private static final SpcfLogger logger = PayrollServices.getLogger(PSPHelper.class);;

    /**
     * @param pEIN
     * @param pSourceSystemCode
     * @return
     */
    public static DomainEntitySet<Company> searchCompaniesByEIN(String pEIN, SourceSystemCode pSourceSystemCode) {

        Criterion<Company> companyCriterion = Company.SourceSystemCd().equalTo(pSourceSystemCode);
        if(pEIN == null){
            companyCriterion = companyCriterion.And(Company.FedTaxIdEnc().isNull());
        } else {
            List<String> einEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pEIN);
            companyCriterion = companyCriterion.And(Company.FedTaxIdEnc().in(einEncList));
        }
        Expression<Company> query =
                new Query<Company>()
                        .Where(companyCriterion);

        DomainEntitySet<Company> companies = Application.find(Company.class, query);
        return companies;
    }

    /**
     * @param pSourceCompanyId
     * @param pSourceSystemCode
     * @return
     */
    public static DomainEntitySet<Company> searchCompaniesBySourceCompanyId(String pSourceCompanyId, SourceSystemCode pSourceSystemCode) {
        Expression<Company> query =
                new Query<Company>()
                        .Where(Company.SourceSystemCd().equalTo(pSourceSystemCode)
                                .And(Company.SourceCompanyId().equalTo(pSourceCompanyId))
                        );

        DomainEntitySet<Company> companies = Application.find(Company.class, query);
        return companies;
    }

    /**
     * @param pEIN
     * @param pSourceCompanyId
     * @param pSourceSystemCode
     * @param mRealmId
     * @return
     */
    public static DomainEntitySet<Company> searchCompaniesByEINSourceCompanyIdAndRealmId(String pEIN, String pSourceCompanyId, String mRealmId, SourceSystemCode pSourceSystemCode) throws DISException {

        if (StringUtils.isEmpty(pSourceCompanyId) && StringUtils.isEmpty(pEIN) && StringUtils.isEmpty(mRealmId)) {
            DISMessage disMessage = DISMessages.einOrSourceCompanyIdOrRealmIdRequiredForCoSearch();
            logger.error("Error Code : " + disMessage.getCode() + " Message : " + disMessage.getMessage());
            throw new DISException(disMessage);
        }

        Criterion<Company> companyCriterion = Company.SourceSystemCd().equalTo(pSourceSystemCode);

        if (StringUtils.isNotEmpty(pSourceCompanyId)) {
            companyCriterion = companyCriterion.And(Company.SourceCompanyId().equalTo(pSourceCompanyId));
        }

        if (StringUtils.isNotEmpty(pEIN)) {
            List<String> einEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pEIN);
            companyCriterion = companyCriterion.And(Company.FedTaxIdEnc().in(einEncList));
        }

        if (StringUtils.isNotEmpty(mRealmId)) {
            companyCriterion = companyCriterion.And(Company.IAMRealmId().equalTo(mRealmId));
        }

        Expression<Company> query =
                new Query<Company>()
                        .Where(companyCriterion);
        DomainEntitySet<Company> companies = Application.find(Company.class, query);
        return companies;
    }

    /**
     * @param pEIN
     * @param pSourceCompanyId
     * @param realmId
     * @return
     * @throws Exception
     */
    public static List<SAPCompanyDISDTO> createSAPCompanyList(String pEIN, String pSourceCompanyId, String realmId, SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO) throws Throwable {
        List<SAPCompanyDISDTO> coList = new ArrayList<SAPCompanyDISDTO>();
        DomainEntitySet<Company> companies = searchCompaniesByEINSourceCompanyIdAndRealmId(pEIN, pSourceCompanyId, realmId, SourceSystemCode.QBDT);
        for (Company company : companies) {
            try {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);
                SAPCompany sapCompany = CompanyTranslator.getSAPCompanyFromDomainEntity(company);

                SAPCompanyDISDTO sapCompanyDISDTO = PSPToDISTransformer.createCompanySAPDISDTO(sapCompany);
//            SAPCompanyStatus sapCompanyStatus = companyAdapter.getCompanyStatus(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), true, true);
//            SAPCompanyStatusDISDTO sapCompanyDtatusDISDTO = PSPToDISTransformer.createSAPCompanyStatus(sapCompanyStatus);
//            sapCompanyDISDTO.setSapCompanyStatus(sapCompanyDtatusDISDTO);
                coList.add(sapCompanyDISDTO);
            } finally {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            }
        }
        return coList;
    }

    // Add on logic that is not returned in SAPCompany that SAPCompany does not include.
    public static void updateSAPCompanyDISDTO(SAPCompanyDISDTO pSapCompanyDISDTO) throws Throwable {
        CompanyAdapter companyAdapter = new CompanyAdapter();
        SAPCompanyStatus sapCompanyStatus = companyAdapter.getCompanyStatus(pSapCompanyDISDTO.getSourceSystemEnum().toString(), pSapCompanyDISDTO.getCompanyId(), true, true);
        SAPCompanyStatusDISDTO sapCompanyStatusDISDTO = PSPToDISTransformer.createSAPCompanyStatus(sapCompanyStatus);
        pSapCompanyDISDTO.setSapCompanyStatus(sapCompanyStatusDISDTO);
        pSapCompanyDISDTO.populateServicesEndDate();
        // A new UOW will be started, so this method cannot be called
        //   when the company is created above in PSPHelper.createSAPCompanyList
        pSapCompanyDISDTO.populateCustomerAccountNumber();
        pSapCompanyDISDTO.populateEntityChanges();
        
        //todo joe verify everything below to see if needed
        List<SAPCompanyStrike> strikeInfo = companyAdapter.getStrikeInfo(pSapCompanyDISDTO.getSourceSystemEnum().toString(), pSapCompanyDISDTO.getCompanyId());
        int payrollRunCount = companyAdapter.getPayrollRunCount(pSapCompanyDISDTO.getSourceSystemEnum().toString(), pSapCompanyDISDTO.getCompanyId());
        int transactionReturnCount = companyAdapter.getBankReturnTransactionCount(pSapCompanyDISDTO.getSourceSystemEnum().toString(), pSapCompanyDISDTO.getCompanyId());
        List<SAPContact> contacts = companyAdapter.getCompanyContacts(pSapCompanyDISDTO.getSourceSystemEnum().toString(), pSapCompanyDISDTO.getCompanyId()).getContacts();
        SAPCompanyBankAccount activeBankAccount = companyAdapter.getActiveBankAccount(pSapCompanyDISDTO.getSourceSystemEnum().toString(), pSapCompanyDISDTO.getCompanyId());
        SAPQBDTTokens qbdtTokens = new TaxAdapter().getQBDTTokens(pSapCompanyDISDTO.getSourceSystemEnum().toString(), pSapCompanyDISDTO.getCompanyId());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(pSapCompanyDISDTO.getCompanyId(), SourceSystemCode.valueOf(pSapCompanyDISDTO.getSourceSystemEnum().toString()));
        pSapCompanyDISDTO.setRealmId(company.getIAMRealmId());
        SAPAgreementInfo agreementInfo = CompanyTranslator.getAgreementInfoFromDomainEntity(company.getQuickbooksInfo()); //todo this is deprecated and does not have valid data!
        SAPQuickbooksInfo quickbooksInfo = CompanyTranslator.getQuickbooksInfoFromDomainEntity(company.getQuickbooksInfo());
        SAPPINInfo pinInfo = CompanyTranslator.getPINInfo(company);
        SAPAddress legalAddress = CompanyTranslator.getSAPAddressFromDomainEntity(company.getLegalAddress());
        SAPAddress mailingAddress = CompanyTranslator.getSAPAddressFromDomainEntity(company.getMailingAddress());
        boolean isFlaggedForFraud = company.getIsFlaggedForFraud();
        String fundingModelCd = company.getFundingModel().getFundingModelCd();
        PayrollServices.rollbackUnitOfWork();

        pSapCompanyDISDTO.setBankReturnTransactionCount(transactionReturnCount);
        pSapCompanyDISDTO.setFraudFlag(isFlaggedForFraud);
        pSapCompanyDISDTO.setFundingModelCd(fundingModelCd);
        pSapCompanyDISDTO.setLastTransactionResponseToken(Long.parseLong(qbdtTokens.getHighToken()));
        pSapCompanyDISDTO.setNextPaycheckId(qbdtTokens.getPaycheckNextId());
        pSapCompanyDISDTO.setNextPayrollTransactionId(qbdtTokens.getPayrollTxNextId());
        pSapCompanyDISDTO.setPayrollRunCount(payrollRunCount);
        if (pinInfo != null) {
            pSapCompanyDISDTO.setPinCreated(pinInfo.getPinCreated());
            pSapCompanyDISDTO.setPinLocked(pinInfo.getPinLocked());
        }
        if (mailingAddress != null) {
            pSapCompanyDISDTO.setMailingAddress(PSPToDISTransformer.createSAPAddressDISDTO(mailingAddress));
        }
        if (legalAddress != null) {
            pSapCompanyDISDTO.setLegalAddress(PSPToDISTransformer.createSAPAddressDISDTO(legalAddress));
        }

        if (contacts != null) {
            pSapCompanyDISDTO.setContacts(PSPToDISTransformer.createSAPContact(contacts));
        }
        if (activeBankAccount != null) {
            pSapCompanyDISDTO.setActiveBankAccount(PSPToDISTransformer.createSAPCompanyBankAccountDISDTO(activeBankAccount));
        }

        if (agreementInfo != null) {
            pSapCompanyDISDTO.setAgreementInfo(PSPToDISTransformer.createSAPAgreementInfoDISDTO(agreementInfo));
        }

        if (quickbooksInfo != null) {
            pSapCompanyDISDTO.setQuickbooksInfo(PSPToDISTransformer.createSAPQuickbooksInfoDISDTO(quickbooksInfo));
        }

        if (strikeInfo != null) {
            pSapCompanyDISDTO.setStrikeCount(strikeInfo.size());
        }

    }
    public static void validateOperationPermissions(AuthUser pAuthUser,Class pOperationClass, String pOperationMethod) throws Exception {
        //REFUND_OPERATION_CLASS_NAME, REFUND_OPERATION_METHOD_NAME
        SAPAuthHelper.validateUserHasAccessToMethod(pAuthUser, pOperationClass, pOperationMethod);
    }

    public static void validateSession(AuthUser pAuthUser,String pToken) throws Exception {
        SAPAuthHelper.validateAuthToken(pAuthUser,pToken);
        SAPAuthHelper.validateSessionTimeout(pAuthUser);
    }


    public static void setPSPPrincipal(AuthUser pUser) {
        PspPrincipal principal = pUser.createPrincipal();
        PayrollServices.setCurrentPrincipal(principal);
    }

    public static void validateUserHasPermissionsInSAP(String pCorpId,String pToken,Class pClass,String pOperationName) throws Exception {
        AuthUser authUser = SAPAuthHelper.getLoggedInUser(pCorpId);
        PSPHelper.setPSPPrincipal(authUser);

        if (!FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_DISPSPUSERAUTH_HANDLER, true)) {
            PSPHelper.validateSession(authUser, pToken);
        }
        PSPHelper.validateOperationPermissions(authUser, pClass,pOperationName);

        SAPAuthHelper.updateLastRemoteCallTimestamp(authUser);
    }

    public static CompanyEvent findCompanyEventByTransactionId(Company pCompany,String pTxId,EventDetailTypeCode pEventDetailTypeCode,EventTypeCode pEventTypeCode) throws Exception {
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(pCompany, pEventTypeCode);
        for (CompanyEvent companyEvent : companyEvents) {
            DomainEntitySet<CompanyEventDetail> eventDetails = companyEvent.getCompanyEventDetails(pEventDetailTypeCode);
            for (CompanyEventDetail companyEventDetail : eventDetails) {
                if (companyEventDetail.getValue().equals(pTxId)) {
                    return companyEvent;
                }
            }
        }
        return null;
    }

    public static void addCompanyNote(Company pCompany,String pCorpId,String pCompanyEventId,String pNoteToAttachToRefundEvent) throws Throwable {
        CompanyAdapter companyAdapter = new CompanyAdapter();
        SAPCompanyNote note = new SAPCompanyNote();
        note.setInsertUserId(pCorpId);
        note.setNotes(pNoteToAttachToRefundEvent);
        companyAdapter.addCompanyNote(pCompany.getSourceSystemCd().toString(),pCompany.getSourceCompanyId(), pCompanyEventId,null,note);
    }
}
