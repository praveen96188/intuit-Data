package com.intuit.sbd.payroll.psp;


import com.intuit.sbd.payroll.psp.iam.VerifyAppId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DisAdapterHeaderUtilsConfig {

    @Value("${PspUserAuthZ.appIdList}")
    private String appListString;

    @Bean("DisAdapterVerifyAppIdBean")
    public VerifyAppId getVerifyAppId(){
        return new VerifyAppId(appListString);
    }
}
