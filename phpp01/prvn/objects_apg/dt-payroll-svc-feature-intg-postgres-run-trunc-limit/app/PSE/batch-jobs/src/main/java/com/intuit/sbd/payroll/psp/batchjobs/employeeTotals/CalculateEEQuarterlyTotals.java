package com.intuit.sbd.payroll.psp.batchjobs.employeeTotals;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.FlushMode;
import org.hibernate.Query;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

/**
 * User: ihannur
 * Date: 10/26/12
 * Time: 5:22 PM
 */
@SuppressWarnings({"JpaQueryApiInspection", "JpaQlInspection"})
public class CalculateEEQuarterlyTotals {

    private static SpcfLogger logger = Application.getLogger(CalculateEEQuarterlyTotals.class);

    private int mThreadsCount;
    private boolean mCalcPayrollItemTotals = false;
    private String mHourlyRateLawIds = "130,101";
    private String[] mHoursWorkedLawIds = new String[]{};
    private SpcfCalendar mPspDate;

    public static final int MAX_WEEKS_IN_QUARTER = 13;
    public static final String LA_LAW_ID = "101";

    public CalculateEEQuarterlyTotals() {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            mCalcPayrollItemTotals = SystemParameter.findBooleanValue(SystemParameter.Code.EE_TOTALS_CALC_PAYROLL_ITEMS, mCalcPayrollItemTotals);
            mThreadsCount = SystemParameter.findIntValue(SystemParameter.Code.EE_TOTALS_CALC_NUM_THREADS, Runtime.getRuntime().availableProcessors() * 2);
            mHourlyRateLawIds = SystemParameter.findStringValue(SystemParameter.Code.HOURLY_RATE_REQUIRED_LAW_IDS, mHourlyRateLawIds);

            Set<String> hoursWorkedLawIds = new HashSet<String>();
            for (HoursWorkedException hoursWorkedException : Application.findObjects(HoursWorkedException.class)) {
                hoursWorkedLawIds.add(hoursWorkedException.getLaw().getLawId());
            }
            mHoursWorkedLawIds = hoursWorkedLawIds.toArray(new String[hoursWorkedLawIds.size()]);

            mPspDate = PSPDate.getPSPTime();

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public enum Mode {
        UPDATE,
        BACKDATE,
        FLUSH
    }

    class CompanyWithStartDate {
        CompanyWithStartDate(SpcfUniqueId pCompanyId, SpcfCalendar pStartDate) {
            companyId = pCompanyId;
            startDate = pStartDate;
        }

        public SpcfUniqueId companyId;
        public SpcfCalendar startDate;
    }

    class MonthsWorked {
        MonthsWorked(SpcfUniqueId pEmployeeId, String pLawId, boolean pMonth1, boolean pMonth2, boolean pMonth3) {
            employeeId = pEmployeeId;
            lawId = pLawId;
            month1 = pMonth1;
            month2 = pMonth2;
            month3 = pMonth3;
        }

        public SpcfUniqueId employeeId;
        public String lawId;
        public boolean month1;
        public boolean month2;
        public boolean month3;
    }

    class SUIHoursWorked {
        SUIHoursWorked(SpcfUniqueId pEmployeeId, String pLawId, double pHoursWorked) {
            employeeId = pEmployeeId;
            lawId = pLawId;
            hoursWorked = pHoursWorked;
        }

        SpcfUniqueId employeeId;
        String lawId;
        double hoursWorked;
    }

    public void flushQuarter(String pYearQuarter, String pSeqPattern) {

        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeTotalsCalculationBatchJob);
            if(pYearQuarter == null) {
                flushCurrentQuarter(pSeqPattern);
                return;
            }

            int currentYear = mPspDate.getYear();
            int currentQuarter = CalendarUtils.getQuarterAsInt(mPspDate);

            int flushYear = Integer.parseInt(pYearQuarter.substring(0, 4));
            int flushQuarter = Integer.parseInt(pYearQuarter.substring(5));

            if(currentYear == flushYear && currentQuarter == flushQuarter) {
                flushCurrentQuarter(pSeqPattern);
                return;
            }

            if(flushYear > currentYear || (flushYear == currentYear && flushQuarter > currentQuarter)) {
                throw new RuntimeException("Flush is for current quarter or previous quarters, not for future quarters");
            } else {
                SpcfCalendar startDate = CalendarUtils.getFirstDayOfQuarter(flushYear, flushQuarter);
                flushPreviousQuarter(startDate, pSeqPattern);
            }

        } catch (Throwable t) {
            logger.error("Failed to process EE Totals for new payrolls", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    public void flushCurrentQuarter(String pSeqPattern) {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeTotalsCalculationBatchJob);
            StopWatch sw = new StopWatch().start();

            PayrollServices.beginUnitOfWork();

            SpcfCalendar startDate = CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime());
            Timestamp startTimestamp = new Timestamp(CalendarUtils.convertLocalTimestamp(startDate.getTimeInMilliseconds()).getTime());

            Query queryObject;
            if(pSeqPattern != null) {
                queryObject = Application.getHibernateSession().getNamedQuery("findFlushCurrentQuarterCompaniesWithPattern");
                queryObject.setParameter("compSeqPattern", pSeqPattern);
            } else {
                queryObject = Application.getHibernateSession().getNamedQuery("findFlushCurrentQuarterCompanies");
            }
            queryObject.setParameter("fromDate", startTimestamp);

            //noinspection unchecked
            List<Object[]> companyIdsWithDates = queryObject.list();

            List<CompanyWithStartDate> companiesToProcess = getCompanyIdsWithFromDate(companyIdsWithDates);

            logger.info(String.format("Fetched %d companies with start date to process for current quarter flush %s,", companiesToProcess.size(), sw.getElapsedTimeString()));

            Application.rollbackUnitOfWork();

            if(companiesToProcess.isEmpty()) {
                return;
            }

            threadProcessing(companiesToProcess);

        } catch (Throwable t) {
            logger.error("Failed to process EE Totals for new payrolls", t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    public void flushPreviousQuarter(SpcfCalendar pStartDate, String pSeqPattern) {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeTotalsCalculationBatchJob);
            StopWatch sw = new StopWatch().start();

            Timestamp startTimestamp = new Timestamp(CalendarUtils.convertLocalTimestamp(pStartDate.getTimeInMilliseconds()).getTime());

            PayrollServices.beginUnitOfWork();
            Query queryObject;
            if(pSeqPattern != null) {
                queryObject = Application.getHibernateSession().getNamedQuery("findFlushPreviousQuarterCompaniesWithPattern");
                queryObject.setParameter("compSeqPattern", pSeqPattern);
            } else {
                queryObject = Application.getHibernateSession().getNamedQuery("findFlushPreviousQuarterCompanies");
            }
            queryObject.setParameter("fromDate", startTimestamp);

            List<CompanyWithStartDate> companiesToProcess = new ArrayList<CompanyWithStartDate>();

            //noinspection unchecked
            List<SpcfUniqueId> companyIds = queryObject.list();

            for (SpcfUniqueId companyId : companyIds) {
                companiesToProcess.add(new CompanyWithStartDate(companyId, pStartDate));
            }

            logger.info(String.format("Fetched  %d companies to process for previous quarter flush %s,", companiesToProcess.size(), sw.getElapsedTimeString()));
            Application.rollbackUnitOfWork();

            if (!companiesToProcess.isEmpty()) {
                threadProcessing(companiesToProcess);
            }

        } catch (Throwable t) {
            logger.error("Failed to flush previous quarter calculations", t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    public void updateForNewPayrolls() {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeTotalsCalculationBatchJob);
            StopWatch sw = new StopWatch().start();

            PayrollServices.beginUnitOfWork();
            Query queryObject = Application.getHibernateSession().getNamedQuery("findCompaniesWithNewPayrolls");

            //noinspection unchecked
            List<Object[]> companyIdsWithDates = queryObject.list();
            List<CompanyWithStartDate> companiesToProcess = getCompanyIdsWithFromDate(companyIdsWithDates);

            logger.info(String.format("Fetched %d companies with start date to process for new payrolls in %s,", companiesToProcess.size(), sw.getElapsedTimeString()));

            Application.rollbackUnitOfWork();

            if (!companiesToProcess.isEmpty()) {
                threadProcessing(companiesToProcess);
            }

        } catch (Throwable t) {
            logger.error("Failed to process EE Totals for new payrolls", t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    public void processBackdatedPayrolls() {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeTotalsCalculationBatchJob);
            StopWatch sw = new StopWatch().start();

            PayrollServices.beginUnitOfWork();
            Query queryObject = Application.getHibernateSession().getNamedQuery("findCompaniesWithBackdatedPayrolls");

            SpcfCalendar startDate = CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime());
            Timestamp startTimestamp = new Timestamp(CalendarUtils.convertLocalTimestamp(startDate.getTimeInMilliseconds()).getTime());

            queryObject.setParameter("cutOffDate", startTimestamp);

            //noinspection unchecked
            List<Object[]> companyIdsWithDates = queryObject.list();

            List<CompanyWithStartDate> companiesToProcess = getCompanyIdsWithFromDate(companyIdsWithDates);

            logger.info(String.format("Fetched %d companies with start date to process backdated payrolls in %s,", companiesToProcess.size(), sw.getElapsedTimeString()));

            Application.rollbackUnitOfWork();

            if (!companiesToProcess.isEmpty()) {
                threadProcessing(companiesToProcess);
            }

        } catch (Throwable t) {
            logger.error("Failed to process EE Totals for new payrolls", t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private List<CompanyWithStartDate> getCompanyIdsWithFromDate(List<Object[]> pCompanyIdsWithDates) {
        List<CompanyWithStartDate> companiesToProcess = new ArrayList<CompanyWithStartDate>();

        for (Object[] values : pCompanyIdsWithDates) {
            if(values[0] != null && values[1] != null) {
                SpcfCalendar fromDate = (SpcfCalendar) values[1];
                companiesToProcess.add(new CompanyWithStartDate((SpcfUniqueId) values[0], SpcfCalendar.createInstance(fromDate.getYear(), fromDate.getMonth(), fromDate.getDay(), SpcfTimeZone.getLocalTimeZone())));
            }
        }
        return companiesToProcess;
    }

    protected HashSet<SpcfUniqueId> threadProcessing(List<CompanyWithStartDate> pCompaniesToProcess) throws Exception {
        ExecutorService threadPool = null;

        if (pCompaniesToProcess.isEmpty()) {
            return null;
        }

        StopWatch sw = new StopWatch().start();
        logger.info("Payroll Items calculation Flag (EE_TOTALS_CALC_PAYROLL_ITEMS) value is:" + String.valueOf(mCalcPayrollItemTotals) +
                                ", Payroll Item totals are " + (mCalcPayrollItemTotals ? "being " : "NOT ") + "calculated in this run.");

        try {
            threadPool = Executors.newFixedThreadPool(mThreadsCount);
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);

            // Execute each company with start date in one thread
            for (CompanyWithStartDate companyWithStartDate : pCompaniesToProcess) {
                processCompanyPayrolls(completionService, companyWithStartDate.companyId, companyWithStartDate.startDate);
            }

            // Wait for the results of each thread execution
            try {
                for (int t = 0; t < pCompaniesToProcess.size(); t++) {
                    completionService.take();
                    if (t % 10000 == 0) {
                        logger.info("Completed " + t + " companies quarterly totals in: " + sw.getElapsedTimeString());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            sw.stop();
            logger.info("Completed "+ pCompaniesToProcess.size() +" companies quarterly totals in: " + sw.getElapsedTimeString());
        } finally {
            ThreadingUtils.shutdownAndAwaitTermination(threadPool);
        }

        return new HashSet<SpcfUniqueId>();
    }


    private void processCompanyPayrolls(CompletionService<Integer> completionService, final SpcfUniqueId pCompanyId, final SpcfCalendar pStartDate) {
        completionService.submit(new Callable<Integer>() {
            public Integer call() {
                return processCompanyPayrolls(pCompanyId, pStartDate);
            }
        });
    }


    private int processCompanyPayrolls(SpcfUniqueId pCompanyId, SpcfCalendar pStartDate) {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeTotalsCalculationBatchJob);
            StopWatch sw = StopWatch.startTimer();

            SpcfCalendar quarterStartDate = CalendarUtils.getFirstDayOfQuarter(pStartDate);
            SpcfCalendar lastDayOfQuarter = CalendarUtils.getLastDayOfQuarter(quarterStartDate);

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            //Picking all "Pending" EmpTotalsPayrollRuns to update as "Processed"
            DomainEntitySet<EmpTotalsPayrollRun> empTotalsPayrollRuns = Application.find(EmpTotalsPayrollRun.class, EmpTotalsPayrollRun.Company().Id().equalTo(pCompanyId)
                                                                                                                                       .And(EmpTotalsPayrollRun.Status().equalTo(EmpTotalsPayrollStatus.Pending))
                                                                                                                                       .And(EmpTotalsPayrollRun.QuarterStartDate().equalTo(quarterStartDate)));
            //Taxes calculation
            processCompanyEmployeeTaxesTotals(pCompanyId, quarterStartDate, lastDayOfQuarter);

            //Payroll item calculation if enabled
            logger.info("Calling storedProcedure="+StoredProcedures.PAYROLL_ITEM_TOTALS_COMP_QTR_PAYROLL_ITEM_TOT.getStoredProcedureName() +
                    " pCompanyId="+pCompanyId+" quarterStartDate="+quarterStartDate.getTimeInMilliseconds());
            if(mCalcPayrollItemTotals) {
                Application.executeSqlProcedure(StoredProcedures.PAYROLL_ITEM_TOTALS_COMP_QTR_PAYROLL_ITEM_TOT, true,
                        Pair.of(String.class, pCompanyId.toString()),
                        Pair.of(Timestamp.class, new Timestamp(quarterStartDate.getTimeInMilliseconds())));
            }

            //Updating selected EmpTotalsPayrollRuns as "Processed"
            for (EmpTotalsPayrollRun empTotalsPayrollRun : empTotalsPayrollRuns) {
                empTotalsPayrollRun.setStatus(EmpTotalsPayrollStatus.Processed);
            }
            PayrollServices.commitUnitOfWork();

            logger.debug("Processing all calculations finished for Company Id:" + pCompanyId + " and StartDate: " + pStartDate +" in :" + sw.getElapsedTimeString());
            return 1;
        } catch (Throwable t) {
            logger.error(String.format("Could not process calculation for company ID: %s ", pCompanyId), t);
            return 0;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private void processCompanyEmployeeTaxesTotals(SpcfUniqueId pCompanyId, SpcfCalendar pStartDate, SpcfCalendar pEndDate) {
        logger.debug(String.format("Processing taxes calculation for CompanyId: %s", pCompanyId));
        StopWatch sw = StopWatch.startTimer();

        DomainEntitySet<EmployeeLawQtrTotals> allCompanyEmployeeLawQtrTotals = new DomainEntitySet<EmployeeLawQtrTotals>();

        DomainEntitySet<EmployeeLawQtrTotals> companyEmployeeLawQtrTotals =
                Application.find(EmployeeLawQtrTotals.class,
                                 EmployeeLawQtrTotals.Company().Id().equalTo(pCompanyId)
                                 .And(EmployeeLawQtrTotals.Year().equalTo(pStartDate.getYear()))
                                 .And(EmployeeLawQtrTotals.Quarter().equalTo(CalendarUtils.getQuarterAsInt(pStartDate))));

        allCompanyEmployeeLawQtrTotals.addAll(companyEmployeeLawQtrTotals);

        StopWatch swTaxAmounts = StopWatch.startTimer();
        @SuppressWarnings({"unchecked"})
        List<Object[]> eeTotalsTaxAmounts = getEETotalsTaxAmounts(pCompanyId, pStartDate);
        //        0: TotalWages
        //        1: TaxableWages
        //        2: TipsTaxableWagesAmount
        //        3: TaxAmount
        //        4: employee_fk
        //        5: company_law_fk
        if (!eeTotalsTaxAmounts.isEmpty()) {
            for (Object[] eeTotalsTaxAmount : eeTotalsTaxAmounts) {
                if (eeTotalsTaxAmount[4] != null) {

                    Employee employee = Application.findById(Employee.class, eeTotalsTaxAmount[4]);
                    CompanyLaw companyLaw = Application.findById(CompanyLaw.class, eeTotalsTaxAmount[5]);
                    Law law = companyLaw.getLaw();
                    int quarter = CalendarUtils.getQuarterAsInt(pStartDate);

                    EmployeeLawQtrTotals employeeQtrTotals = companyEmployeeLawQtrTotals.find(EmployeeLawQtrTotals.Employee().equalTo(employee)
                                                                                                                  .And(EmployeeLawQtrTotals.Law().equalTo(law))).getFirst();
                    if(employeeQtrTotals == null) {
                        employeeQtrTotals = new EmployeeLawQtrTotals();
                        allCompanyEmployeeLawQtrTotals.add(employeeQtrTotals);
                    }

                    employeeQtrTotals.setEmployee(employee);

                    employeeQtrTotals.setCompany(employee.getCompany());
                    employeeQtrTotals.setYear(pStartDate.getYear());
                    employeeQtrTotals.setQuarter(quarter);
                    employeeQtrTotals.setLaw(law);
                    employeeQtrTotals.setCompanyLaw(companyLaw);

                    employeeQtrTotals.setTotalWages((SpcfMoney) eeTotalsTaxAmount[0]);
                    employeeQtrTotals.setTaxableWages((SpcfMoney) eeTotalsTaxAmount[1]);
                    employeeQtrTotals.setTipsTaxableWagesAmount((SpcfMoney) eeTotalsTaxAmount[2]);
                    employeeQtrTotals.setTaxAmount((SpcfMoney) eeTotalsTaxAmount[3]);
                    employeeQtrTotals.setModifiedDate(PSPDate.getPSPTime());

                    Application.save(employeeQtrTotals);
                    companyEmployeeLawQtrTotals.remove(employeeQtrTotals);
                }
            }
        }

        for (EmployeeLawQtrTotals employeeLawQtrTotals : companyEmployeeLawQtrTotals) {
            employeeLawQtrTotals.setTaxAmount(SpcfMoney.ZERO);
            employeeLawQtrTotals.setTaxableWages(SpcfMoney.ZERO);
            employeeLawQtrTotals.setTotalWages(SpcfMoney.ZERO);
            employeeLawQtrTotals.setTipsTaxableWagesAmount(SpcfMoney.ZERO);
            employeeLawQtrTotals.setModifiedDate(PSPDate.getPSPTime());

            employeeLawQtrTotals.setMonthOneWorkedIndicator(false);
            employeeLawQtrTotals.setMonthTwoWorkedIndicator(false);
            employeeLawQtrTotals.setMonthThreeWorkedIndicator(false);

            employeeLawQtrTotals.setWeeksWorked(0);
            employeeLawQtrTotals.setHoursWorked(0);
            employeeLawQtrTotals.setHourlyRate(0);

            allCompanyEmployeeLawQtrTotals.remove(employeeLawQtrTotals);

            Application.save(employeeLawQtrTotals);
        }
        logger.debug("\tprocessed " + eeTotalsTaxAmounts.size() + " tax amount records: " + swTaxAmounts.getElapsedTimeString());

        // update weeks worked
        if(allCompanyEmployeeLawQtrTotals.isNotEmpty()) {
            StopWatch swWeeksWorked = StopWatch.startTimer();
            HashMap<SpcfUniqueId, Integer> employeesAndNotWorkedWeeks = getEmployeesAndNotWorkedWeeks(pCompanyId, pStartDate, pEndDate);
            for (EmployeeLawQtrTotals employeeLawQtrTotals : allCompanyEmployeeLawQtrTotals) {
                if(employeesAndNotWorkedWeeks.containsKey(employeeLawQtrTotals.getEmployee().getId())){
                    employeeLawQtrTotals.setWeeksWorked(MAX_WEEKS_IN_QUARTER - employeesAndNotWorkedWeeks.get(employeeLawQtrTotals.getEmployee().getId()));
                } else {
                    employeeLawQtrTotals.setWeeksWorked(MAX_WEEKS_IN_QUARTER);
                }
            }
            logger.debug("\tprocessed " + allCompanyEmployeeLawQtrTotals.size() + " weeks worked records: " + swWeeksWorked.getElapsedTimeString());
        }

        // Update hourly rate if there is a record with Law = 130 and 101 (VT SUI-ER,LA SUI-ERs)
        DomainEntitySet<EmployeeLawQtrTotals> employeeLawQtrTotalsToUpdateRate = allCompanyEmployeeLawQtrTotals.find(EmployeeLawQtrTotals.Law().LawId().in(mHourlyRateLawIds.split(",")));

        if(employeeLawQtrTotalsToUpdateRate.isNotEmpty()) {
            StopWatch swHourlyRate = StopWatch.startTimer();
            HashMap<SpcfUniqueId, Double> employeesHourlyRate = new HashMap<SpcfUniqueId, Double>();
            for (EmployeeLawQtrTotals employeeLawQtrTotals : employeeLawQtrTotalsToUpdateRate) {
                employeesHourlyRate = getEmployeesHourlyRate(pCompanyId,pStartDate,employeeLawQtrTotals.getLaw().getLawId());
                if(employeesHourlyRate.containsKey(employeeLawQtrTotals.getEmployee().getId())) {
                    employeeLawQtrTotals.setHourlyRate(employeesHourlyRate.get(employeeLawQtrTotals.getEmployee().getId()));
                } else {
                    employeeLawQtrTotals.setHourlyRate(0); // Setting it to 0, in case all paycheck with hourly rates are voided
                }
            }
            logger.debug("\tprocessed " + employeesHourlyRate.size() + " hourly rate records: " + swHourlyRate.getElapsedTimeString());
        }

        // update months months worked
        if (allCompanyEmployeeLawQtrTotals.isNotEmpty()) {
            // if the company has any laws that require month counts, calculate months worked for each law that requires it
            if (allCompanyEmployeeLawQtrTotals.find(EmployeeLawQtrTotals.Law().RequiresMonthCounts().equalTo(true)).isNotEmpty()) {
                StopWatch swMonthsWorked = StopWatch.startTimer();
                List<MonthsWorked> eeLawMonthsWorked = getMonthsWorked(pCompanyId, pStartDate);
                for (MonthsWorked eeMonthsWorkedForLaw : eeLawMonthsWorked) {
                    DomainEntitySet<EmployeeLawQtrTotals> employeeLawQtrTotalsToUpdateMonths =
                            allCompanyEmployeeLawQtrTotals.find(
                                    EmployeeLawQtrTotals.Employee().Id().equalTo(eeMonthsWorkedForLaw.employeeId)
                                    .And(EmployeeLawQtrTotals.Law().LawId().equalTo(eeMonthsWorkedForLaw.lawId)));
                    for (EmployeeLawQtrTotals employeeLawQtrTotalsToUpdateMonth : employeeLawQtrTotalsToUpdateMonths) {
                        employeeLawQtrTotalsToUpdateMonth.setMonthOneWorkedIndicator(eeMonthsWorkedForLaw.month1);
                        employeeLawQtrTotalsToUpdateMonth.setMonthTwoWorkedIndicator(eeMonthsWorkedForLaw.month2);
                        employeeLawQtrTotalsToUpdateMonth.setMonthThreeWorkedIndicator(eeMonthsWorkedForLaw.month3);
                    }
                }
                logger.debug("\tprocessed " + eeLawMonthsWorked.size() + " months worked records: " + swMonthsWorked.getElapsedTimeString());
            }
        }

        // update hours worked
        if (allCompanyEmployeeLawQtrTotals.isNotEmpty()) {
            if (allCompanyEmployeeLawQtrTotals.find(EmployeeLawQtrTotals.Law().LawId().in(mHoursWorkedLawIds)).isNotEmpty()) {
                StopWatch swHoursWorked = StopWatch.startTimer();
                List<SUIHoursWorked> eeHoursWorked = getHoursWorked(pCompanyId, pStartDate);
                for (SUIHoursWorked eeSUIHoursWorked : eeHoursWorked) {
                    DomainEntitySet<EmployeeLawQtrTotals> employeeLawQtrTotalsToUpdateHours =
                            allCompanyEmployeeLawQtrTotals.find(
                                    EmployeeLawQtrTotals.Employee().Id().equalTo(eeSUIHoursWorked.employeeId)
                                                        .And(EmployeeLawQtrTotals.Law().LawId().equalTo(eeSUIHoursWorked.lawId)));
                    for (EmployeeLawQtrTotals employeeLawQtrTotalsToUpdateHour : employeeLawQtrTotalsToUpdateHours) {
                        employeeLawQtrTotalsToUpdateHour.setHoursWorked(eeSUIHoursWorked.hoursWorked);
                    }
                }
                logger.debug("\tprocessed " + eeHoursWorked.size() + " hours worked records: " + swHoursWorked.getElapsedTimeString());
            }
        }

        logger.debug(String.format("Finished processing taxes calculation for CompanyId: %s, in: %s", pCompanyId, sw.getElapsedTimeString()));
    }

    private List getEETotalsTaxAmounts(SpcfUniqueId pCompanyId, SpcfCalendar pFromDate) {

        Date startDate = CalendarUtils.convertLocalTimestamp(pFromDate.getTimeInMilliseconds());
        Timestamp statTimeStamp = new Timestamp(startDate.getTime());

        Query queryObject = Application.getHibernateSession().getNamedQuery("calcEmployeeLawQuarterlyTotals");
        queryObject.setParameter("quarterStartDate", statTimeStamp);
        queryObject.setParameter("companySeqId", pCompanyId.toString());

        return queryObject.list();
    }

    private HashMap<SpcfUniqueId, Integer> getEmployeesAndNotWorkedWeeks(SpcfUniqueId pCompanyId, SpcfCalendar pFromDate, SpcfCalendar pEndDate) {

        HashMap<SpcfUniqueId, Integer> employeesAndWeeks = new HashMap<SpcfUniqueId, Integer>();

        String[] paramNames = new String[3];
        Object[] paramValues = new Object[3];

        paramNames[0] = "companyId";
        paramNames[1] = "fromDate";
        paramNames[2] = "toDate";

        paramValues[0] = pCompanyId;
        paramValues[1] = pFromDate;
        paramValues[2] = pEndDate;

        int startWeek = CalendarUtils.getWeekOfYear(pFromDate);
        int endWeek = CalendarUtils.getWeekOfYear(pEndDate) - 1;  // Terminated week is consider as worked

        List<Object[]> hiredTerminatedEmployees = Application.executeNamedQuery("getHiredOrTerminatedEmployees", paramNames, paramValues);
        for (Object hiredTerminatedEmployee[] : hiredTerminatedEmployees) {
            int weeks = 0;
            SpcfUniqueId employeeId = (SpcfUniqueId) hiredTerminatedEmployee[0];
            SpcfCalendar hiredDate = (SpcfCalendar) hiredTerminatedEmployee[1];
            SpcfCalendar terminationDate = (SpcfCalendar) hiredTerminatedEmployee[2];

            if(hiredDate != null && hiredDate.between(pFromDate, pEndDate)) {
                weeks += CalendarUtils.getWeekOfYear(hiredDate) - startWeek;
            }

            if(terminationDate != null && terminationDate.between(pFromDate, pEndDate)) {
                weeks += endWeek - CalendarUtils.getWeekOfYear(terminationDate) ;
            }

            if(weeks > 0) {
                employeesAndWeeks.put(employeeId, weeks);
            }
        }
        return employeesAndWeeks;
    }

    private HashMap<SpcfUniqueId, Double> getEmployeesHourlyRate(SpcfUniqueId pCompanyId, SpcfCalendar pFromDate,String pHourlyRateLawIds) {

        HashMap<SpcfUniqueId, Double> employeesHourlyRate = new HashMap<SpcfUniqueId, Double>();

        Date startDate = CalendarUtils.convertLocalTimestamp(pFromDate.getTimeInMilliseconds());
        Timestamp statTimeStamp = new Timestamp(startDate.getTime());

        String[] paramNames = new String[2];
        Object[] paramValues = new Object[2];

        paramNames[0] = "companyId";
        paramNames[1] = "quarterStartDate";

        paramValues[0] = pCompanyId.toString();
        paramValues[1] = statTimeStamp;
        String pQuery;

        if (LA_LAW_ID.equals(pHourlyRateLawIds)) {
            pQuery = "getEmployeesHourlyRateLA";
        } else {
            pQuery = "getEmployeesHourlyRate";
        }
        List<Object[]> employeesHourlyRateResultSet = Application.executeNamedQuery(pQuery, paramNames, paramValues);
        for (Object hiredTerminatedEmployee[] : employeesHourlyRateResultSet) {
            SpcfUniqueId employeeId = (SpcfUniqueId) hiredTerminatedEmployee[0];
            employeesHourlyRate.put(employeeId, (Double) hiredTerminatedEmployee[1]);

        }
        return employeesHourlyRate;
    }

    private List<MonthsWorked> getMonthsWorked(SpcfUniqueId pCompanyId, SpcfCalendar pFromDate) {
        List<MonthsWorked> results = new ArrayList<MonthsWorked>();

        Date startDate = CalendarUtils.convertLocalTimestamp(pFromDate.getTimeInMilliseconds());
        Timestamp statTimeStamp = new Timestamp(startDate.getTime());

        SpcfCalendar monthCal = pFromDate.copy();
        monthCal.setValues(monthCal.getYear(), monthCal.getMonth(), 12, 0, 0, 0, 0);
        Timestamp m1 = new Timestamp(monthCal.getTimeInMilliseconds());

        monthCal.addMonths(1);
        Timestamp m2 = new Timestamp(monthCal.getTimeInMilliseconds());

        monthCal.addMonths(1);
        Timestamp m3 = new Timestamp(monthCal.getTimeInMilliseconds());

        String[] paramNames = new String[5];
        Object[] paramValues = new Object[5];

        paramNames[0] = "companyId";
        paramNames[1] = "qtrStart";
        paramNames[2] = "m1";
        paramNames[3] = "m2";
        paramNames[4] = "m3";

        paramValues[0] = pCompanyId.toString();
        paramValues[1] = statTimeStamp;
        paramValues[2] = m1;
        paramValues[3] = m2;
        paramValues[4] = m3;

        List<Object[]> monthsWorkedResultSet = Application.executeNamedQuery(
                Application.getQueryName("getEmployeeMonthsWorkedForRequiredLaws"), paramNames, paramValues);
        for (Object[] eeLawMonthsWorked : monthsWorkedResultSet) {
            if(eeLawMonthsWorked[0] != null) {
                results.add( new MonthsWorked(
                                (SpcfUniqueId) eeLawMonthsWorked[0],
                                (String) eeLawMonthsWorked[1],
                                (Boolean) eeLawMonthsWorked[2],
                                (Boolean) eeLawMonthsWorked[3],
                                (Boolean) eeLawMonthsWorked[4]));
            }
        }

        return results;
    }

    private List<SUIHoursWorked> getHoursWorked(SpcfUniqueId pCompanyId, SpcfCalendar pFromDate) {
        List<SUIHoursWorked> results = new ArrayList<SUIHoursWorked>();

        Date startDate = CalendarUtils.convertLocalTimestamp(pFromDate.getTimeInMilliseconds());
        Timestamp statTimeStamp = new Timestamp(startDate.getTime());

        String[] paramNames = new String[3];
        Object[] paramValues = new Object[3];

        paramNames[0] = "companyId";
        paramNames[1] = "qtrStart";
        paramNames[2] = "lawIds";

        paramValues[0] = pCompanyId.toString();
        paramValues[1] = statTimeStamp;
        paramValues[2] = mHoursWorkedLawIds;

        List<Object[]> hoursWorkedResultSet = Application.executeNamedQuery("getEmployeesSUIHoursWorked", paramNames, paramValues);
        for (Object[] eeHoursWorked : hoursWorkedResultSet) {
            if(eeHoursWorked[0] != null) {
                results.add( new SUIHoursWorked(
                    (SpcfUniqueId) eeHoursWorked[0],
                    (String) eeHoursWorked[1],
                    (Double) eeHoursWorked[2]));
            }
        }

        return results;
    }

}
