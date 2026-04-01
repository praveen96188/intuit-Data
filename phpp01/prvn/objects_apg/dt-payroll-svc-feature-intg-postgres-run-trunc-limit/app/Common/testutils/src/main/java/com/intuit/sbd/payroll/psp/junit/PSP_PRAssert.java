package com.intuit.sbd.payroll.psp.junit;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static junit.framework.Assert.assertEquals;

/**
 * User: rnorian
 * Date: Mar 26, 2010
 * Time: 3:38:18 PM
 */
public class PSP_PRAssert { 
    public static void assertSuccess(String assertMessage, ProcessResult processResult) {
        if (!processResult.isSuccess()) {
            System.out.println("processResult = ");
            for (Message message : processResult.getMessages()) {
                System.out.println(message.getMessageCode() + ":" + message.getMessage() + " at " + message.getInterestingStackElement());
            }
        }
        assertTrue(assertMessage, processResult.isSuccess());
    }

    public static void assertCount(int expectedCount, ProcessResult processResult) {
        assertCount("ProcessResult message count", expectedCount, processResult);
    }

    public static void assertCount(String assertMessage, int expectedCount, ProcessResult processResult) {
        assertCount(assertMessage, expectedCount, processResult.getMessages());
    }

    public static void assertCount(int expectedCount, MessageList messageList) {
        assertCount("MessageList message count", expectedCount, messageList);
    }

    public static void assertCount(String assertMessage, int expectedCount, MessageList messageList) {
        if (expectedCount != messageList.size()) {
            for (Message message : messageList) {
                System.out.println(message.getMessageCode() + ":" + message.getMessage() + " at " + message.getInterestingStackElement());
            }
        }
        assertEquals(assertMessage, expectedCount, messageList.size());
    }

    public static void assertContains(int messageCode, MessageInfo.MessageLevel messageLevel, ProcessResult processResult) {
        assertContains(null, messageCode, messageLevel, processResult);
    }

    public static void assertContains(String assertMessage, int messageCode, MessageInfo.MessageLevel messageLevel, ProcessResult processResult) {
        assertContains(assertMessage, messageCode, messageLevel, processResult.getMessages());
    }

    public static void assertContains(int messageCode, MessageInfo.MessageLevel messageLevel, MessageList messageList) {
        assertContains(null, messageCode, messageLevel, messageList);
    }

    public static void assertContains(String assertMessage, int messageCode, MessageInfo.MessageLevel messageLevel, MessageList messageList) {
        boolean found = false;

        for (Message message : messageList) {
            found =  (message.getLevel() == messageLevel && Integer.parseInt(message.getMessageCode()) == messageCode);
            if (found) break;
        }

        if (!found) {
            for (Message message : messageList) {
                System.out.println(message);
            }

            String msg = "expected to find processing message - code: " + messageCode + "   level:" + messageLevel.name();
            if (assertMessage != null) {
                msg = assertMessage + " - " + msg;
            }
            fail(msg);
        }
    }


    // non ProcessResult specific asserts
    public static void assertCollectionContains(Collection collection, Object expected) {
        assertCollectionContains("", collection, expected);
    }

    public static void assertCollectionContains(String assertMessage, Collection collection, Object expected) {
        for (Object o : collection) {
            if (o.equals(expected))
                return;
        }

        String valueMsg = expected != null ? expected.toString() : "null";

        for (Object o : collection) {
            System.err.println(o);
        }        
        fail(assertMessage + " - expected value " + valueMsg + " not found in collection");

    }

    public static void assertCollectionContains(String assertMessage, Collection collection, String propertyName, Object expected) {
        Method method = null;
        for (Object element : collection) {
            Object value = null;
            try {
                if (method == null) {
                    method = element.getClass().getMethod("get" + propertyName.substring(0,1).toUpperCase() + propertyName.substring(1));
                }
                value = method.invoke(element, null);
            } catch (Exception e) {
                fail("reflection failed trying to access property: " + propertyName + " on class " + element.getClass().getSimpleName());
            }

            if (value.equals(expected))
                return;

            if (value instanceof Enum && expected instanceof String) {
                Object expectedEnumValue = Enum.valueOf((Class<Enum>)value.getClass(), (String)expected);
                if (value.equals(expectedEnumValue))
                    return;
            }
        }

        String valueMsg = expected != null ? expected.toString() : "null";

        for (Object o : collection) {
            try { System.err.println(method.invoke(o, null)); }
            catch (Exception e) {}
        }
        fail(assertMessage + " - expected value " + valueMsg + " not found in collection");

    }
}
