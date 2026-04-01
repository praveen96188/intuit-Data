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

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdjustmentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyDDPlus401kDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * Contains the unit tests for the <CODE>Message</CODE> class.
 *
 * @author: dmartens
 * @version: Sep 6, 2007
 */
public class CancelServiceCoreTests {

    private DataLoader dataloader;


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        dataloader = new DataLoader();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void cancelServiceCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals(0, cancelServiceProcessResult.getMessages().size());

        company1 = cancelServiceProcessResult.getResult().getCompany();
        ddCompanyService = cancelServiceProcessResult.getResult();
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, ddCompanyService.getStatusCd());

        assertServiceCanceled(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
    }

    @Test
    public void cancel401kServiceCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        CompanyDDPlus401kDataLoader ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        ddAnd401kDL.persistQBCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("8575577", SourceSystemCode.QBDT);
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.ThirdParty401k);
        PayrollServices.commitUnitOfWork();
        assertTrue(cancelServiceProcessResult.isSuccess());
        Assert.assertEquals(0, cancelServiceProcessResult.getMessages().size());

        company1 = cancelServiceProcessResult.getResult().getCompany();
        CompanyService fourOhOnekService = cancelServiceProcessResult.getResult();
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, fourOhOnekService.getStatusCd());
    }

    @Test
    public void cancel401kServiceCore_AgentAttempt() {
        try {
            PayrollServices.beginUnitOfWork();
            CompanyDDPlus401kDataLoader ddAnd401kDL = new CompanyDDPlus401kDataLoader();
            ddAnd401kDL.persistQBCompany1();
            PayrollServices.commitUnitOfWork();

            //Simulate that an agent is attempting to cancel 401k
            PayrollServices.beginUnitOfWork();
            AuthRole foundRole = AuthRole.findRole("FRGSupervisor");
            ProcessResult<AuthUser> processResult = PayrollServices.userManager.addUser("simulatedAgent", Arrays.asList(foundRole.getRoleId()),"Test","Agent");
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            AuthUser user = processResult.getResult();
            //Set PSP Principal for the User
            PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));
            PayrollServices.commitUnitOfWork();

            //try it
            PayrollServices.beginUnitOfWork();
            Company company1 = Company.findCompany("8575577", SourceSystemCode.QBDT);
            ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                    company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.ThirdParty401k);

            assertFalse(cancelServiceProcessResult.isSuccess());
            Assert.assertEquals(1, cancelServiceProcessResult.getMessages().size());
            Message message = cancelServiceProcessResult.getMessages().get(0);
            assertEquals("Level", MessageInfo.MessageLevel.ERROR, message.getLevel());
            assertEquals("Message", "The service ThirdParty401k cannot be manually cancelled.", message.getMessage());
            assertEquals("Message Code", "10075", message.getMessageCode());

            company1 = Company.findCompany("8575577", SourceSystemCode.QBDT);
            CompanyService fourOhOnekService = CompanyService.findCompanyService(company1, ServiceCode.ThirdParty401k);
            Assert.assertEquals(ServiceSubStatusCode.PendingFirstPayroll, fourOhOnekService.getStatusCd());
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.UnitTest);
        }
    }

    @Test
    public void cancelServiceCore_ServiceNull() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceCancelProcessResult = PayrollServices.companyManager.deactivateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), null);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(1, ddServiceCancelProcessResult.getMessages().size());
        Assert.assertEquals("117", ddServiceCancelProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals("Company Service Info is not specified.",
                ddServiceCancelProcessResult.getMessages().get(0).getMessage());
    }

/*    @Test
    public void cancelServiceCore_StatusInactive() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);

        //Set service status to terminated

        CompanyServiceBE.updateCompanyServiceStatus(ddCompanyService, ServiceSubStatusCode.Terminated);

        PayrollServicesTest.save(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals(1, cancelServiceProcessResult.getMessages().size());
        Assert.assertEquals("1101", cancelServiceProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals("The operation CancelService is not allowed for company QBOE:123456 in its current state.",
                cancelServiceProcessResult.getMessages().get(0).getMessage());
    }*/

    @Test
    public void cancelServiceCore_StatusTerminated() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        //Terminate company
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> terminateProcess =
                PayrollServices.companyManager.terminateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        assertSuccess(terminateProcess);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals(1, cancelServiceProcessResult.getMessages().size());
        Assert.assertEquals("1101", cancelServiceProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals("The operation CancelService is not allowed for company QBOE:123456 in its current state.",
                cancelServiceProcessResult.getMessages().get(0).getMessage());
    }

    @Test
    public void cancelServiceCore_StatusSuspended() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                    company1.getSourceCompanyId(),
                                                                                    ServiceSubStatusCode.SuspendedDirectDeposit);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("Add Onhold Reason : SuspendedDirectDeposit ", result);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals(1, cancelServiceProcessResult.getMessages().size());
        Assert.assertEquals("1101", cancelServiceProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals("The operation CancelService is not allowed for company QBOE:123456 in its current state.",
                cancelServiceProcessResult.getMessages().get(0).getMessage());
    }

    @Test
    public void cancelServiceCore_StatusOnHold() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                    company1.getSourceCompanyId(),
                                                                                    ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("Add Onhold Reason : Fraud ", result);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals(1, cancelServiceProcessResult.getMessages().size());
        Assert.assertEquals("1101", cancelServiceProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals("The operation CancelService is not allowed for company QBOE:123456 in its current state.",
                cancelServiceProcessResult.getMessages().get(0).getMessage());
    }

    @Test
    public void cancelServiceCore_CompanyNotAssociatedWithService() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Service ddService = PayrollServices.entityFinder.findById(Service.class, ServiceCode.DirectDeposit);
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals(1, cancelServiceProcessResult.getMessages().size());
        Assert.assertEquals("1010", cancelServiceProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals("Company " + company1.getSourceSystemCd() + ":" + company1
                .getSourceCompanyId() + " is not associated with the " + ddService.getServiceCd() + " service.",
                cancelServiceProcessResult.getMessages().get(0).getMessage());
    }


    @Test
    public void cancelServiceCoreNullCompanyId() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceCancelProcessResult = PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, ddServiceCancelProcessResult.getMessages().size());
        Message errorMessage = ddServiceCancelProcessResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void cancelServiceCoreNullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceCancelProcessResult = PayrollServices.companyManager.deactivateService(null, "1234567", null);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, ddServiceCancelProcessResult.getMessages().size());
        Message errorMessage = ddServiceCancelProcessResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void testCompanyDNE() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> processResult = PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, "1234567", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:1234567 does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void testCompanyHasBAAndEEs() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        Company company1 = c1dl.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceCore = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        assertTrue(cancelServiceCore.isSuccess());

        assertServiceCanceled(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
    }

    @Test
    public void testCancelOneService_HappyPath() {
        String psid = "123456789";
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        // submit payroll with DD Transactions only (no liabilities)
        PayrollServices.beginUnitOfWork();
        Company domainCompany;
        PayrollRunDTO payrollDTO = DataLoadServices.createDDPayrollRun(company1, new DateDTO("2007-10-02"));
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(company1.getSourceSystemCd(), company1.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        domainCompany.getEntitlementUnitCollection().get(0).setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        Application.save(domainCompany.getEntitlementUnitCollection().get(0));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addAssistedEntitlementUnit(company1, "12345679", "12346789", true);
        
        //add Tax Service
        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addTaxService(domainCompany);

        // Verify Company has both DD and Tax services
        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        DomainEntitySet<CompanyService> companyServices = getCompanyServices(domainCompany).sort(CompanyService.Service().ServiceCd());
        assertEquals("Number of Company Services", 3, companyServices.size());
        assertEquals("Company Service1", ServiceCode.DirectDeposit, companyServices.get(1).getService().getServiceCd());
        assertEquals("Company Service2", ServiceCode.Tax, companyServices.get(2).getService().getServiceCd());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceCore = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();

        assertSuccess(cancelServiceCore);
        // Verify Company service statuses
        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        companyServices = getCompanyServices(domainCompany).sort(CompanyService.Service().ServiceCd());
        assertEquals("Number of Company Services", 3, companyServices.size());
        assertEquals("Company Service1", ServiceSubStatusCode.Cancelled, companyServices.get(1).getStatusCd());
        assertEquals("Company Service2", ServiceSubStatusCode.Cancelled, companyServices.get(2).getStatusCd());
        PayrollServices.commitUnitOfWork();
        assertServiceCanceled(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
    }

    @Test
    public void testCancelDDService_PendingDDTransactions() {
        String psid = "123456789";
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        // submit payroll with DD Transactions only (no liabilities)
        PayrollServices.beginUnitOfWork();
        Company domainCompany = Application.findById(Company.class, company1.getId());
        PayrollRunDTO payrollDTO = DataLoadServices.createDDPayrollRun(domainCompany, new DateDTO("2007-10-02"));
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                                                                 .submitPayroll(company1.getSourceSystemCd(), company1.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        domainCompany.getEntitlementUnitCollection().get(0).setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        Application.save(domainCompany.getEntitlementUnitCollection().get(0));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addAssistedEntitlementUnit(company1, "12345679", "12346789", true);

        //add Tax Service
        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.addTaxService(domainCompany);

        // Verify Company has both DD and Tax services
        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        DomainEntitySet<CompanyService> companyServices = getCompanyServices(domainCompany).sort(CompanyService.Service().ServiceCd());
        assertEquals("Number of Company Services", 3, companyServices.size());
        assertEquals("Company Service1", ServiceCode.DirectDeposit, companyServices.get(1).getService().getServiceCd());
        assertEquals("Company Service2", ServiceCode.Tax, companyServices.get(2).getService().getServiceCd());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.Tax));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testCancelAllServices_HappyPath() {
        String psid = "123456789";
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        // submit payroll with DD Transactions only (no liabilities)
        PayrollServices.beginUnitOfWork();
        Company domainCompany = Application.findById(Company.class, company1.getId());
        PayrollRunDTO payrollDTO = DataLoadServices.createDDPayrollRun(domainCompany, new DateDTO("2007-10-02"));
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                                                                 .submitPayroll(company1.getSourceSystemCd(), company1.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        domainCompany.getEntitlementUnitCollection().get(0).setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        Application.save(domainCompany.getEntitlementUnitCollection().get(0));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addAssistedEntitlementUnit(company1, "12345679", "12346789", true);

        //add Tax Service
        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.addTaxService(domainCompany);

        // Verify Company has both DD and Tax services
        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        DomainEntitySet<CompanyService> companyServices = getCompanyServices(domainCompany).sort(CompanyService.Service().ServiceCd());
        assertEquals("Number of Company Services", 3, companyServices.size());
        assertEquals("Company Service1", ServiceCode.DirectDeposit, companyServices.get(1).getService().getServiceCd());
        assertEquals("Company Service2", ServiceCode.Tax, companyServices.get(2).getService().getServiceCd());
        PayrollServices.commitUnitOfWork();

        // offload and complete DD transactions
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceCore = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();

        assertSuccess(cancelServiceCore);
        // Verify Company service statuses
        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        companyServices = getCompanyServices(domainCompany).sort(CompanyService.Service().ServiceCd());
        assertEquals("Number of Company Services", 3, companyServices.size());
        assertEquals("Company Service1", ServiceSubStatusCode.Cancelled, companyServices.get(1).getStatusCd());
        assertEquals("Company Service2", ServiceSubStatusCode.Cancelled, companyServices.get(2).getStatusCd());
        PayrollServices.commitUnitOfWork();
        assertServiceCanceled(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        assertServiceCanceled(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.Tax);
    }
    
    @Test
    public void testCancelAllServices_NotCompletedDDTransactions() {
        String psid = "123456789";
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        // submit payroll with DD Transactions only (no liabilities)
        PayrollServices.beginUnitOfWork();
        Company domainCompany = Application.findById(Company.class, company1.getId());
        PayrollRunDTO payrollDTO = DataLoadServices.createDDPayrollRun(domainCompany, new DateDTO("2007-10-02"));
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                                                                 .submitPayroll(company1.getSourceSystemCd(), company1.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        domainCompany.getEntitlementUnitCollection().get(0).setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        Application.save(domainCompany.getEntitlementUnitCollection().get(0));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addAssistedEntitlementUnit(company1, "12345679", "12346789", true);

        //add Tax Service
        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.addTaxService(domainCompany);

        // Verify Company has both DD and Tax services
        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        DomainEntitySet<CompanyService> companyServices = getCompanyServices(domainCompany).sort(CompanyService.Service().ServiceCd());
        assertEquals("Number of Company Services", 3, companyServices.size());
        assertEquals("Company Service1", ServiceCode.DirectDeposit, companyServices.get(1).getService().getServiceCd());
        assertEquals("Company Service2", ServiceCode.Tax, companyServices.get(2).getService().getServiceCd());
        PayrollServices.commitUnitOfWork();

        // offload and complete DD transactions
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceCore = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();

        assertSuccess(cancelServiceCore);

        // Verify Company service statuses
        PayrollServices.beginUnitOfWork();
        domainCompany = Application.findById(Company.class, company1.getId());
        companyServices = getCompanyServices(domainCompany).sort(CompanyService.Service().ServiceCd());
        assertEquals("Number of Company Services", 3, companyServices.size());
        assertEquals("Company Service1", ServiceSubStatusCode.Cancelled, companyServices.get(1).getStatusCd());
        assertEquals("Company Service2", ServiceSubStatusCode.Cancelled, companyServices.get(2).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testCancelService_ActiveCloudCancelDD() {
        PayrollServices.beginUnitOfWork();
        dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        Company company1 = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();

        dataloader.addCloudService(company1);
        dataloader.addDDService(company1);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceCore = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        assertSuccess(cancelServiceCore);

        PayrollServices.beginUnitOfWork();
        company1 = Application.findById(Company.class, company1.getId());
        DomainEntitySet<CompanyService> companyServices = getCompanyServices(company1);
        assertEquals("Number of Company Services", 2, companyServices.size());
        // ordered by service cd
        assertEquals("Cloud service", ServiceSubStatusCode.ActiveCurrent, companyServices.get(0).getStatusCd());
        assertEquals("DD service", ServiceSubStatusCode.Cancelled, companyServices.get(1).getStatusCd());

        DomainEntitySet<CompanyEventEmail> companyEventEmails = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.ServiceCancelledConfirmation1);
        assertEquals("CompanyEventEmail records", 1, companyEventEmails.size());
        DomainEntitySet<CompanyEventEmailParam> companyEventEmailParams = companyEventEmails.get(0).getEmailParamForEmailEvent(EventEmailParamTypeCode.ServiceType);
        assertEquals("CompanyEventEmailParam with ServiceType", 1, companyEventEmailParams.size());
        assertEquals("CompanyEventEmailParam with ServiceType Value", "Direct Deposit", companyEventEmailParams.get(0).getValue());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCancelTax_RAFEnrollmentStatuses() {
        //PendingEnrollment
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceCore = PayrollServices.companyManager.deactivateService(
                company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();

        assertSuccess(cancelServiceCore);

        RAFEnrollment rafEnrollment = company.getCurrentRAFEnrollment();
        assertNotNull(rafEnrollment);
        assertEquals("RAF Status", RAFEnrollmentStatus.Cancelled, rafEnrollment.getStatus());
        assertEquals("Company's IRS agency", CompanyAgency.findCompanyAgency(company, Agency.IRS),rafEnrollment.getCompanyAgency());

        //PendingEnrollmentTape
        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company2);

        PayrollServices.beginUnitOfWork();
        RAFEnrollment rafEnrollment2 = company2.getCurrentRAFEnrollment();
        assertNotNull(rafEnrollment2);
        ProcessResult updateRAFEnrollmentResult =
                    PayrollServices.companyManager.updateRAFEnrollmentStatus(company2.getSourceSystemCd(),
                            company2.getSourceCompanyId(), rafEnrollment2,
                            RAFEnrollmentStatus.PendingEnrollmentTape);
        PayrollServices.commitUnitOfWork();

        assertSuccess(updateRAFEnrollmentResult);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceCore2 = PayrollServices.companyManager.deactivateService(
                company2.getSourceSystemCd(), company2.getSourceCompanyId(), ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();

        assertSuccess(cancelServiceCore2);        

        rafEnrollment2 = company2.getCurrentRAFEnrollment();
        assertNotNull(rafEnrollment2);
        assertEquals("RAF Status", RAFEnrollmentStatus.Cancelled, rafEnrollment2.getStatus());
        assertEquals("Company's IRS agency", CompanyAgency.findCompanyAgency(company2, Agency.IRS),rafEnrollment2.getCompanyAgency());

    }

    @Test
    public void testLastPayrollRunIsUpdatedOnTaxCancel() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        //simulate a payroll (should count)
        DataLoadServices.setPSPDate(2013, 1, 1);
        DataLoadServices.setPrincipalToQBDT();
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));

        //simulate manual ledger keying (shouldn't count)
        DataLoadServices.setPSPDate(2013, 2, 1);
        DataLoadServices.setPrincipalToAgent();

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(Arrays.asList(DataLoadServices.createLiabilityAdjustmentDTO("1", "1", null, new DateDTO("2013-02-10"))));

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.addLiabilityAdjustments(company.getSourceSystemCd(), company.getSourceCompanyId(), null, companyAdjustmentSubmissionDTO, new DateDTO("2012-02-10"), liabilityAdjustmentOptionsDTO));
        PayrollServices.commitUnitOfWork();

        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        DataLoadServices.cancelService(company, ServiceCode.Tax);

        Application.beginUnitOfWork();
        TaxCompanyServiceInfo taxCompanyService = (TaxCompanyServiceInfo) Application.refresh(company).getCompanyService(ServiceCode.Tax);
        assertEquals(SpcfCalendar.createInstance(2013, 1, 10, SpcfTimeZone.getLocalTimeZone()), taxCompanyService.getLastPayrollDate().toLocal());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testCancelService_ActiveCloudCancelTax() {
        PayrollServices.beginUnitOfWork();
        dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        Company company1 = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();

        dataloader.addCloudService(company1);
        dataloader.addTaxService(company1);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceCore = PayrollServices.companyManager.deactivateService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();

        assertSuccess(cancelServiceCore);

        PayrollServices.beginUnitOfWork();
        company1 = Application.findById(Company.class, company1.getId());
        DomainEntitySet<CompanyService> companyServices = getCompanyServices(company1);
        assertEquals("Number of Company Services", 2, companyServices.size());
        // ordered by service cd
        assertEquals("Cloud service", ServiceSubStatusCode.ActiveCurrent, companyServices.get(0).getStatusCd());
        assertEquals("Tax service", ServiceSubStatusCode.Cancelled, companyServices.get(1).getStatusCd());

        DomainEntitySet<CompanyEventEmail> companyEventEmails = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.ServiceCancelledConfirmation1);
        assertEquals("CompanyEventEmail records", 1, companyEventEmails.size());
        DomainEntitySet<CompanyEventEmailParam> companyEventEmailParams = companyEventEmails.get(0).getEmailParamForEmailEvent(EventEmailParamTypeCode.ServiceType);
        assertEquals("CompanyEventEmailParam with ServiceType", 1, companyEventEmailParams.size());
        assertEquals("CompanyEventEmailParam with ServiceType Value", "Assisted", companyEventEmailParams.get(0).getValue());
        PayrollServices.rollbackUnitOfWork();
    }

    private DomainEntitySet<CompanyService> getCompanyServices(Company pComapny) {
        Expression<CompanyService> query =
                new Query<CompanyService>()
                       .Where(CompanyService.Company().equalTo(pComapny))
                       .OrderBy(CompanyService.Service().ServiceCd());

        return Application.find(CompanyService.class, query);
    }

    private void assertServiceCanceled(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, ServiceCode pServiceCode) {
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(pSourceCompanyId, pSourceSystemCd);
        CompanyService companyService = CompanyService
                .findCompanyService(foundCompany, pServiceCode);

        Iterator<CompanyBankAccount> itCompanyBankAccounts = foundCompany.getCompanyBankAccountCollection().iterator();
        PayrollServices.commitUnitOfWork();

        assertEquals("Service Status: ", ServiceSubStatusCode.Cancelled, companyService.getStatusCd());

        PayrollServices.beginUnitOfWork();
        foundCompany = Application.refresh(foundCompany);
        DomainEntitySet<Employee> employees = foundCompany.getDirectDepositEmployees();

        if (employees != null) {
            for (Employee currEmployee : employees) {
                assertEquals("Employee Status", EmployeeStatus.Active, currEmployee.getStatusCd());
                DomainEntitySet<EmployeeBankAccount> eeBAs = currEmployee.getEmployeeBankAccountCollection();

                if (eeBAs != null) {
                    for (EmployeeBankAccount currEEBA : eeBAs) {
                        assertEquals("EEBA Status", BankAccountStatus.Active, currEEBA.getStatusCd());
                    }
                }
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    private void assertServiceNotCanceled(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, ServiceCode pServiceCode) {
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(pSourceCompanyId, pSourceSystemCd);
        CompanyService companyService = CompanyService
                .findCompanyService(foundCompany, pServiceCode);

        Iterator<CompanyBankAccount> itCompanyBankAccounts = foundCompany.getCompanyBankAccountCollection().iterator();
        PayrollServices.commitUnitOfWork();

        assertFalse("Service Status: ", ServiceSubStatusCode.Cancelled == companyService.getStatusCd());

        while (itCompanyBankAccounts.hasNext()) {
            assertEquals("Bank Account Status", BankAccountStatus.Active, itCompanyBankAccounts.next().getStatusCd());
        }

        PayrollServices.beginUnitOfWork();
        foundCompany = Application.refresh(foundCompany);
        DomainEntitySet<Employee> employees = foundCompany.getDirectDepositEmployees();
        if (employees != null) {
            for (Employee currEmployee : employees) {
                assertEquals("Employee Status", EmployeeStatus.Active, currEmployee.getStatusCd());
                DomainEntitySet<EmployeeBankAccount> eeBAs = currEmployee.getEmployeeBankAccountCollection();

                if (eeBAs != null) {
                    for (EmployeeBankAccount currEEBA : eeBAs) {
                        assertEquals("EEBA Status", BankAccountStatus.Active, currEEBA.getStatusCd());
                    }
                }
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testCancelServices_CloudDDBP401k() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment, ServiceCode.ThirdParty401k);

        DataLoadServices.runOffload(PSPDate.getPSPTime());
        DataLoadServices.runACHTransactionProcessor();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceCore = PayrollServices.companyManager.deactivateService(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("deactivate dd service", cancelServiceCore);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyService companyService = company.getService(ServiceCode.DirectDeposit);
        assertEquals("canceled dd service", ServiceSubStatusCode.Cancelled, companyService.getStatusCd());
        companyService = company.getService(ServiceCode.BillPayment);
        assertEquals("canceled dd service", ServiceSubStatusCode.Cancelled, companyService.getStatusCd());

        DomainEntitySet<CompanyEventEmail> companyEventEmails = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.ServiceCancelledConfirmation1);
        assertEquals("CompanyEventEmail records", 2, companyEventEmails.size());
        DomainEntitySet<CompanyEventEmailParam> companyEventEmailParams = new DomainEntitySet<CompanyEventEmailParam>();
        for (CompanyEventEmail companyEventEmail : companyEventEmails) {
            companyEventEmailParams.addAll(companyEventEmail.getEmailParamForEmailEvent(EventEmailParamTypeCode.ServiceType));
        }
        assertEquals("CompanyEventEmailParam with ServiceType", 2, companyEventEmailParams.size());
        assertEquals("CompanyEventEmailParam with ServiceType Value - Direct Deposit", 1, companyEventEmailParams.find(CompanyEventEmailParam.Value().equalTo("Direct Deposit")).size());
        assertEquals("CompanyEventEmailParam with ServiceType Value - Direct Deposit for Vendors", 1, companyEventEmailParams.find(CompanyEventEmailParam.Value().equalTo("Direct Deposit for Vendors")).size());

        PayrollServices.rollbackUnitOfWork();

        // submit 401k payroll
        List<Employee> employees = DataLoadServices.addEEs(company, 1, false, true);
        List<CompanyPayrollItem> companyPayrollItems = DataLoadServices.addPayrollItems(company, PayrollItemCode.Compensation, PayrollItemCode.Tp401kEmployeeDeferral, PayrollItemCode.Tp401kEmployerMatch);

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = DataLoadServices.create401kPayrollRun(employees, companyPayrollItems);
        ProcessResult<PayrollRun> submitPayrollPR =
                PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit 401k Payroll", submitPayrollPR);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        cancelServiceCore = PayrollServices.companyManager.deactivateService(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.ThirdParty401k);
        PSP_PRAssert.assertSuccess("deactivate 401k service", cancelServiceCore);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyService = company.getService(ServiceCode.ThirdParty401k);
        assertEquals("canceled 401k service", ServiceSubStatusCode.Cancelled, companyService.getStatusCd());

        companyEventEmails = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.ServiceCancelledConfirmation1);
        assertEquals("CompanyEventEmail records", 3, companyEventEmails.size());
        companyEventEmailParams = new DomainEntitySet<CompanyEventEmailParam>();
        for (CompanyEventEmail companyEventEmail : companyEventEmails) {
            companyEventEmailParams.addAll(companyEventEmail.getEmailParamForEmailEvent(EventEmailParamTypeCode.ServiceType));
        }
        assertEquals("CompanyEventEmailParam with ServiceType", 3, companyEventEmailParams.size());
        assertEquals("CompanyEventEmailParam with ServiceType Value - Direct Deposit", 1, companyEventEmailParams.find(CompanyEventEmailParam.Value().equalTo("Direct Deposit")).size());
        assertEquals("CompanyEventEmailParam with ServiceType Value - Direct Deposit for Vendors", 1, companyEventEmailParams.find(CompanyEventEmailParam.Value().equalTo("Direct Deposit for Vendors")).size());
        assertEquals("CompanyEventEmailParam with ServiceType Value - Payroll", 1, companyEventEmailParams.find(CompanyEventEmailParam.Value().equalTo("Payroll")).size());

        PayrollServices.rollbackUnitOfWork();

        // submit cloud payroll
        employees = DataLoadServices.addEEs(company, 1, false, true);
        companyPayrollItems = DataLoadServices.addPayrollItems(company, PayrollItemCode.Compensation, PayrollItemCode.Tp401kEmployeeDeferral, PayrollItemCode.Tp401kEmployerMatch);

        PayrollServices.beginUnitOfWork();
        payrollRunDTO = DataLoadServices.create401kPayrollRun(employees, companyPayrollItems);
        submitPayrollPR =
                PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit 401k Payroll", submitPayrollPR);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        cancelServiceCore = PayrollServices.companyManager.deactivateService(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Cloud);
        PSP_PRAssert.assertSuccess("deactivate cloud service", cancelServiceCore);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyService = company.getService(ServiceCode.Cloud);
        assertEquals("canceled cloud service", ServiceSubStatusCode.Cancelled, companyService.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        DomainEntitySet<Employee> cloudEmployees = Employee.findCloudEmployees(company);
        for (Employee cloudEmployee : cloudEmployees) {
            assertEquals("employee inactive", EmployeeStatus.Active, cloudEmployee.getStatusCd());
        }
        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void cancelServiceCore_UnresolvedBankReturns_NoBalanceDue() {
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Company1Dataloader c1dl = new Company1Dataloader();
        Company company1 = c1dl.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = c1dl.persistPayrollRun(c1dl.getCompany1PayrollRunDTO(new DateDTO("2007-10-02")));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        // offload QBOE ER DB
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        // offload QBOE EE CR
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Load Transaction Returns for Company1 - 1234567
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> c1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        PayrollServices.commitUnitOfWork();
        DataLoadServices.returnTxns(c1FinTxns, "C04", "This is an NOC return");

        PayrollServices.beginUnitOfWork();
        company1 = PayrollServices.entityFinder.findById(Company.class, company1.getId());
        ProcessResult<CompanyService> cancelCompanyCore = PayrollServices.companyManager.updateServiceStatus(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Deactivate company", cancelCompanyCore);
        assertCompanyCanceled(company1.getSourceSystemCd(), company1.getSourceCompanyId());
    }

    @Test
    public void testCancelCompanyCancelsPendingBankAccountVerification() {
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Company1Dataloader c1dl = new Company1Dataloader();
        DataLoader dataloader = new DataLoader();
        // Create Company and CompanyBankAccount
        Company company1 = dataloader.persistCompany(c1dl.getCompany1());

        CompanyService ddCompanyService = dataloader.persistCompanyService(company1, c1dl.getCompany1Service());

        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(company1.getSourceSystemCd(), company1.getSourceCompanyId(),
                dataloader.getTestCompanyBankAccount(), true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        assertEquals("Company Bank Account Status", BankAccountStatus.PendingVerification, companyBankAccount.getStatusCd());
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        assertEquals("Employer Verification FTs", 2, verificationTransactions.size());
        for (FinancialTransaction verificationTransaction : verificationTransactions) {
            assertEquals("Employer Verification Debit Transaction type", TransactionType.findTransactionType(TransactionTypeCode.EmployerVerificationDebit), verificationTransaction.getTransactionType());
            assertEquals("Employer Verification Debit state", TransactionState.findTransactionState(TransactionStateCode.Created), verificationTransaction.getCurrentFinancialTransactionState().getTransactionState());
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelCompanyCore = PayrollServices.companyManager.updateServiceStatus(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled);
        assertSuccess(cancelCompanyCore);
        PayrollServices.commitUnitOfWork();

        assertCompanyCanceled(company1.getSourceSystemCd(), company1.getSourceCompanyId());

        PayrollServices.beginUnitOfWork();
        Application.refresh(companyBankAccount);
        assertEquals("Company Bank Account Status", BankAccountStatus.PendingVerification, companyBankAccount.getStatusCd()); //Todo check for BankAccountVerification
        verificationTransactions = companyBankAccount.getVerificationTransactions();
        assertEquals("Employer Verification FTs", 2, verificationTransactions.size());
        for (FinancialTransaction verificationTransaction : verificationTransactions) {
            assertEquals("Employer Verification Debit Transaction type", TransactionType.findTransactionType(TransactionTypeCode.EmployerVerificationDebit), verificationTransaction.getTransactionType());
            assertEquals("Employer Verification Debit state", TransactionState.findTransactionState(TransactionStateCode.Cancelled), verificationTransaction.getCurrentFinancialTransactionState().getTransactionState());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCancelTaxCompany() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax, ServiceCode.BillPayment,  ServiceCode.WorkersComp);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        Assert.assertEquals(ServiceSubStatusCode.Cancelled, company.getService(ServiceCode.Tax).getStatusCd());
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, company.getService(ServiceCode.DirectDeposit).getStatusCd());
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, company.getService(ServiceCode.Cloud).getStatusCd());
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, company.getService(ServiceCode.BillPayment).getStatusCd());
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, company.getService(ServiceCode.WorkersComp).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    private void assertCompanyCanceled(SourceSystemCode pSourceSystemCd, String pSourceCompanyId) {
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(pSourceCompanyId, pSourceSystemCd);
        Iterator<CompanyBankAccount> itCompanyBankAccounts = foundCompany.getCompanyBankAccountCollection().iterator();
        CompanyService companyService = CompanyService.findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        assertEquals("Service Status: ", ServiceSubStatusCode.Cancelled, companyService.getStatusCd());

        PayrollServices.beginUnitOfWork();
        foundCompany = Application.refresh(foundCompany);

        DomainEntitySet<Employee> employees = foundCompany.getDirectDepositEmployees();
        if (employees != null) {
            for (Employee currEmployee : employees) {
                assertEquals("Employee Status", EmployeeStatus.Active, currEmployee.getStatusCd());
                DomainEntitySet<EmployeeBankAccount> eeBAs = currEmployee.getEmployeeBankAccountCollection();
                if (eeBAs != null) {
                    for (EmployeeBankAccount currEEBA : eeBAs) {
                        assertEquals("EEBA Status", BankAccountStatus.Active, currEEBA.getStatusCd());
                    }
                }
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testReturnOnCancelledTaxCompanyPutsPaymentOnHold() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 18);
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 1, 19);

        DataLoadServices.cancelService(company, ServiceCode.Tax);

        DataLoadServices.setPSPDate(2012, 1, 20);
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);

        PayrollServices.beginUnitOfWork();
        CompanyEvent serviceStatusChangeEvent = CompanyEvent.findCompanyEvents(company, EventTypeCode.ServiceStatusChange).sort(CompanyEvent.EventTimeStamp().Descending()).getFirst();
        assertEquals("Cancelled", serviceStatusChangeEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldServiceStatus));

        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().find());
        assertEquals(TaxPaymentStatus.OnHold, payment.getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload();
        DataLoadServices.runACHTransactionProcessor();

        PayrollServices.beginUnitOfWork();
        serviceStatusChangeEvent = CompanyEvent.findCompanyEvents(company, EventTypeCode.ServiceStatusChange).sort(CompanyEvent.EventTimeStamp().Descending()).getFirst();
        assertEquals("Cancelled", serviceStatusChangeEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewServiceStatus));

        payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().find());
        assertEquals(TaxPaymentStatus.ReadyToSend, payment.getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();
    }

}
