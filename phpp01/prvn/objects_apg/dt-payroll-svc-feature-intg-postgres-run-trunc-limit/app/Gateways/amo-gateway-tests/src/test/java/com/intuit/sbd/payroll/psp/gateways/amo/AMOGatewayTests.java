package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 9, 2010
 * Time: 1:31:52 PM
 */
public class AMOGatewayTests {

    @Before
    public void beforeEachTest() {
        AMOMockGateway.getMessages().clear();
    }

    @After
    public void afterEachTest() {
        AMOGatewayFactory.setInstanceClass(AMOGateway.class);
        AMOMockGateway.setWriteMessagesToFiles(false);
        AMOMockGateway.clearMessages();
        deleteMessageFiles();
    }

    private void deleteMessageFiles() {
        File messageDir = new File(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_message_dir"));
        File[] files = messageDir.listFiles();
        if(files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    // only used to manually test connectivity... There is no way to consistently test receiving AMO messages
    @Test
    @Ignore
    public void testAMOConnection() {
        AbstractAMOGateway amoGateway = AMOGatewayFactory.createInstance();
        Collection<AMODTO> amodtos = amoGateway.getMessages(20);

        for (AMODTO amodto : amodtos) {
            amodto.getEntitlementOfferingCode();
        }

    }

    @Test
    public void testMessageProcessing_skipUnmappedItemNumbers() {
        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);
        AbstractAMOGateway amoGateway = AMOGatewayFactory.createInstance();

        // should be skipped
        Message message = new Message();
        Entitlement entitlement = new Entitlement("568", "1234", "12345", "12345678", "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        // fist asset should be skipped, but not second
        message = new Message();
        entitlement = new Entitlement("568", "1234", "12345", "12345678", "Customer1", "89511");
        message.entitlements.add(entitlement);
        entitlement = new Entitlement("123", "1234", "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        Collection<AMODTO> amodtos = amoGateway.getMessages(20);
        assertEquals("filtered dtos", 1, amodtos.size());
        for (AMODTO amodto : amodtos) {
            assertEquals("messages", 1, amodto.getMessages().size());
            assertEquals("license number", "123", amodto.getLicenseNumber());
        }
    }

    @Test
    public void testMessageProcessing_skipMessagesWithoutEntitlements() {
        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);
        AbstractAMOGateway amoGateway = AMOGatewayFactory.createInstance();

        // should be skipped due to null entitlement
        Message message = new Message();
        Entitlement entitlement = new Entitlement(null, null, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        // should not be skipped
        message = new Message();
        entitlement = new Entitlement("123", "1234", "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        Collection<AMODTO> amodtos = amoGateway.getMessages(20);
        assertEquals("filtered dtos", 1, amodtos.size());
        for (AMODTO amodto : amodtos) {
            assertEquals("messages", 1, amodto.getMessages().size());
            assertEquals("license number", "123", amodto.getLicenseNumber());
        }
    }

    @Test
    public void testMessageProcessing_skipMessagesWithoutIdentifiers() {
        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);
        AbstractAMOGateway amoGateway = AMOGatewayFactory.createInstance();

        // should be skipped due to null license number
        Message message = new Message();
        Entitlement entitlement = new Entitlement(null, "1458", "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        // should be skipped due to null eoc
        message = new Message();
        entitlement = new Entitlement("789", null, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        // should not be skipped
        message = new Message();
        entitlement = new Entitlement("123", "1234", "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        Collection<AMODTO> amodtos = amoGateway.getMessages(20);
        assertEquals("filtered dtos", 1, amodtos.size());
        for (AMODTO amodto : amodtos) {
            assertEquals("messages", 1, amodto.getMessages().size());
            assertEquals("license number", "123", amodto.getLicenseNumber());
        }
    }

    @Test
    public void testMessageProcessing_messageEntitlementMapping() {
        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);
        AbstractAMOGateway amoGateway = AMOGatewayFactory.createInstance();

        Message message = new Message();
        Entitlement entitlement = new Entitlement("1", "1", "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        message = new Message();
        entitlement = new Entitlement("1", "1", null, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        message = new Message();
        entitlement = new Entitlement("1", "2", "587", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        // skipped - incorrect item number
        message = new Message();
        entitlement = new Entitlement("1", "2", "587", "123456", "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        message = new Message();
        entitlement = new Entitlement("1", "2", null, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        entitlement = new Entitlement("3", "3", null, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        Collection<AMODTO> amodtos = amoGateway.getMessages(20);
        assertEquals("filtered dtos", 3, amodtos.size());
        for (AMODTO amodto : amodtos) {
            String licenseNumber = amodto.getLicenseNumber();
            String eoc = amodto.getEntitlementOfferingCode();
            if(licenseNumber.equals("1") && eoc.equals("1")) {
                assertEquals("messages", 2, amodto.getMessages().size());
            } else if(licenseNumber.equals("1") && eoc.equals("2")) {
                assertEquals("messages", 2, amodto.getMessages().size());
            } else if(licenseNumber.equals("3") && eoc.equals("3")) {
                assertEquals("messages", 1, amodto.getMessages().size());
            } else {
                fail("message with unexpected lic#: " + licenseNumber + " and eoc: " + eoc + " found");
            }

        }
    }

    @Test
    public void testWriteAndReadFiles() {
        deleteMessageFiles();
        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);
        AMOMockGateway.setWriteMessagesToFiles(true);
        AbstractAMOGateway amoGateway = AMOGatewayFactory.createInstance();

        Message message = new Message();
        Entitlement entitlement = new Entitlement("1", "1", "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        message = new Message();
        entitlement = new Entitlement("1", "1", null, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        message = new Message();
        entitlement = new Entitlement("1", "2", "587", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        // skipped - incorrect item number
        message = new Message();
        entitlement = new Entitlement("1", "2", "587", "123456", "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        message = new Message();
        entitlement = new Entitlement("1", "2", null, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        entitlement = new Entitlement("3", "3", null, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        Collection<AMODTO> amodtos = amoGateway.getMessages(20);
        assertEquals("filtered dtos", 0, amodtos.size());
        AMOMockGateway.setWriteMessagesToFiles(false);
        AMOMockGateway.clearMessages();

        amodtos = amoGateway.getMessages(20);
        assertEquals("filtered dtos", 3, amodtos.size());
        AMOMockGateway.setWriteMessagesToFiles(false);
        for (AMODTO amodto : amodtos) {
            String licenseNumber = amodto.getLicenseNumber();
            String eoc = amodto.getEntitlementOfferingCode();
            if(licenseNumber.equals("1") && eoc.equals("1")) {
                assertEquals("messages", 2, amodto.getMessages().size());
            } else if(licenseNumber.equals("1") && eoc.equals("2")) {
                assertEquals("messages", 2, amodto.getMessages().size());
            } else if(licenseNumber.equals("3") && eoc.equals("3")) {
                assertEquals("messages", 1, amodto.getMessages().size());
            } else {
                fail("message with unexpected lic#: " + licenseNumber + " and eoc: " + eoc + " found");
            }

        }
    }
}
