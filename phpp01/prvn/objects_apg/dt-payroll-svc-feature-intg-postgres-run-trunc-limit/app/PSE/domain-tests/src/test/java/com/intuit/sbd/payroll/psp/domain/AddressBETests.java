package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Address;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import org.junit.*;
import static org.junit.Assert.assertEquals;

/**
 * Contains the unit tests for the <CODE>AddressBE</CODE> class.
 *
 * @author: chetzler
 * @version: Jun 18, 2007
 */
public class AddressBETests {

    private EntityName entityName;
    private String ownerEntityName;
    private Address address;
    private ProcessResult processResult;


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        entityName = EntityName.Address;
        ownerEntityName = "Test Owner";
        address = new Address();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void validateValidAddress()
    {
        processResult = getTestIntuitAddress().validateAddress(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test
    public void validateAddressLine1TooShort()
    {
        address = getTestIntuitAddress();
        address.setAddressLine1("");
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "AddressLine1 has invalid value");
    }

    @Test
    public void validateAddressLine1MaxLength()
    {
        address = getTestIntuitAddress();
        address.setAddressLine1(getStringOfSize(80, "0"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test(expected=java.lang.RuntimeException.class)
    public void validateAddressLine1TooLong()
    {
        address = getTestIntuitAddress();
        address.setAddressLine1(getStringOfSize(81, "1"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "AddressLine1 has invalid value");
    }

    @Test(expected=java.lang.RuntimeException.class)
    public void validateAddressLine2TooLong()
    {
        address = getTestIntuitAddress();
        address.setAddressLine2(getStringOfSize(81, "5"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "AddressLine2 has invalid value");
    }

    @Test(expected=java.lang.RuntimeException.class)
    public void validateAddressLine3TooLong()
    {
        address = getTestIntuitAddress();
        address.setAddressLine3(getStringOfSize(81, "8"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "AddressLine3 has invalid value");
    }

    @Test
    public void validateCityTooShort()
    {
        address = getTestIntuitAddress();
        address.setCity("");
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "City has invalid value");
    }

    @Test
    public void validateCityMaxLength()
    {
        address = getTestIntuitAddress();
        address.setCity(getStringOfSize(255, "A"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test
    public void validateCityTooLong()
    {
        address = getTestIntuitAddress();
        address.setCity(getStringOfSize(256, "1"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "City has invalid value");
    }

    @Test
    public void validateStateTooShort()
    {
        address = getTestIntuitAddress();
        address.setState("");
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "State has invalid value");
    }

    @Test
    public void validateStateMaxLength()
    {
        address = getTestIntuitAddress();
        address.setState(getStringOfSize(21, "b"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test(expected=java.lang.RuntimeException.class)
    public void validateStateTooLong()
    {
        address = getTestIntuitAddress();
        address.setState(getStringOfSize(22, "1"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "State has invalid value");
    }

    @Test
    public void validateZipCodeTooShort()
    {
        address = getTestIntuitAddress();
        address.setZipCode("");
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "ZipCode has invalid value");
    }

    @Test
    public void validateZipCodeMaxLength()
    {
        address = getTestIntuitAddress();
        address.setAddressLine1(getStringOfSize(13, "9"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test(expected=java.lang.RuntimeException.class)
    public void validateZipCodeTooLong()
    {
        address = getTestIntuitAddress();
        address.setZipCode(getStringOfSize(14, "7"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "ZipCode has invalid value");
    }

    @Test
    public void validateZipCodeExtensionMaxLength()
    {
        address = getTestIntuitAddress();
        address.setZipCodeExtension(getStringOfSize(10, "5"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test(expected=java.lang.RuntimeException.class)
    public void validateZipCodeExtensionTooLong()
    {
        address = getTestIntuitAddress();
        address.setZipCodeExtension(getStringOfSize(11, "7"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "ZipCodeExtension has invalid value");
    }

    @Test
    public void validateCountryMaxLength()
    {
        address = getTestIntuitAddress();
        address.setCountry(getStringOfSize(255, "r"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(0, processResult.getMessages().size());
    }

    @Test
    public void validateCountryTooLong()
    {
        address = getTestIntuitAddress();
        address.setCountry(getStringOfSize(256, "x"));
        processResult = address.validateAddress(entityName, ownerEntityName);
        assertEquals(1, processResult.getMessages().size());
        evaluateMessage(processResult.getMessages().get(0), "Country has invalid value");
    }

    private Address getTestIntuitAddress()
    {
        Address legalAddress = new Address();
        legalAddress.setAddressLine1("6888 Sierra Cnt Pkwy");
        legalAddress.setCity("Reno");
        legalAddress.setZipCode("89511");
        legalAddress.setState("NV");
        return legalAddress;
    }

    private void evaluateMessage(Message messageToEvaluate)
    {
        assertEquals(EntityName.Address, messageToEvaluate.getEntityName());
        assertEquals(MessageInfo.MessageLevel.ERROR, messageToEvaluate.getLevel());
        assertEquals("5001", messageToEvaluate.getMessageCode());
        assertEquals(ownerEntityName, messageToEvaluate.getSourceId());
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
