package com.intuit.sbd.payroll.psp.adapters.cdmadapter;

import com.intuit.ems.dataservice.v1.exception.ResourceNotFoundException;
import com.intuit.ems.dataservice.v1.resource.EmployerPreferenceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.PaystubFinder;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers.PayrollCompanyManager;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PaystubFinderTests {
    private static String psid = "99000123";
    private static String consumerRealmId = "123456";
    private static String companyRealmId = "654321";

    @Before
    public void startUp() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void shutdown() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testFindPaystubForEmployee() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        Paystub foundPaystub = PaystubFinder.findPaystubForEmployee(consumerRealmId, paystub.getId().toString());
        Assert.assertEquals(paystub, foundPaystub);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testFindPaystubForEmployeeBadId() {
        //Try a bad paystub id
        PaystubFinder.findPaystubForEmployee(consumerRealmId, "00000000-0000-0000-0000-000000000000");
    }

    @Test
    public void testFindPaystubForCompany() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        Application.commitUnitOfWork();
        Paystub foundPaystub = PaystubFinder.findPaystubForCompany(companyRealmId, paystub.getId().toString());
        Assert.assertEquals(paystub, foundPaystub);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testFindPaystubForCompanyBadId() {
        //Try a bad paystub id
        PaystubFinder.findPaystubForCompany(companyRealmId, "00000000-0000-0000-0000-000000000000");
    }

    @Test
    public void testFindPaystubsInDateRange() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        Paystub paystub1 = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        SpcfCalendar laterPaycheckDate = SpcfCalendar.createInstance(2013, 1, 10);
        Paystub paystub2 = VmpTestUtil.createPaystub(employee, "1000.00", laterPaycheckDate);
        SpcfCalendar evenLaterPaycheckDate = SpcfCalendar.createInstance(2013, 1, 15);
        Paystub paystub3 = VmpTestUtil.createPaystub(employee, "1000.00", evenLaterPaycheckDate);
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        String checkDateStart = "2013-01-01";
        String checkDateEnd = "2013-01-10";
        Application.beginUnitOfWork();
        List<Paystub> paystubs = PaystubFinder.findPaystubsByConsumerRealmAndEmployeeId(consumerRealmId, employee.getId().toString(),checkDateStart, checkDateEnd, 0, 10);
        Assert.assertEquals(2, paystubs.size());
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub1));
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub2));
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testFindPaystubsByConsumerRealmAndEmployeeId() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        List<Employee> employees = DataLoadServices.addEEs(employee.getCompany(), 1);
        //This employee will share the same consumer realm id as employee but have a different id
        Employee otherEmployee = employees.get(0);
        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        Paystub paystub1 = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        SpcfCalendar laterPaycheckDate = SpcfCalendar.createInstance(2013, 1, 10);
        Paystub paystub2 = VmpTestUtil.createPaystub(employee, "1000.00", laterPaycheckDate);
        SpcfCalendar evenLaterPaycheckDate = SpcfCalendar.createInstance(2013, 1, 15);
        VmpTestUtil.createPaystub(employee, "1000.00", evenLaterPaycheckDate);

        VmpTestUtil.createPaystub(otherEmployee, "1000.00", SpcfCalendar.createInstance(2013, 1, 3));
        VmpTestUtil.createPaystub(otherEmployee, "1000.00", SpcfCalendar.createInstance(2013, 1, 12));
        VmpTestUtil.createPaystub(otherEmployee, "1000.00", SpcfCalendar.createInstance(2013, 1, 17));
        Application.commitUnitOfWork();
        //Associating both employees with same consumer realm
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, otherEmployee.getTaxId(), "iamEmailAddress2@intuit.com", "1000.00");
        String checkDateStart = "2013-01-01";
        String checkDateEnd = "2013-01-11";
        Application.beginUnitOfWork();
        List<Paystub> paystubs = PaystubFinder.findPaystubsByConsumerRealmAndEmployeeId(consumerRealmId, employee.getId().toString(), checkDateStart, checkDateEnd, 0, 10);
        Assert.assertEquals(2, paystubs.size());
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub1));
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub2));
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testFindPaystubsByCompanyRealmAndEmployeeId() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        String checkDateStart = "2013-01-01";
        String checkDateEnd = "2013-01-11";
        List<Paystub> paystubs = PaystubFinder.findPaystubsByCompanyRealmidAndEmployeeId(
            companyRealmId, employee.getId().toString(), checkDateStart, checkDateEnd, 0, 10);
        Assert.assertEquals(1, paystubs.size());
        Assert.assertTrue("Paystubs does not contain expected paystub", paystubs.contains(paystub));
    }

    @Test
    public void testBlankDates() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        //Passing blank dates should include the entire range
        String checkDateStart = "";
        String checkDateEnd = "";
        List<Paystub> paystubs = PaystubFinder.findPaystubsByCompanyRealmidAndEmployeeId(
            companyRealmId, employee.getId().toString(), checkDateStart, checkDateEnd, 0, 10);
        Assert.assertEquals(1, paystubs.size());
        Assert.assertTrue("Paystubs does not contain expected paystub", paystubs.contains(paystub));
    }

    @Test
    public void testHideFutureDatedPaystubs() {
        //Set date so only the 3rd paystub is in the future
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 1, 11));
        Application.commitUnitOfWork();

        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        //Make sure company has a realm setup
        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();
        //Set company preference to hide future dated paystubs
        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();
        EmployerPreferenceParams params = new EmployerPreferenceParams();
        params.setPreferenceName(EmployerPreference.HIDE_PAYCHECKS_DATED_IN_THE_FUTURE);
        params.setPreferenceValue(EmployerPreference.ON);
        payrollCompanyManager.createOrUpdateEmployerPreferences(companyRealmId, EmployerPreference.VMP, params);

        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        Paystub paystub1 = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        SpcfCalendar laterPaycheckDate = SpcfCalendar.createInstance(2013, 1, 10);
        Paystub paystub2 = VmpTestUtil.createPaystub(employee, "1000.00", laterPaycheckDate);
        SpcfCalendar evenLaterPaycheckDate = SpcfCalendar.createInstance(2013, 1, 15);
        Paystub paystub3 = VmpTestUtil.createPaystub(employee, "1000.00", evenLaterPaycheckDate);
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        String checkDateStart = "2013-01-01";
        String checkDateEnd = "2013-01-20";
        //Find using first API
        Application.beginUnitOfWork();
        List<Paystub> paystubs = PaystubFinder.findPaystubsByConsumerRealmAndEmployeeId(consumerRealmId, employee.getId().toString(),checkDateStart, checkDateEnd, 0, 10);
        Assert.assertEquals(3, paystubs.size());
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub1));
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub2));
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub3));

        //Next API
        paystubs = PaystubFinder.findPaystubsByConsumerRealmAndEmployeeId(consumerRealmId, employee.getId().toString(), checkDateStart, checkDateEnd, 0, 10);
        Assert.assertEquals(3, paystubs.size());
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub1));
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub2));
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub3));

        paystubs = PaystubFinder.findPaystubsByCompanyRealmidAndEmployeeId(companyRealmId, employee.getId().toString(), checkDateStart, checkDateEnd, 0, 10);
        Assert.assertEquals(3, paystubs.size());
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub1));
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub2));
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(paystub3));

        Application.rollbackUnitOfWork();
    }

    //When a paycheck is voided / deleted it should no longer be marked as active and we should not return it
    @Test
    public void testHideNonActivePaystubs() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        Paystub activePaystub = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        SpcfCalendar laterPaycheckDate = SpcfCalendar.createInstance(2013, 1, 10);
        Paystub deletedPaystub = VmpTestUtil.createPaystub(employee, "1000.00", laterPaycheckDate);
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        PayrollServices.payrollManager.deletePaycheck(SourceSystemCode.QBDT, psid, deletedPaystub.getPaycheck().getSourcePaycheckId(), null);
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        String checkDateStart = "2013-01-01";
        String checkDateEnd = "2013-01-11";
        Application.beginUnitOfWork();
        List<Paystub> paystubs = PaystubFinder.findPaystubsByConsumerRealmAndEmployeeId(consumerRealmId, employee.getId().toString(),checkDateStart, checkDateEnd, 0, 10);
        Assert.assertEquals(1, paystubs.size());
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(activePaystub));
        Application.rollbackUnitOfWork();
    }

    //As part of service migration a copy of existing paychecks is created and the old paycheck's source id
    // becomes a negative number.  We want to hide all negative number duplicate paychecks
    @Test
    public void testHideNegativeSourceIdPaystubs() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        Paystub negativeSourceIdPaystub = VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        Paystub normalPaystub = VmpTestUtil.createPaystub(employee, "2000.00", paycheckDate);
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        Application.beginUnitOfWork();
        Application.refresh(negativeSourceIdPaystub);
        //By giving this paystub a negative id we are simulating what happens in the migration case, it should not be returned by findPaystubs
        negativeSourceIdPaystub.getPaycheck().setSourcePaycheckId("-1");
        Application.save(negativeSourceIdPaystub);
        Application.commitUnitOfWork();
        String checkDateStart = "2013-01-01";
        String checkDateEnd = "2013-01-11";
        Application.beginUnitOfWork();
        List<Paystub> paystubs = PaystubFinder.findPaystubsByConsumerRealmAndEmployeeId(consumerRealmId, employee.getId().toString(), checkDateStart, checkDateEnd, 0, 10);
        Assert.assertEquals(1, paystubs.size());
        Assert.assertTrue("Paystubs doesn't contain expected paystub", paystubs.contains(normalPaystub));
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testFindPaystubEmployeeInfo() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        VmpTestUtil.createPaystub(employee, "1000.00", paycheckDate);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PstubEmployeeInfo pstubEmployeeInfo = PstubEmployeeInfo.findPstubEmployeeInfo(employee, 0);
        Assert.assertNotNull(pstubEmployeeInfo.getCompany());

        Expression<PstubEmployeeInfo> query = new Query<PstubEmployeeInfo>().Where(PstubEmployeeInfo.Company().equalTo(pstubEmployeeInfo.getCompany()));
        DomainEntitySet<PstubEmployeeInfo> comp = Application.find(PstubEmployeeInfo.class, query);
        org.junit.Assert.assertTrue(comp.size() == 1);
        Application.rollbackUnitOfWork();
    }
}
