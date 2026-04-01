package com.intuit.sbd.payroll.psp.processes.iam;

import com.intuit.platform.integration.ius.common.types.FeatureSetObject;
import com.intuit.platform.integration.ius.common.types.Grant;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.GuidelineGrantManager;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

public class AddOrUpdateGuidelineGrantProcessor extends Process{

    private GuidelineGrantManager guidelineGrantManager;
    private boolean useLatestInactivePrimaryEntitlement;
    private AddOrUpdateGrantProcessor addOrUpdateGrantProcessor;
    private Company company;

    private static final SpcfLogger LOGGER = Application.getLogger(AddOrUpdateGuidelineGrantProcessor.class);

    public AddOrUpdateGuidelineGrantProcessor(Company company) {
        this(company, false);
    }

    public AddOrUpdateGuidelineGrantProcessor(Company company, boolean useLatestInactivePrimaryEntitlement) {
        this.company = company;
        this.addOrUpdateGrantProcessor = new AddOrUpdateGrantProcessor(company);
        this.guidelineGrantManager = new GuidelineGrantManager();
        this.useLatestInactivePrimaryEntitlement = useLatestInactivePrimaryEntitlement;
    }

    @Override
    public ProcessResult execute() {
        ProcessResult processFlowResult = validate();
        if (processFlowResult.isSuccess()) {
            ProcessResult processingResult = process();
            processFlowResult.setResult(processingResult.getResult());
            processFlowResult.merge(processingResult);
        }
        return processFlowResult;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (Objects.isNull(company)) {
            validationResult.getMessages()
                    .BadProcessArgument("DomainCompany");
            return validationResult;
        }

        if (Objects.isNull(company.getIAMRealmId())) {
            validationResult.getMessages()
                    .BadProcessArgument("IAMRealmId");
            return validationResult;
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult<>();

        String realmId = company.getIAMRealmId();

        try {
            Grant grant = updatePayrollGrantFeatureSet(company);

            if(Objects.nonNull(grant)) {
                processResult.setResult(grant);
                return processResult;
            }

            LOGGER.error(String.format("Unable to add or update payroll grant for realmId=%s", company.getIAMRealmId()));
            processResult.getMessages().IUSGrantGenericError(realmId, "Unable to add or update grant");
        } catch (RuntimeException e) {
            LOGGER.error("Unable to add or update payroll grant for realmId \n "+e);
            processResult.getMessages().IUSGrantGenericError(realmId, e.getMessage());
        }

        return processResult;
    }

    private Grant updatePayrollGrantFeatureSet(Company company) {

        String realmId = company.getIAMRealmId();

        Entitlement entitlement = addOrUpdateGrantProcessor.getEntitlement(true);

        if(Objects.isNull(entitlement) && useLatestInactivePrimaryEntitlement) {
            entitlement = addOrUpdateGrantProcessor.getEntitlement(false);
        }

        if(Objects.isNull(entitlement)) {
            LOGGER.error(String.format("No active entitlement found for PSID=%s",  realmId));
            return null;
        }

        EntitlementCode entitlementCode = entitlement.getEntitlementCode();

        AssetItemCode assetItemCode = entitlementCode.getAssetItemCd();
        String featureSetCode = "DIY";

        if(Objects.nonNull(assetItemCode)) {
            featureSetCode = assetItemCode.name();
        } else {
            LOGGER.info("Asset Item Code not found, so using default featureSetCode DIY");
        }

        String grantFeatureCode = entitlementCode.getAssetItemNumber();
        if(Objects.isNull(grantFeatureCode)) {
            LOGGER.error("Asset Item Number not found, so unable to add or update grant");
            return null;
        }

        String optionalFeatureStatus = "ACTIVE";

        if(company.hasCancelledService(ServiceCode.Guideline401k)) {
            optionalFeatureStatus= "INACTIVE";
        }

        Map<RealmManager.GrantAttributeKey, String> grantAttributesMap = addOrUpdateGrantProcessor.getGrantAttributeKeyMap();

        Map<RealmManager.EntitlementInfoKey, String> entitlementInfoMap = addOrUpdateGrantProcessor.getEntitlementInfoKeyMap(true);

        LOGGER.info("Grant Attribute Map Value"+ grantAttributesMap);

        LOGGER.info("Entitlement Info Map Value"+ entitlementInfoMap);

        FeatureSetObject featureSetObject = guidelineGrantManager.createFeatureSetObject(featureSetCode, grantFeatureCode, optionalFeatureStatus,RealmManager.OptionalFeatureCode.GUIDELINE_401K);
        return guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);
    }

}
