package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401kBatchProcess;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyDDPlus401kDataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * User: Dawn Martens
 * Date: 1/28/10
 * Time: 11:25:59 AM
 */

public class PayrollSubmit401kV2Tests {

    public CompanyDDPlus401kDataLoader ddAnd401kDL = null;


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testCloudOnlyPayrollSubmit() {
        
    }

    @Test
    public void testAssistedCustomerSignUp() {

    }

    @Test
    public void testSubmitCloudPayroll() {

    }

    @Test
    public void testCloudSignUpThenDDSignup() {

    }

    @Test
    public void testCloudSignUpThenAssistedSignup() {
        
    }

    @Test
    public void testCloudDDSignUpThen401kSignup() {

    }

    @Test
    public void testSubmitCloudWithCancelledDD() {
        
    }

    @Test
    public void testSubmitCloudWithCancelledAssisted() {

    }

    @Test
    public void testMigrateAssistedToDD() {

    }

    @Test
    public void testMigrateDDToAssisted() {

    }

    @Test
    public void testReactivateAssisted() {

    }

    @Test
    public void testTerminateAssisted() {
        
    }

    @Test
    public void testTerminateDD() {
        
    }
    

}