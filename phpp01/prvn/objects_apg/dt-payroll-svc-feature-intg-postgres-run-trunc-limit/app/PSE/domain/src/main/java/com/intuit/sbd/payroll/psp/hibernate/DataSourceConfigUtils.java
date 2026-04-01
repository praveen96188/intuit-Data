package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationProxy;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.cfg.Environment;

import java.util.Properties;

@Slf4j
public class DataSourceConfigUtils {

    public void addReadWriteConnectionProperties(Properties readWriteDatabaseProperties) {
        addConnectionProperties(readWriteDatabaseProperties, DatabaseConfigManager.MonolithDbToken, UserIdKey, PasswordKey, ConnectionUrlKey);
    }

    public void addReadConnectionProperties(Properties readDatabaseProperties) {
        addConnectionProperties(readDatabaseProperties, DatabaseConfigManager.MonolithDbToken, ReadOnlyUserIdKey, ReadOnlyPasswordKey, ReadOnlyConnectionUrlKey);
    }

    private static void addConnectionProperties(Properties properties, String monolithDBConnectionToken, String userIdKey, String passwordKey, String connectionUrlKey) {
        String connectionProvider = ConfigurationManager.getSettingValue(monolithDBConnectionToken, ConnectionProviderKey);

        // Either use connection from data source, or use JDBC directly.
        if (ConfigurationManager.containsKey(monolithDBConnectionToken, ConnectionDataSourceKey)) {
            properties.setProperty(Environment.DATASOURCE, ConfigurationManager.getSettingValue(monolithDBConnectionToken, ConnectionDataSourceKey));
            properties.setProperty(Environment.DIALECT, ConfigurationManager.getSettingValue(monolithDBConnectionToken, DatabaseDialectKey));
            // set connection provider, only if one has been configured.
            if (connectionProvider != null && connectionProvider.length() > 0) {
                properties.setProperty(Environment.CONNECTION_PROVIDER, connectionProvider);
            }
        } else {
            properties.setProperty(Environment.USER, ConfigurationManager.getSettingValue(monolithDBConnectionToken, userIdKey));
            properties.setProperty(Environment.PASS, ConfigurationManager.getSettingValue(monolithDBConnectionToken, passwordKey));
            properties.setProperty(Environment.URL, getJDBCUrl(monolithDBConnectionToken, connectionUrlKey));
            properties.setProperty(Environment.DRIVER, ConfigurationManager.getSettingValue(monolithDBConnectionToken, ConnectionDriverKey));
            properties.setProperty(Environment.DIALECT, ConfigurationManager.getSettingValue(monolithDBConnectionToken, DatabaseDialectKey));
            // set connection provider, only if one has been configured.
            if (connectionProvider != null && connectionProvider.length() > 0) {
                properties.setProperty(Environment.CONNECTION_PROVIDER, connectionProvider);
            }

        }

    }

    /**
     * Key for user ID.
     */
    private static final String UserIdKey = "dataAccess.connection.username";

    /**
     * Key for password.
     */
    private static final String PasswordKey = "dataAccess.connection.password";

    /**
     * Key for connection URL.
     */
    private static final String ConnectionUrlKey = "dataAccess.connection.url";

    /**
     * Key for read only user ID.
     */
    private static final String ReadOnlyUserIdKey = "dataAccess.connection.readOnly.username";

    /**
     * Key for read only password.
     */
    private static final String ReadOnlyPasswordKey = "dataAccess.connection.readOnly.password";

    /**
     * Key for read only connection URL.
     */
    private static final String ReadOnlyConnectionUrlKey = "dataAccess.connection.readOnly.url";

    /**
     * Key for connection provider.
     */
    private static final String ConnectionProviderKey = "dataAccess.connection.provider";

    /**
     * Key for connection driver.
     */
    private static final String ConnectionDriverKey = "dataAccess.connection.driver_class";

    /**
     * Key for datasource provider.
     */
    private static final String ConnectionDataSourceKey = "dataAccess.connection.datasource";

    /**
     * Key for database dialect.
     */
    private static final String DatabaseDialectKey = "dataAccess.connection.dialect";

    public static String getJDBCUrl(String monolithDBConnectionToken, String connectionUrlKey) {
        if (Application.isParallelEnv()) {
            String jdbcUrlKey = System.getenv("JDBC_OVERRIDE_URL");
            if (StringUtils.isNotEmpty(jdbcUrlKey)) {
                String jdbcUrlValue = ConfigurationProxy.decodeProperty(jdbcUrlKey);
                log.info("Parallel Env JDBC_OVERRIDE_URL jdbcUrlKey={} jdbcUrlValue={}", jdbcUrlKey, StringUtils.isNotEmpty(jdbcUrlValue));
                return jdbcUrlValue;
            }
        }

        return ConfigurationManager.getSettingValue(monolithDBConnectionToken, connectionUrlKey);
    }
}
