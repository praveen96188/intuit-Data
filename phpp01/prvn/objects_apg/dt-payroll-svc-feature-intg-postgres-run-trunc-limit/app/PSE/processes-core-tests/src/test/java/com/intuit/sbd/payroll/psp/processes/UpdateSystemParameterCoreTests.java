package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.SystemParameterCodeDTO;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * User: mvillani
 * Date: Nov 15, 2007
 * Time: 11:52:07 AM
 */
public class UpdateSystemParameterCoreTests {
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
    public void testUpdateSystemParameterSuccess() {
        PayrollServices.beginUnitOfWork();

        UpdateSystemParameterCore core = new  UpdateSystemParameterCore(SystemParameter.Code.AS400_TOKEN, "1");
        ProcessResult processResult = core.execute();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        //Retrieve the updated System Parameter
        SystemParameter parameter = core.getSystemParameter();
        assertNotNull(parameter);
        assertEquals("System Param Value", "1", parameter.getSystemParameterValue());
    }

    @Test
    public void testUpdateSystemParameterValueTooLong() {
        PayrollServices.beginUnitOfWork();

        StringBuffer testValue = new StringBuffer();

        for (int x=0; x<401;x++) {
            testValue.append("x");
        }

        UpdateSystemParameterCore core = new  UpdateSystemParameterCore(SystemParameter.Code.AS400_TOKEN, testValue.toString());
        ProcessResult processResult = core.execute();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);
        
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Entity:", EntityName.SystemParameter, message.getEntityName());
        assertEquals("Error Code:", "5001", message.getMessageCode());
        assertEquals("Error Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "System Parameter Value has invalid value", message.getMessage());
    }

    @Test
    public void testUpdateSystemParameterValueZeroLength() {
        PayrollServices.beginUnitOfWork();

        StringBuffer testValue = new StringBuffer();
        UpdateSystemParameterCore core = new  UpdateSystemParameterCore(SystemParameter.Code.AS400_TOKEN, testValue.toString());
        ProcessResult processResult = core.execute();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        Message message = processResult.getMessages().get(0);
        assertEquals("Error Entity:", EntityName.SystemParameter, message.getEntityName());
        assertEquals("Error Code:", "5001", message.getMessageCode());
        assertEquals("Error Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "System Parameter Value has invalid value", message.getMessage());
    }

    @Test
    public void testUpdateSystemParameterValueNull() {
        PayrollServices.beginUnitOfWork();

        UpdateSystemParameterCore core = new  UpdateSystemParameterCore(SystemParameter.Code.AS400_TOKEN, null);
        ProcessResult processResult = core.execute();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Entity:", EntityName.SystemParameter, message.getEntityName());
        assertEquals("Error Code:", "5001", message.getMessageCode());
        assertEquals("Error Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "System Parameter Value has invalid value", message.getMessage());
    }

    // Payroll Services API Test (look toward that)
}
