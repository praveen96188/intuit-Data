package com.intuit.sbd.payroll.psp.gateways.iam.realm;

import com.intuit.platform.integration.ius.common.types.FeatureSetObject;
import com.intuit.platform.integration.ius.common.types.Grant;
import com.intuit.platform.integration.ius.common.types.GrantFeature;
import com.intuit.platform.integration.ius.common.types.OptionalFeature;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * Guideline401k Feature Set Object against the Payroll Grant {@link } will be present only for the companies who have onboarded to SMS platform.
 * Companies whose on boarding happened through PSP will not have any Feature Set against the Payroll Grant
 */
public class GuidelineGrantManager {

    private static SpcfLogger logger = Application.getLogger(GuidelineGrantManager.class);

    private RealmManager realmManager;

    public GuidelineGrantManager() {
        this.realmManager = new RealmManager();
    }

    public Grant addOrUpdatePayrollGrantWithFeatureSet(String realmId, Map<RealmManager.GrantAttributeKey, String> grantAttributesMap, Map<RealmManager.EntitlementInfoKey, String> entitlementInfoMap, FeatureSetObject featureSetObject) {
        Grant grant = realmManager.findPayrollGrant(realmId);

        if(Objects.isNull(grant)) {
            logger.info(String.format("addOrUpdatePayrollGrantWithFeatureSet grant is null for realm id {}", realmId));
            grant = addPayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);
        } else {
            logger.info(String.format("addOrUpdatePayrollGrantWithFeatureSet grant is not null for realm id {}", realmId));
            grant = updatePayrollGrantWithFeatureSet(grant, grantAttributesMap, entitlementInfoMap, featureSetObject);
        }

        return grant;
    }

    public Grant addPayrollGrantWithFeatureSet(String realmId, Map<RealmManager.GrantAttributeKey, String> grantAttributesMap, Map<RealmManager.EntitlementInfoKey, String> entitlementInfoMap, FeatureSetObject featureSetObject) {
        return realmManager.addPayrollGrantToRealm(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);
    }

    public Grant updatePayrollGrantWithFeatureSet(Grant grant, Map<RealmManager.GrantAttributeKey, String> grantAttributesMap, Map<RealmManager.EntitlementInfoKey, String> entitlementInfoMap, FeatureSetObject featureSetObject) {
        return realmManager.updatePayrollGrant(grant, grantAttributesMap, entitlementInfoMap, featureSetObject);
    }

    public Grant deactivatePayrollGrantFeatureSet(String realmId) {
        Grant grant = realmManager.findPayrollGrant(realmId);

        if(Objects.isNull(grant)) {
            logger.warn(String.format("No Grant found for RealmId=%s, cannot process deactivate request", realmId));
            return null;
        }

        FeatureSetObject featureSetObject = grant.getFeatureSetObj();

        if(Objects.isNull(featureSetObject)) {
            logger.warn(String.format("No featureSetObject found in the Payroll Grant for RealmId=%s, cannot process deactivate request", realmId));
            return grant;
        }

        List<OptionalFeature> optionalFeatures = featureSetObject.getOptionalFeatures();

        boolean guideline401kOptionalFeatureFound = false;

        for(OptionalFeature optionalFeature: optionalFeatures) {

            if(!StringUtils.equals(optionalFeature.getCode(), RealmManager.OptionalFeatureCode.GUIDELINE_401K.name())) {
                continue;
            }

            guideline401kOptionalFeatureFound = true;

            if(StringUtils.equals(optionalFeature.getStatus(), "INACTIVE")) {
                logger.warn(String.format("Guideline 401k Feature is already Inactive for RealmId=%s", realmId));
            }

            optionalFeature.setStatus("INACTIVE");
        }

        if(!guideline401kOptionalFeatureFound) {
            return grant;
        }

        return realmManager.updatePayrollGrant(grant);
    }

    public FeatureSetObject createFeatureSetObject(String featureSetCode, String grantFeatureCode, String optionalFeatureStatus,
                                                   RealmManager.OptionalFeatureCode optionalFeatureCode) {
        List<GrantFeature> grantFeatures = new ArrayList<>();

        GrantFeature grantFeature = realmManager.createGrantFeature(grantFeatureCode, RealmManager.GrantFeatureType.Feature);
        grantFeatures.add(grantFeature);

        List<OptionalFeature> optionalFeatures = new ArrayList<>();

        OptionalFeature optionalFeature = realmManager.createOptionalFeature(optionalFeatureCode,
                RealmManager.OptionalFeatureStatus.valueOf(optionalFeatureStatus), RealmManager.OptionalFeatureServiceStatus.PAID);
        optionalFeatures.add(optionalFeature);

        return realmManager.createFeatureSetObject(featureSetCode, grantFeatures, optionalFeatures);
    }


    public FeatureSetObject createFeatureSetObject(String featureSetCode, String grantFeatureCode, String optionalFeatureStatus) {

        return createFeatureSetObject(featureSetCode,grantFeatureCode, optionalFeatureStatus,RealmManager.OptionalFeatureCode.GUIDELINE_401K);
    }

}
