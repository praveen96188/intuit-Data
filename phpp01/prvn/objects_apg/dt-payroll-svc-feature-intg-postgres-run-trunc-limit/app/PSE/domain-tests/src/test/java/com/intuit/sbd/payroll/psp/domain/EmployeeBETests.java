package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeTestSuiteDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.sbd.payroll.psp.util.VMPEmployeePaginationDetails;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit Tests for the EmployeeBE
 * <p/>
 * @author Sean Barenz
 *
 * Date: Aug 15, 2007
 */
public class EmployeeBETests {

    private EmployeeTestSuiteDataLoader dataloader = new EmployeeTestSuiteDataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone()));
        dataloader.loadFullSuite();
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
    public void findEmployees() {

        PayrollServices.beginUnitOfWork();
        Company company = Company
                .findCompany("123456", SourceSystemCode.QBOE);
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals("Number of Employees:", 2, employees.size());
        Application.commitUnitOfWork();
    }
    @Test
    public void testActiveEmployees() {

        Application.beginUnitOfWork();
        Company company = Company.findCompany("123456", SourceSystemCode.QBOE);
        addQBDTEmployees(company.getEmployees());
        assertEquals("Number of Employees:", 1, Employee.findActiveEmployees(company).size());
        Application.rollbackUnitOfWork();

    }
    private void addQBDTEmployees(DomainEntitySet<Employee> employees){
        employees.forEach(
                employee ->{
                    QbdtEmployeeInfo qbEmployee= new QbdtEmployeeInfo();
                    qbEmployee.setEmployee(employee);
                    Application. save(qbEmployee);
                });

    }

    @Test
    public void validateCompanyNotSpecified() {
        Employee employee = getEmployee();
        employee.setCompany(null);
        ProcessResult pr = employee.validateEmployee();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "141", message.getMessageCode());
    }


    @Test
    public void validateCompanyNotExists() {
        Employee employee = getEmployee();
        Company company = getCompany();
        company.setSourceCompanyId("IDONTEXIST");
        employee.setCompany(company);
        ProcessResult pr = employee.validateEmployee();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());
    }


    @Test
    public void validateEmployeeIdNotSpecified() {
        Employee employee = getEmployee();
        employee.setSourceEmployeeId(null);
        ProcessResult pr = employee.validateEmployee();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "145", message.getMessageCode());
    }

    @Test
    public void validateSuccess() {
        Application.beginUnitOfWork();
        Employee employee = getEmployee();
        employee.setCompany(getCompany());
        ProcessResult pr = employee.validateEmployee();

        // validate error count
        assertEquals("Number of Errors:", 0, pr.getMessages().size());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void findLikeEmployeeTerminated() {
        Application.beginUnitOfWork();
        ProcessResult pr = PayrollServices.companyManager.terminateService(getCompany().getSourceSystemCd(),
                getCompany().getSourceCompanyId(), ServiceCode.DirectDeposit);
        assertTrue(pr.getMessages().toString(), pr.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Employee employee = getEmployee();
        employee.setLastName("Employee");
        employee.setFirstName("Active");
        employee.setCompany(getCompany());

        DomainEntitySet<Employee> employees = employee.findEmployeesWithSameNameFromTerminatedCompanies();
        assertTrue(employees.size() > 0);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void findLikeEmployeeMixedCaseTerminated() {
        Application.beginUnitOfWork();
        ProcessResult pr = PayrollServices.companyManager.terminateService(getCompany().getSourceSystemCd(),
                getCompany().getSourceCompanyId(), ServiceCode.DirectDeposit);
        assertTrue(pr.getMessages().toString(), pr.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Employee employee = getEmployee();
        employee.setLastName("EmPlOyEe");
        employee.setFirstName("Active");
        employee.setCompany(getCompany());
        DomainEntitySet<Employee> employees = employee.findEmployeesWithSameNameFromTerminatedCompanies();
        assertTrue(employees.size() > 0);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void findLikeEmployeeNoMatchTerminated() {
        Application.beginUnitOfWork();
        ProcessResult pr = PayrollServices.companyManager.terminateService(getCompany().getSourceSystemCd(),
                getCompany().getSourceCompanyId(), ServiceCode.DirectDeposit);
        assertTrue(pr.getMessages().toString(), pr.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Employee employee = getEmployee();
        employee.setLastName("EmPlOyEe");
        employee.setFirstName("AcTiVeY");
        employee.setCompany(getCompany());
        DomainEntitySet<Employee> employees = employee.findEmployeesWithSameNameFromTerminatedCompanies();
        assertTrue(employees.size() == 0);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void findLikeEmployeeTerminatedNullFirstName() {
        Application.beginUnitOfWork();
        ProcessResult pr = PayrollServices.companyManager.terminateService(getCompany().getSourceSystemCd(),
                getCompany().getSourceCompanyId(), ServiceCode.DirectDeposit);
        assertTrue(pr.getMessages().toString(), pr.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Employee employee = getEmployee();
        employee.setLastName(null);
        employee.setFirstName("Active");
        employee.setCompany(getCompany());

        DomainEntitySet<Employee> employees = employee.findEmployeesWithSameNameFromTerminatedCompanies();
        assertEquals(employees.size(), 0);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void findLikeEmployeeTerminatedBlankLastName() {
        Application.beginUnitOfWork();
        ProcessResult pr = PayrollServices.companyManager.terminateService(getCompany().getSourceSystemCd(),
                getCompany().getSourceCompanyId(), ServiceCode.DirectDeposit);
        assertTrue(pr.getMessages().toString(), pr.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Employee employee = getEmployee();
        employee.setLastName("Employee");
        employee.setFirstName("");
        employee.setCompany(getCompany());

        DomainEntitySet<Employee> employees = employee.findEmployeesWithSameNameFromTerminatedCompanies();
        assertEquals(employees.size(), 0);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void findLikeEmployeeActive() {
        Application.beginUnitOfWork();
        Employee employee = getEmployee();
        employee.setLastName("Employee");
        employee.setFirstName("Active");
        employee.setCompany(getCompany());
        DomainEntitySet<Employee> employees = employee.findEmployeesWithSameNameFromTerminatedCompanies();
        assertTrue(employees.size() == 0);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void findLikeEmployeeMixedCaseActive() {
        Application.beginUnitOfWork();
        Employee employee = getEmployee();
        employee.setLastName("EmPlOyEe");
        employee.setFirstName("AcTiVe");
        employee.setCompany(getCompany());
        DomainEntitySet<Employee> employees = employee.findEmployeesWithSameNameFromTerminatedCompanies();
        assertTrue(employees.size() == 0);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void findLikeEmployeeNoMatchActive() {
        Application.beginUnitOfWork();
        Employee employee = getEmployee();
        employee.setLastName("EmPlOyEe");
        employee.setFirstName("AcTiVeY");
        employee.setCompany(getCompany());
        DomainEntitySet<Employee> employees = employee.findEmployeesWithSameNameFromTerminatedCompanies();
        assertTrue(employees.size() == 0);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void findEmployeeSuccess() {
        Application.beginUnitOfWork();
        Company company = Company
                .findCompany("123456", SourceSystemCode.QBOE);
        String sourceEmployeeId = "TESTACTV";
        Employee employee = Employee.findEmployee(company, sourceEmployeeId);
        Application.commitUnitOfWork();

        // vadliate we get the employee
        assertNotNull("Employee Returned:", employee);
        assertEquals("Employee Source ID:", "TESTACTV", employee.getSourceEmployeeId());
        assertEquals("Employee Company ID:", "123456", employee.getCompany().getSourceCompanyId());
    }

    @Test
    public void findEmployeeEmployeeNotFound() {
        Company company = Company
                .findCompany("123456", SourceSystemCode.QBOE);
        String sourceEmployeeId = "BLAH";
        Employee employee = Employee.findEmployee(company, sourceEmployeeId);

        // vadliate we get the employee
        assertNull("Employee Returned:", employee);
    }

    @Test
    public void testDeactivateEmployeeBankAccounts() {
        Application.beginUnitOfWork();
        Company company = Company
                .findCompany("123456", SourceSystemCode.QBOE);
        String sourceEmployeeId = "TESTACTV";
        Employee employee = Employee.findEmployee(company, sourceEmployeeId);
        employee.deactivateBankAccounts();
        Application.commitUnitOfWork();
    }


    public Company getCompany() {
        Company company = new Company();
        company.setDbaName("Intuit");
        company.setFedTaxId("123456789");
        company.setLegalName("Intuit");
        company.setNotificationEmail("notifications@intuit.com");
        company.setSourceCompanyId("123456");
        company.setSourceSystemCd(SourceSystemCode.QBOE);

        return company;
    }

    public Employee getEmployee() {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("TESTSLB");
        incEmployee.setFirstName("TestFirstName");
        incEmployee.setLastName("TestLastName");
        incEmployee.setMiddleName("TMI");
        incEmployee.setEmail("test@testemail.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8005551212");
        incEmployee.setTaxId("111223333");

        incEmployee.setCompany(Company.findCompany("123456",
                SourceSystemCode.QBOE));
        return incEmployee;

    }

    /**
     * Private method to serve as common point to verify copy of employee
     *
     * @param pExistingEmployee Employee we're copying from
     * @param pNewEmployee      Employee we're copying to
     */
    private void verifyCopy(Employee pExistingEmployee, Employee pNewEmployee) {
        assertEquals("First Name:", pExistingEmployee.getFirstName(), pNewEmployee.getFirstName());
        assertEquals("Last Name:", pExistingEmployee.getLastName(), pNewEmployee.getLastName());
        assertEquals("Middle Name:", pExistingEmployee.getMiddleName(), pNewEmployee.getMiddleName());
        assertEquals("Email:", pExistingEmployee.getEmail(), pNewEmployee.getEmail());
        assertEquals("Gender:", pExistingEmployee.getGenderCd(), pNewEmployee.getGenderCd());
        assertEquals("Phone:", pExistingEmployee.getPhone(), pNewEmployee.getPhone());
        assertEquals("Tax ID:", pExistingEmployee.getTaxId(), pNewEmployee.getTaxId());

        Company newCompany = pNewEmployee.getCompany();
        Company existingCompany = pExistingEmployee.getCompany();
        assertEquals("Company ID Check:", existingCompany.getSourceCompanyId(), newCompany.getSourceCompanyId());
        assertEquals("Company Dba Name:", existingCompany.getDbaName(), newCompany.getDbaName());
        assertEquals("Company Legal Name:", existingCompany.getLegalName(), newCompany.getLegalName());
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testFindBySSNAndEmail() throws Exception{
        for (int i = 1; i <= 6; i++) {
            VmpTestUtil.setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(Integer.toString(i), Integer.toString(i));
        }

        // Regex search on PII fields will not work since the data is now encrypted
        //assertEquals("Employee count by SSN was wrong", 12, Employee.findEmployeeCountBySearchText("findVmpEmployeeBySSN", "%%%"));
        assertEquals("Employee count by Email was wrong", 12, Employee.findEmployeeCountBySearchText("findVmpEmployeeByEmail", "%%%"));

        // Regex search on PII fields will not work since the data is now encrypted
        //assertEquals("Second employee SSN was wrong", Employee.findEmployees(Company.findCompany("1", SourceSystemCode.QBDT)).get(1).getTaxId(), Employee.findEmployeeBySearchText("findVmpEmployeeBySSN", "%%%", new VMPEmployeePaginationDetails(1, 1, "", false)).getFirst().getTaxId());
        assertEquals("Second employee Email was wrong", Employee.findEmployees(Company.findCompany("6", SourceSystemCode.QBDT)).get(0).getEmail(), Employee.findEmployeeBySearchText("findVmpEmployeeByEmail", "%%%", new VMPEmployeePaginationDetails(1, 1, "", false)).getFirst().getEmail());

        assertEquals("SSN from search didn't match", Employee.findEmployees(Company.findCompany("5", SourceSystemCode.QBDT)).get(1).getTaxId(), Employee.findEmployeeBySearchText("findVmpEmployeeBySSN", "999090010", new VMPEmployeePaginationDetails(0, 1, "", false)).getFirst().getTaxId());
        assertEquals("Email from search didn't match", Employee.findEmployees(Company.findCompany("5", SourceSystemCode.QBDT)).get(1).getEmail(), Employee.findEmployeeBySearchText("findVmpEmployeeByEmail", "EE10@intuit.com", new VMPEmployeePaginationDetails(0, 1, "", false)).getFirst().getEmail());
    }

    @Ignore
    @Test
    public void validateEmployeeLookupByNonAuthCriteria(){

        String firstName = "First_1";
        String lastName = "Last_1";
        String ssn = "999090001";
        String email = "EE1@intuit.com";
        String phone = "(775) 111-1111";
        String sourceSystemCode = SourceSystemCode.QBDT.name();
        String psid = "990123456";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addEEs(company, 2);

        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        Employee employee = employees.getFirst();
        String id = employee.getSourceEmployeeId();


        Application.beginUnitOfWork();
        Employee employee1 = Employee.findEmployee(company, id);
        employee1.setPhone(phone);
        Application.save(employee1);
        Application.commitUnitOfWork();

        //Happypath
        List<Employee> employeesByNonAuthCriteria = Employee.findEmployeesByNonAuthCriteria(ssn, firstName, lastName, sourceSystemCode, email, phone);

        assertNotNull("NamedQuery returned a null", employeesByNonAuthCriteria);
        assertEquals("Count of employees does not match", 1, employeesByNonAuthCriteria.size());

        //Phone number format change
        phone = "775 111-1111";
        employeesByNonAuthCriteria = Employee.findEmployeesByNonAuthCriteria(ssn, firstName, lastName, sourceSystemCode, email, phone);

        assertNotNull("NamedQuery returned a null", employeesByNonAuthCriteria);
        assertEquals("Count of employees does not match", 1, employeesByNonAuthCriteria.size());

        //formatted ssn
        ssn = "99-909-0001";
        employeesByNonAuthCriteria = Employee.findEmployeesByNonAuthCriteria(ssn, firstName, lastName, sourceSystemCode, email, phone);

        assertNotNull("NamedQuery returned a null", employeesByNonAuthCriteria);
        assertEquals("Count of employees does not match", 1, employeesByNonAuthCriteria.size());

        //One of the criteria no matching
        ssn = "7751111111";
        employeesByNonAuthCriteria = Employee.findEmployeesByNonAuthCriteria(ssn, firstName, lastName, sourceSystemCode, email, phone);

        assertNotNull("NamedQuery returned a null", employeesByNonAuthCriteria);
        assertEquals("Count of employees does not match", 0, employeesByNonAuthCriteria.size());

        //None of the criteria match
        firstName = "firstName";
        lastName = "lastName";
        ssn = "99099092";
        email = "email@mail.com";
        employeesByNonAuthCriteria = Employee.findEmployeesByNonAuthCriteria(ssn, firstName, lastName, sourceSystemCode, email, phone);

        assertNotNull("NamedQuery returned a null", employeesByNonAuthCriteria);
        assertEquals("Count of employees does not match", 0, employeesByNonAuthCriteria.size());

    }
}
