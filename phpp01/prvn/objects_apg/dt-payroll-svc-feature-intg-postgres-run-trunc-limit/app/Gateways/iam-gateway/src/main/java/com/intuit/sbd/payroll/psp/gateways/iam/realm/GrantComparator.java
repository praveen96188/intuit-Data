package com.intuit.sbd.payroll.psp.gateways.iam.realm;

import com.intuit.platform.integration.ius.common.types.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GrantComparator {

    public boolean isGrantSame(Grant grant, Map<RealmManager.GrantAttributeKey, String> grantAttributesMap, Map<RealmManager.EntitlementInfoKey, String> entitlementInfoMap, FeatureSetObject featureSetObject) {
        if(!isGrantAttributesSame(grant, grantAttributesMap)) {
            return false;
        }

        if(!isGrantEntitlementInfoSame(grant, entitlementInfoMap)) {
            return false;
        }

        if(!isFeatureSetObjectSame(grant.getFeatureSetObj(), featureSetObject)) {
            return false;
        }
        return true;
    }

    private boolean isGrantAttributesSame(Grant grant, Map<RealmManager.GrantAttributeKey, String> grantAttributesMap) {
        for (RealmManager.GrantAttributeKey grantAttribute: RealmManager.GrantAttributeKey.values()) {
            String grantAttributeValue = grantAttributesMap.get(grantAttribute);

            String existingGrantAttributeValue = null;
            switch (grantAttribute) {
                case RELEASE_NUMBER:
                    existingGrantAttributeValue = grant.getReleaseNumber();
                    break;
                case FLAVOR:
                    existingGrantAttributeValue = grant.getFlavor();
                    break;
                case LICENSE_NUMBER:
                    existingGrantAttributeValue = grant.getLicenseNumber();
                    break;
                case VERSION_NUMBER:
                    existingGrantAttributeValue = grant.getVersionNumber();
                    break;
                case BILLING_REALM_ID:
                    existingGrantAttributeValue = grant.getBillingRealmId();
                    break;
                case SERVICE_STATUS:
                    existingGrantAttributeValue = grant.getServiceStatus();
                    break;
            }

            if(StringUtils.isEmpty(grantAttributeValue) && StringUtils.isEmpty(existingGrantAttributeValue)) {
                continue;
            }

            if(!StringUtils.equalsIgnoreCase(grantAttributeValue, existingGrantAttributeValue)) {
                return false;
            }
        }

        return true;
    }

    private boolean isGrantEntitlementInfoSame(Grant grant, Map<RealmManager.EntitlementInfoKey, String> entitlementInfoMap) {
        if(MapUtils.isEmpty(entitlementInfoMap)) {
            return true;
        }

        for (NameValuePair nameValuePair: grant.getEntitlementInfo()) {
            RealmManager.EntitlementInfoKey entitlementInfoKey = RealmManager.EntitlementInfoKey.valueOf(nameValuePair.getName());
            String value = entitlementInfoMap.get(entitlementInfoKey);

            if(StringUtils.isEmpty(value) && StringUtils.isEmpty(nameValuePair.getValue())) {
                continue;
            }

            if(!StringUtils.equalsIgnoreCase(value, nameValuePair.getValue())) {
                return false;
            }
            entitlementInfoMap.remove(entitlementInfoKey);
        }

        if(!entitlementInfoMap.isEmpty()) {
            return false;
        }

        return true;
    }

    public boolean isFeatureSetObjectSame(FeatureSetObject existingFeatureSetObj, FeatureSetObject featureSetObject) {


        if(Objects.isNull(existingFeatureSetObj) && Objects.isNull(featureSetObject)) {
            return true;
        }

        if((Objects.isNull(existingFeatureSetObj) && Objects.nonNull(featureSetObject))
                || (Objects.nonNull(existingFeatureSetObj) && Objects.isNull(featureSetObject))) {
            return false;
        }

        if(!StringUtils.equalsIgnoreCase(existingFeatureSetObj.getFeatureSetCode(), featureSetObject.getFeatureSetCode())) {
            return false;
        }

        if(!isGrantFeaturesSame(existingFeatureSetObj.getFeatures(), featureSetObject.getFeatures())) {
            return false;
        }

        if(!isOptionalFeaturePresent(existingFeatureSetObj.getOptionalFeatures(), featureSetObject.getOptionalFeatures())) {
            return false;
        }

        return true;
    }

    private boolean isGrantFeaturesSame(List<GrantFeature> existingGrantFeatures, List<GrantFeature> grantFeatures) {
        if(existingGrantFeatures.size()!= grantFeatures.size()) {
            return false;
        }

        for (int i=0; i< existingGrantFeatures.size(); i++) {
            GrantFeature existingGrantFeature = existingGrantFeatures.get(i);
            GrantFeature grantFeature = grantFeatures.get(i);

            if(!isGrantFeatureSame(existingGrantFeature, grantFeature)) {
                return false;
            }
        }
        return true;
    }

    public boolean isOptionalFeaturePresent(List<OptionalFeature> existingOptionalFeatures, List<OptionalFeature> optionalFeatures) {

        //Only one optional feature we are going to create
       // Empty optional feature will always be present
        if(optionalFeatures == null || optionalFeatures.size()==0){
            return true;
        }

       /* if(existingOptionalFeatures.size() != optionalFeatures.size()){
            return false;
        }*/

        return CollectionUtils.isEqualCollection(existingOptionalFeatures,optionalFeatures);

    }

    private boolean isGrantFeatureSame(GrantFeature existingGrantFeature, GrantFeature grantFeature) {
        if(!StringUtils.equalsIgnoreCase(existingGrantFeature.getCode(), grantFeature.getCode())) {
            return false;
        }

        if(!StringUtils.equalsIgnoreCase(existingGrantFeature.getType(), grantFeature.getType())) {
            return false;
        }

        return true;
    }

    private boolean isOptionalFeatureSame(OptionalFeature existingOptionalFeature, OptionalFeature optionalFeature) {
        if(!StringUtils.equalsIgnoreCase(existingOptionalFeature.getCode(), optionalFeature.getCode())) {
            return false;
        }

        if(!StringUtils.equalsIgnoreCase(existingOptionalFeature.getStatus(), optionalFeature.getStatus())) {
            return false;
        }

        if(!StringUtils.equalsIgnoreCase(existingOptionalFeature.getServiceStatus(), optionalFeature.getServiceStatus())) {
            return false;
        }

        return true;
    }
}
