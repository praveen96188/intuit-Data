package com.intuit.sbd.payroll.psp.util.launchdarkly;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class FeatureFlagUtil {

    public static Set<String> getFeatureFlagStringSet(FeatureFlags.Key key) {
        String value = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(key);
        if (StringUtils.isEmpty(value)) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(StringUtils.stripAll(value.split(","))));
    }

    public static String getValueFromFeatureFlagMap(FeatureFlags.Key featureFlagKey, String masterKey, String valueKey) {
        Map<String,Map<String, String>> keyValueMap = getFeatureFlagKeyValue(featureFlagKey);
        if (!keyValueMap.containsKey(masterKey)) {
            return null;
        }
        Map<String, String> paramValueMap = keyValueMap.get(masterKey);
        if (!paramValueMap.containsKey(valueKey)) {
            return null;
        }
        return paramValueMap.get(valueKey);
    }

    public static Map<String, Map<String, String>> getFeatureFlagKeyValue(FeatureFlags.Key featureFlagKey) {
        String featureFlagValue = FeatureFlags.get().stringValue(featureFlagKey, "");
        if (StringUtils.isEmpty(featureFlagValue)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, String>> keyValuesMap = new HashMap<>();
        String[] keyValuesPairs = featureFlagValue.split(";");
        for (String keyValuesPair : keyValuesPairs) {
            String[] keyValues = keyValuesPair.split("=");
            String key = keyValues[0].trim();
            String values = keyValues.length > 1 ? keyValues[1].trim() : null;
            if(StringUtils.isEmpty(values)) {
                continue;
            }
            Map<String, String> valueMap = new HashMap<>();
            String[] valueArray = values.split(",");
            for (String value : valueArray) {
                String[] valuePair = value.split(":");
                String valueKey = valuePair[0].trim();
                String valueValue = valuePair.length > 1 ? valuePair[1].trim() : null;
                if(StringUtils.isEmpty(valueValue)) {
                    continue;
                }
                valueMap.put(valueKey, valueValue);
            }
            keyValuesMap.put(key, valueMap);
        }
        return keyValuesMap;
    }
}
