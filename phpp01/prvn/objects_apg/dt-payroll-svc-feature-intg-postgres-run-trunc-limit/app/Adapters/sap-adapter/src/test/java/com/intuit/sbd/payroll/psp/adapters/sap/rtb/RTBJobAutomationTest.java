package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXRequestGenerator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation.RTBAutomationAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyUnprocessedRequest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.QbdtRequestStatus;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.junit.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;

import static org.junit.Assert.assertEquals;


/**
 * User: dmehta2
 * Date: 5/17/2023
 * Time: 3:30 PM
 */

public class RTBJobAutomationTest {

    private static final SpcfLogger logger = PayrollServices.getLogger(RTBJobAutomationTest.class);

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 12, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        DataLoadServices.reinitialize();
    }


    @Test
    @Ignore //Ignoring this test case since this tool is temporarily disabled. Remove @Ignore once tool is live
    public void testUpdateDuplicateEmployee() throws Throwable {
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

        SpcfCalendar payCheckDate = SpcfCalendar.createInstance(2016, 5, 28, SpcfTimeZone.getLocalTimeZone());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(payCheckDate);
        PayrollServices.commitUnitOfWork();

        Company company = psdl.createAssistedCompany("123272727");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit(company, payCheckDate);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT,
                "123272727", payrollRunDTO);

        assertTrue(processResult.isSuccess());

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        logger.info("The Payrollrun:" + payrollRuns.size());

        // Add entry to calculate the Quarterly Employee Totals(QET)
        for (PayrollRun payrollRun : payrollRuns) {
            EmpTotalsPayrollRun.insertEmpTotalsPayrollRun(payrollRun);
        }

        for (PayrollRun payrollRun : payrollRuns) {
            EmpTotalsPayrollRun empTotalsPayrollRun = EmpTotalsPayrollRun.findLatestEmpTotalsPayrollRun(company,
                    CalendarUtils.getFirstDayOfQuarter(payrollRun.getPaycheckDate()), EmpTotalsPayrollStatus.Pending);
            empTotalsPayrollRun.updateEmpTotalsPayrollRunStatus(EmpTotalsPayrollStatus.Processed);
        }

        PayrollServices.commitUnitOfWork();


        RTBAutomationAdapter rtb = new RTBAutomationAdapter();
        rtb.duplicateEmployee("1", "3", "123272727");

    }


    @Test
    public void testProcessUnprocessedRequests() throws Throwable {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.getQuickBooksInfo().setProcessTransmissions(false);
        assertSuccess(PayrollServices.companyManager.updateQBCompanyInfo(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
        PayrollServices.commitUnitOfWork();

        OFX ofx = OFXRequestGenerator.generateBalanceFile(company.getSourceCompanyId(), true, false, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.getQuickBooksInfo().setProcessTransmissions(true);
        assertSuccess(PayrollServices.companyManager.updateQBCompanyInfo(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
        PayrollServices.commitUnitOfWork();

        //set status to Processing
        PayrollServices.beginUnitOfWork();
        for (int i = 0; i < 3; i++) {
            QbdtUnprocessedRequest qbdtUnprocessedRequest = new QbdtUnprocessedRequest();
            qbdtUnprocessedRequest.setCompany(company);
            qbdtUnprocessedRequest.setStatus(QbdtRequestStatus.Processing);
            qbdtUnprocessedRequest.setSourceSystemTransmissionId(String.valueOf(SourceSystemTransmission.findLastTransmissionResponseToken(company)));
            Application.save(qbdtUnprocessedRequest);
        }
        PayrollServices.commitUnitOfWork();

        RTBAutomationAdapter rtb = new RTBAutomationAdapter();

        //Count before running the tool
        SAPCompanyUnprocessedRequest companyAndUnprocessedRequestBefore = rtb.findCompanyNameAndUnprocessedRequest(company.getSourceCompanyId());
        assertEquals("No. of unprocessed request", 3, companyAndUnprocessedRequestBefore.getRequestCount());

        rtb.processUnprocessedRequests(company.getSourceCompanyId());

        //Count After running the tool
        SAPCompanyUnprocessedRequest companyAndUnprocessedRequestAfter = rtb.findCompanyNameAndUnprocessedRequest(company.getSourceCompanyId());
        assertEquals("No. of unprocessed request", 0, companyAndUnprocessedRequestAfter.getRequestCount());

    }

    /**
     * Test if company does not Exist
     */
    @Test
    public void testCompanyDoesNotExistsForUnprocessedRequest() throws Throwable {
        String psid = "123456789";

        RTBAutomationAdapter rtb = new RTBAutomationAdapter();
        SAPCompanyUnprocessedRequest companyAndUnprocessedRequest = rtb.findCompanyNameAndUnprocessedRequest(psid);
        assertNull(companyAndUnprocessedRequest);
    }
}