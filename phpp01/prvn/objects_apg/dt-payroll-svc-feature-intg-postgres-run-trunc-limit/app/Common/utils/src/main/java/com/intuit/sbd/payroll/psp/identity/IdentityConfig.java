package com.intuit.sbd.payroll.psp.identity;

import com.intuit.identity.authn.offline.sdk.client.AuthNClient;
import com.intuit.identity.authn.offline.sdk.config.AuthNClientConfig;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.*;


@Configuration
@Slf4j
public class IdentityConfig {

    @Bean
    public boolean setIdpsPropertiesInAuthNClientConfig(AuthNClientConfig authNClientConfig) {
        authNClientConfig.setAppId(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_appid"));
        authNClientConfig.setAppSecret(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_appsecret"));
        authNClientConfig.setResilienceDisabled(true);
        log.info("AuthN: AuthNClientConfig built successfully");
        return true;
    }


}
