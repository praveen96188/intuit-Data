package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.paycycle.ops.eftpsBp.EdiEftpsFileValidator;
import com.paycycle.ops.eftpsBp.EdiEftpsRecordList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Feb 4, 2011
 * Time: 4:27:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsGeneralTests {
    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        EftpsDataLoader.deleteAllTestDirFiles();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005,1,1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005,1,1));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    private File[] getStaticFileList() {
        File dir = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles");

        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return !file.isDirectory() && file.getName().toLowerCase().startsWith("eftps");
            }
        };

        return dir.listFiles(filter);
    }

    @Test
    public void testEdiFileUtilities() {
        File[] fileList = getStaticFileList();

        if (fileList.length > 0) {
            //
            // Test whether EdiEftpsFileValidator can validate files
            //
            assertTrue("File is invalid", EdiEftpsFileValidator.isValid(fileList[0]));

            //
            // Test whether EdiEftpsRecordList can match files
            //
            EdiEftpsRecordList file1 = new EdiEftpsRecordList(fileList[0]);
            EdiEftpsRecordList file2 = new EdiEftpsRecordList(fileList[0]);
            assertTrue("Files do not match when they should", file1.equals(file2));

            if (fileList.length > 1) {
                //
                // Test whether EdiEftpsRecordList can detect a mismatch
                //
                file2 = new EdiEftpsRecordList(fileList[1]);
                assertFalse("Files match when they should not", file1.equals(file2));
            }
        }
    }
    @Test
    public void testEdi_valid() 
    {
        File file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment110221190.813");
        EdiEftpsFileValidator validator = new EdiEftpsFileValidator(file);
        assertTrue("813 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsEnrollmentResponse.824");
        validator = new EdiEftpsFileValidator(file);
        assertTrue("824 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsEnrollment_838.838");
        validator = new EdiEftpsFileValidator(file);
        assertTrue("838 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment_SameDay.813");
        validator = new EdiEftpsFileValidator(file);
        assertTrue("Same day 813 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPaymentAck.997");
        validator = new EdiEftpsFileValidator(file);
        assertTrue("997 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPaymentResponse.151");
        validator = new EdiEftpsFileValidator(file);
        assertTrue("151 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPaymentResponse_SameDay.151");
        validator = new EdiEftpsFileValidator(file);
        assertTrue("Same day 151 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPaymentFileReturn.827");
        validator = new EdiEftpsFileValidator(file);
        assertTrue("827 file validation failed.",validator.isValid());

    }

    @Test
    public void testEdi_InValid() 
    {
        File file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/invalid/EftpsPayment110221190.813");
        EdiEftpsFileValidator validator = new EdiEftpsFileValidator(file);
        assertFalse("813 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/invalid/EftpsEnrollmentResponse.824");
        validator = new EdiEftpsFileValidator(file);
        assertFalse("824 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/invalid/EftpsEnrollment_838.838");
        validator = new EdiEftpsFileValidator(file);
        assertFalse("838 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/invalid/EftpsPaymentAck.997");
        validator = new EdiEftpsFileValidator(file);
        assertFalse("997 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/invalid/EftpsPaymentResponse.151");
        validator = new EdiEftpsFileValidator(file);
        assertFalse("151 file validation failed.",validator.isValid());

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/invalid/EftpsPaymentFileReturn.827");
        validator = new EdiEftpsFileValidator(file);
        assertFalse("827 file validation failed.",validator.isValid());
    }

    @Test
    public void testEdiEftpsRecordList_HappyPath()
    {
        File file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment110221190.813");
        EdiEftpsRecordList file1 = new EdiEftpsRecordList(file);
        EdiEftpsRecordList file2 = new EdiEftpsRecordList(file);
        assertTrue("Files do not match when they should", file1.equals(file2));

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsEnrollmentResponse.824");
        file1 = new EdiEftpsRecordList(file);
        file2 = new EdiEftpsRecordList(file);
        assertTrue("Files do not match when they should", file1.equals(file2));

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsEnrollment_838.838");
        file1 = new EdiEftpsRecordList(file);
        file2 = new EdiEftpsRecordList(file);
        assertTrue("Files do not match when they should", file1.equals(file2));

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment_SameDay.813");
        file1 = new EdiEftpsRecordList(file);
        file2 = new EdiEftpsRecordList(file);
        assertTrue("Files do not match when they should", file1.equals(file2));

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPaymentAck.997");
        file1 = new EdiEftpsRecordList(file);
        file2 = new EdiEftpsRecordList(file);
        assertTrue("Files do not match when they should", file1.equals(file2));

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPaymentResponse.151");
        file1 = new EdiEftpsRecordList(file);
        file2 = new EdiEftpsRecordList(file);
        assertTrue("Files do not match when they should", file1.equals(file2));

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPaymentResponse_SameDay.151");
        file1 = new EdiEftpsRecordList(file);
        file2 = new EdiEftpsRecordList(file);
        assertTrue("Files do not match when they should", file1.equals(file2));

        file = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPaymentFileReturn.827");
        file1 = new EdiEftpsRecordList(file);
        file2 = new EdiEftpsRecordList(file);
        assertTrue("Files do not match when they should", file1.equals(file2));
    }

    @Test
    public void testEdiEftpsRecordList_NullFiles()
    {
        File file1 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment110221190.813");
        File file2 = null;
        EdiEftpsRecordList ediRecordList1 = new EdiEftpsRecordList(file1);
        EdiEftpsRecordList ediRecordList2 = new EdiEftpsRecordList(file2);
        assertFalse("Files match when they should not", ediRecordList1.equals(ediRecordList2));

        file1 = null;
        file2 = null;
        ediRecordList1 = new EdiEftpsRecordList(file1);
        ediRecordList2 = new EdiEftpsRecordList(file2);
        assertFalse("Files match when they should not", ediRecordList1.equals(ediRecordList2));

        file1 = null;
        file2 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment110221190.813");
        ediRecordList1 = new EdiEftpsRecordList(file1);
        ediRecordList2 = new EdiEftpsRecordList(file2);
        assertFalse("Files match when they should not", ediRecordList1.equals(ediRecordList2));
    }


    @Test
    public void testEdiEftpsRecordList_InvalidEdiType() {

        File file1 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/invalid/EftpsPayment110221190.813");
        File file2 = null;
        EdiEftpsRecordList ediRecordList1 = new EdiEftpsRecordList(file1);
        EdiEftpsRecordList ediRecordList2 = new EdiEftpsRecordList(file2);
        assertFalse("Files match when they should not", ediRecordList1.equals(ediRecordList2));


        file1 = null;
        file2 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/invalid/EftpsPayment110221190.813");
        ediRecordList1 = new EdiEftpsRecordList(file1);
        ediRecordList2 = new EdiEftpsRecordList(file2);
        assertFalse("Files match when they should not", ediRecordList1.equals(ediRecordList2));
    }

    @Test
    public void testEdiEftpsRecordList_mismatchRecords() {

        //Compare 824 & 813 WHICH SHOULD NOT MATCH THOUGH THEY ARE VALID FILES.
        File file1 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment110221190.813");
        File file2 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsEnrollmentResponse.824");
        EdiEftpsRecordList ediRecordList1 = new EdiEftpsRecordList(file1);
        EdiEftpsRecordList ediRecordList2 = new EdiEftpsRecordList(file2);
        assertFalse("Files match when they should not", ediRecordList1.equals(ediRecordList2)); // File type mismatch.

        file1 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment110221190.813");
        file2 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/invalid/EftpsPayment110221190.813");
        ediRecordList1 = new EdiEftpsRecordList(file1);
        ediRecordList2 = new EdiEftpsRecordList(file2);
        assertFalse("Files match when they should not", ediRecordList1.equals(ediRecordList2)); // RHS EftpsEdiType is null. as invalid file contains no ST segment.

        file1 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/invalid/EftpsPayment110221190.813"); // no ST SEGMENT.
        file2 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment110221190.813");
        ediRecordList1 = new EdiEftpsRecordList(file1);
        ediRecordList2 = new EdiEftpsRecordList(file2);
        //TODO : UNCOMMENT BELOW ONE ONCE DEFECT IS FIXED.
        //assertFalse("Files match when they should not", ediRecordList1.equals(ediRecordList2)); // LHS EftpsEdiType is null.

        file1 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment110221190.813");
        file2 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment_withTwoSegments.813"); //Valid file with ST segments.
        ediRecordList1 = new EdiEftpsRecordList(file1);
        ediRecordList2 = new EdiEftpsRecordList(file2);
        assertFalse("Files match when they should not", ediRecordList1.equals(ediRecordList2)); // Record count mismatch.

        file1 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/valid/EftpsPayment110221190.813");
        file2 = new File(System.getProperty("user.dir"), "Common/iop-tests/src/test/resources/staticfiles/edifiles/invalid/EftpsPaymen_withInvalidFieldId.813");
        ediRecordList1 = new EdiEftpsRecordList(file1);
        ediRecordList2 = new EdiEftpsRecordList(file2);
        assertFalse("Files match when they should not", ediRecordList1.equals(ediRecordList2)); // Record count mismatch.

    }

}
