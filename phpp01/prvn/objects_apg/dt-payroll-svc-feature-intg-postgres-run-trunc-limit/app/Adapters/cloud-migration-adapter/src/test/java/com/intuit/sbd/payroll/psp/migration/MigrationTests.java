package com.intuit.sbd.payroll.psp.migration;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.ProcessingResponse;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBEmployee;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBPaycheck;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.SubmitPayrollRequest;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.webservices.QBPayrollWebServices;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.adapters.qbdtws.test.QBDTWSRequestCreator.createSubmitPayrollRequest;
import static com.intuit.sbd.payroll.psp.adapters.qbdtws.test.WS_Assert.assertCount;
import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertSuccess;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * User: rnorian
 * Date: May 20, 2010
 * Time: 1:57:36 PM
 */
public class MigrationTests {

    private QBPayrollWebServices webService;


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        webService = new QBPayrollWebServices();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void testAddCloudToDD() {
        // create a shell DD company w/no cloud service
        Company company = DataLoadServices.newCompany(DataLoadServices.createCompany(SourceSystemCode.QBDT, "5324"), "pindoesntmatter");
        DataLoadServices.addDDService(company);
        DataLoadServices.activateDDService(company);

        // upgrade company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = CloudServiceUpdater.addCloudService(company.getSourceCompanyId());
        assertSuccess("addCloudService", result);
        PayrollServices.commitUnitOfWork();

        SubmitPayrollRequest submitPayrollRequest = createSubmitPayrollRequest(company.getSourceCompanyId(), "test1234!", null);
        QBEmployee qbEE = submitPayrollRequest.getSubmitEmployeesRequest().getEmployees().getEmployee().get(0);
        QBPaycheck qbPaycheck = submitPayrollRequest.getPaycheckList().getPaycheck().get(0);

        QBPayrollWebServices webServices = new QBPayrollWebServices();
        ProcessingResponse response = webServices.SubmitPayroll(submitPayrollRequest);
        assertCount(0, response);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        assertNotNull("company", company);

        Employee ee = Employee.findEmployee(company, qbEE.getSourceEmployeeId());
        assertNotNull("employee", ee);

        Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPspPaycheckId());
        assertNotNull("paycheck", paycheck);
        assertTrue("paycheck gross amt", qbPaycheck.getGrossPay().compareTo(SpcfUtils.convertToBigDecimal(paycheck.getGrossAmount())) == 0);

        PayrollServices.rollbackUnitOfWork();
        
    }

}
