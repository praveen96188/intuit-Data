package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 14, 2011
 * Time: 4:35:44 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "EdiPayment", resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EdiPaymentProcessor extends JSSBatchJob {
    private SpcfCalendar initiationDate;
    private List<Integer> paymentFileIds = new ArrayList<Integer>();
    private String[] reinitiationRejectCodes = new String[]{};

    public EdiPaymentProcessor(String[] pArguments) {
        super(pArguments);
   }
   public EdiPaymentProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
   }

    public SpcfCalendar getInitiationDate() {
        return initiationDate;
    }

    public void setInitiationDate(SpcfCalendar pInitiationDate) {
        initiationDate = pInitiationDate;
    }

    public List<Integer> getPaymentFileIds() {
        return paymentFileIds;
    }

    public void setPaymentFileIds(List<Integer> pFileIdList) {
        paymentFileIds = pFileIdList;
    }

    public String[] getReinitiationRejectCodes() {
        return reinitiationRejectCodes;
    }

    public void setReinitiationRejectCodes(String[] pReinitiationRejectCodes) {
        reinitiationRejectCodes = pReinitiationRejectCodes;
    }

    @Override
    protected void validateRuntimeParameters() {
        SpcfCalendar now = PSPDate.getPSPTime();
        SpcfCalendar initiationDate = null;
        String commandLine = getJobInstanceParameters().trim();

        if ( commandLine.length() == 0) {
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
                initiationDate = MoneyMovementTransaction.getNextInitiationDate(PaymentMethod.EDI);
            }
        }

        setInitiationDate(initiationDate);

    }

    @Override
    protected void validateStepRuntimeParameters(String stepName) {
        validateRuntimeParameters();
        if (stepName.equals(MarkPaymentsAsSent.class.getSimpleName()) || stepName.equals(MarkPaymentsAsSent.class.getSimpleName())) {
            if (paymentFileIds == null || paymentFileIds.size() == 0) {
                throw new RuntimeException(
                        "no FileIds= argument found.  These value(s) are required for marking payments as sent.  Values are found with SELECT FILE_ID, EF.* from PSP_EDI_TAX_FILE EF where FILE_TYPE = 'StateEdiPayment' and TRUNC(CREATED_DATE) = date 'YYYY-MM-DD'");
            }
        }
    }

    @Override
    protected void execute() {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
        	getLogger().warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        getLogger().info("Starting edi process payments job");

        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiPaymentsBatchJob);

        executeStep(MarkPaymentsAsProcessingStep.class);
        executeStep( GeneratePaymentFileStep.class);
        executeStep(MarkPaymentsAsSent.class);
        executeStep(RecordPaymentEvents.class); 

        getLogger().info("Completed Edi payments process batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class MarkPaymentsAsProcessingStep extends   JSSBatchJobStep<EdiPaymentProcessor> {
        public void execute() {
            try {
                StopWatch sw = StopWatch.startTimer();
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiPaymentsBatchJob);
                PayrollServices.beginUnitOfWork();
                int maxRecordsToProcess = EftpsUtil.getMaxPaymentsToProcessPerBatchRun(); // This is the limit used in Eftps payments processing batch job, 100,000. MS - EDI payments we can use the same
                int rowsUpdated = MoneyMovementTransaction.markPaymentsInProcessForDate(PaymentMethod.EDI,
                		getBatchJobProcessor().getInitiationDate(),
                                                                                        maxRecordsToProcess);
                PayrollServices.commitUnitOfWork();
                getLogger().info(
                        "updated " + rowsUpdated + " payment MoneyMovementTransactions to status InProcess for payment method EDI in " + sw
                                .getElapsedTimeString());
                if (maxRecordsToProcess == rowsUpdated) {
                    getLogger().error(
                            "NOTICE: the EdiPayments batch job *must* be run again after this execution completes - maximum number of payments in a batch run has been met: " + maxRecordsToProcess + ".");
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step MarkPaymentsAsProcessing ", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }


    public static class GeneratePaymentFileStep extends JSSBatchJobStep<EdiPaymentProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiPaymentsBatchJob);
                // unit of work is controlled w/in EdiManager.processEDIPayments since it can create multiple files
                getBatchJobProcessor().setPaymentFileIds(EdiManager.processEDIPayments(getBatchJobProcessor().getInitiationDate()));
                getLogger().info("generated files with file_ids= " + getBatchJobProcessor().getPaymentFileIds());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step GeneratePaymentFile - : EDI" , t);
            }
        }
    }

    public static class MarkPaymentsAsSent extends JSSBatchJobStep<EdiPaymentProcessor> {
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiPaymentsBatchJob);
            for (Integer paymentFileId : getBatchJobProcessor().getPaymentFileIds()) {
                try {
                    PayrollServices.beginUnitOfWork();
                    EdiManager.markEDIPaymentsAsSent(paymentFileId);
                    PayrollServices.commitUnitOfWork();
                } catch (Throwable t) {
                    getLogger().error("error in MarkPaymentsAsSent - file control number=" + paymentFileId, t);
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
        }
    }

    public static class RecordPaymentEvents extends JSSBatchJobStep<EdiPaymentProcessor> {
        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiPaymentsBatchJob);
            for (Integer paymentFileId : getBatchJobProcessor().getPaymentFileIds()) {
                try {
                    PayrollServices.beginUnitOfWork();
                    getLogger().info("create company events for the payments included in File Id = " + paymentFileId);
                    EdiManager.insertPaymentSentStatusChangeEventForEDIPayments(paymentFileId);
                    PayrollServices.commitUnitOfWork();
                } catch (Throwable t) {
                    getLogger().error("error in RecordPaymentEvents - file control number= " + paymentFileId, t);
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
        }
    }
    public static void main(String[] args) {
        BatchJobManager.runJobStep(BatchJobType.EdiPayment, GeneratePaymentFileStep.class);
    } 
           
}
