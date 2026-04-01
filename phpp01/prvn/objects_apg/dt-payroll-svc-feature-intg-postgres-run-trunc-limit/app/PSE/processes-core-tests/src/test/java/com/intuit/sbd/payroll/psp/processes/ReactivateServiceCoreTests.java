/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.processes.util.WorkersCompTestUtil;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Contains the unit tests for the <CODE>Message</CODE> class.
 *
 * @author: dmartens
 * @version: Sep 6, 2007
 */
public class ReactivateServiceCoreTests {

    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testCompanyDNE() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> processResult = PayrollServices.companyManager.reactivateService(SourceSystemCode.QBOE, "1234567", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:1234567 does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void reactivateServiceCoreNullCompanyId() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceReactivateProcessResult = PayrollServices.companyManager.reactivateService(SourceSystemCode.QBOE, null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, ddServiceReactivateProcessResult.getMessages().size());
        Message errorMessage = ddServiceReactivateProcessResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void reactivateServiceCoreNullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceReactivateProcessResult = PayrollServices.companyManager.reactivateService(null, "1234567", null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, ddServiceReactivateProcessResult.getMessages().size());
        Message errorMessage = ddServiceReactivateProcessResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void reactivateServiceCoreNullService() {
        //Setup
        PayrollServices.beginUnitOfWork();
        Company domainCompany = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();

        //Test
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceReactivateProcessResult = PayrollServices.companyManager.reactivateService(domainCompany.getSourceSystemCd(), domainCompany.getSourceCompanyId(), null);
        PayrollServices.commitUnitOfWork();

        //Verifications
        Assert.assertEquals(1, ddServiceReactivateProcessResult.getMessages().size());
        Assert.assertEquals("117", ddServiceReactivateProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals("Company Service Info is not specified.",
                ddServiceReactivateProcessResult.getMessages().get(0).getMessage());
    }

    @Test
    public void reactivateServiceCoreSuccess() {
        /********************Setup******************************/
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Cancel the service
        ProcessResult<CompanyService> procResult = PayrollServices.companyManager.deactivateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        assertTrue(procResult.isSuccess());

        /********************End Setup******************************/

        //Test
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.reactivateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        //Verifications
        assertSuccess("reactivateService", cancelServiceProcessResult);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DDCompanyServiceInfo foundDDCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(ServiceSubStatusCode.PendingBankVerification, foundDDCompanyService.getStatusCd());

    }

    @Test
    public void reactivateServiceCore_StatusTerminated() {
        //Setup
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);

        //Set service status to terminated
        ddCompanyService.setStatusCd(ServiceSubStatusCode.Terminated);
        PayrollServicesTest.save(company1);
        PayrollServices.commitUnitOfWork();

        //Test
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> reactivateServiceProcessResult = PayrollServices.companyManager.reactivateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        System.out.println(reactivateServiceProcessResult);

        //Verifications
        Assert.assertEquals(1, reactivateServiceProcessResult.getMessages().size());
        Assert.assertEquals("1013", reactivateServiceProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals(
                "Company " + company1.getSourceSystemCd() + ":" + company1
                        .getSourceCompanyId() + " already exists on the DirectDeposit service.",
                reactivateServiceProcessResult.getMessages().get(0).getMessage());

    }

    @Test
    public void reactivateServiceCore_StatusActive() {
        //Setup
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        //Test
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> reactivateServiceProcessResult = PayrollServices.companyManager.reactivateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        //Verifications
        Assert.assertEquals(1, reactivateServiceProcessResult.getMessages().size());
        Assert.assertEquals("1013", reactivateServiceProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals(
                "Company " + company1.getSourceSystemCd() + ":" + company1
                        .getSourceCompanyId() + " already exists on the DirectDeposit service.",
                reactivateServiceProcessResult.getMessages().get(0).getMessage());
    }

    @Test
    public void reactivateServiceCore_CompanyNotAssociatedWithService() {
        //Setup
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();

        //Test
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> reactivateServiceProcessResult = PayrollServices.companyManager.reactivateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        //Verifications
        Assert.assertEquals(1, reactivateServiceProcessResult.getMessages().size());
        Assert.assertEquals("1010", reactivateServiceProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals("Company " + company1.getSourceSystemCd() + ":" + company1
                .getSourceCompanyId() + " is not associated with the " + ServiceCode.DirectDeposit + " service.",
                reactivateServiceProcessResult.getMessages().get(0).getMessage());
    }

       @Test
    public void reactivateBillPaymentServiceCoreSuccess() {
        /********************Setup******************************/
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();

        ServiceInfoDTO ddCompanyService = dataloader.getTestCompanyService();

        ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.addService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ddCompanyService);
        CompanyService ddService = ddServiceAddProcessResult.getResult();
        ddService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        ddService = Application.save(ddService);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ServiceInfoDTO companyService = dataloader.getTestCompanyBillPaymentService();

        ProcessResult<CompanyService> serviceAddProcessResult = PayrollServices.companyManager.addService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), companyService);


        PayrollServices.commitUnitOfWork();

        assertEquals(0, serviceAddProcessResult.getMessages().size());
        assertNotNull(company1);

        PayrollServices.beginUnitOfWork();
        //Cancel the service
        ProcessResult<CompanyService> procResult = PayrollServices.companyManager.deactivateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.BillPayment);
        PayrollServices.commitUnitOfWork();

        assertTrue(procResult.isSuccess());

        /********************End Setup******************************/

        //Test
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> reactivateServiceProcessResult = PayrollServices.companyManager.reactivateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.BillPayment);
        PayrollServices.commitUnitOfWork();

        //Verifications
        assertSuccess("reactivateService", reactivateServiceProcessResult);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        BPCompanyServiceInfo foundDDCompanyService = (BPCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany, ServiceCode.BillPayment);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, foundDDCompanyService.getStatusCd());

    }
    @Test
     public void testDeactivateTaxThenReactivate_PSRV004008() {
         String psid = "123456789";
         Company taxCompany = WorkersCompTestUtil.createAssistedCompanyWithWorkersCompService(psid);
         PayrollServices.beginUnitOfWork();
         PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
         PayrollServices.commitUnitOfWork();
         taxCompany = DataLoadServices.refreshCompany(taxCompany);
         CompanyService taxService = taxCompany.getService(ServiceCode.Tax);
         Assert.assertEquals(ServiceSubStatusCode.Cancelled, taxService.getStatusCd());
         PayrollServices.beginUnitOfWork();
         ProcessResult<CompanyService> reactivateServiceProcessResult = PayrollServices.companyManager.reactivateService(taxCompany.getSourceSystemCd(), taxCompany.getSourceCompanyId(), ServiceCode.Tax);
         PayrollServices.commitUnitOfWork();
         taxCompany = DataLoadServices.refreshCompany(taxCompany);
         taxService = CompanyService.findCompanyService(taxCompany, ServiceCode.Tax);
         Assert.assertEquals(ServiceSubStatusCode.PendingSetup, taxService.getStatusCd());
     }
}
