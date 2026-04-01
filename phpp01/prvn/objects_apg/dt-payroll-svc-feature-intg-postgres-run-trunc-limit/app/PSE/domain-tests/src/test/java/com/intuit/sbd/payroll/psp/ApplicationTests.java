/**
 * $Id: //psp/dev/PSE/Domain/test/com/com/intuit/sbd/payroll/psp/ApplicationTests.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConstants;
import com.intuit.sbd.payroll.psp.configuration.DatabaseType;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;

/**
 * @author Wiktor Kozlik
 */
public class ApplicationTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.truncateTables();
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void resolveSpcfConfigFile() {
        String path = ConfigurationManager.getConfigFilePath("spcf-meta-conf.xml");
        org.junit.Assert.assertNotNull(path);
    }

    @Test
    public void resolveTruncateTablesSqlFile() {
        String path = Application.findFileOnClassPath("resources/TruncateTables.sql");
        org.junit.Assert.assertNotNull(path);
    }

    @Test
    public void sequenceIdGenerationTests() {

        try {
            PayrollServices.beginUnitOfWork();
            for (SequenceId sequenceId : SequenceId.values()) {
                long seq1 = Application.nextSequenceValue(sequenceId, Long.class);
                long seq2 = Application.nextSequenceValue(sequenceId, Long.class);
                long seqDiff = seq2 - seq1;
                if(SequenceId.SEQ_TRACE_NBR == sequenceId)
                    assertEquals(1000000000, seqDiff);
                else
                    assertEquals(1, seqDiff);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void checkDatabaseType() {
        String originalVal = System.getProperty(DatabaseConstants.MonolithDbKey);
        try {
            System.setProperty(DatabaseConstants.MonolithDbKey, DatabaseType.ORACLE.toString());
            assertTrue(Application.isOracleDB());

            System.setProperty(DatabaseConstants.MonolithDbKey, DatabaseType.POSTGRES.toString());
            assertTrue(Application.isPostgresDB());
        } finally {
            // reset back to original
            System.setProperty(DatabaseConstants.MonolithDbKey, originalVal);
        }
    }
}
