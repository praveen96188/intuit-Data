package com.intuit.sbd.payroll.psp.processes.guideline401k.employee;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import com.intuit.v4.GlobalId;
import com.intuit.v4.network.Contact;
import com.intuit.v4.payroll.employee.EmployeeDeduction;
import com.intuit.v4.payroll.employer.EmployerDeduction;
import com.jscape.util.Assert;
import org.junit.Before;
import org.junit.Test;

public class AddOrUpdate401kEmployeeDeductionCoreValidateTests {

    @Before
    public void runBeforeEachTest(){
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void testAdd401kEmployeeDeductionCoreWithNullEmployeeDeduction(){
        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeeDeduction> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeeDeduction(null);
        PayrollServices.rollbackUnitOfWorkWithSecondary();

        Assert.areEqual(processResult.isSuccess(), false, "The call should fail for null object.");
        Assert.areEqual(processResult.getErrorMessages().size(),1, "error message missing");
        Assert.areEqual(processResult.getErrorMessages().get(0).getMessage().contains("EmployeeDeduction"),true, "error message missing");
    }

    @Test
    public void testAdd401kEmployeeDeductionCoreWithNullEmployeeId(){
        EmployeeDeduction employeeDeduction = new EmployeeDeduction();
        Contact employee = new Contact();
        employee.setCompanyName("company_name");
        employeeDeduction.setEmployee(employee);

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeeDeduction> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeeDeduction(employeeDeduction);
        PayrollServices.rollbackUnitOfWorkWithSecondary();

        Assert.areEqual(processResult.isSuccess(), false, "The call should fail for null object.");
        Assert.areEqual(processResult.getErrorMessages().size(),1, "error message missing");
        Assert.areEqual(processResult.getErrorMessages().get(0).getMessage().contains("EmployeeId"),true, "error message missing");
    }

    @Test
    public void testAdd401kEmployeeDeductionCoreWithNullEmployerDeductionId(){
        EmployeeDeduction employeeDeduction = new EmployeeDeduction();
        Contact employee = new Contact();
        employee.setCompanyName("company_name");
        employee.setId(GlobalId.create("realm_id", "local_id"));
        employeeDeduction.setEmployee(employee);

        EmployerDeduction employerDeduction = new EmployerDeduction();
        employerDeduction.setStatutoryDeductionPolicy("stat");

        employeeDeduction.setEmployerDeduction(employerDeduction);

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeeDeduction> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeeDeduction(employeeDeduction);
        PayrollServices.rollbackUnitOfWorkWithSecondary();

        Assert.areEqual(processResult.isSuccess(), false, "The call should fail for null object.");
        Assert.areEqual(processResult.getErrorMessages().size(),1, "error message missing");
        Assert.areEqual(processResult.getErrorMessages().get(0).getMessage().contains("EmployerId"),true, "error message missing");
    }

    @Test
    public void testUpdate401kEmployeeDeductionCoreWithNullEmployeeDeductionInDB(){
        EmployeeDeduction employeeDeduction = new EmployeeDeduction();
        Contact employee = new Contact();
        employee.setCompanyName("company_name");
        employee.setId(GlobalId.create("realm_id", "local_id"));
        employeeDeduction.setEmployee(employee);

        EmployerDeduction employerDeduction = new EmployerDeduction();
        employerDeduction.setId(GlobalId.create("realm_id", "local_id"));
        employerDeduction.setStatutoryDeductionPolicy("stat");

        employeeDeduction.setEmployerDeduction(employerDeduction);

        employeeDeduction.setId(GlobalId.create("realm_id", SpcfUniqueIdImpl.generateRandomUniqueIdString()));

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeeDeduction> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeeDeduction(employeeDeduction);
        PayrollServices.rollbackUnitOfWorkWithSecondary();

        Assert.areEqual(processResult.isSuccess(), false, "The call should fail for null object.");
        Assert.areEqual(processResult.getErrorMessages().size(),1, "error message missing");
        Assert.areEqual(processResult.getErrorMessages().get(0).getMessage().contains("hcm401kEmployeeDeduction"),true, "error message missing");
    }
}
