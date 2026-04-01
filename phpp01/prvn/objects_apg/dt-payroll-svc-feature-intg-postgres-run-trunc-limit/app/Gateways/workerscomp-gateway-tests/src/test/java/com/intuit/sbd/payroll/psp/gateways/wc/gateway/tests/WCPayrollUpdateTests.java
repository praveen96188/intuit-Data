package com.intuit.sbd.payroll.psp.gateways.wc.gateway.tests;

import com.intuit.bp.wc.common.schema.WorkersCompSubscription;
import com.intuit.bp.wc.common.schema.WorkersCompSubscriptions;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedUnprocessedRequestTests;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.qbdt.billing.UsageOFXDataloader;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PaystubDTO;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.PayrollDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.manager.WorkersCompGatewayManager;
import com.intuit.sbd.payroll.psp.gateways.wc.util.WCUtil;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: afroza786
 * Date: 7/29/13
 * Time: 4:23 PM
 * Test related to paycheck edit functioanality..
 */
public class WCPayrollUpdateTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void shouldOnlyProcessSentIgnorePendingNew() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.WorkersComp);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            //DataLoadServices.addCompanyBankAccount(company);
        }



        // create paustub 1 and 2
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_1.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        Application.beginUnitOfWork();
        Expression<Employee> query = new Query<Employee>()
                .Where(Employee.Company().equalTo(company))
                .EagerLoad(Employee.MailingAddress(),
                           Employee.Company(),
                           Employee.Company().CompanyServiceSet());
        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        Employee employee = employees.getFirst();

        DomainEntitySet<Paycheck> pcs = Paycheck.findPaychecksBySourceEmployee(company, employee);
        Paycheck pc = pcs.get(0);

        DomainEntitySet<WorkersCompPaycheck> wcs = WorkersCompPaycheck.findByPaycheck(pc);

        wcs.get(0).markAsSent();

        Application.commitUnitOfWork();

        // a mod on paystub 1 & 2
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_2.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);


        wcs = WorkersCompPaycheck.findByPaycheck(pcs.get(0));
        assertEquals(wcs.get(0).getCurrentStateCd(), WorkersCompPaycheckStateCode.PendingEdit)  ;
        assertEquals(wcs.get(0).getPaycheckVersion(), 2)  ;
        assertNotNull("WorkerCompPaycheck Company null",wcs.get(0).getCompany());

        wcs = WorkersCompPaycheck.findByPaycheck(pcs.get(1));
        assertEquals(wcs.get(0).getCurrentStateCd(), WorkersCompPaycheckStateCode.PendingNew)  ;
        assertEquals(wcs.get(0).getPaycheckVersion(), 1)  ;




    }

    @Test
    public void shouldHandleCheckDelete(){




        String psid = String.valueOf(System.currentTimeMillis());

        Company    company = TestUtil.createCompanyEmployeesAndPayroll(psid);

        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();
        WorkersCompSubscription subscription = new WorkersCompSubscription();
        subscription.setPSID(psid);
        subscription.setActive(true);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
        subscriptions.getWorkersCompSubscription().add(subscription);
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
        CompanyService cloudV2Service = company.getService(ServiceCode.CloudV2);

        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, cloudV2Service.getStatusCd());


        //Now get one check and delete it....

        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();

        PayrollDTO dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        List<WorkersCompPaycheck> workersCompPaychecks= dto.getIncludedPaychecks(psid)    ;

        PayrollServices.beginUnitOfWork();
        PayrollServices.payrollManager.deletePaycheck(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                                      workersCompPaychecks.get(0).getPaycheck().getSourcePaycheckId(), null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<WorkersCompPaycheck> wcpcs = WorkersCompPaycheck.findByPaycheck(workersCompPaychecks.get(0).getPaycheck());
        junit.framework.Assert.assertEquals(WorkersCompPaycheckStateCode.Cancelled, wcpcs.get(0).getCurrentStateCd());
        assertNotNull("WorkerCompPaycheck Company null",wcpcs.get(0).getCompany());
        assertEquals("Comapny object does not match for paycheck and WCPaycheck",
                wcpcs.getFirst().getPaycheck().getCompany(),wcpcs.getFirst().getCompany());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void shouldChangeToPendingDeleteIfAlreadySend(){




        String psid = String.valueOf(System.currentTimeMillis());

        Company    company = TestUtil.createCompanyEmployeesAndPayroll(psid);

        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();
        WorkersCompSubscription subscription = new WorkersCompSubscription();
        subscription.setPSID(psid);
        subscription.setActive(true);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
        subscriptions.getWorkersCompSubscription().add(subscription);
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
        CompanyService cloudV2Service = company.getService(ServiceCode.CloudV2);

        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, cloudV2Service.getStatusCd());


        //Now get one check and delete it....

        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();

         PayrollDTO dto = manager.getPendingPaychecks(batches.get(0));
         assertNotNull(dto);
         List<WorkersCompPaycheck> workersCompPaychecks= dto.getIncludedPaychecks(psid)    ;


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<WorkersCompPaycheck> wcs = WorkersCompPaycheck.findByPaycheck(workersCompPaychecks.get(0).getPaycheck());
        assertNotNull("WorkerCompPaycheck Company null",wcs.get(0).getCompany());


        wcs.get(0).markAsSent();

        PayrollServices.payrollManager.deletePaycheck(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                                      wcs.get(0).getPaycheck().getSourcePaycheckId(), null);
        PayrollServices.commitUnitOfWork();
        DomainEntitySet<WorkersCompPaycheck> wcpcs = WorkersCompPaycheck.findByPaycheck(wcs.get(0).getPaycheck());
        junit.framework.Assert.assertEquals(WorkersCompPaycheckStateCode.PendingDelete, wcpcs.get(0).getCurrentStateCd());
        assertNotNull("WorkerCompPaycheck Company null",wcpcs.get(0).getCompany());

    }

    @Test
    public void shouldNotSendEditsForInactiveChecks() throws Exception {


        //**********************Data setup*******************************
        String psid = "99000123";
        SpcfCalendar beginDate = SpcfCalendar.createInstance(2012, 2, 10);
        SpcfCalendar endDate = SpcfCalendar.createInstance(2012, 2, 10);

        com.intuit.sbd.payroll.psp.domain.Employee employee = TestUtil.setupCompanyCreateEmployee(psid, SpcfCalendar.createInstance(PayrollServicesTest.BASE_YEAR, 2, 10), ServiceCode.WorkersComp);

        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        PaystubDTO paystubDto = DataLoadServices.createPaystubDto(employee, beginDate, endDate, paycheckDate, "1000.00");

        DomainEntitySet<Paycheck> pcs = Paycheck.findPaychecksBySourceEmployee(employee.getCompany(), employee);

        TestUtil.savePaystub(employee,pcs.get(0), paystubDto);

        Paycheck pc = pcs.get(0);
        pc.getWorkersCompPaycheck().markAsSent();
        pc.setStatus(PaycheckStatusCode.Inactive);
        Application.commitUnitOfWork();

        //******************************************test****************************************

        //edit pay stub paystubDto is modified....
        // the paystub data should be updated and also the data in WorkersComp should be modified to PendingEdit
        paystubDto.setPreTaxDeducts("2000.00");

        Application.beginUnitOfWork();

        pcs = Paycheck.findPaychecksBySourceEmployee(employee.getCompany(), employee);
        Paystub paystub = Paystub.findPaystub(pcs.get(0));
        PayrollServices.paystubManager.updatePaystub(pcs.get(0), employee, paystub, paystubDto);
        Application.commitUnitOfWork();

        //********************************Validate*********************

        DomainEntitySet<WorkersCompPaycheck> wcs = WorkersCompPaycheck.findByPaycheck(pcs.get(0));
        assertEquals(wcs.get(0).getCurrentStateCd(), WorkersCompPaycheckStateCode.Sent)  ;
        assertNotNull("WorkerCompPaycheck Company null",wcs.get(0).getCompany());




    }


    @Test
    public void deletePaychecksFromQBDTProcessorPendingDelete() throws Exception {

        //**********************Data setup*******************************
        String psid = "99000123";
        SpcfCalendar beginDate = SpcfCalendar.createInstance(2012, 2, 10);
        SpcfCalendar endDate = SpcfCalendar.createInstance(2012, 2, 10);

        com.intuit.sbd.payroll.psp.domain.Employee employee = TestUtil.setupCompanyCreateEmployee(psid, SpcfCalendar.createInstance(2011, 7, 22), ServiceCode.WorkersComp);

        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        PaystubDTO paystubDto = DataLoadServices.createPaystubDto(employee, beginDate, endDate, paycheckDate, "1000.00");

        DomainEntitySet<Paycheck> pcs = Paycheck.findPaychecksBySourceEmployee(employee.getCompany(), employee);

        TestUtil.savePaystub(employee,pcs.get(0), paystubDto);

        Paycheck pc = pcs.get(0);
        pc.getWorkersCompPaycheck().markAsSent();

        String mLicenceIdCloud = "590285459983250";
        String mEoc = "389857";

        Application.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(employee.getCompany(), mLicenceIdCloud, mEoc, EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_LOWBASE, null);

        //******************************************test****************************************
        Application.beginUnitOfWork();
        Company c = Company.findCompany(psid, SourceSystemCode.QBDT) ;
        EntitlementUnit primaryEntitlementUnit = c.getActivePrimaryEntitlementUnit();
        Application.commitUnitOfWork();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        DataLoadServices.setPSPDate(2011, 7, 22);
        OFX request = new UsageOFXDataloader().createOFX(psid, UsageOFXDataloader.OFX_NULL_STRING, employee.getCompany().getFedTaxId(), subscriptionNumber, pc.getSourcePaycheckId());
        QBDTTestHelper.processOFXRequestSuccess(request);


        DomainEntitySet<WorkersCompPaycheck> wcs = WorkersCompPaycheck.findByPaycheck(pc);
        assertEquals(WorkersCompPaycheckStateCode.PendingDelete, wcs.get(0).getCurrentStateCd())  ;
        assertNotNull("WorkerCompPaycheck Company null",wcs.get(0).getCompany());




    }


    @Test
    public void deletePaychecksFromQBDTProcessorCancelled() throws Exception {

        //**********************Data setup*******************************
        String psid = "99000123";
        SpcfCalendar beginDate = SpcfCalendar.createInstance(2012, 2, 10);
        SpcfCalendar endDate = SpcfCalendar.createInstance(2012, 2, 10);

        com.intuit.sbd.payroll.psp.domain.Employee employee = TestUtil.setupCompanyCreateEmployee(psid, SpcfCalendar.createInstance(2011, 7, 22), ServiceCode.WorkersComp);

        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        PaystubDTO paystubDto = DataLoadServices.createPaystubDto(employee, beginDate, endDate, paycheckDate, "1000.00");

        DomainEntitySet<Paycheck> pcs = Paycheck.findPaychecksBySourceEmployee(employee.getCompany(), employee);

        TestUtil.savePaystub(employee,pcs.get(0), paystubDto);

        Paycheck pc = pcs.get(0);


        String mLicenceIdCloud = "590285459983250";
        String mEoc = "389857";

        Application.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(employee.getCompany(), mLicenceIdCloud, mEoc, EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_LOWBASE, null);

        //******************************************test****************************************
        Application.beginUnitOfWork();
        Company c = Company.findCompany(psid, SourceSystemCode.QBDT) ;
        EntitlementUnit primaryEntitlementUnit = c.getActivePrimaryEntitlementUnit();
        Application.commitUnitOfWork();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        DataLoadServices.setPSPDate(2011, 7, 22);
        OFX request = new UsageOFXDataloader().createOFX(psid, UsageOFXDataloader.OFX_NULL_STRING, employee.getCompany().getFedTaxId(), subscriptionNumber, pc.getSourcePaycheckId());
        QBDTTestHelper.processOFXRequestSuccess(request);


        DomainEntitySet<WorkersCompPaycheck> wcs = WorkersCompPaycheck.findByPaycheck(pc);
        assertEquals(WorkersCompPaycheckStateCode.Cancelled, wcs.get(0).getCurrentStateCd())  ;
        assertNotNull("WorkerCompPaycheck Company null",wcs.get(0).getCompany());




    }










}
