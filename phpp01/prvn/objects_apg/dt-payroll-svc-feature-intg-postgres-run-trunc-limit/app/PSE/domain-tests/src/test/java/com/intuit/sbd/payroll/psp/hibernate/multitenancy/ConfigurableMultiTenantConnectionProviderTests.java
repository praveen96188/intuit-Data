package com.intuit.sbd.payroll.psp.hibernate.multitenancy;

import com.intuit.sbd.payroll.psp.Application;
import org.hibernate.FlushMode;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class ConfigurableMultiTenantConnectionProviderTests {

    @Test
    public void testReadWriteTenant() throws SQLException {
        try{
            Application.beginUnitOfWork();
            assertEquals("Tenant identifier is incorrect", TenantIdentifier.READ_WRITE.name() ,TenantContext.getTenantId());
            if(Application.isOracleDB()) {
                validateConnectionInfo("jdbc:oracle:thin://@localhost:1521/ORCLCDB.localdomain", "PSP_LOCAL");
            } else{
                validateConnectionInfo("jdbc:postgresql://localhost:5432/psp?escapeSyntaxCallMode=callIfNoReturn&prepareThreshold=0", "psp_local");
            }

        } finally {
            Application.rollbackUnitOfWork();
        }

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testReadOnlyTenant() throws SQLException {
        try{
            Application.beginUnitOfWork(FlushMode.AUTO, true);
            assertEquals("Tenant identifier is incorrect", TenantIdentifier.READ.name() ,TenantContext.getTenantId());
            if(Application.isOracleDB()) {
                validateConnectionInfo("jdbc:oracle:thin://@localhost:1521/ORCLCDB.localdomain", "PSP_LOCAL");
            } else {
                validateConnectionInfo("jdbc:postgresql://localhost:5432/psp?escapeSyntaxCallMode=callIfNoReturn&prepareThreshold=0", "psp_local");
            }
        } finally {
            Application.rollbackUnitOfWork();
        }

    }

    private void validateConnectionInfo(String jdbcUrl, String user) throws SQLException {
        Connection connection = Application.getConnection();
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        assertEquals("Jdbc url not matching", jdbcUrl, databaseMetaData.getURL());
        assertEquals("User not matching", user, databaseMetaData.getUserName());
    }
}
