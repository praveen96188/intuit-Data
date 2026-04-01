package com.intuit.sbd.payroll.psp.api.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.fraudpayrolls.ProcessFraudulentPayrolls;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.FraudEventCategory;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.LoadFraudEvents;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Jun 9, 2008
 * Time: 11:09:12 AM
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class CompanyEventFinderTests {
    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        LoadFraudEvents.before();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testFindSourceSystemEvents() {

        // Load Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();

        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        // Create some company events
        Company company = result.getResult();
        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());

        CompanyEvent.createCompanyEvent(company, EventTypeCode.EINChanged);
        CompanyEvent.createCompanyEvent(company, EventTypeCode.CompanyBankAccountChange);
        CompanyEvent.createCompanyEvent(company, EventTypeCode.ServiceStatusChange);
        CompanyEvent.createCompanyEvent(company, EventTypeCode.PINCreated);
        PayrollServices.commitUnitOfWork();

        // Now find Events that should sync back to CRIS
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(SourceSystemCode.CRIS, SpcfCalendar.createInstance(2007, 8, 29), null);
        assertEquals("Number of events:",1, events.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFindCompanyEventsByFraudEventCategoryEin() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071104000000");
        PayrollServices.commitUnitOfWork();

        // run fraudulent payroll batch job
        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company2 = Company.findCompany("2222222", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);

        company1 = Application.findById(Company.class, company1.getId());

        // find with fraud event category and ein code values as null
        DomainEntitySet<CompanyEvent> retList =
                CompanyEvent.findActiveCompanyFraudEvents(null, null, null, null, null, null);


        Assert.assertEquals("Number of event details found", 10, retList.size());

        // find with SignUp
        retList = CompanyEvent.findActiveCompanyFraudEvents(null, FraudEventCategory.SignUp, null, null, null, null);


        Assert.assertEquals("Number of event details found", 0, retList.size());

        // find with fraud event category and ein value
        retList = CompanyEvent.findActiveCompanyFraudEvents("1234567", FraudEventCategory.Payroll, null, null, null, null);

        Assert.assertEquals("Number of event details found", 7, retList.size());

        for (CompanyEvent event:retList) {
            Assert.assertEquals("Event Detail Value", "Payroll",
                event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
            Assert.assertEquals("Company", company1.getId(), event.getCompany().getId());
        }

        // find with fraud event category and ein value
        retList = CompanyEvent.findActiveCompanyFraudEvents("2222222", FraudEventCategory.Payroll, null, null, null, null);

        Assert.assertEquals("Number of event details found", 2, retList.size());

        for (CompanyEvent event:retList) {
            Assert.assertEquals("Event Detail Value", "Payroll",
                event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
            Assert.assertEquals("Company", company2.getId(), event.getCompany().getId());
        }

        // find with fraud event category and ein value
        retList = CompanyEvent.findActiveCompanyFraudEvents("8574536", FraudEventCategory.Payroll, null, null, null, null);

        Assert.assertEquals("Number of event details found",1, retList.size());

        for (CompanyEvent event:retList) {
            Assert.assertEquals("Event Detail Value", "Payroll",
                event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
            Assert.assertEquals("Company", company3.getId(), event.getCompany().getId());
        }

        PayrollServices.commitUnitOfWork();
    }

    private void loadMultipleCompaniesAndPayrolls() {
        Company1Dataloader company1DataLoader = new Company1Dataloader();
        Company company1 = company1DataLoader.persistCompany1();
        Collection<PayrollRunDTO> payrollDTOs = new ArrayList<PayrollRunDTO>();
        payrollDTOs.add(company1DataLoader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        payrollDTOs.add(company1DataLoader.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-10-09")));
        submitPayroll(company1, payrollDTOs);
        payrollDTOs.clear();
        PSPDate.addDaysToPSPTime(-9);

        Company2Dataloader company2DataLoader = new Company2Dataloader();
        Company company2 = company2DataLoader.persistCompany2();
        payrollDTOs.add(company2DataLoader.getCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        payrollDTOs.add(company2DataLoader.getCompany2PR2_DoesNotExceedLimits(new DateDTO("2007-10-09")));
        submitPayroll(company2, payrollDTOs);
        payrollDTOs.clear();
        PSPDate.addDaysToPSPTime(-8);

        Company3Dataloader company3DataLoader = new Company3Dataloader();
        Company company3 = company3DataLoader.persistCompany3();
        payrollDTOs.add(company3DataLoader.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        payrollDTOs.add(company3DataLoader.getCompany3PR2_DoesNotExceedLimits(new DateDTO("2007-10-09")));
        submitPayroll(company3,payrollDTOs);
        payrollDTOs.clear();
    }

   private void submitPayroll(Company pCompany, Collection<PayrollRunDTO> payrollRunDTOs) {
        for (PayrollRunDTO payrollRunDTO: payrollRunDTOs) {
            ProcessResult<PayrollRun> result = PayrollServices.payrollManager.submitPayroll(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(result);
        }
    }

    @Test
    public void testFindCompanyEvents() {
        // Load Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();

        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        // Create some company events
        Company company = result.getResult();
        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());

        CompanyEvent.createCompanyEvent(company, EventTypeCode.EINChanged);
        CompanyEvent.createCompanyEvent(company, EventTypeCode.CompanyBankAccountChange);
        CompanyEvent.createCompanyEvent(company, EventTypeCode.ServiceStatusChange);
        CompanyEvent.createCompanyEvent(company, EventTypeCode.PINCreated);
        PayrollServices.commitUnitOfWork();

        // Now find Events that should sync back to CRIS
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEventsByTypes(company,
                new EventTypeCode[] {EventTypeCode.EINChanged, EventTypeCode.CompanyBankAccountChange,
                        EventTypeCode.ServiceStatusChange, EventTypeCode.PINCreated},
                null, SpcfCalendar.createInstance(2007, 8, 29), null, 501);
        assertEquals("Number of events:",4, events.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        events = CompanyEvent.findCompanyEventsByTypes(company,
                new EventTypeCode[] {EventTypeCode.EINChanged, EventTypeCode.CompanyBankAccountChange,
                        EventTypeCode.ServiceStatusChange, EventTypeCode.PINCreated},
                null, null, null, 501);
        assertEquals("Number of events:",4, events.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        events = CompanyEvent.findCompanyEventsByTypes(company, null, null, null, null, 501);
        assertEquals("Number of events:",5, events.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        events = CompanyEvent.findCompanyEventsByTypes(company,
                new EventTypeCode[] {EventTypeCode.EINChanged, EventTypeCode.CompanyBankAccountChange,
                        EventTypeCode.ServiceStatusChange, EventTypeCode.PINCreated},
                null, SpcfCalendar.createInstance(2007, 8, 29), SpcfCalendar.createInstance(2007, 8, 30), 501);
        assertEquals("Number of events:",0, events.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        events = CompanyEvent.findCompanyEventsByTypes(company, null, "UnitTest", null, null, 501);
        assertEquals("Number of events:",5, events.size());
        PayrollServices.commitUnitOfWork();
    }
}
