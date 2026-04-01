package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyDDPlus401kDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company401kDataloader;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import junit.framework.TestCase;

/**
 * User: Dawn Martens
 * Date: 1/28/10
 * Time: 11:25:59 AM
 */

public class PaycheckDelete401kTests {

    public CompanyDDPlus401kDataLoader ddAnd401kDL = null;


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.QBDTWSAdapter);
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
    public void testHappyPathDeletePaycheck() {
        PayrollServices.beginUnitOfWork();
        ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        PayrollServices.beginUnitOfWork();
        ProcessResult procresult = PayrollServices.payrollManager.deletePaycheck(SourceSystemCode.QBDT, "8575577", "1234567789", null);
        assertSuccess(procresult);
        //todo ensure paycheck status is Deleted
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDeletePaycheckDNE() {
        PayrollServices.beginUnitOfWork();
        ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        PayrollServices.beginUnitOfWork();
        ProcessResult procresult = PayrollServices.payrollManager.deletePaycheck(SourceSystemCode.QBDT, "8575577", "abcdefg", null);
        assertFalse(procresult.isSuccess());
        //todo ensure paycheck status is not Deleted
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAddPlusDeletePaycheck() {
        PayrollServices.beginUnitOfWork();
        ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        PayrollServices.beginUnitOfWork();
        ProcessResult procResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "8575577", ddAnd401kDL.get401kWarningCloudPayrollRunDTO());
        assertSuccess(procResult2);
        ProcessResult procresult = PayrollServices.payrollManager.deletePaycheck(SourceSystemCode.QBDT, "8575577", "NonDDPaycheckId2", null);
        assertSuccess(procresult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDeleteAfterTOKOffload() {
        PayrollServices.beginUnitOfWork();
        ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        ddAnd401kDL.deleteAfterOffload();

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDeleteAfterTOKOffloadMissedCutoff() {
        PayrollServices.beginUnitOfWork();
        ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();
        // reset principal since FeeEventsBatch job just set
        PayrollServices.setCurrentPrincipal(SystemPrincipal.QBDTWSAdapter);
        ddAnd401kDL.deleteAfterOffloadMissedCutoff();

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDelete_CloudDD401k() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.ThirdParty401k);

        List<Employee> employees = DataLoadServices.addEEs(company, 1, false, true);

        List<CompanyPayrollItem> companyPayrollItems = DataLoadServices.addPayrollItems(company, PayrollItemCode.Compensation, PayrollItemCode.Tp401kEmployeeDeferral, PayrollItemCode.Tp401kEmployerMatch);

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = DataLoadServices.create401kPayrollRun(employees, companyPayrollItems);
        ProcessResult<PayrollRun> submitPayrollPR =
                PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit payroll", submitPayrollPR);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        ProcessResult deleteResult = PayrollServices.payrollManager.deletePaycheck(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId(), null);
        PSP_PRAssert.assertSuccess("submit payroll", deleteResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            assertEquals("voided", PaycheckStatusCode.Deleted, paycheck.getStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

}