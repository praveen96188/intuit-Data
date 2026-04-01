package com.intuit.sbd.payroll.psp.jss.processors.workerscomp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Address;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Paystub;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.mapper.DomainObjToWCObjConverterSplitLimit;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.*;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import java.util.List;

import static junit.framework.Assert.assertTrue;

public class DomainObjToWCObjConverterSplitLimitTest {
    private static final String psid = "99000123";
    private static final String realmId = "123456";
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
    public void testDomainAddressMaptoSplitLimitAddress() throws Exception {
        String psid = String.valueOf(System.currentTimeMillis());
        Employee employee=VmpTestUtil.setupCompanyCreateEmployee(psid);
        Address address=employee.getMailingAddress();
        com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Address address1= DomainObjToWCObjConverterSplitLimit.convert(address);
        Assert.assertNotNull(address1);
    }

    @Test
    public void testDomainBusinessMaptoSplitLimitBusiness() throws Exception
    {
        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtils.createCompanyEmployeesAndComplexPayroll(2,false);
        TestUtils.createWcCompanyEntryForNextCompany(company);
        Business business=DomainObjToWCObjConverterSplitLimit.convert(company);
        List<JAXBElement<?>> elements= business.getContent();
        Assert.assertNotNull(business);
        assertTrue(elements.size() > 0);
        assertTrue(elements.size()==14);
    }
    @Test
    public void testDomainEmployeeMaptoSplitLimitEmployee() throws Exception
    {
        String psid = String.valueOf(System.currentTimeMillis());
        Employee employee=VmpTestUtil.setupCompanyCreateEmployee(psid);
        com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Employee employee1=DomainObjToWCObjConverterSplitLimit.convert(employee);
        Assert.assertNotNull(employee1);
        assertTrue(employee1.getPaychecks() == null);
    }
    @Test
    public void testDomainPaycheckMaptoSplitLimitPaycheck() throws Exception
    {
        Employee employee=VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        Paystub paystub=VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        Company company = TestUtils.createCompanyEmployeesAndComplexPayroll(2,false);
        com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Paycheck paycheck=DomainObjToWCObjConverterSplitLimit.convert(company,paystub);
        Assert.assertNotNull(paycheck);
    }
}
