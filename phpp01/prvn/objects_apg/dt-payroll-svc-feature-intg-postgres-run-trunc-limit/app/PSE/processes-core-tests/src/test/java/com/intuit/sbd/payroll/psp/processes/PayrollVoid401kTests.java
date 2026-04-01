package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401kBatchProcess;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
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

public class PayrollVoid401kTests {

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
    public void testHappyPathVoidPayroll() {
        PayrollServices.beginUnitOfWork();
        ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        VoidPayrollDTO voidDTO = new VoidPayrollDTO();
        voidDTO.setSourcePayrollRunId("BatchTest09");
        ArrayList<String> paycheckList = new ArrayList<String>();
        String paycheckId = "1234567789";
        paycheckList.add(paycheckId);
        voidDTO.setPaycheckIdList(paycheckList);
        ProcessResult procresult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, "8575577", voidDTO);
        assertSuccess(procresult);
        Paycheck paycheck = Paycheck.findPaycheck(company, paycheckId);
        assertEquals("paycheck status", PaycheckStatusCode.Inactive, paycheck.getStatus());
        assertNotNull("adjustment submission", paycheck.getCompanyAdjustmentSubmission());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAddPlusVoidPayroll() {
        PayrollServices.beginUnitOfWork();
        ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        ProcessResult procResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "8575577", ddAnd401kDL.get401kWarningCloudPayrollRunDTO());
        assertSuccess(procResult2);
        VoidPayrollDTO voidDTO = new VoidPayrollDTO();
        voidDTO.setSourcePayrollRunId("SomeOtherNewId");
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add("NonDDPaycheckId2");
        voidDTO.setPaycheckIdList(paycheckList);
        ProcessResult procresult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, "8575577", voidDTO);
        assertSuccess(procresult);
        Paycheck paycheck = Paycheck.findPaycheck(company, "NonDDPaycheckId2");
        assertEquals("paycheck status", PaycheckStatusCode.Inactive, paycheck.getStatus());
        assertNotNull("adjustment submission", paycheck.getCompanyAdjustmentSubmission());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testVoidAfterTOKOffloadButNotOffloadable() {
        PayrollServices.beginUnitOfWork();
        ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        ProcessResult procResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "8575577", ddAnd401kDL.get401kWarningCloudPayrollRunDTO());
        assertSuccess(procResult2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 2, 28, SpcfTimeZone.getLocalTimeZone()));
        VoidPayrollDTO voidDTO = new VoidPayrollDTO();
        voidDTO.setSourcePayrollRunId("SomeOtherNewId");
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add("NonDDPaycheckId2");
        voidDTO.setPaycheckIdList(paycheckList);
        ProcessResult procresult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, "8575577", voidDTO);
        assertSuccess(procresult);
        assertEquals("number of messages", 0, procresult.getMessages().size());
        Paycheck paycheck = Paycheck.findPaycheck(company, "NonDDPaycheckId2");
        assertEquals("paycheck status", PaycheckStatusCode.Inactive, paycheck.getStatus());
        assertNotNull("adjustment submission", paycheck.getCompanyAdjustmentSubmission());        
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testVoidAfterTOKOffload() {
        PayrollServices.beginUnitOfWork();
        ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        ProcessResult procResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "8575577", ddAnd401kDL.get401kWarningCloudPayrollRunDTO());
        assertSuccess(procResult2);
        PayrollServices.commitUnitOfWork();

        ddAnd401kDL.makePaychecksOffloadable();

        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 2, 22, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            new ThirdParty401kBatchProcess().createFiles();
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail("Exception creating files");
        }     

        ddAnd401kDL.voidAfterOffload();
    }

    @Test
    public void testVoidAfterTOKOffloadMissedCutoff() {
        PayrollServices.beginUnitOfWork();
        ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 2, 24, SpcfTimeZone.getLocalTimeZone()));
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        ProcessResult procResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "8575577", ddAnd401kDL.get401kWarningCloudPayrollRunDTO());
        assertSuccess(procResult2);
        PayrollServices.commitUnitOfWork();

        ddAnd401kDL.makePaychecksOffloadable();

        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 2, 22, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            new ThirdParty401kBatchProcess().createFiles();
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail("Exception creating files");
        }

        ddAnd401kDL.voidAfterOffloadMissedCutoff();
    }

    @Test
    public void testVoidPayroll_Cloud401k() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.ThirdParty401k);

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
        VoidPayrollDTO voidDTO = new VoidPayrollDTO();
        voidDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidDTO.setPaycheckIdList(paycheckList);
        ProcessResult voidResult = PayrollServices.payrollManager.voidPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), voidDTO);
        PSP_PRAssert.assertSuccess("submit payroll", voidResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            assertEquals("voided", PaycheckStatusCode.Inactive, paycheck.getStatus());
        }
    }

    @Test
    public void testVoidPayroll_CloudDD401k() {
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
        VoidPayrollDTO voidDTO = new VoidPayrollDTO();
        voidDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidDTO.setPaycheckIdList(paycheckList);
        ProcessResult voidResult = PayrollServices.payrollManager.voidPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), voidDTO);
        PSP_PRAssert.assertSuccess("submit payroll", voidResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            assertEquals("voided", PaycheckStatusCode.Inactive, paycheck.getStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }
}