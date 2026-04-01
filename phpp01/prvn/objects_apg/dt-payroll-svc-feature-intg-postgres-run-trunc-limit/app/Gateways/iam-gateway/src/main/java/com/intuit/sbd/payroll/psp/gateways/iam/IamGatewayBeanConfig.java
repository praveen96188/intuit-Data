package com.intuit.sbd.payroll.psp.gateways.iam;

import com.intuit.client.ius.IUSRestTransport;
import com.intuit.client.ius.IUSRestTransportImpl;
import com.intuit.iam.utilities.IamConfiguration;
import com.intuit.platform.integration.ius.common.types.Invitations;
import com.intuit.sbg.psp.spring.YamlPropertySourceFactory;
import com.intuit.sbg.psp.webserviceclient.ratelimiter.RateLimiterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.gateways.iam"})
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:iam-gateway.yml")
public class IamGatewayBeanConfig {

    private static final String RATE_LIMITER_NAME = "WorkforceInvitation";
    private int LIMIT_FOR_PERIOD = 10;
    private static final int LIMIT_REFRESH_PERIOD = 1;
    private static final int TIMEOUT_DURATION = 10;

    @Bean(name="invitationRateLimiterService")
    public RateLimiterService<Invitations> invitationRateLimiterService() {
        return new RateLimiterService<>(RATE_LIMITER_NAME, LIMIT_FOR_PERIOD, LIMIT_REFRESH_PERIOD, TIMEOUT_DURATION);
    }

    @Bean(name="iusRestTransport")
    public IUSRestTransport iusRestTransport() {
        IUSAppCallback appCallback = new IUSAppCallback();
        IamConfiguration.setIUSRestUri(appCallback.getIUSRestUri());
        IamConfiguration.setIUSConnectionTimeOut(appCallback.getIUSConnectionTimeOut());
        IamConfiguration.setIUSReadTimeOut(appCallback.getIUSReadTimeOut());
        IamConfiguration.setLogger(appCallback.getLogger());
        return new IUSRestTransportImpl(appCallback);
    }
}
