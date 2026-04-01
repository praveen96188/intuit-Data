package com.intuit.sbd.payroll.psp.util.launchdarkly;

import com.intuit.identity.exptplatform.sdk.client.CacheStateChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureFlagCacheStateChangeListener implements CacheStateChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureFlagCacheStateChangeListener.class);

    @Override
    /**
     * Informs that the caches have been initialized and post-initialization actions can happen. Typical
     * action is to allow assignment calls to be serviced by the SDK and/or if you want to print out
     * active experiments, change the implementation to do it.
     */
    public void onCacheInitialize() {
        LOGGER.info("Caches initialized");
    }

    @Override
    /**
     * Informs there was a cache initialization failure and the SDK is not
     * ready to take assignment calls.
     */
    public void onCacheInitializationFailure(Exception ex) {
        LOGGER.error("Caches failed to initialize: ", ex);
        throw new RuntimeException("Cache Initialization Failure", ex);
    }


    @Override
    /**
     * Informs there was a cache refresh failure. Repeated failures on the SDK might require
     * special handling on the app.
     */
    public void onCacheRefreshFailure(Exception ex) {
        LOGGER.error("FeatureFlag Caches Refresh failure: ", ex);
    }

    @Override
    /**
     * Informs caches were successfully refreshed. Print out the updated experiments or do some
     * validations - Knock yourself out.
     */
    public void onCacheRefresh() {
    }

}

