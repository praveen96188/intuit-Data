package com.intuit.sbd.payroll.psp.gateways.iam.realm;

import com.intuit.client.ius.GrantType;
import com.intuit.client.ius.IUSGrantClient;
import com.intuit.platform.integration.ius.common.types.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.gateways.iam.identity.grants.IdentityGrantClient;
import com.intuit.sbd.payroll.psp.gateways.iam.identity.grants.IdentityGrantFactory;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RealmManager {

    private Logger logger = LoggerFactory.getLogger(RealmManager.class);

    public static final String GRANT_ACTIVE_STATUS = "ACTIVE";
    public static final String GRANT_FLAVOUR_EWS = "ews";

    private GrantComparator grantComparator;
    private IdentityGrantFactory identityGrantFactory;

    public RealmManager() {
        this.grantComparator = new GrantComparator();
        this.identityGrantFactory = PayrollApplicationBeanFactory.getBean(IdentityGrantFactory.class);
    }

    private IdentityGrantClient getIdentityGrantClient(){
        return identityGrantFactory.getIdentityGrantsClientInstance();
    }

    public List<Grant> getAllGrantsForRealmId(String realmId) {
        return getIdentityGrantClient().getAllGrants(realmId);
    }

    public Grant findGrantForRealmIdAndOfferingId(String mRealmId, String offeringId) {
        Validate.notBlank(mRealmId, "mRealmId cannot be null or empty");
        Validate.notBlank(offeringId, "offeringId cannot be null or empty");
        List<Grant> grantsForRealmId = getAllGrantsForRealmId(mRealmId);
        for (Grant grant : grantsForRealmId) {
            if (offeringId.equals(grant.getOfferingId())) {
                return grant;
            }
        }
        return null;
    }

    //check for VMP grant in realm
    public Grant findVMPGrant(String mRealmId) {
        return findGrantForRealmIdAndOfferingId(mRealmId, IUSGrantClient.VMP_GRANT_OFFERING_ID);
    }

    //returns true if grant exists
    public Boolean realmHasVMPGrant(String mRealmId) {
        return Objects.nonNull(findVMPGrant(mRealmId));
    }

    //Creating and Adding VMP grant
    public Grant addVMPGrant(Company company) {

        //Check for already existing grant
        Grant grant = findVMPGrant(company.getIAMRealmId());

        if(Objects.isNull(grant)) {
            logger.info(String.format("VMP Grant add started realmId=%s", company.getIAMRealmId()));

            Grant vmpGrant = new Grant();
            vmpGrant.setRealmId(company.getIAMRealmId());
            vmpGrant.setGrantType(GrantType.OFFERING_APP_GRANT.toString());
            vmpGrant.setOfferingId(IUSGrantClient.VMP_GRANT_OFFERING_ID);
            vmpGrant.setStatus(GRANT_ACTIVE_STATUS);
            vmpGrant.setFlavor(GRANT_FLAVOUR_EWS);
            getIdentityGrantClient().addGrants(vmpGrant, true);
        }
        return findVMPGrant(company.getIAMRealmId());
    }

    public Grant findPayrollGrant(String mRealmId) {
        return findGrantForRealmIdAndOfferingId(mRealmId, IUSGrantClient.EWS_GRANT_OFFERING_ID);
    }

    public Boolean realmHasPayrollGrant(String mRealmId) {
        return Objects.nonNull(findPayrollGrant(mRealmId));
    }

    public Grant addPayrollGrantToRealm(String realmid) {
        return addPayrollGrantToRealm(realmid, Collections.EMPTY_MAP, Collections.EMPTY_MAP, null);
    }

    public Grant addPayrollGrantToRealm(String realmid, Map<GrantAttributeKey, String> grantAttributesMap, Map<EntitlementInfoKey, String> entitlementInfoMap) {
        return addPayrollGrantToRealm(realmid, grantAttributesMap, entitlementInfoMap, null);
    }

    public Grant addPayrollGrantToRealm(String realmid, Map<GrantAttributeKey, String> grantAttributesMap, Map<EntitlementInfoKey, String> entitlementInfoMap, FeatureSetObject featureSetObject) {
        logger.info(String.format("Payroll Grant add started for realmId=%s", realmid));
        Grant payrollGrant = new Grant();
        payrollGrant.setRealmId(realmid);
        payrollGrant.setGrantType(GrantType.OFFERING_APP_GRANT.toString());
        payrollGrant.setOfferingId(IUSGrantClient.EWS_GRANT_OFFERING_ID);
        payrollGrant.setStatus("ACTIVE");

        populateGrantAttributes(payrollGrant, grantAttributesMap);

        payrollGrant.getEntitlementInfo().addAll(createEntitlementInfo(entitlementInfoMap));

        if(Objects.nonNull(featureSetObject)) {
            payrollGrant.setFeatureSetObj(featureSetObject);
        }
        getIdentityGrantClient().addGrants(payrollGrant, false);
        logger.info(String.format("Payroll Grant has been successfully added for the realmId=%s", realmid));

        return findPayrollGrant(realmid);
    }

    public Grant updatePayrollGrant(Grant grant) {
        return updatePayrollGrant(grant, Collections.EMPTY_MAP, Collections.EMPTY_MAP, null);
    }

    public Grant updatePayrollGrant(Grant grant, Map<RealmManager.GrantAttributeKey, String> grantAttributesMap, Map<RealmManager.EntitlementInfoKey, String> entitlementInfoMap) {
        return updatePayrollGrant(grant, grantAttributesMap, entitlementInfoMap, null);
    }

    //TODO get the grant from IUS and update it before creating the feature set object

    public Grant updatePayrollGrant(Grant grant, Map<RealmManager.GrantAttributeKey, String> grantAttributesMap, Map<RealmManager.EntitlementInfoKey, String> entitlementInfoMap, FeatureSetObject featureSetObject) {
        logger.info(String.format("Payroll Grant update started for realmId=%s", grant.getRealmId()));
        Validate.notNull(grant, "grant cannot be null");

        // Grant Updates doesn't support assetId attributes, so nullifying the assetId
        grant.setAssetId(null);

        if(Objects.nonNull(featureSetObject) &&
                grantComparator.isGrantSame(grant, grantAttributesMap, entitlementInfoMap, featureSetObject)) {
            logger.info(String.format("Skipped the Payroll Grant update for the realmId=%s as the FeatureSet Objects are same", grant.getRealmId()));
            return grant;
        }

        if(Objects.nonNull(featureSetObject)) {

            //Add all the optional features existing in the grat to the current feature set and update
            if(grant.getFeatureSetObj() != null ){
                Set<String> newOptionalFeatures=featureSetObject.getOptionalFeatures().stream().map(optionalFeature -> optionalFeature.getCode()).collect(Collectors.toSet());

                List<OptionalFeature> existingGrantOptionalFeatures = grant.getFeatureSetObj().getOptionalFeatures();
                existingGrantOptionalFeatures.removeIf(optionalFeature -> newOptionalFeatures.contains(optionalFeature.getCode()) );
                featureSetObject.getOptionalFeatures().addAll(existingGrantOptionalFeatures);

            }


            grant.setFeatureSetObj(featureSetObject);
        }

        populateGrantAttributes(grant, grantAttributesMap);

        updateEntitlementInfo(grant, entitlementInfoMap);

        getIdentityGrantClient().updateGrants(grant);
        logger.info(String.format("Payroll Grant has been successfully updated for the realmId=%s", grant.getRealmId()));

        return findPayrollGrant(grant.getRealmId());
    }

    public FeatureSetObject createFeatureSetObject(String featureSetCode, List<GrantFeature> grantFeatures, List<OptionalFeature> optionalFeatures) {
        FeatureSetObject featureSetObject = new FeatureSetObject();
        featureSetObject.setFeatureSetCode(featureSetCode);
        featureSetObject.getFeatures().addAll(grantFeatures);
        featureSetObject.getOptionalFeatures().addAll(optionalFeatures);
        return featureSetObject;
    }

    public GrantFeature createGrantFeature(String code, GrantFeatureType type) {
        GrantFeature grantFeature = new GrantFeature();
        grantFeature.setCode(code);
        grantFeature.setType(type.getValue());
        return grantFeature;
    }

    public OptionalFeature createOptionalFeature(OptionalFeatureCode code, OptionalFeatureStatus status, OptionalFeatureServiceStatus serviceStatus) {
        OptionalFeature optionalFeature = new OptionalFeature();
        optionalFeature.setCode(code.name());
        optionalFeature.setStatus(status.name());
        optionalFeature.setServiceStatus(serviceStatus.name());
        return optionalFeature;
    }

    private List<NameValuePair> createEntitlementInfo(Map<EntitlementInfoKey, String> entitlementInfoMap) {
        List<NameValuePair> entitlementInfoNameValuePairs = new ArrayList<>();

        if(MapUtils.isEmpty(entitlementInfoMap)) {
            return entitlementInfoNameValuePairs;
        }

        for (Map.Entry<EntitlementInfoKey, String> entitlementInfo: entitlementInfoMap.entrySet()) {
            if(StringUtils.isEmpty(entitlementInfo.getValue())) {
                continue;
            }

            NameValuePair nameValuePair = new NameValuePair();
            nameValuePair.setName(entitlementInfo.getKey().name());
            nameValuePair.setValue(entitlementInfo.getValue());
            entitlementInfoNameValuePairs.add(nameValuePair );
        }
        return entitlementInfoNameValuePairs;
    }

    private void updateEntitlementInfo(Grant grant, Map<EntitlementInfoKey, String> entitlementInfoMap) {
        if(MapUtils.isEmpty(entitlementInfoMap)) {
            return;
        }

        for (NameValuePair nameValuePair: grant.getEntitlementInfo()) {
            EntitlementInfoKey entitlementInfoKey = EntitlementInfoKey.valueOf(nameValuePair.getName());
            String value = entitlementInfoMap.get(entitlementInfoKey);
            if(StringUtils.isEmpty(value)) {
                continue;
            }
            nameValuePair.setValue(value);
            entitlementInfoMap.remove(entitlementInfoKey);
        }

        grant.getEntitlementInfo().addAll(createEntitlementInfo(entitlementInfoMap));
    }

    private void populateGrantAttributes(Grant grant, Map<GrantAttributeKey, String> grantAttributesMap) {
        for (GrantAttributeKey grantAttribute: GrantAttributeKey.values()) {

            String grantAttributeValue = grantAttributesMap.get(grantAttribute);
            if(StringUtils.isEmpty(grantAttributeValue)) {
                continue;
            }

            switch (grantAttribute) {
                case RELEASE_NUMBER:
                    grant.setReleaseNumber(grantAttributeValue);
                    break;
                case FLAVOR:
                    grant.setFlavor(grantAttributeValue);
                    break;
                case LICENSE_NUMBER:
                    grant.setLicenseNumber(grantAttributeValue);
                    break;
                case VERSION_NUMBER:
                    grant.setVersionNumber(grantAttributeValue);
                    break;
                case BILLING_REALM_ID:
                    grant.setBillingRealmId(grantAttributeValue);
                    break;
                case SERVICE_STATUS:
                    grant.setServiceStatus(grantAttributeValue);
                    break;
            }
        }

    }

    enum GrantFeatureType {
        Feature("Feature"),
        FeatureSet("Feature Set");

        private String value;

        GrantFeatureType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum OptionalFeatureCode {
        DIRECT_DEPOSIT,
        DIRECT_DEPOSIT_EWS,
        GUIDELINE_401K
    }

    enum OptionalFeatureStatus {
        ACTIVE,
        INACTIVE
    }

    enum OptionalFeatureServiceStatus {
        PAID,
        FREE
    }

    public enum EntitlementInfoKey {
        SOURCE,
        LICENSE_NUMBER,
        EOC,
        SUBSCRIPTION_NUMBER,
        ENTITLED_OFFERING_ID,
        READ_ACCESS_END_DATE,
        BILLING_PLATFORM
    }

    public enum GrantAttributeKey {
        RELEASE_NUMBER,
        FLAVOR,
        LICENSE_NUMBER,
        VERSION_NUMBER,
        BILLING_REALM_ID,
        SERVICE_STATUS
    }
}
