package com.intuit.sbd.payroll.psp.gateways.salestax;

import com.intuit.sbg.psp.salestaxservices.SalesTaxServiceClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Import(SalesTaxServiceClientConfig.class)
@EnableAsync
@ComponentScan(
        basePackages = {"com.intuit.sbd.payroll.psp.gateways.salestax",
        "com.intuit.sbg.psp.salestaxservices"}
)
public class SalesTaxGatewayConfig {



    @Bean(name = "salesTaxComparisonThreadPoolExecutor")
    public Executor salesTaxComparisonThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.initialize();
        return executor;
    }
}
