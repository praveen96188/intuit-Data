package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.ACHFileType;
import com.intuit.sbd.payroll.psp.domain.OffloadBatch;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Sep 22, 2008
 * Time: 11:10:01 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "OffloadedTransactionsEvents", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class OffloadedTransactionsEventsProcessor extends JSSBatchJob {
    private String offloadBatchId;
    private SpcfCalendar offloadDate;

    public OffloadedTransactionsEventsProcessor(String[] pArguments) {
        super(pArguments);
        init();
	}
    
	public OffloadedTransactionsEventsProcessor(String[] pArguments, String pJobId) {
	    super(pArguments, pJobId);
	    init();
	}

    public void init() {
        String[] parameters = getJobInstanceParameters().split("%");

        offloadBatchId = parameters[0];

        //Set the time portion to zeroes for comparison purposes and create the date in local time zone
        offloadDate = new SpcfCalendarImpl(
                Integer.parseInt(parameters[1]),
                Integer.parseInt(parameters[2]),
                Integer.parseInt(parameters[3]),
                0,
                0,
                0,
                0,
                SpcfTimeZone.getLocalTimeZone());
    }

    @Override
    public void execute() {

        getLogger().info("Starting update money movement transaction batch job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(UpdateMoneyMovementTransaction.class);
        getLogger().info("Completed update money movement transaction batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

        getLogger().info("Starting update financial transaction batch job");
        timer = StopWatch.startTimer();
        executeStep(UpdateFinancialTransaction.class);
        getLogger().info("Completed update financial transaction batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

        getLogger().info("Starting update payroll status batch job");
        executeStep(UpdatePayrollStatus.class);
        getLogger().info("Completed  update payroll status batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

        getLogger().info("Starting insert financial transaction state batch job");
        timer = StopWatch.startTimer();
        executeStep(InsertFinancialTransactionState.class);
        getLogger().info("Completed insert financial transaction state batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

        getLogger().info("Starting transaction offloaded events batch job");
        timer = StopWatch.startTimer();
        executeStep(CreateTransactionOffloadedEvents.class);
        getLogger().info("Completed transaction offloaded events batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

        executeStep(UpdateTaxPaymentsAgencyStatus.class);
        getLogger().info("Completed update tax payments Agency status batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public static class CreateTransactionOffloadedEvents extends JSSBatchJobStep<OffloadedTransactionsEventsProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FeeEventsBatchJob);
                new com.intuit.sbd.payroll.psp.batchjobs.offload.CreateTransactionOffloadedEvents().createTransactionOffloadedEvents();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CreateTransactionOffloadedEvents ", t);
            }
        }
    }


       public static class UpdatePayrollStatus extends JSSBatchJobStep<OffloadedTransactionsEventsProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FeeEventsBatchJob);
              /* Not sure what why is principal feeEvent */

                Timestamp offloadDateSql = new Timestamp(getBatchJobProcessor().offloadDate.getTimeInMilliseconds());
                getLogger().info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD_UPDATE_PAYROLL.getStoredProcedureName() +
                        " currentPrincipal="+Application.getCurrentPrincipal().getId()+" offloadBatchId="+getBatchJobProcessor().offloadBatchId+" offloadDateSql="+offloadDateSql);
                Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD_UPDATE_PAYROLL, true,
                                                Pair.of(String.class, Application.getCurrentPrincipal().getId()),
                                                Pair.of(Timestamp.class, new Timestamp(SpcfCalendar.getNow().getTimeInMilliseconds())),
                                                Pair.of(String.class, getBatchJobProcessor().offloadBatchId),
                                                Pair.of(Timestamp.class, offloadDateSql));
                getLogger().info("Finished PRC_OFFLOAD_UPDATE_PAYROLL stored procedure returning offload_batch_id = " + getBatchJobProcessor().offloadBatchId);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UpdatePayrollStatus ", t);
            }
        }
       }

    public static class UpdateFinancialTransaction extends JSSBatchJobStep<OffloadedTransactionsEventsProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FeeEventsBatchJob);

                Timestamp offloadDateSql = new Timestamp(getBatchJobProcessor().offloadDate.getTimeInMilliseconds());
                getLogger().info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD_UPDATE_FT.getStoredProcedureName() +
                        " currentPrincipal="+Application.getCurrentPrincipal().getId()+" offloadBatchId="+getBatchJobProcessor().offloadBatchId+" offloadDateSql="+offloadDateSql);
                Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD_UPDATE_FT, true,
                                                Pair.of(String.class, Application.getCurrentPrincipal().getId()),
                                                Pair.of(Timestamp.class, new Timestamp(SpcfCalendar.getNow().getTimeInMilliseconds())),
                                                Pair.of(String.class, getBatchJobProcessor().offloadBatchId),
                                                Pair.of(Timestamp.class, offloadDateSql));
                getLogger().info("Finished PRC_OFFLOAD_UPDATE_FT stored procedure returning offload_batch_id = " + getBatchJobProcessor().offloadBatchId);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step InsertFinancialTransactionState ", t);
            }
        }
    }

    public static class UpdateMoneyMovementTransaction extends JSSBatchJobStep<OffloadedTransactionsEventsProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                getLogger().info("About to call PRC_OFFLOAD_UPDATE_MMT stored procedure");

                Timestamp offloadDateSql = new Timestamp(getBatchJobProcessor().offloadDate.getTimeInMilliseconds());

                PayrollServices.beginUnitOfWork();
                OffloadBatch offloadBatch = Application.findById(OffloadBatch.class, SpcfUniqueId.createInstance(getBatchJobProcessor().offloadBatchId));

                ACHFileType achFileType;
                if (!offloadBatch.getOffloadGroup().getOffloadGroupCd().equals("TXP")) {
                    achFileType = ACHFileType.DD;
                } else {
                    achFileType = ACHFileType.Tax;
                }
                PayrollServices.rollbackUnitOfWork();

                getLogger().info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD_UPDATE_MMT.getStoredProcedureName() +
                        " offloadBatchId="+getBatchJobProcessor().offloadBatchId+" offloadDateSql="+offloadDateSql+" achFileType="+achFileType);
                Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD_UPDATE_MMT, true,
                        Pair.of(String.class, SystemPrincipal.AchOffloadBatchJob.getId()),
                        Pair.of(Timestamp.class, new Timestamp(PSPDate.getPSPTime().getTimeInMilliseconds())),
                        Pair.of(String.class, getBatchJobProcessor().offloadBatchId),
                        Pair.of(Timestamp.class, offloadDateSql),
                        Pair.of(String.class, achFileType.toString()));

                getLogger().info("Finished PRC_OFFLOAD_UPDATE_MMT stored procedure returning offload_batch_id = " + getBatchJobProcessor().offloadBatchId);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UpdateMoneyMovementTransaction ", t);
            }
        }
    }

    public static class UpdateTaxPaymentsAgencyStatus extends JSSBatchJobStep<OffloadedTransactionsEventsProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                getLogger().info("About to call PRC_OFFLOAD_UPD_AGENCY_STATUS stored procedure");

                Timestamp offloadDateSql = new Timestamp(getBatchJobProcessor().offloadDate.getTimeInMilliseconds());

                PayrollServices.beginUnitOfWork();
                OffloadBatch offloadBatch = Application.findById(OffloadBatch.class, SpcfUniqueId.createInstance(getBatchJobProcessor().offloadBatchId));

                ACHFileType achFileType;
                if (!offloadBatch.getOffloadGroup().getOffloadGroupCd().equals("TXP")) {
                    achFileType = ACHFileType.DD;
                } else {
                    achFileType = ACHFileType.Tax;
                }
                PayrollServices.rollbackUnitOfWork();

                if (achFileType.equals(ACHFileType.Tax)) {

                    getLogger().info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD_UPD_AGENCY_STATUS.getStoredProcedureName() +
                            " offloadBatchId="+getBatchJobProcessor().offloadBatchId+" offloadDateSql="+offloadDateSql+" achFileType="+achFileType);
                    Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD_UPD_AGENCY_STATUS, true,
                            Pair.of(String.class, SystemPrincipal.AchOffloadBatchJob.getId()),
                            Pair.of(Timestamp.class, new Timestamp(PSPDate.getPSPTime().getTimeInMilliseconds())),
                            Pair.of(String.class, getBatchJobProcessor().offloadBatchId),
                            Pair.of(Timestamp.class, offloadDateSql),
                            Pair.of(String.class, achFileType.toString()));
                }
                getLogger().info("Finished PRC_OFFLOAD_UPD_AGENCY_STATUS stored procedure returning offload_batch_id = " + getBatchJobProcessor().offloadBatchId);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UpdateTaxPaymentsAgencyStatus ", t);
            }
        }
    }

    public static class InsertFinancialTransactionState extends JSSBatchJobStep<OffloadedTransactionsEventsProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FeeEventsBatchJob);

                Timestamp offloadDateSql = new Timestamp(getBatchJobProcessor().offloadDate.getTimeInMilliseconds());
                getLogger().info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD_INSERT_FTS.getStoredProcedureName() +
                        " currentPrincipal="+Application.getCurrentPrincipal().getId()+" offloadBatchId="+getBatchJobProcessor().offloadBatchId+" offloadDateSql="+offloadDateSql);
                Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD_INSERT_FTS, true,
                        Pair.of(String.class, Application.getCurrentPrincipal().getId()),
                        Pair.of(Timestamp.class, new Timestamp(SpcfCalendar.getNow().getTimeInMilliseconds())),
                        Pair.of(String.class, getBatchJobProcessor().offloadBatchId),
                        Pair.of(Timestamp.class, offloadDateSql));
                getLogger().info("Finished PRC_OFFLOAD_INSERT_FTS stored procedure returning offload_batch_id = " + getBatchJobProcessor().offloadBatchId);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step InsertFinancialTransactionState ", t);
            }
        }
    }
}