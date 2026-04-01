package com.intuit.sbd.payroll.psp.gateways.iop;

import com.intuit.sbd.payroll.psp.gateways.efe.EfeGateway;
import com.intuit.sbd.payroll.psp.junit.LenientRunner;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: rnorian
 * Date: 11/23/11
 * Time: 1:12 PM
 */
@RunWith(LenientRunner.class)
public class EfeGatewayTests {

    // liberated from Application.java
    public static String findFileOnClassPath(String fileName) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        ResourceLoader resourceLoader = resolver.getResourceLoader();

        Resource resource = resourceLoader.getResource("classpath:/" + fileName);
        String path = null;
        if (resource.exists()) {
            try {
                path = resource.getFile().getAbsolutePath();
            }
            catch (IOException ex) {
                throw new RuntimeException("File not found: " + fileName);
            }
        } else {
            throw new RuntimeException("File not found: " + fileName);
        }
        return path;
    }

    private static List<File> rafFiles = new ArrayList<File>();
    private static File rafAdd = null;
    private static File rafDelete = null;
    private static String defaultSender = "OSP-FILE";
    private static String defaultAuthCode = "PSP-UNIT-TEST";
    private static String defaultEmail = null;

    @BeforeClass
    public static void beforeClass() {
        rafAdd = new File(findFileOnClassPath("efe/RAFA20110721083814.csv"));
        rafFiles.add(rafAdd);
        rafDelete = new File(findFileOnClassPath("efe/RAFD20111019153100.csv"));
        rafFiles.add(rafDelete);

        for (File rafFile : rafFiles) {
            if (!rafFile.canRead()) {
                throw new RuntimeException("can't read file: " + rafFile.getName());
            }
        }
    }

    //Ignoring the following 3 tests until we figure out why it won't timeout

    @Ignore
    @Test(timeout = 60000)
    public void testEnrollmentSend_HappyPathAdd() {
        EfeGateway.getInstance().sendRAFEnrollmentFile(rafAdd, defaultSender, defaultAuthCode, defaultEmail);
    }

    @Ignore
    @Test(timeout = 60000)
    public void testEnrollmentSend_HappyPathDelete() {
        EfeGateway.getInstance().sendRAFEnrollmentFile(rafDelete, defaultSender, defaultAuthCode, defaultEmail);
    }

    @Ignore
    @Test(timeout = 60000)
    public void testEnrollmentSend_HappyPathAddDelete() {
        EfeGateway.getInstance().sendRAFEnrollmentFile(rafAdd, defaultSender, defaultAuthCode, defaultEmail);
        EfeGateway.getInstance().sendRAFEnrollmentFile(rafDelete, defaultSender, defaultAuthCode, defaultEmail);
    }

    @Ignore
    @Test
    public void testEnrollmentSend_TransmissionFailure() {
        // record expected
        EfeGateway efeMock = EasyMock.createMock(EfeGateway.class);
        efeMock.sendRAFEnrollmentFile(rafAdd, defaultSender, defaultAuthCode, defaultEmail);
        EasyMock.expectLastCall().andThrow(new RuntimeException("Test"));

        // ready for testing
        EasyMock.replay(efeMock);

        EasyMock.verify(efeMock);
    }

    @Ignore
    @Test
    public void testEnrollmentSend_InvalidFilePaths() {
        try {
            EfeGateway.getInstance().sendRAFEnrollmentFile(new File("non-existent file"), defaultSender, defaultAuthCode, defaultEmail);
            Assert.fail("exception should have been thrown for failed file existence");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void testEnrollmentSend_InvalidArgsToEfe() {
        try {
            EfeGateway.getInstance().sendRAFEnrollmentFile(rafAdd, "", defaultAuthCode, defaultEmail);
            Assert.fail("exception should have been thrown for failed field validation");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
