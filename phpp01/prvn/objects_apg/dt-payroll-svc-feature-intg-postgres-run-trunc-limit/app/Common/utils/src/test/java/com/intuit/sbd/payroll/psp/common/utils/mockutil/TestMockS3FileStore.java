package com.intuit.sbd.payroll.psp.common.utils.mockutil;

import com.intuit.sbg.shared.filestore.S3FileStore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestMockS3FileStore {

    public static final Logger logger = Logger.getLogger(TestMockS3FileStore.class.getName());

    @Test
    public void testMethodCountInMockS3FileStoreClass() {
        Method[] s3FileStoreMethods = S3FileStore.class.getDeclaredMethods();
        Method[] mockS3FileStoreMethods = MockS3FileStore.class.getDeclaredMethods();

        assertEquals(s3FileStoreMethods.length, mockS3FileStoreMethods.length);
        assertEquals(17, s3FileStoreMethods.length);
    }

    @Test
    public void testAllMethodsArePresentInMockS3FileStoreClass() throws NoSuchMethodException {
        Method[] s3FileStoreMethods = S3FileStore.class.getDeclaredMethods();

        for (Method s3FileStoreMethod : s3FileStoreMethods) {
            String s3FileStoreMethodName = s3FileStoreMethod.getName();
            Class<?>[] s3FileStoreMethodParameterTypes = s3FileStoreMethod.getParameterTypes();

            Method mockS3FileStoreMethod = MockS3FileStore.class.getDeclaredMethod(s3FileStoreMethodName, s3FileStoreMethodParameterTypes);
            assertNotNull(mockS3FileStoreMethod);

            assertEquals(s3FileStoreMethodName, mockS3FileStoreMethod.getName());
            assertEquals(s3FileStoreMethodParameterTypes.length, mockS3FileStoreMethod.getParameterTypes().length);
        }
    }

    @Test(expected = NoSuchMethodException.class)
    public void testDummyMethodPresentInMockS3FileStore() throws NoSuchMethodException {
        MockS3FileStore.class.getDeclaredMethod("Dummy", String.class);
    }
}