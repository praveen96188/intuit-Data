package com.intuit.sbd.payroll.psp.processes.datamanager;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.VmpEmployeeInfo;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class DGAuthBasedDeleteEmployeeProcessCoreTest {

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
    public void testValidation(){
        PayrollServices.beginUnitOfWork();
        ProcessResult<String> result = PayrollServices.employeeManager.disassociateEmployeeConsumerRealm(null, null, null);
        assertOne(result.getMessages());
        assertEquals("Error Code", "5002", result.getErrorMessages().get(0).getMessageCode());

        result = PayrollServices.employeeManager.disassociateEmployeeConsumerRealm(SpcfUniqueId.generateRandomUniqueIdString(), null, null);
        assertEquals("Count of validation messages does not match",2,result.getMessages().size());
        assertEquals("Error Code", "5003", result.getErrorMessages().get(0).getMessageCode());
        PayrollServices.rollbackUnitOfWork();

        List<Employee> employees = DataLoadServices.setupCompany ("12345");
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.employeeManager.disassociateEmployeeConsumerRealm(employees.get(0).getId().toString(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        assertSuccess(result);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHappyPathForEmployee(){
        //Data setup
        List<Employee> employees = DataLoadServices.setupCompany ("12345");

        PayrollServices.beginUnitOfWork();
        for (Employee employee : employees) {
            Application.refresh(employee);
            employee.setConsumerRealmId("consumerId_"+ employee.getSourceEmployeeId());
            Application.save(employee);
        }

        Company company = employees.get(0).getCompany();
        company.setIAMRealmId("100010010");
        Application.save(company);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findActiveCompanyByRealmId("100010010");
        Employee employee = company.getEmployees().get(0);
        ProcessResult<String> result = PayrollServices.employeeManager.disassociateEmployeeConsumerRealm(employee.getId().toString(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        assertNotNull(result);
        assertEquals("CFR disassociation failed", "100010010", result.getResult());

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.DGDeleteRequest);
        assertNotNull(companyEvents);
        assertOne(companyEvents);

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testHappyPathForVmpEmployeeInfo(){
        //Data setup
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, Boolean.TRUE, ServiceCode.ViewMyPaycheck);
        PayrollServices.beginUnitOfWork();
        VmpEmployeeInfo vmpEmployeeInfo = new VmpEmployeeInfo();
        vmpEmployeeInfo.setConsumerRealmId("231990");
        vmpEmployeeInfo.setEmployeeRecnum("112233");
        vmpEmployeeInfo.setCompany(company);
        Application.save(vmpEmployeeInfo);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        company.setIAMRealmId("100010010");
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<String> result = PayrollServices.employeeManager.disassociateEmployeeConsumerRealm(vmpEmployeeInfo.getId().toString(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        assertNotNull(result);
        assertEquals("CFR disassociation failed", "100010010", result.getResult());

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.DGDeleteRequest);
        assertNotNull(companyEvents);
        assertOne(companyEvents);

        PayrollServices.commitUnitOfWork();
    }

}