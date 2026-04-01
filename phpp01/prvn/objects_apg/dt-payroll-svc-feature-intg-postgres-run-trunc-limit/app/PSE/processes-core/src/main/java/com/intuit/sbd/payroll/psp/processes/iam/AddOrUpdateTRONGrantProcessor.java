package com.intuit.sbd.payroll.psp.processes.iam;

import com.intuit.platform.integration.ius.common.types.FeatureSetObject;
import com.intuit.platform.integration.ius.common.types.Grant;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.TRONGrantManager;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.payroll.authorization.utils.RequestSourceIdentifier;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Map;
import java.util.Objects;

public class AddOrUpdateTRONGrantProcessor extends AddOrUpdateGrantProcessor {

    private static final SpcfLogger logger = Application.getLogger(AddOrUpdateTRONGrantProcessor.class);

    private boolean useLatestInactivePrimaryEntitlement;
    private TRONGrantManager tronGrantManager;
    private RequestSourceIdentifier requestSourceIdentifier;
    private RealmManager.OptionalFeatureCode optionalFeatureCode;
    private boolean createOptionalFeatureforEWSCompany;


    public AddOrUpdateTRONGrantProcessor(Company domainCompany, boolean useLatestInactivePrimaryEntitlement,RealmManager.OptionalFeatureCode optionalFeatureCode,boolean createOptionalFeatureforEWSCompany) {
        super(domainCompany);

        this.useLatestInactivePrimaryEntitlement = useLatestInactivePrimaryEntitlement;
        this.tronGrantManager = new TRONGrantManager();
        this.requestSourceIdentifier = PayrollApplicationBeanFactory.getBean(RequestSourceIdentifier.class);
        this.optionalFeatureCode = optionalFeatureCode;
        this.createOptionalFeatureforEWSCompany = createOptionalFeatureforEWSCompany;
    }

    public AddOrUpdateTRONGrantProcessor(Company domainCompany, boolean useLatestInactivePrimaryEntitlement,RealmManager.OptionalFeatureCode optionalFeatureCode) {
        this(domainCompany,useLatestInactivePrimaryEntitlement,optionalFeatureCode,false);

    }

    public AddOrUpdateTRONGrantProcessor(Company domainCompany) {
        this(domainCompany, false);
    }

    public AddOrUpdateTRONGrantProcessor(Company domainCompany, boolean useLatestInactivePrimaryEntitlement) {
        this(domainCompany,useLatestInactivePrimaryEntitlement,RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT);
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

        if(!isMoneyMovementOnboardingEnabled() && !createOptionalFeatureforEWSCompany) {
            logger.info("Add or update TRON Payroll Grant is initiated from EWS so skipping Payroll Grant Update");
            return processResult;
        }

        String realmId = company.getIAMRealmId();

        try {
            Grant grant = updatePayrollGrantFeatureSet(company);

            if(Objects.nonNull(grant)) {
                processResult.setResult(grant);
                return processResult;
            }

            logger.error(String.format("Unable to add or update payroll grant for realmId=%s", company.getIAMRealmId()));
            processResult.getMessages().IUSGrantGenericError(realmId, "Unable to add or update grant");
        } catch (RuntimeException e) {
            logger.error("Unable to add or update payroll grant for realmId \n "+e);
            processResult.getMessages().IUSGrantGenericError(realmId, e.getMessage());
        }

        return processResult;
    }

    private Grant updatePayrollGrantFeatureSet(Company company) {

        String realmId = company.getIAMRealmId();

        Entitlement entitlement = getEntitlement(true);

        if(Objects.isNull(entitlement) && useLatestInactivePrimaryEntitlement) {
            entitlement = getEntitlement(false);
        }

        if(Objects.isNull(entitlement)) {
            logger.error(String.format("No active entitlement found for PSID=%s",  realmId));
            return null;
        }

        EntitlementCode entitlementCode = entitlement.getEntitlementCode();

        AssetItemCode assetItemCode = entitlementCode.getAssetItemCd();
        String featureSetCode = "DIY";

        if(Objects.nonNull(assetItemCode)) {
            featureSetCode = assetItemCode.name();
        } else {
            logger.info("Asset Item Code not found, so using default featureSetCode DIY");
        }

        String grantFeatureCode = entitlementCode.getAssetItemNumber();
        if(Objects.isNull(grantFeatureCode)) {
            logger.error("Asset Item Number not found, so unable to add or update grant");
            return null;
        }

        String optionalFeatureStatus = "ACTIVE";

        if(company.hasCancelledService(ServiceCode.DirectDeposit)) {
            optionalFeatureStatus= "INACTIVE";
        }


        Map<RealmManager.GrantAttributeKey, String> grantAttributesMap = getGrantAttributeKeyMap();

        Map<RealmManager.EntitlementInfoKey, String> entitlementInfoMap = getEntitlementInfoKeyMap(true);

        logger.info("Grant Attribute Map Value"+ grantAttributesMap);

        logger.info("Entitlement Info Map Value"+ entitlementInfoMap);

        FeatureSetObject featureSetObject = tronGrantManager.createFeatureSetObject(featureSetCode, grantFeatureCode, optionalFeatureStatus,optionalFeatureCode);

        return tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

    }

    private boolean isMoneyMovementOnboardingEnabled() {
        return requestSourceIdentifier.isPayrollPlugin() || company.isMoneyMovementOnboardingEnabled();
    }

}