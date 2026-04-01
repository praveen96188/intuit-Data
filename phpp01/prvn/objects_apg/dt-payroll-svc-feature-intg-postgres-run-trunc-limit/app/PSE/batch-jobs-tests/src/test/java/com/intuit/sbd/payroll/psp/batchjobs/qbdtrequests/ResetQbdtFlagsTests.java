package com.intuit.sbd.payroll.psp.batchjobs.qbdtrequests;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedUnprocessedRequestTests;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXRequestGenerator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.MockSocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.SocketManagerFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: nloharuka
 * Date: Oct 25, 2017
 * Time: 10:18:17 AM
 */
public class ResetQbdtFlagsTests {
    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2017, 10, 24, 9, 0, 0, 0));
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @BeforeClass
    public static void beforeClass() {
        SocketManagerFactory.setInstanceClass(MockSocketManager.class);
    }

    @AfterClass
    public static void afterClass() {
        SocketManagerFactory.setInstanceClass(null);
    }

    /**
     * PSP-13615
     * Reset Process Transmission Flag
     *
     * @throws Exception
     */
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void ResetProcessTransmissionFlagTest() throws Exception {

        String psid = "100093352";

        //Create Company
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_19.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        company1.getCompanyService(ServiceCode.Cloud).setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        PayrollServices.commitUnitOfWork();

        updateQBInfoFlag(company, false);
        validateQbInfoRecordValues(1, false);

        //Submit another OFX so that it is not processed
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_20.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        //Add 1 day to current date time so that 24 hrs cross
        setDate(2017, 10, 24, 11, 20, 0, 0, 1);

        //This call should throw error message as 24 hrs fall after 1 hr of offload time
        ResetQbdtFlags resetQbdtFlags = new ResetQbdtFlags();
        resetQbdtFlags.resetFlags();

        validateQbInfoRecordValues(1, false);

        //Set date time after 24 hrs and 1 hr before offload Cut off time
        setDate(2017, 10, 24, 19, 20, 0, 0, 1);

        //This call should reset the flag successfully
        resetQbdtFlags = new ResetQbdtFlags();
        resetQbdtFlags.resetFlags();

        validateQbInfoRecordValues(1, true);

    }

    /**
     * PSP-13615
     * Method to validate QbInfo record Count
     * and ProcessingFlag
     */
    private void validateQbInfoRecordValues(int rCount, boolean flagVal) {
        Expression<QuickbooksInfo> qbInfo =
                new Query<QuickbooksInfo>()
                        .OrderBy(QuickbooksInfo.Company(), QuickbooksInfo.ModifiedDate());
        DomainEntitySet<QuickbooksInfo> qbInfoRecords = Application.find(QuickbooksInfo.class, qbInfo);
        assertEquals(qbInfoRecords.size(), rCount);
        assertEquals(qbInfoRecords.get(0).getProcessTransmissions(), flagVal);
    }

    /**
     * PSP-13615
     * Method to update QbInfo Processing Flag status
     */
    private void updateQBInfoFlag(Company company, boolean flagVal) {
        //Set QuickbooksInfo Process Transmissions flag to false
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.getQuickBooksInfo().setProcessTransmissions(flagVal);
        assertSuccess(PayrollServices.companyManager.updateQBCompanyInfo(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
        PayrollServices.commitUnitOfWork();
    }

    /**
     * PSP-13615
     * Method to set Date and add num of days
     */
    private void setDate(int yr, int mth, int dy, int hr, int min, int sec, int ms, int numDays) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(yr, mth, dy, hr, min, sec, ms));
        if (numDays != 0) {
            PSPDate.addDaysToPSPTime(1);
        }
        PayrollServices.commitUnitOfWork();
    }

}
