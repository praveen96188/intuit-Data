package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

import org.hibernate.Query;

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Sep 22, 2008
 * Time: 11:10:01 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "LedgerBalance", resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class,  scheduleGenerator = JSSScheduleGenerator.class)
public class LedgerBalanceProcessor extends JSSBatchJob {
	
	public LedgerBalanceProcessor(String[] pArguments) {
        super(pArguments);
    }

    public LedgerBalanceProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public void execute() {
        getLogger().info("Starting ledger balance batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(UpdateLedgerBalance.class);

        getLogger().info("Completed ledger balance batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class UpdateLedgerBalance extends JSSBatchJobStep<LedgerBalanceProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.LedgerBalanceBatchJob);

                getLogger().info("Updating ledger balance, starting from LedgerBalanceStartDate="+getStartDateForNextLedgerBalance());

                try {
                    PayrollServices.beginUnitOfWork();

                    getLogger().info("Calling storedProcedure="+StoredProcedures.PRC_UPDATE_LEDGER_BALANCE.getStoredProcedureName());
                    Application.executeSqlProcedure(StoredProcedures.PRC_UPDATE_LEDGER_BALANCE, true);

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UpdateLedgerBalance ", t);
            }
        }

        public Timestamp getStartDateForNextLedgerBalance(){
            Timestamp startDateForLedgerBalance = null;

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT MAX(BALANCE_DATE) + interval '1' day \n")
               .append("FROM PSP_LEDGER_BALANCE\n");

            try {
                Application.beginUnitOfWork();
                Query query = Application.getHibernateSession().createSQLQuery(sql.toString());
                startDateForLedgerBalance = ((Timestamp) query.list().get(0));

            }finally {
                Application.rollbackUnitOfWork();
            }

            return  startDateForLedgerBalance;
        }
    }

}