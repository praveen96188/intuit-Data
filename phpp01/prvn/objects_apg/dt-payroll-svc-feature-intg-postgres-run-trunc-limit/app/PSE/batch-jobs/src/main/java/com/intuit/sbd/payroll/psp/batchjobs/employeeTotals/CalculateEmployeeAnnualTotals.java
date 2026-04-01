package com.intuit.sbd.payroll.psp.batchjobs.employeeTotals;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.ScrollableResults;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: mvillani
 * Date: 8/27/12
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class CalculateEmployeeAnnualTotals {
    private static SpcfLogger logger = Application.getLogger(CalculateEmployeeAnnualTotals.class);


    private static ArrayList<SpcfUniqueId> companiesToProcess = new ArrayList<SpcfUniqueId>();
    private static int mYear = 0;

    private static final String YEAR_COMMAND = "-year";
    private static final String SINGLE_COMPANYID_COMMAND = "-companyId";


    public void main(String args[]) {
        try {
            StopWatch sw = new StopWatch().start();
            parseArgs(args);
            Application.beginUnitOfWork();

            if (mYear == 0) {
                int currentQuarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
                mYear = PSPDate.getPSPTime().getYear();
                if (currentQuarter == 1) {
                    mYear = mYear - 1;
                }
            }
            calculateW2Totals(mYear, companiesToProcess);
            Application.commitUnitOfWork();
            sw.stop();
            logger.info("completed processing " + "     duration: " + sw.getElapsedTimeString());
        } catch (Throwable t) {
            logger.error("failed to process Annual W2 Calculations", t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private void parseArgs(String[] args) {
        for (String arg : args) {
            String[] argParts = arg.split(":");
            if (argParts.length == 2) {
                if (argParts[0].equals(YEAR_COMMAND)) {
                    mYear = Integer.parseInt(argParts[1]);
                } else if (argParts[0].equals(SINGLE_COMPANYID_COMMAND)) {
                    for (String companyId : argParts[1].split(",")) {
                        Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);
                        if (company == null) {
                            logger.warn("Skipping invalid company " + companyId);
                        } else {
                            if (!companiesToProcess.contains(company.getId())) {
                                companiesToProcess.add(company.getId());
                            }
                        }
                    }
                } else {
                    throw new RuntimeException("Invalid command: " + argParts[0]);
                }
            } else {
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }
    }

    protected void calculateW2Totals(int pYear, ArrayList<SpcfUniqueId> pCompaniesToProcess) throws Exception {

        try {
            // process

            logger.info("Starting to process W2 annual calculations for " + pCompaniesToProcess.size() + " companies.");
            if (pCompaniesToProcess != null && pCompaniesToProcess.size() > 0) {
                multithreadProcessing(pYear, pCompaniesToProcess);
            } else {
                logger.info("Calling storedProcedure="+StoredProcedures.PRC_CALCULATE_W2_TOTALS.getStoredProcedureName()+" pYear="+pYear);
                Application.executeSqlProcedure(StoredProcedures.PRC_CALCULATE_W2_TOTALS, true,
                        Pair.of(Integer.class, pYear),
                        Pair.of(String.class, null));
            }


            logger.info("Processed all companies.");

        } catch (Throwable t) {
            logger.fatal("Exception", t);
            throw new RuntimeException(t);
        }
    }


    protected HashSet<SpcfUniqueId> multithreadProcessing(final int pYear, ArrayList<SpcfUniqueId> pCompaniesToProcess) throws Exception {
        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * 2;
        ExecutorService threadPool = null;
        try {
            threadPool = Executors.newFixedThreadPool(threadCount);
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);

            // Execute each company in one thread
            for (final SpcfUniqueId companyId : pCompaniesToProcess) {
                completionService.submit(new Callable<Integer>() {
                    public Integer call() {
                        return calculateCompanyAnnualTotals(pYear, companyId);
                    }
                });


            }

            // Wait for the results of each thread execution
            try {
                for (SpcfUniqueId companyId : pCompaniesToProcess) {
                    Future<Integer> f = completionService.take();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } finally {
            ThreadingUtils.shutdownAndAwaitTermination(threadPool);
        }

        HashSet<SpcfUniqueId> results = new HashSet<SpcfUniqueId>();
        return results;
    }

    private ArrayList<SpcfUniqueId> findCompaniesToProcess(int pYear) {
        ArrayList<SpcfUniqueId> companiesToProcess = new ArrayList<SpcfUniqueId>();
        // Get distinct companies from EmployeeLawQtrTotals
        Expression<EmployeeLawQtrTotals> query = new com.intuit.sbd.payroll.psp.query.Query<EmployeeLawQtrTotals>()
                .Select(EmployeeLawQtrTotals.Company().Id().Distinct())
                .Where(EmployeeLawQtrTotals.Year().equalTo(pYear));

        ScrollableResults companyIds = Application.findScrollable(EmployeeLawQtrTotals.class, query);

        while (companyIds.next()) {
            companiesToProcess.add((SpcfUniqueId) companyIds.get(0));
        }

        // Get  distinct companies from EmployeeLawQtrTotals

        Expression<EmployeePayrollItemQtrTotals> payrollItemQuery = new com.intuit.sbd.payroll.psp.query.Query<EmployeePayrollItemQtrTotals>()
                .Select(EmployeePayrollItemQtrTotals.CompanyPayrollItem().Company().Id().Distinct())
                .Where(EmployeePayrollItemQtrTotals.Year().equalTo(pYear))
                .OrderBy(EmployeePayrollItemQtrTotals.CompanyPayrollItem().Company().Id());
        companyIds = Application.findScrollable(EmployeePayrollItemQtrTotals.class, payrollItemQuery);

        while (companyIds.next()) {
            SpcfUniqueId companyId = (SpcfUniqueId) companyIds.get(0);
            if (!companiesToProcess.contains(companyId)) {
                companiesToProcess.add(companyId);
            }
        }
        return companiesToProcess;
    }

    private Integer calculateCompanyAnnualTotals(int pYear, SpcfUniqueId pCompanyId) {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeW2TotalsCalculationBatchJob);
            StopWatch sw = StopWatch.startTimer();
            Application.beginUnitOfWork();
            Company company = Application.findById(Company.class, pCompanyId);
            // Add CompanyTFSSubmission
            CompanyTFSSubmission companyTFSSubmission = new CompanyTFSSubmission();
            companyTFSSubmission.setCompany(company);
            companyTFSSubmission.setYear(pYear);
            companyTFSSubmission.setSubmissionStatus(TFSSubmissionStatus.Pending);
            companyTFSSubmission.setStatusEffectiveDate(PSPDate.getPSPTime().toLocal());
            Application.save(companyTFSSubmission);
            calculateLawAnnualTotals(pYear, pCompanyId);
            calculatePayrollItemsAnnualTotals(pYear, pCompanyId);
            Application.commitUnitOfWork();
            logger.info(String.format("Finished processing w2 annual calculation for CompanyId: %s, in: %s", company.getSourceCompanyId(), sw.getElapsedTimeString()));
            return 1;

        } catch (Throwable t) {
            logger.fatal("Could not process anual calculation for company ID: " + pCompanyId, t);
            throw new RuntimeException(t);
        } finally {
            Application.rollbackUnitOfWork();
        }

    }

    private void calculateLawAnnualTotals(int pYear, SpcfUniqueId pCompanyId) {

        try {


            String select =
                    " select company.Id, employee.Id, companyLaw.Id,  " +
                            "sum(eeTotals.TaxAmount) as TotalTaxAmount," +
                            " sum(eeTotals.TaxableWages) as TotalTaxWages, " +
                            " sum(eeTotals.TipsTaxableWagesAmount) as TotalTipsTaxableWages, " +
                            " sum(eeTotals.TotalWages) as TotalTotalWages" +
                            " from  com.intuit.sbd.payroll.psp.domain.EmployeeLawQtrTotals eeTotals, " +
                            "       com.intuit.sbd.payroll.psp.domain.Law as law, " +
                            "       com.intuit.sbd.payroll.psp.domain.CompanyLaw as companyLaw, " +
                            "       com.intuit.sbd.payroll.psp.domain.Company as company," +
                            "       com.intuit.sbd.payroll.psp.domain.Employee as employee ";
            String where =
                    " where eeTotals.Year = :year" +
                            "   and eeTotals.Company = company " +
                            "   and eeTotals.Employee = employee " +
                            "   and eeTotals.CompanyLaw = companyLaw " +
                            "   and companyLaw.Law = law " +
                            "   and eeTotals.Law = law" +
                            "   and companyLaw.IsArchived = false";


            if (pCompanyId != null) {
                where += "   and eeTotals.Company = :company";
            }


            String groupBy = " group by company.Id, employee.Id, companyLaw.Id";
            org.hibernate.Query hibernateQuery;

            Company company = null;

            if (pCompanyId != null) {
                company = Application.findById(Company.class, pCompanyId);
            }

            try {
                hibernateQuery = Application.createHibernateQuery(select + where + groupBy);

                hibernateQuery.setParameter("year", pYear);

                if (company != null) {
                    hibernateQuery.setParameter("company", company);
                }

            } catch (Throwable t) {
                logger.fatal("Exception", t);
                throw new RuntimeException(t);
            }


            try {
                @SuppressWarnings({"unchecked"})
                List<Object[]> results = (List<Object[]>) hibernateQuery.list();

                for (Object[] result : results) {
                    Employee employee = Application.findById(Employee.class, result[1]);
                    CompanyLaw companyLaw = Application.findById(CompanyLaw.class, (SpcfUniqueId) result[2]);
                    Law law = companyLaw.getLaw();
                    EmployeeW2Totals eeW2Totals = getEmployeeW2Totals(employee, companyLaw, null, pYear);
                    eeW2Totals.setCompany(company);
                    eeW2Totals.setEmployee(employee);
                    eeW2Totals.setYear(pYear);
                    eeW2Totals.setLaw(law);
                    eeW2Totals.setCompanyLaw(companyLaw);
                    eeW2Totals.setAmount((SpcfMoney) result[3]);
                    eeW2Totals.setTaxableWages((SpcfMoney) result[4]);
                    eeW2Totals.setTipsTaxableWagesAmount((SpcfMoney) result[5]);
                    eeW2Totals.setTotalWages((SpcfMoney) result[6]);
                    Application.save(eeW2Totals);
                }
            } catch (Throwable t) {
                logger.fatal("Exception", t);
                throw new RuntimeException(t);
            }

        } catch (Throwable t) {
            logger.error("Could not process law calculation for company ID: " + pCompanyId, t);

        }

    }

    private void calculatePayrollItemsAnnualTotals(int pYear, SpcfUniqueId pCompanyId) {

        try {

            String select =
                    " select company.Id, employee.Id, cPItem.Id, " +
                            "sum(eeTotals.Amount) as TotalAmount," +
                            " sum(eeTotals.TaxableWages) as TotalTaxWages, " +
                            " sum(eeTotals.TipsTaxableWagesAmount) as TotalTipsTaxableWages," +
                            " sum(eeTotals.TotalWages) as TotalTotalWages" +
                            " from  com.intuit.sbd.payroll.psp.domain.EmployeePayrollItemQtrTotals eeTotals," +
                            "       com.intuit.sbd.payroll.psp.domain.CompanyPayrollItem as cPItem, " +
                            "       com.intuit.sbd.payroll.psp.domain.Company as company," +
                            "       com.intuit.sbd.payroll.psp.domain.Employee as employee ";
            String where =
                    " where eeTotals.Year = :year" +
                            "   and eeTotals.CompanyPayrollItem.Company = company " +
                            "   and eeTotals.Employee = employee " +
                            "   and eeTotals.CompanyPayrollItem = cPItem" +
                            "   and cPItem.IsArchived = false";

            if (pCompanyId != null) {
                where += "   and eeTotals.CompanyPayrollItem.Company = :company";
            }


            String groupBy = " group by company.Id, employee.Id, cPItem.Id";

            org.hibernate.Query hibernateQuery = Application.createHibernateQuery(select + where + groupBy);

            hibernateQuery.setParameter("year", pYear);

            Company company = null;

            if (pCompanyId != null) {
                company = Application.findById(Company.class, pCompanyId);
                hibernateQuery.setParameter("company", company);
            }

            @SuppressWarnings({"unchecked"})
            List<Object[]> results = (List<Object[]>) hibernateQuery.list();

            for (Object[] result : results) {
                Employee employee = Application.findById(Employee.class, result[1]);
                CompanyPayrollItem cpi = Application.findById(CompanyPayrollItem.class, result[2]);
                EmployeeW2Totals eeW2Totals = getEmployeeW2Totals(employee, null, cpi, pYear);
                eeW2Totals.setEmployee(employee);
                eeW2Totals.setCompany(company);
                eeW2Totals.setYear(pYear);
                eeW2Totals.setCompanyPayrollItem(cpi);
                eeW2Totals.setAmount((SpcfMoney) result[3]);
                eeW2Totals.setTaxableWages((SpcfMoney) result[4]);
                eeW2Totals.setTipsTaxableWagesAmount((SpcfMoney) result[5]);
                eeW2Totals.setTotalWages((SpcfMoney) result[6]);
                Application.save(eeW2Totals);
            }
        } catch (Throwable t) {
            logger.error("Could not process payroll itemp calculation for company ID: " + pCompanyId, t);

        }
    }


    private EmployeeW2Totals getEmployeeW2Totals(Employee pEmployee, CompanyLaw pCompanyLaw, CompanyPayrollItem pCompanyPayrollItem, int pYear) {
        Criterion<EmployeeW2Totals> where = EmployeeW2Totals.Employee().equalTo(pEmployee)
                .And(EmployeeW2Totals.Year().equalTo(pYear));
        if (pCompanyLaw != null) {
            where = where.And(EmployeeW2Totals.CompanyLaw().equalTo(pCompanyLaw));
        }
        if (pCompanyPayrollItem != null) {
            where = where.And(EmployeeW2Totals.CompanyPayrollItem().equalTo(pCompanyPayrollItem));
        }

        DomainEntitySet<EmployeeW2Totals> eeTotals = Application.find(EmployeeW2Totals.class, where);

        if (eeTotals != null && eeTotals.size() > 0) {
            return eeTotals.get(0);
        } else {
            return new EmployeeW2Totals();
        }
    }


}
