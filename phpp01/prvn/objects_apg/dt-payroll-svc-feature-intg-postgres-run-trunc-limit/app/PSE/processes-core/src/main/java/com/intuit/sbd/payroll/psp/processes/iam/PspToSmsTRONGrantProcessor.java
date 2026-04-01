package com.intuit.sbd.payroll.psp.processes.iam;

import com.intuit.platform.integration.ius.common.types.FeatureSetObject;
import com.intuit.platform.integration.ius.common.types.Grant;
import com.intuit.sbd.payroll.psp.domain.AssetItemCode;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementCode;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.TRONGrantManager;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.payroll.authorization.utils.RequestSourceIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class PspToSmsTRONGrantProcessor extends AddOrUpdateGrantProcessor{

    private boolean useLatestInactivePrimaryEntitlement = false;
    private static final String INACTIVE="INACTIVE";
    private TRONGrantManager tronGrantManager;
    private RequestSourceIdentifier requestSourceIdentifier;
    private String realmId;


    public PspToSmsTRONGrantProcessor(Company company) {
        super(company);
        this.tronGrantManager = new TRONGrantManager();
        this.requestSourceIdentifier = PayrollApplicationBeanFactory.getBean(RequestSourceIdentifier.class);
        this.realmId =  company.getIAMRealmId();
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (Objects.isNull(company)) {
            validationResult.getMessages()
                    .BadProcessArgument("DomainCompany");
            return validationResult;
        }

        //Realm id can be null for legacy company. So do realm validation only for SMS onboarded companies
        if (isMoneyMovementOnboardingEnabled() && Objects.isNull(company.getIAMRealmId())) {
            validationResult.getMessages()
                    .BadProcessArgument("IAMRealmId");
            return validationResult;
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult<>();
        String logPrefix = "job=PSPtoSMSMigration, Action=PspToSmsTRONGrantProcessorExecute, Status={}{}";

        try {
            log.info(logPrefix, "AddOrUpdatePayrollGrantFeatureStart", StringUtils.EMPTY);
            Grant grant = updatePayrollGrantFeatureSet(company);

            if(Objects.nonNull(grant)) {
                processResult.setResult(grant);
                log.info(logPrefix, "AddOrUpdatePayrollGrantFeatureComplete", StringUtils.EMPTY);
                return processResult;
            }

            log.error(logPrefix, "AddOrUpdatePayrollGrantFeatureFailed", ", realmId=" + company.getIAMRealmId());
            processResult.getMessages().IUSGrantGenericError(realmId, "Unable to add or update grant");
        } catch (RuntimeException e) {
            log.error(logPrefix, "AddOrUpdatePayrollGrantFeatureException", ", realmId=" + company.getIAMRealmId(), e);
            processResult.getMessages().IUSGrantGenericError(realmId, e.getMessage());
        }

        return processResult;
    }

    private Grant updatePayrollGrantFeatureSet(Company company) {

        Entitlement entitlement = getEntitlement(true);

        if(Objects.isNull(entitlement) && useLatestInactivePrimaryEntitlement) {
            entitlement = getEntitlement(false);
        }

        if(Objects.isNull(entitlement)) {
            log.error("No active entitlement found for psid={}, realmId={}", company.getSourceCompanyId(), realmId);
            return null;
        }

        EntitlementCode entitlementCode = entitlement.getEntitlementCode();

        AssetItemCode assetItemCode = entitlementCode.getAssetItemCd();
        String featureSetCode = "DIY";

        if(Objects.nonNull(assetItemCode)) {
            featureSetCode = assetItemCode.name();
        } else {
            log.info("Asset Item Code not found, so using default featureSetCode DIY");
        }

        String grantFeatureCode = entitlementCode.getAssetItemNumber();
        if(Objects.isNull(grantFeatureCode)) {
            log.error("Asset Item Number not found, so unable to add or update grant");
            return null;
        }

        String optionalFeatureStatus = "ACTIVE";

        if(company.hasCancelledService(com.intuit.sbd.payroll.psp.domain.ServiceCode.DirectDeposit)) {
            optionalFeatureStatus= "INACTIVE";
        }


        Map<RealmManager.GrantAttributeKey, String> grantAttributesMap = getGrantAttributeKeyMap();

        Map<RealmManager.EntitlementInfoKey, String> entitlementInfoMap = getEntitlementInfoKeyMap(true);

        log.info("Grant Attribute Map Value" + grantAttributesMap);

        log.info("Entitlement Info Map Value" + entitlementInfoMap);

        //creating Map of optional features
        Map<RealmManager.OptionalFeatureCode,String> optionalFeaturesMap = new HashMap<>();
        optionalFeaturesMap.put(RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT,optionalFeatureStatus);
        optionalFeaturesMap.put(RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT_EWS,INACTIVE);

        FeatureSetObject featureSetObject = tronGrantManager.createFeatureSetObject(featureSetCode, grantFeatureCode, optionalFeaturesMap);

        return tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

    }

    private boolean isMoneyMovementOnboardingEnabled() {
        return requestSourceIdentifier.isPayrollPlugin() || company.isMoneyMovementOnboardingEnabled();
    }

}


