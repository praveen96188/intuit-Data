package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementMessageDTO;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessage;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessageStatusCode;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 14, 2010
 * Time: 9:54:14 AM
 */
public class AddUpdateEntitlementMessageCoreTests {

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
    public void testAddEntitlementMessage_ValidationErrors() {
        // null dto
        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementMessage> entitlementMessageProcessResult = PayrollServices.entitlementManager.addEntitlementMessage(null);
        PSP_PRAssert.assertContains("validation error", 5001, MessageInfo.MessageLevel.ERROR, entitlementMessageProcessResult);
        PayrollServices.rollbackUnitOfWork();

        EntitlementMessageDTO entitlementMessageDTO = new EntitlementMessageDTO();

        // invalid license
        PayrollServices.beginUnitOfWork();
        entitlementMessageProcessResult = PayrollServices.entitlementManager.addEntitlementMessage(entitlementMessageDTO);
        PSP_PRAssert.assertContains("validation error", 5001, MessageInfo.MessageLevel.ERROR, entitlementMessageProcessResult);
        PayrollServices.rollbackUnitOfWork();

        entitlementMessageDTO.setLicenseNumber("123");

        // invalid EOC
        PayrollServices.beginUnitOfWork();
        entitlementMessageProcessResult = PayrollServices.entitlementManager.addEntitlementMessage(entitlementMessageDTO);
        PSP_PRAssert.assertContains("validation error", 5001, MessageInfo.MessageLevel.ERROR, entitlementMessageProcessResult);
        PayrollServices.rollbackUnitOfWork();

        entitlementMessageDTO.setEntitlementOfferingCode("1234");

        // invalid order number
        PayrollServices.beginUnitOfWork();
        entitlementMessageProcessResult = PayrollServices.entitlementManager.addEntitlementMessage(entitlementMessageDTO);
        PSP_PRAssert.assertContains("validation error", 5001, MessageInfo.MessageLevel.ERROR, entitlementMessageProcessResult);
        PayrollServices.rollbackUnitOfWork();

        entitlementMessageDTO.setOrderNumber("12345");

        // invalid Message
        PayrollServices.beginUnitOfWork();
        entitlementMessageProcessResult = PayrollServices.entitlementManager.addEntitlementMessage(entitlementMessageDTO);
        PSP_PRAssert.assertContains("validation error", 5001, MessageInfo.MessageLevel.ERROR, entitlementMessageProcessResult);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAddEntitlementMessage_HappyPath() {
        EntitlementMessageDTO entitlementMessageDTO = new EntitlementMessageDTO();
        entitlementMessageDTO.setLicenseNumber("123");
        entitlementMessageDTO.setEntitlementOfferingCode("1234");
        entitlementMessageDTO.setOrderNumber("12345");
        entitlementMessageDTO.setMessage("This is a message received from AMO.");

        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementMessage> entitlementMessageProcessResult = PayrollServices.entitlementManager.addEntitlementMessage(entitlementMessageDTO);
        PSP_PRAssert.assertSuccess("message added", entitlementMessageProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals("number of messages", 1, entitlementMessages.size());
        assertEquals("license number", "123", entitlementMessages.get(0).getLicenseNumber());
        assertEquals("eoc", "1234", entitlementMessages.get(0).getEntitlementOfferingCode());
        assertEquals("order number", "12345", entitlementMessages.get(0).getOrderNumber());
        assertEquals("message status", EntitlementMessageStatusCode.New, entitlementMessages.get(0).getStatus());
        assertEquals("message", "This is a message received from AMO.", entitlementMessages.get(0).getMessage());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testUpdateEntitlementMessage_ValidationErrors() {
        // null dto
        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementMessage> entitlementMessageProcessResult = PayrollServices.entitlementManager.updateEntitlementMessage(
                null,
                null,
                null,
                null);
        PSP_PRAssert.assertContains("validation error", 5001, MessageInfo.MessageLevel.ERROR, entitlementMessageProcessResult);
        PayrollServices.rollbackUnitOfWork();

        // invalid message id
        PayrollServices.beginUnitOfWork();
        entitlementMessageProcessResult = PayrollServices.entitlementManager.updateEntitlementMessage(
                "cd0894e2-0548-43e0-b8f2-3fd1eb7eb8ef",
                null,
                EntitlementMessageStatusCode.Processed,
                null);
        PSP_PRAssert.assertContains("validation error", 318, MessageInfo.MessageLevel.ERROR, entitlementMessageProcessResult);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testUpdateEntitlementMessage_HappyPath() {
        testAddEntitlementMessage_HappyPath();

        String messageId;
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals("number of messages", 1, entitlementMessages.size());
        messageId = entitlementMessages.get(0).getId().toString();
        String licenseNumber = entitlementMessages.get(0).getLicenseNumber();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementMessage> entitlementMessageProcessResult = PayrollServices.entitlementManager.updateEntitlementMessage(
                messageId,
                licenseNumber,
                EntitlementMessageStatusCode.Processed,
                null);
        PSP_PRAssert.assertSuccess("message updated", entitlementMessageProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals("number of messages", 1, entitlementMessages.size());
        assertEquals("license number", "123", entitlementMessages.get(0).getLicenseNumber());
        assertEquals("eoc", "1234", entitlementMessages.get(0).getEntitlementOfferingCode());
        assertEquals("order number", "12345", entitlementMessages.get(0).getOrderNumber());
        assertEquals("message status", EntitlementMessageStatusCode.Processed, entitlementMessages.get(0).getStatus());
        assertEquals("message", "This is a message received from AMO.", entitlementMessages.get(0).getMessage());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testUpdateEntitlementMessage_errorCount() {
        testAddEntitlementMessage_HappyPath();

        String errorMessage1 = "An Error";
        String errorMessage2 = "Also an Error";

        String messageId;
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals("number of messages", 1, entitlementMessages.size());
        messageId = entitlementMessages.get(0).getId().toString();
        String licenseNumber = entitlementMessages.get(0).getLicenseNumber();
        assertEquals("message status", EntitlementMessageStatusCode.New, entitlementMessages.get(0).getStatus());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementMessage> entitlementMessageProcessResult = PayrollServices.entitlementManager.updateEntitlementMessage(messageId,licenseNumber, null, errorMessage1);
        PSP_PRAssert.assertSuccess("message updated", entitlementMessageProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals("number of messages", 1, entitlementMessages.size());
        assertEquals("license number", "123", entitlementMessages.get(0).getLicenseNumber());
        assertEquals("eoc", "1234", entitlementMessages.get(0).getEntitlementOfferingCode());
        assertEquals("order number", "12345", entitlementMessages.get(0).getOrderNumber());
        assertEquals("message status", EntitlementMessageStatusCode.New, entitlementMessages.get(0).getStatus());
        assertEquals("message", "This is a message received from AMO.", entitlementMessages.get(0).getMessage());
        assertEquals("error message", errorMessage1, entitlementMessages.get(0).getLastFailureMessage());
        assertEquals("error count", 1, entitlementMessages.get(0).getFailureCount());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementMessageProcessResult = PayrollServices.entitlementManager.updateEntitlementMessage(messageId, licenseNumber, null, errorMessage2);
        PSP_PRAssert.assertSuccess("message updated", entitlementMessageProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals("number of messages", 1, entitlementMessages.size());
        assertEquals("message status", EntitlementMessageStatusCode.New, entitlementMessages.get(0).getStatus());
        assertEquals("error message", errorMessage2, entitlementMessages.get(0).getLastFailureMessage());
        assertEquals("error count", 2, entitlementMessages.get(0).getFailureCount());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementMessageProcessResult = PayrollServices.entitlementManager.updateEntitlementMessage(messageId, licenseNumber, null, errorMessage2);
        PSP_PRAssert.assertSuccess("message updated", entitlementMessageProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals("number of messages", 1, entitlementMessages.size());
        assertEquals("message status", EntitlementMessageStatusCode.New, entitlementMessages.get(0).getStatus());
        assertEquals("error message", errorMessage2, entitlementMessages.get(0).getLastFailureMessage());
        assertEquals("error count", 3, entitlementMessages.get(0).getFailureCount());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementMessageProcessResult = PayrollServices.entitlementManager.updateEntitlementMessage(messageId, licenseNumber, null, errorMessage2);
        PSP_PRAssert.assertSuccess("message updated", entitlementMessageProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals("number of messages", 1, entitlementMessages.size());
        assertEquals("message status", EntitlementMessageStatusCode.New, entitlementMessages.get(0).getStatus());
        assertEquals("error message", errorMessage2, entitlementMessages.get(0).getLastFailureMessage());
        assertEquals("error count", 4, entitlementMessages.get(0).getFailureCount());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementMessageProcessResult = PayrollServices.entitlementManager.updateEntitlementMessage(messageId, licenseNumber, null, errorMessage2);
        PSP_PRAssert.assertSuccess("message updated", entitlementMessageProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals("number of messages", 1, entitlementMessages.size());
        assertEquals("message status", EntitlementMessageStatusCode.Error, entitlementMessages.get(0).getStatus());
        assertEquals("error message", errorMessage2, entitlementMessages.get(0).getLastFailureMessage());
        assertEquals("error count", 5, entitlementMessages.get(0).getFailureCount());
        PayrollServices.rollbackUnitOfWork();
    }
}
