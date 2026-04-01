package com.intuit.sbd.payroll.psp.processes.common;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QuickbooksInfoDTO;
import com.intuit.sbd.payroll.psp.common.utils.RealmLogHelper;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;

public class CompanyRealmValidator {

    private static final SpcfLogger logger = Application.getLogger(CompanyRealmValidator.class);
    private static boolean useLaunchDarkly = true;
    private static boolean enableRealmValidation;
    private static boolean enableRealmValidationForTRONCompanies;

    public ProcessResult validate(CompanyCoreEventType companyCoreEventType, CompanyDTO mDtoCompany) {
        return validate(companyCoreEventType,null, mDtoCompany);
    }

    public ProcessResult validate(CompanyCoreEventType companyCoreEventType, Company mDomainCompany, CompanyDTO mDtoCompany) {
        ProcessResult processResult = new ProcessResult();
        Pair<String, String> realmPair = null;
        String iamRealmId = null;
        String eventType = null;
        try {
            realmPair = Objects.nonNull(mDomainCompany)? determineCompanyRealmId(mDomainCompany, mDtoCompany) : determineCompanyRealmId(mDtoCompany);

            if(Objects.isNull(realmPair)) {
                return processResult;
            }

            eventType = realmPair.getLeft();
            iamRealmId = realmPair.getRight();

            if(StringUtils.isEmpty(iamRealmId)) {
                return processResult;
            }

            if((eventType == RealmLogHelper.COMPANY_DIFFERENT_REALM_UPDATE) && Objects.nonNull(mDomainCompany)) {
                addValidationErrors(mDomainCompany, processResult, iamRealmId, RealmErrorType.UPDATE_NOT_ALLOWED);
            }

            Company company = Company.findActiveCompanyByRealmId(iamRealmId);

            if(Objects.isNull(company)) {
                return processResult;
            }

            if((companyCoreEventType == CompanyCoreEventType.COMPANY_UPDATE || companyCoreEventType == CompanyCoreEventType.QB_COMPANY_UPDATE) && StringUtils.equals(company.getSourceCompanyId(), mDomainCompany.getSourceCompanyId())) {
                return processResult;
            }

            addValidationErrors(mDomainCompany, processResult, iamRealmId, RealmErrorType.ACTIVE);

        } catch (RuntimeException e) {
            addValidationErrors(mDomainCompany, processResult, iamRealmId, RealmErrorType.DUPLICATE);
        }

        return processResult;
    }

    private void addValidationErrors(Company mDomainCompany, ProcessResult processResult, String iamRealmId, RealmErrorType realmErrorType) {

        if(Objects.nonNull(mDomainCompany) &&
                mDomainCompany.isCompanyOnService(ServiceCode.Guideline401k)) {
            addValidationErrors(processResult, iamRealmId, realmErrorType);
            return;
        }

        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.SMS_COMPANY_REALM_UPDATE, false)){
            return;
        }

        // Realm update is supported by Account service removing the realm update block
        if(Objects.nonNull(mDomainCompany) && mDomainCompany.isMoneyMovementOnboardingEnabled()) {
            addValidationErrorsForMoneyMovementEnabledCompanies(processResult,iamRealmId,realmErrorType);
        }

        addValidationErrorsForNonMoneyMovementEnabledCompanies(processResult, iamRealmId, realmErrorType);
    }

    private void addValidationErrorsForNonMoneyMovementEnabledCompanies(ProcessResult processResult, String iamRealmId, RealmErrorType realmErrorType) {
        if(!isRealmValidationEnabled()) {
            logger.warn(String.format("AddValidationErrorsForNonMoneyMovementEnabledCompanies - Active Company Found For RealmId=%s ", iamRealmId));
            return;
        }

        addValidationErrors(processResult, iamRealmId, realmErrorType);
    }

    private void addValidationErrorsForMoneyMovementEnabledCompanies(ProcessResult processResult, String iamRealmId, RealmErrorType realmErrorType) {
        if(!isRealmValidationEnabledForTRONCompanies()) {
            logger.warn(String.format("AddValidationErrorsForMoneyMovementEnabledCompanies - Active Company Found For RealmId=%s ", iamRealmId));
            return;
        }

        addValidationErrors(processResult, iamRealmId, realmErrorType);
    }

    private void addValidationErrors(ProcessResult processResult, String iamRealmId, RealmErrorType realmErrorType) {
        switch (realmErrorType) {
            case ACTIVE:
                processResult.getMessages().ActiveCompanyFoundForRealm(EntityName.Company, iamRealmId);
                break;
            case DUPLICATE:
                processResult.getMessages().DuplicateActiveCompaniesFoundForRealm(EntityName.Company, iamRealmId);
                break;
            case UPDATE_NOT_ALLOWED:
                processResult.getMessages().RealmUpdateNotAllowed(EntityName.Company, iamRealmId);
                break;
        }
    }

    private Pair<String, String> determineCompanyRealmId(Company mDomainCompany, CompanyDTO mDtoCompany) {
        // Realm Id Add use case
        if(StringUtils.isEmpty(mDomainCompany.getIAMRealmId()) && StringUtils.isNotEmpty(mDtoCompany.getIAMRealmId())) {
            return new ImmutablePair(RealmLogHelper.COMPANY_REALM_ADD, mDtoCompany.getIAMRealmId());
        }

        // Realm Id Update use case - Incoming Realm Id is same as existing Company Realm Id
        if(StringUtils.isNotEmpty(mDomainCompany.getIAMRealmId()) && StringUtils.isNotEmpty(mDtoCompany.getIAMRealmId()) && StringUtils.equals(mDomainCompany.getIAMRealmId(), mDtoCompany.getIAMRealmId())) {
            return new ImmutablePair(RealmLogHelper.COMPANY_REALM_UPDATE, mDomainCompany.getIAMRealmId());
        }

        // Realm Id Update use case - Incoming Realm Id is different from existing Company Realm Id
        if(StringUtils.isNotEmpty(mDomainCompany.getIAMRealmId()) && StringUtils.isNotEmpty(mDtoCompany.getIAMRealmId()) && !StringUtils.equals(mDomainCompany.getIAMRealmId(), mDtoCompany.getIAMRealmId())) {
            return new ImmutablePair(RealmLogHelper.COMPANY_DIFFERENT_REALM_UPDATE, mDtoCompany.getIAMRealmId());
        }

        // Realm Id Delete use case
        if(StringUtils.isNotEmpty(mDomainCompany.getIAMRealmId()) && StringUtils.isEmpty(mDtoCompany.getIAMRealmId())) {
            return new ImmutablePair(RealmLogHelper.COMPANY_REALM_DELETE, mDomainCompany.getIAMRealmId());
        }

        QuickbooksInfoDTO quickbooksInfoDTO = mDtoCompany.getQuickBooksInfo();
        if(Objects.isNull(quickbooksInfoDTO)) {
            return null;
        }

        // Copying the Quickbooks Info Realm to Company Realm
        if(StringUtils.isNotEmpty(quickbooksInfoDTO.getIAMRealmId())) {
            return new ImmutablePair(RealmLogHelper.QB_REALM_ADD, quickbooksInfoDTO.getIAMRealmId());
        }

        return null;
    }

    private Pair<String, String> determineCompanyRealmId(CompanyDTO mDtoCompany) {
        if(StringUtils.isNotEmpty(mDtoCompany.getIAMRealmId())) {
            return new ImmutablePair(RealmLogHelper.COMPANY_REALM_ADD, mDtoCompany.getIAMRealmId());
        }

        QuickbooksInfoDTO quickbooksInfoDTO = mDtoCompany.getQuickBooksInfo();
        if(Objects.isNull(quickbooksInfoDTO)) {
            return null;
        }

        if(StringUtils.isNotEmpty(quickbooksInfoDTO.getIAMRealmId())) {
            return new ImmutablePair(RealmLogHelper.QB_REALM_ADD, quickbooksInfoDTO.getIAMRealmId());
        }

        return null;
    }

    private boolean isRealmValidationEnabled() {
        if(useLaunchDarkly) {
            return FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_REALM_VALIDATION, false);
        }
        return enableRealmValidation;
    }

    private boolean isRealmValidationEnabledForTRONCompanies() {
        if(useLaunchDarkly) {
            return FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_REALM_VALIDATION_TRON_COMPANIES, false);
        }
        return enableRealmValidationForTRONCompanies;
    }

    public static void setUseLaunchDarkly(boolean useLaunchDarkly) {
        CompanyRealmValidator.useLaunchDarkly = useLaunchDarkly;
    }

    public static void setEnableRealmValidation(boolean enableRealmValidation) {
        CompanyRealmValidator.enableRealmValidation = enableRealmValidation;
    }

    public static void setEnableRealmValidationForTRONCompanies(boolean enableRealmValidationForTRONCompanies) {
        CompanyRealmValidator.enableRealmValidationForTRONCompanies = enableRealmValidationForTRONCompanies;
    }

    public enum CompanyCoreEventType {
        COMPANY_ADD,
        COMPANY_UPDATE,
        QB_COMPANY_UPDATE
    }

    public enum RealmErrorType {
        ACTIVE,
        DUPLICATE,
        UPDATE_NOT_ALLOWED
    }

}
