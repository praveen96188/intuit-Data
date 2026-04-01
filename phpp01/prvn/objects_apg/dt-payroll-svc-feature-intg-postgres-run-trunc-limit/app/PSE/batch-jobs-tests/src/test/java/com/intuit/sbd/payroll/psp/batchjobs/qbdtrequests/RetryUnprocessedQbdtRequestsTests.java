package com.intuit.sbd.payroll.psp.batchjobs.qbdtrequests;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
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
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: allenc289
 * Date: Oct 27, 2010
 * Time: 10:18:17 AM
 */
public class RetryUnprocessedQbdtRequestsTests {
    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011,4,1,12,0,0,0));
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

    @Test
    public void DontReprocessIfStillInErrorState() throws Exception {
        // Reusing test in QBDTAdapterTests
        // The end result is that we will have two unprocessed requests in the database - one queued and one in error (for the same company)
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008,1,1));

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

        // manually set the status to error
        PayrollServices.beginUnitOfWork();
        QbdtUnprocessedRequest qbdtUnprocessedRequest = assertOne(QbdtUnprocessedRequest.findUnprocessedRequests(company, false, QbdtRequestStatus.Queued));
        qbdtUnprocessedRequest.setStatus(QbdtRequestStatus.Error);
        Application.save(qbdtUnprocessedRequest);
        PayrollServices.commitUnitOfWork();

        // make sure company cannot submit while they have an error request
        QBDTTestHelper.submitQBDTRequest(ofx, false);

        // Running retry ofx should not do anything
        Application.beginUnitOfWork();
        RetryUnprocessedQbdtRequests retryUnprocessedQbdtRequests = new RetryUnprocessedQbdtRequests();
        retryUnprocessedQbdtRequests.retryUnprocessedRequests();

        Expression<QbdtUnprocessedRequest> unprocessedRequestsQuery =
                new Query<QbdtUnprocessedRequest>()
                        .OrderBy(QbdtUnprocessedRequest.Company(), QbdtUnprocessedRequest.CreatedDate());

        DomainEntitySet<QbdtUnprocessedRequest> unprocessedRequests = Application.find(QbdtUnprocessedRequest.class, unprocessedRequestsQuery);
        assertEquals(unprocessedRequests.size(), 1);
        assertEquals(unprocessedRequests.get(0).getStatus(), QbdtRequestStatus.Error);        
        Application.commitUnitOfWork();
    }

}
