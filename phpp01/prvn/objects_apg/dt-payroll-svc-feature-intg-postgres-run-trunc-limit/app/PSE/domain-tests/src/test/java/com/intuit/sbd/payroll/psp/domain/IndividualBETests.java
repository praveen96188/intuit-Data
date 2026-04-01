package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**                                       
 * Contains the unit tests for the <CODE>IndividualBE</CODE> class.
 *
 * @author: chetzler
 * @version: Jun 18, 2007
 */

public class IndividualBETests {

    private EntityName entityName;
    private String ownerEntityName;
    private Individual individual;
    private ProcessResult processResult;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        entityName = EntityName.Contact;
        ownerEntityName = "Test Owner";
        individual = new Individual();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void validateValidIndividual()
    {
        processResult = getTestIndividual().validateIndividual(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test
    public void validateFirstNameTooShort()
    {
        individual = getTestIndividual();
        individual.setFirstName("");
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "FirstName has invalid value");
    }

    @Test
    public void validateFirstNameMinLength()
    {
        individual = getTestIndividual();
        individual.setFirstName("r");
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test
    public void validateFirstNameMaxLength()
    {
        individual = getTestIndividual();
        individual.setFirstName(getStringOfSize(40, "WK"));
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test(expected=java.lang.RuntimeException.class)
    public void validateFirstNameTooLong()
    {
        individual = getTestIndividual();
        individual.setFirstName(getStringOfSize(81, "z"));
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "FirstName has invalid value");
    }

    @Test
    public void validateLastNameTooShort()
    {
        individual = getTestIndividual();
        individual.setLastName("");
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "LastName has invalid value");
    }

    @Test
    public void validateLastNameMinLength()
    {
        individual = getTestIndividual();
        individual.setLastName("e");
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test
    public void validateLastNameMaxLength()
    {
        individual = getTestIndividual();
        individual.setLastName(getStringOfSize(40, "AC"));
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test(expected=java.lang.RuntimeException.class)
    public void validateLastNameTooLong()
    {
        individual = getTestIndividual();
        individual.setLastName(getStringOfSize(81, "p"));
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "LastName has invalid value");
    }

    @Test
    public void validateMiddleNameMinLength()
    {
        individual = getTestIndividual();
        individual.setMiddleName("");
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test
    public void validateMiddleNameMaxLength()
    {
        individual = getTestIndividual();
        individual.setMiddleName(getStringOfSize(40, "TT"));
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test(expected=java.lang.RuntimeException.class)
    public void validateMiddleNameTooLong()
    {
        individual = getTestIndividual();
        individual.setMiddleName(getStringOfSize(81, "y"));
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "MiddleName has invalid value");
    }

    @Test
    public void validateInvlaidEmail()
    {
        individual = getTestIndividual();
        individual.setEmail("invalidEmail");
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "Email has invalid value");
    }

    @Test
    public void validatePhoneMinLength()
    {
        individual = getTestIndividual();
        individual.setPhone("");
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test
    public void validatePhoneMaxLength()
    {
        individual = getTestIndividual();
        individual.setPhone(getStringOfSize(20, "5"));
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test
    public void validatePhoneTooLong()
    {
        individual = getTestIndividual();
        individual.setPhone(getStringOfSize(21, "7"));
        processResult = individual.validateIndividual(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "Phone has invalid value");
    }

     @Test
    public void validateGetIndividualFullName()
    {
       individual.setLastName("Doe");
       assertEquals("Doe", individual.getFullName().trim());
       individual.setMiddleName("L.");
       assertEquals("Doe L.", individual.getFullName().trim());
       individual.setFirstName("John");
       assertEquals("Doe, John L.", individual.getFullName().trim());
       individual.setMiddleName(null);
       assertEquals("Doe, John", individual.getFullName().trim());
       individual.setLastName(null);
       assertEquals("John", individual.getFullName().trim());
       individual.setLastName("O'Neal");
       assertEquals("O'Neal, John", individual.getFullName().trim());
    }
    private Individual getTestIndividual()
    {
        Individual individual = new Individual();
        individual.setEmail("tester@intuit.com");
        individual.setFirstName("Tester");
        individual.setLastName("McHappy");
        individual.setMiddleName("Coding");
        individual.setGenderCd(Gender.Female);
        individual.setPhone("775-424-8334");

        Address individualAddr = new Address();
        individualAddr.setAddressLine1("123 High Country Rd");
        individualAddr.setCity("Reno");
        individualAddr.setState("NV");
        individualAddr.setZipCode("89502");
        individual.setMailingAddress(individualAddr);
        return individual;
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

