package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.fset.FsetManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpFsetConnection;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * User: ihannur
 * Date: 9/12/12
 * Time: 3:46 PM
 */
@ScheduledJob(name = "FsetFilingProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class FsetFilingProcessor extends JSSBatchJob {

    private static SpcfCalendar initiationDate;


	public FsetFilingProcessor(String[] pArguments) {
	        super(pArguments);
	}
	public FsetFilingProcessor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	}
    public void setInitiationDate(SpcfCalendar pInitiationDate) {
        initiationDate = pInitiationDate;
    }

    @Override
    protected void validateRuntimeParameters() {
        SpcfCalendar now = PSPDate.getPSPTime();
        String commandLine = getJobInstanceParameters().trim();

       if ((commandLine.length() == 0)) {
            initiationDate = now;
        } else {
        
            String[] args = commandLine.split(" ");

            if (args.length > 0) {
                for (String arg : args) {
                    // date must be formatted as yyyyMMdd (more precisely, the format must be 20yyMMdd)
                    if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                        SpcfCalendar clDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);

                        initiationDate = SpcfCalendar.createInstance(clDate.getYear(),
                                                                     clDate.getMonth(),
                                                                     clDate.getDay(),
                                                                     now.getHour(),
                                                                     now.getMinute(),
                                                                     now.getSecond(),
                                                                     now.getMillisecond(),
                                                                     SpcfTimeZone.getLocalTimeZone());
                    }
                }
            }

            if (initiationDate == null) {
                initiationDate = MoneyMovementTransaction.getNextInitiationDate(PaymentMethod.ACHCredit);
            }
       }
        CalendarUtils.clearTime(initiationDate);
    }

    @Override
    protected void execute() {
        if (CalendarUtils.isHoliday(initiationDate)) {
        	getLogger().warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        getLogger().info("Starting FSET Filing job");

        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);

        executeStep(GenerateFsetReturnFileStep.class);
        executeStep(TransmitPendingTransmissionFilesStep.class);
        executeStep(ArchiveFsetFilesStep.class);

        getLogger().info("Completed FSET Filing process batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class GenerateFsetReturnFileStep extends JSSBatchJobStep<FsetFilingProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);
                PayrollServices.beginUnitOfWork();

                String[] paramNames = new String[1];
                Object[] paramValues = new Object[1];

                paramNames[0] = "InitDate";
                paramValues[0] = initiationDate;

                DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                        Application.findByNamedQuery("findPaymentsForFsetFiling", paramNames, paramValues);

                if(moneyMovementTransactions.isNotEmpty()) {
                    new FsetManager().createFsetReturnFile(moneyMovementTransactions);
                }

                PayrollServices.commitUnitOfWork();
                getLogger().info("generated FSET filing files.");
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step GenerateFsetReturnFileStep - FSET", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static class TransmitPendingTransmissionFilesStep extends JSSBatchJobStep<FsetFilingProcessor>  {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);
                PayrollServices.beginUnitOfWork();
                DomainEntitySet<FsetFile> fsetFiles = Application.find(FsetFile.class, FsetFile.StatusCd().equalTo(FsetFileStatus.PendingTransmission));
                PayrollServices.commitUnitOfWork();

                SftpFsetConnection sftpFsetConnection = new SftpFsetConnection();
                sftpFsetConnection.upload(fsetFiles);

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step TransmitPendingTransmissionFilesStep - FSET", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static class ArchiveFsetFilesStep extends JSSBatchJobStep<FsetFilingProcessor>  {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);
                new FsetManager().archiveFsetFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveFsetFilesStep - FSET", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

}
