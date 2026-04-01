package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.SourcePayrollParameterDTO;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameter;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameterCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Vector;

import static org.junit.Assert.*;

/**
 * @author Ken Paul
 */
public class UpdateSourcePayrollParameterCoreTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testMissingSourceSystem() {
        PayrollServices.beginUnitOfWork();

        ProcessResult<DomainEntitySet<SourcePayrollParameter>> processResult =
                PayrollServices.payrollManager.updateSourcePayrollParameter(null, null);

        PayrollServices.rollbackUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code", "137", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    @Test
    public void testInvalidParameterCd() {
        List<SourcePayrollParameterDTO> dtoList = new Vector<SourcePayrollParameterDTO>();
        SourcePayrollParameterDTO dto;

        // Attempt to change source payroll parameter code to invalid value of null
        dtoList.add(new SourcePayrollParameterDTO(
                SourceSystemCode.QBOE,
                "Default DD Company Limit",
                "MAXIMUM DOLLAR AMOUNT FOR A COMPANY DIRECT DEPOSIT FOR A PAYROLL.",
                null,
                "50000.00"));

        // Attempt to change source payroll parameter code to invalid value of null
        dtoList.add(new SourcePayrollParameterDTO(
                SourceSystemCode.QBOE,
                "Company Bank Account Duration Limit for Verification",
                "NUM OF CALENDAR DAYS UNTIL A COMPANY BANK ACCOUNT CAN NO LONGER BE VERIFIED.",
                null,
                "14"));

        PayrollServices.beginUnitOfWork();

        ProcessResult<DomainEntitySet<SourcePayrollParameter>> processResult =
                PayrollServices.payrollManager.updateSourcePayrollParameter(SourceSystemCode.QBOE, dtoList);

        PayrollServices.rollbackUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 2, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "270", message1.getMessageCode());
        Message message2 = processResult.getMessages().get(1);
        assertEquals("Error Code", "270", message2.getMessageCode());

        // Verify that the correct message strings were returned
        String message1Text = "Source parameter for source system QBOE and parameter null does not exist.";
        assertEquals("Error Message", message1Text, message1.getMessage());
        String message2Text = "Source parameter for source system QBOE and parameter null does not exist.";
        assertEquals("Error Message", message2Text, message2.getMessage());
    }

    @Test
    public void testInvalidParameterValue() {
        List<SourcePayrollParameterDTO> dtoList = new Vector<SourcePayrollParameterDTO>();
        SourcePayrollParameterDTO dto;

        // Attempt to change DefaultDDCompanyLimit parameter value to invalid value of ""
        dtoList.add(new SourcePayrollParameterDTO(
                SourceSystemCode.QBOE,
                "Default DD Company Limit",
                "MAXIMUM DOLLAR AMOUNT FOR A COMPANY DIRECT DEPOSIT FOR A PAYROLL.",
                SourcePayrollParameterCode.AllowBackdatedPayrolls,
                ""));

        // Attempt to change CompanyBankAccountDurationLimitForVerification parameter value to invalid value of null
        dtoList.add(new SourcePayrollParameterDTO(
                SourceSystemCode.QBOE,
                "Company Bank Account Duration Limit for Verification",
                "NUM OF CALENDAR DAYS UNTIL A COMPANY BANK ACCOUNT CAN NO LONGER BE VERIFIED.",
                SourcePayrollParameterCode.AllowBackdatedPayrolls,
                null));

        PayrollServices.beginUnitOfWork();

        ProcessResult<DomainEntitySet<SourcePayrollParameter>> processResult =
                PayrollServices.payrollManager.updateSourcePayrollParameter(SourceSystemCode.QBOE, dtoList);

        PayrollServices.rollbackUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 2, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "11", message1.getMessageCode());
        Message message2 = processResult.getMessages().get(1);
        assertEquals("Error Code", "11", message2.getMessageCode());

        // Verify that the correct message strings were returned
        String message1Text = "Invalid argument: blank";
        assertEquals("Error Message", message1Text, message1.getMessage());
        String message2Text = "Invalid argument: blank";
        assertEquals("Error Message", message2Text, message2.getMessage());
    }

    @Test
    public void testParameterCdChange() {
        List<SourcePayrollParameterDTO> dtoList = new Vector<SourcePayrollParameterDTO>();
        DomainEntitySet<SourcePayrollParameter> startParams = null;
        DomainEntitySet<SourcePayrollParameter> changedParams = null;

        // Save the original params to restore after the test...
        PayrollServices.beginUnitOfWork();
        try {
            startParams = PayrollServices.entityFinder.find(SourcePayrollParameter.class,
                    SourcePayrollParameter.SourceSystemCd().equalTo(SourceSystemCode.QBOE));
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        assertNotNull("Source Payroll Parameter list is null.", startParams);
        assertFalse("Source Payroll Parameter list is empty.", startParams.isEmpty());

        // Change all of the values for all parameters to a test value of 99...
        for (SourcePayrollParameter param : startParams) {
            dtoList.add(new SourcePayrollParameterDTO(
                    SourceSystemCode.QBOE,
                    param.getName(),
                    param.getDescription(),
                    param.getParameterCd(),
                    "99"));
        }

        // Make the change in the database...
        PayrollServices.beginUnitOfWork();
        try {
            ProcessResult<DomainEntitySet<SourcePayrollParameter>> processResult =
                    PayrollServices.payrollManager.updateSourcePayrollParameter(SourceSystemCode.QBOE, dtoList);

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                PayrollServices.rollbackUnitOfWork();
            }

            // validate error count (if processResult.isSuccess() is false, there will be errors...)
            assertEquals("Number of Errors", 0, processResult.getMessages().size());
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            fail(e.getMessage());
        }

        // check to ensure all params were changed as expected
        PayrollServices.beginUnitOfWork();
        try {
            changedParams = PayrollServices.entityFinder.find(SourcePayrollParameter.class,
                    SourcePayrollParameter.SourceSystemCd().equalTo(SourceSystemCode.QBOE));
        } catch (Exception e) {
            fail(e.getMessage());
        } finally{
            PayrollServices.rollbackUnitOfWork();
        }

        assertNotNull("Source Payroll Parameter list is null.", changedParams);
        assertFalse("Source Payroll Parameter list is empty.", changedParams.isEmpty());

        for (SourcePayrollParameter param : changedParams) {
            assertEquals("Parameter value", "99", param.getParameterValue());
        }

        // restore the original param values
        int i = 0;
        for (SourcePayrollParameterDTO param : dtoList) {
            param.setParameterValue(startParams.get(i++).getParameterValue());
        }

        PayrollServices.beginUnitOfWork();
        try {
            ProcessResult<DomainEntitySet<SourcePayrollParameter>> processResult =
                    PayrollServices.payrollManager.updateSourcePayrollParameter(SourceSystemCode.QBOE, dtoList);
            PayrollServices.commitUnitOfWork();

            // finally, validate error count again to make sure everything was changed back...
            assertEquals("Number of Errors", 0, processResult.getMessages().size());
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            fail(e.getMessage());
        }
    }
}
