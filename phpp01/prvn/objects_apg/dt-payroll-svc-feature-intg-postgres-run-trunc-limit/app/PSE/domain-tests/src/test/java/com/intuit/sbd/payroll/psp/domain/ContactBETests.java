package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Address;
import com.intuit.sbd.payroll.psp.domain.Contact;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.domain.Gender;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import org.junit.*;
import static org.junit.Assert.assertEquals;

/**
 * Contains the unit tests for the <CODE>ContactBE</CODE> class.
 *
 * @author: chetzler
 * @version: Jun 18, 2007
 */

public class ContactBETests {

    private Contact contact;
    private ProcessResult processResult;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        contact = new Contact();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void validateValidContact()
    {
        processResult = getTestContact().validateContact();
        assertEquals(0, processResult.getMessages().size());
    }

    @Test
    public void validateContactRoleCd()
    {
        contact = getTestContact();
        contact.setContactRoleCd(null);
        processResult = contact.validateContact();
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "ContactRoleCd has invalid value");
    }

    private Contact getTestContact()
    {
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setMiddleName("P");
        contact.setLastName("Doe");
        contact.setPhone("(775) 424-8339");
        contact.setGenderCd(Gender.Male);
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAuthSignerYnInd(Boolean.TRUE);

        Address contactAddr = new Address();
        contactAddr.setAddressLine1("123 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");
        contact.setMailingAddress(contactAddr);
        contact.setSourceContactId(ContactRole.PayrollAdmin.toString()+contact.getLastName()+contact.getFirstName()+contact.getMiddleName());
        return contact;
    }

    private void evaluateMessage(Message messageToEvaluate)
    {
        assertEquals(EntityName.Contact, messageToEvaluate.getEntityName());
        assertEquals(MessageInfo.MessageLevel.ERROR, messageToEvaluate.getLevel());
        assertEquals("5001", messageToEvaluate.getMessageCode());
    }

    private void evaluateMessage(Message messageToEvaluate, String message)
    {
        evaluateMessage(messageToEvaluate);
        assertEquals(message, messageToEvaluate.getMessage());
    }

    private String getStringOfSize(int sizeOfString, String stringToRepeat)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sizeOfString; i++)
        {
            builder.append(stringToRepeat);
        }
        return builder.toString();
    }
}
