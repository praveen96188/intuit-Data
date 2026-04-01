package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import junit.framework.Assert;

/**
 *
 * User: rkrishna
 * Date: Dec 21, 2007
 * Time: 11:38:46 AM

 */
public class CancelStrikeDDProcessTests {
    private DataLoader dataLoader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * ************************************Null tests/incoming data verification***********************************
     */

    /**
     * Test message 137 - Source System Code not specified
     */
    @Test
    public void testNullSourceSystemId() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.cancelStrikeEvent(null, "123272727", null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 138 - Source CompanyId not specified
     */
    @Test
    public void testNullCompany() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.cancelStrikeEvent(SourceSystemCode.QBOE, null, null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 169 - Company Does Not Exist
     */
    @Test
    public void testInvalidCompany() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.companyManager.cancelStrikeEvent(SourceSystemCode.QBOE, "1232727", null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:1232727 does not exist.",
                errorMessage.getMessage());
    }

    /**
     * Test message 257 - Illegal Event Cancel
     */
    @Test
    public void cancelStrikeFailure() {
        Application.beginUnitOfWork();
        Company company1 = dataLoader.persistTestActiveCompany();
        AddCompanyDataLoader.dataloader.persistTestCompanyService(company1);
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();

        ProcessResult processResult;
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 12, 22, SpcfTimeZone.getLocalTimeZone()));

        processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE, "123272727", "Strike Reason",
                SpcfCalendar.createInstance(2007, 12, 18, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        assertSuccess("addStrikeEvent", processResult);

        Application.beginUnitOfWork();
        Company foundCompany = Company.findCompany("123272727", company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                foundCompany,
                EventTypeCode.CustomerSignedUp, CompanyEventStatus.Active, null, null);
        Application.commitUnitOfWork();

        //Assertion for CustomerSignedUp Event
        Assert.assertEquals("Company Events", 1, companyEventsList.size());

        Application.beginUnitOfWork();

        processResult = PayrollServices.companyManager.cancelStrikeEvent(SourceSystemCode.QBOE, "123272727", companyEventsList.get(0).getId());

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "257", errorMessage.getMessageCode());
        assertEquals("Error message", "Only strike event can be cancelled.",
                errorMessage.getMessage());
    }

    /**
     * Test message - Cancel Strike Success
     */
    @Test
    public void cancelStrikeSuccess() {
        Application.beginUnitOfWork();
        dataLoader.persistTestActiveCompany();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult processResult;
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 12, 22, SpcfTimeZone.getLocalTimeZone()));

        processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE, "123272727", "Strike Reason",
                SpcfCalendar.createInstance(2007, 12, 18, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        CompanyEvent strikeEvent = (CompanyEvent) processResult.getResult();
        assertSuccess("addStrikeEvent", processResult);

        Application.beginUnitOfWork();
        processResult = PayrollServices.companyManager.cancelStrikeEvent(SourceSystemCode.QBOE, "123272727", strikeEvent.getId());

        CompanyEvent companyEvent = (CompanyEvent) processResult.getResult();

        Application.commitUnitOfWork();

        assertTrue(processResult.isSuccess());
        assertEquals("Company Id ", "123272727", companyEvent.getCompany().getSourceCompanyId());
        assertEquals("Company Event Id ", strikeEvent.getId(), companyEvent.getId());
        assertEquals("Status Cd ", CompanyEventStatus.Inactive, companyEvent.getStatusCd());
    }
}
