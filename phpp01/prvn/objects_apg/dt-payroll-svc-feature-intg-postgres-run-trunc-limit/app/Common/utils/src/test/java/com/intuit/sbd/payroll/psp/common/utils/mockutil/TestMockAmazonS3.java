package com.intuit.sbd.payroll.psp.common.utils.mockutil;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestMockAmazonS3 {

    public static final Logger logger = Logger.getLogger(TestMockAmazonS3.class.getName());

    @Ignore
    @Test
    public void testMethodCountInMockAmazonS3Class() {
        Method[] amazonS3Methods = AmazonS3.class.getDeclaredMethods();
        Method[] mockAmazonS3Methods = MockAmazonS3.class.getDeclaredMethods();
        assertEquals(amazonS3Methods.length, mockAmazonS3Methods.length);
        assertEquals(167, mockAmazonS3Methods.length);
    }

    @Test
    public void testAllMethodsArePresentInMockAmazonS3Class() throws NoSuchMethodException {
        Method[] amazonS3Methods = AmazonS3.class.getDeclaredMethods();

        for (Method amazonS3Method : amazonS3Methods) {
            String amazonS3MethodName = amazonS3Method.getName();
            Class<?>[] amazonS3MethodParameterTypes = amazonS3Method.getParameterTypes();

            Method mockAmazonS3Method = MockAmazonS3.class.getDeclaredMethod(amazonS3MethodName, amazonS3MethodParameterTypes);
            assertNotNull(mockAmazonS3Method);

            assertEquals(amazonS3MethodName, mockAmazonS3Method.getName());
            assertEquals(amazonS3MethodParameterTypes.length, mockAmazonS3Method.getParameterTypes().length);
        }
    }

    @Test(expected = NoSuchMethodException.class)
    public void testDummyMethodPresentInMockAmazonS3Class() throws NoSuchMethodException {
        MockAmazonS3.class.getDeclaredMethod("Dummy", String.class);
    }
}