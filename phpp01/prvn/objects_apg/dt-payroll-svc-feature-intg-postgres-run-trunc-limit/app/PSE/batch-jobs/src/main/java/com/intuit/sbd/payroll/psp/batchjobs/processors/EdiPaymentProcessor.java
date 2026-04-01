package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 14, 2011
 * Time: 4:35:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class EdiPaymentProcessor extends BatchJobProcessor {
    private SpcfCalendar initiationDate;
    private List<Integer> paymentFileIds = new ArrayList<Integer>();
    private String[] reinitiationRejectCodes = new String[]{};

    public EdiPaymentProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
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

        if ((getRunMode() == RunMode.UsingFlux) || (commandLine.length() == 0)) {
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
            logger.warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        logger.info("Starting edi process payments job");

        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiPaymentsBatchJob);

        executeStep(new MarkPaymentsAsProcessingStep());
        executeStep(new GeneratePaymentFileStep());
        executeStep(new MarkPaymentsAsSent());
        executeStep(new RecordPaymentEvents());

        logger.info("Completed Edi payments process batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class MarkPaymentsAsProcessingStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                StopWatch sw = StopWatch.startTimer();
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiPaymentsBatchJob);
                PayrollServices.beginUnitOfWork();
                int maxRecordsToProcess = EftpsUtil.getMaxPaymentsToProcessPerBatchRun(); // This is the limit used in Eftps payments processing batch job, 100,000. MS - EDI payments we can use the same
                int rowsUpdated = MoneyMovementTransaction.markPaymentsInProcessForDate(PaymentMethod.EDI,
                                                                                        getInitiationDate(),
                                                                                        maxRecordsToProcess);
                PayrollServices.commitUnitOfWork();
                logger.info(
                        "updated " + rowsUpdated + " payment MoneyMovementTransactions to status InProcess for payment method EDI in " + sw
                                .getElapsedTimeString());
                if (maxRecordsToProcess == rowsUpdated) {
                    logger.error(
                            "NOTICE: the EdiPayments batch job *must* be run again after this execution completes - maximum number of payments in a batch run has been met: " + maxRecordsToProcess + ".");
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step MarkPaymentsAsProcessing ", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }


    public class GeneratePaymentFileStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiPaymentsBatchJob);
                // unit of work is controlled w/in EdiManager.processEDIPayments since it can create multiple files
                setPaymentFileIds(EdiManager.processEDIPayments(getInitiationDate()));
                logger.info("generated files with file_ids= " + getPaymentFileIds());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step GeneratePaymentFile - : EDI" , t);
            }
        }
    }

    public class MarkPaymentsAsSent extends BatchJobProcessorStep {
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiPaymentsBatchJob);
            for (Integer paymentFileId : getPaymentFileIds()) {
                try {
                    PayrollServices.beginUnitOfWork();
                    EdiManager.markEDIPaymentsAsSent(paymentFileId);
                    PayrollServices.commitUnitOfWork();
                } catch (Throwable t) {
                    logger.error("error in MarkPaymentsAsSent - file control number=" + paymentFileId, t);
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
        }
    }

    public class RecordPaymentEvents extends BatchJobProcessorStep {
        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiPaymentsBatchJob);
            for (Integer paymentFileId : getPaymentFileIds()) {
                try {
                    PayrollServices.beginUnitOfWork();
                    logger.info("create company events for the payments included in File Id = " + paymentFileId);
                    EdiManager.insertPaymentSentStatusChangeEventForEDIPayments(paymentFileId);
                    PayrollServices.commitUnitOfWork();
                } catch (Throwable t) {
                    logger.error("error in RecordPaymentEvents - file control number= " + paymentFileId, t);
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
