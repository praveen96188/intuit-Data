package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.SettlementType;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddEscalationProcessDataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 *
 * User: wnichols
 * Date: Jan 4, 2008
 * Time: 10:04:14 AM

 */
public class AddEscalationProcessTests
{
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        AddEscalationProcessDataLoader.loadPayrollRunForAddEscalationTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void happy_ee()
    {
        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        DateDTO dtoTxnDate = new DateDTO();
        dtoTxnDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEscalation(AddEscalationProcessDataLoader.sCompany.getSourceSystemCd(),
                                                                     AddEscalationProcessDataLoader.sCompany.getSourceCompanyId(),
                                                                     AddEscalationProcessDataLoader.sPayrollRun.getSourcePayRunId(),
                                                                     true, // is employEE (not employER)
                                                                     SettlementType.CheckType,
                                                                     BigDecimal.valueOf(99.95),
                                                                     dtoTxnDate); // today's date
        PayrollServices.commitUnitOfWork();
        Assert.assertTrue(result.isSuccess());
        if (result.isSuccess())
        {
            FinancialTransaction txn = (FinancialTransaction)result.getResult();
            System.out.println("added "+txn.getTransactionType().getTransactionTypeCd()+" escalation txn with ID "+txn.getId());
        }
    }

    @Test
    public void happy_er()
    {
        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        DateDTO dtoTxnDate = new DateDTO();
        dtoTxnDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEscalation(AddEscalationProcessDataLoader.sCompany.getSourceSystemCd(),
                                                                     AddEscalationProcessDataLoader.sCompany.getSourceCompanyId(),
                                                                     AddEscalationProcessDataLoader.sPayrollRun.getSourcePayRunId(),
                                                                     false,
                                                                     SettlementType.CheckType,
                                                                     BigDecimal.valueOf(99.95),
                                                                     dtoTxnDate); // today's date
        PayrollServices.commitUnitOfWork();
        Assert.assertTrue(result.isSuccess());
        if (result.isSuccess())
        {
            FinancialTransaction txn = (FinancialTransaction)result.getResult();
            System.out.println("added "+txn.getTransactionType().getTransactionTypeCd()+" escalation txn with ID "+txn.getId());
        }
    }

    @Test
    public void missing_src_system_code()
    {
        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        DateDTO dtoTxnDate = new DateDTO();
        dtoTxnDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEscalation(null, //sCompany.getSourceSystem().getSourceSystemCd(),
                                                                     AddEscalationProcessDataLoader.sCompany.getSourceCompanyId(),
                                                                     AddEscalationProcessDataLoader.sPayrollRun.getSourcePayRunId(),
                                                                     false,
                                                                     SettlementType.CheckType,
                                                                     BigDecimal.valueOf(99.95),
                                                                     dtoTxnDate); // today's date
        PayrollServices.commitUnitOfWork();
        for (int i=0 ; i<result.getMessages().size() ; i++)
            System.out.println(result.getMessages().get(i));

        Assert.assertTrue(! result.isSuccess());
        Assert.assertTrue(result.getMessages().size() > 0);
        Assert.assertTrue(result.getMessages().get(0).getMessageCode().equals("137"));
    }

    @Test
    public void missing_src_company_id()
    {
        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        DateDTO dtoTxnDate = new DateDTO();
        dtoTxnDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEscalation(AddEscalationProcessDataLoader.sCompany.getSourceSystemCd(),
                                                                     null,//sCompany.getSourceCompanyId(),
                                                                     AddEscalationProcessDataLoader.sPayrollRun.getSourcePayRunId(),
                                                                     false,
                                                                     SettlementType.CheckType,
                                                                     BigDecimal.valueOf(99.95),
                                                                     dtoTxnDate); // today's date
        PayrollServices.commitUnitOfWork();
        for (int i=0 ; i<result.getMessages().size() ; i++)
            System.out.println(result.getMessages().get(i));

        Assert.assertTrue(! result.isSuccess());
        Assert.assertTrue(result.getMessages().size() > 0);
        Assert.assertTrue(result.getMessages().get(0).getMessageCode().equals("138"));
    }

    @Test
    public void no_such_company()
    {
        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        DateDTO dtoTxnDate = new DateDTO();
        dtoTxnDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEscalation(AddEscalationProcessDataLoader.sCompany.getSourceSystemCd(),
                                                                     "NOT_"+ AddEscalationProcessDataLoader.sCompany.getSourceCompanyId(),
                                                                     AddEscalationProcessDataLoader.sPayrollRun.getSourcePayRunId(),
                                                                     false,
                                                                     SettlementType.CheckType,
                                                                     BigDecimal.valueOf(99.95),
                                                                     dtoTxnDate); // today's date
        PayrollServices.commitUnitOfWork();
        for (int i=0 ; i<result.getMessages().size() ; i++)
            System.out.println(result.getMessages().get(i));

        Assert.assertTrue(! result.isSuccess());
        Assert.assertTrue(result.getMessages().size() > 0);
        Assert.assertTrue(result.getMessages().get(0).getMessageCode().equals("169"));
    }

    @Test
    public void no_such_payroll_run()
    {
        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        DateDTO dtoTxnDate = new DateDTO();
        dtoTxnDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEscalation(AddEscalationProcessDataLoader.sCompany.getSourceSystemCd(),
                                                                     AddEscalationProcessDataLoader.sCompany.getSourceCompanyId(),
                                                                     "NOT_"+ AddEscalationProcessDataLoader.sPayrollRun.getSourcePayRunId(),
                                                                     false,
                                                                     SettlementType.CheckType,
                                                                     BigDecimal.valueOf(99.95),
                                                                     dtoTxnDate); // today's date
        PayrollServices.commitUnitOfWork();
        for (int i=0 ; i<result.getMessages().size() ; i++)
            System.out.println(result.getMessages().get(i));

        Assert.assertTrue(! result.isSuccess());
        Assert.assertTrue(result.getMessages().size() > 0);
        Assert.assertTrue(result.getMessages().get(0).getMessageCode().equals("194"));
    }

    @Test
    public void bad_amount()
    {
        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        DateDTO dtoTxnDate = new DateDTO();
        dtoTxnDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEscalation(AddEscalationProcessDataLoader.sCompany.getSourceSystemCd(),
                                                                     AddEscalationProcessDataLoader.sCompany.getSourceCompanyId(),
                                                                     AddEscalationProcessDataLoader.sPayrollRun.getSourcePayRunId(),
                                                                     false,
                                                                     SettlementType.CheckType,
                                                                     BigDecimal.valueOf(0.0),
                                                                     dtoTxnDate); // today's date
        PayrollServices.commitUnitOfWork();
        for (int i=0 ; i<result.getMessages().size() ; i++)
            System.out.println(result.getMessages().get(i));

        Assert.assertTrue(! result.isSuccess());
        Assert.assertTrue(result.getMessages().size() > 0);
        Assert.assertTrue(result.getMessages().get(0).getMessageCode().equals("283"));
    }

    @Test
    public void date_in_future()
    {
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        Calendar badDate = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 3, SpcfTimeZone.getLocalTimeZone()));
        badDate.add(Calendar.DATE, 1); // tomorrow
        DateDTO dtoTxnDate = new DateDTO();
        dtoTxnDate.set(badDate.get(Calendar.YEAR), badDate.get(Calendar.MONTH),
                                badDate.get(Calendar.DAY_OF_MONTH));
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEscalation(AddEscalationProcessDataLoader.sCompany.getSourceSystemCd(),
                                                                     AddEscalationProcessDataLoader.sCompany.getSourceCompanyId(),
                                                                     AddEscalationProcessDataLoader.sPayrollRun.getSourcePayRunId(),
                                                                     false,
                                                                     SettlementType.CheckType,
                                                                     BigDecimal.valueOf(99.95),
                                                                     dtoTxnDate);
        PayrollServices.commitUnitOfWork();
        for (int i=0 ; i<result.getMessages().size() ; i++)
            System.out.println(result.getMessages().get(i));

        Assert.assertTrue(! result.isSuccess());
        Assert.assertTrue(result.getMessages().size() > 0);
        Assert.assertTrue(result.getMessages().get(0).getMessageCode().equals("266"));
    }

    @Test
    public void date_too_old()
    {
        Calendar badDate = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        badDate.add(Calendar.DATE, -46);
        DateDTO dtoTxnDate = new DateDTO();
        dtoTxnDate.set(badDate.get(Calendar.YEAR), badDate.get(Calendar.MONTH),
                                badDate.get(Calendar.DAY_OF_MONTH));
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEscalation(AddEscalationProcessDataLoader.sCompany.getSourceSystemCd(),
                                                                     AddEscalationProcessDataLoader.sCompany.getSourceCompanyId(),
                                                                     AddEscalationProcessDataLoader.sPayrollRun.getSourcePayRunId(),
                                                                     false,
                                                                     SettlementType.CheckType,
                                                                     BigDecimal.valueOf(99.95),
                                                                     dtoTxnDate);
        PayrollServices.commitUnitOfWork();
        for (int i=0 ; i<result.getMessages().size() ; i++)
            System.out.println(result.getMessages().get(i));

        Assert.assertTrue(! result.isSuccess());
        Assert.assertTrue(result.getMessages().size() > 0);
        Assert.assertTrue(result.getMessages().get(0).getMessageCode().equals("271"));
    }


}
