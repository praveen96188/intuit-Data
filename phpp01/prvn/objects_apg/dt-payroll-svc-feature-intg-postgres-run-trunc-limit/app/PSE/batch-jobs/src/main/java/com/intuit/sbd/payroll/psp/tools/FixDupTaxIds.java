package com.intuit.sbd.payroll.psp.tools;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConfigManager;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.LiabilityCheck;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: kpaul
 * Date: 10/19/12
 * Time: 12:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class FixDupTaxIds {
    private static SpcfLogger logger = Application.getLogger(FixDupTaxIds.class);
    private static final ISpcfImmutableConfiguration sfmConfig;

    public class ProcessDups implements Callable<ProcessDups> {
        String mCompanyId;
        String mPsid;
        List<String> mLog = new ArrayList<String>();

        public ProcessDups(String pCompanyId) {
            mCompanyId = pCompanyId;
        }

        public void logResults(SpcfLogger pLogger) {
            StringBuilder sb = new StringBuilder();

            for (String logEntry : mLog) {
                sb.append(String.format("%s%n", logEntry));
            }

            pLogger.info(sb.toString());
            System.out.println(sb.toString());
        }

        public ProcessDups call() throws Exception {
            Application.beginUnitOfWork();

            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.BatchJob);

                Company company = Application.findById(Company.class, SpcfUniqueId.createInstance(mCompanyId));

                if (company == null) {
                    mLog.add(String.format("Error: Could not find company with id %s", mCompanyId));
                    return this;
                }

                mPsid = company.getSourceCompanyId();

                Connection con = getDatabaseConnection();

                try {
                    Statement stmt = con.createStatement();

                    try {
                        stmt.setFetchSize(1000);

                        String sql =
                                " SELECT DISTINCT lc.liability_check_seq " +
                                "   FROM psp_liability_check lc, psp_comp_adjust_submission cas " +
                                "  WHERE lc.created_date >= " +
                                        Application.getUTCTimeExtractString("TO_TIMESTAMP ('20120929000000', 'yyyymmddhh24miss')") +
                                "    AND lc.company_fk = '%s' " +
                                "    AND lc.company_fk = cas.company_fk " +
                                "    AND lc.source_id = cas.source_id ";

                        ResultSet rs = stmt.executeQuery(String.format(sql, mCompanyId));

                        try {
                            mLog.add(String.format("Processing duplicate transaction id's for psid %s (company id %s)", mPsid, mCompanyId));
                            mLog.add(String.format("  Starting tokens are: CurrentToken = %d, NextPayrollTransactionId = %s", company.getCurrentToken(), company.getNextPayrollTransactionId()));

                            while (rs.next()) {
                                String lcId = rs.getString(1);
                                Query<LiabilityCheck> query = new Query<LiabilityCheck>();

                                query.Where(LiabilityCheck.Id().equalTo(SpcfUniqueId.createInstance(lcId)))
                                     .EagerLoad(LiabilityCheck.QbdtTransactionInfo());

                                DomainEntitySet<LiabilityCheck> lcSet = Application.find(LiabilityCheck.class, query);

                                if (lcSet.isEmpty()) {
                                    mLog.add(String.format("  Error: Unable to locate LiabilityCheck record with id %s (skipping record)", lcId));
                                    continue;
                                }

                                LiabilityCheck lc = lcSet.get(0);

                                mLog.add(String.format("  Updating LiabilityCheck with id %s from source id %s to source id %s", lc.getId().toString(), lc.getSourceId(), company.getNextPayrollTransactionId()));

                                // updates both next transaction token and current token
                                lc.setSourceId(company.getNextPayrollTransactionId());

                                Application.save(lc);
                            }

                            company = Application.save(company);

                            mLog.add(String.format("  Ending tokens are: CurrentToken = %d, NextPayrollTransactionId = %s", company.getCurrentToken(), company.getNextPayrollTransactionId()));
                        } finally {
                            rs.close();
                        }
                    } finally {
                        stmt.close();
                    }
                } finally {
                    con.close();
                }

                Application.commitUnitOfWork();
            } finally {
                Application.rollbackUnitOfWork();
            }

            return this;
        }
    }

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

    public void process() {
        try {
            StopWatch jobTimer = StopWatch.create(true);

            Application.initialize();
            ApplicationSecondary.initialize();
            logger.info("Beginning FixDupTaxIds batch job...");
            System.out.println("Beginning FixDupTaxIds batch job...");

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            try {
                Connection con = getDatabaseConnection();
                CompletionService<ProcessDups> service = new ExecutorCompletionService<ProcessDups>(executor);
                int taskCount = 0;

                try {
                    Statement stmt = con.createStatement();

                    try {
                        String sql;
                        ResultSet rs;
                        int cmpCount = 0;
                        int dupCount = 0;

                        stmt.setFetchSize(1000);

                        sql = " SELECT COUNT (DISTINCT lc.company_fk), " +
                              "        COUNT (DISTINCT lc.liability_check_seq) " +
                              "   FROM psp_liability_check lc, psp_comp_adjust_submission cas " +
                              "  WHERE lc.created_date >= " +
                                Application.getUTCTimeExtractString("TO_TIMESTAMP ('20120929000000', 'yyyymmddhh24miss')") +
                              "    AND lc.company_fk = cas.company_fk " +
                              "    AND lc.source_id = cas.source_id ";

                        rs = stmt.executeQuery(sql);

                        try {
                            while (rs.next()) {
                                cmpCount = rs.getInt(1);
                                dupCount = rs.getInt(2);
                            }
                        } finally {
                            rs.close();
                        }

                        if ((cmpCount == 0) || (dupCount == 0)) {
                            logger.info("No companies found with duplicate transaction id's (exiting, no action taken)");
                            System.out.println("No companies found with duplicate transaction id's (exiting, no action taken)");
                        } else {
                            jobTimer.stop();

                            logger.info(String.format("Processing %d companies with %d duplicate transaction id's (elapsed %s)...", cmpCount, dupCount, jobTimer.getElapsedTimeString()));
                            System.out.println(String.format("Processing %d companies with %d duplicate transaction id's (elapsed %s)...", cmpCount, dupCount, jobTimer.getElapsedTimeString()));

                            jobTimer.start(); // restart timer so only tracking elapsed time for work and not above stats...

                            sql = " SELECT DISTINCT lc.company_fk " +
                                  "   FROM psp_liability_check lc, psp_comp_adjust_submission cas " +
                                  "  WHERE lc.created_date >= " +
                                    Application.getUTCTimeExtractString("TO_TIMESTAMP ('20120929000000', 'yyyymmddhh24miss')") +
                                  "    AND lc.company_fk = cas.company_fk " +
                                  "    AND lc.source_id = cas.source_id ";

                            rs = stmt.executeQuery(sql);

                            try {
                                while (rs.next()) {
                                    service.submit(new ProcessDups(rs.getString(1)));
                                    ++taskCount;
                                }
                            } finally {
                                rs.close();
                            }
                        }
                    } finally {
                        stmt.close();
                    }
                } finally {
                    con.close();
                }

                //
                // Iterate all the queued tasks (waiting for completion) and log the results
                //
                for (int i = 0; i < taskCount; ++i) {
                    Future<ProcessDups> task = service.take();
                    task.get().logResults(logger);
                }
            } finally {
                ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
            }

            jobTimer.stop();

            logger.info(String.format("Completed FixDupTaxIds batch job in %s", jobTimer.getElapsedTimeString()));
            System.out.println(String.format("Completed FixDupTaxIds batch job in %s", jobTimer.getElapsedTimeString()));
        } catch (Throwable t) {
            logger.fatal("FixDupTaxIds batch job processing aborted due to exception: ", t);
            t.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new FixDupTaxIds().process();
        } catch (Throwable t) {
            logger.error("Unexpected exception in FixDupTaxIds ", t);
            t.printStackTrace();
        }
    }
}
