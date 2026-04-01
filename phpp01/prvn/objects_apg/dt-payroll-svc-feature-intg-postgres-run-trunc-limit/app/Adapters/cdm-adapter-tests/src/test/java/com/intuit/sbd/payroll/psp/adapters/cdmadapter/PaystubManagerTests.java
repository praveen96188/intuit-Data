package com.intuit.sbd.payroll.psp.adapters.cdmadapter;

import com.intuit.ems.dataservice.v1.exception.AccessDeniedException;
import com.intuit.ems.dataservice.v1.exception.DataServiceException;
import com.intuit.ems.dataservice.v1.exception.ResourceNotFoundException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.factories.CdmFactory;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.PaystubFinder;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers.PayrollEmployeeManager;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers.PaystubManager;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.schema.ems.v3.PayrollEmployee;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PaystubManagerTests {
    @Before
    public void startUp() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        Application.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.VMP_PAYCHECK_PERIOD_LOOK_BACK_MONTHS, "3");
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.VMP_SIZE_THROTTLING_VALUE, "20");
        Application.commitUnitOfWork();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 1, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
    }
    /* Paystub Delete Vmp Data tests */
    @Test
    public void testDeleteVmpDataForOneEmployee(){

        String psid = "99000123";
        String companyRealmId = "654321";

        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);

        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2015, 1, 1);
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Paystub foundPaystub = PaystubFinder.findPaystubForCompany(companyRealmId, paystub.getId().toString());
        assertEquals(paystub, foundPaystub);
        assertNotNull("Paystub compnay null",foundPaystub.getCompany());
        assertEquals(foundPaystub.getPaycheck().getCompany().getSourceCompanyId(),
                foundPaystub.getCompany().getSourceCompanyId());

        PstubMsg foundPstubMsg = foundPaystub.getPstubMsgCollection().getFirst();
        assertNotNull("Paystub message null", foundPstubMsg);
        assertNotNull("Paystub message company null",foundPstubMsg.getCompany());
        assertEquals(foundPaystub.getCompany(),foundPstubMsg.getCompany());

        PstubDDItem foundPstubDDItem = foundPaystub.getPstubDDItemCollection().getFirst();
        assertNotNull("Paystub DD item null",foundPstubDDItem);
        assertNotNull("Paystub DD item null", foundPstubDDItem.getCompany());
        assertEquals(foundPaystub.getCompany(),foundPstubDDItem.getCompany());



        PstubPayItem foundPayItem = foundPaystub.getPstubPayItemCollection().getFirst();
        assertNotNull(foundPayItem);
        assertNotNull(foundPayItem.getCompany());
        assertEquals(foundPaystub.getCompany(),foundPayItem.getCompany());

        PstubPaidTimeoffItem foundPstubPaidTimeoffItem = foundPaystub.getPstubPaidTimeoffItemCollection().getFirst();
        assertNotNull("PstubPaidTimeoffItem null",foundPstubPaidTimeoffItem);
        assertNotNull("PstubPaidTimeOffItem company null",foundPstubPaidTimeoffItem.getCompany());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollServices.paystubManager.deletePaystub(company);
        Application.commitUnitOfWork();

        //delete the paystub present

        foundPaystub = VmpTestUtil.findPaystubForCompany(companyRealmId,paystub.getId().toString());

        assertNull(foundPaystub);
     }

    @Test
    public void testCheckDeleteVmpDataOrder(){
        String psid = "99000123";
        String companyRealmId = "654321";

        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);

        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2015, 1, 1);
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Paystub foundPaystub = PaystubFinder.findPaystubForCompany(companyRealmId, paystub.getId().toString());
        assertEquals(paystub, foundPaystub);
        assertNotNull("Paystub compnay null",foundPaystub.getCompany());
        assertEquals(foundPaystub.getPaycheck().getCompany().getSourceCompanyId(),
                foundPaystub.getCompany().getSourceCompanyId());

        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollServices.paystubManager.deletePaystub(company);
        Application.commitUnitOfWork();

        //delete the paystub present
        PstubMsg foundPstubMsg = VmpTestUtil.findPaystubForMsg( paystub.getId().toString());
        assertNull(foundPstubMsg);

        PstubDDItem foundPstubDDItem = VmpTestUtil.findPstubDDItemForCompany(paystub.getId().toString());
        assertNull(foundPstubDDItem);


        PstubPayItem foundPayItem = VmpTestUtil.findPayItem(paystub.getId().toString());
        assertNull(foundPayItem);

        PstubPaidTimeoffItem foundPstubPaidTimeoffItem = VmpTestUtil.findPstubPaidTimeoffItem( paystub.getId().toString());
        assertNull(foundPstubPaidTimeoffItem);

        foundPaystub = VmpTestUtil.findPaystubForCompany(companyRealmId,paystub.getId().toString());
        assertNull(foundPaystub);

    }

    /* check for multiple company data */
    @Test
    public void testCheckDeleteVmpWithMultipleCompany(){
        String psid = "99000124";
        String companyRealmId = "654322";

        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);

        Application.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        company1.setIAMRealmId(companyRealmId);
        Application.save(company1);

        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2015, 1, 1);
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Paystub foundPaystub = PaystubFinder.findPaystubForCompany(companyRealmId, paystub.getId().toString());
        assertEquals(paystub, foundPaystub);
        assertNotNull("Paystub compnay null",foundPaystub.getCompany());
        assertEquals(foundPaystub.getPaycheck().getCompany().getSourceCompanyId(),
                foundPaystub.getCompany().getSourceCompanyId());

        PayrollServices.commitUnitOfWork();

        psid = "99000125";
        companyRealmId = "654323";
        String ein = "900900001";

        employee = VmpTestUtil.setupCompanyWithEinCreateEmployee(psid, ein);

        Application.beginUnitOfWork();
        Company company2 = Company.findCompany(psid, SourceSystemCode.QBDT);
        company2.setIAMRealmId(companyRealmId);
        Application.save(company2);

        paycheckDate = SpcfCalendar.createInstance(2015, 1, 1);
        paystub = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        Application.commitUnitOfWork();



        Application.beginUnitOfWork();
        PayrollServices.paystubManager.deletePaystub(company1);
        Application.commitUnitOfWork();


        //Check if Paystub for company two is present
        PayrollServices.beginUnitOfWork();
        foundPaystub = PaystubFinder.findPaystubForCompany(companyRealmId, paystub.getId().toString());
        assertEquals(paystub, foundPaystub);

        PayrollServices.commitUnitOfWork();

    }

    @After
    public void shutdown() {
        PayrollServicesTest.afterEachTest();
    }

    //When VMP service is cancelled we should not get back any paystubs, instead an access denied exception
    @Test
    public void testCompanyVmpServiceCancelled() {
        String psid = "123456";
        VmpTestUtil.setupCompanyCreateEmployee(psid);

        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        String ssn = "123456789";
        String consumerRealmId = "123456";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();

        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());

        //Cancel company service
        Application.beginUnitOfWork();
        Application.refresh(company);
        PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.ViewMyPaycheck, ServiceSubStatusCode.Cancelled);
        Application.commitUnitOfWork();

        //All the paystub APIs require VMP service on, test each
        PaystubManager paystubManager = new PaystubManager();
        try {
            paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), null, null, 0, 1);
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            assertEquals(DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK, accessDeniedException.getErrorCode());
        }

        try {
            paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), null, null, 0, 1);
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            assertEquals(DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK, accessDeniedException.getErrorCode());
        }

        try {
            paystubManager.getPaystubForCompany(companyRealmId, paystub.getId().toString());
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            assertEquals(DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK, accessDeniedException.getErrorCode());
        }

        try {
            paystubManager.getPaystubForEmployee(consumerRealmId, paystub.getId().toString());
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            assertEquals(DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK, accessDeniedException.getErrorCode());
        }
    }

    @Test
    public void testIsViewingPaystubDisabled() {
        String psid = "123456";
        VmpTestUtil.setupCompanyCreateEmployee(psid);

        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        String ssn = "123456789";
        String consumerRealmId = "123456";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();

        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());

        //Disable access for this employee
        PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
        PayrollEmployee payrollEmployee = new PayrollEmployee();
        payrollEmployee.setViewingPaystubDisabled(true);
        payrollEmployeeManager.updateEmployeeIsViewingPaystubDisabled(companyRealmId, employee.getId().toString(), payrollEmployee);

        PaystubManager paystubManager = new PaystubManager();

        //These employee paystub APIs should throw an error when IsViewPaystubDisabled set to true
        try {
            paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), null, null, 0, 1);
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            assertEquals(DataServiceException.ERRNUM_EMPLOYEE_VIEWING_PAYSTUBS_DISABLED, accessDeniedException.getErrorCode());
        }

        try {
            paystubManager.getPaystubForEmployee(consumerRealmId, paystub.getId().toString());
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            assertEquals(DataServiceException.ERRNUM_EMPLOYEE_VIEWING_PAYSTUBS_DISABLED, accessDeniedException.getErrorCode());
        }


        //These company APIs should not throw an error when IsViewPaystubDisabled set to true, the employer/admin can still see
        paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), null, null, 0, 1);
        paystubManager.getPaystubForCompany(companyRealmId, paystub.getId().toString());
    }

    @Test
     public void testStartAndEndPaycheckDates() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 7, 25, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        String startDate = null;
        String endDate = null;
        Map<String, String> dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", "2016-04-01", dates.get("paycheckStartDate"));
        assertEquals("endDate", "", dates.get("paycheckEndDate"));

        startDate = "2016-07-01";
        endDate = "2016-08-01";
        dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", "2016-07-01", dates.get("paycheckStartDate"));
        assertEquals("endDate", "2016-08-01", dates.get("paycheckEndDate"));

        startDate = null;
        endDate = "2016-06-01";
        dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", "2016-03-01", dates.get("paycheckStartDate"));
        assertEquals("endDate", "2016-06-01", dates.get("paycheckEndDate"));

        startDate = "2016-01-01";
        endDate = null;
        dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", "2016-01-01", dates.get("paycheckStartDate"));
        assertEquals("sendDate", null, dates.get("paycheckEndDate"));

        dates = PaystubManager.getStartAndEndPaycheckDates("", "");
        assertEquals("startDate", "2016-04-01", dates.get("paycheckStartDate"));
        assertEquals("sendDate", "", dates.get("paycheckEndDate"));
        dates = PaystubManager.getStartAndEndPaycheckDates(null, "");
        assertEquals("startDate", "2016-04-01", dates.get("paycheckStartDate"));
        assertEquals("sendDate", "", dates.get("paycheckEndDate"));
        dates = PaystubManager.getStartAndEndPaycheckDates("", null);
        assertEquals("startDate", "2016-04-01", dates.get("paycheckStartDate"));
        assertEquals("sendDate", "", dates.get("paycheckEndDate"));

        startDate = "2016-01-01";
        endDate = null;
        dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", "2016-01-01", dates.get("paycheckStartDate"));
        assertEquals("sendDate", null, dates.get("paycheckEndDate"));

        startDate = "";
        endDate = "2016-04-01";
        dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", "2016-01-01", dates.get("paycheckStartDate"));
        assertEquals("sendDate", "2016-04-01", dates.get("paycheckEndDate"));

        Application.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.VMP_PAYCHECK_PERIOD_LOOK_BACK_MONTHS, "25");
        Application.commitUnitOfWork();

        startDate = null;
        endDate = null;
        dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", "2014-06-01", dates.get("paycheckStartDate"));
        assertEquals("endDate", "", dates.get("paycheckEndDate"));

        startDate = "2016-07-01";
        endDate = "2016-08-01";
        dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", "2016-07-01", dates.get("paycheckStartDate"));
        assertEquals("endDate", "2016-08-01", dates.get("paycheckEndDate"));

        startDate = null;
        endDate = "2016-06-01";
        dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", "2014-05-01", dates.get("paycheckStartDate"));
        assertEquals("endDate", "2016-06-01", dates.get("paycheckEndDate"));

        startDate = "2016-01-01";
        endDate = null;
        dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", "2016-01-01", dates.get("paycheckStartDate"));
        assertEquals("sendDate", null, dates.get("paycheckEndDate"));

        Application.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.VMP_PAYCHECK_PERIOD_LOOK_BACK_MONTHS, "0");
        Application.commitUnitOfWork();
        startDate = "2016-07-01";
        endDate = "2016-08-01";
        dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", "2016-07-01", dates.get("paycheckStartDate"));
        assertEquals("endDate", "2016-08-01", dates.get("paycheckEndDate"));

        startDate = null;
        endDate = null;
        dates = PaystubManager.getStartAndEndPaycheckDates(startDate, endDate);
        assertEquals("startDate", null, dates.get("paycheckStartDate"));
        assertEquals("endDate", null, dates.get("paycheckEndDate"));
    }
    @Test
    public void testPaystubWithDefaultStartEndDate() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 1, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        String psid = "123456";
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        String ssn = "123456789";
        String consumerRealmId = "123456";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        assertNotNull("Paystub compnay null",paystub.getCompany());
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());
        PaystubManager paystubManager = new PaystubManager();
        //These employee paystub APIs should throw an error when IsViewPaystubDisabled set to true
        List<com.intuit.schema.ems.v3.Paystub> paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), null, null, 0, 100);
        assertEquals("Get by consumer realm id",1,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), null, null, 0, 100);
        assertEquals("Get by company realm id",1,paystbList.size());
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 5, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        try {
            paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), null, null, 0, 100);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            paystbList = new ArrayList<com.intuit.schema.ems.v3.Paystub>();
            assertEquals(ResourceNotFoundException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND, resourceNotFoundException.getErrorCode());
        }
        assertEquals("Get by consumer realm id",0,paystbList.size());
        try {
            paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), null, null, 0, 100);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            paystbList = new ArrayList<com.intuit.schema.ems.v3.Paystub>();
            assertEquals(ResourceNotFoundException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND, resourceNotFoundException.getErrorCode());
        }
        assertEquals("Get by company realm id",0,paystbList.size());
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 5, 1));
        Application.commitUnitOfWork();
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), null, null, 0, 100);
        assertEquals("Get by consumer realm id",1,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), null, null, 0, 100);
        assertEquals("Get by company realm id",1,paystbList.size());
    }
    @Test
    public void testPaystubWithFuturePaycheckDate() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 1, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        String psid = "123456";
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        String ssn = "123456789";
        String consumerRealmId = "123456";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 3, 2));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());
        PaystubManager paystubManager = new PaystubManager();
        //These employee paystub APIs should throw an error when IsViewPaystubDisabled set to true
        List<com.intuit.schema.ems.v3.Paystub> paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), null, null, 0, 100);
        assertEquals("Get by consumer realm id",1,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), null, null, 0, 100);
        assertEquals("Get by company realm id",1,paystbList.size());
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 5, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), null, null, 0, 100);
        assertEquals("Get by consumer realm id",1,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), null, null, 0, 100);
        assertEquals("Get by company realm id",1,paystbList.size());
    }
    @Test
    public void testPaystubWithValuesForStartEndDate() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 1, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        String psid = "123456";
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        String ssn = "123456789";
        String consumerRealmId = "123456";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());
        PaystubManager paystubManager = new PaystubManager();
        //These employee paystub APIs should throw an error when IsViewPaystubDisabled set to true
        List<com.intuit.schema.ems.v3.Paystub> paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "2012-10-01", "2013-01-31", 0, 100);
        assertEquals("Get by consumer realm id",1,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "2012-10-01", "2013-01-31", 0, 100);
        assertEquals("Get by company realm id",1,paystbList.size());
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 5, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        try {
            paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "2012-10-01", "2012-12-31", 0, 100);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            paystbList = new ArrayList<com.intuit.schema.ems.v3.Paystub>();
            assertEquals(ResourceNotFoundException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND, resourceNotFoundException.getErrorCode());
        }
        assertEquals("Get by consumer realm id",0,paystbList.size());
        try {
            paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "2012-10-01", "2012-12-31", 0, 100);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            paystbList = new ArrayList<com.intuit.schema.ems.v3.Paystub>();
            assertEquals(ResourceNotFoundException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND, resourceNotFoundException.getErrorCode());
        }
        assertEquals("Get by company realm id",0,paystbList.size());
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 5, 25));
        Application.commitUnitOfWork();
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "2013-01-01", "2013-05-31", 0, 100);
        assertEquals("Get by consumer realm id",2,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "2013-01-01", "2013-05-31", 0, 100);
        assertEquals("Get by company realm id",2,paystbList.size());
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "2013-01-01", null, 0, 100);
        assertEquals("Get by consumer realm id",2,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "2013-01-01", null, 0, 100);
        assertEquals("Get by company realm id",2,paystbList.size());
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), null,"2013-05-31", 0, 100);
        assertEquals("Get by consumer realm id",1,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), null,"2013-05-31", 0, 100);
        assertEquals("Get by company realm id",1,paystbList.size());
    }
    @Test
    public void testNullValuesForLog() {
            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 1, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
            String psid = "123456";
            VmpTestUtil.setupCompanyCreateEmployee(psid);
            //Setup a company realm
            Application.beginUnitOfWork();
            String companyRealmId = "123456";
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            company.setIAMRealmId(companyRealmId);
            Application.save(company);
            Application.commitUnitOfWork();

            String ssn = "123456789";
            String consumerRealmId = "123456";
            Employee employee = VmpTestUtil.createEmployee(ssn, company);

            Application.beginUnitOfWork();
            Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
            Application.commitUnitOfWork();
            VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());
            PaystubManager paystubManager = new PaystubManager();
            //These employee paystub APIs should throw an error when IsViewPaystubDisabled set to true
        try {
            List<com.intuit.schema.ems.v3.Paystub> paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(null, null, null, null, 0, 100);
        } catch (ResourceNotFoundException resourceNotFoundException) {

        }
        try {
            List<com.intuit.schema.ems.v3.Paystub> paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(null, null, null, null, 0, 100);
        } catch (ResourceNotFoundException resourceNotFoundException) {

        }
    }
    @Test
    public void testPaystubWithValuesForStartEndDateWithMorePaystubs() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 7, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        String psid = "123456";
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        String ssn = "123456789";
        String consumerRealmId = "123456";
        String ssn2 = "123456889";
        String consumerRealmId2 = "123457";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);
        Employee employee2 = VmpTestUtil.createEmployee(ssn2, company);

        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 6, 25));
        VmpTestUtil.createPaystub(employee2, "2000.00", SpcfCalendar.createInstance(2013, 7, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId2, ssn2, "iamEmailAddress@intuit.com", "2000.00", employee2.getLastName());

        PaystubManager paystubManager = new PaystubManager();
        //These employee paystub APIs should throw an error when IsViewPaystubDisabled set to true
        List<com.intuit.schema.ems.v3.Paystub> paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "2013-06-01", "2013-07-30", 0, 100);
        assertEquals("Get by consumer realm id",1,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "2013-06-01", "2013-07-30", 0, 100);
        assertEquals("Get by company realm id",1,paystbList.size());

        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId2, employee2.getId().toString(), null, null, 0, 100);
        assertEquals("Get by consumer realm id",1,paystbList.size());

        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId2, employee2.getId().toString(), null, null, 0, 500);
        assertEquals("Get by consumer realm id",1,paystbList.size());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 7, 3, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Application.beginUnitOfWork();
        int day =3;
        int month =7;
        int year =2013;
        for(int i=0;i< 230;i++) {
            VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(year, month, day++));
            if(day == 29){
                month++;
                day =1;
            }
            if(month == 13){
                year++;
                month =1;
                day =1;
            }
        }
        VmpTestUtil.createPaystub(employee2, "2500.00", SpcfCalendar.createInstance(2013, 12, 31));
        Application.commitUnitOfWork();
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), null, null, 0, 100);
        assertEquals("Get by consumer realm id",100,paystbList.size());

        paystbList = paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), null, null, 0, 500);
        assertEquals("Get by consumer realm id",231,paystbList.size());


        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), null,null, 0, 500);
        assertEquals("Get by company realm id",231,paystbList.size());

        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId2, employee2.getId().toString(), null, null, 0, 100);
        assertEquals("Get by consumer realm id",2,paystbList.size());

        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId2, employee2.getId().toString(), null, null, 0, 500);
        assertEquals("Get by consumer realm id",2,paystbList.size());

        SpcfCalendar nextDate = SpcfCalendar.createInstance(2013, 7, 3, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        nextDate.addDays(230);
        DataLoadServices.setPSPDate(nextDate);
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), null, null, 0, 500);
        assertEquals("Get by consumer realm id",120,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), null,null, 0, 500);
        assertEquals("Get by company realm id",120,paystbList.size());


        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId2, employee2.getId().toString(), null, null, 0, 100);
        assertEquals("Get by consumer realm id",1,paystbList.size());

        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId2, employee2.getId().toString(), null, null, 0, 500);
        assertEquals("Get by consumer realm id",1,paystbList.size());

        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "2013-07-01", "2014-03-31", 0, 500);
        assertEquals("Get by consumer realm id",230,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(),"2013-07-01", "2014-03-31", 0, 500);
        assertEquals("Get by company realm id",230,paystbList.size());

        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId2, employee2.getId().toString(),  "2013-07-01", "2014-03-31", 0, 500);
        assertEquals("Get by consumer realm id",2,paystbList.size());

        paystbList = paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee2.getId().toString(),  "2013-07-01", "2014-03-31", 0, 500);
        assertEquals("Get by consumer realm id",2,paystbList.size());

    }
    @Test
    public void testPaystubWithSizeParamThrottling() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 7, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        String psid = "123456";
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        String ssn = "123456789";
        String consumerRealmId = "123456";
        String ssn2 = "123456889";
        String consumerRealmId2 = "123457";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);
        Employee employee2 = VmpTestUtil.createEmployee(ssn2, company);

        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 6, 25));
        VmpTestUtil.createPaystub(employee2, "2000.00", SpcfCalendar.createInstance(2013, 7, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId2, ssn2, "iamEmailAddress@intuit.com", "2000.00", employee2.getLastName());

        PaystubManager paystubManager = new PaystubManager();
        //These employee paystub APIs should throw an error when IsViewPaystubDisabled set to true
        List<com.intuit.schema.ems.v3.Paystub> paystbList = null;
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 7, 3, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Application.beginUnitOfWork();
        int day =3;
        int month =7;
        int year =2013;
        for(int i=0;i< 230;i++) {
            VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(year, month, day++));
            if(day == 29){
                month++;
                day =1;
            }
            if(month == 13){
                year++;
                month =1;
                day =1;
            }
        }
        VmpTestUtil.createPaystub(employee2, "2500.00", SpcfCalendar.createInstance(2013, 12, 31));
        Application.commitUnitOfWork();
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "", "", 0, 500);
        assertEquals("Get by consumer realm id",231,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "", "", 0, 500);
        assertEquals("Get by company realm id",231,paystbList.size());

        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "", "", 10, 20);
        assertEquals("Get by consumer realm id",20,paystbList.size());
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "", "", 20, 20);
        assertEquals("Get by consumer realm id",20,paystbList.size());

        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "","", 20, 10);
        assertEquals("Get by company realm id",10,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "","", 30, 10);
        assertEquals("Get by company realm id",10,paystbList.size());

        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId2, employee2.getId().toString(),  "2013-07-01", "2014-03-31", 1, 20);
        assertEquals("Get by consumer realm id",1,paystbList.size());

        paystbList = paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee2.getId().toString(),  "2013-07-01", "2014-03-31", 1, 20);
        assertEquals("Get by consumer realm id",1,paystbList.size());

        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "2013-07-01", "2013-07-27", 20, 100);
        assertEquals("Get by consumer realm id",5,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(),"2013-07-01", "2013-07-27", 20, 100);
        assertEquals("Get by company realm id",5,paystbList.size());

        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "2013-07-01", "2014-03-31", 20, 100);
        assertEquals("Get by consumer realm id",100,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(),"2013-07-01", "2014-03-31", 20, 100);
        assertEquals("Get by company realm id",100,paystbList.size());


        SpcfCalendar nextDate = SpcfCalendar.createInstance(2014, 6, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(nextDate);
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "", "", 0, 20);
        assertEquals("Get by consumer realm id",20,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "", "", 0, 20);
        assertEquals("Get by consumer realm id",20,paystbList.size());
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "", "", 10, 15);
        assertEquals("Get by consumer realm id",15,paystbList.size());
        paystbList= paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "", "", 10, 15);
        assertEquals("Get by consumer realm id",15,paystbList.size());
        paystbList = paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "", "", 0, 30);
        assertEquals("Get by company realm id",8,paystbList.size());
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "", "", 0, 30);
        assertEquals("Get by consumer realm id",8,paystbList.size());


        nextDate = SpcfCalendar.createInstance(2014, 7, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(nextDate);
        try {
            paystbList = paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "", "", 0, 30);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            paystbList = new ArrayList<com.intuit.schema.ems.v3.Paystub>();
            assertEquals(ResourceNotFoundException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND, resourceNotFoundException.getErrorCode());
        }
        assertEquals("Get by consumer realm id",0,paystbList.size());
        try {
            paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "", "", 0, 30);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            paystbList = new ArrayList<com.intuit.schema.ems.v3.Paystub>();
            assertEquals(ResourceNotFoundException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND, resourceNotFoundException.getErrorCode());
        }
        assertEquals("Get by consumer realm id",0,paystbList.size());

        Application.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.VMP_SIZE_THROTTLING_VALUE, "30");
        Application.commitUnitOfWork();
        paystbList = paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "", "", 0, 30);
        assertEquals("Get by company realm id",30,paystbList.size());
        paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "", "", 0, 30);
        assertEquals("Get by consumer realm id",30,paystbList.size());

        try {
            paystbList = paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(companyRealmId, employee.getId().toString(), "", "", 0, 31);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            paystbList = new ArrayList<com.intuit.schema.ems.v3.Paystub>();
            assertEquals(ResourceNotFoundException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND, resourceNotFoundException.getErrorCode());
        }
        assertEquals("Get by consumer realm id",0,paystbList.size());
        try {
            paystbList = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(consumerRealmId, employee.getId().toString(), "", "", 0, 31);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            paystbList = new ArrayList<com.intuit.schema.ems.v3.Paystub>();
            assertEquals(ResourceNotFoundException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND, resourceNotFoundException.getErrorCode());
        }
        assertEquals("Get by consumer realm id",0,paystbList.size());

    }
    @Test
    public void testStateTaxIdInVMPPaystub() {
        String psid = "99000123";
        String companyRealmId = "654321";

        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);

        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2015, 1, 1);
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Paystub foundPaystub = PaystubFinder.findPaystubForCompany(companyRealmId, paystub.getId().toString());
        PayrollServices.rollbackUnitOfWork();
        assertEquals(paystub, foundPaystub);
        assertNotNull("Paystub compnay null",foundPaystub.getCompany());
        assertEquals(foundPaystub.getPaycheck().getCompany().getSourceCompanyId(),
                foundPaystub.getCompany().getSourceCompanyId());
        com.intuit.schema.ems.v3.Paystub cdmPaystub = null;
        cdmPaystub = CdmFactory.createPaystub(paystub);
        String agencyId = cdmPaystub.getEmployerInfo().getStateTaxId().get(0).getAgencyId();

        assertNotNull(agencyId);

    }
}
