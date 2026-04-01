package com.intuit.ems.payroll.psp.gateways.iam;

import com.intuit.platform.integration.ius.common.types.*;
import com.intuit.sbd.payroll.psp.gateways.iam.IUSClientWrapper;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.GuidelineGrantManager;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager.EntitlementInfoKey;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager.GrantAttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GuidelineGrantManagerTests {

    private RealmManager realmManager;
    private GuidelineGrantManager guidelineGrantManager;

    private static final String GRANT_FEATURE_CODE_DD = "1099581";
    private static final String GRANT_FEATURE_CODE_ASSISTED = "1099734";
    private static final String FEATURE_SET_CODE_DIY = "DIY";
    private static final String FEATURE_SET_CODE_ASSISTED = "Assisted";
    private static final String OPTIONAL_FEATURE_STATUS_ACTIVE = "ACTIVE";
    private static final String OPTIONAL_FEATURE_STATUS_INACTIVE = "INACTIVE";

    private static final String REALM_EMAIL = "qbdthcmtest+iamtestpass1@gmail.com";
    private static final String REALM_NAME_PREFIX = "GUIDELINE";

    private static final RealmManager.OptionalFeatureCode GUIDELINE_FEATURE_CODE = RealmManager.OptionalFeatureCode.GUIDELINE_401K;

    public GuidelineGrantManagerTests() {
        realmManager = new RealmManager();
        guidelineGrantManager = new GuidelineGrantManager();
    }

    @Test
    public void testPayrollGrantAddWithGuidelineFeatureSet() {
        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = guidelineGrantManager.createFeatureSetObject(FEATURE_SET_CODE_DIY,
                GRANT_FEATURE_CODE_DD, OPTIONAL_FEATURE_STATUS_ACTIVE, GUIDELINE_FEATURE_CODE);
        guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

        assertPayrollGrantGuidelineFeatureSet(realmId, FEATURE_SET_CODE_DIY, GRANT_FEATURE_CODE_DD, OPTIONAL_FEATURE_STATUS_ACTIVE);
    }

    @Test
    public void testPayrollGrantUpdateWithGuidelineFeatureSet() {
        String realmId = createRealmWithPayrollGrant();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = guidelineGrantManager.createFeatureSetObject(
                FEATURE_SET_CODE_ASSISTED, GRANT_FEATURE_CODE_DD, OPTIONAL_FEATURE_STATUS_ACTIVE, GUIDELINE_FEATURE_CODE);
        guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

        assertPayrollGrantGuidelineFeatureSet(realmId, FEATURE_SET_CODE_ASSISTED, GRANT_FEATURE_CODE_DD, OPTIONAL_FEATURE_STATUS_ACTIVE);
    }

    @Test
    public void testNoChangeInPayrollGuidelineGrant() {

        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = guidelineGrantManager.createFeatureSetObject(
                FEATURE_SET_CODE_ASSISTED, GRANT_FEATURE_CODE_ASSISTED, OPTIONAL_FEATURE_STATUS_ACTIVE, GUIDELINE_FEATURE_CODE);
        guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

        guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

        assertPayrollGrantGuidelineFeatureSet(realmId, FEATURE_SET_CODE_ASSISTED, GRANT_FEATURE_CODE_ASSISTED, OPTIONAL_FEATURE_STATUS_ACTIVE);
    }

    @Test
    public void testNoDuplicateOptionalFeatures() {
        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = guidelineGrantManager.createFeatureSetObject(
                FEATURE_SET_CODE_ASSISTED, GRANT_FEATURE_CODE_ASSISTED, OPTIONAL_FEATURE_STATUS_ACTIVE, GUIDELINE_FEATURE_CODE);

        guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

        featureSetObject = guidelineGrantManager.createFeatureSetObject(
                FEATURE_SET_CODE_ASSISTED, GRANT_FEATURE_CODE_ASSISTED, OPTIONAL_FEATURE_STATUS_ACTIVE, GUIDELINE_FEATURE_CODE);

        guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

        assertPayrollGrantFeatureSetsWithoutDuplicates(realmId, FEATURE_SET_CODE_ASSISTED, GRANT_FEATURE_CODE_ASSISTED, OPTIONAL_FEATURE_STATUS_ACTIVE);
    }


    @Test
    public void testPayrollGrantAttributeUpdate() {
        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        grantAttributesMap.put(GrantAttributeKey.RELEASE_NUMBER, "9");
        grantAttributesMap.put(GrantAttributeKey.FLAVOR, "bel");
        grantAttributesMap.put(GrantAttributeKey.VERSION_NUMBER, "2018");

        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = guidelineGrantManager.createFeatureSetObject(
                FEATURE_SET_CODE_ASSISTED, GRANT_FEATURE_CODE_ASSISTED, OPTIONAL_FEATURE_STATUS_ACTIVE, GUIDELINE_FEATURE_CODE);
        Grant grant = guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

        assertNotNull(grant);
        assertEquals("9", grant.getReleaseNumber());
        assertEquals("bel", grant.getFlavor());
        assertEquals("2018", grant.getVersionNumber());

        grantAttributesMap = new HashMap<>();
        grantAttributesMap.put(GrantAttributeKey.RELEASE_NUMBER, "15");
        grantAttributesMap.put(GrantAttributeKey.FLAVOR, "belacct");
        grantAttributesMap.put(GrantAttributeKey.VERSION_NUMBER, "2019");

        grant = guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

        assertNotNull(grant);
        assertEquals("15", grant.getReleaseNumber());
        assertEquals("belacct", grant.getFlavor());
        assertEquals("2019", grant.getVersionNumber());
    }

    @Test
    public void testPayrollGrantEntitlementInfoUpdate() {
        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();

        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();
        entitlementInfoMap.put(EntitlementInfoKey.SOURCE, "Payroll");
        entitlementInfoMap.put(EntitlementInfoKey.SUBSCRIPTION_NUMBER, "8000238");

        FeatureSetObject featureSetObject = guidelineGrantManager.createFeatureSetObject(
                FEATURE_SET_CODE_ASSISTED, GRANT_FEATURE_CODE_ASSISTED, OPTIONAL_FEATURE_STATUS_ACTIVE, GUIDELINE_FEATURE_CODE);
        Grant grant = guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);
        assertNotNull(grant);
        assertEquals("Payroll", getGrantEntitlementInfo(grant, EntitlementInfoKey.SOURCE));
        assertEquals("8000238", getGrantEntitlementInfo(grant, EntitlementInfoKey.SUBSCRIPTION_NUMBER));

        entitlementInfoMap = new HashMap<>();
        entitlementInfoMap.put(EntitlementInfoKey.SOURCE, "Payroll");
        entitlementInfoMap.put(EntitlementInfoKey.SUBSCRIPTION_NUMBER, "8000239");

        grant = guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);
        assertNotNull(grant);
        assertEquals("Payroll", getGrantEntitlementInfo(grant, EntitlementInfoKey.SOURCE));
        assertEquals("8000239", getGrantEntitlementInfo(grant, EntitlementInfoKey.SUBSCRIPTION_NUMBER));
    }

    @Test
    public void testDeactivatePayrollGrant() {

        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = guidelineGrantManager.createFeatureSetObject(FEATURE_SET_CODE_DIY,
                GRANT_FEATURE_CODE_DD, OPTIONAL_FEATURE_STATUS_ACTIVE, GUIDELINE_FEATURE_CODE);
        guidelineGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

        assertPayrollGrantGuidelineFeatureSet(realmId, FEATURE_SET_CODE_DIY, GRANT_FEATURE_CODE_DD, OPTIONAL_FEATURE_STATUS_ACTIVE);

        guidelineGrantManager.deactivatePayrollGrantFeatureSet(realmId);

        assertPayrollGrantGuidelineFeatureSet(realmId, FEATURE_SET_CODE_DIY, GRANT_FEATURE_CODE_DD, OPTIONAL_FEATURE_STATUS_INACTIVE);
    }

    private String createRealmWithPayrollGrant() {
        String realmId = createRealm();
        realmManager.addPayrollGrantToRealm(realmId);
        return realmId;
    }

    private String createRealm() {
        List<RealmIdPersonaIdPair> realmIdPersonaIdPairs = IUSClientWrapper.createRealm("1", REALM_NAME_PREFIX, REALM_EMAIL);

        if (realmIdPersonaIdPairs.isEmpty()) {
            throw new RuntimeException("Error in Realm creation");
        }

        RealmIdPersonaIdPair realmIdPersonaIdPair = realmIdPersonaIdPairs.get(0);
        return realmIdPersonaIdPair.getRealmId();
    }

    private String getGrantEntitlementInfo(Grant grant, EntitlementInfoKey entitlementInfoKey) {
        List<NameValuePair> nameValuePairList = grant.getEntitlementInfo();
        for(NameValuePair nameValuePair: nameValuePairList) {
            if(StringUtils.equalsIgnoreCase(entitlementInfoKey.name(), nameValuePair.getName())) {
                return nameValuePair.getValue();
            }
        }
        return null;
    }

    private void assertPayrollGrantGuidelineFeatureSet(String realmId, String featureSetCode, String grantFeatureCode, String optionalFeatureStatus) {
        Grant grant = realmManager.findPayrollGrant(realmId);

        FeatureSetObject featureSetObject = grant.getFeatureSetObj();
        assertNotNull(featureSetObject);

        assertEquals(featureSetCode, featureSetObject.getFeatureSetCode());

        List<GrantFeature> grantFeatures = featureSetObject.getFeatures();

        GrantFeature grantFeature = grantFeatures.get(0);

        assertEquals(grantFeatureCode, grantFeature.getCode());
        assertEquals("FEATURE", grantFeature.getType());

        List<OptionalFeature> optionalFeatures = featureSetObject.getOptionalFeatures();

        assertEquals(optionalFeatures.size(), 1);

        OptionalFeature optionalFeature = optionalFeatures.get(0);

        assertEquals("GUIDELINE_401K", optionalFeature.getCode());
        assertEquals(optionalFeatureStatus, optionalFeature.getStatus());
        assertEquals("PAID", optionalFeature.getServiceStatus());
    }

    private void assertPayrollGrantFeatureSetsWithoutDuplicates(String realmId, String featureSetCode, String grantFeatureCode, String optionalFeatureStatus) {
        Grant grant = realmManager.findPayrollGrant(realmId);

        FeatureSetObject featureSetObject = grant.getFeatureSetObj();
        assertNotNull(featureSetObject);

        assertEquals(featureSetCode, featureSetObject.getFeatureSetCode());

        List<GrantFeature> grantFeatures = featureSetObject.getFeatures();

        GrantFeature grantFeature = grantFeatures.get(0);

        assertEquals(grantFeatureCode, grantFeature.getCode());
        assertEquals("FEATURE", grantFeature.getType());

        List<OptionalFeature> optionalFeatures = featureSetObject.getOptionalFeatures();

        assertEquals(optionalFeatures.size(), 1);

        OptionalFeature optionalFeatureGuideline = null;

        for (OptionalFeature optionalFeature: optionalFeatures) {
            if(optionalFeature.getCode().equals("GUIDELINE_401K")) {
                optionalFeatureGuideline = optionalFeature;
            }
        }

        assertEquals("GUIDELINE_401K", optionalFeatureGuideline.getCode());
        assertEquals(optionalFeatureStatus, optionalFeatureGuideline.getStatus());
        assertEquals("PAID", optionalFeatureGuideline.getServiceStatus());
    }

}
