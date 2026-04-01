package com.intuit.sbd.payroll.psp.migration;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 18, 2011
 * Time: 7:27:58 PM
 */
public class MigrateAssistedTokens {

    private static SpcfLogger logger = Application.getLogger(MigrateAssistedTokens.class);

    private static Connection connection;
    private static ThreadLocal<PreparedStatement> companyPreparedStatement = new ThreadLocal<PreparedStatement>();
    private static ThreadLocal<PreparedStatement> employeePreparedStatement = new ThreadLocal<PreparedStatement>();
    private static ThreadLocal<PreparedStatement> payrollItemPreparedStatement = new ThreadLocal<PreparedStatement>();

    public static void main(String args[]) throws Exception {
        try {
            StopWatch sw = new StopWatch().start();

            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));

            logger.info("beginning token migration from AS400...");

            if (args.length < 1 || args.length > 2) {
                throw new RuntimeException("Usage MigrateAssistedTokens [commit: true | false] [(optional) single company psid]");
            }

            boolean commit = Boolean.parseBoolean(args[0]);
            String sourceCompanyId = null;
            if(args.length > 1) {
                sourceCompanyId = args[1];
            }

            if(sourceCompanyId != null) {
                migrateSingleCompanyAssistedTokens(sourceCompanyId, commit);
            } else {
                migrateTokensForAllCompanies(commit);
            }

            logger.info(" migrated tokens in " + sw.getElapsedTimeString());
        } catch (Throwable t) {
            logger.fatal("Fatal error during token migration", t);
            System.exit(-1);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            if(connection != null) {
                connection.close();
            }
        }
    }

    private static void migrateTokensForAllCompanies(final boolean pCommit) throws Throwable {
        Statement companiesStatement = null;
        ResultSet companiesResultSet = null;
        List<String> taxCompanyPSIDs = new ArrayList<String>();
        try {
            companiesStatement = connection.createStatement();
            companiesResultSet = companiesStatement.executeQuery("select userid " +
                    "from iqclient " +
                    "where cli_status not in('CC', 'TI')");
            while(companiesResultSet.next()) {
                taxCompanyPSIDs.add(companiesResultSet.getString(1));
            }
        } catch (Exception e) {
            logger.error("FAILED TO INITIALIZE", e);
            System.exit(-1);
        } finally {
            if(companiesStatement != null) {
                companiesStatement.close();
            }
            if(companiesResultSet != null) {
                companiesResultSet.close();
            }
        }

        Executor executor = Executors.newFixedThreadPool(8);
        CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executor);

        for (final String taxCompanyPSID : taxCompanyPSIDs) {
            completionService.submit(new Callable<Void>() {
                public Void call() throws Exception {
                    migrateSingleCompanyAssistedTokens(taxCompanyPSID, pCommit);
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

    public static void migrateSingleCompanyAssistedTokens(String pPSID, boolean pCommit) throws Exception {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            Company company = Company.findCompany(pPSID, SourceSystemCode.QBDT);

            if(company.getQuickbooksInfo() != null) {
                company.getQuickbooksInfo().setToken(getCompanyToken(pPSID));
            }

            Map<String, Long> employeeIdTokens = getEmployeeTokens(pPSID);
            DomainEntitySet<Employee> employees = Application.find(Employee.class, Employee.Company().equalTo(company));
            for (String sourceId : employeeIdTokens.keySet()) {
                Employee employee = employees.findEntity(Employee.SourceEmployeeId().equalTo(sourceId));
                if(employee == null) {
                    logger.error("Employee '" + sourceId + "' was not found for company " + pPSID);
                    continue;
                }
                employee.getQbdtEmployeeInfo().setToken(employeeIdTokens.get(sourceId));
                Application.save(employee);
            }

            Map<String, Long> payrollItemIdTokens = getPayrollItemTokens(pPSID);
            DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class, CompanyPayrollItem.Company().equalTo(company));
            DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class, CompanyLaw.QbdtPayrollItemInfo().Company().equalTo(company));
            for (String sourceId : payrollItemIdTokens.keySet()) {
                QbdtPayrollItemInfo qbdtPayrollItemInfo;
                CompanyPayrollItem companyPayrollItem = payrollItems.findEntity(CompanyPayrollItem.SourcePayrollItemId().equalTo(sourceId));
                if(companyPayrollItem != null) {
                    qbdtPayrollItemInfo = companyPayrollItem.getQbdtPayrollItemInfo();
                } else {
                    CompanyLaw companyLaw = companyLaws.findEntity(CompanyLaw.SourceId().equalTo(sourceId));
                    if(companyLaw == null) {
                        logger.error("Payroll item '" + sourceId + "' was not found for company " + pPSID);
                        continue;
                    }
                    qbdtPayrollItemInfo = companyLaw.getQbdtPayrollItemInfo();
                }
                qbdtPayrollItemInfo.setToken(payrollItemIdTokens.get(sourceId));
                Application.save(qbdtPayrollItemInfo);
            }

            if(pCommit) {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Exception e) {
            logger.error("Error migrating tokens for company " + pPSID, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private static Long getCompanyToken(String pPSID) throws Exception {
        PreparedStatement preparedStatement = companyPreparedStatement.get();

        ResultSet resultSet = null;
        try {
            preparedStatement.setLong(1, new Long(pPSID));
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong("CLI_TOKEN");
            }
        } finally {
            if(resultSet != null) {
                resultSet.close();
            }
        }

        return -1L;
    }

    private static Map<String, Long> getEmployeeTokens(String pPSID) throws Exception {
        PreparedStatement preparedStatement = employeePreparedStatement.get();

        ResultSet resultSet = null;
        Map<String, Long> employeeIdTokenMap = new HashMap<String, Long>();
        try {
            preparedStatement.setLong(1, new Long(pPSID));
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                employeeIdTokenMap.put(resultSet.getString("EMP_EMPID").trim(), resultSet.getLong("EMP_TOKEN"));
            }
        } finally {
            if(resultSet != null) {
                resultSet.close();
            }
        }

        return employeeIdTokenMap;
    }

    private static Map<String, Long> getPayrollItemTokens(String pPSID) throws Exception {
       PreparedStatement preparedStatement = payrollItemPreparedStatement.get();

        ResultSet resultSet = null;
        Map<String, Long> payrollItemIdTokenMap = new HashMap<String, Long>();
        try {
            preparedStatement.setLong(1, new Long(pPSID));
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                payrollItemIdTokenMap.put(resultSet.getString("PIT_PITEMID").trim(), resultSet.getLong("PIT_TOKEN"));
            }
        } finally {
            if(resultSet != null) {
                resultSet.close();
            }
        }

        return payrollItemIdTokenMap;
    }
}
