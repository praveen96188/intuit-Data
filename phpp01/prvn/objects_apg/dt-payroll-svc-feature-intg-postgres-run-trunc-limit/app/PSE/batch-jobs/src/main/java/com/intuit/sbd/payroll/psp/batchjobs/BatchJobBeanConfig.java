package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbg.psp.spring.YamlPropertySourceFactory;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:batch-jobs.yml")
@EnableAsync
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.batchjobs",
        "com.intuit.sbd.payroll.psp.jss.processors.workerscomp",
        "com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob",
        "com.intuit.sbd.payroll.psp.jss.util",
        "com.intuit.sbd.payroll.psp.jss.processors.podcleanup",
})
public class BatchJobBeanConfig {

    @Bean(name = "inviteThreadPoolExecutor")
    public Executor inviteThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.initialize();
        return executor;
    }
}
