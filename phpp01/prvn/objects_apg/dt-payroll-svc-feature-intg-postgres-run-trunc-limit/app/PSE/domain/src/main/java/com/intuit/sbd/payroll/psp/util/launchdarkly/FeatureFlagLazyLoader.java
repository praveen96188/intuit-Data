package com.intuit.sbd.payroll.psp.util.launchdarkly;

import com.intuit.sbd.payroll.psp.util.MoneyMovementControlUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags.Key;

/**
 * FeatureFlagLazyLoader class is designed to load the feature flags on a scheduled interval rather than making instance calls
 * which helps in avoiding lot of calls to Intuit Experimentation Platform (IXP)
 */
@Slf4j
public class FeatureFlagLazyLoader {

    private static final FeatureFlagLazyLoader INSTANCE = new FeatureFlagLazyLoader();

    private Map<Key, Object> featureFlagValues = new HashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private FeatureFlagLazyLoader() {
        log.info("Scheduling FeatureFlag Lazy Loader");
        /*
        Adding initialDelay of 5 minutes, because FeatureFlag is dependent on Configuration which takes some time to load.
        * */
        this.scheduler.scheduleAtFixedRate(this.getRunnable(), 5, 5, TimeUnit.MINUTES);
        initializeDefaultValues();
        log.info("Successfully scheduled FeatureFlag Lazy Loader");
    }

    public static FeatureFlagLazyLoader getInstance() {
        return INSTANCE;
    }

    public <T> T getFeatureFlagValue(Key key) {
        return (T) featureFlagValues.get(key);
    }

    private Runnable getRunnable() {
        return new Runnable() {
            public void run() {
                try {
                    lazyLoadFeatureFlags();
                    log.info("Successfully loaded lazy feature flags. FeatureFlagValues={}", featureFlagValues);
                } catch (Exception var2) {
                    log.warn("Unable to refresh the FeatureFlags");
                }
            }
        };
    }

    private void initializeDefaultValues() {
        featureFlagValues.put(Key.ENABLE_MULTI_TENANCY, false);
        featureFlagValues.put(Key.PSP_SMS_ADDRESS_FIX_ENABLE, true);
        featureFlagValues.put(Key.ENABLE_FINANCIAL_TRANSACTION_LIMIT, true);
        featureFlagValues.put(Key.FINANCIAL_TRANSACTION_LIMIT, (double) MoneyMovementControlUtil.DEFAULT_FINANCIAL_TRANSACTION_LIMIT);
        featureFlagValues.put(Key.ENABLE_QUERY_COMMENT, false);
        featureFlagValues.put(Key.FILTER_ENABLED_REQUEST_OPERATIONS, "");
        featureFlagValues.put(Key.FILTER_ENABLED_REQUEST_TYPES, "");
        featureFlagValues.put(Key.FILTER_DISABLED_REQUEST_OPERATIONS, "");
        featureFlagValues.put(Key.ENABLE_HIBERNATE_COMPANY_FILTER, false);
        featureFlagValues.put(Key.ENABLE_HIBERNATE_DATE_FILTER, false);
        featureFlagValues.put(Key.ENABLE_HIBERNATE_LICENSE_NUMBER_FILTER, false);
        featureFlagValues.put(Key.IS_FILTER_VALIDATOR_ENABLED, false);
        featureFlagValues.put(Key.JPA_PROCEDURE_LIST, "");
        featureFlagValues.put(Key.ENABLE_TENANT_ID_ANNOTATION, false);
        featureFlagValues.put(Key.ENABLE_GUIDELINE_PROD_FIX, true);
        featureFlagValues.put(Key.ENABLE_VMP_MULTI_COMPANY_FILTER,false);
        featureFlagValues.put(Key.ENABLE_BATCHING_ASPECT, false);
        featureFlagValues.put(Key.ENABLE_CRITERIA_EAGER_LOAD, true);
        featureFlagValues.put(Key.PARALLEL_ENV_JSS_SCHEDULED_JOB_LIST, "");
        featureFlagValues.put(Key.MULTIPLE_COMPANY_CONTEXT_ALLOWED_OPERATIONS, "");
        featureFlagValues.put(Key.ENABLE_EAGER_LOAD_QUERIES, true);
        featureFlagValues.put(Key.ENABLE_CRITERIA_EAGER_LOAD_NESTED_COLLECTION, true);
        featureFlagValues.put(Key.ENABLE_EAGER_LOAD_QUERIES_METHODS, "");
        featureFlagValues.put(Key.ENABLE_EAGER_LOAD_BUILD_FROM_ALIAS_MAP_FIX, false);
    }

    public void lazyLoadFeatureFlags() {
        featureFlagValues.put(Key.ENABLE_MULTI_TENANCY, FeatureFlags.get().booleanValue(Key.ENABLE_MULTI_TENANCY, false));
        featureFlagValues.put(Key.PSP_SMS_ADDRESS_FIX_ENABLE, FeatureFlags.get().booleanValue(Key.PSP_SMS_ADDRESS_FIX_ENABLE, false));
        featureFlagValues.put(Key.ENABLE_FINANCIAL_TRANSACTION_LIMIT, FeatureFlags.get().booleanValue(Key.ENABLE_FINANCIAL_TRANSACTION_LIMIT, true));
        featureFlagValues.put(Key.FINANCIAL_TRANSACTION_LIMIT, FeatureFlags.get().doubleValue(Key.FINANCIAL_TRANSACTION_LIMIT, MoneyMovementControlUtil.DEFAULT_FINANCIAL_TRANSACTION_LIMIT));
        featureFlagValues.put(Key.ENABLE_QUERY_COMMENT, FeatureFlags.get().booleanValue(Key.ENABLE_QUERY_COMMENT, false));
        featureFlagValues.put(Key.FILTER_ENABLED_REQUEST_OPERATIONS, FeatureFlags.get().stringValue(Key.FILTER_ENABLED_REQUEST_OPERATIONS, ""));
        featureFlagValues.put(Key.FILTER_ENABLED_REQUEST_TYPES, FeatureFlags.get().stringValue(Key.FILTER_ENABLED_REQUEST_TYPES, ""));
        featureFlagValues.put(Key.FILTER_DISABLED_REQUEST_OPERATIONS, FeatureFlags.get().stringValue(Key.FILTER_DISABLED_REQUEST_OPERATIONS, ""));
        featureFlagValues.put(Key.ENABLE_HIBERNATE_COMPANY_FILTER, FeatureFlags.get().booleanValue(Key.ENABLE_HIBERNATE_COMPANY_FILTER, false));
        featureFlagValues.put(Key.ENABLE_HIBERNATE_DATE_FILTER, FeatureFlags.get().booleanValue(Key.ENABLE_HIBERNATE_DATE_FILTER, false));
        featureFlagValues.put(Key.ENABLE_HIBERNATE_LICENSE_NUMBER_FILTER, FeatureFlags.get().booleanValue(Key.ENABLE_HIBERNATE_LICENSE_NUMBER_FILTER, false));
        featureFlagValues.put(Key.IS_FILTER_VALIDATOR_ENABLED, FeatureFlags.get().booleanValue(Key.IS_FILTER_VALIDATOR_ENABLED, false));
        featureFlagValues.put(Key.JPA_PROCEDURE_LIST, FeatureFlags.get().stringValue(Key.JPA_PROCEDURE_LIST, ""));
        featureFlagValues.put(Key.ENABLE_TENANT_ID_ANNOTATION, FeatureFlags.get().booleanValue(Key.ENABLE_TENANT_ID_ANNOTATION,false));
        featureFlagValues.put(Key.ENABLE_GUIDELINE_PROD_FIX, FeatureFlags.get().booleanValue(Key.ENABLE_GUIDELINE_PROD_FIX,true));
        featureFlagValues.put(Key.ENABLE_VMP_MULTI_COMPANY_FILTER, FeatureFlags.get().booleanValue(Key.ENABLE_VMP_MULTI_COMPANY_FILTER,false));
        featureFlagValues.put(Key.ENABLE_BATCHING_ASPECT, FeatureFlags.get().booleanValue(Key.ENABLE_BATCHING_ASPECT,false));
        featureFlagValues.put(Key.ENABLE_CRITERIA_EAGER_LOAD, FeatureFlags.get().booleanValue(Key.ENABLE_CRITERIA_EAGER_LOAD,true));
        featureFlagValues.put(Key.PARALLEL_ENV_JSS_SCHEDULED_JOB_LIST, FeatureFlags.get().stringValue(Key.PARALLEL_ENV_JSS_SCHEDULED_JOB_LIST,""));
        featureFlagValues.put(Key.MULTIPLE_COMPANY_CONTEXT_ALLOWED_OPERATIONS, FeatureFlags.get().stringValue(Key.MULTIPLE_COMPANY_CONTEXT_ALLOWED_OPERATIONS, ""));
        featureFlagValues.put(Key.ENABLE_EAGER_LOAD_QUERIES, FeatureFlags.get().booleanValue(Key.ENABLE_EAGER_LOAD_QUERIES,true));
        featureFlagValues.put(Key.ENABLE_CRITERIA_EAGER_LOAD_NESTED_COLLECTION, FeatureFlags.get().booleanValue(Key.ENABLE_CRITERIA_EAGER_LOAD_NESTED_COLLECTION,true));
        featureFlagValues.put(Key.ENABLE_EAGER_LOAD_QUERIES_METHODS, FeatureFlags.get().stringValue(Key.ENABLE_EAGER_LOAD_QUERIES_METHODS,""));
        featureFlagValues.put(Key.ENABLE_EAGER_LOAD_BUILD_FROM_ALIAS_MAP_FIX, FeatureFlags.get().booleanValue(Key.ENABLE_EAGER_LOAD_BUILD_FROM_ALIAS_MAP_FIX,false));
    }

}
