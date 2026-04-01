package com.intuit.sbd.payroll.psp.configuration;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConfigManager {
    public static final String MonolithDbToken;
    public static final String MonolithDbHibernateToken;
    public static final String AuditDbToken;
    public static final String AuditDbHibernateToken;

    private static Logger logger = LoggerFactory.getLogger(DatabaseConfigManager.class);
    static{
        MonolithDbToken = generateDbToken(DatabaseConstants.MonolithDbKey, DatabaseConstants.DefaultMonolithDb, DatabaseConstants.MonolithTokenPrefix);
        MonolithDbHibernateToken = generateDbToken(DatabaseConstants.MonolithDbKey, DatabaseConstants.DefaultMonolithDb, DatabaseConstants.HibernateTokenPrefix);
        AuditDbToken = generateDbToken(DatabaseConstants.AuditDbKey, DatabaseConstants.DefaultAuditDb, DatabaseConstants.AuditTokenPrefix);
        AuditDbHibernateToken = generateDbToken(DatabaseConstants.AuditDbKey, DatabaseConstants.DefaultAuditDb, DatabaseConstants.HibernateTokenPrefix);
    }

    private static String generateDbToken(String key, String defaultValue, String tokenTemplate){
        String dbConfig = System.getProperty(key, defaultValue);
        if(StringUtils.isBlank(dbConfig)){
            throw new RuntimeException(String.format("Unable to read %s configuration", key));
        }
        String dbToken = new StringBuilder().append(tokenTemplate).append(dbConfig).toString();

        logger.info("{} configuration file, TOKEN={}", key, dbToken);
        return dbToken;
    }

    public static DatabaseType getDatabaseType(String key, String defaultValue) {
        return DatabaseType.fromValue(System.getProperty(key, defaultValue));
    }
}
