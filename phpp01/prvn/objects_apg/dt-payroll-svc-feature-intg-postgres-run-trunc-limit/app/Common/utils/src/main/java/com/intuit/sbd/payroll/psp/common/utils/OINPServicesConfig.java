package com.intuit.sbd.payroll.psp.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.intuit.sbd.payroll.psp.emailsender.EmailConfig;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.spring.YamlPropertySourceFactory;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.util.Map;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.common.utils"})
@Import(value = {EmailConfig.class})
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:utils.yml")
@ConfigurationProperties(prefix = "email-client.oinp-services")
public class OINPServicesConfig {

    private String serviceName;

    private Map<String,Map<String,String>> templates;

    private final Logger logger = LoggerFactory.getLogger(OINPServicesConfig.class);

    @Bean("SourceServiceName")
    public String getSourceServiceName()
    {
        return serviceName;
    }

    public String getTemplateName(String templateId) {
        String templateNameOverride = getTemplateDetailOverride(templateId, "template-name");
        if(!templateNameOverride.isEmpty())
        {
            logger.info("OINP: Overriding templateName to: {}", templateNameOverride );
            return templateNameOverride;
        }
        return getTemplates().get(templateId).get("template-name");
    }

    public String getTemplateObjectType(String templateId) {
        String templateObjectTypeOverride = getTemplateDetailOverride(templateId, "object-type");
        if(!templateObjectTypeOverride.isEmpty())
        {
            logger.info("OINP: Overriding template object type to: {}", templateObjectTypeOverride);
            return templateObjectTypeOverride;
        }
        return getTemplates().get(templateId).get("object-type");
    }

    public String getTemplateDetailOverride(String templateId, String field)
    {
        String overrideConfig = "";
        JsonNode templatesEnabledForOINP = FeatureFlags.get().jsonValue(FeatureFlags.Key.OINP_TEMPLATE_DETAILS_OVERRIDE, (JsonNode) null);
        if (templatesEnabledForOINP != null && templatesEnabledForOINP.has(templateId) && templatesEnabledForOINP.get(templateId).has(field)) {
            overrideConfig = templatesEnabledForOINP.get(templateId).get(field).textValue();
        }
        return overrideConfig;
    }
}


