package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Contains the unit tests for the <CODE>Message</CODE> class.
 *
 * @author: chetzler
 * @version: Jun 20, 2007
 */
public class ProcessResultTests {

    private ProcessResult result;

    @Before
    public void runBeforeEachTest()
    {
        result = new ProcessResult();
    }

    @After
    public void runAfterEachTest(){
    }

    @Test
    public void defaultSuccessValue()
    {
        assertTrue(result.isSuccess());
    }

    @Test
    public void addErrorMessage()
    {
        result = new ProcessResult();
        MessageList messages = result.getMessages();
        messages.NoAccountSignatory(EntityName.Company, "PSP", "QBOE", "PSP");
        assertFalse(result.isSuccess());
    }

    @Test
    public void addWarningMessage()
    {
        result = new ProcessResult();
        MessageList messages = result.getMessages();
        messages.CompanyPendingActivation(EntityName.Company, "PSP", "QBOE", "PSP");
        assertTrue(result.isSuccess());
    }

    @Test (expected = IllegalArgumentException.class)
    public void mergeNullProcessResult()
    {
        try
        {
            result.merge(null);
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("Process Result is null", ex.getMessage());
            throw ex;
        }
    }

    @Test
    public void mergeValidProcessResult()
    {
        ProcessResult newResult = new ProcessResult();
        result.merge(newResult);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getMessages().size());
    }

    @Test
    public void mergeValidProcessResultWithMessages()
    {
        ProcessResult newResult = new ProcessResult();
        MessageList messages = newResult.getMessages();
        messages.NoAccountSignatory(EntityName.Contact, "PSP", "QBOE", "PSP");
        messages.CompanyPendingActivation(EntityName.Company, "PSP", "QBOE", "PSP");

        result.merge(newResult);
        assertFalse(result.isSuccess());
        assertEquals(2, result.getMessages().size());
    }

    @Test
    public void toStringTest()
    {
        final String newLine = System.getProperty("line.separator");

        // Get rid of memory address on original message
        assertEquals("Process Result" + newLine +
                " Success: true" + newLine +
                " Messages: " + newLine , result.toString());
    }
}
