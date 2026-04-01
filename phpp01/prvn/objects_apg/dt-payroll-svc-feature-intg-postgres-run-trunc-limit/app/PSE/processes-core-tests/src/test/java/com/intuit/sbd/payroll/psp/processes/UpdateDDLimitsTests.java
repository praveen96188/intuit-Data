package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.DDCompanyServiceInfo;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import static java.lang.System.out;

/**
 *
 * User: mvillani
 * Date: Nov 15, 2007
 * Time: 9:33:11 AM

 */
public class UpdateDDLimitsTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testSucessfulUpdateLimits() {
        // Load company data
        PayrollServices.beginUnitOfWork();
        Company1Dataloader company1DL = new Company1Dataloader();
        company1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        // Update DD Limits
        SpcfMoney newCompanyLimit = new SpcfMoney("27000.27");
        SpcfMoney newEmployeeLimit = new SpcfMoney("7272.72");
        Application.beginUnitOfWork();
        UpdateDDLimits updateDDLimits = new UpdateDDLimits(
                SourceSystemCode.valueOf(company1DL.getCompany1().getSourceSystemCd().toString()),
                company1DL.getCompany1().getCompanyId(), newCompanyLimit, newEmployeeLimit);
        ProcessResult processResult = updateDDLimits.execute();
        Application.commitUnitOfWork();

        out.println(processResult);

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        //Retrieve the company bank account
        DDCompanyServiceInfo ddCompanyServiceInfo = updateDDLimits.getDDCompanyServiceInfo();

        // Verify that company DD Limit has been updated
        assertEquals("Company DD Limit:", ddCompanyServiceInfo.getOverrideCompanyLimitAmount(), newCompanyLimit);

        // Verify that employee DD Limit has been updated
        assertEquals("Employee DD Limit:", ddCompanyServiceInfo.getOverrideEmployeeLimitAmount(), newEmployeeLimit);

        // Update DD Limits to null
        Application.beginUnitOfWork();
        updateDDLimits = new UpdateDDLimits(
                SourceSystemCode.valueOf(company1DL.getCompany1().getSourceSystemCd().toString()),
                company1DL.getCompany1().getCompanyId(), null, null);
        processResult = updateDDLimits.execute();
        Application.commitUnitOfWork();
        out.println(processResult);

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        //Retrieve the company bank account
        ddCompanyServiceInfo = updateDDLimits.getDDCompanyServiceInfo();

        // Verify that company DD Limit has been updated
        assertNull("Company DD Limit:", ddCompanyServiceInfo.getOverrideCompanyLimitAmount());

        // Verify that employee DD Limit has been updated
        assertNull("Employee DD Limit:", ddCompanyServiceInfo.getOverrideCompanyLimitAmount());

        // Update DD Limits to a new value
        newCompanyLimit = new SpcfMoney("25700.00");
        newEmployeeLimit = new SpcfMoney("9270.00");
        Application.beginUnitOfWork();
        updateDDLimits = new UpdateDDLimits(
                SourceSystemCode.valueOf(company1DL.getCompany1().getSourceSystemCd().toString()),
                company1DL.getCompany1().getCompanyId(), newCompanyLimit, newEmployeeLimit);
        processResult = updateDDLimits.execute();
        Application.commitUnitOfWork();
        out.println(processResult);

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        //Retrieve updated ddCompanyServiceInfo object
        ddCompanyServiceInfo = updateDDLimits.getDDCompanyServiceInfo();

        // Verify that company DD Limit has been updated
        assertEquals("Company DD Limit:", ddCompanyServiceInfo.getOverrideCompanyLimitAmount(), newCompanyLimit);

        // Verify that employee DD Limit has been updated
        assertEquals("Employee DD Limit:", ddCompanyServiceInfo.getOverrideEmployeeLimitAmount(), newEmployeeLimit);

    }

    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void testCompanyDoesNotExist() {
        // Load company data
        PayrollServices.beginUnitOfWork();
        Company1Dataloader company1DL = new Company1Dataloader();
        company1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        // Call process with invalid company
        PayrollServices.beginUnitOfWork();
        UpdateDDLimits updateDDLimits = new UpdateDDLimits(
                SourceSystemCode.valueOf(company1DL.getCompany1().getSourceSystemCd().toString()),
                "InvalidCompanyId", null, null);
        ProcessResult processResult = updateDDLimits.execute();
        PayrollServices.commitUnitOfWork();
        out.println(processResult);

        // Verify error count
        assertEquals("Number of Errors: ", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());
    }

    @Test
    /**
     * Test error message 1010 - Company is not associated with the DD service.
     */

    public void testCheckServiceStatus_NoDDService() {
        Application.beginUnitOfWork();
        DataLoader dataLoader = new DataLoader();
        CompanyDTO company = dataLoader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company);
        assertEquals(0, result.getMessages().size());
        Application.commitUnitOfWork();

        // Call  UpdateDDLimits process
        PayrollServices.beginUnitOfWork();
        UpdateDDLimits updateDDLimits = new UpdateDDLimits(
                SourceSystemCode.valueOf(company.getSourceSystemCd().toString()),
                company.getCompanyId(), null, null);
        ProcessResult processResult = updateDDLimits.execute();
        PayrollServices.commitUnitOfWork();
        out.println(processResult);

        // Verify error count
        assertEquals("Number of Errors: ", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1010", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error message", "Company QBOE:123456 is not associated with the DD service.",
                message.getMessage());
    }

    @Test
    public void testInvalidCompanyParameters() {
        PayrollServices.beginUnitOfWork();
        UpdateDDLimits updateDDLimits = new UpdateDDLimits(null, "CompanyId", null, null);
        ProcessResult processResult = updateDDLimits.execute();
        PayrollServices.commitUnitOfWork();
        out.println(processResult);

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        PayrollServices.beginUnitOfWork();
        updateDDLimits = new UpdateDDLimits(SourceSystemCode.QBOE, null, null, null);
        processResult = updateDDLimits.execute();
        PayrollServices.commitUnitOfWork();
        
        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

}
