package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo.MessageLevel;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Contains the unit tests for the <CODE>Message</CODE> class.
 *
 * @author: chetzler
 * @version: Jun 20, 2007
 */
public class MessageTests {

    private static MessageList messageList;

    @BeforeClass
    public static void initialize() {
        messageList = new MessageList();
    }

    @AfterClass
    public static void tearDown() throws Exception
    {

    }

    @Before
    public void runBeforeEachTest()
    {

    }

    @After
    public void runAfterEachTest(){

    }

    /** Commenting out until we decide if this is necessary since adding messages is now done in MessageList itself
    @Test (expected = MissingResourceException.class)
    public void getMessageWithNullText()
    {
        try
        {
            messageList.add
            messageList.add(0, null);
        }
        catch (MissingResourceException ex)
        {
            assertEquals("Message Code not found in the message resource file: Code", ex.getMessage());
            throw ex;
        }
    }
     **/

    @Test
    public void toStringTest()
    {
        messageList = new MessageList();
        final String newLine = System.getProperty("line.separator");
        messageList.EinInUse(EntityName.Company, "PSP", "QBOE", "PSP", "12345789");
        assertEquals(1, messageList.size());

        Message message = messageList.get(0);
        assertEquals(MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals("Company QBOE:PSP could not be added because the EIN 12345789 is already in use.", message.getMessage());
        assertEquals(EntityName.Company, message.getEntityName());
        assertEquals("PSP", message.getSourceId());
        assertEquals("1038", message.getMessageCode());
        assertEquals(" Message" + newLine +
                "   Level: ERROR" + newLine +
                "   Message Code: 1038" + newLine +
                "   Message: Company QBOE:PSP could not be added because the EIN 12345789 is already in use." + newLine +
                "   Source Id: PSP" + newLine +
                "   Entity Name: Company" + newLine, message.toString());
    }

    @Test
    public void getMessageWithWarningText()
    {
        messageList = new MessageList();
        messageList.CompanyPendingActivation(EntityName.Company, "sourceId", "QBOE", "sourceId");
        assertEquals(1, messageList.size());
        Message currMessage = messageList.get(0);
        assertEquals(MessageLevel.WARNING, currMessage.getLevel());
    }
}
