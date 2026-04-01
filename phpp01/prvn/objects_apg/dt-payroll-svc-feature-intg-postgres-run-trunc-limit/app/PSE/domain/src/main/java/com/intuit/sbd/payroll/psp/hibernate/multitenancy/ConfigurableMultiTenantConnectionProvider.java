package com.intuit.sbd.payroll.psp.hibernate.multitenancy;

import com.intuit.sbd.payroll.psp.hibernate.DataSourceConfigUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.c3p0.internal.C3P0ConnectionProvider;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class ConfigurableMultiTenantConnectionProvider extends AbstractMultiTenantConnectionProvider implements ServiceRegistryAwareService {

    private final TenantIdentifier defaultTenantIdentifier = TenantIdentifier.READ_WRITE;
    private final Map<String, ConnectionProvider> connectionProviderMap = new HashMap<>();
    private DataSourceConfigUtils dataSourceConfigUtils = new DataSourceConfigUtils();
    private FeatureFlagLazyLoader featureFlagLazyLoader = FeatureFlagLazyLoader.getInstance();
    private ServiceRegistryImplementor serviceRegistryImplementor;

    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        return connectionProviderMap.get(defaultTenantIdentifier.name());
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        if (isMultiTenancyFeatureEnabled()) {
            log.info("Choosing datasource for TenantIdentifier={}", tenantIdentifier);
            return connectionProviderMap.get(tenantIdentifier);
        }
        return connectionProviderMap.get(defaultTenantIdentifier.name());
    }

    @Override
    public void injectServices(ServiceRegistryImplementor serviceRegistry) {
        this.serviceRegistryImplementor = serviceRegistry;
        registerAllConnectionProviders();
    }

    private void registerAllConnectionProviders() {
        registerReadWriteConnectionProvider();
        registerReadConnectionProvider();
    }

    private void registerReadWriteConnectionProvider() {
        Properties readWriteDatabaseProperties = getHibernateProperties();
        dataSourceConfigUtils.addReadWriteConnectionProperties(readWriteDatabaseProperties);
        registerConnectionProvider(TenantIdentifier.READ_WRITE, readWriteDatabaseProperties);
    }

    private void registerReadConnectionProvider() {
        Properties readDatabaseProperties = getHibernateProperties();
        dataSourceConfigUtils.addReadConnectionProperties(readDatabaseProperties);
        registerConnectionProvider(TenantIdentifier.READ, readDatabaseProperties);
    }

    private void registerConnectionProvider(TenantIdentifier tenantIdentifier, Properties databaseProperties) {
        C3P0ConnectionProvider c3P0ConnectionProvider = new C3P0ConnectionProvider();
        c3P0ConnectionProvider.injectServices(serviceRegistryImplementor);
        c3P0ConnectionProvider.configure(databaseProperties);
        this.connectionProviderMap.put(tenantIdentifier.name(), c3P0ConnectionProvider);
    }

    private Properties getHibernateProperties() {
        ConfigurationService cfgService = this.serviceRegistryImplementor.getService(ConfigurationService.class);
        Properties properties = new Properties();
        properties.putAll(cfgService.getSettings());
        return properties;
    }

    private boolean isMultiTenancyFeatureEnabled() {
        return featureFlagLazyLoader.getFeatureFlagValue(FeatureFlags.Key.ENABLE_MULTI_TENANCY);
    }

}
