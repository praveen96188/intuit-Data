package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import static java.lang.System.out;

/**
 * User: mvillani
 * Date: Nov 15, 2007
 * Time: 11:52:07 AM
 */
public class UpdateVoidedAfterOffloadCoreTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void testSucessfulUpdateVoidAfterOffloaded() {


        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        System.out.println("Payroll Submit Starts Here");

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        out.println(processResult);

        // Update Voided After Offload Indicator for the first paycheck
        PayrollServices.beginUnitOfWork();
        Paycheck paycheck = processResult.getResult().getPaycheckCollection().iterator().next();
        ProcessResult<Paycheck> updateProcessResult = PayrollServices.payrollManager.updateVoidedAfterOffload(SourceSystemCode.QBOE, "123272727", paycheck.getSourcePaycheckId(), true, null);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("updateVoidedAfterOffload", updateProcessResult);

        out.println(processResult);

        //Retrieve the updated company
        paycheck = updateProcessResult.getResult();

        // Verify that the voided after offload indicator has been updated
        assertEquals("Voided After Offload Indicator:", true, paycheck.isVoided());

    }


    /**
     * Test error message 169 - Company Does Not Exist
     */
    @Test
    public void testCompanyDoesNotExist() {
        // Load company data
        PayrollServices.beginUnitOfWork();
        Company1Dataloader company1DL = new Company1Dataloader();
        company1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        // Call process with invalid company
        PayrollServices.beginUnitOfWork();
        ProcessResult<Paycheck> processResult = PayrollServices.payrollManager.updateVoidedAfterOffload(
                SourceSystemCode.valueOf(company1DL.getCompany1().getSourceSystemCd().toString()),
                "InvalidCompanyId", null, true, null);
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


    /**
     * Test error message 299 - Paycheck Does Not Exist
     */
    @Test
    public void testPaycheckDoesNotExist() {

        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        System.out.println("Payroll Submit Starts Here");

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        out.println(processResult);

        // Call update process with invalid paycheck
        PayrollServices.beginUnitOfWork();
        ProcessResult<Paycheck> updateProcessResult = PayrollServices.payrollManager.updateVoidedAfterOffload(
                processResult.getResult().getCompany().getSourceSystemCd(),
                "123272727", "12345", true, null);
        PayrollServices.commitUnitOfWork();
        
        out.println(updateProcessResult);

        // Verify error count
        assertEquals("Number of Errors: ", 1, updateProcessResult.getMessages().size());

        // validate error code
        Message message = updateProcessResult.getMessages().get(0);
        assertEquals("Error Code:", "299", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Paycheck 12345 for company QBOE:123272727 does not exist.", message.getMessage());
    }


    @Test
    public void testInvalidCompanyParameters() {

        PayrollServices.beginUnitOfWork();
        ProcessResult<Paycheck> processResult = PayrollServices.payrollManager.updateVoidedAfterOffload(null, "CompanyId", null, true, null);
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
        ProcessResult<Paycheck> updateProcessResult = PayrollServices.payrollManager.updateVoidedAfterOffload(SourceSystemCode.QBOE, null, null, true, null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, updateProcessResult.getMessages().size());

        // validate error code
        message = updateProcessResult.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }


}