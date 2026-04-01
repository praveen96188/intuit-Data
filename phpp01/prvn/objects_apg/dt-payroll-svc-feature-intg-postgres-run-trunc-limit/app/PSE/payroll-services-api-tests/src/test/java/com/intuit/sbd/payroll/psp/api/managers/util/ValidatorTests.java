package com.intuit.sbd.payroll.psp.api.managers.util;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Contains the unit tests for the <CODE>Validator</CODE> class.
 *
 * @author: chetzler
 * @version: Jun 18, 2007
 */

public class ValidatorTests {
    @Before
    public void runBeforeEachTest()
    {
    }

    @After
    public void runAfterEachTest(){
    }

   

    @Test
    public void testNullSourceSystemCd() {
        ProcessResult procResult = Validator.validCompanyParameters(null, "12345");
        assertFalse(procResult.isSuccess());

        Message resultingMessage = procResult.getMessages().get(0);

        assertEquals("Number of messages: ", 1, procResult.getMessages().size());
        assertEquals("Message Code: ", "137", resultingMessage.getMessageCode());
        assertEquals("Message: ", "Source System Code is not specified.", resultingMessage.getMessage());
        assertEquals("Entity Name: ", EntityName.Company, resultingMessage.getEntityName());
        assertEquals("Level: ", MessageInfo.MessageLevel.ERROR, resultingMessage.getLevel());
        assertEquals("Source Id: ", "12345", resultingMessage.getSourceId());

    }

    // dummy enum is used to avoid dependency on the Domain module
    private enum DummyEnum {
        QBOE;
    }

    @Test
    public void testNullSourceCompanyId() {
        ProcessResult procResult = Validator.validCompanyParameters( DummyEnum.QBOE, null);
        assertFalse(procResult.isSuccess());

        assertEquals("Number of messages: ", 1, procResult.getMessages().size());
        Message resultingMessage = procResult.getMessages().get(0);

        assertEquals("Message Code: ", "138", resultingMessage.getMessageCode());
        assertEquals("Message: ", "Source Company ID is not specified.", resultingMessage.getMessage());
        assertEquals("Entity Name: ", EntityName.Company, resultingMessage.getEntityName());
        assertEquals("Level: ", MessageInfo.MessageLevel.ERROR, resultingMessage.getLevel());
        assertEquals("Source Id: ", null, resultingMessage.getSourceId());
    }

    @Test
    public void testNullSourceIdNullSourceSystem() {
        ProcessResult procResult = Validator.validCompanyParameters(null, null);
        assertFalse(procResult.isSuccess());

        assertEquals("Number of messages: ", 2, procResult.getMessages().size());
    }

    @Test
    public void testValidCompanyParameters() {
        ProcessResult procResult = Validator.validCompanyParameters(DummyEnum.QBOE, "1234");
        assertTrue(procResult.isSuccess());

        assertEquals("Number of messages: ", 0, procResult.getMessages().size());
    }
}

