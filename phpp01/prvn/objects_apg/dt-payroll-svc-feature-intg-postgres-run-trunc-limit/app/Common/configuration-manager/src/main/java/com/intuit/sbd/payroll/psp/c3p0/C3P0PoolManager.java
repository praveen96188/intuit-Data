package com.intuit.sbd.payroll.psp.c3p0;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.Database;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConfigManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseType;
import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import com.mchange.v2.c3p0.UnifiedConnectionTester;
import com.mchange.v2.c3p0.impl.DefaultConnectionTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class C3P0PoolManager {
    private static final Logger mLogger = LoggerFactory.getLogger(C3P0PoolManager.class);

    public static final String DEFAULT_HARD_RESET_ELIGIBLE_SQL_STATES = "08003,08006";

    private static String getTestQuery(Connection conn) throws SQLException {
        if(conn.getMetaData().getURL().contains(DatabaseType.ORACLE.toString()))
            return ConfigurationManager.getConfigurationProperties(DatabaseConfigManager.MonolithDbHibernateToken).getProperty("hibernate.c3p0.preferredTestQuery");
        if(conn.getMetaData().getURL().contains(DatabaseType.POSTGRES.toString()))
            return ConfigurationManager.getConfigurationProperties(DatabaseConfigManager.AuditDbHibernateToken).getProperty("hibernate.c3p0.preferredTestQuery");

        throw new RuntimeException(String.format("Invalid connection %s!!", conn.getMetaData().getURL()));
    }

    private static boolean testPoolConnectivity(PooledDataSource pbds){
        try {
            mLogger.info("Action=Test_Connection_Pool, Pool={}", pbds.getIdentityToken());
            Connection conn = pbds.getConnection();
            String testQuery = getTestQuery(conn);

            UnifiedConnectionTester connectionTester = (DefaultConnectionTester) C3P0Registry.getDefaultConnectionTester();
            int res = connectionTester.activeCheckConnection(conn, testQuery);
            mLogger.info("Action=Test_Connection_Pool, Pool={} testQuery={} Status={} ", pbds.getIdentityToken(), testQuery, res);
            if (res == 0)
                return true;
        } catch (SQLException e){
            mLogger.error("Exception in connection checkout", e);
        }

        return false;
    }

    public static void resetAllDataSourcePools() throws SQLException {
        mLogger.info("Action=Attempt_Hard_Reset");
        Set dsSet = C3P0Registry.getPooledDataSources();
        Iterator iter = dsSet.iterator();

        while(iter.hasNext()){
            PooledDataSource pbds = (PooledDataSource)iter.next();
            resetPool(pbds);
        }
    }

    public static void resetPool(PooledDataSource pbds) throws SQLException {
        mLogger.info("Action=Attempt_Hard_Reset, Pool={}", pbds.getIdentityToken());
        // reset only if there are connections in the connection pool
        if(pbds.getNumConnectionsAllUsers() != 0){
            resetConnectionPool(pbds);
        }
    }

    static private synchronized void resetConnectionPool(PooledDataSource pbds) throws SQLException{
        mLogger.info("Action=Inside_resetConnectionPool, Pool={}", pbds.getIdentityToken());

        if(pbds.getNumConnectionsAllUsers() != 0 && !testPoolConnectivity(pbds)){
            logc3p0(pbds);
            mLogger.info("Action=Starting_Hard_Reset, Pool={}", pbds.getIdentityToken());
            pbds.hardReset();
            mLogger.info("Action=Completed_Hard_Reset, Pool={}", pbds.getIdentityToken());
            logc3p0(pbds);
        }
    }

    static private void logc3p0(PooledDataSource pbds){
        try {
            mLogger.info("Pool={}, AllUsers={}, NumUserPools={}, NumConnectionsAllUsers={}, NumIdleConnectionsAllUsers={}, NumBusyConnectionsAllUsers={}, NumUnclosedOrphanedConnectionsAllUsers={}, ThreadPoolSize={}, ThreadPoolNumActiveThreads={}, ThreadPoolNumIdleThreads={}, ThreadPoolNumTasksPending={}, NumHelperThreads={}",
                    pbds.getIdentityToken(), pbds.getAllUsers().toString(), pbds.getNumUserPools(), pbds.getNumConnectionsAllUsers(), pbds.getNumIdleConnectionsAllUsers(), pbds.getNumBusyConnectionsAllUsers(), pbds.getNumUnclosedOrphanedConnectionsAllUsers(), pbds.getThreadPoolSize(), pbds.getThreadPoolNumActiveThreads(), pbds.getThreadPoolNumIdleThreads(), pbds.getThreadPoolNumTasksPending(), pbds.getNumHelperThreads());

        } catch(SQLException ex){
            mLogger.error("Exception while logging c3p0 status",ex);
        }
    }
}
