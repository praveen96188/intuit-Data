package com.intuit.sbd.payroll.psp.gateways.iam.identity;

import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import org.springframework.stereotype.Component;

@Component
public class IdentityConfigManager {

    public boolean isGrant2Enabled() {
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.GRANT2_ENABLED, true);
    }

    public boolean isIDLMEnabled() {
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDLM_ENABLED, true);
    }

    public boolean isIDLMEnabledForInvitations() {
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDLM_ENABLED_FOR_INVITATIONS, true);
    }
}

