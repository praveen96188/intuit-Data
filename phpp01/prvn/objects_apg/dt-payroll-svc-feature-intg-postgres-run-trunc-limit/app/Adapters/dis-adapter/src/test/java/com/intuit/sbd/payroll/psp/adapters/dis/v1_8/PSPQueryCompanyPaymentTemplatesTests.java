package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryCompanyPaymentTemplatesRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryCompanyPaymentTemplatesResponseDISDTO;
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
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPQueryCompanyPaymentTemplatesTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPQueryCompanyPaymentTemplatesTests {
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
            QueryCompanyPaymentTemplatesRequestDISDTO queryCompanyPaymentTemplatesRequestDISDTO = new QueryCompanyPaymentTemplatesRequestDISDTO();
            queryCompanyPaymentTemplatesRequestDISDTO.setSourceCompanyId(psid);
            queryCompanyPaymentTemplatesRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            QueryCompanyPaymentTemplatesResponseDISDTO queryCompanyPaymentTemplatesResponseDISDTO = disAdapter.Query_CompanyPaymentTemplates(queryCompanyPaymentTemplatesRequestDISDTO);
            TestHelper.verifySuccess(queryCompanyPaymentTemplatesResponseDISDTO.getDisResponse());

            TestCase.assertTrue(queryCompanyPaymentTemplatesResponseDISDTO.getCompanyPaymentTemplates().size() > 0);

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
            QueryCompanyPaymentTemplatesRequestDISDTO queryCompanyPaymentTemplatesRequestDISDTO = new QueryCompanyPaymentTemplatesRequestDISDTO();
            queryCompanyPaymentTemplatesRequestDISDTO.setSourceCompanyId(sourceCoIdDNE);
            queryCompanyPaymentTemplatesRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            QueryCompanyPaymentTemplatesResponseDISDTO response = disAdapter.Query_CompanyPaymentTemplates(queryCompanyPaymentTemplatesRequestDISDTO);
            TestHelper.verifyDISResponse(DISMessages.companyDoesNotExist(sourceCoIdDNE), response.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

}
