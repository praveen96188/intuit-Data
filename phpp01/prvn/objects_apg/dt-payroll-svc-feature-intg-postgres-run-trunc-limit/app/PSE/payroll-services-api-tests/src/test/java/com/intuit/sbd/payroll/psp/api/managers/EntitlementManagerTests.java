package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.common.utils.ServiceKey;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 28, 2010
 * Time: 4:45:07 PM
 */
public class EntitlementManagerTests {
    @Test
    public void testAllServiceTypes() {
        System.out.println("Test all service types");
        ServiceKey instance;
        String ein;
        double limit;
        long promo;
        String key;

        key = "14005-03464-705-847";
        ein = "989232474";
        instance = new ServiceKey(key, ein);
        limit = instance.getCreditLimit();
        promo = instance.getPromoCode();
        assertEquals(50000.0, limit, 0.0);
        ServiceKey instance2 = new ServiceKey(limit, promo, ein, ServiceKey.ServiceType.SVC_TAX);
        assertEquals(key, instance2.toString());

        instance = new ServiceKey("21000-10242-703-145", "770327851");
        assertEquals(0.0, instance.getCreditLimit(), 0.0);
        assertEquals(703145, instance.getPromoCode());
        assertEquals(ServiceKey.ServiceType.SVC_DIRDEP, instance.getServiceType());

        instance = new ServiceKey("31004-65850-703-145", "891237471");
        assertEquals(40000.0, instance.getCreditLimit(), 0.0);
        assertEquals(703145, instance.getPromoCode());
        assertEquals(ServiceKey.ServiceType.SVC_TAX_DIRDEP, instance.getServiceType());

        String subId = "0001468422";
        ein = "201311591";

        instance = new ServiceKey("7001-0001-0468-6423", ein);
        assertEquals(subId, instance.getSubscriptionID());

        instance = new ServiceKey("5001-0001-0468-4423", ein);
        assertEquals(subId, instance.getSubscriptionID());

        // REMINDER: GregorianCalendar is constructed with a zero-based month.
        Calendar expireDate = new GregorianCalendar(2007, 11, 31);
        instance = new ServiceKey(ein, subId, expireDate, 0);
        assertEquals("8001-0001-0468-7423 1402-2062-3747", instance.toString());
        assertEquals("1402-2062-3747", instance.getExtensionKey());
        assertEquals("1402-2062-3747", ServiceKey.getExtensionKey(subId, ServiceKey.PSExtensionType.EXT_EXPIREDATE, expireDate, 0));

        expireDate.set(2015, 7, 31);
        instance = new ServiceKey("8002-7489-0789-4714", "1792-5517-3798", "782739122");
        assertEquals(expireDate, instance.getExpirationDate());
        assertEquals(9, instance.getSubType());

        instance = new ServiceKey("8001-0001-0468-7423", "1402-2062-3747", ein);
        assertEquals("8001-0001-0468-7423 1402-2062-3747", instance.toString());

        instance = new ServiceKey("4005-1336-0765-5456", "654885456");
        assertEquals("0005766248", instance.getSubscriptionID());

        instance = new ServiceKey("A001-0337-0982-7185", "752842057");
        assertEquals("0001982392", instance.getSubscriptionID());

        instance = new ServiceKey("B001-0248-0998-9340", "650428340");
        assertEquals("0001998588", instance.getSubscriptionID());

        instance = new ServiceKey("D001-0006-0838-9451", "383307923");
        assertEquals("0001838453", instance.getSubscriptionID());

        instance = new ServiceKey("E001-0005-0838-9451", "383307923");
        assertEquals("0001838454", instance.getSubscriptionID());

        instance = new ServiceKey("4005-4094-0166-0597", "731695573");

        instance = new ServiceKey("731695573", "0005167611", ServiceKey.ServiceType.SVC_BASIC);
        assertEquals("0005167611", instance.getSubscriptionID());
    }

    @Test
    public void testEncode() {
        System.out.println("Encode");

        String m_subID = "5766248";
        String m_ein = "654885456";

        ServiceKey.ServiceType m_service = ServiceKey.ServiceType.SVC_BASIC;
        String expResult = "4005-1336-0765-5456";
        ServiceKey instance = new ServiceKey(m_ein, m_subID, m_service);
        assertEquals(expResult, instance.toString());
    }

    @Test
    public void testEncode_static() {
        System.out.println("Encode Static");
        String m_subID = "5766248";
        String m_ein = "654885456";
        ServiceKey.ServiceType m_service = ServiceKey.ServiceType.SVC_BASIC;
        String expResult = "4005-1336-0765-5456";
        String result = ServiceKey.encode(m_subID, m_service, m_ein);
        assertEquals(expResult, result);
    }

    @Test
    public void testCRIFormatEncodeStatic() {
        String ein = "020488805";
        String key = "20000-78401-703-044";
        String result = ServiceKey.encodeCRIFormat(ein, 0.0, 703044, ServiceKey.ServiceType.SVC_DIRDEP);
        assertEquals(key, result);
    }

    @Test
    public void testOldAssistedKey() {
        System.out.println("Decode Old Assisted Key");
        String key = "7000-0000-0114-8348";
        String ein = "770324892";
        ServiceKey instance = new ServiceKey(key, ein);
        String subscriptionNumber = instance.getSubscriptionID();
        assertEquals("0000114348", subscriptionNumber);
    }

    @Test
    public void testEncodeDecode() {
        Random random = new Random(System.currentTimeMillis());
        final int cycles = 5000;
        System.out.printf("Encode and Decode %d service keys\n", cycles);
        for (int i = 0; i < cycles; i++) {
            int ein = Math.abs(random.nextInt());
            String einString = String.format("%09d", ein % 1000000000);

            int subId = Math.abs(random.nextInt());
            String subIdString = String.format("%010d", subId % 100000000);

            ServiceKey instance = new ServiceKey(einString, subIdString, ServiceKey.ServiceType.SVC_BASIC);
            String serviceKey = instance.toString();
            String returnedSubId = ServiceKey.decode(serviceKey);
            assertEquals(subIdString, returnedSubId);
        }
    }

    @Test
    public void testDiskDeliveryKey() {
        System.out.println("Disk Delivery Key");
        // REMINDER: GregorianCalendar is constructed with a zero-based month.
        Calendar expireDate = new GregorianCalendar(2010, 5, 30);
        ServiceKey instance = new ServiceKey("123412341", "00054151455", expireDate, 0);
        instance.dump(System.out);
        String extensionNumber = instance.getExtensionKey();
        System.out.println(String.format("Disk Delivery Key: %s", extensionNumber));
        assertEquals("1002-6112-4346", extensionNumber);
    }

    @Test
    public void testToString() {
        System.out.println("Test toString");
        ServiceKey instance = new ServiceKey("4005-1336-0765-5456", "654885456");
        String result = instance.toString();
        System.out.println(result);
        assertEquals("0005766248", instance.getSubscriptionID());
    }

    @Test
    public void testInvalidServiceKey() {
        System.out.println("Negative test: invalid service key:");
        try {
            new ServiceKey("9123-2938-1923-7477", "672837198");
        } catch (Exception e) {
            System.out.printf("\tExpected exception was thrown: %s", e.toString());
            assertTrue(e instanceof IllegalArgumentException);
            return;
        }
        fail("Expected exception not thrown");
    }

    @Test
    public void testInvalidServiceTypeForCRIKey() {
        System.out.println("Negative test: CRI key with non-CRI service type");
        try {
            new ServiceKey(50000.0, 123456, "872373827", ServiceKey.ServiceType.SVC_BASIC);
        } catch (Exception e) {
            System.out.printf("\tExpected exception was thrown: %s", e.toString());
            assertTrue(e instanceof IllegalArgumentException);
            return;
        }
        fail("\tExpected exception not thrown");
    }

    @Test
    public void testInvalidServiceTypeForSubscriptionKey() {
        System.out.println("Negative test: Subscription key with CRI service type");
        try {
            new ServiceKey("123876456", "7736271", ServiceKey.ServiceType.SVC_TAX);
        } catch (Exception e) {
            System.out.printf("\tExpected exception was thrown: %s", e.toString());
            assertTrue(e instanceof IllegalArgumentException);
            return;
        }
        fail("\tExpected exception not thrown");

    }

    @Test
    public void testWrongConstructorForDiskDelivery() {
        try {
            Calendar expireDate = new GregorianCalendar(2015, 7, 31);
            ServiceKey instance = new ServiceKey("8002-7489-0789-4714", "782739122");
        } catch (Exception e) {
            System.out.printf("\tExpected exception was thrown: %s", e.toString());
            assertTrue(e instanceof IllegalArgumentException);
            return;
        }
        fail("\tExpected exception not thrown");
    }
}
