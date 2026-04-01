package com.intuit.ems.payroll.psp.gateways.iam;

import com.intuit.platform.integration.ius.common.types.*;
import com.intuit.sbd.payroll.psp.gateways.iam.IUSClientWrapper;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager.EntitlementInfoKey;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager.GrantAttributeKey;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.TRONGrantManager;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TRONGrantManagerTests {

    private RealmManager realmManager;
    private TRONGrantManager tronGrantManager;

    public TRONGrantManagerTests() {
        realmManager = new RealmManager();
        tronGrantManager = new TRONGrantManager();
    }

    @Test
    public void testPayrollGrantAddWithTRONFeatureSet() {
        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = tronGrantManager.createFeatureSetObject("DIY", "1099581", "ACTIVE", RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT);
        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

        assertPayrollGrantTRONFeatureSet(realmId, "DIY", "1099581", "ACTIVE");
    }

    @Test
    public void testPayrollGrantUpdateWithTRONFeatureSet() {
        String realmId = createRealmWithPayrollGrant();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = tronGrantManager.createFeatureSetObject("Assisted", "1099574", "ACTIVE", RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT);
        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

        assertPayrollGrantTRONFeatureSet(realmId, "Assisted", "1099574", "ACTIVE");
    }

    @Test
    public void testPayrollGrantAddWithEWSFeatureSet() {
        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = tronGrantManager.createFeatureSetObject("DIY", "1099592", "ACTIVE", RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT_EWS);
        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

        assertPayrollGrantEWSFeatureSet(realmId, "DIY", "1099592", "ACTIVE");
    }

    @Test
    public void testPayrollGrantUpdateWithEWSFeatureSet() {
        String realmId = createRealmWithPayrollGrant();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = tronGrantManager.createFeatureSetObject("Assisted", "1099563", "ACTIVE", RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT_EWS);
        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

        assertPayrollGrantEWSFeatureSet(realmId, "Assisted", "1099563", "ACTIVE");
    }

    @Test
    public void testNoChangeInPayrollTRONGrant() {
        //String realmId = "9130352897364216";
        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = tronGrantManager.createFeatureSetObject("Assisted", "1101314", "ACTIVE", RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT);
        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

        assertPayrollGrantTRONFeatureSet(realmId, "Assisted", "1101314", "ACTIVE");
    }

    @Test
    public void testNoChangeInPayrollEWSGrant() {
        //String realmId = "9130352897364206";
        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = tronGrantManager.createFeatureSetObject("Assisted", "1101310", "ACTIVE", RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT_EWS);
        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

        assertPayrollGrantEWSFeatureSet(realmId, "Assisted", "1101310", "ACTIVE");
    }

    @Test
    public void testNoDuplicateOptionalFeatures() {
        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = tronGrantManager.createFeatureSetObject("Assisted", "1101314", "ACTIVE", RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT);

        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

        featureSetObject = tronGrantManager.createFeatureSetObject("Assisted", "1101314", "ACTIVE", RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT_EWS);

        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

        featureSetObject = tronGrantManager.createFeatureSetObject("Assisted", "1101314", "ACTIVE", RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT);

        tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap, featureSetObject);

        assertPayrollGrantFeatureSetsWithoutDuplicates(realmId, "Assisted", "1101314", "ACTIVE");
    }


    @Test
    public void testPayrollGrantAttributeUpdate() {
        String realmId = createRealm();

        Map<GrantAttributeKey, String> grantAttributesMap = new HashMap<>();
        grantAttributesMap.put(GrantAttributeKey.RELEASE_NUMBER, "9");
        grantAttributesMap.put(GrantAttributeKey.FLAVOR, "bel");
        grantAttributesMap.put(GrantAttributeKey.VERSION_NUMBER, "2018");

        Map<EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        FeatureSetObject featureSetObject = tronGrantManager.createFeatureSetObject("Assisted", "1101314", "ACTIVE", RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT);
        Grant grant = tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

        assertNotNull(grant);
        assertEquals("9", grant.getReleaseNumber());
        assertEquals("bel", grant.getFlavor());
        assertEquals("2018", grant.getVersionNumber());

        grantAttributesMap = new HashMap<>();
        grantAttributesMap.put(GrantAttributeKey.RELEASE_NUMBER, "15");
        grantAttributesMap.put(GrantAttributeKey.FLAVOR, "belacct");
        grantAttributesMap.put(GrantAttributeKey.VERSION_NUMBER, "2019");

        grant = tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);

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

        FeatureSetObject featureSetObject = tronGrantManager.createFeatureSetObject("Assisted", "1101314", "ACTIVE", RealmManager.OptionalFeatureCode.DIRECT_DEPOSIT);
        Grant grant = tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);
        assertNotNull(grant);
        assertEquals("Payroll", getGrantEntitlementInfo(grant, EntitlementInfoKey.SOURCE));
        assertEquals("8000238", getGrantEntitlementInfo(grant, EntitlementInfoKey.SUBSCRIPTION_NUMBER));

        entitlementInfoMap = new HashMap<>();
        entitlementInfoMap.put(EntitlementInfoKey.SOURCE, "Payroll");
        entitlementInfoMap.put(EntitlementInfoKey.SUBSCRIPTION_NUMBER, "8000239");

        grant =tronGrantManager.addOrUpdatePayrollGrantWithFeatureSet(realmId, grantAttributesMap, entitlementInfoMap,featureSetObject);
        assertNotNull(grant);
        assertEquals("Payroll", getGrantEntitlementInfo(grant, EntitlementInfoKey.SOURCE));
        assertEquals("8000239", getGrantEntitlementInfo(grant, EntitlementInfoKey.SUBSCRIPTION_NUMBER));
    }

    @Test
    public void testDeactivatePayrollGrant() {
        String realmId = "9130352888493416";

        tronGrantManager.deactivatePayrollGrantFeatureSet(realmId);

        assertPayrollGrantTRONFeatureSet(realmId, "Assisted", "1101314", "INACTIVE");
    }

    private String createRealmWithPayrollGrant() {
        String realmId = createRealm();
        realmManager.addPayrollGrantToRealm(realmId);
        return realmId;
    }

    private String createRealm() {
        List<RealmIdPersonaIdPair> realmIdPersonaIdPairs = IUSClientWrapper.createRealm("1", "TRON", "qbdttrontest+231020001@gmail.com");

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

    private void assertPayrollGrantTRONFeatureSet(String realmId, String featureSetCode, String grantFeatureCode, String optionalFeatureStatus) {
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

        assertEquals("DIRECT_DEPOSIT", optionalFeature.getCode());
        assertEquals(optionalFeatureStatus, optionalFeature.getStatus());
        assertEquals("PAID", optionalFeature.getServiceStatus());
    }

    private void assertPayrollGrantEWSFeatureSet(String realmId, String featureSetCode, String grantFeatureCode, String optionalFeatureStatus) {
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

        assertEquals("DIRECT_DEPOSIT_EWS", optionalFeature.getCode());
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

        assertEquals(optionalFeatures.size(), 2);

        OptionalFeature optionalFeatureEWS = null;
        OptionalFeature optionalFeatureTRON = null;

        for (OptionalFeature optionalFeature: optionalFeatures) {
            if(optionalFeature.getCode().equals("DIRECT_DEPOSIT_EWS")) {
                optionalFeatureEWS = optionalFeature;
            } else if(optionalFeature.getCode().equals("DIRECT_DEPOSIT")) {
                optionalFeatureTRON = optionalFeature;
            }
        }

        assertEquals("DIRECT_DEPOSIT", optionalFeatureTRON.getCode());
        assertEquals(optionalFeatureStatus, optionalFeatureTRON.getStatus());
        assertEquals("PAID", optionalFeatureTRON.getServiceStatus());
        assertEquals("DIRECT_DEPOSIT_EWS", optionalFeatureEWS.getCode());
        assertEquals(optionalFeatureStatus, optionalFeatureEWS.getStatus());
        assertEquals("PAID", optionalFeatureEWS.getServiceStatus());
    }

}
