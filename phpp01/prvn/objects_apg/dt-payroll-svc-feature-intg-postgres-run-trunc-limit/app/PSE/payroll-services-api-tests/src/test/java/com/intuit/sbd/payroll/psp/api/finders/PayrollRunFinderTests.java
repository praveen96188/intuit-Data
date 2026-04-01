package com.intuit.sbd.payroll.psp.api.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DDTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.utils.ACHOffloadRunner;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

/**
 * Tests functionality in PayrollRunFinder
 */
public class PayrollRunFinderTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testFindPayrollRuns() {
        Application.beginUnitOfWork();

        // Setup Company
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();

        //set up a company with 12 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        for (int i = 0; i < 12; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(7);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(7);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            int j = 0;
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
        }
        Company testCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        // Test the number of payroll runs returned by the query
        PayrollServices.beginUnitOfWork();
        //Return all 12 by specifying null from and to dates
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(testCompany, null, null);
        Assert.assertEquals("Number of PayrollRuns:", payrollRuns.size(), 12);

        //Return all 12 by specifying a from date and a null to date
        payrollRuns =
                PayrollRun.findPayrollRuns(testCompany, SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone()), null);
        Assert.assertEquals("Number of PayrollRuns:", payrollRuns.size(), 12);

        //Return all 12 by specifying a null from date and a to date
        payrollRuns = PayrollRun.findPayrollRuns(testCompany, null, SpcfCalendar.createInstance(2008, 10, 2, SpcfTimeZone.getLocalTimeZone()));
        Assert.assertEquals("Number of PayrollRuns:", payrollRuns.size(), 12);

        //Return all 12 by specifying a  from date and a to date
        payrollRuns = PayrollRun.findPayrollRuns(testCompany,
                SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2008, 10, 2, SpcfTimeZone.getLocalTimeZone()));
        Assert.assertEquals("Number of PayrollRuns:", payrollRuns.size(), 12);

        //Return a specific number of payrollruns by specifying a from date and a null to date
        payrollRuns =
                PayrollRun.findPayrollRuns(testCompany,
                        SpcfCalendar.createInstance(2008, 1, 2, SpcfTimeZone.getLocalTimeZone()),
                        null);
        Assert.assertEquals("Number of PayrollRuns:", payrollRuns.size(), 6);

        //Return a specific number of payrollruns by specifying a null from date and a to date
        payrollRuns =
                PayrollRun.findPayrollRuns(testCompany,
                        null,
                        SpcfCalendar.createInstance(2008, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        Assert.assertEquals("Number of PayrollRuns:", payrollRuns.size(), 6);

        //Return a specific number of payrollruns by specifying a null from date and a to date
        payrollRuns =
                PayrollRun.findPayrollRuns(testCompany,
                    SpcfCalendar.createInstance(2007, 12, 2, SpcfTimeZone.getLocalTimeZone()),
                    SpcfCalendar.createInstance(2008, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        Assert.assertEquals("Number of PayrollRuns:", payrollRuns.size(), 4);

        Application.commitUnitOfWork();
    }

    @Test
    public void testFindPayrollRunByLimitIncreaseCriteria() {
        PayrollServices.beginUnitOfWork();
        // Setup Company1
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();

        //submit the payroll for company1
        PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1DL.persistPayrollRun(currentPayrollRunDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Setup Company2
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 6, SpcfTimeZone.getLocalTimeZone()));
        Company2Dataloader c2DL = new Company2Dataloader();
        c2DL.persistCompany2();

        //submit the payroll for company2
        currentPayrollRunDTO = c2DL.getCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c2DL.persistPayrollRun(currentPayrollRunDTO);
        PayrollServices.commitUnitOfWork();
        ACHOffloadRunner.runAchOffload("20071002", 5);

        //Make sure returns correct number of payrolls for limit increase
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2008, 1, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);

        String[] paramNames = new String[3];
        Object[] paramValues = new Object[3];
        paramNames[0] = "companyId";
        paramValues[0] = company1.getId().toString();

        paramNames[1] = "eventTypeCd";
        paramValues[1] = EventTypeCode.FirstPayrollReceived;

        SourcePayrollParameter minEarliestPayrollRunDate =
                SourcePayrollParameter.findSourcePayrollParameter(
                        company1.getSourceSystemCd(), SourcePayrollParameterCode.MinimumEarliestPayrollRunDays);
        Integer minDaysInPastForPayRunDate = -1 * Integer.valueOf(minEarliestPayrollRunDate.getParameterValue());

        paramNames[2] = "earliestPayrollRunDateMin";
        SpcfCalendar earliestPayrollRunDateMin = PSPDate.getPSPTime();
        earliestPayrollRunDateMin.addDays(minDaysInPastForPayRunDate);
        paramValues[2] = earliestPayrollRunDateMin;

        DomainEntitySet<PayrollRun> retList = Application
                .findByNamedQueryUsingCache(PayrollRun.class, "findPayrollRunByLimitIncreaseCriteria", paramNames, paramValues);

        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Number of Payrolls ByLimitIncreaseCriteria" , 1, retList.size());
    }

}
