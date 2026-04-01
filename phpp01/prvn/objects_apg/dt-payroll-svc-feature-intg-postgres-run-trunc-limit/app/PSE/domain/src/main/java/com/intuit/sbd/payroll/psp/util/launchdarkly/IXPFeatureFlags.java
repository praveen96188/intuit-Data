package com.intuit.sbd.payroll.psp.util.launchdarkly;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intuit.identity.exptplatform.assignment.entities.DefaultEntityIdImpl;
import com.intuit.identity.exptplatform.assignment.entities.EntityID;
import com.intuit.identity.exptplatform.enums.CacheElementTypeEnum;
import com.intuit.identity.exptplatform.enums.ConfigEnvironmentEnum;
import com.intuit.identity.exptplatform.featureflag.FeatureFlagClient;
import com.intuit.identity.exptplatform.featureflag.FeatureFlagParams;
import com.intuit.identity.exptplatform.sdk.client.IXPClientFactory;
import com.intuit.identity.exptplatform.sdk.client.IXPConfig;
import com.intuit.identity.exptplatform.sdk.exceptions.IXPClientInitializationException;
import com.intuit.identity.exptplatform.sdk.filters.CacheScope;
import com.intuit.identity.exptplatform.sdk.tracking.ClientInfo;
import com.intuit.sbd.payroll.psp.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class IXPFeatureFlags extends FeatureFlags{
    private static final Logger logger = LoggerFactory.getLogger(IXPFeatureFlags.class);
    private static Class<? extends IXPFeatureFlags> instanceClass = IXPFeatureFlags.class;

    private static final String BU = "SBSEG";
    private static final String SUB_ENVIRONMENT = getSubEnvironmentName();
    private static final String APPLICATION = "QBDT-PSP";
    private FeatureFlagClient ffClient;
    private FeatureFlagParams featureFlagParams;

    private static FeatureFlags instance;

    public static FeatureFlags getInstance() {
        try {
            instance =  instanceClass.newInstance();
        } catch (Throwable t) {
            logger.error("IXPFeatureFlags could not be constructed.", t);
            throw new IXPClientInitializationException("IXPFeatureFlags could not be constructed.", t);
        }
        return instance;
    }

    protected IXPFeatureFlags(){
        CacheScope scope = new CacheScope();
        // CacheScope is providing direction to SDK on what metadata to pull in at client initialization time.
        // Specify whether we want Feature flags or Experiments or both.
        // If no cachescope is specified, SDK will pull in both Experiments and Feature Flag.
        scope.setCacheElementType(CacheElementTypeEnum.ALL);

        // Restrict Feature flags for initialization based on Business unit and sub-environment of interest. Country
        // and application can be specified as well to be even more restrictive.
        scope.addBusinessUnit(BU);
        scope.addApplicationName(APPLICATION);
        scope.setFeatureFlagSubEnvironments(Arrays.asList(SUB_ENVIRONMENT));

        // construct IXPConfig instance and initialize FeatureFlagClient. Once Initialization is successful we
        // are ready to evaluate feature flags.
        ConfigEnvironmentEnum configEnvironmentEnum = (Application.isProdEnvironment() || Application.isStgEnvironment()) ? ConfigEnvironmentEnum.PROD : ConfigEnvironmentEnum.PRE_PROD;
        // This is the minimum config required to initialize SDK with FF
        IXPConfig config = IXPConfig.builder()
                .environment(configEnvironmentEnum)
                .clientInfo(new ClientInfo("PSP", "1.0"))
                .cacheScope(scope)
                .build();

        ffClient = IXPClientFactory.getFeatureFlagClient();
        ffClient.init(config, new FeatureFlagCacheStateChangeListener());

        // FeatureFlagParams is used to pass user context required to satisfy Segmentation Rules and credentials
        // as required to call DataProviders for getting remote attributes. In this, this is
        // Feature flag without any Segmentation rules so we are passing no context and credentials
        featureFlagParams = new FeatureFlagParams.FeatureFlagParamsBuilder()
                .context(Collections.emptyMap())
                .build();
    }

    public static void setInstanceClass(Class<? extends IXPFeatureFlags> instanceClass) {
        IXPFeatureFlags.instanceClass = instanceClass;
    }

    /**
     * Test utility method only
     */
    public static void reset() {
        instance = null;
        setInstanceClass(IXPFeatureFlags.class);
    }

    public static String getSubEnvironmentName() {
        String envName = Application.getEnvironmentName();
        if(envName.toUpperCase().contains("PROD")) {
            return "PRD";
        } else if(envName.toUpperCase().contains("PDS")) {
            return "E2E";
        } else if(envName.toUpperCase().contains("DS2")) {
            return "DS2";
        } else if(envName.toUpperCase().contains("SYS")) {
            return "QAL";
        } else if(envName.toUpperCase().contains("LOCAL")) {
            return "LOCAL";
        } else if(envName.toUpperCase().contains("DEV")) {
            return "DEV";
        } else if(envName.toUpperCase().contains("DS1")) {
            return "DS1";
        } else if(envName.toUpperCase().contains("STG")) {
            return "STG";
        } else if(envName.toUpperCase().contains("BUILD")) {
            return "BUILD";
        } else if(envName.toUpperCase().contains("PRF")){
            return "PERF";
        }
        throw new IXPClientInitializationException("SubEnvironment not found" + envName);
    }

    @Override
    public boolean booleanValue(Key featureKey, boolean defaultValue) {
        return booleanValue(featureKey, defaultValue, null);
    }

    @Override
    public boolean booleanValue(Key featureKey, boolean defaultValue, String entityIdString) {
        try {
            EntityID entityID = convertIdtoEntityId(entityIdString);
            return ffClient.evaluateBooleanVariation(entityID, SUB_ENVIRONMENT, featureKey.name(), defaultValue, featureFlagParams);
        } catch (Exception exception) {
            logger.error("IXP: Error getting the flag value for={} ", featureKey, exception);
            return defaultValue;
        }
    }

    @Override
    public int intValue(Key featureKey, int defaultValue) {
        return intValue(featureKey, defaultValue, null);
    }

    @Override
    public int intValue(Key featureKey, int defaultValue, String entityIdString) {
        try {
            EntityID entityID = convertIdtoEntityId(entityIdString);
            return ffClient.evaluateIntVariation(entityID, SUB_ENVIRONMENT, featureKey.name(), defaultValue, featureFlagParams);
        } catch (Exception exception) {
            logger.error("IXP: Error getting the flag value for={} ", featureKey, exception);
            return defaultValue;
        }
    }

    @Override
    public double doubleValue(Key featureKey, double defaultValue) {
        return doubleValue(featureKey, defaultValue, null);
    }

    @Override
    public double doubleValue(Key featureKey, double defaultValue, String entityIdString) {
        try {
            EntityID entityID = convertIdtoEntityId(entityIdString);
            return ffClient.evaluateDoubleVariation(entityID, SUB_ENVIRONMENT, featureKey.name(), defaultValue, featureFlagParams);
        } catch (Exception exception) {
            logger.error("IXP: Error getting the flag value for={} ", featureKey, exception);
            return defaultValue;
        }
    }

    @Override
    public String stringValue(Key featureKey, String defaultValue){
        return stringValue(featureKey, defaultValue, null);
    }

    @Override
    public String stringValue(Key featureKey, String defaultValue, String entityIdString) {
        try {
            EntityID entityID = convertIdtoEntityId(entityIdString);
            return ffClient.evaluateStringVariation(entityID, SUB_ENVIRONMENT, featureKey.name(), defaultValue, featureFlagParams);
        } catch (Exception exception) {
            logger.error("IXP: Error getting the flag value for={} ", featureKey, exception);
            return defaultValue;
        }
    }

    @Override
    public JsonNode jsonValue(Key featureKey, JsonNode defaultValue) {
        return jsonValue(featureKey, defaultValue, null);
    }

    @Override
    public JsonNode jsonValue(Key featureKey, JsonNode defaultValue, String entityIdString) {
        try {
            EntityID entityID = convertIdtoEntityId(entityIdString);
            return ffClient.evaluateJsonVariation(entityID, SUB_ENVIRONMENT, featureKey.name(), defaultValue, featureFlagParams);
        } catch (Exception exception) {
            logger.error("IXP: Error getting the flag value for={} ", featureKey, exception);
            return defaultValue;
        }
    }

    @Override
    public boolean isReady() {
        return ffClient.isOnline() && ffClient.isInitialized();
    }

    private EntityID convertIdtoEntityId(String entityIdString){
        if (!ffClient.isOnline()) {
            throw new RuntimeException("Error initializing Feature Flag client.");
        }
        String genericFFId = Objects.isNull(entityIdString) ? UUID.randomUUID().toString() : entityIdString;
        // Create a EntityId. The EntityID is a container for the different AssignmentIDs.
        // During Feature Flag evaluation, IXP will use the right ID for assignment based on the Assignment set in the experiment.
        return DefaultEntityIdImpl.builder()
                .customIdNameAndValue("GENERIC_FF_ID", genericFFId)
                .ns("PSP.isEnabled")
                .build();
    }
}
