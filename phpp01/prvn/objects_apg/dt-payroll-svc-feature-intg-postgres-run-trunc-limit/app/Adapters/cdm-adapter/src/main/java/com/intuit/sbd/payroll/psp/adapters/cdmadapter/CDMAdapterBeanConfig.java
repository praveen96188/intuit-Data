package com.intuit.sbd.payroll.psp.adapters.cdmadapter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/** Earlier we were scanning "com.intuit.ems","com.intuit.sbd.payroll.psp.spring.config"
 * but now for com.intuit.sbd.payroll.psp.spring.config, the spring factories in domain module will be auto scanned due to presence in classpath
 * That is why only scanning com.intuit.ems for CDM Adapter endpoints
 **/
@Configuration
@EnableAsync
@ComponentScan(basePackages = {"com.intuit.ems"})
public class CDMAdapterBeanConfig {

    //todo - understand the implications of creation of large thread pools in a single JVM
    @Bean(name = "cdmAdapterThreadPoolExecutor")
    public Executor cdmAdapterThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        return executor;
    }

}
