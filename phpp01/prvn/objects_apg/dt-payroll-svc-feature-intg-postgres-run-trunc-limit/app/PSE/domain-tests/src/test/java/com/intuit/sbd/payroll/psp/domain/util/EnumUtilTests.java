package com.intuit.sbd.payroll.psp.domain.util;

import static junit.framework.Assert.assertEquals;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;

/**
 * @author Wiktor Kozlik
 */
public class EnumUtilTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void validateEnumForDataEntity() {
        assertEquals("ER Verification Debit", EnumUtils.getReadableName(TransactionTypeCode.EmployerVerificationDebit));
        assertEquals(TransactionTypeCode.EmployerVerificationDebit, EnumUtils.getEnumForReadableName(TransactionTypeCode.class, "ER Verification Debit"));
    }

    @Test
    public void validateEnumWithoutDataEntity() {
        assertEquals("PendingTransmission", EnumUtils.getReadableName(GemsUploadBatchStatus.PendingTransmission));
        assertEquals(GemsUploadBatchStatus.PendingTransmission, EnumUtils.getEnumForReadableName(GemsUploadBatchStatus.class, "PendingTransmission"));
    }

    @Test
    public void validateEnumWithoutDataEntityButWithCustomNames() {
        assertEquals("Company Pending Termination", EnumUtils.getReadableName(CancellationReasonCode.CompanyPendingTermination));
        assertEquals(CancellationReasonCode.CompanyPendingTermination, EnumUtils.getEnumForReadableName(CancellationReasonCode.class, "Company Pending Termination"));
    }
}
