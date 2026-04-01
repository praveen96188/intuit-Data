package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments.EoqSUITaxAdjustments;
import com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments.LiabilityAdjustmentsCleanUp;
import com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments.SUIRatePaymentsCleanUp;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.jss.*;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.CacheMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

import static sun.util.calendar.CalendarUtils.mod;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * User: mvillani
 */
@ScheduledJob(name = "EoqSUIAdjustments", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EoqSUIAdjustmentsProcessor extends JSSBatchJob {

    private static boolean mCommit = false;
    private static String companyFile = null;
    private static SpcfCalendar mStartDate = null;
    private static SpcfCalendar mEndDate = null;
    private static SpcfCalendar mStartPaycheckDate = null;
    private static SpcfCalendar mEndPaycheckDate = null;
    private static String mAdjustmentType = null;
    private static int mQuarter = -1;
    private static SpcfCalendar mProcessingDateArg = null;
    private static String mProcessingMessage = null;

    private static final String COMMIT_COMMAND = "-commit";
    private static final String COMPANY_FILE_COMMAND = "-companyFile";
    private static final String START_DATE_COMMAND = "-startDate";
    private static final String END_COMMAND = "-endDate";
    private static final String START_PAYCHECK_DATE_COMMAND = "-startPaycheckDate";
    private static final String END_PAYCHECK_DATE_COMMAND = "-endPaycheckDate";
    private static final String ADJUSTMENT_TYPE_COMMAND = "-adjustmentType";
    private static final String QUARTER_COMMAND = "-quarter";
    private static final String DATE_COMMAND = "-processingDate";
    private static final String MESSAGE_COMMAND = "-message";

    
    public EoqSUIAdjustmentsProcessor(String[] pArguments) {
        super(pArguments);
	}
	public EoqSUIAdjustmentsProcessor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	}

    @Override
    protected void validateRuntimeParameters() {
        SpcfCalendar now = PSPDate.getPSPTime();
        String commandLine = getJobInstanceParameters().trim();
        mProcessingDateArg = now;
        if ( commandLine.length() > 0) {
            String[] args = commandLine.split(" ");
            for (String arg : args) {
                String[] argParts = arg.split(":");
                if (argParts.length == 2) {
                    if (argParts[0].equals(COMPANY_FILE_COMMAND)) {
                        companyFile = argParts[1];

                    } else if (argParts[0].equals(COMMIT_COMMAND)) {
                        mCommit = Boolean.parseBoolean(argParts[1]);

                    } else if (argParts[0].equals(START_DATE_COMMAND)) {
                        mStartDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);

                    } else if (argParts[0].equals(END_COMMAND)) {
                        mEndDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);

                    } else if (argParts[0].equals(START_PAYCHECK_DATE_COMMAND)) {
                        mStartPaycheckDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);

                    } else if (argParts[0].equals(END_PAYCHECK_DATE_COMMAND)) {
                        mEndPaycheckDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);

                    } else if (argParts[0].equals(ADJUSTMENT_TYPE_COMMAND)) {
                        mAdjustmentType = argParts[1];

                    } else if (argParts[0].equals(DATE_COMMAND)) {
                        mProcessingDateArg = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);

                    } else if (argParts[0].equals(MESSAGE_COMMAND)) {
                        mProcessingMessage = argParts[1];

                    } else if (argParts[0].equals(QUARTER_COMMAND)) {
                        mQuarter = Integer.parseInt(argParts[1]);
                        try {
                            SpcfCalendar date = CalendarUtils.getFirstDayOfQuarter(mQuarter / 10, mod(mQuarter, 10));
                            if (date == null) {
                                throw new RuntimeException("Invalid quarter: " + arg + " Format must be YYYYQ");

                            }

                        } catch (Exception e) {
                            throw new RuntimeException("Invalid quarter: " + arg + " Format must be YYYYQ");

                        }

                    }

                }

            }

        }
        if (mQuarter == -1 || mStartPaycheckDate == null || mEndPaycheckDate == null || mStartDate == null) {
            throw new RuntimeException(QUARTER_COMMAND + ", " + START_PAYCHECK_DATE_COMMAND + ", " + END_PAYCHECK_DATE_COMMAND + " and " + START_DATE_COMMAND + " are required paramters");

        }

    }

    @Override
    public void execute() {
        StopWatch timer = StopWatch.startTimer();
        executeStep( SUIRatePaymentsCleanupStep.class);
        executeStep( EoqEmailStep.class);
        executeStep( EoqSUITaxAdjustmnentsStep.class);
        executeStep( LiabilityAdjustmentsCleanupStep.class);
        getLogger().info("Completed EOQ job. Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public static class SUIRatePaymentsCleanupStep  extends JSSBatchJobStep<EoqSUIAdjustmentsProcessor> {
        public void execute() {
        	getLogger().info("Started SUIRatePaymentsCleanupStep");
        	StopWatch timer = StopWatch.startTimer();
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EoqSUITaxAdjustmentsBatchJob);
                try {
                    new SUIRatePaymentsCleanUp().process(companyFile, mCommit, mStartDate, mEndDate, mStartPaycheckDate,
                                                         mEndPaycheckDate, mAdjustmentType);

                } finally {
                    PayrollServices.rollbackUnitOfWork();

                }

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step SUIRatePaymentsCleanup ", t);

            }
            getLogger().info("Completed SUIRatePaymentsCleanupStep. Elapsed time: " + timer.stop().getElapsedTimeString()); 
        }

    }

    public static class EoqEmailStep extends JSSBatchJobStep<EoqSUIAdjustmentsProcessor> {
        @Override
        public void execute() {
        	getLogger().info("Started EoqEmailStep");
        	StopWatch timer = StopWatch.startTimer();
            if (!BatchUtils.getConfigString("psp_eoq_email_list").isEmpty()) {
                try {
                    PayrollServices.beginUnitOfWork();
                    SpcfCalendar beginQuarter = CalendarUtils.getFirstDayOfQuarter(mQuarter / 10, mod(mQuarter, 10));

                    StringBuilder builder = new StringBuilder();
                    builder.append("select source_company_id, fed_tax_id_enc, balance_date, balance_amount" +
                            " from psp_company c"  +
                            " join psp_ledger_balance on company_fk = c.company_seq and" +
                            " ledger_account_fk = 'ERSUITaxDue' and balance_amount <> 0" +
                            " where balance_date = (select max(balance_date)" +
                            " from psp_ledger_balance" +
                            " where company_fk = c.company_seq" +
                            " and ledger_account_fk = 'ERSUITaxDue')" +
                            " and balance_date >= :startDate" +
                            " order by balance_date");
                    org.hibernate.Query query = Application.getHibernateSession().createSQLQuery(builder.toString());
                    CalendarUtils.clearTime(beginQuarter);

                    Timestamp statTimeStamp = new Timestamp(beginQuarter.getTimeInMilliseconds());
                    query.setParameter("startDate", statTimeStamp);

                    ScrollableResults ledgerBalances = query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
                    getBatchJobProcessor().SendEmailToRecipients(ledgerBalances);

                } catch (Throwable t){
                    getLogger().error("Exception in executing step EoqEmailStep",t);
                } finally {
                    PayrollServices.rollbackUnitOfWork();

                }
            }
            getLogger().info("Completed EoqEmailStep. Elapsed time: " + timer.stop().getElapsedTimeString());  
        }

    }

    private void SendEmailToRecipients(ScrollableResults pQueryResults) throws S3ConnectionException,S3UploadException {
        String header = "PSID, Fed Tax Id, Balance Date, Balance Amount\n";
        String filenameExtension = ".csv";
        String tempDir = BatchUtils.getConfigString("psp_batch_temp", "");
        String filename = String.format("EOQ_Report_%s", PSPDate.getPSPTime().format("MMM_yyyy"));
        File tempEoqReportFile = new File(tempDir, filename + filenameExtension);
        if (!tempEoqReportFile.getParentFile().exists()) {
            boolean created = tempEoqReportFile.getParentFile().mkdirs();
            if (!created) {
            	getLogger().error("Unable to create directory for temp EOQ files.");
                return;

            }

        }
        // Write out report to file so it can be attached
        FileWriter writer;
        int numberOfRecords = 0;
        try {
            writer = new FileWriter(tempEoqReportFile);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.append(header);
            while (pQueryResults.next()) {
                String formattedBalanceDate = "";
                numberOfRecords++;
                Timestamp balanceDate = (Timestamp) pQueryResults.get(2);
                if (balanceDate != null) {
                    formattedBalanceDate = StringFormatter.formatDate(SpcfCalendar.createInstance(balanceDate.getTime()), "yyyy/MM/dd");

                }
                Object fedTaxId = EncryptionUtils.deterministicDecrypt(Company.FedTaxIdKeyName,(String)pQueryResults.get(1));
                bufferedWriter.append(pQueryResults.get(0) + "," + fedTaxId + "," + formattedBalanceDate + "," +
                ((BigDecimal) pQueryResults.get(3)).setScale(6, RoundingMode.HALF_EVEN).toString() + "\n");
                  // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(pQueryResults.get());

            }
            bufferedWriter.close();
            writer.close();
            

        } catch (Throwable e) {
            throw new RuntimeException(e);

        } finally {
            pQueryResults.close();

        }
        if (numberOfRecords > 0) {
            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                                 BatchUtils.getConfigString("psp_eoq_email_list"),
                                 BatchUtils.getConfigString("psp_eoq_email_list"),
                                 BatchUtils.getConfigString("psp_eoq_email_subject"),
                                 "EOQ List for " + tempEoqReportFile.getName(),
                                 tempEoqReportFile.getAbsolutePath());

        } else {
        	 getLogger().info("No records found to email in SUI Rate Cleanup");

        }
        //encrypt this file and delete the unencrypted files
        tempEoqReportFile = BatchUtils.encryptFileInStreamsUsingIDPS(tempEoqReportFile);

        //upload to s3
        String batchJobName = BatchJobType.EoqSUIAdjustments.name();
        S3UploadUtils.archive(batchJobName,tempDir,tempEoqReportFile.getAbsolutePath());
       
    }

    public static class EoqSUITaxAdjustmnentsStep extends JSSBatchJobStep<EoqSUIAdjustmentsProcessor> {
        public void execute() {
        	getLogger().info("Started EoqSUITaxAdjustmnentsStep");
        	StopWatch timer = StopWatch.startTimer();
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EoqSUITaxAdjustmentsBatchJob);
                try {
                    new EoqSUITaxAdjustments().process(mProcessingDateArg, mProcessingMessage, mCommit);

                } finally {
                    PayrollServices.rollbackUnitOfWork();

                }

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step EoqSUITaxAdjustmnents ", t);

            }
            
            getLogger().info("Completed EoqSUITaxAdjustmnentsStep. Elapsed time: " + timer.stop().getElapsedTimeString());
        }
        
    }

    public static class LiabilityAdjustmentsCleanupStep extends  JSSBatchJobStep<EoqSUIAdjustmentsProcessor> {
        public void execute() {
        	getLogger().info("Started LiabilityAdjustmentsCleanupStep");
        	StopWatch timer = StopWatch.startTimer();
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.LiabilityAdjustmentsCleanup);
                try {
                    new LiabilityAdjustmentsCleanUp().process(mCommit, mQuarter);

                } finally {
                    PayrollServices.rollbackUnitOfWork();

                }

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step LiabilityAdjustmentsCleanup ", t);

            }
            getLogger().info("Completed LiabilityAdjustmentsCleanupStep. Elapsed time: " + timer.stop().getElapsedTimeString());
        }

    }

    protected static void evictObjectsFromCache(Object[] pObjects) {
        for (Object obj : pObjects) {
            if (obj != null) {
                Application.evict(obj);

            }

        }

    }

    public static void main(String[] args) {
    	JSSBatchJobManager.runJob(BatchJobType.EoqSUIAdjustments.name());
    }

}