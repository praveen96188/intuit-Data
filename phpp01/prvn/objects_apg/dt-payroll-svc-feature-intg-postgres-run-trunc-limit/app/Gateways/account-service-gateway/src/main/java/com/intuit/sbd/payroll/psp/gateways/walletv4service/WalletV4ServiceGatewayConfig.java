package com.intuit.sbd.payroll.psp.gateways.walletv4service;

import com.intuit.sbg.psp.payroll.iam.client.offline.OfflineTicketConfig;
import com.intuit.sbg.psp.spring.YamlPropertySourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.gateways.walletv4service"})
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:account-services-gateway.yml")
public class WalletV4ServiceGatewayConfig {

    public static final String WALLET_V4_OFFLINE_TICKET_SERVICES = "wallet.v4.offline-ticket";

    @Bean
    public boolean initializeWalletV4ServicesOfflineTicketConfig(OfflineTicketConfig offlineTicketConfig) {
        addWalletV4ServicesOfflineTicketConfig(offlineTicketConfig);
        return true;
    }

    private void addWalletV4ServicesOfflineTicketConfig(OfflineTicketConfig offlineTicketConfig) {
        offlineTicketConfig.addOfflineTicketConfig(WALLET_V4_OFFLINE_TICKET_SERVICES);
    }

    @Bean(name = "walletCloneThreadPoolExecutor")
    public Executor walletCloneThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.initialize();
        return executor;
    }
}
