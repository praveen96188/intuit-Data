package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.PINUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Tiger Shao
 * Date: 6/19/12
 * Time: 3:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class VerifyCompanyEINCoreTests {
    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testNullEIN() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load company", 0, result.getMessages().size());

        // Create and verify CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyEIN(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                                                                                           company1.getCompanyId(), null, null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "12006", errorMessage.getMessageCode());
        assertEquals("Error message", "EIN not recognized. Please send the correct EIN.",
                     errorMessage.getMessage());
    }

    @Test
    public void testInvalidEIN() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load company", 0, result.getMessages().size());

        // Create and verify CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyEIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "12-3456788", "1218");
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "12006", errorMessage.getMessageCode());
    }

    @Test
    public void VerifyCompanyEIN_Success() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load company", 0, result.getMessages().size());

        // Create and verify CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyEIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "12-3456789", "");
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);
    }
}



