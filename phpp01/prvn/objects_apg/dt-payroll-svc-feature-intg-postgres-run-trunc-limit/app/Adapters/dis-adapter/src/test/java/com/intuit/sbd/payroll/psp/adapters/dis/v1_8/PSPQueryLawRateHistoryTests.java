package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryLawRateHistoryRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryLawRateHistoryResponseDISDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.PayrollSubmitTaxTests;
import com.intuit.sbd.payroll.psp.query.Query;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPQueryLawRateHistoryTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPQueryLawRateHistoryTests {
    String psid;
    public static PayrollSubmitTaxTests payrollSubmitTaxTests = new PayrollSubmitTaxTests();

    @Before
    public void before() {

        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        payrollSubmitTaxTests.runBeforeEachTest();

        try {
            payrollSubmitTaxTests.testAZStateThreshold941Payments_Over100K_State();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        psid = companies.get(0).getSourceCompanyId();
        PayrollServices.rollbackUnitOfWork();
    }

    @AfterClass
    public static void afterClass() {
        payrollSubmitTaxTests.afterClass();
    }

    @After
    public void runAfterEachTest() {
        payrollSubmitTaxTests.runAfterEachTest();
    }

    @Test
    public void testHappyPathEIN() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryLawRateHistoryRequestDISDTO queryLawRateHistoryRequestDISDTO = new QueryLawRateHistoryRequestDISDTO();
            queryLawRateHistoryRequestDISDTO.setSourceCompanyId(psid);
            queryLawRateHistoryRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));
            queryLawRateHistoryRequestDISDTO.setPaymentTemplateCd("IRS-940-PAYMENT");

            QueryLawRateHistoryResponseDISDTO queryLawRateHistoryResponseDISDTO = disAdapter.Query_LawRateHistory(queryLawRateHistoryRequestDISDTO);
            TestHelper.verifySuccess(queryLawRateHistoryResponseDISDTO.getDisResponse());

            TestCase.assertEquals(0, queryLawRateHistoryResponseDISDTO.getCompanyLawRateHistoryDISDTO().getCompanyLawRateDetails().size());

        } catch (Throwable t) {
            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(t.getMessage());
        }
    }

    @Test
    public void testNoCompanyFound() {
        try {
            String sourceCoIdDNE = "companyDNE";

            DISAdapter disAdapter = new DISAdapter();
            QueryLawRateHistoryRequestDISDTO queryLawRateHistoryRequestDISDTO = new QueryLawRateHistoryRequestDISDTO();
            queryLawRateHistoryRequestDISDTO.setSourceCompanyId(sourceCoIdDNE);
            queryLawRateHistoryRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            QueryLawRateHistoryResponseDISDTO response = disAdapter.Query_LawRateHistory(queryLawRateHistoryRequestDISDTO);
            TestHelper.verifyDISResponse(DISMessages.companyDoesNotExist(sourceCoIdDNE), response.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

}
