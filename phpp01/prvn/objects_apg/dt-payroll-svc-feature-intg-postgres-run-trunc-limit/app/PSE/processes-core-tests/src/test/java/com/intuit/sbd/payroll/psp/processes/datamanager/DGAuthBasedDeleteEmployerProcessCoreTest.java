package com.intuit.sbd.payroll.psp.processes.datamanager;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DGAuthBasedDeleteEmployerProcessCoreTest {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testValidation() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<String> result = PayrollServices.companyManager.disassociateCompanyByPSID(null, null, null, null);
        assertOne(result.getMessages());
        assertEquals("Error Code", "5002", result.getErrorMessages().get(0).getMessageCode());

        result = PayrollServices.companyManager.disassociateCompanyByPSID(SpcfUniqueId.generateRandomUniqueIdString(), null, null, null);
        assertEquals("Count of validation messages does not match", 1, result.getMessages().size());
        assertEquals("Error Code", "169", result.getErrorMessages().get(0).getMessageCode());
        PayrollServices.rollbackUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "12345");
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.disassociateCompanyByPSID(company.getSourceCompanyId(), SourceSystemCode.QBDT, SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        assertSuccess(result);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHappyPathForCompany() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "12345");

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("12345", SourceSystemCode.QBDT);

        ProcessResult<String> result = PayrollServices.companyManager.disassociateCompanyByPSID("12345", SourceSystemCode.QBDT, SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        assertNotNull(result);
        assertEquals("Company disassociation failed", "12345", result.getResult());

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.DGDeleteRequest);
        assertNotNull(companyEvents);
        assertOne(companyEvents);

        PayrollServices.commitUnitOfWork();
    }


}
