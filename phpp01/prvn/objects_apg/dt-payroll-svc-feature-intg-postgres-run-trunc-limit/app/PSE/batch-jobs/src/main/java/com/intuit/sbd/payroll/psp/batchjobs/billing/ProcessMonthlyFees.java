package com.intuit.sbd.payroll.psp.batchjobs.billing;

import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConfigManager;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.intuit.sbd.payroll.psp.Application;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 7/3/12
 * Time: 4:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessMonthlyFees {
    private static SpcfLogger logger = Application.getLogger(ProcessMonthlyFees.class);
    private static final ISpcfImmutableConfiguration sfmConfig;

    static {
        try {
            sfmConfig = ConfigurationManager.getConfiguration(DatabaseConfigManager.MonolithDbToken);

            // load the Oracle jdbc driver
            Class.forName(sfmConfig.getString("dataAccess.connection.driver_class"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to load database driver. ", e);
        }
    }

    public static Connection getDatabaseConnection() throws SQLException {
        String url = sfmConfig.getString("dataAccess.connection.url");
        String user = sfmConfig.getString("dataAccess.connection.username");
        String pass = sfmConfig.getString("dataAccess.connection.password");

        return DriverManager.getConnection(url, user, pass);
    }

    public void process(SpcfCalendar pBillingPeriodReferenceDate) {
        try {
            StopWatch jobTimer = StopWatch.create(true);

            logger.info("Beginning ProcessMonthlyFees batch job...");

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            try {
                Connection con = getDatabaseConnection();
                CompletionService<ProcessMonthlyFeesTask> service = new ExecutorCompletionService<ProcessMonthlyFeesTask>(executor);
                int taskCount = 0;

                try {
                    Statement stmt = con.createStatement();
                    SpcfCalendar fromDate = CalendarUtils.getFirstDayOfMonth(pBillingPeriodReferenceDate);
                    SpcfCalendar toDate = CalendarUtils.getLastDayOfMonth(pBillingPeriodReferenceDate);

                    try {
                        stmt.setFetchSize(1000);

                        //
                        // Select all clients:
                        // - With an Offering that contains a MonthlyFee service charge type
                        //   AND
                        // - Was not billed the MonthlyFee for the given billing period
                        //
                        // This will be our starting point.
                        // ProcessMonthlyFeesTask will handle the details around billing dis/qualification.
                        //
                        
                        String sql = " SELECT DISTINCT co.company_fk " +
                                     "   FROM psp_company_offering co " +
                                     "  WHERE EXISTS " +
                                     "       (SELECT 'T' " +
                                     "          FROM psp_offering_svcchg_grp sc " +
                                     "         WHERE sc.offering_fk = co.offering_fk " +
                                     "           AND sc.applies_to = 'MonthlyFee') " +
                                     "    AND NOT EXISTS " +
                                     "       (SELECT 'T' " +
                                     "          FROM psp_payroll_run pr, " +
                                     "               psp_billing_detail bd, " +
                                     "               psp_financial_transaction ft " +
                                     "         WHERE pr.company_fk = co.company_fk " +
                                     "           AND ft.company_fk = pr.company_fk" +
                                     "           AND bd.payroll_run_fk = pr.payroll_run_seq " +
                                     "           AND ft.billing_detail_fk = bd.billing_detail_seq " +
                                     "           AND ft.transaction_type_fk = 'EmployerFeeDebit' " +
                                     "           AND ft.current_transaction_state_fk IN ('Created', 'Executed', 'Completed', 'Returned') " +
                                     "           AND bd.offering_service_charge_type = 'MonthlyFee' " +
                                     "           AND bd.billing_period BETWEEN TO_DATE ('%1$s', 'yyyymmdd hh24miss') " +
                                     "                                     AND TO_DATE ('%2$s', 'yyyymmdd hh24miss')) ";

                        sql = String.format(sql, fromDate.format("yyyyMMdd HHmmss"), toDate.format("yyyyMMdd HHmmss"));

                        ResultSet rs = stmt.executeQuery(sql);

                        try {
                            while (rs.next()) {
                                service.submit(new ProcessMonthlyFeesTask(rs.getString("company_fk"), toDate));
                                ++taskCount;
                            }
                        } finally {
                            rs.close();
                        }
                    } finally {
                        stmt.close();
                    }
                } finally {
                    con.close();
                }

                //
                // Iterate all the queued tasks (waiting for completion)
                //
                for (int i = 0; i < taskCount; ++i) {
                    service.take();
                }
            } finally {
                ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
            }

            logger.info(String.format("Completed ProcessMonthlyFees batch job in %s", jobTimer.getElapsedTimeString()));
        } catch (Throwable t) {
            logger.fatal("ProcessMonthlyFees batch job processing aborted due to exception: ", t);
        }
    }
}
