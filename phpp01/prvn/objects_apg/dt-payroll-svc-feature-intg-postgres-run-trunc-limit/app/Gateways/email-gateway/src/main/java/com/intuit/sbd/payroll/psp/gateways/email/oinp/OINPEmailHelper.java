package com.intuit.sbd.payroll.psp.gateways.email.oinp;

import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import org.springframework.stereotype.Component;

@Component
public class OINPEmailHelper {

    /**
     * Method to check if a template ID is OINP enabled or not
     *
     * @param templateId
     * @return
     */
    public Boolean isTemplateOINPEnabled(String templateId) {

        String templatesEnabledForOINP = FeatureFlags.get().stringValue(FeatureFlags.Key.OINP_ENABLED_TEMPLATES, "Template ID");

        String commaSepTemplateId = "," + templateId + ",";
        String commaSepOinpEnabledTemplates = "," + templatesEnabledForOINP + ",";

        return commaSepOinpEnabledTemplates.contains("ALL") || commaSepOinpEnabledTemplates.contains(commaSepTemplateId);
    }

}

