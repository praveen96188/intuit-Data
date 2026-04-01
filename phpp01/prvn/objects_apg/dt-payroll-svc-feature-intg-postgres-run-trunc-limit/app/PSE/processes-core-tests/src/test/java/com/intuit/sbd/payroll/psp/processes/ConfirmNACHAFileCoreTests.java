package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import static junit.framework.Assert.assertEquals;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 11, 2008
 * Time: 8:55:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfirmNACHAFileCoreTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testNullNACHAFile() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.payrollManager.confirmNACHAFile(null, "ConfirmCode");

        Application.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "11", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid argument: NACHA File Id",
                errorMessage.getMessage());
    }

    @Test
    public void testNullConfirmationCode() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.payrollManager.confirmNACHAFile("123456", null);

        Application.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "11", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid argument: Confirmation Code",
                errorMessage.getMessage());
    }

    @Test
    public void testEmptyConfirmationCode() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.payrollManager.confirmNACHAFile("123456", "");

        Application.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "11", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid argument: Confirmation Code",
                errorMessage.getMessage());
    }

    @Test
    public void testNACHAFileDNE() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.payrollManager.confirmNACHAFile("41ecdc5a-14c4-4ef2-992f-d7e6d5e1564c", "ABC123");

        Application.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "1303", errorMessage.getMessageCode());
        assertEquals("Error message", "NACHAFile with Id 41ecdc5a-14c4-4ef2-992f-d7e6d5e1564c does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void testHappyPath() {
        SpcfUniqueId batchId = ACHReturnsDataLoader.loadQBDTPayrollOffloaded();

        Application.beginUnitOfWork();
        OffloadBatch offloadBatch = PayrollServices.entityFinder.findById(OffloadBatch.class, batchId);
        DomainEntitySet<NACHAFile> files = offloadBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);

        NACHAFile file1 = files.get(0);
        NACHAFile file2 = files.get(1);

        SpcfUniqueId file1Id = file1.getId();
        SpcfUniqueId file2Id = file2.getId();

        PSPDate.setPSPTime("20071031000000");
        ProcessResult processResult1 = PayrollServices.payrollManager.confirmNACHAFile(file1Id.toString(), "ABC123");
        PSPDate.setPSPTime("20071103000000");
        ProcessResult processResult2 = PayrollServices.payrollManager.confirmNACHAFile(file2Id.toString(), "DEFXYZdjfjfj18375");

        Application.commitUnitOfWork();

        assertSuccess(processResult1);
        assertSuccess(processResult2);

        Application.beginUnitOfWork();
        //requery for files
        file1 = PayrollServices.entityFinder.findById(NACHAFile.class, file1.getId());
        file2 = PayrollServices.entityFinder.findById(NACHAFile.class, file2.getId());

        String actualFile1Code = file1.getConfirmationCode();
        String actualFile2Code = file2.getConfirmationCode();

        SpcfCalendar actualFile1Date = file1.getConfirmationDate().toLocal();
        SpcfCalendar actualFile2Date = file2.getConfirmationDate().toLocal();

        Application.commitUnitOfWork();

        assertEquals("File 1 confirmation code", "ABC123", actualFile1Code);
        assertEquals("File 2 confirmation code", "DEFXYZdjfjfj18375", actualFile2Code);

        assertYearMonthDayEquals(SpcfCalendar.createInstance(2007, 10, 31, SpcfTimeZone.getLocalTimeZone()), actualFile1Date);
        assertYearMonthDayEquals(SpcfCalendar.createInstance(2007, 11, 3, SpcfTimeZone.getLocalTimeZone()), actualFile2Date);
    }

    private void assertYearMonthDayEquals(SpcfCalendar pTimeToCompare, SpcfCalendar pPSPTime) {
        Assert.assertEquals(pTimeToCompare.getDay(), pPSPTime.getDay());
        Assert.assertEquals(pTimeToCompare.getMonth(), pPSPTime.getMonth());
        Assert.assertEquals(pTimeToCompare.getYear(), pPSPTime.getYear());
    }
}