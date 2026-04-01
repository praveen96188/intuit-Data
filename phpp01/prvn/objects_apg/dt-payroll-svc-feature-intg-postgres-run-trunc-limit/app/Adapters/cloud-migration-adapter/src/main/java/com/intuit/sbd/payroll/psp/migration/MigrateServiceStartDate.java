package com.intuit.sbd.payroll.psp.migration;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: July 5, 2012
 * Time: 7:27:58 PM
 */
public class MigrateServiceStartDate {

    private static SpcfLogger logger = Application.getLogger(MigrateServiceStartDate.class);

    private static Connection connection;
    private static ThreadLocal<PreparedStatement> companyPreparedStatement = new ThreadLocal<PreparedStatement>();
    private static ThreadLocal<PreparedStatement> employeePreparedStatement = new ThreadLocal<PreparedStatement>();
    private static ThreadLocal<PreparedStatement> payrollItemPreparedStatement = new ThreadLocal<PreparedStatement>();

    public static void main(String args[]) throws Exception {
        try {
            StopWatch sw = new StopWatch().start();

            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));

            logger.info("beginning service start date from AS400...");

            if (args.length < 1 || args.length > 2) {
                throw new RuntimeException("Usage MigrateServiceStartDate [commit: true | false] [(optional) single company psid]");
            }

            boolean commit = Boolean.parseBoolean(args[0]);
            String sourceCompanyId = null;
            if (args.length > 1) {
                sourceCompanyId = args[1];
            }

            migrateServiceStartDate(commit, sourceCompanyId);

            logger.info(" migrated service start dates in " + sw.getElapsedTimeString());
        } catch (Throwable t) {
            logger.fatal("Fatal error during service start date migration", t);
            System.exit(-1);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            if (connection != null) {
                connection.close();
            }
        }
    }

    private static void migrateServiceStartDate(final boolean pCommit, final String pSourceCompanyId) throws Throwable {
        PreparedStatement companiesStatement = null;
        ResultSet companiesResultSet = null;
        final HashMap<String, Double> taxCompanyPSIDs = new HashMap<String, Double>();
        try {
            String sqlString = "select userid, cli_first_tax_qtr from iqclient where  cli_taxservice = 'Y' and cli_first_tax_qtr > 0 ";
            if (pSourceCompanyId != null) {
                sqlString = sqlString + "and userid = ?";
            }
            companiesStatement = connection.prepareStatement(sqlString);
            companiesStatement.setString( 1, pSourceCompanyId);
            companiesResultSet = companiesStatement.executeQuery();
            while (companiesResultSet.next()) {
                taxCompanyPSIDs.put(companiesResultSet.getString(1), companiesResultSet.getDouble(2));
            }
        } catch (Exception e) {
            logger.error("FAILED TO INITIALIZE", e);
            System.exit(-1);
        } finally {
            if (companiesStatement != null) {
                companiesStatement.close();
            }
            if (companiesResultSet != null) {
                companiesResultSet.close();
            }
        }

        Executor executor = Executors.newFixedThreadPool(8);
        CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executor);

        for (final String taxCompanyPSID : taxCompanyPSIDs.keySet()) {
            completionService.submit(new Callable<Void>() {
                public Void call() throws Exception {
                    migrateSingleCompanyServiceStartDate(taxCompanyPSID, taxCompanyPSIDs.get(taxCompanyPSID), pCommit);
                    return null;
                }
            });
        }

        for (int i = 1; i < taxCompanyPSIDs.size() + 1; i++) {
            try {
                Future<Void> f = completionService.take();
                f.get();
            } catch (InterruptedException e) {
                logger.error(e);
            } catch (ExecutionException e) {
                logger.error(e);
            }
        }

        System.exit(0);
    }

    public static void migrateSingleCompanyServiceStartDate(final String pPSID, final Double pStartQuarter, boolean pCommit) throws Exception {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            Company company = Company.findCompany(pPSID, SourceSystemCode.QBDT);
            if (company != null) {
                CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
                if (taxService != null && taxService.getServiceStartDate().before(SpcfCalendar.createInstance(2011, 4, 3))) {
                    int year = pStartQuarter.intValue() / 10;
                    int quarter = pStartQuarter.intValue() - year * 10;
                    SpcfCalendar serviceStartDate = CalendarUtils.getFirstDayOfQuarter(year, quarter);
                    taxService.setServiceStartDate(serviceStartDate);
                }
            }
            if (pCommit) {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Exception e) {
            logger.error("Error migrating service start date for company " + pPSID, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


}
